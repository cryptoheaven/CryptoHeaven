/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_gui.dialog;

import com.CH_gui.util.*;
import com.CH_gui.service.records.ContactRecUtil;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.cryptx.*;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.cnt.*;
import com.CH_co.service.msg.dataSets.key.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.frame.MainFrame;
import com.CH_gui.gui.*;
import com.CH_gui.service.records.RecordUtilsGui;
import com.CH_guiLib.gui.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.border.*;
import javax.swing.*;
import javax.swing.event.*;

/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.29 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class ContactPropertiesDialog extends GeneralDialog { 

  private static final int DEFAULT_OK_INDEX = 0;
  private static final int DEFAULT_CANCEL_INDEX = 2;

  private boolean amIOwner;
  private boolean canAlterContact;
  private JTextField jContactName;
  private JLabel jContactCreator;
  private JLabel jContactOwner;
  private JLabel jContactWith;
  private JLabel jOwnerEncryption;
  private JLabel jOtherEncryption;
  private JCheckBox jAllowMessaging;
  private JCheckBox jAllowFolderSharing;
  private JCheckBox jNotifyOfOnlineStatus;
  private JCheckBox jEnableAudibleOnlineNotify;
  private JLabel jPermissionsLabel;

  private JButton jOk;

  private ServerInterfaceLayer SIL;
  private FetchedDataCache cache;

  private static String FETCHING_DATA = com.CH_gui.lang.Lang.rb.getString("Fetching_Data...");

  private ContactRecord contactRecord;

  private DocumentChangeListener documentChangeListener;

  // used to switch between two related properties dialogs...
  private ContactPropertiesDialog otherContactPropertiesDialog;
  private Component parentWindow;

  // track if dialog close method was called to prevent certain updates
  private boolean isClosed;
  private final Object monitor = new Object();


  /** Creates new ContactPropertiesDialog */
  public ContactPropertiesDialog(Frame owner, ContactRecord contactRecord) {
    super(owner, com.CH_gui.lang.Lang.rb.getString("title_Contact_Properties"));
    constructDialog(owner, contactRecord);
  }
  public ContactPropertiesDialog(Dialog owner, ContactRecord contactRecord) {
    super(owner, com.CH_gui.lang.Lang.rb.getString("title_Contact_Properties"));
    constructDialog(owner, contactRecord);
  }
  private void constructDialog(Component owner, ContactRecord contactRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactPropertiesDialog.class, "constructDialog(Component owner, ContactRecord contactRecord)");
    if (trace != null) trace.args(owner, contactRecord);

    this.parentWindow = owner;
    this.contactRecord = contactRecord;
    this.SIL = MainFrame.getServerInterfaceLayer();
    this.cache = FetchedDataCache.getSingleInstance();

    UserRecord myUser = cache.getUserRecord();
    Long myUserId = myUser.userId;
    this.amIOwner = myUserId.equals(contactRecord.ownerUserId);

    boolean isGivenContact = contactRecord.isGiven();
    this.canAlterContact = !amIOwner && (!isGivenContact || (myUser.flags.longValue() & UserRecord.FLAG_ENABLE_GIVEN_CONTACTS_ALTER) != 0);

    String contactName = amIOwner ? contactRecord.getOwnerNote() : contactRecord.getOtherNote();
    setTitle(java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("title_OBJECT_-_Contact_Properties"), new Object[] {contactName}));

    JButton[] buttons = createButtons();
    JComponent mainComponent = createMainComponent();
    init(owner, buttons, new JScrollPane(mainComponent), DEFAULT_OK_INDEX, DEFAULT_CANCEL_INDEX);

    jContactName.requestFocus();
    setEnabledButtons();
    fetchData();

    if (trace != null) trace.exit(ContactPropertiesDialog.class);
  }

  private void fetchData() {
    fetchHandles();
    fetchPubKeys();
  }

  private void fetchHandles() {
    Thread th = new ThreadTraced("Contact Properties Get Handles") {
      public void runTraced() {
        Obj_IDList_Co request = new Obj_IDList_Co();
        request.IDs = new Long[] { contactRecord.ownerUserId, contactRecord.contactWithId, contactRecord.creatorId };
        request.IDs = (Long[]) ArrayUtils.removeDuplicates(request.IDs);
        MessageAction msgAction = new MessageAction(CommandCodes.USR_Q_GET_HANDLES, request);
        ClientMessageAction replyMsg = SIL.submitAndFetchReply(msgAction, 30000);
        DefaultReplyRunner.nonThreadedRun(SIL, replyMsg);

        if (replyMsg != null) {
          synchronized (monitor) {
            if (!isClosed) {
              synchronized (getTreeLock()) {
                if (replyMsg.getActionCode() == CommandCodes.USR_A_GET_HANDLES) {
                  UserRecord uRec = cache.getUserRecord(contactRecord.creatorId);
                  if (uRec != null) {
                    jContactCreator.setText(uRec.shortInfo());
                    jContactCreator.setIcon(RecordUtilsGui.getIcon(uRec));
                  } else {
                    jContactCreator.setText("Unknown User ("+contactRecord.creatorId+")");
                  }
                  String handleFor = null;
                  String handleBy = null;
                  uRec = cache.getUserRecord(contactRecord.ownerUserId);
                  if (uRec != null) {
                    handleFor = "'" + uRec.handle + "'";
                    jContactOwner.setText(uRec.shortInfo());
                    jContactOwner.setIcon(RecordUtilsGui.getIcon(uRec));
                  } else {
                    handleFor = "'Unknown User ("+contactRecord.ownerUserId+")'";
                    jContactOwner.setText("Unknown User ("+contactRecord.ownerUserId+")");
                  }
                  uRec = cache.getUserRecord(contactRecord.contactWithId);
                  if (uRec != null) {
                    handleBy = "'" + uRec.handle + "'";
                    jContactWith.setText(uRec.shortInfo());
                    jContactWith.setIcon(RecordUtilsGui.getIcon(uRec));
                  } else {
                    handleBy = "'Unknown User ("+contactRecord.contactWithId+")'";
                    jContactWith.setText("Unknown User ("+contactRecord.contactWithId+")");
                  }
                  jPermissionsLabel.setText(java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("label_Permissions_for_USER1_set_by_USER2"), new Object[] {handleFor, handleBy}));
                  setEnabledButtons();
                }
              } // end synchronized
            }
          } // end synchronized
        }
      }
    };
    th.setDaemon(true);
    th.start();
  }

  private void fetchPubKeys() {
    Thread th = new ThreadTraced("Contact Properties Get Public Key") {
      public void runTraced() {
        Obj_IDList_Co request = new Obj_IDList_Co();

        request.IDs = new Long[] { contactRecord.ownerUserId, contactRecord.contactWithId };

        MessageAction msgAction = new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_USERS, request);
        ClientMessageAction replyMsg = SIL.submitAndFetchReply(msgAction, 30000);
        DefaultReplyRunner.nonThreadedRun(SIL, replyMsg);

        if (replyMsg != null) {
          synchronized (monitor) {
            if (!isClosed) {
              synchronized (getTreeLock()) {
                if (replyMsg.getActionCode() == CommandCodes.KEY_A_GET_PUBLIC_KEYS) {
                  Key_PubKeys_Rp replyData = (Key_PubKeys_Rp) replyMsg.getMsgDataSet();
                  KeyRecord[] kRecs = replyData.keyRecords;
                  if (kRecs != null && kRecs.length == 2) {
                    KeyRecord ownerKey = null;
                    KeyRecord otherKey = null;
                    if (kRecs[0].ownerUserId.equals(contactRecord.ownerUserId)) {
                      ownerKey = kRecs[0];
                      otherKey = kRecs[1];
                    } else {
                      ownerKey = kRecs[1];
                      otherKey = kRecs[0];
                    }
                    jOwnerEncryption.setText(ownerKey.plainPublicKey.shortInfo() + "/" + "AES(256)");
                    jOwnerEncryption.setIcon(RecordUtilsGui.getIcon(ownerKey));
                    jOtherEncryption.setText(otherKey.plainPublicKey.shortInfo() + "/" + "AES(256)");
                    jOtherEncryption.setIcon(RecordUtilsGui.getIcon(otherKey));
                    setEnabledButtons();
                  }
                }
              } // end synchronized
            }
          } // end synchronized
        }
      }
    };
    th.setDaemon(true);
    th.start();
  }


  private JButton[] createButtons() {
    JButton[] buttons = new JButton[3];

    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
    buttons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedOK();
      }
    });
    jOk = buttons[0];


    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Transcript"));
    buttons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedTranscript();
      }
    });

    buttons[2] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[2].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedCancel();
      }
    });

    return buttons;
  }


  private JComponent createMainComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactPropertiesDialog.class, "createMainComponent()");
    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));

    panel.setLayout(new GridBagLayout());

    jContactName = new JMyTextField(amIOwner ? contactRecord.getOwnerNote() : contactRecord.getOtherNote());
    documentChangeListener = new DocumentChangeListener();
    jContactName.getDocument().addDocumentListener(documentChangeListener);

    int posY = 0;
    panel.add(new JMyLabel(Images.get(ImageNums.CONTACT32)), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jContactName, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Contact_ID")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JMyLabel(contactRecord.contactId.toString()), new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Contact_Creator")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jContactCreator = new JMyLabel(FETCHING_DATA);
    panel.add(jContactCreator, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Contact_Owner")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jContactOwner = new JMyLabel(FETCHING_DATA);
    panel.add(jContactOwner, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Contact_With")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jContactWith = new JMyLabel(FETCHING_DATA);
    panel.add(jContactWith, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Status")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    String status = ContactRecUtil.getStatusText(contactRecord.status, contactRecord.ownerUserId);
    ImageIcon icon = ContactRecUtil.getStatusIcon(contactRecord.status, contactRecord.ownerUserId);
    panel.add(new JMyLabel(status, icon, JLabel.LEFT), new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;



    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;



    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Owner's_Encryption")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jOwnerEncryption = new JMyLabel(FETCHING_DATA);
    panel.add(jOwnerEncryption, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Contact's_Encryption")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jOtherEncryption = new JMyLabel(FETCHING_DATA);
    panel.add(jOtherEncryption, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    jAllowMessaging = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Allow_messaging."));
    jAllowFolderSharing = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Allow_folder_sharing."));
    jNotifyOfOnlineStatus = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Notify_of_online_status."));
    jEnableAudibleOnlineNotify = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Enable_audible_notification..."));
    if (trace != null) trace.data(100, canAlterContact);
    jAllowMessaging.setEnabled(canAlterContact);
    jAllowFolderSharing.setEnabled(canAlterContact);
    jNotifyOfOnlineStatus.setEnabled(canAlterContact);
    jAllowMessaging.setSelected((contactRecord.permits.intValue() & ContactRecord.PERMIT_DISABLE_MESSAGING) == 0);
    jAllowFolderSharing.setSelected((contactRecord.permits.intValue() & ContactRecord.PERMIT_DISABLE_SHARE_FOLDERS) == 0);
    jNotifyOfOnlineStatus.setSelected((contactRecord.permits.intValue() & ContactRecord.PERMIT_DISABLE_SEE_ONLINE_STATUS) == 0);

    jEnableAudibleOnlineNotify.setEnabled(amIOwner && jNotifyOfOnlineStatus.isSelected());
    jEnableAudibleOnlineNotify.setSelected((contactRecord.permits.intValue() & ContactRecord.SETTING_DISABLE_AUDIBLE_STATUS_NOTIFY) == 0);

    if (trace != null) trace.data(110, contactRecord.permits);
    if (trace != null) trace.data(111, jAllowMessaging.isSelected());
    if (trace != null) trace.data(112, jAllowFolderSharing.isSelected());
    if (trace != null) trace.data(113, jNotifyOfOnlineStatus.isSelected());
    if (trace != null) trace.data(113, jEnableAudibleOnlineNotify.isSelected());
    ChangeListener changeListener = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        setEnabledButtons();
      }
    };
    jAllowMessaging.addChangeListener(changeListener);
    jAllowFolderSharing.addChangeListener(changeListener);
    jNotifyOfOnlineStatus.addChangeListener(changeListener);
    jEnableAudibleOnlineNotify.addChangeListener(changeListener);

    jPermissionsLabel = new JMyLabel(java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("label_Permissions_for_USER1_set_by_USER2"), new Object[] {"contact owner", "other party"}));
    panel.add(jPermissionsLabel, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;
    int reciprocalButtonPosY = posY;
    panel.add(jAllowMessaging, new GridBagConstraints(0, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;
    panel.add(jAllowFolderSharing, new GridBagConstraints(0, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;
    panel.add(jNotifyOfOnlineStatus, new GridBagConstraints(0, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;
    panel.add(jEnableAudibleOnlineNotify, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    {
      final ContactRecord otherContact = cache.getContactRecordOwnerWith(contactRecord.contactWithId, contactRecord.ownerUserId);
      JButton jReciprocalButton = null;
      if (otherContact != null) {
        jReciprocalButton = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Show_Reciprocal_Contact"));
        jReciprocalButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (otherContactPropertiesDialog == null) {
              if (parentWindow instanceof Frame)
                otherContactPropertiesDialog = new ContactPropertiesDialog((Frame) parentWindow, otherContact);
              else if (parentWindow instanceof Dialog)
                otherContactPropertiesDialog = new ContactPropertiesDialog((Dialog) parentWindow, otherContact);
              otherContactPropertiesDialog.otherContactPropertiesDialog = ContactPropertiesDialog.this;
              Point p = otherContactPropertiesDialog.getLocation();
              otherContactPropertiesDialog.setLocation(p.x + 16, p.y + 16);
            } else {
              otherContactPropertiesDialog.toFront();
            }
          }
        });
      } else if (!amIOwner) {
        jReciprocalButton = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Create_Reciprocal_Contact"));
        jReciprocalButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (parentWindow instanceof Frame)
              new InitiateContactDialog((Frame) parentWindow, new Long[] { contactRecord.ownerUserId });
            else if (parentWindow instanceof Dialog)
              new InitiateContactDialog((Dialog) parentWindow, new Long[] { contactRecord.ownerUserId });
          }
        });
      }
      if (jReciprocalButton != null) {
        panel.add(jReciprocalButton, new GridBagConstraints(1, reciprocalButtonPosY, 1, 3, 0, 0,
              GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
      }
    }
    posY ++;


    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Date_Created")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JMyLabel(Misc.getFormattedTimestamp(contactRecord.dateCreated)), new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Date_Updated")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    //String dateUpdated = fileLink.recordUpdated != null ? dateFormat.format(fileLink.recordUpdated) : "";
    String dateUpdated = Misc.getFormattedTimestamp(contactRecord.dateUpdated);
    panel.add(new JMyLabel(dateUpdated), new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    // filler
    panel.add(new JMyLabel(), new GridBagConstraints(0, posY, 2, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    posY ++;

    if (trace != null) trace.exit(ContactPropertiesDialog.class, panel);
    return panel;
  }

  private void setEnabledButtons() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactPropertiesDialog.class, "setEnabledButtons()");
    synchronized (monitor) {
      if (!isClosed) {
        String newName = jContactName.getText().trim();
        jOk.setEnabled(newName.length() > 0 && (isNameChanged() || isPermitsChanged()));
      }
    }
    if (trace != null) trace.exit(ContactPropertiesDialog.class);
  }

  /**
   * @return true if name has changed
   */
  private boolean isNameChanged() {
    String oldName = "";
    if (amIOwner) {
      oldName = contactRecord.getOwnerNote() != null ? contactRecord.getOwnerNote().trim() : "Unknown";
    } else {
      oldName = contactRecord.getOtherNote() != null ? contactRecord.getOtherNote().trim() : "Unknown";
    }
    return !jContactName.getText().trim().equals(oldName);
  }

  private boolean isPermitsChanged() {
    int newPermits = getNewPermits();
    return newPermits != contactRecord.permits.intValue();
  }

  private int getNewPermits() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactPropertiesDialog.class, "getNewPermits()");
    int newPermits = jAllowMessaging.isSelected() ? 0 : ContactRecord.PERMIT_DISABLE_MESSAGING ;
    newPermits |= jAllowFolderSharing.isSelected() ? 0 : ContactRecord.PERMIT_DISABLE_SHARE_FOLDERS;
    newPermits |= jNotifyOfOnlineStatus.isSelected() ? 0 : ContactRecord.PERMIT_DISABLE_SEE_ONLINE_STATUS;
    newPermits |= contactRecord.isGiven() ? ContactRecord.PERMIT__THIS_CONTACT_IS_GIVEN : 0;
    newPermits |= jEnableAudibleOnlineNotify.isSelected() ? 0 : ContactRecord.SETTING_DISABLE_AUDIBLE_STATUS_NOTIFY;
    if (trace != null) trace.exit(ContactPropertiesDialog.class, newPermits);
    return newPermits;
  }


  /**
   * Policy is to rename the current property contact and if the other way contact is
   * of active type, rename it too.
   */
  private void pressedOK() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactPropertiesDialog.class, "pressedOK()");

    closeDialog();

    Thread th = new ThreadTraced("Change Contact Properties - sending...") {
      public void runTraced() {
        // apply Name Change
        if (isNameChanged()) {
          Long userId = null;
          Long otherUserId = null;
          if (amIOwner) {
            userId = contactRecord.ownerUserId;
            otherUserId = contactRecord.contactWithId;
          } else {
            userId = contactRecord.contactWithId;
            otherUserId = contactRecord.ownerUserId;
          }
          ContactRecord c1 = cache.getContactRecordOwnerWith(userId, otherUserId);
          ContactRecord c2 = cache.getContactRecordOwnerWith(otherUserId, userId);
          boolean renamed = false;
          if (c1 != null && c1.isOfActiveType()) {
            int code = CommandCodes.CNT_Q_RENAME_MY_CONTACT;
            SIL.submitAndReturn(new MessageAction(code, prepareDataSet(c1)));
            if (c1.equals(contactRecord))
              renamed = true;
          }
          if (c2 != null && c2.isOfActiveType()) {
            int code = CommandCodes.CNT_Q_RENAME_CONTACTS_WITH_ME;
            SIL.submitAndReturn(new MessageAction(code, prepareDataSet(c2)));
            if (c2.equals(contactRecord))
              renamed = true;
          }

          if (!renamed) {
            int code = amIOwner ? CommandCodes.CNT_Q_RENAME_MY_CONTACT : CommandCodes.CNT_Q_RENAME_CONTACTS_WITH_ME;
            SIL.submitAndReturn(new MessageAction(code, prepareDataSet(contactRecord)));
          }
        }

        // apply Permits Change
        if (isPermitsChanged()) {
          int newPermits = getNewPermits();
          int code = amIOwner ? CommandCodes.CNT_Q_ALTER_SETTINGS : CommandCodes.CNT_Q_ALTER_PERMITS;
          Object[] objs = new Object[] { contactRecord.contactId, new Integer(newPermits) };
          Obj_List_Co dataSet = new Obj_List_Co();
          dataSet.objs = objs;
          SIL.submitAndReturn(new MessageAction(code, dataSet));
        }
      }
    };
    th.setDaemon(true);
    th.start();

    if (trace != null) trace.exit(ContactPropertiesDialog.class);
  }


  private ProtocolMsgDataSet prepareDataSet(ContactRecord contactRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactPropertiesDialog.class, "prepareDataSet(ContactRecord contactRecord)");
    if (trace != null) trace.args(contactRecord);

    ProtocolMsgDataSet dataSet = null;
    if (contactRecord.ownerUserId.equals(cache.getMyUserId())) {
      contactRecord.setOwnerNote(jContactName.getText().trim());
      contactRecord.seal(cache.getFolderShareRecordMy(contactRecord.folderId, true).getSymmetricKey());
      dataSet = new Cnt_Rename_Rq(contactRecord);
    } else {
      contactRecord.setOtherNote(jContactName.getText().trim());
      contactRecord.sealRecrypt(cache.getUserRecord().getSymKeyCntNotes());
      dataSet = new Cnt_AcceptDecline_Rq(contactRecord);
    }

    if (trace != null) trace.exit(ContactPropertiesDialog.class, dataSet);
    return dataSet;
  }

  private void pressedCancel() {
    closeDialog();
  }


  private void pressedTranscript() {
    UserRecord userRec = cache.getUserRecord();
    KeyRecord myKeyRec = cache.getKeyRecordMyCurrent();
    BA encSymmetricKey = null;
    //BASymmetricKey symmetricKey = null;
    BASymCipherBulk encName = null;
    String name = null;
    String RSA = cache.getKeyRecord(userRec.pubKeyId).plainPublicKey.shortInfo().toUpperCase();
    if (amIOwner) {
      FolderShareRecord shareRec = cache.getFolderShareRecordMy(contactRecord.folderId, true);
      encSymmetricKey = shareRec.getEncSymmetricKey();
      //symmetricKey = shareRec.getSymmetricKey();
      encName = contactRecord.getEncOwnerNote();
      name = contactRecord.getOwnerNote();
    } else {
      encSymmetricKey = userRec.getEncSymKeys();
      //symmetricKey = userRec.getSymKeyCntNotes();
      encName = contactRecord.getEncOtherNote();
      name = contactRecord.getOtherNote();
    }

    StringBuffer sb = new StringBuffer();
    sb.append("--- BEGIN RECEIVED CONTACT");

    sb.append("\n--- BEGIN AES(256) ENCRYPTED CONTACT NAME\n\n");
    sb.append(ArrayUtils.breakLines(encName.getHexContent(), 80));
    sb.append("\n\n--- END AES(256) ENCRYPTED CONTACT NAME");

    if (amIOwner) {
      sb.append("\n\n--- BEGIN AES(256) ENCRYPTED FOLDER AES(256) KEY\n\n");
      sb.append(ArrayUtils.breakLines(ArrayUtils.spreadString(encSymmetricKey.getHexContent(), 4, ' '), 80));
      sb.append("\n\n--- END AES(256) ENCRYPTED FOLDER AES(256) KEY");
    }

    sb.append("\n\n--- BEGIN "+RSA+" ENCRYPTED SUPER FOLDER AND CONTACT AES(256) KEYS\n\n");
    sb.append(ArrayUtils.breakLines(userRec.getEncSymKeys().getHexContent(), 80));
    sb.append("\n\n--- END "+RSA+" ENCRYPTED SUPER FOLDER AND CONTACT AES(256) KEYS");

    sb.append("\n\n--- BEGIN AES(256) PASS-CODE ENCRYPTED "+RSA+" PRIVATE KEY\n\n");
    sb.append(ArrayUtils.breakLines(myKeyRec.getEncPrivateKey().getHexContent(), 80));
    sb.append("\n\n--- END AES(256) PASS-CODE ENCRYPTED "+RSA+" PRIVATE KEY");

    sb.append("\n--- END RECEIVED CONTACT");


    sb.append("\n\n--- BEGIN COMPUTED CONTACT");
    sb.append("\n--- BEGIN PLAIN CONTACT NAME\n\n");
    sb.append(name);
    sb.append("\n\n--- END PLAIN CONTACT NAME");
/*
    sb.append("\n\n--- BEGIN PLAIN CONTACT AES(256) KEY\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.spreadString(symmetricKey.getHexContent(), 4, ' '), 80));
    sb.append("\n\n--- END PLAIN CONTACT AES(256) KEY");
*/
    sb.append("\n--- END COMPUTED CONTACT");

    JTextArea textArea = new JMyTextArea(sb.toString());
    textArea.setEditable(false);
    textArea.setCaretPosition(0);
    textArea.setRows(35);
    textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

    JButton jClose = new JMyButton("Close");
    jClose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Dialog d = (Dialog) SwingUtilities.windowForComponent((Component) e.getSource());
        d.dispose();
      }
    });
    new GeneralDialog(this, com.CH_gui.lang.Lang.rb.getString("title_Contact_Transcript"), new JButton[] { jClose }, -1, 0, new JScrollPane(textArea));
  }


  public void closeDialog() {
    synchronized (monitor) {
      if (!isClosed) {
        isClosed = true;
        if (documentChangeListener != null && jContactName != null) {
          jContactName.getDocument().removeDocumentListener(documentChangeListener);
          documentChangeListener = null;
        }
        if (otherContactPropertiesDialog != null) {
          otherContactPropertiesDialog.otherContactPropertiesDialog = null;
          otherContactPropertiesDialog = null;
        }
        super.closeDialog();
      }
    }
  }


  private class DocumentChangeListener implements DocumentListener {
    public void changedUpdate(DocumentEvent e) {
      setEnabledButtons();
    }
    public void insertUpdate(DocumentEvent e) {
      setEnabledButtons();
    }
    public void removeUpdate(DocumentEvent e) {
      setEnabledButtons();
    }
  }
}
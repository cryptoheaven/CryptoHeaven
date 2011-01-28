/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_gui.dialog;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.cache.*;
import com.CH_cl.service.cache.event.*;

import com.CH_co.cryptx.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.cnt.*;
import com.CH_co.service.msg.dataSets.key.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.msg.dataSets.usr.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.frame.MainFrame;
import com.CH_gui.gui.*;
import com.CH_gui.service.records.RecordUtilsGui;
import com.CH_gui.util.*;
import com.CH_guiLib.gui.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.26 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class AcceptDeclineContactDialog extends GeneralDialog {

  private static final boolean AUTO_CREATE_RECIPROCAL_CONTACT = true;

  JLabel jContactFrom;
  JLabel jContactEncryption;
  JTextArea jContactReason;
  JLabel jYourEncryption;
  JTextField jNewContactName;
  JCheckBox jAllowMessaging;
  JCheckBox jAllowFolderSharing;
  JCheckBox jNotifyOfOnlineStatus;
  JCheckBox jAudibleNotify;

  boolean shouldAddToContactList;

  JButton jAccept;
  JButton jDecline;

  ContactRecordListener contactListener;

  static int DEFAULT_OK_BUTTON = 0;
  static int DEFAULT_CANCEL_BUTTON = 2;

  // contact that we are accepting / declining
  ContactRecord contactRecord;
  ServerInterfaceLayer serverInterfaceLayer;

  // my key used to encrypt the new contact name
  KeyRecord keyRecord;
  boolean handleFetched;
  boolean keyFetched;

  private DocumentChangeListener documentChangeListener;

  // track if dialog close method was called to prevent certain updates
  private boolean isClosed;
  private final Object monitor = new Object();

  /** Creates new AcceptDeclineContactDialog */
  public AcceptDeclineContactDialog(Frame owner, ContactRecord contactRecord) {
    super(owner, com.CH_gui.lang.Lang.rb.getString("title_Accept_/_Decline_Contact"));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AcceptDeclineContactDialog.class, "AcceptDeclineContact()");

    this.contactRecord = contactRecord;
    this.serverInterfaceLayer = MainFrame.getServerInterfaceLayer();
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    this.keyRecord = cache.getKeyRecord(cache.getUserRecord().currentKeyId);

    JButton[] jButtons = createButtons();
    JPanel jPanel = createPanel();

    if (!AUTO_CREATE_RECIPROCAL_CONTACT) {
      contactListener = new ContactListener();
      cache.addContactRecordListener(contactListener);
    }

    super.init(owner, jButtons, jPanel, DEFAULT_OK_BUTTON, DEFAULT_CANCEL_BUTTON);

    setEnabledButtons();
    getHandle();
    getPubKey();

    if (trace != null) trace.exit(AcceptDeclineContactDialog.class);
  }

  private void getHandle() {
    // check cache first
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    UserRecord uRec = cache.getUserRecord(contactRecord.ownerUserId);
    if (uRec != null) {
      jContactFrom.setText(uRec.shortInfo());
      jContactFrom.setIcon(RecordUtilsGui.getIcon(uRec));
      jNewContactName.setText(uRec.handle);
      handleFetched = true;
      setEnabledButtons();
    }
    // if not found, run request to the server
    else {
      Thread th = new ThreadTraced("Accept / Decline Contact Get Handle") {
        public void runTraced() {
          Obj_IDList_Co request = new Obj_IDList_Co();
          request.IDs = new Long[] { contactRecord.ownerUserId };
          MessageAction msgAction = new MessageAction(CommandCodes.USR_Q_GET_HANDLES, request);
          ClientMessageAction replyMsg = serverInterfaceLayer.submitAndFetchReply(msgAction, 30000);
          DefaultReplyRunner.nonThreadedRun(serverInterfaceLayer, replyMsg);

          if (replyMsg != null) {
            synchronized (monitor) {
              if (!isClosed) {
                synchronized (getTreeLock()) {
                  if (replyMsg.getActionCode() == CommandCodes.USR_A_GET_HANDLES) {
                    Usr_UsrHandles_Rp replyData = (Usr_UsrHandles_Rp) replyMsg.getMsgDataSet();
                    UserRecord[] uRecs = replyData.userRecords;
                    if (uRecs != null && uRecs.length == 1) {
                      jContactFrom.setText(uRecs[0].shortInfo());
                      jContactFrom.setIcon(RecordUtilsGui.getIcon(uRecs[0]));
                      jNewContactName.setText(uRecs[0].handle);
                      handleFetched = true;
                      setEnabledButtons();
                    }
                  }
                }
              }
            }
          }
        }
      };
      th.setDaemon(true);
      th.start();
    }
  }

  private void getPubKey() {
    // check cache first
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    KeyRecord kRec = cache.getKeyRecordForUser(contactRecord.ownerUserId);
    if (kRec != null) {
      jContactEncryption.setText(kRec.plainPublicKey.shortInfo() + "/" + "AES(256)");
      jContactEncryption.setIcon(RecordUtilsGui.getIcon(kRec));
      keyFetched = true;
      setEnabledButtons();
    }
    // if not found, run request to the server
    else {
      Thread th = new ThreadTraced("Accept / Decline Contact Get Public Key") {
        public void runTraced() {
          Obj_IDList_Co request = new Obj_IDList_Co();
          request.IDs = new Long[] { contactRecord.ownerUserId };
          MessageAction msgAction = new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_USERS, request);
          ClientMessageAction replyMsg = serverInterfaceLayer.submitAndFetchReply(msgAction, 30000);
          DefaultReplyRunner.nonThreadedRun(serverInterfaceLayer, replyMsg);

          if (replyMsg != null) {
            synchronized (monitor) {
              if (!isClosed) {
                synchronized (getTreeLock()) {
                  if (replyMsg.getActionCode() == CommandCodes.KEY_A_GET_PUBLIC_KEYS) {
                    Key_PubKeys_Rp replyData = (Key_PubKeys_Rp) replyMsg.getMsgDataSet();
                    KeyRecord[] kRecs = replyData.keyRecords;
                    if (kRecs != null && kRecs.length == 1) {
                      jContactEncryption.setText(kRecs[0].plainPublicKey.shortInfo() + "/" + "AES(256)");
                      jContactEncryption.setIcon(RecordUtilsGui.getIcon(kRecs[0]));
                      keyFetched = true;
                      setEnabledButtons();
                    }
                  }
                }
              }
            }
          }
        }
      };
      th.setDaemon(true);
      th.start();
    }
  }

  private void setEnabledButtons() {
    synchronized (monitor) {
      if (!isClosed) {
        boolean enable = keyFetched && handleFetched && jNewContactName.getText().trim().length() > 0;
        jAccept.setEnabled(enable);
        jDecline.setEnabled(enable);
        if (!AUTO_CREATE_RECIPROCAL_CONTACT) {
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          ContactRecord mutualContact = cache.getContactRecordOwnerWith(cache.getMyUserId(), contactRecord.ownerUserId);
          boolean mutualOk = mutualContact == null;
          boolean addOk = keyFetched && handleFetched && mutualOk;
          shouldAddToContactList = addOk;
        }
      }
    }
  }

  private JButton[] createButtons() {
    JButton[] jButtons = new JButton[3];

    int button = 0;

    jButtons[button] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Accept"));
    jAccept = jButtons[button];
    jButtons[button].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedAccept();
      }
    });
    button ++;

    jButtons[button] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Decline"));
    jDecline = jButtons[button];
    jButtons[button].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedDecline();
      }
    });
    button ++;

    jButtons[button] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    jButtons[button].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedCancel();
      }
    });
    button ++;

    jAccept.setEnabled(false);
    jDecline.setEnabled(false);

    return jButtons;
  }


  private JPanel createPanel() {

    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    panel.add(new JMyLabel(Images.get(ImageNums.CONTACT_NEW32)), new GridBagConstraints(0, 0, 1, 2, 0, 0,
      GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Contact_From")), new GridBagConstraints(1, 0, 1, 1, 0, 0,
      GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Contact's_Encryption")), new GridBagConstraints(1, 1, 1, 1, 0, 0,
      GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Reason_For_Contact")), new GridBagConstraints(1, 2, 2, 1, 0, 0,
      GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Your_Encryption")), new GridBagConstraints(1, 6, 1, 1, 0, 0,
      GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_New_Contact_Name")), new GridBagConstraints(1, 7, 1, 1, 0, 0,
      GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));


    jContactFrom = new JMyLabel("(" + contactRecord.ownerUserId + ")");
    jContactEncryption = new JMyLabel(".../" + "AES(256)");
    jContactReason = new JMyTextArea(contactRecord.getOtherNote(), 3, 30);
    jContactReason.setEditable(false);
    jContactReason.setWrapStyleWord(true);
    jContactReason.setLineWrap(true);
    jYourEncryption = new JMyLabel(keyRecord.plainPublicKey.shortInfo() + "/" + "AES(256)", RecordUtilsGui.getIcon(keyRecord), JLabel.LEFT);

    jNewContactName = new JMyTextField(com.CH_gui.lang.Lang.rb.getString("textfield_Fetching_Username..."));
    documentChangeListener = new DocumentChangeListener();
    jNewContactName.getDocument().addDocumentListener(documentChangeListener);

    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    Keymap contactReasonMap = jContactReason.getKeymap();
    contactReasonMap.removeKeyStrokeBinding(enter);

    Keymap contactNameMap = jNewContactName.getKeymap();
    contactNameMap.removeKeyStrokeBinding(enter);

    panel.add(jContactFrom, new GridBagConstraints(2, 0, 1, 1, 10, 0,
      GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jContactEncryption, new GridBagConstraints(2, 1, 1, 1, 10, 0,
      GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JScrollPane(jContactReason), new GridBagConstraints(1, 3, 2, 3, 10, 10,
      GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jYourEncryption, new GridBagConstraints(2, 6, 1, 1, 10, 0,
      GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jNewContactName, new GridBagConstraints(2, 7, 1, 1, 10, 0,
      GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    jAllowMessaging = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Allow_messaging."));
    jAllowMessaging.setSelected((contactRecord.permits.intValue() & ContactRecord.PERMIT_DISABLE_MESSAGING) == 0);
    jAllowFolderSharing = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Allow_folder_sharing."));
    jAllowFolderSharing.setSelected((contactRecord.permits.intValue() & ContactRecord.PERMIT_DISABLE_SHARE_FOLDERS) == 0);
    jNotifyOfOnlineStatus = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Notify_of_online_status."));
    jNotifyOfOnlineStatus.setSelected((contactRecord.permits.intValue() & ContactRecord.PERMIT_DISABLE_SEE_ONLINE_STATUS) == 0);
    jAudibleNotify = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Enable_audible_notification..."));
    jAudibleNotify.setSelected((contactRecord.permits.intValue() & ContactRecord.SETTING_DISABLE_AUDIBLE_STATUS_NOTIFY) == 0);

    JPanel jPermitsPanel = new JPanel();
    jPermitsPanel.setLayout(new GridBagLayout());
    jPermitsPanel.setBorder(new TitledBorder(new EtchedBorder(), com.CH_gui.lang.Lang.rb.getString("title_Grant_Permissions")));

    panel.add(jPermitsPanel, new GridBagConstraints(1, 8, 2, 1, 10, 0,
      GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    jPermitsPanel.add(jAllowMessaging, new GridBagConstraints(0, 8, 2, 1, 10, 0,
      GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 2, 5), 0, 0));
    jPermitsPanel.add(jAllowFolderSharing, new GridBagConstraints(0, 9, 2, 1, 10, 0,
      GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 2, 5), 0, 0));
    jPermitsPanel.add(jNotifyOfOnlineStatus, new GridBagConstraints(0, 10, 2, 1, 10, 0,
      GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 5, 5), 0, 0));

    JPanel jOptionsPanel = new JPanel();
    jOptionsPanel.setLayout(new GridBagLayout());
    jOptionsPanel.setBorder(new TitledBorder(new EtchedBorder(), com.CH_gui.lang.Lang.rb.getString("title_Options")));

    panel.add(jOptionsPanel, new GridBagConstraints(1, 9, 2, 1, 10, 0,
      GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    jOptionsPanel.add(jAudibleNotify, new GridBagConstraints(0, 8, 2, 1, 10, 0,
      GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    return panel;
  }


  private ProtocolMsgDataSet prepareDataSet(boolean isAccept) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AcceptDeclineContactDialog.class, "prepareDataSet()");

    contactRecord.setOtherNote(jNewContactName.getText().trim());
    contactRecord.setOtherSymKey(new BASymmetricKey(32));
    contactRecord.seal(keyRecord);

    // determine permits
    int permits = 0;
    if (!jAllowMessaging.isSelected())
      permits |= ContactRecord.PERMIT_DISABLE_MESSAGING;
    if (!jAllowFolderSharing.isSelected())
      permits |= ContactRecord.PERMIT_DISABLE_SHARE_FOLDERS;
    if (!jNotifyOfOnlineStatus.isSelected())
      permits |= ContactRecord.PERMIT_DISABLE_SEE_ONLINE_STATUS;
    if (!jAudibleNotify.isSelected())
      permits |= ContactRecord.SETTING_DISABLE_AUDIBLE_STATUS_NOTIFY;
    contactRecord.permits = new Integer(permits);

    Cnt_AcceptDecline_Rq request = new Cnt_AcceptDecline_Rq(contactRecord);

    if (isAccept)
      request.autoCreateReciprocals = Boolean.valueOf(AUTO_CREATE_RECIPROCAL_CONTACT);

    if (trace != null) trace.exit(AcceptDeclineContactDialog.class, request);
    return request;
  }


  private void pressedAccept() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AcceptDeclineContactDialog.class, "pressedAccept()");
    send(true);
    if (shouldAddToContactList) {

      final Long uId = contactRecord.ownerUserId;
      new InitiateContactDialog(AcceptDeclineContactDialog.this, new Long[] { uId });

    }
    if (trace != null) trace.exit(AcceptDeclineContactDialog.class);
  }

  private void pressedDecline() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AcceptDeclineContactDialog.class, "pressedAccept()");
    send(false);
    if (trace != null) trace.exit(AcceptDeclineContactDialog.class);
  }

  private void send(final boolean accept) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AcceptDeclineContactDialog.class, "send(boolean accept)");
    if (trace != null) trace.args(accept);

    closeDialog();

    Thread th = new ThreadTraced("Accept / Decline Contact Send") {
      public void runTraced() {
        int code = accept ? CommandCodes.CNT_Q_ACCEPT_CONTACTS : CommandCodes.CNT_Q_DECLINE_CONTACTS;
        ProtocolMsgDataSet dataSet = prepareDataSet(accept); // << this may take some time due to encryption
        serverInterfaceLayer.submitAndReturn(new MessageAction(code, dataSet));
      }
    };
    th.setDaemon(true);
    th.start();

    if (trace != null) trace.exit(AcceptDeclineContactDialog.class);
  }

  private void pressedCancel() {
    closeDialog();
  }


  public void closeDialog() {
    synchronized (monitor) {
      if (!isClosed) {
        isClosed = true;
        if (documentChangeListener != null && jNewContactName != null) {
          jNewContactName.getDocument().removeDocumentListener(documentChangeListener);
          documentChangeListener = null;
        }
        if (contactListener != null) {
          FetchedDataCache.getSingleInstance().removeContactRecordListener(contactListener);
          contactListener = null;
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

  /****************************************************************************************/
  /************* LISTENERS ON CHANGES IN THE CACHE *****************************************/
  /****************************************************************************************/

  /**
   * Listen on updates to the ContactRecords in the cache.
   */
  private class ContactListener implements ContactRecordListener {
    public void contactRecordUpdated(ContactRecordEvent event) {
      // to prevent gui tree deadlocks, run on an AWT thread
      javax.swing.SwingUtilities.invokeLater(new ContactGUIUpdater());
    }
  }

  private class ContactGUIUpdater implements Runnable {
    public ContactGUIUpdater() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactGUIUpdater.class, "ContactGUIUpdater()");
      if (trace != null) trace.exit(ContactGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactGUIUpdater.class, "ContactGUIUpdater.run()");

      setEnabledButtons();

      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(ContactGUIUpdater.class);
    }
  }

}
/*
 * Copyright 2001-2010 by CryptoHeaven Development Team,
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

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.records.filters.*;

import com.CH_co.gui.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.cnt.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.frame.*;
import com.CH_gui.list.*;
import com.CH_gui.msgs.*;

/**
 * <b>Copyright</b> &copy; 2001-2010
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
 * <b>$Revision: 1.8 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class ManageContactsDialog extends GeneralDialog {

  public static final int DEFAULT_OK_INDEX = 0;
  public static final int DEFAULT_CANCEL_INDEX = 1;

  private DualListBox dualListBox;
  private JButton jUsersButton;
  private JPanel jUsersPanel;
  private JCheckBox jAllowMessaging;
  private JCheckBox jAllowSharing;
  private JCheckBox jNotifyStatus;
  private JCheckBox jContactsAdd;
  private JCheckBox jContactsSetPermissions;
  private JButton jAdvanced;
  private JButton jOk;

  private UserRecord[] selectedUserRecords;
  private ContactRecord[] selectedUsersContacts;

  /** Creates new ManageContactsDialog */
  public ManageContactsDialog(Dialog owner, UserRecord[] userRecords) {
    super(owner, "Manage Contacts");
    this.selectedUserRecords = userRecords;
    initialize(owner);
  }
  public ManageContactsDialog(Frame owner, UserRecord[] userRecords) {
    super(owner, "Manage Contacts");
    this.selectedUserRecords = userRecords;
    initialize(owner);
  }

  private void initialize(Component owner) {
    JButton[] buttons = createButtons();
    createComponents();
    JPanel panel = createMainPanel();
    init(owner, buttons, panel, DEFAULT_OK_INDEX, DEFAULT_CANCEL_INDEX);
    updateUserSelection(selectedUserRecords);
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[2];
    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
    buttons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedOk();
      }
    });
    jOk = buttons[0];
    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedCancel();
      }
    });
    return buttons;
  }

  private void createComponents() {
    dualListBox = new DualListBox(true, false, true, true);
    dualListBox.setSourceChoicesTitle(com.CH_gui.lang.Lang.rb.getString("label_Available_Contacts"));
    dualListBox.setDestinationChoicesTitle(com.CH_gui.lang.Lang.rb.getString("label_Selected_Contacts"));
    jUsersButton = new JMyButton("user(s):");
    jUsersButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedUsers();
      }
    });
    jUsersPanel = new JPanel();
    jUsersPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    jUsersPanel.setBorder(new EmptyBorder(0,0,0,0));
    jAllowMessaging = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Allow_messaging."), true);
    jAllowSharing = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Allow_folder_sharing."), true);
    jNotifyStatus = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Notify_of_online_status."), true);
    jAdvanced = new JMyButton("Advanced >>");
    jAdvanced.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedAdvanced();
      }
    });
    jContactsAdd = new JMyCheckBox("Create contacts for specified user(s) listed in 'Selected Contacts'.", true);
    jContactsSetPermissions = new JMyCheckBox("Set permissions only, do not delete unselected contacts.", false);
    jContactsAdd.setVisible(false);
    jContactsSetPermissions.setVisible(false);
  }

  private JPanel createMainPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    panel.setLayout(new GridBagLayout());

    int posY = 0;
    panel.add(new JMyLabel("Please select desired contacts for"), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(jUsersButton, new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jUsersPanel, new GridBagConstraints(1, posY, 1, 2, 10, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    posY += 2;

    panel.add(dualListBox, new GridBagConstraints(0, posY, 2, 1, 10, 10,
          GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel("Set Permissions for selected contacts:"), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 2, 5), 0, 0));
    posY ++;

    panel.add(jAllowMessaging, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 15, 2, 5), 0, 0));
    posY ++;
    panel.add(jAllowSharing, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 15, 2, 5), 0, 0));
    posY ++;
    panel.add(jNotifyStatus, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 15, 5, 5), 0, 0));
    posY ++;

    panel.add(jAdvanced, new GridBagConstraints(0, posY, 2, 1, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 2, 5), 0, 0));
    posY ++;
    panel.add(jContactsAdd, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 15, 2, 5), 0, 0));
    posY ++;
    panel.add(jContactsSetPermissions, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 15, 5, 5), 0, 0));
    posY ++;
    return panel;
  }

  private void pressedAdvanced() {
    jContactsAdd.setVisible(!jContactsAdd.isVisible());
    jContactsSetPermissions.setVisible(!jContactsSetPermissions.isVisible());
  }

  private void pressedOk() {
    ContactRecord[] allContacts = selectedUsersContacts; // default contacts are all of the ones applicable to the selected users and the group
    Long[] selectedUserIDs = RecordUtils.getIDs(selectedUserRecords);
    Object[] chosenUsersObjs = dualListBox.getResult();
    UserRecord[] chosenUsers = (UserRecord[]) ArrayUtils.toArray(new Vector(Arrays.asList(chosenUsersObjs)), UserRecord.class);
    Long[] chosenUserIDs = RecordUtils.getIDs(chosenUsers);
    // All contacts that are potentially affected
    Vector selectedUserContactsV = new Vector();
    // All contacts that are potentially modified
    Vector chosenUserContactsV = new Vector();
    // All contacts that need to be removed
    Vector removedContactsV = new Vector();
    // All contacts that need to be created
    Vector createContactsV = new Vector();
    // Gather all contacts that are potentially affected
    if (allContacts != null) {
      for (int i=0; i<allContacts.length; i++) {
        ContactRecord cRec = allContacts[i];
        selectedUserContactsV.addElement(cRec); // all contacts from default choices are included
        /*
        if (ArrayUtils.find(selectedUserIDs, cRec.contactWithId) >= 0 || ArrayUtils.find(selectedUserIDs, cRec.ownerUserId) >= 0)
          selectedUserContactsV.addElement(cRec);
         */
      }
    }
    // Gather all contacts which need to be modified
    if (allContacts != null) {
      for (int i=0; i<allContacts.length; i++) {
        ContactRecord cRec = allContacts[i];
        // only contacts between selected users and chosen users
        if ((ArrayUtils.find(selectedUserIDs, cRec.contactWithId) >= 0 && ArrayUtils.find(chosenUserIDs, cRec.ownerUserId) >= 0) ||
            (ArrayUtils.find(chosenUserIDs, cRec.contactWithId) >= 0 && ArrayUtils.find(selectedUserIDs, cRec.ownerUserId) >= 0))
          chosenUserContactsV.addElement(cRec);
      }
    }

    // Get new permits value from GUI
    boolean allowMessaging = jAllowMessaging.isSelected();
    boolean allowSharing = jAllowSharing.isSelected();
    boolean notifyStatus = jNotifyStatus.isSelected();
    int permits = 0;
    if (!allowMessaging)
      permits |= ContactRecord.PERMIT_DISABLE_MESSAGING;
    if (!allowSharing)
      permits |= ContactRecord.PERMIT_DISABLE_SHARE_FOLDERS;
    if (!notifyStatus)
      permits |= ContactRecord.PERMIT_DISABLE_SEE_ONLINE_STATUS;
    Integer permitsI = Integer.valueOf(permits);

    // Gather removed contacts
    removedContactsV.addAll(selectedUserContactsV);
    removedContactsV.removeAll(chosenUserContactsV);

    // Modify chosen contacts
    for (int i=0; i<chosenUserContactsV.size(); i++) {
      ContactRecord cRec = (ContactRecord) ((ContactRecord) chosenUserContactsV.elementAt(i)).clone();
      cRec.permits = permitsI;
      chosenUserContactsV.setElementAt(cRec, i);
    }

    // Create new contacts
    // for every selected user see if there is a contact with chosen user
    for (int i=0; i<selectedUserIDs.length; i++) {
      Long selectedUid = selectedUserIDs[i];
      for (int k=0; k<chosenUserIDs.length; k++) {
        Long chosenUid = chosenUserIDs[k];
        boolean contactOwnerExists = selectedUid.equals(chosenUid) ? true : false;
        boolean contactWithExists = selectedUid.equals(chosenUid) ? true : false;
        // look between contacts that are being modified
        if (!contactOwnerExists || !contactWithExists) {
          for (int x=0; x<chosenUserContactsV.size(); x++) {
            ContactRecord cRec = (ContactRecord) chosenUserContactsV.elementAt(x);
            if (cRec.ownerUserId.equals(selectedUid) && cRec.contactWithId.equals(chosenUid))
              contactOwnerExists = true;
            if (cRec.ownerUserId.equals(chosenUid) && cRec.contactWithId.equals(selectedUid))
              contactWithExists = true;
            if (contactOwnerExists && contactWithExists) {
              break;
            }
          }
        }
        // look between contacts that are being created
        if (!contactOwnerExists || !contactWithExists) {
          for (int x=0; x<createContactsV.size(); x++) {
            ContactRecord cRec = (ContactRecord) createContactsV.elementAt(x);
            if (cRec.ownerUserId.equals(selectedUid) && cRec.contactWithId.equals(chosenUid))
              contactOwnerExists = true;
            if (cRec.ownerUserId.equals(chosenUid) && cRec.contactWithId.equals(selectedUid))
              contactWithExists = true;
            if (contactOwnerExists && contactWithExists) {
              break;
            }
          }
        }
        if (!contactOwnerExists) {
          ContactRecord cRec = new ContactRecord();
          cRec.ownerUserId = selectedUid;
          cRec.contactWithId = chosenUid;
          cRec.permits = permitsI;
          createContactsV.addElement(cRec);
        }
        if (!contactWithExists) {
          ContactRecord cRec = new ContactRecord();
          cRec.ownerUserId = chosenUid;
          cRec.contactWithId = selectedUid;
          cRec.permits = permitsI;
          createContactsV.addElement(cRec);
        }
      }
    }

    // Group all Create and Modify contacts
    Vector setV = new Vector();
    setV.addAll(chosenUserContactsV);
    boolean doCreate = jContactsAdd.isSelected();
    boolean doRemove = !jContactsSetPermissions.isSelected();
    if (doCreate)
      setV.addAll(createContactsV);
    Cnt_GroupCnt_Rq setContacts = new Cnt_GroupCnt_Rq((ContactRecord[]) ArrayUtils.toArray(setV, ContactRecord.class));
    Obj_IDList_Co removeContacts = new Obj_IDList_Co(RecordUtils.getIDs(removedContactsV));
    ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
    Obj_List_Co set = new Obj_List_Co(new Object[] { setContacts, doRemove ? removeContacts : null});
    SIL.submitAndReturn(new MessageAction(CommandCodes.CNT_Q_SET_GROUP_CONTACTS, set));

    // Gather all contacts that need to be created
    dispose();
  }

  private void pressedCancel() {
    dispose();
  }

  private void pressedUsers() {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    SubUserFilter subUserFilter = new SubUserFilter(cache.getMyUserId(), true, false);
    UserRecord[] subUsers = (UserRecord[]) RecordUtils.filter(cache.getUserRecords(), subUserFilter);

    DualListBoxDialog d = new DualListBoxDialog(this, "Select Users", "Available Users", "Selected Users", subUsers, selectedUserRecords);
    Object[] selectedObjs = d.getResult();
    if (selectedObjs != null) {
      Vector usersV = new Vector(Arrays.asList(selectedObjs));
      selectedUserRecords = (UserRecord[]) ArrayUtils.toArray(usersV, UserRecord.class);
      updateUserSelection(selectedUserRecords);
    }
  }

  private void updateUserSelection(UserRecord[] userRecords) {
    this.selectedUserRecords = userRecords;
    MsgPanelUtils.drawRecordFlowPanel(selectedUserRecords, jUsersPanel);
    updateContactDualBox();
  }

  private void updateContactDualBox() {
    Thread runUpdate = new ThreadTraced("Update Contact Dual Box") {
      public void runTraced() {
        dualListBox.clearAllSourceListModels();
        dualListBox.clearAllDestinationListModels();

        if (selectedUserRecords != null && selectedUserRecords.length > 0) {
          jOk.setEnabled(false);
          ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
          final FetchedDataCache cache = SIL.getFetchedDataCache();
          final ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.CNT_Q_GET_GROUP_CONTACTS, new Obj_IDList_Co(RecordUtils.getIDs(selectedUserRecords))), 60000);
          if (msgAction != null) {
            try {
              SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                  // Gather unique userIDs from fetched contacts
                  ProtocolMsgDataSet set = msgAction.getMsgDataSet();
                  if (set instanceof Cnt_GetCnts_Rp) {
                    Cnt_GetCnts_Rp cntSet = (Cnt_GetCnts_Rp) set;
                    ContactRecord[] cRecs = cntSet.contactRecords;

                    Long[] selectedUserIDs = RecordUtils.getIDs(selectedUserRecords);
                    ContactRecord[] ownerContacts = (ContactRecord[]) RecordUtils.filter(cRecs, new ContactFilterCo(selectedUserIDs));
                    Long[] uIDs = ContactRecord.getContactWithUserIDs(ownerContacts, true);
                    // Fill the lists
                    SubUserFilter subUserFilter = new SubUserFilter(cache.getMyUserId(), true, false);
                    UserRecord[] subUsers = (UserRecord[]) RecordUtils.filter(cache.getUserRecords(), subUserFilter);
                    UserRecord[] chosenContactUsers = cache.getUserRecords(uIDs);
                    UserRecord[] notChosenContactUsers = (UserRecord[]) ArrayUtils.getDifference(subUsers, chosenContactUsers);
                    if (notChosenContactUsers != null && notChosenContactUsers.length > 0) {
                      //dualListBox.setSourceElements(notChosenContactUsers);
                      dualListBox.addDefaultSourceElements(notChosenContactUsers);
                    }
                    if (chosenContactUsers != null && chosenContactUsers.length > 0) {
                      dualListBox.moveToDefaultDestinationElements(chosenContactUsers);
                    }
                    //defaultContactUsers = chosenContactUsers;
                    selectedUsersContacts = cRecs;
                  }
                }
              });
            } catch (InterruptedException e1) {
            } catch (InvocationTargetException e2) {
            }
          }
          DefaultReplyRunner.runAction(msgAction);
          jOk.setEnabled(true);
        }
      }
    };
    runUpdate.setDaemon(true);
    runUpdate.start();
  }

  public static void main(String[] args) {
    new ManageContactsDialog((Frame) null, null);
  }

}
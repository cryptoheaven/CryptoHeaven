/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.dialog;

import com.CH_gui.gui.JMyButton;
import com.CH_gui.util.GeneralDialog;
import com.CH_gui.util.MessageDialog;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import com.CH_gui.frame.*;
import com.CH_gui.table.*;
import com.CH_gui.usrs.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.2 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class UserSelectPassRecoveryDialog extends GeneralDialog {

  private static final int DEFAULT_CANCEL_BUTTON_INDEX = 1;

  private UserSearchPanel userSearchPanel;
  private String selectButtonText;
  private UserRecord selectedUserRecord;

  private JButton jSelect;

  /** Creates new UserSelectPassRecoveryDialog */
  public UserSelectPassRecoveryDialog(Frame owner, String selectButtonText, String searchString) {
    super(owner, "Password Recovery - Account Search");
    constructDialog(owner, selectButtonText, searchString);
  }
  /** Creates new UserSelectPassRecoveryDialog */
  public UserSelectPassRecoveryDialog(Dialog owner, String selectButtonText, String searchString) {
    super(owner, "Password Recovery - Account Search");
    constructDialog(owner, selectButtonText, searchString);
  }

  private void constructDialog(Component owner, String selectButtonText, String searchString) {
    this.selectButtonText = selectButtonText;
    userSearchPanel = new UserSearchPanel(true, false, false, "First find your account by username, email address, or ID.", searchString, true);
    userSearchPanel.getUserActionTable().addRecordSelectionListener(new RecordSelectionListener() {
      public void recordSelectionChanged(RecordSelectionEvent event) {
        setEnabledButtons();
      }
    });
    JButton[] jButtons = createButtons();

    this.getRootPane().setDefaultButton(userSearchPanel.getSearchButton());
    setEnabledButtons();
    super.init(owner, jButtons, userSearchPanel, -1, DEFAULT_CANCEL_BUTTON_INDEX);
  }


  /**
   * @return the dialog 'Search' and 'Cancel' buttons
   */
  private JButton[] createButtons() {
    JButton[] buttons = new JButton[2];

    buttons[0] = new JMyButton(selectButtonText);

    buttons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        // select action
        selectedUserRecord = (UserRecord) userSearchPanel.getUserActionTable().getSelectedRecord();
        Component parent = getParent();
        closeDialog();
        if (parent instanceof LoginFrame) {
          final LoginFrame loginFrame = (LoginFrame) parent;
          UserRecord userRecord = getSelectedUserRecord();
          if (userRecord != null) {
            loginFrame.setUsername(userRecord.handle);
            // Create runnables to be executed after Recovery Challenge is recived
            Runnable passRecoveryRunner = new Runnable() {
              public void run() {
                new PassRecoveryRecoverDialog(loginFrame, MainFrame.getServerInterfaceLayer().getFetchedDataCache().getMyPassRecoveryRecord());
              }
            };
            Runnable timeoutRunner = new Runnable() {
              public void run() {
                MessageDialog.showWarningDialog(loginFrame, "Timeout: could not fetch password recovery data within allotted time. Please check your Internet connection.", "Timeout Error");
              }
            };
            MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.USR_Q_PASS_RECOVERY_GET_CHALLENGE, new Obj_List_Co(userRecord.userId)), 30000, passRecoveryRunner, timeoutRunner);
          }
        }
      }
    });
    jSelect = buttons[0];

    buttons[1] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Cancel"));
    buttons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        // cancel action
        selectedUserRecord = null;
        closeDialog();
      }
    });

    return buttons;
  }

  /**
   * @return User Record that was selected before the dialog was dismissed.
   */
  public UserRecord getSelectedUserRecord() {
    return selectedUserRecord;
  }

  private void setEnabledButtons() {
    jSelect.setEnabled(userSearchPanel.getUserActionTable().getSelectedRecords() != null);
  }

}
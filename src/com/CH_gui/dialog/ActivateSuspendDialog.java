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

import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.ops.*;

import com.CH_co.cryptx.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.frame.*;
import com.CH_gui.gui.*;
import com.CH_gui.list.*;
import com.CH_gui.msgs.MsgPanelUtils;
import com.CH_gui.service.records.RecordUtilsGui;
import com.CH_gui.util.*;
import com.CH_guiLib.gui.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
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
 * <b>$Revision: 1.5 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class ActivateSuspendDialog extends GeneralDialog {

  private static final int DEFAULT_OK_BUTTON_INDEX = 0;
  private static final int DEFAULT_CANCEL_BUTTON_INDEX = 1;

  private JRadioButton jActivate;
  private JRadioButton jSuspend;
  private JTextArea jSuspendReason;

  private JMyPasswordKeyboardField jOldPass;

  private JButton okButton;
  private JButton cancelButton;

  private CheckDocumentListener checkDocumentListener;

  private ServerInterfaceLayer serverInterfaceLayer;
  private FetchedDataCache cache;
  private UserRecord userRecord;

  private Long[] subAccountsToManage;

  /** Creates new ActivateSuspendDialog */
  public ActivateSuspendDialog(Frame frame, Long[] subAccountsToManage) {
    super(frame, com.CH_gui.lang.Lang.rb.getString("title_Activate_Suspend_Accounts"));
    initialize(frame, subAccountsToManage);
  }
  public ActivateSuspendDialog(Dialog dialog, Long[] subAccountsToManage) {
    super(dialog, com.CH_gui.lang.Lang.rb.getString("title_Activate_Suspend_Accounts"));
    initialize(dialog, subAccountsToManage);
  }
  private void initialize(Component parent, Long[] subAccountsToManage) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ActivateSuspendDialog.class, "initialize(Component parent, Long[] subAccountsToManage)");

    this.subAccountsToManage = subAccountsToManage;

    serverInterfaceLayer = MainFrame.getServerInterfaceLayer();
    cache = serverInterfaceLayer.getFetchedDataCache();
    userRecord = cache.getUserRecord();

    JButton[] buttons = createButtons();
    JPanel panel = createMainPanel();
    okButton.setEnabled(isInputValid());

    super.init(parent, buttons, panel, MiscGui.createLogoHeader(), DEFAULT_OK_BUTTON_INDEX, DEFAULT_CANCEL_BUTTON_INDEX);

    if (trace != null) trace.exit(ActivateSuspendDialog.class);
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[2];
    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
    buttons[0].setDefaultCapable(true);
    buttons[0].addActionListener(new OKActionListener());
    okButton = buttons[0];

    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[1].setDefaultCapable(true);
    buttons[1].addActionListener(new CancelActionListener());
    cancelButton = buttons[1];

    return buttons;
  }

  private JPanel createMainPanel() {
    JPanel panel = new JPanel();

    panel.setLayout(new GridBagLayout());

    jActivate = new JMyRadioButton("Activate", true);
    jSuspend = new JMyRadioButton("Suspend; message to the user attempting to login:", false);
    jSuspend.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        boolean isSelected = jSuspend.isSelected();
        jSuspendReason.setEnabled(isSelected);
      }
    });
    ButtonGroup group = new ButtonGroup();
    group.add(jActivate);
    group.add(jSuspend);
    jSuspendReason = new JMyTextArea(3, 10);
    jSuspendReason.setEnabled(false);
    jSuspendReason.setWrapStyleWord(true);
    jSuspendReason.setLineWrap(true);
    jSuspendReason.setText(getCommonStatusInfo(cache.getUserRecords(subAccountsToManage)));

    jOldPass = new JMyPasswordKeyboardField();

    checkDocumentListener = new CheckDocumentListener();
    jOldPass.getDocument().addDocumentListener(checkDocumentListener);


    int posY = 0;

    String changeUserNameLabel = com.CH_gui.lang.Lang.rb.getString("label_Suspend_Sub_User_Accounts_warning_text");

    JLabel warningLabel = new JMyLabel(Images.get(ImageNums.SHIELD32));
    warningLabel.setText(changeUserNameLabel);
    warningLabel.setHorizontalAlignment(JLabel.LEFT);
    warningLabel.setVerticalTextPosition(JLabel.TOP);
    warningLabel.setBorder(new LineBorder(warningLabel.getBackground().darker(), 1, true));
    warningLabel.setPreferredSize(new Dimension(410, 60));
    panel.add(warningLabel, new GridBagConstraints(0, posY, 3, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 1, 10, 1), 20, 20));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Selected_Accounts")), new GridBagConstraints(0, posY, 3, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    JPanel listPanel = new JPanel();
    listPanel.setLayout(new GridBagLayout());
    UserRecord[] subUsers = cache.getUserRecords(subAccountsToManage);
    for (int i=0; i<subUsers.length; i++) {
      Record rec = MsgPanelUtils.convertUserIdToFamiliarUser(subUsers[i].userId, true, true);
      listPanel.add(new JMyLabel(ListRenderer.getRenderedText(rec), ListRenderer.getRenderedIcon(rec), JLabel.LEADING), new GridBagConstraints(0, i, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 10, 2, 10), 0, 0));
    }
//    listPanel.add(new JLabel(), new GridBagConstraints(0, subUsers.length, 2, 1, 10, 10,
//        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    JComponent mainList = null;
    if (subUsers.length > 5) {
      JScrollPane sc = new JScrollPane(listPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      sc.getVerticalScrollBar().setUnitIncrement(5);
      mainList = sc;
    } else {
      mainList = listPanel;
    }
    panel.add(mainList, new GridBagConstraints(0, posY, 3, 1, 10, 10,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(jActivate, new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 2, 5), 0, 0));
    posY ++;
    panel.add(jSuspend, new GridBagConstraints(0, posY, 3, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 5, 1, 5), 0, 0));
    posY ++;
    JScrollPane reasonSC = new JScrollPane(jSuspendReason, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    reasonSC.getVerticalScrollBar().setUnitIncrement(5);
    reasonSC.setPreferredSize(new Dimension(100, 100));
    reasonSC.setMinimumSize(new Dimension(100, 100));
    panel.add(reasonSC, new GridBagConstraints(0, posY, 3, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(1, 5, 5, 5), 0, 0));
    posY ++;

    String confirmPasswordLabel = com.CH_gui.lang.Lang.rb.getString("label_Please_enter_your_account_password_to_confirm_this_action.");
    panel.add(new JMyLabel(confirmPasswordLabel), new GridBagConstraints(0, posY, 3, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    JLabel userName = new JMyLabel(userRecord.handle);
    userName.setIcon(RecordUtilsGui.getIcon(userRecord));
    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Username")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(userName, new GridBagConstraints(1, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 0), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Password")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jOldPass, new GridBagConstraints(1, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    return panel;
  }

  private static String getCommonStatusInfo(UserRecord[] userRecs) {
    String reason = null;
    if (userRecs != null) {
      for (int i=0; i<userRecs.length; i++) {
        if (reason == null)
          reason = userRecs[i].statusInfo;
        else if (!reason.equals(userRecs[i].statusInfo))
          reason = "";
      }
    }
    return reason;
  }

  private boolean isInputValid() {
    return true;
  }

  private void setEnabledInputs(boolean b) {
    jActivate.setEnabled(b);
    jSuspend.setEnabled(b);
    jSuspendReason.setEnabled(b && jSuspend.isSelected());
    jOldPass.setEnabled(b);
    okButton.setEnabled(b && isInputValid());
    cancelButton.setEnabled(b);
  }


  private class OKActionListener implements ActionListener {
    public void actionPerformed (ActionEvent event) {
      String messageText = "";
      messageText = "Are you sure you want to "+ (jSuspend.isSelected() ? "Suspend" : "Activate") +" the following accounts: \n";
      messageText += getUsersAsText(subAccountsToManage, ", ");
      messageText += "\n\nDo you want to continue?";
      String title = com.CH_gui.lang.Lang.rb.getString("msgTitle_Confirmation");
      boolean option = MessageDialog.showDialogYesNo(ActivateSuspendDialog.this, messageText, title);
      if (option == true) {
        // run the long part is another thread
        new OKThread().start();
      }
    }
  }

  private class CancelActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      closeDialog();
    }
  }

  private class CheckDocumentListener implements DocumentListener {
    public void changedUpdate(DocumentEvent e) {
      okButton.setEnabled(isInputValid());
    }
    public void insertUpdate(DocumentEvent e)  {
      okButton.setEnabled(isInputValid());
    }
    public void removeUpdate(DocumentEvent e) {
      okButton.setEnabled(isInputValid());
    }
  }

  public void closeDialog() {
    if (checkDocumentListener != null) {
      if (jOldPass != null)
        jOldPass.getDocument().removeDocumentListener(checkDocumentListener);
      checkDocumentListener = null;
    }
    super.closeDialog();
  }

  /* @return encoded password entered by the user */
  public BAEncodedPassword getOldBAEncodedPassword() {
    return UserRecord.getBAEncodedPassword(jOldPass.getPassword(), userRecord.handle);
  }

  private String getUsersAsText(Long[] uIDs, String separator) {
    StringBuffer sb = new StringBuffer();
    UserRecord[] uRecs = cache.getUserRecords(uIDs);
    for (int i=0; i<uRecs.length; i++) {
      if (i>0) sb.append(separator);
      sb.append(ListRenderer.getRenderedText(uRecs[i]));
    }
    return sb.toString();
  }

  /**
   * Thread that takes all input data and runs the action.
   */
  private class OKThread extends ThreadTraced {
    public OKThread() {
      super("ActivateSuspendDialog OKThread");
      setDaemon(true);
    }
    public void runTraced() {
      setEnabledInputs(false);
      boolean error = false;

      // check if old password matches
      BAEncodedPassword oldBA = getOldBAEncodedPassword();
      if (!cache.getEncodedPassword().equals(oldBA)) {
        error = true;
        String PASSWORD_ERROR = com.CH_gui.lang.Lang.rb.getString("msg_Password_does_not_match");
        MessageDialog.showErrorDialog(ActivateSuspendDialog.this, PASSWORD_ERROR, com.CH_gui.lang.Lang.rb.getString("msgTitle_Invalid_Input"));
        jOldPass.setText("");
      }

      if (!error) {
        short toStatus = jSuspend.isSelected() ? UserRecord.STATUS_BUSINESS_SUB_HELD : UserRecord.STATUS_BUSINESS_SUB;
        String statusMsg = jSuspendReason.getText();
        boolean success = UserOps.sendChangeStatusSubAccounts(serverInterfaceLayer, oldBA, subAccountsToManage, toStatus, statusMsg);
        error = !success;
      }

      if (!error) {
        closeDialog();
      } else {
        // if error occurred than enable inputs
        setEnabledInputs(true);
      }
    }
  }

}
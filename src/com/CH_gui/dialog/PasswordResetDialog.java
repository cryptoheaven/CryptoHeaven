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

import com.CH_gui.util.Images;
import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.JMyButton;
import com.CH_gui.gui.MyInsets;
import com.CH_gui.util.GeneralDialog;
import com.CH_gui.util.MessageDialog;
import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.ops.UserOps;

import com.CH_co.cryptx.*;
import com.CH_co.gui.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.key.Key_KeyRecov_Co;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.frame.*;
import com.CH_gui.gui.*;
import com.CH_gui.list.*;
import com.CH_gui.msgs.MsgPanelUtils;
import com.CH_gui.service.records.RecordUtilsGui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Vector;

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
 * <b>$Revision: 1.5 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class PasswordResetDialog extends GeneralDialog {

  private static final int DEFAULT_OK_BUTTON_INDEX = 0;
  private static final int DEFAULT_CANCEL_BUTTON_INDEX = 1;

  private JMyPasswordKeyboardField jNewPass;
  private JMyPasswordKeyboardField jRePass;
  private JMyPasswordKeyboardField jOldPass;

  private JButton okButton;
  private JButton cancelButton;

  private CheckDocumentListener checkDocumentListener;

  private ServerInterfaceLayer SIL;
  private FetchedDataCache cache;
  private UserRecord userRecord;

  private Long[] subAccountsToManage;
  private JLabel[] jSubAccountsNotes;
  private JLabel jNote1;
  private JLabel jNote2;
  private JLabel jNote3;

  private final Object monitor = new Object();
  private KeyRecoveryRecord[] subAccountsRecoveryRecs;

  /** Creates new PasswordResetDialog */
  public PasswordResetDialog(Frame frame, Long[] subAccountsToManage) {
    super(frame, com.CH_gui.lang.Lang.rb.getString("title_Password_Reset"));
    initialize(frame, subAccountsToManage);
  }
  public PasswordResetDialog(Dialog dialog, Long[] subAccountsToManage) {
    super(dialog, com.CH_gui.lang.Lang.rb.getString("title_Password_Reset"));
    initialize(dialog, subAccountsToManage);
  }
  private void initialize(Component parent, Long[] subAccountsToManage) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PasswordResetDialog.class, "initialize(Component parent, Long[] subAccountsToManage)");

    this.subAccountsToManage = subAccountsToManage;
    this.jSubAccountsNotes = new JMyLabel[subAccountsToManage.length];

    SIL = MainFrame.getServerInterfaceLayer();
    cache = SIL.getFetchedDataCache();
    userRecord = cache.getUserRecord();

    JButton[] buttons = createButtons();
    JPanel panel = createMainPanel();
    okButton.setEnabled(isInputValid());

    super.init(parent, buttons, panel, new JMyLabel(Images.get(ImageNums.LOGO_BANNER_MAIN)), DEFAULT_OK_BUTTON_INDEX, DEFAULT_CANCEL_BUTTON_INDEX);

    fetchKeyRecovery(subAccountsToManage);

    if (trace != null) trace.exit(PasswordResetDialog.class);
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

    JPanel jNote = new JPanel();
    jNote.setLayout(new GridBagLayout());
    jNote1 = new JMyLabel("Status:");
    jNote1.setFont(jNote1.getFont().deriveFont(Font.BOLD));
    jNote2 = new JMyLabel("Fetching Password Reset data...");
    jNote3 = new JMyLabel();
    jNote.add(jNote1, new GridBagConstraints(0, 0, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0, 0, 0, 0), 0, 0));
    jNote.add(jNote2, new GridBagConstraints(1, 0, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0, 5, 0, 2), 0, 0));
    jNote.add(jNote3, new GridBagConstraints(2, 0, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));

    jNewPass = new JMyPasswordKeyboardField();
    jRePass = new JMyPasswordKeyboardField();
    jOldPass = new JMyPasswordKeyboardField();

    checkDocumentListener = new CheckDocumentListener();
    jNewPass.getDocument().addDocumentListener(checkDocumentListener);
    jRePass.getDocument().addDocumentListener(checkDocumentListener);
    jOldPass.getDocument().addDocumentListener(checkDocumentListener);

    int posY = 0;

    String changeUserNameLabel = com.CH_gui.lang.Lang.rb.getString("label_Password_Reset_warning_text");

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
      listPanel.add(new JMyLabel(ListRenderer.getRenderedText(rec), ListRenderer.getRenderedIcon(rec), JLabel.LEADING), new GridBagConstraints(0, i, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 10, 2, 5), 0, 0));
      jSubAccountsNotes[i] = new JMyLabel();
      listPanel.add(jSubAccountsNotes[i], new GridBagConstraints(1, i, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 2, 10), 0, 0));
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

    panel.add(jNote, new GridBagConstraints(0, posY, 3, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_New_Password")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jNewPass, new GridBagConstraints(1, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Re-type_Password")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jRePass, new GridBagConstraints(1, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
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

  private void fetchKeyRecovery(final Long[] subAccountsToManage) {
    Thread th = new ThreadTraced("Key Recovery Fetcher") {
      public void runTraced() {
        ClientMessageAction reply = SIL.submitAndFetchReply(new MessageAction(CommandCodes.KEY_Q_GET_KEY_RECOVERY, new Obj_IDList_Co(subAccountsToManage)), 60000);
        DefaultReplyRunner.nonThreadedRun(SIL, reply);
        if (reply == null || !(reply.getMsgDataSet() instanceof Key_KeyRecov_Co)) {
          MessageDialog.showErrorDialog(PasswordResetDialog.this, "Could not fetch required settings.  Please try again later.", "Recovery Temporarily Unavailable", true);
          synchronized(monitor) {
            closeDialog();
            monitor.notifyAll();
          }
        } else {
          Key_KeyRecov_Co recovSet = (Key_KeyRecov_Co) reply.getMsgDataSet();
          synchronized(monitor) {
            subAccountsRecoveryRecs = recovSet.recoveryRecords;
            boolean anyAvailable = false;
            // fetch any keys that we don't have in the cache
            if (subAccountsRecoveryRecs != null && subAccountsRecoveryRecs.length > 0) {
              Vector keysIDsV = new Vector();
              for (int i=0; i<subAccountsRecoveryRecs.length; i++) {
                Long keyId = subAccountsRecoveryRecs[i].keyId;
                if (cache.getKeyRecord(keyId) == null)
                  keysIDsV.addElement(keyId);
              }
              if (keysIDsV.size() > 0) {
                SIL.submitAndWait(new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_KEYIDS, new Obj_IDList_Co(keysIDsV)), 60000);
              }
              for (int i=0; i<subAccountsRecoveryRecs.length; i++) {
                Long uID = cache.getKeyRecord(subAccountsRecoveryRecs[i].keyId).ownerUserId;
                int index = ArrayUtils.find(subAccountsToManage, uID);
                if (index >= 0) {
                  anyAvailable = true;
                  jSubAccountsNotes[index].setText("(reset available)");
                  jSubAccountsNotes[index].setIcon(Images.get(ImageNums.STATUS_ONLINE16));
                }
              }
            }
            boolean anyUnavailable = false;
            for (int i=0; i<subAccountsToManage.length; i++) {
              if (jSubAccountsNotes[i].getText().length() == 0) {
                anyUnavailable = true;
                Long uID = subAccountsToManage[i];
                UserRecord uRec = cache.getUserRecord(uID);
                if (!Misc.isBitSet(uRec.flags, UserRecord.FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY)) {
                  jSubAccountsNotes[i].setText("(option disabled)");
                  jSubAccountsNotes[i].setIcon(Images.get(ImageNums.STATUS_OFFLINE16));
                } else {
                  jSubAccountsNotes[i].setText("(option enabled, user setup pending)");
                  jSubAccountsNotes[i].setIcon(Images.get(ImageNums.STATUS_AWAY16));
                }
              }
            }
            if (anyAvailable && anyUnavailable) {
              jNote1.setText("Note:");
              jNote2.setText("Password Reset is available only for the accounts marked with ");
              jNote3.setText("");
              jNote3.setIcon(Images.get(ImageNums.STATUS_ONLINE16));
            } else if (anyAvailable) {
              jNote1.setText("Summary:");
              if (subAccountsToManage.length == 1)
                jNote2.setText("Password Reset is available for the selected account.");
              else
                jNote2.setText("Password Reset is available for all of the selected accounts.");
              jNote3.setText("");
            } else {
              jNote1.setText("Warning:");
              if (subAccountsToManage.length == 1)
                jNote2.setText("Password Reset is not available for the selected account.");
              else
                jNote2.setText("Password Reset is not available for the selected accounts.");
              jNote3.setText("");
            }
            monitor.notifyAll();
          }
        }
      }
    };
    th.setDaemon(true);
    th.start();
  }

  private boolean isInputValid() {
    boolean rc = false;
    char[] pass2 = null;
    char[] pass3 = null;

    pass2 = jNewPass.getPassword();
    if (LoginFrame.isPasswordValid(pass2)) {
      pass3 = jRePass.getPassword();
      if (LoginFrame.isPasswordValid(pass3)) {
        rc = true;
      }
    }

    for (int i=0; pass2!=null && i<pass2.length; i++)
      pass2[i] = (char) 0;
    for (int i=0; pass3!=null && i<pass3.length; i++)
      pass3[i] = (char) 0;

    rc = rc && subAccountsRecoveryRecs != null && subAccountsRecoveryRecs.length > 0;

    return rc;
  }

  private void setEnabledInputs(boolean b) {
    jNewPass.setEnabled(b);
    jRePass.setEnabled(b);
    jOldPass.setEnabled(b);
    okButton.setEnabled(b && isInputValid());
    cancelButton.setEnabled(b);
  }


  private class OKActionListener implements ActionListener {
    public void actionPerformed (ActionEvent event) {
      Long[] subAccountsToReset = new Long[subAccountsRecoveryRecs.length];
      for (int i=0; i<subAccountsRecoveryRecs.length; i++) {
        subAccountsToReset[i] = cache.getKeyRecord(subAccountsRecoveryRecs[i].keyId).ownerUserId;
      }
      String messageText = "";
      messageText = "Are you sure you want to Reset the Password for the following accounts: \n";
      messageText += getUsersAsText(subAccountsToReset, ", ");
      messageText += "\n\nDo you want to continue?";
      String title = com.CH_gui.lang.Lang.rb.getString("msgTitle_Confirmation");
      boolean option = MessageDialog.showDialogYesNo(PasswordResetDialog.this, messageText, title);
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
      super("PasswordResetDialog OKThread");
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
        MessageDialog.showErrorDialog(PasswordResetDialog.this, PASSWORD_ERROR, com.CH_gui.lang.Lang.rb.getString("msgTitle_Invalid_Input"));
        jOldPass.setText("");
      }

      if (!error) {
        // check if new Password is properly re-typed
        char[] pass1 = jNewPass.getPassword();
        char[] pass2 = jRePass.getPassword();
        /* Password and re-typed password do not match */
        if (!Arrays.equals(pass1, pass2)) {
          MessageDialog.showErrorDialog(PasswordResetDialog.this, LoginFrame.RETYPE_PASSWORD_ERROR, com.CH_gui.lang.Lang.rb.getString("msgTitle_Invalid_Input"));
          jNewPass.setText(""); jRePass.setText("");
          error = true;
          jNewPass.requestFocus();
        }
        // clear password arrays
        for (int i=0; i<pass1.length; i++)
          pass1[i] = 0;
        for (int i=0; i<pass2.length; i++)
          pass2[i] = 0;
      }

      if (!error) {
        char[] newPass = jNewPass.getPassword();
        boolean success = UserOps.sendPasswordReset(SIL, oldBA, subAccountsRecoveryRecs, newPass);
        // clear password arrays
        for (int i=0; i<newPass.length; i++)
          newPass[i] = 0;
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
/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
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
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.frame.*;
import com.CH_gui.gui.*;
import com.CH_gui.util.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/** 
 * <b>Copyright</b> &copy; 2001-2013
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
 * <b>$Revision: 1.2 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class ChangePasswordDialog extends GeneralDialog {

  private static final int DEFAULT_OK_BUTTON_INDEX = 0;
  private static final int DEFAULT_CANCEL_BUTTON_INDEX = 1;

  // Error messages
  public static final String OLD_PASSWORD_ERROR = com.CH_cl.lang.Lang.rb.getString("msg_Old_Password_does_not_match");

  private JMyPasswordKeyboardField jOldPass;
  private JMyPasswordKeyboardField jNewPass;
  private JMyPasswordKeyboardField jRePass;

  private JButton okButton;
  private JButton cancelButton;

  private CheckDocumentListener checkDocumentListener;

  private ServerInterfaceLayer SIL;
  private FetchedDataCache cache;

  private boolean isSetMode = false;

  /** Creates new ChangePasswordDialog */
  public ChangePasswordDialog(Frame frame, boolean isSetMode) {
    super(frame, isSetMode ? com.CH_cl.lang.Lang.rb.getString("title_Set_Password") : com.CH_cl.lang.Lang.rb.getString("title_Change_Password"));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ChangePasswordDialog.class, "ChangePasswordDialog(Frame frame)");

    this.isSetMode = isSetMode;
    SIL = MainFrame.getServerInterfaceLayer();
    cache = SIL.getFetchedDataCache();

    JButton[] buttons = createButtons();
    JPanel panel = createMainPanel();
    okButton.setEnabled(false);

    super.init(frame, buttons, panel, MiscGui.createLogoHeader(), DEFAULT_OK_BUTTON_INDEX, DEFAULT_CANCEL_BUTTON_INDEX);

    if (trace != null) trace.exit(ChangePasswordDialog.class);
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[2];
    buttons[0] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_OK"));
    buttons[0].setDefaultCapable(true);
    buttons[0].addActionListener(new OKActionListener());
    okButton = buttons[0];

    buttons[1] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Cancel"));
    buttons[1].addActionListener(new CancelActionListener());
    cancelButton = buttons[1];

    return buttons;
  }

  private JPanel createMainPanel() {
    JPanel panel = new JPanel();

    panel.setLayout(new GridBagLayout());

    jOldPass = new JMyPasswordKeyboardField();
    jNewPass = new JMyPasswordKeyboardField();
    jRePass = new JMyPasswordKeyboardField();

    checkDocumentListener = new CheckDocumentListener();
    jOldPass.getDocument().addDocumentListener(checkDocumentListener);
    jNewPass.getDocument().addDocumentListener(checkDocumentListener);
    jRePass.getDocument().addDocumentListener(checkDocumentListener);

    int posY = 0;
//    panel.add(new JMyLabel(Images.get(ImageNums.LOGO_BANNER_MAIN)), new GridBagConstraints(0, posY, 3, 1, 0, 0, // LOGO_KEY_MAIN
//        GridBagConstraints.CENTER, GridBagConstraints.NONE, new MyInsets(0, 0, 0, 0), 0, 0));
//    posY ++;

    if (isSetMode) {
      JLabel warningLabel = new JMyLabel(Images.get(ImageNums.SHIELD32));
      warningLabel.setText("<html><body>Your account is not password protected.  Please protect your account with a password to prevent unauthorized access.</body></html>");
      warningLabel.setHorizontalAlignment(JLabel.LEFT);
      warningLabel.setVerticalTextPosition(JLabel.TOP);
      warningLabel.setBorder(new LineBorder(warningLabel.getBackground().darker(), 1, true));
      warningLabel.setPreferredSize(new Dimension(410, 40));
      panel.add(warningLabel, new GridBagConstraints(0, posY, 3, 1, 10, 0,
          GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(0, 1, 1, 1), 20, 20));
      posY ++;
    }

    panel.add(LoginFrame.getPasswordHint(), new GridBagConstraints(0, posY, 3, 1, 10, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(0, 1, 10, 1), 20, 20));
    posY ++;

    if (!isSetMode) {
      panel.add(new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Old_Password")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
      panel.add(jOldPass, new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;
    }

    panel.add(new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_New_Password")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jNewPass, new GridBagConstraints(1, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Re-type_Password")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jRePass, new GridBagConstraints(1, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    // filler
    panel.add(new JPanel(), new GridBagConstraints(1, posY, 1, 1, 10, 10,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0,0,0,0), 0, 0));

    return panel;
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

    return rc;
  }

  private void setEnabledInputs(boolean b) {
    jOldPass.setEnabled(b);
    jNewPass.setEnabled(b);
    jRePass.setEnabled(b);
    okButton.setEnabled(b && isInputValid());
    cancelButton.setEnabled(b);
  }


  private class OKActionListener implements ActionListener {
    public void actionPerformed (ActionEvent event) {
      // run the long part is another thread
      new OKThread().start();
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
      if (jNewPass != null)
        jNewPass.getDocument().removeDocumentListener(checkDocumentListener);
      if (jRePass != null)
        jRePass.getDocument().removeDocumentListener(checkDocumentListener);
      checkDocumentListener = null;
    }
    super.closeDialog();
  }

  /* @return encoded password entered by the user */
  public BAEncodedPassword getBAEncodedPassword(JMyPasswordKeyboardField jPass) {
    return UserRecord.getBAEncodedPassword(jPass.getPassword(), cache.getUserRecord().handle);
  }

  private class OKThread extends ThreadTraced {

    public OKThread() {
      super("ChangePasswordDialog OKThread");
      setDaemon(true);
    }
    public void runTraced() {
      setEnabledInputs(false);
      boolean error = false;

      // check if old Password matches
      BAEncodedPassword ba = getBAEncodedPassword(jOldPass);
      if (!cache.getEncodedPassword().equals(ba)) {
        error = true;
        MessageDialog.showErrorDialog(ChangePasswordDialog.this, OLD_PASSWORD_ERROR, com.CH_cl.lang.Lang.rb.getString("msgTitle_Invalid_Input"));
        jOldPass.setText("");
      }

      if (!error) {
        ba = getBAEncodedPassword(jNewPass);
        // check if new Password is properly re-typed
        char[] pass1 = jNewPass.getPassword();
        char[] pass2 = jRePass.getPassword();
        /* Password and re-typed password do not match */
        if (!Arrays.equals(pass1, pass2)) {
          MessageDialog.showErrorDialog(ChangePasswordDialog.this, LoginFrame.RETYPE_PASSWORD_ERROR, com.CH_cl.lang.Lang.rb.getString("msgTitle_Invalid_Input"));
          jNewPass.setText(""); jRePass.setText("");
          error = true;
          jNewPass.requestFocusInWindow();
        }
        // clear password arrays
        for (int i=0; i<pass1.length; i++)
          pass1[i] = 0;
        for (int i=0; i<pass2.length; i++)
          pass2[i] = 0;
      }

      if (!error) {
        boolean isMyKeyLocal = !Misc.isBitSet(cache.getUserRecord().flags, UserRecord.FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER);
        //boolean isLocalKey = KeyOps.isKeyStoredLocally(cache.getKeyRecordMyCurrent().keyId);
        StringBuffer errBuffer = new StringBuffer();
        boolean success = UserOps.sendPasswordChange(SIL, ba, !isMyKeyLocal, null, errBuffer);
        error = !success;

        if (error) {
          String where = !isMyKeyLocal ? "on the server" : "locally";
          String msg = "Private key could not be stored " + where + "!";
          if (errBuffer.length() > 0)
            msg += errBuffer.toString();
          MessageDialog.showErrorDialog(null, msg, "Key Storage Failed", true);
        }
      }

      // See if we need to re-setup Password Recovery
      // Don't bother very fresh new accounts with this extra dialog, they will see it upon next login.
      if (!isSetMode && !error) {
        SIL.submitAndWait(new MessageAction(CommandCodes.USR_Q_PASS_RECOVERY_GET_CHALLENGE, new Obj_List_Co(cache.getMyUserId())), 30000);
        if (cache.getMyPassRecoveryRecord() == null)
          new PassRecoverySetupDialog(MainFrame.getSingleInstance());
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
/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
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

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.Keymap;

import java.awt.*;
import java.awt.event.*;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.ops.*;

import com.CH_co.cryptx.*;
import com.CH_co.gui.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.frame.*;
import com.CH_gui.gui.*;
import com.CH_guiLib.gui.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: 1.25 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class ChangeUserNameDialog extends GeneralDialog {

  private static final int DEFAULT_OK_BUTTON_INDEX = 0;
  private static final int DEFAULT_CANCEL_BUTTON_INDEX = 1;

  private JTextField jUserName;
  private JMyPasswordKeyboardField jOldPass;

  private JButton okButton;
  private JButton cancelButton;

  private CheckDocumentListener checkDocumentListener;

  private ServerInterfaceLayer serverInterfaceLayer;
  private FetchedDataCache cache;

  /** Creates new ChangePasswordDialog */
  public ChangeUserNameDialog(Frame frame) {
    super(frame, com.CH_gui.lang.Lang.rb.getString("title_Change_Username"));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ChangePasswordDialog.class, "ChangeUserNameDialog(Frame frame)");

    serverInterfaceLayer = MainFrame.getServerInterfaceLayer();
    cache = serverInterfaceLayer.getFetchedDataCache();

    JButton[] buttons = createButtons();
    JPanel panel = createMainPanel();
    okButton.setEnabled(false);

    super.init(frame, buttons, panel, new JMyLabel(Images.get(ImageNums.LOGO_BANNER_MAIN)), DEFAULT_OK_BUTTON_INDEX, DEFAULT_CANCEL_BUTTON_INDEX);

    if (trace != null) trace.exit(ChangePasswordDialog.class);
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[2];
    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
    buttons[0].setDefaultCapable(true);
    buttons[0].addActionListener(new OKActionListener());
    okButton = buttons[0];

    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[1].addActionListener(new CancelActionListener());
    cancelButton = buttons[1];

    return buttons;
  }

  private JPanel createMainPanel() {
    JPanel panel = new JPanel();

    panel.setLayout(new GridBagLayout());

    jUserName = new JMyTextField();
    jUserName.addHierarchyListener(new InitialFocusRequestor());

    jOldPass = new JMyPasswordKeyboardField();

    checkDocumentListener = new CheckDocumentListener();
    jUserName.getDocument().addDocumentListener(checkDocumentListener);
    jOldPass.getDocument().addDocumentListener(checkDocumentListener);

    // remove enter key binding from the password fields
    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    Keymap userNameMap = jUserName.getKeymap();
    userNameMap.removeKeyStrokeBinding(enter);


    int posY = 0;
//    panel.add(new JMyLabel(Images.get(ImageNums.LOGO_BANNER_MAIN)), new GridBagConstraints(0, posY, 3, 1, 0, 0, // LOGO_KEY_MAIN
//        GridBagConstraints.CENTER, GridBagConstraints.NONE, new MyInsets(0, 0, 0, 0), 0, 0));
//    posY ++;

    JLabel warningLabel = new JMyLabel(Images.get(ImageNums.SHIELD32));
    warningLabel.setText(com.CH_gui.lang.Lang.rb.getString("label_Change_Username_hint_text"));
    warningLabel.setHorizontalAlignment(JLabel.LEFT);
    warningLabel.setVerticalTextPosition(JLabel.TOP);
    warningLabel.setBorder(new EtchedBorder());
    warningLabel.setPreferredSize(new Dimension(410, 60));
    panel.add(warningLabel, new GridBagConstraints(0, posY, 3, 1, 10, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(0, 1, 10, 1), 20, 20));
    posY ++;

    UserRecord uRec = cache.getUserRecord();
    JLabel currentUserName = new JMyLabel(uRec.handle);
    currentUserName.setIcon(uRec.getIcon());
    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Current_Username")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(currentUserName, new GridBagConstraints(1, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_New_Username")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jUserName, new GridBagConstraints(1, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Password")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jOldPass, new GridBagConstraints(1, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    // filler
    panel.add(new JPanel(), new GridBagConstraints(1, posY, 1, 1, 10, 10,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0,0,0,0), 0, 0));

    return panel;
  }

  private boolean isInputValid() {
    boolean rc = false;
    if (jUserName.getText().trim().length() > 0) {
      rc = true;
    }
    return rc;
  }



  private void setEnabledInputs(boolean b) {
    jUserName.setEnabled(b);
    jOldPass.setEnabled(b);
//    jUserName.setEditable(b);
//    jOldPass.setEditable(b);
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
      if (jUserName != null)
        jUserName.getDocument().removeDocumentListener(checkDocumentListener);
      if (jOldPass != null)
        jOldPass.getDocument().removeDocumentListener(checkDocumentListener);
      checkDocumentListener = null;
    }
    super.closeDialog();
  }

  /** @return encoded password entered by the user */
  public BAEncodedPassword getOldBAEncodedPassword() {
    return UserRecord.getBAEncodedPassword(jOldPass.getPassword(), cache.getUserRecord().handle);
  }
  /** @return encoded password entered by the user */
  public BAEncodedPassword getNewBAEncodedPassword() {
    return UserRecord.getBAEncodedPassword(jOldPass.getPassword(), jUserName.getText().trim());
  }

  private class OKThread extends ThreadTraced {

    public OKThread() {
      super("ChangeUserNameDialog OKThread");
      setDaemon(true);
    }
    public void runTraced() {
      setEnabledInputs(false);
      boolean error = false;

      // check if old password matches
      BAEncodedPassword ba = getOldBAEncodedPassword();
      if (!cache.getEncodedPassword().equals(ba)) {
        error = true;
        MessageDialog.showErrorDialog(ChangeUserNameDialog.this, ChangePasswordDialog.OLD_PASSWORD_ERROR, com.CH_gui.lang.Lang.rb.getString("msgTitle_Invalid_Input"));
        jOldPass.setText("");
      }

      String oldUserName = cache.getUserRecord().handle;
      String newUserName = jUserName.getText().trim();

      if (!error) {
        ba = getNewBAEncodedPassword();
        boolean isLocalKey = KeyOps.isKeyStoredLocally(cache.getKeyRecordMyCurrent().keyId);
        boolean success = UserOps.sendPasswordChange(serverInterfaceLayer, newUserName, ba, !isLocalKey);
        error = !success;
      }

      if (!error) {
        closeDialog();
        // See if we need to update the default Login UserName
        if (LoginFrame.getRememberUserNameProperty()) {
          LoginFrame.putUserList(newUserName, oldUserName);
        }
        // Update Main Frame title
        MainFrame.getSingleInstance().setUserTitle(cache.getUserRecord());
      } else {
        // if error occurred than enable inputs
        setEnabledInputs(true);
      }
    }
  }

}
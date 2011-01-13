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

import com.CH_gui.gui.*;
import com.CH_gui.util.*;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.frame.*;
import com.CH_guiLib.gui.*;

import java.awt.*;
import java.awt.event.*;
import java.security.*;
import java.sql.Timestamp;

import javax.swing.*;
import javax.swing.border.*;

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
 * <b>$Revision: 1.2 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class PassRecoveryRecoverDialog extends GeneralDialog {

  private static final int DEFAULT_OK_BUTTON_INDEX = 0;
  private static final int DEFAULT_CANCEL_BUTTON_INDEX = 1;

  private JMyTextField[] jAs;

  private ServerInterfaceLayer serverInterfaceLayer;
  private FetchedDataCache cache;
  private PassRecoveryRecord initialRecoveryRecord;
  private boolean reportActivityMode;
  private Timestamp reportSince;
  private boolean include24ExpiryNote;

  private String recoveredPassword;

  /** Creates new PassRecoveryRecoverDialog */
  public PassRecoveryRecoverDialog(Frame frame, PassRecoveryRecord passRecoveryRecord) {
    this(frame, passRecoveryRecord, null, false);
  }

  /** Creates new PassRecoveryRecoverDialog */
  public PassRecoveryRecoverDialog(Frame frame, PassRecoveryRecord passRecoveryRecord, Timestamp reportSince, boolean include24ExpiryNote) {
    super(frame, reportSince == null ? "Password Recovery" : "Security Notice - Password Recovery");
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PassRecoveryRecoverDialog.class, "PassRecoveryRecoverDialog(Frame frame, PassRecoveryRecord passRecoveryRecord, Timestamp reportSince, boolean include24ExpiryNote)");

    this.initialRecoveryRecord = passRecoveryRecord;
    this.reportActivityMode = reportSince != null ? true : false;
    this.reportSince = reportSince;
    this.include24ExpiryNote = include24ExpiryNote;

    serverInterfaceLayer = MainFrame.getServerInterfaceLayer();
    cache = serverInterfaceLayer.getFetchedDataCache();

    JButton[] buttons = createButtons();
    JPanel passRecoveryPanel = createMainPanel(initialRecoveryRecord);

    super.init(frame, buttons, passRecoveryPanel, new JMyLabel(Images.get(ImageNums.LOGO_BANNER_MAIN)), DEFAULT_OK_BUTTON_INDEX, DEFAULT_CANCEL_BUTTON_INDEX);
    if (trace != null) trace.exit(PassRecoveryRecoverDialog.class);
  }

  private JButton[] createButtons() {
    JButton[] buttons = null;
    if (!reportActivityMode && initialRecoveryRecord != null && initialRecoveryRecord.isEnabledQA()) {
      buttons = new JButton[2];
      buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
      buttons[0].setDefaultCapable(true);
      buttons[0].addActionListener(new OKActionListener());

      buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
      buttons[1].addActionListener(new CancelActionListener());
    } else {
      buttons = new JButton[1];
      buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Close"));
      buttons[0].setDefaultCapable(true);
      buttons[0].addActionListener(new CancelActionListener());
    }

    return buttons;
  }

  private JPanel createMainPanel(PassRecoveryRecord initialRecoveryRecord) {
    JPanel panel = new JPanel();

    panel.setLayout(new GridBagLayout());
    //panel.setBorder(new LineBorder(panel.getBackground().darker(), 1, true));

    if (!reportActivityMode && initialRecoveryRecord != null) {
      jAs = new JMyTextField[initialRecoveryRecord.numQs.shortValue()];
      for (int i=0; i<jAs.length; i++) {
        jAs[i] = new JMyTextField();
      }
    }

    int posY = 0;
//    panel.add(new JMyLabel(Images.get(ImageNums.LOGO_BANNER_MAIN)), new GridBagConstraints(0, posY, 4, 1, 0, 0, // LOGO_KEY_MAIN
//        GridBagConstraints.CENTER, GridBagConstraints.NONE, new MyInsets(0, 0, 0, 0), 0, 0));
//    posY ++;

    if (reportActivityMode) {
      JLabel warningLabel = new JMyLabel(Images.get(ImageNums.SHIELD32));
      warningLabel.setText("<html>Your account has recent Password Recovery activity.</html>");//The notice is shown for 24 hours from recovery attempt and cannot be disabled.</html>");
      warningLabel.setHorizontalAlignment(JLabel.LEFT);
      //warningLabel.setVerticalTextPosition(JLabel.TOP);
      warningLabel.setBorder(new LineBorder(warningLabel.getBackground().darker(), 1, true));
      warningLabel.setPreferredSize(new Dimension(410, 40));
      panel.add(warningLabel, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(0, 1, 1, 1), 20, 20));
      posY ++;
      if (initialRecoveryRecord.lastFetched != null && initialRecoveryRecord.lastFetched.compareTo(reportSince) > 0) {
        String fetchText = null;
        if (initialRecoveryRecord.isEnabledRecovery())
          fetchText = "Password Hint and Challenge questions retrieved on:";
        else
          fetchText = "Attempt to retrieve your Password Recovery settings was made on:";
        panel.add(new JMyLabel(fetchText), new GridBagConstraints(0, posY, 2, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 1, 5), 0, 0));
        posY ++;
        panel.add(new JMyLabel(Misc.getFormattedTimestamp(initialRecoveryRecord.lastFetched)), new GridBagConstraints(0, posY, 2, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(1, 25, 5, 5), 0, 0));
        posY ++;
        if (!initialRecoveryRecord.isEnabledRecovery()) {
          panel.add(new JMyLabel("Your Password Recovery is disabled."), new GridBagConstraints(0, posY, 2, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(1, 5, 5, 5), 0, 0));
          posY ++;
        }
      }
      if (initialRecoveryRecord.lastFailed != null && initialRecoveryRecord.lastFailed.compareTo(reportSince) > 0) {
        panel.add(new JMyLabel("Password Question and Answer challenge failed on:"), new GridBagConstraints(0, posY, 2, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 1, 5), 0, 0));
        posY ++;
        panel.add(new JMyLabel(Misc.getFormattedTimestamp(initialRecoveryRecord.lastFailed)), new GridBagConstraints(0, posY, 2, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(1, 25, 5, 5), 0, 0));
        posY ++;
      }
      if (initialRecoveryRecord.lastRecovered != null && initialRecoveryRecord.lastRecovered.compareTo(reportSince) > 0) {
        panel.add(new JMyLabel("Password Recovery completed successfully on:"), new GridBagConstraints(0, posY, 2, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 1, 5), 0, 0));
        posY ++;
        panel.add(new JMyLabel(Misc.getFormattedTimestamp(initialRecoveryRecord.lastRecovered)), new GridBagConstraints(0, posY, 2, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(1, 25, 5, 5), 0, 0));
        posY ++;
      } else {
        panel.add(new JMyLabel("Password was not recovered."), new GridBagConstraints(0, posY, 2, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
        posY ++;
      }
      panel.add(new JMyLabel("You cannot disable this security notice."), new GridBagConstraints(0, posY, 2, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(10, 5, include24ExpiryNote ? 1:5, 5), 0, 0));
      posY ++;
      if (include24ExpiryNote) {
        panel.add(new JMyLabel("This security notice will continue to be displayed for at least 24 hours from"), new GridBagConstraints(0, posY, 2, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(1, 5, 1, 5), 0, 0));
        posY ++;
        panel.add(new JMyLabel("last recovery attempt."), new GridBagConstraints(0, posY, 2, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(1, 5, 5, 5), 0, 0));
        posY ++;
      }
    } else if (initialRecoveryRecord == null) {
      JLabel warningLabel = new JMyLabel(Images.get(ImageNums.SHIELD32));
      warningLabel.setText("<html>Your Password Recovery options have not been initialized.</html>");
      warningLabel.setHorizontalAlignment(JLabel.LEFT);
      //warningLabel.setVerticalTextPosition(JLabel.TOP);
      warningLabel.setBorder(new LineBorder(warningLabel.getBackground().darker(), 1, true));
      warningLabel.setPreferredSize(new Dimension(410, 40));
      panel.add(warningLabel, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(0, 1, 1, 1), 20, 20));
      posY ++;
    } else {
      panel.add(new JMyLabel("Password Hint:"), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(10, 5, 5, 5), 0, 0));
      JMyTextField hintField = new JMyTextField(initialRecoveryRecord.hint);
      hintField.setEditable(false);
      panel.add(hintField, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(10, 5, 5, 5), 0, 0));
      posY ++;

      if (initialRecoveryRecord.hint.length() == 0) {
        JLabel warningLabel = new JMyLabel(Images.get(ImageNums.SHIELD32));
        warningLabel.setText("<html>Your Password Hint is disabled.</html>");
        warningLabel.setHorizontalAlignment(JLabel.LEFT);
        //warningLabel.setVerticalTextPosition(JLabel.TOP);
        warningLabel.setBorder(new LineBorder(warningLabel.getBackground().darker(), 1, true));
        warningLabel.setPreferredSize(new Dimension(410, 40));
        panel.add(warningLabel, new GridBagConstraints(0, posY, 2, 1, 10, 0,
            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(5, 1, 10, 1), 20, 20));
        posY ++;
      }

      panel.add(new JMyLabel("Recovery Questions:"), new GridBagConstraints(0, posY, 2, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;

      if (initialRecoveryRecord.isEnabledRecovery() && initialRecoveryRecord.isEnabledQA()) {

        panel.add(new JMyLabel("Please note that the exact spelling of Answers is required."), new GridBagConstraints(0, posY, 2, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
        posY ++;

        for (int i=0; i<jAs.length; i++) {
          panel.add(new JMyLabel("Question " + (i+1) + ":"), new GridBagConstraints(0, posY, 1, 1, 0, 0,
              GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 1, 5), 0, 0));
          panel.add(new JMyLabel(initialRecoveryRecord.questions[i]), new GridBagConstraints(1, posY, 1, 1, 10, 0,
              GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));
          posY ++;
          panel.add(new JMyLabel("Answer " + (i+1) + ":"), new GridBagConstraints(0, posY, 1, 1, 0, 0,
              GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(1, 5, 5, 5), 0, 0));
          panel.add(jAs[i], new GridBagConstraints(1, posY, 1, 1, 10, 0,
              GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));
          posY ++;
        }
      } else {
        JLabel warningLabel = new JMyLabel(Images.get(ImageNums.SHIELD32));
        warningLabel.setText("<html>Your Question and Answer Password Recovery is disabled.</html>");
        warningLabel.setHorizontalAlignment(JLabel.LEFT);
        //warningLabel.setVerticalTextPosition(JLabel.TOP);
        warningLabel.setBorder(new LineBorder(warningLabel.getBackground().darker(), 1, true));
        warningLabel.setPreferredSize(new Dimension(410, 40));
        panel.add(warningLabel, new GridBagConstraints(0, posY, 2, 1, 10, 0,
            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(5, 1, 1, 1), 10, 10));
        posY ++;
      }
    }

    // filler
    panel.add(new JLabel(), new GridBagConstraints(0, posY, 1, 1, 0, 10,
        GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new MyInsets(0,0,0,0), 0, 0));

    return panel;
  }

  private void setEnabledInputs(boolean b) {
  }

  public String getRecoveredPassword() {
    return recoveredPassword;
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

  private class OKThread extends ThreadTraced {

    public OKThread() {
      super("PassRecoveryRecoverDialog OKThread");
      setDaemon(true);
    }
    public void runTraced() {
      setEnabledInputs(false);
      boolean error = false;

      int countAnswers = 0;
      String[] answers = new String[jAs.length];
      String[] answersNormalized = new String[jAs.length];
      byte[][] answersHashMD5 = new byte[jAs.length][];

      for (int i=0; i<jAs.length; i++) {
        answers[i] = jAs[i].getText();
        String answerNorm = PassRecoveryRecord.normalizeAnswer(answers[i]);
        if (answerNorm.length() > 0) {
          answersNormalized[i] = answerNorm;
          countAnswers ++;
        }
      }

      // check if minimum number of answers is specified
      if (!error && countAnswers < initialRecoveryRecord.minAs.shortValue()) {
        error = true;
        MessageDialog.showWarningDialog(PassRecoveryRecoverDialog.this, new JMyLabel("You must specify at least " + initialRecoveryRecord.minAs + " answers to decrypt and recover your password."), com.CH_gui.lang.Lang.rb.getString("msgTitle_Invalid_Input"), false);
      }

      if (!error) {
        MessageDigest md5 = null;
        try {
          md5 = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i=0; i<jAs.length; i++) {
          md5.reset();
          if (answersNormalized[i] != null)
            answersHashMD5[i] = md5.digest(Misc.convStrToBytes(answersNormalized[i]));
        }

        boolean success = false;
        Obj_List_Co request = new Obj_List_Co();
        request.objs = new Object[2];
        request.objs[0] = initialRecoveryRecord.userId;
        request.objs[1] = new Obj_List_Co(answersHashMD5);
        serverInterfaceLayer.submitAndWait(new MessageAction(CommandCodes.USR_Q_PASS_RECOVERY_GET_COMPLETE, request), 30000);
        PassRecoveryRecord passRecoveryRec = cache.getMyPassRecoveryRecord();
        if (passRecoveryRec.encPassList != null && passRecoveryRec.encPassList.length > 0) {
          char[] passChars = passRecoveryRec.recoverPassword(passRecoveryRec.questions, answers);
          if (passChars != null) {
            success = true;
            recoveredPassword = new String(passChars);
            JPanel mainPanel = new JPanel();
            JPanel passPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.add(new JMyLabel("Your password is:"), BorderLayout.NORTH);
            mainPanel.add(passPanel, BorderLayout.CENTER);
            passPanel.setLayout(new GridBagLayout());
            for (int i=0; i<passChars.length; i++) {
              JLabel letter = new JMyLabel("" + passChars[i]);
              letter.setBorder(new LineBorder(Color.lightGray));
              letter.setHorizontalAlignment(JLabel.CENTER);
              letter.setFont(letter.getFont().deriveFont(Font.BOLD));
              passPanel.add(letter, new GridBagConstraints(i, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(2, 2, 2, 2), 10, 10));
            }
            MessageDialog.showInfoDialog(PassRecoveryRecoverDialog.this, mainPanel, "Password", true);
          } else {
            MessageDialog.showErrorDialog(PassRecoveryRecoverDialog.this, new JMyLabel("Could not decrypt password characters from information provided."), "Password Recovery Failed", true);
          }
        } else {
          //JOptionPane.showMessageDialog(PassRecoveryRecoverDialog.this, new JMyLabel("Could not fetch Password Recovery data."), "Password Recovery Failed", JOptionPane.ERROR_MESSAGE);
        }
        error = !success;
      }

      if (!error) {
        Component parent = getParent();
        closeDialog();
        if (parent instanceof LoginFrame) {
          LoginFrame loginFrame = (LoginFrame) parent;
          loginFrame.setPassword(getRecoveredPassword());
        }
      } else {
        // if error occurred than enable inputs
        setEnabledInputs(true);
        recoveredPassword = null;
      }
    }
  }
}
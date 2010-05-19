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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

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
public class PassRecoverySetupDialog extends GeneralDialog {

  private static final int DEFAULT_OK_BUTTON_INDEX = 0;
  private static final int DEFAULT_CANCEL_BUTTON_INDEX = 1;

  private String[] QUESTION_NUMBERS = new String[] { "1", "2", "3", "4", "5" };

  private JButton okButton;
  private JButton cancelButton;

  private static int DEFAULT_NUM_QUESTIONS = 3;
  private static int DEFAULT_NUM_ANSWERS = 2;
  private static String[] DEFAULT_QUESTIONS = new String[] {
    "What is your mother's maiden name?",
    "What city were you born in?",
    "What street did you first live on?",
    "What was the name of your elementary school?",
    "What city was your mother born in?",
    "What city was your father born in?",
    "What is your nickname?",
    "What is your favorite pet name?",
    "What is your favorite color?",
     "What city did you give birth in?",
    "<type your own question>"
  };

  private int initial_num_questions = DEFAULT_NUM_QUESTIONS;
  private int initial_num_answers = DEFAULT_NUM_ANSWERS;
  private String[] initial_questions = DEFAULT_QUESTIONS;

  private JMyCheckBox jEnableRecovery;
  private JLabel jOldPassLabel;
  private JMyPasswordKeyboardField jOldPass;
  private JMyCheckBox jEnableHint;
  private JMyTextField jPassHint;
  private JMyCheckBox jEnableQA;
  private JMyTextOptionField jNumQuestionsTotal;
  private JMyTextOptionField jNumAnswersMin;
  private JLabel[] jQLabels;
  private JLabel[] jALabels;
  private JMyTextOptionField[] jQs;
  private JMyTextField[] jAs;

  private JPanel panelPass;
  private JPanel panelRec;
  private JPanel panelPlainRec;
  private JPanel panelEncRec;

  private ServerInterfaceLayer serverInterfaceLayer;
  private FetchedDataCache cache;
  private PassRecoveryRecord initialRecoveryRecord;

  /** Creates new PassRecoverySetupDialog */
  public PassRecoverySetupDialog(Frame frame) {
    super(frame, "Password Recovery");
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PassRecoverySetupDialog.class, "PassRecoverySetupDialog(Frame frame)");

    serverInterfaceLayer = MainFrame.getServerInterfaceLayer();
    cache = serverInterfaceLayer.getFetchedDataCache();
    initialRecoveryRecord = cache.getMyPassRecoveryRecord();
    if (initialRecoveryRecord != null) {
      initial_num_questions = initialRecoveryRecord.numQs.shortValue();
      initial_num_answers = initialRecoveryRecord.minAs.shortValue();
      for (int i=0; initialRecoveryRecord.questions!=null && i<initialRecoveryRecord.questions.length; i++)
        if (initialRecoveryRecord.questions[i] != null && initialRecoveryRecord.questions[i].length() > 0)
          initial_questions[i] = initialRecoveryRecord.questions[i];
    }

    JButton[] buttons = createButtons();
    JPanel passRecoveryPanel = createMainPanel(initialRecoveryRecord);

    // Make sure all our components have proper enablement
    setEnabledInputs();

    super.init(frame, buttons, new JScrollPane(passRecoveryPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), new JMyLabel(Images.get(ImageNums.LOGO_BANNER_MAIN)), DEFAULT_OK_BUTTON_INDEX, DEFAULT_CANCEL_BUTTON_INDEX);

    if (trace != null) trace.exit(PassRecoverySetupDialog.class);
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[2];
    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
    buttons[0].setDefaultCapable(true);
    buttons[0].addActionListener(new OKActionListener());
    okButton = buttons[0];

    buttons[1] = new JMyButton("Not now");
    buttons[1].addActionListener(new CancelActionListener());
    cancelButton = buttons[1];

    return buttons;
  }

  private JPanel createMainPanel(PassRecoveryRecord initialRecoveryRecord) {
    JPanel panel = new JPanel();

    panel.setLayout(new GridBagLayout());

    jEnableRecovery = new JMyCheckBox("Enable password recovery", true);
    //jEnableRecovery.setSelected(initialRecoveryRecord != null && initialRecoveryRecord.isEnabledRecovery());
    jEnableRecovery.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        setEnabledInputs();
      }
    });
    jOldPass = new JMyPasswordKeyboardField();
    jEnableHint = new JMyCheckBox("Enable password hint", initialRecoveryRecord != null && initialRecoveryRecord.hint.length() > 0);
    jEnableHint.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        setEnabledInputs();
      }
    });
    jPassHint = new JMyTextField(5);
    if (initialRecoveryRecord != null)
      jPassHint.setText(initialRecoveryRecord.hint);
    jEnableQA = new JMyCheckBox("Enable encrypted password recovery with questions and answers", initialRecoveryRecord != null && initialRecoveryRecord.isEnabledQA());
    jEnableQA.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        setEnabledInputs();
      }
    });
    jNumQuestionsTotal = new JMyTextOptionField("" + initial_num_questions, new JMyDropdownIcon(), null, QUESTION_NUMBERS);
    jNumQuestionsTotal.setEditable(false);
    jNumQuestionsTotal.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
      }
      public void insertUpdate(DocumentEvent e) {
        int numQs = Integer.parseInt(jNumQuestionsTotal.getText());
        capMaxAnswers();
        for (int i=0; i<jQs.length; i++) {
          jQLabels[i].setVisible(i<numQs);
          jALabels[i].setVisible(i<numQs);
          jQs[i].setVisible(i<numQs);
          jAs[i].setVisible(i<numQs);
        }
        PassRecoverySetupDialog.this.setSize(PassRecoverySetupDialog.this.getSize().width, PassRecoverySetupDialog.this.getPreferredSize().height);
        MiscGui.adjustSizeAndLocationToFitScreen(PassRecoverySetupDialog.this);
      }
      public void removeUpdate(DocumentEvent e) {
      }
    });
    jNumAnswersMin = new JMyTextOptionField("" + initial_num_answers, new JMyDropdownIcon(), null, QUESTION_NUMBERS);
    jNumAnswersMin.setEditable(false);
    capMaxAnswers();
    jQs = new JMyTextOptionField[QUESTION_NUMBERS.length]; // exclude the last option where user is invited to make his own Question
    jAs = new JMyTextField[jQs.length];
    jQLabels = new JMyLabel[jQs.length];
    jALabels = new JLabel[jQs.length];
    for (int i=0; i<jQs.length; i++) {
      jQs[i] = new JMyTextOptionField(initial_questions[i], new JMyDropdownIcon(), null, initial_questions);
      jAs[i] = new JMyTextField(5);
      jQLabels[i] = new JMyLabel("Question " + (i+1) + ":");
      jALabels[i] = new JMyLabel(); //"Answer " + (i+1) + ":");
      jQs[i].setVisible(i < initial_num_questions);
      jAs[i].setVisible(i < initial_num_questions);
      jQLabels[i].setVisible(i < initial_num_questions);
      jALabels[i].setVisible(i < initial_num_questions);
    }

    int posY = 0;
//    panel.add(new JMyLabel(Images.get(ImageNums.LOGO_BANNER_MAIN)), new GridBagConstraints(0, posY, 4, 1, 0, 0, // LOGO_KEY_MAIN
//        GridBagConstraints.CENTER, GridBagConstraints.NONE, new MyInsets(0, 0, 0, 0), 0, 0));
//    posY ++;

    JLabel warningLabel = new JMyLabel(Images.get(ImageNums.SHIELD32));
    warningLabel.setText("<html>Password recovery allows you (and everyone else) to recover a lost password. Please setup your recovery questions and answers so that you and only you can answer them. This is an optional feature, and if you choose to use it your account security will depend on the quality of the questions and answers that you provide.</html>");
    warningLabel.setHorizontalAlignment(JLabel.LEFT);
    warningLabel.setVerticalTextPosition(JLabel.TOP);
    warningLabel.setBorder(new LineBorder(warningLabel.getBackground().darker(), 1, true));
    warningLabel.setPreferredSize(new Dimension(390, 85));
    panel.add(warningLabel, new GridBagConstraints(0, posY, 4, 1, 10, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(0, 1, 5, 1), 20, 20));
    posY ++;

    Insets insets3333 = new MyInsets(3,3,3,3);

    panel.add(jEnableRecovery, new GridBagConstraints(0, posY, 4, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, insets3333, 0, 0));
    posY ++;

    jOldPassLabel = new JMyLabel("Confirm Password");
    panelPass = new JPanel();
    panelPass.setLayout(new GridBagLayout());
    panelPass.setBorder(new EmptyBorder(0,0,0,0));
    panel.add(panelPass, new GridBagConstraints(0, posY, 4, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, insets3333, 0, 0));
    posY ++;

    panelPass.add(jOldPassLabel, new GridBagConstraints(0, posY, 2, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, insets3333, 0, 0));
    panelPass.add(jOldPass, new GridBagConstraints(2, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets3333, 0, 0));
    posY ++;

    panelRec = new JPanel();
    panelRec.setLayout(new GridBagLayout());
    panelRec.setBorder(new LineBorder(panelRec.getBackground().darker(), 1, true));
    panel.add(panelRec, new GridBagConstraints(0, posY, 4, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, insets3333, 0, 0));
    posY ++;

    panelRec.add(jEnableHint, new GridBagConstraints(0, posY, 4, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, insets3333, 0, 0));
    posY ++;

    panelPlainRec = new JPanel();
    panelPlainRec.setLayout(new GridBagLayout());
    panelPlainRec.setBorder(new LineBorder(panelPlainRec.getBackground().darker(), 1, true));
    panelRec.add(panelPlainRec, new GridBagConstraints(0, posY, 4, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, insets3333, 0, 0));
    posY ++;

    panelRec.add(jEnableQA, new GridBagConstraints(0, posY, 4, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, insets3333, 0, 0));
    posY ++;

    panelEncRec = new JPanel();
    panelEncRec.setLayout(new GridBagLayout());
    panelEncRec.setBorder(new LineBorder(panelEncRec.getBackground().darker(), 1, true));
    panelRec.add(panelEncRec, new GridBagConstraints(0, posY, 4, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, insets3333, 0, 0));
    posY ++;

    // Plain Recovery

    panelPlainRec.add(new JMyLabel("Password Hint is considered to be readily available public information."), new GridBagConstraints(0, posY, 4, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, insets3333, 0, 0));
    posY ++;

    panelPlainRec.add(new JMyLabel("Password Hint:"), new GridBagConstraints(0, posY, 2, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, insets3333, 0, 0));
    panelPlainRec.add(jPassHint, new GridBagConstraints(2, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets3333, 0, 0));
    posY ++;

    // Encrypted Recovery

//    panelEncRec.add(new JMyLabel("Recovery Questions:"), new GridBagConstraints(0, posY, 4, 1, 0, 0, 
//        GridBagConstraints.WEST, GridBagConstraints.NONE, insets3333, 0, 0));
//    posY ++;

    panelEncRec.add(new JMyLabel("Number of Questions and Answers I want to specify:"), new GridBagConstraints(0, posY, 3, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, insets3333, 0, 0));
    panelEncRec.add(jNumQuestionsTotal, new GridBagConstraints(3, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets3333, 0, 0));
    posY ++;

    panelEncRec.add(new JMyLabel("Minimum number of Answers required at recovery time:"), new GridBagConstraints(0, posY, 3, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(3, 5, 1, 5), 0, 0));
    panelEncRec.add(jNumAnswersMin, new GridBagConstraints(3, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 1, 5), 0, 0));
    posY ++;

    panelEncRec.add(new JMyLabel("Note: The exact spelling of Answers will be required at recovery time."), new GridBagConstraints(0, posY, 4, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(1, 5, 3, 5), 0, 0));
    posY ++;

    for (int i=0; i<jQs.length; i++) {
      panelEncRec.add(jQLabels[i], new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(3, 5, 0, 5), 0, 0));
      panelEncRec.add(jQs[i], new GridBagConstraints(1, posY, 3, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 0, 5), 0, 0));
      posY ++;
      panelEncRec.add(jALabels[i], new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0, 5, 3, 5), 0, 0));
      panelEncRec.add(jAs[i], new GridBagConstraints(1, posY, 3, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 5, 3, 5), 0, 0));
      posY ++;
    }

    // filler
    panel.add(new JLabel(), new GridBagConstraints(0, posY, 1, 1, 0, 10,
        GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new MyInsets(0,0,0,0), 0, 0));

    return panel;
  }

  private void capMaxAnswers() {
    int numQs = Integer.parseInt(jNumQuestionsTotal.getText());
    int minAs = Integer.parseInt(jNumAnswersMin.getText());
    if (minAs > numQs)
      jNumAnswersMin.setText("" + numQs);
    String[] answerOptions = new String[numQs];
    for (int i=0; i<numQs; i++)
      answerOptions[i] = QUESTION_NUMBERS[i];
    jNumAnswersMin.updateOptions(answerOptions);
  }

  private void setEnabledInputs() {
    boolean enable = jEnableRecovery.isSelected();
    jEnableHint.setEnabled(enable);
    jPassHint.setEnabled(enable && jEnableHint.isSelected());
    jEnableQA.setEnabled(enable);
    jNumQuestionsTotal.setEnabled(enable && jEnableQA.isSelected());
    jNumAnswersMin.setEnabled(enable && jEnableQA.isSelected());
    for (int i=0; i<jQs.length; i++)
      jQs[i].setEnabled(enable && jEnableQA.isSelected());
    for (int i=0; i<jAs.length; i++)
      jAs[i].setEnabled(enable && jEnableQA.isSelected());
    boolean packPending = false;
    if (panelPass.isVisible() != (enable || (initialRecoveryRecord != null && initialRecoveryRecord.isEnabledRecovery()))) {
      panelPass.setVisible(enable || (initialRecoveryRecord != null && initialRecoveryRecord.isEnabledRecovery()));
      packPending = true;
    }
    if (panelRec.isVisible() != enable) {
      panelRec.setVisible(enable);
      packPending = true;
    }
    if (panelPlainRec.isVisible() != jEnableHint.isSelected()) {
      panelPlainRec.setVisible(jEnableHint.isSelected());
      packPending = true;
    }
    if (panelEncRec.isVisible() != jEnableQA.isSelected()) {
      panelEncRec.setVisible(jEnableQA.isSelected());
      packPending = true;
    }
    okButton.setText(enable ? "Enable" : "Disable");
    if (packPending)
      pack();
  }

  private void setEnabledInputs(boolean b) {
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

  /* @return encoded password entered by the user */
  public BAEncodedPassword getBAEncodedPassword(JMyPasswordKeyboardField jPass) {
    return UserRecord.getBAEncodedPassword(jPass.getPassword(), cache.getUserRecord().handle);
  }


  private class OKThread extends ThreadTraced {

    public OKThread() {
      super("PassRecoverySetupDialog OKThread");
      setDaemon(true);
    }
    public void runTraced() {
      setEnabledInputs(false);
      boolean error = false;

      // check if Password Recovery already exists then disabling it requires password confirmation
      if (!error && !jEnableRecovery.isSelected() && initialRecoveryRecord != null && initialRecoveryRecord.isEnabledRecovery()) {
        // check if old password matches
        BAEncodedPassword ba = getBAEncodedPassword(jOldPass);
        if (!error && !cache.getEncodedPassword().equals(ba)) {
          error = true;
          MessageDialog.showErrorDialog(PassRecoverySetupDialog.this, "Please enter your account password.  You must confirm your account password to disable existing Password Recovery settings.", com.CH_gui.lang.Lang.rb.getString("msgTitle_Invalid_Input"));
          jOldPass.setText("");
        }
      }

      // check if old password is specified
      if (!error && jEnableRecovery.isSelected()) {
        if (jOldPass.getPassword().length == 0) {
          error = true;
          MessageDialog.showErrorDialog(PassRecoverySetupDialog.this, "Please specify current account password.", com.CH_gui.lang.Lang.rb.getString("msgTitle_Invalid_Input"));
        }
        // check if old password matches
        BAEncodedPassword ba = getBAEncodedPassword(jOldPass);
        if (!error && !cache.getEncodedPassword().equals(ba)) {
          error = true;
          MessageDialog.showErrorDialog(PassRecoverySetupDialog.this, "Specified password does not match the current account password.", com.CH_gui.lang.Lang.rb.getString("msgTitle_Invalid_Input"));
          jOldPass.setText("");
        }
      }

      // Enabling recovery requires at least one method to be enabled
      if (!error && jEnableRecovery.isSelected() && !jEnableHint.isSelected() && !jEnableQA.isSelected()) {
        error = true;
        MessageDialog.showErrorDialog(PassRecoverySetupDialog.this, "To enable Password Recovery, please select at least one recovery option.", com.CH_gui.lang.Lang.rb.getString("msgTitle_Invalid_Input"));
      }

      // Password Hint must not contain the password
      if (!error && jEnableRecovery.isSelected() && jEnableHint.isSelected() && jPassHint.getText().length() > 0 && jPassHint.getText().indexOf(new String(jOldPass.getPassword())) >= 0) {
        error = true;
        MessageDialog.showErrorDialog(PassRecoverySetupDialog.this, "Password hint must not contain the password.", com.CH_gui.lang.Lang.rb.getString("msgTitle_Invalid_Input"));
      }

      // check if all questions have answers
      if (!error && jEnableRecovery.isSelected() && jEnableQA.isSelected()) {
        int numQs = Integer.parseInt(jNumQuestionsTotal.getText());
        boolean allAnswerd = true;
        for (int i=0; i<numQs; i++) {
          if (PassRecoveryRecord.normalizeAnswer(jAs[i].getText()).length() == 0) {
            allAnswerd = false;
            break;
          }
        }
        if (!allAnswerd) {
          MessageDialog.showErrorDialog(PassRecoverySetupDialog.this, "Please provide answers to all chosen questions.", com.CH_gui.lang.Lang.rb.getString("msgTitle_Invalid_Input"));
          error = true;
        }
      }

      if (!error) {
        PassRecoveryRecord passRecoveryRecord = new PassRecoveryRecord();
        int numQs = Integer.parseInt(jNumQuestionsTotal.getText());
        int numAs = Integer.parseInt(jNumAnswersMin.getText());
        String[] questions = new String[numQs];
        String[] answers = new String[numQs];
        for (int i=0; i<numQs; i++) {
          questions[i] = jQs[i].getText();
          answers[i] = jAs[i].getText();
        }
        passRecoveryRecord.setData(jEnableRecovery.isSelected(), jOldPass.getPassword(), jEnableHint.isSelected() ? jPassHint.getText() : "", jEnableQA.isSelected(), numQs, numAs, questions, answers);
        boolean success = UserOps.sendPassRecoverySettings(serverInterfaceLayer, passRecoveryRecord);
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
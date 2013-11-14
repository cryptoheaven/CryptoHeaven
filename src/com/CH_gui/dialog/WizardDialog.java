/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.dialog;

import com.CH_co.trace.*;

import com.CH_gui.gui.*;
import com.CH_gui.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.6 $</b>
 *
 * @author  Marcin Kurzawa
 */
public abstract class WizardDialog extends GeneralDialog {

  private static final int DEFAULT_FINISH_INDEX = 2;
  private static final int DEFAULT_CANCEL_INDEX = 3;

  private Component parent;

  private JTabbedPane jTabbedPane;
  private int selectedTabIndex = 0;
  private int selectedTabIndexPrev = 0;

  private JButton buttonPrevious;
  private JButton buttonNext;
  private JButton buttonFinish;
  private JButton buttonCancel;

  /** Creates new WizardDialog */
  public WizardDialog(Frame parent, String title) {
    super(parent, title);
    this.parent = parent;
  }
  public WizardDialog(Dialog parent, String title) {
    super(parent, title);
    this.parent = parent;
  }

  /**
   * Initializes the wizard and constructs/shows the GUI.
   * Call this when you want the wizard dialog to be constructed and shown.
   */
  public void initialize() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WizardDialog.class, "initialize()");

    JButton[] buttons = createButtons();
    JComponent mainComp = createMainPanel();
    setEnabledButtons();

    super.init(parent, buttons, mainComp, MiscGui.createLogoHeader(), DEFAULT_FINISH_INDEX, DEFAULT_CANCEL_INDEX);

    if (trace != null) trace.exit(WizardDialog.class);
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[4];

    buttons[0] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Previous"));
    buttons[0].addActionListener(new PreviousActionListener());
    buttonPrevious = buttons[0];

    buttons[1] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Next"));
    buttons[1].addActionListener(new NextActionListener());
    buttonNext = buttons[1];

    buttons[2] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Finish"));
    buttons[2].setDefaultCapable(true);
    buttons[2].addActionListener(new FinishActionListener());
    buttonFinish = buttons[2];

    buttons[3] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Cancel"));
    buttons[3].addActionListener(new CancelActionListener());
    buttonCancel = buttons[3];

    return buttons;
  }

  private JComponent createMainPanel() {
    jTabbedPane = new JMyTabbedPane();
    String[] wizardTabNames = getWizardTabNames();
    JComponent[] wizardPages = createWizardPages();
    for (int i=0; wizardPages!=null && i<wizardPages.length; i++) {
      jTabbedPane.addTab(wizardTabNames[i], wizardPages[i]);
    }

    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

//    panel.add(new JMyLabel(Images.get(ImageNums.LOGO_BANNER_MAIN)), new GridBagConstraints(0, 0, 1, 1, 0, 0, 
//        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
    panel.add(jTabbedPane, new GridBagConstraints(0, 1, 1, 1, 10, 10,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));

    jTabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        int tempPrevTab = selectedTabIndex;
        int tempNewTab = jTabbedPane.getSelectedIndex();
        if (tempPrevTab != tempNewTab && tempNewTab >= 0) {
          if (goFromTab(tempPrevTab)) {
            selectedTabIndexPrev = tempPrevTab;
            goToTab(tempNewTab);
            selectedTabIndex = tempNewTab;
          } else {
            jTabbedPane.setSelectedIndex(selectedTabIndex);
          }
        }
        setEnabledButtons();
      }
    });

    return panel;
  }

  public void setEnabledButtons() {
    buttonPrevious.setEnabled(selectedTabIndex > 0);
    buttonNext.setEnabled(selectedTabIndex + 1 < jTabbedPane.getTabCount());
    buttonFinish.setEnabled(selectedTabIndex + 1 == jTabbedPane.getTabCount() && isFinishActionReady());
    buttonCancel.setEnabled(true);
  }

  /**
   * Invoked to disable inputs when finishTaskRunner() is in progress,
   * or enable inputs when it is done running.
   */
  private void setEnabledInputs(boolean b) {
    if (b) {
      setEnabledButtons();
      jTabbedPane.setEnabled(true);
    } else {
      jTabbedPane.setEnabled(false);
      buttonPrevious.setEnabled(false);
      buttonNext.setEnabled(false);
      buttonFinish.setEnabled(false);
      //buttonCancel.setEnabled(false); // always enabled because wizard is interruptable
    }
    setEnabledInputComponents(b);
  }



  private class PreviousActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      jTabbedPane.setSelectedIndex(selectedTabIndex - 1);
    }
  }

  private class NextActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      jTabbedPane.setSelectedIndex(selectedTabIndex + 1);
    }
  }

  private class FinishActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      new FinishThread().start();
    }
  }

  private class CancelActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      setInterruptProgress(true);
      closeDialog();
    }
  }


  /**
   * Thread that takes all input data and runs the action.
   */
  private class FinishThread extends ThreadTraced {

    public FinishThread() {
      super("Wizard Dialog FinishThread");
    }
    public void runTraced() {
      setEnabledInputs(false);
      // change the priority of this thread to minimum
      setPriority(MIN_PRIORITY);
      boolean error = false;

      // reset interrupt flag
      setInterruptProgress(false);
      // perform main action here
      error = !finishTaskRunner();

      if (!error) {
        closeDialog();
      } else {
        // if error occurred than enable inputs
        setEnabledInputs(true);
        validate();
      }
    }
  } // end class OKThread



  /**
   * Overwrite to return tab names for the pages in wizard.
   * @return names for tabs
   */
  public abstract String[] getWizardTabNames();

  /**
   * Overwrite to return panels for the pages in wizard
   * Makes panels to be displayed on wizard pages.
   */
  public abstract JComponent[] createWizardPages();

  /**
   * Overwrite to check if tab is ready to be left for another tab.
   * Informs the wizard that tab is about to change from the current one.
   * @returns fales if choices on current tab are invalid or incomplete
   */
  public abstract boolean goFromTab(int tabIndex);
  /**
   * Overwrite to be informed when new tab is about to become visible.
   */
  public void goToTab(int tabIndex) {
  }

  /**
   * Overwrite if you need to interrupt actions when Cancel button is pressed.
   * This action should interrupt thread inside the finishTaskRunner() if any.
   */
  public abstract void setInterruptProgress(boolean interrupt);

  /**
   * Overwrite to specify when Finish button is eligible to become enabled.
   * @return true iff Finish button should enable and all required inputs are satisfied.
   */
  public abstract boolean isFinishActionReady();

  /**
   * Overwrite if you need to set enablement of components during Finish action.
   * Invoked to disable inputs when finishTaskRunner() is in progress,
   * or enable inputs when it is done running.
   */
  public void setEnabledInputComponents(boolean enable) {
  }

  /**
   * Overwrite to specify action which needs to be taken when Finish button is pressed.
   * Invoked by FinishThread to do the main action of the wizard.
   * @return true iff success
   */
  public abstract boolean finishTaskRunner();

  /**
   * Test Blank Wizard
   */
  private static void main(String[] args) {
    //new WizardDialog((Frame) null, "title").initialize();
  }

}
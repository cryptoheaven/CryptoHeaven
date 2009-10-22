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

package com.CH_co.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import com.CH_co.gui.*;
import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.15 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class MessageDialog extends Object {

  public static final int QUESTION_MESSAGE = 9001;
  public static final int INFORMATION_MESSAGE = 9002;
  public static final int WARNING_MESSAGE = 9003;
  public static final int ERROR_MESSAGE = 9004;
  public static final int RECYCLE_MESSAGE = 9005;

  /** Show dialog with "OK" button */
  public static void showDialog(Component parent, String messageText, String title, int messageType, boolean modal) {
    showDialog(parent, messageText, title, messageType, null, null, modal);
  }
  /**
   * Shows the message dialog. If no buttons are specified, and 'defaultButtonAction' is specified, 
   * then it will be run when user clicks the default OK button.
   * @param buttons is optional
   * @return the dialog which is created and shown
   */
  public static JDialog showDialog(Component parent, String messageText, String title, int messageType, JButton[] buttons, boolean modal) {
    return showDialog(parent, messageText, title, messageType, buttons, null, modal);
  }
  public static JDialog showDialog(Component parent, String messageText, String title, int messageType, JButton[] buttons, ActionListener defaultButtonAction, boolean modal) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageDialog.class, "showDialog(Component parent, String messageText, String title, int messageType, JButton[] buttons, ActionListener defaultButtonAction, boolean modal)");
    if (trace != null) trace.args(parent, messageText, title);
    if (trace != null) trace.args(messageType);
    if (trace != null) trace.args(modal);
    if (trace != null) trace.args(buttons, defaultButtonAction);

    JDialog dialog = null;
    if (!MiscGui.isAllGUIsuppressed() && !MiscGui.isMsgDialogsGUIsuppressed()) {
      Component message = prepareMessage(messageText);
      if (message != null) {
        dialog = showDialog(parent, message, title, messageType, buttons, defaultButtonAction, modal);
      }
    }

    if (trace != null) trace.exit(MessageDialog.class, dialog);
    return dialog;
  }
  public static JDialog showDialog(Component parent, Component message, String title, int messageType, JButton[] buttons, ActionListener defaultButtonAction, boolean modal) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageDialog.class, "showDialog(Component parent, Component message, String title, int messageType, JButton[] buttons, ActionListener defaultButtonAction, boolean modal)");
    JDialog dialog = showDialog(parent, message, title, messageType, buttons, -1, -1, defaultButtonAction, modal, true, true, true);
    if (trace != null) trace.exit(MessageDialog.class, dialog);
    return dialog;
  }
  public static JDialog showDialog(Component parent, Component message, String title, int messageType, JButton[] buttons, ActionListener defaultButtonAction, boolean modal, boolean playSound, boolean sizeBelowMaximum) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageDialog.class, "showDialog(Component parent, Component message, String title, int messageType, JButton[] buttons, ActionListener defaultButtonAction, boolean modal, boolean playSound, boolean sizeBelowMaximum)");
    JDialog dialog = showDialog(parent, message, title, messageType, buttons, -1, -1, defaultButtonAction, modal, playSound, sizeBelowMaximum, true);
    if (trace != null) trace.exit(MessageDialog.class, dialog);
    return dialog;
  }
  public static JDialog showDialog(Component parent, Component message, String title, int messageType, JButton[] buttons, int default_index, int default_cancel, ActionListener defaultButtonAction, boolean modal, boolean playSound, boolean sizeBelowMaximum, boolean sizeAboveMinimum) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageDialog.class, "showDialog(Component parent, Component message, String title, int messageType, JButton[] buttons, ActionListener defaultButtonAction, boolean modal, boolean playSound, boolean sizeBelowMaximum, boolean sizeAboveMinimum)");

    if (trace != null) trace.args(parent, message, title);
    if (trace != null) trace.args(messageType);
    if (trace != null) trace.args(modal);
    if (trace != null) trace.args(buttons, defaultButtonAction);

    GeneralDialog dialog = null;

    if (!MiscGui.isAllGUIsuppressed() && !MiscGui.isMsgDialogsGUIsuppressed()) {

      // See if we need to attach default action
      if (buttons == null) {
        buttons = new JButton[] { new JMyButton("OK") };
        buttons[0].setDefaultCapable(true);
        if (defaultButtonAction != null) {
          buttons[0].addActionListener(defaultButtonAction);
        } else {
          buttons[0].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
              Window w = SwingUtilities.windowForComponent((Component)event.getSource());
              w.setVisible(false);
              w.dispose();
            }
          });
        }
      }

      // Upgrade parent to a window component
      if (parent == null)
        parent = GeneralDialog.getDefaultParent();
      else if (!(parent instanceof Window)) {
        parent = SwingUtilities.windowForComponent(parent);
      }

      JLabel icon = null;
      switch (messageType) {
        case QUESTION_MESSAGE:
          icon = new JMyLabel((Icon) UIManager.getLookAndFeelDefaults().get("OptionPane.questionIcon"));
          break;
        case INFORMATION_MESSAGE:
          icon = new JMyLabel((Icon) UIManager.getLookAndFeelDefaults().get("OptionPane.informationIcon"));
          break;
        case WARNING_MESSAGE:
          icon = new JMyLabel((Icon) UIManager.getLookAndFeelDefaults().get("OptionPane.warningIcon"));
          break;
        case ERROR_MESSAGE:
          icon = new JMyLabel((Icon) UIManager.getLookAndFeelDefaults().get("OptionPane.errorIcon"));
          break;
        case RECYCLE_MESSAGE:
          icon = new JMyLabel(Images.get(ImageNums.RECYCLE_LARGE));
          break;
      }

      JPanel panel = new JPanel();
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      panel.setLayout(new GridBagLayout());

      if (icon != null) {
        panel.add(icon, new GridBagConstraints(0, 0, 1, 1, 0, 0, 
            GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new MyInsets(10, 10, 10, 10), 0, 0));
      }
      panel.add(message, new GridBagConstraints(1, 0, 1, 1, 10, 10, 
          GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(10, 10, 10, 10), 0, 0));

      if (parent instanceof Frame) {
        dialog = new GeneralDialog((Frame) parent, title);
      } else if (parent instanceof Dialog) {
        dialog = new GeneralDialog((Dialog) parent, title);
      } else {
        dialog = new GeneralDialog(title);
      }

      if (playSound) {
        playSound(messageType);
      }

      dialog.toFront(); // multiple attempts to bring this window to front, some platforms are buggy with this
      dialog.setModal(modal);
      dialog.init(parent, buttons, panel, default_index, default_cancel, false);

      if (sizeBelowMaximum) {
        // trim size of the dialog
        Dimension dim = dialog.getSize();
        int width = dim.width;
        int height = dim.height;
        width = Math.min(width, 500); // make the dialog smaller if too big
        if (sizeAboveMinimum)
          width = Math.max(width, 250); // keep dialog at some minimum size, no less
        height = Math.min(height, 410); // make the dialog smaller if too big // must be big enough to accomodate email sending warning
        if (sizeAboveMinimum)
          height = Math.max(height, 250); // keep dialog at some minimum size, no less
        dialog.setSize(width, height);
      }

      dialog.setLocationRelativeTo(parent);
      dialog.toFront(); // multiple attempts to bring this window to front, some platforms are buggy with this
      dialog.setVisible(true);
      dialog.toFront(); // multiple attempts to bring this window to front, some platforms are buggy with this
    }

    if (trace != null) trace.exit(MessageDialog.class, dialog);
    return dialog;
  }


  /**
   * Displays modal Yes / No option dialog.
   * @return true if user clicks Yes, false for No .
   */
  public static boolean showDialogYesNo(Component parent, String messageText, String title) {
    return showDialogYesNo(parent, messageText, title, QUESTION_MESSAGE);
  }
  public static boolean showDialogYesNo(Component parent, String messageText, String title, int messageType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageDialog.class, "showDialogYesNo(Component parent, String messageText, String title, int messageType)");
    if (trace != null) trace.args(parent, messageText, title);
    if (trace != null) trace.args(messageType);

    boolean rc = false;
    if (!MiscGui.isAllGUIsuppressed() && !MiscGui.isMsgDialogsGUIsuppressed()) {
      Component message = prepareMessage(messageText);
      rc = showDialogYesNo(parent, message, title, messageType);
    }

    if (trace != null) trace.exit(MessageDialog.class, rc);
    return rc;
  }
  public static boolean showDialogYesNo(Component parent, Component message, String title) {
    return showDialogYesNo(parent, message, title, QUESTION_MESSAGE);
  }
  public static boolean showDialogYesNo(Component parent, Component message, String title, int messageType) {
    return showDialogYesNo(parent, message, title, messageType, true, null, null);
  }
  public static boolean showDialogYesNo(Component parent, Component message, String title, int messageType, boolean modal, final ActionListener yesAction, final ActionListener noAction) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageDialog.class, "showDialogYesNo(Component parent, Component message, String titlepublic static boolean showDialogYesNo(Component parent, Component message, String title, int messageType, boolean modal, final ActionListener yesAction, final ActionListener noAction)");
    if (trace != null) trace.args(parent, message, title);
    if (trace != null) trace.args(messageType);
    if (trace != null) trace.args(modal);
    if (trace != null) trace.args(yesAction, noAction);

    final boolean[] option = new boolean[1];

    if (!MiscGui.isAllGUIsuppressed() && !MiscGui.isMsgDialogsGUIsuppressed()) {
      JButton yes = new JMyButton("Yes");
      yes.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent event) {
          option[0] = true;
          Window w = SwingUtilities.windowForComponent((Component)event.getSource());
          w.setVisible(false); w.dispose();
          if (yesAction != null) {
            yesAction.actionPerformed(event);
          }
        }
      });
      // Set focus to YES button
      yes.addHierarchyListener(new HierarchyListener() {
        public void hierarchyChanged(HierarchyEvent e) {
          final Component c = e.getComponent();
          long changeFlags = e.getChangeFlags();
          if ((changeFlags & (HierarchyEvent.SHOWING_CHANGED | HierarchyEvent.DISPLAYABILITY_CHANGED)) != 0 && 
              c != null && 
              c.isShowing()) 
          {
            c.removeHierarchyListener(this);
            c.requestFocus();
          }
        }
      });
      JButton no = new JMyButton("No");
      no.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent event) {
          option[0] = false;
          Window w = SwingUtilities.windowForComponent((Component)event.getSource());
          w.setVisible(false); w.dispose();
          if (noAction != null) {
            noAction.actionPerformed(event);
          }
        }
      });
      JButton[] buttons = new JButton[] { yes, no };
      boolean sizeAboveMinimum = !(message instanceof JPanel);
      showDialog(parent, message, title, messageType, buttons, 0, 1, null, modal, true, true, sizeAboveMinimum);
    }

    if (trace != null) trace.exit(MessageDialog.class, option[0]);
    return option[0];
  }


  /* Show error dialog with "OK" button */
  public static void showErrorDialog(Component parent, String messageText, String title) {
    showDialog(parent, messageText, title, ERROR_MESSAGE, false);
  }
  /* Show error dialog with "OK" button */
  public static void showErrorDialog(Component parent, String messageText, String title, boolean modal) {
    showDialog(parent, messageText, title, ERROR_MESSAGE, modal);
  }
  /* Show warning dialog with "OK" button */
  public static void showWarningDialog(Component parent, String messageText, String title) {
    showDialog(parent, messageText, title, WARNING_MESSAGE, false);
  }
  /* Show warning dialog with "OK" button */
  public static void showWarningDialog(Component parent, String messageText, String title, boolean modal) {
    showDialog(parent, messageText, title, WARNING_MESSAGE, modal);
  }
  /* Show info dialog with "OK" button */
  public static void showInfoDialog(Component parent, String messageText, String title) {
    showDialog(parent, messageText, title, INFORMATION_MESSAGE, false);
  }
  /* Show info dialog with "OK" button */
  public static void showInfoDialog(Component parent, String messageText, String title, boolean modal) {
    showDialog(parent, messageText, title, INFORMATION_MESSAGE, modal);
  }


  public static void playSound(int msgType) {
    try {
      switch (msgType) {
        case ERROR_MESSAGE :
          Sounds.playAsynchronous(Sounds.DIALOG_ERROR);
          break;
        case WARNING_MESSAGE :
          Sounds.playAsynchronous(Sounds.DIALOG_WARN);
          break;
        case INFORMATION_MESSAGE :
          Sounds.playAsynchronous(Sounds.DIALOG_INFO);
          break;
        case QUESTION_MESSAGE :
        case RECYCLE_MESSAGE :
          Sounds.playAsynchronous(Sounds.DIALOG_QUESTION);
          break;
      }
    } catch (Throwable t) {
    }
  }

  public static Component prepareMessage(String messageText) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageDialog.class, "prepareMessage(String messageText)");
    if (trace != null) trace.args(messageText);

    JComponent comp = null;
    if (messageText != null) {
      JTextComponent textComp = null;
      if (messageText.startsWith("<html>")) {
        if (trace != null) trace.data(10, "html message");
        textComp = new HTML_ClickablePane(messageText);
        // Tried setting the preferred size of the textComp, but it seems that dialog
        // refuses to adjust its size.
      } else {
        if (trace != null) trace.data(20, "plain message");
        JTextArea textArea = new JMyTextArea(messageText, 8, 35);
        MiscGui.initKeyBindings(textArea);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textComp = textArea;
      }
      textComp.setEditable(false);

      try {
        if (trace != null) trace.data(30, "setting caret to position 0");
        textComp.setCaretPosition(0);
      } catch (Throwable t) {
        if (trace != null) trace.exception(MessageDialog.class, 35, t);
      }
      comp = new JScrollPane(textComp);
      comp.setPreferredSize(new Dimension(420, 240));
    }

    if (trace != null) trace.exit(MessageDialog.class, comp);
    return comp;
  }

}
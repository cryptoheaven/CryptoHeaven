/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.monitor;

import com.CH_co.monitor.*;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.gui.MyInsets;
import com.CH_gui.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class JournalProgMonitorImpl implements ProgMonitorJournalI {

  private JDialog dialog = null;
  private JTextArea textArea = null;
  private JButton closeButton = null;

  /**
   * No argument constructor for the Factory instantiation.
   */
  public JournalProgMonitorImpl() {
    dialog = new JDialog(MainFrame.getSingleInstance());
    dialog.setSize(600, 300);
    textArea = new JTextArea();
    textArea.setEditable(false);
    closeButton = new JButton("Close");
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        textArea.setText("");
        dialog.setVisible(false);
      }
    });
    Container cont = dialog.getContentPane();
    cont.setLayout(new GridBagLayout());
    cont.add(new JScrollPane(textArea), new GridBagConstraints(0, 0, 1, 1, 10, 10,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    cont.add(closeButton, new GridBagConstraints(0, 1, 1, 1, 0, 0,
        GridBagConstraints.CENTER, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
  }

  public void init(String title) {
    dialog.setTitle(title);
  }

  public void addProgress(String text) {
    if (dialog.isVisible()) {
      textArea.append(text);
      if (text.endsWith("\n")) {
        textArea.setCaretPosition(textArea.getText().length());
      }
    }
  }

  public void setEnabledClose(boolean b) {
    closeButton.setEnabled(b);
  }

  public void setVisible(boolean b) {
    if (!b) {
      dialog.setVisible(false);
      textArea.setText(null);
    } else if (!dialog.isVisible()) {
      MiscGui.setSuggestedWindowLocation(GeneralDialog.getDefaultParent(), dialog);
      textArea.setText(null);
      dialog.setVisible(true);
    }
  }

  public boolean isVisible() {
    return dialog.isVisible();
  }

  public void dispose() {
    dialog.dispose();
  }

}
/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_gui.monitor;

import com.CH_co.monitor.*;
import com.CH_gui.gui.MyInsets;
import com.CH_gui.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class JournalProgMonitorImpl implements ProgMonitorJournalI {

  private JDialog exportProgress = null;
  private JTextArea exportArea = null;
  private JButton closeButton = null;

  /**
   * No argument constructor for the Factory instantiation.
   */
  public JournalProgMonitorImpl() {
    exportProgress = new JDialog();
    exportProgress.setSize(600, 300);
    exportArea = new JTextArea();
    exportArea.setEditable(false);
    closeButton = new JButton("Close");
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        exportArea.setText("");
        exportProgress.setVisible(false);
      }
    });
    Container cont = exportProgress.getContentPane();
    cont.setLayout(new GridBagLayout());
    cont.add(new JScrollPane(exportArea), new GridBagConstraints(0, 0, 1, 1, 10, 10,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    cont.add(closeButton, new GridBagConstraints(0, 1, 1, 1, 0, 0,
        GridBagConstraints.CENTER, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
  }

  public void init(String title) {
    exportProgress.setTitle(title);
  }

  public void addProgress(String text) {
    if (exportProgress.isVisible()) {
      exportArea.append(text);
      if (text.endsWith("\n")) {
        exportArea.setCaretPosition(exportArea.getText().length());
      }
    }
  }

  public void setEnabledClose(boolean b) {
    closeButton.setEnabled(b);
  }

  public void setVisible(boolean b) {
    if (!b) {
      exportProgress.setVisible(false);
      exportArea.setText(null);
    } else if (!exportProgress.isVisible()) {
      MiscGui.setSuggestedWindowLocation(GeneralDialog.getDefaultParent(), exportProgress);
      exportArea.setText(null);
      exportProgress.setVisible(true);
    }
  }

  public void dispose() {
    exportProgress.dispose();
  }

}
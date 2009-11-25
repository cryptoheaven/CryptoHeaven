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

package com.CH_co.service.records;

import java.awt.*;
import java.awt.event.*;

import javax.swing.border.*;
import javax.swing.*;
import java.io.*;

import com.CH_co.gui.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

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
 * <b>$Revision: 1.14 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class ConfirmFileReplaceDialog extends GeneralDialog {

  private static final int DEFAULT_YES_INDEX = 1;
  private static final int DEFAULT_NO_INDEX = 2;

  private boolean isReplace;
  private boolean isRename;
  private File originalFile;
  private File renamedFile;

  private JButton jRenameButton;
  private JButton jYesButton;
  private JCheckBox jRenameCheck;
  private JTextField jRenameText;

  /** Creates new ConfirmFileReplaceDialog */
  public ConfirmFileReplaceDialog(File originalFile, Long newSize, FileDataRecord newFile) {
    super("Confirm File Replace");
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ConfirmFileReplaceDialog.class, "ConfirmFileReplaceDialog(File originalFile, Long new Size, FileDataRecord newFile)");
    if (trace != null) trace.args(originalFile, newSize, newFile);

    this.originalFile = originalFile;

    JButton[] buttons = createButtons();
    JComponent mainComponent = createMainComponent(originalFile, newSize, newFile);

    jRenameButton = buttons[0];
    jRenameButton.setEnabled(false);
    jRenameText.setEnabled(false);
    jYesButton = buttons[1];

    setModal(true);
    MessageDialog.playSound(MessageDialog.WARNING_MESSAGE);
    init(null, buttons, mainComponent, DEFAULT_NO_INDEX, DEFAULT_NO_INDEX);

    if (trace != null) trace.exit(ConfirmFileReplaceDialog.class);
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[3];

    buttons[0] = new JMyButton("Rename");
    buttons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedRename();
      }
    });

    buttons[1] = new JMyButton("Yes");
    buttons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedReplace();
      }
    });

    buttons[2] = new JMyButton("No");
    buttons[2].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedCancel();
      }
    });

    return buttons;
  }


  private JComponent createMainComponent(File originalFile, Long newSize, FileDataRecord newFile) {
    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));

    panel.setLayout(new GridBagLayout());

    int posY = 0;
    panel.add(new JMyLabel(Images.get(ImageNums.FILE_REPLACE32)), new GridBagConstraints(0, posY, 1, 2, 0, 10, 
          GridBagConstraints.NORTH, GridBagConstraints.NORTH, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(new JMyLabel("This folder already contains a file named '" + originalFile.getName() + "'."), new GridBagConstraints(1, posY, 2, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel("Folder name is '" + originalFile.getParent() + "'."), new GridBagConstraints(1, posY, 2, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 15, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel("Would you like to OVERWRITE the existing file:"), new GridBagConstraints(1, posY, 2, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(Misc.getFormattedSize(originalFile.length(), 21, 22), Images.get(ImageNums.FILE32), JLabel.LEFT), new GridBagConstraints(1, posY, 2, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel("with this one?"), new GridBagConstraints(1, posY, 2, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(Misc.getFormattedSize(newSize.longValue(), 21, 22), Images.get(ImageNums.FILE32), JLabel.LEFT), new GridBagConstraints(1, posY, 2, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    jRenameCheck = new JMyCheckBox("Save using an alternate name:");
    jRenameCheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean selected = jRenameCheck.isSelected();
        jRenameButton.setDefaultCapable(true);
        getRootPane().setDefaultButton(jRenameButton);
        jRenameButton.setEnabled(selected);
        jRenameText.setEnabled(selected);
        jYesButton.setEnabled(!selected);
        if (selected) {
          jRenameText.grabFocus();
          jRenameText.selectAll();
        }
      }
    });
    jRenameText = new JTextField(15);
    jRenameText.setText(originalFile.getName());
    panel.add(jRenameCheck, new GridBagConstraints(1, posY, 1, 1, 0, 0, 
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 1), 0, 0));
    panel.add(jRenameText, new GridBagConstraints(2, posY, 1, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 1, 5, 5), 0, 0));
    posY ++;


    // filler
    panel.add(new JMyLabel(), new GridBagConstraints(1, posY, 1, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));

    return panel;
  }

  private void pressedRename() {
    String renameTo = jRenameText.getText().trim();
    renamedFile = new File(originalFile.getParent(), renameTo);
    if (!renamedFile.exists()) {
      isRename = true;
      closeDialog();
    } else {
      isRename = false;
      if (renameTo.length() > 0)
        MessageDialog.showErrorDialog(ConfirmFileReplaceDialog.this, "Duplicate file '" + renamedFile.getName() + "'.  File already exists.", "File already exists", true);
      else
        MessageDialog.showErrorDialog(ConfirmFileReplaceDialog.this, "No alternate file name specified.  Please specify an alternate file name.", "Please specify an alternate file name.", true);
    }
  }

  private void pressedReplace() {
    if (originalFile.canWrite()) {
      isReplace = true;
      closeDialog();
    } else {
      isReplace = false;
      boolean canRead = originalFile.canRead();
      if (!canRead)
        MessageDialog.showErrorDialog(ConfirmFileReplaceDialog.this, "Cannot overwrite file.  Write access denied.", "Cannot Overwrite File", true);
      else
        MessageDialog.showErrorDialog(ConfirmFileReplaceDialog.this, "Cannot overwrite file.  File access is set to Read Only.", "Cannot Overwrite File", true);
    }
  }

  private void pressedCancel() {
    isReplace = false;
    closeDialog();
  }

  public boolean isReplace() {
    return isReplace;
  }

  public boolean isRename() {
    return isRename;
  }

  public File getRenamdFile() {
    return renamedFile;
  }

}
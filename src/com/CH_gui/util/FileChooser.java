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

package com.CH_gui.util;

import com.CH_gui.gui.JMyButton;
import javax.swing.*;
import java.awt.*;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import com.CH_co.gui.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 *  FileChooser on the local file system.
 *  
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.9 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FileChooser extends JFileChooser {

  private File newSelectedFiles[] = null;
  private boolean isDownloadChooser;

  private static final Object monitor = new Object();

  /** 
   * Plays a role of public constructor.
   */
  public static FileChooser makeNew(Component owner, boolean isDownload, File currentDir) {
    return makeNew(owner, isDownload, currentDir, null, null, null, null);
  }
  public static FileChooser makeNew(Component owner, boolean isDownload, File currentDir, String title, String approve, Character approveMnemonic, String approveTip) {
    FileChooser fileChooser = null;
    synchronized (monitor) {
      if (isDownload)
        UIManager.put("FileChooser.fileNameLabelText", "Destination Directory:");
      else 
        UIManager.put("FileChooser.fileNameLabelText", "Source File or Directory:");
      fileChooser = new FileChooser(owner, isDownload, currentDir, title, approve, approveMnemonic, approveTip);
    }
    return fileChooser;
  }

  /** Creates new FileChooser */
  private FileChooser(Component owner, boolean isDownload, File currentDir, String title, String approve, Character approveMnemonic, String approveTip) {
    super(currentDir);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileChooser.class, "FileChooser(Component owner, boolean isDownload, File currentDir, String title, String approve, Character approveMnemonic, String approveTip)");
    if (trace != null) trace.args(owner);
    if (trace != null) trace.args(isDownload);
    if (trace != null) trace.args(currentDir, title, approve, approveMnemonic, approveTip);

    if (isDownload) {
      isDownloadChooser = true;
      if (trace != null) trace.data(10, isDownloadChooser);

      setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      setMultiSelectionEnabled(false);
      setDialogTitle(title != null ? title : "Download - select destination directory");
      setApproveButtonText(approve != null ? approve : "Download");
      setApproveButtonMnemonic(approveMnemonic != null ? approveMnemonic.charValue() : 'D');
      setApproveButtonToolTipText(approveTip != null ? approveTip : "Approve the current directory selection.");
      setControlButtonsAreShown(false);
    }
    else {
      // upload
      isDownloadChooser = false;
      if (trace != null) trace.data(50, isDownloadChooser);

      setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      setMultiSelectionEnabled(true);
      setDialogTitle(title != null ? title : "Upload - select files/directories");
      setApproveButtonText(approve != null ? approve : "Upload");
      setApproveButtonMnemonic(approveMnemonic != null ? approveMnemonic.charValue() : 'U');
      setApproveButtonToolTipText(approveTip != null ? approveTip : "Approve the current file and directory selection.");
      setControlButtonsAreShown(false);
    }


    ChooserDialog chooserDialog = null;
    Window w = SwingUtilities.windowForComponent(owner);
    if (w == null)
      w = GeneralDialog.getDefaultParent();
    if (w instanceof Dialog)
      chooserDialog = new ChooserDialog((Dialog) w, this);
    else if (w instanceof Frame)
      chooserDialog = new ChooserDialog((Frame) w, this);

    if (trace != null) trace.data(20, chooserDialog.approve);
    if (chooserDialog.approve)
      selectionApproved();


    if (trace != null) trace.data(100, newSelectedFiles);
    if (trace != null) trace.exit(FileChooser.class);
  }

  /* Return chosen file or directory */
  public File[] getNewSelectedFiles() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileChooser.class, "getNewSelectedFiles()");
    if (trace != null) trace.exit(FileChooser.class, newSelectedFiles);
    return newSelectedFiles;
  }

  public File getSelectedDir() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileChooser.class, "getSelectedDir()");
    if (!isDownloadChooser)
      throw new IllegalStateException("While operating in UPLOAD mode, this call is invalid!");
    if (newSelectedFiles != null && newSelectedFiles.length > 0) {
      if (trace != null) trace.exit(FileChooser.class, newSelectedFiles[0]);
      return newSelectedFiles[0];
    }
    if (trace != null) trace.exit(FileChooser.class, null);
    return null;
  }

  private void selectionApproved() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileChooser.class, "selectionApproved()");
    if (trace != null) trace.data(10, "selection approved");
    newSelectedFiles = getSelectedFiles();
    if (trace != null) trace.data(15, "newSelectedFiles", newSelectedFiles);
    if (newSelectedFiles == null || newSelectedFiles.length == 0) {
      newSelectedFiles = new File[1];
      newSelectedFiles[0] = getSelectedFile();
      if (trace != null) trace.data(16, newSelectedFiles);
      if (trace != null) trace.data(16, "newSelectedFiles", newSelectedFiles);
    }

    if (isDownloadChooser) {
      if (trace != null) trace.data(20, isDownloadChooser);
      if (trace != null) trace.data(21, newSelectedFiles);
      if (newSelectedFiles == null || newSelectedFiles.length == 0 || 
            (newSelectedFiles != null && newSelectedFiles.length > 0 && newSelectedFiles[0] == null)
          ) {
        newSelectedFiles = new File[] { FileChooser.this.getCurrentDirectory() };
        if (trace != null) trace.data(22, newSelectedFiles);
      }
    }
    if (trace != null) trace.exit(FileChooser.class);
  }

  private class ChooserDialog extends GeneralDialog {
    private static final int DEFAULT_BUTTON_INDEX = 0;
    private static final int DEFAULT_CANCEL_BUTTON_INDEX = 1;

    private JFileChooser fileChooser;
    private boolean approve;

    /** Creates new AboutDialog */
    public ChooserDialog(Frame owner, JFileChooser fileChooser) {
      super(owner, fileChooser.getDialogTitle());
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ChooserDialog.class, "ChooserDialog(Frame owner, JFileChooser fileChooser)");
      initialize(owner, fileChooser);
      if (trace != null) trace.exit(ChooserDialog.class);
    }
    /** Creates new AboutDialog */
    public ChooserDialog(Dialog owner, JFileChooser fileChooser) {
      super(owner, fileChooser.getDialogTitle());
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ChooserDialog.class, "ChooserDialog(Dialog owner, JFileChooser fileChooser)");
      initialize(owner, fileChooser);
      if (trace != null) trace.exit(ChooserDialog.class);
    }

    private void initialize(Component owner, JFileChooser fileChooser) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ChooserDialog.class, "initialize(Component owner, JFileChooser fileChooser)");
      this.fileChooser = fileChooser;
      JButton[] buttons = createButtons();
      setModal(true);
      super.init(owner, buttons, fileChooser, DEFAULT_BUTTON_INDEX, DEFAULT_CANCEL_BUTTON_INDEX);
      if (trace != null) trace.exit(ChooserDialog.class);
    }

    private JButton[] createButtons() {
      JButton[] buttons = new JButton[2];

      buttons[0] = new JMyButton(fileChooser.getApproveButtonText());
      buttons[0].setDefaultCapable(true);
      buttons[0].setMnemonic(fileChooser.getApproveButtonMnemonic());
      buttons[0].setToolTipText(fileChooser.getApproveButtonToolTipText());
      buttons[0].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "actionPerformed(ActionEvent e)");
          if (trace != null) trace.args(e);
          boolean choiceValid = true;
          if (isDownloadChooser) {
            File file = fileChooser.getSelectedFile();
            if (file != null && !file.isDirectory()) {
              choiceValid = false;
              MessageDialog.showWarningDialog(fileChooser, "Please select a download directory. \n\nSelected file " + file.getAbsolutePath() + " is not a directory. \n\nSelection canceled.", "Invalid Choice", true);
            }
          }
          approve = choiceValid;
          closeDialog();
          if (trace != null) trace.exit(getClass());
        }
      });

      buttons[1] = new JMyButton("Cancel");
      buttons[1].setMnemonic('C');
      buttons[1].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "actionPerformed(ActionEvent e)");
          if (trace != null) trace.args(e);
          approve = false;
          closeDialog();
          if (trace != null) trace.exit(getClass());
        }
      });

      return buttons;
    }

  }
}
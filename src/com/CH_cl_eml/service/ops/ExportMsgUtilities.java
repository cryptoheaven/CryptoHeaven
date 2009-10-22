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

package com.CH_cl_eml.service.ops;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_co.gui.MyInsets;
import com.CH_co.service.ops.DataAcquisitionHelperI;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.GeneralDialog;
import com.CH_co.util.MessageDialog;
import com.CH_co.util.Misc;
import com.CH_co.util.MiscGui;
import com.CH_co_eml.service.ops.EmailSendingAttOps;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;

import javax.mail.internet.MimeMessage;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.tree.DefaultMutableTreeNode;

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
 * <b>$Revision: 1.0 $</b>
 * @author  Marcin
 * @version
 */
public class ExportMsgUtilities {
  
  private static JDialog exportProgress = null;
  private static JTextArea exportArea = null;
  private static JButton closeButton = null;
  
  /** 
   * Export messages with attachments to the destination.
   * @param msgs Remote messages to download.
   * @param fromMsgs is the message parent to specified files or is NULL if downloading from a folder.
   * @param destDir is the Local destination directory to which to download the files
   **/
  public static void runDownloadMsgs(MsgLinkRecord[] msgs, MsgLinkRecord[] fromMsgs, File destDir, ServerInterfaceLayer SIL, boolean waitForComplete, boolean openAfterDownload) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ExportMsgUtilities.class, "runDownloadFile(MsgLinkRecord[] msgs, MsgLinkRecord[] fromMsgs, File destDir, ServerInterfaceLayer SIL, boolean waitForComplete, boolean openAfterDownload)");
    if (trace != null) trace.args(msgs, fromMsgs, destDir, SIL);
    if (trace != null) trace.args(waitForComplete);
    if (trace != null) trace.args(openAfterDownload);
    
    if (exportProgress == null) {
      exportProgress = new JDialog(GeneralDialog.getDefaultParent(), "Message Export", false);
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
    if (!exportProgress.isVisible()) {
      MiscGui.setSuggestedWindowLocation(GeneralDialog.getDefaultParent(), exportProgress);
      exportProgress.setVisible(true);
    }
    closeButton.setEnabled(false);
    addProgress("Downloading to " + destDir.getAbsolutePath() + "\n", true);
    if (destDir != null && msgs != null && msgs.length > 0) {
      try {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        for (int i=0; i<msgs.length; i++) {
          MsgLinkRecord msgLink = msgs[i];
          MsgDataRecord msgData = cache.getMsgDataRecord(msgLink.msgId);
          String subject = EmailSendingAttOps.getSubjectForEmail(msgData);
          String filename = Misc.getFileSafeShortString(subject + " " + msgData.msgId + ".eml");
          addProgress("   " + filename, false);
          try {
            // don't recreate already exported messages
            if (new File(destDir, filename).exists() == false) {
              addProgress(" exporting...", false);
              DataAcquisitionHelperI dataHelper  = new DataAcquisitionHelperClient(SIL);
              DefaultMutableTreeNode msgRoot = EmailSendingAttOps.getMessageWithAttachments(dataHelper, cache.getMyUserId(), msgLink.msgLinkId);
              EmailSendingAttOps.fetchAllFileAttachments(dataHelper, msgRoot);
              EmailSendingAttOps.unsealMessageWithAttachments(msgRoot, msgLink.getSymmetricKey());

              String smtpHostProperty = null; // EngineGlobalProperties.PROP_EMAIL_SMTP_HOST;
              String smtpHostValue = null; //EngineGlobalProperties.getProperty(smtpHostProperty);
              EmailSendingAttOps.convertMessageTreeToMimeMessageFormat(dataHelper, msgRoot, smtpHostProperty, smtpHostValue, false, true); // the recipients are also set but from possibly truncated lists
              MimeMessage emailMessage = (MimeMessage) ((Object[]) msgRoot.getUserObject())[0];
              File outFile = new File(destDir, filename);
              outFile.createNewFile();
              FileOutputStream outStream = new FileOutputStream(outFile, false);
              emailMessage.writeTo(outStream);
              outStream.flush();
              outStream.close();
              addProgress(" done.", false);
            } else {
              addProgress(" already exists, skipped.", false);
            }
          } catch (Throwable t) {
            String msg = t.getMessage();
            if (msg == null) msg = "";
            addProgress(" skipped due to ERROR: " + Misc.getClassNameWithoutPackage(t.getClass()) + " " + msg, false);
          }
          addProgress("\n", true);
        }
      } catch (NoClassDefFoundError t) {
        // if mail libraries are not installed, we will end up here
        String msg = t.getMessage();
        if (msg == null) msg = "";
        addProgress(" skipped due to ERROR: " + Misc.getClassNameWithoutPackage(t.getClass()) + " " + msg, false);
        exportProgress.setVisible(false);
        MessageDialog.showErrorDialog(null, "<html><p>Mail export libraries could not be found.  To activate this functionality please download the latest version of the software from the following page: </p><p><a href=\"http://www.cryptoheaven.com/Download\">http://www.cryptoheaven.com/Download</a></p><p>Please close this software before installing the downloaded package.</p>", "Function not available.", false);
      }
    }
    closeButton.setEnabled(true);

    if (trace != null) trace.exit(ExportMsgUtilities.class);
  }
 
  private static void addProgress(String s, boolean scroll) {
    exportArea.append(s);
    if (scroll)
      exportArea.setCaretPosition(exportArea.getText().length());
  }
}
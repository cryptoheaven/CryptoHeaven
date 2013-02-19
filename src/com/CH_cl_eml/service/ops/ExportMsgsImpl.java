/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_cl_eml.service.ops;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.ExportMsgsI;
import com.CH_cl.service.ops.StatOps;

import com.CH_co.monitor.*;
import com.CH_co.service.ops.DataAcquisitionHelperI;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_co_eml.service.ops.EmailSendingAttOps;
import java.io.BufferedOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.mail.internet.MimeMessage;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
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
public class ExportMsgsImpl implements ExportMsgsI {

  private static ProgMonitorJournalI exportProgress = null;

  /**
   * Public no-args constructor for the factory
   */
  public ExportMsgsImpl() {
  }

  /**
   * Export messages with attachments to the destination.
   * @param msgs Remote messages to download.
   * @param fromMsgs is the message parent to specified files or is NULL if downloading from a folder.
   * @param destDir is the Local destination directory to which to download the files
   **/
  public void runDownloadMsgs(MsgLinkRecord[] msgs, MsgLinkRecord[] fromMsgs, File destDir, ServerInterfaceLayer SIL, boolean waitForComplete, boolean openAfterDownload) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ExportMsgsImpl.class, "runDownloadFile(MsgLinkRecord[] msgs, MsgLinkRecord[] fromMsgs, File destDir, ServerInterfaceLayer SIL, boolean waitForComplete, boolean openAfterDownload)");
    if (trace != null) trace.args(msgs, fromMsgs, destDir, SIL);
    if (trace != null) trace.args(waitForComplete);
    if (trace != null) trace.args(openAfterDownload);

    if (exportProgress == null) {
      exportProgress = ProgMonitorFactory.newInstanceJournal("Message Export");
    }
    exportProgress.setVisible(true);
    exportProgress.setEnabledClose(false);
    exportProgress.addProgress("Downloading to " + destDir.getAbsolutePath() + "\n");
    if (destDir != null && msgs != null && msgs.length > 0) {
      try {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        for (int i=0; i<msgs.length; i++) {
          MsgLinkRecord msgLink = msgs[i];
          MsgDataRecord msgData = cache.getMsgDataRecord(msgLink.msgId);
          String subject = EmailSendingAttOps.getSubjectForEmail(msgData);
          String filename = FileTypes.getFileSafeShortString(subject + " " + msgData.msgId + ".eml");
          exportProgress.addProgress("   " + filename);
          ArrayList returnTempFilesBufferL = new ArrayList();
          try {
            // don't recreate already exported messages
            if (new File(destDir, filename).exists() == false) {
              exportProgress.addProgress(" exporting...");
              DataAcquisitionHelperI dataHelper  = new DataAcquisitionHelperClient(SIL);
              DefaultMutableTreeNode msgRoot = EmailSendingAttOps.getMessageWithAttachments(dataHelper, cache.getMyUserId(), msgLink.msgLinkId);
              EmailSendingAttOps.fetchAllFileAttachments(dataHelper, msgRoot);
              EmailSendingAttOps.unsealMessageWithAttachments(msgRoot, msgLink.getSymmetricKey());

              String smtpHostProperty = null; // EngineGlobalProperties.PROP_EMAIL_SMTP_HOST;
              String smtpHostValue = null; //EngineGlobalProperties.getProperty(smtpHostProperty);
              EmailSendingAttOps.convertMessageTreeToMimeMessageFormat(dataHelper, msgRoot, smtpHostProperty, smtpHostValue, false, true, returnTempFilesBufferL); // the recipients are also set but from possibly truncated lists
              MimeMessage emailMessage = (MimeMessage) ((Object[]) msgRoot.getUserObject())[0];
              File outFile = new File(destDir, filename);
              outFile.createNewFile();
              OutputStream outStream = new BufferedOutputStream(new FileOutputStream(outFile, false), 32*1024);
              emailMessage.writeTo(outStream);
              outStream.flush();
              outStream.close();
              exportProgress.addProgress(" done.");
              StatOps.markOldIfNeeded(SIL, msgLink.msgLinkId, FetchedDataCache.STAT_TYPE_INDEX_MESSAGE);
            } else {
              exportProgress.addProgress(" already exists, skipped.");
            }
          } catch (Throwable t) {
            String msg = t.getMessage();
            if (msg == null) msg = "";
            exportProgress.addProgress(" skipped due to ERROR: " + Misc.getClassNameWithoutPackage(t.getClass()) + " " + msg);
          } finally {
            // cleanup any temp files created during export
            for (int z=0; z<returnTempFilesBufferL.size(); z++) {
              CleanupAgent.wipeOrDelete((File) returnTempFilesBufferL.get(z));
            }
          }
          exportProgress.addProgress("\n");
        }
      } catch (NoClassDefFoundError t) {
        // if mail libraries are not installed, we will end up here
        String msg = t.getMessage();
        if (msg == null) msg = "";
        exportProgress.addProgress(" skipped due to ERROR: " + Misc.getClassNameWithoutPackage(t.getClass()) + " " + msg);
        exportProgress.setVisible(false);
        String title = "Function not available.";
        String errMsg = "<html><p>Mail export libraries could not be found.  To activate this functionality please download the latest version of the software from the following page: </p><p><a href=\"http://www.cryptoheaven.com/Download\">http://www.cryptoheaven.com/Download</a></p><p>Please close this software before installing the downloaded package.</p>";
        NotificationCenter.show(NotificationCenter.ERROR_MESSAGE, title, errMsg);
      }
    }
    exportProgress.setEnabledClose(true);

    if (trace != null) trace.exit(ExportMsgsImpl.class);
  }

}
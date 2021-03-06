/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.service.ops;

import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.DownloadUtilities;
import com.CH_co.service.records.*;
import com.CH_co.trace.*;
import com.CH_gui.util.FileChooser;

import java.awt.Component;
import java.io.File;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class DownloadUtilsGui {

  /**
   * @param toDownload are the files/messages/directories to fetch
   * @param fromMsgs are the parent messages that own the file attachments specified, NULL if fetching from a folder.
   * @param owner component for the file chooser dialog
   */
  public static void downloadFilesChoice(Component owner, Record[] toDownload, MsgLinkRecord[] fromMsgs, ServerInterfaceLayer SIL) {
    downloadFilesChoice(owner, toDownload, fromMsgs, SIL, false);
  }
  public static void downloadFilesChoice(Component owner, Record[] toDownload, MsgLinkRecord[] fromMsgs, ServerInterfaceLayer SIL, boolean waitForComplete) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DownloadUtilsGui.class, "downloadFilesChoice(Component owner, Record[] toDownload, MsgLinkRecord[] fromMsgs, ServerInterfaceLayer SIL, boolean waitForComplete)");
    if (trace != null) trace.args(owner, toDownload, fromMsgs, SIL);
    if (trace != null) trace.args(waitForComplete);

    /* Let the user choose the file destination */
    FileChooser fileChooser = FileChooser.makeNew(owner, true, DownloadUtilities.getDefaultDestDir());
    File destDir = fileChooser.getSelectedDir();

    if (destDir != null) {
      if (trace != null) trace.data(10, destDir);
      DownloadUtilities.setDefaultDestDir(destDir);

      // now, download all selected files and directories and messages...
      DownloadUtilities.downloadFilesStartCoordinator(owner, toDownload, fromMsgs, destDir, SIL);
    }

    if (trace != null) trace.exit(DownloadUtilsGui.class);
  }

}
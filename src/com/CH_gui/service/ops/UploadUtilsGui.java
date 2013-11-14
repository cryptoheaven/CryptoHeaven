/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.service.ops;

import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.UploadUtilities;
import com.CH_co.service.records.FolderShareRecord;
import com.CH_co.trace.Trace;
import com.CH_gui.util.FileChooser;

import java.awt.Component;
import java.io.File;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class UploadUtilsGui {

  /**
    * Pops up a file choice dialog to choose which file(s) to upload, then launch UploadCoordinator.
    * @param shareId is the share to which to upload a file
    */
  public static void uploadFileChoice(Component owner, FolderShareRecord shareRecord, ServerInterfaceLayer SIL) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadUtilsGui.class, "uploadFileChoice(Component owner, FolderShareRecord shareRecord, ServerInterfaceLayer SIL)");
    if (trace != null) trace.args(owner, shareRecord, SIL);

    /* Let the user choose which file to upload */
    FileChooser fileChooser = FileChooser.makeNew(owner, false, UploadUtilities.getDefaultSourceDir());
    File[] newFiles = fileChooser.getNewSelectedFiles();

    if (newFiles != null && newFiles.length > 0) {
      if (trace != null) trace.data(10, newFiles);
      // source directory is the current directory from the file chooser, not the selected files
      UploadUtilities.setDefaultSourceDir(fileChooser.getCurrentDirectory());
      // now, upload all selected files and directories...
      UploadUtilities.uploadFilesStartCoordinator(owner, newFiles, shareRecord, SIL);
    }

    if (trace != null) trace.exit(UploadUtilsGui.class);
  }

}
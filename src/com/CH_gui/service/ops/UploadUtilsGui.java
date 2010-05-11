/*
 * Copyright 2001-2010 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_gui.service.ops;

import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.FileChooser;
import com.CH_cl.service.ops.UploadUtilities;
import com.CH_co.service.records.FolderShareRecord;
import com.CH_co.trace.Trace;

import java.awt.Component;
import java.io.File;

/**
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class UploadUtilsGui {

  /**
    * Pops up a file choice dialog to choose which file(s) to upload, then launch UploadCoordinator.
    * @param shareId is the share to which to upload a file
    */
  public static void uploadFileChoice(FolderShareRecord shareRecord, Component owner, ServerInterfaceLayer SIL) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadUtilsGui.class, "uploadFileChoice(FolderShareRecord shareRecord, Component owner, ServerInterfaceLayer SIL)");
    if (trace != null) trace.args(shareRecord, owner, SIL);

    /* Let the user choose which file to upload */
    FileChooser fileChooser = FileChooser.makeNew(owner, false, UploadUtilities.getDefaultSourceDir());
    File[] newFiles = fileChooser.getNewSelectedFiles();

    if (newFiles != null && newFiles.length > 0) {
      if (trace != null) trace.data(10, newFiles);
      // source directory is the current directory from the file chooser, not the selected files
      UploadUtilities.setDefaultSourceDir(fileChooser.getCurrentDirectory());
      // now, upload all selected files and directories...
      UploadUtilities.uploadFilesStartCoordinator(newFiles, shareRecord, SIL);
    }

    if (trace != null) trace.exit(UploadUtilsGui.class);
  }

}
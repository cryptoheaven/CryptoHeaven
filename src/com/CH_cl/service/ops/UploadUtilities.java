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

package com.CH_cl.service.ops;

import com.CH_cl.monitor.TransferProgMonitor;
import java.util.Vector;
import java.io.File;
import java.awt.Component;
import javax.swing.*;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.actions.fld.*;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.ops.*;

import com.CH_co.service.records.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.file.*;
import com.CH_co.service.msg.dataSets.fld.*;

import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_co.monitor.*;

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
 * <b>$Revision: 1.17 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class UploadUtilities extends Object { // implicit no-argument constructor

  private static int uploadRunnerCount = 0;
  private static int uploadCoordinatorCount = 0;

  public static Class lastReplyClass = null;

  public static final String PROPERTY_NAME__LOCAL_FILE_SOURCE_DIR = UploadUtilities.class.getName() + "_localFileSourceDir";


  /** 
   * @return The default upload directory as specified in the properties, or current directory if default is invalid.
   */
  public static File getDefaultSourceDir() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadUtilities.class, "getDefaultSourceDir()");
    File dir = DownloadUtilities.getDefaultDir(PROPERTY_NAME__LOCAL_FILE_SOURCE_DIR);
    if (trace != null) trace.exit(UploadUtilities.class, dir);
    return dir;
  }

  /**
   * Sets the new default upload directory in the properties.
   */
  public static void setDefaultSourceDir(File dir) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadUtilities.class, "setDefaultSourceDir(File dir)");
    DownloadUtilities.setDefaultDir(PROPERTY_NAME__LOCAL_FILE_SOURCE_DIR, dir);
    if (trace != null) trace.exit(UploadUtilities.class);
  }


  /** 
    * This method is called every time the user requests uploading of a file.
    * Pops up a file choice dialog to choose which file(s) to upload, then launch UploadCoordinator.
    * @param shareId is the share to which to upload a file
    * @param owner is the component by which the filechooser will be displayed
    */
  public static void uploadFileChoice(FolderShareRecord shareRecord, Component owner, ServerInterfaceLayer SIL) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadUtilities.class, "uploadFile(Long shareId, Component, ServerInterfaceLayer SIL)");
    if (trace != null) trace.args(shareRecord, owner, SIL);

    // Choose user share instead of group if user has one...
    FetchedDataCache cache = SIL.getFetchedDataCache();
    FolderShareRecord myShare = cache.getFolderShareRecordMy(shareRecord.folderId, false);
    if (myShare != null) {
      shareRecord = myShare;
    }

    /* Let the user choose which file to upload */
    FileChooser fileChooser = FileChooser.makeNew(owner, false, getDefaultSourceDir());
    File[] newFiles = fileChooser.getNewSelectedFiles();

    if (newFiles != null && newFiles.length > 0) {
      if (trace != null) trace.data(10, newFiles);
      // source directory is the current directory from the file chooser, not the selected files
      setDefaultSourceDir(fileChooser.getCurrentDirectory());

      // now, upload all selected files and directories...
      uploadFilesStartCoordinator(newFiles, shareRecord, SIL);
    }

    if (trace != null) trace.exit(UploadUtilities.class);
  }

  public static void uploadFilesStartCoordinator(File[] newFiles, FolderShareRecord destinationShareRecord, ServerInterfaceLayer SIL) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadUtilities.class, "uploadFilesStartCoordinator(File[] newFiles, FolderShareRecord destinationShareRecord, ServerInterfaceLayer SIL)");
    if (trace != null) trace.args(newFiles, destinationShareRecord, SIL);
    new UploadCoordinator(newFiles, destinationShareRecord, SIL, true).start();
    if (trace != null) trace.exit(UploadUtilities.class);
  }


  /******************************************************
  *  Private class   U p l o a d C o o r d i n a t o r  *
  ******************************************************/
  /**
   * Coordinates upload of files and directory trees.
   * Searches recursively through upload file tree, and creates required directories
   * on the remote system and launches a file upload thread to carry on uploads into
   * those directories.
   * @param isThreadedRun if true means that this coordinator thread is allowed to start other helping threads, false for single thread execution.
   */
  public static class UploadCoordinator extends Thread {

    private File[] files;
    private FolderShareRecord shareRecord;
    private ServerInterfaceLayer SIL;
    private boolean isThreadedRun;

    public UploadCoordinator(File[] files, FolderShareRecord shareRecord, ServerInterfaceLayer SIL, boolean isThreadedRun) {
      super("UploadCoordinator # " + uploadCoordinatorCount);
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadCoordinator.class, "UploadCoordinator(File[] files, FolderShareRecord shareRecord, ServerInterfaceLayer SIL, boolean isThreadedRun)");
      if (trace != null) trace.args(files, shareRecord, SIL);
      if (trace != null) trace.args(isThreadedRun);

      this.files = files;
      this.shareRecord = shareRecord;
      this.SIL = SIL;
      this.isThreadedRun = isThreadedRun;
      uploadCoordinatorCount ++;
      uploadCoordinatorCount %= Integer.MAX_VALUE-1;

      // change the priority of this thread to minimum
      setPriority(MIN_PRIORITY);

      if (trace != null) trace.exit(UploadCoordinator.class);
    }

    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadCoordinator.class, "run()");

      uploadFiles(files, shareRecord, SIL, isThreadedRun);

      if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
      if (trace != null) trace.exit(UploadCoordinator.class);
      if (trace != null) trace.clear();
    }

    private void uploadFiles(File[] files, FolderShareRecord shareRecord, ServerInterfaceLayer SIL, boolean isThreadedRun) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadCoordinator.class, "uploadFiles(File[] files, FolderShareRecord shareRecord, ServerInterfaceLayer SIL, boolean isThreadedRun)");
      if (trace != null) trace.args(files, shareRecord, SIL);
      if (trace != null) trace.args(isThreadedRun);

      FetchedDataCache cache = null;

      Vector dirsV = new Vector();
      Vector filesV = new Vector();
      // distinguish between files and directories
      for (int i=0; i<files.length; i++) {
        File file = files[i];
        if (file.isDirectory())
          dirsV.addElement(file);
        else if (file.isFile())
          filesV.addElement(file);
        else
          throw new IllegalArgumentException("File not found: " + file);
      }
      // upload files
      if (filesV.size() > 0) {
        File[] f = new File[filesV.size()];
        filesV.toArray(f);

        runUploadFile(f, shareRecord, SIL, isThreadedRun);
      }
      // process directories
      if (dirsV.size() > 0) {
        File[] f = new File[dirsV.size()];
        dirsV.toArray(f);

        for (int i=0; i<f.length; i++) {

          if (cache == null)
            cache = SIL.getFetchedDataCache();

          FolderPair parentPair = null;
          if (shareRecord != null)
            parentPair = new FolderPair(shareRecord, cache.getFolderRecord(shareRecord.folderId));

          String folderName = f[i].getName();
          String folderDesc = "";

          BASymmetricKey baSymmetricKey = new BASymmetricKey(32);
          boolean useInheritedSharing = true;
          Fld_NewFld_Rq newFolderRequest = FolderOps.createNewFldRq(null, parentPair, FolderRecord.FILE_FOLDER,
                                              folderName, folderDesc, folderName, folderDesc, null, null, baSymmetricKey, useInheritedSharing, null, SIL);

          if (trace != null) trace.data(100, "newFolderRequest", newFolderRequest);

          MessageAction msgAction = new MessageAction(CommandCodes.FLD_Q_NEW_FOLDER, newFolderRequest);
          ClientMessageAction replyAction = SIL.submitAndFetchReply(msgAction);
          Long newShareId = null;
          // get the folderId of directory just created
          if (replyAction instanceof FldAGetFolders) {
            FldAGetFolders folderAction = (FldAGetFolders) replyAction;
            Fld_Folders_Rp folderDataSet = (Fld_Folders_Rp) folderAction.getMsgDataSet();
            newShareId = folderDataSet.shareRecords[0].shareId;
          }

          // don't start a new thread here, just execute the action synchronously
          DefaultReplyRunner.nonThreadedRun(SIL, replyAction);

          // find node of the new directory
          if (newShareId != null) {
            File[] fList = f[i].listFiles();
            if (fList != null && fList.length > 0) 
              uploadFiles(fList, cache.getFolderShareRecord(newShareId), SIL, isThreadedRun);
          }
        } // end for all directories
      }
      if (trace != null) trace.exit(UploadCoordinator.class);
    }
  } // end private class UploadCoordinator



  /** 
   * Sets a request to upload a file to the shareRecord, batches up the files in groups of 10.
   * @param file Local file to upload.
   * @param shareFolder is a folder to which to upload a file
   **/
  private static void runUploadFile(File[] files, FolderShareRecord shareRecord, ServerInterfaceLayer SIL, boolean isThreadedRun) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadUtilities.class, "runUploadFile(File[] files, FolderShareRecord shareRecord, ServerInterfaceLayer SIL, boolean isThreadedRun)");
    if (trace != null) trace.args(files, shareRecord, SIL);
    if (trace != null) trace.args(isThreadedRun);

    if (files != null && files.length > 0) {
      // divide File[] into chunks of 10;
      int count = 0;
      while (count < files.length) {
        int bunch = Math.min(10, files.length-count);
        File[] fileBunch = new File[bunch];
        for (int i=0; i<bunch; i++) {
          fileBunch[i] = files[count+i];
        }
        runUploadFileBunch(fileBunch, shareRecord, SIL, isThreadedRun);
        count += bunch;
      }
    }

    if (trace != null) trace.exit(UploadUtilities.class);
  }

  /** 
   * Sets a request to upload a file to the shareRecord.
   * @param file Local file to upload.
   * @param shareFolder is a folder to which to upload a file
   **/
  private static Class runUploadFileBunch(File[] files, FolderShareRecord shareRecord, ServerInterfaceLayer SIL, boolean isThreadedRun) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadUtilities.class, "runUploadFileBunch(File[] files, FolderShareRecord shareRecord, ServerInterfaceLayer SIL, boolean isThreadedRun)");
    if (trace != null) trace.args(files, shareRecord);
    if (trace != null) trace.args(isThreadedRun);

    // for non threaded run, reply with returned Message Action class type
    Class replyClass = null;

    File_NewFiles_Rq request = new File_NewFiles_Rq();
    MessageAction msgAction = new MessageAction(CommandCodes.FILE_Q_NEW_FILES, request);
    replyClass = runUploadFileBunch(files, msgAction, request, shareRecord.getSymmetricKey(), 
                                    shareRecord.shareId, new Short(Record.RECORD_TYPE_SHARE), isThreadedRun, SIL);

    lastReplyClass = replyClass;
    if (trace != null) trace.exit(UploadUtilities.class, replyClass);
    return replyClass;
  }

  /**
   * Universal function for uploads of files and message with file attachments.
   * @return Class of the reply if non threaded run
   */
  public static Class runUploadFileBunch(File[] files, MessageAction msgActionToSend, File_NewFiles_Rq request, 
                                      BASymmetricKey parentSymmetricKey, Long ownerObjId, Short ownerObjType, boolean isThreadedRun, ServerInterfaceLayer SIL) 
  {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadUtilities.class, "runUploadFileBunch(File[] files, MessageAction msgActionToSend, File_NewFiles_Rq request, BASymmetricKey parentSymmetricKey, Long ownerObjId, Short ownerObjType, boolean isThreadedRun, ServerInterfaceLayer SIL)");
    if (trace != null) trace.args(files, msgActionToSend, request, parentSymmetricKey, ownerObjId, ownerObjType);
    if (trace != null) trace.args(isThreadedRun);
    if (trace != null) trace.args(SIL);

    // for non threaded run, reply with returned Message Action class type
    Class replyClass = null;

    request.fileLinks = new FileLinkRecord [files.length];
    request.fileDataRecords = new FileDataRecord[files.length];

    for (int i=0; i<files.length; i++) {
      File file = files[i];

      request.fileLinks[i] = new FileLinkRecord();
      request.fileDataRecords[i] = new FileDataRecord();

      request.fileLinks[i].ownerObjId = ownerObjId;
      request.fileLinks[i].ownerObjType = ownerObjType;

      String fileName = file.getName();
      request.fileLinks[i].setFileType((String) MiscGui.getFileIconAndType(fileName)[1]);
      request.fileLinks[i].setFileName(fileName);
      request.fileLinks[i].origSize = new Long (file.length());

      request.fileDataRecords[i].setPlainDataFile(file);
    }

    if (isThreadedRun)
      new UploadRunner(msgActionToSend, request, parentSymmetricKey, SIL).start();
    else
      replyClass = UploadRunner.nonThreadedRun(msgActionToSend, request, parentSymmetricKey, SIL);

    lastReplyClass = replyClass;
    if (trace != null) trace.exit(UploadUtilities.class, replyClass);
    return replyClass;
  }


  /********************************************
  *  Private class   U p l o a d R u n n e r  *
  ********************************************/
  /**
   * Runs seal(s) and upload(s) of files.
   */  
  private static class UploadRunner extends Thread {

    private MessageAction msgActionToSend;
    private File_NewFiles_Rq request;
    private BASymmetricKey parentSymmetricKey;
    private ServerInterfaceLayer SIL;


    /**
     * In the effort to make this function universal for uploading files and sending messages with
     * attachments, arguments include the message action and the request.
     */
    private UploadRunner(MessageAction msgActionToSend, File_NewFiles_Rq request, BASymmetricKey parentSymmetricKey, ServerInterfaceLayer SIL) {
      super("UploadRunner # " + uploadRunnerCount);
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadRunner.class, "UploadRunner(MessageAction msgActionToSend, File_NewFiles_Rq request, BASymmetricKey parentSymmetricKey, ServerInterfaceLayer SIL)");
      if (trace != null) trace.args(msgActionToSend, request, parentSymmetricKey, SIL);

      this.msgActionToSend = msgActionToSend;
      this.request = request;
      this.parentSymmetricKey = parentSymmetricKey;
      this.SIL = SIL;
      uploadRunnerCount ++;
      uploadRunnerCount %= Integer.MAX_VALUE-1;

      if (trace != null) trace.exit(UploadRunner.class);
    }

    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadRunner.class, "run()");

      nonThreadedRun(msgActionToSend, request, parentSymmetricKey, SIL);

      if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
      if (trace != null) trace.exit(UploadRunner.class);
      if (trace != null) trace.clear();
    }

    /**
     * @return the type of a response, usefull to detect error replies.
     */
    public static Class nonThreadedRun(MessageAction msgActionToSend, File_NewFiles_Rq request, BASymmetricKey parentSymmetricKey, ServerInterfaceLayer SIL) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadRunner.class, "nonThreadedRun()");

      Class replyClass = null;

      FetchedDataCache cache = SIL.getFetchedDataCache();
      // Try creating the upload worker concurrently with sealing to minimize connection delay.
      SIL.ensureAtLeastOneAdditionalWorker_SpawnThread();

      int maxHeavyWorkers = SIL.getMaxHeavyWorkerCount();
      int maxThreadsInSynchronizedBlock = maxHeavyWorkers;
      if (maxHeavyWorkers < 1)
        maxThreadsInSynchronizedBlock = 1;

      // limit number of entries
      UploadDownloadSynch.entry(maxThreadsInSynchronizedBlock);

      try {
        FileLinkRecord[] linkRecords = request.fileLinks;
        FileDataRecord[] dataRecords = request.fileDataRecords;

        // create a list of file names
        String[] fileNames = new String[linkRecords.length];
        for (int i=0; i<fileNames.length; i++) {
          fileNames[i] = linkRecords[i].getFileName();
        }

        if (!MiscGui.isAllGUIsuppressed()) {
          ProgMonitor progressMonitor = new TransferProgMonitor(fileNames);
          ProgMonitorPool.registerProgMonitor(progressMonitor, msgActionToSend.getStamp());
        }

        for (int i=0; i<linkRecords.length; i++) {
          FileLinkRecord linkRecord = linkRecords[i];
          FileDataRecord dataRecord = dataRecords[i];

          BASymmetricKey symmetricKey = new BASymmetricKey(32);
          linkRecord.setSymmetricKey(symmetricKey);

          // seal the link records with the share's or message's symmetric key
          linkRecord.seal(parentSymmetricKey);

          KeyRecord keyrec = cache.getKeyRecordMyCurrent();

          // seal the data with file's symmetric key
          dataRecord.seal(keyrec, symmetricKey, ProgMonitorPool.getProgMonitor(msgActionToSend.getStamp()));
        }

        ClientMessageAction replyMsgAction = SIL.submitAndFetchReply(msgActionToSend);
        replyClass = replyMsgAction.getClass();
        DefaultReplyRunner.nonThreadedRun(SIL, replyMsgAction);
      } catch (Throwable t) { 
        if (trace != null) trace.exception(UploadRunner.class, 100, t);
      }
      // catch everything so we can decrement the counter properly

      // account for every exit
      UploadDownloadSynch.exit();

      lastReplyClass = replyClass;
      if (trace != null) trace.exit(UploadRunner.class, replyClass);
      return replyClass;
    }

  } // end inner class UploadRunner


}
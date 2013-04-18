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

package com.CH_cl.service.ops;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.actions.fld.*;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.*;

import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.monitor.*;
import com.CH_co.queue.PriorityFifo;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.file.*;
import com.CH_co.service.msg.dataSets.fld.*;
import com.CH_co.service.msg.dataSets.msg.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import java.io.File;
import java.util.*;

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
 * <b>$Revision: 1.17 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class UploadUtilities extends Object { // implicit no-argument constructor

  private static int uploadRunnerCount = 0;
  private static int uploadCoordinatorCount = 0;

  public static Class lastReplyClass = null;

  public static final String PROPERTY_NAME__LOCAL_FILE_SOURCE_DIR = "UploadUtilities_localFileSourceDir";


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

  public static void uploadFilesStartCoordinator(File[] newFiles, FolderShareRecord destinationShareRecord, ServerInterfaceLayer SIL) {
    uploadFilesStartCoordinator(newFiles, null, destinationShareRecord, SIL);
  }
  private static void uploadFilesStartCoordinator(File[] newFiles, FileLinkRecord[] oldFiles, FolderShareRecord destinationShareRecord, ServerInterfaceLayer SIL) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadUtilities.class, "uploadFilesStartCoordinator(File[] newFiles, FileLinkRecord[] oldFiles, FolderShareRecord destinationShareRecord, ServerInterfaceLayer SIL)");
    if (trace != null) trace.args(newFiles, destinationShareRecord, SIL);

    // Choose user share instead of group if user has one...
    if (destinationShareRecord != null) {
      FetchedDataCache cache = SIL.getFetchedDataCache();
      FolderShareRecord myDestinationShare = cache.getFolderShareRecordMy(destinationShareRecord.folderId, false);
      if (myDestinationShare != null) {
        destinationShareRecord = myDestinationShare;
      }
    }

    new UploadCoordinator(newFiles, oldFiles, destinationShareRecord, SIL, true, false).start();
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
  public static class UploadCoordinator extends ThreadTraced {

    private File[] files;
    private FileLinkRecord[] oldFiles;
    private FolderShareRecord shareRecord;
    private ServerInterfaceLayer SIL;
    private boolean isThreadedRun;
    private boolean isSuppressStuds;

    public UploadCoordinator(File[] files, FolderShareRecord shareRecord, ServerInterfaceLayer SIL, boolean isThreadedRun, boolean isSuppressStuds) {
      this(files, null, shareRecord, SIL, isThreadedRun, isSuppressStuds);
    }
    private UploadCoordinator(File[] files, FileLinkRecord[] oldFiles, FolderShareRecord shareRecord, ServerInterfaceLayer SIL, boolean isThreadedRun, boolean isSuppressStuds) {
      super("Upload Coordinator # " + uploadCoordinatorCount);
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadCoordinator.class, "UploadCoordinator(File[] files, FileLinkRecord[] oldFiles, FolderShareRecord shareRecord, ServerInterfaceLayer SIL, boolean isThreadedRun, boolean isSuppressStuds)");
      if (trace != null) trace.args(files, oldFiles, shareRecord, SIL);
      if (trace != null) trace.args(isThreadedRun);
      if (trace != null) trace.args(isSuppressStuds);

      this.files = files;
      this.oldFiles = oldFiles;
      this.shareRecord = shareRecord;
      this.SIL = SIL;
      this.isThreadedRun = isThreadedRun;
      this.isSuppressStuds = isSuppressStuds;
      uploadCoordinatorCount ++;
      uploadCoordinatorCount %= Integer.MAX_VALUE-1;

      setDaemon(true);

      if (trace != null) trace.exit(UploadCoordinator.class);
    }

    public void runTraced() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadCoordinator.class, "UploadCoordinator.runTraced()");
      uploadFiles(files, oldFiles, shareRecord, SIL, isThreadedRun, isSuppressStuds);
      if (trace != null) trace.exit(UploadCoordinator.class);
    }

    private void uploadFiles(File[] files, FileLinkRecord[] oldFiles, FolderShareRecord shareRecord, ServerInterfaceLayer SIL, boolean isThreadedRun, boolean isSuppressStuds) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadCoordinator.class, "uploadFiles(File[] files, FileLinkRecord[] oldFiles, FolderShareRecord shareRecord, ServerInterfaceLayer SIL, boolean isThreadedRun, boolean isSuppressStuds)");
      if (trace != null) trace.args(files, oldFiles, shareRecord, SIL);
      if (trace != null) trace.args(isThreadedRun);
      if (trace != null) trace.args(isSuppressStuds);

      FetchedDataCache cache = null;

      ArrayList dirsL = new ArrayList();
      ArrayList filesL = new ArrayList();
      ArrayList oldFilesL = new ArrayList();
      // distinguish between files and directories
      for (int i=0; i<files.length; i++) {
        File file = files[i];
        if (file.isDirectory()) {
          dirsL.add(file);
        } else if (file.isFile()) {
          filesL.add(file);
          oldFilesL.add(oldFiles != null ? oldFiles[i] : null);
        } else {
          throw new IllegalArgumentException("File not found: " + file);
        }
      }
      // upload files
      if (filesL.size() > 0) {
        File[] f = new File[filesL.size()];
        filesL.toArray(f);
        FileLinkRecord[] n = new FileLinkRecord[oldFilesL.size()];
        oldFilesL.toArray(n);

        runUploadFile(f, n, shareRecord, SIL, isThreadedRun, isSuppressStuds);
      }
      // process directories
      if (dirsL.size() > 0) {
        File[] f = new File[dirsL.size()];
        dirsL.toArray(f);

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
          Fld_NewFld_Rq newFolderRequest = FolderOps.createNewFldRq(parentPair, FolderRecord.FILE_FOLDER,
                                              folderName, folderDesc, folderName, folderDesc, null, null, baSymmetricKey, useInheritedSharing, null, SIL);

          if (trace != null) trace.data(100, "newFolderRequest", newFolderRequest);

          MessageAction msgAction = new MessageAction(CommandCodes.FLD_Q_NEW_FOLDER, newFolderRequest);
          ClientMessageAction replyAction = SIL.submitAndFetchReply(msgAction, 60000);
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
              uploadFiles(fList, null, cache.getFolderShareRecord(newShareId), SIL, isThreadedRun, isSuppressStuds);
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
  private static void runUploadFile(File[] files, FileLinkRecord[] oldFiles, FolderShareRecord shareRecord, ServerInterfaceLayer SIL, boolean isThreadedRun, boolean isSuppressStuds) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadUtilities.class, "runUploadFile(File[] files, FileLinkRecord[] oldFiles, FolderShareRecord shareRecord, ServerInterfaceLayer SIL, boolean isThreadedRun, boolean isSuppressStuds)");
    if (trace != null) trace.args(files, oldFiles, shareRecord, SIL);
    if (trace != null) trace.args(isThreadedRun);

    if (files != null && files.length > 0) {
      // divide File[] into chunks of 100;
      int count = 0;
      while (count < files.length) {
        int bunch = Math.min(100, files.length-count);
        File[] fileBunch = new File[bunch];
        FileLinkRecord[] oldBunch = null;
        if (oldFiles != null)
          oldBunch = new FileLinkRecord[bunch];
        for (int i=0; i<bunch; i++) {
          fileBunch[i] = files[count+i];
          if (oldFiles != null)
            oldBunch[i] = oldFiles[count+i];
        }
        count += bunch;
        runUploadFileBunch(fileBunch, oldBunch, shareRecord, SIL, isThreadedRun, isSuppressStuds);
      }
    }

    if (trace != null) trace.exit(UploadUtilities.class);
  }

  /**
   * Sets a request to upload a file to the shareRecord.
   * @param file Local file to upload.
   * @param shareFolder is a folder to which to upload a file
   **/
  private static Class runUploadFileBunch(File[] files, FileLinkRecord[] oldFiles, FolderShareRecord shareRecord, ServerInterfaceLayer SIL, boolean isThreadedRun, boolean isSuppressStuds) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadUtilities.class, "runUploadFileBunch(File[] files, FileLinkRecord[] oldFiles, FolderShareRecord shareRecord, ServerInterfaceLayer SIL, boolean isThreadedRun, boolean isSuppressStuds)");
    if (trace != null) trace.args(files, oldFiles, shareRecord);
    if (trace != null) trace.args(isThreadedRun);
    if (trace != null) trace.args(isSuppressStuds);

    // for non threaded run, reply with returned Message Action class type
    Class replyClass = null;

    File_NewFiles_Rq request = new File_NewFiles_Rq();
    int actionCode = !isSuppressStuds && GlobalProperties.PROGRAM_BUILD_NUMBER >= 644 ? CommandCodes.FILE_Q_NEW_FILE_STUDS : CommandCodes.FILE_Q_NEW_FILES;
    //int actionCode = CommandCodes.FILE_Q_NEW_FILES;
    MessageAction msgAction = new MessageAction(actionCode, request);
    replyClass = runUploadFileBunch(files, oldFiles, msgAction, request, shareRecord.getSymmetricKey(),
                                    shareRecord.shareId, new Short(Record.RECORD_TYPE_SHARE), SIL, isThreadedRun, isSuppressStuds);

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
    return runUploadFileBunch(files, null, msgActionToSend, request, parentSymmetricKey, ownerObjId, ownerObjType, SIL, isThreadedRun, false);
  }
  public static Class runUploadFileBunch(File[] files, MessageAction msgActionToSend, File_NewFiles_Rq request,
                                      BASymmetricKey parentSymmetricKey, Long ownerObjId, Short ownerObjType, boolean isThreadedRun, boolean isSuppressStuds, ServerInterfaceLayer SIL) 
  {
    return runUploadFileBunch(files, null, msgActionToSend, request, parentSymmetricKey, ownerObjId, ownerObjType, SIL, isThreadedRun, isSuppressStuds);
  }
  private static Class runUploadFileBunch(File[] files, FileLinkRecord[] oldFiles, MessageAction msgActionToSend, File_NewFiles_Rq request,
                                      BASymmetricKey parentSymmetricKey, Long ownerObjId, Short ownerObjType, ServerInterfaceLayer SIL, boolean isThreadedRun, boolean isSuppressStuds)
  {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadUtilities.class, "runUploadFileBunch(File[] files, FileLinkRecord[] oldFiles, MessageAction msgActionToSend, File_NewFiles_Rq request, BASymmetricKey parentSymmetricKey, Long ownerObjId, Short ownerObjType, ServerInterfaceLayer SIL, boolean isThreadedRun, boolean isSuppressStuds)");
    if (trace != null) trace.args(files, oldFiles, msgActionToSend, request, parentSymmetricKey, ownerObjId, ownerObjType);
    if (trace != null) trace.args(SIL);
    if (trace != null) trace.args(isThreadedRun);
    if (trace != null) trace.args(isSuppressStuds);

    prepareFileRequest(request, files, oldFiles, ownerObjId, ownerObjType);

    // for non threaded run, reply with returned Message Action class type
    Class replyClass = null;
    if (isThreadedRun)
      new UploadRunner(msgActionToSend, request, parentSymmetricKey, SIL, isSuppressStuds).start();
    else
      replyClass = UploadRunner.nonThreadedRun(msgActionToSend, request, parentSymmetricKey, SIL, isSuppressStuds);

    lastReplyClass = replyClass;
    if (trace != null) trace.exit(UploadUtilities.class, replyClass);
    return replyClass;
  }

  public static void prepareFileRequest(File_NewFiles_Rq request, File[] files, FileLinkRecord[] oldFiles, Long ownerObjId, Short ownerObjType) {
    request.fileLinks = new FileLinkRecord[files.length];
    request.fileDataRecords = new FileDataRecord[files.length];

    for (int i=0; i<files.length; i++) {
      File file = files[i];

      request.fileLinks[i] = new FileLinkRecord();
      request.fileDataRecords[i] = new FileDataRecord();

      request.fileLinks[i].ownerObjId = ownerObjId;
      request.fileLinks[i].ownerObjType = ownerObjType;

      String fileName = oldFiles != null && oldFiles[i] != null ? oldFiles[i].getFileName() : file.getName();
      request.fileLinks[i].setFileType(FileTypes.getFileType(fileName));
      request.fileLinks[i].setFileName(fileName);
      request.fileLinks[i].origSize = new Long (file.length());
      request.fileLinks[i].status = oldFiles != null && oldFiles[i] != null ? oldFiles[i].status : null;

      request.fileDataRecords[i].setPlainDataFile(file);
    }
  }

  public static void prepareFileSeal(File_NewFiles_Rq request, KeyRecord mySigningKeyRecord, BASymmetricKey parentSymmetricKey, boolean useStuds, ProgMonitorI progMonitor) {
    FileLinkRecord[] linkRecords = request.fileLinks;
    FileDataRecord[] dataRecords = request.fileDataRecords;

    for (int i=0; i<linkRecords.length; i++) {
      FileLinkRecord linkRecord = linkRecords[i];
      FileDataRecord dataRecord = dataRecords[i];

      BASymmetricKey symmetricKey = new BASymmetricKey(32);
      linkRecord.setSymmetricKey(symmetricKey);

      // seal the link records with the share's or message's symmetric key
      linkRecord.seal(parentSymmetricKey);

      if (useStuds) {
        // For now we'll only provide the signer's key ID as a token to identify the authorized user
        // for "filling-in" the data at a later time.
        dataRecord.setSigningKeyId(mySigningKeyRecord.keyId);
        dataRecord.setEncSize(new Long(-1));
      } else {
        // seal the data with file's symmetric key
        dataRecord.seal(mySigningKeyRecord, symmetricKey, progMonitor, 2);
      }
    }
  }

  /********************************************
  *  Private class   U p l o a d R u n n e r  *
  ********************************************/
  /**
   * Runs seal(s) and upload(s) of files.
   */
  private static class UploadRunner extends ThreadTraced {

    private MessageAction msgActionToSend;
    private File_NewFiles_Rq request;
    private BASymmetricKey parentSymmetricKey;
    private boolean isSuppressStuds;
    private ServerInterfaceLayer SIL;

    /**
     * In the effort to make this function universal for uploading files and sending messages with
     * attachments, arguments include the message action and the request.
     */
    private UploadRunner(MessageAction msgActionToSend, File_NewFiles_Rq request, BASymmetricKey parentSymmetricKey, ServerInterfaceLayer SIL, boolean isSuppressStuds) {
      super("Upload Runner # " + uploadRunnerCount);
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadRunner.class, "UploadRunner(MessageAction msgActionToSend, File_NewFiles_Rq request, BASymmetricKey parentSymmetricKey, ServerInterfaceLayer SIL, boolean isSuppressStuds)");
      if (trace != null) trace.args(msgActionToSend, request, parentSymmetricKey);
      if (trace != null) trace.args(SIL);
      if (trace != null) trace.args(isSuppressStuds);

      this.msgActionToSend = msgActionToSend;
      this.request = request;
      this.parentSymmetricKey = parentSymmetricKey;
      this.SIL = SIL;
      this.isSuppressStuds = isSuppressStuds;
      uploadRunnerCount ++;
      uploadRunnerCount %= Integer.MAX_VALUE-1;

      setDaemon(true);

      if (trace != null) trace.exit(UploadRunner.class);
    }

    public void runTraced() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadRunner.class, "UploadRunner.runTraced()");
      nonThreadedRun(msgActionToSend, request, parentSymmetricKey, SIL, isSuppressStuds);
      if (trace != null) trace.exit(UploadRunner.class);
    }

    /**
     * @return the type of a response, useful to detect error replies.
     */
    public static Class nonThreadedRun(final MessageAction msgActionToSend, File_NewFiles_Rq request, BASymmetricKey parentSymmetricKey, ServerInterfaceLayer SIL, boolean isSuppressStuds) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UploadRunner.class, "nonThreadedRun()");

      Class replyClass = null;

      FetchedDataCache cache = SIL.getFetchedDataCache();
      // Try creating the upload worker concurrently with sealing to minimize connection delay.
      SIL.ensureAtLeastOneAdditionalWorker_SpawnThread();

      // limit number of entries
      UploadDownloadSynch.entry(SIL.getMaxHeavyWorkerCount());

      try {
        FileLinkRecord[] linkRecords = request.fileLinks;
        FileDataRecord[] dataRecords = request.fileDataRecords;

        // create a list of file names
        File[] files = new File[dataRecords.length];
        for (int i=0; i<files.length; i++) {
          files[i] = dataRecords[i].getPlainDataFile();
        }

        boolean useStuds = false;
        if (isSuppressStuds) {
          useStuds = false;
        }
        // If we are making new Message without external email request, use the STUD method and fill-in the data later on.
        else if (GlobalProperties.PROGRAM_BUILD_NUMBER >= 644) { // older builds don't use STUDs
          if (msgActionToSend.getActionCode() == CommandCodes.MSG_Q_NEW) {
            Msg_New_Rq msgRequest = (Msg_New_Rq) msgActionToSend.getMsgDataSet();
            // if all internal msg then ok for studs
            useStuds = msgRequest.symmetricKey == null;
          } else if (msgActionToSend.getActionCode() == CommandCodes.FILE_Q_NEW_FILE_STUDS) {
            useStuds = true;
          }
        }

        if (!Misc.isAllGUIsuppressed() && !useStuds) {
          ProgMonitorI progressMonitor = ProgMonitorFactory.newInstanceTransferUp(files);
          ProgMonitorPool.registerProgMonitor(progressMonitor, msgActionToSend.getStamp());
        }

        KeyRecord mySigningKeyRecord = cache.getKeyRecordMyCurrent();
        prepareFileSeal(request, mySigningKeyRecord, parentSymmetricKey, useStuds, ProgMonitorPool.getProgMonitor(msgActionToSend.getStamp()));

        if (trace != null) trace.data(50, "submitting new request in UploadUtilities");
        ClientMessageAction replyMsgAction = null;
        if (useStuds) // only fast non-streaming action is eligible for retry
          replyMsgAction = SIL.submitAndFetchReply(msgActionToSend, 120000, 1);
        else // longer streaming actions are not retriable - must wait forever
          replyMsgAction = SIL.submitAndFetchReply(msgActionToSend);
        if (trace != null) trace.data(51, "got reply to new request in UploadUtilities");

        replyClass = replyMsgAction.getClass();
        DefaultReplyRunner.nonThreadedRun(SIL, replyMsgAction);

        PriorityFifo prioritizedList = new PriorityFifo();

        // If making studs, submit items for upload to fill-in the studs.
        if (useStuds && replyMsgAction.getActionCode() == CommandCodes.FILE_A_GET_FILES) {
          FileLinkRecord[] links = ((File_GetLinks_Rp) replyMsgAction.getMsgDataSet()).fileLinks;
          for (int i=0; i<links.length; i++) {
            // Merge info from returned links and submited data records.
            // Use enc version of key because if file has a Q&A, sym key may not be available now.
            FileLinkRecord link = links[i];
            FileDataRecord data = dataRecords[i];
            if (!link.getEncSymmetricKey().equals(linkRecords[i].getEncSymmetricKey()))
              throw new IllegalStateException("Encryption key does not match!");
            // If file is protected with Q&A, use our local copy of sym key as fetched file link may not have it decrypted yet.
            link.setSymmetricKey(linkRecords[i].getSymmetricKey());
            Object[] job = new Object[] {link, data};
            prioritizedList.add(job, data.getPlainDataFile().length());
          }
        } else if (useStuds && replyMsgAction.getActionCode() == CommandCodes.MSG_A_GET) {
          Msg_GetLinkAndData_Rp msgReply = ((Msg_GetLinkAndData_Rp) replyMsgAction.getMsgDataSet());
          MsgDataRecord[] msgDatas = msgReply.dataRecords;
          String err = "";
          for (int i=0; i<msgDatas.length; i++) {
            MsgDataRecord msgData = msgDatas[i];
            MsgLinkRecord msgLink = cache.getMsgLinkRecordsForMsg(msgData.msgId)[0];
            FileLinkRecord[] links = FileLinkOps.getOrFetchFileLinksByOwner(SIL, msgLink.msgLinkId, msgData.msgId, Record.RECORD_TYPE_MESSAGE);
            if (links != null) {
              for (int k=0; k<links.length; k++) {
                // Merge info from returned links and submited data records.
                // Use enc version of key because if file has a Q&A, sym key may not be available now.
                FileLinkRecord link = links[k];
                // find matching link from local ones that we know we sent
                int dataIndex = -1;
                for (int z=0; z<linkRecords.length; z++) {
                  if (link.getEncSymmetricKey().equals(linkRecords[z].getEncSymmetricKey())) {
                    dataIndex = z;
                    // If file is protected with Q&A, use our local copy of sym key as fetched file link may not have it decrypted yet.
                    link.setSymmetricKey(linkRecords[z].getSymmetricKey());
                    break;
                  }
                }
                // Only incomplete files should cause an error, but only after we upload the ones that we can...
                // Completed file links that are not found locally are probably remote file attachments.
                if (dataIndex == -1 && link.isIncomplete())
                  err += link.getFileName() + "\n";
                else if (dataIndex >= 0) {
                  FileDataRecord data = dataRecords[dataIndex];
                  Object[] job = new Object[] {link, data};
                  prioritizedList.add(job, data.getPlainDataFile().length());
                }
              }
            }
          }
          if (err != null && err.length() > 0)
            NotificationCenter.show(NotificationCenter.ERROR_MESSAGE, "Attachment Error", "Could not attach the following files. \n\n"+err);
        }
        // Process this batch of uploads in order of priority.
        Iterator iter = prioritizedList.iterator();
        while (iter.hasNext()) {
          Object[] job = (Object[]) iter.next();
          FileLinkRecord link = (FileLinkRecord) job[0];
          FileDataRecord data = (FileDataRecord) job[1];
          new FileLobUp(data.getPlainDataFile(), link, data.getSigningKeyId(), 0);
        }
      } catch (Throwable t) {
        if (trace != null) trace.exception(UploadRunner.class, 100, t);
      } finally {
        // After upload is done, remove the temporary files.
        for (int i=0; i<request.fileDataRecords.length; i++) {
          File encFile = request.fileDataRecords[i].getEncDataFile();
          if (encFile != null && encFile.exists() && !CleanupAgent.wipeOrDelete(encFile))
            GlobalProperties.addTempFileToCleanup(encFile);
        }
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
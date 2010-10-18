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

package com.CH_cl.service.ops;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.actions.file.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.cache.*;
import com.CH_cl.service.cache.event.*;

import com.CH_co.monitor.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.file.File_GetFiles_Rq;
import com.CH_co.service.msg.dataSets.msg.Msg_GetMsgs_Rq;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
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


public class DownloadUtilities extends Object { // implicit no-argument constructor

  private static Class implExportMsgs;

  private static int downloadRunnerCount = 0;
  private static int downloadCoordinatorCount = 0;

  public static final String PROPERTY_NAME__LOCAL_FILE_DEST_DIR = "DownloadUtilities_localFileDestDir";
  public static final String PROPERTY_NAME__DOWNLOAD_AND_OPEN = "DownloadUtilities_downloadAndOpen";

  public static void setImplExportMsgs(Class impl) {
    implExportMsgs = impl;
  }

  /**
   * @return The default download directory as specified in the properties, or current directory if default is invalid.
   */
  public static File getDefaultDestDir() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DownloadUtilities.class, "getDefaultDestDir()");
    File dir = getDefaultDir(PROPERTY_NAME__LOCAL_FILE_DEST_DIR);
    if (trace != null) trace.exit(DownloadUtilities.class, dir);
    return dir;
  }
  public static File getDefaultTempDir() {
    return getDefaultDir(PROPERTY_NAME__DOWNLOAD_AND_OPEN);
  }
  public static File getDefaultDir(String propertyName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DownloadUtilities.class, "getDefaultDir(String propertyName)");
    if (trace != null) trace.args(propertyName);

    File defaultDir = getDirForPropertyName(propertyName);
    if (defaultDir == null || !defaultDir.exists() || !defaultDir.isDirectory()) {
      String fileSeparator = System.getProperty("file.separator");
      if (fileSeparator == null || fileSeparator.length() == 0) fileSeparator = "/";
      if (propertyName.equals(PROPERTY_NAME__DOWNLOAD_AND_OPEN)) {
        File tempFile = null;
        try {
          tempFile = File.createTempFile("test", "testing");
          defaultDir = tempFile.getParentFile();
          tempFile.delete();
        } catch (Throwable t) {
        }
      }
      if (defaultDir == null) defaultDir = getDirForPathName(System.getProperty("user.home") + fileSeparator + "Desktop");
      if (defaultDir == null) defaultDir = getDirForPathName(System.getProperty("user.home") + fileSeparator + "My Documents");
      if (defaultDir == null) defaultDir = getDirForPathName(System.getProperty("user.home"));
      if (defaultDir == null) defaultDir = getDirForPathName(System.getProperty("user.dir"));
      if (defaultDir == null) defaultDir = getDirForPathName(".");
    }

    if (trace != null) trace.exit(DownloadUtilities.class, defaultDir);
    return defaultDir;
  }

  /**
   * @return Directory for specified property name or null if unknown or directory DNE.
   */
  private static File getDirForPropertyName(String propertyName) {
    String pathName = GlobalProperties.getProperty(propertyName);
    return getDirForPathName(pathName);
  }

  /**
   * @return Directory for specified path name or null if directory DNE.
   */
  private static File getDirForPathName(String pathName) {
    File dir = null;
    if (pathName != null && pathName.length() > 0) {
      File tryDir = new File(pathName);
      if (tryDir.exists() && tryDir.isDirectory())
        dir = tryDir;
    }
    return dir;
  }

  /**
   * Sets the new default download directory in the properties.
   */
  public static void setDefaultDestDir(File dir) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DownloadUtilities.class, "setDefaultDestDir(File dir)");
    setDefaultDir(PROPERTY_NAME__LOCAL_FILE_DEST_DIR, dir);
    if (trace != null) trace.exit(DownloadUtilities.class);
  }
  public static void setDefaultDir(String propertyName, File dir) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DownloadUtilities.class, "setDefaultDir(String propertyName, File dir)");
    if (trace != null) trace.args(propertyName, dir);

    if (!dir.isDirectory())
      throw new IllegalArgumentException("must be a directory");
    GlobalProperties.setProperty(propertyName, dir.getAbsolutePath());

    if (trace != null) trace.exit(DownloadUtilities.class);
  }


  /**
   * @param fileLink is the file to fetch
   * @param parentMsgLink if the file is an attachment, this specifies parent message
   */
  public static void downloadAndOpen(FileLinkRecord fileLink, MsgLinkRecord[] parentMsgLinks, ServerInterfaceLayer SIL, boolean openCachedFileFirst, boolean suppressDownloadSoundsAndAutoClose) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DownloadUtilities.class, "downloadAndOpen(FileLinkRecord fileLink, MsgLinkRecord[] parentMsgLinks, ServerInterfaceLayer SIL, boolean openCachedFileFirst, boolean suppressDownloadSoundsAndAutoClose)");
    if (trace != null) trace.args(fileLink, parentMsgLinks, SIL);
    if (trace != null) trace.args(openCachedFileFirst);
    if (trace != null) trace.args(suppressDownloadSoundsAndAutoClose);

    if (openCachedFileFirst && openCachedFile(fileLink)) {
      // all done
    } else {
      File destDir = getDefaultTempDir();
      downloadFilesStartCoordinator(new FileRecord[] { fileLink }, parentMsgLinks, destDir, SIL, false, true, suppressDownloadSoundsAndAutoClose);
      //openCachedFile(fileLink);
    }

    if (trace != null) trace.exit(DownloadUtilities.class);
  }

  /**
   * @param fileLink is the file to fetch
   * @param parentMsgLink if the file is an attachment, this specifies parent message
   */
  public static File download(FileLinkRecord fileLink, MsgLinkRecord[] parentMsgLinks, ServerInterfaceLayer SIL, boolean suppressDownloadSoundsAndAutoClose) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DownloadUtilities.class, "download(FileLinkRecord fileLink, MsgLinkRecord[] parentMsgLinks, ServerInterfaceLayer SIL, boolean openCachedFileFirst, boolean suppressDownloadSoundsAndAutoClose)");
    if (trace != null) trace.args(fileLink, parentMsgLinks, SIL);
    if (trace != null) trace.args(suppressDownloadSoundsAndAutoClose);

    File file = null;
    FileDataRecord fileRec = FetchedDataCache.getSingleInstance().getFileDataRecord(fileLink.fileId);
    if (fileRec != null && fileRec.getPlainDataFile() != null && fileRec.getPlainDataFile().exists()) {
      file = fileRec.getPlainDataFile();
    } else {
      File destDir = getDefaultTempDir();
      downloadFilesStartCoordinator(new FileRecord[] { fileLink }, parentMsgLinks, destDir, SIL, true, false, suppressDownloadSoundsAndAutoClose);
      fileRec = FetchedDataCache.getSingleInstance().getFileDataRecord(fileLink.fileId);
      if (fileRec != null) // this can be null if file transfer was cancelled or interrupted
        file = fileRec.getPlainDataFile();
    }

    if (trace != null) trace.exit(DownloadUtilities.class, file);
    return file;
  }

  public static boolean openCachedFile(FileLinkRecord fileLink) {
    boolean cachedFileOpened = false;
    FileDataRecord fileRec = FetchedDataCache.getSingleInstance().getFileDataRecord(fileLink.fileId);
    cachedFileOpened = FileLauncher.openFile(fileRec);
    return cachedFileOpened;
  }

  public static void downloadFilesStartCoordinator(Record[] toDownload, MsgLinkRecord[] fromMsgs, File destDir, ServerInterfaceLayer SIL) {
    downloadFilesStartCoordinator(toDownload, fromMsgs, destDir, SIL, false, false, false);
  }
  public static void downloadFilesStartCoordinator(Record[] toDownload, MsgLinkRecord[] fromMsgs, File destDir, ServerInterfaceLayer SIL, boolean waitForComplete, boolean openAfterDownload, boolean suppressDownloadSoundsAndAutoClose) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DownloadUtilities.class, "downloadFilesStartCoordinator(Record[] toDownload, MsgLinkRecord[] fromMsgs, File destDir, ServerInterfaceLayer SIL, boolean waitForComplete, boolean openAfterDownload, boolean suppressDownloadSoundsAndAutoClose)");
    if (trace != null) trace.args(toDownload, fromMsgs, destDir, SIL);
    if (trace != null) trace.args(waitForComplete);
    if (trace != null) trace.args(openAfterDownload);
    if (trace != null) trace.args(suppressDownloadSoundsAndAutoClose);
    Thread th = new DownloadCoordinator(toDownload, fromMsgs, destDir, SIL, waitForComplete, openAfterDownload, suppressDownloadSoundsAndAutoClose);
    th.start();
    if (waitForComplete)
      try { th.join(); } catch (InterruptedException e) { }
    if (trace != null) trace.exit(DownloadUtilities.class);
  }


  /**********************************************************
  *  Private class   D o w n l o a d C o o r d i n a t o r  *
  **********************************************************/
  /**
   * Coordinates download of files/messages and directory trees.
   * Searches recursively through download file tree, and creates required directories
   * on the local system and launches a file download thread to carry on downloads into
   * those directories.
   */
  public static class DownloadCoordinator extends ThreadTraced {

    private Record[] toDownload;
    private MsgLinkRecord[] fromMsgs;  // set only when fetching file attachments from specified messages, else NULL
    private File destDir;
    private ServerInterfaceLayer SIL;
    private boolean waitForComplete;
    private boolean openAfterDownload;
    private boolean suppressDownloadSoundsAndAutoClose;

    public DownloadCoordinator(Record[] toDownload, MsgLinkRecord[] fromMsgs, File destDir, ServerInterfaceLayer SIL) {
      this(toDownload, fromMsgs, destDir, SIL, false, false, false);
    }

    public DownloadCoordinator(Record[] toDownload, MsgLinkRecord[] fromMsgs, File destDir, ServerInterfaceLayer SIL, boolean waitForComplete, boolean openAfterDownload, boolean suppressDownloadSoundsAndAutoClose) {
      super("Download Coordinator # " + downloadCoordinatorCount);
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DownloadCoordinator.class, "DownloadCoordinator(Record[] toDownload, MsgLinkRecord[] fromMsgs, File destDir, ServerInterfaceLayer SIL, boolean waitForComplete, boolean openAfterDownload, boolean suppressDownloadSoundsAndAutoClose)");
      if (trace != null) trace.args(toDownload, fromMsgs, destDir, SIL);
      if (trace != null) trace.args(waitForComplete);
      if (trace != null) trace.args(openAfterDownload);
      if (trace != null) trace.args(suppressDownloadSoundsAndAutoClose);

      this.toDownload = toDownload;
      this.fromMsgs = fromMsgs;
      this.destDir = destDir;
      this.SIL = SIL;
      this.waitForComplete = waitForComplete;
      this.openAfterDownload = openAfterDownload;
      this.suppressDownloadSoundsAndAutoClose = suppressDownloadSoundsAndAutoClose;
      downloadCoordinatorCount ++;
      downloadCoordinatorCount %= Integer.MAX_VALUE-1;

      setDaemon(true);

      if (trace != null) trace.exit(DownloadCoordinator.class);
    }
    public void runTraced() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DownloadCoordinator.class, "DownloadCoordinator.runTraced()");
      downloadRecords(toDownload, fromMsgs, destDir, false, true, null, SIL);
      if (trace != null) trace.exit(DownloadCoordinator.class);
    }

    private void downloadRecords(Record[] toDownload, MsgLinkRecord[] fromMsgs, File destDir, boolean fetchFilesForSingleFolders, boolean fetchFilesForFolderTreeAtOnce, Set excludeDirsAlreadyDone, ServerInterfaceLayer SIL) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DownloadCoordinator.class, "downloadRecords(Record[] toDownload, MsgLinkRecord[] fromMsgs, File destDir, boolean fetchFilesForSingleFolders, boolean fetchFilesForFolderTree, Collection excludeDirsAlreadyDone, ServerInterfaceLayer SIL)");
      if (trace != null) trace.args(toDownload, fromMsgs, destDir);
      if (trace != null) trace.args(fetchFilesForFolderTreeAtOnce);
      if (trace != null) trace.args(excludeDirsAlreadyDone, SIL);

      if (fetchFilesForSingleFolders && fetchFilesForFolderTreeAtOnce) {
        throw new IllegalArgumentException("Fetch files for single folder or for entire tree, but not both.");
      }

      // make sure that destination directory exists
      if (!destDir.exists() && !destDir.mkdirs()) {
        NotificationCenter.show(NotificationCenter.ERROR_MESSAGE, "File Error", "Failed to create necessary directory: " + destDir.getAbsolutePath());
      }
      else {

        ArrayList dirsV = new ArrayList();
        ArrayList filesV = new ArrayList();
        ArrayList msgsV = new ArrayList();
        // distinguish between files/messages/directories
        for (int i=0; i<toDownload.length; i++) {
          Record recToDownload  = toDownload[i];
          if (recToDownload instanceof FolderPair) {
            if (excludeDirsAlreadyDone == null || !excludeDirsAlreadyDone.contains(recToDownload))
              dirsV.add(recToDownload);
          }
          else if (recToDownload instanceof FileLinkRecord)
            filesV.add(recToDownload);
          else if (recToDownload instanceof MsgLinkRecord)
            msgsV.add(recToDownload);
          else
            throw new IllegalArgumentException("Don't know how to handle download of object type " + recToDownload.getClass());
        }
        // download messages
        if (msgsV.size() > 0) {
          MsgLinkRecord[] m = new MsgLinkRecord[msgsV.size()];
          msgsV.toArray(m);
          String title = "Function not available.";
          String errMsg = "<html><p>Mail export libraries could not be found.  To activate this functionality please download the latest version of the software from the following page: </p><p><a href=\"http://www.cryptoheaven.com/Download\">http://www.cryptoheaven.com/Download</a></p><p>Please close this software before installing the downloaded package.</p>";
          try {
            ExportMsgsI exportMsgUtilities = (ExportMsgsI) implExportMsgs.newInstance();
            exportMsgUtilities.runDownloadMsgs(m, fromMsgs, destDir, SIL, waitForComplete, openAfterDownload);
          } catch (Exception ex) {
            NotificationCenter.show(NotificationCenter.ERROR_MESSAGE, title, errMsg);
          } catch (NoClassDefFoundError err) {
            NotificationCenter.show(NotificationCenter.ERROR_MESSAGE, title, errMsg);
          }
        }
        // download files
        if (filesV.size() > 0) {
          FileLinkRecord[] f = new FileLinkRecord[filesV.size()];
          filesV.toArray(f);
          runDownloadFiles(f, fromMsgs, destDir, SIL, waitForComplete, openAfterDownload, suppressDownloadSoundsAndAutoClose);
        }
        // process directories
        if (dirsV.size() > 0) {
          FolderPair[] f = new FolderPair[dirsV.size()];
          dirsV.toArray(f);

          // exclude non-data type folders (ie: category folders) from fetching
          ArrayList dataFoldersL = new ArrayList();
          for (int i=0; i<f.length; i++) {
            FolderRecord fRec = f[i].getFolderRecord();
            if (!fRec.isCategoryType() && !fRec.isLocalFileType())
              dataFoldersL.add(f[i]);
          }
          FolderPair[] dataFolders = (FolderPair[]) ArrayUtils.toArray(dataFoldersL, FolderPair.class);

          // fetch all messgae links to make sure we have them in our cache
          if (dataFolders != null && dataFolders.length > 0) {
            fetchMsgListings(dataFolders, SIL);
          }
          // fetch all file links to make sure we have them in our cache
          if (fetchFilesForFolderTreeAtOnce) {
            // gather entire tree, starting with original folder set, not just dataFolders...
            FetchedDataCache cache = SIL.getFetchedDataCache();
            FolderPair[] allDescendantAndParentPairs = cache.getFolderPairsViewAllDescending(f, true);
            // eliminate non-data folders
            ArrayList descendantDataPairsL = new ArrayList();
            for (int i=0; i<allDescendantAndParentPairs.length; i++) {
              FolderRecord fRec = allDescendantAndParentPairs[i].getFolderRecord();
              if (!fRec.isCategoryType() && !fRec.isLocalFileType())
                descendantDataPairsL.add(allDescendantAndParentPairs[i]);
            }
            if (descendantDataPairsL.size() > 0) {
              fetchFileListings((FolderPair[]) ArrayUtils.toArray(descendantDataPairsL, FolderPair.class), SIL);
            }
          } else if (fetchFilesForSingleFolders) {
            if (dataFolders != null && dataFolders.length > 0) {
              fetchFileListings(dataFolders, SIL);
            }
          }

          FetchedDataCache cache = ServerInterfaceLayer.getFetchedDataCache();

          for (int i=0; i<f.length; i++) {
            FolderRecord folderRecord = f[i].getFolderRecord();
            FolderShareRecord shareRecord = f[i].getFolderShareRecord();
            String dirName = FileTypes.getDirSafeString(shareRecord.getFolderName());
            File newDestDir = new File(destDir, dirName);

            // Recursive call should avoid nesting into this folder as we are hendling it here...
            if (excludeDirsAlreadyDone == null) excludeDirsAlreadyDone = new HashSet();
            excludeDirsAlreadyDone.add(f[i]);

            if (!folderRecord.isCategoryType() && !folderRecord.isLocalFileType()) {
              // Download all messages to this directory...
              downloadRecords(cache.getMsgLinkRecordsForFolder(shareRecord.folderId), null, newDestDir, fetchFilesForSingleFolders, false, excludeDirsAlreadyDone, SIL);

              // Download all files to this directory...
              downloadRecords(cache.getFileLinkRecords(shareRecord.shareId), null, newDestDir, fetchFilesForSingleFolders, false, excludeDirsAlreadyDone, SIL);
            }

            // Download all child directories too...
            if (trace != null) trace.data(100, "looking for children of folder", shareRecord.folderId);
            FolderPair[] childFolderPairs = cache.getFolderPairsViewChildren(shareRecord.folderId, true);
            // make sure none of our child folders were already handled (case of looping folders)
            childFolderPairs = (FolderPair[]) RecordUtils.difference(childFolderPairs, excludeDirsAlreadyDone);
            // make sure we don't nest into folders we were specified to handle (case of multiple folders specified when some might be children)
            childFolderPairs = (FolderPair[]) RecordUtils.difference(childFolderPairs, f);

            if (childFolderPairs != null && childFolderPairs.length > 0) {
              downloadRecords(childFolderPairs, null, newDestDir, fetchFilesForSingleFolders, false, excludeDirsAlreadyDone, SIL);
            }
          } // end for
        } // end if any directories
      }

      if (trace != null) trace.exit(DownloadCoordinator.class);
    }
  }

  private static void fetchMsgListings(FolderPair[] folders, ServerInterfaceLayer SIL) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DownloadUtilities.class, "fetchMsgListings(FolderPair[] folders, ServerInterfaceLayer SIL)");
    if (trace != null) trace.args(folders);

    final FetchedDataCache _cache = ServerInterfaceLayer.getFetchedDataCache();

    FolderShareRecord[] fldShares = FolderPair.getFolderShareRecords(folders);
    Long[] folderIDs = FolderShareRecord.getFolderIDs(fldShares);

    final ArrayList _folderIDsBeingFetched = new ArrayList(Arrays.asList(folderIDs));

    // Register the completion notify listener
    FolderRecordListener folderListener = new FolderRecordListener() {
      public void folderRecordUpdated(FolderRecordEvent e) {
        int eventType = e.getEventType();
        if (eventType == RecordEvent.FOLDER_FETCH_COMPLETED || eventType == RecordEvent.FOLDER_FETCH_INTERRUPTED) {
          FolderRecord[] fldRecs = e.getFolderRecords();
          synchronized (_folderIDsBeingFetched) {
            for (int i=0; i<fldRecs.length; i++) {
              _folderIDsBeingFetched.remove(fldRecs[i].folderId);
            }
            _folderIDsBeingFetched.notifyAll();
            if (_folderIDsBeingFetched.isEmpty())
              _cache.removeFolderRecordListener(this);
          }
        }
      }
    };
    _cache.addFolderRecordListener(folderListener);

    // First lets fetch all messages so they are in cache and can be downloaded, later on another process will have to fetch attachments too...
    for (int i=0; i<fldShares.length; i++) {
      Long shareId = fldShares[i].shareId;
      Long folderId = fldShares[i].folderId;
      Msg_GetMsgs_Rq request = new Msg_GetMsgs_Rq(shareId, Record.RECORD_TYPE_FOLDER, folderId, (short) -Msg_GetMsgs_Rq.FETCH_NUM_LIST__INITIAL_SIZE, (short) Msg_GetMsgs_Rq.FETCH_NUM_NEW__INITIAL_SIZE, (Timestamp) null);
      // Gather messages already fetched so we don't re-fetch all items if not necessary
      MsgLinkRecord[] existingMsgLinks = CacheUtilities.getMsgLinkRecordsWithFetchedDatas(folderId);
      request.exceptLinkIDs = RecordUtils.getIDs(existingMsgLinks);
      MessageAction msgAction = new MessageAction(CommandCodes.MSG_Q_GET_FULL, request);
      SIL.submitAndReturn(msgAction, 30000);
    }

    // wait until all folder fetching completes
    synchronized (_folderIDsBeingFetched) {
      while (true) {
        if (_folderIDsBeingFetched.isEmpty()) {
          break;
        } else {
          try {
            _folderIDsBeingFetched.wait(1000);
          } catch (InterruptedException e) {
            break;
          }
        }
      }
    }

    if (trace != null) trace.exit(DownloadUtilities.class);
  }

  private static void fetchFileListings(FolderPair[] folders, ServerInterfaceLayer SIL) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DownloadUtilities.class, "fetchFileListings(FolderPair[] folders, ServerInterfaceLayer SIL)");
    if (trace != null) trace.args(folders);

    final FetchedDataCache _cache = ServerInterfaceLayer.getFetchedDataCache();

    FolderShareRecord[] fldShares = FolderPair.getFolderShareRecords(folders);
    Long[] folderIDs = FolderShareRecord.getFolderIDs(fldShares);

    final ArrayList _folderIDsBeingFetched = new ArrayList(Arrays.asList(folderIDs));

    // Register the completion notify listener
    FolderRecordListener folderListener = new FolderRecordListener() {
      public void folderRecordUpdated(FolderRecordEvent e) {
        int eventType = e.getEventType();
        if (eventType == RecordEvent.FOLDER_FETCH_COMPLETED || eventType == RecordEvent.FOLDER_FETCH_INTERRUPTED) {
          FolderRecord[] fldRecs = e.getFolderRecords();
          synchronized (_folderIDsBeingFetched) {
            for (int i=0; i<fldRecs.length; i++) {
              _folderIDsBeingFetched.remove(fldRecs[i].folderId);
            }
            _folderIDsBeingFetched.notifyAll();
            if (_folderIDsBeingFetched.isEmpty())
              _cache.removeFolderRecordListener(this);
          }
        }
      }
    };
    _cache.addFolderRecordListener(folderListener);

    // Lets fetch all files ...
    for (int i=0; i<fldShares.length; i++) {
      Long shareId = fldShares[i].shareId;
      Long folderId = fldShares[i].folderId;
      File_GetFiles_Rq request = new File_GetFiles_Rq(shareId, Record.RECORD_TYPE_FOLDER, folderId, (short) -File_GetFiles_Rq.FETCH_NUM_LIST__INITIAL_SIZE, (Timestamp) null);
      // Gather files already fetched so we don't re-fetch all items if not necessary
      FileLinkRecord[] existingLinks = _cache.getFileLinkRecords(shareId);
      request.exceptLinkIDs = RecordUtils.getIDs(existingLinks);
      MessageAction msgAction = new MessageAction(CommandCodes.FILE_Q_GET_FILES_STAGED, request);
      SIL.submitAndReturn(msgAction);
    }

    // wait until all folder fetching completes
    synchronized (_folderIDsBeingFetched) {
      while (true) {
        if (_folderIDsBeingFetched.isEmpty()) {
          break;
        } else {
          try {
            _folderIDsBeingFetched.wait(1000);
          } catch (InterruptedException e) {
            break;
          }
        }
      }
    }

    if (trace != null) trace.exit(DownloadUtilities.class);
  }


  /**
   * Sets a request to download a file to the destination, batches up the files in groups of 10.
   * @param files Remote files to download.
   * @param fromMsgs is the message parent to specified files or is NULL if downloading from a folder.
   * @param destDir is the Local destination directory to which to download the files
   **/
  private static void runDownloadFiles(FileLinkRecord[] files, MsgLinkRecord[] fromMsgs, File destDir, ServerInterfaceLayer SIL, boolean waitForComplete, boolean openAfterDownload, boolean suppressDownloadSoundsAndAutoClose) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DownloadUtilities.class, "runDownloadFile(FileLinkRecord[] files, MsgLinkRecord[] fromMsgs, File destDir, ServerInterfaceLayer SIL, boolean waitForComplete, boolean openAfterDownload, boolean suppressDownloadSoundsAndAutoClose)");
    if (trace != null) trace.args(files, fromMsgs, destDir, SIL);
    if (trace != null) trace.args(waitForComplete);
    if (trace != null) trace.args(openAfterDownload);
    if (trace != null) trace.args(suppressDownloadSoundsAndAutoClose);

    if (destDir != null && files != null && files.length > 0) {
      // divide FileLinkRecord[] into chunks of 10;
      int count = 0;
      while (count < files.length) {
        int bunchSize = Math.min(10, files.length-count);
        FileLinkRecord[] bunch = new FileLinkRecord[bunchSize];
        for (int i=0; i<bunchSize; i++) {
          bunch[i] = files[count+i];
        }
        runDownloadFileBunch(bunch, fromMsgs, destDir, SIL, waitForComplete, openAfterDownload, suppressDownloadSoundsAndAutoClose);
        count += bunchSize;
      }
    }

    if (trace != null) trace.exit(DownloadUtilities.class);
  }


  private static void runDownloadFileBunch(FileLinkRecord[] files, MsgLinkRecord[] fromMsgs, File destDir, ServerInterfaceLayer SIL, boolean waitForComplete, boolean openAfterDownload, boolean suppressDownloadSoundsAndAutoClose) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DownloadUtilities.class, "FileLinkRecord[] files, MsgLinkRecord[] fromMsgs, File destDir, ServerInterfaceLayer SIL, boolean waitForComplete, boolean openAfterDownload, boolean suppressDownloadSoundsAndAutoClose)");
    if (trace != null) trace.args(files, fromMsgs, destDir, SIL);
    if (trace != null) trace.args(waitForComplete);
    if (trace != null) trace.args(openAfterDownload);
    if (trace != null) trace.args(suppressDownloadSoundsAndAutoClose);

    FetchedDataCache cache = ServerInterfaceLayer.getFetchedDataCache();

    // Order by fileLinkID ascending
    Arrays.sort(files);

    Long[] folderIDs = null;
    if (fromMsgs == null) {
      folderIDs = FileLinkRecord.getOwnerObjIDs(files, Record.RECORD_TYPE_FOLDER);
    } else {
      // don't specify folderIDs if messages don't reside in folders
      folderIDs = MsgLinkRecord.getOwnerObjIDs(fromMsgs, Record.RECORD_TYPE_FOLDER);
    }
    FolderShareRecord[] shareRecords = cache.getFolderSharesMyForFolders(folderIDs, true);

    // file names for the progress monitor
    String[] fileNames = new String[files.length];
    for (int i=0; i<files.length; i++) {
      fileNames[i] = files[i].getFileName();
    }

    Obj_IDs_Co request = new Obj_IDs_Co();
    if (fromMsgs == null)
      request.IDs = new Long[2][];
    else
      request.IDs = new Long[3][];
    request.IDs[0] = RecordUtils.getIDs(files);
    if (shareRecords != null && shareRecords.length > 0)
      request.IDs[1] = RecordUtils.getIDs(shareRecords);
    else
      request.IDs[1] = new Long[0];

    if (fromMsgs != null) {
      request.IDs[2] = RecordUtils.getIDs(fromMsgs);
    }

    MessageAction msgAction = new MessageAction(CommandCodes.FILE_Q_GET_FILES_DATA, request);
    Thread th = new DownloadFileRunner(msgAction, destDir, files, fileNames, SIL, openAfterDownload, suppressDownloadSoundsAndAutoClose);
    th.start();
    if (waitForComplete)
      try { th.join(); } catch (InterruptedException e) { }

    if (trace != null) trace.exit(DownloadUtilities.class);
  }


  public static class DownloadFileRunner extends ThreadTraced {

    private MessageAction msgAction;
    private File destDir;
    private FileLinkRecord[] files;
    private String[] fileNames;
    private ServerInterfaceLayer SIL;
    private boolean openAfterDownload;
    private boolean suppressDownloadSoundsAndAutoClose;

    public DownloadFileRunner(MessageAction msgAction, File destDir, FileLinkRecord[] files, String[] fileNames, ServerInterfaceLayer SIL, boolean openAfterDownload, boolean suppressDownloadSoundsAndAutoClose) {
      super("Download File Runner # " + downloadRunnerCount);
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DownloadFileRunner.class, "DownloadRunner(MessageAction msgAction, File destDir, ServerInterfaceLayer SIL, boolean openAfterDownload, boolean suppressDownloadSoundsAndAutoClose)");
      if (trace != null) trace.args(msgAction, destDir, fileNames, SIL);
      if (trace != null) trace.args(openAfterDownload);
      if (trace != null) trace.args(suppressDownloadSoundsAndAutoClose);

      this.msgAction = msgAction;
      this.destDir = destDir;
      this.files = files;
      this.fileNames = fileNames;
      this.SIL = SIL;
      this.openAfterDownload = openAfterDownload;
      this.suppressDownloadSoundsAndAutoClose = suppressDownloadSoundsAndAutoClose;

      downloadRunnerCount ++;
      downloadRunnerCount %= Integer.MAX_VALUE-1;

      setDaemon(true);

      if (trace != null) trace.exit(DownloadFileRunner.class);
    }

    public void runTraced() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DownloadFileRunner.class, "DownloadFileRunner.runTraced()");

      int maxHeavyWorkers = SIL.getMaxHeavyWorkerCount();
      int maxThreadsInSynchronizedBlock = maxHeavyWorkers;
      if (maxHeavyWorkers < 1)
        maxThreadsInSynchronizedBlock = 1;

      ClientMessageAction replyAction = null;

      // limit number of entries
      UploadDownloadSynch.entry(maxThreadsInSynchronizedBlock);
      try {
        // number of visible progress monitors are limited to the number of active transfer connections too
        ProgMonitorI progressMonitor = ProgMonitorFactory.newInstanceTransferDown(fileNames, destDir, files, !openAfterDownload, suppressDownloadSoundsAndAutoClose);
        ProgMonitorPool.registerProgMonitor(progressMonitor, msgAction.getStamp());

        replyAction = SIL.submitAndFetchReply(msgAction, 0);
      } catch (Throwable t) {
        if (trace != null) trace.exception(DownloadFileRunner.class, 100, t);
      }
      // catch everything so we can decrement the counter properly
      // account for every exit
      UploadDownloadSynch.exit();

      if (replyAction.getActionCode() == CommandCodes.FILE_A_GET_FILES_DATA && replyAction instanceof FileAGetFilesData) {
        ((FileAGetFilesData)replyAction).setDestinationDirectory(destDir);
      }

      // we are already in a dedicated thread so execute the action synchronously
      DefaultReplyRunner.nonThreadedRun(SIL, replyAction);

      if (trace != null) trace.exit(DownloadFileRunner.class);
    }
  } // end inner class DownloadRunner
}
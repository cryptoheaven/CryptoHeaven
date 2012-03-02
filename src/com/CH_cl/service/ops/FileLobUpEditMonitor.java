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
import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;

import com.CH_co.cryptx.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.Str_Rp;
import com.CH_co.service.msg.dataSets.file.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import java.io.*;
import java.util.*;

/**
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Monitors a local file for changes and re-uploads edited documents.
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class FileLobUpEditMonitor {

  private static boolean ENABLE_SYNCH_CONFIRMATION = false;
  private static long[] ERROR_DELAY_BEFORE_RETRY__MILLIS = new long[] { 5*1000, 10*1000, 15*1000, 30*1000, 2*60*1000, 5*60*1000, 15*60*1000 };

  public static final Object monitor = new Object();
  private static boolean isInitialized = false;

  /** List of files we are tracking for modifications. */
  private HashMap fileMap;
  /** List of files from the "fileMap" that were determined to be modified, but could not upload due to connectivity issues. */
  private HashMap fileEditsMap;

  /** Singleton getter. */
  public static FileLobUpEditMonitor getInstance() {
    return FileLobUpEditMonitorHolder.INSTANCE;
  }
  /** Singleton holder. */
  private static class FileLobUpEditMonitorHolder {
    private static final FileLobUpEditMonitor INSTANCE = new FileLobUpEditMonitor();
  }


  private FileLobUpEditMonitor() {
    init();
  }

  private void init() {
    fileMap = new HashMap();
    fileEditsMap = new HashMap();
    Thread runner = new ThreadTraced(new Runnable() {
      public void run() {
        while (true) {
          try {
            Thread.sleep(1000);
            long start = System.currentTimeMillis();
            doScanForChanges();
            long end = System.currentTimeMillis();
            Thread.sleep(Math.min(10000, Math.abs(end-start))); // additionally sleep the amount of time it took to run the scan, but at most 10sec.
          } catch (Throwable t) {
          }
        }
      }
    }, "FileLobUpEditMonitor");
    // Use low priority because digesting files could be CPU intensive and we don't want GUI responsivness to suffer.
    runner.setPriority(Thread.MIN_PRIORITY);
    runner.setDaemon(true);
    runner.start();
    isInitialized = true;
  }

  public static boolean canMonitor(FileLinkRecord remoteFile) {
    boolean canMonitor = false;
    if (remoteFile.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
      FolderShareRecord share = FetchedDataCache.getSingleInstance().getFolderShareRecordMy(remoteFile.ownerObjId, true);
      if (share != null && share.canWrite.shortValue() == FolderShareRecord.YES)
        canMonitor = true;
    }
    return canMonitor;
  }

  private static FileSet[] getFileSets(HashMap map) {
    FileSet[] set = null;
    synchronized (monitor) {
      if (isInitialized) {
        Collection c = map.values();
        set = new FileSet[c.size()];
        c.toArray(set);
      }
    }
    return set;
  }
  public static FileSet[] getModifiedFileSets() {
    FileSet[] set = null;
    synchronized (monitor) {
      if (isInitialized) {
        set = getFileSets(getInstance().fileEditsMap);
      }
    }
    return set;
  }
  public static FileSet[] getMonitoredFileSets() {
    FileSet[] set = null;
    synchronized (monitor) {
      if (isInitialized) {
        set = getFileSets(getInstance().fileMap);
      }
    }
    return set;
  }

  /**
   * Register a local file with remote link to be watched for local editing changes.
   * @param localFile
   * @param remoteFile 
   */
  public static void registerForMonitoring(File localFile, FileLinkRecord remoteFile, FileDataRecord remoteData) {
    Trace trace = null; if (Trace.DEBUG) trace = Trace.entry(FileLobUpEditMonitor.class, "registerForMonitoring(File localFile, FileLinkRecord remoteFile, FileDataRecord remoteData)");
    if (trace != null) trace.args(localFile, remoteFile, remoteData);
    if (!canMonitor(remoteFile))
      throw new IllegalArgumentException("File cannot be monitored for changes.");
    if (!remoteFile.fileId.equals(remoteData.fileId))
      throw new IllegalArgumentException("Please supply matching file link and data objects.");
    addToMonitoring(new FileSet(localFile, remoteFile, remoteData));
    if (trace != null) trace.exit(FileLobUpEditMonitor.class);
  }

  public static boolean addToMonitoring(FileSet set) {
    Trace trace = null; if (Trace.DEBUG) trace = Trace.entry(FileLobUpEditMonitor.class, "addToMonitoring(FileSet set)");
    if (trace != null) trace.args(set);
    boolean rc = false;
    synchronized (monitor) {
      if (set.getLocalFile().exists()) {
        getInstance().fileMap.put(set.getRemoteFile().fileLinkId, set);
        rc = true;
      }
    }
    if (trace != null) trace.exit(FileLobUpEditMonitor.class, rc);
    return rc;
  }

  public static void removeFromMonitoring(Long fileLinkId) {
    Trace trace = null; if (Trace.DEBUG) trace = Trace.entry(FileLobUpEditMonitor.class, "removeFromMonitoring(Long fileLinkId)");
    if (trace != null) trace.args(fileLinkId);
    synchronized (monitor) {
      FileLobUpEditMonitor instance = getInstance();
      instance.fileMap.remove(fileLinkId);
      instance.fileEditsMap.remove(fileLinkId);
    }
    if (trace != null) trace.exit(FileLobUpEditMonitor.class);
  }

  private void doScanForChanges() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLobUpEditMonitor.class, "doScanForChanges()");
    synchronized (monitor) {
      Set keys = fileMap.keySet();
      if (keys != null && keys.size() > 0) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        Iterator iter = keys.iterator();
        while (iter.hasNext()) {
          Object key = iter.next();
          final FileSet set = (FileSet) fileMap.get(key);
          if (!set.localFile.exists()) {
            iter.remove();
            removeFromMonitoring(set.getRemoteFile().fileLinkId);
          } else if (set.timestampNextRetry > System.currentTimeMillis()) {
            // ignore for now until enough time passes from last error
            // Correct invalid stamps incase system clock was adjusted
            set.timestampNextRetry = Math.min(set.timestampNextRetry, System.currentTimeMillis()+ERROR_DELAY_BEFORE_RETRY__MILLIS[ERROR_DELAY_BEFORE_RETRY__MILLIS.length-1]);
          } else {
            final long lastModified = set.localFile.lastModified();
            if (lastModified != set.timestampTrackingStart) {
              // see if we need to re-compute the digest
              if (set.lastDigest == null || set.timestampLastDigest < lastModified) {
                try {
                  set.lastDigest = new BADigestBlock(Digester.digestFile(set.localFile, Digester.getDigest(SHA256.name)));
                  set.timestampLastDigest = lastModified;
                } catch (FileNotFoundException x) {
                } catch (IOException x) {
                }
              }
              if (set.lastDigest != null) {
                // if file content is same or changed
                if (set.lastDigest.equals(set.trackingDataDigest)) {
                  // Since we verified identical content, update our timestamp so next time we check digest is upon next modification and not loop like crazy.
                  set.timestampTrackingStart = lastModified;
                } else {
                  if (FileLobUp.isFileInQueue(set.localFile.getAbsolutePath())) {
                    set.setError("File upload already in progress.");
                    fileEditsMap.put(set.remoteFile.fileLinkId, set);
                  } else {
                    boolean hasWritePrivilege = false;
                    FolderShareRecord share = null;
                    if (set.remoteFile.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
                      share = cache.getFolderShareRecordMy(set.remoteFile.ownerObjId, true);
                      hasWritePrivilege = share != null && share.canWrite.shortValue() == FolderShareRecord.YES;
                    }
                    if (!hasWritePrivilege) {
                      if (share == null)
                        set.setError("Folder was deleted or is inaccessible.");
                      else
                        set.setError("No 'write' privilege to folder '"+share.getFolderName()+"'.");
                      fileEditsMap.put(set.remoteFile.fileLinkId, set);
                    } else {
                      // check if connection is active
                      final ServerInterfaceLayer SIL = ServerInterfaceLayer.lastSIL;
                      boolean connectionVerified = SIL != null && SIL.isLoggedIn() && SIL.hasMainWorker();
                      if (!connectionVerified) {
                        set.setError("No connection to server.");
                        fileEditsMap.put(set.remoteFile.fileLinkId, set);
                      } else {
                        try {
                          if (set.remoteFile.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
                            if (!ENABLE_SYNCH_CONFIRMATION) {
                              doSendFile(set, share, SIL);
                            } else {
                              final FolderShareRecord _share = share;
                              Runnable yes = new Runnable() {
                                public void run() {
                                  doSendFile(set, _share, SIL);
                                }
                              };
                              Runnable no = null;
                              String msg = "The following file has changed, upload changes?\n\n"+set.remoteFile.getFileName();
                              if (!set.remoteFile.getFileName().equals(set.localFile.getName()))
                                msg += "\n\nLocal file name:\n"+set.localFile.getName();
                              NotificationCenter.showYesNo(NotificationCenter.QUESTION_MESSAGE, "Upload changes?", msg, false, yes, no);
                            }
                          }
                        } catch (Throwable t) {
                          if (trace != null) trace.exception(FileLobUpEditMonitor.class, 100, t);
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    if (trace != null) trace.exit(FileLobUpEditMonitor.class);
  }

  private void doSendFile(FileSet set, FolderShareRecord share, ServerInterfaceLayer SIL) {
    synchronized (monitor) {
      File_NewFiles_Rq request = new File_NewFiles_Rq();
      // Prepare request using share to help speed up engine's authorization, it will be converted to folder id and type by the engine.
      UploadUtilities.prepareFileRequest(request, new File[] {set.localFile}, new FileLinkRecord[] {set.remoteFile}, share.shareId, new Short(Record.RECORD_TYPE_SHARE));
      KeyRecord mySigningKeyRecord = SIL.getFetchedDataCache().getKeyRecordMyCurrent();
      UploadUtilities.prepareFileSeal(request, mySigningKeyRecord, share.getSymmetricKey(), true, null);
      MessageAction msgActionToSend = new MessageAction(CommandCodes.FILE_Q_NEW_FILE_STUDS_BACKGROUND, request);
      ClientMessageAction replyMsgAction = SIL.submitAndFetchReply(msgActionToSend, 120000, 1);
      DefaultReplyRunner.nonThreadedRun(SIL, replyMsgAction, true);
      // If success, mark this update with current evaluation and dispatch FileLobUp
      if (replyMsgAction.getActionCode() == CommandCodes.FILE_A_GET_FILES) {
        set.errorCount = 0;
        set.timestampNextRetry = 0;
        set.timestampTrackingStart = System.currentTimeMillis();
        set.trackingDataDigest = set.lastDigest;
        fileEditsMap.remove(set.remoteFile.fileLinkId);
        FileLinkRecord link = ((File_GetLinks_Rp) replyMsgAction.getMsgDataSet()).fileLinks[0];
        FileDataRecord data = request.fileDataRecords[0];
        new FileLobUp(data.getPlainDataFile(), link, data.getSigningKeyId(), 0);
      } else {
        set.errorCount ++;
        int errorDelayIndex = Math.min((int) set.errorCount, ERROR_DELAY_BEFORE_RETRY__MILLIS.length) - 1;
        set.timestampNextRetry = System.currentTimeMillis() + ERROR_DELAY_BEFORE_RETRY__MILLIS[errorDelayIndex];
        ProtocolMsgDataSet dataSet = replyMsgAction.getMsgDataSet();
        String errMsg = "Send failed.";
        if (dataSet instanceof Str_Rp) {
          // Use specific error message if available.
          errMsg = ((Str_Rp) dataSet).message;
        }
        errMsg += " - retry scheduled for "+Misc.getFormattedDate(new Date(set.timestampNextRetry), false);
        set.setError(errMsg);
        fileEditsMap.put(set.remoteFile.fileLinkId, set);
      }
    }
  }

  public static class FileSet {
    String error;
    File localFile;
    FileLinkRecord remoteFile;
    FileDataRecord remoteData;
    long timestampTrackingStart;
    BADigestBlock trackingDataDigest;
    long timestampLastDigest;
    BADigestBlock lastDigest;
    long errorCount;
    long timestampNextRetry;

    private FileSet(File localFile, FileLinkRecord remoteFile, FileDataRecord remoteData) {
      this.localFile = localFile;
      this.remoteFile = remoteFile;
      this.remoteData = remoteData;
      this.timestampTrackingStart = localFile.lastModified();
      this.trackingDataDigest = remoteData.getOrigDataDigest();
    }

    public String getError() {
      return error;
    }

    public File getLocalFile() {
      return localFile;
    }

    public FileLinkRecord getRemoteFile() {
      return remoteFile;
    }

    public FileDataRecord getRemoteData() {
      return remoteData;
    }

    public void setError(String errMsg) {
      error = errMsg;
    }
  }
}
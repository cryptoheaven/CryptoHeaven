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

package com.CH_cl.service.ops;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.actions.sys.SysANoop;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.event.*;
import com.CH_cl.service.engine.*;

import com.CH_co.cryptx.*;
import com.CH_co.io.*;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.file.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import java.io.*;
import java.security.*;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

/**
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class FileLobUp {

  private static final boolean DEBUG_CONSOLE = false;
  private static final String PROPERTY_NAME = "file-lob-up";

  private SingleTokenArbiter arbiter;
  private String arbiterKeySeal = "SEAL";
  private String arbiterKeyUpload = "UPLOAD";

  private File plainDataFile;
  private Long plainDataFileLength;
  private FileLinkRecord fileLink;
  private Long signingKeyId;

  private BADigestBlock origDataDigest;
  private BAAsyCipherBlock signedOrigDigest;
  private BADigestBlock encDataDigest;

  private BADigestBlock verifEncDataDigest;

  private BASymCipherBulk encOrigDataDigest;    // BADigestBlock symmetrically encrypted
  private BASymCipherBulk encSignedOrigDigest;  // BAAsyCipherBlock symmetrically encrypted
  private BASymCipherBulk encEncDataDigest;     // BADigestBlock symmetrically encrypted

  private File encDataFile;
  private Long encSize;
  private Object encFileMonitor;

  private InterruptibleInputStream interruptibleEncStream;
  private FileListener fileListener;
  private MsgListener msgListener;

  private boolean isSealed;
  private boolean isSigned;
  private boolean isEncryptExcempt;
  private boolean isUploaded;
  private boolean isUploadInProgress;
  private boolean isInterrupted;
  private Object interruptMonitor = new Object();

  private static Object stateMonitor = new Object();
  private static ArrayList stateLocalL = null;
  private static ArrayList stateDriveL = null;

  public FileLobUp(File plainLocalFile, FileLinkRecord link, Long signingKeyId, long startFromByte) {
    this.plainDataFile = plainLocalFile;
    this.fileLink = link;
    this.signingKeyId = signingKeyId;
    this.arbiter = new SingleTokenArbiter();
    this.encFileMonitor = new Object();
    addStateSession();
    triggerUploading(startFromByte);
  }

  private FileLobUp(String plainLocalFile, Long fileLinkId, Long fileId, byte[] linkSymKey, Long signingKeyId, String encFile, byte[] encDigest) {
    this.plainDataFile = new File(plainLocalFile);
    this.fileLink = new FileLinkRecord();
    this.fileLink.fileLinkId = fileLinkId;
    this.fileLink.fileId = fileId;
    this.fileLink.setSymmetricKey(new BASymmetricKey(linkSymKey));
    this.signingKeyId = signingKeyId;
    if (encFile != null) {
      File eFile = new File(encFile);
      if (eFile.exists() && eFile.canRead() && eFile.isFile()) {
        this.encDataFile = new File(encFile);
        this.encDataDigest = new BADigestBlock(encDigest);
      }
    }
    this.arbiter = new SingleTokenArbiter();
    this.encFileMonitor = new Object();
    triggerUploading(-1);
  }

  private void addStateSession() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLobUp.class, "addStateSession()");
    Object[] state = new Object[] { plainDataFile.getAbsolutePath(), fileLink.fileLinkId, fileLink.fileId, fileLink.getSymmetricKey().toByteArray(), signingKeyId, null, null };
    synchronized (stateMonitor) {
      if (stateLocalL == null) stateLocalL = new ArrayList();
      if (stateDriveL == null) stateDriveL = new ArrayList();
      stateLocalL.add(state);
      stateDriveL.add(state);
      saveAndStoreState();
    }
    if (trace != null) trace.exit(FileLobUp.class);
  }

  private Object[] getState() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLobUp.class, "getState()");
    Object[] state = null;
    synchronized (stateMonitor) {
      for (int i=0; i<stateLocalL.size(); i++) {
        Object[] st = (Object[]) stateLocalL.get(i);
        if (st[1].equals(fileLink.fileLinkId)) {
          state = st;
          break;
        }
      }
    }
    if (trace != null) trace.exit(FileLobUp.class);
    return state;
  }

  public static ArrayList getStateSessions() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLobUp.class, "getStateSessions()");
    ArrayList list = null;
    synchronized (stateMonitor) {
      if (stateLocalL != null)
        list = (ArrayList) stateLocalL.clone();
    }
    if (trace != null) trace.exit(FileLobUp.class, list);
    return list;
  }

  private void removeStateSession(boolean removeFromMemAndHD) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLobUp.class, "removeStateSession(boolean removeFromMemAndHD)");
    if (trace != null) trace.args(removeFromMemAndHD);
    synchronized (stateMonitor) {
      if (stateLocalL != null) {
        if (trace != null) trace.data(10, "FileLobUp.removeStateSession(): removing from memory list");
        for (int i=0; i<stateLocalL.size(); i++) {
          Object[] state = (Object[]) stateLocalL.get(i);
          if (state[1].equals(fileLink.fileLinkId)) {
            stateLocalL.remove(i);
            break;
          }
        }
        if (removeFromMemAndHD) {
          if (trace != null) trace.data(20, "FileLobUp.removeStateSession(): removing from stored properties list");
          for (int i=0; i<stateDriveL.size(); i++) {
            Object[] state = (Object[]) stateDriveL.get(i);
            if (state[1].equals(fileLink.fileLinkId)) {
              stateDriveL.remove(i);
              saveAndStoreState();
              break;
            }
          }
        }
      }
    }
    if (trace != null) trace.exit(FileLobUp.class);
  }

  public static void restoreState() {
    synchronized (stateMonitor) {
      String state = GlobalProperties.getProperty(PROPERTY_NAME);
      if (state != null && state.length() > 0) {
        try {
          stateLocalL = (ArrayList) ArrayUtils.strToObj(state);
          // Important to keep single copy of states because we are editing them too.
          // Important to keep them in seperate instances of lists because they will change independently.
          stateDriveL = new ArrayList(stateLocalL);
          for (int i=0; i<stateLocalL.size(); i++) {
            Object[] set = (Object[]) stateLocalL.get(i);
            new FileLobUp((String) set[0], (Long) set[1], (Long) set[2], (byte[]) set[3], (Long) set[4], (String) set[5], (byte[]) set[6]);
          }
        } catch (Throwable t) {
          if (DEBUG_CONSOLE) t.printStackTrace();
          GlobalProperties.setProperty(PROPERTY_NAME, "");
        }
      }
    }
  }

  private static void saveAndStoreState() {
    synchronized (stateMonitor) {
      saveState();
      storeState();
    }
  }

  public static void saveState() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLobUp.class, "saveState()");
    synchronized (stateMonitor) {
      if (trace != null) trace.data(10, "FileLobUp.saveState(): setting property");
      if (stateDriveL != null && stateDriveL.size() > 0)
        GlobalProperties.setProperty(PROPERTY_NAME, ArrayUtils.objToStr(stateDriveL));
      else
        GlobalProperties.setProperty(PROPERTY_NAME, "");
    }
    if (trace != null) trace.exit(FileLobUp.class);
  }

  private static void storeState() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLobUp.class, "saveState()");
    synchronized (stateMonitor) {
      if (trace != null) trace.data(20, "FileLobUp.saveState(): storing properties");
      GlobalProperties.store();
      if (trace != null) trace.data(30, "FileLobUp.saveState(): storing done");
    }
    if (trace != null) trace.exit(FileLobUp.class);
  }

  private void triggerEncryption() {
    if (!isSealed || !isSigned) {
      Thread th = new ThreadTraced(new Runnable() {
        public void run() {
          Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");
          Object token = new Object();
          if (arbiter.putToken(arbiterKeySeal, token)) {
            boolean isCompleted = false;
            while (!isCompleted && !isInterrupted) {
              try {
                if (isSigned)
                  isCompleted = doEncryption(ServerInterfaceLayer.lastSIL);
                else if (ServerInterfaceLayer.lastSIL.isLoggedIn())
                  isCompleted = doEncryption(ServerInterfaceLayer.lastSIL);
              } catch (Throwable t) {
                if (DEBUG_CONSOLE) t.printStackTrace();
                if (trace != null) trace.exception(getClass(), 100, t);
              } finally {
                if (isCompleted) {
                  arbiter.removeToken(arbiterKeySeal, token);
                } else {
                  // Any exception or failure should hit this delay.
                  if (trace != null) trace.data(200, "waiting to retry in triggerEncryption()");
                  try { Thread.sleep(3000); } catch (InterruptedException interX) { }
                  if (trace != null) trace.data(201, "woke up from waiting to retry in triggerEncryption()");
                }
              }
            }
          }
          if (trace != null) trace.exit(getClass());
        }
      }, "FileLobUp Sealer");
      th.setDaemon(true);
      th.start();
    }
  }

  private void triggerUploading(final long startFromByte) {
    if (!isUploaded && !isUploadInProgress) {
      Thread th = new ThreadTraced(new Runnable() {
        public void run() {
          Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");
          Object token = new Object();
          if (arbiter.putToken(arbiterKeyUpload, token)) {
            // limit number of running upload threads
            FileLobUpSynch.entry(3);
            FetchedDataCache cache = null;
            try {
              // Hook into cache to listen for any possible deletions so we can interrupt uploads.
              cache = ServerInterfaceLayer.lastSIL.getFetchedDataCache();
              fileListener = new FileListener();
              msgListener = new MsgListener();
              cache.addFileLinkRecordListener(fileListener);
              cache.addMsgLinkRecordListener(msgListener);
              isUploadInProgress = true;
              boolean isRetry = false;
              while (!isUploaded) {
                try {
                  if (DEBUG_CONSOLE) System.out.println("doUpload: trigger for "+plainDataFile);
                  if (trace != null) trace.data(10, "doUpload: trigger", plainDataFile);
                  // If retrying start from unknown byte count.
                  if (ServerInterfaceLayer.lastSIL.isLoggedIn())
                    isUploaded = doUpload(isRetry ? -1 : startFromByte);
                } catch (Throwable t) {
                  if (DEBUG_CONSOLE) t.printStackTrace();
                  if (trace != null) trace.exception(getClass(), 100, t);
                } finally {
                  if (isUploaded) {
                    if (DEBUG_CONSOLE) System.out.println("doUpload: completion in triggerUploading()");
                    if (trace != null) trace.data(110, "doUpload: trigger completed", plainDataFile);
                    isUploadInProgress = false;
                    arbiter.removeToken(arbiterKeyUpload, token);
                  } else {
                    if (DEBUG_CONSOLE) System.out.println("doUpload: trigger will retry for "+plainDataFile);
                    if (trace != null) trace.data(120, "doUpload: trigger will retry", plainDataFile);
                    isRetry = true;
                    // Any exception or failure should hit this delay.
                    if (trace != null) trace.data(200, "waiting to retry in triggerUploading()", plainDataFile);
                    try { Thread.sleep(3000); } catch (InterruptedException interX) { }
                    if (trace != null) trace.data(201, "woke up from waiting to retry in triggerUploading()", plainDataFile);
                  }
                }
              }
            } finally {
              // mark thread exit
              FileLobUpSynch.exit();
              // remove cache listeners after upload exits
              try { cache.removeFileLinkRecordListener(fileListener); } catch (Throwable t) { }
              try { cache.removeMsgLinkRecordListener(msgListener); } catch (Throwable t) { }
            }
          }
          if (trace != null) trace.exit(getClass());
        }
      }, "FileLobUp Uploader");
      th.setDaemon(true);
      th.start();
    }
  }

  private InputStream getEncStream() throws FileNotFoundException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLobUp.class, "getEncStream()");
    // wait until the stream becomes available
    synchronized (encFileMonitor) {
      long timeStart = System.currentTimeMillis();
      if (trace != null) trace.data(10, "FileLobUp.getEncStream(): checking for existance of encDataFile");
      while (!isInterrupted && (encDataFile == null || !encDataFile.exists())) {
        if (trace != null) trace.data(20, "FileLobUp.getEncStream(): encDataFile is not available, will wait");
        try { encFileMonitor.wait(100); } catch (InterruptedException e) { }
        if (trace != null) trace.data(30, "FileLobUp.getEncStream(): woke up from wait on creation of encDataFile");
        if (System.currentTimeMillis() - timeStart > 30000) {
          if (trace != null) trace.data(40, "FileLobUp.getEncStream(): waited long enough, breaking out");
          break;
        }
      }
    }
    if (trace != null) trace.data(50, "FileLobUp.getEncStream(): encDataFile is", encDataFile);
    if (trace != null) trace.data(51, "FileLobUp.getEncStream(): encDataFile.exists() returns", encDataFile != null ? ""+encDataFile.exists() : "null");
    InputStream in = new FileAppendingInputStream(encDataFile);
    if (trace != null) trace.exit(FileLobUp.class, in);
    return in;
  }

  /**
   * Synchronized block that takes care of "sealing" and "signing"
   * Synchronized because two different threads may call it...
   * @param SIL
   * @return
   * @throws FileNotFoundException
   */
  private synchronized boolean doEncryption(ServerInterfaceLayer SIL) throws FileNotFoundException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLobUp.class, "doEncryption(ServerInterfaceLayer SIL)");
    if (!isEncryptExcempt && (!isSealed || !isSigned)) {
      if (!SIL.getFetchedDataCache().getUserRecord().pubKeyId.equals(signingKeyId))
        isEncryptExcempt = true;
      else {
        if (!isSealed && !isInterrupted) {
          if (encDataFile != null) {
            // we restored attributes from properties
            if (DEBUG_CONSOLE) System.out.println("doEncryption: SEAL SKIPPED - attributes already exist "+plainDataFile+" "+encDataFile.getAbsolutePath());
            if (trace != null) trace.data(10, "doEncryption: SEAL SKIPPED - attributes already exist", plainDataFile, encDataFile.getAbsolutePath());
          } else {
            if (DEBUG_CONSOLE) System.out.println("doEncryption: about to seal "+plainDataFile);
            if (trace != null) trace.data(10, "doEncryption: about to seal", plainDataFile);
            FetchedDataCache cache = SIL.getFetchedDataCache();
            KeyRecord key = cache.getKeyRecord(signingKeyId);
            seal(key, fileLink.getSymmetricKey(), null);
            if (DEBUG_CONSOLE) System.out.println("doEncryption: return from seal "+plainDataFile);
            if (trace != null) trace.data(20, "doEncryption: returned from seal", plainDataFile);
            Object[] state = getState();
            if (DEBUG_CONSOLE) System.out.println("doEncryption: state plainDataFile     "+state[0]);
            if (DEBUG_CONSOLE) System.out.println("doEncryption: state fileLinkId        "+state[1]);
            if (DEBUG_CONSOLE) System.out.println("doEncryption: state fileId            "+state[2]);
            // don't expose symKey
            //if (DEBUG_CONSOLE) System.out.println("doEncryption: state linkSymKey        "+Misc.objToStr(state[3]));
            if (DEBUG_CONSOLE) System.out.println("doEncryption: state signingKeyId      "+state[4]);
            if (DEBUG_CONSOLE) System.out.println("doEncryption: state OLD encFile       "+state[5]);
            if (DEBUG_CONSOLE) System.out.println("doEncryption: state OLD encDataDigest "+Misc.objToStr(state[6]));
            state[5] = encDataFile.getAbsolutePath();
            state[6] = encDataDigest.toByteArray();
            if (DEBUG_CONSOLE) System.out.println("doEncryption: state NEW encFile       "+state[5]);
            if (DEBUG_CONSOLE) System.out.println("doEncryption: state NEW encDataDigest "+Misc.objToStr(state[6]));
            saveAndStoreState();
          }
          isSealed = true;
        }
        if (!isSigned && !isInterrupted)
          isSigned = doUpdateSignatures(SIL);
      }
    }
    boolean isSuccess = isEncryptExcempt || (isSealed && isSigned);
    if (trace != null) trace.exit(FileLobUp.class, isSuccess);
    return isSuccess;
  }

  private boolean doUpdateSignatures(ServerInterfaceLayer SIL) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLobUp.class, "doUpdateSignatures()");
    if (!isSigned && !isInterrupted) {
      MessageAction msgAction = new MessageAction(CommandCodes.FILE_Q_UPDATE_DIGESTS, new Obj_List_Co(new Object[] { fileLink.fileLinkId, fileLink.fileId, encSize, encOrigDataDigest.toByteArray(), encSignedOrigDigest.toByteArray(), encEncDataDigest.toByteArray() }));
      if (trace != null) trace.data(10, "submitting request to update signatures", plainDataFile);
      if (DEBUG_CONSOLE) System.out.println("doUpdateSignatures(): attempt for "+plainDataFile);
      ClientMessageAction replyMsgAction = SIL.submitAndFetchReply(msgAction, 60000, 3);
      if (trace != null) trace.data(20, "returned from submitting request to update signatures", plainDataFile);
      DefaultReplyRunner.nonThreadedRun(SIL, replyMsgAction, true);
      if (replyMsgAction != null && replyMsgAction.getActionCode() == CommandCodes.FILE_A_GET_FILES_DATA_ATTRIBUTES) {
        if (DEBUG_CONSOLE) System.out.println("doUpdateSignatures(): SIGNED for "+plainDataFile);
        if (trace != null) trace.data(30, "submitting request to update signatures completed ok", plainDataFile);
        isSigned = true;
      } else {
        if (DEBUG_CONSOLE) System.out.println("doUpdateSignatures(): FAILED for "+plainDataFile);
        if (trace != null) trace.data(40, "submitting request to update signatures FAILED", plainDataFile);
      }
    }
    if (trace != null) trace.exit(FileLobUp.class, isSigned);
    return isSigned;
  }

  private boolean doUpload(long startFromByte) throws FileNotFoundException, IOException, NoSuchAlgorithmException, DigestException, InvalidKeyException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLobUp.class, "doUpload(long startFromByte)");
    if (trace != null) trace.args(startFromByte);
    boolean isCompletedTransfer = false;
    boolean authorizationFailed = false;
    boolean notOriginator = false;
    ServerInterfaceLayer SIL = ServerInterfaceLayer.lastSIL;
    FetchedDataCache cache = SIL.getFetchedDataCache();
    if (!SIL.isLoggedIn())
      throw new IllegalStateException("Cannot doUpload because we are not logged in yet...");
    if (!cache.getUserRecord().pubKeyId.equals(signingKeyId)) {
      if (trace != null) trace.data(10, "doUpload: done - we are not file originator");
      notOriginator = true;
      removeStateSession(false);
    } else {
      // check the existing progress if unsure where to start
      if (startFromByte == -1) {
        if (trace != null) trace.data(20, "doUpload: checking progress", plainDataFile);
        if (DEBUG_CONSOLE) System.out.println("doUpload: checking progress "+plainDataFile+" fileLink="+fileLink.fileLinkId+" fileId="+fileLink.fileId);
        MessageAction msgAction = new MessageAction(CommandCodes.FILE_Q_GET_PROGRESS, new Obj_ID_Rq(fileLink.fileLinkId));
        ClientMessageAction replyMsgAction = SIL.submitAndFetchReply(msgAction, 60000, 3);
        if (trace != null) trace.data(21, "doUpload: checking progress returned", plainDataFile);
        DefaultReplyRunner.nonThreadedRun(SIL, replyMsgAction, true);
        if (replyMsgAction instanceof SysANoop) {
          if (trace != null) trace.data(22, "doUpload: checking progress came back");
          Obj_List_Co data = (Obj_List_Co) replyMsgAction.getMsgDataSet();
          if (DEBUG_CONSOLE) System.out.println("doUpload: replyMsgAction data "+data +" for "+plainDataFile+" fileLink="+fileLink.fileLinkId+" fileId="+fileLink.fileId);
          Boolean isSuccess = (Boolean) data.objs[0];
          if (trace != null) trace.data(23, "doUpload: check success data for fileLink="+fileLink.fileLinkId+" fileId="+fileLink.fileId, data);
          if (isSuccess.booleanValue()) {
            Long dataEncSize = (Long) data.objs[1];
            Long dataTotalLen = (Long) data.objs[2];
            Boolean hasOrigDataDigest = (Boolean) data.objs[3];
            Boolean hasSignedOrigDigest = (Boolean) data.objs[4];
            Boolean hasEncDataDigest = (Boolean) data.objs[5];
            if (dataEncSize != null && dataEncSize.longValue() == dataTotalLen.longValue()) {
              // all bytes already uploaded
              isCompletedTransfer = true;
              if (DEBUG_CONSOLE) System.out.println("doUpload: all bytes already uploaded "+plainDataFile);
              if (trace != null) trace.data(24, "doUpload: all bytes already uploaded", plainDataFile);
            } else {
              // some bytes need uploading
              isCompletedTransfer = false;
              startFromByte = dataTotalLen.longValue();
              if (DEBUG_CONSOLE) System.out.println("doUpload: upload to restart for "+plainDataFile+" at byte " + startFromByte);
              if (trace != null) trace.data(25, "doUpload: upload to restart for "+plainDataFile+" at byte", startFromByte);
            }
            // Regardless of data completion, if it is not signed then we'll need to restart to ensure integrity.
            if (dataEncSize.longValue() > 0 && hasOrigDataDigest.booleanValue() && hasSignedOrigDigest.booleanValue() && hasEncDataDigest.booleanValue()) {
              isSigned = true;
              if (DEBUG_CONSOLE) System.out.println("doUpload: file already signed "+plainDataFile);
              if (trace != null) trace.data(27, "doUpload: file already signed", plainDataFile);
            } else {
              isSigned = false;
              isCompletedTransfer = false;
              startFromByte = 0;
              if (DEBUG_CONSOLE) System.out.println("doUpload: file not signed, need to restart from ZERO "+plainDataFile);
              if (trace != null) trace.data(28, "doUpload: file not signed, need to restart from ZERO", plainDataFile);
            }
          } else {
            // not eligible to upload data or file deleted -- force completion
            if (trace != null) trace.data(30, "doUpload: progress uncertail - likely unauthorized or file was already deleted", plainDataFile);
            if (DEBUG_CONSOLE) System.out.println("doUpload: completion - likely unauthorized or file was already deleted");
            authorizationFailed = true;
            if (trace != null) trace.data(31, "doUpload: aborting un-authorized file", plainDataFile);
            ClientMessageAction reply = SIL.submitAndFetchReply(new MessageAction(CommandCodes.FILE_Q_UPLOAD_ABORT, new Obj_IDPair_Co(fileLink.fileLinkId, fileLink.fileId)));
            DefaultReplyRunner.nonThreadedRun(SIL, reply, true);
          }
        } else {
          if (trace != null) trace.data(35, "doUpload: progress check FAILED", plainDataFile, fileLink.fileLinkId, fileLink.fileId);
          if (DEBUG_CONSOLE) System.out.println("doUpload: progress check FAILED "+plainDataFile+" fileLinkId="+fileLink.fileLinkId+" fileId="+fileLink.fileId);
        }
      }
      if (!authorizationFailed && startFromByte >= 0) {
        // Must re-seal if file on server is not signed.. sealing will generate digests necessary for signing.
        if (!isSigned && encDataFile != null) {
          if (trace != null) trace.data(37, "doUpload: server does not have signatures, need to re-seal to generate signatures", plainDataFile, fileLink.fileId);
          if (DEBUG_CONSOLE) System.out.println("doUpload: server does not have signatures, need to re-seal to generate signatures "+plainDataFile+" fileId="+fileLink.fileId);
          encDataFile = null;
        }
        // If file on server is SIGNED and we are re-sealing, then we must check if server digest verifies with seal digest before any bytes are transferred
        if (isSigned && encDataFile == null) {
          if (trace != null) trace.data(38, "doUpload: finding existing signatures for file", plainDataFile, fileLink.fileId);
          if (DEBUG_CONSOLE) System.out.println("doUpload: finding existing signatures for file "+plainDataFile+" "+fileLink.fileId);
          ClientMessageAction replyMsgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.FILE_Q_GET_FILE_DATA_ATTRIBUTES, new Obj_ID_Rq(fileLink.fileId)));
          DefaultReplyRunner.nonThreadedRun(SIL, replyMsgAction, true);
          if (replyMsgAction != null && replyMsgAction.getMsgDataSet() instanceof File_GetAttr_Rp) {
            File_GetAttr_Rp set = (File_GetAttr_Rp) replyMsgAction.getMsgDataSet();
            SymmetricBulkCipher symCipher = new SymmetricBulkCipher(fileLink.getSymmetricKey());
            verifEncDataDigest = new BADigestBlock(symCipher.bulkDecrypt(set.fileDataRecords[0].getEncEncDataDigest()));
            if (trace != null) trace.data(39, "doUpload: seal verification will be needed before upload restarts", plainDataFile, fileLink.fileId, verifEncDataDigest);
            if (DEBUG_CONSOLE) System.out.println("doUpload: seal verification will be needed before upload restarts "+plainDataFile+" "+fileLink.fileId+" "+verifEncDataDigest);
          } else {
            if (trace != null) trace.data(39, "doUpload: fetching existing signatures FAILED - will have to start from ZERO", plainDataFile, fileLink.fileId, verifEncDataDigest);
            if (DEBUG_CONSOLE) System.out.println("doUpload: fetching existing signatures FAILED - will have to start from ZERO "+plainDataFile+" "+fileLink.fileId+" "+verifEncDataDigest);
            isSigned = false;
            startFromByte = 0;
          }
        }
        if (trace != null) trace.data(40, "doUpload: going into encryption", plainDataFile);
        triggerEncryption();
        if (trace != null) trace.data(50, "doUpload: getting encrypted data stream", plainDataFile);
        InputStream encStream = null;
        try {
          encStream = getEncStream();
          // any failed verification will reset 'isSigned' and we have to start from byte ZERO -- isSigned may have changed during seal()
          if (verifEncDataDigest != null && !isSigned)
            startFromByte = 0;
          interruptibleEncStream = new InterruptibleInputStream(encStream);
          DigestInputStream dEncStream = new DigestInputStream(interruptibleEncStream, new SHA256());
          if (trace != null) trace.data(51, "doUpload: got encrypted data stream for "+plainDataFile+" fileLinkId="+fileLink.fileLinkId+" fileId="+fileLink.fileId+" starting point is "+startFromByte);
          if (DEBUG_CONSOLE) System.out.println("doUpload: got encrypted data stream for "+plainDataFile+" fileLinkId="+fileLink.fileLinkId+" fileId="+fileLink.fileId+" starting point is "+startFromByte);
          if (startFromByte > 0) {
            long skipped = 0;
            byte[] buffer = new byte[4*1024];
            while (skipped < startFromByte) {
              skipped += dEncStream.read(buffer, 0, (int) Math.min(startFromByte - skipped, buffer.length));
            }
          }
          MessageAction msgAction = new MessageAction(CommandCodes.FILE_Q_UPLOAD_CONTENT, new File_Transfer_Co(fileLink.fileLinkId, fileLink.fileId, plainDataFileLength, startFromByte, encDataFile, dEncStream));
          if (trace != null) trace.data(60, "doUpload: submitting streaming upload request, isSealed="+isSealed);
          if (DEBUG_CONSOLE) System.out.println("doUpload: submitting streaming upload request, isSealed="+isSealed);
          ClientMessageAction replyMsgAction = SIL.submitAndFetchReply(msgAction);
          if (trace != null) trace.data(70, "doUpload: streaming upload request came back");
          // If this came back with an error due to CONGESTION then suppress error dialog..
          // CONGESTION is possible if two clients upload the same file!
          DefaultReplyRunner.nonThreadedRun(SIL, replyMsgAction, true);
          if (replyMsgAction != null && replyMsgAction.getActionCode() == CommandCodes.FILE_A_UPLOAD_COMPLETED) {
            if (DEBUG_CONSOLE) System.out.println("doUpload: upload finished for "+plainDataFile);
            if (trace != null) trace.data(80, "doUpload: upload completed ok", plainDataFile);
            isCompletedTransfer = true;
          } else {
            if (DEBUG_CONSOLE) System.out.println("doUpload: upload DID NOT finish for "+plainDataFile);
            if (trace != null) trace.data(81, "doUpload: upload DID NOT complete", plainDataFile);
          }
          // verify data integrity of what was originally sealed and what was transferred
          if (isCompletedTransfer) {
            BADigestBlock encDataDigestVerification = new BADigestBlock(dEncStream.getMessageDigest().digest());
            if (!encDataDigest.equals(encDataDigestVerification)) {
              if (DEBUG_CONSOLE) System.out.println("doUpload: integrity check FAILED "+plainDataFile);
              if (trace != null) trace.data(85, "doUpload: integrity check failed, will restart from ZERO", plainDataFile);
              // start from ZERO
              isCompletedTransfer = false;
              isSigned = false;
              isSealed = false;
              cleanupEncFile();
              ClientMessageAction reply = SIL.submitAndFetchReply(new MessageAction(CommandCodes.FILE_Q_UPLOAD_RESET, new Obj_IDPair_Co(fileLink.fileLinkId, fileLink.fileId)), 60000, 3);
              DefaultReplyRunner.nonThreadedRun(SIL, reply, true);
            } else {
              if (DEBUG_CONSOLE) System.out.println("doUpload: integrity check passed "+plainDataFile);
              if (trace != null) trace.data(86, "doUpload: integrity check passed", plainDataFile);
            }
          }
        } catch (Throwable t) {
          if (DEBUG_CONSOLE) t.printStackTrace();
          if (trace != null) trace.exception(FileLobUp.class, 88, t);
        } finally {
          // close the stream after we returned from upload -- this will make the temp file deletable and socket source stream terminated
          try { if (encStream != null) encStream.close(); } catch (Throwable t) { }
          try { if (interruptibleEncStream != null) interruptibleEncStream.close(); } catch (Throwable t) { }
        }
        if (!isCompletedTransfer && isInterrupted) {
          if (trace != null) trace.data(89, "doUpload: aborting interrupted upload", plainDataFile);
          ClientMessageAction reply = SIL.submitAndFetchReply(new MessageAction(CommandCodes.FILE_Q_UPLOAD_ABORT, new Obj_IDPair_Co(fileLink.fileLinkId, fileLink.fileId)));
          DefaultReplyRunner.nonThreadedRun(SIL, reply, true);
        }
      } // end more bytes need transferring
      // remove the completed upload from saved states
      if ((isCompletedTransfer && isSigned) || authorizationFailed || isInterrupted) {
        if (DEBUG_CONSOLE) System.out.println("doUpload: completion and cleanup or !authorized for "+plainDataFile);
        if (trace != null) trace.data(90, "doUpload: removing upload state session from properties", plainDataFile);
        removeStateSession(true);
        cleanupEncFile();
      }
    }
    boolean isDone = notOriginator || authorizationFailed || (isCompletedTransfer && isSigned) || isInterrupted;
    if (trace != null) trace.exit(FileLobUp.class, isDone);
    return isDone;
  }

  private void seal(KeyRecord signingKeyRecord, BASymmetricKey symmetricKey, ProgMonitorI progressMonitor) throws FileNotFoundException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLobUp.class, "seal()");

    int oldPriority = Thread.currentThread().getPriority();
    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

    File tempFile = null;
    FileOutputStream tempFileOut = null;

    try {

      DigestInputStream dFileIn = null;
      DigestOutputStream dFileOut = null;
      GZIPOutputStream gzipOut = null;

      plainDataFileLength = new Long(plainDataFile.length());
      if (trace != null) trace.data(10, "plainDataFileLength queried", plainDataFile, plainDataFileLength);
      if (DEBUG_CONSOLE) System.out.println("seal: sealing "+plainDataFile+" total length is "+plainDataFileLength);
      FileInputStream fileIn = new FileInputStream(plainDataFile);

      // progress interruptible stream
      InterruptibleInputStream interIn = new InterruptibleInputStream(fileIn);
      if (progressMonitor != null) progressMonitor.setInterrupt(interIn);

      dFileIn = new DigestInputStream(interIn, new SHA256());

      // create a temporary file for the encrypted data
      tempFile = File.createTempFile(FileDataRecord.TEMP_ENCRYPTED_FILE_PREFIX, null);
      if (trace != null) trace.data(20, "temp file crated", tempFile);
      tempFileOut = new FileOutputStream(tempFile);
      dFileOut = new DigestOutputStream(tempFileOut, new SHA256());
      BlockCipherOutputStream cipherOut = new BlockCipherOutputStream(dFileOut, symmetricKey);
      gzipOut = new GZIPOutputStream(cipherOut);

      if (progressMonitor != null) {
        progressMonitor.setCurrentStatus("Compressing and Encrypting File ...");
        progressMonitor.setFileNameSource(plainDataFile.getAbsolutePath());
        progressMonitor.setFileNameDestination(tempFile.getAbsolutePath());
        progressMonitor.setTransferSize(plainDataFileLength.longValue());
        progressMonitor.nextTask();
      }

      // Remember the newly created encrypted file before we start moding bytes into it.
      // If no integrity verification pending, assign to global to release waiting reader.
      if (verifEncDataDigest == null) {
        synchronized (encFileMonitor) {
          encDataFile = tempFile;
          encFileMonitor.notifyAll();
        }
      }

      if (trace != null) trace.data(22, "movingData");
      // move data from the digest input stream to GZIP file output stream
      FileUtils.moveDataEOF(dFileIn, gzipOut, progressMonitor);
      //FileUtils.moveData(new DataInputStream(dFileIn), gzipOut, plainDataFile.length(), progressMonitor);
      if (trace != null) trace.data(23, "movingData done");

      if (progressMonitor != null) {
        progressMonitor.setInterrupt(null);
        progressMonitor.doneTransfer();
      }

      dFileIn.close();
      interIn.close();
      fileIn.close();

      gzipOut.finish();
      gzipOut.flush();
      gzipOut.close();

      if (trace != null) trace.data(25, "GZIP completed of file len="+plainDataFile.length(), plainDataFile);
      if (trace != null) trace.data(26, "GZIP completed to file len="+tempFile.length(), tempFile);

      // create the original data digest
      origDataDigest = new BADigestBlock(dFileIn.getMessageDigest().digest());
      if (trace != null) trace.data(30, "origDataDigest", ArrayUtils.toString(origDataDigest.toByteArray()));

      // create the encrypted data digest
      encDataDigest = new BADigestBlock(dFileOut.getMessageDigest().digest());
      if (trace != null) trace.data(40, "encDataDigest", ArrayUtils.toString(encDataDigest.toByteArray()));

      // If integrity verification is pending, verify as soon as digest is available to release the waiting reader.
      if (verifEncDataDigest != null) {
        synchronized (encFileMonitor) {
          if (verifEncDataDigest != null && !verifEncDataDigest.equals(encDataDigest)) {
            if (trace != null) trace.data(41, "seal: verification failed which means we'll have to start uploading from ZERO and update digests!");
            if (DEBUG_CONSOLE) System.out.println("seal: verification failed which means we'll have to start uploading from ZERO and update digests!");
            // verification failed which means we'll have to start uploading from ZERO and update digests!
            isSigned = false;
          } else {
            if (trace != null) trace.data(42, "seal: verification passed");
            if (DEBUG_CONSOLE) System.out.println("seal: verification passed");
          }
          encDataFile = tempFile;
          encFileMonitor.notifyAll();
        }
      }

      // set the encrypted versions of digests and signed digests
      SymmetricBulkCipher symCipher = new SymmetricBulkCipher(symmetricKey);
      encOrigDataDigest = symCipher.bulkEncrypt(origDataDigest);
      encEncDataDigest = symCipher.bulkEncrypt(encDataDigest);

      // sign the original data digest
      if (progressMonitor != null) progressMonitor.setCurrentStatus("Signing Encrypted File ...");
      // random filling will cause even the same 'origDataDigest' to look different in 'signedOrigDigest' form!
      signedOrigDigest = new BAAsyPlainBlock(origDataDigest).signBlock(signingKeyRecord.getPrivateKey());
      if (trace != null) trace.data(50, "signedOrigDigest", ArrayUtils.toString(signedOrigDigest.toByteArray()));
      encSignedOrigDigest = symCipher.bulkEncrypt(signedOrigDigest);
      if (progressMonitor != null) progressMonitor.setCurrentStatus("Signing Encrypted File ... signed.");

      signingKeyId = signingKeyRecord.keyId;
      encSize = new Long(tempFile.length());

      if (DEBUG_CONSOLE) System.out.println("seal: sealing concluded "+plainDataFile+" total encSize="+encSize+" "+encDataFile.getAbsolutePath());
      if (trace != null) trace.data(90, "sealing concluded with encSize=", plainDataFile, encSize, encDataFile);
    } catch (Throwable t) {
      if (DEBUG_CONSOLE) t.printStackTrace();
      if (trace != null) trace.exception(FileLobUp.class, 100, t);

      // update the job status to KILLED
      if (progressMonitor != null) progressMonitor.jobKilled();

      if (progressMonitor != null && !progressMonitor.isCancelled()) {
        String inFileName = "unknown";
        if (plainDataFile != null)
          inFileName = plainDataFile.getAbsolutePath();

        String outFileName = "unknown";
        if (tempFile != null)
          outFileName = tempFile.getAbsolutePath();

        String msg = "Exception occurred while encrypting the file " + inFileName + "  The destination file " + outFileName + " was not completely written.  This error is not recoverable, the output file will be erased.  Please check destination folder for sufficient free space and write access permissions.  Exception message is: " + t.getMessage();
        String title = "Error Uploading File";
        NotificationCenter.show(NotificationCenter.ERROR_MESSAGE, title, msg);
      }

      // clean up temporary file
      try {
        if (tempFileOut != null)
          tempFileOut.close();
      } catch (Throwable th) { }
      try {
        if (tempFile != null)
          CleanupAgent.wipeOrDelete(tempFile);
      } catch (Throwable th) { }

      if (t instanceof FileNotFoundException)
        interrupt();

      throw new IllegalStateException(t.getMessage());
    } finally {
      Thread.currentThread().setPriority(oldPriority);
    }

    if (trace != null) trace.exit(FileLobUp.class);
  }

  public void cleanupEncFile() {
    if (encDataFile != null) {
      try { CleanupAgent.wipeOrDelete(encDataFile); } catch (Throwable t) { }
      encDataFile = null;
    }
  }

  private class FileListener implements FileLinkRecordListener {
    public void fileLinkRecordUpdated(FileLinkRecordEvent e) {
      if (!isInterrupted) {
        FileLinkRecord[] links = e.getFileLinkRecords();
        if (links != null && links.length > 0) {
          boolean anyMine = false;
          for (int i=0; i<links.length; i++) {
            FileLinkRecord link = links[i];
            // if updated link is for the same file object as we are working on...
            if (link.fileId.equals(fileLink.fileId)) {
              anyMine = true;
              break;
            }
          }
          if (anyMine)
            checkFileAccessForInterrupt(ServerInterfaceLayer.lastSIL);
        }
      }
    }
  }

  private class MsgListener implements MsgLinkRecordListener {
    public void msgLinkRecordUpdated(MsgLinkRecordEvent e) {
      if (!isInterrupted) {
        MsgLinkRecord[] links = e.getMsgLinkRecords();
        if (links != null && links.length > 0) {
          ServerInterfaceLayer SIL = ServerInterfaceLayer.lastSIL;
          FetchedDataCache cache = SIL.getFetchedDataCache();
          boolean anyMine = false;
          for (int i=0; i<links.length; i++) {
            MsgLinkRecord link = links[i];
            MsgDataRecord data = cache.getMsgDataRecord(link.msgId);
            if (data != null && data.attachedFiles.shortValue() > 0) {
              FileLinkRecord[] attachedFiles = cache.getFileLinkRecordsOwnerAndType(link.msgId, new Short(Record.RECORD_TYPE_MESSAGE));
              if (attachedFiles != null) {
                for (int j=0; j<attachedFiles.length; j++) {
                  if (attachedFiles[j].fileId.equals(fileLink.fileId)) {
                    anyMine = true;
                    break;
                  }
                }
                if (anyMine)
                  break;
              }
            }
          }
          // if updated link is for a MSG that contains the same file object as we are working on...
          if (anyMine)
            checkFileAccessForInterrupt(SIL);
        }
      }
    }
  }

  /**
   * Checks with the engine if we still have UPLOAD privilege to it,
   * if not then interrupts the UPLOAD.
   */
  private void checkFileAccessForInterrupt(final ServerInterfaceLayer SIL) {
    Thread th = new ThreadTraced(new Runnable() {
      public void run() {
        synchronized (interruptMonitor) {
          if (!isInterrupted) {
            ClientMessageAction replyMsgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.FILE_Q_GET_PROGRESS, new Obj_ID_Rq(fileLink.fileLinkId)));
            DefaultReplyRunner.nonThreadedRun(SIL, replyMsgAction, true);
            if (replyMsgAction instanceof SysANoop) {
              Obj_List_Co set = (Obj_List_Co) replyMsgAction.getMsgDataSet();
              Boolean isSuccess = (Boolean) set.objs[0];
              if (!isSuccess.booleanValue())
                interrupt();
            }
          }
        }
      }
    }, "FileAccessInterruptChecker");
    th.setDaemon(true);
    th.start();
  }

  private void interrupt() {
    isInterrupted = true;
    try { interruptibleEncStream.interrupt(); } catch (Throwable t) { }
    try { interruptibleEncStream.close(); } catch (Throwable t) { }
  }

  private class FileAppendingInputStream extends FileInputStream {
    public FileAppendingInputStream(File file) throws FileNotFoundException {
      super(file);
    }
    public int read() throws IOException {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileAppendingInputStream.class, "read()");
      int b = -1;
      while (true) {
        b = super.read();
        if (b == -1 && !isSealed) {
          if (trace != null) trace.data(100, "waiting for data", "FileAppendingInputStream.read()");
          try { Thread.sleep(10); } catch (InterruptedException e) { }
          if (trace != null) trace.data(101, "woke up from waiting for data - going into retry", "FileAppendingInputStream.read()");
        } else {
          break;
        }
      }
      if (trace != null) trace.exit(FileAppendingInputStream.class, b);
      return b;
    }
    public int read(byte[] b) throws IOException {
      return read(b, 0, b.length);
    }
    public int read(byte[] b, int off, int len) throws IOException {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileAppendingInputStream.class, "read(byte[] b, int off, int len)");
      int count = -1;
      while (true) {
        count = super.read(b, off, len);
        if (count == -1 && !isSealed) {
          if (trace != null) trace.data(100, "waiting for data", "FileAppendingInputStream.read(byte[] b, int off, int len)");
          try { Thread.sleep(10); } catch (InterruptedException e) { }
          if (trace != null) trace.data(101, "woke up from waiting for data - going into retry", "FileAppendingInputStream.read(byte[] b, int off, int len)");
        } else if (count == 0) {
          // not EOF and no bytes?? wait a little
          if (trace != null) trace.data(200, "waiting for data - not EOF and no bytes", "FileAppendingInputStream.read(byte[] b, int off, int len)");
          try { Thread.sleep(1); } catch (InterruptedException e) { }
          if (trace != null) trace.data(201, "woke up from waiting for data - not EOF and no bytes - going into retry", "FileAppendingInputStream.read(byte[] b, int off, int len)");
        } else {
          break;
        }
      }
      if (trace != null) trace.exit(FileAppendingInputStream.class, count);
      return count;
    }
  }
}
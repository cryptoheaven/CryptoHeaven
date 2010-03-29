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

import ch.cl.CryptoHeaven;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.actions.sys.*;
import com.CH_cl.service.engine.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.msg.dataSets.sys.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import java.io.*;
import java.net.*;
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
 * <b>$Revision: 1.12 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class AutoUpdater extends ThreadTraced {

  private static int TINY_FILE_SIZE = 2048;
  private static int MAX_HEADER_SIZE = 1024;

  private static int DEFAULT_BATCH_SIZE = 1024;
  private int BATCH_SIZE = DEFAULT_BATCH_SIZE;

  private static String FILENAME__LICENSE_TXT = "License.txt";
  private static String FILENAME__ONEJAR_VERSION = ".version";

  private static Byte BOUNDRY_EOF = new Byte((byte) 0);
  private static Byte BOUNDRY_HEADER = new Byte((byte) 1);
  private static Byte BOUNDRY_DATA = new Byte((byte) 2);
  private static Byte BOUNDRY_FREE = new Byte((byte) 3);

  private ServerInterfaceLayer SIL;
  private AutoUpdateRecord[] updateRecs;

  private static long lastRunStamp = 0;
  private static boolean isRunning = false;

  /** Creates new AutoUpdater */
  public AutoUpdater(ServerInterfaceLayer SIL, AutoUpdateRecord[] updateRecs) {
    super("Auto Updater");
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "AutoUpdater()");
    if (trace != null) trace.args(SIL, updateRecs);
    this.SIL = SIL;
    this.updateRecs = updateRecs;
    setDaemon(true);
    if (trace != null) trace.exit(AutoUpdater.class);
  }

  public void runTraced() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "AutoUpdater.runTraced()");
    if (!isRunning && !Misc.isRunningFromApplet() && !MiscGui.isAllGUIsuppressed()) {
      isRunning = true;
      lastRunStamp = System.currentTimeMillis();
      try {
        // Filter out updates that we should not apply based on non-build sensitive and same file size
        if (updateRecs != null && updateRecs.length > 0) {
          Vector updateRecsV = new Vector();
          File mainDir = null;
          for (int i=0; i<updateRecs.length; i++) {
            AutoUpdateRecord updateRec = (AutoUpdateRecord) updateRecs[i];
            boolean apply = true;
            // See if the main jar is available and if we should rename it locally...
            if (updateRec.newFile.equals(URLs.FILENAME__MAIN_JAR)) {
              String mainJarName = getMainJarName();
              updateRec.newFile = mainJarName;
              if (updateRec.oldFile != null && updateRec.oldFile.equals(URLs.FILENAME__MAIN_JAR)) {
                updateRec.oldFile = mainJarName;
              }
            } else {
              // See if old file name is specified, if it is, only update if the file exists
              if (updateRec.oldFile != null) {
                if (mainDir == null) mainDir = getMainDir();
                File file = new File(mainDir, updateRec.oldFile);
                if (!file.exists()) {
                  apply = false;
                }
              }
            }
            if (apply) {
              // For unspecified build number, check file sizes, if the same then do not update.
              // Also check if build numbers are the same, in which case it could be the main JAR being updated.
              if (updateRec.build == null || updateRec.build.shortValue() == GlobalProperties.PROGRAM_BUILD_NUMBER) {
                if (mainDir == null) mainDir = getMainDir();
                File file = new File(mainDir, updateRec.newFile);
                if (file.exists() && file.length() == updateRec.size.intValue()) {
                  apply = false;
                }
              }
            }
            if (apply) {
              updateRecsV.addElement(updateRec);
            }
          }
          updateRecs = (AutoUpdateRecord[]) ArrayUtils.toArray(updateRecsV, AutoUpdateRecord.class);
        }

        if (trace != null) trace.data(10, "update records after filtering what to apply", updateRecs);

        // Write update properties file
        if (updateRecs != null && updateRecs.length > 0) {
          writeUpdateProperties(updateRecs);
        }
        // Get update files
        for (int i=0; updateRecs!=null && i<updateRecs.length; i++) {
          AutoUpdateRecord updateRec = updateRecs[i];
          if (trace != null) trace.data(20, "Processing update record " + updateRec);
          File updateFile = makeOrGetUpdateFile(updateRec);
          if (updateFile != null && isDataInProgress(updateFile)) {
            updateFile = fillFileWithData(updateFile, updateRec);
          }
        }
        UserRecord myUser = SIL.getFetchedDataCache().getUserRecord();
        if (myUser.flags != null && !Misc.isBitSet(myUser.flags, UserRecord.FLAG_DISABLE_AUTO_UPDATES)) {
          // Request ApplyUpdates class
          if (updateRecs != null && updateRecs.length > 0) {
            writeApplyUpdatesClass();
          }
          // Apply update properties file
          if (updateRecs != null && updateRecs.length > 0) {
            setUpdateHook();
          }
        }
      } catch (Throwable t) {
        if (trace != null) trace.exception(AutoUpdater.class, 100, t);
      }
      isRunning = false;
    }
    if (trace != null) trace.data(200, "AutoUpdater done");
    if (trace != null) trace.exit(AutoUpdater.class);
  }

  public static boolean isRunningFromJar() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "isRunningFromJar()");
    boolean fromJar = isRunningFromJar(FILENAME__LICENSE_TXT) || isRunningFromJar(FILENAME__ONEJAR_VERSION);
    if (trace != null) trace.exit(AutoUpdater.class, fromJar);
    return fromJar;
  }

  private static boolean isRunningFromJar(String resourceName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "isRunningFromJar(String resourceName)");
    if (trace != null) trace.args(resourceName);
    URL location = getResourceURL(resourceName);
    if (trace != null) trace.data(10, location);
    String path = location.getPath();
    if (trace != null) trace.data(20, path);
    boolean fromJar = path.indexOf(".jar!/") >= 0;
    if (trace != null) trace.exit(AutoUpdater.class, fromJar);
    return fromJar;
  }

  private static String getMainJarName() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "getMainJarName()");
    String jarName = getMainJarName(FILENAME__LICENSE_TXT);
    if (jarName == null)
      jarName = getMainJarName(FILENAME__ONEJAR_VERSION);
    if (trace != null) trace.exit(AutoUpdater.class, jarName);
    return jarName;
  }

  private static String getMainJarName(String resourceName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "getMainJarName(String resourceName)");
    if (trace != null) trace.args(resourceName);
    String jarName = null;
    URL location = getResourceURL(resourceName);
    if (trace != null) trace.data(10, location);
    String path = location.getPath();
    path = URLDecoder.decode(path);
    if (trace != null) trace.data(20, path);
    boolean fromJar = path.indexOf(".jar!/") >= 0;
    if (fromJar) {
      int jarEnd = path.indexOf(".jar!/");
      String pathJar = path.substring(0, jarEnd + ".jar".length());
      if (trace != null) trace.data(30, pathJar);
      int jarStart = pathJar.lastIndexOf("/");
      jarName = pathJar.substring(jarStart + "/".length());
      if (trace != null) trace.data(40, jarName);
    }
    if (trace != null) trace.exit(AutoUpdater.class, jarName);
    return jarName;
  }

  private boolean isDataComplete(File updateFile) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "isDataComplete(File updateFile)");
    if (trace != null) trace.args(updateFile);
    boolean rc = updateFile.getName().endsWith(".updated");
    if (trace != null) trace.exit(AutoUpdater.class, rc);
    return rc;
  }

  private boolean isDataInProgress(File updateFile) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "isDataInProgress(File updateFile)");
    if (trace != null) trace.args(updateFile);
    boolean rc = updateFile.getName().endsWith(".updating");
    if (trace != null) trace.exit(AutoUpdater.class, rc);
    return rc;
  }

  private File makeOrGetUpdateFile(AutoUpdateRecord updateRec) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "makeOrGetUpdateFile(AutoUpdateRecord updateRec)");
    if (trace != null) trace.args(updateRec);
    File file = makeOrGetUpdateFile(updateRec.id, updateRec.size);
    if (trace != null) trace.exit(AutoUpdater.class, file);
    return file;
  }

  private LinkedList readUpdateStruct(File updateFile) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "readUpdateStruct(File updateFile)");
    if (trace != null) trace.args(updateFile);
    LinkedList updateStruct = null;
    FileInputStream fIn = null;
    DataInputStream dIn = null;
    try {
      fIn = new FileInputStream(updateFile);
      dIn = new DataInputStream(fIn);
      int fileSize = dIn.readInt();
      if (updateFile.length() == fileSize) {
        int updateStructSize = dIn.readInt();
        if (updateStructSize <= fileSize-2) {
          byte[] header = new byte[updateStructSize];
          int headerBytesRead = 0;
          while (headerBytesRead < updateStructSize) {
            headerBytesRead += dIn.read(header, headerBytesRead, header.length - headerBytesRead);
          }
          if (trace != null) trace.data(50, "headerBytesRead", headerBytesRead);
          ByteArrayInputStream bIn = new ByteArrayInputStream(header);
          ObjectInputStream oIn = new ObjectInputStream(bIn);
          updateStruct = (LinkedList) oIn.readObject();
        }
      }
    } catch (Exception t) {
      if (trace != null) trace.exception(AutoUpdater.class, 100, t);
    } finally {
      try { if (dIn != null) dIn.close(); } catch (Throwable t) { }
      try { if (fIn != null) fIn.close(); } catch (Throwable t) { }
    }
    if (trace != null) trace.exit(AutoUpdater.class, updateStruct);
    return updateStruct;
  }

  /**
   * Create an initial 'updateStruct' for the update file to be fetched.
   */
  private LinkedList makeUpdateStruct(AutoUpdateRecord updateRec) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "makeUpdateStruct(AutoUpdateRecord updateRec)");
    if (trace != null) trace.args(updateRec);
    LinkedList updateStruct = new LinkedList();
    // header part
    // boundry byte | boundry type 0=eof, 1=header, 1=data, 2=free space
    if (updateRec.size.intValue() > TINY_FILE_SIZE) {
      Object[] header = new Object[] { new Integer(0), BOUNDRY_HEADER };
      updateStruct.addLast(header);
      Object[] freeSpace = new Object[] { new Integer(MAX_HEADER_SIZE), BOUNDRY_FREE};
      updateStruct.addLast(freeSpace);
      Object[] eof = new Object[] { updateRec.size, BOUNDRY_EOF };
      updateStruct.addLast(eof);
    } else {
      Object[] freeSpace = new Object[] { new Integer(0), BOUNDRY_FREE };
      updateStruct.addLast(freeSpace);
      Object[] eof = new Object[] { updateRec.size, BOUNDRY_EOF };
      updateStruct.addLast(eof);
    }
    if (trace != null) trace.exit(AutoUpdater.class, updateStruct);
    return updateStruct;
  }

  /**
   * Writes the header to the file to make the current 'updateStruct' persistent.
   */
  private void writeHeader(RandomAccessFile rndAccFile, int size, LinkedList updateStruct) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "writeHeader(RandomAccessFile rndAccFile, int size, LinkedList updateStruct)");
    if (trace != null) trace.args(rndAccFile);
    if (trace != null) trace.args(size);
    if (trace != null) trace.args(updateStruct);
    rndAccFile.seek(0);
    rndAccFile.writeInt(size);
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
    objectOut.writeObject(updateStruct);
    objectOut.flush();
    byte[] bytes = byteOut.toByteArray();
    if (bytes.length + 4 > MAX_HEADER_SIZE) {
      throw new IllegalStateException("Header too large");
    }
    rndAccFile.writeInt(bytes.length);
    rndAccFile.write(bytes);
    if (trace != null) trace.exit(AutoUpdater.class);
  }

  /**
   * Writes the data chunk to the file and update 'updateStruct'
   */
  private void writeDataChunk(RandomAccessFile rndAccFile, int startPosition, byte[] bytes, LinkedList updateStruct) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "writeDataChunk(RandomAccessFile rndAccFile, int startPosition, byte[] bytes, LinkedList updateStruct)");
    if (trace != null) trace.args(rndAccFile);
    if (trace != null) trace.args(startPosition);
    if (trace != null) trace.args(bytes, updateStruct);
    rndAccFile.seek(startPosition);
    rndAccFile.write(bytes);
    // update the 'updateStruct'
    // look for start boundry
    ListIterator iter = updateStruct.listIterator();
    while (iter.hasNext()) {
      Object[] boundryObjs = (Object[]) iter.next();
      int boundry = ((Integer) boundryObjs[0]).intValue();
      Byte type = (Byte) boundryObjs[1];
      if (boundry == startPosition) {
        boundryObjs[0] = new Integer(startPosition);
        boundryObjs[1] = BOUNDRY_DATA;
        // Create a new boundry at end of chunk
        Object[] boundryNew = new Object[] { new Integer(startPosition + bytes.length), BOUNDRY_FREE };
        iter.add(boundryNew);
        break;
      }
    }
    compressUpdateStruct(updateStruct);
    if (trace != null) trace.exit(AutoUpdater.class, updateStruct);
  }

  private void compressUpdateStruct(LinkedList updateStruct) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "compressUpdateStruct(LinkedList updateStruct)");
    if (trace != null) trace.args(updateStruct);
    compressUpdateStructRecur(updateStruct);
    if (trace != null) trace.exit(AutoUpdater.class, updateStruct);
  }
  private void compressUpdateStructRecur(LinkedList updateStruct) {
    ListIterator iter = updateStruct.listIterator();
    Object[] boundrySet = null;
    Object[] boundrySetPrev = null;
    while (iter.hasNext()) {
      boundrySetPrev = boundrySet;
      Integer boundryPrev = boundrySetPrev != null ? (Integer) boundrySetPrev[0] : null;
      Byte typePrev = boundrySetPrev != null ? (Byte) boundrySetPrev[1] : null;

      boundrySet = (Object[]) iter.next();
      Integer boundry = (Integer) boundrySet[0];
      Byte type = (Byte) boundrySet[1];

      // compress same types
      if (type.equals(typePrev)) {
        iter.remove();
        // start over
        compressUpdateStructRecur(updateStruct);
        break;
      } else if (boundry != null && boundryPrev != null && boundry.compareTo(boundryPrev) <= 0) {
        iter.previous();
        iter.remove();
        // start over
        compressUpdateStructRecur(updateStruct);
        break;
      }
    }
  }

  /**
   * Make a single request for some file data bytes and write them to file updating the 'updateStruct'.
   * @return true if some data was requested, false if no more bytes are available, null if operation did not return any data (server is not serving updates right now
   */
  private Boolean requestBytes(boolean skipHeader, AutoUpdateRecord updateRec, LinkedList updateStruct, RandomAccessFile rndAccFile) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "requestBytes(boolean skipHeader, AutoUpdateRecord updateRec, LinkedList updateStruct, RandomAccessFile rndAccFile)");
    if (trace != null) trace.args(skipHeader);
    if (trace != null) trace.args(updateRec);
    if (trace != null) trace.args(updateStruct);
    if (trace != null) trace.args(rndAccFile);

    Boolean rc = null;

    // determine the first unfetched byte
    int unfetchedBoundry = -1;
    int batchSize = -1;
    Iterator iter = updateStruct.iterator();
    boolean lastFreeSpace = false;
    boolean isHeader = false;
    while (iter.hasNext()) {
      Object[] boundryObjs = (Object[]) iter.next();
      int boundry = ((Integer) boundryObjs[0]).intValue();
      Byte type = (Byte) boundryObjs[1];
      if (!lastFreeSpace) {
        if (type.equals(BOUNDRY_FREE) || (!skipHeader && type.equals(BOUNDRY_HEADER))) {
          if (type.equals(BOUNDRY_HEADER))
            isHeader = true;
          unfetchedBoundry = boundry;
          lastFreeSpace = true;
        }
      } else {
        if (!isHeader) {
          batchSize = boundry - unfetchedBoundry;
          if (batchSize > BATCH_SIZE)
            batchSize = BATCH_SIZE;
        } else {
          batchSize = MAX_HEADER_SIZE;
        }
        break;
      }
    }

    if (unfetchedBoundry >= 0 && batchSize > 0) {
      Sys_AutoUpdate_Co requestSet = new Sys_AutoUpdate_Co();
      requestSet.dataSet = new Obj_List_Co(new Object[] { updateRec.id, new Integer(unfetchedBoundry), new Integer(batchSize) });
      MessageAction msgAction = new MessageAction(CommandCodes.SYS_Q_GET_AUTO_UPDATE, requestSet);
      ClientMessageAction reply = SIL.submitAndFetchReply(msgAction, 15*60000);
      if (reply instanceof SysAGetAutoUpdate) {
        SysAGetAutoUpdate update = (SysAGetAutoUpdate) reply;
        Sys_AutoUpdate_Co updateSet = (Sys_AutoUpdate_Co) update.getMsgDataSet();
        Object[] set = updateSet.dataSet.objs;
        Long id = (Long) set[0];
        if (id.equals(updateRec.id)) {
          int startByte = ((Integer) set[1]).intValue();
          byte[] compressedDataBytes = (byte[]) set[3];
          byte[] dataBytes = Misc.decompressBytes(compressedDataBytes);
          writeDataChunk(rndAccFile, startByte, dataBytes, updateStruct);
          rc = Boolean.TRUE;
          if (trace != null) trace.data(50, "data chunk written, unfetched boundry = ", unfetchedBoundry);
          if (trace != null) trace.data(51, "data lengths: compressed / uncompressed / ratio", "" + compressedDataBytes.length + " / " + dataBytes.length + " / " + (((float) compressedDataBytes.length) / ((float) dataBytes.length)));
        }
      }
    } else {
      rc = Boolean.FALSE;
    }

    if (trace != null) trace.exit(AutoUpdater.class, rc);
    return rc;
  }

  private static class ProcLauncher extends ThreadTraced {
    String[] cmdarray;
    String[] envp;
    private ProcLauncher(String[] cmdarray, String[] envp) {
      super("ProcLauncher");
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ProcLauncher.class, "ProcLauncher(String[] cmdarray, String[] envp)");
      if (trace != null) trace.args(cmdarray, envp);
      this.cmdarray = cmdarray;
      this.envp = envp;
      if (trace != null) trace.exit(ProcLauncher.class);
    }
    public void runTraced() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ProcLauncher.class, "ProcLauncher.runTraced()");
      try {
        Runtime.getRuntime().exec(cmdarray, envp);
      } catch (IOException e) {
        if (trace != null) trace.exception(ProcLauncher.class, 100, e);
      }
      if (trace != null) trace.exit(ProcLauncher.class);
    }
  }

  private File fillFileWithData(File updateFile, AutoUpdateRecord updateRec) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "fillFileWithData(File updateFile, AutoUpdateRecord updateRec)");
    if (trace != null) trace.args(updateFile, updateRec);
    LinkedList updateStruct = null;
    if (trace != null) trace.data(10, "check for TINY");
    if (updateRec.size.intValue() > TINY_FILE_SIZE) {
      if (trace != null) trace.data(20, "read update struct");
      updateStruct = readUpdateStruct(updateFile);
    }
    if (trace != null) trace.data(30, "check for null struct");
    if (updateStruct == null) {
      if (trace != null) trace.data(40, "update struct is null");
      updateStruct = makeUpdateStruct(updateRec);
    }
    boolean fileAlmostComplete = false;
    boolean fileComplete = false;
    // in a loop request data parts of the file
    if (updateRec.size.intValue() <= TINY_FILE_SIZE) {
      fileAlmostComplete = true;
    } else {
      int batchCount = 0;
      RandomAccessFile rndAccFile = new RandomAccessFile(updateFile, "rw");
      while (!fileAlmostComplete) {
        // Check if auto-update got disabled
        UserRecord myUser = SIL.getFetchedDataCache().getUserRecord();
        if (myUser.flags != null && Misc.isBitSet(myUser.flags, UserRecord.FLAG_DISABLE_AUTO_UPDATES)) {
          break;
        }
        // Measure time for request to adjust batch size
        long timeStart = System.currentTimeMillis();
        Boolean availability = requestBytes(true, updateRec, updateStruct, rndAccFile);
        if (availability == null) {
          // if no legible answer from the server then stop fetching the updates until next login
          break;
        } else {
          // if no more data was fetched then mark the file as almost complete, just lacking the header data portion...
          fileAlmostComplete = !availability.booleanValue();
          // if some data was fetched, then re-adjust the batch size and sleep a little bit, also save header periodically
          if (Boolean.TRUE.equals(availability)) {
            long timeStop = System.currentTimeMillis();
            long timeDiff = Math.max(timeStop - timeStart, 1); // minimum difference is 1 ms
            if (timeDiff < 3000) {
              BATCH_SIZE *= 1.5;  // increase by 50%
            } else if (timeDiff > 8000) {
              BATCH_SIZE /= 2;    // decrease by 50% ... faster to decrease than to step up
              BATCH_SIZE = Math.max(BATCH_SIZE, DEFAULT_BATCH_SIZE);
            }
            batchCount ++;
            batchCount = batchCount % 3;
            if (batchCount == 0) {
              // Every once in a few data requests update the file header.
              writeHeader(rndAccFile, updateRec.size.intValue(), updateStruct);
            }
            // Slow down the connection to allow for other data to come through
            try {
              long sleepTime = timeDiff * 5;
              Thread.sleep(sleepTime);
            } catch (Throwable t) {
            }
          } // end if some data fetched
        }
      }
      if (batchCount > 0) {
        // Update the header when file is almost complete, to finalize it we will only need to request the first bytes of header size.
        writeHeader(rndAccFile, updateRec.size.intValue(), updateStruct);
      }
      rndAccFile.close();
    }

    // Finish off the file by requesting any other HEADER or FREE space bytes
    if (fileAlmostComplete) {
      RandomAccessFile rndAccFile = new RandomAccessFile(updateFile, "rw");
      while (!fileComplete) {
        fileComplete = !requestBytes(false, updateRec, updateStruct, rndAccFile).booleanValue();
      }
      rndAccFile.close();
      if (fileComplete) {
        // Rename the file to signify UPDATED state
        File completedFile = new File(updateFile.getParent(), "." + updateRec.id + ".updated");
        updateFile.renameTo(completedFile);
        updateFile = completedFile;

        // Verify file integrity -- if fails DELETE it
        byte[] hash = Digester.digestFile(updateFile, Digester.getDigest(updateRec.hashAlg));
        String hashExpected = updateRec.hashStr.toUpperCase();
        String hashComputed = ArrayUtils.toString(hash);
        if (!hashExpected.equalsIgnoreCase(hashComputed)) {
          if (trace != null) trace.data(90, "File corrupted -- deleting " + completedFile);
          updateFile.delete();
        }
      }
    }

    if (trace != null) trace.exit(AutoUpdater.class, updateFile);
    return updateFile;
  }

  private void writeUpdateProperties(AutoUpdateRecord[] updateRecs) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "writeUpdateProperties(AutoUpdateRecord[] updateRecs)");
    if (trace != null) trace.args(updateRecs);
    File updatesDir = getUpdatesDir();
    updatesDir.mkdirs();
    File updateProps = new File(updatesDir, ".update.properties");
    updateProps.createNewFile();
    Properties props = new Properties();
    props.put("updates", objToStr(map(updateRecs)));
    FileOutputStream updatePropsOut = null;
    try {
      updatePropsOut = new FileOutputStream(updateProps);
      props.store(updatePropsOut, "Update Properties");
      updatePropsOut.flush();
    } finally {
      if (updatePropsOut != null)
        updatePropsOut.close();
    }
    if (trace != null) trace.exit(AutoUpdater.class);
  }

  private Object[][] map(AutoUpdateRecord[] o) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "map(AutoUpdateRecord[] o)");
    if (trace != null) trace.args(o);
    Object[][] rc = null;
    rc = new Object[o.length][11];
    for (int i=0; i<o.length; i++) {
      rc[i] = map(o[i]);
    }
    if (trace != null) trace.exit(AutoUpdater.class, rc);
    return rc;
  }
  private AutoUpdateRecord[] map(Object[][] o) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "map(Object[][] o)");
    if (trace != null) trace.args(o);
    AutoUpdateRecord[] rc = null;
    rc = new AutoUpdateRecord[o.length];
    for (int i=0; i<o.length; i++) {
      rc[i] = map(o[i]);
    }
    if (trace != null) trace.exit(AutoUpdater.class, rc);
    return rc;
  }
  private Object[] map(AutoUpdateRecord o) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "map(AutoUpdateRecord o)");
    if (trace != null) trace.args(o);
    Object[] rc = null;
    rc = new Object[11];
    rc[0] = o.id;
    rc[1] = o.applyFrom;
    rc[2] = o.applyTo;
    rc[3] = o.size;
    rc[4] = o.build;
    rc[5] = o.hashAlg;
    rc[6] = o.hashStr;
    rc[7] = o.oldFile;
    rc[8] = o.newFile;
    rc[9] = o.locFile;
    rc[10] = o.dateExpired;
    if (trace != null) trace.exit(AutoUpdater.class, rc);
    return rc;
  }
  private AutoUpdateRecord map(Object[] o) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "map(Object[] o)");
    if (trace != null) trace.args(o);
    AutoUpdateRecord rc = null;
    rc = new AutoUpdateRecord();
    rc.id = (Long) o[0];
    rc.applyFrom = (Short) o[1];
    rc.applyTo = (Short) o[2];
    rc.size = (Integer) o[3];
    rc.build = (Short) o[4];
    rc.hashAlg = (String) o[5];
    rc.hashStr = (String) o[6];
    rc.oldFile = (String) o[7];
    rc.newFile = (String) o[8];
    rc.locFile = (String) o[9];
    rc.dateExpired = (Timestamp) o[10];
    if (trace != null) trace.exit(AutoUpdater.class, rc);
    return rc;
  }

  private void writeApplyUpdatesClass() throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "writeApplyUpdatesClass()");
    Sys_AutoUpdate_Co requestSet = new Sys_AutoUpdate_Co();
    requestSet.dataSet = new Obj_List_Co(new Object[] { new Long(-1), null, null });
    MessageAction msgAction = new MessageAction(CommandCodes.SYS_Q_GET_AUTO_UPDATE, requestSet);
    ClientMessageAction reply = SIL.submitAndFetchReply(msgAction, 15*60000);
    if (reply instanceof SysAGetAutoUpdate) {
      SysAGetAutoUpdate update = (SysAGetAutoUpdate) reply;
      Sys_AutoUpdate_Co updateSet = (Sys_AutoUpdate_Co) update.getMsgDataSet();
      Object[] set = updateSet.dataSet.objs;
      Long id = (Long) set[0];
      if (id.longValue() == -1) {
        File applyUpdates = new File(getMainDir(), "CryptoHeavenApplyUpdates.class");
        applyUpdates.createNewFile();
        FileOutputStream fOut = null;
        try {
          fOut = new FileOutputStream(applyUpdates);
          byte[] dataBytes = (byte[]) set[3];
          dataBytes = Misc.decompressBytes(dataBytes);
          fOut.write(dataBytes);
          fOut.flush();
        } finally {
          if (fOut != null)
            fOut.close();
        }
      }
    }
    if (trace != null) trace.exit(AutoUpdater.class);
  }

  private static File makeOrGetUpdateFile(Long id, Integer size) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "makeOrGetUpdateFile(Long id, Integer size)");
    if (trace != null) trace.args(id, size);
    File updatesDir = getUpdatesDir();
    boolean mkdir = updatesDir.mkdirs();
    if (trace != null) trace.data(10, mkdir);
    if (trace != null) trace.data(11, updatesDir.exists());

    File file = null;

    boolean deleted = true;

    try {
      // Check for complete file
      if (file == null) {
        file = new File(updatesDir, "." + id + ".updated");
        if (trace != null) trace.data(20, "trying file", file);
        if (file.exists() && file.length() == size.intValue()) {
          // no-op
        } else if (file.exists()) {
          deleted = file.delete();
          file = null;
        } else {
          file = null;
        }
      }

      // Check for partial file
      if (file == null) {
        file = new File(updatesDir, "." + id + ".updating");
        if (trace != null) trace.data(30, "trying file", file);
        if (file.exists() && file.length() == size.intValue()) {
          // no-op
        } else if (file.exists()) {
          deleted = file.delete();
        }
        // If doesn't exist, make and fill with zeros to size.
        if (!file.exists()) {
          if (trace != null) trace.data(40, "creating blank file");
          boolean created = file.createNewFile();
          if (deleted && created) {
            fillBlankFile(file, size.intValue());
          }
        }
      }

    } catch (Throwable t) {
      t.printStackTrace();
      if (file != null && file.exists()) {
        file.delete();
        file = null;
      }
    }

    if (trace != null) trace.exit(AutoUpdater.class, file);
    return file;
  }

  private static void fillBlankFile(File toFill, int size) throws FileNotFoundException, IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "fillBlankFile(File toFill, int size)");
    if (trace != null) trace.args(toFill);
    if (trace != null) trace.args(size);
    // make a blank file of specified size
    FileOutputStream fout = null;
    BufferedOutputStream bout = null;
    try {
      fout = new FileOutputStream(toFill);
      bout = new BufferedOutputStream(fout, 4*1024);
      for (int i=0; i<size; i++) {
        bout.write(0);
      }
      bout.flush();
      fout.flush();
    } finally {
      if (bout != null) bout.close();
      if (fout != null) fout.close();
    }
    if (trace != null) trace.exit(AutoUpdater.class);
  }

  private static File getMainDir() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "getMainDir()");
    String resourceName = null;
    if (isRunningFromJar(FILENAME__LICENSE_TXT))
      resourceName = FILENAME__LICENSE_TXT;
    else if (isRunningFromJar(FILENAME__ONEJAR_VERSION))
      resourceName = FILENAME__ONEJAR_VERSION;
    URL location = getResourceURL(resourceName);
    String path = location.getPath();
    path = URLDecoder.decode(path);
    if (trace != null) trace.data(10, "location of path", path);
    // subtract path inside the JAR
    int jarPathI = path.indexOf(".jar!/");
    if (jarPathI >= 0) {
      path = path.substring(0, jarPathI + ".jar".length());
      if (trace != null) trace.data(20, "location of path after unJARing", path);
    }
    // subtract filename part from path
    int lastDirSep = path.lastIndexOf("/");
    if (lastDirSep >= 0) {
      path = path.substring(0, lastDirSep + "/".length()); // include the seperator in the path
      if (trace != null) trace.data(30, "location of path after unFILENAMing", path);
    }
    // subtract URL protocol part from path
    if (path.startsWith("file:")) {
      path = path.substring("file:".length());
      if (trace != null) trace.data(40, "location of path after unURLing", path);
    }
    File file = new File(path);
    if (trace != null) trace.exit(AutoUpdater.class, file);
    return file;
  }

  private static File getUpdatesDir() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "getUpdatesDir()");
    File file = new File(getMainDir(), ".CryptoHeavenUpdates");
    if (trace != null) trace.exit(AutoUpdater.class, file);
    return file;
  }

  private void setUpdateHook() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "setUpdateHook()");
    String[] originalArgs = CryptoHeaven.getOriginalArgs();
    if (originalArgs == null)
      originalArgs = new String[0];

    // All environment properties
    Properties props = System.getProperties();

    // Put all properties into string array
    String[] envp = new String[props.size()];

    // Alphabetically sorted properties for tracing only
    Enumeration keyEnum = props.keys();
    TreeMap propMap = new TreeMap();
    while (keyEnum.hasMoreElements()) {
      String key = (String) keyEnum.nextElement();
      String value = props.getProperty(key);
      propMap.put(key, value);
    }
    String mainDir = getMainDir().getAbsolutePath();
    String userDir = ".";
    Set keySet = propMap.keySet();
    Iterator keyIter = keySet.iterator();
    int index = 0;
    while (keyIter.hasNext()) {
      String key = (String) keyIter.next();
      String value = (String) propMap.get(key);
      if (key.equalsIgnoreCase("user.dir")) {
        if (value.length() > 0)
          userDir = value;
      }
      envp[index] = key + "=" + value;
      if (trace != null) trace.data(10 + index, envp[index]);
      index ++;
    }

    int mainDirElements = 0;
    if (mainDir.equals(userDir)) {
      mainDirElements = 0;
    } else {
      mainDirElements = 2;
    }
    String[] cmdarray = new String[originalArgs.length + 6 + mainDirElements];

    String seperator = props.getProperty("file.separator");
    String exec = props.getProperty("java.home", ".") + seperator + "bin" + seperator + "java";
    cmdarray[0] = exec;
    if (mainDirElements > 0) {
      cmdarray[1] = "-cp";
      cmdarray[2] = mainDir;
    }
    cmdarray[1+mainDirElements] = "CryptoHeavenApplyUpdates";
    cmdarray[2+mainDirElements] = exec;
    cmdarray[3+mainDirElements] = "-jar";
    if (mainDirElements > 0)
      cmdarray[4+mainDirElements] = mainDir + seperator + getMainJarName();
    else
      cmdarray[4+mainDirElements] = getMainJarName();
    for (int i=0; i<originalArgs.length; i++)
      cmdarray[i+5+mainDirElements] = originalArgs[i];
    cmdarray[cmdarray.length-1] = "5000";
    try {
      Runtime.getRuntime().addShutdownHook(new ProcLauncher(cmdarray, envp));
    } catch (Throwable t) {
      t.printStackTrace();
    }
    if (trace != null) trace.exit(AutoUpdater.class);
  }

  public static URL getResourceURL(String fileName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "getResourceURL()");
    if (trace != null) trace.args(fileName);
    URL location = new Object().getClass().getResource("/"+fileName);
    if (location == null) {
      location = AutoUpdater.class.getResource("/"+fileName);
    }
    if (trace != null) trace.exit(AutoUpdater.class, location);
    return location;
  }

  /*** HEXER ***/
  /** data for hexadecimal visualisation. */
  private static final char[] HEX_DIGITS = {
    '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
  };
  /**
  * Returns a string of hexadecimal digits from a byte array. Each
  * byte is converted to 2 hex symbols.
  */
  public static String toString(byte[] ba) {
    return toString(ba, 0, ba.length);
  }
  public static String toString (byte[] ba, int offset, int length) {
    char[] buf = new char[length * 2];
    for (int i = offset, j = 0, k; i < offset+length; ) {
      k = ba[i++];
      buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
      buf[j++] = HEX_DIGITS[ k        & 0x0F];
    }
    return new String(buf);
  }

  /**
   * Returns a byte array from a String containing hex encoding bytes.
   */
  public static byte[] toByteArray(String hex) {
    byte[] bytes = null;
    if (hex != null) {
      bytes = new byte[hex.length()/2];
      for (int i=0; i<hex.length(); i+=2) {
        char ch1 = hex.charAt(i);
        char ch2 = hex.charAt(i+1);
        byte b1 = 0;
        byte b2 = 0;
        if (ch1 >= HEX_DIGITS[0] && ch1 <= HEX_DIGITS[9]) {
          b1 = (byte) (ch1-HEX_DIGITS[0]);
        }
        else {
          b1 = (byte) (ch1-HEX_DIGITS[10] + 10);
        }
        if (ch2 >= HEX_DIGITS[0] && ch2 <= HEX_DIGITS[9]) {
          b2 = (byte) (ch2-HEX_DIGITS[0]);
        }
        else {
          b2 = (byte) (ch2-HEX_DIGITS[10] + 10);
        }
        bytes[i/2] = (byte) ((b1 << 4) | b2);
      }
    }
    return bytes;
  }
  private static String objToStr(Object o) {
    byte[] bytes = null;
    try {
      ByteArrayOutputStream bOut = new ByteArrayOutputStream();
      ObjectOutputStream oOut = new ObjectOutputStream(bOut);
      oOut.writeObject(o);
      oOut.flush();
      bOut.flush();
      bytes = bOut.toByteArray();
      oOut.close();
    } catch (Throwable t) {
    }
    return toString(bytes);
  }
  private static Object strToObj(String s) {
    Object obj = null;
    try {
      byte[] bytes = toByteArray(s);
      ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
      ObjectInputStream oIn = new ObjectInputStream(bIn);
      obj = oIn.readObject();
      oIn.close();
    } catch (Throwable t) {
    }
    return obj;
  }

  public static boolean isLongInactive() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoUpdater.class, "isLongInactive()");
    boolean longInactive = false;
    if (!isRunning && lastRunStamp < System.currentTimeMillis() - (12L * 60L * 60L * 1000L)) { // 12 hours ago or later
      longInactive = true;
    }
    if (trace != null) trace.exit(AutoUpdater.class, longInactive);
    return longInactive;
  }

}
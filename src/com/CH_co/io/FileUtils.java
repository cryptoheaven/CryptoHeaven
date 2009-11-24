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

package com.CH_co.io;

import java.io.*;

import com.CH_co.trace.Trace;
import com.CH_co.monitor.*;
import com.CH_co.util.*;

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
 * <b>$Revision: 1.19 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FileUtils extends Object {


  public static void writeFileLength(DataOutputStream out, long fileLength) throws IOException {
    out.writeLong(fileLength);
  }

  public static long readFileLength(DataInputStream in) throws IOException {
    return in.readLong();
  }

  /**
   * Reads serialized file from an input stream and creates a file with specified prefix
   * and writes the data to that file.
   * Does not close the input stream.
   * If the originally written file had 0 bytes, a new File will be created with 0 bytes.
   */
  public static File unserializeFile(String newFilePrefix, DataInputStream2 in, ProgMonitor progressMonitor) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileUtils.class, "unserializeFile(String, DataInputStream)");
    if (trace != null) trace.args(newFilePrefix);

    File tempFile = null;
    FileOutputStream fileOut = null;

    try {
      tempFile = File.createTempFile(newFilePrefix, null);

      GlobalProperties.addTempFileToCleanup(tempFile);

      fileOut = new FileOutputStream(tempFile);

      // read length of the incoming file
      long dataLength = readFileLength(in);

      progressMonitor.setCurrentStatus("Receiving File ...");
      progressMonitor.setFileNameSource(in.getName());
      progressMonitor.setFileNameDestination(tempFile.getAbsolutePath());
      progressMonitor.setTransferSize(dataLength);
      progressMonitor.nextTask();

      moveData(in, fileOut, dataLength, progressMonitor);

      progressMonitor.doneTransfer();

      fileOut.close();
    } catch (IOException ioEx) {
      if (trace != null) trace.exception(FileUtils.class, 100, ioEx);

      // update the job status to KILLED
      progressMonitor.jobKilled();

      if (!progressMonitor.isCancelled()) {
        String fileName = "unknown";
        if (tempFile != null)
          fileName = tempFile.getAbsolutePath();

        String msg = "Exception occurred while downloading file.  The temporary file that was partially written is " + fileName + "  This error is not recoverable, the temporary file will be erased. \n\nException message is: " + ioEx.getMessage();
        String title = "Error Downloading File";
        MessageDialog.showErrorDialog(null, msg, title, true);
      }

      // when error occurred, remove partially written file
      try {
        if (fileOut != null)
          fileOut.close();
      } catch (Throwable th) { }
      try {
        if (tempFile != null)
          CleanupAgent.wipeOrDelete(tempFile);
      } catch (Throwable th) { }

      throw ioEx;
    }

    if (trace != null) trace.exit(FileUtils.class);
    return tempFile;
  }


  /**
   * Serializes the contents of specified file into an output stream 
   * without closing the output stream.
   */
  public static void serializeFile(File file, DataOutputStream2 out, ProgMonitor progressMonitor) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileUtils.class, "serializeFile(File, DataOutputStream)");

    // check file argument for null
    if (file == null) throw new IllegalArgumentException("File argument cannot be null.");

    try {
      FileInputStream fileIn = new FileInputStream(file);
      DataInputStream dataIn = new DataInputStream(fileIn);

      // write length
      long dataLength = file.length();
      writeFileLength(out, dataLength);

      progressMonitor.setCurrentStatus("Sending File ...");
      progressMonitor.setFileNameSource(file.getAbsolutePath());
      progressMonitor.setFileNameDestination(out.getName());
      progressMonitor.setTransferSize(dataLength);
      progressMonitor.nextTask();

      moveData(dataIn, (OutputStream) out, dataLength, progressMonitor);

      progressMonitor.doneTransfer();

      fileIn.close();
    } catch (IOException ioEx) {
      if (trace != null) trace.exception(FileUtils.class, 100, ioEx);

      // Wait a little for the pipes to clear and if storage or bandwidth exceeded,
      // we should get a message about interruption.  If we read interrupt, skip this message,
      // as it could only confuse the user.
      final ProgMonitor progMon = progressMonitor;
      final String fileName = file.getAbsolutePath();
      final String exceptionStr = ioEx.getMessage();

      java.util.Timer timer = new java.util.Timer();
      timer.schedule(new java.util.TimerTask() {
        public void run() {
          Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "TimerTask.run()");

          try {
            boolean msgSuppressed = false;
            msgSuppressed = progMon.isCancelled();
            if (!msgSuppressed) {
              msgSuppressed = progMon.isAllDone() || progMon.isJobKilled();
            }
            if (msgSuppressed) {
              // suppress additional message and do nothing
              if (trace != null) trace.data(110, "message suppressed");
            } else {
              // update the job status to KILLED
              progMon.jobKilled();

              String msg = "Exception occurred while serializing file " + fileName + "  This error is not recoverable, the aciton was terminated. \n\nException message is: " + exceptionStr;
              String title = "Error Uploading File";
              MessageDialog.showErrorDialog(null, msg, title);
            }
          } catch (Throwable t) {
            if (trace != null) trace.exception(getClass(), 100, t);
          }

          if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
          if (trace != null) trace.exit(getClass());
          if (trace != null) trace.clear();
        }
      }, 3000);

      throw ioEx;
    }

    if (trace != null) trace.exit(FileUtils.class);
  }



  /**
   * Reads bytes from input stream until EOF and writes them to a newly created file.
   * If the originally written file had 0 bytes, a new File will be created with 0 bytes.
   */
  public static File streamToFile(String newFilePrefix, InputStream in, ProgMonitor progressMonitor) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileUtils.class, "streamToFile(String, DataInputStream)");
    if (trace != null) trace.args(newFilePrefix);

    File tempFile = null;
    FileOutputStream fileOut = null;

    try {
      tempFile = File.createTempFile(newFilePrefix, null);
      fileOut = new FileOutputStream(tempFile);

      moveDataEOF(in, fileOut, progressMonitor);

      fileOut.close();
    } catch (IOException ioEx) {
      if (trace != null) trace.exception(FileUtils.class, 100, ioEx);

      try {
        if (fileOut != null)
          fileOut.close();
      } catch (Throwable th) { }
      try {
        if (tempFile != null)
          CleanupAgent.wipeOrDelete(tempFile);
      } catch (Throwable th) { }

      throw ioEx;
    }

    if (trace != null) trace.exit(FileUtils.class);
    return tempFile;
  }



  /**
   * Moves specified number of bytes from an InputStream to an OutputStream.
   * Flushes the output stream and does not close any of the streams.
   */
  public static void moveData(DataInputStream in, OutputStream out, long dataLength, ProgMonitor progressMonitor) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileUtils.class, "moveData(DataInputStream, OutputStream, long dataLength, ProgMonitor progressMonitor)");
    if (trace != null) trace.args(dataLength);

    // 32 Kb at a time
    byte[] buf = new byte[1024*4];

    // number of full turns
    long fullTurns = dataLength / buf.length;
    // additional turn with partial buffer size
    int remainder = (int) (dataLength % buf.length);

    for (int i=0; i<fullTurns; i++) {
      in.readFully(buf);
      // This can be a long operation, yield to others
      Thread.yield(); Thread.yield(); Thread.yield();
      out.write(buf);

      if (progressMonitor != null)
        progressMonitor.addBytes(buf.length);
    }
    if (remainder > 0) {
      in.readFully(buf, 0, remainder);
      // This can be a long operation, yield to others
      Thread.yield(); Thread.yield(); Thread.yield();
      out.write(buf, 0, remainder);

      if (progressMonitor != null)
        progressMonitor.addBytes(remainder);
    }

    out.flush();
    if (trace != null) trace.exit(FileUtils.class);
  }
  public static void moveData(DataInputStream in, DataOutput out, long dataLength, ProgMonitor progressMonitor) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileUtils.class, "moveData(DataInputStream, DataOutput, long dataLength, ProgMonitor progressMonitor)");
    if (trace != null) trace.args(dataLength);

    // 32 Kb at a time
    byte[] buf = new byte[1024*4];

    // number of full turns
    long fullTurns = dataLength / buf.length;
    // additional turn with partial buffer size
    int remainder = (int) (dataLength % buf.length);

    for (int i=0; i<fullTurns; i++) {
      in.readFully(buf);
      // This can be a long operation, yield to others
      Thread.yield(); Thread.yield(); Thread.yield();
      out.write(buf);

      if (progressMonitor != null)
        progressMonitor.addBytes(buf.length);
    }
    if (remainder > 0) {
      in.readFully(buf, 0, remainder);
      // This can be a long operation, yield to others
      Thread.yield(); Thread.yield(); Thread.yield();
      out.write(buf, 0, remainder);

      if (progressMonitor != null)
        progressMonitor.addBytes(remainder);
    }

    if (trace != null) trace.exit(FileUtils.class);
  }

  /**
   * Moves bytes from an InputStream to an OutputStream until EOF is reached.
   * Flushes the output stream and does not close any of the streams.
   */
  public static void moveDataEOF(InputStream in, OutputStream out, ProgMonitor progressMonitor) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileUtils.class, "moveDataEOF(InputStream, OutputStream, ProgMonitor progressMonitor)");

     // 32 Kb at a time
    byte[] buf = new byte[1024*4];
    int bytesRead = 0;

    // while not EOF
    while (bytesRead != -1) {

      bytesRead = in.read(buf);
      if (bytesRead > 0) {
        // not EOF
        // This can be a long operation, yield to others
        Thread.yield(); Thread.yield(); Thread.yield();
        out.write(buf, 0, bytesRead);

        if (progressMonitor != null) 
          progressMonitor.addBytes(bytesRead);
      }
    } // end while
    out.flush();
    if (trace != null) trace.exit(FileUtils.class);
  }


  /**
   * Moves characters from a Reader to a Writer until EOF is reached.
   * Flushes the output writer and does not close any of the streams.
   */
  public static void moveDataEOF(Reader in, Writer out, ProgMonitor progressMonitor) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileUtils.class, "moveDataEOF(Reader, Writer, ProgMonitor progressMonitor)");

     // 32 Kb at a time
    char[] buf = new char[1024*4];
    int charsRead = 0;

    // while not EOF
    while (charsRead != -1) {

      charsRead = in.read(buf);
      if (charsRead > 0) {
        // not EOF
        // This can be a long operation, yield to others
        Thread.yield(); Thread.yield(); Thread.yield();
        out.write(buf, 0, charsRead);

        if (progressMonitor != null) 
          progressMonitor.addBytes(charsRead);
      }
    } // end while
    out.flush();
    if (trace != null) trace.exit(FileUtils.class);
  }

}
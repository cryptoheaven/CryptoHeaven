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

package com.CH_co.service.msg.dataSets.file;

import com.CH_co.io.*;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.trace.Trace;

import java.io.*;

/**
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class File_Transfer_Co extends ProtocolMsgDataSet {

  // <fileLinkId> <fileId> <plainDataFileLength> <startFromByte> <encStream>

  public Long fileLinkId;
  public Long fileId;
  public Long plainFileLength;
  public long startFromByte;
  public File inFile;
  public InputStream inStream;

  /** Creates new File_Transfer_Co */
  public File_Transfer_Co() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_Transfer_Co.class, "File_Transfer_Co()");
    if (trace != null) trace.exit(File_Transfer_Co.class);
  }

  /** Creates new File_Transfer_Co */
  public File_Transfer_Co(Long fileLinkId, Long fileId, Long plainFileLength, long startFromByte, File inFile, InputStream inStream) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_Transfer_Co.class, "File_Transfer_Co()");
    this.fileLinkId = fileLinkId;
    this.fileId = fileId;
    this.plainFileLength = plainFileLength;
    this.startFromByte = startFromByte;
    this.inFile = inFile;
    this.inStream = inStream;
    if (trace != null) trace.exit(File_Transfer_Co.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_Transfer_Co.class, "writeToStream(DataOutputStream2)");

    dataOut.writeLongObj(fileLinkId);
    dataOut.writeLongObj(fileId);
    dataOut.writeLongObj(plainFileLength);
     dataOut.writeLong(startFromByte);
    dataOut.writeFileStream(inFile, inStream, progressMonitor);

    if (trace != null) trace.exit(File_Transfer_Co.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_Transfer_Co.class, "initFromStream(DataInputStream2)");

    fileLinkId = dataIn.readLongObj();
    fileId = dataIn.readLongObj();
    plainFileLength = dataIn.readLongObj();
     startFromByte = dataIn.readLong();
    inStream = dataIn;

    if (trace != null) trace.exit(File_Transfer_Co.class);
  } // end initFromStream()


  public String toString() {
    return "[File_Transfer_Co"
      + ": fileLinkId=" + fileLinkId
      + ": fileId=" + fileId
      + ": plainFileLength=" + plainFileLength
       + ": startFromByte=" + startFromByte
      + ": inFile=" + inFile
      + ": inStream=" + inStream
      + "]";
  }

}
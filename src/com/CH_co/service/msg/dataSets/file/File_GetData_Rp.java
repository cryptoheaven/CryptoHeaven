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

package com.CH_co.service.msg.dataSets.file;


import java.io.IOException;

import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.util.Misc;
import com.CH_co.trace.Trace;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.records.FileDataRecord;
import com.CH_co.service.msg.ProtocolMsgDataSet;

import com.CH_co.monitor.ProgMonitorI;

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
 * <b>$Revision: 1.8 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class File_GetData_Rp extends ProtocolMsgDataSet {

  // <numberOfFiles> { 
  //      <fileLinkId> <fileId> <encOrigDataDigest> <encSignedOrigDigest> <encEncDataDigest>  
  //      <signingKeyId> <fileCreated> <fileUpdated> <encSize> <encDataFile>
  //      }*

  public Long[] fileLinkIds;
  public FileDataRecord[] fileDataRecords;

  /** Creates new File_GetData_Rp */
  public File_GetData_Rp() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_GetData_Rp.class, "File_GetData_Rp()");
    if (trace != null) trace.exit(File_GetData_Rp.class);
  }

  /** Creates new File_GetData_Rp */
  public File_GetData_Rp(Long[] fileLinkIds, FileDataRecord[] fileDataRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_GetData_Rp.class, "File_GetData_Rp()");
    if (trace != null) trace.args(fileLinkIds);
    if (trace != null) trace.args(fileDataRecords);

    this.fileLinkIds = fileLinkIds;
    this.fileDataRecords = fileDataRecords;

    if (trace != null) trace.exit(File_GetData_Rp.class);
  }


  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_GetData_Rp.class, "writeToStream(DataOutputStream2, ProgMonitor)");
    if (trace != null) trace.exit(File_GetData_Rp.class);
  } // end writeToStream()


  /** 
   * Partially writes out 'this' object to a stream.
   * Used when writing files straight from the database connection to the stream.
   * Writes all attributes except for the file itself
   */
  public void partialWriteToStream(DataOutputStream2 dataOut, int index) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_GetData_Rp.class, "partialWriteToStream(DataOutputStream2 dataOut, int index)");
    if (trace != null) trace.args(index);

    if (index == 0) {
      // write indicator
      if (fileDataRecords == null)
        dataOut.write(0);
      else {
        dataOut.write(1);

        int length = fileLinkIds.length;
        if (length != fileDataRecords.length)
          throw new IllegalArgumentException("Array lengths do not match!");

        dataOut.writeShort(length);
      }
    }

    dataOut.writeLongObj(fileLinkIds[index]);
    dataOut.writeLongObj(fileDataRecords[index].fileId);
    dataOut.writeBytes(fileDataRecords[index].getEncOrigDataDigest());
    dataOut.writeBytes(fileDataRecords[index].getEncSignedOrigDigest());
    dataOut.writeBytes(fileDataRecords[index].getEncEncDataDigest());
    dataOut.writeLongObj(fileDataRecords[index].getSigningKeyId());
    dataOut.writeTimestamp(fileDataRecords[index].fileCreated);
    dataOut.writeTimestamp(fileDataRecords[index].fileUpdated);

    Long fileSizeObj = fileDataRecords[index].getEncSize();
    dataOut.writeLongObj(fileSizeObj);

    if (trace != null) trace.exit(File_GetData_Rp.class);
  } // end partialWriteToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_GetData_Rp.class, "initFromStream(DataInputStream2, ProgMonitor)");

    progressMonitor.setCurrentStatus("Receiving Files");

    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      fileDataRecords = new FileDataRecord[0];
    else {
      int length = dataIn.readShort();
      fileLinkIds = new Long[length];
      fileDataRecords = new FileDataRecord[length];

      for (int i=0; i<length; i++) {

        fileLinkIds[i] = dataIn.readLongObj();

        fileDataRecords[i] = new FileDataRecord();
        fileDataRecords[i].fileId = dataIn.readLongObj();
        fileDataRecords[i].setEncOrigDataDigest(dataIn.readSymCipherBulk());
        fileDataRecords[i].setEncSignedOrigDigest(dataIn.readSymCipherBulk());
        fileDataRecords[i].setEncEncDataDigest(dataIn.readSymCipherBulk());
        fileDataRecords[i].setSigningKeyId(dataIn.readLongObj());
        fileDataRecords[i].fileCreated = dataIn.readTimestamp();
        fileDataRecords[i].fileUpdated = dataIn.readTimestamp();
        fileDataRecords[i].setEncSize(dataIn.readLongObj());

        fileDataRecords[i].setEncDataFile(dataIn.readFile(progressMonitor));
      }
    }

    if (trace != null) trace.exit(File_GetData_Rp.class);
  } // end initFromStream()


  public String toString() {
    return "[File_GetData_Rp"
      + ": fileLinkIds=" + Misc.objToStr(fileLinkIds)
      + ", fileDataRecords=" + Misc.objToStr(fileDataRecords)
      + "]";
  }

  public String toStringLongFormat() {
    StringBuffer recordsBuf = new StringBuffer();
    for (int i=0; i<fileDataRecords.length; i++) {
      recordsBuf.append("\nrecord[" + i + "]=" + fileDataRecords[i].toStringLongFormat() + "\n");
    }

    return "[File_GetData_Rp"
      + "\n: fileLinkIds=" + Misc.objToStr(fileLinkIds)
      + "\n, fileDataRecords=" + recordsBuf.toString()
      + "]";
  }

}
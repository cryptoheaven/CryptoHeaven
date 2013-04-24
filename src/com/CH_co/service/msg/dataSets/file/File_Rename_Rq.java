/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
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

import java.io.IOException;

import com.CH_co.trace.Trace;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;

import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.records.FileLinkRecord;

/** 
 * <b>Copyright</b> &copy; 2001-2013
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
 * <b>$Revision: 1.8 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class File_Rename_Rq extends ProtocolMsgDataSet {

  // <shareId> <fileLinkId> <encFileType> <encFileName> <encFileDesc>
  public Long shareId;
  public FileLinkRecord fileLinkRecord;
  
  /** Creates new File_Rename_Rq */
  public File_Rename_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_Rename_Rq.class, "File_Rename_Rq()");
    if (trace != null) trace.exit(File_Rename_Rq.class);
  }
  /** Creates new File_Rename_Rq */
  public File_Rename_Rq(Long shareId, FileLinkRecord fileLinkRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_Rename_Rq.class, "File_Rename_Rq(Long shareId, FileLinkRecord fileLinkRecord)");
    if (trace != null) trace.args(shareId);
    if (trace != null) trace.args(fileLinkRecord);
    this.shareId = shareId;
    this.fileLinkRecord = fileLinkRecord;
    if (trace != null) trace.exit(File_Rename_Rq.class);
  }
 
  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_Rename_Rq.class, "writeToStream(DataOutputStream2, ProgMonitor)");
    
    dataOut.writeLongObj(shareId);
    dataOut.writeLongObj(fileLinkRecord.fileLinkId);
    dataOut.writeBytes(fileLinkRecord.getEncFileType());
    dataOut.writeBytes(fileLinkRecord.getEncFileName());
    dataOut.writeBytes(fileLinkRecord.getEncFileDesc());
    
    if (trace != null) trace.exit(File_Rename_Rq.class);
  } // end writeToStream()
  
  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_Rename_Rq.class, "initFromStream(DataInputStream2, ProgMonitor)");
    
    shareId = dataIn.readLongObj();
    
    fileLinkRecord = new FileLinkRecord();
    fileLinkRecord.fileLinkId = dataIn.readLongObj();
    fileLinkRecord.setEncFileType(dataIn.readSymCipherBulk());
    fileLinkRecord.setEncFileName(dataIn.readSymCipherBulk());
    fileLinkRecord.setEncFileDesc(dataIn.readSymCipherBulk());

    if (trace != null) trace.exit(File_Rename_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[File_Rename_Rq"
      + ": shareId=" + shareId
      + ", fileLinkRecord=" + fileLinkRecord
      + "]";
  }

}
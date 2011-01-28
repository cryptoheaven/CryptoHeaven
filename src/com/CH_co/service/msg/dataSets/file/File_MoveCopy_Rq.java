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

package com.CH_co.service.msg.dataSets.file;

import java.io.IOException;

import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.util.Misc;

import com.CH_co.trace.Trace;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;

import com.CH_co.service.records.*;

/** 
 * <b>Copyright</b> &copy; 2001-2011
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
public class File_MoveCopy_Rq extends ProtocolMsgDataSet {

  // <toShareId> <numberOfMsgs> { <fromMsgLinkId> }+ <numberOfShares> { <fromShareId> }+ <numberOfFiles> { <fileLinkId> <encSymmetricKey> }+ 
  public Long toShareId;
  public Long[] fromMsgLinkIDs;
  public Long[] fromShareIDs;
  public FileLinkRecord[] fileLinkRecords;


  /** Creates new File_MoveCopy_Rq */
  public File_MoveCopy_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_MoveCopy_Rq.class, "File_MoveCopy_Rq()");
    if (trace != null) trace.exit(File_MoveCopy_Rq.class);
  }


  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_MoveCopy_Rq.class, "writeToStream(DataOutputStream2, ProgMonitor)");

    dataOut.writeLongObj(toShareId);

    // write fromMsgLinkIDs
    // write indicator
    if (fromMsgLinkIDs == null)
      dataOut.write(0);
    else {
      dataOut.write(1);

      dataOut.writeShort(fromMsgLinkIDs.length);
      for (int i=0; i<fromMsgLinkIDs.length; i++)
        dataOut.writeLongObj(fromMsgLinkIDs[i]);
    }

    // write fromShareIDs
    // write indicator
    if (fromShareIDs == null)
      dataOut.write(0);
    else {
      dataOut.write(1);

      dataOut.writeShort(fromShareIDs.length);
      for (int i=0; i<fromShareIDs.length; i++)
        dataOut.writeLongObj(fromShareIDs[i]);
    }

    // write fileLinkRecords
    // write indicator
    if (fileLinkRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);

      dataOut.writeShort(fileLinkRecords.length);
      for (int i=0; i<fileLinkRecords.length; i++) {
        dataOut.writeLongObj(fileLinkRecords[i].fileLinkId);
        dataOut.writeBytes(fileLinkRecords[i].getEncSymmetricKey());
      }
    }

    if (trace != null) trace.exit(File_MoveCopy_Rq.class);
  } // end writeToStream()


  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_MoveCopy_Rq.class, "initFromStream(DataInputStream2, ProgMonitor)");

    toShareId = dataIn.readLongObj();

    // read fromMsgLinkIDs
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      fromMsgLinkIDs = new Long[0];
    else {
      fromMsgLinkIDs = new Long[dataIn.readShort()];

      for (int i=0; i<fromMsgLinkIDs.length; i++)
        fromMsgLinkIDs[i] = dataIn.readLongObj();
    }

    // read fromShareIDs
    // read indicator
    indicator = dataIn.read();
    if (indicator == 0)
      fromShareIDs = new Long[0];
    else {
      fromShareIDs = new Long[dataIn.readShort()];

      for (int i=0; i<fromShareIDs.length; i++)
        fromShareIDs[i] = dataIn.readLongObj();
    }

    // read fileLinkRecords
    // read indicator
    indicator = dataIn.read();
    if (indicator == 0)
      fileLinkRecords = new FileLinkRecord[0];
    else {
      fileLinkRecords = new FileLinkRecord[dataIn.readShort()];

      for (int i=0; i<fileLinkRecords.length; i++) {
        fileLinkRecords[i] = new FileLinkRecord();
        fileLinkRecords[i].fileLinkId = dataIn.readLongObj();
        fileLinkRecords[i].setEncSymmetricKey(dataIn.readSymCipherBulk());
      }
    }


    if (trace != null) trace.exit(File_MoveCopy_Rq.class);
  } // end initFromStream()


  public String toString() {
    return "[File_MoveCopy_Rq"
      + ": toShareId="        + toShareId
      + ", fromMsgLinkIDs="   + Misc.objToStr(fromMsgLinkIDs)
      + ", fromShareIDs="     + Misc.objToStr(fromShareIDs)
      + ", fileLinkRecords="  + Misc.objToStr(fileLinkRecords)
      + "]";
  }

}
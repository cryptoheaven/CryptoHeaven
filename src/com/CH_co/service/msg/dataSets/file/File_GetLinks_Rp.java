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

package com.CH_co.service.msg.dataSets.file;

import java.io.IOException;

import com.CH_co.monitor.ProgMonitor;
import com.CH_co.util.Misc;

import com.CH_co.trace.Trace;
import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.stat.*;

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
 * <b>$Revision: 1.8 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class File_GetLinks_Rp extends ProtocolMsgDataSet {

  // <numberOfFiles> {
  //    <fileLinkId> <fileId> <ownerObjId> <ownerObjType> <ownerUserId> 
  //    <encFileType> <encFileName> <encFileDesc> <encSymmetricKey> 
  //    <origSize> <recordCreated> <recordUpdated> 
  //    }*
  // <Stats_Get_Rp>
  public FileLinkRecord[] fileLinks;
  public Stats_Get_Rp stats_rp;


  /** Creates new File_GetLinks_Rp */
  public File_GetLinks_Rp() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_GetLinks_Rp.class, "File_GetLinks_Rp()");
    if (trace != null) trace.exit(File_GetLinks_Rp.class);
  }
  /** Creates new File_GetLinks_Rp */
  public File_GetLinks_Rp(FileLinkRecord[] fileLinks) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_GetLinks_Rp.class, "File_GetLinks_Rp(FileLinkRecord[])");
    this.fileLinks = fileLinks;
    if (trace != null) trace.exit(File_GetLinks_Rp.class);
  }
  /** Creates new File_GetLinks_Rp */
  public File_GetLinks_Rp(FileLinkRecord[] fileLinks, StatRecord[] statRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_GetLinks_Rp.class, "File_GetLinks_Rp(FileLinkRecord[], StatRecords statRecords)");
    this.fileLinks = fileLinks;
    if (statRecords != null && statRecords.length > 0)
      this.stats_rp = new Stats_Get_Rp(statRecords);
    if (trace != null) trace.exit(File_GetLinks_Rp.class);
  }
  /** Creates new File_GetLinks_Rp */
  public File_GetLinks_Rp(FileLinkRecord fileLink) {
    this(new FileLinkRecord[] { fileLink });
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_GetLinks_Rp.class, "File_GetLinks_Rp(FileLinkRecord)");
    if (trace != null) trace.exit(File_GetLinks_Rp.class);
  }


  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_GetLinks_Rp.class, "writeToStream(DataOutputStream2)");
    // write indicator
    if (fileLinks == null)
      dataOut.write(0);
    else {
      dataOut.write(1);

      dataOut.writeShort(fileLinks.length);
      for (int i=0; i<fileLinks.length; i++ ) {
        dataOut.writeLongObj(fileLinks[i].fileLinkId);
        dataOut.writeLongObj(fileLinks[i].fileId);
        dataOut.writeLongObj(fileLinks[i].ownerObjId);
        dataOut.writeSmallint(fileLinks[i].ownerObjType);
        dataOut.writeBytes(fileLinks[i].getEncFileType());
        dataOut.writeBytes(fileLinks[i].getEncFileName());
        dataOut.writeBytes(fileLinks[i].getEncFileDesc());
        dataOut.writeBytes(fileLinks[i].getEncSymmetricKey());
        dataOut.writeLongObj(fileLinks[i].origSize);
        dataOut.writeTimestamp(fileLinks[i].recordCreated);
        dataOut.writeTimestamp(fileLinks[i].recordUpdated);
      }
    }
    if (stats_rp == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      stats_rp.writeToStream(dataOut, progressMonitor, clientBuild, serverBuild);
    }

    if (trace != null) trace.exit(File_GetLinks_Rp.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_GetLinks_Rp.class, "initFromStream(DataInputStream2)");
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      fileLinks = new FileLinkRecord[0];
    else {
      fileLinks = new FileLinkRecord[dataIn.readShort()];

      for (int i=0; i<fileLinks.length; i++) {
        fileLinks[i] = new FileLinkRecord();
        fileLinks[i].fileLinkId = dataIn.readLongObj();
        fileLinks[i].fileId = dataIn.readLongObj();
        fileLinks[i].ownerObjId = dataIn.readLongObj();
        fileLinks[i].ownerObjType = dataIn.readSmallint();
        fileLinks[i].setEncFileType(dataIn.readSymCipherBulk());
        fileLinks[i].setEncFileName(dataIn.readSymCipherBulk());
        fileLinks[i].setEncFileDesc(dataIn.readSymCipherBulk());
        fileLinks[i].setEncSymmetricKey(dataIn.readSymCipherBulk());
        fileLinks[i].origSize = dataIn.readLongObj();
        fileLinks[i].recordCreated = dataIn.readTimestamp();
        fileLinks[i].recordUpdated = dataIn.readTimestamp();
      }
    }
    indicator = dataIn.read();
    if (indicator == 0)
      stats_rp = null;
    else {
      stats_rp = new Stats_Get_Rp();
      stats_rp.initFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
    }

    if (trace != null) trace.exit(File_GetLinks_Rp.class);
  } // end initFromStream()



  public String toString() {
    return "[File_GetLinks_Rp"
      + ": fileLinks=" + Misc.objToStr(fileLinks)
      + ", stats_rp=" + stats_rp
      + "]";
  }

  public String toStringLongFormat() {
    StringBuffer recordsBuf = new StringBuffer();
    for (int i=0; i<fileLinks.length; i++) {
      recordsBuf.append("\nrecord[" + i + "]=" + fileLinks[i].toStringLongFormat() + "\n");
    }

    return "[File_GetLinks_Rp"
      + "\n: fileLinks=" + recordsBuf.toString()
      + "\n, stats_rp=" + stats_rp
      + "]";
  }

}
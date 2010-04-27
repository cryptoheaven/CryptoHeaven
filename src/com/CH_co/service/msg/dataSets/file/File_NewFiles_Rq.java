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

import com.CH_co.cryptx.*;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.records.FileLinkRecord;
import com.CH_co.service.records.FileDataRecord;

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
 * <b>$Revision: 1.9 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class File_NewFiles_Rq extends ProtocolMsgDataSet {

  // <numberOfFiles> { 
  //    <ownerObjId> <ownerObjType> <encFileType> <encFileName> <encFileDesc> <encSymmetricKey> <origSize> 
  //    <encOrigDataDigest> <encSignedOrigDigest> <encEncDataDigest> <signingKeyId> <encSize> <encDataFile> 
  //    }+

  // owner object types are defined in com.CH_co.service.records.Record
  public FileLinkRecord[] fileLinks;
  public FileDataRecord[] fileDataRecords;

  /** Creates new File_NewFiles_Rq */
  public File_NewFiles_Rq() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_NewFiles_Rq.class, "File_NewFiles_Rq()");
    if (trace != null) trace.exit(File_NewFiles_Rq.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_NewFiles_Rq.class, "writeToStream(DataOutputStream2)");

    // write indicator
    if (fileLinks == null)
      dataOut.write(0);
    else {
      progressMonitor.setCurrentStatus("New File(s) Request ...");
      dataOut.write(1);

      int length = fileLinks.length;
      
      // check if both arrays have the same number of elements
      if (length != fileDataRecords.length)
        throw new IllegalArgumentException("Arrays are of different length.");

      // write number of elements in both arrays
      dataOut.writeShort(length);

      for (int i=0; i<length; i++ ) {
        dataOut.writeLongObj(fileLinks[i].ownerObjId);
        dataOut.writeSmallint(fileLinks[i].ownerObjType);
        dataOut.writeBytes(fileLinks[i].getEncFileType());
        dataOut.writeBytes(fileLinks[i].getEncFileName());
        dataOut.writeBytes(fileLinks[i].getEncFileDesc());
        dataOut.writeBytes(fileLinks[i].getEncSymmetricKey());
        dataOut.writeLongObj(fileLinks[i].origSize);

        dataOut.writeBytes(fileDataRecords[i].getEncOrigDataDigest());
        dataOut.writeBytes(fileDataRecords[i].getEncSignedOrigDigest());
        dataOut.writeBytes(fileDataRecords[i].getEncEncDataDigest());
        dataOut.writeLongObj(fileDataRecords[i].getSigningKeyId());
        dataOut.writeLongObj(fileDataRecords[i].getEncSize());
        dataOut.writeFile(fileDataRecords[i].getEncDataFile(), progressMonitor);
      }
    }

    if (trace != null) trace.exit(File_NewFiles_Rq.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_NewFiles_Rq.class, "initFromStream(DataInputStream2)");
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0) {
      fileLinks = new FileLinkRecord[0];
      fileDataRecords = new FileDataRecord[0];
    }
    else {
      short numOfElements = dataIn.readShort();
      
      fileLinks = new FileLinkRecord[numOfElements];
      fileDataRecords = new FileDataRecord[numOfElements];
/*
      for (int i=0; i<numOfElements; i++) {
        
        // read in the file link variables
        fileLinks[i] = new FileLinkRecord();
        fileLinks[i].ownerObjId = dataIn.readLongObj();
        fileLinks[i].ownerObjType = dataIn.readSmallint();
        fileLinks[i].setEncFileType(new BASymCipherBulk(dataIn.readBytes()));
        fileLinks[i].setEncFileName(new BASymCipherBulk(dataIn.readBytes()));
        fileLinks[i].setEncSymmetricKey(new BASymCipherBulk(dataIn.readBytes()));
        fileLinks[i].origSize = dataIn.readLongObj();

        // read in the file data record variables
        fileDataRecords[i] = new FileDataRecord();
        fileDataRecords[i].setOrigDataDigest(new BADigestBlock(dataIn.readBytes()));
        fileDataRecords[i].setSignedOrigDigest(new BAAsyCipherBlock(dataIn.readBytes()));
        fileDataRecords[i].setEncDataDigest(new BADigestBlock(dataIn.readBytes()));
        fileDataRecords[i].setSignedEncDigest(new BAAsyCipherBlock(dataIn.readBytes()));
        fileDataRecords[i].setSigningKeyId(dataIn.readLongObj());
        fileDataRecords[i].setEncSize(dataIn.readLongObj());
        fileDataRecords[i].setEncDataFile(dataIn.readFile(progressMonitor));
      }
*/
    }
    if (trace != null) trace.exit(File_NewFiles_Rq.class);
  } // end initFromStream()

  
  /** Initializes 'this' object from a stream. */
  public void partialInitFromStream(DataInputStream2 dataIn, int index, short clientBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_NewFiles_Rq.class, "partialInitFromStream(DataInputStream2 dataIn, int index, short clientBuild)");
    if (trace != null) trace.args(index);
    if (trace != null) trace.args(clientBuild);

    // read in the file link variables
    fileLinks[index] = new FileLinkRecord();
    fileLinks[index].ownerObjId = dataIn.readLongObj();
    fileLinks[index].ownerObjType = dataIn.readSmallint();
    fileLinks[index].setEncFileType(dataIn.readSymCipherBulk());
    fileLinks[index].setEncFileName(dataIn.readSymCipherBulk());
    fileLinks[index].setEncFileDesc(dataIn.readSymCipherBulk());
    fileLinks[index].setEncSymmetricKey(dataIn.readSymCipherBulk());
    fileLinks[index].origSize = dataIn.readLongObj();

    // read in the file data record variables
    fileDataRecords[index] = new FileDataRecord();
    fileDataRecords[index].setEncOrigDataDigest(dataIn.readSymCipherBulk());
    fileDataRecords[index].setEncSignedOrigDigest(dataIn.readSymCipherBulk());
    fileDataRecords[index].setEncEncDataDigest(dataIn.readSymCipherBulk());
    fileDataRecords[index].setSigningKeyId(dataIn.readLongObj());
    fileDataRecords[index].setEncSize(dataIn.readLongObj());
    fileDataRecords[index].fileSource = dataIn;

    if (trace != null) trace.exit(File_NewFiles_Rq.class);
  } // end partialInitFromStream()



  public String toString() {
    return "[File_NewFiles_Rq"
      + ": fileLinks=" + Misc.objToStr(fileLinks)
      + ": fileDataRecords=" + Misc.objToStr(fileDataRecords)
      + "]";
  }

}
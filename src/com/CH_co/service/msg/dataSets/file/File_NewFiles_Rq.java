/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
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
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

import java.io.IOException;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.9 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class File_NewFiles_Rq extends ProtocolMsgDataSet {

  // <isFileStudRequest>
  // <numberOfFiles> {
  //    <ownerObjId> <ownerObjType> <encFileType> <encFileName> <encFileDesc> <encSymmetricKey> <origSize> <status>
  //    <encOrigDataDigest> <encSignedOrigDigest> <encEncDataDigest> <signingKeyId> <encSize> <encDataFile>
  //    }+

  // owner object types are defined in com.CH_co.service.records.Record
  public FileLinkRecord[] fileLinks;
  public FileDataRecord[] fileDataRecords;
  public boolean isFileStudRequest;

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

      if (clientBuild >= 644 && serverBuild >= 644) {
        isFileStudRequest = fileDataRecords.length > 0
                && fileDataRecords[0].getEncSize() != null
                && fileDataRecords[0].getEncSize().longValue() == -1;
        dataOut.writeBoolean(isFileStudRequest);
      }
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
        if (clientBuild >= 580 && serverBuild >= 580)
          dataOut.writeSmallint(fileLinks[i].status);

        dataOut.writeBytes(fileDataRecords[i].getEncOrigDataDigest());
        dataOut.writeBytes(fileDataRecords[i].getEncSignedOrigDigest());
        dataOut.writeBytes(fileDataRecords[i].getEncEncDataDigest());
        dataOut.writeLongObj(fileDataRecords[i].getSigningKeyId());
        dataOut.writeLongObj(fileDataRecords[i].getEncSize());
        if (!isFileStudRequest)
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
      if (clientBuild >= 644 && serverBuild >= 644)
        isFileStudRequest = dataIn.readBoolean();
      short numOfElements = dataIn.readShort();

      fileLinks = new FileLinkRecord[numOfElements];
      fileDataRecords = new FileDataRecord[numOfElements];

      // If we are reading a stud, read the entire packet right away,
      // there will be no streaming parts to read in later.. nullifies the source stream.
      if (isFileStudRequest) {
        for (int i=0; i<fileLinks.length; i++) {
          partialInitFromStream(dataIn, i, clientBuild, serverBuild);
          fileDataRecords[i].fileSource = null;
        }
      }
    }
    if (trace != null) trace.exit(File_NewFiles_Rq.class);
  } // end initFromStream()


  /** Initializes 'this' object from a stream. */
  public void partialInitFromStream(DataInputStream2 dataIn, int index, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(File_NewFiles_Rq.class, "partialInitFromStream(DataInputStream2 dataIn, int index, short clientBuild, short serverBuild)");
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
    if (clientBuild >= 580 && serverBuild >= 580)
      fileLinks[index].status = dataIn.readSmallint();

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
      + ": isFileStudRequest=" + (fileDataRecords != null
                                && fileDataRecords.length > 0
                                && fileDataRecords[0].getEncSize() != null
                                && fileDataRecords[0].getEncSize().longValue() == -1)
      + "]";
  }

}
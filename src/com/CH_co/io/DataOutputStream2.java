/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.io;

import com.CH_co.cryptx.BA;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.util.Misc;

import java.io.*;
import java.sql.Timestamp;
import java.util.Date;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class DataOutputStream2 extends DataOutputStream {

  private String streamName;

  /** Creates new DataOutputStream2 */
  public DataOutputStream2(OutputStream outputStream, String streamName) {
    super(outputStream);
    this.streamName = streamName;
  }

  /** Creates new DataOutputStream2 */
  public DataOutputStream2(OutputStream outputStream) {
    super(outputStream);
  }


  public String getName() {
    return streamName;
  }

  public void writeBooleanObj(Boolean bObj) throws IOException {
    byte nullIndicator = (byte) ((bObj != null) ? 0 : -1);
    writeByte(nullIndicator);

    if (nullIndicator == 0)
      writeBoolean(bObj.booleanValue());
  }

  public void writeBytes(BA ba) throws IOException {
    if (ba == null)
      writeBytes((byte[]) null);
    else
      writeBytes(ba.toByteArray());
  }

  public void writeBytes(byte[] bytes) throws IOException {
    if (bytes != null) {
      writeInt(bytes.length);
      write(bytes);
    } else {
      writeInt(-1);
    }
  }

  public void writeByteObj(Byte bObj) throws IOException {
    byte nullIndicator = (byte) ((bObj != null) ? 0 : -1);
    writeByte(nullIndicator);

    if (nullIndicator == 0)
      writeByte(bObj.byteValue());
  }

  public void writeCharByte(Character ch) throws IOException {
    byte nullIndicator = (byte) ((ch != null) ? 0 : -1);
    writeByte(nullIndicator);

    if (nullIndicator == 0)
      writeByte(ch.charValue());
  }

  public void writeString(String str) throws IOException {
    writeBytes(Misc.convStrToBytes(str));
  }

  public void writeDate(Date date) throws IOException {
    byte nullIndicator = (byte) ((date != null) ? 0 : -1);
    writeByte(nullIndicator);

    if (nullIndicator == 0)
      writeLong(date.getTime());
  }

  public void writeTimestamp(Timestamp timestamp) throws IOException {
    byte nullIndicator = (byte) ((timestamp != null) ? 0 : -1);
    writeByte(nullIndicator);

    if (nullIndicator == 0) {
      writeLong(timestamp.getTime());
      writeInt(timestamp.getNanos());
    }
  }

  public void writeFloatObj(Float floatObj) throws IOException {
    byte nullIndicator = (byte) ((floatObj != null) ? 0 : -1);
    writeByte(nullIndicator);

    if (nullIndicator == 0)
      writeFloat(floatObj.floatValue());
  }

  public void writeDoubleObj(Double doubleObj) throws IOException {
    byte nullIndicator = (byte) ((doubleObj != null) ? 0 : -1);
    writeByte(nullIndicator);

    if (nullIndicator == 0)
      writeDouble(doubleObj.doubleValue());
  }

  public void writeLongObj(Long longObj) throws IOException {
    byte nullIndicator = (byte) ((longObj != null) ? 0 : -1);
    writeByte(nullIndicator);

    if (nullIndicator == 0)
      writeLong(longObj.longValue());
  }

  public void writeInteger(Integer integerObj) throws IOException {
    byte nullIndicator = (byte) ((integerObj != null) ? 0 : -1);
    writeByte(nullIndicator);

    if (nullIndicator == 0)
      writeInt(integerObj.intValue());
  }

  public void writeSmallint(Short shortObj) throws IOException {
    byte nullIndicator = (byte) ((shortObj != null) ? 0 : -1);
    writeByte(nullIndicator);

    if (nullIndicator == 0)
      writeShort(shortObj.shortValue());
  }

  public void writeFile(File file, ProgMonitorI progressMonitor) throws IOException {
    byte nullIndicator = (byte) ((file != null) ? 0 : -1);
    writeByte(nullIndicator);

    if (nullIndicator == 0)
      FileUtils.serializeFile(file, this, progressMonitor);
  }

  /**
   * Write the file content data from the stream and insert the file header in front.
   */
  public void writeFile(long fileSize, DataInputStream fileIn) throws IOException {
    byte nullIndicator = 0;
    writeByte(nullIndicator);
    FileUtils.writeFileLength(this, fileSize);
    FileUtils.moveData(fileIn, (OutputStream) this, fileSize, null);
  }

  /**
   * Write partial file content data from the stream and insert the file header in front if partNo = 1 (first)
   */
  public void writeFilePart(long fileSize, short partNo, short totalParts, long partLength, DataInputStream fileIn) throws IOException {
    if (partNo == 1) {
      byte nullIndicator = 0;
      writeByte(nullIndicator);
      FileUtils.writeFileLength(this, fileSize);
    }
    if (fileSize >= 0) {
      FileUtils.moveData(fileIn, (OutputStream) this, partLength, null);
    } else {
      FileUtils.writePartLength(this, (int) partLength);
      FileUtils.moveData(fileIn, (OutputStream) this, partLength, null);
    }
  }

  public void writeFileStream(File file, InputStream in, ProgMonitorI progressMonitor) throws IOException {
    byte nullIndicator = (byte) ((in != null) ? 0 : -1);
    writeByte(nullIndicator);

    // final file size is unknown as it maybe appended during transfer, will send continuous stream in pieces
    if (nullIndicator == 0) {
      FileUtils.writeFileLength(this, -1);
      FileUtils.moveDataStreamEOF(file, in, (DataOutputStream) this, progressMonitor);
    }
  }

}
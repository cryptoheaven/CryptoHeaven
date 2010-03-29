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

package com.CH_co.io;

import java.io.*;
import java.util.Date;
import java.sql.Timestamp;

import com.CH_co.monitor.ProgMonitor;
import com.CH_co.cryptx.*;
import com.CH_co.service.records.FileDataRecord;
import com.CH_co.util.Misc;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class DataInputStream2 extends DataInputStream {

  private String streamName;

  /** Creates new DataInputStream2 */
  public DataInputStream2(InputStream inputStream, String streamName) {
    super(inputStream);
    this.streamName = streamName;
  }

  /** Creates new DataInputStream2 */
  public DataInputStream2(InputStream inputStream) {
    super(inputStream);
  }


  public String getName() {
    return streamName;
  }

  public byte[] readBytes() throws IOException {
    int len = readInt();
    byte[] bytes = null;
    if (len == -1) {
      bytes = null;
    } else {
      bytes = new byte[len];
      if (len > 0)
        readFully(bytes);
    }
    return bytes;
  }

  public BASymCipherBlock readSymCipherBlock() throws IOException {
    BASymCipherBlock ba = null;
    byte[] bytes = readBytes();
    if (bytes != null) {
      ba = new BASymCipherBlock(bytes);
    }
    return ba;
  }

  public BASymCipherBulk readSymCipherBulk() throws IOException {
    BASymCipherBulk ba = null;
    byte[] bytes = readBytes();
    if (bytes != null) {
      ba = new BASymCipherBulk(bytes);
    }
    return ba;
  }

  public BASymmetricKey readSymmetricKey() throws IOException {
    BASymmetricKey ba = null;
    byte[] bytes = readBytes();
    if (bytes != null) {
      ba = new BASymmetricKey(bytes);
    }
    return ba;
  }

  public BAAsyCipherBlock readAsyCipherBlock() throws IOException {
    BAAsyCipherBlock ba = null;
    byte[] bytes = readBytes();
    if (bytes != null) {
      ba = new BAAsyCipherBlock(bytes);
    }
    return ba;
  }

  public BADigestBlock readDigestBlock() throws IOException {
    BADigestBlock ba = null;
    byte[] bytes = readBytes();
    if (bytes != null) {
      ba = new BADigestBlock(bytes);
    }
    return ba;
  }

  public Boolean readBooleanObj() throws IOException {
    Boolean rc = null;
    byte nullIndicator = readByte();
    if (nullIndicator == -1)
      rc = null;
    else if (nullIndicator != 0)
      throw new IllegalStateException("Invalid byte value read.");
    else
      rc = Boolean.valueOf(readBoolean());
    return rc;
  }

  public Byte readByteObj() throws IOException {
    byte nullIndicator = readByte();
    if (nullIndicator == -1)
      return null;
    else if (nullIndicator != 0)
      throw new IllegalStateException("Invalid byte value read.");
    else
      return new Byte(readByte());
  }

  public Character readCharByte() throws IOException {
    byte nullIndicator = readByte();
    if (nullIndicator == -1)
      return null;
    else if (nullIndicator != 0)
      throw new IllegalStateException("Invalid character value read.");
    else
      return new Character((char) readByte());
  }

  public String readString() throws IOException {
    return Misc.convBytesToStr(readBytes());
  }

  public Date readDate() throws IOException {
    byte nullIndicator = readByte();
    if (nullIndicator == -1)
      return null;
    else if (nullIndicator != 0)
      throw new IllegalStateException("Invalid byte value read.");
    return new Date(readLong());
  }

  public Timestamp readTimestamp() throws IOException {
    byte nullIndicator = readByte();
    if (nullIndicator == -1)
      return null;
    else if (nullIndicator != 0)
      throw new IllegalStateException("Invalid byte value read.");
    Timestamp timestamp = new Timestamp(readLong());
    timestamp.setNanos(readInt());
    return timestamp;
  }

  public Float readFloatObj() throws IOException {
    byte nullIndicator = readByte();
    if (nullIndicator == -1)
      return null;
    else if (nullIndicator != 0)
      throw new IllegalStateException("Invalid byte value read.");
    else
      return new Float(readFloat());
  }

  public Double readDoubleObj() throws IOException {
    byte nullIndicator = readByte();
    if (nullIndicator == -1)
      return null;
    else if (nullIndicator != 0)
      throw new IllegalStateException("Invalid byte value read.");
    else
      return new Double(readDouble());
  }

  public Long readLongObj() throws IOException {
    byte nullIndicator = readByte();
    if (nullIndicator == -1)
      return null;
    else if (nullIndicator != 0)
      throw new IllegalStateException("Invalid byte value read.");
    else
      return new Long(readLong());
  }

  public Integer readInteger() throws IOException {
    byte nullIndicator = readByte();
    if (nullIndicator == -1)
      return null;
    else if (nullIndicator != 0)
      throw new IllegalStateException("Invalid byte value read.");
    else
      return new Integer(readInt());
  }

  public Short readSmallint() throws IOException {
    byte nullIndicator = readByte();
    if (nullIndicator == -1)
      return null;
    else if (nullIndicator != 0)
      throw new IllegalStateException("Invalid byte value read.");
    else
      return new Short(readShort());
  }

  public File readFile(ProgMonitor progressMonitor) throws IOException {
    byte nullIndicator = readByte();
    if (nullIndicator == -1)
      return null;
    else if (nullIndicator != 0)
      throw new IllegalStateException("Invalid byte value read.");
    else
      return FileUtils.unserializeFile(FileDataRecord.TEMP_ENCRYPTED_FILE_PREFIX, this, progressMonitor);
  }

  /**
   * @return number of bytes to follow that file consists of, and strip down the file transfer header
   * to leave just the file content data.
   */
  public long prepareFileForRead() throws IOException {
    byte nullIndicator = readByte();
    if (nullIndicator == -1)
      return 0;
    else if (nullIndicator != 0)
      throw new IllegalStateException("Invalid byte value read.");
    else
      return FileUtils.readFileLength(this);
  }
}
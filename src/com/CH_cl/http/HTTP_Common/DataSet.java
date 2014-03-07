/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.http.HTTP_Common;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.4 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class DataSet {

  public static final String CRLF = "\r\n";

  private static MessageDigest md = null;
  static {
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (Throwable t) {
    }
  }

  public static final int RETRY_PACKET_BATCH_SIZE = 2;

  public static final int ACTION_CONNECT_RQ = 0x01;
  public static final int ACTION_CONNECT_RP = 0x02;
  public static final int ACTION_DISCONNECT = 0x03;
  public static final int ACTION_SEND_RECV = 0x04;
  public static final int ACTION_NOT_CONNECTED = 0x05;
  public static final int ACTION_PING = 0x06;
  public static final int ACTION_PONG = 0x07;
  public static final int ACTION_SEND_RECV_ASYNCH = 0x08;

  public int action; // 1 byte used
  public int connectionId = -1; // 4 bytes used
  public long sequenceId = 0;  // 8 bytes, send and receive actions have independent sequence IDs, both increment by 1 every time
  public String remoteHostAddr;
  public int remoteHostPort; // 4 bytes used
  public byte[] data; // send or receive data buffer
  
  public transient long batchId = 0; // server cumulates replies into batches and uses this to help in watermark-retries decisions

  public ArrayList chain; // attached chain of DataSets
  public long responseSequenceIdProcessed = -1;
  public long responseSequenceIdAvailable = -1;

  public int tryNumber;

  /** 
   * Creates a new EMPTY data set
   */
  private DataSet() {
  }
  /** 
   * Creates a new CONNECT request data set
   */
  public DataSet(String remoteHostAddr, int remoteHostPort) {
    this.action = ACTION_CONNECT_RQ;
    this.remoteHostAddr = remoteHostAddr;
    this.remoteHostPort = remoteHostPort;
  }
  /** 
   * Creates a new CONNECT reply data set
   */
  public DataSet(int connectionId) {
    this.action = ACTION_CONNECT_RP;
    this.connectionId = connectionId;
  }
  /** 
   * Creates a new NOT CONNECTED reply data set
   */
  public DataSet(int action, int connectionId) {
    this.action = action;
    this.connectionId = connectionId;
  }
  /** 
   * Creates a new DISCONNECT data set
   */
  public DataSet(int connectionId, long sequenceId) {
    this.action = ACTION_DISCONNECT;
    this.connectionId = connectionId;
    this.sequenceId = sequenceId;
  }
  /** 
   * Creates a new SEND/RECV data set
   */
  public DataSet(int action, int connectionId, long sequenceId, byte[] data) {
    this.action = action;
    this.connectionId = connectionId;
    this.sequenceId = sequenceId;
    this.data = data;
  }

  public int getApproximatePacketLength() {
    int length = 8+1;
    switch (action) {
      case ACTION_CONNECT_RQ :
        length += remoteHostAddr.length();
        length += 4;
        break;
      case ACTION_CONNECT_RP :
      case ACTION_DISCONNECT :
      case ACTION_PING :
      case ACTION_PONG :
      case ACTION_SEND_RECV :
      case ACTION_SEND_RECV_ASYNCH :
      case ACTION_NOT_CONNECTED :
        length += 4;
        if (action == ACTION_CONNECT_RP || action == ACTION_NOT_CONNECTED)
          break;
        length += 4;
        if (action == ACTION_DISCONNECT)
          break;
        length += 1 + (data != null ? 4+data.length : 0);
        length += 8;
        length += 8;
        break;
    }
    return length;
  }

  public String toURLEncoded() throws IOException {
    return URLEncoder.encode(new String(toBASE64()));
  }
  public char[] toBASE64() throws IOException {
    return BASE64.encode(toByteArray());
  }
  public byte[] toByteArray() throws IOException {
    byte[] byteArray = null;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] digest = new byte[8];
    out.write(digest);
    out.write(action);
    switch (action) {
      case ACTION_CONNECT_RQ :
        writeString(out, remoteHostAddr);
        writeInt(out, remoteHostPort);
        break;
      case ACTION_CONNECT_RP :
      case ACTION_DISCONNECT :
      case ACTION_PING :
      case ACTION_PONG :
      case ACTION_SEND_RECV :
      case ACTION_SEND_RECV_ASYNCH :
      case ACTION_NOT_CONNECTED :
        writeInt(out, connectionId);
        if (action == ACTION_CONNECT_RP || action == ACTION_NOT_CONNECTED)
          break;
        writeLong(out, sequenceId);
        if (action == ACTION_DISCONNECT)
          break;
        writeBytes(out, data);
        writeDSList(out, chain);
        writeLong(out, responseSequenceIdProcessed);
        writeLong(out, responseSequenceIdAvailable);
        break;
    }
    out.flush();
    byteArray = out.toByteArray();
    // overwrite leading buffer bytes with digest
    synchronized (md) {
      md.reset();
      md.update(byteArray, digest.length, byteArray.length - digest.length);
      digest = md.digest();
      System.arraycopy(digest, 0, byteArray, 0, 8); // use only 8 bytes instead of 16
    }
    out.close();
    return byteArray;
  }

  public void appendSet(DataSet set) {
    if (chain == null)
      chain = new ArrayList();
    chain.add(set);
  }

  public static DataSet toDataSet(String urlEncoded) throws IOException {
    return toDataSet(BASE64.decode(URLDecoder.decode(urlEncoded).toCharArray()));
  }
  public static DataSet toDataSet(char[] base64) throws IOException {
    return toDataSet(BASE64.decode(base64));
  }
  public static DataSet toDataSet(byte[] byteArray) throws IOException {
    DataSet ds = null;
    ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
    // check digest in leading buffer bytes
    synchronized (md) {
      byte[] digest = new byte[8];
      in.read(digest);
      md.reset();
      md.update(byteArray, digest.length, byteArray.length - digest.length);
      byte[] expectedDigest = md.digest();
      for (int i=0; i<digest.length; i++)
        if (digest[i] != expectedDigest[i])
          throw new IllegalArgumentException("packet data check failed");
    }
    int action = in.read();
    ds = new DataSet();
    ds.action = action;
    switch (action) {
      case ACTION_CONNECT_RQ :
        ds.remoteHostAddr = readString(in);
        ds.remoteHostPort = readInt(in);
        break;
      case ACTION_CONNECT_RP :
      case ACTION_DISCONNECT :
      case ACTION_PING :
      case ACTION_PONG :
      case ACTION_SEND_RECV :
      case ACTION_SEND_RECV_ASYNCH :
      case ACTION_NOT_CONNECTED :
        ds.connectionId = readInt(in);
        if (action == ACTION_CONNECT_RP || action == ACTION_NOT_CONNECTED)
          break;
        ds.sequenceId = readLong(in);
        if (action == ACTION_DISCONNECT)
          break;
        ds.data = readBytes(in);
        ds.chain = readDSList(in);
        ds.responseSequenceIdProcessed = readLong(in);
        ds.responseSequenceIdAvailable = readLong(in);
        break;
    }
    in.close();
    return ds;
  }

  public static void writeBytes(OutputStream out, byte[] bytes) throws IOException {
    if (bytes == null)
      out.write(0);
    else {
      out.write(1);
      int length = bytes.length;
      writeInt(out, length);
      if (length > 0)
        out.write(bytes);
    }
  }
  public static byte[] readBytes(InputStream in) throws IOException {
    int indicator = in.read();
    if (indicator == 0)
      return null;
    else {
      int length = readInt(in);
      byte[] bytes = new byte[length];
      if (length > 0) {
        // we must exactly read number of bytes specified
        int countRead = 0;
        while (countRead < length)
          countRead += in.read(bytes, countRead, length - countRead);
      }
      return bytes;
    }
  }

  public static void writeInt(OutputStream out, int i) throws IOException {
    out.write((i >> 24) & 0x00FF);
    out.write((i >> 16) & 0x00FF);
    out.write((i >>  8) & 0x00FF);
    out.write((i >>  0) & 0x00FF);
  }
  public static int readInt(InputStream in) throws IOException {
    int i = 0;
    i |= in.read() << 24;
    i |= in.read() << 16;
    i |= in.read() << 8;
    i |= in.read() << 0;
    return i;
  }

  public static void writeLong(OutputStream out, long i) throws IOException {
    out.write((int) ((i >> 56) & 0x00FF));
    out.write((int) ((i >> 48) & 0x00FF));
    out.write((int) ((i >> 40) & 0x00FF));
    out.write((int) ((i >> 32) & 0x00FF));
    out.write((int) ((i >> 24) & 0x00FF));
    out.write((int) ((i >> 16) & 0x00FF));
    out.write((int) ((i >>  8) & 0x00FF));
    out.write((int) ((i >>  0) & 0x00FF));
  }
  public static long readLong(InputStream in) throws IOException {
    long i = 0;
    i |= ((long) in.read()) << 56;
    i |= ((long) in.read()) << 48;
    i |= ((long) in.read()) << 40;
    i |= ((long) in.read()) << 32;
    i |= ((long) in.read()) << 24;
    i |= ((long) in.read()) << 16;
    i |= ((long) in.read()) << 8;
    i |= ((long) in.read()) << 0;
    return i;
  }

  public static void writeString(OutputStream out, String s) throws IOException {
    if (s == null)
      writeBytes(out, null);
    else
      writeBytes(out, s.getBytes());
  }
  public static String readString(InputStream in) throws IOException {
    byte[] bytes = readBytes(in);
    if (bytes == null)
      return null;
    else
      return new String(bytes);
  }

  public static void writeDSList(OutputStream out, ArrayList list) throws IOException {
    if (list == null)
      out.write(0);
    else {
      out.write(1);
      int length = list.size();
      writeInt(out, length);
      for (int i=0; i<length; i++) {
        byte[] bytes = ((DataSet) list.get(i)).toByteArray();
        writeBytes(out, bytes);
      }
    }
  }
  public static ArrayList readDSList(InputStream in) throws IOException {
    int indicator = in.read();
    if (indicator == 0)
      return null;
    else {
      int length = readInt(in);
      ArrayList list = new ArrayList();
      for (int i=0; i<length; i++) {
        byte[] bytes = readBytes(in);
        DataSet ds = DataSet.toDataSet(bytes);
        list.add(ds);
      }
      return list;
    }
  }

  public String toString() {
    return "[DataSet"
      + ": action = " + action
      + ", connectionId = " + connectionId
      + ", sequenceId = " + sequenceId
      + ", remoteHostAddr = " + remoteHostAddr
      + ", remoteHostPort = " + remoteHostPort
      + ", data = " + (data != null ? new String(data) : null)
      + ", chain = " + chain;
  }

  public boolean isContentPacket() {
    return  ( action == DataSet.ACTION_SEND_RECV ||
              action == DataSet.ACTION_SEND_RECV_ASYNCH
            )
            &&
            (data != null && data.length > 0);
  }
  public boolean isControlPacket() {
    return  ( action == DataSet.ACTION_CONNECT_RP ||
              action == DataSet.ACTION_CONNECT_RQ ||
              action == DataSet.ACTION_DISCONNECT ||
              action == DataSet.ACTION_NOT_CONNECTED
            );
  }
  public boolean isContentOrControlPacket() {
    return isContentPacket() || isControlPacket();
  }

  public boolean isClientResponseRequired() {
    return action == ACTION_SEND_RECV_ASYNCH;
  }
  public boolean isServerResponseRequired() {
    return action != ACTION_PONG;
  }

  public String getActionName() {
    String name = "";
    switch (action) {
      case ACTION_CONNECT_RQ :
        name = "connect request";
        break;
      case ACTION_CONNECT_RP :
        name = "connect reply";
        break;
      case ACTION_DISCONNECT :
        name = "disconnect";
        break;
      case ACTION_SEND_RECV :
        name = "send/recv";
        break;
      case ACTION_SEND_RECV_ASYNCH :
        name = "send/recv asynch";
        break;
      case ACTION_NOT_CONNECTED :
        name = "not connected";
        break;
      case ACTION_PING :
        name = "ping";
        break;
      case ACTION_PONG :
        name = "pong";
        break;
      default :
        name = "unknown";
        break;
    }
    return name;
  }

}
/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.cryptx;

import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 *
 * @author  Marcin Kurzawa
 */
public class BlockCipherOutputStream extends OutputStream {

  private transient Rijndael_CBC rijndael_CBC;
  private transient MessageDigest messageDigest;
  private transient OutputStream out;
  private transient int frameCount;

  private transient byte[] buf;
  private transient int bufPos;
  private static final int BLOCK_SIZE = Rijndael_CBC.BLOCK_SIZE;
  protected static final int BUF_SIZE = 255 * BLOCK_SIZE; // max 255 * due to 1 byte of length allowed

  private transient long totalTransmitedBytes;
  private transient long resetableByteCounter;

  // 1st byte : number of blocks
  // 2nd byte : number of bytes used in the last block
  // 3rd - 6th byte  : integer frame count
  // 7th - 22th byte : message digest (MD5 128 bit = 16 bytes) for purpose of error detection
  //--// 7th - 26th byte : message digest (SHA1 160 bit = 20 bytes)
  protected static final int HEADER_SIZE = 22;


  /** Creates new BlockCipherOutputStream */
  public BlockCipherOutputStream(OutputStream out, BASymmetricKey symmetricKey) throws InvalidKeyException, NoSuchAlgorithmException {
    this(out, symmetricKey.toByteArray());
  }


  /** Creates new BlockCipherOutputStream */
  private BlockCipherOutputStream(OutputStream out, byte[] keyMaterial) throws InvalidKeyException, NoSuchAlgorithmException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BlockCipherOutputStream.class, "BlockCipherOutputStream(OutputStream out, byte[] keyMaterial)");
    if (trace != null) trace.args(ArrayUtils.info(keyMaterial));

    this.out = out;
    this.rijndael_CBC = new Rijndael_CBC(Rijndael_CBC.ENCRYPT_STATE, keyMaterial);
    // MD5 for purpose of error detection ONLY, it is wrapped with AES(256) which is stronger than SHA-256 anyway.
    this.messageDigest = MessageDigest.getInstance("MD5");
    //this.messageDigest = MessageDigest.getInstance("SHA-1");
    this.buf = new byte[BUF_SIZE];
    this.bufPos = HEADER_SIZE;
    this.frameCount = 0;
    if (trace != null) trace.exit(BlockCipherOutputStream.class);
  }

  /** Write a byte to the output stream buffer */
  public void write(int b) throws IOException {
    if (bufPos == BUF_SIZE) {
      writeBatch();
    }
    buf[bufPos] = (byte) b;
    bufPos ++;
  }

//  /** Write a byte array to the output stream buffer */
//  public void write(byte[] b) throws IOException {
//    write(b, 0, b.length);
//  }

  /** Write a portion of an array tot he output stream buffer */
  public void write(byte[] b, int off, int len) throws IOException {
    if (len <= (BUF_SIZE - bufPos)) {
      System.arraycopy(b, off, buf, bufPos, len);
      bufPos += len;
    }
    else {
      int spaceLeft = BUF_SIZE - bufPos;
      System.arraycopy(b, off, buf, bufPos, spaceLeft);
      bufPos += spaceLeft;
      writeBatch();

      // copy the rest of the array -- recursively
      write(b, off+spaceLeft, len-spaceLeft);
    }
  }

  /** Write the buffer contents to the underlying output stream to create room for additional data */
  public void writeBatch() throws IOException {
    //Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BlockCipherOutputStream.class, "writeBatch()");
    // either empty or just the header filled, but no data

    if (bufPos <= HEADER_SIZE) {
      //if (trace != null) trace.exit(BlockCipherOutputStream.class);
      return;
    }

    //if (trace != null) trace.data(10, "plain data length="+(bufPos-HEADER_SIZE));

    int blocks = ((bufPos-1) / BLOCK_SIZE) + 1;
    int usedBytes = ((bufPos-1) % BLOCK_SIZE) + 1;

    // enter the header at the beginning of the buffer;
    buf[0] = (byte) blocks;
    buf[1] = (byte) usedBytes;
    // frame sequence number
    buf[2] = (byte) ((frameCount & 0xFF000000) >> 24);
    buf[3] = (byte) ((frameCount & 0x00FF0000) >> 16);
    buf[4] = (byte) ((frameCount & 0x0000FF00) >> 8);
    buf[5] = (byte) ((frameCount & 0x000000FF));
    frameCount ++;

    // message digest 
    messageDigest.update(buf, 0, 6);
    messageDigest.update(buf, HEADER_SIZE, bufPos-HEADER_SIZE);
    try {
      messageDigest.digest(buf, 6, HEADER_SIZE-6);
      /// messageDigest.digest(buf, 6, messageDigest.getDigestLength());
    } catch (DigestException e) {
      //if (trace != null) trace.exception(BlockCipherOutputStream.class, 20, e);
      throw new IOException("Could not produce a message digest!");
    }

    // cipher the required blocks
    /*
    for (int i=0; i<blocks; i++) {
      byte[] cBlock = BlockCipherAlgorithm.blockEncrypt(buf, i*BLOCK_SIZE, sessionKey);
      System.arraycopy(cBlock, 0, buf, i*BLOCK_SIZE, BLOCK_SIZE);
    }
    */
    // since buf is integral number of blocks, then buf array is long enough to handle the result
    //int bytesToWrite = BlockCipherAlgorithm.encrypt(buf, 0, bufPos, buf, 0, sessionKey);
    int bytesToWrite = blocks * BLOCK_SIZE;
    rijndael_CBC.update(buf, 0, buf, 0, bytesToWrite);

    out.write(buf, 0, bytesToWrite);

    // accounting
    totalTransmitedBytes += bytesToWrite;
    resetableByteCounter += bytesToWrite;

    // reset the position to the beginning (skipping the header)
    bufPos = HEADER_SIZE;
    //if (trace != null) trace.exit(BlockCipherOutputStream.class);
  }

  /** Write out the rest of the buffer and flush the underlying output stream */
  public void flush() throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BlockCipherOutputStream.class, "flush()");
    writeBatch();
    out.flush();
    if (trace != null) trace.exit(BlockCipherOutputStream.class);
  }

  /** Flushes and closes the stream, and frees up resources.  Closes the underlying output stream. */
  public void close() throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BlockCipherOutputStream.class, "close()");

    try {
      flush();
    } catch (IOException ioX) {
      if (trace != null) trace.data(50, "flush() failed, nothing to worry about, we will continue with close() normally.");
      if (trace != null) trace.exception(BlockCipherOutputStream.class, 51, ioX);
    }
    super.close();
    out.close();
    buf = null;
    rijndael_CBC = null;
    messageDigest = null;

    if (trace != null) trace.data(100, "Total bytes written=", new Long(totalTransmitedBytes));
    if (trace != null) trace.exit(BlockCipherOutputStream.class);
  }

  public long resetByteCounter() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BlockCipherOutputStream.class, "resetByteCounter()");
    long rc = resetableByteCounter;
    resetableByteCounter -= rc;
    if (trace != null) trace.exit(BlockCipherOutputStream.class, rc);
    return rc;
  }

  public long getByteCounter() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BlockCipherOutputStream.class, "getByteCounter()");
    long rc = resetableByteCounter;
    if (trace != null) trace.exit(BlockCipherOutputStream.class, rc);
    return rc;
  }
}
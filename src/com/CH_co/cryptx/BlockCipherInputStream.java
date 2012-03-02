/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_co.cryptx;

import java.security.InvalidKeyException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;


/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 *
 *
 * Class Details:   W A R N I N G :   BlockCipherInputStream DOES NOT support EOF marker.
 *
 *
 * <b>$Revision: 1.15 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class BlockCipherInputStream extends InputStream {

  private transient Rijndael_CBC rijndael_CBC;
  private transient MessageDigest messageDigest;
  private transient DataInputStream dataIn;
  private transient int frameCount;

  private transient byte[] buf;
  private transient int bufPos;
  private transient int bufBytesUsed;
  private static final int BLOCK_SIZE = Rijndael_CBC.BLOCK_SIZE;
  protected static final int BUF_SIZE = BlockCipherOutputStream.BUF_SIZE;

  private transient long totalTransmitedBytes;
  private transient long resetableByteCounter;

  // 1st byte : number of blocks
  // 2nd byte : number of bytes used in the last block
  // 3rd - 6th byte  : integer frame count
  // 7th - 22th byte : message digest (MD5 128 bit = 16 bytes) for purpose of error detection
  //--// 7th - 26th byte : message digest (SHA1 160 bit = 20 bytes)
  protected static final int HEADER_SIZE = BlockCipherOutputStream.HEADER_SIZE;


  /** Creates new BlockCipherInputStream */
  public BlockCipherInputStream(InputStream in, BASymmetricKey symmetricKey) throws InvalidKeyException, NoSuchAlgorithmException {
    this(in, symmetricKey.toByteArray());
  }


  /** Creates new BlockCipherInputStream */
  private BlockCipherInputStream(InputStream in, byte[] keyMaterial) throws InvalidKeyException, NoSuchAlgorithmException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BlockCipherInputStream.class, "BlockCipherInputStream(InputStream in, byte[] keyMaterial)");
    if (trace != null) trace.args(ArrayUtils.info(keyMaterial));

    this.dataIn = new DataInputStream(in);
    this.rijndael_CBC = new Rijndael_CBC(Rijndael_CBC.DECRYPT_STATE, keyMaterial);
    // MD5 for purpose of error detection ONLY, it is wrapped with AES(256) which is stronger than SHA-256 anyway.
    this.messageDigest = MessageDigest.getInstance("MD5");
    //this.messageDigest = MessageDigest.getInstance("SHA-1");
    this.buf = new byte[BUF_SIZE];
    this.bufPos = HEADER_SIZE;
    this.bufBytesUsed = HEADER_SIZE;
    this.frameCount = 0;
    if (trace != null) trace.exit(BlockCipherInputStream.class);
  }

  /** Read a single byte from the stream */
  public int read() throws IOException {
    if (bufPos >= bufBytesUsed)
      readBatch();
    int b = buf[bufPos] & 0x000000FF;
    bufPos++;
    return b;
  }

//  /** Read an array of bytes from the stream.
//      @return Number of bytes actually read.
//  */
//  public int read(byte[] b) throws IOException {
//    int available = available();
//    int len = b.length > available ? available : b.length;
//    //System.out.println("arr len="+b.length+", available="+available+", len="+len);
//    return read(b, 0, len);
//  }

  /** Read into a section of a buffer.
      @return Number of bytes actually read
  */
  public int read(byte[] b, int off, int len) throws IOException {
    //System.out.println("len2="+len);
    if (len == 0)
      return 0;
    if (bufPos >= bufBytesUsed)
      readBatch();
    if (len <= bufBytesUsed - bufPos) {
      System.arraycopy(buf, bufPos, b, off, len);
      bufPos += len;
      return len;
    }
    else {
      int bytesAvailable = bufBytesUsed - bufPos;
      System.arraycopy(buf, bufPos, b, off, bytesAvailable);
      bufPos += bytesAvailable;
      return bytesAvailable;
    }
  }

  /** Read a batch of data (next frame) from the underlying input stream.
      @return Number of bytes in the frame read.
  */
  private int readBatch() throws IOException {
    //Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BlockCipherInputStream.class, "readBatch()");
    // read the first block where the header is;
    dataIn.readFully(buf, 0, BLOCK_SIZE);

    // accounting
    totalTransmitedBytes += BLOCK_SIZE;
    resetableByteCounter += BLOCK_SIZE;

    // decode the header;
    rijndael_CBC.update(buf, 0, buf, 0, BLOCK_SIZE);
    //BlockCipherAlgorithm.blockDecrypt(buf, 0, buf, 0, sessionKey);

    // get total number of blocks in transmition
    int numBlocks = buf[0] & 0x000000FF;
    if (numBlocks <= 0) {
      throw new IOException("Number of block is negative!");
    }

    //if (trace != null) trace.data(10, "cipher blocks="+numBlocks);

    // get number of bytes used in the last block
    int usedBytes = buf[1];

    // check the frame sequence count
    int frameNum;
    frameNum  = (buf[2] & 0x000000FF) << 24;
    frameNum |= (buf[3] & 0x000000FF) << 16;
    frameNum |= (buf[4] & 0x000000FF) << 8;
    frameNum |= (buf[5] & 0x000000FF);
    if (frameNum != frameCount) {
      throw new IOException("Frame number is out of sequence!  Expecting frame # " + frameCount + ", but received frame # " + frameNum + "!");
    }
    frameCount ++;

    // read the rest of the blocks;
    int outstandingBytes = BLOCK_SIZE*(numBlocks-1);
    if (outstandingBytes > 0) {
      dataIn.readFully(buf, BLOCK_SIZE, outstandingBytes);

      // accounting
      totalTransmitedBytes += outstandingBytes;
      resetableByteCounter += outstandingBytes;
    }

    // decode the rest of the blocks
    /*
    for (int i=1; i<numBlocks; i++) {
      byte tBlock = BlockCipherAlgorithm.blockDecrypt(buf, i*BLOCK_SIZE, sessionKey);
      System.arraycopy(tBlock, 0, buf, i*BLOCK_SIZE, BLOCK_SIZE);  
    }
    */

    // since buf is integral number of blocks, then buf array is long enough to handle the result
    rijndael_CBC.update(buf, BLOCK_SIZE, buf, BLOCK_SIZE, outstandingBytes);
    //BlockCipherAlgorithm.decrypt(buf, BLOCK_SIZE, outstandingBytes, buf, BLOCK_SIZE, sessionKey);


    bufPos = HEADER_SIZE;
    bufBytesUsed = numBlocks * BLOCK_SIZE - (BLOCK_SIZE-usedBytes);

    // check the message digest 
    messageDigest.update(buf, 0, 6);
    messageDigest.update(buf, HEADER_SIZE, bufBytesUsed-HEADER_SIZE);
    byte[] digest = messageDigest.digest();
    for (int k=0; k<digest.length; k++) {
      if (digest[k] != buf[6+k]) {
        throw new IOException("Message digest is different than original one!  Possible message tampering!");
      }
    }

    //if (trace != null) trace.data(30, "plain data length="+(bufBytesUsed-bufPos));

    //if (trace != null) trace.exit(BlockCipherInputStream.class, bufBytesUsed);
    return bufBytesUsed;
  }

  /** Skip over 'n' bytes.
      @return Number of bytes actually skipped.
  */
  public long skip(long n) throws IOException {
    if (n <= bufBytesUsed - bufPos) {
      bufPos += n;
      return n;
    }
    else {
      int bytesAvailable = bufBytesUsed - bufPos;
      bufPos += bytesAvailable;
      readBatch();

      return bytesAvailable + skip(n-bytesAvailable);
    }
  }

  /** @return number of bytes available for reading without blocking */
  public int available() throws IOException {
    if (bufPos >= bufBytesUsed && dataIn.available()>0)
      readBatch();
    return bufBytesUsed - bufPos;
  }

  /** Close the stream, free up resources.  Closes the underlying output stream. */
  public void close() throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BlockCipherInputStream.class, "close()");
    super.close();
    dataIn.close();
    buf = null;
    rijndael_CBC = null;
    messageDigest = null;

    if (trace != null) trace.data(100, "Total bytes read=", new Long(totalTransmitedBytes));
    if (trace != null) trace.exit(BlockCipherInputStream.class);
  }

  public long resetByteCounter() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BlockCipherInputStream.class, "resetByteCounter()");
    long rc = resetableByteCounter;
    resetableByteCounter -= rc;
    if (trace != null) trace.exit(BlockCipherInputStream.class, rc);
    return rc;
  }

  public long getByteCounter() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BlockCipherInputStream.class, "getByteCounter()");
    long rc = resetableByteCounter;
    if (trace != null) trace.exit(BlockCipherInputStream.class, rc);
    return rc;
  }

}
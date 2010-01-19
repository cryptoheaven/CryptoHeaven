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

package com.CH_co.cryptx;

import java.io.*;
import java.security.*;

import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 *
 * Symmetric bulk cipher can handle data length of (integer blocks * 16 bytes per block = hudge!)
 * @author  Marcin Kurzawa
 * @version 
 */
public class SymmetricBulkCipher extends Object {

  private transient byte[] key;
  private transient MessageDigest messageDigest;
  private static final int BLOCK_SIZE = Rijndael_CBC.BLOCK_SIZE;

  // 1st - 4th byte  : integer number of blocks
  // 5th byte        : number of bytes used in the last block
  // 6th - 21st byte : message digest (MD5 128 bit = 16 bytes) for purpose of error detection
  //--// 6th - 25st byte : message digest (SHA-1 160 bit = 20 bytes)
  protected static final int HEADER_SIZE = 21;

  /** Creates new SymmetricBulkCipher */
  public SymmetricBulkCipher(BASymmetricKey symmetricKey) throws NoSuchAlgorithmException {
    this(symmetricKey.toByteArray());
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SymmetricBulkCipher.class, "SymmetricBulkCipher(BASymmetricKey symmetricKey)");
    if (trace != null) trace.exit(SymmetricBulkCipher.class);
  }

  /** Creates new SymmetricBulkCipher */
  private SymmetricBulkCipher(byte[] keyMaterial) throws NoSuchAlgorithmException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SymmetricBulkCipher.class, "SymmetricBulkCipher(byte[] keyMaterial)");
    //this.sessionKey = BlockCipherAlgorithm.makeKey(keyMaterial);
    this.key = keyMaterial;
    //this.messageDigest = MessageDigest.getInstance("MD5");
    this.messageDigest = MessageDigest.getInstance("MD5");
    if (trace != null) trace.exit(SymmetricBulkCipher.class);
  }

  public BASymCipherBulk bulkEncrypt(BA byteArray) throws DigestException, InvalidKeyException {
    byte[] plainBuf = byteArray.toByteArray();
    return new BASymCipherBulk(bulkEncrypt(plainBuf, 0, plainBuf.length));
  }
  public BASymCipherBulk bulkEncrypt(String plainString) throws DigestException, InvalidKeyException {
    byte[] plainBuf = null;
    plainBuf = Misc.convStrToBytes(plainString);
    return new BASymCipherBulk(bulkEncrypt(plainBuf, 0, plainBuf.length));
  }

  public BASymPlainBulk bulkDecrypt(BASymCipherBulk cipherBulk) throws DigestException, InvalidKeyException {
    byte[] cipherBuf = cipherBulk.toByteArray();
    return new BASymPlainBulk(bulkDecrypt(cipherBuf, 0, cipherBuf.length));
  }


  /** Encrypt a block of data symmetrically with message digest */
  public byte[] bulkEncrypt(byte[] buf, int off, int len) throws DigestException, InvalidKeyException {
    int blocks = ((len+HEADER_SIZE-1) / BLOCK_SIZE) + 1;
    int length = blocks * BLOCK_SIZE;

    // write number of blocks
    byte[] cipherBlock = new byte[length];
    cipherBlock[0] = (byte) ((blocks & 0xFF000000) >> 24);
    cipherBlock[1] = (byte) ((blocks & 0x00FF0000) >> 16);
    cipherBlock[2] = (byte) ((blocks & 0x0000FF00) >> 8);
    cipherBlock[3] = (byte) ((blocks & 0x000000FF) >> 0);

    // write number of bytes in last block
    cipherBlock[4] = (byte) (((len + HEADER_SIZE - 1 ) % BLOCK_SIZE) + 1);

    // write data to the array
    System.arraycopy(buf, off, cipherBlock, HEADER_SIZE, len);

    // random filling from the end of data to end of buffer
    int fillLength = length - HEADER_SIZE - len;
    if (fillLength > 0) {
      byte[] randomBytes = new byte[fillLength];
      Rnd.getSecureRandom().nextBytes(randomBytes);
      System.arraycopy(randomBytes, 0, cipherBlock, length-fillLength, fillLength);
    }

    // create a message digest of everything except for the space meant for the digest
    messageDigest.update(cipherBlock, 0, 5);
    messageDigest.update(cipherBlock, HEADER_SIZE, length-HEADER_SIZE);
    messageDigest.digest(cipherBlock, 5, HEADER_SIZE-5);

    // cipher the entire data with header information
    /*
    for (int i=0; i<blocks; i++) {
      byte[] cBlock = BlockCipherAlgorithm.blockEncrypt(cipherBlock, i*BLOCK_SIZE, sessionKey);
      System.arraycopy(cBlock, 0, cipherBlock, i*BLOCK_SIZE, BLOCK_SIZE);
    }
    */
    //BlockCipherAlgorithm.encrypt(cipherBlock, 0, length, cipherBlock, 0, sessionKey);
    new Rijndael_CBC(Rijndael_CBC.ENCRYPT_STATE, key).doFinal(cipherBlock, 0, cipherBlock, 0, length);


    return cipherBlock;
  }

  /** Decrypts a block of data (a frame) from the original buffer in ranges specified by 'off' and 'len' */
  public byte[] bulkDecrypt(byte[] buf, int off, int len) throws DigestException, InvalidKeyException {
    int blocks = len / BLOCK_SIZE;

    byte[] tempBuf = new byte[len];

    /*
    for (int i=0; i<blocks; i++) {
      byte[] tBlock = BlockCipherAlgorithm.blockDecrypt(buf, off+(i*BLOCK_SIZE), sessionKey);
      System.arraycopy(tBlock, 0, tempBuf, i*BLOCK_SIZE, BLOCK_SIZE);
    }
    */
    // tempBuf is integral number of blocks and should be long enough to handle the result
    //BlockCipherAlgorithm.decrypt(buf, off, len, tempBuf, 0, sessionKey);
    new Rijndael_CBC(Rijndael_CBC.DECRYPT_STATE, key).doFinal(buf, off, tempBuf, 0, len);

    // verify message digest
    messageDigest.update(tempBuf, 0, 5);
    messageDigest.update(tempBuf, HEADER_SIZE, len-HEADER_SIZE);
    byte[] digest = messageDigest.digest();
    for (int k=0; k<digest.length; k++) {
      if (digest[k] != tempBuf[off+5+k])
        throw new DigestException("Message digest is different than original one!  Possible message tampering!");
    }

    // retrieve the number of blocks from the frame
    int numBlocks;
    numBlocks  = (tempBuf[0] & 0x000000FF) << 24;
    numBlocks |= (tempBuf[1] & 0x000000FF) << 16;
    numBlocks |= (tempBuf[2] & 0x000000FF) << 8;
    numBlocks |= (tempBuf[3] & 0x000000FF) << 0;
    if (blocks != numBlocks)
      throw new DigestException("Number of blocks processed does not equal number of blocks in the frame!");

    // retrieve number of bytes in the last block
    int lastBytes = tempBuf[4];

    // calculate length of real data content
    int dataLength = (numBlocks*BLOCK_SIZE) - HEADER_SIZE - (BLOCK_SIZE-lastBytes);

    // create storage for the decrypted contents, without header
    byte[] textBlock = new byte[dataLength];
    System.arraycopy(tempBuf, HEADER_SIZE, textBlock, 0, dataLength);

    return textBlock;
  }
}
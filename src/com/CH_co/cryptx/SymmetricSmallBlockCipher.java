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

/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 *
 * Ciphers small blocks of up to 'short int' number of blocks (short * 16 bytes = 1,048,576 bytes).
 * Without message digest.
 * @author  Marcin Kurzawa
 * @version 
 */
public class SymmetricSmallBlockCipher extends Object {

  private transient byte[] key;
  private static final int BLOCK_SIZE = Rijndael_CBC.BLOCK_SIZE;

  // 1st - 2nd byte  : short integer describing number of blocks
  // 3rd byte        : number of bytes used in the last block
  protected static final int HEADER_SIZE = 3;

  /** Creates new SymmetricSmallBlockCipher */
  public SymmetricSmallBlockCipher(byte[] keyMaterial) {
    //this.sessionKey = BlockCipherAlgorithm.makeKey(keyMaterial);
    this.key = keyMaterial;
  }
  /** Creates new SymmetricSmallBlockCipher */
  public SymmetricSmallBlockCipher(BA keyMaterial) {
    this(keyMaterial.toByteArray());
  }

  public BASymCipherBlock blockEncrypt(BA byteArray) throws InvalidKeyException {
    byte[] plainBuf = byteArray.toByteArray();
    return new BASymCipherBlock(blockEncrypt(plainBuf, 0, plainBuf.length));
  }
  public BASymPlainBlock blockDecrypt(BASymCipherBlock cipherBlock) throws InvalidKeyException {
    byte[] cipherBuf = cipherBlock.toByteArray();
    return new BASymPlainBlock(blockDecrypt(cipherBuf, 0, cipherBuf.length));
  }


  public byte[] blockEncrypt(byte[] buf) throws InvalidKeyException {
    return blockEncrypt(buf, 0, buf.length);
  }

  public byte[] blockDecrypt(byte[] buf) throws InvalidKeyException {
    return blockDecrypt(buf, 0, buf.length);
  }

  /** Encrypt a block of data symmetrically with message digest */
  public byte[] blockEncrypt(byte[] buf, int off, int len) throws InvalidKeyException {
    int blocks = ((len+HEADER_SIZE-1) / BLOCK_SIZE) + 1;
    int length = blocks * BLOCK_SIZE;

    if ((blocks & 0x0000FFFF) != blocks)
      throw new IllegalArgumentException("Number of resulting blocks would be too large!");

    // write number of blocks
    byte[] cipherBlock = new byte[length];
    cipherBlock[0] = (byte) ((blocks & 0x0000FF00) >> 8);
    cipherBlock[1] = (byte) ((blocks & 0x000000FF) >> 0);

    // write number of bytes in last block
    cipherBlock[2] = (byte) (((len + HEADER_SIZE - 1 ) % BLOCK_SIZE) + 1);

    // write data to the array
    System.arraycopy(buf, off, cipherBlock, HEADER_SIZE, len);

    // random filling from the end of data to end of buffer
    int fillLength = length - HEADER_SIZE - len;
    if (fillLength > 0) {
      byte[] randomBytes = new byte[fillLength];
      Rnd.getSecureRandom().nextBytes(randomBytes);
      System.arraycopy(randomBytes, 0, cipherBlock, length-fillLength, fillLength);
    }

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
  public byte[] blockDecrypt(byte[] buf, int off, int len) throws InvalidKeyException {
    int blocks = len / BLOCK_SIZE;

    byte[] tempBuf = new byte[len];

    /*
    for (int i=0; i<blocks; i++) {
      byte[] tBlock = BlockCipherAlgorithm.blockDecrypt(buf, off+(i*BLOCK_SIZE), sessionKey);
      System.arraycopy(tBlock, 0, tempBuf, i*BLOCK_SIZE, BLOCK_SIZE);
    }
    */
    //BlockCipherAlgorithm.decrypt(buf, off, len, tempBuf, 0, sessionKey);
    new Rijndael_CBC(Rijndael_CBC.DECRYPT_STATE, key).doFinal(buf, off, tempBuf, 0, len);

    // retrieve the number of blocks from the frame
    int numBlocks;
    numBlocks  = (tempBuf[0] & 0x000000FF) << 8;
    numBlocks |= (tempBuf[1] & 0x000000FF) << 0;
    if (blocks != numBlocks)
      throw new IllegalArgumentException("Number of blocks processed does not equal number of blocks in the frame!");

    // retrieve number of bytes in the last block
    int lastBytes = tempBuf[2];

    // calculate length of real data content
    int dataLength = (numBlocks*BLOCK_SIZE) - HEADER_SIZE - (BLOCK_SIZE-lastBytes);

    // create storage for the decrypted contents, without header
    byte[] textBlock = new byte[dataLength];
    System.arraycopy(tempBuf, HEADER_SIZE, textBlock, 0, dataLength);

    return textBlock;
  }
}
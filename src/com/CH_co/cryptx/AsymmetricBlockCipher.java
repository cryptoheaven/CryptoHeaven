/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
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

import java.util.Random;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.DigestException;
import java.math.BigInteger;

import com.CH_co.trace.Trace;

/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class AsymmetricBlockCipher extends Object {

  // byte 1     : 0
  // byte 2-3   : short integer for plainData length
  // byte 4-23  : message digest (SHA-1 160 bits : 20 bytes)
  // byte 24-x  : filler bytes
  // byte x-end : message data
  public static final int HEADER_SIZE = 23;
  private Random random;
  private MessageDigest messageDigest;

  /** Creates new AsymmetricBlockCipher */
  public AsymmetricBlockCipher(Random random) throws NoSuchAlgorithmException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AsymmetricBlockCipher.class, "AsymmetricBlockCipher(Random random)");
    this.random = random;
    //this.messageDigest = MessageDigest.getInstance("MD5");
    this.messageDigest = MessageDigest.getInstance("SHA-1");
    if (trace != null) trace.exit(AsymmetricBlockCipher.class);
  }

  /** Creates new AsymmetricBlockCipher */
  public AsymmetricBlockCipher() throws NoSuchAlgorithmException {
    this(Rnd.getSecureRandom());
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AsymmetricBlockCipher.class, "AsymmetricBlockCipher()");
    if (trace != null) trace.exit(AsymmetricBlockCipher.class);
  }


  /** 
   * Encrypt a small block of data and internally sign with a message digest.
   * Use random padding when block is shorter than maximum encryptable block size for a given public key.
   * @return the encrypted cipher block
   */
  public BAAsyCipherBlock blockEncrypt(RSAPublicKey publicKey, byte[] plainData) throws IllegalArgumentException, DigestException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AsymmetricBlockCipher.class, "blockEncrypt(RSAPublicKey publicKey, byte[] plainData)");
    BAAsyCipherBlock ba = blockEncrypt(publicKey, plainData, true);
    if (trace != null) trace.exit(AsymmetricBlockCipher.class, ba);
    return ba;
  }
  public BAAsyCipherBlock blockEncrypt(RSAPublicKey publicKey, byte[] plainData, boolean withDigest) throws IllegalArgumentException, DigestException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AsymmetricBlockCipher.class, "blockEncrypt(RSAPublicKey publicKey, byte[] plainData, boolean withDigest)");
    if (trace != null) trace.args(withDigest);
    BigInteger plainInt = prepareEncryptionBlock(publicKey, plainData, withDigest);
    // encryption is an act of encryption of plain data with a public key
    BAAsyCipherBlock ba = new BAAsyCipherBlock(encrypt(publicKey, plainInt).toByteArray());
    if (trace != null) trace.exit(AsymmetricBlockCipher.class, ba);
    return ba;
  }

  /**
   * Decrypt a block which was encrypted with a pair public key, verify its internal digest.
   * @return original plain data
   */
  public BAAsyPlainBlock blockDecrypt(RSAPrivateKey privateKey, byte[] cipherData) throws DigestException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AsymmetricBlockCipher.class, "blockDecrypt(RSAPrivateKey privateKey, byte[] cipherData)");
    BAAsyPlainBlock ba = blockDecrypt(privateKey, cipherData, true);
    if (trace != null) trace.exit(AsymmetricBlockCipher.class, ba);
    return ba;
  }
  public BAAsyPlainBlock blockDecrypt(RSAPrivateKey privateKey, byte[] cipherData, boolean withDigest) throws DigestException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AsymmetricBlockCipher.class, "blockDecrypt(RSAPrivateKey privateKey, byte[] cipherData, boolean withDigest)");
    if (trace != null) trace.args(withDigest);
    BigInteger cipherInt = new BigInteger(1, cipherData);
    byte[] plainBlock = decrypt(privateKey, cipherInt).toByteArray();
    // verify message digest and get the original data part without random fillings
    BAAsyPlainBlock ba = new BAAsyPlainBlock(verifyDecryptionBlock(plainBlock, withDigest));
    if (trace != null) trace.exit(AsymmetricBlockCipher.class, ba);
    return ba;
  }



  /**
   * Sign a short block of data with the given private key.
   * Use random padding when block is shorter than maximum signable block for a given key.
   * Use internal message digest.
   * @return the encrypted cipher block
   */
  public BAAsyCipherBlock signBlock(RSAPrivateKey privateKey, byte[] plainData) throws DigestException, IllegalArgumentException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AsymmetricBlockCipher.class, "signBlock(RSAPrivateKey privateKey, byte[] plainData)");
    BigInteger plainInt = prepareEncryptionBlock(privateKey, plainData, true);
    // signing is an act of decryption of plain data with a private key
    BAAsyCipherBlock ba = new BAAsyCipherBlock(decrypt(privateKey, plainInt).toByteArray());
    if (trace != null) trace.exit(AsymmetricBlockCipher.class, ba);
    return ba;
  }


  /** 
   * Verify the signature with a given public key.  Check the internal message digest.
   * @return the original block which was signed.
   */
  public BAAsyPlainBlock verifySignature(RSAPublicKey publicKey, byte[] cipherData) throws DigestException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AsymmetricBlockCipher.class, "verifySignature(RSAPublicKey publicKey, byte[] cipherData)");
    BigInteger cipherInt = new BigInteger(1, cipherData);
    // verification of a signature is an act of encryption of the cipher data with the public key to yield the plain data 
    byte[] plainBlock = encrypt(publicKey, cipherInt).toByteArray();
    BAAsyPlainBlock ba = new BAAsyPlainBlock(verifyDecryptionBlock(plainBlock, true));
    if (trace != null) trace.exit(AsymmetricBlockCipher.class, ba);
    return ba;
  }


  /** 
   * Produces a structured block ready for encryption.
   * Embedes a message digest, and random fillings.
   * @param withDigest specifies if a digest should be placed into the block
   * @return BigInteger ready for encryption.
   */
  private BigInteger prepareEncryptionBlock(RSAKey key, byte[] plainData, boolean withDigest) throws DigestException, IllegalArgumentException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AsymmetricBlockCipher.class, "prepareEncryptionBlock(RSAKey key, byte[] plainData, boolean withDigest)");
    if (trace != null) trace.args(withDigest);

    byte[] plainBlock = new byte[key.getMaxBlock()];

    // check if plainData is not too long to accomodate it in the encryption block
    if (plainBlock.length < (plainData.length + HEADER_SIZE))
      throw new IllegalArgumentException("Plain data block of length " + plainData.length + " and header of " + HEADER_SIZE + " bytes is collectively too long to accomodate encryption using the given key capable of only " + key.getMaxBlock() + " bytes maximum block length!");

    plainBlock[0] = 1;  // no leading empty bytes -- so that BigInteger doesn't have to be longer than our plainBlock and decrypted block does not shorten
    plainBlock[1] = (byte) ((plainData.length & 0x0000FF00) >> 8);
    plainBlock[2] = (byte) ((plainData.length & 0x000000FF) >> 0);

    // random filling
    int fillLength = plainBlock.length - HEADER_SIZE - plainData.length;
    if (fillLength > 0) {
      byte[] randomBytes = new byte[fillLength];
      random.nextBytes(randomBytes);
      System.arraycopy(randomBytes, 0, plainBlock, HEADER_SIZE, fillLength);
    }

    // plainData
    System.arraycopy(plainData, 0, plainBlock, HEADER_SIZE+fillLength, plainData.length);

    // messageDigest
    if (withDigest) {
      messageDigest.reset();
      messageDigest.update(plainBlock, 0, 3);
      messageDigest.update(plainBlock, HEADER_SIZE, fillLength+plainData.length);
      messageDigest.digest(plainBlock, 3, messageDigest.getDigestLength());
    }
    else {
      // no digest, just fill the space meant for digest with random values
      byte[] randomBytes = new byte[20];
      random.nextBytes(randomBytes);
      System.arraycopy(randomBytes, 0, plainBlock, 3, randomBytes.length);
    }

    // return the entire plainBlock as a BigInteger
    BigInteger bi = new BigInteger(1, plainBlock);

    if (trace != null) trace.exit(AsymmetricBlockCipher.class, "BigInteger");
    return bi;
  }



  /** 
   * Check the structure of the decrypted message.
   * Verifies message digest, discarts random fillings.
   * @param withDigest specifies if the digest should also be checked
   * @return the original data part
   */
  private byte[] verifyDecryptionBlock(byte[] plainBlock, boolean withDigest) throws DigestException { 
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AsymmetricBlockCipher.class, "verifyDecryptionBlock(byte[] plainBlock, boolean withDigest)");
    if (trace != null) trace.args(withDigest);

    // recover the original data length
    int originalDataLength = 0;
    originalDataLength  = (plainBlock[1] & 0x000000FF) << 8;
    originalDataLength |= (plainBlock[2] & 0x000000FF) << 0;

    //if (trace != null) trace.data(10, originalDataLength);

    // verify message digest
    if (withDigest) {
      messageDigest.reset();
      messageDigest.update(plainBlock, 0, 3);
      messageDigest.update(plainBlock, HEADER_SIZE, plainBlock.length-HEADER_SIZE);
      byte[] digest = messageDigest.digest();
      for (int k=0; k<digest.length; k++) {
        if (digest[k] != plainBlock[3+k]) {
          throw new DigestException("Message digest is different than original one!  Possible message tampering!");
          /*
          byte[] dig = new byte[digest.length];
          System.arraycopy(plainBlock, 3, dig, 0, digest.length);
          String s1 = com.CH_co.util.Misc.objToStr(digest);
          String s2 = com.CH_co.util.Misc.objToStr(dig);
          throw new DigestException("Message digest is different than original one!  Possible message tampering!  \n\nCalculated and claimed digests are:\n" + s1 + "\n" + s2);
           */
        }
      }
    }

    // get the original data
    byte[] originalData = new byte[originalDataLength];
    System.arraycopy(plainBlock, plainBlock.length-originalDataLength, originalData, 0, originalDataLength);

    if (trace != null) trace.exit(AsymmetricBlockCipher.class, "originalData");
    return originalData;
  }


  /** Pure BigInteger encryption */
  private BigInteger encrypt(RSAPublicKey publicKey, BigInteger plainInt) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AsymmetricBlockCipher.class, "encrypt(RSAPublicKey publicKey, BigInteger plainInt)");
    BigInteger bi = plainInt.modPow(publicKey.getPublicExponent(), publicKey.getModulus());
    if (trace != null) trace.exit(AsymmetricBlockCipher.class, "BigInteger");
    return bi;
  }

  /** Pure BigInteger decryption */
  private BigInteger decrypt(RSAPrivateKey privateKey, BigInteger cipherInt) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AsymmetricBlockCipher.class, "decrypt(RSAPrivateKey privateKey, BigInteger cipherInt)");

    // decrypt using Chineese remainder theorem
    BigInteger p = privateKey.getPrimeP();
    BigInteger q = privateKey.getPrimeQ();
    BigInteger dP = privateKey.getPrimeExponentP();
    BigInteger dQ = privateKey.getPrimeExponentQ();
    BigInteger qInv = privateKey.getCrtCoefficient();

    BigInteger mP, mQ, tmp;

    // mP = ((data mod p) ^ dP)) mod p
    mP = (cipherInt.remainder(p)).modPow(dP, p);

    // mQ = ((data mod q) ^ dQ)) mod q
    mQ = (cipherInt.remainder(q)).modPow(dQ, q);

    tmp = mP.subtract(mQ);
    tmp = tmp.multiply(qInv);
    tmp = tmp.mod(p);   // mod (in Java) returns the positive residual

    tmp = tmp.multiply(q);
    tmp = tmp.add(mQ);

    if (trace != null) trace.exit(AsymmetricBlockCipher.class, "BigInteger");
    return tmp;
  }

}
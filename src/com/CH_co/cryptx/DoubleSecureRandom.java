/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.cryptx;

import java.security.SecureRandom;

/**
* Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
*
* Class Description:
* 
* Secure Random generator which produces strong CSPRNG random values by XORing
* two independent sources of randomness.  One from the passed in SecureRandom, and
* second by chaining hashes of the SHA256 secure hash.
*
* Class Details:
*
* Secure Random generator that XORs the output from one SecureRandom with 
* chaining hashes of secure hash function.
* Chaining is based on initializing hash function with last output, then
* updating it with XOR value of new SecureRandom bytes and last hash.
*
* <b>$Revision: 1.1 $</b>
*
* @author  Marcin Kurzawa
*/
public class DoubleSecureRandom extends SecureRandom {

  private transient SecureRandom randomSource;
  private transient SHA256 hasher;
  private transient byte[] lastHash;
  private transient int counter;

  public DoubleSecureRandom(SecureRandom random) {
    randomSource = random;
    hasher = new SHA256();
    reset();
  }

  private void reset() {
    byte[] randomBytes = new byte[1024];
    randomSource.nextBytes(randomBytes);
    lastHash = hasher.digest(randomBytes);
    for (int i=0; i<randomBytes.length; i++)
      randomBytes[i] = 0;
    counter = 0;
  }

  public synchronized void setSeed(byte[] seed) {
    if (randomSource != null) {
      randomSource.setSeed(seed);
      reset();
    }
  }

  public synchronized void setSeed(long seed) {
    if (randomSource != null) {
      randomSource.setSeed(seed);
      reset();
    }
  }

  public byte[] generateSeed(int numBytes) {
    byte[] bytes = new byte[numBytes];
    randomSource.nextBytes(bytes);
    return bytes;
  }

  /**
  * Generates a user-specified number of random bytes.
  *
  * <p> If a call to <code>setSeed</code> had not occurred previously,
  * the first call to this method forces this SecureRandom object
  * to seed itself.  This self-seeding will not occur if
  * <code>setSeed</code> was previously called.
  *
  * @param bytes the array to be filled in with random bytes.
  */
  public synchronized void nextBytes(byte[] bytes) {
    // get the initial random bytes from random source
    randomSource.nextBytes(bytes);
    // XOR them with second random source
    int nextByte = 0;
    while (nextByte < bytes.length) {
      // init the hasher using its last output
      hasher.update(lastHash);
      // XOR new bytes with last hash to produce new input bytes
      int blockSize = Math.min(lastHash.length, bytes.length - nextByte);
      for (int i=0; i<blockSize; i++) {
        // last hash becomes a holder for additional entropy from secure random bytes
        lastHash[i] ^= bytes[nextByte+i];
      }
      // update the hasher with new bytes XORed with last hash to keep the full block of data
      hasher.update(lastHash);
      // get the new hash
      lastHash = hasher.digest();
      // XOR the result with output from other independent SecureRandom generator
      for (int i=0; i<blockSize; i++) {
        bytes[nextByte+i] ^= lastHash[i];
      }
      nextByte += blockSize;
      // increment hasher digest counter and reset periodically
      if (counter < Integer.MAX_VALUE)
        counter ++;
      else
        reset();
    }
  }

}
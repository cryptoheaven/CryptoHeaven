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

import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import java.security.SecureRandom;

/** 
* Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.11 $</b>
*
* @author  Marcin Kurzawa
*/
public class BASymmetricKey extends BA {

  /**
  * Creates new SymmetricKey of specified length with randomly picked bytes.
  */
  public BASymmetricKey(int length) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BASymmetricKey.class, "BASymmetricKey(int length)");

    SecureRandom rnd = Rnd.getSecureRandom();
    SHA256 sha = new SHA256();

    // random bytes from Secure generator
    byte[] rndSrc = new byte[length];
    rnd.nextBytes(rndSrc);

    // make special random key by condensing additional random bytes through Secure Hash function in multiple rounds
    byte[] rndKey = new byte[length];

    for (int round=0; round<4; round++) {
      // each round will XOR result from previous rounds
      int bytesFilled = 0;
      byte[] rndBytes = new byte[256];
      while (bytesFilled < length) {
        // take 256 random bytes
        rnd.nextBytes(rndBytes);
        // condense them into 32 bytes using SHA256
        byte[] rndSha = sha.digest(rndBytes);
        // clean the obsolete random source byte array
        for (int i=0; i<rndBytes.length; i++)
          rndBytes[i] = 0;
        // continue filling the key-material with output from SHA256
        int toFill = Math.min(rndSha.length, rndKey.length - bytesFilled);
        for (int i=0; i<toFill; i++) {
          rndKey[bytesFilled+i] ^= rndSha[i];
          // clean the obsolete digest byte array
          rndSha[i] = 0;
        }
        bytesFilled += toFill;
      }
    }

    // resulting random key is the XOR of straight secure random bytes with our special secure random key
    for (int i=0; i<rndKey.length; i++) {
      rndKey[i] ^= rndSrc[i];
      // clean the obsolete byte array rndSrc
      rndSrc[i] = 0;
    }
    setContent(rndKey);

    if (trace != null) trace.exit(BASymmetricKey.class);
  }

  /**
  * Creates new SymmetricKey with specified raw key material.
  */
  public BASymmetricKey(byte[] keyMaterial) {
    super(keyMaterial);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BASymmetricKey.class, "BASymmetricKey(byte[])");
    if (trace != null) trace.exit(BASymmetricKey.class);
  }

  /**
  * Creates new SymmetricKey with specified raw key material.
  */
  public BASymmetricKey(byte[] keySource,int offset,int length) {
    super(keySource, offset, length);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BASymmetricKey.class, "BASymmetricKey(byte[], int offset, int length)");
    if (trace != null) trace.exit(BASymmetricKey.class);
  }


  /**
  * Creates new SymmetricKey with specified raw key material.
  */
  public BASymmetricKey(BA keyMaterial) {
    super(keyMaterial.toByteArray());
  }

  public static void main(String[] args) {
    BASymmetricKey key = null;
    System.out.println("Making 32-byte key");
    key = new BASymmetricKey(32);
    System.out.println("key="+ArrayUtils.toString(key.toByteArray()));
    System.out.println("Making 64-byte key");
    key = new BASymmetricKey(64);
    System.out.println("key="+ArrayUtils.toString(key.toByteArray()));
    System.out.println("Making 111-byte key");
    key = new BASymmetricKey(111);
    System.out.println("key="+ArrayUtils.toString(key.toByteArray()));
    System.out.println("Making 1-byte key");
    key = new BASymmetricKey(1);
    System.out.println("key="+ArrayUtils.toString(key.toByteArray()));
    System.out.println("Making 16-byte key");
    key = new BASymmetricKey(16);
    System.out.println("key="+ArrayUtils.toString(key.toByteArray()));
    System.out.println("Making 31-byte key");
    key = new BASymmetricKey(31);
    System.out.println("key="+ArrayUtils.toString(key.toByteArray()));
    System.out.println("Making 33-byte key");
    key = new BASymmetricKey(33);
    System.out.println("key="+ArrayUtils.toString(key.toByteArray()));
    System.out.println("Making 0-byte key");
    key = new BASymmetricKey(0);
    System.out.println("key="+ArrayUtils.toString(key.toByteArray()));
  }
}
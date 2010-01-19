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

import java.math.BigInteger;
import java.io.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class RSAPublicKey extends RSAKey {

  // transient is used to prevent serialization of sensitive data
  private transient BigInteger e, n;

  /** Creates new RSAPublicKey */
  public RSAPublicKey(BigInteger e, BigInteger n) {
    this.e = e;
    this.n = n;
    maxBlock = (n.bitLength()-1) / 8;
  }

  public BigInteger getPublicExponent() {
    return e;
  }

  public BigInteger getModulus() {
    return n;
  }

  public int getKeyBitLength() {
    return n.bitLength();
  }

  public byte[] objectToBytes() {

    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    DataOutputStream dataOut = new DataOutputStream(byteOut);
    byte[] bytes = null;

    try {
      bytes = e.toByteArray();
      dataOut.writeShort(bytes.length);
      dataOut.write(bytes, 0, bytes.length);

      bytes = n.toByteArray();
      dataOut.writeShort(bytes.length);
      dataOut.write(bytes, 0, bytes.length);

      dataOut.flush();

      bytes = byteOut.toByteArray();

      byteOut.close();
      dataOut.close();

    } catch (IOException ioEx) {
      ioEx.printStackTrace();
    }

    return bytes;
  }

  public static RSAPublicKey bytesToObject(byte[] bytes) {
    ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
    DataInputStream dataIn = new DataInputStream(byteIn);
    byte[] buf;

    BigInteger e = null, n = null;

    try {
      buf = new byte[dataIn.readShort()];
      dataIn.readFully(buf);
      e = new BigInteger(buf);

      buf = new byte[dataIn.readShort()];
      dataIn.readFully(buf);
      n = new BigInteger(buf);

      byteIn.close();
      dataIn.close();

    } catch (IOException ioEx) {
      ioEx.printStackTrace();
      throw new IllegalArgumentException("Sequence of bytes does not constitute a valid RSAPublicKey instance.");
    }

    return new RSAPublicKey(e, n);
  }

}
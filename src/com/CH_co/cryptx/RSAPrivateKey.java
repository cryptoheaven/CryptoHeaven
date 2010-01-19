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

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

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
public class RSAPrivateKey extends RSAKey {

  // transient is used to prevent serialization of sensitive data
  private transient BigInteger d, p, q, dP, dQ, qInv;
  public static final String OBJECT_NAME = "RSAPrivateKey"; // used for GlobalProperties

  /** Creates new RSAPrivateKey */
  public RSAPrivateKey( BigInteger d, 
                        BigInteger p, 
                        BigInteger q, 
                        BigInteger dP, 
                        BigInteger dQ,
                        BigInteger qInv) {
    this.d = d;
    this.p = p;
    this.q = q;
    this.dP = dP;
    this.dQ = dQ;
    this.qInv = qInv;

    maxBlock = (p.multiply(q).bitLength()-1) / 8;
  }

  public int getKeyBitLength() {
    return p.multiply(q).bitLength();
  }

  public BigInteger getPrivateExponent() {
    return d;
  }
  public BigInteger getPrimeP() {
    return p;
  }
  public BigInteger getPrimeQ() {
    return q;
  }
  public BigInteger getPrimeExponentP() {
    return dP;
  }
  public BigInteger getPrimeExponentQ() {
    return dQ;
  }
  public BigInteger getCrtCoefficient() {
    return qInv;
  }

  public byte[] objectToBytes() {

    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    DataOutputStream dataOut = new DataOutputStream(byteOut);
    byte[] bytes = null;

    try {
      bytes = d.toByteArray();
      dataOut.writeShort(bytes.length);
      dataOut.write(bytes, 0, bytes.length);

      bytes = p.toByteArray();
      dataOut.writeShort(bytes.length);
      dataOut.write(bytes, 0, bytes.length);

      bytes = q.toByteArray();
      dataOut.writeShort(bytes.length);
      dataOut.write(bytes, 0, bytes.length);

      // don't write dP as its easy to calculate from 'd' and 'p-1'
      //bytes = dP.toByteArray();
      //dataOut.writeShort(bytes.length);
      //dataOut.write(bytes, 0, bytes.length);

      // don't write dQ as its easy to calculate from 'd' and 'q-1'
      //bytes = dQ.toByteArray();
      //dataOut.writeShort(bytes.length);
      //dataOut.write(bytes, 0, bytes.length);

      // don't write the qInv as its easy to calculate from 'p' and 'q'
      //bytes = qInv.toByteArray();
      //dataOut.writeShort(bytes.length);
      //dataOut.write(bytes, 0, bytes.length);

      dataOut.flush();

      bytes = byteOut.toByteArray();

      byteOut.close();
      dataOut.close();

    } catch (IOException ioEx) {
      ioEx.printStackTrace();
    }

    return bytes;
  }

  public static RSAPrivateKey bytesToObject(byte[] bytes) {

    ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
    DataInputStream dataIn = new DataInputStream(byteIn);
    byte[] buf;

    BigInteger d = null, p = null, q = null, dP = null, dQ = null, qInv = null;

    try {
      buf = new byte[dataIn.readShort()];
      dataIn.readFully(buf);
      d = new BigInteger(buf);

      buf = new byte[dataIn.readShort()];
      dataIn.readFully(buf);
      p = new BigInteger(buf);

      buf = new byte[dataIn.readShort()];
      dataIn.readFully(buf);
      q = new BigInteger(buf);

      // calculate the rest of the parameters: dP, dQ, qInv
      BigInteger pSub1 = p.subtract(BigInteger.ONE);
      BigInteger qSub1 = q.subtract(BigInteger.ONE);

      dP = d.remainder(pSub1);
      dQ = d.remainder(qSub1);
      qInv = q.modInverse(p);

      byteIn.close();
      dataIn.close();

    } catch (IOException ioEx) {
      ioEx.printStackTrace();
      throw new IllegalArgumentException("Sequence of bytes does not constitute a valid RSAPrivateKey instance.");
    }

    return new RSAPrivateKey(d, p, q, dP, dQ, qInv);
  }
}
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

import java.security.*;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.12 $</b>
 *
 * @author  Marcin Kurzawa
 */
public final class SHA256 extends HashBase implements Cloneable {

  public static final String name = "SHA-256";

  private static final int[] k = {
       0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
       0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
       0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
       0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
       0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
       0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
       0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
       0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
  };

  private static final int BLOCK_SIZE = 64; // inner engine block size in bytes

  private static final String DIGEST0 = "BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD";

  private static final int[] w = new int[64];

  /** caches the result of the correctness test, once executed. */
  private static Boolean valid;

  /** 256-bit interim result. */
  private int h0, h1, h2, h3, h4, h5, h6, h7;


  /** No argument constructor. */
  public SHA256() {
    super(name, 32, BLOCK_SIZE);
  }

  /**
  * <p>Private constructor for cloning purposes.</p>
  *
  * @param md the instance to clone.
  */
  private SHA256(SHA256 md) {
    this();

    this.h0 = md.h0;
    this.h1 = md.h1;
    this.h2 = md.h2;
    this.h3 = md.h3;
    this.h4 = md.h4;
    this.h5 = md.h5;
    this.h6 = md.h6;
    this.h7 = md.h7;
    this.count = md.count;
    this.buffer = (byte[]) md.buffer.clone();
  }

  public static final int[] G(int hh0, int hh1, int hh2, int hh3, int hh4, int hh5, int hh6, int hh7, byte[] in, int offset) {
    return sha(hh0, hh1, hh2, hh3, hh4, hh5, hh6, hh7, in, offset);
  }

  public Object clone() {
    return new SHA256(this);
  }

  protected void transform(byte[] in, int offset) {
    int[] result = sha(h0, h1, h2, h3, h4, h5, h6, h7, in, offset);

    h0 = result[0];
    h1 = result[1];
    h2 = result[2];
    h3 = result[3];
    h4 = result[4];
    h5 = result[5];
    h6 = result[6];
    h7 = result[7];
  }

  protected byte[] padBuffer() {
    int n = (int) (count % BLOCK_SIZE);
    int padding = (n < 56) ? (56 - n) : (120 - n);
    byte[] result = new byte[padding + 8];

    // padding is always binary 1 followed by binary 0s
    result[0] = (byte) 0x80;

    // save number of bits, casting the long to an array of 8 bytes
    long bits = count << 3;
    result[padding++] = (byte)(bits >>> 56);
    result[padding++] = (byte)(bits >>> 48);
    result[padding++] = (byte)(bits >>> 40);
    result[padding++] = (byte)(bits >>> 32);
    result[padding++] = (byte)(bits >>> 24);
    result[padding++] = (byte)(bits >>> 16);
    result[padding++] = (byte)(bits >>> 8);
    result[padding  ] = (byte) bits;

    return result;
  }

  protected byte[] getResult() {
    return new byte[] {
      (byte)(h0 >>> 24), (byte)(h0 >>> 16), (byte)(h0 >>> 8), (byte) h0,
      (byte)(h1 >>> 24), (byte)(h1 >>> 16), (byte)(h1 >>> 8), (byte) h1,
      (byte)(h2 >>> 24), (byte)(h2 >>> 16), (byte)(h2 >>> 8), (byte) h2,
      (byte)(h3 >>> 24), (byte)(h3 >>> 16), (byte)(h3 >>> 8), (byte) h3,
      (byte)(h4 >>> 24), (byte)(h4 >>> 16), (byte)(h4 >>> 8), (byte) h4,
      (byte)(h5 >>> 24), (byte)(h5 >>> 16), (byte)(h5 >>> 8), (byte) h5,
      (byte)(h6 >>> 24), (byte)(h6 >>> 16), (byte)(h6 >>> 8), (byte) h6,
      (byte)(h7 >>> 24), (byte)(h7 >>> 16), (byte)(h7 >>> 8), (byte) h7
    };
  }

  protected void resetContext() {
    // magic SHA-256 initialisation constants
    h0 = 0x6a09e667;
    h1 = 0xbb67ae85;
    h2 = 0x3c6ef372;
    h3 = 0xa54ff53a;
    h4 = 0x510e527f;
    h5 = 0x9b05688c;
    h6 = 0x1f83d9ab;
    h7 = 0x5be0cd19;
  }

  public boolean selfTest() {
    if (valid == null) {
      SHA256 md = new SHA256();
      md.update((byte) 0x61); // a
      md.update((byte) 0x62); // b
      md.update((byte) 0x63); // c
      String result = com.CH_co.util.ArrayUtils.toString(md.digest());
      valid = Boolean.valueOf(DIGEST0.equals(result));
    }

    return valid.booleanValue();
  }

  private static final synchronized int[] sha(int hh0, int hh1, int hh2, int hh3, int hh4, int hh5, int hh6, int hh7, byte[] in, int offset) {
    int A = hh0;
    int B = hh1;
    int C = hh2;
    int D = hh3;
    int E = hh4;
    int F = hh5;
    int G = hh6;
    int H = hh7;
    int r, T, T2;

    for (r = 0; r < 16; r++) {
      w[r] = in[offset++]         << 24 |
            (in[offset++] & 0xFF) << 16 |
            (in[offset++] & 0xFF) <<  8 |
            (in[offset++] & 0xFF);
    }
    for (r = 16; r < 64; r++) {
      T = w[r - 2];
      T2 = w[r - 15];
      w[r] = (((T >>> 17) | (T << 15)) ^ ((T >>> 19) | (T << 13)) ^ (T >>> 10)) + w[r - 7] + (((T2 >>> 7) | (T2 << 25)) ^ ((T2 >>> 18) | (T2 << 14)) ^ (T2 >>> 3)) + w[r - 16];
    }

    for (r = 0; r < 64; r++) {
      T = H + (((E >>> 6) | (E << 26)) ^ ((E >>> 11) | (E << 21)) ^ ((E >>> 25) | (E << 7))) + ((E & F) ^ (~E & G)) + k[r] + w[r];
      T2 = (((A >>> 2) | (A << 30)) ^ ((A >>> 13) | (A << 19)) ^ ((A >>> 22) | (A << 10))) + ((A & B) ^ (A & C) ^ (B & C));
      H = G;
      G = F;
      F = E;
      E = D + T;
      D = C;
      C = B;
      B = A;
      A = T + T2;
    }

    return new int[] {
      hh0 + A, hh1 + B, hh2 + C, hh3 + D, hh4 + E, hh5 + F, hh6 + G, hh7 + H
    };
  }

}
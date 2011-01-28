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

import java.security.InvalidKeyException;
import java.security.InvalidParameterException;

import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.12 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public final class Rijndael_CBC extends Object{

  public static final int ENCRYPT_STATE = 1;
  public static final int DECRYPT_STATE = 2;

  // transient is used to prevent serialization of sensitive data

  public static final int BLOCK_SIZE = Rijndael_Algorithm.BLOCK_SIZE; // bytes in an AES block
  private transient Object sessionKey = null; // current session key

  private transient byte[] buffer = new byte[BLOCK_SIZE];
  private transient int buffered = 0;


  private int state;

  protected static transient byte[] DEFAULT_IV = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
  protected transient byte[] iv = null; // current contents of feedback buffer
  protected transient byte[] userIV = null; // initial user-supplied IV value


  /**
   * @param iv is the initial state of the feedback buffer
   * @param state is either ENCRYPT_STATE or DECRYPT_STATE
   * @param key is the key material for generating a session key
   */
  public Rijndael_CBC(byte[] iv, int state, byte[] key) throws InvalidKeyException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Rijndael_CBC.class, "Rijndael_CBC(byte[] iv, int state, byte[] key)");
    if (trace != null) trace.args(state);
    setIV(iv);
    init(state, key);
    if (trace != null) trace.exit(Rijndael_CBC.class, this);
  }
  public Rijndael_CBC(int state, byte[] key) throws InvalidKeyException {
    this(DEFAULT_IV, state, key);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Rijndael_CBC.class, "Rijndael_CBC(int state, byte[] key)");
    if (trace != null) trace.args(state);
    if (trace != null) trace.exit(Rijndael_CBC.class, this);
  }
  private Rijndael_CBC() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Rijndael_CBC.class, "Rijndael_CBC()");
    if (trace != null) trace.exit(Rijndael_CBC.class, this);
  }

  /**
   * Cloning of ciphers is not allowed for security reasons.
   * Throws a CloneNotSupportedException. 
   * @exception CloneNotSupportedException Not allowed for security reasons.
   */
  public final Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }


  public void init(int state, byte[] key) throws InvalidKeyException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Rijndael_CBC.class, "init(int state, byte[] key)");
    if (trace != null) trace.args(state);
    generateKey(key);
    engineInit();
    this.state = state;
    if (trace != null) trace.exit(Rijndael_CBC.class);
  }

  public void setIV(byte[] iv) throws InvalidParameterException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Rijndael_CBC.class, "setIV(byte[] iv)");
    if ((state == ENCRYPT_STATE || state == DECRYPT_STATE) && userIV != null)
      throw new IllegalStateException();

    if (iv.length != BLOCK_SIZE)
      throw new InvalidParameterException(""+iv.length);

    userIV = (byte[]) (iv.clone());
    this.iv = (byte[]) (userIV.clone());
    if (trace != null) trace.exit(Rijndael_CBC.class);
  }


  /**
   * @return processed byte array
   */
  public byte[] update(byte[] in, int inOff, int inLen) {
    byte[] out = new byte[BLOCK_SIZE * ((inLen + buffered) / BLOCK_SIZE)];
    return update(in, inOff, out, 0, inLen);
  }
  /**
   * @return processed byte array buffer 'out'.
   */
  public byte[] update(byte[] in, int inOff, byte[] out, int outOff, int inLen) {
    if (iv == null)
      throw new NullPointerException("IV");
    if (buffered > 0) {
      int i = 0;
      for ( ; buffered < BLOCK_SIZE && i < inLen ; i ++) {
        buffer[buffered++] = in[inOff++];
        if (buffered >= BLOCK_SIZE) {
          processBuffer(buffer, 0, buffer, 0);
          System.arraycopy(buffer, 0, out, outOff, buffer.length);
          outOff += buffer.length;
          break;
        }
      }
      inLen -= (i+1);
    }
    int endOff = inOff + inLen;
    for (int i = BLOCK_SIZE; i <= inLen; i += BLOCK_SIZE) {
      processBuffer(in, inOff, out, outOff);
      inOff += BLOCK_SIZE;
      outOff += BLOCK_SIZE;
    }
    if (inOff < endOff) {
      for (int i = inOff; i < endOff; i ++) {
        buffer[buffered++] = in[i];
      }
    }
    return out;
  }

  public byte[] doFinal(byte[] in, int inOff, int inLen) {
    byte[] out = new byte[BLOCK_SIZE * ((inLen + buffered) / BLOCK_SIZE)];
    return doFinal(in, inOff, out, 0, inLen);
  }
  public byte[] doFinal(byte[] in, int inOff, byte[] out, int outOff, int inLen) {
    if ((inLen + buffered) % BLOCK_SIZE != 0)
      throw new InvalidParameterException();
    out = this.update(in, inOff, out, outOff, inLen);
    // un-initialize the cipher
    state = 0; 
    return out;
  }


  public String toString() {
    return "[Rijndael_CBC="
        + "state=" + (state == 0 ? "un-initialized" : (state == ENCRYPT_STATE ? "encryption":"decryption"))
        + "]";
  }


  // ==================================================================
  // Own private methods
  // ==================================================================




  private void generateKey(byte[] key) throws InvalidKeyException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Rijndael_CBC.class, "generateKey(byte[] key)");
    if (key == null)
      throw new InvalidKeyException("Null key");

    int length = key.length;
    if (!(length == 16 || length == 24 || length == 32))
      throw new InvalidKeyException("Incorrect length: "+length);

    sessionKey = Rijndael_Algorithm.makeKey(key);
    if (trace != null) trace.exit(Rijndael_CBC.class);
  }

  private void engineInit() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Rijndael_CBC.class, "engineInit()");
    buffered = 0;
    if (userIV != null) {
      iv = (byte[]) (userIV.clone());
    }
    if (trace != null) trace.exit(Rijndael_CBC.class);
  }

  /**
   * Process a single block from the buffer.
   */
  private void processBuffer(byte[] in, int inOffset, byte[] out, int outOffset) {
    switch (state) {
      case ENCRYPT_STATE:
        for (int i = 0; i < BLOCK_SIZE; i++) {
          iv[i] ^= in[i+inOffset];
        }
        Rijndael_Algorithm.blockEncrypt(iv, 0, out, outOffset, sessionKey);
        for (int i = 0; i < BLOCK_SIZE; i++) {
          iv[i] = out[i+outOffset];
        }
        //System.arraycopy(out, outOffset, iv, 0, BLOCK_SIZE);
        break;
      case DECRYPT_STATE:
        byte[] tempOrig = new byte[BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
          tempOrig[i] = in[i+inOffset];
        }
        //System.arraycopy(in, inOffset, tempOrig, 0, BLOCK_SIZE);
        Rijndael_Algorithm.blockDecrypt(in, inOffset, out, outOffset, sessionKey);
        for (int i = 0; i < BLOCK_SIZE; i++) {
          out[i+outOffset] ^= iv[i];
        }
        iv = tempOrig;
        break;
      default:
        throw new IllegalStateException();
    }
    buffered = 0;
  }










  private static boolean self_test() {
    boolean ok = false;
    try {
      byte[] key = new byte[] {
        0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7 };
      byte[] iv = new byte[] {
        0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7 };
      byte[] input = new byte[] {
        1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        2, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        3, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        4, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 
        5, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 
        6, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, };

      Rijndael_CBC cipher = new Rijndael_CBC();
      cipher.setIV(iv);
      cipher.init(ENCRYPT_STATE, key);
      System.out.println("1");
      byte[] ct1 = cipher.doFinal(input, 0, input.length);
      System.out.println("2");

      int i = BLOCK_SIZE-1;

      cipher.init(ENCRYPT_STATE, key);
      byte[] ct2 = new byte[ct1.length];
      System.out.println("3a");
      byte[] t1 = cipher.update(input, 0, i);
      System.out.println("3b");
      byte[] t2 = cipher.update(input, i, i);
      System.out.println("4");
      byte[] t3 = cipher.doFinal(input, i*2, input.length - i*2);
      System.out.println("5");
      System.arraycopy(t1, 0, ct2, 0, t1.length);
      System.arraycopy(t2, 0, ct2, t1.length, t2.length);
      System.arraycopy(t3, 0, ct2, t1.length + t2.length, t3.length);
      ok = java.util.Arrays.equals(ct1, ct2);
      if (!ok) throw new RuntimeException("CBC encryption failed");

      i = BLOCK_SIZE*2;

      cipher.init(DECRYPT_STATE, key);
      System.out.println("6");
      byte[] plainAgain1 = cipher.update(ct2, 0, i);
      System.out.println("7");
      byte[] plainAgain2 = cipher.doFinal(ct2, i, input.length - i);
      System.out.println("8");
      byte[] plainAgain = new byte[input.length];
      System.arraycopy(plainAgain1, 0, plainAgain, 0, plainAgain1.length);
      System.arraycopy(plainAgain2, 0, plainAgain, plainAgain1.length, plainAgain2.length);
      ok = java.util.Arrays.equals(input, plainAgain);
      if (!ok) throw new RuntimeException("CBC decryption failed");

      byte[] sameBytes = new byte[] { 
          1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6 
      };
      byte[] sB = (byte[]) sameBytes.clone();
      cipher.init(ENCRYPT_STATE, key);
      cipher.update(sB, 0, sB, 0, 16);
      cipher.update(sB, 16, sB, 16, ((sameBytes.length/16)-1)*16);
      cipher.init(DECRYPT_STATE, key);
      cipher.update(sB, 0, sB, 0, 16);
      cipher.update(sB, 16, sB, 16, ((sameBytes.length/16)-1)*16);
      ok = java.util.Arrays.equals(sB, sameBytes);
      if (!ok) throw new RuntimeException("Same array source-destination failed");
    } catch (Throwable t) {
      t.printStackTrace();
      return false;
    }
    return ok;
  }

  private static boolean speed_test() {
    boolean ok = false;
    try {
      byte[] key = new byte[] {
        0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7 };
      byte[] iv = new byte[] {
        0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7 };
      java.util.Random rnd = new java.util.Random();

      Rijndael_CBC cipher = new Rijndael_CBC();
      cipher.setIV(iv);

      long start = System.currentTimeMillis();
      for (int i=0; i<1000; i++)
        cipher.init(ENCRYPT_STATE, key);
      long end = System.currentTimeMillis();
      System.out.println("Cipher.init(): " + (end - start));


      int DATA_SIZE = 1024*1024*BLOCK_SIZE;
//      int rndSplit = rnd.nextInt(DATA_SIZE-1)+1;
      byte[] testBytesIn = new byte[DATA_SIZE];
      byte[] testBytesOut = new byte[DATA_SIZE];
      byte[] testBytesOutIn = new byte[DATA_SIZE];
      rnd.nextBytes(testBytesIn);

      cipher.init(ENCRYPT_STATE, key);

      start = System.currentTimeMillis();
//      cipher.update(testBytesIn, 0, testBytesOut, 0, rndSplit);
//      cipher.update(testBytesIn, rndSplit, testBytesOut, (rndSplit/BLOCK_SIZE)*BLOCK_SIZE, DATA_SIZE-rndSplit);
      cipher.update(testBytesIn, 0, testBytesOut, 0, DATA_SIZE);
      
      end = System.currentTimeMillis();
      System.out.println("Cipher.update(ENCRYPT) on " + DATA_SIZE/1024 + " KB took: " + (end - start) + " ms.");

      cipher.init(DECRYPT_STATE, key);

      start = System.currentTimeMillis();
      cipher.update(testBytesOut, 0, testBytesOutIn, 0, DATA_SIZE);
      end = System.currentTimeMillis();
      System.out.println("Cipher.update(DECRYPT) on " + DATA_SIZE/1024 + " KB took: " + (end - start) + " ms.");


      ok = java.util.Arrays.equals(testBytesIn, testBytesOutIn);
      if (!ok) {
        System.out.println("testBytesIn=\n" + Misc.objToStr(testBytesIn));
        System.out.println("testBytesOutIn=\n" + Misc.objToStr(testBytesOutIn));
        throw new RuntimeException("Same array source-destination failed");
      }
    } catch (Throwable t) {
      t.printStackTrace();
      return false;
    }
    return ok;
  }

  /**
   * main(): Basic self test
   */
  public static void main(String[] args) {
    try {
      System.out.println("Self test " + (Rijndael_CBC.self_test() ? "OK":"FAILED"));
      System.out.println("Speed test " + (Rijndael_CBC.speed_test() ? "OK":"FAILED"));
    } catch (Exception x) {
      System.err.println("Exception occurred: "+x.getMessage());
      x.printStackTrace();
    }
  }
}
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

import java.math.BigInteger;
import java.util.Random;

import com.CH_co.monitor.Interrupter;

/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class RSAKeyPairGenerator extends Object {

  public static final int DEFAULT_CERTAINTY = 128;

  // Fermat prime F4.
  private static final BigInteger F4 = BigInteger.valueOf(0x10001L);

  private transient int strength = 3072;
  private transient int certainty = DEFAULT_CERTAINTY;
  private transient Random random;

   /** Creates new RSAKeyPairGenerator */
  public RSAKeyPairGenerator() {
  }

  public RSAKeyPair generateKeyPair() {
    return generateKeyPair(strength, certainty, random);
  }

  public void initialize(int keySize, int certainty, Random random) {
    this.strength = keySize;
    this.certainty = certainty;
    this.random = random;
  }

  public void initialize(int keySize, Random random) {
    this.strength = keySize;
    this.random = random;
  }

  // statics

  public static RSAKeyPair generateKeyPair(int strength) {
    return generateKeyPair(strength, DEFAULT_CERTAINTY, Rnd.getSecureRandom());
  }
  public static RSAKeyPair generateKeyPair(int strength, Random random) {
    return generateKeyPair(strength, DEFAULT_CERTAINTY, random);
  }
  public static RSAKeyPair generateKeyPair(int strength, int certainty) {
    return generateKeyPair(strength, certainty, Rnd.getSecureRandom());
  }
  public static RSAKeyPair generateKeyPair(int strength, int certainty, Interrupter interrupter) {
    return generateKeyPair(strength, certainty, Rnd.getSecureRandom(), interrupter);
  }

   /** 
    * @return RSA key pair of the minimum (or higher) strength specified using 
    * specified minimum certainty that selected numbers are prime within 1-(1/2)^certainty.
    * @param random source of randomness for selection of prime numbers.
    */
  public static RSAKeyPair generateKeyPair(int strength, int certainty, Random random) {
    return generateKeyPair(strength, certainty, random, null);
  }
  public static RSAKeyPair generateKeyPair(int strength, int certainty, Random random, Interrupter interrupter) {
      BigInteger p=null, q=null, n=null, d=null, e=null, pSub1=null, qSub1=null, phi=null;

      boolean interrupted = false;
      /*
       * Each part of the key should be half the given strength, 
       * plus some margin, better to be stronger than weaker.
       */
      int keyStrength1 = strength / 2 + 1;
      int keyStrength2 = strength / 2 + 1;

      while (true) {
        try {
          if (interrupted || (interrupter != null && (interrupted=interrupter.isInterrupted()))) break;
          do {
            if (interrupted || (interrupter != null && (interrupted=interrupter.isInterrupted()))) break;
            p = new BigInteger(keyStrength1, certainty, random);
            if (interrupted || (interrupter != null && (interrupted=interrupter.isInterrupted()))) break;
            /**
             * Generate a random number of strength bits that is a
             * probable prime with a certainty of 1 - 1/2**certainty.
             */
            q = new BigInteger(keyStrength2, certainty, random);
            if (interrupted || (interrupter != null && (interrupted=interrupter.isInterrupted()))) break;
            n = p.multiply(q);
            if (interrupted || (interrupter != null && (interrupted=interrupter.isInterrupted()))) break;
          } while (p.equals(q) || n.bitLength() < strength+1); // just in case

          if (interrupted || (interrupter != null && (interrupted=interrupter.isInterrupted()))) break;
          if (p.compareTo(q) < 0) {
            // swap
             BigInteger tmp = p; p = q; q = tmp;
          }

          if (interrupted || (interrupter != null && (interrupted=interrupter.isInterrupted()))) break;
          pSub1 = p.subtract(BigInteger.ONE);
          if (interrupted || (interrupter != null && (interrupted=interrupter.isInterrupted()))) break;
          qSub1 = q.subtract(BigInteger.ONE);
          if (interrupted || (interrupter != null && (interrupted=interrupter.isInterrupted()))) break;
          phi = pSub1.multiply(qSub1);

          // phi must be even 
          //if (phi.mod(two) != BigInteger.ZERO)
          //  System.out.println("PHI must be even, but its ODD!");

          // Fermat prime F4.
          e = F4;
          /*
          e = BigInteger.valueOf(17);
          while (!e.gcd(phi).equals(one))
             e = e.add(two);
          */

          if (interrupted || (interrupter != null && (interrupted=interrupter.isInterrupted()))) break;
          // make private part of key
          d = e.modInverse(phi);
          break;

        } catch (ArithmeticException exception) { }
      }
      // Encryption function: (T) = (T^E) mod PQ   where T is the plaintext (a positive integer)
      // Decryption function: (C) = (C^D) mod PQ   where C is the ciphertext (a positive integer)

      /*
       * create the factors for the private key
       */
      BigInteger dP=null, dQ=null, qInv=null;
      while (true) { // loop only for conveniance so we can break-out with interrupt
        if (interrupted || (interrupter != null && (interrupted=interrupter.isInterrupted()))) break;
        dP = d.remainder(pSub1);
        if (interrupted || (interrupter != null && (interrupted=interrupter.isInterrupted()))) break;
        dQ = d.remainder(qSub1);
        if (interrupted || (interrupter != null && (interrupted=interrupter.isInterrupted()))) break;
        qInv = q.modInverse(p);
        break; // one pass loop for conveniance of break-out with interrupt
      }

      if (interrupted)
        return null;
      else 
        return new RSAKeyPair(new RSAPublicKey(e, n), new RSAPrivateKey(d, p, q, dP, dQ, qInv));
   }

}
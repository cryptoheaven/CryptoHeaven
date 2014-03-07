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

import com.CH_co.monitor.Interrupter;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;


/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class RSAKeyPairGenerator extends Object {

  public static final int DEFAULT_CERTAINTY = 128;

  // Fermat prime F4.
  private static final BigInteger F4 = BigInteger.valueOf(0x10001L);

  private transient int strength = 3072;
  private transient int certainty = DEFAULT_CERTAINTY;
  private transient SecureRandom random;

  /** Creates new RSAKeyPairGenerator */
  public RSAKeyPairGenerator() {
  }

  public RSAKeyPair generateKeyPair() {
    return generateKeyPair(strength, certainty, random);
  }

  public void initialize(int keySize, int certainty, SecureRandom random) {
    this.strength = keySize;
    this.certainty = certainty;
    this.random = random;
  }

  public void initialize(int keySize, SecureRandom random) {
    this.strength = keySize;
    this.random = random;
  }

  /**
  * @return approximate time the key generation will run in seconds.
  */
  public static int estimateGenerationTime(int keyLength, int certainty) {
    // make sure the secure random is initialized
    Rnd.initSecureRandom();
    try {
      // rest for 1 second so that things have a change to seattle down
      Thread.sleep(1000);
    } catch (InterruptedException e) {
    }

    Date start = new Date();
    // average out 5 quick runs
    for (int i=0; i<5; i++) {
      RSAKeyPairGenerator.generateKeyPair(512, 128);
    }
    Date end = new Date();
    double tDiff = (end.getTime() - start.getTime()) / 5.0;
    // tDiff should be about 420 ms on a reference machine that the following approx. can be used
    // use the approximation curve 6.42*10^(-9) * keyLength^(2.867)

    // find a scale for this machine
    double scale = tDiff / 420.0;
    double expectedTime = ( ((double)certainty)/128.0 ) * 0.00000642 * Math.pow(keyLength, 2.867) * scale;
    // add 30%
    expectedTime *= 1.3;
    return (int) (expectedTime / 1000.0);
  }

  public static RSAKeyPair generateKeyPair(int strength) {
    return generateKeyPair(strength, DEFAULT_CERTAINTY, Rnd.getSecureRandom());
  }
  public static RSAKeyPair generateKeyPair(int strength, SecureRandom random) {
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
  public static RSAKeyPair generateKeyPair(int strength, int certainty, SecureRandom random) {
    return generateKeyPair(strength, certainty, random, null);
  }
  public static RSAKeyPair generateKeyPair(int strength, int certainty, SecureRandom random, Interrupter interrupter) {
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
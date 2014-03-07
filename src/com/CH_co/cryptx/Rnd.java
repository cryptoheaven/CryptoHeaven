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

import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/** 
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.14 $</b>
*
* @author  Marcin Kurzawa
*/
public class Rnd extends Object {

  private static class SingletonHolder {
    private static SecureRandom random;
    static {
      // initialize a secure random generator
      Thread th = new ThreadTraced("Rnd Seeder") {
        public void runTraced() {
          SecureRandom sr = null;
          try {
            sr = SecureRandom.getInstance("SHA1PRNG");
          } catch (NoSuchAlgorithmException e) {
            sr = new SecureRandom();
          }
          DoubleSecureRandom dsr = new DoubleSecureRandom(sr);
          random = dsr;
        }
      };
      th.setDaemon(true);
      th.start();
    }
  }

  /**
  * Hide constructor, all methods are static.
  */
  private Rnd() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Rnd.class, "Rnd()");
    if (trace != null) trace.exit(Rnd.class);
  }

  /**
  * @return true if secure random finished initializing
  */
  public static boolean initSecureRandom() {
    return SingletonHolder.random != null;
  }
  /**
  * @return a Random generator.
  */
  public static SecureRandom getSecureRandom() {
    SecureRandom rnd = SingletonHolder.random;
    if (rnd == null) {
      while ((rnd = SingletonHolder.random) == null) {
        try { Thread.sleep(10); } catch (Throwable t) { }
      }
    }
    return rnd;
  }

  public static void main(String[] args) {
    System.out.println("initializing baseline SecureRandom...");
    SecureRandom srSimple = new SecureRandom();
    System.out.println("initializing Rnd...");
    SecureRandom srStrong = Rnd.getSecureRandom();
    System.out.println("initializaiton done.");
    byte[] buf = new byte[1024];
    long start = System.currentTimeMillis();
    srSimple.nextBytes(buf);
    long end = System.currentTimeMillis();
    System.out.println("baseline time="+(end-start)+" ms.");
    start = System.currentTimeMillis();
    srStrong.nextBytes(buf);
    end = System.currentTimeMillis();
    System.out.println("compared to time="+(end-start)+" ms.");
  }
}
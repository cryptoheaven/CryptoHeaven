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

import java.security.SecureRandom;
import java.util.Random;

import com.CH_co.trace.*;

/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.14 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class Rnd extends Object {

  private static class SingletonHolder {
    private static Random random;
    static {
      // initialize a secure random generator
      Thread th = new ThreadTraced("Rnd Seeder") {
        public void runTraced() {
          random = new SecureRandom();
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
  public static Random getSecureRandom() {
    Random rnd = SingletonHolder.random;
    if (rnd == null) {
      while ((rnd = SingletonHolder.random) == null) {
        try { Thread.sleep(10); } catch (Throwable t) { }
      }
    }
    return rnd;
  }
}
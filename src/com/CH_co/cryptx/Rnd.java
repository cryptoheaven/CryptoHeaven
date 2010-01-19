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

import java.security.SecureRandom;

import com.CH_co.trace.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
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
 * <b>$Revision: 1.14 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class Rnd extends Object {

  static {
    // initialize a secure random generator
    Thread th = new ThreadTraced("Rnd Seeder") {
      public void runTraced() {
        getSecureRandom().nextInt();  // call for next will seed the generator (this can take a while)
      }
    };
    th.setPriority(Thread.MIN_PRIORITY);
    th.setDaemon(true);
    th.start();
  }

  /** Creates new Rnd */
  public Rnd() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Rnd.class, "Rnd()");
    if (trace != null) trace.exit(Rnd.class);
  }


  /**
   * @return a Random generator.
   */
  private static java.util.Random random;
  public static java.util.Random getSecureRandom() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Rnd.class, "getSecureRandom()");
    // Lazily create a random generator
    if (random == null) {
      random = new SecureRandom();
    }

    if (trace != null) trace.exit(Rnd.class);
    return random;
  }
}
/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
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
import com.CH_co.trace.Trace;

/** 
 * <b>Copyright</b> &copy; 2001-2009
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
    Thread t = new Thread("Rnd") {
      public void run() {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");

        // change the priority of this thread to minimum
        setPriority(MIN_PRIORITY);

        if (trace != null) trace.data(10, "auto seeding random generator ...");
        getSecureRandom().nextInt();  // call for next will seed the generator (this can take a while)
        if (trace != null) trace.data(11, "auto seeding random generator ... done.");

        if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
        if (trace != null) trace.exit(getClass());
        if (trace != null) trace.clear();
      }
    };
    t.setPriority(Thread.MIN_PRIORITY);
    t.setDaemon(true);
    t.start();
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
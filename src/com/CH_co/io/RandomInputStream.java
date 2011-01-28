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

package com.CH_co.io;

import java.io.InputStream;
import java.util.Random;

import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.10 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class RandomInputStream extends InputStream {

  private Random random;

  /** Creates new RandomInputStream */
  public RandomInputStream(Random rnd) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RandomInputStream.class, "RandomInputStream()");
    random = rnd;
    if (trace != null) trace.exit(RandomInputStream.class);
  }

  public int read() {
    byte[] b = new byte[1];
    random.nextBytes(b);
    return b[0];
  }

  public int read(byte[] b) {
    random.nextBytes(b);
    return b.length;
  }

  public int read(byte[] b, int off, int len) {
    byte[] bytes = new byte[len];
    random.nextBytes(bytes);
    System.arraycopy(bytes, 0, b, off, len);
    return len;
  }

  public int available() {
    return 32*1024;
  }

}
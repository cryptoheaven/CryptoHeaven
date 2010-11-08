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

package com.CH_co.io;

import java.io.*;
import java.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class SpeedLimitedOutputStream extends OutputStream {

  private OutputStream out;
  private long maxRate; // -1 to disable tracking and enforcement, 0 to enable tracking but disable enforcement

  private LinkedList timeCountL = new LinkedList();

  // if globaly hooked up, then the rate will be shared between all streams globaly hooked up
  private boolean globalRateHookup;

  /**
   * Creates new SpeedLimitedOutputStream
   * @param maxRate Maximum allowed throughput in kpbs (kilo bits per second)
   */
  public SpeedLimitedOutputStream(OutputStream out, long maxRate, boolean globalRateHookup) {
    this.out = out;
    this.maxRate = maxRate;
    this.globalRateHookup = globalRateHookup;
  }

  private void checkSlowDown(int additionalBytes) {
    if (globalRateHookup) {
      SpeedLimiter.moreBytesWritten(additionalBytes);
    }
    if (maxRate >= 0) {
      SpeedLimiter.moreBytesSlowDown(additionalBytes, timeCountL, maxRate);
    }
  }

  public long calculateRate() {
    return SpeedLimiter.calculateRate(timeCountL);
  }

  public void write(int b) throws IOException {
    out.write(b);
    checkSlowDown(1);
  }

  public void write(byte[] b) throws IOException {
    out.write(b);
    checkSlowDown(b.length);
  }

  public void write(byte[] b, int off, int len) throws IOException {
    out.write(b, off, len);
    checkSlowDown(len);
  }

  public void close() throws IOException {
    out.close();
  }

  public void flush() throws IOException {
    out.flush();
  }
}
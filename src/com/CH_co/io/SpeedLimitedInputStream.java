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
public class SpeedLimitedInputStream extends InputStream {

  private InputStream in;
  private long rate;

  private LinkedList startDateMillisL = new LinkedList();
  private LinkedList totalBytesL = new LinkedList();

  // if globaly hooked up, then the rate will be shared between all streams globaly hooked up
  private boolean globalRateHookup;

  /** 
   * Creates new SpeedLimitedInputStream 
   * @param rate Maximum allowed throughput in kpbs (kilo bits per second)
   */
  public SpeedLimitedInputStream(InputStream in, long rate, boolean globalRateHookup) {
    this.in = in;
    this.rate = rate;
    this.globalRateHookup = globalRateHookup;
  }

  // Connection breaking tests...
  private boolean CONNECTION_BREAK_TEST_ENABLED = false;
  private long totalBytes;
  public long testBreakBytes = -1;
  
  private void checkSlowDown(int additionalBytes) throws IOException {
    if (globalRateHookup) {
      SpeedLimiter.moreBytesRead(additionalBytes);
    }
    if (rate > 0) {
      SpeedLimiter.moreBytesSlowDown(additionalBytes, startDateMillisL, totalBytesL, rate);
    }
    if (CONNECTION_BREAK_TEST_ENABLED && testBreakBytes >= 0) {
      totalBytes += additionalBytes;
      System.out.println(totalBytes + " bytes through connection so far, time is " + new Date());
      if (totalBytes >= testBreakBytes) {
        System.out.println("closing stream for testing");
        in.close();
        System.out.println("closed");
        throw new IOException("stream closed due to testing");
      }
    }
  }


  public int available() throws IOException {
    return in.available();
  }

  public int read() throws IOException {
    int byteRead = in.read();
    checkSlowDown(1);
    return byteRead;
  }

  public int read(byte[] b) throws IOException {
    int numRead = in.read(b);
    checkSlowDown(numRead);
    return numRead;
  }

  public int read(byte[] b, int off, int len) throws IOException {
    int numRead = in.read(b, off, len);
    checkSlowDown(numRead);
    return numRead;
  }

  public void close() throws IOException {
    in.close();
  }

}
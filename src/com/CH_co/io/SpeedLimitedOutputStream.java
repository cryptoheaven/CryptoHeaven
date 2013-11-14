/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.io;

import com.CH_co.monitor.Interruptible;

import java.io.*;
import java.util.*;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class SpeedLimitedOutputStream extends OutputStream implements Interruptible {

  private OutputStream out;
  private boolean interrupted;
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
    if (interrupted)
      throw new InterruptedIOException("IO was interrupted.");
    out.write(b);
    checkSlowDown(1);
  }

  public void write(byte[] b) throws IOException {
    if (interrupted)
      throw new InterruptedIOException("IO was interrupted.");
    out.write(b);
    checkSlowDown(b.length);
  }

  public void write(byte[] b, int off, int len) throws IOException {
    if (interrupted)
      throw new InterruptedIOException("IO was interrupted.");
    out.write(b, off, len);
    checkSlowDown(len);
  }

  public void close() throws IOException {
    out.close();
  }

  public void flush() throws IOException {
    out.flush();
  }

  public void interrupt() {
    interrupted = true;
  }
}
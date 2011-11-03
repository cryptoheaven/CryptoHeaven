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

import java.io.*;

import com.CH_co.monitor.Interruptible;

/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class InterruptibleInputStream extends InputStream implements Interruptible {

  InputStream in;
  long byteCounter = 0;
  boolean interrupted;

  /** Creates new InterruptibleInputStream */
  public InterruptibleInputStream(InputStream in) {
    this.in = in;
  }

  public int available() throws IOException {
    return in.available();
  }

  public int read() throws IOException {
    if (interrupted) throw new InterruptedIOException("IO was interrupted.");
    int rc = in.read();
    if (rc >= 0) byteCounter ++;
    return rc;
  }

  public int read(byte[] b) throws IOException {
    if (interrupted) throw new InterruptedIOException("IO was interrupted.");
    int numBytes = in.read(b);
    if (numBytes > 0) byteCounter += numBytes;
    return numBytes;
  }

  public int read(byte[] b, int off, int len) throws IOException {
    if (interrupted) throw new InterruptedIOException("IO was interrupted.");
    int numBytes = in.read(b, off, len);
    if (numBytes > 0) byteCounter += numBytes;
    return numBytes;
  }

  public void close() throws IOException {
    in.close();
  }

  /**
   * Interruptible interface method.
   */
  public void interrupt() {
    interrupted = true;
  }

  public long getByteCounter() {
    return byteCounter;
  }

  public void setByteCounter(long counterValue) {
    byteCounter = counterValue;
  }
}
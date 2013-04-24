/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_co.io;

import java.io.*;

import com.CH_co.monitor.Interruptible;

/** 
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p> 
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class InterruptibleOutputStream extends OutputStream implements Interruptible {

  OutputStream out;
  boolean interrupted;

  /** Creates new InterruptibleOutputStream */
  public InterruptibleOutputStream(OutputStream out) {
    this.out = out;
  }


  public void write(int b) throws IOException {
    if (interrupted) throw new InterruptedIOException("IO was interrupted.");
    out.write(b);
  }

  public void write(byte[] b) throws IOException {
    if (interrupted) throw new InterruptedIOException("IO was interrupted.");
    out.write(b);
  }

  public void write(byte[] b, int off, int len) throws IOException {
    if (interrupted) throw new InterruptedIOException("IO was interrupted.");
    out.write(b, off, len);
  }

  public void close() throws IOException {
    out.close();
  }

  public void flush() throws IOException {
    out.flush();
  }  

  /**
   * Interruptible interface method.
   */
  public void interrupt() {
    interrupted = true;
  }
}
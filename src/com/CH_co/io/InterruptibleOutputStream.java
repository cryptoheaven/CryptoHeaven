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

import java.io.*;

import com.CH_co.monitor.Interruptible;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
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
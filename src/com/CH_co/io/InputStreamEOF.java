/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.io;

import java.io.*;

import com.CH_co.trace.Trace;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * Class Description: InputStream wrapper that adds EOF when specified number of bytes was read.
 *
 * <b>$Revision: 1.5 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class InputStreamEOF extends InputStream {

  InputStream in;
  int bytes;
  int counter;

  /** Creates new InputStreamEOF */
  public InputStreamEOF(InputStream in, int bytes) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(InputStreamEOF.class, "InputStreamEOF(InputStream in, long bytes)");
    if (trace != null) trace.args(in);
    if (trace != null) trace.args(bytes);
    this.in = in;
    this.bytes = bytes;
    if (trace != null) trace.exit(InputStreamEOF.class);
  }

  public int available() {
    return bytes - counter;
  }

  public void close() throws IOException {
    in.close();
  }

  public void mark(int readlimit) {
    in.mark(readlimit);
  }

  public boolean markSupported() {
    return false;
    //return in.markSupported();
  }

  public int read() throws IOException  {
    if (counter >= bytes) {
      return -1;
    } else {
      counter ++;
      return in.read();
    }
  }

  public int read(byte[] b) throws IOException {
    if (counter + b.length <= bytes) {
      int readCount = in.read(b);
      counter += readCount;
      return readCount;
    } else {
      int toRead = bytes - counter;
      if (toRead > 0) {
        int readCount = in.read(b, 0, toRead);
        counter += readCount;
        return readCount;
      } else {
        return -1;
      }
    }
  }

  public int read(byte[] b, int off, int len) throws IOException {
    if (counter + len <= bytes) {
      int readCount = in.read(b, off, len);
      counter += readCount;
      return readCount;
    } else {
      int toRead = bytes - counter;
      if (toRead > 0) {
        int readCount = in.read(b, off, toRead);
        counter += readCount;
        return readCount;
      } else {
        return -1;
      }
    }
  }

  public void reset() throws IOException {
    in.reset();
  }

  public long skip(long n) throws IOException {
    counter += n;
    return in.skip(n);
  }

}
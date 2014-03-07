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

import java.io.OutputStream;
import java.io.IOException;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.5 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class CountingOutputStream extends OutputStream {

  OutputStream out;
  long byteCount = 0;

  /** Creates new CountingOutputStream */
  public CountingOutputStream(OutputStream out) {
    this.out = out;
  }

  public void write(int b) throws IOException {
    out.write(b);
    byteCount ++;
  }

  public void write(byte[] b) throws IOException {
    out.write(b);
    byteCount += b.length;
  }

  public void write(byte[] b, int off, int len) throws IOException {
    out.write(b, off, len);
    byteCount += len;
  }

  public void close() throws IOException {
    out.close();
  }

  public void flush() throws IOException {
    out.flush();
  }  

  public long getByteCount() {
    return byteCount;
  }

}
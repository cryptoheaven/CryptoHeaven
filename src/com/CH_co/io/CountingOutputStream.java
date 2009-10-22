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

package com.CH_co.io;

import java.io.OutputStream;
import java.io.IOException;

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
 * <b>$Revision: 1.5 $</b>
 * @author  Marcin Kurzawa
 * @version
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
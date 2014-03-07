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

import com.CH_co.trace.Trace;
import java.io.InputStream;
import java.util.Random;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * Class Description:
 *
 * Class Details:
 *
 * <b>$Revision: 1.10 $</b>
 *
 * @author  Marcin Kurzawa
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
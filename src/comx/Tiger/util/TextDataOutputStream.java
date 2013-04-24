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
// Source File Name:   TextDataOutputStream.java

package comx.Tiger.util;

import java.io.*;

public class TextDataOutputStream extends DataOutputStream {

  public TextDataOutputStream(OutputStream outputstream) {
    super(outputstream);
  }

  public void writeTextLine(String s) throws IOException {
    writeChars(s);
    writeChars(System.getProperty("line.separator"));
  }
}
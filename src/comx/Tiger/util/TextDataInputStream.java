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
// Source File Name:   TextDataInputStream.java

package comx.Tiger.util;

import java.io.*;

public class TextDataInputStream extends DataInputStream {

  public TextDataInputStream(InputStream inputstream) {
    super(inputstream);
  }

  public String readTextLine() throws IOException {
    StringBuffer stringbuffer = new StringBuffer();
    char c = '\0';
    boolean flag = false;
    boolean flag1 = false;
    do {
      if (flag || flag1)
        break;
      try {
        c = readChar();
      }
      catch (EOFException eofexception) {
        flag = true;
      }
      if (!flag)
        if (c == '\n')
          flag1 = true;
        else
          if (c != '\r')
            stringbuffer.append(c);
    } while (true);
    if (flag && stringbuffer.length() == 0)
      return null;
    else
      return stringbuffer.toString();
  }
}
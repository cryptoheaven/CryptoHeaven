/*
 * Copyright 2001-2010 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */
// Source File Name:   CharArray.java

package comx.Tiger.util;


public class CharArray {

  public CharArray() {
  }

  public static final int compare(char ac[], char ac1[]) {
    int i;
    for (i = 0; ac[i] == ac1[i];)
      if (ac[i++] == 0)
        return 0;

    return ac[i] - ac1[i];
  }

  public static final int length(char ac[]) {
    int i = 0;
    for (int j = 0; ac[j] != 0; j++)
      i++;

    return i;
  }

  public static final String toString(char ac[]) {
    return new String(ac, 0, length(ac));
  }
}
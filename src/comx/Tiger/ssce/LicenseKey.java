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
// Source File Name:   LicenseKey.java

package comx.Tiger.ssce;


// Referenced classes of package com.wintertree.ssce:
//      SpellingSession

public class LicenseKey {

  public static int key;

  public LicenseKey() {
  }

  public static void setKey(int i) {
    SpellingSession.setOption(Integer.valueOf(32), Integer.valueOf(i));
    key = i;
  }
}
/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package comx.Tiger.ssce;

public class LicenseKey {

  public static int key;

  public LicenseKey() {
  }

  public static void setKey(int i) {
    SpellingSession.setOption(new Integer(32), new Integer(i));
    key = i;
  }
}
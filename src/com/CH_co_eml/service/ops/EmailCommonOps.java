/*
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_co_eml.service.ops;

import javax.mail.internet.InternetAddress;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class EmailCommonOps {

  public static String[] getPersonalAndEmailAddressParts(String fullEmailAddress) {
    try {
      InternetAddress ia = new InternetAddress(fullEmailAddress);
      String personal = ia.getPersonal();
      String address = ia.getAddress();
      return new String[] { personal, address };
    } catch (Throwable t) {
      throw new IllegalArgumentException("Invalid email address, error: " + t.getMessage());
    }
  }

}
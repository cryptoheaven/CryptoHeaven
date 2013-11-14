/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.lang;

import java.io.Serializable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * Class Description: 
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.10 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class LangCo extends Object implements Serializable { // use Serializable as a trick to skip obfuscation

  public static ResourceBundle rb;

  static {
    String className = LangCo.class.getName();
    String resourceName = className.substring(0, className.lastIndexOf('.')+1) + "ch-co-lang";
    try {
      rb = java.util.ResourceBundle.getBundle(resourceName);
    } catch (MissingResourceException e) {
    }
  }
}
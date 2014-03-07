/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.cryptx;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 *
 * @author  Marcin Kurzawa
 */
abstract public class RSAKey extends Object {

  protected transient int maxBlock;
  
  /** Creates new RSAKey */
  public RSAKey() {
  }
  
  /** @return maximum number of bytes that can be signed using this length key */
  public int getMaxBlock() {
    return maxBlock;
  }

  public abstract int getKeyBitLength();
  
  public String shortInfo() {
    return "RSA(" + (getMaxBlock()*8) + ")";
  }
  
  public String toString() {
    return "[" + getClass().getName() + " maxBlock=" + getMaxBlock() + "]";
  }

}
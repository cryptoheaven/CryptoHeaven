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

package com.CH_co.cryptx;

/** 
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version 
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
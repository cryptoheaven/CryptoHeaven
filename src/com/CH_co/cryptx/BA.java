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

import java.util.Arrays;

import com.CH_co.util.ArrayUtils;
import com.CH_co.util.Misc;

/** 
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: BA --- (B)yte(A)rray
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.15 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public abstract class BA extends Object {

  // transient is used to prevent serialization of sensitive data
  private transient byte[] blockContent;

  /** Creates new BA */
  public BA(byte[] blockContent) {
    this(blockContent, 0, blockContent.length);
  }
  public BA(byte[] source, int offset, int length) {
    blockContent = new byte[length];
    System.arraycopy(source, offset, blockContent, 0, length);
    /*
    if (blockContent == null) { // || blockContent.length == 0) {
      throw new IllegalArgumentException("This constructor does not support empty byte arrays!");
    }
     */
  }
  /** Creates new BA from the contents of the specified BA, array contents is cloned */
  public BA(BA ba) {
    this(ba.toByteArray());
  }
  protected BA() {
  }


  /**
   * Sets the content of this ByteArray.
   */
  protected void setContent(byte[] content) {
    blockContent = content;
  }

  /**
   * @return a byte array version of the block contents.
   */
  public byte[] toByteArray() {
    if (blockContent == null)
      return null;
    byte[] cloneData = (byte[]) blockContent.clone();
    return cloneData;
  }

  /**
   * @return a byte array version of the block contents.
   */
  public String toByteStr() {
    if (blockContent == null)
      return null;
    return Misc.convBytesToStr(blockContent);
  }

  /**
   * @return hex string with the array content
   */
  public String getHexContent() {
    if (blockContent == null)
      return null;
    String str = ArrayUtils.toString(blockContent);
    return str;
  }
  /*
  public String getCodedContent() {
    if (blockContent == null)
      return null;
    BigInteger bInt = new BigInteger(blockContent);
    String str = bInt.toString(36);
    return str;
  }
   */

  /**
   * @return true if the byte array content is equivalent
   */
  public boolean equals(Object o) {
    if (o instanceof BA) {
      BA ba = (BA) o;
      return Arrays.equals(blockContent, ba.blockContent);
    }
    else
      return super.equals(o);
  }
  public int hashCode() {
    if (blockContent != null)
      return Arrays.hashCode(blockContent);
    else
      return super.hashCode();
  }

  public String toString() {
    return "[" +Misc.getClassNameWithoutPackage(getClass()) + " " + ArrayUtils.info(blockContent) + "]";
  }

  public String verboseInfo() {
    return  "" + (blockContent.length * 8) + " bits";
  }

  public void clearContent() {
    for (int i=0; blockContent!=null && i<blockContent.length; i++)
      blockContent[i] = 0;
    blockContent = null;
  }

  public static boolean isEmptyOrZero(BA ba) {
    return ba == null || ba.size() == 0 || (ba.size() == 1 && ba.toByteArray()[0] == (byte) 0x0);
  }
  public static byte[] makeZeroMarker() {
    return new byte[] { 0x0 };
  }

  public int size() {
    return blockContent.length;
  }

  public void XOR(byte[] bytes) {
    for (int i=0; i<blockContent.length; i++) {
      blockContent[i] ^= bytes[i];
    }
  }

}
/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
// Source File Name:   CompressedLexicon.java

package comx.Tiger.ssce;

import comx.Tiger.util.CharArray;
import comx.Tiger.util.Comparable;

import java.io.Serializable;

class CLexSegment implements Comparable, Serializable {

  public int offset;
  public int size;
  public byte data[];
  public int lastUsed;
  public char id[];

  CLexSegment() {
    id = new char[4];
    for (int i = 0; i < id.length; i++)
      id[i] = '\0';

  }

  public int compareTo(Comparable comparable) {
    CLexSegment clexsegment = (CLexSegment)comparable;
    return CharArray.compare(id, clexsegment.id);
  }
}
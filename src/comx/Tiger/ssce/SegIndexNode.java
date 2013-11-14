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

import comx.Tiger.util.CharArray;
import comx.Tiger.util.Comparable;

class SegIndexNode implements Comparable {

  int offset;
  int size;
  char id[];

  SegIndexNode() {
    offset = 0;
    size = 0;
    id = new char[4];
    id[0] = '\0';
  }

  public int compareTo(Comparable comparable) {
    SegIndexNode segindexnode = (SegIndexNode) comparable;
    return CharArray.compare(id, segindexnode.id);
  }
}
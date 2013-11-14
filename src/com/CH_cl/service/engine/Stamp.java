/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.engine;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * Stamp/Long object wrapper to avoid using incorrect synchronization on cachable Long objects.
 *
 * @author  Marcin Kurzawa
 */
public class Stamp {

  private long value;

  protected Stamp(long value) {
    this.value = value;
  }

  protected long longValue() {
    return value;
  }

  /**
   * Auto-Generated hashCode method.
   * @return
   */
  public int hashCode() {
    int hash = 7;
    hash = 59 * hash + (int) (this.value ^ (this.value >>> 32));
    return hash;
  }

  public boolean equals(Object o) {
    return o instanceof Stamp && ((Stamp) o).value == value;
  }

  public String toString() {
    return String.valueOf(value);
  }
}
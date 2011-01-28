/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_cl.service.engine;

/**
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Stamp/Long object wrapper to avoid using incorrect synchronization on cachable Long objects.
 *
 * @author  Marcin Kurzawa
 * @version
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
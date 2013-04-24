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

package com.CH_co.service.records;

import java.util.*;

/**
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.2 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class IdComparator extends Object implements Comparator {

  private Long id;

  /** Creates new IdComparator */
  public IdComparator() {
  }

  /** Creates new IdComparator */
  public IdComparator(Long id) {
    setId(id);
  }

  public void setId(Long id) {
    this.id = id;
  }

  public int compare(Object o1, Object o2) {
    int rc = 0;
    if (o1 instanceof Record && o2 instanceof Long)
      rc = ((Record) o1).getId().compareTo((Long) o2);
    else if (o1 instanceof Long && o2 instanceof Record)
      rc = ((Long) o1).compareTo(((Record) o2).getId());
    else if (o1 instanceof Record && o2 instanceof Record)
      rc = ((Record) o1).compareTo((Record) o2);
    return rc;
  }

  /**
   * This function is generated.
   * hashCode() of Comparators that are equal must return the same value
   */
  public int hashCode() {
    int hash = 7;
    hash = 89 * hash + (this.id != null ? this.id.hashCode() : 0);
    return hash;
  }

  public boolean equals(Object o) {
    boolean rc = false;
    if (o instanceof IdComparator) {
      rc = id.equals(((IdComparator) o).id);
    }
    return rc;
  }

}
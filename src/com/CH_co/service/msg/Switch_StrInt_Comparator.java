/*
 * Copyright 2001-2010 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_co.service.msg;

import java.util.*;

import com.CH_co.trace.Trace;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.12 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */

public class Switch_StrInt_Comparator implements Comparator {

  private boolean compNames;
  private boolean nameIsFirst;
  private int strIndex;
  private int intIndex;

  public Switch_StrInt_Comparator(boolean compareNames, boolean isNameFirst) {
    compNames = compareNames;
    nameIsFirst = isNameFirst;
    strIndex = isNameFirst ? 0 : 1;
    intIndex = isNameFirst ? 1 : 0;
  }

  public boolean equals(Object o) {
    if (o instanceof Switch_StrInt_Comparator) {
      Switch_StrInt_Comparator sC = (Switch_StrInt_Comparator) o;
      return compNames == sC.compNames && nameIsFirst == sC.nameIsFirst;
    }
    return false;
  }
  public int hashCode() {
    return (Boolean.valueOf(compNames).hashCode()) + (2*Boolean.valueOf(nameIsFirst).hashCode());
  }

  public int compare(Object o1, Object o2) {
    //Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Switch_StrInt_Comparator.class, "compare(Object o1, Object o2)");
    //if (trace != null) trace.args(o1, o2);

    int rc = 0;
    if (o2 instanceof String) {
      Object[] obj1 = (Object[]) o1;
      rc = ((String)obj1[strIndex]).compareTo((String) o2);
    }
    else if (o1 instanceof String) {
      Object[] obj2 = (Object[]) o2;
      rc = ((String)o1).compareTo((String) obj2[strIndex]);
    }
    else if (o2 instanceof Integer) {
      Object[] obj1 = (Object[]) o1;
      rc = ((Integer)obj1[intIndex]).compareTo((Integer) o2);
    }
    else if (o1 instanceof Integer) {
      Object[] obj2 = (Object[]) o2;
      rc = ((Integer)o1).compareTo((Integer) obj2[intIndex]);
    }
    else {
      Object[] obj1 = (Object[]) o1;
      Object[] obj2 = (Object[]) o2;
      if (compNames)
        rc = ((String)obj1[strIndex]).compareTo((String) obj2[strIndex]);
      else
        rc = ((Integer)obj1[intIndex]).compareTo((Integer) obj2[intIndex]);
    }

    //if (trace != null) trace.exit(Switch_StrInt_Comparator.class, rc);
    return rc;
  }

}
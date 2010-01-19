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

package com.CH_gui.list;

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
 * <b>$Revision: 1.5 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class ListComparator extends Object implements Comparator {

  /** Creates new ListComparator */
  public ListComparator() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ListComparator.class, "ListComparator()");
    if (trace != null) trace.exit(ListComparator.class);
  }

  /**
   * Compare values by rendered text values
   */
  public int compare(Object obj1, Object obj2) {
    return ListRenderer.getRenderedText(obj1).compareToIgnoreCase(ListRenderer.getRenderedText(obj2));
  }

}
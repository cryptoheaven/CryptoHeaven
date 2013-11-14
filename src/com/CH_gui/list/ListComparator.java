/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.list;

import java.util.*;
import com.CH_co.trace.Trace;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.5 $</b>
 *
 * @author  Marcin Kurzawa
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
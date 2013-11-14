/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.sortedTable;

import java.util.EventObject;
import com.CH_co.trace.Trace;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.10 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class TableModelSortEvent extends EventObject {

  /** Creates new TableModelSortEvent */
  public TableModelSortEvent(Object source) {
    super(source);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableModelSortEvent.class, "TableModelSortEvent()");
    if (trace != null) trace.exit(TableModelSortEvent.class);
  }
  
}
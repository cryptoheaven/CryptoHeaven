/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.traceTable;

import com.CH_co.service.records.*;
import com.CH_gui.util.VisualsSavable;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.8 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class TraceActionMultiTable extends TraceActionTable implements VisualsSavable {

  /** Creates new TraceActionMultiTable */
  public TraceActionMultiTable(Record[] parentObjLinks) {
    super(parentObjLinks);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "TraceActionMultiTable";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }

}
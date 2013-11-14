/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.msgTable;

import com.CH_gui.table.RecordTableModel;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved. 
 *
 * @author  Marcin Kurzawa 
 */
public class MsgSentActionTable extends MsgActionTable {

  /** Creates new MsgSentActionTable */
  public MsgSentActionTable(RecordTableModel model, boolean previewMode) {
    super(model, previewMode);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "MsgSentActionTable";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
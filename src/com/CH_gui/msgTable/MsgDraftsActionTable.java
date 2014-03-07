/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.msgTable;

import javax.swing.*;

import com.CH_gui.table.RecordTableModel;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved. 
 *
 * @author  Marcin Kurzawa 
 */
public class MsgDraftsActionTable extends MsgActionTable {

  /** Creates new MsgDraftsActionTable */
  public MsgDraftsActionTable(RecordTableModel model, boolean previewMode) {
    super(model, previewMode);
  }

  /**
   * Overwrite to return New Message from Draft action.
   */
  public Action getDoubleClickAction() {
    return actions[NEW_FROM_DRAFT_ACTION];
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "MsgDraftsActionTable";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
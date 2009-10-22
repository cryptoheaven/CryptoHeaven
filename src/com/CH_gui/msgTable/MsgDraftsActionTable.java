/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_gui.msgTable;

import javax.swing.*;

import com.CH_gui.table.RecordTableModel;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class MsgDraftsActionTable extends MsgActionTable {

  /** Creates new MsgDraftsActionTable */
  public MsgDraftsActionTable(RecordTableModel model) {
    super(model);
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
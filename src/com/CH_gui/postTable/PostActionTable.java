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

package com.CH_gui.postTable;

import javax.swing.JViewport;

import com.CH_gui.gui.JBottomStickViewport;
import com.CH_gui.msgTable.MsgActionTable;
import com.CH_gui.table.RecordTableModel;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class PostActionTable extends MsgActionTable {

  /** Creates new PostActionTable */
  public PostActionTable(RecordTableModel model) {
    super(model);
    // disable auto-scrolls in viewport since row heights are variable and it doesn't quite work with variable row heights
    JViewport view = getViewport();
    if (view instanceof JBottomStickViewport) {
      ((JBottomStickViewport) view).setAutoScrollEnabled(false);
    }
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "PostActionTable";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
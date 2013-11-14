/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.postTable;

import com.CH_gui.gui.JBottomStickViewport;
import com.CH_gui.msgTable.MsgActionTable;
import com.CH_gui.table.RecordTableModel;
import javax.swing.JViewport;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved. 
 *
 * @author  Marcin Kurzawa 
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
/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.recycleTable;

import com.CH_co.trace.Trace;

import com.CH_gui.gui.*;
import com.CH_gui.table.*;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.1 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class RecycleTableComponent extends RecordTableComponent {

  /** Creates new RecycleTableComponent */
  public RecycleTableComponent(boolean suppressToolbar, boolean suppressUtilityBar, boolean suppressVisualsSavable) {
    super(new RecycleActionTable(), Template.get(Template.EMPTY_RECYCLE), Template.get(Template.NONE), null, suppressToolbar, suppressUtilityBar, suppressVisualsSavable);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleTableComponent.class, "RecycleTableComponent()");
    if (trace != null) trace.exit(RecycleTableComponent.class);
  }

  public void initDataModel(Long folderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleTableComponent.class, "initDataModel(Long folderId)");
    if (trace != null) trace.args(folderId);
    ((RecycleTableModel) getActionTable().getTableModel()).initData(folderId);
    if (trace != null) trace.exit(RecycleTableComponent.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "RecycleTableComponent";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
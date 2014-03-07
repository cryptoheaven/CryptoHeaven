/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.groupTable;

import com.CH_co.trace.Trace;

import com.CH_gui.gui.*;
import com.CH_gui.table.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.3 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class GroupTableComponent extends RecordTableComponent {

  /** Creates new GroupTableComponent */
  public GroupTableComponent(boolean suppressToolbar, boolean suppressUtilityBar, boolean suppressVisualsSavable) {
    super(new GroupActionTable(), Template.get(Template.EMPTY_GROUP), Template.get(Template.BACK_GROUP), Template.get(Template.CATEGORY_GROUP), suppressToolbar, suppressUtilityBar, suppressVisualsSavable);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GroupTableComponent.class, "GroupTableComponent()");
    if (trace != null) trace.exit(GroupTableComponent.class);
  }

  public void initDataModel(Long folderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GroupTableComponent.class, "initDataModel(Long folderId)");
    if (trace != null) trace.args(folderId);
    ((GroupTableModel) getActionTable().getTableModel()).initData(folderId);
    if (trace != null) trace.exit(GroupTableComponent.class);  
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "GroupTableComponent";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
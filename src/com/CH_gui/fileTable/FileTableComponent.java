/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.fileTable;

import com.CH_co.trace.Trace;

import com.CH_gui.gui.*;
import com.CH_gui.table.*;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.16 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class FileTableComponent extends RecordTableComponent {

  /** Creates new FileTableComponent */
//  public FileTableComponent() {
//    super(new FileActionTable(), Template.get(Template.EMPTY_FILES), Template.get(Template.BACK_FILES), Template.get(Template.CATEGORY_FILE));
//    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileTableComponent.class, "FileTableComponent()");
//    if (trace != null) trace.exit(FileTableComponent.class);
//  }

  public FileTableComponent(boolean suppressToolbar, boolean suppressUtilityBar, boolean suppressVisualsSavable) {
    super(new FileActionTable(), Template.get(Template.EMPTY_FILES), Template.get(Template.BACK_FILES), Template.get(Template.CATEGORY_FILE), suppressToolbar, suppressUtilityBar, suppressVisualsSavable);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileTableComponent.class, "FileTableComponent()");
    if (trace != null) trace.exit(FileTableComponent.class);
  }

  public void initDataModel(Long folderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileTableComponent.class, "initDataModel(Long folderId)");
    if (trace != null) trace.args(folderId);
    ((FileTableModel) getActionTable().getTableModel()).initData(folderId);
    if (trace != null) trace.exit(FileTableComponent.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "FileTableComponent";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
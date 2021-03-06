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

import com.CH_gui.gui.Template;
import com.CH_co.trace.Trace;

import com.CH_gui.gui.*;
import com.CH_gui.table.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.7 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class MsgDraftsTableComponent extends RecordTableComponent {

  /** Creates new MsgDraftsTableComponent */
  public MsgDraftsTableComponent(boolean previewMode, boolean suppressToolbar, boolean suppressUtilityBar, boolean suppressVisualsSavable) {
    super(new MsgDraftsActionTable(new MsgTableModel(null, MsgTableModel.MODE_DRAFTS), previewMode), Template.get(Template.EMPTY_MAIL_DRAFTS), null, null, suppressToolbar, suppressUtilityBar, suppressVisualsSavable);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgDraftsTableComponent.class, "MsgDraftsTableComponent(boolean previewMode)");
    if (trace != null) trace.args(previewMode);
    if (trace != null) trace.exit(MsgDraftsTableComponent.class);
  }

  public void initDataModel(Long folderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgDraftsTableComponent.class, "initDataModel(Long folderId)");
    if (trace != null) trace.args(folderId);
    ((MsgTableModel) getActionTable().getTableModel()).initData(folderId);
    if (trace != null) trace.exit(MsgDraftsTableComponent.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "MsgDraftsTableComponent";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
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

import com.CH_gui.gui.Template;
import com.CH_co.trace.Trace;

import com.CH_gui.gui.*;
import com.CH_gui.table.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.7 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class MsgDraftsTableComponent extends RecordTableComponent {

  /** Creates new MsgDraftsTableComponent */
  public MsgDraftsTableComponent(boolean previewMode) {
    super(new MsgDraftsActionTable(new MsgTableModel(null, MsgTableModel.MODE_DRAFTS), previewMode), Template.get(Template.EMPTY_MAIL_DRAFTS));
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
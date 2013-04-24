/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_gui.msgTable;

import com.CH_co.trace.Trace;

import com.CH_gui.gui.*;
import com.CH_gui.table.*;

/** 
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.16 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class MsgTableComponent extends RecordTableComponent {

  /** Creates new MsgTableComponent */
  public MsgTableComponent(boolean suppressToolbar, boolean suppressUtilityBar, boolean suppressVisualsSavable) {
    this(Template.get(Template.EMPTY_MAIL), false, suppressToolbar, suppressUtilityBar, suppressVisualsSavable);
  }
  public MsgTableComponent(String emptyTemplateName, boolean msgPreviewMode, boolean suppressToolbar, boolean suppressUtilityBar, boolean suppressVisualsSavable) {
    this(Template.get(Template.EMPTY_MAIL), Template.get(Template.NONE), Template.get(Template.CATEGORY_MAIL), msgPreviewMode, suppressToolbar, suppressUtilityBar, suppressVisualsSavable);
  }
  public MsgTableComponent(String emptyTemplateName, String backTemplateName, String categoryTemplateName, boolean msgPreviewMode, boolean suppressToolbar, boolean suppressUtilityBar, boolean suppressVisualsSavable) {
    super(new MsgActionTable(msgPreviewMode), emptyTemplateName, backTemplateName, categoryTemplateName, suppressToolbar, suppressUtilityBar, suppressVisualsSavable);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTableComponent.class, "MsgTableComponent()");
    if (trace != null) trace.exit(MsgTableComponent.class);
  }

  public void initDataModel(Long folderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTableComponent.class, "initDataModel(Long folderId)");
    if (trace != null) trace.args(folderId);
    ((MsgTableModel) getActionTable().getTableModel()).initData(folderId);
    if (trace != null) trace.exit(MsgTableComponent.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "MsgTableComponent";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
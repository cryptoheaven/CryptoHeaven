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

package com.CH_gui.groupTable;

import com.CH_gui.gui.Template;
import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.*;

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
 * <b>$Revision: 1.3 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class GroupTableComponent extends RecordTableComponent {

  /** Creates new GroupTableComponent */
  public GroupTableComponent() {
    super(new GroupActionTable(), Template.get(Template.EMPTY_GROUP), Template.get(Template.BACK_GROUP), Template.get(Template.CATEGORY_GROUP));
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
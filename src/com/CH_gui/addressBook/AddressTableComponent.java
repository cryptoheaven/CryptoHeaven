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

package com.CH_gui.addressBook;

import com.CH_gui.gui.Template;
import com.CH_co.trace.Trace;

import com.CH_gui.gui.*;
import com.CH_gui.msgTable.*;
import com.CH_gui.table.*;

/**
 * <b>Copyright</b> &copy; 2001-2010
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
 * <b>$Revision: 1.9 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class AddressTableComponent extends RecordTableComponent {

  /** Creates new AddressTableComponent */
  public AddressTableComponent(boolean previewMode, boolean suppressToolbar, boolean suppressUtilityBar, boolean suppressVisualsSavable) {
    this(new AddressActionTable(new MsgTableModel(null, MsgTableModel.MODE_ADDRESS), previewMode), Template.get(Template.EMPTY_ADDRESSES), suppressToolbar, suppressUtilityBar, suppressVisualsSavable);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AddressTableComponent.class, "AddressTableComponent(boolean previewMode)");
    if (trace != null) trace.exit(AddressTableComponent.class);
  }
  public AddressTableComponent(RecordActionTable actionTable, String emptyTemplate, boolean suppressToolbar, boolean suppressUtilityBar, boolean suppressVisualsSavable) {
    super(actionTable, emptyTemplate, null, null, suppressToolbar, suppressUtilityBar, suppressVisualsSavable);
  }

  public void initDataModel(Long folderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AddressTableComponent.class, "initDataModel(Long folderId)");
    if (trace != null) trace.args(folderId);
    ((MsgTableModel) getActionTable().getTableModel()).initData(folderId);
    if (trace != null) trace.exit(AddressTableComponent.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "AddressTableComponent";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
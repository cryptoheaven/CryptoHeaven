/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.userTable;

import com.CH_co.trace.Trace;
import com.CH_gui.table.*;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.7 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class SubUserTableComponent extends RecordTableComponent {

  /** Creates new SubUserTableComponent */
  public SubUserTableComponent() {
    super(new SubUserActionTable());
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SubUserTableComponent.class, "SubUserTableComponent()");
    setTitle("Available User Accounts for management.");
    if (trace != null) trace.exit(SubUserTableComponent.class);
  }

  /**
   * This call is currently ignored as sub-user accounts are only displayed for current user at once.
   */
  public void initDataModel(Long folderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SubUserTableComponent.class, "initDataModel(Long folderId)");
    if (trace != null) trace.args(folderId);
    //((UserTableModel) getActionTable().getTableModel()).initData(folderId);
    if (trace != null) trace.exit(SubUserTableComponent.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "SubUserTableComponent";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
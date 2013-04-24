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

package com.CH_gui.userTable;

import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.1 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class PassRecoveryUserActionTable extends UserActionTable {

  /** Creates new PassRecoveryUserActionTable */
  public PassRecoveryUserActionTable() {
    super(new UserTableModel(UserTableModel.columnHeaderData_passRecovery));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PassRecoveryUserActionTable.class, "PassRecoveryUserActionTable()");
    if (trace != null) trace.exit(PassRecoveryUserActionTable.class);
  }

  /****************************************************************************/
  /*        V i s u a l s S a v a b l e I                                  
  /****************************************************************************/
  public static final String visualsClassKeyName = "PassRecoveryUserActionTable";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }

}
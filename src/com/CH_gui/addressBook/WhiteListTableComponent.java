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
 * <b>$Revision: 1.2 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class WhiteListTableComponent extends AddressTableComponent {

  /** Creates new WhiteListTableComponent */
  public WhiteListTableComponent(boolean previewMode) {
    super(new WhiteListActionTable(new MsgTableModel(null, MsgTableModel.MODE_WHITELIST), previewMode), Template.get(Template.EMPTY_WHITELIST));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WhiteListTableComponent.class, "WhiteListTableComponent(boolean previewMode)");
    if (trace != null) trace.args(previewMode);
    if (trace != null) trace.exit(WhiteListTableComponent.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "WhiteListTableComponent";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
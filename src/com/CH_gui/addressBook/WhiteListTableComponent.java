/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.addressBook;

import com.CH_gui.gui.Template;
import com.CH_co.trace.Trace;

import com.CH_gui.gui.*;
import com.CH_gui.msgTable.*;
import com.CH_gui.table.*;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.2 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class WhiteListTableComponent extends AddressTableComponent {

  /** Creates new WhiteListTableComponent */
  public WhiteListTableComponent(boolean previewMode, boolean suppressToolbar, boolean suppressUtilityBar, boolean suppressVisualsSavable) {
    super(new WhiteListActionTable(new MsgTableModel(null, MsgTableModel.MODE_WHITELIST), previewMode), Template.get(Template.EMPTY_WHITELIST), suppressToolbar, suppressUtilityBar, suppressVisualsSavable);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WhiteListTableComponent.class, "WhiteListTableComponent(boolean previewMode)");
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
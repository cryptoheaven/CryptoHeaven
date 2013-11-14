/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.addressBook;

import javax.swing.*;

import com.CH_co.service.records.*;

import com.CH_gui.msgTable.MsgActionTable;
import com.CH_gui.table.RecordTableModel;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.8 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class AddressActionTable extends MsgActionTable {

  private static Integer versionedVisualsSavable = new Integer(1);

  /** Creates new AddressActionTable */
  public AddressActionTable(RecordTableModel model, boolean previewMode) {
    super(model, MsgDataRecord.OBJ_TYPE_ADDR, previewMode);
  }

  /**
   * Overwrite to return Compose to Address(es) action.
   */
  public Action getDoubleClickAction() {
    return actions[REPLY_TO_SENDER__OR__COMPOSE_TO_ADDRESS_ACTION];
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "AddressActionTable";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
  public Integer getVisualsVersion() {
    return versionedVisualsSavable;
  }
}
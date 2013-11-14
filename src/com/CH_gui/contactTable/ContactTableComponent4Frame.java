/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.contactTable;

import com.CH_co.service.records.Record;
import com.CH_co.service.records.filters.RecordFilter;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class ContactTableComponent4Frame extends ContactTableComponent {

  public ContactTableComponent4Frame(Record[] initialData, RecordFilter contactFilter, String emptyTemplateName, String backTemplateName, boolean withDoubleClickAction, boolean suppressToolbar, boolean suppressUtilityBar) {
    super(initialData, contactFilter, emptyTemplateName, backTemplateName, withDoubleClickAction, suppressToolbar, suppressUtilityBar);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "ContactTableComponent4Frame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }

}
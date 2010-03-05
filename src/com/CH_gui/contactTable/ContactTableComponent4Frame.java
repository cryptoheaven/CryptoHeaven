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

package com.CH_gui.contactTable;

import com.CH_co.service.records.Record;
import com.CH_co.service.records.filters.RecordFilter;

/**
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
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
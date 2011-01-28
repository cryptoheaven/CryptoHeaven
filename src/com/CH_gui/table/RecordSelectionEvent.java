/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_gui.table;

import java.util.EventObject;

import com.CH_co.service.records.Record;

/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class RecordSelectionEvent extends EventObject {

  private Record[] selectedRecords;

  /** Creates new RecordSelectionEvent */
  public RecordSelectionEvent(Object source, Record selectedRecord) {
    super(source);
    this.selectedRecords = new Record[] { selectedRecord };
  }

  /** Creates new RecordSelectionEvent */
  public RecordSelectionEvent(Object source, Record[] selectedRecords) {
    super(source);
    this.selectedRecords = selectedRecords;
  }

  public Record[] getSelectedRecords() {
    return selectedRecords;
  }

}
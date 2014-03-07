/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.table;

import java.util.EventObject;

import com.CH_co.service.records.Record;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved. 
 *
 * @author  Marcin Kurzawa 
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
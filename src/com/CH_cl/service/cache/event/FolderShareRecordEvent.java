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

package com.CH_cl.service.cache.event;

import com.CH_cl.service.cache.*;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.FolderShareRecord;

/** 
 * <b>Copyright</b> &copy; 2001-2011
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
 * <b>$Revision: 1.8 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FolderShareRecordEvent extends RecordEvent {

  /** Creates new FolderShareRecordEvent */
  public FolderShareRecordEvent(FetchedDataCache source, FolderShareRecord[] folderShareRecords, int eventType) {
    super(source, folderShareRecords, eventType);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderShareRecordEvent.class, "(FetchedDataCach, FolderShareRecord[], int eventType)");
    if (trace != null) trace.exit(FolderShareRecordEvent.class);
  }
  
  public FolderShareRecord[] getFolderShareRecords() {
    return (FolderShareRecord[]) records;
  }

}
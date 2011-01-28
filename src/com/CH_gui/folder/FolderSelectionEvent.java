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

package com.CH_gui.folder;

import java.util.EventObject;

import com.CH_co.service.records.FolderRecord;
import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.7 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FolderSelectionEvent extends EventObject {

  public FolderRecord selectedFolderRecord;

  /** Creates new FolderSelectionEvent */
  public FolderSelectionEvent(Object source, FolderRecord fRec) {
    super(source);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderSelectionEvent.class, "FolderSelectionEvent(Object source, FolderRecord fRec)");
    this.selectedFolderRecord = fRec;
    if (trace != null) trace.exit(FolderSelectionEvent.class);
  }

}
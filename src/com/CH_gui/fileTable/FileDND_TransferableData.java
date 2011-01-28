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

package com.CH_gui.fileTable;

import java.io.Serializable;

import com.CH_co.service.records.*;

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
 * <b>$Revision: 1.9 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FileDND_TransferableData extends Object implements Serializable {
  public Long[][] fileRecordIDs;
  public FileDND_TransferableData() {
  }
  public FileDND_TransferableData(FolderPair[] fPairs,FileLinkRecord[] fLinks) {
    fileRecordIDs = new Long[2][];
    fileRecordIDs[0] = RecordUtils.getIDs(fPairs);
    fileRecordIDs[1] = RecordUtils.getIDs(fLinks);
  }
}
/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.fileTable;

import java.io.Serializable;

import com.CH_co.service.records.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.9 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class FileDND_TransferableData extends Object implements Serializable {
  public Long[][] fileRecordIDs;
  public FileDND_TransferableData() {
  }
  public FileDND_TransferableData(FolderPair[] fPairs,FileLinkRecord[] fLinks, FileLinkRecord[] fLinksAllVersions) {
    fileRecordIDs = new Long[3][];
    fileRecordIDs[0] = RecordUtils.getIDs(fPairs);
    fileRecordIDs[1] = RecordUtils.getIDs(fLinks);
    fileRecordIDs[2] = RecordUtils.getIDs(fLinksAllVersions);
  }
}
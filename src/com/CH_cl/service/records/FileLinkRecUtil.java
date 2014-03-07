/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.records;

import com.CH_cl.service.cache.*;

import com.CH_co.service.records.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.5 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class FileLinkRecUtil extends Object {

  public static String getLocationInfo(FileLinkRecord fileLinkRecord) {
    String locationType = "";
    String locationDesc = "";

    short type = fileLinkRecord.ownerObjType.shortValue();
    switch (type) {
      case Record.RECORD_TYPE_FOLDER:
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        FolderShareRecord sRec = cache.getFolderShareRecordMy(fileLinkRecord.ownerObjId, true);
        if (sRec != null)
          locationDesc = sRec.getFolderName();
        FolderRecord fRec = cache.getFolderRecord(fileLinkRecord.ownerObjId);
        if (fRec != null)
          locationType = fRec.getFolderType();
        break;
      default:
        throw new IllegalArgumentException("Type " + type + " not yet supported!");
    }

    return locationType + " (" + locationDesc + ")";
  }


  public static Record getLocationRecord(FileLinkRecord fileLinkRecord) {
    Record locationRecord = null;
    short type = fileLinkRecord.ownerObjType.shortValue();
    switch (type) {
      case Record.RECORD_TYPE_FOLDER:
        //locationType = FolderRecord.getFolderType(type);
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        FolderShareRecord shareRecord = cache.getFolderShareRecordMy(fileLinkRecord.ownerObjId, true);
        FolderRecord folderRecord = cache.getFolderRecord(fileLinkRecord.ownerObjId);
        locationRecord = new FolderPair(shareRecord, folderRecord);
        break;
      default:
        throw new IllegalArgumentException("Type " + type + " not yet supported!");
    }

    return locationRecord;
  }

}
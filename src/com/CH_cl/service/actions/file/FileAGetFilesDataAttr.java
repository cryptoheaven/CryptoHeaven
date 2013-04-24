/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_cl.service.actions.file;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_co.cryptx.BA;

import com.CH_co.trace.Trace;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.file.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;

/** 
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
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
public class FileAGetFilesDataAttr extends ClientMessageAction {

  /** Creates new FileAGetFilesDataAttr */
  public FileAGetFilesDataAttr() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileAGetFilesDataAttr.class, "FileAGetFilesDataAttr()");
    if (trace != null) trace.exit(FileAGetFilesDataAttr.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileAGetFilesDataAttr.class, "runAction(Connection)");

    // get the returning data set
    File_GetAttr_Rp reply = (File_GetAttr_Rp) getMsgDataSet();
    Long[] fileLinkIds = reply.fileLinkIds;
    FileDataRecord[] dataRecords = reply.fileDataRecords;

    if (fileLinkIds != null && fileLinkIds.length == 1 && fileLinkIds[0].longValue() == -1) {
      // no-op -- this is a reply to file-lob-uploader integrity check
    } else {
      // data cache for fetching required keys
      FetchedDataCache cache = getFetchedDataCache();

      for (int i=0; i<dataRecords.length; i++) {
        FileDataRecord dataRecord = dataRecords[i];
        if (!BA.isEmptyOrZero(dataRecord.getEncOrigDataDigest()) &&
                !BA.isEmptyOrZero(dataRecord.getEncSignedOrigDigest()) &&
                !BA.isEmptyOrZero(dataRecord.getEncEncDataDigest()) )
        {
          FileLinkRecord linkRecord = cache.getFileLinkRecord(fileLinkIds[i]);
          if (linkRecord != null) {
            // get the veryfying key
            Long keyId = dataRecord.getSigningKeyId();
            KeyRecord verifyingKeyRecord = cache.getKeyRecord(keyId);

            if (verifyingKeyRecord == null) {
              // we need to fetch the verifying key before we can unseal!
              getServerInterfaceLayer().submitAndWait(new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_KEYIDS, new Obj_IDList_Co(keyId)), 120000);
              verifyingKeyRecord = cache.getKeyRecord(keyId);
            }
            // un-seal the data record -- create the plain version of encrypted attributes
            dataRecord.unSeal(verifyingKeyRecord, linkRecord.getSymmetricKey());
          }
        }
      }
      cache.addFileDataRecords(dataRecords);
    }

    if (trace != null) trace.exit(FileAGetFilesDataAttr.class, null);
    return null;
  }

}
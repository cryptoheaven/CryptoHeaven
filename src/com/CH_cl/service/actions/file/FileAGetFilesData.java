/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
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
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.*;

import com.CH_co.cryptx.*;
import com.CH_co.monitor.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.dataSets.file.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import java.io.File;

/** 
* Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.10 $</b>
*
* @author  Marcin Kurzawa
*/
public class FileAGetFilesData extends ClientMessageAction {

  private File destinationDirectory;

  /** Creates new FileAGetFilesData */
  public FileAGetFilesData() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileAGetFilesData.class, "FileAGetFilesData()");
    if (trace != null) trace.exit(FileAGetFilesData.class);
  }

  public void setDestinationDirectory(File destinationDirectory) {
    this.destinationDirectory = destinationDirectory;
  }

  /**
  * The action handler performs all actions related to the received message (reply),
  * and optionally returns a request Message.  If there is no request, null is returned.
  */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileAGetFilesData.class, "runAction(Connection)");

    // get the returning data set
    File_GetData_Rp reply = (File_GetData_Rp) getMsgDataSet();
    Long[] fileLinkIds = reply.fileLinkIds;
    FileDataRecord[] fileDataRecords = reply.fileDataRecords;
    Long errFileId = reply.errFileId;
    Long errFileLinkId = reply.errFileLinkId;
    boolean errIsPermanent = reply.errIsPermanent;

    if (errFileId != null) {
      FetchedDataCache cache = getFetchedDataCache();
      if (errIsPermanent)
        NotificationCenter.show(NotificationCenter.ERROR_MESSAGE, "Download Aborted", "File is not available for downloading, because the original upload was permanently aborted!\n\n"+cache.getFileLinkRecord(errFileLinkId).getFileName());
      else
        NotificationCenter.show(NotificationCenter.ERROR_MESSAGE, "Download Aborted", "File is currently not available for downloading, because the original upload has not finished.  Please try again later.\n\n"+cache.getFileLinkRecord(errFileLinkId).getFileName());
    } else {
      // data cache for fetching required keys
      FetchedDataCache cache = getFetchedDataCache();

      ProgMonitorI progressMonitor = ProgMonitorPool.getProgMonitor(getStamp());

      for (int i=0; i<fileDataRecords.length; i++) {
        ServerInterfaceLayer SIL = getServerInterfaceLayer();
        FileDataRecord fileDataRecord = fileDataRecords[i];

        // get the file link for this data record which contains the symmetricKey
        FileLinkRecord fileLinkRecord = cache.getFileLinkRecord(fileLinkIds[i]);
        // get the veryfying key
        Long keyId = fileDataRecord.getSigningKeyId();
        KeyRecord verifyingKeyRecord = cache.getKeyRecord(keyId);

        if (verifyingKeyRecord == null) {
          // we need to fetch the verifying key before we can unseal!
          SIL.submitAndWait(new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_KEYIDS, new Obj_IDList_Co(keyId)), 120000);
          verifyingKeyRecord = cache.getKeyRecord(keyId);
        }

        // We'll need completed data attributes before we can unseal!
        FileDataRecord fileData = FileDataOps.getOrFetchFileDataAttr(SIL, fileLinkRecord.fileLinkId, fileDataRecord.fileId);
        fileDataRecord.merge(fileData);

        try {
          BASymmetricKey symmetricKey = fileLinkRecord.getSymmetricKey();
          if (symmetricKey == null) {
            if (fileLinkRecord.ownerObjType.shortValue() == Record.RECORD_TYPE_MESSAGE) {
              MsgDataRecord parentData = cache.getMsgDataRecord(fileLinkRecord.ownerObjId);
              if (parentData != null && parentData.bodyPassHash != null && parentData.getSymmetricBodyKey() == null)
                throw new IllegalStateException("Please use the message password first to access the content. File symmetric key is not available without matching password.");
            }
            throw new IllegalStateException("File symmetric key is not available.");
          }
          // un-seal the data record -- create the plain version of encrypted file and verify signatures
          Boolean isDefaultTempDir = destinationDirectory != null && destinationDirectory.equals(DownloadUtilities.getDefaultTempDir()) ? Boolean.TRUE : Boolean.FALSE;
          fileDataRecord.unSeal(verifyingKeyRecord, symmetricKey,
                                    destinationDirectory, isDefaultTempDir, fileLinkRecord.getFileName(),
                                    progressMonitor, fileLinkRecord.origSize);
          Stats.setStatus("File downloaded: "+fileLinkRecord.getFileName());
          StatOps.markOldIfNeeded(SIL, fileLinkRecord.fileLinkId, FetchedDataCache.STAT_TYPE_INDEX_FILE);
          FileLinkOps.markRecordsAs(SIL, new FileLinkRecord[] { fileLinkRecord }, Short.valueOf(StatRecord.FLAG_READ));
        } catch (Throwable t) {
          // Failure of one of the files, should not affect the other when processing a few of them here.
          if (trace != null) trace.exception(FileAGetFilesData.class, 100, t);

          // show error dialog
          if (isCancelled() || progressMonitor.isCancelled()) {
            // Just ignore the error as operation was cancelled by the user.
            // Just incase it was a streaming reply, set the message as canceled for others to see.
            setCancelled();
          } else {
            String msg = "Exception message is : " + t.getMessage() +
                        "\n\nError occurred while decrypting and decompressing of file : " + fileLinkRecord.getFileName() +
                        "\nThe attempted destination directory was : " + destinationDirectory.getAbsolutePath() +
                        "\nThe private key for digest verification is : " + verifyingKeyRecord.verboseInfo() +
                        "\nThe symmetric key for decryption of the file is : " + (fileLinkRecord.getSymmetricKey() != null ? fileLinkRecord.getSymmetricKey().verboseInfo() : null);
            String title = "Error Dialog";
            NotificationCenter.show(NotificationCenter.ERROR_MESSAGE, title, msg);
          }
        }
      }
      cache.addFileDataRecords(fileDataRecords);
    }

    if (trace != null) trace.exit(FileAGetFilesData.class, null);
    return null;
  }

}
/*
* Copyright 2001-2012 by CryptoHeaven Corp.,
* Mississauga, Ontario, Canada.
* All rights reserved.
*
* This software is the confidential and proprietary information
* of CryptoHeaven Corp. ("Confidential Information").  You
* shall not disclose such Confidential Information and shall use
* it only in accordance with the terms of the license agreement
* you entered into with CryptoHeaven Corp.
*/

package com.CH_co.service.records;

import com.CH_co.cryptx.BASymCipherBulk;
import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.cryptx.SymmetricBulkCipher;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import com.CH_co.util.ImageNums;
import com.CH_co.util.Misc;
import com.CH_co.util.NotificationCenter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/** 
* <b>Copyright</b> &copy; 2001-2012
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
* <b>$Revision: 1.26 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class FileLinkRecord extends FileRecord implements LinkRecordI {

  public static final short STATUS_FLAG__DEFAULT = 0;
  public static final short STATUS_FLAG__STARRED = 32;
  public static final short STATUS_FLAG__UPLOAD_INCOMPLETE = 64;
  public static final short STATUS_FLAG__UPLOAD_ABORTED = 128;

  public Long fileLinkId;
  public Long fileId;
  public Long ownerObjId;
  public Short ownerObjType;
  private BASymCipherBulk encFileType;
  private BASymCipherBulk encFileName;
  private BASymCipherBulk encFileDesc;
  private BASymCipherBulk encSymmetricKey;
  public Long origSize;
  public Short status;
  public Timestamp recordCreated;
  public Timestamp recordUpdated;

  /** unwrapped variables */
  private String fileType;
  private String fileName = "unknown";
  private String fileDesc;
  private BASymmetricKey symmetricKey;

  /** Creates new FileLinkRecord */
  public FileLinkRecord() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLinkRecord.class, "FileLinkRecord()");
    if (trace != null) trace.exit(FileLinkRecord.class);
  }

  public Long getId() {
    return fileLinkId;
  }

  public int getIcon() {
    return ImageNums.IMAGE_SPECIAL_HANDLING;
  }

  public Long getParentId() {
    return ownerObjId;
  }
  public Long getFileViewParentId() {
    return ownerObjId;
  }

  public String getMyName() {
    return fileName;
  }

  public boolean isStarred() {
    return status != null && (status.shortValue() & STATUS_FLAG__STARRED) != 0;
  }
  public void markStarred(boolean setStar) {
    status = (Short) Misc.setBitObj(setStar, status != null ? status : new Short(STATUS_FLAG__DEFAULT), STATUS_FLAG__STARRED);
  }

  public boolean isIncomplete() {
    return status != null && (status.shortValue() & STATUS_FLAG__UPLOAD_INCOMPLETE) != 0;
  }
  public void markIncomplete(boolean setFlag) {
    status = (Short) Misc.setBitObj(setFlag, status != null ? status : new Short(STATUS_FLAG__DEFAULT), STATUS_FLAG__UPLOAD_INCOMPLETE);
  }

  public boolean isAborted() {
    return status != null && (status.shortValue() & STATUS_FLAG__UPLOAD_ABORTED) != 0;
  }
  public void markAborted(boolean setFlag) {
    status = (Short) Misc.setBitObj(setFlag, status != null ? status : new Short(STATUS_FLAG__DEFAULT), STATUS_FLAG__UPLOAD_ABORTED);
  }

  public void setEncFileType      (BASymCipherBulk encFileType)     { this.encFileType = encFileType;         }
  public void setEncFileName      (BASymCipherBulk encFileName)     { this.encFileName = encFileName;         }
  public void setEncFileDesc      (BASymCipherBulk encFileDesc)     { this.encFileDesc = encFileDesc;         }
  public void setEncSymmetricKey  (BASymCipherBulk encSymmetricKey) { this.encSymmetricKey = encSymmetricKey; }
  public void setFileType         (String fileType)                 { this.fileType = fileType;               }
  public void setFileName         (String fileName)                 { this.fileName = fileName;               }
  public void setFileDesc         (String fileDesc)                 { this.fileDesc = fileDesc;               }
  public void setSymmetricKey     (BASymmetricKey symmetricKey)     { this.symmetricKey = symmetricKey;       }


  public BASymCipherBulk  getEncFileType()      { return encFileType;     }
  public BASymCipherBulk  getEncFileName()      { return encFileName;     }
  public BASymCipherBulk  getEncFileDesc()      { return encFileDesc;     }
  public BASymCipherBulk  getEncSymmetricKey()  { return encSymmetricKey; }
  public String           getFileType()         { return fileType;        }
  public String           getFileName()         { return fileName;        }
  public String           getFileDesc()         { return fileDesc;        }
  public BASymmetricKey   getSymmetricKey()     { return symmetricKey;    }


  /**
  * Seals the <code> fileType, fileName, symmetricKey </code>
  * into <code> encFileType, encFileName, encSymmetricKey </code>
  * using the sealant object which is the owner object's symmetric key.
  * @param owner's symmetric key
  */
  public void seal(BASymmetricKey sealingKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLinkRecord.class, "seal(BASymmetricKey sealingKey)");
    if (trace != null) trace.args(sealingKey);

    try {

      // seal the name and type
      seal();
      // encrypt the symmetric with which object is sealed with the owner's symmetric key
      encSymmetricKey = new SymmetricBulkCipher(sealingKey).bulkEncrypt(symmetricKey);

    } catch (Throwable t) {
      if (trace != null) trace.exception(FileLinkRecord.class, 100, t);
      if (trace != null) trace.data(101, this);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(FileLinkRecord.class);
  }

  /**
  * Seals the fileName and fileType with this object's symmetricKey.
  */
  public void seal() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLinkRecord.class, "seal()");

    if (symmetricKey == null) {
      NotificationCenter.show(NotificationCenter.ERROR_MESSAGE, "Error", "Encryption key is not available.");
    } else {
      try {
        // encrypt the file type and file name with symmetric key
        SymmetricBulkCipher symCipher = new SymmetricBulkCipher(symmetricKey);
        BASymCipherBulk tempEncFileType = symCipher.bulkEncrypt(fileType);
        BASymCipherBulk tempEncFileName = symCipher.bulkEncrypt(fileName);
        BASymCipherBulk tempEncFileDesc = fileDesc != null ? symCipher.bulkEncrypt(fileDesc) : null;

        super.seal();

        // assign all sealed variables
        encFileType = tempEncFileType;
        encFileName = tempEncFileName;
        encFileDesc = tempEncFileDesc;

      } catch (Throwable t) {
        if (trace != null) trace.exception(FileLinkRecord.class, 100, t);
        if (trace != null) trace.data(101, this);
        throw new SecurityException(t.getMessage());
      }
    }

    if (trace != null) trace.exit(FileLinkRecord.class);
  }

  /**
  * Unseals the <code> encFileType, encFileName, encSymmetricKey </code>
  * into <code> fileType, fileName, symmetricKey </code> .
  * using the unSealant object which is the owner object's symmetric key.
  * @param owner's symmetric key
  */
  public void unSeal(BASymmetricKey unsealingKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLinkRecord.class, "unSeal(BASymmetricKey unsealingKey)");
    if (trace != null) trace.args(unsealingKey);

    try {
      // decipher the symmetric key with which file data in encrypted
      BASymmetricKey tempSymmetricKey = new BASymmetricKey(new SymmetricBulkCipher(unsealingKey).bulkDecrypt(encSymmetricKey));

      // decipher file name and file type
      SymmetricBulkCipher symCipher = new SymmetricBulkCipher(tempSymmetricKey);
      String tempFileType = symCipher.bulkDecrypt(encFileType).toByteStr();
      String tempFileName = symCipher.bulkDecrypt(encFileName).toByteStr();
      String tempFileDesc = encFileDesc != null ? symCipher.bulkDecrypt(encFileDesc).toByteStr() : null;

      super.unSeal();

      // assign all unsealed variables
      symmetricKey = tempSymmetricKey;
      fileType = tempFileType;
      fileName = tempFileName;
      fileDesc = tempFileDesc;

    } catch (Throwable t) {
      if (trace != null) trace.exception(FileLinkRecord.class, 100, t);
      if (trace != null) trace.data(101, this);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(FileLinkRecord.class);
  }

  public static FileLinkRecord getMostRecent(Collection fLinks) {
    FileLinkRecord mostRecent = null;
    if (fLinks != null) {
      Iterator iter = fLinks.iterator();
      while (iter.hasNext()) {
        FileLinkRecord fLink = (FileLinkRecord) iter.next();
        if (mostRecent == null || fLink.recordCreated.after(mostRecent.recordCreated))
          mostRecent = fLink;
      }
    }
    return mostRecent;
  }

  public static Long[] getOwnerObjIDs(FileLinkRecord[] fileLinks, short ownerType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLinkRecord.class, "getOwnerObjIDs(FileLinkRecord[] fileLinks, short ownerType)");
    if (trace != null) trace.args(fileLinks);
    if (trace != null) trace.args(ownerType);

    Long[] ownerObjIDs = null;
    if (fileLinks != null) {
      ArrayList fileLinksL = null;
      for (int i=0; i<fileLinks.length; i++) {
        if (fileLinks[i].ownerObjType.shortValue() == ownerType) {
          Long id = fileLinks[i].ownerObjId;
          if (fileLinksL == null) fileLinksL = new ArrayList();
          if (!fileLinksL.contains(id))
            fileLinksL.add(id);
        }
      }
      ownerObjIDs = (Long[]) ArrayUtils.toArray(fileLinksL, Long.class);
    }

    if (trace != null) trace.exit(FileLinkRecord.class, ownerObjIDs);
    return ownerObjIDs;
  }

  public static Long[] getFileIDs(FileLinkRecord[] fileLinks) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLinkRecord.class, "getFileIDs(FileLinkRecord[] fileLinks)");
    if (trace != null) trace.args(fileLinks);

    Long[] fileIDs = null;
    if (fileLinks != null) {
      fileIDs = new Long[fileLinks.length];
      for (int i=0; i<fileLinks.length; i++) {
        fileIDs[i] = fileLinks[i].fileId;
      }
      fileIDs = (Long[]) ArrayUtils.removeDuplicates(fileIDs);
    }

    if (trace != null) trace.exit(FileLinkRecord.class, fileIDs);
    return fileIDs;
  }

  public static long getFileOrigSizeSum(FileLinkRecord[] fileLinks) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileLinkRecord.class, "getFileOrigSizeSum(FileLinkRecord[] fileLinks)");
    if (trace != null) trace.args(fileLinks);

    long sum = 0;
    if (fileLinks != null) {
      for (int i=0; i<fileLinks.length; i++) {
        FileLinkRecord link = fileLinks[i];
        sum += link.origSize != null ? link.origSize.longValue() : 0;
      }
    }

    if (trace != null) trace.exit(FileLinkRecord.class, sum);
    return sum;
  }

  public void merge(Record updated) {
    if (updated instanceof FileLinkRecord) {
      FileLinkRecord record = (FileLinkRecord) updated;
      if (record.fileLinkId        != null) fileLinkId       = record.fileLinkId;
      if (record.fileId            != null) fileId           = record.fileId;
      if (record.ownerObjId        != null) ownerObjId       = record.ownerObjId;
      if (record.ownerObjType      != null) ownerObjType     = record.ownerObjType;
      if (record.encFileType       != null) encFileType      = record.encFileType;
      if (record.encFileName       != null) encFileName      = record.encFileName;
      if (record.encFileDesc       != null) encFileDesc      = record.encFileDesc;
      if (record.encSymmetricKey   != null) encSymmetricKey  = record.encSymmetricKey;
      if (record.origSize          != null) origSize         = record.origSize;
      if (record.status            != null) status           = record.status;
      if (record.recordCreated     != null) recordCreated    = record.recordCreated;
      if (record.recordUpdated     != null) recordUpdated    = record.recordUpdated;

      // un-sealed data
      if (record.fileType          != null) fileType         = record.fileType;
      if (record.fileName          != null) fileName         = record.fileName;
      if (record.fileDesc          != null) fileDesc         = record.fileDesc;
      if (record.symmetricKey      != null) symmetricKey     = record.symmetricKey;

      // After fetching, it is unSealed and later on merged into the cache,
      // so copy the unSealed flag too, but don't reset it if fetched data
      // was not unSealed and cached copy might be already unSealed.
      if (updated.isUnSealed())
        setUnSealed(true);
    }
    else
      super.mergeError(updated);
  }


  public String toString() {
    return "[FileLinkRecord"
      + ": fileLinkId="       + fileLinkId
      + ", fileId="           + fileId
      + ", ownerObjId="       + ownerObjId
      + ", ownerObjType="     + ownerObjType
      + ", encFileType="      + encFileType
      + ", encFileName="      + encFileName
      + ", encFileDesc="      + encFileDesc
      + ", encSymmetricKey="  + encSymmetricKey
      + ", origSize="         + origSize
      + ", status="           + status
      + ", recordCreated="    + recordCreated
      + ", recordUpdated="    + recordUpdated
      + ", un-sealed data >> "
      + ", fileType="         + fileType
      + ", fileName="         + fileName
      + ", fileDesc="         + fileDesc
      + ", symmetricKey="     + symmetricKey
      + "]";
  }

  public String toStringLongFormat() {
    return "FileLinkRecord"
      + "\n: fileLinkId="       + fileLinkId
      + "\n, fileId="           + fileId
      + "\n, ownerObjId="       + ownerObjId
      + "\n, ownerObjType="     + ownerObjType
      + "\n, encFileType="      + encFileType
      + "\n, encFileName="      + encFileName
      + "\n, encFileDesc="      + encFileDesc
      + "\n, encSymmetricKey="  + encSymmetricKey
      + "\n, origSize="         + origSize
      + "\n, status="           + status
      + "\n, recordCreated="    + recordCreated
      + "\n, recordUpdated="    + recordUpdated
      + "\n un-sealed data >> "
      + "\n, fileType="         + fileType
      + "\n, fileName="         + fileName
      + "\n, fileDesc="         + fileDesc
      + "\n, symmetricKey="     + symmetricKey;
  }

  /**********************
  * LinkRecordI methods
  *********************/
  public int getCompatibleStatTypeIndex() {
    return StatRecord.STAT_TYPE_INDEX_FILE;
  }

  public Timestamp getCreatedStamp() {
    return recordCreated;
  }

  public Long getObjId() {
    return fileId;
  }

  public Short getOwnerObjType() {
    return ownerObjType;
  }

  public Long getOwnerObjId() {
    return ownerObjId;
  }

  public void setId(Long id) {
    fileLinkId = id;
  }

}
/*
 * Copyright 2001-2010 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_co.service.records;

import java.sql.Timestamp;
import java.util.*;

import com.CH_co.cryptx.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class FolderShareRecord extends Record implements LinkRecordI {

  public static final short NO = 1;
  public static final short YES = 2;

  public static final long SHARE_LOCAL_ID = -501;

  public static final long CATEGORY_MAIL_ID = -401;
  public static final long CATEGORY_FILE_ID = -301;
  public static final long CATEGORY_CHAT_ID = -201;
  public static final long CATEGORY_GROUP_ID = -101;

  public static final String SHARE_LOCAL_NAME = "My Local Computer";
  public static final String SHARE_LOCAL_DESC = "Local File System View";

  public static final String CATEGORY_FILE_NAME = "Files";
  public static final String CATEGORY_FILE_DESC = " "; //"File Folders [DESCRIPTION]";
  public static final String CATEGORY_MAIL_NAME = "eMail";
  public static final String CATEGORY_MAIL_DESC = " "; //"eMail Folders [DESCRIPTION]";
  public static final String CATEGORY_CHAT_NAME = "Instant Message History";
  public static final String CATEGORY_CHAT_DESC = " "; //"Instant Message History [DESCRIPTION]";
  public static final String CATEGORY_GROUP_NAME = "Groups";
  public static final String CATEGORY_GROUP_DESC = " "; //"Groups [DESCRIPTION]";

  public static final short SHOW_HIDE__ALWAYS_SHOW = 1;
  public static final short SHOW_HIDE__HIDE_WHEN_NOTHING_NEW = 2;
  public static final short SHOW_HIDE__ALWAYS_HIDE = 2;

  public Long shareId;
  public Long folderId;
  public Short ownerType; // user or group
  public Long ownerUserId;
  private Long viewParentId;  // View parent folder ID to manage other people's folders in the tree view...
  private BASymCipherBulk encFolderName;
  private BASymCipherBulk encFolderDesc;
  private BA encSymmetricKey; // this maybe BAAsyCipherBlock or BASymCipherBulk
  private Long pubKeyId;
  public Short canWrite;
  public Short canDelete;
  public Timestamp dateCreated;
  public Timestamp dateUpdated;

  /** unwrapped data */
  private String folderName;
  private String folderDesc;
  private BASymmetricKey symmetricKey;

  /** cache GUI view hierarchy */
  public Long guiViewParentId;

  /** Creates new FolderShareRecord */
  public FolderShareRecord() {
  }

  public Long getId() {
    return shareId;
  }

  public int getIcon() {
    int icon = ImageNums.IMAGE_NONE;
    if (ownerType.shortValue() == Record.RECORD_TYPE_GROUP)
      icon = ImageNums.PEOPLE16;
    return icon;
  }

  public void setViewParentId   (Long viewParentId              ) { this.viewParentId   = viewParentId;   }
  public void setEncFolderName  (BASymCipherBulk encFolderName  ) { this.encFolderName  = encFolderName;  }
  public void setEncFolderDesc  (BASymCipherBulk encFolderDesc  ) { this.encFolderDesc  = encFolderDesc;  }
  public void setEncSymmetricKey(BA encSymmetricKey             ) { this.encSymmetricKey= encSymmetricKey;}
  public void setFolderName     (String folderName              ) { this.folderName     = folderName;     }
  public void setFolderDesc     (String folderDesc              ) { this.folderDesc     = folderDesc;     }
  public void setSymmetricKey   (BASymmetricKey symmetricKey    ) { this.symmetricKey   = symmetricKey;   }
  public void setPubKeyId       (Long pubKeyId                  ) { this.pubKeyId       = pubKeyId;       }


  public Long             getViewParentId()   { return viewParentId;    }
  public BASymCipherBulk  getEncFolderName()  { return encFolderName;   }
  public BASymCipherBulk  getEncFolderDesc()  { return encFolderDesc;   }
  public BA               getEncSymmetricKey(){ return encSymmetricKey; }
  public Long             getPubKeyId()       { return pubKeyId;        }
  public String           getFolderName()     { return folderName;      }
  public String           getFolderDesc()     { return folderDesc;      }

  public BASymmetricKey getSymmetricKey() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderShareRecord.class, "getSymmetricKey()");
    if (trace != null) trace.exit(FolderShareRecord.class, symmetricKey);
    return symmetricKey;
  }


  /**
   * Seals the <code> folderName, folderDesc, symmetricKey </code>
   * into <code> encFolderName, encFolderDesc, encSymmetricKey </code>
   * using the sealant object which is the Key Record and BASymmetricKey.
   * Also sets pubKeyId.
   */
  public void seal(KeyRecord keyRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderShareRecord.class, "seal(KeyRecord)");

    seal();

    try {
      BAAsyCipherBlock tempEncSymmetricKey = new BAAsyPlainBlock(symmetricKey).encrypt(keyRecord.plainPublicKey);

      pubKeyId = keyRecord.keyId;
      encSymmetricKey = tempEncSymmetricKey;
    } catch (Throwable t) {
      if (trace != null) trace.exception(FolderShareRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(FolderShareRecord.class);
  }

  /**
   * Seals the <code> folderName, folderDesc </code>
   * into <code> encFolderName, encFolderDesc </code>
   * using the sealant object which is the BASymmetricKey of this record.
   */
  public void seal() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderShareRecord.class, "seal()");

    try {
      SymmetricBulkCipher symCipher = new SymmetricBulkCipher(symmetricKey);
      BASymCipherBulk tempEncFolderName = symCipher.bulkEncrypt(folderName);
      BASymCipherBulk tempEncFolderDesc = folderDesc != null ? symCipher.bulkEncrypt(folderDesc) : null;

      super.seal();

      encFolderName = tempEncFolderName;
      encFolderDesc = tempEncFolderDesc;
    } catch (Throwable t) {
      if (trace != null) trace.exception(FolderShareRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(FolderShareRecord.class);
  }


  /**
   * Seals the <code> folderName, folderDesc, symmetricKey </code>
   * to <code> encFolderName, endFolderDesc, encSymmetricKey </code>
   * using the sealant object which is the owner's symKeyFldShares.
   */
  public void seal(BASymmetricKey symKeyFldShares) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderShareRecord.class, "seal(BASymmetricKey symKeyFldShares)");

    try {
      SymmetricBulkCipher symCipher = new SymmetricBulkCipher(symKeyFldShares);
      BASymCipherBulk tempEncSymmetricKey = symCipher.bulkEncrypt(symmetricKey);
      BASymCipherBulk tempEncFolderName = symCipher.bulkEncrypt(folderName);
      BASymCipherBulk tempEncFolderDesc = folderDesc != null ? symCipher.bulkEncrypt(folderDesc) : null;

      super.seal();

      pubKeyId = null;
      encSymmetricKey = tempEncSymmetricKey;
      encFolderName = tempEncFolderName;
      encFolderDesc = tempEncFolderDesc;
    } catch (Throwable t) {
      if (trace != null) trace.exception(FolderShareRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(FolderShareRecord.class);
  }


  /**
   * Unseals the <code> encFolderName, encFolderDesc, encSymmetricKey </code>
   * into <code> folderName, folderDesc, symmetricKey </code>
   * using the unSealant object which is the RSAPrivateKey and BASymmetricKey.
   */
  public void unSeal(RSAPrivateKey privateKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderShareRecord.class, "unSeal(RSAPrivateKey)");
    if (trace != null) trace.args(privateKey);

    if (trace != null) trace.data(10, "this", this);
    if (encSymmetricKey != null && encFolderName != null) {
      try {
        // if the folder does not decrypt correctly, show some default title anyway...
        folderName = "unknown (encrypted data)";
        folderDesc = "unknown (encrypted data)";
        BASymmetricKey tempSymmetricKey = new BASymmetricKey(new BAAsyCipherBlock(encSymmetricKey.toByteArray()).decrypt(privateKey));
        symmetricKey = tempSymmetricKey;
        SymmetricBulkCipher symCipher = new SymmetricBulkCipher(tempSymmetricKey);
        if (trace != null) trace.data(20, "symCipher", symCipher);
        String tempFolderName = symCipher.bulkDecrypt(encFolderName).toByteStr();
        folderName = tempFolderName;
        if (trace != null) trace.data(30, "tempFolderName", tempFolderName);
        if (trace != null) trace.data(38, "encFolderDesc", encFolderDesc);
        String tempFolderDesc = (encFolderDesc != null) ? symCipher.bulkDecrypt(encFolderDesc).toByteStr() : null;
        folderDesc = tempFolderDesc;
        if (trace != null) trace.data(40, "tempFolderDesc", tempFolderDesc);

        //symmetricKey = tempSymmetricKey;
        //folderName = tempFolderName;
        //folderDesc = tempFolderDesc;
      } catch (Throwable t) {
        if (trace != null) trace.exception(FolderShareRecord.class, 100, t);
        throw new SecurityException(t.getMessage());
      }
    }

    if (trace != null) trace.exit(FolderShareRecord.class);
  }


  /**
   * Unseals the <code> encFolderName, encFolderDesc, encSymmetricKey </code>
   * into <code> folderName, folderDesc, symmetricKey </code>
   * using the unSealant object which is the user's symKeyFldShares.
   */
  public void unSeal(BASymmetricKey symKeyFldShares) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderShareRecord.class, "unSeal(BASymmetricKey symKeyFldShares)");
    if (trace != null) trace.args(symKeyFldShares);

    if (trace != null) trace.data(10, "this", this);
    if (encSymmetricKey != null && encFolderName != null) {
      try {
        SymmetricBulkCipher symCipher = new SymmetricBulkCipher(symKeyFldShares);
        byte[] encSymmetricKeyBytes = encSymmetricKey.toByteArray();
        BASymmetricKey tempSymmetricKey = new BASymmetricKey(symCipher.bulkDecrypt(encSymmetricKeyBytes, 0, encSymmetricKeyBytes.length));
        String tempFolderName = symCipher.bulkDecrypt(encFolderName).toByteStr();
        String tempFolderDesc = null;
        if (encFolderDesc != null) {
          tempFolderDesc = symCipher.bulkDecrypt(encFolderDesc).toByteStr();
        }

        symmetricKey = tempSymmetricKey;
        folderName = tempFolderName;
        folderDesc = tempFolderDesc;
      } catch (Throwable t) {
        if (trace != null) trace.exception(FolderShareRecord.class, 100, t);
        throw new SecurityException(t.getMessage());
      }
    }

    if (trace != null) trace.exit(FolderShareRecord.class);
  }


  public static FolderShareRecord[] filterDesiredFolderRecords(FolderShareRecord[] records, Long folderId) {
    FolderShareRecord[] recs = null;
    if (records != null && records.length > 0) {
      Vector recsV = new Vector();
      for (int i=0; i<records.length; i++) {
        if (records[i].folderId.equals(folderId))
          recsV.addElement(records[i]);
      }
      recs = (FolderShareRecord[]) ArrayUtils.toArray(recsV, FolderShareRecord.class);
    }
    return recs;
  }

  public static FolderShareRecord[] filterDesiredFolderRecords(FolderShareRecord[] records, Long[] desiredUserIDs) {
    FolderShareRecord[] recs = null;
    if (records != null && records.length > 0) {
      Vector recsV = new Vector();
      for (int i=0; i<records.length; i++) {
        FolderShareRecord share = records[i];
        if (share.isOwnedByUser() &&
            ArrayUtils.find(desiredUserIDs, share.ownerUserId) >= 0 &&
            !recsV.contains(share))
          recsV.addElement(share);
      }
      recs = (FolderShareRecord[]) ArrayUtils.toArray(recsV, FolderShareRecord.class);
    }
    return recs;
  }

  public static Long[] getFolderIDs(FolderShareRecord[] shareRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderShareRecord.class, "FolderShareRecord[] shareRecords");
    if (trace != null) trace.args(shareRecords);

    HashSet folderIDsHS = new HashSet();
    if (shareRecords != null) {
      for (int i=0; i<shareRecords.length; i++) {
        if (folderIDsHS.contains(shareRecords[i].folderId) == false)
          folderIDsHS.add(shareRecords[i].folderId);
      }
    }
    Long[] folderIDs = (Long[]) ArrayUtils.toArray(folderIDsHS, Long.class);

    if (trace != null) trace.exit(FolderShareRecord.class, folderIDs);
    return folderIDs;
  }

  public static Long[] getOwnerGroupIDs(FolderShareRecord[] shareRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderShareRecord.class, "getOwnerGroupIDs(FolderShareRecord[] shareRecords)");
    if (trace != null) trace.args(shareRecords);

    Vector ownerIDsV = new Vector();
    if (shareRecords != null) {
      for (int i=0; i<shareRecords.length; i++) {
        if (shareRecords[i].ownerType.shortValue() == Record.RECORD_TYPE_GROUP &&
            ownerIDsV.contains(shareRecords[i].ownerUserId) == false)
          ownerIDsV.addElement(shareRecords[i].ownerUserId);
      }
    }
    Long[] ownerIDs = (Long[]) ArrayUtils.toArray(ownerIDsV, Long.class);

    if (trace != null) trace.exit(FolderShareRecord.class, ownerIDs);
    return ownerIDs;
  }

  public static Long[] getOwnerUserIDs(FolderShareRecord[] shareRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderShareRecord.class, "getOwnerUserIDs(FolderShareRecord[] shareRecords)");
    if (trace != null) trace.args(shareRecords);

    Vector ownerIDsV = new Vector();
    if (shareRecords != null) {
      for (int i=0; i<shareRecords.length; i++) {
        if (shareRecords[i].ownerType.shortValue() == Record.RECORD_TYPE_USER &&
            ownerIDsV.contains(shareRecords[i].ownerUserId) == false)
          ownerIDsV.addElement(shareRecords[i].ownerUserId);
      }
    }
    Long[] ownerIDs = (Long[]) ArrayUtils.toArray(ownerIDsV, Long.class);

    if (trace != null) trace.exit(FolderShareRecord.class, ownerIDs);
    return ownerIDs;
  }

  public boolean isOwnedBy(Long userId, Long[] groupIDs) {
    boolean rc = false;
    if (ownerType.shortValue() == Record.RECORD_TYPE_USER) {
      if (userId != null)
        rc = ownerUserId.equals(userId);
    } else if (ownerType.shortValue() == Record.RECORD_TYPE_GROUP) {
      if (groupIDs != null)
        rc = ArrayUtils.find(groupIDs, ownerUserId) >= 0;
    }
    return rc;
  }
  public boolean isOwnedBy(Long userId, Set groupIDsSet) {
    boolean rc = false;
    if (ownerType.shortValue() == Record.RECORD_TYPE_USER) {
      if (userId != null)
        rc = ownerUserId.equals(userId);
    } else if (ownerType.shortValue() == Record.RECORD_TYPE_GROUP) {
      if (groupIDsSet != null)
        rc = groupIDsSet.contains(ownerUserId);
    }
    return rc;
  }
  public boolean isOwnedByUser() {
    return ownerType.shortValue() == Record.RECORD_TYPE_USER;
  }
  public boolean isOwnedByGroup() {
    return ownerType.shortValue() == Record.RECORD_TYPE_GROUP;
  }

  public void clearSensitiveData() {
    encFolderName  = null;
    encFolderDesc  = null;
    encSymmetricKey= null;
    pubKeyId       = null;
    folderName     = null;
    folderDesc     = null;
    symmetricKey   = null;
  }

  public static void clearSensitiveData(FolderShareRecord[] shares) {
    if (shares != null)
      for (int i=0; i<shares.length; i++)
        shares[i].clearSensitiveData();
  }

  public void merge(Record updated) {
    if (updated instanceof FolderShareRecord) {
      FolderShareRecord record = (FolderShareRecord) updated;
      if (record.shareId        != null) shareId        = record.shareId;
      if (record.folderId       != null) folderId       = record.folderId;
      if (record.ownerType      != null) ownerType      = record.ownerType;
      if (record.ownerUserId    != null) ownerUserId    = record.ownerUserId;
      if (record.viewParentId   != null) viewParentId   = record.viewParentId;

      if (record.canWrite       != null) canWrite       = record.canWrite;
      if (record.canDelete      != null) canDelete      = record.canDelete;
      if (record.dateCreated    != null) dateCreated    = record.dateCreated;
      if (record.dateUpdated    != null) dateUpdated    = record.dateUpdated;

      if (record.encFolderName != null) {
        encFolderName  = record.encFolderName;
        encFolderDesc  = record.encFolderDesc;
        encSymmetricKey= record.encSymmetricKey;
        pubKeyId       = record.pubKeyId;
        symmetricKey   = record.symmetricKey;
      }

      // don't want to NULLIFY the name as some threads may need it while the new one is being decrypted
      if (record.folderName     != null) folderName     = record.folderName;
      if (record.folderDesc     != null) folderDesc     = record.folderDesc;

      //if (record.encFolderName  != null) encFolderName  = record.encFolderName;
      //if (record.encFolderDesc  != null) encFolderDesc  = record.encFolderDesc;
      //if (record.encSymmetricKey!= null) encSymmetricKey= record.encSymmetricKey;
      //if (record.pubKeyId       != null) pubKeyId       = record.pubKeyId;

      // un-sealed data
      //if (record.folderName     != null) folderName     = record.folderName;
      //if (record.folderDesc     != null) folderDesc     = record.folderDesc;
      //if (record.symmetricKey   != null) symmetricKey   = record.symmetricKey;

    }
    else
      super.mergeError(updated);
  }

  public String toString() {
    return "[FolderShareRecord"
      + ": shareId="          + shareId
      + ", folderId="         + folderId
      + ", ownerType="        + ownerType
      + ", ownerUserId="      + ownerUserId
      + ", viewParentId="     + viewParentId
      + ", encFolderName="    + encFolderName
      + ", encFolderDesc="    + encFolderDesc
      + ", encSymmetricKey="  + encSymmetricKey
      + ", pubKeyId="         + pubKeyId
      + ", canWrite="         + canWrite
      + ", canDelete="        + canDelete
      + ", dateCreated="      + dateCreated
      + ", dateUpdated="      + dateUpdated
      + ", un-sealed data >> "
      + ", folderName="       + folderName
      + ", folderDesc="       + folderDesc
      + ", symmetricKey="     + symmetricKey
      + "]";
  }


  /**********************
   * LinkRecordI methods
   *********************/
  public Long getObjId() {
    return folderId;
  }

  public Long getOwnerObjId() {
    return ownerUserId;
  }

  public Short getOwnerObjType() {
    return ownerType;
  }

  public Long[] getOwnerObjIDs(LinkRecordI[] links, short ownerType) {
    return getOwnerUserIDs((FolderShareRecord[]) links);
  }

  public Long[] getObjIDs(LinkRecordI[] links) {
    return getFolderIDs((FolderShareRecord[]) links);
  }

  public void setId(Long id) {
    shareId = id;
  }

}
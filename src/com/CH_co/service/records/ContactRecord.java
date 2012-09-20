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

import com.CH_co.cryptx.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import java.sql.Timestamp;
import java.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>  
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class ContactRecord extends Record implements MemberContactRecordI {

  public static final short STATUS_INITIATED = 1;
  public static final short STATUS_ACCEPTED = 2;
  public static final short STATUS_DECLINED = 3;
  public static final short STATUS_ACCEPTED_ACKNOWLEDGED = 4;
  public static final short STATUS_DECLINED_ACKNOWLEDGED = 5;

  // Runtime status
  public static final short STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE = 6;
  public static final short STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_DND = 7;
  public static final short STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_NA = 8;
  public static final short STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AWAY = 9;
  public static final short STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_INACTIVE = 10;
  public static final short STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AVAILABLE = 11;

  public static final int MASK_PERMITS = 1 | 2 | 4 | 8;
  public static final int MASK_SETTINGS = 16;
  public static final int PERMIT_DISABLE_MESSAGING = 1;
  public static final int PERMIT_DISABLE_SHARE_FOLDERS = 2;
  public static final int PERMIT_DISABLE_SEE_ONLINE_STATUS = 4;
  public static final int PERMIT__THIS_CONTACT_IS_GIVEN = 8;
  public static final int SETTING_DISABLE_AUDIBLE_STATUS_NOTIFY = 16; // personal user setting is not protected by given-modify-permits

  public Long contactId;
  public Long folderId;
  public Long ownerUserId;               // owner User ID
  public Long contactWithId;
  public Long creatorId;                // User ID of person creating the contact, used for sub-accounts.
  public Short status;
  public Integer permits;

  // To communicate with listeners the previous status just before this update,
  // useful for knowing if a guy just went offline and there is a chat window open.
  public Short previousStatus;          
  private BASymCipherBulk encOwnerNote;
  private Long otherKeyId;
  private BAAsyCipherBlock encOtherSymKey;
  private BASymCipherBulk encOtherNote;
  public Timestamp dateCreated;
  public Timestamp dateUpdated;


  /** unwrapped data */
  private String ownerNote = null;
  private BASymmetricKey otherSymKey = null;
  private String otherNote = null;

  /** Creates new ContactRecord */
  public ContactRecord() {
  }

  /**
   * Default comparison is made by object ID but this is rather user unfriendly comparison.
   * We overwrite with name comparison.
   */
  public int compareTo(Object record) {
    if (record instanceof ContactRecord) {
      ContactRecord rec = (ContactRecord) record;
      if (ownerNote != null && rec.ownerNote != null)
        return ownerNote.compareToIgnoreCase(rec.ownerNote);
      else if (otherNote != null && rec.otherNote != null)
        return otherNote.compareToIgnoreCase(rec.otherNote);
      else if (ownerNote != null || otherNote != null)
        return 1;
      else if (rec.ownerNote != null || rec.otherNote != null)
        return -1;
      else 
        return 0;
    }
    else
      return super.compareTo(record);
  }

  public Long getId() {
    return contactId;
  }

  public int getIcon() {
    return ImageNums.CONTACT16;
  }

  public boolean isOfActiveType() {
    boolean rc = false;
    if (status != null) {
      short s = status.shortValue();
      rc = (s == STATUS_ACCEPTED_ACKNOWLEDGED || isOnlineStatus(s));
    }
    return rc;
  }

  public boolean isOfActiveTypeAnyState() {
    boolean rc = false;
    if (status != null) {
      short s = status.shortValue();
      rc = (s == STATUS_ACCEPTED || s == STATUS_ACCEPTED_ACKNOWLEDGED || isOnlineStatus(s));
    }
    return rc;
  }

  public boolean isOfDeclinedTypeAnyState() {
    boolean rc = false;
    if (status != null) {
      short s = status.shortValue();
      rc = (s == STATUS_DECLINED || s == STATUS_DECLINED_ACKNOWLEDGED);
    }
    return rc;
  }

  public boolean isOfInitiatedType() {
    boolean rc = false;
    if (status != null) {
      short s = status.shortValue();
      rc = s == STATUS_INITIATED;
    }
    return rc;
  }

  public void setEncOwnerNote   (BASymCipherBulk encOwnerNote   ) { this.encOwnerNote = encOwnerNote;     }
  public void setOtherKeyId     (Long otherKeyId                ) { this.otherKeyId = otherKeyId;         }
  public void setEncOtherSymKey (BAAsyCipherBlock encOtherSymKey) { this.encOtherSymKey = encOtherSymKey; }
  public void setEncOtherNote   (BASymCipherBulk encOtherNote   ) { this.encOtherNote = encOtherNote;     }
  public void setOwnerNote      (String ownerNote               ) { this.ownerNote = ownerNote;           }
  public void setOtherSymKey    (BASymmetricKey otherSymKey     ) { this.otherSymKey = otherSymKey;       }
  public void setOtherNote      (String otherNote               ) { this.otherNote = otherNote;           }

  public BASymCipherBulk getEncOwnerNote() { 
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactRecord.class, "getEncOwnerNote()");
    if (trace != null) trace.exit(ContactRecord.class, encOwnerNote);
    return encOwnerNote;    
  }

  public Long             getOtherKeyId()     { return otherKeyId;      }
  public BAAsyCipherBlock getEncOtherSymKey() { return encOtherSymKey;  }
  public BASymCipherBulk  getEncOtherNote()   { return encOtherNote;    }
  public String           getOwnerNote()      { return ownerNote;       }
  public BASymmetricKey   getOtherSymKey()    { return otherSymKey;     }
  public String           getOtherNote()      { return otherNote;       }

  public String getNote(Long forUserId) {
    return forUserId.equals(ownerUserId) ? getOwnerNote() : getOtherNote();
  }

  /**
   * Seals the <code> ownerNote and otherSymKey </code> into 
   * <code> encOwnerNote and encOtherSymKey </code> 
   * using the sealant object which is the symmetric key of the folder and 
   * symmetric key of the contact for the other party.
   * Also sets otherKeyId when sealing.
   * @param folderSymKey symmetric key for the owner's folder where this contact is to be located
   * @param otherKey private key of the other party to seal reason for contact for that person's eyes
   */
  public void seal(BASymmetricKey folderSymKey, KeyRecord otherKeyRec) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactRecord.class, "seal(BASymmetricKey folderSymKey, KeyRecord otherKeyRec)");

    if (trace != null) trace.data(10, "before", this);
    seal(folderSymKey);
    seal(otherKeyRec);
    if (trace != null) trace.data(11, "after", this);

    if (trace != null) trace.exit(ContactRecord.class);
  }

  /**
   * Owner part of the seal.
   */
  public void seal(BASymmetricKey folderSymKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactRecord.class, "seal(BASymmetricKey folderSymKey)");

    try {
      SymmetricBulkCipher symOwnerCipher = new SymmetricBulkCipher(folderSymKey);
      BASymCipherBulk tempEncOwnerNote = symOwnerCipher.bulkEncrypt(ownerNote);
      super.seal();
      encOwnerNote = tempEncOwnerNote;
    } catch (Throwable t) {
      if (trace != null) trace.exception(ContactRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(ContactRecord.class);
  }

  /**
   * Other part of the seal.
   */
  public void seal(KeyRecord otherKeyRec) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactRecord.class, "seal(KeyRecord otherKeyRec)");

    try {
      SymmetricBulkCipher symOtherCipher = new SymmetricBulkCipher(otherSymKey);
      AsymmetricBlockCipher asyOtherCipher = new AsymmetricBlockCipher();
      BAAsyCipherBlock tempEncOtherSymKey = asyOtherCipher.blockEncrypt(otherKeyRec.plainPublicKey, otherSymKey.toByteArray());
      BASymCipherBulk tempEncOtherNote = symOtherCipher.bulkEncrypt(otherNote);
      super.seal();
      otherKeyId = otherKeyRec.keyId;
      encOtherSymKey = tempEncOtherSymKey;
      encOtherNote = tempEncOtherNote;
    } catch (Throwable t) {
      if (trace != null) trace.exception(ContactRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(ContactRecord.class);
  }

  /**
   * Other part of the seal used when recrypting.  Junk the symmetric key received from our contact
   * and instead recrypt everything with our own symKeyCntNotes symmetric key.
   */
  public void sealRecrypt(BASymmetricKey symKeyCntNotes) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactRecord.class, "sealRecrypt(BASymmetricKey symKeyCntNotes)");

    try {
      SymmetricBulkCipher symOtherCipher = new SymmetricBulkCipher(symKeyCntNotes);
      BASymCipherBulk tempEncOtherNote = symOtherCipher.bulkEncrypt(otherNote);
      super.seal();
      otherKeyId = null;
      otherSymKey = null;
      encOtherSymKey = null;
      encOtherNote = tempEncOtherNote;
    } catch (Throwable t) {
      if (trace != null) trace.exception(ContactRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(ContactRecord.class);
  }

  public void sealGivenContact(BASymmetricKey owner_folderSymKey, BASymmetricKey contactWith_cntSymKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactRecord.class, "sealGivenContact(BASymmetricKey ownerSymKey, BASymmetricKey contactWithSymKey)");

    seal(owner_folderSymKey);
    sealRecrypt(contactWith_cntSymKey);

    if (trace != null) trace.exit(ContactRecord.class);
  }

  /**
   * UnSeals the <code> encContactName and encContactDesc and encOtherSymKey </code> into 
   * <code> contactName and contactDesc and otherSymKey </code> 
   * using the sealant object which is the symmetric key of the folder and
   * the private key of the receipient.
   * @param folderSymKey symmetric key for the owner's folder where this contact is located
   * @param otherKey private key of the other party to unSeal reason for contact 
   */
  public void unSeal(BASymmetricKey folderSymKey, KeyRecord otherKeyRec) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactRecord.class, "unSeal(BASymmetricKey folderSymKey, KeyRecord otherKeyRec)");

    if (trace != null) trace.data(10, this);
    unSeal(folderSymKey);
    unSeal(otherKeyRec);

    if (trace != null) trace.exit(ContactRecord.class);
  }


  /**
   * Owner part of the unSeal.
   */
  public void unSeal(BASymmetricKey folderSymKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactRecord.class, "unSeal(BASymmetricKey folderSymKey)");

    if (trace != null) trace.data(10, this);
    if (encOwnerNote != null) {
      try {
        SymmetricBulkCipher symOwnerCipher = new SymmetricBulkCipher(folderSymKey);
        BASymPlainBulk tempOwnerNote = symOwnerCipher.bulkDecrypt(encOwnerNote);
        super.unSeal();
        ownerNote = tempOwnerNote.toByteStr();
      } catch (Throwable t) {
        if (trace != null) trace.exception(ContactRecord.class, 100, t);
        throw new SecurityException(t.getMessage());
      }
    }

    if (trace != null) trace.exit(ContactRecord.class);
  }

  /**
   * Other part of the unSeal.
   */
  public void unSeal(KeyRecord otherKeyRec) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactRecord.class, "unSeal(KeyRecord otherKeyRec)");
    
    if (trace != null) trace.data(10, this);
    if (!otherKeyId.equals(otherKeyRec.keyId)) {
      throw new IllegalArgumentException("Wrong key record specified!");
    }

    try {
      AsymmetricBlockCipher asyOtherCipher = new AsymmetricBlockCipher();
      BAAsyPlainBlock tempOtherSymKey = asyOtherCipher.blockDecrypt(otherKeyRec.getPrivateKey(), encOtherSymKey.toByteArray());
      BASymmetricKey tempOtherSymKey2 = new BASymmetricKey(tempOtherSymKey);
      SymmetricBulkCipher symOtherCipher = new SymmetricBulkCipher(tempOtherSymKey2);
      BASymPlainBulk tempOtherNote = symOtherCipher.bulkDecrypt(encOtherNote);
      super.unSeal();
      otherSymKey = tempOtherSymKey2;
      otherNote = tempOtherNote.toByteStr();
    } catch (Throwable t) {
      if (trace != null) trace.exception(ContactRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(ContactRecord.class);
  }


  /**
   * Other part of the unSeal.  UnSeal the recrypted contact record with symmetric key from UserRecord (symKeyCntNotes)
   */
  public void unSealRecrypted(BASymmetricKey symKeyCntNotes) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactRecord.class, "unSealRecrypted(BASymmetricKey symKeyCntNotes)");

    if (trace != null) trace.data(10, this);
    if (encOtherNote != null) {
      try {
        SymmetricBulkCipher symOtherCipher = new SymmetricBulkCipher(symKeyCntNotes);
        BASymPlainBulk tempOtherNote = symOtherCipher.bulkDecrypt(encOtherNote);
        super.seal();
        otherNote = tempOtherNote.toByteStr();
      } catch (Throwable t) {
        if (trace != null) trace.exception(ContactRecord.class, 100, t);
        throw new SecurityException(t.getMessage());
      }
    }

    if (trace != null) trace.exit(ContactRecord.class);
  }


  public static Long[] getOwnerUserIDs(ContactRecord[] cRecs, boolean uniqueOnly) {
    Long[] ids = null;
    if (cRecs != null && cRecs.length > 0) {
      ArrayList idsL = new ArrayList();
      for (int i=0; i<cRecs.length; i++) {
        Long id = cRecs[i].ownerUserId;
        if (uniqueOnly) {
          if (!idsL.contains(id)) {
            idsL.add(id);
          }
        } else {
          idsL.add(id);
        }
      }
      ids = (Long[]) ArrayUtils.toArray(idsL, Long.class);
    }
    return ids;
  }

  public static Long[] getContactWithUserIDs(ContactRecord[] cRecs, boolean uniqueOnly) {
    Long[] ids = null;
    if (cRecs != null && cRecs.length > 0) {
      ArrayList idsL = new ArrayList();
      for (int i=0; i<cRecs.length; i++) {
        Long id = cRecs[i].contactWithId;
        if (uniqueOnly) {
          if (!idsL.contains(id)) {
            idsL.add(id);
          }
        } else {
          idsL.add(id);
        }
      }
      ids = (Long[]) ArrayUtils.toArray(idsL, Long.class);
    }
    return ids;
  }

  public static Long[] getCreatorIDs(ContactRecord[] cRecs, boolean uniqueOnly) {
    Long[] ids = null;
    if (cRecs != null && cRecs.length > 0) {
      ArrayList idsL = new ArrayList();
      for (int i=0; i<cRecs.length; i++) {
        Long id = cRecs[i].creatorId;
        if (uniqueOnly) {
          if (!idsL.contains(id)) {
            idsL.add(id);
          }
        } else {
          idsL.add(id);
        }
      }
      ids = (Long[]) ArrayUtils.toArray(idsL, Long.class);
    }
    return ids;
  }

  public static Long[] getInvolvedUserIDs(ContactRecord[] cRecs) {
    Long[] ids1 = getOwnerUserIDs(cRecs, true);
    Long[] ids2 = getContactWithUserIDs(cRecs, true);
    Long[] ids3 = getCreatorIDs(cRecs, true);
    Long[] ids = (Long[]) ArrayUtils.concatinate(ids1, ids2);
    ids = (Long[]) ArrayUtils.concatinate(ids, ids3);
    ids = (Long[]) ArrayUtils.removeDuplicates(ids);
    return ids;
  }

  public static ContactRecord[] filterDesiredStatusRecords(ContactRecord[] cRecs, Short status) {
    ContactRecord[] recs = null;
    if (cRecs != null && cRecs.length > 0) {
      ArrayList recsL = new ArrayList();
      for (int i=0; i<cRecs.length; i++) {
        if (status.equals(cRecs[i].status))
          recsL.add(cRecs[i]);
      }
      recs = (ContactRecord[]) ArrayUtils.toArray(recsL, ContactRecord.class);
    }
    return recs;
  }

  public static ContactRecord[] filterDesiredStatusRecords(ContactRecord[] cRecs, Short[] status) {
    ContactRecord[] recs = null;
    if (cRecs != null && cRecs.length > 0) {
      HashSet statusHS = new HashSet();
      for (int i=0; i<status.length; i++) {
        statusHS.add(status[i]);
      }
      ArrayList recsL = new ArrayList();
      for (int i=0; i<cRecs.length; i++) {
        if (statusHS.contains(cRecs[i].status))
          recsL.add(cRecs[i]);
      }
      recs = (ContactRecord[]) ArrayUtils.toArray(recsL, ContactRecord.class);
    }
    return recs;
  }

  public static ContactRecord[] filterDesiredOwnerRecords(ContactRecord[] cRecs, Long owner) {
    ContactRecord[] recs = null;
    if (cRecs != null && cRecs.length > 0) {
      ArrayList recsL = new ArrayList();
      for (int i=0; i<cRecs.length; i++) {
        if (cRecs[i].ownerUserId.equals(owner))
          recsL.add(cRecs[i]);
      }
      recs = (ContactRecord[]) ArrayUtils.toArray(recsL, ContactRecord.class);
    }
    return recs;
  }

  public static ContactRecord[] filterStatusOnlineRecords(ContactRecord[] cRecs) {
    ContactRecord[] recs = null;
    if (cRecs != null && cRecs.length > 0) {
      ArrayList recsL = new ArrayList();
      for (int i=0; i<cRecs.length; i++) {
        if (isOnlineStatus(cRecs[i].status))
          recsL.add(cRecs[i]);
      }
      recs = (ContactRecord[]) ArrayUtils.toArray(recsL, ContactRecord.class);
    }
    return recs;
  }

  public static ContactRecord[] filterToAcceptOrDecline(ContactRecord[] contactRecords, Long myUserId) {
    ArrayList toAcceptDeclineL = null;
    for (int i=0; i<contactRecords.length; i++) {
      ContactRecord cRec = contactRecords[i];
      if (cRec.status != null) {
        short status = cRec.status.shortValue();
        if (cRec.ownerUserId != null && 
            !cRec.ownerUserId.equals(myUserId) && 
            status == ContactRecord.STATUS_INITIATED
           )
        {
          if (toAcceptDeclineL == null) toAcceptDeclineL = new ArrayList();
          toAcceptDeclineL.add(cRec);
        }
      }
    }
    ContactRecord[] cRecs = (ContactRecord[]) ArrayUtils.toArray(toAcceptDeclineL, ContactRecord.class);
    return cRecs;
  }


  public static ContactRecord[] filterDesiredPermitFlags(ContactRecord[] cRecs, int flagsSet, int mask) {
    ContactRecord[] recs = null;
    if (cRecs != null && cRecs.length > 0) {
      ArrayList recsL = new ArrayList();
      for (int i=0; i<cRecs.length; i++) {
        int perms = cRecs[i].permits.intValue();
        if ((perms & mask) == flagsSet) {
          recsL.add(cRecs[i]);
        }
      }
      recs = (ContactRecord[]) ArrayUtils.toArray(recsL, ContactRecord.class);
    }
    return recs;
  }

  public static MemberContactRecordI[] filterDesiredPermitFlags(MemberContactRecordI[] cRecs, int flagsSet, int mask, Boolean keepGroups) {
    MemberContactRecordI[] recs = null;
    if (cRecs != null && cRecs.length > 0) {
      ArrayList recsL = new ArrayList();
      for (int i=0; i<cRecs.length; i++) {
        if (cRecs[i].getMemberType() == Record.RECORD_TYPE_GROUP && keepGroups != null) {
          if (keepGroups.booleanValue())
            recsL.add(cRecs[i]);
        } else if (cRecs[i] instanceof ContactRecord) {
          int perms = ((ContactRecord) cRecs[i]).permits.intValue();
          if ((perms & mask) == flagsSet) {
            recsL.add(cRecs[i]);
          }
        }
      }
      recs = (ContactRecord[]) ArrayUtils.toArray(recsL, ContactRecord.class);
    }
    return recs;
  }

  public boolean isGiven() {
    return (permits.intValue() & PERMIT__THIS_CONTACT_IS_GIVEN) != 0;
  }

  public boolean isOnlineStatus() {
    return isOnlineStatus(status);
  }
  public static boolean isOnlineStatus(Short status) {
    // status maybe nullified when for example server doesn't want to report status to the client
    return status != null ? isOnlineStatus(status.shortValue()) : false;
  }
  public static boolean isOnlineStatus(short status) {
    return    status == ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE ||
              status == ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_DND ||
              status == ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_NA ||
              status == ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AWAY ||
              status == ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_INACTIVE ||
              status == ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AVAILABLE;
  }

  public static Short onlineCharToFlag(Character onlineStatus) {
    Short status = null;
    if (onlineStatus != null) {
      switch (onlineStatus.charValue()) {
        case UserRecord.ONLINE_INVISIBLE :
          status = new Short(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED);
          break;
        case UserRecord.ONLINE_DND :
          status = new Short(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_DND);
          break;
        case UserRecord.ONLINE_NA :
          status = new Short(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_NA);
          break;
        case UserRecord.ONLINE_AWAY :
          status = new Short(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AWAY);
          break;
        case UserRecord.ONLINE_INACTIVE :
          status = new Short(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_INACTIVE);
          break;
        case UserRecord.ONLINE_AVAILABLE :
          status = new Short(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AVAILABLE);
          break;
        default :
          status = new Short(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE);
          break;
      }
    } else {
      status = new Short(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE);
    }
    return status;
  }

  public static Character onlineFlagToChar(short flag) {
    Character online = null;
    switch (flag) {
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED :
        online = new Character(UserRecord.ONLINE_INVISIBLE);
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_DND :
        online = new Character(UserRecord.ONLINE_DND);
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_NA :
        online = new Character(UserRecord.ONLINE_NA);
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AWAY :
        online = new Character(UserRecord.ONLINE_AWAY);
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_INACTIVE :
        online = new Character(UserRecord.ONLINE_INACTIVE);
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AVAILABLE :
        online = new Character(UserRecord.ONLINE_AVAILABLE);
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE :
      default :
        online = new Character(UserRecord.ONLINE_AVAILABLE);
        break;
    }
    return online;
  }

  public void clearOtherNote() {
    otherKeyId = null;
    encOtherSymKey = null;
    encOtherNote = null;
    otherSymKey = null;
    otherNote = null;
  }

  public void clearOwnerNote() {
    encOwnerNote = null;
    ownerNote = null;
  }

  public void merge(Record updated) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactRecord.class, "merge(Record updated)");
    if (trace != null) trace.args(updated);
    if (trace != null) trace.data(100, "this", this);

    if (updated instanceof ContactRecord) {
      ContactRecord record = (ContactRecord) updated;
      if (record.contactId       != null) contactId      = record.contactId;
      if (record.folderId        != null) folderId       = record.folderId;
      if (record.ownerUserId     != null) ownerUserId    = record.ownerUserId;
      if (record.contactWithId   != null) contactWithId  = record.contactWithId;
      if (record.creatorId       != null) creatorId      = record.creatorId;
      // Remember the last status, don't merge if from the new record.
      // Only do it if the new status is not null, which is possible in other updates...
      if (record.status != null) {
        if (!record.status.equals(status)) {
          if (status != null)
            previousStatus = status;
          else
            previousStatus = record.status;
          status = record.status;
        }
      }

      if (record.permits         != null) permits        = record.permits;
      if (record.dateCreated     != null) dateCreated    = record.dateCreated;
      if (record.dateUpdated     != null) dateUpdated    = record.dateUpdated;

      if (record.encOtherNote != null) {
        otherKeyId      = record.otherKeyId;
        encOtherSymKey  = record.encOtherSymKey;
        encOtherNote    = record.encOtherNote;
        otherSymKey     = record.otherSymKey;
        otherNote       = record.otherNote;
      }

      if (record.encOwnerNote != null) {
        encOwnerNote = record.encOwnerNote;
        ownerNote = record.ownerNote;
      }
    }
    else
      super.mergeError(updated);

    if (trace != null) trace.data(200, "this", this);
    if (trace != null) trace.exit(ContactRecord.class);
  }

  public String toString() {
    return "[ContactRecord"
      + ": contactId="      + contactId
      + ", folderId="       + folderId
      + ", ownerUserId="    + ownerUserId
      + ", contactWithId="  + contactWithId
      + ", creatorId="      + creatorId
      + ", status="         + status
      + ", permits="        + permits
      + ", previousStatus=" + previousStatus
      + ", encOwnerNote="   + encOwnerNote
      + ", otherKeyId="     + otherKeyId
      + ", encOtherSymKey=" + encOtherSymKey
      + ", encOtherNote="   + encOtherNote
      + ", dateCreated="    + dateCreated
      + ", dateUpdated="    + dateUpdated
      + ", un-sealed data >> "
      + ", ownerNote="      + ownerNote
      + ", otherSymKey="    + otherSymKey
      + ", otherNote="      + otherNote
      + "]";
  }

  public void setId(Long id) {
    contactId = id;
  }

  public short getMemberType() {
    return Record.RECORD_TYPE_USER;
  }

  public Long getMemberId() {
    return contactWithId;
  }

}
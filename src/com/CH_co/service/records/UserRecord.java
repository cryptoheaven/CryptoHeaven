/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
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

import javax.swing.Icon;
import java.sql.Timestamp;

import com.CH_co.cryptx.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>  
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class UserRecord extends Record implements MemberRecordI { // implicit no-argument constructor

  public static final short STATUS_WEB = 0;
  public static final short STATUS_PROMO = 1 << 0;
  public static final short STATUS_PROMO_HELD = 1 << 1;
  public static final short STATUS_GUEST = 1 << 2;
  public static final short STATUS_GUEST_HELD = 1 << 3;
  public static final short STATUS_PAID = 1 << 4;
  public static final short STATUS_PAID_HELD = 1 << 5;
  public static final short STATUS_BUSINESS = 1 << 6;
  public static final short STATUS_BUSINESS_HELD = 1 << 7;
  public static final short STATUS_BUSINESS_SUB = 1 << 8;
  public static final short STATUS_BUSINESS_SUB_HELD = 1 << 9;
  public static final short STATUS_MASK_HELD = 1 << 1 | 1 << 3 | 1 << 5 | 1 << 7 | 1 << 9;

  public static final short ACC_SPAM_YES_INTER = 1 << 1;
  public static final short ACC_SPAM_YES_INTER__NO_UPDATE = 1 << 2;
  public static final short ACC_SPAM_YES_INTER__NO_GRANT = 1 << 3;
  public static final short ACC_SPAM_YES_REG_EMAIL = 1 << 4;
  public static final short ACC_SPAM_YES_REG_EMAIL__NO_UPDATE = 1 << 5;
  public static final short ACC_SPAM_YES_REG_EMAIL__NO_GRANT = 1 << 6;
  public static final short ACC_SPAM_YES_SSL_EMAIL = 1 << 7;
  public static final short ACC_SPAM_YES_SSL_EMAIL__NO_UPDATE = 1 << 8;
  public static final short ACC_SPAM_YES_SSL_EMAIL__NO_GRANT = 1 << 9;
//  public static final short ACC_SPAM_BLOCK_REG_NUMERIC_ADDRESS = 1 << 10;
//  public static final short ACC_SPAM_BLOCK_REG_NUMERIC_ADDRESS__NO_UPDATE = 1 << 11;
//  public static final short ACC_SPAM_BLOCK_REG_NUMERIC_ADDRESS__NO_GRANT = 1 << 12;
  public static final short ACC_SPAM_MASK__NO_UPDATE = 1 << 2 | 1 << 5 | 1 << 8 | 1 << 11;
  public static final short ACC_SPAM_MASK__NO_GRANT = 1 << 3 | 1 << 6 | 1 << 9 | 1 << 12;
  public static final short ACC_SPAM_MASK__NO_UPDATE_AND_GRANT = ACC_SPAM_MASK__NO_UPDATE | ACC_SPAM_MASK__NO_GRANT;

  public static final short EMAIL_NOTIFY_YES = 1 << 1;
  public static final short EMAIL_NOTIFY_YES__NO_UPDATE = 1 << 2;
  public static final short EMAIL_NOTIFY_YES__NO_GRANT = 1 << 3;
  public static final short EMAIL_WARN_EXTERNAL = 1 << 4;
  public static final short EMAIL_WARN_EXTERNAL__NO_UPDATE = 1 << 5;
  public static final short EMAIL_WARN_EXTERNAL__NO_GRANT = 1 << 6;
  public static final short EMAIL_MANUAL_SELECT_PREVIEW_MODE = 1 << 7;
  public static final short EMAIL_MANUAL_SELECT_PREVIEW_MODE__NO_UPDATE = 1 << 8;
  public static final short EMAIL_MANUAL_SELECT_PREVIEW_MODE__NO_GRANT = 1 << 9;
  public static final short EMAIL_NOTIFY_INCLUDE_SUBJECT_AND_FROM_ADDRESS = 1 << 10;
  public static final short EMAIL_NOTIFY_INCLUDE_SUBJECT_AND_FROM_ADDRESS__NO_UPDATE = 1 << 11;
  public static final short EMAIL_NOTIFY_INCLUDE_SUBJECT_AND_FROM_ADDRESS__NO_GRANT = 1 << 12;
  public static final short EMAIL_MASK__NO_UPDATE = 1 << 2 | 1 << 5 | 1 << 8 | 1 << 11;
  public static final short EMAIL_MASK__NO_GRANT = 1 << 3 | 1 << 6 | 1 << 9 | 1 << 12;
  public static final short EMAIL_MASK__NO_UPDATE_AND_GRANT = EMAIL_MASK__NO_UPDATE | EMAIL_MASK__NO_GRANT;

  public static final long FLAG_ENABLE_CREATING_SUBACCOUNTS__MASTER_CAPABLE = 1L << 0;
  public static final long FLAG_ENABLE_CREATING_SUBACCOUNTS__MASTER_CAPABLE__NO_UPDATE = 1L << 1;
  public static final long FLAG_ENABLE_CREATING_SUBACCOUNTS__MASTER_CAPABLE__NO_GRANT = 1L << 2;
  public static final long FLAG_ENABLE_NICKNAME_CHANGE = 1L << 3;
  public static final long FLAG_ENABLE_NICKNAME_CHANGE__NO_UPDATE = 1L << 4;
  public static final long FLAG_ENABLE_NICKNAME_CHANGE__NO_GRANT = 1L << 5;
  public static final long FLAG_ENABLE_PASSWORD_CHANGE = 1L << 6;
  public static final long FLAG_ENABLE_PASSWORD_CHANGE__NO_UPDATE = 1L << 7;
  public static final long FLAG_ENABLE_PASSWORD_CHANGE__NO_GRANT = 1L << 8;
  public static final long FLAG_ENABLE_ACCOUNT_DELETE = 1L << 9;
  public static final long FLAG_ENABLE_ACCOUNT_DELETE__NO_UPDATE = 1L << 10;
  public static final long FLAG_ENABLE_ACCOUNT_DELETE__NO_GRANT = 1L << 11;
  public static final long FLAG_ENABLE_GIVEN_CONTACTS_ALTER = 1L << 12;
  public static final long FLAG_ENABLE_GIVEN_CONTACTS_ALTER__NO_UPDATE = 1L << 13;
  public static final long FLAG_ENABLE_GIVEN_CONTACTS_ALTER__NO_GRANT = 1L << 14;
  public static final long FLAG_ENABLE_GIVEN_CONTACTS_DELETE = 1L << 15;
  public static final long FLAG_ENABLE_GIVEN_CONTACTS_DELETE__NO_UPDATE = 1L << 16;
  public static final long FLAG_ENABLE_GIVEN_CONTACTS_DELETE__NO_GRANT = 1L << 17;
  public static final long FLAG_ENABLE_GIVEN_EMAILS_ALTER = 1L << 18;
  public static final long FLAG_ENABLE_GIVEN_EMAILS_ALTER__NO_UPDATE = 1L << 19;
  public static final long FLAG_ENABLE_GIVEN_EMAILS_ALTER__NO_GRANT = 1L << 20;
  public static final long FLAG_ENABLE_GIVEN_EMAILS_DELETE = 1L << 21;
  public static final long FLAG_ENABLE_GIVEN_EMAILS_DELETE__NO_UPDATE = 1L << 22;
  public static final long FLAG_ENABLE_GIVEN_EMAILS_DELETE__NO_GRANT = 1L << 23;
  public static final long FLAG_ENABLE_MAKING_CONTACTS_OUTSIDE_ORGANIZATION = 1L << 24;
  public static final long FLAG_ENABLE_MAKING_CONTACTS_OUTSIDE_ORGANIZATION__NO_UPDATE = 1L << 25;
  public static final long FLAG_ENABLE_MAKING_CONTACTS_OUTSIDE_ORGANIZATION__NO_GRANT = 1L << 26;
  public static final long FLAG_USER_OFFLINE_POPUP = 1L << 27;
  public static final long FLAG_USER_OFFLINE_POPUP__NO_UPDATE = 1L << 28;
  public static final long FLAG_USER_OFFLINE_POPUP__NO_GRANT = 1L << 29;
  public static final long FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER = 1L << 30; // not used as a permission
  public static final long FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER__NO_UPDATE = 1L << 31; 
  public static final long FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER__NO_GRANT = 1L << 32;
  public static final long FLAG_SKIP_SECURE_REPLY_LINK_IN_EXTERNAL_EMAILS = 1L << 33;
  public static final long FLAG_SKIP_SECURE_REPLY_LINK_IN_EXTERNAL_EMAILS__NO_UPDATE = 1L << 34;
  public static final long FLAG_SKIP_SECURE_REPLY_LINK_IN_EXTERNAL_EMAILS__NO_GRANT = 1L << 35;
  public static final long FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES = 1L << 36;
  public static final long FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES__NO_UPDATE = 1L << 37;
  public static final long FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES__NO_GRANT = 1L << 38;
  public static final long FLAG_DISABLE_AUTO_UPDATES = 1L << 39;
  public static final long FLAG_DISABLE_AUTO_UPDATES__NO_UPDATE = 1L << 40;
  public static final long FLAG_DISABLE_AUTO_UPDATES__NO_GRANT = 1L << 41;
  public static final long FLAG_ENABLE_GIVEN_MASTER_FOLDERS_DELETE = 1L << 42;
  public static final long FLAG_ENABLE_GIVEN_MASTER_FOLDERS_DELETE__NO_UPDATE = 1L << 43;
  public static final long FLAG_ENABLE_GIVEN_MASTER_FOLDERS_DELETE__NO_GRANT = 1L << 44;
  public static final long FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY = 1L << 45;
  public static final long FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY__NO_UPDATE = 1L << 46;
  public static final long FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY__NO_GRANT = 1L << 47;
  public static final long FLAG_MASK__NO_UPDATE = 1L << 1 | 1L << 4 | 1L << 7 | 1L << 10 | 1L << 13 | 1L << 16 | 1L << 19 | 1L << 22 | 1L << 25 | 1L << 28 | 1L << 31 | 1L << 34 | 1L << 37 | 1L << 40 | 1L << 43 | 1L << 46;
  public static final long FLAG_MASK__NO_GRANT  = 1L << 2 | 1L << 5 | 1L << 8 | 1L << 11 | 1L << 14 | 1L << 17 | 1L << 20 | 1L << 23 | 1L << 26 | 1L << 29 | 1L << 32 | 1L << 35 | 1L << 38 | 1L << 41 | 1L << 44 | 1L << 47;
  public static final long FLAG_MASK__NO_UPDATE_AND_GRANT = FLAG_MASK__NO_UPDATE | FLAG_MASK__NO_GRANT;

  public static final char ONLINE_INVISIBLE = 'I';
  public static final char ONLINE_DND = 'D';
  public static final char ONLINE_NA = 'N';
  public static final char ONLINE_AWAY = 'A';
  public static final char ONLINE_INACTIVE = 'V';
  public static final char ONLINE_AVAILABLE = 'O';

  public static final short UNLIMITED_AMOUNT = -1;
  public static final short GENERIC_EMAIL_ID = -1;

  public Long userId;
  public String handle;
  public Timestamp dateCreated;
  public Timestamp dateExpired;
  public Timestamp dateLastLogin;
  public Timestamp dateLastLogout;
  public Short status;
  public String statusInfo;
  public Short notifyByEmail;
  public Timestamp dateNotified;
  public Short acceptingSpam;
  public String emailAddress;
  public Long passwordHash;
  private BAAsyCipherBlock encSymKeys;  // asymmetrically encrypted symmetric keys
  public Long pubKeyId;                 // key for encryption of encSymKeys
  public Long currentKeyId;
  public Long fileFolderId;
  public Long addrFolderId;
  public Long whiteFolderId;
  public Long draftFolderId;
  public Long msgFolderId;
  public Long junkFolderId;
  public Long sentFolderId;
  public Long contactFolderId;
  public Long keyFolderId;
  public Long recycleFolderId;

  public Long storageLimit;             // UNLIMITED_AMOUNT (-1) for no limit
  public Long storageUsed;
  public Timestamp checkStorageDate;
  public Long transferLimit;            // UNLIMITED_AMOUNT (-1) for no limit
  public Long transferUsed;

  public Short maxSubAccounts;
  public Long parentId;
  public Long masterId;
  public Long defaultEmlId;
  public Long flags;
  public Character autoResp;            // Y/N to mark enablement of auto-responder
  public Character online;              // O_nline/Inacti_V_e/A_way/N_A/D_nd/I_nisible

  // cached 'shortInfo' name data, unique for this instance only...
  private transient String shortInfoHandle;
  private transient Long shortInfoId;
  private transient String shortInfoCached;

  // unwrapped data
  private BASymmetricKey symKeyFldShares;
  private BASymmetricKey symKeyCntNotes;


  public String getAccountType() {
    return getAccountType(status);
  }

  public static String getAccountType(Short status) {
    String rc = "unknown";
    switch (status.shortValue()) {
      case STATUS_WEB:
        rc = "Web Account";
        break;
      case STATUS_PROMO:
        rc = "Promotional Demo";
        break;
      case STATUS_PROMO_HELD:
        rc = "Suspended Promotional Demo";
        break;
      case STATUS_GUEST:
        rc = "Guest Account";
        break;
      case STATUS_GUEST_HELD:
        rc = "Suspended Guest Account";
        break;
      case STATUS_PAID:
        rc = "Regular Account";
        break;
      case STATUS_PAID_HELD:
        rc = "Suspended Regular Account";
        break;
      case STATUS_BUSINESS:
        rc = "Business Account";
        break;
      case STATUS_BUSINESS_HELD:
        rc = "Suspended Business Account";
        break;
      case STATUS_BUSINESS_SUB:
        rc = "User Account";
        break;
      case STATUS_BUSINESS_SUB_HELD:
        rc = "Suspended User Account";
        break;
    }
    return rc;
  }

  public Long getId() { 
    return userId; 
  }

  public Icon getIcon() {
    return Images.get(ImageNums.PERSON_SMALL);
  }

  public void setEncSymKeys       (BAAsyCipherBlock encSymKeys)     { this.encSymKeys       = encSymKeys;       }
  public void setSymKeyFldShares  (BASymmetricKey symKeyFldShares)  { this.symKeyFldShares  = symKeyFldShares;  }
  public void setSymKeyCntNotes   (BASymmetricKey symKeyCntNotes)   { this.symKeyCntNotes   = symKeyCntNotes;   }

  public BAAsyCipherBlock getEncSymKeys()       { return encSymKeys;      }
  public BASymmetricKey   getSymKeyFldShares()  { return symKeyFldShares; }
  public BASymmetricKey   getSymKeyCntNotes()   { return symKeyCntNotes;  }


  /**
   * Called often by renderers, make this method efficiently return String
   * representation of User and cache the value instead of generating it every time.
   * @return User's short info name.
   */
  public String shortInfo() {
    String rc = null;
    if (userId == null) {
      rc = "User Unknown";
    } else {
      String infoName = handle != null ? handle : "User";
      Long infoId = userId;
      if (infoName.equals(shortInfoHandle) && infoId.equals(shortInfoId)) {
        rc = shortInfoCached;
      } else {
        shortInfoHandle = infoName;
        shortInfoId = infoId;
        shortInfoCached = infoName + " (" + infoId + ")";
        rc = shortInfoCached;
      }
    }
    return rc;
  }


  /**
   * Seals the <code> symKey* </code> to <code> encSymKeys </code> 
   * using the sealant object which is the user's public key.
   */
  public void seal(KeyRecord publicKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserRecord.class, "seal(KeyRecord publicKey)");
    if (symKeyFldShares == null || symKeyCntNotes == null)
      throw new IllegalArgumentException("All of the symmetric keys must be present before sealing can take place!");

    try {
      AsymmetricBlockCipher asyCipher = new AsymmetricBlockCipher();
      byte[] keys = new byte[32*2];
      System.arraycopy(symKeyFldShares.toByteArray(), 0, keys, 0, 32);
      System.arraycopy(symKeyCntNotes.toByteArray(), 0, keys, 32, 32);
      encSymKeys = asyCipher.blockEncrypt(publicKey.plainPublicKey, keys);
      pubKeyId = publicKey.keyId;
      super.seal();
    } catch (Throwable t) {
      if (trace != null) trace.exception(UserRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(UserRecord.class);
  }


  /**
   * Unseals the <code> encSymKeys </code> into <code> symKey* </code> 
   * using the unSealant object which is the user's private key.
   */
  public void unSeal(KeyRecord privateKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserRecord.class, "unSeal(KeyRecord privateKey)");
    if (!privateKey.keyId.equals(pubKeyId))
      throw new IllegalArgumentException("Specified private key record cannot decrypt these symmetric keys!");

    try {
      AsymmetricBlockCipher asyCipher = new AsymmetricBlockCipher();
      RSAPrivateKey rsaPrivateKey = privateKey.getPrivateKey();
      byte[] encSymKeysBytes = encSymKeys.toByteArray();
      byte[] keys = asyCipher.blockDecrypt(rsaPrivateKey, encSymKeysBytes).toByteArray();
      symKeyFldShares = new BASymmetricKey(keys, 0, 32);
      symKeyCntNotes = new BASymmetricKey(keys, 32, 32);
      super.unSeal();
    } catch (Throwable t) {
      if (trace != null) trace.exception(UserRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(UserRecord.class);
  }


  /**
   * Encode users password
   */
  public static BAEncodedPassword getBAEncodedPassword(char[] pass, String userName) {
    return getBAEncodedPassword(pass, userName, true);
  }
  public static BAEncodedPassword getBAEncodedPassword(char[] pass, String userName, boolean clearPass) {
    // add salt
    char[] salt = userName.toCharArray();

    char[] password = new char[pass.length + salt.length];
    System.arraycopy(pass, 0, password, 0, pass.length);
    System.arraycopy(salt, 0, password, pass.length, salt.length);

    BAEncodedPassword passcode = new BAEncodedPassword(password);

    if (clearPass) {
      for (int i=0; i<pass.length; i++)
        pass[i] = 0;
    }
    for (int i=0; i<password.length; i++)
      password[i] = 0;

    return passcode;
  }

  public boolean isAutoResp() {
    return autoResp != null && autoResp.charValue() == 'Y';
  }

  public boolean isNotifyByEmail() {
    return (notifyByEmail.shortValue() & EMAIL_NOTIFY_YES) != 0;
  }
  public boolean isSkipWarnExternal() {
    return (notifyByEmail.shortValue() & EMAIL_WARN_EXTERNAL) == 0;
  }

  public boolean isBusinessMasterAccount() {
    return status.shortValue() >= STATUS_BUSINESS && status.shortValue() <= STATUS_BUSINESS_HELD;
  }
  public boolean isBusinessSubAccount() {
    return status.shortValue() >= STATUS_BUSINESS_SUB && status.shortValue() <= STATUS_BUSINESS_SUB_HELD;
  }
  public boolean isBusinessAccount() {
    return status.shortValue() >= STATUS_BUSINESS && status.shortValue() <= STATUS_BUSINESS_SUB_HELD;
  }
  public boolean isPersonalAccount() {
    return status.shortValue() >= STATUS_PAID && status.shortValue() <= STATUS_PAID_HELD;
  }
  public boolean isGuestAccount() {
    return status.shortValue() >= STATUS_GUEST && status.shortValue() <= STATUS_GUEST_HELD;
  }
  public boolean isDemoAccount() {
    return status.shortValue() >= STATUS_PROMO && status.shortValue() <= STATUS_PROMO_HELD;
  }
  public boolean isWebAccount() {
    return status.shortValue() == STATUS_WEB;
  }
  public boolean isFreePromoAccount() {
    return isWebAccount() || isDemoAccount();
  }
  public boolean isCapableToManageUserAccounts() {
    return isBusinessAccount() && ((flags.longValue() & FLAG_ENABLE_CREATING_SUBACCOUNTS__MASTER_CAPABLE) != 0);
  }
  public boolean isHeld() {
    return (status.shortValue() & STATUS_MASK_HELD) != 0;
  }
  public static boolean isHeld(short status) {
    return (status & STATUS_MASK_HELD) != 0;
  }

  public boolean isCapableToEmptyFolder(FolderRecord fRec) {
    return fRec != null && (fRec.folderType.shortValue() == FolderRecord.RECYCLE_FOLDER || fRec.folderId.equals(junkFolderId) || fRec.folderId.equals(recycleFolderId));
  }

  public boolean isTransferLimitExceeded() {
    return isTransferLimitExceeded(0);
  }
  public boolean isTransferLimitExceeded(long additionalBytes) {
    long limit = transferLimit != null ? transferLimit.longValue() : 0L;
    long used = transferUsed != null ? transferUsed.longValue() : 0L;
    return limit != UserRecord.UNLIMITED_AMOUNT && used+additionalBytes > limit;
  }
  public boolean isStorageLimitExceeded() {
    return isStorageLimitExceeded(0);
  }
  public boolean isStorageLimitExceeded(long additionalBytes) {
    long limit = storageLimit != null ? storageLimit.longValue() : 0L;
    long used = storageUsed != null ? storageUsed.longValue() : 0L;
    return limit != UserRecord.UNLIMITED_AMOUNT && used+additionalBytes > limit;
  }
  /**
   * return true is warning level (within 2Kb of the limit or 10% whichever hits first)
   */
  public boolean isStorageAboveWarning() {
    long limit = storageLimit != null ? storageLimit.longValue() : 0L;
    long used = storageUsed != null ? storageUsed.longValue() : 0L;
    return limit != UserRecord.UNLIMITED_AMOUNT && (limit < used+(1024*1024) || limit < used+(limit*0.1));
  }


  public static Long[] getDefaultEmlIDs(UserRecord[] records) {
    Long[] IDs = null;
    if (records != null) {
      IDs = new Long[records.length];
      for (int i=0; i<records.length; i++) {
        IDs[i] = records[i].defaultEmlId;
      }
      IDs = (Long[]) ArrayUtils.removeDuplicates(IDs);
    }
    return IDs;
  }

  public static long getSumTransferUsed(UserRecord[] records) {
    long sum = 0;
    if (records != null) {
      for (int i=0; i<records.length; i++) {
        UserRecord rec = records[i];
        if (rec != null && rec.transferUsed != null)
          sum += rec.transferUsed.longValue();
      }
    }
    return sum;
  }

  public static long getSumStorageUsed(UserRecord[] records) {
    long sum = 0;
    if (records != null) {
      for (int i=0; i<records.length; i++) {
        UserRecord rec = records[i];
        if (rec != null && rec.storageUsed != null)
          sum += rec.storageUsed.longValue();
      }
    }
    return sum;
  }


  public static void trimChildToParent(UserRecord userToTrim, UserRecord trimToUser) {
    userToTrim.notifyByEmail = new Short((short) trimChildBitsToParentGrants(userToTrim.notifyByEmail, trimToUser.notifyByEmail, UserRecord.EMAIL_MASK__NO_GRANT));
    userToTrim.acceptingSpam = new Short((short) trimChildBitsToParentGrants(userToTrim.acceptingSpam, trimToUser.acceptingSpam, UserRecord.ACC_SPAM_MASK__NO_GRANT));
    userToTrim.flags = new Long((long) trimChildBitsToParentGrants(userToTrim.flags, trimToUser.flags, UserRecord.FLAG_MASK__NO_GRANT));
  }
  private static long trimChildBitsToParentGrants(Number childBits, Number parentBits, long grantMask) {
    long child = childBits.longValue();
    long parent = parentBits.longValue();
    // set NO GRANT bits for all since I cannot grant GRANT option
    child |= grantMask;
    long noGrantBits = parent & grantMask;
    // remove PERMIT bits which I have NO GRANT set
    child &= (~noGrantBits >>> 2);
    // set NO UPDATE bits which I have NO GRANT set
    child |= (noGrantBits >>> 1);
    return child;
  }


  /**
   * Keep bits already granted, only upgrade permits if they are higher.
   */
  public static void upgradeUserSettingsAndStatus(UserRecord toUpgrade, short newStatus, Long newParentUserId, boolean keepOldPermits) {
    UserRecord defUsr = getDefaultUserSettings(newStatus);
    toUpgrade.notifyByEmail = new Short((short) upgradeUserSettingBits(toUpgrade.notifyByEmail, defUsr.notifyByEmail, keepOldPermits, EMAIL_MASK__NO_GRANT));
    toUpgrade.acceptingSpam = new Short((short) upgradeUserSettingBits(toUpgrade.acceptingSpam, defUsr.acceptingSpam, keepOldPermits, ACC_SPAM_MASK__NO_GRANT));
    toUpgrade.flags = new Long(upgradeUserSettingBits(toUpgrade.flags, defUsr.flags, keepOldPermits, FLAG_MASK__NO_GRANT));
    // make sure that "Send notifications by e-mail" are off when there is no e-mail address set.
    boolean validEmailFormat = EmailRecord.gatherAddresses(toUpgrade.emailAddress) != null;
    toUpgrade.notifyByEmail = new Short((short) Misc.setBit(validEmailFormat, toUpgrade.notifyByEmail, UserRecord.EMAIL_NOTIFY_YES)); 
    toUpgrade.status = new Short(newStatus);
    toUpgrade.parentId = newParentUserId;
    toUpgrade.masterId = newParentUserId;
  }
  private static long upgradeUserSettingBits(Number toUpgrade, Number higherLevel, boolean keepOptionsSame, long noGrantMask) {
    long bits = 0;
    if (toUpgrade != null)
      bits = toUpgrade.longValue();

    long perms = higherLevel.longValue();

    // leave bits as they are set
    long newPermitBits = bits & (noGrantMask >>> 2);
    if (!keepOptionsSame)
      newPermitBits = (perms) & (noGrantMask >>> 2);
    // remove some NO UPDATE bits
    long newNoUpdateBits = perms & (noGrantMask >>> 1);
    long newNoGrantBits = perms & (noGrantMask);

    bits = newPermitBits | newNoUpdateBits | newNoGrantBits;
    return bits;
  }

  public static UserRecord getDefaultUserSettings(short status) {
    UserRecord uRec = new UserRecord();
    long notify, accSpam, flags;
    switch (status) {
      case STATUS_WEB:
      case STATUS_PROMO:
        notify = EMAIL_NOTIFY_YES | 
                 EMAIL_WARN_EXTERNAL | 
                 EMAIL_NOTIFY_INCLUDE_SUBJECT_AND_FROM_ADDRESS | 
                 EMAIL_MASK__NO_GRANT;
        accSpam = ACC_SPAM_YES_REG_EMAIL | 
                  ACC_SPAM_YES_SSL_EMAIL | 
                  ACC_SPAM_YES_INTER | 
                  ACC_SPAM_MASK__NO_UPDATE | 
                  ACC_SPAM_MASK__NO_GRANT;
        flags = FLAG_ENABLE_ACCOUNT_DELETE | 
                //FLAG_ENABLE_CREATING_SUBACCOUNTS__MASTER_CAPABLE |
                //FLAG_ENABLE_GIVEN_CONTACTS_ALTER |
                //FLAG_ENABLE_GIVEN_CONTACTS_DELETE |
                //FLAG_ENABLE_GIVEN_EMAILS_ALTER |
                //FLAG_ENABLE_GIVEN_EMAILS_DELETE |
                //FLAG_ENABLE_MAKING_CONTACTS_OUTSIDE_ORGANIZATION |
                //FLAG_ENABLE_NICKNAME_CHANGE |
                // web accounts can change password, promos can't
                (status == STATUS_WEB ? FLAG_ENABLE_PASSWORD_CHANGE : 0) | 
                FLAG_USER_OFFLINE_POPUP | 
                FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER | 
                //FLAG_SKIP_SECURE_REPLY_LINK_IN_EXTERNAL_EMAILS | 
                FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES | 
                FLAG_MASK__NO_GRANT | 
                FLAG_MASK__NO_UPDATE;
        notify = Misc.setBit(true, notify, EMAIL_WARN_EXTERNAL__NO_UPDATE);
        break;
      case STATUS_GUEST:
      case STATUS_PAID:
        notify = EMAIL_NOTIFY_YES | 
                 EMAIL_WARN_EXTERNAL | 
                 EMAIL_NOTIFY_INCLUDE_SUBJECT_AND_FROM_ADDRESS | 
                 EMAIL_MASK__NO_GRANT;
        accSpam = ACC_SPAM_YES_REG_EMAIL | 
                  ACC_SPAM_YES_SSL_EMAIL | 
                  ACC_SPAM_YES_INTER | 
                  ACC_SPAM_MASK__NO_GRANT;
        flags = FLAG_ENABLE_ACCOUNT_DELETE | 
                //FLAG_ENABLE_CREATING_SUBACCOUNTS__MASTER_CAPABLE |
                //FLAG_ENABLE_GIVEN_CONTACTS_ALTER |
                //FLAG_ENABLE_GIVEN_CONTACTS_DELETE |
                //FLAG_ENABLE_GIVEN_EMAILS_ALTER |
                //FLAG_ENABLE_GIVEN_EMAILS_DELETE |
                //FLAG_ENABLE_MAKING_CONTACTS_OUTSIDE_ORGANIZATION |
                FLAG_ENABLE_NICKNAME_CHANGE | 
                FLAG_ENABLE_PASSWORD_CHANGE | 
                FLAG_USER_OFFLINE_POPUP | 
                FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER | 
                //FLAG_SKIP_SECURE_REPLY_LINK_IN_EXTERNAL_EMAILS | 
                FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES | 
                FLAG_MASK__NO_GRANT | 
                FLAG_MASK__NO_UPDATE;
        flags = Misc.setBit(false, flags, FLAG_USER_OFFLINE_POPUP__NO_UPDATE);
        flags = Misc.setBit(false, flags, FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER__NO_UPDATE);
        flags = Misc.setBit(false, flags, FLAG_SKIP_SECURE_REPLY_LINK_IN_EXTERNAL_EMAILS__NO_UPDATE);
        flags = Misc.setBit(false, flags, FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES__NO_UPDATE);
        flags = Misc.setBit(false, flags, FLAG_DISABLE_AUTO_UPDATES__NO_UPDATE);
        break;
      case STATUS_BUSINESS:
        notify = EMAIL_NOTIFY_YES | 
                 EMAIL_NOTIFY_INCLUDE_SUBJECT_AND_FROM_ADDRESS | 
                 EMAIL_WARN_EXTERNAL;
        accSpam = ACC_SPAM_YES_REG_EMAIL | 
                  ACC_SPAM_YES_SSL_EMAIL |
                  ACC_SPAM_YES_INTER;
        flags = FLAG_ENABLE_ACCOUNT_DELETE | 
                FLAG_ENABLE_CREATING_SUBACCOUNTS__MASTER_CAPABLE | 
                FLAG_ENABLE_GIVEN_CONTACTS_ALTER | 
                FLAG_ENABLE_GIVEN_CONTACTS_DELETE | 
                FLAG_ENABLE_GIVEN_EMAILS_ALTER | 
                FLAG_ENABLE_GIVEN_EMAILS_DELETE | 
                FLAG_ENABLE_MAKING_CONTACTS_OUTSIDE_ORGANIZATION | 
                FLAG_ENABLE_NICKNAME_CHANGE | 
                FLAG_ENABLE_PASSWORD_CHANGE | 
                FLAG_USER_OFFLINE_POPUP | 
                FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER | 
                //FLAG_SKIP_SECURE_REPLY_LINK_IN_EXTERNAL_EMAILS | 
                FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES | 
                FLAG_ENABLE_GIVEN_MASTER_FOLDERS_DELETE |
                //FLAG_MASK__NO_GRANT | 
                FLAG_MASK__NO_UPDATE;
        flags = Misc.setBit(false, flags, FLAG_USER_OFFLINE_POPUP__NO_UPDATE);
        flags = Misc.setBit(false, flags, FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER__NO_UPDATE);
        flags = Misc.setBit(false, flags, FLAG_SKIP_SECURE_REPLY_LINK_IN_EXTERNAL_EMAILS__NO_UPDATE);
        flags = Misc.setBit(false, flags, FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES__NO_UPDATE);
        flags = Misc.setBit(false, flags, FLAG_DISABLE_AUTO_UPDATES__NO_UPDATE);
        break;
      case STATUS_BUSINESS_SUB:
        notify = EMAIL_NOTIFY_YES | 
                 EMAIL_NOTIFY_INCLUDE_SUBJECT_AND_FROM_ADDRESS | 
                 EMAIL_WARN_EXTERNAL;
        accSpam = ACC_SPAM_YES_REG_EMAIL | 
                  ACC_SPAM_YES_SSL_EMAIL | 
                  ACC_SPAM_YES_INTER;
        flags = //FLAG_ENABLE_ACCOUNT_DELETE | 
                //FLAG_ENABLE_CREATING_SUBACCOUNTS__MASTER_CAPABLE | 
                //FLAG_ENABLE_GIVEN_CONTACTS_ALTER | 
                //FLAG_ENABLE_GIVEN_CONTACTS_DELETE | 
                //FLAG_ENABLE_GIVEN_EMAILS_ALTER | 
                //FLAG_ENABLE_GIVEN_EMAILS_DELETE | 
                FLAG_ENABLE_MAKING_CONTACTS_OUTSIDE_ORGANIZATION | 
                //FLAG_ENABLE_NICKNAME_CHANGE | 
                FLAG_ENABLE_PASSWORD_CHANGE | 
                FLAG_USER_OFFLINE_POPUP | 
                FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER | 
                //FLAG_SKIP_SECURE_REPLY_LINK_IN_EXTERNAL_EMAILS | 
                FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES | 
                //FLAG_ENABLE_GIVEN_MASTER_FOLDERS_DELETE |
                FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY |
                //FLAG_MASK__NO_GRANT | 
                FLAG_MASK__NO_UPDATE;
        flags = Misc.setBit(false, flags, FLAG_USER_OFFLINE_POPUP__NO_UPDATE);
        flags = Misc.setBit(false, flags, FLAG_SKIP_SECURE_REPLY_LINK_IN_EXTERNAL_EMAILS__NO_UPDATE);
        flags = Misc.setBit(false, flags, FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES__NO_UPDATE);
        flags = Misc.setBit(false, flags, FLAG_DISABLE_AUTO_UPDATES__NO_UPDATE);
        break;
      default:
        throw new IllegalArgumentException("Invalid status.");
    }
    uRec.notifyByEmail = new Short((short) notify);
    uRec.acceptingSpam = new Short((short) accSpam);
    uRec.flags = new Long(flags);
    uRec.status = new Short(status);
    return uRec;
  }

  public void merge(Record updated) {
    if (updated instanceof UserRecord) {
      UserRecord record = (UserRecord) updated;
      if (record.userId            != null) userId           = record.userId;
      if (record.handle            != null) handle           = record.handle;
      if (record.dateCreated       != null) dateCreated      = record.dateCreated;
      if (record.dateExpired       != null) dateExpired      = record.dateExpired;
      if (record.dateLastLogin     != null) dateLastLogin    = record.dateLastLogin;
      if (record.dateLastLogout    != null) dateLastLogout   = record.dateLastLogout;
      if (record.status            != null) status           = record.status;
      if (record.statusInfo        != null) statusInfo       = record.statusInfo;
      if (record.notifyByEmail     != null) notifyByEmail    = record.notifyByEmail;
      if (record.dateNotified      != null) dateNotified     = record.dateNotified;
      if (record.acceptingSpam     != null) acceptingSpam    = record.acceptingSpam;
      if (record.emailAddress      != null) emailAddress     = record.emailAddress;
      if (record.passwordHash      != null) passwordHash     = record.passwordHash;
      if (record.encSymKeys        != null) encSymKeys       = record.encSymKeys;
      if (record.pubKeyId          != null) pubKeyId         = record.pubKeyId;
      if (record.currentKeyId      != null) currentKeyId     = record.currentKeyId;
      if (record.fileFolderId      != null) fileFolderId     = record.fileFolderId;
      if (record.addrFolderId      != null) addrFolderId     = record.addrFolderId;
      if (record.whiteFolderId     != null) whiteFolderId    = record.whiteFolderId;
      if (record.draftFolderId     != null) draftFolderId    = record.draftFolderId;
      if (record.msgFolderId       != null) msgFolderId      = record.msgFolderId;
      if (record.junkFolderId      != null) junkFolderId     = record.junkFolderId;
      if (record.sentFolderId      != null) sentFolderId     = record.sentFolderId;
      if (record.contactFolderId   != null) contactFolderId  = record.contactFolderId;
      if (record.keyFolderId       != null) keyFolderId      = record.keyFolderId;
      if (record.recycleFolderId   != null) recycleFolderId  = record.recycleFolderId;
      if (record.storageLimit      != null) storageLimit     = record.storageLimit;
      if (record.storageUsed       != null) storageUsed      = record.storageUsed;
      if (record.checkStorageDate  != null) checkStorageDate = record.checkStorageDate;
      if (record.transferLimit     != null) transferLimit    = record.transferLimit;
      if (record.transferUsed      != null) transferUsed     = record.transferUsed;

      if (record.maxSubAccounts    != null) maxSubAccounts   = record.maxSubAccounts;
      if (record.parentId          != null) parentId         = record.parentId;
      if (record.masterId          != null) masterId         = record.masterId;
      if (record.defaultEmlId      != null) defaultEmlId     = record.defaultEmlId;
      if (record.flags             != null) flags            = record.flags;
      if (record.autoResp          != null) autoResp         = record.autoResp;
      if (record.online            != null) online           = record.online;

      // merge un-sealed data
      if (record.symKeyFldShares   != null) symKeyFldShares  = record.symKeyFldShares;
      if (record.symKeyCntNotes    != null) symKeyCntNotes   = record.symKeyCntNotes;
    } else {
      super.mergeError(updated);
    }
  }


  public String toString() {
    return "[UserRecord"
      + ": userId="           + userId
      + ", handle="           + handle
      + ", dateCreated="      + dateCreated
      + ", dateExpired="      + dateExpired
      + ", dateLastLogin="    + dateLastLogin
      + ", dateLastLogout="   + dateLastLogout
      + ", status="           + status
      + ", statusInfo="       + statusInfo
      + ", notifyByEmail="    + notifyByEmail
      + ", dateNotified="     + dateNotified
      + ", acceptingSpam="    + acceptingSpam
      + ", emailAddress="     + emailAddress
      + ", passwordHash="     + passwordHash
      + ", encSymKeys="       + encSymKeys
      + ", pubKeyId="         + pubKeyId
      + ", currentKeyId="     + currentKeyId
      + ", fileFolderId="     + fileFolderId
      + ", addrFolderId="     + addrFolderId
      + ", whiteFolderId="    + whiteFolderId
      + ", draftFolderId="    + draftFolderId
      + ", msgFolderId="      + msgFolderId
      + ", junkFolderId="     + junkFolderId
      + ", sentFolderId="     + sentFolderId
      + ", contactFolderId="  + contactFolderId
      + ", keyFolderId="      + keyFolderId
      + ", recycleFolderId="  + recycleFolderId
      + ", storageLimit="     + storageLimit
      + ", storageUsed="      + storageUsed
      + ", checkStorageDate=" + checkStorageDate
      + ", transferLimit="    + transferLimit
      + ", transferUsed="     + transferUsed
      + ", maxSubAccounts="   + maxSubAccounts
      + ", parentId="         + parentId
      + ", masterId="         + masterId
      + ", defaultEmlId="     + defaultEmlId
      + ", flags="            + flags
      + ", autoResp="         + autoResp
      + ", online="           + online
      + ", un-sealed data >> "
      + ", symKeyFldShares="  + symKeyFldShares
      + ", symKeyCntNotes="   + symKeyCntNotes
      + "]";
  }

  public void setId(Long id) {
    userId = id;
  }

  public short getMemberType() {
    return Record.RECORD_TYPE_USER;
  }

  public Long getMemberId() {
    return userId;
  }

  private static String getFlagInfo(Short notifyByEmail, Short acceptingSpam, Long flags) {
    StringBuffer sb = new StringBuffer();

    getFlagInfo("Create subaccounts", flags, FLAG_ENABLE_CREATING_SUBACCOUNTS__MASTER_CAPABLE, sb);
    sb.append("\n");

    sb.append("Account page\n");
    getFlagInfo("Send e-mail notification when new messages arrive", notifyByEmail, EMAIL_NOTIFY_YES, sb);
    getFlagInfo("Include sender address and message subject", notifyByEmail, EMAIL_NOTIFY_INCLUDE_SUBJECT_AND_FROM_ADDRESS, sb);
    sb.append("\n");

    sb.append("Options page\n");
    getFlagInfo("Accept messages from outside of Contact List", acceptingSpam, ACC_SPAM_YES_INTER, sb);
    getFlagInfo("Accept regular external e-mail", acceptingSpam, ACC_SPAM_YES_REG_EMAIL, sb);
    getFlagInfo("Accept encrypted external e-mail", acceptingSpam, ACC_SPAM_YES_SSL_EMAIL, sb);
    getFlagInfo("Do not include secure reply link in external e-mail", flags, FLAG_SKIP_SECURE_REPLY_LINK_IN_EXTERNAL_EMAILS, sb);
    getFlagInfo("Display a warning before sending unencrypted e-mail", notifyByEmail, EMAIL_WARN_EXTERNAL, sb);
    getFlagInfo("Preview Rich Text email in Plain Text mode", notifyByEmail, EMAIL_MANUAL_SELECT_PREVIEW_MODE, sb);
    getFlagInfo("Disable auto updates", flags, FLAG_DISABLE_AUTO_UPDATES, sb);
    getFlagInfo("User offline popup", flags, FLAG_USER_OFFLINE_POPUP, sb);
    getFlagInfo("Store Key on server", flags, FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER, sb);
    getFlagInfo("Enable Password Reset and Key Recovery", flags, FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY, sb);
    getFlagInfo("Use enter key to send chat", flags, FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES, sb);
    sb.append("\n");

    sb.append("Permissions page\n");
    getFlagInfo("Change password", flags, FLAG_ENABLE_PASSWORD_CHANGE, sb);
    getFlagInfo("Change username", flags, FLAG_ENABLE_NICKNAME_CHANGE, sb);
    getFlagInfo("Destruction of account", flags, FLAG_ENABLE_ACCOUNT_DELETE, sb);
    getFlagInfo("Modify assigned contacts", flags, FLAG_ENABLE_GIVEN_CONTACTS_ALTER, sb);
    getFlagInfo("Delete assigned contacts", flags, FLAG_ENABLE_GIVEN_CONTACTS_DELETE, sb);
    getFlagInfo("Change assigned email address", flags, FLAG_ENABLE_GIVEN_EMAILS_ALTER, sb);
    getFlagInfo("Delete assigned email address", flags, FLAG_ENABLE_GIVEN_EMAILS_DELETE, sb);
    getFlagInfo("Delete admin assigned folders", flags, FLAG_ENABLE_GIVEN_MASTER_FOLDERS_DELETE, sb);
    getFlagInfo("Contacts outside organization", flags, FLAG_ENABLE_MAKING_CONTACTS_OUTSIDE_ORGANIZATION, sb);
    sb.append("\n");

    return sb.toString();
  }
  private static void getFlagInfo(String flagName, Number bits, long flag, StringBuffer sb) {
    if (bits != null) {
      for (int i=0; i<52; i++) {
        if (flagName.length() > i)
          sb.append(flagName.charAt(i));
        else
          sb.append(' ');
      }
      sb.append(": ");
      sb.append(Misc.isBitSet(bits, flag) ? "TRUE  " : "FALSE ");
      sb.append(Misc.isBitSet(bits, flag << 1) ? "no-update " : "update    ");
      sb.append(Misc.isBitSet(bits, flag << 2) ? "no-grant " : "grant    ");
      sb.append("\n");
    }
  }

  private static void parseForFlagsAndPrint(String[] args) {
    Short notifyByEmail = null;
    Short acceptingSpam = null;
    Long flags = null;
    System.out.println("Parsing input looking for flags...");
    StringBuffer oneArg = new StringBuffer();
    for (int i=0; i<args.length; i++) {
      oneArg.append(args[i]);
      oneArg.append(' ');
    }
    String[] splits = oneArg.toString().split("[ =,]+");
    for (int i=0; i<splits.length; i++) {
      String split = splits[i];
      if (split.equalsIgnoreCase("notifyByEmail"))
        notifyByEmail = new Short(splits[i+1]);
      else if (split.equalsIgnoreCase("acceptingSpam"))
        acceptingSpam = new Short(splits[i+1]);
      else if (split.equalsIgnoreCase("flags"))
        flags = new Long(splits[i+1]);
    }
    if (notifyByEmail != null || acceptingSpam != null || flags != null) {
      System.out.println("Translating FLAGs: notifyByEmail="+notifyByEmail+", acceptingSpam="+acceptingSpam+", flags="+flags);
      System.out.println("\n");
      System.out.println(getFlagInfo(notifyByEmail, acceptingSpam, flags));
    } else {
      System.out.println("Example: notifyByEmail=<value> acceptiongSpam=<value> flags=<value>");
    }
  }

  public static void main(String[] args) {
    parseForFlagsAndPrint(args);
  }
}
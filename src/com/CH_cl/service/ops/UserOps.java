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

package com.CH_cl.service.ops;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.actions.usr.UsrALoginSecureSession;
import com.CH_cl.service.cache.CacheUsrUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.DefaultReplyRunner;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.records.EmailAddressRecord;
import com.CH_cl.service.records.filters.FolderFilter;
import com.CH_cl.util.GlobalSubProperties;
import com.CH_co.cryptx.*;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.service.msg.dataSets.usr.*;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.MsgFilter;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import com.CH_co.util.URLs;
import java.io.File;
import java.security.DigestException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

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
* <b>$Revision: 1.34 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class UserOps extends Object {


  public static void fetchUnknownUsers(ServerInterfaceLayer SIL, StatRecord[] statRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserOps.class, "fetchUnknownUsers(ServerInterfaceLayer SIL, StatRecord[] statRecords)");
    if (trace != null) trace.args(SIL, statRecords);

    // Gather all userIDs for which we don't have user handles and fetch them.
    ArrayList userIDsL = null;
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    for (int i=0; i<statRecords.length; i++) {
      StatRecord stat = statRecords[i];
      if (cache.getUserRecord(stat.ownerUserId) == null) {
        if (userIDsL == null) userIDsL = new ArrayList();
        if (!userIDsL.contains(stat.ownerUserId)) {
          userIDsL.add(stat.ownerUserId);
        }
      }
    }
    if (userIDsL != null && userIDsL.size() > 0) {
      Long[] userIDs = (Long[]) ArrayUtils.toArray(userIDsL, Long.class);
      SIL.submitAndWait(new MessageAction(CommandCodes.USR_Q_GET_HANDLES, new Obj_IDList_Co(userIDs)), 30000);
    }

    if (trace != null) trace.exit(UserOps.class);
  }

  public static void fetchUnknownUsers(ServerInterfaceLayer SIL, EmailRecord[] emailRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserOps.class, "fetchUnknownUsers(ServerInterfaceLayer SIL, EmailRecord[] emailRecords)");
    if (trace != null) trace.args(SIL, emailRecords);

    // Gather all userIDs for which we don't have user handles and fetch them.
    ArrayList userIDsL = null;
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    for (int i=0; i<emailRecords.length; i++) {
      EmailRecord email = emailRecords[i];
      if (email.userId != null && cache.getUserRecord(email.userId) == null) {
        if (userIDsL == null) userIDsL = new ArrayList();
        if (!userIDsL.contains(email.userId)) {
          userIDsL.add(email.userId);
        }
      }
      if (email.creatorId != null && cache.getUserRecord(email.creatorId) == null) {
        if (userIDsL == null) userIDsL = new ArrayList();
        if (!userIDsL.contains(email.creatorId)) {
          userIDsL.add(email.creatorId);
        }
      }
    }
    if (userIDsL != null && userIDsL.size() > 0) {
      Long[] userIDs = (Long[]) ArrayUtils.toArray(userIDsL, Long.class);
      SIL.submitAndWait(new MessageAction(CommandCodes.USR_Q_GET_HANDLES, new Obj_IDList_Co(userIDs)), 30000);
    }

    if (trace != null) trace.exit(UserOps.class);
  }

  public static boolean sendPasswordChange(ServerInterfaceLayer SIL, BAEncodedPassword ba, boolean storeKeyOnServer, File privateKeyFile, StringBuffer errBuffer) {
    return sendPasswordChange(SIL, null, ba, storeKeyOnServer, null, privateKeyFile, errBuffer);
  }
  public static boolean sendPasswordChange(ServerInterfaceLayer SIL, String newUserName, BAEncodedPassword ba, boolean storeKeyOnServer, StringBuffer errBuffer) {
    return sendPasswordChange(SIL, newUserName, ba, storeKeyOnServer, null, null, errBuffer);
  }
  private static boolean sendPasswordChange(ServerInterfaceLayer SIL, String newUserName, BAEncodedPassword ba, boolean storeKeyOnServer, Integer actionCode, File privateKeyFile, StringBuffer errBuffer) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserOps.class, "sendPasswordChange(ServerInterfaceLayer SIL, String newUserName, BAEncodedPassword ba, boolean storeKeyOnServer, Integer actionCode, File privateKeyFile, StringBuffer errBuffer)");
    if (trace != null) trace.args(SIL, newUserName, ba);
    if (trace != null) trace.args(storeKeyOnServer);
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(privateKeyFile);
    if (trace != null) trace.args(errBuffer);

    boolean error = false;
    boolean storeKeyOnLocal = !storeKeyOnServer;

    if (actionCode == null) {
      actionCode = new Integer(CommandCodes.USR_Q_ALTER_PASSWORD);
    }

    FetchedDataCache cache = SIL.getFetchedDataCache();
    KeyRecord keyRecord = cache.getKeyRecordMyCurrent();
    keyRecord.seal(ba);

    // Check if the encrypted private part of the key is stored remotely... if so we will need to send an update.
    String keyPropertyName = "Enc" + RSAPrivateKey.OBJECT_NAME + "_" + keyRecord.keyId;
    String keyPropertyNameInfo = keyPropertyName+"_info";
    String keyInfo = cache.getUserRecord().shortInfo();
    GlobalSubProperties keyProperties = new GlobalSubProperties(privateKeyFile, GlobalSubProperties.PROPERTY_EXTENSION_KEYS);
    String oldProperty = keyProperties.getProperty(keyPropertyName);
    boolean wasLocalKey = (oldProperty != null && oldProperty.length() > 0);
    boolean storedLocally = false;

    // If key is to be stored locally, do it first before removing it from remote storage.
    if (storeKeyOnLocal) {
      try {
        keyProperties.setProperty(keyPropertyName, keyRecord.getEncPrivateKey().getHexContent());
        keyProperties.setProperty(keyPropertyNameInfo, keyInfo);
        keyProperties.store();
        UsrALoginSecureSession.addPathToLastPrivKeyPaths(keyProperties.getPropertiesFullFileName());
        storedLocally = true;
      } catch (Throwable t) {
        if (errBuffer != null) errBuffer.append("\n\n"+t.getLocalizedMessage());
        error = true;
      }
    }

    if (!error) {
      Usr_AltUsrPass_Rq updateKeyRequest = createAltUserPassRequest(SIL, newUserName, ba, storeKeyOnServer);
      MessageAction updateKeyAction = new MessageAction(actionCode.intValue(), updateKeyRequest);
      ClientMessageAction msgAction = SIL.submitAndFetchReply(updateKeyAction, 60000);
      if (msgAction == null || msgAction.getActionCode() <= 0) {
        if (errBuffer != null && (msgAction == null || msgAction.getActionCode() == 0))
          errBuffer.append("\n\nserver failed to respond");
        error = true;
      } else {
        cache.setEncodedPassword(ba);
        UserRecord userRec = cache.getUserRecord();
        userRec.passwordHash = ba.getHashValue();
        if (newUserName != null)
          userRec.handle = newUserName;
        // The original login user record wasn't in cache so now that we have it, lets update it here.
        ((Usr_LoginSecSess_Rq) SIL.getLoginMsgDataSet()).userRecord = userRec;
      }
      DefaultReplyRunner.nonThreadedRun(SIL, msgAction);
    }

    // if all ok
    if (!error && storeKeyOnServer && wasLocalKey) {
      // delete the key from local storage...
      keyProperties.remove(keyPropertyName);
      keyProperties.remove(keyPropertyNameInfo);
      keyProperties.store();
    } else if (error) {
      if (storeKeyOnServer && wasLocalKey) {
        // storage on server failed, just leave the local keys as they are
      } else if (storeKeyOnLocal && wasLocalKey) {
        // password change on local keys failed, restore local key storage
        keyProperties.setProperty(keyPropertyName, oldProperty);
        keyProperties.setProperty(keyPropertyNameInfo, keyInfo);
        keyProperties.store();
      } else if (storeKeyOnLocal && !wasLocalKey && storedLocally) {
        // delete the key from local storage...
        keyProperties.remove(keyPropertyName);
        keyProperties.remove(keyPropertyNameInfo);
        keyProperties.store();
      }
    }

    boolean success = !error;

    if (trace != null) trace.exit(UserOps.class, success);
    return success;
  }

  public static boolean sendPasswordReset(ServerInterfaceLayer SIL, BAEncodedPassword ba, KeyRecoveryRecord[] subAccountsRecoveryRecs, char[] newPassword) {
    boolean success = false;
    Usr_AltUsrPass_Rq altUsrPassSet = createAltUserPassRequest(SIL, null, ba, false);

    UserRecord[] subUsers = new UserRecord[subAccountsRecoveryRecs.length];
    KeyRecord[] subKeys = new KeyRecord[subAccountsRecoveryRecs.length];

    FetchedDataCache cache = SIL.getFetchedDataCache();

    for (int i=0; i<subAccountsRecoveryRecs.length; i++) {
      KeyRecord subKey = cache.getKeyRecord(subAccountsRecoveryRecs[i].keyId);
      UserRecord subUser = cache.getUserRecord(subKey.ownerUserId);
      BAEncodedPassword encPass = UserRecord.getBAEncodedPassword(newPassword, subUser.handle, false);
      subUser.passwordHash = encPass.getHashValue();
      // calculate new subKey private key
      KeyRecord masterKey = cache.getKeyRecord(subAccountsRecoveryRecs[i].masterKeyId);
      subAccountsRecoveryRecs[i].unSeal(masterKey);
      subKey.setPrivateKey(subAccountsRecoveryRecs[i].getPvtKey());
      subKey.seal(encPass);
      subUsers[i] = subUser;
      subKeys[i] = subKey;
    }

    Obj_List_Co request = new Obj_List_Co(new Object[] { altUsrPassSet, new Usr_PassReset_Rq(subUsers, subKeys) });
    ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.USR_Q_PASSWORD_RESET, request), 60000);
    success = msgAction != null && msgAction.getActionCode() >= 0;
    DefaultReplyRunner.nonThreadedRun(SIL, msgAction);
    return success;
  }

  public static boolean sendChangeStatusSubAccounts(ServerInterfaceLayer SIL, BAEncodedPassword ba, Long[] toManageUserIDs, short toStatus, String statusMsg) {
    boolean success = false;
    Usr_AltUsrPass_Rq altUsrPassSet = createAltUserPassRequest(SIL, null, ba, false);
    Obj_List_Co request = new Obj_List_Co();
    request.objs = new Object[] { altUsrPassSet, new Obj_IDList_Co(toManageUserIDs), new Short(toStatus), statusMsg };
    ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.USR_Q_CHANGE_STATUS, request), 60000);
    success = msgAction != null && msgAction.getActionCode() >= 0;
    DefaultReplyRunner.nonThreadedRun(SIL, msgAction);
    return success;
  }

  public static boolean sendDeleteAccount(ServerInterfaceLayer SIL, BAEncodedPassword ba) {
    boolean success = false;
    Usr_AltUsrPass_Rq altUsrPassSet = createAltUserPassRequest(SIL, null, ba, false);
    ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.USR_Q_DELETE, altUsrPassSet), 5*60000);
    success = msgAction.getActionCode() >= 0;
    DefaultReplyRunner.nonThreadedRun(SIL, msgAction);
    return success;
  }

  public static boolean sendDeleteSubAccounts(ServerInterfaceLayer SIL, BAEncodedPassword ba, Long[] toDeleteUserIDs) {
    boolean success = false;
    Usr_AltUsrPass_Rq altUsrPassSet = createAltUserPassRequest(SIL, null, ba, false);
    Obj_List_Co request = new Obj_List_Co();
    request.objs = new Object[] { altUsrPassSet, new Obj_IDList_Co(toDeleteUserIDs) };
    ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.USR_Q_REMOVE, request), 5*60000);
    success = msgAction.getActionCode() >= 0;
    DefaultReplyRunner.nonThreadedRun(SIL, msgAction);
    return success;
  }

  private static Usr_AltUsrPass_Rq createAltUserPassRequest(ServerInterfaceLayer SIL, String newUserName, BAEncodedPassword ba, boolean includePrivKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserOps.class, "createAltUserPassRequest(ServerInterfaceLayer SIL, String newUserName, BAEncodedPassword ba, boolean includePrivKey)");
    if (trace != null) trace.args(SIL, newUserName, ba);
    if (trace != null) trace.args(includePrivKey);

    FetchedDataCache cache = SIL.getFetchedDataCache();

    KeyRecord keyRecord = cache.getKeyRecordMyCurrent();
    keyRecord.seal(ba);

    BASymmetricKey symKey = new BASymmetricKey(32);
    AsymmetricBlockCipher asyCipher;
    try {
      asyCipher = new AsymmetricBlockCipher();
    } catch (NoSuchAlgorithmException e) {
      if (trace != null) trace.exception(UserOps.class, 100, e);
      throw new IllegalStateException("Could not instantiate an Asymmetric Cipher!");
    }
    BAAsyCipherBlock signed32ByteProof;
    try {
      signed32ByteProof = asyCipher.signBlock(keyRecord.getPrivateKey(), symKey.toByteArray());
    } catch (DigestException e) {
      if (trace != null) trace.exception(UserOps.class, 200, e);
      throw new IllegalStateException("Digest Exception while signing a key update proof!");
    }

    KeyRecord keyUpdate = keyRecord;
    if (includePrivKey) {
      // just leave the key record as it is...
    } else {
      // Mask out the encrypted private portion of the key if it should remain on local system only.
      keyUpdate = (KeyRecord) keyUpdate.clone();
      keyUpdate.setEncPrivateKey(null);
    }

    UserRecord uRec = (UserRecord) cache.getUserRecord().clone();
    uRec.handle = newUserName;
    uRec.passwordHash = ba.getHashValue();

    Usr_AltUsrPass_Rq request = new Usr_AltUsrPass_Rq(uRec, keyUpdate, signed32ByteProof.toByteArray());
    if (trace != null) trace.exit(UserOps.class, request);
    return request;
  }

  /**
  * @return converted records into ContactRecords or User records, unwinding Groups as well
  */
  public static Record[] getOrFetchFamiliarUsers(ServerInterfaceLayer SIL, Record[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserOps.class, "getOrFetchFamiliarUsers(Record[] records)");
    if (trace != null) trace.args(records);

    Record[] users = null;
    ArrayList cRecsL = new ArrayList();
    ArrayList fRecsL = new ArrayList();

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    Long userId = cache.getMyUserId();
    if (records != null && records.length > 0) {
      for (int i=0; i<records.length; i++) {
        if (records[i] instanceof ContactRecord) {
          ContactRecord cRec = (ContactRecord) records[i];
          if (!cRecsL.contains(cRec) && cRec.ownerUserId.equals(userId) && cRec.isOfActiveType())
            cRecsL.add(cRec);
        } else if (records[i] instanceof FolderPair) {
          FolderPair fPair = (FolderPair) records[i];
          if (fPair.getFolderRecord().isGroupType()) {
            if (!fRecsL.contains(fPair))
              fRecsL.add(fPair);
          }
        }
      }
    }

    ArrayList usersL = new ArrayList(cRecsL);

    if (fRecsL.size() > 0) {
      ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.FLD_Q_GET_ACCESS_USERS, new Obj_IDList_Co(RecordUtils.getIDs(fRecsL))), 30000);
      DefaultReplyRunner.nonThreadedRun(SIL, msgAction);
      if (msgAction != null && msgAction.getActionCode() == CommandCodes.USR_A_GET_HANDLES) {
        Usr_UsrHandles_Rp usrSet = (Usr_UsrHandles_Rp) msgAction.getMsgDataSet();
        UserRecord[] usrRecs = usrSet.userRecords;
        for (int i=0; i<usrRecs.length; i++) {
          Record user = CacheUsrUtils.convertUserIdToFamiliarUser(usrRecs[i].userId, true, false);
          if (!usersL.contains(user))
            usersL.add(user);
        }
      }
    }

    users = (Record[]) ArrayUtils.toArray(usersL, Record.class);

    if (trace != null) trace.exit(UserOps.class, users);
    return users;
  }

  public static String[] getOrFetchOrMakeDefaultEmail(ServerInterfaceLayer SIL, Long userId, boolean isGeneratePersonalPart) {
    String[] emailAddr = null;
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    UserRecord uRec = cache.getUserRecord(userId);
    if (uRec != null) {
      emailAddr = getOrFetchDefaultEmail(SIL, uRec, isGeneratePersonalPart);
    }
    if (emailAddr == null) {
      if (uRec != null)
        emailAddr = makeDefaultEmail(uRec, isGeneratePersonalPart);
      else
        emailAddr = makeDefaultEmail(userId, isGeneratePersonalPart);
    }
    return emailAddr;
  }

  private static String[] makeDefaultEmail(Long userId, boolean isGeneratePersonalPart) {
    String personal = isGeneratePersonalPart ? userId.toString() : null;
    String emailShort = "" + userId + "@" + URLs.getElements(URLs.DOMAIN_MAIL)[0];
    String emailFull = emailShort; // don't include the userId as part of full email address
    return new String[] { personal, emailShort, emailFull };
  }

  private static String[] makeDefaultEmail(UserRecord userRec, boolean isGeneratePersonalPart) {
    String personal = isGeneratePersonalPart ? userRec.handle : null;
    String emailShort = "" + userRec.userId + "@" + URLs.getElements(URLs.DOMAIN_MAIL)[0];
    String emailFull = personal != null ? personal + "<" + emailShort + ">" : emailShort;
    return new String[] { personal, emailShort, emailFull };
  }

  /**
  * Find user's default email address in the cache or fetch from the server.
  * @return personal (nullable), short, full parts of default email address for specified user
  */
  public static String[] getOrFetchDefaultEmail(ServerInterfaceLayer SIL, UserRecord userRecord, boolean isGeneratePersonalPart) {
    String[] emailAddr = CacheUsrUtils.getCachedDefaultEmail(userRecord, isGeneratePersonalPart);
    if (emailAddr == null) {
      if (userRecord.defaultEmlId != null && userRecord.defaultEmlId.longValue() != UserRecord.GENERIC_EMAIL_ID) {
        SIL.submitAndWait(new MessageAction(CommandCodes.USR_Q_GET_HANDLES, new Obj_IDList_Co(userRecord.userId)), 30000);
      }
      emailAddr = CacheUsrUtils.getCachedDefaultEmail(userRecord, isGeneratePersonalPart);
    }
    return emailAddr;
  }

  public static Object[] getCachedOrMakeSenderDefaultEmailSet(MsgDataRecord dataRecord) {
    Record sender = null;
    String senderEmailShort = "";
    String senderEmailFull = "";

    String fromEmailAddress = dataRecord.getFromEmailAddress();
    if (dataRecord.isEmail() || fromEmailAddress != null) {
      sender = new EmailAddressRecord(fromEmailAddress);
      EmailAddressRecord tempSender = new EmailAddressRecord(fromEmailAddress);
      senderEmailFull = tempSender.address;
      senderEmailShort = senderEmailFull;
      String nick = EmailRecord.getNick(senderEmailFull);
      String domain = EmailRecord.getDomain(senderEmailFull);
      if (nick != null && nick.length() > 0 && domain != null && domain.length() > 0) {
        senderEmailShort = nick + "@" + domain;
      }
    } else {
      sender = FetchedDataCache.getSingleInstance().getUserRecord(dataRecord.senderUserId);
      String[] emls = null;
      if (sender != null)
        emls = CacheUsrUtils.getCachedDefaultEmail((UserRecord) sender, true);
      if (emls == null) {
        if (sender != null)
          emls = makeDefaultEmail((UserRecord) sender, true);
        else
          emls = makeDefaultEmail(dataRecord.senderUserId, false);
      }
      senderEmailShort = emls[1];
      senderEmailFull = emls[2];
      if (sender == null)
        sender = new EmailAddressRecord(senderEmailShort);
    }
    return new Object[] { sender, senderEmailShort, senderEmailFull };
  }

  /**
  * @return expanded list of recipients
  */
  public static Record[] getExpandedListOfRecipients(ServerInterfaceLayer SIL, Record[] recipients, boolean expandAddressBooks, boolean expandGroups) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserOps.class, "getExpandedListOfRecipients(ServerInterfaceLayer SIL, Record[] recipients, boolean expandAddressBooks, boolean expandGroups)");
    if (trace != null) trace.args(recipients);
    if (trace != null) trace.args(expandAddressBooks);
    if (trace != null) trace.args(expandGroups);
    if (expandAddressBooks) {
      FolderFilter addressBookFilter = new FolderFilter(FolderRecord.ADDRESS_FOLDER);
      Record[] addressBooks = (Record[]) addressBookFilter.filterInclude(recipients);
      recipients = addressBookFilter.filterExclude(recipients);
      // gather address contacts for the address books selected
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      MsgLinkRecord[] addressContactLinks = cache.getMsgLinkRecordsOwnersAndType(RecordUtils.getIDs(addressBooks), new Short(Record.RECORD_TYPE_FOLDER));
      Record[] addressContactDatas = cache.getMsgDataRecordsForLinks(RecordUtils.getIDs(addressContactLinks));
      // filter out messages leaving address contacts objects
      addressContactDatas = new MsgFilter(MsgDataRecord.OBJ_TYPE_ADDR).filterInclude(addressContactDatas);
      // add address contacts from selected books to the list of recipients
      recipients = RecordUtils.concatinate(recipients, addressContactDatas);
    }
    if (expandGroups) {
      FolderFilter groupFilter = new FolderFilter(FolderRecord.GROUP_FOLDER);
      Record[] groups = (Record[]) groupFilter.filterInclude(recipients);
      recipients = groupFilter.filterExclude(recipients);
      // gather group members for the group folders selected
      Record[] members = UserOps.getOrFetchFamiliarUsers(SIL, groups);
      // add members from selected groups to the list of recipients
      recipients = RecordUtils.concatinate(recipients, members);
    }
    if (trace != null) trace.exit(UserOps.class, recipients);
    return recipients;
  }


  /**
  * Convert any EmailAddressRecords in the array to familiar users or contact objects.
  * Convert any 'reciprocal' contact to our own contact if possible, otherwise to user record.
  * Conversion steps: EmailAddressRecord -> (UserRecord | ContactRecord)
  * @return true if anything was converted.
  */
  public static boolean convertRecipientEmailAndUnknownUsersToFamiliars(ServerInterfaceLayer SIL, Record[] recipients, boolean convertNotHostedEmailsToWebAccounts) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserOps.class, "convertRecipientEmailAndUnknownUsersToFamiliars(ServerInterfaceLayer SIL, Record[] recipients, boolean convertNotHostedEmailsToWebAccounts)");
    if (trace != null) trace.args(recipients);
    if (trace != null) trace.args(convertNotHostedEmailsToWebAccounts);

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();

    boolean anyConverted = false;
    ArrayList unknownEmailsV = new ArrayList();
    ArrayList unknownUserIDsV = new ArrayList();

    if (trace != null) trace.data(10, "gather unknown email addresses");

    // gather unknown email addresses
    for (int i=0; recipients!=null && i<recipients.length; i++) {
      Record rec = recipients[i];

      if (rec instanceof MsgDataRecord) {
        MsgDataRecord mData = (MsgDataRecord) rec;
        if (mData.isTypeAddress()) {
          rec = new EmailAddressRecord(mData.getEmailAddress());
          recipients[i] = rec;
          anyConverted = true;
        }
      } else if (rec instanceof InvEmlRecord) {
        InvEmlRecord invRec = (InvEmlRecord) rec;
        rec = new EmailAddressRecord(invRec.emailAddr);
        recipients[i] = rec;
        anyConverted = true;
      }

      if (rec instanceof EmailAddressRecord) {
        EmailAddressRecord eaRec = (EmailAddressRecord) rec;
        String[] addresses = EmailRecord.gatherAddresses(eaRec.address);
        for (int k=0; addresses!=null && k<addresses.length; k++) {
          String addr = addresses[k];
          // see if we have the email address cached
          // If unknown and numeric address, request for email lookup will return nothing, so we still need the handle lookup request too!
          if (cache.getEmailRecord(addr) == null) {
            if (!unknownEmailsV.contains(addr)) {
              if (trace != null) trace.data(20, addr);
              unknownEmailsV.add(addr);
            }
          }
          // see if numeric, then prepare to fetch user's handle
          try {
            Long uID = Long.valueOf(EmailRecord.getNick(addr));
            if (EmailRecord.isDomainEqual(URLs.getElements(URLs.DOMAIN_MAIL)[0], EmailRecord.getDomain(addr)) && cache.getUserRecord(uID) == null) {
              if (!unknownUserIDsV.contains(uID)) {
                if (trace != null) trace.data(30, uID);
                unknownUserIDsV.add(uID);
              }
            }
          } catch (Exception e) {
          }
        }
      }

      if (rec instanceof ContactRecord) {
        ContactRecord cRec = (ContactRecord) rec;
        if (!cRec.ownerUserId.equals(cache.getMyUserId())) {
          Long otherUserId = cRec.ownerUserId;
          rec = CacheUsrUtils.convertUserIdToFamiliarUser(otherUserId, true, false);
          recipients[i] = rec;
          anyConverted = true;
        }
      }

      if (rec instanceof UserRecord) {
        Long uID = ((UserRecord) rec).userId;
        if (cache.getUserRecord(uID) == null)
          unknownUserIDsV.add(uID);
      }
    }

    if (trace != null) trace.data(20, "unknown email addresses gathered", unknownEmailsV);

    // fetch all unknown email addresses -- this will inturn fetch unknown user handles
    if (unknownEmailsV.size() > 0) {
      if (trace != null) trace.data(40, unknownEmailsV);
      Object[] emls = new Object[unknownEmailsV.size()];
      unknownEmailsV.toArray(emls);
      Object[] set = new Object[] { emls, Boolean.valueOf(convertNotHostedEmailsToWebAccounts) }; // adds new web-account addresses if they don't already exist
      SIL.submitAndWait(new MessageAction(CommandCodes.EML_Q_LOOKUP_ADDR, new Obj_List_Co(set)), 30000);
    }
    // fetch unknown users
    if (unknownUserIDsV.size() > 0) {
      if (trace != null) trace.data(50, unknownUserIDsV);
      SIL.submitAndWait(new MessageAction(CommandCodes.USR_Q_GET_HANDLES, new Obj_IDList_Co(unknownUserIDsV)), 30000);
    }

    if (trace != null) trace.data(60, "convert all EmailAddressRecords to UserRecords or ContactRecords");

    // convert all EmailAddressRecords to UserRecords or ContactRecords
    for (int i=0; recipients!=null && i<recipients.length; i++) {
      Record rec = recipients[i];
      if (rec instanceof EmailAddressRecord) {
        EmailAddressRecord eaRec = (EmailAddressRecord) rec;
        String[] addresses = EmailRecord.gatherAddresses(eaRec.address);
        for (int k=0; addresses!=null && k<addresses.length; k++) {
          String addr = addresses[k];
          EmailRecord eRec = cache.getEmailRecord(addr);

          Long userID = null;
          boolean isEmailHosted = false;
          if (eRec != null) {
            userID = eRec.userId;
            isEmailHosted = eRec.isHosted();
            if (trace != null) trace.data(70, addr);
          } else {
            // see if numeric
            if (trace != null) trace.data(80, addr);
            try {
              Long uID = Long.valueOf(EmailRecord.getNick(addr));
              if (EmailRecord.isDomainEqual(URLs.getElements(URLs.DOMAIN_MAIL)[0], EmailRecord.getDomain(addr))) {
                userID = uID;
                if (trace != null) trace.data(90, addr);
              }
            } catch (Exception e) {
            }
          }

          if (userID != null) {
            boolean includeWebUsers = convertNotHostedEmailsToWebAccounts || isEmailHosted;
            Record familiar = CacheUsrUtils.convertUserIdToFamiliarUser(userID, true, false, includeWebUsers);
            if (trace != null) trace.data(100, familiar);
            if (familiar != null) {
              recipients[i] = familiar;
              anyConverted = true;
              if (trace != null) trace.data(110, "converted to", familiar);
            }
          }
        }
      }
    }

    if (trace != null) trace.exit(UserOps.class, anyConverted);
    return anyConverted;
  }

  public static boolean sendPassRecoverySettings(ServerInterfaceLayer SIL, PassRecoveryRecord passRecoveryRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserOps.class, "sendPasswordRecoverySettings(ServerInterfaceLayer SIL, PassRecoveryRecord passRecoveryRecord)");
    if (trace != null) trace.args(SIL, passRecoveryRecord);

    boolean error = false;

    FetchedDataCache cache = SIL.getFetchedDataCache();
    KeyRecord keyRecord = cache.getKeyRecordMyCurrent();

    // Create proof that we actually have the private key
    BASymmetricKey symKey = new BASymmetricKey(32);
    AsymmetricBlockCipher asyCipher;
    try {
      asyCipher = new AsymmetricBlockCipher();
    } catch (NoSuchAlgorithmException e) {
      if (trace != null) trace.exception(UserOps.class, 100, e);
      throw new IllegalStateException("Could not instantiate an Asymmetric Cipher!");
    }
    BAAsyCipherBlock signed32ByteProof;
    try {
      signed32ByteProof = asyCipher.signBlock(keyRecord.getPrivateKey(), symKey.toByteArray());
    } catch (DigestException e) {
      if (trace != null) trace.exception(UserOps.class, 200, e);
      throw new IllegalStateException("Digest Exception while signing password recovery key proof!");
    }

    Usr_PassRecovery_Co updatePassRecoveryRequest = new Usr_PassRecovery_Co(passRecoveryRecord, signed32ByteProof.toByteArray());
    MessageAction updateKeyAction = new MessageAction(CommandCodes.USR_Q_PASS_RECOVERY_UPDATE, updatePassRecoveryRequest);
    ClientMessageAction msgAction = SIL.submitAndFetchReply(updateKeyAction, 60000);

    if (msgAction == null || msgAction.getActionCode() <= 0) {
      error = true;
    }

    DefaultReplyRunner.nonThreadedRun(SIL, msgAction);

    boolean success = !error;

    if (trace != null) trace.exit(UserOps.class, success);
    return success;
  }

  public static void updateUsedStamp(ServerInterfaceLayer SIL, MemberContactRecordI recipient) {
    if (recipient instanceof Record) {
      updateUsedStamp(SIL, new Record[][] { { (Record) recipient } });
    }
  }
  public static void updateUsedStamp(ServerInterfaceLayer SIL, MemberContactRecordI[] memberRecipients) {
    Record[][] recipients = new Record[1][memberRecipients.length];
    for (int i=0; i<recipients.length; i++) {
      recipients[0][i] = (Record) memberRecipients[i];
    }
    updateUsedStamp(SIL, recipients);
  }
  public static void updateUsedStamp(ServerInterfaceLayer SIL, Record[][] msgRecipients) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserOps.class, "updateUsedStamp(ServerInterfaceLayer SIL, Record[][] msgRecipients)");
    if (trace != null) trace.args(SIL, msgRecipients);

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    if (msgRecipients != null) {
      HashSet contactIDsHS = new HashSet();
      HashSet addressLinkIDsHS = new HashSet();
      HashSet groupShareIDsHS = new HashSet();
      for (int i=0; i<msgRecipients.length; i++) {
        Record[] recipients = msgRecipients[i];
        if (recipients != null) {
          for (int j=0; j<recipients.length; j++) {
            Record recipient = recipients[j];
            if (trace != null) trace.data(10, "processing recipient", recipient.getClass().getSimpleName(), recipient);
            if (recipient instanceof ContactRecord) {
              // contact
              contactIDsHS.add(recipient.getId());
            } else if (recipient instanceof MsgLinkRecord) {
              // address book entry
              System.out.println("updateUsedStamp for Addres via Link");
              addressLinkIDsHS.add(recipient.getId());
            } else if (recipient instanceof MsgDataRecord) {
              // address book entry
              System.out.println("updateUsedStamp for Addres via Data");
              MsgLinkRecord[] addrLinks = cache.getMsgLinkRecordsForMsg(recipient.getId());
              if (addrLinks != null && addrLinks.length > 0) {
                for (int k=0; k<addrLinks.length; k++)
                  addressLinkIDsHS.add(addrLinks[k].msgLinkId);
              }
            } else if (recipient instanceof EmailAddressRecord) {
              // email address - match with address books
              if (trace != null) trace.data(20, "email address needs to be converted to Address Book entry");
              EmailAddressRecord emlAddr = (EmailAddressRecord) recipient;
              MsgDataRecord[] addrs = cache.getAddrRecords(emlAddr.address);
              if (addrs != null && addrs.length > 0) {
                if (trace != null) trace.data(21, "matching Address Book entry", addrs);
                Long[] addrIDs = RecordUtils.getIDs(addrs);
                MsgLinkRecord[] addrLinks = cache.getMsgLinkRecordsForMsgs(addrIDs);
                if (addrLinks != null && addrLinks.length > 0) {
                  for (int u=0; u<addrLinks.length; u++) {
                    addressLinkIDsHS.add(addrLinks[u].msgLinkId);
                    if (trace != null) trace.data(22, "Address Book entry link", addrLinks[u].msgLinkId);
                  }
                }
              }
            } else if (recipient instanceof FolderPair) {
              // mail/posting folder, or group
              FolderPair pair = (FolderPair) recipient;
              FolderRecord folder = pair.getFolderRecord();
              FolderShareRecord share = pair.getFolderShareRecord();
              groupShareIDsHS.add(share.shareId);
              if (folder.isGroupType()) {
                if (trace != null) trace.data(30, "Group Id", recipient.getId());
                if (trace != null) trace.data(31, "Group Share", share);
              } else {
                if (trace != null) trace.data(40, "Folder Id", recipient.getId());
                if (trace != null) trace.data(41, "Folder Share", share);
              }
            }
          }
        }
      }
      if (contactIDsHS.size() > 0 || addressLinkIDsHS.size() > 0 || groupShareIDsHS.size() > 0) {
        Long[] contactIDs = (Long[]) ArrayUtils.toArray(contactIDsHS, Long.class);
        Long[] addressLinkIDs = (Long[]) ArrayUtils.toArray(addressLinkIDsHS, Long.class);
        Long[] groupShareIDs = (Long[]) ArrayUtils.toArray(groupShareIDsHS, Long.class);
        SIL.submitAndReturn(new MessageAction(CommandCodes.CNT_Q_UPDATE_USED, new Obj_List_Co(new Object[] { contactIDs, addressLinkIDs, groupShareIDs })));
      }
    }

    if (trace != null) trace.exit(UserOps.class);
  }

  public static void updateUserSettingsSpellingProperties(ServerInterfaceLayer SIL, Properties properties) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    UserSettingsRecord usrSettingsRec = cache.getMyUserSettingsRecord();
    if (usrSettingsRec == null) {
      usrSettingsRec = new UserSettingsRecord();
      usrSettingsRec.setSymKey(new BASymmetricKey(32));
    }
    usrSettingsRec.spellingProps = properties;
    usrSettingsRec.setXmlText(usrSettingsRec.makeXMLData());
    usrSettingsRec.seal(cache.getKeyRecordMyCurrent());
    Usr_AltUsrData_Rq request = new Usr_AltUsrData_Rq();
    request.userSettingsRecord = usrSettingsRec;
    SIL.submitAndReturn(new MessageAction(CommandCodes.USR_Q_ALTER_DATA, request));
  }

}
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

package com.CH_cl.service.ops;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.actions.usr.UsrALoginSecureSession;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.records.EmailAddressRecord;
import com.CH_cl.util.GlobalSubProperties;
import com.CH_cl.util.PopupWindow;

import com.CH_co.cryptx.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.msg.dataSets.usr.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import java.awt.Component;
import java.io.File;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
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
 * <b>$Revision: 1.34 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class UserOps extends Object {


  public static void fetchUnknownUsers(ServerInterfaceLayer SIL, StatRecord[] statRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserOps.class, "fetchUnknownUsers(ServerInterfaceLayer SIL, StatRecord[] statRecords)");
    if (trace != null) trace.args(SIL, statRecords);

    // Gather all userIDs for which we don't have user handles and fetch them.
    Vector userIDsV = null;
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    for (int i=0; i<statRecords.length; i++) {
      StatRecord stat = statRecords[i];
      if (cache.getUserRecord(stat.ownerUserId) == null) {
        if (userIDsV == null) userIDsV = new Vector();
        if (!userIDsV.contains(stat.ownerUserId)) {
          userIDsV.addElement(stat.ownerUserId);
        }
      }
    }
    if (userIDsV != null && userIDsV.size() > 0) {
      Long[] userIDs = (Long[]) ArrayUtils.toArray(userIDsV, Long.class);
      SIL.submitAndWait(new MessageAction(CommandCodes.USR_Q_GET_HANDLES, new Obj_IDList_Co(userIDs)), 30000);
    }

    if (trace != null) trace.exit(UserOps.class);
  }

  public static void fetchUnknownUsers(ServerInterfaceLayer SIL, EmailRecord[] emailRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserOps.class, "fetchUnknownUsers(ServerInterfaceLayer SIL, EmailRecord[] emailRecords)");
    if (trace != null) trace.args(SIL, emailRecords);

    // Gather all userIDs for which we don't have user handles and fetch them.
    Vector userIDsV = null;
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    for (int i=0; i<emailRecords.length; i++) {
      EmailRecord email = emailRecords[i];
      if (email.userId != null && cache.getUserRecord(email.userId) == null) {
        if (userIDsV == null) userIDsV = new Vector();
        if (!userIDsV.contains(email.userId)) {
          userIDsV.addElement(email.userId);
        }
      }
      if (email.creatorId != null && cache.getUserRecord(email.creatorId) == null) {
        if (userIDsV == null) userIDsV = new Vector();
        if (!userIDsV.contains(email.creatorId)) {
          userIDsV.addElement(email.creatorId);
        }
      }
    }
    if (userIDsV != null && userIDsV.size() > 0) {
      Long[] userIDs = (Long[]) ArrayUtils.toArray(userIDsV, Long.class);
      SIL.submitAndWait(new MessageAction(CommandCodes.USR_Q_GET_HANDLES, new Obj_IDList_Co(userIDs)), 30000);
    }

    if (trace != null) trace.exit(UserOps.class);
  }

  public static boolean sendPasswordChange(ServerInterfaceLayer SIL, BAEncodedPassword ba, boolean storeKeyOnServer, File privateKeyFile, StringBuffer errorBuffer) {
    return sendPasswordChange(SIL, null, ba, storeKeyOnServer, null, privateKeyFile, errorBuffer);
  }
  public static boolean sendPasswordChange(ServerInterfaceLayer SIL, String newUserName, BAEncodedPassword ba, boolean storeKeyOnServer, StringBuffer errorBuffer) {
    return sendPasswordChange(SIL, newUserName, ba, storeKeyOnServer, null, null, errorBuffer);
  }
  private static boolean sendPasswordChange(ServerInterfaceLayer SIL, String newUserName, BAEncodedPassword ba, boolean storeKeyOnServer, Integer actionCode, File privateKeyFile, StringBuffer errorBuffer) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserOps.class, "sendPasswordChange(ServerInterfaceLayer SIL, String newUserName, BAEncodedPassword ba, boolean storeKeyOnServer, Integer actionCode, File privateKeyFile, StringBuffer errorBuffer)");
    if (trace != null) trace.args(SIL, newUserName, ba);
    if (trace != null) trace.args(storeKeyOnServer);
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(privateKeyFile);
    if (trace != null) trace.args(errorBuffer);

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
    GlobalSubProperties keyProperties = new GlobalSubProperties(privateKeyFile, GlobalSubProperties.PROPERTY_EXTENSION_KEYS);
    String oldProperty = keyProperties.getProperty(keyPropertyName);
    boolean wasLocalKey = (oldProperty != null && oldProperty.length() > 0);
    boolean storedLocally = false;

    // If key is to be stored locally, do it first before removing it from remote storage.
    if (storeKeyOnLocal) {
      try {
        keyProperties.setProperty(keyPropertyName, keyRecord.getEncPrivateKey().getHexContent());
        keyProperties.store();
        UsrALoginSecureSession.addPathToLastPrivKeyPaths(keyProperties.getPropertiesFullFileName());
        storedLocally = true;
      } catch (Throwable t) {
        if (errorBuffer != null) errorBuffer.append("\n\n"+t.getLocalizedMessage());
        error = true;
      }
    }

    if (!error) {
      Usr_AltUsrPass_Rq updateKeyRequest = createAltUserPassRequest(SIL, newUserName, ba, storeKeyOnServer);
      MessageAction updateKeyAction = new MessageAction(actionCode.intValue(), updateKeyRequest);
      ClientMessageAction msgAction = SIL.submitAndFetchReply(updateKeyAction, 60000);
      if (msgAction == null || msgAction.getActionCode() <= 0) {
        if (errorBuffer != null) errorBuffer.append("\n\nserver failed to respond");
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
      keyProperties.store();
    } else if (error) {
      if (storeKeyOnServer && wasLocalKey) {
        // storage on server failed, just leave the local keys as they are
      } else if (storeKeyOnLocal && wasLocalKey) {
        // password change on local keys failed, restore local key storage
        keyProperties.setProperty(keyPropertyName, oldProperty);
        keyProperties.store();
      } else if (storeKeyOnLocal && !wasLocalKey && storedLocally) {
        // delete the key from local storage...
        keyProperties.remove(keyPropertyName);
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


  public static void checkExpiry() {
    UserRecord uRec = FetchedDataCache.getSingleInstance().getUserRecord();
    if (uRec != null) {
      long dateExpired = uRec.dateExpired.getTime();
      long now = System.currentTimeMillis();
      // show message "due for renewal" or "expired"
      long fullDay = 24L * 60L * 60L * 1000L;

      long balance = dateExpired - now;
      int balanceDays = (int) (balance/fullDay);

      String error = null;
      String warning = null;

      String dateExpiredS = new SimpleDateFormat("MMM dd, yyyy").format(uRec.dateExpired);
      String url = URLs.get(URLs.SIGNUP_PAGE) +"?UserID="+ FetchedDataCache.getSingleInstance().getMyUserId();

      if (balanceDays <= 3 && balanceDays >= 1) {
        // Don't bother people as most are setup on auto-renewal anyway and they would think that they need to call for support when they don't
        //warning = "<html>Your account is due for renewal on "+dateExpiredS+".</html>";
      } else if (balanceDays == 0) {
        // Don't bother people as most are setup on auto-renewal anyway and they would think that they need to call for support when they don't
        //warning = "<html>Your account is due for renewal today.";
      } else if (balanceDays <= -3 && balanceDays >= -7) {
        // already expired but withing grace period 7 days...
        error = "<html>Your account is past due for renewal.  Account expired on "+dateExpiredS+".  ";
        if (uRec.isBusinessSubAccount())
          error += "To renew your account please contact your administrator.";
        else
          error += "<a href="+url+">Click here</a> to renew your account now.";
        error += "</html>";
      } else if (balanceDays < -7) {
        if (balanceDays < -7 && balanceDays >= -15) {
          error = "<html>Your account is expired since "+dateExpiredS+".  ";
        } else if (balanceDays < -15) {
          error = "<html>Your account is expired since "+dateExpiredS+" and will soon be deleted.  ";
        }
        if (uRec.isBusinessSubAccount())
          error += "To renew your account please contact your administrator.";
        else
          error += "<a href="+url+">Click here</a> to renew your account now.";
        error += "</html>";
      }

      // don't bother sub-accounts with warinig messages
      if (warning != null && !uRec.isBusinessSubAccount()) {
        PopupWindow.getSingleInstance().addForScrolling(new HTML_ClickablePane(warning));
      }
      if (error != null) {
        PopupWindow.getSingleInstance().addForScrolling(new HTML_ClickablePane(error));
      }
    }
  }

  public static void checkQuotas() {
    UserRecord uRec = FetchedDataCache.getSingleInstance().getUserRecord();
    if (uRec != null) {
      long transferLimit = uRec.transferLimit.longValue();
      long transferUsed = uRec.transferUsed != null ? uRec.transferUsed.longValue() : 0;

      String msg = "";
      boolean error = false;
      boolean warning = false;
      if (uRec.isStorageLimitExceeded()) {
        msg = "Your server storage space limit is exceeded! ";
        error = true;
      } else if (uRec.isStorageAboveWarning()) {
        long storageLimit = uRec.storageLimit.longValue();
        long storageUsed = uRec.storageUsed != null ? uRec.storageUsed.longValue() : 0;
        msg = "Your server storage space usage is within " + Misc.getFormattedSize(storageLimit-storageUsed, 4, 3) + " of the set limit. ";
        warning = true;
      }

      if (transferLimit != UserRecord.UNLIMITED_AMOUNT && transferLimit < transferUsed) {
        msg += "Your transfer bandwidth limit is exceeded! ";
        error = true;
      }
      else if (transferLimit != UserRecord.UNLIMITED_AMOUNT && (transferLimit < transferUsed+(2*1024*1024) || transferLimit < transferUsed+(transferLimit*0.1))) {
        msg += "Your transfer bandwidth usage is within " + Misc.getFormattedSize(transferLimit-transferUsed, 4, 3) + " of the set limit. ";
        warning = true;
      }

      String urlStr = "\""+URLs.get(URLs.SIGNUP_PAGE)+"?UserID=" + uRec.userId + "\"";
      if (error || warning) {
        msg = "<html>" + msg + "For uninterrupted service, please visit <a href="+urlStr+">"+URLs.get(URLs.SIGNUP_PAGE)+"</a> to upgrade your account. <p>Thank You.";
        String title = "Account Usage Notice";
        if (error) {
          if (uRec.isBusinessSubAccount()) {
            String htmlText = "<html>Your account limits are exceeded, please contact your administrator to assign you larger quotas.</html>";
            PopupWindow.getSingleInstance().addForScrolling(new HTML_ClickablePane(htmlText));
          } else {
            String htmlText = "<html>Your account limits are exceeded, <a href="+urlStr+">click here</a> to upgrade.</html>";
            PopupWindow.getSingleInstance().addForScrolling(new HTML_ClickablePane(htmlText));
          }
        }
        else {
          if (uRec.isBusinessSubAccount()) {
            String htmlText = "<html>Your account is near capacity, please contact your administrator to assign you larger quotas.</html>";
            PopupWindow.getSingleInstance().addForScrolling(new HTML_ClickablePane(htmlText));
          } else {
            String htmlText = "<html>Your account is near capacity, <a href="+urlStr+">click here</a> to upgrade.</html>";
            PopupWindow.getSingleInstance().addForScrolling(new HTML_ClickablePane(htmlText));
          }
        }
      }
    }
  } // end checkQuotas()

  /**
   * @return personal (nullable), short, full parts of default email address for specified user
   */
  public static String[] getCachedDefaultEmail(UserRecord userRecord, boolean isGeneratePersonalPart) {
    String[] emailAddr = null;
    if (userRecord.defaultEmlId != null && userRecord.defaultEmlId.longValue() != UserRecord.GENERIC_EMAIL_ID) {
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      EmailRecord emlRec = cache.getEmailRecord(userRecord.defaultEmlId);
      if (emlRec != null) {
        emailAddr = new String[3];
        String personal = emlRec.personal;
        if (isGeneratePersonalPart) {
          personal = personal != null ? personal : userRecord.handle;
          emailAddr[2] = personal + " <" + emlRec.emailAddr + ">";
        } else {
          emailAddr[2] = emlRec.getEmailAddressFull();
        }
        emailAddr[0] = personal;
        emailAddr[1] = emlRec.emailAddr;
      }
    }
    return emailAddr;
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
   * TO-DO: Legacy function because default email addresses are always fetched with User Handles... so remove this stuff...
   * @return personal (nullable), short, full parts of default email address for specified user
   */
  public static String[] getOrFetchDefaultEmail(ServerInterfaceLayer SIL, UserRecord userRecord, boolean isGeneratePersonalPart) {
    String[] emailAddr = getCachedDefaultEmail(userRecord, isGeneratePersonalPart);
    if (emailAddr == null) {
      if (userRecord.defaultEmlId != null && userRecord.defaultEmlId.longValue() != UserRecord.GENERIC_EMAIL_ID) {
        SIL.submitAndWait(new MessageAction(CommandCodes.USR_Q_GET_HANDLES, new Obj_IDList_Co(userRecord.userId)), 30000);
      }
      emailAddr = getCachedDefaultEmail(userRecord, isGeneratePersonalPart);
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
        emls = UserOps.getCachedDefaultEmail((UserRecord) sender, true);
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

  public static boolean isShowWebAccountRestrictionDialog(Component parent) {
    boolean isShow = false;
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    UserRecord myUserRec = cache.getUserRecord();
    if (myUserRec != null && myUserRec.isWebAccount()) {
      String urlStr = "\""+URLs.get(URLs.SIGNUP_PAGE)+"?UserID=" + myUserRec.userId + "\"";
      String htmlText = "<html>This functionality is not available for complementary web accounts.  To upgrade your user account to a Premium Account click here <a href="+urlStr+">"+URLs.get(URLs.SIGNUP_PAGE)+"</a>. <p>Thank You.</html>";
      MessageDialog.showWarningDialog(parent, htmlText, "Account Incapable", false);
      isShow = true;
    }
    return isShow;
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
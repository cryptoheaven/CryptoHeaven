/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.ops;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_co.service.records.UserRecord;
import com.CH_co.util.Misc;
import com.CH_co.util.URLs;
import java.text.SimpleDateFormat;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class SysOps {

  public static void checkExpiry(final FetchedDataCache cache) {
    UserRecord uRec = cache.getUserRecord();
    if (uRec != null) {
      // we are not enforcing expiry date on free accounts
      if (uRec.isPersonalAccount() || uRec.isBusinessAccount()) {
        long dateExpired = uRec.dateExpired.getTime();
        long now = System.currentTimeMillis();
        // show message "due for renewal" or "expired"
        long fullDay = 24L * 60L * 60L * 1000L;

        long balance = dateExpired - now;
        int balanceDays = (int) (balance/fullDay);

        String error = null;
        String warning = null;

        String dateExpiredS = new SimpleDateFormat("MMM dd, yyyy").format(uRec.dateExpired);
        String url = URLs.get(URLs.SIGNUP_PAGE) +"?UserID="+ cache.getMyUserId();

        if (balanceDays <= 3 && balanceDays >= 1) {
          // Don't bother people as most are setup on auto-renewal anyway and they would think that they need to call for support when they don't
          //warning = "<html>Your account is due for renewal on "+dateExpiredS+".</html>";
        } else if (balanceDays == 0) {
          // Don't bother people as most are setup on auto-renewal anyway and they would think that they need to call for support when they don't
          //warning = "<html>Your account is due for renewal today.";
        } else if (balanceDays <= -3 && balanceDays >= -7) {
          // already expired but withing grace period 7 days...
          error = "<html><font size='-1'>Your account is past due for renewal.  Account expired on "+dateExpiredS+".  ";
          if (uRec.isBusinessSubAccount())
            error += "To renew your account please contact your administrator.";
          else
            error += "<a href="+url+">Click here</a> to renew your account now.";
          error += "</font></html>";
        } else if (balanceDays < -7) {
          if (balanceDays < -7 && balanceDays >= -15) {
            error = "<html><font size='-1'>Your account is expired since "+dateExpiredS+".  ";
          } else if (balanceDays < -15) {
            error = "<html><font size='-1'>Your account is expired since "+dateExpiredS+" and will soon be deleted.  ";
          }
          if (uRec.isBusinessSubAccount())
            error += "To renew your account please contact your administrator.";
          else
            error += "<a href="+url+">Click here</a> to renew your account now.";
          error += "</font></html>";
        }

        // don't bother sub-accounts with warinig messages
        if (warning != null && !uRec.isBusinessSubAccount()) {
          cache.fireMsgPopupEvent(warning);
        }
        if (error != null) {
          cache.fireMsgPopupEvent(error);
        }
      }
    }
  }

  public static void checkQuotas(final FetchedDataCache cache) {
    checkQuotas(cache, null, null, null);
  }
  public static void checkQuotas(final FetchedDataCache cache, Long storageUsedF, Long transferUsedF, Short subAccountsUsedF) {
    UserRecord uRec = cache.getUserRecord();
    if (uRec != null) {
      long transferLimit = uRec.transferLimit.longValue();
      long transferUsed = transferUsedF != null ? transferUsedF.longValue() : (uRec.transferUsed != null ? uRec.transferUsed.longValue() : 0);

      String msg = "";
      boolean error = false;
      boolean warning = false;
      if (uRec.isStorageLimitExceeded()) {
        msg = "Your server storage space limit is exceeded! ";
        error = true;
      } else if (uRec.isStorageAboveWarning()) {
        long storageLimit = uRec.storageLimit.longValue();
        long storageUsed = storageUsedF != null ? storageUsedF.longValue() : (uRec.storageUsed != null ? uRec.storageUsed.longValue() : 0);
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

      if (uRec.isBusinessMasterAccount() && subAccountsUsedF != null && uRec.maxSubAccounts.shortValue() < subAccountsUsedF.shortValue()) {
        msg += "Your account is licensed to manage "+uRec.maxSubAccounts+" sub-accounts and you are managing "+subAccountsUsedF+"! ";
        error = true;
      }

      String urlStr = "\""+URLs.get(URLs.SIGNUP_PAGE)+"?UserID=" + uRec.userId + "\"";
      if (error || warning) {
        msg = "<html>" + msg + "For uninterrupted service, please visit <a href="+urlStr+">"+URLs.get(URLs.SIGNUP_PAGE)+"</a> to upgrade your account. <p>Thank You.";
        String title = "Account Usage Notice";
        if (error) {
          if (uRec.isBusinessSubAccount()) {
            String htmlText = "<html><font size='-1'>Your account limits are exceeded, please contact your administrator to assign you larger quotas.</font></html>";
            cache.fireMsgPopupEvent(htmlText);
          } else {
            String htmlText = "<html><font size='-1'>Your account limits are exceeded, <a href="+urlStr+">click here</a> to upgrade.</font></html>";
            cache.fireMsgPopupEvent(htmlText);
          }
        }
        else {
          if (uRec.isBusinessSubAccount()) {
            String htmlText = "<html><font size='-1'>Your account is near capacity, please contact your administrator to assign you larger quotas.</font></html>";
            cache.fireMsgPopupEvent(htmlText);
          } else {
            String htmlText = "<html><font size='-1'>Your account is near capacity, <a href="+urlStr+">click here</a> to upgrade.</font></html>";
            cache.fireMsgPopupEvent(htmlText);
          }
        }
      }
    }
  } // end checkQuotas()

}
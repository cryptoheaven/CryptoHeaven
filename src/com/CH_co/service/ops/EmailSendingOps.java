/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_co.service.ops;

import com.CH_co.service.records.EmailRecord;
import com.CH_co.util.URLs;

/**
 *
 * @author Marcin
 */
public class EmailSendingOps {

  public static String getEmailBannerDivider(String contentType) {
    String bannerDivider = "";
    if (contentType != null) {
      if (contentType.equalsIgnoreCase("text/plain")) {
        bannerDivider = "\n\n---";
      } else if (contentType.equalsIgnoreCase("text/html")) {
        bannerDivider = "\n<p></p>\n---";
      }
    }
    return bannerDivider;
  }

  public static String getSecureReplyBanner(Long userId, String messageSubject, String contentType, String toEmailAddress, String replyPageURL) {
    String secureReplyBanner = "";
    if (contentType != null) {
      String subjectENC = "subject=" + java.net.URLEncoder.encode("Re: " + messageSubject);
      String replyPage = replyPageURL != null ? replyPageURL : URLs.get(URLs.REPLY_PAGE);
      if (!replyPage.endsWith("?uId="))
        replyPage += "?uId=";
      replyPage += userId;
      String replyURL = replyPage+"&"+subjectENC;
      if (toEmailAddress != null && toEmailAddress.length() > 0) {
        String name = EmailRecord.getPersonalOrNick(toEmailAddress);
        if (name != null && name.length() > 0)
          replyURL += "&fromName=" + java.net.URLEncoder.encode(name);
        String[] addrs = EmailRecord.gatherAddresses(toEmailAddress);
        if (addrs != null && addrs.length > 0)
          replyURL += "&fromEmail=" + java.net.URLEncoder.encode(addrs[addrs.length-1]);
      }
      if (contentType.equalsIgnoreCase("text/plain")) {
        secureReplyBanner = "\nSecure Reply (option enabled):\n"+replyURL;
      } else if (contentType.equalsIgnoreCase("text/html")) {
        secureReplyBanner = "\n<br>Secure Reply (option enabled):<br><a href=\""+replyURL+"\">"+replyPage+"</a>";
      }
    }
    return secureReplyBanner;
  }

}
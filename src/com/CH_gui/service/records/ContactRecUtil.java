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

package com.CH_gui.service.records;

import com.CH_gui.util.Images;

import com.CH_cl.service.cache.*;

import com.CH_co.service.records.*;
import com.CH_co.util.*;

import javax.swing.*;

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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class ContactRecUtil extends Object {

  public static ImageIcon getStatusIcon(Short status, Long ownerUserId) {
    return Images.get(getStatusIconCode(status, ownerUserId));
  }
  public static int getStatusIconCode(Short status, Long ownerUserId) {
    int iconCode = -1;
    short s = status.shortValue();
    switch (s) {
      case ContactRecord.STATUS_INITIATED :
        if (ownerUserId.equals(FetchedDataCache.getSingleInstance().getMyUserId())) {
          iconCode = ImageNums.STATUS_WAITING16;
        }
        else {
          iconCode = ImageNums.STATUS_QUESTION16;
        }
        break;
      case ContactRecord.STATUS_ACCEPTED :
        iconCode = ImageNums.STATUS_OFFLINE16;
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED :
        iconCode = ImageNums.STATUS_OFFLINE16;
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE :
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AVAILABLE :
        iconCode = ImageNums.STATUS_ONLINE16;
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_INACTIVE :
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AWAY :
        iconCode = ImageNums.STATUS_AWAY16;
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_NA :
        iconCode = ImageNums.STATUS_NA16;
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_DND :
        iconCode = ImageNums.STATUS_DND16;
        break;
      case ContactRecord.STATUS_DECLINED :
        iconCode = ImageNums.DELETE16;
        break;
      case ContactRecord.STATUS_DECLINED_ACKNOWLEDGED :
        iconCode = ImageNums.DELETE16;
        break;
      default :
        iconCode = ImageNums.PRIORITY_HIGH_SMALL;
    }
    return iconCode;
  }

  public static String getStatusText(Short status, Long ownerUserId) {
    String statusText = "-";
    short s = status.shortValue();
    switch (s) {
      case ContactRecord.STATUS_INITIATED :
        statusText = "Initiated";
        if (ownerUserId.equals(FetchedDataCache.getSingleInstance().getMyUserId())) {
          statusText += " (Awaiting Authorization)";
        }
        else {
          statusText += " (Requires Your Action)";
        }
        break;
      case ContactRecord.STATUS_ACCEPTED :
        statusText = "Offline; Accepted";
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED :
        statusText = "Offline; Accepted and Acknowledged";
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE :
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AVAILABLE :
        statusText = "Online; Accepted and Acknowledged";
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_INACTIVE :
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AWAY :
        statusText = "Online; Away from keyboard";
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_NA :
        statusText = "Online; Not Available";
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_DND :
        statusText = "Online; Do Not Disturb";
        break;
      case ContactRecord.STATUS_DECLINED :
        statusText = "Declined";
        break;
      case ContactRecord.STATUS_DECLINED_ACKNOWLEDGED :
        statusText = "Declined and Acknowledged";
        break;
      default :
        statusText = "unknown";
    }
    return statusText;
  }

}
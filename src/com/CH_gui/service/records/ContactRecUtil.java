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

package com.CH_gui.service.records;

import com.CH_gui.util.Images;

import com.CH_cl.service.cache.*;

import com.CH_co.service.records.*;
import com.CH_co.util.*;

import javax.swing.*;

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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class ContactRecUtil extends Object {

  public static ImageIcon getStatusIcon(Short status, Long ownerUserId) {
    ImageIcon icon = null;
    short s = status.shortValue();
    switch (s) {
      case ContactRecord.STATUS_INITIATED :
        if (ownerUserId.equals(FetchedDataCache.getSingleInstance().getMyUserId())) {
          icon = Images.get(ImageNums.STATUS_WAITING16);
        }
        else {
          icon = Images.get(ImageNums.STATUS_QUESTION16);
        }
        break;
      case ContactRecord.STATUS_ACCEPTED :
        icon = Images.get(ImageNums.STATUS_OFFLINE16);
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED :
        icon = Images.get(ImageNums.STATUS_OFFLINE16);
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE :
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AVAILABLE :
        icon = Images.get(ImageNums.STATUS_ONLINE16);
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_INACTIVE :
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AWAY :
        icon = Images.get(ImageNums.STATUS_AWAY16);
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_NA :
        icon = Images.get(ImageNums.STATUS_NA16);
        break;
      case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_DND :
        icon = Images.get(ImageNums.STATUS_DND16);
        break;
      case ContactRecord.STATUS_DECLINED :
        icon = Images.get(ImageNums.DELETE16);
        break;
      case ContactRecord.STATUS_DECLINED_ACKNOWLEDGED :
        icon = Images.get(ImageNums.DELETE16);
        break;
      default :
        icon = Images.get(ImageNums.PRIORITY_HIGH_SMALL);
    }
    return icon;
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
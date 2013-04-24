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

package com.CH_gui.usrs;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_co.service.records.UserRecord;
import com.CH_co.util.URLs;
import com.CH_gui.util.MessageDialog;

import java.awt.Component;

/**
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class UserGuiOps {

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

}
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

package com.CH_cl.service.actions.sys;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.ops.*;

import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.dataSets.sys.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import java.util.Date;

/**
 * <b>Copyright</b> &copy; 2001-2011
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
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class SysAGetAutoUpdate extends ClientMessageAction {

  private static String PROPERTY_SOFTWARE_UPDATE_MSG_STAMP_MILLIS = "SOFTWARE_UPDATE_MSG_STAMP_MILLIS";

  /** Creates new SysAGetAutoUpdate */
  public SysAGetAutoUpdate() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysAGetAutoUpdate.class, "SysAGetAutoUpdate()");
    if (trace != null) trace.exit(SysAGetAutoUpdate.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysAGetAutoUpdate.class, "runAction(Connection)");

    Sys_AutoUpdate_Co set = (Sys_AutoUpdate_Co) getMsgDataSet();
    if (set != null && set.updateRecords != null && set.updateRecords.length > 0) {
      FetchedDataCache cache = getFetchedDataCache();
      UserRecord myUser = cache.getUserRecord();
      if (!Misc.isAllGUIsuppressed()) {
        if (AutoUpdater.isLongInactive()) {
          if (myUser != null && myUser.flags != null &&
              !Misc.isBitSet(myUser.flags, UserRecord.FLAG_DISABLE_AUTO_UPDATES) &&
              AutoUpdater.isRunningFromJar()) {
            new AutoUpdater(getServerInterfaceLayer(), set.updateRecords).start();
          } else {
            // check if any of the updates have higher build number before we popup a message, ignore different file size updates as this maybe too minor of a change
            AutoUpdateRecord[] updateRecords = set.updateRecords;
            boolean isSignificant = false;
            for (int i=0; i < updateRecords.length; i++) {
              if (updateRecords[i].build != null && updateRecords[i].build.shortValue() > GlobalProperties.PROGRAM_BUILD_NUMBER) {
                isSignificant = true;
                break;
              }
            }
            if (isSignificant) {
              String lastStamp = GlobalProperties.getProperty(PROPERTY_SOFTWARE_UPDATE_MSG_STAMP_MILLIS, "0");
              long now = new Date().getTime();
              long lastStampL = 0;
              try { lastStampL = Long.parseLong(lastStamp); } catch (Throwable t) { }
              if (lastStampL > now || lastStampL < now-1000L*60L*60L*24L*3L) {// - 3 days;
                String hrefStart = "<a href=\""+URLs.get(URLs.HOME_PAGE)+"\">";
                String hrefEnd = "</a>";
                String msg = "<html><small>Software update is available for download free of charge at " + hrefStart + URLs.get(URLs.HOME_PAGE) + hrefEnd + " on the products download page.</small></html>";
                cache.fireMsgPopupEvent(msg);
                GlobalProperties.setProperty(PROPERTY_SOFTWARE_UPDATE_MSG_STAMP_MILLIS, ""+now);
              }
            }
          }
        }
      }
    }

    if (trace != null) trace.exit(SysAGetAutoUpdate.class, null);
    return null;
  }

}
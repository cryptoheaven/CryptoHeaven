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

package com.CH_gui.statTable;

import java.util.*;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.ops.*;
import com.CH_cl.service.records.filters.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.stat.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.*;

import com.CH_gui.frame.*;
import com.CH_gui.list.*;
import com.CH_gui.table.*;

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
 * <b>$Revision: 1.20 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class StatTableModel extends RecordTableModel {

  // Parent is either MsgLinkRecord or FileLinkRecord
  private Record parentObjLink;

  private static String STR_USER = com.CH_gui.lang.Lang.rb.getString("column_User");
  private static String STR_FIRST_SEEN = com.CH_gui.lang.Lang.rb.getString("column_First_Seen");
  private static String STR_CONTENT_RETRIEVED = com.CH_gui.lang.Lang.rb.getString("column_Content_Retrieved");

  private static final ColumnHeaderData columnHeaderData = 
      new ColumnHeaderData(new Object[][]
        { { STR_USER, STR_FIRST_SEEN, STR_CONTENT_RETRIEVED },
          { STR_USER, STR_FIRST_SEEN, STR_CONTENT_RETRIEVED },
          { null, null, null },
          { null, null, null },
          { Integer.valueOf(249), Integer.valueOf(160), Integer.valueOf(160) },
          { Integer.valueOf(249), Integer.valueOf(160), Integer.valueOf(160) },
          { Integer.valueOf(249), Integer.valueOf(160), Integer.valueOf(160) },
          { Integer.valueOf(  0), Integer.valueOf(  0), Integer.valueOf(  0) },
          { Integer.valueOf( 90), Integer.valueOf(160), Integer.valueOf(160) },
          { Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2) },
          { Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2) },
          { Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2) },
          { Integer.valueOf(0) }
        });

  /** Creates new StatTableModel */
  public StatTableModel(Record parentObjLink) {
    super(columnHeaderData);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StatTableModel.class, "StatTableModel(Record parentObjLink)");
    if (trace != null) trace.args(parentObjLink);
    this.parentObjLink = parentObjLink;

    Long objId = null;
    if (parentObjLink instanceof MsgLinkRecord)
      objId = ((MsgLinkRecord)parentObjLink).msgId;
    else if (parentObjLink instanceof FileLinkRecord)
      objId = ((FileLinkRecord)parentObjLink).fileId;

    setFilter(new StatFilter(objId));
    refreshData();
    if (trace != null) trace.exit(StatTableModel.class);
  }

  /**
   * When folders are fetched, their IDs are cached so that we know if table fetch is required when
   * user switches focus to another folder...
   * This vector should also be cleared when users are switched...
   */
  public Vector getCachedFetchedFolderIDs() {
    return null;
  }

  /**
   * Sets auto update mode by listening on the cache stat updates.
   */
  public void setAutoUpdate(boolean flag) {
  }

  public Object getValueAtRawColumn(Record record, int column, boolean forSortOnly) {
    Object value = null;

    if (record instanceof StatRecord) {
      StatRecord statRecord = (StatRecord) record;

      FetchedDataCache cache = null;
      UserRecord uRec = null;

      if (column == 0) {
        cache = FetchedDataCache.getSingleInstance();
        uRec = cache.getUserRecord(statRecord.ownerUserId);
      }

      switch (column) {
        case 0: value = ListRenderer.getRenderedText(uRec);
          break;
        case 1: value = statRecord.firstSeen;
          break;
        case 2: value = statRecord.firstDelivered;
          break;
      }
    }

    return value;
  }

  public RecordTableCellRenderer createRenderer() {
    return new StatTableCellRenderer();
  }

  public synchronized void initData(Long parentObjLinkId) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    parentObjLink = cache.getFileLinkRecord(parentObjLinkId);
    if (parentObjLink == null)
      parentObjLink = cache.getMsgLinkRecord(parentObjLinkId);
    refreshData();
  }

  public synchronized void refreshData() {
    Thread th = new ThreadTraced("Stat Refresher") {
      public void runTraced() {
        removeData();
        Stats_Get_Rq request = new Stats_Get_Rq();
        if (parentObjLink instanceof MsgLinkRecord)
          request.statsForObjType = Short.valueOf(Record.RECORD_TYPE_MSG_LINK);
        else if (parentObjLink instanceof FileLinkRecord)
          request.statsForObjType = Short.valueOf(Record.RECORD_TYPE_FILE_LINK);
        request.ownerObjType = Short.valueOf(Record.RECORD_TYPE_SHARE);
        request.objLinkIDs = new Long[] { parentObjLink.getId() };

        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        Long objId = null;
        if (parentObjLink instanceof MsgLinkRecord)
          objId = cache.getFolderShareRecordMy(((MsgLinkRecord) parentObjLink).ownerObjId, true).shareId;
        else if (parentObjLink instanceof FileLinkRecord)
          objId = cache.getFolderShareRecordMy(((FileLinkRecord) parentObjLink).ownerObjId, true).shareId;

        request.ownerObjIDs = new Long[] { objId };
        MessageAction msgAction = new MessageAction(CommandCodes.STAT_Q_FETCH_ALL_OBJ_STATS, request);
        ServerInterfaceLayer serverInterfaceLayer = MainFrame.getServerInterfaceLayer();
        ClientMessageAction reply = serverInterfaceLayer.submitAndFetchReply(msgAction, 30000);
        if (reply != null && reply.getActionCode() == CommandCodes.STAT_A_GET) {
          Stats_Get_Rp statsReply = (Stats_Get_Rp) reply.getMsgDataSet();
          UserOps.fetchUnknownUsers(serverInterfaceLayer, statsReply.stats);
          updateData(statsReply.stats);
          // Clear the data from reply, we don't want it going to the cache.
          statsReply.stats = new StatRecord[0];
        }
        DefaultReplyRunner.nonThreadedRun(serverInterfaceLayer, reply);
      }
    };
    th.setDaemon(true);
    th.start();
  }

  /**
   * Checks if folder share's content of a given ID was already retrieved.
   */
  public boolean isContentFetched(Long shareId) {
    return false;
  }

}
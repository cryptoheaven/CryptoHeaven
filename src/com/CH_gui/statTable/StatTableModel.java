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

package com.CH_gui.statTable;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.DefaultReplyRunner;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.UserOps;
import com.CH_cl.service.records.filters.StatFilter;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.stat.Stats_Get_Rp;
import com.CH_co.service.msg.dataSets.stat.Stats_Get_Rq;
import com.CH_co.service.records.*;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.table.ColumnHeaderData;
import com.CH_gui.table.RecordTableCellRenderer;
import com.CH_gui.table.RecordTableModel;

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
* <b>$Revision: 1.20 $</b>
* @author  Marcin Kurzawa
* @version 
*/
public class StatTableModel extends RecordTableModel {

  // Parent is either MsgLinkRecord or FileLinkRecord
  private Record parentObjLink;

  private static String STR_USER = com.CH_cl.lang.Lang.rb.getString("column_User");
  private static String STR_FIRST_SEEN = com.CH_cl.lang.Lang.rb.getString("column_First_Seen");
  private static String STR_CONTENT_RETRIEVED = com.CH_cl.lang.Lang.rb.getString("column_Content_Retrieved");

  private static final ColumnHeaderData columnHeaderData = 
      new ColumnHeaderData(new Object[][]
        { { STR_USER, STR_FIRST_SEEN, STR_CONTENT_RETRIEVED },
          { STR_USER, STR_FIRST_SEEN, STR_CONTENT_RETRIEVED },
          { null, null, null },
          { null, null, null },
          { new Integer(249), new Integer(160), new Integer(160) },
          { new Integer(249), new Integer(160), new Integer(160) },
          { new Integer(249), new Integer(160), new Integer(160) },
          { new Integer(  0), new Integer(  0), new Integer(  0) },
          { new Integer( 90), new Integer(160), new Integer(160) },
          { new Integer(0), new Integer(1), new Integer(2) },
          { new Integer(0), new Integer(1), new Integer(2) },
          { new Integer(0), new Integer(1), new Integer(2) },
          { new Integer(0) }
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
  public void clearCachedFetchedFolderIDs() {
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
    setCollapseFileVersions(true);
  }

  public synchronized void refreshData() {
    Thread th = new ThreadTraced("Stat Refresher") {
      public void runTraced() {
        removeData();
        Stats_Get_Rq request = new Stats_Get_Rq();
        if (parentObjLink instanceof MsgLinkRecord)
          request.statsForObjType = new Short(Record.RECORD_TYPE_MSG_LINK);
        else if (parentObjLink instanceof FileLinkRecord)
          request.statsForObjType = new Short(Record.RECORD_TYPE_FILE_LINK);
        request.ownerObjType = new Short(Record.RECORD_TYPE_SHARE);
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
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

package com.CH_gui.groupTable;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.event.FolderShareRecordEvent;
import com.CH_cl.service.cache.event.FolderShareRecordListener;
import com.CH_cl.service.cache.event.RecordEvent;
import com.CH_cl.service.records.filters.FixedFilter;
import com.CH_cl.service.records.filters.ShareFilter;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.Obj_IDAndIDList_Rq;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.RecordFilter;
import com.CH_co.trace.Trace;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.table.ColumnHeaderData;
import com.CH_gui.table.RecordTableCellRenderer;
import com.CH_gui.table.RecordTableModel;
import java.util.ArrayList;

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
* <b>$Revision: 1.3 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class GroupTableModel extends RecordTableModel {

  // FolderShareIds for which records have been fetched already.
  private static final ArrayList fetchedIds = new ArrayList();

  private FolderShareListener shareListener;

  private static String STR_NAME = com.CH_cl.lang.Lang.rb.getString("column_Member");
  private static String STR_ADD = com.CH_cl.lang.Lang.rb.getString("column_Add_Members");
  private static String STR_REMOVE = com.CH_cl.lang.Lang.rb.getString("column_Remove_Members");

  static final ColumnHeaderData columnHeaderData = 
      new ColumnHeaderData(new Object[][] 
        { { STR_NAME, STR_ADD, STR_REMOVE },
          { STR_NAME, STR_ADD, STR_REMOVE },
          { null, null, null },
          { null, null, null },
          { new Integer(120), new Integer(55), new Integer(55) },
          { new Integer(120), new Integer(55), new Integer(55) },
          { new Integer(120), new Integer(55), new Integer(55) },
          { new Integer(  0), new Integer( 0), new Integer( 0) },
          { new Integer( 90), new Integer(55), new Integer(55) },
          { new Integer(0), new Integer(1), new Integer(2) },
          { new Integer(0), new Integer(1), new Integer(2) },
          { new Integer(0), new Integer(1), new Integer(2) },
          { new Integer(0) }
        });

  /** 
  * Creates new GroupTableModel.
  * Sets auto update.
  */
  public GroupTableModel() {
    super(columnHeaderData);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GroupTableModel.class, "GroupTableModel()");
    if (trace != null) trace.exit(GroupTableModel.class);
  }

  /** 
  * Creates new GroupTableModel.
  * Sets auto update.
  */
  public GroupTableModel(Long folderId) {
    super(columnHeaderData, new ShareFilter(folderId));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GroupTableModel.class, "GroupTableModel()");
    initData(folderId);
    if (trace != null) trace.exit(GroupTableModel.class);
  }

  /**
  * When folders are fetched, their IDs are cached so that we know if table fetch is required when
  * user switches focus to another folder...
  * This vector should also be cleared when users are switched...
  */
  public void clearCachedFetchedFolderIDs() {
    fetchedIds.clear();
  }

  /**
  * Sets auto update mode by listening on the cache share updates.
  */
  public synchronized void setAutoUpdate(boolean flag) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GroupTableModel.class, "setAutoUpdate(boolean flag)");
    if (trace != null) trace.args(flag);
    if (flag) {
      if (shareListener == null) {
        shareListener = new FolderShareListener();
        FetchedDataCache.getSingleInstance().addFolderShareRecordListener(shareListener);
      }
    } else {
      if (shareListener != null) {
        FetchedDataCache.getSingleInstance().removeFolderShareRecordListener(shareListener);
        shareListener = null;
      }
    }
    if (trace != null) trace.exit(GroupTableModel.class);
  }

  /**
  * Initializes the model setting the specified folderId as its main variable
  */
  public synchronized void initData(Long folderId) {
    FolderPair folderPair = getParentFolderPair();
    if (folderPair == null || folderPair.getFolderRecord() == null || !folderPair.getId().equals(folderId)) {
      setFilter(folderId != null ? (RecordFilter) new ShareFilter(folderId) : (RecordFilter) new FixedFilter(false));
      switchData(folderId);
      refreshData(folderId, false);
    }
    setCollapseFileVersions(true);
  }

  /**
  * @param fetch true if data should be refetched from the database.
  */
  public synchronized void refreshData(boolean forceFetch) {
    FolderPair folderPair = getParentFolderPair();
    if (folderPair != null) {
      refreshData(folderPair.getId(), forceFetch);
    }
  }

  /**
  * Initializes the model setting the specified folderId as its main variable
  */
  private synchronized void switchData(Long folderId) {
    FolderPair folderPair = getParentFolderPair();
    if (folderPair == null || folderPair.getFolderRecord() == null || !folderPair.getId().equals(folderId)) {

      removeData();

      if (folderId != null) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        FolderShareRecord shareRec = cache.getFolderShareRecordMy(folderId, true);
        FolderRecord folderRec = cache.getFolderRecord(folderId);
        if (shareRec != null && folderRec != null) {

          folderPair = new FolderPair(shareRec, folderRec);
          setParentFolderPair(folderPair);

          // add all shares
          FolderShareRecord[] folderShares = cache.getFolderShareRecordsForFolder(folderId);
          if (folderShares != null && folderShares.length > 0) {
            updateData(folderShares);
          }
        }
      } // end if folderId != null
      else
        setParentFolderPair(null);
    }
  }


  /**
  * Forces a refresh of data displayed even if its already displaying the specified folder data.
  */
  private synchronized void refreshData(Long folderId, boolean forceFetch) {
    if (folderId != null) {
      FolderShareRecord shareRec = FetchedDataCache.getSingleInstance().getFolderShareRecordMy(folderId, true);
      if (shareRec != null) {
        Long shareId = shareRec.shareId;
        fetchShares(shareId, folderId, forceFetch);
      }
    }
  }

  public Object getValueAtRawColumn(Record record, int column, boolean forSortOnly) {
    Object value = null;

    if (record instanceof FolderShareRecord) {
      FolderShareRecord shareRecord = (FolderShareRecord) record;

      switch (column) {
        case 0: 
          UserRecord uRec = FetchedDataCache.getSingleInstance().getUserRecord(shareRecord.ownerUserId);
          if (uRec != null) value = uRec.shortInfo();
          else value = shareRecord.ownerUserId.toString();
          break;
        case 1: //value = Boolean.valueOf(shareRecord.canWrite.shortValue() == FolderShareRecord.YES);
          value = shareRecord.canWrite.shortValue() == FolderShareRecord.YES ? "YES" : "-";
          break;
        case 2: //value = Boolean.valueOf(shareRecord.canDelete.shortValue() == FolderShareRecord.YES);
          value = shareRecord.canDelete.shortValue() == FolderShareRecord.YES ? "YES" : "-";
          break;
      }
    }

    return value;
  }

  public Class getColumnClass(int columnIndex) {
    Class c = null;
    switch (columnIndex) {
      case 0: c = String.class;
        break;
      case 1: 
      case 2: c = String.class;
        break;
    }
    return c;
  }

  public RecordTableCellRenderer createRenderer() {
    return new GroupTableCellRenderer();
  }

  /** Filter the records with desired folderId. */
/*
  public void setData(Record[] records) {
    if (folderPair != null && records != null && records.length > 0) {
      records = FolderShareRecord.filterDesiredFolderRecords((FolderShareRecord[]) records, folderPair.getId());
    }
    if (records != null && records.length > 0) {
      super.setData(records);
    }
  }
*/  

  /** Filter the records with desired folderId. */
/*  public void updateData(Record[] records) {
    if (folderPair != null && records != null && records.length > 0) {
      records = FolderShareRecord.filterDesiredFolderRecords((FolderShareRecord[]) records, folderPair.getId());
    }
    if (records != null && records.length > 0) {
      super.updateData(records);
    }
  }

*/

  /**
  * Invoked by the cell editor when value in the column changes.
  */
  public void setValueAt(Object aValue, int row, int column) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GroupTableModel.class, "setValueAt(Object aValue, int row, int column)");
    if (trace != null) trace.args(aValue);
    if (trace != null) trace.args(row);
    if (trace != null) trace.args(column);

    if (column > 0 && aValue instanceof Boolean) {
      FolderShareRecord shareRecord = (FolderShareRecord) getRowObject(row);
      Boolean b = (Boolean) aValue;
      Short newShort = b.booleanValue() ? new Short(FolderShareRecord.YES) : new Short(FolderShareRecord.NO);
      if (column == 1)
        shareRecord.canWrite = newShort;
      else if (column == 2)
        shareRecord.canDelete = newShort;
    }

    if (trace != null) trace.exit(GroupTableModel.class);
  }


  /* The cell is only editable if it belongs to permission columns */
  public boolean isCellEditable (int row, int column) {
    boolean isEditable = false;
    if (isEditable() && column > 0) {
      isEditable = true;
    }
    return isEditable;
  }


  /** 
  * Send a request to fetch shares for the <code> folderPair </code> from the server.
  */
  private synchronized void fetchShares(final Long shareId, Long folderId, boolean force) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GroupTableModel.class, "fetchShares(FolderPair folderPair, Long folderId, boolean force)");
    if (trace != null) trace.args(shareId, folderId);
    if (trace != null) trace.args(force);

    synchronized (fetchedIds) {
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      if (force || 
              !cache.wasFolderFetchRequestIssued(folderId) || 
              !fetchedIds.contains(shareId)) {

//        // if folder previously fetched, remove file links from the cache, leave the folders
//        if (fetchedIds.contains(shareId)) {
//          int rowCount = getRowCount();
//          Vector linksToRemove = new Vector();
//          for (int row=0; row<rowCount; row++) {
//            Record rec = getRowObject(row);
//            if (rec instanceof FileLinkRecord) {
//              linksToRemove.addElement(rec);
//            }
//          }
//          if (linksToRemove.size() > 0) {
//            FileLinkRecord[] fRecs = new FileLinkRecord[linksToRemove.size()];
//            linksToRemove.toArray(fRecs);
//            fetchedIds.remove(shareId);
//            cache.removeFileLinkRecords(fRecs);
//          }
//        }

        // if we should frech only when we already have the folder-share pair, or they weren't already deleted
        if (cache.getFolderShareRecord(shareId) != null &&
            cache.getFolderRecord(folderId) != null &&
            !cache.getFolderRecord(folderId).isCategoryType()) {

          FolderRecord folder = cache.getFolderRecord(folderId);
          if (folder != null) cache.markFolderFetchRequestIssued(folder.folderId);

          Obj_IDAndIDList_Rq request = new Obj_IDAndIDList_Rq();
          request.IDs = new Obj_IDList_Co();
          request.IDs.IDs = new Long[] {shareId}; 
          request.id = new Long(Record.RECORD_TYPE_SHARE);

          MessageAction msgAction = new MessageAction(CommandCodes.FLD_Q_GET_FOLDER_SHARES, new Obj_IDList_Co(shareId));
          Runnable replyReceivedJob = new Runnable() {
            public void run() {
              if (!fetchedIds.contains(shareId)) {
                fetchedIds.add(shareId);
              }
            }
          };
          MainFrame.getServerInterfaceLayer().submitAndReturn(msgAction, 10000, replyReceivedJob, null, null);
        }
      }
    } // end synchronized

    if (trace != null) trace.exit(GroupTableModel.class);
  } 

  /**
  * Checks if folder share's content of a given ID was already retrieved.
  */
  public boolean isContentFetched(Long shareId) {
    synchronized (fetchedIds) {
      return fetchedIds.contains(shareId);
    }
  }


  /****************************************************************************************/
  /************* LISTENERS ON CHANGES IN THE CACHE *****************************************/
  /****************************************************************************************/

  /** Listen on updates to the FolderShareRecords in the cache.
    * If the event happens, add, move or remove shares
    */
  private class FolderShareListener implements FolderShareRecordListener {
    public void folderShareRecordUpdated(FolderShareRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second, and to prevent gui tree deadlocks.
      javax.swing.SwingUtilities.invokeLater(new GroupGUIUpdater(event));

    }
  }


  private class GroupGUIUpdater implements Runnable {
    private FolderShareRecordEvent event;
    public GroupGUIUpdater(FolderShareRecordEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GroupGUIUpdater.class, "GroupGUIUpdater(FolderShareRecordEvent event)");
      if (trace != null) trace.args(event);
      this.event = event;
      if (trace != null) trace.exit(GroupGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GroupGUIUpdater.class, "GroupGUIUpdater.run()");

      FolderShareRecord[] records = event.getFolderShareRecords();
      if (event.getEventType() == RecordEvent.SET) {
        // clone the shares so when we edit them and cancel the dialog, they don't alter our cached values
        for (int i=0; i<records.length; i++) {
          records[i] = (FolderShareRecord) records[i].clone();
        }
        updateData(records);
      } else if (event.getEventType() == RecordEvent.REMOVE) {
        removeData(records);
      }

      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(GroupGUIUpdater.class);
    }
  }

  protected void finalize() throws Throwable {
    setAutoUpdate(false);
    super.finalize();
  }

}
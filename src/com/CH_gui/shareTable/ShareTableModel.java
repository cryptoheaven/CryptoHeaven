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

package com.CH_gui.shareTable;

import java.util.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.cache.event.*;
import com.CH_cl.service.records.filters.*;

import com.CH_gui.frame.MainFrame;
import com.CH_gui.table.*;

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
 * <b>$Revision: 1.23 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class ShareTableModel extends RecordTableModel {

  private FolderShareListener shareListener;

  /* Main folder pair of which this class is managing shares */
  private FolderPair folderPair;

  private static String STR_NAME = com.CH_gui.lang.Lang.rb.getString("column_Name");
  private static String STR_WRITE = com.CH_gui.lang.Lang.rb.getString("column_Write");
  private static String STR_DELETE = com.CH_gui.lang.Lang.rb.getString("column_Delete");
  private static String STR_ADD = com.CH_gui.lang.Lang.rb.getString("column_Add");
  private static String STR_REMOVE = com.CH_gui.lang.Lang.rb.getString("column_Remove");

  //private static final ColumnHeaderData columnHeaderData = 
  public static final ColumnHeaderData[] columnHeaderDatas = new ColumnHeaderData[] {
      // Folders
      new ColumnHeaderData(new Object[][] 
        { { STR_NAME, STR_WRITE, STR_DELETE },
          { STR_NAME, STR_WRITE, STR_DELETE },
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
        }),
      // Groups
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
        })
  };

  /** 
   * Creates new ShareTableModel.
   * Sets auto update.
   */
  public ShareTableModel() {
    super(columnHeaderDatas[0]);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ShareTableModel.class, "ShareTableModel()");
    if (trace != null) trace.exit(ShareTableModel.class);
  }

  /** 
   * Creates new ShareTableModel.
   * Sets auto update.
   */
  public ShareTableModel(FolderPair folderPair) {
    super(columnHeaderDatas[0], new ShareFilter(folderPair.getId()));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ShareTableModel.class, "ShareTableModel()");
    this.folderPair = folderPair;
    setAutoUpdate(true);
    fetchShares(folderPair);
    if (trace != null) trace.exit(ShareTableModel.class);
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
   * Sets auto update mode by listening on the cache share updates.
   */
  public synchronized void setAutoUpdate(boolean flag) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ShareTableModel.class, "setAutoUpdate(boolean flag)");
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
    if (trace != null) trace.exit(ShareTableModel.class);
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
        case 1: value = Boolean.valueOf(shareRecord.canWrite.shortValue() == FolderShareRecord.YES);
          break;
        case 2: value = Boolean.valueOf(shareRecord.canDelete.shortValue() == FolderShareRecord.YES);
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
      case 2: c = Boolean.class;
    }
    return c;
  }

  public RecordTableCellRenderer createRenderer() {
    return new ShareTableCellRenderer();
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
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ShareTableModel.class, "setValueAt(Object aValue, int row, int column)");
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

    if (trace != null) trace.exit(ShareTableModel.class);
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
  private synchronized void fetchShares(FolderPair folderPair) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ShareTableModel.class, "fetchShares(FolderPair folderPair)");
    if (trace != null) trace.args(folderPair);

    MessageAction msgAction = new MessageAction(CommandCodes.FLD_Q_GET_FOLDER_SHARES, new Obj_IDList_Co(folderPair.getFolderShareRecord().shareId));
    MainFrame.getServerInterfaceLayer().submitAndReturn(msgAction);

    if (trace != null) trace.exit(ShareTableModel.class);
  } 


  /****************************************************************************************/
  /************* LISTENERS ON CHANGES IN THE CACHE *****************************************/
  /****************************************************************************************/

   /** Listen on updates to the FolderShareRecords in the cache.
    * If the event happens, add, move or remove shares
    */
   private class FolderShareListener implements FolderShareRecordListener {
    public void folderShareRecordUpdated(FolderShareRecordEvent event) {
      // to prevent deadlocks, run in seperate thread
      javax.swing.SwingUtilities.invokeLater(new ModelUpdater(event));
    }
  }

  private class ModelUpdater implements Runnable {
    private FolderShareRecordEvent event;
    public ModelUpdater(FolderShareRecordEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ModelUpdater.class, "ModelUpdater(FolderShareRecordEvent event)");
      this.event = event;
      if (trace != null) trace.exit(ModelUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ModelUpdater.class, "ModelUpdater.run()");

      if (trace != null) trace.args(event);

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
      if (trace != null) trace.exit(ModelUpdater.class);
    }
  }
   
   
   
  protected void finalize() throws Throwable {
    setAutoUpdate(false);
    super.finalize();
  }

  /**
   * Checks if folder share's content of a given ID was already retrieved.
   */
  public boolean isContentFetched(Long shareId) {
    return false;
  }

}
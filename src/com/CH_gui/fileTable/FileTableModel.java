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

package com.CH_gui.fileTable;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.cache.event.*;
import com.CH_cl.service.records.*;
import com.CH_cl.service.records.filters.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.file.File_GetFiles_Rq;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.frame.MainFrame;
import com.CH_gui.file.FileUtilities;
import com.CH_gui.recycleTable.*;
import com.CH_gui.table.*;

import java.sql.Timestamp;
import java.util.*;

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
 * <b>$Revision: 1.30 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FileTableModel extends RecordTableModel {

  private FileLinkListener linkListener;
  private FolderShareListener shareListener;
  private FolderListener folderListener;

  /* folderShareIds for which the files has been fetched already */
  private static final Vector fetchedIds = new Vector();

  protected static final String STR_FILE_FOLDER = com.CH_gui.lang.Lang.rb.getString("folder_File_Folder");
  protected static final String STR_SHARED_FOLDER = com.CH_gui.lang.Lang.rb.getString("folder_Shared_Folder");
  protected static final String STR_FLAG = com.CH_gui.lang.Lang.rb.getString("column_Flag");
  protected static final String STR_FILE_NAME = com.CH_gui.lang.Lang.rb.getString("column_File_Name");
  protected static final String STR_TYPE = com.CH_gui.lang.Lang.rb.getString("column_Type");
  protected static final String STR_SIZE = com.CH_gui.lang.Lang.rb.getString("Size");
  protected static final String STR_CREATED = com.CH_gui.lang.Lang.rb.getString("column_Created");
  protected static final String STR_UPDATED = com.CH_gui.lang.Lang.rb.getString("column_Updated");
  protected static final String STR_LINK_ID = com.CH_gui.lang.Lang.rb.getString("column_Link_ID");
  protected static final String STR_DATA_ID = com.CH_gui.lang.Lang.rb.getString("column_Data_ID");

  static final ColumnHeaderData columnHeaderData =
      new ColumnHeaderData(new Object[][]
        { { null, STR_FILE_NAME, STR_TYPE, STR_SIZE, STR_CREATED, STR_UPDATED, STR_LINK_ID, STR_DATA_ID },
          { STR_FLAG, STR_FILE_NAME, STR_TYPE, STR_SIZE, STR_CREATED, STR_UPDATED, STR_LINK_ID, STR_DATA_ID },
          { com.CH_gui.lang.Lang.rb.getString("columnTip_New/Old_Status_Flag"), null, null, null, null, null },
          { new Integer(ImageNums.FLAG_GRAY_SMALL), null, null, null, null, null },
          { new Integer(16), new Integer(141), new Integer(85), new Integer( 74), TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 60), new Integer( 60) },
          { new Integer(16), new Integer(141), new Integer(85), new Integer( 74), TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 60), new Integer( 60) },
          { new Integer(16), new Integer(141), new Integer(85), new Integer( 74), TIMESTAMP_PRS, TIMESTAMP_PRS, new Integer( 60), new Integer( 60) },
          { new Integer(16), new Integer(  0), new Integer( 0), new Integer(100), TIMESTAMP_MAX, TIMESTAMP_MAX, new Integer(120), new Integer(120) },
          { new Integer(16), new Integer( 90), new Integer(80), new Integer( 70), TIMESTAMP_MIN, TIMESTAMP_MIN, new Integer( 50), new Integer( 50) },
          { new Integer(0), new Integer(1), new Integer(2), new Integer(3), new Integer(4) },
          { new Integer(0), new Integer(1), new Integer(2), new Integer(3), new Integer(4) },
          { new Integer(0), new Integer(1), new Integer(2), new Integer(3), new Integer(4) },
          { new Integer(1), new Integer(4) }
        });


  /** Creates new FileTableModel */
  public FileTableModel(Long folderId) {
    super(columnHeaderData, new FixedFilter(false));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileTableModel.class, "FileTableModel()");
    initData(folderId);
    if (trace != null) trace.exit(FileTableModel.class);
  }

  /**
   * When folders are fetched, their IDs are cached so that we know if table fetch is required when
   * user switches focus to another folder...
   * This vector should also be cleared when users are switched...
   */
  public Vector getCachedFetchedFolderIDs() {
    return fetchedIds;
  }

  /**
   * Sets auto update mode by listening on the cache contact updates.
   */
  public synchronized void setAutoUpdate(boolean flag) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileTableModel.class, "setAutoUpdate(boolean flag)");
    if (trace != null) trace.args(flag);
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    if (flag) {
      if (linkListener == null) {
        linkListener = new FileLinkListener();
        shareListener = new FolderShareListener();
        folderListener = new FolderListener();

        cache.addFileLinkRecordListener(linkListener);
        cache.addFolderShareRecordListener(shareListener);
        cache.addFolderRecordListener(folderListener);
      }
    } else {
      if (linkListener != null) {
        cache.removeFileLinkRecordListener(linkListener);
        cache.removeFolderShareRecordListener(shareListener);
        cache.removeFolderRecordListener(folderListener);

        linkListener = null;
        shareListener = null;
        folderListener = null;
      }
    }
    if (trace != null) trace.exit(FileTableModel.class);
  }


  /**
   * Initializes the model setting the specified folderId as its main variable
   */
  public synchronized void initData(Long folderId) {
    initData(folderId, false);
  }
  public synchronized void initData(Long folderId, boolean forceSwitch) {
    FolderPair folderPair = getParentFolderPair();
    if (forceSwitch || folderPair == null || folderPair.getFolderRecord() == null || !folderPair.getId().equals(folderId)) {
      setFilter(folderId != null ? (RecordFilter) new FileFilter(folderId, false) : (RecordFilter) new FixedFilter(false));
      switchData(folderId, forceSwitch);
      refreshData(folderId, false);
    }
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
  private synchronized void switchData(Long folderId, boolean forceSwitch) {
    FolderPair folderPair = getParentFolderPair();
    if (forceSwitch || folderPair == null || folderPair.getFolderRecord() == null || !folderPair.getId().equals(folderId)) {

      removeData();

      if (folderId != null) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        FolderShareRecord shareRec = cache.getFolderShareRecordMy(folderId, true);
        FolderRecord folderRec = cache.getFolderRecord(folderId);
        if (shareRec != null && folderRec != null) {

          folderPair = new FolderPair(shareRec, folderRec);
          setParentFolderPair(folderPair);

          // Add the child folders, include viewChildren
          FolderPair[] childFolderPairs = cache.getFolderPairsViewChildren(folderId, true);
          if (childFolderPairs != null && childFolderPairs.length > 0) {
            updateData(childFolderPairs);
          }

          // add all files
          FileLinkRecord[] fileLinks = cache.getFileLinkRecordsOwnerAndType(folderId, new Short(Record.RECORD_TYPE_FOLDER));
          if (fileLinks != null && fileLinks.length > 0) {
            updateData(fileLinks);
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
        // fetch the files for this folder
        fetchFiles(shareId, folderId, forceFetch);
      }
    }
  }

  public Collection getSearchableCharSequencesFor(Object searchableObj) {
    if (searchableObj instanceof Record)
      return RecycleTableModel.getSearchTextFor((Record) searchableObj, true);
    else
      return null;
  }

  public Object getValueAtRawColumn(Record record, int column, boolean forSortOnly) {
    Object value = null;

    if (record instanceof FileLinkRecord) {
      FileLinkRecord fileLink = (FileLinkRecord) record;

      switch (column) {
        case 0:
          boolean isStarred = fileLink.isStarred();
          boolean isFlagged = false;
          StatRecord stat = FetchedDataCache.getSingleInstance().getStatRecord(fileLink.fileLinkId, FetchedDataCache.STAT_TYPE_FILE);
          if (stat != null)
            isFlagged = StatRecord.getIconForFlag(stat.getFlag()) != ImageNums.IMAGE_NONE;
          if (isStarred && isFlagged)
            value = new Short((short) 1);
          else if (isStarred)
            value = new Short((short) 2);
          else if (isFlagged)
            value = new Short((short) 3);
          else
            value = new Short((short) 4);
          break;
        case 1: value = fileLink.getFileName();
          break;
        case 2:
          if (fileLink.isAborted())
            value = "Upload Aborted by User!";
          else if(fileLink.isIncomplete())
            value = "Pending Data Upload...";
          else
            value = fileLink.getFileType();
          break;
        case 3: value = fileLink.origSize;
          break;
        case 4: value = fileLink.recordCreated;
          break;
        case 5: value = fileLink.recordUpdated;
          break;
        // Link ID
        case 6: value = fileLink.fileLinkId;
          break;
        case 7: value = fileLink.fileId;
          break;
      }

    } else if (record instanceof FolderPair) {
      FolderPair folderPair = (FolderPair) record;

      switch (column) {
        case 1: value = folderPair.getMyName();
          break;
        case 2: value = folderPair.getFolderRecord().numOfShares.shortValue() <=1 ? STR_FILE_FOLDER : STR_SHARED_FOLDER;
          break;
        case 3: value = new Long(-1);
          break;
        case 4: value = folderPair.getFolderShareRecord().dateCreated;
          break;
        case 5: value = folderPair.getFolderShareRecord().dateUpdated;
          break;
        // Link ID
        case 6: value = folderPair.getFolderShareRecord().shareId;
          break;
        case 7: value = folderPair.getFolderRecord().folderId;
          break;
      }
    }

    return value;
  }

  public RecordTableCellRenderer createRenderer() {
    return new FileTableCellRenderer();
  }


  /**
   * Invoked by the cell editor when value in the column changes.
   */
  public void setValueAt(Object aValue, int row, int column) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileTableModel.class, "setValueAt(Object aValue, int row, int column)");
    if (trace != null) trace.args(aValue);
    if (trace != null) trace.args(row);
    if (trace != null) trace.args(column);

    if (column == 1 && aValue instanceof String) {
      String newName = ((String) aValue).trim();
      if (newName.length() > 0) {
        Object o = getRowObject(row);
        if (o instanceof FileLinkRecord) {
          FileLinkRecord fileLinkRecord = (FileLinkRecord) o;
          String oldName = fileLinkRecord.getFileName().trim();

          if (!oldName.equals(newName)) {
            FileLinkRecord newFileLinkRecord = (FileLinkRecord) fileLinkRecord.clone();
            newFileLinkRecord.setFileName(newName);

            fileLinkRecord.setFileName(newName + "^");
            super.setValueAt(aValue, row, column);

            FileUtilities.renameFile(newName, newFileLinkRecord);
          }
        } else if (o instanceof FolderPair) {
          FolderPair folderPair = (FolderPair) o;
          String oldName = folderPair.getMyName().trim();

          if (!oldName.equals(newName)) {
            FolderShareRecord newFolderShare = (FolderShareRecord) folderPair.getFolderShareRecord().clone();
            newFolderShare.setFolderName(newName);

            folderPair.getFolderShareRecord().setFolderName(newName + "^");
            super.setValueAt(aValue, row, column);

            // keep Old description
            String newDesc = newFolderShare.getFolderDesc();
            FileUtilities.renameFolderAndShares(newName, newDesc, newName, newDesc, newFolderShare);
          }
        }
      }
    }

    if (trace != null) trace.exit(FileTableModel.class);
  }


  /**
   * Send a request to fetch files for the <code> shareId </code> from the server
   * if files were not fetched for this folder, otherwise get them from cache
   * @param force true to force a fetch from the database
   */
  private void fetchFiles(final Long shareId, final Long folderId, boolean force) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileTableModel.class, "fetchFiles(Long shareId, Long folderId, boolean force)");
    if (trace != null) trace.args(shareId, folderId);
    if (trace != null) trace.args(force);

    synchronized (fetchedIds) {
      if (force || !fetchedIds.contains(shareId)) {

        FetchedDataCache cache = FetchedDataCache.getSingleInstance();

        // if refreshing and folder previously fetched, remove file links from the cache, leave the folders
        if (force && fetchedIds.contains(shareId)) {
          int rowCount = getRowCount();
          Vector linksToRemove = new Vector();
          for (int row=0; row<rowCount; row++) {
            Record rec = getRowObjectNoTrace(row);
            if (rec instanceof FileLinkRecord) {
              linksToRemove.addElement(rec);
            }
          }
          if (linksToRemove.size() > 0) {
            FileLinkRecord[] fRecs = new FileLinkRecord[linksToRemove.size()];
            linksToRemove.toArray(fRecs);
            fetchedIds.remove(shareId);
            cache.removeFileLinkRecords(fRecs);
          }
        }

        // if we should fetch only when we already have the folder-share pair, or they weren't already deleted
        if (cache.getFolderShareRecord(shareId) != null &&
            cache.getFolderRecord(folderId) != null &&
            !cache.getFolderRecord(folderId).isCategoryType()) {

          FolderRecord folder = cache.getFolderRecord(folderId);
          if (folder != null) FolderRecUtil.markFolderViewInvalidated(folder.folderId, false);
          if (folder != null) FolderRecUtil.markFolderFetchRequestIssued(folder.folderId);

          final int _action = CommandCodes.FILE_Q_GET_FILES_STAGED;

          // order of fetching is from newest to oldest
          short fetchNumMax = -File_GetFiles_Rq.FETCH_NUM_LIST__INITIAL_SIZE;

          // <shareId> <ownerObjType> <ownerObjId> <fetchNum> <timestamp>
          File_GetFiles_Rq request = new File_GetFiles_Rq(shareId, Record.RECORD_TYPE_FOLDER, folderId, fetchNumMax, (Timestamp) null);

          // Gather items already fetched so we don't re-fetch all items if not necessary.
          FileLinkRecord[] existingLinks = cache.getFileLinkRecords(shareId);
          request.exceptLinkIDs = RecordUtils.getIDs(existingLinks);

          MessageAction msgAction = new MessageAction(_action, request);
          Runnable replyReceivedJob = new Runnable() {
            public void run() {
              if (!fetchedIds.contains(shareId)) {
                fetchedIds.add(shareId);
              }
            }
          };
          Runnable afterJob = new Runnable() {
            public void run() {
              FolderRecord folder = FetchedDataCache.getSingleInstance().getFolderRecord(folderId);
              if (folder != null) FolderRecUtil.markFolderViewInvalidated(folder.folderId, false);
            }
          };
          MainFrame.getServerInterfaceLayer().submitAndReturn(msgAction, 10000, replyReceivedJob, afterJob, afterJob);
        }
      }
    } // end synchronized


    if (trace != null) trace.exit(FileTableModel.class);
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
  /************* LISTENERS ON CHANGES IN THE CACHE ****************************************/
  /****************************************************************************************/

  /** Listen on updates to the FileLinkRecords in the cache.
    * if the event happens, add, move or remove files
    */
  private class FileLinkListener implements FileLinkRecordListener {
    public void fileLinkRecordUpdated(FileLinkRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new FileGUIUpdater(event));
    }
  }

  /** Listen on updates to the FolderShareRecords in the cache.
    * If the event happens, add, move or remove shares
    */
  private class FolderShareListener implements FolderShareRecordListener {
    public void folderShareRecordUpdated(FolderShareRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new FileGUIUpdater(event));
    }
  }

  /** Listen on updates to the FolderRecords in the cache.
    * If the event happens, set or remove records
    */
  private class FolderListener implements FolderRecordListener {
    public void folderRecordUpdated(FolderRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second, and to prevent gui tree deadlocks.
      javax.swing.SwingUtilities.invokeLater(new FileGUIUpdater(event));
    }
  }

  private class FileGUIUpdater implements Runnable {
    private RecordEvent event;
    public FileGUIUpdater(RecordEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileGUIUpdater.class, "FileGUIUpdater(RecordEvent event)");
      this.event = event;
      if (trace != null) trace.exit(FileGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileGUIUpdater.class, "FileGUIUpdater.run()");
      fileAndFolderUpdate(event);
      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(FileGUIUpdater.class);
    }
  }

  /** Get the records and event type from the event and switch to appropriate methods
    * to set or remove these records.
    * Note: Removing shares is not supported
    */
  private void fileAndFolderUpdate(RecordEvent event) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileTableModel.class, "fileAndFolderUpdate(RecordEvent event)");
    if (trace != null) trace.args(event);

    Record[] recs = event.getRecords();
    FolderPair folderPair = getParentFolderPair();
    if (trace != null) trace.data(10, "folderPair", folderPair);
    if (trace != null) trace.data(11, "recs.length", recs.length);
    if (folderPair != null && recs != null && recs.length > 0) {

      // filter out only interested records
      Vector halfPairPicksV = new Vector();
      Vector linksPicksV = new Vector();
      Record[] halfPairPicks = null;
      FolderPair[] pairPicks = null;
      FileLinkRecord[] linksPicks = null;

      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      Long userId = cache.getMyUserId();

      for (int i=0; i<recs.length; i++) {
        Record rec = recs[i];
        if (rec instanceof FolderRecord) {
          halfPairPicksV.addElement(rec);
        } else if (rec instanceof FolderShareRecord) {
          FolderShareRecord sRec = (FolderShareRecord) rec;
          if (sRec.ownerUserId.equals(userId)) {
            halfPairPicksV.addElement(rec);
          }
        } else if (rec instanceof FileLinkRecord) {
          linksPicksV.addElement(rec);
        }
      }

      if (halfPairPicksV.size() > 0) {
        halfPairPicks = new Record[halfPairPicksV.size()];
        halfPairPicksV.toArray(halfPairPicks);
        pairPicks = CacheUtilities.convertRecordsToPairs(halfPairPicks, event.getEventType() == RecordEvent.REMOVE);
      }

      if (linksPicksV.size() > 0) {
        linksPicks = new FileLinkRecord[linksPicksV.size()];
        linksPicksV.toArray(linksPicks);
      }

      if (event.getEventType() == RecordEvent.SET) {
        if (pairPicks != null && pairPicks.length > 0) {
          updateData(pairPicks);
        }
        if (linksPicks != null && linksPicks.length > 0) {
          updateData(linksPicks);
        }
      } else if (event.getEventType() == RecordEvent.REMOVE) {
        if (recs[0] instanceof FolderShareRecord) {
          // ignore
        } else {
          if (pairPicks != null && pairPicks.length > 0) {
            removeData(pairPicks);
          }
          if (linksPicks != null && linksPicks.length > 0) {
            removeData(linksPicks);
          }
        }
      }
    }

    if (trace != null) trace.exit(FileTableModel.class);
  }

  protected void finalize() throws Throwable {
    setAutoUpdate(false);
    super.finalize();
  }

}
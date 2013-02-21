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

package com.CH_gui.recycleTable;

import com.CH_cl.service.cache.CacheFldUtils;
import com.CH_cl.service.cache.CacheUsrUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.event.*;
import com.CH_cl.service.records.filters.FileFilter;
import com.CH_cl.service.records.filters.FixedFilter;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.file.File_GetFiles_Rq;
import com.CH_co.service.msg.dataSets.msg.Msg_GetMsgs_Rq;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.MsgFilter;
import com.CH_co.service.records.filters.MultiFilter;
import com.CH_co.service.records.filters.RecordFilter;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import com.CH_co.util.ImageNums;
import com.CH_gui.file.FileUtilities;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.table.ColumnHeaderData;
import com.CH_gui.table.RecordTableCellRenderer;
import com.CH_gui.table.RecordTableModel;
import java.sql.Timestamp;
import java.util.ArrayList;

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
* <b>$Revision: 1.2 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class RecycleTableModel extends RecordTableModel {

  private FileLinkListener fileLinkListener;
  private FolderShareListener shareListener;
  private FolderListener folderListener;
  private MsgLinkListener msgLinkListener;

  /* folderShareIds for which the files has been fetched already */
  private static final ArrayList fetchedIds = new ArrayList(); // briefs and full
  private static final ArrayList fetchedIdsBriefs = new ArrayList();
  private static final ArrayList fetchedIdsFull = new ArrayList();

  protected static final String STR_FILE_FOLDER = com.CH_cl.lang.Lang.rb.getString("folder_File_Folder");
  protected static final String STR_SHARED_FOLDER = com.CH_cl.lang.Lang.rb.getString("folder_Shared_Folder");
  protected static final String STR_FLAG = com.CH_cl.lang.Lang.rb.getString("column_Flag");
  protected static final String STR_NAME = com.CH_cl.lang.Lang.rb.getString("column_Name");
  protected static final String STR_FROM = com.CH_cl.lang.Lang.rb.getString("column_From");
  protected static final String STR_TYPE = com.CH_cl.lang.Lang.rb.getString("column_Type");
  protected static final String STR_SIZE = com.CH_cl.lang.Lang.rb.getString("Size");
  protected static final String STR_CREATED = com.CH_cl.lang.Lang.rb.getString("column_Created");
  protected static final String STR_DELETED = com.CH_cl.lang.Lang.rb.getString("column_Deleted");
  protected static final String STR_LINK_ID = com.CH_cl.lang.Lang.rb.getString("column_Link_ID");
  protected static final String STR_DATA_ID = com.CH_cl.lang.Lang.rb.getString("column_Data_ID");

  static final ColumnHeaderData columnHeaderData =
      new ColumnHeaderData(new Object[][]
        { { null, STR_NAME, STR_FROM, STR_TYPE, STR_SIZE, STR_CREATED, STR_DELETED, STR_LINK_ID, STR_DATA_ID },
          { STR_FLAG, STR_NAME, STR_FROM, STR_TYPE, STR_SIZE, STR_CREATED, STR_DELETED, STR_LINK_ID, STR_DATA_ID },
          { com.CH_cl.lang.Lang.rb.getString("columnTip_New/Old_Status_Flag"), null, null, null, null, null, null },
          { new Integer(ImageNums.FLAG_BLANK_SMALL), null, null, null, null, null, null },
          { new Integer(16), new Integer(141), new Integer(100), new Integer(85), new Integer( 74), TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 60), new Integer( 60) },
          { new Integer(16), new Integer(141), new Integer(100), new Integer(85), new Integer( 74), TIMESTAMP_PRL, TIMESTAMP_PRL, new Integer( 60), new Integer( 60) },
          { new Integer(16), new Integer(141), new Integer(100), new Integer(85), new Integer( 74), TIMESTAMP_PRS, TIMESTAMP_PRS, new Integer( 60), new Integer( 60) },
          { new Integer(16), new Integer(  0), new Integer(  0), new Integer( 0), new Integer(100), TIMESTAMP_MAX, TIMESTAMP_MAX, new Integer(120), new Integer(120) },
          { new Integer(16), new Integer( 90), new Integer(100), new Integer(80), new Integer( 70), TIMESTAMP_MIN, TIMESTAMP_MIN, new Integer( 50), new Integer( 50) },
          { new Integer(1), new Integer(2), new Integer(3), new Integer(4), new Integer(6) },
          { new Integer(1), new Integer(2), new Integer(3), new Integer(4), new Integer(6) },
          { new Integer(1), new Integer(2), new Integer(3), new Integer(4), new Integer(6) },
          { new Integer(1), new Integer(6) }
        });


  /** Creates new RecycleTableModel */
  public RecycleTableModel(Long folderId) {
    super(columnHeaderData, new FixedFilter(false));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleTableModel.class, "RecycleTableModel()");
    initData(folderId);
    if (trace != null) trace.exit(RecycleTableModel.class);
  }

  /**
  * When folders are fetched, their IDs are cached so that we know if table fetch is required when
  * user switches focus to another folder...
  * This vector should also be cleared when users are switched...
  */
  public void clearCachedFetchedFolderIDs() {
    fetchedIds.clear();
    fetchedIdsBriefs.clear();
    fetchedIdsFull.clear();
  }

  /**
  * Sets auto update mode by listening on the cache contact updates.
  */
  public synchronized void setAutoUpdate(boolean flag) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleTableModel.class, "setAutoUpdate(boolean flag)");
    if (trace != null) trace.args(flag);
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    if (flag) {
      if (fileLinkListener == null) {
        fileLinkListener = new FileLinkListener();
        shareListener = new FolderShareListener();
        folderListener = new FolderListener();
        msgLinkListener = new MsgLinkListener();

        cache.addFileLinkRecordListener(fileLinkListener);
        cache.addFolderShareRecordListener(shareListener);
        cache.addFolderRecordListener(folderListener);
        cache.addMsgLinkRecordListener(msgLinkListener);
      }
    } else {
      if (fileLinkListener != null) {
        cache.removeFileLinkRecordListener(fileLinkListener);
        cache.removeFolderShareRecordListener(shareListener);
        cache.removeFolderRecordListener(folderListener);
        cache.removeMsgLinkRecordListener(msgLinkListener);

        fileLinkListener = null;
        shareListener = null;
        folderListener = null;
        msgLinkListener = null;
      }
    }
    if (trace != null) trace.exit(RecycleTableModel.class);
  }


  /**
  * Initializes the model setting the specified folderId as its main variable
  */
  public synchronized void initData(Long folderId) {
    initData(folderId, false);
    setCollapseFileVersions(true);
  }
  public synchronized void initData(Long folderId, boolean forceSwitch) {
    FolderPair folderPair = getParentFolderPair();
    if (forceSwitch || folderPair == null || folderPair.getFolderRecord() == null || !folderPair.getId().equals(folderId)) {
      setFilter(folderId != null ? (RecordFilter) new MultiFilter(new FileFilter(folderId, false), new MsgFilter(Record.RECORD_TYPE_FOLDER, folderId), MultiFilter.OR) : (RecordFilter) new FixedFilter(false));
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

          // add all messages
          MsgLinkRecord[] msgLinks = cache.getMsgLinkRecordsOwnerAndType(folderId, new Short(Record.RECORD_TYPE_FOLDER));
          if (msgLinks != null && msgLinks.length > 0) {
            updateData(msgLinks);
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
        // fetch the files and messages for this folder
        fetchFilesAndMsgs(shareId, folderId, forceFetch);
      }
    }
  }


  public Object getValueAtRawColumn(Record record, int column, boolean forSortOnly) {
    Object value = null;

    if (record instanceof FileLinkRecord) {
      FileLinkRecord fileLink = (FileLinkRecord) record;

      switch (column) {
        case 0:
          boolean isStarred = fileLink.isStarred();
          boolean isFlagged = false;
          StatRecord stat = FetchedDataCache.getSingleInstance().getStatRecord(fileLink.fileLinkId, FetchedDataCache.STAT_TYPE_INDEX_FILE);
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
        case 3: 
          if (fileLink.isAborted())
            value = "Upload Aborted by User!";
          else if(fileLink.isIncomplete())
            value = "Pending Data Upload...";
          else
            value = fileLink.getFileType();
          break;
        case 4: value = fileLink.origSize;
          break;
        case 5: value = fileLink.recordCreated;
          break;
        case 6: value = fileLink.recordUpdated;
          break;
        // Link ID
        case 7: value = fileLink.fileLinkId;
          break;
        case 8: value = fileLink.fileId;
          break;
      }

    } else if (record instanceof FolderPair) {
      FolderPair folderPair = (FolderPair) record;

      switch (column) {
        case 1: value = folderPair.getMyName();
          break;
        case 3: value = folderPair.getFolderRecord().numOfShares.shortValue() <=1 ? STR_FILE_FOLDER : STR_SHARED_FOLDER;
          break;
        case 4: value = new Long(-1);
          break;
        case 5: value = folderPair.getFolderShareRecord().dateCreated;
          break;
        case 6: value = folderPair.getFolderShareRecord().dateUpdated;
          break;
        // Link ID
        case 7: value = folderPair.getFolderShareRecord().shareId;
          break;
        case 8: value = folderPair.getFolderRecord().folderId;
          break;
      }
    } else if (record instanceof MsgLinkRecord) {
      MsgLinkRecord msgLink = (MsgLinkRecord) record;

      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      MsgDataRecord msgData = forSortOnly ? cache.getMsgDataRecordNoTrace(msgLink.msgId) : cache.getMsgDataRecord(msgLink.msgId);

      switch (column) {
        case 0:
        boolean isStarred = msgLink.isStarred();
          boolean isFlagged = false;
          StatRecord stat = FetchedDataCache.getSingleInstance().getStatRecord(msgLink.msgLinkId, FetchedDataCache.STAT_TYPE_INDEX_MESSAGE);
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
        // Subject or Posting or Address Name
        case 1:
          value = ListRenderer.getRenderedText(msgData, false, false, false, false, true);
          break;
        // From
        case 2:
          if (msgData != null) {
            String fromEmailAddress = msgData.getFromEmailAddress();
            if (msgData.isEmail() || fromEmailAddress != null) {
              value = fromEmailAddress;
            } else {
              if (forSortOnly) {
                // use my contact list only, not the reciprocal contacts
                value = ListRenderer.getRenderedText(CacheUsrUtils.convertUserIdToFamiliarUser(msgData.senderUserId, true, false));
              } else {
                value = msgData.senderUserId;
              }
            }
          }
          break;
        // type
        case 3:
          if (msgData != null)
            value = msgData.isTypeAddress() ? "Address Contact" : (msgData.isTypeMessage() ? "Message" : "Other");
          break;
        // size
        case 4:
          if (msgData != null)
            value = msgData.recordSize;
          break;
        // Created
        case 5:
          if (msgData != null)
            value = msgData.dateCreated;
          break;
        // Updated
        case 6: value = msgLink.dateUpdated;
          break;
        // Link ID
        case 7: value = msgLink.msgLinkId;
          break;
        // Msg ID
        case 8: value = msgLink.msgId;
          break;
      }
    }

    return value;
  }

  public RecordTableCellRenderer createRenderer() {
    return new RecycleTableCellRenderer();
  }


  /**
  * Invoked by the cell editor when value in the column changes.
  */
  public void setValueAt(Object aValue, int row, int column) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleTableModel.class, "setValueAt(Object aValue, int row, int column)");
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

    if (trace != null) trace.exit(RecycleTableModel.class);
  }


  /**
  * Send a request to fetch files and msg briefs for the <code> shareId </code> from the server
  * if files were not fetched for this folder, otherwise get them from cache
  * @param force true to force a fetch from the database
  */
  private void fetchFilesAndMsgs(final Long shareId, final Long folderId, boolean force) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleTableModel.class, "fetchFilesAndMsgs(Long shareId, Long folderId, boolean force)");
    if (trace != null) trace.args(shareId, folderId);
    if (trace != null) trace.args(force);

    synchronized (fetchedIds) {
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      if (force || 
              !cache.wasFolderFetchRequestIssued(folderId) || 
              !fetchedIds.contains(shareId) || 
              (getFilterNarrowing() != null && !fetchedIdsFull.contains(shareId))) {

        // if folder previously fetched, remove file and msg links from the cache, leave the folders
        if (fetchedIds.contains(shareId)) {
          int rowCount = getRowCount();
          ArrayList fileLinksToRemove = new ArrayList();
          ArrayList msgLinksToRemove = new ArrayList();
          for (int row=0; row<rowCount; row++) {
            Record rec = getRowObjectNoTrace(row);
            if (rec instanceof FileLinkRecord) {
              fileLinksToRemove.add(rec);
            } else if (rec instanceof MsgLinkRecord) {
              msgLinksToRemove.add(rec);
            }
          }
          if (fileLinksToRemove.size() > 0) {
            FileLinkRecord[] fRecs = (FileLinkRecord[]) ArrayUtils.toArray(fileLinksToRemove, FileLinkRecord.class);
            fetchedIds.remove(shareId);
            fetchedIdsBriefs.remove(shareId);
            fetchedIdsFull.remove(shareId);
            cache.removeFileLinkRecords(fRecs);
          }
          if (msgLinksToRemove.size() > 0) {
            MsgLinkRecord[] mRecs = (MsgLinkRecord[]) ArrayUtils.toArray(msgLinksToRemove, MsgLinkRecord.class);
            fetchedIds.remove(shareId);
            fetchedIdsBriefs.remove(shareId);
            fetchedIdsFull.remove(shareId);
            cache.removeMsgLinkRecords(mRecs);
          }
        }

        // we should fetch only when we already have the folder-share pair, or they weren't already deleted
        if (cache.getFolderShareRecord(shareId) != null &&
            cache.getFolderRecord(folderId) != null &&
            !cache.getFolderRecord(folderId).isCategoryType()) {

          FolderRecord folder = cache.getFolderRecord(folderId);
          if (folder != null) cache.markFolderFetchRequestIssued(folder.folderId);

          {
            // Request Files
            // <shareId> <ownerObjType> <ownerObjId> <fetchNumMax> <timestamp>
            File_GetFiles_Rq request = new File_GetFiles_Rq(shareId, Record.RECORD_TYPE_FOLDER, folderId, (short) -File_GetFiles_Rq.FETCH_NUM_LIST__INITIAL_SIZE, (Timestamp) null);

            MessageAction msgAction = new MessageAction(CommandCodes.FILE_Q_GET_FILES_STAGED, request);
            Runnable replyReceivedJob = new Runnable() {
              public void run() {
                if (!fetchedIds.contains(shareId)) {
                  fetchedIds.add(shareId);
                }
              }
            };
            MainFrame.getServerInterfaceLayer().submitAndReturn(msgAction, 25000, replyReceivedJob, null, null);
          }
          {
            // Request Msgs
            // <shareId> <ownerObjType> <ownerObjId> <fetchNumMax> <fetchNumNew> <timestamp>
            Msg_GetMsgs_Rq request = new Msg_GetMsgs_Rq(shareId, Record.RECORD_TYPE_FOLDER, folderId, (short) -Msg_GetMsgs_Rq.FETCH_NUM_LIST__INITIAL_SIZE, (short) Msg_GetMsgs_Rq.FETCH_NUM_NEW__INITIAL_SIZE, (Timestamp) null);

            final int _action = getFilterNarrowing() != null ? CommandCodes.MSG_Q_GET_FULL : CommandCodes.MSG_Q_GET_BRIEFS;
            MessageAction msgAction = new MessageAction(_action, request);
            Runnable replyReceivedJob = new Runnable() {
              public void run() {
                FetchedDataCache cache = FetchedDataCache.getSingleInstance();
                if (!fetchedIds.contains(shareId)) fetchedIds.add(shareId);
                if (_action == CommandCodes.MSG_Q_GET_BRIEFS)
                  if (!fetchedIdsBriefs.contains(shareId)) fetchedIdsBriefs.add(shareId);
                if (_action == CommandCodes.MSG_Q_GET_FULL)
                  if (!fetchedIdsFull.contains(shareId)) fetchedIdsFull.add(shareId);
              }
            };
            MainFrame.getServerInterfaceLayer().submitAndReturn(msgAction, 25000, replyReceivedJob, null, null);
          }
        }
      }
    } // end synchronized

    if (trace != null) trace.exit(RecycleTableModel.class);
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
      javax.swing.SwingUtilities.invokeLater(new RecycleGUIUpdater(event));
    }
  }

  /** Listen on updates to the FolderShareRecords in the cache.
    * If the event happens, add, move or remove shares
    */
  private class FolderShareListener implements FolderShareRecordListener {
    public void folderShareRecordUpdated(FolderShareRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new RecycleGUIUpdater(event));
    }
  }

  /** Listen on updates to the FolderRecords in the cache.
    * If the event happens, set or remove records
    */
  private class FolderListener implements FolderRecordListener {
    public void folderRecordUpdated(FolderRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second, and to prevent gui tree deadlocks.
      javax.swing.SwingUtilities.invokeLater(new RecycleGUIUpdater(event));
    }
  }

  /** Listen on updates to the MsgLinkRecords in the cache.
    * if the event happens, add, move or remove messages
    */
  private class MsgLinkListener implements MsgLinkRecordListener {
    public void msgLinkRecordUpdated(MsgLinkRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new RecycleGUIUpdater(event));
    }
  }

  private class RecycleGUIUpdater implements Runnable {
    private RecordEvent event;
    public RecycleGUIUpdater(RecordEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleGUIUpdater.class, "RecycleGUIUpdater(RecordEvent event)");
      this.event = event;
      if (trace != null) trace.exit(RecycleGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleGUIUpdater.class, "RecycleGUIUpdater.run()");
      fileAndFolderAndMsgUpdate(event);
      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(RecycleGUIUpdater.class);
    }
  }

  /** Get the records and event type from the event and switch to appropriate methods
    * to set or remove these records.
    * Note: Removing shares is not supported
    */
  private void fileAndFolderAndMsgUpdate(RecordEvent event) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleTableModel.class, "fileAndFolderAndMsgUpdate(RecordEvent event)");
    if (trace != null) trace.args(event);

    Record[] recs = event.getRecords();
    FolderPair folderPair = getParentFolderPair();
    if (trace != null) trace.data(10, "folderPair", folderPair);
    if (trace != null) trace.data(11, "recs.length", recs.length);
    if (folderPair != null && recs != null && recs.length > 0) {

      // filter out only interested records
      ArrayList halfPairPicksL = new ArrayList();
      ArrayList linksPicksL = new ArrayList();
      Record[] halfPairPicks = null;
      FolderPair[] pairPicks = null;
      Record[] linksPicks = null;

      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      Long userId = cache.getMyUserId();

      for (int i=0; i<recs.length; i++) {
        Record rec = recs[i];
        if (rec instanceof FolderRecord) {
          halfPairPicksL.add(rec);
        } else if (rec instanceof FolderShareRecord) {
          FolderShareRecord sRec = (FolderShareRecord) rec;
          if (sRec.ownerUserId.equals(userId)) {
            halfPairPicksL.add(rec);
          }
        } else if (rec instanceof FileLinkRecord || rec instanceof MsgLinkRecord) {
          linksPicksL.add(rec);
        }
      }

      if (halfPairPicksL.size() > 0) {
        halfPairPicks = new Record[halfPairPicksL.size()];
        halfPairPicksL.toArray(halfPairPicks);
        pairPicks = CacheFldUtils.convertRecordsToPairs(halfPairPicks, event.getEventType() == RecordEvent.REMOVE);
      }

      if (linksPicksL.size() > 0) {
        linksPicks = new Record[linksPicksL.size()];
        linksPicksL.toArray(linksPicks);
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

    if (trace != null) trace.exit(RecycleTableModel.class);
  }

  protected void finalize() throws Throwable {
    setAutoUpdate(false);
    super.finalize();
  }

}
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

package com.CH_gui.fileTable;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;

import java.util.*;
import java.lang.reflect.Array;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.*;
import com.CH_cl.service.records.filters.*;

import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.file.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.msg.dataSets.stat.*;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.monitor.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.action.*;
import com.CH_gui.dialog.*;
import com.CH_gui.folder.*;
import com.CH_gui.frame.*;
import com.CH_gui.msgTable.*;
import com.CH_gui.table.*;
import com.CH_gui.tree.*;

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
 * <b>$Revision: 1.42 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FileActionTable extends RecordActionTable implements ActionProducerI {

  private Action[] actions;

  private static final int NUM_OF_SORT_COLUMNS = FileTableModel.columnHeaderData.data[ColumnHeaderData.I_SHORT_DISPLAY_NAMES].length;

  public static final int DOWNLOAD_ACTION = 0;
  private static final int COPY_ACTION = 1;
  private static final int MOVE_ACTION = 2;
  private static final int DELETE_ACTION = 3;
  private static final int PROPERTIES_ACTION = 4;
  private static final int FORWARD_ACTION = 5;
  public static final int REFRESH_ACTION = 6;

  private static final int MARK_AS_SEEN_ACTION = 7;
  private static final int MARK_AS_UNSEEN_ACTION = 8;
  private static final int MARK_ALL_SEEN_ACTION = 9;

  public static final int OPEN_IN_SEPERATE_WINDOW_ACTION = 10;
  private static final int TRACE_PRIVILEGE_AND_HISTORY_ACTION = 11;
  private static final int INVITE_ACTION = 12;
  private static final int OPEN_FILE_ACTION = 13;
  private static final int FILTER_ACTION = 14;

  private static final int SORT_ASC_ACTION = 15;
  private static final int SORT_DESC_ACTION = 16;
  private static final int SORT_BY_FIRST_COLUMN_ACTION = 17;
  private static final int CUSTOMIZE_COLUMNS_ACTION = SORT_BY_FIRST_COLUMN_ACTION + NUM_OF_SORT_COLUMNS;

  private static final int NUM_ACTIONS = CUSTOMIZE_COLUMNS_ACTION + 1;

  private int leadingActionId = Actions.LEADING_ACTION_ID_FILE_ACTION_TABLE;
  private int leadingFolderActionId = Actions.LEADING_ACTION_ID_FOLDER_ACTION_TREE;
  private int leadingMsgActionId = Actions.LEADING_ACTION_ID_MSG_ACTION_TABLE;
  private ServerInterfaceLayer SIL;

  private final EventListenerList folderSelectionListenerList = new EventListenerList();

  /** Creates new FileActionTable */
  public FileActionTable() {
    super(new FileTableModel(null), Fld_First_TableSorter.class);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileActionTable.class, "FileActionTable()");

    initActions();
    this.SIL = MainFrame.getServerInterfaceLayer();
    ((FileTableModel) getTableModel()).setAutoUpdate(true);

    addDND(getJSortedTable());
    addDND(getViewport());

    if (trace != null) trace.exit(FileActionTable.class);
  }

  public DragGestureListener createDragGestureListener() {
    return new FileDND_DragGestureListener(this);
  }
  public DropTargetListener createDropTargetListener() {
    return new FileDND_DropTargetListener(this);
  }


  private void initActions() {
    actions = new Action[NUM_ACTIONS];
    actions[DOWNLOAD_ACTION] = new DownloadAction(leadingActionId + DOWNLOAD_ACTION);

    actions[COPY_ACTION] = new CopyAction(leadingMsgActionId + MsgActionTable.COPY_ACTION);
    actions[MOVE_ACTION] = new MoveAction(leadingMsgActionId + MsgActionTable.MOVE_ACTION);
    actions[DELETE_ACTION] = new DeleteAction(leadingMsgActionId + MsgActionTable.DELETE_ACTION);

    actions[PROPERTIES_ACTION] = new PropertiesAction(leadingActionId + PROPERTIES_ACTION);
    actions[FORWARD_ACTION] = new ForwardToAction(leadingMsgActionId + MsgActionTable.FORWARD_ACTION);
    actions[REFRESH_ACTION] = new RefreshAction(leadingMsgActionId + MsgActionTable.REFRESH_ACTION);

    // Use the same actions for the menu as the message MARKs to keep the menu cleaner/simpler.
    actions[MARK_AS_SEEN_ACTION] = new MarkAsSeenAction(leadingMsgActionId + MsgActionTable.MARK_AS_READ_ACTION);
    actions[MARK_AS_UNSEEN_ACTION] = new MarkAsUnseenAction(leadingMsgActionId + MsgActionTable.MARK_AS_UNREAD_ACTION);
    actions[MARK_ALL_SEEN_ACTION] = new MarkAllSeenAction(leadingMsgActionId + MsgActionTable.MARK_ALL_READ_ACTION);

    actions[OPEN_IN_SEPERATE_WINDOW_ACTION] = new OpenInSeperateWindowAction(leadingActionId + OPEN_IN_SEPERATE_WINDOW_ACTION);
    actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION] = new TracePrivilegeAndHistoryAction(leadingMsgActionId + MsgActionTable.TRACE_PRIVILEGE_AND_HISTORY_ACTION);
    actions[INVITE_ACTION] = new InviteAction(leadingFolderActionId + FolderActionTree.INVITE_ACTION);
    actions[OPEN_FILE_ACTION] = new OpenFileAction(leadingMsgActionId + MsgActionTable.OPEN_IN_SEPERATE_VIEW);
    actions[FILTER_ACTION] = new FilterAction(leadingMsgActionId + MsgActionTable.FILTER_ACTION);

    {
      ButtonGroup sortAscDescGroup = new ButtonGroup();
      actions[SORT_ASC_ACTION] = new SortAscDescAction(true, 0+Actions.LEADING_ACTION_ID_RECORD_ACTION_TABLE, sortAscDescGroup);
      actions[SORT_DESC_ACTION] = new SortAscDescAction(false, 1+Actions.LEADING_ACTION_ID_RECORD_ACTION_TABLE, sortAscDescGroup);
      actions[CUSTOMIZE_COLUMNS_ACTION] = new CustomizeColumnsAction(2+Actions.LEADING_ACTION_ID_RECORD_ACTION_TABLE);
      ButtonGroup columnSortGroup = new ButtonGroup();
      for (int i=0; i<NUM_OF_SORT_COLUMNS; i++) {
        actions[SORT_BY_FIRST_COLUMN_ACTION+i] = new SortByColumnAction(i, i+3+Actions.LEADING_ACTION_ID_RECORD_ACTION_TABLE, columnSortGroup);
      }
    }
    setEnabledActions();
  }
  public Action getRefreshAction() {
    return actions[REFRESH_ACTION];
  }
  public Action getCloneAction() {
    return actions[OPEN_IN_SEPERATE_WINDOW_ACTION];
  }
  public Action getDoubleClickAction() {
    Action action = null;
    final FileRecord[] folders = getSelectedInstancesOf(FolderPair.class);
    if (folders != null && folders.length > 0 && folderSelectionListenerList.getListenerCount() > 0) {
      action = new AbstractAction() {
        public void actionPerformed(ActionEvent ae) {
          FolderRecord fRec = ((FolderPair)folders[0]).getFolderRecord();
          fireFolderSelectionChange(fRec);
        }
      };
    } else {
      FileRecord[] fileRecords = (FileRecord[]) getSelectedRecords();
      if (fileRecords != null && fileRecords.length == 1 && fileRecords[0] instanceof FileLinkRecord) {
        action = actions[OPEN_FILE_ACTION];
      } else {
        action = actions[DOWNLOAD_ACTION];
      }
    }
    return action;
  }
  public Action getFilterAction() {
    if (actions == null) initActions();
    return actions[FILTER_ACTION];
  }


  /**
   * @return all selected records, if there are none selected, return null
   * Runtime instance of the returned array is of FileRecord[].
   */
  public Record[] getSelectedRecords() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileActionTable.class, "getSelectedRecords()");
    FileRecord[] records = (FileRecord[]) ArrayUtils.toArray(getSelectedRecordsV(), FileRecord.class);
    if (trace != null) trace.exit(FileActionTable.class, records);
    return records;
  }

  /**
   * @return all selected FileRecords of specified class type (FolderPair or FileLinkRecord), or null if none
   */
  public FileRecord[] getSelectedInstancesOf(Class classType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileActionTable.class, "getSelectedInstancesOf(Class classType)");
    if (trace != null) trace.args(classType);

    FileRecord[] fileRecords = null;
    Vector recordsV = getSelectedRecordsV();
    if (recordsV != null && recordsV.size() > 0) {
      Vector selectedV = new Vector();
      for (int i=0; i<recordsV.size(); i++) {
        if (recordsV.elementAt(i).getClass().equals(classType)) {
          selectedV.addElement(recordsV.elementAt(i));
        }
      }
      if (selectedV.size() > 0) {
        fileRecords = (FileRecord[]) Array.newInstance(classType, selectedV.size());
        selectedV.toArray(fileRecords);
      }
    }

    if (trace != null) trace.exit(FileActionTable.class, fileRecords);
    return fileRecords;
  }


  // =====================================================================
  // LISTENERS FOR THE MENU ITEMS
  // =====================================================================

  /**
   * Open single file (no directories or multi selections)
   */
  private class OpenFileAction extends AbstractActionTraced {
    public OpenFileAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Open"), Images.get(ImageNums.CLONE_FILE16));
      putValue(Actions.ACTION_ID, Integer.valueOf(actionId));
      putValue(Actions.TOOL_TIP, "Transfer and Open remote file on local system using default file type association.");
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CLONE_FILE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Open"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      FileRecord[] fileRecords = (FileRecord[]) getSelectedRecords();
      if (fileRecords != null && fileRecords.length == 1 && fileRecords[0] instanceof FileLinkRecord) {
        openFile((FileLinkRecord) fileRecords[0]);
      }
    }
  }

  /**
   * Download a file/directory to the local system using DownloadUtilities to do all the work
   */
  private class DownloadAction extends AbstractActionTraced {
    public DownloadAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Download_..."), Images.get(ImageNums.IMPORT_FILE16));
      putValue(Actions.ACTION_ID, Integer.valueOf(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Download"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.IMPORT_FILE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Download"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FileRecord[] fileRecords = (FileRecord[]) getSelectedRecords();
      if (fileRecords != null && fileRecords.length > 0) {
        DownloadUtilities.downloadFilesChoice(fileRecords, null, FileActionTable.this, MainFrame.getServerInterfaceLayer());
      }
    }
  }

  private class CopyAction extends AbstractActionTraced {
    public CopyAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Copy_..."), Images.get(ImageNums.COPY16));
      putValue(Actions.ACTION_ID, Integer.valueOf(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Copy_selected_file_to_another_folder."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.COPY24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Copy"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FileLinkRecord[] fLinks = (FileLinkRecord[]) getSelectedInstancesOf(FileLinkRecord.class);
      FolderPair chosenFolderPair = getMoveCopyDestination(false, false, true);
      if (chosenFolderPair != null) {
        File_MoveCopy_Rq request = prepareMoveCopyRequest(chosenFolderPair.getFolderShareRecord(), fLinks);
        if (request != null) {
          SIL.submitAndReturn(new MessageAction(CommandCodes.FILE_Q_COPY_FILES, request));
        }
      }
    }
    private void updateText(int countSelectedFiles) {
      if (countSelectedFiles > 1) {
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Copy_selected_files_to_another_folder."));
      } else {
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Copy_selected_file_to_another_folder."));
      }
    }
  }

  private class MoveAction extends AbstractActionTraced {
    public MoveAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Move_..."), Images.get(ImageNums.FILE_MOVE16));
      putValue(Actions.ACTION_ID, Integer.valueOf(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Move_selected_file_to_another_folder."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FILE_MOVE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Move"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FileLinkRecord[] fLinks = (FileLinkRecord[]) getSelectedInstancesOf(FileLinkRecord.class);
      FolderPair[] fPairs = (FolderPair[]) getSelectedInstancesOf(FolderPair.class);
      FolderPair chosenFolderPair = getMoveCopyDestination(true, fPairs != null && fPairs.length > 0, fLinks != null && fLinks.length > 0);
      doMoveOrSaveAttachmentsAction(chosenFolderPair, fLinks, fPairs);
    }
    private void updateText(int countSelectedFiles) {
      if (countSelectedFiles > 1) {
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Move_selected_files_to_another_folder."));
      } else {
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Move_selected_file_to_another_folder."));
      }
    }
  }
  public static void doMoveOrSaveAttachmentsAction(FolderPair chosenFolderPair, FileLinkRecord[] fLinks, FolderPair[] fPairs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileActionTable.class, "doMoveOrSaveAttachmentsAction(FolderPair chosenFolderPair, FileLinkRecord[] fLinks, FolderPair[] fPairs)");
    if (trace != null) trace.args(chosenFolderPair, fLinks, fPairs);
    if (chosenFolderPair != null) {
      ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
      final FetchedDataCache cache = SIL.getFetchedDataCache();
      // move files
      File_MoveCopy_Rq request = prepareMoveCopyRequest(chosenFolderPair.getFolderShareRecord(), fLinks);
      if (request != null) { // fLinks can not be empty if request was generated
        int actionCode = CommandCodes.FILE_Q_MOVE_FILES;
        // if files are attachments, switch MOVE request to SAVE ATTACHMENTS request
        if (fLinks[0].ownerObjType.shortValue() == Record.RECORD_TYPE_MESSAGE)
          actionCode = CommandCodes.FILE_Q_SAVE_MSG_FILE_ATT;

        FolderRecord sourceFolder = null;
        if (fLinks[0].ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER)
          sourceFolder = cache.getFolderRecord(fLinks[0].ownerObjId);

        // create runnable to run after reply is received that will update new object count
        final FolderRecord _sourceFolder = sourceFolder;
        Runnable newCountUpdate = null;
        if (sourceFolder != null) {
          newCountUpdate = new Runnable() {
            public void run() {
              cache.statUpdatesInFoldersForVisualNotification(new FolderRecord[] { _sourceFolder });
            }
          };
        }

        // if Move then immediatelly remove links so that source GUI table responds FAST, when action comes back from server destination folder will update
        if (actionCode == CommandCodes.FILE_Q_MOVE_FILES && sourceFolder != null) {
          FolderShareRecord sourceShare = cache.getFolderShareRecordMy(sourceFolder.folderId, true);
          FolderShareRecord chosenShare = chosenFolderPair.getFolderShareRecord();
          if (sourceShare.canDelete.shortValue() == FolderShareRecord.YES && chosenShare.canWrite.shortValue() == FolderShareRecord.YES)
            cache.removeFileLinkRecords(fLinks);
        }

        SIL.submitAndReturn(new MessageAction(actionCode, request), 30000, newCountUpdate, newCountUpdate);
      }
      // move folders
      if (fPairs != null && fPairs.length > 0) {
        for (int i=0; i<fPairs.length; i++) {
          FolderPair fPair = fPairs[i];
          FolderRecord folderRecord = fPair.getFolderRecord();
          //// skip moving if destination folder equals source folder
          // Allow moving folders onto itself... this is rootifying or moving to 'Desktop'
          if (true || !chosenFolderPair.equals(folderRecord)) {
            Obj_IDPair_Co folderRequest = new Obj_IDPair_Co();
            folderRequest.objId_1 = folderRecord.folderId;
            folderRequest.objId_2 = chosenFolderPair.getId();

            // create runnable to run after reply is received that will update new object count
            FolderRecord sourceFolder = null;
            if (fLinks != null && fLinks.length > 0 && fLinks[0].ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
              sourceFolder = cache.getFolderRecord(fLinks[0].ownerObjId);
            }
            final FolderRecord _sourceFolder = sourceFolder;
            Runnable newCountUpdate = null;
            if (sourceFolder != null) {
              newCountUpdate = new Runnable() {
                public void run() {
                  cache.statUpdatesInFoldersForVisualNotification(new FolderRecord[] { _sourceFolder });
                }
              };
            }

            SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_MOVE_FOLDER, folderRequest), 30000, newCountUpdate, newCountUpdate);
          }
        }
      }
    }
    if (trace != null) trace.exit(FileActionTable.class);
  }

  private class DeleteAction extends AbstractActionTraced {
    public DeleteAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Delete_..."), Images.get(ImageNums.FILE_REMOVE16));
      putValue(Actions.ACTION_ID, Integer.valueOf(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Permanently_delete_selected_file."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FILE_REMOVE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Delete"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FileLinkRecord[] fileLinks = (FileLinkRecord[]) getSelectedInstancesOf(FileLinkRecord.class);
      FolderPair[] folderPairs = (FolderPair[]) getSelectedInstancesOf(FolderPair.class);

      if ((fileLinks != null && fileLinks.length > 0) || (folderPairs != null && folderPairs.length > 0)) {
        String title = com.CH_gui.lang.Lang.rb.getString("title_Delete_Confirmation");
        String messageText = "Are you sure you want to send these items to the Recycle Bin?";
        Record[] toDelete = RecordUtils.concatinate(folderPairs, fileLinks);
        boolean confirmed = MsgActionTable.showConfirmationDialog(FileActionTable.this, title, messageText, toDelete, MessageDialog.RECYCLE_MESSAGE, true);
        if (confirmed) {
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          FolderPair recycleFolderPair = CacheUtilities.convertRecordToPair(cache.getFolderRecord(cache.getUserRecord().recycleFolderId));
          doMoveOrSaveAttachmentsAction(recycleFolderPair, fileLinks, folderPairs);
        }

      }
    }
    private void updateText(int countSelectedFiles) {
      if (countSelectedFiles > 1) {
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Permanently_delete_selected_files."));
      } else {
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Permanently_delete_selected_file."));
      }
    }
  }

  private class PropertiesAction extends AbstractActionTraced {
    public PropertiesAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_File_Properties"));
      putValue(Actions.ACTION_ID, Integer.valueOf(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Show_General_Properties."));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FileLinkRecord[] fileLinks = (FileLinkRecord[]) getSelectedInstancesOf(FileLinkRecord.class);
      if (fileLinks != null && fileLinks.length == 1) {
        Window w = SwingUtilities.windowForComponent(FileActionTable.this);
        if (w instanceof Frame) new FilePropertiesDialog((Frame) w, fileLinks[0]);
        else if (w instanceof Dialog) new FilePropertiesDialog((Dialog) w, fileLinks[0]);
      }
      FolderPair[] folderPairs = (FolderPair[]) getSelectedInstancesOf(FolderPair.class);
      if (folderPairs != null && folderPairs.length == 1) {
        Window w = SwingUtilities.windowForComponent(FileActionTable.this);
        if (w instanceof Frame) new FolderPropertiesDialog((Frame) w, folderPairs[0]);
        else if (w instanceof Dialog) new FolderPropertiesDialog((Dialog) w, folderPairs[0]);
      }
    }
  }

  /**
   * Show the Message Compose frame with selected files as initial attachments.
   */
  private class ForwardToAction extends AbstractActionTraced {
    public ForwardToAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Forward_..."), Images.get(ImageNums.FORWARD16));
      putValue(Actions.ACTION_ID, Integer.valueOf(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Forward"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FORWARD24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Forward"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FileLinkRecord[] fileLinks = (FileLinkRecord[]) getSelectedInstancesOf(FileLinkRecord.class);
      if (fileLinks != null && fileLinks.length > 0) {
        new MessageFrame(null, fileLinks);
      }
    }
  }

  /**
   * Refresh File List.
   */
  private class RefreshAction extends AbstractActionTraced {
    public RefreshAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Refresh_Files"), Images.get(ImageNums.REFRESH16));
      putValue(Actions.ACTION_ID, Integer.valueOf(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Refresh_File_List_from_the_server."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.REFRESH24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Refresh"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      ((FileTableModel) getTableModel()).refreshData(true);
    }
  }


  /**
   * Mark selected items as SEEN
   */
  private class MarkAsSeenAction extends AbstractActionTraced {
    public MarkAsSeenAction (int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Mark_as_Seen"), Images.get(ImageNums.FLAG_BLANK_SMALL));
      putValue(Actions.ACTION_ID, Integer.valueOf(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Mark_all_selected_files_as_seen."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLAG_BLANK24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Seen"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      markSelectedAs(StatRecord.FLAG_OLD);
    }
  }

  /**
   * Mark selected items as UNSEEN
   */
  private class MarkAsUnseenAction extends AbstractActionTraced {
    public MarkAsUnseenAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Mark_as_Unseen"), Images.get(ImageNums.FLAG_GREEN_SMALL));
      putValue(Actions.ACTION_ID, Integer.valueOf(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Mark_all_selected_files_as_unseen."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLAG_GREEN24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Unseen"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      markSelectedAs(StatRecord.FLAG_NEW);
    }
  }

  /**
   * Mark all items in the folder as SEEN
   */
  private class MarkAllSeenAction extends AbstractActionTraced {
    public MarkAllSeenAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Mark_All_Seen"), Images.get(ImageNums.FLAG_BLANK_DOUBLE16));
      putValue(Actions.ACTION_ID, Integer.valueOf(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Mark_all_files_in_selected_folder_as_seen."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLAG_BLANK_DOUBLE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_All_Seen"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      markAllAs(StatRecord.FLAG_OLD);
    }
  }

  /**
   * Open in seperate window
   */
  private class OpenInSeperateWindowAction extends AbstractActionTraced {
    public OpenInSeperateWindowAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Clone_File_View"), Images.get(ImageNums.CLONE_FILE16));
      putValue(Actions.ACTION_ID, Integer.valueOf(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Display_file_table_in_its_own_window."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CLONE_FILE24));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FolderPair parentFolderPair = ((FileTableModel) getTableModel()).getParentFolderPair();
      if (parentFolderPair != null)
        new FileTableFrame(parentFolderPair);
    }
  }


  /**
   * Open history stat dialog for selected file link object.
   */
  private class TracePrivilegeAndHistoryAction extends AbstractActionTraced {
    public TracePrivilegeAndHistoryAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Trace_File_Access"));
      putValue(Actions.ACTION_ID, Integer.valueOf(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Trace_File_Access"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      Record[] selected = getSelectedRecords();
      if (selected != null && selected.length >= 1) {
        Window w = SwingUtilities.windowForComponent(FileActionTable.this);
        if (w instanceof Frame) new TraceRecordDialog((Frame) w, selected);
        else if (w instanceof Dialog) new TraceRecordDialog((Dialog) w, selected);
      }
    }
  }


  /**
   * Show selected Folder's sharing panel so user can add/invite others.
   */
  private class InviteAction extends AbstractActionTraced {
    public InviteAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Share_Folder_..."), Images.get(ImageNums.FLD_CLOSED_SHARED16, true));
      putValue(Actions.ACTION_ID, Integer.valueOf(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Share_Folder_..."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLD_CLOSED_SHARED24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Share"));
      putValue(Actions.IN_POPUP, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FolderPair fPair = FileActionTable.this.getTableModel().getParentFolderPair();
      boolean useSelected = isActionActivatedFromPopup(event);
      if (useSelected) {
        Record[] selectedRecords = getSelectedInstancesOf(FolderPair.class);
        if (selectedRecords != null) {
          if (selectedRecords.length > 1) {
            MessageDialog.showInfoDialog(FileActionTable.this, "Please select a single folder you would like to share.", com.CH_gui.lang.Lang.rb.getString("msgTitle_Invalid_Selection"), false);
            fPair = null;
          } else if (selectedRecords.length == 1 && selectedRecords[0] instanceof FolderPair) {
            fPair = (FolderPair) selectedRecords[0];
          }
        }
      }
      if (fPair != null && fPair.getFolderRecord().isCategoryType()) {
        Window w = SwingUtilities.windowForComponent(FileActionTable.this);
        String title = com.CH_gui.lang.Lang.rb.getString("title_Select_Folder_or_Group_to_Share");
        fPair = FolderActionTree.selectFolder(w, title, new MultiFilter(new RecordFilter[] { FolderFilter.MAIN_VIEW, FolderFilter.NON_LOCAL_FOLDERS }, MultiFilter.AND));
      }
      if (fPair != null) {
        Window w = SwingUtilities.windowForComponent(FileActionTable.this);
        if (w instanceof Frame) new FolderPropertiesDialog((Frame) w, fPair, 1);
        else if (w instanceof Dialog) new FolderPropertiesDialog((Dialog) w, fPair, 1);
      }
    }
  }


  /**
   * Open a single file (no folder) -- give option to OPEN or SAVE
   */
  private void openFile(final FileLinkRecord _fileLink) {
    // single file (no folder) download -- give option to OPEN or SAVE
    if (false && FileLauncher.isAudioWaveFilename(_fileLink.getFileName())) { // skip this and default to the dialog
      DownloadUtilities.downloadAndOpen(_fileLink, null, MainFrame.getServerInterfaceLayer(), true, true);
    } else {
      Runnable openTask = new Runnable() {
        public void run() {
          DownloadUtilities.downloadAndOpen(_fileLink, null, MainFrame.getServerInterfaceLayer(), true, false);
        }
      };
      Runnable saveTask = new Runnable() {
        public void run() {
          DownloadUtilities.downloadFilesChoice(new FileLinkRecord[] { _fileLink }, null, FileActionTable.this, MainFrame.getServerInterfaceLayer());
        }
      };
      Window w = SwingUtilities.windowForComponent(FileActionTable.this);
      if (w instanceof Frame)
        new OpenSaveCancelDialog((Frame) w, _fileLink, null, openTask, saveTask);
      else if (w instanceof Dialog)
        new OpenSaveCancelDialog((Dialog) w, _fileLink, null, openTask, saveTask);
    }
  }


  private void markSelectedAs(Short newMark) {
    FileLinkRecord[] records = (FileLinkRecord[]) getSelectedInstancesOf(FileLinkRecord.class);
    markRecordsAs(records, newMark);
  }
  private void markAllAs(Short newMark) {
    FileTableModel tableModel = (FileTableModel) getTableModel();
    Vector linksV = new Vector();
    for (int i=0; i<tableModel.getRowCount(); i++) {
      Record rec = tableModel.getRowObjectNoTrace(i);
      if (rec instanceof FileLinkRecord)
        linksV.addElement(rec);
    }
    if (linksV.size() > 0) {
      FileLinkRecord[] links = new FileLinkRecord[linksV.size()];
      linksV.toArray(links);
      markRecordsAs(links, newMark);
    }
  }
  private void markRecordsAs(FileLinkRecord[] records, Short newMark) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileActionTable.class, "markRecordsAs(FileLinkRecord[] records, Short newMark)");
    if (trace != null) trace.args(records, newMark);
    if (records != null && records.length > 0) {
      // gather all stats which need to be updated
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      Vector statsV = new Vector();
      for (int i=0; i<records.length; i++) {
        StatRecord statRecord = cache.getStatRecord(records[i].fileLinkId, FetchedDataCache.STAT_TYPE_FILE);
        if (statRecord != null && !statRecord.mark.equals(newMark))
          statsV.addElement(statRecord);
      }
      if (statsV.size() > 0) {
        StatRecord[] stats = new StatRecord[statsV.size()];
        statsV.toArray(stats);
        // clone the stats to send the request
        StatRecord[] statsClones = (StatRecord[]) RecordUtils.cloneRecords(stats);

        // set mark to "newMark" on the clones
        for (int i=0; i<statsClones.length; i++)
          statsClones[i].mark = newMark;

        Stats_Update_Rq request = new Stats_Update_Rq(statsClones);
        SIL.submitAndReturn(new MessageAction(CommandCodes.STAT_Q_UPDATE, request));
      }
    }
    if (trace != null) trace.exit(FileActionTable.class);
  }



  /**
   * Show a Move / Copy dialog and get the chosen destination FolderPair.
   */
  private FolderPair getMoveCopyDestination(boolean isMove, boolean forDirs, boolean forFiles) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileActionTable.class, "getMoveCopyDestination(boolean isMove, boolean forDirs, boolean forFiles)");
    if (trace != null) trace.args(isMove);
    if (trace != null) trace.args(forDirs);
    if (trace != null) trace.args(forFiles);

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    FolderRecord[] allFolderRecords = cache.getFolderRecords();
    FolderPair[] allFolderPairs = CacheUtilities.convertRecordsToPairs(allFolderRecords);
    allFolderPairs = (FolderPair[]) FolderFilter.MOVE_FOLDER.filterInclude(allFolderPairs);

    Window w = SwingUtilities.windowForComponent(this);

    String title = isMove ? com.CH_gui.lang.Lang.rb.getString("title_Move_to_Folder") : com.CH_gui.lang.Lang.rb.getString("title_Copy_to_Folder");
    FolderPair[] forbidenPairs = (FolderPair[]) getSelectedInstancesOf(FolderPair.class);

    // If we are not moving folders because there are no folder pairs selected, than desendants are OK.
    boolean isDescendantOk = true;
    // Since any folder type is permited to be a child of any other folder, remove descendant restriction.
    //if (forDirs)
    //  isDescendantOk = false;

    // If there are no selected folders, we are copying/moving files only and forbiden folder
    // will be the one that is parent to the selected file(s).
    if (forFiles) {
      FolderPair parentFolderPair = ((FileTableModel) getTableModel()).getParentFolderPair();
      forbidenPairs = (FolderPair[]) ArrayUtils.concatinate(forbidenPairs, new FolderPair[] { parentFolderPair });
    }

    // if moving/copying files, destination folder cannot be of non-file type
    if (forFiles)
      forbidenPairs = (FolderPair[]) ArrayUtils.concatinate(forbidenPairs, FolderFilter.NON_FILE_FOLDERS.filterInclude(allFolderPairs));


    Move_NewFld_Dialog d = null;
    if (w instanceof Frame) d = new Move_NewFld_Dialog((Frame) w, allFolderPairs, forbidenPairs, null, title, isDescendantOk, cache);
    else if (w instanceof Dialog) d = new Move_NewFld_Dialog((Dialog) w, allFolderPairs, forbidenPairs, null, title, isDescendantOk, cache);

    FolderPair chosenPair = null;
    if (d != null) {
      chosenPair = d.getChosenDestination();
    }

    if (trace != null) trace.exit(FileActionTable.class, chosenPair);
    return chosenPair;
  }

  /**
   * Prepares a request for currently selected FileLinks.
   */
  public static File_MoveCopy_Rq prepareMoveCopyRequest(FolderShareRecord destination, FileLinkRecord[] fLinks) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileActionTable.class, "prepareMoveCopyRequest(FolderShareRecord destination, FileLinkRecord[] fLinks)");
    if (trace != null) trace.args(destination, fLinks);

    File_MoveCopy_Rq request = null;

    if (fLinks != null && fLinks.length > 0) {
      request = new File_MoveCopy_Rq();
      request.toShareId = destination.shareId;

      // gather owners of given FileLinkRecords
      short ownerObjType = fLinks[0].ownerObjType.shortValue();
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      Long[] ownerObjIDs = FileLinkRecord.getOwnerObjIDs(fLinks, ownerObjType);
      ownerObjIDs = (Long[]) ArrayUtils.removeDuplicates(ownerObjIDs);

      // We are working with regular files in folders
      if (ownerObjType == Record.RECORD_TYPE_FOLDER) {
        Long[] folderIDs = ownerObjIDs;
        request.fromShareIDs = RecordUtils.getIDs(cache.getFolderSharesMyForFolders(folderIDs, true));
      }
      // We are working with message attachments
      else if (ownerObjType == Record.RECORD_TYPE_MESSAGE) {
        // Find some message links for the owner messages
        Vector msgLinksV = new Vector();
        for (int i=0; i<ownerObjIDs.length; i++) {
          MsgLinkRecord[] linksForMsg = cache.getMsgLinkRecordsForMsg(ownerObjIDs[i]);
          if (linksForMsg != null && linksForMsg.length > 0)
            msgLinksV.addElement(linksForMsg[0]);
        }
        MsgLinkRecord[] msgLinks = new MsgLinkRecord[msgLinksV.size()];
        msgLinksV.toArray(msgLinks);

        request.fromMsgLinkIDs = RecordUtils.getIDs(msgLinks);

        // if messages don't reside in folders without nested attachments, don't specify any shares
        Long[] folderIDs = MsgLinkRecord.getOwnerObjIDs(msgLinks, Record.RECORD_TYPE_FOLDER);
        request.fromShareIDs = RecordUtils.getIDs(cache.getFolderSharesMyForFolders(folderIDs, true));
        //request.fromShareIDs = RecordUtils.getIDs(cache.getFolderShareRecordsMyRootsForMsgs(msgLinks));
      }
      else {
        throw new IllegalArgumentException("Don't know how to handle owner type " + ownerObjType);
      }

      FileLinkRecord[] clonedLinks = (FileLinkRecord[]) RecordUtils.cloneRecords(fLinks);
      // give the new encrypted symmetric keys for the new destination folder
      BASymmetricKey destinationSymKey = destination.getSymmetricKey();
      for (int i=0; i<clonedLinks.length; i++) {
        clonedLinks[i].seal(destinationSymKey);
      }
      request.fileLinkRecords = clonedLinks;
    }

    if (trace != null) trace.exit(FileActionTable.class, request);
    return request;
  }

  /****************************************************************************/
  /*        A c t i o n P r o d u c e r I
  /****************************************************************************/

  /** @return all the acitons that this objects produces.
   */
  public Action[] getActions() {
    return actions;
  }

  public void setEnabledActions() {

    // Always enable Invite action
    actions[INVITE_ACTION].setEnabled(true);
    // Always enable Filter action
    actions[FILTER_ACTION].setEnabled(true);
    // Always enable sort actions
    actions[SORT_ASC_ACTION].setEnabled(true);
    actions[SORT_DESC_ACTION].setEnabled(true);
    actions[CUSTOMIZE_COLUMNS_ACTION].setEnabled(true);
    for (int i=SORT_BY_FIRST_COLUMN_ACTION; i<SORT_BY_FIRST_COLUMN_ACTION+NUM_OF_SORT_COLUMNS; i++)
      actions[i].setEnabled(true);

    FileRecord[] records = (FileRecord[]) getSelectedRecords();
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();

    int count = 0;
    Long totalSize = null;
    boolean noFolderPairs = true;
    if (records != null) {
      count = records.length;
      for (int i=0; i<count; i++) {
        if (records[i] instanceof FolderPair) {
          noFolderPairs = false;
          break;
        }
      }
    }

    // check for 'seen' and 'unseen' files
    boolean anySeen = false;
    boolean anyUnseen = false;

    boolean anyFileLink = false;

    if (records != null) {
      for (int i=0; i<records.length; i++) {
        if (records[i] instanceof FileLinkRecord) {
          anyFileLink = true;
          FileLinkRecord fLink = (FileLinkRecord) records[i];

          // count the file towards total selected size
          totalSize = Long.valueOf(fLink.origSize.longValue() + (totalSize != null ? totalSize.longValue() : 0));

          if (!(anySeen && anyUnseen)) {
            StatRecord statRecord = cache.getStatRecord(fLink.fileLinkId, FetchedDataCache.STAT_TYPE_FILE);
            if (statRecord != null) {
              if (statRecord.mark.equals(StatRecord.FLAG_OLD))
                anySeen = true;
              else if (statRecord.mark.equals(StatRecord.FLAG_NEW))
                anyUnseen = true;
            }
          }
        }
      }
    }

    boolean anyUnseenGlobal = anyUnseen;
    if (!anyUnseen) {
      FileTableModel tableModel = (FileTableModel) getTableModel();
      for (int i=0; i<tableModel.getRowCount(); i++) {
        FileRecord fRec = (FileRecord) tableModel.getRowObjectNoTrace(i);
        if (fRec instanceof FileLinkRecord) {
          FileLinkRecord fLink = (FileLinkRecord) fRec;
          StatRecord statRecord = cache.getStatRecord(fLink.fileLinkId, FetchedDataCache.STAT_TYPE_FILE);
          if (statRecord != null && statRecord.mark.equals(StatRecord.FLAG_NEW)) {
            anyUnseenGlobal = true;
            break;
          }
        }
      }
    }

    boolean isParentNonCategoryFolder = false;
    {
      RecordTableModel model = getTableModel();
      if (model != null) {
        FolderPair fPair = model.getParentFolderPair();
        if (fPair != null && fPair.getId() != null) {
          if (fPair.getFolderRecord() != null)
            isParentNonCategoryFolder = !fPair.getFolderRecord().isCategoryType();
        }
      }
    }

    if (count == 0) {
      actions[OPEN_FILE_ACTION].setEnabled(false);
      actions[DOWNLOAD_ACTION].setEnabled(false);
      actions[COPY_ACTION].setEnabled(false);
      actions[MOVE_ACTION].setEnabled(false);
      actions[DELETE_ACTION].setEnabled(false);
      actions[PROPERTIES_ACTION].setEnabled(false);
      actions[FORWARD_ACTION].setEnabled(false);
      actions[MARK_AS_SEEN_ACTION].setEnabled(false);
      actions[MARK_AS_UNSEEN_ACTION].setEnabled(false);
      actions[MARK_ALL_SEEN_ACTION].setEnabled(anyUnseenGlobal);
      actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION].setEnabled(false);
    } else if (count == 1) {
      actions[OPEN_FILE_ACTION].setEnabled(noFolderPairs);
      actions[DOWNLOAD_ACTION].setEnabled(true);
      actions[COPY_ACTION].setEnabled(noFolderPairs);
      actions[MOVE_ACTION].setEnabled(true);
      actions[DELETE_ACTION].setEnabled(true);
      actions[PROPERTIES_ACTION].setEnabled(true);
      actions[FORWARD_ACTION].setEnabled(noFolderPairs);
      actions[MARK_AS_SEEN_ACTION].setEnabled(anyUnseen);
      actions[MARK_AS_UNSEEN_ACTION].setEnabled(anySeen);
      actions[MARK_ALL_SEEN_ACTION].setEnabled(anyUnseenGlobal);
      actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION].setEnabled(true);
    } else if (count > 1) {
      actions[OPEN_FILE_ACTION].setEnabled(false);
      actions[DOWNLOAD_ACTION].setEnabled(true);
      actions[COPY_ACTION].setEnabled(noFolderPairs);
      actions[MOVE_ACTION].setEnabled(true);
      actions[DELETE_ACTION].setEnabled(true);
      actions[PROPERTIES_ACTION].setEnabled(false);
      actions[FORWARD_ACTION].setEnabled(noFolderPairs);
      actions[MARK_AS_SEEN_ACTION].setEnabled(anyUnseen);
      actions[MARK_AS_UNSEEN_ACTION].setEnabled(anySeen);
      actions[MARK_ALL_SEEN_ACTION].setEnabled(anyUnseenGlobal);
      actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION].setEnabled(true);
    }

    Window w = SwingUtilities.windowForComponent(this);
    actions[OPEN_IN_SEPERATE_WINDOW_ACTION].setEnabled(isParentNonCategoryFolder && w != null);
    actions[REFRESH_ACTION].setEnabled(isParentNonCategoryFolder && w != null);

    CopyAction copyAction = (CopyAction) actions[COPY_ACTION];
    MoveAction moveAction = (MoveAction) actions[MOVE_ACTION];
    DeleteAction deleteAction = (DeleteAction) actions[DELETE_ACTION];

    copyAction.updateText(count);
    moveAction.updateText(count);
    deleteAction.updateText(count);

    if (noFolderPairs)
      Stats.setSize(totalSize != null ? totalSize.longValue() : -1);
  }



  /*************************************************
   ***   Folder double-click Listener handling   ***
   ************************************************/

  public void addFolderSelectionListener(FolderSelectionListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileActionTable.class, "addFolderSelectionListener(FolderSelectionListener)");
    if (trace != null) trace.args(l);

    synchronized (folderSelectionListenerList) {
      folderSelectionListenerList.add(FolderSelectionListener.class, l);
    }

    if (trace != null) trace.exit(FileActionTable.class);
  }

  public void removeFolderSelectionListener(FolderSelectionListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileActionTable.class, "removeFolderSelectionListener(FolderSelectionListener)");
    if (trace != null) trace.args(l);

    synchronized (folderSelectionListenerList) {
      folderSelectionListenerList.remove(FolderSelectionListener.class, l);
    }

    if (trace != null) trace.exit(FileActionTable.class);
  }


  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   */
  protected void fireFolderSelectionChange(FolderRecord selectedFolder) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileActionTable.class, "fireFolderSelectionChange(FolderRecord)");
    if (trace != null) trace.args(selectedFolder);

    Vector toNotifyV = null;
    FolderSelectionEvent e = null;
    synchronized (folderSelectionListenerList) {
      // Guaranteed to return a non-null array
      Object[] listeners = folderSelectionListenerList.getListenerList();
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
        if (listeners[i] == FolderSelectionListener.class) {
          // Lazily create the event:
          if (e == null)
            e = new FolderSelectionEvent(this, selectedFolder);
          if (toNotifyV == null)
            toNotifyV = new Vector();
          toNotifyV.addElement(listeners[i+1]);
        }
      }
    }

    if (toNotifyV != null && toNotifyV.size() > 0) {
      int oldPriority = Thread.currentThread().getPriority();
      Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
      for (int i=0; i<toNotifyV.size(); i++) {
        ((FolderSelectionListener)toNotifyV.elementAt(i)).folderSelectionChanged(e);
      }
      Thread.currentThread().setPriority(oldPriority);
    }

    if (trace != null) trace.exit(FileActionTable.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "FileActionTable";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
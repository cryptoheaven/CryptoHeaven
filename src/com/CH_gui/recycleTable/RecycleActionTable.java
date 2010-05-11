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

package com.CH_gui.recycleTable;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.dnd.*;
import java.awt.event.*;

import java.util.*;
import java.lang.reflect.Array;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.ops.*;
import com.CH_cl.service.records.filters.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.monitor.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.action.*;
import com.CH_gui.dialog.*;
import com.CH_gui.fileTable.*;
import com.CH_gui.folder.*;
import com.CH_gui.frame.*;
import com.CH_gui.msgTable.*;
import com.CH_gui.service.ops.DownloadUtilsGui;
import com.CH_gui.table.*;
import com.CH_gui.tree.*;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;

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
 * <b>$Revision: 1.3 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class RecycleActionTable extends RecordActionTable implements ActionProducerI {

  private Action[] actions;

  private static final int NUM_OF_SORT_COLUMNS = RecycleTableModel.columnHeaderData.data[ColumnHeaderData.I_SHORT_DISPLAY_NAMES].length;

  private static final int DOWNLOAD_ACTION = 0;
  private static final int COPY_ACTION = 1;
  private static final int MOVE_ACTION = 2;
  private static final int DELETE_ACTION = 3;
  private static final int PROPERTIES_ACTION = 4;
  private static final int FORWARD_ACTION = 5;
  public static final int REFRESH_ACTION = 6;

  private static final int MARK_AS_SEEN_ACTION = 7;
  private static final int MARK_AS_UNSEEN_ACTION = 8;
  private static final int MARK_ALL_SEEN_ACTION = 9;

  private static final int OPEN_FILES_IN_SEPERATE_WINDOW_ACTION = 10;
  private static final int TRACE_PRIVILEGE_AND_HISTORY_ACTION = 11;
  private static final int INVITE_ACTION = 12;
  private static final int OPEN_FILE_ACTION = 13;
  private static final int OPEN_MSGS_IN_SEPERATE_WINDOW_ACTION = 14;
  private static final int EMPTY_FOLDER_ACTION = 15;
  private static final int OPEN_RECYCLE_BIN_IN_SEPERATE_WINDOW_ACTION = 16;
  private static final int FILTER_ACTION = 17;

  private static final int SORT_ASC_ACTION = 18;
  private static final int SORT_DESC_ACTION = 19;
  private static final int SORT_BY_FIRST_COLUMN_ACTION = 20;
  private static final int CUSTOMIZE_COLUMNS_ACTION = SORT_BY_FIRST_COLUMN_ACTION + NUM_OF_SORT_COLUMNS;

  private static final int NUM_ACTIONS = CUSTOMIZE_COLUMNS_ACTION + 1;

  private int leadingActionId = Actions.LEADING_ACTION_ID_RECYCLE_ACTION_TABLE;
  private int leadingFileActionId = Actions.LEADING_ACTION_ID_FILE_ACTION_TABLE; // most actions are duplicates of file table actions
  private int leadingFolderActionId = Actions.LEADING_ACTION_ID_FOLDER_ACTION_TREE;
  private int leadingMsgActionId = Actions.LEADING_ACTION_ID_MSG_ACTION_TABLE;

  private final EventListenerList folderSelectionListenerList = new EventListenerList();

  /** Creates new RecycleActionTable */
  public RecycleActionTable() {
    super(new RecycleTableModel(null), Fld_First_TableSorter.class);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleActionTable.class, "RecycleActionTable()");

    initActions();
    ((RecycleTableModel) getTableModel()).setAutoUpdate(true);

    addDND(getJSortedTable());
    addDND(getViewport());

    if (trace != null) trace.exit(RecycleActionTable.class);
  }

  public DragGestureListener createDragGestureListener() {
    return new RecycleDND_DragGestureListener(this);
  }
  public DropTargetListener createDropTargetListener() {
    return new RecycleDND_DropTargetListener(this);
  }


  private void initActions() {
    actions = new Action[NUM_ACTIONS];
    actions[DOWNLOAD_ACTION] = new DownloadAction(leadingFileActionId + FileActionTable.DOWNLOAD_ACTION);

    //actions[COPY_ACTION] = new CopyAction(leadingMsgActionId + MsgActionTable.COPY_ACTION);
    actions[MOVE_ACTION] = new MoveAction(leadingMsgActionId + MsgActionTable.MOVE_ACTION);
    actions[DELETE_ACTION] = new DeleteAction(leadingMsgActionId + MsgActionTable.DELETE_ACTION);

    actions[PROPERTIES_ACTION] = new PropertiesAction(leadingFileActionId + PROPERTIES_ACTION);
    actions[FORWARD_ACTION] = new ForwardToAction(leadingMsgActionId + MsgActionTable.FORWARD_ACTION);
    actions[REFRESH_ACTION] = new RefreshAction(leadingMsgActionId + MsgActionTable.REFRESH_ACTION);

//    // Use the same actions for the menu as the message MARKs to keep the menu cleaner/simpler.
//    actions[MARK_AS_SEEN_ACTION] = new MarkAsSeenAction(leadingMsgActionId + MsgActionTable.MARK_AS_READ_ACTION);
//    actions[MARK_AS_UNSEEN_ACTION] = new MarkAsUnseenAction(leadingMsgActionId + MsgActionTable.MARK_AS_UNREAD_ACTION);
//    actions[MARK_ALL_SEEN_ACTION] = new MarkAllSeenAction(leadingMsgActionId + MsgActionTable.MARK_ALL_READ_ACTION);

    actions[OPEN_FILES_IN_SEPERATE_WINDOW_ACTION] = new OpenFilesInSeperateWindowAction(leadingFileActionId + FileActionTable.OPEN_IN_SEPERATE_WINDOW_ACTION);
    actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION] = new TracePrivilegeAndHistoryAction(leadingMsgActionId + MsgActionTable.TRACE_PRIVILEGE_AND_HISTORY_ACTION);
    actions[INVITE_ACTION] = new InviteAction(leadingFolderActionId + FolderActionTree.INVITE_ACTION);
    actions[OPEN_FILE_ACTION] = new OpenFileAction(leadingMsgActionId + MsgActionTable.OPEN_IN_SEPERATE_VIEW);
    actions[OPEN_MSGS_IN_SEPERATE_WINDOW_ACTION] = new OpenMsgsInSeperateWindowAction(leadingMsgActionId + MsgActionTable.OPEN_IN_SEPERATE_WINDOW_ACTION);
    actions[EMPTY_FOLDER_ACTION] = new EmptyFolderAction(leadingActionId + EMPTY_FOLDER_ACTION);
    actions[OPEN_RECYCLE_BIN_IN_SEPERATE_WINDOW_ACTION] = new OpenRecycleBinInSeperateWindowAction(leadingActionId + OPEN_RECYCLE_BIN_IN_SEPERATE_WINDOW_ACTION);
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
    return actions[OPEN_RECYCLE_BIN_IN_SEPERATE_WINDOW_ACTION];
  }
  public Action getDoubleClickAction() {
    Action action = null;
    final Record[] folders = (Record[]) getSelectedInstancesOf(FolderPair.class);
    if (folders != null && folders.length > 0 && folderSelectionListenerList.getListenerCount() > 0) {
      action = new AbstractAction() {
        public void actionPerformed(ActionEvent ae) {
          FolderRecord fRec = ((FolderPair)folders[0]).getFolderRecord();
          fireFolderSelectionChange(fRec);
        }
      };
    } else {
      Record[] records = (Record[]) getSelectedRecords();
      if (records != null && records.length == 1 && (records[0] instanceof FileLinkRecord || records[0] instanceof MsgLinkRecord)) {
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
   * @return all selected Records of specified class type (FolderPair or FileLinkRecord or MsgLinkRecord), or null if none
   */
  public Object[] getSelectedInstancesOf(Class classType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleActionTable.class, "getSelectedInstancesOf(Class classType)");
    if (trace != null) trace.args(classType);

    Object[] records = null;
    List recordsL = getSelectedRecordsL();
    if (recordsL != null && recordsL.size() > 0) {
      Vector selectedV = new Vector();
      for (int i=0; i<recordsL.size(); i++) {
        if (classType.isInstance(recordsL.get(0))) { // check if classType specified is equivalent or superclass (or super interface) of record element
        //if (recordsV.elementAt(i).getClass().equals(classType)) {
          selectedV.addElement(recordsL.get(i));
        }
      }
      if (selectedV.size() > 0) {
        records = (Object[]) Array.newInstance(classType, selectedV.size());
        selectedV.toArray(records);
      }
    }

    if (trace != null) trace.exit(RecycleActionTable.class, records);
    return records;
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
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, "Transfer and Open remote file on local system using default file type association.");
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CLONE_FILE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Open"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      FileLinkRecord[] fileRecords = (FileLinkRecord[]) getSelectedInstancesOf(FileLinkRecord.class);
      if (fileRecords != null && fileRecords.length == 1) {
        openFile(fileRecords[0]);
      }
      MsgLinkRecord[] msgRecords = (MsgLinkRecord[]) getSelectedInstancesOf(MsgLinkRecord.class);
      if (msgRecords != null && msgRecords.length > 0) {
        new MsgPreviewFrame(RecycleActionTable.this.getTableModel().getParentFolderPair(), msgRecords, msgRecords.length == 1 ? RecycleActionTable.this : null);
      }
    }
  }

  /** 
   * Download a file/directory to the local system using DownloadUtilities to do all the work 
   */
  private class DownloadAction extends AbstractActionTraced {
    public DownloadAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Download_..."), Images.get(ImageNums.IMPORT_FILE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Download"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.IMPORT_FILE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Download"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FileRecord[] fileRecords = (FileRecord[]) getSelectedInstancesOf(FileRecord.class);
      if (fileRecords != null && fileRecords.length > 0) {
        DownloadUtilsGui.downloadFilesChoice(fileRecords, null, RecycleActionTable.this, MainFrame.getServerInterfaceLayer());
      }
      MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) getSelectedInstancesOf(MsgLinkRecord.class);
      if (msgLinks != null && msgLinks.length > 0) {
        DownloadUtilsGui.downloadFilesChoice(msgLinks, null, RecycleActionTable.this, MainFrame.getServerInterfaceLayer());
      }
    }
  }

// Copy action is kind-of useless in Recycle Bin
//  private class CopyAction extends AbstractActionTraced {
//    public CopyAction(int actionId) {
//      super(com.CH_gui.lang.Lang.rb.getString("action_Copy_..."), Images.get(ImageNums.COPY16));
//      putValue(Actions.ACTION_ID, new Integer(actionId));
//      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Copy_selected_file_to_another_folder."));
//      putValue(Actions.TOOL_ICON, Images.get(ImageNums.COPY24));
//      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
//    }
//    public void actionPerformedTraced(ActionEvent event) {
//      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CopyAction.class, "actionPerformed(ActionEvent event)");
//      if (trace != null) trace.args(event);
//      FileLinkRecord[] fLinks = (FileLinkRecord[]) getSelectedInstancesOf(FileLinkRecord.class);
//      MsgLinkRecord[] mLinks = (MsgLinkRecord[]) getSelectedInstancesOf(MsgLinkRecord.class);
//      boolean forFiles = fLinks != null && fLinks.length > 0;
//      boolean forMsgs = mLinks != null && mLinks.length > 0;
//      FolderPair chosenFolderPair = getMoveCopyDestination(false, false, forFiles, forMsgs);
//      if (chosenFolderPair != null) {
//        if (forFiles) {
//          File_MoveCopy_Rq request = FileActionTable.prepareMoveCopyRequest(chosenFolderPair.getFolderShareRecord(), fLinks);
//          if (request != null) {
//            serverInterfaceLayer.submitAndReturn(new MessageAction(CommandCodes.FILE_Q_COPY_FILES, request));
//          }
//        }
//        if (forMsgs) {
//          Msg_MoveCopy_Rq request = MsgActionTable.prepareMoveCopyRequest(chosenFolderPair.getFolderShareRecord(), mLinks);
//          if (request != null) {
//            serverInterfaceLayer.submitAndReturn(new MessageAction(CommandCodes.MSG_Q_COPY, request));
//          }
//        }
//      }
//      if (trace != null) trace.exit(CopyAction.class);
//    }
//  }

  private class MoveAction extends AbstractActionTraced {
    public MoveAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Move_..."), Images.get(ImageNums.FILE_MOVE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Move_selected_file_to_another_folder."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FILE_MOVE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Move"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FolderPair[] fPairs = (FolderPair[]) getSelectedInstancesOf(FolderPair.class);
      FileLinkRecord[] fLinks = (FileLinkRecord[]) getSelectedInstancesOf(FileLinkRecord.class);
      MsgLinkRecord[] mLinks = (MsgLinkRecord[]) getSelectedInstancesOf(MsgLinkRecord.class);
      boolean forDirs = fPairs != null && fPairs.length > 0;
      boolean forFiles = fLinks != null && fLinks.length > 0;
      boolean forMsgs = mLinks != null && mLinks.length > 0;
      FolderPair chosenFolderPair = getMoveCopyDestination(true, forDirs, forFiles, forMsgs);
      FileActionTable.doMoveOrSaveAttachmentsAction(chosenFolderPair, fLinks, fPairs);
      MsgActionTable.doMoveOrCopyOrSaveAttachmentsAction(true, chosenFolderPair, mLinks);
    }
  }

  private class DeleteAction extends AbstractActionTraced {
    public DeleteAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Delete_..."), Images.get(ImageNums.FILE_REMOVE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Permanently_delete_selected_files."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FILE_REMOVE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Delete"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FileLinkRecord[] fileLinks = (FileLinkRecord[]) getSelectedInstancesOf(FileLinkRecord.class);
      FolderPair[] folderPairs = (FolderPair[]) getSelectedInstancesOf(FolderPair.class);
      MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) getSelectedInstancesOf(MsgLinkRecord.class);

      doDeleteAction(folderPairs, fileLinks, msgLinks, RecycleActionTable.this);
    }
  }

  private class PropertiesAction extends AbstractActionTraced {
    public PropertiesAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Properties"));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Show_General_Properties."));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FileLinkRecord[] fileLinks = (FileLinkRecord[]) getSelectedInstancesOf(FileLinkRecord.class);
      if (fileLinks != null && fileLinks.length == 1) {
        Window w = SwingUtilities.windowForComponent(RecycleActionTable.this);
        if (w instanceof Frame) new FilePropertiesDialog((Frame) w, fileLinks[0]);
        else if (w instanceof Dialog) new FilePropertiesDialog((Dialog) w, fileLinks[0]);
      }
      FolderPair[] folderPairs = (FolderPair[]) getSelectedInstancesOf(FolderPair.class);
      if (folderPairs != null && folderPairs.length == 1) {
        Window w = SwingUtilities.windowForComponent(RecycleActionTable.this);
        if (w instanceof Frame) new FolderPropertiesDialog((Frame) w, folderPairs[0]);
        else if (w instanceof Dialog) new FolderPropertiesDialog((Dialog) w, folderPairs[0]);
      }
      MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) getSelectedInstancesOf(MsgLinkRecord.class);
      if (msgLinks != null && msgLinks.length == 1) {
        Window w = SwingUtilities.windowForComponent(RecycleActionTable.this);
        if (w instanceof Frame) new MsgPropertiesDialog((Frame) w, msgLinks[0]);
        else if (w instanceof Dialog) new MsgPropertiesDialog((Dialog) w, msgLinks[0]);
      }
    }
  }

  /**
   * Show the Message Compose frame with selected files and messaged as initial attachments.
   */
  private class ForwardToAction extends AbstractActionTraced {
    public ForwardToAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Forward_..."), Images.get(ImageNums.FORWARD16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Forward"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FORWARD24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Forward"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      LinkRecordI[] links = (LinkRecordI[]) getSelectedInstancesOf(LinkRecordI.class);
      if (links != null && links.length > 0) {
        new MessageFrame(null, links);
      }
    }
  }

  /**
   * Refresh List.
   */
  private class RefreshAction extends AbstractActionTraced {
    public RefreshAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Refresh"), Images.get(ImageNums.REFRESH16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Refresh_List_from_the_server."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.REFRESH24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Refresh"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      ((RecycleTableModel) getTableModel()).refreshData(true);
    }
  }


//  /**
//   * Mark selected items as SEEN
//   */
//  private class MarkAsSeenAction extends AbstractActionTraced {
//    public MarkAsSeenAction (int actionId) {
//      super(com.CH_gui.lang.Lang.rb.getString("action_Mark_as_Seen"), Images.get(ImageNums.FLAG_BLANK13_15));
//      putValue(Actions.ACTION_ID, new Integer(actionId));
//      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Mark_all_selected_files_as_seen."));
//      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLAG_BLANK24));
//      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
//    }
//    public void actionPerformedTraced(ActionEvent event) {
//      markSelectedAs(StatRecord.MARK_OLD);
//    }
//  }
//
//  /**
//   * Mark selected items as UNSEEN
//   */
//  private class MarkAsUnseenAction extends AbstractActionTraced {
//    public MarkAsUnseenAction(int actionId) {
//      super(com.CH_gui.lang.Lang.rb.getString("action_Mark_as_Unseen"), Images.get(ImageNums.FLAG_GREEN13_15));
//      putValue(Actions.ACTION_ID, new Integer(actionId));
//      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Mark_all_selected_files_as_unseen."));
//      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLAG_GREEN24));
//      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
//    }
//    public void actionPerformedTraced(ActionEvent event) {
//      markSelectedAs(StatRecord.MARK_NEW);
//    }
//  }
//
//  /**
//   * Mark all items in the folder as SEEN
//   */
//  private class MarkAllSeenAction extends AbstractActionTraced {
//    public MarkAllSeenAction(int actionId) {
//      super(com.CH_gui.lang.Lang.rb.getString("action_Mark_All_Seen"), Images.get(ImageNums.FLAG_BLANK_DOUBLE16));
//      putValue(Actions.ACTION_ID, new Integer(actionId));
//      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Mark_all_files_in_selected_folder_as_seen."));
//      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLAG_BLANK_DOUBLE24));
//      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
//    }
//    public void actionPerformedTraced(ActionEvent event) {
//      markAllAs(StatRecord.MARK_OLD);
//    }
//  }

  /**
   * Open Files in seperate window
   */
  private class OpenFilesInSeperateWindowAction extends AbstractActionTraced {
    public OpenFilesInSeperateWindowAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Clone_File_View"), Images.get(ImageNums.CLONE_FILE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Display_file_table_in_its_own_window."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CLONE_FILE24));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FolderPair parentFolderPair = ((RecycleTableModel) getTableModel()).getParentFolderPair();
      if (parentFolderPair != null)
        new FileTableFrame(parentFolderPair);
    }
  }

  /**
   * Open Msgs in seperate window
   */
  private class OpenMsgsInSeperateWindowAction extends AbstractActionTraced {
    public OpenMsgsInSeperateWindowAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Clone_Message_View"), Images.get(ImageNums.CLONE_MSG16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Display_message_table_in_its_own_window."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CLONE_MSG24));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FolderPair parentFolderPair = ((RecycleTableModel) getTableModel()).getParentFolderPair();
      if (parentFolderPair != null)
        new MsgTableFrame(parentFolderPair);
    }
  }
  
  /**
   * Open Recycle Bin in seperate window
   */
  private class OpenRecycleBinInSeperateWindowAction extends AbstractActionTraced {
    public OpenRecycleBinInSeperateWindowAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Clone_Recycle_Bin_View"), Images.get(ImageNums.CLONE_FILE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Display_Recycle_Bin_in_its_own_window."));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.IN_MENU, Boolean.FALSE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FolderPair parentFolderPair = ((RecycleTableModel) getTableModel()).getParentFolderPair();
      if (parentFolderPair != null)
        new RecycleTableFrame(parentFolderPair);
    }
  }

  /**
   * Empty Folder Action
   */
  private class EmptyFolderAction extends AbstractActionTraced {
    public EmptyFolderAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Empty_Folder_..."), Images.get(ImageNums.FLD_RECYCLE_CLEAR16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Empty_Folder_..."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLD_RECYCLE_CLEAR24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Empty"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.IN_MENU, Boolean.FALSE);
      //putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FolderPair folderToEmpty = RecycleActionTable.this.getTableModel().getParentFolderPair();
      FolderActionTree.doEmptyAction(folderToEmpty, true, RecycleActionTable.this);
    }
  }

  /**
   * Open history stat dialog for selected file link object.
   */
  private class TracePrivilegeAndHistoryAction extends AbstractActionTraced {
    public TracePrivilegeAndHistoryAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Trace_Access"));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Trace_Access"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      Record[] selected = getSelectedRecords();
      if (selected != null && selected.length >= 1) {
        Window w = SwingUtilities.windowForComponent(RecycleActionTable.this);
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
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Share_Folder_..."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLD_CLOSED_SHARED24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Share"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      FolderPair fPair = RecycleActionTable.this.getTableModel().getParentFolderPair();
      boolean useSelected = isActionActivatedFromPopup(event);
      if (useSelected) {
        Record[] selectedRecords = (Record[]) getSelectedInstancesOf(FolderPair.class);
        if (selectedRecords != null) {
          if (selectedRecords.length > 1) {
            MessageDialog.showInfoDialog(RecycleActionTable.this, "Please select a single folder you would like to share.", com.CH_gui.lang.Lang.rb.getString("msgTitle_Invalid_Selection"), false);
            fPair = null;
          } else if (selectedRecords.length == 1 && selectedRecords[0] instanceof FolderPair) {
            fPair = (FolderPair) selectedRecords[0];
          }
        }
      }
      if (fPair != null && fPair.getFolderRecord().isCategoryType()) {
        Window w = SwingUtilities.windowForComponent(RecycleActionTable.this);
        String title = com.CH_gui.lang.Lang.rb.getString("title_Select_Folder_or_Group_to_Share");
        fPair = FolderActionTree.selectFolder(w, title, new MultiFilter(new RecordFilter[] { FolderFilter.MAIN_VIEW, FolderFilter.NON_LOCAL_FOLDERS }, MultiFilter.AND));
      }
      if (fPair != null) {
        Window w = SwingUtilities.windowForComponent(RecycleActionTable.this);
        if (w instanceof Frame) new FolderPropertiesDialog((Frame) w, fPair, 1);
        else if (w instanceof Dialog) new FolderPropertiesDialog((Dialog) w, fPair, 1);
      }
    }
  }


  /**
   * Delete specified files and folders, present a confirmation dialog if desired
   */
  private static void doDeleteAction(FolderPair[] folderPairs, FileLinkRecord[] fileLinks, MsgLinkRecord[] msgLinks, Component parent) {
    if ((fileLinks != null && fileLinks.length > 0) || (folderPairs != null && folderPairs.length > 0) || (msgLinks != null && msgLinks.length > 0)) {

      boolean confirmed = false;

      String title = com.CH_gui.lang.Lang.rb.getString("title_Delete_Confirmation");
      String messageText = com.CH_gui.lang.Lang.rb.getString("msg_Are_you_sure_you_want_to_delete_the_following_object(s)?");

      Record[] toDelete = RecordUtils.concatinate(folderPairs, fileLinks);
      toDelete = RecordUtils.concatinate(toDelete, msgLinks);
      confirmed = MsgActionTable.showConfirmationDialog(parent, title, messageText, toDelete, MessageDialog.DELETE_MESSAGE, false);
      if (confirmed == true) {
        if (fileLinks != null && fileLinks.length > 0) {
          Long[] fileIDs = RecordUtils.getIDs(fileLinks);
          Obj_IDs_Co request = new Obj_IDs_Co();
          request.IDs = new Long[2][];
          request.IDs[0] = fileIDs;
          // Owner of a file is a folder, but we specify file share object to aid in delete permission checking.
          Long shareId = FetchedDataCache.getSingleInstance().getFolderShareRecordMy(fileLinks[0].ownerObjId, true).shareId;
          request.IDs[1] = new Long[] { shareId };
          MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.FILE_Q_REMOVE_FILES, request));
        }
        if (msgLinks != null && msgLinks.length > 0) {
          Long[] msgIDs = RecordUtils.getIDs(msgLinks);
          Obj_IDs_Co request = new Obj_IDs_Co();
          request.IDs = new Long[2][];
          request.IDs[0] = msgIDs;
          // Owner of a msg is a folder, but we specify msg share object to aid in delete permission checking.
          Long shareId = FetchedDataCache.getSingleInstance().getFolderShareRecordMy(msgLinks[0].ownerObjId, true).shareId;
          request.IDs[1] = new Long[] { shareId };
          MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.MSG_Q_REMOVE, request));
        }
        if (folderPairs != null && folderPairs.length > 0) {
          new FolderActionTree.DeleteRunner(parent, folderPairs, true).start();
        }
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
          DownloadUtilsGui.downloadFilesChoice(new FileLinkRecord[] { _fileLink }, null, RecycleActionTable.this, MainFrame.getServerInterfaceLayer());
        }
      };
      Window w = SwingUtilities.windowForComponent(RecycleActionTable.this);
      if (w instanceof Frame)
        new OpenSaveCancelDialog((Frame) w, _fileLink, null, openTask, saveTask);
      else if (w instanceof Dialog)
        new OpenSaveCancelDialog((Dialog) w, _fileLink, null, openTask, saveTask);
    }
  }


//  private void markSelectedAs(Short newMark) {
//    LinkRecordI[] records = (LinkRecordI[]) getSelectedInstancesOf(LinkRecordI.class);
//    markRecordsAs(records, newMark);
//  }
//  private void markAllAs(Short newMark) {
//    RecycleTableModel tableModel = (RecycleTableModel) getTableModel();
//    Vector linksV = new Vector();
//    for (int i=0; i<tableModel.getRowCount(); i++) {
//      Record rec = tableModel.getRowObject(i);
//      if (rec instanceof FileLinkRecord || rec instanceof MsgLinkRecord)
//        linksV.addElement(rec);
//    }
//    if (linksV.size() > 0) {
//      LinkRecordI[] links = new LinkRecordI[linksV.size()];
//      linksV.toArray(links);
//      markRecordsAs(links, newMark);
//    }
//  }
//  private void markRecordsAs(LinkRecordI[] records, Short newMark) {
//    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleActionTable.class, "markRecordsAs(Record[] records, Short newMark)");
//    if (trace != null) trace.args(records, newMark);
//    if (records != null && records.length > 0) {
//      // gather all stats which need to be updated
//      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
//      Vector statsV = new Vector();
//      for (int i=0; i<records.length; i++) {
//        StatRecord statRecord = null;
//        if (records[i] instanceof FileLinkRecord)
//          statRecord = cache.getStatRecord(((FileLinkRecord) records[i]).fileLinkId, FetchedDataCache.STAT_TYPE_FILE);
//        else if (records[i] instanceof MsgLinkRecord)
//          statRecord = cache.getStatRecord(((MsgLinkRecord) records[i]).msgLinkId, FetchedDataCache.STAT_TYPE_MESSAGE);
//        if (statRecord != null && !statRecord.mark.equals(newMark))
//          statsV.addElement(statRecord);
//      }
//      if (statsV.size() > 0) {
//        StatRecord[] stats = new StatRecord[statsV.size()];
//        statsV.toArray(stats);
//        // clone the stats to send the request
//        StatRecord[] statsClones = (StatRecord[]) RecordUtils.cloneRecords(stats);
//
//        // set mark to "newMark" on the clones
//        for (int i=0; i<statsClones.length; i++)
//          statsClones[i].mark = newMark;
//
//        Stats_Update_Rq request = new Stats_Update_Rq(statsClones);
//
//        ServerInterfaceLayer serverInterfaceLayer = MainFrame.getServerInterfaceLayer();
//        serverInterfaceLayer.submitAndReturn(new MessageAction(CommandCodes.STAT_Q_UPDATE, request));
//      }
//    }
//    if (trace != null) trace.exit(RecycleActionTable.class);
//  }



  /**
   * Show a Move / Copy dialog and get the chosen destination FolderPair.
   */
  private FolderPair getMoveCopyDestination(boolean isMove, boolean forDirs, boolean forFiles, boolean forMsgs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleActionTable.class, "getMoveCopyDestination(boolean isMove, boolean forDirs, boolean forFiles, boolean forMsgs)");
    if (trace != null) trace.args(isMove);
    if (trace != null) trace.args(forDirs);
    if (trace != null) trace.args(forFiles);
    if (trace != null) trace.args(forMsgs);

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

    // If there are no selected folders, we are copying/moving files or msgs only and forbiden folder 
    // will be the one that is parent to the selected files or msgs.
    if (forFiles || forMsgs) {
      FolderPair parentFolderPair = ((RecycleTableModel) getTableModel()).getParentFolderPair();
      forbidenPairs = (FolderPair[]) ArrayUtils.concatinate(forbidenPairs, new FolderPair[] { parentFolderPair });
    }

    // if moving/copying files, destination folder cannot be of non-file type
    if (forFiles)
      forbidenPairs = (FolderPair[]) ArrayUtils.concatinate(forbidenPairs, FolderFilter.NON_FILE_FOLDERS.filterInclude(allFolderPairs));
    // if moving/copying msgs, destination folder cannot be of non-msg type
    if (forMsgs)
      forbidenPairs = (FolderPair[]) ArrayUtils.concatinate(forbidenPairs, FolderFilter.NON_MSG_FOLDERS.filterInclude(allFolderPairs));


    Move_NewFld_Dialog d = null;
    if (w instanceof Frame) d = new Move_NewFld_Dialog((Frame) w, allFolderPairs, forbidenPairs, null, title, isDescendantOk, cache); 
    else if (w instanceof Dialog) d = new Move_NewFld_Dialog((Dialog) w, allFolderPairs, forbidenPairs, null, title, isDescendantOk, cache); 

    FolderPair chosenPair = null;
    if (d != null) {
      chosenPair = d.getChosenDestination();
    }

    if (trace != null) trace.exit(RecycleActionTable.class, chosenPair);
    return chosenPair;
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

    // enable Empty action only if there any objects in the table...
    int objCount = RecycleActionTable.this.getTableModel().getRowCount();
    actions[EMPTY_FOLDER_ACTION].setEnabled(objCount > 0);

    Record[] records = (Record[]) getSelectedRecords();
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

//    // check for 'seen' and 'unseen' files
//    boolean anySeen = false;
//    boolean anyUnseen = false;

    boolean anyFileLink = false;
    boolean anyMsgLink = false;

    if (records != null) {
      for (int i=0; i<records.length; i++) {
        if (records[i] instanceof FileLinkRecord) {
          anyFileLink = true;
          FileLinkRecord fLink = (FileLinkRecord) records[i];
          // count the file towards total selected size
          totalSize = new Long(fLink.origSize.longValue() + (totalSize != null ? totalSize.longValue() : 0));
//          if (!(anySeen && anyUnseen)) {
//            StatRecord statRecord = cache.getStatRecord(fLink.fileLinkId, FetchedDataCache.STAT_TYPE_FILE);
//            if (statRecord != null) {
//              if (statRecord.mark.equals(StatRecord.MARK_OLD))
//                anySeen = true;
//              else if (statRecord.mark.equals(StatRecord.MARK_NEW))
//                anyUnseen = true;
//            }
//          }
        }
        if (records[i] instanceof MsgLinkRecord) {
          anyMsgLink = true;
          MsgLinkRecord mLink = (MsgLinkRecord) records[i];
          // count the msg towards total selected size
          MsgDataRecord mData = cache.getMsgDataRecord(mLink.msgId);
          if (mData != null) {
            totalSize = new Long(mData.recordSize.intValue() + (totalSize != null ? totalSize.longValue() : 0));
//            if (!(anySeen && anyUnseen)) {
//              StatRecord statRecord = cache.getStatRecord(mLink.msgLinkId, FetchedDataCache.STAT_TYPE_MESSAGE);
//              if (statRecord != null) {
//                if (statRecord.mark.equals(StatRecord.MARK_OLD))
//                  anySeen = true;
//                else if (statRecord.mark.equals(StatRecord.MARK_NEW))
//                  anyUnseen = true;
//              }
//            }
          }
        }
      }
    }

//    boolean anyUnseenGlobal = anyUnseen;
//    if (!anyUnseen) {
//      RecycleTableModel tableModel = (RecycleTableModel) getTableModel();
//      for (int i=0; i<tableModel.getRowCount(); i++) {
//        Record rec = (Record) tableModel.getRowObject(i);
//        if (rec instanceof FileLinkRecord) {
//          FileLinkRecord fLink = (FileLinkRecord) rec;
//          StatRecord statRecord = cache.getStatRecord(fLink.fileLinkId, FetchedDataCache.STAT_TYPE_FILE);
//          if (statRecord != null && statRecord.mark.equals(StatRecord.MARK_NEW)) {
//            anyUnseenGlobal = true;
//            break;
//          }
//        }
//        if (rec instanceof MsgLinkRecord) {
//          MsgLinkRecord mLink = (MsgLinkRecord) rec;
//          StatRecord statRecord = cache.getStatRecord(mLink.msgLinkId, FetchedDataCache.STAT_TYPE_MESSAGE);
//          if (statRecord != null && statRecord.mark.equals(StatRecord.MARK_NEW)) {
//            anyUnseenGlobal = true;
//            break;
//          }
//        }
//      }
//    }

    if (count == 0) {
      actions[OPEN_FILE_ACTION].setEnabled(false);
      actions[DOWNLOAD_ACTION].setEnabled(false);
      //actions[COPY_ACTION].setEnabled(false);
      actions[MOVE_ACTION].setEnabled(false);
      actions[DELETE_ACTION].setEnabled(false);
      actions[PROPERTIES_ACTION].setEnabled(false);
      actions[FORWARD_ACTION].setEnabled(false);
//      actions[MARK_AS_SEEN_ACTION].setEnabled(false);
//      actions[MARK_AS_UNSEEN_ACTION].setEnabled(false);
//      actions[MARK_ALL_SEEN_ACTION].setEnabled(anyUnseenGlobal);
      actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION].setEnabled(false);
    } else if (count == 1) {
      actions[OPEN_FILE_ACTION].setEnabled(noFolderPairs);
      actions[DOWNLOAD_ACTION].setEnabled(true);
      //actions[COPY_ACTION].setEnabled(noFolderPairs);
      actions[MOVE_ACTION].setEnabled(true);
      actions[DELETE_ACTION].setEnabled(true);
      actions[PROPERTIES_ACTION].setEnabled(true);
      actions[FORWARD_ACTION].setEnabled(noFolderPairs);
//      actions[MARK_AS_SEEN_ACTION].setEnabled(anyUnseen);
//      actions[MARK_AS_UNSEEN_ACTION].setEnabled(anySeen);
//      actions[MARK_ALL_SEEN_ACTION].setEnabled(anyUnseenGlobal);
      actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION].setEnabled(true);
    } else if (count > 1) {
      actions[OPEN_FILE_ACTION].setEnabled(noFolderPairs && !anyFileLink);
      actions[DOWNLOAD_ACTION].setEnabled(true);
      //actions[COPY_ACTION].setEnabled(noFolderPairs);
      actions[MOVE_ACTION].setEnabled(true);
      actions[DELETE_ACTION].setEnabled(true);
      actions[PROPERTIES_ACTION].setEnabled(false);
      actions[FORWARD_ACTION].setEnabled(noFolderPairs);
//      actions[MARK_AS_SEEN_ACTION].setEnabled(anyUnseen);
//      actions[MARK_AS_UNSEEN_ACTION].setEnabled(anySeen);
//      actions[MARK_ALL_SEEN_ACTION].setEnabled(anyUnseenGlobal);
      actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION].setEnabled(true);
    }

    Window w = SwingUtilities.windowForComponent(this);
    actions[OPEN_FILES_IN_SEPERATE_WINDOW_ACTION].setEnabled(w != null);
    actions[OPEN_MSGS_IN_SEPERATE_WINDOW_ACTION].setEnabled(w != null);
    actions[OPEN_RECYCLE_BIN_IN_SEPERATE_WINDOW_ACTION].setEnabled(w != null);
    actions[REFRESH_ACTION].setEnabled(w != null);

    if (noFolderPairs)
      Stats.setSizeBytes(totalSize != null ? totalSize.longValue() : -1);
  }



  /*************************************************
   ***   Folder double-click Listener handling   ***
   ************************************************/

  public void addFolderSelectionListener(FolderSelectionListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleActionTable.class, "addFolderSelectionListener(FolderSelectionListener)");
    if (trace != null) trace.args(l);

    synchronized (folderSelectionListenerList) {
      folderSelectionListenerList.add(FolderSelectionListener.class, l);
    }

    if (trace != null) trace.exit(RecycleActionTable.class);
  }

  public void removeFolderSelectionListener(FolderSelectionListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleActionTable.class, "removeFolderSelectionListener(FolderSelectionListener)");
    if (trace != null) trace.args(l);

    synchronized (folderSelectionListenerList) {
      folderSelectionListenerList.remove(FolderSelectionListener.class, l);
    }

    if (trace != null) trace.exit(RecycleActionTable.class);
  }


  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   */
  protected void fireFolderSelectionChange(FolderRecord selectedFolder) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleActionTable.class, "fireFolderSelectionChange(FolderRecord)");
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

    if (trace != null) trace.exit(RecycleActionTable.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "RecycleActionTable";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
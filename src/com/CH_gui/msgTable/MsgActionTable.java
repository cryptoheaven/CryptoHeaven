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

package com.CH_gui.msgTable;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.*;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.ops.*;
import com.CH_cl.service.records.*;
import com.CH_cl.service.records.filters.*;

import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.gui.*;
import com.CH_co.nanoxml.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.msg.*;
import com.CH_co.service.msg.dataSets.stat.*;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.monitor.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.action.*;
import com.CH_gui.addressBook.*;
import com.CH_gui.dialog.*;
import com.CH_gui.chatTable.*;
import com.CH_gui.fileTable.*;
import com.CH_gui.frame.*;
import com.CH_gui.list.*;
import com.CH_gui.msgs.*;
import com.CH_gui.postTable.*;
import com.CH_gui.sortedTable.*;
import com.CH_gui.table.*;
import com.CH_gui.tree.*;
import com.CH_guiLib.util.HTML_utils;

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
 * <b>$Revision: 1.61 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class MsgActionTable extends RecordActionTable implements ActionProducerI {

  private static final String PROPERTY_NAME_THREADED = "threadedView";

  protected Action[] actions;

  // If different message tables have different number of columns, pick the the largest possible value ...
  private static final int NUM_OF_SORT_COLUMNS = MsgTableModel.columnHeaderDatas[MsgTableModel.MODE_ADDRESS].data[ColumnHeaderData.I_SHORT_DISPLAY_NAMES].length;

  public static final int NEW_ACTION = 0;
  public static final int COPY_ACTION = 1;
  public static final int MOVE_ACTION = 2;
  public static final int DELETE_ACTION = 3;
  private static final int PROPERTIES_ACTION = 4;
  public static final int FORWARD_ACTION = 5;
  private static final int SAVE_ATTACHMENTS_ACTION = 6;
  public static final int REPLY_TO_SENDER__OR__COMPOSE_TO_ADDRESS_ACTION = 7;
  public static final int REFRESH_ACTION = 8;
  public static final int MARK_AS_READ_ACTION = 9;
  public static final int MARK_AS_UNREAD_ACTION = 10;
  public static final int MARK_ALL_READ_ACTION = 11;
  public static final int OPEN_IN_SEPERATE_WINDOW_ACTION = 12;
  public static final int TRACE_PRIVILEGE_AND_HISTORY_ACTION = 13;
  private static final int INVITE_SENDER_ACTION = 14;
  private static final int POST_REPLY__OR__EDIT_ACTION = 15;
  public static final int OPEN_IN_SEPERATE_VIEW = 16;
  private static final int THREADED_VIEW_ACTION = 17;
  protected static final int NEW_FROM_DRAFT_ACTION = 18;
  private static final int ITERATE_NEXT_ACTION = 19;
  private static final int ITERATE_PREV_ACTION = 20;
  private static final int ADD_SENDER_TO_ADDRESS_BOOK_ACTION = 21;
  private static final int REPLY_TO_ALL_ACTION = 22;
  private static final int PRINT_ACTION = 23;
  private static final int REVOKE_ACTION = 24;
  private static final int INVITE_ACTION = 25;
  private static final int SPLIT_LAYOUT_ACTION = 26;
  public static final int FILTER_ACTION = 27;
  private static final int DOWNLOAD_ACTION = 28;
  private static final int MSG_COMPOSE_ACTION = 29;

  private static final int SORT_ASC_ACTION = 30;
  private static final int SORT_DESC_ACTION = 31;
  private static final int SORT_BY_FIRST_COLUMN_ACTION = 32;
  private static final int CUSTOMIZE_COLUMNS_ACTION = SORT_BY_FIRST_COLUMN_ACTION + NUM_OF_SORT_COLUMNS;

  private static final int NUM_ACTIONS = CUSTOMIZE_COLUMNS_ACTION + 1;

  private int leadingActionId = Actions.LEADING_ACTION_ID_MSG_ACTION_TABLE;
  private int leadingFileActionId = Actions.LEADING_ACTION_ID_FILE_ACTION_TABLE;
  private int leadingFolderActionId = Actions.LEADING_ACTION_ID_FOLDER_ACTION_TREE;

  private boolean msgPreviewMode;
  private RecordTableScrollPane parentViewTable;

  private short selectionObjType;
  private short componentObjType;

  /** Creates new MsgActionTable */
  public MsgActionTable() {
    this(false);
  }
  public MsgActionTable(boolean msgPreviewMode) {
    this(new MsgTableModel(null, MsgTableModel.MODE_MSG), msgPreviewMode);
  }
  /** Creates new MsgActionTable */
  public MsgActionTable(RecordTableModel model) {
    this(model, MsgDataRecord.OBJ_TYPE_MSG, false);
  }
  public MsgActionTable(RecordTableModel model, short componentObjType) {
    this(model, componentObjType, false);
  }
  public MsgActionTable(RecordTableModel model, boolean msgPreviewMode) {
    this(model, MsgDataRecord.OBJ_TYPE_MSG, msgPreviewMode);
  }
  public MsgActionTable(RecordTableModel model, short componentObjType, boolean msgPreviewMode) {
    super(model, MsgTableSorter.class);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgActionTable.class, "MsgActionTable(RecordTableModel model, short componentObjType, boolean msgPreviewMode)");
    if (trace != null) trace.args(model);
    if (trace != null) trace.args(componentObjType);
    if (trace != null) trace.args(msgPreviewMode);

    this.componentObjType = componentObjType;
    this.msgPreviewMode = msgPreviewMode;

    ((MsgTableModel) getTableModel()).setAutoUpdate(true);

    addDND(getJSortedTable());
    addDND(getViewport());

    if (trace != null) trace.exit(MsgActionTable.class);
  }

  public DragGestureListener createDragGestureListener() {
    return new MsgDND_DragGestureListener(this);
  }
  public DropTargetListener createDropTargetListener() {
    return new MsgDND_DropTargetListener(this);
  }


  private void initActions() {
    actions = new Action[NUM_ACTIONS];
    if (!msgPreviewMode)
      actions[NEW_ACTION] = new NewAction(leadingActionId + NEW_ACTION);
    actions[MSG_COMPOSE_ACTION] = new MsgComposeAction(leadingActionId + MSG_COMPOSE_ACTION);
    actions[COPY_ACTION] = new CopyAction(leadingActionId + COPY_ACTION);
    //if (!msgPreviewMode) {
      actions[MOVE_ACTION] = new MoveAction(leadingActionId + MOVE_ACTION);
      actions[DELETE_ACTION] = new DeleteAction(leadingActionId + DELETE_ACTION);
      actions[REVOKE_ACTION] = new RevokeAction(leadingActionId + REVOKE_ACTION);
      actions[DOWNLOAD_ACTION] = new DownloadAction(leadingFileActionId + FileActionTable.DOWNLOAD_ACTION);
    //}
    actions[PROPERTIES_ACTION] = new PropertiesAction(leadingActionId + PROPERTIES_ACTION);
    actions[FORWARD_ACTION] = new ForwardToAction(leadingActionId + FORWARD_ACTION);
    actions[SAVE_ATTACHMENTS_ACTION] = new SaveAttachmentsAction(leadingActionId + SAVE_ATTACHMENTS_ACTION);
    actions[REPLY_TO_SENDER__OR__COMPOSE_TO_ADDRESS_ACTION] = new ReplyToSenderOrComposeAction(leadingActionId + REPLY_TO_SENDER__OR__COMPOSE_TO_ADDRESS_ACTION);
    actions[REPLY_TO_ALL_ACTION] = new ReplyToAllAction(leadingActionId + REPLY_TO_ALL_ACTION);
    actions[PRINT_ACTION] = new PrintAction(leadingActionId + PRINT_ACTION);
    actions[NEW_FROM_DRAFT_ACTION] = new UseAsDraftAction(leadingActionId + NEW_FROM_DRAFT_ACTION);
    actions[ITERATE_NEXT_ACTION] = new IterateNextAction(leadingActionId + ITERATE_NEXT_ACTION);
    actions[ITERATE_PREV_ACTION] = new IteratePrevAction(leadingActionId + ITERATE_PREV_ACTION);
    if (!msgPreviewMode) {
      actions[REFRESH_ACTION] = new RefreshAction(leadingActionId + REFRESH_ACTION);
    }
    actions[MARK_AS_READ_ACTION] = new MarkAsReadAction(leadingActionId + MARK_AS_READ_ACTION);
    actions[MARK_AS_UNREAD_ACTION] = new MarkAsUnreadAction(leadingActionId + MARK_AS_UNREAD_ACTION);
    if (!msgPreviewMode) {
      actions[MARK_ALL_READ_ACTION] = new MarkAllReadAction(leadingActionId + MARK_ALL_READ_ACTION);
      actions[OPEN_IN_SEPERATE_WINDOW_ACTION] = new OpenInSeperateWindowAction(leadingActionId + OPEN_IN_SEPERATE_WINDOW_ACTION);
    }
    actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION] = new TracePrivilegeAndHistoryAction(leadingActionId + TRACE_PRIVILEGE_AND_HISTORY_ACTION);
    actions[INVITE_SENDER_ACTION] = new InviteSenderAction(leadingActionId + INVITE_SENDER_ACTION);
    actions[ADD_SENDER_TO_ADDRESS_BOOK_ACTION] = new AddSenderToAddressBook(leadingActionId + ADD_SENDER_TO_ADDRESS_BOOK_ACTION);
    if (!msgPreviewMode) {
      actions[POST_REPLY__OR__EDIT_ACTION] = new PostReplyOrEditAction(leadingActionId + POST_REPLY__OR__EDIT_ACTION);
      actions[OPEN_IN_SEPERATE_VIEW] = new OpenInSeperateViewAction(leadingActionId + OPEN_IN_SEPERATE_VIEW);
      actions[THREADED_VIEW_ACTION] = new ThreadedViewAction(leadingActionId + THREADED_VIEW_ACTION);
      actions[INVITE_ACTION] = new InviteAction(leadingFolderActionId + FolderActionTree.INVITE_ACTION);
      if (!(this instanceof PostActionTable) && !(this instanceof ChatActionTable)) {
        actions[SPLIT_LAYOUT_ACTION] = new SplitLayoutAction(leadingActionId + SPLIT_LAYOUT_ACTION, 30, 50);
      }
      actions[FILTER_ACTION] = new FilterAction(leadingActionId + FILTER_ACTION);
      {
        ButtonGroup sortAscDescGroup = new ButtonGroup();
        actions[SORT_ASC_ACTION] = new SortAscDescAction(true, 0+Actions.LEADING_ACTION_ID_RECORD_ACTION_TABLE, sortAscDescGroup);
        actions[SORT_DESC_ACTION] = new SortAscDescAction(false, 1+Actions.LEADING_ACTION_ID_RECORD_ACTION_TABLE, sortAscDescGroup);
        actions[CUSTOMIZE_COLUMNS_ACTION] = new CustomizeColumnsAction(2+Actions.LEADING_ACTION_ID_RECORD_ACTION_TABLE);
        ButtonGroup columnSortGroup = new ButtonGroup();
        for (int i=0; i<NUM_OF_SORT_COLUMNS; i++) {
          try {
            actions[SORT_BY_FIRST_COLUMN_ACTION+i] = new SortByColumnAction(i, i+3+Actions.LEADING_ACTION_ID_RECORD_ACTION_TABLE, columnSortGroup);
          } catch (IllegalArgumentException x) {
            // Ignore column action, it doesn't exist!
          }
        }
      }
    }
    setEnabledActions();
  }
  public Action getRefreshAction() {
    if (actions == null) initActions();
    return actions[REFRESH_ACTION];
  }
  public Action getCloneAction() {
    if (actions == null) initActions();
    return actions[OPEN_IN_SEPERATE_WINDOW_ACTION];
  }
  public Action getThreadedAction() {
    if (actions == null) initActions();
    return actions[THREADED_VIEW_ACTION];
  }
  public Action getDoubleClickAction() {
    if (actions == null) initActions();
    return actions[OPEN_IN_SEPERATE_VIEW];
  }
  public Action getSplitLayoutAction() {
    if (actions == null) initActions();
    return actions[SPLIT_LAYOUT_ACTION];
  }
  public Action getFilterAction() {
    if (actions == null) initActions();
    return actions[FILTER_ACTION];
  }



  // =====================================================================
  // LISTENERS FOR THE MENU ITEMS
  // =====================================================================


  /**
   * Create a new message
   */
  private class NewAction extends AbstractActionTraced {
    public NewAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_New_Message_to_Folder"), Images.get(ImageNums.MAIL_COMPOSE_TO_FOLDER16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_New_Message_to_Folder"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_COMPOSE_TO_FOLDER24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Post"));
      putValue(Actions.PARENT_NAME, com.CH_gui.lang.Lang.rb.getString("Message"));
      putValue(Actions.PARENT_MNEMONIC, new Integer(77));
      RecordTableModel tableModel = getTableModel();
      if (tableModel instanceof MsgTableModel) {
        if (((MsgTableModel) tableModel).getMode() == MsgTableModel.MODE_ADDRESS ||
            ((MsgTableModel) tableModel).getMode() == MsgTableModel.MODE_WHITELIST
        ) {
          putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_New_Address_to_Folder"));
          putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_New_Address_to_selected_Folder."));
          putValue(Actions.MENU_ICON, Images.get(ImageNums.ADDRESS_ADD16));
          putValue(Actions.TOOL_ICON, Images.get(ImageNums.ADDRESS_ADD24));
          putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_New_Address"));
          putValue(Actions.PARENT_NAME, com.CH_gui.lang.Lang.rb.getString("Address"));
          putValue(Actions.PARENT_MNEMONIC, new Integer(65));
        }
      }
    }
    public void actionPerformedTraced(ActionEvent event) {
      // new message trigger
      FolderPair pair = ((MsgTableModel)MsgActionTable.this.getTableModel()).getParentFolderPair();
      if (pair != null && pair.getFolderRecord().isCategoryType()) {
        pair = null;
      }
      if (pair != null) {
        if (pair.getFolderRecord().folderType.shortValue() == FolderRecord.ADDRESS_FOLDER ||
            pair.getFolderRecord().folderType.shortValue() == FolderRecord.WHITELIST_FOLDER
        ) {
          new AddressFrame(pair);
        } else {
          new MessageFrame(pair);
        }
      } else {
        new MessageFrame();
      }
    }
  }

  /**
   * Compose a new message
   */
  private class MsgComposeAction extends AbstractActionTraced {
    public MsgComposeAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_New_Message"), Images.get(ImageNums.MAIL_COMPOSE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_New_Message"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_COMPOSE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_New_Message"));
      putValue(Actions.IN_MENU, Boolean.FALSE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      new MessageFrame();
    }
  }

  /**
   * Copy selected message(s) to another folder
   */
  private class CopyAction extends AbstractActionTraced {
    public CopyAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Copy_..."), Images.get(ImageNums.MAIL_COPY16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Copy_selected_message(s)_to_another_folder."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_COPY24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Copy"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      moveOrCopyAction(false);
    }
  }

  /**
   * Move selected message(s) to another folder
   */
  private class MoveAction extends AbstractActionTraced {
    public MoveAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Move_..."), Images.get(ImageNums.MAIL_MOVE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Move_selected_message(s)_to_another_folder."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_MOVE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Move"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      moveOrCopyAction(true);
    }
  }

  /**
   * Delete selected message(s)
   */
  private class DeleteAction extends AbstractActionTraced {
    public DeleteAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Delete_..."), Images.get(ImageNums.MAIL_DELETE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Permanently_delete_selected_message(s)."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_DELETE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Delete"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) getSelectedRecords();
      if (msgLinks != null && msgLinks.length > 0) {
        String title = com.CH_gui.lang.Lang.rb.getString("msgTitle_Delete_Confirmation");
        String messageText = "Are you sure you want to send these items to the Recycle Bin?";
        boolean option = showConfirmationDialog(MsgActionTable.this, title, messageText, msgLinks, MessageDialog.RECYCLE_MESSAGE, true);
        if (option == true) {
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          FolderPair recycleFolderPair = CacheUtilities.convertRecordToPair(cache.getFolderRecord(cache.getUserRecord().recycleFolderId));
          moveOrCopyAction(true, msgLinks, recycleFolderPair);
        }
      }
    }
  }

  public static boolean showConfirmationDialog(Component parent, String title, String messageText, Record[] recordsInQuestion, int messageType, boolean isSkippable) {
    boolean option = true;
    boolean confirmationValue = false;
    String confirmationProperty = null;
    if (isSkippable) {
      confirmationProperty = "ConfirmationDialog-skip-"+messageType;
      String confirmationPropertyValue = GlobalProperties.getProperty(confirmationProperty, "false", true);
      try {
        confirmationValue = Boolean.valueOf(confirmationPropertyValue).booleanValue();
      } catch (Throwable t) {
      }
    }
    if (!confirmationValue) {
      option = false;
      JPanel panel = new JPanel();
      panel.setMaximumSize(new Dimension(500, 200));
      panel.setLayout(new BorderLayout(0, 10));
      panel.add(new JMyLabel(messageText), BorderLayout.NORTH);
      JPanel itemPanel = new JPanel();
      itemPanel.setBorder(new EmptyBorder(0,0,0,0));
      itemPanel.setLayout(new GridBagLayout());
      int itemCount = 0;
      for (int i=0; i<recordsInQuestion.length; i++) {
        itemCount ++;
        JLabel item = new JMyLabel(ListRenderer.getRenderedText(recordsInQuestion[i]));
        item.setIcon(ListRenderer.getRenderedIcon(recordsInQuestion[i]));
        itemPanel.add(item, new GridBagConstraints(0, itemCount, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3,15,3,0), 0, 0));
      }
      if (itemCount > 4) {
        JScrollPane sc = new JScrollPane(itemPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(sc, BorderLayout.CENTER);
      } else {
        panel.add(itemPanel, BorderLayout.CENTER);
      }
      JCheckBox itemQuestion = null;
      if (isSkippable) {
        itemQuestion = new JMyCheckBox("Skip this confirmation dialog in the future.", false);
        panel.add(itemQuestion, BorderLayout.SOUTH);
      }
      option = MessageDialog.showDialogYesNo(parent, panel, title, messageType);
      if (isSkippable && itemQuestion.isSelected()) {
        GlobalProperties.setProperty(confirmationProperty, "true", true);
      }
    }
    return option;
  }

  /**
   * Delete selected message(s)
   */
  private class RevokeAction extends AbstractActionTraced {
    public RevokeAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Revoke_..."), Images.get(ImageNums.STOPWATCH_ALERT16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Change_Expiry_or_Revoke."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.STOPWATCH_ALERT24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Revoke"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) getSelectedRecords();
      if (msgLinks != null && msgLinks.length > 0) {
        Window w = SwingUtilities.windowForComponent(MsgActionTable.this);
        if (w instanceof Frame) new ExpiryRevocationDialog((Frame) w, msgLinks);
        else if (w instanceof Dialog) new ExpiryRevocationDialog((Dialog) w, msgLinks);
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
      MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) getSelectedRecords();
      if (msgLinks != null && msgLinks.length > 0) {
        DownloadUtilities.downloadFilesChoice(msgLinks, null, MsgActionTable.this, MainFrame.getServerInterfaceLayer());
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
      putValue(Actions.IN_POPUP, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FolderPair fPair = MsgActionTable.this.getTableModel().getParentFolderPair();
      if (fPair != null && fPair.getFolderRecord().isCategoryType()) {
        Window w = SwingUtilities.windowForComponent(MsgActionTable.this);
        String title = com.CH_gui.lang.Lang.rb.getString("title_Select_Folder_or_Group_to_Share");
        fPair = FolderActionTree.selectFolder(w, title, new MultiFilter(new RecordFilter[] { FolderFilter.MAIN_VIEW, FolderFilter.NON_LOCAL_FOLDERS }, MultiFilter.AND));
      }
      if (fPair != null) {
        Window w = SwingUtilities.windowForComponent(MsgActionTable.this);
        if (w instanceof Frame) new FolderPropertiesDialog((Frame) w, fPair, 1);
        else if (w instanceof Dialog) new FolderPropertiesDialog((Dialog) w, fPair, 1);
      }
    }
    public void updateText() {
      FolderPair fPair = MsgActionTable.this.getTableModel().getParentFolderPair();
      if (fPair == null) {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Share_Folder_..."));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Share_Folder_..."));
        putValue(Actions.MENU_ICON, Images.get(ImageNums.FLD_CLOSED_SHARED16, true));
        putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLD_CLOSED_SHARED24));
        putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Share"));
      } else if (fPair.getFolderRecord().isAddressType()) {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Share_Address_Book_..."));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Share_Address_Book_..."));
        putValue(Actions.MENU_ICON, Images.get(ImageNums.FLD_ADDR_CLOSED_SHARED16, true));
        putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLD_ADDR_CLOSED_SHARED24));
        putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Share"));
      } else if (fPair.getFolderRecord().isChatting()) {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Invite_to_the_Conversation_..."));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Invite_to_the_Conversation_..."));
        putValue(Actions.MENU_ICON, Images.get(ImageNums.FLD_CHAT_CLOSED_SHARED16, true));
        putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLD_CHAT_CLOSED_SHARED24));
        putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Invite"));
      } else {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Share_Folder_..."));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Share_Folder_..."));
        putValue(Actions.MENU_ICON, Images.get(ImageNums.FLD_CLOSED_SHARED16, true));
        putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLD_CLOSED_SHARED24));
        putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Share"));
      }
    }
  }

  /**
   * Show Message Properties dialog
   */
  private class PropertiesAction extends AbstractActionTraced {
    public PropertiesAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Message_Properties"));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Show_properties_of_selected_message."));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      MsgLinkRecord msgLink = (MsgLinkRecord) getSelectedRecord();
      if (msgLink != null) {
        Window w = SwingUtilities.windowForComponent(MsgActionTable.this);
        if (w instanceof Frame) new MsgPropertiesDialog((Frame) w, msgLink);
        else if (w instanceof Dialog) new MsgPropertiesDialog((Dialog) w, msgLink);
      }
    }
  }

  /**
   * Show the Message Compose frame with selected messages as initial attachments.
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
      MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) getSelectedRecords();
      if (msgLinks != null && msgLinks.length > 0) {
        new MessageFrame(null, msgLinks);
      }
    }
  }

  /**
   * Show the Save Attachments Dialog.
   */
  private class SaveAttachmentsAction extends AbstractActionTraced {
    public SaveAttachmentsAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Save_Attachment(s)_..."), Images.get(ImageNums.DETACH16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Save_Attachments_into_another_folder."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.DETACH24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Save_Attachments"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) getSelectedRecords();
      if (msgLinks != null && msgLinks.length > 0) {
        Component source = (Component) event.getSource();
        if (source.isShowing()) {
          new AttachmentFetcherPopup(source, msgLinks).start();
        }
        else {
          Window w = SwingUtilities.windowForComponent(MsgActionTable.this);
          if (w instanceof Frame) new SaveAttachmentsDialog((Frame) w, msgLinks);
          else if (w instanceof Dialog) new SaveAttachmentsDialog((Dialog) w, msgLinks);
        }
      }
    }
  }


  /**
   * Show the Compose Message dialog with original message quoted and reply header.
   * Reply To Sender ---OR--- Compose To Address(es)
   */
  private class ReplyToSenderOrComposeAction extends AbstractActionTraced {
    public ReplyToSenderOrComposeAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Reply_to_Sender_..."), Images.get(ImageNums.REPLY16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Reply_to_Sender"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.REPLY24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Reply"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      if (selectionObjType == MsgDataRecord.OBJ_TYPE_MSG) {
        MsgLinkRecord msgLink = (MsgLinkRecord) getSelectedRecord();
        if (msgLink != null) {
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          MsgDataRecord msgData = cache.getMsgDataRecord(msgLink.msgId);
          if (msgData != null) {
            Record recipient = null;
            {
              String fromEmailAddress = msgData.getFromEmailAddress();
              if (msgData.isEmail() || fromEmailAddress != null) {
                String[] replyTos = msgData.getReplyToAddresses();
                if (replyTos != null && (replyTos.length > 1 || (replyTos.length == 1 && !EmailRecord.isAddressEqual(replyTos[0], msgData.getFromEmailAddress())))) {
                  recipient = new EmailAddressRecord(replyTos[0]);
                } else {
                  recipient = new EmailAddressRecord(fromEmailAddress);
                }
              } else {
                recipient = cache.getContactRecordOwnerWith(cache.getMyUserId(), msgData.senderUserId);
              }
            }
//            if (msgData.isEmail())
//              recipient = new EmailAddressRecord(msgData.getFromEmailAddress());
//            else
//              recipient = cache.getContactRecordOwnerWith(cache.getMyUserId(), msgData.senderUserId);
            if (recipient instanceof ContactRecord) {
              ContactRecord cRec = (ContactRecord) recipient;
              if (!cRec.isOfActiveType()) recipient = cache.getUserRecord(msgData.senderUserId);
            } else if (recipient == null) {
              recipient = cache.getUserRecord(msgData.senderUserId);
            }
            // if the sender was a User but is now deleted, lets create an email address instead
            if (recipient == null && !msgData.isEmail()) {
              recipient = new EmailAddressRecord("" + msgData.senderUserId + "@" + URLs.getElements(URLs.DOMAIN_MAIL)[0]);
            }
            if (recipient != null) {
              // new message trigger
              new MessageFrame(new Record[][] {{ recipient }}, msgLink);
            }
          }
        }
      } else if (selectionObjType == MsgDataRecord.OBJ_TYPE_ADDR) {
        MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) getSelectedRecords();
        if (msgLinks != null) {
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          MsgDataRecord[] msgDatas = cache.getMsgDataRecordsForLinks(RecordUtils.getIDs(msgLinks));
          if (msgDatas != null) {
            new MessageFrame(msgDatas);
          }
        }
      }
    }
  }


  /**
   * Show the Compose Message dialog with original message quoted and reply header.
   * Preselected recipients include the sender and all the recipients of the original message.
   */
  private class ReplyToAllAction extends AbstractActionTraced {
    public ReplyToAllAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Reply_to_All_..."), Images.get(ImageNums.REPLY_TO_ALL16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Reply_All"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.REPLY_TO_ALL24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Reply_All"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      if (selectionObjType == MsgDataRecord.OBJ_TYPE_MSG) {
        MsgLinkRecord msgLink = (MsgLinkRecord) getSelectedRecord();
        if (msgLink != null) {
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          MsgDataRecord msgData = cache.getMsgDataRecord(msgLink.msgId);
          if (msgData != null) {
            Record recipient = null;
            {
              String fromEmailAddress = msgData.getFromEmailAddress();
              if (msgData.isEmail() || fromEmailAddress != null) {
                recipient = new EmailAddressRecord(fromEmailAddress);
              } else {
                recipient = cache.getContactRecordOwnerWith(cache.getMyUserId(), msgData.senderUserId);
              }
            }
//            if (msgData.isEmail())
//              recipient = new EmailAddressRecord(msgData.getFromEmailAddress());
//            else
//              recipient = cache.getContactRecordOwnerWith(cache.getMyUserId(), msgData.senderUserId);
            if (recipient instanceof ContactRecord) {
              ContactRecord cRec = (ContactRecord) recipient;
              if (!cRec.isOfActiveType()) recipient = cache.getUserRecord(msgData.senderUserId);
            } else if (recipient == null) {
              recipient = cache.getUserRecord(msgData.senderUserId);
            }
            // if the sender was a User but is now deleted, lets create an email address instead
            if (recipient == null && !msgData.isEmail()) {
              recipient = new EmailAddressRecord("" + msgData.senderUserId + "@" + URLs.getElements(URLs.DOMAIN_MAIL)[0]);
            }
            Record[][] allRecipients = MsgPanelUtils.gatherAllMsgRecipients(msgData);

            Record[] sender = new Record[] { recipient };
            // add the sender to the TO header as recipient
            if (allRecipients[0] != null) {
              allRecipients[0] = (Record[]) ArrayUtils.concatinate(sender, allRecipients[0], Record.class);
              allRecipients[0] = (Record[]) ArrayUtils.removeDuplicates(allRecipients[0]);
            } else {
              allRecipients[0] = new Record[] { recipient };
            }

            // add all replyTo addresses to the TO header as recipients
            String[] replyTos = msgData.getReplyToAddresses();
            if (replyTos != null && (replyTos.length > 1 || (replyTos.length == 1 && !EmailRecord.isAddressEqual(replyTos[0], msgData.getFromEmailAddress())))) {
              EmailAddressRecord[] replyToEmlRecs = new EmailAddressRecord[replyTos.length];
              for (int i=0; i<replyTos.length; i++) {
                replyToEmlRecs[i] = new EmailAddressRecord(replyTos[i]);
              }
              allRecipients[0] = (Record[]) ArrayUtils.concatinate(allRecipients[0], replyToEmlRecs, Record.class);
              allRecipients[0] = (Record[]) ArrayUtils.removeDuplicates(allRecipients[0]);
            }

            // subtract myself from TO, CC, BCC
            // subtract my UserRecord first
            for (int i=0; i<allRecipients.length; i++) {
              if (allRecipients[i] != null && allRecipients[i].length > 0)
                allRecipients[i] = RecordUtils.getDifference(allRecipients[i], new Record[] { cache.getUserRecord() });
            }
            // subtract my EmailRecords (after converting to EmailAddressRecords) next
            EmailRecord[] myEmlRecs = cache.getEmailRecords(cache.getMyUserId());
            EmailAddressRecord[] myEmlAddrRecs = new EmailAddressRecord[myEmlRecs.length];
            for (int i=0; i<myEmlRecs.length; i++)
              myEmlAddrRecs[i] = new EmailAddressRecord(myEmlRecs[i].getEmailAddressFull());
            for (int i=0; i<allRecipients.length; i++) {
              if (allRecipients[i] != null && allRecipients[i].length > 0)
                allRecipients[i] = RecordUtils.getDifference(allRecipients[i], myEmlAddrRecs, new InternetAddressRecord.AddressComparator());
            }

            // new message trigger
            new MessageFrame(allRecipients, msgLink);
          }
        }
      }
    }
  }

  private class PrintAction extends AbstractActionTraced {
    public PrintAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Print_..."), Images.get(ImageNums.PRINT16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      //putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Print_..."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.PRINT24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Print"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) getSelectedRecords();
      // if nothing selected, make all selected
      boolean isAllSelected = false;
      if (msgLinks == null || msgLinks.length == 0) {
        isAllSelected = true;
        JSortedTable table = getJSortedTable();
        RecordTableModel model = getTableModel();
        msgLinks = new MsgLinkRecord[table.getRowCount()];
        for (int i=0; i<msgLinks.length; i++) {
          msgLinks[i] = (MsgLinkRecord) model.getRowObject(table.convertMyRowIndexToModel(i));
        }
      }
      // if single object selected, print email
      if (msgLinks != null && msgLinks.length == 1 && !isAllSelected) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        MsgDataRecord msgData = cache.getMsgDataRecord(msgLinks[0].msgId);
        Thread th = new ThreadTraced(new MsgPreviewPanel.PrintRunnable(msgLinks[0], msgData, false, MsgActionTable.this), "Print Runnable");
        th.setDaemon(true);
        th.start();
      } else if (msgLinks != null && msgLinks.length >= 1) {
        // more than one selected, print table format
        RecordTableModel mdl = getTableModel();
        if (mdl instanceof MsgTableModel) {
          ColumnHeaderData chd = mdl.getColumnHeaderData();
          MsgTableModel model = (MsgTableModel) mdl;

          JSortedTable table = getJSortedTable();
          int columns = table.getColumnCount();

          StringBuffer sb = new StringBuffer();

          sb.append("<html><body>");
          sb.append("<font size='-1' face="+HTML_utils.DEFAULT_FONTS_QUOTED+"><b>" + ListRenderer.getRenderedText(model.getParentFolderPair()) + "</b></font>");
          sb.append("<hr color=#000000 noshade size=2>");
          sb.append("<table cellpadding='0' cellspacing='0' border='0'>");
          sb.append("<tr>");
          for (int c=0; c<columns; c++) {
            int viewCol = c;
            int modelCol = table.convertColumnIndexToModel(viewCol);
            int rawCol = chd.convertColumnToRawModel(modelCol);
            String headerName = chd.getRawColumnName(rawCol);
            sb.append("<td NOWRAP align='left' valign='top'><font size='-1' face="+HTML_utils.DEFAULT_FONTS_QUOTED+"><b>");
            sb.append(headerName != null && headerName.trim().length() > 0 ? Misc.encodePlainIntoHtml(headerName + "  ") : "&nbsp;");
            sb.append("</b></td>");
          }
          sb.append("</tr>");
          for (int i=0; i<msgLinks.length; i++) {
            MsgLinkRecord msgLink = msgLinks[i];
            sb.append("<tr>");
            for (int c=0; c<columns; c++) {
              int viewCol = c;
              int modelCol = table.convertColumnIndexToModel(viewCol);
              int rawCol = chd.convertColumnToRawModel(modelCol);
              Object value = model.getValueAtRawColumn(msgLink, rawCol, false);
              Class cc = table.getColumnClass(c);
              TableCellRenderer tcr = table.getDefaultRenderer(cc);
              Component comp = tcr.getTableCellRendererComponent(table, value, false, false, model.getRowForObject(msgLink.msgLinkId), viewCol);
              String s = "";
              boolean isHTMLformatted = false;
              boolean isNOwrap = false;
              if (comp instanceof JLabel) {
                String t = ((JLabel) comp).getText();
                if (t != null)
                  s = t;
                isHTMLformatted = s.startsWith("<html>");
                isNOwrap = s.length() < 20;
              } else if (comp instanceof JEditorPane) {
                JEditorPane pane = (JEditorPane) comp;
                isHTMLformatted = pane.getContentType().equalsIgnoreCase("text/html");
                String t = pane.getText();
                if (t != null)
                  s = t;
              } else if (comp instanceof JTextComponent) {
                String t = ((JTextComponent) comp).getText();
                if (t != null)
                  s = t;
              } else if (comp instanceof JPanel) {
                // compaund elements like the reply column in postings or reply indented subject of messages
                isHTMLformatted = true;
                JPanel panel = (JPanel) comp;
                int count = panel.getComponentCount();
                for (int k=0; k<count; k++) {
                  Component co = panel.getComponent(k);
                  String str = "";
                  if (co instanceof JLabel) {
                    str = ((JLabel) co).getText();
                  } else if (co instanceof JEditorPane) {
                    JEditorPane pane = (JEditorPane) co;
                    isHTMLformatted = pane.getContentType().equalsIgnoreCase("text/html");
                    String t = pane.getText();
                    if (t != null)
                      s = t;
                  } else if (co instanceof JTextComponent) {
                    String t = ((JTextComponent) co).getText();
                    if (t != null)
                      s = t;
                  }
                  if (str != null) {
                    if (str.startsWith("<html>"))
                      s += str;
                    else
                      s += Misc.encodePlainIntoHtml(str + (k < count-1 ? ", " : ""));
                  }
                }
              }
              // make column spacing by adding blank character, don't use cellpadding or cellspacing as those make row spacing too large...
              if (s != null && s.trim().length() > 0) {
                if (isHTMLformatted)
                  s += " &nbsp;";
                else
                  s += "  ";
              }
              // make short columns not wrapable
              if (isNOwrap)
                sb.append("<td NOWRAP align='left' valign='top'>");
              else
                sb.append("<td align='left' valign='top'>");
              // set cell font
              sb.append("<font size='-2' face="+HTML_utils.DEFAULT_FONTS_QUOTED+">");
              //set cell data
              if (s != null && s.trim().length() > 0) {
                if (isHTMLformatted) {
                  s = ArrayUtils.replaceKeyWords(s,
                      new String[][] {
                        {"<body>", ""},
                        {"<BODY>", ""},
                        {"</body>", ""},
                        {"</BODY>", ""},
                        {"<html>", ""},
                        {"<HTML>", ""},
                        {"</html>", ""},
                        {"</HTML>", ""},
                  });
                  sb.append(s);
                } else {
                  sb.append(Misc.encodePlainIntoHtml(s));
                }
              } else {
                sb.append("&nbsp;");
              }
              sb.append("</font>");
              sb.append("</td>");
            }
            sb.append("</tr>");
          }
          sb.append("</table></body></html>");

          String content = sb.toString();
          com.CH_gui.print.DocumentRenderer renderer = new com.CH_gui.print.DocumentRenderer();
          JEditorPane normalization = new JEditorPane("text/html", content);
          String normalizedText = normalization.getText();
          normalization = null; // this may take a lot of memory so make it available for cleanup asap
          // Use our own pane to fix display of internal icons because it will adjust document base
          //JEditorPane pane = new JEditorPane("text/html", "<html></html>");
          JEditorPane pane = new HTML_ClickablePane("");
          MsgPanelUtils.setMessageContent(normalizedText, true, pane);
          renderer.setDocument(pane);
          Window w = SwingUtilities.windowForComponent(MsgActionTable.this);
          if (w instanceof Dialog)
            new com.CH_gui.print.PrintPreview(renderer, "Print Preview", (Dialog) w);
          else if (w instanceof Frame)
            new com.CH_gui.print.PrintPreview(renderer, "Print Preview", (Frame) w);
        }
      }
    }
  }

  /**
   * Show the Compose Message dialog with original message filled as draft.
   */
  private class UseAsDraftAction extends AbstractActionTraced {
    public UseAsDraftAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_New_Message_from_Draft_..."), Images.get(ImageNums.MAIL_COMPOSE_FROM_DRAFT16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_New_Message_from_selected_Draft_message."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_COMPOSE_FROM_DRAFT24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_New_from_Draft"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      MsgLinkRecord msgLink = (MsgLinkRecord) getSelectedRecord();
      if (msgLink != null) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        MsgDataRecord msgData = cache.getMsgDataRecord(msgLink.msgId);
        boolean deleteDraftAfterSave = msgLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER && msgLink.ownerObjId.equals(cache.getUserRecord().draftFolderId);
        if (msgData != null && msgData.isTypeAddress()) {
          new AddressFrame(msgLink, deleteDraftAfterSave);
        } else {
          new MessageFrame(msgLink, deleteDraftAfterSave);
        }
      }
    }
  }

  /**
   * Refresh Message List.
   */
  private class RefreshAction extends AbstractActionTraced {
    public RefreshAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Refresh_Messages"), Images.get(ImageNums.REFRESH16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Refresh_Message_List_from_the_server."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.REFRESH24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Refresh"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
      RecordTableModel tableModel = getTableModel();
      if (tableModel instanceof MsgTableModel) {
        if (((MsgTableModel) tableModel).getMode() == MsgTableModel.MODE_ADDRESS ||
            ((MsgTableModel) tableModel).getMode() == MsgTableModel.MODE_WHITELIST
        ) {
          putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Refresh_Addresses"));
          putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Refresh_Address_List_from_the_server."));
        }
      }
    }
    public void actionPerformedTraced(ActionEvent event) {
      ((MsgTableModel) getTableModel()).refreshData(true);
    }
  }

  /**
   * Mark selected items as READ
   */
  private class MarkAsReadAction extends AbstractActionTraced {
    public MarkAsReadAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Mark_as_Read"), Images.get(ImageNums.FLAG_BLANK_SMALL));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Mark_all_selected_messages_as_read."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLAG_BLANK24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Read"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      markSelectedAs(StatRecord.FLAG_OLD);
    }
  }

  /**
   * Mark selected items as UNREAD
   */
  private class MarkAsUnreadAction extends AbstractActionTraced {
    public MarkAsUnreadAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Mark_as_Unread"), Images.get(ImageNums.FLAG_GREEN_SMALL));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Mark_all_selected_messages_as_unread."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLAG_GREEN24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Unread"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      markSelectedAs(StatRecord.FLAG_NEW);
    }
  }

  /**
   * Mark all items in the folder as READ
   */
  private class MarkAllReadAction extends AbstractActionTraced {
    public MarkAllReadAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Mark_All_Read"), Images.get(ImageNums.FLAG_BLANK_DOUBLE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Mark_all_messages_in_selected_folder_as_read."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLAG_BLANK_DOUBLE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_All_Read"));
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
      super(com.CH_gui.lang.Lang.rb.getString("action_Clone_Message_View"), Images.get(ImageNums.CLONE_MSG16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Display_message_table_in_its_own_window."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CLONE_MSG24));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
      RecordTableModel tableModel = getTableModel();
      if (tableModel instanceof MsgTableModel) {
        if (((MsgTableModel) tableModel).getMode() == MsgTableModel.MODE_ADDRESS ||
            ((MsgTableModel) tableModel).getMode() == MsgTableModel.MODE_WHITELIST
        ) {
          putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Clone_Address_View"));
          putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Display_address_table_in_its_own_window."));
          putValue(Actions.MENU_ICON, Images.get(ImageNums.CLONE_ADDR16));
          putValue(Actions.TOOL_ICON, Images.get(ImageNums.CLONE_ADDR24));
        }
      }
    }
    public void actionPerformedTraced(ActionEvent event) {
      FolderPair parentFolderPair = ((MsgTableModel) getTableModel()).getParentFolderPair();
      if (parentFolderPair != null) {
        if (parentFolderPair.getFolderRecord().isChatting())
          new ChatTableFrame(parentFolderPair);
        else if (parentFolderPair.getFolderRecord().folderType.shortValue() == FolderRecord.POSTING_FOLDER)
          new PostTableFrame(parentFolderPair);
        else if (parentFolderPair.getFolderRecord().folderType.shortValue() == FolderRecord.ADDRESS_FOLDER)
          new AddressTableFrame(parentFolderPair);
        else if (parentFolderPair.getFolderRecord().folderType.shortValue() == FolderRecord.WHITELIST_FOLDER)
          new WhiteListTableFrame(parentFolderPair);
        else
          new MsgTableFrame(parentFolderPair);
      }
    }
  }

  /**
   * Open history stat dialog for selected message link object.
   */
  private class TracePrivilegeAndHistoryAction extends AbstractActionTraced {
    public TracePrivilegeAndHistoryAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Trace_Message_Access"));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Trace_Message_Access"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) getSelectedRecords();
      if (msgLinks != null && msgLinks.length >= 1) {
        Window w = SwingUtilities.windowForComponent(MsgActionTable.this);
        if (w instanceof Frame) new TraceRecordDialog((Frame) w, msgLinks);
        else if (w instanceof Dialog) new TraceRecordDialog((Dialog) w, msgLinks);
      }
    }
  }

  /**
   * Open User Invitation dialog for sender of the message.
   */
  private class InviteSenderAction extends AbstractActionTraced {
    public InviteSenderAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Invite_Sender"), Images.get(ImageNums.HANDSHAKE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Invite_Message_Sender_to_Contact_List"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.HANDSHAKE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Invite"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      Vector inviteUIDsV = new Vector();
      Vector inviteEmailAddrsV = new Vector();
      StringBuffer inviteEmailAddrs = new StringBuffer();

      MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) getSelectedRecords();
      if (msgLinks != null) {
        Long[] msgIDs = MsgLinkRecord.getMsgIDs(msgLinks);
        msgIDs = (Long[]) ArrayUtils.removeDuplicates(msgIDs);
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        for (int i=0; i<msgIDs.length; i++) {
          MsgDataRecord dataRecord = cache.getMsgDataRecord(msgIDs[i]);
          if (dataRecord != null) {
            if (dataRecord.isEmail() || dataRecord.isTypeAddress()) {
              String emailAddr = "";
              if (dataRecord.isEmail()) {
                emailAddr = dataRecord.getFromEmailAddress();
              } else {
                emailAddr = dataRecord.email;
              }
              if (emailAddr != null && emailAddr.trim().length() > 0) {
                if (!inviteEmailAddrsV.contains(emailAddr)) {
                  inviteEmailAddrsV.addElement(emailAddr);
                  inviteEmailAddrs.append(emailAddr);
                  inviteEmailAddrs.append("; ");
                }
              }
            } else {
              Long withUserId = dataRecord.senderUserId;
              Long userId = cache.getMyUserId();
              ContactRecord cRec = cache.getContactRecordOwnerWith(userId, dataRecord.senderUserId);
              if (cRec == null && !userId.equals(dataRecord.senderUserId)) {
                if (!inviteUIDsV.contains(withUserId)) {
                  inviteUIDsV.addElement(withUserId);
                }
              }
            }
          }
        }

        boolean anyToInvite = inviteUIDsV.size() > 0 || inviteEmailAddrsV.size() > 0;
        if (!anyToInvite) {
          // if no email address available, at least show empty dialog
          inviteEmailAddrsV.addElement("");
          inviteEmailAddrs.append("");
        }
        Window w = SwingUtilities.windowForComponent(MsgActionTable.this);
        if (inviteUIDsV.size() > 0) {
          if (w instanceof Frame) new InitiateContactDialog((Frame) w, RecordUtils.getIDs2(inviteUIDsV));
          else if (w instanceof Dialog) new InitiateContactDialog((Dialog) w, RecordUtils.getIDs2(inviteUIDsV));
        }
        if (inviteEmailAddrsV.size() > 0) {
          if (w instanceof Dialog) new InviteByEmailDialog((Dialog) w, inviteEmailAddrs.toString());
          else if (w instanceof Frame) new InviteByEmailDialog((Frame) w, inviteEmailAddrs.toString());
        }
      }
    }
  }

  /**
   * Open User Invitation dialog for sender of the message.
   */
  private class AddSenderToAddressBook extends AbstractActionTraced {
    public AddSenderToAddressBook(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Add_Sender_to_Address_Book"), Images.get(ImageNums.ADDRESS_ADD16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Add_Sender_to_Address_Book"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.ADDRESS_ADD24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Add_Sender"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) getSelectedRecords();
      if (msgLinks != null) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        Long[] msgIDs = MsgLinkRecord.getMsgIDs(msgLinks);
        MsgDataRecord[] msgDatas = cache.getMsgDataRecords(msgIDs);
        Vector emailNicksV = new Vector();
        Vector emailStringRecordsV = new Vector();
        for (int i=0; i<msgDatas.length; i++) {
          getEmailNickAndAddress(msgDatas[i], emailNicksV, emailStringRecordsV, true);
        }
        if (emailStringRecordsV.size() > 0)
          MsgComposePanel.checkEmailAddressesForAddressBookAdition_Threaded(MsgActionTable.this, emailNicksV, emailStringRecordsV, true, new FolderFilter(FolderRecord.ADDRESS_FOLDER));
      }
    }
  }

  public static void getEmailNickAndAddress(MsgDataRecord msgData, Vector emailNicksV, Vector emailStringRecordsV, boolean showAddressFrameIfNoEmail) {
    String emailAddress = null;
    UserRecord senderUser = null;
    FolderPair addrBook = null;
    FolderPair[] fPairs = null;
    if (msgData.isTypeMessage() && (msgData.isEmail() || msgData.getFromEmailAddress() != null)) {
      emailAddress = msgData.getFromEmailAddress();
    } else {
      senderUser = FetchedDataCache.getSingleInstance().getUserRecord(msgData.senderUserId);
      if (senderUser != null) {
        String[] emailStrings = UserOps.getCachedDefaultEmail(senderUser, true);
        emailAddress = emailStrings != null ? emailStrings[2] : null;
        if (emailAddress == null || emailAddress.length() == 0) {
          if (showAddressFrameIfNoEmail) {
            String fullName = senderUser.handle;
            XMLElement draftData = ContactInfoPanel.getContent(new XMLElement[] {
                                            NamePanel.getContent(fullName, null, null, null),
                                            EmailPanel.getContent(EmailPanel.getTypes(), new String[] { emailAddress }, null, 0) });
            if (addrBook == null) {
              addrBook = FolderOps.getOrCreateAddressBook(MainFrame.getServerInterfaceLayer());
              fPairs = addrBook != null ? new FolderPair[] { addrBook } : null;
            }
            new AddressFrame(fullName, fPairs, draftData);
          }
        }
      } else {
        emailAddress = msgData.senderUserId + "@" + URLs.getElements(URLs.DOMAIN_MAIL)[0];
      }
    }
    if (emailAddress != null && emailAddress.length() > 0) {
      if (senderUser != null)
        emailNicksV.addElement(senderUser.handle);
      else
        emailNicksV.addElement(EmailRecord.getPersonalOrNick(emailAddress));
      emailStringRecordsV.addElement(emailAddress);
    }
  }

  /**
   * Post a Reply Message to the selected message.
   * Post Reply to Folder --- OR --- Edit Address
   */
  private class PostReplyOrEditAction extends AbstractActionTraced {
    public PostReplyOrEditAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Post_Reply_to_Folder_..."), Images.get(ImageNums.REPLY_TO_MSG16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Post_a_reply_for_the_selected_message_into_the_folder."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.REPLY_TO_MSG24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Post_Reply"));
      RecordTableModel tableModel = getTableModel();
      if (tableModel instanceof MsgTableModel) {
        if (((MsgTableModel) tableModel).getMode() == MsgTableModel.MODE_ADDRESS ||
            ((MsgTableModel) tableModel).getMode() == MsgTableModel.MODE_WHITELIST
        ) {
          putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Edit_..."));
          putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Edit_..."));
          putValue(Actions.MENU_ICON, Images.get(ImageNums.ADDRESS_EDIT16));
          putValue(Actions.TOOL_ICON, Images.get(ImageNums.ADDRESS_EDIT24));
          putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Edit"));
        }
      }
    }
    public void actionPerformedTraced(ActionEvent event) {
      MsgLinkRecord msgLink = (MsgLinkRecord) getSelectedRecord();
      if (msgLink != null) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        FolderRecord fRec = cache.getFolderRecord(msgLink.ownerObjId);
        if (fRec != null) {
          FolderShareRecord sRec = cache.getFolderShareRecordMy(fRec.folderId, true);
          if (sRec != null) {
            if (selectionObjType == MsgDataRecord.OBJ_TYPE_MSG) {
              if (sRec.canWrite.shortValue() == FolderShareRecord.YES) {
                FolderPair folderPair = new FolderPair(sRec, fRec);
                new MessageFrame(folderPair, msgLink);
              } else {
                MessageDialog.showWarningDialog(MsgActionTable.this, com.CH_gui.lang.Lang.rb.getString("msg_You_do_not_have_a_write_privilege_to_the_folder..."), com.CH_gui.lang.Lang.rb.getString("msgTitle_Folder_write_access_denied."));
              }
            } else if (selectionObjType == MsgDataRecord.OBJ_TYPE_ADDR) {
              if (sRec.canWrite.shortValue() == FolderShareRecord.YES && sRec.canDelete.shortValue() == FolderShareRecord.YES) {
                new AddressFrame(msgLink, true);
              } else {
                MessageDialog.showWarningDialog(MsgActionTable.this, com.CH_gui.lang.Lang.rb.getString("msg_You_do_not_have_both_write_and_delete_privilege_to_the_folder..."), com.CH_gui.lang.Lang.rb.getString("msgTitle_Folder_edit_access_denied."));
              }
            }
          }
        }
      }
    } // end actionPerformed();
  } // end inner class PostReplyOrEditAction

  /**
   * Switch between threaded and non-threaded view.
   */
  private class ThreadedViewAction extends AbstractActionTraced {
    private String propertyName = MiscGui.getVisualsKeyName(MsgActionTable.this, PROPERTY_NAME_THREADED);
    public ThreadedViewAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Group_Messages_by_Conversation"));
      if (MsgActionTable.this instanceof ChatActionTable)
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Group_Chat_Messages_by_Conversation"));
      else if (MsgActionTable.this instanceof PostActionTable)
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Group_Postings_by_Conversation"));
      else if (MsgActionTable.this instanceof MsgSentActionTable)
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Group_Sent_Mail_by_Conversation"));
      else
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Group_Mail_by_Conversation"));

      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
      //putValue(Actions.REMOVABLE_MENU, Boolean.TRUE);
      MsgTableSorter mts = (MsgTableSorter) getJSortedTable().getModel();
      Boolean threaded = Boolean.valueOf(GlobalProperties.getProperty(propertyName, "true"));
      putValue(Actions.STATE_CHECK, threaded);
      mts.setThreaded(threaded.booleanValue());
      mts.resort();

      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      JSortedTable jst = getJSortedTable();
      MsgTableSorter mts = (MsgTableSorter) jst.getModel();
      boolean oldValue = mts.isThreaded();
      boolean newValue = ((Boolean) getThreadedAction().getValue(Actions.STATE_CHECK)).booleanValue();
      if (oldValue != newValue) {
        mts.setThreaded(newValue);
        mts.resort();
      }
      GlobalProperties.setProperty(propertyName, "" + mts.isThreaded());
    } // end actionPerformed();
  } // end inner class ThreadedViewAction

  /**
   * Open a message in its own window view.
   */
  private class OpenInSeperateViewAction extends AbstractActionTraced {
    public OpenInSeperateViewAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Open"), Images.get(ImageNums.CLONE_FILE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Open_the_selected_object_in_its_own_view."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CLONE_FILE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Open"));
      putValue(Actions.IN_POPUP, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FolderPair parentFolderPair = ((MsgTableModel) getTableModel()).getParentFolderPair();
      if (parentFolderPair != null) {
        MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) getSelectedRecords();
        if (msgLinks != null && msgLinks.length > 0) {
          new MsgPreviewFrame(MsgActionTable.this.getTableModel().getParentFolderPair(), msgLinks, msgLinks.length == 1 ? MsgActionTable.this : null);
          //new MsgPreviewFrame(MsgActionTable.this.getTableModel().getParentFolderPair(), msgLinks);
        }
      }
    } // end actionPerformed();
  } // end inner class OpenInSeperateViewAction



  private void markSelectedAs(Short newMark) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgActionTable.class, "markSelectedAs(Short newMark)");
    if (trace != null) trace.args(newMark);

    MsgLinkRecord[] records = (MsgLinkRecord[]) getSelectedRecords();
    markRecordsAs(records, newMark);

    if (trace != null) trace.exit(MsgActionTable.class);
  }
  private void markAllAs(Short newMark) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgActionTable.class, "markAllAs(Short newMark)");
    if (trace != null) trace.args(newMark);

    MsgTableModel tableModel = (MsgTableModel) getTableModel();
    Vector linksV = new Vector();
    for (int i=0; i<tableModel.getRowCount(); i++) {
      linksV.addElement(tableModel.getRowObject(i));
    }
    if (linksV.size() > 0) {
      MsgLinkRecord[] links = new MsgLinkRecord[linksV.size()];
      linksV.toArray(links);
      markRecordsAs(links, newMark);
    }

    if (trace != null) trace.exit(MsgActionTable.class);
  }
  private void markRecordsAs(MsgLinkRecord[] records, Short newMark) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgActionTable.class, "markRecordsAs(MsgLinkRecord[] records, Short newMark)");
    if (trace != null) trace.args(records, newMark);

    if (records != null && records.length > 0) {
      // gather all stats which need to be updated
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      Vector statsV = new Vector();
      for (int i=0; i<records.length; i++) {
        StatRecord statRecord = cache.getStatRecord(records[i].msgLinkId, FetchedDataCache.STAT_TYPE_MESSAGE);
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

        ServerInterfaceLayer serverInterfaceLayer = MainFrame.getServerInterfaceLayer();
        serverInterfaceLayer.submitAndReturn(new MessageAction(CommandCodes.STAT_Q_UPDATE, request));
      }
    }

    if (trace != null) trace.exit(MsgActionTable.class);
  }


  /**
   * Move or Copy action has been activated, send an appropriate request.
   */
  private void moveOrCopyAction(boolean isMove) {
    moveOrCopyAction(isMove, null, null);
  }
  private void moveOrCopyAction(boolean isMove, MsgLinkRecord[] mLinks, FolderPair chosenFolderPair) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgActionTable.class, "moveOrCopyAction(boolean isMove, MsgLinkRecord[] mLinks, FolderPair chosenFolderPair)");
    if (trace != null) trace.args(isMove);
    if (trace != null) trace.args(mLinks, chosenFolderPair);

    if (mLinks == null)
      mLinks = (MsgLinkRecord[]) getSelectedRecords();
    if (chosenFolderPair == null)
      chosenFolderPair = getMoveCopyDestination(isMove);
    // links are now gathered, now select the next record before we request move...
    if (isMove) {
      FolderPair fPair = MsgActionTable.this.getTableModel().getParentFolderPair();
      if (fPair != null && fPair.getFolderRecord().folderType.shortValue() == FolderRecord.MESSAGE_FOLDER && fPair.getFolderShareRecord().canDelete.shortValue() == FolderShareRecord.YES) {
        if (chosenFolderPair != null && chosenFolderPair.getFolderShareRecord().canWrite.shortValue() == FolderShareRecord.YES) {
          if (msgPreviewMode || fPair.getFolderRecord().folderType.shortValue() == FolderRecord.MESSAGE_FOLDER) {
            if (!pressedNext())
              pressedPrev();
          }
        }
      }
    }
    // carry on with the action ...
    doMoveOrCopyOrSaveAttachmentsAction(isMove, chosenFolderPair, mLinks);
    if (trace != null) trace.exit(MsgActionTable.class);
  }
  public static void doMoveOrCopyOrSaveAttachmentsAction(boolean isMove, FolderPair chosenFolderPair, MsgLinkRecord[] mLinks) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgActionTable.class, "doMoveOrCopyOrSaveAttachmentsAction(boolean isMove, FolderPair chosenFolderPair, MsgLinkRecord[] mLinks)");
    if (trace != null) trace.args(isMove);
    if (trace != null) trace.args(chosenFolderPair, mLinks);
    if (chosenFolderPair != null) {
      Msg_MoveCopy_Rq request = prepareMoveCopyRequest(chosenFolderPair.getFolderShareRecord(), mLinks);
      if (request != null) { // mLinks can not be empty if request was generated
        ServerInterfaceLayer serverInterfaceLayer = MainFrame.getServerInterfaceLayer();
        int actionCode = isMove ? CommandCodes.MSG_Q_MOVE : CommandCodes.MSG_Q_COPY;
        // if messages are attachments, switch MOVE request to SAVE ATTACHMENTS request
        if (mLinks[0].ownerObjType.shortValue() == Record.RECORD_TYPE_MESSAGE)
          actionCode = CommandCodes.MSG_Q_SAVE_MSG_ATT;

        // create runnable to run after reply is received that will update new object count
        FolderRecord sourceFolder = null;
        if (isMove) {
          if (mLinks[0].ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
            sourceFolder = FetchedDataCache.getSingleInstance().getFolderRecord(mLinks[0].ownerObjId);
          }
        }

        // create runnable to run after reply is received that will update new object count
        final FolderRecord _sourceFolder = sourceFolder;
        Runnable newCountUpdate = null;
        if (sourceFolder != null) {
          newCountUpdate = new Runnable() {
            public void run() {
              FetchedDataCache.getSingleInstance().statUpdatesInFoldersForVisualNotification(new FolderRecord[] { _sourceFolder });
            }
          };
        }

        serverInterfaceLayer.submitAndReturn(new MessageAction(actionCode, request), 30000, newCountUpdate, newCountUpdate);
      }

    }
    if (trace != null) trace.exit(MsgActionTable.class);
  }


  /**
   * Show a Move / Copy dialog and get the chosen destination FolderPair.
   */
  private FolderPair getMoveCopyDestination(boolean isMove) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgActionTable.class, "getMoveCopyDestination()");

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();

    FolderRecord[] allFolderRecords = cache.getFolderRecords();
    FolderPair[] allFolderPairs = CacheUtilities.convertRecordsToPairs(allFolderRecords);
    allFolderPairs = (FolderPair[]) FolderFilter.MOVE_FOLDER.filterInclude(allFolderPairs);

    Window w = SwingUtilities.windowForComponent(MsgActionTable.this);

    String title = isMove ? com.CH_gui.lang.Lang.rb.getString("title_Move_to_Folder") : com.CH_gui.lang.Lang.rb.getString("title_Copy_to_Folder");

    // An invalid destination will be the folder from which we are moving/copying message(s).
    FolderPair parentFolderPair = ((MsgTableModel) getTableModel()).getParentFolderPair();
    FolderPair[] forbidenPairs = null;
    if (parentFolderPair != null)
      forbidenPairs = new FolderPair[] { parentFolderPair };
    else
      forbidenPairs = new FolderPair[0];

    // An invalid destinations are the non-msg folders
    forbidenPairs = (FolderPair[]) ArrayUtils.concatinate(forbidenPairs, FolderFilter.NON_MSG_FOLDERS.filterInclude(allFolderPairs));

    // Since we are moving messages/postings only, not folders, descendant destination folders are always ok.
    boolean isDescendantOk = true;

    Move_NewFld_Dialog d = null;
    RecordFilter filter = FolderFilter.MOVE_MSG;
    if (w instanceof Frame) d = new Move_NewFld_Dialog((Frame) w, allFolderPairs, forbidenPairs, null, title, isDescendantOk, cache, filter);
    else if (w instanceof Dialog) d = new Move_NewFld_Dialog((Dialog) w, allFolderPairs, forbidenPairs, null, title, isDescendantOk, cache, filter);

    FolderPair chosenPair = null;
    if (d != null) {
      chosenPair = d.getChosenDestination();
    }

    if (trace != null) trace.exit(MsgActionTable.class, chosenPair);
    return chosenPair;
  }


  /**
   * Prepares a Move or Copy request for currently selected MsgLinks.
   */
  public static Msg_MoveCopy_Rq prepareMoveCopyRequest(FolderShareRecord destination, MsgLinkRecord[] mLinks) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgActionTable.class, "prepareMoveCopyRequest(FolderPair destination, MsgLinkRecord[] mLinks)");
    if (trace != null) trace.args(destination, mLinks);

    Msg_MoveCopy_Rq request = null;

    if (mLinks != null && mLinks.length > 0) {
      request = new Msg_MoveCopy_Rq();
      request.toShareId = destination.shareId;

      // gather owners of given MsgLinkRecords
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();

      short ownerObjType = mLinks[0].ownerObjType.shortValue();
      // We are working with regular messages in folders
      if (ownerObjType == Record.RECORD_TYPE_FOLDER) {
        Long[] folderIDs = MsgLinkRecord.getOwnerObjIDs(mLinks, Record.RECORD_TYPE_FOLDER);
        request.fromShareIDs = RecordUtils.getIDs(cache.getFolderSharesMyForFolders(folderIDs, true));
      }
      // We are working with message attachments
      else if (ownerObjType == Record.RECORD_TYPE_MESSAGE) {
        Long[] ownerObjIDs = MsgLinkRecord.getOwnerObjIDs(mLinks, Record.RECORD_TYPE_MESSAGE);
        // Find some message links for the owner messages
        Vector msgLinksV = new Vector();
        for (int i=0; i<ownerObjIDs.length; i++) {
          MsgLinkRecord[] msgLinksForMsg = cache.getMsgLinkRecordsForMsg(ownerObjIDs[i]);
          if (msgLinksForMsg != null && msgLinksForMsg.length > 0)
            msgLinksV.addElement(msgLinksForMsg[0]);
        }
        MsgLinkRecord[] msgLinks = new MsgLinkRecord[msgLinksV.size()];
        msgLinksV.toArray(msgLinks);

        request.fromMsgLinkIDs = RecordUtils.getIDs(msgLinks);

        // if parent message links don't reside directly in folders, don't specify any shares as server will find access path by looking at parent link IDs
        Long[] folderIDs = MsgLinkRecord.getOwnerObjIDs(msgLinks, Record.RECORD_TYPE_FOLDER);
        request.fromShareIDs = RecordUtils.getIDs(cache.getFolderSharesMyForFolders(folderIDs, true));
      } else {
        throw new IllegalArgumentException("Don't know how to handle owner type " + ownerObjType);
      }


      MsgLinkRecord[] clonedLinks = (MsgLinkRecord[]) RecordUtils.cloneRecords(mLinks);
      // give the new encrypted symmetric keys for the new destination folder
      BASymmetricKey destinationSymKey = destination.getSymmetricKey();
      for (int i=0; i<clonedLinks.length; i++) {
        // check if we have access to the message's content, if not, leave it in recryption pending state for the original user recipient to change from asymetric to symetric
        if (clonedLinks[i].getSymmetricKey() != null) {
          clonedLinks[i].seal(destinationSymKey);
        }
      }
      request.msgLinkRecords = clonedLinks;
    }

    if (trace != null) trace.exit(MsgActionTable.class, request);
    return request;
  }


  public void setParentViewTable(RecordTableScrollPane parentViewTable) {
    this.parentViewTable = parentViewTable;
  }

  /**
   * Iterate Next
   */
  public boolean pressedNext() {
    boolean selected = false;
    if (parentViewTable != null) {
      Record currentSelection = parentViewTable.getSelectedRecord();
      selected = parentViewTable.advanceSelection(true, true, null, currentSelection, -1, false);
      if (selected) {
        Record selectedRec = parentViewTable.getSelectedRecord();
        if (selectedRec instanceof MsgLinkRecord)
          switchViewItem((MsgLinkRecord) selectedRec);
      }
      setEnabledActions();
    } else {
      selected = super.pressedNext();
    }
    return selected;
  }

  /**
   * Iterate Previous
   */
  public boolean pressedPrev() {
    boolean selected = false;
    if (parentViewTable != null) {
      Record currentSelection = parentViewTable.getSelectedRecord();
      selected = parentViewTable.advanceSelection(false, true, null, currentSelection, -1, false);
      if (selected) {
        Record selectedRec = parentViewTable.getSelectedRecord();
        if (selectedRec instanceof MsgLinkRecord)
          switchViewItem((MsgLinkRecord) selectedRec);
      }
      setEnabledActions();
    } else {
      selected = super.pressedPrev();
    }
    return selected;
  }

  private void switchViewItem(MsgLinkRecord singleViewItem) {
    getTableModel().setFilter(new MsgFilter(new Long[] { singleViewItem.getId() }, singleViewItem.ownerObjType, singleViewItem.ownerObjId));
    getTableModel().setData(new Record[] { singleViewItem });
    getJSortedTable().selectAll();
    fireRecordSelectionChanged();
  }

  /****************************************************************************/
  /*        A c t i o n P r o d u c e r I
  /****************************************************************************/

  /**
   * @return all the acitons that this objects produces.
   * Special consideration for MsgTableStarterFrame where minimum of actions are returned
   */
  public Action[] getActions() {
    if (actions == null) initActions();
    Action[] filteredActions = (Action[]) actions.clone();
    Window w = SwingUtilities.windowForComponent(this);
    if (w instanceof MsgTableStarterFrame) {
      filteredActions[NEW_ACTION] = null;
      filteredActions[COPY_ACTION] = null;
      filteredActions[MOVE_ACTION] = null;
      filteredActions[FORWARD_ACTION] = null;
      filteredActions[OPEN_IN_SEPERATE_WINDOW_ACTION] = null;
      filteredActions[INVITE_SENDER_ACTION] = null;
      filteredActions[POST_REPLY__OR__EDIT_ACTION] = null;
      filteredActions[OPEN_IN_SEPERATE_VIEW] = null;
      filteredActions[THREADED_VIEW_ACTION] = null;
      filteredActions[NEW_FROM_DRAFT_ACTION] = null;
      filteredActions[ITERATE_NEXT_ACTION] = null;
      filteredActions[ITERATE_PREV_ACTION] = null;
      filteredActions[ADD_SENDER_TO_ADDRESS_BOOK_ACTION] = null;
      filteredActions[REVOKE_ACTION] = null;
    }
    return filteredActions;
  }

  /**
   * Enables or Disables actions based on the current state of the Action Producing component.
   */
  public void setEnabledActions() {
    if (actions == null) initActions();

    // force selection of all rows if only 1 row in message preview mode
    if (msgPreviewMode && getTableModel().getRowCount() == 1 && getJSortedTable().getSelectedRowCount() == 0) {
      getJSortedTable().selectAll();
      fireRecordSelectionChanged();
    }

    // Always enable compose message action.
    actions[MSG_COMPOSE_ACTION].setEnabled(true);

    if (!msgPreviewMode) {
      // Always enable the threaded view checkbox.
      actions[THREADED_VIEW_ACTION].setEnabled(true);
      // Always enable Invite action
      actions[INVITE_ACTION].setEnabled(true);
      // Always enable Filter action
      actions[FILTER_ACTION].setEnabled(true);
      // Always enable sort actions
      actions[SORT_ASC_ACTION].setEnabled(true);
      actions[SORT_DESC_ACTION].setEnabled(true);
      actions[CUSTOMIZE_COLUMNS_ACTION].setEnabled(true);
      for (int i=SORT_BY_FIRST_COLUMN_ACTION; i<SORT_BY_FIRST_COLUMN_ACTION+NUM_OF_SORT_COLUMNS; i++) {
        if (actions[i] != null)
          actions[i].setEnabled(true);
      }
    }

    MsgLinkRecord[] records = (MsgLinkRecord[]) getSelectedRecords();
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    Long totalSize = null;
    boolean anyUnknownSizes = false;
    // check for attachments
    boolean anyAttachments = false;
    if (records != null && records.length > 0) {
      MsgDataRecord[] msgDatas = cache.getMsgDataRecordsForLinks(RecordUtils.getIDs(records));
      anyAttachments = MsgDataRecord.sumAttachedFiles(msgDatas) > 0 || MsgDataRecord.sumAttachedMsgs(msgDatas) > 0;
    }

    // Check for read and unread messages and if initiation contact with sender is ok.
    boolean anyRead = false;
    boolean anyUnread = false;
    boolean initiateContactOk = false;
    boolean anyMsgsSelected = false;
    boolean anyAddrsSelected = false;
    boolean isEditOk = true;
    if (records != null) {
      for (int i=0; i<records.length; i++) {
        // count the message towards total selected size
        MsgDataRecord dataRecord = cache.getMsgDataRecord(records[i].msgId);
        if (dataRecord != null) {
          if (dataRecord.isTypeMessage()) {
            isEditOk = false;
            anyMsgsSelected = true;
            // Check if we have contact with the message sender, if not we should be able to initiate contact
            {
              if (dataRecord.isEmail()) {
                initiateContactOk = true;
              } else {
                Long userId = cache.getMyUserId();
                ContactRecord cRec = cache.getContactRecordOwnerWith(userId, dataRecord.senderUserId);
                if (cRec == null && !userId.equals(dataRecord.senderUserId)) {
                  initiateContactOk = true;
                }
              }
            }
          } else if (dataRecord.isTypeAddress()) {
            anyAddrsSelected = true;
          }
        }
        if (dataRecord == null || dataRecord.recordSize == null) {
          anyUnknownSizes = true;
        } else {
          long len = dataRecord.recordSize.intValue();
          totalSize = new Long(len + (totalSize != null ? totalSize.longValue() : 0L));
        }
        if (!(anyRead && anyUnread)) {
          StatRecord statRecord = cache.getStatRecord(records[i].msgLinkId, FetchedDataCache.STAT_TYPE_MESSAGE);
          if (statRecord != null) {
            if (statRecord.mark.equals(StatRecord.FLAG_OLD))
              anyRead = true;
            else if (statRecord.mark.equals(StatRecord.FLAG_NEW))
              anyUnread = true;
          }
        }
      }
    }

    boolean anyUnreadGlobal = anyUnread;
    if (!anyUnread) {
      MsgTableModel tableModel = (MsgTableModel) getTableModel();
      for (int i=0; i<tableModel.getRowCount(); i++) {
        Record rec = tableModel.getRowObject(i);
        if (rec instanceof MsgLinkRecord) {
          MsgLinkRecord msgLink = (MsgLinkRecord) rec;
          StatRecord statRecord = cache.getStatRecord(msgLink.msgLinkId, FetchedDataCache.STAT_TYPE_MESSAGE);
          if (statRecord != null && statRecord.mark.equals(StatRecord.FLAG_NEW)) {
            anyUnreadGlobal = true;
            break;
          }
        }
      }
    }

    boolean isPostReplyToFolderOk = true;
    boolean isParentNonCategoryFolder = false;
    boolean isPrintOk = false;
    {
      RecordTableModel model = getTableModel();
      if (model != null) {
        FolderPair fPair = model.getParentFolderPair();
        if (fPair != null && fPair.getId() != null) {
          if (fPair.getFolderRecord() != null)
            isParentNonCategoryFolder = !fPair.getFolderRecord().isCategoryType();
          isPrintOk = !(fPair.getFolderRecord() != null && fPair.getFolderRecord().isCategoryType());
          UserRecord uRec = cache.getUserRecord();
          if (uRec != null) {
            FolderRecord fRec = fPair.getFolderRecord();
            isPostReplyToFolderOk = fRec.folderType.shortValue() != FolderRecord.MESSAGE_FOLDER || !fRec.isSuperRoot(uRec);
          }
        }
      }
    }

    RecordTableScrollPane viewTable = parentViewTable != null ? parentViewTable : this;
    boolean isNextOk = viewTable.advanceSelection(true, true, null, null, -1, true);
    boolean isPrevOk = viewTable.advanceSelection(false, true, null, null, -1, true);
    actions[ITERATE_NEXT_ACTION].setEnabled(isNextOk);
    actions[ITERATE_PREV_ACTION].setEnabled(isPrevOk);

    if (actions[SPLIT_LAYOUT_ACTION] != null) {
      actions[SPLIT_LAYOUT_ACTION].setEnabled(isParentNonCategoryFolder);
      ((SplitLayoutAction) actions[SPLIT_LAYOUT_ACTION]).updateTextAndIcon();
    }

    int count = 0;
    if (records != null) {
      count = records.length;
    }
    if (count == 0) {
      actions[DOWNLOAD_ACTION].setEnabled(false);
      actions[COPY_ACTION].setEnabled(false);
      //if (!msgPreviewMode) {
        actions[MOVE_ACTION].setEnabled(false);
        actions[DELETE_ACTION].setEnabled(false);
        actions[REVOKE_ACTION].setEnabled(false);
      //}
      actions[PROPERTIES_ACTION].setEnabled(false);
      actions[FORWARD_ACTION].setEnabled(false);
      actions[SAVE_ATTACHMENTS_ACTION].setEnabled(false);
      actions[REPLY_TO_SENDER__OR__COMPOSE_TO_ADDRESS_ACTION].setEnabled(false);
      actions[REPLY_TO_ALL_ACTION].setEnabled(false);
      actions[PRINT_ACTION].setEnabled(isPrintOk);
      actions[NEW_FROM_DRAFT_ACTION].setEnabled(false);
      actions[MARK_AS_READ_ACTION].setEnabled(false);
      actions[MARK_AS_UNREAD_ACTION].setEnabled(false);
      if (!msgPreviewMode) {
        actions[MARK_ALL_READ_ACTION].setEnabled(anyUnreadGlobal);
      }
      actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION].setEnabled(false);
      actions[INVITE_SENDER_ACTION].setEnabled(false);
      actions[ADD_SENDER_TO_ADDRESS_BOOK_ACTION].setEnabled(false);
      if (!msgPreviewMode) {
        actions[POST_REPLY__OR__EDIT_ACTION].setEnabled(false);
        actions[OPEN_IN_SEPERATE_VIEW].setEnabled(false);
      }
    } else if (count == 1) {
      actions[DOWNLOAD_ACTION].setEnabled(true);
      actions[COPY_ACTION].setEnabled(true);
      //if (!msgPreviewMode) {
        actions[MOVE_ACTION].setEnabled(isParentNonCategoryFolder && true);
        actions[DELETE_ACTION].setEnabled(isParentNonCategoryFolder && true);
        actions[REVOKE_ACTION].setEnabled(isParentNonCategoryFolder && true);
      //}
      actions[PROPERTIES_ACTION].setEnabled(true);
      actions[FORWARD_ACTION].setEnabled(true);
      actions[SAVE_ATTACHMENTS_ACTION].setEnabled(anyAttachments);
      actions[REPLY_TO_SENDER__OR__COMPOSE_TO_ADDRESS_ACTION].setEnabled(true);
      actions[REPLY_TO_ALL_ACTION].setEnabled(anyMsgsSelected);
      actions[PRINT_ACTION].setEnabled(isPrintOk);
      actions[NEW_FROM_DRAFT_ACTION].setEnabled(true);
      actions[MARK_AS_READ_ACTION].setEnabled(anyUnread);
      actions[MARK_AS_UNREAD_ACTION].setEnabled(anyRead);
      if (!msgPreviewMode) {
        actions[MARK_ALL_READ_ACTION].setEnabled(anyUnreadGlobal);
      }
      actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION].setEnabled(true);
      actions[INVITE_SENDER_ACTION].setEnabled(initiateContactOk || anyAddrsSelected);
      actions[ADD_SENDER_TO_ADDRESS_BOOK_ACTION].setEnabled(anyMsgsSelected);
      if (!msgPreviewMode) {
        actions[POST_REPLY__OR__EDIT_ACTION].setEnabled(isPostReplyToFolderOk || isEditOk);
        actions[OPEN_IN_SEPERATE_VIEW].setEnabled(true);
      }
    } else if (count > 1) {
      actions[DOWNLOAD_ACTION].setEnabled(true);
      actions[COPY_ACTION].setEnabled(true);
      //if (!msgPreviewMode) {
        actions[MOVE_ACTION].setEnabled(isParentNonCategoryFolder && true);
        actions[DELETE_ACTION].setEnabled(isParentNonCategoryFolder && true);
        actions[REVOKE_ACTION].setEnabled(isParentNonCategoryFolder && true);
      //}
      actions[PROPERTIES_ACTION].setEnabled(false);
      actions[FORWARD_ACTION].setEnabled(true);
      actions[SAVE_ATTACHMENTS_ACTION].setEnabled(anyAttachments);
      actions[REPLY_TO_SENDER__OR__COMPOSE_TO_ADDRESS_ACTION].setEnabled(anyAddrsSelected && !anyMsgsSelected);
      actions[REPLY_TO_ALL_ACTION].setEnabled(false);
      actions[PRINT_ACTION].setEnabled(isPrintOk);
      actions[NEW_FROM_DRAFT_ACTION].setEnabled(false);
      actions[MARK_AS_READ_ACTION].setEnabled(anyUnread);
      actions[MARK_AS_UNREAD_ACTION].setEnabled(anyRead);
      if (!msgPreviewMode) {
        actions[MARK_ALL_READ_ACTION].setEnabled(anyUnreadGlobal);
      }
      actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION].setEnabled(true);
      actions[INVITE_SENDER_ACTION].setEnabled(initiateContactOk || anyAddrsSelected);
      actions[ADD_SENDER_TO_ADDRESS_BOOK_ACTION].setEnabled(anyMsgsSelected);
      if (!msgPreviewMode) {
        actions[POST_REPLY__OR__EDIT_ACTION].setEnabled(false);
        actions[OPEN_IN_SEPERATE_VIEW].setEnabled(true);
      }
    }

    Window w = SwingUtilities.windowForComponent(this);
    if (!msgPreviewMode) {
      actions[NEW_ACTION].setEnabled(isParentNonCategoryFolder);
      actions[REFRESH_ACTION].setEnabled(isParentNonCategoryFolder && w != null);
      actions[OPEN_IN_SEPERATE_WINDOW_ACTION].setEnabled(isParentNonCategoryFolder && w != null);
      // change text accordingly to selected items
      InviteAction inviteAction = (InviteAction) actions[INVITE_ACTION];
      inviteAction.updateText();
    }

    /*
    CopyAction copyAction = (CopyAction) actions[COPY_ACTION];
    ForwardToAction forwardToAction = (ForwardToAction) actions[FORWARD_ACTION];
    copyAction.updateText(count);
    forwardToAction.updateText(count);

    if (!msgPreviewMode) {
      MoveAction moveAction = (MoveAction) actions[MOVE_ACTION];
      DeleteAction deleteAction = (DeleteAction) actions[DELETE_ACTION];
      moveAction.updateText(count);
      deleteAction.updateText(count);
    }
     */

    short selectedObjType = 0;
    if (anyMsgsSelected && !anyAddrsSelected)
      selectedObjType = MsgDataRecord.OBJ_TYPE_MSG;
    else if (!anyMsgsSelected && anyAddrsSelected)
      selectedObjType = MsgDataRecord.OBJ_TYPE_ADDR;
    else {
      RecordTableModel model = getTableModel();
      if (model instanceof MsgTableModel) {
        int mode = ((MsgTableModel) model).getMode();
        if (mode == MsgTableModel.MODE_ADDRESS ||
            mode == MsgTableModel.MODE_WHITELIST) {
          selectedObjType = MsgDataRecord.OBJ_TYPE_ADDR;
        } else {
          selectedObjType = MsgDataRecord.OBJ_TYPE_MSG;
        }
      }
    }
    updateActionNamesAndIcons(selectedObjType);

    Stats.setSize((!anyUnknownSizes && totalSize != null) ? totalSize.longValue() : -1L);
  }


  private void updateActionNamesAndIcons(short newSelectionObjType) {
    boolean objTypeChanged = false;
    if (selectionObjType != newSelectionObjType) {
      selectionObjType = newSelectionObjType;
      objTypeChanged = true;
    }
    if (objTypeChanged) {

      if (actions[COPY_ACTION] != null) {
        if (selectionObjType == MsgDataRecord.OBJ_TYPE_MSG) {
          actions[COPY_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Copy_selected_message(s)_to_another_folder."));
          actions[COPY_ACTION].putValue(Actions.MENU_ICON, Images.get(ImageNums.MAIL_COPY16));
          actions[COPY_ACTION].putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_COPY24));
        } else if (selectionObjType == MsgDataRecord.OBJ_TYPE_ADDR) {
          actions[COPY_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Copy_selected_address(es)_to_another_folder."));
          actions[COPY_ACTION].putValue(Actions.MENU_ICON, Images.get(ImageNums.ADDRESS_COPY16));
          actions[COPY_ACTION].putValue(Actions.TOOL_ICON, Images.get(ImageNums.ADDRESS_COPY24));
        } else {
          actions[COPY_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Copy_selected_object(s)_to_another_folder."));
          actions[COPY_ACTION].putValue(Actions.MENU_ICON, Images.get(ImageNums.COPY16));
          actions[COPY_ACTION].putValue(Actions.TOOL_ICON, Images.get(ImageNums.COPY24));
        }
      }

      if (actions[MOVE_ACTION] != null) {
        if (selectionObjType == MsgDataRecord.OBJ_TYPE_MSG) {
          actions[MOVE_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Move_selected_message(s)_to_another_folder."));
          actions[MOVE_ACTION].putValue(Actions.MENU_ICON, Images.get(ImageNums.MAIL_MOVE16));
          actions[MOVE_ACTION].putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_MOVE24));
        } else if (selectionObjType == MsgDataRecord.OBJ_TYPE_ADDR) {
          actions[MOVE_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Move_selected_address(es)_to_another_folder."));
          actions[MOVE_ACTION].putValue(Actions.MENU_ICON, Images.get(ImageNums.ADDRESS_MOVE16));
          actions[MOVE_ACTION].putValue(Actions.TOOL_ICON, Images.get(ImageNums.ADDRESS_MOVE24));
        } else {
          actions[MOVE_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Move_selected_object(s)_to_another_folder."));
          actions[MOVE_ACTION].putValue(Actions.MENU_ICON, Images.get(ImageNums.FILE_MOVE16));
          actions[MOVE_ACTION].putValue(Actions.TOOL_ICON, Images.get(ImageNums.FILE_MOVE24));
        }
      }

      if (actions[DELETE_ACTION] != null) {
        if (selectionObjType == MsgDataRecord.OBJ_TYPE_MSG) {
          actions[DELETE_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Permanently_delete_selected_message(s)."));
          actions[DELETE_ACTION].putValue(Actions.MENU_ICON, Images.get(ImageNums.MAIL_DELETE16));
          actions[DELETE_ACTION].putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_DELETE24));
        } else if (selectionObjType == MsgDataRecord.OBJ_TYPE_ADDR) {
          actions[DELETE_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Permanently_delete_selected_address(es)."));
          actions[DELETE_ACTION].putValue(Actions.MENU_ICON, Images.get(ImageNums.ADDRESS_DELETE16));
          actions[DELETE_ACTION].putValue(Actions.TOOL_ICON, Images.get(ImageNums.ADDRESS_DELETE24));
        } else {
          actions[DELETE_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Permanently_delete_selected_object(s)."));
          actions[DELETE_ACTION].putValue(Actions.MENU_ICON, Images.get(ImageNums.FILE_REMOVE16));
          actions[DELETE_ACTION].putValue(Actions.TOOL_ICON, Images.get(ImageNums.FILE_REMOVE24));
        }
      }

      if (actions[PROPERTIES_ACTION] != null) {
        if (selectionObjType == MsgDataRecord.OBJ_TYPE_MSG) {
          actions[PROPERTIES_ACTION].putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Message_Properties"));
          actions[PROPERTIES_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Show_properties_of_selected_message."));
        } else if (selectionObjType == MsgDataRecord.OBJ_TYPE_ADDR) {
          actions[PROPERTIES_ACTION].putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Address_Properties"));
          actions[PROPERTIES_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Show_properties_of_selected_address."));
        }
      }

      if (actions[REPLY_TO_SENDER__OR__COMPOSE_TO_ADDRESS_ACTION] != null) {
        if (selectionObjType == MsgDataRecord.OBJ_TYPE_MSG) {
          actions[REPLY_TO_SENDER__OR__COMPOSE_TO_ADDRESS_ACTION].putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Reply_to_Sender_..."));
          actions[REPLY_TO_SENDER__OR__COMPOSE_TO_ADDRESS_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Reply_to_Sender"));
          actions[REPLY_TO_SENDER__OR__COMPOSE_TO_ADDRESS_ACTION].putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Reply"));
        } else if (selectionObjType == MsgDataRecord.OBJ_TYPE_ADDR) {
          actions[REPLY_TO_SENDER__OR__COMPOSE_TO_ADDRESS_ACTION].putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Compose_to_Address(es)_..."));
          actions[REPLY_TO_SENDER__OR__COMPOSE_TO_ADDRESS_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_New_Message_to_the_selected_address(es)."));
          actions[REPLY_TO_SENDER__OR__COMPOSE_TO_ADDRESS_ACTION].putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Compose"));
        } else {
          actions[REPLY_TO_SENDER__OR__COMPOSE_TO_ADDRESS_ACTION].putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Compose_to_Address(es)_..."));
          actions[REPLY_TO_SENDER__OR__COMPOSE_TO_ADDRESS_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_New_Message_to_the_selected_address(es)."));
          actions[REPLY_TO_SENDER__OR__COMPOSE_TO_ADDRESS_ACTION].putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Compose"));
        }
      }

      if (actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION] != null) {
        if (selectionObjType == MsgDataRecord.OBJ_TYPE_MSG) {
          actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION].putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Trace_Message_Access"));
          actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Trace_Message_Access"));
        } else if (selectionObjType == MsgDataRecord.OBJ_TYPE_ADDR) {
          actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION].putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Trace_Address_Access"));
          actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Trace_Address_Access"));
        } else {
          actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION].putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Trace_Object_Access"));
          actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Trace_Object_Access"));
        }
      }

      if (actions[INVITE_SENDER_ACTION] != null) {
        if (selectionObjType == MsgDataRecord.OBJ_TYPE_MSG) {
          actions[INVITE_SENDER_ACTION].putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Invite_Sender"));
          actions[INVITE_SENDER_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Invite_Message_Sender_to_Contact_List"));
        } else if (selectionObjType == MsgDataRecord.OBJ_TYPE_ADDR) {
          actions[INVITE_SENDER_ACTION].putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Invite_by_Email"));
          actions[INVITE_SENDER_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Invite_by_Email"));
        } else {
          actions[INVITE_SENDER_ACTION].putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Invite"));
          actions[INVITE_SENDER_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Invite"));
        }
      }

      if (actions[POST_REPLY__OR__EDIT_ACTION] != null) {
        if (selectionObjType == MsgDataRecord.OBJ_TYPE_MSG) {
          actions[POST_REPLY__OR__EDIT_ACTION].putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Post_Reply_to_Folder_..."));
          actions[POST_REPLY__OR__EDIT_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Post_a_reply_for_the_selected_message_into_the_folder."));
          actions[POST_REPLY__OR__EDIT_ACTION].putValue(Actions.MENU_ICON, Images.get(ImageNums.REPLY_TO_MSG16));
          actions[POST_REPLY__OR__EDIT_ACTION].putValue(Actions.TOOL_ICON, Images.get(ImageNums.REPLY_TO_MSG24));
          actions[POST_REPLY__OR__EDIT_ACTION].putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Post_Reply"));
        } else if (selectionObjType == MsgDataRecord.OBJ_TYPE_ADDR) {
          actions[POST_REPLY__OR__EDIT_ACTION].putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Edit_..."));
          actions[POST_REPLY__OR__EDIT_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Edit_..."));
          actions[POST_REPLY__OR__EDIT_ACTION].putValue(Actions.MENU_ICON, Images.get(ImageNums.ADDRESS_EDIT16));
          actions[POST_REPLY__OR__EDIT_ACTION].putValue(Actions.TOOL_ICON, Images.get(ImageNums.ADDRESS_EDIT24));
          actions[POST_REPLY__OR__EDIT_ACTION].putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Edit"));
        }
      }

      if (actions[NEW_FROM_DRAFT_ACTION] != null) {
        if (selectionObjType == MsgDataRecord.OBJ_TYPE_MSG) {
          actions[NEW_FROM_DRAFT_ACTION].putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_New_Message_from_Draft_..."));
          actions[NEW_FROM_DRAFT_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_New_Message_from_selected_Draft_message."));
          actions[NEW_FROM_DRAFT_ACTION].putValue(Actions.MENU_ICON, Images.get(ImageNums.MAIL_COMPOSE_FROM_DRAFT16));
          actions[NEW_FROM_DRAFT_ACTION].putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_COMPOSE_FROM_DRAFT24));
        } else if (selectionObjType == MsgDataRecord.OBJ_TYPE_ADDR) {
          actions[NEW_FROM_DRAFT_ACTION].putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_New_Address_from_Draft_..."));
          actions[NEW_FROM_DRAFT_ACTION].putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_New_Address_from_selected_Draft_address."));
          actions[NEW_FROM_DRAFT_ACTION].putValue(Actions.MENU_ICON, Images.get(ImageNums.ADDRESS_ADD16));
          actions[NEW_FROM_DRAFT_ACTION].putValue(Actions.TOOL_ICON, Images.get(ImageNums.ADDRESS_ADD24));
        }
      }
    }
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "MsgActionTable";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
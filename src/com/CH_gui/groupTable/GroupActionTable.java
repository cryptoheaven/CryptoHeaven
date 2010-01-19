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

package com.CH_gui.groupTable;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;

import com.CH_gui.action.*;
import com.CH_gui.dialog.*;
import com.CH_gui.frame.*;
import com.CH_gui.msgs.*;
import com.CH_gui.table.*;
import com.CH_gui.tree.*;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.UserOps;
import com.CH_cl.service.records.filters.*;

import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

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
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class GroupActionTable extends RecordActionTable implements ActionProducerI {

  private Action[] actions;

  private static final int NUM_OF_SORT_COLUMNS = GroupTableModel.columnHeaderData.data[ColumnHeaderData.I_SHORT_DISPLAY_NAMES].length;

  private static final int INVITE_ACTION = 0;
  private static final int NEW_MESSAGE_TO_GROUP_ACTION = 1;
  private static final int NEW_MESSAGE_TO_MEMBER_ACTION = 2;
  private static final int SORT_ASC_ACTION = 3;
  private static final int SORT_DESC_ACTION = 4;
  private static final int SORT_BY_FIRST_COLUMN_ACTION = 5;
  private static final int CUSTOMIZE_COLUMNS_ACTION = SORT_BY_FIRST_COLUMN_ACTION + NUM_OF_SORT_COLUMNS;

  private static final int NUM_ACTIONS = CUSTOMIZE_COLUMNS_ACTION + 1;

  private int leadingActionId = Actions.LEADING_ACTION_ID_GROUP_ACTION_TABLE;
  private int leadingFolderActionId = Actions.LEADING_ACTION_ID_FOLDER_ACTION_TREE;

  private ServerInterfaceLayer serverInterfaceLayer;

  /** Creates new GroupActionTable, fetches the initial data from the database. */
  public GroupActionTable() {
    super(new GroupTableModel());
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GroupActionTable.class, "GroupActionTable()");

    initActions();
    this.serverInterfaceLayer = MainFrame.getServerInterfaceLayer();
    ((GroupTableModel) getTableModel()).setAutoUpdate(true);

    addDND(getJSortedTable());
    addDND(getViewport());

    if (trace != null) trace.exit(GroupActionTable.class);
  }

  public DragGestureListener createDragGestureListener() {
    return null;
  }
  public DropTargetListener createDropTargetListener() {
    return null;
  }


  private void initActions() {
    actions = new Action[NUM_ACTIONS];
    actions[INVITE_ACTION] = new InviteAction(leadingFolderActionId + FolderActionTree.INVITE_ACTION);
    actions[NEW_MESSAGE_TO_GROUP_ACTION] = new NewMessageToGroupAction(leadingActionId + NEW_MESSAGE_TO_GROUP_ACTION);
    actions[NEW_MESSAGE_TO_MEMBER_ACTION] = new NewMessageToMemberAction(leadingActionId + NEW_MESSAGE_TO_MEMBER_ACTION);
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


  /****************************************************************************/
  /*        A c t i o n P r o d u c e r I                                  
  /****************************************************************************/

  /** @return all the acitons that this objects produces.
   */
  public Action[] getActions() {
    return actions;
  }

  /**
   * Show selected Folder's sharing panel so user can add/invite others.
   */
  private class InviteAction extends AbstractActionTraced {
    public InviteAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Invite_to_the_Group_..."), Images.get(ImageNums.MEMBER_ADD16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Invite_to_the_Group_..."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.MEMBER_ADD24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Add_Member"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      FolderPair fPair = GroupActionTable.this.getTableModel().getParentFolderPair();
      if (fPair != null && fPair.getFolderRecord().isCategoryType()) {
        Window w = SwingUtilities.windowForComponent(GroupActionTable.this);
        String title = com.CH_gui.lang.Lang.rb.getString("title_Select_Folder_or_Group_to_Share");
        fPair = FolderActionTree.selectFolder(w, title, new MultiFilter(new RecordFilter[] { FolderFilter.MAIN_VIEW, FolderFilter.NON_LOCAL_FOLDERS }, MultiFilter.AND));
      }
      if (fPair != null) {
        Window w = SwingUtilities.windowForComponent(GroupActionTable.this);
        if (w instanceof Frame) new FolderPropertiesDialog((Frame) w, fPair, 1);
        else if (w instanceof Dialog) new FolderPropertiesDialog((Dialog) w, fPair, 1);
      }
    }
  }

  /** 
   * Create a new message to the entire Group
   */
  private class NewMessageToGroupAction extends AbstractActionTraced {
    public NewMessageToGroupAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_New_Message_To_Group"), Images.get(ImageNums.MAIL_COMPOSE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_New_Message_to_selected_Group."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_COMPOSE24));
      putValue(Actions.PARENT_NAME, com.CH_gui.lang.Lang.rb.getString("Message"));
      putValue(Actions.IN_MENU, Boolean.FALSE);
      putValue(Actions.IN_POPUP, Boolean.TRUE);
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      // new message trigger
      FolderPair pair = ((GroupTableModel) GroupActionTable.this.getTableModel()).getParentFolderPair();
      if (pair != null && pair.getFolderRecord().isCategoryType()) {
        pair = null;
      }
      if (pair != null) {
        new MessageFrame(pair);
      } else {
        new MessageFrame();
      }
    }
  }
  
  /** 
   * Create a new message to the selected Member(s)
   */
  private class NewMessageToMemberAction extends AbstractActionTraced {
    public NewMessageToMemberAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_New_Message_To_Member"), Images.get(ImageNums.MAIL_COMPOSE_TO_MEMBER16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_New_Message"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_COMPOSE_TO_MEMBER24));
      putValue(Actions.PARENT_NAME, com.CH_gui.lang.Lang.rb.getString("Message"));
      putValue(Actions.IN_MENU, Boolean.FALSE);
      putValue(Actions.IN_POPUP, Boolean.TRUE);
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      // new message trigger
      FolderShareRecord[] shares = (FolderShareRecord[]) getSelectedRecords();
      if (shares != null) {
        Vector recipientsV = new Vector();
        StringBuffer errorBuffer = new StringBuffer();
        for (int i=0; i<shares.length; i++) {
          FolderShareRecord shareRecord = shares[i];
          if (shareRecord.isOwnedByUser()) {
            UserRecord uRec = FetchedDataCache.getSingleInstance().getUserRecord(shareRecord.ownerUserId);
            Record rec = MsgPanelUtils.convertUserIdToFamiliarUser(uRec.userId, true, false);
            recipientsV.addElement(rec);
          } else if (shareRecord.isOwnedByGroup()) {
            FolderRecord gRec = FetchedDataCache.getSingleInstance().getFolderRecord(shareRecord.ownerUserId);
            if (gRec != null) {
              FolderShareRecord share = FetchedDataCache.getSingleInstance().getFolderShareRecordMy(gRec.folderId, true);
              FolderPair fPair = new FolderPair(share, gRec);
              recipientsV.addElement(fPair);
            } else {
              errorBuffer.append("You cannot send messages to Group (" + shareRecord.ownerUserId + ") because you are not a member.\n");
            }
          }
        }
        Component parent = GroupActionTable.this;
        if (recipientsV.size() > 0) {
          Record[] recipients = (Record[]) ArrayUtils.toArray(recipientsV, Record.class);
          parent = new MessageFrame(recipients);
        }
        if (errorBuffer.length() > 0) {
          MessageDialog.showWarningDialog(parent, errorBuffer.toString(), "Not a member", false);
        }
      }
    }
  }

  /**
   * Enables or Disables actions based on the current state of the Action Producing component.
   */
  public void setEnabledActions() {
    // Always enable Invite action
    actions[INVITE_ACTION].setEnabled(true);
    // Always enable New Message to Group action
    actions[NEW_MESSAGE_TO_GROUP_ACTION].setEnabled(true);
    // Always enable sort actions
    actions[SORT_ASC_ACTION].setEnabled(true);
    actions[SORT_DESC_ACTION].setEnabled(true);
    actions[CUSTOMIZE_COLUMNS_ACTION].setEnabled(true);
    for (int i=SORT_BY_FIRST_COLUMN_ACTION; i<SORT_BY_FIRST_COLUMN_ACTION+NUM_OF_SORT_COLUMNS; i++) {
      actions[i].setEnabled(true);
    }

    FolderShareRecord[] records = (FolderShareRecord[]) getSelectedRecords();
    Long[] userIDs = FolderShareRecord.getOwnerUserIDs(records);

    int count = 0;
    if (records != null) {
      count = records.length;
    }
    if (count == 0) {
      actions[NEW_MESSAGE_TO_MEMBER_ACTION].setEnabled(false);
    } else {
      actions[NEW_MESSAGE_TO_MEMBER_ACTION].setEnabled(true);
    }
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassGroupName = "GroupActionTable";
  public String getVisualsClassKeyName() {
    return visualsClassGroupName;
  }
}
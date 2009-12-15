/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_gui.userTable;

import com.CH_gui.action.*;
import com.CH_gui.contactTable.*;
import com.CH_gui.dialog.*;
import com.CH_gui.frame.*;
import com.CH_gui.table.*;

import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_co.service.records.*;
import com.CH_co.util.*;
import com.CH_co.trace.Trace;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: 1.24 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class UserActionTable extends RecordActionTable implements ActionProducerI {

  private static Integer versionedVisualsSavable = new Integer(1);

  private Action[] actions;

  private static final int NUM_OF_SORT_COLUMNS = UserTableModel.columnHeaderData.data[ColumnHeaderData.I_SHORT_DISPLAY_NAMES].length;

  public static final int INITIATE_ACTION = 0;
  private static final int MESSAGE_ACTION = 1;
  private static final int SORT_ASC_ACTION = 2;
  private static final int SORT_DESC_ACTION = 3;
  private static final int SORT_BY_FIRST_COLUMN_ACTION = 4;
  private static final int CUSTOMIZE_COLUMNS_ACTION = SORT_BY_FIRST_COLUMN_ACTION + NUM_OF_SORT_COLUMNS;

  private static final int NUM_ACTIONS = CUSTOMIZE_COLUMNS_ACTION + 1;

  private int leadingActionId = Actions.LEADING_ACTION_ID_USER_ACTION_TABLE;
  private ServerInterfaceLayer serverInterfaceLayer;


  /** Creates new UserActionTable */
  public UserActionTable() {
    this(new UserTableModel());
  }
  public UserActionTable(RecordTableModel model) {
    super(model);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserActionTable.class, "UserActionTable()");
    serverInterfaceLayer = MainFrame.getServerInterfaceLayer();
    initActions();
    addDND(getJSortedTable());
    addDND(getViewport());
    if (trace != null) trace.exit(UserActionTable.class);
  }


  public DragGestureListener createDragGestureListener() {
    return null;
  }
  public DropTargetListener createDropTargetListener() {
    return new PersonDND_DropTargetListener(this);
  }


  // =====================================================================
  // LISTENERS FOR THE MENU ITEMS        
  // =====================================================================

  /** 
   * Initiate a new contact.
   */
  private class InitiateAction extends AbstractActionTraced {
    public InitiateAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Add_to_Contact_List_..."), Images.get(ImageNums.CONTACT_ADD16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Add_User_to_your_Contact_List."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CONTACT_ADD24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Add_to_Contacts"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      UserRecord[] uRecs = (UserRecord[]) getSelectedRecords();
      if (uRecs != null && uRecs.length > 0) {
        Window w = SwingUtilities.windowForComponent(UserActionTable.this);
        if (w instanceof Frame)
          new InitiateContactDialog((Frame) w, RecordUtils.getIDs(uRecs));
      }
    }
  }

  /** 
   * Message a user.
   */
  private class SendMessageAction extends AbstractActionTraced {
    public SendMessageAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Send_Message_..."), Images.get(ImageNums.MAIL_COMPOSE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_New_Message_to_the_selected_user(s)."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_COMPOSE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Send_Message"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      new MessageFrame(getSelectedRecords());
    }
  }


  private void initActions() {
    actions = new Action[NUM_ACTIONS];
    actions[INITIATE_ACTION] = new InitiateAction(leadingActionId + INITIATE_ACTION);
    actions[MESSAGE_ACTION] = new SendMessageAction(leadingActionId + MESSAGE_ACTION);
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
  public Action getMsgAction() {
    return actions[MESSAGE_ACTION];
  }
  public Action[] getUserActions() {
    return new Action[] { actions[INITIATE_ACTION], actions[MESSAGE_ACTION] };
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
   * Enables or Disables actions based on the current state of the Action Producing component.
   */
  public void setEnabledActions() {

    // Always enable sort actions
    actions[SORT_ASC_ACTION].setEnabled(true);
    actions[SORT_DESC_ACTION].setEnabled(true);
    actions[CUSTOMIZE_COLUMNS_ACTION].setEnabled(true);
    for (int i=SORT_BY_FIRST_COLUMN_ACTION; i<SORT_BY_FIRST_COLUMN_ACTION+NUM_OF_SORT_COLUMNS; i++) 
      actions[i].setEnabled(true);

    int count = 0;
    boolean messageOk = true;
    boolean initiateOk = true;

    UserRecord[] selectedRecords = (UserRecord[]) getSelectedRecords();
    if (selectedRecords != null) {
      count = selectedRecords.length;

      FetchedDataCache cache = serverInterfaceLayer.getFetchedDataCache();
      Long userId = cache.getMyUserId();

      for (int i=0; i<selectedRecords.length; i++) {
        UserRecord uRec = selectedRecords[i];
        ContactRecord cRec = cache.getContactRecordOwnerWith(userId, uRec.userId);

        if (cRec != null)
          initiateOk = false;

        if (uRec.userId.equals(userId))
          initiateOk = false;

        // If user does not want spam...
        if (((uRec.acceptingSpam.shortValue() & UserRecord.ACC_SPAM_YES_INTER)) == 0) {
          // If we don't have an active contact, then we can't message
          if (cRec == null || !cRec.isOfActiveType())
            messageOk = false;
        }
      }
    }

    if (count == 0) {
      actions[INITIATE_ACTION].setEnabled(false);
      actions[MESSAGE_ACTION].setEnabled(false);
    } else if (count == 1) {
      actions[INITIATE_ACTION].setEnabled(initiateOk);
      actions[MESSAGE_ACTION].setEnabled(messageOk);
    } else {
      actions[INITIATE_ACTION].setEnabled(initiateOk);
      actions[MESSAGE_ACTION].setEnabled(messageOk);
    }
  }


  /****************************************************************************/
  /*        V i s u a l s S a v a b l e I                                  
  /****************************************************************************/
  public static final String visualsClassKeyName = "UserActionTable";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
  public Integer getVisualsVersion() {
    return versionedVisualsSavable;
  }

}
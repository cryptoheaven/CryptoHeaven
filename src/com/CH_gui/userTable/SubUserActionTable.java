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

package com.CH_gui.userTable;

import com.CH_gui.action.*;
import com.CH_gui.contactTable.*;
import com.CH_gui.dialog.*;
import com.CH_gui.frame.*;
import com.CH_gui.table.*;
import com.CH_gui.util.*;

import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.records.filters.*;

import com.CH_co.service.records.*;
import com.CH_co.util.*;
import com.CH_co.trace.Trace;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import javax.swing.*;

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
 * <b>$Revision: 1.19 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class SubUserActionTable extends RecordActionTable implements ActionProducerI {

  private static Integer versionedVisualsSavable = new Integer(1);

  private Action[] actions;

  private static final int NUM_OF_SORT_COLUMNS = UserTableModel.columnHeaderData_subAccounts.data[ColumnHeaderData.I_SHORT_DISPLAY_NAMES].length;

  private static final int ADD_ACCOUNT_ACTION = 0;
  private static final int EDIT_ACCOUNT_ACTION = 1;
  private static final int REMOVE_ACCOUNT_ACTION = 2;
  private static final int MESSAGE_ACTION = 3;
  private static final int REFRESH_ACTION = 4;
  private static final int OPEN_IN_SEPERATE_WINDOW_ACTION = 5;
  private static final int ADD_REMOVE_CONTACTS_ACTION = 6;
  private static final int ACTIVATE_SUSPEND_ACTION = 7;
  private static final int PASSWORD_RESET = 8;
  private static final int SORT_ASC_ACTION = 9;
  private static final int SORT_DESC_ACTION = 10;
  private static final int SORT_BY_FIRST_COLUMN_ACTION = 11;
  private static final int CUSTOMIZE_COLUMNS_ACTION = SORT_BY_FIRST_COLUMN_ACTION + NUM_OF_SORT_COLUMNS;

  private static final int NUM_ACTIONS = CUSTOMIZE_COLUMNS_ACTION + 1;

  private int leadingActionId = Actions.LEADING_ACTION_ID_SUB_USER_ACTION_TABLE;
  private ServerInterfaceLayer SIL;

  /** Creates new SubUserActionTable */
  public SubUserActionTable() {
    super(new UserTableModel(UserTableModel.columnHeaderData_subAccounts, new SubUserFilter(MainFrame.getServerInterfaceLayer().getFetchedDataCache().getMyUserId(), false, true)));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SubUserActionTable.class, "SubUserActionTable()");
    SIL = MainFrame.getServerInterfaceLayer();
    initActions();
    addDND(getJSortedTable());
    addDND(getViewport());
    if (trace != null) trace.exit(SubUserActionTable.class);
  }


  public DragGestureListener createDragGestureListener() {
    return null;
  }
  public DropTargetListener createDropTargetListener() {
    return new PersonDND_DropTargetListener(this);
  }


  private void initActions() {
    actions = new Action[NUM_ACTIONS];
    actions[ADD_ACCOUNT_ACTION] = new AddAccountAction(leadingActionId + ADD_ACCOUNT_ACTION);
    actions[EDIT_ACCOUNT_ACTION] = new EditAccountAction(leadingActionId + EDIT_ACCOUNT_ACTION);
    actions[REMOVE_ACCOUNT_ACTION] = new RemoveAccountAction(leadingActionId + REMOVE_ACCOUNT_ACTION);
    actions[MESSAGE_ACTION] = new SendMessageAction(leadingActionId + MESSAGE_ACTION);
    actions[REFRESH_ACTION] = new RefreshAction(leadingActionId + REFRESH_ACTION);
    actions[OPEN_IN_SEPERATE_WINDOW_ACTION] = new OpenInSeperateWindowAction(leadingActionId + OPEN_IN_SEPERATE_WINDOW_ACTION);
    actions[ADD_REMOVE_CONTACTS_ACTION] = new AddRemoveContactsAction(leadingActionId + ADD_REMOVE_CONTACTS_ACTION);
    actions[ACTIVATE_SUSPEND_ACTION] = new ActivateSuspendAction(leadingActionId + ACTIVATE_SUSPEND_ACTION);
    actions[PASSWORD_RESET] = new PasswordResetAction(leadingActionId + PASSWORD_RESET);
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
    return actions[EDIT_ACCOUNT_ACTION];
  }
  public Action getMsgAction() {
    return actions[MESSAGE_ACTION];
  }
  public Action[] getUserActions() {
    return new Action[] { actions[ADD_ACCOUNT_ACTION], actions[EDIT_ACCOUNT_ACTION] };
  }



  // =====================================================================
  // LISTENERS FOR THE MENU ITEMS
  // =====================================================================

   /**
    * Add a user.
    */
  private class AddAccountAction extends AbstractActionTraced {
    public AddAccountAction(int actionId) {
      super("Create New", Images.get(ImageNums.USER_NEW16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, "Create New Account");
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.USER_NEW24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Create"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      Window w = SwingUtilities.windowForComponent(SubUserActionTable.this);
      if (w instanceof Frame) new CreateSubAccountsWizardDialog((Frame) w);
      else if (w instanceof Dialog) new CreateSubAccountsWizardDialog((Dialog) w);
    }
  }

  /**
   * Edit a user.
   */
  private class EditAccountAction extends AbstractActionTraced {
    public EditAccountAction(int actionId) {
      super("Edit Account", Images.get(ImageNums.USER_EDIT16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, "Edit User Account");
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.USER_EDIT24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Edit"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      UserRecord[] selectedRecords = (UserRecord[]) getSelectedRecords();
      if (selectedRecords != null) {
        Window w = SwingUtilities.windowForComponent(SubUserActionTable.this);
        if (w instanceof Frame) new AccountOptionsDialog((Frame) w, selectedRecords);
        else if (w instanceof Dialog) new AccountOptionsDialog((Dialog) w, selectedRecords);
      }
    }
  }

  /**
   * Remove a user.
   */
  private class RemoveAccountAction extends AbstractActionTraced {
    public RemoveAccountAction(int actionId) {
      super("Delete Account", Images.get(ImageNums.USER_DELETE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, "Delete User Account");
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.USER_DELETE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Delete"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      UserRecord[] records = (UserRecord[]) getSelectedRecords();
      if (records != null && records.length > 0) {
        Window w = SwingUtilities.windowForComponent(SubUserActionTable.this);
        if (w instanceof Frame) new DeleteAccountDialog((Frame) w, false, RecordUtils.getIDs(records));
        if (w instanceof Dialog) new DeleteAccountDialog((Dialog) w, false, RecordUtils.getIDs(records));
      }
    }
    private void updateText(int countSelected) {
      if (countSelected > 1) {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Delete_Accounts_..."));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Permanently_delete_the_selected_user_accounts."));
      } else {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Delete_Account_..."));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Permanently_delete_the_selected_user_account."));
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
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_New_Message"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      new MessageFrame(getSelectedRecords());
    }
  }

  /**
   * Refresh Sub Account List.
   */
  private class RefreshAction extends AbstractActionTraced {
    public RefreshAction(int actionId) {
      super("Refresh Accounts", Images.get(ImageNums.REFRESH16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, "Refresh Account List from the server.");
      //putValue(Actions.TOOL_ICON, Images.get(ImageNums.REFRESH24));
      //putValue(Actions.TOOL_NAME, "Refresh");
    }
    public void actionPerformedTraced(ActionEvent event) {
      UserTableModel tableModel = (UserTableModel) getTableModel();
      tableModel.refreshData(true);
    }
  }

  /**
   * Open in seperate window
   */
  private class OpenInSeperateWindowAction extends AbstractActionTraced {
    public OpenInSeperateWindowAction(int actionId) {
      super("Clone Account List View", Images.get(ImageNums.CLONE_CONTACT16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, "Display account list table in its own window.");
      //putValue(Actions.TOOL_ICON, Images.get(ImageNums.CLONE_CONTACT24));
    }
    public void actionPerformedTraced(ActionEvent event) {
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      UserRecord myUserRec = cache.getUserRecord();
      if (myUserRec.isCapableToManageUserAccounts()) {
        new SubUserTableFrame();
      } else if (myUserRec.isPersonalAccount()) {
        MessageDialog.showWarningDialog(SubUserActionTable.this, "Only Business accounts can manage their user accounts.", "Account Incapable", false);
      } else {
        MessageDialog.showWarningDialog(SubUserActionTable.this, "Your account is not capable to manage other user accounts.", "Account Incapable", false);
      }
    }
  }

  /**
   * Add or remove contacts from sub-accounts
   */
  private class AddRemoveContactsAction extends AbstractActionTraced {
    public AddRemoveContactsAction(int actionId) {
      super("Manage Contacts", Images.get(ImageNums.CONTACT_ADD16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, "Manage user contacts.");
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CONTACT_ADD24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Manage_Contacts"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      UserRecord[] selectedRecords = (UserRecord[]) getSelectedRecords();
      if (selectedRecords == null || selectedRecords.length == 0) {
        FetchedDataCache cache = SIL.getFetchedDataCache();
        selectedRecords = new UserRecord[] { cache.getUserRecord() };
      }
      Window w = SwingUtilities.windowForComponent(SubUserActionTable.this);
      if (w instanceof Frame) new ManageContactsDialog((Frame) w, selectedRecords);
      if (w instanceof Dialog) new ManageContactsDialog((Dialog) w, selectedRecords);
    }
  }

  /**
   * Activate or Suspend selected sub-accounts
   */
  private class ActivateSuspendAction extends AbstractActionTraced {
    public ActivateSuspendAction(int actionId) {
      super("Activate or Suspend ...", Images.get(ImageNums.USER_ACTIVATE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, "Activate or Suspend user accounts.");
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.USER_ACTIVATE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Activate_or_Suspend"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      UserRecord[] records = (UserRecord[]) getSelectedRecords();
      if (records != null && records.length > 0) {
        Window w = SwingUtilities.windowForComponent(SubUserActionTable.this);
        if (w instanceof Frame) new ActivateSuspendDialog((Frame) w, RecordUtils.getIDs(records));
        if (w instanceof Dialog) new ActivateSuspendDialog((Dialog) w, RecordUtils.getIDs(records));
      }
    }
  }

  /**
   * Reset User's Password of selected sub-accounts
   */
  private class PasswordResetAction extends AbstractActionTraced {
    public PasswordResetAction(int actionId) {
      super("Password Reset ...", Images.get(ImageNums.USER_PASS_RESET16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, "Password Reset for user accounts.");
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.USER_PASS_RESET24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Password_Reset"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      UserRecord[] records = (UserRecord[]) getSelectedRecords();
      if (records != null && records.length > 0) {
        Window w = SwingUtilities.windowForComponent(SubUserActionTable.this);
        if (w instanceof Frame) new PasswordResetDialog((Frame) w, RecordUtils.getIDs(records));
        if (w instanceof Dialog) new PasswordResetDialog((Dialog) w, RecordUtils.getIDs(records));
      }
    }
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
    // Always enable Add Account action
    actions[ADD_ACCOUNT_ACTION].setEnabled(true);

    int count = 0;
    boolean messageOk = true;

    UserRecord[] selectedRecords = (UserRecord[]) getSelectedRecords();
    if (selectedRecords != null) {
      count = selectedRecords.length;

      FetchedDataCache cache = SIL.getFetchedDataCache();
      Long userId = cache.getMyUserId();

      for (int i=0; i<selectedRecords.length; i++) {
        UserRecord uRec = selectedRecords[i];
        ContactRecord cRec = cache.getContactRecordOwnerWith(userId, uRec.userId);

        // If user does not want spam...
        if (((uRec.acceptingSpam.shortValue() & UserRecord.ACC_SPAM_YES_INTER)) == 0) {
          // If we don't have an active contact, then we can't message
          if (cRec == null || !cRec.isOfActiveType())
            messageOk = false;
        }
      }
    }

    if (count == 0) {
      actions[EDIT_ACCOUNT_ACTION].setEnabled(false);
      actions[REMOVE_ACCOUNT_ACTION].setEnabled(false);
      actions[MESSAGE_ACTION].setEnabled(false);
      actions[ACTIVATE_SUSPEND_ACTION].setEnabled(false);
      actions[PASSWORD_RESET].setEnabled(false);
    } else {
      actions[EDIT_ACCOUNT_ACTION].setEnabled(true);
      actions[REMOVE_ACCOUNT_ACTION].setEnabled(true);
      actions[MESSAGE_ACTION].setEnabled(messageOk);
      actions[ACTIVATE_SUSPEND_ACTION].setEnabled(true);
      actions[PASSWORD_RESET].setEnabled(true);
    }

    RemoveAccountAction removeAction = (RemoveAccountAction) actions[REMOVE_ACCOUNT_ACTION];
    removeAction.updateText(count);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "SubUserActionTable";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
  public Integer getVisualsVersion() {
    return versionedVisualsSavable;
  }
}
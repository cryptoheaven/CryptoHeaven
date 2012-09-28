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

package com.CH_gui.contactTable;

import com.CH_cl.service.cache.CacheFldUtils;
import com.CH_cl.service.cache.CacheUsrUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.FolderOps;
import com.CH_cl.service.records.filters.ContactFilterCl;
import com.CH_cl.service.records.filters.FixedFilter;
import com.CH_cl.service.records.filters.FolderFilter;
import com.CH_cl.service.records.filters.InvEmlFilter;
import com.CH_co.nanoxml.XMLElement;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.service.msg.dataSets.obj.Obj_IDs_Co;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.MultiFilter;
import com.CH_co.service.records.filters.RecordFilter;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;
import com.CH_gui.action.AbstractActionTraced;
import com.CH_gui.action.Actions;
import com.CH_gui.actionGui.JActionButton;
import com.CH_gui.addressBook.ContactInfoPanel;
import com.CH_gui.addressBook.EmailPanel;
import com.CH_gui.addressBook.NamePanel;
import com.CH_gui.dialog.*;
import com.CH_gui.frame.*;
import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.MyInsets;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.msgs.MsgComposePanel;
import com.CH_gui.table.ColumnHeaderData;
import com.CH_gui.table.RecordActionTable;
import com.CH_gui.table.RecordTableModel;
import com.CH_gui.tree.FolderActionTree;
import com.CH_gui.tree.FolderTree;
import com.CH_gui.tree.FolderTreeComponent;
import com.CH_gui.tree.FolderTreeModelGui;
import com.CH_gui.util.*;
import java.awt.*;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

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
* <b>$Revision: 1.41 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class ContactActionTable extends RecordActionTable implements ActionProducerI {

  private Action[] actions;

  private static final int NUM_OF_SORT_COLUMNS = ContactTableModel.columnHeaderData.data[ColumnHeaderData.I_SHORT_DISPLAY_NAMES].length;

  private static final int NEW_CONTACT = 0;
  private static final int ACCEPT_DECLINE_ACTION = 1;
  private static final int REMOVE_ACTION = 2;
  private static final int MESSAGE_ACTION = 3;
  private static final int PROPERTIES_ACTION = 4;
  private static final int REFRESH_ACTION = 5;
  private static final int TOGGLE_INCOMING_ACTION = 6;
  private static final int OPEN_IN_SEPERATE_WINDOW_ACTION = 7;
  private static final int CHAT_ACTION = 8;
  private static final int SEND_EMAIL_INVITAION_ACTION = 9;
  private static final int CREATE_SHARED_SPACE_ACTION = 10;
  private static final int ADDRESS_ACTION = 11;
  private static final int NEW_GROUP = 12;
  private static final int SORT_ASC_ACTION = 13;
  private static final int SORT_DESC_ACTION = 14;
  private static final int SORT_BY_FIRST_COLUMN_ACTION = 15;
  private static final int CUSTOMIZE_COLUMNS_ACTION = SORT_BY_FIRST_COLUMN_ACTION + NUM_OF_SORT_COLUMNS;

  private static final int NUM_ACTIONS = CUSTOMIZE_COLUMNS_ACTION + 1;

  private int leadingActionId = Actions.LEADING_ACTION_ID_CONTACT_ACTION_TABLE;

  private DropTarget dropTarget1;
  private DropTarget dropTarget2;

  private boolean withDoubleClickAction;

  /** Creates new ContactActionTable */
  public ContactActionTable() {
    this(new ContactTableModel(), false, true);
  }
  /** Creates new ContactActionTable, sets filter with specified one. */
  public ContactActionTable(RecordFilter contactFilter, boolean withDoubleClickAction) {
    this(new ContactTableModel(contactFilter), false, withDoubleClickAction);
  }
  /** Creates new ContactActionTable, sets initial data with specified records and filters. */
  public ContactActionTable(Record[] initialData, RecordFilter contactFilter, boolean withDoubleClickAction) {
    this(new ContactTableModel(initialData, contactFilter), false, withDoubleClickAction);
  }

  private ContactActionTable(ContactTableModel contactTableModel, boolean fetchContacts, boolean withDoubleClickAction) {
    super(contactTableModel);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactActionTable.class, "ContactActionTable(ContactTableModel contactTableModel, boolean fetchContacts)");
    if (trace != null) trace.args(contactTableModel);
    if (trace != null) trace.args(fetchContacts);

    this.withDoubleClickAction = withDoubleClickAction;

    initActions();
    ((ContactTableModel) getTableModel()).setAutoUpdate(true);
    if (fetchContacts) {
      fetchContacts();
      reAddGroupsToCache();
    } else {
      contactTableModel.setData(FetchedDataCache.getSingleInstance().getContactRecords());
      contactTableModel.updateData(FetchedDataCache.getSingleInstance().getFolderPairsMyOfType(FolderRecord.GROUP_FOLDER, true));
      contactTableModel.updateData(FetchedDataCache.getSingleInstance().getInvEmlRecords());
    }

    addDND(getJSortedTable());
    addDND(getViewport());

    // skip lines in the contact list
    getJSortedTable().setShowHorizontalLines(false);

    if (trace != null) trace.exit(ContactActionTable.class);
  }


  public static String getTogglePropertyName(Window w) {
    if (w instanceof VisualsSavable)
      return ((VisualsSavable) w).getVisualsClassKeyName() + "_" + visualsClassKeyName + "_showIncoming";
    else
      return w.getClass().getName() + "_" + visualsClassKeyName + "_showIncoming";
  }


  public DragGestureListener createDragGestureListener() {
    return null;
  }
  public DropTargetListener createDropTargetListener() {
    return new PersonDND_DropTargetListener(this);
  }


  private void initActions() {
    actions = new Action[NUM_ACTIONS];
    actions[NEW_CONTACT] = new NewContactAction(leadingActionId + NEW_CONTACT);
    actions[ACCEPT_DECLINE_ACTION] = new AcceptDeclineAction(leadingActionId + ACCEPT_DECLINE_ACTION);
    actions[REMOVE_ACTION] = new RemoveAction(leadingActionId + REMOVE_ACTION);
    actions[MESSAGE_ACTION] = new NewMessageAction(leadingActionId + MESSAGE_ACTION);
    actions[PROPERTIES_ACTION] = new PropertiesAction(leadingActionId + PROPERTIES_ACTION);
    actions[REFRESH_ACTION] = new RefreshAction(leadingActionId + REFRESH_ACTION);
    actions[TOGGLE_INCOMING_ACTION] = new ToggleIncomingAction(leadingActionId + TOGGLE_INCOMING_ACTION);
    actions[OPEN_IN_SEPERATE_WINDOW_ACTION] = new OpenInSeperateWindowAction(leadingActionId + OPEN_IN_SEPERATE_WINDOW_ACTION);
    actions[CHAT_ACTION] = new ChatAction(leadingActionId + CHAT_ACTION);
    actions[SEND_EMAIL_INVITAION_ACTION] = new SendEmailInvitationAction(leadingActionId + SEND_EMAIL_INVITAION_ACTION);
    actions[CREATE_SHARED_SPACE_ACTION] = new CreateSharedSpaceAction(leadingActionId + CREATE_SHARED_SPACE_ACTION);
    actions[ADDRESS_ACTION] = new NewAddressAction(leadingActionId + ADDRESS_ACTION);
    actions[NEW_GROUP] = new NewGroupAction(leadingActionId + NEW_GROUP);
    {
      ButtonGroup sortAscDescGroup = new ButtonGroup();
      actions[SORT_ASC_ACTION] = new SortAscDescAction(true, leadingActionId + SORT_ASC_ACTION, sortAscDescGroup);
      actions[SORT_DESC_ACTION] = new SortAscDescAction(false, leadingActionId + SORT_DESC_ACTION, sortAscDescGroup);
      actions[CUSTOMIZE_COLUMNS_ACTION] = new CustomizeColumnsAction(leadingActionId + CUSTOMIZE_COLUMNS_ACTION, com.CH_cl.lang.Lang.rb.getString("action_Contact_Columns_..."));
      ButtonGroup columnSortGroup = new ButtonGroup();
      for (int i=0; i<NUM_OF_SORT_COLUMNS; i++) {
        actions[SORT_BY_FIRST_COLUMN_ACTION+i] = new SortByColumnAction(i, leadingActionId+SORT_BY_FIRST_COLUMN_ACTION+i, columnSortGroup);
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
  public Action getMsgAction() {
    return actions[MESSAGE_ACTION];
  }
  public Action getDoubleClickAction() {
    Action action = null;
    if (withDoubleClickAction)
      action = actions[MESSAGE_ACTION];
    return action;
  }


  /**
  * @return all selected records, if there are none selected, return null
  * Runtime instance of the returned array is of MemberContactRecordI[].
  */
  public MemberContactRecordI[] getSelectedMemberContacts() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactActionTable.class, "getSelectedMemberContacts()");
    List selectedRecsL = getSelectedRecordsL();
    List selectedMembersL = new ArrayList();
    if (selectedRecsL != null) {
      for (int i=0; i<selectedRecsL.size(); i++) {
        Record rec = (Record) selectedRecsL.get(i);
        if (rec instanceof MemberContactRecordI)
          selectedMembersL.add(rec);
      }
    }
    MemberContactRecordI[] records = (MemberContactRecordI[]) ArrayUtils.toArray(selectedMembersL, MemberContactRecordI.class);
    if (trace != null) trace.exit(ContactActionTable.class, records);
    return records;
  }

  /**
  * @return all selected records, if there are none selected, return null
  * Runtime instance of the returned array is of Record[].
  */
  public Record[] getSelectedRecords() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactActionTable.class, "getSelectedRecords()");
    Record[] records = (Record[]) ArrayUtils.toArray(getSelectedRecordsL(), Record.class);
    if (trace != null) trace.exit(ContactActionTable.class, records);
    return records;
  }

  /**
  * @return all selected Records of specified class type (ContactRecord or FolderPair), or null if none
  */
  public Record[] getSelectedInstancesOf(Class classType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactActionTable.class, "getSelectedInstancesOf(Class classType)");
    if (trace != null) trace.args(classType);

    Record[] records = null;
    List recordsL = getSelectedRecordsL();
    if (recordsL != null && recordsL.size() > 0) {
      ArrayList selectedL = new ArrayList();
      for (int i=0; i<recordsL.size(); i++) {
        if (recordsL.get(i).getClass().equals(classType)) {
          selectedL.add(recordsL.get(i));
        }
      }
      if (selectedL.size() > 0) {
        records = (Record[]) Array.newInstance(classType, selectedL.size());
        selectedL.toArray(records);
      }
    }

    if (trace != null) trace.exit(ContactActionTable.class, records);
    return records;
  }


  // =====================================================================
  // LISTENERS FOR THE MENU ITEMS
  // =====================================================================

  /**
  * Initiate a new contact.
  */
  private static class NewContactAction extends AbstractActionTraced {
    public NewContactAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Find_Friends_and_Associates_..."), Images.get(ImageNums.USER_FIND16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Find_Users_on_the_System_and_Invite_them_to_your_Contact_List."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.USER_FIND24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Find"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      new FindUserFrame();
    }
  }

  /**
  * Create a new group.
  */
  private class NewGroupAction extends AbstractActionTraced {
    public NewGroupAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Create_Group"), Images.get(ImageNums.GROUP_ADD16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("action_Create_Group"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.GROUP_ADD24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Create_Group"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      chatOrShareSpace(false, FolderRecord.GROUP_FOLDER, event);
    }
  }

  /**
  * Accept contact(s).
  */
  private class AcceptDeclineAction extends AbstractActionTraced {
    public AcceptDeclineAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Accept_/_Decline_Contact(s)_..."), Images.get(ImageNums.CONTACT_CHECK16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Accept_or_Decline_selected_contact(s)"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CONTACT_CHECK24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Accept"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      ContactRecord[] contacts = (ContactRecord[]) getSelectedInstancesOf(ContactRecord.class);
      if (contacts != null && contacts.length > 0) {
        for (int i=0; i<contacts.length; i++) {
          new AcceptDeclineContactDialog(GeneralDialog.getDefaultParent(), contacts[i]);
        }
      }
    }
  }

  /**
  * Ignore contact request and Remove the contact object.
  */
  private class RemoveAction extends AbstractActionTraced {
    public RemoveAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Delete_Contact_..."), Images.get(ImageNums.CONTACT_DELETE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Permanently_delete_the_selected_contact."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CONTACT_DELETE24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Delete"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      String messageText = com.CH_cl.lang.Lang.rb.getString("msg_Are_you_sure_you_want_to_delete_selected_contact(s)?");
      String title = com.CH_cl.lang.Lang.rb.getString("msgTitle_Delete_Confirmation");
      boolean option = MessageDialog.showDialogYesNo(ContactActionTable.this, messageText, title);
      if (option == true) {
        ContactRecord[] contacts = (ContactRecord[]) getSelectedInstancesOf(ContactRecord.class);
        if (contacts != null && contacts.length > 0) {
          Obj_IDs_Co request = new Obj_IDs_Co();
          request.IDs = new Long[2][];
          request.IDs[0] = RecordUtils.getIDs(contacts);
          FetchedDataCache cache = ServerInterfaceLayer.getFetchedDataCache();
          request.IDs[1] = new Long[] { cache.getFolderShareRecordMy(cache.getUserRecord().contactFolderId, false).shareId };
          MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.CNT_Q_REMOVE_CONTACTS, request));
        }
        FolderPair[] groups = (FolderPair[]) getSelectedInstancesOf(FolderPair.class);
        if (groups != null && groups.length > 0) {
          new FolderActionTree.DeleteRunner(ContactActionTable.this, groups, false).start();
        }
        InvEmlRecord[] invites = (InvEmlRecord[]) getSelectedInstancesOf(InvEmlRecord.class);
        if (invites != null && invites.length > 0) {
          Obj_IDList_Co request = new Obj_IDList_Co();
          request.IDs = RecordUtils.getIDs(invites);
          // hide the removed invites
          for (int i=0; i<invites.length; i++)
            invites[i].removed = Boolean.TRUE;
          // add updated records to the cache so listeners can update
          FetchedDataCache cache = ServerInterfaceLayer.getFetchedDataCache();
          cache.addInvEmlRecords(invites);
          MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.INV_Q_REMOVE, request));
        }
      }
    }
    private void updateText(int countSelectedContacts) {
      if (countSelectedContacts > 1) {
        putValue(Actions.NAME, com.CH_cl.lang.Lang.rb.getString("action_Delete_Contacts_..."));
        putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Permanently_delete_the_selected_contacts."));
      } else {
        putValue(Actions.NAME, com.CH_cl.lang.Lang.rb.getString("action_Delete_Contact_..."));
        putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Permanently_delete_the_selected_contact."));
      }
    }
  }


  /**
  * Send a new message to contact(s).
  */
  private class NewMessageAction extends AbstractActionTraced {
    public NewMessageAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_New_Message"), Images.get(ImageNums.MAIL_COMPOSE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_New_Message"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_COMPOSE24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_New_Message"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      // Determine if action from menu, popup menu or shortcut
      // If action is from popup menu, prefill the To: field in the new message
      boolean useSelected = isActionActivatedFromPopup(event);
      Record[] selectedRecords = null;
      if (useSelected) selectedRecords = getSelectedRecords();
      //Record[] initialRecipients = MsgPanelUtils.getOrFetchFamiliarUsers(selectedRecords);
      Record[] initialRecipients = selectedRecords;
      if (initialRecipients != null && initialRecipients.length > 0) {
        new MessageFrame(initialRecipients);
      } else {
        new MessageFrame();
      }
    }
  }


  /**
  * Send a new message to contact(s).
  */
  private class NewAddressAction extends AbstractActionTraced {
    public NewAddressAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Add_to_Address_Book"), Images.get(ImageNums.ADDRESS_ADD16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Create_a_New_Address_Book_entry."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.ADDRESS_ADD24));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      Object sourceObj = event.getSource();
      // Do not prefill the from field when user presses tool bar action button.
      if (sourceObj instanceof JActionButton) {
        new MessageFrame();
      } else {
        // Determine if action from menu, popup menu or shortcut
        // If action is from popup menu, prefill the To: field in the new message
        boolean fromPopup = isActionActivatedFromPopup(event);

        ArrayList cRecsL = new ArrayList();
        ArrayList uIDsL = new ArrayList();

        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        Long userId = cache.getMyUserId();

        if (fromPopup) {
          ContactRecord[] records = (ContactRecord[]) getSelectedInstancesOf(ContactRecord.class);
          if (records != null && records.length > 0) {
            for (int i=0; i<records.length; i++) {
              ContactRecord cRec = records[i];
              Long otherUserId = !userId.equals(cRec.contactWithId) ? cRec.contactWithId : cRec.ownerUserId;
              if (!uIDsL.contains(otherUserId)) {
                uIDsL.add(otherUserId);
                cRecsL.add(cRec);
              }
            }
          }
        }

        FolderPair addrBook = FolderOps.getOrCreateAddressBook(MainFrame.getServerInterfaceLayer());
        if (addrBook != null) {
          FolderPair[] fPairs = new FolderPair[] { addrBook };
          if (cRecsL.size() > 0) {
            ArrayList emailNicksL = new ArrayList();
            ArrayList emailStringRecordsL = new ArrayList();
            for (int i=0; i<cRecsL.size(); i++) {
              boolean batched = false;
              ContactRecord cRec = (ContactRecord) cRecsL.get(i);
              Long otherUID = (Long) uIDsL.get(i);
              String emailAddress = "";
              String fullName = userId.equals(cRec.ownerUserId) ? cRec.getOwnerNote() : cRec.getOtherNote();
              UserRecord uRec = cache.getUserRecord(otherUID);
              if (uRec != null) {
                String[] emailStrings = CacheUsrUtils.getCachedDefaultEmail(uRec, false);
                emailAddress = emailStrings != null ? emailStrings[2] : null;
                if (emailAddress != null && emailAddress.length() > 0) {
                  emailNicksL.add(fullName);
                  emailStringRecordsL.add(emailAddress);
                  batched = true;
                }
              }
              if (!batched) {
                XMLElement draftData = ContactInfoPanel.getContent(new XMLElement[] {
                                                NamePanel.getContent(fullName, null, null, null),
                                                EmailPanel.getContent(EmailPanel.getTypes(), new String[] { emailAddress }, null, 0) });
                new AddressFrame(fullName, fPairs, draftData);
              }
            }
            if (emailStringRecordsL.size() > 0)
              MsgComposePanel.checkEmailAddressesForAddressBookAdition_Threaded(ContactActionTable.this, emailNicksL, emailStringRecordsL, true, new FolderFilter(FolderRecord.ADDRESS_FOLDER));
          } else {
            new AddressFrame(fPairs);
          }
        }
      }
    }
  }


  /**
  * Show Contact Properties Dialog.
  */
  private class PropertiesAction extends AbstractActionTraced {
    public PropertiesAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Contact_Properties"));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Show_Properties_of_the_selected_Contact."));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      Record record = getSelectedRecord();
      if (record instanceof ContactRecord) {
        Window w = SwingUtilities.windowForComponent(ContactActionTable.this);
        if (w instanceof Frame) new ContactPropertiesDialog((Frame) w, (ContactRecord) record);
        else if (w instanceof Dialog) new ContactPropertiesDialog((Dialog) w, (ContactRecord) record);
      } else if (record instanceof FolderPair) {
        Window w = SwingUtilities.windowForComponent(ContactActionTable.this);
        if (w instanceof Frame) new FolderPropertiesDialog((Frame) w, (FolderPair) record);
        else if (w instanceof Dialog) new FolderPropertiesDialog((Dialog) w, (FolderPair) record);
      }
    }
  }


  /**
  * Refresh Contact List.
  */
  private class RefreshAction extends AbstractActionTraced {
    public RefreshAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Refresh_Contacts"), Images.get(ImageNums.REFRESH16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Refresh_Contact_List_from_the_server."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.REFRESH24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Refresh"));
      putValue(Actions.IN_MENU, Boolean.FALSE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      ContactTableModel tableModel = (ContactTableModel) getTableModel();
      tableModel.removeData();
      FetchedDataCache.getSingleInstance().clearContactRecords();
      fetchContacts();
      reAddGroupsToCache();
    }
  }

  /**
  * Show/Hide incoming Contacts
  */
  private class ToggleIncomingAction extends AbstractActionTraced {
    private Boolean lastSetShow = null;
    public ToggleIncomingAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Show_Other's_Contacts"), Images.get(ImageNums.ARROW_DOUBLE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Show_other's_contacts_with_you."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.ARROW_DOUBLE24));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      boolean newShow = !(isShowing().booleanValue());
      setShowModel(newShow);
      setShowGUI(newShow);
    }
    private Boolean isShowing() {
      Window w = SwingUtilities.windowForComponent(ContactActionTable.this);
      Boolean showing = null;
      if (w != null) {
        String propertyName = getTogglePropertyName(w);
        String showingS = GlobalProperties.getProperty(propertyName);
        showing = showingS == null ? Boolean.FALSE : Boolean.valueOf(showingS);
      }
      return showing;
    }
    private void setShowGUI(boolean isShow) {
      if (lastSetShow == null || lastSetShow.booleanValue() != isShow) {
        lastSetShow = Boolean.valueOf(isShow);
        if (isShow) {
          putValue(Actions.NAME, com.CH_cl.lang.Lang.rb.getString("action_Hide_Other's_Contacts"));
          putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Hide_other's_contacts_with_you."));
          putValue(Actions.MENU_ICON, Images.get(ImageNums.ARROW_LEFT16));
          putValue(Actions.TOOL_ICON, Images.get(ImageNums.ARROW_LEFT24));

          // Add "Direction" column to viewable columns
          RecordTableModel recordTableModel = getTableModel();
          ColumnHeaderData data = recordTableModel.getColumnHeaderData();
          recordTableModel.updateHeaderDataFrom(getJSortedTable());
          Integer[] viewSeq = data.getRawColumnViewableSequence();
          ArrayList viewSeqL = new ArrayList(Arrays.asList(viewSeq));
          if (viewSeqL.contains(new Integer(0))) {
            // its already viewable - noop
          } else {
            viewSeqL.add(0, new Integer(0));
            Integer[] viewSeqNew = (Integer[]) ArrayUtils.toArray(viewSeqL, Integer.class);
            data.data[ColumnHeaderData.I_VIEWABLE_SEQUENCE] = viewSeqNew;
            getTableModel().updateHeaderDataFromTo(null, getJSortedTable());
          }
        } else {
          putValue(Actions.NAME, com.CH_cl.lang.Lang.rb.getString("action_Show_Other's_Contacts"));
          putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Show_other's_contacts_with_you."));
          putValue(Actions.MENU_ICON, Images.get(ImageNums.ARROW_DOUBLE16));
          putValue(Actions.TOOL_ICON, Images.get(ImageNums.ARROW_DOUBLE24));

          // Remove "Direction" column from viewable columns
          RecordTableModel recordTableModel = getTableModel();
          ColumnHeaderData data = recordTableModel.getColumnHeaderData();
          recordTableModel.updateHeaderDataFrom(getJSortedTable());
          Integer[] viewSeq = data.getRawColumnViewableSequence();
          ArrayList viewSeqL = new ArrayList(Arrays.asList(viewSeq));
          if (viewSeqL.contains(new Integer(0))) {
            viewSeqL.remove(new Integer(0));
            Integer[] viewSeqNew = (Integer[]) ArrayUtils.toArray(viewSeqL, Integer.class);
            data.data[ColumnHeaderData.I_VIEWABLE_SEQUENCE] = viewSeqNew;
            getTableModel().updateHeaderDataFromTo(null, getJSortedTable());
          } else {
            // its already not viewable - noop
          }
        }
      }
    }
    private void setShowModel(boolean isShow) {
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      //UserRecord myUserRec = cache.getUserRecord();
      ContactTableModel tableModel = (ContactTableModel) ContactActionTable.this.getTableModel();
      RecordFilter filter = new MultiFilter(new RecordFilter[] {
        //new ContactFilterCl(myUserRec != null ? myUserRec.contactFolderId : null, isShow),
        new ContactFilterCl(isShow),
        new FolderFilter(FolderRecord.GROUP_FOLDER),
        new InvEmlFilter(true, false) }
      , MultiFilter.OR);
      tableModel.setFilter(filter);
      tableModel.setData(cache.getContactRecords());
      tableModel.updateData(cache.getFolderPairsMyOfType(FolderRecord.GROUP_FOLDER, true));
      tableModel.updateData(cache.getInvEmlRecords());
      String propertyName = getTogglePropertyName(SwingUtilities.windowForComponent(ContactActionTable.this));
      GlobalProperties.setProperty(propertyName, Boolean.valueOf(isShow).toString());
    }
  } // end private class ToggleIncomingAction


  /**
  * Open in separate window
  */
  private static class OpenInSeperateWindowAction extends AbstractActionTraced {
    public OpenInSeperateWindowAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Clone_Contact_List_View"), Images.get(ImageNums.CLONE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Display_contact_list_table_in_its_own_window."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CLONE24));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
      putValue(Actions.IN_MENU, Boolean.FALSE);
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      new ContactTableFrame();
    }
  }


  private class ChatAction extends AbstractActionTraced {
    public ChatAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Chat"), Images.get(ImageNums.CHAT16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Enter_chat_mode_with_selected_party."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CHAT24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Chat"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      chatOrShareSpace(true, (short) 0, event);
    } // end actionPerformed
  } // end class ChatAction


  /**
  * Send Email Message to invite someone to join.
  */
  private class SendEmailInvitationAction extends AbstractActionTraced {
    public SendEmailInvitationAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Invite_Friends_and_Associates_..."), Images.get(ImageNums.MAIL_COMPOSE_TO_MEMBER16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_New_Email_Message_to_invite_others_to_join_the_service."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_COMPOSE_TO_MEMBER24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Invite"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      InvEmlRecord[] invEmls = (InvEmlRecord[]) getSelectedInstancesOf(InvEmlRecord.class);
      sendEmailInvitationAction(invEmls);
    }
  }

  private void sendEmailInvitationAction(Record[] initialEmailRecs) {
    String initialEmails = getEmailAddrs(initialEmailRecs);
    Window w = SwingUtilities.windowForComponent(ContactActionTable.this);
    if (w == null) w = MainFrame.getSingleInstance();
    if (w instanceof Dialog) new InviteByEmailDialog((Dialog) w, initialEmails);
    else if (w instanceof Frame) new InviteByEmailDialog((Frame) w, initialEmails);
  }

  private String getEmailAddrs(Record[] invEmlRecs) {
    StringBuffer invEmailsSB = new StringBuffer();
    ArrayList emailsLowerL = new ArrayList();
    ArrayList emailsOrigL = new ArrayList();
    InvEmlRecord[] invEmls = (InvEmlRecord[]) ArrayUtils.gatherAllOfType(invEmlRecs, InvEmlRecord.class);
    if (invEmls != null && invEmls.length > 0) {
      for (int i=0; i<invEmls.length; i++) {
        String addr = invEmls[i].emailAddr;
        if (!emailsLowerL.contains(addr.toLowerCase())) {
          emailsLowerL.add(addr.toLowerCase());
          emailsOrigL.add(addr);
        }
      }
      for (int i=0; i<emailsOrigL.size(); i++) {
        if (invEmailsSB.length() > 0)
          invEmailsSB.append(", ");
        invEmailsSB.append(emailsOrigL.get(i).toString());
      }
    }
    return invEmailsSB.toString();
  }

  /** Display a dialog for creation of new folder shared between selected contacts.
    * Submit Create New Folder request
    */
  private class CreateSharedSpaceAction extends AbstractActionTraced {
    public CreateSharedSpaceAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Create_Shared_Space_..."), Images.get(ImageNums.SHARE_ADD16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Create_New_Shared_Space"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.SHARE_ADD24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Create_Shared_Space"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      chatOrShareSpace(false, (short) 0, event);
    }
  }


  private void chatOrShareSpace(final boolean isChat, final short folderType, ActionEvent event) {
    final boolean useSelected = isActionActivatedFromPopup(event);
    MemberContactRecordI[] selectedRecords = null;
    InvEmlRecord[] selectedInvEmls = null;
    if (useSelected) {
      selectedRecords = ContactActionTable.this.getSelectedMemberContacts();
      selectedInvEmls = (InvEmlRecord[]) getSelectedInstancesOf(InvEmlRecord.class);
    }
    if (selectedInvEmls != null && selectedInvEmls.length > 0) {
      JPanel panel = new JPanel();
      panel.setLayout(new GridBagLayout());

      int posY = 0;
      String msgText1 = "The following users have not yet created their accounts:";
      panel.add(new JMyLabel(msgText1), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(10, 10, 10, 10), 0, 0));
      posY ++;
      JPanel emlListPanel = new JPanel();
      emlListPanel.setLayout(new GridBagLayout());
      for (int i=0; i<selectedInvEmls.length; i++) {
        Icon icon = ListRenderer.getRenderedIcon(selectedInvEmls[i]);
        String label = ListRenderer.getRenderedText(selectedInvEmls[i], false, false, true);
        emlListPanel.add(new JMyLabel(label, icon, JLabel.LEADING), new GridBagConstraints(0, i, 2, 1, 10, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 10, 2, 10), 0, 0));
      }
      emlListPanel.add(new JLabel(), new GridBagConstraints(0, selectedInvEmls.length, 2, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
      JComponent listPane = emlListPanel;
      if (selectedInvEmls.length >= 3) {
        JScrollPane sc = new JScrollPane(emlListPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sc.getVerticalScrollBar().setUnitIncrement(5);
        listPane = sc;
      }
      panel.add(listPane, new GridBagConstraints(0, posY, 2, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(10, 10, 10, 10), 0, 0));
      posY ++;

      panel.add(new JMyLabel("Would you like to invite them now?"), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(10, 10, 10, 10), 0, 0));
      posY ++;

      final Record[] _initialEmailRecs = selectedInvEmls;
      ActionListener yesAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          sendEmailInvitationAction(_initialEmailRecs);
        }
      };
      final MemberContactRecordI[] _selectedRecords = selectedRecords;
      ActionListener noAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          chatOrShareSpace(ContactActionTable.this, _selectedRecords, useSelected, isChat, folderType);
        }
      };
      MessageDialog.showDialogYesNo(this, panel, "User account required.", NotificationCenter.QUESTION_MESSAGE, true, yesAction, noAction);
    } else {
      chatOrShareSpace(ContactActionTable.this, selectedRecords, useSelected, isChat, folderType);
    }
  }

  public static void chatOrShareSpace(final Component parent, MemberContactRecordI[] selectedRecords, boolean useSelected, final boolean isChat, final short folderType) {
    CallbackI selectionCallback = new CallbackI() {
      public void callback(Object value) {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "callback(Object value)");
        if (trace != null) trace.args(value);

        MemberContactRecordI[] selectedRecords = (MemberContactRecordI[]) value;
        if (selectedRecords != null && selectedRecords.length > 0) {
          boolean createSharedSpaceOk = true;
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          Long userId = cache.getMyUserId();
          for (int i=0; i<selectedRecords.length; i++) {
            if (selectedRecords[i].getMemberType() == Record.RECORD_TYPE_CONTACT) {
              ContactRecord cRec = (ContactRecord) selectedRecords[i];
              if (!cRec.ownerUserId.equals(userId) || !cRec.isOfActiveType() || (cRec.permits.intValue() & ContactRecord.PERMIT_DISABLE_SHARE_FOLDERS) != 0) {
                createSharedSpaceOk = false;
              }
            }
          }
          if (isChat) {
            // Gather possible chat folder matches in a broad search
            ArrayList suitableChatsL = new ArrayList();
            FolderPair perfectMatch = null;

            ArrayList userIDsL = new ArrayList();
            // add self to list of required members
            userIDsL.add(cache.getMyUserId());
            ArrayList groupIDsL = new ArrayList();
            ArrayList targetUIDsL = new ArrayList();
            for (int i=0; i<selectedRecords.length; i++) {
              MemberContactRecordI member = selectedRecords[i];
              short memberType = member.getMemberType();
              Long memberId = member.getMemberId();
              if (memberType == Record.RECORD_TYPE_GROUP)
                groupIDsL.add(member.getMemberId());
              else if (memberType == Record.RECORD_TYPE_USER)
                userIDsL.add(memberId);
              else {
                // not a Contact and not a Group
                if (trace != null) trace.data(10, "Not a contact and Not a group!");
              }
            }
            targetUIDsL.addAll(userIDsL);
            for (int i=0; i<groupIDsL.size(); i++)
              CacheFldUtils.getKnownGroupMembers((Long) groupIDsL.get(i), targetUIDsL);
            Collections.sort(targetUIDsL);
            if (trace != null) trace.data(20, "target UIDs are: ", Misc.objToStr(targetUIDsL));


            if (trace != null) trace.data(30, "userIDsL=", Misc.objToStr(userIDsL));
            if (trace != null) trace.data(31, "groupIDsL=", Misc.objToStr(groupIDsL));

            // Gather all chat folders to see if we have a single exact match or other possible sessions involving selected members...
            ArrayList chatUserIDsL = new ArrayList();
            ArrayList lvl1userIDsL = new ArrayList();
            ArrayList lvl1groupIDsL = new ArrayList();
            FolderRecord[] chats = cache.getFolderRecordsChatting();
            if (chats != null) {
              for (int i=0; i<chats.length; i++) {
                FolderRecord chat = chats[i];
                if (trace != null) trace.data(40, ListRenderer.getRenderedText(chat));
                FolderShareRecord[] shares = cache.getFolderShareRecordsForFolder(chat.folderId);
                // find all users of each chat
                chatUserIDsL.clear();
                lvl1userIDsL.clear();
                lvl1groupIDsL.clear();
                if (shares != null) {
                  for (int j=0; j<shares.length; j++) {
                    FolderShareRecord share = shares[j];
                    if (share.isOwnedByUser()) {
                      lvl1userIDsL.add(share.ownerUserId);
                      if (!chatUserIDsL.contains(share.ownerUserId))
                        chatUserIDsL.add(share.ownerUserId);
                      if (trace != null) trace.data(41, "u"+share.ownerUserId+"("+cache.getUserRecord(share.ownerUserId) != null ? cache.getUserRecord(share.ownerUserId).handle : "null" +")");
                    } else if (share.isOwnedByGroup()) {
                      lvl1groupIDsL.add(share.ownerUserId);
                      if (trace != null) trace.data(42, "g"+share.ownerUserId+"("+ListRenderer.getRenderedText(cache.getFolderRecord(share.ownerUserId))+")");
                      CacheFldUtils.getKnownGroupMembers(share.ownerUserId, chatUserIDsL);
                    }
                  }
                }
                Collections.sort(chatUserIDsL);
                if (trace != null) trace.data(50, " found UIDs are: ", Misc.objToStr(chatUserIDsL));
                if (chatUserIDsL.containsAll(targetUIDsL)) {
                  if (trace != null) trace.data(60, "lvl1userIDsL=", Misc.objToStr(lvl1userIDsL));
                  if (trace != null) trace.data(61, "lvl1groupIDsL=", Misc.objToStr(lvl1groupIDsL));
                  FolderPair suitableChat = CacheFldUtils.convertRecordToPair(chat);
                  if (perfectMatch == null 
                          && userIDsL.containsAll(lvl1userIDsL) && lvl1userIDsL.containsAll(userIDsL) 
                          && groupIDsL.containsAll(lvl1groupIDsL) && lvl1groupIDsL.containsAll(groupIDsL)) 
                  {
                    if (trace != null) trace.data(70, " found PERFECT MATCH is ", ListRenderer.getRenderedText(suitableChat));
                    perfectMatch = suitableChat;
                  } else {
                    if (trace != null) trace.data(71, " found SUITABLE FOLDER is ", ListRenderer.getRenderedText(suitableChat));
                    suitableChatsL.add(suitableChat);
                  }
                }
              }
            }

            if (suitableChatsL.size() > 0) {
              Window w = SwingUtilities.windowForComponent(parent);
              if (w == null || !(w instanceof Frame))
                w = MainFrame.getSingleInstance();
              new ChatSessionChooserDialog((Frame) w, selectedRecords, perfectMatch, suitableChatsL);
            } else {
              doChat(selectedRecords);
            }
          } else if (createSharedSpaceOk) {
            doSharedSpace(parent, selectedRecords, folderType);
          } else {
            MessageDialog.showInfoDialog(parent, com.CH_cl.lang.Lang.rb.getString("msg_Cannot_create_shared_space"), com.CH_cl.lang.Lang.rb.getString("msgTitle_No_folder_sharing_permission."), false);
          }
        }
        if (trace != null) trace.exit(getClass());
      }
    };
    if (!useSelected || selectedRecords == null || selectedRecords.length == 0) {
      Window w = SwingUtilities.windowForComponent(parent);
      if (w == null) w = MainFrame.getSingleInstance();
      if (w instanceof Dialog)
        new ContactSelectDialog((Dialog) w, true).setSelectionCallback(selectionCallback);
      else if (w instanceof Frame)
        new ContactSelectDialog((Frame) w, true).setSelectionCallback(selectionCallback);
    } else {
      selectionCallback.callback(selectedRecords);
    }
  }

  private static void doChat(MemberContactRecordI[] contacts) {
    new ChatSessionCreator(contacts).start();
  }
  private static void doSharedSpace(Component parent, MemberContactRecordI[] contacts, short folderType) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();

    FolderTreeModelGui treeModel = null;
    FolderPair selectedFolderPair = null;

    MainFrame mainFrame = MainFrame.getSingleInstance();
    if (mainFrame != null) {
      FolderTreeComponent mainTreeComp = mainFrame.getMainTreeComponent(parent);
      if (mainTreeComp != null) {
        FolderTree fTree = mainTreeComp.getFolderTreeScrollPane().getFolderTree();
        treeModel = fTree.getFolderTreeModel();
        selectedFolderPair = fTree.getLastSelectedPair();
      }
    }
    if (treeModel == null) {
      treeModel = new FolderTreeModelGui();
      treeModel.addNodes(cache.getFolderPairs(new FixedFilter(true), true));
    }
    Window w = SwingUtilities.windowForComponent(parent);
    if (w == null) w = MainFrame.getSingleInstance();
    String title = folderType == FolderRecord.GROUP_FOLDER ? com.CH_cl.lang.Lang.rb.getString("title_Create_New_Shared_Group") : com.CH_cl.lang.Lang.rb.getString("title_Create_New_Shared_Space");
    if (w instanceof Frame) new Move_NewFld_Dialog((Frame) w, treeModel, selectedFolderPair, title, true, folderType, cache, contacts);
    else if (w instanceof Dialog) new Move_NewFld_Dialog((Dialog) w, treeModel, selectedFolderPair, title, true, folderType, cache, contacts);
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
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactActionTable.class, "setEnabledActions()");

    if (!MainFrame.isLoggedIn()) {
      ActionUtils.setEnabledActions(actions, false);
    } else {
      // Some actions are always enabled
      actions[CHAT_ACTION].setEnabled(true);
      actions[CREATE_SHARED_SPACE_ACTION].setEnabled(true);
      actions[NEW_GROUP].setEnabled(true);
      actions[NEW_CONTACT].setEnabled(true);
      actions[SEND_EMAIL_INVITAION_ACTION].setEnabled(true);
      // Always enable sort actions
      actions[SORT_ASC_ACTION].setEnabled(true);
      actions[SORT_DESC_ACTION].setEnabled(true);
      actions[CUSTOMIZE_COLUMNS_ACTION].setEnabled(true);
      for (int i=SORT_BY_FIRST_COLUMN_ACTION; i<SORT_BY_FIRST_COLUMN_ACTION+NUM_OF_SORT_COLUMNS; i++)
        actions[i].setEnabled(true);

      // initiate always enabled
      // actions[FIND_USERS_ACTION].setEnabled(true);
      ToggleIncomingAction toggleAction = (ToggleIncomingAction) actions[TOGGLE_INCOMING_ACTION];
      Boolean isShowing = toggleAction.isShowing();
      if (isShowing != null)
        toggleAction.setShowGUI(isShowing.booleanValue());

      int count = 0;
      boolean messageOk = true;
      boolean addressOk = true;
      boolean acceptDeclineOk = true;
      boolean propsOk = true;

      Record[] records = getSelectedRecords();

      if (records != null) {
        count = records.length;
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        Long userId = cache.getMyUserId();

        for (int i=0; i<records.length; i++) {
          if (records[i] instanceof ContactRecord) {
            ContactRecord cRec = (ContactRecord) records[i];
            short status = cRec.status.shortValue();
            if (!cRec.ownerUserId.equals(userId) || !cRec.isOfActiveType()) {
              //messageOk = false;
              //createSharedSpaceOk = false;
            }
            if (cRec.ownerUserId.equals(userId) && ContactRecord.isOnlineStatus(status)) {
              //chatAnyUserOnline = true;
            }
            if (cRec.ownerUserId.equals(userId) || !cRec.isOfInitiatedType()) {
              acceptDeclineOk = false;
            }
          } else {
            addressOk = false;
            acceptDeclineOk = false;
          }
          if (records[i] instanceof InvEmlRecord) {
            propsOk = false;
          }
        }
      }

      if (count == 0) {
        actions[ACCEPT_DECLINE_ACTION].setEnabled(false);
        actions[REMOVE_ACTION].setEnabled(false);
        actions[MESSAGE_ACTION].setEnabled(true);  // when nothing is selected, set null initial recipients
        actions[ADDRESS_ACTION].setEnabled(true);  // when nothing is selected, set null initial data
        actions[PROPERTIES_ACTION].setEnabled(false);
      } else if (count == 1) {
        actions[ACCEPT_DECLINE_ACTION].setEnabled(acceptDeclineOk);
        actions[REMOVE_ACTION].setEnabled(true);
        actions[MESSAGE_ACTION].setEnabled(messageOk);
        actions[ADDRESS_ACTION].setEnabled(addressOk);
        actions[PROPERTIES_ACTION].setEnabled(propsOk);
      } else {
        actions[ACCEPT_DECLINE_ACTION].setEnabled(false);
        actions[REMOVE_ACTION].setEnabled(true);
        actions[MESSAGE_ACTION].setEnabled(messageOk);
        actions[ADDRESS_ACTION].setEnabled(addressOk);
        actions[PROPERTIES_ACTION].setEnabled(false);
      }

      Window w = SwingUtilities.windowForComponent(this);
      actions[REFRESH_ACTION].setEnabled(w != null);
      actions[TOGGLE_INCOMING_ACTION].setEnabled(w != null);
      actions[OPEN_IN_SEPERATE_WINDOW_ACTION].setEnabled(w != null);

      RemoveAction removeAction = (RemoveAction) actions[REMOVE_ACTION];
      removeAction.updateText(count);
    }

    if (trace != null) trace.exit(ContactActionTable.class);
  }


  /**
  * Send a request to fetch contacts for the current user from the server
  * if contacts were not fetched for yet, otherwise get them from cache
  */
  private void fetchContacts() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactActionTable.class, "fetchContacts()");

    ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
    if (SIL != null) {
      MessageAction msgAction = new MessageAction(CommandCodes.CNT_Q_GET_CONTACTS);
      SIL.submitAndReturn(msgAction, 30000);
    }

    if (trace != null) trace.exit(ContactActionTable.class);
  }

  /**
  * Re-Add my groups to the cache so that listeners can grab them
  */
  private void reAddGroupsToCache() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactActionTable.class, "reAddGroupsToCache()");

    ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
    if (SIL != null) {
      FetchedDataCache cache = ServerInterfaceLayer.getFetchedDataCache();
      FolderPair[] myGroups = cache.getFolderPairsMyOfType(FolderRecord.GROUP_FOLDER, true);
      FolderRecord[] myGroupFolders = FolderPair.getFolderRecords(myGroups);
      cache.addFolderRecords(myGroupFolders);
    }

    if (trace != null) trace.exit(ContactActionTable.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "ContactActionTable";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
  public Integer getVisualsVersion() {
    return new Integer(1);
  }

}
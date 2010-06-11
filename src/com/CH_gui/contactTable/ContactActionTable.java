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

package com.CH_gui.contactTable;

import com.CH_gui.action.*;
import com.CH_gui.actionGui.*;
import com.CH_gui.addressBook.*;
import com.CH_gui.dialog.*;
import com.CH_gui.frame.*;
import com.CH_gui.gui.*;
import com.CH_gui.msgs.*;
import com.CH_gui.table.*;
import com.CH_gui.tree.*;
import com.CH_gui.util.*;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.ops.*;
import com.CH_cl.service.records.filters.*;

import com.CH_co.nanoxml.*;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.msg.*;
import com.CH_co.util.*;
import com.CH_co.trace.Trace;
import com.CH_gui.list.ListRenderer;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.dnd.*;
import java.awt.event.*;
import java.lang.reflect.Array;
import java.util.*;
import javax.swing.*;

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
      actions[CUSTOMIZE_COLUMNS_ACTION] = new CustomizeColumnsAction(leadingActionId + CUSTOMIZE_COLUMNS_ACTION, com.CH_gui.lang.Lang.rb.getString("action_Contact_Columns_..."));
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
    return withDoubleClickAction ? actions[MESSAGE_ACTION] : null;
  }


  /**
   * @return all selected records, if there are none selected, return null
   * Runtime instance of the returned array is of MemberContactRecordI[].
   */
  public MemberContactRecordI[] getSelectedMemberContacts() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactActionTable.class, "getSelectedMemberContacts()");
    List selectedRecsL = getSelectedRecordsL();
    List selectedMembersL = new ArrayList();
    for (int i=0; i<selectedRecsL.size(); i++) {
      Record rec = (Record) selectedRecsL.get(i);
      if (rec instanceof MemberContactRecordI)
        selectedMembersL.add(rec);
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
      Vector selectedV = new Vector();
      for (int i=0; i<recordsL.size(); i++) {
        if (recordsL.get(i).getClass().equals(classType)) {
          selectedV.addElement(recordsL.get(i));
        }
      }
      if (selectedV.size() > 0) {
        records = (Record[]) Array.newInstance(classType, selectedV.size());
        selectedV.toArray(records);
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
      super(com.CH_gui.lang.Lang.rb.getString("action_Find_Friends_and_Associates_..."), Images.get(ImageNums.USER_FIND16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Find_Users_on_the_System_and_Invite_them_to_your_Contact_List."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.USER_FIND24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Find"));
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
      super(com.CH_gui.lang.Lang.rb.getString("action_Create_Group"), Images.get(ImageNums.GROUP_ADD16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Create_Group"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.GROUP_ADD24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Create_Group"));
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
      super(com.CH_gui.lang.Lang.rb.getString("action_Accept_/_Decline_Contact(s)_..."), Images.get(ImageNums.CONTACT_CHECK16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Accept_or_Decline_selected_contact(s)"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CONTACT_CHECK24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Accept"));
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
      super(com.CH_gui.lang.Lang.rb.getString("action_Delete_Contact_..."), Images.get(ImageNums.CONTACT_DELETE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Permanently_delete_the_selected_contact."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CONTACT_DELETE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Delete"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      String messageText = com.CH_gui.lang.Lang.rb.getString("msg_Are_you_sure_you_want_to_delete_selected_contact(s)?");
      String title = com.CH_gui.lang.Lang.rb.getString("msgTitle_Delete_Confirmation");
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
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Delete_Contacts_..."));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Permanently_delete_the_selected_contacts."));
      } else {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Delete_Contact_..."));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Permanently_delete_the_selected_contact."));
      }
    }
  }


  /**
   * Send a new message to contact(s).
   */
  private class NewMessageAction extends AbstractActionTraced {
    public NewMessageAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_New_Message"), Images.get(ImageNums.MAIL_COMPOSE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_New_Message"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_COMPOSE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_New_Message"));
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
      super(com.CH_gui.lang.Lang.rb.getString("action_Add_to_Address_Book"), Images.get(ImageNums.ADDRESS_ADD16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Create_a_New_Address_Book_entry."));
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

        Vector cRecsV = new Vector();
        Vector uIDsV = new Vector();

        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        Long userId = cache.getMyUserId();

        if (fromPopup) {
          ContactRecord[] records = (ContactRecord[]) getSelectedInstancesOf(ContactRecord.class);
          if (records != null && records.length > 0) {
            for (int i=0; i<records.length; i++) {
              ContactRecord cRec = records[i];
              Long otherUserId = !userId.equals(cRec.contactWithId) ? cRec.contactWithId : cRec.ownerUserId;
              if (!uIDsV.contains(otherUserId)) {
                uIDsV.addElement(otherUserId);
                cRecsV.addElement(cRec);
              }
            }
          }
        }

        FolderPair addrBook = FolderOps.getOrCreateAddressBook(MainFrame.getServerInterfaceLayer());
        if (addrBook != null) {
          FolderPair[] fPairs = new FolderPair[] { addrBook };
          if (cRecsV.size() > 0) {
            Vector emailNicksV = new Vector();
            Vector emailStringRecordsV = new Vector();
            for (int i=0; i<cRecsV.size(); i++) {
              boolean batched = false;
              ContactRecord cRec = (ContactRecord) cRecsV.elementAt(i);
              Long otherUID = (Long) uIDsV.elementAt(i);
              String emailAddress = "";
              String fullName = userId.equals(cRec.ownerUserId) ? cRec.getOwnerNote() : cRec.getOtherNote();
              UserRecord uRec = cache.getUserRecord(otherUID);
              if (uRec != null) {
                String[] emailStrings = UserOps.getCachedDefaultEmail(uRec, false);
                emailAddress = emailStrings != null ? emailStrings[2] : null;
                if (emailAddress != null && emailAddress.length() > 0) {
                  emailNicksV.addElement(fullName);
                  emailStringRecordsV.addElement(emailAddress);
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
            if (emailStringRecordsV.size() > 0)
              MsgComposePanel.checkEmailAddressesForAddressBookAdition_Threaded(ContactActionTable.this, emailNicksV, emailStringRecordsV, true, new FolderFilter(FolderRecord.ADDRESS_FOLDER));
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
      super(com.CH_gui.lang.Lang.rb.getString("action_Contact_Properties"));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Show_Properties_of_the_selected_Contact."));
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
      super(com.CH_gui.lang.Lang.rb.getString("action_Refresh_Contacts"), Images.get(ImageNums.REFRESH16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Refresh_Contact_List_from_the_server."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.REFRESH24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Refresh"));
      putValue(Actions.IN_POPUP, Boolean.FALSE);
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
      super(com.CH_gui.lang.Lang.rb.getString("action_Show_Other's_Contacts"), Images.get(ImageNums.ARROW_DOUBLE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Show_other's_contacts_with_you."));
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
          putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Hide_Other's_Contacts"));
          putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Hide_other's_contacts_with_you."));
          putValue(Actions.MENU_ICON, Images.get(ImageNums.ARROW_LEFT16));
          putValue(Actions.TOOL_ICON, Images.get(ImageNums.ARROW_LEFT24));

          // Add "Direction" column to viewable columns
          RecordTableModel recordTableModel = getTableModel();
          ColumnHeaderData data = recordTableModel.getColumnHeaderData();
          recordTableModel.updateHeaderDataFrom(getJSortedTable());
          Integer[] viewSeq = data.getRawColumnViewableSequence();
          Vector viewSeqV = new Vector(Arrays.asList(viewSeq));
          if (viewSeqV.contains(new Integer(0))) {
            // its already viewable - noop
          } else {
            viewSeqV.insertElementAt(new Integer(0), 0);
            Integer[] viewSeqNew = (Integer[]) ArrayUtils.toArray(viewSeqV, Integer.class);
            data.data[ColumnHeaderData.I_VIEWABLE_SEQUENCE] = viewSeqNew;
            getTableModel().updateHeaderDataFromTo(null, getJSortedTable());
          }
        } else {
          putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Show_Other's_Contacts"));
          putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Show_other's_contacts_with_you."));
          putValue(Actions.MENU_ICON, Images.get(ImageNums.ARROW_DOUBLE16));
          putValue(Actions.TOOL_ICON, Images.get(ImageNums.ARROW_DOUBLE24));

          // Remove "Direction" column from viewable columns
          RecordTableModel recordTableModel = getTableModel();
          ColumnHeaderData data = recordTableModel.getColumnHeaderData();
          recordTableModel.updateHeaderDataFrom(getJSortedTable());
          Integer[] viewSeq = data.getRawColumnViewableSequence();
          Vector viewSeqV = new Vector(Arrays.asList(viewSeq));
          if (viewSeqV.contains(new Integer(0))) {
            viewSeqV.remove(new Integer(0));
            Integer[] viewSeqNew = (Integer[]) ArrayUtils.toArray(viewSeqV, Integer.class);
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
   * Open in seperate window
   */
  private static class OpenInSeperateWindowAction extends AbstractActionTraced {
    public OpenInSeperateWindowAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Clone_Contact_List_View"), Images.get(ImageNums.CLONE_CONTACT16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Display_contact_list_table_in_its_own_window."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CLONE_CONTACT24));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      new ContactTableFrame();
    }
  }


  private class ChatAction extends AbstractActionTraced {
    public ChatAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Chat"), Images.get(ImageNums.CHAT16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Enter_chat_mode_with_selected_party."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CHAT24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Chat"));
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
      super(com.CH_gui.lang.Lang.rb.getString("action_Invite_Friends_and_Associates_..."), Images.get(ImageNums.PEOPLE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_New_Email_Message_to_invite_others_to_join_the_service."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.PEOPLE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Invite"));
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
    Vector emailsLowerV = new Vector();
    Vector emailsOrigV = new Vector();
    InvEmlRecord[] invEmls = (InvEmlRecord[]) ArrayUtils.gatherAllOfType(invEmlRecs, InvEmlRecord.class);
    if (invEmls != null && invEmls.length > 0) {
      for (int i=0; i<invEmls.length; i++) {
        String addr = invEmls[i].emailAddr;
        if (!emailsLowerV.contains(addr.toLowerCase())) {
          emailsLowerV.addElement(addr.toLowerCase());
          emailsOrigV.addElement(addr);
        }
      }
      for (int i=0; i<emailsOrigV.size(); i++) {
        if (invEmailsSB.length() > 0)
          invEmailsSB.append(", ");
        invEmailsSB.append(emailsOrigV.elementAt(i).toString());
      }
    }
    return invEmailsSB.toString();
  }

  /** Display a dialog for creation of new folder shared between selected contacts.
    * Submit Create New Folder request
    */
  private class CreateSharedSpaceAction extends AbstractActionTraced {
    public CreateSharedSpaceAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Create_Shared_Space_..."), Images.get(ImageNums.FOLDER_NEW_SHARED16, true));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Create_New_Shared_Space"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FOLDER_NEW_SHARED24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Create_Shared_Space"));
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

  public static void chatOrShareSpace(Component parent, MemberContactRecordI[] selectedRecords, boolean useSelected, boolean isChat, short folderType) {
    if (!useSelected || selectedRecords == null || selectedRecords.length == 0) {
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      ContactRecord[] contacts = cache.getContactRecordsMyActive();
      if (contacts != null && contacts.length > 0) {
        Window w = SwingUtilities.windowForComponent(parent);
        if (w == null) w = MainFrame.getSingleInstance();
        if (w instanceof Dialog)
          selectedRecords = new ContactSelectDialog((Dialog) w, true).getSelectedMemberContacts();
        else if (w instanceof Frame)
          selectedRecords = new ContactSelectDialog((Frame) w, true).getSelectedMemberContacts();
      } else {
        String title = com.CH_gui.lang.Lang.rb.getString("msgTitle_Confirmation");
        String messageText = null;
        if (isChat) {
          messageText = com.CH_gui.lang.Lang.rb.getString("msg_Chat_session_requires_at_least_one_active_contact._Would_you_like_to_invite_your_Friends_and_Associates?");
        } else {
          messageText = com.CH_gui.lang.Lang.rb.getString("msg_Shared_Space_requires_at_least_one_active_contact._Would_you_like_to_invite_your_Friends_and_Associates?");
        }
        boolean option = MessageDialog.showDialogYesNo(parent, messageText, title);
        if (option == true) {
          new FindUserFrame();
        }
      }
    }
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
        doChat(selectedRecords);
      } else if (createSharedSpaceOk) {
        doSharedSpace(parent, selectedRecords, folderType);
      } else {
        MessageDialog.showInfoDialog(parent, com.CH_gui.lang.Lang.rb.getString("msg_Cannot_create_shared_space"), com.CH_gui.lang.Lang.rb.getString("msgTitle_No_folder_sharing_permission."), false);
      }
    }
  }

  private static void doChat(MemberContactRecordI[] contacts) {
    new ChatSessionCreator(contacts).start();
  }
  private static void doSharedSpace(Component parent, MemberContactRecordI[] contacts, short folderType) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();

    FolderTreeModelCl treeModel = null;
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
      treeModel = new FolderTreeModelCl();
      treeModel.addNodes(cache.getFolderPairs(new FixedFilter(true), true));
    }
    Window w = SwingUtilities.windowForComponent(parent);
    if (w == null) w = MainFrame.getSingleInstance();
    String title = folderType == FolderRecord.GROUP_FOLDER ? com.CH_gui.lang.Lang.rb.getString("title_Create_New_Shared_Group") : com.CH_gui.lang.Lang.rb.getString("title_Create_New_Shared_Space");
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
            if (cRec.ownerUserId.equals(userId) || status != ContactRecord.STATUS_INITIATED) {
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
  public void fetchContacts() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactActionTable.class, "fetchContacts()");

    ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
    if (SIL != null) {
      MessageAction msgAction = new MessageAction(CommandCodes.CNT_Q_GET_CONTACTS);
      SIL.submitAndReturn(msgAction);
    }

    if (trace != null) trace.exit(ContactActionTable.class);
  }

  /**
   * Re-Add my groups to the cache so that listeners can grab them
   */
  public void reAddGroupsToCache() {
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
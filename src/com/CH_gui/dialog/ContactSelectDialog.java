/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.dialog;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.records.filters.FixedFilter;
import com.CH_cl.service.records.filters.FolderFilter;
import com.CH_cl.service.records.filters.InverseFilter;
import com.CH_cl.service.records.filters.RecordIdFilter;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.ContactFilterCo;
import com.CH_co.service.records.filters.MultiFilter;
import com.CH_co.service.records.filters.RecordFilter;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import com.CH_co.util.CallbackI;
import com.CH_gui.contactTable.ContactActionTable;
import com.CH_gui.contactTable.ContactTableComponent;
import com.CH_gui.gui.JMyButton;
import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.MyInsets;
import com.CH_gui.gui.Template;
import com.CH_gui.table.RecordTableComponent;
import com.CH_gui.util.GeneralDialog;
import com.CH_gui.util.VisualsSavable;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.23 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class ContactSelectDialog extends GeneralDialog implements VisualsSavable {

  public static final int DEFAULT_OK_INDEX = 0;
  public static final int DEFAULT_CANCEL_INDEX = 1;

  private JButton jOk;

  private Integer resultButton;
  private MemberContactRecordI[] selectedMemberContacts;
  private ContactRecord[] selectedContacts;
  private FolderPair[] selectedGroups;
  private JLabel contactTableLabel;
  private RecordTableComponent contactTable;

  // Main panel where all dialog GUI is located
  private JPanel panel;

  private RecordSelectionListener recordSelectionListener ;

  private CallbackI selectionCallback;

  /** Creates new ContactSelectDialog */
  public ContactSelectDialog(Dialog owner, boolean includeGroups, FolderPair excludePair) {
    this(owner, includeGroups, excludePair, true);
  }
  public ContactSelectDialog(Frame owner, boolean includeGroups, FolderPair excludePair) {
    this(owner, includeGroups, excludePair, true);
  }
  /** Creates new ContactSelectDialog */
  public ContactSelectDialog(Dialog owner, boolean includeGroups, FolderPair excludePair, boolean show) {
    super(owner, com.CH_cl.lang.Lang.rb.getString("title_Select_Contacts"));
    initialize(owner, includeGroups, excludePair, show);
  }
  public ContactSelectDialog(Frame owner, boolean includeGroups, FolderPair excludePair, boolean show) {
    super(owner, com.CH_cl.lang.Lang.rb.getString("title_Select_Contacts"));
    initialize(owner, includeGroups, excludePair, show);
  }
  private void initialize(Component owner, boolean includeGroups, FolderPair excludePair, boolean show) {
    JButton[] buttons = createButtons();
    JPanel panel = createMainPanel(includeGroups, excludePair);
    init(owner, buttons, panel, DEFAULT_OK_INDEX, DEFAULT_CANCEL_INDEX, show);
    setEnabledButtons();
  }


  private JButton[] createButtons() {
    JButton[] buttons = new JButton[2];

    buttons[0] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Select"));
    buttons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedOK();
      }
    });
    jOk = buttons[0];
    jOk.setEnabled(false);

    buttons[1] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Cancel"));
    buttons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedCancel();
      }
    });

    return buttons;
  }

  private JPanel createMainPanel(boolean includeGroups, FolderPair excludePair) {
    panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    contactTableLabel = new JMyLabel("Select from your Contact List below:");

    int posY = 10;
    panel.add(contactTableLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 2, 5), 0, 0));
    posY ++;
    panel.add(new JMyLabel("To add to the list, type in an email address"), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));
    posY ++;
    panel.add(new JMyLabel("or the member's name in the space below."), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));
    posY ++;

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    ContactRecord[] contacts = cache.getContactRecordsMyActive(true);
    FolderPair[] myGroups = null;
    if (includeGroups) {
      myGroups = cache.getFolderPairsMyOfType(FolderRecord.GROUP_FOLDER, true);
      // exclude specific pair, to prevent users to adding its own group share to the list of additional shares....
      // ie: Group1 should not be added to Group1 list of members to avoid 2nd share creation of same Group membership
      if (excludePair != null)
        myGroups = (FolderPair[]) ArrayUtils.removeElements(myGroups, excludePair);
    }
    Record[] initialRecords = RecordUtils.concatinate(contacts, myGroups);
    UserRecord myUserRec = cache.getUserRecord();
    RecordFilter folderFilter = null;
    if (includeGroups)
      folderFilter = new FolderFilter(FolderRecord.GROUP_FOLDER);
    else
      folderFilter = new FixedFilter(false);
    RecordFilter filter = new MultiFilter(new RecordFilter[] {
      new ContactFilterCo(myUserRec != null ? myUserRec.contactFolderId : null,
                          new Short[] { new Short(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED), new Short(ContactRecord.STATUS_INITIATED) },
                          true, myUserRec != null ? myUserRec.userId : null),
      folderFilter }
    , MultiFilter.OR);
    if (excludePair != null) {
      // additionally modify the live update filter to keep out the excludePair
      RecordFilter excludeFilter = new InverseFilter(new RecordIdFilter(excludePair.getId()));
      filter = new MultiFilter(new RecordFilter[] { filter, excludeFilter }, MultiFilter.AND);
    }
    ContactTableComponent contactTableComponent = new ContactTableComponent(initialRecords, filter, Template.get(Template.NONE), Template.get(Template.BACK_CONTACTS), false, true, true);
    contactTableComponent.addTopContactBuildingPanel();
    contactTableComponent.setAutoCreateWebAccounts(true);
    contactTable = contactTableComponent;
    recordSelectionListener = new RecordSelectionListener();
    contactTable.getActionTable().getJSortedTable().getSelectionModel().addListSelectionListener(recordSelectionListener);
    panel.add(contactTable, new GridBagConstraints(0, posY, 1, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(2, 5, 5, 5), 0, 0));

    return panel;
  }

  /**
   * Add a GUI header above the table component label
   */
  public void addHeader(JComponent header, int posY, Insets insets) {
    if (posY >= 10)
      throw new IllegalArgumentException("posY too large");
    panel.add(header, new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
  }

  public JButton getOkButton() {
    return jOk;
  }

  public RecordTableComponent getTable() {
    return contactTable;
  }

  public JLabel getTableLabel() {
    return contactTableLabel;
  }

  private void setEnabledButtons() {
    Record[] recs = contactTable.getActionTable().getSelectedRecords();
    jOk.setEnabled(recs != null && recs.length > 0);
  }

  public void setSelectionCallback(CallbackI callback) {
    selectionCallback = callback;
  }

  private void pressedOK() {
    resultButton = new Integer(DEFAULT_OK_INDEX);
    Record[] selected = contactTable.getActionTable().getSelectedRecords();
    int size = selected != null ? selected.length : 0;
    selectedMemberContacts = new MemberContactRecordI[size];
    if (size > 0) {
      System.arraycopy(selected, 0, selectedMemberContacts, 0, size);
    }
    selectedContacts = (ContactRecord[]) ((ContactActionTable) contactTable.getActionTable()).getSelectedInstancesOf(ContactRecord.class);
    selectedGroups = (FolderPair[]) ((ContactActionTable) contactTable.getActionTable()).getSelectedInstancesOf(FolderPair.class);
    if (selectionCallback != null) {
      selectionCallback.callback(selectedMemberContacts);
    }
    closeDialog();
  }

  private void pressedCancel() {
    resultButton = new Integer(DEFAULT_CANCEL_INDEX);
    closeDialog();
  }

  public Integer getResultButton() {
    return resultButton;
  }

  public ContactRecord[] getSelectedContacts() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactSelectDialog.class, "getSelectedContacts()");
    if (trace != null) trace.exit(ContactSelectDialog.class, selectedContacts);
    return selectedContacts;
  }

  public FolderPair[] getSelectedGroups() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactSelectDialog.class, "getSelectedGroups()");
    if (trace != null) trace.exit(ContactSelectDialog.class, selectedGroups);
    return selectedGroups;
  }

  public MemberContactRecordI[] getSelectedMemberContacts() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactSelectDialog.class, "getSelectedMemberContacts()");
    if (trace != null) trace.exit(ContactSelectDialog.class, selectedMemberContacts);
    return selectedMemberContacts;
  }

  public void closeDialog() {
    if (recordSelectionListener != null) {
      contactTable.getActionTable().getJSortedTable().getSelectionModel().removeListSelectionListener(recordSelectionListener);
      recordSelectionListener = null;
    }
    contactTable.disposeObj();
    super.closeDialog();
  }

  private class RecordSelectionListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent event) {
      setEnabledButtons();
    }
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "ContactSelectDialog";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
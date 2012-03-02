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

package com.CH_gui.folder;

import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.cryptx.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.dialog.*;
import com.CH_gui.frame.*;
import com.CH_gui.gui.*;
import com.CH_gui.list.*;
import com.CH_gui.shareTable.*;
import com.CH_gui.table.*;
import com.CH_gui.util.*;

import com.CH_guiLib.gui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

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
 * <b>$Revision: 1.29 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FolderSharingPanel extends JPanel implements DisposableObj {

  // Sharing page
  private JLabel jShareIcon;
  private JLabel jShareMsg;
  public JRadioButton jRadioDoNotShare;
  public JRadioButton jRadioInheritSharing;
  public JRadioButton jRadioDoShare;
  public JLabel jShareLabel;
  public JTextField jShareName;
  public JTextArea jShareDesc;
  private JButton jAdd;
  private JButton jRemove;

  public ShareTableModel shareTableModel;
  public RecordTableScrollPane tablePane;

  public boolean amIOwner;
  public boolean canIAdd;
  public boolean canIRemove;
  public boolean isSharableType;
  //public boolean isMySuperRoot;
  public boolean isFolderCreationMode;
  private short folderType;

  private FolderPair folderPair;
  private FetchedDataCache cache;

  // When in create folder mode, this key will be used to encrypt the new shares.
  private BASymmetricKey newFolderSymmetricKey;

  // The first time, in Folder Create mode, switching radio button to add shares copies
  // share name and description from those sources.
  private JTextField jShareNameSource;
  private JTextArea jShareDescSource;
  // true if radio button was ever pressed
  public boolean radioPressedActionDone;

  private DocumentChangeListener documentChangeListener;
  private boolean shareNameChangedByTyping;
  private boolean shareDescChangedByTyping;

  /**
   * Creates new FolderSharingPanel
   * Folder Creation mode
   */
  public FolderSharingPanel(BASymmetricKey newFolderSymmetricKey, JTextField jShareNameSource, JTextArea jShareDescSource, MemberContactRecordI[] addContacts) {
    this(null, true);
    this.newFolderSymmetricKey = newFolderSymmetricKey;
    this.jShareNameSource = jShareNameSource;
    this.jShareDescSource = jShareDescSource;
    initializeTrackingSourceFields();
    if (addContacts != null && addContacts.length > 0) {
      jRadioDoShare.setSelected(true);
      radioPressedPerform();
      addSharesForContacts(addContacts);
    }
  }
  /**
   * Creates new FolderSharingPanel
   * Folder Editing mode
   */
  public FolderSharingPanel(FolderPair folderPair) {
    this(folderPair, false);
  }
  private FolderSharingPanel(FolderPair folderPair, boolean isFolderCreationMode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderSharingPanel.class, "FolderSharingPanel(FolderPair folderPair, boolean isFolderCreationMode)");
    if (trace != null) trace.args(folderPair);
    if (trace != null) trace.args(isFolderCreationMode);

    this.cache = FetchedDataCache.getSingleInstance();
    this.folderPair = folderPair;

    FolderRecord folderRecord = isFolderCreationMode ? null : folderPair.getFolderRecord();
    this.isSharableType = isFolderCreationMode ? true : folderRecord.isSharableType();
    //this.isMySuperRoot = isFolderCreationMode ? false : FolderRecUtil.isMySuperRoot(folderRecord);
    this.amIOwner = isFolderCreationMode ? true : folderRecord.ownerUserId.equals(cache.getMyUserId());
    if (isFolderCreationMode || !folderRecord.isGroupType()) {
      canIAdd = amIOwner;
      canIRemove = amIOwner;
    } else {
      FolderShareRecord myShare = cache.getFolderShareRecordMy(folderRecord.folderId, false);
      if (myShare != null) {
        canIAdd = myShare.canWrite.shortValue() == FolderShareRecord.YES;
        canIRemove = myShare.canDelete.shortValue() == FolderShareRecord.YES;
      } else {
        FolderShareRecord[] myShares = cache.getFolderSharesMyForFolders(new Long[] { folderRecord.folderId }, true);
        for (int i=0; i<myShares.length; i++) {
          if (!canIAdd)
            canIAdd = myShares[i].canWrite.shortValue() == FolderShareRecord.YES;
          if (!canIRemove)
            canIRemove = myShares[i].canDelete.shortValue() == FolderShareRecord.YES;
          if (canIAdd && canIRemove)
            break;
        }
      }
    }
    //System.out.println("canIAdd="+canIAdd+", canIRemove="+canIRemove);
    this.isFolderCreationMode = isFolderCreationMode;

    createSharingPanel();


    setEnabledButtons();
    setEnabledFields();
    setEnabledRemoveButton();

    // set multi line html label after init() (after pack()) because it messes up the sizing of the dialog
    setFolderType(folderPair == null ? 0 : folderPair.getFolderRecord().folderType.shortValue());

    // set enabled / disabled components (add, remove buttons, and text fields)
    radioPressedPerform();

    // only the owner can modify permissions, even of non sharable super-root folders
    tablePane.getJSortedTable().setEnabled(isSharableType && canChange());
    shareTableModel.setEditable(isSharableType && (amIOwner || canIRemove));

    if (trace != null) trace.exit(FolderSharingPanel.class);
  }


  private void createSharingPanel() {
    setBorder(new EmptyBorder(10, 10, 10, 10));

    setLayout(new GridBagLayout());

    int posY = 0;
    jShareIcon = new JMyLabel(Images.get(ImageNums.FOLDER_SHARED48));
    add(jShareIcon, new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jShareMsg = new JMyLabel();
    add(jShareMsg, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    // separator
    add(new JSeparator(), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    if (isFolderCreationMode) {
      jRadioInheritSharing = new JMyRadioButton();
      jRadioInheritSharing.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          radioPressedAction();
        }
      });
      add(jRadioInheritSharing, new GridBagConstraints(0, posY, 2, 1, 10, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 15, 0, 5), 0, 0));
      posY ++;
    }

    jRadioDoNotShare = new JMyRadioButton();
//    if (folderType == FolderRecord.GROUP_FOLDER)
//      jRadioDoNotShare = new JMyRadioButton(com.CH_gui.lang.Lang.rb.getString("radio_Do_not_share_this_group."));
//    else
//      jRadioDoNotShare = new JMyRadioButton(com.CH_gui.lang.Lang.rb.getString("radio_Do_not_share_this_folder."));
    jRadioDoNotShare.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        radioPressedAction();
      }
    });
    add(jRadioDoNotShare, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, isFolderCreationMode ? new MyInsets(0, 15, 0, 5) : new MyInsets(5, 15, 0, 5), 0, 0));
    posY ++;

//    if (folderType == FolderRecord.GROUP_FOLDER)
//      jRadioDoShare = new JMyRadioButton(com.CH_gui.lang.Lang.rb.getString("radio_Customize_group_memberships."));
//    else
//      jRadioDoShare = new JMyRadioButton(com.CH_gui.lang.Lang.rb.getString("radio_Customize_sharing_properties."));
    jRadioDoShare = new JMyRadioButton();
    jRadioDoShare.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        radioPressedAction();
      }
    });
    add(jRadioDoShare, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 15, 0, 5), 0, 0));
    posY ++;

    ButtonGroup group = new ButtonGroup();
    if (jRadioInheritSharing != null) group.add(jRadioInheritSharing);
    group.add(jRadioDoNotShare);
    group.add(jRadioDoShare);
    if (!isFolderCreationMode && folderPair.getFolderRecord().numOfShares.shortValue() > 1) {
      group.setSelected(jRadioDoShare.getModel(), true);
    } else if (isFolderCreationMode && jRadioInheritSharing != null) {
      group.setSelected(jRadioInheritSharing.getModel(), true);
    } else {
      group.setSelected(jRadioDoNotShare.getModel(), true);
    }
    if (!isSharableType || !(amIOwner || canIAdd || canIRemove)) {
      if (jRadioInheritSharing != null) jRadioInheritSharing.setEnabled(false);
      jRadioDoNotShare.setEnabled(false);
      jRadioDoShare.setEnabled(false);
    }


    JPanel jSharePanel = new JPanel();
    jSharePanel.setBorder(new TitledBorder(new EtchedBorder(), com.CH_gui.lang.Lang.rb.getString("title_Currently_Authorized_Users")));
    // dummy
    add(jSharePanel, new GridBagConstraints(0, posY, 2, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 5, 5, 5), 0, 0));
    posY ++;


    jSharePanel.setLayout(new GridBagLayout());

    //String labelName = null;
//    String labelText = null;
//    if (folderType == FolderRecord.GROUP_FOLDER) {
//      //labelName = com.CH_gui.lang.Lang.rb.getString("label_Group_Name");
//      labelText = com.CH_gui.lang.Lang.rb.getString("label_Input_Group_Name_here");
//    } else {
//      //labelName = com.CH_gui.lang.Lang.rb.getString("label_Share_Name");
//      labelText = com.CH_gui.lang.Lang.rb.getString("label_Input_Share_Name_here");
//    }
    posY = 0;
    jShareLabel = new JMyLabel();
    jSharePanel.add(jShareLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jShareName = new JMyTextField();
//    if (!isFolderCreationMode)
//      jShareName.setText(folderPair.getMyName());
//    else
//      jShareName.setText(labelText);
    jSharePanel.add(jShareName, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    jSharePanel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Comment")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jShareDesc = new JMyTextArea(3, 20);
    if (!isFolderCreationMode)
      jShareDesc.setText(folderPair.getFolderShareRecord().getFolderDesc());
    jShareDesc.setWrapStyleWord(true);
    jShareDesc.setLineWrap(true);
    jSharePanel.add(new JScrollPane(jShareDesc), new GridBagConstraints(1, posY, 1, 3, 10, 5,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    posY += 3;


    if (!isFolderCreationMode)
      shareTableModel = new ShareTableModel(folderPair);
    else
      shareTableModel = new ShareTableModel();
    tablePane = new RecordTableScrollPane(shareTableModel);
    tablePane.setPreferredSize(new Dimension(230, 70));
    tablePane.getJSortedTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent event) {
        setEnabledRemoveButton();
      }
    });
    jSharePanel.add(tablePane, new GridBagConstraints(1, posY, 1, 3, 10, 20,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    jAdd = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Add"));
    jAdd.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedAdd();
      }
    });
    jSharePanel.add(jAdd, new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    jRemove = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Remove"));
    jRemove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedRemove();
      }
    });
    jSharePanel.add(jRemove, new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

  }

  private void initializeTrackingSourceFields() {
    documentChangeListener = new DocumentChangeListener();
    if (jShareNameSource != null) {
      jShareName.addKeyListener(documentChangeListener);
      jShareNameSource.addKeyListener(documentChangeListener);
    }
    if (jShareDescSource != null) {
      jShareDesc.addKeyListener(documentChangeListener);
      jShareDescSource.addKeyListener(documentChangeListener);
    }
  }

  private void radioPressedPerform() {
    boolean enableRename = jRadioDoShare.isSelected();
    boolean enableAdd = jRadioDoShare.isSelected() && canIAdd;
    boolean enableRemove = jRadioDoShare.isSelected() && canIRemove;
    if (enableRename) {
      enableRename = amIOwner;
    }
    jShareName.setEnabled(enableRename);
    jShareDesc.setEnabled(enableRename);
    jAdd.setEnabled(enableAdd);
    if (!enableRemove) {
      jRemove.setEnabled(false);
    }
  }

  private void radioPressedAction() {
    if (isFolderCreationMode) {
      // If first radio click, copy source name and description.
      if (!radioPressedActionDone) {
        jShareName.setText(jShareNameSource.getText());
        jShareDesc.setText(jShareDescSource.getText());
      }
    }
    radioPressedActionDone = true;
    radioPressedPerform();
  }


  private void setEnabledButtons() {
    if (!canIAdd)
      jAdd.setEnabled(false);
    if (!canIRemove)
      jRemove.setEnabled(false);
//    if (!amIOwner) {
//      jAdd.setEnabled(false);
//      jRemove.setEnabled(false);
//    }
  }


  private void setEnabledRemoveButton() {
    if (tablePane != null) {
      FolderShareRecord[] shares = (FolderShareRecord[]) tablePane.getSelectedRecords();
      if (shares != null && shares.length > 0) {
        boolean foundOld = false;
        boolean foundNotOwners = false;
        for (int i=0; i<shares.length; i++) {
          if (shares[i].shareId != null && shares[i].shareId.longValue() >= 0) {
            foundOld = true;
          }
          if (isFolderCreationMode || !shares[i].ownerUserId.equals(folderPair.getFolderRecord().ownerUserId)) {
            foundNotOwners = true;
          }
        }
        jRemove.setEnabled(foundNotOwners || !foundOld);
      } else {
        jRemove.setEnabled(false);
      }
    }
  }


  private void setEnabledFields() {
    if (!amIOwner) {
      jShareName.setEnabled(false);
      jShareDesc.setEnabled(false);
    }
    if (!amIOwner && !canIAdd && !canIRemove) {
      tablePane.getJSortedTable().setEnabled(false);
    }
  }


  private long fakeShareIdIndex = 0;
  private void pressedAdd() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderSharingPanel.class, "pressedAdd()");

    ContactSelectDialog d = new ContactSelectDialog((JDialog) SwingUtilities.windowForComponent(this), true);
    d.setSelectionCallback(new CallbackI() {
      public void callback(Object value) {
        addSharesForContacts((MemberContactRecordI[]) value);
      }
    });

    if (trace != null) trace.exit(FolderSharingPanel.class);
  }

  private void addSharesForContacts(MemberContactRecordI[] contacts) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderSharingPanel.class, "addSharesForContacts(ContactRecord[] contacts)");
    if (trace != null) trace.args(contacts);

    //ContactRecord[] noFldShareContacts = ContactRecord.filterDesiredPermitFlags(contacts, ContactRecord.PERMIT_DISABLE_SHARE_FOLDERS, ContactRecord.PERMIT_DISABLE_SHARE_FOLDERS);
    MemberContactRecordI[] noFldShareContacts = ContactRecord.filterDesiredPermitFlags(contacts, ContactRecord.PERMIT_DISABLE_SHARE_FOLDERS, ContactRecord.PERMIT_DISABLE_SHARE_FOLDERS, Boolean.FALSE);

    if (noFldShareContacts != null && noFldShareContacts.length > 0) {
      // display warning for contacts with missing permissions and subtract them from original list.
      if (noFldShareContacts != null && noFldShareContacts.length > 0) {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<noFldShareContacts.length; i++) {
          sb.append(ListRenderer.getRenderedText(noFldShareContacts[i]));
          if (i<noFldShareContacts.length-1)
            sb.append(", ");
        }
        String warnMsg = com.CH_gui.lang.Lang.rb.getString("msg_The_following_selected_contact(s)_have_folder_sharing_permission_disabled...");
        warnMsg += "\n\n" + sb.toString();
        String title = com.CH_gui.lang.Lang.rb.getString("msgTitle_No_folder_sharing_permission.");
        MessageDialog.showDialog(FolderSharingPanel.this, warnMsg, title, NotificationCenter.WARNING_MESSAGE, false);
      }

      contacts = (MemberContactRecordI[]) ArrayUtils.getDifference(contacts, noFldShareContacts);
    }

    if (trace != null) trace.data(10, contacts);
    if (contacts != null && contacts.length > 0) {
      ArrayList newSharesL = new ArrayList();
      ArrayList userIDsToFetchL = new ArrayList();
      for (int i=0; i<contacts.length; i++) {
        if (trace != null) trace.data(11, i);
        int rowCount = shareTableModel.getRowCount();
        if (trace != null) trace.data(12, rowCount);

        // don't select contacts for which shares already exist
        boolean alreadyExists = false;
        for (int k=0; k<rowCount; k++) {
          FolderShareRecord sRec = (FolderShareRecord) shareTableModel.getRowObject(k);
          if (sRec.ownerUserId.equals(contacts[i].getMemberId()) && sRec.ownerType.shortValue() == contacts[i].getMemberType()) {
            if (trace != null) trace.data(13, sRec);
            if (trace != null) trace.data(14, contacts[i]);
            if (trace != null) trace.data(15, sRec.ownerUserId);
            if (trace != null) trace.data(16, contacts[i].getMemberId());
            if (trace != null) trace.data(17, contacts[i].getMemberType());
            alreadyExists = true;
            break;
          }
        }
        if (trace != null) trace.data(18, alreadyExists);
        if (!alreadyExists) {
          FolderShareRecord newShare = new FolderShareRecord();
          //newShare.shareId = null; // don't assign any ID to the new share object -- its server's job

          if (!isFolderCreationMode) {
            newShare.folderId = folderPair.getId();

            // Set the view parent if one exists, if it is root (also super-root) then it has no parent and we will not set it.
            FolderRecord fRec = folderPair.getFolderRecord();
            if (!fRec.isRoot()) {
              newShare.setViewParentId(fRec.parentFolderId);
            }
          }

          newShare.ownerType = new Short(contacts[i].getMemberType());
          newShare.ownerUserId = contacts[i].getMemberId();
          newShare.canWrite = new Short(FolderShareRecord.YES);
          newShare.canDelete = new Short(FolderShareRecord.YES);

          // Set fake shareId for "Remove" purpose only, server will generate shareIDs later.
          fakeShareIdIndex --;
          newShare.shareId = new Long(fakeShareIdIndex);

          // unwrapped data
          if (!isFolderCreationMode)
            newShare.setSymmetricKey(folderPair.getFolderShareRecord().getSymmetricKey());
          else
            newShare.setSymmetricKey(newFolderSymmetricKey);

          newSharesL.add(newShare);
          // make sure we have that users record
          UserRecord uRec = cache.getUserRecord(newShare.ownerUserId);
          if (uRec == null) {
            userIDsToFetchL.add(newShare.ownerUserId);
          }

        }
      }
      if (newSharesL.size() > 0) {
        FolderShareRecord[] shares = new FolderShareRecord[newSharesL.size()];
        newSharesL.toArray(shares);
        if (trace != null) trace.data(18, shares);
        shareTableModel.updateData(shares);
      }
      if (userIDsToFetchL.size() > 0) {
        Long[] userIDs = new Long[userIDsToFetchL.size()];
        userIDsToFetchL.toArray(userIDs);
        MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.USR_Q_GET_HANDLES, new Obj_IDList_Co(userIDs)));
      }
    }

    if (trace != null) trace.exit(FolderSharingPanel.class);
  }


  private void pressedRemove() {
    FolderShareRecord[] shares = (FolderShareRecord[]) tablePane.getSelectedRecords();
    if (shares != null && shares.length > 0) {
      for (int i=0; i<shares.length; i++) {
        // if not mine then remove
        if (!shares[i].ownerUserId.equals(cache.getMyUserId()))
          shareTableModel.removeData(new FolderShareRecord[] {shares[i]});
      }
    }
  }


  private void setShareMsg() {
    String msg;

    if (!amIOwner) {
      if (!canIAdd && !canIRemove) {
        if (folderType == FolderRecord.GROUP_FOLDER)
          msg = com.CH_gui.lang.Lang.rb.getString("label_You_have_been_granted_access_to_this_group...");
        else
          msg = com.CH_gui.lang.Lang.rb.getString("label_You_have_been_granted_access_to_this_folder...");
      } else {
        if (folderType == FolderRecord.GROUP_FOLDER)
          msg = com.CH_gui.lang.Lang.rb.getString("label_You_have_been_granted_access_to_this_group_with_limited_ability_to_change_membership_properties...");
        else
          msg = com.CH_gui.lang.Lang.rb.getString("label_You_have_been_granted_access_to_this_folder_with_limited_ability_to_change_sharing_properties...");
      }
    }
    else if (isSharableType) {// && !isMySuperRoot)
      if (folderType == FolderRecord.GROUP_FOLDER)
        msg = com.CH_gui.lang.Lang.rb.getString("label_You_can_share_this_group_among_other_users_in_your_contact_list...");
      else
        msg = com.CH_gui.lang.Lang.rb.getString("label_You_can_share_this_folder_among_other_users_in_your_contact_list...");
//    else if (isSharableType && isMySuperRoot)
//      msg = com.CH_gui.lang.Lang.rb.getString("label_You_cannot_share_a_super_root_folder...");
    } else if (!isSharableType) {
      msg = com.CH_gui.lang.Lang.rb.getString("label_Folders_of_this_type_are_not_sharable.");
    } else {
      msg = com.CH_gui.lang.Lang.rb.getString("label_Unexpected_folder_type.");
    }

    jShareMsg.setText(msg);
  }

  public void setFolderType(short folderType) {
    this.folderType = folderType;
    setShareMsg();
    String labelText = null;
    String labelText1 = com.CH_gui.lang.Lang.rb.getString("label_Input_Group_Name_here");
    String labelText2 = com.CH_gui.lang.Lang.rb.getString("label_Input_Share_Name_here");
    if (folderType == FolderRecord.GROUP_FOLDER) {
      ShareTableModel.columnHeaderDatas[1].applyToTable(tablePane.getJSortedTable());
      jShareIcon.setIcon(Images.get(ImageNums.PEOPLE48));
      if (jRadioInheritSharing != null)
        jRadioInheritSharing.setText(com.CH_gui.lang.Lang.rb.getString("radio_Inherit_membership_properties_from_parent_folder."));
      jRadioDoNotShare.setText(com.CH_gui.lang.Lang.rb.getString("radio_Do_not_share_this_group."));
      jRadioDoShare.setText(com.CH_gui.lang.Lang.rb.getString("radio_Customize_group_memberships."));
      jShareLabel.setText(com.CH_gui.lang.Lang.rb.getString("label_Group_Name"));
      labelText = labelText1;
    } else {
      ShareTableModel.columnHeaderDatas[0].applyToTable(tablePane.getJSortedTable());
      if (FolderRecord.isAddressType(folderType))
        jShareIcon.setIcon(Images.get(ImageNums.ADDRESS_BOOK_SHARED48));
      else if(folderType == FolderRecord.CHATTING_FOLDER)
        jShareIcon.setIcon(Images.get(ImageNums.CHAT_BUBBLE_SHARED48));
      else
        jShareIcon.setIcon(Images.get(ImageNums.FOLDER_SHARED48));
      if (jRadioInheritSharing != null)
        jRadioInheritSharing.setText(com.CH_gui.lang.Lang.rb.getString("radio_Inherit_sharing_properties_from_parent_folder."));
      jRadioDoNotShare.setText(com.CH_gui.lang.Lang.rb.getString("radio_Do_not_share_this_folder."));
      jRadioDoShare.setText(com.CH_gui.lang.Lang.rb.getString("radio_Customize_sharing_properties."));
      jShareLabel.setText(com.CH_gui.lang.Lang.rb.getString("label_Share_Name"));
      labelText = labelText2;
    }
    if (jShareName.getText().equals("") || jShareName.getText().equals(labelText1) || jShareName.getText().equals(labelText2)) {
      if (!isFolderCreationMode)
        jShareName.setText(folderPair.getMyName());
      else
        jShareName.setText(labelText);
    }
  }

  public boolean canAdd() {
    return canIAdd;
  }

  public boolean canChange() {
    return amIOwner || canIAdd || canIRemove;
  }

  public boolean canRemove() {
    return canIRemove;
  }

  public ArrayList gatherWantedShares() {
    ArrayList sharesL = shareTableModel.getRowListForViewOnly();
    ArrayList wantedSharesL = new ArrayList();
    // clear wanted shares if Radio DO NOT SHARE is selected
    if (jRadioDoNotShare.isSelected()) {
      for (int i=0; i<sharesL.size(); i++) {
        FolderShareRecord share = (FolderShareRecord) sharesL.get(i);
        if (share.isOwnedBy(folderPair.getFolderRecord().ownerUserId, (Long[]) null)) {
          wantedSharesL.add(share);
        }
      }
    } else {
      wantedSharesL.addAll(sharesL);
    }
    return wantedSharesL;
  }


  /**
   * I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
   * Dispose the object and release resources to help in garbage collection.
  */
  public void disposeObj() {
    if (tablePane != null) {
      tablePane.disposeObj();
      tablePane = null;
    }
    if (documentChangeListener != null) {
      if (jShareName != null)
        jShareName.removeKeyListener(documentChangeListener);
      if (jShareDesc != null)
        jShareDesc.removeKeyListener(documentChangeListener);
      if (jShareNameSource != null)
        jShareNameSource.removeKeyListener(documentChangeListener);
      if (jShareDescSource != null)
        jShareDescSource.removeKeyListener(documentChangeListener);
      documentChangeListener = null;
    }
  }


  private class DocumentChangeListener implements KeyListener {
    private void typeEvent(KeyEvent e) {
      Object source = e.getSource();
      if (source.equals(jShareName)) {
        shareNameChangedByTyping = true;
      }
      if (source.equals(jShareDesc)) {
        shareDescChangedByTyping = true;
      }
      if (jShareName != null && !radioPressedActionDone && !shareNameChangedByTyping && source.equals(jShareNameSource)) {
        jShareName.setText(jShareNameSource.getText());
      }
      if (jShareDesc != null && !radioPressedActionDone && !shareDescChangedByTyping && source.equals(jShareDescSource)) {
        jShareDesc.setText(jShareDescSource.getText());
      }
    }
    public void keyPressed(java.awt.event.KeyEvent keyEvent) {
      typeEvent(keyEvent);
    }
    public void keyReleased(java.awt.event.KeyEvent keyEvent) {
      typeEvent(keyEvent);
    }
    public void keyTyped(java.awt.event.KeyEvent keyEvent) {
      typeEvent(keyEvent);
    }
  }

}
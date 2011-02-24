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

package com.CH_gui.dialog;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.ops.*;

import com.CH_co.service.records.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.fld.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.file.FileUtilities;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.folder.*;
import com.CH_gui.gui.*;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.service.records.RecordUtilsGui;
import com.CH_gui.util.*;
import com.CH_guiLib.gui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.border.*;
import javax.swing.*;
import javax.swing.event.*;

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
 * <b>$Revision: 1.34 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FolderPropertiesDialog extends GeneralDialog implements VisualsSavable {

  private static final int DEFAULT_OK_INDEX = 0;
  private static final int DEFAULT_CANCEL_INDEX = 2;

  private FolderPair folderPair;
  private boolean amIOwner;
  private boolean isMySuperRoot;
  private boolean isSharableType;

  // General page
  private JTextField jFolderName;
  private JTextArea jFolderDesc;
  private JLabel jFolderOwner;
  private JLabel jSize;
  private JLabel jSizeOnDisk;
  private JLabel jContains;

  private FolderSharingPanel folderSharingPanel;
  private FolderPurgingPanel folderPurgingPanel;

  private JButton jOk;
  private JButton jTranscript;

  private ServerInterfaceLayer SIL;
  private FetchedDataCache cache;

  private static String FETCHING_DATA = com.CH_gui.lang.Lang.rb.getString("Fetching_Data...");

  private DocumentChangeListener documentChangeListener;


  /** Creates new FolderPropertiesDialog */
  public FolderPropertiesDialog(Frame owner, FolderPair folderPair) {
    this(owner, folderPair, 0);
  }
  public FolderPropertiesDialog(Dialog owner, FolderPair folderPair) {
    this(owner, folderPair, 0);
  }
  public FolderPropertiesDialog(Frame owner, FolderPair folderPair, int openWithTabNumber) {
    super(owner, java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString(folderPair.getFolderRecord().isGroupType() ? "title_OBJECT_-_Group_Properties_and_Members" : "title_OBJECT_-_Folder_Properties_and_Sharing"), new Object[] {folderPair.getMyName()}));
    constructDialog(owner, folderPair, openWithTabNumber);
  }
  public FolderPropertiesDialog(Dialog owner, FolderPair folderPair, int openWithTabNumber) {
    super(owner, java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString(folderPair.getFolderRecord().isGroupType() ? "title_OBJECT_-_Group_Properties_and_Members" : "title_OBJECT_-_Folder_Properties_and_Sharing"), new Object[] {folderPair.getMyName()}));
    constructDialog(owner, folderPair, openWithTabNumber);
  }
  private void constructDialog(Component owner, FolderPair folderPair, int openWithTabNumber) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderPropertiesDialog.class, "FolderPropertiesDialog()");

    this.folderPair = folderPair;
    this.SIL = MainFrame.getServerInterfaceLayer();
    this.cache = FetchedDataCache.getSingleInstance();

    FolderRecord folderRecord = folderPair.getFolderRecord();
    UserRecord myUser = cache.getUserRecord();
    this.amIOwner = folderPair.getFolderRecord().ownerUserId.equals(myUser.userId);
    this.isMySuperRoot = folderRecord.isSuperRoot(myUser);
    this.isSharableType = folderRecord.isSharableType();

    JButton[] buttons = createButtons();
    JComponent mainComponent = createTabbedPane(openWithTabNumber);
    init(owner, buttons, mainComponent, DEFAULT_OK_INDEX, DEFAULT_CANCEL_INDEX);

    // Add focus requestor to the folder name line
    jFolderName.addHierarchyListener(new InitialFocusRequestor());

    setEnabledButtons();

    fetchData();

    if (trace != null) trace.exit(FolderPropertiesDialog.class);
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[3];

    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
    buttons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedOK();
      }
    });
    jOk = buttons[0];

    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Transcript"));
    buttons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedTranscript();
      }
    });
    jTranscript = buttons[1];

    buttons[2] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[2].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedCancel();
      }
    });

    return buttons;
  }

  private JTabbedPane createTabbedPane(int openWithTabNumber) {
    FolderRecord folderRec = folderPair.getFolderRecord();

    JTabbedPane pane = new JMyTabbedPane();
    pane.addTab(com.CH_gui.lang.Lang.rb.getString("tab_General"), createGeneralPanel());

    folderSharingPanel = new FolderSharingPanel(folderPair);
    folderSharingPanel.jShareName.getDocument().addDocumentListener(documentChangeListener);
    if (folderRec.isGroupType()) {
      pane.addTab(com.CH_gui.lang.Lang.rb.getString("tab_Members"), folderSharingPanel);
    } else {
      pane.addTab(com.CH_gui.lang.Lang.rb.getString("tab_Sharing"), folderSharingPanel);
    }

    if (folderRec.isFileType() || folderRec.isMailType()) {
      folderPurgingPanel = new FolderPurgingPanel(folderPair);
      pane.addTab(com.CH_gui.lang.Lang.rb.getString("tab_Purging"), folderPurgingPanel);
    }

    pane.setSelectedIndex(openWithTabNumber);
    return pane;
  }

  private JPanel createGeneralPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));

    panel.setLayout(new GridBagLayout());

    jFolderName = new JMyTextField(folderPair.getMyName());
    jFolderName.addHierarchyListener(new InitialFocusRequestor());
    documentChangeListener = new DocumentChangeListener();
    jFolderName.getDocument().addDocumentListener(documentChangeListener);

    int imageNum = 0;
    if (folderPair.getFolderRecord().isGroupType())
      imageNum = ImageNums.PEOPLE48;
    else if (folderPair.getFolderRecord().isAddressType())
      imageNum = ImageNums.ADDRESS_BOOK48;
    else if (folderPair.getFolderRecord().isChatting())
      imageNum = ImageNums.CHAT_BUBBLE48;
    else
      imageNum = ImageNums.FOLDER48;
    int posY = 0;
    panel.add(new JMyLabel(Images.get(imageNum)), new GridBagConstraints(0, posY, 1, 2, 0, 0,
          GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Name")), new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jFolderName, new GridBagConstraints(1, posY+1, 2, 1, 10, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;
    posY ++;


    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 3, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Folder_ID")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JMyLabel(folderPair.getId().toString()), new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Type")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    JLabel jType = new JMyLabel(folderPair.getFolderRecord().getFolderType(), RecordUtilsGui.getIcon(folderPair), JLabel.LEFT);
    panel.add(jType, new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Folder_Owner")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jFolderOwner = new JMyLabel(FETCHING_DATA);
    panel.add(jFolderOwner, new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Size")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jSize = new JMyLabel(FETCHING_DATA);
    panel.add(jSize, new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Size_on_Disk")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jSizeOnDisk = new JMyLabel(FETCHING_DATA);
    panel.add(jSizeOnDisk, new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Contains")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jContains = new JMyLabel(FETCHING_DATA);
    panel.add(jContains, new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 3, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Folder_Created")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JMyLabel(Misc.getFormattedTimestamp(folderPair.getFolderRecord().dateCreated)), new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Folder_Updated")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    String dateUpdated = Misc.getFormattedTimestamp(folderPair.getFolderRecord().dateUpdated);
    panel.add(new JMyLabel(dateUpdated), new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 3, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Encryption")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JMyLabel("AES(256)"), new GridBagConstraints(1, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Comment")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    String desc = folderPair.getFolderShareRecord().getFolderDesc();
    jFolderDesc = new JMyTextArea(desc != null ? desc : "", 4, 20);
    jFolderDesc.setWrapStyleWord(true);
    jFolderDesc.setLineWrap(true);
    jFolderDesc.getDocument().addDocumentListener(documentChangeListener);
    panel.add(new JScrollPane(jFolderDesc), new GridBagConstraints(1, posY, 2, 3, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    return panel;
  }


  private void setEnabledButtons() {
    // see if Name or Comment has changed
    String newName = jFolderName.getText().trim();
    String newShareName = folderSharingPanel.jShareName.getText().trim();
    if (newName != null && newName.length() > 0 && newShareName != null && newShareName.length() > 0) {
      jOk.setEnabled(true);
    } else {
      jOk.setEnabled(false);
    }
  }


  private void pressedOK() {
    //System.out.println("pressedOK");
    // see if we need to update File Name or Comment
    FolderShareRecord oldFolderShare = (FolderShareRecord) folderPair.getFolderShareRecord();
    String oldName = oldFolderShare.getFolderName();
    String oldDesc = oldFolderShare.getFolderDesc();
    oldDesc = oldDesc != null ? oldDesc : "";
    String newName = jFolderName.getText().trim();
    String newDesc = jFolderDesc.getText().trim();
    String newShareName = folderSharingPanel.jShareName.getText().trim();
    String newShareDesc = folderSharingPanel.jShareDesc.getText().trim();
    // if share name/desc is not adjusted, copy it from main page
    if (newShareName.equals(oldName) && newShareDesc.equals(oldDesc)) {
      newShareName = newName;
      newShareDesc = newDesc;
    }
    // reaname folder and set new comment
    if (newName.length() > 0 && newShareName.length() > 0 && (!newName.equals(oldName) || !newDesc.equals(oldDesc) || !newShareName.equals(oldName) || !newShareDesc.equals(oldDesc))) {
      FolderShareRecord newFolderShare = (FolderShareRecord) oldFolderShare.clone();
      newFolderShare.setFolderName(newName);

      oldFolderShare.setFolderName(newName + "^");
      oldFolderShare.setFolderDesc(newDesc + "^");

      if (newDesc.length() == 0)
        newDesc = null;
      if (newShareDesc.length() == 0)
        newShareDesc = null;

      FileUtilities.renameFolderAndShares(newName, newDesc, newShareName, newShareDesc, newFolderShare);
    }

    // submit any changes to the purging settings
    if (folderPurgingPanel != null && folderPurgingPanel.folderAttributesPanel != null && amIOwner && isSharableType) {
      FolderRecord fRec = folderPair.getFolderRecord();
      Short numToKeep = folderPurgingPanel.folderAttributesPanel.getNumToKeep();
      Integer keepAsOldAs = folderPurgingPanel.folderAttributesPanel.getKeepAsOldAs();
      if (numToKeep != null && keepAsOldAs != null && (!fRec.numToKeep.equals(numToKeep) || !fRec.keepAsOldAs.equals(keepAsOldAs))) {
        Obj_IDList_Co request = new Obj_IDList_Co(new Long[] { fRec.folderId, new Long(numToKeep.shortValue()), new Long(keepAsOldAs.intValue()) });
        SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_ALTER_FLD_ATTR, request));
      }
    }

    // see if anything changed with sharing of the folder, if so submit changes, else do not attempt to submit
    if (folderSharingPanel.canChange()) {
      FolderShareRecord[] existingShares = cache.getFolderShareRecordsForFolder(folderPair.getId());
      ArrayList wantedSharesL = folderSharingPanel.gatherWantedShares();
      FolderShareRecord[][] shareChanges = FolderShareOps.getFolderShareChanges(existingShares, wantedSharesL, folderPair);
      //System.out.println("shareChanges="+Misc.objToStr(shareChanges));
      if (shareChanges != null) {
        final FolderPair[] children = cache.getFolderPairsViewAllDescending(new FolderPair[] { folderPair }, false);
        if (!folderPair.getFolderRecord().isGroupType() && children != null && children.length > 0) {
          String[] lines = new String[] {
              "You have chosen to make folder sharing changes.",
              "Do you want to apply this change to this folder only, or do you",
              "want to apply it to all subfolders and files as well?"
          };
          String[] choices = new String[] {
              "Apply changes to this folder only",
              "Apply changes to this folder and subfolders"
          };
          Insets[] insets = new Insets[] {
              new MyInsets(5, 5, 5, 5),
              new MyInsets(5, 5, 1, 5),
              new MyInsets(1, 5, 5, 5),
              new MyInsets(5, 25, 2, 5),
              new MyInsets(2, 25, 5, 5)
          };
          ChoiceDialog choiceDialog = new ChoiceDialog(FolderPropertiesDialog.this, "Confirm Sharing Changes", lines, choices, insets, 1);
          Integer resultButton = choiceDialog.getResultButton();
          if (resultButton != null && resultButton.intValue() == ChoiceDialog.DEFAULT_OK_INDEX) {
            submitShareChanges(choiceDialog.getResultChoice().intValue() == 1, children);
          }
        } else {
          submitShareChanges(false, null);
        }
      }
    }

    closeDialog();
  }

  private void submitShareChanges(final boolean applyToTree, final FolderPair[] children) {

    Thread th = new ThreadTraced("Submitter of Share Changes") {
      public void runTraced() {
        FolderPair[] foldersToApply = new FolderPair[] { folderPair };
        if (children != null && children.length > 0) {
          if (applyToTree) {
            foldersToApply = (FolderPair[]) ArrayUtils.concatinate(foldersToApply, children);
            foldersToApply = (FolderPair[]) ArrayUtils.removeDuplicates(foldersToApply);
          }
        }

        Vector allSharesToRemoveV = new Vector();
        Vector allSharesToChangeV = new Vector();
        Vector allSharesToAddV = new Vector();

        for (int x=0; x<foldersToApply.length; x++) {

          // which folder is being altered... in case of applying changes to sub-tree;
          FolderPair folderPairChanged = foldersToApply[x];

          // if my folder so I can change sharing of this folder
          if (
              (!folderPairChanged.getFolderRecord().isGroupType() && folderPairChanged.getFolderRecord().ownerUserId.equals(cache.getMyUserId())) ||
              // or a group folder the one being changed in the dialog
              (folderPairChanged.getFolderRecord().isGroupType() && folderPairChanged.getId().equals(folderPair.getId()))
             ) {

            // commit removal of old shares
            FolderShareRecord[] existingShares = cache.getFolderShareRecordsForFolder(folderPairChanged.getId());
            ArrayList wantedSharesL = folderSharingPanel.gatherWantedShares();
            FolderShareRecord[][] shareChanges = FolderShareOps.getFolderShareChanges(existingShares, wantedSharesL, folderPairChanged);
            if (shareChanges != null && shareChanges[0] != null && shareChanges[0].length > 0) {
              allSharesToRemoveV.addAll(Arrays.asList(shareChanges[0]));
            }
            // commit changes to the old existing shares
            if (shareChanges != null && shareChanges[1] != null && shareChanges[1].length > 0) {
              allSharesToChangeV.addAll(Arrays.asList(shareChanges[1]));
            }
            // commit addition of new shares
            // if (folderSharingPanel.jRadioDoShare.isSelected()) {
              if (shareChanges != null && shareChanges[2] != null && shareChanges[2].length > 0) {
                FolderShareRecord[] newShares = shareChanges[2];

                // Fetch public keys for involved users that are not already fetched
                Vector userIDsV = new Vector();
                for (int i=0; i<newShares.length; i++) {
                  Long ownerUserId = newShares[i].ownerUserId;
                  KeyRecord keyRec = cache.getKeyRecordForUser(ownerUserId);
                  if (keyRec == null || keyRec.plainPublicKey == null) {
                    if (!userIDsV.contains(ownerUserId))
                      userIDsV.addElement(ownerUserId);
                  }
                }
                // We are lacking public KEYs, fetch them now.
                if (userIDsV.size() > 0) {
                  Long[] userIDs = new Long[userIDsV.size()];
                  userIDsV.toArray(userIDs);
                  SIL.submitAndWait(new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_USERS, new Obj_IDList_Co(userIDs)), 60000);
                }

                for (int i=0; i<newShares.length; i++) {
                  String name = null;
                  String desc = null;
                  if (folderPairChanged.getId().equals(folderPair.getId())) {
                    name = folderSharingPanel.jShareName.getText().trim();
                    desc = folderSharingPanel.jShareDesc.getText().trim();
                  } else {
                    name = folderPairChanged.getFolderShareRecord().getFolderName();
                    desc = folderPairChanged.getFolderShareRecord().getFolderDesc();
                  }
                  newShares[i].setFolderName(name);
                  if (desc != null && desc.length() > 0) {
                    newShares[i].setFolderDesc(desc);
                  } else {
                    newShares[i].setFolderDesc(null);
                  }
                  if (newShares[i].isOwnedByUser()) {
                    KeyRecord kRec = cache.getKeyRecordForUser(newShares[i].ownerUserId);
                    // we should have the public key of the user, but check just in case
                    if (kRec != null) {
                      newShares[i].seal(kRec);
                    } else {
                      MessageDialog.showErrorDialog(FolderPropertiesDialog.this, com.CH_gui.lang.Lang.rb.getString("msg_Could_not_fetch_user's_Public_Key.__Operation_terminated."), com.CH_gui.lang.Lang.rb.getString("msgTitle_Fetch_Error"));
                      throw new RuntimeException(com.CH_gui.lang.Lang.rb.getString("msg_Could_not_fetch_user's_Public_Key.__Operation_terminated."));
                    }
                  } else {
                    FolderShareRecord groupShare = cache.getFolderShareRecordMy(newShares[i].ownerUserId, true);
                    // we should have the key of the group, but check just in case
                    if (groupShare != null) {
                      newShares[i].seal(groupShare.getSymmetricKey());
                    } else {
                      MessageDialog.showErrorDialog(FolderPropertiesDialog.this, "Could not locate group's encryption key.  Operation terminated.", "Fetch Error");
                      throw new RuntimeException("Could not locate group's encryption key.  Operation terminated.");
                    }
                  }
                }

                allSharesToAddV.addAll(Arrays.asList(newShares));
              }
            // }
          }
        } // end for

        StringBuffer errorSB = new StringBuffer();
        if (allSharesToRemoveV.size() > 0 && !folderSharingPanel.canRemove()) {
          errorSB.append("You do not have sufficient privilege to REMOVE shares.\n");
          allSharesToRemoveV.clear();
        }
        if (allSharesToChangeV.size() > 0 && !folderSharingPanel.canChange()) {
          errorSB.append("You do not have sufficient privilege to MODIFY existing shares.\n");
          allSharesToChangeV.clear();
        }
        if (allSharesToAddV.size() > 0 && !folderSharingPanel.canAdd()) {
          errorSB.append("You do not have sufficient privilege to ADD new shares.\n");
          allSharesToAddV.clear();
        }
        boolean anythingToDo = true;
        if (errorSB.length() > 0) {
          if (allSharesToRemoveV.size() > 0 || allSharesToChangeV.size() > 0 || allSharesToAddV.size() > 0) {
            errorSB.append("\nWould you like to continue?");
          } else {
            anythingToDo = false;
          }
        }
        if (errorSB.length() > 0) {
          if (anythingToDo)
            anythingToDo = MessageDialog.showDialogYesNo(null, errorSB.toString(), "Insufficient privileges...", NotificationCenter.WARNING_MESSAGE);
          else
            MessageDialog.showDialog(null, errorSB.toString(), "Insufficient privileges...", NotificationCenter.ERROR_MESSAGE, false);
        }

        if (anythingToDo) {
          if (allSharesToRemoveV.size() > 0) {
            Long[] IDs = RecordUtils.getIDs(allSharesToRemoveV);
            Obj_IDList_Co request = new Obj_IDList_Co(IDs);
            SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_REMOVE_FOLDER_SHARES, request));
          }

          if (allSharesToChangeV.size() > 0) {
            FolderShareRecord[] changedShares = new FolderShareRecord[allSharesToChangeV.size()];
            allSharesToChangeV.toArray(changedShares);
            Fld_AltPerm_Rq request = new Fld_AltPerm_Rq(changedShares);
            SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_ALTER_PERMISSIONS, request));
          }

          if (allSharesToAddV.size() > 0) {
            FolderShareRecord[] newShares = new FolderShareRecord[allSharesToAddV.size()];
            allSharesToAddV.toArray(newShares);
            Fld_AddShares_Rq request = new Fld_AddShares_Rq();
            request.contactIds = new Obj_IDList_Co(RecordUtils.getIDs(cache.getContactRecordsMyActive(true)));
            request.groupShareIds = new Obj_IDList_Co(RecordUtils.getIDs(cache.getFolderSharesMyForFolders(cache.getFolderGroupIDsMy(), true)));
            request.shareRecords = newShares;
            SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_ADD_FOLDER_SHARES, request));
          }
        }
      }
    };
    th.setDaemon(true);
    th.start();
  }

  private void pressedCancel() {
    closeDialog();
  }



  private void pressedTranscript() {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    UserRecord userRec = cache.getUserRecord();
    String RSA = cache.getKeyRecord(userRec.pubKeyId).plainPublicKey.shortInfo().toUpperCase();
    FolderShareRecord sRec = folderPair.getFolderShareRecord();
    KeyRecord myKeyRec = cache.getKeyRecordMyCurrent();

    StringBuffer sb = new StringBuffer();
    sb.append("--- BEGIN RECEIVED FOLDER");

    sb.append("\n--- BEGIN AES(256) ENCRYPTED FOLDER NAME\n\n");
    sb.append(ArrayUtils.breakLines(sRec.getEncFolderName().getHexContent(), 80));
    sb.append("\n\n--- END AES(256) ENCRYPTED FOLDER NAME");

    sb.append("\n\n--- BEGIN AES(256) ENCRYPTED FOLDER DESCRIPTION\n\n");
    sb.append(ArrayUtils.breakLines(sRec.getEncFolderDesc() != null ? sRec.getEncFolderDesc().getHexContent() : "", 80));
    sb.append("\n\n--- END AES(256) ENCRYPTED FOLDER DESCRIPTION");

    sb.append("\n\n--- BEGIN AES(256) ENCRYPTED FOLDER AES(256) KEY\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.spreadString(sRec.getEncSymmetricKey().getHexContent(), 4, ' '), 80));
    sb.append("\n\n--- END AES(256) ENCRYPTED FOLDER AES(256) KEY");

    sb.append("\n\n--- BEGIN "+RSA+" ENCRYPTED SUPER FOLDER AND CONTACT AES(256) KEYS\n\n");
    sb.append(ArrayUtils.breakLines(userRec.getEncSymKeys().getHexContent(), 80));
    sb.append("\n\n--- END "+RSA+" ENCRYPTED SUPER FOLDER AND CONTACT AES(256) KEYS");

    sb.append("\n\n--- BEGIN AES(256) PASS-CODE ENCRYPTED "+RSA+" PRIVATE KEY\n\n");
    sb.append(ArrayUtils.breakLines(myKeyRec.getEncPrivateKey().getHexContent(), 80));
    sb.append("\n\n--- END AES(256) PASS-CODE ENCRYPTED "+RSA+" PRIVATE KEY");

    sb.append("\n--- END RECEIVED FOLDER");


    sb.append("\n\n--- BEGIN COMPUTED FOLDER");
    sb.append("\n--- BEGIN PLAIN FOLDER NAME\n\n");
    sb.append(sRec.getFolderName());
    sb.append("\n\n--- END PLAIN FOLDER NAME");

    sb.append("\n\n--- BEGIN PLAIN FOLDER DESCRIPTION\n\n");
    sb.append(sRec.getFolderDesc() != null ? sRec.getFolderDesc() : "");
    sb.append("\n\n--- END PLAIN FOLDER DESCRIPTION");
/*
    sb.append("\n\n--- BEGIN PLAIN SUPER FOLDER KEY\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.spreadString(userRec.getSymKeyFldShares().getHexContent(), 4, ' '), 80));
    sb.append("\n\n--- END PLAIN SUPER FOLDER KEY");

    sb.append("\n\n--- BEGIN PLAIN FOLDER KEY\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.spreadString(sRec.getSymmetricKey().getHexContent(), 4, ' '), 80));
    sb.append("\n\n--- END PLAIN FOLDER KEY");
*/
    sb.append("\n--- END COMPUTED FOLDER");


    JTextArea textArea = new JMyTextArea(sb.toString());
    textArea.setEditable(false);
    textArea.setCaretPosition(0);
    textArea.setRows(35);
    textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

    JButton jClose = new JMyButton("Close");
    jClose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Dialog d = (Dialog) SwingUtilities.windowForComponent((Component) e.getSource());
        d.dispose();
      }
    });
    new GeneralDialog(this, com.CH_gui.lang.Lang.rb.getString("title_Folder_Transcript"), new JButton[] { jClose }, -1, 0, new JScrollPane(textArea));
  }



  private void fetchData() {
    Thread th = new ThreadTraced("Folder Properties Dialog Data Fetcher") {
      public void runTraced() {
        // fetch owner User Record
        Long userId = folderPair.getFolderRecord().ownerUserId;
        UserRecord uRec = cache.getUserRecord(userId);
        if (uRec == null) {
          SIL.submitAndWait(new MessageAction(CommandCodes.USR_Q_GET_HANDLES, new Obj_IDList_Co(userId)), 30000);
          uRec = cache.getUserRecord(userId);
        }
        if (uRec == null) {
          uRec = new UserRecord();
          uRec.userId = userId;
        }
        jFolderOwner.setText(ListRenderer.getRenderedText(uRec));
        jFolderOwner.setIcon(RecordUtilsGui.getIcon(uRec));

        // fetch folder size

        FolderPair[] folderViewTree = cache.getFolderPairsViewAllDescending(new FolderPair[] { folderPair }, true);
        FolderShareRecord[] shareViewTree = FolderPair.getFolderShareRecords(folderViewTree);
        ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.FLD_Q_GET_SIZE_SUMMARY, new Obj_IDList_Co(RecordUtils.getIDs(shareViewTree))), 120000);
        DefaultReplyRunner.nonThreadedRun(SIL, msgAction);

        if (msgAction != null && msgAction.getActionCode() == CommandCodes.FLD_A_GET_SIZE_SUMMARY) {
          Obj_List_Co data = (Obj_List_Co) msgAction.getMsgDataSet();
          //System.out.println("data="+Misc.objToStr(data.objs));
          // sum the size
          long size = 0;
          long sizeOnDisk = 0;
          size += ((Long[]) data.objs[0])[2].longValue();
          size += ((Long[]) data.objs[1])[2].longValue();
          size += ((Long[]) data.objs[2])[2].longValue();
          sizeOnDisk += ((Long[]) data.objs[0])[3].longValue();
          sizeOnDisk += ((Long[]) data.objs[1])[3].longValue();
          sizeOnDisk += ((Long[]) data.objs[2])[3].longValue();

          StringBuffer sb = new StringBuffer();
          long countFiles = ((Long[]) data.objs[0])[0].longValue();
          if (countFiles > 0) {
            if (sb.length() > 0)
              sb.append(", ");
            sb.append(countFiles);
            sb.append(' ');
            sb.append(countFiles == 1 ? com.CH_gui.lang.Lang.rb.getString("File") : com.CH_gui.lang.Lang.rb.getString("Files"));
          }

          long countMsgs = ((Long[]) data.objs[1])[0].longValue();
          if (countMsgs > 0) {
            if (sb.length() > 0)
              sb.append(", ");
            sb.append(countMsgs);
            sb.append(' ');
            sb.append(countMsgs == 1 ? com.CH_gui.lang.Lang.rb.getString("Message") : com.CH_gui.lang.Lang.rb.getString("Messages"));
          }

          long countAddrs = ((Long[]) data.objs[2])[0].longValue();
          if (countAddrs > 0) {
            if (sb.length() > 0)
              sb.append(", ");
            sb.append(countAddrs);
            sb.append(' ');
            sb.append(countAddrs == 1 ? com.CH_gui.lang.Lang.rb.getString("Address") : com.CH_gui.lang.Lang.rb.getString("Addresses"));
          }

          long countMembers = ((Long[]) data.objs[3])[0].longValue();
          if (countMembers > 0) {
            if (sb.length() > 0)
              sb.append(", ");
            sb.append(countMembers);
            sb.append(' ');
            sb.append(countMembers == 1 ? com.CH_gui.lang.Lang.rb.getString("Member") : com.CH_gui.lang.Lang.rb.getString("Members"));
          }

          long countFolders = 0;
          // subtract one for current folder (it is not contained inside itself)
          switch (folderPair.getFolderRecord().folderType.shortValue()) {
            case FolderRecord.FILE_FOLDER:
            case FolderRecord.MESSAGE_FOLDER:
            case FolderRecord.POSTING_FOLDER:
            case FolderRecord.CHATTING_FOLDER:
            case FolderRecord.ADDRESS_FOLDER:
            case FolderRecord.WHITELIST_FOLDER:
            case FolderRecord.RECYCLE_FOLDER:
              countFolders = -1;
          }
          countFolders += ((Long[]) data.objs[0])[1].longValue();
          countFolders += ((Long[]) data.objs[1])[1].longValue();
          countFolders += ((Long[]) data.objs[2])[1].longValue();
          if (sb.length() > 0)
            sb.append(", ");
          sb.append(countFolders);
          sb.append(' ');
          sb.append(countFolders == 1 ? "Folder" : "Folders");

          /*
          String suffix = "";
          long countObjs = data.objId_1.longValue();
          folderPair.getFolderRecord().objectCount = new Long(countObjs);
          switch (folderPair.getFolderRecord().folderType.shortValue()) {
            case FolderRecord.FILE_FOLDER:
              suffix = countObjs == 1 ? com.CH_gui.lang.Lang.rb.getString("File") : com.CH_gui.lang.Lang.rb.getString("Files");
              break;
            case FolderRecord.MESSAGE_FOLDER:
              suffix = countObjs == 1 ? com.CH_gui.lang.Lang.rb.getString("Message") : com.CH_gui.lang.Lang.rb.getString("Messages");
              break;
            case FolderRecord.POSTING_FOLDER:
              suffix = countObjs == 1 ? com.CH_gui.lang.Lang.rb.getString("Posting") : com.CH_gui.lang.Lang.rb.getString("Postings");
              break;
            case FolderRecord.ADDRESS_FOLDER:
              suffix = countObjs == 1 ? com.CH_gui.lang.Lang.rb.getString("Address") : com.CH_gui.lang.Lang.rb.getString("Addresses");
              break;
            case FolderRecord.CONTACT_FOLDER:
              suffix = countObjs == 1 ? com.CH_gui.lang.Lang.rb.getString("Contact") : com.CH_gui.lang.Lang.rb.getString("Contacts");
              break;
            case FolderRecord.KEY_FOLDER:
              suffix = countObjs == 1 ? com.CH_gui.lang.Lang.rb.getString("Key") : com.CH_gui.lang.Lang.rb.getString("Keys");
              break;
          }

          jContains.setText(java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("NUM_ITEM(S)"), new Object[] {data.objId_1.toString(), suffix}));
           */

          jContains.setText(sb.toString());
          String oSize = "";
          String dSize = "";
          if (size > 0) {
            oSize = Misc.getFormattedSize(size, 3, 2);
            if (size >= 1000)
              oSize += " (" + Misc.getFormattedSize(size, 10, 10) + ")";
          }
          if (sizeOnDisk > 0) {
            dSize = Misc.getFormattedSize(sizeOnDisk, 3, 2);
            if (sizeOnDisk >= 1000)
              dSize += " (" + Misc.getFormattedSize(sizeOnDisk, 10, 10) + ")";
          }
          jSize.setText(oSize);
          jSizeOnDisk.setText(dSize);
        }

      }
    };
    th.setDaemon(true);
    th.start();
  }


  public void closeDialog() {
    if (documentChangeListener != null) {
      if (jFolderName != null)
        jFolderName.getDocument().removeDocumentListener(documentChangeListener);
      if (jFolderDesc != null)
        jFolderDesc.getDocument().removeDocumentListener(documentChangeListener);
      if (folderSharingPanel.jShareName != null)
        folderSharingPanel.jShareName.getDocument().removeDocumentListener(documentChangeListener);
      documentChangeListener = null;
    }
    if (folderSharingPanel != null)
      folderSharingPanel.disposeObj();
    super.closeDialog();
  }


  private class DocumentChangeListener implements DocumentListener {
    public void changedUpdate(DocumentEvent e) {
      setEnabledButtons();
    }
    public void insertUpdate(DocumentEvent e) {
      setEnabledButtons();
    }
    public void removeUpdate(DocumentEvent e) {
      setEnabledButtons();
    }
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "FolderPropertiesDialog";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
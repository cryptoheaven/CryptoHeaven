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

package com.CH_gui.dialog;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.DefaultReplyRunner;
import com.CH_cl.service.ops.FolderOps;
import com.CH_cl.service.records.filters.FolderFilter;
import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.fld.Fld_NewFld_Rq;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.service.records.FolderPair;
import com.CH_co.service.records.FolderRecord;
import com.CH_co.service.records.FolderShareRecord;
import com.CH_co.service.records.MemberContactRecordI;
import com.CH_co.service.records.filters.RecordFilter;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_co.tree.FolderTreeNode;
import com.CH_co.util.ImageNums;
import com.CH_gui.folder.FolderPurgingPanel;
import com.CH_gui.folder.FolderSharingPanel;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.gui.*;
import com.CH_gui.tree.FolderTree;
import com.CH_gui.tree.FolderTreeComponent;
import com.CH_gui.tree.FolderTreeModelGui;
import com.CH_gui.tree.FolderTreeSelectionExpansion;
import com.CH_gui.util.GeneralDialog;
import com.CH_gui.util.Images;
import com.CH_gui.util.MessageDialog;
import com.CH_gui.util.VisualsSavable;
import com.CH_guiLib.gui.JMyComboBox;
import com.CH_guiLib.gui.JMyTextField;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreePath;

/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *  This is a dialog that can serve two menu items: Create New Folder and Move Folder
 *  It displays a dialog where a user can enter necessary info and select the tree folder
 *  to which to add or move a folder
 *
 * Class Details:
 * It uses the tree model it gets to filter its tree to selected folder type.
 * The types are the same as in FolderRecord and in the same order.
 *
 * <b>$Revision: 1.35 $</b>
 * @author  Marcin Kurzawa
 * @version
 */

public class Move_NewFld_Dialog extends GeneralDialog implements VisualsSavable {

  private static final int TAB_GENERAL = 0;
  private static final int TAB_SHARING = 1;
  private static final int TAB_PURGING = 2;

  private static final boolean MONO_FOLDERS = false; // Mode where a folder tree may consist of a single folder type, only the roots may be different.
  public static final short DEFAULT_CHAT_PURGING_RECORD_NUM = 100; // For Chatting folders, by default purge records if more than 100;
  public static final int DEFAULT_CHAT_PURGING_RECORD_SECONDS = 0; // unlimited, old value was 60*60*24*7*8;  // For Chatting folders, by default purge records if older than 8 weeks;

  FolderTreeModelGui treeModel = null;
  FolderPair        selectedFolderPair = null;
  FolderPair[]      forbidenFolderPairs;
  boolean           isNewFolder = false;
  boolean           isChooseDestination = false;
  boolean           isDescendantOk = false;
  RecordFilter      folderFilter = null;   // an overwrite to the default folder filter used when selecting destination for messages/postings
  int               mode; // mode of operation
  short             newFolderType;

  private static final int MODE__NEW_OR_MOVE_FOLDER = 1;
  private static final int MODE__COPY_OR_MOVE_FILES = 2;
  private static final int MODE__COPY_OR_MOVE_MESSAGES_OR_POSTINGS = 3;

  private static final int DEFAULT_BUTTON_INDEX = 0;
  private static final int DEFAULT_CANCEL_BUTTON_INDEX = 1;
  private static final String FLD_NAME_EMPTY = com.CH_cl.lang.Lang.rb.getString("msg_Folder_Name_must_be_at_least_one_character_long");
  private static final String CHATTING_FOLDER_INVALID_INPUT = com.CH_cl.lang.Lang.rb.getString("msg_To_create_a_Chatting_Folder...");
  private static final String SHARE_FOLDER_NAME_EMPTY = com.CH_cl.lang.Lang.rb.getString("msg_Please_assign_a_folder_share_name.");

  FetchedDataCache  cache = null;
  Fld_NewFld_Rq     newFolderRequest;   // create new folder request
  Obj_List_Co       moveFolderRequest;  // move folder request
  FolderPair        chosenFolderPair;   // move files to chosed folder

  /* Components that will be placed in the main panel */
  JButton           okButton = null;
  JComboBox         jFolderType = null;
  JTextField        jFolderName = null;
  JTextArea         jFolderDesc = null;
  JLabel            jTreeLabel = null;
  JScrollPane       treeScrollPane = null;
  FolderTree        filteredTree = null;
  JTabbedPane       tabbedPane = null;

  // When creating a new folder, we will need a brand new symmetric key.
  private BASymmetricKey baSymmetricKey = null;
  private FolderSharingPanel folderSharingPanel = null;
  private FolderPurgingPanel folderPurgingPanel = null;

  private DocumentChangeListener documentChangeListener;
  private FolderTypeActionListener folderTypeActionListener;

  private boolean isOkNextAction = false;

  /**
   * Creates new  Move_NewFld_Dialog for moving a single folder or creating a new folder.
   */
  public Move_NewFld_Dialog(Dialog owner, FolderTreeModelGui treeModel, FolderPair selectedFolderPair, String title, boolean isNewFolder, short newFolderType, FetchedDataCache cache, MemberContactRecordI[] addInitialContacts) {
    super(owner, title);
    // if MOVE folder action
    if (!isNewFolder)
      this.forbidenFolderPairs = new FolderPair[] { selectedFolderPair };
    if (isNewFolder)
      this.newFolderType = newFolderType;
    mode = MODE__NEW_OR_MOVE_FOLDER;
    constructDialog(owner, treeModel, selectedFolderPair, isNewFolder, cache, addInitialContacts);
  }
  /**
   * Creates new  Move_NewFld_Dialog for moving a single folder or creating a new folder.
   */
  public Move_NewFld_Dialog(Frame owner, FolderTreeModelGui treeModel, FolderPair selectedFolderPair, String title, boolean isNewFolder, short newFolderType, FetchedDataCache cache, MemberContactRecordI[] addInitialContacts) {
    super(owner, title);
    // if MOVE folder action
    if (!isNewFolder)
      this.forbidenFolderPairs = new FolderPair[] { selectedFolderPair };
    if (isNewFolder)
      this.newFolderType = newFolderType;
    mode = MODE__NEW_OR_MOVE_FOLDER;
    constructDialog(owner, treeModel, selectedFolderPair, isNewFolder, cache, addInitialContacts);
  }


  /**
   * Creates new  Move_NewFld_Dialog for moving an array of files and/or folders.
   */
  public Move_NewFld_Dialog(Dialog owner, FolderPair[] folderPairs, FolderPair[] forbidenFolderPairs, FolderPair selectedFolderPair, String title, boolean isDescendantOk, FetchedDataCache cache) {
    super(owner, title);
    mode = MODE__COPY_OR_MOVE_FILES;
    createMoveFilesDialog(owner, folderPairs, forbidenFolderPairs, selectedFolderPair, title, isDescendantOk, cache);
  }
  public Move_NewFld_Dialog(Frame owner, FolderPair[] folderPairs, FolderPair[] forbidenFolderPairs, FolderPair selectedFolderPair, String title, boolean isDescendantOk, FetchedDataCache cache) {
    super(owner, title);
    mode = MODE__COPY_OR_MOVE_FILES;
    createMoveFilesDialog(owner, folderPairs, forbidenFolderPairs, selectedFolderPair, title, isDescendantOk, cache);
  }


  /**
   * Creates new  Move_NewFld_Dialog for moving an array of messages and/or postings.
   * @param folderFilter the priority folder filter to be used instead of the default
   */
  public Move_NewFld_Dialog(Dialog owner, FolderPair[] folderPairs, FolderPair[] forbidenFolderPairs, FolderPair selectedFolderPair, String title, boolean isDescendantOk, FetchedDataCache cache, RecordFilter folderFilter) {
    super(owner, title);
    this.folderFilter = folderFilter;
    mode = MODE__COPY_OR_MOVE_MESSAGES_OR_POSTINGS;
    createMoveFilesDialog(owner, folderPairs, forbidenFolderPairs, selectedFolderPair, title, isDescendantOk, cache);
  }
  public Move_NewFld_Dialog(Frame owner, FolderPair[] folderPairs, FolderPair[] forbidenFolderPairs, FolderPair selectedFolderPair, String title, boolean isDescendantOk, FetchedDataCache cache, RecordFilter folderFilter) {
    super(owner, title);
    this.folderFilter = folderFilter;
    mode = MODE__COPY_OR_MOVE_MESSAGES_OR_POSTINGS;
    createMoveFilesDialog(owner, folderPairs, forbidenFolderPairs, selectedFolderPair, title, isDescendantOk, cache);
  }

  private void createMoveFilesDialog(Component owner, FolderPair[] folderPairs, FolderPair[] forbidenFolderPairs, FolderPair selectedFolderPair, String title, boolean isDescendantOk, FetchedDataCache cache) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Move_NewFld_Dialog.class, "createMoveFilesDialog()");

    FolderTreeModelGui treeModel = new FolderTreeModelGui();
    treeModel.addNodes(folderPairs);
    this.isChooseDestination = true;
    this.isDescendantOk = isDescendantOk;
    this.forbidenFolderPairs = forbidenFolderPairs;

    constructDialog(owner, treeModel, selectedFolderPair, false, cache, null);
    if (trace != null) trace.exit(Move_NewFld_Dialog.class);
  }
  private void constructDialog(Component owner, FolderTreeModelGui treeModel, FolderPair selectedFolderPair, boolean isNewFolder, FetchedDataCache cache, MemberContactRecordI[] addInitialContacts) {
    this.treeModel = treeModel;
    // Expand/select the tree to the first forbiden folder, if one is specified.
    if (selectedFolderPair == null)
      selectedFolderPair = forbidenFolderPairs != null ? forbidenFolderPairs[0] : null;
    if (selectedFolderPair != null)
      selectedFolderPair = treeModel.findNode(selectedFolderPair.getId(), true) != null ? selectedFolderPair : null;
    this.selectedFolderPair = selectedFolderPair;
    this.isNewFolder = isNewFolder;
    this.cache = cache;

    if (isNewFolder)
      baSymmetricKey = new BASymmetricKey(32);

    JButton[] buttons = createButtons();

    initPanelComponents(owner, addInitialContacts);
    JComponent mainComponent = createMainComponent();

    this.setModal(isChooseDestination);
    super.init(owner, buttons, mainComponent, DEFAULT_BUTTON_INDEX, DEFAULT_CANCEL_BUTTON_INDEX);
    TreePath selectedPath = filteredTree.getSelectionPath();
    if (selectedPath != null)
      filteredTree.scrollPathToVisible(selectedPath);
  }

  /* @return a request to create a new folder */
  public Fld_NewFld_Rq getNewFolderRequest() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Move_NewFld_Dialog.class, "getNewFolderRequest()");
    if (trace != null) trace.exit(Move_NewFld_Dialog.class, newFolderRequest);
    return newFolderRequest;
  }
  /* @return a request to move a folder to some other parent folder */
  public Obj_List_Co getMoveFolderRequest() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Move_NewFld_Dialog.class, "getMoveFolderRequest()");
    if (trace != null) trace.exit(Move_NewFld_Dialog.class, moveFolderRequest);
    return moveFolderRequest;
  }
  /* @return a chosen destination folder pair */
  public FolderPair getChosenDestination() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Move_NewFld_Dialog.class, "getChosenDestination()");
    if (trace != null) trace.exit(Move_NewFld_Dialog.class, chosenFolderPair);
    return chosenFolderPair;
  }

  /** Initialize all components that will be placed in the main panel
    * taking into consideration whether it is a new folder or move folder dialog
    */
  private void initPanelComponents(Component owner, MemberContactRecordI[] addInitialContacts){
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Move_NewFld_Dialog.class, "initPanelComponents(owner, addInitialContacts)");
    if (trace != null) trace.args(owner, addInitialContacts);

    /** index corresponds to assignment of folderTypes in FolderRecord file */
    /** i.e. index+1 = folderType */
    Short folderType = getFolderType();

    if (isChooseDestination) {
      jTreeLabel = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Select_a_folder"));
    } else {

      jFolderType = new JMyComboBox(new FolderTypeComboBoxModel());

      if (isNewFolder) {
        String folderName = newFolderType == FolderRecord.GROUP_FOLDER ? com.CH_cl.lang.Lang.rb.getString("folder_Group_Name") : com.CH_cl.lang.Lang.rb.getString("folder_Folder_Name");
        jFolderName = new JMyTextField(folderName);
        jFolderName.selectAll();
        documentChangeListener = new DocumentChangeListener();
        jFolderName.getDocument().addDocumentListener(documentChangeListener);

        jFolderDesc = new JMyTextArea(3, 20);

        folderTypeActionListener = new FolderTypeActionListener();
        jFolderType.addActionListener(folderTypeActionListener);

        jTreeLabel = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Select_a_folder_in_which_to_create_the_new_folder"));

        // sharing
        folderSharingPanel = new FolderSharingPanel(baSymmetricKey, jFolderName, jFolderDesc, addInitialContacts);
        // auto purging
        folderPurgingPanel = new FolderPurgingPanel();
      } else {
        jFolderType.setEnabled(false);
        jTreeLabel = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Select_a_folder_to_which_the_folder_should_be_moved"));
        jFolderName = new JMyTextField(selectedFolderPair.getFolderShareRecord().getFolderName());
        jFolderName.setEnabled(false);
        jFolderDesc = new JMyTextArea(selectedFolderPair.getFolderShareRecord().getFolderDesc());
        jFolderDesc.setEnabled(false);
      }
      jFolderDesc.setWrapStyleWord(true);
      jFolderDesc.setLineWrap(true);

      // initialize the preselected folder type
      jFolderType.setRenderer(new ComboBoxRenderer());
      {
        short fType = folderType.shortValue();
        // allow only types: FILE, MESSAGE, POSTING and special CHATTING folder, ADDRESS, GROUP
        switch (fType) {
          case FolderRecord.FILE_FOLDER:
          case FolderRecord.MESSAGE_FOLDER:
          case FolderRecord.POSTING_FOLDER:
          case FolderRecord.CHATTING_FOLDER:
          case FolderRecord.ADDRESS_FOLDER:
          case FolderRecord.GROUP_FOLDER:
            break;
          default:
            fType = FolderRecord.FILE_FOLDER;
        }
        // convert old chatting type to new explicit type
        if (selectedFolderPair != null && selectedFolderPair.getFolderRecord() != null && selectedFolderPair.getFolderRecord().isChatting()) {
          fType = FolderRecord.CHATTING_FOLDER;
        }
        if (newFolderType != 0) {
          setSelectedType(newFolderType);
        } else {
          setSelectedType(fType);
        }
      }
      setFolderType(getSelectedFolderType());

    }
    if (jFolderName != null) {
      jFolderName.selectAll();
      jFolderName.addHierarchyListener(new InitialFocusRequestor());
    }

    // Display a filtered tree, select and expand same node as in original tree.
    // Use folder filter according to the mode of operation...
    RecordFilter filter = null;
    if (MONO_FOLDERS) {
      if (mode == MODE__COPY_OR_MOVE_MESSAGES_OR_POSTINGS)
        filter = folderFilter;
      else if (mode == MODE__COPY_OR_MOVE_FILES)
        filter = new FolderFilter(folderType.shortValue());
    }
    if (mode == MODE__NEW_OR_MOVE_FOLDER) {
      if (MONO_FOLDERS)
        filter = new FolderFilter(folderType.shortValue(), cache.getMyUserId());
      else {
        // Enable move and creation of new folders under other peoples folders, not just my own.
        //filter = FolderFilter.NON_LOCAL_FOLDERS.cloneAndKeepOwner(cache.getMyUserId());
        filter = FolderFilter.NON_LOCAL_FOLDERS;
      }
    }
    filteredTree = new FolderTree((FolderTreeModelGui) treeModel.createFilteredModel(filter, new FolderTreeModelGui()));

    MainFrame mainFrame = MainFrame.getSingleInstance();
    if (mainFrame != null) {
      FolderTreeComponent mainTreeComp = mainFrame.getMainTreeComponent(owner);
      if (mainTreeComp != null) {
        FolderTree fTree = mainTreeComp.getFolderTreeScrollPane().getFolderTree();
        FolderTreeSelectionExpansion selectionExpansion = FolderTreeSelectionExpansion.getData(fTree);
        selectionExpansion.restoreData(filteredTree);
      }
    }

    treeScrollPane = new JScrollPane(filteredTree);
    // expand current selection sub-folders too
    if (selectedFolderPair == null) {
      filteredTree.expandRow(0);
      filteredTree.setSelectionRow(0);
    }
    else {
      TreePath treePath = filteredTree.getFolderTreeModel().getPathToRoot(selectedFolderPair);
      if (treePath != null) {
        filteredTree.expandPath(treePath);
        filteredTree.setSelectionPath(treePath);
      }
    }

    if (trace != null) trace.exit(Move_NewFld_Dialog.class);
  }

  private JComponent createMainComponent() {
    JComponent mainComponent = null;
    if (isNewFolder) {
      tabbedPane = new JMyTabbedPane();
      tabbedPane.addTab(com.CH_cl.lang.Lang.rb.getString("tab_General"), createMainPanel());
      tabbedPane.addTab(com.CH_cl.lang.Lang.rb.getString("tab_Sharing"), folderSharingPanel);
      tabbedPane.addTab(com.CH_cl.lang.Lang.rb.getString("tab_Purging"), folderPurgingPanel);
      tabbedPane.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (isOkNextAction && tabbedPane.getSelectedIndex() == 1) {
            isOkNextAction = false;
            okButton.setText(com.CH_cl.lang.Lang.rb.getString("button_OK"));
          }
        }
      });
      mainComponent = tabbedPane;
    }
    else
      mainComponent = createMainPanel();
    return mainComponent;
  }

  /** Create and set panel with components initialized earlier in setPanelComponents() method
    * using GridBagLayout and using global variables
    */
  private JPanel createMainPanel() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Move_NewFld_Dialog.class, "createMainPanel()");

    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    int posY = 0;
    if (jFolderName != null) {
      panel.add(new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Name")), new GridBagConstraints(0, posY, 1, 1, 5, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

      panel.add(jFolderName, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;
    }

    if (jFolderDesc != null) {
      panel.add(new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Comment")), new GridBagConstraints(0, posY, 1, 1, 5, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

      panel.add(new JScrollPane(jFolderDesc), new GridBagConstraints(1, posY, 1, 3, 10, 2,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
      posY += 3;
    }

    if (jFolderType != null) {
      panel.add(new JLabel (com.CH_cl.lang.Lang.rb.getString("label_Type")), new GridBagConstraints(0, posY, 1, 1, 5, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

      panel.add(jFolderType, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;
    }

    if (jTreeLabel != null) {
      panel.add(jTreeLabel, new GridBagConstraints(0, posY, 2, 1, 15, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;
    }

    if (treeScrollPane != null) {
      panel.add(treeScrollPane, new GridBagConstraints(0, posY, 2, 1, 15, 10,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;
    }

    if (trace != null) trace.exit(Move_NewFld_Dialog.class, panel);
    return panel;
  }

  /** @return a folderType that corresponds to the ones in a FolderRecord file
    * folderType will be the type of the selected folder or if selected folder is null
    * the default is "File" type
    */
  private Short getFolderType() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Move_NewFld_Dialog.class, "getFolderType()");
    Short folderType = new Short((short)1);             /* default file type*/
    if (selectedFolderPair != null) {
      folderType = selectedFolderPair.getFolderRecord().folderType;
    }
    if (trace != null) trace.exit(Move_NewFld_Dialog.class, folderType);
    return folderType;
  }

  private JButton[] createButtons() {

    JButton[] buttons = new JButton[2];
    if (isNewFolder) {
      isOkNextAction = true;
      okButton = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Next"));
    } else {
      okButton = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_OK"));
    }
    okButton.setDefaultCapable(true);
    okButton.addActionListener(new OKActionListener());

    buttons[0] = okButton;

    JButton cancelButton = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Cancel"));
    cancelButton.addActionListener(new CancelActionListener());
    buttons[1] = cancelButton;

    return buttons;
  }

  private boolean isDestinationForbiden(FolderTreeNode destinationNode) {
    boolean isForbiden = false;
    if (forbidenFolderPairs != null) {
      for (int i=0; i<forbidenFolderPairs.length; i++) {
        FolderTreeNode node = filteredTree.getFolderTreeModel().findNode(forbidenFolderPairs[i].getId(), true);
        if (node != null) {
          // If we are looking for descendants also...
          if (!isDescendantOk && node.isNodeDescendant(destinationNode)) {
            isForbiden = true;
            break;

          // Else, just check the current node, not the descendants.
          } else if (node.equals(destinationNode)) {
            // Just check the current node, not the descendants.
            isForbiden = true;
            break;
          }
        }
      }
    }
    return isForbiden;
  }

  /**
   * Gather data from panel components to set a chosen destination folder.
   * @return true for success.
   **/
  private boolean setChosenDestination() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Move_NewFld_Dialog.class, "setChosenDestination()");

    boolean success = false;

    FolderTreeNode destinationNode = (FolderTreeNode) filteredTree.getLastSelectedPathComponent();

    // if destinationNode cannot be descendant of forbidenSubtrees, if so, show an error dialog
    if (isDestinationForbiden(destinationNode)) {
      MessageDialog.showErrorDialog(this, com.CH_cl.lang.Lang.rb.getString("msg_Selected_destination_folder_is_not_a_valid_choice."), com.CH_cl.lang.Lang.rb.getString("title_Invalid_Input"));
    } else {
      if (destinationNode != null)
        chosenFolderPair = destinationNode.getFolderObject();
      if (chosenFolderPair.getFolderRecord().isCategoryType())
        chosenFolderPair = null;
      // if moving to root
      if (chosenFolderPair == null) {
        JPanel msgPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        msgPanel.add(new JMyLabel(com.CH_cl.lang.Lang.rb.getString("msg_Category_folder_is_an_invalid_choice.")));
        msgPanel.add(new JMyLabel(com.CH_cl.lang.Lang.rb.getString("msg_Please_select_another_folder.")));
        MessageDialog.showInfoDialog(this, msgPanel, com.CH_cl.lang.Lang.rb.getString("title_Invalid_Input"), false);
      } else {
        success = true;
      }
    }

    if (trace != null) trace.exit(Move_NewFld_Dialog.class, success);
    return success;
  }

  private void setFolderType(short folderType) {
    if (folderSharingPanel != null) {
      folderSharingPanel.setFolderType(folderType);
    }
    if (tabbedPane != null) {
      if (folderType == FolderRecord.GROUP_FOLDER) {
        tabbedPane.setTitleAt(TAB_SHARING, com.CH_cl.lang.Lang.rb.getString("tab_Members"));
      } else {
        tabbedPane.setTitleAt(TAB_SHARING, com.CH_cl.lang.Lang.rb.getString("tab_Sharing"));
      }
      if (folderType == FolderRecord.GROUP_FOLDER || FolderRecord.isAddressType(folderType)) {
        tabbedPane.setEnabledAt(TAB_PURGING, false);
      } else {
        tabbedPane.setEnabledAt(TAB_PURGING, true);
      }
    }

    if (folderType == FolderRecord.CHATTING_FOLDER) {
      if (folderPurgingPanel != null) {
        Short numToKeep = folderPurgingPanel.folderAttributesPanel.getNumToKeep();
        if (numToKeep == null || numToKeep.shortValue() == 0) {
          folderPurgingPanel.folderAttributesPanel.setNumToKeep(new Short(DEFAULT_CHAT_PURGING_RECORD_NUM));
        }
        Integer keepAsOldAs = folderPurgingPanel.folderAttributesPanel.getKeepAsOldAs();
        if (keepAsOldAs == null || keepAsOldAs.intValue() == 0) {
          folderPurgingPanel.folderAttributesPanel.setKeepAsOldAs(new Integer(DEFAULT_CHAT_PURGING_RECORD_SECONDS));
        }
      }
    } else {
      if (folderPurgingPanel != null) {
        folderPurgingPanel.folderAttributesPanel.disableAttributesIfNotUsed();
      }
    }
  }

  /**
   * Gather data from panel components to set a request to create a new folder
   * @return true for success.
   **/
  private boolean setNewFolderRequest() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Move_NewFld_Dialog.class, "setNewFolderRequest()");

    boolean success = false;
    Short numToKeep = folderPurgingPanel.folderAttributesPanel.getNumToKeep();
    Integer keepAsOldAs = folderPurgingPanel.folderAttributesPanel.getKeepAsOldAs();

    if (jFolderName.getText() == null || jFolderName.getText().trim().length() == 0) {
      MessageDialog.showErrorDialog(this, FLD_NAME_EMPTY, com.CH_cl.lang.Lang.rb.getString("title_Invalid_Input"));
      jFolderName.requestFocusInWindow();
    } else if (folderSharingPanel.shareTableModel.getRowCount() > 0 && folderSharingPanel.jShareName.getText().length() == 0) {
      MessageDialog.showErrorDialog(this, SHARE_FOLDER_NAME_EMPTY, com.CH_cl.lang.Lang.rb.getString("title_Invalid_Input"));
    } else if (
              isSelectedChatting() &&
              (numToKeep == null || numToKeep.shortValue() == 0) &&
              (keepAsOldAs == null || keepAsOldAs.intValue() == 0)
              )
    {
      MessageDialog.showErrorDialog(this, CHATTING_FOLDER_INVALID_INPUT, com.CH_cl.lang.Lang.rb.getString("title_Invalid_Input"));
    } else if (
              isSelectedChatting() &&
              folderSharingPanel.shareTableModel.getRowCount() == 0
            )
    {
      MessageDialog.showErrorDialog(this, CHATTING_FOLDER_INVALID_INPUT, com.CH_cl.lang.Lang.rb.getString("title_Invalid_Input"));
    } else {

      FolderShareRecord[] additionalShares = null;
      if (folderSharingPanel.jRadioDoShare.isSelected() && folderSharingPanel.shareTableModel.getRowCount() > 0) {
        additionalShares = new FolderShareRecord[folderSharingPanel.shareTableModel.getRowCount()];
        for (int i=0; i<additionalShares.length; i++)
          additionalShares[i] = (FolderShareRecord) folderSharingPanel.shareTableModel.getRowObject(i);
      }

      String folderName = jFolderName.getText().trim();
      String folderDesc = jFolderDesc.getText().trim();
      boolean useInheritedSharing = folderSharingPanel.jRadioInheritSharing.isSelected();
      String shareName = useInheritedSharing ? folderName : folderSharingPanel.jShareName.getText().trim();
      String shareDesc = useInheritedSharing ? folderDesc : folderSharingPanel.jShareDesc.getText().trim();

      FolderPair parentFolder = filteredTree.getLastSelectedPair();
      if (parentFolder != null) {
        if (parentFolder.getFolderRecord().isCategoryType())
          parentFolder = null;
      }

      newFolderRequest = FolderOps.createNewFldRq
          (
          parentFolder,
          getSelectedFolderType(),
          folderName, folderDesc,
          shareName, shareDesc,
          numToKeep,
          keepAsOldAs,
          baSymmetricKey,
          useInheritedSharing,
          additionalShares,
          MainFrame.getServerInterfaceLayer()
          );
      success = true;
    }

    if (trace != null) trace.exit(Move_NewFld_Dialog.class, success);
    return success;
  }

  /**
   * Gather data from panel compnents to set a request to move a folder
   * @return true for success.
   **/
  private boolean setMoveFolderRequest() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Move_NewFld_Dialog.class, "setMoveFolderRequest()");

    boolean success = false;

    Long folderId = selectedFolderPair.getFolderRecord().getId();
    Long folderShareId = selectedFolderPair.getFolderShareRecord().getId();
    FolderTreeNode destinationNode = (FolderTreeNode) filteredTree.getLastSelectedPathComponent();

    // if destinationNode cannot be descendant of selectedNode, if so, show an error dialog
    if (isDestinationForbiden(destinationNode)) {
      MessageDialog.showErrorDialog(this, com.CH_cl.lang.Lang.rb.getString("msg_Destination_folder_cannot_be_the_one_being_moved_or_one_of_its_descendants."), com.CH_cl.lang.Lang.rb.getString("title_Invalid_Input"));
    } else {
      Long newParentId = null;
      Long newParentShareId = null;

      FolderPair destinationFolder = destinationNode.getFolderObject();
      // if moving to root
      if (destinationFolder == null || destinationFolder.getFolderRecord().isCategoryType()) {
        newParentId = folderId;
        newParentShareId = folderShareId;
      } else {
        newParentId = destinationFolder.getFolderRecord().getId();
        newParentShareId = destinationFolder.getFolderShareRecord().getId();
      }

      moveFolderRequest = new Obj_List_Co();
      moveFolderRequest.objs = new Long[4];
      moveFolderRequest.objs[0] = folderId;
      moveFolderRequest.objs[1] = folderShareId;
      moveFolderRequest.objs[2] = newParentId;
      moveFolderRequest.objs[3] = newParentShareId;

      success = true;
    }

    if (trace != null) trace.exit(Move_NewFld_Dialog.class, success);
    return success;
  }


  public void closeDialog() {
    if (baSymmetricKey != null)
      baSymmetricKey.clearContent();
    if (documentChangeListener != null && jFolderName != null) {
      jFolderName.getDocument().removeDocumentListener(documentChangeListener);
      documentChangeListener = null;
    }
    if (folderTypeActionListener != null && jFolderType != null) {
      jFolderType.removeActionListener(folderTypeActionListener);
      folderTypeActionListener = null;
    }
    super.closeDialog();
  }



  /*****************************************************************************************************/
  /************************* INNER CLASSES : ACTION LISTENERS ******************************************/
  /*****************************************************************************************************/


  /** Set the appropriate request and close the window **/
  private class OKActionListener implements ActionListener {
    public void actionPerformed (ActionEvent event) {
      if (isOkNextAction) {
        isOkNextAction = false;
        okButton.setText(com.CH_cl.lang.Lang.rb.getString("button_OK"));
        tabbedPane.setSelectedIndex(1);
      } else {
        // seperate AWT Thread from any potention network request in set request methods
        Thread th = new ThreadTraced("Folder Creator") {
          public void runTraced() {
            boolean canClose = false;
            if (isChooseDestination) {
              canClose = setChosenDestination();
            } else if (isNewFolder) {
              canClose = setNewFolderRequest();
            } else {
              canClose = setMoveFolderRequest();
            }

            if (canClose) {
              Move_NewFld_Dialog.this.setVisible(false);
              ClientMessageAction replyAction = null;
              try {
                if (!isChooseDestination) {
                  if (isNewFolder) {
                    Fld_NewFld_Rq requestNew = getNewFolderRequest();
                    if (requestNew != null) {
                      MessageAction msgAction = new MessageAction(CommandCodes.FLD_Q_NEW_FOLDER, requestNew);
                      replyAction = MainFrame.getServerInterfaceLayer().submitAndFetchReply(msgAction, 30000);
                    }
                  } else {
                    Obj_List_Co requestMove = getMoveFolderRequest();
                    if (requestMove != null) {
                      MessageAction msgAction = new MessageAction(CommandCodes.FLD_Q_MOVE_FOLDER, requestMove);
                      replyAction = MainFrame.getServerInterfaceLayer().submitAndFetchReply(msgAction, 30000);
                    }
                  }
                  DefaultReplyRunner.nonThreadedRun(MainFrame.getServerInterfaceLayer(), replyAction);
                }
              } catch (Throwable t) {
              }
              if (isChooseDestination || (replyAction != null && replyAction.getActionCode() == CommandCodes.FLD_A_GET_FOLDERS))
                closeDialog();
              else
                Move_NewFld_Dialog.this.setVisible(true);
            } // end canClose
          } // end run()
        };
        th.setDaemon(true);
        th.start();
      }
    } // end actionPerformed()
  } // end class OKActionListener


  /** "Cancel" pressed don't do anything, just close the dialog **/
  private class CancelActionListener implements ActionListener {
    public void actionPerformed (ActionEvent event) {
      closeDialog();
    }
  }

  /** Listen on combo box, display a tree with folderType currently selected in combo box **/
  private class FolderTypeActionListener implements ActionListener {
    public void actionPerformed (ActionEvent event) {
      if (MONO_FOLDERS) {
        short newType = getSelectedFolderType();
        FolderTreeModelGui newModel = (FolderTreeModelGui) treeModel.createFilteredModel(new FolderFilter(newType), new FolderTreeModelGui());
        filteredTree.setModel(newModel);
      }
      setFolderType(getSelectedFolderType());
    }
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

  private void setEnabledButtons() {
    okButton.setEnabled(jFolderName.getText().trim().length() > 0);
  }



  private short getSelectedFolderType() {
    return FolderTypeComboBoxModel.types[jFolderType.getSelectedIndex()];
  }
  private boolean isSelectedChatting() {
    return getSelectedFolderType() == FolderRecord.CHATTING_FOLDER;
  }
  private void setSelectedType(short type) {
    short[] types = FolderTypeComboBoxModel.types;
    for (int i=0; i<types.length; i++) {
      if (types[i] == type) {
        jFolderType.setSelectedIndex(i);
        break;
      }
    }
  }



  private static class FolderTypeComboBoxModel extends AbstractListModel implements ComboBoxModel {

    public static short[] types;

    Object currentValue;
    ImageIcon[] images;
    Hashtable[] cache;

    static {
      types = new short[6];
      types[0] = 1;
      types[1] = 2;
      types[2] = 3;
      types[3] = 10;
      types[4] = 7;
      types[5] = 8;
    }

    public FolderTypeComboBoxModel() {
      images = new ImageIcon[6];
      images[0] = Images.get(ImageNums.FLD_CLOSED16);
      images[1] = Images.get(ImageNums.FLD_MAIL_CLOSED16);
      images[2] = Images.get(ImageNums.FLD_MAIL_POST_CLOSED16);
      images[3] = Images.get(ImageNums.FLD_CHAT_CLOSED16);
      images[4] = Images.get(ImageNums.FLD_ADDR_CLOSED16);
      images[5] = Images.get(ImageNums.PEOPLE16);

      //images[3] = Images.get(ImageNums.FLD_CNT_CLOSED16);
      //images[4] = Images.get(ImageNums.FLD_KEY_CLOSED16);
      cache = new Hashtable[getSize()];
    }

    public void setSelectedItem(Object anObject) {
      currentValue = anObject;
      fireContentsChanged(this,-1,-1);
    }

    public Object getSelectedItem() {
      return currentValue;
    }

    public int getSize() {
      return images.length;
    }

    public Object getElementAt(int index) {
      if(cache[index] != null)
        return cache[index];
      else {
        String title = null;
        if (index == 3)
          title = FolderRecord.CHATTING_FOLDER_STR;
        else
          title = FolderRecord.getFolderType(types[index]);

        Hashtable result = new Hashtable();
        result.put("title",title);
        if (images[index] != null)
          result.put("image", images[index]);
        cache[index] = result;
        return result;
      }
    }
  } // end private class FolderTypeComboBoxModel

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "Move_NewFld_Dialog";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
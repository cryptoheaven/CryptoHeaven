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

package com.CH_gui.tree;

import com.CH_co.trace.*;
import com.CH_co.tree.*;
import com.CH_co.util.*;

import com.CH_co.service.msg.dataSets.fld.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.cache.event.*;
import com.CH_cl.service.records.filters.*;

import com.CH_gui.action.*;
import com.CH_gui.contactTable.*;
import com.CH_gui.dialog.*;
import com.CH_gui.fileTable.*;
import com.CH_gui.frame.*;
import com.CH_gui.gui.*;
import com.CH_gui.list.*;
import com.CH_gui.menuing.PopupMouseAdapter;
import com.CH_gui.msgTable.MsgActionTable;
import com.CH_gui.service.ops.*;
import com.CH_gui.table.*;
import com.CH_gui.usrs.UserGuiOps;
import com.CH_gui.util.*;

import com.CH_guiLib.gui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.TreePath;

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
 * <b>$Revision: 1.35 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FolderActionTree extends FolderTree implements ActionProducerI, DisposableObj {

  private Action[] actions;

  private static final int NEW_FOLDER_ACTION = 0;
  private static final int MOVE_FOLDER_ACTION = 1;
  private static final int DELETE_FOLDER_ACTION = 2;
  private static final int UPLOAD_ACTION = 3;
  private static final int DOWNLOAD_FOLDER_ACTION = 4;
  private static final int PROPERTIES_ACTION = 5;
  private static final int REFRESH_ACTION = 6;
  private static final int OPEN_IN_SEPERATE_WINDOW_ACTION = 7;
  private static final int EXPLORE_FOLDER_ACTION = 8;
  private static final int NEW_MSG_ACTION = 9;
  private static final int TRACE_PRIVILEGE_AND_HISTORY_ACTION = 10;
  private static final int TRANSFER_OWNERSHIP_ACTION = 11;
  public static final int INVITE_ACTION = 12;
  private static final int INVITE_POPUP_ACTION = 13;
  private static final int EMPTY_FOLDER_ACTION = 14;
  private static final int MSG_COMPOSE_ACTION = 15;

  private static final int NUM_ACTIONS = MSG_COMPOSE_ACTION + 1;

  private int leadingActionId = Actions.LEADING_ACTION_ID_FOLDER_ACTION_TREE;
  private int leadingMsgActionId = Actions.LEADING_ACTION_ID_MSG_ACTION_TABLE;

  // For listening on folder updates so we can act when new stuff comes.
  private FolderListener folderListener;
  // Keep history of update counts per folders to see in appropriate act on updates.
  private Hashtable folderUpdateHistoryHT;

  /** Creates new FolderActionTree */
  public FolderActionTree() {
    this(new FolderTreeModelCl());
  }
  /** Creates new FolderActionTree */
  public FolderActionTree(RecordFilter filter) {
    this(new FolderTreeModelCl(filter));
  }
  /** Creates new FolderActionTree */
  public FolderActionTree(RecordFilter filter, FolderPair[] initialFolderPairs) {
    this(new FolderTreeModelCl(filter, initialFolderPairs));
  }
  /** Creates new FolderActionTree */
  public FolderActionTree(FolderTreeModelCl treeModel) {
    super(treeModel);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderActionTree.class, "FolderActionTree()");

    initActions();
    //this.setEditable(true);  // disable editing in the tree
    //this.setCellEditor(new FolderTreeCellEditor(this));
    this.setInvokesStopCellEditing(true);
//    this.setShowsRootHandles(false);

    /** If right mouse button is clicked then the node is being selected
      * and the popup window shown
      */
    addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent mouseEvent) {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MouseAdapter.class, "mouseClicked(MouseEvent)");
        if (trace != null) trace.args(mouseEvent);
        if (trace != null) trace.data(10, "in FolderActionTree.class");
        boolean rightButton = SwingUtilities.isRightMouseButton(mouseEvent);
        boolean doubleClick = mouseEvent.getClickCount() == 2;
        if (rightButton || doubleClick) {
          Object source = mouseEvent.getSource();
          if (source instanceof FolderTree) {
            FolderTree treeSource = (FolderTree) source;
            int row = treeSource.getRowForLocation(mouseEvent.getX(), mouseEvent.getY());
            if (row >= 0) {
              if (!treeSource.isRowSelected(row))
                treeSource.setSelectionRow(row);
            }
            if (doubleClick) {
              Action doubleClickAction = getDoubleClickAction();
              if (doubleClickAction != null)
                doubleClickAction.actionPerformed(new ActionEvent(mouseEvent.getSource(), mouseEvent.getID(), "doubleClick"));
            }
          }
        }
//        { // expand selected root
//          Object source = mouseEvent.getSource();
//          if (source instanceof FolderTree) {
//            FolderTree treeSource = (FolderTree) source;
//            int row = treeSource.getRowForLocation(mouseEvent.getX(), mouseEvent.getY());
//            if (row >= 0) {
//              TreePath path = treeSource.getPathForRow(row);
//              if (path.getPathCount() <= 2) {
//                treeSource.expandPath(path);
//              }
////                if (!treeSource.hasBeenExpanded(path))
////                  treeSource.expandPath(path);
////                else if (doubleClick) {
////                  boolean isExpanded = treeSource.isExpanded(path);
////                  if (isExpanded)
////                    treeSource.collapsePath(path);
////                  else
////                    treeSource.expandPath(path);
////                }
////              }
//            }
//          }
//        }
        if (trace != null) trace.exit(MouseAdapter.class);
      }
    }); // end addMouseListener()

    /** If right mouse button is clicked then the popup is shown.  Adding popup listener after right-click listener
     will cause the popup be shown after selection changes take effect. */
    addMouseListener(new PopupMouseAdapter(this, this));

    // tree selection listener used to enable/disable certain actions.
    addTreeSelectionListener(new TreeSelectionListener() {
      /** Called whenever the value of the selection changes. */
      public void valueChanged(TreeSelectionEvent e) {
        setEnabledActions();
      }
    });

    // Register folder listener to act on folder updates (expand branches where new items become available).
    this.folderListener = new FolderListener();
    FetchedDataCache.getSingleInstance().addFolderRecordListener(folderListener);

    if (trace != null) trace.exit(FolderActionTree.class);
  }


  private void initActions() {
    actions = new Action[NUM_ACTIONS];
    actions[NEW_FOLDER_ACTION] = new NewFolderAction(leadingActionId + NEW_FOLDER_ACTION);
    actions[MSG_COMPOSE_ACTION] = new MsgComposeAction(leadingActionId + MSG_COMPOSE_ACTION);
    actions[MOVE_FOLDER_ACTION] = new MoveFolderAction(leadingActionId + MOVE_FOLDER_ACTION);
    actions[DELETE_FOLDER_ACTION] = new DeleteFolderAction(leadingActionId + DELETE_FOLDER_ACTION);
    actions[UPLOAD_ACTION] = new UploadAction(leadingActionId + UPLOAD_ACTION);
    //actions[RENAME_FOLDER_ACTION] = new RenameFolderAction(leadingActionId+4);
    actions[DOWNLOAD_FOLDER_ACTION] = new DownloadFolderAction(leadingActionId + DOWNLOAD_FOLDER_ACTION);
    actions[PROPERTIES_ACTION] = new PropertiesAction(leadingActionId + PROPERTIES_ACTION);
    actions[REFRESH_ACTION] = new RefreshAction(leadingActionId + REFRESH_ACTION);
    actions[OPEN_IN_SEPERATE_WINDOW_ACTION] = new OpenInSeperateWindowAction(leadingActionId + OPEN_IN_SEPERATE_WINDOW_ACTION);
    actions[EXPLORE_FOLDER_ACTION] = new ExploreFolderAction(leadingActionId + EXPLORE_FOLDER_ACTION);

    actions[NEW_MSG_ACTION] = new NewMsgAction(leadingMsgActionId + MsgActionTable.NEW_ACTION);

    actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION] = new TracePrivilegeAndHistoryAction(leadingActionId + TRACE_PRIVILEGE_AND_HISTORY_ACTION);
    actions[TRANSFER_OWNERSHIP_ACTION] = new TransferOwnershipAction(leadingActionId + TRANSFER_OWNERSHIP_ACTION);
    actions[INVITE_ACTION] = new InviteAction(leadingActionId + INVITE_ACTION);
    actions[INVITE_POPUP_ACTION] = new InvitePopupAction(leadingActionId + INVITE_POPUP_ACTION);

    actions[EMPTY_FOLDER_ACTION] = new EmptyFolderAction(leadingActionId + EMPTY_FOLDER_ACTION);
    setEnabledActions();
  }
  public Action getRefreshAction() {
    return actions[REFRESH_ACTION];
  }
  public Action getCloneAction() {
    return actions[OPEN_IN_SEPERATE_WINDOW_ACTION];
  }
  public Action getExploreAction() {
    return actions[EXPLORE_FOLDER_ACTION];
  }
  // Double Click used to activate ExploreAction, but it is pretty useless, right now it expands the tree as swing default...
  public Action getDoubleClickAction() {
    return null;
  }



  // =====================================================================
  // LISTENERS FOR THE MENU ITEMS
  // =====================================================================

  /** Display a dialog so the user can enter new folder's info and
    * selectect which folder of the tree will be the parent.
    * Submit Create New Folder request
    */
  private class NewFolderAction extends AbstractActionTraced {
    public NewFolderAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_New_Folder_..."), Images.get(ImageNums.FOLDER_NEW16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Create_New_Folder"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FOLDER_NEW24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_New_Folder"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      short newFolderType = 0;
      boolean isChatAction = false;
      FolderTreeNode folderNode = (FolderTreeNode) getLastSelectedPathComponent();
      if (folderNode != null) {
        FolderPair folderPair = folderNode.getFolderObject();
        if (folderPair != null) {
          switch (folderPair.getFolderRecord().folderType.shortValue()) {
            case FolderRecord.CATEGORY_MAIL_FOLDER:
              newFolderType = FolderRecord.MESSAGE_FOLDER;
              break;
            case FolderRecord.CATEGORY_FILE_FOLDER:
              newFolderType = FolderRecord.FILE_FOLDER;
              break;
            case FolderRecord.CATEGORY_CHAT_FOLDER:
              // do chat action
              isChatAction = true;
              newFolderType = -1;
              ContactActionTable.chatOrShareSpace(FolderActionTree.this, null, false, true, (short) 0);
              break;
            case FolderRecord.CATEGORY_GROUP_FOLDER:
              newFolderType = FolderRecord.GROUP_FOLDER;
              break;
          }
        }
      }
      if (!isChatAction) {
        Window w = SwingUtilities.windowForComponent(FolderActionTree.this);
        if (w instanceof Frame) new Move_NewFld_Dialog((Frame) w, getFolderTreeModel(), getLastSelectedPair(), com.CH_gui.lang.Lang.rb.getString("title_Create_New_Folder"), true, newFolderType, MainFrame.getServerInterfaceLayer().getFetchedDataCache(), null);
        else if (w instanceof Dialog) new Move_NewFld_Dialog((Dialog) w, getFolderTreeModel(), getLastSelectedPair(), com.CH_gui.lang.Lang.rb.getString("title_Create_New_Folder"), true, newFolderType, MainFrame.getServerInterfaceLayer().getFetchedDataCache(), null);
      }
    }
    private void updateIconAndText(FolderPair[] selectedFolderPairs) {
      if (selectedFolderPairs != null && selectedFolderPairs.length == 1) {
        if (selectedFolderPairs[0].getFolderRecord().folderType.shortValue() == FolderRecord.CATEGORY_GROUP_FOLDER ||
            selectedFolderPairs[0].getFolderRecord().folderType.shortValue() == FolderRecord.GROUP_FOLDER) {
          putValue(Actions.MENU_ICON, Images.get(ImageNums.GROUP_ADD16));
          putValue(Actions.TOOL_ICON, Images.get(ImageNums.GROUP_ADD24));
          putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Create_Group"));
          putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Create_Group"));
          putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Create_Group"));
        } else if (selectedFolderPairs[0].getFolderRecord().folderType.shortValue() == FolderRecord.CATEGORY_CHAT_FOLDER) {
          putValue(Actions.MENU_ICON, Images.get(ImageNums.CHAT16));
          putValue(Actions.TOOL_ICON, Images.get(ImageNums.CHAT24));
          putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Chat"));
          putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Chat"));
          putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Enter_chat_mode_with_selected_party."));
        } else {
          putValue(Actions.MENU_ICON, Images.get(ImageNums.FOLDER_NEW16));
          putValue(Actions.TOOL_ICON, Images.get(ImageNums.FOLDER_NEW24));
          putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_New_Folder"));
          putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_New_Folder_..."));
          putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Create_New_Folder"));
        }
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

  /** Display a dialog so the user can choose to which folder the move should be done.
    * Submit Move Folder request
    */
  private class MoveFolderAction extends AbstractActionTraced {
    public MoveFolderAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Move_Folder_..."), Images.get(ImageNums.FOLDER_MOVE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Move_folder_to_a_different_parent_folder"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FOLDER_MOVE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Move"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      Window w = SwingUtilities.windowForComponent(FolderActionTree.this);
      FolderTreeModelCl treeModel = (FolderTreeModelCl) FolderActionTree.this.getFolderTreeModel().createFilteredModel(FolderFilter.MOVE_FOLDER, new FolderTreeModelCl());
      if (w instanceof Frame) new Move_NewFld_Dialog((Frame) w, treeModel, getLastSelectedPair(), com.CH_gui.lang.Lang.rb.getString("title_Move_Folder"), false, (short) 0, MainFrame.getServerInterfaceLayer().getFetchedDataCache(), null);
      else if (w instanceof Dialog) new Move_NewFld_Dialog((Dialog) w, treeModel, getLastSelectedPair(), com.CH_gui.lang.Lang.rb.getString("title_Move_Folder"), false, (short) 0, MainFrame.getServerInterfaceLayer().getFetchedDataCache(), null);
    }
  }

  /**
   * Submit a Delete Folder request
   * Note: that this only works for single-selection of the nodes in the tree
  */
  private class DeleteFolderAction extends AbstractActionTraced {
    public DeleteFolderAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Delete_Folder_..."), Images.get(ImageNums.FOLDER_DELETE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Permanently_Delete_Folder_and_all_of_its_children_folders_and_contents."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FOLDER_DELETE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Delete"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      TreePath[] selectedPaths = getSelectionPaths();
      FolderPair[] folderPairs = getLastPathComponentFolderPairs(selectedPaths);

      if (folderPairs != null && folderPairs.length > 0) {
        String title = com.CH_gui.lang.Lang.rb.getString("msgTitle_Delete_Confirmation");
        String messageText = "Are you sure you want to send these items to the Recycle Bin?";
        boolean confirmed = MsgActionTable.showConfirmationDialog(FolderActionTree.this, title, messageText, folderPairs, NotificationCenter.RECYCLE_MESSAGE, true);
        if (confirmed) {
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          FolderPair recycleFolderPair = CacheUtilities.convertRecordToPair(cache.getFolderRecord(cache.getUserRecord().recycleFolderId));
          FileActionTable.doMoveOrSaveAttachmentsAction(recycleFolderPair, null, folderPairs);
        }
      }
    }
    private void updateText(int countSelectedFolderPairs) {
      if (countSelectedFolderPairs > 1) {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Delete_Folders_..."));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Permanently_delete_selected_folders_and_all_of_its_children_folders_and_contents."));
      } else {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Delete_Folder_..."));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Permanently_delete_selected_folder_and_all_of_its_children_folders_and_contents."));
      }
    }
  } // end private class DeleteFolderAction

  /**
   * Upload a file/directory to the table using UploadUtilities to do all the work
   */
  private class UploadAction extends AbstractActionTraced {
    public UploadAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Upload_..."), Images.get(ImageNums.EXPORT16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Upload"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.EXPORT24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Upload"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      if (!UserGuiOps.isShowWebAccountRestrictionDialog(FolderActionTree.this)) {
        Window w = SwingUtilities.windowForComponent(FolderActionTree.this);
        TreePath[] selectedPaths = getSelectionPaths();
        FolderPair[] selected = getLastPathComponentFolderPairs(selectedPaths);
        if (selected == null ||
            selected.length != 1 ||
            selected[0].getFolderRecord() == null ||
            selected[0].getFolderRecord().folderType.shortValue() != FolderRecord.FILE_FOLDER
           )
        {
          String title = "Select Upload Destination";
          selected = new FolderPair[] { selectFolder(w, title, new FolderFilter(new short[] { FolderRecord.CATEGORY_FILE_FOLDER, FolderRecord.FILE_FOLDER })) };
        }
        if (selected != null && selected[0] != null) {
          UploadUtilsGui.uploadFileChoice(selected[0].getFolderShareRecord(), FolderActionTree.this, MainFrame.getServerInterfaceLayer());
        }
      }
    }
  }

  /**
   * Download a file/directory to the local system using DownloadUtilities to do all the work
   */
  private class DownloadFolderAction extends AbstractActionTraced {
    public DownloadFolderAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Download_Folder_..."), Images.get(ImageNums.IMPORT_FOLDER16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Download_Folder"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.IMPORT_FOLDER24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Download"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      TreePath[] selectedPaths = getSelectionPaths();

      FolderTreeNode[] selectedNodes = getLastPathComponentNodes(selectedPaths);
      Vector selectedFolderPairs = new Vector();
      // eliminate all double selected folders through descendants
      for (int i=0; i<selectedNodes.length; i++) {
        boolean descendant = false;
        for (int j=0; j<selectedNodes.length; j++) {
          // is node 'i' descendant of 'j' ?? if so, we don't want 'i'
          if (i != j && selectedNodes[j].isNodeDescendant(selectedNodes[i])) {
            descendant = true;
            break;
          }
        }
        if (!descendant) {
          FolderPair pair = selectedNodes[i].getFolderObject();
          if (pair != null) {
            selectedFolderPairs.addElement(pair);
          }
        }
      }
      if (selectedFolderPairs.size() > 0) {
        FolderPair[] folderPairs = new FolderPair[selectedFolderPairs.size()];
        selectedFolderPairs.toArray(folderPairs);
        if (folderPairs != null && folderPairs.length > 0)
          DownloadUtilsGui.downloadFilesChoice(folderPairs, null, FolderActionTree.this, MainFrame.getServerInterfaceLayer());
      }
    }
    private void updateText(int countSelectedFolderPairs) {
      if (countSelectedFolderPairs > 1) {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Download_Folders_..."));
      } else {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Download_Folder_..."));
      }
    }
  }

  /**
   * Rename the folder, show a dialog to enter a new name and use FolderUtilities to do
   * the rest of the work
   */
  /*
  private class RenameFolderAction extends AbstractActionTraced {
    public RenameFolderAction(int actionId) {
      super("Rename Folder...");
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, "Rename Folder");
      //putValue(Actions.TOOL_ICON, Images.get(ImageNums.FILE_UPLOAD));
    }
    public void actionPerformedTraced(ActionEvent event) {
      FolderPair folderPair = getLastSelectedPair();
      if (folderPair == null) return;

      Window w = SwingUtilities.windowForComponent(FolderActionTree.this);
      Rename_Fld_Dialog d;
      if (w instanceof Frame) d = new Rename_Fld_Dialog((Frame) w, folderPair, "Rename");
      else if (w instanceof Dialog) d = new Rename_Fld_Dialog((Dialog) w, folderPair, "Rename");
      else return;

      String newName = d.getNewName();
      if (newName == null) return;

      // delegate rename to model.valueForPathChanged();
      TreePath path = getFolderTreeModel().getPathToRoot(folderPair);
      getFolderTreeModel().valueForPathChanged(path, newName);
    }
  }
*/

  /**
   * Rename the folder, show a dialog to enter a new name and use FolderUtilities to do
   * the rest of the work
   */
  private class PropertiesAction extends AbstractActionTraced {
    public PropertiesAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Folder_Properties_and_Sharing"));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Show_Folder_Properties_and_Manage_Folder_Sharing"));
      //putValue(Actions.TOOL_ICON, Images.get(ImageNums.FILE_UPLOAD));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FolderPair folderPair = getLastSelectedPair();
      if (folderPair == null ||
              (folderPair.getFolderRecord() != null &&
                (folderPair.getFolderRecord().folderType.shortValue() == FolderRecord.LOCAL_FILES_FOLDER ||
                 folderPair.getFolderRecord().isCategoryType())
              )
         )
      {
        return;
      }

      Window w = SwingUtilities.windowForComponent(FolderActionTree.this);
      if (w instanceof Frame) new FolderPropertiesDialog((Frame) w, folderPair);
      else if (w instanceof Dialog) new FolderPropertiesDialog((Dialog) w, folderPair);
    }
    private void updateText(FolderPair[] selectedFolderPairs) {
      if (selectedFolderPairs == null || selectedFolderPairs.length != 1) {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Folder_Properties_and_Sharing"));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Show_Folder_Properties_and_Manage_Folder_Sharing"));
      } else if (selectedFolderPairs[0].getFolderRecord().isGroupType()) {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Group_Properties_and_Members"));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Show_Group_Properties_and_Manage_Group_Memberships"));
      } else {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Folder_Properties_and_Sharing"));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Show_Folder_Properties_and_Manage_Folder_Sharing"));
      }
    }
  }


  /**
   * Refresh Folder List.
   */
  private class RefreshAction extends AbstractActionTraced {
    public RefreshAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Refresh_Folders"), Images.get(ImageNums.REFRESH16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Refresh_Folder_List_from_the_server."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.REFRESH24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Refresh"));
      putValue(Actions.IN_POPUP, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      new FolderTreeRefreshRunner(FolderActionTree.this).start();
    }
  }

  /**
   * Open in seperate window
   */
  private class OpenInSeperateWindowAction extends AbstractActionTraced {
    public OpenInSeperateWindowAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Clone_Folder_View"), Images.get(ImageNums.CLONE_FOLDER16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Display_folder_tree_in_its_own_window."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CLONE_FOLDER24));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      new FolderTreeFrame(FolderActionTree.this.getFolderTreeModel().getFilter());
    }
  }

  /**
   * Explore selected folders
   */
  private class ExploreFolderAction extends AbstractActionTraced {
    public ExploreFolderAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Explore_Folder"), Images.get(ImageNums.CLONE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Display_folder_in_its_own_window."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CLONE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Explore"));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      TreePath[] selectedPaths = getSelectionPaths();
      FolderPair[] folderPairs = getLastPathComponentFolderPairs(selectedPaths);
      for (int i=0; i<folderPairs.length; i++) {
        FolderPair fPair = folderPairs[i];
        if (fPair.getFolderRecord() != null) {
          short folderType = fPair.getFolderRecord().folderType.shortValue();
          if (folderType == FolderRecord.FILE_FOLDER) {
            new FileTableFrame(fPair);
          } else if (folderType == FolderRecord.MESSAGE_FOLDER) {
            new MsgTableFrame(fPair);
          } else if (folderType == FolderRecord.POSTING_FOLDER) {
            new PostTableFrame(fPair);
          } else if (folderType == FolderRecord.CHATTING_FOLDER) {
            new ChatTableFrame(fPair);
          } else if (folderType == FolderRecord.LOCAL_FILES_FOLDER) {
            new LocalFileTableFrame("Browse");
          } else if (folderType == FolderRecord.ADDRESS_FOLDER) {
            new AddressTableFrame(fPair);
          } else if (folderType == FolderRecord.WHITELIST_FOLDER) {
            new WhiteListTableFrame(fPair);
          } else if (folderType == FolderRecord.GROUP_FOLDER) {
            new GroupTableFrame(fPair);
          } else if (folderType == FolderRecord.RECYCLE_FOLDER) {
            new RecycleTableFrame(fPair);
          }
        }
      }
    }
    private void updateIcon(FolderPair[] selectedFolderPairs) {
      ImageIcon icon16 = Images.get(ImageNums.CLONE16);
      ImageIcon icon24 = Images.get(ImageNums.CLONE24);
      boolean isAddrType = false;
      boolean isFileType = false;
      boolean isLocalType = false;
      boolean isMsgType = false;
      boolean isGroupType = false;
      if (selectedFolderPairs != null && selectedFolderPairs.length > 1) {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Explore_Folders"));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Display_selected_folders_in_their_own_windows."));
      } else {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Explore_Folder"));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Display_folder_in_its_own_window."));
      }
      if (selectedFolderPairs != null && selectedFolderPairs.length > 0) {
        for (int i=0; i<selectedFolderPairs.length; i++) {
          FolderPair fPair = selectedFolderPairs[i];
          short folderType = fPair.getFolderRecord().folderType.shortValue();
          if (folderType == FolderRecord.FILE_FOLDER) {
            isFileType = true;
          } else if (FolderRecord.isMailType(folderType)) {
            isMsgType = true;
          } else if (folderType == FolderRecord.LOCAL_FILES_FOLDER) {
            isLocalType = true;
          } else if (FolderRecord.isAddressType(folderType)) {
            isAddrType = true;
          } else if (folderType == FolderRecord.GROUP_FOLDER) {
            isGroupType = true;
          }
        }
      }
      if (isFileType && !isMsgType && !isLocalType) {
        icon16 = Images.get(ImageNums.CLONE_FILE16);
        icon24 = Images.get(ImageNums.CLONE_FILE24);
      } else if (isMsgType && !isFileType && !isLocalType) {
        icon16 = Images.get(ImageNums.CLONE_MSG16);
        icon24 = Images.get(ImageNums.CLONE_MSG24);
      } else if (isAddrType) {
        icon16 = Images.get(ImageNums.CLONE_ADDR16);
        icon24 = Images.get(ImageNums.CLONE_ADDR24);
      } else if (isGroupType) {
        icon16 = Images.get(ImageNums.CLONE_GROUP16);
        icon24 = Images.get(ImageNums.CLONE_GROUP24);
      }
      putValue(Actions.MENU_ICON, icon16);
      putValue(Actions.TOOL_ICON, icon24);
    }
  }


  /**
   * Create a new message to a folder
   */
  private class NewMsgAction extends AbstractActionTraced {
    public NewMsgAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_New_Message_to_Folder"), Images.get(ImageNums.MAIL_COMPOSE_TO_FOLDER16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_New_Message_to_Folder"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_COMPOSE_TO_FOLDER24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Post"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      FolderPair pair = getLastSelectedPair();
      if (pair != null) {
        if (pair.getFolderRecord().folderType.shortValue() == FolderRecord.ADDRESS_FOLDER ||
            pair.getFolderRecord().folderType.shortValue() == FolderRecord.WHITELIST_FOLDER) {
          new AddressFrame(pair);
        } else {
          new MessageFrame(pair);
        }
      } else {
        new MessageFrame();
      }
    }
    private void updateIconAndText(FolderPair[] selectedFolderPairs) {
      if (selectedFolderPairs != null && selectedFolderPairs.length == 1) {
        if (selectedFolderPairs[0].getFolderRecord().folderType.shortValue() == FolderRecord.ADDRESS_FOLDER ||
            selectedFolderPairs[0].getFolderRecord().folderType.shortValue() == FolderRecord.WHITELIST_FOLDER) {
          putValue(Actions.MENU_ICON, Images.get(ImageNums.ADDRESS_ADD16));
          putValue(Actions.TOOL_ICON, Images.get(ImageNums.ADDRESS_ADD24));
          putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_New_Address"));
          putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_New_Address_to_Folder"));
          putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_New_Address_to_selected_Folder."));
        } else {
          putValue(Actions.MENU_ICON, Images.get(ImageNums.MAIL_COMPOSE_TO_FOLDER16));
          putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_COMPOSE_TO_FOLDER24));
          putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Post"));
          putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_New_Message_to_Folder"));
          putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_New_Message_to_Folder"));
        }
      }
    }
  }


  /**
   * Open history stat dialog for selected object.
   */
  private class TracePrivilegeAndHistoryAction extends AbstractActionTraced {
    public TracePrivilegeAndHistoryAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Trace_Folder_Access"));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Trace_Folder_Access"));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      TreePath[] selectedPaths = getSelectionPaths();
      FolderPair[] selected = getLastPathComponentFolderPairs(selectedPaths);
      if (selected != null && selected.length >= 1) {
        Window w = SwingUtilities.windowForComponent(FolderActionTree.this);
        if (w instanceof Frame) new TraceRecordDialog((Frame) w, selected);
        else if (w instanceof Dialog) new TraceRecordDialog((Dialog) w, selected);
      }
    }
    private void updateText(FolderPair[] selectedFolderPairs) {
      if (selectedFolderPairs == null || selectedFolderPairs.length != 1) {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Trace_Folder_Access"));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Trace_Folder_Access"));
      } else if (selectedFolderPairs[0].getFolderRecord().isGroupType()) {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Trace_Group_Access"));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Trace_Group_Access"));
      } else {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Trace_Folder_Access"));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Trace_Folder_Access"));
      }
    }
  }


  /**
   * Transfer folder ownership.
   */
  private class TransferOwnershipAction extends AbstractActionTraced {
    public TransferOwnershipAction(int actionId) {
      super("Transfer Folder Ownership");
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, "Transfer Folder Ownership");
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      if (!UserGuiOps.isShowWebAccountRestrictionDialog(FolderActionTree.this)) {
        TreePath[] selectedPaths = getSelectionPaths();
        FolderPair[] selected = getLastPathComponentFolderPairs(selectedPaths);
        if (selected != null && selected.length >= 1) {
          ContactSelectDialog d = null;
          Window w = SwingUtilities.windowForComponent(FolderActionTree.this);
          if (w instanceof Dialog || w instanceof Frame) {
            if (w instanceof Dialog)
              d = new ContactSelectDialog((Dialog) w, false, false);
            else
              d = new ContactSelectDialog((Frame) w, false, false);

            d.setTitle("Transfer Ownership");

            JRadioButton jYourself = new JMyRadioButton("Take ownership of the selected folder(s), or", false);
            JRadioButton jOther = new JMyRadioButton("Transfer ownership to selected contact", false);

            ButtonGroup group = new ButtonGroup();
            group.add(jYourself);
            group.add(jOther);

            final JButton jOk = d.getOkButton();
            jOk.setText(com.CH_gui.lang.Lang.rb.getString("button_Apply"));
            final RecordTableComponent jTable = d.getTable();
            jTable.getActionTable().getJSortedTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            // disable table
            jTable.setEnabled(false);
            jTable.getActionTable().setEnabled(false);
            jTable.getActionTable().getJSortedTable().setEnabled(false);

            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            int posY = 0;
            panel.add(new JMyLabel("Transfer Ownership to:"), new GridBagConstraints(0, posY, 1, 1, 0, 0,
                  GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
            posY ++;
            panel.add(jYourself, new GridBagConstraints(0, posY, 1, 1, 0, 0,
                  GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));
            posY ++;
            panel.add(jOther, new GridBagConstraints(0, posY, 1, 1, 0, 0,
                  GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));
            posY ++;

            d.addHeader(panel, 0, new MyInsets(0, 0, 0, 0));
            d.getTableLabel().setText("Transfer ownership to:");

            jYourself.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                jOk.setEnabled(true);
                // disable table
                jTable.setEnabled(false);
                jTable.getActionTable().setEnabled(false);
                jTable.getActionTable().getJSortedTable().setEnabled(false);
              }
            });
            jOther.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                Record[] selected = jTable.getActionTable().getSelectedRecords();
                jOk.setEnabled(selected != null && selected.length > 0);
                // enable table
                jTable.setEnabled(true);
                jTable.getActionTable().setEnabled(true);
                jTable.getActionTable().getJSortedTable().setEnabled(true);
              }
            });

            d.pack();
            d.setVisible(true);

            FetchedDataCache cache = MainFrame.getServerInterfaceLayer().getFetchedDataCache();
            Long toUserId = null;
            if (d.getResultButton() != null && d.getResultButton().intValue() == ContactSelectDialog.DEFAULT_OK_INDEX) {
              if (jYourself.isSelected()) {
                toUserId = cache.getMyUserId();
              } else if (jOther.isSelected()) {
                ContactRecord[] selectedContacts = d.getSelectedContacts();
                if (selectedContacts != null && selectedContacts.length == 1) {
                  toUserId = selectedContacts[0].contactWithId;
                }
              }
            }

            // if a user was selected
            if (toUserId != null) {
              final FolderPair[] children = cache.getFolderPairsViewAllDescending(selected, false);
              boolean runTransfer = true;
              if (children != null && children.length > 0) {
                String[] lines = new String[] {
                    "You have chosen to make folder ownership changes.",
                    "Do you want to apply this change to the selected folder(s) only, ",
                    "or do you want to apply it to all subfolders as well?"
                };
                String[] choices = new String[] {
                    "Apply changes to the selected folder(s) only",
                    "Apply changes to the selected folder(s) and subfolders"
                };
                Insets[] insets = new Insets[] {
                    new MyInsets(5, 5, 5, 5),
                    new MyInsets(5, 5, 1, 5),
                    new MyInsets(1, 5, 5, 5),
                    new MyInsets(5, 25, 2, 5),
                    new MyInsets(2, 25, 5, 5)
                };

                runTransfer = false;
                ChoiceDialog choiceDialog = null;
                if (w instanceof Dialog || w instanceof Frame) {
                  if (w instanceof Dialog)
                    choiceDialog = new ChoiceDialog((Dialog) w, "Confirm Ownership Changes", lines, choices, insets, 1);
                  else
                    choiceDialog = new ChoiceDialog((Frame) w, "Confirm Ownership Changes", lines, choices, insets, 1);

                  Integer resultButton = choiceDialog.getResultButton();
                  if (resultButton != null && resultButton.intValue() == ChoiceDialog.DEFAULT_OK_INDEX) {
                    runTransfer = true;
                    if (choiceDialog.getResultChoice().intValue() == 1) {
                      selected = (FolderPair[]) ArrayUtils.concatinate(selected, children);
                      selected = (FolderPair[]) ArrayUtils.removeDuplicates(selected);
                    }
                  }
                }
              }
              if (runTransfer) {
                Long[] folderIDs = RecordUtils.getIDs(selected);
                // See if we need to fetch any shares so that we can start determining which ones we need to create
                Vector shareIDsV = new Vector();
                for (int i=0; i<selected.length; i++) {
                  FolderRecord fRec = selected[i].getFolderRecord();
                  FolderShareRecord sRec = selected[i].getFolderShareRecord();
                  short numOfShares = fRec.numOfShares.shortValue();
                  FolderShareRecord[] sRecs = cache.getFolderShareRecordsForFolder(fRec.folderId);
                  int sRecsLength = sRecs != null ? sRecs.length : 0;
                  if (numOfShares > sRecsLength) {
                    if (!shareIDsV.contains(sRec.shareId))
                      shareIDsV.addElement(sRec.shareId);
                  }
                }
                // Fetch any shares that are not in cache yet
                if (shareIDsV.size() > 0) {
                  Long[] shareIDs = (Long[]) ArrayUtils.toArray(shareIDsV, Long.class);
                  MainFrame.getServerInterfaceLayer().submitAndWait(new MessageAction(CommandCodes.FLD_Q_GET_FOLDER_SHARES, new Obj_IDList_Co(shareIDs)), 60000);
                }
                // See if we need to create any additional shares
                ArrayList addSharesL = new ArrayList();
                //int fakeShareIdIndex = 0;
                for (int i=0; i<selected.length; i++) {
                  FolderPair fPair = selected[i];
                  FolderRecord fRec = selected[i].getFolderRecord();
                  FolderShareRecord sRec = selected[i].getFolderShareRecord();
                  FolderShareRecord[] sRecs = cache.getFolderShareRecordsForFolder(fRec.folderId);
                  int sRecsLength = sRecs != null ? sRecs.length : 0;
                  // Find share of the 'toUserId'
                  FolderShareRecord toUserShare = null;
                  for (int k=0; k<sRecsLength; k++) {
                    if (sRecs[k].ownerUserId.equals(toUserId)) {
                      toUserShare = sRecs[k];
                      break;
                    }
                  }
                  // If we need to create a share, do it now and seal it with user specific key
                  if (toUserShare == null) {
                    FolderShareRecord newShare = (FolderShareRecord) sRec.clone();
                    newShare.setViewParentId(fPair.getFileViewParentId());
                    newShare.ownerUserId = toUserId;
                    newShare.ownerType = new Short(Record.RECORD_TYPE_USER);
                    // Fetch public key for 'toUserId' if not already fetched
                    KeyRecord kRec = cache.getKeyRecordForUser(toUserId);
                    if (kRec == null || kRec.plainPublicKey == null) {
                      MainFrame.getServerInterfaceLayer().submitAndWait(new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_USERS, new Obj_IDList_Co(toUserId)), 60000);
                      kRec = cache.getKeyRecordForUser(toUserId);
                    }
                    newShare.seal(kRec);
                    addSharesL.add(newShare);
                  }
                }
                // Send Ownership Transfer request
                Fld_AddShares_Rq addSharesRequest = new Fld_AddShares_Rq();
                if (addSharesL.size() > 0) {
                  addSharesRequest.shareRecords = (FolderShareRecord[]) ArrayUtils.toArray(addSharesL, FolderShareRecord.class);
                }
                addSharesRequest.contactIds = new Obj_IDList_Co(RecordUtils.getIDs(cache.getContactRecordsMyActive()));
                MessageAction requestAction = new MessageAction(CommandCodes.FLD_Q_TRANSFER_OWNERSHIP, new Obj_List_Co(new Object[] { toUserId, folderIDs, addSharesRequest }));
                MainFrame.getServerInterfaceLayer().submitAndReturn(requestAction);
              }
            }
          }
        }
      }
    } // end actionPerformed()
    private void updateText(FolderPair[] selectedFolderPairs) {
      if (selectedFolderPairs == null || selectedFolderPairs.length != 1) {
        putValue(Actions.NAME, "Transfer Folder Ownership");
        putValue(Actions.TOOL_TIP, "Transfer Folder Ownership");
      } else if (selectedFolderPairs[0].getFolderRecord().isGroupType()) {
        putValue(Actions.NAME, "Transfer Group Ownership");
        putValue(Actions.TOOL_TIP, "Transfer Group Ownership");
      } else {
        putValue(Actions.NAME, "Transfer Folder Ownership");
        putValue(Actions.TOOL_TIP, "Transfer Folder Ownership");
      }
    }
  } // end class TransferOwnershipAction


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
      putValue(Actions.IN_MENU, Boolean.TRUE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
      putValue(Actions.IN_TOOLBAR, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      actionPerformed_Invite(event);
    }
    private void updateText(FolderPair[] selectedFolderPairs) {
      updateInviteActionText(selectedFolderPairs, InviteAction.this);
    }
  }

  /**
   * Show selected Folder's sharing panel so user can add/invite others.
   */
  private class InvitePopupAction extends AbstractActionTraced {
    public InvitePopupAction (int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Share_Folder_..."), Images.get(ImageNums.FLD_CLOSED_SHARED16, true));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Share_Folder_..."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLD_CLOSED_SHARED24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Share"));
      putValue(Actions.IN_MENU, Boolean.FALSE);
      putValue(Actions.IN_POPUP, Boolean.TRUE);
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      actionPerformed_Invite(event);
    }
    private void updateText(FolderPair[] selectedFolderPairs) {
      updateInviteActionText(selectedFolderPairs, InvitePopupAction.this);
    }
  }

  private void actionPerformed_Invite(ActionEvent event) {
    Window w = SwingUtilities.windowForComponent(FolderActionTree.this);
    TreePath[] selectedPaths = getSelectionPaths();
    FolderPair[] selected = getLastPathComponentFolderPairs(selectedPaths);
    if (selected == null || selected.length != 1 ||
          (selected[0].getFolderRecord() != null &&
            (selected[0].getFolderRecord().folderType.shortValue() == FolderRecord.LOCAL_FILES_FOLDER ||
             selected[0].getFolderRecord().isCategoryType())
          )
       )
    {
      String title = com.CH_gui.lang.Lang.rb.getString("title_Select_Folder_or_Group_to_Share");
      selected = new FolderPair[] { selectFolder(w, title, new MultiFilter(new RecordFilter[] { FolderFilter.MAIN_VIEW, FolderFilter.NON_LOCAL_FOLDERS }, MultiFilter.AND)) };
    }
    if (selected != null && selected[0] != null) {
      if (w instanceof Frame) new FolderPropertiesDialog((Frame) w, selected[0], 1);
      else if (w instanceof Dialog) new FolderPropertiesDialog((Dialog) w, selected[0], 1);
    }
  }

  private void updateInviteActionText(FolderPair[] selectedFolderPairs, Action action) {
    if (selectedFolderPairs == null || selectedFolderPairs.length != 1) {
      action.putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Share_Folder_..."));
      action.putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Share_Folder_..."));
      action.putValue(Actions.MENU_ICON, Images.get(ImageNums.FLD_CLOSED_SHARED16, true));
      action.putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLD_CLOSED_SHARED24));
      action.putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Share"));
    } else if (selectedFolderPairs[0].getFolderRecord().isAddressType()) {
      action.putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Share_Address_Book_..."));
      action.putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Share_Address_Book_..."));
      action.putValue(Actions.MENU_ICON, Images.get(ImageNums.FLD_ADDR_CLOSED_SHARED16, true));
      action.putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLD_ADDR_CLOSED_SHARED24));
      action.putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Share"));
    } else if (selectedFolderPairs[0].getFolderRecord().isChatting()) {
      action.putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Invite_to_the_Conversation_..."));
      action.putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Invite_to_the_Conversation_..."));
      action.putValue(Actions.MENU_ICON, Images.get(ImageNums.FLD_CHAT_CLOSED_SHARED16, true));
      action.putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLD_CHAT_CLOSED_SHARED24));
      action.putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Invite"));
    } else if (selectedFolderPairs[0].getFolderRecord().isGroupType()) {
      action.putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Invite_to_the_Group_..."));
      action.putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Invite_to_the_Group_..."));
      action.putValue(Actions.MENU_ICON, Images.get(ImageNums.MEMBER_ADD16));
      action.putValue(Actions.TOOL_ICON, Images.get(ImageNums.MEMBER_ADD24));
      action.putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Add_Member"));
    } else {
      action.putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Share_Folder_..."));
      action.putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Share_Folder_..."));
      action.putValue(Actions.MENU_ICON, Images.get(ImageNums.FLD_CLOSED_SHARED16, true));
      action.putValue(Actions.TOOL_ICON, Images.get(ImageNums.FLD_CLOSED_SHARED24));
      action.putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Share"));
    }
  }

  /**
   * Empty Recycle Bin Action
   */
  private class EmptyFolderAction extends AbstractActionTraced {
    public EmptyFolderAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Empty_Folder_..."), Images.get(ImageNums.FLD_RECYCLE_CLEAR16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Empty_Folder_..."));
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
      putValue(Actions.IN_POPUP_SHOW_DEACTIVATED, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      FolderPair fPair = getActionFolderPair();
      if (fPair != null) {
        doEmptyAction(fPair, fPair.getFolderRecord().folderType.shortValue() == FolderRecord.RECYCLE_FOLDER, FolderActionTree.this);
      }
    }
    private FolderPair getActionFolderPair() {
      FolderPair actionFolderPair = null;
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      UserRecord uRec = cache.getUserRecord();
      FolderPair fPair = getLastSelectedPair();
      FolderRecord fRec = null;
      if (fPair != null && uRec != null && uRec.isCapableToEmptyFolder(fPair.getFolderRecord())) {
        fRec = fPair.getFolderRecord();
      } else if (uRec != null) {
        fRec = cache.getFolderRecord(uRec.recycleFolderId);
      }
      if (fRec != null) {
        actionFolderPair = CacheUtilities.convertRecordToPair(fRec);
      }
      return actionFolderPair;
    }
    private void updateTextAndIcon() {
      FolderPair fPair = getActionFolderPair();
      if (fPair != null) {
        UserRecord uRec = FetchedDataCache.getSingleInstance().getUserRecord();
        boolean isRecycleFld = uRec != null && uRec.recycleFolderId.equals(fPair.getId());
        putValue(Actions.NAME, java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("action_Empty_FOLDER_NAME_Folder_..."), new Object[] { fPair.getMyName()}));
        putValue(Actions.TOOL_TIP, java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("action_Empty_FOLDER_NAME_Folder_..."), new Object[] { fPair.getMyName()}));
        if (isRecycleFld)
          putValue(Actions.MENU_ICON, Images.get(ImageNums.FLD_RECYCLE_CLEAR16));
        else
          putValue(Actions.MENU_ICON, Images.get(ImageNums.FLD_CLEAR16));
      } else {
        putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Empty_Folder_..."));
        putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Empty_Folder_..."));
        putValue(Actions.MENU_ICON, Images.get(ImageNums.FLD_CLEAR16));
      }
    }
  }

  public static void doEmptyAction(final FolderPair folderToEmpty, boolean includeSubTree, Component parent) {
    String title = com.CH_gui.lang.Lang.rb.getString("title_Delete_Confirmation");
    String messageText = "<html>Are you sure you want to <b>permanently delete</b> items from the <br>following folder?  This action cannot be reversed.</html>";
    short folderType = folderToEmpty.getFolderRecord().folderType.shortValue();
    int messageType = folderType == FolderRecord.RECYCLE_FOLDER ? NotificationCenter.EMPTY_RECYCLE_FOLDER : NotificationCenter.EMPTY_SPAM_FOLDER;
    boolean confirmed = MsgActionTable.showConfirmationDialog(parent, title, messageText, new Record[] { folderToEmpty }, messageType, false);
    if (confirmed) {
      Obj_IDs_Co request = new Obj_IDs_Co();
      request.IDs = new Long[][] { null, { folderToEmpty.getFolderShareRecord().shareId } };
      MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.MSG_Q_REMOVE, request));
      Runnable clearRedFlagsJob = new Runnable() {
        public void run() {
          // reset the values including cached rendered text
          folderToEmpty.getFolderRecord().resetUpdates();
          // Cause folder listeners to do visual update.
          FetchedDataCache.getSingleInstance().addFolderRecords(new FolderRecord[] { folderToEmpty.getFolderRecord() });
        }
      };
      MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.FILE_Q_REMOVE_FILES, request), 30000, clearRedFlagsJob, clearRedFlagsJob);
      if (includeSubTree) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        FolderPair[] children = cache.getFolderPairsViewChildren(folderToEmpty.getId(), true);
        if (children != null && children.length > 0) {
          new FolderActionTree.DeleteRunner(parent, children, true).start();
        }
      }
    }
  }

  /**
   * Show a dialog for user to select a Folder.
   */
  public static FolderPair selectFolder(Window parent, String title, AbstractRecordFilter filter) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    FolderRecord[] allFolderRecords = cache.getFolderRecords();
    FolderPair[] allFolderPairs = CacheUtilities.convertRecordsToPairs(allFolderRecords);
    allFolderPairs = (FolderPair[]) filter.filterInclude(allFolderPairs);

    FolderPair chosenPair = null;

    // all remote folders are always ok
    boolean isDescendantOk = true;

    Move_NewFld_Dialog d = null;
    if (parent instanceof Frame) d = new Move_NewFld_Dialog((Frame) parent, allFolderPairs, null, null, title, isDescendantOk, cache, filter);
    else if (parent instanceof Dialog) d = new Move_NewFld_Dialog((Dialog) parent, allFolderPairs, null, null, title, isDescendantOk, cache, filter);

    if (d != null) {
      chosenPair = d.getChosenDestination();
    }

    return chosenPair;
  }


  /**
   * Delete Action private helper.
   */
  public static class DeleteRunner extends ThreadTraced {
    private Component parent;
    private FolderPair[] folderPairs;
    private boolean includeSubTree;
    public DeleteRunner(Component parent, FolderPair[] folderPairs, boolean includeSubTree) {
      super("Delete Runner");
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DeleteRunner.class, "DeleteRunner(Component parent, FolderPair[] folderPairs, boolean includeSubTree)");
      if (trace != null) trace.args(parent, folderPairs);
      if (trace != null) trace.args(includeSubTree);
      this.parent = parent;
      this.folderPairs = folderPairs;
      this.includeSubTree = includeSubTree;
      setDaemon(true);
      if (trace != null) trace.exit(DeleteRunner.class);
    }
    public void runTraced() {
      // Gather the entire sub-tree of folders
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      UserRecord userRecord = cache.getUserRecord();
      FolderPair[] folderTree = folderPairs;
      if (includeSubTree) {
        FolderPair[] folderSubTree = cache.getFolderPairsViewAllDescending(folderPairs, false);
        folderTree = (FolderPair[]) ArrayUtils.concatinate(folderPairs, folderSubTree);
      } else {
        folderTree = folderPairs;
      }
      // Create a deapth-first logical order for the sub-tree
      FolderTreeNode rootNode = new FolderTreeNode();
      rootNode.addNodes(folderTree, true);
      Enumeration enm = rootNode.depthFirstEnumeration();
      Vector protectedMasterFoldersV = null;
      while (enm.hasMoreElements()) {
        FolderTreeNode n = (FolderTreeNode) enm.nextElement();
        FolderPair fPair = (FolderPair) n.getUserObject();
        if (fPair != null) {
          // Check that this folder is still available.
          // It might have dissapeared when server reply came back from previous deletion in the batch.
          Long folderId = fPair.getFolderRecord().getId();
          if (cache.getFolderRecord(folderId) != null && FolderTreeNode.findNode(folderId, true, rootNode) != null) {

            boolean canDelete = true;
            if (userRecord.isBusinessSubAccount() &&
                fPair.getFolderRecord().ownerUserId.equals(userRecord.masterId) &&
                !Misc.isBitSet(userRecord.flags, UserRecord.FLAG_ENABLE_GIVEN_MASTER_FOLDERS_DELETE))
            {
              canDelete = false;
            }
            if (canDelete) {
              Obj_ID_Rq request = new Obj_ID_Rq();
              request.objId = folderId;
              MessageAction msgAction = new MessageAction(CommandCodes.FLD_Q_REMOVE_FOLDER, request);
              MainFrame.getServerInterfaceLayer().submitAndWait(msgAction, 60000);
            } else {
              if (protectedMasterFoldersV == null) protectedMasterFoldersV = new Vector();
              protectedMasterFoldersV.addElement(fPair);
            }
          }
        }
      }
      if (protectedMasterFoldersV != null && protectedMasterFoldersV.size() > 0) {
        StringBuffer sb = new StringBuffer();
        sb.append("Could not delete the following protected folders because they are assigned to you by your administrator.  To change this setting please contact your administrator.\n\n");
        for (int i=0; i<protectedMasterFoldersV.size(); i++) {
          if (i>0)
            sb.append(", \n");
          sb.append("   ");
          sb.append(ListRenderer.getRenderedText(protectedMasterFoldersV.elementAt(i), false, true, false));
        }
        MessageDialog.showWarningDialog(parent, sb.toString(), "Could not delete protected folders...", false);
      }
    }
  } // end private class DeleteRunner


  /****************************************************************************/
  /*        A c t i o n P r o d u c e r I
  /****************************************************************************/

  /** @return all the acitons that this objects produces.
   */
  public Action[] getActions() {
    return actions;
  }
  /** Final Action Producers will not be traversed to collect its containing objects' actions.
   * @return true if this object will gather all actions from its childeren or hide them counciously.
   */
  public boolean isFinalActionProducer() {
    return true;
  }

  /**
   * Enables or Disables actions based on the current state of the Action Producing component.
   */
  public void setEnabledActions() {
    if (!MainFrame.isLoggedIn()) {
      ActionUtils.setEnabledActions(actions, false);
    } else {
      // Always enable these actions
      actions[INVITE_ACTION].setEnabled(true);
      actions[INVITE_POPUP_ACTION].setEnabled(true);
      actions[NEW_FOLDER_ACTION].setEnabled(true);
      actions[UPLOAD_ACTION].setEnabled(true);
      actions[EMPTY_FOLDER_ACTION].setEnabled(true); // if no suitable folder is selected, this will empty the Recycle Bin
      actions[MSG_COMPOSE_ACTION].setEnabled(true);

      int count = getSelectionCount();
      int countPairs = 0;

      UserRecord userRecord = MainFrame.getServerInterfaceLayer().getFetchedDataCache().getUserRecord();

      boolean moveOk = true;
      boolean deleteOk = true;
      boolean downloadOk = true;
      boolean exploreOk = true;
      boolean newMsgOk = true;
      boolean propertiesOk = true;
      boolean traceOk = true;
      boolean transferOk = true;
      boolean emptyFolderOk = false;
      TreePath[] selectedPaths = getSelectionPaths();
      FolderPair[] selectedFolderPairs = null;
      if (selectedPaths != null && selectedPaths.length > 0) {

        selectedFolderPairs = getLastPathComponentFolderPairs(selectedPaths);
        countPairs = selectedFolderPairs.length;

        for (int i=0; i<countPairs; i++) {
          FolderPair folderPair = selectedFolderPairs[i];
          FolderRecord folderRecord = folderPair.getFolderRecord();
          Long folderId = folderRecord.folderId;

          short folderType = folderRecord.folderType.shortValue();
          if (!folderRecord.isFileType() &&
              !folderRecord.isMsgType() &&
              !folderRecord.isRecycleType() &&
              !folderRecord.isCategoryType())
            downloadOk = false;
          if (folderType == FolderRecord.CATEGORY_GROUP_FOLDER)
            downloadOk = false;
          if (folderType == FolderRecord.RECYCLE_FOLDER)
            emptyFolderOk = true;
          if (userRecord != null && userRecord.junkFolderId.equals(folderId))
            emptyFolderOk = true;
          if (folderType != FolderRecord.FILE_FOLDER &&
              folderType != FolderRecord.ADDRESS_FOLDER &&
              folderType != FolderRecord.WHITELIST_FOLDER &&
              folderType != FolderRecord.MESSAGE_FOLDER &&
              folderType != FolderRecord.POSTING_FOLDER &&
              folderType != FolderRecord.CHATTING_FOLDER &&
              folderType != FolderRecord.LOCAL_FILES_FOLDER &&
              folderType != FolderRecord.GROUP_FOLDER &&
              folderType != FolderRecord.RECYCLE_FOLDER)
            exploreOk = false;
          if (!FolderRecord.isMsgType(folderType))
            newMsgOk = false;

          if (folderType == FolderRecord.LOCAL_FILES_FOLDER || folderRecord.isCategoryType()) {
            moveOk = false;
            deleteOk = false;
            propertiesOk = false;
            traceOk = false;
            transferOk = false;
          }

          // cannot delete or move super-root folders
          if (userRecord == null || folderRecord.isSuperRoot(userRecord)) {
            moveOk = false;
            deleteOk = false;
            transferOk = false;
          }

          // cannot transfer non-sharable folders
          if (transferOk && !folderRecord.isSharableType()) {
            transferOk = false;
          }
        }
      }

      if (count == 1 && countPairs == 1) {
        actions[MOVE_FOLDER_ACTION].setEnabled(moveOk);
        actions[DELETE_FOLDER_ACTION].setEnabled(deleteOk);
        //actions[RENAME_FOLDER_ACTION].setEnabled(true);
        actions[DOWNLOAD_FOLDER_ACTION].setEnabled(downloadOk);
        actions[PROPERTIES_ACTION].setEnabled(propertiesOk);
        actions[EXPLORE_FOLDER_ACTION].setEnabled(exploreOk);
        actions[NEW_MSG_ACTION].setEnabled(newMsgOk);
        actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION].setEnabled(traceOk);
        actions[TRANSFER_OWNERSHIP_ACTION].setEnabled(transferOk);
        //actions[EMPTY_FOLDER_ACTION].setEnabled(emptyFolderOk);
        actions[EMPTY_FOLDER_ACTION].putValue(Actions.IN_POPUP, Boolean.valueOf(emptyFolderOk));
      } else if (count > 1 && countPairs > 1) {
        actions[MOVE_FOLDER_ACTION].setEnabled(false);
        actions[DELETE_FOLDER_ACTION].setEnabled(deleteOk);
        //actions[RENAME_FOLDER_ACTION].setEnabled(false);
        actions[DOWNLOAD_FOLDER_ACTION].setEnabled(downloadOk);
        actions[PROPERTIES_ACTION].setEnabled(false);
        actions[EXPLORE_FOLDER_ACTION].setEnabled(exploreOk);
        actions[NEW_MSG_ACTION].setEnabled(false);
        actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION].setEnabled(true);
        actions[TRANSFER_OWNERSHIP_ACTION].setEnabled(transferOk);
        //actions[EMPTY_FOLDER_ACTION].setEnabled(false);
        actions[EMPTY_FOLDER_ACTION].putValue(Actions.IN_POPUP, Boolean.FALSE);
      } else {
        // nothing selected or root selected
        actions[MOVE_FOLDER_ACTION].setEnabled(false);
        actions[DELETE_FOLDER_ACTION].setEnabled(false);
        //actions[RENAME_FOLDER_ACTION].setEnabled(false);
        actions[DOWNLOAD_FOLDER_ACTION].setEnabled(false);
        actions[PROPERTIES_ACTION].setEnabled(false);
        actions[EXPLORE_FOLDER_ACTION].setEnabled(false);
        actions[NEW_MSG_ACTION].setEnabled(false);
        actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION].setEnabled(false);
        actions[TRANSFER_OWNERSHIP_ACTION].setEnabled(false);
        //actions[EMPTY_FOLDER_ACTION].setEnabled(false);
        actions[EMPTY_FOLDER_ACTION].putValue(Actions.IN_POPUP, Boolean.FALSE);
      }

      Window w = SwingUtilities.windowForComponent(this);
      actions[REFRESH_ACTION].setEnabled(w != null);
      actions[OPEN_IN_SEPERATE_WINDOW_ACTION].setEnabled(w != null);

      // change new icons and text according to selected items
      ((NewFolderAction) actions[NEW_FOLDER_ACTION]).updateIconAndText(selectedFolderPairs);
      ((NewMsgAction) actions[NEW_MSG_ACTION]).updateIconAndText(selectedFolderPairs);
      ((ExploreFolderAction) actions[EXPLORE_FOLDER_ACTION]).updateIcon(selectedFolderPairs);
      ((InviteAction) actions[INVITE_ACTION]).updateText(selectedFolderPairs);
      ((InvitePopupAction) actions[INVITE_POPUP_ACTION]).updateText(selectedFolderPairs);
      ((PropertiesAction) actions[PROPERTIES_ACTION]).updateText(selectedFolderPairs);
      ((TracePrivilegeAndHistoryAction) actions[TRACE_PRIVILEGE_AND_HISTORY_ACTION]).updateText(selectedFolderPairs);
      ((TransferOwnershipAction) actions[TRANSFER_OWNERSHIP_ACTION]).updateText(selectedFolderPairs);
      ((DeleteFolderAction) actions[DELETE_FOLDER_ACTION]).updateText(countPairs);
      ((DownloadFolderAction) actions[DOWNLOAD_FOLDER_ACTION]).updateText(countPairs);
      ((EmptyFolderAction) actions[EMPTY_FOLDER_ACTION]).updateTextAndIcon();
    }
  }



  /**
   * Remembers the folderId and associates it with a mark.
   * Useful to check if folder update count has changed since last marking.
   */
  private void setFolderUpdateMark(Long folderId, int mark) {
    if (folderUpdateHistoryHT == null) folderUpdateHistoryHT = new Hashtable();
    folderUpdateHistoryHT.put(folderId, new Integer(mark));
  }
  /**
   * @return last stored mark for given folder, or zero '0' is not stored at all.
   */
  private int getFolderUpdateMark(Long folderId) {
    Integer mark = null;
    if (folderUpdateHistoryHT != null)
      mark = (Integer) folderUpdateHistoryHT.get(folderId);
    return mark != null ? mark.intValue() : 0;
  }


  /** Listen on updates to the FolderRecords in the cache.
   * If the event happens, set or remove records
   */
  private class FolderListener implements FolderRecordListener {
    public void folderRecordUpdated(FolderRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new FolderGUIUpdater(event));
    }
  }
  private class FolderGUIUpdater implements Runnable {
    private FolderRecordEvent event;
    public FolderGUIUpdater(FolderRecordEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderGUIUpdater.class, "FolderGUIUpdater(FolderRecordEvent event)");
      this.event = event;
      if (trace != null) trace.exit(FolderGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderGUIUpdater.class, "FolderGUIUpdater.run()");
      FolderRecord[] folderRecords = null;
      folderRecords = event.getFolderRecords();
      if (folderRecords != null) {
        FolderTreeModelCl model = null;
        for (int i=0; i<folderRecords.length; i++) {
          FolderRecord fRec = folderRecords[i];
          int updates = fRec.getUpdateCount();
          if (updates > 0) {
            int lastMark = getFolderUpdateMark(fRec.folderId);
            if (updates > lastMark) { // if updates is less then who cares for deletions, nothing new after all
              if (model == null) model = FolderActionTree.this.getFolderTreeModel();
              FolderTreeNode node = model.findNode(folderRecords[i].folderId, true);
              TreePath path = model.getPathToRoot(fRec);
              if (path != null && !FolderActionTree.this.isVisible(path)) {
                //FolderActionTree.this.scrollPathToVisible(path);
                FolderActionTree.this.scrollPathToVisible2(path);
              }
            }
          }
          // set the last processed undate count even if zero
          setFolderUpdateMark(fRec.folderId, updates);
        } // end for
      }
      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(FolderGUIUpdater.class);
    }
  } // end class FolderGUIUpdater


  /**
   * I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
   * Dispose the object and release resources to help in garbage collection.
  */
  public void disposeObj() {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    if (folderListener != null)
      cache.removeFolderRecordListener(folderListener);
    folderListener = null;
    super.disposeObj();
  }

}
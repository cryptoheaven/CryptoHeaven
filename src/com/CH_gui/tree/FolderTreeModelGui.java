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
package com.CH_gui.tree;

import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.Trace;
import com.CH_co.tree.FolderTreeNode;
import com.CH_co.util.*;

import java.util.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FolderTreeModelGui extends DefaultTreeModel {

  private RecordFilter filter = null;
  private final HashMap folderNodesHM = new HashMap();

  /** Creates new FolderTreeModelGui */
  public FolderTreeModelGui() {
    this(new FolderTreeNodeGui());
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelGui.class, "FolderTreeModelGui()");
    if (trace != null) trace.exit(FolderTreeModelGui.class);
  }

  /** Creates new FolderTreeModelGui with specified folder filter. */
  public FolderTreeModelGui(RecordFilter filter) {
    this(new FolderTreeNodeGui());
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelGui.class, "FolderTreeModelGui(FolderFilter folderFilter)");
    this.filter = filter;
    if (trace != null) trace.exit(FolderTreeModelGui.class);
  }

  /** Creates new FolderTreeModelGui with specified folder filter. */
  public FolderTreeModelGui(RecordFilter filter, FolderPair[] initialFolderPairs) {
    this(new FolderTreeNodeGui());
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelGui.class, "FolderTreeModelGui(FolderFilter folderFilter, FolderPair[] initialFolderPairs)");
    this.filter = filter;
    addNodes(initialFolderPairs);
    if (trace != null) trace.exit(FolderTreeModelGui.class);
  }

  /** Creates new FolderTreeModelGui */
  public FolderTreeModelGui(FolderTreeNodeGui root) {
    super(root); // <-- asksAllowToHaveChildren
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelGui.class, "FolderTreeModelGui(FolderTreeNodeGui root)");
    if (trace != null) trace.exit(FolderTreeModelGui.class);
  }

  /** Creates new FolderTreeModelGui */
  public FolderTreeModelGui(FolderTreeNodeGui root, RecordFilter filter, FolderPair[] initialFolderPairs) {
    super(root); // <-- asksAllowToHaveChildren
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelGui.class, "FolderTreeModelGui(FolderTreeNodeGui root, FolderFilter folderFilter, FolderPair[] initialFolderPairs)");
    this.filter = filter;
    addNodes(initialFolderPairs);
    if (trace != null) trace.exit(FolderTreeModelGui.class);
  }


//  public FolderTreeNode getChildNode(FolderTreeNode parent, int childIndex) {
//    return (FolderTreeNode) getChild(parent, childIndex);
//  }

  public FolderTreeNodeGui getRootNode() {
    return (FolderTreeNodeGui) getRoot();
  }
  public FolderTreeNodeGui getRootNodeById(long folderId) {
    return (FolderTreeNodeGui) getRootNode().getRootNodeById(folderId);
  }

  public FolderTreeNodeGui getRootChatNode() {
    return getRootNodeById(FolderRecord.CATEGORY_CHAT_ID);
  }
  public FolderTreeNodeGui getRootFileNode() {
    return getRootNodeById(FolderRecord.CATEGORY_FILE_ID);
  }
  public FolderTreeNodeGui getRootGroupNode() {
    return getRootNodeById(FolderRecord.CATEGORY_GROUP_ID);
  }
  public FolderTreeNodeGui getRootMsgNode() {
    return getRootNodeById(FolderRecord.CATEGORY_MAIL_ID);
  }

  public RecordFilter getFilter() {
    return filter;
  }

  /**
   * overwrite method of super
   * This sets the user object of the TreeNode identified by path and posts a node changed
   */
//  public void valueForPathChanged(TreePath path, Object newValue) {
//    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelGui.class, "valueForPathChanged(TreePath path, Object newValue)");
//    if (trace != null) trace.args(path);
//    if (trace != null) trace.args(newValue);
//
//    FolderTreeNode treeNode = (FolderTreeNode) path.getLastPathComponent();
//    FolderPair folderPair = treeNode.getFolderObject();
//
//    // editing of Root is ignored
//    if (folderPair != null) {
//
//      //FolderPair newFolderPair = folderPair;
//      String newName = ((String) newValue).trim();
//      String oldName = folderPair.getFolderShareRecord().getFolderName().trim();
//
//      if (!oldName.equals(newName) && newName.length() > 0) {
//        FolderShareRecord newFolderShare = (FolderShareRecord) folderPair.getFolderShareRecord().clone();
//        newFolderShare.setFolderName(newName);
//
//        folderPair.getFolderShareRecord().setFolderName(newName + "^");
//        super.valueForPathChanged(path, folderPair);
//
//        // keep Old description
//        String newDesc = newFolderShare.getFolderDesc();
//        FileUtilities.renameFolderAndShares(newName, newDesc, newName, newDesc, newFolderShare);
//      }
//    }
//
//    if (trace != null) trace.exit(FolderTreeModelGui.class);
//  }


  /**
   * @param folders is a non-empty array of FolderPairs that will be added to this tree model
   */
  public void addNodes(FolderPair[] folders) {
    addNodes(folders, true);
    // Java 1.4.1 screwes up in tree display if we don't reload() so do it here...
    reload();
  }
  /**
   * Adding nodes keeping each child level sorted or not
   * @param inOrder node sorting
   */
  public void addNodes(FolderPair[] folders, boolean inOrder) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelGui.class, "addNodes(FolderPair[], boolean inOrder)");
    if (trace != null) trace.args(folders);
    if (trace != null) trace.args(inOrder);

    ArrayList toAddFolderPairsL = null;
    ArrayList toRemoveFolderPairsL = null;

    // First remove the folders that are not wanted anymore from the ones for addition...
    // and gather the nodes that will be added.
    if (folders != null && folders.length > 0) {
      for (int i = 0; i<folders.length; i++) {
        FolderPair fPair = folders[i];
        if (fPair != null) {
          if (filter == null || (filter != null && filter.keep(fPair))) {
            if (inOrder) {
              if (toAddFolderPairsL == null) toAddFolderPairsL = new ArrayList();
              toAddFolderPairsL.add(fPair);
            } else {
              addNode(fPair, inOrder);
            }
          } else {
            // If folder is not to be kept, remove it, maybe it was changed it a way that its
            // no longer acceptable.
            if (inOrder) {
              if (toRemoveFolderPairsL == null) toRemoveFolderPairsL = new ArrayList();
              toRemoveFolderPairsL.add(fPair);
            } else {
              removeRecord(fPair.getFolderRecord(), true);
            }
          }
        }
      }

      // Order the list of additions and removals and do those operation in order to minimize
      // the observer's tree chaos (expansions and structural changes and jumping nodes)
      if (inOrder) {
        orderedProcess_AddOrRemove(true, toAddFolderPairsL);
        orderedProcess_AddOrRemove(false, toRemoveFolderPairsL);
      }
    }

    if (trace != null) trace.exit(FolderTreeModelGui.class);
  }

  private void orderedProcess_AddOrRemove(boolean toAdd, List folderPairs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelGui.class, "orderedProcess_AddOrRemove(boolean toAdd, List folderPairs)");
    if (trace != null) trace.args(toAdd);
    if (trace != null) trace.args(folderPairs);

    if (folderPairs != null && folderPairs.size() > 0) {
      FolderTreeNodeGui tempRoot = new FolderTreeNodeGui();
      FolderTreeModelGui tempModel = new FolderTreeModelGui(tempRoot);
      FolderPair[] fPairs = (FolderPair[]) ArrayUtils.toArray(folderPairs, FolderPair.class);
      tempModel.addNodes(fPairs, false); // unordered addition to avoid infinite recursion
      Enumeration enm = null;
      if (toAdd) {
        enm = tempRoot.preorderEnumeration();
      } else {
        enm = tempRoot.depthFirstEnumeration();
      }
      while (enm.hasMoreElements()) {
        FolderTreeNodeGui node = (FolderTreeNodeGui) enm.nextElement();
        FolderPair fPair = node.getFolderObject();
        if (fPair != null && !fPair.equals(tempRoot.getFolderObject())) {
          if (toAdd) {
            addNode(fPair);
          } else {
            removeRecord(fPair.getFolderRecord(), true);
          }
        }
      }

    }

    if (trace != null) trace.exit(FolderTreeModelGui.class);
  }


  /** Add just one FolderPair to this tree
   * If folder already exists in the tree than it is merged, if the parentNode changes, it is moved
   * otherwise, it is added to new parent node or to the root
   */
  private synchronized void addNode(FolderPair folderPair) {
    addNode(folderPair, true);
  }
  private synchronized void addNode(FolderPair folderPair, boolean inOrder) {
    addNode(folderPair, inOrder, false);
  }
  private synchronized void addNode(FolderPair folderPair, boolean inOrder, boolean suppressReccur) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelGui.class, "addNode(FolderPair, inOrder, suppressReccur)");
    if (trace != null) trace.args(folderPair);
    if (trace != null) trace.args(inOrder);
    if (trace != null) trace.args(suppressReccur);

    FolderRecord fRec = folderPair.getFolderRecord();

    // Folders are added according to therir parent child relationship (using parent id then viewParentId).
    // To try keeping shared folders tree the same between users, depending if we have access to parent folder, determine who is the real view parent...
    Long folderId = fRec.folderId;
    Long parentId = folderPair.getFileViewParentId();

    // find relavent nodes in the tree
    FolderTreeNodeGui prevNode = findNode(folderId, true);
    FolderTreeNodeGui parentNode = findNode(parentId, true);

    if (parentNode == null || parentNode == prevNode) {
      parentNode = getRootNode();
      if (trace != null) trace.data(5, "Adding folder to ROOT!");
      if (fRec.getId().longValue() >= 0 || fRec.isLocalFileType()) {
        if (fRec.isFileType() || fRec.isLocalFileType()) {
          parentNode = getRootFileNode();
        } else if (fRec.isChatting()) {
          parentNode = getRootChatNode();
        } else if (fRec.isMailType()) {
          parentNode = getRootMsgNode();
        } else if (fRec.isGroupType()) {
          parentNode = getRootGroupNode();
        }
      }
    }
    FolderTreeNode oldParent = null;
    Long oldParentId = null;

    /* if node exists in the tree already, then merge it */
    if (prevNode != null) {
      boolean structureChanged = false;
      oldParent = (FolderTreeNode) prevNode.getParent();
      // if old parent is root, old parent Id is folder's id
      if (oldParent == getRootNode()) {
        oldParentId = folderId;
      }
      else {
        oldParentId = oldParent.getFolderObject().getFolderRecord().getId();
      }

      // if parent ID has changed -- move the tree branch to new parent
      if (!oldParentId.equals(parentId)) {
        // live chatting folders have dynamic names so always remove/insert them to keep proper sort ordering
        if (!fRec.isDynamicName() &&
                ((oldParentId.longValue() < 0 && folderPair.isViewRoot()) ||
                (oldParentId.equals(folderPair.getFileViewParentId())))
                ) {
          // skip change because old parent was a Category folder and new parent is ROOT too, so nothing changed
        } else {
          // if oldParent is ancestor to prevNode AND parentNode is not descendant of prevNode
          if (prevNode.isNodeAncestor(oldParent) && !prevNode.isNodeDescendant(parentNode)) {
            removeNodeFromParent(prevNode, false);
            insertNodeInto(prevNode, parentNode, inOrder ? parentNode.getInsertionIndex(folderPair) : 0);
            structureChanged = true;
          } else {
            if (trace != null) trace.data(10, "WARNING: illegal node position -- structure change ignored!");
            if (trace != null) trace.data(11, "oldParent", oldParent);
            if (trace != null) trace.data(12, "prevNode", prevNode);
            if (trace != null) trace.data(13, "parentNode", parentNode);
          }
        }
      }
      nodeChanged(prevNode);
      if (structureChanged)
        reload(parentNode);
    }
    // Add it to the new parent -- automatically remove from the old spot if required.
    else {
      // node to add is newly created
      FolderTreeNodeGui newNode = new FolderTreeNodeGui(folderPair);
      insertNodeInto(newNode, parentNode, inOrder ? parentNode.getInsertionIndex(folderPair) : 0);
      // attach all root's children that have parentID = folderID to the new node
      if (!suppressReccur)
        selectAndMoveRootChildrenToNewParent(newNode);
      reload(parentNode);
    }


    if (trace != null) trace.exit(FolderTreeModelGui.class);
  }

  private boolean selectAndMoveRootChildrenToNewParent(FolderTreeNodeGui newParent) {
    boolean anyMoved = false;
    anyMoved |= selectAndMoveChildrenToNewParent(newParent, getRootNode());
    if (!newParent.getFolderObject().getFolderRecord().isCategoryType()) {
      anyMoved |= selectAndMoveChildrenToNewParent(newParent, getRootFileNode());
      anyMoved |= selectAndMoveChildrenToNewParent(newParent, getRootChatNode());
      anyMoved |= selectAndMoveChildrenToNewParent(newParent, getRootMsgNode());
      anyMoved |= selectAndMoveChildrenToNewParent(newParent, getRootGroupNode());
    }
    return anyMoved;
  }

  /** Takes all children of <code> moveFromParent </code> that parentIds match id of
    * <code> newParent </code> and move them to the new parent
    * Also adds all root none-category folders to a new parent category folder.
    * This method creates the appropriate events for you.
    * @return true if any nodes have been moved
    */
  private boolean selectAndMoveChildrenToNewParent(FolderTreeNodeGui newParent, FolderTreeNodeGui moveFromParent) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelGui.class, "moveChildrenToNewParent(FolderTreeNodeGui newParent, FolderTreeNodeGui moveFromParent)");
    if (trace != null) trace.args(newParent);
    if (trace != null) trace.args(moveFromParent);

    FolderRecord newParentFolder = newParent.getFolderObject().getFolderRecord();
    Long newParentId = newParentFolder.getId();

    ArrayList nodesToMove = new ArrayList();

    // gather nodes to be moved... (so we don't invalidate the Enumerateion
    int childCount = moveFromParent.getChildCount();
    for (int i=0; i<childCount; i++) {
      FolderTreeNodeGui nextChild = (FolderTreeNodeGui) moveFromParent.getChildAt(i);
      FolderPair fPair = nextChild.getFolderObject();
      FolderRecord folder = fPair.getFolderRecord();

      // include all folders those parent should be the newParent, exclude folders of the same id as newParentId
      if (folder != null) {
        if (!folder.isCategoryType()) {
          boolean move = false;
          Long viewParentId = fPair.getFileViewParentId();
          if (newParentId.equals(viewParentId) && !(newParentId.equals(folder.folderId)))
            move = true;
          else if (newParentFolder.isCategoryType() && moveFromParent == getRootNode()) {
            if (newParentFolder.folderType.shortValue() == FolderRecord.CATEGORY_FILE_FOLDER && (folder.isFileType() || folder.isLocalFileType()))
              move = true;
            else if (newParentFolder.folderType.shortValue() == FolderRecord.CATEGORY_CHAT_FOLDER && folder.isChatting())
              move = true;
            else if (newParentFolder.folderType.shortValue() == FolderRecord.CATEGORY_MAIL_FOLDER && folder.isMailType())
              move = true;
            else if (newParentFolder.folderType.shortValue() == FolderRecord.CATEGORY_GROUP_FOLDER && folder.isGroupType())
              move = true;
          }
          if (move) {
            nodesToMove.add(nextChild);
          }
        }
      }
    }

    // move the nodes
    int moveSize = nodesToMove.size();
    for (int i=0; i<moveSize; i++) {
      FolderTreeNodeGui node = (FolderTreeNodeGui) nodesToMove.get(i);
      if (!node.isNodeDescendant(newParent)) {
        removeNodeFromParent(node, false);
        insertNodeInto(node, newParent, newParent.getInsertionIndex(node.getFolderObject()));
      } else {
        if (trace != null) trace.data(10, "WARNING: illegal node position -- structure change ignored!");
        if (trace != null) trace.data(11, "node (being moved)", node);
        if (trace != null) trace.data(12, "newParent", newParent);
        if (trace != null) trace.data(13, "this (oldParent)", this);
      }
    }

    boolean anyMoved = moveSize > 0;
    if (trace != null) trace.exit(FolderTreeModelGui.class, anyMoved);
    return anyMoved;
  }


  /** @param folder is a FolderRecord that will be removed from this tree model
   * <code> folder </code> cannnot be null
   * Note: only removal of FolderRecords is supported, and not FolderShares alone
   */
  public synchronized boolean removeRecord(FolderRecord folder, boolean keepCacheResidantChildren) {
    Trace trace = null; if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelGui.class, "removeNodeFromModel(FolderRecord, boolean keepCacheResidantChildren)");
    if (trace != null) trace.args(folder);
    if (trace != null) trace.args(keepCacheResidantChildren);

    FolderTreeNodeGui nodeToRemove = findNode(folder.getId(), true);

    if (nodeToRemove != null) {

      // Remember the children nodes which still exist in the cache,
      // if the 'keepCacheResidantChildren' flag is specified.
      ArrayList keepChildrenL = null;
      if (keepCacheResidantChildren) {
        Enumeration enm = nodeToRemove.postorderEnumeration(); // all descending children
        if (enm != null && enm.hasMoreElements()) {
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          while (enm.hasMoreElements()) {
            FolderTreeNodeGui childNode = (FolderTreeNodeGui) enm.nextElement();
            FolderPair fPair = childNode.getFolderObject();
            if (cache.getFolderRecord(fPair.getId()) != null) {
              if (keepChildrenL == null) keepChildrenL = new ArrayList();
              keepChildrenL.add(fPair);
            }
          }
        }
      }

      removeNodeFromParent(nodeToRemove, true);

      // if we have kept some children, add them back to the tree...
      if (keepCacheResidantChildren) {
        FolderPair[] keepChildren = (FolderPair[]) ArrayUtils.toArray(keepChildrenL, FolderPair.class);
        addNodes(keepChildren);
      }
    }

    if (trace != null) trace.exit(FolderTreeModelGui.class, nodeToRemove != null);
    return nodeToRemove != null;
  }

  public synchronized boolean removeRecords(FolderRecord[] folders, boolean keepCacheResidantChildren) {
    boolean anyRemoved = false;
    for (int i = 0; i<folders.length; i++) {
      anyRemoved |= removeRecord(folders[i], keepCacheResidantChildren);
    }
    return anyRemoved;
  }


  /** @Return a tree path of <code> folderPair </code> in the tree **/
  public TreePath getPathToRoot(FolderPair folderPair) {
    return getPathToRoot(folderPair.getFolderRecord());
  }
  /** @Return a tree path of <code> folderRecord </code> in the tree **/
  public TreePath getPathToRoot(FolderRecord folderRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelGui.class, "getPathToRoot(FolderRecord folderRecord)");

    FolderTreeNodeGui node = findNode(folderRecord.folderId, true);
    TreePath treePath = null;
    if (node != null)
      treePath = new TreePath(getPathToRoot(node));

    if (trace != null) trace.exit(FolderTreeModelGui.class, treePath);
    return treePath;
  }


  /** @return a brand new filtered tree model with nodes accepted by the <code> filter </code> only
   * User Objects are the same, nodes are newly instantiated.
   * Uses <code> treeModel </code> as a original model
   */
  public synchronized FolderTreeModelGui createFilteredModel(RecordFilter filter, FolderTreeModelGui newModel) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelGui.class, "createFilteredModel(RecordFilter filter, FolderTreeModelCl newModel)");
    if (trace != null) trace.args(filter, newModel);

    FolderTreeNodeGui rootNode = (FolderTreeNodeGui) getRootNode();

    /* add chosen folderPairs */
    FolderTreeNodeGui newRoot = new FolderTreeNodeGui(rootNode.getFolderObject()); // <-- expect NULL FolderPair
    newModel.setRoot(newRoot);

    createFilteredModel_copyStructure(rootNode, newRoot, newModel.folderNodesHM, filter != null, filter);

    if (trace != null) trace.exit(FolderTreeModelGui.class, newModel);
    return newModel;
  }


  private void createFilteredModel_copyStructure(FolderTreeNodeGui fromNode, FolderTreeNodeGui toNode, HashMap toNodeHM, boolean filterOn, RecordFilter filter) {
    int count = fromNode.getChildCount();
    for (int i=0; i<count; i++) {
      FolderTreeNodeGui child = (FolderTreeNodeGui) fromNode.getChildAt(i);
      FolderPair folderPair = child.getFolderObject();
      if (filterOn == false || filter.keep(folderPair)) {
        FolderTreeNodeGui newNode = new FolderTreeNodeGui(folderPair);
        toNode.add(newNode);
        toNodeHM.put(folderPair.getId(), newNode);
        createFilteredModel_copyStructure(child, newNode, toNodeHM, filterOn, filter);
      }
    }
  }


  /**
   * @return a node that has the specified folderId/shareId.
   */
  public synchronized FolderTreeNodeGui findNode(Long id, boolean isFolderId) {
    FolderTreeNodeGui node = (FolderTreeNodeGui) folderNodesHM.get(id);
    // if node found, see if it is the up-to-date node of this tree
    if (node != null) {
      FolderTreeNode rootNode = node.getRootNode();
      FolderTreeNode modelRoot = getRootNode();
      if (rootNode != modelRoot) {
        // remove old node
        folderNodesHM.remove(id);
        // find it using the tree traversal method
        node = (FolderTreeNodeGui) FolderTreeNode.findNode(id, isFolderId, getRootNode());
        // cache the new node
        if (node != null)
          folderNodesHM.put(node.getFolderObject().getId(), node);
      }
    }
    return node;
  }

  private void insertNodeInto(FolderTreeNodeGui newChild, FolderTreeNodeGui parent, int index) {
    folderNodesHM.put(newChild.getFolderObject().getId(), newChild);
    // Cache the new parent Id in child's view hierarchy so cache queries can find
    //children by view (case of parent not being availble when only child folder
    //is available through granted share)
    if (parent.getFolderObject() != null)
      newChild.getFolderObject().getFolderShareRecord().guiViewParentId = parent.getFolderObject().getId();
    super.insertNodeInto(newChild, parent, index);
  }

  public synchronized void removeNodeFromParent(FolderTreeNodeGui node, boolean removeChildrenFromHT) {
    folderNodesHM.remove(node.getFolderObject().getId());
    if (removeChildrenFromHT) {
      Enumeration enm = node.depthFirstEnumeration();
      while (enm.hasMoreElements()) {
        FolderTreeNodeGui n = (FolderTreeNodeGui) enm.nextElement();
        folderNodesHM.remove(n.getFolderObject().getId());
      }
    }
    super.removeNodeFromParent(node);
  }

  /**
   * @return synchronization object for compound tree manipulations.
   */
  public Object getMonitor() {
    return this;
  }

}

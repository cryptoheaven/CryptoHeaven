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

package com.CH_co.tree;

import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FolderTreeModelCo extends DefaultTreeModel {

  private RecordFilter filter = null;
  private Hashtable folderNodesHT = new Hashtable();

  /** Creates new FolderTreeModelCo */
  public FolderTreeModelCo() {
    this(new FolderTreeNode());
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelCo.class, "FolderTreeModel()");
    if (trace != null) trace.exit(FolderTreeModelCo.class);
  }

  /** Creates new FolderTreeModelCo with specified folder filter. */
  public FolderTreeModelCo(RecordFilter filter) {
    this(new FolderTreeNode());
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelCo.class, "FolderTreeModel(FolderFilter folderFilter)");
    this.filter = filter;
    if (trace != null) trace.exit(FolderTreeModelCo.class);
  }

  /** Creates new FolderTreeModelCo with specified folder filter. */
  public FolderTreeModelCo(RecordFilter filter, FolderPair[] initialFolderPairs) {
    this(new FolderTreeNode());
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelCo.class, "FolderTreeModel(FolderFilter folderFilter, FolderPair[] initialFolderPairs)");
    this.filter = filter;
    addNodes(initialFolderPairs);
    if (trace != null) trace.exit(FolderTreeModelCo.class);
  }

  /** Creates new FolderTreeModelCo */
  public FolderTreeModelCo(FolderTreeNode root) {
    super(root); // <-- asksAllowToHaveChildren
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelCo.class, "FolderTreeModel(FolderTreeNode root)");
    if (trace != null) trace.exit(FolderTreeModelCo.class);
  }

  /** Creates new FolderTreeModelCo */
  public FolderTreeModelCo(FolderTreeNode root, RecordFilter filter, FolderPair[] initialFolderPairs) {
    super(root); // <-- asksAllowToHaveChildren
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelCo.class, "FolderTreeModel(FolderTreeNode root, FolderFilter folderFilter, FolderPair[] initialFolderPairs)");
    this.filter = filter;
    addNodes(initialFolderPairs);
    if (trace != null) trace.exit(FolderTreeModelCo.class);
  }


  public FolderTreeNode getChildNode(FolderTreeNode parent, int childIndex) {
    return (FolderTreeNode) getChild(parent, childIndex);
  }

  public FolderTreeNode getRootNode() {
    return (FolderTreeNode) getRoot();
  }
  public FolderTreeNode getRootNodeById(long folderId) {
    FolderTreeNode rootFiles = null;
    FolderTreeNode theRoot = (FolderTreeNode) getRoot();
    rootFiles = theRoot;
    Enumeration enm = theRoot.children();
    while (enm.hasMoreElements()) {
      FolderTreeNode child = (FolderTreeNode) enm.nextElement();
      if (child.getFolderObject().getFolderRecord().folderId.longValue() == folderId) {
        rootFiles = child;
        break;
      }
    }
    return rootFiles;
  }

  public FolderTreeNode getRootChatNode() {
    return getRootNodeById(FolderRecord.CATEGORY_CHAT_ID);
  }
  public FolderTreeNode getRootFileNode() {
    return getRootNodeById(FolderRecord.CATEGORY_FILE_ID);
  }
  public FolderTreeNode getRootGroupNode() {
    return getRootNodeById(FolderRecord.CATEGORY_GROUP_ID);
  }
  public FolderTreeNode getRootMsgNode() {
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
//    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelCo.class, "valueForPathChanged(TreePath path, Object newValue)");
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
//    if (trace != null) trace.exit(FolderTreeModelCo.class);
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
   * This private function should only be called recursively when doing ordered processing
   * @param inOrder should be false when processing temporary trees to avoid infinite recurseve calls.
   */
  private void addNodes(FolderPair[] folders, boolean inOrder) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelCo.class, "addNodes(FolderPair[], boolean inOrder)");
    if (trace != null) trace.args(folders);
    if (trace != null) trace.args(inOrder);

    Vector toAddFolderPairsV = null;
    Vector toRemoveFolderPairsV = null;

    // First remove the folders that are not wanted anymore from the ones for addition...
    // and gather the nodes that will be added.
    if (folders != null && folders.length > 0) {
      for (int i = 0; i<folders.length; i++) {
        FolderPair fPair = folders[i];
        if (fPair != null) {
          if (filter == null || (filter != null && filter.keep(fPair))) {
            if (inOrder) {
              if (toAddFolderPairsV == null) toAddFolderPairsV = new Vector();
              toAddFolderPairsV.addElement(fPair);
            } else {
              addNode(fPair, inOrder);
            }
          } else {
            // If folder is not to be kept, remove it, maybe it was changed it a way that its
            // no longer acceptable.
            if (inOrder) {
              if (toRemoveFolderPairsV == null) toRemoveFolderPairsV = new Vector();
              toRemoveFolderPairsV.addElement(fPair);
            } else {
              removeRecord(fPair.getFolderRecord(), true);
            }
          }
        }
      }

      // Order the list of additions and removals and do those operation in order to minimize 
      // the GUI tree chaos (expansions and structural changes and jumping nodes)
      if (inOrder) {
        orderedProcess_AddOrRemove(true, toAddFolderPairsV);
        orderedProcess_AddOrRemove(false, toRemoveFolderPairsV);
      }
    }

    if (trace != null) trace.exit(FolderTreeModelCo.class);
  }

  private void orderedProcess_AddOrRemove(boolean toAdd, Collection folderPairs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelCo.class, "orderedProcess_AddOrRemove(boolean toAdd, Collection folderPairs)");
    if (trace != null) trace.args(toAdd);
    if (trace != null) trace.args(folderPairs);

    if (folderPairs != null && folderPairs.size() > 0) {
      FolderTreeNode tempRoot = new FolderTreeNode();
      FolderTreeModelCo tempModel = new FolderTreeModelCo(tempRoot);
      FolderPair[] fPairs = (FolderPair[]) ArrayUtils.toArray(folderPairs, FolderPair.class);
      tempModel.addNodes(fPairs, false); // unordered addition
      Enumeration enm = null;
      if (toAdd) {
        enm = tempRoot.preorderEnumeration();
      } else {
        enm = tempRoot.depthFirstEnumeration();
      }
      while (enm.hasMoreElements()) {
        FolderTreeNode node = (FolderTreeNode) enm.nextElement();
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

    if (trace != null) trace.exit(FolderTreeModelCo.class);
  }


  /** Add just one FolderPair to this tree
   * If folder already exists in the tree than it is merged, if the parentNode changes, it is moved
   * otherwise, it is added to new parent node or to the root
   */
  private synchronized void addNode(FolderPair folderPair) {
    addNode(folderPair, true);
  }
  private synchronized void addNode(FolderPair folderPair, boolean inOrder) {
    addNode(folderPair, true, false);
  }
  private synchronized void addNode(FolderPair folderPair, boolean inOrder, boolean suppressReccur) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelCo.class, "addNode(FolderPair, inOrder, suppressReccur)");
    if (trace != null) trace.args(folderPair);
    if (trace != null) trace.args(inOrder);
    if (trace != null) trace.args(suppressReccur);

    FolderRecord fRec = folderPair.getFolderRecord();

    // Folders are added according to therir parent child relationship (using parent id then viewParentId).
    // To try keeping shared folders tree the same between users, depending if we have access to parent folder, determine who is the real view parent...
    Long folderId = fRec.folderId;
    Long parentId = folderPair.getFileViewParentId();

    // find relavent nodes in the tree
    FolderTreeNode prevNode = findNode(folderId, true);
    FolderTreeNode parentNode = findNode(parentId, true);

    if (parentNode == null || parentNode == prevNode) {
      parentNode = getRootNode();
      if (trace != null) trace.data(5, "Adding folder to ROOT!");
      if (fRec.getId().longValue() >= 0 || fRec.isLocalFileType()) {
        if (fRec.isFileType() || fRec.isLocalFileType()) {
          parentNode = getRootFileNode();
        } else if (fRec.isChatting()) {
          parentNode = getRootChatNode();
        } else if (fRec.isMsgType()) {
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
            int insertionIndex = 0;
            if (inOrder) insertionIndex = parentNode.getInsertionIndex(folderPair);
            if (insertionIndex > -1) {
              insertNodeInto(prevNode, parentNode, insertionIndex);
            }
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
      FolderTreeNode newNode = new FolderTreeNode(folderPair);
      int insertionIndex = 0;
      if (inOrder) insertionIndex = parentNode.getInsertionIndex(folderPair);
      if (insertionIndex > -1) {
        insertNodeInto(newNode, parentNode, insertionIndex);
        // attach all root's children that have parentID = folderID to the new node
        if (!suppressReccur) {
          selectAndMoveRootChildrenToNewParent(newNode);
        }
        reload(parentNode);
      }
    }


    if (trace != null) trace.exit(FolderTreeModelCo.class);
  }

  private boolean selectAndMoveRootChildrenToNewParent(FolderTreeNode newParent) {
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
  private boolean selectAndMoveChildrenToNewParent(FolderTreeNode newParent, FolderTreeNode moveFromParent) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeNode.class, "moveChildrenToNewParent(FolderTreeNode newParent, FolderTreeNode moveFromParent)");
    if (trace != null) trace.args(newParent);
    if (trace != null) trace.args(moveFromParent);

    FolderRecord newParentFolder = newParent.getFolderObject().getFolderRecord();
    Long newParentId = newParentFolder.getId();

    Vector nodesToMove = new Vector();

    // gather nodes to be moved... (so we don't invalidate the Enumerateion
    int childCount = moveFromParent.getChildCount();
    for (int i=0; i<childCount; i++) {
      FolderTreeNode nextChild = (FolderTreeNode) moveFromParent.getChildAt(i);
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
            else if (newParentFolder.folderType.shortValue() == FolderRecord.CATEGORY_MAIL_FOLDER && folder.isMsgType())
              move = true;
            else if (newParentFolder.folderType.shortValue() == FolderRecord.CATEGORY_GROUP_FOLDER && folder.isGroupType())
              move = true;
          }
          if (move) {
            nodesToMove.addElement(nextChild);
          }
        }
      }
    }

    // move the nodes
    int moveSize = nodesToMove.size();
    for (int i=0; i<moveSize; i++) {
      FolderTreeNode node = (FolderTreeNode) nodesToMove.elementAt(i);
      FolderPair fPair = node.getFolderObject();
      if (!node.isNodeDescendant(newParent)) {
        removeNodeFromParent(node, false);
        int insertionIndex = newParent.getInsertionIndex(node.getFolderObject());
        if (insertionIndex > -1) {
          insertNodeInto(node, newParent, insertionIndex);
        }
      } else {
        if (trace != null) trace.data(10, "WARNING: illegal node position -- structure change ignored!");
        if (trace != null) trace.data(11, "node (being moved)", node);
        if (trace != null) trace.data(12, "newParent", newParent);
        if (trace != null) trace.data(13, "this (oldParent)", this);
      }
    }

    boolean anyMoved = moveSize > 0;
    if (trace != null) trace.exit(FolderTreeNode.class, anyMoved);
    return anyMoved;
  }

   public synchronized void removeRecord(FolderRecord folder, boolean keepCacheResidantChildren) {
     throw new IllegalStateException("removeRecord not implemented");
   }

  public synchronized void removeRecords(FolderRecord[] folders, boolean keepCacheResidantChildren) {
    for (int i = 0; i<folders.length; i++) {
      removeRecord(folders[i], keepCacheResidantChildren);
    }
  }


  /** @Return a tree path of <code> folderPair </code> in the tree **/
  public TreePath getPathToRoot(FolderPair folderPair) {
    return getPathToRoot(folderPair.getFolderRecord());
  }
  /** @Return a tree path of <code> folderRecord </code> in the tree **/
  public TreePath getPathToRoot(FolderRecord folderRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelCo.class, "getPathToRoot(FolderRecord folderRecord)");

    FolderTreeNode node = findNode(folderRecord.folderId, true);
    TreePath treePath = null;
    if (node != null)
      treePath = new TreePath(getPathToRoot(node));

    if (trace != null) trace.exit(FolderTreeModelCo.class, treePath);
    return treePath;
  }


  /** @return a brand new filtered tree model with nodes accepted by the <code> filter </code> only
   * User Objects are the same, nodes are newly instantiated.
   * Uses <code> treeModel </code> as a original model
   */
  public synchronized FolderTreeModelCo createFilteredModel(RecordFilter filter, FolderTreeModelCo newModel) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelCo.class, "createFilteredModel(RecordFilter filter, FolderTreeModelCo newModel)");
    if (trace != null) trace.args(filter, newModel);

    FolderTreeNode root = (FolderTreeNode) getRootNode();

    /* add chosen folderPairs */
    FolderTreeNode newRoot = new FolderTreeNode(root.getFolderObject()); // <-- expect NULL FolderPair
    newModel.setRoot(newRoot);

    createFilteredModel_copyStructure(root, newRoot, newModel.folderNodesHT, filter != null, filter);

    if (trace != null) trace.exit(FolderTreeModelCo.class, newModel);
    return newModel;
  }


  private void createFilteredModel_copyStructure(FolderTreeNode fromNode, FolderTreeNode toNode, Hashtable toNodeHT, boolean filterOn, RecordFilter filter) {
    int count = fromNode.getChildCount();
    for (int i=0; i<count; i++) {
      FolderTreeNode child = (FolderTreeNode) fromNode.getChildAt(i);
      FolderPair folderPair = child.getFolderObject();
      if (filterOn == false || filter.keep(folderPair)) {
        FolderTreeNode newNode = new FolderTreeNode(folderPair);
        toNode.add(newNode);
        toNodeHT.put(folderPair.getId(), newNode);
        createFilteredModel_copyStructure(child, newNode, toNodeHT, filterOn, filter);
      }
    }
  }


  /**
   * @return a node that has the specified folderId/shareId.
   */
  public FolderTreeNode findNode(Long id, boolean isFolderId) {
    FolderTreeNode node = (FolderTreeNode) folderNodesHT.get(id);
    // if node found, see if it is the up-to-date node of this tree
    if (node != null) {
      TreeNode root = node.getRoot();
      TreeNode modelRoot = getRootNode();
      if (root != modelRoot) {
        // remove old node
        folderNodesHT.remove(id);
        // find it using the tree traversal method
        node = FolderTreeNode.findNode(id, isFolderId, getRootNode());
        // cache the new node
        if (node != null)
          folderNodesHT.put(node.getFolderObject().getId(), node);
      }
    }
    return node;
  }

  public void insertNodeInto(FolderTreeNode newChild, FolderTreeNode parent, int index) {
    folderNodesHT.put(newChild.getFolderObject().getId(), newChild);
    // Cache the new parent Id in child's view hierarchy so cache queries can find 
    //children by view (case of parent not being availble when only child folder 
    //is available through granted share)
    if (parent != null && parent.getFolderObject() != null)
      newChild.getFolderObject().getFolderShareRecord().guiViewParentId = parent.getFolderObject().getId();
    super.insertNodeInto(newChild, parent, index);
  }

  public void removeNodeFromParent(FolderTreeNode node, boolean removeChildrenFromHT) {
    folderNodesHT.remove(node.getFolderObject().getId());
    if (removeChildrenFromHT) {
      Enumeration enm = node.depthFirstEnumeration();
      while (enm.hasMoreElements()) {
        FolderTreeNode n = (FolderTreeNode) enm.nextElement();
        folderNodesHT.remove(n.getFolderObject().getId());
      }
    }
    super.removeNodeFromParent(node);
  }

}
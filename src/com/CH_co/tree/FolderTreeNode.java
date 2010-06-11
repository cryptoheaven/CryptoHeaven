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
import com.CH_co.trace.Trace;

import java.util.*;

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
 * <b>$Revision: 1.5 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FolderTreeNode extends MyDefaultMutableTreeNode {

  public static FolderTreeNodeSortNameProviderI folderTreeNodeSortNameProviderI;

  /** Creates new FolderTreeNode */
  public FolderTreeNode() {
    this(null);
  }

  /** Creates new FolderTreeNode */
  public FolderTreeNode(FolderPair folderPair) {
    super(folderPair);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeNode.class, "FolderTreeNode()");
    if (trace != null) trace.exit(FolderTreeNode.class);
  }

  public FolderPair getFolderObject() {
    return (FolderPair) getUserObject();
  }

  /**
   * @return a node with <code> folderId/shareId </code> if found in this sub-tree
   * @return null if not found
   */
  public static FolderTreeNode findNode(Long id, boolean isFolderId, FolderTreeNode root) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeNode.class, "findNode(Long id, boolean isFolderId, FolderTreeNode root)");
    if (trace != null) trace.args(id);
    if (trace != null) trace.args(isFolderId);
    if (trace != null) trace.args(root);

    FolderTreeNode foundNode = null;
    if (id != null) {
      Enumeration children = root.preorderEnumeration();

      while (children.hasMoreElements()) {
        FolderTreeNode nextNode = (FolderTreeNode) children.nextElement();
        FolderPair folderPair = nextNode.getFolderObject();
        // If root, it will NOT have a folder pair!
        if (folderPair != null) {
          // If local folder, it will not have a folder record or share record
          Long foundId = null;
          if (isFolderId)
            foundId = folderPair.getId();
          else
            foundId = folderPair.getFolderShareRecord().getId();
          if (foundId.equals(id)) {
            foundNode = nextNode;
            break;
          }
        }
      } // end while
    } // end if id != null
    if (trace != null) trace.exit(FolderTreeNode.class, foundNode);
    return foundNode;
  }


  /** @return an index of where the folder should be inserted among the children
    * of <code> this parent </code>. If parent is a root, grouping of folderTypes
    * is taken into consideration. Index is calculated so that the folders are placed
    * in alphabetical order of their names.  If inserting folder of brand new type
    * the index after the last child will be returned.
    */
  public int getInsertionIndex(FolderPair folder) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeNode.class, "getInsertionIndex(FolderPair)");
    if (trace != null) trace.args(folder);

    int index = 0;

    FolderRecord folderRecord = folder.getFolderRecord();
    Short folderType = folderRecord.folderType;
    Short prevType = folderType;
    String folderName = folderTreeNodeSortNameProviderI != null ? folderTreeNodeSortNameProviderI.getSortName(folder) : folder.getMyName();
    if (folderName == null) folderName = "";
    // chat folders use "[owner]" square brackets to indicate owner, ignore this in sorting
    if (folderName.startsWith("[")) folderName = folderName.substring(1);

    Enumeration children = children();
    if (trace != null) trace.data(13, children);

    while (children.hasMoreElements()) {
      FolderPair tempFolder = ((FolderTreeNode) children.nextElement()).getFolderObject();

      String tempName = null;
      Short tempType = null;

      FolderRecord tempFolderRecord = tempFolder.getFolderRecord();
      tempType = tempFolderRecord.folderType;
      tempName = folderTreeNodeSortNameProviderI != null ? folderTreeNodeSortNameProviderI.getSortName(tempFolder) : tempFolder.getMyName();
      if (tempName == null) tempName = "";
      // chat folders use "[owner]" square brackets to indicate owner, ignore this in sorting
      if (tempName.startsWith("[")) tempName = tempName.substring(1);

      int compare = folderRecord.compareFolderType(tempFolderRecord);
      if (compare > 0) {
        break;
      } else if (compare == 0) {
        if (folderName != null && folderName.compareToIgnoreCase(tempName) < 0 && folderType.equals(tempType)) {
          break;
        } else if ((!folderType.equals(tempType)) && prevType.equals(folderType)) {
          break;
        }
      }
      index++;
      prevType = tempType;
    }

    if (trace != null) trace.exit(FolderTreeNode.class, index);
    return index;
  }


// /**
//  * @return FolderPair of parent of <code> this node </code>
//  * @return null if parent is a root or it doesn't exist
//  */
//  private FolderPair getParentFolderPair() {
//    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeNode.class, "getParentFolderPair()");
//
//    FolderPair folderPair = null;
//    FolderTreeNode parentNode = (FolderTreeNode) getParent();
//
//    if (parentNode != null)
//      folderPair = parentNode.getFolderObject();
//
//    if (trace != null) trace.exit(FolderTreeNode.class, folderPair);
//    return folderPair;
//  }
//
//
//  /**
//   * @return folderId of parent of <code> this node </code>
//   * @return -1 if parent is a root or it doesn't exist
//   */
//  public Long getParentFolderID() {
//    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeNode.class, "getParentFolderID()");
//
//    Long parentId = new Long(-1);
//    FolderPair parentFolderPair = getParentFolderPair();
//
//    if (parentFolderPair != null)
//      parentId = parentFolderPair.getFolderRecord().getId();
//
//    if (trace != null) trace.exit(FolderTreeNode.class, parentId);
//    return parentId;
//  }
//
//  /**
//   * @return shareId of parent share of <code> this node </code>
//   * @return -1 if parent is a root or it doesn't exist
//   */
//  public Long getParentShareID() {
//    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeNode.class, "getParentShareID()");
//
//    Long parentShareId = new Long(-1);
//    FolderPair parentFolderPair = getParentFolderPair();
//
//    if (parentFolderPair != null)
//      parentShareId = parentFolderPair.getFolderShareRecord().getId();
//
//    if (trace != null) trace.exit(FolderTreeNode.class, parentShareId);
//    return parentShareId;
//  }
//
//
//  /**
//   * @return an array of FolderPairs which are children of <code> this parent </code>
//   */
//  public FolderPair[] getChildrenPairs() {
//    Vector v = new Vector();
//
//    int childCount = getChildCount();
//    for (int i=0; i<childCount; i++) {
//      FolderTreeNode childNode = (FolderTreeNode) getChildAt(i);
//      FolderPair folderPair = childNode.getFolderObject();
//      if (folderPair != null)
//        v.add(folderPair);
//    }
//
//    FolderPair[] folderPairs = (FolderPair[]) ArrayUtils.toArray(v, FolderPair.class);
//
//    return folderPairs;
//  }


  public String toString() {
    return "[FolderTreeNode"
      + ": this=" + super.toString()
      + ", folderPair=" + getFolderObject()
      + "]";
  }


  /**
   * This private function should only be called recursively when doing ordered processing
   * @param inOrder should be false when processing temporary trees to avoid infinite recurseve calls.
   */
  public void addNodes(FolderPair[] folders, boolean inOrder) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeNode.class, "addNodes(FolderPair[], boolean inOrder)");
    if (trace != null) trace.args(folders);
    if (trace != null) trace.args(inOrder);

    // First remove the folders that are not wanted anymore from the ones for addition...
    // and gather the nodes that will be added.
    if (folders != null && folders.length > 0) {
      FolderTreeNode root = getRootNode();
      for (int i = 0; i<folders.length; i++) {
        FolderPair fPair = folders[i];
        if (fPair != null) {
          addNode(fPair, root, inOrder);
        }
      }
    }

    if (trace != null) trace.exit(FolderTreeNode.class);
  }

  private static synchronized void addNode(FolderPair folderPair, FolderTreeNode root, boolean inOrder) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeNode.class, "addNode(FolderPair, FolderTreeNode root, boolean inOrder)");
    if (trace != null) trace.args(folderPair, root);
    if (trace != null) trace.args(inOrder);

    FolderRecord fRec = folderPair.getFolderRecord();

    // Folders are added according to therir parent child relationship (using parent id then viewParentId).
    // To try keeping shared folders tree the same between users, depending if we have access to parent folder, determine who is the real view parent...
    Long folderId = fRec.folderId;
    Long parentId = folderPair.getFileViewParentId();

    // find relavent nodes in the tree
    FolderTreeNode prevNode = findNode(folderId, true, root);
    FolderTreeNode parentNode = findNode(parentId, true, root);

    if (parentNode == null || parentNode == prevNode) {
      parentNode = root;
      if (trace != null) trace.data(5, "Adding folder to ROOT!");
      if (fRec.getId().longValue() >= 0 || fRec.isLocalFileType()) {
        if (fRec.isFileType() || fRec.isLocalFileType()) {
          parentNode = root.getRootFileNode();
        } else if (fRec.isChatting()) {
          parentNode = root.getRootChatNode();
        } else if (fRec.isMsgType()) {
          parentNode = root.getRootMsgNode();
        } else if (fRec.isGroupType()) {
          parentNode = root.getRootGroupNode();
        }
      }
    }
    FolderTreeNode oldParent = null;
    Long oldParentId = null;

    /* if node exists in the tree already, then merge it */
    if (prevNode != null) {
      oldParent = (FolderTreeNode) prevNode.getParentNode();
      // if old parent is root, old parent Id is folder's id
      if (oldParent == root) {
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
            prevNode.removeFromParent();
            insertNodeInto(prevNode, parentNode, inOrder ? parentNode.getInsertionIndex(folderPair) : 0);
          } else {
            if (trace != null) trace.data(10, "WARNING: illegal node position -- structure change ignored!");
            if (trace != null) trace.data(11, "oldParent", oldParent);
            if (trace != null) trace.data(12, "prevNode", prevNode);
            if (trace != null) trace.data(13, "parentNode", parentNode);
          }
        }
      }
    }
    // Add it to the new parent -- automatically remove from the old spot if required.
    else {
      // node to add is newly created -- copy the runtime instance from tree root
      FolderTreeNode newNode = null;
      try {
        newNode = (FolderTreeNode) root.getClass().newInstance();
      } catch (InstantiationException ex) {
        ex.printStackTrace();
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
      }
      newNode.setUserObject(folderPair);
      insertNodeInto(newNode, parentNode, inOrder ? parentNode.getInsertionIndex(folderPair) : 0);
      // attach all root's children that have parentID = folderID to the new node
      selectAndMoveRootChildrenToNewParent(newNode, root);
    }

    if (trace != null) trace.exit(FolderTreeNode.class);
  }

  private static void insertNodeInto(FolderTreeNode newChild, FolderTreeNode parent, int index) {
    // Cache the new parent Id in child's view hierarchy so cache queries can find
    // children by view (case of parent not being availble when only child folder
    // is available through granted share)
    if (parent.getFolderObject() != null)
      newChild.getFolderObject().getFolderShareRecord().guiViewParentId = parent.getFolderObject().getId();
    parent.insert((MyMutableTreeNode) newChild, index);
  }

  private static boolean selectAndMoveRootChildrenToNewParent(FolderTreeNode newParent, FolderTreeNode root) {
    boolean anyMoved = false;
    anyMoved |= selectAndMoveChildrenToNewParent(newParent, root, root);
    if (!newParent.getFolderObject().getFolderRecord().isCategoryType()) {
      anyMoved |= selectAndMoveChildrenToNewParent(newParent, root, root);
      anyMoved |= selectAndMoveChildrenToNewParent(newParent, root, root);
      anyMoved |= selectAndMoveChildrenToNewParent(newParent, root, root);
      anyMoved |= selectAndMoveChildrenToNewParent(newParent, root, root);
    }
    return anyMoved;
  }

  /** Takes all children of <code> moveFromParent </code> that parentIds match id of
    * <code> newParent </code> and move them to the new parent
    * Also adds all root none-category folders to a new parent category folder.
    * This method creates the appropriate events for you.
    * @return true if any nodes have been moved
    */
  private static boolean selectAndMoveChildrenToNewParent(FolderTreeNode newParent, FolderTreeNode moveFromParent, FolderTreeNode root) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeNode.class, "moveChildrenToNewParent(FolderTreeNode newParent, FolderTreeNode moveFromParent, FolderTreeNode root)");
    if (trace != null) trace.args(newParent);
    if (trace != null) trace.args(moveFromParent);

    FolderRecord newParentFolder = newParent.getFolderObject().getFolderRecord();
    Long newParentId = newParentFolder.getId();

    ArrayList nodesToMove = new ArrayList();

    // gather nodes to be moved... (so we don't invalidate the Enumerateion
    int childCount = moveFromParent.getChildCount();
    for (int i=0; i<childCount; i++) {
      FolderTreeNode nextChild = (FolderTreeNode) moveFromParent.getChildNodeAt(i);
      FolderPair fPair = nextChild.getFolderObject();
      FolderRecord folder = fPair.getFolderRecord();

      // include all folders those parent should be the newParent, exclude folders of the same id as newParentId
      if (folder != null) {
        if (!folder.isCategoryType()) {
          boolean move = false;
          Long viewParentId = fPair.getFileViewParentId();
          if (newParentId.equals(viewParentId) && !(newParentId.equals(folder.folderId)))
            move = true;
          else if (newParentFolder.isCategoryType() && moveFromParent == root) {
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
            nodesToMove.add(nextChild);
          }
        }
      }
    }

    // move the nodes
    int moveSize = nodesToMove.size();
    for (int i=0; i<moveSize; i++) {
      FolderTreeNode node = (FolderTreeNode) nodesToMove.get(i);
      if (!node.isNodeDescendant(newParent)) {
        node.removeFromParent();
        insertNodeInto(node, newParent, 0);
      } else {
        if (trace != null) trace.data(10, "WARNING: illegal node position -- structure change ignored!");
        if (trace != null) trace.data(11, "node (being moved)", node);
        if (trace != null) trace.data(12, "newParent", newParent);
        if (trace != null) trace.data(13, "node parent", node.getParentNode());
      }
    }

    boolean anyMoved = moveSize > 0;
    if (trace != null) trace.exit(FolderTreeNode.class, anyMoved);
    return anyMoved;
  }


  public FolderTreeNode getRootNode() {
    return (FolderTreeNode) getRoot();
  }

  public FolderTreeNode getRootNodeById(long folderId) {
    FolderTreeNode rootNode = null;
    FolderTreeNode theRoot = (FolderTreeNode) getRoot();
    rootNode = theRoot;
    Enumeration enm = theRoot.children();
    while (enm.hasMoreElements()) {
      FolderTreeNode child = (FolderTreeNode) enm.nextElement();
      if (child.getFolderObject().getFolderRecord().folderId.longValue() == folderId) {
        rootNode = child;
        break;
      }
    }
    return rootNode;
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

}
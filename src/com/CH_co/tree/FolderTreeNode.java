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

import javax.swing.tree.DefaultMutableTreeNode;

import java.util.Enumeration;
import java.util.Vector;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;

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
public class FolderTreeNode extends DefaultMutableTreeNode {

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


 /** 
  * @return FolderPair of parent of <code> this node </code>
  * @return null if parent is a root or it doesn't exist
  */
  public FolderPair getParentFolderPair() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeNode.class, "getParentFolderPair()");

    FolderPair folderPair = null;
    FolderTreeNode parentNode = (FolderTreeNode) getParent();

    if (parentNode != null)
      folderPair = parentNode.getFolderObject();

    if (trace != null) trace.exit(FolderTreeNode.class, folderPair);
    return folderPair;
  }


  /** 
   * @return folderId of parent of <code> this node </code>
   * @return -1 if parent is a root or it doesn't exist
   */
  public Long getParentFolderID() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeNode.class, "getParentFolderID()");

    Long parentId = new Long(-1);
    FolderPair parentFolderPair = getParentFolderPair();

    if (parentFolderPair != null) 
      parentId = parentFolderPair.getFolderRecord().getId();

    if (trace != null) trace.exit(FolderTreeNode.class, parentId);
    return parentId;
  }

  /** 
   * @return shareId of parent share of <code> this node </code>
   * @return -1 if parent is a root or it doesn't exist
   */
  public Long getParentShareID() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeNode.class, "getParentShareID()");

    Long parentShareId = new Long(-1);
    FolderPair parentFolderPair = getParentFolderPair();

    if (parentFolderPair != null) 
      parentShareId = parentFolderPair.getFolderShareRecord().getId();

    if (trace != null) trace.exit(FolderTreeNode.class, parentShareId);
    return parentShareId;
  }


  /**
   * @return an array of FolderPairs which are children of <code> this parent </code> 
   */
  public FolderPair[] getChildrenPairs() {
    Vector v = new Vector();

    int childCount = getChildCount();
    for (int i=0; i<childCount; i++) {
      FolderTreeNode childNode = (FolderTreeNode) getChildAt(i);
      FolderPair folderPair = childNode.getFolderObject();
      if (folderPair != null)
        v.add(folderPair);
    }

    FolderPair[] folderPairs = (FolderPair[]) ArrayUtils.toArray(v, FolderPair.class);

    return folderPairs;
  }


  public String toString() {
    return "[FolderTreeNode"
      + ": this=" + super.toString()
      + ", folderPair=" + getFolderObject()
      + "]";
  }
}
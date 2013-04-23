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

package com.CH_gui.tree;

import com.CH_co.service.records.FolderPair;
import com.CH_co.trace.Trace;
import com.CH_co.tree.*;

import javax.swing.tree.*;

/**
* <b>Copyright</b> &copy; 2001-2012
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
*
* @author  Marcin Kurzawa
* @version
*/
public class FolderTreeNodeGui extends FolderTreeNode implements MutableTreeNode {

  /** Creates new FolderTreeNodeGui */
  public FolderTreeNodeGui() {
    this(null);
  }

  /** Creates new FolderTreeNodeGui */
  public FolderTreeNodeGui(FolderPair folderPair) {
    super(folderPair);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeNodeGui.class, "FolderTreeNodeGui()");
    if (trace != null) trace.exit(FolderTreeNodeGui.class);
  }

  public void insert(MutableTreeNode child, int index) {
    insert((MyMutableTreeNode) child, index);
  }

  public void remove(MutableTreeNode node) {
    remove((MyMutableTreeNode) node);
  }

  public void setParent(MutableTreeNode newParent) {
    setParent((MyMutableTreeNode) newParent);
  }

  public TreeNode getChildAt(int childIndex) {
    return (TreeNode) getChildNodeAt(childIndex);
  }

  public TreeNode getParent() {
    return (TreeNode) getParentNode();
  }

  public int getIndex(TreeNode node) {
    return getIndex((MyTreeNode) node);
  }

}
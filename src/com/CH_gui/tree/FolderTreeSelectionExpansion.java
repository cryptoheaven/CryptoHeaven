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

import java.util.*;
import javax.swing.tree.TreePath;

import com.CH_co.service.records.FolderPair;
import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.12 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FolderTreeSelectionExpansion extends Object {

  Vector visibleFolderIDsV;
  Vector selectedFolderIDsV;

  /** Create an empty object */
  private FolderTreeSelectionExpansion() {
    visibleFolderIDsV = new Vector();
    selectedFolderIDsV = new Vector();
  }

  /**
   * Creates new visuals object that stores current tree expansions
   * and selections.
   */
  public static FolderTreeSelectionExpansion getData(FolderTree folderTree) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeSelectionExpansion.class, "getData(FolderTree folderTree)");
    if (trace != null) trace.args(folderTree);

    FolderTreeSelectionExpansion newVisuals = new FolderTreeSelectionExpansion();

    FolderTreeModelGui model = folderTree.getFolderTreeModel();
    FolderTreeNodeGui root = model.getRootNode();
    Enumeration enm = root.depthFirstEnumeration();
    while (enm.hasMoreElements()) {
      FolderTreeNodeGui node = (FolderTreeNodeGui) enm.nextElement();
      if (node != root) {
        TreePath path = new TreePath(model.getPathToRoot(node));
        FolderPair fPair = node.getFolderObject();
        Long folderId = fPair.getFolderRecord().getId();
        if (folderTree.isVisible(path))
          newVisuals.visibleFolderIDsV.addElement(folderId);
        if (folderTree.isPathSelected(path))
          newVisuals.selectedFolderIDsV.addElement(folderId);
      }
    }

    if (trace != null) trace.exit(FolderTreeSelectionExpansion.class, newVisuals);
    return newVisuals;
  }

  /**
   * Restores visuals back onto the tree.
   */
  public void restoreData(FolderTree folderTree) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeSelectionExpansion.class, "restoreData(FolderTree folderTree)");
    if (trace != null) trace.args(folderTree);

    FolderTreeModelGui model = folderTree.getFolderTreeModel();
    for (int i=0; i<visibleFolderIDsV.size(); i++) {
      FolderTreeNodeGui node = model.findNode((Long) visibleFolderIDsV.elementAt(i), true);
      if (node != null) {
        TreePath path = new TreePath(model.getPathToRoot(node));
        if (path != null) {
          folderTree.makeVisible(path);
        }
      }
    }
    Vector selectionPathsV = new Vector();
    for (int i=0; i<selectedFolderIDsV.size(); i++) {
      Long folderId = (Long) selectedFolderIDsV.elementAt(i);
      FolderTreeNodeGui node = model.findNode(folderId, true);
      if (node != null) {
        TreePath path = new TreePath(model.getPathToRoot(node));
        if (path != null) {
          selectionPathsV.addElement(path);
        }
      }
    }
    if (selectionPathsV.size() > 0) {
      TreePath[] selectionPaths = new TreePath[selectionPathsV.size()];
      selectionPathsV.toArray(selectionPaths);
      folderTree.setSelectionPaths(selectionPaths);
    }

    if (trace != null) trace.exit(FolderTreeSelectionExpansion.class);
  }
}
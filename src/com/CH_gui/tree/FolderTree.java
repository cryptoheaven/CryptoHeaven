/*
* Copyright 2001-2013 by CryptoHeaven Corp.,
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

import com.CH_cl.service.cache.TextRenderer;
import com.CH_co.service.records.FolderPair;
import com.CH_co.service.records.filters.RecordFilter;
import com.CH_co.trace.Trace;
import com.CH_co.tree.FolderTreeNode;
import com.CH_co.tree.FolderTreeNodeSortNameProviderI;
import com.CH_co.tree.MyTreeNode;
import com.CH_co.util.DisposableObj;
import java.awt.Rectangle;
import java.util.ArrayList;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

/** 
* <b>Copyright</b> &copy; 2001-2013
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
* <b>$Revision: 1.26 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class FolderTree extends JTree implements DisposableObj {

  private boolean selectionSuppressed;

  private long lastTimeScrolledExtraHight;
  private static long DELAY_BETWEEN_SCROLLING_EXTRA_HIGHT = 250;

  static {
    FolderTreeNode.folderTreeNodeSortNameProviderI = new FolderTreeNodeSortNameProviderI() {
      public String getSortName(FolderPair fPair) {
        String sortName = TextRenderer.getFolderAndShareNamesForTreeDisplaySort(fPair);
        // If name is known/unsealed, append unique ID to prevent jumping sort positions when folders have identical names and types
        if (sortName != null && sortName.length() > 0)
          sortName += " " + fPair.getId();
        return sortName;
      }
    };
  }

  /** Creates new FolderTree */
  public FolderTree() {
    this(new FolderTreeNodeGui());
  }
  /** Creates new FolderTree */
  public FolderTree(RecordFilter filter) {
    this(new FolderTreeModelGui(filter));
  }
  /** Creates new FolderTree */
  public FolderTree(RecordFilter filter, FolderPair[] initialFolderPairs) {
    this(new FolderTreeNodeGui(), filter, initialFolderPairs);
  }

  /** Creates new FolderTree */
  public FolderTree(FolderTreeNodeGui root) {
    this(new FolderTreeModelGui(root));
  }

  /** Creates new FolderTree */
  public FolderTree(FolderTreeNodeGui root, RecordFilter filter, FolderPair[] initialFolderPairs) {
    this(new FolderTreeModelGui(root, filter, initialFolderPairs));
  }

  /** Creates new FolderTree */
  public FolderTree(FolderTreeModelGui treeModel) {
    super(treeModel);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTree.class, "FolderTree(FolderTreeNodeGui)");
    init();
    if (trace != null) trace.exit(FolderTree.class);
  }

  private void init() {
    this.setCellRenderer(new FolderTreeCellRenderer());

    /* display lines to connect folders in the tree */
    this.putClientProperty("JTree.lineStyle", "Angled");
    this.setScrollsOnExpand(true);
    this.setEditable(false);
    this.setLargeModel(true);
    this.setRowHeight(21);

    // Do not display the root of the tree
    setRootVisible(false);
    setShowsRootHandles(true);
  }


  protected void suppressSelection(boolean value) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTree.class, "suppressSelection(boolean value)");
    if (trace != null) trace.args(value);
    selectionSuppressed = value;
    if (trace != null) trace.exit(FolderTree.class);
  }
  public boolean isSelectionSuppressed() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTree.class, "isSelectionSuppressed()");
    boolean rc = selectionSuppressed;
    if (trace != null) trace.exit(FolderTree.class, rc);
    return rc;
  }

  public FolderTreeModelGui getFolderTreeModel() {
    return (FolderTreeModelGui) getModel();
  }

  public void removeTreeSelectionListeners() {
    TreeSelectionListener[] listeners = (TreeSelectionListener[]) getListeners(TreeSelectionListener.class);
    if (listeners != null && listeners.length > 0)
      for (int i=0; i<listeners.length; i++)
        removeTreeSelectionListener(listeners[i]);
  }


  /**
  * Overwrite the method from super class to get the string for the label.
  * @return string for each node label
  */
  public String convertValueToText(Object value,
                                boolean selected,
                                boolean expanded,
                                boolean leaf,
                                int row,
                                boolean hasFocus)
  {
    String nodeText = "";
    if (value instanceof FolderTreeNode) {
      FolderPair folderPair = ((FolderTreeNode)value).getFolderObject();
      if (folderPair == null) {
        nodeText = com.CH_cl.lang.Lang.rb.getString("folder_Desktop");
      } else {
        StringBuffer toolTipReturn = new StringBuffer();
        nodeText = TextRenderer.convertValueToText(folderPair, toolTipReturn);
        String toolTip = toolTipReturn.toString();
        setToolTipText(toolTip != null && toolTip.length() > 0 ? toolTip : null);
      }
    } else {
      nodeText = super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
    }

    return nodeText;
  }

  /** Shortcut to get a FolderPair of a last selected node in this tree */
  public FolderPair getLastSelectedPair() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTree.class, "getLastSelectedPair()");

    FolderPair folderPair = null;
    FolderTreeNode node = (FolderTreeNode) getLastSelectedPathComponent();

    if (node != null)
      folderPair = node.getFolderObject();

    if (trace != null) trace.exit(FolderTree.class, folderPair);
    return folderPair;
  }

  public void setSelectedFolder(Long folderId) {
    FolderTreeNode node = getFolderTreeModel().findNode(folderId, true);
    if (node != null) {
      MyTreeNode[] pathNodes = node.getPath();
      TreePath path = new TreePath(pathNodes);
      getSelectionModel().setSelectionPath(path);
    }
  }

  public void scrollPathToVisible2(TreePath path) {
    makeVisible(path);
    int row = getRowForPath(path);
    Rectangle rect = getRowBounds(row);
    long now = System.currentTimeMillis();
    if (Math.abs(now - lastTimeScrolledExtraHight) > DELAY_BETWEEN_SCROLLING_EXTRA_HIGHT) {
      lastTimeScrolledExtraHight = now;
      rect.setBounds(rect.x-40, rect.y-rect.height, 50+40, rect.height*3); // 50 width enough to see part of childrens' level
    } else {
      rect.setBounds(rect.x-40, rect.y, 50+40, rect.height);
    }
    scrollRectToVisible(rect);
  }

  /** @return an array of FolderPairs of all specified nodes in the tree */
  public static FolderPair[] getLastPathComponentFolderPairs(TreePath[] treePaths) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTree.class, "getLastPathComponentFolderPairs(TreePath[])");
    if (trace != null) trace.args(treePaths);

    ArrayList folderPairsL = new ArrayList();
    if (treePaths != null && treePaths.length > 0) {
      FolderTreeNode[] lastNodes = getLastPathComponentNodes(treePaths);

      if (lastNodes != null) {
        for (int i=0; i<lastNodes.length; i++) {
          FolderPair folderPair = lastNodes[i].getFolderObject();
          if (folderPair != null)
            folderPairsL.add(folderPair);
        }
      }
    }

    FolderPair[] folderPairs = new FolderPair[folderPairsL.size()];
    if (folderPairsL.size() > 0)
      folderPairsL.toArray(folderPairs);

    if (trace != null) trace.exit(FolderTree.class, folderPairs);
    return folderPairs;
  }

  /** @return an array of FolderPairs of all specified nodes in the tree */
  public static FolderPair getLastPathComponentFolderPair(TreePath treePath) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTree.class, "getLastPathComponentFolderPair(TreePath)");
    if (trace != null) trace.args(treePath);

    FolderPair folderPair = null;
    if (treePath != null) {
      FolderTreeNode node = (FolderTreeNode) treePath.getLastPathComponent();
      folderPair = node.getFolderObject();
    }

    if (trace != null) trace.exit(FolderTree.class, folderPair);
    return folderPair;
  }

  /** @return an array of folder ids of specified nodes which paths are given */
  public static Long[] getLastPathComponentFolderIds(TreePath[] treePaths) {
    FolderPair[] folderPairs = getLastPathComponentFolderPairs(treePaths);

    Long[] ids = new Long[folderPairs.length];

    for (int i=0; i<folderPairs.length; i++)
      ids[i] = folderPairs[i].getFolderRecord().getId();

    return ids;
  }

  /** @return an array of folder share id of selected nodes */
  public static Long[] getLastPathComponentShareIds(TreePath[] treePaths) {
    FolderPair[] folderPairs = getLastPathComponentFolderPairs(treePaths);

    Long[] shareIds = new Long[folderPairs.length];

    for (int i=0; i<folderPairs.length; i++)
      shareIds[i] = folderPairs[i].getFolderShareRecord().shareId;

    return shareIds;
  }

  /** @return FolderTreeNode[] that are at the end of specified paths */
  public static FolderTreeNode[] getLastPathComponentNodes(TreePath[] treePaths) {

    FolderTreeNode[] lastNodes = new FolderTreeNode[treePaths.length];

    for (int i=0; i<treePaths.length; i++)
      lastNodes[i] = (FolderTreeNode) treePaths[i].getLastPathComponent();

    return lastNodes;
  }


  /**
  * I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
  * Dispose the object and release resources to help in garbage collection.
  */
  public void disposeObj() {
    removeTreeSelectionListeners();
  }

}
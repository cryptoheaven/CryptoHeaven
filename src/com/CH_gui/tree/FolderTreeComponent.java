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

import com.CH_cl.tree.*;

import com.CH_co.gui.*;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.util.*;
import com.CH_co.trace.Trace;

import com.CH_gui.action.*;
import com.CH_gui.actionGui.JActionFrame;
import com.CH_gui.menuing.*;
import com.CH_gui.folder.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.StringTokenizer;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionListener;
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
 * <b>$Revision: 1.23 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FolderTreeComponent extends JPanel implements FolderSelectionListener, ToolBarProducerI, VisualsSavable, DisposableObj {

  private FolderTreeScrollPane folderTreeScrollPane;

  private JLabel jTitleLabel;
  private ToolBarModel toolBarModel;

  /**
   * Creates new FolderTreeComponent.
   * @param filter is nullable and specifies the record filter for the tree.
   */
  public FolderTreeComponent(boolean withActions) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeComponent.class, "FolderTreeComponent(boolean withActions)");
    if (trace != null) trace.args(withActions);
    this.folderTreeScrollPane = new FolderTreeScrollPane(withActions);
    init(false);
    if (trace != null) trace.exit(FolderTreeComponent.class);
  }
  /**
   * Creates new FolderTreeComponent.
   * @param filter specifies the record filter for the tree.
   */
  public FolderTreeComponent(boolean withActions, RecordFilter filter) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeComponent.class, "FolderTreeComponent(boolean withActions, RecordFilter filter)");
    if (trace != null) trace.args(withActions);
    if (trace != null) trace.args(filter);
    this.folderTreeScrollPane = new FolderTreeScrollPane(withActions, filter);
    init(false);
    if (trace != null) trace.exit(FolderTreeComponent.class);
  }
  /**
   * Creates new FolderTreeComponent.
   * @param filter specifies the record filter for the tree.
   */
  public FolderTreeComponent(boolean withActions, RecordFilter filter, FolderPair[] initialFolderPairs, boolean withExploreUtilityTool) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeComponent.class, "FolderTreeComponent(boolean withActions, RecordFilter filter, FolderPair[] initialFolderPairs, boolean withExploreUtilityTool)");
    if (trace != null) trace.args(withActions);
    if (trace != null) trace.args(filter);
    if (trace != null) trace.args(initialFolderPairs);
    if (trace != null) trace.args(withExploreUtilityTool);
    this.folderTreeScrollPane = new FolderTreeScrollPane(withActions, filter, initialFolderPairs);
    init(withExploreUtilityTool);
    if (trace != null) trace.exit(FolderTreeComponent.class);
  }

  /**
   * Creates new FolderTreeComponent.
   * Auto Fetch.
   */
  public FolderTreeComponent(FolderTree folderTree) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeComponent.class, "FolderTreeComponent()");
    this.folderTreeScrollPane = new FolderTreeScrollPane(folderTree, true);
    init(false);
    if (trace != null) trace.exit(FolderTreeComponent.class);
  }

  public FolderTreeScrollPane getFolderTreeScrollPane() {
    return folderTreeScrollPane;
  }

  public void addTreeSelectionListener(TreeSelectionListener tsl) {
    folderTreeScrollPane.getFolderTree().addTreeSelectionListener(tsl);
  }

  public void removeTreeSelectionListener(TreeSelectionListener tsl) {
    folderTreeScrollPane.getFolderTree().removeTreeSelectionListener(tsl);
  }

  public void removeTreeSelectionListeners() {
    folderTreeScrollPane.getFolderTree().removeTreeSelectionListeners();
  }

  private void init(boolean withExploreUtilityTool) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeComponent.class, "init(boolean withExploreUtilityTool)");
    if (trace != null) trace.args(withExploreUtilityTool);

    // If we don't have global from toolbars, we'll setup content toolbars
    if (!JActionFrame.ENABLE_FRAME_TOOLBARS)
      toolBarModel = initToolBarModel(MiscGui.getVisualsKeyName(this), null, null);

    // So the split panes are not limited in movement, but must have at least visible header.
    setMinimumSize(new Dimension(0, 24));
    setLayout(new GridBagLayout());
    setBorder(new EmptyBorder(0,0,0,0));

    AbstractButton refreshButton = null;
    AbstractButton cloneButton = null;
    AbstractButton exploreButton = null;
    FolderTree folderTree = folderTreeScrollPane.getFolderTree();
    if (folderTree instanceof FolderActionTree) {
      FolderActionTree actionTree = (FolderActionTree) folderTree;
      Action refreshAction = actionTree.getRefreshAction();
      Action cloneAction = actionTree.getCloneAction();
      Action exploreAction = withExploreUtilityTool ? actionTree.getExploreAction() : null;

      if (refreshAction != null)
        refreshButton = ActionUtilities.makeSmallComponentToolButton(refreshAction);
      if (cloneAction != null)
        cloneButton = ActionUtilities.makeSmallComponentToolButton(cloneAction);
      if (exploreAction != null)
        exploreButton = ActionUtilities.makeSmallComponentToolButton(exploreAction);
    }

    int posY = 0;

    jTitleLabel = new JMyLabel(com.CH_gui.lang.Lang.rb.getString("title_Folders"));
    add(jTitleLabel, new GridBagConstraints(0, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 5, 0, 5), 0, 0));
    JLabel minRowHeight = new JLabel(" ");
    add(minRowHeight, new GridBagConstraints(1, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(3, 0, 3, 0), 0, 0));
    if (cloneButton != null)
      add(cloneButton, new GridBagConstraints(2, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new MyInsets(0, 0, 0, 0), 0, 0));
    if (exploreButton != null)
      add(exploreButton, new GridBagConstraints(3, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new MyInsets(0, 0, 0, 0), 0, 0));
    if (refreshButton != null)
      add(refreshButton, new GridBagConstraints(4, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new MyInsets(0, 0, 0, 0), 0, 0));
    posY ++;

    if (toolBarModel != null) {
      add(toolBarModel.getToolBar(), new GridBagConstraints(0, posY, 5, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
      posY ++;
    }

    add(folderTreeScrollPane, new GridBagConstraints(0, posY, 5, 1, 10, 10,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));

    restoreVisuals(GlobalProperties.getProperty(MiscGui.getVisualsKeyName(this), "Dimension width 160 height 260"));
    installMouseListener();

    if (toolBarModel != null)
      toolBarModel.addComponentActions(this);

    if (trace != null) trace.exit(FolderTreeComponent.class);
  }

  private void installMouseListener() {
    /** If right mouse button is clicked then the popup is shown. */
    if (folderTreeScrollPane.getFolderTree() instanceof ActionProducerI)
      addMouseListener(new PopupMouseAdapter(this, (ActionProducerI) folderTreeScrollPane.getFolderTree()));
  }

  /***********************************************************
  *** T o o l B a r P r o d u c e r I    interface methods ***
  ***********************************************************/
  public ToolBarModel getToolBarModel() {
    return toolBarModel;
  }
  public String getToolBarTitle() {
    return jTitleLabel.getText();
  }
  public ToolBarModel initToolBarModel(String propertyKeyName, String toolBarName, Component sourceComponent) {
    if (!JActionFrame.ENABLE_FRAME_TOOLBARS && toolBarModel == null)
      toolBarModel = new ToolBarModel(propertyKeyName, toolBarName != null ? toolBarName : propertyKeyName, false);
    if (toolBarModel != null && sourceComponent != null)
      toolBarModel.addComponentActions(sourceComponent);
    return toolBarModel;
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public String getVisuals() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeComponent.class, "getVisuals()");

    StringBuffer visuals = new StringBuffer();
    visuals.append("Dimension width ");
    Dimension dim = getSize();
    visuals.append(dim.width);
    visuals.append(" height ");
    visuals.append(dim.height);

    String rc = visuals.toString();
    if (trace != null) trace.exit(FolderTreeComponent.class, rc);
    return rc;
  }

  public void restoreVisuals(String visuals) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeComponent.class, "restoreVisuals(String visuals)");
    if (trace != null) trace.args(visuals);

    try {
      StringTokenizer st = new StringTokenizer(visuals);
      st.nextToken();
      st.nextToken();
      int width = Integer.parseInt(st.nextToken());
      st.nextToken();
      int height = Integer.parseInt(st.nextToken());
      setPreferredSize(new Dimension(width, height));
    } catch (Throwable t) {
      if (trace != null) trace.exception(FolderTreeComponent.class, 100, t);
    }

    if (trace != null) trace.exit(FolderTreeComponent.class);
  }
  public String getExtension() {
    return null;
  }
  public Integer getVisualsVersion() {
    return null;
  }
  public boolean isVisuallyTraversable() {
    return true;
  }



  /**
   * I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
   * Dispose the object and release resources to help in garbage collection.
  */
  public void disposeObj() {
    removeTreeSelectionListeners();
    if (folderTreeScrollPane != null)
      folderTreeScrollPane.disposeObj();
  }


  /**
   * Tracks selection forced by the double-click on a table folder row.
   */
  public void folderSelectionChanged(FolderSelectionEvent e) {
    FolderRecord fRec = e.selectedFolderRecord;
    if (fRec != null) {
      FolderTree tree = folderTreeScrollPane.getFolderTree();
      FolderTreeModelCl model = tree.getFolderTreeModel();
      TreePath path = model.getPathToRoot(fRec);
      if (path != null)
        tree.getSelectionModel().setSelectionPath(path);
    }
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "FolderTreeComponent";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
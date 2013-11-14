/*
 * Copyright 2001-2002 by CryptoHeaven Corp.,
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

import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import com.CH_gui.actionGui.JActionFrame;
import com.CH_gui.menuing.MenuActionItem;
import com.CH_gui.menuing.MenuActionTreeCellRenderer;
import com.CH_gui.menuing.MenuTreeModel;
import com.CH_gui.util.GeneralDialog;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

/** 
 * <b>Copyright</b> &copy; 2001-2002
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * <b>$Revision: 1.4 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class MenuEditorDialog extends GeneralDialog {

  private JActionFrame jActionFrame;
  private static final int DEFAULT_CLOSE_BUTTON_INDEX = 0;

  private JTree jTree;
  private JButton jSave;
  private JButton jCancel;

  private JButton jLeft;
  private JButton jUp;
  private JButton jDown;
  private JButton jRightUp;
  private JButton jRightDown;
  private JButton jAddSeparator;
  private JButton jAddMenu;
  private JButton jRemove;

  private JTextField jName;
  private JTextField jMnemonic;
  private JTextField jShortcut;
  private Integer mnemonic;
  private KeyStroke shortcut;

  private JCheckBox jMnemonicCheck;
  private JCheckBox jShortcutCheck;

  private DefaultMutableTreeNode currentSelectionNode;
  private MenuActionItem currentMenuActionItem;

  /** Creates new MenuEditorDialog */
  public MenuEditorDialog(JActionFrame jActionFrame) {
    super(jActionFrame, "Menu Editor for: " + jActionFrame.getTitle());
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuEditorDialog.class, "MenuEditorDialog()");

    this.jActionFrame = jActionFrame;
    constructDialog(jActionFrame, jActionFrame.getMenuTreeModel());

    if (trace != null) trace.exit(MenuEditorDialog.class);
  }



  private void constructDialog(Component owner, MenuTreeModel menuTreeModel) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuEditorDialog.class, "constructDialog(Component owner, MenuTreeModel menuTreeModel)");
    if (trace != null) trace.args(owner, menuTreeModel);

    initializeComponents(menuTreeModel);
    JButton[] jButtons = createButtons();
    JPanel jPanel = createMainPanel();
    setEnabledButtons();

    super.init(owner, jButtons, jPanel, DEFAULT_CLOSE_BUTTON_INDEX, DEFAULT_CLOSE_BUTTON_INDEX);

    if (trace != null) trace.exit(MenuEditorDialog.class);
  }

  private JButton[] createButtons() {
    jSave = new JButton("Save");
    jSave.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedSave();
      }
    });

    jCancel = new JButton("Cancel");
    jCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedCancel();
      }
    });

    return new JButton[] { jSave, jCancel };
  }

  private DefaultMutableTreeNode findNode(DefaultMutableTreeNode root, Integer id) {
    DefaultMutableTreeNode foundNode = null;
    Enumeration enm = root.breadthFirstEnumeration();
    while (enm.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) enm.nextElement();
      if (((MenuActionItem) node.getUserObject()).getActionId().equals(id)) {
        foundNode = node;
        break;
      }
    }
    return foundNode;
  }

  private void initializeComponents(MenuTreeModel menuTreeModel) {

    // iterate through Actions in the model and discard leafs that are not visible
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) menuTreeModel.getTreeModel().getRoot();
    while (true) { // break out when nothing was purged
      Vector leafsToPurgeV = new Vector();
      Enumeration enm = root.depthFirstEnumeration();
      while (enm.hasMoreElements()) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) enm.nextElement();
        if (node.isLeaf()) {
          MenuActionItem action = (MenuActionItem) node.getUserObject();
          if (!action.isShowing() && !action.isGUIButtonSet())
            leafsToPurgeV.addElement(node);
        }
      }
      for (int i=0; i<leafsToPurgeV.size(); i++) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) leafsToPurgeV.elementAt(i);
        node.removeFromParent();
      }
      if (leafsToPurgeV.size() == 0)
        break;
    }

    Enumeration enm1 = root.breadthFirstEnumeration();
    DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode(root.getUserObject());
    while (enm1.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) enm1.nextElement();
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
      if (parent != null) {
        MenuActionItem parentItem = (MenuActionItem) parent.getUserObject();
        DefaultMutableTreeNode newParent = findNode(newRoot, parentItem.getActionId());
        newParent.add(new DefaultMutableTreeNode(node.getUserObject()));
      }
    }

    jTree = new JTree(new DefaultTreeModel(newRoot));
    jTree.setCellRenderer(new MenuActionTreeCellRenderer());
    jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    jTree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        currentMenuActionItem = null;
        TreePath path = jTree.getSelectionPath();
        if (path != null) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          if (node != null) {
            currentMenuActionItem = (MenuActionItem) node.getUserObject();
            currentSelectionNode = node;
            setEnabledButtons();
          }
        }
      }
    });

    // expand main menus by analyzing length of path from root to each node
    Enumeration enm2 = newRoot.breadthFirstEnumeration();
    while (enm2.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) enm2.nextElement();
      TreeNode[] pathNodes = node.getPath();
      if (pathNodes.length == 2) {
        jTree.expandPath(new TreePath(pathNodes));
      } else if (pathNodes.length > 2) {
        break;
      }
    }

    jLeft = new JButton("Left");
    jLeft.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedLeft();
      }
    });

    jUp = new JButton("Up");
    jUp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedUp();
      }
    });

    jDown = new JButton("Down");
    jDown.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedDown();
      }
    });

    jRightUp = new JButton("Up-Right");
    jRightUp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedRightUp();
      }
    });

    jRightDown = new JButton("Down-Right");
    jRightDown.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedRightDown();
      }
    });


    jAddSeparator = new JButton("Insert Separator");
    jAddSeparator.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedAddSeparator();
      }
    });

    jAddMenu = new JButton("Insert Menu");
    jAddMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedAddMenu();
      }
    });

    jRemove = new JButton("Remove");
    jRemove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedRemove();
      }
    });


    jName = new JTextField();
    jName.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
      }
      public void keyReleased(KeyEvent e) {
        textChangedName();
      }
      public void keyTyped(KeyEvent e) {
      }
    });

    jMnemonic = new JTextField();
    jMnemonic.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        textChangedMnemonic(e);
        e.consume();
      }
      public void keyReleased(KeyEvent e) {
        e.consume();
      }
      public void keyTyped(KeyEvent e) {
        e.consume();
      }
    });

    jShortcut = new JTextField();
    jShortcut.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        textChangedShortcut(e);
        e.consume();
      }
      public void keyReleased(KeyEvent e) {
        e.consume();
      }
      public void keyTyped(KeyEvent e) {
        e.consume();
      }
    });

    jMnemonicCheck = new JCheckBox("Mnemonic");
    jMnemonicCheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pressedMnemonicCheck();
      }
    });

    jShortcutCheck = new JCheckBox("Shortcut");
    jShortcutCheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pressedShortcutCheck();
      }
    });
  }


  private JPanel createMainPanel() {
    JPanel panel = new JPanel();

    panel.setLayout(new GridBagLayout());

    // main tree 5x9
    panel.add(new JScrollPane(jTree), new GridBagConstraints(0, 0, 5, 10, 20, 20,
        GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

    // Name
    panel.add(new JLabel("Name"), new GridBagConstraints(5, 0, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    panel.add(jName, new GridBagConstraints(5, 1, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

    // Mnemonic
    panel.add(jMnemonicCheck, new GridBagConstraints(5, 2, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    panel.add(jMnemonic, new GridBagConstraints(5, 3, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

    // Shortcut
    panel.add(jShortcutCheck, new GridBagConstraints(5, 4, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    panel.add(jShortcut, new GridBagConstraints(5, 5, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

    // streacher
    panel.add(new JPanel(), new GridBagConstraints(5, 6, 2, 1, 10, 10,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

    // Add Separator
    panel.add(jAddSeparator, new GridBagConstraints(5, 7, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    // Add Menu
    panel.add(jAddMenu, new GridBagConstraints(5, 8, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    // Remove
    panel.add(jRemove, new GridBagConstraints(5, 9, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

    // arrows
    // filler
    panel.add(new JPanel(), new GridBagConstraints(0, 10, 1, 3, 0, 0,
        GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    // Left
    panel.add(jLeft, new GridBagConstraints(1, 11, 1, 2, 10, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    // Up
    panel.add(jUp, new GridBagConstraints(2, 11, 1, 1, 10, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    // Down
    panel.add(jDown, new GridBagConstraints(2, 12, 1, 1, 10, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    // RightUp
    panel.add(jRightUp, new GridBagConstraints(3, 11, 1, 1, 10, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    // RightDown
    panel.add(jRightDown, new GridBagConstraints(3, 12, 1, 1, 10, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    // filler
    panel.add(new JPanel(), new GridBagConstraints(4, 10, 1, 3, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));


    return panel;
  }


  private void textChangedName() {
    currentMenuActionItem.setName(jName.getText());
  }

  private void textChangedMnemonic(KeyEvent e) {
    int modifiers = e.getModifiers();
    int keyCode = e.getKeyCode();
    if (modifiers == 0) {
      jMnemonic.setText(KeyEvent.getKeyText(keyCode));
      mnemonic = new Integer(keyCode);
      currentMenuActionItem.setMnemonic(mnemonic);
    }
  }

  private void textChangedShortcut(KeyEvent e) {
    int modifiers = e.getModifiers();
    int keyCode = e.getKeyCode();
    if ((keyCode & ~KeyEvent.VK_ALT & ~KeyEvent.CTRL_MASK & ~KeyEvent.META_MASK & ~KeyEvent.SHIFT_MASK) != 0) {
      if (modifiers != 0) {
        jShortcut.setText(KeyEvent.getKeyModifiersText(modifiers) + "+" + KeyEvent.getKeyText(keyCode));
      } else {
        jShortcut.setText(KeyEvent.getKeyText(keyCode));
      }
      shortcut = KeyStroke.getKeyStrokeForEvent(e);
      currentMenuActionItem.setKeyStroke(shortcut);
    }
  }

  private void pressedMnemonicCheck() {
    jMnemonic.setEnabled(jMnemonicCheck.isSelected());
    if (jMnemonicCheck.isSelected()) {
      currentMenuActionItem.setMnemonic(mnemonic);
    } else {
      currentMenuActionItem.setMnemonic(null);
    }
    jMnemonic.requestFocusInWindow();
  }

  private void pressedShortcutCheck() {
    jShortcut.setEnabled(jShortcutCheck.isSelected());
    if (jShortcutCheck.isSelected()) {
      currentMenuActionItem.setKeyStroke(shortcut);
    } else {
      currentMenuActionItem.setKeyStroke(null);
    }
    jShortcut.requestFocusInWindow();
  }

  private void pressedSave() {
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) jTree.getModel().getRoot();
    int childCount = root.getChildCount();
    DefaultMutableTreeNode plugins = null;
    for (int i=0; i<childCount; i++) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
      MenuActionItem item = (MenuActionItem) child.getUserObject();
      if (item.getActionId().intValue() == MenuTreeModel.PLUGINS_ID) {
        plugins = child;
        break;
      }
    }
    if (plugins == null) {
      plugins = new DefaultMutableTreeNode(new MenuActionItem("Plugins", MenuTreeModel.PLUGINS_ID, -1, -1, -1));
      root.add(plugins);
    }
    jActionFrame.reconstructMenusFromScratch((DefaultTreeModel) jTree.getModel());
    closeDialog();
  }

  private void pressedCancel() {
    closeDialog();
  }

  private void pressedLeft() {
    DefaultMutableTreeNode oldParent = (DefaultMutableTreeNode) currentSelectionNode.getParent();
    DefaultMutableTreeNode newParent = (DefaultMutableTreeNode) oldParent.getParent();
    newParent.insert(currentSelectionNode, newParent.getIndex(oldParent)+1);
    ((DefaultTreeModel) jTree.getModel()).reload(newParent);
    jTree.expandPath(new TreePath(oldParent.getPath()));
    jTree.setSelectionPath(new TreePath(currentSelectionNode.getPath()));
    setEnabledButtons();
  }

  private void pressedUp() {
    boolean anyChanged = false;
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) currentSelectionNode.getParent();
    int index = parent.getIndex(currentSelectionNode);
    if (index > 0) {
      parent.insert(currentSelectionNode, index-1);
      anyChanged = true;
    } else {
      DefaultMutableTreeNode node = currentSelectionNode;
      while (true) {
        node = node.getPreviousNode();
        if (node == null || node.isRoot())
          break;
        else if (!parent.equals(node)) {
          if (((MenuActionItem) node.getUserObject()).isMenu()) {
            node.add(currentSelectionNode);
            anyChanged = true;
            ((DefaultTreeModel) jTree.getModel()).reload(node);
            break;
          } else if (node.getParent() != null && ((MenuActionItem) ((DefaultMutableTreeNode) node.getParent()).getUserObject()).isMenu()) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
            int nodeIndex = parentNode.getIndex(node);
            parentNode.insert(currentSelectionNode, nodeIndex+1);
            anyChanged = true;
            ((DefaultTreeModel) jTree.getModel()).reload(parentNode);
            break;
          }
        }
      }
    }
    if (anyChanged) {
      ((DefaultTreeModel) jTree.getModel()).reload(parent);
      jTree.setSelectionPath(new TreePath(currentSelectionNode.getPath()));
      setEnabledButtons();
    }
  }

  private void pressedDown() {
    boolean anyChanged = false;
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) currentSelectionNode.getParent();
    int index = parent.getIndex(currentSelectionNode);
    if (index+1 < parent.getChildCount()) {
      parent.insert(currentSelectionNode, index+1);
      anyChanged = true;
    } else {
      DefaultMutableTreeNode node = currentSelectionNode;
      while (true) {
        node = node.getNextNode();
        if (node == null)
          break;
        else if (!currentSelectionNode.isNodeDescendant(node)) {
          if (((MenuActionItem) node.getUserObject()).isMenu()) {
            node.insert(currentSelectionNode, 0);
            anyChanged = true;
            ((DefaultTreeModel) jTree.getModel()).reload(node);
            break;
          } else if (node.getParent() != null && ((MenuActionItem) ((DefaultMutableTreeNode) node.getParent()).getUserObject()).isMenu()) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
            int nodeIndex = parentNode.getIndex(node);
            parentNode.insert(currentSelectionNode, nodeIndex);
            anyChanged = true;
            ((DefaultTreeModel) jTree.getModel()).reload(parentNode);
            break;
          }
        }
      }
    }
    if (anyChanged) {
      ((DefaultTreeModel) jTree.getModel()).reload(parent);
      jTree.setSelectionPath(new TreePath(currentSelectionNode.getPath()));
      setEnabledButtons();
    }
  }

  private void pressedRightUp() {
    DefaultMutableTreeNode prevSibling = currentSelectionNode.getPreviousSibling();
    prevSibling.add(currentSelectionNode);
    ((DefaultTreeModel) jTree.getModel()).reload(prevSibling.getParent());
    jTree.setSelectionPath(new TreePath(currentSelectionNode.getPath()));
    setEnabledButtons();
  }

  private void pressedRightDown() {
    DefaultMutableTreeNode nextSibling = currentSelectionNode.getNextSibling();
    nextSibling.insert(currentSelectionNode, 0);
    ((DefaultTreeModel) jTree.getModel()).reload(nextSibling.getParent());
    jTree.setSelectionPath(new TreePath(currentSelectionNode.getPath()));
    setEnabledButtons();
  }

  private void pressedAddSeparator() {
    TreePath path = jTree.getSelectionPath();
    DefaultMutableTreeNode child = (DefaultMutableTreeNode) path.getLastPathComponent();
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) child.getParent();
    if (!parent.isRoot()) {
      int index = parent.getIndex(child);
      DefaultMutableTreeNode separator = new DefaultMutableTreeNode(new MenuActionItem(MenuActionItem.STR_SEPARATOR, 0, -1, -1, -1));
      parent.insert(separator, index+1);
      ((DefaultTreeModel) jTree.getModel()).reload(parent);
      jTree.setSelectionPath(new TreePath(separator.getPath()));
      setEnabledButtons();
    }
  }

  private void pressedAddMenu() {
    TreePath path = jTree.getSelectionPath();
    DefaultMutableTreeNode child = (DefaultMutableTreeNode) path.getLastPathComponent();
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) child.getParent();
    int index = parent.getIndex(child);
    DefaultMutableTreeNode menu = new DefaultMutableTreeNode(new MenuActionItem("Menu", getAvailableMenuID(), -1, -1, -1));
    parent.insert(menu, index+1);
    ((DefaultTreeModel) jTree.getModel()).reload(parent);
    jTree.setSelectionPath(new TreePath(menu.getPath()));
    setEnabledButtons();
  }

  private void pressedRemove() {
    TreePath path = jTree.getSelectionPath();
    DefaultMutableTreeNode child = (DefaultMutableTreeNode) path.getLastPathComponent();
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) child.getParent();
    DefaultMutableTreeNode sibling = child.getPreviousSibling();
    if (sibling == null)
      sibling = child.getNextSibling();
    child.removeFromParent();
    ((DefaultTreeModel) jTree.getModel()).reload(parent);
    if (sibling != null)
      jTree.setSelectionPath(new TreePath(sibling.getPath()));
    setEnabledButtons();
  }

  private int getAvailableMenuID() {
    int nextID = 0;
    DefaultTreeModel model = (DefaultTreeModel) jTree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
    Enumeration enm = root.depthFirstEnumeration();
    Vector menuIDsV = new Vector();
    while (enm.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) enm.nextElement();
      if (node.getUserObject() != null && node.getUserObject() instanceof MenuActionItem) {
        MenuActionItem item = (MenuActionItem) node.getUserObject();
        if (item.getActionId().intValue() < 0)
          menuIDsV.addElement(item.getActionId());
      }
    }
    Integer[] menuIDs = (Integer[]) ArrayUtils.toArray(menuIDsV, Integer.class);
    Arrays.sort(menuIDs);
    for (int i=menuIDs.length-1; i>=1; i--) {
      if (menuIDs[i].intValue() - 1 > menuIDs[i-1].intValue()) {
        nextID = menuIDs[i].intValue()-1;
        break;
      }
    }
    if (nextID == 0)
      nextID = menuIDs[0].intValue()-1;
    return nextID;
  }

  private void setEnabledButtons() {
    if (currentMenuActionItem == null) {
      jName.setEnabled(false);

      jMnemonicCheck.setSelected(false);
      jMnemonicCheck.setEnabled(false);
      jMnemonic.setText("");
      jMnemonic.setEnabled(false);

      jShortcutCheck.setSelected(false);
      jShortcutCheck.setEnabled(false);
      jShortcut.setText("");
      jShortcut.setEnabled(false);

      jAddSeparator.setEnabled(false);
      jAddMenu.setEnabled(false);
      jRemove.setEnabled(false);

      jLeft.setEnabled(false);
      jUp.setEnabled(false);
      jDown.setEnabled(false);
      jRightUp.setEnabled(false);
      jRightDown.setEnabled(false);
    }
    else {
      jName.setEnabled(true);
      jName.setText(currentMenuActionItem.getLabel());
      jMnemonicCheck.setEnabled(true);
      jShortcutCheck.setEnabled(true);

      DefaultMutableTreeNode root = (DefaultMutableTreeNode) jTree.getModel().getRoot();
      DefaultMutableTreeNode node = currentSelectionNode;
      TreeNode[] path = node.getPath();
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();

      jTree.scrollPathToVisible(new TreePath(path));

      boolean isRoot = root.getUserObject() == currentMenuActionItem;
      jAddSeparator.setEnabled(!isRoot && !parent.isRoot());
      jAddMenu.setEnabled(!isRoot);
      jRemove.setEnabled(!isRoot && (currentMenuActionItem.isSeparator() || (currentMenuActionItem.isMenu() && node.isLeaf())));

      jLeft.setEnabled(path.length >= 4 || (currentMenuActionItem.isMenu() && path.length >= 3));
      jUp.setEnabled(!isRoot);
      jDown.setEnabled(!isRoot);
      jRightUp.setEnabled(!isRoot && node.getPreviousSibling() != null && ((MenuActionItem) node.getPreviousSibling().getUserObject()).isMenu());
      jRightDown.setEnabled(!isRoot && node.getNextSibling() != null && ((MenuActionItem) node.getNextSibling().getUserObject()).isMenu());

      if (currentMenuActionItem.getMnemonic() == null || currentMenuActionItem.getMnemonic().intValue() <= 0) {
        mnemonic = null;
        jMnemonicCheck.setSelected(false);
        jMnemonic.setEnabled(false);
        jMnemonic.setText("");
      }
      else {
        mnemonic = currentMenuActionItem.getMnemonic();
        jMnemonicCheck.setSelected(true);
        jMnemonic.setEnabled(true);
        jMnemonic.setText(KeyEvent.getKeyText(mnemonic.intValue()));
      }

      if (currentMenuActionItem.getKeyStroke() == null || currentMenuActionItem.getKeyStroke().getKeyCode() <= 0) {
        shortcut = null;
        jShortcutCheck.setSelected(false);
        jShortcut.setEnabled(false);
        jShortcut.setText("");
      }
      else {
        shortcut = currentMenuActionItem.getKeyStroke();
        jShortcutCheck.setSelected(true);
        jShortcut.setEnabled(true);
        jShortcut.setText(KeyEvent.getKeyModifiersText(shortcut.getModifiers()) + "+" + KeyEvent.getKeyText(shortcut.getKeyCode()));
      }
    }
  }
}
/*
 * Copyright 2001-2002 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_gui.dialog;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;

import com.CH_gui.actionGui.*;
import com.CH_gui.menuing.*;
//import com.CH_co.service.records.*;

import com.CH_co.trace.Trace;
import com.CH_co.util.GeneralDialog;

/** 
 * <b>Copyright</b> &copy; 2001-2002
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
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version 
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

  private JCheckBox jMnemonicCheck;
  private JCheckBox jShortcutCheck;

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

  private void initializeComponents(MenuTreeModel menuTreeModel) {

    jTree = new JTree(menuTreeModel.getTreeModel());
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
            setEnabledButtons();
          }
        }
      }
    });


    jLeft = new JButton("<");
    jLeft.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedLeft();
      }
    });

    jUp = new JButton("^");
    jUp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedUp();
      }
    });

    jDown = new JButton("_");
    jDown.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedDown();
      }
    });

    jRightUp = new JButton("/");
    jRightUp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedRightUp();
      }
    });

    jRightDown = new JButton("\\");
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
    panel.add(jLeft, new GridBagConstraints(1, 11, 1, 2, 0, 0, 
        GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));  
    // Up
    panel.add(jUp, new GridBagConstraints(2, 11, 1, 1, 0, 0, 
        GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));  
    // Down
    panel.add(jDown, new GridBagConstraints(2, 12, 1, 1, 0, 0, 
        GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));  
    // RightUp
    panel.add(jRightUp, new GridBagConstraints(3, 11, 1, 1, 0, 0, 
        GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));  
    // RightDown
    panel.add(jRightDown, new GridBagConstraints(3, 12, 1, 1, 0, 0, 
        GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));  
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
      jMnemonic.setText(e.getKeyText(keyCode));
      currentMenuActionItem.setMnemonic(new Integer(keyCode));
    }
  }

  private void textChangedShortcut(KeyEvent e) {
    int modifiers = e.getModifiers();
    int keyCode = e.getKeyCode();
    if ((keyCode & ~e.VK_ALT & ~e.CTRL_MASK & ~e.META_MASK & ~e.SHIFT_MASK) != 0) {
      if (modifiers != 0) {
        jShortcut.setText(e.getKeyModifiersText(modifiers) + "+" + e.getKeyText(keyCode));
      }
      else {
        jShortcut.setText(e.getKeyText(keyCode));
      }
      currentMenuActionItem.setKeyStroke(KeyStroke.getKeyStrokeForEvent(e));
    }
  }

  private void pressedMnemonicCheck() {
    jMnemonic.setEnabled(jMnemonicCheck.isSelected());
    if (!jMnemonicCheck.isSelected()) {
      currentMenuActionItem.setMnemonic(null);
    }
  }

  private void pressedShortcutCheck() {
    jShortcut.setEnabled(jShortcutCheck.isSelected());
    if (!jShortcutCheck.isSelected()) {
      currentMenuActionItem.setKeyStroke(null);
    }
  }

  private void pressedSave() {
    jActionFrame.reconstructMenusFromScratch();
    closeDialog();
  }

  private void pressedCancel() {
    closeDialog();
  }

  private void pressedLeft() {
  }

  private void pressedUp() {
  }

  private void pressedDown() {
  }

  private void pressedRightUp() {
  }

  private void pressedRightDown() {
  }

  private void pressedAddSeparator() {
  }

  private void pressedAddMenu() {
  }

  private void pressedRemove() {
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

      DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) jTree.getModel().getRoot();
      boolean isRoot = rootNode.getUserObject() == currentMenuActionItem;
      jAddSeparator.setEnabled(!isRoot);
      jAddMenu.setEnabled(!isRoot);
      jRemove.setEnabled(!isRoot);

      if (currentMenuActionItem.getMnemonic() == null || currentMenuActionItem.getMnemonic().intValue() <= 0) {
        jMnemonicCheck.setSelected(false);
        jMnemonic.setEnabled(false);
        jMnemonic.setText("");
      }
      else {
        jMnemonicCheck.setSelected(true);
        jMnemonic.setEnabled(true);
        jMnemonic.setText(KeyEvent.getKeyText(currentMenuActionItem.getMnemonic().intValue()));
      }

      if (currentMenuActionItem.getKeyStroke() == null || currentMenuActionItem.getKeyStroke().getKeyCode() <= 0) {
        jShortcutCheck.setSelected(false);
        jShortcut.setEnabled(false);
        jShortcut.setText("");
      }
      else {
        jShortcutCheck.setSelected(true);
        jShortcut.setEnabled(true);
        KeyStroke keyStroke = currentMenuActionItem.getKeyStroke();
        String label = KeyEvent.getKeyModifiersText(keyStroke.getModifiers()) + "+" + KeyEvent.getKeyText(keyStroke.getKeyCode());
        jShortcut.setText(label);
      }

    }
  }
}
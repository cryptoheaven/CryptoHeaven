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

package com.CH_gui.gui;

import com.CH_co.util.ImageNums;
import com.CH_gui.util.*;
import comx.Tiger.gui.*;
import comx.tig.en.SingleTigerSession;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;

import sferyx.administration.editors.HTMLEditor;

/**
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class MyHTMLEditor extends HTMLEditor {

  private JComponent actionsPanel = null;

  public MyHTMLEditor(boolean isSimplified, boolean suppressSpellCheck) {
    super(false, false, true, false, false, true);
    if (!suppressSpellCheck)
      enableSpeller(getInternalJEditorPane());

    setSingleParagraphSpacing(true);
//    setRemovedToolbarItems("newFileButton, openFileButton, saveFileButton, "
//            +"saveToolbarSeparator, zoomoutTextButton, zoominTextButton, "
//            +"pdfExportButton, imageMapRectButton, imageMapCircleButton, imageMapPolyButton, styleClasses, "
//            +"printFileButton, printToolbarSeparator, copyButton, cutButton, pasteButton, pasteToolbarSeparator, "
//            +"tableBtn, tableToolbarSeparator, undoButton, redoButton, "
//            +"headingStyles, fontsList, fontSizes, showParagraphsButton"
//            );

    setBorder(new EmptyBorder(0,0,0,0));
    getInternalJEditorPane().setBorder(new EmptyBorder(3,3,3,3));

    HTML_ClickablePane.setBaseToDefault((HTMLDocument) getInternalJEditorPane().getDocument());

    if (isSimplified)
      initToolBarSimple();
    else
      initToolBarExtended();
    setToolBarVisible(false);
  }

  public JComponent getActionsPanel() {
    return actionsPanel;
  }

  private void initToolBarExtended() {
    final AbstractButton[] actionsTop = new AbstractButton[18];
    final AbstractButton[] actionsBot = new AbstractButton[17];

    Component[] editComps = getEditingToolBar().getComponents();
    for (int i=0; i<editComps.length; i++) {
      Component comp = editComps[i];
      if (comp instanceof AbstractButton) {
        AbstractButton ab = (AbstractButton) comp;
        String command = ab.getActionCommand();
        ab.setToolTipText(command);
        if (command.equals("find-action"))
          actionsTop[0] = ab;
        else if (command.equals("replace-action"))
          actionsTop[1] = ab;
        else if (command.equals("insert-image"))
          actionsTop[2] = ab;
        else if(command.equals("hyperlink-properties"))
          actionsTop[3] = ab;
        else if (command.equals("insert-symbol"))
          actionsTop[4] = ab;
        else if (command.equals("copy-style"))
          actionsTop[5] = ab;
        else if (command.equals("table-insert"))
          actionsTop[6] = ab;
        else if (command.equals("table-insert-row"))
          actionsTop[7] = ab;
        else if (command.equals("table-insert-column"))
          actionsTop[8] = ab;
        else if (command.equals("table-cell-delete"))
          actionsTop[9] = ab;
        else if (command.equals("table-select"))
          actionsTop[10] = ab;
        else if (command.equals("table-column-select"))
          actionsTop[11] = ab;
        else if (command.equals("table-row-select"))
          actionsTop[12] = ab;
        else if (command.equals("table-cell-select"))
          actionsTop[13] = ab;
        else if (command.equals("table-cell-split"))
          actionsTop[14] = ab;
        else if (command.equals("table-cell-merge"))
          actionsTop[15] = ab;
        else if (command.equals("table-properties"))
          actionsTop[16] = ab;
        else if (command.equals("cell-properties"))
          actionsTop[17] = ab;
      }
    }

    Component[] formatComps = getFormattingToolBar().getComponents();
    for (int i=0; i<formatComps.length; i++) {
      Component comp = formatComps[i];
      if (comp instanceof AbstractButton) {
        AbstractButton ab = (AbstractButton) comp;
        String command = ab.getActionCommand();
        ab.setToolTipText(command);
        if (command.equals("font-properties"))
          actionsBot[0] = ab;
        else if (command.equals("font-bold"))
          actionsBot[1] = ab;
        else if (command.equals("font-italic"))
          actionsBot[2] = ab;
        else if (command.equals("font-underline"))
          actionsBot[3] = ab;
        else if (command.equals("strikethrough"))
          actionsBot[4] = ab;
        else if (command.equals("subscript"))
          actionsBot[5] = ab;
        else if (command.equals("superscript"))
          actionsBot[6] = ab;
        else if (command.equals("left-justify"))
          actionsBot[7] = ab;
        else if (command.equals("center-justify"))
          actionsBot[8] = ab;
        else if (command.equals("right-justify"))
          actionsBot[9] = ab;
        else if (command.equals("align-justify"))
          actionsBot[10] = ab;
        else if (command.equals("increase-indent"))
          actionsBot[11] = ab;
        else if (command.equals("decrease-indent"))
          actionsBot[12] = ab;
        else if (command.equals("insert-ordered-list"))
          actionsBot[13] = ab;
        else if (command.equals("insert-unordered-list"))
          actionsBot[14] = ab;
        else if (command.equals("font-foreground"))
          actionsBot[15] = ab;
        else if (command.equals("font-background"))
          actionsBot[16] = ab;
      }
    }

    JToolBar actionsBarTop = new JToolBar();
    actionsBarTop.setFloatable(false);
    actionsBarTop.setBorder(new EmptyBorder(0,0,0,0));
    JToolBar actionsBarBot = new JToolBar();
    actionsBarBot.setFloatable(false);
    actionsBarBot.setBorder(new EmptyBorder(0,0,0,0));

    actionsBarTop.add(makeDelegatingButton("Find", ImageNums.EDITOR_FIND, actionsTop, 0));
    actionsBarTop.add(makeDelegatingButton("Replace", ImageNums.EDITOR_REPLACE, actionsTop, 1));
    actionsBarTop.add(makeSeparator());
    actionsBarTop.add(makeDelegatingButton("Insert image", ImageNums.EDITOR_IMAGE, actionsTop, 2));
    actionsBarTop.add(makeDelegatingButton("Insert link", ImageNums.EDITOR_LINK, actionsTop, 3));
    actionsBarTop.add(makeDelegatingButton("Insert symbol", -1, actionsTop, 4));
    actionsBarTop.add(makeDelegatingButton("Copy formatting", ImageNums.EDITOR_COPY_FORMATTING, actionsTop, 5));
    actionsBarTop.add(makeSeparator());
    actionsBarTop.add(makeDelegatingButton("Insert table", ImageNums.EDITOR_T_INSERT, actionsTop, 6));
    actionsBarTop.add(makeDelegatingButton("Insert row", ImageNums.EDITOR_T_INSERT_ROW, actionsTop, 7));
    actionsBarTop.add(makeDelegatingButton("Insert column", ImageNums.EDITOR_T_INSERT_COLUMN, actionsTop, 8));
    actionsBarTop.add(makeSeparator());
    actionsBarTop.add(makeDelegatingButton("Delete table cell", ImageNums.EDITOR_T_DELETE_CELL, actionsTop, 9));
    actionsBarTop.add(makeDelegatingButton("Select table", ImageNums.EDITOR_T_SELECT, actionsTop, 10));
    actionsBarTop.add(makeDelegatingButton("Select column", ImageNums.EDITOR_T_SELECT_COLUMN, actionsTop, 11));
    actionsBarTop.add(makeDelegatingButton("Select row", ImageNums.EDITOR_T_SELECT_ROW, actionsTop, 12));
    actionsBarTop.add(makeDelegatingButton("Select cell", ImageNums.EDITOR_T_SELECT_CELL, actionsTop, 13));
    actionsBarTop.add(makeSeparator());
    actionsBarTop.add(makeDelegatingButton("Split cell", ImageNums.EDITOR_T_SPLIT, actionsTop, 14));
    actionsBarTop.add(makeDelegatingButton("Merge cells", ImageNums.EDITOR_T_MERGE, actionsTop, 15));
    actionsBarTop.add(makeDelegatingButton("Table properties", ImageNums.EDITOR_T_PROPERTIES, actionsTop, 16));
    actionsBarTop.add(makeDelegatingButton("Cell properties", ImageNums.EDITOR_T_PROPERTIES_CELL, actionsTop, 17));

    actionsBarBot.add(makeDelegatingButton("Change font", ImageNums.EDITOR_FONTS, actionsBot, 0));
    actionsBarBot.add(makeSeparator());
    actionsBarBot.add(makeDelegatingToggleButton("Bold", ImageNums.EDITOR_BOLD, actionsBot, 1));
    actionsBarBot.add(makeDelegatingToggleButton("Italic", ImageNums.EDITOR_ITALIC, actionsBot, 2));
    actionsBarBot.add(makeDelegatingToggleButton("Underline", ImageNums.EDITOR_UNDERLINE, actionsBot, 3));
    actionsBarBot.add(makeDelegatingToggleButton("Strikethrough", ImageNums.EDITOR_STRIKE, actionsBot, 4));
    actionsBarBot.add(makeDelegatingToggleButton("Subscript", ImageNums.EDITOR_SUBSCRIPT, actionsBot, 5));
    actionsBarBot.add(makeDelegatingToggleButton("Superscript", ImageNums.EDITOR_SUPERSCRIPT, actionsBot, 6));
    actionsBarBot.add(makeSeparator());
    actionsBarBot.add(makeDelegatingButton("Left", ImageNums.EDITOR_LEFT, actionsBot, 7));
    actionsBarBot.add(makeDelegatingButton("Center", ImageNums.EDITOR_CENTER, actionsBot, 8));
    actionsBarBot.add(makeDelegatingButton("Right", ImageNums.EDITOR_RIGHT, actionsBot, 9));
    actionsBarBot.add(makeDelegatingButton("Align", ImageNums.EDITOR_ALIGN, actionsBot, 10));
    actionsBarBot.add(makeSeparator());
    actionsBarBot.add(makeDelegatingButton("Increase indent", ImageNums.EDITOR_INDENT_I, actionsBot, 11));
    actionsBarBot.add(makeDelegatingButton("Decrease indent", ImageNums.EDITOR_INDENT_D, actionsBot, 12));
    actionsBarBot.add(makeDelegatingButton("Insert ordered list", ImageNums.EDITOR_LIST_O, actionsBot, 13));
    actionsBarBot.add(makeDelegatingButton("Insert unordered list", ImageNums.EDITOR_LIST_U, actionsBot, 14));
    actionsBarBot.add(makeSeparator());
    actionsBarBot.add(makeDelegatingButton("Font color", ImageNums.EDITOR_COLOR, actionsBot, 15));
    actionsBarBot.add(makeDelegatingButton("Highlight", ImageNums.EDITOR_HIGHLIGHT, actionsBot, 16));

    JMyButtonNoFocus jEmoticons = new JMyButtonNoFocus(Images.get(ImageNums.EDITOR_EMOTICONS));
    jEmoticons.setToolTipText("Emoticons");
    jEmoticons.setBorder(new EmptyBorder(2,2,2,2));
    jEmoticons.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        insertImage(MyHTMLEditor.this, event);
      }
    });
    actionsBarBot.add(jEmoticons);

    actionsPanel = new JPanel(new GridBagLayout());
    actionsPanel.add(actionsBarTop, new GridBagConstraints(0, 0, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
    actionsPanel.add(actionsBarBot, new GridBagConstraints(0, 1, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
  }

  private void initToolBarSimple() {
    final AbstractButton[] actions = new AbstractButton[7];

    Component[] editComps = getEditingToolBar().getComponents();
    for (int i=0; i<editComps.length; i++) {
      Component comp = editComps[i];
      if (comp instanceof AbstractButton) {
        AbstractButton ab = (AbstractButton) comp;
        String command = ab.getActionCommand();
        if (command.equals("hyperlink-properties"))
          actions[6] = ab;
      }
    }
    Component[] formatComps = getFormattingToolBar().getComponents();
    for (int i=0; i<formatComps.length; i++) {
      Component comp = formatComps[i];
      if (comp instanceof AbstractButton) {
        AbstractButton ab = (AbstractButton) comp;
        String command = ab.getActionCommand();
        if (command.equals("font-properties"))
          actions[0] = ab;
        else if (command.equals("font-bold"))
          actions[1] = ab;
        else if (command.equals("font-italic"))
          actions[2] = ab;
        else if (command.equals("font-underline"))
          actions[3] = ab;
        else if (command.equals("font-foreground"))
          actions[4] = ab;
        else if (command.equals("font-background"))
          actions[5] = ab;
      }
    }

    JToolBar actionsBar = new JToolBar();
    actionsBar.setFloatable(false);
    actionsBar.setBorder(new EmptyBorder(0,0,0,0));

    actionsBar.add(makeDelegatingButton("Change font", ImageNums.EDITOR_FONTS, actions, 0));
    actionsBar.add(makeDelegatingToggleButton("Bold", ImageNums.EDITOR_BOLD, actions, 1));
    actionsBar.add(makeDelegatingToggleButton("Italic", ImageNums.EDITOR_ITALIC, actions, 2));
    actionsBar.add(makeDelegatingToggleButton("Underline", ImageNums.EDITOR_UNDERLINE, actions, 3));
    actionsBar.add(makeDelegatingButton("Foreground color", ImageNums.EDITOR_COLOR, actions, 4));
    actionsBar.add(makeDelegatingButton("Highlight", ImageNums.EDITOR_HIGHLIGHT, actions, 5));
    actionsBar.add(makeDelegatingButton("Insert link", ImageNums.EDITOR_LINK, actions, 6));

    JMyButtonNoFocus jEmoticons = new JMyButtonNoFocus(Images.get(ImageNums.EDITOR_EMOTICONS));
    jEmoticons.setToolTipText("Emoticons");
    jEmoticons.setBorder(new EmptyBorder(2,2,2,2));
    jEmoticons.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        insertImage(MyHTMLEditor.this, event);
      }
    });
    actionsBar.add(jEmoticons);

    actionsPanel = actionsBar;
  }

  private static JMyButtonNoFocus makeDelegatingButton(String toolTip, int iconIndex, final AbstractButton[] delegateToButtons, final int delegateToIndex) {
    final JMyButtonNoFocus button = new JMyButtonNoFocus(iconIndex >= 0 ? Images.get(iconIndex) : delegateToButtons[delegateToIndex].getIcon());
    initDelegatingButton(button, toolTip, iconIndex, delegateToButtons, delegateToIndex);
    return button;
  }

  private static JToggleButtonNoFocus makeDelegatingToggleButton(String toolTip, int iconIndex, final AbstractButton[] delegateToButtons, final int delegateToIndex) {
    final JToggleButtonNoFocus button = new JToggleButtonNoFocus(iconIndex >= 0 ? Images.get(iconIndex) : delegateToButtons[delegateToIndex].getIcon());
    initDelegatingButton(button, toolTip, iconIndex, delegateToButtons, delegateToIndex);
    return button;
  }

  private static void initDelegatingButton(final AbstractButton button, String toolTip, int iconIndex, final AbstractButton[] delegateToButtons, final int delegateToIndex) {
    button.setBorder(new EmptyBorder(2,2,2,2));
    button.setToolTipText(toolTip);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        delegateToButtons[delegateToIndex].doClick();
      }
    });
    delegateToButtons[delegateToIndex].addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (delegateToButtons[delegateToIndex].isSelected() != button.isSelected())
          button.setSelected(delegateToButtons[delegateToIndex].isSelected());
        if (delegateToButtons[delegateToIndex].isEnabled() != button.isEnabled())
          button.setEnabled(delegateToButtons[delegateToIndex].isEnabled());
      }
    });
  }

  private JComponent makeSeparator() {
    JPanel sep = new JPanel();
    sep.setLayout(null);
    sep.setBackground(sep.getBackground().darker());
    sep.setMaximumSize(new Dimension(1, 50));
    return sep;
  }

  private void enableSpeller(JTextComponent textComp) {
    try {
      TigerBkgChecker tigerBkgChecker = new TigerBkgChecker(SingleTigerSession.getSingleInstance());
      ((TigerBkgChecker)tigerBkgChecker).restart(textComp);
      textComp.addMouseListener(new TigerMouseAdapter(this));
    } catch (Exception e) {
    }
  }

  private static void insertImage(final HTMLEditor editor, ActionEvent ae) {
    final String command = "Emoticons";
    Component invoker = (Component) ae.getSource();
    JPopupMenu jPop = new JPopupMenu(command);
    final ItemQuickPickPanel cPanel = new ItemQuickPickPanel(command, com.CH_co.util.ImageNums.getEmoticonCodes());
    cPanel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        Integer indexI = cPanel.getMainCompIndex();
        if (indexI != null) {
          int imageIndex = com.CH_co.util.ImageNums.getEmoticonCodes()[indexI.intValue()];
          String imageName = "images/" + com.CH_co.util.ImageNums.getImageName(imageIndex) + ".gif";
          try {
            editor.insertImage(imageName);
          } catch (Exception e) {
          }
        }
      }
    });
    jPop.add(cPanel);
    jPop.show(invoker, 0, invoker.getSize().height);
  }
}
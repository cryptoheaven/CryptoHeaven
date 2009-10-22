/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package comx.Tiger.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.event.*;

import com.CH_co.gui.*;
import com.CH_co.util.*;

import com.CH_guiLib.gui.*;

import comx.Tiger.ssce.*;
import comx.Tiger.util.MessageBox;
import comx.Tiger.util.Sort;

/**
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:  
 *
 * Interact with the user to edit the contents of a FileTextLexicon. 
 *
 * Class Details: 
 *
 *
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class JTigerUserDialog extends JDialog {

  JTextField wordTextFld = new JMyTextField(20);
  JTextField otherWordTextFld = new JMyTextField(20);
  JList wordList = new JList();
  JRadioButton ignoreRb = new JMyRadioButton("Ignore (skip)");
  JRadioButton autoChangeCheckWordRb = new JMyRadioButton("Auto change, using case of checked word");
  JRadioButton autoChangeOtherWordRb = new JMyRadioButton("Auto change, using case of other word");
  JRadioButton condChangeCheckWordRb = new JMyRadioButton("Conditionally change, using case of checked word");
  JRadioButton condChangeOtherWordRb = new JMyRadioButton("Conditionally change, using case of other word");
  JRadioButton excludeRb = new JMyRadioButton("Exclude (treat as misspelled)");
  JButton addWordBtn = new JMyButton("Add word");
  JButton deleteWordBtn = new JMyButton("Delete word");
  JButton doneBtn = new JMyButton("Done");

  /**
   * Interact with the user to edit the contents of an EditableLexicon.
   * @param parent The parent frame
   * @param lexicon The lexicon to edit
   */
  public JTigerUserDialog(Frame parent, FileTextLexicon lexicon) {
    super(parent, "Edit user dictionary");

    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    getContentPane().add(panel, BorderLayout.CENTER);

    panel.add(new JMyLabel("Words:"), new GridBagConstraints(0, 0, 2, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));
    panel.add(new JMyLabel("Other word:"), new GridBagConstraints(2, 0, 2, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));

    panel.add(wordTextFld, new GridBagConstraints(0, 1, 2, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 0, 5), 0, 0));
    panel.add(otherWordTextFld, new GridBagConstraints(2, 1, 2, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 0, 5), 0, 0));

    panel.add(new JScrollPane(wordList), new GridBagConstraints(0, 2, 2, 2, 10, 10, 
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 5, 5, 5), 0, 0));
    panel.add(addWordBtn, new GridBagConstraints(2, 2, 1, 1, 5, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(deleteWordBtn, new GridBagConstraints(3, 2, 1, 1, 5, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JLabel(), new GridBagConstraints(2, 3, 1, 1, 10, 10, 
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));

    panel.add(new JMyLabel("Action"), new GridBagConstraints(0, 4, 4, 1, 20, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(ignoreRb, new GridBagConstraints(0, 5, 4, 1, 20, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0, 5, 0, 5), 0, 0));
    panel.add(autoChangeCheckWordRb, new GridBagConstraints(0, 6, 4, 1, 20, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0, 5, 0, 5), 0, 0));
    panel.add(autoChangeOtherWordRb, new GridBagConstraints(0, 7, 4, 1, 20, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0, 5, 0, 5), 0, 0));
    panel.add(condChangeCheckWordRb, new GridBagConstraints(0, 8, 4, 1, 20, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0, 5, 0, 5), 0, 0));
    panel.add(condChangeOtherWordRb, new GridBagConstraints(0, 9, 4, 1, 20, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0, 5, 0, 5), 0, 0));
    panel.add(excludeRb, new GridBagConstraints(0, 10, 4, 1, 20, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0, 5, 0, 5), 0, 0));

    panel.add(doneBtn, new GridBagConstraints(0, 12, 4, 1, 20, 0, 
        GridBagConstraints.CENTER, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));

    wordList.setVisibleRowCount(5);
    wordList.setCellRenderer(new MyDefaultListCellRenderer());

    //{{REGISTER_LISTENERS
    SymAction lSymAction = new SymAction();
    addWordBtn.addActionListener(lSymAction);
    deleteWordBtn.addActionListener(lSymAction);
    doneBtn.addActionListener(lSymAction);
    SymListSelection lSymListSelection = new SymListSelection();
    wordList.addListSelectionListener(lSymListSelection);
    //}}

    wordListModel = new DefaultListModel();
    wordList.setModel(wordListModel);
    this.lexicon = lexicon;
    lexiconChanged();

    // Add the radio buttons to a ButtonGroup for mutual exclusivity
    ButtonGroup bg = new ButtonGroup();
    bg.add(ignoreRb);
    bg.add(autoChangeCheckWordRb);
    bg.add(autoChangeOtherWordRb);
    bg.add(condChangeCheckWordRb);
    bg.add(condChangeOtherWordRb);
    bg.add(excludeRb);

    ignoreRb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (wordTextFld.getText().equals(wordList.getSelectedValue()))
          addWordBtn_actionPerformed(event);
      }
    });
    autoChangeCheckWordRb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (wordTextFld.getText().equals(wordList.getSelectedValue()))
          addWordBtn_actionPerformed(event);
      }
    });
    autoChangeOtherWordRb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (wordTextFld.getText().equals(wordList.getSelectedValue()))
          addWordBtn_actionPerformed(event);
      }
    });
    condChangeCheckWordRb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (wordTextFld.getText().equals(wordList.getSelectedValue()))
          addWordBtn_actionPerformed(event);
      }
    });
    condChangeOtherWordRb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (wordTextFld.getText().equals(wordList.getSelectedValue()))
          addWordBtn_actionPerformed(event);
      }
    });
    excludeRb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (wordTextFld.getText().equals(wordList.getSelectedValue()))
          addWordBtn_actionPerformed(event);
      }
    });

    pack();
  }


  // Word list selection model needed because list data is dynamic.
  DefaultListModel wordListModel;

  /**
   * Lexicon being edited
   */
  protected EditableLexicon lexicon;

  // set to true to enable debugging messages
  public boolean debug = false;

  class SymAction implements java.awt.event.ActionListener {
    public void actionPerformed(java.awt.event.ActionEvent event) {
      Object object = event.getSource();
      if (object == addWordBtn)
        addWordBtn_actionPerformed(event);
      else if (object == deleteWordBtn)
        deleteWordBtn_actionPerformed(event);
      else if (object == doneBtn)
        doneBtn_actionPerformed(event);
    }
  }

  void addWordBtn_actionPerformed(java.awt.event.ActionEvent event) {
    int action;

    // Add the word to the lexicon.
    if (autoChangeCheckWordRb.isSelected()) {
      action = Lexicon.AUTO_CHANGE_PRESERVE_CASE_ACTION;
    }
    else if (autoChangeOtherWordRb.isSelected()) {
      action = Lexicon.AUTO_CHANGE_ACTION;
    }
    else if (condChangeCheckWordRb.isSelected()) {
      action = Lexicon.CONDITIONAL_CHANGE_PRESERVE_CASE_ACTION;
    }
    else if (condChangeOtherWordRb.isSelected()) {
      action = Lexicon.CONDITIONAL_CHANGE_ACTION;
    }
    else if (excludeRb.isSelected()) {
      action = Lexicon.EXCLUDE_ACTION;
    }
    else {
      action = Lexicon.IGNORE_ACTION;
    }

    String word = wordTextFld.getText();
    String other = otherWordTextFld.getText();

    try {
      lexicon.addWord(SpellingSession.stripPossessives(word), action, SpellingSession.stripPossessives(other));
    }
    catch (Exception e) {
      if (debug) System.out.println(e);
      MessageBox.createMessageBox("Edit dictionary", "Error adding word: " + e);
      return;
    }
    lexiconChanged();
    wordList.setSelectedValue(word, true);
  }

  void deleteWordBtn_actionPerformed(java.awt.event.ActionEvent event) {
    // Delete the word from the lexicon.
    try {
      lexicon.deleteWord(wordTextFld.getText());
    }
    catch (Exception e) {
      if (debug) System.out.println(e);
      MessageBox.createMessageBox("Edit dictionary",
      "Error deleting word: " + e);
      return;
    }
    finally {
      lexiconChanged();
    }
  }

  void doneBtn_actionPerformed(java.awt.event.ActionEvent event) {
    setVisible(false);
    dispose();
  }

  class SymListSelection implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent event) {
      Object object = event.getSource();
      if (object == wordList)
        wordList_valueChanged(event);
    }
  }

  void wordList_valueChanged(ListSelectionEvent event) {
    // The word list selection changed.
    wordListSelectionChanged();
  }

  /**
   * Respond to a change in the lexicon contents.
   */
  protected void lexiconChanged() {
    int curSel = wordList.getSelectedIndex();

    // Sort the words in the lexicon.
    wordListModel.clear();
    String words[] = new String[lexicon.size()];
    int i = 0;
    for (Enumeration e = lexicon.words(); e.hasMoreElements();) {
      words[i++] = (String)e.nextElement();
    }
    Sort.ascending(words);

    // Add the sorted words to the word list.
    for (i = 0; i < words.length; ++i) {
      wordListModel.addElement(words[i]);
    }

    if (curSel < 0) {
      curSel = 0;
    }
    if (curSel >= wordListModel.size()) {
      curSel = wordListModel.size() - 1;
    }
    if (curSel >= 0) {
      wordList.setSelectedIndex(curSel);
      wordListSelectionChanged();
    }
  }

  /**
   * Respond to a different word being selected in the word list.
   */
  protected void wordListSelectionChanged() {
    String word = (String)wordList.getSelectedValue();
    if (word != null) {
      wordTextFld.setText(word);
      StringBuffer otherWord = new StringBuffer();
      int action = lexicon.findWord(word, true, otherWord);
      switch (action) {
        case Lexicon.AUTO_CHANGE_PRESERVE_CASE_ACTION:
          autoChangeCheckWordRb.setSelected(true);
          break;
        case Lexicon.AUTO_CHANGE_ACTION:
          autoChangeOtherWordRb.setSelected(true);
          break;
        case Lexicon.CONDITIONAL_CHANGE_PRESERVE_CASE_ACTION:
          condChangeCheckWordRb.setSelected(true);
          break;
        case Lexicon.CONDITIONAL_CHANGE_ACTION:
          condChangeOtherWordRb.setSelected(true);
          break;
        case Lexicon.EXCLUDE_ACTION:
          excludeRb.setSelected(true);
          break;
        default:
          ignoreRb.setSelected(true);
          break;
      }
      otherWordTextFld.setText(otherWord.toString());
    }
    else {
      wordTextFld.setText("");
      otherWordTextFld.setText("");
    }
  }
}
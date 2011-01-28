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

package com.CH_gui.dialog;

import com.CH_co.util.NotificationCenter;
import com.CH_gui.util.GeneralDialog;
import com.CH_gui.util.MessageDialog;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import com.CH_gui.gui.*;
import com.CH_guiLib.gui.*;

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
 * <b>$Revision: 1.5 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class ChoiceDialog extends GeneralDialog {

  public static final int DEFAULT_OK_INDEX = 0;
  public static final int DEFAULT_CANCEL_INDEX = 1;

  private JRadioButton[] jChoices;
  private Integer resultChoice;
  private Integer resultButton;

  /** Creates new ChoiceDialog */
  public ChoiceDialog(Dialog owner, String title, String[] lines, String[] choices, Insets[] insets, int selectedIndex) {
    super(owner, title);
    initialize(owner, lines, choices, insets, selectedIndex);
  }
  /** Creates new ChoiceDialog */
  public ChoiceDialog(Frame owner, String title, String[] lines, String[] choices, Insets[] insets, int selectedIndex) {
    super(owner, title);
    initialize(owner, lines, choices, insets, selectedIndex);
  }

  private void initialize(Component owner, String[] lines, String[] choices, Insets[] insets, int selectedIndex) {
    setModal(true);
    JButton[] buttons = createButtons();
    jChoices = createChoices(choices, selectedIndex);
    JPanel panel = createMainPanel(lines, choices, insets);
    MessageDialog.playSound(NotificationCenter.QUESTION_MESSAGE);
    init(owner, buttons, panel, DEFAULT_OK_INDEX, DEFAULT_CANCEL_INDEX);
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[2];
    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
    buttons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        resultButton = new Integer(DEFAULT_OK_INDEX);
        for (int i=0; i<jChoices.length; i++) {
          if (jChoices[i].isSelected()) {
            resultChoice = new Integer(i);
            break;
          }
        }
        dispose();
      }
    });
    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        resultButton = null;
        resultChoice = null;
        dispose();
      }
    });
    return buttons;
  }

  private JRadioButton[] createChoices(String[] choices, int selectedIndex) {
    JRadioButton[] jChoices = new JMyRadioButton[choices.length];
    ButtonGroup group = new ButtonGroup();
    for (int i=0; i<choices.length; i++) {
      jChoices[i] = new JMyRadioButton(choices[i]);
      boolean selected = i == selectedIndex;
      jChoices[i].setSelected(selected);
      if (selected) {
        jChoices[i].addHierarchyListener(new InitialFocusRequestor());
      }
      group.add(jChoices[i]);
    }
    return jChoices;
  }

  private JPanel createMainPanel(String[] lines, String[] choices, Insets[] insets) {
    JPanel main = new JPanel();
    main.setBorder(new EmptyBorder(10, 10, 10, 10));
    main.setLayout(new GridBagLayout());

    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(0, 0, 0, 0));
    panel.setLayout(new GridBagLayout());

    UIDefaults table = UIManager.getLookAndFeelDefaults();
    JLabel icon = new JMyLabel(table.getIcon("OptionPane.questionIcon"));
    main.add(icon, new GridBagConstraints(0, 0, 1, 1, 0, 10, 
          GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    main.add(panel, new GridBagConstraints(1, 0, 1, 1, 10, 10, 
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    int posY = 0;
    for (int i=0; i<lines.length; i++) {
      panel.add(new JMyLabel(lines[i]), new GridBagConstraints(0, posY, 1, 1, 0, 0, 
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, insets[posY], 0, 0));
      posY ++;
    }
    for (int i=0; i<jChoices.length; i++) {
      panel.add(jChoices[i], new GridBagConstraints(0, posY, 1, 1, 0, 0, 
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, insets[posY], 0, 0));
      posY ++;
    }
    return main;
  }

  public Integer getResultButton() {
    return resultButton;
  }

  public Integer getResultChoice() {
    return resultChoice;
  }

}
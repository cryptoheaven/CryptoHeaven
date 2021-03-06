/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import com.CH_guiLib.gui.*;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.5 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class JMyTextComboBox extends JPanel {

  private JTextField jText;
  private JComboBox jCombo;

  /** Creates new JMyTextComboBox */
  public JMyTextComboBox(String defaultText, String[] defaultItems) {
    this(defaultText, -1, defaultItems);
  }
  public JMyTextComboBox(String defaultText, int columns, String[] defaultItems) {
    setLayout(new BorderLayout(0, 0));

    if (columns >= 0)
      jText = new JMyTextField(defaultText, columns);
    else 
      jText = new JMyTextField(defaultText);

    jCombo = new JMyComboBox(defaultItems);

    add(jText, BorderLayout.CENTER);
    add(jCombo, BorderLayout.EAST);
  }


  public void setSelectedIndex(int index) {
    jCombo.setSelectedIndex(index);
  }

  public void setBorder() {
    javax.swing.border.Border border = UIManager.getBorder("TextField.border");
    if (!border.equals(getBorder())) {
      setBorder(border);
      repaint();
    }
  }

  public JTextField getTextField() {
    return jText;
  }

  public Document getDocument() {
    return jText.getDocument();
  }

  public void setEnabled(boolean flag) {
    jText.setEnabled(flag);
    jCombo.setEnabled(flag);
  }

  public void setEditable(boolean flag) {
    jText.setEditable(flag);
    jCombo.setEditable(flag);
  }

  public String getTextCombined() {
    return jText.getText() + jCombo.getSelectedItem();
  }

  public void setText(String s) {
    jText.setText(s);
  }

}
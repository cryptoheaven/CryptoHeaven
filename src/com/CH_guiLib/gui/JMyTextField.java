/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_guiLib.gui;

import com.CH_gui.gui.JMyLabel;
import com.CH_gui.util.MiscGui;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.6 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class JMyTextField extends JTextField {

  private Color originalColor = null;
  private Color hintColor = null;
  private Font originalFont = null;
  private Font hintFont = null;
  private String hintText = null;
  private String backgroundText = null;

  /** Creates new JMyTextField */
  public JMyTextField() {
    super();
    MiscGui.initKeyBindings(this);
  }

  /** Creates new JMyTextField */
  public JMyTextField(int columns) {
    super(columns);
    MiscGui.initKeyBindings(this);
  }

  /** Creates new JMyTextField */
  public JMyTextField(String s, int columns) {
    super(s, columns);
    MiscGui.initKeyBindings(this);
  }

  /** Creates new JMyTextField */
  public JMyTextField(String s) {
    super(s);
    MiscGui.initKeyBindings(this);
  }

  public void setUnfocusedTextWhenEmpty(String text) {
    JMyLabel dummyLabel = new JMyLabel();
    hintText = text;
    originalColor = getForeground();
    originalFont = dummyLabel.getFont();
    hintColor = getBackground().darker();
    hintFont = originalFont.deriveFont(Font.ITALIC);

    addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        if (getText().trim().equalsIgnoreCase(hintText.trim())) {
          setText("");
          setFont(originalFont);
          setForeground(originalColor);
        }
      }

      public void focusLost(FocusEvent e) {
        if (getText().trim().equals("") || getText().trim().equalsIgnoreCase(hintText.trim())) {
          setText(hintText);
          setFont(hintFont);
          setForeground(hintColor);
          setCaretPosition(0);
        }
      }
    });

    setText(hintText);
    setFont(hintFont);
    setForeground(hintColor);
    setCaretPosition(0);
  }

  public void setBackgroundTextWhenEmpty(String text) {
    JMyLabel dummyLabel = new JMyLabel();
    hintText = text;
    originalColor = getForeground();
    originalFont = dummyLabel.getFont();
    hintColor = getBackground().darker();
    hintFont = originalFont.deriveFont(Font.ITALIC);
    backgroundText = text;
  }

  public void paint(Graphics g) {
    MiscGui.setPaintPrefs(g);
    super.paint(g);
    if (backgroundText != null && getText().trim().length() == 0) {
      g.setColor(hintColor);
      g.setFont(hintFont);
      g.drawString(backgroundText, 0, (int) (0.75 * getHeight()));
    }
  }

}
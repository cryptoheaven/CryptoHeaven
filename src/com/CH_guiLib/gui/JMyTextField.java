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

package com.CH_guiLib.gui;

import com.CH_gui.gui.JMyLabel;
import com.CH_gui.util.MiscGui;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;

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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class JMyTextField extends JTextField {

  private Color originalColor = null;
  private Color hintColor = null;
  private Font originalFont = null;
  private Font hintFont = null;
  private String hintText = null;

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

  public void setUnfocusedEmptyText(String text) {
    JMyLabel dummyLabel = new JMyLabel();
    hintText = text;
    originalColor = getForeground();
    hintColor = getBackground().darker();
    originalFont = dummyLabel.getFont();
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

  public void paint(Graphics g) {
    MiscGui.setPaintPrefs(g);
    super.paint(g);
  }

}
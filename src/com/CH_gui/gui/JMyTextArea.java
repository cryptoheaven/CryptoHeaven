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
import javax.swing.JTextArea;

import com.CH_gui.util.MiscGui;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.6 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class JMyTextArea extends JTextArea {

  /** Creates new JMyTextArea */
  public JMyTextArea(String s, int rows, int columns) {
    super(s, rows, columns);
    MiscGui.initKeyBindings(this);
    setTabSize(4);
  }

  /** Creates new JMyTextArea */
  public JMyTextArea(int rows, int columns) {
    super(rows, columns);
    MiscGui.initKeyBindings(this);
    setTabSize(4);
  }

  /** Creates new JMyTextArea */
  public JMyTextArea(String s) {
    super(s);
    MiscGui.initKeyBindings(this);
    setTabSize(4);
  }

  /** Creates new JMyTextArea */
  public JMyTextArea() {
    super();
    MiscGui.initKeyBindings(this);
    setTabSize(4);
  }

  public void paint(Graphics g) {
    MiscGui.setPaintPrefs(g);
    super.paint(g);
  }

}
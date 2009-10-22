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

package com.CH_co.gui;

import java.awt.*;
import javax.swing.JTextArea;

import com.CH_co.util.MiscGui;

/**
 * <b>Copyright</b> &copy; 2001-2009
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
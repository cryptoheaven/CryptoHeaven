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

package com.CH_guiLib.gui;

import java.awt.*;
import javax.swing.JTextField;

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
public class JMyTextField extends JTextField {

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

  public void paint(Graphics g) {
    MiscGui.setPaintPrefs(g);
    super.paint(g);
  }

}
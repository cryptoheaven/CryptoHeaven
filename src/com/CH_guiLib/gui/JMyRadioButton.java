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

import java.awt.*;
import javax.swing.*;

import com.CH_co.util.MiscGui;

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
public class JMyRadioButton extends JRadioButton {

  /** Creates new JMyRadioButton */
  public JMyRadioButton() {
    super();
  }

  /** Creates new JMyRadioButton */
  public JMyRadioButton(String text) {
    super(text);
  }

  /** Creates new JMyRadioButton */
  public JMyRadioButton(String text, boolean selected) {
    super(text, selected);
  }

  public void paint(Graphics g) {
    MiscGui.setPaintPrefs(g);
    super.paint(g);
  }

}
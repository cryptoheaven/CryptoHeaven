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

package com.CH_gui.gui;

import java.awt.*;
import javax.swing.*;

import com.CH_gui.util.MiscGui;

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
 * <b>$Revision: 1.5 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class JMyLabel extends JLabel {

  /** Creates new JMyLabel */
  public JMyLabel() {
  }

  /** Creates new JMyLabel */
  public JMyLabel(String text) {
    super(text);
  }

  public JMyLabel(String text, float fontRelativeSize) {
    super(text);
    Font font = getFont();
    setFont(font.deriveFont((float) (font.getSize()+fontRelativeSize)));
  }

  /** Creates new JMyLabel */
  public JMyLabel(String text, int horizontalAlignment) {
    super(text, horizontalAlignment);
  }

  /** Creates new JMyLabel */
  public JMyLabel(String text, Icon image, int horizontalAlignment) {
    super(text, image, horizontalAlignment);
  }

  /** Creates new JMyLabel */
  public JMyLabel(Icon image) {
    super(image);
  }

  public void paint(Graphics g) {
    MiscGui.setPaintPrefs(g);
    super.paint(g);
  }

}
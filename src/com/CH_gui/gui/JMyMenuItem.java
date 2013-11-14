/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.gui;

import com.CH_gui.util.MiscGui;

import java.awt.*;
import javax.swing.*;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.5 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class JMyMenuItem extends JMenuItem {

  /** Creates new JMyMenuItem */
  public JMyMenuItem() {
  }

  /** Creates new JMyMenuItem */
  public JMyMenuItem(String text) {
    super(text);
  }

  /** Creates new JMyMenuItem */
  public JMyMenuItem(String text, Icon icon) {
    super(text, icon);
  }

  public void paint(Graphics g) {
    MiscGui.setPaintPrefs(g);
    super.paint(g);
  }

}
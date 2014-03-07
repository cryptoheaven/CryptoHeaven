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
import javax.swing.*;

import com.CH_gui.util.MiscGui;

/**
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.5 $</b>
*
* @author  Marcin Kurzawa
*/
public class JMyCheckBox extends JCheckBox {

  /** Creates new JCheckBox */
  public JMyCheckBox() {
  }

  /** Creates new JCheckBox */
  public JMyCheckBox(String text) {
    super(text);
  }

  /** Creates new JCheckBox */
  public JMyCheckBox(String text, boolean selected) {
    super(text, selected);
  }

  public void paint(Graphics g) {
    MiscGui.setPaintPrefs(g);
    super.paint(g);
  }

}
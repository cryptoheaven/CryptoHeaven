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
    if (fontRelativeSize != 0) {
      Font font = getFont();
      setFont(font.deriveFont((float) (font.getSize()+fontRelativeSize)));
    }
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
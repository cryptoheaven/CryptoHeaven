/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_guiLib.gui;

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
public class JMyEditorPane extends JEditorPane {

  /** Creates new JMyEditorPane */
  public JMyEditorPane(String type, String text) {
    super(type, text);
    MiscGui.initKeyBindings(this);
  }

  public void paint(Graphics g) {
    MiscGui.setPaintPrefs(g);
    super.paint(g);
  }

}
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package com.CH_gui.gui;

import com.CH_gui.util.MiscGui;
import java.awt.Graphics;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JToggleButton;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class JMyToggleButton extends JToggleButton {

  /** Creates new JMyButton */
  public JMyToggleButton() {
  }

  /** Creates new JMyButton */
  public JMyToggleButton(Action a) {
    super(a);
  }

  /** Creates new JMyButton */
  public JMyToggleButton(String text) {
    super(text);
  }

  /** Creates new JMyButton */
  public JMyToggleButton(Icon icon) {
    super(icon);
  }

  /** Creates new JMyButton */
  public JMyToggleButton(String text, Icon icon) {
    super(text, icon);
  }

  public void paint(Graphics g) {
    MiscGui.setPaintPrefs(g);
    super.paint(g);
  }

}
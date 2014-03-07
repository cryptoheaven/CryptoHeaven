/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.actionGui;

import com.CH_gui.util.MiscGui;
import java.awt.*;
import javax.swing.*;

import com.CH_co.util.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.9 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class JActionButtonNoFocus extends JActionButton {

  /** Creates new JActionButtonNoFocus */
  public JActionButtonNoFocus(Action action) {
    super(action);
    setRequestFocusEnabled(false);
  }
  /** Creates new JActionButtonNoFocus */
  public JActionButtonNoFocus(Action action, Dimension maxAndPrefSize) {
    super(action, maxAndPrefSize);
    setRequestFocusEnabled(false);
  }
  /** Creates new JActionButtonNoFocus */
  public JActionButtonNoFocus(Action action, boolean smallIcon) {
    super(action, smallIcon);
    setRequestFocusEnabled(false);
  }
  /** Creates new JActionButtonNoFocus */
  public JActionButtonNoFocus(Action action, boolean smallIcon, Dimension maxAndPrefSize, boolean isToolButton) {
    super(action, smallIcon, maxAndPrefSize, isToolButton);
    setRequestFocusEnabled(false);
  }

  public boolean isFocusTraversable() {
    return false;
  }

  public void paint(Graphics g) {
    MiscGui.setPaintPrefs(g);
    super.paint(g);
  }

}
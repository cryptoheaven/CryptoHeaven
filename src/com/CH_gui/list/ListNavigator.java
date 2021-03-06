/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.list;

import java.awt.event.*;
import javax.swing.*;
import com.CH_co.trace.Trace;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.3 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class ListNavigator extends KeyAdapter {

  private JList list;

  /** Creates new ListNavigator */
  public ListNavigator(JList listToNavigate) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ListNavigator.class, "ListNavigator()");
    list = listToNavigate;
    if (trace != null) trace.exit(ListNavigator.class);
  }

  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_UP) {
      ListUtils.moveSourceSelection(list, false, false);
      e.consume();
    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
      ListUtils.moveSourceSelection(list, true, false);
      e.consume();
    } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
      ListUtils.moveSourceSelection(list, false, true);
      e.consume();
    } else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
      ListUtils.moveSourceSelection(list, true, true);
      e.consume();
    }
  }

}
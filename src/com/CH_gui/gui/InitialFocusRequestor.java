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
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;


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
 * <b>$Revision: 1.12 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class InitialFocusRequestor extends Object implements HierarchyListener {

  /** Creates new InitialFocusRequestor */
  public InitialFocusRequestor() {
  }

  /**
   * As soon as the component is shown, focus is requested and listener removed.
   */
  public void hierarchyChanged(HierarchyEvent event) {
    final Component c = event.getComponent();
    long changeFlags = event.getChangeFlags();
    if ((changeFlags & (HierarchyEvent.SHOWING_CHANGED | HierarchyEvent.DISPLAYABILITY_CHANGED)) != 0 && 
        c != null && 
        c.isShowing()) 
    {
      c.removeHierarchyListener(this);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          Thread.yield();
          c.requestFocus();
          // In jre 1.3 there is a problem with text components not showing visible caret when windows initially shows, try to fix it with grabFocus()
          if (c instanceof JComponent)
            ((JComponent) c).grabFocus();
          if (c instanceof JTextComponent) {
            JTextComponent tC = (JTextComponent) c;
            Caret caret = tC.getCaret();
            if (caret != null) {
              caret.setVisible(true);
              caret.setSelectionVisible(true);
            }
          }
        }
      });
    }
  }

}
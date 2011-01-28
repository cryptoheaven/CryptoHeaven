/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
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

import com.CH_gui.util.MiscGui;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * <b>Copyright</b> &copy; 2001-2011
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
 * <b>$Revision: 1.7 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class JMyPopupMenu extends JPopupMenu {

  /** Creates new JMyPopupMenu */
  public JMyPopupMenu() {
    super();
    initialize();
  }

  /** Creates new JMyPopupMenu */
  public JMyPopupMenu(String name) {
    super(name);
    initialize();
  }

  private void initialize() {
    addPopupMenuListener(new PopupMenuListener() {
      private void removeAllItemsandListeners() {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            MiscGui.removeAllComponentsAndListeners(JMyPopupMenu.this);
            Container c = JMyPopupMenu.this.getParent();
            if (c != null) {
              c.remove(JMyPopupMenu.this);
            }
          }
        });
      }
      public void popupMenuCanceled(PopupMenuEvent e) {
        // executed when popup is cancelled
        removeAllItemsandListeners();
        removePopupMenuListener(this);
      }
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        // executed when user picks and action to run
        removeAllItemsandListeners();
        removePopupMenuListener(this);
      }
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      }
    });
  }

  public void paint(Graphics g) {
    MiscGui.setPaintPrefs(g);
    super.paint(g);
  }

}
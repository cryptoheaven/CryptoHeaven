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

package comx.Tiger.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.EventListener;
import javax.swing.*;
import javax.swing.event.CaretListener;

/**
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class TigerMouseAdapter extends MouseAdapter {

  public void mouseClicked(MouseEvent mouseEvent) {
    if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
      Object source = mouseEvent.getSource();
      if (source instanceof JComponent) {
        JComponent jComp = (JComponent) source;
        EventListener[] listeners = jComp.getListeners(CaretListener.class);
        for (int i=0; listeners!=null && i<listeners.length; i++) {
          CaretListener listener = (CaretListener) listeners[i];
          if (listener instanceof TigerBkgChecker) {
            TigerBkgChecker bgc = (TigerBkgChecker) listener;
            Point pt = new Point(mouseEvent.getX(), mouseEvent.getY());
            if (bgc.isInMisspelledWord(pt)) {
              mouseEvent.consume();
              JPopupMenu jPopupSpell = bgc.createPopupMenu(mouseEvent.getX(), mouseEvent.getY(), 8, "Ignore All", "Add to Dictionary", "(no spelling suggestions)");
              if (jPopupSpell != null)
                jPopupSpell.show((Component) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
            }
          }
        }
      }
    }
  }

}
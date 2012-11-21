/*
* Copyright 2001-2012 by CryptoHeaven Corp.,
* Mississauga, Ontario, Canada.
* All rights reserved.
*
* This software is the confidential and proprietary information
* of CryptoHeaven Corp. ("Confidential Information").  You
* shall not disclose such Confidential Information and shall use
* it only in accordance with the terms of the license agreement
* you entered into with CryptoHeaven Corp.
*/

package comx.Tiger.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import sferyx.administration.editors.HTMLEditor;

/**
* <b>Copyright</b> &copy; 2001-2012
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
*
* @author  Marcin Kurzawa
* @version
*/
public class TigerMouseAdapter extends MouseAdapter {

  HTMLEditor editor = null;

  public TigerMouseAdapter(HTMLEditor editor) {
    this.editor = editor;
  }

  public void mousePressed(MouseEvent mouseEvent) {
    if (!mouseEvent.isConsumed() && SwingUtilities.isRightMouseButton(mouseEvent)) {
      tigerPopup(mouseEvent);
    }
  }

  private void tigerPopup(MouseEvent mouseEvent) {
    JPopupMenu jPopupSpell = null;
    JPopupMenu jPopupEditor = null;

    Object source = mouseEvent.getSource();

    if (source instanceof JComponent) {
      JComponent jComp = (JComponent) source;
      EventListener[] listeners = jComp.getListeners(CaretListener.class);
      for (int i=0; listeners!=null && i<listeners.length; i++) {
        CaretListener listener = (CaretListener) listeners[i];
        if (listener instanceof TigerBkgChecker) {
          TigerBkgChecker bgc = (TigerBkgChecker) listener;
          Point pt = new Point(mouseEvent.getX(), mouseEvent.getY());
          if (bgc.isInMisspelledWord(pt))
            jPopupSpell = bgc.createPopupMenu(mouseEvent.getX(), mouseEvent.getY(), 8, "Ignore All", "Add to Dictionary", "(no spelling suggestions)");
        }
      }
    }

    if (editor != null) {
      if (jPopupSpell == null) {
        editor.setPopupMenuVisible(true);
        jPopupEditor = editor.getVisualEditorPopupMenu();
      } else {
        editor.setPopupMenuVisible(false);
      }
    }

    JPopupMenu jPopup = null;
    ArrayList removedComponentsL = null;
    ArrayList addedComponentsL = null;
    if (jPopupSpell != null && jPopupEditor != null) {
      // the editor's popup will be primary, and we'll insert other items on top
      addedComponentsL = new ArrayList();
      removedComponentsL = new ArrayList();
      int count = jPopupEditor.getComponentCount();
      for (int i=3; i<count; i++) {
        removedComponentsL.add(jPopupEditor.getComponent(i));
      }
      for (int i=0; i<removedComponentsL.size(); i++) {
        jPopupEditor.remove((Component) removedComponentsL.get(i));
      }
      count = jPopupSpell.getComponentCount();
      for (int i=count-1; i>=0; i--) {
        Component c = jPopupSpell.getComponent(i);
        addedComponentsL.add(c);
        jPopupEditor.insert(c, 0);
      }
      Component jSep = new JPopupMenu.Separator();
      addedComponentsL.add(jSep);
      jPopupEditor.insert(jSep, count);
      jPopupEditor.pack();
      jPopup = jPopupEditor;
    } else if (jPopupSpell != null) {
      jPopup = jPopupSpell;
    } else {
      jPopup = jPopupEditor;
    }

    if (addedComponentsL != null) {
      final JPopupMenu jPopup_ = jPopup;
      final ArrayList toRemoveL = addedComponentsL;
      final ArrayList toAddL = removedComponentsL;
      jPopup_.addPopupMenuListener(new PopupMenuListener() {
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
          jPopup_.pack();
        }
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
          jPopup_.removePopupMenuListener(this);
          for (int i=0; i<toRemoveL.size(); i++)
            jPopup_.remove((Component) toRemoveL.get(i));
          for (int i=0; i<toAddL.size(); i++)
            jPopup_.add((Component) toAddL.get(i));
          toRemoveL.clear();
          toAddL.clear();
        }
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
      }
      );
    }

    // show non-editor popups, leave editor popup to be shown from the editor code itself
    if (jPopup != null && jPopupEditor == null) {
      mouseEvent.consume();
      jPopup.show((Component) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
    }

  }
}
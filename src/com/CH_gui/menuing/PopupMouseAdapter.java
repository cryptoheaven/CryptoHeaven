/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_gui.menuing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.lang.ref.*;
import java.util.*;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.actionGui.JActionFrame;
import comx.Tiger.gui.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
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
public class PopupMouseAdapter extends MouseAdapter {

  private WeakReference registerForRef;
  private WeakReference actionProducerRef;

  /** Creates new PopupMouseAdapter */
  public PopupMouseAdapter(Component registerFor, ActionProducerI actionProducer) {
    this.registerForRef = new WeakReference(registerFor);
    this.actionProducerRef = new WeakReference(actionProducer);
  }

  public void mouseClicked(MouseEvent mouseEvent) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PopupMouseAdapter.class, "mouseClicked(MouseEvent)");
    if (trace != null) trace.args(mouseEvent);

    if (!mouseEvent.isConsumed() && SwingUtilities.isRightMouseButton(mouseEvent)) {

      JPopupMenu jPopupSpell = null;
      JPopupMenu jPopupActions = null;

      Object source = mouseEvent.getSource();
      if (source instanceof JComponent) {
        JComponent jComp = (JComponent) source;
        EventListener[] listeners = jComp.getListeners(CaretListener.class);
        for (int i=0; listeners!=null && i<listeners.length; i++) {
          CaretListener listener = (CaretListener) listeners[i];
          // "Tiger" is an optional spell-checker module. If "Tiger" family of packages is not included with the source, simply comment out this part.
          if (listener instanceof TigerBkgChecker) {
            TigerBkgChecker bgc = (TigerBkgChecker) listener;
            Point pt = new Point(mouseEvent.getX(), mouseEvent.getY());
            if (bgc.isInMisspelledWord(pt)) {
              mouseEvent.consume();
              jPopupSpell = bgc.createPopupMenu(mouseEvent.getX(), mouseEvent.getY(), 8, "Ignore All", "Add to Dictionary", "(no spelling suggestions)");
            }
          }
        }
      }


      if (registerForRef != null && actionProducerRef != null) {
        Component registerFor = (Component) registerForRef.get();
        ActionProducerI actionProducer = (ActionProducerI) actionProducerRef.get();
        if (registerFor != null && actionProducer != null) {
          Window window = SwingUtilities.windowForComponent(registerFor);
          if (window instanceof JActionFrame) {
            JActionFrame jActionFrame = (JActionFrame) window;
            jPopupActions = jActionFrame.getMenuTreeModel().generatePopup(actionProducer.getActions());
            mouseEvent.consume();
          }
        }
      }

      JPopupMenu jPopup = null;
      if (jPopupSpell != null && jPopupActions != null) {
        jPopup = jPopupSpell;
        jPopup.addSeparator();
        Component[] elements = jPopupActions.getComponents();
        for (int i=0; i<elements.length; i++)
          jPopup.add((Component) elements[i]);
      } else if (jPopupSpell != null) {
        jPopup = jPopupSpell;
      } else {
        jPopup = jPopupActions;
      }

      if (jPopup != null)
        jPopup.show((Component) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());

    }
    if (trace != null) trace.exit(PopupMouseAdapter.class);
  }

}
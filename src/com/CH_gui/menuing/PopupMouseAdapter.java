/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.menuing;

import com.CH_co.trace.Trace;
import com.CH_gui.actionGui.JActionFrame;
import com.CH_gui.util.ActionProducerI;
import com.CH_gui.util.SpellCheckerI;
import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.EventListener;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretListener;

/** 
* Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.12 $</b>
*
* @author  Marcin Kurzawa
*/
public class PopupMouseAdapter extends MouseAdapter {

  private WeakReference registerForRef;
  private WeakReference actionProducerRef;

  /** Creates new PopupMouseAdapter */
  public PopupMouseAdapter(Component registerFor, ActionProducerI actionProducer) {
    this.registerForRef = new WeakReference(registerFor);
    this.actionProducerRef = new WeakReference(actionProducer);
  }

  public void mouseClicked(MouseEvent e) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PopupMouseAdapter.class, "mouseClicked(MouseEvent)");
    if (trace != null) trace.args(e);

    if (!e.isConsumed() && SwingUtilities.isRightMouseButton(e)) {
      JPopupMenu jPopupSpell = null;
      JPopupMenu jPopupActions = null;

      Object source = e.getSource();
      if (source instanceof JComponent) {
        JComponent jComp = (JComponent) source;
        EventListener[] listeners = jComp.getListeners(CaretListener.class);
        for (int i=0; listeners!=null && i<listeners.length; i++) {
          CaretListener listener = (CaretListener) listeners[i];
          try {
            if (listener instanceof SpellCheckerI) {
              SpellCheckerI bgc = (SpellCheckerI) listener;
              Point pt = new Point(e.getX(), e.getY());
              if (bgc.isInMisspelledWord(pt))
                jPopupSpell = bgc.createPopupMenu(e.getX(), e.getY(), 8, "Ignore All", "Add to Dictionary", "(no spelling suggestions)");
            }
          } catch (Throwable t) {
            t.printStackTrace();
          }
        }
      }

      if (registerForRef != null && actionProducerRef != null) {
        Component registerFor = (Component) registerForRef.get();
        ActionProducerI actionProducer = (ActionProducerI) actionProducerRef.get();
        if (registerFor != null && actionProducer != null) {
          Window window = SwingUtilities.windowForComponent(registerFor);
          if (window instanceof JActionFrame) {
            MenuTreeModel model = ((JActionFrame) window).getMenuTreeModel();
            if (model != null) {
              Action[] actions = actionProducer.getActions();
              if (actions != null && actions.length > 0)
                jPopupActions = model.generatePopup(actions);
            }
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

      if (jPopup != null) {
        e.consume();
        jPopup.show((Component) e.getSource(), e.getX(), e.getY());
      }

    }
    if (trace != null) trace.exit(PopupMouseAdapter.class);
  }

}
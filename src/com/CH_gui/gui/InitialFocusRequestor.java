/*
* Copyright 2001-2013 by CryptoHeaven Corp.,
* Mississauga, Ontario, Canada.
* All rights reserved.
*
* This software is the confidential and proprietary information
* of CryptoHeaven Corp. ("Confidential Information").  You
* shall not disclose such Confidential Information and shall use
* it only in accordance with the terms of the license agreement
* you entered into with CryptoHeaven Corp.
*/

package com.CH_gui.gui;

import java.awt.Component;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import javax.swing.SwingUtilities;

/** 
* <b>Copyright</b> &copy; 2001-2013
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
* Class Description:
*
* Attach this listener to GUI components that should request/grab focus
* after their peers are shown on the screen.
*
* Class Details:
* 
* This is INSANE to have double delayed requestor, but I found no other
* reliable way of gaining desired focus. If after this effort the focus is
* not reliably grabbed then look for bugs caused by racing condition where
* two components use InitialFocusRequestor and randomly one succeeds before
* the other.
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
  * A delayed cycle is started where focus is requested again and hopefully
  * succeeds after all listened upon GUI is constructed and shown.
  */
  public void hierarchyChanged(HierarchyEvent event) {
    final Component c = event.getComponent();
    long changeFlags = event.getChangeFlags();
    if ((changeFlags & (HierarchyEvent.SHOWING_CHANGED | HierarchyEvent.DISPLAYABILITY_CHANGED)) != 0 
            && c != null && c.isShowing())
    {
      c.removeHierarchyListener(this);
      c.requestFocusInWindow();

      // Start delayed requests
      new Thread("Delayed Initial Focus Requestor 1") {
        public void run() {
          try {
            Thread.sleep(50);
          } catch (Throwable t) {
          } finally {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                c.requestFocusInWindow();

                // Start 2nd delayed request incase 1st failed.
                new Thread("Delayed Initial Focus Requestor 2") {
                  public void run() {
                    try {
                      Thread.sleep(50);
                    } catch (Throwable t) {
                    } finally {
                      SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                          c.requestFocusInWindow();
                        }
                      });
                    }
                  }
                }.start();
              }
            });
          }
        }
      }.start();
    }
  }
}
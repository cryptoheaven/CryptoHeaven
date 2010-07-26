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

package com.CH_gui.util;

import com.CH_co.trace.ThreadTraced;
import com.CH_co.util.Sounds;
import com.CH_gui.actionGui.JActionFrame;

import java.awt.*;
import javax.swing.SwingUtilities;

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
public class Nudge {

  public static void nudge(Component comp, boolean includeSound, boolean includeTitleRoll) {
    nudge(new Component[] { comp }, includeSound, includeTitleRoll);
  }

  public static void nudge(Component[] comps, boolean includeSound, final boolean includeTitleRoll) {
    if (includeSound)
      Sounds.playAsynchronous(Sounds.RING_BELL);
    if (comps != null && comps.length > 0) {
      for (int i=0; i<comps.length; i++) {
        Component comp = comps[i];
        if (comp != null) {
          Window w = null;
          if (comp instanceof Window) w = (Window) comp;
          else w = SwingUtilities.windowForComponent(comp);
          if (w != null) {
            if (w instanceof Frame) {
              if (((Frame)w).getState() == Frame.ICONIFIED)
                ((Frame)w).setState(Frame.NORMAL);
            }
            final Window window = w;
            Thread th = new ThreadTraced("Nudger") {
              public void runTraced() {
                if (window.isShowing()) {
                  Point p = window.getLocation();
                  double d = 0;
                  double magnitude = 4.0;
                  for (int i=0; i<30; i++) {
                    d = d+((double)i)/10.0;
                    double x = p.x+magnitude*Math.sin(2*Math.PI*d);
                    double y = p.y-magnitude*Math.cos(2*Math.PI*d);
                    window.setLocation((int) x, (int) y);
                    try { Thread.sleep(15); } catch (InterruptedException ex) { }
                  }
                  window.setLocation(p);
                }
                try { Thread.sleep(50); } catch (InterruptedException ex) { }
                if (includeTitleRoll && window instanceof JActionFrame)
                  ((JActionFrame) window).triggerVisualUpdateNotificationRoll(null, "  *ring*");
              }
            };
            th.setDaemon(true);
            th.start();
          }
        }
      }
    }
  }

}
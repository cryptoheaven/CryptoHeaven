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

package com.CH_co.monitor;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

import com.CH_co.gui.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

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
 * <b>$Revision: 1.19 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class Stats extends Object {

  // All progress monitors should update this label with the current status change.... (used for status bar)
  protected static JLabel jLastStatus;
  protected static LinkedList statusHistoryL = new LinkedList();
  protected static LinkedList statusHistoryDatesL = new LinkedList(); // pair dates for the string entries.. always go hand-in-hand
  private static final int MAX_HISTORY_SIZE = 100;

  protected static ImageIcon staticPic;
  protected static ImageIcon movingPic;
  protected static ImageIcon connectedPic;
  protected static ImageIcon disconnectedPic;
  protected static ImageIcon greenLightOnPic;
  protected static ImageIcon greenLightOffPic;

  private static Vector globeMoversV = new Vector(); // objects that are causing the globe to move
  private static Hashtable globeMoversTraceHT = new Hashtable(); // for debug, stack traces of our Movers

  protected static JLabel jPing;
  protected static JLabel jOnlineStatus;
  protected static JLabel jConnections;
  protected static JLabel jTransferRate;
  protected static JLabel jSize;

  private static long maxTransferRate;

  protected static final Object monitor = new Object();

  static {
    if (!MiscGui.isAllGUIsuppressed()) {
      jLastStatus = new JMyLabel();

      staticPic = Images.get(ImageNums.ANIM_GLOBE_FIRST16);
      movingPic = Images.get(ImageNums.ANIM_GLOBE16);
      connectedPic = Images.get(ImageNums.LIGHT_GREEN_SMALL);
      disconnectedPic = Images.get(ImageNums.LIGHT_X_SMALL);
      greenLightOnPic = Images.get(ImageNums.LIGHT_ON_SMALL);
      greenLightOffPic = Images.get(ImageNums.LIGHT_OFF_SMALL);

      jPing = new JMyLabel();
      jOnlineStatus = new JMyLabel();
      jConnections = new JMyLabel();
      jTransferRate = new JMyLabel();
      jSize = new JMyLabel();
    }
  }

  public static void adjustFonts(AffineTransform transform) {
    Font font = jLastStatus.getFont().deriveFont(transform);
    jLastStatus.setFont(font);
    jPing.setFont(font);
    jOnlineStatus.setFont(font);
    jConnections.setFont(font);
    jTransferRate.setFont(font);
    jSize.setFont(font);
  }

  public static void installStatsLabelMouseAdapter(MouseListener listener) {
    if (jLastStatus != null)
      jLastStatus.addMouseListener(listener);
  }

  public static JLabel getStatusLabel() {
    return jLastStatus;
  }
  public static JLabel getPingLabel() {
    return jPing;
  }
  public static JLabel getOnlineLabel() {
    return jOnlineStatus;
  }
  public static JLabel getConnectionsLabel() {
    return jConnections;
  }
  public static JLabel getTransferRateLabel() {
    return jTransferRate;
  }
  public static long getMaxTransferRate() {
    return maxTransferRate;
  }
  public static JLabel getSizeLabel() {
    return jSize;
  }

  public static Vector getGlobeMoversTraceV() {
    Vector moversTraceV = null;
    synchronized (monitor) {
      if (globeMoversTraceHT.size() > 0) {
        moversTraceV = new Vector(globeMoversTraceHT.values());
      }
    }
    return moversTraceV;
  }

  public static void moveGlobe(Object mover) {
    synchronized (monitor) {
      if (globeMoversV.size() == 0) {
        if (jLastStatus != null)
          jLastStatus.setIcon(movingPic);
      }
      if (!globeMoversV.contains(mover)) {
        globeMoversV.addElement(mover);
        globeMoversTraceHT.put(mover, Misc.getStack(new Throwable(""+mover+" at " + new Date())));
      }
    }
  }

  public static void stopGlobe(Object mover) {
    synchronized (monitor) {
      if (globeMoversV.contains(mover)) {
        globeMoversTraceHT.remove(mover);
        globeMoversV.removeElement(mover);
        if (globeMoversV.size() == 0) {
          if (jLastStatus != null)
            jLastStatus.setIcon(staticPic);
        }
      }
    }
  }

  public static void setStatus(String newStatus) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Stats.class, "setStatus(String newStatus)");
    if (trace != null) trace.args(newStatus);
    synchronized (monitor) {
      if (jLastStatus != null)
        jLastStatus.setText(newStatus);

      statusHistoryL.addFirst(newStatus);
      statusHistoryDatesL.addFirst(new Date());
      while (statusHistoryL.size() > MAX_HISTORY_SIZE) {
        statusHistoryL.removeLast();
        statusHistoryDatesL.removeLast();
      }
    }
    if (trace != null) trace.exit(Stats.class);
  }

  public static void setPing(long ms) {
    synchronized (monitor) {
      if (jPing != null) {
        if (ms > 2000)
          jPing.setText("" + ((int) (ms/1000)) + " s");
        else
          jPing.setText("" + ms + " ms");
      }
    }
  }

  public static void setConnections(int connectionCount, int[] connectionTypeCounts) {
    Random rnd = new Random();
    int i = rnd.nextInt();
    synchronized (monitor) {
      if (jConnections != null) {
        jConnections.setText("" + connectionCount);
        jConnections.setToolTipText("<html>Number of open socket connections: <b>" + connectionTypeCounts[0] + 
                                  "</b><br>Number of open HTTP sockets: <b>" + connectionTypeCounts[1] + "</b>");
      }
      if (connectionCount > 0) {
        if (jOnlineStatus != null) {
          jOnlineStatus.setText("Online");
          jOnlineStatus.setIcon(connectedPic);
        }
      }
      else {
        if (jOnlineStatus != null) {
          jOnlineStatus.setText("Offline");
          jOnlineStatus.setIcon(disconnectedPic);
        }
      }
    }
  }

  public static void setTransferRate(long bytesPerSecond) {
    synchronized (monitor) {
      maxTransferRate = Math.max(maxTransferRate, bytesPerSecond);
      String size = Misc.getFormattedSize(bytesPerSecond, 4, 3);
      if (size != null && size.length() > 0) {
        if (bytesPerSecond > 0) {
          if (jTransferRate != null)
            jTransferRate.setIcon(greenLightOnPic);
        } else {
          if (jTransferRate != null)
            jTransferRate.setIcon(greenLightOffPic);
        }
        if (jTransferRate != null)
          jTransferRate.setText(size + "/sec");
      }
      else {
        if (jTransferRate != null) {
          jTransferRate.setIcon(greenLightOffPic);
          jTransferRate.setText(null);
        }
      }
    }
  }

  public static void setSize(long size) {
    synchronized (monitor) {
      String sizeS = null;
      if (size >= 0)
        sizeS = Misc.getFormattedSize(size, 4, 3);
      if (sizeS != null && sizeS.length() > 0) {
        if (jSize != null)
          jSize.setText(sizeS);
      }
      else {
        if (jSize != null)
          jSize.setText(null);
      }
    }
  }

}
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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

import com.CH_co.gui.*;

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
 * <b>$Revision: 1.9 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class Scroller extends JPanel {

  private float pixelsPerSecond = 60;
  private float framesPerSecond = 20;
  private int pauseMillis = 1000; // pause between scrolling components

  // properties
  private boolean scrollOffLastItem = false;
  private boolean snapOn = true;
  private boolean biggerStepsOnSlowMachines = true;
  private boolean setComponentsOpaque;

  private boolean pausing = false;
  private int pausingSoFar;

  private Vector scrolledComponentsV = new Vector();
  private Timer timer = null;

  private int windowShiftPosition = 0;
  private int windowBottomPosition = 0;

  private int lastElementInView = -1;

  // Last time timer was activated.
  private long lastTime;

  /** Creates new Scroller */
  public Scroller(float pixelsPerSecond, float framesPerSecond, int pauseMillis, boolean setComponentsOpaque) {
    this.pixelsPerSecond = pixelsPerSecond;
    this.framesPerSecond = framesPerSecond;
    this.pauseMillis = pauseMillis;
    this.setComponentsOpaque = setComponentsOpaque;

    this.pausing = pauseMillis > 0;
    setOpaque(setComponentsOpaque);
    setLayout(null);
  }

  public synchronized void setPixelSpeed(float pixelsPerSecond) {
    this.pixelsPerSecond = pixelsPerSecond;
  }
  public synchronized void setFrameSpeed(float framesPerSecond) {
    this.framesPerSecond = framesPerSecond;
    timer.setDelay((int) (1000/framesPerSecond));
  }
  public synchronized void setPause(int pauseMillis) {
    this.pauseMillis = pauseMillis;
  }

  /**
   * Add a component to be scrolled, it should already be sized correctly.
   */
  public synchronized void addForScrolling(JComponent c) {
    if (scrolledComponentsV.size() == 0 && pauseMillis > 0)
      pausing = true;
    scrolledComponentsV.addElement(c);
    c.setOpaque(setComponentsOpaque);
    c.setSize(getSize());

    checkForAdditionsToViewWindow();
    repaint();

    if (timer == null) {
      timer = new Timer((int) (1000/framesPerSecond), new TimerListener());
      timer.start();
    } else {
      timer.restart();
    }
  }

  public synchronized boolean isRunning() {
    return timer != null && timer.isRunning();
  }

  private synchronized void checkForAdditionsToViewWindow() {
    Dimension d = getSize();
    if (windowBottomPosition-windowShiftPosition < d.height) {
      if (scrolledComponentsV.size()-1 > lastElementInView) {
        lastElementInView ++;
        JComponent c = (JComponent) scrolledComponentsV.elementAt(lastElementInView);
        Dimension dc = c.getSize();
        c.setBounds(0, windowBottomPosition, dc.width, dc.height);
        windowBottomPosition += dc.height;
        add(c);
      }
    }
  }

  private synchronized void checkForRemovalsFromViewWindow() {
    while (scrolledComponentsV.size() > 0) {
      Dimension d = getSize();
      JComponent firstElement = (JComponent) scrolledComponentsV.elementAt(0);
      Dimension fd = firstElement.getSize();
      if (fd.height <= windowShiftPosition) {
        remove(firstElement);
        scrolledComponentsV.removeElementAt(0);
        lastElementInView --;
        windowShiftPosition -= fd.height;
        windowBottomPosition -= fd.height;

        // adjust the other elements bounds after origin shift
        int yPos = 0;
        for (int i=0; i<=lastElementInView; i++) {
          JComponent tc = (JComponent) scrolledComponentsV.elementAt(i);
          Dimension tcd = tc.getSize();
          tc.setBounds(0, yPos, tcd.width, tcd.height);
          yPos += tcd.height;
        }
      } else {
        break;
      }
    }
  }

  public synchronized void removeAll() {
    super.removeAll();
    scrolledComponentsV.removeAllElements();
    lastElementInView = -1;
    windowShiftPosition = 0;
    windowBottomPosition = 0;
    if (timer != null)
      timer.stop();
    lastTime = 0;
    pausing = false;
    pausingSoFar = 0;
    repaint();
  }

  public void paint(Graphics g) {
    g.translate(0, -windowShiftPosition);
    super.paint(g);
  }


  private class TimerListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      synchronized (Scroller.this) {
        long currentTime = System.currentTimeMillis();
        long delayed = currentTime - lastTime;
        if (lastTime == 0) {
          delayed = timer.getDelay();
        }
        lastTime = currentTime;
        if (pausing) {
          pausingSoFar += delayed;
          if (pausingSoFar >= pauseMillis) {
            pausingSoFar = 0;
            pausing = false;
          }
        }
        else if (scrolledComponentsV.size() <= 0 || (!scrollOffLastItem && scrolledComponentsV.size() <= 1)) {
          timer.stop();
          lastTime = 0;
        } else {
          float sp = pixelsPerSecond / framesPerSecond;
          int shiftPixels = (int) (!biggerStepsOnSlowMachines ? sp : sp * ((double) delayed / (double) timer.getDelay()));
          JComponent c = (JComponent) scrolledComponentsV.elementAt(0);
          Dimension cd = c.getSize();

          // see if snap
          if (snapOn && windowShiftPosition < cd.height && windowShiftPosition + shiftPixels > cd.height)
            shiftPixels = cd.height - windowShiftPosition;

          // see if time to pause
          if (pauseMillis > 0 && windowShiftPosition + shiftPixels == cd.height)
            pausing = true;

          windowShiftPosition += shiftPixels;

          checkForAdditionsToViewWindow();
          checkForRemovalsFromViewWindow();

          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              validate();
              repaint();
            }
          });
        }
      }
    }
  }


  /**
   * main for testing
   */
  public static void main(String[] args) {
    JFrame f = new JFrame("Testing");
    Scroller s = new Scroller(100, 20, 1000, false);
    f.getContentPane().add(s);
    s.setSize(200,200);
    f.setSize(200,200);
    f.setVisible(true);
    try {
      java.io.BufferedReader d = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
      int count = 0;
      while (true) {
        count ++;
        JLabel ll = new JMyLabel("" + count + " : " +d.readLine());
        s.addForScrolling(ll);
      }
    } catch (Throwable t) {
    }
  }
}
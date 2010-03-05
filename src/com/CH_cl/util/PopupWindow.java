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

package com.CH_cl.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import com.CH_co.gui.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

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
public class PopupWindow extends JWindow {

  private Scroller scroller;

  private Point location;
  private Dimension size;

  private float pixelsPerSecond = 250;
  private float framesPerSecond = 50;
  private int pauseMillis = 12000; // pause between scrolling

  private boolean biggerStepsOnSlowMachines = true;

  private boolean dirDown = true;
  private boolean pausing = false;
  private int pausingSoFar;

  private Timer timer = null;

  private int windowShiftPosition = 30; // its zero when its fully closed, and +ve when extended
  private int windowStartsFrom = 30;

  // Last time timer was activated.
  private long lastTime;

  private static final Object objMonitor = new Object();

  /**
   * @returns a single instance of the class.
   */
  public static PopupWindow getSingleInstance() {
    return SingletonHolder.INSTANCE;
  }
  private static class SingletonHolder {
    private static final PopupWindow INSTANCE = new PopupWindow();
  }


  /** Creates new PopupWindow */
  private PopupWindow(float pixelsPerSecond, float framesPerSecond, int pauseMillis) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PopupWindow.class, "PopupWindow()");
    this.pixelsPerSecond = pixelsPerSecond;
    this.framesPerSecond = framesPerSecond;
    this.pauseMillis = pauseMillis;
    init();
    if (trace != null) trace.exit(PopupWindow.class);
  }
  private PopupWindow() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PopupWindow.class, "PopupWindow()");
    init();
    if (trace != null) trace.exit(PopupWindow.class);
  }

  private void init() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PopupWindow.class, "init()");
    scroller = new Scroller(pixelsPerSecond, framesPerSecond, 8000, false);

    JLayeredPane lp = getLayeredPane();
    lp.setLayout(null);

    JLabel background = new JMyLabel(Images.get(ImageNums.WINDOW_POPUP));
    Dimension d = background.getPreferredSize();
    background.setBounds(0, 0, d.width, d.height);
    lp.add(background, JLayeredPane.DEFAULT_LAYER);

    scroller.setBounds(4, 5, d.width-(4+4), d.height-(5+14));
    scroller.setSize(d.width-(4+4), d.height-(5+14));
    scroller.setBorder(new EmptyBorder(0, 0, 0, 0));
    lp.add(scroller, JLayeredPane.PALETTE_LAYER);

    // show
    Rectangle bounds = MiscGui.getScreenBounds(0, 0, this);
    Insets insets = MiscGui.getScreenInsets(0, 0, this);
    this.size = d;
    this.location = new Point(bounds.x+bounds.width - insets.right - d.width - 5, bounds.y+bounds.height - insets.bottom - 5);

    setSize(size);
    setLocation(location.x, location.y + windowShiftPosition);

    if (trace != null) trace.exit(PopupWindow.class);
  }

  public void addForScrolling(JComponent componentToScroll) {
    synchronized (objMonitor) {
      if (pausing) {
        pausingSoFar = 0;
      }
      if (dirDown && !scroller.isRunning()) {
        scroller.removeAll();
      }
      if (dirDown) {
        dirDown = false;
        // Play the sound if direction changes.
        Sounds.playAsynchronous(Sounds.WINDOW_POPUP);
      }
      if (!isShowing()) {
        setVisible(true);
        toFront();
      }

      if (timer == null) {
        timer = new Timer((int) (1000/framesPerSecond), new TimerListener());
        timer.start();
      } else if (!timer.isRunning()) {
        timer.restart();
      }

      scroller.addForScrolling(componentToScroll);
    }
  }

  private Scroller getScroller() {
    return scroller;
  }
  
  /**
   * Hides and resets the window.
   */
  public void dismiss() {
    synchronized (objMonitor) {
      setVisible(false);
      scroller.removeAll();
      dirDown = true;
      pausing = false;
      pausingSoFar = 0;
      windowShiftPosition = windowStartsFrom;
      lastTime = 0;
      if (timer != null)
        timer.stop();
      dispose();
    }
  }

  public void dispose() {
    synchronized (objMonitor) {
      super.dispose();
    }
  }

  private void updateGUI() {
    // Resizing a JWindow causes flickering as that is a heavy peer component and we cannot fix it.
    //setSize(size.width, size.height);//windowShiftPosition);
    setLocation(location.x, location.y + windowShiftPosition);
    validate();
    if (!isShowing()) {
      setVisible(true);
      toFront();
    }
  }

  private class TimerListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      synchronized (objMonitor) {
        long currentTime = System.currentTimeMillis();
        long delayed = currentTime - lastTime;
        if (lastTime == 0) {
          delayed = timer.getDelay();
        }
        lastTime = currentTime;
        if (pausing) {
          pausingSoFar += delayed;
          if (pausingSoFar >= pauseMillis && !scroller.isRunning()) {
            pausingSoFar = 0;
            pausing = false;
            dirDown = true;
          }
        }
        // else if done
        else if (dirDown && windowShiftPosition == windowStartsFrom) {
          timer.stop();
          setVisible(false);
          lastTime = 0;
          dispose();
        }
        // else going up
        else {
          float sp = pixelsPerSecond / framesPerSecond;
          int shiftPixels = !biggerStepsOnSlowMachines ? (int) sp : (int) (sp * (((float) delayed) / ((float) timer.getDelay())));

          if (!dirDown)
            shiftPixels = -shiftPixels;

          // see if we should snap 
          if (dirDown && windowShiftPosition < windowStartsFrom && windowShiftPosition + shiftPixels > windowStartsFrom)
            shiftPixels = windowStartsFrom - windowShiftPosition;
          else if (!dirDown && -windowShiftPosition < size.height && -(windowShiftPosition + shiftPixels) > size.height)
            shiftPixels = -(size.height + windowShiftPosition);

          windowShiftPosition += shiftPixels;

          // see if time to pause
          if (pauseMillis > 0 && windowShiftPosition + size.height == 0)
            pausing = true;

          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              updateGUI();
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
    PopupWindow w = PopupWindow.getSingleInstance();
    try {
      java.io.BufferedReader d = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
      int count = 0;
      while (true) {
        count ++;
        JLabel ll = new JMyLabel("" + count + " : " +d.readLine());
        w.addForScrolling(ll);
      }
    } catch (Throwable t) {
    }
  }
}
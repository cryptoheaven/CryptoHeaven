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
package com.CH_gui.monitor;

import com.CH_co.monitor.*;

import com.CH_co.trace.ThreadTraced;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.util.MiscGui;
import java.awt.*;
import javax.swing.*;

/**
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class SimpleProgMonitorImpl implements ProgMonitorI {

  JLabel jLabelTop, jLabelBottom;
  JProgressBar jProgressBar;
  JFrame frame;

  long totalBytes = 0;
  boolean isClosed = false;
  final Object monitor = new Object();


  public SimpleProgMonitorImpl(long maxProgressValue) {
    frame = new JFrame("Temporary File Cleanup");

    int maxValue = maxProgressValue > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) maxProgressValue;
    jProgressBar = new JProgressBar(0, maxValue);
    jProgressBar.setValue(0);
    jProgressBar.setStringPainted(true);

    jLabelTop = new JLabel("Securely wiping and removing temporary files:");
    jLabelBottom = new JLabel("File:");

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(jLabelTop, BorderLayout.NORTH);
    panel.add(jProgressBar, BorderLayout.CENTER);
    panel.add(jLabelBottom, BorderLayout.SOUTH);
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    frame.setContentPane(panel);
    frame.pack();
    MiscGui.setSuggestedWindowLocation(MainFrame.getSingleInstance(), frame);
    // delay visibility of this frame in case it will close super fast
    Thread th = new ThreadTraced("Delayed Visibility of SimpleProgMonitor") {
      public void runTraced() {
        try { Thread.sleep(1000); } catch (InterruptedException x) { }
        synchronized (monitor) {
          if (!isClosed) {
            frame.setVisible(true);
          }
        }
      }
    };
    th.setDaemon(true);
    th.start();
  }


  public void enqueue(int actionCode, long stamp) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void dequeue(int actionCode, long stamp) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void startSend(int actionCode, long stamp) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void startSendAction(String actionName) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void startSendData(String dataName) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void doneSend(int actionCode, long stamp) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void doneSendAction(String actionName) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void doneSendData(String dataName) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void startReceive(int actionCode, long stamp) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void startReceiveAction(String actionName) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void startReceiveData(String dataName) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void doneReceive(int actionCode, long stamp) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void doneReceiveAction(String actionName) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void doneReceiveData(String dataName) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void startExecution(int actionCode) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void doneExecution(int actionCode) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setCurrentStatus(String currentStatus) {
    // no-op
  }

  public void setFileNameSource(String fileName) {
  }

  public void setFileNameDestination(String fileName) {
    jLabelBottom.setText(fileName);
  }

  public void setFilePathDestination(String filePath) {
    // no-op, we are using short form with file name instead
  }

  public long getTransferred() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setTransferSize(long size) {
    // no-op as we already set collective size for multiple files
  }

  public void updateTransferSize(long size) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void addBytes(long bytes) {
    totalBytes += bytes;
    jProgressBar.setValue((int) Math.min(totalBytes, Integer.MAX_VALUE));
  }

  public void doneTransfer() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void nextTask(String task) {
    // no-op as we are aggregating multiple cleanup/wiping tasks with single progress
  }

  public void appendLine(String info) {
    // no-op
  }

  public void nextTask() {
  }

  public void allDone() {
    synchronized (monitor) {
      isClosed = true;
      frame.setVisible(false);
      frame.dispose();
    }
  }

  public void jobKilled() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void jobForRetry() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setInterrupt(Interruptible interruptible) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setCancellable(Cancellable cancellable) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean isAllDone() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean isCancelled() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean isJobKilled() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
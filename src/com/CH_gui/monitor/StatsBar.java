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

package com.CH_gui.monitor;

import com.CH_cl.service.ops.FileLobUp;
import com.CH_co.monitor.*;
import com.CH_co.util.*;
import com.CH_gui.gui.*;
import com.CH_gui.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;

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
public class StatsBar extends JPanel implements StatsListenerI, DisposableObj {

  private JLabel jLastStatus;
  private long lastStatusStamp;
  private JLabel jPing;
  private JLabel jOnlineStatus;
  private JLabel jConnections;
  private JLabel jTransferRate;
  private JLabel jSize;

  private MouseListener mouseListener;

  public StatsBar() {
    init();
  }

  private void init() {

    jLastStatus = new JMyLabel();
    jPing = new JMyLabel();
    jOnlineStatus = new JMyLabel();
    jConnections = new JMyLabel();
    jTransferRate = new JMyLabel();
    jSize = new JMyLabel();

    // setup a timer to cleanup the status info line
    Timer timer = new Timer(1000, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (Math.abs(System.currentTimeMillis() - lastStatusStamp) > 30000)
          setStatsLastStatus("");
      }
    });
    timer.start(); 

    JPanel jStatusBar = this;
    jStatusBar.setLayout(new GridBagLayout());
    Dimension dim = null;

    dim = new Dimension(80, 14);
    jSize.setMinimumSize(dim);
    jSize.setPreferredSize(dim);

    dim = new Dimension(120, 14);
    jTransferRate.setMinimumSize(dim);
    jTransferRate.setPreferredSize(dim);

    dim = new Dimension(60, 14);
    jPing.setMinimumSize(dim);
    jPing.setPreferredSize(dim);

    dim = new Dimension(26, 14);
    jConnections.setMinimumSize(dim);
    jConnections.setPreferredSize(dim);

    dim = new Dimension(90, 14);
    jOnlineStatus.setMinimumSize(dim);
    jOnlineStatus.setPreferredSize(dim);

    Insets insets = new MyInsets(0, 1, 0, 1);
    Insets insets0 = new MyInsets(0, 0, 0, 0);

    int posX = 0;

    jStatusBar.add(jLastStatus, new GridBagConstraints(posX, 0, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
    posX ++;
    jStatusBar.add(makeStatusSeparator(16), new GridBagConstraints(posX, 0, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, insets0, 0, 0));
    posX ++;

    jStatusBar.add(jSize, new GridBagConstraints(posX, 0, 1, 1, 0, 0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
    posX ++;
    jStatusBar.add(makeStatusSeparator(16), new GridBagConstraints(posX, 0, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, insets0, 0, 0));
    posX ++;

    jStatusBar.add(jTransferRate, new GridBagConstraints(posX, 0, 1, 1, 0, 0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
    posX ++;
    jStatusBar.add(makeStatusSeparator(16), new GridBagConstraints(posX, 0, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, insets0, 0, 0));
    posX ++;

    jStatusBar.add(jPing, new GridBagConstraints(posX, 0, 1, 1, 0, 0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
    posX ++;
    jStatusBar.add(makeStatusSeparator(16), new GridBagConstraints(posX, 0, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, insets0, 0, 0));
    posX ++;

    jStatusBar.add(jConnections, new GridBagConstraints(posX, 0, 1, 1, 0, 0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
    posX ++;
    jStatusBar.add(makeStatusSeparator(16), new GridBagConstraints(posX, 0, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, insets0, 0, 0));
    posX ++;

    jStatusBar.add(jOnlineStatus, new GridBagConstraints(posX, 0, 1, 1, 0, 0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));

    jStatusBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));
    jStatusBar.setBorder(new LineBorder(jStatusBar.getBackground().darker(), 1, true));

    Font font = jLastStatus.getFont().deriveFont(Font.PLAIN);

    jLastStatus.setFont(font);
    jPing.setFont(font);
    jOnlineStatus.setFont(font);
    jConnections.setFont(font);
    jTransferRate.setFont(font);
    jSize.setFont(font);

    jLastStatus.setHorizontalAlignment(JLabel.LEFT);
    jLastStatus.setIcon(Images.get(ImageNums.ANIM_GLOBE_FIRST16));

    jLastStatus.setToolTipText("Last reported status.");
    jPing.setToolTipText("Network Delay");
    jOnlineStatus.setToolTipText("Indicates client online/offline status");
    jConnections.setToolTipText("Number of open connections to the server.");
    jTransferRate.setToolTipText("Current cummulative transfer rate for all connections.");
    jSize.setToolTipText("Size summary of selected objects.");
  }

  private static JComponent makeStatusSeparator(int sepSize) {
    JPanel sep = new JPanel();
    sep.setLayout(null);
    sep.setSize(new java.awt.Dimension(1, sepSize));
    sep.setMaximumSize(new java.awt.Dimension(1, sepSize));
    sep.setMinimumSize(new java.awt.Dimension(1, sepSize));
    sep.setPreferredSize(new java.awt.Dimension(1, sepSize));
    sep.setBackground(sep.getBackground().darker());
    return sep;
  }

  public void adjustFontSize(AffineTransform transform) {
    Font font = null;
    if (jLastStatus != null)
      font = jLastStatus.getFont().deriveFont(transform);
    if (font != null) {
      if (jLastStatus != null) jLastStatus.setFont(font);
      if (jPing != null) jPing.setFont(font);
      if (jOnlineStatus != null) jOnlineStatus.setFont(font);
      if (jConnections != null) jConnections.setFont(font);
      if (jTransferRate != null) jTransferRate.setFont(font);
      if (jSize != null) jSize.setFont(font);
    }
  }

  public JLabel getStatusLabel() {
    return jLastStatus;
  }
  public JLabel getPingLabel() {
    return jPing;
  }
  public JLabel getOnlineLabel() {
    return jOnlineStatus;
  }
  public JLabel getConnectionsLabel() {
    return jConnections;
  }
  public JLabel getTransferRateLabel() {
    return jTransferRate;
  }
  public JLabel getSizeLabel() {
    return jSize;
  }

  public void installListeners() {
    Stats.addStatsListener(this);
    if (mouseListener == null) {
      mouseListener = new StatusMouseListener();
      jLastStatus.addMouseListener(mouseListener);
    }
  }

  public void uninstallListeners() {
    Stats.removeStatsListener(this);
    if (mouseListener != null) {
      jLastStatus.removeMouseListener(mouseListener);
      mouseListener = null;
    }
  }

/***********************************************
 * StatsListenerI interface methods
 * *********************************************/

  public void setStatsConnections(Integer connectionsPlain, Integer connectionsHTML) {
    int connectionCount = (connectionsPlain != null ? connectionsPlain.intValue() : 0) + (connectionsHTML != null ? connectionsHTML.intValue() : 0);
    jConnections.setText("" + connectionCount);
    jConnections.setToolTipText("<html>Number of open socket connections: <b>" + connectionsPlain +
                                "</b><br>Number of open HTTP sockets: <b>" + connectionsHTML + "</b>");
    if (connectionCount > 0) {
      jOnlineStatus.setText("Online");
      jOnlineStatus.setIcon(Images.get(ImageNums.LIGHT_GREEN_SMALL));
    } else {
      jOnlineStatus.setText("Offline");
      jOnlineStatus.setIcon(Images.get(ImageNums.LIGHT_X_SMALL));
    }
  }

  public void setStatsGlobeMove(Boolean isMoving) {
    if (isMoving != null && isMoving.booleanValue()) {
      jLastStatus.setIcon(Images.get(ImageNums.ANIM_GLOBE16));
    } else {
      jLastStatus.setIcon(Images.get(ImageNums.ANIM_GLOBE_FIRST16));
    }
  }

  public void setStatsLastStatus(String status) {
    jLastStatus.setText(status);
    lastStatusStamp = System.currentTimeMillis();
  }

  public void setStatsPing(Long pingMS) {
    if (pingMS == null)
      jPing.setText(null);
    else {
      long ms = pingMS.longValue();
      if (ms > 2000)
        jPing.setText("" + ((int) (ms/1000)) + " s");
      else if (ms >= 0)
        jPing.setText("" + ms + " ms");
    }
  }

  public void setStatsSizeBytes(Long sizeBytes) {
    long size = sizeBytes != null ? sizeBytes.longValue() : -1L;
    String sizeS = null;
    if (size >= 0)
      sizeS = Misc.getFormattedSize(size, 4, 3);
    if (sizeS != null && sizeS.length() > 0) {
      jSize.setText(sizeS);
    } else {
      jSize.setText(null);
    }
  }

  public void setStatsTransferRate(Long transferRate) {
    long bytesPerSecond = transferRate != null ? transferRate.longValue() : -1L;
    String size = Misc.getFormattedSize(bytesPerSecond, 4, 3);
    if (size != null && size.length() > 0) {
      if (bytesPerSecond > 0) {
        jTransferRate.setIcon(Images.get(ImageNums.LIGHT_ON_SMALL));
      } else {
        jTransferRate.setIcon(Images.get(ImageNums.LIGHT_OFF_SMALL));
      }
      jTransferRate.setText(size + "/sec");
    } else {
      jTransferRate.setIcon(Images.get(ImageNums.LIGHT_OFF_SMALL));
      jTransferRate.setText(null);
    }
    // update upload summary info
    String summary = FileLobUp.getSummary();
    if (summary != null)
      setStatsLastStatus(summary);
    else {
      String status = getStatusLabel().getText();
      if (status != null && status.startsWith("Uploading"))
        setStatsLastStatus("");
    }
  }
  public void setStatsTransferRateIn(Long transferRate) {
  }
  public void setStatsTransferRateOut(Long transferRate) {
  }

  public void disposeObj() {
    uninstallListeners();
  }

  private class StatusMouseListener extends MouseAdapter {
    public void mouseClicked(MouseEvent e) {
      JPopupMenu popup = new JPopupMenu("Status History");
      JTextArea textArea = new JMyTextArea(10, 55);
      MiscGui.initKeyBindings(textArea);
      textArea.setLineWrap(false);
      textArea.setEditable(false);
      JScrollPane textPane = new JScrollPane(textArea);
      StringBuffer sb = new StringBuffer();
//      ArrayList moversTraceL = Stats.getGlobeMoversTraceL();
//      if (moversTraceL != null && moversTraceL.size() > 0) {
//        sb.append("Spinning Globe\n");
//      }
      String upSummary = FileLobUp.getSummary();
      if (upSummary != null)
        sb.append(upSummary).append("\n");
      String upProgress = FileLobUp.getProgress();
      if (upProgress != null)
        sb.append(upProgress).append("\n");

      ArrayList[] historyLists = Stats.getStatsHistoryLists();
      Iterator iterS = historyLists[0].iterator();
      Iterator iterD = historyLists[1].iterator();
      boolean next = false;
      while (iterS.hasNext()) {
        String nextStatus = (String) iterS.next();
        Date nextStamp = (Date) iterD.next();
        if (next)
          sb.append('\n');
        next = true;
        sb.append(Misc.getFormattedDate(nextStamp));
        sb.append("   ");
        sb.append(nextStatus);
      }
//      if (moversTraceL != null && moversTraceL.size() > 0) {
//        sb.append("\n\nSpinning Globe traces:\n");
//        for (int i=0; i<moversTraceL.size(); i++) {
//          sb.append("\nRunning Action: ").append(moversTraceL.get(i));
//        }
//      }
      textArea.setText(sb.toString());
      textArea.setCaretPosition(0);
      popup.add(textPane);
      popup.show(jLastStatus, 0, jLastStatus.getSize().height);
    }
  }
}
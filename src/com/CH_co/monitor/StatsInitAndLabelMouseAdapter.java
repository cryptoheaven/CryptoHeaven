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

import java.sql.Timestamp;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import com.CH_co.gui.*;
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
 * <b>$Revision: 1.13 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class StatsInitAndLabelMouseAdapter extends MouseAdapter {

  /** Creates new StatsInitAndLabelMouseAdapter */
  public StatsInitAndLabelMouseAdapter() {
    initialize();
  }

  public static void initialize() {
    if (!MiscGui.isAllGUIsuppressed()) {
      Font font = Stats.jLastStatus.getFont().deriveFont(Font.PLAIN);

      Stats.jLastStatus.setFont(font);
      Stats.jPing.setFont(font);
      Stats.jOnlineStatus.setFont(font);
      Stats.jConnections.setFont(font);
      Stats.jTransferRate.setFont(font);
      Stats.jSize.setFont(font);

      Stats.jLastStatus.setHorizontalAlignment(JLabel.LEFT);
      Stats.jLastStatus.setIcon(Stats.staticPic);

      Stats.jLastStatus.setToolTipText("Last reported client-server request-reply status.");
      Stats.jPing.setToolTipText("Network Delay");
      Stats.jOnlineStatus.setToolTipText("Indicates client online/offline status");
      Stats.jConnections.setToolTipText("Number of open connections to the server.");
      Stats.jTransferRate.setToolTipText("Current cummulative transfer rate for all connections.");
      Stats.jSize.setToolTipText("Size summary of selected objects.");
    }
  }

  public void mouseClicked(MouseEvent e) {
    JPopupMenu popup = new JPopupMenu("Status History");
    JTextArea textArea = new JMyTextArea(10, 55);
    MiscGui.initKeyBindings(textArea);
    textArea.setLineWrap(false);
    textArea.setEditable(false);
    JScrollPane textPane = new JScrollPane(textArea);
    StringBuffer sb = new StringBuffer();
    synchronized (Stats.monitor) {
      Vector moversTraceV = Stats.getGlobeMoversTraceV();
      if (moversTraceV != null && moversTraceV.size() > 0) {
        sb.append("Spinning Globe\n");
      }
      Iterator iterS = Stats.statusHistoryL.iterator();
      Iterator iterD = Stats.statusHistoryDatesL.iterator();
      boolean next = false;
      while (iterS.hasNext()) {
        String nextStatus = (String) iterS.next();
        Date nextStamp = (Date) iterD.next();
        if (next)
          sb.append('\n');
        next = true;
        sb.append(Misc.getFormattedDate(new Timestamp(nextStamp.getTime())));
        sb.append("   ");
        sb.append(nextStatus);
      }
      if (moversTraceV != null && moversTraceV.size() > 0) {
        sb.append("\n\nSpinning Globe traces:\n");
        for (int i=0; i<moversTraceV.size(); i++) {
          sb.append("\nRunning Action: " + moversTraceV.elementAt(i));
        }
      }
    }
    textArea.setText(sb.toString());
    textArea.setCaretPosition(0);
    popup.add(textPane);
    popup.show(Stats.jLastStatus, 0, Stats.jLastStatus.getSize().height);
  }
}
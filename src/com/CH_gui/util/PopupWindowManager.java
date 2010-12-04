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

import com.CH_co.service.records.*;
import com.CH_co.util.*;

import com.CH_gui.actionGui.JActionFrameClosable;
import com.CH_gui.list.*;
import com.CH_gui.msgs.*;

import com.CH_guiLib.util.HTML_Ops;

import java.awt.*;
import java.awt.event.*;

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
 * <b>$Revision: 1.17 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class PopupWindowManager extends Object {

  private static long lastMsgStamp;

  /**
   * @param rec MsgDataRecord for which to store the last stamp
   * @return true if it was marked, false if specified msg was older than last marked stamp
   */
  public static boolean markNewMsgStamp(MsgDataRecord msgData) {
    boolean isNew = false;
    long msgStamp = msgData.dateCreated.getTime();
    if (msgStamp > lastMsgStamp) {
      isNew = true;
      lastMsgStamp = msgStamp;
    }
    return isNew;
  }
  public static void addForScrolling(final Component[] componentBuffer, MsgDataRecord msgData, boolean suppressIsNewCheck) {
    try {
      if (suppressIsNewCheck || markNewMsgStamp(msgData)) {
        String user = ListRenderer.getRenderedText(MsgPanelUtils.convertUserIdToFamiliarUser(msgData.senderUserId, false, true));
        final String sub = msgData.isTypeAddress() ? msgData.fileAs : msgData.getSubject();
        final boolean addSub = sub != null && sub.length() > 0;

        String body = null;
        if (msgData.isHtml()) {
          body = msgData.isTypeAddress() ? msgData.addressBody : HTML_Ops.clearHTMLheaderAndConditionForDisplay(msgData.getText(), true, true, true);
        } else {
          body = msgData.getEncodedHTMLData();
        }
        // body could be null in rare connectivity/synchronization problems when client missed body fetch reply
        if (body != null) {
          String msgBody = (addSub ? ("<b>" + sub + "</b> ") : "") + body;
          String htmlText = "<html><body><font face="+HTML_utils.DEFAULT_FONTS_QUOTED+" size='-1'>"
                  + "<img src=\"images/" + ImageNums.getImageName(ImageNums.CHAT16) + ".png\" height=\"16\" width=\"16\">&nbsp;"
                  + Misc.encodePlainIntoHtml(user)
                  + ":&nbsp;"
                  + msgBody;
          HTML_ClickablePane msgPane = new HTML_ClickablePane(htmlText);
          PopupWindow.getSingleInstance().addForScrolling(msgPane);
          msgPane.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
              if (componentBuffer[0] instanceof Window) {
                Window w = (Window) componentBuffer[0];
                if (w instanceof JActionFrameClosable && ((JActionFrameClosable) w).isClosed()) {
                  // already closed, no-op
                } else {
                  if (!w.isShowing()) {
                    w.setVisible(true);
                  }
                  if (w instanceof Frame) {
                    ((Frame)w).setState(Frame.NORMAL);
                  }
                  w.toFront();
                }
              }
              PopupWindow.getSingleInstance().dismiss();
              e.consume();
            }
          });
        }
      }
    } catch (Throwable t) {
    }
  }

}
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

package com.CH_gui.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.html.*;

import com.CH_cl.util.*;

import com.CH_co.service.records.*;
import com.CH_co.util.*;

import com.CH_gui.actionGui.JActionFrameClosable;
import com.CH_gui.list.*;
import com.CH_gui.msgs.*;

import com.CH_guiLib.gui.*;
import com.CH_guiLib.util.*;

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
 * <b>$Revision: 1.17 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class PopupWindowManager extends Object {

  public static void addForScrolling(final Component component, MsgDataRecord msgData) {
    try {
      String user = ListRenderer.getRenderedText(MsgPanelUtils.convertUserIdToFamiliarUser(msgData.senderUserId, false, true));
      final String sub = msgData.getSubject();
      final boolean addSub = sub != null && sub.length() > 0;

      String body = null;
      if (msgData.isHtmlMail()) {
        body = HTML_utils.clearHTMLheaderAndConditionForDisplay(msgData.getText(), true, true, true);
      } else {
        body = msgData.getEncodedHTMLData();
      }
      String msgBody = (addSub ? ("<b>" + sub + "</b> ") : "") + body;
      String hrefStart = "<html><body><font face="+HTML_utils.DEFAULT_FONTS_QUOTED+" size='-1'><a href=\""+user+"\">";
      String hrefEnd = "</a>";
      String text = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("USER_says_-_MESSAGE"), new Object[] {hrefStart, user, hrefEnd, msgBody});
      final JTextPane msgPane = new JMyTextPane();
      MiscGui.initKeyBindings(msgPane);
      msgPane.setContentType("text/html");
      msgPane.setText(text);
      HTML_ClickablePane.setBaseToDefault((HTMLDocument) msgPane.getDocument());  // editor base to display images....
      Font f = msgPane.getFont();
      msgPane.setFont(f.deriveFont(f.getSize2D()-1));
      msgPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      PopupWindow.getSingleInstance().addForScrolling(msgPane);
      msgPane.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          if (component instanceof Window) {
            Window w = (Window) component;
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
    } catch (Throwable t) {
    }
  }
}
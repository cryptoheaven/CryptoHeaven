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

package com.CH_co.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.CH_co.trace.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: Shows a single modal message dialog per specified key and arbiter.
 *                    Repeated calls are ignored.
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.18 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class SingleDialogShower extends ThreadTraced {

  private SingleTokenArbiter arbiter;
  private Object key;
  private Component parent;
  private int messageType;
  private String title;
  private String msg;
  private boolean tokenRemoved;

  /** Creates new SingleDialogShower */
  public SingleDialogShower(SingleTokenArbiter arbiter, Object key, Component parent, int messageType, String title, String msg) {
    super(title);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SingleDialogShower.class, "SingleDialogShower(SingleTokenArbiter arbiter, Object key, Component parent, int messageType, String title, String msg)");
    if (trace != null) trace.args(arbiter, key, parent);
    if (trace != null) trace.args(messageType);
    if (trace != null) trace.args(title, msg);
    this.arbiter = arbiter;
    this.key = key;
    this.parent = parent;
    this.messageType = messageType;
    this.title = title;
    this.msg = msg;
    if (trace != null) trace.exit(SingleDialogShower.class);
  }

  public void runTraced() {
    if (!MiscGui.isAllGUIsuppressed()) {
      final Object token = new Object();
      if (arbiter.putToken(key, token)) {
        ActionListener closeAction = new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            Object source = event.getSource();
            if (source instanceof AbstractButton) {
              AbstractButton button = (AbstractButton) source;
              button.removeActionListener(this);
              Window w = SwingUtilities.windowForComponent(button);
              if (w != null) {
                w.setVisible(false);
                w.dispose();
              }
            }
          }
        };
        JDialog d = MessageDialog.showDialog(parent, msg, title, messageType, null, closeAction, false);
        d.addWindowListener(new WindowAdapter() {
          public synchronized void windowClosed(WindowEvent e) {
            // prevent multiple execution due to event system bugs
            if (!tokenRemoved) {
              arbiter.removeToken(key, token);
              tokenRemoved = true;
            }
          }
        });
      }
    }
  }
}
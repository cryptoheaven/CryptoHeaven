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

import com.CH_co.util.*;
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
public class NotificationShowerImpl implements NotificationShowerI {

  /**
   * No-argument constructor for the factory.
   */
  public NotificationShowerImpl() {
  }

  public void show(final int type, final String title, final String msg) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        MessageDialog.showDialog(null, msg, title, type, false);
      }
    });
  }

  public void show(final SingleTokenArbiter arbiter, final Object key, final int type, final String title, final String msg) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        MessageDialog.showDialog(arbiter, key, null, msg, title, type);
      }
    });
  }

  public void showYesNo(final int type, final String title, final String msg, final Runnable yes, final Runnable no) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        boolean rc = MessageDialog.showDialogYesNo(null, msg, title, type);
        if (rc) {
          if (yes != null)
            yes.run();
        } else {
          if (no != null)
            no.run();
        }
      }
    });
  }

}
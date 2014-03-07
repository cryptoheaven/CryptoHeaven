/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.util;

import com.CH_co.util.*;
import javax.swing.SwingUtilities;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
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

  public void showYesNo(final int type, final String title, final String msg, final boolean highlightButtonYes, final Runnable yes, final Runnable no) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        String strYes = null;
        String strNo = null;
        if (yes instanceof NamedRunnable)
          strYes = ((NamedRunnable) yes).getName();
        if (no instanceof NamedRunnable)
          strNo = ((NamedRunnable) no).getName();
        boolean rc = MessageDialog.showDialogYesNo(null, msg, title, type, strYes, strNo, highlightButtonYes);
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
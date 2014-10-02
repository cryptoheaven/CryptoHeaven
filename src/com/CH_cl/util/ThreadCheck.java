/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.util;

import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;
import com.CH_co.util.NotificationCenter;

/** 
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.1 $</b>
*
* @author  Marcin Kurzawa
*/
public class ThreadCheck {

  public static void warnIfOnAWTthread() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ThreadCheck.class, "warnIfOnAWTthread()");
    // setup
    String part = "AWT";
    boolean isCheckGroupAlso = true;
    boolean needMatch = true;
    // check
    Thread th = Thread.currentThread();
    ThreadGroup thGroup = th.getThreadGroup();
    String thName = th.getName();
    String thGroupName = null;
    if (thGroup != null)
      thGroupName = thGroup.getName();
    if (needMatch == (thName.indexOf(part) >= 0 || (isCheckGroupAlso && thGroupName != null && thGroupName.indexOf(part) >= 0))) {
      String messageText = "This Warning should be displayed only to users with ID < 100\n\nAWT Thread " + thName + " (group " + thGroupName + ") at \n\n" + Misc.getStack(new Exception("Execution on AWT Thread detected!"));
      String title = "Warning: Using AWT Thread";
      if (trace != null) trace.info(100, messageText);
      NotificationCenter.show(NotificationCenter.WARNING_MESSAGE, title, messageText);
      System.out.println(title);
      System.out.println(messageText);
      System.out.println();
    }
    if (trace != null) trace.exit(ThreadCheck.class);
  }

}
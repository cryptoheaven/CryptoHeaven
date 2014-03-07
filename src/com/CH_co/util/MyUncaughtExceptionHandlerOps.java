/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.util;

import com.CH_co.monitor.Stats;
import com.CH_co.monitor.StatsListenerAdapter;
import java.lang.reflect.Method;

/**
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
* 
* Class Description:
* 
*
* Class Details:
* 
*
* <b>$Revision: 1.2 $</b>
*
* @author Marcin Kurzawa
* @version
*/
public class MyUncaughtExceptionHandlerOps {

  private static boolean isAttachAttempted;
  private static Object exceptionHandler;
  private static String tag;

  public static void attachDefaultHandler(String theTag) {
    if (!isAttachAttempted) {
      isAttachAttempted = true;
      try {
        tag = theTag;
        Class handlerClass = null;
        try {
          handlerClass = Class.forName("com.CH_cl.util.MyUncaughtExceptionHandler");
        } catch (Throwable t) {
          // try again, maybe obfuscation changed the package
          handlerClass = Class.forName(MyUncaughtExceptionHandlerOps.class.getPackage().getName()+".MyUncaughtExceptionHandler");
        }
        Object handlerImpl = handlerClass.newInstance();
        if (handlerImpl != null) {
          Thread.UncaughtExceptionHandler handler = (Thread.UncaughtExceptionHandler) handlerImpl;
          Thread.setDefaultUncaughtExceptionHandler(handler);
          exceptionHandler = handler;
        }
        Stats.registerStatsListener(new StatsListenerAdapter() {
          public void setStatsConnections(Integer connectionsPlain, Integer connectionsHTML) {
            Thread th = new Thread() {
              public void run() {
                try {
                  Class handlerClass = Class.forName("com.CH_cl.util.MyUncaughtExceptionHandler");
                  Method methodSend = handlerClass.getMethod("crashReport_triggerAnyPendingIfPossible", null);
                  methodSend.invoke(null, null);
                } catch (Throwable t) {
                  t.printStackTrace();
                  // This is JRE 1.5 code, so catch all errors!
                }
              }
            };
            th.setDaemon(true);
            th.start();
          }
        });
      } catch (Throwable t) {
        // This is JRE 1.5 code, so catch all errors!
      }
    }
  }

  public static void unhandledException(Throwable ex) {
    if (exceptionHandler != null) {
      try {
        ((Thread.UncaughtExceptionHandler) exceptionHandler).uncaughtException(Thread.currentThread(), ex);
      } catch (Throwable t) {
      }
    }
  }

  public static void setTag(String theTag) {
    tag = theTag;
  }

  public static String getTag() {
    return tag;
  }

}
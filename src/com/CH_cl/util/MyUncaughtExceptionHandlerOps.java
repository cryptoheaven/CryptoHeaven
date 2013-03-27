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
package com.CH_cl.util;

import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_co.monitor.Stats;
import com.CH_co.monitor.StatsListenerAdapter;
import com.CH_co.util.GlobalProperties;
import java.lang.reflect.Method;

/**
* <b>Copyright</b> &copy; 2001-2013 <a
* href="http://www.CryptoHeaven.com/DevelopmentTeam/"> CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
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

  private static boolean isAttached;
  private static ServerInterfaceLayer SIL;

  public static void attachHandler(String platformTag) {
    if (!isAttached) {
      isAttached = true;
      try {
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
          Method setTag = handlerImpl.getClass().getMethod("setTag", new Class[] {String.class});
          setTag.invoke(handlerImpl, new Object[] {platformTag});
          Thread.setDefaultUncaughtExceptionHandler(handler);
        }
        Stats.registerStatsListener(new StatsListenerAdapter() {
          public void setStatsConnections(Integer connectionsPlain, Integer connectionsHTML) {
            Thread th = new Thread() {
              public void run() {
                try {
                  // Don't use reflection in listener as we know that handler was already setup so it is present and working.
                  MyUncaughtExceptionHandler.crashReport_sendAnyPendingIfPossible();
                } catch (Throwable t) {
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

  protected static ServerInterfaceLayer getSIL() {
    return SIL;
  }

  public static void setSIL(ServerInterfaceLayer theSIL) {
    SIL = theSIL;
  }

}
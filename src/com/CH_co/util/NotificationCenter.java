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

package com.CH_co.util;

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
public class NotificationCenter {

  public static final int QUESTION_MESSAGE = 9001;
  public static final int INFORMATION_MESSAGE = 9002;
  public static final int WARNING_MESSAGE = 9003;
  public static final int ERROR_MESSAGE = 9004;
  public static final int RECYCLE_MESSAGE = 9005;
  public static final int DELETE_MESSAGE = 9006;
  public static final int EMPTY_RECYCLE_FOLDER = 9007;
  public static final int EMPTY_SPAM_FOLDER = 9008;

  private static Class implNotificationCenterI;

  public static void setImpl(Class notificationCenterImpl) {
    implNotificationCenterI = notificationCenterImpl;
  }

  public static void show(int type, String title, String msg) {
    if (implNotificationCenterI != null) {
      try {
        NotificationShowerI impl = (NotificationShowerI) implNotificationCenterI.newInstance();
        impl.show(type, title, msg);
      } catch (Throwable t) {
      }
    }
  }
  public static void show(int type, String title, String msg, boolean modal) {
    if (implNotificationCenterI != null) {
      try {
        NotificationShowerI impl = (NotificationShowerI) implNotificationCenterI.newInstance();
        impl.show(type, title, msg, modal);
      } catch (Throwable t) {
      }
    }
  }
  public static void show(final SingleTokenArbiter arbiter, final Object key, int type, String title, String msg) {
    if (implNotificationCenterI != null) {
      try {
        NotificationShowerI impl = (NotificationShowerI) implNotificationCenterI.newInstance();
        impl.show(arbiter, key, type, title, msg);
      } catch (Throwable t) {
      }
    }
  }
  public static boolean showYesNo(int type, String title, String msg, boolean defaultChoice) {
    boolean rc = defaultChoice;
    if (implNotificationCenterI != null) {
      try {
        NotificationShowerI impl = (NotificationShowerI) implNotificationCenterI.newInstance();
        rc = impl.showYesNo(type, title, msg);
      } catch (Throwable t) {
      }
    }
    return rc;
  }

}
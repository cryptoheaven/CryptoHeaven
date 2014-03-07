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

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public interface NotificationShowerI {

  public void show(int type, String title, String msg);
  public void show(final SingleTokenArbiter arbiter, final Object key, int type, String title, String msg);
  public void showYesNo(int type, String title, String msg, boolean highlightButtonYes, Runnable yes, Runnable no);

}
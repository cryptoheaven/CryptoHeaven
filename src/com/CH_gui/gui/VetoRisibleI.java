/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.gui;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.6 $</b>
 *
 * @author  Marcin Kurzawa
 */
public interface VetoRisibleI {

  public static int TYPE_WINDOW_CLOSE = 1;
  public static int TYPE_SAVE = 2;

  public boolean isVetoRaised(int type);

}
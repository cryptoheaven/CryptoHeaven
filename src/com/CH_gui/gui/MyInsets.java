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

import java.awt.Insets;
import com.CH_gui.util.MiscGui;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class MyInsets extends Insets {

  /** Creates new MyInsets */
  public MyInsets(int top, int left, int bottom, int right) {
    super(t(top), t(left), t(bottom), t(right));
  }

  private static int t(int i) {
    int ii = MiscGui.isSmallScreen() ? (i+1) / 2 : i;
    return ii;
  }

}
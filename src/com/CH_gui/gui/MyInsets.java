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

package com.CH_gui.gui;

import java.awt.Insets;
import com.CH_gui.util.MiscGui;

/**
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: $</b>
 * @author  Marcin Kurzawa
 * @version
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
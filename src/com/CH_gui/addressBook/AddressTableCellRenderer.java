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

package com.CH_gui.addressBook;

import com.CH_gui.util.Images;
import java.awt.*;
import javax.swing.*;

import com.CH_co.util.*;

import com.CH_gui.msgTable.*;

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
 * <b>$Revision: 1.7 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class AddressTableCellRenderer extends MsgTableCellRenderer {

  //private static Color addressAltColor = new Color(241, 255, 231);
  //private static Color addressAltColorSelected = new Color(202, 214, 194);
  private static Color addressAltColor = new Color(234, 244, 252, ALPHA);
  private static Color addressAltColorSelected = new Color(198, 206, 213, ALPHA);
  private static Color[] altBkColors = new Color[] { addressAltColor, addressAltColorSelected };

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    int rawColumn = getRawColumn(table, column);

    // Email Address
    if (rawColumn == 16) {
      if (((String) value).length() > 0)
        setIcon(Images.get(ImageNums.EMAIL_SYMBOL_SMALL));
    }

    return renderer;
  }

  /**
   * Provide alternate row background colors.
   */
  public Color[] getAltBkColors() {
    return altBkColors;
  }

}
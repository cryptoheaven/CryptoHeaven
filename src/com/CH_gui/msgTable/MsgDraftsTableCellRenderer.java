/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.msgTable;

import java.awt.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.7 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class MsgDraftsTableCellRenderer extends MsgTableCellRenderer {

  //private static Color sentMsgAltColor = new Color(233, 238, 251);
  //private static Color sentMsgAltColorSelected = new Color(188, 193, 207);
  private static Color sentMsgAltColor = new Color(241, 255, 231, ALPHA);
  private static Color sentMsgAltColorSelected = new Color(202, 214, 194, ALPHA);
  private static Color[] altBkColors = new Color[] { sentMsgAltColor, sentMsgAltColorSelected };

//  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//    Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//    return renderer;
//  }

  /**
   * Provide alternate row background colors.
   */
  public Color[] getAltBkColors() {
    return altBkColors;
  }

}
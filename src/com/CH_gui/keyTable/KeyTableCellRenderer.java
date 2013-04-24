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

package com.CH_gui.keyTable;

import com.CH_cl.service.cache.CacheUsrUtils;
import com.CH_co.service.records.KeyRecord;
import com.CH_co.service.records.Record;
import com.CH_co.service.records.UserRecord;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.service.records.RecordUtilsGui;
import com.CH_gui.sortedTable.JSortedTable;
import com.CH_gui.table.RecordTableCellRenderer;
import java.awt.Component;
import javax.swing.JTable;

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
 * <b>$Revision: 1.14 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class KeyTableCellRenderer extends RecordTableCellRenderer {

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    int rawColumn = getRawColumn(table, column);

    // Key
    if (rawColumn == 0) {
      if (value != null) {
        setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
        JSortedTable jSortedTable = (JSortedTable) table;
        KeyRecord kRec = (KeyRecord) (((KeyTableModel) jSortedTable.getRawModel()).getRowObject(jSortedTable.convertMyRowIndexToModel(row)));
        setIcon(RecordUtilsGui.getIcon(kRec));
      }
    }

    // Owner
    else if (rawColumn == 1) {
      if (value != null) {
        setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
        JSortedTable jSortedTable = (JSortedTable) table;
        KeyRecord kRec = (KeyRecord) (((KeyTableModel) jSortedTable.getRawModel()).getRowObject(jSortedTable.convertMyRowIndexToModel(row)));
        // use my contact list only, not the reciprocal contacts
        Record owner = CacheUsrUtils.convertUserIdToFamiliarUser(kRec.ownerUserId, true, false);
        if (owner == null) {
          UserRecord uRec = new UserRecord();
          uRec.userId = kRec.keyId;
          uRec.handle = com.CH_cl.lang.Lang.rb.getString("User");
          owner = uRec;
        }
        setText(ListRenderer.getRenderedText(owner));
        setIcon(ListRenderer.getRenderedIcon(owner));
      }
    }

    // if we have too little space, set tool tip
    setDefaultToolTip(this, table, row, column);

    return this;
  }


}
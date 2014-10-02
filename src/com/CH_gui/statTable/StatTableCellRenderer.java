/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.statTable;

import com.CH_cl.service.cache.CacheUsrUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_co.service.records.Record;
import com.CH_co.service.records.StatRecord;
import com.CH_co.service.records.UserRecord;
import com.CH_co.util.ImageNums;
import com.CH_co.util.Misc;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.sortedTable.JSortedTable;
import com.CH_gui.table.RecordTableCellRenderer;
import com.CH_gui.util.Images;
import java.awt.Component;
import java.sql.Timestamp;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/** 
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* Class Description:
* This class renderers cells of a table, where stats' information is displayed
*
* Class Details:
* 
*
* <b>$Revision: 1.18 $</b>
*
* @author  Marcin Kurzawa
*/

public class StatTableCellRenderer extends RecordTableCellRenderer {

  private final FetchedDataCache cache = FetchedDataCache.getSingleInstance();

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    // User info
    if (value instanceof String) {
      // Find the stat record
      JSortedTable sTable = (JSortedTable) table;
      TableModel tableModel = sTable.getRawModel();
      if (tableModel instanceof StatTableModel) {
        StatTableModel tm = (StatTableModel) tableModel;
        StatRecord statRecord = (StatRecord) tm.getRowObject(sTable.convertMyRowIndexToModel(row));

        UserRecord uRec = cache.getUserRecord(statRecord.ownerUserId);

        if (uRec != null) {
          // use my contact list only, not the reciprocal contacts
          Record rec = CacheUsrUtils.convertUserIdToFamiliarUser(cache, uRec.userId, true, false);
          setIcon(ListRenderer.getRenderedIcon(rec));
          setText(ListRenderer.getRenderedText(rec));
        }
        else {
          setText(java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("User_(USER-ID)"), new Object[] {statRecord.ownerUserId}));
          setIcon(Images.get(ImageNums.PERSON16));
        }

        setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
      }
    }

    // detailed timestamp for trace tables
    else if (value instanceof Timestamp) {
      setText(Misc.getFormattedTimestamp((Timestamp) value));
    }

    return this;
  }


}
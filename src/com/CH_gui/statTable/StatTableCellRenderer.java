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

package com.CH_gui.statTable;

import com.CH_gui.util.Images;
import java.awt.*;
import java.sql.Timestamp;
import javax.swing.JTable;
import javax.swing.table.*;

import com.CH_gui.list.ListRenderer;
import com.CH_gui.msgs.MsgPanelUtils;
import com.CH_gui.sortedTable.JSortedTable;
import com.CH_gui.table.*;

import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 * This class renderers cells of a table, where stats' information is displayed
 *
 * Class Details:
 * 
 *
 * <b>$Revision: 1.18 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */

public class StatTableCellRenderer extends RecordTableCellRenderer {

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

        UserRecord uRec = FetchedDataCache.getSingleInstance().getUserRecord(statRecord.ownerUserId);

        if (uRec != null) {
          Record rec = MsgPanelUtils.convertUserIdToFamiliarUser(uRec.userId, true, true);
          setIcon(ListRenderer.getRenderedIcon(rec));
          setText(ListRenderer.getRenderedText(rec));
        }
        else {
          setText(java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("User_(USER-ID)"), new Object[] {statRecord.ownerUserId}));
          setIcon(Images.get(ImageNums.PERSON_SMALL));
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
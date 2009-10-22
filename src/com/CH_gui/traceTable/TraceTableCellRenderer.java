/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_gui.traceTable;

import java.awt.*;
import java.sql.Timestamp;
import javax.swing.JTable;
import javax.swing.table.*;

import com.CH_gui.list.ListRenderer;
import com.CH_gui.msgs.MsgPanelUtils;
import com.CH_gui.table.*;
import com.CH_gui.sortedTable.JSortedTable;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 * This class renderers cells of a table, where traces' information is displayed
 *
 * Class Details:
 * 
 *
 * <b>$Revision: 1.21 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */

public class TraceTableCellRenderer extends RecordTableCellRenderer {

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    // User info OR object name
    if (value instanceof String) {
      int rawColumn = ((RecordTableModel) ((JSortedTable) table).getRawModel()).getColumnHeaderData().convertColumnToRawModel(table.convertColumnIndexToModel(column));
      
      // Find the trace record
      JSortedTable sTable = (JSortedTable) table;
      TableModel tableModel = sTable.getRawModel();
      if (tableModel instanceof TraceTableModel) {
        TraceTableModel tm = (TraceTableModel) tableModel;
        TraceRecord traceRecord = (TraceRecord) tm.getRowObject(sTable.convertMyRowIndexToModel(row));

        // Object name
        if (rawColumn == 0) {
          Record rec = tm.getTracedObjRecord(traceRecord.objId);
          setIcon(ListRenderer.getRenderedIcon(rec));
          setText(ListRenderer.getRenderedText(rec, true, true, true));
        }
        // User info
        else if (rawColumn == 3) {
          UserRecord uRec = FetchedDataCache.getSingleInstance().getUserRecord(traceRecord.ownerUserId);

          if (uRec != null) {
            Record rec = MsgPanelUtils.convertUserIdToFamiliarUser(uRec.userId, true, true);
            setIcon(ListRenderer.getRenderedIcon(rec));
            setText(ListRenderer.getRenderedText(rec));
          }
          else {
            setText(java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("User_(USER-ID)"), new Object[] {traceRecord.ownerUserId}));
            setIcon(Images.get(ImageNums.PERSON_SMALL));
          }
        }
        setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
      }
    }

    // Privilege OR History
    else if (value instanceof Boolean) {
      setText(null);
      Boolean b = (Boolean) value;

      int rawColumn = ((RecordTableModel) ((JSortedTable) table).getRawModel()).getColumnHeaderData().convertColumnToRawModel(table.convertColumnIndexToModel(column));

      // Privilege
      if (rawColumn == 1) {
        if (b.booleanValue()) {
          setIcon(Images.get(ImageNums.TRACE_PRIVILEGE13));
          setToolTipText(com.CH_gui.lang.Lang.rb.getString("rowTip_This_user_has_a_direct_or_indirect_read_privilege_to_the_object."));
        }
        else {
          setIcon(Images.get(ImageNums.TRANSPARENT16));
          setToolTipText(com.CH_gui.lang.Lang.rb.getString("rowTip_This_user_does_not_possess_a_direct_or_indirect_read_privilege_to_the_object."));
        }
      }
      // History
      else if (rawColumn == 2) {
        if (b.booleanValue()) {
          setIcon(Images.get(ImageNums.TRACE_HISTORY13));
          setToolTipText(com.CH_gui.lang.Lang.rb.getString("rowTip_This_user_has_an_access_history_for_the_object."));
        }
        else {
          setIcon(Images.get(ImageNums.TRANSPARENT16));
          setToolTipText(com.CH_gui.lang.Lang.rb.getString("rowTip_This_user_does_not_possess_an_access_history_for_the_object."));
        }
      }
    }

    // detailed timestamp for trace tables
    else if (value instanceof Timestamp) {
      setText(Misc.getFormattedTimestamp((Timestamp) value));
    }

    return this;
  }


}
/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_gui.groupTable;

import com.CH_cl.service.cache.CacheUsrUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_co.service.records.FolderRecord;
import com.CH_co.service.records.FolderShareRecord;
import com.CH_co.service.records.Record;
import com.CH_co.service.records.UserRecord;
import com.CH_co.util.ImageNums;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.sortedTable.JSortedTable;
import com.CH_gui.table.RecordTableCellRenderer;
import com.CH_gui.util.Images;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * <b>Copyright</b> &copy; 2001-2012
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
 * <b>$Revision: 1.2 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class GroupTableCellRenderer extends RecordTableCellRenderer {

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    int rawColumn = getRawColumn(table, column);

    if (rawColumn == 0) {
      if (value instanceof String) {
        // Find the share record
        JSortedTable sTable = (JSortedTable) table;
        TableModel tableModel = sTable.getRawModel();
        if (tableModel instanceof GroupTableModel) {
          GroupTableModel tm = (GroupTableModel) tableModel;
          FolderShareRecord shareRecord = (FolderShareRecord) tm.getRowObject(sTable.convertMyRowIndexToModel(row));

          UserRecord uRec = null;
          FolderRecord gRec = null;
          if (shareRecord.isOwnedByUser()) {
            uRec = FetchedDataCache.getSingleInstance().getUserRecord(shareRecord.ownerUserId);
          } else if (shareRecord.isOwnedByGroup()) {
            gRec = FetchedDataCache.getSingleInstance().getFolderRecord(shareRecord.ownerUserId);
          }

          if (uRec != null) {
            Record rec = CacheUsrUtils.convertUserIdToFamiliarUser(uRec.userId, true, false);
            setIcon(ListRenderer.getRenderedIcon(rec));
            setText(ListRenderer.getRenderedText(rec));
          } else if (gRec != null) {
            setIcon(ListRenderer.getRenderedIcon(gRec, true));
            setText(ListRenderer.getRenderedText(gRec));
          } else if (shareRecord.isOwnedByGroup()) {
            setText(java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("Group_(GROUP-ID)"), new Object[] {shareRecord.ownerUserId}));
            setIcon(Images.get(ImageNums.PEOPLE_SECURE16));
          } else {
            setText(java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("User_(USER-ID)"), new Object[] {shareRecord.ownerUserId}));
            setIcon(Images.get(ImageNums.PERSON16));
          }

          setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
        }
      }
    }

    return this;
  }

}
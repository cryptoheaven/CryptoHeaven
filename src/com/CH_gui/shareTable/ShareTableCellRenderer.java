/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.shareTable;

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
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.17 $</b>
*
* @author  Marcin Kurzawa
*/
public class ShareTableCellRenderer extends RecordTableCellRenderer {

  private final FetchedDataCache cache = FetchedDataCache.getSingleInstance();

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    int rawColumn = getRawColumn(table, column);

    if (rawColumn == 0) {
      if (value instanceof String) {
        // Find the share record
        JSortedTable sTable = (JSortedTable) table;
        TableModel tableModel = sTable.getRawModel();
        if (tableModel instanceof ShareTableModel) {
          ShareTableModel tm = (ShareTableModel) tableModel;
          FolderShareRecord shareRecord = (FolderShareRecord) tm.getRowObject(sTable.convertMyRowIndexToModel(row));

          UserRecord uRec = null;
          FolderRecord gRec = null;
          if (shareRecord.isOwnedByUser()) {
            uRec = cache.getUserRecord(shareRecord.ownerUserId);
          } else if (shareRecord.isOwnedByGroup()) {
            gRec = cache.getFolderRecord(shareRecord.ownerUserId);
          }

          if (uRec != null) {
            // use my contact list only, not the reciprocal contacts
            Record rec = CacheUsrUtils.convertUserIdToFamiliarUser(cache, uRec.userId, true, false);
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
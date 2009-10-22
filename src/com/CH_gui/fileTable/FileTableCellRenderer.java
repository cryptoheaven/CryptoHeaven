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

package com.CH_gui.fileTable;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import java.awt.Component;
import java.sql.Timestamp;

import com.CH_gui.sortedTable.JSortedTable;
import com.CH_gui.table.*;

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
 * This class renderers cells of a table, where files' information is displayed
 *
 * Class Details:
 * 
 *
 * <b>$Revision: 1.21 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */

public class FileTableCellRenderer extends RecordTableCellRenderer {


  private static Color fileAltColor = new Color(245, 243, 233, ALPHA);
  private static Color fileAltColorSelected = new Color(202, 200, 192, ALPHA);
  private static Color[] altBkColors = new Color[] { fileAltColor, fileAltColorSelected };


  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    int rawColumn = getRawColumn(table, column);

    // set an appropriate icon beside a file name 
    // Column "File Name"
    if (rawColumn == 1) {
      if (value != null) {
        setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
        setIcon(null);

        Icon icon = null;
        // check the type of the file/folder
        if (table instanceof JSortedTable) {
          JSortedTable sTable = (JSortedTable) table;
          TableModel rawModel = sTable.getRawModel();
          if (rawModel instanceof FileTableModel) {
            FileTableModel tableModel = (FileTableModel) rawModel;
            Record rec = tableModel.getRowObject(sTable.convertMyRowIndexToModel(row));
            if (rec != null) icon = rec.getIcon();
          }
        }
        if (icon == null) {
          int typeColumn = JSortedTable.getColumnIndex(table.getModel(), com.CH_gui.lang.Lang.rb.getString("column_Type"));
          String fileType = (String) table.getModel().getValueAt(row, typeColumn);
          icon = MiscGui.getFileInternalIconForType(fileType);
        }

        setIcon(icon);

        // Fix up the height of the row in case the file name needs more space.
        int desiredHeight = Math.max(getPreferredSize().height, table.getRowHeight());
        if (table.getRowHeight(row) != desiredHeight) {
          table.setRowHeight(row, desiredHeight);
        }
      }
    } // end if rawColumn == 1

    else {
      setIcon(null);

      // set an file icon beside folder name 
      // this is for weird scenario where we have selected multiple folders and folder names appear in first column
      if (value instanceof String && table.getColumnName(column).equals(com.CH_gui.lang.Lang.rb.getString("column_Folder_Name"))) {
        setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
        setIcon(Images.get(ImageNums.FLD_CLOSED16));
      }

      // set the FLAG
      else if (rawColumn == 0) {
        if (value != null) {
          setBorder(RecordTableCellRenderer.BORDER_ICON);
          if (table instanceof JSortedTable) {
            JSortedTable sTable = (JSortedTable) table;
            TableModel rawModel = sTable.getRawModel();
            if (rawModel instanceof FileTableModel) {
              FileTableModel tableModel = (FileTableModel) rawModel;
              Record rec = tableModel.getRowObject(sTable.convertMyRowIndexToModel(row));
              if (rec instanceof FileLinkRecord) {
                FileLinkRecord fileLink = (FileLinkRecord) rec;

                StatRecord statRecord = FetchedDataCache.getSingleInstance().getStatRecord(fileLink.fileLinkId, FetchedDataCache.STAT_TYPE_FILE);
                if (statRecord != null) {
                  setIcon(statRecord.getIconForFlag((Short) value));
                  setText("");
                  setToolTipText(statRecord.getInfo());
                }
              }
            }
          }
        }
      } // end if rawColumn == 0

      // Size
      // set the size string adding words: bytes, KB, MB or GB 
      else if (rawColumn == 3) {
        if (value != null) {
          setBorder(RecordTableCellRenderer.BORDER_TEXT);
          String sizeString = Misc.getFormattedSize((Long) value, 3, 2);
          setText(sizeString);
        }
      } // end if rawColumn == 3

    }

    // if we have too little space, set tool tip
    setDefaultToolTip(this, table, row, column);

    return this;
  }


  /**
   * Provide alternate row background colors.
   */
  public Color[] getAltBkColors() {
    return altBkColors;
  }

}
/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.recycleTable;

import com.CH_cl.service.cache.CacheEmlUtils;
import com.CH_cl.service.cache.CacheUsrUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_co.service.records.FileLinkRecord;
import com.CH_co.service.records.MsgLinkRecord;
import com.CH_co.service.records.Record;
import com.CH_co.service.records.StatRecord;
import com.CH_co.util.ImageNums;
import com.CH_co.util.Misc;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.sortedTable.JSortedTable;
import com.CH_gui.table.RecordTableCellRenderer;
import com.CH_gui.util.Images;
import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/** 
* Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
*
* Class Description:
* This class renderers cells of a table, where files' information is displayed
*
* <b>$Revision: 1.1 $</b>
*
* @author  Marcin Kurzawa
*/

public class RecycleTableCellRenderer extends RecordTableCellRenderer {


  private static final Color fileAltColor = new Color(245, 243, 233, ALPHA);
  private static final Color fileAltColorSelected = new Color(202, 200, 192, ALPHA);
  private static Color[] altBkColors = new Color[] { fileAltColor, fileAltColorSelected };

  private Color defaultForeground = null;

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    // incomplete downloads should be painted in Gray, otherwise Black
    boolean isIncompleteOrAborted = false;
    if (value != null) {
      if (table instanceof JSortedTable) {
        JSortedTable sTable = (JSortedTable) table;
        TableModel rawModel = sTable.getRawModel();
        if (rawModel instanceof RecycleTableModel) {
          RecycleTableModel tableModel = (RecycleTableModel) rawModel;
          Record rec = tableModel.getRowObject(sTable.convertMyRowIndexToModel(row));
          if (rec instanceof FileLinkRecord) {
            FileLinkRecord link = (FileLinkRecord) rec;
            isIncompleteOrAborted = link.isIncomplete() || link.isAborted();
          }
        }
      }
    }

    if (defaultForeground == null)
      defaultForeground = getForeground();
    setForeground(isIncompleteOrAborted ? Color.GRAY : defaultForeground);

    int rawColumn = getRawColumn(table, column);

    // set an appropriate icon beside a name
    // Column "Name"
    if (rawColumn == 1) {
      if (value != null) {
        setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
        setIcon(null);

        Icon icon = null;
        // check the type of the file/folder
        if (table instanceof JSortedTable) {
          JSortedTable sTable = (JSortedTable) table;
          TableModel rawModel = sTable.getRawModel();
          if (rawModel instanceof RecycleTableModel) {
            RecycleTableModel tableModel = (RecycleTableModel) rawModel;
            Record rec = tableModel.getRowObject(sTable.convertMyRowIndexToModel(row));
            // handle File, Msg, Address icons, MsgLinkRecord alone would not give us a proper AddressRecord icon
            if (rec != null) {
              icon = ListRenderer.getRenderedIcon(rec);
            }
          }
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
      if (value instanceof String && table.getColumnName(column).equals(com.CH_cl.lang.Lang.rb.getString("column_Folder_Name"))) {
        setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
        setIcon(Images.get(ImageNums.FLD_FILE16));
      }

      // set the FLAG
      else if (rawColumn == 0) {
        if (value != null) {
          setText("");
          setBorder(RecordTableCellRenderer.BORDER_ICON);
          if (table instanceof JSortedTable) {
            JSortedTable sTable = (JSortedTable) table;
            TableModel rawModel = sTable.getRawModel();
            if (rawModel instanceof RecycleTableModel) {
              RecycleTableModel tableModel = (RecycleTableModel) rawModel;
              Record rec = tableModel.getRowObject(sTable.convertMyRowIndexToModel(row));
              boolean isStarred = false;
              int flagIcon = ImageNums.IMAGE_NONE;
              StatRecord statRecord = null;
              if (rec instanceof FileLinkRecord) {
                FileLinkRecord link = (FileLinkRecord) rec;
                isStarred = link.isStarred();
                statRecord = FetchedDataCache.getSingleInstance().getStatRecordMyLinkId(link.getId(), FetchedDataCache.STAT_TYPE_INDEX_FILE);
              } else if (rec instanceof MsgLinkRecord) {
                MsgLinkRecord link = (MsgLinkRecord) rec;
                isStarred = link.isStarred();
                statRecord = FetchedDataCache.getSingleInstance().getStatRecordMyLinkId(link.getId(), FetchedDataCache.STAT_TYPE_INDEX_MESSAGE);
              }
              if (statRecord != null)
                flagIcon = StatRecord.getIconForFlag(statRecord.getFlag());
              if (isStarred && flagIcon != ImageNums.IMAGE_NONE) {
                setIcon(Images.get(ImageNums.STAR_BRIGHTER));
                setToolTipText("Starred and Flagged");
              } else if (isStarred) {
                setIcon(Images.get(ImageNums.STAR_BRIGHT));
                setToolTipText("Starred");
              } else if (flagIcon != ImageNums.IMAGE_NONE) {
                setIcon(Images.get(flagIcon));
                setToolTipText(StatRecord.getInfo(statRecord.getFlag()));
              } else {
                setIcon(Images.get(ImageNums.STAR_WIRE));
                setToolTipText(null);
              }
            }
          }
        }
      } // end if rawColumn == 0

      // From
      else if (rawColumn == 2) {
        // From (internal)
        if (value instanceof Long) {
          setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
          setHorizontalAlignment(LEFT);
          // The From field is the contact name or user's short info, whichever is available
          Long userId = (Long) value;
          // use my contact list only, not the reciprocal contacts
          Record rec = CacheUsrUtils.convertUserIdToFamiliarUser(userId, true, false);
          if (rec != null) {
            setIcon(ListRenderer.getRenderedIcon(rec));
            setText(ListRenderer.getRenderedText(rec));
          }
          else {
            setText(java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("User_(USER-ID)"), new Object[] {userId}));
            setIcon(Images.get(ImageNums.PERSON16));
          }
        }
        // From (email)
        else if (value instanceof String) {
          setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
          setHorizontalAlignment(LEFT);
          Record sender = CacheEmlUtils.convertToFamiliarEmailRecord((String) value);
          setIcon(ListRenderer.getRenderedIcon(sender));
          setText(ListRenderer.getRenderedText(sender));
        }
        setDefaultBackground(this, row, isSelected);
      } // end if rawColumn == 2

      // Size
      // set the size string adding words: bytes, KB, MB or GB
      else if (rawColumn == 4) {
        if (value != null) {
          setBorder(RecordTableCellRenderer.BORDER_TEXT);
          String sizeString = Misc.getFormattedSize(((Number) value).longValue(), 3, 2);
          setText(sizeString);
        }
      } // end if rawColumn == 4

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
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

package com.CH_gui.postTable;

import java.awt.*;
import java.security.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.cryptx.*;
import com.CH_co.service.records.*;
import com.CH_co.util.*;

import com.CH_gui.gui.URLLauncherCHACTION;
import com.CH_gui.gui.URLLauncherMAILTO;
import com.CH_gui.msgs.*;
import com.CH_gui.msgTable.*;
import com.CH_gui.sortedTable.*;
import com.CH_gui.table.*;

/**
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class PostTableCellRenderer extends MsgTableCellRenderer {

  private HTML_ClickablePane jTextAreaRenderer = null;

  //private static Color postingAltColor = new Color(230, 242, 255);
  //private static Color postingAltColorSelected = new Color(184, 194, 204);
  //private static Color[] altBkColors = new Color[] { postingAltColor, postingAltColorSelected };

  // User is the key, value is the Color object
  private static final HashMap altBkUserAssignedColors;
  private static final ArrayList altBkUserColors;
  private static Color altMyBkColor;
  private static MessageDigest sha256;
  static {
    altBkUserColors = new ArrayList();
    altBkUserColors.add(new Color(230, 242, 255)); // old chat color
    altBkUserColors.add(new Color(236, 251, 232));
    altBkUserColors.add(new Color(253, 242, 230));
    altBkUserColors.add(new Color(245, 243, 233));

    altBkUserAssignedColors = new HashMap();
    altMyBkColor = new Color(247, 247, 247);
    sha256 = new SHA256();

    // for clickable links and actions, register the handler...
    HTML_ClickablePane.setRegisteredGlobalLauncher(HTML_ClickablePane.PROTOCOL_MAIL, new URLLauncherMAILTO());
  };

  public PostTableCellRenderer() {
    super();
  }

  private JTextComponent getHTMLRenderer(Component rendererContainer) {
    if (jTextAreaRenderer == null) {
      jTextAreaRenderer = makeHTMLRenderer(null);
    }
    jTextAreaRenderer.setRendererContainer(rendererContainer);
    return jTextAreaRenderer;
  }

  private HTML_ClickablePane makeHTMLRenderer(Component rendererContainer) {
    // our own image icon view to prevent dancing icons
    HTML_ClickablePane htmlPane = new HTML_ClickablePane("", new HTML_EditorKit());
    htmlPane.setRegisteredLocalLauncher(new URLLauncherCHACTION(), URLLauncherCHACTION.ACTION_PATH);
    htmlPane.setRendererContainer(rendererContainer);
    return htmlPane;
  }

  private static Color makeBkColor(Object sourceContents) {
    byte[] digest = sha256.digest(("" + sourceContents).getBytes());
    int[] colors = new int[3];
    colors[0] = (32 + 16 + 8 + 4 + 2 + 1) & digest[0];
    colors[1] = (32 + 16 + 8 + 4 + 2 + 1) & digest[1];
    colors[2] = (32 + 16 + 8 + 4 + 2 + 1) & digest[2];
    while (true) {
      int diffToGoal = 720 - (colors[0] + colors[1] + colors[2]);
      int add = diffToGoal/3;
      int newSum = 0;
      for (int i=0; i<3; i++) {
        if (colors[i] + add > 255)
          colors[i] = 255;
        else
          colors[i] += add;
        newSum += colors[i];
      }
      if (newSum > 710)
        break;
    }
    return new Color(colors[0], colors[1], colors[2]);
  }

  private static Color getUserColor(Object colorKey) {
    Color color = null;
    synchronized (altBkUserAssignedColors) {
      color = (Color) altBkUserAssignedColors.get(colorKey);
      if (color == null) {
        if (altBkUserAssignedColors.size() == altBkUserColors.size()) {
          color = makeBkColor(colorKey);
          altBkUserColors.add(color);
        } else {
          color = (Color) altBkUserColors.get(altBkUserAssignedColors.size());
        }
        altBkUserAssignedColors.put(colorKey, color);
      }
    }
    return color;
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    return getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column, false);
  }
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean forPrint) {
    Object v = value instanceof StringBuffer ? "" : value;
    Component renderer = super.getTableCellRendererComponent(table, v, isSelected, hasFocus, row, column);

    // Determine color of the message, white or none-white?
    {
      Color bkColor = null;
      if (isSelected && false) {
        bkColor = getDefaultBackground(row, isSelected);
      } else {
        if (table instanceof JSortedTable) {
          JSortedTable sTable = (JSortedTable) table;
          TableModel rawModel = sTable.getRawModel();
          if (rawModel instanceof MsgTableModel) {
            MsgTableModel tableModel = (MsgTableModel) rawModel;
            Record rec = tableModel.getRowObject(sTable.convertMyRowIndexToModel(row));
            if (rec instanceof MsgLinkRecord) {
              MsgLinkRecord msgLink = (MsgLinkRecord) rec;
              FetchedDataCache cache = FetchedDataCache.getSingleInstance();
              MsgDataRecord msgData = cache.getMsgDataRecord(msgLink.msgId);
              if (msgData != null) {
                if (cache.getMyUserId().equals(msgData.senderUserId)) {
                  bkColor = altMyBkColor;
                } else {
                  if (msgData.isEmail()) {
                    bkColor = getUserColor((""+msgData.getFromEmailAddress()).toLowerCase());
                  } else {
                    bkColor = getUserColor(""+msgData.senderUserId);
                  }
                }
              }
            }
          }
        }
      }
      if (bkColor == null) {
        bkColor = defaultWhite;
      }
      if (isSelected) {
        bkColor = bkColor.darker();
      }
      renderer.setBackground(bkColor);
      setDefaultForeground(renderer, row, isSelected);
    }
    // end of background color management

    int rawColumn = getRawColumn(table, column);

    // subject + body
    if (rawColumn == 5) {
      StringBuffer sb = null;

      // Get the right indent level.
      int indentLevel = 0;
      JSortedTable sTable = (JSortedTable) table;
      TableModel tableModel = sTable.getRawModel();

      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      MsgLinkRecord mLink = null;
      MsgDataRecord mData = null;
      MsgLinkRecord pLink = null;

      // Since multiple views may display the same message links, we must choose how to view them in the renderer.
      if (tableModel instanceof MsgTableModel) {
        MsgTableModel mtm = (MsgTableModel) tableModel;
        mLink = (MsgLinkRecord) mtm.getRowObject(sTable.convertMyRowIndexToModel(row));
        mData = cache.getMsgDataRecord(mLink.msgId);
        pLink = row > 0 ? (MsgLinkRecord) mtm.getRowObject(sTable.convertMyRowIndexToModel(row-1)) : null;
        sb = (StringBuffer) mtm.getSubjectColumnValue(mtm, mLink, mData, pLink, cache);
        if (((MsgTableSorter) sTable.getModel()).isThreaded()) {
          indentLevel = mLink.getSortThreadLayer();
        }
      }

      TableColumnModel columnModel = table.getColumnModel();
      int colWidth = columnModel.getColumn(column).getWidth();
      int colMargin = columnModel.getColumnMargin();
      int usableColumnWidth = colWidth - colMargin - (indentLevel * RecordTableCellRenderer.BORDER_INDENT_PIXELS);
      // in case indent is too deep, make this at least 1 pixel wide
      usableColumnWidth = Math.max(usableColumnWidth, 1);

      // HTML_ClickablePane somehow fixes the sizing adjustement bug
      // Don't recycle renderers that deal with IMAGE views, our HTML_ImageView doesn't get cleaned-up properly.
      JTextComponent editor = sb != null && (sb.indexOf("img src=") >= 0 || sb.indexOf("IMG SRC=") >= 0) ? makeHTMLRenderer(table) : getHTMLRenderer(table);

      // Convert indent level to border -- leave indent to the outer pannel which will include arrow too
      editor.setBorder(RecordTableCellRenderer.getIndentedBorder(0, false));

      // Set contents of the message area
      MsgPanelUtils.setMessageContent(sb != null ? sb.toString() : "", true, editor, true, true);
      //xx-xx editor.setText(value.toString());

//      editor.setPreferredWidthLimit(usableColumnWidth);

      editor.setMinimumSize(new Dimension(usableColumnWidth, table.getRowHeight()));
      editor.setSize(usableColumnWidth, 16);
      int desiredHeight = Math.max(editor.getPreferredSize().height, table.getRowHeight());
      // don't lower the estimate from the superclass, only bump it up
      desiredHeight = Math.max(desiredHeight, renderer.getPreferredSize().height);

      editor.setForeground(renderer.getForeground());
      editor.setBackground(renderer.getBackground());

      renderer = editor;

      // indent replies
      if (indentLevel > 0) {
        renderer = makeIndentedAreaRenderer(indentLevel, renderer, false, true);
      }

      if (table.getRowHeight(row) != desiredHeight) {
        table.setRowHeight(row, desiredHeight);
      }
    } // end if String Buffer
    // From
    else if (rawColumn == 3 && !forPrint) {
      if (row > 0) {
        JSortedTable sTable = (JSortedTable) table;
        TableModel tableModel = sTable.getRawModel();
        if (tableModel instanceof MsgTableModel) {
          MsgTableModel mtm = (MsgTableModel) tableModel;
          MsgLinkRecord mLink = (MsgLinkRecord) mtm.getRowObject(sTable.convertMyRowIndexToModel(row));
          MsgLinkRecord pLink = row > 0 ? (MsgLinkRecord) mtm.getRowObject(sTable.convertMyRowIndexToModel(row-1)) : null;
          if (pLink != null) {
            Object obj = mtm.getValueAtRawColumn(mLink, rawColumn, false);
            if (obj != null) {
              Object pObj = mtm.getValueAtRawColumn(pLink, rawColumn, false);
              if (obj.equals(pObj)) {
                ((MsgTableCellRenderer) renderer).setText(null);
                ((MsgTableCellRenderer) renderer).setIcon(null);
              }
            }
          }
        }
      }
    }

    return renderer;
  }


  /**
   * Provide alternate row background colors.
   */
  /*
  public Color[] getAltBkColors() {
    return altBkColors;
  }
   */


  /**
   * @return true to overwrite background color management.
   */
  public boolean isSubClassManagingRowColors() {
    return true;
  }

}
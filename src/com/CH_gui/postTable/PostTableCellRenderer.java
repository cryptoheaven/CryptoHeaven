/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.postTable;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.util.UserColor;
import com.CH_co.service.records.MsgDataRecord;
import com.CH_co.service.records.MsgLinkRecord;
import com.CH_co.service.records.Record;
import com.CH_gui.gui.URLLauncherCHACTION;
import com.CH_gui.gui.URLLauncherMAILTO;
import com.CH_gui.msgTable.MsgTableCellRenderer;
import com.CH_gui.msgTable.MsgTableModel;
import com.CH_gui.msgTable.MsgTableSorter;
import com.CH_gui.msgs.MsgPanelUtils;
import com.CH_gui.sortedTable.JSortedTable;
import com.CH_gui.table.RecordTableCellRenderer;
import com.CH_gui.util.HTML_ClickablePane;
import com.CH_gui.util.HTML_EditorKit;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.HashMap;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class PostTableCellRenderer extends MsgTableCellRenderer {

  private HTML_ClickablePane jTextAreaRenderer = null;

  // POST and CHAT have a little taller borders
  private static final int BORDER_TOP = 5;
  private static final int BORDER_BOTTOM = 5;
  private static final Border BORDER_ICON = new EmptyBorder(BORDER_TOP,1,BORDER_BOTTOM,1);
  private static final Border BORDER_ICONIZED = new EmptyBorder(BORDER_TOP,1,BORDER_BOTTOM,5);
  private static final Border BORDER_ICONIZED_FIRST = new EmptyBorder(BORDER_TOP,5,BORDER_BOTTOM,5);
  private static final Border BORDER_TEXT = new EmptyBorder(BORDER_TOP,5,BORDER_BOTTOM,5);

  // Background color for 'me' user
  private static Color altMyBkColor;

  // Cache for user color objects
  private static HashMap colorCacheBk = new HashMap();
  private static HashMap colorCacheFg = new HashMap();

  private static Color colorFgDefault = null;
  private static Font fontBold = null;
  private static Font fontPlain = null;

  static {
    altMyBkColor = new Color(247, 247, 247);
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
    HTML_ClickablePane htmlPane = new HTML_ClickablePane("", new HTML_EditorKit(true));
    htmlPane.setRegisteredLocalLauncher(new URLLauncherCHACTION(), URLLauncherCHACTION.ACTION_PATH);
    htmlPane.setRendererContainer(rendererContainer);
    return htmlPane;
  }

  private static Color getUserBkColor(Object colorKey) {
    Color color = (Color) colorCacheBk.get(colorKey);
    if (color == null) {
      int userColor = 0;
      if (colorKey instanceof Long) {
        userColor = UserColor.getUserColor((Long) colorKey);
      } else {
        userColor = UserColor.getUserColor(colorKey.toString());
      }
      double[] rgb = new double[] { UserColor.getRed(userColor), UserColor.getGreen(userColor), UserColor.getBlue(userColor) };
      lighter_rgb(rgb, 0.90);
      color = new Color((int) rgb[0], (int) rgb[1], (int) rgb[2]);
      colorCacheBk.put(colorKey, color);
    }
    return color;
  }

  private static Color getUserFgColor(Object colorKey) {
    Color color = (Color) colorCacheFg.get(colorKey);
    if (color == null) {
      int userColor = 0;
      if (colorKey instanceof Long) {
        userColor = UserColor.getUserColor((Long) colorKey);
      } else {
        userColor = UserColor.getUserColor(colorKey.toString());
      }
      // Make it 10% darker because we'll put it against color backgrount... 
      // We want 90% of the current color amount
      double scale = 0.90;
      color = new Color((int) (UserColor.getRed(userColor)*scale), (int) (UserColor.getGreen(userColor)*scale), (int) (UserColor.getBlue(userColor)*scale));
      colorCacheFg.put(colorKey, color);
    }
    return color;
  }

  /**
   * Make lighter shade of color;
   * @param rgb values 0..255 for Red, Green, Blue
   * @param factor Amount of adjustment; 0.5 half way from current to white
   */
  private static void lighter_rgb(double[] rgb, double factor) {
    double total = rgb[0]+rgb[1]+rgb[2];
    double adjust = ((255.0 * 3 - total) * factor) / 3;
    rgb[0] = rgb[0]+adjust;
    rgb[1] = rgb[1]+adjust;
    rgb[2] = rgb[2]+adjust;
    redistribute_rgb(rgb);
  }
  private static void redistribute_rgb(double[] rgb) {
    double r = rgb[0];
    double g = rgb[1];
    double b = rgb[2];
    double threshold = 255.0;
    double m = Math.max(Math.max(r, g), b);
    if (m <= threshold) {
      return;
    }
    double total = r + g + b;
    if (total >= 3 * threshold) {
      rgb[0] = threshold; rgb[1] = threshold; rgb[2] = threshold;
      return;
    }
    double x = (3 * threshold - total) / (3 * m - total);
    double gray = threshold - x * m;
    rgb[0] = gray + x * r;
    rgb[1] = gray + x * g;
    rgb[2] = gray + x * b;
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    return getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column, false);
  }
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean forPrint) {
    Object v = value instanceof StringBuffer ? "" : value;
    Component renderer = super.getTableCellRendererComponent(table, v, isSelected, hasFocus, row, column);

    // init static values
    if (colorFgDefault == null) {
      colorFgDefault = renderer.getForeground();
      fontPlain = renderer.getFont();
      fontBold = fontPlain.deriveFont(Font.BOLD);
    }

    Border border = getBorder();
    if (border == RecordTableCellRenderer.BORDER_TEXT) {
      setBorder(PostTableCellRenderer.BORDER_TEXT);
    } else if (border == RecordTableCellRenderer.BORDER_ICON) {
      setBorder(PostTableCellRenderer.BORDER_ICON);
    } else if (border == RecordTableCellRenderer.BORDER_ICONIZED) {
      setBorder(PostTableCellRenderer.BORDER_ICONIZED);
    } else if (border == RecordTableCellRenderer.BORDER_ICONIZED_FIRST) {
      setBorder(PostTableCellRenderer.BORDER_ICONIZED_FIRST);
    }

    int rawColumn = getRawColumn(table, column);

    // Determine color of the message, white or none-white?
    // Determine color of FROM name
    {
      Color bkColor = null;
      Color fgColor = null;
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
                if (msgData.senderUserId.equals(cache.getMyUserId())) {
                  bkColor = altMyBkColor;
                } else {
                  Object colorKey;
                  if (msgData.isEmail()) {
                    colorKey = (""+msgData.getFromEmailAddress()).toLowerCase();
                  } else {
                    colorKey = msgData.senderUserId;
                  }
                  bkColor = getUserBkColor(colorKey);
                  // From column
                  if (rawColumn == 3 && !forPrint) {
                    fgColor = getUserFgColor(colorKey);
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

      // Adjust the FROM color and BOLDNESS
      if (fgColor != null) {
        renderer.setForeground(fgColor);
        renderer.setFont(fontBold);
      } else {
        renderer.setForeground(colorFgDefault);
        renderer.setFont(fontPlain);
      }
    }
    // end of background color management

    // subject + body
    if (rawColumn == 5) {
      StringBuffer sb = null;

      // Get the right indent level.
      int indentLevel = 0;
      JSortedTable sTable = (JSortedTable) table;
      TableModel tableModel = sTable.getRawModel();

      // Since multiple views may display the same message links, we must choose how to view them in the renderer.
      if (tableModel instanceof MsgTableModel) {
        MsgTableModel mtm = (MsgTableModel) tableModel;
        MsgLinkRecord mLink = (MsgLinkRecord) mtm.getRowObject(sTable.convertMyRowIndexToModel(row));
        if (mLink != null) {
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          MsgDataRecord mData = cache.getMsgDataRecord(mLink.msgId);
          if (mData != null) {
            MsgLinkRecord pLink = row > 0 ? (MsgLinkRecord) mtm.getRowObject(sTable.convertMyRowIndexToModel(row-1)) : null;
            MsgLinkRecord nLink = row < mtm.getRowCount() ? (MsgLinkRecord) mtm.getRowObject(sTable.convertMyRowIndexToModel(row+1)) : null;
            Object subjectValue = mtm.getSubjectColumnValue(mtm, mLink, mData, pLink, nLink, cache, false);
            if (subjectValue != null) {
              if (subjectValue instanceof StringBuffer)
                sb = (StringBuffer) subjectValue;
              else
                sb = new StringBuffer(subjectValue.toString());
            }
            if (((MsgTableSorter) sTable.getModel()).isThreaded()) {
              indentLevel = mLink.getSortThreadLayer();
            }
          }
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
      MsgPanelUtils.setMessageContent(sb != null ? sb.toString() : "", true, editor, true);
      //xx-xx editor.setText(value.toString());

//      editor.setPreferredWidthLimit(usableColumnWidth);

      editor.setMinimumSize(new Dimension(usableColumnWidth, table.getRowHeight()));
      editor.setSize(usableColumnWidth, 21);
      int desiredHeight = Math.max(editor.getPreferredSize().height+2, table.getRowHeight());
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
      JSortedTable sTable = (JSortedTable) table;
      TableModel tableModel = sTable.getRawModel();
      if (tableModel instanceof MsgTableModel) {
        MsgTableModel mtm = (MsgTableModel) tableModel;
        MsgLinkRecord mLink = (MsgLinkRecord) mtm.getRowObject(sTable.convertMyRowIndexToModel(row));
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        MsgDataRecord mData = mLink != null ? cache.getMsgDataRecord(mLink.msgId) : null;
        boolean isFromMe = mData != null && mData.senderUserId.equals(cache.getMyUserId());
        if (isFromMe)
          ((MsgTableCellRenderer) renderer).setText("me");
        // check previous row to see if the same 'from'
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
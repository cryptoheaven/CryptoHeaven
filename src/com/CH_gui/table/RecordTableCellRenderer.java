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

package com.CH_gui.table;

import com.CH_co.gui.*;
import com.CH_co.service.records.*;
import com.CH_co.util.*;

import com.CH_gui.gui.*;
import com.CH_gui.list.*;
import com.CH_gui.sortedTable.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.sql.Timestamp;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableModel;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.21 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
abstract public class RecordTableCellRenderer extends MyDefaultTableCellRenderer {

  private JPanel jIndentAreaRenderer = new JPanel();
  private JMyLabel jIndentLabelRenderer = new JMyLabel();
  protected StringHighlighterI stringHighlighter;

  public static final int ALPHA = 150;
  public static Color defaultWhite = new Color(255, 255, 255, ALPHA);

  public static final int ALT_BK_DEFAULT_COLOR_I = 0;
  public static final int ALT_BK_SELECTED_COLOR_I = 1;

  private static Color defaultAltColor = new Color(245, 243, 233, ALPHA);
  private static Color defaultAltColorSelected = new Color(202, 200, 192, ALPHA);
  private static Color[] defaultAltBkColors = new Color[] { defaultAltColor, defaultAltColorSelected };

  /**
   * Provide alternate row background colors.
   */
  public Color[] getAltBkColors() {
    return defaultAltBkColors;
  }


  public static final Border BORDER_ICON = null;
  public static final Border BORDER_ICONIZED = new EmptyBorder(0,1,0,5);
  public static final Border BORDER_ICONIZED_FIRST = new EmptyBorder(0,5,0,5);
  public static final Border BORDER_TEXT = new EmptyBorder(0,5,0,5);
  public static Border[] BORDERS_INDENTED_ICONIZED = null;
  public static Border[] BORDERS_INDENTED_TEXT = null;
  public static final int BORDER_INDENT_PIXELS = 16;


  static {
    int BORDER_INDENTED_LEN = 10;
    BORDERS_INDENTED_ICONIZED = new EmptyBorder[BORDER_INDENTED_LEN];
    for (int i=0; i<BORDERS_INDENTED_ICONIZED.length; i++) {
      BORDERS_INDENTED_ICONIZED[i] = makeIndentedBorder(i, true);
    }
    BORDERS_INDENTED_TEXT = new EmptyBorder[BORDER_INDENTED_LEN];
    for (int i=0; i<BORDERS_INDENTED_TEXT.length; i++) {
      BORDERS_INDENTED_TEXT[i] = makeIndentedBorder(i, false);
    }
  }

//  public static Color makeAlphaAdjustedColor(int r, int g, int b, int a, Color commonBack) {
//    Color inverse = new Color(255-commonBack.getRed(), 255-commonBack.getGreen(), 255-commonBack.getBlue());
//    int percent = (int) (((double) a) / 2.55);
//    return getInBetweenColor(new Color(r, g, b), inverse, percent);
//  }

  public static Border makeIndentedBorder(int numOfIndents, boolean iconized) {
    return new EmptyBorder(0, BORDER_INDENT_PIXELS*numOfIndents+(iconized ? 1 : 5), 0, 5);
  }

  public static Border getIndentedBorder(int numOfIndents, boolean iconized) {
    Border border = null;
    Border[] borders = iconized ? BORDERS_INDENTED_ICONIZED : BORDERS_INDENTED_TEXT;
    if (numOfIndents < borders.length) {
      border = borders[numOfIndents];
    } else {
      border = makeIndentedBorder(numOfIndents, iconized);
    }
    return border;
  }

  public StringHighlighterI getStringHighlighter() {
    return stringHighlighter;
  }

  public void setStringHighlighter(StringHighlighterI stringHighlighter) {
    this.stringHighlighter = stringHighlighter;
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    setHorizontalAlignment(LEADING);
    setHorizontalTextPosition(TRAILING);
    setVerticalAlignment(TOP);
    setVerticalTextPosition(CENTER);
    setToolTipText(null);
    setText(null);
    setIcon(null);
    setEnabled(true);
    setBorder(BORDER_TEXT);
    //super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
    setOpaque(true);

    // Date
    if (value instanceof Timestamp) {
      setText(Misc.getFormattedDate((Timestamp) value, false));
    }
    // Numbers
    else if (value instanceof Number) {
      setText(value.toString());
      setHorizontalAlignment(RIGHT);
    }

    if (stringHighlighter != null && stringHighlighter.hasHighlightingStr()) {
      String highlightedStr = getStringHighlight(getText());
      setText(highlightedStr);
      int desiredHeight = Math.max(getPreferredSize().height, table.getRowHeight());
      if (table.getRowHeight(row) < desiredHeight) {
        table.setRowHeight(row, desiredHeight);
      }
    }

    setDefaultToolTip(this, table, row, column);
    if (column > -1 && !isSubClassManagingRowColors()) {
      setDefaultBackground(this, row, isSelected);
      setDefaultForeground(this, row, isSelected);
    }

    // avoid painting white background
    Color bkColor = getBackground();
    setOpaque(!(bkColor.equals(Color.white) || bkColor.equals(defaultWhite)));

    return this;
  }

  public String getStringHighlight(String str) {
    if (str != null) {
      if (stringHighlighter != null && stringHighlighter.hasHighlightingStr()) {
        String[] visualsReturnBuffer = new String[1];
        int match = StringHighlighter.matchStrings(str, stringHighlighter, true, visualsReturnBuffer);
        if (visualsReturnBuffer[0] != null)
          str = visualsReturnBuffer[0];
      }
    }
    return str;
  }

  public void setDefaultToolTip(JLabel label, JTable table, int row, int column) {
    setDefaultToolTip(label.getText(), label, table, row, column);
  }
  public void setDefaultToolTip(String text, JComponent target, JTable table, int row, int column) {
    if (column > -1 && text != null && text.length() > 1) {
      // if we have too little space, set tool tip
      if (target.getPreferredSize().width > table.getCellRect(row, column, true).width) {
        target.setToolTipText(text);
      }
    }
  }

  public static int getRawColumn(JTable table, int column) {
    return ((RecordTableModel) ((JSortedTable) table).getRawModel()).getColumnHeaderData().convertColumnToRawModel(table.convertColumnIndexToModel(column));
  }


  /**
   * Classes that wish to handle their own alternative color background should overwrite this method.
   * @return false by default
   */
  public boolean isSubClassManagingRowColors() {
    return false;
  }

  /**
   * @return background color for the row if alternative color exists, null otherwise.
   */
  public Color getDefaultBackground(int row, boolean isSelected) {
    Color bgColor = null;
    Color[] altBkColors = getAltBkColors();
    if (altBkColors == null)
      altBkColors = defaultAltBkColors;
    if (isSelected) {
      if (altBkColors != null)
        bgColor = altBkColors[ALT_BK_SELECTED_COLOR_I];
    } else if ((row % 2) == 0) {
      bgColor = defaultWhite;
    } else if (altBkColors != null) {
      bgColor = altBkColors[ALT_BK_DEFAULT_COLOR_I];
    }
    return bgColor;
  } // end getDefaultBackground()

  public void setDefaultBackground(Component c, int row, boolean isSelected) {
    Color bgColor = getDefaultBackground(row, isSelected);
    if (bgColor != null) {
      c.setBackground(bgColor);
    }
  }

  public void setDefaultForeground(Component c, int row, boolean isSelected) {
    c.setForeground(Color.black);
  }

  /**
   * Usually all tables have two background colors defined.  This method gets other color than
   * one specified.  Used in alteration of background colors.
   * @return another color to the one specified but from the list of available background colors.
   */
  public Color getOtherColor(Color color) {
    Color bgColor = null;
    Color[] altBkColors = getAltBkColors();
    if (altBkColors == null)
      altBkColors = defaultAltBkColors;
    if (color == null) {
      bgColor = altBkColors[ALT_BK_DEFAULT_COLOR_I];
    } else {
      bgColor = defaultWhite;
    }
    return bgColor;
  }
  public static Color getInBetweenColor(Color c1, Color c2) {
    return getInBetweenColor(c1, c2, 50);
  }
  public static Color getInBetweenColor(Color c1, Color c2, int percentC1) {
    if (c1 == null) c1 = Color.white;
    if (c2 == null) c2 = Color.white;
    int pC1 = percentC1;
    int pC2 = 100 - pC1;
    return new Color((c1.getRed()*pC1+c2.getRed()*pC2)/100, (c1.getGreen()*pC1+c2.getGreen()*pC2)/100, (c1.getBlue()*pC1+c2.getBlue()*pC2)/100, ALPHA);
  }

  public String getColumnShortName(JTable table, int viewColumn) {
    String shortName = null;
    int modelColumn = table.convertColumnIndexToModel(viewColumn);
    if (table instanceof JSortedTable) {
      JSortedTable sTable = (JSortedTable) table;
      TableModel rawModel = sTable.getRawModel();
      if (rawModel instanceof RecordTableModel) {
        RecordTableModel rtModel = (RecordTableModel) rawModel;
        ColumnHeaderData chd = rtModel.getColumnHeaderData();
        int rawColumn = chd.convertColumnToRawModel(modelColumn);
        shortName = chd.getRawColumnShortName(rawColumn);
      }
    }
    return shortName;
  }

  public Component makeIndentedAreaRenderer(int identLevel, Component renderer, boolean includeArrow, boolean isIconized) {
    if (identLevel == 0) {
      // no-op
    } else if (includeArrow) {
      Color fw = renderer.getForeground();
      Color bk = renderer.getBackground();
      JPanel panel = jIndentAreaRenderer;
      panel.removeAll();
      panel.setForeground(fw);
      panel.setBackground(bk);
      panel.setOpaque(true);
      panel.setLayout(new BorderLayout());//new GridBagLayout());
      panel.setBorder(new EmptyBorder(0, 0, 0, 0));
      JLabel arrow = jIndentLabelRenderer;
      arrow.setIcon(Images.get(ImageNums.REPLY_ARROW16));
      arrow.setOpaque(true);
      arrow.setForeground(fw);
      arrow.setBackground(bk);
      arrow.setVerticalAlignment(SwingConstants.TOP);

      panel.add(arrow, BorderLayout.WEST);
      panel.add(renderer, BorderLayout.CENTER);
      /*
      panel.add(arrow, new GridBagConstraints(0, 0, 1, 1, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(0, 0, 0, 0), 0, 0));
      panel.add(renderer, new GridBagConstraints(1, 0, 1, 1, 10, 10, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
        */
      identLevel --;
      renderer = panel;
      panel.setBorder(RecordTableCellRenderer.getIndentedBorder(identLevel, isIconized));
      renderer = panel;
    } else {
      if (renderer instanceof JComponent) {
        Color bk = renderer.getBackground();
        Border bPart = new CompoundBorder(new MatteBorder(0, 5, 0, 0, bk), new MatteBorder(0, 2, 0, 0, Color.GRAY));
        Border bSet = bPart;
        for (int i=1; i<identLevel; i++) {
          bSet = new CompoundBorder(bSet, bPart);
        }
        ((JComponent) renderer).setBorder(new CompoundBorder(bSet, new MatteBorder(0, 7, 0, 0, bk)));//RecordTableCellRenderer.getIndentedBorder(identLevel, isIconized));
      }
    }
    return renderer;
  }

  public Record getRecord(JTable table, int row) {
    Record rec = null;
    if (table instanceof JSortedTable) {
      JSortedTable sTable = (JSortedTable) table;
      TableModel rawModel = sTable.getRawModel();
      if (rawModel instanceof RecordTableModel) {
        RecordTableModel tableModel = (RecordTableModel) rawModel;
        rec = tableModel.getRowObject(sTable.convertMyRowIndexToModel(row));
      }
    }
    return rec;
  }

  public boolean isColumnVisible(JTable table, int rawColumn) {
    boolean visible = true;
    if (table instanceof JSortedTable) {
      JSortedTable sTable = (JSortedTable) table;
      TableModel rawModel = sTable.getRawModel();
      if (rawModel instanceof RecordTableModel) {
        boolean hidden = ((RecordTableModel) rawModel).getColumnHeaderData().convertRawColumnToModel(rawColumn) == -1;
        visible = !hidden;
      }
    }
    return visible;
  }

}
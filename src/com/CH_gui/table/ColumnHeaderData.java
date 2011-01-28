/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
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

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.sortedTable.*;
import com.CH_gui.util.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/** 
 * <b>Copyright</b> &copy; 2001-2011
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
 * <b>$Revision: 1.13 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class ColumnHeaderData extends Object implements Serializable {

  // 9 by X dimension matrix where following rows store column model info
  // Note: only the last row will vary in length as not all the columns may be showing or being sorted by.
  // [0] header names (String),
  // [1] short display names (String),
  // [2] tooltips (String),
  // [3] header icon index (Integer),
  // [4] preferred size (Integer),
  // [5] preferred default long size (Integer),
  // [6] preferred default short size (Integer)
  // [7] maximum size (Integer),
  // [8] minimum size (Integer),
  // [9] viewable sequence (Integer),
  // [10] viewable default long sequence/ non-hiden column indexes (Integer),
  // [11] viewable default short sequence/ non-hiden column indexes (Integer),
  // [12] sorting by raw columns (Integer +ve=ascending, -ve=descending [100 negative offset])
  public Object[][] data = null;

  public static final int I_HEADER_NAMES = 0;
  public static final int I_SHORT_DISPLAY_NAMES = 1;
  public static final int I_TOOLTIPS = 2;
  public static final int I_HEADER_ICON_INDEX = 3;
  public static final int I_SIZE_PREF = 4;
  public static final int I_SIZE_PREF_DEFAULT_LONG = 5;
  public static final int I_SIZE_PREF_DEFAULT_SHORT = 6;
  public static final int I_SIZE_MAX = 7;
  public static final int I_SIZE_MIN = 8;
  public static final int I_VIEWABLE_SEQUENCE = 9;
  public static final int I_VIEWABLE_SEQUENCE_DEFAULT_LONG = 10;
  public static final int I_VIEWABLE_SEQUENCE_DEFAULT_SHORT = 11;
  public static final int I_SORT_SEQUENCE = 12;

  public static final int DESCENDING_OFFSET = 100;

  /** Creates new ColumnHeaderData */
  public ColumnHeaderData(Object[][] data) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ColumnHeaderData.class, "ColumnHeaderData(Object[][] data)");
    if (trace != null) trace.args(data);
    this.data = data;
    if (data.length != I_SORT_SEQUENCE+1)
      throw new IllegalArgumentException("ColumnHeaderData structure incorrect!\n" + this);
    if (trace != null) trace.exit(ColumnHeaderData.class);
  }


  public int getColumnCount() {
    return data[I_VIEWABLE_SEQUENCE].length;
  }
  public int convertColumnToRawModel(int column) {
    return ((Integer) data[I_VIEWABLE_SEQUENCE][column]).intValue();
  }
  public int convertRawColumnToModel(int rawColumn) {
    return ArrayUtils.find(data[I_VIEWABLE_SEQUENCE], new Integer(rawColumn));
  }
  public String getRawColumnName(int rawColumn) {
    if (data[I_HEADER_NAMES] != null && data[I_HEADER_NAMES].length > rawColumn)
      return (String) data[I_HEADER_NAMES][rawColumn];
    else
      return null;
  }
  public String getRawColumnShortName(int rawColumn) {
    if (data[I_SHORT_DISPLAY_NAMES] != null && data[I_SHORT_DISPLAY_NAMES].length > rawColumn)
      return (String) data[I_SHORT_DISPLAY_NAMES][rawColumn];
    else
      return null;
  }
  public String getRawColumnTooltip(int rawColumn) {
    if (data[I_TOOLTIPS] != null && data[I_TOOLTIPS].length > rawColumn)
      return (String) data[I_TOOLTIPS][rawColumn];
    else
      return null;
  }
  public ImageIcon getRawColumnIcon(int rawColumn) {
    Integer imageIconIndex = null;
    if (data[I_HEADER_ICON_INDEX] != null && data[I_HEADER_ICON_INDEX].length > rawColumn)
      imageIconIndex = (Integer) data[I_HEADER_ICON_INDEX][rawColumn];
    if (imageIconIndex != null)
      return Images.get(imageIconIndex.intValue());
    else
      return null;
  }
  public Integer getRawColumnSizePref(int rawColumn) {
    if (data[I_SIZE_PREF] != null && data[I_SIZE_PREF].length > rawColumn)
      return (Integer) data[I_SIZE_PREF][rawColumn];
    else
      return null;
  }
  public Integer getRawColumnSizeMax(int rawColumn) {
    if (data[I_SIZE_MAX] != null && data[I_SIZE_MAX].length > rawColumn)
      return (Integer) data[I_SIZE_MAX][rawColumn];
    else
      return null;
  }
  public Integer getRawColumnSizeMin(int rawColumn) {
    if (data[I_SIZE_MIN] != null && data[I_SIZE_MIN].length > rawColumn)
      return (Integer) data[I_SIZE_MIN][rawColumn];
    else
      return null;
  }
  public Integer[] getRawColumnViewableSequence() {
    return (Integer[]) data[I_VIEWABLE_SEQUENCE];
  }
  public Integer[] getRawColumnSortSequence() {
    return (Integer[]) data[I_SORT_SEQUENCE];
  }

  /**
   * Return the index of the primary sorting column, -1 if there is no sorting column.
   */
  public int getPrimarySortingColumn() {
    int column = -1;
    if (data[I_SORT_SEQUENCE] != null && data[I_SORT_SEQUENCE].length > 0) {
      column = ((Integer) data[I_SORT_SEQUENCE][0]).intValue();
      if (column < 0)
        column = -column-DESCENDING_OFFSET;
    }
    return column;
  }

  /**
   * Return the +1 or -1 for ascending or descending sorting of the primary sort column, 0 if does not exist.
   */
  public int getPrimarySortingDirection() {
    int dir = 0;
    if (data[I_SORT_SEQUENCE] != null && data[I_SORT_SEQUENCE].length > 0) {
      dir = ((Integer) data[I_SORT_SEQUENCE][0]).intValue() >= 0 ? 1 : -1;
    }
    return dir;
  }


  public String toString() {
    StringBuffer sb = new StringBuffer();

    {
      sb.append("I_SIZE_PREF ");
      int len = data[I_SIZE_PREF].length;
      sb.append(len); sb.append(' ');
      for (int i=0; i<len; i++) {
        sb.append(data[I_SIZE_PREF][i]);
        sb.append(' ');
      }
    }

    {
      sb.append("I_VIEWABLE_SEQUENCE ");
      int len = data[I_VIEWABLE_SEQUENCE].length;
      sb.append(len); sb.append(' ');
      for (int i=0; i<len; i++) {
        sb.append(data[I_VIEWABLE_SEQUENCE][i]);
        sb.append(' ');
      }
    }

    {
      sb.append("I_SORT_SEQUENCE ");
      int len = data[I_SORT_SEQUENCE].length;
      sb.append(len); sb.append(' ');
      for (int i=0; i<len; i++) {
        sb.append(data[I_SORT_SEQUENCE][i]);
        sb.append(' ');
      }
    }

    return sb.toString();
  } // end toString()


  public void initFromString(String str) {
    try {
      StringTokenizer st = new StringTokenizer(str);

      if (st.nextToken().equals("I_SIZE_PREF")) {
        int len = Integer.parseInt(st.nextToken());
        if (data[I_SIZE_PREF] == null || len > data[I_SIZE_PREF].length)
          data[I_SIZE_PREF] = new Integer[len];
        for (int i=0; i<len; i++) {
          String colLenS = st.nextToken();
          if (colLenS.equalsIgnoreCase("null")) {
            data[I_SIZE_PREF][i] = null;
          } else {
            int prefSize = Integer.parseInt(colLenS);
            int prefMax = 0;
            int prefMin = 0;
            try { prefMax = ((Integer) data[I_SIZE_MAX][i]).intValue(); } catch (Throwable tx) { }
            try { prefMin = ((Integer) data[I_SIZE_MIN][i]).intValue(); } catch (Throwable tx) { }
            if (prefMax > 0) prefSize = Math.min(prefMax, prefSize);
            if (prefMin > 0) prefSize = Math.max(prefMin, prefSize);
            data[I_SIZE_PREF][i] = new Integer(prefSize);
          }
        }
      }

      if (st.nextToken().equals("I_VIEWABLE_SEQUENCE")) {
        int len = Integer.parseInt(st.nextToken());
        data[I_VIEWABLE_SEQUENCE] = new Integer[len];
        for (int i=0; i<len; i++) {
          data[I_VIEWABLE_SEQUENCE][i] = Integer.valueOf(st.nextToken());
        }
      }

      if (st.nextToken().equals("I_SORT_SEQUENCE")) {
        int len = Integer.parseInt(st.nextToken());
        data[I_SORT_SEQUENCE] = new Integer[len];
        for (int i=0; i<len; i++) {
          data[I_SORT_SEQUENCE][i] = Integer.valueOf(st.nextToken());
        }
      }

    } catch (Throwable t) {
    }
  } // end initFromString()


  public void initFromTable(JTable table, boolean saveDefaultColumnSizes) {
    TableColumnModel tcModel = table.getColumnModel();

    boolean updateSizePrefsLong = false;
    boolean updateSizePrefsShort = false;
    if (saveDefaultColumnSizes) {
      JSplitPane sp = MiscGui.getParentSplitPane(table);
      if (sp != null) {
        updateSizePrefsLong = sp.getOrientation() == JSplitPane.VERTICAL_SPLIT;
        updateSizePrefsShort = sp.getOrientation() == JSplitPane.HORIZONTAL_SPLIT;
      }
    }

    // Construct I_SIZE_PREF and I_VIEWABLE_SEQUENCE
    int numColumns = tcModel.getColumnCount();
    Integer[] newViewableSeq = new Integer[numColumns];
    Vector sizePrefV = new Vector(Arrays.asList(data[I_SIZE_PREF]));
    for (int i=0; i<numColumns; i++) {
      TableColumn tc = tcModel.getColumn(i);
      int modelColumn = table.convertColumnIndexToModel(i);
      int rawColumn = convertColumnToRawModel(modelColumn);

      // Extend the length of the Vector to accomodate the size.
      for (int j=sizePrefV.size(); j<=rawColumn; j++)
        sizePrefV.addElement(null);

      sizePrefV.setElementAt(new Integer(tc.getWidth()), rawColumn);
      newViewableSeq[i] = new Integer(rawColumn);
    }
    if (data[I_SIZE_PREF] == null || data[I_SIZE_PREF].length < sizePrefV.size())
      data[I_SIZE_PREF] = new Integer[sizePrefV.size()];
    sizePrefV.toArray(data[I_SIZE_PREF]);
    if (updateSizePrefsLong) sizePrefV.toArray(data[I_SIZE_PREF_DEFAULT_LONG]);
    if (updateSizePrefsShort) sizePrefV.toArray(data[I_SIZE_PREF_DEFAULT_SHORT]);
    data[I_VIEWABLE_SEQUENCE] = newViewableSeq;

    // Construct model's sorting columns and directions in the data format.
    // Construct I_SORT_SEQUENCE
    TableModel tModel = table.getModel();
    if (tModel instanceof TableSorter) {
      TableSorter ts = (TableSorter) tModel;
      Vector sortingColumns = ts.getSortingColumns(); // sorting columns are always in raw column coordinates
      int len = sortingColumns.size();
      data[I_SORT_SEQUENCE] = new Integer[len];
      for (int i=0; i<len; i++) {
        data[I_SORT_SEQUENCE][i] = (Integer) sortingColumns.elementAt(i);
      }
    }
  } // end initFromTable()


  public void applyToTable(JTable table) {
    applyToTable(table, false);
  }

  public void applyToTable(JTable table, boolean useDefaultColumnSizes) {
    TableColumnModel tcModel = table.getColumnModel();

    int sizePrefsIndex = useDefaultColumnSizes ? I_SIZE_PREF_DEFAULT_LONG : I_SIZE_PREF;
    if (useDefaultColumnSizes) {
      JSplitPane sp = MiscGui.getParentSplitPane(table);
      if (sp != null && sp.getOrientation() == JSplitPane.HORIZONTAL_SPLIT)
        sizePrefsIndex = I_SIZE_PREF_DEFAULT_SHORT;
    }

    // Apply I_HEADER_NAMES, I_SIZE_PREF, I_SIZE_MAX, I_SIZE_MIN
    int numColumns = tcModel.getColumnCount();
    for (int i=0; i<numColumns; i++) {
      TableColumn tc = tcModel.getColumn(i);
      int modelColumn = table.convertColumnIndexToModel(i);
      int rawColumn = convertColumnToRawModel(modelColumn);
      String headerStr = (String) data[I_HEADER_NAMES][rawColumn];
      tc.setHeaderValue(headerStr);
      int prefSize = ((Integer) data[sizePrefsIndex][rawColumn]).intValue();
      if (prefSize > 0) {
        tc.setPreferredWidth(prefSize);
        tc.setWidth(prefSize);
      }
      int maxSize = ((Integer) data[I_SIZE_MAX][rawColumn]).intValue();
      if (maxSize > 0)
        tc.setMaxWidth(maxSize);
      int minSize = ((Integer) data[I_SIZE_MIN][rawColumn]).intValue();
      if (minSize > 0)
        tc.setMinWidth(minSize);
    }

    // I_VIEWABLE_SEQUENCE cannot be applied to a table, the model already took care of it.

    // Construct model's sorting columns and directions in the data format.
    // Construct I_SORT_SEQUENCE
    TableModel tModel = table.getModel();
    if (tModel instanceof TableSorter) {
      TableSorter ts = (TableSorter) tModel;
      Vector sortingColumns = new Vector(Arrays.asList(data[I_SORT_SEQUENCE]));
      ts.setSortingColumns(sortingColumns);
      ts.resort();
    }
  } // end applyToTable()

}
/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_gui.sortedTable;

import java.util.*;
import javax.swing.event.*;
import javax.swing.table.TableModel;

import com.CH_co.trace.Trace;
import com.CH_gui.table.*;

/**
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 *
 * Class Details:   Sorts by raw columns, not model columns, hence it is
 *                  possible to sort by hidden as well as shown columns.
 *
 *
 * <b>$Revision: 1.18 $</b>
 * @author  Marcin Kurzawa
 * @version
 */

public class TableSorter extends TableMap implements Comparator, TableModelListener {

  int indexes[] = new int[0];
  int indexesReverse[] = new int [0];

  // positive=ascending, negative(+100)=descending; can't have -0 hance the offset
  private Vector sortingColumns = new Vector();

  /** Creates new TableSorter */
  public TableSorter() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableSorter.class, "TableSorter()");
    if (trace != null) trace.exit(TableSorter.class);
  }
  /** Creates new TableSorter */
  public TableSorter(TableModel model) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableSorter.class, "TableSorter(TableModel)");
    if (model instanceof TableSorter)
      throw new IllegalArgumentException("Cannot wrap a sorted map with a sorted map!");
    setModel(model);
    if (trace != null) trace.exit(TableSorter.class);
  }

  public boolean suppressUpdateSorts() {
    return false;
  }

  public void setModel(TableModel model) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableSorter.class, "setModel(TableModel)");

    // fire before we adjust indexes because selection saving involves using valid indexes!
    fireSortNotification(true);

    synchronized (this) {
      // do the rest
      super.setRawModel(model);
      reallocateIndexes();

      clearSortOrders();
      int numOfColumns = model.getColumnCount();
      numOfColumns = (numOfColumns > 2) ? 2 : numOfColumns;

      for (int i=0; i<numOfColumns; i++)
        setSortColumn(numOfColumns-1-i, true);
      sort();
    }

    fireSortNotification(false);

    if (trace != null) trace.exit(TableSorter.class);
  }

  public Vector getSortingColumns() {
    return sortingColumns;
  }

  public void setSortingColumns(Vector columns) {
    sortingColumns = columns;
  }

  /**
   * Return the index of the primary sorting column, -1 if there is no sorting column.
   */
  public int getPrimarySortingColumn() {
    int column = -1;
    if (sortingColumns.size() > 0) {
      column = ((Integer) sortingColumns.elementAt(0)).intValue();
      if (column < 0)
        column = -column-ColumnHeaderData.DESCENDING_OFFSET;
    }
    return column;
  }

  /**
   * Return the +1 or -1 for ascending or descending sorting of the primary sort column, 0 if does not exist.
   */
  public int getPrimarySortingDirection() {
    int dir = 0;
    if (sortingColumns.size() > 0) {
      dir = ((Integer) sortingColumns.elementAt(0)).intValue() >= 0 ? 1 : -1;
    }
    return dir;
  }

  /**
   * @return The index of specified column in the sorting columns vector. 0=primary, -1=not found
   */
  public int getSortingColumnIndex(int column) {
    int index = getSortingColumns().indexOf(new Integer(column));
    if (index == -1)
      index = getSortingColumns().indexOf(new Integer(-(column+ColumnHeaderData.DESCENDING_OFFSET)));
    return index;
  }

  /**
   * @return The direction of sorting for specified column. 1=ascending, -1=descending, 0=undefined
   */
  public int getSortingColumnDirection(int column) {
    int index = getSortingColumnIndex(column);
    if (index >= 0)
      return ((Integer) getSortingColumns().elementAt(index)).intValue() >= 0 ? 1 : -1;
    else
      return 0;
  }

  public int compareRowsByColumn(int row1, int row2, int rawColumn) {
    // Return Code
    int rc = 0;

    TableModel model = getRawModel();
    if (model != null) {

      /* Check for empty objects */

      Object obj1, obj2;

      // If sorting by raw columns, then skip model column to raw column translation.
      if (model instanceof RecordTableModel) {
        RecordTableModel rtModel = (RecordTableModel) model;
        obj1 = rtModel.getValueAtRawColumn(rtModel.getRowObjectNoTrace(row1), rawColumn, true);
        obj2 = rtModel.getValueAtRawColumn(rtModel.getRowObjectNoTrace(row2), rawColumn, true);
      } else {
        obj1 = model.getValueAt(row1, rawColumn);
        obj2 = model.getValueAt(row2, rawColumn);
      }

      /* if both null, return 0, otherwise treat null as less than everything */
      if (obj1 == null && obj2 == null)
        rc = 0;
      else if (obj1 == null)
        rc = -1;
      else if (obj2 == null)
        rc = 1;
      else if (obj1 instanceof String && obj2 instanceof String) {
        rc = ((String) obj1).compareToIgnoreCase((String) obj2);
      }
      // if we are comparing Integer to Long it must be done by its value and method 'compareTo' only works for same instances
      else if (obj1 instanceof Comparable && obj2 instanceof Comparable && obj1 instanceof Number && obj2 instanceof Number) {
        double d1 = ((Number) obj1).doubleValue();
        double d2 = ((Number) obj2).doubleValue();
        if (d1 < d2)
          rc = -1;
        else if (d1 > d2)
          rc = 1;
      }
      else if (obj1 instanceof Comparable && obj2 instanceof Comparable && obj1.getClass().equals(obj2.getClass())) {
        rc = ((Comparable) obj1).compareTo((Comparable) obj2);
      }
      else {
       // if (trace != null) trace.data(40, "not comparable ... convert to String!");
        String s1 = obj1.toString();
        String s2 = obj2.toString();
        rc = s1.compareTo(s2);
      }

    }

    return rc;
  }

  private void reallocateIndexes() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableSorter.class, "reallocateIndexes()");

    TableModel model = getRawModel();
    if (model != null) {
      int rowCount = model.getRowCount();
      indexes = new int[rowCount];
      indexesReverse = new int[rowCount];
      for (int row = 0; row < rowCount; row++) {
        indexes[row] = row;
        indexesReverse[row] = row;
      }
    }

    if (trace != null) trace.exit(TableSorter.class);
  }



  public void tableChanged(TableModelEvent tableModelEvent) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableSorter.class, "tableChanged(TableModelEvent)");
    // if fake pre-delete call
    if (tableModelEvent.getFirstRow() < -1) {
      // skip sorting for item updates
      boolean suppressSort = suppressUpdateSorts() && tableModelEvent.getType() == TableModelEvent.UPDATE;
      if (!suppressSort) {
        fireSortNotification(true, true);
      }
    } else {
      resort(tableModelEvent);
    }
    if (trace != null) trace.exit(TableSorter.class);
  }


  private void resort(TableModelEvent tableModelEvent) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableSorter.class, "resort(TableModelEvent tableModelEvent)");
    if (trace != null) trace.args(tableModelEvent);
    // skip sorting for item updates
    boolean suppressSort = suppressUpdateSorts() && tableModelEvent != null && tableModelEvent.getType() == TableModelEvent.UPDATE;
    // fire before we adjust indexes because selection saving involves using valid indexes!
    if (!suppressSort)
      fireSortNotification(true);
    if (tableModelEvent != null) {
      // Notify of insertions/deletions first, then sort and notify of update.
      // This should fix the SortSequence problem.
      TableSorter.super.tableChanged(tableModelEvent);
    }
    if (!suppressSort) {
      synchronized (this) {
        reallocateIndexes();
        sort();
      }
      fireSortNotification(false);
    }
    if (trace != null) trace.exit(TableSorter.class);
  }


  public void resort() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableSorter.class, "resort()");
    resort(null);
    if (trace != null) trace.exit(TableSorter.class);
  }


  /**
   * Immediately sort the table and fire table updated event for all rows.
   * Threaded sort is out of the question because we need to be in synch with selection listeners.
   */
  private void sort() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableSorter.class, "sort()");

    TableModel model = getRawModel();
    if (model != null) {

      synchronized (this) {
        int length = indexes.length;
        Object[] objs = new Object[length];
        for (int i=0; i<length; i++) {
          objs[i] = new Integer(indexes[i]);
        }

//        if (model instanceof RecordTableModel) {
//          com.CH_co.service.records.FolderPair fPair = ((RecordTableModel) model).getParentFolderPair();
//          if (fPair != null && fPair.getId().longValue() > 0) {
//            System.out.println("" + new java.util.Date() + "  in sort for folder " + fPair.getId());
//          }
//        }

        /** This call sorts entire table where compare() method of 'this' object is used. */
        if (trace != null) trace.data(10, "calling Arrays.sort()");
        int token = 0;
        if (trace != null) token = trace.pause();
        try {
          Arrays.sort(objs, TableSorter.this);
        } catch (Throwable t) {
          if (trace != null) trace.resume(token);
          if (trace != null) trace.exception(TableSorter.class, 15, t);
        }
        if (trace != null) trace.resume(token);
        if (trace != null) trace.data(20, "returned from Arrays.sort()");

        sortPostProcessing(objs);

        for (int i=0; i<length; i++) {
          int row = ((Integer) objs[i]).intValue();
          indexes[i] = row;
          indexesReverse[row] = i;
        }
      }

      //fireTableStructureChanged();

      int size = model.getRowCount();
      if (size > 0) {
        fireTableChanged(new TableModelEvent(this, -2));
        //fireTableDataChanged();
      }
    }

    if (trace != null) trace.exit(TableSorter.class);
  }

  /**
   * Post processing sort operation that allowes sub classes to add additional
   * functionality to the sort operation.
   * It is called immediately after the normal sort() operation.
   * For example, threaded message view can implement additional sort routine
   * that will arrange the messages in a thread oriented view.
   * By default this method does nothing.
   */
  public void sortPostProcessing(Object[] objs) {
  }

  public void setValueAt(Object value, int row, int column) {
    TableModel model = getRawModel();
    if (model != null) {
      model.setValueAt(value, convertMyRowIndexToModel(row), column);
    }
  }

  public Object getValueAt(int row, int column) {
    Object rc = null;
    TableModel model = getRawModel();
    if (model != null) {
      int indexLength = 0;
      synchronized (this) {
        indexLength = indexes.length;
      }
      if (indexLength != 0 && model.getRowCount() > row && row >= 0 && model.getColumnCount() > column)
        rc = model.getValueAt(convertMyRowIndexToModel(row), column);
    }
    return rc;
  }

  /**
   * If the column is not in sort order, then sort it in ascending order.
   * If the column is not a primary sort column, than make it primary and keep the original direction.
   * If the column is a primary sort column, than invert the sorting direction.
   */
  public void sortByColumn(int column) {
    sortByColumns(new Integer[] { new Integer(column) });
  }
  /**
   * Make direction for all columns uniform, based on first specified column.
   */
  public void sortByColumns(Integer[] columns) {
    int firstColumn = columns[0].intValue();
    int index = getSortingColumnIndex(firstColumn);
    boolean ascending = getSortingColumnDirection(firstColumn) > 0 ? true : false;
    if (index <= 0)
      ascending = !ascending;
    sortByColumns(columns, ascending);
  }

  public void sortByColumn(int column, boolean ascending) {
    sortByColumns(new Integer[] { new Integer(column) }, new Boolean[] { Boolean.valueOf(ascending) });
  }

  /**
   * @param array of column indexes from most significant to least significant
   */
  public void sortByColumns(Integer[] columns, boolean ascending) {
    Boolean[] directions = new Boolean[columns.length];
    for (int i=0; i<columns.length; i++)
      directions[i] = Boolean.valueOf(ascending);
    sortByColumns(columns, directions);
  }
  public void sortByColumns(Integer[] columns, Boolean[] directions) {
    for (int i=columns.length-1; i>=0; i--) {
      int column = columns[i].intValue();
      boolean ascending = directions[i].booleanValue();
      setSortColumn(column, ascending);
    }
    // fire before we adjust indexes because selection saving involves using valid indexes!
    fireSortNotification(true);
    sort();
    fireSortNotification(false);
  }

  private void clearSortOrders() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableSorter.class, "clearSortOrders()");
    sortingColumns.removeAllElements();
    setSortColumn(0, true);
    if (trace != null) trace.exit(TableSorter.class);
  }

  private void setSortColumn(int column, boolean ascending) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableSorter.class, "setSortColumn("+column+", "+ascending+")");

    int spot = getSortingColumnIndex(column);

    // remove column/direction from old ordering sequence
    if (spot >= 0) {
      sortingColumns.removeElementAt(spot);
    }

    // insert to the front of ordering sequence;
    sortingColumns.insertElementAt(new Integer(ascending ? column : (-(column+ColumnHeaderData.DESCENDING_OFFSET))), 0);

    /*
    // fire before we adjust indexes because selection saving involves using valid indexes!
    fireSortNotification(true);
    sort();
    fireSortNotification(false);
     */

    if (trace != null) trace.exit(TableSorter.class);
  }

  /**
   * Return the index of the row in the model whose data is being displayed in
   * the row viewRowIndex in the display or -1 if invalid view row is specified.
   */
  synchronized int convertMyRowIndexToModel(int viewRowIndex) {
    int rc = -1;
    if (viewRowIndex >= 0 && viewRowIndex <= (indexes.length-1)) {
      rc = indexes[viewRowIndex];
    }
    return rc;
  }

  /**
   * Return the index of the row in the view which is displaying the data from the column modelRowIndex in the model.
   */
  synchronized int convertMyRowIndexToView(int modelRowIndex) {
    int rc = -1;
    if (modelRowIndex >= 0 && modelRowIndex < indexesReverse.length)
      rc = indexesReverse[modelRowIndex];
    return rc;
  }

  /**
   * Comparator Interface methods
   */
  public int compare(final Object o1, final Object o2) {
    int row1 = ((Integer) o1).intValue();
    int row2 = ((Integer) o2).intValue();
    int rc = 0;
    for (int level=0; level < sortingColumns.size(); level++) {
      int sortingColumn = ((Integer) sortingColumns.elementAt(level)).intValue();
      int rawColumn = sortingColumn >= 0 ? sortingColumn : -sortingColumn-ColumnHeaderData.DESCENDING_OFFSET;
      rc = compareRowsByColumn(row1, row2, rawColumn);
      if (rc != 0) {
        // if sorting this column in descending order, invert return code.
        rc = sortingColumn >= 0 ? rc : -rc;
        break;
      }
    }
    return rc;
  }

}
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

package com.CH_gui.sortedTable;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.CH_co.trace.Trace;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 *  Sorts the table by column which is clicked.
 *  Click on the column header means ascending order
 *  Shift and click on the column header means descending order
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.12 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class TableHeaderSorter extends MouseAdapter {

  private TableSorter sorter;
  private JSortedTable table;

  /** Creates new TableHeaderSorter */
  private TableHeaderSorter() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableHeaderSorter.class, "TableHeaderSorter()");
    if (trace != null) trace.exit(TableHeaderSorter.class);
  }

  /* The header sorter has to be installed to the table if requested 
    and the TableHeaderSorter instance will be constructed here 
  */
  public static void install(TableSorter sorter, JSortedTable table) {
    TableHeaderSorter tableHeaderSorter = new TableHeaderSorter();
    tableHeaderSorter.sorter = sorter;
    tableHeaderSorter.table = table;
    JTableHeader tableHeader = table.getTableHeader();
    tableHeader.addMouseListener(tableHeaderSorter);
  }

  public void mouseClicked(MouseEvent mouseEvent) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableHeaderSorter.class, "mouseClicked(MouseEvent)");

    TableColumnModel columnModel = table.getColumnModel();
    if (columnModel == null) {
      if (trace != null) trace.exit(TableHeaderSorter.class);
      return;
    }
    int viewColumn = columnModel.getColumnIndexAtX(mouseEvent.getX());
    int column = table.convertColumnIndexToModel(viewColumn);

    if (mouseEvent.getClickCount() == 1 && column != -1) {
      if (trace != null) trace.data(30, "SORTING . . .");
      boolean shiftPressed = (mouseEvent.getModifiers() & InputEvent.SHIFT_MASK) == 0;
      boolean controlPressed = (mouseEvent.getModifiers() & InputEvent.CTRL_MASK) == 0;

      // Shift and click
      if ( (shiftPressed && !controlPressed) || (!shiftPressed && controlPressed) ) {
        sorter.sortByColumn(column, shiftPressed);
      }
      else {
        // Click only
        sorter.sortByColumn(column);
      }
    }
    if (trace != null) trace.exit(TableHeaderSorter.class);
  }
}
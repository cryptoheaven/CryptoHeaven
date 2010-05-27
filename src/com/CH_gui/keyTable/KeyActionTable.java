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

package com.CH_gui.keyTable;

import com.CH_gui.util.ActionProducerI;
import java.awt.dnd.*;
import javax.swing.*;

import com.CH_gui.action.*;
import com.CH_gui.table.*;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.13 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class KeyActionTable extends RecordActionTable implements ActionProducerI {

  private Action[] actions;

  private static final int NUM_OF_SORT_COLUMNS = KeyTableModel.columnHeaderData.data[ColumnHeaderData.I_SHORT_DISPLAY_NAMES].length;

  private static final int SORT_ASC_ACTION = 2;
  private static final int SORT_DESC_ACTION = 3;
  private static final int SORT_BY_FIRST_COLUMN_ACTION = 4;
  private static final int CUSTOMIZE_COLUMNS_ACTION = SORT_BY_FIRST_COLUMN_ACTION + NUM_OF_SORT_COLUMNS;

  private static final int NUM_ACTIONS = CUSTOMIZE_COLUMNS_ACTION + 1;


  /** Creates new KeyActionTable, fetches the initial data from the database. */
  public KeyActionTable() {
    super(new KeyTableModel());
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(KeyActionTable.class, "KeyActionTable()");

    ((KeyTableModel) getTableModel()).setAutoUpdate(true);
    initActions();

    if (trace != null) trace.exit(KeyActionTable.class);
  }

  /** Creates new KeyActionTable, sets initial data with specified records and filters. */
  public KeyActionTable(KeyRecord[] initialData) {
    super(new KeyTableModel(initialData));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(KeyActionTable.class, "KeyActionTable(KeyRecord[] initialData)");
    if (trace != null) trace.args(initialData);

    ((KeyTableModel) getTableModel()).setAutoUpdate(true);
    initActions();

    if (trace != null) trace.exit(KeyActionTable.class);
  }


  public DragGestureListener createDragGestureListener() {
    return null;
  }
  public DropTargetListener createDropTargetListener() {
    return null;
  }


  private void initActions() {
    actions = new Action[NUM_ACTIONS];
    {
      ButtonGroup sortAscDescGroup = new ButtonGroup();
      actions[SORT_ASC_ACTION] = new SortAscDescAction(true, 0+Actions.LEADING_ACTION_ID_RECORD_ACTION_TABLE, sortAscDescGroup);
      actions[SORT_DESC_ACTION] = new SortAscDescAction(false, 1+Actions.LEADING_ACTION_ID_RECORD_ACTION_TABLE, sortAscDescGroup);
      actions[CUSTOMIZE_COLUMNS_ACTION] = new CustomizeColumnsAction(2+Actions.LEADING_ACTION_ID_RECORD_ACTION_TABLE);
      ButtonGroup columnSortGroup = new ButtonGroup();
      for (int i=0; i<NUM_OF_SORT_COLUMNS; i++) {
        actions[SORT_BY_FIRST_COLUMN_ACTION+i] = new SortByColumnAction(i, i+3+Actions.LEADING_ACTION_ID_RECORD_ACTION_TABLE, columnSortGroup);
      }
    }
    setEnabledActions();
  }


  /****************************************************************************/
  /*        A c t i o n P r o d u c e r I                                  
  /****************************************************************************/

  /** @return all the acitons that this objects produces.
   */
  public Action[] getActions() {
    return actions;
  }

  /**
   * Enables or Disables actions based on the current state of the Action Producing component.
   */
  public void setEnabledActions() {

    // Always enable sort actions
    actions[SORT_ASC_ACTION].setEnabled(true);
    actions[SORT_DESC_ACTION].setEnabled(true);
    actions[CUSTOMIZE_COLUMNS_ACTION].setEnabled(true);
    for (int i=SORT_BY_FIRST_COLUMN_ACTION; i<SORT_BY_FIRST_COLUMN_ACTION+NUM_OF_SORT_COLUMNS; i++) 
      actions[i].setEnabled(true);

  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "KeyActionTable";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
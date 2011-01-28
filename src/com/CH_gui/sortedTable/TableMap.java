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

package com.CH_gui.sortedTable;

import java.lang.ref.WeakReference;
import javax.swing.table.*;
import javax.swing.event.*;

import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.11 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */

public class TableMap extends AbstractTableModel implements TableModelListener {

  private WeakReference modelRef;
  private EventListenerList sortingListenerList = new EventListenerList();

  /** Creates new TableMap */
  public TableMap() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableMap.class, "TableMap()");
    if (trace != null) trace.exit(TableMap.class);
  }

  public TableModel getRawModel() {
    TableModel model = null;
    if (modelRef != null) {
      model = (TableModel) modelRef.get();
    }
    return model;
  }
  
//  public TableModel getModel() {
//    return model;
//  }

  public void setRawModel (TableModel model) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableMap.class, "setModel()");
    
    TableModel oldModel = getRawModel();
    if (oldModel != null) 
      oldModel.removeTableModelListener(this);

    modelRef = new WeakReference(model);

    if (model != null)
      model.addTableModelListener(this);

    if (trace != null) trace.exit(TableMap.class, model);
  }

  public Class getColumnClass(int column) {
    TableModel model = getRawModel();
    if (model != null) 
      return model.getColumnClass(column);
    else
      return null;
  }

  public int getColumnCount() {
    TableModel model = getRawModel();
    return ((model == null) ? 0 : model.getColumnCount());
  }

  public String getColumnName (int column) {
    TableModel model = getRawModel();
    return (model == null) ? null : model.getColumnName(column);
  }

  public int getRowCount() {
    TableModel model = getRawModel();
    return (model == null) ? 0 : model.getRowCount();
  }

  public Object getValueAt(int row, int column) {
    TableModel model = getRawModel();
    return (model == null) ? null : model.getValueAt(row, column);
  }

  public void setValueAt(Object value, int row, int column) {
    TableModel model = getRawModel();
    if (model != null)
      model.setValueAt(value, row, column);
  }

  public boolean isCellEditable (int row, int column) {
    TableModel model = getRawModel();
    return (model == null) ? false : model.isCellEditable(row, column);
  }

  public void tableChanged (final TableModelEvent tableModelEvent) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableMap.class, "tableChanged(TableModelEvent tableModelEvent)");
    // sort will fire appropriate update event...
    // incase rows were inserted or deleted...
    if (tableModelEvent.getFirstRow() < -1)
      fireSortNotification(true, true);
    else {
      fireTableChanged(tableModelEvent);
    }
    if (trace != null) trace.exit(TableMap.class);
  }


  /********************
  Sorting Notifications
  ********************/
  public void addTableModelSortListener(TableModelSortListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableMap.class, "addTableModelSortListener(TableModelSortListener l)");
    if (trace != null) trace.args(l);
    sortingListenerList.add(TableModelSortListener.class, l);
    if (trace != null) trace.exit(TableMap.class);
  }

  public void removeTableModelSortListener(TableModelSortListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableMap.class, "removeTableModelSortListener(TableModelSortListener l)");
    if (trace != null) trace.args(l);
    sortingListenerList.remove(TableModelSortListener.class, l);
    if (trace != null) trace.exit(TableMap.class);
  }
  
  public void removeTableModelSortListeners() {
    TableModelSortListener[] listeners = (TableModelSortListener[]) sortingListenerList.getListeners(TableModelSortListener.class);
    if (listeners != null && listeners.length > 0)
      for (int i=0; i<listeners.length; i++)
        sortingListenerList.remove(TableModelSortListener.class, listeners[i]);
  }

  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.
   */
  public void fireSortNotification(boolean preSort) {
    fireSortNotification(preSort, false);
  }
  public void fireSortNotification(boolean preSort, boolean isDeleteEvent) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableMap.class, "fireSortNotification(boolean preSort, boolean isDeleteEvent)");
    if (trace != null) trace.args(preSort);
    if (trace != null) trace.args(isDeleteEvent);

    // Guaranteed to return a non-null array
    Object[] listeners = sortingListenerList.getListenerList();
    TableModelSortEvent e = null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length-2; i>=0; i-=2) {
      if (listeners[i] == TableModelSortListener.class) {
        // Lazily create the event:
        if (e == null) e = new TableModelSortEvent(this);
        if (preSort) {
          if (isDeleteEvent)
            ((TableModelSortListener)listeners[i+1]).preSortDeleteNotify(e);
          else
            ((TableModelSortListener)listeners[i+1]).preSortNotify(e);
        }
        else {
          ((TableModelSortListener)listeners[i+1]).postSortNotify(e);
        }
      }
    }
    if (trace != null) trace.exit(TableMap.class);
  }

}
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
import com.CH_co.service.records.Record;

import com.CH_gui.sortedTable.*;

import java.util.ArrayList;
import java.util.Vector;

import javax.swing.ListSelectionModel;

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
public class RecordTableSelection extends Object {

  Vector selectedRecordIDsV;

  /** Creates new RecordTableSelection */
  private RecordTableSelection() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableSelection.class, "RecordTableSelection()");
    selectedRecordIDsV = new Vector();
    if (trace != null) trace.exit(RecordTableSelection.class);
  }

  public RecordTableSelection(Long objId) {
    addSelectedID(objId);
  }

  private void addSelectedID(Long objId) {
    if (selectedRecordIDsV == null) selectedRecordIDsV = new Vector();
    if (!selectedRecordIDsV.contains(objId)) selectedRecordIDsV.addElement(objId);
  }

//  public boolean removeSelectedID(Long objId) {
//    return selectedRecordIDsV.remove(objId);
//  }

  /**
   * Creates new data object that stores current table selections.
   */
  public static RecordTableSelection getData(RecordTableScrollPane recordTable) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableSelection.class, "getData(RecordTableScrollPane recordTable)");
    if (trace != null) trace.args(recordTable);

    RecordTableSelection selection = new RecordTableSelection();

    Record[] records = recordTable.getSelectedRecords();
    if (records != null) {
      for (int i=0; i<records.length; i++) {
        selection.selectedRecordIDsV.addElement(records[i].getId());
      }
    }

    if (trace != null) trace.exit(RecordTableSelection.class, selection);
    return selection;
  }

  /**
   * Restores selection back onto the table.
   */
  public void restoreData(RecordTableScrollPane recordTable) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableSelection.class, "restoreData(RecordTableScrollPane recordTable)");
    if (trace != null) trace.args(recordTable);

    if (trace != null) trace.data(10, "selectedRecordIDsV", selectedRecordIDsV);

    if (recordTable != null && recordTable.getJSortedTable() != null) {
      JSortedTable jst = recordTable.getJSortedTable();
      RecordTableModel rtm = recordTable.getTableModel();

      ListSelectionModel selectionModel = jst.getSelectionModel();
      Vector selectionDataV = new Vector();

      int rowCount = jst.getRowCount();
      for (int i=0; i<rowCount; i++) {
        int rowView = i;
        boolean selected = selectionModel.isSelectedIndex(rowView);

        int rowModel = jst.convertMyRowIndexToModel(rowView);

        Long objId = rtm.getRowObjectNoTrace(rowModel).getId();
        boolean shouldSelect = selectedRecordIDsV.contains(objId);

        boolean add = !selected && shouldSelect;
        boolean remove = selected && !shouldSelect;
        if (add || remove) {
          int command = add ? SelectionData.COMMAND_ADD : SelectionData.COMMAND_REMOVE;
          selectionDataV.addElement(new SelectionData(command, rowView, rowView));
        }
      }

      if (selectionDataV.size() > 0) {
        // compress the selection data vector to merge the neighbouring similar elements
        SelectionData lastData = null;
        for (int i=0; i<selectionDataV.size(); i++) {
          if (lastData == null)
            lastData = (SelectionData) selectionDataV.elementAt(i);
          else {
            SelectionData thisData = (SelectionData) selectionDataV.elementAt(i);
            if (lastData.command == thisData.command && lastData.toRow == thisData.fromRow-1) {
              thisData.fromRow = lastData.fromRow;
              selectionDataV.removeElementAt(i-1);
              i--;
            }
            lastData = thisData;
          }
        } // end for
        boolean isOrderInverted = selectionModel.getLeadSelectionIndex() < selectionModel.getAnchorSelectionIndex();
        // apply selection vector
        for (int i=0; i<selectionDataV.size(); i++) {
          SelectionData selectionData = null;
          if (!isOrderInverted)
            selectionData = (SelectionData) selectionDataV.elementAt(i);
          else
            selectionData = (SelectionData) selectionDataV.elementAt(selectionDataV.size()-1-i);
          int fromRow = !isOrderInverted ? selectionData.fromRow : selectionData.toRow;
          int toRow = !isOrderInverted ? selectionData.toRow : selectionData.fromRow;
          if (selectionData.command == SelectionData.COMMAND_ADD)
            selectionModel.addSelectionInterval(fromRow, toRow);
          else
            selectionModel.removeSelectionInterval(fromRow, toRow);
        }
        // fix anchor to first row selected
        if (!isOrderInverted)
          selectionModel.setAnchorSelectionIndex(selectionModel.getMinSelectionIndex());
        else
          selectionModel.setAnchorSelectionIndex(selectionModel.getMaxSelectionIndex());
        // if single row selection, (or no row selection) fix focus by adjusting our Lead row
        if (selectionModel.getMinSelectionIndex() == selectionModel.getMaxSelectionIndex())
          selectionModel.setLeadSelectionIndex(selectionModel.getMinSelectionIndex());
      }
    }

    if (trace != null) trace.exit(RecordTableSelection.class);
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<selectedRecordIDsV.size(); i++) {
      sb.append(selectedRecordIDsV.elementAt(i));
      sb.append(",");
    }

    return "[RecordTableSelection"
      + ": selectedRecordIDsV=" + sb.toString()
      + "]";
  }

  public int hashCode() {
    assert false : "hashCode not designed";
    return 0;
  }

  public boolean equals(Object o) {
    if (o instanceof RecordTableSelection) {
      RecordTableSelection rts = (RecordTableSelection) o;
      if (selectedRecordIDsV == rts.selectedRecordIDsV) {
        return true;
      } else if (selectedRecordIDsV == null || rts.selectedRecordIDsV == null) {
        return false;
      } else if (selectedRecordIDsV.equals(rts.selectedRecordIDsV)) {
        return true;
      } else if (selectedRecordIDsV.size() == rts.selectedRecordIDsV.size()) {
        ArrayList a1 = new ArrayList(selectedRecordIDsV);
        ArrayList a2 = new ArrayList(rts.selectedRecordIDsV);
        return a1.containsAll(a2) && a2.containsAll(a1);
      } else {
        return false;
      }
    } else {
      return super.equals(o);
    }
  }

  private static class SelectionData extends Object {
    private static int COMMAND_ADD = 1;
    private static int COMMAND_REMOVE = 2;
    private int command;
    private int fromRow;
    private int toRow;
    private SelectionData(int command, int fromRow, int toRow) {
      this.command = command;
      this.fromRow = fromRow;
      this.toRow = toRow;
    }
  }
}
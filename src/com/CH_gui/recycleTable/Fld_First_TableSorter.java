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

package com.CH_gui.recycleTable;

import com.CH_co.service.records.FolderPair;
import com.CH_co.service.records.Record;
import javax.swing.table.TableModel;

import com.CH_co.trace.Trace;
import com.CH_gui.sortedTable.JSortedTable;
import com.CH_gui.sortedTable.TableSorter;
import com.CH_gui.table.RecordTableModel;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *  The only difference between this class and a TableSorter is that
 *  a method compareRowsByColumn is overriten, the new method makes sure that
 *  folders will go ahead of any files when sorting.
 * Class Details:
 *
 *
 * <b>$Revision: 1.1 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class Fld_First_TableSorter extends TableSorter {

  /** Creates new Fld_First_TableSorter */
  public Fld_First_TableSorter() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fld_First_TableSorter.class, "Fld_First_TableSorter()");
    if (trace != null) trace.exit(Fld_First_TableSorter.class);
  }
  /** Creates new Fld_First_TableSorter */
  public Fld_First_TableSorter(TableModel model) {
    super(model);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fld_First_TableSorter.class, "Fld_First_TableSorter(TableModel model)");
    if (trace != null) trace.args(model);
    if (trace != null) trace.exit(Fld_First_TableSorter.class);
  }

  /* If folders are compared put them first ahead of filesd, otherwise call a super method */
  public int compareRowsByColumn (int row1, int row2, int column) {
    // Return Code
    int rc = 0;

    boolean isFolder1;
    boolean isFolder2;

    int typeColumn = JSortedTable.getColumnIndex(getRawModel(), RecycleTableModel.STR_TYPE);
    String fileType1 = null;
    String fileType2 = null;
    TableModel model = getRawModel();
    if (model instanceof RecordTableModel) {
      RecordTableModel rtModel = (RecordTableModel) model;
      Record rec1 = rtModel.getRowObjectNoTrace(row1);
      Record rec2 = rtModel.getRowObjectNoTrace(row2);
      isFolder1 = rec1 != null && rec1 instanceof FolderPair;
      isFolder2 = rec2 != null && rec2 instanceof FolderPair;
    } else {
      fileType1 = (String) model.getValueAt(row1, typeColumn);
      fileType2 = (String) model.getValueAt(row2, typeColumn);
      isFolder1 = fileType1 != null && (fileType1.equals(RecycleTableModel.STR_FILE_FOLDER) || fileType1.equals(RecycleTableModel.STR_SHARED_FOLDER));
      isFolder2 = fileType2 != null && (fileType2.equals(RecycleTableModel.STR_FILE_FOLDER) || fileType2.equals(RecycleTableModel.STR_SHARED_FOLDER));
    }

    if (isFolder1 && !isFolder2)
      rc = -1;
    else if (!isFolder1 && isFolder2)
      rc = 1;
    else
      rc = super.compareRowsByColumn (row1, row2, column);

    return rc;
  }

  /* Disable cell editing */
  public boolean isCellEditable (int row, int column) {
    boolean isEditable = false;
    return isEditable;
  }
}
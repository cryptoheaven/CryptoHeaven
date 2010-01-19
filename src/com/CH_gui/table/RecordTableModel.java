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

import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JTable;
import javax.swing.event.EventListenerList;
import javax.swing.table.AbstractTableModel;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: Provides a superclass table model for all record types.
 *                    Also manages all raw and viewable columns and provides
 *                    column number translation for subclases.
 * Class Details: This class manages hidden and shown columns and handles
 *                structure changes to resize the columns properly.
 *                Convention is that all column values mean, model columns,
 *                and not raw columns (unless atherwise specified).
 * <b>$Revision: 1.27 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public abstract class RecordTableModel extends AbstractTableModel implements SearchTextProviderI {

  public static final Integer TIMESTAMP_PRL = new Integer(110); // Preferred Long
  public static final Integer TIMESTAMP_PRS = new Integer(93);  // Preferred Short
  public static final Integer TIMESTAMP_MAX = new Integer(130); // Mac OSX has wider text
  public static final Integer TIMESTAMP_MIN = new Integer(60);

  /** vector of all records stored in this model */
  private Vector recordsV = new Vector();
  /** hashtable of all records stored in this model, always holds identical data as recordsV, its purpose is to speed up queries */
  private Hashtable recordsHT = new Hashtable();

  private ColumnHeaderData columnHeaderData;
  public ButtonGroup sortButtonGroup;
  public ButtonGroup sortDirButtonGroup;
  // global flag for editable columns
  boolean editable;
  private RecordFilter recordFilter;
  private RecordFilter recordFilterNarrowing;
  private RecordFilter multiFilter;

  // Main folder pair of which this class is managing messages
  private FolderPair folderPair;

  private EventListenerList myListenerList = new EventListenerList();

  /** Used by Chat table to scroll to the inserted Record */
  public CallbackI recordInsertionCallback;

  /** 
   * Creates new RecordTableModel 
   * Default filter is no-filter = accept all; for objects managed in folders this is usually not desirable.
   */
  public RecordTableModel(ColumnHeaderData columnHeaderData) {
    this (columnHeaderData, null);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableModel.class, "RecordTableModel(ColumnHeaderData columnHeaderData)");
    if (trace != null) trace.exit(RecordTableModel.class);
  }

  /** Creates new RecordTableModel */
  public RecordTableModel(ColumnHeaderData columnHeaderData, RecordFilter recordFilter) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableModel.class, "RecordTableModel(ColumnHeaderData columnHeaderData, RecordFilter recordFilter)");
    if (trace != null) trace.args(columnHeaderData);
    if (trace != null) trace.args(recordFilter);
    // Each instance of the table will get its own header data so that
    // changes by that table will not affect other tables of the same type.
    this.columnHeaderData = (ColumnHeaderData) Misc.cloneSerializable(columnHeaderData);
    this.recordFilter = recordFilter;
    if (trace != null) trace.exit(RecordTableModel.class);
  }

  public void setSortButtonGroup(ButtonGroup sortButtonGroup) {
    this.sortButtonGroup = sortButtonGroup;
  }

  public void setSortDirButtonGroup(ButtonGroup sortDirButtonGroup) {
    this.sortDirButtonGroup = sortDirButtonGroup;
  }


  public void setParentFolderPair(FolderPair folderPair) {
    FolderPair oldFolderPair = this.folderPair;
    this.folderPair = folderPair;
    if (oldFolderPair != folderPair)
      fireParentFolderChanged(oldFolderPair, folderPair);
  }
  public FolderPair getParentFolderPair() {
    return folderPair;
  }

  /**
   * When folders are fetched, their IDs are cached so that we know if table fetch is required when
   * user switches focus to another folder...
   * This vector should also be cleared when users are switched...
   */
  public abstract Vector getCachedFetchedFolderIDs();

  /**
   * Assigns or removes record cache listeners in the cache.
   */
  public abstract void setAutoUpdate(boolean b);

  /**
   * Checks if folder share's content of a given ID was already retrieved.
   */
  public abstract boolean isContentFetched(Long shareId);

  /**
   * Initializes tables data
   */
  public synchronized void initData(Long folderId) {
  }
  public synchronized void initData(Long folderId, boolean forceSwitch) {
    initData(folderId);
  }

  /**
   * Triggers refreshing of view when filter changes.
   */
  public synchronized void reInitData() {
    FolderPair fPair = getParentFolderPair();
    if (fPair != null)
      initData(fPair.getId(), true);
  }

  public void setEditable(boolean b) {
    editable = b;
  }

  public boolean isEditable() {
    return editable;
  }

  public synchronized void setFilter(RecordFilter recordFilter) {
    this.recordFilter = recordFilter;
    this.multiFilter = null; // reset our combined filter
  }

  public synchronized void setFilterNarrowing(RecordFilter recordFilterNarrowing) {
    boolean isChanged = this.recordFilterNarrowing != recordFilterNarrowing;
    this.recordFilterNarrowing = recordFilterNarrowing;
    this.multiFilter = null; // reset our combined filter
    if (isChanged) {
      reInitData();
    }
  }

  public synchronized RecordFilter getFilterCombined() {
    if (recordFilter != null && recordFilterNarrowing == null)
      return recordFilter;
    else if (recordFilter == null && recordFilterNarrowing != null)
      return recordFilterNarrowing;
    else if (recordFilter != null && recordFilterNarrowing != null) {
      if (multiFilter == null)
        multiFilter = new MultiFilter(recordFilter, recordFilterNarrowing, MultiFilter.AND);
      return multiFilter;
    }
    else
      return null;
  }

  public synchronized RecordFilter getFilterNarrowing() {
    return recordFilterNarrowing;
  }

  public int getColumnCount() {
    return columnHeaderData.getColumnCount();
  }

  public synchronized int getRowCount() {
    return recordsV.size();
  }


  /**
   * JTable uses this method to fetch displayable Objects for the table cells.
   * The coordinates used are the display model row and column.
   */
  public synchronized Object getValueAt(int row, int column) {
    return getValueAt(row, column, false);
  }
  public synchronized Object getValueAt(int row, int column, boolean forSortOnly) {
    if (row >= 0 && row < recordsV.size())
      return getValueAtRawColumn((Record) recordsV.elementAt(row), columnHeaderData.convertColumnToRawModel(column), forSortOnly);
    else
      return null;
  }

  public abstract Object getValueAtRawColumn(Record record, int rawColumn, boolean forSortOnly);
  public abstract RecordTableCellRenderer createRenderer();

  public Collection getSearchableCharSequencesFor(Object searchableObj) {
    return getSearchableCharSequencesFor(searchableObj, true);
  }
  public Collection getSearchableCharSequencesFor(Object searchableObj, boolean providerSetting) {
    return null;
  }

  public String getColumnName(int column) {
    String name = columnHeaderData.getRawColumnName(columnHeaderData.convertColumnToRawModel(column));
    return name;
  }


  /**
   * @return Record object at specified row position.
   */
  public synchronized Record getRowObject(int modelRow) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableModel.class, "getRowObject(int modelRow)");
    if (trace != null) trace.args(modelRow);
    Record record = getRowObjectNoTrace(modelRow);
    if (trace != null) trace.exit(RecordTableModel.class, record);
    return record;
  }
  public synchronized Record getRowObjectNoTrace(int modelRow) {
    Record record = null;
    if (modelRow >= 0 && modelRow < recordsV.size()) {
      record = (Record) recordsV.elementAt(modelRow);
    }
    return record;
  }

  /**
   * @return model row index for a given object ID, -1 if not found.
   */
  public synchronized int getRowForObject(Long id) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableModel.class, "getRowForObject(Long id)");
    if (trace != null) trace.args(id);
    int row = -1;
    for (int i=0; i<recordsV.size(); i++) {
      Record record = (Record) recordsV.elementAt(i);
      if (record.getId().equals(id)) {
        row = i;
        break;
      }
    }
    if (trace != null) trace.exit(RecordTableModel.class, row);
    return row;
  }

  public synchronized boolean contains(Record rec) {
    boolean rc = false;
    if (rec != null) {
      if (recordsHT.containsKey(rec.getId())) {
        rc = true;
      }
    }
    return rc;
  }

  public synchronized boolean containsAnyOf(Record[] recs) {
    boolean rc = false;
    if (recs != null) {
      for (int i=0; i<recs.length; i++) {
        if (recordsHT.containsKey(recs[i].getId())) {
          rc = true;
          break;
        }
      }
    }
    return rc;
  }

  /** 
   * Sets the data for the model.
   */
  public synchronized void setData(Record[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableModel.class, "setData(Record[] records)");
    if (trace != null) trace.args(records);

    Vector insertedRecsV = null;

    // clear up the table first
    removeData();
    if (records != null && records.length > 0) {
      RecordFilter filter = getFilterCombined();
      if (filter == null) {
        List recs = Arrays.asList(records);
        recordsV.addAll(recs);
        for (int i=0; i<records.length; i++) recordsHT.put(records[i].getId(), records[i]);
        if (recordInsertionCallback != null) {
          if (insertedRecsV == null) insertedRecsV = new Vector();
          insertedRecsV.addAll(recs);
        }
      } else {
        for (int i=0; i<records.length; i++ ) {
          if (filter.keep(records[i])) {
            recordsV.addElement(records[i]);
            recordsHT.put(records[i].getId(), records[i]);
            if (recordInsertionCallback != null) {
              if (insertedRecsV == null) insertedRecsV = new Vector();
              insertedRecsV.addElement(records[i]);
            }
          }
        }
      }
      if (recordsV.size() > 0) {
        if (trace != null) trace.info(20, "RecordTableModel.fireTableRowsInserted(0, "+(recordsV.size()-1)+");");
        fireTableRowsInserted(0, recordsV.size()-1);
      }
    }
    // Notify callback of inserted records
    if (recordInsertionCallback != null && insertedRecsV != null && insertedRecsV.size() > 0) {
      recordInsertionCallback.callback(insertedRecsV);
    }
    if (trace != null) trace.exit(RecordTableModel.class);
  }

  /**
   * Updates the specified records into the model by merging the new updates or
   * adding the elements if they don't already exist in the model.  If any of the records
   * should not be in the model, it will be removed with a call to removeData().
   */
  public synchronized void updateData(Record[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableModel.class, "updateData(Record[] records)");
    if (trace != null) trace.args(records);

    Vector insertedRecsV = null;

    if (records != null) {
      int countInserted = 0;
      int countUpdated = 0;
      int countToDelete = 0;
      Vector removeRecordsV = null;
      for (int i=0; i<records.length; i++) {
        Record newRec = records[i];
        boolean keep = keep(newRec);
        Record rec = (Record) recordsHT.get(newRec.getId());
        if (keep) {
          if (rec != null) {
            rec.merge(newRec);
            countUpdated ++;
          } else {
            recordsV.addElement(newRec);
            recordsHT.put(newRec.getId(), newRec);
            countInserted ++;
            if (recordInsertionCallback != null) {
              if (insertedRecsV == null) insertedRecsV = new Vector();
              insertedRecsV.addElement(newRec);
            }
          }
        } 
        // if we don't want to keep it, see if we should remove it...
        else {
          if (rec != null) {
            if (removeRecordsV == null) removeRecordsV = new Vector();
            removeRecordsV.addElement(rec);
            countToDelete ++;
          }
        }
      }
      if (countInserted > 0 || countUpdated > 0) {
        // Can not fire event for specific row since sorter could shuffle it to another row -- fire entire data change.
        // We don't want the sorter to translate event rows and split row ranges.
        int size = recordsV.size();
        if (countInserted > 0) {
          // Always inserts are at the end
          if (trace != null) trace.info(10, "RecordTableModel.fireTableRowsInserted("+(size - countInserted) + ", " + (size - 1) + ");");
          fireTableRowsInserted(size - countInserted, size - 1); // don't fire structure changed -- it would screw up the header renderer
        }
        // No need to fire update if insert was already fired, event will convert to entire table data update anyway.
        else {
          if (trace != null) trace.info(20, "RecordTableModel.fireTableRowsUpdated(0, "+(size-1)+");");
          fireTableRowsUpdated(0, size -1); // don't fire structure changed -- it would screw up the header renderer
        }
      }
      // if somethind is to be removed/deleted, use another call to removeData()
      if (countToDelete > 0) {
        Record[] recs = new Record[removeRecordsV.size()];
        removeRecordsV.toArray(recs);
        removeData(recs);
      }
    }
    // Notify callback of inserted records
    if (recordInsertionCallback != null && insertedRecsV != null && insertedRecsV.size() > 0) {
      recordInsertionCallback.callback(insertedRecsV);
    }
    if (trace != null) trace.exit(RecordTableModel.class);
  }

  /**
   * Removes the specified records from the model.
   */
  public synchronized void removeData(Record[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableModel.class, "removeData(Record[] records)");
    if (trace != null) trace.args(records);

    int removeCount = 0;
    // records are compared with equals() by their ID.. so we can use generic Vector methods
    for (int i=0; i<records.length; i++ ) {
      boolean contains = recordsHT.containsKey(records[i].getId());
      if (contains) {

        if (removeCount == 0) {
          // As a preparation to delete rows, so that selection model can record the state before rows are deleted
          // fire an event that is a fake one, before real action is taken.
          if (trace != null) trace.data(10, "fire fake rows deleted event");
          fireTableRowsDeleted(-10,-10);
        }

        recordsV.removeElement(records[i]);
        recordsHT.remove(records[i].getId());
        removeCount ++;
      }
    }
    if (removeCount > 0) {
      // simulate reletions from the end.... to minimally confuse the selection list as
      // the rows are remapped by the sorter anyway
      int originalSize = recordsV.size() + removeCount;
      if (trace != null) trace.info(10, "RecordTableModel.fireTableRowsDeleted("+(originalSize - removeCount) + ", " +(originalSize - 1)+");");
      fireTableRowsDeleted(originalSize - removeCount, originalSize - 1); // to fix number of elements in SizeSequence

      // don't need to call updated, because when a row is deleted, the table sorter will call update for all rows
      //-//System.out.println("RecordTableModel.fireTableRowsUpdated(0, "+(size-1)+");");
      //-//fireTableRowsUpdated(0, recordsV.size() -1); // don't fire structure changed -- it would screw up the header renderer
    }

    if (trace != null) trace.exit(RecordTableModel.class);
  }

  /**
   * Removes all data from the model.
   */
  public synchronized void removeData() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableModel.class, "removeData()");

    int size = recordsV.size();
    if (size > 0) {
      recordsV.removeAllElements();
      recordsHT.clear();
      if (trace != null) trace.info(10, "RecordTableModel.fireTableRowsDeleted(0, "+(size-1)+");");
      fireTableRowsDeleted(0, size-1);
      //fireTableDataChanged();
    }

    if (trace != null) trace.exit(RecordTableModel.class);
  }

  public synchronized boolean keep(Record rec) {
    boolean keep = true;
    RecordFilter filter = getFilterCombined();
    if (filter != null) {
      keep = filter.keep(rec);
    }
    return keep;
  }

  public final Vector getRowVectorForViewOnly() {
    return recordsV;
  }

  public final ColumnHeaderData getColumnHeaderData() {
    return columnHeaderData;
  }
  public void updateHeaderDataFrom(JTable table) {
    updateHeaderDataFrom(table, false);
  }
  public void updateHeaderDataFrom(JTable table, boolean saveDefaultColumnSizes) {
    columnHeaderData.initFromTable(table, saveDefaultColumnSizes);
    fireTableStructureChanged();
    columnHeaderData.applyToTable(table);
  }
  public void updateHeaderDataFromTo(String visuals, JTable table) {
    updateHeaderDataFromTo(visuals, table, false);
  }
  public void updateHeaderDataFromTo(String visuals, JTable table, boolean useDefaultColumnSizes) {
    if (visuals != null) {
      try {
        columnHeaderData.initFromString(visuals);
      } catch (Throwable t) {
      }
    }
    fireTableStructureChanged();
    columnHeaderData.applyToTable(table, useDefaultColumnSizes);
  }




  /*******************************************
   ***    ParentFolderListener handling    ***
   *******************************************/

  public synchronized void addParentFolderListener(ParentFolderListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableModel.class, "addParentFolderListener(FolderParentListener l)");
    if (trace != null) trace.args(l);
    myListenerList.add(ParentFolderListener.class, l);
    if (trace != null) trace.exit(RecordTableModel.class);
  }

  public synchronized void removeParentFolderListener(ParentFolderListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableModel.class, "removeParentFolderListener(FolderParentListener l)");
    if (trace != null) trace.args(l);
    myListenerList.remove(ParentFolderListener.class, l);
    if (trace != null) trace.exit(RecordTableModel.class);
  }


  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   */
  protected void fireParentFolderChanged(FolderPair prevParent, FolderPair newParent) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableModel.class, "fireParentFolderChanged(FolderPair prevParent, FolderPair newParent)");
    if (trace != null) trace.args(prevParent, newParent);

    // Guaranteed to return a non-null array
    Object[] listeners = myListenerList.getListenerList();
    ParentFolderEvent e = null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length-2; i>=0; i-=2) {
      if (listeners[i] == ParentFolderListener.class) {
        // Lazily create the event:
        if (e == null)
          e = new ParentFolderEvent(this, prevParent, newParent);
        ((ParentFolderListener)listeners[i+1]).parentFolderChanged(e);
      }
    }

    if (trace != null) trace.exit(RecordTableModel.class);
  }
}
/*
* Copyright 2001-2012 by CryptoHeaven Corp.,
* Mississauga, Ontario, Canada.
* All rights reserved.
*
* This software is the confidential and proprietary information
* of CryptoHeaven Corp. ("Confidential Information").  You
* shall not disclose such Confidential Information and shall use
* it only in accordance with the terms of the license agreement
* you entered into with CryptoHeaven Corp.
*/

package com.CH_gui.table;

import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.records.FolderRecUtil;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.file.File_GetFiles_Rq;
import com.CH_co.service.msg.dataSets.msg.Msg_GetMsgs_Rq;
import com.CH_co.service.records.*;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_co.util.DisposableObj;
import com.CH_co.util.GlobalProperties;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.gui.JBottomStickViewport;
import com.CH_gui.msgTable.MsgTableModel;
import com.CH_gui.sortedTable.JSortedTable;
import com.CH_gui.sortedTable.TableModelSortEvent;
import com.CH_gui.sortedTable.TableModelSortListener;
import com.CH_gui.sortedTable.TableSorter;
import com.CH_gui.util.MiscGui;
import com.CH_gui.util.VisualsSavable;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.TableModel;

/** 
* <b>Copyright</b> &copy; 2001-2012
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
* Class Description:
*
*
* Class Details:
*
*
* <b>$Revision: 1.31 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class RecordTableScrollPane extends JScrollPane implements VisualsSavable, DisposableObj {

  private RecordTableModel recordTableModel;
  private JSortedTable jSTable;

  // List of record selection listeners, selection events are suppressed when sorting takes place.
  private EventListenerList recordSelectionListenerList = new EventListenerList();
  private boolean isSuspendedRecordSelectionEvents;
  private SortListener sortListener;
  private RecordListSelectionListener recordListSelectionListener;
  private Thread selectionUpdater;

  // larger component of which this scroll pane in an integral part of
  private JComponent areaComponent;

  private Thread silentValidator;
  private boolean isDisposed = false;

  // Used to enable auto-scroll correction if recently resized
  private long lastResizeStamp = 0;

  // Auto scroll suppression
  private boolean isAutoScrollSuppressed = false;

  /** Creates new RecordTableScrollPane */
  public RecordTableScrollPane(RecordTableModel recordTableModel) {
    this(recordTableModel, TableSorter.class);
  }
  /** Creates new RecordTableScrollPane */
  public RecordTableScrollPane(RecordTableModel recordTableModel, Class tableSorterClass) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableScrollPane.class, "RecordTableScrollPane(RecordTableModel recordTableModel, Class tableSorterClass)");
    if (trace != null) trace.args(recordTableModel);
    if (trace != null) trace.args(tableSorterClass);

    this.recordTableModel = recordTableModel;
    TableSorter tableSorter = null;
    try {
      tableSorter = (TableSorter) tableSorterClass.newInstance();
    } catch (Exception t) {
      if (trace != null) trace.exception(RecordTableScrollPane.class, 100, t);
      throw new IllegalArgumentException(t.getMessage());
    }

    tableSorter.setModel(recordTableModel);
    this.jSTable = new JSortedTable(tableSorter);
    this.jSTable.setCellSelectionEnabled(false);
    this.jSTable.setColumnSelectionAllowed(false);
    this.jSTable.setRowSelectionAllowed(true);
    this.jSTable.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
      public void columnAdded(TableColumnModelEvent e) {
        if (jSTable.getColumnCount() >= 2) {
          jSTable.configureEnclosingScrollPane();
        }
      }
      public void columnMarginChanged(ChangeEvent e) {
      }
      public void columnMoved(TableColumnModelEvent e) {
      }
      public void columnRemoved(TableColumnModelEvent e) {
        if (jSTable.getColumnCount() <= 1) {
          jSTable.unconfigureEnclosingScrollPane();
        }
      }
      public void columnSelectionChanged(ListSelectionEvent e) {
      }
    });
    this.sortListener = new SortListener();
    init();

    // Create a silent view updater that will try to re-fetch data after connection re-establishment or other
    // synchronizing events
    this.silentValidator = new SilentValidator();
    this.silentValidator.setDaemon(true);
    this.silentValidator.start();

    if (trace != null) trace.exit(RecordTableScrollPane.class);
  }

  protected JComponent getAreaComponent() {
    return areaComponent;
  }
  protected void setAreaComponent(JComponent areaComponent) {
    this.areaComponent = areaComponent;
  }

  public boolean isAutoScrollSuppressed() {
    return isAutoScrollSuppressed;
  }
  public void setAutoScrollSuppressed(boolean flag) {
    isAutoScrollSuppressed = flag;
  }

  public void setOpaqueTable(boolean opaque) {
    getViewport().setOpaque(opaque);
    setOpaque(opaque);
    setOpaqueRecurse(jSTable, opaque);
  }
  private void setOpaqueRecurse(JComponent c, boolean opaque) {
    if (c == null)
      return;
    c.setOpaque(opaque);
    Component[] cc = c.getComponents();
    if (cc != null) {
      for (int i=0; i<cc.length; i++) {
        if (cc[i] instanceof JComponent)
          setOpaqueRecurse((JComponent) cc[i], opaque);
      }
    }
  }

  private void init() {
    RecordTableCellRenderer renderer = recordTableModel.createRenderer();
    jSTable.setSelectionBackground(renderer.getAltBkColors()[RecordTableCellRenderer.ALT_BK_SELECTED_COLOR_I]);
    jSTable.setDefaultRenderer(Object.class, renderer);
    jSTable.setShowGrid(false);
    jSTable.getColumnModel().setColumnMargin(0);
    jSTable.addTableModelSortListener(sortListener);

    // Inform interested listeners about record selection changes when sorting is not in progress.
    recordListSelectionListener = new RecordListSelectionListener(sortListener);
    jSTable.getSelectionModel().addListSelectionListener(recordListSelectionListener);

    selectionUpdater = new SelectionUpdater();
    selectionUpdater.setDaemon(true);
    selectionUpdater.start();

    setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    setViewport(new JBottomStickViewport());

    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        lastResizeStamp = System.currentTimeMillis();
      }
    });
    getVerticalScrollBar().setUnitIncrement(16);
    getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
      private boolean isAutoScroll = false;
      public void adjustmentValueChanged(AdjustmentEvent e) {
        JScrollBar vBar = getVerticalScrollBar();
        if (e.getValueIsAdjusting() || vBar.getValueIsAdjusting()) {
          if (isAutoScroll) {
            isAutoScroll = false;
          }
        }
        if (!e.getValueIsAdjusting() && !vBar.getValueIsAdjusting()) {
          if (isAutoScroll) {
            if (Math.abs(System.currentTimeMillis() - lastResizeStamp) < 1000)
              vBar.setValue(vBar.getMaximum());
          } else {
            // if MAX is the same then this cannot change our autoscroll property
            isAutoScroll = vBar.getValue() > 0
                    &&  vBar.getVisibleAmount() < vBar.getMaximum()
                    // 15 pixels from the bottom of viewable component, not from the bottom of the adjustment bar
                    && vBar.getMaximum() - (vBar.getValue() + vBar.getVisibleAmount()) < 15;
            if (isAutoScroll) {
              // Snap to position
              if (Math.abs(System.currentTimeMillis() - lastResizeStamp) < 1000)
                vBar.setValue(vBar.getMaximum());
            }
          }
        }
      }
    });

    setViewportView(jSTable);
    getViewport().setBackground(jSTable.getBackground());
    setBorder(new EmptyBorder(0,0,0,0));

    restoreVisuals(GlobalProperties.getProperty(MiscGui.getVisualsKeyName(this)));
  }

  public RecordTableModel getTableModel() {
    return recordTableModel;
  }

  public JSortedTable getJSortedTable() {
    return jSTable;
  }

  /**
  * @return a single selected record, if there are multiple selected, return null
  */
  public Record getSelectedRecord() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableScrollPane.class, "getSelectedRecord()");

    Record record = null;
    if (jSTable.getSelectedRowCount() == 1) {
      int selectedRow = jSTable.getSelectedRow();
      record = recordTableModel.getRowObject(jSTable.convertMyRowIndexToModel(selectedRow));
    }

    if (trace != null) trace.exit(RecordTableScrollPane.class, record);
    return record;
  }

  /**
  * @return all selected records, if there are none selected, return null
  */
  public List getSelectedRecordsL() {
    return getSelectedRecordsL(false);
  }
  public List getSelectedRecordsL(boolean includeOlderVersions) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableScrollPane.class, "getSelectedRecordsL(boolean includeOlderVersions)");
    if (trace != null) trace.args(includeOlderVersions);

    ArrayList recordsL = new ArrayList();
    if (jSTable != null && jSTable.getSelectedRowCount() > 0) {
      int[] selectedRows = jSTable.getSelectedRows();
      for (int i=0; selectedRows!=null && i<selectedRows.length; i++) {
        Record rec = recordTableModel.getRowObject(jSTable.convertMyRowIndexToModel(selectedRows[i]));
        if (rec != null) {
          if (rec instanceof FileLinkRecord) {
            FileLinkRecord fLink = (FileLinkRecord) rec;
            if (includeOlderVersions)
              recordsL.addAll(recordTableModel.getAllVersions(fLink));
            else
              recordsL.add(rec);
          } else {
            recordsL.add(rec);
          }
        }
      }
    }

    if (trace != null) trace.exit(RecordTableScrollPane.class, recordsL);
    return recordsL;
  }

  /**
  * @return all selected records, if there are none selected, return null
  * Runtime instance of the array is of the contained records (ie: UserRecord[])
  */
  public Record[] getSelectedRecords() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableScrollPane.class, "getSelectedRecords()");

    Record[] records = null;
    List recordsL = getSelectedRecordsL();
    if (recordsL != null && recordsL.size() > 0) {
      try {
        records = (Record[]) Array.newInstance(recordsL.get(0).getClass(), recordsL.size());
        recordsL.toArray(records);
      } catch (ArrayStoreException x) {
        records = new Record[recordsL.size()];
        recordsL.toArray(records);
      }
    }

    if (trace != null) trace.exit(RecordTableScrollPane.class, records);
    return records;
  }

  public void setSelectedRecords(Long[] recordIDs, Class classType, boolean scrollToVisible) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableScrollPane.class, "setSelectedRecords(Long[] recordIDs, Class classType, boolean scrollToVisible)");

    if (recordIDs != null) {
      boolean firstSelected = false;
      for (int i=0; i<recordIDs.length; i++) {
        Long recId = recordIDs[i];
        int row = recordTableModel.getRowForObject(recId, classType);
        if (row > -1) {
          int viewRow = jSTable.convertMyRowIndexToView(row);
          if (viewRow > -1) {
            if (!firstSelected) {
              firstSelected = true;
              jSTable.getSelectionModel().setSelectionInterval(viewRow, viewRow);
              if (scrollToVisible)
                jSTable.scrollRectToVisible(jSTable.getCellRect(viewRow, 0, true));
            } else {
              jSTable.getSelectionModel().addSelectionInterval(viewRow, viewRow);
            }
          }
        }
      }
    }

    if (trace != null) trace.exit(RecordTableScrollPane.class);
  }


  public boolean advanceSelectionNext() {
    return advanceSelection(true, true, null, null, -1, false);
  }

  public boolean advanceSelectionPrevious() {
    return advanceSelection(false, true, null, null, -1, false);
  }

  /**
  * Advances record selection in the table in directions next or previous (down or up)
  * @param isNext true for next, flase for previous
  * @param skipOverCurrentSelection true if currently selected records are not to be selected next
  * @param skipOverRecs additional records that are to be skipped when considering next selection
  * @param startFrom consider starting position to be from this given record
  * @param startFromViewIndex consider starting position to be from this view index (-1 for none)
  * @param isSimulation true if only consider advancing selection without actually doing so
  * @return true if selection could be advanced
  */
  public boolean advanceSelection(boolean isNext, boolean skipOverCurrentSelection, Record[] skipOverRecs, Record startFrom, int startFromViewIndex, boolean isSimulation) {
    boolean selectionAdvanced = false;
    if (startFromViewIndex <= -1) {
      if (startFrom != null) {
        int row = recordTableModel.getRowForObject(startFrom.getId());
        if (row >= 0)
          startFromViewIndex = jSTable.convertMyRowIndexToView(row);
        else
          startFromViewIndex = -1;
      } else {
        startFromViewIndex = jSTable.getSelectedRow();
      }
    }
    if (startFromViewIndex >= 0 || (startFromViewIndex == -1 && isNext)) {
      if (skipOverCurrentSelection) {
        Record[] currentSelectionRecs = getSelectedRecords();
        if ((currentSelectionRecs != null && currentSelectionRecs.length > 0) || (skipOverRecs != null && skipOverRecs.length > 0)) {
          skipOverRecs = RecordUtils.concatinate(currentSelectionRecs, skipOverRecs);
        }
      }
      int increment = isNext ? 1 : -1;
      int nextViewIndex = startFromViewIndex;
      while (true) {
        nextViewIndex += increment;
        if (nextViewIndex < 0 || nextViewIndex >= recordTableModel.getRowCount()) {
          break;
        }
        boolean isSkip = false;
        if (skipOverRecs != null && skipOverRecs.length > 0) {
          Record next = recordTableModel.getRowObject(jSTable.convertMyRowIndexToModel(nextViewIndex));
          if (next != null) {
            isSkip = RecordUtils.find(skipOverRecs, next.getId()) != null;
          }
        }
        if (!isSkip) {
          if (!isSimulation) {
            ListSelectionModel selectionModel = jSTable.getSelectionModel();
            selectionModel.setAnchorSelectionIndex(nextViewIndex);
            selectionModel.setSelectionInterval(nextViewIndex, nextViewIndex);
          }
          selectionAdvanced = true;
          break;
        }
      } // end while
    }
    return selectionAdvanced;
  }

  private class SortListener implements TableModelSortListener {
    private RecordTableSelection selection;

    public synchronized void preSortDeleteNotify(TableModelSortEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "preSortDeleteNotify(TableModelSortEvent event)");
      if (trace != null) trace.args(event);
      selection = RecordTableSelection.getData(RecordTableScrollPane.this);
      if (trace != null) trace.exit(getClass());
    }
    public synchronized void preSortNotify(TableModelSortEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "preSortNotify(TableModelSortEvent event)");
      if (trace != null) trace.args(event);
      if (selection == null)
        selection = RecordTableSelection.getData(RecordTableScrollPane.this);
      if (trace != null) trace.exit(getClass());
    }
    public synchronized void postSortNotify(TableModelSortEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "postSortNotify(TableModelSortEvent event)");
      if (trace != null) trace.args(event);
      if (isSortingInProgress()) {
        //recordTableModel.columnHeaderData.initFromTable(jSTable);
        // Repaint the header to fix the sorting arrows.
        jSTable.getTableHeader().repaint();
        // Restore selected records.
        selection.restoreData(RecordTableScrollPane.this);
        endSorting();
        // repaint the rows so the new data order is visible
        repaint();
      }
      if (trace != null) trace.exit(getClass());
    }
    private synchronized boolean isSortingInProgress() {
      boolean rc = selection != null;
      return rc;
    }
    private synchronized void endSorting() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "endSorting()");
      selection = null;
      if (trace != null) trace.exit(getClass());
    }
  } // end class SortListener



  /*************************************
  *** Record Selection Notifications ***
  *************************************/
  public void addRecordSelectionListener(RecordSelectionListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableScrollPane.class, "addRecordSelectionListener(RecordSelectionListener l)");
    if (trace != null) trace.args(l);
    recordSelectionListenerList.add(RecordSelectionListener.class, l);
    if (trace != null) trace.exit(RecordTableScrollPane.class);
  }

  public void setSuspendedRecordSelectionEvents(boolean flag) {
    isSuspendedRecordSelectionEvents = flag;
  }

  public void removeRecordSelectionListener(RecordSelectionListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableScrollPane.class, "removeRecordSelectionListener(RecordSelectionListener l)");
    if (trace != null) trace.args(l);
    recordSelectionListenerList.remove(RecordSelectionListener.class, l);
    if (trace != null) trace.exit(RecordTableScrollPane.class);
  }

  public void removeRecordSelectionListeners() {
    RecordSelectionListener[] listeners = (RecordSelectionListener[]) recordSelectionListenerList.getListeners(RecordSelectionListener.class);
    if (listeners != null && listeners.length > 0)
      for (int i=0; i<listeners.length; i++)
        recordSelectionListenerList.remove(RecordSelectionListener.class, listeners[i]);
  }


  /**
  * Notify all listeners that have registered interest for
  * notification on this event type.
  */
  public void fireRecordSelectionChanged() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableScrollPane.class, "fireRecordSelectionChanged()");

    if (!isSuspendedRecordSelectionEvents) {
      Record[] selectedRecords = getSelectedRecords();
      if (trace != null) trace.info(10, selectedRecords);

      // Guaranteed to return a non-null array
      Object[] listeners = recordSelectionListenerList.getListenerList();
      RecordSelectionEvent e = null;
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
        if (listeners[i] == RecordSelectionListener.class) {
          // Lazily create the event:
          if (e == null) e = new RecordSelectionEvent(this, selectedRecords);
          RecordSelectionListener l = (RecordSelectionListener) listeners[i+1];
          l.recordSelectionChanged(e);
        }
      }
    }

    if (trace != null) trace.exit(RecordTableScrollPane.class);
  }


  private class SelectionUpdater extends ThreadTraced {
    private Record lastNotifyRecord;
    public SelectionUpdater() {
      super("Selection Updater");
    }
    public void runTraced() {
      while (true) {
        try {
          Thread.sleep(1000);
          if (isDisposed || isInterrupted()) break;
          doWork();
        } catch (InterruptedException e) {
          break;
        } catch (Throwable t) {
        }
      }
    }
    private void doWork() {
      if (!sortListener.isSortingInProgress()) {
        int selectedRowCount = jSTable.getSelectedRowCount();
        if (selectedRowCount == 0) {
          if (lastNotifyRecord != null) {
            lastNotifyRecord = null;
            fireRecordSelectionChanged();
          }
        } else if (selectedRowCount == 1) {
          Record rec = recordTableModel.getRowObjectNoTrace(jSTable.convertMyRowIndexToModel(jSTable.getSelectedRow()));
          if (rec != null && !rec.equals(lastNotifyRecord)) {
            lastNotifyRecord = rec;
            fireRecordSelectionChanged();
          }
        }
      }
    }
  }


  /**
  * Worker that checks in the background for possible invalidated GUI that require refresh
  */
  private class SilentValidator extends ThreadTraced {
    private SilentValidator() {
      super("Silent Validator");
    }
    public void runTraced() {
      while (true) {
        try {
          Thread.sleep(1000 + new java.util.Random().nextInt(1000)); // 1 + 1 seconds random delay
          if (isDisposed || isInterrupted()) break;
          doWork();
        } catch (InterruptedException e) {
          break;
        } catch (Throwable t) {
        }
      }
    }
    private RecordTableComponent getParentRecordTableComponent(Component c) {
      RecordTableComponent recordTableComponent = null;
      Container cont = c.getParent();
      if (cont != null) {
        while (true) {
          if (cont == null)
            break;
          else if (cont instanceof RecordTableComponent) {
            recordTableComponent = (RecordTableComponent) cont;
            break;
          }
          cont = cont.getParent();
        }
      }
      return recordTableComponent;
    }
    private void doWork() {
      // only do this if GUI is actually showing
      boolean isShowing = false;
      if (isShowing()) {
        isShowing = true;
      } else {
        RecordTableComponent recordTableComponent = getParentRecordTableComponent(RecordTableScrollPane.this);
        isShowing = recordTableComponent != null && recordTableComponent.isShowing();
      }
      if (isShowing) {
        FolderPair folderPair = recordTableModel.getParentFolderPair();
        FolderRecord folder = folderPair != null ? folderPair.getFolderRecord() : null;
        Long folderId = folder != null ? folder.folderId : null;
        boolean viewInvalidated = FolderRecUtil.wasFolderViewInvalidated(folderId);
        if (folder != null && viewInvalidated) {
          ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
          if (SIL.hasPersistentMainWorker()) {
            if (folder.isFileType() || folder.isRecycleType()) {
              FolderRecUtil.markFolderViewInvalidated(folderId, false);
              File_GetFiles_Rq request = new File_GetFiles_Rq(folderPair.getFolderShareRecord().shareId, Record.RECORD_TYPE_FOLDER, folderId, (short) -File_GetFiles_Rq.FETCH_NUM_LIST__INITIAL_SIZE, (Timestamp) null);
              int action = CommandCodes.FILE_Q_GET_FILES_STAGED;
              SIL.submitAndReturn(new MessageAction(action, request), 30000);
            }
            if (folder.isMsgType() || folder.isRecycleType()) {
              FolderRecUtil.markFolderViewInvalidated(folderId, false);
              Msg_GetMsgs_Rq request = new Msg_GetMsgs_Rq(folderPair.getFolderShareRecord().shareId, Record.RECORD_TYPE_FOLDER, folderId, (short) -Msg_GetMsgs_Rq.FETCH_NUM_LIST__INITIAL_SIZE, (short) Msg_GetMsgs_Rq.FETCH_NUM_NEW__INITIAL_SIZE, (Timestamp) null);
              int messageMode = MsgTableModel.MODE_MSG; // default value in case this is a Recycle table
              if (recordTableModel instanceof MsgTableModel) {
                messageMode = ((MsgTableModel) recordTableModel).getMode();
              }
              int action = (messageMode == MsgTableModel.MODE_POST || messageMode == MsgTableModel.MODE_CHAT) ? CommandCodes.MSG_Q_GET_FULL : CommandCodes.MSG_Q_GET_BRIEFS;
              SIL.submitAndReturn(new MessageAction(action, request), 30000);
            }
          }
        }
      }
    }
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public String getVisuals() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableScrollPane.class, "getVisuals()");

    String visuals = null;

    recordTableModel.updateHeaderDataFrom(jSTable);
    visuals = recordTableModel.getColumnHeaderData().toString();

    if (trace != null) trace.exit(RecordTableScrollPane.class, visuals);
    return visuals;
  }

  public void restoreVisuals(String visuals) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableScrollPane.class, "restoreVisuals(String visuals)");
    if (trace != null) trace.args(visuals);

    try {
      // Restore headers.
      recordTableModel.updateHeaderDataFromTo(visuals, jSTable);
    } catch (Exception t) {
      if (trace != null) trace.exception(RecordTableScrollPane.class, 100, t);
      // reset the properties since they are corrupted
      GlobalProperties.resetMyAndGlobalProperties();
    }

    if (trace != null) trace.exit(RecordTableScrollPane.class);
  }
  /**
  * We cannot provide an extension depending on owner window because
  * when 'this' is instantiated, it doesn't yet have an owning window to restore settings for it.
  * @return null
  */
  public String getExtension() {
    return null;
  }
  public String getVisualsClassKeyName() {
    return null;
  }
  public Integer getVisualsVersion() {
    return null;
  }
  public boolean isVisible(Record rec) {
    boolean visible = false;
    try {
      int rowModel = recordTableModel.getRowForObject(rec.getId());
      int rowSorted = jSTable.convertMyRowIndexToView(rowModel);
      // This rectangle is relative to the table where the
      // northwest corner of cell (0,0) is always (0,0)
      Rectangle rect = jSTable.getCellRect(rowSorted, 0, true);
      // The location of the viewport relative to the table
      Point pt = viewport.getViewPosition();
      // Translate the cell location so that it is relative
      // to the view, assuming the northwest corner of the
      // view is (0,0)
      rect.setLocation(rect.x-pt.x, rect.y-pt.y);
      // Check if view completely contains cell
      visible = new Rectangle(viewport.getExtentSize()).contains(rect);
    } catch (Throwable t) {
    }
    return visible;
  }
  public boolean isVisuallyTraversable() {
    return true;
  }


  private class RecordListSelectionListener implements ListSelectionListener {
    private SortListener sListener;
    private RecordListSelectionListener(SortListener sListener) {
      this.sListener = sListener;
    }
    public void valueChanged(ListSelectionEvent event) {
      if (!event.getValueIsAdjusting() && !sListener.isSortingInProgress())
        fireRecordSelectionChanged();
    }
  }


  /**
  ****  I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
  * Dispose the object and release resources to help in garbage collection.
  */
  public void disposeObj() {
    isDisposed = true;
    if (silentValidator != null) {
      try { silentValidator.interrupt(); } catch (Throwable t) { }
      silentValidator = null;
    }
    if (selectionUpdater != null) {
      try { selectionUpdater.interrupt(); } catch (Throwable t) { }
      selectionUpdater = null;
    }
    TableModel rawModel = jSTable.getRawModel();
    if (rawModel instanceof RecordTableModel)
      ((RecordTableModel) rawModel).setAutoUpdate(false);
    if (sortListener != null) {
      jSTable.removeTableModelSortListener(sortListener);
      sortListener = null;
    }
    if (recordListSelectionListener != null) {
      jSTable.getSelectionModel().removeListSelectionListener(recordListSelectionListener);
      recordListSelectionListener = null;
    }
    jSTable.disposeObj();
    if (recordSelectionListenerList != null)
      removeRecordSelectionListeners();
  }

}
/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.sortedTable;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.*;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.dialog.*;
import com.CH_gui.fileTable.*;
import com.CH_gui.frame.*;
import com.CH_gui.gui.*;
import com.CH_gui.msgTable.*;
import com.CH_gui.table.*;
import com.CH_gui.util.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * Class Description:
 *  JSortedTable wraps around its DefaultTableModel a sorter -- TableSorter
 *
 * Class Details:
 *  It can be either constructed with a TableSorter or DefaultTableModel which will
 *  be wraped anyways with the TableSorter
 *
 * <b>$Revision: 1.29 $</b>
 *
 * @author  Marcin Kurzawa
 */

public class JSortedTable extends JTable implements DisposableObj {

  private static final boolean DISPLAY_SECONDARY_SORT_ORDER_ICON = false;

  TableHeaderSorter tableHeaderSorter = null;

  /** Creates new JSortedTable */
  public JSortedTable(TableModel model) {
    this(new TableSorter(model));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JSortedTable.class, "JSortedTable(DefaultTableModel)");
    if (trace != null) trace.args(model);
    if (trace != null) trace.exit(JSortedTable.class);
  }

  /** Creates new JSortedTable */
  public JSortedTable(TableSorter sorter) {
    super(sorter);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JSortedTable.class, "JSortedTable(TableSorter)");
    if (trace != null) trace.args(sorter);

    // Cause JTable to initialize to use variable row heights.
    setRowHeight(21);
    setRowHeight(1, getRowHeight()+2);
    setBackground(Color.white);
    setBorder(new EmptyBorder(0,0,0,0));

    //TableHeaderSorter.install(sorter, this);
    tableHeaderSorter = new TableHeaderSorter();
    getTableHeader().addMouseListener(tableHeaderSorter);
    getTableHeader().setDefaultRenderer(new TableHeaderRenderer());

    addMouseListener(new MouseAdapter() {
      private long lastPopupTriggerTime;
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger())
          lastPopupTriggerTime = System.currentTimeMillis();
      }
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger())
          lastPopupTriggerTime = System.currentTimeMillis();
      }
      /**
       * Dispatch from mouseClicked, because from mouseReleased the chat http link clicks don't work
       */
      public void mouseClicked(MouseEvent e) {
        if (!e.isPopupTrigger() && Math.abs(System.currentTimeMillis() - lastPopupTriggerTime) > 1000)
          mouseClickNoPopup(e);
      }
      private void mouseClickNoPopup(MouseEvent e) {
        JSortedTable sTable = JSortedTable.this;
        int viewRow = rowAtPoint(e.getPoint());
        int viewColumn = columnAtPoint(e.getPoint());
        int modelColumn = sTable.convertColumnIndexToModel(viewColumn);
        Rectangle cellRect = sTable.getCellRect(viewRow, viewColumn, false);
        int cellX = e.getPoint().x-cellRect.x;
        int cellY = e.getPoint().y-cellRect.y;
        TableCellRenderer renderer = getCellRenderer(viewRow, viewColumn);
        Object value = sTable.getValueAt(viewRow, viewColumn);
        Component component = renderer.getTableCellRendererComponent(sTable, value, false, false, viewRow, modelColumn);
        if (component instanceof HTML_ClickablePane) {
          MouseEvent event = new MouseEvent(component, e.getID(), e.getWhen(), e.getModifiers(), cellX, cellY, e.getClickCount(), e.isPopupTrigger());
          ((HTML_ClickablePane) component).processMouseEvent(event);
        }

        TableModel tableModel = sTable.getRawModel();

        // if clicked at attachment (1) icon or star (2) icon then activate it
        if (tableModel instanceof MsgTableModel) {
          MsgTableModel mtm = (MsgTableModel) tableModel;
          int rawColumn = mtm.getColumnHeaderData().convertColumnToRawModel(modelColumn);
          if (rawColumn == 1 || rawColumn == 2) {
            int rawRow = sTable.convertMyRowIndexToModel(viewRow);
            MsgLinkRecord msgLink = (MsgLinkRecord) mtm.getRowObject(rawRow);
            if (msgLink != null) {
              if (rawColumn == 1) {
                // attachments column
                FetchedDataCache cache = FetchedDataCache.getSingleInstance();
                MsgDataRecord msgData = cache.getMsgDataRecord(msgLink.msgId);
                if (msgData != null) {
                  int numOfAttachments = msgData.getAttachmentCount(true);
                  if (numOfAttachments > 0) {
                    Window w = SwingUtilities.windowForComponent(sTable);
                    if (w instanceof Frame) new SaveAttachmentsDialog((Frame) w, new MsgLinkRecord[] { msgLink });
                    else if (w instanceof Dialog) new SaveAttachmentsDialog((Dialog) w, new MsgLinkRecord[] { msgLink });
                  }
                }
              } else if (rawColumn == 2) {
                // flag/star column
                MsgActionTable.markStarred(new MsgLinkRecord[] { msgLink }, !msgLink.isStarred());
              }
            }
          }
          // See if we should remove the red flag from posting/chatting (open content) folder tables, mark as Read, or fetch Read stats
          if (mtm.isModeMsgBody()) {
            int rawRow = sTable.convertMyRowIndexToModel(viewRow);
            MsgLinkRecord msgLink = (MsgLinkRecord) mtm.getRowObject(rawRow);
            if (msgLink != null) {
              ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
              StatOps.markOldIfNeeded(SIL, msgLink.msgLinkId, FetchedDataCache.STAT_TYPE_INDEX_MESSAGE);
              MsgLinkOps.markRecordsAs(SIL, new MsgLinkRecord[] { msgLink }, Short.valueOf(StatRecord.FLAG_READ));
              StatOps.fetchStatsIfNotRecentlyRequested(SIL, msgLink);
            }
          }
        }

        // if clicked at star icon then activate it
        if (tableModel instanceof FileTableModel) {
          FileTableModel ftm = (FileTableModel) tableModel;
          int rawColumn = ftm.getColumnHeaderData().convertColumnToRawModel(modelColumn);
          if (rawColumn == 0) {
            // flag/star column
            int rawRow = sTable.convertMyRowIndexToModel(viewRow);
            FileRecord fileRec = (FileRecord) ftm.getRowObject(rawRow);
            if (fileRec instanceof FileLinkRecord) {
              FileLinkRecord fileLink = (FileLinkRecord) fileRec;
              if (!ftm.getIsCollapseFileVersions()) {
                FileActionTable.markStarred(new FileLinkRecord[] { fileLink }, !fileLink.isStarred());
              } else {
                Collection c = ftm.getAllVersions(fileLink);
                if (c != null && c.size() > 0) {
                  FileLinkRecord[] fileLinks = (FileLinkRecord[]) ArrayUtils.toArray(c, FileLinkRecord.class);
                  FileActionTable.markStarred(fileLinks, !fileLink.isStarred());
                }
              }
            }
          }
        }
      }
    });
    if (trace != null) trace.exit(JSortedTable.class);
  }

  protected void processMouseEvent(MouseEvent e) {
    super.processMouseEvent(e);
  }
  
  /**
   * Shortcut method to add a sort listener to the TableSorter.
   */
  public void addTableModelSortListener(TableModelSortListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JSortedTable.class, "addTableModelSortListener(TableModelSortListener l)");
    if (trace != null) trace.args(l);
    TableSorter sorter = (TableSorter) getModel();
    sorter.addTableModelSortListener(l);
    if (trace != null) trace.exit(JSortedTable.class);
  }
  /**
   * Shortcut method to remove a sort listener from the TableSorter.
   */
  public void removeTableModelSortListener(TableModelSortListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JSortedTable.class, "removeTableModelSortListener(TableModelSortListener l)");
    if (trace != null) trace.args(l);
    TableSorter sorter = (TableSorter) getModel();
    sorter.removeTableModelSortListener(l);
    if (trace != null) trace.exit(JSortedTable.class);
  }

  public void tableChanged(TableModelEvent e) {
    if (e == null || e.getFirstRow() == -2) {
      // no-op
    } else {
      super.tableChanged(e);
    }
  }

  /** 
   * Set the viewable model in the TableSorter 
   */
  public void setModel(TableModel model) {
    if (model instanceof TableSorter)
      super.setModel(model);
    else
      ((TableSorter) getModel()).setModel(model);
  }

  /**
   * @return a model which is a sorter -- viewable model 
   */
  public TableModel getModel() {
    TableModel rc = super.getModel();
    if (rc != null && (!(rc instanceof TableSorter)))
      throw new IllegalStateException("Returned model should be of TableSorter instance.");
    return rc;
  }

  /**
   * Set the raw data model 
   */
  public void setRawModel(TableModel model) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JSortedTable.class, "setRawModel(TableModel)");
    if (trace != null) trace.args(model);
    ((TableSorter) getModel()).setModel(model);
    if (trace != null) trace.exit(JSortedTable.class);
  }

  /**
   * @return a raw model, i.e. original data model -- not sorted 
   */
  public TableModel getRawModel() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JSortedTable.class, "getRawModel()");
    TableSorter ts = (TableSorter) getModel();
    TableModel rc;
    if (ts == null)
      rc = null;
    else
      rc = ts.getRawModel();
    if (trace != null) trace.exit(JSortedTable.class, rc);
    return rc;
  }

  /** Set column headers with names from <code> headers </code> in the table model
    * and then set renderers for the headers
    */
  public void setColumnHeaders (String [] headers, DefaultTableModel model) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JSortedTable.class, "addColumnNames(Object[], DefaultTableModel)");
    if (trace != null) trace.args(headers, model);

    /* Set the Strings first */
    for (int i = 0; i < headers.length; i++) {
      model.addColumn(headers[i]);
    }

    if (trace != null) trace.exit(JSortedTable.class);
  }

  /** @return an array of selected row objects from the table
    * The raw model is assumed to be DefaultTableModel!!!
    * Viewed rows are converted to model rows and returned
    */
  public Object[] getSelectedRowObjects() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JSortedTable.class, "getSelectedRowObjects()");

    Vector data = ((DefaultTableModel)this.getRawModel()).getDataVector();
    int[] selectedRows = this.getSelectedRows();
    Vector v = new Vector();

    for (int row = 0; row < data.size(); row++) {

      if (ArrayUtils.find(selectedRows, row) != -1) {   
        int modelRow = convertMyRowIndexToModel(row);
        Object curr = data.elementAt(modelRow);
        v.add(curr);
      }
    }
    Object[] selected = new Object[v.size()];
    v.toArray(selected);

    if (trace != null) trace.exit(JSortedTable.class, selected);
    return selected;
  }

   /** @return an index of a <code> column </code> in the table */
  public static int getColumnIndex(TableModel tableModel, String column) {

    int index = 0;
    for (int i = 0; i < tableModel.getColumnCount(); i++) {
      if (column.equals(tableModel.getColumnName(i))) {
        index = i;
        break;
      }
    }
    return index;
  } 

  /**
   * Return the index of the row in the model whose data is being displayed in the row viewRowIndex in the display. 
   */
  public int convertMyRowIndexToModel(int viewRowIndex) {
    //Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JSortedTable.class, "convertMyRowIndexToModel(int viewRowIndex)");
    //if (trace != null) trace.args(viewRowIndex);
    TableSorter sorter = (TableSorter) getModel();
    int rc = sorter.convertMyRowIndexToModel(viewRowIndex);
    //if (trace != null) trace.exit(JSortedTable.class, rc);
    return rc;
  }

  /**
   * Return the index of the row in the view which is displaying the data from the column modelRowIndex in the model. 
   */
  public int convertMyRowIndexToView(int modelRowIndex) {
    //Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JSortedTable.class, "convertMyRowIndexToView(int modelRowIndex)");
    //if (trace != null) trace.args(modelRowIndex);
    TableSorter sorter = (TableSorter) getModel();
    int rc = sorter.convertMyRowIndexToView(modelRowIndex);
    //if (trace != null) trace.exit(JSortedTable.class, rc);
    return rc;
  }


  /**
   * I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
   * Dispose the object and release resources to help in garbage collection.
   */
  public void disposeObj() {
    TableSorter sorter = (TableSorter) getModel();
    if (sorter != null)
      sorter.removeTableModelSortListeners();

    TableModel rawModel = getRawModel();
    if (rawModel != null && sorter != null)
      rawModel.removeTableModelListener(sorter);

    if (tableHeaderSorter != null) {
      getTableHeader().removeMouseListener(tableHeaderSorter);
      tableHeaderSorter = null;
    }
  }


  /**********************************************************************************************/
  /**************************** Column Header Renderer ******************************************/
  /**********************************************************************************************/

  /** Renders a column header, it adds an arrow icon to the column by which the table is sorted
    * and to previous sorted column.  The arrow is up or down depending if the order is ascending
    * or descending
    */
  private class TableHeaderRenderer extends MyDefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table,
                                               Object value,
                                               boolean isSelected,
                                               boolean hasFocus,
                                               int row,
                                               int column) 
    {
      ImageIcon columnIcon = null;
      String columnName = value != null ? value.toString() : null;
      String columnToolTip = null;
      ImageIcon sortIcon = null;
      String sortToolTip = null;

      {
        int sortIconIndex;
        TableSorter sorter = (TableSorter) getModel();
        int modelColumn = table.convertColumnIndexToModel(column);
        int rawColumn = modelColumn;
        TableModel rawModel = getRawModel();
        if (rawModel instanceof RecordTableModel) {
          rawColumn = ((RecordTableModel) rawModel).getColumnHeaderData().convertColumnToRawModel(modelColumn);
        }

        int sortColumnIndex = sorter.getSortingColumnIndex(rawColumn);
        boolean ascending = sorter.getSortingColumnDirection(rawColumn) > 0 ? true : false;
        String order = ascending ? com.CH_cl.lang.Lang.rb.getString("sort_ascending") : com.CH_cl.lang.Lang.rb.getString("sort_descending");

        /* sorted column */
        if (sortColumnIndex == 0) {
          sortIconIndex = (ascending) ? ImageNums.ORDER_ASCENDING2 : ImageNums.ORDER_DESCENDING2;
          sortIcon = Images.get(sortIconIndex);
          sortToolTip = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("sort_Primary_sort_column,_sorted_in_{0}_order."), new Object[] {order});
        }
        /* last sorted column */
        else if (sortColumnIndex == 1) {
          if (DISPLAY_SECONDARY_SORT_ORDER_ICON) {
            sortIconIndex = (ascending) ? ImageNums.ORDER_ASCENDING : ImageNums.ORDER_DESCENDING;
            sortIcon = Images.get(sortIconIndex);
          }
          sortToolTip = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("sort_Secondary_sort_column,_sorted_in_{0}_order."), new Object[] {order});
        }
      }

      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      panel.setBorder(UIManager.getBorder("TableHeader.cellBorder"));

      if (table instanceof JSortedTable) {
        JSortedTable sTable = (JSortedTable) table;
        TableModel tModel = sTable.getRawModel();
        if (tModel instanceof RecordTableModel) {
          RecordTableModel rtModel = (RecordTableModel) tModel;
          ColumnHeaderData columnHeaderData = rtModel.getColumnHeaderData();
          int rawColumn = columnHeaderData.convertColumnToRawModel(table.convertColumnIndexToModel(column));
          columnIcon = columnHeaderData.getRawColumnIcon(rawColumn);
          columnToolTip = columnHeaderData.getRawColumnTooltip(rawColumn);
        }
      }

      JLabel columnLabel = new JMyLabel(columnName);
      columnLabel.setToolTipText(columnToolTip);
      columnLabel.setIcon(columnIcon);
      columnLabel.setHorizontalAlignment(JLabel.LEFT);
      columnLabel.setVerticalAlignment(JLabel.TOP);
      if (columnName != null && columnName.trim().length() > 0)
        columnLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
      else 
        columnLabel.setBorder(new EmptyBorder(0, 0, 0, 0));

      panel.add(columnLabel);

      if (sortIcon != null) {
        JLabel sortLabel = new JMyLabel(sortIcon);
        sortLabel.setToolTipText(sortToolTip);
        sortLabel.setHorizontalAlignment(JLabel.LEFT);
        sortLabel.setVerticalAlignment(JLabel.TOP);
        sortLabel.setBorder(new EmptyBorder(0, 3, 0, 0));
        panel.add(sortLabel);
      }

      if (columnToolTip != null || sortToolTip != null) {
        String combinedToolTip = "<html>";
        if (columnToolTip != null)
          combinedToolTip += columnToolTip;
        if (sortToolTip != null) {
          if (columnToolTip != null)
            combinedToolTip += "<br>";
          combinedToolTip += sortToolTip;
        }
        panel.setToolTipText(combinedToolTip);
      }

      return panel;
    }
  }


  /**********************************************************************************************/
  /****************************  Column Header Sorter  ******************************************/
  /**********************************************************************************************/
  /**
   * Mouse listener to sort the columns when clicked on table headers.
   */
  private class TableHeaderSorter extends MouseAdapter {
    public void mouseClicked(MouseEvent mouseEvent) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TableHeaderSorter.class, "mouseClicked(MouseEvent)");
      if (!mouseEvent.isConsumed()) {
        TableColumnModel columnModel = JSortedTable.this.getColumnModel();
        if (columnModel == null) {
          if (trace != null) trace.exit(TableHeaderSorter.class);
          return;
        }
        int viewColumn = columnModel.getColumnIndexAtX(mouseEvent.getX());
        int column = JSortedTable.this.convertColumnIndexToModel(viewColumn);

        if (mouseEvent.getClickCount() == 1 && column != -1) {
          if (trace != null) trace.data(30, "SORTING . . .");
          boolean shiftPressed = (mouseEvent.getModifiers() & InputEvent.SHIFT_MASK) == 0;
          boolean controlPressed = (mouseEvent.getModifiers() & InputEvent.CTRL_MASK) == 0;

          TableSorter sorter = (TableSorter) getModel();
          TableModel rawModel = getRawModel();
          Vector columnsV = new Vector();
          // Convert to raw column index
          if (rawModel instanceof RecordTableModel) {
            RecordTableModel rtModel = (RecordTableModel) rawModel;
            if (rtModel instanceof MsgTableModel)
              ((MsgTableModel) rtModel).clearMsgPostRenderingCache();
            column = rtModel.getColumnHeaderData().convertColumnToRawModel(column);
            FolderPair fPair = rtModel.getParentFolderPair();
            // Special case for Posting/Chatting tables:
            // If clicked on 'Posting' column and 'From' or 'Sent' columns are incorporated, then use them as primary order.
            if (fPair != null && column == MsgTableModel.COLUMN_INDEX__POSTING &&
                  ( fPair.getFolderRecord().folderType.shortValue() == FolderRecord.POSTING_FOLDER ||
                    fPair.getFolderRecord().folderType.shortValue() == FolderRecord.CHATTING_FOLDER
                  )
               )
            {
              int modelColumnIndex_From = rtModel.getColumnHeaderData().convertRawColumnToModel(MsgTableModel.COLUMN_INDEX__FROM);
              int modelColumnIndex_Sent = rtModel.getColumnHeaderData().convertRawColumnToModel(MsgTableModel.COLUMN_INDEX__SENT);
              int viewColumnIndex_From = JSortedTable.this.convertColumnIndexToView(modelColumnIndex_From);
              int viewColumnIndex_Sent = JSortedTable.this.convertColumnIndexToView(modelColumnIndex_Sent);
              // in order columns appear when combined: 1) From, 2) Sent, 3) Posting
              if (viewColumnIndex_From < 0)
                columnsV.addElement(new Integer(MsgTableModel.COLUMN_INDEX__FROM));
              if (viewColumnIndex_Sent < 0)
                columnsV.addElement(new Integer(MsgTableModel.COLUMN_INDEX__SENT));
              columnsV.addElement(new Integer(MsgTableModel.COLUMN_INDEX__POSTING));
            }
          }
          Integer[] columns = null;
          // If using special case combined column sorting, or just the clicked upon single column:
          if (columnsV.size() > 0) {
            columns = (Integer[]) ArrayUtils.toArray(columnsV, Integer.class);
          } else {
            columns = new Integer[] { new Integer(column) };
          }
          // Shift and click
          if ( (shiftPressed && !controlPressed) || (!shiftPressed && controlPressed) ) {
            sorter.sortByColumns(columns, shiftPressed);
          } else {
            // Click only
            sorter.sortByColumns(columns);
          }
          // Fix the Sort Column Name and Direction radio buttons.
          fixSortRadioButtonSelection();
        }
        mouseEvent.consume();
      }
      if (trace != null) trace.exit(TableHeaderSorter.class);
    }
  }



  public void fixSortRadioButtonSelection() {
    RecordTableModel model = (RecordTableModel) getRawModel();
    // some tables do not have actions assigned and there may be nothing to fix...
    if (model.sortButtonGroup != null) {
      TableSorter sorter = (TableSorter) getModel();

      int column = sorter.getPrimarySortingColumn();
      int dir = sorter.getPrimarySortingDirection();

      String columnShortName = model.getColumnHeaderData().getRawColumnShortName(column);
      // Forward the enumeration to the desired column to fix its selection.
      Enumeration enm = model.sortButtonGroup.getElements();
      while (enm.hasMoreElements()) {
        AbstractButton button = (AbstractButton) enm.nextElement();
        String str = button.getText();
        if (columnShortName.equals(str)) {
          if (!button.isSelected()) {
            button.setSelected(true);
          }
          break;
        }
      }
      // Fix the Ascending/Descending radio buttons.
      Enumeration dirButtons = model.sortDirButtonGroup.getElements();
      AbstractButton dirButton = null;
      if (dirButtons.hasMoreElements()) {
        dirButton = (AbstractButton) dirButtons.nextElement();
        if (dir < 0)
          dirButton = (AbstractButton) dirButtons.nextElement();
        if (!dirButton.isSelected()) {
          dirButton.setSelected(true);
        }
      }
    }
  } // end fixSortRadioButtonSelection()


  /**
   * Overload function to avoid displaying headers if there is only 1 column.
   */
  public void configureEnclosingScrollPane() {
    JScrollPane jsp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
    if (jsp != null) {
      if (tableHeader != null && getColumnCount() > 1) {
        jsp.setColumnHeaderView(tableHeader);
      } else {
        jsp.setColumnHeaderView(null);
      }
    }
  }

  /**
   * Overload function to avoid displaying headers if there is only 1 column.
   */
  public void unconfigureEnclosingScrollPane() {
    JScrollPane jsp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
    if (jsp != null) {
      jsp.setColumnHeaderView(null);
    }
  }

}
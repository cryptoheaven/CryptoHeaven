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

import com.CH_cl.service.cache.CacheUsrUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.event.ContactRecordEvent;
import com.CH_cl.service.cache.event.ContactRecordListener;
import com.CH_cl.service.cache.event.FolderRecordEvent;
import com.CH_cl.service.cache.event.FolderRecordListener;
import com.CH_cl.service.ops.FolderOps;
import com.CH_co.monitor.Stats;
import com.CH_co.service.records.*;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_co.util.DisposableObj;
import com.CH_co.util.ImageNums;
import com.CH_co.util.SingleTokenArbiter;
import com.CH_co.util.Sounds;
import com.CH_gui.action.AbstractActionTraced;
import com.CH_gui.action.Actions;
import com.CH_gui.actionGui.JActionButton;
import com.CH_gui.actionGui.JActionFrame;
import com.CH_gui.contactTable.ChatSessionCreator;
import com.CH_gui.contactTable.ContactActionTable;
import com.CH_gui.dialog.CustomizeColumnsDialog;
import com.CH_gui.frame.MsgTableStarterFrame;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.menuing.PopupMouseAdapter;
import com.CH_gui.msgTable.MsgTableModel;
import com.CH_gui.service.records.ContactRecUtil;
import com.CH_gui.sortedTable.JSortedTable;
import com.CH_gui.sortedTable.TableSorter;
import com.CH_gui.tree.FolderActionTree;
import com.CH_gui.util.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

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
* <b>$Revision: 1.34 $</b>
* @author  Marcin Kurzawa
* @version
*/
public abstract class RecordActionTable extends RecordTableScrollPane implements ActionProducerI, DisposableObj {

  private RecordTableModelListener tableModelListener;
  private static final ButtonGroup sharedAscDescButtonGroup = new ButtonGroup();
  private static final ButtonGroup sharedSortColumnButtonGroup = new ButtonGroup();

  private boolean enabledFolderNewItemsNotify = true; // enabled by default.

  // For listening on folder updates so we can act when new stuff comes.
  private FolderListener folderListener;
  // For listening on contact updates so we can visually notify users.
  private ContactListener contactListener;
  // Keep history of update counts per folders to see in appropriate act on updates.
  private Hashtable folderUpdateHistoryHT;
//  // When users go offline, issue tokens for the dialogs to suppress multiple dialogs
//  // for a single contact, before the shown dialog is dismissed.
//  // NON-STATIC!! because we want one dialog per each chat table inside a frame.
//  private SingleTokenArbiter offlineDialogArbiter;
  // STATIC because we want common notification per user for all chat windows
  private static SingleTokenArbiter offlineDialogArbiter;

  private HashSet dropTargetHS = new HashSet();
  private HashSet componentsForDNDHS = new HashSet();
  private HashSet componentsForPopupHS = new HashSet();


  /** Creates new RecordActionTable */
  public RecordActionTable(RecordTableModel recordTableModel) {
    this(recordTableModel, TableSorter.class);
  }
  public RecordActionTable(RecordTableModel recordTableModel, Class tableSorterClass) {
    super(recordTableModel, tableSorterClass);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordActionTable.class, "RecordActionTable(RecordTableModel recordTableModel, Class tableSorterClass)");
    if (trace != null) trace.args(recordTableModel);
    if (trace != null) trace.args(tableSorterClass);

    final JSortedTable jSTable = getJSortedTable();
    /** If right mouse button is clicked then the popup is shown. */
    addPopup(jSTable);

    /** If right mouse button is clicked then the node is being selected. */
    if (trace != null) trace.data(10, "adding mouse listener to record action table", this);
    jSTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent mouseEvent) {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "mouseClicked(MouseEvent mouseEvent)");
        if (trace != null) trace.args(mouseEvent);
        boolean rightButton = SwingUtilities.isRightMouseButton(mouseEvent);
        if (trace != null) trace.data(10, "rightButton", rightButton);
        boolean doubleClick = mouseEvent.getClickCount() == 2;
        if (trace != null) trace.data(11, "doubleClick", doubleClick);
        if (rightButton || doubleClick) {
          int row = jSTable.rowAtPoint(mouseEvent.getPoint());
          if (trace != null) trace.data(12, "row", row);
          if (row >= 0 && !jSTable.isRowSelected(row)) {
            if (trace != null) trace.data(13, "setRowSelectionInterval");
            jSTable.setRowSelectionInterval(row, row);
          }
          if (doubleClick) {
            Action action = getDoubleClickAction();
            if (trace != null) trace.data(14, "action", action);
            if (action != null) {
              action.actionPerformed(new ActionEvent(mouseEvent.getSource(), mouseEvent.getID(), "doubleClick"));
            }
          }
        }
        if (trace != null) trace.exit(getClass());
      }
    }); // end addMouseListener() to table

    addPopup(this);
    addPopup(getViewport());

    // tree selection listener used to enable/disable certain actions.
    if (trace != null) trace.data(20, "adding list selection listener to record action table", this);
    jSTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      /** Called whenever the value of the selection changes. */
      public void valueChanged(ListSelectionEvent e) {
        // reset stat size
        Stats.setSizeBytes(-1);
        setEnabledActions();
      }
    });

    recordTableModel.addTableModelListener(tableModelListener = new RecordTableModelListener());

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    // Register folder listener to act on folder updates (expand branches where new items become available).
    this.folderListener = new FolderListener();
    cache.addFolderRecordListener(folderListener);
    // Register contact listener to act on updates for visual notification purposes.
    this.contactListener = new ContactListener();
    cache.addContactRecordListener(contactListener);

    if (trace != null) trace.exit(RecordActionTable.class);
  }

  /**
  * Enable notification of new content in this tables folder on the frame's title bar.
  */
  public void setEnabledFolderNewItemsNotify(boolean b) {
    if (enabledFolderNewItemsNotify && !b) {
      if (folderListener != null) {
        FetchedDataCache.getSingleInstance().removeFolderRecordListener(folderListener);
        folderListener = null;
      }
    } else if (!enabledFolderNewItemsNotify && b) {
      folderListener = new FolderListener();
      FetchedDataCache.getSingleInstance().addFolderRecordListener(folderListener);
    }
    enabledFolderNewItemsNotify = b;
  }

  protected void setAreaComponent(JComponent areaComponent) {
    if (getAreaComponent() == null && areaComponent != null) {
      Component[] components = MiscGui.getComponentsRecursively(areaComponent);
      if (components != null) {
        for (int i=0; i<components.length; i++) {
          Component c = components[i];
          addPopup(c);
          addDND(components[i], false);
        }
      }
    }
    super.setAreaComponent(areaComponent);
  }
  private void addPopup(Component c) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordActionTable.class, "addPopup(Component c)");
    if (trace != null) trace.args(c);
    if (!componentsForPopupHS.contains(c)) {
      c.addMouseListener(new PopupMouseAdapter(c, this));
      componentsForPopupHS.add(c);
    }
    if (trace != null) trace.exit(RecordActionTable.class);
  }
  public void addDND(Component c) {
    addDND(c, true);
  }
  private void addDND(Component c, boolean includeDrag) {
    if (!componentsForDNDHS.contains(c)) {
      componentsForDNDHS.add(c);
      DropTargetListener dropTargetListener = createDropTargetListener();
      if (dropTargetListener != null) {
        dropTargetHS.add(new DropTarget(c, dropTargetListener));
      }
      if (includeDrag) {
        DragGestureListener dragGestureListener = createDragGestureListener();
        if (dragGestureListener != null) {
          DragSource dragSource = DragSource.getDefaultDragSource();
          dragSource.createDefaultDragGestureRecognizer(c, DnDConstants.ACTION_COPY_OR_MOVE, dragGestureListener);
        }
      }
    }
  }
  abstract public DragGestureListener createDragGestureListener();
  abstract public DropTargetListener createDropTargetListener();


  /** @return all the acitons that this objects produces.
  */
  public abstract Action[] getActions();

  public Action getRefreshAction() {
    return null;
  }
  public Action getCloneAction() {
    return null;
  }
  public Action getDoubleClickAction() {
    return null;
  }
  public Action getSplitLayoutAction() {
    return null;
  }
  public Action getFilterAction() {
    return null;
  }


  public HashSet getDropTargetHS() {
    return dropTargetHS;
  }
  public HashSet getComponentsForDNDHS() {
    return componentsForDNDHS;
  }
  public HashSet getComponentsForPopupHS() {
    return componentsForPopupHS;
  }


  /** Final Action Producers will not be traversed to collect its containing objects' actions.
  * @return true (because this object will gather all actions from its childeren or hide them counciously).
  */
  public boolean isFinalActionProducer() {
    return true;
  }

  /**
  * Determine is action is from the popup menu, ie: not menu nor shortcut.
  */
  public static boolean isActionActivatedFromPopup(ActionEvent event) {
    boolean fromMenu = false;
    boolean fromPopup = false;
    Object source = event.getSource();
    if (source instanceof JActionButton) {
      // from Toolbar button
      fromMenu = false;
      fromPopup = false;
    } else if (source instanceof HTML_ClickablePane) {
      // from HTML document / template
      fromMenu = false;
      fromPopup = false;
    } else {
      Object prev = null;
      while (source != null && (source instanceof JPopupMenu || source instanceof JMenuItem)) {
        prev = source;
        source = ((Container)source).getParent();
        if (source == null && prev instanceof JPopupMenu)
          source = ((JPopupMenu) prev).getInvoker();
      }
      fromMenu = source instanceof JMenuBar;
      fromPopup = !fromMenu;
    }
    return fromPopup;
  }


  /** Enables or Disables actions based on the current state of the Action Producing component.
  */
  public abstract void setEnabledActions();


  private class RecordTableModelListener implements TableModelListener {
    public void tableChanged(TableModelEvent e) {
      setEnabledActions();
    }
  }


  public class IterateNextAction extends AbstractActionTraced {
    public IterateNextAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Next"), Images.get(ImageNums.GO_NEXT16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("action_Next"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.GO_NEXT24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Next"));
      putValue(Actions.IN_MENU, Boolean.FALSE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      pressedNext();
    }
  }
  public boolean pressedNext() {
    boolean selected = advanceSelectionNext();
    setEnabledActions();
    return selected;
  }


  public class IteratePrevAction extends AbstractActionTraced {
    public IteratePrevAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Previous"), Images.get(ImageNums.GO_PREV16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("action_Previous"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.GO_PREV24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Previous"));
      putValue(Actions.IN_MENU, Boolean.FALSE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      pressedPrev();
    }
  }
  public boolean pressedPrev() {
    boolean selected = advanceSelectionPrevious();
    setEnabledActions();
    return selected;
  }


  /**
  * Toggle the split layout between horizontal and vertical.
  */
  public class SplitLayoutAction extends AbstractActionTraced {
    private boolean initialized = false;
    private int switchPercentageMin;
    private int switchPercentageMax;
    public SplitLayoutAction(int actionId, int switchPercentageMin, int switchPercentageMax) {
      super("Split Left-Right", Images.get(ImageNums.SPLIT_LEFT_RIGHT16));
      this.switchPercentageMin = switchPercentageMin;
      this.switchPercentageMax = switchPercentageMax;
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, "Split Left-Right");
      putValue(Actions.IN_MENU, Boolean.FALSE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
      init();
    }
    public void actionPerformedTraced(ActionEvent event) {
      toggleLayout();
      updateColumns();
      updateTextAndIcon();
      // store the changes so that new frames will take layout from this table
      JSplitPane splitPane = MiscGui.getParentSplitPane(RecordActionTable.this);
      if (splitPane != null) {
        MiscGui.storeVisualsSavable(splitPane);
      }
    }
    public void init() {
      if (!initialized) {
        initialized = true;
        updateColumns();
        updateTextAndIcon();
      }
    }
    public void toggleLayout() {
      JSplitPane splitPane = MiscGui.getParentSplitPane(RecordActionTable.this);
      if (splitPane != null) {
        // save column sizes so we can go back again to same sizes
        RecordActionTable.this.getTableModel().getColumnHeaderData().initFromTable(getJSortedTable(), true);
        int orientation = splitPane.getOrientation();
        int divLocation = splitPane.getDividerLocation();
        int spSize = 0;
        if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
          spSize = splitPane.getSize().width;
          splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        } else if (orientation == JSplitPane.VERTICAL_SPLIT) {
          spSize = splitPane.getSize().height;
          splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        }
        if (divLocation > 1 && spSize > 0) { // if not fully collapsed
          double dividerLocation = (double) divLocation + (double) splitPane.getDividerSize() / 2.0;
          double proportionalLocation = dividerLocation / (double) spSize;
          proportionalLocation = Math.min(switchPercentageMax/100.0, Math.max(switchPercentageMin/100.0, proportionalLocation)); // should not be outside specified bounds
          splitPane.setDividerLocation(proportionalLocation);
        }
        // Trigger selection event so that preview component can re-layout itself
        RecordActionTable.this.fireRecordSelectionChanged();
      }
    }
    public void updateColumns() {
      JSplitPane splitPane = MiscGui.getParentSplitPane(RecordActionTable.this);
      if (splitPane != null) {
        RecordTableModel recordTableModel = getTableModel();
        ColumnHeaderData data = recordTableModel.getColumnHeaderData();
        recordTableModel.updateHeaderDataFrom(getJSortedTable());
        int orientation = splitPane.getOrientation();
        if (orientation == JSplitPane.VERTICAL_SPLIT) {
          if (!Arrays.equals(data.data[ColumnHeaderData.I_VIEWABLE_SEQUENCE], data.data[ColumnHeaderData.I_VIEWABLE_SEQUENCE_DEFAULT_LONG])) {
            data.data[ColumnHeaderData.I_VIEWABLE_SEQUENCE] = data.data[ColumnHeaderData.I_VIEWABLE_SEQUENCE_DEFAULT_LONG];
            recordTableModel.updateHeaderDataFromTo(null, getJSortedTable(), true);
          }
        } else if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
          if (!Arrays.equals(data.data[ColumnHeaderData.I_VIEWABLE_SEQUENCE], data.data[ColumnHeaderData.I_VIEWABLE_SEQUENCE_DEFAULT_SHORT])) {
            data.data[ColumnHeaderData.I_VIEWABLE_SEQUENCE] = data.data[ColumnHeaderData.I_VIEWABLE_SEQUENCE_DEFAULT_SHORT];
            recordTableModel.updateHeaderDataFromTo(null, getJSortedTable(), true);
          }
        }
      }
    }
    public void updateTextAndIcon() {
      JSplitPane splitPane = MiscGui.getParentSplitPane(RecordActionTable.this);
      if (splitPane != null) {
        int orientation = splitPane.getOrientation();
        if (orientation == JSplitPane.VERTICAL_SPLIT) {
          putValue(Actions.NAME, "Split Left-Right");
          putValue(Actions.TOOL_TIP, "Split Left-Right");
          putValue(Actions.MENU_ICON, Images.get(ImageNums.SPLIT_LEFT_RIGHT16));
        } else if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
          putValue(Actions.NAME, "Split Top-Bottom");
          putValue(Actions.TOOL_TIP, "Split Top-Bottom");
          putValue(Actions.MENU_ICON, Images.get(ImageNums.SPLIT_TOP_BOTTOM16));
        }
      }
    }
  }


  /**
  * Toggle Filter.
  */
  public static class FilterAction extends AbstractActionTraced {
    public FilterAction(int actionId) {
      super(com.CH_cl.lang.Lang.rb.getString("action_Search"), Images.get(ImageNums.FIND16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, "Find Messages and Files ...");
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FIND24));
      putValue(Actions.TOOL_NAME, com.CH_cl.lang.Lang.rb.getString("actionTool_Search"));
      putValue(Actions.STATE_CHECK, Boolean.FALSE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
    }
  }


  public class SortAscDescAction extends AbstractActionTraced {
    boolean isAscending;
    public SortAscDescAction(boolean isAscending, int actionId) {
      this(isAscending, actionId, sharedAscDescButtonGroup);
    }
    public SortAscDescAction(boolean isAscending, int actionId, ButtonGroup group) {
      super(isAscending ? com.CH_cl.lang.Lang.rb.getString("sort_Ascending") : com.CH_cl.lang.Lang.rb.getString("sort_Descending"));
      this.isAscending = isAscending;
      putValue(Actions.ACTION_ID, new Integer(actionId));
      // Set "selected" only if the primary sort column direction matches 'isAscending'
      int dir = getTableModel().getColumnHeaderData().getPrimarySortingDirection();
      boolean set = dir != 0 && isAscending == (1 == dir);
      putValue(Actions.SELECTED_RADIO, Boolean.valueOf(set));
      putValue(Actions.BUTTON_GROUP, group);
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
      getTableModel().setSortDirButtonGroup(group);
    }
    public void actionPerformedTraced(ActionEvent actionEvent) {
      JSortedTable jst = getJSortedTable();
      TableSorter ts = (TableSorter) jst.getModel();
      int column = ts.getPrimarySortingColumn();
      if (column >= 0) {
        ts.sortByColumn(column, isAscending);
      }
      fixSortRadioButtonSelection();
      RecordTableModel tableModel = getTableModel();
      if (tableModel instanceof MsgTableModel)
        ((MsgTableModel) tableModel).clearMsgPostRenderingCache();
    }
  } // end class SortAscDescAction


  public class SortByColumnAction extends AbstractActionTraced {
    int columnIndex;
    public SortByColumnAction(int columnIndex, int actionId) {
      this(columnIndex, actionId, sharedSortColumnButtonGroup);
    }
    public SortByColumnAction(int columnIndex, int actionId, ButtonGroup group) {
      super(getTableModel().getColumnHeaderData().getRawColumnShortName(columnIndex));
      if (getTableModel().getColumnHeaderData().getRawColumnShortName(columnIndex) == null)
        throw new IllegalArgumentException("Action does not exist, ignore it!");
      this.columnIndex = columnIndex;
      putValue(Actions.ACTION_ID, new Integer(actionId));
      // Set "selected" only if the primary sort column direction matches the 'columnIndex'
      int col = getTableModel().getColumnHeaderData().getPrimarySortingColumn();
      boolean set = col == columnIndex;
      putValue(Actions.SELECTED_RADIO, Boolean.valueOf(set));
      putValue(Actions.BUTTON_GROUP, group);
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
      getTableModel().setSortButtonGroup(group);
    }
    public void actionPerformedTraced(ActionEvent actionEvent) {
      JSortedTable jst = getJSortedTable();
      TableSorter ts = (TableSorter) jst.getModel();
      ts.sortByColumn(columnIndex);
      fixSortRadioButtonSelection();
      RecordTableModel tableModel = getTableModel();
      if (tableModel instanceof MsgTableModel)
        ((MsgTableModel) tableModel).clearMsgPostRenderingCache();
    }
  } // end class SortByColumnAction


  public class CustomizeColumnsAction extends AbstractActionTraced {
    public CustomizeColumnsAction(int actionId) {
      this(actionId, com.CH_cl.lang.Lang.rb.getString("action_Table_Columns_..."));
    }
    public CustomizeColumnsAction(int actionId, String actionName) {
      super(actionName);
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_cl.lang.Lang.rb.getString("actionTip_Select_columns_to_render_in_the_table."));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
      putValue(Actions.IN_POPUP, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent actionEvent) {
      CustomizeColumnsDialog d = null;
      RecordTableModel tableModel = getTableModel();
      ColumnHeaderData data = tableModel.getColumnHeaderData();

      // if user has moved (drag-drop) columns, update our model so that current state is reflected in the Customization dialog
      tableModel.updateHeaderDataFrom(getJSortedTable());

      Window w = SwingUtilities.windowForComponent(RecordActionTable.this);
      if (w instanceof Dialog)
        d = new CustomizeColumnsDialog((Dialog) w, data);
      else if (w instanceof Frame)
        d = new CustomizeColumnsDialog((Frame) w, data);
      if (d != null && d.pressedOk) {
        d.applyChoices(data);
        if (tableModel instanceof MsgTableModel)
          ((MsgTableModel) tableModel).clearMsgPostRenderingCache();
        getTableModel().updateHeaderDataFromTo(null, getJSortedTable());
      }
    }
  } // end class CustomizeColumnsAction

  public void fixSortRadioButtonSelection() {
    getJSortedTable().fixSortRadioButtonSelection();
  }

  /**
  * Remembers the folderId and associates it with a mark.
  * Useful to check if folder update count has changed since last marking.
  */
  private void setFolderUpdateMark(Long folderId, int mark) {
    if (folderUpdateHistoryHT == null) folderUpdateHistoryHT = new Hashtable();
    folderUpdateHistoryHT.put(folderId, new Integer(mark));
  }
  /**
  * @return last stored mark for given folder, or zero '0' is not stored at all.
  */
  private int getFolderUpdateMark(Long folderId) {
    Integer mark = null;
    if (folderUpdateHistoryHT != null)
      mark = (Integer) folderUpdateHistoryHT.get(folderId);
    return mark != null ? mark.intValue() : 0;
  }


  /** Listen on updates to the FolderRecords in the cache.
  * If the event happens, visually notify the user of new items.
  */
  private class FolderListener implements FolderRecordListener {
    public void folderRecordUpdated(FolderRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new FolderGUIUpdater(event));
    }
  }
  private class FolderGUIUpdater implements Runnable {
    private FolderRecordEvent event;
    public FolderGUIUpdater(FolderRecordEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderGUIUpdater.class, "FolderGUIUpdater(FolderRecordEvent event)");
      this.event = event;
      if (trace != null) trace.exit(FolderGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderGUIUpdater.class, "FolderGUIUpdater.run()");
      FolderRecord[] folderRecords = null;
      folderRecords = event.getFolderRecords();
      if (folderRecords != null) {
        FolderPair parentFolderPair = RecordActionTable.this.getTableModel().getParentFolderPair();
        FolderRecord parentFolder = parentFolderPair != null ? parentFolderPair.getFolderRecord() : null;
        for (int i=0; i<folderRecords.length; i++) {
          FolderRecord fRec = folderRecords[i];
          if (fRec.equals(parentFolder)) {
            int updates = fRec.getUpdateCount();
            if (updates > 0) {
              int lastMark = getFolderUpdateMark(fRec.folderId);
              if (updates > lastMark) { // if updates is less then who cares for deletions, nothing new after all
                Window w = SwingUtilities.windowForComponent(RecordActionTable.this);
                if (w instanceof JActionFrame) {
                  JActionFrame frame = (JActionFrame) w;
                  frame.triggerVisualUpdateNotificationRoll();
                }
              }
            }
            // set the last processed update count even if zero
            setFolderUpdateMark(fRec.folderId, updates);
          }
        } // end for
      }
      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(FolderGUIUpdater.class);
    }
  } // end class FolderGUIUpdater


  /** Listen on updates to the ContactRecords in the cache.
  * If the event happens, visually notify the user of new items.
  */
  private class ContactListener implements ContactRecordListener {
    public void contactRecordUpdated(ContactRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new ContactGUIUpdater(event));
    }
  }
  private class ContactGUIUpdater implements Runnable {
    private ContactRecordEvent event;
    public ContactGUIUpdater(ContactRecordEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactGUIUpdater.class, "ContactGUIUpdater(ContactRecordEvent event)");
      this.event = event;
      if (trace != null) trace.exit(ContactGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactGUIUpdater.class, "ContactGUIUpdater.run()");
      // When user goes offline and we are chatting with him, popup an information dialog.
      FolderPair parentPair = RecordActionTable.this.getTableModel().getParentFolderPair();
      FolderRecord parentFolder = parentPair != null ? parentPair.getFolderRecord() : null;
      ContactRecord[] cRecs = ((ContactRecordEvent) event).getContactRecords();
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      UserRecord userRecord = cache.getUserRecord();
      Long userId = cache.getMyUserId();
      boolean chattingFoldersGathered = false;
      FolderRecord[] chattingFolders = null;
      for (int i=0; i<cRecs.length; i++) {
        ContactRecord cRec = cRecs[i];
        // If notification capable contact.
        if (cRec.previousStatus != null && cRec.ownerUserId.equals(userId)) {
          // use my contact list only, not the reciprocal contacts
          String userName = ListRenderer.getRenderedText( CacheUsrUtils.convertUserIdToFamiliarUser(cRec.contactWithId, true, false) );
          // If STATUS pop-up notifications enabled
          if (userRecord != null && (userRecord.flags.longValue() & UserRecord.FLAG_USER_ONLINE_STATUS_POPUP) != 0) {
            // Popup slider for user "OFFLINE" notification
            // For pop-up offline-notification consider only contact updates with status transition from online to non-online
            if (ContactRecord.isOnlineStatus(cRec.previousStatus) &&
                cRec.status.shortValue() == ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED)
            {
              // if user went OFFLINE
              if (!chattingFoldersGathered) {
                //chattingFolders = cache.getFolderRecordsChatting();
                if (parentFolder != null && parentFolder.isChatting())
                  chattingFolders = new FolderRecord[] { parentFolder };
                chattingFoldersGathered = true;
              }

              if (chattingFolders != null && chattingFolders.length > 0) {
                FolderPair[] chatFolderPairs = FolderOps.getAllChatFolderPairsFromCache(cRec, chattingFolders);
                if (chatFolderPairs != null && chatFolderPairs.length > 0) {
                  for (int k=0; k<chatFolderPairs.length; k++) {
                    Window w = SwingUtilities.windowForComponent(RecordActionTable.this);
                    // If this chat table is showing in a frame...
                    if (w instanceof JActionFrame) {

                      boolean notifyEnabled = (cRec.permits.intValue() & ContactRecord.SETTING_DISABLE_AUDIBLE_STATUS_NOTIFY) == 0; // disable bit is blank
                      if (notifyEnabled) {
                        // About to show or arbiter-skip popup
                        if (offlineDialogArbiter == null) offlineDialogArbiter = new SingleTokenArbiter();
                        final Object key = "offline"+cRec.contactWithId;
                        final String msgUserName = userName;
                        Thread th = new ThreadTraced("Offline Popup Notifier") {
                          public void run() {
                            Object token = new Object();
                            if (offlineDialogArbiter.putToken(key, token)) {
                              String msg = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("msg_{0}_left_the_chat_area_and_went_Offline."), new Object[] {msgUserName});
                              PopupWindow.getSingleInstance().addForScrolling("<html><img src=\"images/"+ImageNums.getImageName(ImageNums.STATUS_OFFLINE16)+"\" height=\"16\" width=\"16\">&nbsp;"+msg, true);
                              Sounds.playAsynchronous(Sounds.OFFLINE);
                              try {
                                // wait for other chat scrolling events for the same user to skip over this arbiter
                                Thread.sleep(1000);
                              } catch (InterruptedException ex) {
                              }
                              offlineDialogArbiter.removeToken(key, token);
                            }
                          }
                        };
                        th.setDaemon(true);
                        th.start();
                      } // end notifyWithSound
                    }
                  } // end if for 'k'
                }
              } // end if any chatting folders
            } // end if user went OFFLINE
            // popup slider for user "ONLINE" notification
            if (cRec.previousStatus.shortValue() == ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED && ContactRecord.isOnlineStatus(cRec.status)) {
              boolean notifyEnabled = (cRec.permits.intValue() & ContactRecord.SETTING_DISABLE_AUDIBLE_STATUS_NOTIFY) == 0; // disable bit is blank
              if (notifyEnabled) {
                // About to show or arbiter-skip popup
                if (offlineDialogArbiter == null) offlineDialogArbiter = new SingleTokenArbiter();
                final Object key = "online"+cRec.contactWithId;
                final String msgUserName = userName;
                final int imageCode = ContactRecUtil.getStatusIconCode(cRec.status, cRec.ownerUserId);
                final ContactRecord _cRec = cRec;
                final boolean[] _isProcessed = new boolean[1];
                Thread th = new ThreadTraced("Online Popup Notifier") {
                  public void runTraced() {
                    Object token = new Object();
                    if (offlineDialogArbiter.putToken(key, token)) {
                      String msg = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("title_USER-NAME_came_online."), new Object[] {msgUserName});
                      String iconStr = ImageNums.getImageName(imageCode);
                      MouseListener clickListener = new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                          if (!_isProcessed[0]) {
                            _isProcessed[0] = true;
                            ContactActionTable.chatOrShareSpace(RecordActionTable.this, new MemberContactRecordI[] {_cRec}, true, true, (short) 0);
                          }
                        }
                      };
                      PopupWindow.getSingleInstance().addForScrolling("<html><img src=\"images/"+iconStr+"\" height=\"16\" width=\"16\">&nbsp;"+msg, true, clickListener);
                      try {
                        // wait for other chat scrolling events for the same user to skip over this arbiter
                        Thread.sleep(1000);
                      } catch (InterruptedException ex) {
                      }
                      offlineDialogArbiter.removeToken(key, token);
                    }
                  }
                };
                th.setDaemon(true);
                th.start();
              } // end notifyWithSound
            }
          } // end if STATUS pop-up notifications enabled
          // check if this record table has anything to do with the contact record
          boolean contactInvolved = false;
          if (RecordActionTable.this instanceof ContactActionTable)
            contactInvolved = true;
          else if (parentFolder != null) {
            FolderShareRecord[] shareRecs = cache.getFolderShareRecordsForFolder(parentFolder.folderId);
            if (shareRecs != null) {
              for (int z=0; z<shareRecs.length; z++) {
                if (shareRecs[z].ownerUserId.equals(cRec.contactWithId)) {
                  contactInvolved = true;
                  break;
                }
              }
            }
          }
          // Make an exception for MsgTableStarterFrame to show the notifications
          if (!contactInvolved && SwingUtilities.windowForComponent(RecordActionTable.this) instanceof MsgTableStarterFrame)
            contactInvolved = true;
          if (contactInvolved && cRec.previousStatus != null) {
            boolean notifyEnabled = (cRec.permits.intValue() & ContactRecord.SETTING_DISABLE_AUDIBLE_STATUS_NOTIFY) == 0; // disable bit is blank
            if (notifyEnabled) {
              // Window title visual notifications
              Window w = SwingUtilities.windowForComponent(RecordActionTable.this);
              if (w instanceof JActionFrame) {
                JActionFrame frame = (JActionFrame) w;
                String msg = null;
                if (ContactRecord.isOnlineStatus(cRec.previousStatus) &&
                    cRec.status.shortValue() == ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED) {
                      msg = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("title_USER-NAME_went_offline."), new Object[] {userName});
                } else if (cRec.previousStatus.shortValue() == ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED &&
                    ContactRecord.isOnlineStatus(cRec.status)) {
                      msg = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("title_USER-NAME_came_online."), new Object[] {userName});
                } else if (cRec.previousStatus.shortValue() == ContactRecord.STATUS_INITIATED &&
                    cRec.status.shortValue() == ContactRecord.STATUS_ACCEPTED) {
                      msg = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("title_Contact_with_USER-NAME_was_accepted."), new Object[] {userName});
                } else if (cRec.previousStatus.shortValue() == ContactRecord.STATUS_INITIATED &&
                    cRec.status.shortValue() == ContactRecord.STATUS_DECLINED) {
                      msg = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("title_Contact_with_USER-NAME_was_declined."), new Object[] {userName});
                }
                if (msg != null) {
                  String userStr = ListRenderer.getRenderedText(FetchedDataCache.getSingleInstance().getUserRecord());
                  frame.triggerVisualUpdateNotificationAnim(msg, userStr + " :: ", null, 7000);
                  Stats.setStatus(msg);
                }
              } // end if in JActionFrame
            } // end notifyEnabled
          } // end if contact involved
        } // end if notification capable contact
      } // end for 'i' all contact records


      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(ContactGUIUpdater.class);
    }
  } // end class ContactGUIUpdater

  /**
  * I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
  * Dispose the object and release resources to help in garbage collection.
  */
  public void disposeObj() {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    if (folderListener != null) {
      cache.removeFolderRecordListener(folderListener);
      folderListener = null;
    }
    if (contactListener != null) {
      cache.removeContactRecordListener(contactListener);
      contactListener = null;
    }
    // remove model's listeners in the cache
    getTableModel().setAutoUpdate(false);
    // remove our model listener
    if (tableModelListener != null) {
      getTableModel().removeTableModelListener(tableModelListener);
      tableModelListener = null;
    }
    Iterator iter = dropTargetHS.iterator();
    while (iter.hasNext()) {
      try {
        DropTarget target = (DropTarget) iter.next();
        if (target != null) {
          Component c = target.getComponent();
          if (c != null)
            c.setDropTarget(null);
          target.setComponent(null);
        }
      } catch (Throwable t) {
      }
    }
    dropTargetHS.clear();
    componentsForDNDHS.clear();
    componentsForPopupHS.clear();
    super.disposeObj();
  }
}
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

package com.CH_gui.chatTable;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.cache.event.*;

import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.actionGui.*;
import com.CH_gui.gui.*;
import com.CH_gui.list.*;
import com.CH_gui.msgTable.MsgActionTable;
import com.CH_gui.msgs.*;
import com.CH_gui.sortedTable.*;
import com.CH_gui.table.RecordTableModel;

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
public class ChatActionTable extends MsgActionTable implements DisposableObj {

  private static final int TYPING_NOTIFY_MILLIS = 6000; // 6 sec.
  private MsgTypingListener msgTypingListener;
  private MsgLinkListener msgLinkRecordListener;
  private TableModelSortListener sortListener = null;

  // used for auto-scrolling to the new chat message
  private MsgLinkRecord prevKeepRecord;
  private MsgLinkRecord lastKeepRecord;
  private MsgLinkRecord lastScrollToRecord;

  private MsgLinkRecord mostRecentMsgLink;

  /** Creates new ChatActionTable */
  public ChatActionTable(RecordTableModel model) {
    super(model);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ChatActionTable.class, "ChatActionTable(RecordTableModel model)");
    msgTypingListener = new ChatTypingListener();
    FetchedDataCache.getSingleInstance().addMsgTypingListener(msgTypingListener);
    // auto-scroll to new chat message...
    msgLinkRecordListener = new MsgLinkListener();
    FetchedDataCache.getSingleInstance().addMsgLinkRecordListener(msgLinkRecordListener);
//    // scrolling should happen after the table had a change to re-sort itself so the record finds its final row destination...
//    TableModel tModel = getJSortedTable().getModel();
//    if (tModel instanceof TableMap) {
////      TableMap map = (TableMap) tModel;
////      sortListener = new SortListener();
////      map.addTableModelSortListener(sortListener);
//    }
    // disable auto-scrolls in viewport since row heights are variable and it doesn't quite work with variable row heights
    JViewport view = getViewport();
    if (view instanceof JBottomStickViewport) {
      ((JBottomStickViewport) view).setAutoScrollEnabled(false);
    }
    // add a callback hook that will notify us when new items have been added
    getTableModel().recordInsertionCallback = new CallbackI() {
      public void callback(final Object value) {
        try {
          if (value != null && value instanceof java.util.List) {
            java.util.List valueList = (java.util.List) value;
            // pick the most recent element out of the vector
            MsgLinkRecord mostRecentInVector = null;
            for (int i=0; i<valueList.size(); i++) {
              Record rec = (Record) valueList.get(i);
              if (rec instanceof MsgLinkRecord) {
                MsgLinkRecord link = (MsgLinkRecord) rec;
                if (mostRecentInVector == null || mostRecentInVector.dateCreated.before(link.dateCreated))
                  mostRecentInVector = link;
              }
            }
            if (mostRecentInVector != null) {
              if (mostRecentMsgLink == null ||
                  getTableModel().getRowForObject(mostRecentMsgLink.getId()) < 0 ||
                  mostRecentMsgLink.dateCreated.before(mostRecentInVector.dateCreated))
              {
                mostRecentMsgLink = mostRecentInVector;
              }
              // if there ever was a link we scrolled to, bring it to view again
              if (mostRecentMsgLink != null) {
                // oddly without immediate scroll, scroll invoked later will fail with acuracy
                try {
                  JSortedTable jSTable = getJSortedTable();
                  int rowModel = getTableModel().getRowForObject(mostRecentMsgLink.getId());
                  int rowSorted = jSTable.convertMyRowIndexToView(rowModel);
                  Rectangle rect = jSTable.getCellRect(rowSorted, 0, true);
                  //System.out.println("callback: immediate: scroll to rect="+rect);
                  jSTable.scrollRectToVisible(rect);
                } catch (Throwable t) {
                }
                SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                    try {
                      JSortedTable jSTable = getJSortedTable();
                      int rowModel = getTableModel().getRowForObject(mostRecentMsgLink.getId());
                      int rowSorted = jSTable.convertMyRowIndexToView(rowModel);
                      Rectangle rect = jSTable.getCellRect(rowSorted, 0, true);
                      //System.out.println("callback: invoked  : scroll to rect="+rect);
                      jSTable.scrollRectToVisible(rect);
                    } catch (Throwable t) {
                    }
                  } // end run()
                }); // end Runnable
              }
            }
          }
        } catch (Throwable t) {
          t.printStackTrace();
        }

      } // end callback()
    }; // end CallbackI
    if (trace != null) trace.exit(ChatActionTable.class);
  }

  private class MsgLinkListener implements MsgLinkRecordListener {
    public void msgLinkRecordUpdated(MsgLinkRecordEvent event) {
      // Exec on event thread to avoid any GUI deadlocks
      javax.swing.SwingUtilities.invokeLater(new MsgGUIUpdater(event));
    }
  }
  
  private class MsgGUIUpdater implements Runnable {
    private MsgLinkRecordEvent e;
    public MsgGUIUpdater(MsgLinkRecordEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgGUIUpdater.class, "MsgGUIUpdater(MsgLinkRecordEvent event)");
      this.e = event;
      if (trace != null) trace.exit(MsgGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgGUIUpdater.class, "MsgGUIUpdater.run()");
      if (e.getEventType() == MsgLinkRecordEvent.SET) {
        // only auto-scroll if single message arrived, this means chat session is in progress and folder content is not fetched
        MsgLinkRecord[] msgLinks = e.getMsgLinkRecords();
        if (msgLinks != null && msgLinks.length == 1) {
          if (getTableModel().keep(msgLinks[0])) {
            lastKeepRecord = msgLinks[0];
          }
        } else if (msgLinks != null && msgLinks.length > 1) {
          lastKeepRecord = prevKeepRecord;
          if (!getTableModel().contains(lastKeepRecord)) {
            lastKeepRecord = msgLinks[0];
          }
        }
      }
      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(MsgGUIUpdater.class);
    }
  }

//  private class SortListener implements TableModelSortListener {
//    public void preSortDeleteNotify(TableModelSortEvent event) {
//    }
//    public void preSortNotify(TableModelSortEvent event) {
//      lastScrollToRecord = lastKeepRecord;
//      lastKeepRecord = null;
//    }
//    public void postSortNotify(TableModelSortEvent event) {
//      SwingUtilities.invokeLater(new Runnable() {
//        public void run() {
//          if (lastScrollToRecord != null) {
//            RecordTableModel model = getTableModel();
//            JSortedTable table = getJSortedTable();
//            table.doLayout();
//            int row = model.getRowForObject(lastScrollToRecord.msgLinkId);
//            if (row >= 0) {
//              int viewRow = table.convertMyRowIndexToView(row);
//              final JScrollBar sBar = getVerticalScrollBar();
//              if (viewRow == 0) {
//                int min = sBar.getMinimum();
//                sBar.setValue(min);
//              } else if (viewRow + 1 == getTableModel().getRowCount()) {
//                int max = sBar.getMaximum();
//                sBar.setValue(max);
//                //System.out.println("postSort: immediate: setValue="+max);
//                // try setting the value again as table has layout problems the first time
//                SwingUtilities.invokeLater(new Runnable() {
//                  public void run() {
//                    int max = sBar.getMaximum();
//                    sBar.setValue(max);
//                    //System.out.println("postSort: invoked  : setValue="+max);
//                  }
//                });
//              } else {
//                Rectangle rect = getJSortedTable().getCellRect(viewRow, 0, true);
//                table.scrollRectToVisible(rect);
//                //System.out.println("postSort: immediate: scroll to rect="+rect);
//              }
//            }
//            prevKeepRecord = lastScrollToRecord;
//            lastScrollToRecord = null;
//          }
//        }
//      });
//    }
//  }

  private class ChatTypingListener implements MsgTypingListener {
    public void msgTypingUpdate(EventObject event) {
      // Exec on event thread to avoid potential GUI deadlocks
      javax.swing.SwingUtilities.invokeLater(new ChatGUIUpdater(event));
    }
  }
  private class ChatGUIUpdater implements Runnable {
    private EventObject e;
    public ChatGUIUpdater(EventObject event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ChatGUIUpdater.class, "ChatGUIUpdater(EventObject event)");
      this.e = event;
      if (trace != null) trace.exit(ChatGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ChatGUIUpdater.class, "ChatGUIUpdater.run()");
      Object s = e.getSource();
      if (s instanceof Obj_List_Co) {
        Obj_List_Co o = (Obj_List_Co) s;
        Long userId = (Long) o.objs[0];
        Long folderId = (Long) o.objs[1];

        RecordTableModel rtm = ChatActionTable.this.getTableModel();
        if (rtm != null) {
          FolderPair fp = rtm.getParentFolderPair();
          if (fp != null) {
            if (fp.getId().equals(folderId)) {
              Window w = SwingUtilities.windowForComponent(ChatActionTable.this);
              if (w instanceof JActionFrame) {
                JActionFrame f = (JActionFrame) w;
                Record r = MsgPanelUtils.convertUserIdToFamiliarUser(userId, false, true);
                String name = ListRenderer.getRenderedText(r);
                String msg = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("USER-NAME_is_typing_a_message."), new Object[] {name});
                f.triggerVisualUpdateNotificationStill(msg, f.getTitle() + " :: ", null, TYPING_NOTIFY_MILLIS);
              }
            }
          }
        }
      }
      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(ChatGUIUpdater.class);
    }
  }


  /**
   * I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
   * Dispose the object and release resources to help in garbage collection.
  */
  public void disposeObj() {
    if (msgTypingListener != null) {
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      cache.removeMsgTypingListener(msgTypingListener);
      msgTypingListener = null;
    }
    if (sortListener != null) {
      TableModel tModel = getJSortedTable().getModel();
      if (tModel instanceof TableMap) {
        TableMap map = (TableMap) tModel;
        map.removeTableModelSortListener(sortListener);
      }
      sortListener = null;
    }
    if (msgLinkRecordListener != null) {
      FetchedDataCache.getSingleInstance().removeMsgLinkRecordListener(msgLinkRecordListener);
      msgLinkRecordListener = null;
    }
//    getTableModel().recordInsertionCallback = null;
    super.disposeObj();
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "ChatActionTable";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
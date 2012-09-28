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

package com.CH_gui.chatTable;

import com.CH_cl.service.cache.CacheUsrUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.event.MsgTypingListener;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.service.records.FolderPair;
import com.CH_co.service.records.MsgLinkRecord;
import com.CH_co.service.records.Record;
import com.CH_co.trace.Trace;
import com.CH_co.util.CallbackI;
import com.CH_co.util.DisposableObj;
import com.CH_gui.actionGui.JActionFrame;
import com.CH_gui.gui.JBottomStickViewport;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.msgTable.MsgActionTable;
import com.CH_gui.sortedTable.JSortedTable;
import com.CH_gui.sortedTable.TableMap;
import com.CH_gui.sortedTable.TableModelSortListener;
import com.CH_gui.table.RecordTableModel;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.EventObject;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
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
* <b>$Revision: 1.13 $</b>
* @author  Marcin Kurzawa
* @version 
*/
public class ChatActionTable extends MsgActionTable implements DisposableObj {

  public static final int TYPING_NOTIFY_MILLIS = 6000; // 6 sec.
  private MsgTypingListener msgTypingListener;
  private TableModelSortListener sortListener = null;

  private MsgLinkRecord mostRecentMsgLink;

  /** Creates new ChatActionTable */
  public ChatActionTable(RecordTableModel model) {
    super(model);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ChatActionTable.class, "ChatActionTable(RecordTableModel model)");
    msgTypingListener = new ChatTypingListener();
    FetchedDataCache.getSingleInstance().addMsgTypingListener(msgTypingListener);
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
            MsgLinkRecord mostRecentInList = null;
            for (int i=0; i<valueList.size(); i++) {
              Record rec = (Record) valueList.get(i);
              if (rec instanceof MsgLinkRecord) {
                MsgLinkRecord link = (MsgLinkRecord) rec;
                if (mostRecentInList == null || mostRecentInList.dateCreated.before(link.dateCreated))
                  mostRecentInList = link;
              }
            }
            if (mostRecentInList != null) {
              if (mostRecentMsgLink == null ||
                  getTableModel().getRowForObject(mostRecentMsgLink.getId()) < 0 ||
                  mostRecentMsgLink.dateCreated.before(mostRecentInList.dateCreated))
              {
                mostRecentMsgLink = mostRecentInList;
              }
              // if there ever was a link we scrolled to, bring it to view again
              if (mostRecentMsgLink != null) {
                if (!isAutoScrollSuppressed()) {
                  // oddly without immediate scroll, scroll invoked later will fail with acuracy
                  scrollToVisible(mostRecentMsgLink);
                  SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                      scrollToVisible(mostRecentMsgLink);
                    } // end run()
                  }); // end Runnable
                }
              }
            }
          }
        } catch (Throwable t) {
        }

      } // end callback()
    }; // end CallbackI
    if (trace != null) trace.exit(ChatActionTable.class);
  }

  public boolean isVisibleMostRecent() {
    return isVisible(mostRecentMsgLink);
  }

  public void scrollToMostRecent() {
    if (!isAutoScrollSuppressed())
      scrollToVisible(mostRecentMsgLink);
  }

  private void scrollToVisible(MsgLinkRecord msgLink) {
    try {
      JSortedTable jSTable = getJSortedTable();
      int rowModel = getTableModel().getRowForObject(msgLink.getId());
      int rowSorted = jSTable.convertMyRowIndexToView(rowModel);
      Rectangle rect = jSTable.getCellRect(rowSorted, 0, true);
      jSTable.scrollRectToVisible(rect);
    } catch (Throwable t) {
    }
  }

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
                // use my contact list only, not the reciprocal contacts
                Record r = CacheUsrUtils.convertUserIdToFamiliarUser(userId, true, false);
                String name = ListRenderer.getRenderedText(r);
                String msg = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("USER-NAME_is_typing_a_message."), new Object[] {name});
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
    getTableModel().recordInsertionCallback = null;
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
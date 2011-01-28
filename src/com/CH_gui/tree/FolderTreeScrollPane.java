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

package com.CH_gui.tree;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.cache.event.*;

import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.frame.*;
import com.CH_gui.util.*;

import java.awt.*;
import java.awt.dnd.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

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
 * <b>$Revision: 1.32 $</b>
 * @author  Marcin Kurzawa
 * @version
 */

public class FolderTreeScrollPane extends JScrollPane implements DisposableObj {

  private FolderTree tree;

  private FolderShareListener shareListener;
  private FolderListener folderListener;
  private FolderRingingListener folderRingListener;
  private UserListener userListener;
  private ContactListener contactListener;
  private MsgLinkListener msgLinkListener;

  private DropTarget dropTarget1;
  private DropTarget dropTarget2;


  /**
   * Creates new FolderTreeScrollPane
   * With Actions, No Filter, Auto Fetch.
   */
  public FolderTreeScrollPane() {
    this(new FolderActionTree(), true);
  }
  /**
   * Creates new FolderTreeScrollPane
   * With Actions, No Filter, Auto Fetch.
   */
  public FolderTreeScrollPane(RecordFilter filter) {
    this(new FolderActionTree(filter), true);
  }
  /**
   * Creates new FolderTreeScrollPane
   * No Filter, Auto Fetch.
   * @param withAction If true, action packed tree will be used, false for no action tree.
   */
  public FolderTreeScrollPane(boolean withAction) {
    this(withAction ? new FolderActionTree() : new FolderTree(), true);
  }
  /**
   * Creates new FolderTreeScrollPane
   * @param withAction If true, action packed tree will be used, false for no action tree.
   * @param filter Sets the tree to be filtered.
   */
  public FolderTreeScrollPane(boolean withAction, RecordFilter filter) {
    this(withAction ? new FolderActionTree(filter) : new FolderTree(filter), true);
  }
  /**
   * Creates new FolderTreeScrollPane
   * @param withAction If true, action packed tree will be used, false for no action tree.
   * @param filter Sets the tree to be filtered.
   * @param initialFolderPairs initial folders -- automated fetch is suppressed
   */
  public FolderTreeScrollPane(boolean withAction, RecordFilter filter, FolderPair[] initialFolderPairs) {
    this(withAction ? new FolderActionTree(filter, initialFolderPairs) : new FolderTree(filter, initialFolderPairs), false);
  }
  /**
   * Creates new FolderTreeScrollPane with a specified FolderTree.
   * @param tree The underlying folder tree in this pane.
   * @param autoFetch If true, request to fetch all folders is sent to the server.
   */
  public FolderTreeScrollPane(FolderTree tree, boolean autoFetch) {
    super(tree);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeScrollPane.class, "FolderTreeScrollPane()");

    this.tree = tree;

    shareListener = new FolderShareListener();
    folderListener = new FolderListener();
    folderRingListener = new FolderRingingListener();
    userListener = new UserListener();
    contactListener = new ContactListener();
    msgLinkListener = new MsgLinkListener();

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    cache.addFolderShareRecordListener(shareListener);
    cache.addFolderRecordListener(folderListener);
    cache.addFolderRingListener(folderRingListener);
    cache.addUserRecordListener(userListener);
    cache.addContactRecordListener(contactListener);
    cache.addMsgLinkRecordListener(msgLinkListener);

    /* request folders for the tree */
    if (autoFetch)
      fetchFolders();

    FolderDND_DropTargetListener dropListener = new FolderDND_DropTargetListener(tree);
    dropTarget1 = new DropTarget(tree, dropListener);
    dropTarget2 = new DropTarget(getViewport(), dropListener);
    DragSource dragSource = DragSource.getDefaultDragSource();
    FolderDND_DragGestureListener dragGestureListener = new FolderDND_DragGestureListener(tree);
    dragSource.createDefaultDragGestureRecognizer(tree, DnDConstants.ACTION_MOVE, dragGestureListener);
    dragSource.createDefaultDragGestureRecognizer(getViewport(), DnDConstants.ACTION_MOVE, dragGestureListener);

    setDoubleBuffered(false);
    setBorder(new EmptyBorder(0,0,0,0));

    if (trace != null) trace.exit(FolderTreeScrollPane.class);
  }

  /**
   * @return the content tree
   */
  public FolderTree getFolderTree() {
    return tree;
  }

  /** Send a request to fetch folders from the server **/
  public void fetchFolders() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeScrollPane.class, "fetchFolders()");

    MessageAction msgAction = new MessageAction(CommandCodes.FLD_Q_GET_FOLDERS);
    MainFrame.getServerInterfaceLayer().submitAndReturn(msgAction);

    if (trace != null) trace.exit(FolderTreeScrollPane.class);
  }

  /** Set <code> records </code> in the tree.
    * if records do not exist, add them,
    * else update the existing ones
    */
  private void setFoldersInTree(FolderPair[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeScrollPane.class, "setFolderInTree(FolderPair[])");
    if (trace != null) trace.args(records);
    if (records != null) {
      tree.suppressSelection(true);
      FolderTreeSelectionExpansion selectionExpansion = FolderTreeSelectionExpansion.getData(tree);
      FolderTreeModelCl treeModel = tree.getFolderTreeModel();
      treeModel.addNodes(records);
      selectionExpansion.restoreData(tree);
      tree.suppressSelection(false);
    }
    if (trace != null) trace.exit(FolderTreeScrollPane.class);
  }

  /** Remove <code> records </code> from the tree if they exist there **/
  private void removeFoldersFromTree(FolderRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeScrollPane.class, "removeFoldersFromTree(FolderRecord[])");
    if (trace != null) trace.args(records);
    if (records != null && records.length > 0) {
      FolderTreeSelectionExpansion selectionExpansion = FolderTreeSelectionExpansion.getData(tree);
      // Remove specified records but keep their children if they still exist in the cache.
      boolean keepCacheResidantChildren = true;
      tree.getFolderTreeModel().removeRecords(records, keepCacheResidantChildren);
      selectionExpansion.restoreData(tree);
    }
    if (trace != null) trace.exit(FolderTreeScrollPane.class);
  }


  /** Listen on updates to the FolderShareRecords in the cache.
   *
   * If the event happens, set or remove shares
   */
  private class FolderShareListener implements FolderShareRecordListener {
    public void folderShareRecordUpdated(FolderShareRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new FolderGUIUpdater(event));
    }
  }

  /** Listen on updates to the FolderRecords in the cache.
   * If the event happens, set or remove records
   */
  private class FolderListener implements FolderRecordListener {
    public void folderRecordUpdated(FolderRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new FolderGUIUpdater(event));
    }
  }

  /** Listen on updates to Folder Rings in the cache.
   */
  private class FolderRingingListener implements FolderRingListener {
    public void fldRingRingUpdate(EventObject event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new ChatFrameDispatcher(event));
    }
  }

  /**
   * Listen on updates to the users in the cache and change the visible folder names if necessary.
   */
  private class UserListener implements UserRecordListener {
    public void userRecordUpdated(UserRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new FolderGUIUpdater(event));
    }
  }

  /**
   * Listen on updates to the contacts in the cache and change the visible folder names if necessary.
   */
  private class ContactListener implements ContactRecordListener {
    public void contactRecordUpdated(ContactRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new FolderGUIUpdater(event));
    }
  }

  /**
   * Listen on updates to message links to make a dispatcher for new chatting frames.
   */
  private static class MsgLinkListener implements MsgLinkRecordListener {
    public void msgLinkRecordUpdated(MsgLinkRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new ChatFrameDispatcher(event));
    }
  }
  private static class ChatFrameDispatcher implements Runnable {
    private EventObject event;
    public ChatFrameDispatcher(EventObject event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ChatFrameDispatcher.class, "ChatFrameDispatcher(EventObject event)");
      this.event = event;
      if (trace != null) trace.exit(ChatFrameDispatcher.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ChatFrameDispatcher.class, "ChatFrameDispatcher.run()");
      if (event instanceof MsgLinkRecordEvent) {
        MsgLinkRecord[] msgLinks = ((MsgLinkRecordEvent) event).getMsgLinkRecords();
        // Just-in-time chat updates always come singulairly, 1 msg link.
        if (msgLinks != null && msgLinks.length == 1) {
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          MsgLinkRecord mLink = msgLinks[0];
          if (mLink.ownerObjType != null && mLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
            UserRecord myUserRec = cache.getUserRecord();
            // only when msg link was created after my last login
            if (myUserRec != null && myUserRec.dateLastLogin != null && mLink.dateCreated.compareTo(myUserRec.dateLastLogin) > 0) {
              // don't alert on my own messages
              MsgDataRecord msgData = cache.getMsgDataRecord(mLink.msgId);
              FolderRecord fRec = cache.getFolderRecord(mLink.ownerObjId);
              if (fRec != null && fRec.isChatting() &&
                  fRec.getUpdateCount() > 0 &&
                  msgData != null && !msgData.senderUserId.equals(myUserRec.userId)
                  )
              {
                if (!OpenChatFolders.isOpenChatFolder(fRec.folderId)) {
                  FolderShareRecord sRec = cache.getFolderShareRecordMy(fRec.folderId, true);
                  if (sRec != null) {
                    // Only continue with set of GUI actions when we are sure the message is trully new, not an update of older msg.
                    if (PopupWindowManager.markNewMsgStamp(msgData)) {
                      // For quick sound use buffer and create the actual GUI chat frame later.
                      Component componentBuffer[] = new Component[1];
                      PopupWindowManager.addForScrolling(componentBuffer, msgData, true);
                      ChatTableFrame chatFrame = new ChatTableFrame(new FolderPair(sRec, fRec), Frame.ICONIFIED);
                      componentBuffer[0] = chatFrame;
                      chatFrame.triggerVisualUpdateNotificationRoll();
                    }
                  }
                } else {
                  Component chatComp = OpenChatFolders.getOpenChatFolder(fRec.folderId);
                  Frame chatFrame = null;
                  if (chatComp instanceof Frame) {
                    chatFrame = (Frame) chatComp;
                  } else {
                    Window chatWindow = SwingUtilities.windowForComponent(chatComp);
                    if (chatWindow instanceof Frame) {
                      chatFrame = (Frame) chatWindow;
                    }
                  }
                  if (chatFrame != null) {
                    int state = chatFrame.getState();
                    if (state == Frame.ICONIFIED) {
                      PopupWindowManager.addForScrolling(new Component[] { chatFrame }, msgData, false); // no suppression of isNewCheck
                    }
                  }
                }
              }
            }
          }
        }
      } else {
        Obj_List_Co reply = (Obj_List_Co) event.getSource();
        Object[] objs = reply.objs;

        Long folderId = (Long) objs[0];
        Long ringingUserId = (Long) objs[1];
//        Long[] allUserIDs = (Long[]) ArrayUtils.toArrayType((Object[]) objs[2], Long.class);
//        Long[] distributionUserIDs = (Long[]) ArrayUtils.toArrayType((Object[]) objs[3], Long.class);

        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        UserRecord myUserRec = cache.getUserRecord();
        if (myUserRec != null && !ringingUserId.equals(myUserRec.userId)) {
          FolderRecord fRec = cache.getFolderRecord(folderId);
          if (fRec != null) {
            FolderShareRecord sRec = cache.getFolderShareRecordMy(folderId, true);
            if (sRec != null) {
              try {
                Collection chatCompsV = OpenChatFolders.getOpenChatFolders(folderId);
                Component[] chatComps = null;
                if (chatCompsV != null && chatCompsV.size() > 0) {
                  chatComps = (Component[]) ArrayUtils.toArray(chatCompsV, Component.class);
                } else {
                  chatComps = new Component[1];
                  chatComps[0] = new ChatTableFrame(new FolderPair(sRec, fRec));
                }
                Nudge.nudge(chatComps, cache.getUserRecord().online.charValue() != 'D', true);
              } catch (Throwable t) {
                t.printStackTrace();
              }
            }
          }
        }
      }
      if (trace != null) trace.exit(ChatFrameDispatcher.class);
    } // end run
  } // end class ChatFrameDispatcher



  private class FolderGUIUpdater implements Runnable {
    private RecordEvent event;
    public FolderGUIUpdater(RecordEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderGUIUpdater.class, "FolderGUIUpdater(RecordEvent event)");
      this.event = event;
      if (trace != null) trace.exit(FolderGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderGUIUpdater.class, "FolderGUIUpdater.run()");

      Record[] records = null;
      if (event instanceof FolderRecordEvent) {
        records = ((FolderRecordEvent) event).getFolderRecords();
        if (event.getEventType() == RecordEvent.REMOVE) {
          removeFoldersFromTree((FolderRecord[]) records);
        } else if (event.getEventType() == RecordEvent.SET) {
          setFoldersInTree(CacheUtilities.convertRecordsToPairs(records));
        }
      }
      else if (event instanceof FolderShareRecordEvent) {
        records = ((FolderShareRecordEvent) event).getFolderShareRecords();
        if (event.getEventType() == RecordEvent.REMOVE) {
          // removal of shares does nothing to the tree
        } else {
          setFoldersInTree(CacheUtilities.convertRecordsToPairs(records));
        }
      }
      else if (event instanceof UserRecordEvent) {
        repaint();
      } // end else if instance of UserRecordEvent
      else if (event instanceof ContactRecordEvent) {
        repaint();
      }

      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(FolderGUIUpdater.class);
    }
  } // end class FolderGUIUpdater


  protected void finalize() throws Throwable {
    disposeObj();
    super.finalize();
  }


  /**
   * I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
   * Dispose the object and release resources to help in garbage collection.
  */
  public void disposeObj() {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    if (shareListener != null)
      cache.removeFolderShareRecordListener(shareListener);
    shareListener = null;
    if (folderListener != null)
      cache.removeFolderRecordListener(folderListener);
    folderListener = null;
    if (folderRingListener != null)
      cache.removeFolderRingListener(folderRingListener);
    folderRingListener = null;
    if (userListener != null)
      cache.removeUserRecordListener(userListener);
    userListener = null;
    if (contactListener != null)
      cache.removeContactRecordListener(contactListener);
    contactListener = null;
    if (msgLinkListener != null)
      cache.removeMsgLinkRecordListener(msgLinkListener);
    msgLinkListener = null;
    if (tree != null)
      tree.disposeObj();
  }

}
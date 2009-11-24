/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
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

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.*;
import com.CH_cl.tree.*;

import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.trace.*;
import com.CH_co.tree.*;

import com.CH_gui.frame.MainFrame;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class FolderTreeRefreshRunner extends ThreadTraced {

  private FolderTree fTree;
  private static boolean refreshInProgress;
  private static final Object refreshMonitor = new Object();
  private static int refreshThreadCount;
  private boolean withClearFolderCache = true;

  public FolderTreeRefreshRunner(FolderTree folderTree) {
    this(folderTree, true);
  }
  public FolderTreeRefreshRunner(FolderTree folderTree, boolean withClearFolderCache) {
    super("FolderTreeRefreshRunner # " + refreshThreadCount);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeRefreshRunner.class, "FolderTreeRefreshRunner(FolderTree folderTree)");
    if (trace != null) trace.args(folderTree);

    refreshThreadCount ++;
    refreshThreadCount %= Integer.MAX_VALUE-1;
    this.fTree = folderTree;
    this.withClearFolderCache = withClearFolderCache;
    setDaemon(true);

    if (trace != null) trace.exit(FolderTreeRefreshRunner.class);
  }

  public void runTraced() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeRefreshRunner.class, "FolderTreeRefreshRunner.runTraced()");

    boolean refreshNow = false;

    Thread.yield();

    // limit number of entries
    synchronized (refreshMonitor) {
      if (!refreshInProgress) {
        refreshNow = true;
        refreshInProgress = true;
      }
    }

    if (refreshNow) {
      try {
        Object AWTTreeLock = fTree.getTreeLock();

        FolderTreeSelectionExpansion selectionExpansion = null;
        FolderTreeModelCl model = null;
        FolderTreeNode root = null;
        synchronized (AWTTreeLock) {
          // suppress consideration for selection changes by event processors...
          fTree.suppressSelection(true);
          // dissalow any selection changes while in refresh
          fTree.setEnabled(false);
          // save all visuals
          selectionExpansion = FolderTreeSelectionExpansion.getData(fTree);
          model = fTree.getFolderTreeModel();
          root = model.getRootNode();
        }

        MessageAction msgAction = new MessageAction(CommandCodes.FLD_Q_GET_FOLDERS);
        ClientMessageAction replyAction = MainFrame.getServerInterfaceLayer().submitAndFetchReply(msgAction, 60000);

        boolean gotFolders = replyAction != null && replyAction.getActionCode() == CommandCodes.FLD_A_GET_FOLDERS;

        if (gotFolders) {
          synchronized (AWTTreeLock) {
            // We kept the visuals as long as possible, time to get rid of it as update just came in.
            root.removeAllChildren();
            model.nodeStructureChanged(root);
            // clear all folders and shares from the cache -- new ones just came in
            if (withClearFolderCache) {
              FetchedDataCache cache = FetchedDataCache.getSingleInstance();
              cache.clearFolderPairRecords();
            }
          }
        }

        // don't start a new thread here, just execute the action synchronously
        DefaultReplyRunner.nonThreadedRun(MainFrame.getServerInterfaceLayer(), replyAction);

        synchronized (AWTTreeLock) {
          // restore visuals once we are done
          selectionExpansion.restoreData(fTree);
          fTree.setEnabled(true);
          fTree.suppressSelection(false);
        }

        refreshInProgress = false;

      } catch (Throwable t) { 
        if (trace != null) trace.exception(FolderTreeRefreshRunner.class, 100, t);
      }
      // catch everything so we can decrement the counter properly

      // account for every exit -- only refreshing threads can mark when they are done.
      synchronized (refreshMonitor) {
        refreshInProgress = false;
      }

    }

    if (trace != null) trace.exit(FolderTreeRefreshRunner.class);
  }
} // end class FolderTreeRefreshRunner
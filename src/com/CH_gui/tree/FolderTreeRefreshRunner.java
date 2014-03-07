/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.tree;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.DefaultReplyRunner;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_co.util.SingleTokenArbiter;
import com.CH_gui.frame.MainFrame;
import javax.swing.SwingUtilities;

/** 
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* @author  Marcin Kurzawa
*/
public class FolderTreeRefreshRunner extends ThreadTraced {

  private FolderTree fTree;
  private boolean withClearFolderCache = true;

  private static SingleTokenArbiter arbiter = new SingleTokenArbiter();

  public FolderTreeRefreshRunner(FolderTree folderTree) {
    this(folderTree, true);
  }
  public FolderTreeRefreshRunner(FolderTree folderTree, boolean withClearFolderCache) {
    super("FolderTreeRefreshRunner");
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeRefreshRunner.class, "FolderTreeRefreshRunner(FolderTree folderTree)");
    if (trace != null) trace.args(folderTree);

    this.fTree = folderTree;
    this.withClearFolderCache = withClearFolderCache;
    setDaemon(true);

    if (trace != null) trace.exit(FolderTreeRefreshRunner.class);
  }

  public void runTraced() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeRefreshRunner.class, "FolderTreeRefreshRunner.runTraced()");

    Object key = "refresh";
    Object token = new Object();
    // limit to single entry
    if (arbiter.putToken(key, token)) {
      try {
        doRefresh();
      } catch (Throwable t) {
        if (trace != null) trace.exception(FolderTreeRefreshRunner.class, 100, t);
      } finally {
        arbiter.removeToken(key, token);
      }
    }

    if (trace != null) trace.exit(FolderTreeRefreshRunner.class);
  }

  private void doRefresh() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeRefreshRunner.class, "doRefresh()");

    final FolderTreeSelectionExpansion[] _selectionExpansion = new FolderTreeSelectionExpansion[1];

    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          // suppress consideration for selection changes by event processors...
          fTree.suppressSelection(true);
          // dissalow any selection changes while in refresh
          fTree.setEnabled(false);
          // save all visuals
          _selectionExpansion[0] = FolderTreeSelectionExpansion.getData(fTree);
        }
      });
    } catch (Throwable t) {
      if (trace != null) trace.exception(FolderTreeRefreshRunner.class, 100, t);
    }

    MessageAction msgAction = new MessageAction(CommandCodes.FLD_Q_GET_FOLDERS);
    ClientMessageAction replyAction = MainFrame.getServerInterfaceLayer().submitAndFetchReply(msgAction, 60000);
    boolean gotFolders = replyAction != null && replyAction.getActionCode() == CommandCodes.FLD_A_GET_FOLDERS;

    if (gotFolders) {
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          public void run() {
            // We kept the visuals as long as possible, time to get rid of it as update just came in.
            FolderTreeModelGui model = fTree.getFolderTreeModel();
            FolderTreeNodeGui root = model.getRootNode();
            root.removeAllChildren();
            model.nodeStructureChanged(root);
            // clear all folders and shares from the cache -- new ones just came in
            if (withClearFolderCache) {
              FetchedDataCache cache = FetchedDataCache.getSingleInstance();
              cache.clearFolderPairRecords();
            }
          }
        });
      } catch (Throwable t) {
        if (trace != null) trace.exception(FolderTreeRefreshRunner.class, 200, t);
      }
    }

    // don't start a new thread here, just execute the action synchronously
    DefaultReplyRunner.nonThreadedRun(MainFrame.getServerInterfaceLayer(), replyAction);

    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          // restore visuals once we are done
          _selectionExpansion[0].restoreData(fTree);
          fTree.setEnabled(true);
          fTree.suppressSelection(false);
        }
      });
    } catch (Throwable t) {
      if (trace != null) trace.exception(FolderTreeRefreshRunner.class, 300, t);
    }

    if (trace != null) trace.exit(FolderTreeRefreshRunner.class);
  }

} // end class FolderTreeRefreshRunner
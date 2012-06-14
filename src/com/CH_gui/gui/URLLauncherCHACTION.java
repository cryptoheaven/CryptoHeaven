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

package com.CH_gui.gui;

import com.CH_cl.service.cache.CacheFldUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.ops.FolderOps;
import com.CH_co.service.records.FolderPair;
import com.CH_co.service.records.UserRecord;
import com.CH_gui.actionGui.JActionFrame;
import com.CH_gui.frame.*;
import com.CH_gui.menuing.MenuActionItem;
import com.CH_gui.menuing.MenuTreeModel;
import com.CH_gui.util.HTML_ClickablePane;
import com.CH_gui.util.URLLauncher;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

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
* <b>$Revision: 1.4 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class URLLauncherCHACTION extends Object implements URLLauncher {

  public static final String ACTION_PATH = "actions";

  public void openURL(URL url, Component invoker) {
    openActionURL(url, invoker);
  }

  public static void openActionURL(URL url, Component invoker) {
    String pathRootDir = null;
    String path = url.getPath();
    String args = url.getQuery(); // args in most cases is null
    if (path != null) path = URLDecoder.decode(path);
    if (args != null) args = URLDecoder.decode(args);
    if (path != null) path = path.trim();
    if (args != null) args = args.trim();
    if (args != null && args.length() == 0) args = null;

    // path must not be null because it stores the action to execute
    if (path == null) path = "";

    if (path.startsWith("\\") || path.startsWith("/"))
      path = path.substring(1);
    int endRootDir = path.indexOf('\\');
    if (endRootDir == -1) endRootDir = path.indexOf('/');
    if (endRootDir > 0) {
      pathRootDir = path.substring(0, endRootDir);
      path = path.substring(endRootDir + 1);
    }
    if (pathRootDir != null && pathRootDir.equalsIgnoreCase(ACTION_PATH)) {
      String actionName = null;
      Integer actionId = null;
      try {
        actionId = Integer.valueOf(path);
      } catch (Throwable t) {
        actionName = path;
      }
      if (actionId != null && actionId.intValue() == 308 && args != null) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        UserRecord uRec = cache.getUserRecord();
        if (uRec != null) {
          if (args.equalsIgnoreCase("Local")) {
            new LocalFileTableFrame("Browse");
          } else if (args.equalsIgnoreCase("My Files")) {
            new FileTableFrame(CacheFldUtils.convertRecordToPair(cache.getFolderRecord(uRec.fileFolderId)));
          } else if (args.equalsIgnoreCase("Address Book") || args.equalsIgnoreCase("Address-Book")) {
            FolderPair fPair = FolderOps.getOrCreateAddressBook(MainFrame.getServerInterfaceLayer());
            new AddressTableFrame(fPair);
          } else if (args.equalsIgnoreCase("Allowed Senders") || args.equalsIgnoreCase("Allowed-Senders")) {
            FolderPair fPair = FolderOps.getOrCreateWhiteList(MainFrame.getServerInterfaceLayer());
            new WhiteListTableFrame(fPair);
          } else if (args.equalsIgnoreCase("Drafts")) {
            FolderPair fPair = FolderOps.getOrCreateDraftFolder(MainFrame.getServerInterfaceLayer());
            new MsgTableFrame(fPair);
          } else if (args.equalsIgnoreCase("Inbox")) {
            new MsgTableFrame(CacheFldUtils.convertRecordToPair(cache.getFolderRecord(uRec.msgFolderId)));
          } else if (args.equalsIgnoreCase("Spam")) {
            FolderPair fPair = FolderOps.getOrCreateJunkFolder(MainFrame.getServerInterfaceLayer());
            new MsgTableFrame(fPair);
          } else if (args.equalsIgnoreCase("Sent")) {
            new MsgTableFrame(CacheFldUtils.convertRecordToPair(cache.getFolderRecord(uRec.sentFolderId)));
          } else if (args.equalsIgnoreCase("Contacts")) {
            new ContactTableFrame();
          }
        }
      } else {
        Frame parentFrame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, invoker);
        if (parentFrame == null && invoker instanceof HTML_ClickablePane) {
          Component renderingFor = ((HTML_ClickablePane) invoker).getRendererContainer();
          if (renderingFor != null)
            parentFrame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, renderingFor);
        }
        if (parentFrame instanceof JActionFrame) {
          JActionFrame actionFrame = (JActionFrame) parentFrame;
          MenuTreeModel menuModel = actionFrame.getMenuTreeModel();
          DefaultTreeModel treeModel = menuModel.getTreeModel();

          MenuActionItem foundAction = null;

          DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
          Enumeration enm = root.preorderEnumeration();

          while (enm.hasMoreElements()) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) enm.nextElement();
            MenuActionItem menuNode = (MenuActionItem) treeNode.getUserObject();

            if (actionId != null && actionId.equals(menuNode.getActionId())) {
              foundAction = menuNode;
              break;
            } else if (actionName != null && actionName.equals(menuNode.getName())) {
              foundAction = menuNode;
              break;
            } else if (actionName != null && actionName.equalsIgnoreCase(menuNode.getName())) {
              // no break if found action is not exactly CASE MATCHING
              foundAction = menuNode;
            }
          } // end while hasMoreElements

          if (foundAction != null) {
            foundAction.getAction().actionPerformed(new ActionEvent(invoker, 0, "run"));
          }
        }
      }
    }
  }

}
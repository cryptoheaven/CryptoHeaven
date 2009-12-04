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

import com.CH_cl.service.cache.*;
import com.CH_cl.tree.*;

import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.Trace;
import com.CH_co.tree.*;
import com.CH_co.util.DisposableObj;

import com.CH_gui.list.ListRenderer;
import com.CH_gui.msgs.MsgPanelUtils;

import java.awt.Rectangle;
import java.util.Vector;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/** 
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: 1.26 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FolderTree extends JTree implements DisposableObj {

  private boolean selectionSuppressed;

  private long lastTimeScrolledExtraHight;
  private static long DELAY_BETWEEN_SCROLLING_EXTRA_HIGHT = 250;

  static {
    FolderTreeNode.folderTreeNodeSortNameProviderI = new FolderTreeNodeSortNameProviderI() {
      public String getSortName(FolderPair fPair) {
        return getFolderAndShareNamesForTreeDisplaySort(fPair);
      }
    };
  }

  /** Creates new FolderTree */
  public FolderTree() {
    this(new FolderTreeNode());
  }
  /** Creates new FolderTree */
  public FolderTree(RecordFilter filter) {
    this(new FolderTreeModelCl(filter));
  }
  /** Creates new FolderTree */
  public FolderTree(RecordFilter filter, FolderPair[] initialFolderPairs) {
    this(new FolderTreeNode(), filter, initialFolderPairs);
  }

  /** Creates new FolderTree */
  public FolderTree(FolderTreeNode root) {
    this(new FolderTreeModelCl(root));
  }

  /** Creates new FolderTree */
  public FolderTree(FolderTreeNode root, RecordFilter filter, FolderPair[] initialFolderPairs) {
    this(new FolderTreeModelCl(root, filter, initialFolderPairs));
  }

  /** Creates new FolderTree */
  public FolderTree(FolderTreeModelCl treeModel) {
    super(treeModel);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTree.class, "FolderTree(FolderTreeNode)");  
    init();
    if (trace != null) trace.exit(FolderTree.class);
  }

  private void init() {
    this.setCellRenderer(new FolderTreeCellRenderer());

    /* display lines to connect folders in the tree */
    this.putClientProperty("JTree.lineStyle", "Angled");
    this.setScrollsOnExpand(true);
    this.setEditable(false);
    this.setLargeModel(true);
    this.setRowHeight(18);

    // Do not display the root of the tree
    setRootVisible(false);
    setShowsRootHandles(true);
  }


  protected void suppressSelection(boolean value) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTree.class, "suppressSelection(boolean value)");  
    if (trace != null) trace.args(value);
    selectionSuppressed = value;
    if (trace != null) trace.exit(FolderTree.class);
  }
  public boolean isSelectionSuppressed() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTree.class, "isSelectionSuppressed()");  
    boolean rc = selectionSuppressed;
    if (trace != null) trace.exit(FolderTree.class, rc);
    return rc;
  }

  public FolderTreeModelCl getFolderTreeModel() {
    return (FolderTreeModelCl) getModel();
  }

  public void removeTreeSelectionListeners() {
    TreeSelectionListener[] listeners = (TreeSelectionListener[]) getListeners(TreeSelectionListener.class);
    if (listeners != null && listeners.length > 0)
      for (int i=0; i<listeners.length; i++)
        removeTreeSelectionListener(listeners[i]);
  }


  /** 
   * Overwrite the method from super class to get the string for the label.
   * @return string for each node label
   */
  public String convertValueToText(Object value,
                                 boolean selected,
                                 boolean expanded,
                                 boolean leaf,
                                 int row,
                                 boolean hasFocus)
  {
    String nodeText = "";
    if (value instanceof FolderTreeNode) {
      FolderPair folderPair = ((FolderTreeNode)value).getFolderObject();
      if (folderPair == null) {
        nodeText = com.CH_gui.lang.Lang.rb.getString("folder_Desktop");
      } else {
        FolderRecord fRec = folderPair.getFolderRecord();
        nodeText = fRec.getCachedDisplayText();
        String toolTip = fRec.getCachedToolTip();

        String ownerNote = null;
        String chatNote = null;
        String appendPostfix = null;

        if (nodeText == null || toolTip == null) {
          String[] notes = getOwnerAndChatNote(fRec);
          ownerNote = notes[0];
          chatNote = notes[1];
        }

        if (toolTip == null) {
          // Here and not in the renderer is the proper place to set tool tip!  I don't know why is that...
          if (ownerNote.length() > 0 && chatNote.length() == 0) {
            toolTip = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("folderTip_The_user_USER_is_the_primary_owner_of_this_folder."), new Object[] {ownerNote});
          } else if (ownerNote.length() == 0 && chatNote.length() > 0) {
            toolTip = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("folderTip_You_are_sharing_this_chatting_folder_with_USER."), new Object[] {chatNote});
          } else if (ownerNote.length() > 0 && chatNote.length() > 0) {
            toolTip = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("folderTip_The_user_USER_is_the_primary_owner_of_this_chatting_folder._Other_participants_are_OTHER-USERS."), new Object[] {ownerNote, chatNote});
          } else {
            toolTip = "";
          }
          fRec.setCachedToolTip(toolTip == null ? "" : toolTip);
        }
        setToolTipText(toolTip != null && toolTip.length() > 0 ? toolTip : null);

        if (nodeText == null) {

          String ownerAndChatNotes = ownerNote.length() > 0 ? ownerNote + " " + chatNote : chatNote;
          String folderName = folderPair.getFolderShareRecord().getFolderName();

          // For Chatting folders, to save space, don't display the default "Chat Log" name.
          if (folderPair.getFolderRecord().isChattingLive()) {
            String defaultChatFolderName = com.CH_gui.lang.Lang.rb.getString("folderName_Chat_Log");
            String fName = folderName != null ? folderName : "";
            if (fName.startsWith(defaultChatFolderName)) {
              if (ownerAndChatNotes.length() > 0) {
                nodeText = "";
                appendPostfix = " chat";
              } else {
                nodeText = "Chat...";
              }
            } else if (ownerAndChatNotes.length() == 0) {
              // not yet fetched live chatting shares
              nodeText = "" + folderName + "...";
            }
          }

          if (nodeText == null)
            nodeText = folderName;
          if (nodeText == null)
            nodeText = "";

          StringBuffer updateNoteSB = new StringBuffer();
          int updateCount = fRec.getUpdateCount();
          if (updateCount > 0) {
            updateNoteSB.append("(");
            updateNoteSB.append(updateCount);
            updateNoteSB.append(")");
          }

          if (ownerAndChatNotes.length() > 0 || updateNoteSB.length() > 0) {
            String text = nodeText;

            StringBuffer newNameSB = new StringBuffer();
            if (updateNoteSB.length() > 0) {
              boolean isSpamFolder = fRec.folderId.equals(FetchedDataCache.getSingleInstance().getUserRecord().junkFolderId);
              boolean isRecycleFolder = fRec.isRecycleType();
              boolean isHTML = false;
              // skip BOLD for Spam folder
              if (!isSpamFolder && !isRecycleFolder) {
                newNameSB.append("<html><b>"); // string "<html><b>" is in determination is renderer should make this bold... skip closing tags!
                isHTML = true;
              }
              newNameSB.append(text);
              if (!isRecycleFolder) {
                newNameSB.append(' ');
                newNameSB.append(updateNoteSB);
              }
              if (ownerAndChatNotes.length() > 0) {
                if (isHTML) {
                  //newNameSB.append("</b>"); -- skipping closing tag for renderer
                }
                newNameSB.append(" : ");
                newNameSB.append(ownerAndChatNotes);
                if (isHTML) {
                  //newNameSB.append("</html>"); -- skipping closing tag for renderer
                }
              } else {
                if (isHTML) {
                  //newNameSB.append("</b></html>"); -- skipping closing tag for renderer
                }
              }
            } else if (ownerAndChatNotes.length() > 0) {
              if (text.length() > 0) {
                newNameSB.append(text);
                newNameSB.append(" : ");
              }
              newNameSB.append(ownerAndChatNotes);
            } else {
              newNameSB.append(text);
            }

            nodeText = newNameSB.toString();
          }
          if (appendPostfix != null)
            nodeText += appendPostfix;
          // only when folder name has been retrieved and decrypted we know the proper display text
          if (folderName != null)
            fRec.setCachedDisplayText(nodeText);
        }
      }
    } else {
      nodeText = super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
    }

    return nodeText;
  }


  /**
   * @return name of the folder with participants for display.
   */
  public static String getFolderAndShareNames(FolderPair fPair, boolean includeAllParticipants) {
    FolderRecord fRec = fPair.getFolderRecord();
    String[] notes = getFolderNote(fRec, includeAllParticipants);
    String additionalNote = notes[0].length() > 0 ? notes[0] + " " + notes[1] : notes[1];
    additionalNote = additionalNote.trim();
    String title = fPair.getMyName();
    String appendPostfix = null;
    if (title == null) title = "";
    if (additionalNote.length() > 0) {
      if (fRec.isChatting()) {
        String defaultChatFolderName = com.CH_gui.lang.Lang.rb.getString("folderName_Chat_Log");
        if (title.startsWith(defaultChatFolderName)) {
          title = "";
          appendPostfix = " chat";
        }
      }
      title = title.length() > 0 ? title + " : " + additionalNote : additionalNote;
    }
    if (appendPostfix != null)
      title += appendPostfix;
    return title;
  }

  public static String getFolderAndShareNamesForTreeDisplaySort(FolderPair fPair) {
    return getFolderAndShareNames(fPair, fPair.getFolderRecord().isChatting());
  }

  /**
   * @return name of the folder owner and all participants in a String[2] array with first being the owner.
   */
  public static String[] getOwnerAndChatNote(FolderRecord fRec) {
    String[] rc = null;
    if (fRec != null)
      rc = getFolderNote(fRec, fRec.isChatting());
    return rc;
  }
  /**
   * @return name of the folder owner and all participants in a String[2] array with first being the owner.
   */
  private static String[] getFolderNote(FolderRecord fRec, boolean includeAllParticipants) {
    FetchedDataCache cache = null;
    Long myUserId = null;
    Long ownerUserId = null;
    String ownerNote = fRec.getCachedOwnerNote();
    String chatNote = fRec.getCachedChatNote();
    String rcChatNote = "";

    if (ownerNote == null || chatNote == null) {
      cache = FetchedDataCache.getSingleInstance();
      myUserId = cache.getMyUserId();
      ownerUserId = fRec.ownerUserId;
    }

    if (ownerNote == null) {
      // If folder is not yours show whose it is.
      if (!ownerUserId.equals(myUserId)) {
        StringBuffer sb = new StringBuffer(32);
        Record rec = MsgPanelUtils.convertUserIdToFamiliarUser(ownerUserId, true, true);
        if (rec != null) {
          sb.append('[');
          sb.append(ListRenderer.getRenderedText(rec));
          sb.append(']');
        }
        else {
          sb.append("[*]");
        }
        ownerNote = sb.toString();
      } else {
        ownerNote = "";
      }
      fRec.setCachedOwnerNote(ownerNote);
    }

    // If folder is a chatting folder, show other participants.
    // Also show other participants for table frames.
    if (includeAllParticipants) {
      if (chatNote == null) {
        StringBuffer sb = new StringBuffer(32);
        FolderShareRecord[] allShares = cache.getFolderShareRecordsForFolder(fRec.folderId);
        if (allShares != null) {
          boolean appended = false;
          boolean foundMine = false;
          for (int i=0; i<allShares.length; i++) {
            FolderShareRecord share = allShares[i];
            Long shareOwnerUserId = share.ownerUserId;
            if (share.isOwnedByGroup() || 
                (!share.isOwnedBy(ownerUserId, (Long[]) null) && 
                !share.isOwnedBy(myUserId, (Long[]) null))) {
              if (appended)
                sb.append(" / ");
              Record recipient = null;
              if (share.isOwnedByUser())
                recipient = MsgPanelUtils.convertUserIdToFamiliarUser(shareOwnerUserId, true, true);
              else
                recipient = FetchedDataCache.getSingleInstance().getFolderRecord(share.ownerUserId);
              if (recipient != null) 
                sb.append(ListRenderer.getRenderedText(recipient));
              else {
                if (share.isOwnedByUser())
                  sb.append("User ("+share.ownerUserId+")");
                else
                  sb.append("Group ("+share.ownerUserId+")");
              }
              appended = true;
            } else if (share.isOwnedBy(myUserId, (Long[]) null)) {
              foundMine = true;
            }
          }
//          if (allShares.length > 1 && foundMine) {
//            if (appended)
//              sb.append(" / ");
//            sb.append("me");
//          }
        }
        chatNote = sb.toString();
        if (chatNote == null) chatNote = "";
        fRec.setCachedChatNote(chatNote);
      }
      rcChatNote = chatNote;
    }

    return new String[] {ownerNote, rcChatNote};
  }

  /** Shortcut to get a FolderPair of a last selected node in this tree */
  public FolderPair getLastSelectedPair() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTree.class, "getLastSelectedPair()");

    FolderPair folderPair = null;
    FolderTreeNode node = (FolderTreeNode) getLastSelectedPathComponent();

    if (node != null)
      folderPair = node.getFolderObject();

    if (trace != null) trace.exit(FolderTree.class, folderPair);
    return folderPair;
  }

  public void setSelectedFolder(Long folderId) {
    FolderTreeNode node = getFolderTreeModel().findNode(folderId, true);
    if (node != null) {
      TreeNode[] pathNodes = node.getPath();
      TreePath path = new TreePath(pathNodes);
      getSelectionModel().setSelectionPath(path);
    }
  }

  public void scrollPathToVisible2(TreePath path) {
    makeVisible(path);
    int row = getRowForPath(path);
    Rectangle rect = getRowBounds(row);
    long now = System.currentTimeMillis();
    if (Math.abs(now - lastTimeScrolledExtraHight) > DELAY_BETWEEN_SCROLLING_EXTRA_HIGHT) {
      lastTimeScrolledExtraHight = now;
      rect.setBounds(rect.x-40, rect.y-rect.height, 50+40, rect.height*3); // 50 width enough to see part of childrens' level
    } else {
      rect.setBounds(rect.x-40, rect.y, 50+40, rect.height);
    }
    scrollRectToVisible(rect);
  }

  /** @return an array of FolderPairs of all specified nodes in the tree */
  public static FolderPair[] getLastPathComponentFolderPairs(TreePath[] treePaths) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTree.class, "getLastPathComponentFolderPairs(TreePath[])");
    if (trace != null) trace.args(treePaths);

    Vector folderPairsV = new Vector();
    if (treePaths != null && treePaths.length > 0) {
      FolderTreeNode[] lastNodes = getLastPathComponentNodes(treePaths);

      if (lastNodes != null) {
        for (int i=0; i<lastNodes.length; i++) {
          FolderPair folderPair = lastNodes[i].getFolderObject();
          if (folderPair != null)
            folderPairsV.addElement(folderPair);
        }
      }
    }

    FolderPair[] folderPairs = new FolderPair[folderPairsV.size()];
    if (folderPairsV.size() > 0)
      folderPairsV.toArray(folderPairs);

    if (trace != null) trace.exit(FolderTree.class, folderPairs);
    return folderPairs;
  }

  /** @return an array of folder ids of specified nodes which paths are given */
  public static Long[] getLastPathComponentFolderIds(TreePath[] treePaths) {
    FolderPair[] folderPairs = getLastPathComponentFolderPairs(treePaths);

    Long[] ids = new Long[folderPairs.length];

    for (int i=0; i<folderPairs.length; i++) 
      ids[i] = folderPairs[i].getFolderRecord().getId();

    return ids;
  }

  /** @return an array of folder share id of selected nodes */
  public static Long[] getLastPathComponentShareIds(TreePath[] treePaths) {
    FolderPair[] folderPairs = getLastPathComponentFolderPairs(treePaths);

    Long[] shareIds = new Long[folderPairs.length];

    for (int i=0; i<folderPairs.length; i++) 
      shareIds[i] = folderPairs[i].getFolderShareRecord().shareId;

    return shareIds;
  }

  /** @return FolderTreeNode[] that are at the end of specified paths */
  public static FolderTreeNode[] getLastPathComponentNodes(TreePath[] treePaths) {

    FolderTreeNode[] lastNodes = new FolderTreeNode[treePaths.length];

    for (int i=0; i<treePaths.length; i++) 
      lastNodes[i] = (FolderTreeNode) treePaths[i].getLastPathComponent();

    return lastNodes;
  }


  /**
   * I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
   * Dispose the object and release resources to help in garbage collection.
  */
  public void disposeObj() {
    removeTreeSelectionListeners();
  }

}
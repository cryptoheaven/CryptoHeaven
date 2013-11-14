/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.msgTable;

import java.util.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import com.CH_cl.service.cache.*;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.sortedTable.TableSorter;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.12 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class MsgTableSorter extends TableSorter {

  private boolean isThreaded = true;

  public void setThreaded(boolean threaded) {
    isThreaded = threaded;
  }
  public boolean isThreaded() {
    return isThreaded;
  }

  public boolean suppressUpdateSorts() {
    return true;
  }

  /**
   * Post processing sort operation that allowes for threaded ordering of messages.
   * It is called immediately after the normal sort() operation by the super class.
   */
  public void sortPostProcessing(Object[] objIndexes) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTableSorter.class, "sortPostProcessing(Object[] objIndexes)");
    if (trace != null) trace.args(objIndexes);

    if (isThreaded && objIndexes != null && objIndexes.length > 1) {
      TableModel tableModel = getRawModel();

      if (tableModel instanceof MsgTableModel) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();

        MsgTableModel msgTableModel = (MsgTableModel) tableModel;
        if (trace != null) trace.data(5, "msgTableModel", msgTableModel);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        MultiHashMap rootNodes_byReplyMsgId = new MultiHashMap();
        MultiHashMap nodes_byMsgId = new MultiHashMap();
        MultiHashMap nodes_byObjIndex = new MultiHashMap();
        // one index at a time build our tree top-to-bottom
        for (int i=0; i<objIndexes.length; i++) {
          Integer objIndexI = (Integer) objIndexes[i];
          int objIndex = objIndexI.intValue();
          MsgLinkRecord mLink = (MsgLinkRecord) msgTableModel.getRowObjectNoTrace(objIndex);
          Object[] nodeObj = new Object[] { mLink, objIndexI };
          DefaultMutableTreeNode mLinkNode = new DefaultMutableTreeNode(nodeObj);
          nodes_byMsgId.put(mLink.msgId, mLinkNode);
          nodes_byObjIndex.put(objIndexI, mLinkNode);
          MsgDataRecord mData = cache.getMsgDataRecordNoTrace(mLink.msgId);
          // move root nodes to new one if they are replies to it.
          Collection childrenV = rootNodes_byReplyMsgId.poolAll(mLink.msgId);
          if (childrenV != null) {
            Iterator iter = childrenV.iterator();
            while (iter.hasNext()) {
              //root.remove(child);  // interesting that nodes need not be removed to change their location
              mLinkNode.add((DefaultMutableTreeNode) iter.next());
            }
          }

          Long replyToMsgId = mData != null ? mData.replyToMsgId : null;
          if (replyToMsgId == null) {
            // not a reply, add to root
            root.add(mLinkNode);
          } else {
            // is a reply, find parent node
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) nodes_byMsgId.get(replyToMsgId);
            if (parent != null) {
              parent.add(mLinkNode);
            } else {
              root.add(mLinkNode);
              rootNodes_byReplyMsgId.put(replyToMsgId, mLinkNode);
            }
          }
        }
        int sortDir = getPrimarySortingDirection();
        if (sortDir == -1) {
          // for descending ordering
          // iterate through original list to order top ones' threads to the top
          for (int i=objIndexes.length-1; i>=0; i--) {
            Integer objIndexI = (Integer) objIndexes[i];
            DefaultMutableTreeNode mLinkNode = (DefaultMutableTreeNode) nodes_byObjIndex.get(objIndexI);
            TreeNode[] path = mLinkNode.getPath();
            // path[0] is always root, so take next element as it must be connected to root
            DefaultMutableTreeNode firstOrderChild = (DefaultMutableTreeNode) path[1];
            root.remove(firstOrderChild);
            root.insert(firstOrderChild, 0);
          }
        } else if (sortDir == +1) {
          // for ascending ordering
          // iterate through original list to order top ones' threads to the bottom
          for (int i=0; i<objIndexes.length; i++) {
            Integer objIndexI = (Integer) objIndexes[i];
            DefaultMutableTreeNode mLinkNode = (DefaultMutableTreeNode) nodes_byObjIndex.get(objIndexI);
            TreeNode[] path = mLinkNode.getPath();
            // path[0] is always root, so take next element as it must be connected to root
            DefaultMutableTreeNode firstOrderChild = (DefaultMutableTreeNode) path[1];
            root.remove(firstOrderChild);
            root.add(firstOrderChild);
          }
        }
        // iterate through the tree to get updated order
        Enumeration enm = root.preorderEnumeration();
        int index = 0;
        while (enm.hasMoreElements()) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) enm.nextElement();
          if (node != root) {
            Object[] nodeObj = (Object[]) node.getUserObject();
            MsgLinkRecord mLink = (MsgLinkRecord) nodeObj[0];
            Integer objIndex = (Integer) nodeObj[1];
            mLink.setSortThreadLayer(node.getLevel()-1);
            objIndexes[index] = objIndex;
            index ++;
          }
        }
      }
    }

    // return values have overwritten the original array
    if (trace != null) trace.exit(MsgTableSorter.class, objIndexes);
  } // end sortPostProcessing()

}
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
package com.CH_cl.tree;

import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.Trace;
import com.CH_co.tree.*;
import com.CH_co.util.*;

import java.util.Enumeration;
import java.util.Vector;

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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FolderTreeModelCl extends FolderTreeModelCo {

  /** Creates new FolderTreeModelCl */
  public FolderTreeModelCl() {
    this(new FolderTreeNode());
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelCl.class, "FolderTreeModel()");
    if (trace != null) trace.exit(FolderTreeModelCl.class);
  }

  /** Creates new FolderTreeModelCl with specified folder filter. */
  public FolderTreeModelCl(RecordFilter filter) {
    super(filter);
  }

  /** Creates new FolderTreeModelCl with specified folder filter. */
  public FolderTreeModelCl(RecordFilter filter, FolderPair[] initialFolderPairs) {
    super(filter, initialFolderPairs);
  }

  /** Creates new FolderTreeModelCl */
  public FolderTreeModelCl(FolderTreeNode root) {
    super(root);
  }

  /** Creates new FolderTreeModelCl */
  public FolderTreeModelCl(FolderTreeNode root, RecordFilter filter, FolderPair[] initialFolderPairs) {
    super(root, filter, initialFolderPairs);
  }

  /** @param folder is a FolderRecord that will be removed from this tree model
   * <code> folder </code> cannnot be null
   * Note: only removal of FolderRecords is supported, and not FolderShares alone
   */
  public synchronized void removeRecord(FolderRecord folder, boolean keepCacheResidantChildren) {
    Trace trace = null;
    if (Trace.DEBUG) {
      trace = Trace.entry(FolderTreeModelCl.class, "removeNodeFromModel(FolderRecord, boolean keepCacheResidantChildren)");
    }
    if (trace != null) {
      trace.args(folder);
    }
    if (trace != null) {
      trace.args(keepCacheResidantChildren);
    }

    FolderTreeNode nodeToRemove = findNode(folder.getId(), true);

    if (nodeToRemove != null) {

      // Remember the children nodes which still exist in the cache, 
      // if the 'keepCacheResidantChildren' flag is specified.
      Vector keepChildrenV = null;
      if (keepCacheResidantChildren) {
        Enumeration enm = nodeToRemove.postorderEnumeration(); // all descending children
        if (enm != null && enm.hasMoreElements()) {
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          while (enm.hasMoreElements()) {
            FolderTreeNode childNode = (FolderTreeNode) enm.nextElement();
            FolderPair fPair = childNode.getFolderObject();
            if (cache.getFolderRecord(fPair.getId()) != null) {
              if (keepChildrenV == null) {
                keepChildrenV = new Vector();
              }
              keepChildrenV.addElement(fPair);
            }
          }
        }
      }

      removeNodeFromParent(nodeToRemove, true);

      // if we have kept some children, add them back to the tree...
      if (keepCacheResidantChildren) {
        FolderPair[] keepChildren = (FolderPair[]) ArrayUtils.toArray(keepChildrenV, FolderPair.class);
        addNodes(keepChildren);
      }
    }

    if (trace != null) {
      trace.exit(FolderTreeModelCl.class);
    }
  }
}

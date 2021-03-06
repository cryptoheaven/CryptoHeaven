/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.tree;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.RecordFilter;
import com.CH_co.trace.Trace;
import com.CH_co.tree.*;
import com.CH_co.util.ArrayUtils;

import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class FolderTreeModelCl extends FolderTreeModel {

  private FetchedDataCache cache;

  /** Creates new FolderTreeModelCl */
  public FolderTreeModelCl() {
    super();
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelCl.class, "FolderTreeModelCl()");
    if (trace != null) trace.exit(FolderTreeModelCl.class);
  }

  /** Creates new FolderTreeModelCl with specified folder filter. */
  public FolderTreeModelCl(RecordFilter filter) {
    super(filter);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelCl.class, "FolderTreeModelCl(FolderFilter folderFilter)");
    if (trace != null) trace.exit(FolderTreeModelCl.class);
  }

  /** Creates new FolderTreeModelCl */
  public FolderTreeModelCl(FolderTreeNode root) {
    super(root);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeModelCl.class, "FolderTreeModelCl(FolderTreeNode root)");
    if (trace != null) trace.exit(FolderTreeModelCl.class);
  }

  /**
   * Must call this initialization method before using the model.
   * @param cache 
   */
  public synchronized void initWithReferenceCache(FetchedDataCache cache) {
    this.cache = cache;
  }

  /** @param folder is a FolderRecord that will be removed from this tree model
   * <code> folder </code> cannnot be null
   * Note: only removal of FolderRecords is supported, and not FolderShares alone
   */
  public synchronized boolean removeRecord(FolderRecord folder, boolean keepCacheResidantChildren) {
    Trace trace = null; if (Trace.DEBUG) trace = Trace.entry(FolderTreeModel.class, "removeNodeFromModel(FolderRecord, boolean keepCacheResidantChildren)");
    if (trace != null) trace.args(folder);
    if (trace != null) trace.args(keepCacheResidantChildren);

    FolderTreeNode nodeToRemove = findNode(folder.getId(), true);

    if (nodeToRemove != null) {

      // Remember the children nodes which still exist in the cache,
      // if the 'keepCacheResidantChildren' flag is specified.
      ArrayList keepChildrenL = null;
      if (keepCacheResidantChildren) {
        Enumeration enm = nodeToRemove.postorderEnumeration(); // all descending children
        if (enm != null && enm.hasMoreElements()) {
          while (enm.hasMoreElements()) {
            FolderTreeNode childNode = (FolderTreeNode) enm.nextElement();
            FolderPair fPair = childNode.getFolderObject();
            if (cache.getFolderRecord(fPair.getId()) != null) {
              if (keepChildrenL == null) keepChildrenL = new ArrayList();
              keepChildrenL.add(fPair);
            }
          }
        }
      }

      removeNodeFromParent(nodeToRemove, true);

      // if we have kept some children, add them back to the tree...
      if (keepCacheResidantChildren) {
        FolderPair[] keepChildren = (FolderPair[]) ArrayUtils.toArray(keepChildrenL, FolderPair.class);
        addNodes(keepChildren);
      }
    }

    if (trace != null) trace.exit(FolderTreeModel.class, nodeToRemove != null);
    return nodeToRemove != null;
  }

}
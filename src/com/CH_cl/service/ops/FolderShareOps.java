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

package com.CH_cl.service.ops;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import java.util.*;

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
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FolderShareOps extends Object {

  /**
   * Creates a difference set with shares to Remove/Change/Add in order to arrive at 'wantedShares' set.
   * When changing sharing of any particular folder, the returned 3 arrays of shares will specify
   * which ones need to be removed, which need permission changes, and which need to be added
   * so that resulting sharing of a particular folder is that of specified in 'wantedShares'.
   * Note: 'wantedShares' may not specify any real shareId numbers as some of them may be created in client GUI
   * @param folderPair is the folder to which we are seeking change from existing shares to wanted shares
   * @return null if wantedShares are all the same as existing, otherwise an array of 3 sets in order: remove/change/add
   */
  public static FolderShareRecord[][] getFolderShareChanges(FolderShareRecord[] existingShares, ArrayList wantedSharesL, FolderPair folderPair) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderShareOps.class, "getFolderShareChanges(FolderShareRecord[] existingShares, ArrayList wantedSharesL, FolderPair folderPair)");
    if (trace != null) trace.args(existingShares, wantedSharesL, folderPair);
    //System.out.println("getFolderShareChanges() ENTER");
    ArrayList existingSharesL = new ArrayList();
    //Vector wantedSharesV = new Vector();
    ArrayList toRemoveL = new ArrayList();
    ArrayList toChangeL = new ArrayList();
    ArrayList toAddL = new ArrayList();

    // populate existing and wanted vectors
    existingSharesL.addAll(Arrays.asList(existingShares));
    //wantedSharesV.addAll(Arrays.asList(wantedShares));

    // sanity check the sizes of vectors
    if (existingSharesL.size() != existingShares.length)
      throw new IllegalStateException("Error populating vector of existing shares.");
    //if (wantedSharesV.size() != wantedShares.length)
      //throw new IllegalStateException("Error populating vector of wanted shares.");

    // go through all wanted shares and see if there are any NEW shares
    for (int i=0; i<wantedSharesL.size(); i++) {
      FolderShareRecord wantedShare = (FolderShareRecord) wantedSharesL.get(i);
      FolderShareRecord wantedShareClone = (FolderShareRecord) wantedShare.clone();
      wantedShareClone.folderId = folderPair.getId();
      FolderShareRecord existingShare = null;
      boolean found = false;
      for (int k=0; k<existingSharesL.size(); k++) {
        existingShare = (FolderShareRecord) existingSharesL.get(k);
        if (existingShare.folderId.equals(wantedShareClone.folderId) && existingShare.ownerUserId.equals(wantedShareClone.ownerUserId)) {
          found = true;
          existingSharesL.remove(k);
          break;
        }
      }
      if (!found) {
        wantedShareClone.setSymmetricKey(folderPair.getFolderShareRecord().getSymmetricKey());
        wantedShareClone.setViewParentId(folderPair.getFileViewParentId());
        toAddL.add(wantedShareClone);
      } else {
        // if found, see if the share changed
        if (existingShare.canDelete.equals(wantedShareClone.canDelete) && existingShare.canWrite.equals(wantedShareClone.canWrite)) {
          // identical
          //System.out.println("Identical");
          //System.out.println("Existing="+existingShare);
          //System.out.println("Wanted="+wantedShareClone);
        } else {
          // changed
          //System.out.println("Changed");
          //System.out.println("Existing="+existingShare);
          //System.out.println("Wanted="+wantedShareClone);
          FolderShareRecord changedShare = (FolderShareRecord) existingShare.clone();
          changedShare.canDelete = wantedShareClone.canDelete;
          changedShare.canWrite = wantedShareClone.canWrite;
          toChangeL.add(changedShare);
          //System.out.println("Changed="+changedShare);
        }
      }
    }

    // those left in the existing set are to be removed because they are not present in the wanted set
    for (int i=0; i<existingSharesL.size(); i++) {
      toRemoveL.add(((FolderShareRecord) existingSharesL.get(i)).clone());
    }

    // pack the resulting vectors into return structure
    FolderShareRecord[][] sharesDiff = null;
    if (toRemoveL.size() > 0 || toChangeL.size() > 0 || toAddL.size() > 0) {
      sharesDiff = new FolderShareRecord[3][];
      sharesDiff[0] = (FolderShareRecord[]) ArrayUtils.toArray(toRemoveL, FolderShareRecord.class);
      sharesDiff[1] = (FolderShareRecord[]) ArrayUtils.toArray(toChangeL, FolderShareRecord.class);
      sharesDiff[2] = (FolderShareRecord[]) ArrayUtils.toArray(toAddL, FolderShareRecord.class);
    }
    //System.out.println("getFolderShareChanges() EXIT");
    if (trace != null) trace.exit(FolderShareOps.class, sharesDiff);
    return sharesDiff;
  }

}
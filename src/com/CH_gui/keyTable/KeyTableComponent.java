/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_gui.keyTable;

import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.*;

import com.CH_gui.table.*;

/** 
 * <b>Copyright</b> &copy; 2001-2013
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
 * <b>$Revision: 1.15 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class KeyTableComponent extends RecordTableComponent {


  /** Creates new KeyTableComponent */
  public KeyTableComponent(boolean suppressToolbar, boolean suppressUtilityBar, boolean suppressVisualsSavable) {
    super(new KeyActionTable(), null, null, null, suppressToolbar, suppressUtilityBar, suppressVisualsSavable);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(KeyTableComponent.class, "KeyTableComponent()");
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    setTitle(cache.getFolderRecord(cache.getUserRecord().keyFolderId));
    if (trace != null) trace.exit(KeyTableComponent.class);
  }

  /** Creates new KeyTableComponent */
  public KeyTableComponent(KeyRecord[] initialData, boolean suppressToolbar, boolean suppressUtilityBar, boolean suppressVisualsSavable) {
    super(new KeyActionTable(initialData), null, null, null, suppressToolbar, suppressUtilityBar, suppressVisualsSavable);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(KeyTableComponent.class, "KeyTableComponent(KeyRecord[] initialData)");
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    UserRecord myUser = cache.getUserRecord();
    if (myUser != null)
      setTitle(cache.getFolderRecord(myUser.keyFolderId));
    if (trace != null) trace.exit(KeyTableComponent.class);
  }


  /**
   * Refresh keys from all folders.
   */
  public void refreshPressed() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(KeyTableComponent.class, "refreshPressed()");
    //((KeyActionTable) getActionTable()).refreshPressed();
    if (trace != null) trace.exit(KeyTableComponent.class);
  }

  /**
   * This call is currently ignored as keys are only displayed for all folders at once.
   */
  public void initDataModel(Long folderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(KeyTableComponent.class, "initDataModel(Long folderId)");
    if (trace != null) trace.args(folderId);
    //((KeyTableModel) getActionTable().getTableModel()).initData(folderId);
    if (trace != null) trace.exit(KeyTableComponent.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "KeyTableComponent";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
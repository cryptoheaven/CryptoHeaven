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

package com.CH_gui.frame;

import com.CH_cl.service.cache.CacheFldUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_co.service.records.FolderPair;
import com.CH_co.service.records.FolderRecord;
import com.CH_co.service.records.filters.RecordFilter;
import com.CH_co.trace.Trace;
import com.CH_gui.actionGui.JActionFrameClosable;
import com.CH_gui.tree.FolderTreeComponent;
import com.CH_gui.tree.FolderTreeComponent4Frame;
import java.awt.BorderLayout;

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
 * <b>$Revision: 1.16 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FolderTreeFrame extends JActionFrameClosable {

  /** Creates new FolderTreeFrame */
  public FolderTreeFrame(RecordFilter recordFilter) {
    super(com.CH_cl.lang.Lang.rb.getString("title_Folders"), true, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeFrame.class, "FolderTreeFrame(FolderFilter folderFilter)");
    if (trace != null) trace.args(recordFilter);

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    FolderRecord[] fRecs = cache.getFolderRecords();
    FolderPair[] fPairs = CacheFldUtils.convertRecordsToPairs(fRecs);
    FolderTreeComponent mainComponent = new FolderTreeComponent4Frame(true, recordFilter, fPairs, true);
    setMainTreeComponent(mainComponent);
    this.getContentPane().add(mainComponent, BorderLayout.CENTER);

    // all JActionFrames already size themself
    setVisible(true);

    if (trace != null) trace.exit(FolderTreeFrame.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "FolderTreeFrame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
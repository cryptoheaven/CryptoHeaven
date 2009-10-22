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

package com.CH_gui.frame;

import java.awt.*;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;

import com.CH_cl.service.cache.*;

import com.CH_gui.actionGui.JActionFrameClosable;
import com.CH_gui.tree.*;

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
 * <b>$Revision: 1.16 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FolderTreeFrame extends JActionFrameClosable {

  /** Creates new FolderTreeFrame */
  public FolderTreeFrame(RecordFilter recordFilter) {
    super(com.CH_gui.lang.Lang.rb.getString("title_Folders"), true, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeFrame.class, "FolderTreeFrame(FolderFilter folderFilter)");
    if (trace != null) trace.args(recordFilter);

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    FolderRecord[] fRecs = cache.getFolderRecords();
    FolderPair[] fPairs = CacheUtilities.convertRecordsToPairs(fRecs);
    FolderTreeComponent mainComponent = new FolderTreeComponent(true, recordFilter, fPairs);
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
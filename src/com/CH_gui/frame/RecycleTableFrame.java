/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.frame;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.TextRenderer;
import com.CH_co.service.records.FolderPair;
import com.CH_co.trace.Trace;
import com.CH_gui.recycleTable.RecycleTableComponent;
import java.awt.BorderLayout;

/**
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.2 $</b>
*
* @author  Marcin Kurzawa
*/
public class RecycleTableFrame extends RecordTableFrame {

  /** Creates new RecycleTableFrame */
  public RecycleTableFrame(FolderPair folderPair) {
    super(TextRenderer.getFolderAndShareNames(FetchedDataCache.getSingleInstance(), folderPair, true), true, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileTableFrame.class, "RecycleTableFrame(FolderPair folderPair)");
    if (trace != null) trace.args(folderPair);

    mainTableComponent = new RecycleTableComponent(false, false, false);
    mainTableComponent.initData(folderPair.getId());
    this.getContentPane().add(mainTableComponent, BorderLayout.CENTER);

    // all JActionFrames already size themself
    setVisible(true);

    if (trace != null) trace.exit(RecycleTableFrame.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "RecycleTableFrame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
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

import com.CH_cl.service.cache.TextRenderer;
import com.CH_co.service.records.FolderPair;
import com.CH_co.trace.Trace;
import com.CH_gui.groupTable.GroupTableComponent4Frame;
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
* <b>$Revision: 1.3 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class GroupTableFrame extends RecordTableFrame {

  /** Creates new GroupTableFrame */
  public GroupTableFrame(FolderPair folderPair) {
    super(TextRenderer.getFolderAndShareNames(folderPair, true), true, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GroupTableFrame.class, "GroupTableFrame(FolderPair folderPair)");
    if (trace != null) trace.args(folderPair);

    mainTableComponent = new GroupTableComponent4Frame();
    mainTableComponent.initData(folderPair.getId());
    this.getContentPane().add(mainTableComponent, BorderLayout.CENTER);

    // all JActionFrames already size themself
    setVisible(true);

    if (trace != null) trace.exit(GroupTableFrame.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "GroupTableFrame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
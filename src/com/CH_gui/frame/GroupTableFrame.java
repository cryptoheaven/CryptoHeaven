/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
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

import java.awt.*;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.*;

import com.CH_gui.actionGui.JActionFrameClosable;
import com.CH_gui.groupTable.*;
import com.CH_gui.tree.FolderTree;

/**
 * <b>Copyright</b> &copy; 2001-2012
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
public class GroupTableFrame extends JActionFrameClosable {

  /** Creates new GroupTableFrame */
  public GroupTableFrame(FolderPair folderPair) {
    super(FolderTree.getFolderAndShareNames(folderPair, true), true, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GroupTableFrame.class, "GroupTableFrame(FolderPair folderPair)");
    if (trace != null) trace.args(folderPair);

    GroupTableComponent mainComponent = new GroupTableComponent4Frame();
    mainComponent.initData(folderPair.getId());
    this.getContentPane().add(mainComponent, BorderLayout.CENTER);

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
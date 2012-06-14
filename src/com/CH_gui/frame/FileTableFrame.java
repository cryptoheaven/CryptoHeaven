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

import com.CH_cl.service.cache.TextRenderer;
import com.CH_co.service.records.FolderPair;
import com.CH_co.trace.Trace;
import com.CH_gui.actionGui.JActionFrameClosable;
import com.CH_gui.fileTable.FileTableComponent;
import java.awt.BorderLayout;

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
 * <b>$Revision: 1.13 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FileTableFrame extends JActionFrameClosable {

  /** Creates new FileTableFrame */
  public FileTableFrame(FolderPair folderPair) {
    super(TextRenderer.getFolderAndShareNames(folderPair, true), true, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileTableFrame.class, "FileTableFrame(FolderPair folderPair)");
    if (trace != null) trace.args(folderPair);

    FileTableComponent mainComponent = new FileTableComponent(false, false, false);
    mainComponent.initData(folderPair.getId());
    this.getContentPane().add(mainComponent, BorderLayout.CENTER);

    // all JActionFrames already size themself
    setVisible(true);

    if (trace != null) trace.exit(FileTableFrame.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "FileTableFrame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
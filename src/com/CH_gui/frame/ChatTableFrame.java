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

package com.CH_gui.frame;

import java.awt.Frame;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_gui.util.OpenChatFolders;

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
 * <b>$Revision: 1.16 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class ChatTableFrame extends PostTableFrame {

  private Long registeredFolderId;

  /** Creates new ChatTableFrame */
  public ChatTableFrame(FolderPair folderPair) {
    this(folderPair, Frame.NORMAL);
  }
  public ChatTableFrame(FolderPair folderPair, int initialState) {
    super(folderPair, initialState);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ChatTableFrame.class, "ChatTableFrame(FolderPair folderPair, int initialState)");
    if (trace != null) trace.args(folderPair);
    if (trace != null) trace.args(initialState);
    registeredFolderId = folderPair.getId();
    OpenChatFolders.setOpenChatFolder(registeredFolderId, this);
    if (trace != null) trace.exit(ChatTableFrame.class);
  }

  public void closeFrame() {
    OpenChatFolders.clearOpenChatFolder(registeredFolderId, this);
    super.closeFrame();
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "ChatTableFrame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
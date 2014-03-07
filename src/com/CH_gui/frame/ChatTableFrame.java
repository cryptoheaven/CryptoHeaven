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

import java.awt.Frame;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_gui.util.OpenChatFolders;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.16 $</b>
 *
 * @author  Marcin Kurzawa
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
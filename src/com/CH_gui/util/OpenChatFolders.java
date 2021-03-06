/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.util;

import com.CH_co.util.MultiHashtable;
import java.awt.Component;
import java.util.Collection;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class OpenChatFolders {

  // When update comes to the chatting folder, keep track of open Chat Frames or chat Components
  private static final MultiHashtable openChatFolders = new MultiHashtable(); // use synchronized version of this class

  public static Component getOpenChatFolder(Long folderId) {
    return (Component) openChatFolders.get(folderId);
  }
  public static Collection getOpenChatFolders(Long folderId) {
    return openChatFolders.getAll(folderId);
  }
  public static boolean isOpenChatFolder(Long folderId) {
    return openChatFolders.get(folderId) != null;
  }
  public static void setOpenChatFolder(Long folderId, Component comp) {
    openChatFolders.put(folderId, comp);
  }
  public static void clearOpenChatFolder(Long folderId, Component comp) {
    openChatFolders.remove(folderId, comp);
  }

}
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

import com.CH_co.service.records.FolderPair;
import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.13 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class PostTableFrame extends MsgTableFrame {

  /** Creates new PostTableFrame */
  public PostTableFrame(FolderPair folderPair) {
    super(folderPair);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PostTableFrame.class, "PostTableFrame(FolderPair folderPair)");
    if (trace != null) trace.args(folderPair);
    if (trace != null) trace.exit(PostTableFrame.class);
  }

  /** Creates new PostTableFrame */
  public PostTableFrame(FolderPair folderPair, int initialState) {
    super(folderPair, initialState);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PostTableFrame.class, "PostTableFrame(FolderPair folderPair, int initialState)");
    if (trace != null) trace.args(folderPair);
    if (trace != null) trace.args(initialState);
    if (trace != null) trace.exit(PostTableFrame.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "PostTableFrame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
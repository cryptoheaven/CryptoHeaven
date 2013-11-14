/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
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
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.2 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class WhiteListTableFrame extends AddressTableFrame {

  /** Creates new WhiteListTableFrame */
  public WhiteListTableFrame(FolderPair folderPair) {
    super(folderPair);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WhiteListTableFrame.class, "WhiteListTableFrame()");
    if (trace != null) trace.exit(WhiteListTableFrame.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "WhiteListTableFrame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
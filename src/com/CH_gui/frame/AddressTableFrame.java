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
 * <b>$Revision: 1.6 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class AddressTableFrame extends MsgTableFrame {

  /** Creates new AddressTableFrame */
  public AddressTableFrame(FolderPair folderPair) {
    super(folderPair);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AddressTableFrame.class, "AddressTableFrame(FolderPair folderPair)");
    if (trace != null) trace.args(folderPair);
    if (trace != null) trace.exit(AddressTableFrame.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "AddressTableFrame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
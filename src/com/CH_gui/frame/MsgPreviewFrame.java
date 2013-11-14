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

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;

import com.CH_gui.table.*;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.7 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class MsgPreviewFrame extends MsgTableFrame {

  /** Creates new MsgPreviewFrame */
  public MsgPreviewFrame(Record parent, MsgLinkRecord[] initialData) {
    super(parent, initialData);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPreviewFrame.class, "MsgPreviewFrame(Record parent, MsgLinkRecord[] initialData)");
    if (trace != null) trace.args(parent, initialData);
    if (trace != null) trace.exit(MsgPreviewFrame.class);
  }

  /** Creates new MsgPreviewFrame */
  public MsgPreviewFrame(Record parent, MsgLinkRecord[] initialData, RecordTableScrollPane scrollPane) {
    super(parent, initialData, scrollPane);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPreviewFrame.class, "MsgPreviewFrame(Record parent, MsgLinkRecord[] initialData, RecordTableScrollPane scrollPane)");
    if (trace != null) trace.args(parent, initialData, scrollPane);
    if (trace != null) trace.exit(MsgPreviewFrame.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "MsgPreviewFrame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
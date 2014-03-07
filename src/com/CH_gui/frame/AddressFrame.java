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

import com.CH_co.nanoxml.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.6 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class AddressFrame extends MessageFrame {

  /** Creates new AddressFrame */
  public AddressFrame(MsgLinkRecord draftMsgLink, boolean isDeleteDraftAfterSave) {
    super(MsgDataRecord.OBJ_TYPE_ADDR, draftMsgLink, isDeleteDraftAfterSave);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AddressFrame.class, "AddressFrame(MsgLinkRecord draftMsgLink, boolean isDeleteDraftAfterSave)");
    if (trace != null) trace.args(draftMsgLink);
    if (trace != null) trace.args(isDeleteDraftAfterSave);
    if (trace != null) trace.exit(AddressFrame.class);
  }

  /** Creates new AddressFrame */
  public AddressFrame(Record initialRecipient) {
    super(new Record[] { initialRecipient }, MsgDataRecord.OBJ_TYPE_ADDR);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AddressFrame.class, "AddressFrame(Record initialRecipient)");
    if (trace != null) trace.args(initialRecipient);
    if (trace != null) trace.exit(AddressFrame.class);
  }

  /** Creates new AddressFrame */
  public AddressFrame(Record[] initialRecipients) {
    super(initialRecipients, MsgDataRecord.OBJ_TYPE_ADDR);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AddressFrame.class, "AddressFrame(Record[] initialRecipients)");
    if (trace != null) trace.args(initialRecipients);
    if (trace != null) trace.exit(AddressFrame.class);
  }

  /** Creates new AddressFrame */
  public AddressFrame(String title, Record[] initialRecipients, XMLElement draftData) {
    super(title, initialRecipients, draftData, MsgDataRecord.OBJ_TYPE_ADDR);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AddressFrame.class, "AddressFrame(String title, Record[] initialRecipients, XMLElement draftData)");
    if (trace != null) trace.args(title, initialRecipients, draftData);
    if (trace != null) trace.exit(AddressFrame.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "AddressFrame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
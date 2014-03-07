/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.msgs;

import javax.swing.event.*;

import com.CH_co.trace.Trace;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.5 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class MsgUndoableEditListener extends Object implements UndoableEditListener {

  private UndoManagerI undoMngrI;

  /** Creates new MsgUndoableEditListener */
  public MsgUndoableEditListener(UndoManagerI undoMngrI) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgUndoableEditListener.class, "MsgUndoableEditListener(UndoManagerI undoMngrI)");
    this.undoMngrI = undoMngrI;
    if (trace != null) trace.exit(MsgUndoableEditListener.class);
  }

  public void undoableEditHappened(UndoableEditEvent uee) {
    if (uee != null) 
      undoMngrI.getUndoManager().addEdit(uee.getEdit());
    undoMngrI.setEnabledUndoAndRedo();
  }

}
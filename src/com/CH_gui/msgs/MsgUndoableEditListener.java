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

package com.CH_gui.msgs;

import javax.swing.event.*;

import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.5 $</b>
 * @author  Marcin Kurzawa
 * @version
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
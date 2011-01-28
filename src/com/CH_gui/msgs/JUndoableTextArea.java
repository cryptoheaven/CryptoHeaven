/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
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

import com.CH_gui.gui.JMyTextArea;

/**
 * <b>Copyright</b> &copy; 2001-2011
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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class JUndoableTextArea extends JMyTextArea {

  /** Creates new JUndoableTextArea */
  public JUndoableTextArea(String s, int rows, int columns, UndoManagerI undoMngrI) {
    super(s, rows, columns);
    init(undoMngrI);
  }

  /** Creates new JUndoableTextArea */
  public JUndoableTextArea(int rows, int columns, UndoManagerI undoMngrI) {
    super(rows, columns);
    init(undoMngrI);
  }

  /** Creates new JUndoableTextArea */
  public JUndoableTextArea(String s, UndoManagerI undoMngrI) {
    super(s);
    init(undoMngrI);
  }

  /** Creates new JUndoableTextArea */
  public JUndoableTextArea(UndoManagerI undoMngrI) {
    super();
    init(undoMngrI);
  }

  private void init(UndoManagerI undoMngrI) {
    if (undoMngrI != null)
      getDocument().addUndoableEditListener(new MsgUndoableEditListener(undoMngrI));
  }

}
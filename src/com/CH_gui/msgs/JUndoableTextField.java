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

import com.CH_guiLib.gui.*;

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
 * <b>$Revision: 1.5 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class JUndoableTextField extends JMyTextField {

  /** Creates new JUndoableTextField */
  public JUndoableTextField(UndoManagerI undoMngrI) {
    super();
    init(undoMngrI);
  }

  /** Creates new JUndoableTextField */
  public JUndoableTextField(int columns, UndoManagerI undoMngrI) {
    super(columns);
    init(undoMngrI);
  }


  /** Creates new JUndoableTextField */
  public JUndoableTextField(String s, int columns, UndoManagerI undoMngrI) {
    super(s, columns);
    init(undoMngrI);
  }

  /** Creates new JUndoableTextField */
  public JUndoableTextField(String s, UndoManagerI undoMngrI) {
    super(s);
    init(undoMngrI);
  }

  private void init(UndoManagerI undoMngrI) {
    if (undoMngrI != null)
      getDocument().addUndoableEditListener(new MsgUndoableEditListener(undoMngrI));
  }

}
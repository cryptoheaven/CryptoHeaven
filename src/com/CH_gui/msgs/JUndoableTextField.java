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

import com.CH_guiLib.gui.*;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.5 $</b>
 *
 * @author  Marcin Kurzawa
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
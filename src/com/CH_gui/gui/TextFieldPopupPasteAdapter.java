/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_gui.gui;

import com.CH_co.util.*;
import com.CH_gui.action.*;
import com.CH_gui.util.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class TextFieldPopupPasteAdapter extends MouseAdapter {

  public void mouseClicked(MouseEvent e) {
    if (!e.isConsumed() && SwingUtilities.isRightMouseButton(e)) {
      Object source = e.getSource();
      if (source instanceof JTextComponent) {
        JTextComponent textComp = (JTextComponent) source;
        JPopupMenu jPopup = new JPopupMenu();
        JMenuItem jItem = new JMenuItem(new PasteAction(textComp));
        jPopup.add(jItem);
        jPopup.show(textComp, e.getX(), e.getY());
        e.consume();
      }
    }
  }

  private static class PasteAction extends AbstractActionTraced {
    private JTextComponent textComp;
    public PasteAction(JTextComponent textComp) {
      this.textComp = textComp;
      putValue(Actions.NAME, com.CH_gui.lang.Lang.rb.getString("action_Paste"));
      putValue(Actions.MENU_ICON, Images.get(ImageNums.PASTE16));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Paste"));
    }
    public void actionPerformedTraced(ActionEvent e) {
      textComp.paste();
    }
  }

}
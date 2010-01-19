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

package com.CH_gui.dialog;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.CH_co.gui.*;
import com.CH_co.util.*;
import com.CH_co.trace.Trace;

import com.CH_gui.gui.*;

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
 * <b>$Revision: 1.3 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class MouseTextEntryDialog extends GeneralDialog implements VisualsSavable {

  private static final int DEFAULT_OK_BUTTON_INDEX = 0;
  private static final int DEFAULT_CANCEL_BUTTON_INDEX = 1;

  private JButton okButton;
  private JButton cancelButton;

  private MouseTextEntry mouseTextEntry;

  private boolean isOKeyed;

  /** Creates new MouseTextEntryDialog */
  public MouseTextEntryDialog(Dialog parent, String initialStr) {
    super(parent, com.CH_gui.lang.Lang.rb.getString("title_Virtual_Keyboard"));
    init(parent, initialStr);
  }
  public MouseTextEntryDialog(Frame parent, String initialStr) {
    super(parent, com.CH_gui.lang.Lang.rb.getString("title_Virtual_Keyboard"));
    init(parent, initialStr);
  }
  private void init(Component parent, String initialStr) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MouseTextEntryDialog.class, "init(Component parent, String initialStr)");
    if (trace != null) trace.args(parent, initialStr);

    JButton[] buttons = createButtons();
    mouseTextEntry = createMainPanel(initialStr);

    setModal(true);
    super.init(parent, buttons, mouseTextEntry, DEFAULT_OK_BUTTON_INDEX, DEFAULT_CANCEL_BUTTON_INDEX);

    if (trace != null) trace.exit(MouseTextEntryDialog.class);
  }


  private JButton[] createButtons() {
    JButton[] buttons = new JButton[2];
    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
    buttons[0].setDefaultCapable(true);
    buttons[0].addActionListener(new OKActionListener());
    okButton = buttons[0];

    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[1].addActionListener(new CancelActionListener());
    cancelButton = buttons[1];

    return buttons;
  }

  private MouseTextEntry createMainPanel(String initialStr) {
    MouseTextEntry panel = new MouseTextEntry(initialStr);
    return panel;
  }


  private class OKActionListener implements ActionListener {
    public void actionPerformed (ActionEvent event) {
      isOKeyed = true;
      closeDialog();
    }
  }

  private class CancelActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      closeDialog();
    }
  }

  public char[] getPass() {
    return mouseTextEntry.getPass();
  }

  public boolean isOKeyed() {
    return isOKeyed;
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "MouseTextEntryDialog";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
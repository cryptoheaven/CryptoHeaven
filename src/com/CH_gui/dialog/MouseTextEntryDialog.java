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

package com.CH_gui.dialog;

import com.CH_co.trace.Trace;
import com.CH_gui.gui.*;
import com.CH_gui.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
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
public class MouseTextEntryDialog extends GeneralDialog {

  private static final int DEFAULT_OK_BUTTON_INDEX = 0;
  private static final int DEFAULT_CANCEL_BUTTON_INDEX = 1;

  private JButton okButton;
  private JButton cancelButton;

  private MouseTextEntry mouseTextEntry;

  private boolean isOKeyed;
  private JTextComponent editingTextComp;

  /** Creates new MouseTextEntryDialog */
  public MouseTextEntryDialog(Dialog parent, JTextComponent textComp) {
    super(parent, com.CH_cl.lang.Lang.rb.getString("title_Virtual_Keyboard"));
    this.editingTextComp = textComp;
    init(parent, textComp);
  }
  public MouseTextEntryDialog(Frame parent, JTextComponent textComp) {
    super(parent, com.CH_cl.lang.Lang.rb.getString("title_Virtual_Keyboard"));
    this.editingTextComp = textComp;
    init(parent, textComp);
  }
  private void init(Component parent, JTextComponent textComp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MouseTextEntryDialog.class, "init(Component parent, TextComponent textComp)");
    if (trace != null) trace.args(parent, textComp);

    String initialText = "";
    try {
      initialText = textComp.getText();
    } catch (Exception e) {
    }

    JButton[] buttons = createButtons();
    mouseTextEntry = createMainPanel(initialText);

    super.init(parent, buttons, mouseTextEntry, DEFAULT_OK_BUTTON_INDEX, DEFAULT_CANCEL_BUTTON_INDEX);

    if (trace != null) trace.exit(MouseTextEntryDialog.class);
  }


  private JButton[] createButtons() {
    JButton[] buttons = new JButton[2];
    buttons[0] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_OK"));
    buttons[0].setDefaultCapable(true);
    buttons[0].addActionListener(new OKActionListener());
    okButton = buttons[0];

    buttons[1] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Cancel"));
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
      editingTextComp.setText(new String(getPass()));
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

}
/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
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

import com.CH_gui.util.VisualsSavable;
import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.JMyButton;
import com.CH_gui.gui.MyInsets;
import com.CH_gui.util.GeneralDialog;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.CH_cl.service.cache.*;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;

import com.CH_gui.list.*;
import com.CH_gui.statTable.*;

/** 
 * <b>Copyright</b> &copy; 2001-2013
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
 * <b>$Revision: 1.16 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class StatHistoryDialog extends GeneralDialog implements VisualsSavable {

  private static final int DEFAULT_BUTTON_INDEX = 0;

  /** Creates new StatHistoryDialog */
  public StatHistoryDialog(Frame owner, Record parentObjLink) {
    super(owner, getTitle(parentObjLink));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StatHistoryDialog.class, "StatHistoryDialog()");
    initialize(owner, parentObjLink);
    if (trace != null) trace.exit(StatHistoryDialog.class);
  }
  public StatHistoryDialog(Dialog owner, Record parentObjLink) {
    super(owner, getTitle(parentObjLink));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StatHistoryDialog.class, "StatHistoryDialog()");
    initialize(owner, parentObjLink);
    if (trace != null) trace.exit(StatHistoryDialog.class);
  }

  private static String getTitle(Record parentObjLink) {
    if (parentObjLink instanceof MsgLinkRecord) {
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      MsgDataRecord msgData = cache.getMsgDataRecord(((MsgLinkRecord) parentObjLink).msgId);
      if (msgData != null && msgData.isTypeAddress())
        return com.CH_cl.lang.Lang.rb.getString("title_Address_Access_History");
      else
        return com.CH_cl.lang.Lang.rb.getString("title_Message_Access_History");
    } else {
      return com.CH_cl.lang.Lang.rb.getString("title_File_Access_History");
    }
  }

  private void initialize(Component owner, Record parentObjLink) {
    JPanel panel = createMainPanel(parentObjLink);
    JButton[] buttons = createButtons();
    super.init(owner, buttons, panel, DEFAULT_BUTTON_INDEX, DEFAULT_BUTTON_INDEX);
  }


  private JButton[] createButtons() {
    JButton[] buttons = new JButton[1];

    buttons[0] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Close"));
    buttons[0].setDefaultCapable(true);
    buttons[0].addActionListener(new CloseActionListener());

    return buttons;
  }

  private JPanel createMainPanel(Record parentObjLink) {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    int posY = 0;
    JLabel label = new JMyLabel(ListRenderer.getRenderedText(parentObjLink));
    label.setIcon(ListRenderer.getRenderedIcon(parentObjLink));
    panel.add(label, new GridBagConstraints(0, posY, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 0, 5), 0, 0));
    posY ++;

    StatTableComponent statComponent = new StatTableComponent(parentObjLink);
    panel.add(statComponent, new GridBagConstraints(0, posY, 1, 1, 10, 10, 
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 5, 5, 5), 0, 0));

    return panel;
  }


  private class CloseActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      closeDialog();
    }
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "StatHistoryDialog";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
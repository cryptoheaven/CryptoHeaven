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

import com.CH_gui.util.VisualsSavable;
import com.CH_gui.util.Images;
import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.JMyButton;
import com.CH_gui.gui.MyInsets;
import com.CH_gui.util.GeneralDialog;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.CH_cl.service.cache.*;

import com.CH_co.service.msg.dataSets.stat.Stats_Get_Rp;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.actionGui.*;
import com.CH_gui.list.*;
import com.CH_gui.traceTable.*;

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
 * <b>$Revision: 1.19 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class TraceRecordDialog extends GeneralDialog implements VisualsSavable {

  private static final int DEFAULT_BUTTON_INDEX = 0;
  private JLabel completenessLabel = null;

  /** Creates new TraceRecordDialog */
  public TraceRecordDialog(Frame owner, Record[] parentObjLinks) {
    super(owner, getTitle(parentObjLinks));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TraceRecordDialog.class, "TraceRecordDialog()");
    initialize(owner, parentObjLinks);
    if (trace != null) trace.exit(TraceRecordDialog.class);
  }
  public TraceRecordDialog(Dialog owner, Record[] parentObjLinks) {
    super(owner, getTitle(parentObjLinks));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TraceRecordDialog.class, "TraceRecordDialog()");
    initialize(owner, parentObjLinks);
    if (trace != null) trace.exit(TraceRecordDialog.class);
  }

  private static String getTitle(Record[] parentObjLinks) {
    if (parentObjLinks.length == 1) {
      if (parentObjLinks[0] instanceof MsgLinkRecord) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        MsgDataRecord msgData = cache.getMsgDataRecord(((MsgLinkRecord) parentObjLinks[0]).msgId);
        if (msgData != null && msgData.isTypeAddress())
          return com.CH_cl.lang.Lang.rb.getString("title_Address_Access_Trace");
        else
          return com.CH_cl.lang.Lang.rb.getString("title_Message_Access_Trace");
      } else if (parentObjLinks[0] instanceof FileLinkRecord) {
        return com.CH_cl.lang.Lang.rb.getString("title_File_Access_Trace");
      }
    }
    return com.CH_cl.lang.Lang.rb.getString("title_Access_Trace");
  }

  private void initialize(Component owner, Record[] parentObjLinks) {
    JPanel panel = createMainPanel(parentObjLinks);
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

  private JPanel createMainPanel(Record[] parentObjLinks) {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    int posY = 0;

    if (parentObjLinks.length == 1) {
      JLabel label = new JMyLabel(ListRenderer.getRenderedText(parentObjLinks[0]));
      label.setIcon(ListRenderer.getRenderedIcon(parentObjLinks[0]));
      panel.add(label, new GridBagConstraints(0, posY, 3, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 0, 5), 0, 0));
      posY ++;
    }

    TraceTableComponent traceComponent = new TraceTableComponent(parentObjLinks);
    panel.add(traceComponent, new GridBagConstraints(0, posY, 3, 1, 10, 10,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 5, 5, 5), 0, 0));
    posY ++;

    completenessLabel = new JMyLabel();
    TraceTableModel traceModel = (TraceTableModel) traceComponent.getRecordTableScrollPane().getTableModel();
    traceModel.setStatCallback(new CallbackI() {
      public void callback(Object value) {
        if (((Stats_Get_Rp)value).isAnyBCCskipped) {
          completenessLabel.setIcon(Images.get(ImageNums.WARNING16));
          completenessLabel.setText("BCC message recipients are not shown.");
        } else {
          completenessLabel.setIcon(null);
          completenessLabel.setText("");
        }
      }
    });

    panel.add(completenessLabel, new GridBagConstraints(0, posY, 3, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    TraceActionTable actionTable = (TraceActionTable) traceComponent.getActionTable();
    JButton initContactButton = new JActionButton(actionTable.getInitiateAction(), false, null, false);
//    initContactButton.setText((String) actionTable.getInitiateAction().getValue(Actions.NAME));
//    initContactButton.setHorizontalTextPosition(JButton.RIGHT);
//    initContactButton.setVerticalTextPosition(JButton.CENTER);

    JButton msgUserButton = new JActionButton(actionTable.getMessageAction(), false, null, false);
//    msgUserButton.setText((String) actionTable.getMessageAction().getValue(Actions.NAME));
//    msgUserButton.setHorizontalTextPosition(JButton.RIGHT);
//    msgUserButton.setVerticalTextPosition(JButton.CENTER);

    // Filler and two buttons in one row.
    panel.add(new JMyLabel(), new GridBagConstraints(0, posY, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(initContactButton, new GridBagConstraints(1, posY, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(msgUserButton, new GridBagConstraints(2, posY, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));

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
  public static final String visualsClassKeyName = "TraceRecordDialog";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
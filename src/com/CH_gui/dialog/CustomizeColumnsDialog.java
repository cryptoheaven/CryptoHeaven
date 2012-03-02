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
import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.JMyButton;
import com.CH_gui.gui.MyInsets;
import com.CH_gui.util.GeneralDialog;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import com.CH_co.util.*;
import com.CH_co.trace.Trace;

import com.CH_gui.list.*;
import com.CH_gui.table.*;

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
 * <b>$Revision: 1.16 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class CustomizeColumnsDialog extends GeneralDialog implements VisualsSavable {

  public boolean pressedOk = false;

  private static final int DEFAULT_OK_INDEX = 0;
  private static final int DEFAULT_CANCEL_INDEX = 1;

  private DualListBox dualListBox;
  private ColumnHeaderData columnHeaderData;

  /** Creates new CustomizeColumnsDialog */
  public CustomizeColumnsDialog(Frame parent, ColumnHeaderData columnHeaderData) {
    super(parent, com.CH_gui.lang.Lang.rb.getString("title_Choose_Columns"));
    init(parent, columnHeaderData);
  }
  /** Creates new CustomizeColumnsDialog */
  public CustomizeColumnsDialog(Dialog parent, ColumnHeaderData columnHeaderData) {
    super(parent, com.CH_gui.lang.Lang.rb.getString("title_Choose_Columns"));
    init(parent, columnHeaderData);
  }
  private void init(Component parent, ColumnHeaderData columnHeaderData) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CustomizeColumnsDialog.class, "init(Component parent, ColumnHeaderData columnHeaderData)");

    this.columnHeaderData = columnHeaderData;

    dualListBox = createDualList();
    JButton[] buttons = createButtons();
    JPanel panel = createMainPanel();
    setModal(true);

    super.init(parent, buttons, panel, DEFAULT_OK_INDEX, DEFAULT_CANCEL_INDEX);

    if (trace != null) trace.exit(CustomizeColumnsDialog.class);
  }

  private DualListBox createDualList() {
    DualListBox dlb = new DualListBox();

    int numOfColumns = columnHeaderData.data[ColumnHeaderData.I_SHORT_DISPLAY_NAMES].length;
    int numOfChosenColumns = columnHeaderData.data[ColumnHeaderData.I_VIEWABLE_SEQUENCE].length;

    // make a vector of JLabels for all columns
    Vector allColumnsV = new Vector(numOfColumns);
    Object[] allObjsTemp = new Object[numOfColumns];
    for (int i=0; i<numOfColumns; i++) {
      String name = (String) columnHeaderData.data[ColumnHeaderData.I_SHORT_DISPLAY_NAMES][i];
      if (name != null) {
        JLabel label = new JMyLabel(name);
        label.setIcon(columnHeaderData.getRawColumnIcon(i));
        allColumnsV.addElement(label);
        allObjsTemp[i] = label;
      }
    }

    // separate items for chosen columns
    Vector chosenColumnsV = new Vector(numOfChosenColumns);
    for (int i=0; i<numOfChosenColumns; i++) {
      int rawColumn = ((Integer) columnHeaderData.data[ColumnHeaderData.I_VIEWABLE_SEQUENCE][i]).intValue();
      chosenColumnsV.addElement(allObjsTemp[rawColumn]);
    }

    Object[] source = new Object[allColumnsV.size()];
    allColumnsV.toArray(source);
    dlb.addDefaultSourceElements(source);

    if (chosenColumnsV.size() > 0) {
      Object[] dest = new Object[chosenColumnsV.size()];
      chosenColumnsV.toArray(dest);
      dlb.moveToDefaultDestinationElements(dest);
    }

    return dlb;
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[2];

    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
    buttons[0].setDefaultCapable(true);
    buttons[0].addActionListener(new OKActionListener());

    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[1].addActionListener(new CancelActionListener());

    return buttons;
  }

  private JPanel createMainPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    panel.add(dualListBox, new GridBagConstraints(0, 0, 1, 1, 10, 10, 
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    return panel;
  }



  /**
   * Apply the choices if OK was pressed.
   * @return true if changes were applied, false otherwise.
   */
  public boolean applyChoices(ColumnHeaderData headerData) {
    if (pressedOk) {
      Object[] chosenColumns = dualListBox.getResult();
      Integer[] newViewableColumns = new Integer[chosenColumns.length];
      for (int i=0; i<chosenColumns.length; i++) {
        String columnName = ((JLabel) chosenColumns[i]).getText();
        newViewableColumns[i] = new Integer(ArrayUtils.find(headerData.data[ColumnHeaderData.I_SHORT_DISPLAY_NAMES], columnName));
      }
      headerData.data[ColumnHeaderData.I_VIEWABLE_SEQUENCE] = newViewableColumns;
    }
    return pressedOk;
  }


  private class OKActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      pressedOk = true;
      closeDialog();
    }
  }


  private class CancelActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      closeDialog();
    }
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "CustomizeColumnsDialog";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
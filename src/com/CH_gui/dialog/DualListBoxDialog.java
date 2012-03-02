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

import com.CH_gui.gui.JMyButton;
import com.CH_gui.util.GeneralDialog;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import com.CH_co.gui.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.gui.*;
import com.CH_gui.list.*;
//import com.CH_gui.msgs.*;

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
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class DualListBoxDialog extends GeneralDialog {

  public static final int DEFAULT_OK_INDEX = 0;
  public static final int DEFAULT_CANCEL_INDEX = 1;

  private DualListBox dualListBox;
  public Object[] selectedObjs;

  /** Creates new DualListBoxDialog */
  public DualListBoxDialog(Dialog owner, String title, String titleBoxLeft, String titleBoxRight, Object[] sourceObjs, Object[] destinationObjs) {
    super(owner, title);
    initialize(owner, titleBoxLeft, titleBoxRight, sourceObjs, destinationObjs);
  }

  private void initialize(Component owner, String titleBoxLeft, String titleBoxRight, Object[] sourceObjs, Object[] destinationObjs) {
    setModal(true);
    JButton[] buttons = createButtons();
    createComponents(titleBoxLeft, titleBoxRight, sourceObjs, destinationObjs);
    init(owner, buttons, dualListBox, DEFAULT_OK_INDEX, DEFAULT_CANCEL_INDEX);
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[2];
    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
    buttons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedOk();
      }
    });
    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedCancel();
      }
    });
    return buttons;
  }

  private void createComponents(String titleBoxLeft, String titleBoxRight, Object[] sourceObjs, Object[] destinationObjs) {
    dualListBox = new DualListBox(true, false, false, true);
    dualListBox.setSourceChoicesTitle(titleBoxLeft);
    dualListBox.setDestinationChoicesTitle(titleBoxRight);
    if (sourceObjs != null && sourceObjs.length > 0 && destinationObjs != null && destinationObjs.length > 0) {
      sourceObjs = ArrayUtils.getDifference(sourceObjs, destinationObjs);
    }
    if (sourceObjs != null && sourceObjs.length > 0) {
      dualListBox.setSourceElements(sourceObjs);
    }
    if (destinationObjs != null && destinationObjs.length > 0) {
      dualListBox.moveToDefaultDestinationElements(destinationObjs);
    }
  }

  private void pressedOk() {
    selectedObjs = dualListBox.getResult();
    dispose();
  }

  private void pressedCancel() {
    selectedObjs = null;
    dispose();
  }

  public Object[] getResult() {
    return selectedObjs;
  }
  
}
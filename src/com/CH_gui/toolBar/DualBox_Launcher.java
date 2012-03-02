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

package com.CH_gui.toolBar;

import com.CH_gui.gui.JMyButton;
import java.awt.Frame;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import com.CH_gui.actionGui.*;
import com.CH_gui.dialog.CustomizeToolbarDialog;
import com.CH_gui.list.*;
import com.CH_gui.menuing.*;

import com.CH_co.gui.*;
import com.CH_co.trace.Trace;
import com.CH_gui.util.GeneralDialog;

/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>  
 *
 * @author  Marcin Kurzawa
 * @version 
 */

/** This class works as a linkage between Menu_ToolBarFrame and DualListBox,
  * i.e. takes data from Menu_ToolBarFrame and uses DualListBox to display it.
  * It displays a dialog where the user can choose tools that will go on the
  * tool bar.
  */
public class DualBox_Launcher extends Object {

  ToolBarModel toolBarModel = null;
  Tools_DualListBox dual = null;
  CustomizeToolbarDialog dialog = null;

  private static final int DEFAULT_BUTTON_INDEX = 0;
  private static final int DEFAULT_CANCEL_BUTTON_INDEX = 1;

  /** Creates new DualBox_Launcher */
  public DualBox_Launcher(Frame parentFrame, ToolBarModel toolBarModel) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DualBox_Launcher.class, "DualBox_Launcher(Frame parentFrame)");
    if (trace != null) trace.args(parentFrame);

    this.toolBarModel = toolBarModel;

    setDualListBox(toolBarModel);

    JButton[] buttons = createButtons();

    dialog = new CustomizeToolbarDialog(parentFrame, com.CH_gui.lang.Lang.rb.getString("title_Customize_Tool_Bar"), buttons, 
                                DEFAULT_BUTTON_INDEX, DEFAULT_CANCEL_BUTTON_INDEX, dual);

    if (trace != null) trace.exit(DualBox_Launcher.class);
  }

  /** Get already existing selections and set both lists in the dual box **/ 
  private void setDualListBox(ToolBarModel toolBarModel) {

    dual = new Tools_DualListBox();
    List_Viewable[] selectedItems = toolBarModel.getAvailableTools(true, false);
    List_Viewable[] otherAvailableItems = toolBarModel.getAvailableTools(false, false);

    for (int i=0; i<selectedItems.length; i++) {
      if (selectedItems[i].getLabel().equals(MenuActionItem.STR_SEPARATOR)) {
        // switch all real separator MenuItemActions to uniquely distinguishable separators
        selectedItems[i] = new UniqueSeparator();
      }
    }
    for (int i=0; i<otherAvailableItems.length; i++) {
      if (otherAvailableItems[i].getLabel().equals(MenuActionItem.STR_SEPARATOR)) {
        // switch all real separator MenuItemActions to uniquely distinguishable separators
        otherAvailableItems[i] = new UniqueSeparator();
      }
    }
    dual.addDefaultSourceElements(otherAvailableItems);
    dual.addDefaultDestinationElements(selectedItems);
  }

  /** Create "ok" and "cancel" buttons **/
  private JButton[] createButtons() {
    JButton okButton = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
    okButton.addActionListener(new OKActionListener());

    JButton cancelButton = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    cancelButton.addActionListener(new CancelActionListener());

    JButton[] buttons = new JButton[2];
    buttons[0] = okButton;
    buttons[1] = cancelButton;

    return buttons;
  }

  /** When the user is done selecting tools, and presses "ok",
    * the tool bar is being updated and the result stored in Properties 
    */
  private class OKActionListener implements ActionListener {
    public void actionPerformed (ActionEvent event) {
      Object[] objects = dual.getResult();

      // switch all fake Separators to real MenuItemActions
      for (int i=0; i<objects.length; i++) {
        if (objects[i] instanceof UniqueSeparator)
          objects[i] = ToolBarModel.makeSeparator();
      }

      List_Viewable[] chosenToolItems = new List_Viewable[objects.length];
      Vector v = new Vector(Arrays.asList(objects));
      v.toArray(chosenToolItems);

      toolBarModel.updateToolBar(chosenToolItems);
      dialog.closeDialog();
    }
  }
  /** Just close the window, do nothing **/
  private class CancelActionListener implements ActionListener {
    public void actionPerformed (ActionEvent event) {
      dialog.closeDialog();
    }
  }
}
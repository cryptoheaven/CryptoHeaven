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

import com.CH_gui.util.GeneralDialog;
import java.awt.*;
import javax.swing.*;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.toolBar.*;

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
 * <b>$Revision: 1.12 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class CustomizeToolbarDialog extends GeneralDialog implements VisualsSavable {

  /** Creates new CustomizeToolbarDialog */
  public CustomizeToolbarDialog(Frame parentFrame, String title, JButton[] buttons, 
                                int defaultButton, int defaultCancel, Tools_DualListBox dual) {
    super(parentFrame, title, buttons, defaultButton, defaultCancel, dual);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CustomizeToolbarDialog.class, "CustomizeToolbarDialog(Frame parentFrame, String title, JButton[] buttons, int defaultButton, int defaultCancel, Tools_DualListBox dual)");
    if (trace != null) trace.args(parentFrame, title, buttons);
    if (trace != null) trace.args(defaultButton);
    if (trace != null) trace.args(defaultCancel);
    if (trace != null) trace.args(dual);
    if (trace != null) trace.exit(CustomizeToolbarDialog.class);
  }

  public String getExtension() {
    Window w = getOwner();
    if (w instanceof VisualsSavable)
      return ((VisualsSavable) w).getVisualsClassKeyName();
    else
      return w.getClass().getName();
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "CustomizeToolbarDialog";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
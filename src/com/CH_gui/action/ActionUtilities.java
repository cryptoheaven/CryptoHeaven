/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_gui.action;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import java.beans.*;
import java.util.*;
import java.util.List;

import com.CH_co.util.*;

import com.CH_gui.actionGui.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>  
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class ActionUtilities extends Object {


  /** 
   * @return concatinated array of actions or one of the arrays if the other is empty.
   */
  public static Action[] concatinate(Action[] a1, Action[] a2) {
    //Action[] a = (Action[]) ArrayUtils.concatinate(super.getActions(), actions);
    if (a2 == null || a2.length == 0)
      return a1;
    else if (a1 == null || a1.length == 0)
      return a2;
    List l1 = Arrays.asList(a1);
    List l2 = Arrays.asList(a2);
    Vector v = new Vector(l1);
    v.addAll(l2);
    Action[] a = (Action[]) ArrayUtils.toArray(v, Action.class);
    return a;
  }


  /**
   * Clears an array of Actions and dissassembles Button Groups if any are found.
   */
  public static void clearActions(Action[] actions) {
    if (actions != null) {
      for (int i=0; i<actions.length; i++) {
        Action a = actions[i];
        ButtonGroup bg = (ButtonGroup) a.getValue(Actions.BUTTON_GROUP);
        if (bg != null) {
          Vector buttonsV = new Vector();
          Enumeration e = bg.getElements();
          while (e.hasMoreElements()) {
            buttonsV.addElement(e.nextElement());
          }
          if (buttonsV.size() > 0) {
            for (int k=0; k<buttonsV.size(); k++) {
              bg.remove((AbstractButton) buttonsV.elementAt(k));
            }
          }

          a.putValue(Actions.BUTTON_GROUP, null);
        }
        actions[i] = null;
      }
    }
  } // end clearActions()


  /**
   * Create small action tool button for component views.
   * Does not handle Radio ToggleButtons -- creates regular action buttons instead
   */
  public static AbstractButton makeSmallComponentToolButton(Action action) {
    AbstractButton aButton = null; 
    Boolean state = (Boolean) action.getValue(Actions.STATE_CHECK);
    if (state != null) {
      aButton = new JActionToggleButtonNoFocus(action, true);
    } else {
      aButton = new JActionButtonNoFocus(action, true);
    }
    aButton.setText("");
    aButton.setIcon((ImageIcon) action.getValue(Actions.MENU_ICON));
    aButton.setBorder(new EmptyBorder(2,2,2,2));
    aButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return aButton;
  }

}
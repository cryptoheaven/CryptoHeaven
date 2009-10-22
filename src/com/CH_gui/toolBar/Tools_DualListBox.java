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

package com.CH_gui.toolBar;

import javax.swing.*;
import java.awt.event.*;

import com.CH_gui.actionGui.JActionFrame;
import com.CH_gui.list.*;
import com.CH_gui.menuing.*;

import com.CH_co.trace.Trace;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 *  The only difference between this class and its parent's class is that 
 *  this class has a separator on the top of source list and the user can
 *  add it to the destination list as many times as desired without removing
 *  it from the source. Also, removal of the separator from the destination
 *  doesn't cause addition to the source.
 *
* Class Details:
 *  Two methods that deal with separators are overriden. 
 *  And separator is added to the source list before all the other elements
 *
 * <b>$Revision: 1.11 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class Tools_DualListBox extends DualListBox {

  /** Creates new Tools_DualListBox */
  public Tools_DualListBox() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Tools_DualListBox.class, "Tools_DualListBox()");

    addDefaultSourceElementsIfNotInLists(new Object[] { ToolBarModel.makeSeparator() }, true);

    if (trace != null) trace.exit(Tools_DualListBox.class);
  }

  /** Dissallow removal of Seperators from the source list.
   */
  /*
  public boolean isElementRemovable(Object obj, boolean fromSource) {
    if (fromSource && isSeparator(obj))
      return false;
    else
      return super.isElementRemovable(obj, fromSource);
  }
   */

  /** Immediately replace the removed Separators from the source list with new different one. */
  public Object getElementRemovalReplacement(Object obj, boolean fromSource) {
    if (fromSource && isSeparator(obj))
      return new UniqueSeparator();
    else
      return super.getElementRemovalReplacement(obj, fromSource);
  }

  /** Dissallow adding of Seperators to the source list if there is one already.
   */
  public boolean isElementAddable(Object obj, boolean toSource) {
    if (toSource && isSeparator(obj))
      return false;
    else
      return super.isElementAddable(obj, toSource);
  }

  /**
   * @return true if the object is acting as a Separator.
   */
  private boolean isSeparator(Object value) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Tools_DualListBox.class, "isSeparator(Object value)");
    if (trace != null) trace.args(value);

    boolean isSeparator = false;
    if (value instanceof JSeparator)
      isSeparator = true;
    else if (value instanceof List_Viewable) {
      List_Viewable list_viewable = (List_Viewable) value;
      if (list_viewable.getLabel().equals(MenuActionItem.STR_SEPARATOR))
        isSeparator = true;
    }

    if (trace != null) trace.exit(Tools_DualListBox.class, isSeparator);
    return isSeparator;
  }

}
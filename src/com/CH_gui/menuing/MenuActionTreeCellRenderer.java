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

package com.CH_gui.menuing;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.*;

import java.awt.Component;

import com.CH_co.trace.Trace;
import com.CH_co.util.Images;

import com.CH_gui.gui.*;
import com.CH_gui.list.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: 1.10 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class MenuActionTreeCellRenderer extends MyDefaultTreeCellRenderer {

  /**
    * Configures the renderer based on the passed in components.
    * The value is set from messaging value with toString().
    * The foreground color is set based on the selection and the icon
    * is set based on selected.
    */
  public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                boolean selected,
                                                boolean expanded,
                                                boolean leaf, int row,
                                                boolean hasFocus) 
  {
    super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

    if (value instanceof DefaultMutableTreeNode) {
      Object userObj = ((DefaultMutableTreeNode)value).getUserObject();
      
      if (userObj instanceof MenuActionItem) {
        MenuActionItem menuActionItem = (MenuActionItem) userObj;
        setIcon(menuActionItem.getIcon());
        String label = menuActionItem.getLabel();

        boolean bracket = false;
        Integer mnemonic = menuActionItem.getMnemonic();
        if (mnemonic != null && mnemonic.intValue() > 0) {
          bracket = true;
          label += " [" + KeyEvent.getKeyText(mnemonic.intValue());
        }
        
        KeyStroke keyStroke = menuActionItem.getKeyStroke();
        if (keyStroke != null && keyStroke.getKeyCode() > 0)
          label += ", " + KeyEvent.getKeyModifiersText(keyStroke.getModifiers()) + "+" + KeyEvent.getKeyText(keyStroke.getKeyCode());
        
        if (bracket)
          label += "]";

        setText(label);
      }
    }
    
    return this;
  }
}
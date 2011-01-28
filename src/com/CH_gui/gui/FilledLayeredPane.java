/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_gui.gui;

import java.awt.*;
import javax.swing.*;


/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: Keeps all components at maximum bounds of this JLayeredPane.
 *
 * <b>$Revision: 1.8 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FilledLayeredPane extends JLayeredPane {

  /** Creates new FilledLayeredPane */
  public FilledLayeredPane() {
    super();
  }

  public void doLayout() {
    Component[] comps = getComponents();
    if (comps != null) {
      Rectangle r = getBounds();
      for (int i=0; i<comps.length; i++) {
        Component c = comps[i];
        c.setBounds(r);
      }
    }
    super.doLayout();
  }

}
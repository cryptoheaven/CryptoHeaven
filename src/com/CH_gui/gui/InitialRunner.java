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

package com.CH_gui.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


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
 * Executes an Runnable object when object is first shown on screen.
 *
 * <b>$Revision: 1.2 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class InitialRunner extends Object implements HierarchyListener {

  private Runnable runner;

  /** Creates new InitialRunner */
  public InitialRunner(Runnable runner) {
    this.runner = runner;
  }

  /**
   * As soon as the component is shown, focus is requested and listener removed.
   */
  public void hierarchyChanged(HierarchyEvent event) {
    final Component c = event.getComponent();
    long changeFlags = event.getChangeFlags();
    if ((changeFlags & (HierarchyEvent.SHOWING_CHANGED | HierarchyEvent.DISPLAYABILITY_CHANGED)) != 0 && 
        c != null && c.isShowing()) 
    {
      c.removeHierarchyListener(this);
      SwingUtilities.invokeLater(runner);
    }
  }

}
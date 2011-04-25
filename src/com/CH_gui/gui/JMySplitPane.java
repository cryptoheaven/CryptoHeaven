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

package com.CH_gui.gui;

import com.CH_gui.util.MiscGui;
import java.awt.Component;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

/**
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class JMySplitPane extends JSplitPane {

  public JMySplitPane(String propertyName, int orientation, double resizeWeight) {
    super(orientation);
    initialize(propertyName, resizeWeight);
  }
  public JMySplitPane(String propertyName, int orientation, Component c1, Component c2, double resizeWeight) {
    super(orientation, c1, c2);
    initialize(propertyName, resizeWeight);
  }
  private void initialize(String propertyName, double resizeWeight) {
    setResizeWeight(resizeWeight);
    setOneTouchExpandable(false);
    if (MiscGui.isSmallScreen())
      setDividerSize(2);
    setBorder(new EmptyBorder(0,0,0,0));
    if (getDividerSize() > 5)
      setDividerSize(5);
  }

}
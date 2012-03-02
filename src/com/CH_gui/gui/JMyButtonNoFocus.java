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

package com.CH_gui.gui;

import javax.swing.*;

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
 * <b>$Revision: 1.3 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class JMyButtonNoFocus extends JMyButton {

  /** Creates new JMyButtonNoFocus */
  public JMyButtonNoFocus() {
    super();
    setRequestFocusEnabled(false);
  }
  /** Creates new JMyButtonNoFocus */
  public JMyButtonNoFocus(Icon icon) {
    super(icon);
    setRequestFocusEnabled(false);
  }
  /** Creates new JMyButtonNoFocus */
  public JMyButtonNoFocus(String text) {
    super(text);
    setRequestFocusEnabled(false);
  }
  /** Creates new JMyButtonNoFocus */
  public JMyButtonNoFocus(String text, Icon icon) {
    super(text, icon);
    setRequestFocusEnabled(false);
  }

  public boolean isFocusTraversable() {
    return false;
  }

}
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

package comx.Jaguar.gui;

import javax.swing.*;

/** 
 * <b>Copyright</b> &copy; 2001-2011
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
 * <b>$Revision: 1.3 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
/** Class for providing a JComboBox that does not obtain focus
    */
public class JComboBoxNoFocus extends JComboBox {
  public JComboBoxNoFocus()                     { super();        this.setRequestFocusEnabled(false); }
  public JComboBoxNoFocus(Object[] items)       { super(items);   this.setRequestFocusEnabled(false); }
  public JComboBoxNoFocus(ComboBoxModel model)  { super(model);   this.setRequestFocusEnabled(false); }

  public boolean isFocusTraversable() {
    return false;
  }
}
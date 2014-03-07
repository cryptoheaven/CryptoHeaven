/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
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
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * Class Description:
 * Class for providing a JToggleButton that does not obtain focus
 *
 * <b>$Revision: 1.4 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class JToggleButtonNoFocus extends JToggleButton {
  public JToggleButtonNoFocus()                       { super();           this.setRequestFocusEnabled(false); this.setMargin(new MyInsets(1,1,1,1));}
  public JToggleButtonNoFocus(Action a)               { super(a);          this.setRequestFocusEnabled(false); this.setMargin(new MyInsets(1,1,1,1));}
  public JToggleButtonNoFocus(Icon icon)              { super(icon);       this.setRequestFocusEnabled(false); this.setMargin(new MyInsets(1,1,1,1));}
  public JToggleButtonNoFocus(String text)            { super(text);       this.setRequestFocusEnabled(false); this.setMargin(new MyInsets(1,1,1,1));}
  public JToggleButtonNoFocus(String text, Icon icon) { super(text, icon); this.setRequestFocusEnabled(false); this.setMargin(new MyInsets(1,1,1,1));}

  public boolean isFocusTraversable() {
    return false;
  }
}
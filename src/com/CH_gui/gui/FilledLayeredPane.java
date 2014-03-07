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

import java.awt.*;
import javax.swing.*;


/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * Class Description: Keeps all components at maximum bounds of this JLayeredPane.
 *
 * <b>$Revision: 1.8 $</b>
 *
 * @author  Marcin Kurzawa
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
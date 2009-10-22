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

package com.CH_guiLib.gui;

import java.awt.*;
import javax.swing.*;

import com.CH_co.util.*;

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
 * <b>$Revision: 1.5 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class JMyComboBox extends JComboBox {

  /** Creates new JMyComboBox */
  public JMyComboBox() {
    super();
    setRenderer(new MyBasicComboBoxRenderer());
  }

  /** Creates new JMyComboBox */
  public JMyComboBox(Object[] items) {
    super(items);
    setRenderer(new MyBasicComboBoxRenderer());
  }

  /** Creates new JMyComboBox */
  public JMyComboBox(ComboBoxModel aModel) {
    super(aModel);
    setRenderer(new MyBasicComboBoxRenderer());
  }
  
  public void paint(Graphics g) {
    MiscGui.setPaintPrefs(g);
    super.paint(g);
  }

}
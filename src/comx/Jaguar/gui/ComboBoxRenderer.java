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

import java.awt.*;
import javax.swing.*;

import java.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team. 
 * </a><br>All rights reserved.<p> 
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class ComboBoxRenderer extends javax.swing.plaf.basic.BasicComboBoxRenderer {

  /** Creates new ComboBoxRenderer */
  public ComboBoxRenderer() {
    setOpaque(true);
  }

  public Component getListCellRendererComponent(
      JList listbox, 
      Object value, 
      int index, 
      boolean isSelected, 
      boolean cellHasFocus) 
  {
    Hashtable h = (Hashtable) value;
    if (UIManager.getLookAndFeel().getName().equals("CDE/Motif")) {
      if (index == -1 )
        setOpaque(false);
      else
        setOpaque(true);
    } else 
      setOpaque(true);

    if (value == null) {
      setText("");
      setIcon(null);
    } else {
      JLabel label = null;
      if (index == -1) {
        label = (JLabel) h.get("topLabel");
        return super.getListCellRendererComponent(listbox, label.getText(), index, isSelected, cellHasFocus);
      } else {
        label = (JLabel) h.get("listLabel");


      setText(label.getText());
      setIcon(label.getIcon());

      if (isSelected) {
        setBackground(UIManager.getColor("ComboBox.selectionBackground"));
        setForeground(UIManager.getColor("ComboBox.selectionForeground"));
      }
      else {
        setBackground(UIManager.getColor("ComboBox.background"));
        setForeground(UIManager.getColor("ComboBox.foreground"));
      }
      }
    }
    return this;
  }
}
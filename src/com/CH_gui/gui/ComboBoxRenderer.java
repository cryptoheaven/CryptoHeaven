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
import java.util.*;

import com.CH_guiLib.gui.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved. 
 *
 * @author  Marcin Kurzawa 
 */
public class ComboBoxRenderer extends MyBasicComboBoxRenderer implements ListCellRenderer {

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
    if(UIManager.getLookAndFeel().getName().equals("CDE/Motif"))
      setOpaque(index != -1);
    else 
      setOpaque(true);

    if (value == null) {
      setText("");
      setIcon(null);
    } else {
      setText((String) h.get("title"));
      setIcon((ImageIcon) h.get("image"));
      if (isSelected) {
        setBackground(UIManager.getColor("ComboBox.selectionBackground"));
        setForeground(UIManager.getColor("ComboBox.selectionForeground"));
      }
      else {
        setBackground(UIManager.getColor("ComboBox.background"));
        setForeground(UIManager.getColor("ComboBox.foreground"));
      }
    }
    return this;
  }

}
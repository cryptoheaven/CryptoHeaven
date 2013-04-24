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

import com.CH_co.util.*;
import com.CH_gui.util.*;
import com.CH_gui.dialog.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

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
 *
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class JMyPasswordKeyboardField extends JPasswordField {

  private JButton jPassKeys;

  /** Creates new JMyPasswordKeyboardField */
  public JMyPasswordKeyboardField() {
    this(null);
  }
  public JMyPasswordKeyboardField(String buttonToolTip) {
    this(buttonToolTip, null);
  }
  public JMyPasswordKeyboardField(String buttonToolTip, String password) {
    super(password);
    addMouseListener(new TextFieldPopupPasteAdapter());

    Icon icon = Images.get(ImageNums.KEYBOARD);
    jPassKeys = new JMyButtonNoFocus(icon);
    if (icon == null)
      jPassKeys.setText("Keyboard");
    if (buttonToolTip != null)
      jPassKeys.setToolTipText(buttonToolTip);
    jPassKeys.setBorder(new EmptyBorder(0,0,0,0));
    jPassKeys.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    jPassKeys.setRequestFocusEnabled(false);
    jPassKeys.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            MouseTextEntryDialog dialog = null;
            Window parentWindow = SwingUtilities.windowForComponent(JMyPasswordKeyboardField.this);
            if (parentWindow instanceof Dialog) {
              dialog = new MouseTextEntryDialog((Dialog) parentWindow, JMyPasswordKeyboardField.this);
            } else if (parentWindow instanceof Frame) {
              dialog = new MouseTextEntryDialog((Frame) parentWindow, JMyPasswordKeyboardField.this);
            }
          }
        });
      }
    });

    if (icon != null) {
      Dimension dimension1 = new Dimension();
      dimension1.width = icon.getIconWidth();
      dimension1.height = icon.getIconHeight();
      jPassKeys.setPreferredSize(dimension1);
      jPassKeys.setSize(dimension1);
    }

    // remove enter key binding from the password fields
    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    Keymap oldPassMap = getKeymap();
    oldPassMap.removeKeyStrokeBinding(enter);

    setLayout(new BorderLayout(0,0));
    add(jPassKeys, "East");
  }

  public void setEnabled(boolean flag) {
    super.setEnabled(flag);
    if (jPassKeys != null)
      jPassKeys.setEnabled(flag);
  }

  public void setEditable(boolean flag) {
    super.setEditable(flag);
    if (jPassKeys != null)
      jPassKeys.setEnabled(flag);
  }

}
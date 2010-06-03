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

import com.CH_co.util.*;
import com.CH_gui.util.*;
import com.CH_gui.dialog.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

/**
 * <b>Copyright</b> &copy; 2001-2010
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
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class JMyPasswordKeyboardField extends JPanel {

  private JPasswordField jPass;
  private JButton jPassKeys;

  /** Creates new JMyPasswordKeyboardField */
  public JMyPasswordKeyboardField() {
    this(null);
  }
  public JMyPasswordKeyboardField(String buttonToolTip) {
    this(buttonToolTip, null);
  }
  public JMyPasswordKeyboardField(String buttonToolTip, String password) {
    setLayout(new BorderLayout(0, 0));

    jPass = new JPasswordField(password);
    jPass.addMouseListener(new TextFieldPopupPasteAdapter());
    setBorder(jPass.getBorder());
    jPass.setBorder(new EmptyBorder(0,0,0,0));

    Icon icon = Images.get(ImageNums.KEYBOARD);
    jPassKeys = new JMyButtonNoFocus(icon);
    if (icon == null)
      jPassKeys.setText("Keyboard");
    if (buttonToolTip != null) jPassKeys.setToolTipText(buttonToolTip);
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
              dialog = new MouseTextEntryDialog((Dialog) parentWindow, new String(jPass.getPassword()));
            } else if (parentWindow instanceof Frame) {
              dialog = new MouseTextEntryDialog((Frame) parentWindow, new String(jPass.getPassword()));
            }
            if (dialog != null && dialog.isOKeyed()) {
              jPass.setText(new String(dialog.getPass()));
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
    Keymap oldPassMap = jPass.getKeymap();
    oldPassMap.removeKeyStrokeBinding(enter);

    add(jPass, "Center");
    add(jPassKeys, "East");
  }

  public JPasswordField getPasswordField() {
    return jPass;
  }

  public Document getDocument() {
    return jPass.getDocument();
  }

  public void setEnabled(boolean flag) {
    jPass.setEnabled(flag);
    jPassKeys.setEnabled(flag);
  }

  public void setEditable(boolean flag) {
    jPass.setEditable(flag);
    jPassKeys.setEnabled(flag);
  }

  public char[] getPassword() {
    return jPass.getPassword();
  }

  public void setText(String s) {
    jPass.setText(s);
  }

}
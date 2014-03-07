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

import com.CH_guiLib.gui.JMyPopupMenu;
import com.CH_guiLib.gui.JMyTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Document;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.7 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class JMyTextOptionField extends JPanel {

  private JTextField jText;
  private JButton jButton;
  private String[] options;

  /** Creates new JMyTextOptionField */
  public JMyTextOptionField() {
    this(null, null, null);
  }
  public JMyTextOptionField(String text, ActionListener actionListener) {
    this(text, null, actionListener);
  }
  public JMyTextOptionField(String text, Icon icon, ActionListener actionListener) {
    this(text, icon, actionListener, null);
  }
  public JMyTextOptionField(String text, Icon icon, ActionListener actionListener, final String[] options) {
    this.options = options;
    if (text == null)
      jText = new JMyTextField();
    else
      jText = new JMyTextField(text);

    if (icon == null)
      icon = new JMyDotsIcon();

    jButton = new JMyButton(icon);

    if (actionListener == null && options != null) {
      jButton.setFocusable(false);

      AbstractAction showPopupAction = new AbstractAction("Show Options Popup") {
        public void actionPerformed(ActionEvent e) {
          System.out.println("action performed, popup");
          showPopup("DOWN");
        }
      };

      // Pressing DOWN key we want the popup to show
      jText.getInputMap().put(KeyStroke.getKeyStroke("pressed DOWN"), showPopupAction.getValue(Action.NAME));

      // Add action
      jText.getActionMap().put(showPopupAction.getValue(Action.NAME), showPopupAction);
      actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          showPopup(e.getSource());
        }
      };
    }
    if (actionListener != null) {
      jButton.addActionListener(actionListener);
      jText.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          showPopup(e.getSource());
        }
      });
    }

    setLayout(new BorderLayout(0, 0));

    jText.setBorder(new EmptyBorder(0,0,0,0));
    //jButton.setBorder(new EmptyBorder(0,0,0,0));

    Dimension buttonDimension = new Dimension(icon.getIconWidth(), icon.getIconHeight());
    jButton.setPreferredSize(buttonDimension);
    jButton.setSize(buttonDimension);

    add(jText, BorderLayout.CENTER);
    add(jButton, BorderLayout.EAST);

    setBorder();
  }

  private void showPopup(Object source) {
    if (jButton.isEnabled() && (source == jButton || (source == jText && !jText.isEditable()) || source.equals("DOWN"))) {
      if (options != null && options.length > 0) {
        JPopupMenu popup = new JMyPopupMenu();
        for (int i=0; i<options.length; i++) {
          JMyMenuItem menuItem = new JMyMenuItem(options[i]);
          final int index = i;
          menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
              jText.setText(options[index]);
              if (options[index].startsWith("<") && options[index].endsWith(">")) {
                jText.requestFocusInWindow();
                jText.selectAll();
              }
              else
                jText.select(0,0);
            }
          });
          popup.add(menuItem);
        }
        popup.pack();
        popup.show(JMyTextOptionField.this, 0, JMyTextOptionField.this.getSize().height);
      }
    }
  }

  public void setBorder() {
    javax.swing.border.Border border = UIManager.getBorder("TextField.border");
    if (!border.equals(getBorder())) {
      setBorder(border);
      repaint();
    }
  }


  public JTextField getTextField() {
    return jText;
  }

  public JButton getButton() {
    return jButton;
  }

  public Document getDocument() {
    return jText.getDocument();
  }

  public void setEnabled(boolean flag) {
    jText.setEnabled(flag);
    jButton.setEnabled(flag);
  }

  public void setEditable(boolean flag) {
    jText.setEditable(flag);
  }

  public String getText() {
    return jText.getText();
  }

  public void setText(String s) {
    jText.setText(s);
  }

  public void updateOptions(String[] strs) {
    if (options == null)
      throw new IllegalArgumentException("Options cannot be update because the instance was not created to use them.");
    options = strs;
  }
}
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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import java.util.Random;

import com.CH_co.cryptx.Rnd;
import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.13 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class MouseTextEntry extends JPanel {

  private static int ROW_LENGTH = 9;
  private String[] keys = new String[] { com.CH_gui.lang.Lang.rb.getString("virtual_keys_1"), 
                                         com.CH_gui.lang.Lang.rb.getString("virtual_keys_2"),
                                         com.CH_gui.lang.Lang.rb.getString("virtual_keys_3"),
                                         com.CH_gui.lang.Lang.rb.getString("virtual_keys_4") 
                                        };
  private JButton[][] keyButtons;
  private JPasswordField jPass;
  private JButton jabc;
  private JButton jABC;
  private JButton j123;
  private JButton jsym;
  private JButton jbak;
  private JButton jclr;
  private ButtonActionListener buttonActionListener;

  /** Creates new MouseTextEntry */
  public MouseTextEntry(String initialStr) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MouseTextEntry.class, "MouseTextEntry(String initialStr)");
    if (trace != null) trace.args(initialStr);

    randomizeKeys(keys);
    buttonActionListener = new ButtonActionListener();
    keyButtons = createKeys();
    createMainPanel(initialStr);

    if (trace != null) trace.exit(MouseTextEntry.class);
  }

  private void randomizeKeys(String[] theKeys) {
    Random random = Rnd.getSecureRandom();

    StringBuffer[] sbs = new StringBuffer[theKeys.length];
    for (int i=0; i<4; i++)
      sbs[i] = new StringBuffer(theKeys[i]);

    for (int k=0; k<theKeys[0].length(); k++) {
      int pos = k + random.nextInt(theKeys[0].length()-k);
      // lower case letters should correspond to the upper case ones to minimize confusion
      swap(sbs[0], k, pos);
      swap(sbs[1], k, pos);
    }

    for (int k=0; k<theKeys[2].length(); k++) {
      int pos = k + random.nextInt(theKeys[2].length()-k);
      // certain identical symbols are the same in both arrays, so they should correspond to minimize confusion
      swap(sbs[2], k, pos);
      swap(sbs[3], k, pos);
    }

    for (int i=0; i<4; i++)
      theKeys[i] = sbs[i].toString();
  }

  private void swap(StringBuffer sb, int i1, int i2) {
    char temp = sb.charAt(i1);
    sb.setCharAt(i1, sb.charAt(i2));
    sb.setCharAt(i2, temp);
  }

  private JButton[][] createKeys() {
    JButton[][] bts = new JButton[4][];
    for (int i=0; i<keys.length; i++) {
      bts[i] = new JButton[keys[i].length()];
      for (int k=0; k<keys[i].length(); k++) {
        String s = keys[i].substring(k, k+1);
        if (s.equals(" ")) {
          s = com.CH_gui.lang.Lang.rb.getString("virtual_keys_space");
        }
        bts[i][k] = new JMyButton(s);
        bts[i][k].addActionListener(buttonActionListener);
        bts[i][k].addKeyListener(buttonActionListener);
        bts[i][k].setBorder(new EmptyBorder(4, 4, 4, 4));
        bts[i][k].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      }
    }
    return bts;
  }

  private JPanel createMainPanel(String initialStr) {
    JPanel panel = this;

    panel.setLayout(new GridBagLayout());

    jPass = new JPasswordField(initialStr);
    jPass.addMouseListener(new TextFieldPopupPasteAdapter());
    //jPass.setEditable(false);
    //jPass.addKeyListener(buttonActionListener);

    Color grayBack = new Color(180, 180, 180);

    jabc = new JMyButton(com.CH_gui.lang.Lang.rb.getString("virtual_keys_button1_abc"));
    jabc.setBackground(grayBack);
    jabc.setForeground(Color.blue);
    jabc.setBorder(new EmptyBorder(6, 6, 6, 6));

    jABC = new JMyButton(com.CH_gui.lang.Lang.rb.getString("virtual_keys_button2_ABC"));
    jABC.setBackground(grayBack);
    jABC.setForeground(Color.blue);
    jABC.setBorder(new EmptyBorder(6, 6, 6, 6));

    j123 = new JMyButton(com.CH_gui.lang.Lang.rb.getString("virtual_keys_button3_123"));
    j123.setBackground(grayBack);
    j123.setForeground(Color.blue);
    j123.setBorder(new EmptyBorder(6, 6, 6, 6));

    jsym = new JMyButton(com.CH_gui.lang.Lang.rb.getString("virtual_keys_button4_#@%"));
    jsym.setBackground(grayBack);
    jsym.setForeground(Color.blue);
    jsym.setBorder(new EmptyBorder(6, 6, 6, 6));

    jbak = new JMyButton(com.CH_gui.lang.Lang.rb.getString("virtual_keys_button5_DEL"));
    jbak.setBackground(grayBack);
    jbak.setForeground(Color.blue);
    jbak.setBorder(new EmptyBorder(6, 6, 6, 6));

    jclr = new JMyButton(com.CH_gui.lang.Lang.rb.getString("virtual_keys_button6_CLR"));
    jclr.setBackground(grayBack);
    jclr.setForeground(Color.blue);
    jclr.setBorder(new EmptyBorder(6, 6, 6, 6));


    jabc.addActionListener(buttonActionListener);
    jABC.addActionListener(buttonActionListener);
    j123.addActionListener(buttonActionListener);
    jsym.addActionListener(buttonActionListener);
    jbak.addActionListener(buttonActionListener);
    jclr.addActionListener(buttonActionListener);
    jabc.addKeyListener(buttonActionListener);
    jABC.addKeyListener(buttonActionListener);
    j123.addKeyListener(buttonActionListener);
    jsym.addKeyListener(buttonActionListener);
    jbak.addKeyListener(buttonActionListener);
    jclr.addKeyListener(buttonActionListener);

    panel.add(jPass, new GridBagConstraints(0, 0, ROW_LENGTH, 1, 0, 0, 
      GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));

    setGrid(panel, keyButtons, 0);

    JPanel functionPanel = new JPanel();

    panel.add(functionPanel, new GridBagConstraints(0, 5, ROW_LENGTH, 1, 1, 0, 
      GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));

    functionPanel.add(jabc, new GridBagConstraints(0, 0, 1, 1, 1, 0, 
      GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    functionPanel.add(jABC, new GridBagConstraints(1, 0, 1, 1, 1, 0, 
      GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    functionPanel.add(j123, new GridBagConstraints(2, 0, 1, 1, 1, 0, 
      GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    functionPanel.add(jsym, new GridBagConstraints(3, 0, 1, 1, 1, 0, 
      GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    functionPanel.add(jbak, new GridBagConstraints(4, 0, 1, 1, 1, 0, 
      GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    functionPanel.add(jclr, new GridBagConstraints(5, 0, 1, 1, 1, 0, 
      GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));

    return panel;
  }

  private void setGrid(JPanel panel, JButton[][] buttons, int index) {
    for (int i=0; i<buttons.length; i++) {
      for (int k=0; k<buttons[i].length; k++) {
        panel.remove(buttons[i][k]);
      }
    }


    int posY = 0;

    for (int i=0; i<buttons[index].length; i++) {
      if (i % ROW_LENGTH == 0) {
        posY ++;
      }
      int x = i % ROW_LENGTH;
      panel.add(buttons[index][i], new GridBagConstraints(x, posY, 1, 1, 10, 10, 
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    }

    invalidate();
    validate();
    repaint();
  }

  private class ButtonActionListener implements ActionListener, KeyListener {
    private void doBackspace() {
      char[] pass = jPass.getPassword();
      if (pass != null && pass.length > 0) {
        String p = new String(pass, 0, pass.length - 1);
        jPass.setText(p);
      }
    }
    private void doAppend(String s) {
      StringBuffer sb = new StringBuffer(new String(jPass.getPassword()));
      sb.append(s);
      jPass.setText(sb.toString());
      sb.delete(0, sb.length()-1);
    }
    
    public void actionPerformed(ActionEvent event) {
      JButton button = (JButton) event.getSource();
      if (button == jbak) {
        doBackspace();
      }
      else if (button == jclr) {
        jPass.setText("");
      }
      else if (button == jabc) {
        setGrid(MouseTextEntry.this, keyButtons, 0);
      }
      else if (button == jABC) {
        setGrid(MouseTextEntry.this, keyButtons, 1);
      }
      else if (button == j123) {
        setGrid(MouseTextEntry.this, keyButtons, 2);
      }
      else if (button == jsym) {
        setGrid(MouseTextEntry.this, keyButtons, 3);
      }
      else {
        String s = button.getText();
        if (s.equals(com.CH_gui.lang.Lang.rb.getString("virtual_keys_space"))) {
          s = " ";
        }
        doAppend(s);
      }
    }

    public void keyPressed(java.awt.event.KeyEvent keyEvent) {
    }

    public void keyReleased(java.awt.event.KeyEvent keyEvent) {
    }

    public void keyTyped(java.awt.event.KeyEvent keyEvent) {
      keyEvent.consume();
      char ch = keyEvent.getKeyChar();
      boolean isBackspace = ch == '\u0008';
      if (isBackspace) {
        doBackspace();
      } else if (!Character.isWhitespace(ch)) {
        doAppend("" + ch);
      }
    }

  }

  public char[] getPass() {
    return jPass.getPassword();
  }
}
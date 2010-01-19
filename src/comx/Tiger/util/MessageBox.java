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
// Source File Name:   MessageBox.java

package comx.Tiger.util;

import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;

public class MessageBox extends Dialog {
  class SymAction implements ActionListener {

    public void actionPerformed(ActionEvent actionevent) {
      Object obj = actionevent.getSource();
      if (obj == okButton)
        okButton_ActionPerformed(actionevent);
    }

    SymAction() {
    }
  }

  class SymWindow extends WindowAdapter {

    public void windowClosing(WindowEvent windowevent) {
      Object obj = windowevent.getSource();
      if (obj == MessageBox.this)
        MessageBox_WindowClosing(windowevent);
    }

    SymWindow() {
    }
  }


  boolean fComponentsAdjusted;
  Label msgLabel;
  Button okButton;
  protected static Frame localFrame;

  public MessageBox(Frame frame, String s, String s1) {
    super(frame, s);
    fComponentsAdjusted = false;
    msgLabel = new Label();
    okButton = new Button();
    setLayout(null);
    setSize(430, 136);
    setVisible(false);
    msgLabel.setText("text");
    msgLabel.setAlignment(1);
    add(msgLabel);
    msgLabel.setBounds(16, 8, 408, 28);
    okButton.setLabel("OK");
    add(okButton);
    okButton.setBackground(Color.lightGray);
    okButton.setBounds(165, 80, 99, 31);
    SymWindow symwindow = new SymWindow();
    addWindowListener(symwindow);
    SymAction symaction = new SymAction();
    okButton.addActionListener(symaction);
    msgLabel.setText(s1);
  }

  public void addNotify() {
    Dimension dimension = getSize();
    super.addNotify();
    if (fComponentsAdjusted)
      return;
    Insets insets = getInsets();
    setSize(insets.left + insets.right + dimension.width, insets.top + insets.bottom + dimension.height);
    Component acomponent[] = getComponents();
    for (int i = 0; i < acomponent.length; i++) {
      Point point = acomponent[i].getLocation();
      point.translate(insets.left, insets.top);
      acomponent[i].setLocation(point);
    }

    fComponentsAdjusted = true;
  }

  public void setVisible(boolean flag) {
    if (flag) {
      Rectangle rectangle = getParent().getBounds();
      Rectangle rectangle1 = getBounds();
      setLocation(rectangle.x + (rectangle.width - rectangle1.width) / 2, rectangle.y + (rectangle.height - rectangle1.height) / 2);
    }
    super.setVisible(flag);
  }

  public static void createMessageBox(String s, String s1) {
    if (localFrame == null)
      localFrame = new Frame(s);
    MessageBox messagebox = new MessageBox(localFrame, s, s1);
    localFrame.setSize(messagebox.getSize().width, messagebox.getSize().height);
    messagebox.setVisible(true);
  }

  void MessageBox_WindowClosing(WindowEvent windowevent) {
    setVisible(false);
  }

  void okButton_ActionPerformed(ActionEvent actionevent) {
    setVisible(false);
    dispose();
    if (localFrame != null)
      localFrame.setVisible(false);
  }
}
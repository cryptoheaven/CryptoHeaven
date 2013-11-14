/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.addressBook;

import com.CH_gui.util.Images;
import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.MyInsets;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import com.CH_co.gui.*;
import com.CH_co.nanoxml.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.gui.*;
import com.CH_gui.msgs.*;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.7 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class NamePanel extends JPanel {

  Icon jIcon;

  JTextField jFullName;
  JTextField jJobTitle;
  JTextField jCompany;
  JTextField jFileAs;

  /** Creates new NamePanel */
  public NamePanel(UndoManagerI undoMngrI) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(NamePanel.class, "NamePanel()");
    initialize(undoMngrI);
    if (trace != null) trace.exit(NamePanel.class);
  }


  private void initialize(UndoManagerI undoMngrI) {
    jIcon = Images.get(ImageNums.PERSON32);
    jFullName = new JUndoableTextField(10, undoMngrI);
    jFullName.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        if (jFileAs.getText().trim().length() == 0)
          jFileAs.setText(jFullName.getText().trim());
      }
    });
    jJobTitle = new JUndoableTextField(10, undoMngrI);
    jCompany = new JUndoableTextField(10, undoMngrI);
    jFileAs = new JUndoableTextField(10, undoMngrI);

    setLayout(new GridBagLayout());

    add(new JMyLabel(jIcon), new GridBagConstraints(0, 0, 1, 4, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(2, 2, 2, 2), 0, 0));

    int posY = 0;

    add(new JMyLabel("Full Name:"), new GridBagConstraints(1, posY, 1, 1, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
    add(jFullName, new GridBagConstraints(2, posY, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
    posY ++;
    add(new JMyLabel("Job title:"), new GridBagConstraints(1, posY, 1, 1, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
    add(jJobTitle, new GridBagConstraints(2, posY, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
    posY ++;

    add(new JMyLabel("Company:"), new GridBagConstraints(1, posY, 1, 1, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
    add(jCompany, new GridBagConstraints(2, posY, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
    posY ++;

    add(new JMyLabel("File as:"), new GridBagConstraints(1, posY, 1, 1, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
    add(jFileAs, new GridBagConstraints(2, posY, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
    posY ++;
  }

  String getFileAs() {
    String fileAs = jFileAs.getText().trim();
    if (fileAs.length() == 0)
      fileAs = jFullName.getText().trim();
    return fileAs;
  }

  String getFullName() {
    return jFullName.getText().trim();
  }

  public XMLElement getContent() {
    return getContent(jFullName.getText().trim(), 
                      jJobTitle.getText().trim(),
                      jCompany.getText().trim(),
                      getFileAs());
  }

  public static XMLElement getContent(String fullName, String jobTitle, String company, String fileAs) {
    XMLElement elt = new XMLElement();
    elt.setNameSafe("Name");

    String c = null;

    c = fullName;
    if (c != null && c.length() > 0) {
      XMLElement e = new XMLElement();
      e.setNameSafe("Full Name");
      e.setContent(c);
      elt.addChild(e);
    }

    c = jobTitle;
    if (c != null && c.length() > 0) {
      XMLElement e = new XMLElement();
      e.setNameSafe("Job Title");
      e.setContent(c);
      elt.addChild(e);
    }

    c = company;
    if (c != null && c.length() > 0) {
      XMLElement e = new XMLElement();
      e.setNameSafe("Company");
      e.setContent(c);
      elt.addChild(e);
    }

    c = fileAs;
    if (c != null && c.length() > 0 && !c.equals(fullName)) {
      XMLElement e = new XMLElement();
      e.setNameSafe("File As");
      e.setContent(c);
      elt.addChild(e);
    }

    return elt;
  }

  public void setContent(XMLElement elt) {
    clear();
    if (elt.getNameSafe().equals("Name")) {
      Vector v = elt.getChildren();
      for (int i=0; i<v.size(); i++) {
        XMLElement e = (XMLElement) v.elementAt(i);
        String n = e.getNameSafe();
        String c = e.getContent();
        if (n.equals("Full Name"))
          jFullName.setText(c);
        else if (n.equals("Job Title"))
          jJobTitle.setText(c);
        else if (n.equals("Company"))
          jCompany.setText(c);
        else if (n.equals("File As"))
          jFileAs.setText(c);
      }
    }
  }


  public boolean isAnyContent() {
    return jFullName.getText().trim().length() > 0 || 
           jJobTitle.getText().trim().length() > 0 || 
           jCompany.getText().trim().length() > 0 || 
           getFileAs().length() > 0;
  }


  void clear() {
    jFullName.setText("");
    jJobTitle.setText("");
    jCompany.setText("");
    jFileAs.setText("");
  }

}
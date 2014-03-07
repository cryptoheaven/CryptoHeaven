/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
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

import com.CH_guiLib.gui.JMyComboBox;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.8 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class EmailPanel extends JPanel {

  Icon jIcon;

  JComboBox jType;
  JTextField jEmail;
  JTextField jDisplayAs;

  private static final String[] types = new String[] { "E-mail", "E-mail 2", "E-mail 3" };
  private String[] emails = new String[types.length];
  private String[] displays = new String[types.length];

  int currentType = 0;

  /** Creates new EmailPanel */
  public EmailPanel(UndoManagerI undoMngrI) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EmailPanel.class, "EmailPanel()");
    initialize(undoMngrI);
    if (trace != null) trace.exit(EmailPanel.class);
  }

  private void initialize(UndoManagerI undoMngrI) {
    jIcon = Images.get(ImageNums.MAIL32);
    jType = new JMyComboBox(types);
    jType.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        currentType = jType.getSelectedIndex();
        String email = emails[currentType];
        email = email != null ? email : "";
        jEmail.setText(email);
        String display = displays[currentType];
        display = display != null ? display : "";
        jDisplayAs.setText(display);
      }
    });
    jEmail = new JUndoableTextField(10, undoMngrI);
    jEmail.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        emails[currentType] = jEmail.getText().trim();
      }
    });
    jDisplayAs = new JUndoableTextField(10, undoMngrI);
    jDisplayAs.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        displays[currentType] = jDisplayAs.getText().trim();
      }
    });

    setLayout(new GridBagLayout());

    add(new JMyLabel(jIcon), new GridBagConstraints(0, 0, 1, 2, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(2, 2, 2, 2), 0, 0));

    add(jType, new GridBagConstraints(1, 0, 1, 1, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
    add(jEmail, new GridBagConstraints(2, 0, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
    add(new JMyLabel("Display as:"), new GridBagConstraints(1, 1, 1, 1, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
    add(jDisplayAs, new GridBagConstraints(2, 1, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
  }


  public XMLElement getContent() {
    return getContent(getTypes(), getEmails(), getDisplaysAs(), currentType);
  }

  public static XMLElement getContent(String[] types, String[] emails, String[] displays, int currentType) {
    XMLElement elt = new XMLElement();
    elt.setNameSafe("Emails");
    elt.setAttribute("first", types[currentType]);

    for (int i=0; i<types.length; i++) {
      if (emails.length > i) {
        XMLElement e = new XMLElement();
        e.setNameSafe(types[i]);
        String email = emails[i];
        String displayAs = displays != null && displays.length > i ? displays[i] : null;
        if (displayAs != null && displayAs.length() > 0) {
          e.setAttribute("displayAs", displayAs);
        }
        if (email != null && email.length() > 0) {
          e.setContent(email);
          elt.addChild(e);
        }
      }
    }

    return elt;
  }

  public void setContent(XMLElement elt) {
    clear();
    if (elt.getNameSafe().equals("Emails")) {
      String first = (String) elt.getAttribute("first");
      Vector v = elt.getChildren();
      for (int i=0; i<v.size(); i++) {
        XMLElement e = (XMLElement) v.elementAt(i);
        String n = e.getNameSafe();
        String c = e.getContent();
        for (int k=0; k<types.length; k++) {
          if (types[k].equals(n)) {
            emails[k] = c;
            displays[k] = (String) e.getAttribute("displayAs", "");
            if (first.equals(n)) {
              currentType = k;
              jEmail.setText(emails[k]);
              jDisplayAs.setText(displays[k]);
            }
          }
        }
      }
      for (int i=0; i<types.length; i++) {
        if (types[i].equals(first)) {
          jType.setSelectedIndex(i);
          break;
        }
      }
    }
  }

  public static final String[] getTypes() {
    return (String[]) types.clone();
  }
  public String[] getEmails() {
    emails[currentType] = jEmail.getText().trim();
    return emails;
  }
  public String[] getDisplaysAs() {
    displays[currentType] = jDisplayAs.getText().trim();
    return displays;
  }


  public boolean isAnyContent() {
    String[] emails = getEmails();
    for (int i=0; i<emails.length; i++) {
      if (emails[i] != null && emails[i].trim().length() > 0)
        return true;
    }
    String[] displays = getDisplaysAs();
    for (int i=0; i<displays.length; i++) {
      if (displays[i] != null && displays[i].trim().length() > 0)
        return true;
    }
    return false;
  }


  void clear() {
    jEmail.setText("");
    jDisplayAs.setText("");
    for (int i=0; i<types.length; i++) {
      emails[i] = "";
      displays[i] = "";
    }
  }

}
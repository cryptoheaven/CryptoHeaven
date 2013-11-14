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

import com.CH_guiLib.gui.*;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.8 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class PhonePanel extends JPanel {

  Icon jIcon;

  static int NUM_OF_FIELDS = 4;

  JComboBox[] jTypes = new JComboBox[NUM_OF_FIELDS];
  JMyTextField[] jPhones = new JMyTextField[NUM_OF_FIELDS];
  int[] currentType = new int[] { 0, 6, 2, 10 };

  private static final String[] types = new String[] { "Business", "Business 2", "Business Fax", "Callback", "Car", "Company", "Home", "Home 2", "Home Fax", "ISDN", "Mobile", "Other", "Other Fax", "Pager", "Primary" };
  private String[] phones = new String[types.length];

  /** Creates new PhonePanel */
  public PhonePanel(UndoManagerI undoMngrI) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PhonePanel.class, "PhonePanel()");
    initialize(undoMngrI);
    if (trace != null) trace.exit(PhonePanel.class);
  }

  private void initialize(UndoManagerI undoMngrI) {
    jIcon = Images.get(ImageNums.PHONE32);
    for (int i=0; i<NUM_OF_FIELDS; i++) {
      final int index = i;
      jTypes[i] = new JMyComboBox(types);
      jTypes[i].setSelectedIndex(currentType[i]);
      jTypes[i].addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          currentType[index] = jTypes[index].getSelectedIndex();
          String phone = phones[currentType[index]];
          phone = phone != null ? phone : "";
          jPhones[index].setText(phone);
        }
      });
      jPhones[i] = new JUndoableTextField(10, undoMngrI);
      jPhones[i].addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          phones[currentType[index]] = jPhones[index].getText().trim();
          for (int k=0; k<NUM_OF_FIELDS; k++) {
            if (k != index && currentType[k] == currentType[index])
              jPhones[k].setText(phones[currentType[index]]);
          }
        }
      });
    }

    setLayout(new GridBagLayout());

    add(new JMyLabel(jIcon), new GridBagConstraints(0, 0, 1, 4, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(2, 2, 2, 2), 0, 0));

    for (int i=0; i<NUM_OF_FIELDS; i++) {
      add(jTypes[i], new GridBagConstraints(1, i, 1, 1, 0, 0, 
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
      add(jPhones[i], new GridBagConstraints(2, i, 1, 1, 10, 0, 
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
    }
  }


  public XMLElement getContent() {
    return getContent(getTypes(), getPhones(), currentType);
  }

  public static XMLElement getContent(String[] types, String[] phones, int[] currentTypes) {
    XMLElement elt = new XMLElement();
    elt.setNameSafe("Phones");
    elt.setAttribute("first", types[currentTypes[0]]);
    elt.setAttribute("second", types[currentTypes[1]]);
    elt.setAttribute("third", types[currentTypes[2]]);
    elt.setAttribute("forth", types[currentTypes[3]]);
    for (int i=0; i<types.length; i++) {
      XMLElement e = new XMLElement();
      e.setNameSafe(types[i]);
      String phone = phones[i];
      if (phone != null && phone.length() > 0) {
        e.setContent(phone);
        elt.addChild(e);
      }
    }
    return elt;
  }


  public void setContent(XMLElement elt) {
    clear();
    if (elt.getNameSafe().equals("Phones")) {
      String[] currents = new String[] { (String) elt.getAttribute("first"), (String) elt.getAttribute("second"), (String) elt.getAttribute("third"), (String) elt.getAttribute("forth") };
      Vector v = elt.getChildren();
      for (int i=0; i<v.size(); i++) {
        XMLElement e = (XMLElement) v.elementAt(i);
        String n = e.getNameSafe();
        String c = e.getContent();
        for (int k=0; k<types.length; k++) {
          if (types[k].equals(n)) {
            phones[k] = c;
            for (int j=0; j<currents.length; j++) {
              String current = currents[j];
              if (current == null || current.length() == 0)
                current = types[currentType[j]];
              if (current.equals(n)) {
                currentType[j] = k;
                jPhones[j].setText(phones[k]); // in-case selection will not change
              }
            }
          }
        }
      }
      for (int i=0; i<types.length; i++) {
        for (int k=0; k<NUM_OF_FIELDS; k++) {
          if (types[i].equals(currents[k])) {
            jTypes[k].setSelectedIndex(i);
            break;
          }
        }
      }
    }
  }

  public static String[] getTypes() {
    return (String[]) types.clone();
  }
  public String[] getPhones() {
    for (int i=0; i<NUM_OF_FIELDS; i++) {
      phones[currentType[i]] = jPhones[i].getText().trim();
    }
    return phones;
  }


  public boolean isAnyContent() {
    String[] phones = getPhones();
    for (int i=0; i<phones.length; i++) {
      if (phones[i] != null && phones[i].trim().length() > 0)
        return true;
    }
    return false;
  }


  void clear() {
    for (int i=0; i<NUM_OF_FIELDS; i++) {
      jPhones[i].setText("");
    }
    for (int i=0; i<types.length; i++) {
      phones[i] = "";
    }
  }

}
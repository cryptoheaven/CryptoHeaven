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

package com.CH_gui.addressBook;

import com.CH_gui.util.Images;
import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.MyInsets;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

import com.CH_co.gui.*;
import com.CH_co.nanoxml.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.msgs.*;
import com.CH_guiLib.gui.JMyComboBox;

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
 * <b>$Revision: 1.8 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class AddressPanel extends JPanel {

  Icon jIcon;

  JComboBox jType;
  JTextArea jAddress;

  private static final String[] types = new String[] { "Business", "Home", "Other" };
  private String[] addresses = new String[types.length];

  int currentType = 0;

  /** Creates new AddressPanel */
  public AddressPanel(UndoManagerI undoMngrI) {
    super();
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AddressPanel.class, "AddressPanel()");
    initialize(undoMngrI);
    if (trace != null) trace.exit(AddressPanel.class);
  }

  private void initialize(UndoManagerI undoMngrI) {
    jIcon = Images.get(ImageNums.HOME32);
    jType = new JMyComboBox(types);
    jType.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        currentType = jType.getSelectedIndex();
        String addr = addresses[currentType];
        addr = addr != null ? addr : "";
        jAddress.setText(addr);
      }
    });
    jAddress = new JUndoableTextArea(6, 10, undoMngrI);
    KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
    Keymap textMap = jAddress.getKeymap();
    textMap.removeKeyStrokeBinding(tab);
    jAddress.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        addresses[currentType] = jAddress.getText().trim();
      }
    });

    setLayout(new GridBagLayout());

    add(new JMyLabel(jIcon), new GridBagConstraints(0, 0, 1, 3, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(2, 2, 2, 2), 0, 0));

    add(new JMyLabel("Address:"), new GridBagConstraints(1, 0, 1, 1, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
    add(jType, new GridBagConstraints(1, 1, 1, 1, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
    add(new JScrollPane(jAddress), new GridBagConstraints(2, 0, 1, 3, 10, 10, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(2, 2, 2, 2), 0, 0));
  }


  public XMLElement getContent() {
    return getContent(getTypes(), getAddresses(), currentType);
  }

  public static XMLElement getContent(String[] types, String[] addresses, int currentType) {
    XMLElement elt = new XMLElement();
    elt.setNameSafe("Addresses");
    elt.setAttribute("first", types[currentType]);

    for (int i=0; i<types.length; i++) {
      XMLElement e = new XMLElement();
      e.setNameSafe(types[i]);
      String address = addresses[i];
      if (address != null && address.length() > 0) {
        e.setContent(address);
        elt.addChild(e);
      }
    }

    return elt;
  }


  public void setContent(XMLElement elt) {
    clear();
    if (elt.getNameSafe().equals("Addresses")) {
      String first = (String) elt.getAttribute("first");
      Vector v = elt.getChildren();
      for (int i=0; i<v.size(); i++) {
        XMLElement e = (XMLElement) v.elementAt(i);
        String n = e.getNameSafe();
        String c = e.getContent();
        for (int k=0; k<types.length; k++) {
          if (types[k].equals(n)) {
            addresses[k] = c;
            if (first.equals(n)) {
              currentType = k;
              jAddress.setText(addresses[k]);
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

  public static String[] getTypes() {
    return (String[]) types.clone();
  }
  public String[] getAddresses() {
    addresses[currentType] = jAddress.getText().trim();
    return addresses;
  }


  public boolean isAnyContent() {
    String[] addresses = getAddresses();
    for (int i=0; i<addresses.length; i++) {
      if (addresses[i] != null && addresses[i].trim().length() > 0)
        return true;
    }
    return false;
  }


  void clear() {
    jAddress.setText("");
    for (int i=0; i<types.length; i++) {
      addresses[i] = "";
    }
  }

}
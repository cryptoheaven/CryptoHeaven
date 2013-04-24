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
 * <b>$Revision: 1.8 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class WebPanel extends JPanel {

  Icon jIcon;

  JTextField jWeb;
  JTextField jIM;

  /** Creates new WebPanel */
  public WebPanel(UndoManagerI undoMngrI) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WebPanel.class, "WebPanel()");
    initialize(undoMngrI);
    if (trace != null) trace.exit(WebPanel.class);
  }

  private void initialize(UndoManagerI undoMngrI) {
    jIcon = Images.get(ImageNums.WEB32);
    jWeb = new JUndoableTextField(10, undoMngrI);
    jIM = new JUndoableTextField(10, undoMngrI);

    setLayout(new GridBagLayout());

    add(new JMyLabel(jIcon), new GridBagConstraints(0, 0, 1, 2, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(2, 2, 2, 2), 0, 0));

    add(new JMyLabel("Web page address:"), new GridBagConstraints(1, 0, 1, 1, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
    add(jWeb, new GridBagConstraints(2, 0, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
    add(new JMyLabel("IM address:"), new GridBagConstraints(1, 1, 1, 1, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
    add(jIM, new GridBagConstraints(2, 1, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
  }


  public XMLElement getContent() {
    return getContent(jWeb.getText().trim(), jIM.getText().trim());
  }

  public static XMLElement getContent(String webPage, String IMaddress) {
    XMLElement elt = new XMLElement();
    elt.setNameSafe("Web");

    String c = null;

    c = webPage;
    if (c.length() > 0) {
      XMLElement e = new XMLElement();
      e.setNameSafe("Home");
      e.setContent(c);
      elt.addChild(e);
    }

    c = IMaddress;
    if (c.length() > 0) {
      XMLElement e = new XMLElement();
      e.setNameSafe("IM");
      e.setContent(c);
      elt.addChild(e);
    }

    return elt;
  }


  public void setContent(XMLElement elt) {
    clear();
    if (elt.getNameSafe().equals("Web")) {
      Vector v = elt.getChildren();
      for (int i=0; i<v.size(); i++) {
        XMLElement e = (XMLElement) v.elementAt(i);
        String n = e.getNameSafe();
        String c = e.getContent();
        if (n.equals("Home"))
          jWeb.setText(c);
        else if (n.equals("IM"))
          jIM.setText(c);
      }
    }
  }


  public boolean isAnyContent() {
    return jWeb.getText().trim().length() > 0 || jIM.getText().trim().length() > 0;
  }


  void clear() {
    jWeb.setText("");
    jIM.setText("");
  }

}
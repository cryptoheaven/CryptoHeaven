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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import com.CH_gui.gui.MyInsets;
import com.CH_co.nanoxml.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.gui.*;
import com.CH_gui.msgs.*;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.9 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class ContactInfoPanel extends JPanel {

  NamePanel namePanel;
  AddressPanel addressPanel;
  PhonePanel phonePanel;
  EmailPanel emailPanel;
  WebPanel webPanel;

  UndoManagerI undoMngrI;

  //JButton jSelectAttachments;
  //JPanel jAttachments;
  //JPanel jAttachPanel;

  /** Creates new ContactInfoPanel */
  public ContactInfoPanel(UndoManagerI undoMngrI) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactInfoPanel.class, "ContactInfoPanel(UndoManagerI undoMngrI)");
    this.undoMngrI = undoMngrI;
    initialize();
    if (trace != null) trace.exit(ContactInfoPanel.class);
  }

  private void initialize() {
    initComponents();
    initMainPanel();
  }

  private void initComponents() {
    namePanel = new NamePanel(undoMngrI);
    addressPanel = new AddressPanel(undoMngrI);
    phonePanel = new PhonePanel(undoMngrI);
    emailPanel = new EmailPanel(undoMngrI);
    webPanel = new WebPanel(undoMngrI);

    /*
    jSelectAttachments = new JButtonNoFocus(com.CH_gui.lang.Lang.rb.getString("button_Attach"), Images.get(ImageNums.ATTACH16));
    jSelectAttachments.setAlignmentX(JButton.LEFT_ALIGNMENT);
    jSelectAttachments.setBorder(new EtchedBorder());
    jSelectAttachments.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    jSelectAttachments.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        selectAttachmentsPressed();
      }
    });

    jAttachments = new JPanel();
    jAttachments.setLayout(new FlowLayout(FlowLayout.LEFT));
    jAttachments.setBorder(new EmptyBorder(0,0,0,0));

    jAttachPanel = new JPanel();
    jAttachPanel.setLayout(new GridBagLayout());
    jAttachPanel.add(jSelectAttachments, new GridBagConstraints(0, 0, 1, 1, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    jAttachPanel.add(jAttachments, new GridBagConstraints(0, 1, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
     */
  }

  private void initMainPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    panel.add(namePanel, new GridBagConstraints(0, 0, 1, 3, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(new JSeparator(), new GridBagConstraints(0, 3, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 10, 2, 10), 0, 0));

    
    panel.add(emailPanel, new GridBagConstraints(1, 0, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(new JSeparator(), new GridBagConstraints(1, 1, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 10, 2, 10), 0, 0));

    panel.add(webPanel, new GridBagConstraints(1, 2, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    
    panel.add(new JSeparator(), new GridBagConstraints(1, 3, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 10, 2, 10), 0, 0));
    
    
    panel.add(addressPanel, new GridBagConstraints(0, 4, 1, 3, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    
    
    panel.add(phonePanel, new GridBagConstraints(1, 4, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    
    setLayout(new GridBagLayout());

    add(panel, new GridBagConstraints(0, 0, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    /*
    add(jAttachPanel, new GridBagConstraints(0, 1, 1, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
     */
  }

  /*
  private void selectAttachmentsPressed() {
  }
   */


  public XMLElement getContent() {
    return getContent(new XMLElement[] { namePanel.getContent(), 
                                         addressPanel.getContent(),
                                         phonePanel.getContent(),
                                         emailPanel.getContent(),
                                         webPanel.getContent() } );
  }

  public static XMLElement getContent(XMLElement[] children) {
    XMLElement elt = new XMLElement();
    elt.setNameSafe("AddrFull");

    for (int i=0; i<children.length; i++) {
      XMLElement e = children[i];
      Vector v = e.getChildren();
      if (v != null && v.size() > 0)
        elt.addChild(e);
    }

    return elt;
  }


  public XMLElement getContentPreview() {
    String name = namePanel.getFullName();
    String fileAs = namePanel.getFileAs();
    String[] emails = emailPanel.getEmails();
    String[] displays = emailPanel.getDisplaysAs();
    int currentEmailIndex = emailPanel.currentType;
    String[] types = phonePanel.getTypes();
    String[] phones = phonePanel.getPhones();
    return getContentPreview(name, fileAs, emails, displays, currentEmailIndex, types, phones);
  }

  public static XMLElement getContentPreview(String name, String fileAs, String[] emails, String[] displays, int currentEmailIndex, String[] types, String[] phones) {
    XMLElement elt = new XMLElement();
    elt.setNameSafe("AddrPrev");

    if (name.length() > 0) {
      XMLElement e = new XMLElement();
      e.setNameSafe("Name");
      e.setContent(name);
      elt.addChild(e);
    }

    if (fileAs == null || fileAs.length() == 0)
      fileAs = name;
    if (fileAs.length() > 0) {
      XMLElement e = new XMLElement();
      e.setNameSafe("FileAs");
      e.setContent(fileAs);
      elt.addChild(e);
    }

    String currentEmail = emails[currentEmailIndex];
    String currentDisp = displays != null && displays.length > currentEmailIndex ? displays[currentEmailIndex] : null;
    if (currentEmail != null && currentEmail.length() > 0) {
      XMLElement e = new XMLElement();
      e.setNameSafe("E-mail");
      if (currentDisp != null && currentDisp.length() > 0) {
        e.setContent(currentDisp + " <" + currentEmail + ">");
      } else {
        e.setContent(currentEmail);
      }
      elt.addChild(e);
    } else {
      for (int i=0; i<emails.length; i++) {
        if (emails[i] != null && emails[i].length() > 0) {
          XMLElement e = new XMLElement();
          e.setNameSafe("E-mail");
          e.setContent(emails[i]);
          elt.addChild(e);
          break;
        }
      }
    }

    for (int i=0; phones!=null && i<phones.length; i++) {
      if (phones[i] != null && phones[i].length() > 0) {
        if (types[i].startsWith("Business") && types[i].indexOf("Fax") == -1) {
          XMLElement e = new XMLElement();
          e.setNameSafe("Business Phone");
          e.setContent(phones[i]);
          elt.addChild(e);
          break;
        }
      }
    }

    for (int i=0; phones!=null && i<phones.length; i++) {
      if (phones[i] != null && phones[i].length() > 0) {
        if (types[i].startsWith("Home") && types[i].indexOf("Fax") == -1) {
          XMLElement e = new XMLElement();
          e.setNameSafe("Home Phone");
          e.setContent(phones[i]);
          elt.addChild(e);
          break;
        }
      }
    }

    return elt;
  }


  public void setContent(XMLElement elt) {
    clear();
    if (elt != null && elt.getNameSafe().equals("AddrFull")) {
      Vector v = elt.getChildren();
      for (int i=0; i<v.size(); i++) {
        XMLElement e = (XMLElement) v.elementAt(i);
        String n = e.getNameSafe();
        if (n.equals("Name"))
          namePanel.setContent(e);
        else if (n.equals("Addresses"))
          addressPanel.setContent(e);
        else if (n.equals("Phones"))
          phonePanel.setContent(e);
        else if (n.equals("Emails"))
          emailPanel.setContent(e);
        else if (n.equals("Web"))
          webPanel.setContent(e);
      }
    }
  }


  public boolean isAnyContent() {
    return namePanel.isAnyContent() || addressPanel.isAnyContent() || phonePanel.isAnyContent() || emailPanel.isAnyContent() || webPanel.isAnyContent();
  }

  public boolean isNameAndEmailPresent() {
    return namePanel.isAnyContent() && emailPanel.isAnyContent();
  }

  private void clear() {
    namePanel.clear();
    addressPanel.clear();
    phonePanel.clear();
    emailPanel.clear();
    webPanel.clear();
  }

}
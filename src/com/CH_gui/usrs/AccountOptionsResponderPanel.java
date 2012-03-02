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

package com.CH_gui.usrs;

import com.CH_gui.gui.*;
import com.CH_gui.util.Images;
import com.CH_guiLib.gui.*;

import com.CH_co.nanoxml.*;
import com.CH_co.service.records.AutoResponderRecord;
import com.CH_co.service.records.UserRecord;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

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
 * <b>$Revision: 1.8 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class AccountOptionsResponderPanel extends JPanel implements DisposableObj {

  public JCheckBox jIncludeChangesToResponder;

  private JCheckBox jEnable;
  private JMyCalendarDropdownField jLeaving;
  private JMyCalendarDropdownField jReturning;
  private JTextField jSubject;
  private JTextArea jMessage;

  private JButton jAppendSignature;
  private String messageBeforeAppendingSignature;
  private String messageAfterAppendingSignature;
  private AccountOptionsSignaturesPanel signaturePanel;

  private Boolean originalEnabled;
  private Date originalDateStart;
  private Date originalDateEnd;
  private XMLElement originalData;
  private boolean isChangeAttempted;

  private DocumentChangeListener documentChangeListener;

  private EventListenerList myListenerList = new EventListenerList();


  /** Creates new AccountOptionsResponderPanel */
  public AccountOptionsResponderPanel(UserRecord[] userRecords, AccountOptionsSignaturesPanel signaturePanel, ChangeListener checkBoxListener) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AccountOptionsResponderPanel.class, "AccountOptionsResponderPanel()");

    this.signaturePanel = signaturePanel;

    createComponents(userRecords, checkBoxListener);
    initializeComponents();
    layoutPanel();

    pressedEnable();
    isChangeAttempted = false;

    if (trace != null) trace.exit(AccountOptionsResponderPanel.class);
  }

  public void initializeData(Character enabled, AutoResponderRecord autoResponderRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AccountOptionsResponderPanel.class, "initializeData(Character enabled, AutoResponderRecord autoResponderRecord)");
    if (trace != null) trace.args(enabled, autoResponderRecord);

    initializeData(enabled != null ? Boolean.valueOf(enabled.charValue() == 'Y') : Boolean.FALSE, autoResponderRecord.dateStart, autoResponderRecord.dateEnd, autoResponderRecord.getXmlText());

    if (trace != null) trace.exit(AccountOptionsResponderPanel.class);
  }

  private void initializeData(Boolean enabled, Date dateStart, Date dateEnd, XMLElement xml) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AccountOptionsResponderPanel.class, "initializeData(Boolean enabled, Date dateStart, Date dateEnd, XMLElement xml)");
    if (trace != null) trace.args(enabled, dateStart, dateEnd, xml);

    originalEnabled = enabled;
    originalDateStart = dateStart;
    originalDateEnd = dateEnd;
    originalData = xml;

    // initialize enablement
    jEnable.setSelected(enabled != null && enabled.equals(Boolean.TRUE));

    // initialize dates
    if (dateStart != null) jLeaving.setDate(dateStart);
    if (dateEnd != null) jReturning.setDate(dateEnd);
    // initialize responder message
    if (xml != null && xml.getNameSafe().equalsIgnoreCase("responder")) {
      Vector elementsV = xml.getChildren();
      if (elementsV != null) {
        for (int i=0; i<elementsV.size(); i++) {
          XMLElement element = (XMLElement) elementsV.elementAt(i);
          String elementName = element.getNameSafe();
          String elementValue = element.getContent();
          if (elementName.equalsIgnoreCase("subject")) {
            jSubject.setText(elementValue);
          } else if (elementName.equalsIgnoreCase("message")) {
            jMessage.setText(elementValue);
          }
        }
      }
    }

    // correct enablement of other fields if needed
    pressedEnable();
    isChangeAttempted = false;

    if (trace != null) trace.exit(AccountOptionsResponderPanel.class);
  }


  public boolean isChangeAttempted() {
    return isChangeAttempted;
  }


  public boolean isDataChanged() {
    boolean changed = originalEnabled == null || originalData == null || originalDateStart == null || originalDateEnd == null;
    if (!changed) {
      changed = !originalEnabled.equals(getEnabled()) ||
                !originalData.toString().equals(getData().toString()) ||
                !originalDateStart.equals(getDateStart()) ||
                !originalDateEnd.equals(getDateEnd());
    }
    return changed;
  }

  public Boolean getEnabled() {
    return Boolean.valueOf(jEnable.isSelected());
  }

  public XMLElement getData() {
    XMLElement xml = new XMLElement();
    xml.setNameSafe("responder");
    XMLElement subject = new XMLElement();
    subject.setNameSafe("subject");
    subject.setContent(jSubject.getText());
    XMLElement message = new XMLElement();
    message.setNameSafe("message");
    message.setContent(jMessage.getText());
    xml.addChild(subject);
    xml.addChild(message);
    return xml;
  }

  public Date getDateStart() {
    return jLeaving.getDate();
  }

  public Date getDateEnd() {
    return jReturning.getDate();
  }

  private void createComponents(UserRecord[] userRecs, ChangeListener checkBoxListener) {
    if (userRecs.length > 1) {
      jIncludeChangesToResponder = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Include_the_following_settings_in_this_update"));
      jIncludeChangesToResponder.setFont(jIncludeChangesToResponder.getFont().deriveFont(Font.BOLD));
      jIncludeChangesToResponder.addChangeListener(checkBoxListener);
    }
    jEnable = new JMyCheckBox("Enable Auto-Responder");
    jLeaving = new JMyCalendarDropdownField(DateFormat.MEDIUM, 3, 1);
    jReturning = new JMyCalendarDropdownField(DateFormat.MEDIUM, 3, 1);
    setInitialDates();
    jSubject = new JMyTextField("Out of Office Auto-Response", 20);
    jMessage = new JMyTextArea("I will respond to your message when I return.", 4, 20);
    if (signaturePanel != null) {
      jAppendSignature = new JMyButton("Append Default Signature");
    }

    // set min size of calendar fields
    jLeaving.setMinimumSize(jLeaving.getPreferredSize());
    jReturning.setMinimumSize(jReturning.getPreferredSize());

    documentChangeListener = new DocumentChangeListener();
    jLeaving.getTextField().getDocument().addDocumentListener(documentChangeListener);
    jReturning.getTextField().getDocument().addDocumentListener(documentChangeListener);
    jSubject.getDocument().addDocumentListener(documentChangeListener);
    jMessage.getDocument().addDocumentListener(documentChangeListener);
  }

  private void setInitialDates() {
    jLeaving.setDate(new Date());
    jReturning.setDate(new Date(jLeaving.getDate().getTime() + (7L * 24L * 60L * 60L * 1000L))); // leaving date + 7 days
  }

  private void initializeComponents() {
    jEnable.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        isChangeAttempted = true;
        pressedEnable();
        if (jEnable.isSelected() && jLeaving.getDate().getTime() + 1L*24L*60L*60L*1000L < System.currentTimeMillis()) {
          setInitialDates();
        }
        fireDocumentUpdated();
      }
    });
    if (jAppendSignature != null && signaturePanel != null) {
      jAppendSignature.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          isChangeAttempted = true;
          jMessage.setText(jMessage.getText() + "\n" + signaturePanel.getDefaultSignatureText());
        }
      });
    }
  }

  private void pressedEnable() {
    boolean enabled = jEnable.isSelected();
    jLeaving.setEnabled(enabled);
    jReturning.setEnabled(enabled);
    jSubject.setEnabled(enabled);
    jMessage.setEnabled(enabled);
    if (jAppendSignature != null)
      jAppendSignature.setEnabled(enabled);
  }


  private void layoutPanel() {
    JPanel panel = this;
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    panel.setLayout(new GridBagLayout());

    int posY = 0;

    if (jIncludeChangesToResponder != null) {
      panel.add(jIncludeChangesToResponder, new GridBagConstraints(0, posY, 3, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;
    }

    panel.add(AccountOptionsSignaturesPanel.makeDivider("Dates"), new GridBagConstraints(0, posY, 3, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(Images.get(ImageNums.AUTO_RESPONDER32)), new GridBagConstraints(0, posY, 1, 4, 0, 0,
        GridBagConstraints.NORTH, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 2), 0, 0));
    panel.add(jEnable, new GridBagConstraints(1, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 2, 2, 5), 0, 0));
    panel.add(new JMyLabel("I will be out of the office..."), new GridBagConstraints(1, posY+1, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 2, 2, 5), 0, 0));
    panel.add(new JMyLabel("Leaving:"), new GridBagConstraints(1, posY+2, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 2), 0, 0));
    panel.add(jLeaving, new GridBagConstraints(2, posY+2, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 2, 2, 5), 0, 0));
    panel.add(new JMyLabel("Returning:"), new GridBagConstraints(1, posY+3, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 5, 2), 0, 0));
    panel.add(jReturning, new GridBagConstraints(2, posY+3, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 2, 5, 5), 0, 0));
    posY += 4;

    panel.add(AccountOptionsSignaturesPanel.makeDivider("Out of Office Message"), new GridBagConstraints(0, posY, 3, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel("This is the default message which will be sent while you are away."), new GridBagConstraints(0, posY, 3, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 3, 5), 0, 0));
    posY ++;

    JLabel warning = new JMyLabel("Warning: Auto-Responder settings are stored unencrypted.", Images.get(ImageNums.LOCK_OPEN_SMALL), SwingConstants.LEADING);
    warning.setFont(warning.getFont().deriveFont(Font.BOLD));
    panel.add(warning, new GridBagConstraints(0, posY, 3, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 8, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel("To:"), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 2, 2), 0, 0));
    panel.add(new JMyLabel("<html><i>Whomever"), new GridBagConstraints(1, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 2, 2, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel("Subject:"), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 2, 2), 0, 0));
    panel.add(jSubject, new GridBagConstraints(1, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 5), 0, 0));
    posY ++;

    panel.add(new JScrollPane(jMessage), new GridBagConstraints(0, posY, 3, 1, 10, 10,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(2, 5, 5, 5), 0, 0));
    posY ++;

    if (jAppendSignature != null) {
      panel.add(jAppendSignature, new GridBagConstraints(0, posY, 3, 1, 0, 0,
          GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;
    }
  }


  public void addDocumentListener(DocumentListener l) {
    myListenerList.add(DocumentListener.class, l);
  }

  public void removeDocumentListener(DocumentListener l) {
    myListenerList.remove(DocumentListener.class, l);
  }

  private void fireDocumentUpdated() {
    // Guaranteed to return a non-null array
    Object[] listeners = myListenerList.getListenerList();
    DocumentEvent e = null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length-2; i>=0; i-=2) {
      if (listeners[i] == DocumentListener.class) {
        // Lazily create the event:
        if (e == null)
          e = new DocumentEvent() {
             public DocumentEvent.ElementChange getChange(Element elem) {
               return null;
             }
             public Document getDocument() {
               return null;
             }
             public int getLength() {
               return 0;
             }
             public int getOffset() {
               return 0;
             }
             public DocumentEvent.EventType getType() {
               return null;
             }
          };
        ((DocumentListener)listeners[i+1]).changedUpdate(e);
      }
    }
  }

  private class DocumentChangeListener implements DocumentListener {
    public void changedUpdate(DocumentEvent e) {
      isChangeAttempted = true;
      fireDocumentUpdated();
    }
    public void insertUpdate(DocumentEvent e) {
      isChangeAttempted = true;
      fireDocumentUpdated();
    }
    public void removeUpdate(DocumentEvent e) {
      isChangeAttempted = true;
      fireDocumentUpdated();
    }
  }

  /**
   ****  I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
   * Dispose the object and release resources to help in garbage collection.
   */
  public void disposeObj() {
    if (documentChangeListener != null) {
      jSubject.getDocument().removeDocumentListener(documentChangeListener);
      jMessage.getDocument().removeDocumentListener(documentChangeListener);
      jLeaving.getTextField().getDocument().removeDocumentListener(documentChangeListener);
      jReturning.getTextField().getDocument().removeDocumentListener(documentChangeListener);
      documentChangeListener = null;
    }
  }

  /*
  public static void main(String[] args) {
    final AccountOptionsResponderPanel p = new AccountOptionsResponderPanel();
    JFrame f = new JFrame();
    f.getContentPane().add(p);
    f.pack();
    f.show();
    f.addWindowListener(new WindowAdapter() {
      public void windowDeactivated(WindowEvent e) {
        System.out.println();
        System.out.println(p.getData());
      }
    });
  }
   */

}
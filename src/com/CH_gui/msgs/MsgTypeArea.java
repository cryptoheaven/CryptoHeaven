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

package com.CH_gui.msgs;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.CH_co.gui.*;
import com.CH_co.nanoxml.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.addressBook.*;
import com.CH_gui.gui.*;
import com.CH_guiLib.gui.*;

// "Tiger" is an optional spell-checker module. If "Tiger" family of packages is not included with the source, simply comment out this part.
import comx.Tiger.gui.*;

/**
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:  Component for typing message in either PLAIN or HTML mode.
 *
 *
 * Class Details: put text component on this to isolate layout calculations when switching PLAIN/HTML
 *
 *
 * <b>$Revision: 1.20 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class MsgTypeArea extends JPanel implements ComponentContainerI {

  private UndoManagerI undoMngrI;

  private short objType;

  private ContactInfoPanel contactInfoPanel;

  private JPanel jAttachments;

  private JPanel jTextAreaPanel;
  private JComponent jMessage;
  private JTextArea jTextMessage;
  private JComponent jHtmlMessage; // this could be either JEditorPane or Jaguar

  private KeyListener enterKeyListener;
  private KeyListener registeredEnterKeyListener;

  private JMyLinkLikeLabel jHTML;

  private boolean isHTML;
  private String PROPERTY_NAME_isHTML_prefix = "MsgTypeArea" + "_isHTML";
  private String PROPERTY_NAME_isHTML;
  
  private boolean isChatMode;

  private static boolean JAGUAR_EDITOR_PRESENT = false;
  private static String JAGUAR_CLASS_NAME;

  // "Tiger" is an optional spell-checker module. If "Tiger" family of packages is not included with the source, simply comment out this part.
  private Object tigerBkgChecker = null;
  //private TigerBkgChecker tigerBkgChecker = null;

  static {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "static()");
    try {
      //new comx.gui.Jaguar.MyJaguar();
      JAGUAR_CLASS_NAME = "comx.Jaguar.gui.MyJaguar";
      Class.forName(JAGUAR_CLASS_NAME).newInstance();
      JAGUAR_EDITOR_PRESENT = true;
    } catch (Throwable t) {
      if (trace != null) trace.exception(MsgTypeArea.class, 100, t);
    }
    if (trace != null) trace.exit(MsgTypeArea.class);
  }

  public static String formatShortCode(String code) {
    if (JAGUAR_EDITOR_PRESENT) {
      return comx.Jaguar.gui.MyJaguar.formatShortCode(code);
    } else {
      return null;
    }
  }

  /** Creates new MsgTypeArea */
  public MsgTypeArea(String htmlPropertyPostfix, short objType, boolean defaultHTML, UndoManagerI undoMngrI, boolean grabInitialFocus, boolean suppressSpellCheck, boolean isChatMode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "MsgTypeArea(String htmlPropertyPostfix, short objType, boolean defaultHTML, UndoManagerI undoMngrI, boolean grabInitialFocus, boolean suppressSpellCheck, boolean isChatMode)");

    PROPERTY_NAME_isHTML = PROPERTY_NAME_isHTML_prefix + htmlPropertyPostfix;
    this.objType = objType;
    String isHTML_s = GlobalProperties.getProperty(PROPERTY_NAME_isHTML, ""+defaultHTML);
    this.isHTML = isHTML_s.equalsIgnoreCase("true");
    this.undoMngrI = undoMngrI;
    this.isChatMode = isChatMode;

    init();

    if (grabInitialFocus) {
      getTextComponent().addHierarchyListener(new InitialFocusRequestor());
    }

    if (!suppressSpellCheck) {
      // Create spell checker for the message
      // "Tiger" is an optional spell-checker module. If "Tiger" family of packages is not included with the source, simply comment out this part.
      try {
        tigerBkgChecker = new TigerBkgChecker(SingleTigerSession.getSingleInstance());
        ((TigerBkgChecker)tigerBkgChecker).restart(getTextComponent());
      } catch (Throwable t) {
      }
    }

    // Cycle caret and text to fix the bug of inserting emoticos into the header 
    // instead of body when this field did not have focus or no click on it.
    pressedHTML(false, true);
    pressedHTML(false, true);

    if (trace != null) trace.exit(MsgTypeArea.class);
  }

  private void init() {
    initComponents();
    initMainPanel();
  }

  private void initComponents() {

    if (objType == MsgDataRecord.OBJ_TYPE_ADDR) {
      contactInfoPanel = new ContactInfoPanel(undoMngrI);
    }

    if (isAttachmentPanelEmbeded()) {
      jAttachments = new JPanel();
      jAttachments.setLayout(new FlowLayout(FlowLayout.LEFT));
      jAttachments.setBorder(new EmptyBorder(0,0,0,0));
    }

    jTextAreaPanel = new JPanel();
    jTextAreaPanel.setLayout(new BorderLayout(0, 0));

    if (isHTML) {
      jHTML = new JMyLinkLikeLabel("Plain Text", -2);
    } else {
      jHTML = new JMyLinkLikeLabel("Rich Text", -2);
    }
    jHTML.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        e.consume();
        pressedHTML(true);
        getTextComponent().requestFocus();
      }
    });

    enterKeyListener = new EnterKeyListener();

    jTextMessage = new JMyTextArea();
    jTextMessage.setAutoscrolls(true);
    jTextMessage.setWrapStyleWord(true);
    jTextMessage.setLineWrap(true);
    jTextMessage.setEditable(true);
    jTextMessage.setEnabled(true);
    jTextMessage.setBorder(new EmptyBorder(0, 0, 0, 0));
    jTextMessage.addKeyListener(enterKeyListener);

    // <<< begin
    if (JAGUAR_EDITOR_PRESENT) {
      try {
        jHtmlMessage = (JComponent) Class.forName(JAGUAR_CLASS_NAME).newInstance();
        JTextComponent textComp = getHTMLTextPane();
        textComp.addKeyListener(enterKeyListener);
//        if (isChatMode) {
//          System.out.println("jag: Chat mode ON, setting custom HTML Editor Kit");
//        } else {
//          System.out.println("jag: Chat mode OFF");
//        }
//        if (textComp instanceof JEditorPane) {
//          if (isChatMode)
//            ((JEditorPane) textComp).setEditorKit(new HTML_EditorKit());
//          else
//            ((JEditorPane) textComp).setEditorKit(new HTMLEditorKit());
//        }
      } catch (Throwable t) {
      }
    }
    // no Jaguar...
    if (jHtmlMessage == null) {
      HTML_ClickablePane htmlPane = new HTML_ClickablePane("", null);
      htmlPane.setAutoscrolls(true);
      htmlPane.setContentType("text/html");
      htmlPane.setEditable(true);
      htmlPane.setEnabled(true);
      htmlPane.addKeyListener(enterKeyListener);
      jHtmlMessage = htmlPane;
    }
    // >>> end

    jHtmlMessage.setBorder(new EmptyBorder(0, 0, 0, 0));

    if (isHTML) {
      jMessage = jHtmlMessage;
    } else {
      jMessage = jTextMessage;
    }

    if (undoMngrI != null)
      addUndoableEditListener(new MsgUndoableEditListener(undoMngrI));
  }

  private void initMainPanel() {
    setLayout(new GridBagLayout());

    int posY = 0;

    if (objType == MsgDataRecord.OBJ_TYPE_ADDR) {
      add(contactInfoPanel, new GridBagConstraints(0, posY, 2, 1, 10, 0, 
          GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
      posY ++;
    }

    if (isAttachmentPanelEmbeded()) {
      add(getAttachmentsPanel(), new GridBagConstraints(0, posY, 2, 1, 10, 0, 
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 2, 0), 0, 0));
      posY ++;
    }

    if (objType == MsgDataRecord.OBJ_TYPE_ADDR) {
      add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Notes")), new GridBagConstraints(0, posY, 1, 1, 10, 0, 
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
      add(jHTML, new GridBagConstraints(1, posY, 1, 1, 0, 0, 
          GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new MyInsets(0, 0, 0, 0), 0, 0));
      posY ++;
    }

    add(jTextAreaPanel, new GridBagConstraints(0, posY, 2, 1, 10, 10, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    posY ++;

    if (jMessage instanceof JTextComponent) {
      jTextAreaPanel.add(new JScrollPane(jMessage), BorderLayout.CENTER);
    } else {
      jTextAreaPanel.add(jMessage, BorderLayout.CENTER);
    }

  }

  public void addDocumentListener(DocumentListener l) {
    jTextMessage.getDocument().addDocumentListener(l);
    getHTMLTextPane().getDocument().addDocumentListener(l);
  }
  public void removeDocumentListener(DocumentListener l) {
    jTextMessage.getDocument().removeDocumentListener(l);
    getHTMLTextPane().getDocument().removeDocumentListener(l);
  }

  private void addUndoableEditListener(UndoableEditListener l) {
    getHTMLTextPane().getDocument().addUndoableEditListener(l);
    jTextMessage.getDocument().addUndoableEditListener(l);
  }
  private void removeUndoableEditListener(UndoableEditListener l) {
    getHTMLTextPane().getDocument().removeUndoableEditListener(l);
    jTextMessage.getDocument().removeUndoableEditListener(l);
  }

  private String getSubject() {
    String subject = null;
    if (objType == MsgDataRecord.OBJ_TYPE_ADDR) {
      subject = contactInfoPanel.getContentPreview().toString();
    }
    return subject;
  }

  public JMyLinkLikeLabel getHTMLSwitchButton() {
    return jHTML;
  }

  public String[] getContent() {
    String subject = "";
    String body = "";
    try {
      subject = getSubject();
      body = getTextComponent().getText().trim();
      String textMode = getTextMode();
      if (objType == MsgDataRecord.OBJ_TYPE_MSG) {
        if (textMode.equalsIgnoreCase("text/html")) {
          body = removeHEADtag(body);
        }
      }
      if (objType == MsgDataRecord.OBJ_TYPE_ADDR) {
        XMLElement content = contactInfoPanel.getContent();
        if (isAnyText()) {
          XMLElement notes = new XMLElement();
          notes.setName("Notes");
          notes.setAttribute("type", textMode);
          notes.setContent(body);
          content.addChild(notes);
        }
        body = content.toString();
      }
    } catch (Throwable t) {
    }
    return new String[] { subject, body };
  }
  public void setContentText(String text, boolean requestFocus, boolean caretOnTop) {
    JTextComponent textComp = getTextComponent();
    textComp.setText(text);
    // cycle through setText to remove formatting bugs
    textComp.setText(textComp.getText());
    if (caretOnTop)
      setCaretAtTheTop();
    if (requestFocus)
      textComp.requestFocus();
  }
  public void setContent(MsgDataRecord msgData) {
    if (msgData.isTypeAddress()) {
      contactInfoPanel.setContent(msgData.parseAddressContent());
      // make sure we are in proper PLAIN/HTML mode
      if (msgData.addressNotes != null) {
        boolean isHtml = !msgData.addressNotes.getAttribute("type").equals("text/plain");
        setHTML(isHtml, false);
        setContentText(msgData.addressNotes.getContent(), false, true);
      }
    } else {
      // make sure we are in proper PLAIN/HTML mode
      setHTML(msgData.isHtmlMail(), false);
      // set content
      setContentText(msgData.getText(), false, true);
    }
  }
  public void setContent(XMLElement data) {
    if (objType == MsgDataRecord.OBJ_TYPE_ADDR) {
      contactInfoPanel.setContent(data);
    }
  }

  public boolean isAnyContent() {
    boolean anyContent = isAnyText();
    if (!anyContent && objType == MsgDataRecord.OBJ_TYPE_ADDR) {
      anyContent = contactInfoPanel.isAnyContent();
    }
    return anyContent;
  }
  private boolean isAnyText() {
    Document contentDoc = getTextComponent().getDocument();
    int contentLen = contentDoc.getLength();
    boolean anyText = false;
    try {
      anyText = contentLen > 0 && contentDoc.getText(0, contentLen).trim().length() > 0;
    } catch (Throwable t) {
    }
    return anyText;
  }

  public boolean isSufficientContent() {
    boolean sufficient = false;
    if (objType == MsgDataRecord.OBJ_TYPE_ADDR) {
      sufficient = contactInfoPanel.isNameAndEmailPresent();
    } else if (objType == MsgDataRecord.OBJ_TYPE_MSG) {
      sufficient = isAnyText();
    }
    return sufficient;
  }

  public void setEnabled(boolean b) {
    getTextComponent().setEnabled(b);
  }

  public void setEditable(boolean b) {
    getTextComponent().setEditable(b);
  }

  public JPanel getAttachmentsPanel() {
    return jAttachments;
  }

  public Short getContentType() {
    return new Short(objType);
  }

  public short getContentMode() {
    short mode = 0;
    short type = getContentType().shortValue();
    if (type == MsgDataRecord.OBJ_TYPE_MSG) {
      String textMode = getTextMode();
      if (textMode.equalsIgnoreCase("text/plain")) {
        mode = MsgComposePanel.CONTENT_MODE_MAIL_PLAIN;
      } else if (textMode.equalsIgnoreCase("text/html")) {
        mode = MsgComposePanel.CONTENT_MODE_MAIL_HTML;
      }
    } else if (type == MsgDataRecord.OBJ_TYPE_ADDR) {
      mode = MsgComposePanel.CONTENT_MODE_ADDRESS_BOOK_ENTRY;
    }
    return mode;
  }

  private String getTextMode() {
    String contentType = isHTML ? "text/html" : "text/plain";
    /*
    JTextComponent textComp = getTextComponent();
    String contentType = "text/plain";
    // see if the text component might be of html content type
    if (textComp instanceof JEditorPane) {
      contentType = ((JEditorPane) textComp).getContentType();
    }*/
    return contentType;
  }

  public boolean isAttachmentPanelEmbeded() {
    return objType == MsgDataRecord.OBJ_TYPE_ADDR;
  }
  public boolean isSubjectGenerated() {
    return objType == MsgDataRecord.OBJ_TYPE_ADDR;
  }

  public boolean isHTML() {
    return isHTML;
  }
  public void setHTML(boolean modeIsHTML, boolean propertyUpdate) {
    if (isHTML != modeIsHTML) {
      pressedHTML(propertyUpdate);
    }
  }

  /**
   * @return current text component used in the message composition.
   */
  protected JTextComponent getTextComponent() {
    JTextComponent textComp = null;
    if (jMessage instanceof JTextComponent) {
      textComp = (JTextComponent) jMessage;
    } else {
      try {
        if (jMessage instanceof TextEditorI) {
          textComp = ((TextEditorI) jMessage).getTextPane();
        }
      } catch (Throwable t) {
      }
    }
    return textComp;
  }

  private JTextComponent getHTMLTextPane() {
    if (jHtmlMessage instanceof JTextComponent) {
      return (JTextComponent) jHtmlMessage;
    } else {
      try {
        if (jHtmlMessage instanceof TextEditorI)
          return ((TextEditorI) jHtmlMessage).getTextPane();
      } catch (Throwable t) {
      }
    }
    return null;
  }

  private void pressedHTML(boolean propertyUpdate) {
    pressedHTML(propertyUpdate, false);
  }
  private void pressedHTML(boolean propertyUpdate, boolean skipSwitch) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "pressedHTML()");

    try {
      if (!skipSwitch)
        isHTML = !isHTML;
      if (trace != null) trace.data(10, "isHTML", isHTML);
      if (propertyUpdate) {
        GlobalProperties.setProperty(PROPERTY_NAME_isHTML, String.valueOf(isHTML));
      }
      JTextComponent textComp = getTextComponent();

      boolean isEditable = textComp.isEditable();
      boolean isEnabled = textComp.isEnabled();

      String currentText = textComp.getText();
      String text = null;
      if (skipSwitch) {
        text = currentText;
      } else {
        if (isHTML)
          text = Misc.encodePlainIntoHtml(currentText);
        else 
          text = MsgPanelUtils.extractPlainFromHtml(currentText);
      }
      if (isHTML) {
        jMessage = jHtmlMessage;
        //jHTML.setIcon(Images.get(ImageNums.TO_PLAIN40_16));
        jHTML.setText("Plain Text");
        // if there is no <html> or <p> then insert text between HTML armor
        String lowerText = text.toLowerCase();
        if (text.length() == 0 || text.trim().length() == 0) {
          text = "<html><body><p>" + text + "&nbsp;</p></body></html>";
        } else if (lowerText.indexOf("<html>") < 0 && lowerText.indexOf("<p>") < 0) {
          text = "<html><body><p>" + text + "</p></body></html>";
        }
        // condition the text through a test HTML component
        try {
          JEditorPane testPane = new JMyEditorPane("text/html", text);
          text = testPane.getText();
        } catch (Throwable t) {
        }
      } else {
        jMessage = jTextMessage;
        //jHTML.setIcon(Images.get(ImageNums.TO_HTML40_16));
        jHTML.setText("Rich Text");
      }
      textComp = getTextComponent();
      textComp.setText(text);
      switchMessageArea(jMessage);

      textComp.setEnabled(isEnabled);
      textComp.setEditable(isEditable);

      // fix the content and focus bugs in swing by repeated call to following...
      textComp.setText(textComp.getText());
      setCaretAtTheTop();
    } catch (Throwable t) {
    }

    if (trace != null) trace.exit(MsgTypeArea.class);
  }

  private void switchMessageArea(JComponent newTextComponent) {
    // "Tiger" is an optional spell-checker module. If "Tiger" family of packages is not included with the source, simply comment out this part.
    if (tigerBkgChecker != null) ((TigerBkgChecker)tigerBkgChecker).stop();
    jTextAreaPanel.removeAll();
    if (jMessage instanceof JTextComponent) {
      jTextAreaPanel.add(new JScrollPane(jMessage), BorderLayout.CENTER);
    } else {
      jTextAreaPanel.add(jMessage, BorderLayout.CENTER);
    }
    revalidate();
    repaint();
    if (undoMngrI != null) {
      undoMngrI.getUndoManager().discardAllEdits();
      undoMngrI.setEnabledUndoAndRedo();
    }
    // "Tiger" is an optional spell-checker module. If "Tiger" family of packages is not included with the source, simply comment out this part.
    if (tigerBkgChecker != null) ((TigerBkgChecker)tigerBkgChecker).restart(getTextComponent());
  }

  public void setCaretAtTheTop() {
    JTextComponent textComp = getTextComponent();
    Caret caret = textComp.getCaret();
//    if (caret != null) {
//      caret.setVisible(true);
//      caret.setSelectionVisible(true);
//    }
    boolean caretSet = false;
    try {
      Document d = textComp.getDocument();
      String content = d.getText(0, d.getLength());
      // if empty content...
      if ((content.length() > 0 && content.trim().length() == 0) || content.equals("\n") || content.equals("\r\n") || content.equals("\n\r")) {
        d.remove(0, d.getLength());
        textComp.setCaretPosition(0);
        caretSet = true;
      }
    } catch (Throwable t) {
    }
    if (!caretSet) {
      for (int i=0; i<40; i++) {
        try {
          textComp.setCaretPosition(i);
          caretSet = true;
        } catch (Throwable t) {
        }
        if (caretSet) {
          break;
        }
      }
    }
  }

  public void clearMessageArea() {
    JTextComponent textComp = getTextComponent();
    if (!isHTML) {
      textComp.setText("");
      textComp.setCaretPosition(0);
    } else if (isHTML && textComp instanceof JEditorPane) {
      Document doc = textComp.getDocument();
      try {
        // remove the document to keep the "hidden" stuff like background color
        doc.remove(0, doc.getLength());
        doc.remove(0, doc.getLength());
        textComp.setCaretPosition(1);
      } catch (Throwable e) {
      }
    } else {
      throw new IllegalStateException("Don't know how to clear HTML area without proper handling component!");
    }
  }

  public Component[] getPotentiallyHiddenComponents() {
    Component[] components = new Component[] { jTextMessage, jHtmlMessage };
    return components;
  }


  /**
   * Strip down the HEAD tag
   */
  private static String removeHEADtag(String str) {
    String rc = str;
    String h1 = "<head>";
    String h2 = "</head>";
    int iStart = str.indexOf(h1);
    int iEnd = str.indexOf(h2);
    if (iStart >= 0 && iEnd >= 0 && iStart < iEnd)
      rc = str.substring(0, iStart) + str.substring(iEnd + h2.length());
    return rc;
  }

  public void setEnterKeyListener(KeyListener enterKeyListener) {
    registeredEnterKeyListener = enterKeyListener;
  }

  private class EnterKeyListener extends KeyAdapter {
    public void keyPressed(KeyEvent e) {
      char ch = e.getKeyChar();
      if (ch == '\n' || ch == '\r') {
        // notify listeners for SEND action
        if (registeredEnterKeyListener != null) {
          registeredEnterKeyListener.keyPressed(e);
        }
      }
    }
  }

}
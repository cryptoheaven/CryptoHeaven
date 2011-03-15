/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
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

import com.CH_co.nanoxml.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.DisposableObj;
import com.CH_co.util.Misc;

import com.CH_gui.addressBook.*;
import com.CH_gui.gui.*;
import com.CH_gui.util.*;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;

/**
 * <b>Copyright</b> &copy; 2001-2011
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
public class MsgTypeArea extends JPanel implements DisposableObj {

  private UndoManagerI undoMngrI;

  private short objType;

  private ContactInfoPanel contactInfoPanel;

  private JPanel jAttachments;

  private MyHTMLEditor jMessage;

  private DocumentListener myDocumentListener;
  private DocumentListener registeredDocumentListener;
  private KeyListener myKeyListener;
  private KeyListener registeredEnterKeyListener;
  private MsgUndoableEditListener undoableEditListener;

  private boolean isChatMode;

  /** Creates new MsgTypeArea */
  public MsgTypeArea(short objType, UndoManagerI undoMngrI, boolean grabInitialFocus, boolean suppressSpellCheck, boolean isChatMode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "MsgTypeArea(short objType, UndoManagerI undoMngrI, boolean grabInitialFocus, boolean suppressSpellCheck, boolean isChatMode)");

    this.objType = objType;
    this.undoMngrI = undoMngrI;
    this.isChatMode = isChatMode;

    init(suppressSpellCheck);

    if (grabInitialFocus) {
      getTextComponent().addHierarchyListener(new InitialFocusRequestor());
    }

    // avoid super small sizing especially in chat entry panel
    setMinimumSize(new Dimension(70, 70));

    if (trace != null) trace.exit(MsgTypeArea.class);
  }

  private void init(boolean suppressSpellCheck) {
    try { initComponents(suppressSpellCheck); } catch (Throwable t) { t.printStackTrace(); }
    try { addMyListeners(); } catch (Throwable t) { t.printStackTrace(); }
    try { initMainPanel(); } catch (Throwable t) { t.printStackTrace(); }
  }

  private void initComponents(boolean suppressSpellCheck) {

    if (objType == MsgDataRecord.OBJ_TYPE_ADDR) {
      contactInfoPanel = new ContactInfoPanel(undoMngrI);
    }

    if (isAttachmentPanelEmbeded()) {
      jAttachments = new JPanel();
      jAttachments.setLayout(new FlowLayout(FlowLayout.LEFT));
      jAttachments.setBorder(new EmptyBorder(0,0,0,0));
    }

    myKeyListener = new MyKeyListener();
    myDocumentListener = new MyDocumentListener();

    if (isChatMode || objType == MsgDataRecord.OBJ_TYPE_ADDR || objType == -1)
      jMessage = new MyHTMLEditor(true, suppressSpellCheck);
    else
      jMessage = new MyHTMLEditor(false, suppressSpellCheck);

    if (undoMngrI != null)
      undoableEditListener = new MsgUndoableEditListener(undoMngrI);
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
      posY ++;
    }

    if (!isChatMode || objType == MsgDataRecord.OBJ_TYPE_ADDR || objType == -1) {
      add(getToolBar(), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(0, 0, 0, 0), 0, 0));
      add(new JLabel(), new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
      posY ++;
    }
    add(jMessage, new GridBagConstraints(0, posY, 2, 1, 10, 10,
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
  }

  private String getSubject() {
    String subject = null;
    if (objType == MsgDataRecord.OBJ_TYPE_ADDR) {
      subject = contactInfoPanel.getContentPreview().toString();
    }
    return subject;
  }

  public JComponent getToolBar() {
    return jMessage.getActionsPanel();
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
        if (isAnyBody()) {
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
  public void setContentText(String text, boolean isHtml, boolean requestFocus, boolean caretOnTop) {
    setContent(isHtml ? text : Misc.encodePlainIntoHtml(text));
    if (caretOnTop)
      setCaretAtTheTop();
    if (requestFocus)
      jMessage.requestFocus();
  }
  public void setContent(MsgDataRecord msgData) {
    if (msgData.isTypeAddress()) {
      contactInfoPanel.setContent(msgData.parseAddressContent());
      // make sure we are in proper PLAIN/HTML mode
      if (msgData.addressNotes != null) {
        boolean isHtml = !msgData.addressNotes.getAttribute("type").equals("text/plain");
        setContentText(msgData.addressNotes.getContent(), isHtml, false, true);
      }
    } else {
      // set content
      setContentText(msgData.getText(), msgData.isHtmlMail(), false, true);
    }
  }
  public void setContent(XMLElement data) {
    if (objType == MsgDataRecord.OBJ_TYPE_ADDR) {
      contactInfoPanel.setContent(data);
    }
  }
  private void setContent(String htmlBody) {
    removeMyListeners();
    jMessage.setContent(htmlBody);
    addMyListeners();
    HTML_ClickablePane.setBaseToDefault((HTMLDocument) jMessage.getInternalJEditorPane().getDocument());
  }
  private void addMyListeners() {
    jMessage.getInternalJEditorPane().getDocument().addDocumentListener(myDocumentListener);
    jMessage.getInternalJEditorPane().addKeyListener(myKeyListener);
    if (undoableEditListener != null)
      jMessage.getInternalJEditorPane().getDocument().addUndoableEditListener(undoableEditListener);
  }
  private void removeMyListeners() {
    jMessage.getInternalJEditorPane().getDocument().removeDocumentListener(myDocumentListener);
    jMessage.getInternalJEditorPane().removeKeyListener(myKeyListener);
    if (undoableEditListener != null)
      jMessage.getInternalJEditorPane().getDocument().removeUndoableEditListener(undoableEditListener);
  }
  public boolean isAnyContent() {
    boolean anyContent = isAnyBody();
    if (!anyContent && objType == MsgDataRecord.OBJ_TYPE_ADDR) {
      anyContent = contactInfoPanel.isAnyContent();
    }
    return anyContent;
  }
  private boolean isAnyBody() {
    boolean anyBody = jMessage.getPlainText().trim().length() > 0;
    if (!anyBody) {
      String content = jMessage.getContent();
      if (content.contains("<img ") || content.contains("<IMG "))
        anyBody = true;
    }
    return anyBody;
  }

  public boolean isSufficientContent() {
    boolean sufficient = false;
    if (objType == MsgDataRecord.OBJ_TYPE_ADDR) {
      sufficient = contactInfoPanel.isNameAndEmailPresent();
    } else if (objType == MsgDataRecord.OBJ_TYPE_MSG) {
      sufficient = isAnyBody();
    }
    return sufficient;
  }

  public void setEnabled(boolean b) {
    jMessage.setEnabled(b);
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
      mode = MsgComposePanel.CONTENT_MODE_MAIL_HTML;
    } else if (type == MsgDataRecord.OBJ_TYPE_ADDR) {
      mode = MsgComposePanel.CONTENT_MODE_ADDRESS_BOOK_ENTRY;
    }
    return mode;
  }

  private String getTextMode() {
    return "text/html";
  }

  public boolean isAttachmentPanelEmbeded() {
    return objType == MsgDataRecord.OBJ_TYPE_ADDR;
  }
  public boolean isSubjectGenerated() {
    return objType == MsgDataRecord.OBJ_TYPE_ADDR;
  }

  public boolean isHTML() {
    return true;
  }

  /**
   * @return current text component used in the message composition.
   */
  protected JTextComponent getTextComponent() {
    return jMessage.getInternalJEditorPane();
  }

  public void focusMessageArea() {
    getTextComponent().requestFocus();
  }

  public void setCaretAtTheTop() {
    JTextComponent textComp = getTextComponent();
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
    setContent("");
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

  public void setDocumentListener(DocumentListener documentListener) {
    registeredDocumentListener = documentListener;

  }

  public void setEnterKeyListener(KeyListener enterKeyListener) {
    registeredEnterKeyListener = enterKeyListener;
  }

  public void disposeObj() {
    removeMyListeners();
    remove(jMessage);
  }

  private class MyDocumentListener implements DocumentListener {
    public void insertUpdate(DocumentEvent e) {
      if (registeredDocumentListener != null)
        registeredDocumentListener.insertUpdate(e);
    }
    public void removeUpdate(DocumentEvent e) {
      if (registeredDocumentListener != null)
        registeredDocumentListener.removeUpdate(e);
    }
    public void changedUpdate(DocumentEvent e) {
      if (registeredDocumentListener != null)
        registeredDocumentListener.changedUpdate(e);
    }
  }
  private class MyKeyListener extends KeyAdapter {
    public void keyPressed(KeyEvent e) {
      char ch = e.getKeyChar();
      int code = e.getKeyCode();
      if (ch == '\n' || ch == '\r') {
        // notify listeners for SEND action
        if (registeredEnterKeyListener != null) {
          registeredEnterKeyListener.keyPressed(e);
        }
      }
      // workaround the buggy Ctrl-V paste, pasting multiple lines on plain text are pasted all in 1 line and this seems to fix that
      else if (code == KeyEvent.VK_V && e.isControlDown() && !e.isShiftDown() && !e.isAltDown() && !e.isAltGraphDown()) {
        // check if we are forced to paste plain text, if so use the special call to create new lines
        DataFlavor[] flavors = Toolkit.getDefaultToolkit().getSystemClipboard().getAvailableDataFlavors();
        if (flavors != null) {
          boolean isFormattedAvailable = false;
          boolean isPlainAvailable = false;
          for (int i=0; i<flavors.length; i++) {
            String mimeType = flavors[i].getMimeType();
            if (mimeType.startsWith("text/html") || mimeType.startsWith("text/rtf"))
              isFormattedAvailable = true;
            else if (mimeType.startsWith("text/plain"))
              isPlainAvailable = true;
            if (isFormattedAvailable && isPlainAvailable)
              break;
          }
          if (!isFormattedAvailable && isPlainAvailable) {
            jMessage.pastePlainTextFromClipboard(true);
            e.consume();
          } else if (isFormattedAvailable) {
            jMessage.pasteFormattedTextFromClipboard();
            // terminate any link at the end of pasted content
            jMessage.insertContent("&nbsp;");
            e.consume();
          }
        }
      }
    }
  }

}
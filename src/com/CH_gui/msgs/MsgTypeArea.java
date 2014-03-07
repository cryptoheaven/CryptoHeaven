/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.msgs;

import com.CH_co.nanoxml.XMLElement;
import com.CH_co.service.records.MsgDataRecord;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;
import com.CH_gui.addressBook.ContactInfoPanel;
import com.CH_gui.gui.*;
import com.CH_gui.util.ComponentContainerI;
import com.CH_gui.util.HTML_ClickablePane;
import com.CH_gui.util.SpellCheckerI;
import com.CH_gui.util.SpellCheckerWrapper;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.parser.ParserDelegator;

/**
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* Class Description:  Component for typing message in either PLAIN or HTML mode.
*
* Class Details: put text component on this to isolate layout calculations when switching PLAIN/HTML
*
*
* <b>$Revision: 1.20 $</b>
*
* @author  Marcin Kurzawa
*/
public class MsgTypeArea extends JPanel implements ComponentContainerI, DisposableObj {

  private UndoManagerI undoMngrI;

  private short objType;

  private ContactInfoPanel contactInfoPanel;

  private JPanel jAttachments;

  private JPanel jTextAreaPanel;
  private JPanel jTextActionPanel;
  private JComponent jMessage;
  private JTextArea jTextMessage;
  private MyHTMLEditor jHtmlMessage; // this could be either JEditorPane or Jaguar

  private DocumentListener myDocumentListener;
  private DocumentListener registeredDocumentListener;
  private KeyListener myKeyListener;
  private KeyListener registeredEnterKeyListener;
  private MsgUndoableEditListener undoableEditListener;

  private JButton jHTML;

  private boolean isHTML;
  private String PROPERTY_NAME_isHTML_prefix = "MsgTypeArea" + "_isHTML";
  private String PROPERTY_NAME_isHTML;

  private boolean isChatMode;

  private String prevHtmlText, prevPlainText, setHtmlText, setPlainText;

  private SpellCheckerI tigerBkgChecker = null;

  /** Creates new MsgTypeArea */
  public MsgTypeArea(String htmlPropertyPostfix, short objType, boolean defaultHTML, UndoManagerI undoMngrI, boolean suppressSpellCheck, boolean isChatMode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "MsgTypeArea(String htmlPropertyPostfix, short objType, boolean defaultHTML, UndoManagerI undoMngrI, boolean suppressSpellCheck, boolean isChatMode)");
    if (trace != null) trace.args(htmlPropertyPostfix);
    if (trace != null) trace.args(objType);
    if (trace != null) trace.args(defaultHTML);
    if (trace != null) trace.args(undoMngrI);
    if (trace != null) trace.args(suppressSpellCheck);
    if (trace != null) trace.args(isChatMode);

    PROPERTY_NAME_isHTML = PROPERTY_NAME_isHTML_prefix + htmlPropertyPostfix;
    this.objType = objType;
    String isHTML_s = GlobalProperties.getProperty(PROPERTY_NAME_isHTML, ""+defaultHTML);
    // chat mode overwrites into HTML
    this.isHTML = isChatMode || isHTML_s.equalsIgnoreCase("true");
    this.undoMngrI = undoMngrI;
    this.isChatMode = isChatMode;

    init();

    // Create spell checker for the message
    if (!suppressSpellCheck) {
      try {
        tigerBkgChecker = SpellCheckerWrapper.getSpellChecker();
        if (tigerBkgChecker != null) {
          tigerBkgChecker.restart(getTextComponent());
          jHtmlMessage.getInternalJEditorPane().addMouseListener(SpellCheckerWrapper.newSpellCheckerMouseAdapter(jHtmlMessage));
          if (jTextMessage != null)
            jTextMessage.addMouseListener(SpellCheckerWrapper.newSpellCheckerMouseAdapter(null));
        }
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }

    // avoid super small sizing especially in chat entry panel
    setMinimumSize(new Dimension(70, 70));

    if (trace != null) trace.exit(MsgTypeArea.class);
  }

  private void init() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "init()");

    initComponents();
    addMyListeners();
    initMainPanel();

    if (trace != null) trace.exit(MsgTypeArea.class);
  }

  private void initComponents() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "initComponents()");

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
    jTextActionPanel = new JPanel();
    jTextActionPanel.setLayout(new GridBagLayout());
    jTextActionPanel.setVisible(isHTML);

    ParserDelegator workaround = new ParserDelegator();

    if (!isChatMode) {
      if (isHTML)
        jHTML = new JMyButtonNoFocus("Plain Text");
      else
        jHTML = new JMyButtonNoFocus("Rich Text");
      jHTML.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(0, 2, 0, 2)));
      jHTML.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          e.consume();
          pressedHTML(true);
          focusMessageArea();
        }
      });
      jTextMessage = new JMyTextArea();
      jTextMessage.setAutoscrolls(true);
      jTextMessage.setWrapStyleWord(true);
      jTextMessage.setLineWrap(true);
      jTextMessage.setEditable(true);
      jTextMessage.setEnabled(true);
      jTextMessage.setMargin(UIManager.getInsets("EditorPane.margin")); // matching to the corresponding HTML component
      jTextMessage.addKeyListener(myKeyListener);

      Font font = UIManager.getFont("Label.font");
      font = font.deriveFont(Font.PLAIN, 14.0f);
      jTextMessage.setFont(font);
    }

    myKeyListener = new MyKeyListener();
    myDocumentListener = new MyDocumentListener();

    if (isChatMode || objType == MsgDataRecord.OBJ_TYPE_ADDR || objType == -1)
      jHtmlMessage = new MyHTMLEditor(true, true);
    else
      jHtmlMessage = new MyHTMLEditor(false, true);

    if (isHTML || jTextMessage == null)
      jMessage = jHtmlMessage;
    else
      jMessage = jTextMessage;

    if (undoMngrI != null)
      undoableEditListener = new MsgUndoableEditListener(undoMngrI);

    if (trace != null) trace.exit(MsgTypeArea.class);
  }

  private void initMainPanel() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "initMainPanel()");

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
      add(new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Notes")), new GridBagConstraints(0, posY, 1, 1, 10, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
      add(jHTML, new GridBagConstraints(1, posY, 1, 1, 0, 0,
          GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new MyInsets(0, 0, 0, 0), 0, 0));
      posY ++;
    }

    if (!isChatMode || objType == MsgDataRecord.OBJ_TYPE_ADDR || objType == -1) {
      jTextActionPanel.add(getToolBar(), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(0, 0, 0, 0), 0, 0));
      jTextActionPanel.add(new JLabel(), new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
      add(jTextActionPanel, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(0, 0, 0, 0), 0, 0));
      posY ++;
    }

    add(jTextAreaPanel, new GridBagConstraints(0, posY, 2, 1, 10, 10,
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    posY ++;

    if (jMessage instanceof JTextComponent)
      jTextAreaPanel.add(new JScrollPane(jMessage), BorderLayout.CENTER);
    else
      jTextAreaPanel.add(jMessage, BorderLayout.CENTER);

    if (trace != null) trace.exit(MsgTypeArea.class);
  }

  private String getSubject() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "getSubject()");

    String subject = null;
    if (objType == MsgDataRecord.OBJ_TYPE_ADDR) {
      subject = contactInfoPanel.getContentPreview().toString();
    }

    if (trace != null) trace.exit(MsgTypeArea.class, subject);
    return subject;
  }

  public JButton getHTMLSwitchButton() {
    return jHTML;
  }

  public JComponent getToolBar() {
    return jHtmlMessage.getActionsPanel();
  }

  public String[] getContent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "getContent()");

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
      // remove pasted images and replace them by [file name]
      {
        final String sTag = "<img src=\"file:";
        final String eTag = "/>";
        String[][] startTags = new String[][] {{ sTag }};
        String[][] endTags = new String[][] {{ eTag }};
        CallbackReturnI callback = new CallbackReturnI() {
          public Object callback(Object value) {
            String str = (String) value;
            int start = str.indexOf(sTag) + sTag.length();
            int end = str.lastIndexOf(eTag);
            String part = str.substring(start, end);
            return " [" + part.substring(part.lastIndexOf('/')+1, part.lastIndexOf('"')) + "] ";
          }
        };
        body = ArrayUtils.removeTags(new StringBuffer(body), startTags, endTags, new Object[] { callback }).toString();
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
    String[] content = new String[] { subject, body };

    if (trace != null) trace.exit(MsgTypeArea.class, content);
    return content;
  }

  public void setContentText(String text, boolean isHtml, boolean requestFocus, boolean caretOnTop) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "setContentText(String text, boolean isHtml, boolean requestFocus, boolean caretOnTop)");
    if (trace != null) trace.args(text);
    if (trace != null) trace.args(isHtml);
    if (trace != null) trace.args(requestFocus);
    if (trace != null) trace.args(caretOnTop);

    if (isHTML && !isHtml)
      text = Misc.encodePlainIntoHtml(text);
    else if (!isHTML && isHtml)
      text = MsgPanelUtils.extractPlainFromHtml(text);
    setContent(text);
    if (caretOnTop)
      setCaretAtTheTop();
    if (requestFocus)
      focusMessageArea();

    if (trace != null) trace.exit(MsgTypeArea.class);
  }

  public void setContent(MsgDataRecord msgData) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "setContent(MsgDataRecord msgData)");
    if (trace != null) trace.args(msgData);

    if (msgData.isTypeAddress()) {
      contactInfoPanel.setContent(msgData.parseAddressContent());
      // make sure we are in proper PLAIN/HTML mode
      if (msgData.addressNotes != null) {
        boolean isHtml = !msgData.addressNotes.getAttribute("type").equals("text/plain");
        setHTML(isHtml);
        setContentText(msgData.addressNotes.getContent(), isHtml, false, true);
      }
    } else {
      // make sure we are in proper PLAIN/HTML mode
      setHTML(msgData.isHtmlMail());
      // set content
      setContentText(msgData.getText(), msgData.isHtmlMail(), false, true);
    }

    if (trace != null) trace.exit(MsgTypeArea.class);
  }

  public void setContent(XMLElement data) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "setContent(XMLElement data)");
    if (trace != null) trace.args(data);

    if (objType == MsgDataRecord.OBJ_TYPE_ADDR) {
      contactInfoPanel.setContent(data);
    }

    if (trace != null) trace.exit(MsgTypeArea.class);
  }

  private void setContent(String body) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "setContent(String body)");
    if (trace != null) trace.args(body);

    if (tigerBkgChecker != null) ((SpellCheckerI) tigerBkgChecker).stop();
    removeMyListeners();
    if (isHTML) {
      jHtmlMessage.setContent(body);
      setHtmlText = jHtmlMessage.getContent();
    } else {
      jTextMessage.setText(body);
      setPlainText = jTextMessage.getText();
    }
    addMyListeners();
    HTML_ClickablePane.setBaseToDefault((HTMLDocument) jHtmlMessage.getInternalJEditorPane().getDocument());
    if (tigerBkgChecker != null) ((SpellCheckerI) tigerBkgChecker).restart(getTextComponent());

    if (trace != null) trace.exit(MsgTypeArea.class);
  }

  private void addMyListeners() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "addMyListeners()");

    addMyListeners(jHtmlMessage.getInternalJEditorPane());
    if (jTextMessage != null)
      addMyListeners(jTextMessage);

    if (trace != null) trace.exit(MsgTypeArea.class);
  }

  private void addMyListeners(JTextComponent textComp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "addMyListeners(JTextComponent textComp)");
    if (trace != null) trace.args(textComp);

    textComp.getDocument().addDocumentListener(myDocumentListener);
    textComp.addKeyListener(myKeyListener);
    if (undoableEditListener != null)
      textComp.getDocument().addUndoableEditListener(undoableEditListener);

    if (trace != null) trace.exit(MsgTypeArea.class);
  }

  private void removeMyListeners() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "removeMyListeners()");

    removeMyListeners(jHtmlMessage.getInternalJEditorPane());
    if (jTextMessage != null)
      removeMyListeners(jTextMessage);

    if (trace != null) trace.exit(MsgTypeArea.class);
  }

  private void removeMyListeners(JTextComponent textComp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "removeMyListeners(JTextComponent textComp)");
    if (trace != null) trace.args(textComp);

    textComp.getDocument().removeDocumentListener(myDocumentListener);
    textComp.removeKeyListener(myKeyListener);
    if (undoableEditListener != null)
      textComp.getDocument().removeUndoableEditListener(undoableEditListener);

    if (trace != null) trace.exit(MsgTypeArea.class);
  }

  public boolean isAnyContent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "isAnyContent()");

    boolean anyContent = isAnyBody();
    if (!anyContent && objType == MsgDataRecord.OBJ_TYPE_ADDR) {
      anyContent = contactInfoPanel.isAnyContent();
    }

    if (trace != null) trace.exit(MsgTypeArea.class, anyContent);
    return anyContent;
  }

  private boolean isAnyBody() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "isAnyBody()");

    boolean anyBody = false;
    if (isHTML) {
      anyBody = jHtmlMessage.getPlainText().trim().length() > 0;
      if (!anyBody) {
        String content = jHtmlMessage.getContent();
        if (content.indexOf("<img ") >= 0 || content.indexOf("<IMG ") >= 0)
          anyBody = true;
      }
    } else {
      anyBody = jTextMessage.getText().trim().length() > 0;
    }

    if (trace != null) trace.exit(MsgTypeArea.class, anyBody);
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
  public void setHTML(boolean modeIsHTML) {
    if (isHTML != modeIsHTML) {
      pressedHTML(false);
    }
  }

  /**
  * @return current text component used in the message composition.
  */
  protected JTextComponent getTextComponent() {
    JTextComponent textComp = null;
    if (isHTML)
      textComp = jHtmlMessage.getInternalJEditorPane();
    else
      textComp = jTextMessage;
    return textComp;
  }

  private void pressedHTML(boolean isUserAction) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "pressedHTML()");

    try {
      if (trace != null) trace.data(10, "isHTML", isHTML);
      JTextComponent textComp = getTextComponent();
      boolean isEditable = textComp.isEditable();
      boolean isEnabled = textComp.isEnabled();
      String currentText = textComp.getText();

      // after grabbing current text component and content, switch the view type
      isHTML = !isHTML;
      if (isUserAction)
        GlobalProperties.setProperty(PROPERTY_NAME_isHTML, String.valueOf(isHTML));

      if (isHTML) {
        jMessage = jHtmlMessage;
        //
        if (isUserAction)
          prevPlainText = currentText;
        // if the Plain editor has no change since last setting, use the saved Html version to restore formatting
        if (prevHtmlText != null && setPlainText != null && setPlainText.equals(currentText))
          setContentText(prevHtmlText, true, false, true);
        else
          setContentText(currentText, false, false, true);
        if (!isChatMode)
          jHTML.setText("Plain Text");
      } else {
        jMessage = jTextMessage;
        if (isUserAction)
          prevHtmlText = currentText;
        // if the Html editor has no change since last setting, use the saved Plain version to restore formatting
        if (prevPlainText != null && setHtmlText != null && setHtmlText.equals(currentText))
          setContentText(prevPlainText, false, false, true);
        else
          setContentText(currentText, true, false, true);
        if (!isChatMode)
          jHTML.setText("Rich Text");
      }
      textComp = getTextComponent();
      textComp.setEnabled(isEnabled);
      textComp.setEditable(isEditable);
      switchMessageArea();
    } catch (Throwable t) {
      t.printStackTrace();
    }

    if (trace != null) trace.exit(MsgTypeArea.class);
  }

  private void switchMessageArea() {
    jTextAreaPanel.removeAll();
    if (jMessage instanceof JTextComponent)
      jTextAreaPanel.add(new JScrollPane(jMessage), BorderLayout.CENTER);
    else
      jTextAreaPanel.add(jMessage, BorderLayout.CENTER);
    jTextActionPanel.setVisible(isHTML);
    revalidate();
    repaint();
    if (undoMngrI != null) {
      undoMngrI.getUndoManager().discardAllEdits();
      undoMngrI.setEnabledUndoAndRedo();
    }
  }

  public void focusMessageArea() {
    getTextComponent().requestFocusInWindow();
  }

  public void setCaretAtTheTop() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "setCaretAtTheTop()");

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

    if (trace != null) trace.exit(MsgTypeArea.class);
  }

  public void clearMessageArea() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeArea.class, "clearMessageArea()");
    setContent("");
    if (trace != null) trace.exit(MsgTypeArea.class);
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

  public void setDocumentListener(DocumentListener documentListener) {
    registeredDocumentListener = documentListener;

  }

  public void setEnterKeyListener(KeyListener enterKeyListener) {
    registeredEnterKeyListener = enterKeyListener;
  }

  public void disposeObj() {
    if (tigerBkgChecker != null) ((SpellCheckerI) tigerBkgChecker).stop();
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
        if (isHTML) {
          // check if we are forced to paste plain text, if so use the special call to create new lines
          DataFlavor[] flavors = null;
          try {
            flavors = Toolkit.getDefaultToolkit().getSystemClipboard().getAvailableDataFlavors();
          } catch (NoSuchMethodError x) {
            // jre 1.4 -- ignore special handling, editor will use default PASTE
          }
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
              jHtmlMessage.pastePlainTextFromClipboard(true);
                e.consume();
              } else if (isFormattedAvailable) {
              jHtmlMessage.pasteFormattedTextFromClipboard();
                // terminate any link at the end of pasted content
              jHtmlMessage.insertContent("&nbsp;");
                e.consume();
            }
          }
        }
      }
    }
  }

}
/*
 * MutableHTMLDocument.java
 *
 * Created on July 24, 2002, 4:00 PM
 */

package comx.Jaguar.gui;

import java.util.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

/**
 *
 * @author  marcin
 * @version 
 */
public class MutableHTMLDocument extends HTMLDocument {

  public MutableHTMLDocument() {
  }

  public MutableHTMLDocument(StyleSheet styles) {
    super(styles);
  }

  public Element getElementByTag(HTML.Tag tag) {
    Element root = getDefaultRootElement();
    return getElementByTag(root, tag);
  }

  public Element getElementByTag(Element parent, HTML.Tag tag) {
    if (parent == null || tag == null)
      return null;
    for (int k=0; k<parent.getElementCount(); k++) {
      Element child = parent.getElement(k);
      if (child.getAttributes().getAttribute(StyleConstants.NameAttribute).equals(tag))
        return child;
      Element e = getElementByTag(child, tag);
      if (e != null)
        return e;
    }
    return null;
  }

  public String getTitle() {
    return (String)getProperty(Document.TitleProperty);
  }

  // This will work only if <title> element was
  // previously created. Looks like a bug in HTML package.
  public void setTitle(String title) {
    Dictionary di = getDocumentProperties();
    di.put(Document.TitleProperty, title);
    setDocumentProperties(di);
  }

  public void addAttributes(Element e, AttributeSet attributes) {
    if (e == null || attributes == null)
      return;
    try {
      writeLock();
      MutableAttributeSet mattr = (MutableAttributeSet)e.getAttributes();
      mattr.addAttributes(attributes);
      fireChangedUpdate(new DefaultDocumentEvent(0, getLength(), DocumentEvent.EventType.CHANGE));
    }
    finally {
      writeUnlock();
    }
  }




  private boolean undoableSequence = false;
  private javax.swing.undo.CompoundEdit compoundEdit  = null;
  protected boolean isUndoableSequence() {
    return undoableSequence;
  }
  protected void startUndoableSequence() {
    endUndoableSequence();
    undoableSequence = true;
  }
  protected void endUndoableSequence() {
    if (compoundEdit != null && compoundEdit.isInProgress()) {
      compoundEdit.end();
    }
    compoundEdit = null;
    undoableSequence = false;
    super.fireUndoableEditUpdate(null);
  }
  protected javax.swing.undo.CompoundEdit getUndoableSequence() {
    if (compoundEdit == null) {
      compoundEdit = new javax.swing.undo.CompoundEdit();
      super.fireUndoableEditUpdate(new UndoableEditEvent(this, compoundEdit));
    }
    return compoundEdit;
  }
  protected void fireUndoableEditUpdate(UndoableEditEvent _event) {
    if (isUndoableSequence()) {
      getUndoableSequence().addEdit(_event.getEdit());
    } else {
      super.fireUndoableEditUpdate(_event);		}
  }
  /*
  public void setInnerHTML(Element _element, String _htmlText) throws BadLocationException, java.io.IOException {
    startUndoableSequence();
    super.setInnerHTML(_element, _htmlText);
    endUndoableSequence();
  }
  public void setOuterHTML(Element _element, String _htmlText) throws BadLocationException, java.io.IOException {
    startUndoableSequence();
    super.setOuterHTML(_element, _htmlText);
    endUndoableSequence();
  }
   */

}
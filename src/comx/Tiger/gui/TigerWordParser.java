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

package comx.Tiger.gui;

import comx.Tiger.ssce.StringWordParser;
import java.util.NoSuchElementException;
import javax.swing.text.*;

/**
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 * Parse words from a Swing JTextComponent. In addition to parsing,
 * this class can also delete words, replace words, and detect doubled words.
 * TextCompWordParser maintains a cursor which points to the start of the
 * current word. The current word is always selected in the JTextComponent to
 * show context.
 *
 * Class Details:
 *
 * <P>The text in the JTextComponent is read when the TextCompWordParser
 * is constructed. If the contents of the JTextComponent change, the updateText
 * method must be called.
 *
 * <P>If the JTextComponent can contain text with HTML markups, extend this class
 * from HTMLStringWordParser instead of StringWordParser.
 *
 * <b>$Revision: 1.3 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class TigerWordParser extends StringWordParser {
  /**
   * Construct a TextCompWordParser to parse words from the contents of a
   * JTextComponent.
   * @param component The JTextComponent to parse.
   * @param isHyphenDelim true if a hyphen is to be considered a word
   *	delimiter, false if it's part of a word
   * @param selectCurWord true if each word should be selected in the
   *	component to show context. Enabling this option degrades performance.
   */
  public TigerWordParser(JTextComponent component, boolean isHyphenDelim, boolean selectCurWord) {
    super(isHyphenDelim);

    // Get the text. Get the text from the document rather than the
    // component, since this has more consistent presentation of
    // newline characters which keeps word offsets correct.
    try {
      setText(component.getDocument().getText(0,
      component.getDocument().getLength()));
    }
    catch (BadLocationException e) {
      setText(component.getText());
    }

    this.selectCurWord = selectCurWord;
    this.component = component;
  }

  /**
   * Delete a specified number of characters from the text starting at
   * the current cursor position.
   * @param numChars The number of characters to delete.
   * @exception NoSuchElementException Attempt to delete beyond the end of
   * the string.
   */
  public void deleteText(int numChars) throws NoSuchElementException {
    try {
      component.getDocument().remove(cursor, numChars);
    }
    catch (BadLocationException ex) {
      //System.err.println("Can't happen: " + ex);
    }

    super.deleteText(numChars);
  }

  /**
   * Highlight the current word in the text area.
   */
  public void highlightWord() {
    if (hasMoreElements()) {
      String word = getWord();
      component.requestFocusInWindow();

      // selectionStart and selectionEnd are constrained so
      // selectionStart <= selectionEnd, so selectionStart
      // must be set 2x to ensure it gets set to what we want.
      component.setSelectionStart(cursor);
      component.setSelectionEnd(cursor + word.length());
      component.setSelectionStart(cursor);
    }
  }

  /**
   * Insert text at a specified position.
   * @param pos The position at which new text is to be inserted.
   * @param newText The text to insert.
   */
  public void insertText(int pos, String newText) {
    super.insertText(pos, newText);
    try {
      component.getDocument().insertString(pos, newText, null);
    }
    catch (BadLocationException ex) {
      //System.err.println("Can't happen: " + ex);
    }
  }

  /**
   * Replace the word at the current position with a new word.
   * @param newWord The word to replace the word at the current position.
   * @exception NoSuchElementException The cursor is positioned at the end
   *	of the string.
   */
  public void replaceWord(String newWord) throws NoSuchElementException {
    // Update the text pane first.
    String oldWord = getWord();

    // Replacing the word may remove its formatting. To get around this,
    // sample the original style attributes and restore them
    // when the replacement word is inserted.
    AttributeSet attrs = null;
    if (component.getDocument() instanceof StyledDocument) {
      Element el =
      ((StyledDocument)component.getDocument()).getCharacterElement(cursor);
      attrs = el.getAttributes();
    }

    try {
      component.getDocument().remove(cursor, oldWord.length());
      component.getDocument().insertString(cursor, newWord, attrs);
    }
    catch (BadLocationException ex) {
      // Do nothing
    }

    super.replaceWord(newWord);
  }

  /**
   * Report that the contents of the TextArea have changed. This method
   * must be called if the TextArea is edited by the user or updated
   * by other software. Currently, it causes checking of the TextArea
   * to restart. In future, it will call the 1.1 TextArea's getCaret
   * method and check from that point.
   */
  public void updateText() {
    // Get the text. Get the text from the document rather than the
    // JTextPane, since this has more consistent presentation of
    // newline characters which keeps word offsets correct.
    theString.setLength(0);
    try {
      theString.append(component.getDocument().getText(0,
      component.getDocument().getLength()));
    }
    catch (BadLocationException e) {
      theString.append(component.getText());
    }

    cursor = 0;
    subWordLength = 0;
  }

  /**
   * The JTextComponent being parsed
   */
  protected JTextComponent component;

  /**
   * true if the current word should always be selected in the TextArea
   * to show context.
   */
  private boolean selectCurWord;

}
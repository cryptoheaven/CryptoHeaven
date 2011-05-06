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

package comx.Tiger.gui;

import comx.Tiger.ssce.*;
import comx.Tiger.util.*;

import com.CH_co.trace.Trace;
import com.CH_gui.action.AbstractActionTraced;
import com.CH_guiLib.gui.JMyPopupMenu;

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.TextUI;
import javax.swing.text.*;

/**
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 * Component which can be added as listener to JTextComponents to perform spell
 * checking in a background thread.  Underlines with zig-zag mispelled words.
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class TigerBkgChecker implements DocumentListener, CaretListener { //, MouseListener {

  public static String PROPERTY__BACKGROUND_CHECK_ENABLED = "BACKGROUND_CHECKER";
  public static boolean backgroundCheckEnabled = true;


  private class ReplaceWordAction extends AbstractActionTraced {

    private int len;
    private int offset;

    public void actionPerformedTraced(ActionEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ReplaceWordAction.class, "actionPerformed(ActionEvent event)");
      removeMisspelledWord(offset);
      javax.swing.text.AttributeSet attrs = null;
      if (component.getDocument() instanceof StyledDocument) {
        Element el = ((StyledDocument)component.getDocument()).getCharacterElement(offset);
        attrs = el.getAttributes();
      }
      try {
        component.getDocument().remove(offset, len);
        component.getDocument().insertString(offset, event.getActionCommand(), attrs);
      } catch (BadLocationException ex) {
        if (trace != null) trace.exception(ReplaceWordAction.class, 100, ex);
      }
      if (trace != null) trace.exit(ReplaceWordAction.class);
    }

    public ReplaceWordAction(String word, int offset, int len) {
      super(word);
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ReplaceWordAction.class, "ReplaceWordAction(String word, int offset, int len)");
      this.offset = offset;
      this.len = len;
      if (trace != null) trace.exit(ReplaceWordAction.class);
    }
  }

  private class IgnoreAllAction extends AbstractActionTraced {

    private String word;

    public void actionPerformedTraced(ActionEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(IgnoreAllAction.class, "actionPerformed(ActionEvent event)");
      try {
        session.getTempLexicon().addWord(word);
      } catch (Exception ex) {
        if (trace != null) trace.data(90, "Can't add", word, event);
        if (trace != null) trace.exception(IgnoreAllAction.class, 100, ex);
      }
      recheckAll(word);
      if (trace != null) trace.exit(IgnoreAllAction.class);
    }

    public IgnoreAllAction(String label, String word) {
      super(label);
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(IgnoreAllAction.class, "IgnoreAllAction(String label, String word)");
      this.word = word;
      if (trace != null) trace.exit(IgnoreAllAction.class);
    }
  }

  private class AddWordAction extends AbstractActionTraced {

    private String word;

    public void actionPerformedTraced(ActionEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AddWordAction.class, "actionPerformed(ActionEvent event)");
      if (userDictionary != null) {
        try {
          EditableLexicon _tmp = userDictionary;
          userDictionary.addWord(word, 105, "");
        } catch (Exception ex) {
          if (trace != null) trace.data(90, "Can't add", word, ex);
          if (trace != null) trace.exception(AddWordAction.class, 100, ex);
        }
        recheckAll(word);
      }
      if (trace != null) trace.exit(AddWordAction.class);
    }

    public AddWordAction(String label, String word) {
      super(label);
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AddWordAction.class, "AddWordAction(String label, String word)");
      this.word = word;
      if (trace != null) trace.exit(AddWordAction.class);
    }
  }

  private static class ZigZagHighlightPainter implements javax.swing.text.Highlighter.HighlightPainter {

    private static final int zigZagHeight = 3;
    protected Color color;

    public Color getColor() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ZigZagHighlightPainter.class, "getColor()");
      if (trace != null) trace.exit(ZigZagHighlightPainter.class, color);
      return color;
    }

    public void paint(Graphics g, int start, int end, Shape bounds, JTextComponent component) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ZigZagHighlightPainter.class, "paint(Graphics g, int start, int end, Shape bounds, JTextComponent component)");
      try {
        if (component != null && component.isShowing() && TigerBkgChecker.backgroundCheckEnabled) {
          TextUI ui = component.getUI();
          try {
            Rectangle rStart = ui.modelToView(component, start);
            Rectangle rEnd = ui.modelToView(component, end);
            Rectangle rBounds = bounds.getBounds();
            int xStart = rStart.x;
            int xEnd = rEnd.x;
            int yStart = rStart.y;
            int yEnd = rEnd.y;
            int yStartBase = (yStart + rStart.height) - 3;
            int yEndBase = (yEnd + rEnd.height) - 3;
            g.setColor(color);
            if (yStart == yEnd) {
              zigzag(g, xStart, xEnd, yStartBase);
            } else {
              if (trace != null) trace.data(10, "first");
              zigzag(g, xStart, (rBounds.x + rBounds.width) - xStart, yStartBase);
              if (rStart.height > 0) {
                for (int line = yStartBase + 1 + rStart.height; line < yEnd; line += rStart.height) {
                  if (trace != null) trace.data(20, "inside for");
                  zigzag(g, rBounds.x, rBounds.x + rBounds.width, line - 1);
                }
              }
              if (trace != null) trace.data(30, "after for");
              zigzag(g, rBounds.x, xEnd, yEndBase);
            }
          } catch (BadLocationException e) {
            if (trace != null) trace.exception(ZigZagHighlightPainter.class, 100, e);
          } catch (Throwable t) {
            if (trace != null) trace.exception(ZigZagHighlightPainter.class, 200, t);
          }
        }
      } catch (Throwable t) {
        t.printStackTrace();
      }
      if (trace != null) trace.exit(ZigZagHighlightPainter.class);
    }

    public void setColor(Color c) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ZigZagHighlightPainter.class, "setColor(Color c)");
      color = c;
      if (trace != null) trace.exit(ZigZagHighlightPainter.class);
    }

    protected void zigzag(Graphics g, int xStart, int xEnd, int y) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ZigZagHighlightPainter.class, "zigzag(Graphics g, int xStart, int xEnd, int y)");
      if (trace != null) trace.args(xStart);
      if (trace != null) trace.args(xEnd);
      if (trace != null) trace.args(y);
      if (y > 5) {
        for (int x = xStart; x < xEnd; x += 2) {
          {
            int xFull = (x + 3) - 1;
            int xReal = Math.min(xFull, xEnd);
            int xDiff = xFull - xReal;
            if (xReal > x || x == xStart)
              g.drawLine(x, y, xReal, (y + 3) - 1 - xDiff);
          }
          x += 2;
          {
            int xFull = (x + 3) - 1;
            int xReal = Math.min(xFull, xEnd);
            int xDiff = xFull - xReal;
            if (xReal > x)
              g.drawLine(x, (y + 3) - 1, xReal, y + xDiff);
          }
        }
      }
      if (trace != null) trace.exit(ZigZagHighlightPainter.class);
    }

    public ZigZagHighlightPainter(Color c) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ZigZagHighlightPainter.class, "ZigZagHighlightPainter(Color c)");
      color = c;
      if (trace != null) trace.exit(ZigZagHighlightPainter.class);
    }

    public ZigZagHighlightPainter() {
      this(Color.red);
    }
  }

  private class MisspelledWord {

    protected int len;
    protected Position pos;
    protected Object tag;

    public int getLen() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MisspelledWord.class, "getLen()");
      if (trace != null) trace.exit(MisspelledWord.class, len);
      return len;
    }

    public int getOffset() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MisspelledWord.class, "getOffset()");
      int offset = pos.getOffset();
      if (trace != null) trace.exit(MisspelledWord.class, offset);
      return offset;
    }

    public String getWord() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MisspelledWord.class, "getWord()");
      String w = "";
      try {
        w = component.getText(pos.getOffset(), len);
      } catch (BadLocationException e) {
        if (trace != null) trace.data(90, "Can't get word", e);
        if (trace != null) trace.exception(MisspelledWord.class, 100, e);
      }
      if (trace != null) trace.exit(MisspelledWord.class, w);
      return w;
    }

    public void hide() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MisspelledWord.class, "hide()");
      component.getHighlighter().removeHighlight(tag);
      len = 0;
      if (trace != null) trace.exit(MisspelledWord.class);
    }

    public void setLen(int len) throws BadLocationException {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MisspelledWord.class, "setLen(int len)");
      this.len = len;
      int o = getOffset();
      component.getHighlighter().changeHighlight(tag, o, o + len);
      if (trace != null) trace.exit(MisspelledWord.class);
    }

    public void setOffset(int offset) throws BadLocationException {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MisspelledWord.class, "setOffset(int offset)");
      pos = component.getDocument().createPosition(offset);
      if (trace != null) trace.exit(MisspelledWord.class);
    }

    public MisspelledWord(String word, int offset) throws BadLocationException {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MisspelledWord.class, "MisspelledWord(String word, int offset)");
      pos = component.getDocument().createPosition(offset);
      tag = component.getHighlighter().addHighlight(offset, offset + word.length(), highlightPainter);
      len = word.length();
      if (trace != null) trace.exit(MisspelledWord.class);
    }
  }


  protected boolean busy;
  private final Object busyMonitor = new Object();
  protected JTextComponent component;
  protected int caretPos;
  protected Vector misspelledWords;
  public boolean debug;
  protected javax.swing.text.Highlighter.HighlightPainter highlightPainter;
  protected PropSpellingSession session;
  protected EditableLexicon userDictionary;

  public TigerBkgChecker(PropSpellingSession session, javax.swing.text.Highlighter.HighlightPainter highlightPainter) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "()");
    misspelledWords = new Vector();
    debug = false;
    //this.component = component;
    this.session = session;
    if (highlightPainter != null)
      this.highlightPainter = highlightPainter;
    else
      this.highlightPainter = new ZigZagHighlightPainter(Color.red);
    FileTextLexicon userLex[] = session.getUserLexicons();
    if (userLex != null)
      userDictionary = userLex[0];
    else
      userDictionary = null;
    if (trace != null) trace.exit(TigerBkgChecker.class);
  }

  public TigerBkgChecker(PropSpellingSession session) {
    this(session, null);
  }

  public void addMisspelledWord(String word, int offset) throws BadLocationException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "()");
    MisspelledWord mw;
    boolean shouldReturn = false;
    for (Enumeration e = misspelledWords.elements(); e.hasMoreElements();) {
      mw = (MisspelledWord)e.nextElement();
      int o = mw.getOffset();
      int len = mw.getLen();
      if (offset >= mw.getOffset() && offset < mw.getOffset() + mw.getLen()) {
        mw.setLen(word.length());
        if (debug) dumpMisspelledWords("add.1");
        shouldReturn = true;
        break;
      }
    }
    if (!shouldReturn) {
      mw = new MisspelledWord(word, offset);
      misspelledWords.addElement(mw);
      if (debug) dumpMisspelledWords("add.2");
    }
    if (trace != null) trace.exit(TigerBkgChecker.class);
  }

  public void caretUpdate(final CaretEvent ev) {
    if (TigerBkgChecker.backgroundCheckEnabled) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          caretUpdate_Threaded(ev);
        }
      });
    }
  }
  private void caretUpdate_Threaded(CaretEvent ev) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "caretUpdate(CaretEvent ev)");
    if (!busy) {
      busy = true;
      synchronized (busyMonitor) {
        try {
          int cp = ev.getDot();
          if (cp != caretPos) {
            boolean recheckWord = false;
            StringBuffer word = new StringBuffer();
            int wordStartPos = getWordAt(caretPos, word);
            if (wordStartPos >= 0 && (cp < wordStartPos || cp > wordStartPos + word.length()))
              recheckWord = true;
            if (cp == caretPos + 1) {
              String t = null;
              try {
                t = component.getDocument().getText(caretPos - 1, 2);
              } catch (BadLocationException e) {
                //if (trace != null) trace.exception(TigerBkgChecker.class, 100, e);
              }
              if (t != null && isWordChar(t.charAt(0)) && !isWordChar(t.charAt(1))) {
                wordStartPos = getWordAt(caretPos - 1, word);
                recheckWord = true;
              }
            }
            if (recheckWord)
              checkWord(word.toString(), wordStartPos, null);
            caretPos = cp;
          }
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
      busy = false;
    }
    if (trace != null) trace.exit(TigerBkgChecker.class);
  }

  public void changedUpdate(DocumentEvent documentevent) {
  }

  //public boolean checkWord(String word, int offset) {
  public boolean checkWord(WordParser parser) {
    int offset = parser.getCursor();
    String word = parser.getWord();
    return checkWord(word, offset, parser);
  }
  public boolean checkWord(String word, int offset, WordParser parser) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "checkWord(String word, int offset, WordParser parser)");
    label0: {
      int result;
      label1: {
        StringBuffer otherWord = new StringBuffer();
        if (parser != null)
          result = session.check(parser, otherWord);
        else
          result = session.check(word, otherWord);
        if ((result & PropSpellingSession.AUTO_CHANGE_WORD_RSLT) != 0) {
          result &= ~PropSpellingSession.AUTO_CHANGE_WORD_RSLT;
          result |= PropSpellingSession.CONDITIONALLY_CHANGE_WORD_RSLT;
        }
        if ((result & PropSpellingSession.MISSPELLED_WORD_RSLT) == 0) {
          // no double word rules because only single words can be checked when typing (it would work inside text fragment inserts)
          if ((result & PropSpellingSession.CONDITIONALLY_CHANGE_WORD_RSLT) == 0)// && (result & session.DOUBLED_WORD_RSLT) == 0)
            break label1;
        }
        try {
          addMisspelledWord(word, offset);
        } catch (BadLocationException e) {
          if (trace != null) trace.data(90, "Can't happen:", e);
          if (trace != null) trace.exception(TigerBkgChecker.class, 100, e);
        }
        break label0;
      }
      if (result == 0 && isInMisspelledWord(offset, offset + word.length()))
        removeMisspelledWord(offset);
    }
    if (trace != null) trace.exit(TigerBkgChecker.class, false);
    return false;
  }

  public JPopupMenu createPopupMenu(int x, int y, int maxSuggestions, String ignoreAllLabel, String addLabel, String noSuggestionsLabel) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "createPopupMenu(int x, int y, int maxSuggestions, String ignoreAllLabel, String addLabel, String noSuggestionsLabel)");
    JPopupMenu menu = null;
    if (isInWord(x, y)) {
      menu = new JMyPopupMenu();
      fillPopupMenu(menu, x, y, maxSuggestions, ignoreAllLabel, addLabel, noSuggestionsLabel);
    }
    if (trace != null) trace.exit(TigerBkgChecker.class, menu);
    return menu;
  }

  public boolean fillPopupMenu(JPopupMenu menu, int x, int y, int maxSuggestions, String ignoreAllLabel, String addLabel, String noSuggestionsLabel) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "fillPopupMenu(JPopupMenu menu, int x, int y, int maxSuggestions, String ignoreAllLabel, String addLabel, String noSuggestionsLabel)");
    boolean filled = false;
    TextUI ui = component.getUI();
    Point pt = new Point(x, y);
    int offset = ui.viewToModel(component, pt);
    StringBuffer word = new StringBuffer();
    int wordStartPos = getWordAt(offset, word);
    if (wordStartPos < 0) {
      filled = false;
    } else {
      SuggestionSet suggestions = new SuggestionSet(maxSuggestions);
      session.suggest(word.toString(), session.getMinSuggestDepth(), session.getComparator(), suggestions);
      if (suggestions.size() > 0) {
        for (int i = 0; i < suggestions.size(); i++) {
          JMenuItem item = menu.add(new ReplaceWordAction(suggestions.wordAt(i), wordStartPos, word.length()));
          item.setFont(item.getFont().deriveFont(Font.BOLD));
        }
        menu.addSeparator();
      } else if (noSuggestionsLabel != null) {
        JMenuItem item = new JMenuItem(noSuggestionsLabel);
        item.setEnabled(false);
        menu.add(item);
        menu.addSeparator();
      }
      if (ignoreAllLabel != null)
        menu.add(new IgnoreAllAction(ignoreAllLabel, word.toString()));
      if (addLabel != null && userDictionary != null)
        menu.add(new AddWordAction(addLabel, word.toString()));
      filled = true;
    }
    if (trace != null) trace.exit(TigerBkgChecker.class, filled);
    return filled;
  }

  public Color getZigZagColor() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "getZigZagColor()");
    Color color = null;
    if (highlightPainter instanceof ZigZagHighlightPainter)
      color = ((ZigZagHighlightPainter)highlightPainter).getColor();
    else
      color = Color.black;
    if (trace != null) trace.exit(TigerBkgChecker.class, color);
    return color;
  }

  public void insertUpdate(final DocumentEvent ev) {
    if (TigerBkgChecker.backgroundCheckEnabled) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          insertUpdate_Threaded(ev);
        }
      });
    }
  }
  private void insertUpdate_Threaded(DocumentEvent ev) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "insertUpdate(DocumentEvent ev)");
    if (!busy) {
      busy = true;
      synchronized (busyMonitor) {
        try {
          int nInserted = ev.getLength();
          int cp = ev.getOffset();
          if (nInserted == 1) {
            int prevCp = cp - nInserted;
            StringBuffer word = new StringBuffer();
            int wordStartPos = -1;
            char cPrev = '\0';
            char cTyped = '\0';
            try {
              cPrev = component.getText(prevCp, 1).charAt(0);
              cTyped = component.getText(cp, 1).charAt(0);
            } catch (BadLocationException e) {
              //if (trace != null) trace.exception(TigerBkgChecker.class, 100, e);
            }
            boolean cPrevIsWord = isWordChar(cPrev);
            boolean cTypedIsWord = isWordChar(cTyped);
            boolean recheckWord = false;
            int wordPos = cp;
            MisspelledWord mw;
            if (cTypedIsWord) {
              if (isInMisspelledWord(prevCp, cp))
                recheckWord = true;
              else
                if (!isInMisspelledWord(cp) && (mw = findMisspelledWord(cp + 1)) != null) {
                  recheckWord = true;
                  try {
                    mw.setOffset(cp);
                  } catch (BadLocationException e) {
                    if (trace != null) trace.data(190, "Can't happen:", e);
                    if (trace != null) trace.exception(TigerBkgChecker.class, 200, e);
                  }
                }
            } else {
              if (cPrevIsWord) {
                wordPos = prevCp;
                recheckWord = true;
                char cNext = '\0';
                try {
                  cNext = component.getText(cp + 1, 1).charAt(0);
                } catch (BadLocationException e) {
                  if (trace != null) trace.exception(TigerBkgChecker.class, 300, e);
                }
                if (isWordChar(cNext)) {
                  wordStartPos = getWordAt(cp + 1, word);
                  checkWord(word.toString(), wordStartPos, null);
                }
              }
            }
            if (recheckWord) {
              wordStartPos = getWordAt(wordPos, word);
              checkWord(word.toString(), wordStartPos, null);
            }
          } else {
            onInsertText(cp, nInserted);
          }
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
      busy = false;
    }
    if (trace != null) trace.exit(TigerBkgChecker.class);
  }

  public boolean isInMisspelledWord(Point pt) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "isInMisspelledWord(Point pt)");
    boolean isInMisspelled = false;
    if (TigerBkgChecker.backgroundCheckEnabled) {
      TextUI ui = component.getUI();
      int offset = ui.viewToModel(component, pt);
      isInMisspelled = isInMisspelledWord(offset);
    }
    if (trace != null) trace.exit(TigerBkgChecker.class, isInMisspelled);
    return isInMisspelled;
  }

  public void recheckAll() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "recheckAll()");
    try {
      String text = null;
      try {
        text = component.getDocument().getText(0, component.getDocument().getLength());
      } catch (BadLocationException e) {
        if (trace != null) trace.data(90, "Can't happen:", e);
        if (trace != null) trace.exception(TigerBkgChecker.class, 100, e);
      }
      if (text != null) {
        for (StringWordParser parser = new StringWordParser(text, !session.getOption(4096)); parser.hasMoreElements(); parser.nextWord()) {
          //checkWord(parser.getWord(), parser.getCursor(), parser);
          checkWord(parser.getWord(), parser.getCursor(), null);
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    if (trace != null) trace.exit(TigerBkgChecker.class);
  }

  public void removeMisspelledWord(int offset) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "removeMisspelledWord(int offset)");
    boolean found = false;
    for (Enumeration e = misspelledWords.elements(); e.hasMoreElements();) {
      MisspelledWord mw = (MisspelledWord)e.nextElement();
      int o = mw.getOffset();
      int len = mw.getLen();
      if (offset >= o && offset < o + len) {
        mw.hide();
        misspelledWords.removeElement(mw);
        found = true;
        break;
      }
    }
    if (debug && !found) {
      if (trace != null) trace.data(90, "removeMisspelledWords: word at " + offset + " not found");
    }
    if (debug) dumpMisspelledWords("remove");
    if (trace != null) trace.exit(TigerBkgChecker.class);
  }

  private void removeMisspelledWords() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "removeMisspelledWords()");
    for (Enumeration e = misspelledWords.elements(); e.hasMoreElements();)
      ((MisspelledWord) e.nextElement()).hide();
    misspelledWords.clear();
    if (trace != null) trace.exit(TigerBkgChecker.class);
  }

  public void removeUpdate(final DocumentEvent ev) {
    if (TigerBkgChecker.backgroundCheckEnabled) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          removeUpdate_Threaded(ev);
        }
      });
    }
  }
  private void removeUpdate_Threaded(DocumentEvent ev) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "removeUpdate(DocumentEvent ev)");
    if (!busy) {
      busy = true;
      synchronized (busyMonitor) {
        try {
          int nRemoved = ev.getLength();
          int cp = ev.getOffset();
          boolean recheckWord = false;
          int wordPos = cp;
          if (isInMisspelledWord(cp))
            recheckWord = true;
          if (cp > 0 && isInMisspelledWord(cp - 1)) {
            wordPos = cp - 1;
            recheckWord = true;
          }
          if (recheckWord) {
            StringBuffer word = new StringBuffer();
            int wordStartPos = getWordAt(wordPos, word);
            checkWord(word.toString(), wordStartPos, null);
          }
        } catch (Throwable t) {
        }
      }
      busy = false;
    }
    if (trace != null) trace.exit(TigerBkgChecker.class);
  }

  public synchronized void restart(JTextComponent textComp) {
    stop();
    if (session.getLexicons() != null && session.getLexicons().length > 1) {
      component = textComp;
      caretPos = component.getCaret().getDot();
      resume();
      recheckAll();
    }
  }

  public synchronized void resume() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "resume()");
    component.getDocument().addDocumentListener(this);
    component.addCaretListener(this);
    //component.addMouseListener(this);
    if (trace != null) trace.exit(TigerBkgChecker.class);
  }

  public void setUserDictionary(EditableLexicon lex) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "setUserDictionary(EditableLexicon lex)");
    userDictionary = lex;
    if (trace != null) trace.exit(TigerBkgChecker.class);
  }

  public void setZigZagColor(Color c) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "setZigZagColor(Color c)");
    if (highlightPainter instanceof ZigZagHighlightPainter)
      ((ZigZagHighlightPainter)highlightPainter).setColor(c);
    if (trace != null) trace.exit(TigerBkgChecker.class);
  }

  public synchronized void pause() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "pause()");
    if (component != null) {
      component.getDocument().removeDocumentListener(this);
      component.removeCaretListener(this);
      //component.removeMouseListener(this);
    }
    if (trace != null) trace.exit(TigerBkgChecker.class);
  }

  public synchronized void stop() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "stop()");
    pause();
    removeMisspelledWords();
    component = null;
    if (trace != null) trace.exit(TigerBkgChecker.class);
  }

  private MisspelledWord findMisspelledWord(int start, int end) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "findMisspelledWord(int start, int end)");
    MisspelledWord mpWord = null;
    for (Enumeration e = misspelledWords.elements(); e.hasMoreElements();) {
      MisspelledWord word = (MisspelledWord)e.nextElement();
      int o = word.getOffset();
      int len = word.getLen();
      if (start >= o && start < o + len || end >= o && end < o + len || o >= start && o <= end || (o + len) - 1 >= start && (o + len) - 1 <= end) {
        mpWord = word;
        break;
      }
    }
    if (trace != null) trace.exit(TigerBkgChecker.class, mpWord);
    return mpWord;
  }

  private MisspelledWord findMisspelledWord(int offset) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "findMisspelledWord(int offset)");
    MisspelledWord mpWord = findMisspelledWord(offset, offset);
    if (trace != null) trace.exit(TigerBkgChecker.class, mpWord);
    return mpWord;
  }

  protected int getWordAt(int offset, StringBuffer word) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "getWordAt(int offset, StringBuffer word)");
    int rc = 0;
    if (offset < 0 || offset >= component.getDocument().getLength())
      rc = -1;
    else {
      int maxWordLen = 100;
      int chunkStart = Math.max(0, offset - 100);
      int chunkLen = Math.min(component.getDocument().getLength() - chunkStart, 200);
      String chunk = "";
      try {
        chunk = component.getText(chunkStart, chunkLen);
      } catch (BadLocationException e) {
        if (trace != null) trace.data(90, "Can't happen:", e);
        if (trace != null) trace.exception(TigerBkgChecker.class, 100, e);
        rc = -1;
      }
      if (rc == 0) {
        word.setLength(0);
        int relOffset = offset - chunkStart;
        if (!isWordChar(chunk.charAt(relOffset)))
          rc = -1;
        else {
          int start;
          for (start = relOffset; start >= 0 && isWordChar(chunk.charAt(start)); start--) {
          }
          if (start < 0)
            start = 0;
          for (; start < chunk.length() && !UniCharacter.isLetterOrDigit(chunk.charAt(start)); start++) {
          }
          boolean containsEmbeddedPeriods = false;
          for (int i = start; i < chunk.length() && isWordChar(chunk.charAt(i)); i++) {
            char c = chunk.charAt(i);
            word.append(c);
            if (c == '.' && i > 0 && UniCharacter.isLetterOrDigit(chunk.charAt(i - 1)) && i < chunk.length() - 1 && UniCharacter.isLetterOrDigit(chunk.charAt(i + 1)))
              containsEmbeddedPeriods = true;
          }

          boolean isInitialism = false;
          if (containsEmbeddedPeriods) {
            int nWordChars = 0;
            isInitialism = true;
            for (int i = 0; i < word.length(); i++) {
              if (UniCharacter.isLetterOrDigit(word.charAt(i))) {
                if (++nWordChars <= 2)
                  continue;
                isInitialism = false;
                break;
              }
              nWordChars = 0;
            }

          }
          for (boolean wordChanged = true; wordChanged && word.length() > 0;) {
            char lastChar = word.charAt(word.length() - 1);
            wordChanged = false;
            if (UniCharacter.isApostrophe(lastChar) && word.length() > 1 && Character.toLowerCase(word.charAt(word.length() - 2)) != 's') {
              word.setLength(word.length() - 1);
              wordChanged = true;
            }
            if (lastChar == '.' && !isInitialism) {
              word.setLength(word.length() - 1);
              wordChanged = true;
            }
          }

          rc = start + chunkStart;
        }
      }
    }
    if (trace != null) trace.exit(TigerBkgChecker.class, rc);
    return rc;
  }

  private boolean isInMisspelledWord(int start, int end) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "isInMisspelledWord(int start, int end)");
    boolean found = findMisspelledWord(start, end) != null;
    if (trace != null) trace.exit(TigerBkgChecker.class, found);
    return found;
  }

  private boolean isInMisspelledWord(int offset) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "isInMisspelledWord(int offset)");
    boolean isInMisspelled = isInMisspelledWord(offset, offset);
    if (trace != null) trace.exit(TigerBkgChecker.class, isInMisspelled);
    return isInMisspelled;
  }

  protected boolean isInWord(int x, int y) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "isInWord(int x, int y)");
    TextUI ui = component.getUI();
    Point pt = new Point(x, y);
    int offset = ui.viewToModel(component, pt);
    String text = null;
    try {
      text = component.getText(offset, 1);
    } catch (BadLocationException e) {
      if (trace != null) trace.exception(TigerBkgChecker.class, 100, e);
    }
    boolean isInWord = text != null && text.length() != 0 && isWordChar(text.charAt(0));
    if (trace != null) trace.exit(TigerBkgChecker.class, isInWord);
    return isInWord;
  }

  protected boolean isWordChar(char c) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "isWordChar(char c)");
    boolean isWordChar = UniCharacter.isLetterOrDigit(c) || UniCharacter.isApostrophe(c) || c == '.';
    if (trace != null) trace.exit(TigerBkgChecker.class, isWordChar);
    return isWordChar;
  }

  protected void onInsertText(int offset, int nChars) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "onInsertText(int offset, int nChars)");
    StringBuffer firstWord = new StringBuffer();
    StringBuffer lastWord = new StringBuffer();
    int firstWordStartPos = getWordAt(offset, firstWord);
    int lastWordStartPos = getWordAt((offset + nChars) - 1, lastWord);
    int start = offset;
    int end = offset + nChars;
    if (firstWordStartPos >= 0)
      start = firstWordStartPos;
    if (lastWordStartPos >= 0)
      end = lastWordStartPos + lastWord.length();
    if (firstWordStartPos >= 0 && lastWordStartPos >= 0 && firstWordStartPos == lastWordStartPos) {
      checkWord(firstWord.toString(), firstWordStartPos, null);
    } else {
      String text = null;
      try {
        text = component.getText(start, end - start);
      } catch (BadLocationException e) {
        if (trace != null) trace.data(90, "Can't happen:", e);
        if (trace != null) trace.exception(TigerBkgChecker.class, 100, e);
      }
      if (text != null) {
        StringWordParser parser = new StringWordParser(text, !session.getOption(4096));
        StringBuffer otherWord = new StringBuffer();
        do {
          if (trace != null) trace.data(110, "in do");
          if (session.check(parser, otherWord) != 8) {
            checkWord(parser.getWord(), offset + parser.getCursor(), parser);
            parser.nextWord();
          } else {
            break;
          }
        } while (true);
      }
    }
    if (trace != null) trace.exit(TigerBkgChecker.class);
  }

  protected void recheckAll(String word) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "recheckAll(String word)");
    String text = null;
    try {
      text = component.getDocument().getText(0, component.getDocument().getLength());
    } catch (BadLocationException e) {
      if (trace != null) trace.data(90, "Can't happen:", e);
      if (trace != null) trace.exception(TigerBkgChecker.class, 100, e);
    }
    if (text != null) {
      StringBuffer tmpWord = new StringBuffer();
      for (int i = text.indexOf(word); i >= 0; i = text.indexOf(word, i + word.length())) {
        int wordStartPos = getWordAt(i, tmpWord);
        checkWord(tmpWord.toString(), wordStartPos, null);
      }
    }
    if (trace != null) trace.exit(TigerBkgChecker.class);
  }

  private void dumpMisspelledWords(String label) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TigerBkgChecker.class, "dumpMisspelledWords(String label)");
    System.out.print(label + "(" + misspelledWords.size() + "): ");
    MisspelledWord mw;
    for (Enumeration e = misspelledWords.elements(); e.hasMoreElements(); System.out.print("[" + mw.getOffset() + "," + mw.getLen() + "] ")) {
      mw = (MisspelledWord)e.nextElement();
    }
    System.out.println();
    if (trace != null) trace.exit(TigerBkgChecker.class);
  }

//
//  /*************************************************
//   * M o u s e L i s t e n e r   Interface methods *
//   *************************************************/
//
//  /**
//   * Respond to a MouseClicked event. We listen for this event to determine
//   * if the popup menu should be displayed over a misspelled word.
//   * @param e Information about the event.
//   */
//  public void mouseClicked(MouseEvent e) {
//    checkPopup(e);
//  }
//
//  /**
//   * Respond to a MouseEntered event. We listen for this event to determine
//   * if the popup menu should be displayed over a misspelled word.
//   * @param e Information about the event.
//   */
//  public void mouseEntered(MouseEvent e) {
//    //checkPopup(e);
//  }
//
//  /**
//   * Respond to a MouseExited event. We listen for this event to determine
//   * if the popup menu should be displayed over a misspelled word.
//   * @param e Information about the event.
//   */
//  public void mouseExited(MouseEvent e) {
//    //checkPopup(e);
//  }
//
//  /**
//   * Respond to a MousePressed event. We listen for this event to determine
//   * if the popup menu should be displayed over a misspelled word.
//   * @param e Information about the event.
//   */
//  public void mousePressed(MouseEvent e) {
//    checkPopup(e);
//  }
//
//  /**
//   * Respond to a MouseReleased event. We listen for this event to determine
//   * if the popup menu should be displayed over a misspelled word.
//   * @param e Information about the event.
//   */
//  public void mouseReleased(MouseEvent e) {
//    checkPopup(e);
//  }
//
//  /**
//   * Display a spelling-related popup menu if the popup event (platform
//   * dependent) is triggered over a misspelled word.
//   * @param e Information about the event.
//   */
//  private void checkPopup(MouseEvent e) {
//    if (e.isPopupTrigger() && !e.isConsumed()) {
//      // Determine which component the event was triggered for, and
//      // from that determine which TigerBkgChecker to use. Because
//      // we have only a couple of components, we can use a brute-force
//      // approach. In a more complex application, some means of
//      // associating components and TigerBkgCheckers would be a good
//      // idea.
//      Component c = e.getComponent();
//      TigerBkgChecker bgc = TigerBkgChecker.this;
//      if (bgc == null) {
//        // We're not interested in the mouse event because it didn't
//        // occur over one of the text components.
//        return;
//      }
//
//      // Determine if the popup was requested over a misspelled word.
//      Point pt = new Point(e.getX(), e.getY());
//      if (bgc.isInMisspelledWord(pt)) {
//        e.consume();
//        // Display the popup.
//        JPopupMenu popup = bgc.createPopupMenu(e.getX(), e.getY(), 8, "Ignore All", "Add", "(No suggestions)");
//        if (popup == null) {
//          // This shouldn't happen, because we know the popup was
//          // requested over a misspelled word. But just in case...
//          return;
//        }
//        popup.show(c, e.getX(), e.getY());
//      }
//    }
//  }

}
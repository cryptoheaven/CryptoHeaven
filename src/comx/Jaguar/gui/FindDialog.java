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

package comx.Jaguar.gui;

import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.JMyButton;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import com.CH_gui.gui.*;
import com.CH_guiLib.gui.*;
import com.CH_co.gui.*;

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
 * <b>$Revision: 1.3 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FindDialog extends JDialog {
  protected Jaguar m_owner;
  protected JTabbedPane m_tb;
  protected JTextField m_txtFind1;
  protected JTextField m_txtFind2;
  protected Document m_docFind;
  protected Document m_docReplace;
  protected ButtonModel m_modelWord;
  protected ButtonModel m_modelCase;
  protected ButtonModel m_modelUp;
  protected ButtonModel m_modelDown;

  protected int m_searchIndex = -1;
  protected boolean m_searchUp = false;
  protected String m_searchData;

  public FindDialog(Dialog parent, Jaguar owner, int index) {
    super(parent, "Find and Replace", false);
    init(owner, index);
  }
  public FindDialog(Frame parent, Jaguar owner, int index) {
    super(parent, "Find and Replace", false);
    init(owner, index);
  }
  private void init(Jaguar owner, int index) {
    m_owner = owner;

    m_tb = new JTabbedPane();

    // "Find" panel
    JPanel p1 = new JPanel(new BorderLayout());

    JPanel pc1 = new JPanel(new BorderLayout());

    JPanel pf = new JPanel();
    pf.setLayout(new DialogLayout2(20, 5));
    pf.setBorder(new EmptyBorder(8, 5, 8, 0));
    pf.add(new JMyLabel("Find what:"));

    m_txtFind1 = new JMyTextField();
    Utils.initKeyBindings(m_txtFind1);
    m_docFind = m_txtFind1.getDocument();
    pf.add(m_txtFind1);
    pc1.add(pf, BorderLayout.CENTER);

    JPanel po = new JPanel(new GridLayout(2, 2, 8, 2));
    po.setBorder(new TitledBorder(new EtchedBorder(), "Options"));

    JCheckBox chkWord = new JMyCheckBox("Whole words only");
    chkWord.setMnemonic('w');
    m_modelWord = chkWord.getModel();
    po.add(chkWord);

    ButtonGroup bg = new ButtonGroup();
    JRadioButton rdUp = new JMyRadioButton("Search up");
    rdUp.setMnemonic('u');
    m_modelUp = rdUp.getModel();
    bg.add(rdUp);
    po.add(rdUp);

    JCheckBox chkCase = new JMyCheckBox("Match case");
    chkCase.setMnemonic('c');
    m_modelCase = chkCase.getModel();
    po.add(chkCase);

    JRadioButton rdDown = new JMyRadioButton("Search down", true);
    rdDown.setMnemonic('d');
    m_modelDown = rdDown.getModel();
    bg.add(rdDown);
    po.add(rdDown);
    pc1.add(po, BorderLayout.SOUTH);

    p1.add(pc1, BorderLayout.CENTER);

    JPanel p01 = new JPanel(new FlowLayout());
    JPanel p = new JPanel(new GridLayout(2, 1, 2, 8));

    ActionListener findAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        findNext(false, true);
      }
    };
    JButton btFind = new JMyButton("Find Next");
    btFind.addActionListener(findAction);
    btFind.setMnemonic('f');
    p.add(btFind);

    ActionListener closeAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    };
    JButton btClose = new JMyButton("Close");
    btClose.addActionListener(closeAction);
    btClose.setDefaultCapable(true);
    p.add(btClose);
    p01.add(p);
    p1.add(p01, BorderLayout.EAST);

    m_tb.addTab("Find", p1);

    // "Replace" panel
    JPanel p2 = new JPanel(new BorderLayout());

    JPanel pc2 = new JPanel(new BorderLayout());

    JPanel pc = new JPanel();
    pc.setLayout(new DialogLayout2(20, 5));
    pc.setBorder(new EmptyBorder(8, 5, 8, 0));

    pc.add(new JMyLabel("Find what:"));
    m_txtFind2 = new JMyTextField();
    Utils.initKeyBindings(m_txtFind2);
    m_txtFind2.setDocument(m_docFind);
    pc.add(m_txtFind2);

    pc.add(new JMyLabel("Replace:"));
    JTextField txtReplace = new JMyTextField();
    Utils.initKeyBindings(txtReplace);
    m_docReplace = txtReplace.getDocument();
    pc.add(txtReplace);
    pc2.add(pc, BorderLayout.CENTER);

    po = new JPanel(new GridLayout(2, 2, 8, 2));
    po.setBorder(new TitledBorder(new EtchedBorder(), "Options"));

    chkWord = new JMyCheckBox("Whole words only");
    chkWord.setMnemonic('w');
    chkWord.setModel(m_modelWord);
    po.add(chkWord);

    bg = new ButtonGroup();
    rdUp = new JMyRadioButton("Search up");
    rdUp.setMnemonic('u');
    rdUp.setModel(m_modelUp);
    bg.add(rdUp);
    po.add(rdUp);

    chkCase = new JMyCheckBox("Match case");
    chkCase.setMnemonic('c');
    chkCase.setModel(m_modelCase);
    po.add(chkCase);

    rdDown = new JMyRadioButton("Search down", true);
    rdDown.setMnemonic('d');
    rdDown.setModel(m_modelDown);
    bg.add(rdDown);
    po.add(rdDown);
    pc2.add(po, BorderLayout.SOUTH);

    p2.add(pc2, BorderLayout.CENTER);

    JPanel p02 = new JPanel(new FlowLayout());
    p = new JPanel(new GridLayout(3, 1, 2, 8));

    ActionListener replaceAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        findNext(true, true);
      }
    };
    JButton btReplace = new JMyButton("Replace");
    btReplace.addActionListener(replaceAction);
    btReplace.setMnemonic('r');
    p.add(btReplace);

    ActionListener replaceAllAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int counter = 0;
        while (true) {
          int result = findNext(true, false);
          if (result < 0)		// error
            return;
          else if (result == 0)		// no more
            break;
          counter++;
        }
        JOptionPane.showMessageDialog(m_owner, counter+" replacement(s) have been done", "Find and Replace", JOptionPane.INFORMATION_MESSAGE);
      }
    };
    JButton btReplaceAll = new JMyButton("Replace All");
    btReplaceAll.addActionListener(replaceAllAction);
    btReplaceAll.setMnemonic('a');
    p.add(btReplaceAll);

    btClose = new JMyButton("Close");
    btClose.addActionListener(closeAction);
    btClose.setDefaultCapable(true);
    p.add(btClose);
    p02.add(p);
    p2.add(p02, BorderLayout.EAST);

    // Make button columns the same size
    p01.setPreferredSize(p02.getPreferredSize());

    m_tb.addTab("Replace", p2);

    m_tb.setSelectedIndex(index);

    JPanel pp = new JPanel(new BorderLayout());
    pp.setBorder(new EmptyBorder(5,5,5,5));
    pp.add(m_tb, BorderLayout.CENTER);
    getContentPane().add(pp, BorderLayout.CENTER);

    pack();
    setResizable(false);
    setLocationRelativeTo(owner);

    WindowListener flst = new WindowAdapter() {
      public void windowActivated(WindowEvent e) {
        m_searchIndex = -1;
      }

      public void windowDeactivated(WindowEvent e) {
        m_searchData = null;
      }
    };
    addWindowListener(flst);
  }

  public void setSelectedIndex(int index) {
    m_tb.setSelectedIndex(index);
    setVisible(true);
    m_searchIndex = -1;
  }

  /**
   * @return -ve if error, 0 if no more occurances.
   */
  public int findNext(boolean doReplace, boolean showWarnings) {
    JTextPane monitor = m_owner.getTextPane();
    int pos = monitor.getCaretPosition();
    if (m_modelUp.isSelected() != m_searchUp) {
      m_searchUp = m_modelUp.isSelected();
      m_searchIndex = -1;
    }

    if (m_searchIndex == -1) {
      try {
        Document doc = m_owner.getDocument();
        if (m_searchUp) {
          m_searchData = doc.getText(0, pos);
        } else {
          m_searchData = doc.getText(pos, doc.getLength()-pos);
        }
        m_searchIndex = pos;
      }
      catch (BadLocationException ex) {
        warning(ex.toString());
        return -1;
      }
    }

    String key = "";
    try {
      key = m_docFind.getText(0, m_docFind.getLength());
    } catch (BadLocationException ex) {}
    if (key.length()==0) {
      warning("Please enter the target to search");
      return -1;
    }
    if (!m_modelCase.isSelected()) {
      m_searchData = m_searchData.toLowerCase();
      key = key.toLowerCase();
    }
    if (m_modelWord.isSelected()) {
      for (int k=0; k<Utils.WORD_SEPARATORS.length; k++) {
        if (key.indexOf(Utils.WORD_SEPARATORS[k]) >= 0) {
          warning("The text target contains an illegal character \'"+Utils.WORD_SEPARATORS[k]+"\'");
          return -1;
        }
      }
    }

    String replacement = "";
    if (doReplace) {
      try {
        replacement = m_docReplace.getText(0, m_docReplace.getLength());
      } catch (BadLocationException ex) {}
    }

    int xStart = -1;
    int xFinish = -1;
    while (true) {
      if (m_searchUp)
        xStart = m_searchData.lastIndexOf(key, pos-1);
      else
        xStart = m_searchData.indexOf(key, pos-m_searchIndex);
      if (xStart < 0) {
        if (showWarnings)
          warning("Text not found");
        return 0;
      }

      xFinish = xStart + key.length();

      if (m_modelWord.isSelected()) {
        boolean s1 = xStart > 0;
        boolean b1 = s1 && !Utils.isSeparator(m_searchData.charAt(xStart-1));
        boolean s2 = xFinish > 0 && xFinish < m_searchData.length();
        boolean b2 = s2 && !Utils.isSeparator(m_searchData.charAt(xFinish));

        if (b1 || b2)		// Not a whole word
        {
          if (m_searchUp && s1)		// Can continue up
          {
            pos = xStart;
            continue;
          }
          if (!m_searchUp && s2)		// Can continue down
          {
            pos += xFinish+1;
            continue;
          }
          // Found, but not a whole word, and we cannot continue
          if (showWarnings)
            warning("Text not found");
          return 0;
        }
      }
      break;
    }

    if (!m_searchUp) {
      xStart += m_searchIndex;
      xFinish += m_searchIndex;
    }
    if (doReplace) {
      m_owner.setSelection(xStart, xFinish, m_searchUp);
      monitor.replaceSelection(replacement);
      m_owner.setSelection(xStart, xStart+replacement.length(), m_searchUp);
      m_searchIndex = -1;
    } else {
      m_owner.setSelection(xStart, xFinish, m_searchUp);
    }
    return 1;
  }

  protected void warning(String message) {
    JOptionPane.showMessageDialog(m_owner, message, "Find and Replace", JOptionPane.INFORMATION_MESSAGE);
  }
}
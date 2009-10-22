/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_gui.usrs;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.CH_cl.util.*;

import com.CH_co.gui.*;
import com.CH_co.nanoxml.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.gui.*;
import com.CH_gui.msgs.*;

import com.CH_guiLib.gui.*;

/**
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: 1.11 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class AccountOptionsSignaturesPanel extends JPanel {

  private JCheckBox jAddSigToNew;
  private JCheckBox jAddSigToRepFwd;
  private JList jSigList;
  private JButton jNew;
  private JButton jRemove;
  private JButton jRename;
  private JButton jSetAsDefault;
  private JRadioButton jTypePlain;
  private JRadioButton jTypeHTML;
  private JRadioButton jTypeFile;
  private MsgTypeArea sigTypeArea;
  private JTextField jFileField;
  private JButton jBrowse;

  private Vector listHeadersV = new Vector();
  private Vector listDatasV = new Vector();
  private int currentSig = -1;
  private int defaultSig = -1;
  private boolean isRemoveInProgress = false;

  private UserSettingsRecord originalSettingsClone;
  private String originalDataStr;
  private boolean isChangeAttempted;

  private EventListenerList myListenerList = new EventListenerList();


  /** Creates new AccountOptionsSignaturesPanel */
  public AccountOptionsSignaturesPanel() {
    this(null);
  }
  public AccountOptionsSignaturesPanel(UserSettingsRecord initialSettings) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AccountOptionsSignaturesPanel.class, "AccountOptionsSignaturesPanel()");

    createComponents();
    initializeComponents();
    layoutPanel();

    pressedText();
    clearEditableFieldsAndDisable();

    if (initialSettings != null) {
      originalSettingsClone = (UserSettingsRecord) initialSettings.clone();
      initializeData(originalSettingsClone);
    } else {
      originalSettingsClone = new UserSettingsRecord();
    }
    originalDataStr = originalSettingsClone.makeXMLData().toString();

    if (trace != null) trace.exit(AccountOptionsSignaturesPanel.class);
  }


  public void initializeData(UserSettingsRecord initialSettings) {
    clearEditableFieldsAndDisable();
    // set-up for removal of previous data
    isRemoveInProgress = true;
    // clear data vectors
    listHeadersV = new Vector();
    listDatasV = new Vector();
    // initialize new data vectors
    if (initialSettings.sigDefault != null)
      defaultSig = initialSettings.sigDefault.intValue();

    jAddSigToNew.setSelected(("" + initialSettings.sigAddToNew).equalsIgnoreCase("true"));
    jAddSigToRepFwd.setSelected(("" + initialSettings.sigAddToReFwd).equalsIgnoreCase("true"));

    for (int i=0; initialSettings.sigListV!=null && i<initialSettings.sigListV.size(); i++) {
      String[] data = (String[]) initialSettings.sigListV.elementAt(i);
      listHeadersV.addElement(data[0]);
      listDatasV.addElement(data);
    }

//    if (xml != null && xml.getNameSafe().equalsIgnoreCase("signatures")) {
//      jAddSigToNew.setSelected(("" + xml.getAttribute("sign_new")).equalsIgnoreCase("true"));
//      jAddSigToRepFwd.setSelected(("" + xml.getAttribute("sign_re_fwd")).equalsIgnoreCase("true"));
//      Vector sigsV = xml.getChildren();
//      if (sigsV != null) {
//        for (int i=0; i<sigsV.size(); i++) {
//          XMLElement sig = (XMLElement) sigsV.elementAt(i);
//          if (sig.getNameSafe().equalsIgnoreCase("sig")) {
//            String isDefaultSig = "" + sig.getAttribute("default");
//            if (isDefaultSig.equalsIgnoreCase("true"))
//              defaultSig = i;
//            String[] data = new String[3];
//            Vector sigElementsV = sig.getChildren();
//            for (int k=0; k<sigElementsV.size(); k++) {
//              XMLElement element = (XMLElement) sigElementsV.elementAt(k);
//              String elementName = element.getNameSafe();
//              String elementValue = element.getContent();
//              if (elementName.equalsIgnoreCase("name"))
//                data[0] = elementValue;
//              else if (elementName.equalsIgnoreCase("type"))
//                data[1] = elementValue;
//              else if (elementName.equalsIgnoreCase("value"))
//                data[2] = elementValue;
//            }
//            //System.out.println("Adding element " + Misc.objToStr(data));
//            listHeadersV.addElement(data[0]);
//            listDatasV.addElement(data);
//          }
//        }
//      }
//    }
    //System.out.println("default sig index=" + defaultSig);
    // set data back to model
    jSigList.setListData(listHeadersV);
    setDefaultSig(defaultSig);
    // make default selection
    if (defaultSig >= 0) {
      jSigList.setSelectedIndex(defaultSig);
    } else if (listDatasV.size() > 0) {
      jSigList.setSelectedIndex(0);
    }
  }


  public boolean isChangeAttempted() {
    return isChangeAttempted;
  }


  public boolean isDataChanged() {
    boolean rc = originalDataStr == null;
    if (!rc) {
      rc = !originalDataStr.equals(getData().makeXMLData().toString());
    }
    return rc;
  }


  public UserSettingsRecord getData() {
    storeEdit(currentSig);
    originalSettingsClone.sigAddToNew = Boolean.valueOf(jAddSigToNew.isSelected());
    originalSettingsClone.sigAddToReFwd = Boolean.valueOf(jAddSigToRepFwd.isSelected());
    originalSettingsClone.sigListV = listDatasV;
    originalSettingsClone.sigDefault = new Integer(defaultSig);
    return originalSettingsClone;
  }

  public String getDefaultSignatureText() {
    String defaultSignature = "";
    storeEdit(currentSig);
    if (currentSig >= 0) {
      String[] data = (String[]) listDatasV.elementAt(currentSig);
      defaultSignature = data[2];
      if (defaultSignature != null && data[1].equals("text/html"))
        defaultSignature = MsgPanelUtils.extractPlainFromHtml(defaultSignature);
      if (defaultSignature == null)
        defaultSignature = "";
    }
    return defaultSignature;
  }

  private void createComponents() {
    jAddSigToNew = new JMyCheckBox("Add signatures to New messages");
    jAddSigToRepFwd = new JMyCheckBox("Add signatures to Replies and Forwards");
    jSigList = new JList();
    jSigList.setVisibleRowCount(1);
    jNew = new JMyButton("New");
    jRemove = new JMyButton("Remove");
    jRename = new JMyButton("Rename");
    jSetAsDefault = new JMyButton("Set as Default");
    jBrowse = new JMyButton("Browse ...");

    jTypePlain = new JMyRadioButton("Text");
    jTypeHTML = new JMyRadioButton("HTML");
    jTypeFile = new JMyRadioButton("File");

    sigTypeArea = new MsgTypeArea("_sig", (short) -1, false, null, false, true, false);
    jFileField = new JMyTextField();
    sigTypeArea.setMaximumSize(new Dimension(350, 200));
  }


  private void initializeComponents() {
    ButtonGroup group = new ButtonGroup();
    jTypePlain.getModel().setGroup(group);
    jTypeHTML.getModel().setGroup(group);
    jTypeFile.getModel().setGroup(group);
    group.setSelected(jTypePlain.getModel(), true);

    jTypePlain.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pressedText();
      }
    });
    jTypeHTML.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pressedHTML();
      }
    });
    jTypeFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pressedFile();
      }
    });

    // add File Chooser to Browse button
    jBrowse.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        javax.swing.filechooser.FileFilter ff_txt = new ExtensionFileFilter("Text Documents (*.txt)", "txt");
        javax.swing.filechooser.FileFilter ff_htm = new ExtensionFileFilter("HTML Files (*.htm; *.html)", new String[] { "htm", "html"});
        fc.addChoosableFileFilter(ff_txt);
        fc.addChoosableFileFilter(ff_htm);
        fc.setFileFilter(ff_txt);
        int retVal = fc.showOpenDialog(AccountOptionsSignaturesPanel.this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
          File file = fc.getSelectedFile();
          jFileField.setText(file.getAbsolutePath());
          isChangeAttempted = true;
          fireDocumentUpdated();
        }
      }
    });

    jNew.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        isChangeAttempted = true;
        fireDocumentUpdated();
        String type = jTypeFile.isSelected() ? "text/file" : (sigTypeArea.isHTML() ? "text/html" : "text/plain");
        String title = "Signature #" + (listDatasV.size() + 1);
        listHeadersV.addElement(title);
        listDatasV.addElement(new String[] { title, type, "" });
        jSigList.setListData(listHeadersV);
        jSigList.setSelectedIndex(listDatasV.size() - 1);
        if (defaultSig < 0)
          setDefaultSig(0);
      }
    });

    jRemove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final int index = jSigList.getSelectedIndex();
        if (index >= 0) {
          isChangeAttempted = true;
          fireDocumentUpdated();
          isRemoveInProgress = true;
          listHeadersV.removeElementAt(index);
          listDatasV.removeElementAt(index);
          jSigList.setListData(listHeadersV);
          if (listDatasV.size() > index) {
            jSigList.setSelectedIndex(index);
          } else if (listDatasV.size() > 0) {
            jSigList.setSelectedIndex(listDatasV.size()-1);
          } else {
            jSigList.setSelectedIndex(-1);
          }
          if (index < defaultSig)
            setDefaultSig(defaultSig - 1);
          else if (index == defaultSig)
            setDefaultSig(0);
        }
      }
    });

    jRename.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String text = JOptionPane.showInputDialog(AccountOptionsSignaturesPanel.this, "Rename signature to:", "Rename Signature", JOptionPane.INFORMATION_MESSAGE);
        if (text != null && text.trim().length() > 0) {
          isChangeAttempted = true;
          fireDocumentUpdated();
          String[] data = (String[]) listDatasV.elementAt(currentSig);
          data[0] = text;
          int savedSelection = jSigList.getSelectedIndex();
          listHeadersV.setElementAt(text, currentSig);
          jSigList.setListData(listHeadersV);
          jSigList.setSelectedIndex(savedSelection);
          jSigList.repaint();
        }
      }
    });

    jSetAsDefault.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (currentSig != defaultSig) {
          isChangeAttempted = true;
          fireDocumentUpdated();
        }
        setDefaultSig(currentSig);
      }
    });

    jSigList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    jSigList.setCellRenderer(new MyListCellRenderer());
    jSigList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        int prevIndex = currentSig;
        int newIndex = jSigList.getSelectedIndex();
        if (newIndex != prevIndex) {
          if (!isRemoveInProgress) {
            storeEdit(prevIndex);
          }
        }
        if (newIndex >= 0) {
          jTypeFile.setEnabled(true);
          jTypePlain.setEnabled(true);
          jTypeHTML.setEnabled(true);
          jRemove.setEnabled(true);
          jRename.setEnabled(true);
          jSetAsDefault.setEnabled(true);
          jBrowse.setEnabled(true);
          // set new row as current data being edited
          String[] data = (String[]) listDatasV.elementAt(newIndex);
          if (data[1].equals("text/file")) {
            jTypeFile.setSelected(true);
            jFileField.setText(data[2]);
            sigTypeArea.setContentText("", false, false);
            pressedFile();
          } else {
            jFileField.setText("");
            if (data[1].equals("text/plain")) {
              jTypePlain.setSelected(true);
              sigTypeArea.setHTML(false, false);
              sigTypeArea.setContentText(data[2], false, false);
              pressedText();
            } else {
              jTypeHTML.setSelected(true);
              sigTypeArea.setHTML(true, false);
              sigTypeArea.setContentText(data[2], false, false);
              pressedHTML();
            }
          }
        } else {
          clearEditableFieldsAndDisable();
        }
        currentSig = newIndex;
        isRemoveInProgress = false;
      }
    });
  }


  private void layoutPanel() {
    JPanel panel = this;
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    panel.setLayout(new GridBagLayout());

    int posY = 0;

    panel.add(makeDivider("Signature settings"), new GridBagConstraints(0, posY, 4, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(Images.get(ImageNums.SIGNATURE32)), new GridBagConstraints(0, posY, 1, 2, 0, 0, 
        GridBagConstraints.NORTH, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 2), 0, 0));
    panel.add(jAddSigToNew, new GridBagConstraints(1, posY, 3, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 2, 1, 5), 0, 0));
    panel.add(jAddSigToRepFwd, new GridBagConstraints(1, posY+1, 3, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 2, 5, 5), 0, 0));
    posY += 2;

    panel.add(makeDivider("Signatures"), new GridBagConstraints(0, posY, 4, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    JScrollPane list = new JScrollPane(jSigList);
    panel.add(list, new GridBagConstraints(1, posY, 2, 3, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 2, 5, 5), 0, 0));
    panel.add(jNew, new GridBagConstraints(3, posY, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 2, 5), 0, 0));
    panel.add(jRemove, new GridBagConstraints(3, posY+1, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 2, 5), 0, 0));
    panel.add(jRename, new GridBagConstraints(3, posY+2, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 5, 5), 0, 0));
    //panel.add(jSetAsDefault, new GridBagConstraints(3, posY+3, 1, 1, 0, 0, 
        //GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 5, 5), 0, 0));
    posY += 3;

    panel.add(makeDivider("Edit Signature"), new GridBagConstraints(0, posY, 4, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(jTypePlain, new GridBagConstraints(0, posY, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 0, 1), 0, 0));
    panel.add(jTypeHTML, new GridBagConstraints(0, posY+1, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0, 5, 5, 1), 0, 0));
    panel.add(sigTypeArea, new GridBagConstraints(1, posY, 2, 3, 10, 20, 
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 2, 2, 5), 0, 0));
    panel.add(jSetAsDefault, new GridBagConstraints(3, posY, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jTypeFile, new GridBagConstraints(0, posY+3, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(1, 5, 5, 1), 0, 0));
    panel.add(jFileField, new GridBagConstraints(1, posY+3, 2, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 2, 5, 5), 0, 0));
    panel.add(jBrowse, new GridBagConstraints(3, posY+3, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));
    posY += 4;
  }

  private void pressedText() {
    boolean anySelected = jSigList.getSelectedIndex() >= 0;
    sigTypeArea.setEditable(anySelected);
    sigTypeArea.setEnabled(anySelected);
    jFileField.setEditable(false);
    jFileField.setEnabled(false);
    jBrowse.setEnabled(false);
    sigTypeArea.setHTML(false, true);
    if (anySelected)
      sigTypeArea.requestFocus();
  }

  private void pressedHTML() {
    boolean anySelected = jSigList.getSelectedIndex() >= 0;
    sigTypeArea.setEditable(anySelected);
    sigTypeArea.setEnabled(anySelected);
    jFileField.setEditable(false);
    jFileField.setEnabled(false);
    jBrowse.setEnabled(false);
    sigTypeArea.setHTML(true, true);
    if (anySelected)
      sigTypeArea.requestFocus();
  }

  private void pressedFile() {
    boolean anySelected = jSigList.getSelectedIndex() >= 0;
    sigTypeArea.setEditable(false);
    sigTypeArea.setEnabled(false);
    jFileField.setEditable(anySelected);
    jFileField.setEnabled(anySelected);
    jBrowse.setEnabled(anySelected);
    if (anySelected)
      jFileField.requestFocus();
  }

  private void storeEdit(int sigIndex) {
    if (sigIndex >= 0 && sigIndex < jSigList.getModel().getSize()) {
      // remember the previous row editing state
      String type = jTypeFile.isSelected() ? "text/file" : (sigTypeArea.isHTML() ? "text/html" : "text/plain");
      if (listDatasV.size() > 0) {
        String content = "";
        if (jTypeFile.isSelected())
          content = jFileField.getText();
        else
          content = sigTypeArea.getContent()[1];
        String title = ((String[]) listDatasV.elementAt(currentSig))[0];
        listDatasV.setElementAt(new String[] { title, type, content }, sigIndex);
        isChangeAttempted = true;
        fireDocumentUpdated();
      }
    }
  }

  private void clearEditableFieldsAndDisable() {
    jTypeFile.setEnabled(false);
    jTypePlain.setEnabled(false);
    jTypeHTML.setEnabled(false);
    jFileField.setText("");
    jFileField.setEnabled(false);
    jFileField.setEditable(false);
    jBrowse.setEnabled(false);
    sigTypeArea.setContentText("", false, false);
    sigTypeArea.setEnabled(false);
    sigTypeArea.setEditable(false);
    jRemove.setEnabled(false);
    jRename.setEnabled(false);
    jSetAsDefault.setEnabled(false);
  }

  private void setDefaultSig(int index) {
    if (index < 0 || index >= listDatasV.size()) {
      if (listDatasV.size() > 0)
        defaultSig = 0;
      else
        defaultSig = -1;
    } else {
      defaultSig = index;
    }
    jSigList.repaint();
    isChangeAttempted = true;
    fireDocumentUpdated();
  }

  public static JPanel makeDivider(String str) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(0, 0, 0, 0));
    panel.setLayout(new GridBagLayout());
    panel.add(new JMyLabel(str), new GridBagConstraints(0, 0, 1, 1, 0, 0, 
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 5), 0, 0));
    panel.add(new JSeparator(), new GridBagConstraints(1, 0, 1, 1, 10, 0, 
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(0, 5, 0, 0), 0, 0));
    return panel;
  }


  private class MyListCellRenderer extends MyDefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      String text = "" + value + (index == defaultSig ? " (Default signature)" : "");
      return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
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


  /*
  public static void main(String[] args) {
    final AccountOptionsSignaturesPanel p = new AccountOptionsSignaturesPanel();
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
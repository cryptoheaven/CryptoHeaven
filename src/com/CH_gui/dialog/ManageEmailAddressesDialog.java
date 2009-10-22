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

package com.CH_gui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.records.filters.*;

import com.CH_co.gui.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.frame.*;
import com.CH_gui.gui.*;
import com.CH_guiLib.gui.*;
import com.CH_gui.list.*;
import com.CH_gui.usrs.*;

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
 * <b>$Revision: 1.9 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class ManageEmailAddressesDialog extends GeneralDialog {

  public static final int DEFAULT_OK_INDEX = 0;
  public static final int DEFAULT_CANCEL_INDEX = 1;

  private JList jEmlList;

  private JButton jNew;
  private JButton jRemove;
  private JButton jRename;
  private JButton jSetAsDefault;

  private JButton jOk;

  private UserRecord selectedUserRecord;
  private String[] defaultEmailDomains;
  private EmailRecord[] defaultEmailRecords;

  private Vector listEmlsV = new Vector();
  private int currentEml = -1;
  private int defaultEml = -1;
  private boolean isChangeAttempted;
  private int limitEmlAddrs = -1;

  private ServerInterfaceLayer SIL;
  private AccountOptionsDialog accountOptionsDialog;

  /** Creates new ManageEmailAddressesDialog */
  public ManageEmailAddressesDialog(Dialog owner, UserRecord userRecord, AccountOptionsDialog accountOptionsDialog) {
    super(owner, "Manage E-mail Addresses");
    this.selectedUserRecord = userRecord;
    this.accountOptionsDialog = accountOptionsDialog;
    initialize(owner);
  }
  public ManageEmailAddressesDialog(Frame owner, UserRecord userRecord, AccountOptionsDialog accountOptionsDialog) {
    super(owner, "Manage E-mail Addresses");
    this.selectedUserRecord = userRecord;
    this.accountOptionsDialog = accountOptionsDialog;
    initialize(owner);
  }

  private void initialize(Component owner) {
    SIL = MainFrame.getServerInterfaceLayer();
    JButton[] buttons = createButtons();
    createComponents();
    initializeComponents();
    setEnabledButtons();
    JPanel panel = createMainPanel();
    init(owner, buttons, panel, DEFAULT_OK_INDEX, DEFAULT_CANCEL_INDEX);
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[2];
    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
    buttons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedOk();
      }
    });
    jOk = buttons[0];
    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedCancel();
      }
    });
    return buttons;
  }

  private void createComponents() {
    jEmlList = new JList();
    jNew = new JMyButton("Add");
    jRemove = new JMyButton("Remove");
    jRename = new JMyButton("Edit");
    jSetAsDefault = new JMyButton("Set as Default");
  }

  private void initializeComponents() {
    jNew.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ActionListener OKAction = new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            String text = event.getActionCommand();
            if (text != null && text.trim().length() > 0) {
              if (EmailRecord.findEmailAddress(listEmlsV, text) < 0) {
                if (listEmlsV.size() < limitEmlAddrs) {
                  isChangeAttempted = true;
                  listEmlsV.addElement(text);
                  jEmlList.setListData(listEmlsV);
                  jEmlList.setSelectedIndex(listEmlsV.size() - 1);
                  if (defaultEml < 0)
                    setDefaultEml(0);
                }
              } else {
                MessageDialog.showErrorDialog(ManageEmailAddressesDialog.this, "Duplicate E-mail Address '" + text + "'.", com.CH_gui.lang.Lang.rb.getString("title_Invalid_Input"), true);
              }
            }
          }
        };
        String modalReturn = showInputDialog(ManageEmailAddressesDialog.this, null, "Type in your new e-mail address:", "Display as:", "New E-mail Address", OKAction);
      }
    });

    jRemove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final int index = jEmlList.getSelectedIndex();
        if (index >= 0) {
          isChangeAttempted = true;
          listEmlsV.removeElementAt(index);
          jEmlList.setListData(listEmlsV);
          if (listEmlsV.size() > index) {
            jEmlList.setSelectedIndex(index);
          } else if (listEmlsV.size() > 0) {
            jEmlList.setSelectedIndex(listEmlsV.size()-1);
          } else {
            jEmlList.setSelectedIndex(-1);
          }
          if (index < defaultEml)
            setDefaultEml(defaultEml - 1);
          else if (index == defaultEml)
            setDefaultEml(0);
        }
      }
    });

    jRename.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final String _emlAddr = (String) listEmlsV.elementAt(currentEml);
        final int _currentEml = currentEml;
        ActionListener OKAction = new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            String text = event.getActionCommand();
            if (text != null && text.trim().length() > 0) {
              if (EmailRecord.findEmailAddress(listEmlsV, text) < 0 || EmailRecord.isAddressEqual(_emlAddr, text)) {
                if (_currentEml < listEmlsV.size()) {
                  isChangeAttempted = true;
                  listEmlsV.setElementAt(text, _currentEml);
                  int savedSelection = jEmlList.getSelectedIndex();
                  jEmlList.setListData(listEmlsV);
                  jEmlList.setSelectedIndex(savedSelection);
                  jEmlList.repaint();
                }
              } else {
                MessageDialog.showErrorDialog(ManageEmailAddressesDialog.this, "Duplicate E-mail Address '" + text + "'.", com.CH_gui.lang.Lang.rb.getString("title_Invalid_Input"), true);
              }
            }
          }
        };
        String modalReturn = showInputDialog(ManageEmailAddressesDialog.this, _emlAddr, "Edit your e-mail address:", "Display as:", "Edit E-mail Address", OKAction);
      }
    });

    jSetAsDefault.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (currentEml != defaultEml) {
          isChangeAttempted = true;
        }
        setDefaultEml(currentEml);
      }
    });

    jEmlList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    jEmlList.setCellRenderer(new MyListCellRenderer());
    jEmlList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        currentEml = jEmlList.getSelectedIndex();
        setEnabledButtons();
      }
    });

    if (selectedUserRecord.isFreePromoAccount())
      limitEmlAddrs = 1;
    else if (selectedUserRecord.isBusinessAccount())
      limitEmlAddrs = EmailRecord.MAX_EMAIL_ADDRESSES_BUSINESS;
    else 
      limitEmlAddrs = EmailRecord.MAX_EMAIL_ADDRESSES_PERSONAL;
    // fill list with data
    fetchData(selectedUserRecord);
  }

  private JPanel createMainPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    panel.setLayout(new GridBagLayout());

    int posY = 0;

    panel.add(AccountOptionsSignaturesPanel.makeDivider("E-mail Address management"), new GridBagConstraints(0, posY, 4, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 2, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(Images.get(ImageNums.SIGNATURE32)), new GridBagConstraints(0, posY, 1, 2, 0, 0, 
        GridBagConstraints.NORTH, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    URL signupURL = null;
    if (selectedUserRecord != null) {
      try {
        signupURL = new URL(URLs.get(URLs.SIGNUP_PAGE)+"?UserID=" + selectedUserRecord.userId);
      } catch (MalformedURLException e) {
      }
    }
    if (selectedUserRecord != null && selectedUserRecord.isFreePromoAccount()) {
      panel.add(new JMyLabel("Demo accounts have e-mail address management disabled."), new GridBagConstraints(1, posY, 3, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));
      panel.add(new JMyLinkLabel("Click here to upgrade.", signupURL), new GridBagConstraints(1, posY+1, 3, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));
    } else if (selectedUserRecord != null && selectedUserRecord.isBusinessAccount()) {
      panel.add(new JMyLabel("You are allowed up to '"+limitEmlAddrs+"' personalized e-mail addresses per account."), new GridBagConstraints(1, posY, 3, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      panel.add(new JMyLabel("All changes to e-mail addresses take effect immediately."), new GridBagConstraints(1, posY+1, 3, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));
    } else {
      panel.add(new JMyLabel("You are allowed upto '"+limitEmlAddrs+"' personalized e-mail addresses."), new GridBagConstraints(1, posY, 3, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));
//      panel.add(new JMyLinkLabel("Click here to upgrade.", signupURL), new GridBagConstraints(1, posY+1, 3, 1, 10, 0, 
//          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));
      panel.add(new JMyLabel("All changes to your e-mail addresses take effect immediately."), new GridBagConstraints(1, posY+1, 3, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));
    }
    posY += 2;

    panel.add(AccountOptionsSignaturesPanel.makeDivider("E-mail Addresses"), new GridBagConstraints(0, posY, 4, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 2, 5), 0, 0));
    posY ++;

    panel.add(new JScrollPane(jEmlList), new GridBagConstraints(1, posY, 2, 5, 10, 10, 
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 2, 5, 5), 0, 0));
    panel.add(jNew, new GridBagConstraints(3, posY, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 2, 5), 0, 0));
    panel.add(jRemove, new GridBagConstraints(3, posY+1, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 2, 5), 0, 0));
    panel.add(jRename, new GridBagConstraints(3, posY+2, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 2, 5), 0, 0));
    panel.add(jSetAsDefault, new GridBagConstraints(3, posY+3, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 5, 5), 0, 0));
    posY += 5;

    return panel;
  }

  private void fetchData(final UserRecord forUserRecord) {
    new Thread("Manage Email Addresses Domains Fetcher") {
      public void run() {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");

        // change the priority of this thread to minimum
        setPriority(MIN_PRIORITY);

        try {
          // fetch all available domains and all current email addresses
          Obj_List_Co request = new Obj_List_Co();
          request.objs = new Object[] { forUserRecord.userId, Boolean.TRUE };
          if (trace != null) trace.data(10, "about to get domains and email addresses");
          ClientMessageAction reply = SIL.submitAndFetchReply(new MessageAction(CommandCodes.EML_Q_GET_DOMAINS, request), 30000);
          if (trace != null) trace.data(11, "about to run reply", reply);
          if (reply != null) {
            DefaultReplyRunner.nonThreadedRun(SIL, reply);
            ProtocolMsgDataSet set = reply.getMsgDataSet();
            if (set instanceof Obj_List_Co) {
              Obj_List_Co set2 = (Obj_List_Co) set;
              final String[] domains = (String[]) set2.objs[0];
              final EmailRecord[] emailRecs = ((Eml_Get_Rp) set2.objs[1]).emailRecords;
              if (trace != null) trace.data(12, "about to set gui with fetched data");
              // Perform GUI updates in a GUI-safe-thread
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  // fill GUI with data
                  defaultEmailRecords = emailRecs;
                  defaultEmailDomains = domains;
                  // sort fetched data
                  if (defaultEmailRecords != null && defaultEmailRecords.length > 1)
                    Arrays.sort(defaultEmailRecords, new ListComparator());
                  if (defaultEmailDomains != null && defaultEmailDomains.length > 1)
                    Arrays.sort(defaultEmailDomains);
                  int defaultEmlIndex = -1;
                  listEmlsV.clear();
                  if (defaultEmailRecords != null) {
                    for (int i=0; i<defaultEmailRecords.length; i++) {
                      listEmlsV.addElement(defaultEmailRecords[i].getEmailAddressFull());
                      if (forUserRecord.defaultEmlId.equals(defaultEmailRecords[i].emlId))
                        defaultEmlIndex = i;
                    }
                  }
                  defaultEml = defaultEmlIndex;
                  jEmlList.setListData(listEmlsV);
                  setEnabledButtons();
                }
              });
            }
          }
        } catch (Throwable t) {
          if (trace != null) trace.exception(getClass(), 100, t);
        }

        if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
        if (trace != null) trace.exit(getClass());
        if (trace != null) trace.clear();
      }
    }.start();
  }


  private void pressedOk() {
    new Thread("Manage Email Addresses Commiter") {
      public void run() {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");

        try {
          Vector emlsToCreateV = (Vector) listEmlsV.clone();
          Vector emlIDsToDeleteV = new Vector();
          Vector emlsToDeleteV = new Vector();
          Vector emlsNoChangeV = new Vector();
          // find email addresses to delete
          for (int i=0; i<defaultEmailRecords.length; i++) {
            EmailRecord emlRec = defaultEmailRecords[i];
            String addr = emlRec.getEmailAddressFull();
            boolean found = false;
            int index = EmailRecord.findEmailAddress(emlsToCreateV, addr);
            // Check if the "Personal" part is the same, if changed then treat it as a different email address
            if (index >= 0) {
              String emlToCreate = (String) emlsToCreateV.elementAt(index);
              if (!EmailRecord.isAddressEqualStrict(emlToCreate, addr))
                index = -1;
            }
            if (index < 0) {
              emlIDsToDeleteV.addElement(emlRec.emlId);
              emlsToDeleteV.addElement(addr);
            } else {
              emlsToCreateV.removeElementAt(index);
              emlsNoChangeV.addElement(addr);
            }
          }
          String[] emlsToCreate = (String[]) ArrayUtils.toArray(emlsToCreateV, String.class);
          Long[] emlIDsToDelete = (Long[]) ArrayUtils.toArray(emlIDsToDeleteV, Long.class);
          if (emlsToCreate != null && emlsToCreate.length == 0)
            emlsToCreate = null;
          if (emlIDsToDelete != null && emlIDsToDelete.length == 0)
            emlIDsToDelete = null;
          String emlDefault = null;
          if (defaultEml >= 0)
            emlDefault = (String) listEmlsV.elementAt(defaultEml);
          ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.EML_Q_MANAGE, new Obj_List_Co(new Object[] { selectedUserRecord.userId, emlsToCreate, emlIDsToDelete, emlDefault })), 60000);
          DefaultReplyRunner.nonThreadedRun(SIL, msgAction);
          if (msgAction != null && msgAction.getActionCode() > 0) {
            setVisible(false);
            dispose();
            if (accountOptionsDialog != null)
              accountOptionsDialog.refreshEmailAddressFromCache();
          }
        } catch (Throwable t) {
          if (trace != null) trace.exception(getClass(), 100, t);
        }

        if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
        if (trace != null) trace.exit(getClass());
        if (trace != null) trace.clear();
      }
    }.start();
  }

  private void pressedCancel() {
    setVisible(false);
    dispose();
  }

  private void setEnabledButtons() {
    if (limitEmlAddrs < 0 || limitEmlAddrs > listEmlsV.size())
      jNew.setEnabled(defaultEmailDomains != null && defaultEmailDomains.length > 0);
    else
      jNew.setEnabled(false);
    jRemove.setEnabled(currentEml >= 0);
    jRename.setEnabled(currentEml >= 0);
    jSetAsDefault.setEnabled(currentEml >= 0);
    jOk.setEnabled(isChangeAttempted);
  }

  private void setDefaultEml(int index) {
    int defaultEmlSave = defaultEml;
    if (index < 0 || index >= listEmlsV.size()) {
      if (listEmlsV.size() > 0)
        defaultEml = 0;
      else
        defaultEml = -1;
    } else {
      defaultEml = index;
    }
    if (defaultEml != defaultEmlSave) {
      jEmlList.repaint();
      isChangeAttempted = true;
      setEnabledButtons();
    }
  }

  private class MyListCellRenderer extends MyDefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      String text = "" + value + (index == defaultEml ? " (Default)" : "");
      return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
    }
  }


  private String showInputDialog(Component parent, String defaultEml, String label, String label2, String title, final ActionListener OKActionListener) {
    // server returned available domains
    String[] defaultDomains = (String[]) defaultEmailDomains.clone();
    // add label domains
    String[] labelDomains = URLs.getElements(URLs.DOMAIN_MAIL);
    for (int i=0; labelDomains!=null && i<labelDomains.length; i++) {
      String domain = labelDomains[i];
      if (EmailRecord.findDomain(defaultDomains, domain) < 0)
        defaultDomains = (String[]) ArrayUtils.concatinate(defaultDomains, new String[] { domain });
    }
    // add domain of current edited email address
    if (defaultEml != null && defaultEml.length() > 0 && EmailRecord.findDomain(defaultDomains, EmailRecord.getDomain(defaultEml)) < 0) {
      defaultDomains = (String[]) ArrayUtils.concatinate(defaultDomains, new String[] { EmailRecord.getDomain(defaultEml) });
    }
    String[] atDefaultDomains = new String[defaultDomains.length];
    for (int i=0; i<atDefaultDomains.length; i++) {
      atDefaultDomains[i] = "@" + defaultDomains[i];
    }
    // set default values
    String nick = "";
    String[] domains = atDefaultDomains;
    String personal = null;
    int defaultEmlDomainIndex = -1;
    if (defaultEml != null && defaultEml.length() > 0 && EmailRecord.gatherAddresses(defaultEml) != null) {
      nick = EmailRecord.getNick(defaultEml);
      personal = EmailRecord.getPersonal(defaultEml);
      String domain = EmailRecord.getDomain(defaultEml);
      // put the current domain as 1st in the combo box so it is selected also
      defaultEmlDomainIndex = EmailRecord.findDomain(defaultDomains, domain);
    }
    // create dialog components
    final JMyTextComboBox jInput = new JMyTextComboBox(nick, 15, domains);
    final JMyTextField jDisplayAs = new JMyTextField(personal != null ? personal : "");
    if (defaultEmlDomainIndex >= 0)
      jInput.setSelectedIndex(defaultEmlDomainIndex);
    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    Keymap textMap = jInput.getTextField().getKeymap();
    textMap.removeKeyStrokeBinding(enter);
    textMap = jDisplayAs.getKeymap();
    textMap.removeKeyStrokeBinding(enter);
    // create dialog panel
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    panel.add(new JMyLabel(label), new GridBagConstraints(0, 0, 1, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 2, 5), 0, 0));
    panel.add(jInput, new GridBagConstraints(0, 1, 1, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 3, 5), 0, 0));
    panel.add(new JMyLabel(label2), new GridBagConstraints(0, 2, 1, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 2, 5), 0, 0));
    panel.add(jDisplayAs, new GridBagConstraints(0, 3, 1, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 5, 5), 0, 0));
    panel.add(new JLabel(), new GridBagConstraints(0, 4, 1, 1, 10, 10, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
    JButton jOk = new JMyButton("OK");
    JButton jCancel = new JMyButton("Cancel");
    final String[] rcStr = new String[1];
    jOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        String emlAddr = jInput.getTextCombined();
        String personal = jDisplayAs.getText().trim();
        if (EmailRecord.isEmailFormatValid(emlAddr)) {
          String fullEmlAddr = personal != null && personal.length() > 0 ? personal + " <" + emlAddr + ">" : emlAddr;
          rcStr[0] = fullEmlAddr;
          SwingUtilities.windowForComponent((Component) event.getSource()).dispose();
          OKActionListener.actionPerformed(new ActionEvent(event.getSource(), 0, fullEmlAddr));
        } else {
          jInput.getTextField().selectAll();
          MessageDialog.showErrorDialog(ManageEmailAddressesDialog.this, "Invalid E-mail Address '" + emlAddr + "'.", com.CH_gui.lang.Lang.rb.getString("title_Invalid_Input"), true);
        }
      }
    });
    jCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        rcStr[0] = null;
        SwingUtilities.windowForComponent((Component) event.getSource()).dispose();
      }
    });
    JButton[] optionButtons = new JButton[] { jOk, jCancel };
    MessageDialog.showDialog(ManageEmailAddressesDialog.this, panel, title, MessageDialog.QUESTION_MESSAGE, optionButtons, null, false, false, false);
    // modal vertion commented out
    //MessageDialog.showDialog(ManageEmailAddressesDialog.this, panel, title, MessageDialog.QUESTION_MESSAGE, optionButtons, null, true, false, false);
    return rcStr[0];
  }

  public static void main(String[] args) {
    final ManageEmailAddressesDialog d = new ManageEmailAddressesDialog((Frame) null, null, null);
    d.addWindowListener(new WindowAdapter() {
      public void windowClosed(WindowEvent e) {
        System.exit(0);
      }
    });
    d.pack();
    d.setVisible(true);
  }

}
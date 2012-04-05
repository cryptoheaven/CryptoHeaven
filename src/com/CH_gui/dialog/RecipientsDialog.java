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

package com.CH_gui.dialog;

import com.CH_cl.service.records.EmailAddressRecord;
import com.CH_co.service.records.EmailRecord;
import com.CH_co.service.records.Record;
import com.CH_co.service.records.UserRecord;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import com.CH_co.util.DisposableObj;
import com.CH_co.util.ImageNums;
import com.CH_co.util.Misc;
import com.CH_gui.gui.InitialFocusRequestor;
import com.CH_gui.gui.JMyButton;
import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.MyInsets;
import com.CH_gui.list.*;
import com.CH_gui.util.GeneralDialog;
import com.CH_gui.util.Images;
import com.CH_gui.util.VisualsSavable;
import com.CH_guiLib.gui.JMyTextField;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
* <b>$Revision: 1.31 $</b>
* @author  Marcin Kurzawa
* @version 
*/
public class RecipientsDialog extends GeneralDialog implements DisposableObj, VisualsSavable, ObjectsProviderUpdaterI {

  private static final int DEFAULT_BUTTON_INDEX = 0;
  private static final int DEFAULT_CANCEL_BUTTON_INDEX = 2;

  private JTextField jSearchName;
  private JButton jAddInput;
  private JButton jOkButton;
  private JButton jOtherUsers;
  private DualListBox dualListBox;

  private Record[] selectedResults;

  private SearchDocumentListener searchDocumentListener;
  private SearchKeyListener searchKeyListener;
  private ListNavigator navigateKeyListener;
  private DualBoxChangeListener dualBoxChangeListener;

  private RecipientListProvider provider;
  private ListUpdatableI updatable;

  private boolean isOKed;

  /** Creates new RecipientsDialog */
  public RecipientsDialog(Frame frame, String titlePostfix, Record[] initialChoices) {
    this(frame, titlePostfix, initialChoices, null, null, true);
  }
  /** Creates new RecipientsDialog */
  public RecipientsDialog(Frame frame, String titlePostfix, Record[] initialChoices, String selectedStringsDelimited, String searchString, boolean skipDialogIfPerfectMatch) {
    super(frame, com.CH_gui.lang.Lang.rb.getString("title_Select_Recipients") + (titlePostfix != null && titlePostfix.length() > 0 ? (" - " + titlePostfix):""));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecipientsDialog.class, "RecipientsDialog(Frame frame, String titlePostfix, Record[] initialChoices, String selectedStringsDelimited, String searchString, boolean skipDialogIfPerfectMatch)");
    if (trace != null) trace.args(frame);
    if (trace != null) trace.args(titlePostfix);
    if (trace != null) trace.args(initialChoices);
    if (trace != null) trace.args(selectedStringsDelimited);
    if (trace != null) trace.args(searchString);
    if (trace != null) trace.args(skipDialogIfPerfectMatch);

    if (trace != null) trace.data(60, "creating components...");

    JButton[] buttons = createButtons();
    JPanel panel = createMainPanel();
    jOkButton.setEnabled(true);
    jSearchName.addHierarchyListener(new InitialFocusRequestor());

    provider = new RecipientListProvider(true, true, true, true, true);
    dualListBox.addDefaultSourceElements(provider.provide(null));

    boolean perfectMatch = initChosenValues(frame, initialChoices, selectedStringsDelimited, searchString);

    if (skipDialogIfPerfectMatch && perfectMatch) {
      if (trace != null) trace.data(70, "skipping gui");
      // skipping GUI
      pressedOK();
    } else {
      if (trace != null) trace.data(80, "making sure address books are fetched");
      provider.registerForUpdates(dualListBox);
      if (trace != null) trace.data(81, "making gui");
      setModal(skipDialogIfPerfectMatch);
      super.init(frame, buttons, panel, null, DEFAULT_BUTTON_INDEX, DEFAULT_CANCEL_BUTTON_INDEX);
    }

    if (trace != null) trace.exit(RecipientsDialog.class);
  }

  /**
  * @return true if "perfect match" -- all strings were resolved at once, without additional searches/editing/etc
  */
  private boolean initChosenValues(Frame parent, Record[] initialChoices, String selectedStringsDelimited, String searchString) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecipientsDialog.class, "initChosenValues(Frame parent, Record[] initialChoices, String selectedStringsDelimited, String searchString)");
    if (trace != null) trace.args(parent);
    if (trace != null) trace.args(initialChoices);
    if (trace != null) trace.args(selectedStringsDelimited);
    if (trace != null) trace.args(searchString);

    // in case dialog is canceled, the result is the initial choice
    this.selectedResults = initialChoices;

    if (initialChoices != null && initialChoices.length > 0)
      dualListBox.moveToDefaultDestinationElements(initialChoices);

    boolean perfectMatch = initSelectedStrings(parent, selectedStringsDelimited, searchString);

    if (trace != null) trace.exit(RecipientsDialog.class, perfectMatch);
    return perfectMatch;
  }

  /**
  * @return true if "perfect match" -- all strings were resolved at once, without additional searches/editing/etc
  */
  private boolean initSelectedStrings(Window parent, String selectedStringsDelimited, String searchString) {
    boolean perfectMatch = true;

    // initial search string
    if (searchString != null && searchString.length() > 0) {
      jSearchName.setText(searchString);
      perfectMatch = false;
    } else {
      jSearchName.setText("");
    }

    String[] selectedStrings = Misc.parseDelimitedStr(selectedStringsDelimited, ",;");
    // preselect items by string values
    if (selectedStrings != null) {
      String[] failedStrs = dualListBox.moveToDestinationElementsSearchByUniqueStringsOnly(selectedStrings);
      ArrayList otherFailedStrsL = null;
      // process all failed strings to see if they qualify as UserRecord or EmailAddress
      if (failedStrs != null && failedStrs.length > 0) {
        for (int i=0; i<failedStrs.length; i++) {
          String failedStr = failedStrs[i];
          if (isStrInUserNameNumberFormat(failedStr, null)) {
            selectOrAddUserRecord(failedStr);
          } else if (isEmailLineValid(failedStr)) {
            String emailAddress = failedStr;
            selectOrAddEmailAddress(emailAddress);
          } else {
            if (otherFailedStrsL == null) otherFailedStrsL = new ArrayList();
            otherFailedStrsL.add(failedStr);
          }
        }
      }
      if (otherFailedStrsL != null && otherFailedStrsL.size() > 0) {
        // if no search string defined, make these the search strings
        StringBuffer searchStr = new StringBuffer();
        for (int i=0; i<otherFailedStrsL.size(); i++) {
          searchStr.append(otherFailedStrsL.get(i));
          if (i+1 < otherFailedStrsL.size())
            searchStr.append(", ");
        }
        jSearchName.setText(searchStr.toString());
        String searchElement = ListUtils.getLastElement(searchStr.toString());
        boolean found = dualListBox.setSourceSearchString(searchElement, searchElement.length() > 0);
        if (!found)
          otherUsersAction();
        perfectMatch = false;
      }
    }
    return perfectMatch;
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[3];

    jOkButton = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
    jOkButton.addActionListener(new OKActionListener());
    buttons[0] = jOkButton;

    jOtherUsers = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Other_Users_..."));
    jOtherUsers.addActionListener(new OtherUsersActionListener());
    buttons[1] = jOtherUsers;

    buttons[2] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[2].addActionListener(new CancelActionListener());

    return buttons;
  }

  private JPanel createMainPanel() {
    JPanel panel = new JPanel();

    panel.setLayout(new GridBagLayout());

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Search_by_name,_select_from_list,_or_enter_an_email_address")), new GridBagConstraints(0, 0, 2, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));

    jSearchName = new JMyTextField(40);
    searchDocumentListener = new SearchDocumentListener();
    searchKeyListener = new SearchKeyListener();
    jSearchName.getDocument().addDocumentListener(searchDocumentListener);
    jSearchName.addKeyListener(searchKeyListener);
    panel.add(jSearchName, new GridBagConstraints(0, 1, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    jAddInput = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Add_Recipients"), Images.get(ImageNums.EMAIL_SYMBOL_SMALL));
    jAddInput.setEnabled(jSearchName.getText().length() > 0);
    jAddInput.addActionListener(new AddInputActionListener());
    panel.add(jAddInput, new GridBagConstraints(1, 1, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));

    dualListBox = new DualListBox(false, false, false, false);
    navigateKeyListener = new ListNavigator(dualListBox.getSourceList());
    jSearchName.addKeyListener(navigateKeyListener);
    dualListBox.setSourceChoicesTitle(com.CH_gui.lang.Lang.rb.getString("label_Available_Recipients"));
    dualListBox.setDestinationChoicesTitle(com.CH_gui.lang.Lang.rb.getString("label_Chosen_Recipients"));
    dualBoxChangeListener = new DualBoxChangeListener();
    dualListBox.addChangeListener(dualBoxChangeListener);

    panel.add(dualListBox, new GridBagConstraints(0, 2, 2, 5, 10, 10, 
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));

    return panel;
  }

  private void setEnabledButtons() {
    //jOkButton.setEnabled(dualListBox.getDestinationElements().hasMoreElements());
    jOkButton.setEnabled(true);
  }

  public Record[] getRecipients() {
    return selectedResults;
  }

  public void closeDialog() {
    if (dualBoxChangeListener != null && dualListBox != null) {
      dualListBox.removeChangeListener(dualBoxChangeListener);
      dualBoxChangeListener = null;
    }
    if (searchDocumentListener != null && jSearchName != null) {
      jSearchName.getDocument().removeDocumentListener(searchDocumentListener);
      searchDocumentListener = null;
    }
    if (searchKeyListener != null && jSearchName != null) {
      jSearchName.removeKeyListener(searchKeyListener);
      searchKeyListener = null;
    }
    if (navigateKeyListener != null && jSearchName != null) {
      jSearchName.removeKeyListener(navigateKeyListener);
      navigateKeyListener = null;
    }
    if (updatable != null) {
      updatable = null;
    }
    super.closeDialog();
  }


  private void selectOrAddEmailAddress(String emailAddress) {
    boolean found = false;
    Enumeration enm = dualListBox.getDestinationElements();
    while (enm.hasMoreElements()) {
      Object o = enm.nextElement();
      if (o instanceof EmailAddressRecord && ((EmailAddressRecord) o).address.equalsIgnoreCase(emailAddress)) {
        found = true;
        break;
      }
    }
    if (!found) {
      // In case the elements is in the source list then remove it from there,
      // moving it to the destination list.
      dualListBox.moveToDestinationElements(new EmailAddressRecord[] { new EmailAddressRecord(emailAddress) });
      jOkButton.setDefaultCapable(true);
      RecipientsDialog.this.getRootPane().setDefaultButton(jOkButton);
    }
  }

  private boolean isStrInUserNameNumberFormat(String userNameAndNumber, Object[] returnBuffer) {
    boolean formatOk = false;
    String handle = null;
    String number = null;
    Long numberL = null;
    try {
      int startBracket = userNameAndNumber.lastIndexOf('(');
      int endBracket = userNameAndNumber.indexOf(')');
      if (startBracket < endBracket) {
        handle = userNameAndNumber.substring(0, startBracket).trim();
        number = userNameAndNumber.substring(startBracket+1, endBracket);
        numberL = Long.valueOf(number);
        formatOk = true;
      }
    } catch (Throwable t) {
    }
    if (formatOk && returnBuffer != null) {
      returnBuffer[0] = handle;
      returnBuffer[1] = numberL;
    }
    return formatOk;
  }

  private void selectOrAddUserRecord(String userNameAndNumber) {
    boolean found = false;
    Enumeration enm = dualListBox.getDestinationElements();
    while (enm.hasMoreElements()) {
      Object o = enm.nextElement();
      if (o instanceof UserRecord && ListRenderer.getRenderedText(o).equalsIgnoreCase(userNameAndNumber)) {
        found = true;
        break;
      }
    }
    if (!found) {
      // In case the elements is in the source list then remove it from there,
      // moving it to the destination list.
      UserRecord uRec = new UserRecord();
      Object[] returnBuffer = new Object[2];
      isStrInUserNameNumberFormat(userNameAndNumber, returnBuffer);
      uRec.handle = (String) returnBuffer[0];
      uRec.userId = (Long) returnBuffer[1];
      dualListBox.addDestinationElements(new UserRecord[] { uRec });
      jOkButton.setDefaultCapable(true);
      RecipientsDialog.this.getRootPane().setDefaultButton(jOkButton);
    }
  }

  private class AddInputActionListener implements ActionListener {
    public void actionPerformed (ActionEvent event) {
      String inputLine = jSearchName.getText().trim();
      initSelectedStrings(RecipientsDialog.this, inputLine, null);
    }
  }

  private class OKActionListener implements ActionListener {
    public void actionPerformed (ActionEvent event) {
      pressedOK();
    }
  }

  private void pressedOK() {
    isOKed = true;
    Object[] objs = dualListBox.getResult();
    selectedResults = (Record[]) ArrayUtils.gatherAllOfInstance(objs, Record.class);
    if (updatable != null) updatable.update(selectedResults);
    closeDialog();
  }

  private class OtherUsersActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      otherUsersAction();
    }
  }

  private void otherUsersAction() {
    // select other users
    String buttonName = com.CH_gui.lang.Lang.rb.getString("button_Select");
    UserSelectorDialog d = new UserSelectorDialog(RecipientsDialog.this, buttonName, jSearchName.getText().trim());
    d.registerForUpdates(new ListUpdatableI() {
      public void update(Object[] objects) {
        UserRecord[] userRecords = (UserRecord[]) objects;
        if (userRecords != null && userRecords.length > 0) {
          dualListBox.addDestinationElements(userRecords);
        }
      }
    });
  }

  private class CancelActionListener implements ActionListener {
    public void actionPerformed (ActionEvent event) {
      closeDialog();
    }
  }

  public boolean isOKed() {
    return isOKed;
  }


  private boolean searchStringChanged = false;
  private boolean searchStringChangeDispatching = false;
  private javax.swing.Timer searchStringTimer;

  private class SearchDocumentListener implements DocumentListener {
    private void searchRecipients(DocumentEvent documentEvent) {
      searchStringChanged = true;
      if (searchStringTimer == null) {
        searchStringTimer = new javax.swing.Timer(300, new ActionListener() {
          public void actionPerformed(ActionEvent actionEvent) {
            searchStringChanged = false;
            searchStringChangeDispatching = false;
            searchStringTimer.stop();

            String searchText = jSearchName.getText().trim();
            String search = ListUtils.getLastElement(searchText);
            boolean found = dualListBox.setSourceSearchString(search, search.length() > 0);
            if (found) {
              JButton jAdd = dualListBox.getAddButton();
              if (getRootPane().getDefaultButton() != jAdd)
                getRootPane().setDefaultButton(jAdd);
            }
            if (!found && searchText.length() > 0) {
              if (getRootPane().getDefaultButton() != jAddInput)
                getRootPane().setDefaultButton(jAddInput);
            }
            if (searchStringChanged) {
              searchStringChangeDispatching = true;
              searchStringTimer.restart();
            }
          }
        });
      }
      if (!searchStringChangeDispatching) {
        searchStringChangeDispatching = true;
        searchStringTimer.restart();
      }
    }
    public void changedUpdate(DocumentEvent e)  { searchRecipients(e); }
    public void insertUpdate(DocumentEvent e)   { searchRecipients(e); }
    public void removeUpdate(DocumentEvent e)   { searchRecipients(e); }
  }

  private class SearchKeyListener extends KeyAdapter {
    private boolean moveHighlightedAndClear() {
      boolean found = dualListBox.moveHighlightedToDestination();
      if (found) {
        String search = jSearchName.getText();
        jSearchName.setText(ListUtils.stripLastElement(search));
      }
      return found;
    }
    public void keyPressed(KeyEvent e) {
      if (e.getModifiers() == 0 && (e.getKeyCode() == KeyEvent.VK_ENTER)) {
        if (moveHighlightedAndClear())
          e.consume();
      }
    }
    public void keyReleased(KeyEvent e) {
      // this button works on delimited strings (with commas too)
      jAddInput.setEnabled(jSearchName.getText().length() > 0); 
    }
  }

  private class DualBoxChangeListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      setEnabledButtons();
      if (getRootPane().getDefaultButton() != jOkButton)
        getRootPane().setDefaultButton(jOkButton);
    }
  }

  /**
  * Check the input line for possibly valid email address, allow for different
  * encodings which may be compatible with InternetAddress constructor.
  */
  public static boolean isEmailLineValid(String addr) {
    StringTokenizer st = new StringTokenizer(addr, " ,;:[]{}<>\t\r\n");
    while (st.hasMoreTokens()) {
      if (EmailRecord.isEmailFormatValid(st.nextToken()))
        return true;
    }
    return false;
  }


  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "RecipientsDialog";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }

  /*************************************************************************
  *** O b j e c t s P r o v i d e r U p d a t e r I    interface methods ***
  *************************************************************************/

  public Object[] provide(Object args) {
    return selectedResults;
  }

  public Object[] provide(Object args, ListUpdatableI updatable) {
    if (this.updatable == null) {
      this.updatable = updatable;
      return selectedResults;
    } else {
      throw new IllegalStateException("Already registered updatable object.");
    }
  }

  public void registerForUpdates(ListUpdatableI updatable) {
    if (this.updatable == null) {
      this.updatable = updatable;
    } else {
      throw new IllegalStateException("Already registered for updates.");
    }
  }

  /*****************************************************
  *** D i s p o s a b l e O b j    interface methods ***
  *****************************************************/
  public void disposeObj() {
    if (provider != null) {
      provider.disposeObj();
      provider = null;
    }
  }

}
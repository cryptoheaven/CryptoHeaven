/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.dialog;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.CacheUsrUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.DefaultReplyRunner;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.SendMessageRunner;
import com.CH_cl.service.records.filters.SubUserFilter;
import com.CH_co.cryptx.BAEncodedPassword;
import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.cryptx.RSAKeyPair;
import com.CH_co.cryptx.RSAKeyPairGenerator;
import com.CH_co.monitor.Interrupter;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.cnt.Cnt_NewCnt_Rq;
import com.CH_co.service.msg.dataSets.key.Key_KeyRecov_Co;
import com.CH_co.service.msg.dataSets.msg.Msg_New_Rq;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.service.msg.dataSets.usr.Usr_GetSubAcc_Rp;
import com.CH_co.service.msg.dataSets.usr.Usr_NewUsr_Rq;
import com.CH_co.service.records.*;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;
import com.CH_gui.frame.LoginFrame;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.gui.*;
import com.CH_gui.usrs.AccountOptionPermitChecks;
import com.CH_gui.util.MessageDialog;
import com.CH_gui.util.MiscGui;
import com.CH_guiLib.gui.JMyTextField;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.16 $</b>
*
* @author  Marcin Kurzawa
*/
public class CreateSubAccountsWizardDialog extends WizardDialog implements Interrupter {

  private static final int PAGE_ACCOUNTS = 0;
  private static final int PAGE_SUMMARY = 3;

  // vector filled with UserRecord objects
  private Vector accountsV = new Vector();

  private AccountOptionPermitChecks checks;

  // account page
  private JPanel jAccounts;
  private Vector jUsernamesV = new Vector();
  private Vector jPasswordsV = new Vector();
  private Vector jEmailV = new Vector();
  private JButton jAddMore;
  private JLabel jAccountsNote;
  private JLabel jSummaryLabel;
  private JLabel jAdvancedLabel;
  private JButton jAdvancedButton;
  private JCheckBox jCheckContactsForUsers;
  private JCheckBox jCheckContactsToMe;
  private JLabel jExpectedTime;
  private JProgressBar jProgressBar;
  private int progressValue;

  private final Object estimateMonitor = new Object();
  private Integer estimatedTime = null;

  private ServerInterfaceLayer SIL;
  private FetchedDataCache cache;
  private UserRecord myUser;
  private UserRecord userRecord;

  private boolean interrupted = false;

  private int keyLength = KeyRecord.KEY_LENGTH_DEFAULT;
  private int certainty = KeyRecord.CERTAINTY_DEFAULT;


  /** Creates new CreateSubAccountsWizardDialog */
  public CreateSubAccountsWizardDialog(Frame parent) {
    super(parent, com.CH_cl.lang.Lang.rb.getString("title_Account_Management"));
    init();
  }
  public CreateSubAccountsWizardDialog(Dialog parent) {
    super(parent, com.CH_cl.lang.Lang.rb.getString("title_Account_Management"));
    init();
  }

  private void init() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CreateSubAccountsWizardDialog.class, "init()");

    this.SIL = MainFrame.getServerInterfaceLayer();
    this.cache = SIL.getFetchedDataCache();
    this.myUser = cache.getUserRecord();

    // Set default PERMITS into a sample user record which we are creating...
    this.userRecord = UserRecord.getDefaultUserSettings(UserRecord.STATUS_BUSINESS_SUB);
    // skip NOTIFY_EMAIL_YES because it needs a contact email address too
    userRecord.notifyByEmail = new Short((short) Misc.setBit(false, userRecord.notifyByEmail, UserRecord.EMAIL_NOTIFY_YES));
    UserRecord.trimChildToParent(userRecord, myUser);

    super.initialize();

    if (trace != null) trace.exit(CreateSubAccountsWizardDialog.class);
  }


  /**
  * Overwrite to return tab names for the pages in wizard.
  * @return names for tabs
  */
  public String[] getWizardTabNames() {
    return new String[] { com.CH_cl.lang.Lang.rb.getString("tab_User_Accounts"),
                          com.CH_cl.lang.Lang.rb.getString("tab_Options"),
                          com.CH_cl.lang.Lang.rb.getString("tab_Permissions"),
                          com.CH_cl.lang.Lang.rb.getString("tab_Summary") };
  }
  /**
  * Overwrite to return panels for the pages in wizard
  * Makes panels to be displayed on wizard pages.
  */
  public JComponent[] createWizardPages() {
    checks = new AccountOptionPermitChecks();
    checks.isUpdatePermitsMode = true;

    ChangeListener checkBoxListener = new ChangeListener() {
      public void stateChanged(ChangeEvent event) {
        setEnabledButtons();
      }
    };

    JScrollPane jAccountsScrollPane = new JScrollPane();
    jAccountsScrollPane.setViewport(new JBottomStickViewport());
    jAccountsScrollPane.setViewportView(createAccountsPanel());
    JPanel optionsPanel = checks.createOptionsPanel(checkBoxListener, myUser, new UserRecord[] { userRecord });
    JPanel permitsPanel = checks.createPermissionsPanel(checkBoxListener, myUser, new UserRecord[] { userRecord });
    return new JComponent[] {
      jAccountsScrollPane,
      MiscGui.isSmallScreen() ? (JComponent) new JScrollPane(optionsPanel) : (JComponent) optionsPanel,
      MiscGui.isSmallScreen() ? (JComponent) new JScrollPane(permitsPanel) : (JComponent) permitsPanel,
      new JScrollPane(createSummaryPanel())
    };
  }

  /**
  * Overwrite to check if tab is ready to be left for another tab.
  * Informs the wizard that tab is about to change from the current one.
  * @returns fales if choices on current tab are invalid or incomplete
  */
  public boolean goFromTab(int tabIndex) {
    boolean rc = true;
    if (tabIndex == PAGE_ACCOUNTS) {
      rc = gatherAccounts(accountsV);
      synchronized (estimateMonitor) {
        estimatedTime = null;
      }
    }
    return rc;
  }
  /**
  * Overwrite to be informed when new tab is about to become visible.
  */
  public void goToTab(int tabIndex) {
    if (tabIndex == PAGE_SUMMARY) {
      // prepare the summary page
      StringBuffer textBuf = new StringBuffer("<html>You have chosen to create <b>");
      textBuf.append(accountsV.size());
      textBuf.append("</b> new accounts. ");
      if (accountsV.size() == 0) {
        textBuf.append("<p>Please go back and enter user accounts.");
      } else {
        updateKeyGenerationTimeThreaded();
        textBuf.append("<br>The accounts you are about to create are: <br><blockquote>");
        for (int i=0; i<accountsV.size(); i++) {
          UserAccount uAcc = (UserAccount) accountsV.elementAt(i);
          String emlS = uAcc.emailAddress.indexOf('@') > 0 ? "email address" : "email nickname";
          textBuf.append("  login name: \"");
          textBuf.append(uAcc.handle);
          textBuf.append("\", ");
          textBuf.append(emlS);
          textBuf.append(": \"");
          textBuf.append(uAcc.emailAddress);
          textBuf.append("\"<br>");
        }
        textBuf.append("</blockquote>");
        textBuf.append("After the new user accounts are created successfuly <br>");
        textBuf.append("a reference email will be sent to your 'Inbox' folder <br>");
        textBuf.append("with the account names and chosen passwords.");
      }
      jSummaryLabel.setText(textBuf.toString());
      jAdvancedLabel.setVisible(accountsV.size() > 0);
      jAdvancedButton.setVisible(accountsV.size() > 0);
      jCheckContactsForUsers.setVisible(accountsV.size() > 1);
      jCheckContactsToMe.setVisible(accountsV.size() > 0);
      validate();
    }
  }

  /**
  * Overwrite if you need to interrupt actions when Cancel button is pressed.
  * This action should interrupt thread inside the finishTaskRunner() if any.
  */
  public void setInterruptProgress(boolean interrupt) {
    interrupted = interrupt;
  }
  /**
  * Method of Interrupter interface.
  */
  public boolean isInterrupted() {
    return interrupted;
  }

  /**
  * Overwrite to specify when Finish button is eligible to become enabled.
  * @return true iff Finish button should enable and all required inputs are satisfied.
  */
  public boolean isFinishActionReady() {
    return accountsV.size() > 0;
  }

  /**
  * Overwrite if you need to set enablement of components during Finish action.
  * Invoked to disable inputs when finishTaskRunner() is in progress,
  * or enable inputs when it is done running.
  */
  public void setEnabledInputComponents(boolean enable) {
    jCheckContactsForUsers.setEnabled(enable);
    jCheckContactsToMe.setEnabled(enable);
  }

  /**
  * Overwrite to specify action which needs to be taken when Finish button is pressed.
  * Invoked by FinishThread to do the main action of the wizard.
  * @return true iff success
  */
  public boolean finishTaskRunner() {
    // error flag for running this task
    boolean error = false;

    // check availability of chosen usernames
    if (!interrupted && !error) {
      Obj_List_Co set = new Obj_List_Co();
      set.objs = new Object[2];
      Vector handlesV = new Vector();
      Vector passHashesV = new Vector();
      for (int i=0; i<accountsV.size(); i++) {
        UserAccount uAcc = (UserAccount) accountsV.elementAt(i);
        handlesV.addElement(uAcc.handle);
        passHashesV.addElement(getBAEncodedPassword(uAcc.password, uAcc.handle).getHashValue());
      }
      String[] handles = new String[handlesV.size()];
      handlesV.toArray(handles);
      Long[] passHashes = new Long[passHashesV.size()];
      passHashesV.toArray(passHashes);
      set.objs[0] = handles;
      set.objs[1] = passHashes;
      ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.USR_Q_CHECK_AVAIL, set), 60000);
      DefaultReplyRunner.nonThreadedRun(SIL, msgAction);

      if (msgAction == null || msgAction.getActionCode() < 0) {
        error = true;
      }
    }

    // check availability of chosen email addresses
    if (!interrupted && !error) {
      Obj_List_Co set = new Obj_List_Co();
      set.objs = new Object[2];
      Vector emailAddrsV = new Vector();
      for (int i=0; i<accountsV.size(); i++) {
        UserAccount uAcc = (UserAccount) accountsV.elementAt(i);
        emailAddrsV.addElement(uAcc.emailAddress);
      }
      String[] emailAddrs = new String[emailAddrsV.size()];
      emailAddrsV.toArray(emailAddrs);
      set.objs[0] = emailAddrs;
      set.objs[1] = Boolean.TRUE;
      ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.EML_Q_CHECK_AVAIL, set), 60000);
      DefaultReplyRunner.nonThreadedRun(SIL, msgAction);

      if (msgAction == null || msgAction.getActionCode() < 0) {
        error = true;
      }
    }

    // continue with generating keys for new accounts and sending CREATE requests
    if (!interrupted && !error) {
      jProgressBar.setMinimum(0);
      jProgressBar.setVisible(true);
      validate();

      // start the progress bar
      final int estSeconds = estimateKeyGenerationTimeNow(keyLength, certainty) * accountsV.size();
      jProgressBar.setMaximum(estSeconds);
      jProgressBar.setValue(0);
      javax.swing.Timer timer = new javax.swing.Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          progressValue ++;
          if (progressValue < estSeconds) {
            jProgressBar.setValue(progressValue);
          }
        }
      });
      timer.start();


      // DO ACTION
      // gather final options
      Short spam = checks.getNewSpamSetting(userRecord);
      Short notify = checks.getNewNotifySetting(userRecord);
      Long flags = checks.getNewFlagSetting(userRecord);
      // Create new user requests for all accounts...
      Vector newUsrRequestsV = new Vector();
      for (int i=0; i<accountsV.size(); i++) {
        UserAccount uAcc = (UserAccount) accountsV.elementAt(i);
        BAEncodedPassword ba = getBAEncodedPassword(uAcc.password, uAcc.handle);

        // exit if action is cancelled
        if (interrupted)
          break;

        RSAKeyPair keyPair = RSAKeyPairGenerator.generateKeyPair(keyLength, certainty, this);
        Usr_NewUsr_Rq request = LoginFrame.createNewUserRequest(keyPair, uAcc.handle, ba.getHashValue(), uAcc.emailAddress, null, spam, notify, flags, ba);
        request.userRecord.maxSubAccounts = new Short(UserRecord.UNLIMITED_AMOUNT);
        newUsrRequestsV.addElement(request);
      }


      // last chance to quit if cancelled, once we send request, the action has to complete including contact creation...
      if (!interrupted && newUsrRequestsV.size() > 0) {
        Obj_List_Co set = new Obj_List_Co();
        set.objs = new Usr_NewUsr_Rq[newUsrRequestsV.size()];
        newUsrRequestsV.toArray(set.objs);
        ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.USR_Q_NEW_SUB, set), 120000);
        DefaultReplyRunner.nonThreadedRun(SIL, msgAction);

        if (msgAction == null || msgAction.getActionCode() < 0) {
          error = true;
        } else {

          Usr_GetSubAcc_Rp reply = (Usr_GetSubAcc_Rp) msgAction.getMsgDataSet();
          FolderShareRecord myCntFld = cache.getFolderShareRecordMy(cache.getUserRecord().contactFolderId, false);
          KeyRecord masterKey = cache.getKeyRecordMyCurrent();

          // <<<<
          // 1) Create new contact requests for all accounts so they all become connected with me and each other...
          // 2) Create new Password Reset and Key Recovery records if requested
          Vector newCntRequestsV = new Vector();
          Vector newPassResetRecordsV = new Vector();
          for (int i=0; i<newUsrRequestsV.size(); i++) {
            Usr_NewUsr_Rq usrReq1 = (Usr_NewUsr_Rq) newUsrRequestsV.elementAt(i);
            UserRecord newUsr1 = findUserRec(usrReq1.userRecord.handle, usrReq1.userRecord.passwordHash, reply.userRecords);
            usrReq1.userRecord.merge(newUsr1);
            newUsr1 = usrReq1.userRecord;
            // Create Password Reset and Key Recovery request ...
            if (Misc.isBitSet(newUsr1.flags, UserRecord.FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY)) {
              // if the account was created successfully, it will have the currentKeyId filled in
              if (newUsr1.currentKeyId != null) {
                KeyRecoveryRecord recoveryRecord = new KeyRecoveryRecord();
                recoveryRecord.seal(masterKey, newUsr1.currentKeyId, usrReq1.keyRecord.getPrivateKey(), new BASymmetricKey(32));
                newPassResetRecordsV.addElement(recoveryRecord);
              }
            }
            // Create contact request with me...
            if (jCheckContactsToMe.isSelected()) {
              newCntRequestsV.addElement(createNewContactRequest(myUser.userId, newUsr1.userId, myUser.userId, newUsr1.handle, myUser.handle, myCntFld.getSymmetricKey(), newUsr1.getSymKeyCntNotes()));
              newCntRequestsV.addElement(createNewContactRequest(newUsr1.userId, myUser.userId, myUser.userId, myUser.handle, newUsr1.handle, usrReq1.contactShareRecord.getSymmetricKey(), myUser.getSymKeyCntNotes()));
            }
            // Create contact request with all other ...
            for (int k=i+1; k<newUsrRequestsV.size(); k++) {
              Usr_NewUsr_Rq usrReq2 = (Usr_NewUsr_Rq) newUsrRequestsV.elementAt(k);
              UserRecord newUsr2 = findUserRec(usrReq2.userRecord.handle, usrReq2.userRecord.passwordHash, reply.userRecords);
              usrReq2.userRecord.merge(newUsr2);
              newUsr2 = usrReq2.userRecord;
              // Create contact request between users ...
              if (jCheckContactsForUsers.isSelected()) {
                newCntRequestsV.addElement(createNewContactRequest(newUsr1.userId, newUsr2.userId, myUser.userId, newUsr2.handle, newUsr1.handle, usrReq1.contactShareRecord.getSymmetricKey(), newUsr2.getSymKeyCntNotes()));
                newCntRequestsV.addElement(createNewContactRequest(newUsr2.userId, newUsr1.userId, myUser.userId, newUsr1.handle, newUsr2.handle, usrReq2.contactShareRecord.getSymmetricKey(), newUsr1.getSymKeyCntNotes()));
              }
            }
          }
          if (newCntRequestsV.size() > 0) {
            Cnt_NewCnt_Rq[] cntRequests = new Cnt_NewCnt_Rq[newCntRequestsV.size()];
            newCntRequestsV.toArray(cntRequests);
            Obj_List_Co reqSet = new Obj_List_Co();
            reqSet.objs = cntRequests;
            SIL.submitAndReturn(new MessageAction(CommandCodes.CNT_Q_NEW_SUB_CONTACTS, reqSet));
          }
          if (newPassResetRecordsV.size() > 0) {
            KeyRecoveryRecord[] passResetRecords = new KeyRecoveryRecord[newPassResetRecordsV.size()];
            newPassResetRecordsV.toArray(passResetRecords);
            SIL.submitAndReturn(new MessageAction(CommandCodes.KEY_Q_SET_KEY_RECOVERY, new Key_KeyRecov_Co(passResetRecords)));
          }
          // >>>>

          // send summary message
          UserRecord[] usrRecs = reply.userRecords;
          StringBuffer bodyBuf = new StringBuffer("For your reference, here is a summary of your newly created user accounts:\n\n");
          for (int i=0; i<usrRecs.length; i++) {
            UserRecord uRec = usrRecs[i];
            String handle = uRec.handle;
            UserAccount uAcc = null;
            // find UserAccount for this handle.... in case of the same handles, they should be in order
            for (int k=0; k<accountsV.size(); k++) {
              UserAccount acc = (UserAccount) accountsV.elementAt(k);
              if (handle.equals(acc.handle)) {
                uAcc = acc;
                accountsV.removeElementAt(k);
                break;
              }
            }
            String[] emailStrings = CacheUsrUtils.getCachedDefaultEmail(uRec, false);
            String emailAddress = emailStrings != null ? emailStrings[2] : "N/A";
            bodyBuf.append("  login name: \"");
            bodyBuf.append(handle);
            if (uAcc != null) {
              bodyBuf.append("\", password: \"");
              bodyBuf.append(uAcc.password);
            }
            bodyBuf.append("\", email address: \"");
            bodyBuf.append(emailAddress);
            bodyBuf.append("\", user ID: \"");
            bodyBuf.append(uRec.userId);
            bodyBuf.append("\"\n");
          }
          sendMessageToSelf(SIL, "User Account creation summary", bodyBuf.toString());
        }
      }


      timer.stop();
    }

    if (error)
      jProgressBar.setVisible(false);

    return !error;
  }


  private JPanel createAccountsPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());

    jAccounts = new JPanel();
    jAccounts.setLayout(new GridBagLayout());

    int posY = 0;
    jAccounts.add(new JMyLabel("Username"), new GridBagConstraints(0, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jAccounts.add(new JMyLabel("Password"), new GridBagConstraints(1, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jAccounts.add(new JMyLabel("Email Address"), new GridBagConstraints(2, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    jAddMore = new JMyButton("Add More");
    jAddMore.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        addAccountFields();
      }
    });
    jAccounts.add(jAddMore, new GridBagConstraints(0, posY, 1, 1, 10, 0,
      GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    jAccountsNote = new JMyLabel(
        "<html>Note: Email Address is optional.  When email address is not <br>" +
        "specified or only the nickname without domain is specified, <br>" +
        "the system will try to assign a default email address in the <br>" +
        "form of:  nickname@yourDomain.com");
    jAccounts.add(jAccountsNote, new GridBagConstraints(0, posY, 1, 3, 10, 0,
      GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    for (int i=0; i<3; i++) {
      addAccountFields();
      posY ++;
    }

    panel.add(jAccounts, BorderLayout.NORTH);
    return panel;
  }


  private void addAccountFields() {
    jAccounts.remove(jAddMore);
    jAccounts.remove(jAccountsNote);

    jUsernamesV.addElement(new JMyTextField());
    jPasswordsV.addElement(new JMyTextField());
    jEmailV.addElement(new JMyTextField());

    // focus on the first username field
    if (jUsernamesV.size() == 1) {
      ((JTextField) jUsernamesV.elementAt(0)).addHierarchyListener(new InitialFocusRequestor());
    }

    int posY = jUsernamesV.size();

    jAccounts.add((JComponent) jUsernamesV.lastElement(), new GridBagConstraints(0, posY, 1, 1, 10, 0,
      GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jAccounts.add((JComponent) jPasswordsV.lastElement(), new GridBagConstraints(1, posY, 1, 1, 10, 0,
      GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jAccounts.add((JComponent) jEmailV.lastElement(), new GridBagConstraints(2, posY, 1, 1, 10, 0,
      GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    jAccounts.add(jAddMore, new GridBagConstraints(0, posY+1, 1, 1, 10, 0,
      GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    jAccounts.add(jAccountsNote, new GridBagConstraints(0, posY+2, 3, 1, 10, 0,
      GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    jAccounts.revalidate();
    jAccounts.repaint();
  }


  private JPanel createSummaryPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    jSummaryLabel = new JMyLabel("<html>Users to create:</html>");
    jAdvancedLabel = new JMyLabel("Advanced Options:");
    jAdvancedButton = new JMyButton("Customize...");
    jAdvancedButton.addActionListener(new AdvancedListener());
    jCheckContactsForUsers = new JMyCheckBox("Create contacts between above listed users.", true);
    jCheckContactsToMe = new JMyCheckBox("Create contacts between myself and new users.", true);
    jCheckContactsForUsers.setVisible(false);
    jCheckContactsToMe.setVisible(false);
    jExpectedTime = new JMyLabel();
    jProgressBar = new JProgressBar();
    jProgressBar.setVisible(false);

    int posY = 0;

    panel.add(jSummaryLabel, new GridBagConstraints(0, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(jAdvancedLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jAdvancedButton, new GridBagConstraints(1, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(jCheckContactsForUsers, new GridBagConstraints(0, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;

    panel.add(jCheckContactsToMe, new GridBagConstraints(0, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(4, 5, 4, 5), 0, 0));
    posY ++;

    panel.add(jExpectedTime, new GridBagConstraints(0, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(jProgressBar, new GridBagConstraints(0, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    // filler
    panel.add(new JMyLabel(), new GridBagConstraints(0, posY, 2, 1, 10, 10,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0,0,0,0), 0, 0));

    return panel;
  }


  /**
  * @return true if successful, false if error.
  */
  private boolean gatherAccounts(Vector accountsV) {
    // start fresh
    accountsV.clear();
    for (int i=0; i<jUsernamesV.size(); i++) {
      String uName = ((JTextField) jUsernamesV.elementAt(i)).getText().trim();
      String pass = ((JTextField) jPasswordsV.elementAt(i)).getText().trim();
      String emailAddress = ((JTextField) jEmailV.elementAt(i)).getText().trim();
      if (emailAddress == null || emailAddress.length() == 0) {
        emailAddress = uName;
      }
      // add new account to vector
      if (uName.length() > 0) {
        // validate requested email address
        String emailAddr = "";
        if (emailAddress.indexOf('@') < 0) {
          emailAddr = EmailRecord.validateNickName(emailAddress);
        } else {
          emailAddr = EmailRecord.validateEmailAddress(emailAddress);
        }
        accountsV.addElement(new UserAccount(uName, pass, emailAddr));
      }
    }

    // all accounts gathered, now do some checking
    boolean isValid = true;
    StringBuffer sb = new StringBuffer();
    // check if all accounts data is valid
    for (int i=0; i<accountsV.size(); i++) {
      UserAccount subAcc = (UserAccount) accountsV.elementAt(i);
      // check for duplicate emails
      String email = subAcc.emailAddress;
      if (email.length() > 0) {
        for (int k=i+1; k<accountsV.size(); k++) {
          UserAccount ua = (UserAccount) accountsV.elementAt(k);
          if (subAcc.handle.equalsIgnoreCase(ua.handle)) {
            sb.append("All Usernames should be unique.\n");
            break;
          } else if (email.equalsIgnoreCase(ua.emailAddress)) {
            sb.append("All email addresses should be unique.\n");
            break;
          }
        }
      }
      if (subAcc.handle == null || subAcc.handle.length() == 0) {
        sb.append("Please specify usernames for all new accounts.\n");
      }
      if (subAcc.password == null) {
        sb.append("Please specify passwords for all new accounts.\n");
      } else if (subAcc.password.length() < 6) {
        sb.append("Minimum password length for user accounts is 6 characters.\n");
      }
      if (sb.length() > 0) {
        isValid = false;
        break;
      }
    }
    // Check if you are not creating more accounts than your master account can handle
    //myUser.
    UserRecord[] myCurrentSubUsers = (UserRecord[]) RecordUtils.filter(cache.getUserRecords(), new SubUserFilter(myUser.userId, false, true));
    if (myCurrentSubUsers != null && myCurrentSubUsers.length + accountsV.size() > myUser.maxSubAccounts.shortValue()) {
      isValid = false;
      sb.append("Your subscription level is for "+(myUser.maxSubAccounts.shortValue()+1)+" user accounts in total, 1 administrative and "+myUser.maxSubAccounts+" managed.  Creation of all specified user accounts would exceed your quota.  To enable management of a larger group please upgrade your account.");
    }

    if (!isValid) {
      MessageDialog.showWarningDialog(CreateSubAccountsWizardDialog.this, sb.toString(), com.CH_cl.lang.Lang.rb.getString("title_Invalid_Input"));
    }
    return isValid;
  }


  /* @return encoded password entered by the user */
  private BAEncodedPassword getBAEncodedPassword(String password, String userName) {
    return UserRecord.getBAEncodedPassword(password.toCharArray(), userName);
  }


  private static UserRecord findUserRec(String handle, Long passwordHash, UserRecord[] fromUserRecords) {
    UserRecord uRec = null;
    if (fromUserRecords != null) {
      for (int i=0; i<fromUserRecords.length; i++) {
        UserRecord u = fromUserRecords[i];
        if (handle.equals(u.handle) && passwordHash.equals(u.passwordHash)) {
          uRec = u;
          break;
        }
      }
    }
    return uRec;
  }


  /**
  * <shareId>   <ownerUserId> <contactWithId> <encOwnerNote> <otherKeyId> <encOtherSymKey> <encOtherNote>
  */
  private static Cnt_NewCnt_Rq createNewContactRequest(Long ownerUserId, Long contactWithId, Long creatorId, String ownerNote, String otherNote, BASymmetricKey owner_folderSymKey, BASymmetricKey contactWith_cntSymKey) {
    Cnt_NewCnt_Rq request = new Cnt_NewCnt_Rq();
    request.contactRecord = new ContactRecord();
    request.contactRecord.ownerUserId = ownerUserId;
    request.contactRecord.contactWithId = contactWithId;
    request.contactRecord.creatorId = creatorId;
    request.contactRecord.setOwnerNote(ownerNote);
    request.contactRecord.setOtherNote(otherNote);
    request.contactRecord.sealGivenContact(owner_folderSymKey, contactWith_cntSymKey);
    return request;
  }


  private static void sendMessageToSelf(ServerInterfaceLayer SIL, String subject, String body) {
    BASymmetricKey ba = new BASymmetricKey(32);
    MsgLinkRecord[] linkRecords = SendMessageRunner.prepareMsgLinkRecords(SIL, new UserRecord[] { SIL.getFetchedDataCache().getUserRecord() }, ba);
    MsgDataRecord dataRecord = SendMessageRunner.prepareMsgDataRecord(ba, new Short(MsgDataRecord.IMPORTANCE_HIGH_PLAIN), subject, body, null);
    Msg_New_Rq newMsgRequest = new Msg_New_Rq(null, null, null, linkRecords, dataRecord, null, null, null, null);
    MessageAction requestMessageAction = new MessageAction(CommandCodes.MSG_Q_NEW, newMsgRequest);
    SIL.submitAndReturn(requestMessageAction);
  }


  private static class UserAccount {
    private String handle;
    private String password;
    private String emailAddress;
    private UserAccount(String handle, String password, String emailAddress) {
      this.handle = handle;
      this.password = password;
      this.emailAddress = emailAddress;
    }
  }


  private class AdvancedListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      KeyGenerationOptionsDialog d = new KeyGenerationOptionsDialog(CreateSubAccountsWizardDialog.this, keyLength, certainty, null);
      if (d.isOK()) {
        if (keyLength != d.getKeyLength() || certainty != d.getCertainty()) {
          keyLength = d.getKeyLength();
          certainty = d.getCertainty();
          // reset the old estimate
          synchronized (estimateMonitor) {
            estimatedTime = null;
          }
          updateKeyGenerationTimeThreaded();
        }
      }
    }
  }

  private void updateKeyGenerationTimeThreaded() {
    Thread th = new ThreadTraced("KeyGenerationTimeEstimator") {
      public void runTraced() {
        synchronized (estimateMonitor) {
          if (estimatedTime == null) {
            jExpectedTime.setText(com.CH_cl.lang.Lang.rb.getString("label_Estimating_Key_Generation_time..."));
            validate();
            estimatedTime = new Integer(estimateKeyGenerationTimeNow(keyLength, certainty)+1);
            jExpectedTime.setText(java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("label_Key_Generation_will_take_approximately_###_seconds."), new Object[] { estimatedTime }));
            validate();
          }
        }
      }
    };
    th.setDaemon(true);
    th.start();
  }

  private int estimateKeyGenerationTimeNow(int keyLength, int certainty) {
    synchronized (estimateMonitor) {
      if (estimatedTime == null) {
        estimatedTime = new Integer(RSAKeyPairGenerator.estimateGenerationTime(keyLength, certainty) * accountsV.size());
      }
      return estimatedTime.intValue();
    }
  }

}
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

package com.CH_gui.dialog;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.ops.*;
import com.CH_cl.service.records.*;
import com.CH_cl.util.GlobalSubProperties;

import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.gui.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.msg.dataSets.usr.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.frame.*;
import com.CH_gui.gui.*;
import com.CH_guiLib.gui.*;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.msgs.MsgPanelUtils;
import com.CH_gui.usrs.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

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
 * <b>$Revision: 1.50 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class AccountOptionsDialog extends GeneralDialog {

  private static final int DEFAULT_OK_INDEX = 0;
  private static final int DEFAULT_CANCEL_INDEX = 1;

  private JMyTextOptionField jDefaultEmail;
  private JMyLabel jEncryption;
  private JTextField jContactEmail;
  private JTextField jActivationCode;

  private JCheckBox jEnableSound;
  private JCheckBox jEnableAntialiasing;
  private JCheckBox jResetLocalSettings;

  private JCheckBox jIncludeChangesToAccounts;

  private AccountOptionPermitChecks checks;
  private AccountOptionsSignaturesPanel jPanelSignatures;
  private AccountOptionsResponderPanel jPanelResponder;
  private AccountOptionsQuotasPanel jPanelQuotas;

  private boolean isMyKeyLocal;
  private boolean isSoundEnabled;
  private boolean isAntialiasingEnabled;

  private JButton jOk;

  private static String FETCHING_DATA = com.CH_gui.lang.Lang.rb.getString("Fetching_Data...");

  private DocumentChangeListener documentChangeListener;

  private String defaultEmail;

  private FetchedDataCache cache;
  private ServerInterfaceLayer SIL;

  private UserRecord[] userRecords;
  private UserRecord myUserRecord;
  private boolean isChangingMyUserRecord;
  private boolean isUpdatePermitsMode;

  private int maxDialogWidth = 435;

  private Component parent;

  /** Creates new AccountOptionsDialog */
  public AccountOptionsDialog(Frame parent) {
    this(parent, (UserRecord[]) null);
  }
  public AccountOptionsDialog(Frame parent, UserRecord userRec) {
    this(parent, new UserRecord[] { userRec });
  }
  public AccountOptionsDialog(Frame parent, UserRecord[] userRecs) {
    super(parent, com.CH_gui.lang.Lang.rb.getString("title_Account_Options"));
    initialize(parent, userRecs);
  }
  /** Creates new AccountOptionsDialog */
  public AccountOptionsDialog(Dialog parent) {
    this(parent, (UserRecord[]) null);
  }
  public AccountOptionsDialog(Dialog parent, UserRecord userRec) {
    this(parent, new UserRecord[] { userRec });
  }
  public AccountOptionsDialog(Dialog parent, UserRecord[] userRecs) {
    super(parent, com.CH_gui.lang.Lang.rb.getString("title_Account_Options"));
    initialize(parent, userRecs);
  }

  private void initialize(Component parent, UserRecord[] userRecs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AccountOptionsDialog.class, "init()");

    SIL = MainFrame.getServerInterfaceLayer();
    cache = SIL.getFetchedDataCache();
    this.myUserRecord = cache.getUserRecord();
    this.parent = parent;
    if (userRecs != null) {
      this.userRecords = userRecs;
    } else {
      this.userRecords = new UserRecord[] { myUserRecord };
    }

    if (userRecords.length == 1)
      setTitle(getTitle() + " : " + userRecords[0].shortInfo());

    this.checks = new AccountOptionPermitChecks();
    this.isChangingMyUserRecord = userRecords.length == 1 && userRecords[0].userId.equals(cache.getMyUserId());
    this.isUpdatePermitsMode = !isChangingMyUserRecord;

    checks.isUpdatePermitsMode = isUpdatePermitsMode;

    JButton[] buttons = createButtons();
    JPanel pane = createMainPanel(myUserRecord, userRecords);

    if (isChangingMyUserRecord) {
      isMyKeyLocal = !Misc.isBitSet(cache.getUserRecord().flags, UserRecord.FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER);
      //isMyKeyLocal = KeyOps.isKeyStoredLocally(cache.getKeyRecordMyCurrent().keyId);
    }

    if (maxDialogWidth > 0) {
      pane.setMaximumSize(new Dimension(maxDialogWidth, 1000));
      pane.setPreferredSize(new Dimension(maxDialogWidth, pane.getPreferredSize().height+16));
    }

    super.init(parent, buttons, pane, new JMyLabel(Images.get(ImageNums.LOGO_BANNER_MAIN)), DEFAULT_OK_INDEX, DEFAULT_CANCEL_INDEX);

    setEnabledButtons();
    fetchData();

    if (trace != null) trace.exit(AccountOptionsDialog.class);
  }

  private void fetchData() {
    Thread th = new ThreadTraced("Account Options Quotas Fetcher") {
      private AutoResponderRecord autoResponderRecord = null;
      public void runTraced() {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "AccountOptionsDialog.fetchData.runTraced()");

        // fetch User's Encryption info
        if (userRecords.length == 1) {
          UserRecord user = cache.getUserRecord(userRecords[0].userId);
          KeyRecord key = cache.getKeyRecord(user.currentKeyId);
          if (key == null || key.plainPublicKey == null) {
            Obj_IDList_Co request = new Obj_IDList_Co();
            request.IDs = new Long[] { user.currentKeyId };
            MessageAction msgAction = new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_KEYIDS, request);
            SIL.submitAndWait(msgAction, 30000);
            key = cache.getKeyRecord(user.currentKeyId);
          }
          if (key != null) {
            jEncryption.setText(key.plainPublicKey.shortInfo() + "/" + "AES(256)");
            jEncryption.setIcon(key.getIcon());
          }
        }

        // fetch a single sub user account info
        {
          Obj_List_Co request = new Obj_List_Co();
          request.objs = new Object[] { null, null, Boolean.TRUE, userRecords.length == 1 ? userRecords[0].userId : null, userRecords.length == 1 ? Boolean.TRUE : Boolean.FALSE };
          if (trace != null) trace.data(10, "about to get responder");
          ClientMessageAction reply = SIL.submitAndFetchReply(new MessageAction(CommandCodes.USR_Q_GET_SUB_ACCOUNTS, request), 60000);
          if (trace != null) trace.data(11, "about to run reply", reply);
          if (reply != null) {
            DefaultReplyRunner.nonThreadedRun(SIL, reply);
            ProtocolMsgDataSet set = reply.getMsgDataSet();
            if (set instanceof Usr_GetSubAcc_Rp) {
              Usr_GetSubAcc_Rp set2 = (Usr_GetSubAcc_Rp) set;
              if (trace != null) trace.data(12, "about to set responder record");
              if (set2.autoResponderRecords != null && set2.autoResponderRecords.length == 1) {
                autoResponderRecord = set2.autoResponderRecords[0];
                autoResponderRecord.unSeal();
              }
              if (trace != null) trace.data(20, "responder record set", autoResponderRecord);
            }
          }
        }

        Long storageUsed = null;
        Long transferUsed = null;
        Short accountsUsed = null;

        if (userRecords.length == 1) {
          storageUsed = userRecords[0].storageUsed;
          transferUsed = userRecords[0].transferUsed;
        }

        // fetch cumulative usage for master accounts
        if (userRecords.length == 1 && userRecords[0].isCapableToManageUserAccounts()) {
          Obj_IDList_Co request = new Obj_IDList_Co();
          request.IDs = new Long[] { userRecords[0].userId };
          ClientMessageAction reply = SIL.submitAndFetchReply(new MessageAction(CommandCodes.USR_Q_CUMULATIVE_USAGE, request), 60000);
          if (reply != null) {
            DefaultReplyRunner.nonThreadedRun(SIL, reply);
            Obj_List_Co set = (Obj_List_Co) reply.getMsgDataSet();
            storageUsed = (Long) ((Object[]) set.objs[1])[0];
            transferUsed = (Long) ((Object[]) set.objs[2])[0];
            accountsUsed = (Short) ((Object[]) set.objs[3])[0];
          }
        }

        final Long storageUsedF = storageUsed;
        final Long transferUsedF = transferUsed;
        final Short accountsUsedF = accountsUsed;

        // Perform GUI updates in a GUI-safe-thread
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            // userRecord was updated with the fetched record and merged together
            if (jPanelQuotas.jStorageUsed != null) {
              if (storageUsedF != null) {
                jPanelQuotas.jStorageUsed.setText(Misc.getFormattedSize(storageUsedF, 4, 3));
              } else {
                jPanelQuotas.jStorageUsed.setText("");
              }
            }

            if (jPanelQuotas.jStorageCalcDate != null) {
              if (userRecords.length == 1 && userRecords[0].checkStorageDate != null) {
                jPanelQuotas.jStorageCalcDate.setText(Misc.getFormattedTimestamp(userRecords[0].checkStorageDate));
              } else {
                jPanelQuotas.jStorageCalcDate.setText("");
              }
            }

            if (jPanelQuotas.jBandwidthUsed != null) {
              if (transferUsedF != null) {
                jPanelQuotas.jBandwidthUsed.setText(Misc.getFormattedSize(transferUsedF, 4, 3));
              } else {
                jPanelQuotas.jBandwidthUsed.setText("");
              }
            }

            if (userRecords.length == 1 && userRecords[0].defaultEmlId.longValue() != UserRecord.GENERIC_EMAIL_ID) {
              EmailRecord emlRec = cache.getEmailRecord(userRecords[0].defaultEmlId);
              defaultEmail = emlRec.getEmailAddressFull().toLowerCase();
              jDefaultEmail.setText(emlRec.getEmailAddressFull());
              setEditableDefaultEmail(myUserRecord, emlRec);
            }

            if (jPanelQuotas.jAccountsUsed != null) {
              jPanelQuotas.jAccountsUsed.setText(""+accountsUsedF);
            }

            // update checkboxes
            checks.updateCheckBoxes(myUserRecord, userRecords);

            // update responder panel
            if (autoResponderRecord != null) {
              jPanelResponder.initializeData(userRecords.length == 1 ? userRecords[0].autoResp : null, autoResponderRecord);
            }

            UserOps.checkExpiry();
            UserOps.checkQuotas();

            // buttons enablement after fetch is done
            setEnabledButtons();
          }
        });

        if (trace != null) trace.exit(getClass());
      }
    };
    th.setDaemon(true);
    th.start();
  }


  private JButton[] createButtons() {
    JButton[] buttons = new JButton[2];

    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
    buttons[0].addActionListener(new OKActionListener());
    jOk = buttons[0];

    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[1].setDefaultCapable(true);
    buttons[1].addActionListener(new CancelActionListener());

    return buttons;
  }


  private JPanel createMainPanel(UserRecord myUserRec, UserRecord[] userRecs) {
    JTabbedPane pane = new JMyTabbedPane(JTabbedPane.TOP);

    documentChangeListener = new DocumentChangeListener();
    boolean includePricingInfo = isChangingMyUserRecord && !myUserRec.isBusinessSubAccount();

    JPanel panelAccount = null;
    if (userRecs.length == 1)
      panelAccount = createAccountPanel(myUserRec, userRecs[0]);
    else
      panelAccount = createAccountsPanel(myUserRec, userRecs);
    JPanel panelOptions = createOptionsPanel();
    JPanel panelPermissions = createPermissionsPanel();

    ChangeListener checkBoxListener = new ChangeListener() {
      public void stateChanged(ChangeEvent event) {
        setEnabledButtons();
      }
    };

    jPanelQuotas = new AccountOptionsQuotasPanel(checkBoxListener, userRecs, isChangingMyUserRecord, includePricingInfo);
    if (jPanelQuotas.jStorageLimit != null) {
      jPanelQuotas.jStorageLimit.getDocument().addDocumentListener(documentChangeListener);
    }
    if (jPanelQuotas.jBandwidthLimit != null) {
      jPanelQuotas.jBandwidthLimit.getDocument().addDocumentListener(documentChangeListener);
    }

    JComponent accountComp = MiscGui.isSmallScreen() ? (JComponent) new JScrollPane(panelAccount) : (JComponent) panelAccount;
    JComponent optionsComp = MiscGui.isSmallScreen() ? (JComponent) new JScrollPane(panelOptions) : (JComponent) panelOptions;
    JComponent permitsComp = MiscGui.isSmallScreen() ? (JComponent) new JScrollPane(panelPermissions) : (JComponent) panelPermissions;
    pane.addTab(com.CH_gui.lang.Lang.rb.getString("tab_Account"), accountComp);
    pane.addTab(com.CH_gui.lang.Lang.rb.getString("tab_Options"), optionsComp);
    pane.addTab(com.CH_gui.lang.Lang.rb.getString("tab_Permissions"), permitsComp);

    if (isChangingMyUserRecord) {
      UserSettingsRecord userSettingsRecord = cache.getMyUserSettingsRecord();
      if (userSettingsRecord != null) {
        jPanelSignatures = new AccountOptionsSignaturesPanel(userSettingsRecord);
      } else {
        jPanelSignatures = new AccountOptionsSignaturesPanel();
      }
      pane.addTab(com.CH_gui.lang.Lang.rb.getString("tab_Signatures"), jPanelSignatures);
      jPanelSignatures.addDocumentListener(documentChangeListener);
    }
    jPanelResponder = new AccountOptionsResponderPanel(userRecs, jPanelSignatures, checkBoxListener);
    jPanelResponder.addDocumentListener(documentChangeListener);
    JComponent responderComp = MiscGui.isSmallScreen() ? (JComponent) new JScrollPane(jPanelResponder) : (JComponent) jPanelResponder;
    pane.addTab(com.CH_gui.lang.Lang.rb.getString("tab_Auto-Responder"), responderComp);
    pane.addTab(com.CH_gui.lang.Lang.rb.getString("tab_Quotas"), jPanelQuotas);

    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(0, 0, 0, 0));
    panel.setLayout(new GridBagLayout());

    ImageIcon logoBanner = Images.get(ImageNums.LOGO_BANNER_MAIN);
    if (logoBanner != null)
      maxDialogWidth = logoBanner.getIconWidth();

//    panel.add(new JMyLabel(logoBanner), new GridBagConstraints(0, 0, 1, 1, 0, 0, 
//        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
    panel.add(pane, new GridBagConstraints(0, 1, 1, 1, 10, 10,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));

    return panel;
  }


  private JPanel createAccountPanel(UserRecord myUserRec, final UserRecord userRec) {
    JPanel panel = new JPanel();

    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    panel.setLayout(new GridBagLayout());

    int posY = 0;


    JPanel topPanel = new JPanel();
    topPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
    topPanel.setLayout(new GridBagLayout());

    topPanel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Account_Type")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    JLabel jAccountStatus = new JMyLabel(userRec.getAccountType());
    if (userRec.isHeld())
      jAccountStatus.setIcon(Images.get(ImageNums.WARNING16));
    topPanel.add(jAccountStatus, new GridBagConstraints(1, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    JLabel accNameLabel = new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Account_Name"));
    JLabel accName = new JMyLabel(ListRenderer.getRenderedText(userRec));
    topPanel.add(accNameLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    topPanel.add(new JMyLabel(ListRenderer.getRenderedIcon(userRec)), new GridBagConstraints(1, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 2), 0, 0));
    topPanel.add(accName, new GridBagConstraints(2, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 2, 5, 5), 0, 0));
    posY ++;

    JLabel emlAddrLabel = new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Email_Address"));
    EmailAddressRecord emlRec = new EmailAddressRecord("");
    defaultEmail = emlRec.address.toLowerCase();
    ActionListener emlAction = new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        Window w = SwingUtilities.windowForComponent(AccountOptionsDialog.this);
        if (w instanceof Dialog) new ManageEmailAddressesDialog((Dialog) w, userRec, AccountOptionsDialog.this);
        else if (w instanceof Frame) new ManageEmailAddressesDialog((Frame) w, userRec, AccountOptionsDialog.this);
      }
    };
    jDefaultEmail = new JMyTextOptionField(emlRec.address, new JMyDropdownIcon(), emlAction);
    if (userRec.defaultEmlId.longValue() != UserRecord.GENERIC_EMAIL_ID) {
      EmailRecord emlRecord = cache.getEmailRecord(userRec.defaultEmlId);
      if (emlRecord != null) {
        defaultEmail = emlRecord.getEmailAddressFull().toLowerCase();
        jDefaultEmail.setText(emlRecord.getEmailAddressFull());
        setEditableDefaultEmail(myUserRec, emlRecord);
      } else {
        jDefaultEmail.setText(FETCHING_DATA);
      }
    }
    jDefaultEmail.getDocument().addDocumentListener(documentChangeListener);
    JLabel jEmlLabel = new JMyLabel(emlRec.getIcon());
    topPanel.add(emlAddrLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    topPanel.add(jEmlLabel, new GridBagConstraints(1, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 2), 0, 0));
    topPanel.add(jDefaultEmail, new GridBagConstraints(2, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 2, 5, 5), 0, 0));
    posY ++;

    topPanel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Encryption")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    jEncryption = new JMyLabel("...");
    topPanel.add(jEncryption, new GridBagConstraints(1, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    topPanel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Last_Login_Date")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    String loginDate = Misc.getFormattedTimestamp(userRec.dateLastLogin);
    topPanel.add(new JMyLabel(loginDate), new GridBagConstraints(1, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

//    topPanel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Last_Logout_Date")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
//        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
//    String logoutDate = Misc.getFormattedTimestamp(userRec.dateLastLogout);
//    topPanel.add(new JMyLabel(logoutDate), new GridBagConstraints(1, posY, 2, 1, 10, 0,
//        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
//    posY ++;

    topPanel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Activation_Code")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    jActivationCode = new JMyTextField();
    jActivationCode.getDocument().addDocumentListener(documentChangeListener);
    topPanel.add(jActivationCode, new GridBagConstraints(1, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    ChangeListener checkBoxListener = new ChangeListener() {
      public void stateChanged(ChangeEvent event) {
        setEnabledButtons();
      }
    };

    // insert the top portion of the panel first
    panel.add(topPanel, new GridBagConstraints(0, 0, 1, 1, 10, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(0,0,0,0), 0, 0));


    boolean includeUpdate = isUpdatePermitsMode;

    // insert the center portion of the panel
    JPanel centerPanel = new JPanel();
    centerPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
    centerPanel.setLayout(new BorderLayout());
    centerPanel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_User_Settings")), BorderLayout.WEST);
    centerPanel.add(new JMyLabel(), BorderLayout.CENTER);
    if (isUpdatePermitsMode) {
      centerPanel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Allow_User_to_Change_the_Setting")), BorderLayout.EAST);
    }
    // insert the center portion of the panel
    panel.add(centerPanel, new GridBagConstraints(0, 1, 1, 1, 10, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));


    JPanel bottomPanel = new JPanel();
    bottomPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
    bottomPanel.setLayout(new GridBagLayout());

    posY = 0;

    checks.jNotify = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Send_email_notification_when_new_messages_arrive."));
    checks.jNotifyUpdate = new JMyCheckBox();
    checks.addCheckBoxes(bottomPanel, includeUpdate, checks.jNotify, checks.jNotifyUpdate, myUserRec.notifyByEmail, userRec.notifyByEmail, UserRecord.EMAIL_NOTIFY_YES, checkBoxListener, posY);
    posY ++;

    jContactEmail = new JMyTextField(userRec.emailAddress);
    jContactEmail.getDocument().addDocumentListener(documentChangeListener);
    final JMyLabel jSendNotificationsLabel = new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Send_notifications_to_address"));
    bottomPanel.add(jSendNotificationsLabel, new GridBagConstraints(0, posY, 2, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0, 25, 5, 5), 0, 0));
    posY ++;
    bottomPanel.add(jContactEmail, new GridBagConstraints(0, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 25, 5, 5), 0, 0));
    posY ++;
    checks.jNotify.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        boolean isSelected = checks.jNotify.isSelected();
        jSendNotificationsLabel.setEnabled(isSelected);
        jContactEmail.setEnabled(isSelected);
      }
    });
    jSendNotificationsLabel.setEnabled(userRec.isNotifyByEmail());
    jContactEmail.setEnabled(userRec.isNotifyByEmail());

    checks.jNotifySubjectAddress = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Include_sender_address_and_message_subject_in_notifications."));
    checks.jNotifySubjectAddressUpdate = new JMyCheckBox();
    checks.addCheckBoxes(bottomPanel, includeUpdate, checks.jNotifySubjectAddress, checks.jNotifySubjectAddressUpdate, myUserRec.notifyByEmail, userRec.notifyByEmail, UserRecord.EMAIL_NOTIFY_INCLUDE_SUBJECT_AND_FROM_ADDRESS, checkBoxListener, posY);
    posY ++;

    if (isChangingMyUserRecord) {
      // separator
      bottomPanel.add(new JSeparator(), new GridBagConstraints(0, posY, 2, 1, 0, 0,
          GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;

      isSoundEnabled = Boolean.valueOf(GlobalProperties.getProperty(Sounds.SOUND_ENABLEMENT_PROPERTY, "true")).booleanValue();
      jEnableSound = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Enable_sound_on_this_computer."), isSoundEnabled);
      jEnableSound.addChangeListener(checkBoxListener);
      bottomPanel.add(jEnableSound, new GridBagConstraints(0, posY, 4, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;

      isAntialiasingEnabled = Boolean.valueOf(GlobalProperties.getProperty(MiscGui.ANTIALIASING_ENABLEMENT_PROPERTY, "true")).booleanValue();
      jEnableAntialiasing = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Enable_anti-aliasing_on_this_computer."), isAntialiasingEnabled);
      jEnableAntialiasing.addChangeListener(checkBoxListener);
      jEnableAntialiasing.setEnabled(MiscGui.isAntiAliasingCapable());
      bottomPanel.add(jEnableAntialiasing, new GridBagConstraints(0, posY, 4, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;

      jResetLocalSettings = new JMyCheckBox("Reset local settings.", false);
      jResetLocalSettings.addChangeListener(checkBoxListener);
      bottomPanel.add(jResetLocalSettings, new GridBagConstraints(0, posY, 4, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;
    }

    // filler
    bottomPanel.add(new JMyLabel(), new GridBagConstraints(0, posY, 2, 1, 0, 10,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));

    // insert the bottom portion of the panel
    panel.add(bottomPanel, new GridBagConstraints(0, 2, 1, 1, 10, 10,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0,0,0,0), 0, 0));

    return panel;
  }

  private JPanel createAccountsPanel(UserRecord myUserRec, final UserRecord[] subUsers) {
    JPanel panel = new JPanel();

    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    panel.setLayout(new GridBagLayout());

    int posY = 0;


    JPanel topPanel = new JPanel();
    topPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
    topPanel.setLayout(new GridBagLayout());

    JLabel accNameLabel = new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Selected_Accounts"));
    topPanel.add(accNameLabel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    JPanel listPanel = new JPanel();
    listPanel.setLayout(new GridBagLayout());
    for (int i=0; i<subUsers.length; i++) {
      Record rec = MsgPanelUtils.convertUserIdToFamiliarUser(subUsers[i].userId, true, true);
      listPanel.add(new JMyLabel(ListRenderer.getRenderedText(rec), ListRenderer.getRenderedIcon(rec), JLabel.LEADING), new GridBagConstraints(0, i, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 10, 2, 10), 0, 0));
    }
    JComponent mainList = null;
    if (subUsers.length > 5) {
      JScrollPane sc = new JScrollPane(listPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      sc.getVerticalScrollBar().setUnitIncrement(5);
      mainList = sc;
    } else {
      mainList = listPanel;
    }
    topPanel.add(mainList, new GridBagConstraints(1, posY, 2, 1, 10, 10,
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    ChangeListener checkBoxListener = new ChangeListener() {
      public void stateChanged(ChangeEvent event) {
        setEnabledButtons();
      }
    };

    // insert the top portion of the panel first
    panel.add(topPanel, new GridBagConstraints(0, 0, 1, 1, 10, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(0,0,0,0), 0, 0));

    boolean includeUpdate = isUpdatePermitsMode;

    // insert the center portion of the panel
    JPanel centerPanel = new JPanel();
    centerPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
    centerPanel.setLayout(new BorderLayout(10, 10));
    jIncludeChangesToAccounts = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Include_the_following_settings_in_this_update"));
    jIncludeChangesToAccounts.setFont(jIncludeChangesToAccounts.getFont().deriveFont(Font.BOLD));
    jIncludeChangesToAccounts.addChangeListener(checkBoxListener);
    centerPanel.add(jIncludeChangesToAccounts, BorderLayout.NORTH);
    centerPanel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_User_Settings")), BorderLayout.WEST);
    centerPanel.add(new JMyLabel(), BorderLayout.CENTER);
    if (isUpdatePermitsMode) {
      centerPanel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Allow_User_to_Change_the_Setting")), BorderLayout.EAST);
    }
    // insert the center portion of the panel
    panel.add(centerPanel, new GridBagConstraints(0, 1, 1, 1, 10, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    final JPanel bottomPanel = new JPanel();
    bottomPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
    bottomPanel.setLayout(new GridBagLayout());

    posY = 0;

    checks.jNotify = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Send_email_notification_when_new_messages_arrive."));
    checks.jNotifyUpdate = new JMyCheckBox();
    checks.addCheckBoxes(bottomPanel, includeUpdate, checks.jNotify, checks.jNotifyUpdate, myUserRec.notifyByEmail, AccountOptionPermitChecks.getMostCommonNotifyByEmailBits(subUsers), UserRecord.EMAIL_NOTIFY_YES, checkBoxListener, posY);
    posY ++;

    checks.jNotifySubjectAddress = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Include_sender_address_and_message_subject_in_notifications."));
    checks.jNotifySubjectAddressUpdate = new JMyCheckBox();
    checks.addCheckBoxes(bottomPanel, includeUpdate, checks.jNotifySubjectAddress, checks.jNotifySubjectAddressUpdate, myUserRec.notifyByEmail, AccountOptionPermitChecks.getMostCommonNotifyByEmailBits(subUsers), UserRecord.EMAIL_NOTIFY_INCLUDE_SUBJECT_AND_FROM_ADDRESS, checkBoxListener, posY);
    posY ++;

    // filler
    bottomPanel.add(new JMyLabel(), new GridBagConstraints(0, posY, 2, 1, 0, 10,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));

    // insert the bottom portion of the panel
    panel.add(bottomPanel, new GridBagConstraints(0, 2, 1, 1, 10, 10,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0,0,0,0), 0, 0));

    return panel;
  }

  private JPanel createOptionsPanel() {
    ChangeListener checkBoxListener = new ChangeListener() {
      public void stateChanged(ChangeEvent event) {
        setEnabledButtons();
      }
    };
    return checks.createOptionsPanel(checkBoxListener, myUserRecord, userRecords);
  }

  private JPanel createPermissionsPanel() {
    ChangeListener checkBoxListener = new ChangeListener() {
      public void stateChanged(ChangeEvent event) {
        setEnabledButtons();
      }
    };
    return checks.createPermissionsPanel(checkBoxListener, myUserRecord, userRecords);
  }

  private class OKActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(OKActionListener.class, "actionPerformed(ActionEvent event)");
      if (trace != null) trace.args(event);

      if (userRecords.length == 1)
        pressedOk(userRecords[0]);
      else
        pressedOk(userRecords);

      closeDialog();
      if (trace != null) trace.exit(OKActionListener.class);
    }
  }

  private void pressedOk(UserRecord uRec) {
    Long storageLimit = null;
    Long transferLimit = null;
    try {
      storageLimit = jPanelQuotas.getNewStorageLimit();
    } catch (Throwable t) { }
    try {
      transferLimit = jPanelQuotas.getNewBandwidthLimit();
    } catch (Throwable t) { }

    // See if auto-update setting changes, if it becomes enabled, request updates.
    boolean requestAutoUpdate = false;
    if (isChangingMyUserRecord) {
      Long oldFlags = uRec.flags;
      Long newFlags = checks.getNewFlagSetting(uRec);
      requestAutoUpdate = Misc.isBitSet(oldFlags, UserRecord.FLAG_DISABLE_AUTO_UPDATES) && !Misc.isBitSet(newFlags, UserRecord.FLAG_DISABLE_AUTO_UPDATES);
    }

    {
      Usr_AltUsrData_Rq request = new Usr_AltUsrData_Rq();
      request.userRecord = (UserRecord) uRec.clone();
      request.userRecord.emailAddress = jContactEmail.getText().trim();
      request.userRecord.acceptingSpam = checks.getNewSpamSetting(uRec);
      request.userRecord.flags = checks.getNewFlagSetting(uRec);
      request.userRecord.notifyByEmail = checks.getNewNotifySetting(uRec);
      request.userRecord.storageLimit = storageLimit;
      request.userRecord.transferLimit = transferLimit;

      // check prerequisites for options
      if (Misc.isBitSet(request.userRecord.flags, UserRecord.FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY) && !Misc.isBitSet(request.userRecord.flags, UserRecord.FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER)) {
        MessageDialog.showWarningDialog(parent, "Password Recovery option requires Private Key to be stored on the server.  Option was disabled.", "Invalid Password Recovery setting");
        request.userRecord.flags = (Long) Misc.setBitObj(false, request.userRecord.flags, UserRecord.FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY);
      }

      UserSettingsRecord usrSettingsRec = null;
      // if signatures changed
      if (isChangingMyUserRecord && jPanelSignatures != null && jPanelSignatures.isDataChanged()) {
        usrSettingsRec = cache.getMyUserSettingsRecord();
        if (usrSettingsRec == null) {
          usrSettingsRec = new UserSettingsRecord();
          usrSettingsRec.setSymKey(new BASymmetricKey(32));
        }
        usrSettingsRec.setXmlText(jPanelSignatures.getData().makeXMLData());
        usrSettingsRec.seal(cache.getKeyRecordMyCurrent());
        request.userSettingsRecord = usrSettingsRec;
      }

      // if auto-responder changed
      if (jPanelResponder != null && jPanelResponder.isDataChanged()) {
        AutoResponderRecord autoRespRec = new AutoResponderRecord();
        autoRespRec.userId = uRec.userId;
        autoRespRec.dateStart = jPanelResponder.getDateStart();
        autoRespRec.dateEnd = jPanelResponder.getDateEnd();
        autoRespRec.setXmlText(jPanelResponder.getData());
        autoRespRec.seal();
        request.userRecord.autoResp = jPanelResponder.getEnabled().equals(Boolean.TRUE) ? new Character('Y') : new Character('N');
        request.autoResponderRecord = autoRespRec;
      }

      SIL.submitAndReturn(new MessageAction(CommandCodes.USR_Q_ALTER_DATA, request));
    }


    {
      String newDefaultEmail = jDefaultEmail.getText().trim();
      if (!defaultEmail.equalsIgnoreCase(newDefaultEmail)) {
        if (newDefaultEmail.length() == 0) {
          if (uRec.defaultEmlId.longValue() != UserRecord.GENERIC_EMAIL_ID) {
            // remove email address
            String title = com.CH_gui.lang.Lang.rb.getString("msgTitle_Delete_Confirmation");
            String body = com.CH_gui.lang.Lang.rb.getString("msg_You_are_about_to_delete_your_personalized_email_address.");
            body += "  \n\n";
            body += com.CH_gui.lang.Lang.rb.getString("msg_Are_you_sure_you_want_to_do_this?");
            boolean option = MessageDialog.showDialogYesNo(AccountOptionsDialog.this, body, title);
            if (option) {
              Obj_IDList_Co ids = new Obj_IDList_Co(uRec.defaultEmlId);
              SIL.submitAndReturn(new MessageAction(CommandCodes.EML_Q_REMOVE, ids));
            }
          }
        } else if (uRec.defaultEmlId.longValue() == UserRecord.GENERIC_EMAIL_ID) {
          // create new email address
          Object[] objs = new Object[] { uRec.userId, newDefaultEmail };
          SIL.submitAndReturn(new MessageAction(CommandCodes.EML_Q_CREATE, new Obj_List_Co(objs)));
        } else {
          // alter existing email address
          String title = com.CH_gui.lang.Lang.rb.getString("msgTitle_Confirmation");
          String body = com.CH_gui.lang.Lang.rb.getString("msg_You_are_about_to_change_your_personalized_email_address.");
          if (!EmailRecord.isDomainEqual(newDefaultEmail, defaultEmail)) {
            body += "  ";
            body += java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("msg_This_change_will_terminate_your_current_use_of_email_domain_'{0}'."), new Object[] {EmailRecord.getDomain(defaultEmail)});
          }
          body += "  \n\n";
          body += com.CH_gui.lang.Lang.rb.getString("msg_Are_you_sure_you_want_to_do_this?");
          boolean option = MessageDialog.showDialogYesNo(AccountOptionsDialog.this, body, title);
          if (option) {
            Object[] objs = new Object[] { uRec.defaultEmlId, newDefaultEmail, Boolean.TRUE };
            SIL.submitAndReturn(new MessageAction(CommandCodes.EML_Q_ALTER, new Obj_List_Co(objs)));
          }
        }
      }
    }

    if (isChangingMyUserRecord) {
      // if key should be send to server...
      if (isMyKeyLocal == checks.jKeyOnServer.isSelected()) {
        final boolean storeKeyOnServer = checks.jKeyOnServer.isSelected();
        Thread th = new ThreadTraced("Private Key Location Changer") {
          public void runTraced() {
            StringBuffer errorBuffer = new StringBuffer();
            boolean error = false;
            try {
              if (!storeKeyOnServer) {
                GlobalSubProperties keyProps = new GlobalSubProperties(GlobalSubProperties.PROPERTY_EXTENSION_KEYS);
                String defaultFileName = keyProps.getPropertiesFullFileName();
                File chosenFile = LoginFrame.choosePrivKeyStorageFile(new File(defaultFileName));
                if (chosenFile != null) {
                  error = !UserOps.sendPasswordChange(SIL, cache.getEncodedPassword(), storeKeyOnServer, chosenFile, errorBuffer);
                }
              } else {
                error = !UserOps.sendPasswordChange(SIL, cache.getEncodedPassword(), storeKeyOnServer, null, errorBuffer);
              }
              if (error) {
                String where = storeKeyOnServer ? "on the server" : "locally";
                String msg = "Private key could not be stored " + where + "!";
                if (errorBuffer.length() > 0)
                  msg += errorBuffer.toString();
                MessageDialog.showErrorDialog(null, msg, "Key Storage Failed", true);
              }
            } catch (Throwable t) {
              MessageDialog.showErrorDialog(null, t.getMessage(), "Key Storage Failed");
            }
          }
        };
        th.setDaemon(true);
        th.start();
      }
      // if sound enablement changed
      if (isSoundEnabled != jEnableSound.isSelected()) {
        GlobalProperties.setProperty(Sounds.SOUND_ENABLEMENT_PROPERTY, ""+jEnableSound.isSelected());
      }
      // if anti-aliasing enablement changed
      if (isAntialiasingEnabled != jEnableAntialiasing.isSelected()) {
        boolean enable = jEnableAntialiasing.isSelected();
        GlobalProperties.setProperty(MiscGui.ANTIALIASING_ENABLEMENT_PROPERTY, ""+enable);
        MiscGui.setAntiAliasingEnabled(enable);
      }
      if (jResetLocalSettings.isSelected()) {
        GlobalProperties.resetMyAndGlobalProperties();
        GlobalProperties.store();
        MessageDialog.showInfoDialog(null, "Local settings reset and reinitialized.", "Confirmation", false);
      }
    }

    if (jActivationCode.getText().trim().length() > 0) {
      String code = jActivationCode.getText().trim();
      if (code.equalsIgnoreCase("reinitialize trace")) {
        TraceProperties.initialLoad();
        MessageDialog.showInfoDialog(null, "Trace reinitialized.", "Confirmation", false);
      } else {
        // After applying the code, reload the welcome screen
        Runnable afterJob = new Runnable() {
          public void run() {
            MainFrame.getSingleInstance().setDefaultWelcomeScreenPane();
          }
        };
        SIL.submitAndReturn(new MessageAction(CommandCodes.USR_Q_APPLY_CODE, new Obj_List_Co(new Object[] { uRec.userId, code })), 60000, afterJob, null);
      }
    }

    if (requestAutoUpdate) {
      SIL.submitAndReturn(new MessageAction(CommandCodes.SYS_Q_GET_AUTO_UPDATE));
    }
  }

  private void pressedOk(UserRecord[] uRecs) {
    Long storageLimit = null;
    Long transferLimit = null;
    if (jPanelQuotas.jIncludeChangesToQuotas.isSelected()) {
      boolean capped = false;
      try {
        storageLimit = jPanelQuotas.getNewStorageLimit();
        if (storageLimit != null && myUserRecord.storageLimit != null && storageLimit.longValue() > myUserRecord.storageLimit.longValue()) {
          storageLimit = myUserRecord.storageLimit;
          capped = true;
        }
      } catch (Throwable t) {
      }
      try {
        transferLimit = jPanelQuotas.getNewBandwidthLimit();
        if (transferLimit != null && myUserRecord.transferLimit != null && transferLimit.longValue() > myUserRecord.transferLimit.longValue()) {
          transferLimit = myUserRecord.transferLimit;
          capped = true;
        }
      } catch (Throwable t) {
      }
      if (capped) {
        MessageDialog.showWarningDialog(parent, com.CH_gui.lang.Lang.rb.getString("msg_Quotas_for_user_accounts_may_not_exceed_your_account_quotas.__User_accounts'_limits_have_been_reduced_to_match_your_quotas."), com.CH_gui.lang.Lang.rb.getString("title_Quotas_too_high"));
      }
    }

    // check prerequisites for options
    if (checks.jPasswordReset.isSelected() && !checks.jKeyOnServer.isSelected()) {
      MessageDialog.showWarningDialog(parent, "Password Recovery option requires Private Key to be stored on the server.  Option was disabled.", "Invalid Password Recovery setting");
      checks.jPasswordReset.setSelected(false);
    }

    for (int i=0; i<uRecs.length; i++) {
      UserRecord uRec = uRecs[i];
      Usr_AltUsrData_Rq request = new Usr_AltUsrData_Rq();
      UserRecord uRecC = (UserRecord) uRec.clone();
      request.userRecord = uRecC;
      if (jIncludeChangesToAccounts.isSelected()) {
        uRecC.notifyByEmail = (Short) Misc.setBitObj(checks.jNotify.isSelected(), uRecC.notifyByEmail, UserRecord.EMAIL_NOTIFY_YES);
        uRecC.notifyByEmail = (Short) Misc.setBitObj(!checks.jNotifyUpdate.isSelected(), uRecC.notifyByEmail, UserRecord.EMAIL_NOTIFY_YES__NO_UPDATE);
        uRecC.notifyByEmail = (Short) Misc.setBitObj(checks.jNotifySubjectAddress.isSelected(), uRecC.notifyByEmail, UserRecord.EMAIL_NOTIFY_INCLUDE_SUBJECT_AND_FROM_ADDRESS);
        uRecC.notifyByEmail = (Short) Misc.setBitObj(!checks.jNotifySubjectAddressUpdate.isSelected(), uRecC.notifyByEmail, UserRecord.EMAIL_NOTIFY_INCLUDE_SUBJECT_AND_FROM_ADDRESS__NO_UPDATE);
      }
      if (checks.jIncludeChangesToOptions.isSelected()) {
        uRecC.acceptingSpam = (Short) Misc.setBitObj(checks.jSpamInternal.isSelected(), uRecC.acceptingSpam, UserRecord.ACC_SPAM_YES_INTER);
        uRecC.acceptingSpam = (Short) Misc.setBitObj(!checks.jSpamInternalUpdate.isSelected(), uRecC.acceptingSpam, UserRecord.ACC_SPAM_YES_INTER__NO_UPDATE);
        uRecC.acceptingSpam = (Short) Misc.setBitObj(checks.jSpamRegEmail.isSelected(), uRecC.acceptingSpam, UserRecord.ACC_SPAM_YES_REG_EMAIL);
        uRecC.acceptingSpam = (Short) Misc.setBitObj(!checks.jSpamRegEmailUpdate.isSelected(), uRecC.acceptingSpam, UserRecord.ACC_SPAM_YES_REG_EMAIL__NO_UPDATE);
        uRecC.acceptingSpam = (Short) Misc.setBitObj(checks.jSpamSslEmail.isSelected(), uRecC.acceptingSpam, UserRecord.ACC_SPAM_YES_SSL_EMAIL);
        uRecC.acceptingSpam = (Short) Misc.setBitObj(!checks.jSpamSslEmailUpdate.isSelected(), uRecC.acceptingSpam, UserRecord.ACC_SPAM_YES_SSL_EMAIL__NO_UPDATE);
        uRecC.flags = (Long) Misc.setBitObj(!checks.jSecureReplyLink.isSelected(), uRecC.flags, UserRecord.FLAG_SKIP_SECURE_REPLY_LINK_IN_EXTERNAL_EMAILS); // inverted meaning of the checkbox
        uRecC.flags = (Long) Misc.setBitObj(!checks.jSecureReplyLinkUpdate.isSelected(), uRecC.flags, UserRecord.FLAG_SKIP_SECURE_REPLY_LINK_IN_EXTERNAL_EMAILS__NO_UPDATE);
        uRecC.notifyByEmail = (Short) Misc.setBitObj(checks.jWarnExternal.isSelected(), uRecC.notifyByEmail, UserRecord.EMAIL_WARN_EXTERNAL);
        uRecC.notifyByEmail = (Short) Misc.setBitObj(!checks.jWarnExternalUpdate.isSelected(), uRecC.notifyByEmail, UserRecord.EMAIL_WARN_EXTERNAL__NO_UPDATE);
        uRecC.notifyByEmail = (Short) Misc.setBitObj(checks.jSwitchPreview.isSelected(), uRecC.notifyByEmail, UserRecord.EMAIL_MANUAL_SELECT_PREVIEW_MODE);
        uRecC.notifyByEmail = (Short) Misc.setBitObj(!checks.jSwitchPreviewUpdate.isSelected(), uRecC.notifyByEmail, UserRecord.EMAIL_MANUAL_SELECT_PREVIEW_MODE__NO_UPDATE);
        uRecC.flags = (Long) Misc.setBitObj(!checks.jAutoUpdates.isSelected(), uRecC.flags, UserRecord.FLAG_DISABLE_AUTO_UPDATES); // inverted meaning of the checkbox
        uRecC.flags = (Long) Misc.setBitObj(!checks.jAutoUpdatesUpdate.isSelected(), uRecC.flags, UserRecord.FLAG_DISABLE_AUTO_UPDATES__NO_UPDATE);
        uRecC.flags = (Long) Misc.setBitObj(checks.jUserOffline.isSelected(), uRecC.flags, UserRecord.FLAG_USER_OFFLINE_POPUP);
        uRecC.flags = (Long) Misc.setBitObj(!checks.jUserOfflineUpdate.isSelected(), uRecC.flags, UserRecord.FLAG_USER_OFFLINE_POPUP__NO_UPDATE);
        uRecC.flags = (Long) Misc.setBitObj(checks.jKeyOnServer.isSelected(), uRecC.flags, UserRecord.FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER);
        uRecC.flags = (Long) Misc.setBitObj(!checks.jKeyOnServerUpdate.isSelected(), uRecC.flags, UserRecord.FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER__NO_UPDATE);
        uRecC.flags = (Long) Misc.setBitObj(checks.jPasswordReset.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY);
        uRecC.flags = (Long) Misc.setBitObj(!checks.jPasswordResetUpdate.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY__NO_UPDATE);
        uRecC.flags = (Long) Misc.setBitObj(checks.jUseEnterKeyChatSend.isSelected(), uRecC.flags, UserRecord.FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES);
        uRecC.flags = (Long) Misc.setBitObj(!checks.jUseEnterKeyChatSendUpdate.isSelected(), uRecC.flags, UserRecord.FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES__NO_UPDATE);
      }
      if (checks.jIncludeChangesToPermissions.isSelected()) {
        uRecC.flags = (Long) Misc.setBitObj(checks.jMasterCapable.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_CREATING_SUBACCOUNTS__MASTER_CAPABLE);
        uRecC.flags = (Long) Misc.setBitObj(!checks.jMasterCapableUpdate.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_CREATING_SUBACCOUNTS__MASTER_CAPABLE__NO_UPDATE);
        uRecC.flags = (Long) Misc.setBitObj(checks.jPasswordChange.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_PASSWORD_CHANGE);
        uRecC.flags = (Long) Misc.setBitObj(!checks.jPasswordChangeUpdate.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_PASSWORD_CHANGE__NO_UPDATE);
        uRecC.flags = (Long) Misc.setBitObj(checks.jNicknameChange.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_NICKNAME_CHANGE);
        uRecC.flags = (Long) Misc.setBitObj(!checks.jNicknameChangeUpdate.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_NICKNAME_CHANGE__NO_UPDATE);
        uRecC.flags = (Long) Misc.setBitObj(checks.jAccountDelete.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_ACCOUNT_DELETE);
        uRecC.flags = (Long) Misc.setBitObj(!checks.jAccountDeleteUpdate.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_ACCOUNT_DELETE__NO_UPDATE);
        uRecC.flags = (Long) Misc.setBitObj(checks.jContactsAlter.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_GIVEN_CONTACTS_ALTER);
        uRecC.flags = (Long) Misc.setBitObj(!checks.jContactsAlterUpdate.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_GIVEN_CONTACTS_ALTER__NO_UPDATE);
        uRecC.flags = (Long) Misc.setBitObj(checks.jContactsDelete.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_GIVEN_CONTACTS_DELETE);
        uRecC.flags = (Long) Misc.setBitObj(!checks.jContactsDeleteUpdate.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_GIVEN_CONTACTS_DELETE__NO_UPDATE);
        uRecC.flags = (Long) Misc.setBitObj(checks.jEmailAlter.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_GIVEN_EMAILS_ALTER);
        uRecC.flags = (Long) Misc.setBitObj(!checks.jEmailAlterUpdate.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_GIVEN_EMAILS_ALTER__NO_UPDATE);
        uRecC.flags = (Long) Misc.setBitObj(checks.jEmailDelete.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_GIVEN_EMAILS_DELETE);
        uRecC.flags = (Long) Misc.setBitObj(!checks.jEmailDeleteUpdate.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_GIVEN_EMAILS_DELETE__NO_UPDATE);
        uRecC.flags = (Long) Misc.setBitObj(checks.jFolderDelete.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_GIVEN_MASTER_FOLDERS_DELETE);
        uRecC.flags = (Long) Misc.setBitObj(!checks.jFolderDeleteUpdate.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_GIVEN_MASTER_FOLDERS_DELETE__NO_UPDATE);
        uRecC.flags = (Long) Misc.setBitObj(checks.jContactOutside.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_MAKING_CONTACTS_OUTSIDE_ORGANIZATION);
        uRecC.flags = (Long) Misc.setBitObj(!checks.jContactOutsideUpdate.isSelected(), uRecC.flags, UserRecord.FLAG_ENABLE_MAKING_CONTACTS_OUTSIDE_ORGANIZATION__NO_UPDATE);
      }
      if (jPanelQuotas.jIncludeChangesToQuotas.isSelected()) {
        uRecC.storageLimit = storageLimit;
        uRecC.transferLimit = transferLimit;
      }
      // if auto-responder changed
      if (jPanelResponder.jIncludeChangesToResponder.isSelected()) {
        AutoResponderRecord autoRespRec = new AutoResponderRecord();
        autoRespRec.userId = uRecC.userId;
        autoRespRec.dateStart = jPanelResponder.getDateStart();
        autoRespRec.dateEnd = jPanelResponder.getDateEnd();
        autoRespRec.setXmlText(jPanelResponder.getData());
        autoRespRec.seal();
        request.userRecord.autoResp = jPanelResponder.getEnabled().equals(Boolean.TRUE) ? new Character('Y') : new Character('N');
        request.autoResponderRecord = autoRespRec;
      }
      SIL.submitAndReturn(new MessageAction(CommandCodes.USR_Q_ALTER_DATA, request));
    }
  }


  private class CancelActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      closeDialog();
    }
  }

  protected void refreshEmailAddressFromCache() {
    if (userRecords.length == 1) {
      UserRecord uRec = cache.getUserRecord(userRecords[0].userId);
      if (uRec != null) {
        String[] emls = UserOps.getCachedDefaultEmail(uRec, false);
        if (emls != null) {
          jDefaultEmail.setText(emls[2]);
          defaultEmail = emls[2].toLowerCase();
        }
        setEnabledButtons();
      }
    }
  }

  private void setEnabledButtons() {
    // see if any options have changed

    boolean limitsOk = true;
    Long storageLimit = null;
    Long transferLimit = null;

    try {
      storageLimit = jPanelQuotas.getNewStorageLimit();
      transferLimit = jPanelQuotas.getNewBandwidthLimit();
    } catch (Throwable t) {
      limitsOk = false;
    }

    if (userRecords.length == 1) {
      String newDefaultEmail = jDefaultEmail.getText().trim().toLowerCase();
      String newEmail = jContactEmail.getText().trim().toLowerCase();
      String oldEmail = ""+(userRecords.length == 1 ? userRecords[0].emailAddress : "");

      oldEmail = oldEmail.toLowerCase();

      boolean newDefaultEmailOk = newDefaultEmail.length() == 0 || EmailRecord.gatherAddresses(newDefaultEmail) != null;
      boolean newEmailOk = newEmail.length() == 0 || EmailRecord.gatherAddresses(newEmail) != null;

      if (newEmailOk && newDefaultEmailOk && limitsOk &&
            (!newEmail.equals(oldEmail) ||
              jActivationCode.getText().trim().length() > 0 ||
              !userRecords[0].acceptingSpam.equals(checks.getNewSpamSetting(userRecords[0])) ||
              !userRecords[0].notifyByEmail.equals(checks.getNewNotifySetting(userRecords[0])) ||
              !userRecords[0].flags.equals(checks.getNewFlagSetting(userRecords[0])) ||
              (isChangingMyUserRecord && isSoundEnabled != jEnableSound.isSelected()) ||
              (isChangingMyUserRecord && isAntialiasingEnabled != jEnableAntialiasing.isSelected()) ||
              (isChangingMyUserRecord && jResetLocalSettings.isSelected()) ||
              !newDefaultEmail.equals(defaultEmail) ||
              (storageLimit != null && !storageLimit.equals(userRecords[0].storageLimit)) ||
              (transferLimit != null && !transferLimit.equals(userRecords[0].transferLimit)) ||
              (jPanelSignatures != null && jPanelSignatures.isChangeAttempted()) ||
              (jPanelResponder != null && jPanelResponder.isChangeAttempted())
            )
         )
      {
        // When there is no email provided, notify must be off too.
        boolean ok = ((newEmail.length() == 0 && !checks.jNotify.isSelected()) || newEmail.length() > 0);
        jOk.setEnabled(ok);
      } else {
        jOk.setEnabled(false);
      }
    } else {
      if ((jIncludeChangesToAccounts != null && jIncludeChangesToAccounts.isSelected()) ||
              (checks != null && checks.jIncludeChangesToOptions != null && checks.jIncludeChangesToOptions.isSelected()) ||
              (checks != null && checks.jIncludeChangesToPermissions != null && checks.jIncludeChangesToPermissions.isSelected()) ||
              (jPanelResponder != null && jPanelResponder.jIncludeChangesToResponder != null && jPanelResponder.jIncludeChangesToResponder.isSelected()) ||
              (jPanelQuotas != null && jPanelQuotas.jIncludeChangesToQuotas != null && jPanelQuotas.jIncludeChangesToQuotas.isSelected())
                      )
        jOk.setEnabled(true);
      else
        jOk.setEnabled(false);
    }
  }

  /**
   * If my email is 'given-to-me' and I am not allowed to change it, then set enabled false
   */
  private void setEditableDefaultEmail(UserRecord myUserRecord, EmailRecord emlRecord) {
    // if 'given-to-me' email and not allowed to change it, then set enabled false
    if (emlRecord.isGiven() && (myUserRecord.flags.longValue() & UserRecord.FLAG_ENABLE_GIVEN_EMAILS_ALTER) == 0)
      jDefaultEmail.setEditable(false);
    else
      jDefaultEmail.setEditable(true);
  }

  public void closeDialog() {
    if (documentChangeListener != null && jContactEmail != null) {
      jContactEmail.getDocument().removeDocumentListener(documentChangeListener);
    }
    if (documentChangeListener != null && jDefaultEmail != null) {
      jDefaultEmail.getDocument().removeDocumentListener(documentChangeListener);
    }
    if (documentChangeListener != null && jActivationCode != null) {
      jActivationCode.getDocument().removeDocumentListener(documentChangeListener);
    }
    if (documentChangeListener != null && jPanelQuotas.jStorageLimit != null) {
      jPanelQuotas.jStorageLimit.getDocument().removeDocumentListener(documentChangeListener);
    }
    if (documentChangeListener != null && jPanelQuotas.jBandwidthLimit != null) {
      jPanelQuotas.jBandwidthLimit.getDocument().removeDocumentListener(documentChangeListener);
    }
    if (documentChangeListener != null && jPanelSignatures != null) {
      jPanelSignatures.removeDocumentListener(documentChangeListener);
    }
    if (documentChangeListener != null && jPanelResponder != null) {
      jPanelResponder.removeDocumentListener(documentChangeListener);
    }
    documentChangeListener = null;
    super.closeDialog();
  }


  private class DocumentChangeListener implements DocumentListener {
    public void changedUpdate(DocumentEvent e) {
      setEnabledButtons();
    }
    public void insertUpdate(DocumentEvent e) {
      setEnabledButtons();
    }
    public void removeUpdate(DocumentEvent e) {
      setEnabledButtons();
    }
  }

}
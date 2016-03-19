/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.frame;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.DefaultReplyRunner;
import com.CH_cl.service.engine.EngineFinder;
import com.CH_cl.service.engine.LoginCoordinatorI;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.FileLobUp;
import com.CH_cl.service.ops.UserOps;
import com.CH_co.cryptx.*;
import com.CH_co.monitor.ProgMonitorFactory;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.monitor.ProgMonitorPool;
import com.CH_co.monitor.Stats;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.Str_Rp;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.service.msg.dataSets.usr.Usr_LoginSecSess_Rp;
import com.CH_co.service.msg.dataSets.usr.Usr_LoginSecSess_Rq;
import com.CH_co.service.msg.dataSets.usr.Usr_NewUsr_Rq;
import com.CH_co.service.records.EmailRecord;
import com.CH_co.service.records.FolderShareRecord;
import com.CH_co.service.records.KeyRecord;
import com.CH_co.service.records.UserRecord;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_co.util.GlobalProperties;
import com.CH_co.util.ImageNums;
import com.CH_co.util.Misc;
import com.CH_co.util.URLs;
import com.CH_gui.dialog.KeyGenerationOptionsDialog;
import com.CH_gui.dialog.LicenseDialog;
import com.CH_gui.dialog.UserSelectPassRecoveryDialog;
import com.CH_gui.gui.*;
import com.CH_gui.list.ListUpdatableI;
import com.CH_gui.list.ObjectsProviderUpdaterI;
import com.CH_gui.list.TypeAheadPopupList;
import com.CH_gui.util.*;
import com.CH_guiLib.gui.JMyComboBox;
import com.CH_guiLib.gui.JMyTextField;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.Keymap;

/** 
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.39 $</b>
*
* @author  Marcin Kurzawa
*/
public class LoginFrame extends JFrame {

  private static final boolean ENABLE_PASSWORD_LENGTH_NOTE = false;
  private static final boolean ENABLE_PASSWORD_NON_RECOVERY_WARNING = true;

  private static final String PROPERTY_USER_NAME_LIST = "LastUserNameList";
  private static final String PROPERTY_SERVER_LIST = "ServerList";
  private static final String PROPERTY_REMEMBER_USER_NAME = "RememberUserName";
  private static final String DEFAULT_USER_NAME = com.CH_cl.lang.Lang.rb.getString("Username");

  public static final String MODE_LOGIN = "login";
  public static final String MODE_SIGNUP = "signup";

  private LoginCoordinatorI loginCoordinator;
  private String defaultUserName;
  private String[] defaultUserNameList;
  private boolean defaultRememberUserName;
  private JPanel mainPanel;
  private JButton switchModeButton;
  private boolean isNewAccountOptionEnabled;
  private JButton okButton;
  private JButton cancelButton;

  private static final int DEFAULT_BUTTON_INDEX = 0;

  private static final int MIN_PASSWORD_LENGTH = 6;

  private boolean isNewAccountDialog;

  /* Components that are placed in the mainPanel */
  private JLabel jLogo ;
//  private JLabel switchModeLink ;
  private JLabel jUsernameLabel ;
  private JTextField userName ;
//  private JMyTextComboBox newEmail ;
//  private JLabel newEmailLabel ;
  private JMyPasswordKeyboardField password ;
  private JLabel capsLockWarning ;
  private Timer capsLockTimer ;
  private JLabel recoveryLabel ;
  private JMyPasswordKeyboardField retypePassword ;
  private JLabel passwordLabel ;
  private JLabel reTypeLabel ;
  private JTextField currentEmail ;
  private JLabel currentEmailLabel ;
  private JLabel serverLabel ;
  private JComboBox serverCombo ;
  private JLabel changeServerLabel ;
  private JLabel proxySettingsLabel ;
  private JLabel passwordConditionLabel ;
  private JLabel advancedLabel ;
  private JButton advancedButton ;
  private JLabel accountCodeLabel ;
  private JMyTextField accountCode ;
  private JCheckBox rememberUserName ;
  private JLabel versionLabel ;
  private JLabel licenseConditionLabel ;
  private JButton licenseButton ;
  private JCheckBox licenseCheck ;
  private JLabel expectedTime ;

  private Boolean proxyUsed;

  private Boolean socksProxyUsed;
  private String socksProxyAddress;
  private Integer socksProxyPort;
  private Boolean httpProxyUsed;
  private String httpProxyAddress;
  private Integer httpProxyPort;

  private Boolean proxyAuthentication;
  private String proxyUsername;
  private String proxyPassword;

  private final Object estimateMonitor = new Object();
  private Integer estimatedTime = null;

  private Usr_LoginSecSess_Rq login_request;
  private Usr_NewUsr_Rq newUser_request;

  private int keyLength = KeyRecord.KEY_LENGTH_DEFAULT;
  private int certainty = KeyRecord.CERTAINTY_DEFAULT;
  private boolean storeRemoteFlag = true;
  private File localPrivKeyFile = null;

  private RSAKeyPair keyPair;
  private int usedKeyLength = -1;
  private int usedCertainty = -1;

  // Error messages
  public static final String RETYPE_PASSWORD_ERROR = com.CH_cl.lang.Lang.rb.getString("msg_Re-typed_Password_does_not_match...");
  private static final String EMAIL_NULL = com.CH_cl.lang.Lang.rb.getString("msg_Proceed_without_Email_address...");

  private static String OK_BUTTON_LOGIN_MODE = com.CH_cl.lang.Lang.rb.getString("button_Sign_In");
  private static String OK_BUTTON_NEW_ACCOUNT_MODE = com.CH_cl.lang.Lang.rb.getString("button_Register");
  private TypeAheadPopupList typeAheadPopupList;

  private JWindow keyGenSplash = null;

  public static String defaultServer;
  public static String defaultPassword;
  public static String defaultMode;
  public static String defaultSignupEmail;

  private boolean isUsernameForRetry;
  private boolean isUsernameInRetry;
  private String usernamePreRetry;

  // We'll publish this GUI for the progress monitor to attach to... and clean it up when we are done.
  public static JFrame loginFrameForProgress = null;

  /** Creates new LoginFrame */
  public LoginFrame(LoginCoordinatorI loginCoordinator, Window splashWindow) {
    super(com.CH_cl.lang.Lang.rb.getString("title_Sign_In"));

    if (KeyRecord.DEBUG__ALLOW_SHORT_KEYS) {
      keyLength = KeyRecord.DEBUG__SHORTEST_KEY;
      certainty = KeyRecord.DEBUG__CERTAINTY_MIN;
    }

    this.loginCoordinator = loginCoordinator;

    // Icon for the Frame
    ImageIcon frameIcon = Images.get(ImageNums.FRAME_LOCK32);
    if (frameIcon != null) {
      try {
        setIconImage(frameIcon.getImage());
      } catch (NoSuchMethodError e) {
        // API since 1.6!!! - ignore it as it is not crytical
      }
    }

    this.defaultUserName = GlobalProperties.getProperty(GlobalProperties.PROPERTY_USER_NAME, DEFAULT_USER_NAME);
    this.defaultUserNameList = getUserList();
    this.defaultRememberUserName = getRememberUserNameProperty();

    if (GeneralDialog.getDefaultParent() == null)
      GeneralDialog.setDefaultParent(this);

    jLogo = new JMyLabel(Images.get(ImageNums.LOGO_KEY_MAIN));
    JComponent jLogoHeader = MiscGui.createLogoHeader(jLogo);

    JButton[] buttons = createButtons(false);
    this.mainPanel = createMainPanel();
    // add buttons panel indented the same amount as username field
    Dimension usernameLabelDim = jUsernameLabel.getPreferredSize();
    int usernameLabelWidth = usernameLabelDim.width;
    int indentAmount = usernameLabelWidth + 5 + 5; // 5 + 5 are the indents between username label and field
    this.mainPanel.add(MiscGui.createButtonPanel(buttons, new EmptyBorder(0,indentAmount,0,indentAmount), BorderLayout.CENTER), new GridBagConstraints(1, 50, 4, 1, 0, 0,
            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 8, 5), 0, 0));

    JButton defaultButton = buttons[DEFAULT_BUTTON_INDEX];
    getRootPane().setDefaultButton(defaultButton);

    getContentPane().add("North", jLogoHeader);
    if (getToolkit().getScreenSize().height >= 600) {
      getContentPane().add("Center", mainPanel);
    } else {
      getContentPane().add("Center", new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
    }

    // connect 'X' window button to cancel
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        closeFrame();
        LoginFrame.this.loginCoordinator.loginComplete(MainFrame.getServerInterfaceLayer(), false);
      }
    });

    pack();

    MiscGui.setSuggestedWindowLocation(null, this);

    if (splashWindow != null && splashWindow.isShowing()) {
      splashWindow.setVisible(false);
      splashWindow.dispose();
    }

    if (defaultMode != null && defaultMode.equalsIgnoreCase(MODE_SIGNUP)) {
      changeModeToNewAccount();
    }
    if (defaultPassword != null) {
      // if password needs to be entered, dialog will show with password field in focus
      password.addHierarchyListener(new InitialFocusRequestor());
      // if no errors on the dialog
      if (isInputValid() == null) {
        new OKActionListener().actionPerformed(null);
      } else {
        setVisible(true);
        toFront();
      }
    } else {
      setVisible(true);
      toFront();
    }

    // start initializing the GUIs
    loginCoordinator.startPreloadingComponents_Threaded();

    // Start initializing secure random, we'll need it for login
    Rnd.initSecureRandom();
  }

  /* @return true is if this is a new account dialog */
  private boolean isNewAccountRequested() {
    return isNewAccountDialog;
  }

  /* @return request for login */
  private Usr_LoginSecSess_Rq getLoginRequestAndClear() {
    Usr_LoginSecSess_Rq request = login_request;
    login_request = null;
    return request;
  }

  /* @return request for creating new account */
  private Usr_NewUsr_Rq getNewUserRequest() {
    return newUser_request;
  }

  /* @return encoded password entered by the user */
  private BAEncodedPassword getBAEncodedPassword() {
    return UserRecord.getBAEncodedPassword(password.getPassword(), getUserName());
  }

  private void setEnabledInputs(boolean b) {
    userName.setEnabled(b);
    password.setEnabled(b);
    if (retypePassword != null)
      retypePassword.setEnabled(b);
    if (currentEmail != null)
      currentEmail.setEnabled(b);
    if (accountCode != null)
      accountCode.setEnabled(b);
    if (serverCombo != null)
      serverCombo.setEnabled(b);
    okButton.setEnabled(b);
    if (cancelButton != null)
      cancelButton.setEnabled(b);
    if (advancedButton != null)
      advancedButton.setEnabled(b);
    rememberUserName.setEnabled(b);
    if (switchModeButton != null)
      switchModeButton.setEnabled(isNewAccountOptionEnabled && b);
    if (licenseButton != null)
      licenseButton.setEnabled(b);
    if (licenseCheck != null)
      licenseCheck.setEnabled(b);
    invalidate();
    validate();
  }

  public void setPassword(String pass) {
    password.setText(pass);
  }

  public void setUsername(String name) {
    userName.setText(name);
  }

  /* Change dialog to Login Dialog */
  private void changeModeToLogin() {

    isNewAccountDialog = false;
    setTitle(com.CH_cl.lang.Lang.rb.getString("title_Sign_In"));
//    switchModeLink.setText("Sign up for a new account");
    userName.selectAll();
    userName.requestFocusInWindow();
    passwordLabel.setText(com.CH_cl.lang.Lang.rb.getString("label_Password"));
    recoveryLabel.setText("Forgot your password?");
    recoveryLabel.setVisible(true);
    if (switchModeButton != null) {
      switchModeButton.setText(com.CH_cl.lang.Lang.rb.getString("button_Register"));
      switchModeButton.setEnabled(isNewAccountOptionEnabled);
    }
    okButton.setText(OK_BUTTON_LOGIN_MODE);

    jLogo.setIcon(Images.get(ImageNums.LOGO_KEY_MAIN));

    /*
//    newEmailLabel.setVisible(false);
//    newEmail.setVisible(false);
    passwordConditionLabel.setVisible(false);
    reTypeLabel.setVisible(false);
    retypePassword.setVisible(false);
    currentEmailLabel.setVisible(false);
    currentEmail.setVisible(false);
    advancedLabel.setVisible(false);
    advancedButton.setVisible(false);
    accountCodeLabel.setVisible(false);
    accountCode.setVisible(false);
    licenseConditionLabel.setVisible(false);
    licenseButton.setVisible(false);
    licenseCheck.setVisible(false);
    expectedTime.setVisible(false);
    */

//    mainPanel.remove(newEmailLabel);
//    mainPanel.remove(newEmail);
    if (passwordConditionLabel != null)
      mainPanel.remove(passwordConditionLabel);
    mainPanel.remove(reTypeLabel);
    mainPanel.remove(retypePassword);
    mainPanel.remove(currentEmailLabel);
    mainPanel.remove(currentEmail);
    mainPanel.remove(advancedLabel);
    mainPanel.remove(advancedButton);
    mainPanel.remove(accountCodeLabel);
    mainPanel.remove(accountCode);
    mainPanel.remove(licenseConditionLabel);
    mainPanel.remove(licenseButton);
    mainPanel.remove(licenseCheck);
    mainPanel.remove(expectedTime);

    versionLabel.setVisible(true);

    this.pack();
    MiscGui.adjustSizeAndLocationToFitScreen(this);

    // Always enable the ok Button, check for validity of input alter its pressed.
    //okButton.setEnabled(isInputValid());
  }

  /** Change dialog to Create New Account Dialog.
    * Add "Retype password" and "Enter email" fields plus labels
    */
  private void changeModeToNewAccount() {
    if (passwordConditionLabel == null)
      passwordConditionLabel = getPasswordHint();
    if (reTypeLabel == null)
      reTypeLabel = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Re-type_Password"));
    if (retypePassword == null) {
      retypePassword = new JMyPasswordKeyboardField(com.CH_cl.lang.Lang.rb.getString("actionTip_Use_Virtual_Keyboard_for_key-less_entry."));
    }
    if (currentEmailLabel == null)
      currentEmailLabel = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Current_Email_Address"));
    if (currentEmail == null)
      currentEmail = new JMyTextField(defaultSignupEmail);
    if (advancedLabel == null)
      advancedLabel = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Advanced_Options"));
    if (advancedButton == null) {
      advancedButton = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Customize..."));
      advancedButton.addActionListener(new AdvancedListener());
    }
    if (accountCodeLabel == null) {
      try {
        accountCodeLabel = new JMyLinkLikeLabel(com.CH_cl.lang.Lang.rb.getString("label_Activation_Code"), 0);
        accountCodeLabel.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent event) {
            try {
              BrowserLauncher.openURL(URLs.get(URLs.ACTIVATION_CODE_PAGE));
            } catch (Throwable t) {
            }
          }
        });
      } catch (Throwable t) {
        accountCodeLabel = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Activation_Code"));
      }
    }
    if (accountCode == null) {
      accountCode = new JMyTextField();
      //accountCode.setBackgroundTextWhenEmpty(" leave empty for a free account with ads");
      accountCode.setText(URLs.get(URLs.ACTIVATION_CODE_DEFAULT));
    }
    if (expectedTime == null)
      expectedTime = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Key_Generation_will_take_approximately"));

    if (licenseConditionLabel == null)
      licenseConditionLabel = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Please_read_and_indicate_your_acceptance_of_the_License_Agreement..."));
    if (licenseButton == null) {
      licenseButton = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Read_License_Agreement"));
      licenseButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          new LicenseDialog(LoginFrame.this);
        }
      });
    }
    if (licenseCheck == null) {
      licenseCheck = new JMyCheckBox(com.CH_cl.lang.Lang.rb.getString("check_I_Accept"), false);
      licenseCheck.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          // Always enable the ok Button, check for validity of input alter its pressed.
          //okButton.setEnabled(isInputValid());
        }
      });
    }

    isNewAccountDialog = true;
    setTitle(com.CH_cl.lang.Lang.rb.getString("title_Register"));
//    switchModeLink.setText("Sign in to an existing account");
    userName.selectAll();
    userName.requestFocusInWindow();
    passwordLabel.setText(com.CH_cl.lang.Lang.rb.getString("label_Unique_Password"));
    recoveryLabel.setText("To setup optional Password Recovery settings click here.");
    recoveryLabel.setVisible(false);
    retypePassword.setText("");
    licenseCheck.setSelected(false);
    if (switchModeButton != null) {
      switchModeButton.setText(com.CH_cl.lang.Lang.rb.getString("button_Switch_to_Sign_In"));
      switchModeButton.setEnabled(true);
    }

    okButton.setText(OK_BUTTON_NEW_ACCOUNT_MODE);

    jLogo.setIcon(Images.get(ImageNums.LOGO_BANNER_MAIN));

//    mainPanel.add(newEmailLabel, new GridBagConstraints(1, 2, 1, 1, 0, 0, 
//        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));
//
//    mainPanel.add(newEmail, new GridBagConstraints(2, 2, 2, 1, 10, 0, 
//        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));
//
//    mainPanel.add(passwordConditionLabel, new GridBagConstraints(1, 3, 3, 1, 10, 0, 
//        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));

    mainPanel.add(reTypeLabel, new GridBagConstraints(1, 6, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));

    mainPanel.add(retypePassword, new GridBagConstraints(2, 6, 3, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));

    if (defaultSignupEmail != null && EmailRecord.isEmailFormatValid(defaultSignupEmail)) {
      currentEmailLabel.setVisible(false);
      currentEmail.setVisible(false);
    }
    mainPanel.add(currentEmailLabel, new GridBagConstraints(1, 7, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));

    mainPanel.add(currentEmail, new GridBagConstraints(2, 7, 3, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));

    // Account code wigets
    if (URLs.get(URLs.ACTIVATION_CODE_FIELD_REMOVED, "false").equalsIgnoreCase("true")) {
      // activation code items removed
    } else {
      mainPanel.add(accountCodeLabel, new GridBagConstraints(1, 8, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));

      mainPanel.add(accountCode, new GridBagConstraints(2, 8, 3, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));
    }

    // Advanced options for key generation, etc...
    mainPanel.add(advancedLabel, new GridBagConstraints(1, 12, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));

    mainPanel.add(advancedButton, new GridBagConstraints(2, 12, 3, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(3, 5, 3, 5), 0, 0));

    // License wigets
    mainPanel.add(licenseConditionLabel, new GridBagConstraints(1, 13, 4, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));

    mainPanel.add(licenseCheck, new GridBagConstraints(1, 14, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(3, 5, 3, 5), 0, 0));

    mainPanel.add(licenseButton, new GridBagConstraints(2, 14, 3, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(3, 5, 3, 5), 0, 0));

    // time estimate
    mainPanel.add(expectedTime, new GridBagConstraints(1, 15, 4, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));

    // Password Length Warning
    if (passwordConditionLabel != null) {
      mainPanel.add(passwordConditionLabel, new GridBagConstraints(1, 16, 4, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 20, 20));
    }

    versionLabel.setVisible(false);

    updateKeyGenerationTimeThreaded();

    setSize(getSize().width, getPreferredSize().height);
    MiscGui.adjustSizeAndLocationToFitScreen(this);
    validate();
  }

  private void updateKeyGenerationTimeThreaded() {
    Thread t = new ThreadTraced("KeyGenerationTimeEstimator") {
      public void runTraced() {
        synchronized (estimateMonitor) {
          if (estimatedTime == null) {
            expectedTime.setText(com.CH_cl.lang.Lang.rb.getString("label_Estimating_Key_Generation_time..."));
            int expecTime = estimateKeyGenerationTimeNow(keyLength, certainty)+1;
            expectedTime.setText(java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("label_Key_Generation_will_take_approximately_###_seconds."), new Object[] {new Integer(expecTime)}));
            estimatedTime = new Integer(expecTime);
          }
        }
      }
    };
    t.setDaemon(true);
    t.start();
  }
  private int estimateKeyGenerationTimeNow(int keyLength, int certainty) {
    synchronized (estimateMonitor) {
      if (estimatedTime == null) {
        estimatedTime = new Integer(RSAKeyPairGenerator.estimateGenerationTime(keyLength, certainty));
      }
      return estimatedTime.intValue();
    }
  }


  public static JLabel getPasswordHint() {
    JLabel warningLabel = null;
    if (ENABLE_PASSWORD_LENGTH_NOTE && ENABLE_PASSWORD_NON_RECOVERY_WARNING)
      throw new IllegalStateException("Please use one warning note only!");
    if (ENABLE_PASSWORD_LENGTH_NOTE) {
      warningLabel = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Password_length_recommendation..."));
    } else if (ENABLE_PASSWORD_NON_RECOVERY_WARNING) {
      warningLabel = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Password_non-recovery_warning..."));
    }
    if (warningLabel != null) {
      warningLabel.setIcon(Images.get(ImageNums.SHIELD32));
      warningLabel.setBorder(new LineBorder(warningLabel.getBackground().darker(), 1, true));
      warningLabel.setHorizontalAlignment(JLabel.LEFT);
      warningLabel.setVerticalTextPosition(JLabel.TOP);
    }
    return warningLabel;
  }

  /**
  * Private helper to fetch the server list as Vector of Strings.
  */
  private ArrayList getServerList() {
    String serversStr = defaultServer != null ? defaultServer : GlobalProperties.getProperty(PROPERTY_SERVER_LIST);
    ArrayList al = new ArrayList();
    StringTokenizer st = new StringTokenizer(serversStr);
    while (st.hasMoreTokens()) {
      String serverS = st.nextToken();
      Object[] server = Misc.parseHostAndPort(serverS);
      if (server != null) {
        al.add(getServerStr(server));
      }
    }
    return al;
  }
  private static String getServerStr(Object[] server) {
    String str = server[0].toString();
    // ommit port if regular (80)
    if (((Integer) server[1]).intValue() != 80)
      str += ":" + server[1].toString();
    return str;
  }

  private static String[] getUserList() {
    String usersStr = GlobalProperties.getProperty(PROPERTY_USER_NAME_LIST, "");
    ArrayList usersL = new ArrayList();
    StringTokenizer st = new StringTokenizer(usersStr);
    while (st.hasMoreTokens()) {
      String userS = st.nextToken();
      String userName = Misc.escapeWhiteDecode(userS);
      usersL.add(userName);
    }
    String[] userList = new String[usersL.size()];
    if (usersL.size() > 0)
      usersL.toArray(userList);
    return userList;
  }

  public static String[] putUserList(String additionalMostRecentUsername, String removeUsername) {
    return putUserList(getUserList(), additionalMostRecentUsername, removeUsername);
  }
  private static String[] putUserList(String[] userList, String additionalMostRecentUsername) {
    return putUserList(userList, additionalMostRecentUsername, null);
  }
  private static String[] putUserList(String[] userList, String additionalMostRecentUsername, String removeUsername) {
    ArrayList usersL = new ArrayList(Arrays.asList(userList));
    if (removeUsername != null && removeUsername.trim().length() > 0) {
      String toRemove = removeUsername.trim();
      usersL.remove(toRemove);
    }
    if (additionalMostRecentUsername != null && additionalMostRecentUsername.trim().length() > 0) {
      String toAdd = additionalMostRecentUsername.trim();
      usersL.remove(toAdd);
      usersL.add(0, toAdd);
      // trim list to max 100 entries
      while (usersL.size() > 100)
        usersL.remove(usersL.size()-1);
      // set the default username property too
      GlobalProperties.setProperty(GlobalProperties.PROPERTY_USER_NAME, toAdd);
    }
    StringBuffer usersStr = new StringBuffer();
    for (int i=0; i<usersL.size(); i++) {
      if (i > 0)
        usersStr.append(' ');
      usersStr.append(Misc.escapeWhiteEncode(usersL.get(i).toString()));
    }
    GlobalProperties.setProperty(PROPERTY_USER_NAME_LIST, usersStr.toString());
    userList = new String[usersL.size()];
    if (usersL.size() > 0)
      usersL.toArray(userList);
    return userList;
  }

  /**
  * Initiates last saved proxy settings.
  */
  private void initiateProxySettings() {
    // Fetch Proxy settings -- use try-catch blocks incase properties are corrupted or blank
    try {
      proxyUsed = Boolean.valueOf(GlobalProperties.getProperty("ProxyUsed", "false"));
    } catch (Throwable t) {
      proxyUsed = Boolean.FALSE;
    }
    try {
      socksProxyUsed = Boolean.valueOf(GlobalProperties.getProperty("SocksProxyUsed", "false"));
      socksProxyAddress = GlobalProperties.getProperty("SocksProxyAddress", "");
      socksProxyPort = Integer.valueOf(GlobalProperties.getProperty("SocksProxyPort", "1080"));
    } catch (Throwable t) {
      socksProxyUsed = Boolean.FALSE;
      socksProxyAddress = "";
      socksProxyPort = new Integer(1080);
    }
    try {
      httpProxyUsed = Boolean.valueOf(GlobalProperties.getProperty("HttpProxyUsed", "false"));
      httpProxyAddress = GlobalProperties.getProperty("HttpProxyAddress", "");
      httpProxyPort = Integer.valueOf(GlobalProperties.getProperty("HttpProxyPort", "80"));
    } catch (Throwable t) {
      httpProxyUsed = Boolean.FALSE;
      httpProxyAddress = "";
      httpProxyPort = new Integer(80);
    }
    try {
      proxyAuthentication = Boolean.valueOf(GlobalProperties.getProperty("ProxyAuthentication", "false"));
      proxyUsername = GlobalProperties.getProperty("ProxyUsername", "");
      proxyPassword = GlobalProperties.getProperty("ProxyPassword", "");
    } catch (Throwable t) {
      proxyAuthentication = Boolean.FALSE;
      proxyUsername = "";
      proxyPassword = "";
    }
    try {
      // enable proxy usage if any proxy type is selected to make it compatible with old client versions
      if (proxyUsed.equals(Boolean.FALSE)) {
        if (socksProxyUsed.equals(Boolean.TRUE) || httpProxyUsed.equals(Boolean.TRUE))
          proxyUsed = Boolean.TRUE;
      }
      if (proxyUsed.booleanValue()) {
        applyProxySettings();
      }
    } catch (Throwable t) {
    }
  }

  /**
  * Applies current proxy settings into system properties.
  */
  private void applyProxySettings() {
    Properties prop = System.getProperties();
    prop.remove("socksProxyHost");
    prop.remove("socksProxyPort");
    if (proxyUsed.equals(Boolean.TRUE)) {
      if (socksProxyUsed.equals(Boolean.TRUE)) {
        prop.put("socksProxyHost", socksProxyAddress);
        prop.put("socksProxyPort", socksProxyPort.toString());
      }
    }
    prop.remove("proxySet");
    prop.remove("http.proxyHost");
    prop.remove("http.proxyPort");
    prop.remove("proxyHost");
    prop.remove("proxyPort");
    prop.remove("firewallSet");
    prop.remove("firewallHost");
    prop.remove("firewallPort");
    prop.remove("httpProxyHost");
    prop.remove("httpProxyPort");
    if (proxyUsed.equals(Boolean.TRUE)) {
      if (httpProxyUsed.equals(Boolean.TRUE)) {
        prop.put("proxySet", "true");
        prop.put("http.proxyHost", httpProxyAddress);
        prop.put("http.proxyPort", httpProxyPort.toString());
        prop.put("proxyHost", httpProxyAddress);
        prop.put("proxyPort", httpProxyPort.toString());
        prop.put("firewallSet", "true");
        prop.put("firewallHost", httpProxyAddress);
        prop.put("firewallPort", httpProxyPort.toString());
        prop.put("httpProxyHost", httpProxyAddress);
        prop.put("httpProxyPort", httpProxyPort.toString());
      }
    }
    if (proxyAuthentication.equals(Boolean.TRUE)) {
      Authenticator.setDefault(new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
//          System.out.println("Protocol: " + getRequestingProtocol() ); // getRequestingPrompt() was "SOCKS authentication"
//          System.out.println("Prompt: " + getRequestingPrompt() ); 
//          System.out.println("Username: " + proxyUsername); 
//          System.out.println("Passowrd: " + proxyPassword); 
          return new PasswordAuthentication(proxyUsername, proxyPassword.toCharArray());
        }
      });
    } else {
      Authenticator.setDefault(null);
    }
    System.setProperties(prop);
  }

  /**
  * @return currently selected server - the array that includes Server name as String and port number as Integer.
  */
  private Object[] getServer() {
    return Misc.parseHostAndPort(getServerStr());
  }
  private String getServerStr() {
    String serverStr = null;
    if (serverCombo != null)
      serverStr = (String) serverCombo.getSelectedItem();
    else
      serverStr = (String) getServerList().get(0);
    return serverStr;
  }


  /**
  * Put the new server list and proxy servers to Global Properties.
  */
  private void putServerListAndProxySettings() {
    Object[] server = getServer();
    if (server != null) {
      if (defaultServer == null) {
        String serverStr = getServerStr(server);
        StringBuffer serverListSB = new StringBuffer();
        serverListSB.append(server[0].toString());
        serverListSB.append(':');
        serverListSB.append(server[1]);
        serverListSB.append(' ');

        ArrayList serverList = getServerList();
        for (int i=0; i<serverList.size(); i++) {
          if (i>10)
            break;
          String item = (String) serverList.get(i);
          if (!item.equals(serverStr)) {
            server = Misc.parseHostAndPort(item);
            if (server != null) {
              serverListSB.append(server[0].toString());
              serverListSB.append(':');
              serverListSB.append(server[1]);
              serverListSB.append(' ');
            }
          }
        }
        GlobalProperties.setProperty(PROPERTY_SERVER_LIST, serverListSB.toString());
      }

      // Put the proxy config there too together with the server list.
      GlobalProperties.setProperty("ProxyUsed", proxyUsed.toString());

      GlobalProperties.setProperty("SocksProxyUsed", socksProxyUsed.toString());
      GlobalProperties.setProperty("SocksProxyAddress", socksProxyAddress != null ? socksProxyAddress : "");
      GlobalProperties.setProperty("SocksProxyPort", socksProxyPort.toString());

      GlobalProperties.setProperty("HttpProxyUsed", httpProxyUsed.toString());
      GlobalProperties.setProperty("HttpProxyAddress", httpProxyAddress != null ? httpProxyAddress : "");
      GlobalProperties.setProperty("HttpProxyPort", httpProxyPort.toString());

      GlobalProperties.setProperty("ProxyAuthentication", proxyAuthentication.toString());
      GlobalProperties.setProperty("ProxyUsername", proxyUsername != null ? proxyUsername : "");
      GlobalProperties.setProperty("ProxyPassword", proxyPassword != null ? proxyPassword : "");
    }
  }

  /** This method sets the main mainPanel using GridBagLayout to place components **/
  private JPanel createMainPanel() {
    JPanel panel = new JPanel();

//    switchModeLink = new JMyLinkLikeLabel("Sign up for a new account", -1);
//    switchModeLink.addMouseListener(new MouseAdapter() {
//      public void mouseClicked(MouseEvent event) {
//        if (isNewAccountRequested()) {
//          changeModeToLogin();
//        } else {
//          changeModeToNewAccount();
//        }
//      }
//    });

    userName = new JMyTextField(defaultUserName, 20);
    typeAheadPopupList = new TypeAheadPopupList(new ObjectsProviderUpdaterI() {
      public Object[] provide(Object args) {
        return defaultUserNameList;
      }
      public Object[] provide(Object args, ListUpdatableI updatable) {
        return defaultUserNameList;
      }
      public void registerForUpdates(ListUpdatableI updatable) {
      }
      public void disposeObj() {
      }
    }, true);
    userName.addKeyListener(typeAheadPopupList);
    password = new JMyPasswordKeyboardField(com.CH_cl.lang.Lang.rb.getString("actionTip_Use_Virtual_Keyboard_for_key-less_entry."), defaultPassword);
    try {
      boolean capsLockState = getToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
      capsLockWarning = new JMyLabel("Caps Lock is ON");
      capsLockWarning.setIcon(Images.get(ImageNums.WARNING16));
      capsLockWarning.setVisible(capsLockState);
      capsLockTimer = new Timer(100, new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          boolean shouldBeVisible = capsLockWarning.getToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
          if (shouldBeVisible != capsLockWarning.isVisible())
            capsLockWarning.setVisible(shouldBeVisible);
        }
      });
      capsLockTimer.start();
    } catch (Throwable t) {
      // not every OS/keyboard supports the CAPS LOCK key state
    }

//    recoveryButton = new JMyButtonNoFocus(com.CH_gui.lang.Lang.rb.getString("button_Recover"));
//    recoveryButton.setToolTipText(com.CH_gui.lang.Lang.rb.getString("actionTip_Recover_lost_password."));
//    recoveryButton.setFont(recoveryButton.getFont().deriveFont(10f));
//    recoveryButton.setBorder(new EmptyBorder(2,2,2,2));
//    recoveryButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//    recoveryButton.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent event) {
//      }
//    });

    // new account stuff
    passwordLabel = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Password"));
    recoveryLabel = new JMyLinkLikeLabel("Forgot your password?", -1);
    recoveryLabel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent event) {
        performDisconnect();
        performConnect(getServer());
        MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.SYS_Q_VERSION, new Obj_List_Co(new Object[] { new Float(GlobalProperties.PROGRAM_VERSION), new Short(GlobalProperties.PROGRAM_RELEASE), new Short(GlobalProperties.PROGRAM_BUILD_NUMBER)})));
        new UserSelectPassRecoveryDialog(LoginFrame.this, "Recover Password", userName.getText().trim());
      }
    });
    changeServerLabel = new JMyLinkLikeLabel("Change server", -1);
    changeServerLabel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent event) {
        boolean show = !serverLabel.isVisible();
        serverLabel.setVisible(show);
        serverCombo.setVisible(show);
        LoginFrame.this.pack();
      }
    });
    serverLabel = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Server"));
    serverCombo = new JMyComboBox();
    serverCombo.setEditable(true);
    proxySettingsLabel = new JMyLinkLikeLabel("Proxy", -1);
    proxySettingsLabel.addMouseListener(new MouseAdapter() {
      JLabel jSocksProxyUsed = null;
      JTextField jSocksProxyAddress = null;
      JTextField jSocksProxyPort = null;
      JLabel jHttpProxyUsed = null;
      JTextField jHttpProxyAddress = null;
      JTextField jHttpProxyPort = null;
      JCheckBox jProxyAuthentication = null;
      JTextField jProxyUsername = null;
      JPasswordField jProxyPassword = null;
      JCheckBox jProxyUsed = null;

      public void setEnablement() {
        boolean enabled = jProxyUsed.isSelected();
        //boolean enabledSocks = enabled & jSocksProxyUsed.isSelected();
        //boolean enabledHttp = enabled & jHttpProxyUsed.isSelected();
        boolean enabledSocks = enabled;
        boolean enabledHttp = enabled;
        boolean enabledAuthentication = enabled & jProxyAuthentication.isSelected();
        if (!enabled) {
          //jSocksProxyUsed.setSelected(false);
          //jHttpProxyUsed.setSelected(false);
          jProxyAuthentication.setSelected(false);
        }
        //jSocksProxyUsed.setEnabled(enabled);
        //jHttpProxyUsed.setEnabled(enabled);
        jProxyAuthentication.setEnabled(enabled);
        jSocksProxyAddress.setEnabled(enabledSocks);
        jSocksProxyPort.setEnabled(enabledSocks);
        jHttpProxyAddress.setEnabled(enabledHttp);
        jHttpProxyPort.setEnabled(enabledHttp);
        jProxyUsername.setEnabled(enabledAuthentication);
        jProxyPassword.setEnabled(enabledAuthentication);
      }
      public void mouseClicked(MouseEvent event) {
      //public void actionPerformed(ActionEvent event) {
        //jSocksProxyUsed = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Socks"), socksProxyUsed != null ? socksProxyUsed.booleanValue() : false);
        //jHttpProxyUsed = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Http"), httpProxyUsed != null ? httpProxyUsed.booleanValue() : false);
        jSocksProxyUsed = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("check_Socks"));
        jSocksProxyAddress = new JMyTextField(socksProxyAddress != null ? socksProxyAddress : "", 20);
        jSocksProxyPort = new JMyTextField(socksProxyPort != null ? socksProxyPort.toString() : "", 5);
        jHttpProxyUsed = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("check_Http"));
        jHttpProxyAddress = new JMyTextField(httpProxyAddress != null ? httpProxyAddress : "", 20);
        jHttpProxyPort = new JMyTextField(httpProxyPort != null ? httpProxyPort.toString() : "", 5);
        jProxyAuthentication = new JMyCheckBox("Use Proxy Authentication", proxyAuthentication != null ? proxyAuthentication.booleanValue() : false);
        jProxyUsername = new JMyTextField(proxyUsername != null ? proxyUsername : "", 20);
        jProxyPassword = new JPasswordField(proxyPassword != null ? proxyPassword : "", 5);
        jProxyUsed = new JMyCheckBox("Use Proxy Server", proxyUsed != null ? proxyUsed.booleanValue() : false);
        setEnablement();

        jProxyUsed.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setEnablement();
          }
        });
        /*
        jSocksProxyUsed.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setEnablement();
          }
        });
        */
        /*
        jHttpProxyUsed.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setEnablement();
          }
        });
        */
        jProxyAuthentication.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setEnablement();
          }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        int posY = 0;

        panel.add(com.CH_gui.usrs.AccountOptionsSignaturesPanel.makeDivider(com.CH_cl.lang.Lang.rb.getString("title_Proxy_server")), new GridBagConstraints(0, posY, 4, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
        posY ++;

        panel.add(jProxyUsed, new GridBagConstraints(0, posY, 4, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
        posY ++;

        panel.add(new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Server_Type")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 25, 2, 5), 0, 0));
        panel.add(new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Proxy_address_to_use")), new GridBagConstraints(1, posY, 1, 1, 10, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 2, 5), 0, 0));
        panel.add(new JMyLabel(), new GridBagConstraints(2, posY, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
        panel.add(new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Port")), new GridBagConstraints(3, posY, 1, 1, 5, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 2, 5), 0, 0));
        posY ++;

        panel.add(jSocksProxyUsed, new GridBagConstraints(0, posY, 1, 1, 0, 0,
            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(2, 25, 2, 5), 0, 0));
        panel.add(jSocksProxyAddress, new GridBagConstraints(1, posY, 1, 1, 0, 0,
            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 2, 5), 0, 0));
        panel.add(new JMyLabel(":"), new GridBagConstraints(2, posY, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
        panel.add(jSocksProxyPort, new GridBagConstraints(3, posY, 1, 1, 0, 0,
            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 2, 5), 0, 0));
        posY ++;

        panel.add(jHttpProxyUsed, new GridBagConstraints(0, posY, 1, 1, 0, 0,
            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(2, 25, 5, 5), 0, 0));
        panel.add(jHttpProxyAddress, new GridBagConstraints(1, posY, 1, 1, 0, 0,
            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 5, 5), 0, 0));
        panel.add(new JMyLabel(":"), new GridBagConstraints(2, posY, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
        panel.add(jHttpProxyPort, new GridBagConstraints(3, posY, 1, 1, 0, 0,
            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 5, 5), 0, 0));
        posY ++;

        panel.add(com.CH_gui.usrs.AccountOptionsSignaturesPanel.makeDivider("Proxy Authentication"), new GridBagConstraints(0, posY, 4, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 25, 5, 5), 0, 0));
        posY ++;

        panel.add(jProxyAuthentication, new GridBagConstraints(0, posY, 4, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 25, 5, 5), 0, 0));
        posY ++;

        panel.add(new JMyLabel("Username:"), new GridBagConstraints(0, posY, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 45, 2, 5), 0, 0));
        panel.add(jProxyUsername, new GridBagConstraints(1, posY, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 2, 5), 0, 0));
        posY ++;
        panel.add(new JMyLabel("Password:"), new GridBagConstraints(0, posY, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 45, 5, 5), 0, 0));
        panel.add(jProxyPassword, new GridBagConstraints(1, posY, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 5, 5), 0, 0));
        posY ++;


        JButton[] buttons = new JButton[] { new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_OK")) };

        final GeneralDialog dialog = new GeneralDialog(LoginFrame.this, com.CH_cl.lang.Lang.rb.getString("title_Proxy_Configuration"), buttons, 0, panel);
        buttons[0].addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            try {
              proxyUsed = Boolean.valueOf(jProxyUsed.isSelected());

              socksProxyAddress = jSocksProxyAddress.getText().trim();
              String socksProxyPortS = jSocksProxyPort.getText().trim();
              if (socksProxyPortS.length() > 0)
                socksProxyPort = Integer.valueOf(socksProxyPortS);
              if (jProxyUsed.isSelected()) {
                //socksProxyUsed = Boolean.valueOf(jSocksProxyUsed.isSelected() && socksProxyAddress.length() > 0 && socksProxyPortS.length() > 0);
                socksProxyUsed = Boolean.valueOf(socksProxyAddress.length() > 0 && socksProxyPortS.length() > 0);
              } else {
                socksProxyUsed = Boolean.FALSE;
              }

              httpProxyAddress = jHttpProxyAddress.getText().trim();
              String httpProxyPortS = jHttpProxyPort.getText().trim();
              if (httpProxyPortS.length() > 0)
                httpProxyPort = Integer.valueOf(httpProxyPortS);
              if (jProxyUsed.isSelected()) {
                //httpProxyUsed = Boolean.valueOf(jHttpProxyUsed.isSelected() && httpProxyAddress.length() > 0 && httpProxyPortS.length() > 0);
                httpProxyUsed = Boolean.valueOf(httpProxyAddress.length() > 0 && httpProxyPortS.length() > 0);
              } else {
                httpProxyUsed = Boolean.FALSE;
              }

              proxyUsername = jProxyUsername.getText().trim();
              proxyPassword = new String(jProxyPassword.getPassword());
              proxyAuthentication = Boolean.valueOf(jProxyAuthentication.isSelected() && (proxyUsername.length() > 0 || proxyPassword.length() > 0));
              applyProxySettings();
            } catch (Throwable t) {
            }
            dialog.closeDialog();
          }
        });
      }
    });

    // add other stored in properties servers
    ArrayList serverList = getServerList();
    for (int i=0; i<serverList.size(); i++) {
      String srvStr = (String) serverList.get(i);
      serverCombo.addItem(serverList.get(i));


      boolean isPresent = false;
      for (int k=0; k<serverCombo.getItemCount(); k++) {
        if (serverCombo.getItemAt(k).equals(srvStr)) {
          isPresent = true;
          break;
        }
      }
      if (!isPresent) {
        serverCombo.addItem(srvStr);
      }
    }
    // insert any default or PrivateLabel servers under the current selected item
    if (defaultServer == null) {
      insertServerChoice(URLs.get(URLs.DEFAULT_SERVER_1), 0, false);
      insertServerChoice(URLs.get(URLs.DEFAULT_SERVER_2), 1, false);
      insertServerChoice(URLs.get(URLs.DEFAULT_SERVER_3), 2, false);
    }
    initiateProxySettings();

//    newEmailLabel = new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Email_Address"));
//    newEmail = new JMyTextComboBox("", new String[] { "@"+URLs.get(URLs.DOMAIN_MAIL) });
//    newEmail.setEditable(true);

    String rememberStr = com.CH_cl.lang.Lang.rb.getString("check_Remember_my_Username_on_this_computer.");
    rememberUserName = new JMyCheckBox(rememberStr, defaultRememberUserName);

    versionLabel = new JMyLabel(GlobalProperties.PROGRAM_VERSION_STR);// + " b" + GlobalProperties.PROGRAM_BUILD_NUMBER);
    AffineTransform newAT = new AffineTransform();
    newAT.setToScale(0.8, 0.8); // make version text 80% of default font size
    AffineTransform oldAT = versionLabel.getFont().getTransform();
    AffineTransform modifiedAT = new AffineTransform(oldAT);
    modifiedAT.preConcatenate(newAT); // first original transform will be applied, then the new one
    versionLabel.setFont(versionLabel.getFont().deriveFont(modifiedAT));

    // combine remember username checkbox with version label
    JPanel rememberUserNamePanel = new JPanel();
    rememberUserNamePanel.setLayout(new GridBagLayout());
    rememberUserNamePanel.add(rememberUserName, new GridBagConstraints(1, 1, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0, 0, 0, 5), 0, 0));
    rememberUserNamePanel.add(versionLabel, new GridBagConstraints(2, 1, 1, 1, 0, 0,
        GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(0, 5, 0, 0), 0, 0));

    userName.selectAll();

    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    Keymap userMap = userName.getKeymap();
    userMap.removeKeyStrokeBinding(enter);

    ComboBoxEditor comboEditor = serverCombo.getEditor();
    Component comboEditorComp = comboEditor.getEditorComponent();
    if (comboEditorComp instanceof JTextField) {
      JTextField serverEditorField = (JTextField) comboEditorComp;
      Keymap serverMap = serverEditorField.getKeymap();
      serverMap.removeKeyStrokeBinding(enter);
    }

    panel.setBorder(BorderFactory.createEmptyBorder());
    panel.setLayout(new GridBagLayout());


//    mainPanel.add(jLogo, new GridBagConstraints(1, 0, 4, 1, 10, 0, 
//        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(0, 0, 5, 0), 0, 0));

//    mainPanel.add(switchModeLink, new GridBagConstraints(1, 0, 4, 1, 10, 0, 
//        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    jUsernameLabel = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Username"));
    panel.add(jUsernameLabel, new GridBagConstraints(1, 1, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(10, 5, 5, 5), 0, 0));

    panel.add(userName, new GridBagConstraints(2, 1, 3, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(10, 5, 5, 5), 0, 0));

    panel.add(passwordLabel, new GridBagConstraints(1, 4, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(password, new GridBagConstraints(2, 4, 3, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    if (capsLockWarning != null) {
      panel.add(capsLockWarning, new GridBagConstraints(2, 5, 3, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 5, 0, 5), 0, 0));
    }

    //if (URLs.get(URLs.SERVER_FIELD_REMOVED, "false").equalsIgnoreCase("true")) {
    // server field items removed
    serverLabel.setVisible(false);
    serverCombo.setVisible(false);
    //}
    panel.add(serverLabel, new GridBagConstraints(1, 9, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(serverCombo, new GridBagConstraints(2, 9, 3, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(recoveryLabel, new GridBagConstraints(2, 10, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0, 5, 5, 5), 0, 0));
    panel.add(proxySettingsLabel, new GridBagConstraints(3, 10, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0, 5, 5, 5), 0, 0));
    panel.add(changeServerLabel, new GridBagConstraints(4, 10, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0, 5, 5, 5), 0, 0));

    panel.add(rememberUserNamePanel, new GridBagConstraints(1, 11, 4, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));

//    mainPanel.add(versionLabel, new GridBagConstraints(4, 11, 1, 1, 0, 0, 
//        GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(3, 5, 3, 5), 0, 0));

    // filler
    panel.add(new JLabel(), new GridBagConstraints(1, 17, 4, 1, 10, 10,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));

    return panel;
  }

  private void insertServerChoice(String addSrv, int index, boolean isReplaceExisting) {
    if (addSrv != null && addSrv.length() > 0) {
      boolean shouldInsert = true;
      for (int i=0; i<serverCombo.getItemCount(); i++) {
        String srv = (String) serverCombo.getItemAt(i);
        if (srv.equalsIgnoreCase(addSrv)) {
          if (isReplaceExisting) {
            shouldInsert = true;
            serverCombo.removeItemAt(i);
          } else {
            shouldInsert = false;
          }
          break;
        }
      }
      if (shouldInsert) {
        int itemCount = serverCombo.getItemCount();
        if (itemCount <= index) {
          serverCombo.addItem(addSrv);
        } else {
          serverCombo.insertItemAt(addSrv, index);
        }
      }
    }
  }

  /* Create three buttons: "Login", "Cancel" and "Create New Account */
  private JButton[] createButtons(boolean includeCancel) {

    boolean includeSwitchModeButton = !URLs.get(URLs.NEW_ACCOUNT_BUTTON).equalsIgnoreCase("remove");
    isNewAccountOptionEnabled = URLs.get(URLs.NEW_ACCOUNT_BUTTON).equalsIgnoreCase("enable");

    ArrayList buttonsL = new ArrayList();

    okButton = new JMyButton(OK_BUTTON_LOGIN_MODE);
    okButton.setDefaultCapable(true);
    okButton.addActionListener(new OKActionListener());
    buttonsL.add(okButton);

    if (includeSwitchModeButton) {
      switchModeButton = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Register"));
      switchModeButton.setEnabled(isNewAccountOptionEnabled);
      switchModeButton.addActionListener(new NewAccountSignInActionListener());
      buttonsL.add(switchModeButton);
    }

    if (includeCancel) {
      cancelButton = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Cancel"));
      cancelButton.addActionListener(new CancelActionListener());
      buttonsL.add(cancelButton);
    }

    JButton[] buttons = new JButton[buttonsL.size()];
    buttonsL.toArray(buttons);

    return buttons;
  }

  public static boolean isPasswordValid(char[] pass) {
    return isPasswordPresent(pass);
  }
  private static boolean isPasswordPresent(char[] pass) {
    boolean isLenOk = false;
    if (pass.length >= MIN_PASSWORD_LENGTH) {
      isLenOk = true;
    }
    return isLenOk;
  }

  /**
  * @return error message if input is not valid, else return NULL for valid.
  */
  private String isInputValid() {
    String errorMsg = null;
    boolean rc = false;
    char[] pass1 = null;
    char[] pass2 = null;
    if (getUserName().length() > 0) {
      pass1 = password.getPassword();
      if (isPasswordPresent(pass1) || defaultPassword != null || pass1.length == 0) { // 0 len pass for login to new web accounts only
        if (!isNewAccountDialog || isPasswordValid(pass1)) {
          if (!isNewAccountDialog || Arrays.equals(pass1, pass2 = retypePassword.getPassword())) {
            if (!isNewAccountDialog || currentEmail.getText().trim().length() == 0 || EmailRecord.isEmailFormatValid(currentEmail.getText().trim())) {
              String serverS = getServerStr();
              if (Misc.parseHostAndPort(serverS) != null) {
                if (!isNewAccountDialog || licenseCheck.isSelected()) {
                  errorMsg = null;
                } else {
                  errorMsg = com.CH_cl.lang.Lang.rb.getString("msg_You_must_accept_the_License_Agreement...");
                }
              } else {
                errorMsg = com.CH_cl.lang.Lang.rb.getString("msg_Specified_Server_does_not_appear_to_be_in_a_valid_format...");
              }
            } else {
              errorMsg = com.CH_cl.lang.Lang.rb.getString("msg_Specified_email_address_does_not_appear_to_be_in_a_valid_format...");
            }
          } else {
            errorMsg = RETYPE_PASSWORD_ERROR;
          }
        } else {
          errorMsg = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("msg_Password_must_be_minimum_of_###_characters_long..."), new Object[] {new Integer(MIN_PASSWORD_LENGTH)});
        }
      } else {
        errorMsg = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("msg_Password_must_be_minimum_of_###_characters_long..."), new Object[] {new Integer(MIN_PASSWORD_LENGTH)});
      }
    } else {
      errorMsg = com.CH_cl.lang.Lang.rb.getString("msg_Username_must_have_at_least_1_non_blank_character...");
    }

    for (int i=0; pass1!=null && i<pass1.length; i++)
      pass1[i] = (char) 0;
    for (int i=0; pass2!=null && i<pass2.length; i++)
      pass2[i] = (char) 0;

    return errorMsg;
  }


  /**
  * Close and dispose of the frame.
  */
  public void closeFrame() {
    try {
      if (typeAheadPopupList != null) {
        typeAheadPopupList.disposeObj();
        typeAheadPopupList = null;
      }
    } catch (Throwable t) {
    }
    try {
      // reset defaults so when this window is used again for Switch Identity then it start up clean
      defaultPassword = null;
      defaultMode = null;
      defaultSignupEmail = null;
      setVisible(false);
    } catch (Throwable t) {
    }
    try {
      if (capsLockTimer != null)
        capsLockTimer.stop();
      dispose();
    } catch (Throwable t) {
    }
    // Nullify the frame reference for progress -- so it gets GC'd eventually.
    loginFrameForProgress = null;
  }


  private void pressedCancel() {
    newUser_request = null;
    login_request = null;
    closeFrame();
    loginCoordinator.loginComplete(MainFrame.getServerInterfaceLayer(), false);
  }


  public static File choosePrivKeyStorageFile(File defaultFileChoice) {
    File chosenFile = null;
    javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
    fc.setSelectedFile(defaultFileChoice);
    fc.setDialogTitle("Save Private Key file:");
    while (true) {
      int retVal = fc.showSaveDialog(null);
      if (retVal == javax.swing.JFileChooser.APPROVE_OPTION) {
        java.io.File file = fc.getSelectedFile();
        if (isPrivKeyFileChoiceValid(file)) {
          chosenFile = file;
          break;
        } else {
          continue; // retry the loop
        }
      } else {
        break;
      }
    }
    return chosenFile;
  }

  private static boolean isPrivKeyFileChoiceValid(File file) {
    boolean rc = true;
    if (file.exists()) {
      InputStream fileIn = null;
      boolean fileFormatValid = false;
      String errorMsg = null;
      try {
        Properties testLoad = new Properties();
        fileIn = new BufferedInputStream(new FileInputStream(file), 32*1024);
        testLoad.load(fileIn);
        fileIn.close();
        fileFormatValid = true;
      } catch (Exception e) {
        errorMsg = e.getLocalizedMessage();
      } finally {
        try { fileIn.close(); } catch (Exception e) { }
      }
      if (!fileFormatValid) {
        MessageDialog.showErrorDialog(null, "File verification failed.  Please choose a different file.\n\nFile:\n"+file.getAbsolutePath()+"\n\nReason:\n"+errorMsg, "File error", true);
        rc = false;
      } else {
        boolean appendOk = MessageDialog.showDialogYesNo(null, "Append to existing file?\n\nAre you sure you want to add this key to the already existing file?\n\n"+file.getAbsolutePath(), "Append to existing file?");
        if (!appendOk)
          rc = false;
      }
    } else {
      // test if file can be created
      boolean canCreate = false;
      String errorMsg = null;
      try {
        file.createNewFile();
        canCreate = true;
      } catch (Exception e) {
        errorMsg = e.getLocalizedMessage();
      } finally {
        try { file.delete(); } catch (Exception e) { }
      }
      if (!canCreate) {
        MessageDialog.showErrorDialog(null, "File could not be created.  Please choose a different file.\n\nFile:\n"+file.getAbsolutePath()+"\n\nReason:\n"+errorMsg, "File error", true);
        rc = false;
      }
    }
    return rc;
  }

  private class AdvancedListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      boolean prevStoreRemoteFlag = storeRemoteFlag;
      KeyGenerationOptionsDialog d = new KeyGenerationOptionsDialog(LoginFrame.this, keyLength, certainty, Boolean.valueOf(storeRemoteFlag));
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
        boolean tempStoreRemoteFlag = d.getStoreRemoteFlag().booleanValue();
        if (tempStoreRemoteFlag) {
          storeRemoteFlag = tempStoreRemoteFlag;
        } else if (prevStoreRemoteFlag != tempStoreRemoteFlag) {
          String userHome = System.getProperty("user.home");
          File privKeyFileDefault = userHome != null && userHome.length() > 0 ? new File(new File(userHome), "private-key.properties") : new File("private-key.properties");
          File privKeyFile = choosePrivKeyStorageFile(privKeyFileDefault);
          if (privKeyFile != null) {
            localPrivKeyFile = privKeyFile;
            storeRemoteFlag = tempStoreRemoteFlag;
          }
        }
      }
    }
  }

  private class CancelActionListener implements ActionListener {
    public void actionPerformed (ActionEvent event) {
      pressedCancel();
    }
  }

  private class NewAccountSignInActionListener implements ActionListener {
    /** Set appropriate fields and show create new account dialog */
    public void actionPerformed (ActionEvent event) {
      if (!isNewAccountRequested())
        changeModeToNewAccount();
      else
        changeModeToLogin();
    }
  }

  private class OKActionListener implements ActionListener {
    /** The request for either login or create new account is being set here.
      * All fields are checked and if not correctly filled by the user
      * appropriate error dialogs are displayed
      */
    public void actionPerformed (ActionEvent event) {
      String errorMsg = isInputValid();
      if (errorMsg != null) {
        LoginFrame.this.setVisible(true);
        MessageDialog.showErrorDialog(LoginFrame.this, errorMsg, com.CH_cl.lang.Lang.rb.getString("title_Invalid_Input"));
      } else {
        setEnabledInputs(false);
        // Run the long parts in a seperate thread
        new OKThread().start();
      }
    }
  }

  private class OKThread extends ThreadTraced {
    private JProgressBar jProgressBar;
    private int progressValue = 0;

    public OKThread() {
      super("LoginFrame OKThread");
      setDaemon(false);
    }
    public void runTraced() {
      // expose this frame for the login progress to attach
      loginFrameForProgress = LoginFrame.this;

      // keep track of the regular vs retry cycle
      isUsernameInRetry = isUsernameForRetry;
      isUsernameForRetry = false;

      // start clean
      login_request = null;
      newUser_request = null;

      boolean error = false;
      /* Set the login request -- required in all cases, even when new account is created, login must follow */
      UserRecord uRec = new UserRecord();
      uRec.handle = getUserName();
      BAEncodedPassword ba = getBAEncodedPassword();
      uRec.passwordHash = ba.getHashValue();
      ba.clearContent();

      String clientOS = "";
      String info = "" + System.getProperty("os.name");
      if (info == null || info.length() == 0) {
        info = System.getProperty("http.agent");
        if (info != null && info.length() > 0)
          clientOS = "b";
      }
      if (info != null) {
        info = info.toLowerCase();
        if (info.indexOf("win") >= 0)
          clientOS = "w";
        else if (info.indexOf("mac") >= 0)
          clientOS = "m";
        else if (info.indexOf("linux") >= 0)
          clientOS = "l";
        else if (info.indexOf("unix") >= 0)
          clientOS = "x";
        else
          clientOS = "-";
      }

      login_request = new Usr_LoginSecSess_Rq(uRec, 0, GlobalProperties.PROGRAM_VERSION, GlobalProperties.PROGRAM_RELEASE, true, defaultSignupEmail, clientOS);

      /* set new account request */
      if (isNewAccountDialog) {
        /* Empty re-type password field */
        char[] pass1 = password.getPassword();
        char[] pass2 = retypePassword.getPassword();
        /* Password and re-typed password do not match */
        if (!Arrays.equals(pass1, pass2)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              MessageDialog.showErrorDialog(LoginFrame.this, RETYPE_PASSWORD_ERROR, com.CH_cl.lang.Lang.rb.getString("title_Invalid_Input"));
            }
          });
          password.setText(""); retypePassword.setText("");
          error = true;
          password.requestFocusInWindow();
        }

        // clear password arrays
        for (int i=0; i<pass1.length; i++)
          pass1[i] = 0;
        for (int i=0; i<pass2.length; i++)
          pass2[i] = 0;

        // Standardise and validate email address
        String requestedEmailAddress = null;
        if (!error) {
          String eName = getUserName();
          if (eName.indexOf('@') >= 0)
            eName = eName.substring(0, eName.indexOf('@')).trim();
          eName = EmailRecord.validateEmailNickOrDomain(eName, null, null, true);
          if (eName == null) {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                MessageDialog.showErrorDialog(LoginFrame.this, "You entered an invalid Username.  Please choose another Username.", com.CH_cl.lang.Lang.rb.getString("title_Invalid_Input"));
              }
            });
            error = true;
          } else {
            requestedEmailAddress = eName + "@" + URLs.getElements(URLs.DOMAIN_MAIL)[0];
          }
        }

        // empty email warning
        if (!error && (currentEmail.getText() == null || currentEmail.getText().trim().length() == 0)) {
          String title = com.CH_cl.lang.Lang.rb.getString("title_No_Email_Specified");
          boolean option = MessageDialog.showDialogYesNo(LoginFrame.this, EMAIL_NULL, title);
          if (option == false) {
            error = true;
          }
        }

        if (!error) {
          // remind users to remember their Password
          JComponent splash = Template.getTemplate(Template.KEY_GEN);
          if (splash != null) {
            keyGenSplash = new JWindow();
            keyGenSplash.getContentPane().add(splash);
            keyGenSplash.setSize(600, 400);
            MiscGui.setSuggestedWindowLocation(LoginFrame.this, keyGenSplash);
            keyGenSplash.setVisible(true);
            keyGenSplash.toFront();
            try {
              // this is java 1.5 API call
              keyGenSplash.setAlwaysOnTop(true);
            } catch (Throwable t) {
            }
          }

          /* generate key pair */
          // remember key pair in case create account will fail due to already existing userId
          if (keyPair == null || usedKeyLength != keyLength || usedCertainty != certainty) {
            final int estSeconds = estimateKeyGenerationTimeNow(keyLength, certainty);
            jProgressBar = new JProgressBar(0, estSeconds);

            javax.swing.Timer timer = new javax.swing.Timer(1000, new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                progressValue ++;
                if (progressValue < estSeconds) {
                  jProgressBar.setValue(progressValue);
                }
              }
            });
            timer.start();

            mainPanel.remove(expectedTime);
            mainPanel.add(jProgressBar, new GridBagConstraints(1, 15, 4, 1, 10, 0,
              GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
            mainPanel.validate();

            keyPair = RSAKeyPairGenerator.generateKeyPair(keyLength, certainty);
            usedKeyLength = keyLength;
            usedCertainty = certainty;

            expectedTime.setText(com.CH_cl.lang.Lang.rb.getString("label_Key_Generation_has_completed_successfuly."));

            mainPanel.remove(jProgressBar);
            mainPanel.add(expectedTime, new GridBagConstraints(1, 15, 4, 1, 10, 0,
              GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
            mainPanel.validate();

            timer.stop();
          }

          // Create new user request
          newUser_request = createNewUserRequest(keyPair,
              getUserName(), login_request.userRecord.passwordHash,
              requestedEmailAddress,
              //null,
              currentEmail.getText().trim(),
              new Short((short) 1), null, null,
              null
              );

          // Hide the key generation window if showing..
          // Do it on a delay as the user will wait a while before application loads.
          if (keyGenSplash != null) {
            javax.swing.Timer splashWindowTimer = new javax.swing.Timer(5000, new ActionListener() {
              public void actionPerformed(ActionEvent event) {
                if (keyGenSplash != null)
                  keyGenSplash.dispose();
              }
            });
            splashWindowTimer.setRepeats(false);
            splashWindowTimer.start();
          }
        }
      }

      if (!error) {
        LoginFrame.this.setVisible(false);
        boolean isLoginSuccess = performLogin();
        if (isLoginSuccess) {
          // Restore any prior uploads giving some time to other supporting login queries to complete.
          // Delay is for smoother login experience, not necessary for any flow dependency.
          FileLobUp.restoreStateDelayed(3000);
          // dismiss GUI and complete
          LoginFrame.this.closeFrame();
          loginCoordinator.loginComplete(MainFrame.getServerInterfaceLayer(), true);
          UserRecord myUser = FetchedDataCache.getSingleInstance().getUserRecord();
          if (myUser != null)
            Stats.setStatus("Welcome "+myUser.handle);
        } else if (!isUsernameForRetry) {
          if (isUsernameInRetry) {
            setUsername(usernamePreRetry);
            usernamePreRetry = null;
          }
          LoginFrame.this.setVisible(true);
          userName.selectAll();
          password.requestFocusInWindow();
          password.getCaret().setVisible(true);
          // remove incorrect password entry
          password.setText("");
        }
      }

      // if error occurred than enable inputs
      // ALSO in case the dialog is reshown, inputs should be enabled -- so always enable inputs
      setEnabledInputs(true);
      if (!error && isUsernameForRetry) {
        // launch the action again and reset the flag -- optionally with user approval
        new OKActionListener().actionPerformed(null);
      }
    }
  }


  public static void performDisconnect() {
    ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
    if (SIL != null) {
      SIL.destroyServer();
      MainFrame.setServerInterfaceLayer(null);
    }
  }

  public static void performConnect(Object[] server) {
    if (MainFrame.getServerInterfaceLayer() == null) {
      Object[][] hostsAndPorts = null;
      try {
        // this is a complex call that involves IO so wrap it in try-catch
        hostsAndPorts = EngineFinder.queryServerForHostsAndPorts(server);
      } catch (Throwable t) {
        t.printStackTrace();
      }
      try {
        if (defaultServer == null) {
          hostsAndPorts = EngineFinder.addOrRemoveServer(hostsAndPorts, true, URLs.get(URLs.DEFAULT_SERVER_1));
          hostsAndPorts = EngineFinder.addOrRemoveServer(hostsAndPorts, true, URLs.get(URLs.DEFAULT_SERVER_2));
          hostsAndPorts = EngineFinder.addOrRemoveServer(hostsAndPorts, true, URLs.get(URLs.DEFAULT_SERVER_3));
          hostsAndPorts = EngineFinder.addOrRemoveServer(hostsAndPorts, false, URLs.get(URLs.DEFAULT_SERVER__PROHIBIT_DATA_CONNECTIONS_1));
        }
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        MainFrame.setServerInterfaceLayer(new ServerInterfaceLayer(cache, hostsAndPorts, true));
        FileLobUp.restoreStateDelayed(5000);
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  /** Process a login window with an option of creating new account,
    * if login or creating of new account fails, returns false.
    * Note: Login request follows creating new account request.
    * @return true if login successful, and user info and keys fetched to cache
    */
  private boolean performLogin() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginFrame.class, "performLogin()");

    boolean newAccountFailure = false;
    boolean loginSuccess = false;
    boolean isSuccess = false;
    boolean newAccountCreated = false;
    boolean isStoreRemoteFlag = false;
    long[] loginMsgStampBuffer = new long[] { 0 };

    // Notify main frame that login will now be attempted.
    loginCoordinator.loginAttemptCloseCurrentSession(MainFrame.getServerInterfaceLayer());

    // logout current session
    performDisconnect();

    // when login dialog is reshown due to previous failure, need to recheck the mode!
    if (isNewAccountRequested()) {
      Usr_NewUsr_Rq request = getNewUserRequest();
      /* User pressed "Cancel" button */
      if (request == null) {
        newAccountFailure = true;
      } else {
        performConnect(getServer());
        if (createNewAccount(request)) {
          newAccountCreated = true;
          isStoreRemoteFlag = storeRemoteFlag;
        } else {
          MainFrame.getServerInterfaceLayer().destroyServer();
          MainFrame.setServerInterfaceLayer(null);
          newAccountFailure = true;
        }
      }
    } // end if, new account created successfuly

    if (!newAccountFailure) {
      Usr_LoginSecSess_Rq request = getLoginRequestAndClear();
      ProgMonitorI loginProgMonitor = null;
      if (request != null) {
        // HTTP fetch server list and create ServerInterfaceLayer
        performConnect(getServer());
        // try to login...
        loginProgMonitor = ProgMonitorFactory.newInstanceLogin(com.CH_cl.lang.Lang.rb.getString("title_Secure_Sign_In"),
                new String[] {  com.CH_cl.lang.Lang.rb.getString("label_Open_Secure_Channel"),
                                com.CH_cl.lang.Lang.rb.getString("label_Retrieve_Account_Information"),
                                //com.CH_gui.lang.Lang.rb.getString("label_Load_Key_Pairs"),
                                com.CH_cl.lang.Lang.rb.getString("label_Load_Main_Program") },
                                isUsernameInRetry ? "Retrying with username: " + userName.getText() : null);
        loginCoordinator.setLoginProgMonitor(loginProgMonitor);
        loginSuccess = login(request, loginProgMonitor, loginMsgStampBuffer);
      }

      if (!loginSuccess) {      /* try again */
        // Destroy the last unsuccessful SIL (might not exist if more than
        // one Login dialog was used at the same time when 'switching identity')
        ServerInterfaceLayer prevSIL = MainFrame.getServerInterfaceLayer();
        if (prevSIL != null) prevSIL.destroyServer();
        MainFrame.setServerInterfaceLayer(null);
        if (isNewAccountRequested()) {
          changeModeToLogin();
        }
      } else {
        // Successful login requires save of the server list, proxy settings, and the username
        // so that the user does not have to re-enter it the next time.
        putServerListAndProxySettings();
        String uName = getUserName();
        boolean isRememberUserName = getRememberUserName();
        // remember the check box state
        GlobalProperties.setProperty(PROPERTY_REMEMBER_USER_NAME, ""+isRememberUserName);
        if (isRememberUserName) {
          defaultUserNameList = putUserList(defaultUserNameList, getUserName());
        } else {
          if (uName.equals(GlobalProperties.getProperty(GlobalProperties.PROPERTY_USER_NAME))) {
            // Remove the username only if the same one is specified, leave other's name if different.
            GlobalProperties.setProperty(GlobalProperties.PROPERTY_USER_NAME, DEFAULT_USER_NAME);
          }
          // remove the user from the type-ahead-popup list
          putUserList((String) null, getUserName());
        }

        // see if we have an activation code to send
        if (isNewAccountRequested()) {
          if (accountCode != null && accountCode.getText().trim().length() > 0) {
            Long userId = null; // NULL means my account
            String code = accountCode.getText().trim();
            MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.USR_Q_APPLY_CODE, new Obj_List_Co(new Object[] { userId, code })));
          }
        }

        // retry a few times in case connection breaks in the middle of the request we don't want login to fail
        for (int i=0; i<3; i++) {
          if (trace != null) trace.data(100, "fetching login info to cache try #" + (i+1));
          if (fetchLoginInfoToCache(loginProgMonitor, newAccountCreated, isStoreRemoteFlag, loginMsgStampBuffer[0])) {
            isSuccess = true;
            break;
          }
        }
      }
    } // end if !newAccountFailure

    if (trace != null) trace.exit(LoginFrame.class, isSuccess);
    return isSuccess;
  }


  private String getUserName() {
    return userName.getText().trim();
  }
  private boolean getRememberUserName() {
    return rememberUserName.isSelected();
  }
  public static boolean getRememberUserNameProperty() {
    return Boolean.valueOf(GlobalProperties.getProperty(PROPERTY_REMEMBER_USER_NAME, "true")).booleanValue();
  }


  /**
  * Submit and fetch request to login
  * @return true on success, false on failure
  */
  private boolean login(Usr_LoginSecSess_Rq request, ProgMonitorI loginProgMonitor, long[] loginMsgStampBuffer) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginFrame.class, "login(Usr_LoginSecSess_Rq request, ProgMonitorI loginProgMonitor)");
    MessageAction msgAction = new MessageAction(CommandCodes.USR_Q_LOGIN_SECURE_SESSION, request);
    if (loginMsgStampBuffer != null && loginMsgStampBuffer.length > 0)
      loginMsgStampBuffer[0] = msgAction.getStamp();

    boolean success = false;

    // login better be synchronized due to securing streams.
    ClientMessageAction replyAction = null;
    try {
      // register a Login Progress Monitor
      ProgMonitorPool.registerProgMonitor(loginProgMonitor, msgAction.getStamp());
      if (trace != null) trace.data(10, "advance progress monitor, login is being attempted");
      if (!loginProgMonitor.isAllDone())
        loginProgMonitor.nextTask();

      // Retry a few times in case connection breaks in the middle of the request we don't want login to fail.
      // It is possible to get back request when connection failed so we will retry in that case.
      for (int i=0; i<3; i++) {
        if (trace != null) trace.data(20, "login short loop", i);
        replyAction = MainFrame.getServerInterfaceLayer().submitAndFetchReply(msgAction, 90000);
        if (replyAction == null || replyAction.getActionCode() != msgAction.getActionCode()) break;
      }
      // If we are in the original typed-in-username cycle, check for failure and retry suggestion
      if (!isUsernameInRetry && replyAction != null && replyAction.getActionCode() == CommandCodes.USR_E_LOGIN_FAILED) {
        ProtocolMsgDataSet dataSet = replyAction.getMsgDataSet();
        if (dataSet instanceof Obj_List_Co) {
          Obj_List_Co objDataSet = (Obj_List_Co) dataSet;
          if (objDataSet.objs != null && objDataSet.objs.length >= 3 && objDataSet.objs[1] != null) {
            // Reset the message so it doesn't get displayed, we are handling it here with a automatic retry
            objDataSet.objs[0] = null;
            String retryHandle = (String) objDataSet.objs[1];
            usernamePreRetry = userName.getText();
            isUsernameForRetry = true;
            setUsername(retryHandle);
          }
        }
      }

      if (replyAction != null && replyAction.getActionCode() == CommandCodes.USR_A_LOGIN_SECURE_SESSION) {
        FetchedDataCache.getSingleInstance().setEncodedPassword(getBAEncodedPassword());
        success = true;

        // check version
        Usr_LoginSecSess_Rp reply = (Usr_LoginSecSess_Rp) replyAction.getMsgDataSet();
        if (EngineFinder.compareVersion(reply.serverVersion, reply.serverRelease,
            GlobalProperties.PROGRAM_VERSION, GlobalProperties.PROGRAM_RELEASE) > 0)
        {
          String newRelease = "";
          if (reply.serverRelease == GlobalProperties.PROGRAM_RELEASE_ALPHA)
            newRelease = com.CH_cl.lang.Lang.rb.getString("version_alpha");
          else if (reply.serverRelease == GlobalProperties.PROGRAM_RELEASE_BETA)
            newRelease = com.CH_cl.lang.Lang.rb.getString("version_beta");
          else if (reply.serverRelease == GlobalProperties.PROGRAM_RELEASE_FINAL)
            newRelease = "";

          String currentRelease = "";
          if (GlobalProperties.PROGRAM_RELEASE == GlobalProperties.PROGRAM_RELEASE_ALPHA)
            currentRelease = com.CH_cl.lang.Lang.rb.getString("version_alpha");
          else if (GlobalProperties.PROGRAM_RELEASE == GlobalProperties.PROGRAM_RELEASE_BETA)
            currentRelease = com.CH_cl.lang.Lang.rb.getString("version_beta");
          else if (GlobalProperties.PROGRAM_RELEASE == GlobalProperties.PROGRAM_RELEASE_FINAL)
            currentRelease = "";

          String hrefStart = "<a href=\""+URLs.get(URLs.DOWNLOAD_PAGE)+"\">";
          String hrefEnd = "</a>";
          String text = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("msg_Newer_version_is_now_available_for_download..."), new Object[] {String.valueOf(reply.serverVersion), newRelease, hrefStart, hrefEnd, String.valueOf(GlobalProperties.PROGRAM_VERSION), currentRelease, URLs.get(URLs.SERVICE_SOFTWARE_NAME), URLs.get(URLs.DOMAIN_WEB)});

          MessageDialog.showInfoDialog(null, text, com.CH_cl.lang.Lang.rb.getString("msgTitle_Newer_Version_Available"), false);
          try {
            Thread.sleep(5000);
          } catch (InterruptedException e) {
          }
        }
      }
      else {
        if (trace != null) trace.data(100, "closing prog monitor, login failed");
        loginProgMonitor.allDone();
      }

      // We don't want to disturb the login progress monitor, so we don't use the DefaultReplyRunner
      if (replyAction != null)
        replyAction.runAction();
    } catch (Exception e) {
      success = false;
      if (trace != null) trace.data(200, "closing prog monitor, exception during login");
      loginProgMonitor.allDone();
      MessageDialog.showErrorDialog(null, e.getMessage(), com.CH_cl.lang.Lang.rb.getString("msgTitle_Sign_In_Error"));
    }

    if (trace != null) trace.exit(LoginFrame.class, success);
    return success;
  }

  /* Submit and fetch request to fetch user info, keys, contacts, folders to the cache */
  private boolean fetchLoginInfoToCache(ProgMonitorI loginProgMonitor, boolean newAccountCreated, boolean storeRemoteFlag, long loginMsgStamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginFrame.class, "fetchLoginInfoToCache(ProgMonitorI loginProgMonitor, boolean newAccountCreated, boolean storeRemoteFlag)");
    if (trace != null) trace.args(loginProgMonitor);
    if (trace != null) trace.args(newAccountCreated);
    if (trace != null) trace.args(storeRemoteFlag);
    boolean success = false;
    MessageAction msgAction = new MessageAction(CommandCodes.USR_Q_GET_LOGIN_INFO, loginMsgStamp);

    if (trace != null) trace.data(10, "advance progress monitor, about to fetch login info");
    if (!loginProgMonitor.isAllDone())
      loginProgMonitor.nextTask();

    ClientMessageAction replyAction = null;

    // Retry a few times in case connection breaks in the middle of the request we don't want login to fail.
    // It is possible to get back request when connection failed so we will retry in that case.
    for (int i=0; i<3; i++) {
      if (trace != null) trace.data(20, "fetch login info short loop", i);
      replyAction = MainFrame.getServerInterfaceLayer().submitAndFetchReply(msgAction, 90000);
      if (replyAction == null || replyAction.getActionCode() != msgAction.getActionCode()) break;
    }

    if (replyAction != null && replyAction.getActionCode() == CommandCodes.SYS_A_REPLY_DATA_SETS) {
      success = true;
    } else {
      if (trace != null) trace.data(100, "closing prog monitor, failed to fetch login info");
      loginProgMonitor.allDone();
    }

    // We don't want to disturb the login progress monitor, so we don't use the DefaultReplyRunner
    if (replyAction != null)
      replyAction.runAction();

    // If this was creation of a new account, we must update the key record with encrypted private portion,
    // or store it locally.
    if (success && newAccountCreated) {
      FetchedDataCache cache = MainFrame.getServerInterfaceLayer().getFetchedDataCache();
      RSAPrivateKey rsaPrivKey = cache.getNewUserPrivateKey();
      if (rsaPrivKey != null) { // if we are retrying this function then it would be already null
        sendKeyUpdate(rsaPrivKey, storeRemoteFlag);
        cache.setNewUserPrivateKey(null);
      }
    }

    if (trace != null) trace.exit(LoginFrame.class, success);
    return success;
  }


  /* Submit and fetch request to fetch user info to the cache */
  /*
  private boolean fetchUserInfoToCache(LoginProgMonitor loginProgMonitor){
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginFrame.class, "fetchUserInfoToCache()");
    boolean success = false;
    MessageAction msgAction = new MessageAction(CommandCodes.USR_Q_GET_INFO, false);

    loginProgMonitor.nextTask();

    ClientMessageAction replyAction = MainFrame.getServerInterfaceLayer().submitAndFetchReply(msgAction, 0);

    if (replyAction.getActionCode() == CommandCodes.USR_A_GET_INFO) {
      success = true;
    }
    else {
      loginProgMonitor.closeProgMonitor();
    }

    // We don't want to disturb the login progress monitor, so we don't use the DefaultReplyRunner
    replyAction.runAction();
    if (trace != null) trace.exit(LoginFrame.class, success);
    return success;
  }
  */


  /* Submit and fetch request to fetch keys for the user to the cache */
  /*
  private boolean fetchKeysToCache(LoginProgMonitor loginProgMonitor, boolean newAccountCreated, boolean storeRemoteFlag) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginFrame.class, "fetchKeysToCache()");

    boolean success = false;
    MessageAction msgAction = new MessageAction(CommandCodes.KEY_Q_GET_KEY_PAIRS, false);
    loginProgMonitor.nextTask();

    ClientMessageAction replyAction = MainFrame.getServerInterfaceLayer().submitAndFetchReply(msgAction);

    if (replyAction.getActionCode() == CommandCodes.KEY_A_GET_KEY_PAIRS) {
      success = true;
    }
    else {
      // In case of error, hide the progress dialog first!
      loginProgMonitor.closeProgMonitor();
    }

    // We don't want to disturb the login progress monitor, so we don't use the DefaultReplyRunner
    replyAction.runAction();

    // not done yet, wait for classes to load and MainFrame to show!!!
    //loginProgMonitor.allDone();


    // If this was creation of a new account, we must update the key record with encrypted private portion,
    // or store it locally.
    if (newAccountCreated) {
      FetchedDataCache cache = MainFrame.getServerInterfaceLayer().getFetchedDataCache();
      RSAPrivateKey rsaPrivKey = cache.getNewUserPrivateKey();
      sendKeyUpdate(rsaPrivKey, storeRemoteFlag);
      cache.setNewUserPrivateKey(null);
    }

    if (trace != null) trace.exit(LoginFrame.class, success);
    return success;
  }
  */

  /**
  * When the account was first created, it was created without a private key.
  * We must update the key before any requests are sent to the server for other regular data.
  */
  private void sendKeyUpdate(RSAPrivateKey rsaPrivateKey, boolean storeKeyOnServer) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginFrame.class, "sendKeyUpdate(RSAPrivateKey rsaPrivateKey, boolean storeKeyOnServer)");
    if (trace != null) trace.args(storeKeyOnServer);

    try {
      ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
      FetchedDataCache cache = SIL.getFetchedDataCache();
      BAEncodedPassword baEncPass = cache.getEncodedPassword();

      KeyRecord keyRecord = cache.getKeyRecordMyCurrent();
      keyRecord.setPrivateKey(rsaPrivateKey);
      keyRecord.seal(baEncPass);
      // Add it to the cache so that it can be grabbed and UserRecord unsealed.
      cache.addKeyRecords(new KeyRecord[] { keyRecord });

      // Try uploading the key, if upload fails try to store it on local disk, repeat once if errors continue.
      // It is critical that one of these finishes OK, or else the account will become inoperable.
      StringBuffer errBuffer = new StringBuffer();
      if (!UserOps.sendPasswordChange(SIL, baEncPass, storeKeyOnServer, localPrivKeyFile, errBuffer)) {
        // do the other operation right away
        UserOps.sendPasswordChange(SIL, baEncPass, !storeKeyOnServer, localPrivKeyFile, errBuffer);
        // retry same operation 2nd time
        if (!UserOps.sendPasswordChange(SIL, baEncPass, storeKeyOnServer, localPrivKeyFile, errBuffer)) {
          // do the other operation 2nd time and show message that key was not stored as it should
          UserOps.sendPasswordChange(SIL, baEncPass, !storeKeyOnServer, localPrivKeyFile, errBuffer);
          String where = storeKeyOnServer ? "on the server" : "locally";
          String msg = "Private key could not be stored " + where + "!";
          if (errBuffer.length() > 0)
            msg += errBuffer.toString();
          MessageDialog.showErrorDialog(null, msg, com.CH_cl.lang.Lang.rb.getString("title_New_Account_Error"), true);
        }
      }
    } catch (Throwable t) {
      if (trace != null) trace.exception(LoginFrame.class, 100, t);
      MessageDialog.showErrorDialog(null, t.getMessage(), com.CH_cl.lang.Lang.rb.getString("title_New_Account_Error"));
    }

    if (trace != null) trace.exit(LoginFrame.class);
  }


  /** Submit and fetch request for new account */
  private boolean createNewAccount(Usr_NewUsr_Rq request) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginFrame.class, "createNewAccount(Usr_NewUsr_Rq)");

    boolean success = false;

    try {

      boolean chkEmailOk = false;

//      // check for available username
//      Object[] set1 = new Object[] { new Object[] { request.userRecord.handle }, new Object[] { request.userRecord.passwordHash }};
//      Obj_List_Co chkRequest1 = new Obj_List_Co(set1);
//      MessageAction chkAction1 = new MessageAction(CommandCodes.USR_Q_CHECK_AVAIL, chkRequest1);
//      ClientMessageAction replyChkAction1 = MainFrame.getServerInterfaceLayer().submitAndFetchReply(chkAction1, 30000);
//      if (replyChkAction1 != null && replyChkAction1.getActionCode() >= 0)
//        chkUsernameOk = true;
//      DefaultReplyRunner.nonThreadedRun(MainFrame.getServerInterfaceLayer(), replyChkAction1);

      // check for available email address
      Object[] set2 = new Object[] { new Object[] { request.requestedEmailAddress }, Boolean.FALSE };
      Obj_List_Co chkRequest2 = new Obj_List_Co(set2);
      MessageAction chkAction2 = new MessageAction(CommandCodes.EML_Q_CHECK_AVAIL, chkRequest2);
      ClientMessageAction replyChkAction2 = MainFrame.getServerInterfaceLayer().submitAndFetchReply(chkAction2, 90000);
      if (replyChkAction2 != null && replyChkAction2.getActionCode() >= 0) {
        chkEmailOk = true;
      } else if (replyChkAction2 != null && replyChkAction2.getActionCode() < 0) {
        // Suppress the generic error to avoid the "Operation did not complete successfully" part and replace if with more pleasant Warning style message,
        // and we still want to run the reply action to comply with SIL rules and progress monitors, etc.
        String customMessage = null;
        if (replyChkAction2.getMsgDataSet() instanceof Str_Rp) {
          Str_Rp reply = (Str_Rp) replyChkAction2.getMsgDataSet();
          customMessage = "<html>"+reply.message;
          reply.message = null;
        }
        if (customMessage == null) {
          customMessage = "<html>Email address '" + request.requestedEmailAddress + "' is already taken, please choose a different username.";
        }
        MessageDialog.showWarningDialog(null, customMessage, com.CH_cl.lang.Lang.rb.getString("title_New_Account_Error"));
      }
      DefaultReplyRunner.nonThreadedRun(MainFrame.getServerInterfaceLayer(), replyChkAction2);

      // username and email address check passed, create new user account now...
      if (chkEmailOk) {
        // remember the new user private key for the purpose of decrypting the login reply session key
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        cache.setNewUserPrivateKey(request.keyRecord.getPrivateKey());
        MessageAction msgAction = new MessageAction(CommandCodes.USR_Q_NEW_USER, request);
        ClientMessageAction replyAction = MainFrame.getServerInterfaceLayer().submitAndFetchReply(msgAction, 90000);
        if (replyAction.getActionCode() == CommandCodes.USR_A_NEW_USER){
          success = true;
        } else {
          cache.setNewUserPrivateKey(null);
        }
        // don't start a new thread here, just execute the action synchronously
        DefaultReplyRunner.nonThreadedRun(MainFrame.getServerInterfaceLayer(), replyAction);
      }
    } catch (Throwable t) {
      if (trace != null) trace.exception(LoginFrame.class, 100, t);
      MessageDialog.showErrorDialog(null, t.getMessage(), com.CH_cl.lang.Lang.rb.getString("title_New_Account_Error"));
      success = false;
    }

    if (trace != null) trace.exit(LoginFrame.class, success);
    return success;
  }


  public static Usr_NewUsr_Rq createNewUserRequest(RSAKeyPair keyPair, String userName, Long passwordHash, String emailAddress, String contactEmail, Short acceptingSpam, Short notifyByEmail, Long flags, BAEncodedPassword baEncodedPassword) {
    Usr_NewUsr_Rq newUser_request = new Usr_NewUsr_Rq();

    newUser_request.requestedEmailAddress = emailAddress;

    // keyRecord
    KeyRecord kRec = new KeyRecord();
    newUser_request.keyRecord = kRec;
    newUser_request.keyRecord.setPrivateKey(keyPair.getPrivateKey());
    newUser_request.keyRecord.plainPublicKey = keyPair.getPublicKey();
    // Don't send the private portion when creating new account -- only send it for sub accounts.
    if (baEncodedPassword != null) {
      newUser_request.keyRecord.seal(baEncodedPassword);
    }

    // Welcome email
    newUser_request.welcomeEmailSet = new Obj_List_Co(new String[] { URLs.get(URLs.WELCOME_EMAIL_FROM), URLs.get(URLs.WELCOME_EMAIL_SUBJECT), URLs.get(URLs.WELCOME_EMAIL_BODY) });

    BASymmetricKey symKeyFldShares = new BASymmetricKey(32);
    newUser_request.userRecord = new UserRecord();
    newUser_request.userRecord.handle = userName;
    newUser_request.userRecord.passwordHash = passwordHash;
    newUser_request.userRecord.emailAddress = contactEmail;
    newUser_request.userRecord.acceptingSpam = acceptingSpam;
    newUser_request.userRecord.notifyByEmail = notifyByEmail;
    newUser_request.userRecord.flags = flags;
    newUser_request.userRecord.setSymKeyFldShares(symKeyFldShares);
    newUser_request.userRecord.setSymKeyCntNotes(new BASymmetricKey(32));
    newUser_request.userRecord.seal(kRec);


    // fileShareRecord
    newUser_request.fileShareRecord = new FolderShareRecord();
    newUser_request.fileShareRecord.setFolderName(com.CH_cl.lang.Lang.rb.getString("folder_My_Files"));
    newUser_request.fileShareRecord.setFolderDesc(com.CH_cl.lang.Lang.rb.getString("folderDesc_Files_on_remote_system"));
    newUser_request.fileShareRecord.setSymmetricKey(new BASymmetricKey(32));
    newUser_request.fileShareRecord.seal(symKeyFldShares);

    // addrShareRecord
    newUser_request.addrShareRecord = new FolderShareRecord();
    newUser_request.addrShareRecord.setFolderName(com.CH_cl.lang.Lang.rb.getString("folder_Address_Book"));
    newUser_request.addrShareRecord.setFolderDesc(com.CH_cl.lang.Lang.rb.getString("folderDesc_Saved_Email_Addresses"));
    newUser_request.addrShareRecord.setSymmetricKey(new BASymmetricKey(32));
    newUser_request.addrShareRecord.seal(symKeyFldShares);

    // whiteShareRecord
    newUser_request.whiteShareRecord = new FolderShareRecord();
    newUser_request.whiteShareRecord.setFolderName(com.CH_cl.lang.Lang.rb.getString("folder_Whitelist"));
    newUser_request.whiteShareRecord.setFolderDesc(com.CH_cl.lang.Lang.rb.getString("folderDesc_Whitelist"));
    newUser_request.whiteShareRecord.setSymmetricKey(new BASymmetricKey(32));
    newUser_request.whiteShareRecord.seal(symKeyFldShares);

    // draftShareRecord
    newUser_request.draftShareRecord = new FolderShareRecord();
    newUser_request.draftShareRecord.setFolderName(com.CH_cl.lang.Lang.rb.getString("folder_Drafts"));
    newUser_request.draftShareRecord.setFolderDesc(com.CH_cl.lang.Lang.rb.getString("folderDesc_Saved_Drafts_for_future_editing"));
    newUser_request.draftShareRecord.setSymmetricKey(new BASymmetricKey(32));
    newUser_request.draftShareRecord.seal(symKeyFldShares);

    // msgShareRecord
    newUser_request.msgShareRecord = new FolderShareRecord();
    newUser_request.msgShareRecord.setFolderName(com.CH_cl.lang.Lang.rb.getString("folder_Inbox"));
    newUser_request.msgShareRecord.setFolderDesc(com.CH_cl.lang.Lang.rb.getString("folderDesc_My_default_inbox_message_folder"));
    newUser_request.msgShareRecord.setSymmetricKey(new BASymmetricKey(32));
    newUser_request.msgShareRecord.seal(symKeyFldShares);

    // junkShareRecord
    newUser_request.junkShareRecord = new FolderShareRecord();
    newUser_request.junkShareRecord.setFolderName(com.CH_cl.lang.Lang.rb.getString("folder_Spam"));
    newUser_request.junkShareRecord.setFolderDesc(com.CH_cl.lang.Lang.rb.getString("folderDesc_Suspected_spam_email_is_deposited_here"));
    newUser_request.junkShareRecord.setSymmetricKey(new BASymmetricKey(32));
    newUser_request.junkShareRecord.seal(symKeyFldShares);

    // sentShareRecord
    newUser_request.sentShareRecord = new FolderShareRecord();
    newUser_request.sentShareRecord.setFolderName(com.CH_cl.lang.Lang.rb.getString("folder_Sent"));
    newUser_request.sentShareRecord.setFolderDesc(com.CH_cl.lang.Lang.rb.getString("folderDesc_My_default_sent_message_folder"));
    newUser_request.sentShareRecord.setSymmetricKey(new BASymmetricKey(32));
    newUser_request.sentShareRecord.seal(symKeyFldShares);

    // contactShareRecord
    newUser_request.contactShareRecord = new FolderShareRecord();
    newUser_request.contactShareRecord.setFolderName(com.CH_cl.lang.Lang.rb.getString("folder_Contacts"));
    newUser_request.contactShareRecord.setFolderDesc(com.CH_cl.lang.Lang.rb.getString("folderDesc_Contact_folder"));
    newUser_request.contactShareRecord.setSymmetricKey(new BASymmetricKey(32));
    newUser_request.contactShareRecord.seal(symKeyFldShares);

    // keyShareRecord
    newUser_request.keyShareRecord = new FolderShareRecord();
    newUser_request.keyShareRecord.setFolderName(com.CH_cl.lang.Lang.rb.getString("folder_Keys"));
    newUser_request.keyShareRecord.setFolderDesc(com.CH_cl.lang.Lang.rb.getString("folderDesc_Key_folder"));
    newUser_request.keyShareRecord.setSymmetricKey(new BASymmetricKey(32));
    newUser_request.keyShareRecord.seal(symKeyFldShares);

    // recycleShareRecord
    newUser_request.recycleShareRecord = new FolderShareRecord();
    newUser_request.recycleShareRecord.setFolderName(com.CH_cl.lang.Lang.rb.getString("folder_RecycleBin"));
    newUser_request.recycleShareRecord.setFolderDesc(com.CH_cl.lang.Lang.rb.getString("folderDesc_RecycleBin"));
    newUser_request.recycleShareRecord.setSymmetricKey(new BASymmetricKey(32));
    newUser_request.recycleShareRecord.seal(symKeyFldShares);

    return newUser_request;
  }

} // end LoginFrame
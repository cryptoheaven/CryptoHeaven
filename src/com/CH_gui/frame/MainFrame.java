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

package com.CH_gui.frame;

import com.CH_cl.monitor.LoginProgMonitor;
import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.ops.*;
import com.CH_cl.service.records.filters.*;
import com.CH_cl.util.PopupWindow;
import com.CH_co.cryptx.BAEncodedPassword;
import com.CH_co.gui.*;
import com.CH_co.monitor.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.action.*;
import com.CH_gui.actionGui.*;
import com.CH_gui.contactTable.*;
import com.CH_gui.dialog.*;
import com.CH_gui.gui.*;
import com.CH_gui.table.TableComponent;
import com.CH_gui.tree.FolderTreeComponent;

import comx.Tiger.gui.*; // "Tiger" is an optional spell-checker module. If "Tiger" family of packages is not included with the source, simply comment out this line

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

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
 * <b>$Revision: 1.72 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class MainFrame extends JActionFrame implements ActionProducerI, LoginCoordinatorI {

  private Action[] actions;
  private static final int EXIT_ACTION = 0;
  protected static final int ABOUT_ACTION = 1;
  private static final int CHANGE_PASS_ACTION = 2;
//  private static final int CONNECTION_OPTIONS_ACTION = 3;
  private static final int ACCOUNT_OPTIONS_ACTION = 4;
  protected static final int URL__GENERAL_FAQ_ACTION = 5;
  protected static final int URL__QUICK_TOUR_ACTION = 6;
  protected static final int URL__USERS_GUIDE_ACTION = 7;
  private static final int CHANGE_USER_NAME = 8;
  private static final int SWITCH_IDENTITY = 9;
  private static final int MANAGE_SUB_ACCOUNTS = 10;
  private static final int DELETE_MY_ACCOUNT = 11;
  private static final int IMPORT_ADDRESS_BOOK = 12;
  protected static final int URL__ACCOUNT_UPGRADE = 13;
  private static final int MANAGE_WHITELIST = 14;
  private static final int SETUP_PASSWORD_RECOVERY = 15;
  private static final int TRACE_DIAGNOSTICS_ACTION = 16;

  private static ServerInterfaceLayer SIL;

  private static MainFrame singleInstance;

  private LoginProgMonitor loginProgMonitor;
  private JScrollPane welcomeScreenPane;

  private static final String PROPERTY_NAME_PREFIX__PERSONALIZE_EMAIL_ADDRESS_COUNT = "PersonalizeEmailAddressCount";

  private Long initialFolderId;
  private Long initialMsgLinkId;
  private TableComponent tableComp;
  private ContactTableComponent contactComp;


  static {
    Toolkit.getDefaultToolkit().getSystemEventQueue().push(InactivityEventQueue.getInstance());
  }


  /**
   * @returns a single instance of the MainFrame.
   */
  public static MainFrame getSingleInstance() {
    return singleInstance;
  }

  /** Creates new MainFrame */
//  private MainFrame(JWindow splashWindow) {
//    this(splashWindow, false, false);
//  }
  protected MainFrame(Window splashWindow, Long initialFolderId, Long initialMsgLinkId) {
    super(URLs.get(URLs.SERVICE_SOFTWARE_NAME), true, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MainFrame.class, "MainFrame()");

    this.initialFolderId = initialFolderId;
    this.initialMsgLinkId = initialMsgLinkId;
    if (singleInstance != null)
      throw new IllegalStateException("Main Frame already instantiated.");
    else
      singleInstance = this;

    if (splashWindow != null && splashWindow.isShowing()) {
      splashWindow.setVisible(false);
      splashWindow.dispose();
    }

    addWindowListener(new WindowAdapter() {
      public void windowIconified(WindowEvent e) {
        Thread gc = new Thread("Garbage Collection") {
          public void run() {
            System.gc();
          }
        };
        gc.setDaemon(true);
        gc.setPriority(Thread.MIN_PRIORITY);
        gc.start();
      }
    });

    // set default parent to multi-progress monitors to the main window
    MultiProgressMonitor.setDefaultParentComponent(this);

    if (trace != null) trace.exit(MainFrame.class);
  }

  public LoginProgMonitor getLoginProgMonitor() {
    return loginProgMonitor;
  }
  public void setLoginProgMonitor(LoginProgMonitor loginProgMonitor) {
    this.loginProgMonitor = loginProgMonitor;
  }

  /**
   * Called just before a login is attempted.
   */
  public void loginAttemptCloseCurrentSession() {
    JActionFrameClosable.closeAllClosableFramesLeaveNonUserSensitive();
    if (SIL != null)
      SIL.disconnectAndClear();
    ActionUtils.setEnabledActionsRecur(this);
  }

  /**
   * Called right after login completed.
   */
  public void loginComplete(boolean success, LoginCoordinatorI loginCoordinator) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MainFrame.class, "loginComplete(boolean success, loginCoordinator)");
    if (trace != null) trace.args(success);
    if (trace != null) trace.args(loginCoordinator);

    if (success) {
      if (trace != null) trace.data(10, "advance progress monitor, login is complete");
      if (loginCoordinator != null) {
        if (!loginCoordinator.getLoginProgMonitor().isAllDone()) {
          loginCoordinator.getLoginProgMonitor().nextTask();
          loginCoordinator.getLoginProgMonitor().setCurrentStatus(com.CH_gui.lang.Lang.rb.getString("label_Loading_Main_Program..._Please_Wait."));
        }
      }

      initScreen();

      // all JActionFrames save their own size
      // this.pack();

      if (trace != null) trace.data(20, "closing down the Login Progress Monitor");
      if (loginCoordinator != null) {
        loginCoordinator.getLoginProgMonitor().allDone();
        loginCoordinator.setLoginProgMonitor(null);
      }

      // All parent-less dialogs should go on top of the main window from now on.
      GeneralDialog.setDefaultParent(this);

      // clear cached folders and contacts before re-updating them
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      cache.removeFolderRecords(cache.getFolderRecords());
      //cache.removeFolderShareRecords(cache.getFolderShareRecords());
      cache.removeContactRecords(cache.getContactRecords());
      // clear cached fetched folder IDs
      tableComp.clearCachedFetchedFolderIDs();
      // TO-DO: Clean-up duplicate code...
      com.CH_cl.service.records.FolderRecUtil.clearFetchedIDs();

      // Mark Active Status right away since the GUI timer is scheduled in intervals... 
      // if user was disconnected in INACTIVE state, he should be marked active now...
      InactivityEventQueue.getInstance().sendActiveFlagIfInactive();

      // Get the folders and contacts when login Completes...
      if (SIL != null) {
        // fetch and select desired message
        final Long msgLinkId = initialMsgLinkId;
        initialMsgLinkId = null;
        if (msgLinkId != null) {
          if (trace != null) trace.data(50, "initial msgLinkId", msgLinkId);
          SIL.submitAndWait(new MessageAction(CommandCodes.MSG_Q_GET_MSG, new Obj_IDList_Co(new Long[] { null, msgLinkId })), 20000);
          MsgLinkRecord msgLink = cache.getMsgLinkRecord(msgLinkId);
          if (msgLink != null && msgLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
            FolderRecord fldRec = cache.getFolderRecord(msgLink.ownerObjId);
            if (fldRec != null) {
              final Long selectFolderId = fldRec.folderId;
              Runnable folderSelect = new Runnable() {
                public void run() {
                  Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");
                  if (trace != null) trace.data(10, "selectFolderId", selectFolderId);
                  FolderTreeComponent treeComp = getMainTreeComponent();
                  treeComp.getFolderTreeScrollPane().getFolderTree().setSelectedFolder(selectFolderId);

                  // if main window is not yet visible, show it
                  if (!MainFrame.this.isShowing()) MainFrame.this.setVisible(true);

                  if (trace != null) trace.exit(getClass());
                }
              };
              try { SwingUtilities.invokeAndWait(folderSelect); } catch (Throwable t) { }
              Runnable msgSelect = new Runnable() {
                public void run() {
                  Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");
                  if (trace != null) trace.data(10, "initial msgLinkId", msgLinkId);
                  tableComp.setSelectedId(msgLinkId);
                  if (trace != null) trace.exit(getClass());
                }
              };
              try { SwingUtilities.invokeAndWait(msgSelect); } catch (Throwable t) { }
            }
          }
        }
        // fetch and select desired folder
        else if (initialFolderId != null) {
          final Long folderId = initialFolderId;
          initialFolderId = null;
          Runnable folderSelect = new Runnable() {
            public void run() {
              Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");
              if (trace != null) trace.data(10, "initial folderId", folderId);
              FolderTreeComponent treeComp = getMainTreeComponent();
              treeComp.getFolderTreeScrollPane().getFolderTree().setSelectedFolder(folderId);
              if (trace != null) trace.exit(getClass());
            }
          };
          SIL.submitAndWait(new MessageAction(CommandCodes.FLD_Q_GET_FOLDERS_SOME, new Obj_IDList_Co(folderId)), 20000);
          try { SwingUtilities.invokeAndWait(folderSelect); } catch (Throwable t) { }
        }
        if (loginCoordinator != null) {
          loginCoordinator.readyForMainData();
        }
      }

      // Make the frame visible in a GUI thread
      Runnable showFrame = new Runnable() {
        public void run() {
          if (!MainFrame.this.isShowing()) MainFrame.this.setVisible(true);
        }
      };
      try { SwingUtilities.invokeAndWait(showFrame); } catch (Throwable t) { }

      // check if password is set
      Boolean isSet = isPasswordSet();
      if (isSet != null && !isSet.booleanValue())
        new ChangePasswordDialog(MainFrame.this, true);
    } else {
      if (!MainFrame.isLoggedIn())
        Misc.systemExit(0);
    }
    ActionUtils.setEnabledActionsRecur(this);

    if (trace != null) trace.exit(MainFrame.class);
  }

  public void readyForMainData() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MainFrame.class, "readyForMainData()");
    SIL.submitAndReturn(new MessageAction(CommandCodes.USR_Q_GET_RECONNECT_UPDATE));
    if (trace != null) trace.exit(MainFrame.class);
  }

  // =====================================================================
  // MAIN FUNCTION    
  // =====================================================================


  protected static void setServerInterfaceLayer(ServerInterfaceLayer newSIL) {
    SIL = newSIL;
  }
  public static ServerInterfaceLayer getServerInterfaceLayer() {
    return SIL;
  }


  /**
   * Remove all components from the frame leaving the menu and toolbar intact.
   */
  private void removeComponents() {
    Container contentPane = getContentPane();
    // clear all content in case we are re-initializing the main window
    Component[] comps = contentPane.getComponents();
    for (int i=0; comps != null && i < comps.length; i++) {
      if (!(comps[i] instanceof JToolBar)) {
        contentPane.remove(comps[i]);
        if (comps[i] instanceof DisposableObj)
          ((DisposableObj) comps[i]).disposeObj();
      }
    }
  }

  /**
   * Initialize the main window and layout the main components.
   */
  private void initScreen() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MainFrame.class, "initScreen()");

    // Remember old FolderTreeComponent for cleanup -- incase we are re-initializing screen
    FolderTreeComponent oldTreeComp = getMainTreeComponent();
    // Remove the Contact Folders from display
    // FolderTreeComponent treeComp = new FolderTreeComponent(true);
    FolderTreeComponent treeComp = new FolderTreeComponent(true, FolderFilter.MAIN_VIEW, SIL.getFetchedDataCache().getFolderPairs(new FixedFilter(true), true));
    setMainTreeComponent(treeComp);

    // Get displayable welcome screen in the empty table component.
    UserRecord myUserRec = SIL.getFetchedDataCache().getUserRecord();
    Long userId = null;
    if (myUserRec != null)
      userId = myUserRec.userId;
    welcomeScreenPane = new JScrollPane();
    welcomeScreenPane.setBorder(new EmptyBorder(0,0,0,0));
    setDefaultWelcomeScreenPane();

    // Remember old TableComponent for cleanup -- incase we are re-initializing screen
    TableComponent oldTableComp = tableComp;
    // Make new main TableComponent
    tableComp = new TableComponent("Browse", welcomeScreenPane);

    // Check or display the 'upgrade' popup window.
    {
      // See if a user account is expired or out of space, if so this will display a popup window with a message.
      UserOps.checkExpiry();
      UserOps.checkQuotas();

      // Display popup window to suggest upgrading
      if (myUserRec != null && myUserRec.isFreePromoAccount()) {
        String urlStrStart = "<a href=\""+URLs.get(URLs.SIGNUP_PAGE)+"?UserID=" + userId + "\">";
        String urlStrEnd = "</a>";
        String htmlText = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("msg_free_demo_account_sliding_message"), new Object[] {urlStrStart, urlStrEnd, URLs.get(URLs.SERVICE_SOFTWARE_NAME)});
        PopupWindow.getSingleInstance().addForScrolling(new HTML_ClickablePane(htmlText));
      } else if (myUserRec != null && myUserRec.isGuestAccount()) {
        String urlStrStart = "<a href=\""+URLs.get(URLs.SIGNUP_PAGE)+"?UserID=" + userId + "\">";
        String urlStrEnd = "</a>";
        String htmlText = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("msg_free_guest_account_sliding_message"), new Object[] {urlStrStart, urlStrEnd, URLs.get(URLs.SERVICE_SOFTWARE_NAME)});
        PopupWindow.getSingleInstance().addForScrolling(new HTML_ClickablePane(htmlText));
      }
      
      if (myUserRec != null) {
        // see if we should remind users to update their email address
        if (myUserRec.defaultEmlId.longValue() == -1 && !myUserRec.isFreePromoAccount() && !myUserRec.isHeld() && !myUserRec.isBusinessSubAccount()) {
          String propertyKey = PROPERTY_NAME_PREFIX__PERSONALIZE_EMAIL_ADDRESS_COUNT + "_" + myUserRec.userId;
          int count = Integer.parseInt(GlobalProperties.getProperty(propertyKey, "0"));
          if (count < 3) {
            GlobalProperties.setProperty(propertyKey, ""+(count+1));
            JComponent comp = new HTML_ClickablePane("Choose your personalized 'E-mail Address', click here to open 'Account Options'");
            comp.addMouseListener(new MouseAdapter() {
              public void mouseClicked(MouseEvent e) {
                actions[ACCOUNT_OPTIONS_ACTION].actionPerformed(new ActionEvent(this, 0, ""));
              }
            });
            PopupWindow.getSingleInstance().addForScrolling(comp);
          }
        }
      }
    }

    String propertyName = ContactActionTable.getTogglePropertyName(this);
    String oldShowS = GlobalProperties.getProperty(propertyName);
    boolean oldShow = oldShowS != null ? Boolean.valueOf(oldShowS).booleanValue() : false;
    RecordFilter filter = new MultiFilter(new RecordFilter[] { 
      new ContactFilterCl(myUserRec != null ? myUserRec.contactFolderId : null, oldShow), 
      new FolderFilter(FolderRecord.GROUP_FOLDER) }
    , MultiFilter.OR);

    // Remember old ContactTableComponent for cleanup -- incase we are re-initializing screen
    ContactTableComponent oldContactComp = contactComp;
    // Make new main TableComponent
    contactComp = new ContactTableComponent(filter, Template.get(Template.EMPTY_CONTACTS), Template.get(Template.BACK_CONTACTS), true);

    JSplitPane vSplit = new JSplitPaneVS(getVisualsClassKeyName() + "_vSplit", JSplitPane.VERTICAL_SPLIT, treeComp, contactComp, 0.8d);
    vSplit.setOneTouchExpandable(false);
    if (vSplit.getDividerSize() > 5) vSplit.setDividerSize(5);
    JSplitPane hSplit = new JSplitPaneVS(getVisualsClassKeyName() + "_hSplit", JSplitPane.HORIZONTAL_SPLIT, vSplit, tableComp, 0.15d);
    hSplit.setOneTouchExpandable(false);
    if (hSplit.getDividerSize() > 5) hSplit.setDividerSize(5);

    // status bar
    JPanel jStatusBar = createStatusBar();

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());
    mainPanel.add(hSplit, BorderLayout.CENTER);
    mainPanel.add(jStatusBar, BorderLayout.SOUTH);

    Container contentPane = getContentPane();
    removeComponents();
    // put everything on to the center...  leave N/S/E/W sides for droppable toolbar
    contentPane.add(mainPanel, BorderLayout.CENTER);

    UserRecord uRec = SIL.getFetchedDataCache().getUserRecord();
    setUserTitle(uRec);

    // init main table components so actions are added to the frame
    tableComp.initAddressTableComponent();
    tableComp.initFileTableComponent();
    tableComp.initMsgTableComponent();
    tableComp.initPostTableComponent();
    tableComp.initChatTableComponent();
    tableComp.initGroupTableComponent();
    tableComp.initLocalFileTableComponent();
    tableComp.initKeyTableComponent();
    tableComp.initRecycleTableComponent();

    // adding a listener will initialize the FileTableComponent so do that after we add it into the frame so actions can be generated and displayed
    treeComp.addTreeSelectionListener(tableComp);
    tableComp.addFolderSelectionListener(treeComp);

    // Cleanup old components from previous login...
    if (oldContactComp != null) {
      oldContactComp.disposeObj();
      //MiscGui.removeAllComponentsAndListeners(oldContactComp);
    }
    if (oldTableComp != null) {
      oldTableComp.disposeObj();
      //MiscGui.removeAllComponentsAndListeners(oldTableComp);
    }
    if (oldTreeComp != null) {
      oldTreeComp.disposeObj();
      //MiscGui.removeAllComponentsAndListeners(oldTreeComp);
    }

    if (trace != null) trace.exit(MainFrame.class);
  }


  private JPanel createStatusBar() {
    JPanel jStatusBar = new JPanel();
    jStatusBar.setLayout(new GridBagLayout());
    Dimension dim = null;

    JLabel jStatus = Stats.getStatusLabel();
    jStatus.setBorder(new EtchedBorder());

    JLabel jSize = Stats.getSizeLabel();
    jSize.setBorder(new EtchedBorder());
    dim = new Dimension(80, 14);
    jSize.setMinimumSize(dim);
    jSize.setPreferredSize(dim);

    JLabel jTransferRate = Stats.getTransferRateLabel();
    jTransferRate.setBorder(new EtchedBorder());
    dim = new Dimension(120, 14);
    jTransferRate.setMinimumSize(dim);
    jTransferRate.setPreferredSize(dim);

    JLabel jPing = Stats.getPingLabel();
    jPing.setBorder(new EtchedBorder());
    dim = new Dimension(60, 14);
    jPing.setMinimumSize(dim);
    jPing.setPreferredSize(dim);

    JLabel jConnections = Stats.getConnectionsLabel();
    jConnections.setBorder(new EtchedBorder());
    dim = new Dimension(26, 14);
    jConnections.setMinimumSize(dim);
    jConnections.setPreferredSize(dim);

    JLabel jOnlineStatus = Stats.getOnlineLabel();
    jOnlineStatus.setBorder(new EtchedBorder());
    dim = new Dimension(90, 14);
    jOnlineStatus.setMinimumSize(dim);
    jOnlineStatus.setPreferredSize(dim);

    Insets insets = new MyInsets(0, 1, 0, 1);
    int posX = 0;
    jStatusBar.add(jStatus, new GridBagConstraints(posX, 0, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
    posX ++;
    jStatusBar.add(jSize, new GridBagConstraints(posX, 0, 1, 1, 0, 0, 
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
    posX ++;
    jStatusBar.add(jTransferRate, new GridBagConstraints(posX, 0, 1, 1, 0, 0, 
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
    posX ++;
    jStatusBar.add(jPing, new GridBagConstraints(posX, 0, 1, 1, 0, 0, 
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
    posX ++;
    jStatusBar.add(jConnections, new GridBagConstraints(posX, 0, 1, 1, 0, 0, 
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
    posX ++;
    jStatusBar.add(jOnlineStatus, new GridBagConstraints(posX, 0, 1, 1, 0, 0, 
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));

    jStatusBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));

    return jStatusBar;
  }

  public FolderTreeComponent getMainTreeComponent(Component forComponent) {
    FolderTreeComponent treeComp = null;
    Window w = forComponent != null ? (forComponent instanceof JActionFrame ? (Window) forComponent : SwingUtilities.windowForComponent(forComponent)) : null;
    if (w instanceof JActionFrame) {
      treeComp = ((JActionFrame)w).getMainTreeComponent();
    } else {
      treeComp = getMainTreeComponent();
    }
    return treeComp;
  }

  /** Sets the default welcome screen component */
  public void setDefaultWelcomeScreenPane() {
    Long userId = SIL.getFetchedDataCache().getMyUserId();
    Component welcomePane = getWelcomeScreenComponent(URLs.get(URLs.WELCOME_TEMPLATE)+"?uId=" + userId, false);
    welcomeScreenPane.setViewportView(welcomePane);
  }

  /** Create a welcome screen message component for the new user or regular login component to established user */
  private Component getWelcomeScreenComponent(final String url, boolean onceDaily) {
    Component pane = null;
    try {
      boolean displayOk = true;
      if (onceDaily) {
        String PROPERTY_NAME__WELCOME_SCREEN_DATE = "WelcomeScreenLastDate";
        Date today = new Date();
        long todaysDate = today.getTime() / (1000*60*60*24); // round off to a day number
        try {
          long displayedDate = Long.parseLong(GlobalProperties.getProperty(PROPERTY_NAME__WELCOME_SCREEN_DATE, "0"));
          if (displayedDate == todaysDate) {
            displayOk = false;
          }
        } catch (Throwable t) {
        }
        if (displayOk) {
          GlobalProperties.setProperty(PROPERTY_NAME__WELCOME_SCREEN_DATE, ""+todaysDate); 
        }
      }
      if (displayOk) {
        HTML_ClickablePane clickPane = HTML_ClickablePane.createNewAndLoading(new URL(url));
        pane = clickPane;
        clickPane.setRegisteredLocalLauncher(HTML_ClickablePane.PROTOCOL_HTTP, clickPane);
        clickPane.setRegisteredLocalLauncher(HTML_ClickablePane.PROTOCOL_MAIL, new URLLauncherMAILTO());
        clickPane.setRegisteredLocalLauncher(new URLLauncherCHACTION(), URLLauncherCHACTION.ACTION_PATH);
      }
    } catch (Throwable t) {
    }
    return pane;
  }


  private void initActions() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MainFrame.class, "initActions()");
    int leadingActionId = Actions.LEADING_ACTION_ID_MAIN_FRAME;
    actions = new Action[17];
    actions[EXIT_ACTION] = new ExitAction(leadingActionId + EXIT_ACTION);
    actions[ABOUT_ACTION] = new AboutAction(leadingActionId + ABOUT_ACTION);
    actions[CHANGE_PASS_ACTION] = new ChangePassAction(leadingActionId + CHANGE_PASS_ACTION);
//    actions[CONNECTION_OPTIONS_ACTION] = new ConnectionOptionsAction(leadingActionId + CONNECTION_OPTIONS_ACTION);
    actions[ACCOUNT_OPTIONS_ACTION] = new AccountOptionsAction(leadingActionId + ACCOUNT_OPTIONS_ACTION);
    actions[URL__GENERAL_FAQ_ACTION] = new URLGeneralFAQAction(leadingActionId + URL__GENERAL_FAQ_ACTION);
    actions[URL__QUICK_TOUR_ACTION] = new URLQuickTourAction(leadingActionId + URL__QUICK_TOUR_ACTION);
    actions[URL__USERS_GUIDE_ACTION] = new URLUsersGuideAction(leadingActionId + URL__USERS_GUIDE_ACTION);
    actions[CHANGE_USER_NAME] = new ChangeUserNameAction(leadingActionId + CHANGE_USER_NAME);
    actions[SWITCH_IDENTITY] = new SwitchIdentityAction(leadingActionId + SWITCH_IDENTITY);
    actions[MANAGE_SUB_ACCOUNTS] = new ManageSubAccountsAction(leadingActionId + MANAGE_SUB_ACCOUNTS);
    actions[DELETE_MY_ACCOUNT] = new DeleteMyAccountAction(leadingActionId + DELETE_MY_ACCOUNT);
    actions[IMPORT_ADDRESS_BOOK] = new ImportAddressBookAction(leadingActionId + IMPORT_ADDRESS_BOOK);
    actions[URL__ACCOUNT_UPGRADE] = new URLAccountUpgradeAction(leadingActionId + URL__ACCOUNT_UPGRADE);
    actions[MANAGE_WHITELIST] = new ManageWhiteListAction(leadingActionId + MANAGE_WHITELIST);
    actions[SETUP_PASSWORD_RECOVERY] = new SetupPasswordRecovery(leadingActionId + SETUP_PASSWORD_RECOVERY);
    actions[TRACE_DIAGNOSTICS_ACTION] = new TraceDiagnosticsAction(leadingActionId + TRACE_DIAGNOSTICS_ACTION);
    if (trace != null) trace.exit(MainFrame.class);
  }


  /*********************************************/
  /**    A c t i o n   P r o d u c e r  I     **/
  /*********************************************/

  /** @return all the acitons that this objects produces.
   */
  public Action[] getActions() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MainFrame.class, "getActions()");
    if (actions == null) {
      initActions();
    }
    Action[] a = ActionUtilities.concatinate(super.getActions(), actions);
    if (trace != null) trace.exit(MainFrame.class, a);
    return a;
  }
  /** Final Action Producers will not be traversed to collect its containing objects' actions.
   * @return true if this object will gather all actions from its childeren or hide them counciously.
   */
  public boolean isFinalActionProducer() {
    return false;
  }

  /**
   * Enables or Disables actions based on the current state of the Action Producing component.
   */
  public void setEnabledActions() {
    if (actions == null)
      initActions();
    // only change state for actions which are not always enabled!
    boolean loggedIn = MainFrame.isLoggedIn();
    actions[CHANGE_PASS_ACTION].setEnabled(loggedIn);
//    actions[CONNECTION_OPTIONS_ACTION].setEnabled(loggedIn);
    actions[ACCOUNT_OPTIONS_ACTION].setEnabled(loggedIn);
    actions[CHANGE_USER_NAME].setEnabled(loggedIn);
    actions[MANAGE_SUB_ACCOUNTS].setEnabled(loggedIn);
    actions[DELETE_MY_ACCOUNT].setEnabled(loggedIn);
    actions[IMPORT_ADDRESS_BOOK].setEnabled(loggedIn);
    actions[MANAGE_WHITELIST].setEnabled(loggedIn);
    actions[SETUP_PASSWORD_RECOVERY].setEnabled(loggedIn);
  }

  // =====================================================================
  // LISTENERS FOR THE MENU ITEMS        
  // =====================================================================

  /** 
   * Exit the program and store menus and chosen tools in configuration file 
   **/
  private class ExitAction extends AbstractAction {
    public ExitAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Exit"), Images.get(ImageNums.DELETE16));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Exit_the_application."));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.DELETE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Exit"));
    }
    public void actionPerformed(ActionEvent event) {
      exitAction();
    }
  }

  /**
   * Show the About Dialog
   */
  protected static class AboutAction extends AbstractAction {
    public AboutAction(int actionId) {
      super(java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("action_About__SERVICE_SOFTWARE_NAME"), 
            new Object[] { URLs.get(URLs.SERVICE_SOFTWARE_NAME) }), Images.get(ImageNums.INFO16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      //putValue(Actions.TOOL_ICON, Images.get(ImageNums.INFO16));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformed(ActionEvent event) {
      new AboutDialog(GeneralDialog.getDefaultParent());
    }
  }

  /**
   * Show the Change Password Dialog
   */
  private class ChangePassAction extends AbstractAction {
    public ChangePassAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Change_Password"));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Change_Password"));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformed(ActionEvent event) {
      boolean isSetMode = false;
      Boolean isSet = isPasswordSet();
      if (isSet != null)
        isSetMode = !isSet.booleanValue();
      new ChangePasswordDialog(MainFrame.this, isSetMode);
    }
  }
  public static Boolean isPasswordSet() {
    Boolean isSet = null;
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    UserRecord uRec = cache.getUserRecord();
    if (uRec != null) {
      BAEncodedPassword ba = cache.getEncodedPassword();
      BAEncodedPassword baEmpty = UserRecord.getBAEncodedPassword("".toCharArray(), uRec.handle);
      isSet = Boolean.valueOf(!ba.equals(baEmpty));
    }
    return isSet;
  }

  /**
   * Show the Change UserName Dialog
   */
  private class ChangeUserNameAction extends AbstractAction {
    public ChangeUserNameAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Change_Username"));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Change_Username"));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformed(ActionEvent event) {
      if (!UserOps.isShowWebAccountRestrictionDialog(MainFrame.this)) {
        new ChangeUserNameDialog(MainFrame.this);
      }
    }
  }

//  /**
//   * Show the Connection Options Dialog
//   */
//  private class ConnectionOptionsAction extends AbstractAction {
//    public ConnectionOptionsAction(int actionId) {
//      super(com.CH_gui.lang.Lang.rb.getString("action_Connection_Options"));
//      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Connection_Options"));
//      putValue(Actions.ACTION_ID, new Integer(actionId));
//      //putValue(Actions.TOOL_ICON, Images.get(ImageNums.COMP_NET32));
//      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
//    }
//    public void actionPerformed(ActionEvent event) {
//      new ConnectionOptionsDialog(MainFrame.this);
//    }
//  }


  /**
   * Show the Account Options Dialog
   */
  private class AccountOptionsAction extends AbstractAction {
    public AccountOptionsAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Account_Options"), Images.get(ImageNums.USER_EDIT16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Account_Options"));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.USER_EDIT24));
    }
    public void actionPerformed(ActionEvent event) {
      new AccountOptionsDialog(MainFrame.this);
    }
  }


  /**
   * Switch Identiry to login as a different user
   */
  private class SwitchIdentityAction extends AbstractAction {
    public SwitchIdentityAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Switch_Identity"));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Log_off_current_identity_and_log_in_as_a_different_user."));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformed(ActionEvent event) {
      new LoginFrame(MainFrame.this, null);
    }
  }

  /**
   * Show dialog to Manage Sub-Accounts
   */
  private class ManageSubAccountsAction extends AbstractAction {
    public ManageSubAccountsAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Manage_User_Accounts"), Images.get(ImageNums.USER_MANAGE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      //putValue(Actions.TOOL_TIP, "Manage User Accounts");
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.USER_MANAGE24));
    }
    public void actionPerformed(ActionEvent actionEvent) {
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      UserRecord myUserRec = cache.getUserRecord();
      if (myUserRec.isCapableToManageUserAccounts()) {
        new SubUserTableFrame();
      } else {
        String urlStr = "\""+URLs.get(URLs.SIGNUP_PAGE)+"?UserID=" + myUserRec.userId + "&account=2\""; // account 2 for business
        String htmlText = "";
        if (myUserRec.isBusinessSubAccount()) {
          htmlText = "<html>You cannot manage other user accounts.  If you would like to change your personal options, use Account Options instead.  To change your storage quotas or special permissions, please contact your administrator for assistance.</html>";
        } else {
          htmlText = "<html>Only Business Accounts can manage their user accounts.  To upgrade your user account to a Business Account click here <a href="+urlStr+">"+URLs.get(URLs.SIGNUP_PAGE)+"</a>. <p>Thank You.</html>";
        }
        MessageDialog.showWarningDialog(MainFrame.this, htmlText, "Account Incapable", false);
      }
    }
  }

  /**
   * Show dialog to Delete User Account
   */
  private class DeleteMyAccountAction extends AbstractAction {
    public DeleteMyAccountAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Delete_Account_..."));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Permanently_delete_my_user_account."));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformed(ActionEvent actionEvent) {
      new DeleteAccountDialog(MainFrame.this, true, null);
    }
  }

  /**
   * Show import Address Book wizard
   */
  private class ImportAddressBookAction extends AbstractAction {
    public ImportAddressBookAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Import_Address_Book_..."));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformed(ActionEvent actionEvent) {
      new AddressBookImportWizardDialog(MainFrame.this);
    }
  }

  /**
   * Open the General FAQ URL
   */
  protected static class URLGeneralFAQAction extends AbstractAction {
    private String url = URLs.get(URLs.HELP_FAQ_PAGE);
    public URLGeneralFAQAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_General_FAQ"), Images.get(ImageNums.ANIM_GLOBE_FIRST16));
      putValue(Actions.TOOL_TIP, url);
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformed(ActionEvent event) {
      try {
        BrowserLauncher.openURL(url);
      } catch (Throwable t) {
        MessageDialog.showErrorDialog(GeneralDialog.getDefaultParent(), java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("msg_Error_occured_while_trying_to_open_URL..."), new Object[] {url, t.getMessage()}), com.CH_gui.lang.Lang.rb.getString("msgTitle_Error_opening_URL"));
      }
    }
  }

  /**
   * Open the Quick Tour URL
   */
  protected static class URLQuickTourAction extends AbstractAction {
    private String url = URLs.get(URLs.HELP_QUICK_TOUR_PAGE);
    public URLQuickTourAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Quick_Tour"), Images.get(ImageNums.ANIM_GLOBE_FIRST16));
      putValue(Actions.TOOL_TIP, url);
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformed(ActionEvent event) {
      try {
        BrowserLauncher.openURL(url);
      } catch (Throwable t) {
        MessageDialog.showErrorDialog(GeneralDialog.getDefaultParent(), java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("msg_Error_occured_while_trying_to_open_URL..."), new Object[] {url, t.getMessage()}), com.CH_gui.lang.Lang.rb.getString("msgTitle_Error_opening_URL"));
      }
    }
  }

  /**
   * Open the User's Guide URL
   */
  protected static class URLUsersGuideAction extends AbstractAction {
    private String url = URLs.get(URLs.HELP_USER_GUIDE_PAGE);
    public URLUsersGuideAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_User's_Guide"), Images.get(ImageNums.ANIM_GLOBE_FIRST16));
      putValue(Actions.TOOL_TIP, url);
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformed(ActionEvent event) {
      try {
        BrowserLauncher.openURL(url);
      } catch (Throwable t) {
        MessageDialog.showErrorDialog(GeneralDialog.getDefaultParent(), java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("msg_Error_occured_while_trying_to_open_URL..."), new Object[] {url, t.getMessage()}), com.CH_gui.lang.Lang.rb.getString("msgTitle_Error_opening_URL"));
      }
    }
  }

  /**
   * Open the Account Upgrade URL
   */
  protected static class URLAccountUpgradeAction extends AbstractAction {
    private String url = URLs.get(URLs.SIGNUP_PAGE);
    public URLAccountUpgradeAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Account_Upgrade"), Images.get(ImageNums.ANIM_GLOBE_FIRST16));
      putValue(Actions.TOOL_TIP, url);
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformed(ActionEvent event) {
      String urlToOpen = this.url +"?UserID="+ SIL.getFetchedDataCache().getMyUserId();
      try {
        BrowserLauncher.openURL(urlToOpen);
      } catch (Throwable t) {
        MessageDialog.showErrorDialog(GeneralDialog.getDefaultParent(), java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("msg_Error_occured_while_trying_to_open_URL..."), new Object[] {urlToOpen, t.getMessage()}), com.CH_gui.lang.Lang.rb.getString("msgTitle_Error_opening_URL"));
      }
    }
  }

  /**
   * Manage WhiteList
   */
  protected static class ManageWhiteListAction extends AbstractAction {
    public ManageWhiteListAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Manage_WhiteList_..."));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Manage_WhiteList_..."));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformed(ActionEvent actionEvent) {
      new Thread("Manage White List Action") {
        public void run() {
          Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");
          FolderPair whiteListFolderPair = FolderOps.getOrCreateWhiteList(SIL);
          if (whiteListFolderPair != null) {
            new WhiteListTableFrame(whiteListFolderPair);
          }
          if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
          if (trace != null) trace.exit(getClass());
          if (trace != null) trace.clear();
        }
      }.start();
    }
  }

  /**
   * Show the Setup Password Recovery Dialog
   */
  private class SetupPasswordRecovery extends AbstractAction {
    public SetupPasswordRecovery(int actionId) {
      super("Setup Password Recovery");
      putValue(Actions.TOOL_TIP, "Setup Password Recovery");
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformed(ActionEvent event) {
      new PassRecoverySetupDialog(MainFrame.this);
    }
  }

  /**
   * Show the Trace Diagnostics Dialog
   */
  private class TraceDiagnosticsAction extends AbstractAction {
    public TraceDiagnosticsAction(int actionId) {
      super("Problem Reporting", Images.get(ImageNums.TOOLS_FIX16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, "Send Diagnostics Information");
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformed(ActionEvent event) {
      new TraceDiagnosticsDialog(MainFrame.this);
    }
  }

  private void exitAction() {
    exitAction(this);
  }
  public static void exitAction(JActionFrame actionFrame) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MainFrame.class, "exitAction()");
    // Global try-catch to make sure we don't skip over the System.exit() statement.
    boolean closed = false;
    try {
      closed = JActionFrameClosable.closeAllClosableFramesVetoable();
      if (closed) {
        if (actionFrame != null) {
          actionFrame.saveFrameProperties();
        }

        GlobalProperties.store();

        if (actionFrame != null) {
          actionFrame.setVisible(false);
          actionFrame.dispose();
        }
      }
    } catch (Throwable t) {
    }

    if (closed) {
      new Thread("Logout Request Sender") {
        public void run() {
          Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");
          // from now on don't show any error messages when logging out and quitting.
          MiscGui.suppressAllGUI();
          // send logout
          MessageAction msgAction = new MessageAction(CommandCodes.USR_Q_LOGOUT);
          try {
            SIL.submitAndWait(msgAction, 5000);
          } catch (Throwable t) {
            if (trace != null) trace.exception(MainFrame.class, 100, t);
          }
          try {
            SIL.destroyServer();
          } catch (Throwable t) {
            if (trace != null) trace.exception(MainFrame.class, 200, t);
          }
          Misc.systemExit(0);
          if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
          if (trace != null) trace.exit(getClass());
          if (trace != null) trace.clear();
        } // end run()
      }.start();
    }
    if (trace != null) trace.exit(MainFrame.class);
  } // end exitAction()

  public void dispose() {
    try { super.dispose(); } catch (Throwable t) { }
    singleInstance = null;
  }

  protected void processWindowEvent(WindowEvent windowEvent) {
    super.processWindowEvent(windowEvent);
    if (windowEvent.getID() == WindowEvent.WINDOW_CLOSING) {
      exitAction();
    }
  }

  public static boolean isLoggedIn() {
    boolean rc = false;
    if (SIL != null)
      if (SIL.isLastLoginMsgActionSet())
        if (SIL.getFetchedDataCache().getMyUserId() != null)
          rc = true;
    return rc;
  }

  /**
   * Show one time message notification when main window shows.
   */
  private boolean wasShown = false;
  public void setVisible(boolean b) {
    super.setVisible(b);
    if (b && !wasShown) {
      wasShown = true;
      // "Tiger" is an optional spell-checker module. If "Tiger" family of packages is not included with the source, simply comment out this part.
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          try {
            String[] langs = SingleTigerSession.getAvailableLanguages();
            if (langs != null) {
              FetchedDataCache cache = FetchedDataCache.getSingleInstance();
              UserSettingsRecord usrSettingsRec = cache.getMyUserSettingsRecord();
              int maxAvailLangs = -1;
              try {
                String maxAvailLangsStr = usrSettingsRec.spellingProps.getProperty(SingleTigerSession.PROPERTY__MAX_AVAIL_LANGS);
                if (maxAvailLangsStr != null) maxAvailLangs = Integer.parseInt(maxAvailLangsStr);
              } catch (Throwable t) {
              }
              if (maxAvailLangs == -1) {
                Properties props = SingleTigerSession.getSingleInstance().getProperties();
                props.setProperty(SingleTigerSession.PROPERTY__MAX_AVAIL_LANGS, ""+langs.length);
                UserOps.updateUserSettingsSpellingProperties(SIL, props);
              } else if (maxAvailLangs > -1 && langs.length > 1 && langs.length > maxAvailLangs) {
                JPanel panel = new JPanel();
                final JMyCheckBox jCheck = new JMyCheckBox("Do not show this message again.");
                panel.setLayout(new GridBagLayout());
                int posY = 0;
                panel.add(new JMyLabel("Spell checking module has been updated, "), new GridBagConstraints(0, posY++, 1, 1, 10, 1,
                  GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(10, 10, 1, 10), 0, 0));
                panel.add(new JMyLabel("the following language dictionaries are available: "), new GridBagConstraints(0, posY++, 1, 1, 10, 1,
                  GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 10, 10, 10), 0, 0));
                for (int i=0; i<langs.length; i++)
                  panel.add(new JMyLabel(langs[i]), new GridBagConstraints(0, posY++, 1, 1, 10, 1,
                    GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 35, 1, 10), 0, 0));
                panel.add(jCheck, new GridBagConstraints(0, posY++, 1, 1, 10, 1,
                  GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(10, 10, 5, 10), 0, 0));
                JButton jOk = new JButton("OK");
                ActionListener defaultButtonAction = new ActionListener() {
                  public void actionPerformed(ActionEvent e) {
                    Window w = SwingUtilities.windowForComponent((Component) e.getSource());
                    if (w != null)
                      w.dispose();
                    if (jCheck.isSelected()) {
                      Properties props = SingleTigerSession.getSingleInstance().getProperties();
                      props.setProperty(SingleTigerSession.PROPERTY__MAX_AVAIL_LANGS, ""+SingleTigerSession.getAvailableLanguages().length);
                      UserOps.updateUserSettingsSpellingProperties(SIL, props);
                    }
                  }
                };
                jOk.addActionListener(defaultButtonAction);
                JButton[] buttons = new JButton[] { jOk };
                MessageDialog.showDialog(MainFrame.this, panel, "Update", MessageDialog.INFORMATION_MESSAGE, buttons, defaultButtonAction, false, true, false);
              }
            }
          } catch (Throwable t) {
          }
        }
      });
      // See if we have Password Recovery setup, if not open the Settings dialog
      Thread passRecoveryEnforcer = new Thread("Password Recovery Settings enforcer") {
        public void run() {
          Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");

          // additional delay so we don't slow down loading of MainFrame
          try {
            Thread.sleep(1000);
          } catch (Throwable t) {
          }
          ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
          FetchedDataCache cache = SIL.getFetchedDataCache();
          if (MainFrame.isLoggedIn() && cache.getMyUserId() != null) {
            // Show Password Recovery only if Password is set
            Boolean isSet = isPasswordSet();
            if (isSet != null && isSet.booleanValue()) {
              ClientMessageAction replyAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.USR_Q_PASS_RECOVERY_GET_CHALLENGE, new Obj_List_Co(cache.getMyUserId())), 60000);
              DefaultReplyRunner.nonThreadedRun(SIL, replyAction);
              if (replyAction != null && replyAction.getActionCode() == CommandCodes.USR_A_PASS_RECOVERY_GET_CHALLENGE) {
                PassRecoveryRecord myPassRecoveryRec = cache.getMyPassRecoveryRecord();
                if (myPassRecoveryRec == null)
                  new PassRecoverySetupDialog(MainFrame.this);
                else {
                  UserRecord myUserRec = cache.getUserRecord();
                  java.sql.Timestamp last = null;
                  boolean include24ExpiryNote = false;
                  if (myUserRec.dateLastLogin != null && myUserRec.dateLastLogout != null)
                    last = myUserRec.dateLastLogin.compareTo(myUserRec.dateLastLogout) > 0 ? myUserRec.dateLastLogout : myUserRec.dateLastLogin;
                  else if (myUserRec.dateLastLogin != null)
                    last = myUserRec.dateLastLogin;
                  else if (myUserRec.dateLastLogout != null)
                    last = myUserRec.dateLastLogout;
                  long _24h = 1L * 24L * 60L * 60L * 1000L;
                  long _24hAgo = new Date().getTime() - _24h;
                  if (last == null || last.getTime() > _24hAgo) {
                    last = new java.sql.Timestamp(_24hAgo);
                    include24ExpiryNote = true;
                  }

                  if ((myPassRecoveryRec.lastFetched != null && myPassRecoveryRec.lastFetched.compareTo(last) > 0) ||
                      (myPassRecoveryRec.lastFailed != null && myPassRecoveryRec.lastFailed.compareTo(last) > 0) ||
                      (myPassRecoveryRec.lastRecovered != null && myPassRecoveryRec.lastRecovered.compareTo(last) > 0))
                  {
                    new PassRecoveryRecoverDialog(MainFrame.this, myPassRecoveryRec, last, include24ExpiryNote);
                  }
                }
              }
            }
          }

          if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
          if (trace != null) trace.exit(getClass());
          if (trace != null) trace.clear();
        }
      };
      passRecoveryEnforcer.setDaemon(true);
      passRecoveryEnforcer.setPriority(Thread.MIN_PRIORITY);
      passRecoveryEnforcer.start();
    }
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "MainFrame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }

}
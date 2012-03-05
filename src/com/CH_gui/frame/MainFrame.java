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

package com.CH_gui.frame;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.event.ContactRecordEvent;
import com.CH_cl.service.cache.event.ContactRecordListener;
import com.CH_cl.service.cache.event.RecordEvent;
import com.CH_cl.service.engine.DefaultReplyRunner;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.*;
import com.CH_cl.service.records.EmailAddressRecord;
import com.CH_cl.service.records.filters.ContactFilterCl;
import com.CH_cl.service.records.filters.FixedFilter;
import com.CH_cl.service.records.filters.FolderFilter;
import com.CH_cl.service.records.filters.InvEmlFilter;
import com.CH_co.cryptx.BAEncodedPassword;
import com.CH_co.cryptx.Rnd;
import com.CH_co.io.RandomInputStream;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.MultiFilter;
import com.CH_co.service.records.filters.RecordFilter;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;
import com.CH_gui.action.AbstractActionTraced;
import com.CH_gui.action.ActionUtilities;
import com.CH_gui.action.Actions;
import com.CH_gui.actionGui.JActionFrame;
import com.CH_gui.actionGui.JActionFrameClosable;
import com.CH_gui.contactTable.ContactActionTable;
import com.CH_gui.contactTable.ContactTableComponent;
import com.CH_gui.contactTable.ContactTableComponent4Frame;
import com.CH_gui.dialog.*;
import com.CH_gui.gui.*;
import com.CH_gui.monitor.MultiProgressMonitorImpl;
import com.CH_gui.monitor.SimpleProgMonitorImpl;
import com.CH_gui.monitor.StatsBar;
import com.CH_gui.table.TableComponent;
import com.CH_gui.tree.FolderTreeComponent;
import com.CH_gui.usrs.UserGuiOps;
import com.CH_gui.util.*;
import comx.tig.en.SingleTigerSession;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

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
 * <b>$Revision: 1.72 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class MainFrame extends JActionFrame implements ActionProducerI, LoginCoordinatorI, ComponentContainerI, DisposableObj {

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
  private static final int EMAIL_SUPPORT_ACTION = 17;

  private static ServerInterfaceLayer SIL;

  private static MainFrame singleInstance;

  private ProgMonitorI loginProgMonitor;

  private static final String PROPERTY_NAME_PREFIX__PERSONALIZE_EMAIL_ADDRESS_COUNT = "PersonalizeEmailAddressCount";

  private Long initialFolderId;
  private Long initialMsgLinkId;

  private JPanel mainPanel;
  private JPanel welcomeScreenPanel;
  private ContactTableComponent welcomeContactTableComponent;
  private FolderTreeComponent treeComp;
  private TableComponent tableComp;
  private ContactTableComponent contactComp;
  private StatsBar statsBar;

  private boolean isInitializationsStarted = false;
  private boolean isInitializationsFinished = false;

  private ContactListener contactListener;

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
  protected MainFrame(Window splashWindow, Long initialFolderId, Long initialMsgLinkId) {
    super(URLs.get(URLs.SERVICE_SOFTWARE_NAME), true, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MainFrame.class, "MainFrame()");

    this.initialFolderId = initialFolderId;
    this.initialMsgLinkId = initialMsgLinkId;
    // don't check if already instantiated as applets can re-initialize in a single JVM
    singleInstance = this;

    if (splashWindow != null && splashWindow.isShowing()) {
      splashWindow.setVisible(false);
      splashWindow.dispose();
    }

    // set default parent to multi-progress monitors to the main window
    MultiProgressMonitorImpl.setDefaultParentComponent(this);

    contactListener = new ContactListener();
    FetchedDataCache.getSingleInstance().addContactRecordListener(contactListener);

    if (trace != null) trace.exit(MainFrame.class);
  }

  public ProgMonitorI getLoginProgMonitor() {
    return loginProgMonitor;
  }
  public void setLoginProgMonitor(ProgMonitorI progMonitor) {
    this.loginProgMonitor = progMonitor;
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
  public void loginComplete(boolean success) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MainFrame.class, "loginComplete(boolean success)");
    if (trace != null) trace.args(success);

    if (success) {

      if (!isInitializationsStarted) startPreloadingComponents_Threaded();

      if (trace != null) trace.data(10, "advance progress monitor, login is complete");
      if (!getLoginProgMonitor().isAllDone()) {
        getLoginProgMonitor().nextTask();
        getLoginProgMonitor().setCurrentStatus(com.CH_gui.lang.Lang.rb.getString("label_Loading_Main_Program..._Please_Wait."));
      }

      initScreen();

      // all JActionFrames save their own size
      // this.pack();

      if (trace != null) trace.data(20, "closing down the Login Progress Monitor");
      getLoginProgMonitor().allDone();
      setLoginProgMonitor(null);

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
          SIL.submitAndWait(new MessageAction(CommandCodes.MSG_Q_GET_MSG, new Obj_IDList_Co(new Long[] { null, msgLinkId })), 30000, 3);
          MsgLinkRecord msgLink = cache.getMsgLinkRecord(msgLinkId);
          if (msgLink != null && msgLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
            FolderRecord fldRec = cache.getFolderRecord(msgLink.ownerObjId);
            if (fldRec != null) {
              final Long selectFolderId = fldRec.folderId;
              Runnable folderSelect = new Runnable() {
                public void run() {
                  Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "MainFrame.loginComplete.folderSelect.run()");
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
                  Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "MainFrame.loginComplete.msgSelect.run()");
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
          Runnable folderSelect2 = new Runnable() {
            public void run() {
              Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "MainFrame.loginComplete.folderSelect2.run()");
              if (trace != null) trace.data(10, "initial folderId", folderId);
              FolderTreeComponent treeComp = getMainTreeComponent();
              treeComp.getFolderTreeScrollPane().getFolderTree().setSelectedFolder(folderId);
              if (trace != null) trace.exit(getClass());
            }
          };
          SIL.submitAndWait(new MessageAction(CommandCodes.FLD_Q_GET_FOLDERS_SOME, new Obj_IDList_Co(folderId)), 25000, 3);
          try { SwingUtilities.invokeAndWait(folderSelect2); } catch (Throwable t) { }
        }
        readyForMainData();
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
      if (!MainFrame.isLoggedIn()) {
        // cleanup gui and running Threads before applet exits, application exit would kipp deamon threads anyway
        MainFrame mainFrame = MainFrame.getSingleInstance();
        if (mainFrame != null) {
          mainFrame.exitAction();
        }
        Misc.systemExit(0);
      }
    }
    ActionUtils.setEnabledActionsRecur(this);

    if (trace != null) trace.exit(MainFrame.class);
  }

  public void startPreloadingComponents_Threaded() {
    if (!isInitializationsStarted) {
      isInitializationsStarted = true;
      // init main table components after initial frame is shown so actions are added to the frame menus
      Thread tableCompInitializerThread = new ThreadTraced("Main-Table-Comp-initializer") {
        public void runTraced() {
          Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "runTraced()");

          try {
            // Adding actions will instantiate menu and toolbar components...
            MainFrame.this.addComponentActions(MainFrame.this);
          } catch (Throwable t) {
            if (trace != null) trace.exception(getClass(), 100, t);
            t.printStackTrace();
          }

          try {
            // Make the main table
            tableComp = new TableComponent("Browse", false, false, false);
            tableComp.initAddressTableComponent();
            tableComp.initPostTableComponent(); // fastest msg type component to change any prior address related menu changes
            tableComp.initChatTableComponent(); // chat menu loads here for the first time
            tableComp.initMsgTableComponent();
            tableComp.initGroupTableComponent();
            tableComp.initKeyTableComponent();
            tableComp.initRecycleTableComponent();
            tableComp.initFileTableComponent();
            tableComp.initLocalFileTableComponent();
          } catch (Throwable t) {
            if (trace != null) trace.exception(getClass(), 200, t);
            t.printStackTrace();
          }

          try {
            // Make the main tree
            treeComp = new FolderTreeComponent(true, FolderFilter.MAIN_VIEW, SIL.getFetchedDataCache().getFolderPairs(new FixedFilter(true), true), false);
          } catch (Throwable t) {
            if (trace != null) trace.exception(getClass(), 300, t);
            t.printStackTrace();
          }

          try {
            // Make the main contact table
            String propertyName = ContactActionTable.getTogglePropertyName(MainFrame.this);
            boolean oldShow = false;
            String oldShowS = GlobalProperties.getProperty(propertyName);
            if (oldShowS != null) {
              Boolean oldShowsB = Boolean.valueOf(oldShowS);
              if (oldShowsB != null)
                oldShow = oldShowsB.booleanValue();
            }
            RecordFilter contactFilter = new MultiFilter(new RecordFilter[] {
              //new ContactFilterCl(myUserRec != null ? myUserRec.contactFolderId : null, oldShow),
              new ContactFilterCl(oldShow),
              new FolderFilter(FolderRecord.GROUP_FOLDER),
              new InvEmlFilter(true, false) }
            , MultiFilter.OR);

            // Make the ContactTableComponent
            contactComp = new ContactTableComponent(contactFilter, Template.get(Template.EMPTY_CONTACTS), Template.get(Template.BACK_CONTACTS), true, false, false);
            contactComp.addTopContactBuildingPanel();
          } catch (Throwable t) {
            if (trace != null) trace.exception(getClass(), 400, t);
            t.printStackTrace();
          }

          try {
            // Make the welcome ContactTableComponent used for toolbar actions
            welcomeContactTableComponent = new ContactTableComponent4Frame(null, new FixedFilter(false), null, null, false, false, true);
          } catch (Throwable t) {
            if (trace != null) trace.exception(getClass(), 500, t);
            t.printStackTrace();
          }

          try {
            // Make the main panel
            JSplitPane vSplit = new JSplitPaneVS(getVisualsClassKeyName() + "_vSplit", JSplitPane.VERTICAL_SPLIT, treeComp, contactComp, 0.65d, 0.65d);
            JSplitPane hSplit = new JSplitPaneVS(getVisualsClassKeyName() + "_hSplit", JSplitPane.HORIZONTAL_SPLIT, vSplit, tableComp, 0.15d, 0.15d);

            // status bar
            statsBar = new StatsBar();
            statsBar.installListeners();

            mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.add(hSplit, BorderLayout.CENTER);
            mainPanel.add(statsBar, BorderLayout.SOUTH);
          } catch (Throwable t) {
            if (trace != null) trace.exception(getClass(), 600, t);
            t.printStackTrace();
          }

          try {
            // getting actions will instantiate Action objects
            ActionUtils.getActionsRecursively(treeComp);
            ActionUtils.getActionsRecursively(contactComp);
            ActionUtils.getActionsRecursively(welcomeContactTableComponent);
            ActionUtils.getActionsRecursively(tableComp.getAddressTableComponent());
            ActionUtils.getActionsRecursively(tableComp.getPostTableComponent());
            ActionUtils.getActionsRecursively(tableComp.getChatTableComponent());
            ActionUtils.getActionsRecursively(tableComp.getMsgTableComponent());
            ActionUtils.getActionsRecursively(tableComp.getGroupTableComponent());
            ActionUtils.getActionsRecursively(tableComp.getKeyTableComponent());
            ActionUtils.getActionsRecursively(tableComp.getRecycleTableComponent());
            ActionUtils.getActionsRecursively(tableComp.getFileTableComponent());
            ActionUtils.getActionsRecursively(tableComp.getLocalFileTableComponent());
          } catch (Throwable t) {
            if (trace != null) trace.exception(getClass(), 700, t);
            t.printStackTrace();
          }

          isInitializationsFinished = true;
          if (trace != null) trace.exit(getClass());
        }
      };
      tableCompInitializerThread.setDaemon(true);
      tableCompInitializerThread.start();
    }
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
   * Initialize the main window and layout the main components.
   */
  private void initScreen() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MainFrame.class, "initScreen()");

    // wait for initializer thread to complete
    while (!isInitializationsFinished) {
      try { Thread.sleep(10); } catch (Throwable t) { }
    }

    // use a component initialized below to check if run for the first time
    boolean isFirstTimeInitialization = welcomeScreenPanel == null;

    // Make new Folder Tree
    if (isFirstTimeInitialization) {
      setMainTreeComponent(treeComp);

      // set window welcome panel
      welcomeScreenPanel = new JPanel();
      tableComp.setWelcomeScreenComponent(welcomeScreenPanel);

      // adding a listener will initialize the FileTableComponent so do that after we add it into the frame so actions can be generated and displayed
      treeComp.addTreeSelectionListener(tableComp);
      tableComp.addFolderSelectionListener(treeComp);
    }

    // set welcome content customized for current user
    setDefaultWelcomeScreenPanel();

    // After all of the mainPanel is constructed (including user customized welcome panel) then
    // add it to the frame which will trigger component added event and menus/toolbars will be populated
    if (isFirstTimeInitialization) {
      // put everything on to the center...
      getContentPane().add(mainPanel, BorderLayout.CENTER);
    }

    // set window title
    UserRecord uRec = SIL.getFetchedDataCache().getUserRecord();
    setUserTitle(uRec);

    // Check or display the 'upgrade' popup window.
    {
      // See if a user account is expired or out of space, if so this will display a popup window with a message.
      SysOps.checkExpiry();
      SysOps.checkQuotas();

      UserRecord myUserRec = SIL.getFetchedDataCache().getUserRecord();
      Long userId = SIL.getFetchedDataCache().getMyUserId();
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
            JComponent comp = new HTML_ClickablePane("Choose your personalized 'Email Address', click here to open 'Account Options'");
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

    if (trace != null) trace.exit(MainFrame.class);
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
  public void setDefaultWelcomeScreenPanel() {
    // clear prior panel content
    welcomeScreenPanel.removeAll();

    welcomeScreenPanel.setBorder(new EmptyBorder(0,0,0,0));
    welcomeScreenPanel.setLayout(new BorderLayout());

    // Create welcome Top panel with toolbar
    UserRecord userRec = SIL.getFetchedDataCache().getUserRecord();
    String username = userRec != null ? userRec.handle : "";
    welcomeContactTableComponent.setTitle("Welcome " + username);
    welcomeContactTableComponent.setTitleIcon(null); // remove the 16x16 transparent default icon
    welcomeScreenPanel.add(welcomeContactTableComponent.getTopPanel(), BorderLayout.NORTH);
    ActionProducerI[] actionProducers = ActionUtils.getActionProducersRecursively(welcomeContactTableComponent);
    for (int i=0; i<actionProducers.length; i++) {
      actionProducers[i].setEnabledActions();
    }

    // Create welcome content
    Long userId = SIL.getFetchedDataCache().getMyUserId();
    Component welcomeComp = getWelcomeScreenComponent(URLs.get(URLs.WELCOME_TEMPLATE)+"?uId=" + userId, false);
    welcomeScreenPanel.add(new JScrollPane(welcomeComp), BorderLayout.CENTER);
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


  /**
   * Synchronization makes sure only single thread can initialize it, and
   * that initialization can happen only once.
   */
  private synchronized void initActions() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MainFrame.class, "initActions()");
    if (this.actions == null) {
      int leadingActionId = Actions.LEADING_ACTION_ID_MAIN_FRAME;
      Action[] actions = new Action[18];
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
      actions[EMAIL_SUPPORT_ACTION] = new EmailSupportAction(leadingActionId + EMAIL_SUPPORT_ACTION);
      // assign to global once all actions are initialized
      this.actions = actions;
    }
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

  public void disposeObj() {
    if (contactListener != null) {
      FetchedDataCache.getSingleInstance().removeContactRecordListener(contactListener);
      contactListener = null;
    }
    if (statsBar != null) {
      statsBar.uninstallListeners();
    }
  }

  // =====================================================================
  // LISTENERS FOR THE MENU ITEMS
  // =====================================================================

  /**
   * Exit the program and store menus and chosen tools in configuration file
   **/
  private class ExitAction extends AbstractActionTraced {
    public ExitAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Exit"), Images.get(ImageNums.DELETE16));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Exit_the_application."));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.DELETE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Exit"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      exitAction();
    }
  }

  /**
   * Show the About Dialog
   */
  protected static class AboutAction extends AbstractActionTraced {
    public AboutAction(int actionId) {
      super(java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("action_About__SERVICE_SOFTWARE_NAME"),
            new Object[] { URLs.get(URLs.SERVICE_SOFTWARE_NAME) }), Images.get(ImageNums.INFO16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      new AboutDialog(GeneralDialog.getDefaultParent());
    }
  }

  /**
   * Show the Change Password Dialog
   */
  private class ChangePassAction extends AbstractActionTraced {
    public ChangePassAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Change_Password"));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
      putValue(Actions.GENERATED_NAME, Boolean.TRUE);
    }
    public void actionPerformedTraced(ActionEvent event) {
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
  private class ChangeUserNameAction extends AbstractActionTraced {
    public ChangeUserNameAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Change_Username"));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      if (!UserGuiOps.isShowWebAccountRestrictionDialog(MainFrame.this)) {
        new ChangeUserNameDialog(MainFrame.this);
      }
    }
  }

//  /**
//   * Show the Connection Options Dialog
//   */
//  private class ConnectionOptionsAction extends AbstractActionTraced {
//    public ConnectionOptionsAction(int actionId) {
//      super(com.CH_gui.lang.Lang.rb.getString("action_Connection_Options"));
//      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("action_Connection_Options"));
//      putValue(Actions.ACTION_ID, new Integer(actionId));
//      //putValue(Actions.TOOL_ICON, Images.get(ImageNums.COMP_NET32));
//      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
//    }
//    public void actionPerformedTraced(ActionEvent event) {
//      new ConnectionOptionsDialog(MainFrame.this);
//    }
//  }


  /**
   * Show the Account Options Dialog
   */
  private class AccountOptionsAction extends AbstractActionTraced {
    public AccountOptionsAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Account_Options"), Images.get(ImageNums.USER_EDIT16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.USER_EDIT24));
    }
    public void actionPerformedTraced(ActionEvent event) {
      new AccountOptionsDialog(MainFrame.this);
    }
  }


  /**
   * Switch Identity to login as a different user
   */
  private class SwitchIdentityAction extends AbstractActionTraced {
    public SwitchIdentityAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Switch_Identity"));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Log_off_current_identity_and_log_in_as_a_different_user."));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      new LoginFrame(MainFrame.this, null);
    }
  }

  /**
   * Show dialog to Manage Sub-Accounts
   */
  private class ManageSubAccountsAction extends AbstractActionTraced {
    public ManageSubAccountsAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Manage_User_Accounts"), Images.get(ImageNums.USER_MANAGE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      //putValue(Actions.TOOL_TIP, "Manage User Accounts");
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.USER_MANAGE24));
    }
    public void actionPerformedTraced(ActionEvent event) {
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
  private class DeleteMyAccountAction extends AbstractActionTraced {
    public DeleteMyAccountAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Delete_Account_..."));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Permanently_delete_my_user_account."));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      new DeleteAccountDialog(MainFrame.this, true, null);
    }
  }

  /**
   * Show import Address Book wizard
   */
  private class ImportAddressBookAction extends AbstractActionTraced {
    public ImportAddressBookAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Import_Address_Book_..."));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      new AddressBookImportWizardDialog(MainFrame.this);
    }
  }

  /**
   * Open the General FAQ URL
   */
  protected static class URLGeneralFAQAction extends AbstractActionTraced {
    private String url = URLs.get(URLs.HELP_FAQ_PAGE);
    public URLGeneralFAQAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_General_FAQ"), Images.get(ImageNums.ANIM_GLOBE_FIRST16));
      putValue(Actions.TOOL_TIP, url);
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
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
  protected static class URLQuickTourAction extends AbstractActionTraced {
    private String url = URLs.get(URLs.HELP_QUICK_TOUR_PAGE);
    public URLQuickTourAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Quick_Tour"), Images.get(ImageNums.ANIM_GLOBE_FIRST16));
      putValue(Actions.TOOL_TIP, url);
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
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
  protected static class URLUsersGuideAction extends AbstractActionTraced {
    private String url = URLs.get(URLs.HELP_USER_GUIDE_PAGE);
    public URLUsersGuideAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_User's_Guide"), Images.get(ImageNums.ANIM_GLOBE_FIRST16));
      putValue(Actions.TOOL_TIP, url);
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
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
  protected static class URLAccountUpgradeAction extends AbstractActionTraced {
    private String url = URLs.get(URLs.SIGNUP_PAGE);
    public URLAccountUpgradeAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Account_Upgrade"), Images.get(ImageNums.ANIM_GLOBE_FIRST16));
      putValue(Actions.TOOL_TIP, url);
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
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
  protected static class ManageWhiteListAction extends AbstractActionTraced {
    public ManageWhiteListAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Manage_WhiteList_..."));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Manage_WhiteList_..."));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      Thread th = new ThreadTraced("Manage White List Action") {
        public void runTraced() {
          FolderPair whiteListFolderPair = FolderOps.getOrCreateWhiteList(SIL);
          if (whiteListFolderPair != null) {
            new WhiteListTableFrame(whiteListFolderPair);
          }
        }
      };
      th.setDaemon(true);
      th.start();
    }
  }

  /**
   * Show the Setup Password Recovery Dialog
   */
  private class SetupPasswordRecovery extends AbstractActionTraced {
    public SetupPasswordRecovery(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Setup_Password_Recovery"));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      new PassRecoverySetupDialog(MainFrame.this);
    }
  }

  /**
   * Show the Trace Diagnostics Dialog
   */
  private class TraceDiagnosticsAction extends AbstractActionTraced {
    public TraceDiagnosticsAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Problem_Reporting"), Images.get(ImageNums.TOOLS_FIX16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, "Send Diagnostics Information");
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      new TraceDiagnosticsDialog(MainFrame.this);
    }
  }

  /**
   * Email Support
   */
  private class EmailSupportAction extends AbstractActionTraced {
    public EmailSupportAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Email_Support"), Images.get(ImageNums.EMAIL_SYMBOL_SMALL));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.IN_TOOLBAR, Boolean.FALSE);
    }
    public void actionPerformedTraced(ActionEvent event) {
      new MessageFrame(new Record[] { new EmailAddressRecord(URLs.get(URLs.SUPPORT_EMAIL)) }, "Support Request", URLs.get(URLs.SUPPORT_BODY));
    }
  }

  private void exitAction() {
    // check for modified files that could not start uploading
    FileLobUpEditMonitor.FileSet[] modifiedSets = FileLobUpEditMonitor.getModifiedFileSets();
    
    // check for active transfers
    ArrayList activeUps = FileLobUp.getStateSessions();
    final boolean anyModifications = modifiedSets != null && modifiedSets.length > 0;
    final boolean anyUploads = activeUps != null && activeUps.size() > 0;
    if (anyModifications || anyUploads) {
      String exitName = modifiedSets != null && modifiedSets.length > 0 ? "Discard and Exit" : "Suspend Uploads and Exit";
      Runnable yes = new NamedRunnable(exitName) {
        public void run() {
          exitActionCheckOpenFiles_Threaded(false);
        }
      };
      Runnable no = new NamedRunnable(anyUploads ? "Continue Uploading" : "Cancel") {
        public void run() {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              if (anyUploads) {
                // minimize the window
                try {
                  MainFrame.this.setState(JFrame.ICONIFIED);
                } catch (Throwable t) {
                }
              }
            }
          }); // end Runnable class
        }
      };
      String summary = FileLobUp.getSummary();
      String progress = FileLobUp.getProgress();
      String title = "";
      String msg = "";
      int severityLevel = 0;
      if (!anyModifications) {
        severityLevel = NotificationCenter.WARNING_MESSAGE;
        title = "Recent file transfers are incomplete!";
        msg = "<html>Exit and suspend current file transfers?<br>"
              +"Transfers will resume upon your next login.<br><br>"
              +(summary != null ? summary+"<br><br>" : "")
              +(progress != null ? "Incomplete file transfers are:<br>"+Misc.encodePlainIntoHtml(progress) : "");
      } else {
        severityLevel = NotificationCenter.ERROR_MESSAGE;
        String modifiedFiles = "";
        for (int i=0; i<modifiedSets.length; i++) {
          String errMsg = modifiedSets[i].getError();
          modifiedFiles += modifiedSets[i].getRemoteFile().getFileName() + (errMsg != null && errMsg.length() > 0 ? " - "+errMsg : "") + "<br>";
        }
        title = "Discard recent changes?";
        msg = "<html>Exit and discard file changes?<br>"
              +"File changes could not be uploaded.<br><br>"
              +modifiedFiles+"<br>";
        if (activeUps != null && activeUps.size() > 0 && progress != null) {
          msg += "Currently incomplete, but resumable transfers are:<br>"+Misc.encodePlainIntoHtml(progress);
        }
      }
      NotificationCenter.showYesNo(severityLevel, title, msg, false, yes, no);
    } else {
      exitActionCheckOpenFiles_Threaded(false);
    }
  }
  private void exitActionCheckOpenFiles_Threaded(final boolean suppressErrors) {
    FileLobUpEditMonitor.FileSet[] monitoredFiles = FileLobUpEditMonitor.getMonitoredFileSets();
    if (monitoredFiles != null && monitoredFiles.length > 0) {
      File tempDir = DownloadUtilities.getDefaultTempDir();
      ArrayList wipeLocalFilesL = new ArrayList();
      long totalSize = 0;
      for (int i=0; i<monitoredFiles.length; i++) {
        FileLobUpEditMonitor.FileSet set = monitoredFiles[i];
        File file = set.getLocalFile();
        if (file.exists()) {
          File dir = file.getParentFile();
          if (tempDir.equals(dir)) {
            // cleanup
            wipeLocalFilesL.add(set);
            totalSize += file.length();
          }
        }
      }
      if (wipeLocalFilesL.size() > 0) {
        final ArrayList _wipeLocalFilesL = wipeLocalFilesL;
        final long _totalSize = totalSize;
        Thread tempFileWiper = new ThreadTraced("TempFileWiper") {
          public void runTraced() {
            SimpleProgMonitorImpl progMonitor = new SimpleProgMonitorImpl(_totalSize);
            StringBuffer errorMsg = new StringBuffer();
            boolean anyError = false;
            for (int i=0; i<_wipeLocalFilesL.size(); i++) {
              FileLobUpEditMonitor.FileSet set = (FileLobUpEditMonitor.FileSet) _wipeLocalFilesL.get(i);
              File file = set.getLocalFile();
              FileLobUpEditMonitor.removeFromMonitoring(set.getRemoteFile().fileLinkId);
              if (!CleanupAgent.wipe(file, new RandomInputStream(Rnd.getSecureRandom()), progMonitor, false, null)) {
                // re-register if wipe failed - note that wipe does rename so if any data was wiped/changed, adding will fail
                FileLobUpEditMonitor.addToMonitoring(set);
                if (!suppressErrors)
                  anyError = true;
                if (errorMsg.length() == 0) {
                  errorMsg.append("<html>Could not cleanup temporary file(s).<br><br>Files maybe locked by another process. When you exit, any edits on opened files will not synchronize to remote storage:<br><br>");
                }
                errorMsg.append(file.getName());
                errorMsg.append("<br>");
              }
            }
            progMonitor.allDone();
            if (anyError) {
              String msg = errorMsg.toString();
              Runnable yes = new NamedRunnable("Retry and Exit") {
                public void run() {
                  exitActionCheckOpenFiles_Threaded(true);
                }
              };
              Runnable no = new NamedRunnable("Cancel") {
                public void run() {
                  SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                      // no-op
                    }
                  }); // end Runnable class
                }
              };
              NotificationCenter.showYesNo(NotificationCenter.WARNING_MESSAGE, "Temporary file cleanup failed.", msg, true, yes, no);
            } else {
              exitAction(MainFrame.this);
            }
          }

        };
        tempFileWiper.setDaemon(true);
        tempFileWiper.start();
      } else {
        exitAction(this);
      }
    } else {
      exitAction(this);
    }
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

        // Make sure we have file transfer settings saved even in the event of "clearing local settings"
        FileLobUp.saveState();

        // Store properties to disk.
        GlobalProperties.store();

        if (actionFrame != null) {
          actionFrame.setVisible(false);
          actionFrame.dispose();
        }
      }
    } catch (Throwable t) {
    }

    if (closed) {
      Thread th = new ThreadTraced("Logout Request Sender") {
        public void runTraced() {
          Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "MainFrame.exitAction.runTraced()");
          // from now on don't show any error messages when logging out and quitting.
          Misc.suppressAllGUI();
          try {
            SIL.destroyServer();
          } catch (Throwable t) {
            if (trace != null) trace.exception(getClass(), 200, t);
          }
          // Again to be rally sure since we had some complains about running
          // processes being left behind.
          try {
            SIL.destroyServer();
          } catch (Throwable t) {
            if (trace != null) trace.exception(getClass(), 300, t);
          }
          // If this is applet quitting, reset the gui flag in case it will re-initialize
          Misc.suppressAllGUI(false);
          Misc.systemExit(0);
          if (trace != null) trace.exit(getClass());
        } // end run()
      };
      th.setDaemon(true);
      th.start();
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
    return SIL != null && SIL.isLoggedIn();
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
                MessageDialog.showDialog(MainFrame.this, panel, "Update", NotificationCenter.INFORMATION_MESSAGE, buttons, defaultButtonAction, false, true, false);
              }
            }
          } catch (Throwable t) {
          }
        }
      });
      // See if we have Password Recovery setup, if not open the Settings dialog
      Thread passRecoveryEnforcer = new ThreadTraced("Password Recovery Settings enforcer") {
        public void runTraced() {
          // additional delay so we don't slow down loading of MainFrame
          try {
            Thread.sleep(2000);
          } catch (Throwable t) {
          }
          ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
          FetchedDataCache cache = SIL.getFetchedDataCache();
          if (MainFrame.isLoggedIn() && cache.getMyUserId() != null) {
            // Show Password Recovery only if Password is set
            Boolean isSet = isPasswordSet();
            if (isSet != null && isSet.booleanValue()) {
              ClientMessageAction replyAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.USR_Q_PASS_RECOVERY_GET_CHALLENGE, new Obj_List_Co(cache.getMyUserId())), 30000, 3);
              DefaultReplyRunner.nonThreadedRun(SIL, replyAction);
              if (replyAction != null && replyAction.getActionCode() == CommandCodes.USR_A_PASS_RECOVERY_GET_CHALLENGE) {
                PassRecoveryRecord myPassRecoveryRec = cache.getMyPassRecoveryRecord();
                if (myPassRecoveryRec == null)
                  new PassRecoverySetupDialog(MainFrame.this);
                else {
                  UserRecord myUserRec = cache.getUserRecord();
                  java.sql.Timestamp last = null;
                  boolean include24ExpiryNote = false;
                  if (myUserRec.dateLastLogin != null)
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
        }
      };
      passRecoveryEnforcer.setDaemon(true);
      passRecoveryEnforcer.start();
    }
  }

  /*****************************************************************
  *** C o m p o n e n t C o n t a i n e r I    interface methods ***
  *****************************************************************/
  public Component[] getPotentiallyHiddenComponents() {
    // useful for cleanup of hidden gui or instantiated gui but not yet sticked inside this frame
    return new Component[] { welcomeContactTableComponent, treeComp, tableComp, contactComp };
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "MainFrame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }


  /**
   * Listen on updates to the contacts in the cache and notify users of changes.
   */
  private class ContactListener implements ContactRecordListener {
    public void contactRecordUpdated(ContactRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new GUIUpdater(event));
    }
  }


  private class GUIUpdater implements Runnable {
    private RecordEvent event;
    public GUIUpdater(RecordEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GUIUpdater.class, "FolderGUIUpdater(RecordEvent event)");
      this.event = event;
      if (trace != null) trace.exit(GUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GUIUpdater.class, "FolderGUIUpdater.run()");

      if (event instanceof ContactRecordEvent) {

        ContactRecord[] contactRecords = ((ContactRecordEvent) event).getContactRecords();
        final ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
        FetchedDataCache cache = SIL.getFetchedDataCache();

        for (int i=0; contactRecords!=null && i<contactRecords.length; i++) {
          final ContactRecord cRec = contactRecords[i];
          if (cRec.status != null) {
            short status = cRec.status.shortValue();
            if (cRec.ownerUserId != null && cRec.ownerUserId.equals(cache.getMyUserId()) &&
               (status == ContactRecord.STATUS_ACCEPTED || status == ContactRecord.STATUS_DECLINED)) {

              UserRecord uRec = cache.getUserRecord(cRec.contactWithId);
              String userName = uRec != null ? uRec.shortInfo() : ("(" + cRec.contactWithId + ")");
              String newState = status == ContactRecord.STATUS_ACCEPTED ? "accepted" : "declined";
              String msg = "Contact '" + cRec.getOwnerNote() + "' with user " + userName + " has been " + newState + " by the other party.";
              String title = "Contact " + newState;

              if (status == ContactRecord.STATUS_ACCEPTED) {
                Sounds.playAsynchronous(Sounds.YOU_WERE_AUTHORIZED);
                final JCheckBox jEnableAudibleNotify = new JMyCheckBox("Enable audible notification when contact's status changes to Available.");
                jEnableAudibleNotify.setSelected((cRec.permits.intValue() & ContactRecord.SETTING_DISABLE_AUDIBLE_STATUS_NOTIFY) == 0);

                JPanel panel = new JPanel();
                panel.setLayout(new GridBagLayout());

                panel.setLayout(new GridBagLayout());

                int posY = 0;
                panel.add(new JMyLabel(msg), new GridBagConstraints(0, posY, 1, 1, 0, 0,
                      GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
                posY ++;
                panel.add(jEnableAudibleNotify, new GridBagConstraints(0, posY, 1, 1, 10, 0,
                      GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
                posY ++;

                ActionListener defaultButtonAction = new ActionListener() {
                  public void actionPerformed(ActionEvent event) {
                    Object[] objs = new Object[] { cRec.contactId, new Integer(jEnableAudibleNotify.isSelected() ? 0 : ContactRecord.SETTING_DISABLE_AUDIBLE_STATUS_NOTIFY) };
                    Obj_List_Co dataSet = new Obj_List_Co();
                    dataSet.objs = objs;
                    SIL.submitAndReturn(new MessageAction(CommandCodes.CNT_Q_ALTER_SETTINGS, dataSet));
                    Window w = SwingUtilities.windowForComponent((Component)event.getSource());
                    w.setVisible(false);
                    w.dispose();
                  }
                };
                MessageDialog.showDialog(null, panel, title, NotificationCenter.INFORMATION_MESSAGE, null, defaultButtonAction, false, false, false);
              } else if (status == ContactRecord.STATUS_DECLINED) {
                Sounds.playAsynchronous(Sounds.DIALOG_ERROR);
                MessageDialog.showInfoDialog(null, msg, title);
              }
            }
          }
        }

      }

      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(GUIUpdater.class);
    }
  } // end class GUIUpdater

}
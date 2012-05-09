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

import com.CH_cl.service.actions.usr.UsrALoginSecureSession;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.event.*;
import com.CH_cl.service.engine.LoginCoordinatorI;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.DownloadUtilities;
import com.CH_cl_eml.service.ops.ExportMsgsImpl;
import com.CH_co.monitor.ConfirmFileReplaceFactory;
import com.CH_co.monitor.ProgMonitorFactory;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.ContactFilterCo;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_co.trace.TraceDiagnostics;
import com.CH_co.util.*;
import com.CH_gui.actionGui.JActionFrame;
import com.CH_gui.dialog.AcceptDeclineContactDialog;
import com.CH_gui.dialog.ChangePasswordDialog;
import com.CH_gui.gui.InactivityEventQueue;
import com.CH_gui.gui.SingleFileChooser;
import com.CH_gui.monitor.*;
import com.CH_gui.util.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.net.URLDecoder;
import java.util.*;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;


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
 * <b>$Revision: 1.7 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class MainFrameStarter extends Object {

  private MainFrameStarter(JWindow splashWindow, boolean skipLogin, boolean swingMemoryFootprintTestExitWhenMainScreenLoaded, Long initialFolderId, Long initialMsgLinkId) {
    LoginCoordinatorI loginCoordinator = null;
    if (initialMsgLinkId == null)
      loginCoordinator = new MainFrame(splashWindow, initialFolderId, null);
    else
      loginCoordinator = new StarterLoginCoordinator(initialFolderId, initialMsgLinkId);

    if (!skipLogin) {
      new LoginFrame(loginCoordinator, splashWindow);
    } else {
      loginCoordinator.loginAttemptCloseCurrentSession(MainFrame.getServerInterfaceLayer());
      try {
        // Create progress monitor, but ignore any exceptions as it is not necessary that we have it.
        loginCoordinator.setLoginProgMonitor(ProgMonitorFactory.newInstanceLogin("Initializing ...", new String[] { "Loading Main Window" }, null));
      } catch (Throwable t) {
      }
      loginCoordinator.loginComplete(MainFrame.getServerInterfaceLayer(), true);
    }
    if (swingMemoryFootprintTestExitWhenMainScreenLoaded) {
      System.exit(0);
    }
    // Register various listeners to update title bars and provide general notifications
    FetchedDataCache.getSingleInstance().addContactRecordListener(new ContactListener());
    FetchedDataCache.getSingleInstance().addUserRecordListener(new UserListener());
    FetchedDataCache.getSingleInstance().addEmailRecordListener(new EmailListener());
  }


  public static void main(String[] args) {
    main(args, null);
  }
  public static void main(String[] args, JWindow splashWindow) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MainFrameStarter.class, "main(String[])");
    Thread.currentThread().setName("MainFrameStarter Creator Thread");

    // initialize a clean-up agent to run every 15 minutes for garbage-collection and every 60 minutes for temp file cleanup
    CleanupAgent.startSingleInstance(CleanupAgent.MODE_FINALIZATION | CleanupAgent.MODE_GC | CleanupAgent.MODE_TEMP_FILE_CLEANER,
                     new String[][] { { FileDataRecord.TEMP_ENCRYPTED_FILE_PREFIX, null },
                                      { FileDataRecord.TEMP_PLAIN_FILE_PREFIX, null } },
                     29, 29, 37, 37, 1, 1440);
    // File cleaner will start early, but wipe/delete left over temp files after they are at least 24h old.
    // All temp files should be wiped/deleted in other ways, this is a last resort cleanup in case of exceptions, etc.

    initLookAndFeelComponentDefaults();

    try {
      boolean checkVersion = true;
      boolean skipLogin = false;
      boolean swingMemoryFootprintTestExitWhenMainScreenLoaded = false;

      Long initialFolderId = null;
      Long initialMsgLinkId = null;

      if (args != null) {
        int countD = 0;
        for (int i=0; i<args.length; i++) {
          if (args[i].equals("nocheck")) {
            checkVersion = false;
          } else if (args[i].startsWith("-D")) {
            if (countD > 0)
              usageExit();
            countD ++;
            GlobalProperties.setAlternatePropertiesDir(args[i].substring(2));
          } else if (args[i].equalsIgnoreCase("noLogin")) {
            skipLogin = true;
          } else if (args[i].equalsIgnoreCase("exitWhenMainFrameLoaded")) {
            swingMemoryFootprintTestExitWhenMainScreenLoaded = true;
          } else if (args[i].equalsIgnoreCase("-killAfterMilliseconds")) {
            i ++;
            try {
              final Long millisToKill = Long.valueOf(args[i]);
              Thread th = new ThreadTraced("Delayed Kill Thread") {
                public void runTraced() {
                  try { Thread.sleep(millisToKill.longValue()); } catch (InterruptedException e) { }
                  Misc.systemExit(-1);
                }
              };
              th.setDaemon(true);
              th.start();
            } catch (Throwable t) {
            }
          } else if (args[i].equalsIgnoreCase("-killAfterRandomMilliseconds")) {
            i ++;
            try {
              final Long rndMillisToKill = Long.valueOf(args[i]);
              Thread th = new ThreadTraced("Delayed Kill Thread") {
                public void runTraced() {
                  Random rnd = new Random();
                  int delay = rnd.nextInt(rndMillisToKill.intValue());
                  try { Thread.sleep(delay); } catch (InterruptedException e) { }
                  Misc.systemExit(-1);
                }
              };
              th.setDaemon(true);
              th.start();
            } catch (Throwable t) {
            }
          } else if (args[i].equalsIgnoreCase("-no-splash")) {
            // already handled so ignore this...
          } else if (args[i].equalsIgnoreCase("-privateLabelURL")) {
            // already loaded so ignore this...
            i ++;
          } else if (args[i].equalsIgnoreCase("-username")) {
            i ++;
            String handle = URLDecoder.decode(args[i]);
            GlobalProperties.setProperty(LoginFrame.PROPERTY_USER_NAME, handle);
          } else if (args[i].equalsIgnoreCase("-password")) {
            i ++;
            LoginFrame.defaultPassword = args[i];
          } else if (args[i].equalsIgnoreCase("-password-blank")) {
            LoginFrame.defaultPassword = "";
          } else if (args[i].equalsIgnoreCase("-server")) {
            i ++;
            String server = URLDecoder.decode(args[i]);
            LoginFrame.defaultServer = server;
          } else if (args[i].equalsIgnoreCase("-signup")) {
            LoginFrame.defaultMode = LoginFrame.MODE_SIGNUP;
          } else if (args[i].equalsIgnoreCase("-signupEmail")) {
            i++;
            LoginFrame.defaultSignupEmail = args[i];
          } else if (args[i].equalsIgnoreCase("-folderId")) {
            i++;
            try {
              initialFolderId = Long.valueOf(args[i]);
            } catch (Throwable t) {
            }
          } else if (args[i].equalsIgnoreCase("-msgLinkId")) {
            i++;
            try {
              initialMsgLinkId = Long.valueOf(args[i]);
            } catch (Throwable t) {
            }
          } else if (args[0].equals("-menuEditor")) {
            i++;
            String onOff = args[i];
            if (onOff.equalsIgnoreCase("on") || onOff.equalsIgnoreCase("true"))
              JActionFrame.ENABLE_MENU_CUSTOMIZATION_ACTION = true;
            else if (onOff.equalsIgnoreCase("off") || onOff.equalsIgnoreCase("false"))
              JActionFrame.ENABLE_MENU_CUSTOMIZATION_ACTION = false;
          } else if (args[0].equals("-trace")) {
            i++;
            String onOff = args[i];
            if (onOff.equalsIgnoreCase("on") || onOff.equalsIgnoreCase("true"))
              TraceDiagnostics.traceStart(null);
            else if (onOff.equalsIgnoreCase("off") || onOff.equalsIgnoreCase("false"))
              TraceDiagnostics.traceStop();
          } else if (args[i].equalsIgnoreCase("-?") || args[i].equalsIgnoreCase("/?") || args[i].equalsIgnoreCase("-help") || args[i].equalsIgnoreCase("/help")) {
            System.out.println("Usage 1: [-D[!]<propertiesDir>] [-privateLabelURL <URL>] [-username <username>] [<-password <password>>|<-password-blank>] [-signup] [-signupEmail <emailAddress>] [-folderId <id>] [-msgLinkId <id>] [-no-splash]");
            System.out.println("Usage 2: -localKeyChangePass <localKeyId> <old name> <old password> <new name> <new password>");
            System.out.println("Usage 3: -version");
            Misc.systemExit(-1);
          } else {
            // ignore invalid arguments... only print out a warning
            System.out.println("Warning: invalid argument " + args[i]);
          }
        }
      }

      if (checkVersion) {
        String version = System.getProperty("java.version");
        StringTokenizer st = new StringTokenizer(version, ".");
        int major = 0;
        int minor = 0;
        if (st.hasMoreTokens())
          major = Integer.parseInt(st.nextToken());
        if (st.hasMoreTokens())
          minor = Integer.parseInt(st.nextToken());
        if (major < 1 || (major == 1 && minor < 3)) {
          System.out.println(com.CH_gui.lang.Lang.rb.getString("Please_upgrade_your_JRE..."));
          Misc.systemExit(-2);
        }
      }

      // setup MsgPopup Listener in the form of PopupWindow
      PopupWindow.getSingleInstance();
      // setup ProgMonitorFactory
      ProgMonitorFactory.setImplJournal(JournalProgMonitorImpl.class);
      ProgMonitorFactory.setImplLogin(LoginProgMonitorImpl.class);
      ProgMonitorFactory.setImplMulti(MultiProgressMonitorImpl.class);
      ProgMonitorFactory.setImplTransfer(TransferProgMonitorImpl.class);
      ProgMonitorFactory.setImplWipe(WipeProgMonitorImpl.class);
      // setup login action key file chooser
      UsrALoginSecureSession.setImplFileChooser(SingleFileChooser.class);
      // setup various factories
      ConfirmFileReplaceFactory.setImpl(ConfirmFileReplaceImpl.class);
      DownloadUtilities.setImplExportMsgs(ExportMsgsImpl.class);
      NotificationCenter.setImpl(NotificationShowerImpl.class);
      Sounds.setImpl(SoundsPlayerImpl.class);
      FileTypes.setFileTypeImpl(FileTypesIcons.class);

      // start main GUI
      new MainFrameStarter(splashWindow, skipLogin, swingMemoryFootprintTestExitWhenMainScreenLoaded, initialFolderId, initialMsgLinkId);

    } catch (Throwable t) {
      System.out.println(t.getMessage());
      System.out.println(Misc.getStack(t));
      if (trace != null) trace.exception(MainFrameStarter.class, 100, t);

      try {
        MainFrame.getServerInterfaceLayer().destroyServer();
      } catch (Throwable th) {
        if (trace != null) trace.exception(MainFrameStarter.class, 200, th);
      }
      Misc.systemExit(-2);
    }
    if (trace != null) trace.exit(MainFrameStarter.class);
  }

  private static void usageExit() {
    System.out.println(com.CH_gui.lang.Lang.rb.getString("usage_line_1"));
    System.out.println(com.CH_gui.lang.Lang.rb.getString("usage_line_2"));
    Misc.systemExit(-1);
  }


  public static void initLookAndFeelComponentDefaults() {
    UIDefaults table = UIManager.getLookAndFeelDefaults();

    Set keys = table.keySet();
    Object[] keysO = new Object[keys.size()];
    keys.toArray(keysO);
    // odd menu bar border and tool bar border colors
    Color colorToChange1 = new Color(204, 204, 204);
    Color colorSubstitute1 = new Color(223, 221, 214);
    Color colorToChange2 = new Color(180, 195, 212);
    Color colorSubstitute2 = new Color(206, 219, 230);
    boolean withConsolePrintout = false;
    if (withConsolePrintout) {
      Arrays.sort(keysO, new Comparator() { // use custom comparator because keys are of different class types...
        public int compare(Object o1, Object o2) {
          String s1 = o1 != null ? o1.toString() : "";
          String s2 = o2 != null ? o2.toString() : "";
          return s1.compareTo(s2);
        }
      });
    }
    for (int i=0; i<keysO.length; i++) {
      Object key = keysO[i];
      Object o = table.get(key);
      //if (key.indexOf("font") >= 0) System.out.println("" + key + " = " + o);
      if (o instanceof Color) {
        Color c = (Color) o;
        if (c.equals(colorToChange1)) {
          //System.out.println("key for color change 1 is " + key);
          table.put(key, colorSubstitute1);
        } else if (c.equals(colorToChange2)) {
          //System.out.println("key for color change 2 is " + key);
          table.put(key, colorSubstitute2);
        }
      }
    }

    if (Toolkit.getDefaultToolkit().getScreenSize().width <= 800) {
      MiscGui.setSmallScreen(true);
    }

    AffineTransform at = null;
    HashMap fontCache = null;

    // Change default labels and menu items to normal font attribute and black color, also make the default fonts little smaller
    Enumeration keyEnum = table.keys();
    while (keyEnum.hasMoreElements()) {
      Object key = keyEnum.nextElement();
      Object value = table.get(key);
      if (value instanceof FontUIResource) {
        FontUIResource fontUI = (FontUIResource) value;
        if (fontCache == null) fontCache = new HashMap();
        String fontKey = fontUI.getName()+"."+fontUI.getSize()+"."+fontUI.getStyle();
        FontUIResource fontUIderived = (FontUIResource) fontCache.get(fontKey);
        if (fontUIderived == null) {
          fontUIderived = fontUI;
          if (!fontUIderived.isPlain()) {
            fontUIderived = new FontUIResource(fontUIderived.deriveFont(Font.PLAIN));
          }
          if (at == null && MiscGui.isSmallScreen()) {
            at = new AffineTransform();
            at.setToScale(0.9, 0.9); // any smaller than 0.9 and BOLD becomes NORMAL
          }
          if (at != null) {
            fontUIderived = new FontUIResource(fontUIderived.deriveFont(at));
          }
          fontCache.put(fontKey, fontUIderived);
        }
        table.put(key, fontUIderived);
      }
    }

    // Change default label color to black
    try {
      table.put("Label.foreground", Color.black);
    } catch (Throwable t) {
    }
  }

  private static class StarterLoginCoordinator implements LoginCoordinatorI {
//    private Long initialFolderId;
    private Long initialMsgLinkId;

    private StarterLoginCoordinator(Long initialFolderId, Long initialMsgLinkId) {
//      this.initialFolderId = initialFolderId;
      this.initialMsgLinkId = initialMsgLinkId;
    }

    private ProgMonitorI monitor;

    public ProgMonitorI getLoginProgMonitor() {
      return monitor;
    }

    public void setLoginProgMonitor(ProgMonitorI progMonitor) {
      monitor = progMonitor;
    }

    public void loginAttemptCloseCurrentSession(ServerInterfaceLayer SIL) {
    }

    public void loginComplete(ServerInterfaceLayer SIL, boolean success) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StarterLoginCoordinator.class, "loginComplete(ServerInterfaceLayer SIL, boolean success)");
      if (trace != null) trace.args(success);

      final Frame[] mainStartupFrame = new Frame[1];

      if (success) {
        if (trace != null) trace.data(10, "advance progress monitor, login is complete");
        getLoginProgMonitor().nextTask();
        getLoginProgMonitor().setCurrentStatus(com.CH_gui.lang.Lang.rb.getString("label_Loading_Main_Program..._Please_Wait."));

        // Mark Active Status right away since the GUI timer is scheduled in intervals...
        // if user was disconnected in INACTIVE state, he should be marked active now...
        InactivityEventQueue.getInstance().sendActiveFlagIfInactive();

        // Get the folders and contacts when login Completes...
        final FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        if (SIL != null) {
          // fetch and select desired message
          final Long msgLinkId = initialMsgLinkId;
          initialMsgLinkId = null;
          if (msgLinkId != null) {
            if (trace != null) trace.data(50, "initial msgLinkId", msgLinkId);
            SIL.submitAndWait(new MessageAction(CommandCodes.MSG_Q_GET_MSG, new Obj_IDList_Co(new Long[] { null, msgLinkId })), 20000);
            final MsgLinkRecord msgLink = cache.getMsgLinkRecord(msgLinkId);
            if (msgLink != null && msgLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
              final FolderRecord fldRec = cache.getFolderRecord(msgLink.ownerObjId);
              if (fldRec != null) {
                Runnable msgSelect = new Runnable() {
                  public void run() {
                    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "MainFrameStarter.loginComplete.msgSelect.run()");
                    if (trace != null) trace.data(10, "initial msgLinkId", msgLinkId);
                    FolderPair fPair = new FolderPair(cache.getFolderShareRecordMy(fldRec.folderId, true), fldRec);
                    mainStartupFrame[0] = new MsgTableStarterFrame(fPair, new MsgLinkRecord[] { msgLink }, true);

                    GeneralDialog.setDefaultParent(mainStartupFrame[0]);

                    // Display popup window to suggest upgrading
                    UserRecord myUserRec = cache.getUserRecord();
                    if (myUserRec != null && myUserRec.isFreePromoAccount()) {
                      Long userId = myUserRec.userId;
                      String urlStrStart = "<a href=\""+URLs.get(URLs.SIGNUP_PAGE)+"?UserID=" + userId + "\">";
                      String urlStrEnd = "</a>";
                      String htmlText = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("msg_free_demo_account_sliding_message"), new Object[] {urlStrStart, urlStrEnd, URLs.get(URLs.SERVICE_SOFTWARE_NAME)});
                      PopupWindow.getSingleInstance().addForScrolling(new HTML_ClickablePane(htmlText));
                    } else if (myUserRec != null && myUserRec.isGuestAccount()) {
                      Long userId = myUserRec.userId;
                      String urlStrStart = "<a href=\""+URLs.get(URLs.SIGNUP_PAGE)+"?UserID=" + userId + "\">";
                      String urlStrEnd = "</a>";
                      String htmlText = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("msg_free_guest_account_sliding_message"), new Object[] {urlStrStart, urlStrEnd, URLs.get(URLs.SERVICE_SOFTWARE_NAME)});
                      PopupWindow.getSingleInstance().addForScrolling(new HTML_ClickablePane(htmlText));
                    }

                    if (trace != null) trace.exit(getClass());
                  }
                };
                try { SwingUtilities.invokeAndWait(msgSelect); } catch (Throwable t) { }
              }
            }
          }
          // fetch the rest of supporting data required for replies and other operations
          readyForMainData(SIL);
        }

        if (trace != null) trace.data(20, "closing down the Login Progress Monitor");
        getLoginProgMonitor().allDone();
        setLoginProgMonitor(null);

        if (mainStartupFrame[0] == null) {
          MainFrame mainFrame = new MainFrame(null, null, null);
          try {
            // Create progress monitor, but ignore any exceptions as it is not necessary that we have it.
            mainFrame.setLoginProgMonitor(ProgMonitorFactory.newInstanceLogin("Initializing ...", new String[] { "Loading Main Window" }, null));
          } catch (Throwable t) {
          }
          mainFrame.loginComplete(SIL, true);
          mainStartupFrame[0] = mainFrame;
        } else {
          // check if password is set
          Boolean isSet = MainFrame.isPasswordSet();
          if (isSet != null && !isSet.booleanValue())
            new ChangePasswordDialog((Frame) GeneralDialog.getDefaultParent(), true);
        }
      } else {
        if (!MainFrame.isLoggedIn())
          Misc.systemExit(0);
      }
//      // not needed as no re-login is possible here
//      if (mainStartupFrame[0] != null) {
//        ActionUtils.setEnabledActionsRecur(mainStartupFrame[0]);
//      }

      if (trace != null) trace.exit(StarterLoginCoordinator.class);
    }

    public void startPreloadingComponents_Threaded() {
      // no-op
    }

    public void readyForMainData(ServerInterfaceLayer SIL) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MainFrameStarter.class, "readyForMainData(ServerInterfaceLayer SIL)");
      // fetch contacts, root folders (including Sent folder for replying to message), etc...
      SIL.submitAndReturn(new MessageAction(CommandCodes.USR_Q_GET_RECONNECT_UPDATE));
      if (trace != null) trace.exit(MainFrameStarter.class);
    }
  } // end class StarterLoginCoordinator


  /****************************************************************************************/
  /************* LISTENERS ON CHANGES IN THE CACHE *****************************************/
  /****************************************************************************************/

  /**
   * Listen on updates to the ContactRecords in the cache.
   */
  private class ContactListener implements ContactRecordListener {
    public void contactRecordUpdated(ContactRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second, and to prevent gui tree deadlocks.
      javax.swing.SwingUtilities.invokeLater(new ContactGUIUpdater(event));
    }
  }

  private static SingleTokenArbiter contactArbiter = new SingleTokenArbiter();
  private class ContactGUIUpdater implements Runnable {
    private ContactRecordEvent event;
    public ContactGUIUpdater(ContactRecordEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactGUIUpdater.class, "ContactGUIUpdater(ContactRecordEvent event)");
      this.event = event;
      if (trace != null) trace.exit(ContactGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactGUIUpdater.class, "ContactGUIUpdater.run()");

      ContactRecord[] records = event.getContactRecords();
      if (event.getEventType() == RecordEvent.SET) {
        Long myUserId = MainFrame.getServerInterfaceLayer().getFetchedDataCache().getMyUserId();
        ContactRecord[] toAcceptDecline = ContactRecord.filterToAcceptOrDecline(records, myUserId);
        toAcceptDecline = (ContactRecord[]) RecordUtils.filter(toAcceptDecline, new ContactFilterCo(null, new Long[] { myUserId }));
        if (toAcceptDecline != null && toAcceptDecline.length > 0)
          showAcceptDeclineDialogs(toAcceptDecline, -1);
      }

      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(ContactGUIUpdater.class);
    }
  }

  private void showAcceptDeclineDialogs(final ContactRecord[] toAcceptDecline, int prevShownIndex) {
    final int currentIndex = prevShownIndex + 1;
    if (currentIndex < toAcceptDecline.length) {
      final ContactRecord cRec = toAcceptDecline[currentIndex];
      final Object token = new Object();
      final boolean tokenRemoved[] = new boolean[1];
      if (contactArbiter.putToken(cRec, token)) {
        final AcceptDeclineContactDialog dialog = new AcceptDeclineContactDialog(GeneralDialog.getDefaultParent(), cRec);
        dialog.addWindowListener(new WindowAdapter() {
          public void windowClosed(WindowEvent e) {
            // prevent multiple execution due to event system bugs
            if (!tokenRemoved[0]) {
              contactArbiter.removeToken(cRec, token);
              tokenRemoved[0] = true;
              showAcceptDeclineDialogs(toAcceptDecline, currentIndex);
            }
          }
        });
      }
    }
  }

  /**
   * Listen on updates to the UserRecords in the cache.
   */
  private static class UserListener implements UserRecordListener {
    public void userRecordUpdated(UserRecordEvent event) {
      // to prevent gui tree deadlocks, run on an AWT thread
      javax.swing.SwingUtilities.invokeLater(new TitleGUIUpdater());
    }
  }

  /**
   * Listen on updates to the EmailRecords in the cache.
   */
  private static class EmailListener implements EmailRecordListener {
    public void emailRecordUpdated(EmailRecordEvent event) {
      // to prevent gui tree deadlocks, run on an AWT thread
      javax.swing.SwingUtilities.invokeLater(new TitleGUIUpdater());
    }
  }

  private static class TitleGUIUpdater implements Runnable {
    public TitleGUIUpdater() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TitleGUIUpdater.class, "TitleGUIUpdater()");
      if (trace != null) trace.exit(TitleGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TitleGUIUpdater.class, "TitleGUIUpdater.run()");

      Frame frame = GeneralDialog.getDefaultParent();
      if (frame != null && frame instanceof JActionFrame)
        ((JActionFrame) frame).setUserTitle(FetchedDataCache.getSingleInstance().getUserRecord());

      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(TitleGUIUpdater.class);
    }
  }

}
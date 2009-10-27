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
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.event.*;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.util.PopupWindow;

import com.CH_co.cryptx.Rnd;
import com.CH_co.monitor.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.ContactFilterCo;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.actionGui.JActionFrame;
import com.CH_gui.dialog.*;
import com.CH_gui.gui.InactivityEventQueue;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.net.URLDecoder;
import java.util.*;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;


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
      loginCoordinator.loginAttemptCloseCurrentSession();
      loginCoordinator.setLoginProgMonitor(new LoginProgMonitor("Initializing ...", new String[] { "Loading Main Window" }));
      loginCoordinator.loginComplete(true, loginCoordinator);
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

    // initialize the stats labels
    Stats.installStatsLabelMouseAdapter(new StatsInitAndLabelMouseAdapter());

    // initialize a clean-up agent to run every 15 minutes for garbage-collection and every 60 minutes for temp file cleanup
    CleanupAgent.startSingleInstance(CleanupAgent.MODE_FINALIZATION | CleanupAgent.MODE_GC | CleanupAgent.MODE_TEMP_FILE_CLEANER, 
                     new String[][] { { FileDataRecord.TEMP_ENCRYPTED_FILE_PREFIX, null }, 
                                      { FileDataRecord.TEMP_PLAIN_FILE_PREFIX, null } }, 
                     1, 29, 29, 37, 37, 1, 71); // file cleaner will start early, but run infrequently

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
              final Long millisToKill = new Long(args[i]);
              new Thread() {
                public void run() {
                  try { Thread.sleep(millisToKill.longValue()); } catch (InterruptedException e) { }
                  Misc.systemExit(-1);
                }
              }.start();
            } catch (Throwable t) {
            }
          } else if (args[i].equalsIgnoreCase("-killAfterRandomMilliseconds")) {
            i ++;
            try {
              final Long rndMillisToKill = new Long(args[i]);
              new Thread() {
                public void run() {
                  Random rnd = new Random();
                  int delay = rnd.nextInt(rndMillisToKill.intValue());
                  try { Thread.sleep(delay); } catch (InterruptedException e) { }
                  Misc.systemExit(-1);
                }
              }.start();
            } catch (Throwable t) {
            }
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
          } else if (args[i].equalsIgnoreCase("-signup")) {
            LoginFrame.defaultMode = LoginFrame.MODE_SIGNUP;
          } else if (args[i].equalsIgnoreCase("-signupEmail")) {
            i++;
            LoginFrame.defaultSignupEmail = args[i];
          } else if (args[i].equalsIgnoreCase("-folderId")) {
            i++;
            try {
              initialFolderId = new Long(args[i]);
            } catch (Throwable t) {
            }
          } else if (args[i].equalsIgnoreCase("-msgLinkId")) {
            i++;
            try {
              initialMsgLinkId = new Long(args[i]);
            } catch (Throwable t) {
            }
          } else if (args[0].equals("-menuEditor")) {
            i++;
            String onOff = args[i];
            if (onOff.equalsIgnoreCase("on") || onOff.equalsIgnoreCase("true"))
              JActionFrame.ENABLE_MENU_CUSTOMIZATION_ACTION = true;
            else if (onOff.equalsIgnoreCase("off") || onOff.equalsIgnoreCase("false"))
              JActionFrame.ENABLE_MENU_CUSTOMIZATION_ACTION = false;
          } else if (args[i].equalsIgnoreCase("-?") || args[i].equalsIgnoreCase("/?") || args[i].equalsIgnoreCase("-help") || args[i].equalsIgnoreCase("/help")) {
            System.out.println("Usage 1: [-D[!]<propertiesDir>] [-privateLabelURL <URL>] [-username <username>] [<-password <password>>|<-password-blank>] [-signup] [-signupEmail <emailAddress>] [-folderId <id>] [-msgLinkId <id>]");
            System.out.println("Usage 2: -localKeyChangePass <localKeyId> <old name> <old password> <new name> <new password>");
            Misc.systemExit(-1);
          } else {
            System.out.println("Warning: invalid argument " + args[i]);
            //[-D[!]<propertiesDir>] [noLogin] [exitWhenMainFrameLoaded] [-privateLabelURL <URL>] [-username <username>] [-password <password>] [-password-blank] [-signup] [-signupEmail <emailAddress>] [-folderId <id>] [-msgLinkId <id>]
            //usageExit();
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

      // start initializing Secure Random we will need it either for key generation or encryption
      Rnd.getSecureRandom();

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
    if (trace != null) trace.clear();
  }

  private static void usageExit() {
    System.out.println(com.CH_gui.lang.Lang.rb.getString("usage_line_1"));
    System.out.println(com.CH_gui.lang.Lang.rb.getString("usage_line_2"));
    Misc.systemExit(-1);
  }


  public static void initLookAndFeelComponentDefaults() {
    UIDefaults table = UIManager.getLookAndFeelDefaults();

    Set keys = table.keySet();
    Vector keysV = new Vector(keys);
//    // make sure we have only strings as keys because jre v6 seems to return StringBuffer as well
//    for (int i=0; i<keysV.size(); i++) {
//      Object key = keysV.elementAt(i);
//      keysV.setElementAt(""+key, i);
//    }
    // odd menu bar border and tool bar border colors
    Color colorToChange1 = new Color(204, 204, 204);
    Color colorSubstitute1 = new Color(223, 221, 214);
    Color colorToChange2 = new Color(180, 195, 212);
    Color colorSubstitute2 = new Color(206, 219, 230);
    Object[] keysO = new Object[keysV.size()];
    keysV.toArray(keysO);
    Arrays.sort(keysO, new Comparator() { // use custom comparator because keys are of different class types...
      public int compare(Object o1, Object o2) {
        String s1 = "";
        String s2 = "";
        if (o1 != null) s1 = o1.toString();
        if (o2 != null) s2 = o2.toString();
        return s1.compareTo(s2);
      }
    });
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

    if (Toolkit.getDefaultToolkit().getScreenSize().width <= 800)
      MiscGui.setSmallScreen(true);

    AffineTransform at = null;

    // Change default labels and menu items to normal font attribute and black color, also make the default fonts little smaller
    Enumeration keyEnum = table.keys();
    while (keyEnum.hasMoreElements()) {
      Object key = keyEnum.nextElement();
      Object value = table.get(key);
      if (value instanceof FontUIResource) {
        FontUIResource fontUI = (FontUIResource) value;
        fontUI = new FontUIResource(fontUI.deriveFont(Font.PLAIN));
        if (at == null) {
          at = new AffineTransform();
          if (MiscGui.isSmallScreen()) {
            at.setToScale(0.9, 0.9); // any smaller than 0.9 and BOLD becomes NORMAL
          } else {
            at.setToScale(1.0, 1.0);
          }
        }
        fontUI = new FontUIResource(fontUI.deriveFont(at));
        table.put(key, fontUI); 
      }
    }

    // Fix the static labels in the Stats bar
    Stats.adjustFonts(at);

    // Change default label color to black
    try {
      /*
      String key = "Label.font";
      Font labelFont = (Font) table.get(key);
      labelFont = labelFont.deriveFont(Font.PLAIN);
      table.put(key, labelFont);
       */
      table.put("Label.foreground", Color.black);
    } catch (Throwable t) {
    }
//    Font sansSerif = new Font("SansSerif", Font.PLAIN, 12);
//    for (Enumeration enm = table.keys(); enm.hasMoreElements() ; ) {
//      Object key = enm.nextElement();
//      Object value = table.get(key);
//      /*
//      if (value instanceof Icon) {
//        System.out.println(key + "=" + value);
//      }
//       */
//      if (value instanceof Font) {
//        /*
//        Font font = (Font) value;
//        if (!font.isPlain()) {
//          font = font.deriveFont(Font.PLAIN);
//          table.put(key, font);
//        }
//         */
//        table.put(key, sansSerif);
//      }
//    }

    /*
    String fontName = System.getProperty("CryptoHeaven.font.name", "Arial");
    String fontSize = System.getProperty("CryptoHeaven.font.normal.size", "12");
    Font arialPlain = new Font(fontName, Font.PLAIN, Integer.parseInt(fontSize));
    ColorUIResource black = new ColorUIResource(Color.black);
    Object[] defaults = {      //fonts
      "Button.font", arialPlain,
      "MenuBar.font", arialPlain,
      "Menu.font", arialPlain,
      "MenuItem.font", arialPlain,
      "CheckBox.font", arialPlain,
      "CheckBoxMenuItem.font", arialPlain,
      "ToggleButton.font", arialPlain,
      "RadioButton.font", arialPlain,
      "ToolTip.font", arialPlain,
      "ProgressBar.font", arialPlain,
      "Panel.font", arialPlain,
      "MenuItem.acceleratorFont", arialPlain,
      "CheckBoxMenuItem.acceleratorFont", arialPlain,
      "PopupMenu.font", arialPlain,
      "Label.font", arialPlain,
      "List.font", arialPlain,
      "ComboBox.font", arialPlain,
      "TextField.font", arialPlain,
      "PasswordField.font", arialPlain,
      "TextArea.font", arialPlain,
      "TextPane.font", arialPlain,
      "EditorPane.font", arialPlain,
      "ScrollPane.font", arialPlain,
      "TabbedPane.font", arialPlain,
      "Table.font", arialPlain,
      "TableHeader.font", arialPlain,
      "TitledBorder.font", arialPlain,
      "ToolBar.font", arialPlain,
      "ProgressBar.font", arialPlain,
      "OptionPane.font", arialPlain,
      "ColorChooser.font", arialPlain,
      "InternalFrame.titleFont", arialPlain,
      //Colours
      "ComboBox.disabledForeground",  black,
      "TextField.inactiveForeground", black,
      "TextArea.inactiveForeground",  black
    };
    table.putDefaults(defaults);
     */
  }

  private static class StarterLoginCoordinator implements LoginCoordinatorI {
    private Long initialFolderId;
    private Long initialMsgLinkId;

    private StarterLoginCoordinator(Long initialFolderId, Long initialMsgLinkId) {
      this.initialFolderId = initialFolderId;
      this.initialMsgLinkId = initialMsgLinkId;
    }
            
    private LoginProgMonitor monitor;

    public LoginProgMonitor getLoginProgMonitor() {
      return monitor;
    }

    public void loginAttemptCloseCurrentSession() {
    }

    public void loginComplete(boolean success, final LoginCoordinatorI loginCoordinator) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StarterLoginCoordinator.class, "loginComplete(boolean success, loginCoordinator)");
      if (trace != null) trace.args(success);
      if (trace != null) trace.args(loginCoordinator);

      final Frame[] mainStartupFrame = new Frame[1];

      if (success) {
        if (trace != null) trace.data(10, "advance progress monitor, login is complete");
        if (loginCoordinator != null) {
          loginCoordinator.getLoginProgMonitor().nextTask();
          loginCoordinator.getLoginProgMonitor().setCurrentStatus(com.CH_gui.lang.Lang.rb.getString("label_Loading_Main_Program..._Please_Wait."));
        }

//        // All parent-less dialogs should go on top of the main window from now on.
//        GeneralDialog.setDefaultParent(this);

        // Mark Active Status right away since the GUI timer is scheduled in intervals... 
        // if user was disconnected in INACTIVE state, he should be marked active now...
        InactivityEventQueue.getInstance().sendActiveFlagIfInactive();

        // Get the folders and contacts when login Completes...
        ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
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
                    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");
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
          loginCoordinator.readyForMainData();
        }

        if (trace != null) trace.data(20, "closing down the Login Progress Monitor");
        if (loginCoordinator != null) {
          loginCoordinator.getLoginProgMonitor().allDone();
          loginCoordinator.setLoginProgMonitor(null);
        }

        if (mainStartupFrame[0] == null) {
          MainFrame mainFrame = new MainFrame(null, null, null);
          mainFrame.setLoginProgMonitor(new LoginProgMonitor("Initializing ...", new String[] { "Loading Main Window" }));
          mainFrame.loginComplete(true, mainFrame);
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

    public void readyForMainData() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MainFrameStarter.class, "readyForMainData()");
      // fetch contacts, root folders (including Sent folder for replying to message), etc...
      MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.USR_Q_GET_RECONNECT_UPDATE));
      if (trace != null) trace.exit(MainFrameStarter.class);
    }

    public void setLoginProgMonitor(LoginProgMonitor loginProgMonitor) {
      monitor = loginProgMonitor;
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
      this.event = event;
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactGUIUpdater.class, "run()");

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
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TitleGUIUpdater.class, "run()");

      Frame frame = GeneralDialog.getDefaultParent();
      if (frame != null && frame instanceof JActionFrame)
        ((JActionFrame) frame).setUserTitle(FetchedDataCache.getSingleInstance().getUserRecord());

      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(TitleGUIUpdater.class);
    }
  }

}
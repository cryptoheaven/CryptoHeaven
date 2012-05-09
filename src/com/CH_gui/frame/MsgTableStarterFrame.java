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

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_co.monitor.ProgMonitorFactory;
import com.CH_co.service.records.MsgLinkRecord;
import com.CH_co.service.records.Record;
import com.CH_co.service.records.UserRecord;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_co.util.DisposableObj;
import com.CH_co.util.ImageNums;
import com.CH_gui.action.AbstractActionTraced;
import com.CH_gui.action.ActionUtilities;
import com.CH_gui.action.Actions;
import com.CH_gui.monitor.LoginProgMonitorImpl;
import com.CH_gui.monitor.StatsBar;
import com.CH_gui.util.ActionProducerI;
import com.CH_gui.util.Images;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.SwingUtilities;

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
 * <b>$Revision: 1.3 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class MsgTableStarterFrame extends MsgTableFrame implements ActionProducerI, DisposableObj {

  private Action[] actions;
  private static final int SWITCH_TO_FULL_APPLICATION = 0;
  private StatsBar statsBar;

  /** Creates new MsgTableStarterFrame */
  protected MsgTableStarterFrame(Record parent, MsgLinkRecord[] initialData, boolean isInitDataModel) {
    super(parent, initialData, isInitDataModel);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTableStarterFrame.class, "MsgTableStarterFrame()");
    UserRecord uRec = FetchedDataCache.getSingleInstance().getUserRecord();
    setUserTitle(uRec);
    statsBar = new StatsBar();
    statsBar.installListeners();
    getContentPane().add(statsBar, BorderLayout.SOUTH);
    if (trace != null) trace.exit(MsgTableStarterFrame.class);
  }

  private void initActions() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTableStarterFrame.class, "initActions()");
    int leadingActionId = Actions.LEADING_ACTION_ID_MSG_TABLE_FRAME;
    actions = new Action[6];
    int actionIndex = 0;
    actions[actionIndex] = new SwitchToFullAction(leadingActionId + SWITCH_TO_FULL_APPLICATION);
    actionIndex ++;
    // Include Help menu from Main Frame.
    int leadingActionMainFrameId = Actions.LEADING_ACTION_ID_MAIN_FRAME;
    actions[actionIndex] = new MainFrame.AboutAction(leadingActionMainFrameId + MainFrame.ABOUT_ACTION);
    actionIndex ++;
    actions[actionIndex] = new MainFrame.URLGeneralFAQAction(leadingActionMainFrameId + MainFrame.URL__GENERAL_FAQ_ACTION);
    actionIndex ++;
    actions[actionIndex] = new MainFrame.URLQuickTourAction(leadingActionMainFrameId + MainFrame.URL__QUICK_TOUR_ACTION);
    actionIndex ++;
    actions[actionIndex] = new MainFrame.URLUsersGuideAction(leadingActionMainFrameId + MainFrame.URL__USERS_GUIDE_ACTION);
    actionIndex ++;
    actions[actionIndex] = new MainFrame.URLAccountUpgradeAction(leadingActionMainFrameId + MainFrame.URL__ACCOUNT_UPGRADE);
    actionIndex ++;
    if (trace != null) trace.exit(MsgTableStarterFrame.class);
  }

  // =====================================================================
  // LISTENERS FOR THE MENU ITEMS
  // =====================================================================

  /**
   * Switch to Full App.
   **/
  private class SwitchToFullAction extends AbstractActionTraced {
    public SwitchToFullAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Switch_To_Full_App"));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Switch_To_Full_App."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.FRAME_LOCK32));
    }
    public void actionPerformedTraced(ActionEvent event) {
      // do it in the non-GUI thread so it doesn't block the display
      Thread th = new ThreadTraced("Switch To Full Runner") {
        public void runTraced() {
          if (MainFrame.getSingleInstance() == null) {
            final MainFrame mainFrame = new MainFrame(MsgTableStarterFrame.this, null, null);
            // Set Main Frame as the parent to the progress dialog.
            LoginFrame.loginFrameForProgress = mainFrame;
            try {
              // Create progress monitor, but ignore any exceptions as it is not necessary that we have it.
              mainFrame.setLoginProgMonitor(ProgMonitorFactory.newInstanceLogin("Initializing ...", new String[] { "Loading Main Window" }, null));
            } catch (Throwable t) {
            }
            mainFrame.loginComplete(MainFrame.getServerInterfaceLayer(), true);
            LoginFrame.loginFrameForProgress = null;
            SwingUtilities.invokeLater(new Runnable() { 
              public void run() {
                mainFrame.setVisible(true);
              }
            });
          }
        }
      };
      th.setDaemon(false);
      th.start();
    }
  }

  /*********************************************/
  /**    A c t i o n   P r o d u c e r  I     **/
  /*********************************************/

  /** @return all the acitons that this objects produces.
   */
  public Action[] getActions() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTableStarterFrame.class, "getActions()");
    if (actions == null) {
      initActions();
    }
    Action[] a = ActionUtilities.concatinate(super.getActions(), actions);
    if (trace != null) trace.exit(MsgTableStarterFrame.class, a);
    return a;
  }

  /**
   * I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
   * Dispose the object and release resources to help in garbage collection.
   */
  private boolean isDisposed = false;
  public void disposeObj() {
    if (!isDisposed) {
      isDisposed = true;
      statsBar.uninstallListeners();
      super.disposeObj();
    }
  }

  public void dispose() {
    super.dispose();
    disposeObj();
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "MsgTableStarterFrame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
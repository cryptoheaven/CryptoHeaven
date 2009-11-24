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

package com.CH_gui.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.cache.event.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.MessageDialog;
import com.CH_gui.frame.MainFrame;

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
 * <b>$Revision: 1.5 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class InactivityEventQueue extends EventQueue implements ActionListener {

  private static InactivityEventQueue instance = null;
  private boolean isActive;
  private static int interval = UserSettingsRecord.DEFAULT__AWAY_MINUTES*60*1000;
  private Timer timer;
  private long lastActiveTime;

  private static UserSettingsListener userSettingsListener;

  private static long memoryErrorMessageStamp;

  static {
    String timeout = System.getProperty("InactiveTimeout"); // Timeout property in minutes
    if (timeout != null)
      interval = Integer.parseInt(timeout) * 60000;
    instance = new InactivityEventQueue();
    userSettingsListener = new UserSettingsListener();
    FetchedDataCache.getSingleInstance().addUserSettingsRecordListener(userSettingsListener);
  }

  public static InactivityEventQueue getInstance() {
    return (instance);
  }

  public void sendActiveFlagIfInactive() {
    UserRecord uRec = FetchedDataCache.getSingleInstance().getUserRecord();
    if (uRec != null) {
      if (ContactRecord.onlineCharToFlag(uRec.online).shortValue() == ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_INACTIVE) {
        Character onlineFlag = ContactRecord.onlineFlagToChar(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AVAILABLE);
        // mark our local user record online so that we don't try sending another request if we are disconnected and interval passes
        uRec.online = onlineFlag;
        MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.USR_Q_CHANGE_ONLINE_STATUS, new Obj_List_Co(onlineFlag)));
      }
    }
  }

  public void setActive(boolean flag) {
    if (!isActive && flag) {
      // Change state from inactive to active...
      sendActiveFlagIfInactive();
    }
    isActive = flag;
    if (isActive) {
      lastActiveTime = System.currentTimeMillis();
    }
  }

  private InactivityEventQueue() {
    initialize();
  }

  private void initialize() {
    if (interval != 0) {
      if (timer == null) {
        isActive = false;
        lastActiveTime = System.currentTimeMillis();
        // Initialize it with 15 sec initial interval... Due to a bug in the timer which prevents
        // proper action fireing if the interval is decreased and timer restarted... 
        // The delay will be corrected after this initial interval expires and action event is fired.
        //timer = new Timer(interval, this);
        timer = new Timer(15*1000, this);
        timer.start();
      } else if (!timer.isRunning()) {
        timer.restart();
      }
    } else {
      if (timer != null) {
        timer.stop();
      }
    }
  }

  protected void dispatchEvent(AWTEvent event) {
    try {
      super.dispatchEvent(event);
      switch (event.getID()) {
        case MouseEvent.MOUSE_PRESSED :
        case MouseEvent.MOUSE_RELEASED :
        case MouseEvent.MOUSE_MOVED :
        case MouseEvent.MOUSE_ENTERED :
        case KeyEvent.KEY_PRESSED :
        case KeyEvent.KEY_RELEASED :
          setActive(true);
          break;
        case WindowEvent.WINDOW_DEICONIFIED :
          setActive(true);
          break;
        case WindowEvent.WINDOW_ACTIVATED :
          setActive(true);
          break;
        case WindowEvent.WINDOW_DEACTIVATED :
          setActive(true);
          break;
  //      case WindowEvent.WINDOW_ICONIFIED : // chat popup window opens iconified window...
  //        setActive(true);
  //        break;
        case FocusEvent.FOCUS_GAINED :
          setActive(true);
          break;
        case FocusEvent.FOCUS_LOST :
          setActive(true);
          break;
      }
    } catch (OutOfMemoryError e) {
      e.printStackTrace();
      if (System.currentTimeMillis() - memoryErrorMessageStamp > 60000L) {
        memoryErrorMessageStamp = System.currentTimeMillis();
        try {
          MessageDialog.showErrorDialog(null, "Sufficient amount of memory could not be allocated.  Please close some of your unused windows and retry.", "Memory heap full");
        } catch (Throwable t) {
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  private void setDelayToRemainder() {
    // Check the elapsed time since the last event
    long currentTime = System.currentTimeMillis();
    int elapsedTime = (int) (currentTime - lastActiveTime);
    int delay = interval - elapsedTime;
    // set additional delay to complete the interval
    if (delay < 1)
      delay = 1;
    timer.setDelay(delay);
  }
  public void actionPerformed(ActionEvent e) {
    if (isActive) {
      setDelayToRemainder();
      // Set the flag and start the countdown
      isActive = false;
    } else {
      // set the delay to the full interval...
      timer.setDelay(interval);
      // No new event made the application inActive after countdown expired...
      // Change state from Active to Inactive
      UserRecord uRec = FetchedDataCache.getSingleInstance().getUserRecord();
      if (uRec != null) {
        if (ContactRecord.onlineCharToFlag(uRec.online).shortValue() == ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE ||
            ContactRecord.onlineCharToFlag(uRec.online).shortValue() == ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AVAILABLE
            )
        {
          Character inactiveFlag = ContactRecord.onlineFlagToChar(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_INACTIVE);
          // mark our local user record inactive so that we don't try sending another request if we are dicsonnected and interval passes
          uRec.online = inactiveFlag;
          MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.USR_Q_CHANGE_ONLINE_STATUS, new Obj_List_Co(inactiveFlag)));
        }
      }
    }
  }

  private static class UserSettingsListener extends Object implements UserSettingsRecordListener {
    public void userSettingsRecordUpdated(UserSettingsRecordEvent e) {
      if (e.getEventType() == RecordEvent.SET)
        javax.swing.SwingUtilities.invokeLater(new UserSettingsUpdater(e));
    }
  }

  private static class UserSettingsUpdater implements Runnable {
    private UserSettingsRecordEvent e;
    public UserSettingsUpdater(UserSettingsRecordEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserSettingsUpdater.class, "UserSettingsUpdater(UserSettingsRecordEvent event)");
      this.e = event;
      if (trace != null) trace.exit(UserSettingsUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserSettingsUpdater.class, "UserSettingsUpdater.run()");
      if (e.getEventType() == RecordEvent.SET) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        UserSettingsRecord userSettingsRecord = cache.getMyUserSettingsRecord();
        int newInterval = userSettingsRecord.awayMinutes.intValue() * 60*1000;
        if (newInterval != InactivityEventQueue.interval) {
          InactivityEventQueue.interval = newInterval;
          InactivityEventQueue eventQueue = InactivityEventQueue.getInstance();
          eventQueue.initialize();
          eventQueue.setDelayToRemainder();
        }
      }
      if (trace != null) trace.exit(UserSettingsUpdater.class);
    }
  }

}
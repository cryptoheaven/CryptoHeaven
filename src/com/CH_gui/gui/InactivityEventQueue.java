/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.gui;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.event.RecordEvent;
import com.CH_cl.service.cache.event.UserSettingsRecordEvent;
import com.CH_cl.service.cache.event.UserSettingsRecordListener;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.service.records.ContactRecord;
import com.CH_co.service.records.UserRecord;
import com.CH_co.service.records.UserSettingsRecord;
import com.CH_co.trace.Trace;
import com.CH_co.util.MyUncaughtExceptionHandlerOps;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.util.MessageDialog;
import com.CH_gui.util.SpellCheckerWrapper;
import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.*;
import javax.swing.Timer;


/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.5 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class InactivityEventQueue extends EventQueue implements ActionListener {

  private static InactivityEventQueue instance = null;
  private boolean isActive;
  private static int interval = UserSettingsRecord.DEFAULT__AWAY_MINUTES*60*1000;
  private Timer timer;
  private long lastActiveTime;
  private Boolean isEligibleToSendInactiveState;

  private boolean isTimeToCheck;
  private Timer timeToCheckHeartbeat;

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

  private InactivityEventQueue() {
    initialize();
  }

  private void initialize() {
    if (interval != 0) {
      if (timer == null) {
        isActive = true;
        lastActiveTime = System.currentTimeMillis();
        // Initialize it with 13 sec initial interval... Due to a bug in the timer which prevents
        // proper action fireing if the interval is decreased and timer restarted...
        // The delay will be corrected after this initial interval expires and action event is fired.
        timer = new Timer(13*1000, this);
        timer.start();
      } else if (!timer.isRunning()) {
        timer.restart();
      }
      if (timeToCheckHeartbeat == null) {
        // beats every 7 seconds and flips the flag for marking activity timestamp
        timeToCheckHeartbeat = new Timer(7*1000, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            isTimeToCheck = true;
          }
        });
        timeToCheckHeartbeat.start();
      } else if (!timeToCheckHeartbeat.isRunning()) {
        timeToCheckHeartbeat.restart();
      }
    } else {
      if (timer != null) {
        timer.stop();
      }
      if (timeToCheckHeartbeat != null) {
        timeToCheckHeartbeat.stop();
      }
    }
  }


  public void sendActiveFlagIfInactive() {
    UserRecord uRec = FetchedDataCache.getSingleInstance().getUserRecord();
    if (uRec != null) {
      ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
      if (SIL.hasPersistentMainWorker()) {
        if (ContactRecord.onlineCharToFlag(uRec.online).shortValue() == ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_INACTIVE) {
          Character onlineFlag = ContactRecord.onlineFlagToChar(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AVAILABLE);
          // mark our local user record online so that we don't try sending another request if we are disconnected and interval passes
          uRec.online = onlineFlag;
          SIL.submitAndReturn(new MessageAction(CommandCodes.USR_Q_CHANGE_ONLINE_STATUS, new Obj_List_Co(onlineFlag)));
        }
      }
    }
  }

  public void setActive(boolean flag) {
    isEligibleToSendInactiveState = Boolean.TRUE;
    isActive = flag;
    if (isActive) {
      // change to active contact if needed
      sendActiveFlagIfInactive();
      // mark last active time
      lastActiveTime = System.currentTimeMillis();
    } else {
      // if disabled the flag then give chance to flip it back right away without waiting for secondary timer
      isTimeToCheck = true;
    }
  }

  public void actionPerformed(ActionEvent e) {
    if (isActive) {
      // Set the flag and start the countdown
      setActive(false);
      // after setting the flag delay the remainder
      setDelayToRemainder();
    } else {
      // set the delay to the full interval...
      timer.setDelay(interval);
      // skip turning off active state by other multiple client sessions that are sitting dormant.
      if (isEligibleToSendInactiveState == null || isEligibleToSendInactiveState.equals(Boolean.TRUE)) {
        // No new event made the application inActive after countdown expired...
        // Change state from Active to Inactive
        UserRecord uRec = FetchedDataCache.getSingleInstance().getUserRecord();
        if (uRec != null) {
          if (ContactRecord.onlineCharToFlag(uRec.online).shortValue() == ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE ||
              ContactRecord.onlineCharToFlag(uRec.online).shortValue() == ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AVAILABLE)
          {
            Character inactiveFlag = ContactRecord.onlineFlagToChar(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_INACTIVE);
            // mark our local user record inactive so that we don't try sending another request if we are dicsonnected and interval passes
            uRec.online = inactiveFlag;
            MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.USR_Q_CHANGE_ONLINE_STATUS, new Obj_List_Co(inactiveFlag)));
            isEligibleToSendInactiveState = Boolean.FALSE;
          }
        }
      }
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

  protected void dispatchEvent(AWTEvent event) {
    try {
      super.dispatchEvent(event);
      if (isTimeToCheck) {
        // turn off flag only if new activity was marked and timestamp updated
        if (checkActivity(event)) {
          isTimeToCheck = false;
        }
      }
    } catch (OutOfMemoryError e) {
      e.printStackTrace();
      if (System.currentTimeMillis() - memoryErrorMessageStamp > 60000L) {
        memoryErrorMessageStamp = System.currentTimeMillis();
        try {
          MessageDialog.showErrorDialog(null, "Sufficient amount of memory could not be allocated. Please restart the program or close unused windows and retry.", "Memory heap full");
        } catch (Throwable t) {
        }
      }
    } catch (Throwable t) {
      MyUncaughtExceptionHandlerOps.unhandledException(t);
    }
  }

  /**
   * @param event
   * @return true if activity was marked in this call
   */
  private boolean checkActivity(AWTEvent event) {
    boolean isActivity = false;
    switch (event.getID()) {
      case MouseEvent.MOUSE_PRESSED :
      case MouseEvent.MOUSE_RELEASED :
      case MouseEvent.MOUSE_MOVED :
      case MouseEvent.MOUSE_ENTERED :
      case KeyEvent.KEY_PRESSED :
      case KeyEvent.KEY_RELEASED :
      case WindowEvent.WINDOW_DEICONIFIED :
      case WindowEvent.WINDOW_ACTIVATED :
      case WindowEvent.WINDOW_DEACTIVATED :
      case FocusEvent.FOCUS_GAINED :
      case FocusEvent.FOCUS_LOST :
        setActive(true);
        isActivity = true;
        break;
      // skip:
      // case WindowEvent.WINDOW_ICONIFIED : // chat popup window opens iconified window without user intervention...
    }
    return isActivity;
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
        if (userSettingsRecord.spellingProps != null) {
          try {
            SpellCheckerWrapper.setOptionsFromProperties(userSettingsRecord.spellingProps);
          } catch (Throwable t) {
            t.printStackTrace();
          }
        }
      }
      if (trace != null) trace.exit(UserSettingsUpdater.class);
    }
  }

}
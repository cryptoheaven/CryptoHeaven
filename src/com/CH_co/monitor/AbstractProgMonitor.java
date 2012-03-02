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

package com.CH_co.monitor;

import com.CH_co.trace.Trace;
import com.CH_co.service.msg.*;

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
 * <b>$Revision: 1.14 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
abstract public class AbstractProgMonitor extends Object {

  private String name;

  private static final Object counterMonitor = new Object();
  private static int counter = 0;

  protected boolean allDone;
  protected boolean cancelled;
  protected boolean killed;

  /** Creates new AbstractProgMonitor */
  public AbstractProgMonitor() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AbstractProgMonitor.class, "AbstractProgMonitor()");
    synchronized (counterMonitor) {
      name = AbstractProgMonitor.class.getName() + " #" + counter;
      if (trace != null) trace.data(10, "creating... ", name);
      counter ++;
      if (counter == Integer.MAX_VALUE)
        counter = 0;
    }
    if (trace != null) trace.exit(AbstractProgMonitor.class);
  }

  public boolean isAllDone() {
    return allDone;
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public boolean isJobKilled() {
    return killed;
  }

  public void enqueue(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AbstractProgMonitor.class, "enqueue(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    Stats.moveGlobe(this);
//    Stats.setStatus("New request created ... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(AbstractProgMonitor.class);
  }

  public void dequeue(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AbstractProgMonitor.class, "dequeue(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    //Stats.setStatus("Preparing to send ... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(AbstractProgMonitor.class);
  }

  public void startSend(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AbstractProgMonitor.class, "startSend(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    Stats.moveGlobe(this);
//    Stats.setStatus("Sending request ... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(AbstractProgMonitor.class);
  }

  public void doneSend(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AbstractProgMonitor.class, "doneSend(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
//    if (!CommandCodes.isCodeForShortenStatusNotification(actionCode))
//      Stats.setStatus("Waiting for reply ... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(AbstractProgMonitor.class);
  }

  public void startReceive(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AbstractProgMonitor.class, "startReceive(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
//    if (!CommandCodes.isCodeForShortenStatusNotification(actionCode))
//      Stats.setStatus("Receiving reply ... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(AbstractProgMonitor.class);
  }

  public void doneReceive(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AbstractProgMonitor.class, "doneReceive(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    Stats.stopGlobe(this); // fixing ever-spinning globe
    //// No need for additional status line as "Executing reply" will follow without any potential delay
    //Stats.setStatus("Reply received. [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(AbstractProgMonitor.class);
  }

  public void startExecution(int actionCode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AbstractProgMonitor.class, "startExecution(int actionCode)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.data(10, name);
//    if (!CommandCodes.isCodeForShortenStatusNotification(actionCode))
//      Stats.setStatus("Executing reply ... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(AbstractProgMonitor.class);
  }
  public void doneExecution(int actionCode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AbstractProgMonitor.class, "doneExecution(int actionCode)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.data(10, name);
//    Stats.setStatus("Action completed. [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    //Stats.stopGlobe(this); // fixing ever-spinning globe
    if (trace != null) trace.exit(AbstractProgMonitor.class);
  }

  public void allDone() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AbstractProgMonitor.class, "allDone()");
    if (trace != null) trace.data(10, name);
    allDone = true;
    Stats.stopGlobe(this);
    if (trace != null) trace.exit(AbstractProgMonitor.class);
  }

  public void jobKilled() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AbstractProgMonitor.class, "jobKilled()");
    if (trace != null) trace.data(10, name);
    killed = true;
    allDone();
    //Stats.stopGlobe(this);
    if (trace != null) trace.exit(AbstractProgMonitor.class);
  }

  public void jobForRetry() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AbstractProgMonitor.class, "jobForRetry()");
    if (trace != null) trace.data(10, name);
    Stats.stopGlobe(this);
    if (trace != null) trace.exit(AbstractProgMonitor.class);
  }
}
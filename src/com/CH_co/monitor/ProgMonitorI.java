/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_co.monitor;

/** 
 * <b>Copyright</b> &copy; 2001-2011
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
 * <b>$Revision: 1.13 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public interface ProgMonitorI {

  public void enqueue(int actionCode, long stamp);
  public void dequeue(int actionCode, long stamp);

  public void startSend(int actionCode, long stamp);
  public void startSendAction(String actionName);
  public void startSendData(String dataName);

  public void doneSend(int actionCode, long stamp);
  public void doneSendAction(String actionName);
  public void doneSendData(String dataName);


  public void startReceive(int actionCode, long stamp);
  public void startReceiveAction(String actionName);
  public void startReceiveData(String dataName);

  public void doneReceive(int actionCode, long stamp);
  public void doneReceiveAction(String actionName);
  public void doneReceiveData(String dataName);


  public void startExecution(int actionCode);
  public void doneExecution(int actionCode);


  public void setCurrentStatus(String currentStatus);

  public void setFileNameSource(String fileName);
  public void setFileNameDestination(String fileName);

  public void setTransferSize(long size);
  public void addBytes(long bytes);
  public void doneTransfer();

  public void nextTask(String task);
  public void appendLine(String info);
  
  public void nextTask();
  public void allDone();
  public void jobKilled();
  public void jobForRetry();

  public void setInterrupt(Interruptible interruptible);
  public void setCancellable(Cancellable cancellable);

  public boolean isAllDone();
  public boolean isCancelled();
  public boolean isJobKilled();
}
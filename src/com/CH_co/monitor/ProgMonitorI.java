/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.monitor;

/** 
* Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.13 $</b>
*
* @author  Marcin Kurzawa
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
  public void setFilePathDestination(String fileName);

  public void setTransferSize(long size);
  public void updateTransferSize(long size);
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
  public void interruptAndCancel();

  public boolean isAllDone();
  public boolean isCancelled();
  public boolean isJobKilled();

  // pooling values from monitor
  public long getTransferred();
  public long getTransferSize();
  public String getLastStatusInfo();
  public String getLastStatusTitle();

}
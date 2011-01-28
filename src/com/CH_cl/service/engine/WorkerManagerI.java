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

package com.CH_cl.service.engine;

import com.CH_co.service.msg.MessageAction;

/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public interface WorkerManagerI extends RequestSubmitterI {


  /**
   * Logs out all workers and disconnects them.
   * This call causes loss of all knowledge about connections for workers and data cache
   */
  public void logoutWorkers();

  /**
   * Sets the last successful login action, called by workers after successful login.
   */
  public void setLoginMsgAction(MessageAction loginMsgAction);
  public boolean isLastLoginMsgActionSet();

  /**
   * Marks the timestamp of last activity.
   */
  public void markLastWorkerActivityStamp();

  /**
   * Worker notifies the manager that it quit processing and it will no longer be active.
   */
  public void workerDone(ServerInterfaceWorker worker, boolean cleanLogout, boolean suppressConnectionTypePenalization);

  /**
   * @return maximum number of workers this manager can have
   */
  public int getMaxWorkerCount();

  /**
   * @return maximum number of heavy workers this manager can have
   */
  public int getMaxHeavyWorkerCount();

  /** 
   * @return true if there is a designated Main Worker.
   */
  public boolean hasMainWorker();

  /**
   * @return true if running in client mode, false for server mode
   */
  public boolean isClientMode();

  /**
   * @return true if Manager is destroyed
   */
  public boolean isDestroyed();

  /**
   * @return true if the specified worker is registered as the Main Worker.
   */
  public boolean isMainWorker(ServerInterfaceWorker worker);

  /**
   * The worker that picks up the SYS_Q_NOTIFY message action should claim Main Worker status.
   */
  public void claimMainWorker(ServerInterfaceWorker worker);

  /**
   * Push-back a request to be placed on the job queue again.
   */
  public void pushbackRequest(MessageAction msgAction);

  /**
   * Called to inform that some worker has completed a login sequence, successfully or not.
   */
  public void workerLoginComplete(ServerInterfaceWorker worker, boolean loginSuccessful);

  /**
   * Sets the remote session id for the case when engine SIL logs in to another engine.
   */
  public void setRemoteSessionID(Long remoteSessionID);

}
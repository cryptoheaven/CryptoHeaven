/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_cl.service.engine;

import com.CH_co.monitor.ProgMonitorI;

/**
 * <b>Copyright</b> &copy; 2001-2013
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
 * <b>$Revision: 1.2 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public interface LoginCoordinatorI {

  public ProgMonitorI getLoginProgMonitor();
  public void loginAttemptCloseCurrentSession(ServerInterfaceLayer SIL);
  public void loginComplete(ServerInterfaceLayer SIL, boolean isSuccess);
  public void readyForMainData(ServerInterfaceLayer SIL);
  public void setLoginProgMonitor(ProgMonitorI progMonitor);
  public void startPreloadingComponents_Threaded();

}
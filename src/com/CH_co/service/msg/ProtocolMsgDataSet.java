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

package com.CH_co.service.msg;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.util.Misc;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;

/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>  
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public abstract class ProtocolMsgDataSet extends Object {

  private Boolean isEngineToEngine;
  private Long serverSessionUserId;
  private Timestamp currentStamp;
  private Date currentStampRecordDate;

  /** Creates new ProtocolMsgDataSet */
  public ProtocolMsgDataSet() {
  }

  public abstract void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException;
  public abstract void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException;

  /**
   * @return User displayable name of this data set, classes should overwrite this for progress monitoring.
   */
  public String getDataSetInfoName() {
    return Misc.getClassNameWithoutPackage(getClass());
  }

  protected Timestamp getServerSessionCurrentStamp() {
    if (currentStampRecordDate != null && currentStamp != null)
      return new Timestamp((System.currentTimeMillis() - currentStampRecordDate.getTime()) + currentStamp.getTime());
    else
      return null;
  }

  protected Long getServerSessionUserId() {
    return serverSessionUserId;
  }

  public boolean isEngineToEngine() {
    return isEngineToEngine.booleanValue();
  }

  public boolean isTimeSensitive() {
    return false;
  }

  public boolean isUserSensitive() {
    return false;
  }

  public void setServerSessionCurrentStamp(Timestamp ts) {
    currentStamp = ts;
    currentStampRecordDate = new Date();
  }
  public void setServerSessionUserId(Long userId) {
    serverSessionUserId = userId;
    if (userId == null)
      isEngineToEngine = Boolean.TRUE;
    else
      isEngineToEngine = Boolean.FALSE;
  }

  public String toStringLongFormat() {
    return toString();
  }
}
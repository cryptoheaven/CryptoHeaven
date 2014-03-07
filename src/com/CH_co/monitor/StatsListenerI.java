/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.monitor;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public interface StatsListenerI {

  public void setStatsConnections(Integer connectionsPlain, Integer connectionsHTML);
  public void setStatsGlobeMove(Boolean isMoving);
  public void setStatsLastStatus(String status);
  public void setStatsPing(Long pingMS);
  public void setStatsSizeBytes(Long sizeBytes);
  public void setStatsTransferRate(Long transferRate);
  public void setStatsTransferRateIn(Long transferRateIn);
  public void setStatsTransferRateOut(Long transferRateOut);

}
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

import com.CH_cl.service.cache.*;

import com.CH_co.monitor.*;
import com.CH_co.trace.Trace;
import com.CH_co.queue.*;
import com.CH_co.service.records.*;

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
 * <b>$Revision: 1.18 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class JobFifo extends PriorityJobFifo {

  private FetchedDataCache cache;

  /** Creates new JobFifo */
  public JobFifo() {
    super();
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JobFifo.class, "JobFifo()");
    cache = FetchedDataCache.getSingleInstance();
    if (trace != null) trace.exit(JobFifo.class);
  }

  public long getFileOrigSizeSum(Long[] fileLinkIDs) {
    FileLinkRecord[] fileLinks = cache.getFileLinkRecords(fileLinkIDs);
    long sizeSum = FileLinkRecord.getFileOrigSizeSum(fileLinks);
    return sizeSum;
  }

  public long getMaxFileDownSizeForMainConnection() {
    long maxTransferProportional = Stats.getMaxTransferRateIn()*1; // 1 second worth of download
    long maxSize = Math.max(maxTransferProportional, ServerInterfaceLayer.DEFAULT_MAX_FILE_SIZE_FOR_MAIN_CONNECTION);
    return maxSize;
  }

  public long getMaxFileUpSizeForMainConnection() {
    long maxTransferProportional = Stats.getMaxTransferRateOut()*1; // 1 second worth of upload
    long maxSize = Math.max(maxTransferProportional, ServerInterfaceLayer.DEFAULT_MAX_FILE_SIZE_FOR_MAIN_CONNECTION);
    return maxSize;
  }

}
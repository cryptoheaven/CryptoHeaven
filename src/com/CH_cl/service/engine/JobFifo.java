/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.engine;

import com.CH_cl.service.cache.*;

import com.CH_co.monitor.*;
import com.CH_co.trace.Trace;
import com.CH_co.queue.*;
import com.CH_co.service.records.*;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.18 $</b>
 *
 * @author  Marcin Kurzawa
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
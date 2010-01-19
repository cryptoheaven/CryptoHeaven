/*
 * Copyright 2001-2010 by CryptoHeaven Development Team,
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

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.file.*;
import com.CH_co.service.msg.dataSets.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
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

  public long getMaxFileSizeForMainConnection() {
    long maxTransferProportional = Stats.getMaxTransferRate()*5; // 5 seconds worth of transfer
    long maxSize = Math.max(maxTransferProportional, ServerInterfaceLayer.DEFAULT_MAX_FILE_SIZE_FOR_MAIN_CONNECTION);
    return maxSize;
  }

}
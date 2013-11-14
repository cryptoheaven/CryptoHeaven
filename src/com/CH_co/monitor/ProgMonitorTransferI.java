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

import com.CH_co.service.records.FileLinkRecord;
import java.io.File;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public interface ProgMonitorTransferI extends ProgMonitorI {


  /**
  * File Download/Open
  */
  public void init(Object context, String[] tasks, File destDir, FileLinkRecord[] fileLinks, boolean isDownload, boolean suppressTransferSoundsAndAutoClose);

  /**
  * File Upload
  */
  public void init(Object context, File[] tasks);

  public boolean isMonitoringDownload();
  public boolean isMonitoringOpen();
  public void setOpenWhenFinished(boolean flag);

}
/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
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
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public interface ProgMonitorTransferI extends ProgMonitorI {


  /**
   * File Download/Open
   */
  public void init(String[] tasks, File destDir, FileLinkRecord[] fileLinks, boolean isDownload, boolean suppressTransferSoundsAndAutoClose);

  /**
   * File Upload
   */
  public void init(File[] tasks);

}
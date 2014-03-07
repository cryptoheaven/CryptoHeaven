/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.ops;

import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_co.service.records.MsgLinkRecord;

import java.io.File;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public interface ExportMsgsI {

  public void runDownloadMsgs(MsgLinkRecord[] msgs, MsgLinkRecord[] fromMsgs, File destDir, ServerInterfaceLayer SIL, boolean waitForComplete, boolean openAfterDownload);

}
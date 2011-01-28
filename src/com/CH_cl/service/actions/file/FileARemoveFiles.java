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

package com.CH_cl.service.actions.file;

import com.CH_cl.service.actions.*;

import com.CH_co.trace.Trace;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.file.*;
import com.CH_co.service.records.FileLinkRecord;

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
 * <b>$Revision: 1.7 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FileARemoveFiles extends ClientMessageAction {

  /** Creates new FileARemoveFiles */
  public FileARemoveFiles() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileARemoveFiles.class, "FileARemoveFiles()");
    if (trace != null) trace.exit(FileARemoveFiles.class);
  }

  /** 
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileARemoveFiles.class, "runAction(Connection)");

    FileLinkRecord[] fileLinks = ((File_GetLinks_Rp) getMsgDataSet()).fileLinks;
    getFetchedDataCache().removeFileLinkRecords(fileLinks);
    
    if (trace != null) trace.exit(FileARemoveFiles.class, null);
    return null;
  }
 
}
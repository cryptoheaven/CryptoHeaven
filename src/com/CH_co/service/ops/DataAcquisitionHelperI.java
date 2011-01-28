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

package com.CH_co.service.ops;

import java.sql.*;
import java.util.Date;

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
 * <b>$Revision: 1.0 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public interface DataAcquisitionHelperI {

  public FileDataRecord getOrFetchEncFileLOB(FileLinkRecord link, FileDataRecord data) throws SQLException;
  public FileDataRecord fetchFileDataAttrByID(Long fileLinkId, Long fileId) throws SQLException;
  public FileLinkRecord[] fetchFileLinksByOwner(Long ownerLinkId, Long ownerObjId, short ownerType) throws SQLException;
  public MsgDataRecord fetchMsgDataByID(Long msgLinkId, Long msgId) throws SQLException;
  public MsgLinkRecord fetchMsgLinkByID(Long msgLinkId) throws SQLException;
  public MsgLinkRecord[] fetchMsgLinksByOwner(Long ownerLinkId, Long ownerObjId, short ownerType) throws SQLException;
  public Date getCurrentTime(boolean isLenient) throws SQLException;
  
  /**
   * return personal/short/full email address
   */
  public String[] getDefaultEmailAddressSet(Long userId) throws SQLException;
  
}

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.ops;

import java.sql.*;
import java.util.Date;

import com.CH_co.service.records.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * Class Description:
 *
 * Class Details:
 *
 * <b>$Revision: $</b>
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

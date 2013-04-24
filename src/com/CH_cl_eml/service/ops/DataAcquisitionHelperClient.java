/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_cl_eml.service.ops;

import com.CH_cl.service.ops.*;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_co.service.ops.DataAcquisitionHelperI;
import com.CH_co.service.records.*;

import com.CH_co.trace.Trace;
import java.sql.SQLException;
import java.util.Date;

/**
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.0 $</b>
 * @author  Marcin
 * @version
 */
public class DataAcquisitionHelperClient implements DataAcquisitionHelperI {

  private ServerInterfaceLayer SIL;
  private FetchedDataCache cache;

  public DataAcquisitionHelperClient(ServerInterfaceLayer SIL) {
    this.SIL = SIL;
    this.cache = SIL.getFetchedDataCache();
  }

  public FileDataRecord getOrFetchEncFileLOB(FileLinkRecord link, FileDataRecord data) throws SQLException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DataAcquisitionHelperClient.class, "getOrFetchEncFileLOB(FileLinkRecord link, FileDataRecord data)");
    if (trace != null) trace.args(link, data);
    FileDataRecord rec = FileDataOps.getOrFetchFileLOB(SIL, link, data);
    if (trace != null) trace.exit(DataAcquisitionHelperClient.class, rec);
    return rec;
  }

  public FileDataRecord fetchFileDataAttrByID(Long fileLinkId, Long fileId) throws SQLException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DataAcquisitionHelperClient.class, "fetchFileDataAttrByID(Long fileLinkId, Long fileId)");
    if (trace != null) trace.args(fileLinkId, fileId);
    FileDataRecord rec = FileDataOps.getOrFetchFileDataAttr(SIL, fileLinkId, fileId);
    if (trace != null) trace.exit(DataAcquisitionHelperClient.class, rec);
    return rec;
  }

  public FileLinkRecord[] fetchFileLinksByOwner(Long ownerLinkId, Long ownerObjId, short ownerType) throws SQLException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DataAcquisitionHelperClient.class, "fetchFileLinksByOwner(Long ownerLinkId, Long ownerObjId, short ownerType)");
    if (trace != null) trace.args(ownerLinkId, ownerObjId);
    if (trace != null) trace.args(ownerType);
    FileLinkRecord[] recs = FileLinkOps.getOrFetchFileLinksByOwner(SIL, ownerLinkId, ownerObjId, ownerType);
    if (trace != null) trace.exit(DataAcquisitionHelperClient.class, recs);
    return recs;
  }

  public MsgDataRecord fetchMsgDataByID(Long msgLinkId, Long msgId) throws SQLException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DataAcquisitionHelperClient.class, "fetchMsgDataByID(Long msgLinkId, Long msgId)");
    if (trace != null) trace.args(msgLinkId, msgId);
    MsgDataRecord rec = MsgDataOps.getOrFetchMsgBody(SIL, msgLinkId, msgId);
    if (trace != null) trace.exit(DataAcquisitionHelperClient.class, rec);
    return rec;
  }

  public MsgLinkRecord fetchMsgLinkByID(Long msgLinkId) throws SQLException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DataAcquisitionHelperClient.class, "fetchMsgLinkByID(Long msgLinkId)");
    if (trace != null) trace.args(msgLinkId);
    MsgLinkRecord rec = cache.getMsgLinkRecord(msgLinkId);
    if (trace != null) trace.exit(DataAcquisitionHelperClient.class, rec);
    return rec;
  }

  public MsgLinkRecord[] fetchMsgLinksByOwner(Long ownerLinkId, Long ownerObjId, short ownerType) throws SQLException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DataAcquisitionHelperClient.class, "fetchMsgLinksByOwner(Long ownerLinkId, Long ownerObjId, short ownerType)");
    if (trace != null) trace.args(ownerLinkId, ownerObjId);
    if (trace != null) trace.args(ownerType);
    MsgLinkRecord[] recs = MsgLinkOps.getOrFetchMsgLinksByOwner(SIL, ownerLinkId, ownerObjId, ownerType);
    if (trace != null) trace.exit(DataAcquisitionHelperClient.class, recs);
    return recs;
  }

  public Date getCurrentTime(boolean isLenient) throws SQLException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DataAcquisitionHelperClient.class, "getCurrentTime(boolean isLenient)");
    if (trace != null) trace.args(isLenient);
    Date now = new Date();
    if (trace != null) trace.exit(DataAcquisitionHelperClient.class, now);
    return now;
  }

  /**
   * return personal/short/full email address
   */
  public String[] getDefaultEmailAddressSet(Long userId) throws SQLException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DataAcquisitionHelperClient.class, "getDefaultEmailAddressSet(Long userId)");
    if (trace != null) trace.args(userId);
    String[] set = UserOps.getOrFetchOrMakeDefaultEmail(SIL, userId, true);
    if (trace != null) trace.exit(DataAcquisitionHelperClient.class, set);
    return set;
  }

}
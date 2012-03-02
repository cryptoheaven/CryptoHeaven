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

package com.CH_co.service.records;

import com.CH_co.cryptx.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

/**
 * <b>Copyright</b> &copy; 2001-2012
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
 * <b>$Revision: 1.5 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class AddrHashRecord extends Record {

  public Long addrHashId;
  public Long msgId;
  public BADigestBlock hash;

  public int getIcon() {
    return ImageNums.IMAGE_NONE;
  }

  public Long getId() {
    return addrHashId;
  }

  public static Long[] getMsgIDs(AddrHashRecord[] addrs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AddrHashRecord.class, "getMsgIDs(AddrHashRecord[] addrs)");
    if (trace != null) trace.args(addrs);

    Long[] msgIDs = null;
    if (addrs != null) {
      msgIDs = new Long[addrs.length];
      for (int i=0; i<addrs.length; i++) {
        msgIDs[i] = addrs[i].msgId;
      }
      msgIDs = (Long[]) ArrayUtils.removeDuplicates(msgIDs);
    }

    if (trace != null) trace.exit(AddrHashRecord.class, msgIDs);
    return msgIDs;
  }

  public void merge(Record updated) {
    if (updated instanceof AddrHashRecord) {
      AddrHashRecord record = (AddrHashRecord) updated;
      if (record.addrHashId   != null) addrHashId   = record.addrHashId;
      if (record.msgId        != null) msgId        = record.msgId;
      if (record.hash         != null) hash         = record.hash;
    }
    else 
      super.mergeError(updated);
  }

  public String toString() {
    return "[AddrHashRecord"
      + ": addrHashId="     + addrHashId
      + ", msgId="          + msgId
      + ", hash="           + Misc.objToStr(hash)
      + "]";
  }

  public void setId(Long id) {
    addrHashId = id;
  }

}
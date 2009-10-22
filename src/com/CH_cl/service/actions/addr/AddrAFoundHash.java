/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_cl.service.actions.addr;

import java.util.*;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.*;
import com.CH_co.service.msg.dataSets.addr.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.util.*;

import com.CH_co.trace.Trace;

/**
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class AddrAFoundHash extends ClientMessageAction {

  /** Creates new AddrAFoundHash */
  public AddrAFoundHash() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AddrAFoundHash.class, "AddrAFoundHash()");
    if (trace != null) trace.exit(AddrAFoundHash.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AddrAFoundHash.class, "runAction(Connection)");

    AddrHashRecord[] addrHashRecords = ((Addr_GetHash_Rp) getMsgDataSet()).addrHashRecs;
    FetchedDataCache cache = getFetchedDataCache();
    cache.addAddrHashRecords(addrHashRecords);

//    Vector msgIDsV = null;
//    for (int i=0; addrHashRecords!=null && i<addrHashRecords.length; i++) {
//      if (cache.getMsgDataRecord(addrHashRecords[i].msgId) == null) {
//        if (msgIDsV == null) msgIDsV = new Vector();
//        msgIDsV.addElement(addrHashRecords[i].msgId);
//      }
//    }
//    if (msgIDsV != null) {
//      Long[] msgIDs = (Long[]) ArrayUtils.toArray(msgIDsV, Long.class);
//      FolderPair[] folderPairs = cache.getFolderPairsMyOfType(FolderRecord.ADDRESS_FOLDER, true);
//      Long[] shareIDs = FolderPair.getShareIDs(folderPairs);
//      Obj_IDs_Co request = new Obj_IDs_Co();
//      request.IDs = new Long[][] { shareIDs, msgIDs };
//      // Wait 3 seconds for the answer because this is usually called when previewing a message so it would be nice
//      // if we already had the familiar user as stored in the Address Book instead of some unfamiliar looking email address.
//      getServerInterfaceLayer().submitAndWait(new MessageAction(CommandCodes.ADDR_Q_FIND_ADDRS, request), 3000);
//    }

    if (trace != null) trace.exit(AddrAFoundHash.class, null);
    return null;
  }

}
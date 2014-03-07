/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.msg.dataSets;

import java.io.IOException;
import java.sql.Timestamp;

import com.CH_co.monitor.ProgMonitorI;

import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.records.OrganizationRecord;
import com.CH_co.service.msg.ProtocolMsgDataSet;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 * 
 * Get My Organization Reply
 * @author  Marcin Kurzawa
 */
public class Org_GetOrg_Rp extends ProtocolMsgDataSet {
  // <userId> <sponsorId> <lvl0Total> <lvl1Total> <lvl2Total> <lvl3Total> <lvl4Total> <lvl5Total> <lvl6Total> <lvl7Total> <lvl8Total> <lvl9Total> <dataUpdated>
  public OrganizationRecord orgRecord;
  
  /** Creates new Org_GetOrg_Rp */
  public Org_GetOrg_Rp() {
  }

  
  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    dataOut.writeLongObj(orgRecord.userId);
    dataOut.writeLongObj(orgRecord.sponsorId);
    for (int i=0; i<OrganizationRecord.LEVELS; i++) {
      dataOut.writeInteger(orgRecord.lvlTotals[i]);
    }
    dataOut.writeTimestamp(orgRecord.dateUpdated);

  } // end writeToStream()
  
  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    orgRecord = new OrganizationRecord();
    orgRecord.userId = dataIn.readLongObj();
    orgRecord.sponsorId = dataIn.readLongObj();
    for (int i=0; i<OrganizationRecord.LEVELS; i++) {
      orgRecord.lvlTotals[i] = dataIn.readInteger();
    }
    orgRecord.dateUpdated = dataIn.readTimestamp();
  } // end initFromStream()
  
}

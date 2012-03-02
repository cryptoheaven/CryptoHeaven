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

package com.CH_co.service.msg.dataSets.usr;

import java.io.IOException;

import com.CH_co.trace.Trace;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.io.DataInputStream2;
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.records.*;

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
public class Usr_Settings_Co extends ProtocolMsgDataSet {

  // <userId> <pubKeyId> <encSymKey> <encText>
  public UserSettingsRecord userSettingsRecord;

  /** Creates new Usr_Settings_Co */
  public Usr_Settings_Co() {
  }

  /** Creates new Usr_Settings_Co */
  public Usr_Settings_Co(UserSettingsRecord userSettingsRecord) {
    this.userSettingsRecord = userSettingsRecord;
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_Settings_Co.class, "writeToStream(DataOutputStream2, ProgMonitor, clientBuild, serverBuild)");

    // write UserSettingsRecord
    if (userSettingsRecord == null)
      dataOut.write(0);
    else {
      dataOut.write(1);
      dataOut.writeLongObj(userSettingsRecord.userId);
      dataOut.writeLongObj(userSettingsRecord.pubKeyId);
      dataOut.writeBytes(userSettingsRecord.getEncSymKey());
      dataOut.writeBytes(userSettingsRecord.getEncText());
    }

    if (trace != null) trace.exit(Usr_Settings_Co.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_Settings_Co.class, "initFromStream(DataInputStream2, ProgMonitor, clientBuild, serverBuild)");

    // read UserSettingsRecord
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      userSettingsRecord = null;
    else {
      userSettingsRecord = new UserSettingsRecord();
      userSettingsRecord.userId = dataIn.readLongObj();
      userSettingsRecord.pubKeyId = dataIn.readLongObj();
      userSettingsRecord.setEncSymKey(dataIn.readAsyCipherBlock());
      userSettingsRecord.setEncText(dataIn.readSymCipherBulk());
    }

    if (trace != null) trace.exit(Usr_Settings_Co.class);
  } // end initFromStream()


  public String toString() {
    return "[Usr_Settings_Co"
    + ": userSettingsRecord=" + userSettingsRecord
    + "]";
  }

}
/*
 * Copyright 2001-2010 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_co.service.msg.dataSets.usr;

import java.io.IOException;

import com.CH_co.trace.Trace;
import com.CH_co.monitor.ProgMonitor;
import com.CH_co.io.DataInputStream2;
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.records.*;
import com.CH_co.util.Misc;

/**
 * <b>Copyright</b> &copy; 2001-2010
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
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class Usr_AutoResponder_Co extends ProtocolMsgDataSet {

  // <numOfResponders> { <userId> <dateStart> <dateEnd> <compText> }*
  public AutoResponderRecord[] autoResponderRecords;

  /** Creates new Usr_AutoResponder_Co */
  public Usr_AutoResponder_Co() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_AutoResponder_Co.class, "Usr_AutoResponder_Co()");
    if (trace != null) trace.exit(Usr_AutoResponder_Co.class);
  }

  /** Creates new Usr_AutoResponder_Co */
  public Usr_AutoResponder_Co(AutoResponderRecord autoResponderRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_AutoResponder_Co.class, "Usr_AutoResponder_Co(AutoResponderRecord autoResponderRecord)");
    if (autoResponderRecord != null)
      this.autoResponderRecords = new AutoResponderRecord[] { autoResponderRecord };
    else
      this.autoResponderRecords = null;
    if (trace != null) trace.exit(Usr_AutoResponder_Co.class);
  }

  /** Creates new Usr_AutoResponder_Co */
  public Usr_AutoResponder_Co(AutoResponderRecord[] autoResponderRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_AutoResponder_Co.class, "Usr_AutoResponder_Co(AutoResponderRecord[] autoResponderRecords)");
    this.autoResponderRecords = autoResponderRecords;
    if (trace != null) trace.exit(Usr_AutoResponder_Co.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_AutoResponder_Co.class, "writeToStream(DataOutputStream2, ProgMonitor, clientBuild, serverBuild)");

    // write AutoResponderRecord
    if (autoResponderRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);

      dataOut.writeShort(autoResponderRecords.length);

      for (int i=0; i<autoResponderRecords.length; i++) {
        AutoResponderRecord autoResponderRecord = autoResponderRecords[i];
        dataOut.writeLongObj(autoResponderRecord.userId);
        dataOut.writeDate(autoResponderRecord.dateStart);
        dataOut.writeDate(autoResponderRecord.dateEnd);
        dataOut.writeBytes(autoResponderRecord.getCompText());
      }
    }

    if (trace != null) trace.exit(Usr_AutoResponder_Co.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Usr_AutoResponder_Co.class, "initFromStream(DataInputStream2, ProgMonitor, clientBuild, serverBuild)");

    // read AutoResponderRecord
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      autoResponderRecords = null;
    else {

      autoResponderRecords = new AutoResponderRecord[dataIn.readShort()];

      for (int i=0; i<autoResponderRecords.length; i++) {
        AutoResponderRecord autoResponderRecord = new AutoResponderRecord();
        autoResponderRecord.userId = dataIn.readLongObj();
        autoResponderRecord.dateStart = dataIn.readDate();
        autoResponderRecord.dateEnd = dataIn.readDate();
        autoResponderRecord.setCompText(dataIn.readBytes());
        autoResponderRecords[i] = autoResponderRecord;
      }
    }

    if (trace != null) trace.exit(Usr_AutoResponder_Co.class);
  } // end initFromStream()


  public String toString() {
    return "[Usr_AutoResponder_Co"
    + ": autoResponderRecords=" + Misc.objToStr(autoResponderRecords)
    + "]";
  }

}
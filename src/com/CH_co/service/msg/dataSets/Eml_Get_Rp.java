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

package com.CH_co.service.msg.dataSets;

import java.io.IOException;

import com.CH_co.io.DataInputStream2;
import com.CH_co.io.DataOutputStream2;
import com.CH_co.monitor.ProgMonitor;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.records.EmailRecord;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

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
 * <b>$Revision: 1.9 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class Eml_Get_Rp extends ProtocolMsgDataSet {

  // <numOfEmls> { <emlId> <userId> <creatorId> <emailAddr> <dateCreated> <dateUpdated> }+
  public EmailRecord[] emailRecords;

  /** Creates new Eml_Get_Rp */
  public Eml_Get_Rp() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Eml_Get_Rp.class, "Eml_Get_Rp()");
    if (trace != null) trace.exit(Eml_Get_Rp.class);
  }
  /** Creates new Eml_Get_Rp */
  public Eml_Get_Rp(EmailRecord[] emailRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Eml_Get_Rp.class, "Eml_Get_Rp(EmailRecord[] emailRecords)");
    this.emailRecords = emailRecords;
    if (trace != null) trace.exit(Eml_Get_Rp.class);
  }

  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Eml_Get_Rp.class, "writeToStream(DataOutputStream2, ProgMonitor)");
    // write indicator
    if (emailRecords == null)
      dataOut.write(0);
    else {
      dataOut.write(1);

      dataOut.writeShort(emailRecords.length);

      for (int i=0; i<emailRecords.length; i++) {
        dataOut.writeLongObj(emailRecords[i].emlId);
        dataOut.writeLongObj(emailRecords[i].userId);
        dataOut.writeLongObj(emailRecords[i].creatorId);
        dataOut.writeString(emailRecords[i].emailAddr);
        if (clientBuild >= 212 && serverBuild >= 212)
          dataOut.writeString(emailRecords[i].personal);
        if (clientBuild >= 276 && serverBuild >= 276)
          dataOut.writeCharByte(emailRecords[i].isHosted);
        dataOut.writeTimestamp(emailRecords[i].dateCreated);
        dataOut.writeTimestamp(emailRecords[i].dateUpdated);
      }
    }
    if (trace != null) trace.exit(Eml_Get_Rp.class);
  } // end writeToStream()

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitor progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Eml_Get_Rp.class, "initFromStream(DataInputStream2, ProgMonitor)");
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      emailRecords = new EmailRecord[0];
    else {

      emailRecords = new EmailRecord[dataIn.readShort()];

      for (int i=0; i<emailRecords.length; i++) {
        emailRecords[i] = new EmailRecord();
        emailRecords[i].emlId = dataIn.readLongObj();
        emailRecords[i].userId = dataIn.readLongObj();
        emailRecords[i].creatorId = dataIn.readLongObj();
        emailRecords[i].emailAddr = dataIn.readString();
        if (clientBuild >= 212 && serverBuild >= 212)
          emailRecords[i].personal = dataIn.readString();
        if (clientBuild >= 276 && serverBuild >= 276)
          emailRecords[i].isHosted = dataIn.readCharByte();
        emailRecords[i].dateCreated = dataIn.readTimestamp();
        emailRecords[i].dateUpdated = dataIn.readTimestamp();
      }
    }
    if (trace != null) trace.exit(Eml_Get_Rp.class);
  } // end initFromStream()


  public String toString() {
    return "[Eml_Get_Rp"
    + ": emailRecords=" + Misc.objToStr(emailRecords)
    + "]";
  }

}
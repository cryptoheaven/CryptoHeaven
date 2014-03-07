/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.records;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import com.CH_co.nanoxml.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.4 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class AutoResponderRecord extends Record {

  public Long userId;
  public Date dateStart;
  public Date dateEnd;
  private byte[] compText;      // compressed XML settings text
  public Timestamp t1;
  public byte[] e1;
  public Timestamp t2;
  public byte[] e2;
  public Timestamp t3;
  public byte[] e3;

  /** unwrapped data */
  private XMLElement xmlText;

  /** cached data */
  public String subject;
  public String message;


  /** Creates new AutoResponderRecord */
  public AutoResponderRecord() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoResponderRecord.class, "AutoResponderRecord()");
    if (trace != null) trace.exit(AutoResponderRecord.class);
  }

  public int getIcon() {
    return ImageNums.IMAGE_NONE;
  }

  public Long getId() {
    return userId;
  }

  public void setCompText(byte[] compText) {
    this.compText = compText;
  }
  public void setXmlText(XMLElement xmlText) {
    this.xmlText = xmlText;
  }

  public byte[] getCompText() {
    return compText;
  }
  public XMLElement getXmlText() {
    return xmlText;
  }

  /**
   * Seals the <code> xmlText </code> to <code> compText </code> 
   * using the sealant object which is the compressor.
   */
  public void seal() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoResponderRecord.class, "seal()");
    try {
      byte[] tempText = null;
      if (xmlText != null) {
        tempText = Misc.compress(xmlText.toString());
      }
      compText = tempText;
      super.seal();
    } catch (Throwable t) {
      if (trace != null) trace.exception(AutoResponderRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(AutoResponderRecord.class);
  }


  /**
   * Unseals the <code> compText </code> into <code> xmlText </code> 
   * using the unSealant object which is the compressor.
   */
  public void unSeal() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoResponderRecord.class, "unSeal()");
    try {
      XMLElement tempXML = null;
      if (compText != null) {
        tempXML = new XMLElement();
        tempXML.parseString(Misc.decompressStr(compText));
      }

      // set values when all done
      xmlText = tempXML;
      super.unSeal();
      parseSettings();
    } catch (Throwable t) {
      if (trace != null) trace.exception(AutoResponderRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }
    if (trace != null) trace.exit(AutoResponderRecord.class);
  }

  private void parseSettings() {
    // reset old settings
    subject = null;
    message = null;
    // parse new settings
    // initialize responder message
    if (xmlText != null && xmlText.getNameSafe().equalsIgnoreCase("responder")) {
      Vector elementsV = xmlText.getChildren();
      if (elementsV != null) {
        for (int i=0; i<elementsV.size(); i++) {
          XMLElement element = (XMLElement) elementsV.elementAt(i);
          String elementName = element.getNameSafe();
          String elementValue = element.getContent();
          if (elementName.equalsIgnoreCase("subject")) {
            subject = elementValue;
          } else if (elementName.equalsIgnoreCase("message")) {
            message = elementValue;
          }
        }
      }
    }
  }

  /**
   * @return true if current time "now" is during the specified absence.
   */
  public boolean isBetweenAbsenceTimes(Timestamp now) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoResponderRecord.class, "isBetweenAbsenceTimes(Timestamp now)");
    if (trace != null) trace.args(now);
    boolean rc = dateStart != null && dateEnd != null && dateStart.getTime() < now.getTime() && dateEnd.getTime() > now.getTime();
    if (trace != null) trace.exit(AutoResponderRecord.class, rc);
    return rc;
  }

  /**
   * @return true if current time "now" is still before the scheduled absence.
   */
  public boolean isAheadOfAbsenceTime(Timestamp now) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AutoResponderRecord.class, "isAheadOfAbsenceTime(Timestamp now)");
    if (trace != null) trace.args(now);
    boolean rc = dateStart != null && dateEnd != null && dateStart.getTime() > now.getTime() && dateEnd.getTime() > now.getTime();
    if (trace != null) trace.exit(AutoResponderRecord.class, rc);
    return rc;
  }

  public void merge(Record updated) {
    if (updated instanceof AutoResponderRecord) {
      AutoResponderRecord record = (AutoResponderRecord) updated;

      if (record.userId           != null) userId           = record.userId;
      if (record.dateStart        != null) dateStart        = record.dateStart;
      if (record.dateEnd          != null) dateEnd          = record.dateEnd;
      if (record.compText         != null) compText         = record.compText;
      if (record.t1               != null) t1               = record.t1;
      if (record.e1               != null) e1               = record.e1;
      if (record.t2               != null) t2               = record.t2;
      if (record.e2               != null) e2               = record.e2;
      if (record.t3               != null) t3               = record.t3;
      if (record.e3               != null) e3               = record.e3;

      // un-sealed data
      if (record.xmlText          != null) xmlText          = record.xmlText;

      // cached data
      if (record.subject          != null) subject          = record.subject;
      if (record.message          != null) message          = record.message;
    }
    else
      super.mergeError(updated);
  }

  public void setId(Long id) {
    userId = id;
  }

  public String toString() {
    return "[AutoResponderRecord"
      + ": userId="         + userId
      + ", dateStart="      + dateStart
      + ", dateEnd="        + dateEnd
      + ", compText="       + Misc.objToStr(compText)
      + ", t1="             + t1
      + ", e1="             + Misc.objToStr(e1)
      + ", t2="             + t2
      + ", e2="             + Misc.objToStr(e2)
      + ", t3="             + t3
      + ", e3="             + Misc.objToStr(e3)
      + ", un-sealed data >> "
      + ", xmlText="        + xmlText
      + ", cached data >> "
      + ", subject="        + subject
      + ", message="        + message
      + "]";
  }

}
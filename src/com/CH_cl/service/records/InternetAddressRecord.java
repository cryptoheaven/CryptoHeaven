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

package com.CH_cl.service.records;

import java.util.*;

import com.CH_co.service.records.Record;
import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.11 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
abstract public class InternetAddressRecord extends Record {

  // Uniqueness of addresses is kept by this hashtable.
  // Email records of the same address will automatically get assigned the same id.
  private static final HashMap ids = new HashMap();
  private static long lastId = 0;

  public Long id;
  public String address;


  /** Creates new InternetAddressRecord */
  public InternetAddressRecord() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(InternetAddressRecord.class, "InternetAddressRecord()");
    if (trace != null) trace.exit(InternetAddressRecord.class);
  }

  /** Creates new InternetAddressRecord and auto-assign a unique id per unique address. */
  public InternetAddressRecord(String addr) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(InternetAddressRecord.class, "InternetAddressRecord(String addr)");
    if (trace != null) trace.args(addr);
    this.address = addr != null ? addr : "";
    synchronized (ids) {
      Long oldId = (Long) ids.get(address);
      if (oldId == null) {
        lastId ++;
        oldId = new Long(lastId);
        ids.put(address, oldId);
      }
      this.id = oldId;
    }
    if (trace != null) trace.exit(InternetAddressRecord.class);
  }

  public void merge(Record updated) {
    if (updated instanceof InternetAddressRecord) {
      InternetAddressRecord record = (InternetAddressRecord) updated;
      if (record.id      != null) id      = record.id;
      if (record.address != null) address = record.address;
    }
    else
      super.mergeError(updated);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public static class AddressComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      if (o1 instanceof InternetAddressRecord && o2 instanceof InternetAddressRecord)
        return ((InternetAddressRecord) o1).address.compareToIgnoreCase(((InternetAddressRecord) o2).address);
      else if (o1 == null)
        return -1;
      else
        return 1;
    }
  }

}
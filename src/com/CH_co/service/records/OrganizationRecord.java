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

package com.CH_co.service.records;

import javax.swing.Icon;
import java.sql.Timestamp;

import com.CH_co.trace.Trace;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>  
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class OrganizationRecord extends Record {

  /** Cannot change this, it is tied to the database and many other functions. */
  public static final int LEVELS = 10;

  public Long userId;
  public Long sponsorId;
  public Integer[] lvlTotals = new Integer[LEVELS];
  public Timestamp dateUpdated;

  /** Creates new OrganizationRecord */
  public OrganizationRecord() {
  }

  public Long getId() {
    return userId;
  }

  public Icon getIcon() {
    return null;
  }

  /**
   * Seals the <code> </code> <code> </code> 
   * using the sealant object which is 
   */
  public void seal() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(OrganizationRecord.class, "seal()");
    if (trace != null) trace.exit(OrganizationRecord.class);
  }

  /**
   * Unseals the <code> </code> into <code> </code> 
   * using the unSealant object which is 
   */
  public void unSeal() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(OrganizationRecord.class, "unSeal()");
    if (trace != null) trace.exit(OrganizationRecord.class);
  }


  public void merge(Record updated) {
    if (updated instanceof OrganizationRecord) {
      OrganizationRecord record = (OrganizationRecord) updated;
      if (record.userId            != null) userId           = record.userId;
      if (record.sponsorId         != null) sponsorId        = record.sponsorId;

      for (int i=0; i<LEVELS; i++) 
        if (record.lvlTotals[i]    != null) lvlTotals[i]     = record.lvlTotals[i];

      if (record.dateUpdated       != null) dateUpdated      = record.dateUpdated;
    }
    else
      super.mergeError(updated);
  }

  public String toString() {
    StringBuffer levelsBuffer = new StringBuffer("(" + lvlTotals[0] + ",");
    for (int i=1; i<lvlTotals.length; i++) {
      levelsBuffer.append("," + lvlTotals[i]);
    }
    levelsBuffer.append(")");

    return "[OrganizationRecord"
      + ": userId="           + userId
      + ", sponsorId="        + sponsorId
      + ", lvlTotals[]="      + levelsBuffer.toString()
      + ", dateUpdated="      + dateUpdated
      + "]";
  }
  
  public void setId(Long id) {
    userId = id;
  }
  
}
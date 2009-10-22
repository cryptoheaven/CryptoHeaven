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

package com.CH_co.service.records;

import java.sql.Timestamp;
import javax.swing.Icon;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

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
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class AutoUpdateRecord extends Record {

  public Long id;
  public String hashStr;
  public String hashAlg;
  public Short build;
  public Short applyFrom;
  public Short applyTo;
  public String oldFile;
  public String newFile;
  public String locFile;
  public Integer size;
  public Timestamp dateExpired;

  public Icon getIcon() {
    return null;
  }

  public Long getId() {
    return id;
  }

  public void merge(Record updated) {
    if (updated instanceof AutoUpdateRecord) {
      AutoUpdateRecord record = (AutoUpdateRecord) updated;
      if (record.id           != null) id           = record.id;
      if (record.hashStr      != null) hashStr      = record.hashStr;
      if (record.hashAlg      != null) hashAlg      = record.hashAlg;
      if (record.build        != null) build        = record.build;
      if (record.applyFrom    != null) applyFrom    = record.applyFrom;
      if (record.applyTo      != null) applyTo      = record.applyTo;
      if (record.oldFile      != null) oldFile      = record.oldFile;
      if (record.newFile      != null) newFile      = record.newFile;
      if (record.locFile      != null) locFile      = record.locFile;
      if (record.size         != null) size         = record.size;
      if (record.dateExpired  != null) dateExpired  = record.dateExpired;
    }
    else 
      super.mergeError(updated);
  }

  public String toString() {
    return "[AutoUpdateRecord"
      + ": id="           + id
      + ", hashStr="      + Misc.objToStr(hashStr)
      + ", hashAlg="      + hashAlg
      + ", build="        + build
      + ", applyFrom="    + applyFrom
      + ", applyTo="      + applyTo
      + ", oldFile="      + oldFile
      + ", newFile="      + newFile
      + ", locFile="      + locFile
      + ", size="         + size
      + ", dateExpired="  + dateExpired
      + "]";
  }

  public void setId(Long id) {
    this.id = id;
  }

}
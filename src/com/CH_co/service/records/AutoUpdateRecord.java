/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.records;

import java.sql.Timestamp;

import com.CH_co.util.*;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.4 $</b>
 *
 * @author  Marcin Kurzawa
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

  public int getIcon() {
    return ImageNums.IMAGE_NONE;
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
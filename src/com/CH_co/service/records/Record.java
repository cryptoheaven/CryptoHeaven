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

import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
abstract public class Record extends Object implements Cloneable, Comparable {

  // Types of records
  public static final short RECORD_TYPE_FOLDER = 1;
  public static final short RECORD_TYPE_SHARE = 2;
  public static final short RECORD_TYPE_MESSAGE = 3;
  public static final short RECORD_TYPE_KEY = 4;
  public static final short RECORD_TYPE_CONTACT = 5;
  public static final short RECORD_TYPE_FILE_LINK = 6;
  public static final short RECORD_TYPE_USER = 7;
  public static final short RECORD_TYPE_MSG_LINK = 8;
  public static final short RECORD_TYPE_GROUP = 9;
  public static final short RECORD_TYPE_INVEML = 10;

  private boolean sealed;
  private boolean unSealed;

  protected void seal() { 
    sealed = true;    
  }
  protected void unSeal() { 
    unSealed = true; 
  }
  protected void setUnSealed (boolean isUnSealed) {
    unSealed = isUnSealed;
  }

  public boolean isSealed()   { return sealed;   }
  public boolean isUnSealed() { return unSealed; }
  
  public abstract Long getId();
  public abstract void setId(Long id);
  public Object getIdObject() {
    return getId();
  }
  public void setIdObject(Object idObj) {
    setId((Long) idObj);
  }

  /**
   * @return the default icon to represent this Record type.
   */
  public abstract int getIcon();

  public String toStringLongFormat() {
    return toString();
  }

  /** 
   * Default comparison is made by object ID but this is rather user unfriendly comparison.
   * Subclasses should overwrite with more meaningful sorting comparisons, perhaps by name.
   */
  public int compareTo(Object record) { 
    int comp = getClass().getName().compareTo(record.getClass().getName());
    if (comp == 0) {
      // group the objects by its type
      comp = getId().compareTo( ((Record) record).getId() );
    }
    return comp;
  }

  public void mergeError(Record updated)  { throw new IllegalArgumentException("Objects " + Misc.getClassNameWithoutPackage(getClass()) + " and " + Misc.getClassNameWithoutPackage(updated.getClass()) +" are not compatible!");  }
  public abstract void merge(Record updated);

  /** Overwrite equals method to compare by record's ID. */
  public boolean equals(Object o) {
    boolean rc = false;
    if (o instanceof Record) {
      rc = getId().equals( ((Record)o).getId() );
      // Additionally check if records are of same type, 
      // for example ContactRecord != FolderPair even if they have the same id
      if (rc && !getClass().equals(o.getClass())) {
        rc = false;
      }
    } else {
      rc = super.equals(o);
    }
    return rc;
  }
  public int hashCode() {
    Object id = getIdObject();
    if (id != null) 
      return id.hashCode();
    else 
      return super.hashCode();
  }

  /** Records are cloneable. */
  public Object clone() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Record.class, "clone()");
    if (trace != null) trace.info(10, this);
    Record record = null;
    try {
      record = (Record) super.clone();
    } catch (CloneNotSupportedException e) {
      if (trace != null) trace.exception(Record.class, 50, e);
    }
    if (trace != null) trace.info(90, record);
    if (trace != null) trace.exit(Record.class, "record");
    return record;
  }

}
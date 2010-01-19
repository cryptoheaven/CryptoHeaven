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

import com.CH_co.util.ImageNums;
import com.CH_co.util.Images;
import javax.swing.Icon;
import java.sql.Timestamp;

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
 * <b>$Revision: 1.7 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class InvEmlRecord extends Record {

  public Long id;
  public String emailAddr;
  public Long sentByUID;
  public String fromName;
  public String fromEmail;
  public String msg;
  public Timestamp dateSent;
  public Boolean removed;

  /** Creates new InvEmlRecord */
  public InvEmlRecord() {
  }

  public Long getId() {
    return id;
  }

  /**
   * @return the default icon to represent this Record type.
   */
  public Icon getIcon() {
    return Images.get(ImageNums.EMAIL_SYMBOL_SMALL);
  }

  public void merge(Record updated) {
    if (updated instanceof InvEmlRecord) {
      InvEmlRecord record = (InvEmlRecord) updated;
      if (record.id         != null) id         = record.id;
      if (record.emailAddr  != null) emailAddr  = record.emailAddr;
      if (record.sentByUID  != null) sentByUID  = record.sentByUID;
      if (record.fromName   != null) fromName   = record.fromName;
      if (record.fromEmail  != null) fromEmail  = record.fromEmail;
      if (record.msg        != null) msg        = record.msg;
      if (record.dateSent   != null) dateSent   = record.dateSent;
      if (record.removed    != null) removed    = record.removed;
    }
    else
      super.mergeError(updated);
  }

  public String toString() {
    return "[InvEmlRecord"
      + ": id="         + id
      + ", emailAddr="  + emailAddr
      + ", sentByUID="  + sentByUID
      + ", fromName="   + fromName
      + ", fromEmail="  + fromEmail
      + ", msg="        + msg
      + ", dateSent="   + dateSent
      + ", removed="    + removed
      + "]";
  }

  public void setId(Long id) {
    this.id = id;
  }

}
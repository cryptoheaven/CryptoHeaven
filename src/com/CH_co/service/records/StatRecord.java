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

import javax.swing.Icon;
import java.sql.Timestamp;
import java.util.Vector;

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
 * <b>$Revision: 1.17 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class StatRecord extends Record { // implicit no-argument constructor

  public static final Short FLAG_NEW = new Short((short) 1);
  public static final Short FLAG_OLD = new Short((short) 2);
  public static final short FLAG_BCC = 4; // server only flag, client never sees it

  public static final short STATUS__UNSEEN_UNDELIVERED = 3;
  public static final short STATUS__UNSEEN_DELIVERED = 4;
  public static final short STATUS__SEEN_UNDELIVERED = 5;
  public static final short STATUS__SEEN_DELIVERED = 6;

  public static final byte STAT_TYPE_FILE = 'F';
  public static final byte STAT_TYPE_FOLDER = 'R';
  public static final byte STAT_TYPE_MESSAGE = 'M';
  public static final byte STAT_TYPE_UNKNOWN = 'U';
  public static final byte[] STAT_TYPES = new byte[] { STAT_TYPE_FILE, STAT_TYPE_FOLDER, STAT_TYPE_MESSAGE, STAT_TYPE_UNKNOWN };

  public Long statId;
  public Long ownerUserId;
  public Byte objType;
  public Long objId;
  public Long objLinkId;
  public Short mark;
  public Timestamp firstSeen;
  public Timestamp firstDelivered;

  public Long getId() { 
    return statId; 
  }

  public Icon getIcon() {
    return getIconForFlag(getFlag());
  }

  public static Icon getIconForFlag(Short flag) {
    Icon icon = null;
    switch (flag.intValue()) {
      case STATUS__UNSEEN_UNDELIVERED:
        icon = Images.get(ImageNums.FLAG_RED_SMALL);
        break;
      case STATUS__SEEN_DELIVERED:
        icon = null;
        break;
      case STATUS__UNSEEN_DELIVERED:
        icon = Images.get(ImageNums.FLAG_GREEN_SMALL);
        break;
      case STATUS__SEEN_UNDELIVERED:
        icon = Images.get(ImageNums.FLAG_YELLOW_SMALL);
        break;
    }
    return icon;
  }

  public Short getFlag() {
    return getFlag(false);
  }
  public Short getFlag(boolean forceIfSeenThenDelivered) {
    Short value = null;
    if ((mark.shortValue() & FLAG_NEW.shortValue()) != 0 && firstDelivered == null)
      value = new Short(STATUS__UNSEEN_UNDELIVERED);
    else if ((mark.shortValue() & FLAG_OLD.shortValue()) != 0 && firstDelivered != null)
      value = new Short(STATUS__SEEN_DELIVERED);
    else if ((mark.shortValue() & FLAG_NEW.shortValue()) != 0 && firstDelivered != null)
      value = new Short(STATUS__UNSEEN_DELIVERED);
    else if ((mark.shortValue() & FLAG_OLD.shortValue()) != 0 && firstDelivered == null) {
      if (!forceIfSeenThenDelivered)
        value = new Short(STATUS__SEEN_UNDELIVERED);
      else
        value = new Short(STATUS__SEEN_DELIVERED);
    }
    return value;
  }

  public static String getInfo(Short flag) {
    String info = null;
    switch (flag.intValue()) {
      case STATUS__UNSEEN_UNDELIVERED:
        info = "Unseen/Undelivered";
        break;
      case STATUS__SEEN_UNDELIVERED:
        info = "Seen/Undelivered";
        break;
      case STATUS__UNSEEN_DELIVERED:
        info = "Unseen/Delivered";
        break;
      case STATUS__SEEN_DELIVERED:
        info = "Seen/Delivered";
        break;
    }
    return info;
  }

  public static Long[] getLinkIDs(StatRecord[] statRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StatRecord.class, "getLinkIDs(StatRecord[] statRecords)");
    if (trace != null) trace.args(statRecords);

    Vector linkIDsV = new Vector();
    if (statRecords != null) {
      for (int i=0; i<statRecords.length; i++) {
        if (linkIDsV.contains(statRecords[i].objLinkId) == false)
          linkIDsV.addElement(statRecords[i].objLinkId);
      }
    }
    Long[] linkIDs = (Long[]) ArrayUtils.toArray(linkIDsV, Long.class);

    if (trace != null) trace.exit(StatRecord.class, linkIDs);
    return linkIDs;
  }

  public static Long[] getUserIDs(StatRecord[] statRecords) {
    Vector userIDsV = new Vector();
    if (statRecords != null) {
      for (int i=0; i<statRecords.length; i++) {
        if (userIDsV.contains(statRecords[i].ownerUserId) == false)
          userIDsV.addElement(statRecords[i].ownerUserId);
      }
    }
    Long[] userIDs = (Long[]) ArrayUtils.toArray(userIDsV, Long.class);
    return userIDs;
  }

  public static StatRecord[] gatherStatsOfType(StatRecord[] statRecords, byte objType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StatRecord.class, "gatherStatsOfType(StatRecord[] statRecords, byte objType)");
    if (trace != null) trace.args(statRecords);
    if (trace != null) trace.args(objType);
    Vector statsV = new Vector();
    if (statRecords != null) {
      for (int i=0; i<statRecords.length; i++) {
        if (statRecords[i].objType.byteValue() == objType)
          statsV.addElement(statRecords[i]);
      }
    }
    StatRecord[] stats = (StatRecord[]) ArrayUtils.toArray(statsV, StatRecord.class);
    if (trace != null) trace.exit(StatRecord.class, stats);
    return stats;
  }

  public boolean isFlagGreen() {
    return (mark.shortValue() & FLAG_NEW.shortValue()) != 0 && firstDelivered != null;
  }

  public boolean isFlagRed() {
    return (mark.shortValue() & FLAG_NEW.shortValue()) != 0 && firstDelivered == null;
  }
  public boolean isFlagNew() {
    return (mark.shortValue() & FLAG_NEW.shortValue()) != 0;
  }

  public void merge(Record updated) {
    if (updated instanceof StatRecord) {
      StatRecord record = (StatRecord) updated;
      if (record.statId         != null) statId         = record.statId;
      if (record.ownerUserId    != null) ownerUserId    = record.ownerUserId;
      if (record.objType        != null) objType        = record.objType;
      if (record.objId          != null) objId          = record.objId;
      if (record.objLinkId      != null) objLinkId      = record.objLinkId;
      if (record.mark           != null) mark           = record.mark;
      if (record.firstSeen      != null) firstSeen      = record.firstSeen;
      if (record.firstDelivered != null) firstDelivered = record.firstDelivered;
      /* // didn't fix the problem, the problem has to do with fetching entire folder (with stats) when chat frame first shows up...
      // Exception to the rule, stats are always fetched in whole so lets merge the delivery
      // date no matter what.  It should fix the lack of red flag for the first chat message.
      firstDelivered = record.firstDelivered;
       */
    }
    else 
      super.mergeError(updated);
  }


  public String toString() {
    return "[StatRecord"
      + ": statId="         + statId
      + ", ownerUserId="    + ownerUserId
      + ", objType="        + "'" + (objType != null ? (char)objType.byteValue() : '-') + "'"
      + ", objId="          + objId
      + ", objLinkId="      + objLinkId
      + ", mark="           + mark
      + ", firstSeen="      + firstSeen
      + ", firstDelivered=" + firstDelivered
      + "]";
  }

  public void setId(Long id) {
    statId = id;
  }

}
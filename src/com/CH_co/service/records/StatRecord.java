/*
* Copyright 2001-2013 by CryptoHeaven Corp.,
* Mississauga, Ontario, Canada.
* All rights reserved.
*
* This software is the confidential and proprietary information
* of CryptoHeaven Corp. ("Confidential Information").  You
* shall not disclose such Confidential Information and shall use
* it only in accordance with the terms of the license agreement
* you entered into with CryptoHeaven Corp.
*/

package com.CH_co.service.records;

import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import com.CH_co.util.ImageNums;
import java.sql.Timestamp;
import java.util.ArrayList;

/** 
* <b>Copyright</b> &copy; 2001-2013
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
* <b>$Revision: 1.17 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class StatRecord extends Record { // implicit no-argument constructor

  public static final Short FLAG_NEW = new Short((short) 1);
  public static final Short FLAG_OLD = new Short((short) 2);
  public static final short FLAG_BCC = 4; // server only flag, client never sees it
  public static final Short FLAG_MARKED_NEW = new Short((short) 8);

  public static final short STATUS__UNSEEN_UNDELIVERED = 3;
  public static final short STATUS__UNSEEN_DELIVERED = 4;
  public static final short STATUS__SEEN_UNDELIVERED = 5;
  public static final short STATUS__SEEN_DELIVERED = 6;

  public static final byte STAT_TYPE_FILE = 'F';
  public static final byte STAT_TYPE_FOLDER = 'R';
  public static final byte STAT_TYPE_MESSAGE = 'M';
  public static final byte STAT_TYPE_UNKNOWN = 'U';
  public static final byte[] STAT_TYPES = new byte[] { STAT_TYPE_FILE, STAT_TYPE_FOLDER, STAT_TYPE_MESSAGE, STAT_TYPE_UNKNOWN };

  public static final int STAT_TYPE_INDEX_FILE = 0;
  public static final int STAT_TYPE_INDEX_FOLDER = 1;
  public static final int STAT_TYPE_INDEX_MESSAGE = 2;

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

  public int getIcon() {
    return getIconForFlag(getFlag());
  }

  public static int getIconForFlag(Short flag) {
    int icon = ImageNums.IMAGE_NONE;
    switch (flag.intValue()) {
      case STATUS__UNSEEN_UNDELIVERED:
        icon = ImageNums.FLAG_RED_SMALL;
        break;
      case STATUS__SEEN_DELIVERED:
        icon = ImageNums.IMAGE_NONE;
        break;
      case STATUS__UNSEEN_DELIVERED:
        icon = ImageNums.FLAG_RED_SMALL;
        break;
      // Yellow flags are eliminated, green flags replaced by red (red now has double meaning to simplify the interface) -- use Trace function to get such detailed information
//      case STATUS__SEEN_UNDELIVERED:
//        icon = ImageNums.FLAG_YELLOW_SMALL;
//        break;
    }
    return icon;
  }

  public Short getFlag() {
    return getFlag(false);
  }
  public Short getFlag(boolean forceIfSeenThenDelivered) {
    Short value = null;
    if (((mark.shortValue() & FLAG_NEW.shortValue()) != 0 || (mark.shortValue() & FLAG_MARKED_NEW.shortValue()) != 0) && firstDelivered == null)
      value = new Short(STATUS__UNSEEN_UNDELIVERED);
    else if ((mark.shortValue() & FLAG_OLD.shortValue()) != 0 && firstDelivered != null)
      value = new Short(STATUS__SEEN_DELIVERED);
    else if (((mark.shortValue() & FLAG_NEW.shortValue()) != 0 || (mark.shortValue() & FLAG_MARKED_NEW.shortValue()) != 0) && firstDelivered != null)
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
        info = "Unseen/Never Opened";
        break;
      case STATUS__SEEN_UNDELIVERED:
        info = "Seen/Never Opened";
        break;
      case STATUS__UNSEEN_DELIVERED:
        info = "Unseen/Previously Opened";
        break;
      case STATUS__SEEN_DELIVERED:
        info = "Seen/Previously Opened";
        break;
    }
    return info;
  }

  public static Long[] getLinkIDs(StatRecord[] statRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StatRecord.class, "getLinkIDs(StatRecord[] statRecords)");
    if (trace != null) trace.args(statRecords);

    ArrayList linkIDsL = new ArrayList();
    if (statRecords != null) {
      for (int i=0; i<statRecords.length; i++) {
        if (linkIDsL.contains(statRecords[i].objLinkId) == false)
          linkIDsL.add(statRecords[i].objLinkId);
      }
    }
    Long[] linkIDs = (Long[]) ArrayUtils.toArray(linkIDsL, Long.class);

    if (trace != null) trace.exit(StatRecord.class, linkIDs);
    return linkIDs;
  }

  public static Long[] getUserIDs(StatRecord[] statRecords) {
    ArrayList userIDsL = new ArrayList();
    if (statRecords != null) {
      for (int i=0; i<statRecords.length; i++) {
        if (userIDsL.contains(statRecords[i].ownerUserId) == false)
          userIDsL.add(statRecords[i].ownerUserId);
      }
    }
    Long[] userIDs = (Long[]) ArrayUtils.toArray(userIDsL, Long.class);
    return userIDs;
  }

  public static StatRecord[] gatherStatsOfType(StatRecord[] statRecords, byte objType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StatRecord.class, "gatherStatsOfType(StatRecord[] statRecords, byte objType)");
    if (trace != null) trace.args(statRecords);
    if (trace != null) trace.args(objType);
    ArrayList statsL = new ArrayList();
    if (statRecords != null) {
      for (int i=0; i<statRecords.length; i++) {
        if (statRecords[i].objType.byteValue() == objType)
          statsL.add(statRecords[i]);
      }
    }
    StatRecord[] stats = (StatRecord[]) ArrayUtils.toArray(statsL, StatRecord.class);
    if (trace != null) trace.exit(StatRecord.class, stats);
    return stats;
  }

//  public boolean isFlagGreen() {
//    return (mark.shortValue() & FLAG_NEW.shortValue()) != 0 && firstDelivered != null;
//  }

  public boolean isFlagRed() {
    return (mark.shortValue() & FLAG_NEW.shortValue()) != 0 || (mark.shortValue() & FLAG_MARKED_NEW.shortValue()) != 0;
  }
  public boolean isFlagRedManual() {
    return (mark.shortValue() & FLAG_MARKED_NEW.shortValue()) != 0;
  }
  public boolean isFlagNew() {
    return (mark.shortValue() & FLAG_NEW.shortValue()) != 0 && firstDelivered == null;
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
      + ", objType="        + objType != null ? "'"+(char)objType.byteValue()+"'" : "null"
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
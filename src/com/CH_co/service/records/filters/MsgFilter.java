/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.records.filters;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.10 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class MsgFilter extends AbstractRecordFilter implements RecordFilter {

  // keep only the ownerObjTypes
  private Short ownerObjType;
  // keep only the messages belonging to specified folder
  private Long ownerObjId;
  // keep only the messages belonging to specified folders
  private Long[] ownerObjIDs;
  // keep only chosen messages
  private Long[] msgLinkIDs;
  // keep only messages of type
  private Short objType;
  // keep only address contacts with existing email address
  private Boolean isEmailAddressPresent;
  // keep only address contacts with valid email address
  private Boolean isEmailAddressValid;
  // keep only messages which do not have body unsealed
  private Boolean isBodyUnsealed;


  /** Creates new MsgFilter */
  public MsgFilter(boolean isEmailAddressPresent) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgFilter.class, "MsgFilter(boolean isEmailAddressPresent)");
    if (trace != null) trace.args(isEmailAddressPresent);
    this.isEmailAddressPresent = Boolean.valueOf(isEmailAddressPresent);
    if (trace != null) trace.exit(MsgFilter.class);
  }

  public MsgFilter(Boolean isEmailAddressPresent, Boolean isEmailAddressValid) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgFilter.class, "MsgFilter(Boolean isEmailAddressPresent, Boolean isEmailAddressValid)");
    if (trace != null) trace.args(isEmailAddressPresent);
    if (trace != null) trace.args(isEmailAddressValid);
    this.isEmailAddressPresent = isEmailAddressPresent;
    this.isEmailAddressValid = isEmailAddressValid;
    if (trace != null) trace.exit(MsgFilter.class);
  }

  public MsgFilter(Boolean isEmailAddressPresent, Boolean isEmailAddressValid, Boolean isBodyUnsealed) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgFilter.class, "MsgFilter(Boolean isEmailAddressPresent, Boolean isEmailAddressValid, Boolean isBodyUnsealed)");
    if (trace != null) trace.args(isEmailAddressPresent);
    if (trace != null) trace.args(isEmailAddressValid);
    if (trace != null) trace.args(isBodyUnsealed);
    this.isEmailAddressPresent = isEmailAddressPresent;
    this.isEmailAddressValid = isEmailAddressValid;
    this.isBodyUnsealed = isBodyUnsealed;
    if (trace != null) trace.exit(MsgFilter.class);
  }

  /** Creates new MsgFilter */
  public MsgFilter(short objType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgFilter.class, "MsgFilter(short objType)");
    if (trace != null) trace.args(objType);
    this.objType = new Short(objType);
    if (trace != null) trace.exit(MsgFilter.class);
  }

  /** Creates new MsgFilter */
  public MsgFilter(short ownerObjType, Long ownerObjId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgFilter.class, "MsgFilter(short ownerObjType, Long ownerObjId)");
    if (trace != null) trace.args(ownerObjType);
    if (trace != null) trace.args(ownerObjId);
    this.ownerObjType = new Short(ownerObjType);
    this.ownerObjId = ownerObjId;
    if (trace != null) trace.exit(MsgFilter.class);
  }

  /** Creates new MsgFilter */
  public MsgFilter(short ownerObjType, Long[] ownerObjIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgFilter.class, "MsgFilter(short ownerObjType, Long[] ownerObjIDs)");
    if (trace != null) trace.args(ownerObjType);
    if (trace != null) trace.args(ownerObjIDs);
    this.ownerObjType = new Short(ownerObjType);
    this.ownerObjIDs = ownerObjIDs;
    if (trace != null) trace.exit(MsgFilter.class);
  }

  /** Creates new MsgFilter */
  public MsgFilter(Long[] msgLinkIDs, Short ownerObjType, Long ownerObjId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgFilter.class, "MsgFilter(Long[] msgLinkIDs, Short ownerObjType, Long ownerObjId)");
    if (trace != null) trace.args(msgLinkIDs);
    if (trace != null) trace.args(ownerObjType);
    if (trace != null) trace.args(ownerObjId);
    this.msgLinkIDs = msgLinkIDs;
    this.ownerObjType = ownerObjType;
    this.ownerObjId = ownerObjId;
    if (trace != null) trace.exit(MsgFilter.class);
  }

  /** Creates new MsgFilter */
  public MsgFilter(Long[] msgLinkIDs, Short ownerObjType, Long[] ownerObjIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgFilter.class, "MsgFilter(Long[] msgLinkIDs, Short ownerObjType, Long[] ownerObjIDs)");
    if (trace != null) trace.args(msgLinkIDs);
    if (trace != null) trace.args(ownerObjType);
    if (trace != null) trace.args(ownerObjId);
    this.msgLinkIDs = msgLinkIDs;
    this.ownerObjType = ownerObjType;
    this.ownerObjIDs = ownerObjIDs;
    if (trace != null) trace.exit(MsgFilter.class);
  }

  /*
  public boolean keep(Record record) {
    boolean keep = false;

    if (record instanceof MsgLinkRecord) {
      MsgLinkRecord linkRecord = (MsgLinkRecord) record;
      if (msgLinkIDs != null && ownerObjType != null && ownerObjId != null) {
        if (linkRecord.ownerObjType.equals(ownerObjType) && linkRecord.ownerObjId.equals(ownerObjId) && ArrayUtils.find(msgLinkIDs, linkRecord.msgLinkId) >= 0) {
          keep = true;
        }
      } else if (ownerObjType != null && ownerObjId != null) {
        if (linkRecord.ownerObjType.equals(ownerObjType) && linkRecord.ownerObjId.equals(ownerObjId)) {
          keep = true;
        }
      }
    }

    return keep;
  }
   */

  public boolean keep(Record record) {
    boolean ownerObjTypeOk = ownerObjType == null;
    boolean ownerObjIdOk = ownerObjId == null;
    boolean ownerObjIDsOk = ownerObjIDs == null;
    boolean msgLinkIdOk = msgLinkIDs == null;
    boolean objTypeOk = objType == null;
    boolean isEmailAddressPresentOk = isEmailAddressPresent == null;
    boolean isEmailAddressValidOk = isEmailAddressValid == null;
    boolean isBodyUnsealedOk = isBodyUnsealed == null;

    MsgLinkRecord mLink = null;
    MsgDataRecord mData = null;
    if (record instanceof MsgLinkRecord) {
      mLink = (MsgLinkRecord) record;
    }
    if (record instanceof MsgDataRecord) {
      mData = (MsgDataRecord) record;
    }

    if (mLink != null) {
      if (!ownerObjTypeOk) ownerObjTypeOk = ownerObjType.equals(mLink.ownerObjType);
      if (!ownerObjIdOk) ownerObjIdOk = ownerObjId.equals(mLink.ownerObjId);
      if (!ownerObjIDsOk) ownerObjIDsOk = ArrayUtils.find(ownerObjIDs, mLink.ownerObjId) >= 0;
      if (!msgLinkIdOk) msgLinkIdOk = ArrayUtils.find(msgLinkIDs, mLink.msgLinkId) >= 0;
    }
    if (mData != null) {
      if (!objTypeOk) objTypeOk = objType.equals(mData.objType);
      if (!isEmailAddressPresentOk) isEmailAddressPresentOk = isEmailAddressPresent.booleanValue() == (mData.isTypeAddress() && mData.email != null && mData.email.length() > 0);
      if (!isEmailAddressValidOk) isEmailAddressValidOk = isEmailAddressValid.booleanValue() == (mData.isTypeAddress() && mData.email != null && EmailRecord.isEmailFormatValid(mData.email));
      if (!isBodyUnsealedOk) isBodyUnsealedOk = isBodyUnsealed.booleanValue() == (mData.getTextBody() != null) && mData.getEncText() != null && mData.getEncText().size() > 0;
    }

    return ownerObjTypeOk && ownerObjIdOk && ownerObjIDsOk && msgLinkIdOk && objTypeOk && isEmailAddressPresentOk && isEmailAddressValidOk && isBodyUnsealedOk;
  }

}
/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.records;

import com.CH_co.service.records.*;
import com.CH_co.util.Misc;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class MsgUtil {

  /**
  * Gather all recipients into a String
  */
  public static String gatherAllMsgRecipients(Record[][] recipients) {
    StringBuffer sb = new StringBuffer();
    for (int i=0; recipients!= null && i<recipients.length; i++) {
      for (int k=0; recipients[i]!=null && k<recipients[i].length; k++) {
        if (i == 1) {
          sb.append(MsgDataRecord.RECIPIENT_COPY);
        } else if (i == 2) {
          sb.append(MsgDataRecord.RECIPIENT_COPY_BLIND);
        }
        Record rec = recipients[i][k];
        if (rec instanceof UserRecord) {
          sb.append(MsgDataRecord.RECIPIENT_USER);
          sb.append(' ');
          sb.append(((UserRecord) rec).userId);
        } else if (rec instanceof ContactRecord) {
          sb.append(MsgDataRecord.RECIPIENT_USER);
          sb.append(' ');
          sb.append(((ContactRecord) rec).contactWithId);
        } else if (rec instanceof FolderPair) {
          sb.append(MsgDataRecord.RECIPIENT_BOARD);
          sb.append(' ');
          sb.append(((FolderPair) rec).getId());
        } else if (rec instanceof InternetAddressRecord) {
          sb.append(MsgDataRecord.RECIPIENT_EMAIL_INTERNET);
          sb.append(' ');
          sb.append(Misc.escapeWhiteEncode(((InternetAddressRecord) rec).address));
        } else if (rec instanceof NewsAddressRecord) {
          sb.append(MsgDataRecord.RECIPIENT_EMAIL_NEWS);
          sb.append(' ');
          sb.append(Misc.escapeWhiteEncode(((NewsAddressRecord) rec).address));
        } else if (rec instanceof MsgDataRecord && ((MsgDataRecord) rec).isTypeAddress()) {
          sb.append(MsgDataRecord.RECIPIENT_EMAIL_INTERNET);
          sb.append(' ');
          sb.append(Misc.escapeWhiteEncode(((MsgDataRecord) rec).getEmailAddress()));
        } else {
          throw new IllegalArgumentException("Don't know how to handle rec from family " + Misc.getPackageName(rec.getClass()) + " and of type " + Misc.getClassNameWithoutPackage(rec.getClass()));
        }
        sb.append(' ');
      }
    }
    return sb.toString().trim();
  }

}
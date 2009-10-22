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

package com.CH_cl.service.actions.cnt;

import java.util.Vector;
import javax.swing.JOptionPane;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.cnt.*;
import com.CH_co.service.records.*;

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
 * <b>$Revision: 1.10 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class CntARemoveContacts extends ClientMessageAction {

  /** Creates new CntARemoveContacts */
  public CntARemoveContacts() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CntARemoveContacts.class, "CntARemoveContacts()");
    if (trace != null) trace.exit(CntARemoveContacts.class);
  }

  /** 
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CntARemoveContacts.class, "runAction(Connection)");

    ContactRecord[] contactRecords = ((Cnt_GetCnts_Rp) getMsgDataSet()).contactRecords;
    FetchedDataCache cache = getFetchedDataCache();
    Vector contactsToRemoveV = new Vector();
    // Find records we are about to remove so that we can display a message with its name.
    for (int i=0; i<contactRecords.length; i++) {
      ContactRecord cRec = contactRecords[i];
      ContactRecord cRecOld = cache.getContactRecord(contactRecords[i].contactId);
      if (cRecOld != null) {
        cRec.merge(cRecOld);
        contactsToRemoveV.addElement(cRec);
      }
    }
    cache.removeContactRecords(contactRecords);

    // Notify user with a dialog
    /*
    if (contactsToRemoveV.size() > 0) {
      ContactRecord[] contactsToRemove = new ContactRecord[contactsToRemoveV.size()];
      contactsToRemoveV.toArray(contactsToRemove);
      contactsToRemove = (ContactRecord[]) ArrayUtils.removeDuplicates(contactsToRemove);
      StringBuffer sb = new StringBuffer();
      for (int i=0; i<contactsToRemove.length; i++) {
        ContactRecord cRec = contactsToRemove[i];
        // Only alert when non-given contacts were removed.
        if (!cRec.isGiven()) {
          UserRecord uRec = cache.getUserRecord(cRec.contactWithId);
          String userName = uRec != null ? uRec.shortInfo() : ("(" + cRec.contactWithId + ")");
          // unSeal the right part...
          String contactName = "unknown";
          if (cache.getMyUserId().equals(cRec.ownerUserId)) {
            cRec.unSeal(cache.getFolderShareRecordMy(cRec.folderId).getSymmetricKey());
            contactName = '"' + cRec.getOwnerNote() + '"';
          } else {
            if (cRec.getOtherKeyId() != null)
              cRec.unSeal(cache.getKeyRecord(cRec.getOtherKeyId()));
            else
              cRec.unSealRecrypted(cache.getUserRecord().getSymKeyCntNotes());
            contactName = '"' + cRec.getOtherNote() + '"';
          }
          String msg = null;
          if (cRec.status.shortValue() == ContactRecord.STATUS_INITIATED && cRec.contactWithId.equals(cache.getMyUserId()))
            msg = "<html>Contact with authorization request:<br><i>" + contactName + "</i><br>with user <i>" + userName + "</i> has been removed.<br> ";
          else
            msg = "<html>Contact <i>" + contactName + "</i> with user <i>" + userName + "</i> has been removed.<br> ";
          sb.append(msg);
        }
      }
      if (sb.length() > 0) {
        String title = "Contact Removed";
        String msg = sb.toString();
        MessageDialog.showInfoDialog(null, msg, title);
      }
    }
     */

    if (trace != null) trace.exit(CntARemoveContacts.class, null);
    return null;
  }

}
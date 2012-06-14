/*
* Copyright 2001-2012 by CryptoHeaven Corp.,
* Mississauga, Ontario, Canada.
* All rights reserved.
*
* This software is the confidential and proprietary information
* of CryptoHeaven Corp. ("Confidential Information").  You
* shall not disclose such Confidential Information and shall use
* it only in accordance with the terms of the license agreement
* you entered into with CryptoHeaven Corp.
*/
package com.CH_cl.service.cache;

import com.CH_cl.service.records.EmailAddressRecord;
import com.CH_cl.service.records.NewsAddressRecord;
import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.MsgFilter;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import com.CH_co.util.Hasher;
import com.CH_co.util.Misc;
import com.CH_co.util.URLs;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
*
* @author Marcin
*/
public class CacheMsgUtils {


  /**
  * Gathers all recipients into a Record[]
  */
  public static Record[][] gatherAllMsgRecipients(MsgDataRecord dataRecord) {
    if (dataRecord != null)
      return gatherAllMsgRecipients(dataRecord.getRecipients());
    return null;
  }
  public static Record[][] gatherAllMsgRecipients(String recipients) {
    return gatherAllMsgRecipients(recipients, -1);
  }
  public static Record[][] gatherAllMsgRecipients(String recipients, int gatherFirst_N_only) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheMsgUtils.class, "gatherAllMsgRecipients(String recipients, int gatherFirst_N_only)");
    if (trace != null) trace.args(recipients);
    if (trace != null) trace.args(gatherFirst_N_only);

    ArrayList recsLto = new ArrayList();
    ArrayList recsLcc = new ArrayList();
    ArrayList recsLbcc = new ArrayList();
    int countGathered = 0;
    if (recipients != null && recipients.length() > 0) {
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      StringTokenizer st = new StringTokenizer(recipients);
      try { // If recipient list is invalid, skip them all together
        while (st.hasMoreTokens() && (gatherFirst_N_only == -1 || countGathered <= gatherFirst_N_only)) {

          String type = st.nextToken();
          char typeChar = type.charAt(0);
          boolean isCopy = typeChar == MsgDataRecord.RECIPIENT_COPY;
          boolean isCopyBlind = typeChar == MsgDataRecord.RECIPIENT_COPY_BLIND;
          if (isCopy || isCopyBlind) {
            typeChar = type.charAt(1);
          }

          // depending if TO: or CC: collect to a different set
          ArrayList recsL = isCopy ? recsLcc : recsLto;
          recsL = isCopyBlind ? recsLbcc : recsL;

          String sId = st.nextToken();
          Record rec = null;
          if (typeChar == MsgDataRecord.RECIPIENT_USER || typeChar == MsgDataRecord.RECIPIENT_BOARD) {
            Long lId = Long.valueOf(sId);
            if (typeChar == MsgDataRecord.RECIPIENT_USER) {
              rec = CacheUsrUtils.convertUserIdToFamiliarUser(lId, true, false);
              if (rec != null) {
                recsL.add(rec);
              } else {
                UserRecord uRec = new UserRecord();
                uRec.userId = lId;
                uRec.handle = "User";
                recsL.add(uRec);
              }
              countGathered ++;
            } // end "u"
            else if (typeChar == MsgDataRecord.RECIPIENT_BOARD) {
              FolderShareRecord sRec = cache.getFolderShareRecordMy(lId, true);
              FolderRecord fRec = cache.getFolderRecord(lId);
              if (sRec != null && fRec != null) {
                recsL.add(new FolderPair(sRec, fRec));
              } else {
                fRec = new FolderRecord();
                fRec.folderId = lId;
                fRec.folderType = new Short(FolderRecord.FILE_FOLDER);
                fRec.numOfShares = new Short((short)1);
                recsL.add(fRec);
              }
              countGathered ++;
            } // end "b"
          } else if (typeChar == MsgDataRecord.RECIPIENT_EMAIL_INTERNET) {
            recsL.add(new EmailAddressRecord(Misc.escapeWhiteDecode(sId)));
            countGathered ++;
          } else if (typeChar == MsgDataRecord.RECIPIENT_EMAIL_NEWS) {
            recsL.add(new NewsAddressRecord(Misc.escapeWhiteDecode(sId)));
            countGathered ++;
          }
        } // end while
      } catch (Throwable t) {
        if (trace != null) trace.data(99, "Invalid recipients list, skipping recipients", recipients);
        if (trace != null) trace.exception(CacheMsgUtils.class, 100, t);
      }
    }

    Record[][] recs = new Record[3][];
    recs[MsgLinkRecord.RECIPIENT_TYPE_TO] = new Record[recsLto.size()];
    recs[MsgLinkRecord.RECIPIENT_TYPE_CC] = new Record[recsLcc.size()];
    recs[MsgLinkRecord.RECIPIENT_TYPE_BCC] = new Record[recsLbcc.size()];
    if (recsLto.size() > 0) {
      recsLto.toArray(recs[MsgLinkRecord.RECIPIENT_TYPE_TO]);
    }
    if (recsLcc.size() > 0) {
      recsLcc.toArray(recs[MsgLinkRecord.RECIPIENT_TYPE_CC]);
    }
    if (recsLbcc.size() > 0) {
      recsLbcc.toArray(recs[MsgLinkRecord.RECIPIENT_TYPE_BCC]);
    }

    if (trace != null) trace.exit(CacheMsgUtils.class, recs);
    return recs;
  }


  public static Record getFromAsFamiliar(MsgDataRecord msgData) {
    Record fromRec = null;
    String fromEmailAddress = msgData.getFromEmailAddress();
    if (msgData.isEmail() || fromEmailAddress != null) {
      fromRec = CacheEmlUtils.convertToFamiliarEmailRecord(fromEmailAddress);
    } else {
      fromRec = CacheUsrUtils.convertUserIdToFamiliarUser(msgData.senderUserId, false, true);
    }
    return fromRec;
  }

  public static MsgLinkRecord[] getMsgLinkRecordsWithFetchedDatas(Long folderId) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    MsgLinkRecord[] mLinks = cache.getMsgLinkRecordsForFolder(folderId);
    ArrayList mLinksFetchedL = new ArrayList();
    for (int i=0; i<mLinks.length; i++) {
      MsgDataRecord mData = cache.getMsgDataRecord(mLinks[i].msgId);
      if (mData != null && mData.getEncText() != null)
        mLinksFetchedL.add(mLinks[i]);
    }
    return (MsgLinkRecord[]) ArrayUtils.toArray(mLinksFetchedL, MsgLinkRecord.class);
  }

  public static Record getMsgSenderForReply(MsgDataRecord msgData) {
    Record sender = null;
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    String fromEmailAddress = msgData.getFromEmailAddress();
    if (msgData.isEmail() || fromEmailAddress != null) {
      String[] replyTos = msgData.getReplyToAddresses();
      if (replyTos != null && (replyTos.length > 1 || (replyTos.length == 1 && !EmailRecord.isAddressEqual(replyTos[0], msgData.getFromEmailAddress())))) {
        sender = new EmailAddressRecord(replyTos[0]);
      } else {
        sender = new EmailAddressRecord(fromEmailAddress);
      }
    } else {
      sender = cache.getContactRecordOwnerWith(cache.getMyUserId(), msgData.senderUserId);
    }
    if (sender instanceof ContactRecord) {
      ContactRecord cRec = (ContactRecord) sender;
      if (!cRec.isOfActiveType()) 
        sender = cache.getUserRecord(msgData.senderUserId);
    } else if (sender == null) {
      sender = cache.getUserRecord(msgData.senderUserId);
    }
    // if the sender was a User but is now deleted, lets create an email address instead
    if (sender == null && !msgData.isEmail()) {
      sender = new EmailAddressRecord("" + msgData.senderUserId + "@" + URLs.getElements(URLs.DOMAIN_MAIL)[0]);
    }
    return sender;
  }

  public static boolean hasAttachments(Record rec) {
    boolean hasAttachments = false;
    if (rec instanceof MsgLinkRecord) {
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      MsgDataRecord msgData = cache.getMsgDataRecord(((MsgLinkRecord) rec).msgId);
      hasAttachments = msgData.hasAttachments();
    } else if (rec instanceof MsgDataRecord) {
      hasAttachments = ((MsgDataRecord) rec).hasAttachments();
    }
    return hasAttachments;
  }

  public static void unlockPassProtectedMsg(MsgDataRecord msgDataRecStartWith, Hasher.Set bodyKey) {
    // first unseal the specific message so its fast for the user in the view
    unlockPassProtectedMsgs(new MsgDataRecord[] { msgDataRecStartWith }, bodyKey);
    // next unseal any other message that might be using the same password...
    Thread th = new ThreadTraced("UnlockPassProtectedMsgs Runner") {
      public void runTraced() {
        unlockPassProtectedMsgs(null, null);
      }
    };
    th.setDaemon(true);
    th.start();
  }
  private static void unlockPassProtectedMsgs(MsgDataRecord[] msgDatas, Hasher.Set bodyKey) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    if (msgDatas == null)
      msgDatas = cache.getMsgDataRecords(new MsgFilter((Boolean) null, (Boolean) null, Boolean.FALSE));
    List bodyKeys = null;
    if (bodyKey == null) {
      bodyKeys = cache.getMsgBodyKeys();
    } else {
      bodyKeys = new ArrayList();
      bodyKeys.add(bodyKey);
    }
    ArrayList msgDatasL = new ArrayList();
    ArrayList msgLinksL = new ArrayList();
    if (msgDatas != null) {
      for (int i=0; i<msgDatas.length; i++) {
        MsgLinkRecord[] msgLinks = cache.getMsgLinkRecordsForMsg(msgDatas[i].msgId);
        if (msgLinks != null && msgLinks.length > 0) {
          // Find a symmetric key from links that might have been password protected and not unsealed yet...
          BASymmetricKey symmetricKey = null;
          for (int k=0; k<msgLinks.length; k++) {
            if (msgLinks[k].getSymmetricKey() != null) {
              symmetricKey = msgLinks[k].getSymmetricKey();
              break;
            }
          }
          if (symmetricKey != null) {
            if (msgDatas[i].getSendPrivKeyId() != null) {
              // for performance don't verify everything, do it when person asks to see it
              msgDatas[i].unSealWithoutVerify(symmetricKey, bodyKeys);
            }
            //msgDatas[i].unSealWithoutVerify(symmetricKey, bodyKeys);
            if (msgDatas[i].getTextBody() != null) {
              MsgLinkRecord.clearPostRenderingCache(msgLinks);
              msgDatasL.add(msgDatas[i]);
              for (int k=0; k<msgLinks.length; k++)
                msgLinksL.add(msgLinks[k]);
            }
          }
        }
      }
    }
    MsgDataRecord[] msgDatasUnsealed = (MsgDataRecord[]) ArrayUtils.toArray(msgDatasL, MsgDataRecord.class);
    MsgLinkRecord[] msgLinksUnsealed = (MsgLinkRecord[]) ArrayUtils.toArray(msgLinksL, MsgLinkRecord.class);
    // trigger listeners to update changed messages
    cache.addMsgDataRecords(msgDatasUnsealed);
    cache.addMsgLinkRecords(msgLinksUnsealed);

    if (msgDatasUnsealed != null && msgDatasUnsealed.length > 0) {
      // check if unsealing some messages can unlock any attachments
      Long[] msgDataIDsUnsealed = RecordUtils.getIDs(msgDatasUnsealed);
      // start with file attachments...
      FileLinkRecord[] fileLinkAttachments = cache.getFileLinkRecordsOwnersAndType(msgDataIDsUnsealed, new Short(Record.RECORD_TYPE_MESSAGE));
      // adding attachments back to cache will unseal them and allow for unsealing of nested bodies...
      // it will also refresh registered listener viewes with unsealed data...
      cache.addFileLinkRecords(fileLinkAttachments);
      // follow with message attachments...
      MsgLinkRecord[] msgLinkAttachments = cache.getMsgLinkRecordsOwnersAndType(msgDataIDsUnsealed, new Short(Record.RECORD_TYPE_MESSAGE));
      // adding attachments back to cache will unseal them and allow for unsealing of nested bodies...
      cache.addMsgLinkRecords(msgLinkAttachments);
      // if there are message attachments present, unseal their bodies too
      // it will also refresh registered listener viewes with unsealed data...
      if (msgLinkAttachments != null && msgLinkAttachments.length > 0) {
        Long[] msgLinkAttachmentIDs = RecordUtils.getIDs(msgLinkAttachments);
        MsgDataRecord[] msgDataAttachments = cache.getMsgDataRecordsForLinks(msgLinkAttachmentIDs);
        if (msgDataAttachments != null && msgDataAttachments.length > 0) {
          // recursively go through the attachments
          unlockPassProtectedMsgs(msgDataAttachments, null);
        }
      }
    }
  }

}
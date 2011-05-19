/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_cl.service.actions.msg;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.*;
import com.CH_cl.service.cache.event.*;
import com.CH_cl.service.records.FolderRecUtil;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.msg.dataSets.stat.*;
import com.CH_co.service.records.*;

import java.sql.Timestamp;
import java.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2011
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
 * <b>$Revision: 1.18 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class MsgAGet extends ClientMessageAction {

  public static int MAX_BATCH_MILLIS = 2000;

  /** Creates new MsgAGet */
  public MsgAGet() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgAGet.class, "MsgAGet()");
    if (trace != null) trace.exit(MsgAGet.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgAGet.class, "runAction(Connection)");

    Msg_GetLinkAndData_Rp reply = (Msg_GetLinkAndData_Rp) getMsgDataSet();

    Short fetchingOwnerObjType = reply.ownerObjType;
    Long fetchingOwnerObjId = reply.ownerObjId;
    Short fetchNumMax = reply.fetchNumMax;
    Short fetchNumNew = reply.fetchNumNew;
    Timestamp timestamp = reply.timestamp;
    boolean anySkippedOver = reply.anySkippedOver;
    MsgLinkRecord[] linkRecords = reply.linkRecords;
    MsgDataRecord[] dataRecords = reply.dataRecords;
    StatRecord[] statRecords = reply.stats_rp != null ? reply.stats_rp.stats : null;

    FetchedDataCache cache = getFetchedDataCache();
    Set groupIDsSet = null;


    HashSet newDataRecordIDsHS = new HashSet();
    if (dataRecords != null) {
      for (int i=0; i<dataRecords.length; i++) {
        if (cache.getMsgDataRecord(dataRecords[i].msgId) == null)
          newDataRecordIDsHS.add(dataRecords[i].msgId);
      }
    }


    // Decompress all recipients and gather all unknown user recipients/senders to later on fetch the user handles.
    ArrayList userIDsL = null;
    for (int i=0; i<dataRecords.length; i++) {
      MsgDataRecord data = dataRecords[i];

      if (cache.getUserRecord(data.senderUserId) == null) {
        if (userIDsL == null) userIDsL = new ArrayList();
        userIDsL.add(data.senderUserId);
      }

      if (trace != null) trace.data(10, "Decompressing recipients for msgId", data.msgId);
      data.decompressRecipients();

      String recipients = data.getRecipients();
      if (recipients != null) {
        String[] recips = recipients.split("[ ]+");
        int recipsIndex = recips[0].equals("") ? 1 : 0; // ignore leading delimited blanks

        try { // If recipient list is invalid, skip them all together
          while (recips != null && recips.length > recipsIndex) {
            String type = recips[recipsIndex++];
            String sId = recips[recipsIndex++];
            if (type.charAt(0) == MsgDataRecord.RECIPIENT_USER ||
                (type.charAt(0) == MsgDataRecord.RECIPIENT_COPY && type.charAt(1) == MsgDataRecord.RECIPIENT_USER) ||
                (type.charAt(0) == MsgDataRecord.RECIPIENT_COPY_BLIND && type.charAt(1) == MsgDataRecord.RECIPIENT_USER)
                ) {
              Long id = Long.valueOf(sId);
              if (cache.getUserRecord(id) == null) {
                if (userIDsL == null) userIDsL = new ArrayList();
                userIDsL.add(id);
              }
            }
          }
        } catch (Throwable t) {
          if (trace != null) trace.data(99, "Exception while tokenizing recipients string, error will be ignored.", recipients);
          if (trace != null) trace.exception(MsgAGet.class, 100, t);
        }
      }
    }
    // if found unknown users, get handles
    if (userIDsL != null && userIDsL.size() > 0) {
      Long[] userIDs = (Long[]) ArrayUtils.toArray(userIDsL, Long.class);
      userIDs = (Long[]) ArrayUtils.removeDuplicates(userIDs);
      getServerInterfaceLayer().submitAndWait(new MessageAction(CommandCodes.USR_Q_GET_HANDLES, new Obj_IDList_Co(userIDs)), 30000);
    }


//    // gather all signing keys which we don't have yet and are necessary to verity signatures
//    Vector keyIDsV = null;
//    for (int i=0; i<dataRecords.length; i++) {
//      // keyId will be null if it was a Brief that was fetched
//      Long keyId = dataRecords[i].getSendPrivKeyId();
//      if (keyId != null && cache.getKeyRecord(keyId) == null) {
//        if (keyIDsV == null) keyIDsV = new Vector();
//        keyIDsV.addElement(keyId);
//      }
//    }
//    // for performance, don't request all keys right away, do it when person asks to see it
//    if (keyIDsV != null && keyIDsV.size() > 0) {
//      Long[] keyIDs = new Long[keyIDsV.size()];
//      keyIDsV.toArray(keyIDs);
//      keyIDs = (Long[]) ArrayUtils.removeDuplicates(keyIDs);
//      getServerInterfaceLayer().submitAndWait(new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_KEYIDS, new Obj_IDList_Co(keyIDs)), 30000, 3);
//    }


    // gather all Folder Records that we don't have fetched yet
    ArrayList folderIDsL = null;
    for (int i=0; i<linkRecords.length; i++) {
      MsgLinkRecord link = linkRecords[i];
      if (link.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
        if (groupIDsSet == null) groupIDsSet = cache.getFolderGroupIDsMySet();
        if (cache.getFolderShareRecordMy(link.ownerObjId, groupIDsSet) == null) {
          if (folderIDsL == null) folderIDsL = new ArrayList();
          folderIDsL.add(link.ownerObjId);
        }
      }
    }
    if (folderIDsL != null && folderIDsL.size() > 0) {
      Long[] folderIDs = (Long[]) ArrayUtils.toArray(folderIDsL, Long.class);
      folderIDs = (Long[]) ArrayUtils.removeDuplicates(folderIDs);
      getServerInterfaceLayer().submitAndWait(new MessageAction(CommandCodes.FLD_Q_GET_FOLDERS_SOME, new Obj_IDList_Co(folderIDs)), 60000);
    }

    // Gather messages for removal by looking for non-existing messages, this works in fetches done in stages because they return data in sorted sequencial batches.
    MsgLinkRecord[] toRemoveMsgLinks = null;
    if (!anySkippedOver && fetchNumMax != null && fetchNumNew != null) {
      // TODO remove temp check below
      // temporary check before we upgrade engines to build 638 or newer
      if ((getClientContext().serverBuild < 638 && timestamp != null) || getClientContext().serverBuild >= 638)
      if (linkRecords != null && linkRecords.length > 0) {
        MsgLinkRecord[] priorMsgs = cache.getMsgLinkRecords(linkRecords[0].dateCreated, linkRecords[linkRecords.length-1].dateCreated, fetchingOwnerObjId, fetchingOwnerObjType);
        toRemoveMsgLinks = (MsgLinkRecord[]) ArrayUtils.getDifference(priorMsgs, linkRecords);
      }
    }

    // We need data Records in the cache before the message table can display contents.
    // For that reason, the event will be fired when we are done with both, links and datas.
    // Suppress events to wait for additional needed message bodies.
    cache.addMsgLinkAndDataRecords(linkRecords, dataRecords, true);


    // If the message is a notification, it might be a BRIEF, and may need to be placed in a posting folder,
    // in which case we need the body.  Such notifications always come in singles, with all links pointing to single MsgDataRecord.
    if (linkRecords != null && linkRecords.length > 0) {
      // These 3 lists will go hand-in-hand to create sets of IDs for msgBody requests.
      // So far, there is no such case that will require to send more than 1 msg body request,
      // but to accomodate for the remote possibility of introducing such thing in the futury, handle any number of bodies one-by-one.
      ArrayList needMsgBody_dataIDsV = null;
      ArrayList needMsgBody_shareIDsV = null;
      ArrayList needMsgBody_linkIDsV = null;
      for (int i=0; i<linkRecords.length; i++) {
        MsgLinkRecord msgLink = linkRecords[i];
        if (msgLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
          FolderRecord fRec = cache.getFolderRecord(msgLink.ownerObjId);
          // If the folder was ever fetched, then get body, otherwise, we never displayed its data, and do not need the body.
          if (fRec != null && (
                (fRec.folderType.shortValue() == FolderRecord.POSTING_FOLDER && FolderRecUtil.wasFolderFetchRequestIssued(fRec.folderId)) ||
                fRec.isChatting() // always fetch chatting messages
                ))
          {
            MsgDataRecord msgData = null;
            if (dataRecords != null && dataRecords.length > 0)
              msgData = (MsgDataRecord) RecordUtils.find(dataRecords, msgLink.msgId);
            if (msgData == null || msgData.getEncSignedDigest() == null)
              msgData = cache.getMsgDataRecord(msgLink.msgId);
            if (msgData == null || msgData.getEncSignedDigest() == null) {
              // Can't find a body anywhere, just fetch it. -- but we must have a shareID
              if (groupIDsSet == null) groupIDsSet = cache.getFolderGroupIDsMySet();
              FolderShareRecord sRec = cache.getFolderShareRecordMy(fRec.folderId, groupIDsSet);
              if (sRec != null) {
                if (needMsgBody_dataIDsV == null) {
                  needMsgBody_dataIDsV = new ArrayList();
                  needMsgBody_shareIDsV = new ArrayList();
                  needMsgBody_linkIDsV = new ArrayList();
                }
                if (!needMsgBody_dataIDsV.contains(msgLink.msgId)) {
                  needMsgBody_dataIDsV.add(msgLink.msgId);
                  needMsgBody_shareIDsV.add(sRec.shareId);
                  needMsgBody_linkIDsV.add(msgLink.msgLinkId);
                }
              }
            }
          }
        }
      }
      // Process all body fetch requests that are needed before the links can be displayed correctly.
      if (needMsgBody_dataIDsV != null) {
        for (int i=0; i<needMsgBody_dataIDsV.size(); i++) {
          Obj_IDList_Co request = new Obj_IDList_Co(new Long[] {(Long)(needMsgBody_shareIDsV.get(i)), (Long)(needMsgBody_linkIDsV.get(i)), null, new Long(1)});
          getServerInterfaceLayer().submitAndWait(new MessageAction(CommandCodes.MSG_Q_GET_BODY, request), 30000);
        }
      }
    }

    // Update the fetched stats if any, do this after fetching message bodies
    // so that the original values count and automatic fetching doesn't affect the shown stats.
    // Stats in the database are unaffected by this client sequence overwrite.
    // Update the stats before fireing events for messages so the flags get displayed at the same time as messages.
    if (statRecords != null)
      cache.addStatRecords(statRecords);

    // fire events now to be sure that no messages are skipped if their bodies were not just fetched...
    if (linkRecords != null && linkRecords.length > 0)
      cache.fireMsgLinkRecordUpdated(linkRecords, MsgLinkRecordEvent.SET);
    if (dataRecords != null && dataRecords.length > 0) {
      if (linkRecords == null || linkRecords.length == 0) {
        MsgLinkRecord[] relatedLinks = cache.getMsgLinkRecordsForMsgs(RecordUtils.getIDs(dataRecords));
        cache.fireMsgLinkRecordUpdated(relatedLinks, MsgLinkRecordEvent.SET);
      }
      cache.fireMsgDataRecordUpdated(dataRecords, MsgDataRecordEvent.SET);
    }

    // Remove the skipped messages from the cache, and also remove the Message Datas with no more links to them
    if (toRemoveMsgLinks != null && toRemoveMsgLinks.length > 0) {
      MsgDataRecord[] toRemoveDatas = cache.getMsgDataRecordsForLinks(RecordUtils.getIDs(toRemoveMsgLinks));
      cache.removeMsgLinkRecords(toRemoveMsgLinks);
      ArrayList toRemoveDatasL = new ArrayList();
      for (int i=0; i<toRemoveDatas.length; i++) {
        MsgLinkRecord[] otherMsgLinks = cache.getMsgLinkRecordsForMsg(toRemoveDatas[i].msgId);
        if (otherMsgLinks == null || otherMsgLinks.length == 0) {
          toRemoveDatasL.add(toRemoveDatas[i]);
        }
      }
      toRemoveDatas = (MsgDataRecord[]) ArrayUtils.toArray(toRemoveDatasL, MsgDataRecord.class);
      if (toRemoveDatas != null && toRemoveDatas.length > 0) {
        cache.removeMsgDataRecords(toRemoveDatas);
      }
    }

//    // Fire the suppressed events as the needed bodies should be in the cache already.
//    cache.fireMsgLinkRecordUpdated(linkRecords, RecordEvent.SET);
//    cache.fireMsgDataRecordUpdated(dataRecords, RecordEvent.SET);

    // Gather all Message Links that we don't have stat records for, and fetch the stats
    {
      HashSet shareIDsHS = null;
      HashSet objLinkIDsHS = null;
      for (int i=0; i<linkRecords.length; i++) {
        MsgLinkRecord link = linkRecords[i];
        if (cache.getStatRecord(link.msgLinkId, FetchedDataCache.STAT_TYPE_MESSAGE) == null) {
          if (link.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
            if (groupIDsSet == null) groupIDsSet = cache.getFolderGroupIDsMySet();
            FolderShareRecord share = cache.getFolderShareRecordMy(link.ownerObjId, groupIDsSet);
            if (shareIDsHS == null) shareIDsHS = new HashSet();
            if (objLinkIDsHS == null) objLinkIDsHS = new HashSet();
            if (!shareIDsHS.contains(share.shareId))
              shareIDsHS.add(share.shareId);
            Long msgLinkId = link.msgLinkId;
            if (!objLinkIDsHS.contains(msgLinkId))
              objLinkIDsHS.add(msgLinkId);
          }
        }
      }
      if (shareIDsHS != null && shareIDsHS.size() > 0 && objLinkIDsHS != null && objLinkIDsHS.size() > 0) {
        Long[] shareIDs = (Long[]) ArrayUtils.toArray(shareIDsHS, Long.class);
        Long[] objLinkIDs = (Long[]) ArrayUtils.toArray(objLinkIDsHS, Long.class);

        Stats_Get_Rq request = new Stats_Get_Rq();
        request.statsForObjType = new Short(Record.RECORD_TYPE_MSG_LINK);
        request.ownerObjType = new Short(Record.RECORD_TYPE_SHARE);
        request.ownerObjIDs = shareIDs;
        request.objLinkIDs = objLinkIDs;

        getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.STAT_Q_GET, request), 30000);
      }
    }


    {
      // see if any message links need to be recrypted to symmetric encryption
      ArrayList linksL = null;
      ArrayList shareL = null;
      for (int i=0; i<linkRecords.length; i++) {
        MsgLinkRecord link = linkRecords[i];
        if (link.isUnSealed() && link.getRecPubKeyId() != null) {
          FolderRecord fOwner = cache.getFolderRecord(link.ownerObjId);
          if (fOwner != null) {
            if (groupIDsSet == null) groupIDsSet = cache.getFolderGroupIDsMySet();
            FolderShareRecord sOwner = cache.getFolderShareRecordMy(fOwner.folderId, groupIDsSet);
            if (sOwner != null) {
              if (sOwner.isOwnedByUser() && sOwner.ownerUserId.equals(cache.getMyUserId()) && sOwner.canWrite.shortValue() != FolderShareRecord.YES) {
                // no-op; don't recrypt, no permission
              } else {
                // Clone the link to prevent accidental update when it is retrieved from the server.
                // When retrieved (actively or pasively), it could have the key encrypted asymetrically which
                // would result in removal of recipient key id keeping the encryption assymetric...
                link = (MsgLinkRecord) link.clone();
                link.seal(sOwner.getSymmetricKey());
                if (linksL == null) linksL = new ArrayList();
                linksL.add(link);
                if (shareL == null) shareL = new ArrayList();
                shareL.add(sOwner.shareId);
              }
            }
          }
        }
      }
      // if message links for re-cryption to symmetric encryption, send request
      if (linksL != null && linksL.size() > 0) {
        MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) ArrayUtils.toArray(linksL, MsgLinkRecord.class);
        Long[] shareIDs = (Long[]) ArrayUtils.toArray(shareL, Long.class);
        shareIDs = (Long[]) ArrayUtils.removeDuplicates(shareIDs);
        Msg_ToSymEnc_Rq request = new Msg_ToSymEnc_Rq(shareIDs, msgLinks);
        getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.MSG_Q_TO_SYM_ENC, request));
      }
    }


    // see if any email message has unknown from address, or any Address Record has unknown hash, if so, request it from the server
    {
      ArrayList hashesL = null;
      for (int i=0; i<dataRecords.length; i++) {
        MsgDataRecord dataRecord = dataRecords[i];
        String fromEmailAddress = dataRecord.getFromEmailAddress();
        byte[] hash = null;
        if (dataRecord.isEmail() || fromEmailAddress != null) {
          // convert address to hash
          hash = cache.getAddrHashForEmail(fromEmailAddress);
        } else if (dataRecord.isTypeAddress()) {
          String emailAddr = dataRecord.getEmailAddress();
          hash = cache.getAddrHashForEmail(emailAddr);
        }
        if (hash != null) {
          // if hash not already in the cache, add it to be fetched
          // always fetch hashes for new Address Records in case Address Record was deleted and recreated
          // (there could be wanted duplicates of hashes when address exists in Address Book and Allowed Senders book)
          if ((dataRecord.isTypeAddress() && newDataRecordIDsHS.contains(dataRecord.msgId)) ||
              (cache.getAddrHashRecords(hash) == null && !cache.wasRequestedAddrHash(hash)))
          {
            if (hashesL == null) hashesL = new ArrayList();
            hashesL.add(hash);
          }
        }
      }
      if (hashesL != null) {
        Obj_List_Co requestSet = new Obj_List_Co(hashesL);
        getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.ADDR_Q_FIND_HASH, requestSet), 30000);
        cache.addRequestedAddrHashes(hashesL);
      }
    }


    // if this was a bulk fetch done in stages, continue the fetch
    if (linkRecords != null && fetchNumMax != null && fetchNumNew != null) {
      int fetchNumMaxLength = Math.abs(fetchNumMax.shortValue());
      if (fetchingOwnerObjType != null && fetchingOwnerObjType.shortValue() != Record.RECORD_TYPE_FOLDER)
        throw new IllegalStateException("Don't know how to handle staged fetching of non-folder messages.");
      Long fetchingFolderId = fetchingOwnerObjId; // this was always a folder staged fetch so should be compatible with back versions
      FolderRecord fetchingFolderRec = cache.getFolderRecord(fetchingFolderId);
      if (linkRecords.length < Math.min(fetchNumMaxLength, fetchNumNew.shortValue())) {
        // fetch completed, time to notify listeners
        // When connecting to pre build 388 engine this may be null when number of messages in the folder modulus fetchNumMax = 0, so check it just in case...
        if (fetchingFolderRec != null)
          cache.fireFolderRecordUpdated(new FolderRecord[] { fetchingFolderRec }, RecordEvent.FOLDER_FETCH_COMPLETED);
      } else {
        if (isInterrupted()) {
          interrupt();
          // When connecting to pre build 388 engine this may be null when number of messages in the folder modulus fetchNumMax = 0, so check it just in case...
          if (fetchingFolderRec != null)
            cache.fireFolderRecordUpdated(new FolderRecord[] { fetchingFolderRec }, RecordEvent.FOLDER_FETCH_INTERRUPTED);
        } else {
          Timestamp timeStamp = linkRecords[linkRecords.length-1].dateCreated;
          if (groupIDsSet == null) groupIDsSet = cache.getFolderGroupIDsMySet();
          Long fetchingShareId = cache.getFolderShareRecordMy(fetchingFolderId, groupIDsSet).shareId;

          // are these full messages, or briefs?
          boolean full = dataRecords[0].getEncText() != null;

          short numNew = fetchNumNew.shortValue();
          short numMax = fetchNumMax.shortValue();

          long startTime = getStampTime();
          long endTime = System.currentTimeMillis();
          double ellapsed = (double) Math.max(1, endTime-startTime); // avoid division by zero
          double multiplier = ((double) MAX_BATCH_MILLIS) / ellapsed; // adjust the new fetch size so that it doesn't take too much time
          // multiplier cannot make too drastic of a change
          multiplier = Math.max(0.2, Math.min(10.0, multiplier));

          // if only new msgs fetched apply adjustment
          if (linkRecords.length == fetchNumNew.shortValue()) {
            numNew = (short) (fetchNumNew.shortValue() * multiplier);
            if (numNew == 0) numNew = fetchNumNew.shortValue(); // on error repeat previous iteration value
            // non-new msgs should be at least 1 more than new msgs, this is to allow for next adjustment when new msgs end fetching
            if (numMax > 0)
              numMax = (short) Math.max(numMax, numNew+1);
            else if (numMax < 0)
              numMax = (short) -Math.max(-numMax, numNew+1);
          }

          // if fetched any regular non-new msgs apply adjustment to regular msg fetch size
          if (linkRecords.length > Math.abs(fetchNumNew.shortValue())) {
            numMax = (short) (fetchNumMax.shortValue() * multiplier);
            if (numMax == 0) numMax = fetchNumMax.shortValue(); // on error repeat previous iteration value
          }

          // apply hard maximum limits
          if (numMax > Msg_GetMsgs_Rq.FETCH_NUM_LIST__MAX_SIZE__HARD_LIMIT)
            numMax = Msg_GetMsgs_Rq.FETCH_NUM_LIST__MAX_SIZE__HARD_LIMIT;
          else if (numMax < -Msg_GetMsgs_Rq.FETCH_NUM_LIST__MAX_SIZE__HARD_LIMIT)
            numMax = -Msg_GetMsgs_Rq.FETCH_NUM_LIST__MAX_SIZE__HARD_LIMIT;
          if (numNew > Msg_GetMsgs_Rq.FETCH_NUM_NEW__MAX_SIZE__HARD_LIMIT)
            numNew = Msg_GetMsgs_Rq.FETCH_NUM_NEW__MAX_SIZE__HARD_LIMIT;

          // apply hard minimum limits
          if (numMax > 0)
            numMax = (short) Math.max(numMax, Msg_GetMsgs_Rq.FETCH_NUM_LIST__INITIAL_SIZE);
          else if (numMax < 0)
            numMax = (short) -Math.max(-numMax, Msg_GetMsgs_Rq.FETCH_NUM_LIST__INITIAL_SIZE);
          numNew = (short) Math.max(numNew, Msg_GetMsgs_Rq.FETCH_NUM_NEW__INITIAL_SIZE);

          Msg_GetMsgs_Rq request = new Msg_GetMsgs_Rq(fetchingShareId, Record.RECORD_TYPE_FOLDER, fetchingFolderId, numMax, numNew, timeStamp);
          int actionCode = full ? CommandCodes.MSG_Q_GET_FULL : CommandCodes.MSG_Q_GET_BRIEFS;
          MessageAction msgAction = new MessageAction(actionCode, request);
          msgAction.setInterruptsFrom(this);
          //msgAction.setPriority(PriorityJobFifo.MAIN_WORKER_HIGH_PRIORITY);
          getServerInterfaceLayer().submitAndReturn(msgAction, 30000);
        }
      }
    }

    if (trace != null) trace.exit(MsgAGet.class, null);
    return null;
  }

}
/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.ops;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.TextRenderer;
import com.CH_cl.service.engine.DefaultReplyRunner;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.fld.Fld_Folders_Rp;
import com.CH_co.service.msg.dataSets.fld.Fld_NewFld_Rq;
import com.CH_co.service.records.*;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_co.util.CallbackI;

/** 
* Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.22 $</b>
*
* @author  Marcin Kurzawa
*/
public class ChatOps {

  /** 
  * Look in the cache for existing chat folder 
  * @return null if not found or FolderPair that matches the member exactly.
  */
  public static FolderPair doGetChatFolder(MemberContactRecordI chatWithContact) {
    return doGetChatFolder(new MemberContactRecordI[] { chatWithContact });
  }

  /** 
  * Look in the cache for existing chat folder 
  * @return null if not found or FolderPair that matches the members exactly.
  */
  public static FolderPair doGetChatFolder(MemberContactRecordI[] chatWithContacts) {
    // Look for a folder that is shared exclusively with specified contact(s).
    FolderRecord[] chatFlds = FetchedDataCache.getSingleInstance().getFolderRecordsChatting();
    FolderPair chatFolderPair = FolderOps.getChatFolderPairFromCache(chatWithContacts, chatFlds);
    return chatFolderPair;
  }

  /**
  * Queries the server for matching Chat folder.
  * @param SIL
  * @param chatWithContacts
  * @param chatFolderPairCallback 
  */
  public static void doCreateOrFetchChatFolder(final ServerInterfaceLayer _SIL, final MemberContactRecordI _chatWithContact, final CallbackI _chatFolderPairCallback) {
    doCreateOrFetchChatFolder(_SIL, new MemberContactRecordI[] { _chatWithContact }, _chatFolderPairCallback);
  }
  public static void doCreateOrFetchChatFolder(final ServerInterfaceLayer _SIL, final MemberContactRecordI[] _chatWithContacts, final CallbackI _chatFolderPairCallback) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ChatOps.class, "doCreateOrFetchChatFolder(ServerInterfaceLayer SIL, MemberContactRecordI[] chatWithContacts, CallbackI chatFolderPairCallback)");
    if (trace != null) trace.args(_chatWithContacts);

    ThreadTraced th = new ThreadTraced(new Runnable() {
      public void run() {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");
        if (trace != null) trace.args(_chatWithContacts);

        FolderPair chatPair = null;

        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        StringBuffer folderName = new StringBuffer(com.CH_cl.lang.Lang.rb.getString("folderName_Chat_Log"));
        String folderDesc = "";
        String ownerHandle = cache.getUserRecord().handle;
        folderName.append(" : ");
        folderName.append(ownerHandle);
        if (_chatWithContacts.length > 1) {
          for (int i=0; i<_chatWithContacts.length; i++) {
            String name = TextRenderer.getRenderedText(_chatWithContacts[i]);
            folderName.append(", ");
            // if there are too many shares that would make the name too long, then just truncate it
            if (folderName.length() + name.length() > 100) {
              folderName.append(" ...");
              break;
            } else {
              folderName.append(name);
            }
          }
        } else {
          String otherName = TextRenderer.getRenderedText(_chatWithContacts[0]);
          folderName.append(", ");
          folderName.append(otherName);
        }
        BASymmetricKey baSymmetricKey = new BASymmetricKey(32);

        // Create a folder share with the chatting partner.
        boolean isError = false;
        FolderShareRecord[] additionalShares = new FolderShareRecord[_chatWithContacts.length];
        for (int i=0; i<_chatWithContacts.length; i++) {
          additionalShares[i] = new FolderShareRecord();
          FolderShareRecord newShare = additionalShares[i];
          if (_chatWithContacts[i].getMemberType() == Record.RECORD_TYPE_USER) {
            ContactRecord cRec = (ContactRecord) _chatWithContacts[i];
            if (cRec.ownerUserId.equals(cache.getMyUserId()))
              newShare.ownerUserId = cRec.contactWithId;
            else {
              isError = true;
              break;
            }
            newShare.ownerType = new Short(Record.RECORD_TYPE_USER);
          } else if (_chatWithContacts[i].getMemberType() == Record.RECORD_TYPE_GROUP) {
            newShare.ownerUserId = ((FolderPair) _chatWithContacts[i]).getId();
            newShare.ownerType = new Short(Record.RECORD_TYPE_GROUP);
          }
          newShare.canWrite = new Short(FolderShareRecord.YES);
          newShare.canDelete = new Short(FolderShareRecord.YES);
          newShare.setSymmetricKey(baSymmetricKey);
        }

        if (!isError) {
          String myFolderName = folderName.toString();
          String shareFolderName = folderName.toString();

          Fld_NewFld_Rq request = FolderOps.createNewFldRq(
              null,
              FolderRecord.CHATTING_FOLDER, // Chatting folder since July 2006
              myFolderName, folderDesc, shareFolderName, folderDesc,
              new Short(FolderRecord.DEFAULT_CHAT_PURGING_RECORD_NUM),
              new Integer(FolderRecord.DEFAULT_CHAT_PURGING_RECORD_SECONDS),
              baSymmetricKey,
              false,
              additionalShares,
              _SIL
              );
          baSymmetricKey.clearContent();

          ClientMessageAction msgAction = _SIL.submitAndFetchReply(new MessageAction(CommandCodes.FLD_Q_NEW_OR_GET_OLD, request), 30000);
          DefaultReplyRunner.nonThreadedRun(_SIL, msgAction);
          if (msgAction != null && msgAction.getActionCode() == CommandCodes.FLD_A_GET_FOLDERS) {
            Fld_Folders_Rp reply = (Fld_Folders_Rp) msgAction.getMsgDataSet();
            FolderRecord folderRec = reply.folderRecords[0];
            // Shares might have came in any order, so just ask cache to get the right one.
            FolderShareRecord shareRec = cache.getFolderShareRecordMy(folderRec.folderId, true);
            chatPair = new FolderPair(shareRec, folderRec);
          }
        }

        _chatFolderPairCallback.callback(chatPair);
        if (trace != null) trace.exit(getClass());
      }

    }, "Chat create/fetch");
    th.setDaemon(true);
    th.start();

    if (trace != null) trace.exit(ChatOps.class);
  }

}
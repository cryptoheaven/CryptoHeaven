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

package com.CH_gui.contactTable;

import com.CH_gui.dialog.*;
import com.CH_gui.frame.*;
import com.CH_gui.list.*;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.ops.*;

import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.dataSets.fld.*;
import com.CH_co.service.msg.*;
import com.CH_co.trace.*;

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
 * <b>$Revision: 1.19 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class ChatSessionCreator extends ThreadTraced {

  private MemberContactRecordI[] chatWithContacts;

  public ChatSessionCreator(MemberContactRecordI chatWithContact) {
    this(new MemberContactRecordI[] { chatWithContact });
  }
  /** Creates new ChatSessionCreator */
  public ChatSessionCreator(MemberContactRecordI[] chatWithContacts) {
    super("ChatSessionCreator");
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ChatSessionCreator.class, "ChatSessionCreator(MemberContactRecordI chatWithContacts)");
    if (trace != null) trace.args(chatWithContacts);
    setDaemon(true);
    this.chatWithContacts = chatWithContacts;
    if (trace != null) trace.exit(ChatSessionCreator.class);
  }


  public void runTraced() {
    // Look for a folder that is shared exclusively with this one contact.
    FolderRecord[] chatFlds = FetchedDataCache.getSingleInstance().getFolderRecordsChatting();
    FolderPair chatFolderPair = FolderOps.getChatFolderPairFromCache(chatWithContacts, chatFlds);

    // Found required chatting folder.
    if (chatFolderPair != null) {
      new ChatTableFrame(chatFolderPair);
    }
    // Chatting folder not found, attempt creating it.
    else {
      chatFolderPair = doCreateOrFetchChatFolder(chatWithContacts);
      if (chatFolderPair != null)
        new ChatTableFrame(chatFolderPair);
    }
  } // end run()


  private static FolderPair doCreateOrFetchChatFolder(MemberContactRecordI[] chatWithContacts) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ChatSessionCreator.class, "doCreateOrFetchChatFolder(MemberContactRecordI[] chatWithContacts)");
    if (trace != null) trace.args(chatWithContacts);

    FolderPair chatPair = null;

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();

    StringBuffer folderName = new StringBuffer(com.CH_gui.lang.Lang.rb.getString("folderName_Chat_Log"));
    String folderDesc = "";
    String ownerHandle = cache.getUserRecord().handle;
    folderName.append(" : ");
    folderName.append(ownerHandle);
    if (chatWithContacts.length > 1) {
      for (int i=0; i<chatWithContacts.length; i++) {
        String name = ListRenderer.getRenderedText(chatWithContacts[i]);
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
      String otherName = ListRenderer.getRenderedText(chatWithContacts[0]);
      folderName.append(", ");
      folderName.append(otherName);
    }
    BASymmetricKey baSymmetricKey = new BASymmetricKey(32);

    // Create a folder share with the chatting partner.
    FolderShareRecord[] additionalShares = new FolderShareRecord[chatWithContacts.length];
    for (int i=0; i<chatWithContacts.length; i++) {
      additionalShares[i] = new FolderShareRecord();
      FolderShareRecord newShare = additionalShares[i];
      if (chatWithContacts[i].getMemberType() == Record.RECORD_TYPE_USER) {
        newShare.ownerUserId = ((ContactRecord) chatWithContacts[i]).contactWithId;
        newShare.ownerType = new Short(Record.RECORD_TYPE_USER);
      } else if (chatWithContacts[i].getMemberType() == Record.RECORD_TYPE_GROUP) {
        newShare.ownerUserId = ((FolderPair) chatWithContacts[i]).getId();
        newShare.ownerType = new Short(Record.RECORD_TYPE_GROUP);
      }
      newShare.canWrite = new Short(FolderShareRecord.YES);
      newShare.canDelete = new Short(FolderShareRecord.YES);
      newShare.setSymmetricKey(baSymmetricKey);
    }

    String myFolderName = folderName.toString();
    String shareFolderName = folderName.toString();
    /*
    if (chatWithContacts.length == 1) {
      UserRecord otherUserRec = cache.getUserRecord(chatWithContacts[0].contactWithId);
      if (otherUserRec != null) {
        myFolderName = otherUserRec.handle + " " + folderName;
      }
      shareFolderName = cache.getUserRecord().handle + " " + folderName;
    } else {
      myFolderName = "Multi-User " + folderName;
      shareFolderName = myFolderName;
    }
     */

    ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
    Fld_NewFld_Rq request = FolderOps.createNewFldRq(
        null,
        FolderRecord.CHATTING_FOLDER, // Chatting folder since July 2006
        myFolderName, folderDesc, shareFolderName, folderDesc,
        new Short(Move_NewFld_Dialog.DEFAULT_CHAT_PURGING_RECORD_NUM),
        new Integer(Move_NewFld_Dialog.DEFAULT_CHAT_PURGING_RECORD_SECONDS),
        baSymmetricKey,
        false,
        additionalShares,
        SIL
        );
    baSymmetricKey.clearContent();

    ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.FLD_Q_NEW_OR_GET_OLD, request), 60000);
    DefaultReplyRunner.nonThreadedRun(SIL, msgAction);
    if (msgAction != null && msgAction.getActionCode() == CommandCodes.FLD_A_GET_FOLDERS) {
      Fld_Folders_Rp reply = (Fld_Folders_Rp) msgAction.getMsgDataSet();
      FolderRecord folderRec = reply.folderRecords[0];
      // Shares might have came in any order, so just ask cache to get the right one.
      FolderShareRecord shareRec = cache.getFolderShareRecordMy(folderRec.folderId, true);
      chatPair = new FolderPair(shareRec, folderRec);
    }

    if (trace != null) trace.exit(ChatSessionCreator.class, chatPair);
    return chatPair;
  }

} // end class
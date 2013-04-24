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
package com.CH_cl.service.cache;

import com.CH_cl.service.records.InternetAddressRecord;
import com.CH_co.service.records.*;
import com.CH_co.util.Misc;
import java.io.File;
import java.util.*;

/**
*
* @author Marcin
*/
public class TextRenderer {

  public static String getRenderedText(Object value) {
    return getRenderedText(value, false, false, false, false, false, false, false);
  }

  public static String getRenderedText(Object value, boolean includeFileSizes, boolean includeFolderParticipants, boolean includeFolderOwner, boolean includeGroupOwner, boolean includeChatParticipants, boolean includeFullEmailAddress, boolean includeUploadPendingNote) {

    String label = null;

    if (value instanceof ContactRecord) {
      ContactRecord cRec = (ContactRecord) value;
      Long myUserId = FetchedDataCache.getSingleInstance().getMyUserId();
      if (myUserId != null)
        label = cRec.getNote(myUserId);
    }
    else if (value instanceof FileLinkRecord) {
      FileLinkRecord fLink = (FileLinkRecord) value;
      label = fLink.getFileName();
      if (includeFileSizes) {
        label += "   (" + Misc.getFormattedSize(fLink.origSize.longValue(), 4, 3) + (includeUploadPendingNote ? (fLink.isAborted() ? " Upload Aborded..." : (fLink.isIncomplete() ? " Upload Pending..." : "")) : "") + ")";
      } else if (includeUploadPendingNote) {
        label += "   " + (fLink.isAborted()? "(Upload Aborded)" : (fLink.isIncomplete() ? "(Upload Pending...)" : ""));
      }
    }
    else if (value instanceof FolderPair) {
      FolderPair fPair = (FolderPair) value;
      if (fPair.getFolderRecord().isChatting()) {
        if (includeChatParticipants)
          label = getFolderAndShareNames(fPair, true); // true for all participants
      } else if (fPair.getFolderRecord().isGroupType()) {
        if (includeGroupOwner)
          label = getFolderAndShareNames(fPair, false); // false for all members, owner only please
      } else {
        if (includeFolderParticipants)
          label = getFolderAndShareNames(fPair, true);
        else if (includeFolderOwner)
          label = getFolderAndShareNames(fPair, false);
      }
      if (label == null) {
        label = fPair.getMyName();
      }
    }
    else if (value instanceof MsgLinkRecord || value instanceof MsgDataRecord) {
      MsgDataRecord mData = null;
      if (value instanceof MsgLinkRecord) {
        MsgLinkRecord mLink = (MsgLinkRecord) value;
        mData = FetchedDataCache.getSingleInstance().getMsgDataRecord(mLink.msgId);
      } else {
        mData = (MsgDataRecord) value;
      }
      if (mData != null) {
        if (mData.isTypeAddress()) {
          if (mData.fileAs != null && mData.fileAs.length() > 0 && mData.fileAs.trim().length() > 0) {
            label = mData.fileAs.trim();
          } else if (mData.name != null && mData.name.length() > 0 && mData.name.trim().length() > 0) {
            label = mData.name.trim();
          }
          if (label != null) {
            if (includeFullEmailAddress && mData.email != null) {
              String personal = EmailRecord.getPersonal(mData.email);
              if (personal != null && personal.length() > 0)
                label = mData.email;
              else
                label += " <" + mData.email + ">";
            }
          } else {
            if (mData.email != null)
              label = mData.email.trim();
            else
              label = "(No name, Address ID " + mData.msgId + ")";
          }
        } else if (mData.isTypeMessage()) {
          String subject = mData.getSubject();
          if (subject == null || subject.length() == 0) {
            subject = "(No subject, Message ID " + mData.msgId + ")";
          }
          label = subject;
        }
        if (includeFileSizes && mData.recordSize != null)
          label += "   (" + Misc.getFormattedSize(mData.recordSize.intValue(), 4, 3) + ")";
      }
    }
    else if (value instanceof FolderShareRecord) {
      FolderShareRecord sRec = (FolderShareRecord) value;
      label = sRec.getFolderName();
    }
    else if (value instanceof FolderRecord) {
      FolderRecord fRec = (FolderRecord) value;
      FolderShareRecord sRec = FetchedDataCache.getSingleInstance().getFolderShareRecordMy(fRec.folderId, true);
      if (sRec != null) {
        label = sRec.getFolderName();
      } else if (!fRec.isGroupType()) {
        label = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("Folder_(FOLDER-ID)"), new Object[] {fRec.folderId});
      } else {
        label = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("Group_(GROUP-ID)"), new Object[] {fRec.folderId});
      }
    }
    else if (value instanceof UserRecord) {
      UserRecord uRec = (UserRecord) value;
      label = uRec.shortInfo();
    }
    else if (value instanceof String) {
      label = (String) value;
    }
    else if (value instanceof File) {
      File file = (File) value;
      label = file.getName();
    }
    else if (value instanceof InternetAddressRecord) {
      InternetAddressRecord eRec = (InternetAddressRecord) value;
      label = eRec.address;
    }
    else if (value instanceof EmailRecord) {
      EmailRecord eRec = (EmailRecord) value;
      label = eRec.getEmailAddressFull();
    }
    else if (value instanceof InvEmlRecord) {
      InvEmlRecord rec = (InvEmlRecord) value;
      label = rec.emailAddr;
    }

    return label;
  }

  public Collection getSearchableCharSequencesFor(Object searchableObj) {
    return getSearchableCharSequencesFor(searchableObj, true);
  }
  public Collection getSearchableCharSequencesFor(Object searchableObj, boolean includeMsgBody) {
    if (searchableObj instanceof Record)
      return getSearchTextFor((Record) searchableObj, includeMsgBody);
    else
      return null;
  }

  public static Collection getSearchTextFor(Record searchableObj, boolean includeMsgBody) {
    Collection sb = new LinkedList();
    if (searchableObj instanceof FileLinkRecord)
      sb = getSearchTextFor((FileLinkRecord) searchableObj, sb);
    else if (searchableObj instanceof MsgLinkRecord)
      sb = getSearchTextFor((MsgLinkRecord) searchableObj, includeMsgBody, sb);
    return sb;
  }

  private static Collection getSearchTextFor(FileLinkRecord fileLink, Collection sb) {
    sb.add(fileLink.getFileName());
    sb.add(fileLink.getFileType());
    return sb;
  }

  private static Collection getSearchTextFor(MsgLinkRecord msgLink, boolean includeMsgBody, Collection sb) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    MsgDataRecord msgData = cache.getMsgDataRecord(msgLink.msgId);
    if (msgData != null) {
      // searching chat msgs should also include attachment listing -- use cached string
      if (includeMsgBody && msgLink.getPostRenderingCache() != null)
        sb.add(msgLink.getPostRenderingCache());
      else {
        sb.add(msgData.getSubject());
        if (includeMsgBody) {
          String text = msgData.getText();
          if (text != null)
            sb.add(text);
        }
      }

      String fromEmailAddress = msgData.getFromEmailAddress();
      if (msgData.isEmail() || fromEmailAddress != null) {
        sb.add(TextRenderer.getRenderedText(CacheEmlUtils.convertToFamiliarEmailRecord(fromEmailAddress)));
        sb.add(fromEmailAddress); // also include the email address instead of only the converted Address Contact
      } else {
        // use my contact list only, not the reciprocal contacts
        Record fromAsFamiliar = CacheUsrUtils.convertUserIdToFamiliarUser(msgData.senderUserId, true, false);
        Record fromUser = cache.getUserRecord(msgData.senderUserId);
        if (fromAsFamiliar != null)
          sb.add(TextRenderer.getRenderedText(fromAsFamiliar));
        if (fromUser != null)
          sb.add(TextRenderer.getRenderedText(fromUser));
      }

      Record[][] recipients = CacheMsgUtils.gatherAllMsgRecipients(msgData.getRecipients());
      for (int i=0; i<recipients.length; i++)
        for (int k=0; k<recipients[i].length; k++)
          if (recipients[i][k] != null && !(recipients[i][k] instanceof FolderPair) && !(recipients[i][k] instanceof FolderRecord) && !(recipients[i][k] instanceof FolderShareRecord))
            sb.add(TextRenderer.getRenderedText(recipients[i][k]));
    }
    return sb;
  }

  /**
  * Overwrite the method from super class to get the string for the label.
  * @return string for each node label
  */
  public static String convertValueToText(FolderPair folderPair, StringBuffer toolTipReturn) {
    String nodeText = "";
    FolderRecord fRec = folderPair.getFolderRecord();
    nodeText = fRec.getCachedDisplayText();

    String toolTip = fRec.getCachedToolTip();

    String ownerNote = null;
    String chatNote = null;
    String appendPostfix = null;

    if (nodeText == null || (toolTip == null && toolTipReturn != null)) {
      String[] notes = getOwnerAndChatNote(fRec);
      ownerNote = notes[0];
      chatNote = notes[1];
    }

    if (toolTip == null && toolTipReturn != null) {
      // Here and not in the renderer is the proper place to set tool tip!  I don't know why is that...
      if (ownerNote.length() > 0 && chatNote.length() == 0) {
        toolTip = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("folderTip_The_user_USER_is_the_primary_owner_of_this_folder."), new Object[] {ownerNote});
      } else if (ownerNote.length() == 0 && chatNote.length() > 0) {
        toolTip = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("folderTip_You_are_sharing_this_chatting_folder_with_USER."), new Object[] {chatNote});
      } else if (ownerNote.length() > 0 && chatNote.length() > 0) {
        toolTip = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("folderTip_The_user_USER_is_the_primary_owner_of_this_chatting_folder._Other_participants_are_OTHER-USERS."), new Object[] {ownerNote, chatNote});
      } else {
        toolTip = "";
      }
      fRec.setCachedToolTip(toolTip == null ? "" : toolTip);
    }
    if (toolTipReturn != null) {
      toolTipReturn.append(toolTip != null && toolTip.length() > 0 ? toolTip : "");
    }

    if (nodeText == null) {

      String ownerAndChatNotes = ownerNote.length() > 0 ? ownerNote + " " + chatNote : chatNote;
      String folderName = folderPair.getFolderShareRecord().getFolderName();

      // For Chatting folders, to save space, don't display the default "Chat Log" name.
      if (folderPair.getFolderRecord().isChattingLive()) {
        String defaultChatFolderName = com.CH_cl.lang.Lang.rb.getString("folderName_Chat_Log");
        String fName = folderName != null ? folderName : "";
        if (fName.startsWith(defaultChatFolderName)) {
          if (ownerAndChatNotes.length() > 0) {
            nodeText = "";
            appendPostfix = "chat";
          } else {
            nodeText = "Chat...";
          }
        } else if (ownerAndChatNotes.length() == 0) {
          // not yet fetched live chatting shares
          nodeText = "" + folderName + "...";
        }
      }

      if (nodeText == null)
        nodeText = folderName;
      if (nodeText == null)
        nodeText = "";

      StringBuffer updateNoteSB = new StringBuffer();
      int updateCount = fRec.getUpdateCount();
      if (updateCount > 0) {
        updateNoteSB.append("(");
        updateNoteSB.append(updateCount);
        updateNoteSB.append(")");
      }

      if (ownerAndChatNotes.length() > 0 || updateNoteSB.length() > 0) {
        String text = nodeText;

        StringBuffer newNameSB = new StringBuffer();
        if (updateNoteSB.length() > 0) {
          boolean isSpamFolder = fRec.folderId.equals(FetchedDataCache.getSingleInstance().getUserRecord().junkFolderId);
          boolean isRecycleFolder = fRec.isRecycleType();
          boolean isHTML = false;
          // skip BOLD for Spam folder
          if (!isSpamFolder && !isRecycleFolder) {
            newNameSB.append("<html><b>"); // string "<html><b>" is in determination is renderer should make this bold... skip closing tags!
            isHTML = true;
          }
          newNameSB.append(text);
          if (!isRecycleFolder) {
            newNameSB.append(' ');
            newNameSB.append(updateNoteSB);
          }
          if (ownerAndChatNotes.length() > 0) {
            if (isHTML) {
              //newNameSB.append("</b>"); -- skipping closing tag for renderer
            }
            newNameSB.append(" : ");
            newNameSB.append(ownerAndChatNotes);
            if (isHTML) {
              //newNameSB.append("</html>"); -- skipping closing tag for renderer
            }
          } else {
            if (isHTML) {
              //newNameSB.append("</b></html>"); -- skipping closing tag for renderer
            }
          }
        } else if (ownerAndChatNotes.length() > 0) {
          if (text.length() > 0) {
            newNameSB.append(text);
            newNameSB.append(" : ");
          }
          newNameSB.append(ownerAndChatNotes);
        } else {
          newNameSB.append(text);
        }

        nodeText = newNameSB.toString();
      }
      if (appendPostfix != null) {
        if (!nodeText.endsWith(" "))
          nodeText += " ";
        nodeText += appendPostfix;
      }
      // only when folder name has been retrieved and decrypted we know the proper display text
      if (folderName != null)
        fRec.setCachedDisplayText(nodeText);
    }

    return nodeText;
  }

  /**
  * @return name of the folder owner and all participants in a String[2] array with first being the owner.
  */
  public static String[] getOwnerAndChatNote(FolderRecord fRec) {
    String[] rc = null;
    if (fRec != null)
      rc = getFolderNote(fRec, fRec.isChatting());
    return rc;
  }
  /**
  * @return name of the folder owner and all participants in a String[2] array with first being the owner.
  */
  private static String[] getFolderNote(FolderRecord fRec, boolean includeAllParticipants) {
    FetchedDataCache cache = null;
    Long myUserId = null;
    Long ownerUserId = null;
    String ownerNote = fRec.getCachedOwnerNote();
    String chatNote = fRec.getCachedChatNote();
    String rcChatNote = "";

    if (ownerNote == null || chatNote == null) {
      cache = FetchedDataCache.getSingleInstance();
      myUserId = cache.getMyUserId();
      ownerUserId = fRec.ownerUserId;
    }

    if (ownerNote == null) {
      // If folder is not yours show whose it is.
      if (!ownerUserId.equals(myUserId)) {
        StringBuffer sb = new StringBuffer(32);
        // use my contact list only, not the reciprocal contacts
        Record rec = CacheUsrUtils.convertUserIdToFamiliarUser(ownerUserId, true, false);
        if (rec != null) {
          sb.append('[');
          sb.append(getRenderedText(rec));
          sb.append(']');
        }
        else {
          sb.append("[*]");
        }
        ownerNote = sb.toString();
      } else {
        ownerNote = "";
      }
      fRec.setCachedOwnerNote(ownerNote);
    }

    // If folder is a chatting folder, show other participants.
    // Also show other participants for table frames.
    if (includeAllParticipants) {
      if (chatNote == null) {
        StringBuffer sb = new StringBuffer(32);
        FolderShareRecord[] allShares = cache.getFolderShareRecordsForFolder(fRec.folderId);
        if (allShares != null) {
          boolean foundMine = false;
          ArrayList userL = null;
          ArrayList uidL = null;
          ArrayList groupL = null;
          ArrayList gidL = null;
          for (int i=0; i<allShares.length; i++) {
            FolderShareRecord share = allShares[i];
            if (share.isOwnedByGroup() ||
                (!share.isOwnedBy(ownerUserId, (Long[]) null) &&
                !share.isOwnedBy(myUserId, (Long[]) null))) {
              Record recipient = null;
              if (share.isOwnedByUser()) {
                if (userL == null) userL = new ArrayList();
                // use my contact list only, not the reciprocal contacts
                recipient = CacheUsrUtils.convertUserIdToFamiliarUser(share.ownerUserId, true, false);
                if (recipient != null)
                  userL.add(recipient);
                else {
                  if (uidL == null) uidL = new ArrayList();
                  uidL.add(share.ownerUserId);
                }
              } else {
                if (groupL == null) groupL = new ArrayList();
                recipient = FetchedDataCache.getSingleInstance().getFolderRecord(share.ownerUserId);
                if (recipient != null)
                  groupL.add(recipient);
                else {
                  if (gidL == null) gidL = new ArrayList();
                  gidL.add(share.ownerUserId);
                }
              }
            } else if (share.isOwnedBy(myUserId, (Long[]) null)) {
              foundMine = true;
            }
          }
          Comparator compare = new Comparator() {
            public int compare(Object o1, Object o2) {
              String s1 = getRenderedText(o1);
              String s2 = getRenderedText(o2);
              if (s1 == null && s2 == null)
                return 0;
              else if (s1 == null)
                return -1;
              else if (s2 == null)
                return +1;
              return s1.compareTo(s2);
            }
          };
          ArrayList recipientsL = new ArrayList();
          boolean appended = false;
          if (groupL != null) {
            Collections.sort(groupL, compare);
            recipientsL.addAll(groupL);
          }
          if (userL != null) {
            Collections.sort(userL, compare);
            recipientsL.addAll(userL);
          }
          for (int i=0; i<recipientsL.size(); i++) {
            Record recipient = (Record) recipientsL.get(i);
            if (appended) sb.append(" / ");
            sb.append(getRenderedText(recipient));
            appended = true;
          }
          for (int i=0; gidL != null && i<gidL.size(); i++) {
            if (appended) sb.append(" / ");
            sb.append(java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("Group_(GROUP-ID)"), new Object[] {gidL.get(i)}));
            appended = true;
          }
          for (int i=0; uidL != null && i<uidL.size(); i++) {
            if (appended) sb.append(" / ");
            sb.append(java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("User_(USER-ID)"), new Object[] {uidL.get(i)}));
            appended = true;
          }

//          if (allShares.length > 1 && foundMine) {
//            if (appended)
//              sb.append(" / ");
//            sb.append("me");
//          }
        }
        chatNote = sb.toString();
        if (chatNote == null) chatNote = "";
        fRec.setCachedChatNote(chatNote);
      }
      rcChatNote = chatNote;
    }

    return new String[] {ownerNote, rcChatNote};
  }

  /**
  * @return name of the folder with participants for display.
  */
  public static String getFolderAndShareNames(FolderPair fPair, boolean includeAllParticipants) {
    FolderRecord fRec = fPair.getFolderRecord();
    String[] notes = getFolderNote(fRec, includeAllParticipants);
    String additionalNote = notes[0].length() > 0 ? notes[0] + " " + notes[1] : notes[1];
    additionalNote = additionalNote.trim();
    String title = fPair.getMyName();
    String appendPostfix = null;
    if (title == null) title = "";
    if (additionalNote.length() > 0) {
      if (fRec.isChatting()) {
        String defaultChatFolderName = com.CH_cl.lang.Lang.rb.getString("folderName_Chat_Log");
        if (title.startsWith(defaultChatFolderName)) {
          title = "";
          appendPostfix = " chat";
        }
      }
      title = title.length() > 0 ? title + " : " + additionalNote : additionalNote;
    }
    if (appendPostfix != null)
      title += appendPostfix;
    return title;
  }

  public static String getFolderAndShareNamesForTreeDisplaySort(FolderPair fPair) {
    return getFolderAndShareNames(fPair, fPair.getFolderRecord().isChatting());
  }
}
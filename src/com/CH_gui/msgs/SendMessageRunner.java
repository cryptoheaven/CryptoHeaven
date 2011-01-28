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

package com.CH_gui.msgs;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.ops.*;
import com.CH_cl.service.actions.*;
import com.CH_cl.service.actions.msg.*;
import com.CH_cl.service.actions.sys.*;
import com.CH_cl.service.records.*;

import com.CH_co.cryptx.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.file.*;
import com.CH_co.service.msg.dataSets.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.frame.MainFrame;
import com.CH_gui.util.MessageDialog;

import java.awt.*;
import java.io.*;
import java.security.*;
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
 * <b>$Revision: 1.19 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class SendMessageRunner extends ThreadTraced {

  private static final short TO = MsgLinkRecord.RECIPIENT_TYPE_TO;
  private static final short CC = MsgLinkRecord.RECIPIENT_TYPE_CC;
  private static final short BCC = MsgLinkRecord.RECIPIENT_TYPE_BCC;

  private MsgSendInfoProviderI msgSendInfoProvider;

  private Record[][] selectedRecipients;
  private Object[] selectedAttachments;
  private FileLinkRecord[] selectedFileAttachments;
  private MsgLinkRecord[] selectedMsgAndPostAttachments;
  private File[] selectedLocalFileAttachments;


  /** Creates new SendMessageRunner */
  public SendMessageRunner(MsgSendInfoProviderI msgSendInfoProvider) {
    super("Send Message Runner");
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SendMessageRunner.class, "SendMessageRunner(MsgSendInfoProviderI msgSendInfoProvider)");
    if (trace != null) trace.args(msgSendInfoProvider);

    this.msgSendInfoProvider = msgSendInfoProvider;

    selectedRecipients = msgSendInfoProvider.getSelectedRecipients();
    selectedAttachments = msgSendInfoProvider.getSelectedAttachments();

    if (trace != null) trace.data(40, selectedRecipients);
    if (trace != null) trace.data(41, selectedAttachments);

    selectedFileAttachments = (FileLinkRecord[]) ArrayUtils.gatherAllOfType(selectedAttachments, FileLinkRecord.class);
    selectedMsgAndPostAttachments = (MsgLinkRecord[]) ArrayUtils.gatherAllOfType(selectedAttachments, MsgLinkRecord.class);
    selectedLocalFileAttachments = (File[]) ArrayUtils.gatherAllOfType(selectedAttachments, File.class);

    if (trace != null) trace.data(50, selectedFileAttachments);
    if (trace != null) trace.data(51, selectedMsgAndPostAttachments);
    if (trace != null) trace.data(52, selectedLocalFileAttachments);

    setDaemon(true);

    if (trace != null) trace.exit(SendMessageRunner.class);
  }


  public void runTraced() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SendMessageRunner.class, "SendMessageRunner.runTraced()");

    ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
    FetchedDataCache cache = SIL.getFetchedDataCache();

    msgSendInfoProvider.setSendMessageInProgress(true);

    try {
      UserRecord myUserRecord = cache.getUserRecord();

      boolean isSavingAsDraft = msgSendInfoProvider.isSavingAsDraft();
      if (!isSavingAsDraft && msgSendInfoProvider.isCopyToOutgoing()) {
        selectedRecipients[BCC] = addOutgoingToRecipients(selectedRecipients[BCC]);
      }

      // remove recipients that are repeated in lower level recipient type
      // For example, if user is TO, it shouldn't be CC or BCC
      for (int i=0; i<selectedRecipients.length-1; i++) {
        for (int k=i+1; k<selectedRecipients.length; k++) {
          if (selectedRecipients[k] != null && selectedRecipients[k].length > 0 &&
              selectedRecipients[i] != null && selectedRecipients[i].length > 0)
            selectedRecipients[k] = (Record[]) ArrayUtils.getDifference(selectedRecipients[k], selectedRecipients[i]);
        }
      }

      Record[] recipientsAll = null;
      for (int i=0; i<selectedRecipients.length; i++) {
        if (selectedRecipients[i] != null)
          recipientsAll = (Record[]) RecordUtils.concatinate(recipientsAll, selectedRecipients[i]);
      }
      recipientsAll = (Record[]) ArrayUtils.removeDuplicates(recipientsAll);

      Long[] shareIDs = null;
      Long[] contactIDs = null;

      if (!isSavingAsDraft) {
        // Gather all recipient posting boards.
        FolderPair[] folderPairs = (FolderPair[]) ArrayUtils.gatherAllOfType(recipientsAll, FolderPair.class);
        shareIDs = FolderPair.getShareIDs(folderPairs);

        // Gather all involved contact records.
        ContactRecord[] contactRecords = (ContactRecord[]) ArrayUtils.gatherAllOfType(recipientsAll, ContactRecord.class);
        // Add all active contacts if present for each user record selected.
        UserRecord[] userRecords = (UserRecord[]) ArrayUtils.gatherAllOfType(recipientsAll, UserRecord.class);
        for (int i=0; i<userRecords.length; i++) {
          ContactRecord cRec = cache.getContactRecordOwnerWith(cache.getMyUserId(), userRecords[i].userId);
          // If we have an active contact for some user, add it too.
          if (cRec != null && cRec.isOfActiveType() && ArrayUtils.find(contactRecords, cRec) < 0)
            contactRecords = (ContactRecord[]) ArrayUtils.concatinate(contactRecords, new ContactRecord[] { cRec });
        }
        contactIDs = RecordUtils.getIDs(contactRecords);
      }

      // message encryption key
      BASymmetricKey symmetricKey = new BASymmetricKey(32);
      BASymmetricKey symmetricAttachmentsKey = new BASymmetricKey(symmetricKey);

      {
        String password = msgSendInfoProvider.getPassword();
        Hasher.Set bodyKey = password != null ? new Hasher.Set(password.toCharArray()) : null;
        if (bodyKey != null)
          symmetricAttachmentsKey.XOR(bodyKey.encodedPassword);
      }

      StringBuffer recipientsSB = null;
      if (isSavingAsDraft) {
        recipientsSB = new StringBuffer();
        // Mark FROM address if specified email address
        Record fromAccount = msgSendInfoProvider.getFromAccount();
        if (fromAccount instanceof EmailRecord) {
          EmailRecord fromEmlRec = (EmailRecord) fromAccount;
          recipientsSB.append(MsgDataRecord.RECIPIENT_FROM_EMAIL + " " + Misc.escapeWhiteEncode(fromEmlRec.getEmailAddressFull()) + " ");
        }
      }
      Component parent = msgSendInfoProvider instanceof Component ? (Component) msgSendInfoProvider : (Component) null;
      // create Msg Link Records
      Object[] errorBuffer = new Object[1];
      MsgLinkRecord[] linkRecords = prepareMsgLinkRecords(selectedRecipients, symmetricKey, isSavingAsDraft, recipientsSB, parent, errorBuffer);

      if (errorBuffer[0] == null) {

        MsgDataRecord dataRecord = null;
        Long[] fromMsgLinkIDs = null;
        Long[] fromShareIDs = null;
        MsgLinkRecord[] msgAttachments = null;
        FileLinkRecord[] fileAttachments = null;

        if (linkRecords != null && linkRecords.length > 0) {
          // create Msg Data Record
          dataRecord = prepareMsgDataRecord(symmetricKey);
          if (isSavingAsDraft) {
            dataRecord.setRecipients(recipientsSB.toString());
            shareIDs = new Long[] { cache.getFolderShareRecordMy(myUserRecord.draftFolderId, false).shareId };
          }

          // msgLink id parent array from which attachments are taken
          ArrayList fromMsgLinkIDsL = new ArrayList();
          // share id array from which the attachments are taken and where parent attachment source messages reside
          ArrayList fromShareIDsL = new ArrayList();

          // create message attachments
          if (selectedMsgAndPostAttachments != null && selectedMsgAndPostAttachments.length > 0) {
            msgAttachments = prepareMsgAttachments(selectedMsgAndPostAttachments, symmetricAttachmentsKey, fromMsgLinkIDsL, fromShareIDsL);
          }

          // create file attachments
          if (selectedFileAttachments != null && selectedFileAttachments.length > 0) {
            fileAttachments = prepareFileAttachments(selectedFileAttachments, symmetricAttachmentsKey, fromMsgLinkIDsL, fromShareIDsL);
          }

          // convert IDs vector to array of IDs
          if (fromMsgLinkIDsL.size() > 0) {
            fromMsgLinkIDs = new Long[fromMsgLinkIDsL.size()];
            fromMsgLinkIDsL.toArray(fromMsgLinkIDs);
          }
          if (fromShareIDsL.size() > 0) {
            fromShareIDs = new Long[fromShareIDsL.size()];
            fromShareIDsL.toArray(fromShareIDs);
          }
        }

        // Set regular email request.
        Obj_List_Co emailRequest = null;
        boolean regularEmailWithAttachments = false;
        if (!isSavingAsDraft) {
          // Gather all recipient email addresses.
          EmailAddressRecord[] emailAddresses = (EmailAddressRecord[]) ArrayUtils.gatherAllOfType(recipientsAll, EmailAddressRecord.class);
          if (emailAddresses != null && emailAddresses.length > 0) {
            // Check if any attachments, if so then skip them for external recipients.
            if (selectedAttachments != null && selectedAttachments.length > 0) {
              regularEmailWithAttachments = true;
            }

            String[] addresses = new String[emailAddresses.length];
            Short[] recipientTypes = new Short[emailAddresses.length];
            for (int i=0; i<emailAddresses.length; i++) {
              addresses[i] = emailAddresses[i].address;
              if (ArrayUtils.find(selectedRecipients[TO], emailAddresses[i]) >= 0) {
                recipientTypes[i] = new Short(TO);
              } else if (ArrayUtils.find(selectedRecipients[CC], emailAddresses[i]) >= 0) {
                recipientTypes[i] = new Short(CC);
              } else {
                recipientTypes[i] = new Short(BCC);
              }
            }
            emailRequest = new Obj_List_Co();
            String subject = null;
            String body = null;
            String contentType = null;
            Short priority = null;

            // only include reqular email content if no attachments, else stuff will be created from inserted email structure
            if (!regularEmailWithAttachments) {
              String[] content = msgSendInfoProvider.getContent();
              subject = content[0];
              body = content[1];
              short msgType = msgSendInfoProvider.getContentMode();
              if (msgType == MsgComposePanel.CONTENT_MODE_MAIL_HTML) {
                contentType = "text/html";
              } else {
                contentType = "text/plain";
              }
              priority = new Short(msgSendInfoProvider.getPriority());
            }

            emailRequest.objs = new Object[] { addresses, subject, contentType, body, recipientTypes, priority };
          } else {
            // Send plain subject so that recipient can be notified that he has a new message about so-and-so.
            if (Misc.isBitSet(myUserRecord.notifyByEmail, UserRecord.EMAIL_NOTIFY_INCLUDE_SUBJECT_AND_FROM_ADDRESS)) {
              emailRequest = new Obj_List_Co();
              emailRequest.objs = new Object[] { null, msgSendInfoProvider.getContent()[0], null, null, null, null };
            }
          }
        }

        // new Message request
        Record fromAccount = msgSendInfoProvider.getFromAccount();
        Msg_New_Rq newMsgRequest = new Msg_New_Rq(fromAccount, shareIDs, contactIDs, linkRecords, dataRecord, fromMsgLinkIDs, fromShareIDs, msgAttachments, fileAttachments);
        newMsgRequest.replyToMsgLink = msgSendInfoProvider.getReplyToMsgLink();
        newMsgRequest.emailRequest = emailRequest;
        newMsgRequest.hashes = prepareAddrHashes(dataRecord);

        // Send the message key if regular email with attachments should be created.
        if (regularEmailWithAttachments) {
          newMsgRequest.symmetricKey = symmetricKey;
        }
        MessageAction sendMsgAction = new MessageAction(CommandCodes.MSG_Q_NEW, newMsgRequest);
        Class replyClass = null;

        if (trace != null) trace.data(80, "about to send new message request");

        // Create local file attachments only when there are some recipients selected other than external email.
        if (linkRecords != null && linkRecords.length > 0 &&
            selectedLocalFileAttachments != null && selectedLocalFileAttachments.length > 0) {
          File_NewFiles_Rq localFileRequest = new File_NewFiles_Rq();
          newMsgRequest.localFiles = localFileRequest;
          // skip the owner ID and owner type, threaded run=false
          replyClass = UploadUtilities.runUploadFileBunch(
                            selectedLocalFileAttachments, sendMsgAction, localFileRequest,
                            symmetricAttachmentsKey, (Long) null, (Short) null, false, SIL);
          if (trace != null) trace.data(90, "reply class", replyClass);
        }
        // no local files attached
        else {
          if (trace != null) trace.data(100, "send the new message request");
          // send the new message request
          ClientMessageAction reply = SIL.submitAndFetchReply(sendMsgAction, 5*60000);
          if (trace != null) trace.data(110, "got back reply from new message request");
          // run reply...
          if (reply != null) {
            DefaultReplyRunner.nonThreadedRun(SIL, reply);
            replyClass = reply.getClass();
            if (trace != null) trace.data(115, "reply class", replyClass);
          }
        }

        if (trace != null) trace.data(120, "about to invokeLater() some GUI cleanup");

        // if all ok, close send dialog
        if (MsgAGet.class.equals(replyClass) || SysANoop.class.equals(replyClass)) {
          msgSendInfoProvider.messageSentNotify();
        } // end if all ok, close send dialog
      } // end if !error
    } catch (Throwable t) {
      if (trace != null) trace.exception(SendMessageRunner.class, 200, t);
    }

    msgSendInfoProvider.setSendMessageInProgress(false);

    if (trace != null) trace.exit(SendMessageRunner.class);
  } // end run()


  /**
   * Adjusts the recipient list to include the CC is selected.
   * Call only when CC checkbox is selected.
   */
  private static Record[] addOutgoingToRecipients(Record[] recipients) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SendMessageRunner.class, "addOutgoingToRecipients(Record[] recipients)");
    if (trace != null) trace.args(recipients);

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    // if CC: to Outgoing folder, then prepare the sentPair FolderPair and add it to recipients (if not already selected)
    Long sentFolderId = cache.getUserRecord().sentFolderId;
    if (RecordUtils.find(recipients, sentFolderId) == null) {
      FolderShareRecord shareRecord = cache.getFolderShareRecordMy(sentFolderId, false);
      FolderRecord folderRecord = cache.getFolderRecord(sentFolderId);
      FolderPair sentPair = new FolderPair(shareRecord, folderRecord);
      LinkedList list = new LinkedList(Arrays.asList(recipients));
      list.addLast(sentPair);

      recipients = new Record[list.size()];
      list.toArray(recipients);
    }

    if (trace != null) trace.exit(SendMessageRunner.class, recipients);
    return recipients;
  }

  public static Obj_List_Co prepareAddrHashes(MsgDataRecord dataRecord) {
    Obj_List_Co hashSet = null;
    if (dataRecord != null && dataRecord.objType.shortValue() == MsgDataRecord.OBJ_TYPE_ADDR) {
      String text = dataRecord.getText();
      int emlStart = text.indexOf("<Emails"); // no closing bracket due to following attributes
      int emlEnd = text.lastIndexOf("</Emails>");
      if (emlStart >= 0 && emlEnd >= 0 && emlEnd > emlStart) {
        String emails = text.substring(emlStart, emlEnd);
        String[] addresses = EmailRecord.gatherAddresses(emails, true);
        if (addresses != null) {
          Object[] hashes = new Object[addresses.length];
          MessageDigest messageDigest = null;
          try {
            messageDigest = MessageDigest.getInstance("MD5");
          } catch (NoSuchAlgorithmException e) {
          }
          for (int i=0; i<addresses.length; i++) {
            addresses[i] = addresses[i].trim().toLowerCase(Locale.US);
            hashes[i] = messageDigest.digest(addresses[i].getBytes());
          }
          hashSet = new Obj_List_Co(hashes);
        }
      }
    }
    return hashSet;
  }

  public static MsgLinkRecord[] prepareMsgLinkRecords(Record[] recipientsTO, BASymmetricKey symmetricKey, Component parent) {
    return prepareMsgLinkRecords(new Record[][] { recipientsTO }, symmetricKey, false, null, parent, null);
  }
  /**
   * Prepares links for sending...
   * @param isSavingAsDraft If in draft mode then use user's default draft folder as recipient, and output original recipient list in the StringBuffer
   * @param recipientsSB Buffer for original recipient list when in draft mode
   */
  public static MsgLinkRecord[] prepareMsgLinkRecords(Record[][] recipientsAll, BASymmetricKey symmetricKey, boolean isSavingAsDraft, StringBuffer recipientsSB, Component parent, Object[] errorBuffer) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SendMessageRunner.class, "prepareMsgLinkRecords(Record[][] recipientsAll, Record[] recipientsCC, BASymmetricKey symmetricKey, boolean isSavingAsDraft, StringBuffer recipientsSB, Object[] errorBuffer)");
    if (trace != null) trace.args(recipientsAll, symmetricKey);
    if (trace != null) trace.args(isSavingAsDraft);
    if (trace != null) trace.args(recipientsSB);

    // create new Msg Link Records
    ArrayList linkRecordsL = new ArrayList();

    ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
    FetchedDataCache cache = SIL.getFetchedDataCache();

    if (isSavingAsDraft) {
      // gather the recipient list
      recipientsSB.append(MsgPanelUtils.gatherAllMsgRecipients(recipientsAll));
      // switch the recipients to the draft folder only
      FolderPair draftPair = FolderOps.getOrCreateDraftFolder(SIL);
      recipientsAll = new Record[][] { new FolderPair[] { draftPair } };
    }

    Record[] recipients = null;
    for (int pass=0; pass<2; pass++) {
      // pass 0 is fetch public keys
      // pass 1 is seal
      int FETCHING_KEYS_PASS = 0;
      int SEALING_MSGS_PASS = 1;
      ArrayList userIDsWeNeedKeysL = new ArrayList();
      for (int recipientType=0; recipientType<recipientsAll.length; recipientType++) {
        recipients = recipientsAll[recipientType];
        for (int i=0; recipients!=null && i<recipients.length; i++) {
          MsgLinkRecord linkRec = null;
          if (pass == SEALING_MSGS_PASS) {
            linkRec = new MsgLinkRecord();
            linkRec.setSymmetricKey(symmetricKey);
            linkRec.status = new Short((short) recipientType);
          }

          boolean toAdd = false;
          Long toUserId = null;
          Record recipient = recipients[i];

          if (recipient instanceof UserRecord) {
            UserRecord uRec = (UserRecord) recipients[i];
            toUserId = uRec.userId;
          }
          else if (recipient instanceof ContactRecord) {
            ContactRecord cRec = (ContactRecord) recipients[i];
            toUserId = cRec.contactWithId;
          }
          // prepare Msg Link to reside in a folder
          else if (pass == SEALING_MSGS_PASS && recipients[i] instanceof FolderPair) {
            FolderPair fPair = (FolderPair) recipients[i];
            linkRec.ownerObjType = new Short(Record.RECORD_TYPE_FOLDER);
            linkRec.ownerObjId = fPair.getId();
            linkRec.seal(fPair.getFolderShareRecord().getSymmetricKey());
            toAdd = true;
          }
          // prepare Msg Link to be sent to a user
          if (toUserId != null) {
            KeyRecord kRec = cache.getKeyRecordForUser(toUserId);
            if (pass == FETCHING_KEYS_PASS && kRec == null && !userIDsWeNeedKeysL.contains(toUserId)) {
              userIDsWeNeedKeysL.add(toUserId);
            } else if (pass == SEALING_MSGS_PASS) {
              linkRec.seal(kRec);
              toAdd = true;
            }
          } // end if
          if (pass == SEALING_MSGS_PASS && toAdd) {
            linkRecordsL.add(linkRec);
          }
        } // end for
      }
      if (pass == FETCHING_KEYS_PASS && userIDsWeNeedKeysL.size() > 0) {
        Long[] uIDs = new Long[userIDsWeNeedKeysL.size()];
        userIDsWeNeedKeysL.toArray(uIDs);
        SIL.submitAndWait(new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_USERS, new Obj_IDList_Co(uIDs)), 60000);
        ArrayList missingKeysUIDsL = new ArrayList();
        for (int i=0; i<userIDsWeNeedKeysL.size(); i++) {
          Long uId = (Long) userIDsWeNeedKeysL.get(i);
          if (cache.getKeyRecordForUser(uId) == null)
            missingKeysUIDsL.add(uId);
        }
        if (missingKeysUIDsL.size() > 0) {
          StringBuffer sb = new StringBuffer("Message was not sent.  Encryption key could not be found for the following user IDs:\n\n");
          for (int i=0; i<missingKeysUIDsL.size(); i++) {
            sb.append(missingKeysUIDsL.get(i));
            sb.append("\n");
          }
          sb.append("\nPlease check your recipients list.");
          MessageDialog.showErrorDialog(parent, sb.toString(), "Message not sent", true);
          if (errorBuffer != null) errorBuffer[0] = "error";
          break;
        }
      }
    } // end for 2 passes

    MsgLinkRecord[] mLinkRecs = new MsgLinkRecord[linkRecordsL.size()];
    if (linkRecordsL.size() > 0) {
      linkRecordsL.toArray(mLinkRecs);
    }

    if (trace != null) trace.exit(SendMessageRunner.class, mLinkRecs);
    return mLinkRecs;
  }


  public static MsgDataRecord prepareMsgDataRecord(BASymmetricKey symmetricKey, Short importance, String subject, String body, String password) {
    return prepareMsgDataRecord(symmetricKey, importance, new Short(MsgDataRecord.OBJ_TYPE_MSG), subject, body, password);
  }
  public static MsgDataRecord prepareMsgDataRecord(BASymmetricKey symmetricKey, Short importance, Short objType, String subject, String body, String password) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SendMessageRunner.class, "prepareMsgDataRecord(BASymmetricKey symmetricKey, Short importance, String subject, String body, String password)");
    if (trace != null) trace.args(symmetricKey, importance, subject, body, password);
    MsgDataRecord dataRecord = new MsgDataRecord();
    dataRecord.importance = importance;
    dataRecord.objType = objType;
    if (subject != null) {
      dataRecord.setSubject(subject);
    } else {
      dataRecord.setSubject("");
    }
    dataRecord.setTextBody(body);
    Hasher.Set bodyKey = password != null ? new Hasher.Set(password.toCharArray()) : null;
    dataRecord.seal(symmetricKey, bodyKey, FetchedDataCache.getSingleInstance().getKeyRecordMyCurrent());
    if (trace != null) trace.exit(SendMessageRunner.class, dataRecord);
    return dataRecord;
  }
  private MsgDataRecord prepareMsgDataRecord(BASymmetricKey symmetricKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SendMessageRunner.class, "prepareMsgAttachments(BASymmetricKey symmetricKey)");
    if (trace != null) trace.args(symmetricKey);
    short msgPriority = msgSendInfoProvider.getPriority();
    Short objType = msgSendInfoProvider.getContentType();
    String[] content = msgSendInfoProvider.getContent();
    String question = msgSendInfoProvider.getQuestion();
    String password = msgSendInfoProvider.getPassword();
    MsgDataRecord dataRecord = prepareMsgDataRecord(symmetricKey, new Short(msgPriority), objType, content[0], content[1], password);
    dataRecord.bodyPassHint = question == null || question.trim().length() == 0 ? null : question.trim();
    dataRecord.dateExpired = msgSendInfoProvider.getExpiry();
    if (trace != null) trace.exit(SendMessageRunner.class, dataRecord);
    return dataRecord;
  }


  /**
   * Clone and encrypt message links to create message attachments links.
   * Fills the share ID vector with involved shares from where the attachments are taken.
   */
  private static MsgLinkRecord[] prepareMsgAttachments(MsgLinkRecord[] selectedMsgLinks, BASymmetricKey symmetricKey, ArrayList fromMsgLinkIDsV, ArrayList fromShareIDsV) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SendMessageRunner.class, "prepareMsgAttachments(MsgLinkRecord[] selectedMsgLinks, BASymmetricKey symmetricKey, ArrayList fromMsgLinkIDsL, ArrayList fromShareIDsL)");
    if (trace != null) trace.args(selectedMsgLinks, symmetricKey, fromMsgLinkIDsV, fromShareIDsV);

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    MsgLinkRecord[] msgAttachments = (MsgLinkRecord[]) RecordUtils.cloneRecords(selectedMsgLinks);
    for (int i=0; i<msgAttachments.length; i++) {
      MsgLinkRecord mLink = msgAttachments[i];
      // check if we have access to the message's content, if not, leave it in recryption pending state for the original user recipient to change from asymetric to symetric
      if (mLink.getSymmetricKey() != null)
        mLink.seal(symmetricKey);

      short ownerObjType = mLink.ownerObjType.shortValue();
      if (ownerObjType == Record.RECORD_TYPE_FOLDER) {
        // get shareId
        FolderShareRecord shareRecord = cache.getFolderShareRecordMy(mLink.ownerObjId, true);
        if (shareRecord != null) {
          addUniqueIdTo(shareRecord.shareId, fromShareIDsV);
        }
      } else if (ownerObjType == Record.RECORD_TYPE_MESSAGE) {
        addUniqueAttachmentIDsTo(mLink.ownerObjId, fromMsgLinkIDsV, fromShareIDsV);
      } else {
        throw new IllegalArgumentException("Don't know how to handle owner type " + ownerObjType);
      }
    } // end for

    if (trace != null) trace.exit(SendMessageRunner.class, msgAttachments);
    return msgAttachments;
  }

  private static void addUniqueIdTo(Long id, ArrayList v) {
    if (!v.contains(id)) {
      v.add(id);
    }
  }
  private static void addUniqueAttachmentIDsTo(Long ownerMsgId, ArrayList msgLinkIDsL, ArrayList shareIDsL) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    MsgLinkRecord[] ownerMsgLinks = cache.getMsgLinkRecordsForMsg(ownerMsgId);
    MsgLinkRecord ownerMsgLink = ownerMsgLinks != null && ownerMsgLinks.length > 0 ? ownerMsgLinks[0] : null;
    if (ownerMsgLink != null) {
      addUniqueIdTo(ownerMsgLink.msgLinkId, msgLinkIDsL);
      if (ownerMsgLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
        FolderShareRecord shareRecord = cache.getFolderShareRecordMy(ownerMsgLink.ownerObjId, true);
        if (shareRecord != null) {
          addUniqueIdTo(shareRecord.shareId, shareIDsL);
        }
      }
    }
  }


  /**
   * Clone and encrypt file links to create message attachment file links.
   * Fills the share ID vector with involved shares from where the attachments are taken.
   */
  private static FileLinkRecord[] prepareFileAttachments(FileLinkRecord[] selectedFileLinks, BASymmetricKey symmetricKey, ArrayList fromMsgLinkIDsL, ArrayList fromShareIDsL) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SendMessageRunner.class, "prepareFileAttachments(FileLinkRecord[] selectedFileLinks, BASymmetricKey symmetricKey, ArrayList fromMsgLinkIDsL, ArrayList fromShareIDsL)");
    if (trace != null) trace.args(selectedFileLinks, symmetricKey, fromMsgLinkIDsL, fromShareIDsL);

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    FileLinkRecord[] fileAttachments = (FileLinkRecord[]) RecordUtils.cloneRecords(selectedFileLinks);

    for (int i=0; i<fileAttachments.length; i++) {
      FileLinkRecord fLink = fileAttachments[i];
      fLink.seal(symmetricKey);

      short ownerObjType = fLink.ownerObjType.shortValue();
      if (ownerObjType == Record.RECORD_TYPE_FOLDER) {
        // get shareId
        FolderShareRecord shareRecord = cache.getFolderShareRecordMy(fLink.ownerObjId, true);
        if (shareRecord != null) {
          addUniqueIdTo(shareRecord.shareId, fromShareIDsL);
        }
      } else if (ownerObjType == Record.RECORD_TYPE_MESSAGE) {
        addUniqueAttachmentIDsTo(fLink.ownerObjId, fromMsgLinkIDsL, fromShareIDsL);
      } else {
        throw new IllegalArgumentException("Don't know how to handle owner type " + ownerObjType);
      }
    } // end for

    if (trace != null) trace.exit(SendMessageRunner.class, fileAttachments);
    return fileAttachments;
  }

}
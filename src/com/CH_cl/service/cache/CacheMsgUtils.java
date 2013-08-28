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

import com.CH_cl.service.ops.SendMessageInfoProviderI;
import com.CH_cl.service.ops.UserOps;
import com.CH_cl.service.records.EmailAddressRecord;
import com.CH_cl.service.records.NewsAddressRecord;
import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.MsgFilter;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
* <b>$Revision: 1.1 $</b>
* @author  Marcin Kurzawa
* @version
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
      // use my contact list only, not the reciprocal contacts
      fromRec = CacheUsrUtils.convertUserIdToFamiliarUser(msgData.senderUserId, true, false);
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

  public static String[] getSigText(UserSettingsRecord userSettingsRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheMsgUtils.class, "getSigText(UserSettingsRecord userSettingsRecord)");
    if (trace != null) trace.args(userSettingsRecord);

    boolean isHtmlMode = getSigType(userSettingsRecord).equals("text/html");
    String text = "";
    String mode = "";
    String defaultSig = userSettingsRecord.getDefaultSig();
    String defaultSigType = userSettingsRecord.getDefaultSigType();
    if (defaultSig != null) {
      if (defaultSigType.equalsIgnoreCase("text/file")) {
        String error = null;
        try {
          BufferedReader br = new BufferedReader(new FileReader(defaultSig));
          StringBuffer sb = new StringBuffer();
          String line = null;
          while ((line=br.readLine()) != null) {
            if (sb.length() > 0)
              sb.append("\n");
            sb.append(line);
          }
          br.close();
          text = sb.toString();
          // check all characters of the signature for validity
          for (int i=0; i<text.length(); i++) {
            char ch = text.charAt(i);
            if (!Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch) && (ch < 32 || ch > 126)) {
              error = "The signature file specified is not a valid text-file.";
              break;
            }
          }
          if (error != null)
            text = "";
        } catch (Throwable t) {
          error = "The signature text-file could not be loaded. \n\nError code is: " + t.getMessage();
        }
        if (error != null) {
          NotificationCenter.show(NotificationCenter.WARNING_MESSAGE, "Invalid Signature", error);
        }
      } else {
        text = defaultSig;
      }
    }
    if (isHtmlMode) {
      text = HTML_Ops.clearHTMLheaderAndConditionForDisplay(text, true, false, true, true, true, true, false, false);
    }
    boolean addSpaces = text.trim().length() > 0;
    if (isHtmlMode)
      mode = "text/html";
    else
      mode = "text/plain";
    if (addSpaces && isHtmlMode)
      text = "<p></p>" + text;
    else if (addSpaces && !isHtmlMode)
      text = "\n\n" + text;

    String[] sig = new String[] { mode, text };
    if (trace != null) trace.exit(CacheMsgUtils.class, sig);
    return sig;
  }

  private static String getSigType(UserSettingsRecord userSettingsRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheMsgUtils.class, "getSigType(UserSettingsRecord userSettingsRecord)");
    if (trace != null) trace.args(userSettingsRecord);

    String sigType = userSettingsRecord.getDefaultSigType();
    String type = null;
    if (sigType == null) {
      type = "";
    } else if (sigType.equalsIgnoreCase("text/html")) {
      type = "text/html";
    } else if (sigType.equalsIgnoreCase("text/plain")) {
      type = "text/plain";
    } else if (sigType.equalsIgnoreCase("text/file")) {
      String fileName = userSettingsRecord.getDefaultSig().toLowerCase();
      if (fileName.endsWith(".htm") || fileName.endsWith(".html"))
        type = "text/html";
      else
        type = "text/plain";
    } else {
      type = null;
    }

    if (trace != null) trace.exit(CacheMsgUtils.class, type);
    return type;
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

  /**
  * @return Elements consist of: header type ("text/plain" or "text/html"), header, body type, body
  */
  public static String[] makeReplyToContent(MsgLinkRecord linkRecord, MsgDataRecord dataRecord, boolean isForceConvertHTMLtoPLAIN, boolean isForceOutputInHTMLPrintHeader, boolean isForceOutputInHTMLBody) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheMsgUtils.class, "makeReplyToContent(MsgDataRecord dataRecord, boolean isForceConvertHTMLtoPLAIN, boolean isForceOutputInHTMLPrintHeader, boolean isForceOutputInHTMLBody)");
    if (trace != null) trace.args(dataRecord);
    if (trace != null) trace.args(isForceConvertHTMLtoPLAIN);
    if (trace != null) trace.args(isForceOutputInHTMLPrintHeader);
    if (trace != null) trace.args(isForceOutputInHTMLBody);

    String[] content = new String[4]; // header type, header, body type, body

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();

    Object[] senderSet = UserOps.getCachedOrMakeSenderDefaultEmailSet(dataRecord);
    Record sender = (Record) senderSet[0];
    String senderEmailShort = (String) senderSet[1];
    String senderEmailFull = (String) senderSet[2];

    if (sender != null) {
      Record[][] recipients = gatherAllMsgRecipients(dataRecord);
      StringBuffer[] sb = new StringBuffer[recipients.length];
      for (int recipientType=0; recipientType<recipients.length; recipientType++) {
        sb[recipientType] = new StringBuffer();
        if (recipients[recipientType] != null) {
          for (int i=0; i<recipients[recipientType].length; i++) {
            Record recipient = recipients[recipientType][i];
            String displayName = TextRenderer.getRenderedText(recipient);
            String[] emailAddr = CacheUsrUtils.getEmailAddressSet(recipient);
            if (dataRecord.isHtmlMail()) {
              // skip embeding link if generating Print Header
              if (isForceOutputInHTMLPrintHeader || emailAddr == null) {
                if (emailAddr != null) {
                  sb[recipientType].append(Misc.encodePlainIntoHtml(emailAddr[2]));
                } else {
                  sb[recipientType].append(Misc.encodePlainIntoHtml(displayName));
                }
              } else {
                sb[recipientType].append("<A href='mailto:" + emailAddr[1] + "'>" + Misc.encodePlainIntoHtml(displayName) + "</A>");
              }
            } else {
              if (emailAddr != null) {
                if (isForceOutputInHTMLPrintHeader) {
                  sb[recipientType].append(Misc.encodePlainIntoHtml(emailAddr[2]));
                } else {
                  sb[recipientType].append(emailAddr[2]);
                }
              } else {
                if (isForceOutputInHTMLPrintHeader) {
                  sb[recipientType].append(Misc.encodePlainIntoHtml(displayName));
                } else {
                  sb[recipientType].append(displayName);
                }
              }
            }
            if (i < recipients[recipientType].length - 1) {
              sb[recipientType].append("; ");
            }
          }
        }
      }

      boolean convertHTMLBodyToPlain = isForceConvertHTMLtoPLAIN;
//      if (!isForceConvertHTMLtoPLAIN) {
//        convertHTMLBodyToPlain = MsgPreviewPanel.isDefaultToPLAINpreferred(linkRecord, dataRecord);
//      }

      String quotedSubject = dataRecord.isTypeMessage() ? dataRecord.getSubject() : dataRecord.name;
      String quotedMsgBody = dataRecord.isTypeMessage() ? dataRecord.getText() : dataRecord.addressBody;

      // << comes in HTML
      if (dataRecord.isHtmlMail() || dataRecord.isTypeAddress()) {
        // clear excessive HTML to make feasible for usage as reply content
        quotedMsgBody = HTML_Ops.clearHTMLheaderAndConditionForDisplay(quotedMsgBody, true, false, true, true, true, true, false, false);
//        if (convertHTMLBodyToPlain) {
//          String html = quotedMsgBody;
//          if (trace != null) trace.data(50, "extracting plain from html");
//          String plain = MsgPanelUtils.extractPlainFromHtml(html);
//          if (trace != null) trace.data(51, "encoding plain into html");
//          quotedMsgBody = Misc.encodePlainIntoHtml(plain);
//        }
        content[2] = "text/html";
        // >> comes out in reduced HTML, but still HTML
      } else {
        if (isForceOutputInHTMLBody) {
          // << comes in PLAIN
          if (trace != null) trace.data(60, "extracting plain from html");
          quotedMsgBody = Misc.encodePlainIntoHtml(quotedMsgBody);
          content[2] = "text/html";
          // >> comes out converted to HTML
        } else {
          // << comes in PLAIN
          content[2] = "text/plain";
          // >> comes out PLAIN
        }
      }

      // additional conditioning...
      if (quotedMsgBody != null && quotedMsgBody.length() > 0 && dataRecord.isHtmlMail() && !convertHTMLBodyToPlain) {
        // quotedMsgBody = "<table>" + quotedMsgBody + "</table>";
      } else if (quotedMsgBody == null) {
        quotedMsgBody = "";
      }

      if (isForceOutputInHTMLPrintHeader || isForceOutputInHTMLBody || dataRecord.isHtmlMail() || dataRecord.isTypeAddress()) {
        if (trace != null) trace.data(70, "making HTML header lines");
        String dateCreated = "";
        try {
          dateCreated = new SimpleDateFormat("EEEEE, MMMMM dd, yyyy h:mm aa").format(dataRecord.dateCreated);
        } catch (NullPointerException e) {
        }
        content[0] = "text/html";
        content[1] =
            (isForceOutputInHTMLPrintHeader ? "<font size='-1'><b>" + Misc.encodePlainIntoHtml(TextRenderer.getRenderedText(cache.getUserRecord())) + "</b></font>" : "") +
            (isForceOutputInHTMLPrintHeader ? "<hr color=#000000 noshade size=2>" : "") +
            (isForceOutputInHTMLPrintHeader ? "<table cellpadding='0' cellspacing='0' border='0'>" : "") +

            makeHtmlHeaderLine(com.CH_cl.lang.Lang.rb.getString("column_From"),
                (isForceOutputInHTMLPrintHeader ? Misc.encodePlainIntoHtml(senderEmailFull) : "<A href='mailto:" + senderEmailShort + "'>" + Misc.encodePlainIntoHtml(senderEmailFull) + "</A>"),
                isForceOutputInHTMLPrintHeader) +
            //(sbReplyTo != null ? ("<b>" + com.CH_gui.lang.Lang.rb.getString("column_Reply_To") + ":</b>  " + Misc.encodePlainIntoHtml(sbReplyTo.toString()) + " <br>") : "") +
            makeHtmlHeaderLine(com.CH_cl.lang.Lang.rb.getString("column_To"), sb[SendMessageInfoProviderI.TO].toString(), isForceOutputInHTMLPrintHeader) +
            makeHtmlHeaderLine(com.CH_cl.lang.Lang.rb.getString("column_Cc"), sb[SendMessageInfoProviderI.CC].toString(), isForceOutputInHTMLPrintHeader) +
            makeHtmlHeaderLine(com.CH_cl.lang.Lang.rb.getString("column_Sent"), Misc.encodePlainIntoHtml(dateCreated), isForceOutputInHTMLPrintHeader) +
            makeHtmlHeaderLine(com.CH_cl.lang.Lang.rb.getString("column_Subject"), Misc.encodePlainIntoHtml(quotedSubject), isForceOutputInHTMLPrintHeader) +
            (isForceOutputInHTMLPrintHeader ? "</table>" : "");

        if (content[2].equalsIgnoreCase("text/html")) {
          // condition formatting is a performance hit for complex messages -- removed
//          if (trace != null) trace.data(100, "condition formatting start");
//          quotedMsgBody = ArrayUtils.replaceKeyWords(quotedMsgBody,
//              new String[][] {
//                {"<P>", "<p>"},
//                {"</P>", "</p>"},
//                {"\n", " "},
//                {"\r", " "},
//                {"     ", " "},
//                {"   ", " "},
//                {"  ", " "},
//                {" <p>", "<p>"},
//                {" </p>", "</p>"},
//                {"<p> ", "<p>"},
//                {"</p> ", "</p>"},
//                {"<p></p><p></p><p></p><p></p><p></p>", "<p></p><p></p>"},
//                {"<p></p><p></p><p></p>", "<p></p><p></p>"},
//                {"<p><p><p><p><p>", "<p><p>"},
//                {"<p><p><p>", "<p><p>"},
//            }, new String[] { "<PRE>", "<pre>" }, new String[] { "</PRE>", "</pre>"} );
//          if (trace != null) trace.data(101, "condition formatting done");
        }
        content[3] = quotedMsgBody;
      } else {
        content[0] = "text/plain";
        content[1] =
            com.CH_cl.lang.Lang.rb.getString("column_From") + ": " + senderEmailFull + "\n" +
            (sb[SendMessageInfoProviderI.TO].length() > 0 ? (com.CH_cl.lang.Lang.rb.getString("column_To") + ": " + sb[SendMessageInfoProviderI.TO].toString() + "\n") : "") +
            (sb[SendMessageInfoProviderI.CC].length() > 0 ? (com.CH_cl.lang.Lang.rb.getString("column_Cc") + ": " + sb[SendMessageInfoProviderI.CC].toString() + "\n") : "") +
            com.CH_cl.lang.Lang.rb.getString("column_Sent") + ": " + new SimpleDateFormat("EEEEE, MMMMM dd, yyyy h:mm aa").format(dataRecord.dateCreated) + "\n" +
            com.CH_cl.lang.Lang.rb.getString("column_Subject") + ": " + quotedSubject + " \n\n";
        content[3] = quotedMsgBody;
      }
    }

    if (trace != null) trace.exit(CacheMsgUtils.class, content);
    return content;
  }

  private static String makeHtmlHeaderLine(String name, String htmlText, boolean isPrintHeader) {
    if (htmlText != null && htmlText.length() > 0) {
      return (isPrintHeader ? "<tr><td align='left' valign='top' width='100'><font size='-2'>" : "") +
              "<b>" + name + ": </b>" +
              (isPrintHeader ? "</font></td><td align='left' valign='top'><font size='-2'>" : "") +
              htmlText +
              (isPrintHeader ? "</font></td></tr>" : "<br>");
    } else {
      return "";
    }
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
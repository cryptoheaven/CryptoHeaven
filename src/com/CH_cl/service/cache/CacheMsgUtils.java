/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
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
import com.CH_cl.service.records.InternetAddressRecord;
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
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.1 $</b>
*
* @author  Marcin Kurzawa
*/
public class CacheMsgUtils {

  private static final String STR_RE = com.CH_cl.lang.Lang.rb.getString("msg_Re");
  private static final String STR_FWD = com.CH_cl.lang.Lang.rb.getString("msg_Fwd");

  public static final String[] CONTENT_REPLY_HEADINGS = { 
    com.CH_cl.lang.Lang.rb.getString("column_From") + ":", 
    com.CH_cl.lang.Lang.rb.getString("column_To") + ":", 
    com.CH_cl.lang.Lang.rb.getString("column_Cc") + ":", 
    com.CH_cl.lang.Lang.rb.getString("column_Date") + ":", 
    com.CH_cl.lang.Lang.rb.getString("column_Subject") + ":"
  };

  /**
  * Gathers all recipients into a Record[]
  */
  public static Record[][] gatherAllMsgRecipients(final FetchedDataCache cache, MsgDataRecord dataRecord) {
    if (dataRecord != null)
      return gatherAllMsgRecipients(cache, dataRecord.getRecipients());
    return null;
  }
  public static Record[][] gatherAllMsgRecipients(final FetchedDataCache cache, String recipients) {
    return gatherAllMsgRecipients(cache, recipients, -1);
  }
  public static Record[][] gatherAllMsgRecipients(final FetchedDataCache cache, String recipients, int gatherFirst_N_only) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheMsgUtils.class, "gatherAllMsgRecipients(String recipients, int gatherFirst_N_only)");
    if (trace != null) trace.args(recipients);
    if (trace != null) trace.args(gatherFirst_N_only);

    ArrayList recsLto = new ArrayList();
    ArrayList recsLcc = new ArrayList();
    ArrayList recsLbcc = new ArrayList();
    int countGathered = 0;
    if (recipients != null && recipients.length() > 0) {
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
              rec = CacheUsrUtils.convertUserIdToFamiliarUser(cache, lId, true, false);
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


  public static Record getFromAsFamiliar(final FetchedDataCache cache, MsgDataRecord msgData) {
    Record fromRec = null;
    String fromEmailAddress = msgData.getFromEmailAddress();
    if (msgData.isEmail() || fromEmailAddress != null) {
      fromRec = CacheEmlUtils.convertToFamiliarEmailRecord(cache, fromEmailAddress);
    } else {
      // use my contact list only, not the reciprocal contacts
      fromRec = CacheUsrUtils.convertUserIdToFamiliarUser(cache, msgData.senderUserId, true, false);
    }
    return fromRec;
  }

  public static MsgLinkRecord[] getMsgLinkRecordsWithFetchedDatas(final FetchedDataCache cache, Long folderId) {
    MsgLinkRecord[] mLinks = cache.getMsgLinkRecordsForFolder(folderId);
    ArrayList mLinksFetchedL = new ArrayList();
    for (int i=0; i<mLinks.length; i++) {
      MsgDataRecord mData = cache.getMsgDataRecord(mLinks[i].msgId);
      if (mData != null && mData.getEncText() != null)
        mLinksFetchedL.add(mLinks[i]);
    }
    return (MsgLinkRecord[]) ArrayUtils.toArray(mLinksFetchedL, MsgLinkRecord.class);
  }

  public static Record getMsgSenderForReply(final FetchedDataCache cache, MsgDataRecord msgData) {
    Record sender = null;
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

  public static Record[][] getReplyAllRecipients(final FetchedDataCache cache, MsgDataRecord msgData) {
    Record recipient = CacheMsgUtils.getMsgSenderForReply(cache, msgData);
    Record[][] allRecipients = CacheMsgUtils.gatherAllMsgRecipients(cache, msgData);
    Record[] sender = new Record[] { recipient };
    // add the sender to the TO header as recipient
    if (allRecipients[0] != null) {
      allRecipients[0] = (Record[]) ArrayUtils.concatinate(sender, allRecipients[0], Record.class);
      allRecipients[0] = (Record[]) ArrayUtils.removeDuplicates(allRecipients[0]);
    } else {
      allRecipients[0] = new Record[] { recipient };
    }
    // add all replyTo addresses to the TO header as recipients
    String[] replyTos = msgData.getReplyToAddresses();
    if (replyTos != null && (replyTos.length > 1 || (replyTos.length == 1 && !EmailRecord.isAddressEqual(replyTos[0], msgData.getFromEmailAddress())))) {
      EmailAddressRecord[] replyToEmlRecs = new EmailAddressRecord[replyTos.length];
      for (int i=0; i<replyTos.length; i++) {
        replyToEmlRecs[i] = new EmailAddressRecord(replyTos[i]);
      }
      allRecipients[0] = (Record[]) ArrayUtils.concatinate(allRecipients[0], replyToEmlRecs, Record.class);
      allRecipients[0] = (Record[]) ArrayUtils.removeDuplicates(allRecipients[0]);
    }
    // subtract myself from TO, CC, BCC
    // subtract my UserRecord first
    for (int i=0; i<allRecipients.length; i++) {
      if (allRecipients[i] != null && allRecipients[i].length > 0)
        allRecipients[i] = RecordUtils.getDifference(allRecipients[i], new Record[] { cache.getUserRecord() });
    }
    // subtract my EmailRecords (after converting to EmailAddressRecords) next
    EmailRecord[] myEmlRecs = cache.getEmailRecords(cache.getMyUserId());
    EmailAddressRecord[] myEmlAddrRecs = new EmailAddressRecord[myEmlRecs.length];
    for (int i=0; i<myEmlRecs.length; i++)
      myEmlAddrRecs[i] = new EmailAddressRecord(myEmlRecs[i].getEmailAddressFull());
    for (int i=0; i<allRecipients.length; i++) {
      if (allRecipients[i] != null && allRecipients[i].length > 0)
        allRecipients[i] = RecordUtils.getDifference(allRecipients[i], myEmlAddrRecs, new InternetAddressRecord.AddressComparator());
    }
    return allRecipients;
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

  public static String getSubjectForward(final FetchedDataCache cache, Object[] attachments) {
    return getSubjectForward(cache, attachments, 0);
  }
  public static String getSubjectForward(final FetchedDataCache cache, Object[] attachments, int truncateShort) {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<attachments.length; i++) {
      sb.append(TextRenderer.getRenderedText(cache, attachments[i]));
      if (i < attachments.length - 1)
        sb.append("; ");
    }
    String subject = STR_FWD + " [" + sb.toString() + "]";
    if (truncateShort > 0 && subject.length() > truncateShort) {
      subject = subject.substring(0, truncateShort) + "...";
    }
    return subject;
  }

  public static String getSubjectReply(final FetchedDataCache cache, MsgLinkRecord replyToLink) {
    return getSubjectReply(cache, replyToLink, 0);
  }
  public static String getSubjectReply(final FetchedDataCache cache, MsgLinkRecord replyToLink, int truncateShort) {
    MsgDataRecord replyToData = cache.getMsgDataRecord(replyToLink.msgId);
    return getSubjectReply(replyToData, truncateShort);
  }

  public static String getSubjectReply(MsgDataRecord replyToData, int truncateShort) {
    String oldSubject = replyToData.getSubject();
    if (oldSubject == null)
      oldSubject = "";
    String subject = STR_RE + " " + eliminatePrefixes(oldSubject);
    if (truncateShort > 0 && subject.length() > truncateShort) {
      subject = subject.substring(0, truncateShort) + "...";
    }
    return subject;
  }

  private static String eliminatePrefixes(String str) {
    if (str != null) {
      boolean changed = false;
      while (true) {
        str = str.trim();
        if (str.startsWith(STR_RE + " ") || str.toUpperCase().startsWith(STR_RE.toUpperCase() + " ")) {
          str = str.substring(STR_RE.length() + 1);
          changed = true;
        }
        if (str.startsWith(STR_FWD + " ") || str.toUpperCase().startsWith(STR_FWD.toUpperCase() + " ")) {
          str = str.substring(STR_FWD.length() + 1);
          changed = true;
        }
        if (!changed)
          break;
        else
          changed = false;
      }
    }
    return str;
  }

  public static boolean hasAttachments(final FetchedDataCache cache, Record rec) {
    boolean hasAttachments = false;
    if (rec instanceof MsgLinkRecord) {
      MsgDataRecord msgData = cache.getMsgDataRecord(((MsgLinkRecord) rec).msgId);
      hasAttachments = msgData.hasAttachments();
    } else if (rec instanceof MsgDataRecord) {
      hasAttachments = ((MsgDataRecord) rec).hasAttachments();
    }
    return hasAttachments;
  }

  /**
   * @param headings: translated strings for From, To, Cc, Date, Subject
  * @return Elements consist of: header type ("text/plain" or "text/html"), header, body type, body
  */
  public static String[] makeReplyToContent(final FetchedDataCache cache, MsgLinkRecord linkRecord, MsgDataRecord dataRecord, boolean isForceConvertHTMLtoPLAIN, boolean isForceOutputInHTMLPrintHeader, boolean isForceOutputInHTMLBody, String[] headings) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheMsgUtils.class, "makeReplyToContent(MsgDataRecord dataRecord, boolean isForceConvertHTMLtoPLAIN, boolean isForceOutputInHTMLPrintHeader, boolean isForceOutputInHTMLBody)");
    if (trace != null) trace.args(dataRecord);
    if (trace != null) trace.args(isForceConvertHTMLtoPLAIN);
    if (trace != null) trace.args(isForceOutputInHTMLPrintHeader);
    if (trace != null) trace.args(isForceOutputInHTMLBody);

    String[] content = new String[4]; // header type, header, body type, body
    int FROM=0,TO=1, CC=2, DATE=3, SUBJECT=4; // heading indexes

    Object[] senderSet = UserOps.getCachedOrMakeSenderDefaultEmailSet(cache, dataRecord);
    Record sender = (Record) senderSet[0];
    String senderEmailShort = (String) senderSet[1];
    String senderEmailFull = (String) senderSet[2];

    if (sender != null) {
      Record[][] recipients = gatherAllMsgRecipients(cache, dataRecord);
      StringBuffer[] sb = new StringBuffer[recipients.length];
      for (int recipientType=0; recipientType<recipients.length; recipientType++) {
        sb[recipientType] = new StringBuffer();
        if (recipients[recipientType] != null) {
          for (int i=0; i<recipients[recipientType].length; i++) {
            Record recipient = recipients[recipientType][i];
            String displayName = TextRenderer.getRenderedText(cache, recipient);
            String[] emailAddr = CacheUsrUtils.getEmailAddressSet(cache, recipient);
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

      String dateCreated = "";
      try {
        dateCreated = new SimpleDateFormat("EEEEE, MMMMM dd, yyyy h:mm aa").format(dataRecord.dateCreated);
      } catch (NullPointerException e) {
      }

      if (isForceOutputInHTMLPrintHeader || isForceOutputInHTMLBody || dataRecord.isHtmlMail() || dataRecord.isTypeAddress()) {
        if (trace != null) trace.data(70, "making HTML header lines");
        content[0] = "text/html";
        content[1] =
            (isForceOutputInHTMLPrintHeader ? "<font size='-1'><b>" + Misc.encodePlainIntoHtml(TextRenderer.getRenderedText(cache, cache.getUserRecord())) + "</b></font>" : "") +
            (isForceOutputInHTMLPrintHeader ? "<hr color=#000000 noshade size=2>" : "") +
            (isForceOutputInHTMLPrintHeader ? "<table cellpadding='0' cellspacing='0' border='0'>" : "") +

            makeHtmlHeaderLine(headings[FROM],
                (isForceOutputInHTMLPrintHeader ? Misc.encodePlainIntoHtml(senderEmailFull) : "<A href='mailto:" + senderEmailShort + "'>" + Misc.encodePlainIntoHtml(senderEmailFull) + "</A>"),
                isForceOutputInHTMLPrintHeader) +
            makeHtmlHeaderLine(headings[TO], sb[SendMessageInfoProviderI.TO].toString(), isForceOutputInHTMLPrintHeader) +
            makeHtmlHeaderLine(headings[CC], sb[SendMessageInfoProviderI.CC].toString(), isForceOutputInHTMLPrintHeader) +
            makeHtmlHeaderLine(headings[DATE], Misc.encodePlainIntoHtml(dateCreated), isForceOutputInHTMLPrintHeader) +
            makeHtmlHeaderLine(headings[SUBJECT], Misc.encodePlainIntoHtml(quotedSubject), isForceOutputInHTMLPrintHeader) +
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
            headings[FROM] + senderEmailFull + "\n" +
            (sb[SendMessageInfoProviderI.TO].length() > 0 ? (headings[TO] + sb[SendMessageInfoProviderI.TO].toString() + "\n") : "") +
            (sb[SendMessageInfoProviderI.CC].length() > 0 ? (headings[CC] + sb[SendMessageInfoProviderI.CC].toString() + "\n") : "") +
            headings[DATE] + dateCreated + "\n" +
            headings[SUBJECT] + quotedSubject + " \n\n";
        content[3] = quotedMsgBody;
      }
    }

    if (trace != null) trace.exit(CacheMsgUtils.class, content);
    return content;
  }

  private static String makeHtmlHeaderLine(String name, String htmlText, boolean isPrintHeader) {
    if (htmlText != null && htmlText.length() > 0) {
      return (isPrintHeader ? "<tr><td><font size='-2'>" : "") +
              "<b>" + name + " </b>" +
              (isPrintHeader ? "</font></td><td><font size='-2'>" : "") +
              htmlText +
              (isPrintHeader ? "</font></td></tr>" : "<br>");
    } else {
      return "";
    }
  }

  public static void tryToUnsealMsgDataWithVerification(final FetchedDataCache cache, MsgDataRecord dataRecord) {
    MsgLinkRecord[] linkRecords = cache.getMsgLinkRecordsForMsg(dataRecord.msgId);
    if (linkRecords != null && linkRecords.length > 0) {
      // if this data record contains sendPrivKeyId, then signature needs to be verified
      if (dataRecord.getSendPrivKeyId() != null) {
        KeyRecord msgSigningKeyRec = cache.getKeyRecord(dataRecord.getSendPrivKeyId());
        if (msgSigningKeyRec != null) {
          BASymmetricKey symKey = null;
          for (int i=0; i<linkRecords.length; i++) {
            if (linkRecords[i] != null)
              symKey = linkRecords[i].getSymmetricKey();
            if (symKey != null)
              break;
          }
          if (symKey != null)
            dataRecord.unSeal(symKey, cache.getMsgBodyKeys(), msgSigningKeyRec);
        }
      }
    }
  }

  public static void unlockPassProtectedMsgIncludingCached(final FetchedDataCache cache, MsgDataRecord msgDataRecStartWith, Hasher.Set bodyKey) {
    // first unseal the specific message so its fast for the user in the view
    doUnlockPassProtectedMsgs(cache, new MsgDataRecord[] { msgDataRecStartWith }, bodyKey);
    // next unseal any other message that might be using the same password...
    Thread th = new ThreadTraced("UnlockPassProtectedMsgs Runner") {
      public void runTraced() {
        doUnlockPassProtectedMsgs(cache, null, null);
      }
    };
    th.setDaemon(true);
    th.start();
  }
  private static void doUnlockPassProtectedMsgs(final FetchedDataCache cache, MsgDataRecord[] msgDatas, Hasher.Set bodyKey) {
    if (msgDatas == null)
      msgDatas = cache.getMsgDataRecords(new MsgFilter((Boolean) null, (Boolean) null, Boolean.FALSE));
    List keys = null;
    if (bodyKey == null) {
      keys = cache.getMsgBodyKeys();
    } else {
      keys = new ArrayList();
      keys.add(bodyKey);
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
              msgDatas[i].unSealWithoutVerify(symmetricKey, keys);
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
          doUnlockPassProtectedMsgs(cache, msgDataAttachments, null);
        }
      }
    }
  }

  
  /**
  * @param mData
  * @return true iff message should default to PLAIN mode given user's general settings
  */
  public static boolean isDefaultToPLAINpreferred(final FetchedDataCache cache, MsgDataRecord mData) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheMsgUtils.class, "isDefaultToPLAINpreferred(MsgDataRecord mData)");
    boolean plainPreferred = false;
    if (mData == null)
      plainPreferred = Misc.isBitSet(cache.getUserRecord().notifyByEmail, UserRecord.EMAIL_MANUAL_SELECT_PREVIEW_MODE);
    else
      plainPreferred = Misc.isBitSet(cache.getUserRecord().notifyByEmail, UserRecord.EMAIL_MANUAL_SELECT_PREVIEW_MODE) &&
                      !mData.senderUserId.equals(cache.getMyUserId()) && // Msgs created by myself never display in non-native mode
                      !(CacheUsrUtils.convertUserIdToFamiliarUser(cache, mData.senderUserId, true, false, false) instanceof ContactRecord); // skip non-native mode for Msgs from your Contacts
    if (trace != null) trace.exit(CacheMsgUtils.class, plainPreferred);
    return plainPreferred;
  }

  /**
  * @param msgLink
  * @param dataRecord
  * @return true iff message should be displayed in PLAIN mode given individual Message setting and user settings.
  */
  public static boolean isDefaultToPLAINpreferred(final FetchedDataCache cache, MsgLinkRecord msgLink, MsgDataRecord dataRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheMsgUtils.class, "isDefaultToPLAINpreferred(MsgLinkRecord msgLink, MsgDataRecord dataRecord)");
    boolean plainPreferred = false;
    FolderRecord ownerFolder = null;
    if (dataRecord.isTypeAddress()) {
      // display Address Records always in HTML
      plainPreferred = false;
      if (trace != null) trace.data(10, "plainPreferred=false due to address type");
    } else if ( msgLink.getOwnerObjType().shortValue() == Record.RECORD_TYPE_FOLDER &&
                (ownerFolder = cache.getFolderRecord(msgLink.getOwnerObjId())) != null &&
                (ownerFolder.folderType.shortValue() == FolderRecord.POSTING_FOLDER || ownerFolder.folderType.shortValue() == FolderRecord.CHATTING_FOLDER)
                ) {
      // display Postings (Chatting msgs too) in NATIVE form because they were already visible in their full form the table anyway
      plainPreferred = !dataRecord.isHtmlMail();
      if (trace != null) trace.data(20, "plainPreferred = !dataRecord.isHtmlMail(); due to owning folder being posting/chatting type");
    } else if (!Misc.isBitSet(msgLink.status, MsgLinkRecord.STATUS_FLAG__APPROVED_FOR_NATIVE_PREVIEW_MODE) && isDefaultToPLAINpreferred(cache, dataRecord)) {
      plainPreferred = true;
      if (trace != null) trace.data(30, "plainPreferred=true due to native preview mode non-approval and default-to-plain-preferred");
    } else {
      plainPreferred = !dataRecord.isHtmlMail();
      if (trace != null) trace.data(40, "plainPreferred = !dataRecord.isHtmlMail();");
    }
    if (trace != null) trace.exit(CacheMsgUtils.class, plainPreferred);
    return plainPreferred;
  }

}
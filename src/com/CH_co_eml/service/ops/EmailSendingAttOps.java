/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co_eml.service.ops;

import com.CH_co.cryptx.*;
import com.CH_co.service.ops.DataAcquisitionHelperI;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.tree.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * Class Description:
 *
 * Class Details:
 *
 * <b>$Revision: 1.25 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class EmailSendingAttOps extends Object {

  // Prevent chain letters from breaking the runtime by consuming all the memory
  private static int MAX_ATTACHMENT_LEVELS = 10;

  /**
   * Fetches the message structure including all message and file attachments.
   * @return root of the tree which contains the complete nested message structure of message links and attached files.
   */
  public static DefaultMutableTreeNode getMessageWithAttachments(DataAcquisitionHelperI dataAcquisitionHelper, Long fromUserId, Long msgLinkId) throws SQLException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EmailSendingAttOps.class, "getMessageWithAttachments(DataAcquisitionHelperI dataAcquisitionHelper, Long fromUserId, Long msgLinkId)");
    if (trace != null) trace.args(dataAcquisitionHelper, fromUserId, msgLinkId);

    MsgLinkRecord msgLink = dataAcquisitionHelper.fetchMsgLinkByID(msgLinkId);
    MsgDataRecord msgData = dataAcquisitionHelper.fetchMsgDataByID(msgLink.msgLinkId, msgLink.msgId);

    boolean isPrivileged = msgData.isPrivilegedBodyAccess(fromUserId, dataAcquisitionHelper.getCurrentTime(true));
    if (!isPrivileged) {
      msgData.setEncText(new BASymCipherBulk(new byte[0]));
    }

    DefaultMutableTreeNode msgRoot = new DefaultMutableTreeNode(new Record[] { msgLink, msgData });
    if (isPrivileged)
      getAttachmentsRecur(dataAcquisitionHelper, fromUserId, msgRoot);

    if (trace != null) trace.exit(EmailSendingAttOps.class, msgRoot);
    return msgRoot;
  }

  /**
   * Recursive call to fetch all attachments to a message.
   * @param node is a Record pair of MsgLinkRecord and MsgLinkData
   * @param workingRows contains operational rows which we will user for fetching data from DB.
   */
  private static void getAttachmentsRecur(DataAcquisitionHelperI dataAcquisitionHelper, Long fromUserId, DefaultMutableTreeNode node) throws SQLException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EmailSendingAttOps.class, "getAttachmentsRecur(DataAcquisitionHelperI dataAcquisitionHelper, Long fromUserId, DefaultMutableTreeNode node)");
    if (trace != null) trace.args(dataAcquisitionHelper, fromUserId, node);

    Record[] nodeObj = (Record[]) node.getUserObject();
    MsgLinkRecord msgLink = (MsgLinkRecord) nodeObj[0];
    MsgDataRecord msgData = (MsgDataRecord) nodeObj[1];

    if (msgData.attachedMsgs.shortValue() > 0) {
      MsgLinkRecord[] links = dataAcquisitionHelper.fetchMsgLinksByOwner(msgLink.getId(), msgData.getId(), Record.RECORD_TYPE_MESSAGE);
      if (links != null) {
        for (int i=0; i<links.length; i++) {
          MsgLinkRecord link = links[i];
          MsgDataRecord data = dataAcquisitionHelper.fetchMsgDataByID(link.msgLinkId, link.msgId);
          DefaultMutableTreeNode child = new DefaultMutableTreeNode(new Record[] { link, data });
          node.add(child);
          boolean isPrivileged = data.isPrivilegedBodyAccess(fromUserId, dataAcquisitionHelper.getCurrentTime(true));
          if (!isPrivileged) {
            data.setEncText(new BASymCipherBulk(new byte[0]));
          } else {
            if (child.getLevel() < MAX_ATTACHMENT_LEVELS)
              getAttachmentsRecur(dataAcquisitionHelper, fromUserId, child);
          }
        }
      }
    }
    if (msgData.attachedFiles.shortValue() > 0) {
      FileLinkRecord[] links = dataAcquisitionHelper.fetchFileLinksByOwner(msgLink.getId(), msgData.getId(), Record.RECORD_TYPE_MESSAGE);
      if (links != null) {
        for (int i=0; i<links.length; i++) {
          FileLinkRecord link = links[i];
          FileDataRecord data = dataAcquisitionHelper.fetchFileDataAttrByID(link.fileLinkId, link.fileId);
          DefaultMutableTreeNode child = new DefaultMutableTreeNode(new Record[] { link, data });
          node.add(child);
        }
      }
    }

    if (trace != null) trace.exit(EmailSendingAttOps.class);
  }

  /**
   * Fetches all attached files from the db into the filesystem and stores them as File objects.
   * @return msgRoot structure with all encrypted files fetched from db.
   */
  public static void fetchAllFileAttachments(DataAcquisitionHelperI dataAcquisitionHelper, DefaultMutableTreeNode msgRoot) throws SQLException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EmailSendingAttOps.class, "fetchAllFileAttachments(DataAcquisitionHelperI dataAcquisitionHelper, DefaultMutableTreeNode msgRoot)");
    if (trace != null) trace.args(dataAcquisitionHelper, msgRoot);
    Enumeration enm = msgRoot.breadthFirstEnumeration();
    while (enm.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) enm.nextElement();
      Record[] recs = (Record[]) node.getUserObject();
      if (recs[0] instanceof FileLinkRecord) {
        FileLinkRecord link = (FileLinkRecord) recs[0];
        FileDataRecord data = (FileDataRecord) recs[1];
        File encFile = data.getEncDataFile();
        File plnFile = data.getPlainDataFile();
        if ((encFile == null || encFile.exists() == false) && (plnFile == null || plnFile.exists() == false))
          recs[1] = dataAcquisitionHelper.getOrFetchEncFileLOB(link, data);
      }
    }
    if (trace != null) trace.exit(EmailSendingAttOps.class);
  }

  /**
   * Take a root message symmetric key and use it to unseal itself and all attached children
   * attachments.  Unsealed child symmetric keys are used to unseal subsequent children.
   * Files are also unsealed and stored as plain files in the temporary filesystem.
   * @return msgRoot structure with unsealed message and file tree
   */
  public static void unsealMessageWithAttachments(DefaultMutableTreeNode msgRoot, BASymmetricKey msgRootKey, List bodyKeys) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EmailSendingAttOps.class, "unsealMessageWithAttachments(DefaultMutableTreeNode msgRoot, BASymmetricKey msgRootKey, List bodyKeys)");
    if (trace != null) trace.args(msgRoot, msgRootKey);
    Enumeration enm = msgRoot.breadthFirstEnumeration();
    while (enm.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) enm.nextElement();
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
      Record[] recs = (Record[]) node.getUserObject();
      if (recs[0] instanceof MsgLinkRecord) {
        MsgLinkRecord link = (MsgLinkRecord) recs[0];
        MsgDataRecord data = (MsgDataRecord) recs[1];

        if (trace != null) trace.data(20, "processing link", link);
        if (trace != null) trace.data(21, "processing data", data);

        data.decompressRecipients();
        if (parent == null) {
          link.setSymmetricKey(msgRootKey);
          data.unSealWithoutVerify(msgRootKey, bodyKeys);
        } else {
          Record[] parentRecs = (Record[]) parent.getUserObject();
          MsgLinkRecord parentLink = (MsgLinkRecord) parentRecs[0];
          MsgDataRecord parentData = (MsgDataRecord) parentRecs[1];

          if (trace != null) trace.data(40, "processing parentLink", parentLink);
          if (trace != null) trace.data(41, "processing parentData", parentData);

          BASymmetricKey unsealingKey = null;
          if (parentData.bodyPassHash != null) {
            unsealingKey = parentData.getSymmetricBodyKey();
            if (trace != null) trace.data(50, "using symmetric body key", unsealingKey);
          } else {
            unsealingKey = parentLink.getSymmetricKey();
            if (trace != null) trace.data(60, "using symmetric link key", unsealingKey);
          }

          if (unsealingKey != null) {
            link.unSeal(unsealingKey);
            data.unSealWithoutVerify(link.getSymmetricKey(), bodyKeys);
          }
        }
      } else if (recs[0] instanceof FileLinkRecord) {
        FileLinkRecord link = (FileLinkRecord) recs[0];
        FileDataRecord data = (FileDataRecord) recs[1];
        if (data.getPlainDataFile() == null || data.getPlainDataFile().exists() == false) {
          Record[] parentRecs = (Record[]) parent.getUserObject();
          MsgLinkRecord parentLink = (MsgLinkRecord) parentRecs[0];
          MsgDataRecord parentData = (MsgDataRecord) parentRecs[1];

          BASymmetricKey unsealingKey = null;
          if (parentData.bodyPassHash != null) {
            unsealingKey = parentData.getSymmetricBodyKey();
          } else {
            unsealingKey = parentLink.getSymmetricKey();
          }

          if (unsealingKey != null) {
            link.unSeal(unsealingKey);
            data.setAutoRemovePlainFile(true);
            data.unSeal(null, link.getSymmetricKey(), null, null, null, null, link.origSize);
          }
        }
      }
    }
    if (trace != null) trace.exit(EmailSendingAttOps.class);
  }

  public static String getSubjectForEmail(MsgDataRecord data) {
    String subject = null;
    if (data.isTypeAddress()) {
      subject = data.name != null ? data.name : "empty";
    } else if (data.isTypeMessage()) {
      subject = data.getSubject() != null ? data.getSubject() : "empty";
    }
    return subject;
  }
  
  /**
   * Take a tree structure of message and file links and datas, and convert it into MIME message and body parts.
   * All nested attachments are converted and linked to the root of the tree into a single MIME message.
   * All messages have their complete (but from possibly truncated list) email recipient list assigned into 
   * their TO anc CC fields except the root which is addressed to the external regular emails only.  
   * This is to avoid loops to sending message to inside of the system.
   * @return msgRoot User Object holds the resulting MIME message
   */
  public static void convertMessageTreeToMimeMessageFormat(DataAcquisitionHelperI dataAcquisitionHelper, DefaultMutableTreeNode msgRoot, String smtpHostProperty, String smtpHostValue, boolean includeReplyLink, boolean forExport, ArrayList returnTempFilesBufferL) throws SQLException, MessagingException, IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EmailSendingAttOps.class, "convertMessageTreeToMimeMessageFormat(DataAcquisitionHelperI dataAcquisitionHelper, DefaultMutableTreeNode msgRoot, String smtpHostProperty, String smtpHostValue, boolean includeReplyLink, boolean forExport, ArrayList returnTempFilesBufferL)");
    if (trace != null) trace.args(dataAcquisitionHelper, msgRoot, smtpHostProperty, smtpHostValue);
    if (trace != null) trace.args(includeReplyLink);
    if (trace != null) trace.args(forExport);
    Enumeration enm = msgRoot.depthFirstEnumeration();
    while (enm.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) enm.nextElement();
      Record[] recs = (Record[]) node.getUserObject();

      if (recs[0] instanceof MsgLinkRecord) {
        MsgLinkRecord link = (MsgLinkRecord) recs[0];
        MsgDataRecord data = (MsgDataRecord) recs[1];

        // create MimeMessage
        // create some properties and get the default Session
        Properties props = new Properties();
        if (smtpHostProperty != null)
          props.put(smtpHostProperty, smtpHostValue);
        Session session = Session.getDefaultInstance(props, null);
        // create a message
        MimeMessage mimeMessage = new MimeMessage(session);
        MimeMultipart mimeMultipart = null;
        MimeBodyPart mimeBodyPart = null;

        MimePart part = null;
        // if more children create 
        if (node.isLeaf()) {
          part = mimeMessage;
        } else {
          mimeMultipart = new MimeMultipart();
          mimeBodyPart = new MimeBodyPart();
          mimeMultipart.addBodyPart(mimeBodyPart);
          part = mimeBodyPart;
          mimeMessage.setContent(mimeMultipart);
        }

        // get FROM email address
        String fromEmailAddress = data.getFromEmailAddress();
        String[] fromSet = null;
        if (fromEmailAddress != null && fromEmailAddress.trim().length() > 0) {
          fromSet = EmailCommonOps.getPersonalAndEmailAddressParts(fromEmailAddress);
        }
        if (!data.isEmail()) {
          if (fromEmailAddress == null || fromEmailAddress.trim().length() == 0)
            fromSet = dataAcquisitionHelper.getDefaultEmailAddressSet(data.senderUserId);
          mimeMessage.setHeader("X-From-CryptoHeaven-UserID", data.senderUserId.toString());
        }
        // set message priority, Normal=3, FYI=5, High=1
        String priority = null; // "3" would be normal
        if (MsgDataRecord.isImpHigh(data.importance.shortValue())) {
          priority = "1";
          mimeMessage.setHeader("Importance", "High");
        } else if (MsgDataRecord.isImpFYI(data.importance.shortValue())) {
          priority = "5";
          mimeMessage.setHeader("Importance", "Low");
        }
        if (priority != null)
          mimeMessage.setHeader("X-Priority", priority);
        // set FROM email address
        mimeMessage.setFrom(new InternetAddress(fromSet[1], fromSet[0]));
        String[] replyTos = data.getReplyToAddresses();
        if (replyTos != null && replyTos.length > 0) {
          Address[] replyToAddrs = new InternetAddress[replyTos.length];
          for (int i=0; i<replyTos.length; i++) {
            replyToAddrs[i] = new InternetAddress(replyTos[i]);
          }
          mimeMessage.setReplyTo(replyToAddrs);
        }
        // set recipients
        {
          // when exporting always include recipients, otherwise message sender code will set who should be recipient of this message...
          Address[][] addresses = gatherEmailableMsgRecipients(dataAcquisitionHelper, data, forExport || !node.isRoot());
          mimeMessage.setRecipients(Message.RecipientType.TO, addresses[MsgLinkRecord.RECIPIENT_TYPE_TO]);
          mimeMessage.setRecipients(Message.RecipientType.CC, addresses[MsgLinkRecord.RECIPIENT_TYPE_CC]);
        }
        mimeMessage.setSentDate(new java.util.Date(data.dateCreated.getTime()));
        String subject = getSubjectForEmail(data);
        mimeMessage.setSubject(subject, "UTF-8"); // in case its null
        String body = "";
        String contentType = "text/plain";
        if (data.isTypeAddress()) {
          if (data.addressBody != null)
            body = data.addressBody;
          else if (data.getTextError() != null)
            body = data.getTextError();
          else
            body = "empty";
          contentType = "text/html";
        } else if (data.isTypeMessage()) {
          body = data.getText() != null ? data.getText() : "empty"; // in case its null
          contentType = data.isHtmlMail() ? "text/html" : "text/plain";
        }
        if (node.isRoot() && includeReplyLink) {
          body += com.CH_co.service.ops.EmailSendingOps.getEmailBannerDivider(contentType);
          body += com.CH_co.service.ops.EmailSendingOps.getSecureReplyBanner(data.senderUserId, data.getSubject(), contentType, null, null);
        }
        part.setContent(body, contentType);
        //part.setText(body, "UTF-8", contentType.substring(contentType.indexOf('/')+1));
        // keep a reference to recs so it doesn't get garbage collected before it is sent
        node.setUserObject(new Object[] { mimeMessage, mimeMultipart, mimeBodyPart, recs });

        // iterate through children and attach them to our multipart
        if (!node.isLeaf()) {
          int childCount = node.getChildCount();
          for (int i=0; i<childCount; i++) {
            TreeNode child = node.getChildAt(i);
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) child;
            Object[] childObjs = (Object[]) childNode.getUserObject();
            if (childObjs[0] != null) {
              // serialize a message and attach as a file
              MimeMessage mm = (MimeMessage) childObjs[0];
              File tempFile = File.createTempFile(FileDataRecord.TEMP_PLAIN_FILE_PREFIX, null);
              // once temporary file is created, add it to collection for removal after we are done with this message tree
              returnTempFilesBufferL.add(tempFile);
              OutputStream tempFileOut = new BufferedOutputStream(new FileOutputStream(tempFile), 32*1024);
              mm.writeTo(tempFileOut);
              tempFileOut.flush();
              tempFileOut.close();
              // take filename from the email because it already handles Records of Address type and Message type...
              String emlSubject = mm.getSubject();
              String filename = null;
              // case of chat messages, most of them have no subject, so use msgId if there is no subject
              if (emlSubject.length() == 0) {
                Record[] childRecs = (Record[]) childObjs[3];
                filename = FileTypes.getFileSafeShortString(((MsgDataRecord) childRecs[1]).msgId + ".eml");
              } else {
                filename = FileTypes.getFileSafeShortString(emlSubject + ".eml");
              }
              DataSource ds = new FileDataSource(tempFile);
              MimeBodyPart bodyPart = new MimeBodyPart();
              bodyPart.setDataHandler(new DataHandler(ds));
              bodyPart.setDisposition("attachment; filename=\"" + filename + "\"");
              bodyPart.setFileName(filename);

              mimeMultipart.addBodyPart(bodyPart);
            } else {
              mimeMultipart.addBodyPart((MimeBodyPart) childObjs[2]);
            }
          }
        }

      } else if (recs[0] instanceof FileLinkRecord) {
        FileLinkRecord link = (FileLinkRecord) recs[0];
        FileDataRecord data = (FileDataRecord) recs[1];
        if (link.isUnSealed() && data.isUnSealed()) {
          MimeBodyPart mimeBodyPart = new MimeBodyPart();
          mimeBodyPart.setDataHandler(new DataHandler(new FileDataSource(data.getPlainDataFile())));
          String filename = link.getFileName();
          mimeBodyPart.setDisposition("attachment; filename=\"" + filename + "\"");
          mimeBodyPart.setFileName(filename);
          String filenameLower = filename.toLowerCase();
          if (filenameLower.endsWith(".wav"))
            mimeBodyPart.setHeader("Content-Type", "audio/wav");
          else if (filenameLower.endsWith(".jpg") || filenameLower.endsWith(".jpeg"))
            mimeBodyPart.setHeader("Content-Type", "image/jpeg");
          else if (filenameLower.endsWith(".gif"))
            mimeBodyPart.setHeader("Content-Type", "image/gif");
          node.setUserObject(new Object[] { null, null, mimeBodyPart, recs }); // keep a reference to recs so it doesn't get garbage collected before it is sent
        } else {
          MimeBodyPart mimeBodyPart = new MimeBodyPart();
          mimeBodyPart.setText("empty");
          mimeBodyPart.setDisposition("attachment; filename=\"empty\"");
          mimeBodyPart.setFileName("empty-file-name");
          node.setUserObject(new Object[] { null, null, mimeBodyPart, recs }); // keep a reference to recs so it doesn't get garbage collected before it is sent
        }
      }
    }
    if (trace != null) trace.exit(EmailSendingAttOps.class);
  }

  /**
   * Convert recipients string of a message data to Email Addresses.  Skip recipient folders or other recipients
   * which cannot be represented in an internet email address format.  If recipient user no longer exists in the system
   * substitute it by his original numeric email address.
   * @return Arrays of addresses, TO and CC and BCC addresses seperately.
   */
  private static Address[][] gatherEmailableMsgRecipients(DataAcquisitionHelperI dataAcquisitionHelper, MsgDataRecord dataRecord, boolean includeUsers) throws AddressException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EmailSendingAttOps.class, "gatherEmailableMsgRecipients(DataAcquisitionHelperI dataAcquisitionHelper, MsgDataRecord dataRecord, boolean includeUsers)");
    if (trace != null) trace.args(dataAcquisitionHelper, dataRecord);
    if (trace != null) trace.args(includeUsers);

    String recipients = dataRecord.getRecipients();
    Vector recsVto = new Vector();
    Vector recsVcc = new Vector();
    Vector recsVbcc = new Vector();
    if (recipients != null && recipients.length() > 0) {
      StringTokenizer st = new StringTokenizer(recipients);
      while (st.hasMoreTokens()) {
        String type = st.nextToken();
        char typeChar = type.charAt(0);
        boolean isCopy = typeChar == MsgDataRecord.RECIPIENT_COPY;
        boolean isCopyBlind = typeChar == MsgDataRecord.RECIPIENT_COPY_BLIND;
        if (isCopy || isCopyBlind) {
          typeChar = type.charAt(1);
        }

        // depending if TO: or CC: collect to a different set
        Vector recsV = isCopy ? recsVcc : recsVto;
        recsV = isCopyBlind ? recsVbcc : recsV;

        String sId = st.nextToken();
        Address rec = null;
        if (typeChar == MsgDataRecord.RECIPIENT_USER) {
          if (includeUsers) {
            Long userId = Long.valueOf(sId);
            try {
              String[] addressSet = dataAcquisitionHelper.getDefaultEmailAddressSet(userId);
              InternetAddress address = new InternetAddress(addressSet[1], addressSet[0]);
              recsV.addElement(address);
            } catch (Throwable t) {
              recsV.addElement(new InternetAddress(""+userId+"@"+URLs.getElements(URLs.DOMAIN_MAIL)[0]));
            }
          }
        } else if (typeChar == MsgDataRecord.RECIPIENT_EMAIL_INTERNET) {
          recsV.addElement(new InternetAddress(Misc.escapeWhiteDecode(sId)));
        } else if (typeChar == MsgDataRecord.RECIPIENT_EMAIL_NEWS) {
          recsV.addElement(new NewsAddress(Misc.escapeWhiteDecode(sId)));
        }
      } // end while
    }

    InternetAddress[][] recs = new InternetAddress[3][];
    recs[MsgLinkRecord.RECIPIENT_TYPE_TO] = new InternetAddress[recsVto.size()];
    recs[MsgLinkRecord.RECIPIENT_TYPE_CC] = new InternetAddress[recsVcc.size()];
    recs[MsgLinkRecord.RECIPIENT_TYPE_BCC] = new InternetAddress[recsVbcc.size()];
    if (recsVto.size() > 0) {
      recsVto.toArray(recs[MsgLinkRecord.RECIPIENT_TYPE_TO]);
    }
    if (recsVcc.size() > 0) {
      recsVcc.toArray(recs[MsgLinkRecord.RECIPIENT_TYPE_CC]);
    }
    if (recsVbcc.size() > 0) {
      recsVbcc.toArray(recs[MsgLinkRecord.RECIPIENT_TYPE_BCC]);
    }

    if (trace != null) trace.exit(EmailSendingAttOps.class, recs);
    return recs;
  }

}
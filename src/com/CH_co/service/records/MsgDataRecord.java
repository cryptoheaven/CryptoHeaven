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

package com.CH_co.service.records;

import com.CH_co.cryptx.*;
import com.CH_co.nanoxml.XMLElement;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;
import java.io.IOException;
import java.security.DigestException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/** 
 * <b>Copyright</b> &copy; 2001-2012
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
 * <b>$Revision: 1.40 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class MsgDataRecord extends Record {

  public static final short MAX_RECIPIENTS_LEN = 255;

  public static final short IMPORTANCE_FYI_OLD = 1;
  public static final short IMPORTANCE_NORMAL_OLD = 2;
  public static final short IMPORTANCE_HIGH_OLD = 3;
  public static final short IMPORTANCE_SYSTEM_NOTICE = 4;
  public static final short IMPORTANCE_OLD_MASK = 1 | 2 | 3 | 4;

  // Cryptogram - Low
  public static final short IMPORTANCE_FYI_PLAIN = 8;
  public static final short IMPORTANCE_FYI_HTML = 16;
  // Regular Email
  public static final short IMPORTANCE_EMAIL_PLAIN = 32;
  public static final short IMPORTANCE_EMAIL_HTML = 64;
  // Secure Web Forms
  public static final short IMPORTANCE_ENC_EMAIL_PLAIN = 128;
  public static final short IMPORTANCE_ENC_EMAIL_HTML = 256;
  // Cryptogram - Normal
  public static final short IMPORTANCE_NORMAL_PLAIN = 512;
  public static final short IMPORTANCE_NORMAL_HTML = 1024;
  // Cryptogram - High
  public static final short IMPORTANCE_HIGH_PLAIN = 2048;
  public static final short IMPORTANCE_HIGH_HTML = 4096;

  public static final short IMPORTANCE_PLAIN_MASK = 8 | 32 | 128 | 512 | 2048;
  public static final short IMPORTANCE_HTML_MASK = 16| 64 | 256 | 1024 | 4096;
//  public static final short IMPORTANCE_HTML_MASK = 1 | 2 | 3 | 4 | 16| 64 | 256 | 1024 | 4096;


  public static final char RECIPIENT_USER = 'u';
  public static final char RECIPIENT_BOARD = 'b';
  public static final char RECIPIENT_EMAIL_INTERNET = 'e';
  public static final char RECIPIENT_EMAIL_NEWS = 'n';
  public static final char RECIPIENT_COPY = 'c';
  public static final char RECIPIENT_COPY_BLIND = 'd';
  // Incoming email from external systems will have
  public static final char RECIPIENT_FROM_EMAIL = 'f';
  public static final char RECIPIENT_REPLY_TO = 'r';

  public static final short FLAG__COMPRESSED_RECIPIENTS = 0x01;
  public static final short FLAG__COMPRESSED_SUBJECT = 0x02;
  public static final short FLAG__COMPRESSED_BODY = 0x04;
  public static final short FLAG__REVOKED = 0x08; // overload compressed integer with other flags

  public static final short OBJ_TYPE_MSG = 1;
  public static final short OBJ_TYPE_ADDR = 2;

  public static final String BACKGROUND_COLOR_INFO = "dfe0cd";
  public static final String BACKGROUND_COLOR_WARNING = "FFFF99";
  public static final String BACKGROUND_COLOR_ERROR = "ffaaaa";

  public Long msgId;
  public Short objType;   // either OBJ_TYPE_MSG or OBJ_TYPE_ADDR, if address, use importance FYI/NORMAL/HIGH in PLAIN only for compatibility with older clients
  public Long replyToMsgId;
  public Short importance;
  public Short attachedFiles;
  public Short attachedMsgs;
  public Long senderUserId;
  private byte[] rawRecipients;
  private BASymCipherBulk encSubject;
  private BASymCipherBulk encText;
  private Short flags = new Short((short) 0); // bits flags to mark compression in recipients/subject/body (1,2,3rd bit respectively)
  private BASymCipherBulk encSignedDigest;    // digest of plain data, signed, then encrypted with msg's symmetric key
  private BASymCipherBulk encEncDigest;       // digest of encrypted data, encrypted with msg's symmetric key
  private Long sendPrivKeyId;
  public Timestamp dateCreated;
  public Timestamp dateExpired;
  public Integer recordSize;
  public Long bodyPassHash; // partial hash of the additional message password, to be XORed with message key to recover original key
  public String bodyPassHint;

  /** unwrapped data */
  private String recipients;
  private String subject;
  private String textBody;
  private String textErr;
  private BADigestBlock digest;     // digest of the plain message (subject and body)
  private BAAsyCipherBlock signedDigest; // signed digest of the plain message
  private BADigestBlock encDigest;  // digest of the encrypted message
  private BASymmetricKey symmetricBodyKey;  // cached symmetric key for the message body if body is password protected
  private Boolean digestOk;         // true if digest verifies ok
  private Boolean encDigestOk;      // true if encDigest verifies ok
  private String fromEmailAddress;  // used when source is a regular email address (combined with IMPORTANCE_EMAIL)
  private String[] replyToAddresses;  // used for regular email with a Reply-To header
  private String encodedHTMLData;   // cached data for rendering in message boards and chats

  /** cached data for addresses */
  public String name;
  public String email;
  public String fileAs;
  public String phoneB;
  public String phoneH;
  public String addressBody;
  public XMLElement addressNotes;


  /** Creates new MsgDataRecord */
  public MsgDataRecord() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgDataRecord.class, "MsgDataRecord()");
    if (trace != null) trace.exit(MsgDataRecord.class);
  }

  public boolean hasAttachments() {
    return getAttachmentCount() > 0;
  }

  public int getAttachmentCount() {
    int numFiles = attachedFiles != null ? attachedFiles.shortValue() : 0;
    int numMsgs = attachedMsgs != null ? attachedMsgs.shortValue() : 0;
    return numFiles + numMsgs;
  }

  public Long getId() {
    return msgId;
  }

  public int getIcon() {
    if (objType.shortValue() == OBJ_TYPE_MSG)
      return ImageNums.MAIL_READ16;
    else if (objType.shortValue() == OBJ_TYPE_ADDR)
      return ImageNums.ADDRESS16;
    else
      return ImageNums.IMAGE_NONE;
  }

  public void setRawRecipients    (byte[] rawRecipients)            { this.rawRecipients    = rawRecipients;    }
  public void setRecipients       (String recipients)               { this.recipients       = recipients;       }
  public void setEncSubject       (BASymCipherBulk encSubject)      { this.encSubject       = encSubject;       }
  public void setEncText          (BASymCipherBulk encText)         { this.encText          = encText;          }
  public void setFlags            (Short flags)                     { this.flags            = flags;            }
  public void setEncSignedDigest  (BASymCipherBulk encSignedDigest) { this.encSignedDigest  = encSignedDigest;  }
  public void setEncEncDigest     (BASymCipherBulk encEncDigest)    { this.encEncDigest     = encEncDigest;     }
  public void setSendPrivKeyId    (Long sendPrivKeyId)              { this.sendPrivKeyId    = sendPrivKeyId;    }
  public void setSubject          (String subject)                  { this.subject          = subject;          }
  public void setTextBody         (String textBody)                 { this.textBody         = textBody;         }

  public byte[]          getRawRecipients()   { return rawRecipients;   }
  public String          getRecipients()      { return recipients;      }
  public BASymCipherBulk getEncSubject()      { return encSubject;      }
  public BASymCipherBulk getEncText()         { return encText;         }
  public Short           getFlags()           { return flags;           }
  public BASymCipherBulk getEncSignedDigest() { return encSignedDigest; }
  public BASymCipherBulk getEncEncDigest()    { return encEncDigest;    }
  public Long            getSendPrivKeyId()   { return sendPrivKeyId;   }
  public String          getSubject()         { return subject;         }
  public BADigestBlock   getDigest()          { return digest;          }
  public BAAsyCipherBlock getSignedDigest()   { return signedDigest;    }
  public BADigestBlock   getEncDigest()       { return encDigest;       }
  public BASymmetricKey  getSymmetricBodyKey(){ return symmetricBodyKey;}
  public String          getFromEmailAddress(){ return fromEmailAddress;}
  public String[]        getReplyToAddresses(){ return replyToAddresses;}

  public String          getTextBody()        { return textBody;        }
  public String          getTextError()       { return textErr;         }

  public String getText() {
    if (textBody != null)
      return textBody;
    else
      return textErr;
  }

  public ImageText getExpirationIconAndText(Long forUserId) {
    return getExpirationIconAndText(forUserId, false);
  }
  public ImageText getExpirationIconAndText(Long forUserId, boolean isShortForm) {
    int icon = ImageNums.IMAGE_NONE;
    String expiration = dateExpired == null ? (isShortForm ? "" : "Never") : (isShortForm ? Misc.getFormattedDate(dateExpired, true, false) : Misc.getFormattedTimestamp(dateExpired));
    String note = "";
    if (dateExpired != null) {
      if (dateExpired.getTime() > System.currentTimeMillis())
        icon = ImageNums.STOPWATCH16;
      else {
        note = " (Expired)";
        if (senderUserId.equals(forUserId))
          icon = ImageNums.STOPWATCH_WARN16;
        else
          icon = ImageNums.STOPWATCH_ALERT16;
      }
    }
    if (Misc.isBitSet(getFlags(), MsgDataRecord.FLAG__REVOKED)) {
      if (dateExpired == null || dateExpired.getTime() < System.currentTimeMillis()) {
        if (dateExpired != null) {
          note = " (Revoked)";
        } else {
          note = "";
          expiration = "Revoked";
        }
        if (senderUserId.equals(forUserId))
          icon = ImageNums.STOPWATCH_WARN16;
        else
          icon = ImageNums.STOPWATCH_ALERT16;
      } else if (dateExpired != null) {
        note = " (Revocation Scheduled)";
      }
    }
    if (!isShortForm)
      expiration += note;
    return new ImageText(icon, expiration);
  }

  public Boolean isDigestOk() {
    return digestOk;
  }

  public Boolean isEncDigestOk() {
    return digestOk;
  }

  /**
   * Seals the <code> subject and text </code> to <code> encSubject and encText </code>
   * using the sealant object which is the message's symmetric key from corresponding message link.
   * Sealing also sets the signed digests and sender's private key id.
   */
  public void seal(BASymmetricKey symmetricKey, KeyRecord signingPrivKey) {
    seal(symmetricKey, null, signingPrivKey);
  }
  public void seal(BASymmetricKey symmetricKey, Hasher.Set bodyKey, KeyRecord signingPrivKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgDataRecord.class, "seal(BASymmetricKey symmetricKey, Hasher.Set bodyKey, KeyRecord signingPrivKey)");

    if (trace != null) trace.data(10, "this", this);
    try {
      if (subject == null)
        subject = "";
      if (textBody == null)
        textBody = "";
      // try compressing the strings to see if they would be smaller
      byte[] cSubject = Misc.compress(subject);
      byte[] cText = Misc.compress(textBody);
      if (cSubject.length >= subject.length()*2)
        cSubject = null;
      if (cText.length >= textBody.length()*2)
        cText = null;

      SymmetricBulkCipher symCipher = new SymmetricBulkCipher(symmetricKey);
      SymmetricBulkCipher symBodyCipher = null;
      if (bodyKey == null || bodyKey.encodedPassword == null)
        symBodyCipher = symCipher;
      else {
        symmetricBodyKey = new BASymmetricKey(symmetricKey);
        symmetricBodyKey.XOR(bodyKey.encodedPassword);
        symBodyCipher = new SymmetricBulkCipher(symmetricBodyKey);
      }
      BASymCipherBulk tempEncSubject = null;
      if (cSubject != null)
        tempEncSubject = new BASymCipherBulk(symCipher.bulkEncrypt(cSubject, 0, cSubject.length));
      else
        tempEncSubject = symCipher.bulkEncrypt(subject);
      BASymCipherBulk tempEncText = null;
      if (cText != null)
        tempEncText = new BASymCipherBulk(symBodyCipher.bulkEncrypt(cText, 0, cText.length));
      else
        tempEncText = symBodyCipher.bulkEncrypt(textBody);

      // The subject and body of the message is digested, then encrypted with message's symmetric key.
      //MessageDigest md = MessageDigest.getInstance("SHA-1");
      MessageDigest md = new SHA256();
      byte[] msgHash = null;
      md.update(Misc.convStrToBytes(subject));
      msgHash = md.digest(Misc.convStrToBytes(textBody));

      if (trace != null) trace.data(10, "Signing message ...");
      AsymmetricBlockCipher asyCipher = new AsymmetricBlockCipher();
      BAAsyCipherBlock tempSignedDigest = asyCipher.signBlock(signingPrivKey.getPrivateKey(), msgHash);
      BASymCipherBulk tempEncSignedDigest = symCipher.bulkEncrypt(tempSignedDigest);
      if (trace != null) trace.data(11, "Signing message ... done.");

      // We also create a digest of the encrypted message and then also encrypt it with message's symmetric key.
      md.reset();
      md.update(tempEncSubject.toByteArray());
      byte[] encMsgHash = md.digest(tempEncText.toByteArray());

      BASymCipherBulk tempEncEncDigest = symCipher.bulkEncrypt(new BASymPlainBulk(encMsgHash));


      super.seal();


      sendPrivKeyId = signingPrivKey.keyId;
      encSubject = tempEncSubject;
      encText = tempEncText;
      encSignedDigest = tempEncSignedDigest;
      signedDigest = tempSignedDigest;
      encEncDigest = tempEncEncDigest;
      bodyPassHash = bodyKey != null ? bodyKey.passwordHash : null;
      // Set the new compressed subject and body flags, but keep the old compressed recipients flag.
      flags = (Short) Misc.setBitObj(cSubject != null, flags, FLAG__COMPRESSED_SUBJECT);
      flags = (Short) Misc.setBitObj(cText != null, flags, FLAG__COMPRESSED_BODY);
    } catch (Throwable t) {
      if (trace != null) trace.exception(MsgDataRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.data(200, "this", this);
    if (trace != null) trace.exit(MsgDataRecord.class);
  }

  public void decompressRecipients() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgDataRecord.class, "decompressRecipients()");
    if (trace != null) trace.data(10, "flags", flags);
    if (trace != null) trace.data(11, "rawRecipients", rawRecipients);
    if (trace != null) trace.data(12, "recipients", recipients);

    if (rawRecipients != null) {
      if (Misc.isBitSet(flags, FLAG__COMPRESSED_RECIPIENTS)) {
        if (trace != null) trace.data(20, "uncompressing", rawRecipients);
        try {
          recipients = Misc.decompressStr(rawRecipients);
          if (trace != null) trace.data(21, "recipients", recipients);
        } catch (IOException e) {
          if (trace != null) trace.exception(MsgDataRecord.class, 22, e);
        }
      } else {
        if (trace != null) trace.data(30, "no need to uncompress", rawRecipients);
        recipients = Misc.convBytesToStr(rawRecipients);
        if (trace != null) trace.data(31, "recipients", recipients);
      }

      if (recipients == null) {
        if (trace != null) trace.data(40, "recipients == null");
        if (trace != null) trace.data(41, "MsgDataRecord", this);
      }

      if (recipients != null) {
        String[] recips = recipients.split("[ ]+");
        int recipsIndex = recips[0].equals("") ? 1 : 0; // ignore leading delimited blanks
        Vector replyToAddressesV = null;
        while (recips.length > recipsIndex) {
          String type = recips[recipsIndex++];
          char typeChar = type.charAt(0);
          boolean isReplyTo = typeChar == MsgDataRecord.RECIPIENT_REPLY_TO;
          if (isReplyTo) {
            typeChar = type.charAt(1);
          }
          if (recips.length > recipsIndex) {
            String token = recips[recipsIndex++];
            if (typeChar == MsgDataRecord.RECIPIENT_FROM_EMAIL) {
              fromEmailAddress = Misc.escapeWhiteDecode(token);
              if (trace != null) trace.data(52, "FROM address found", fromEmailAddress);
            } else if (isReplyTo && typeChar == MsgDataRecord.RECIPIENT_EMAIL_INTERNET) {
              if (replyToAddressesV == null) replyToAddressesV = new Vector();
              String addr = Misc.escapeWhiteDecode(token);
              replyToAddressesV.addElement(addr);
              if (trace != null) trace.data(62, "Reply-To address found", addr);
            }
          }
        }
        replyToAddresses = (String[]) ArrayUtils.toArray(replyToAddressesV, String.class);
      }
    } else {
      if (trace != null) trace.data(200, "rawRecipients were null");
    }
    if (trace != null) trace.data(300, "final recipients value", recipients);
    if (trace != null) trace.exit(MsgDataRecord.class);
  }

  /**
   * Decrypt specified encText using a matching password set from the specified array.
   * @return Object[] { BASymPlainBulk and BASymmetricKey } ie: the decrypted text and symmetric body key
   */
  private static Object[] decryptBody(BASymCipherBulk encText, SymmetricBulkCipher symCipher, BASymmetricKey symmetricKey, Long bodyPassHash, List bodyKeys) throws DigestException, NoSuchAlgorithmException, InvalidKeyException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgDataRecord.class, "decryptBody(BASymCipherBulk encText, SymmetricBulkCipher symCipher, BASymmetricKey symmetricKey, Long bodyPassHash, List bodyKeys)");
    if (trace != null) trace.args(encText, symCipher, symmetricKey, bodyPassHash, bodyKeys);
    BASymPlainBulk plainText = null;
    BASymmetricKey symmetricBodyKey = null;
    SymmetricBulkCipher symBodyCipher = null;
    if (bodyPassHash == null)
      symBodyCipher = symCipher;
    if (symBodyCipher == null) {
      DigestException de = null;
      if (trace != null) trace.data(10, "decryptBody : trying keys");
      if (bodyKeys != null) {
        for (int i=0; i<bodyKeys.size(); i++) {
          Hasher.Set bodyKey = (Hasher.Set) bodyKeys.get(i);
          if (trace != null) trace.data(20, "decryptBody : trying key : ", bodyKey);
          try {
            if (bodyKey.passwordHash.equals(bodyPassHash)) {
              symmetricBodyKey = new BASymmetricKey(symmetricKey);
              symmetricBodyKey.XOR(bodyKey.encodedPassword);
              symBodyCipher = new SymmetricBulkCipher(symmetricBodyKey);
              plainText = symBodyCipher.bulkDecrypt(encText);
              break;
            }
          } catch (DigestException e) {
            de = e;
          }
        }
      }
      if (plainText == null && de != null)
        throw de;
      if (plainText != null)
        if (trace != null) trace.data(10, "decryptBody : trying key WAS SUCCESS");
    } else {
      plainText = symBodyCipher.bulkDecrypt(encText);
    }
    Object[] rc = null;
    if (plainText != null)
      rc = new Object[] { plainText, symmetricBodyKey };
    if (trace != null) trace.exit(MsgDataRecord.class, rc);
    return rc;
  }

  /**
   * Unseals the <code> encSubject, encText, encSignedDigest, encEncDigest </code> into <code> subject, text, digest, encDigest </code>
   * using the unSealant object which is the message's symmetric key and signers public key.
   */
  public void unSeal(BASymmetricKey symmetricKey, List bodyKeys, KeyRecord keyRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgDataRecord.class, "unSeal(BASymmetricKey symmetricKey, List bodyKeys, KeyRecord keyRecord)");

    try {
      SymmetricBulkCipher symCipher = new SymmetricBulkCipher(symmetricKey);
      BASymPlainBulk tempSubject = symCipher.bulkDecrypt(encSubject);
      BASymPlainBulk tempText = null;
      if (encText != null && encText.size() > 0) {
        Object[] rc = decryptBody(encText, symCipher, symmetricKey, bodyPassHash, bodyKeys);
        if (rc != null) {
          tempText = (BASymPlainBulk) rc[0];
          symmetricBodyKey = (BASymmetricKey) rc[1];
        }
      }
      BASymPlainBulk tempSignedDigest = symCipher.bulkDecrypt(encSignedDigest);
      BASymPlainBulk tempEncDigest = symCipher.bulkDecrypt(encEncDigest);
      AsymmetricBlockCipher asyCipher = new AsymmetricBlockCipher();
      BAAsyPlainBlock tempDigest = asyCipher.verifySignature(keyRecord.plainPublicKey, tempSignedDigest.toByteArray());
      // uncompress required fields
      if (Misc.isBitSet(flags, FLAG__COMPRESSED_SUBJECT))
        tempSubject = new BASymPlainBulk(Misc.decompressBytes(tempSubject.toByteArray()));
      if (Misc.isBitSet(flags, FLAG__COMPRESSED_BODY) && tempText != null)
        tempText = new BASymPlainBulk(Misc.decompressBytes(tempText.toByteArray()));

      // The subject and body of the message is digested.
      if (tempText != null || encText != null) {
        MessageDigest md = new SHA256();
        if (tempText != null) {
          md.update(tempSubject.toByteArray());
          byte[] msgHash = md.digest(tempText.toByteArray());
          digestOk = Boolean.valueOf(MessageDigest.isEqual(tempDigest.toByteArray(), msgHash));
        }
        if (encText != null) {
          md.reset();
          md.update(encSubject.toByteArray());
          byte[] encMsgHash = md.digest(encText.toByteArray());
          encDigestOk = Boolean.valueOf(MessageDigest.isEqual(tempEncDigest.toByteArray(), encMsgHash));
        }
      }

      super.unSeal();

      subject = tempSubject.toByteStr();
      if (tempText != null) {
        textBody = tempText.toByteStr();
        textErr = null;
      } else {
        textBody = null;
        textErr = makeBodyUnavailableContent();
      }
      digest = new BADigestBlock(tempDigest);
      signedDigest = new BAAsyCipherBlock(tempSignedDigest);
      encDigest = new BADigestBlock(tempEncDigest);

      parseAddressPreview();
      // address body will be assigned with proper data or unavailable message
      addressBody = parseAddressBody();
      encodedHTMLData = null;

    } catch (Throwable t) {
      if (trace != null) trace.exception(MsgDataRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(MsgDataRecord.class);
  }

  /**
   * Unseals the <code> encSubject, encText, encEncDigest </code> into <code> subject, text, encDigest </code>
   * using the unSealant object which is the message's symmetric key -- DOES NOT verify signatures!
   */
  public void unSealWithoutVerify(BASymmetricKey symmetricKey, List bodyKeys) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgDataRecord.class, "unSealWithoutVerify(BASymmetricKey symmetricKey, List bodyKeys)");

    try {
      SymmetricBulkCipher symCipher = new SymmetricBulkCipher(symmetricKey);
      BASymPlainBulk tempSubject = symCipher.bulkDecrypt(encSubject);
      BASymPlainBulk tempText = null;
      if (encText != null && encText.size() > 0) {
        Object[] rc = decryptBody(encText, symCipher, symmetricKey, bodyPassHash, bodyKeys);
        if (rc != null) {
          tempText = (BASymPlainBulk) rc[0];
          symmetricBodyKey = (BASymmetricKey) rc[1];
        }
      }
      BASymPlainBulk tempSignedDigest = symCipher.bulkDecrypt(encSignedDigest);
      BASymPlainBulk tempEncDigest = symCipher.bulkDecrypt(encEncDigest);

      // uncompress required fields
      if (Misc.isBitSet(flags, FLAG__COMPRESSED_SUBJECT))
        tempSubject = new BASymPlainBulk(Misc.decompressBytes(tempSubject.toByteArray()));
      if (Misc.isBitSet(flags, FLAG__COMPRESSED_BODY) && tempText != null)
        tempText = new BASymPlainBulk(Misc.decompressBytes(tempText.toByteArray()));

      // The subject and body of the message is digested.
      if (encText != null) {
        MessageDigest md = new SHA256();
        md.update(encSubject.toByteArray());
        byte[] encMsgHash = md.digest(encText.toByteArray());
        encDigestOk = Boolean.valueOf(MessageDigest.isEqual(tempEncDigest.toByteArray(), encMsgHash));
      }

      super.unSeal();

      subject = tempSubject.toByteStr();
      if (tempText != null) {
        textBody = tempText.toByteStr();
        textErr = null;
      } else {
        textBody = null;
        textErr = makeBodyUnavailableContent();
      }
      signedDigest = new BAAsyCipherBlock(tempSignedDigest);
      encDigest = new BADigestBlock(tempEncDigest);

      parseAddressPreview();
      // address body will be assigned with proper data or unavailable message
      addressBody = parseAddressBody();
      encodedHTMLData = null;

    } catch (Throwable t) {
      if (trace != null) trace.exception(MsgDataRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(MsgDataRecord.class);
  }


  /**
   * Unseals the <code> encSubject </code> into <code> subject</code>
   * using the unSealant object which is the message's symmetric key.
   */
  public void unSealSubject(BASymmetricKey symmetricKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgDataRecord.class, "unSealSubject(BASymmetricKey symmetricKey)");

    try {
      SymmetricBulkCipher symCipher = new SymmetricBulkCipher(symmetricKey);
      BASymPlainBulk tempSubject = symCipher.bulkDecrypt(encSubject);

      // uncompress required fields
      if (Misc.isBitSet(flags, FLAG__COMPRESSED_SUBJECT))
        tempSubject = new BASymPlainBulk(Misc.decompressBytes(tempSubject.toByteArray()));

      super.unSeal();

      subject = tempSubject.toByteStr();
      parseAddressPreview();

    } catch (Throwable t) {
      if (trace != null) trace.exception(MsgDataRecord.class, 100, t);
      subject = "*** Error: Subject could not be decrypted: " + t.getMessage();
    }

    if (trace != null) trace.exit(MsgDataRecord.class);
  }


  private String makeBodyUnavailableContent() {
    String text = null;
    int impPlain = importance.shortValue() & IMPORTANCE_PLAIN_MASK;
    boolean isPlain = impPlain != 0;
    String imgName = ImageNums.getImageName(ImageNums.STOPWATCH_ALERT16);
    String imgStr = isPlain ? "" : "<img src=\"images/"+imgName+"\" align=\"center\">";
    String accessStr = "";
    String dateStr = "";
    if (encText != null && encText.size() > 0 && getTextBody() == null && bodyPassHash != null) {
      imgStr = "";
      accessStr = "password protected";
    } else if (Misc.isBitSet(flags, FLAG__REVOKED)) {
      accessStr = "revoked";
      dateStr = dateExpired != null ? " on " + Misc.getFormattedTimestamp(dateExpired) : "";
    } else if (dateExpired != null) {
      accessStr = "expired";
      dateStr = " on " + Misc.getFormattedTimestamp(dateExpired);
    } else {
      imgStr = "";
      accessStr = "unavailable";
    }

    String space = isPlain ? " " : "&nbsp;";
    String str = space + imgStr + space + " Content " + accessStr + dateStr + space;
    String tablePre = isPlain ? "" :
            "<table border='0' cellspacing='0' cellpadding='0'>"
              +"<tr>"
                +"<td width='100%' bgcolor='#"+BACKGROUND_COLOR_WARNING+"'>";
    String tablePost = isPlain ? "" :
                "</td>"
              +"</tr>"
            +"</table>";
    text =  tablePre + str + tablePost;

    return text;
  }


  public void parseAddressPreview() {
    name = "";
    email = "";
    fileAs = "";
    phoneB = "";
    phoneH = "";
    if (objType.shortValue() == MsgDataRecord.OBJ_TYPE_ADDR) {
      XMLElement e = new XMLElement();
      try {
        e.parseString(subject);
      } catch (Throwable t) {
      }
      if (e.getNameSafe().equals("AddrPrev")) {
        Vector v = e.getChildren();
        for (int i=0; i<v.size(); i++) {
          XMLElement c = (XMLElement) v.elementAt(i);
          String n = c.getNameSafe();
          String s = c.getContent();
          if (n.equals("Name"))
            name = s;
          else if (n.equals("E-mail"))
            email = s;
          else if (n.equals("FileAs"))
            fileAs = s;
          else if (n.equals("Business Phone"))
            phoneB = s;
          else if (n.equals("Home Phone"))
            phoneH = s;
        }
      }
    }
  }

  public XMLElement parseAddressContent() {
    XMLElement e = new XMLElement();
    try {
      e.parseString(textBody);
    } catch (Throwable t) {
    }
    return e;
  }

  private String parseAddressBody() {
    return parseAddressBody(false, false);
  }
  public String parseAddressBody(boolean useVerticalLayout, boolean useSmallerFont) {
    String parsedAddrBody = "";
    addressNotes = null;
    if (objType.shortValue() == MsgDataRecord.OBJ_TYPE_ADDR) {
      XMLElement e = new XMLElement();
      // get Body or Error message
      String text = getText();
      boolean parsed = false;
      try {
        e.parseString(text);
        parsed = true;
      } catch (Throwable t) {
        // this must be the "Content Revoked" or similar error message
        parsedAddrBody = text;
      }
      if (parsed && e.getNameSafe().equals("AddrFull")) {
        String nameHtml = null;
        String addrHtml = null;
        String phoneHtml = null;
        String emailHtml = null;
        String webHtml = null;
        String notesHtml = null;
        Vector v = e.getChildren();
        for (int i=0; i<v.size(); i++) {
          XMLElement c = (XMLElement) v.elementAt(i);
          String n = c.getNameSafe();
          if (n.equals("Name")) {
            nameHtml = getHTMLforPart(c, useVerticalLayout, useSmallerFont);
          } else if (n.equals("Addresses")) {
            addrHtml = getHTMLforPart(c, useVerticalLayout, useSmallerFont);
          } else if (n.equals("Phones")) {
            phoneHtml = getHTMLforPart(c, useVerticalLayout, useSmallerFont);
          } else if (n.equals("Emails")) {
            emailHtml = getHTMLforPart(c, useVerticalLayout, useSmallerFont);
          } else if (n.equals("Web")) {
            webHtml = getHTMLforPart(c, useVerticalLayout, useSmallerFont);
          } else if (n.equals("Notes")) {
            notesHtml = getHTMLforPart(c, useVerticalLayout, useSmallerFont);
            addressNotes = c;
          }
        }
        StringBuffer sb = new StringBuffer();
        sb.append(
            "<html>\n" +
            "<body>\n"
            );
        sb.append("<table>\n");
        if (!useVerticalLayout) sb.append("<tr>\n");
        if (!useVerticalLayout) sb.append("<td valign='top'>\n");
        else sb.append("<tr>\n");
        if (nameHtml != null || addrHtml != null) {
          if (nameHtml != null) {
            sb.append(nameHtml);
            if (!useVerticalLayout) sb.append("&nbsp;");
          }
          if (addrHtml != null) {
            sb.append(addrHtml);
            if (!useVerticalLayout) sb.append("&nbsp;");
          }
        } else {
          if (!useVerticalLayout) sb.append("&nbsp;");
        }
        if (!useVerticalLayout) sb.append("</td>\n");
        else sb.append("</tr>\n");
        if (!useVerticalLayout) sb.append("<td valign='top' class='SECTION' v>\n");
        else sb.append("<tr>\n");
        if (phoneHtml != null || emailHtml != null || webHtml != null) {
          if (emailHtml != null) {
            sb.append(emailHtml);
            if (!useVerticalLayout) sb.append("&nbsp;");
          }
          if (webHtml != null) {
            sb.append(webHtml);
            if (!useVerticalLayout) sb.append("&nbsp;");
          }
          if (phoneHtml != null) {
            sb.append(phoneHtml);
            if (!useVerticalLayout) sb.append("&nbsp;");
          }
        } else {
          if (!useVerticalLayout) sb.append("&nbsp;");
        }
        if (!useVerticalLayout) sb.append("</td>\n");
        else sb.append("</tr>\n");
        if (!useVerticalLayout) sb.append("</tr>\n");
        sb.append("</table>\n");
        if (notesHtml != null) {
          sb.append(notesHtml);
        }
        sb.append("</body>\n");
        sb.append("</html>");
        parsedAddrBody = sb.toString();
      }
    }
    return parsedAddrBody;
  }
  private String getHTMLforPart(XMLElement e, boolean useVerticalLayout, boolean useSmallerFont) {
    StringBuffer sb = new StringBuffer();
    String width = "";
    String size = useSmallerFont ? "size='-1'" : "";
    if (e != null) {
      Vector v = e.getChildren();
      if (v.size() > 0) {
        //sb.append("<table cellspacing='0' bgcolor='#FFFFCC'>\n");
        sb.append("<table cellspacing='0' cellpadding='2'>\n");
        sb.append("<tr><td colspan='2' valign='top' bgcolor='#B2D9F5'><font color='#003366' "+size+">\n");
        sb.append(e.getNameSafe());
        sb.append("</font></td></tr>\n");
        for (int i=0; i<v.size(); i++) {
          XMLElement c = (XMLElement) v.elementAt(i);
          width = useVerticalLayout && useSmallerFont ? "width='65'" : "width='85'";
          sb.append("<tr><td valign='top' bgcolor='#EAF4FC' "+width+"><font "+size+">\n");
          sb.append(c.getNameSafe() + ":");
          width = useVerticalLayout && useSmallerFont ? "width='150'" : "width='190'";
          sb.append("</font></td><td valign='top' bgcolor='#EAF4FC' "+width+"><font "+size+">\n");
          String content = c.getContent();
          if (e.getNameSafe().equals("Web") && c.getNameSafe().equals("Home")) {
            String contentLower = content.toLowerCase();
            String prefix = "";
            if (!contentLower.startsWith("http"))
              prefix = "http://";
            sb.append("<a href=" + prefix + content + ">" + content + "</a>\n");
          } else if (e.getNameSafe().equals("Emails") && c.getNameSafe().startsWith("E-mail")) {
            String nick = EmailRecord.getNick(content);
            String domain = EmailRecord.getDomain(content);
            if (nick != null && nick.length() > 0 && domain != null && domain.length() > 0) {
              sb.append("<a href='mailto:" + (nick + "@" + domain) + "'>");
              sb.append(Misc.encodePlainIntoHtml(content));
              sb.append("</a>");
            } else {
              sb.append(Misc.encodePlainIntoHtml(content));
            }
          } else {
            sb.append(Misc.encodePlainIntoHtml(content));
          }
          sb.append("</font></td></tr>\n");
          Object displayAs = c.getAttribute("displayAs");
          if (displayAs != null) {
            width = useVerticalLayout && useSmallerFont ? "width='65'" : "width='85'";
            sb.append("<tr><td valign='top' bgcolor='#EAF4FC' "+width+"><font "+size+">\n");
            sb.append("Display As:");
            width = useVerticalLayout && useSmallerFont ? "width='150'" : "width='190'";
            sb.append("</font></td><td valign='top' bgcolor='#EAF4FC' "+width+"><font "+size+">\n");
            sb.append(Misc.encodePlainIntoHtml(displayAs.toString()));
            sb.append("</font></td></tr>\n");
          }
        }
      } else if (e.getContent().length() > 0) {
        if (useVerticalLayout && useSmallerFont)
          width = "width='219'";
        else if (useVerticalLayout)
          width = "width='278'";
        else
          width = "width='560'";
        sb.append("<table "+width+" border='0'>\n");
        sb.append("<tr><td valign='top' bgcolor='#B2D9F5'><font color='#003366' "+size+">\n");
        sb.append(e.getNameSafe());
        sb.append("</font></td></tr>\n");
        sb.append("<tr><td valign='top'><font "+size+">\n");
        if (e.getAttribute("type").equals("text/plain")) {
          sb.append(Misc.encodePlainIntoHtml(e.getContent()));
        } else {
          sb.append(e.getContent());
        }
        sb.append("</font></td></tr>\n");
      }
      sb.append("</table>\n");
    }
    return sb.toString();
  }


  public static short sumAttachedFiles(MsgDataRecord[] msgDatas) {
    short sum = 0;
    if (msgDatas != null) {
      for (int i=0; i<msgDatas.length; i++) {
        if (msgDatas[i] != null && msgDatas[i].attachedFiles != null)
          sum += msgDatas[i].attachedFiles.shortValue();
      }
    }
    return sum;
  }

  public static short sumAttachedMsgs(MsgDataRecord[] msgDatas) {
    short sum = 0;
    if (msgDatas != null) {
      for (int i=0; i<msgDatas.length; i++) {
        if (msgDatas[i] != null && msgDatas[i].attachedMsgs != null)
          sum += msgDatas[i].attachedMsgs.shortValue();
      }
    }
    return sum;
  }


  /**
   * return true if this is an Internet Email or Web Email
   */
  public boolean isEmail() {
    return isEmail(importance);
  }
  public static boolean isEmail(Short importance) {
    return isEmail(importance.shortValue());
  }
  public static boolean isEmail(short importance) {
    return isRegularEmail(importance) || isSecureEmail(importance);
  }

  public boolean isRegularEmail() {
    return isRegularEmail(importance);
  }
  public static boolean isRegularEmail(Short importance) {
    return isRegularEmail(importance.shortValue());
  }
  public static boolean isRegularEmail(short importance) {
    return (importance & (IMPORTANCE_EMAIL_PLAIN | IMPORTANCE_EMAIL_HTML)) != 0;
  }

  public boolean isSecureEmail() {
    return isSecureEmail(importance);
  }
  public static boolean isSecureEmail(Short importance) {
    return isSecureEmail(importance.shortValue());
  }
  public static boolean isSecureEmail(short importance) {
    return (importance & (IMPORTANCE_ENC_EMAIL_PLAIN | IMPORTANCE_ENC_EMAIL_HTML)) != 0;
  }


  public boolean isHtml() {
    return isTypeAddress() || isHtmlMail();
  }
  public static boolean isHtmlMail(Short importance) {
    return isHtmlMail(importance.shortValue());
  }
  public static boolean isHtmlMail(short importance) {
    return (importance & IMPORTANCE_HTML_MASK) != 0;
  }
  public boolean isHtmlMail() {
    return isHtmlMail(importance.shortValue());
  }

  public static boolean isImpFYI(short imp) {
    return (imp & (IMPORTANCE_FYI_HTML | IMPORTANCE_FYI_PLAIN)) != 0 || (imp & IMPORTANCE_OLD_MASK) == IMPORTANCE_FYI_OLD;
  }
  public static boolean isImpNormal(short imp) {
    return (imp & (IMPORTANCE_NORMAL_HTML | IMPORTANCE_NORMAL_PLAIN)) != 0 || (imp & IMPORTANCE_OLD_MASK) == IMPORTANCE_NORMAL_OLD;
  }
  public static boolean isImpHigh(short imp) {
    return (imp & (IMPORTANCE_HIGH_HTML | IMPORTANCE_HIGH_PLAIN)) != 0 || (imp & IMPORTANCE_OLD_MASK) == IMPORTANCE_HIGH_OLD;
  }
  public static boolean isImpSystem(short imp) {
    return (imp & IMPORTANCE_OLD_MASK) == IMPORTANCE_SYSTEM_NOTICE;
  }

  public boolean isImpFYI() {
    return isImpFYI(importance.shortValue());
  }
  public boolean isImpNormal() {
    return isImpNormal(importance.shortValue());
  }
  public boolean isImpHigh() {
    return isImpHigh(importance.shortValue());
  }
  public boolean isImpSystem() {
    return isImpSystem(importance.shortValue());
  }

  public static boolean isTypeMessage(Short objType) {
    return isTypeMessage(objType.shortValue());
  }
  public static boolean isTypeMessage(short objType) {
    return objType == OBJ_TYPE_MSG;
  }
  public boolean isTypeMessage() {
    return objType.shortValue() == OBJ_TYPE_MSG;
  }


  public static boolean isTypeAddress(Short objType) {
    return isTypeAddress(objType.shortValue());
  }
  public static boolean isTypeAddress(short objType) {
    return objType == OBJ_TYPE_ADDR;
  }
  public boolean isTypeAddress() {
    return objType.shortValue() == OBJ_TYPE_ADDR;
  }


  /**
   * Should user have access to message body?
   * @return true iff user should have access to message body or user is UNSPECIFIED
   */
  public boolean isPrivilegedBodyAccess(Long userId, Date currentTime) {
    boolean access = false;
    // null is allowed, used in server-server communications
    if (userId == null || senderUserId.equals(userId)) {
      access = true;
    } else {
      boolean isRevokeBit = Misc.isBitSet(flags, MsgDataRecord.FLAG__REVOKED);
      if (isRevokeBit)
        access = false;
      else if (dateExpired == null)
        access = true;
      else
        access = dateExpired.compareTo(currentTime) > 0;
    }
    return access;
  }


  public String getEmailAddress() {
    if (name != null && name.length() > 0 && email != null && email.length() > 0) {
      if (email.indexOf('<') == -1 && email.indexOf('>') == -1)
        return name + " <" + email + ">";
      else
        return email;
    }
    else if (email != null && email.length() > 0)
      return email;
    else
      return "";
  }


  public ImageText getPriorityTextAndIcon() {
    return getPriorityTextAndIcon(importance.shortValue());
  }
  public static ImageText getPriorityTextAndIcon(short imp) {
    String text = "unknown";
    int icon = ImageNums.IMAGE_NONE;
    if (isImpHigh(imp)) {
      text = "High Priority";
      icon = ImageNums.PRIORITY_HIGH_SMALL;
    } else if (isImpFYI(imp)) {
      text = "Low Priority";
      icon = ImageNums.PRIORITY_LOW_SMALL;
    } else if (isImpSystem(imp)) {
      text = "System Notification";
      icon = ImageNums.LIGHT_ON_SMALL;
    } else if (isImpNormal(imp)) {
      text = "Normal Priority";
      icon = ImageNums.IMAGE_NONE;
    } else if (isRegularEmail(imp)) {
      text = "Normal Priority Regular Email";
    } else if (isSecureEmail(imp)) {
      text = "Normal Priority Secure Email";
    }
    return new ImageText(icon, text);
  }

  public ImageText getSecurityTextAndIcon() {
    return getSecurityTextAndIcon(importance.shortValue());
  }
  public static ImageText getSecurityTextAndIcon(short imp) {
    String text = "unknown";
    int icon = ImageNums.IMAGE_NONE;
    if (isRegularEmail(imp)) {
      text = "Regular Email / Stored Encrypted with AES(256)";
      icon = ImageNums.LOCK_OPEN_SMALL;
    } else if (isSecureEmail(imp)) {
      text = "Secure and Encrypted with AES(256) / Web Delivered";
      icon = ImageNums.LOCK_CLOSED_WEB_SMALL;
    } else {
      text = "Secure and Encrypted with AES(256)";
      icon = ImageNums.LOCK_CLOSED_SMALL;
    }
    return new ImageText(icon, text);
  }

  /**
   * Encodes and caches the message text for display in HTML renderers
   * where it should look like PLAIN.
   */
  public String getEncodedHTMLData() {
    if (encodedHTMLData == null || encodedHTMLData.length() == 0) {
     encodedHTMLData = Misc.encodePlainIntoHtml(getText());
    }
    return encodedHTMLData;
  }

  public void makeBrief() {
    setEncText(null);
    setEncSignedDigest(null);
    setEncEncDigest(null);
    setSendPrivKeyId(null);
    signedDigest = null;
    // recipients should stay because they are used in 'Sent' folder
  }
  public static void makeBriefs(MsgDataRecord[] dataRecords) {
    if (dataRecords != null)
      for (int i=0; i<dataRecords.length; i++)
        dataRecords[i].makeBrief();
  }

  public void merge(Record updated) {
  if (updated instanceof MsgDataRecord) {
      MsgDataRecord record = (MsgDataRecord) updated;

      if (record.msgId            != null) msgId            = record.msgId;
      if (record.objType          != null) objType          = record.objType;
      if (record.replyToMsgId     != null) replyToMsgId     = record.replyToMsgId;
      if (record.importance       != null) importance       = record.importance;
      if (record.attachedFiles    != null) attachedFiles    = record.attachedFiles;
      if (record.attachedMsgs     != null) attachedMsgs     = record.attachedMsgs;
      if (record.senderUserId     != null) senderUserId     = record.senderUserId;
      if (record.encSubject       != null) encSubject       = record.encSubject;
      if (record.encText          != null) encText          = record.encText;
      if (record.flags            != null) flags            = record.flags;
      if (record.encSignedDigest  != null) encSignedDigest  = record.encSignedDigest;
      if (record.encEncDigest     != null) encEncDigest     = record.encEncDigest;
      if (record.sendPrivKeyId    != null) sendPrivKeyId    = record.sendPrivKeyId;
      if (record.dateCreated      != null) dateCreated      = record.dateCreated;
      // Exception here:  flags and dateExpired always MUST come in pairs... (this is where REVOKE flag is)
      // if flags (non-nullable) is not null then copy the nullable dateExpired variable
      if (record.flags            != null) dateExpired      = record.dateExpired;
      if (record.recordSize       != null) recordSize       = record.recordSize;
      if (record.bodyPassHash     != null) bodyPassHash     = record.bodyPassHash;
      if (record.bodyPassHint     != null) bodyPassHint     = record.bodyPassHint;
      if (record.rawRecipients != null) {
        rawRecipients    = record.rawRecipients;
        recipients       = record.recipients;
        fromEmailAddress = record.fromEmailAddress;
      }

      // un-sealed data
      if (record.subject            != null) subject          = record.subject;
      // if encText is present, copy the possibly null textBody and textErr variables
      if (record.encText            != null) textBody         = record.textBody;
      if (record.encText            != null) textErr          = record.textErr;
      if (record.encText            != null) encodedHTMLData  = record.encodedHTMLData;
      if (record.digest             != null) digest           = record.digest;
      if (record.signedDigest       != null) signedDigest     = record.signedDigest;
      if (record.encDigest          != null) encDigest        = record.encDigest;

      // cached data
      if (record.name               != null) name             = record.name;
      if (record.email              != null) email            = record.email;
      if (record.fileAs             != null) fileAs           = record.fileAs;
      if (record.phoneB             != null) phoneB           = record.phoneB;
      if (record.phoneH             != null) phoneH           = record.phoneH;
      if (record.addressBody        != null) addressBody      = record.addressBody;
      if (record.addressNotes       != null) addressNotes     = record.addressNotes;

      if (record.digestOk           != null) digestOk         = record.digestOk;
      if (record.encDigestOk        != null) encDigestOk      = record.encDigestOk;

    }
    else
      super.mergeError(updated);
  }


  public String toString() {
    return "[MsgDataRecord"
      + ": msgId="              + msgId
      + ", objType="            + objType
      + ", replyToMsgId="       + replyToMsgId
      + ", importance="         + importance
      + ", attachedFiles="      + attachedFiles
      + ", attachedMsgs="       + attachedMsgs
      + ", senderUserId="       + senderUserId
      + ", rawRecipients="      + Misc.objToStr(rawRecipients)
      + ", encSubject="         + encSubject
      + ", encText="            + encText
      + ", flags="              + flags
      + ", encSignedDigest="    + encSignedDigest
      + ", encEncDigest="       + encEncDigest
      + ", sendPrivKeyId="      + sendPrivKeyId
      + ", dateCreated="        + dateCreated
      + ", dateExpired="        + dateExpired
      + ", recordSize="         + recordSize
      + ", bodyPassHash="       + bodyPassHash
      + ", bodyPassHint="       + bodyPassHint
      + ", cached data >> "
      + ", name="               + name
      + ", email="              + email
      + ", fileAs="             + fileAs
      + ", phoneB="             + phoneB
      + ", phoneH="             + phoneH
      + ", addressBody="        + addressBody
      + ", addressNotes="       + addressNotes
      + ", un-sealed data >> "
      + ", recipients="         + Misc.objToStr(recipients)
      + ", subject="            + Misc.objToStr(subject)
      + ", textBody="           + (textBody != null && textBody.length() < 255 ? Misc.objToStr(textBody) : (textBody != null ? "too long length="+textBody.length() : null))
      + ", textErr="            + Misc.objToStr(textErr)
      + ", digest="             + digest
      + ", signedDigest="       + signedDigest
      + ", encDigest="          + encDigest
      + ", symmetricBodyKey="   + symmetricBodyKey
      + ", digestOk="           + digestOk
      + ", encDigestOk="        + encDigestOk
      + "]";
  }

  public void setId(Long id) {
    msgId = id;
  }

}
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

package com.CH_co.service.records;

import javax.swing.Icon;
import java.sql.Timestamp;
import java.util.*;

import com.CH_co.cryptx.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

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
 * <b>$Revision: 1.27 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class MsgLinkRecord extends Record implements LinkRecordI {

  public static final short STATUS_FLAG__DEFAULT = 0;
  public static final short STATUS_FLAG__READ = 1;
  public static final short STATUS_FLAG__REPLIED = 2;
  public static final short STATUS_FLAG__FORWARDED = 4;
  public static final short STATUS_FLAG__APPROVED_FOR_NATIVE_PREVIEW_MODE = 8;
  public static final short STATUS_FLAG__HIDDEN_THROUGH_BCC = 16;

  public static final short RECIPIENT_TYPE_TO = 0;
  public static final short RECIPIENT_TYPE_CC = 1;
  public static final short RECIPIENT_TYPE_BCC = 2;
  public static final short[] RECIPIENT_TYPES = new short[] { RECIPIENT_TYPE_TO, RECIPIENT_TYPE_CC, RECIPIENT_TYPE_BCC };

  // MsgLinkRecord
  public Long msgLinkId;
  public Long msgId;
  public Long ownerObjId;
  public Short ownerObjType; // typically Record.RECORD_TYPE_FOLDER or Record.RECORD_TYPE_MESSAGE
  private BA encSymmetricKey; // this maybe BAAsyCipherBlock or BASymCipherBulk
  private Long recPubKeyId;
  public Short status;
  public Timestamp dateCreated;
  public Timestamp dateUpdated;
  public Timestamp dateDelivered;

  private String postRenderingCache;


  /** unwrapped data */
  private BASymmetricKey symmetricKey;


  // Used to indent threaded messages, and set by the sorter.
  private Integer sortThreadLayer;

  /** Creates new MsgLinkRecord */
  public MsgLinkRecord() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgLinkRecord.class, "MsgLinkRecord()");
    if (trace != null) trace.exit(MsgLinkRecord.class);
  }

  public Long getId() {
    return msgLinkId;
  }

  public Icon getIcon() {
    int image = -1;
    Icon icon = null;
    short st = status.shortValue();
    if ((st & STATUS_FLAG__FORWARDED) != 0 && (st & STATUS_FLAG__REPLIED) != 0)
      image = ImageNums.MAIL_RPYFWD16;
    else if ((st & STATUS_FLAG__REPLIED) != 0)
      image = ImageNums.MAIL_RPY16;
    else if ((st & STATUS_FLAG__FORWARDED) != 0)
      image = ImageNums.MAIL_FWD16;
    else if ((st & STATUS_FLAG__READ) != 0)
      image = ImageNums.MAIL_READ16;
    else 
      image = ImageNums.MAIL_UNREAD16;
    if (image >= 0)
      icon = Images.get(image);
    return icon;
  }

  public String getStatusName() {
    String statusName = "unknown";
    short st = status.shortValue();
    if ((st & STATUS_FLAG__FORWARDED) != 0 && (st & STATUS_FLAG__REPLIED) != 0)
      statusName = "Replied and Forwarded";
    else if ((st & STATUS_FLAG__REPLIED) != 0)
      statusName = "Replied";
    else if ((st & STATUS_FLAG__FORWARDED) != 0)
      statusName = "Forwarded";
    else 
      statusName = "Normal";
    return statusName;
  }

  public void setEncSymmetricKey (BA encSymmetricKey)          { this.encSymmetricKey   = encSymmetricKey;   }
  public void setRecPubKeyId     (Long recPubKeyId)            { this.recPubKeyId       = recPubKeyId;       }
  public void setSymmetricKey    (BASymmetricKey symmetricKey) { this.symmetricKey      = symmetricKey;      }

  public BA             getEncSymmetricKey() { return encSymmetricKey;    }
  public Long           getRecPubKeyId()     { return recPubKeyId;        }
  public BASymmetricKey getSymmetricKey()    { return symmetricKey;       }


  public void setPostRenderingCache(String value) {
    postRenderingCache = value;
  }
  public String getPostRenderingCache() {
    return postRenderingCache;
  }
  public void clearPostRenderingCache() {
    setPostRenderingCache(null);
  }

  public static void clearPostRenderingCache(MsgLinkRecord[] mLinks) {
    if (mLinks != null) {
      for (int i=0; i<mLinks.length; i++) {
        mLinks[i].clearPostRenderingCache();
      }
    }
  }

  /**
   * Seals the <code> symmetricKey </code> to <code> encSymmetricKey </code> 
   * using the sealant object which is the owning object's symmetric key (case of postings and attachments).
   */
  public void seal(BASymmetricKey ownerSymKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgLinkRecord.class, "seal(BASymmetricKey ownerSymKey)");

    if (ownerObjType.shortValue() != Record.RECORD_TYPE_FOLDER && ownerObjType.shortValue() != Record.RECORD_TYPE_MESSAGE)
      throw new IllegalArgumentException("Cannot seal a non folder/message owned message link with symmetric key!");

    try {
      SymmetricBulkCipher symCipher = new SymmetricBulkCipher(ownerSymKey);
      encSymmetricKey = symCipher.bulkEncrypt(symmetricKey);
      recPubKeyId = null;
      super.seal();
    } catch (Throwable t) {
      if (trace != null) trace.exception(MsgLinkRecord.class, 100, t);
      t.printStackTrace();
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(MsgLinkRecord.class);
  }


  /**
   * Seals the <code> symmetricKey </code> to <code> encSymmetricKey </code> 
   * using the sealant object which is the recipients public key (case of personal message).
   */
  public void seal(KeyRecord recipientPubKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgLinkRecord.class, "seal(KeyRecord recipientPubKey)");
    if (ownerObjType != null || ownerObjId != null)
      throw new IllegalArgumentException("Messages directed to user recipients must not have owner type and id assigned!");

    try {
      AsymmetricBlockCipher asyCipher = new AsymmetricBlockCipher();
      encSymmetricKey = asyCipher.blockEncrypt(recipientPubKey.plainPublicKey, symmetricKey.toByteArray());
      recPubKeyId = recipientPubKey.keyId;
      super.seal();
    } catch (Throwable t) {
      if (trace != null) trace.exception(MsgLinkRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(MsgLinkRecord.class);
  }


  /**
   * Unseals the <code> encSymmetricKey </code> into <code> symmetricKey </code> 
   * using the unSealant object which is the owning object's symmetric key (case of postings and attachments).
   */
  public void unSeal(BASymmetricKey ownerSymKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgLinkRecord.class, "unSeal(BASymmetricKey ownerSymKey)");
    if (trace != null) trace.args(ownerSymKey);

    try {
      SymmetricBulkCipher symCipher = new SymmetricBulkCipher(ownerSymKey);
      byte[] encBytes = encSymmetricKey.toByteArray();
      symmetricKey = new BASymmetricKey(symCipher.bulkDecrypt(encBytes, 0, encBytes.length));
      super.unSeal();
      clearPostRenderingCache(); // after unSealing, if any field was updated, it should be re-prepared for rendering...
    } catch (Throwable t) {
      if (trace != null) trace.exception(MsgLinkRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(MsgLinkRecord.class);
  }

  /**
   * Unseals the <code> encSymmetricKey </code> into <code> symmetricKey </code> 
   * using the unSealant object which is the recipient's private key (case of personal message).
   */
  public void unSeal(KeyRecord privateKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgLinkRecord.class, "unSeal(KeyRecord privateKey)");
    if (!privateKey.keyId.equals(recPubKeyId))
      throw new IllegalArgumentException("Specified private key record cannot decrypt this message!");

    try {
      AsymmetricBlockCipher asyCipher = new AsymmetricBlockCipher();
      symmetricKey = new BASymmetricKey(asyCipher.blockDecrypt(privateKey.getPrivateKey(), encSymmetricKey.toByteArray()));
      super.unSeal();
      clearPostRenderingCache(); // after unSealing, if any field was updated, it should be re-prepared for rendering...
    } catch (Throwable t) {
      if (trace != null) trace.exception(MsgLinkRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(MsgLinkRecord.class);
  }


  public static Long[] getMsgIDs(MsgLinkRecord[] msgLinks) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgLinkRecord.class, "getMsgIDs(MsgLinkRecord[] msgLinks)");
    if (trace != null) trace.args(msgLinks);

    Long[] msgIDs = null;
    if (msgLinks != null) {
      msgIDs = new Long[msgLinks.length];
      for (int i=0; i<msgLinks.length; i++) {
        msgIDs[i] = msgLinks[i].msgId;
      }
      msgIDs = (Long[]) ArrayUtils.removeDuplicates(msgIDs);
    }

    if (trace != null) trace.exit(MsgLinkRecord.class, msgIDs);
    return msgIDs;
  }


  public static Long[] getOwnerObjIDs(MsgLinkRecord[] msgLinks, short ownerType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgLinkRecord.class, "getOwnerObjIDs(MsgLinkRecord[] msgLinks, short ownerType)");
    if (trace != null) trace.args(msgLinks);
    if (trace != null) trace.args(ownerType);

    Long[] ownerObjIDs = null;
    if (msgLinks != null) {
      Vector msgLinksV = null;
      for (int i=0; i<msgLinks.length; i++) {
        if (msgLinks[i].ownerObjType.shortValue() == ownerType) {
          Long id = msgLinks[i].ownerObjId;
          if (msgLinksV == null) msgLinksV = new Vector();
          if (!msgLinksV.contains(id))
            msgLinksV.addElement(id);
        }
      }
      ownerObjIDs = (Long[]) ArrayUtils.toArray(msgLinksV, Long.class);
    }

    if (trace != null) trace.exit(MsgLinkRecord.class, ownerObjIDs);
    return ownerObjIDs;
  }

  /**
   * @return index of fould message link (using collection's iterator) with given message id, or -1 if not found.
   */
  public static int findLinkByMsgId(AbstractCollection collection, Long msgId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgLinkRecord.class, "findLinkByMsgId(AbstractCollection collection, Long msgId)");
    if (trace != null) trace.args(collection, msgId);
    int index = -1;
    if (collection != null) {
      Iterator iter = collection.iterator();
      int i = 0;
      while (iter.hasNext()) {
        MsgLinkRecord mLink = (MsgLinkRecord) iter.next();
        if (mLink != null && mLink.msgId.equals(msgId)) {
          index = i;
          break;
        }
        i ++;
      }
    }
    if (trace != null) trace.exit(MsgLinkRecord.class, index);
    return index;
  }

  /**
   * @return index (in the selectedIndexes) of fould message link with given message id, or -1 if not found.
   */
  public static int findLinkByMsgId(AbstractCollection selectedIndexes, Vector collection, Long msgId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgLinkRecord.class, "findLinkByMsgId(AbstractCollection selectedIndexes, Vector collection, Long msgId)");
    if (trace != null) trace.args(selectedIndexes, collection, msgId);
    int index = -1;
    if (collection != null) {
      Iterator iter = selectedIndexes.iterator();
      int i = 0;
      while (iter.hasNext()) {
        int elementIndex = ((Integer) iter.next()).intValue();
        MsgLinkRecord mLink = (MsgLinkRecord) collection.elementAt(elementIndex);
        if (mLink != null && mLink.msgId.equals(msgId)) {
          index = i;
          break;
        }
        i ++;
      }
    }
    if (trace != null) trace.exit(MsgLinkRecord.class, index);
    return index;
  }

  public void setSortThreadLayer(int layer) {
    setSortThreadLayer(new Integer(layer));
  }
  public void setSortThreadLayer(Integer layer) {
    sortThreadLayer = layer;
  }
  public int getSortThreadLayer() {
    return sortThreadLayer != null ? sortThreadLayer.intValue() : 0;
  }

  public void merge(Record updated) {
    if (updated instanceof MsgLinkRecord) {
      MsgLinkRecord record = (MsgLinkRecord) updated;
      // MsgLinkRecord
      if (record.msgLinkId         != null) msgLinkId        = record.msgLinkId;
      if (record.msgId             != null) msgId            = record.msgId;
      if (record.ownerObjId        != null) ownerObjId       = record.ownerObjId;
      if (record.ownerObjType      != null) ownerObjType     = record.ownerObjType;
      // When converting to symmetric encryption, we need to nullify the key id so that update stats won't cause asymetric decryption again.
      // This is required because recPubKeyId is NULLable field
      if (record.encSymmetricKey != null) {
        encSymmetricKey  = record.encSymmetricKey;
        recPubKeyId      = record.recPubKeyId;
      }
      if (record.status            != null) status           = record.status;
      if (record.dateCreated       != null) dateCreated      = record.dateCreated;
      if (record.dateUpdated       != null) dateUpdated      = record.dateUpdated;
      if (record.dateDelivered     != null) dateDelivered    = record.dateDelivered;

      // un-sealed data
      if (record.symmetricKey      != null) symmetricKey     = record.symmetricKey;
      if (record.sortThreadLayer   != null) sortThreadLayer  = record.sortThreadLayer;
    }
    else
      super.mergeError(updated);

    postRenderingCache = null;
  }


  public String toString() {
    return "[MsgLinkRecord"
      + ": msgLinkId="      + msgLinkId
      + ", msgId="          + msgId
      + ", ownerObjId="     + ownerObjId
      + ", ownerObjType="   + ownerObjType
      + ", encSymmetricKey="+ encSymmetricKey
      + ", recPubKeyId="    + recPubKeyId
      + ", status="         + status
      + ", dateCreated="    + dateCreated
      + ", dateUpdated="    + dateUpdated
      + ", dateDelivered="  + dateDelivered
      + ", un-sealed data >> "
      + ", symmetricKey="   + symmetricKey
      + "]";
  }

  /**********************
   * LinkRecordI methods
   *********************/
  public Long getObjId() {
    return msgId;
  }

  public Long[] getObjIDs(LinkRecordI[] links) {
    return getMsgIDs((MsgLinkRecord[]) links);
  }

  public Short getOwnerObjType() {
    return ownerObjType;
  }

  public Long getOwnerObjId() {
    return ownerObjId;
  }

  public Long[] getOwnerObjIDs(LinkRecordI[] links, short ownerType) {
    return getOwnerObjIDs((MsgLinkRecord[]) links, ownerType);
  }

  public void setId(Long id) {
    msgLinkId = id;
  }

}
/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
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

import com.CH_co.cryptx.*;
import com.CH_co.trace.Trace;
import java.sql.Timestamp;
import javax.swing.Icon;

/**
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class KeyRecoveryRecord extends Record {

  public Long keyRecovId;
  public Long keyId;
  public Long masterUID;
  public Long masterKeyId;
  private BAAsyCipherBlock encSymKey; // asymmetrically encrypted symmetric key
  private BASymCipherBlock encPvtKey; // a symmetric cipher block (NOT an AsymmetricCipherBlock!)
  public Timestamp dateCreated;
  public Timestamp dateUpdated;

  // plain data
  private RSAPrivateKey privateKey;

  public Long getId() {
    return keyRecovId;
  }
  public Icon getIcon() {
    return null;
  }

  public void setEncSymKey(BAAsyCipherBlock encSymKey)  { this.encSymKey = encSymKey; }
  public void setEncPvtKey(BASymCipherBlock encPvtKey)  { this.encPvtKey = encPvtKey; }
  public void setPvtKey   (RSAPrivateKey    pvtKey)     { this.privateKey = pvtKey;   }

  public BAAsyCipherBlock getEncSymKey()  { return encSymKey;   }
  public BASymCipherBlock getEncPvtKey()  { return encPvtKey;   }
  public RSAPrivateKey    getPvtKey()     { return privateKey;  }

  /**
   * Seals the private key of <code> pvtKeyRecord </code> into <code> encPrivateKey </code>
   * using the sealant object which is the <code> symKey </code>.
   * Also seals the <code> symKey </code> into <code> encSymKey </code>
   * using the sealant object which is the public key of <code> masterPubKeyRecord </code>.
   * Also sets keyId, masterKeyId.
   */
  public synchronized void seal(KeyRecord masterPubKeyRecord, KeyRecord pvtKeyRecord, BASymmetricKey symKey) {
    seal(masterPubKeyRecord, pvtKeyRecord.keyId, pvtKeyRecord.getPrivateKey(), symKey);
  }
  public synchronized void seal(KeyRecord masterPubKeyRecord, Long privateKeyId, RSAPrivateKey privateKey, BASymmetricKey symKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(KeyRecoveryRecord.class, "seal()");

    try {
      SymmetricSmallBlockCipher symCipher = new SymmetricSmallBlockCipher(symKey);
      BASymCipherBlock tempEncPvtKey = symCipher.blockEncrypt(new BASymPlainBlock(privateKey.objectToBytes()));
      BAAsyCipherBlock tempEncSymKey = new BAAsyPlainBlock(symKey).encrypt(masterPubKeyRecord.plainPublicKey, false);

      super.seal();

      keyId = privateKeyId;
      masterUID = masterPubKeyRecord.ownerUserId;
      masterKeyId = masterPubKeyRecord.keyId;
      encSymKey = tempEncSymKey;
      encPvtKey = tempEncPvtKey;
    } catch (Throwable t) {
      if (trace != null) trace.exception(KeyRecoveryRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(KeyRecoveryRecord.class);
  }

  /**
   * Unseals the <code> encPrivateKey </code> into <code> privateKey </code>
   * using the unSealant object which is the <code> encodedPassword </code>.
   */
  public synchronized void unSeal(KeyRecord masterPvtKeyRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(KeyRecoveryRecord.class, "unSeal()");

    try {
      BAAsyPlainBlock symKey = encSymKey.decrypt(masterPvtKeyRecord.getPrivateKey(), false);
      SymmetricSmallBlockCipher symCipher = new SymmetricSmallBlockCipher(symKey);
      BASymPlainBlock plainPvtKey = symCipher.blockDecrypt(encPvtKey);

      super.unSeal();

      privateKey = RSAPrivateKey.bytesToObject(plainPvtKey.toByteArray());
    } catch (Throwable t) {
      if (trace != null) trace.exception(KeyRecoveryRecord.class, 100, t);
      throw new SecurityException(t.toString());
    }

    if (trace != null) trace.exit(KeyRecoveryRecord.class);
  }

  public void merge(Record updated) {
    if (updated instanceof KeyRecoveryRecord) {
      KeyRecoveryRecord record = (KeyRecoveryRecord) updated;
      if (record.keyRecovId   != null) keyRecovId   = record.keyRecovId;
      if (record.keyId        != null) keyId        = record.keyId;
      if (record.masterUID    != null) masterUID    = record.masterUID;
      if (record.masterKeyId  != null) masterKeyId  = record.masterKeyId;
      if (record.encSymKey    != null) encSymKey    = record.encSymKey;
      if (record.encPvtKey    != null) encPvtKey    = record.encPvtKey;
      if (record.dateCreated  != null) dateCreated  = record.dateCreated;
      if (record.dateUpdated  != null) dateUpdated  = record.dateUpdated;

      // un-sealed objects
      if (record.privateKey   != null) privateKey   = record.privateKey;
    }
    else
      super.mergeError(updated);
  }

  public String toString() {
    return "[KeyRecoveryRecord"
      + ": keyRecovId="       + keyRecovId
      + ", keyId="            + keyId
      + ", masterUID="        + masterUID
      + ", masterKeyId="      + masterKeyId
      + ", encSymmetricKey="  + encSymKey
      + ", encPrivateKey="    + encPvtKey
      + ", created="          + dateCreated
      + ", updated="          + dateUpdated
      + ", un-sealed data >> "
      + ", privateKey="       + privateKey
      + "]";
  }

  public void setId(Long id) {
    keyRecovId = id;
  }

}
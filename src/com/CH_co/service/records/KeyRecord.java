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

import java.sql.Timestamp;

import com.CH_co.trace.Trace;
import com.CH_co.cryptx.*;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>  
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class KeyRecord extends Record { // implicit no-argument constructor

  // for production set DEBUG to false
  public static boolean DEBUG__ALLOW_SHORT_KEYS = false;
  public static final int DEBUG__SHORTEST_KEY = 696;
  public static final int DEBUG__MIN_CERTAINTY = 8;

  // accounts key sizes and prime number generation certainty
  public static final int MIN__KEY_LENGTH = 1024;
  //public static final int MIN__KEY_LENGTH = 2048;
  public static final int DEFAULT__KEY_LENGTH = 1024;
  //public static final int DEFAULT__KEY_LENGTH = 2048;
  public static final int MAX__KEY_LENGTH = 4096;
  public static final int MIN__CERTAINTY = 128;
  public static final int DEFAULT__CERTAINTY = 128;
  public static final int MAX__CERTAINTY = 256;


  public Long keyId;
  public Long folderId;
  public Long ownerUserId;
  private BASymCipherBlock encPrivateKey;         // a symmetric cipher block (NOT an AsymmetricCipherBlock!)
  public RSAPublicKey plainPublicKey;
  public Timestamp dateCreated;
  public Timestamp dateUpdated;

  // plain data
  private RSAPrivateKey privateKey;


  public Long getId() { 
    return keyId;
  }

  public int getIcon() {
    return ImageNums.KEY16;
  }

  public void setEncPrivateKey  (BASymCipherBlock encPrivateKey)  { this.encPrivateKey = encPrivateKey; }
  public void setPrivateKey     (RSAPrivateKey    privateKey)     { this.privateKey = privateKey;       }

  public BASymCipherBlock getEncPrivateKey()  { return encPrivateKey; }
  public RSAPrivateKey    getPrivateKey()     { return privateKey;    }

  public String verboseInfo() {
    return  "RSA " + (plainPublicKey.getMaxBlock() * 8) + " bits, key ID " + keyId;
  }

  /**
   * Seals the <code> privateKey </code> into <code> encPrivateKey </code> 
   * using the sealant object which is the <code> encodedPassword </code>.
   */
  public synchronized void seal(BAEncodedPassword encodedPassword) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(KeyRecord.class, "seal()");

    // if anything to do
    if (privateKey != null) {
      super.seal();
      try {
        SymmetricSmallBlockCipher symCipher = new SymmetricSmallBlockCipher(encodedPassword);
        encPrivateKey = symCipher.blockEncrypt(new BASymPlainBlock(privateKey.objectToBytes()));
      } catch (Throwable t) {
        if (trace != null) trace.exception(KeyRecord.class, 100, t);
        throw new SecurityException(t.getMessage());
      }
    }

    if (trace != null) trace.exit(KeyRecord.class);
  }

  /**
   * Unseals the <code> encPrivateKey </code> into <code> privateKey </code> 
   * using the unSealant object which is the <code> encodedPassword </code>.
   */
  public synchronized void unSeal(BAEncodedPassword encodedPassword) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(KeyRecord.class, "unSeal()");

    // if anything to do
    if (encPrivateKey != null && encPrivateKey.toByteArray() != null) {
      super.unSeal();
      try {
        SymmetricSmallBlockCipher symCipher = new SymmetricSmallBlockCipher(encodedPassword);
        privateKey = RSAPrivateKey.bytesToObject(symCipher.blockDecrypt(encPrivateKey).toByteArray());
      } catch (Throwable t) {
        if (trace != null) trace.exception(KeyRecord.class, 100, t);
        throw new SecurityException(t.toString());
      }
    }

    if (trace != null) trace.exit(KeyRecord.class);
  }

  public static Long[] getOwnerUserIDs(KeyRecord[] records) {
    Long[] IDs = null;
    if (records != null) {
      IDs = new Long[records.length];
      for (int i=0; i<records.length; i++) {
        IDs[i] = records[i].ownerUserId;
      }
      IDs = (Long[]) ArrayUtils.removeDuplicates(IDs);
    }
    return IDs;
  }

  public void merge(Record updated) {
    if (updated instanceof KeyRecord) {
      KeyRecord record = (KeyRecord) updated;
      if (record.keyId             != null) keyId            = record.keyId;
      if (record.folderId          != null) folderId         = record.folderId;
      if (record.ownerUserId       != null) ownerUserId      = record.ownerUserId;
      if (record.encPrivateKey     != null) encPrivateKey    = record.encPrivateKey;
      if (record.plainPublicKey    != null) plainPublicKey   = record.plainPublicKey;
      if (record.dateCreated       != null) dateCreated      = record.dateCreated;
      if (record.dateUpdated       != null) dateUpdated      = record.dateUpdated;

      // un-sealed objects
      if (record.privateKey        != null) privateKey       = record.privateKey;
    }
    else
      super.mergeError(updated);
  }

  public String toString() {
    return "[KeyRecord"
      + ": keyId="            + keyId
      + ", folderId="         + folderId
      + ", ownerUserId="      + ownerUserId
      + ", encPrivateKey="    + encPrivateKey
      + ", plainPublicKey="   + plainPublicKey
      + ", dateCreated="      + dateCreated
      + ", dateUpdated="      + dateUpdated
      + ", un-sealed data >> "
      + ", privateKey="       + privateKey
      + "]";
  }

  public void setId(Long id) {
    keyId = id;
  }

}
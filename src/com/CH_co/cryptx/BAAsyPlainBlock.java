/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.cryptx;

import com.CH_co.trace.Trace;
import java.security.GeneralSecurityException;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.11 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class BAAsyPlainBlock extends BA {

  /** Creates new AsymmetricPlainBlock */
  public BAAsyPlainBlock(byte[] asymmetricCipherBlock) {
    super(asymmetricCipherBlock);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BAAsyPlainBlock.class, "BAAsyPlainBlock(byte[])");
    if (trace != null) trace.exit(BAAsyPlainBlock.class);
  }

  public BAAsyPlainBlock(BA ba) {
    this(ba.toByteArray());
  }
  
  
  /***************************************************************************/
  /************** C o n v i n i e n c e   M e t h o d s **********************/
  /***************************************************************************/
  
  
  /**
   ************** C o n v i n i e n c e   M e t h o d ************************
   * Shortcut for AsymmetricBlockCipher.blockEncrypt()
   * @return the encrypted cipher block
   */
  public BAAsyCipherBlock encrypt(RSAPublicKey publicKey, boolean withDigest) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BAAsyPlainBlock.class, "encrypt(RSAPublicKey)");
    BAAsyCipherBlock cipherBlock;
    try {
      cipherBlock = new AsymmetricBlockCipher().blockEncrypt(publicKey, toByteArray(), withDigest);
    } catch (GeneralSecurityException e) {
      if (trace != null) trace.exception(BAAsyPlainBlock.class, 100, e);
      throw new SecurityException(e.toString());
    }
    if (trace != null) trace.exit(BAAsyPlainBlock.class, cipherBlock);
    return cipherBlock;
  }
  public BAAsyCipherBlock encrypt(RSAPublicKey publicKey) {
    return encrypt(publicKey, true);
  }
  
  
  /**
   ************** C o n v i n i e n c e   M e t h o d ************************
   * Shortcut for AsymmetricBlockCipher.signBlock()
   * @return the encrypted cipher block
   */
  public BAAsyCipherBlock signBlock(RSAPrivateKey privateKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BAAsyPlainBlock.class, "signBlock(RSAPrivateKey)");
    BAAsyCipherBlock cipherBlock;
    try {
      cipherBlock = new AsymmetricBlockCipher().signBlock(privateKey, toByteArray());
    } catch (GeneralSecurityException e) {
      if (trace != null) trace.exception(BAAsyPlainBlock.class, 100, e);
      throw new SecurityException(e.toString());
    }
    if (trace != null) trace.exit(BAAsyPlainBlock.class, cipherBlock);
    return cipherBlock;
  }
  
  
}
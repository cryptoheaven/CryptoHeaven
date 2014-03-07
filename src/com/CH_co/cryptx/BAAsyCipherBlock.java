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
 * <b>$Revision: 1.12 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class BAAsyCipherBlock extends BA {

  /** Creates new BAAsyCipherBlock */
  public BAAsyCipherBlock(byte[] asymmetricCipherBlock) {
    super(asymmetricCipherBlock);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BAAsyCipherBlock.class, "BAAsyCipherBlock(byte[])");
    if (trace != null) trace.exit(BAAsyCipherBlock.class);
  }
  /** Creates new BAAsyCipherBlock */
  public BAAsyCipherBlock(BA content) {
    super(content);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BAAsyCipherBlock.class, "BAAsyCipherBlock()");
    if (trace != null) trace.exit(BAAsyCipherBlock.class);
  }
  
  /***************************************************************************/
  /************** C o n v i n i e n c e   M e t h o d s **********************/
  /***************************************************************************/
  
  
  /**
   ************** C o n v i n i e n c e   M e t h o d ************************
   * Shortcut to AsymmetricBlockCipher.blockDecrypt()
   * @return original plain data
   */
  public BAAsyPlainBlock decrypt(RSAPrivateKey privateKey, boolean withDigest) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BAAsyCipherBlock.class, "decrypt(RSAPrivateKey)");
    BAAsyPlainBlock plainBlock;
    try {
      plainBlock = new AsymmetricBlockCipher().blockDecrypt(privateKey, toByteArray(), withDigest);
    } catch (GeneralSecurityException e) {
      if (trace != null) trace.exception(BAAsyCipherBlock.class, 100, e);
      throw new SecurityException(e.toString());
    }
    if (trace != null) trace.exit(BAAsyCipherBlock.class, plainBlock);
    return plainBlock;
  }
  public BAAsyPlainBlock decrypt(RSAPrivateKey privateKey) {
    return decrypt(privateKey, true);
  }
  
  
  
  /** 
   ************** C o n v i n i e n c e   M e t h o d ************************
   * Shortcut to AsymmetricBlockCipher.verifySignature()
   * @return the original block which was signed.
   */
  public BAAsyPlainBlock verifySignature(RSAPublicKey publicKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BAAsyCipherBlock.class, "verifySignature(RSAPublicKey)");
    BAAsyPlainBlock plainBlock;
    try {
      plainBlock = new AsymmetricBlockCipher().verifySignature(publicKey, toByteArray());
    } catch (GeneralSecurityException e) {
      if (trace != null) trace.exception(BAAsyCipherBlock.class, 100, e);
      throw new SecurityException(e.toString());
    }
    if (trace != null) trace.exit(BAAsyCipherBlock.class, plainBlock);
    return plainBlock;
  }
  
  
}
/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.cryptx;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 *
 * @author  Marcin Kurzawa
 */
public class RSAKeyPair extends Object {

  private RSAPublicKey publicKey;
  private RSAPrivateKey privateKey;

  /** Creates new RSAKeyPair */
  public RSAKeyPair(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
    this.publicKey = publicKey;
    this.privateKey = privateKey;
  }
  
  public RSAPublicKey getPublicKey() {
    return publicKey;
  }
  
  public RSAPrivateKey getPrivateKey() {
    return privateKey;
  }
  
}
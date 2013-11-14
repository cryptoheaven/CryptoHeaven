/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.util;

import com.CH_co.cryptx.SHA256;

import java.io.*;
import java.security.MessageDigest;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.4 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class Digester extends Object {

  public static MessageDigest getDigest(String hashAlgorithm) {
    String algorithm = hashAlgorithm.toUpperCase();
    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance(algorithm);
    } catch (Throwable t) {
      if (algorithm.equals(SHA256.name)) {
        digest = new com.CH_co.cryptx.SHA256();
      } else {
        throw new IllegalStateException(t.getMessage());
      }
    }
    return digest;
  }

  public static byte[] digestFile(File file, MessageDigest digest) throws FileNotFoundException, IOException {
    digest.reset();
    InputStream fIn = new BufferedInputStream(new FileInputStream(file), 32*1024);

    byte[] block = new byte[32*1024];
    int read = fIn.read(block);
    while (read != -1) {
      if (read > 0)
        digest.update(block, 0, read);
      read = fIn.read(block);
    }
    fIn.close();

    byte[] hexDigest = digest.digest();
    return hexDigest;
  }

}
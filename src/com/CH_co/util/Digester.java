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

package com.CH_co.util;

import java.io.*;
import java.security.MessageDigest;

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
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class Digester extends Object {

  public static MessageDigest getDigest(String hashAlgorithm) {
    String algorithm = hashAlgorithm.toUpperCase();
    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance(algorithm);
      algorithm = digest.getAlgorithm();
    } catch (Throwable t) {
      if (algorithm.equals("SHA256") || algorithm.equals("SHA-256")) {
        digest = new com.CH_co.cryptx.SHA256();
      } else {
        throw new IllegalStateException(t.getMessage());
      }
    }
    return digest;
  }

  public static byte[] digestFile(File file, MessageDigest digest) throws FileNotFoundException, IOException {
    digest.reset();
    FileInputStream fIn = new FileInputStream(file);

    byte[] block = new byte[16*1024];
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
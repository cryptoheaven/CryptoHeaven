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

import java.math.BigInteger;

import com.CH_co.cryptx.SHA256;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class Hasher extends Object { // implicit no-argument constructor
 
  
  /** @return a password hash suitable for sending to the remote server for authentication. 
    * Utilizes SHA-256 secure hash to produce 32 bytes = 256 bits.
    * Adds a little bit of salt, then takes the resulting 32 bytes and rehashes it again
    * with the original password and additional salt, and returns only 6 bytes as the passwordHash.
    */
  public static Long getPasswordHash(byte[] encodedPassword) {
    Long hashValue = null;
    try {
      
      // hash part of the password
      // Make it less probable that we would infringe any patents and not generate
      // the password hash based on transformed password, but rather from a portion of it,
      // and further protect uniqueness of our technology by using only part of the result as the password hash!
      // Shorter password hash does not lower our system's security, the weakest link is the user chosen 
      // password and the asymmetric key length.
      // Lets make it unique enough so that users don't get frastrated when trying to pick
      // a unique userId/password combination to form a unique user identifier.
      
      SHA256 sha = new SHA256();
      sha.update(encodedPassword, 0, encodedPassword.length-1);
      // Salt for the end
      sha.update(new byte[] { (byte)0x29, (byte)0x25, (byte)0xf3, (byte)0x67, (byte)0xc3, (byte)0xa1 });
      byte[] hash = sha.digest();
      
      // now hash encoded password (part) with previous hash with additional salt
      sha.update(encodedPassword, 0, encodedPassword.length-1);
      sha.update(hash);
      sha.update(new byte[] { (byte)0x13, (byte)0x68, (byte)0x43, (byte)0xd2, (byte)0xa1, (byte)0x7b });
      
      // get the final hash
      hash = sha.digest();
      sha.reset();
      
      // return only 6 bytes of the resulting hash
      byte[] h = new byte[6];
      System.arraycopy(hash, 0, h, 0, h.length);
      hashValue = new Long(new BigInteger(h).longValue());
      
    } catch (Throwable t) {
      throw new SecurityException("Could not produce an encoded password hash.");
    }
    return hashValue;
  }


  /**
   * Use SHA-256 to produce 32 bytes from the password. 
   */
  public static byte[] getEncodedPassword(char[] password) {
    byte[] encPass = null;
    try {
      // a character is 2 bytes long
      byte[] passwordBytes = new byte[password.length * 2];
      for (int i=0; i<password.length; i++) {
        passwordBytes[i] = (byte) (password[i] & 0x00FF);
        passwordBytes[password.length+i] = (byte) ((password[i] >>> 8) & 0x00FF);
      }
      
      SHA256 sha = new SHA256();
      sha.update(passwordBytes);
      // add a little salt
      sha.update(new byte[] { (byte)0x45, (byte)0xd7, (byte)0x72 });
      encPass = sha.digest();
      sha.reset();
      
    } catch (Throwable t) {
      throw new SecurityException("Could not produce an encoded password from the specified source.");
    }

    return encPass;
  }


  public static class Set {
    public byte[] encodedPassword;
    public Long passwordHash;

    public Set(char[] password) {
      this(getEncodedPassword(password));
    }

    public Set(byte[] encodedPassword, Long passwordHash) {
      this.encodedPassword = encodedPassword;
      this.passwordHash = passwordHash;
    }

    public Set(byte[] encodedPassword) {
      this(encodedPassword, getPasswordHash(encodedPassword));
    }

    public void clear() {
      if (encodedPassword != null)
        for (int i=0; i<encodedPassword.length; i++)
          encodedPassword[i] = 0;
    }

    public String toString() {
      return "[Hasher.Set"
      + ": encodedPassword="  + "*SHA-256-encodedPassword*"
      + ", passwordHash="     + passwordHash
      + "]";
    }
  }

}
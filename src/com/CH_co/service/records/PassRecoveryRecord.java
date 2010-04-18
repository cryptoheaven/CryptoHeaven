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

import java.security.*;
import java.sql.Timestamp;

import com.CH_co.cryptx.*;
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
 * <b>$Revision: 1.1 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class PassRecoveryRecord extends Record {

  public Long userId;
  public Timestamp lastUpdated;
  public Character enabledRecovery; // Y/N
  public String hint;
  public Character  enabledQA; // Y/N
  public Short numQs;
  public Short minAs;
  public String[] questions;
  public BADigestBlock[] answersHashMD5;
  // password is encrypted using SHA256 hashes of the normalized answer combos
  public BASymCipherBulk[] encPassList;
  public Timestamp lastFetched;
  public Timestamp lastFailed;
  public Timestamp lastRecovered;

  private static int[][] combos0q0a = { };
  private static int[][][] combos0q = { combos0q0a };

  private static int[][] combos1q0a = { };
  private static int[][] combos1q1a = { {0} };
  private static int[][][] combos1q = { combos1q0a, combos1q1a };

  private static int[][] combos2q0a = { };
  private static int[][] combos2q1a = { {0}, {1} };
  private static int[][] combos2q2a = { {0,1} };
  private static int[][][] combos2q = { combos2q0a, combos2q1a, combos2q2a };

  private static int[][] combos3q0a = { };
  private static int[][] combos3q1a = { {0}, {1}, {2} };
  private static int[][] combos3q2a = { {0,1}, {0,2}, {1,2} };
  private static int[][] combos3q3a = { {0,1,2} };
  private static int[][][] combos3q = { combos3q0a, combos3q1a, combos3q2a, combos3q3a };

  private static int[][] combos4q0a = { };
  private static int[][] combos4q1a = { {0}, {1}, {2}, {3} };
  private static int[][] combos4q2a = { {0,1}, {0,2}, {0,3}, {1,2}, {1,3}, {2,3} };
  private static int[][] combos4q3a = { {0,1,2}, {0,1,3}, {0,2,3}, {1,2,3} };
  private static int[][] combos4q4a = { {0,1,2,3} };
  private static int[][][] combos4q = { combos4q0a, combos4q1a, combos4q2a, combos4q3a, combos4q4a };

  private static int[][] combos5q0a = { };
  private static int[][] combos5q1a = { {0}, {1}, {2}, {3}, {4} };
  private static int[][] combos5q2a = { {0,1}, {0,2}, {0,3}, {0,4}, {1,2}, {1,3}, {1,4}, {2,3}, {2, 4}, {3,4} };
  private static int[][] combos5q3a = { {0,1,2}, {0,1,3}, {0,1,4}, {0,2,3}, {0,2,4}, {0,3,4}, {1,2,3}, {1,2,4}, {1,3,4}, {2,3,4} };
  private static int[][] combos5q4a = { {0,1,2,3}, {0,1,2,4}, {0,1,3,4}, {0,2,3,4}, {1,2,3,4} };
  private static int[][] combos5q5a = { {0,1,2,3,4} };
  private static int[][][] combos5q = { combos5q0a, combos5q1a, combos5q2a, combos5q3a, combos5q4a, combos5q5a };

  public static int[][][][] combos = { combos0q, combos1q, combos2q, combos3q, combos4q, combos5q };

//  static {
//    for (int numQs=0; numQs<combos.length; numQs++) {
//
//      for (int numAs=0; numAs<combos[numQs].length; numAs++) {
//        System.out.println("Doing number of Questions = " + numQs + ", and number of Answeres = " + numAs);
//
//        int[][] checkCombos = combos[numQs][numAs];
//        int combinations = checkCombos.length;
//        for (int i=0; i<combinations; i++) {
//          System.out.println("combo " +i);
//          for (int k=0; k<checkCombos[i].length; k++) {
//            int element = checkCombos[i][k];
//            if (k>0)
//              System.out.print(",");
//            System.out.print(""+element);
//          }
//          System.out.println();
//        }
//      }
//    }
//
//    String str = "  Hello      there and some.  ";
//    System.out.println("Normalizing " + str + "~");
//    System.out.println("Normalized: " + normalizeAnswer(str) + "~");
//    System.out.println("Compresses: " + normalizeAnswer(str) + "~");
//
//    PassRecoveryRecord passRecoveryRecord = new PassRecoveryRecord();
//    String[] theQs = new String[5];
//    theQs[0] = "Question 1 is simple";
//    theQs[1] = "Question 2 is red";
//    theQs[2] = "Question 3";
//    theQs[3] = "Question 4";
//    theQs[4] = "Question 5";
//    String[] theAs = new String[5];
//    theAs[0] = "Koza";
//    theAs[1] = "MA";
//    theAs[2] = "rogi";
//    theAs[3] = "aNd  ";
//    theAs[4] = " some  more";
//    passRecoveryRecord.setData(true, "test and then some here and !!1".toCharArray(), "This is hint", true, 5, 2, theQs, theAs);
//    String[] theOtherAs = new String[5];
//    theOtherAs[0] = "";
//    theOtherAs[1] = "ma";
//    theOtherAs[2] = "";
//    theOtherAs[3] = " and  ";
//    theOtherAs[4] = " some  moressssss";
//    System.out.println("Recovered pass is " + new String(passRecoveryRecord.recoverPassword(theQs, theOtherAs)) + "~");
//    System.out.println("Size of encPassList is " + passRecoveryRecord.encPassList.length);
//    int maxEncPassSize = 0;
//    for (int i=0; i<passRecoveryRecord.encPassList.length; i++)
//      maxEncPassSize = Math.max(maxEncPassSize, passRecoveryRecord.encPassList[i].size());
//    System.out.println("max length of any encPass is " + maxEncPassSize);
//  }
//
//  public static void main(String[] args) {
//  }

  public static String normalizeAnswer(String answer) {
    answer = answer.trim();
    answer = answer.toLowerCase();
    // remove double spaces
    StringBuffer sb = new StringBuffer(answer);
    while (true) {
      int i = sb.indexOf("  ");
      if (i < 0)
        break;
      else {
        sb.replace(i, i+2, " ");
      }
    }
    return sb.toString();
  }

  public void setData(boolean enabledRecovery, char[] pass, String hint, boolean enabledQA, int numQuestions, int minAnsweres, String[] theQs, String[] theAs) {
    this.enabledRecovery = new Character(enabledRecovery ? 'Y' : 'N');
    if (enabledRecovery) {
      this.hint = hint;
    } else {
      this.hint = "";
    }

    this.numQs = new Short((short) numQuestions);
    this.minAs = new Short((short) minAnsweres);

    this.enabledQA = new Character(enabledRecovery && enabledQA ? 'Y' : 'N');
    if (enabledRecovery && enabledQA) {
      this.questions = new String[numQuestions];
      this.answersHashMD5 = new BADigestBlock[numQuestions];
      this.encPassList = new BASymCipherBulk[combos[numQuestions][minAnsweres].length];
    } else {
      this.questions = null;
      this.answersHashMD5 = null;
      this.encPassList = null;
    }

    if (enabledRecovery && enabledQA) {
      String[] answersNormalized = new String[numQuestions];
      MessageDigest md5 = null;
      try {
        md5 = MessageDigest.getInstance("md5");
      } catch (NoSuchAlgorithmException e) {
      }
      for (int i=0; i<numQuestions; i++) {
        this.questions[i] = theQs[i];
        answersNormalized[i] = normalizeAnswer(theAs[i]);
        md5.reset();
        answersHashMD5[i] = new BADigestBlock(md5.digest(Misc.convStrToBytes(answersNormalized[i])));
      }

      BADigestBlock[] comboHashSHA256 = new BADigestBlock[encPassList.length];

      StringBuffer comboAnswers = null;
      MessageDigest sha256 = new SHA256();
      int[][] checkCombos = combos[numQuestions][minAnsweres];
      int combinations = checkCombos.length;
      for (int comboIndex=0; comboIndex<combinations; comboIndex++) {
        comboAnswers = new StringBuffer();
        for (int comboElement=0; comboElement<checkCombos[comboIndex].length; comboElement++) {
          int elementValue = checkCombos[comboIndex][comboElement];
          comboAnswers.append(answersNormalized[elementValue]);
        }
        sha256.reset();
        comboHashSHA256[comboIndex] = new BADigestBlock(sha256.digest(Misc.convStrToBytes(comboAnswers.toString())));

        // encrypt the password with combined combo hash
        SymmetricBulkCipher cipher = null;
        try {
          cipher = new SymmetricBulkCipher(new BASymmetricKey(comboHashSHA256[comboIndex]));
          encPassList[comboIndex] = cipher.bulkEncrypt(new BASymPlainBulk(Misc.convCharsToBytes(pass)));
        } catch (NoSuchAlgorithmException e) {
        } catch (DigestException e) {
        } catch (InvalidKeyException e) {
        }
      }
    }
  }

  /**
   * Check if specified questions are the same as stored in the record.
   * If not the same throw an exception.
   */
  private void checkQuestions(String[] theQs) {
    // check that order of questions matches exactly the order of ones stored in the record
    for (int i=0; i<theQs.length; i++) {
      String q = theQs[i];
      if (!q.equals(this.questions[i]))
        throw new IllegalArgumentException("Specified questions array do not match those stored in the record.");
    }
  }

  /**
   * Checks if the answeres match ones stored in the record (compare their hash values)
   * return array of Normalized Correct Answeres, those incorrect will be NULL in the return array.
   */
  public String[] checkAnsweres(String[] theAs) {
    int numAs = theAs.length;
    String[] answersNormalized = new String[numAs];
    String[] answersNormalizedCorrect = new String[numAs];
    MessageDigest md5 = null;
    try {
      md5 = MessageDigest.getInstance("md5");
    } catch (NoSuchAlgorithmException e) {
    }
    for (int i=0; i<numAs; i++) {
      answersNormalized[i] = normalizeAnswer(theAs[i]);
      md5.reset();
      if (answersHashMD5[i].equals(new BADigestBlock(md5.digest(Misc.convStrToBytes(answersNormalized[i]))))) {
        answersNormalizedCorrect[i] = answersNormalized[i];
      }
    }
    return answersNormalizedCorrect;
  }

  /**
   * Use setting 'minAnsweres' to create all combinations of answeres among specified 'answeres' according to 'combos' permutations.
   * If at least one permutation of answeres matches one stored in the record we will use it to decrypt the stored encrypted password data.
   */
  public char[] recoverPassword(String[] theQs, String[] theAs) {
    char[] password = null;

    checkQuestions(theQs);

    String[] answersNormalizedCorrect = checkAnsweres(theAs);

    int numCombos = combos[numQs.shortValue()][minAs.shortValue()].length;
    BADigestBlock[] comboHashSHA256 = new BADigestBlock[numCombos];

    StringBuffer comboAnswers = null;
    MessageDigest sha256 = new SHA256();
    int[][] checkCombos = combos[numQs.shortValue()][minAs.shortValue()];
    int combinations = checkCombos.length;
    for (int comboIndex=0; comboIndex<combinations; comboIndex++) {
      comboAnswers = new StringBuffer();
      boolean foundAllCorrectAnsweres = true;
      for (int comboElement=0; comboElement<checkCombos[comboIndex].length; comboElement++) {
        int elementValue = checkCombos[comboIndex][comboElement];
        if (answersNormalizedCorrect[elementValue] != null) {
          comboAnswers.append(answersNormalizedCorrect[elementValue]);
        } else {
          foundAllCorrectAnsweres = false;
          break;
        }
      }
      if (!foundAllCorrectAnsweres) {
        continue;
      }
      sha256.reset();
      BADigestBlock digest = null;
      comboHashSHA256[comboIndex] = new BADigestBlock(sha256.digest(Misc.convStrToBytes(comboAnswers.toString())));

      // decrypt the password with combined combo hash
      SymmetricBulkCipher cipher = null;
      try {
        cipher = new SymmetricBulkCipher(new BASymmetricKey(comboHashSHA256[comboIndex]));
        BASymPlainBulk plainPasswordBytes = cipher.bulkDecrypt(encPassList[comboIndex]);
        password = Misc.convBytesToChars(plainPasswordBytes.toByteArray());
      } catch (NoSuchAlgorithmException e) {
      } catch (DigestException e) {
      } catch (InvalidKeyException e) {
      }
    }

    return password;
  }

  public int getIcon() {
    return ImageNums.IMAGE_NONE;
  }

  public Long getId() {
    return userId;
  }

  public boolean isEnabledRecovery() {
    return enabledRecovery != null && (enabledRecovery.charValue() == 'Y' || enabledRecovery.charValue() == 'y');
  }

  public boolean isEnabledQA() {
    return enabledQA != null && (enabledQA.charValue() == 'Y' || enabledQA.charValue() == 'y');
  }

  public void merge(Record updated) {
    if (updated instanceof PassRecoveryRecord) {
      PassRecoveryRecord record = (PassRecoveryRecord) updated;
      if (record.userId           != null) userId           = record.userId;
      if (record.lastUpdated      != null) lastUpdated      = record.lastUpdated;
      if (record.enabledRecovery  != null) enabledRecovery  = record.enabledRecovery;
      if (record.hint             != null) hint             = record.hint;
      if (record.enabledQA        != null) enabledQA        = record.enabledQA;
      if (record.numQs            != null) numQs            = record.numQs;
      if (record.minAs            != null) minAs            = record.minAs;
      if (record.isEnabledQA()) {
        questions = record.questions;
        answersHashMD5 = record.answersHashMD5;
        encPassList = record.encPassList;
      } else {
        questions = null;
        answersHashMD5 = null;
        encPassList = null;
      }
      if (record.lastFetched   != null) lastFetched   = record.lastFetched;
      if (record.lastFailed    != null) lastFailed    = record.lastFailed;
      if (record.lastRecovered != null) lastRecovered = record.lastRecovered;
    }
    else 
      super.mergeError(updated);
  }

  public String toString() {
    return "[PassRecoveryRecord"
      + ": userId="           + userId
      + ", lastUpdated="      + lastUpdated
      + ", enabledRecovery="  + enabledRecovery
      + ", hint="             + hint
      + ", enabledQA="        + enabledQA
      + ", numQs="            + numQs
      + ", minAs="            + minAs
      + ", questions="        + Misc.objToStr(questions)
      + ", answersHashMD5="   + Misc.objToStr(answersHashMD5)
      + ", encPassList="      + Misc.objToStr(encPassList)
      + ", lastFetched="      + lastFetched
      + ", lastFailed="       + lastFailed
      + ", lastRecovered="    + lastRecovered
      + "]";
  }

  public void setId(Long id) {
    userId = id;
  }

}
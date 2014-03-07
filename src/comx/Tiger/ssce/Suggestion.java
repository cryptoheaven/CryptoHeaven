/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package comx.Tiger.ssce;

class Suggestion {

  String word;
  int score;
  boolean matchCase;

  Suggestion() {
  }

  public boolean equals(Object obj) {
    if (obj instanceof Suggestion) {
      Suggestion suggestion = (Suggestion) obj;
      return word.equals(suggestion.word);
    } else {
      return false;
    }
  }
}

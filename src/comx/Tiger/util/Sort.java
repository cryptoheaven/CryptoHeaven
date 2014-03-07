/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package comx.Tiger.util;

public class Sort {

  public Sort() {
  }

  public static void ascending(String as[]) {
    for (int i = as.length / 2; i > 0; i /= 2) {
      for (int j = i; j < as.length; j++) {
        for (int k = j - i; k >= 0 && as[k].compareTo(as[k + i]) > 0; k -= i) {
          String s = as[k];
          as[k] = as[k + i];
          as[k + i] = s;
        }
      }
    }
  }

  public static void ascending(Comparable acomparable[]) {
    for (int i = acomparable.length / 2; i > 0; i /= 2) {
      for (int j = i; j < acomparable.length; j++) {
        for (int k = j - i; k >= 0 && acomparable[k].compareTo(acomparable[k + i]) > 0; k -= i) {
          Comparable comparable = acomparable[k];
          acomparable[k] = acomparable[k + i];
          acomparable[k + i] = comparable;
        }
      }
    }
  }

  public static void descending(Comparable acomparable[]) {
    for (int i = acomparable.length / 2; i > 0; i /= 2) {
      for (int j = i; j < acomparable.length; j++) {
        for (int k = j - i; k >= 0 && acomparable[k].compareTo(acomparable[k + i]) <= 0; k -= i) {
          Comparable comparable = acomparable[k];
          acomparable[k] = acomparable[k + i];
          acomparable[k + i] = comparable;
        }
      }
    }
  }
}
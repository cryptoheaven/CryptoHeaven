/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */
// Source File Name:   Lexicon.java

package comx.Tiger.ssce;

import java.io.Serializable;

// Referenced classes of package com.wintertree.ssce:
//      WordComparator, SuggestionSet

public interface Lexicon extends Serializable {

  public static final int AFRIKAANS_LANG = 24934;
  public static final int AMERICAN_ENGLISH_LANG = 24941;
  public static final int ARABIC_LANG = 24946;
  public static final int BYELORUSSIAN_LANG = 25189;
  public static final int BULGARIAN_LANG = 25191;
  public static final int BRITISH_ENGLISH_LANG = 25202;
  public static final int CANADIAN_ENGLISH_LANG = 25441;
  public static final int NORWEGIAN_BOKMAL_LANG = 28258;
  public static final int NORWEGIAN_NYNORSK_LANG = 28270;
  public static final int CZECH_LANG = 25459;
  public static final int DANISH_LANG = 25697;
  public static final int DUTCH_LANG = 25717;
  public static final int GREEK_LANG = 25964;
  public static final int ESPERANTO_LANG = 25967;
  public static final int ESTONIAN_LANG = 25972;
  public static final int BASQUE_LANG = 25973;
  public static final int FINNISH_LANG = 26217;
  public static final int FAROESE_LANG = 26223;
  public static final int FRENCH_LANG = 26226;
  public static final int GERMAN_LANG = 26469;
  public static final int CROATIAN_LANG = 26738;
  public static final int HUNGARIAN_LANG = 26741;
  public static final int ICELANDIC_LANG = 26995;
  public static final int ITALIAN_LANG = 26996;
  public static final int HEBREW_LANG = 26999;
  public static final int YIDDISH_LANG = 27241;
  public static final int GREENLANDIC_LANG = 27500;
  public static final int LITHUANIAN_LANG = 27764;
  public static final int LATVIAN_LANG = 27766;
  public static final int MACEDONIAN_LANG = 28011;
  public static final int MALTESE_LANG = 28020;
  public static final int PORTUGUESE_BRAZIL_LANG = 28770;
  public static final int POLISH_LANG = 28780;
  public static final int PORTUGUESE_IBERIAN_LANG = 28783;
  public static final int ROMANIC_LANG = 29293;
  public static final int ROMANIAN_LANG = 29295;
  public static final int RUSSIAN_LANG = 29301;
  public static final int CATALAN_LANG = 29539;
  public static final int SWAHILI_LANG = 29545;
  public static final int SLOVAK_LANG = 29547;
  public static final int SLOVENIAN_LANG = 29548;
  public static final int SPANISH_LANG = 29552;
  public static final int ALBANIAN_LANG = 29553;
  public static final int SERBIAN_LANG = 29554;
  public static final int SWEDISH_LANG = 29559;
  public static final int TURKISH_LANG = 29813;
  public static final int UKRAINIAN_LANG = 30059;
  public static final int ANY_LANG = 30840;
  public static final int AUTO_CHANGE_ACTION = 97;
  public static final int AUTO_CHANGE_PRESERVE_CASE_ACTION = 65;
  public static final int CONDITIONAL_CHANGE_ACTION = 99;
  public static final int CONDITIONAL_CHANGE_PRESERVE_CASE_ACTION = 67;
  public static final int EXCLUDE_ACTION = 101;
  public static final int IGNORE_ACTION = 105;
  public static final int NOT_FOUND = 0;

  public abstract int findWord(String s, boolean flag, StringBuffer stringbuffer);

  public abstract void suggest(String s, int i, WordComparator wordcomparator, SuggestionSet suggestionset);
}
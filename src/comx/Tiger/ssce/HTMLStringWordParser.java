/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package comx.Tiger.ssce;

import comx.Tiger.util.UniCharacter;

public class HTMLStringWordParser extends StringWordParser {

  private static final String wordCharEntities[] = {
    "Agrave", "Aacute", "Acirc", "Atilde", "Auml", "Aring", "AElig", "Ccedil", "Egrave", "Eacute",
    "Ecirc", "Euml", "Igrave", "Iacute", "Icirc", "Iuml", "ETH", "Ntilde", "Ograve", "Oacute",
    "Ocirc", "Otilde", "Ouml", "Oslash", "Ugrave", "Uacute", "Ucirc", "Uuml", "Yacute", "THORN",
    "szlig", "agrave", "aacute", "acirc", "atilde", "auml", "aring", "aelig", "ccedil", "egrave",
    "eacute", "ecirc", "euml", "igrave", "iacute", "icirc", "iuml", "eth", "ntilde", "ograve",
    "oacute", "ocirc", "otilde", "ouml", "oslash", "ugrave", "uacute", "ucirc", "uuml", "yacute",
    "thorn", "yuml"
  };
  private static final char charEntitiesToChar[] = {
    '\300', '\301', '\302', '\303', '\304', '\305', '\306', '\307', '\310', '\311',
    '\312', '\313', '\314', '\315', '\316', '\317', '\320', '\321', '\322', '\323',
    '\324', '\325', '\326', '\330', '\331', '\332', '\333', '\334', '\335', '\336',
    '\337', '\340', '\341', '\342', '\343', '\344', '\345', '\346', '\347', '\350',
    '\351', '\352', '\353', '\354', '\355', '\356', '\357', '\360', '\361', '\362',
    '\363', '\364', '\365', '\366', '\370', '\371', '\372', '\373', '\374', '\375',
    '\376', '\377'
  };

  public HTMLStringWordParser(String s, boolean flag) {
    super(s, flag);
  }

  public HTMLStringWordParser(boolean flag) {
    this(null, flag);
  }

  public static String decodeCharacterEntities(String s) {
    StringBuffer stringbuffer = new StringBuffer();
    for (int i = 0; i < s.length(); i++) {
      boolean flag = false;
      if (s.charAt(i) == '&' && i < s.length() - 1) {
        int j = s.indexOf(';', i + 1);
        if (j >= 0) {
          String s1 = s.substring(i + 1, j);
          char c = wordCharEntityToChar(s1);
          if (c != 0) {
            stringbuffer.append(c);
            flag = true;
            i = j;
          }
        }
      }
      if (!flag) {
        stringbuffer.append(s.charAt(i));
      }
    }

    return stringbuffer.toString();
  }

  public static String encodeCharacterEntities(String s) {
    StringBuffer stringbuffer = new StringBuffer();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      boolean flag = false;
      if (c > '\177' && c < '\377' && UniCharacter.isLetter(c)) {
        int j = 0;
        do {
          if (j >= charEntitiesToChar.length) {
            break;
          }
          if (charEntitiesToChar[j] == c) {
            stringbuffer.append("&" + wordCharEntities[j] + ";");
            flag = true;
            break;
          }
          j++;
        } while (true);
      }
      if (!flag) {
        stringbuffer.append(s.charAt(i));
      }
    }

    return stringbuffer.toString();
  }

  protected int findWordStart() {
    boolean flag = false;
    boolean flag1 = false;
    int i = 0;
    int j;
    for (j = super.cursor; j < super.theString.length() && (flag || flag1 || !StringWordParser.is1stWordChar(super.theString.charAt(j))); j++) {
      char c = super.theString.charAt(j);
      if ('<' == c) {
        flag = true;
        continue;
      }
      if ('&' == c && !flag) {
        flag1 = true;
        i = j;
        continue;
      }
      if (flag && '>' == c) {
        flag = false;
        continue;
      }
      if (!flag1) {
        continue;
      }
      if (';' == c) {
        flag1 = false;
        if (i + 1 >= super.theString.length()) {
          continue;
        }
        String s = super.theString.toString().substring(i + 1, j);
        if (!isWordCharEntity(s)) {
          continue;
        }
        j = i;
        break;
      }
      if (!UniCharacter.isWhitespace(c) && !UniCharacter.isPunctuation(c)) {
        continue;
      }
      flag1 = false;
      j = i + 1;
      break;
    }

    return j;
  }

  protected String getPrevWord() {
    boolean flag = false;
    boolean flag1 = false;
    int i;
    for (i = super.cursor - 1; i >= 0 && !flag1; i--) {
      char c = super.theString.charAt(i);
      if ('>' == c) {
        flag = true;
      } else if (';' == c && !flag) {
        int k = i - 1;
        do {
          if (k < 0) {
            break;
          }
          char c1 = super.theString.charAt(k);
          if (c1 == '&') {
            i = k;
            c = c1;
            break;
          }
          if (!UniCharacter.isLetterOrDigit(c1) && c1 != '#') {
            break;
          }
          k--;
        } while (true);
      }
      if (flag && '<' == c) {
        flag = false;
      }
      flag1 = !flag && !UniCharacter.isWhitespace(c);
    }

    if (i <= 0) {
      return null;
    }
    for (; i > 0 && StringWordParser.isWordChar(super.theString.charAt(i - 1)); i--);
    int j = super.cursor;
    int l = super.subWordLength;
    String s = super.cachedWord.toString();
    super.cursor = i;
    super.subWordLength = -1;
    super.cachedWord.setLength(0);
    String s1 = getWord();
    super.cursor = j;
    super.subWordLength = l;
    super.cachedWord.setLength(0);
    super.cachedWord.append(s);
    return s1;
  }

  protected boolean includeCharInWord(StringBuffer stringbuffer, int i, boolean flag) {
    char c = stringbuffer.charAt(i);
    if (c == '&') {
      int j;
      for (j = i + 1; j < stringbuffer.length() && stringbuffer.charAt(j) != ';'; j++) {
        if (!UniCharacter.isLetterOrDigit(stringbuffer.charAt(j))) {
          return false;
        }
      }

      if (i + 1 < stringbuffer.length() && j < stringbuffer.length() && stringbuffer.charAt(j) == ';') {
        char ac[] = new char[j - (i + 1)];
        stringbuffer.getChars(i + 1, j, ac, 0);
        String s = new String(ac);
        return isWordCharEntity(s);
      } else {
        return false;
      }
    }
    if (c == ';') {
      int k;
      for (k = i - 1; k >= 0 && stringbuffer.charAt(k) != '&'; k--);
      if (k >= 0 && stringbuffer.charAt(k) == '&') {
        char ac1[] = new char[i - (k + 1)];
        stringbuffer.getChars(k + 1, i, ac1, 0);
        String s1 = new String(ac1);
        return isWordCharEntity(s1);
      } else {
        return false;
      }
    } else {
      return super.includeCharInWord(stringbuffer, i, flag);
    }
  }

  protected static String charToWordCharEntity(char c) {
    for (int i = 0; i < charEntitiesToChar.length; i++) {
      if (charEntitiesToChar[i] == c) {
        return wordCharEntities[i];
      }
    }

    return null;
  }

  protected static char wordCharEntityToChar(String s) {
    for (int i = 0; i < wordCharEntities.length; i++) {
      if (wordCharEntities[i].equals(s)) {
        return charEntitiesToChar[i];
      }
    }

    return '\0';
  }

  protected static boolean isWordCharEntity(String s) {
    return wordCharEntityToChar(s) != 0;
  }
}
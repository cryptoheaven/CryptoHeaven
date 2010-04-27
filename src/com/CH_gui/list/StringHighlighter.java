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

package com.CH_gui.list;

import java.util.*;

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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class StringHighlighter extends Object {

  public static final int MATCH_STRING__EXACT = 1024;
  public static final int MATCH_STRING__TRIMMED = 512;
  public static final int MATCH_STRING__NO_CASE = 256;
  public static final int MATCH_STRING__STARTS_WITH = 128;
  public static final int MATCH_STRING__LEADING_TOKENS = 64;
  public static final int MATCH_STRING__SEQUENCED_TOKENS = 32;
  public static final int MATCH_STRING__SEQUENCED_TOKENS__WITH_SKIPPING = 16;
  public static final int MATCH_STRING__SEQUENCED_TOKENS__WITH_SKIPPING_MORE = 8;
  public static final int MATCH_STRING__SEQUENCED_TOKENS__WITH_SKIPPING_LOTS = 4;
  public static final int MATCH_STRING__CONTAINS = 2;
  public static final int MATCH_STRING__NO_MATCH = 0;

  private static final String BOLD_START = "<b>";//<font color=\"#FF0000\">";
  private static final String BOLD_END = "</b>";//"</font>";

  private static final int MATCH_STRING__STARTS_WITH_EMPTY_MASK = 
      MATCH_STRING__STARTS_WITH |
      MATCH_STRING__LEADING_TOKENS |
      MATCH_STRING__SEQUENCED_TOKENS |
      MATCH_STRING__SEQUENCED_TOKENS__WITH_SKIPPING |
      MATCH_STRING__SEQUENCED_TOKENS__WITH_SKIPPING_MORE |
      MATCH_STRING__SEQUENCED_TOKENS__WITH_SKIPPING_LOTS |
      MATCH_STRING__CONTAINS;

  /**
   * @return one of MATCH_... comparison levels depending on how closely strings match
   * @param s1 is the original String and s2 is the search part we are looking for
   */
  public static int matchStrings(String s1, StringHighlighterI highlighter, boolean includeVisual, String[] visualsReturnBuffer) {
    String search = highlighter.getHighlightStr();
    if (search != null)
      return matchStrings(s1, search, highlighter.getHighlightMatch(), includeVisual, highlighter.alwaysArmorInHTML(), highlighter.includePreTags(), visualsReturnBuffer);
    else
      return matchStrings(s1, highlighter.getHighlightStrs(), highlighter.getHighlightMatch(), includeVisual, highlighter.alwaysArmorInHTML(), highlighter.includePreTags(), visualsReturnBuffer);
  }
  public static int matchStrings(String s1, String[] s2, int matchBITS, boolean includeVisual, boolean alwaysArmorInHTML, boolean includePreTags, String[] visualsReturnBuffer) {
    int match = MATCH_STRING__NO_MATCH;
    for (int i=0; s2!=null && i<s2.length; i++) {
      if (s2[i].length() == 0 && (matchBITS & MATCH_STRING__STARTS_WITH_EMPTY_MASK) != 0)
        match = MATCH_STRING__STARTS_WITH;
      else
        match = matchStrings(s1, s2[i], matchBITS, includeVisual, alwaysArmorInHTML, includePreTags, visualsReturnBuffer);
      if (match != MATCH_STRING__NO_MATCH)
        break;
    }
    return match;
  }
  public static int matchStrings(String s1, String s2, int matchBITS, boolean includeVisual, boolean alwaysArmorInHTML, boolean includePreTags, String[] visualsReturnBuffer) {
    int match = MATCH_STRING__NO_MATCH;
    String visual = null;
    String s1Orig = s1;
    String s2Orig = s2;
    if (match == MATCH_STRING__NO_MATCH && (matchBITS & MATCH_STRING__EXACT) != 0) {
      if (s1.equals(s2)) {
        match = MATCH_STRING__EXACT;
        if (includeVisual)
          visual = BOLD_START + Misc.encodePlainLineIntoHtmlLine(s1Orig) + BOLD_END;
      }
    }

    if (match == MATCH_STRING__NO_MATCH && (matchBITS & MATCH_STRING__TRIMMED) != 0) {
      s1 = s1.trim();
      s2 = s2 != null ? s2.trim() : null;
      if (s1.equals(s2)) {
        match = MATCH_STRING__TRIMMED;
        if (includeVisual)
          visual = BOLD_START + Misc.encodePlainLineIntoHtmlLine(s1Orig) + BOLD_END;
      }
    }

    if (match == MATCH_STRING__NO_MATCH && (matchBITS & MATCH_STRING__NO_CASE) != 0) {
      s1 = s1.toLowerCase();
      s2 = s2 != null ? s2.toLowerCase() : null;
      if (s1.equals(s2)) {
        match = MATCH_STRING__NO_CASE;
        if (includeVisual)
          visual = BOLD_START + Misc.encodePlainLineIntoHtmlLine(s1Orig) + BOLD_END;
      }
    }

    if (match == MATCH_STRING__NO_MATCH && (matchBITS & MATCH_STRING__STARTS_WITH) != 0) {
      if (s1.startsWith(s2)) {
        match = MATCH_STRING__STARTS_WITH;
        if (includeVisual) {
          String s1Temp = (matchBITS & MATCH_STRING__NO_CASE) != 0 ? s1Orig.toLowerCase() : s1Orig;
          String s2Temp = (matchBITS & MATCH_STRING__NO_CASE) != 0 ? s2Orig.toLowerCase() : s2Orig;
          if ((matchBITS & MATCH_STRING__TRIMMED) != 0)
            s2Temp = s2.trim();
          int index = s1Temp.indexOf(s2Temp);
          if (index >= 0) {
            int endIndex = index + s2Temp.length();
            visual = BOLD_START + Misc.encodePlainLineIntoHtmlLine(s1Orig.substring(0, endIndex)) + BOLD_END;
            if (endIndex < s1Orig.length())
              visual += Misc.encodePlainLineIntoHtmlLine(s1Orig.substring(endIndex));
          } else
            visual = BOLD_START + Misc.encodePlainLineIntoHtmlLine(s1Orig) + BOLD_END;
        }
      }
    }

    if (match == MATCH_STRING__NO_MATCH && 
          (
            (matchBITS & MATCH_STRING__LEADING_TOKENS) != 0 || (matchBITS & MATCH_STRING__SEQUENCED_TOKENS) != 0
          )
        )
    {
      boolean isSequencedTokens = (matchBITS & MATCH_STRING__SEQUENCED_TOKENS) != 0;
      int matchType = MATCH_STRING__LEADING_TOKENS;
      String delimeters = " \"<>-_@.(){}[]'";
      StringTokenizer st1 = new StringTokenizer(s1Orig, delimeters, true);
      StringTokenizer st2 = new StringTokenizer(s2Orig);
      StringBuffer visualSB = null;
      if (includeVisual) visualSB = new StringBuffer();
      boolean ok = true;
      String st2_t = null;
      String prevSt2_t = null;
      while (st1.hasMoreTokens() && (st2.hasMoreTokens() || (includeVisual && st2_t != null))) {
        // store previous search token...
        prevSt2_t = st2_t;
        // if st2 DOES NOT have more tokens then repeat the last token
        boolean repeatedToken = false;
        if (st2.hasMoreTokens())
          st2_t = st2.nextToken();
        else
          repeatedToken = true;
        // check for new delimiters that should be treated as characters
        String delim = delimeters;
        for (int i=0; i<delim.length(); i++) {
          char ch = delim.charAt(i);
          if (ch != ' ')
            if (st2_t.indexOf(ch) >= 0)
              delim = delim.replace(ch, ' ');
        }
        do {
          // downgrade match type
          if (!ok) {
            if (matchType > MATCH_STRING__SEQUENCED_TOKENS__WITH_SKIPPING_LOTS)
              matchType = matchType >> 1;
          }
          // find next token from Source string
          String st1_t = null;
          while (st1.hasMoreTokens()) {
            st1_t = null;
            try {
              if (st1.hasMoreTokens())
                st1_t = st1.nextToken(delim);
            } catch (Throwable t) {
            }
            boolean isDelim = st1_t != null && st1_t.length() == 1 && delim.indexOf(st1_t) >= 0;
            if (!isDelim) {
              // found next token
              break;
            } else if (st1_t == null) {
              // search token exists, but no more source string tokens
              ok = false;
              break;
            } else {
              // append non-final delimiter, final one will be handled as not found token...
              if (includeVisual) visualSB.append(Misc.encodePlainLineIntoHtmlLine(st1_t));
            }
            if (isDelim)
              st1_t = null;
          }
          boolean isLastToken = !st2.hasMoreTokens();
          boolean found = false;
          String sourceToken = st1_t;
          String searchToken = st2_t;
          for (int twice=0; twice<2; twice++) {
            boolean isFirstLap = twice == 0;
            boolean isFinalLap = twice == 1 || prevSt2_t == null || !includeVisual;
            if (isLastToken) {
              if ((matchBITS & MATCH_STRING__NO_CASE) != 0)
                found = sourceToken != null && sourceToken.toLowerCase().startsWith(searchToken.toLowerCase());
              else
                found = sourceToken != null && sourceToken.startsWith(searchToken);
              if (includeVisual) {
                if (found) {
                  visualSB.append(BOLD_START);
                  visualSB.append(Misc.encodePlainLineIntoHtmlLine(sourceToken.substring(0, searchToken.length())));
                  visualSB.append(BOLD_END);
                  if (searchToken.length() < sourceToken.length()) {
                    visualSB.append(Misc.encodePlainLineIntoHtmlLine(sourceToken.substring(searchToken.length())));
                  }
                } else if (sourceToken != null && isFinalLap) {
                  //visualSB.append(sourceToken);
                }
              }
            } else {
              if ((matchBITS & MATCH_STRING__NO_CASE) != 0)
                found = sourceToken != null && sourceToken.equalsIgnoreCase(searchToken);
              else
                found = sourceToken != null && sourceToken.equals(searchToken);
              if (includeVisual) {
                if (found) {
                  visualSB.append(BOLD_START);
                  visualSB.append(Misc.encodePlainLineIntoHtmlLine(sourceToken));
                  visualSB.append(BOLD_END);
                } else if (sourceToken != null && isFinalLap) {
                  //visualSB.append(sourceToken);
                }
              }
            }
            // first lap 
            if (isFirstLap) {
              if (!found && repeatedToken) {
                ok = true;
              } else {
                ok = found;
              }
            }
            if (found || isFinalLap) {
              // don't do second iteration
              break;
            } else {
              // on second iteration compare to previous search token....
              isLastToken = false;
              searchToken = prevSt2_t;
            }
          }
          if (includeVisual && !found && st1_t != null) {
            visualSB.append(Misc.encodePlainLineIntoHtmlLine(st1_t));
          }
        } while (!ok && isSequencedTokens && st1.hasMoreTokens());
        if (!ok)
          break;
      }
      if (ok) {
        ok = !st2.hasMoreTokens(); // no match if search string has more unprocessed tokens
      }
      if (ok) {
        match = matchType;
        if (includeVisual) {
          // append rest of source string
          while (st1.hasMoreTokens())
            visualSB.append(Misc.encodePlainLineIntoHtmlLine(st1.nextToken()));
          visual = visualSB.toString();
        }
      }
    }

    if (match == MATCH_STRING__NO_MATCH && (matchBITS & MATCH_STRING__CONTAINS) != 0) {
      if (s1.indexOf(s2) >= 0) {
        match = MATCH_STRING__CONTAINS;
        if (includeVisual) {
          String s1Temp = (matchBITS & MATCH_STRING__NO_CASE) != 0 ? s1Orig.toLowerCase() : s1Orig;
          String s2Temp = (matchBITS & MATCH_STRING__NO_CASE) != 0 ? s2Orig.toLowerCase() : s2Orig;
          if ((matchBITS & MATCH_STRING__TRIMMED) != 0)
            s2Temp = s2.trim();
          int index = s1Temp.indexOf(s2Temp);
          if (index >= 0) {
            int endIndex = index + s2Temp.length();
            visual = "";
            if (index > 0)
              visual = Misc.encodePlainLineIntoHtmlLine(s1Orig.substring(0, index));
            visual += BOLD_START + Misc.encodePlainLineIntoHtmlLine(s1Orig.substring(index, endIndex)) + BOLD_END;
            if (endIndex < s1Orig.length())
              visual += Misc.encodePlainLineIntoHtmlLine(s1Orig.substring(endIndex));
          } else
            visual = BOLD_START + Misc.encodePlainLineIntoHtmlLine(s1Orig) + BOLD_END;
        }
      }
    }

    if (includeVisual) {
      if (match != MATCH_STRING__NO_MATCH) {
        if (includePreTags)
          visualsReturnBuffer[0] = "<html><pre><font face="+HTML_utils.DEFAULT_FONTS_QUOTED+">" + visual + "</font></pre></html>";
        else
          visualsReturnBuffer[0] = "<html>" + visual + "</html>";
      } else if (alwaysArmorInHTML) {
        if (includePreTags)
          visualsReturnBuffer[0] = "<html><pre><font face="+HTML_utils.DEFAULT_FONTS_QUOTED+">" + Misc.encodePlainLineIntoHtmlLine(s1Orig) + "</font></pre></html>";
        else
          visualsReturnBuffer[0] = "<html>" + Misc.encodePlainLineIntoHtmlLine(s1Orig) + "</html>";
      }
    }
    return match;
  }

}
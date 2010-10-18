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

package com.CH_cl.service.records.filters;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.util.*;

import java.util.*;

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
public class TextSearchFilter extends AbstractRecordFilter implements RecordFilter {

  private String searchStr;
  private String[] searchTokens;
  private boolean includeMsgBodies;
  private SearchTextProviderI searchTextProvider;

  /** Creates new TextSearchFilter */
  public TextSearchFilter(String searchStr, boolean includeMsgBodies, SearchTextProviderI searchTextProvider) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TextSearchFilter.class, "TextSearchFilter(String searchStr, boolean includeMsgBodies, SearchTextProviderI searchTextProvider)");
    if (trace != null) trace.args(searchStr);
    if (trace != null) trace.args(includeMsgBodies);
    if (trace != null) trace.args(searchTextProvider);
    this.searchStr = searchStr;
    this.includeMsgBodies = includeMsgBodies;
    this.searchTextProvider = searchTextProvider;
    searchTokens = searchStr.split("[ ]+");
    if (trace != null) trace.exit(TextSearchFilter.class);
  }

  public boolean keep(Record record) {
    boolean keep = false;

    if (record instanceof FileLinkRecord) {
      FileLinkRecord fLink = (FileLinkRecord) record;
      if (isMatch(searchTextProvider.getSearchableCharSequencesFor(fLink)))
        keep = true;
    } else if (record instanceof MsgLinkRecord) {
      MsgLinkRecord mLink = (MsgLinkRecord) record;
      if (isMatch(searchTextProvider.getSearchableCharSequencesFor(mLink, includeMsgBodies)))
        keep = true;
    }

    return keep;
  }

  private boolean isMatch(Collection charSequences) {
    boolean isMatch = true;
    if (charSequences == null) {
      isMatch = false;
    } else if (charSequences.size() > 0 && searchTokens.length > 0) {
      for (int i=0; i<searchTokens.length; i++) {
        boolean anyFound = false;
        Iterator iter = charSequences.iterator();
        while (iter.hasNext()) {
          CharSequence charSeq = (CharSequence) iter.next();
          // charSeq maybe null in rare cases so check for it...
          if (charSeq != null) {
            if (contains(charSeq, searchTokens[i], true)) {
              anyFound = true;
              break;
            }
          }
        }
        if (!anyFound) {
          isMatch = false;
          break;
        }
      }
    }
    return isMatch;
  }

  private static boolean contains(CharSequence string, CharSequence phrase, boolean ignoreCase) {
    int stringLen = string.length();
    int subStringLen = phrase.length();
    if (subStringLen == 0)
      return true;
    else {
      for (int i = 0; i <= stringLen - subStringLen; i++) {
        if (regionMatches(ignoreCase, i, string, 0, phrase, subStringLen)) {
          return true;
        }
      }
    }
    return false;
  }
  private static boolean regionMatches(boolean ignoreCase, int strOffset, CharSequence string, int phraseOffset, CharSequence phrase, int len) {
    boolean match = true;
    for (int i=0; i<len; i++) {
      char c1 = string.charAt(i+strOffset);
      char c2 = phrase.charAt(i+phraseOffset);
      boolean same = c1 == c2 || (ignoreCase && Character.toLowerCase(c1) == Character.toLowerCase(c2));
      if (!same) {
        match = false;
        break;
      }
    }
    return match;
  }

  public String getSearchStr() {
    return searchStr;
  }

  public boolean isIncludingMsgBodies() {
    return includeMsgBodies;
  }

  public void setIncludingMsgBodies(boolean includeMsgBodies) {
    this.includeMsgBodies = includeMsgBodies;
  }

  public void setSearchTextProvider(SearchTextProviderI searchTextProvider) {
    this.searchTextProvider = searchTextProvider;
  }

  public String toString() {
    return "[TextSearchFilter"
      + ": searchStr=" + searchStr
      + ", includeMsgBodies=" + includeMsgBodies
      + ", searchTextProvider=" + searchTextProvider
      + "]";
  }
}
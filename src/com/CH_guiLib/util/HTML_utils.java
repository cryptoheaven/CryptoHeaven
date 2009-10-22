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

package com.CH_guiLib.util;

import com.CH_co.trace.*;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: 1.0 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class HTML_utils {

  public static String clearHTMLheaderAndConditionForDisplay(String htmlMessage, boolean isRemoveHead, boolean isRemoveLeadP, boolean isRemoveMap) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_utils.class, "clearHTMLheaderAndConditionForDisplay(String htmlMessage, boolean isRemoveHead, boolean isRemoveLeadP, boolean isRemoveMap)");
    if (trace != null) trace.args(htmlMessage != null && htmlMessage.length() < 255 ? htmlMessage : (htmlMessage != null ? "too long length="+htmlMessage.length() : "null"));
    if (trace != null) trace.args(isRemoveHead);
    if (trace != null) trace.args(isRemoveLeadP);
    if (trace != null) trace.args(isRemoveMap);

    if (htmlMessage != null) {
      if (isRemoveHead) {
        String[][] startTags = new String[][] {{ "<head>", "<HEAD>", "<head ", "<HEAD " }};
        String[][] endTags = new String[][] {{ "</head>", "</HEAD>" }};
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, null);
//        htmlMessage = ArrayUtils.replaceKeyWords(htmlMessage, new String[][] {
//                        {" src='http", " scr='http"},
//                        {" src=\"http", " scr=\"http"},
//                        {" SRC='HTTP", " SCR='HTTP"},
//                        {" SRC=\"HTTP", " SCR=\"HTTP"},
//                        {" SRC='http", " SCR='http"},
//                        {" SRC=\"http", " SCR=\"http"},
//                  });
      }

      boolean removedLeadP = !isRemoveLeadP;
      if (!removedLeadP) {
        int iStartP = htmlMessage.indexOf("<P>");
        int iStartP2 = htmlMessage.indexOf("</P>");
        int iStartB = htmlMessage.indexOf("<BODY");
        if (iStartB >= 0 && iStartP >= iStartB && iStartP2 > iStartP && iStartP2 + "</P>".length() < htmlMessage.length()) {
          int iStartB2 = htmlMessage.indexOf('>', iStartB);
          if (iStartB2 > iStartB && iStartB2 < iStartP) {
            htmlMessage = htmlMessage.substring(0, iStartP) + htmlMessage.substring(iStartP + "<P>".length(), iStartP2) + htmlMessage.substring(iStartP2 + "</P>".length());
            removedLeadP = true;
          }
        }
      }
      if (!removedLeadP) {
        int iStartP = htmlMessage.indexOf("<p>");
        int iStartP2 = htmlMessage.indexOf("</p>");
        int iStartB = htmlMessage.indexOf("<body");
        if (iStartB >= 0 && iStartP >= iStartB && iStartP2 > iStartP && iStartP2 + "</p>".length() < htmlMessage.length()) {
          int iStartB2 = htmlMessage.indexOf('>', iStartB);
          if (iStartB2 > iStartB && iStartB2 < iStartP) {
            htmlMessage = htmlMessage.substring(0, iStartP) + htmlMessage.substring(iStartP + "<p>".length(), iStartP2) + htmlMessage.substring(iStartP2 + "</p>".length());
            removedLeadP = true;
          }
        }
      }

      if (isRemoveMap) {
        String[][] startTags = new String[][] {{ "<map", "<MAP" }};
        String[][] endTags = new String[][] {{ "</map>", "</MAP>" }};
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, null);
      }

      {
        // remove SCRIPTs
        String[][] startTags = new String[][] {{ "<script", "<SCRIPT" }};
        String[][] endTags = new String[][] {{ "</script>", "</SCRIPT>" }};
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, null);
      }

      {
        String divStyleEmpty = "<div style='background-color:'>";
        int iStart = htmlMessage.indexOf(divStyleEmpty);
        while (iStart >= 0) {
          String buf = "";
          if (iStart > 0)
            buf = htmlMessage.substring(0, iStart);
          buf += "<div style='background-color:white'>";
          if (iStart + divStyleEmpty.length() < htmlMessage.length())
            buf += htmlMessage.substring(iStart + divStyleEmpty.length());
          htmlMessage = buf;
          iStart = htmlMessage.indexOf(divStyleEmpty);
        }
      }
    }

    String traceHTMLmsg = (htmlMessage != null && htmlMessage.length() < 255) ? htmlMessage : (htmlMessage != null ? "too long length="+htmlMessage.length() : "null");
    if (trace != null) trace.exit(HTML_utils.class, traceHTMLmsg);
    return htmlMessage;
  }
}

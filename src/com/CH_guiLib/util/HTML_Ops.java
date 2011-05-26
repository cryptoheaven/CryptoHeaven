/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
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
 * <b>Copyright</b> &copy; 2001-2011
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
public class HTML_Ops {

  public static String clearHTMLheaderAndConditionForDisplay(String htmlMessage, boolean isRemoveHead, boolean isRemoveLeadP, boolean isRemoveMap) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_Ops.class, "clearHTMLheaderAndConditionForDisplay(String htmlMessage, boolean isRemoveHead, boolean isRemoveLeadP, boolean isRemoveMap)");
    if (trace != null) trace.args(htmlMessage != null && htmlMessage.length() < 255 ? htmlMessage : (htmlMessage != null ? "too long length="+htmlMessage.length() : "null"));
    if (trace != null) trace.args(isRemoveHead);
    if (trace != null) trace.args(isRemoveLeadP);
    if (trace != null) trace.args(isRemoveMap);

    if (htmlMessage != null) {
      if (isRemoveHead) {
        boolean isBodyPresent = htmlMessage.indexOf("<body") >= 0 || htmlMessage.indexOf("<BODY") >= 0;
        String[][] startTags = new String[][] {{ "<head>", "<HEAD>", "<head ", "<HEAD " }};
        String[][] endTags = new String[][] {{ "</head>", "</HEAD>" }};
        String trimmedHtmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, null);
        if (isBodyPresent && trimmedHtmlMessage.indexOf("<body") < 0 && trimmedHtmlMessage.indexOf("<BODY") < 0) {
          // keep original html message because trimming the 'head' somehow removed the 'body' which is not right
        } else {
          htmlMessage = trimmedHtmlMessage;
        }
      }

      // noticed an email contained thousands of <v> and </v> tags which are invalid and crashed the client, so I removed them here
      htmlMessage = ArrayUtils.replaceKeyWords(htmlMessage,
        new String[][] {
          {"<v>", ""},
          {"<V>", ""},
          {"</v>", ""},
          {"</V>", ""},
      });

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

      // java renderer has trouble with empty 'background' stype and with #fff colors turning blue
      if (htmlMessage.indexOf("background:") >= 0)
        htmlMessage = ArrayUtils.replaceKeyWords(htmlMessage,
          new String[][] {
                  { "background: #fff;", "background: #ffffff;" },
                  { "background: #fff'", "background: #ffffff'" },
                  { "background: #fff\"", "background: #ffffff\"" },
                  { "background: #FFF;", "background: #ffffff;" },
                  { "background: #FFF'", "background: #ffffff'" },
                  { "background: #FFF\"", "background: #ffffff\"" },
                  { "background:#fff;", "background: #ffffff;" },
                  { "background:#fff'", "background: #ffffff'" },
                  { "background:#fff\"", "background: #ffffff\"" },
                  { "background:#FFF;", "background: #ffffff;" },
                  { "background:#FFF'", "background: #ffffff'" },
                  { "background:#FFF\"", "background: #ffffff\"" },
                  { "background: ;", "background: #ffffff;" },
                  { "background: '", "background: #ffffff'" },
                  { "background: \"", "background: #ffffff\"" },
                  { "background:;", "background: #ffffff;" },
                  { "background:'", "background: #ffffff'" },
                  { "background:\"", "background: #ffffff\"" },
                }, new String[] { "<" }, new String[] { ">" }, true);
      if (htmlMessage.indexOf("background-color:") >= 0)
        htmlMessage = ArrayUtils.replaceKeyWords(htmlMessage,
          new String[][] {
                  { "background-color: #fff;", "background-color: #ffffff;" },
                  { "background-color: #fff'", "background-color: #ffffff'" },
                  { "background-color: #fff\"", "background-color: #ffffff\"" },
                  { "background-color: #FFF;", "background-color: #ffffff;" },
                  { "background-color: #FFF'", "background-color: #ffffff'" },
                  { "background-color: #FFF\"", "background-color: #ffffff\"" },
                  { "background-color:#fff;", "background-color: #ffffff;" },
                  { "background-color:#fff'", "background-color: #ffffff'" },
                  { "background-color:#fff\"", "background-color: #ffffff\"" },
                  { "background-color:#FFF;", "background-color: #ffffff;" },
                  { "background-color:#FFF'", "background-color: #ffffff'" },
                  { "background-color:#FFF\"", "background-color: #ffffff\"" },
                  { "background-color: ;", "background-color: #ffffff;" },
                  { "background-color: '", "background-color: #ffffff'" },
                  { "background-color: \"", "background-color: #ffffff\"" },
                  { "background-color:;", "background-color: #ffffff;" },
                  { "background-color:'", "background-color: #ffffff'" },
                  { "background-color:\"", "background-color: #ffffff\"" },
                }, new String[] { "<" }, new String[] { ">" }, true);
    }

    String traceHTMLmsg = (htmlMessage != null && htmlMessage.length() < 255) ? htmlMessage : (htmlMessage != null ? "too long length="+htmlMessage.length() : "null");
    if (trace != null) trace.exit(HTML_Ops.class, traceHTMLmsg);
    return htmlMessage;
  }
}
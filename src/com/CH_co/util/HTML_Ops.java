/*
* Copyright 2001-2012 by CryptoHeaven Corp.,
* Mississauga, Ontario, Canada.
* All rights reserved.
*
* This software is the confidential and proprietary information
* of CryptoHeaven Corp. ("Confidential Information").  You
* shall not disclose such Confidential Information and shall use
* it only in accordance with the terms of the license agreement
* you entered into with CryptoHeaven Corp.
*/

package com.CH_co.util;

import com.CH_co.trace.Trace;

/** 
* <b>Copyright</b> &copy; 2001-2012
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
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

  public static String clearHTMLheaderAndConditionForDisplay(String htmlMessage, boolean isRemoveStyles, 
          boolean isRemoveHead, boolean isRemoveLeadP, boolean isRemoveMap, 
          boolean isRemoveComment, boolean isRemoveRemoteLoading, boolean isConvertPtoBR) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_Ops.class, "clearHTMLheaderAndConditionForDisplay(String htmlMessage, boolean isRemoveStyles, boolean isRemoveHead, boolean isRemoveLeadP, boolean isRemoveMap, boolean isRemoveComment, boolean isRemoveRemoteLoading, boolean isConvertPtoBR)");
    if (trace != null) trace.args(htmlMessage != null && htmlMessage.length() < 255 ? htmlMessage : (htmlMessage != null ? "too long length="+htmlMessage.length() : "null"));
    if (trace != null) trace.args(isRemoveStyles);
    if (trace != null) trace.args(isRemoveHead);
    if (trace != null) trace.args(isRemoveLeadP);
    if (trace != null) trace.args(isRemoveMap);
    if (trace != null) trace.args(isRemoveComment);
    if (trace != null) trace.args(isRemoveRemoteLoading);
    if (trace != null) trace.args(isConvertPtoBR);

    if (htmlMessage != null) {

      if (isRemoveStyles) {
        String[][] startTags = new String[][] {{ "<style", "<STYLE" }};
        String[][] endTags = new String[][] {{ "</style>", "</STYLE>" }};
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, null);
        startTags = new String[][] {{ "style=\"", "STYLE=\"", "class=\"", "CLASS=\"" }};
        endTags = new String[][] {{ "\"" }};
        String[] replacementTags = new String[] { "" };
        String[] outerBeginTags = new String[] { "<" };
        String[] outerEndTags = new String[] { ">" };
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, replacementTags, true, outerBeginTags, outerEndTags);
      }

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

      if (isRemoveComment) {
        String[][] startTags = new String[][] {{ "<!--" }};
        String[][] endTags = new String[][] {{ "-->" }};
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, null);
      }

      // removes loading of resources through styles
      if (isRemoveRemoteLoading) {
        // Removal example:
        // <img src="http://domain.com/image.jpg">    ---->    <img src="images/clear-pixel.gif">
        String[][] startTags = new String[][] {{ "src=\"http", "SRC=\"http", "src='http", "SRC='http"  }};
        String[][] endTags = new String[][] {{ "\"", "'" }};
        String[] replacementTags = new String[] { "src=\"images/clear-pixel.gif\"" };
        String[] outerBeginTags = new String[] { "<img", "<IMG" };
        String[] outerEndTags = new String[] { ">" };
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, replacementTags, true, outerBeginTags, outerEndTags);
        // Removal example:
        // <img src=http://domain.com/image.jpg>    ---->    <img src=xhttp://domain.com/image.jpg>
        // <img src=https://domain.com/image.jpg width=10>    ---->    <img src=xhttps://domain.com/image.jpg width=10>
        startTags = new String[][] {{ "src=http", "SRC=http" }};
        endTags = new String[][] {{ ":" }};
        replacementTags = new String[] { "src=xhttp:" };
        outerBeginTags = new String[] { "<img", "<IMG" };
        outerEndTags = new String[] { ">" };
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, replacementTags, true, outerBeginTags, outerEndTags);
        // Removal example:
        // <table bgcolor="#ff9900" width="620" border="0" background="http://www.axandra.com/images/newfactsheader.gif" cellspacing="0" cellpadding="0">
        startTags = new String[][] {{ "background=\"", "BACKGROUND=\"" }};
        endTags = new String[][] {{ "\"" }};
        replacementTags = new String[] { "" };
        outerBeginTags = new String[] { "<" };
        outerEndTags = new String[] { ">" };
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, replacementTags, true, outerBeginTags, outerEndTags);
        // Removal example:
        // <link rel="stylesheet" type="text/css" href="theme.css" />
        startTags = new String[][] {{ "href=\"", "HREF=\"" }};
        endTags = new String[][] {{ "\"" }};
        replacementTags = new String[] { "" };
        outerBeginTags = new String[] { "<link", "<LINK" };
        outerEndTags = new String[] { ">" };
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, replacementTags, true, outerBeginTags, outerEndTags);
        // Removal example:
        // <td style="background-image:url(http://img.en25.com/eloquaimages/clients/Tridel/{7c789a68-f648-42f2-aa2f-47a92ab8dbc8}_av2_nowavailable_bg_410.jpg);background-repeat:no-repeat;" width="410" align="left" valign="top">
        if (htmlMessage.indexOf("url(") >= 0 || htmlMessage.indexOf("URL(") >= 0) {
          htmlMessage = ArrayUtils.replaceKeyWords(htmlMessage,
                  new String[][] {
                    // make it not-loadable, comment out the url
                    { ":url(", ":urlx(" },
                    { " url(", " urlx(" },
                    { ":URL(", ":URLX(" },
                    { " URL(", " URLX(" },
                  }, new String[] { "<" }, new String[] { ">" }, true);
        }
      }

      if (isConvertPtoBR) {
        // First eliminate double spaces inside formatting tags.
        // Don't touch double spaces else where as they maybe renderable when inside <PRE></PRE> tags
        htmlMessage = ArrayUtils.replaceKeyWords(htmlMessage,
          new String[][] {
            { "  ", " " },
          }, new String[] { "<" }, new String[] { ">" }, true);
        // change all <p> into <br> tags
        htmlMessage = ArrayUtils.replaceKeyWords(htmlMessage,
          new String[][] {
            { "<p>", "<br>" },
            { "<P>", "<br>" },
            { "<p >", "<br>" },
            { "<P >", "<br>" },
            { "</p>", " " },
            { "</P>", " " },
          }, null, null, true);
      }

      // Always remove unsupported CID sources
      {
        // Removal example:
        // <img src="cid:24234234234">    ---->    <img src="images/clear-pixel.gif">
        String[][] startTags = new String[][] {{ "src=\"cid", "SRC=\"cid", "src='cid", "SRC='cid" }};
        String[][] endTags = new String[][] {{ "\"", "'" }};
        String[] replacementTags = new String[] { "src=\"images/clear-pixel.gif\"" };
        String[] outerBeginTags = new String[] { "<img", "<IMG" };
        String[] outerEndTags = new String[] { ">" };
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, replacementTags, true, outerBeginTags, outerEndTags);
        startTags = new String[][] {{ "src=cid", "SRC=cid" }};
        endTags = new String[][] {{ ":" }};
        replacementTags = new String[] { "src=xcid:" };
        outerBeginTags = new String[] { "<img", "<IMG" };
        outerEndTags = new String[] { ">" };
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, replacementTags, true, outerBeginTags, outerEndTags);
      }

      // Always remove scripts
      {
        // remove SCRIPTs
        String[][] startTags = new String[][] {{ "<script", "<SCRIPT" }};
        String[][] endTags = new String[][] {{ "</script>", "</SCRIPT>" }};
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, null);
        // remove SCRIPT actions
        startTags = new String[][] {{ "onselectstart=\"", "ONSELECTSTART=\"" }};
        endTags = new String[][] {{ "\"" }};
        String[] replacementTags = new String[] { "" };
        String[] outerBeginTags = new String[] { "<" };
        String[] outerEndTags = new String[] { ">" };
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, replacementTags, true, outerBeginTags, outerEndTags);
      }

      // java renderer has trouble with empty 'background' style and with #fff colors turning blue
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

      // java renderer has trouble with rgb(256, 256, 256) values that have a space between numbers
      if (htmlMessage.indexOf("rgb(") >= 0) {
        String[][] startTags = new String[][] {{ "rgb(" }};
        String[][] endTags = new String[][] {{ ")" }};
        Object[] replacementTags = new Object[] { new CallbackReturnI() {
          public Object callback(Object value) {
            return ((String) value).replaceAll(" ", "");
          }
        }};
        String[] outerBeginTags = new String[] { "<" };
        String[] outerEndTags = new String[] { ">" };
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, replacementTags, true, outerBeginTags, outerEndTags);
      }
    }

    String traceHTMLmsg = (htmlMessage != null && htmlMessage.length() < 255) ? htmlMessage : (htmlMessage != null ? "too long length="+htmlMessage.length() : "null");
    if (trace != null) trace.exit(HTML_Ops.class, traceHTMLmsg);
    return htmlMessage;
  }
}
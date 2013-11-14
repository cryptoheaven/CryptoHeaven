/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.util;

import com.CH_co.trace.Trace;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/** 
* Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: $</b>
*
* @author  Marcin Kurzawa
*/
public class HTML_Ops {

  public static String clearHTMLheaderAndConditionForDisplay(String htmlMessage, boolean isRemoveStyles, boolean isRemoveInlineStyles,
          boolean isRemoveHead, boolean isRemoveLeadP, boolean isRemoveMap, 
          boolean isRemoveComment, boolean isRemoveRemoteLoading, boolean isConvertPtoBR) {
    StringBuffer htmlMsgReturn = null;
    if (htmlMessage != null) {
      StringBuffer htmlMsgSB = new StringBuffer(htmlMessage);
      htmlMsgReturn = clearHTMLheaderAndConditionForDisplay(htmlMsgSB, isRemoveStyles, isRemoveInlineStyles, 
              isRemoveHead, isRemoveLeadP, isRemoveMap, 
              isRemoveComment, isRemoveRemoteLoading, isConvertPtoBR, null);
    }
    return htmlMsgReturn != null ? htmlMsgReturn.toString() : null;
  }
  public static StringBuffer clearHTMLheaderAndConditionForDisplay(StringBuffer htmlMessage, boolean isRemoveStyles, boolean isRemoveInlineStyles,
          boolean isRemoveHead, boolean isRemoveLeadP, boolean isRemoveMap, 
          boolean isRemoveComment, boolean isRemoveRemoteLoading, boolean isConvertPtoBR, StringBuffer reportSB) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTML_Ops.class, "clearHTMLheaderAndConditionForDisplay(String htmlMessage, boolean isRemoveStyles, boolean isRemoveInlineStyles, boolean isRemoveHead, boolean isRemoveLeadP, boolean isRemoveMap, boolean isRemoveComment, boolean isRemoveRemoteLoading, boolean isConvertPtoBR)");
    if (trace != null) trace.args(htmlMessage != null && htmlMessage.length() < 255 ? htmlMessage.toString() : (htmlMessage != null ? "too long length="+htmlMessage.length() : "null"));
    if (trace != null) trace.args(isRemoveStyles);
    if (trace != null) trace.args(isRemoveInlineStyles);
    if (trace != null) trace.args(isRemoveHead);
    if (trace != null) trace.args(isRemoveLeadP);
    if (trace != null) trace.args(isRemoveMap);
    if (trace != null) trace.args(isRemoveComment);
    if (trace != null) trace.args(isRemoveRemoteLoading);
    if (trace != null) trace.args(isConvertPtoBR);

    // timing
    long start, end;
    
    if (htmlMessage != null) {

      if (isRemoveStyles) {
        start = System.currentTimeMillis();
        String[][] startTags = new String[][] {{ "<style", "<STYLE" }};
        String[][] endTags = new String[][] {{ "</style>", "</STYLE>" }};
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, null);
        end = System.currentTimeMillis();
        if (reportSB != null) reportSB.append("removed styles in ").append(end-start).append(" ms.\n");
      }
      
      if (isRemoveInlineStyles) {
        start = System.currentTimeMillis();
        String[][] startTags = new String[][] {{ "style=\"", "STYLE=\"", "class=\"", "CLASS=\"" }};
        String[][] endTags = new String[][] {{ "\"" }};
        String[] replacementTags = new String[] { "" };
        String[] outerBeginTags = new String[] { "<" };
        String[] outerEndTags = new String[] { ">" };
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, replacementTags, true, outerBeginTags, outerEndTags);
        end = System.currentTimeMillis();
        if (reportSB != null) reportSB.append("removed inline styles in ").append(end-start).append(" ms.\n");
      }

      if (isRemoveHead) {
        start = System.currentTimeMillis();
        boolean isBodyPresent = htmlMessage.indexOf("<body") >= 0 || htmlMessage.indexOf("<BODY") >= 0;
        String[][] startTags = new String[][] {{ "<head>", "<HEAD>", "<head ", "<HEAD " }};
        String[][] endTags = new String[][] {{ "</head>", "</HEAD>" }};
        StringBuffer trimmedHtmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, null);
        if (isBodyPresent && trimmedHtmlMessage.indexOf("<body") < 0 && trimmedHtmlMessage.indexOf("<BODY") < 0) {
          // keep original html message because trimming the 'head' somehow removed the 'body' which is not right
        } else {
          htmlMessage = trimmedHtmlMessage;
        }
        end = System.currentTimeMillis();
        if (reportSB != null) reportSB.append("removed head in ").append(end-start).append(" ms.\n");
      }

      // noticed an email contained thousands of <v> and </v> tags which are invalid and crashed the client, so I removed them here
      {
        start = System.currentTimeMillis();
        htmlMessage = ArrayUtils.replaceKeyWords(htmlMessage,
          new String[][] {
            {"<v>", ""},
            {"<V>", ""},
            {"</v>", ""},
            {"</V>", ""},
        });
        end = System.currentTimeMillis();
        if (reportSB != null) reportSB.append("removed invalid v's in ").append(end-start).append(" ms.\n");
      }

      boolean removedLeadP = !isRemoveLeadP;
      if (!removedLeadP) {
        start = System.currentTimeMillis();
        int iStartP = htmlMessage.indexOf("<P>");
        int iStartP2 = htmlMessage.indexOf("</P>");
        int iStartB = htmlMessage.indexOf("<BODY");
        if (iStartB >= 0 && iStartP >= iStartB && iStartP2 > iStartP && iStartP2 + "</P>".length() < htmlMessage.length()) {
          int iStartB2 = htmlMessage.indexOf(">", iStartB);
          if (iStartB2 > iStartB && iStartB2 < iStartP) {
            htmlMessage = new StringBuffer(htmlMessage.substring(0, iStartP) + htmlMessage.substring(iStartP + "<P>".length(), iStartP2) + htmlMessage.substring(iStartP2 + "</P>".length()));
//            htmlMessage = htmlMessage.substring(0, iStartP) + htmlMessage.substring(iStartP + "<P>".length(), iStartP2) + htmlMessage.substring(iStartP2 + "</P>".length());
            removedLeadP = true;
          }
        }
        end = System.currentTimeMillis();
        if (reportSB != null) reportSB.append("removed lead Ps1 in ").append(end-start).append(" ms.\n");
      }
      if (!removedLeadP) {
        start = System.currentTimeMillis();
        int iStartP = htmlMessage.indexOf("<p>");
        int iStartP2 = htmlMessage.indexOf("</p>");
        int iStartB = htmlMessage.indexOf("<body");
        if (iStartB >= 0 && iStartP >= iStartB && iStartP2 > iStartP && iStartP2 + "</p>".length() < htmlMessage.length()) {
          int iStartB2 = htmlMessage.indexOf(">", iStartB);
          if (iStartB2 > iStartB && iStartB2 < iStartP) {
            htmlMessage = new StringBuffer(htmlMessage.substring(0, iStartP) + htmlMessage.substring(iStartP + "<p>".length(), iStartP2) + htmlMessage.substring(iStartP2 + "</p>".length()));
//            htmlMessage = htmlMessage.substring(0, iStartP) + htmlMessage.substring(iStartP + "<p>".length(), iStartP2) + htmlMessage.substring(iStartP2 + "</p>".length());
            removedLeadP = true;
          }
        }
        end = System.currentTimeMillis();
        if (reportSB != null) reportSB.append("removed lead Ps2 in ").append(end-start).append(" ms.\n");
      }

      if (isRemoveMap) {
        start = System.currentTimeMillis();
        String[][] startTags = new String[][] {{ "<map", "<MAP" }};
        String[][] endTags = new String[][] {{ "</map>", "</MAP>" }};
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, null);
        end = System.currentTimeMillis();
        if (reportSB != null) reportSB.append("removed map in ").append(end-start).append(" ms.\n");
      }

      if (isRemoveComment) {
        start = System.currentTimeMillis();
        String[][] startTags = new String[][] {{ "<!--" }};
        String[][] endTags = new String[][] {{ "-->" }};
        htmlMessage = ArrayUtils.removeTags(htmlMessage, startTags, endTags, null);
        end = System.currentTimeMillis();
        if (reportSB != null) reportSB.append("removed comment in ").append(end-start).append(" ms.\n");
      }

      // removes loading of resources through styles
      if (isRemoveRemoteLoading) {
        start = System.currentTimeMillis();
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
        end = System.currentTimeMillis();
        if (reportSB != null) reportSB.append("removed remote loading in ").append(end-start).append(" ms.\n");
      }

      if (isConvertPtoBR) {
        start = System.currentTimeMillis();
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
        end = System.currentTimeMillis();
        if (reportSB != null) reportSB.append("converted Ps to BRs in ").append(end-start).append(" ms.\n");
      }

      // Always remove unsupported CID sources
      {
        start = System.currentTimeMillis();
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
        end = System.currentTimeMillis();
        if (reportSB != null) reportSB.append("removed unsupported CIDs in ").append(end-start).append(" ms.\n");
      }

      // Always remove scripts
      {
        start = System.currentTimeMillis();
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
        end = System.currentTimeMillis();
        if (reportSB != null) reportSB.append("removed scripts in ").append(end-start).append(" ms.\n");
      }

      // java renderer has trouble with empty 'background' style and with #fff colors turning blue
      {
        start = System.currentTimeMillis();
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
        end = System.currentTimeMillis();
        if (reportSB != null) reportSB.append("converted background colors in ").append(end-start).append(" ms.\n");
      }

      // java renderer has trouble with rgb(256, 256, 256) values that have a space between numbers
      {
        start = System.currentTimeMillis();
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
        end = System.currentTimeMillis();
        if (reportSB != null) reportSB.append("converted RGBs in ").append(end-start).append(" ms.\n");
      }
    }

    String traceHTMLmsg = (htmlMessage != null && htmlMessage.length() < 255) ? htmlMessage.toString() : (htmlMessage != null ? "too long length="+htmlMessage.length() : "null");
    if (trace != null) trace.exit(HTML_Ops.class, traceHTMLmsg);
    return htmlMessage;
  }
  
  public static void main(String[] args) {
    if (args == null || args.length == 0) {
      System.out.println("Specify input HTML file.");
      return;
    } else {
      try {
        File inFile = new File(args[0]);
        BufferedReader bf = new BufferedReader(new FileReader(inFile));
        StringBuffer sb = new StringBuffer();
        String line = bf.readLine();
        while (line != null) {
          sb.append(line).append("\n");
          line = bf.readLine();
        }

        StringBuffer reportSB = null;
        
        String html = sb.toString();
        StringBuffer htmlSB = new StringBuffer(html);
        System.out.println("Timing clear function...");
        long start = System.currentTimeMillis();
        int REPS = 10;
        for (int i=0; i<REPS; i++) {
          reportSB = new StringBuffer();
          clearHTMLheaderAndConditionForDisplay(htmlSB, true, true, true, true, true, true, false, false, reportSB);
        }
        long end = System.currentTimeMillis();
        System.out.println("completed " + REPS + " reps in " + (end-start) + " ms.");
        System.out.println("report: "+reportSB.toString());

      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }
}
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

package com.CH_gui.msgs;

import com.CH_cl.service.cache.CacheEmlUtils;
import com.CH_cl.service.cache.CacheMsgUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.TextRenderer;
import com.CH_cl.service.ops.UserOps;
import com.CH_cl.service.records.EmailAddressRecord;
import com.CH_cl.service.records.filters.FolderFilter;
import com.CH_co.cryptx.BASymCipherBulk;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.MsgFilter;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.MyHTMLEditor;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.util.HTML_ClickablePane;
import com.CH_gui.util.MessageDialog;
import com.CH_gui.util.MiscGui;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

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
* <b>$Revision: 1.37 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class MsgPanelUtils extends Object {

  /**
  * Finds a matching EmailRecord that is our Recipient for the specified Message
  * @param originalMsg
  * @return
  */
  public static EmailRecord getOurMatchingFromEmlRec(MsgDataRecord originalMsg) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPanelUtils.class, "getOurMatchingFromEmlRec(MsgDataRecord originalMsg)");
    if (trace != null) trace.args(originalMsg);

    EmailRecord ourMatchingEmlRec = null;
    if (originalMsg != null) {
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      EmailRecord[] emlRecs = cache.getEmailRecords(cache.getMyUserId());
      if (emlRecs.length > 0) {
        Record[][] recipients = CacheMsgUtils.gatherAllMsgRecipients(originalMsg);
        for (int recipientType=0; recipientType<recipients.length; recipientType++) {
          if (recipients[recipientType] != null) {
            for (int i=0; i<recipients[recipientType].length; i++) {
              Record recipient = recipients[recipientType][i];
              if (recipient instanceof EmailAddressRecord) {
                String emlAddr = ((EmailAddressRecord) recipient).address;
                for (int k=0; k<emlRecs.length; k++) {
                  if (EmailRecord.isAddressEqual(emlRecs[k].emailAddr, emlAddr)) {
                    ourMatchingEmlRec = emlRecs[k];
                    break;
                  }
                }
              }
              if (ourMatchingEmlRec != null) break;
            }
          }
          if (ourMatchingEmlRec != null) break;
        }
      }
    }
    if (trace != null) trace.exit(MsgPanelUtils.class, ourMatchingEmlRec);
    return ourMatchingEmlRec;
  }


  /**
  * Gathers all recipients into a Record[] and uses static method from MsgComposePanel
  * to draw the recipients panel.
  */
  public static void drawMsgRecipientsPanel(MsgDataRecord dataRecord, JPanel jRecipients, boolean includeLabels, boolean includeTO, boolean includeCC, boolean includeBCC) {
    drawMsgRecipientsPanel(dataRecord, jRecipients, null, includeLabels, includeTO, includeCC, includeBCC);
  }
  private static void drawMsgRecipientsPanel(MsgDataRecord dataRecord, JPanel jRecipients, Dimension maxSize, boolean includeLabels, boolean includeTO, boolean includeCC, boolean includeBCC) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPanelUtils.class, "drawMsgRecipientsPanel(MsgDataRecord dataRecord, JPanel jRecipients, Dimension maxSize, boolean includeLabels, boolean includeTO, boolean includeCC, boolean includeBCC)");
    if (trace != null) trace.args(dataRecord, jRecipients, maxSize);
    if (trace != null) trace.args(includeLabels);
    if (trace != null) trace.args(includeTO);
    if (trace != null) trace.args(includeCC);
    if (trace != null) trace.args(includeBCC);

    Record[][] recipients = null;
    if (dataRecord != null)
      recipients = CacheMsgUtils.gatherAllMsgRecipients(dataRecord);
    drawRecordFlowPanel(recipients,
                        new Boolean[] { Boolean.valueOf(includeTO), Boolean.valueOf(includeCC), Boolean.valueOf(includeBCC) },
                        includeLabels ? new String[] { com.CH_cl.lang.Lang.rb.getString("label_To"), com.CH_cl.lang.Lang.rb.getString("label_Cc"), com.CH_cl.lang.Lang.rb.getString("label_Bcc") } : null,
                        jRecipients, null, null, maxSize);
    if (trace != null) trace.exit(MsgPanelUtils.class);
  }


  /**
  * @param objs include all objects renderable by ListRenderer.
  */
  public static void drawRecordFlowPanel(Object[] objs, JPanel jFlowPanel) {
    drawRecordFlowPanel(new Object[][] { objs }, new Boolean[] { Boolean.TRUE }, new String[] { null }, jFlowPanel, null, null, null);
  }
  public static void drawRecordFlowPanel(Object[][] objSets, Boolean[] includeSets, String[] setHeaders, JPanel jFlowPanel, JLabel[] jHeaderRenderers, JLabel[] jLabelRenderers) {
    drawRecordFlowPanel(objSets, includeSets, setHeaders, jFlowPanel, jHeaderRenderers, jLabelRenderers, null);
  }
  public static void drawRecordFlowPanel(final Object[][] objSets, final Boolean[] includeSets, final String[] setHeaders, final JPanel jFlowPanel, final JLabel[] jHeaderRenderers, final JLabel[] jLabelRenderers, final Dimension maxSize) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPanelUtils.class, "drawRecordFlowPanel(Object[] objSets, Boolean[] includeSets, String[] setHeaders, JPanel jFlowPanel, JLabel[] jHeaderRenderers, JLabel[] jLabelRenderers, Dimension maxSize)");
    if (trace != null) trace.args(objSets, includeSets, setHeaders, jFlowPanel, jHeaderRenderers, jLabelRenderers, maxSize);

    MiscGui.removeAllComponentsAndListeners(jFlowPanel);
    //jFlowPanel.removeAll();
    Container parent = jFlowPanel.getParent();
    if (parent != null) {
      parent.doLayout();
    }
    jFlowPanel.doLayout();

    // due to Java 1.6 library bugs, keep header and label renderers seperate, library doesn't switch well between HTML and plain labels
    int headerRendererIndex = jHeaderRenderers != null ? 0 : -1;
    int labelRendererIndex = jLabelRenderers != null ? 0 : -1;

    if (includeSets != null) {
      for (int setIndex=0; setIndex<objSets.length; setIndex++) {
        if (includeSets.length > setIndex) {
          Object[] objs = objSets[setIndex];
          if (includeSets[setIndex].equals(Boolean.TRUE) && objs != null && objs.length > 0) {
            String setHeader = setHeaders != null && setHeaders.length > setIndex ? setHeaders[setIndex] : null;
            if (setHeader != null) {
              JLabel label = null;
              if (headerRendererIndex >= 0 && headerRendererIndex < jHeaderRenderers.length) {
                label = jHeaderRenderers[headerRendererIndex];
                headerRendererIndex ++;
              } else {
                label = new JMyLabel();
              }
              label.setText(HTML_utils.HTML_START + "<b>"+setHeader+"</b>" + HTML_utils.HTML_END);
              label.setBorder(new EmptyBorder(2,0,2,5));
              label.setIcon(null);
              label.setIconTextGap(5);
              jFlowPanel.add(label);
            }

            if (objs != null && objs.length > 0) {
              for (int i=0; i<objs.length; i++) {
                Object obj = objs[i];
                JLabel label = null;
                if (labelRendererIndex >= 0 && labelRendererIndex < jLabelRenderers.length) {
                  label = jLabelRenderers[labelRendererIndex];
                  labelRendererIndex ++;
                } else {
                  label = new JMyLabel();
                }
                label.setBorder(new EmptyBorder(2,0,2,5));
                // just for display convert any EmailAddressRecord to familiar Address Book entry
                if (obj instanceof EmailAddressRecord) {
                  obj = CacheEmlUtils.convertToFamiliarEmailRecord(((EmailAddressRecord) obj).address);
                }
                label.setIcon(ListRenderer.getRenderedIcon(obj, true));
                label.setText(ListRenderer.getRenderedText(obj, false, false, true, true, true));
                label.setIconTextGap(2);
                jFlowPanel.add(label);
              }
            }
          }
        }
      } // end for all sets
    } // end if any sets included

    // resize the flow panel
    jFlowPanel.doLayout();
    Component[] comps = jFlowPanel.getComponents();
    if (comps != null) {
      FlowLayout fl = (FlowLayout) jFlowPanel.getLayout();
      int hGap = fl.getHgap();
      int vGap = fl.getVgap();
      Dimension panelDim = jFlowPanel.getSize();
      int preferredHeight = calculatePreferredFlowLayoutHeight(panelDim, hGap, vGap, comps);
      Dimension dim = new Dimension(panelDim.width, preferredHeight);
      jFlowPanel.setPreferredSize(dim);
      jFlowPanel.setMinimumSize(dim);
    }

    // validate the entire panel, maybe additional lines are required
    jFlowPanel.doLayout();
    jFlowPanel.revalidate();
    jFlowPanel.repaint();

    if (trace != null) trace.exit(MsgPanelUtils.class);
  }

  /**
  * Calculate desired flow panel height given a fixed width.
  */
  private static int calculatePreferredFlowLayoutHeight(Dimension panelDim, int hGap, int vGap, Component[] comps) {
    int maxWidth = panelDim.width;
    int prevLinesHeight = 2 * vGap;
    int currentLineHeight = 0;
    int lineWidth = hGap;
    int countLineItems = 0;
    for (int i=0; i<comps.length; i++) {
      Component c = comps[i];
      Dimension d = c.getPreferredSize();
      boolean enoughWidthInLine = true;
      // is there enough space in the current line
      if (countLineItems > 0) {
        int newWidth = lineWidth + hGap + d.width;
        if (newWidth <= maxWidth) {
          enoughWidthInLine = true;
        } else {
          enoughWidthInLine = false;
        }
      } else if (countLineItems == 0) {
        enoughWidthInLine = true;
      }
      if (enoughWidthInLine) {
        countLineItems ++;
        if (countLineItems > 1)
          lineWidth += hGap + d.width;
        else
          lineWidth = hGap + d.width; // horizontal gap always leads, but doesn't lag the items on the line
        currentLineHeight = Math.max(currentLineHeight, d.height);
      } else {
        prevLinesHeight += currentLineHeight + vGap;
        currentLineHeight = d.height;
        lineWidth = hGap + d.width; // horizontal gap always leads, but doesn't lag the items on the line
        countLineItems = 1;
      }
    }
    int allLinesHeight = prevLinesHeight + currentLineHeight;
    return allLinesHeight;
  }

  /**
  * Sets the message text component with specified content, tries to
  * fight certain exceptions and wrap the content so that it is not changed
  * visually but made displayable.
  */
  public static boolean setMessageContent(String content, boolean isHTML, JComponent jMessage, boolean skipCaretPlacement) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPanelUtils.class, "setMessageContent(String content, boolean isHTML, JComponent jMessage, boolean skipHeaderClearing, boolean skipRemoteLoadingCleaning, boolean skipCaretPlacement)");
    if (trace != null) trace.args(content == null || content.length() < 255 ? content : "too long length="+content.length());
    if (trace != null) trace.args(isHTML);
    if (trace != null) trace.args(jMessage);
    if (trace != null) trace.args(skipCaretPlacement);

    boolean displayedOk = false;
    if (content == null) {
      content = "";
    }

    // clear the message first
    if (jMessage instanceof JTextComponent) {
      JTextComponent jTextMessage = (JTextComponent) jMessage;
      try {
        jTextMessage.getDocument().remove(0, jTextMessage.getDocument().getLength());
      } catch (Throwable t) {
        if (trace != null) trace.exception(MsgPanelUtils.class, 8, t);
      }
      try {
        jTextMessage.setText("");
      } catch (Throwable t) {
        if (trace != null) trace.exception(MsgPanelUtils.class, 9, t);
      }
      // Remove old styles if any
      try {
        Document doc = jTextMessage.getDocument();
        if (doc instanceof DefaultStyledDocument) {
          DefaultStyledDocument sDoc = (DefaultStyledDocument) doc;
          Enumeration enm = sDoc.getStyleNames();
          ArrayList stylesL = new ArrayList();
          while (enm.hasMoreElements()) {
            stylesL.add(enm.nextElement());
          }
          for (int i=0; i<stylesL.size(); i++) {
            String styleName = (String) stylesL.get(i);
            sDoc.removeStyle(styleName);
          }
        }
      } catch (Throwable t) {
        if (trace != null) trace.exception(MsgPanelUtils.class, 10, t);
      }
    }

    // Remove old components/Views if any
    try {
      if (jMessage instanceof JTextComponent) {
        Container cnt = jMessage;
        Component[] comps = cnt.getComponents();
        if (comps != null) {
          for (int i=0; i<comps.length; i++) {
            Component comp = comps[i];
            if (comp instanceof Container) {
              Container cnt2 = (Container) comp;
              Component[] comps2 = cnt2.getComponents();
              if (comps2 != null) {
                for (int j=0; j<comps2.length; j++) {
                  Component comp2 = comps2[j];
                  cnt2.remove(comp2);
                }
              }
            }
            cnt.remove(comp);
          }
        }
      }
    } catch (Throwable t) {
      if (trace != null) trace.exception(MsgPanelUtils.class, 11, t);
    }

    String text = content;

    // first try original data
    {
      text = content;
      try {
        if (trace != null) trace.data(12, MsgPanelUtils.class, "try 0");
        setText(jMessage, text);
        contentSet(text, jMessage);
        if (trace != null) trace.data(13, "setText length", text.length());
        displayedOk = true;
      } catch (Throwable t) {
        if (trace != null) trace.exception(MsgPanelUtils.class, 14, t);
      }
    }
    if (!displayedOk) {
      try {
        if (trace != null) trace.data(15, MsgPanelUtils.class, "try 1");
        if (isHTML)
          text = HTML_utils.HTML_START + HTML_utils.HTML_BODY_START + content + HTML_utils.HTML_BODY_END + HTML_utils.HTML_END;
        setText(jMessage, text);
        contentSet(text, jMessage);
        if (trace != null) trace.data(16, "setText length", text.length());
        displayedOk = true;
      } catch (Throwable t) {
        if (trace != null) trace.exception(MsgPanelUtils.class, 17, t);
      }
    }
    if (!displayedOk) {
      try {
        if (trace != null) trace.data(20, MsgPanelUtils.class, "try 2");
        if (isHTML)
          text = HTML_utils.HTML_START + content + HTML_utils.HTML_END;
        setText(jMessage, text);
        contentSet(text, jMessage);
        if (trace != null) trace.data(21, "setText length", text.length());
        displayedOk = true;
      } catch (Throwable t) {
        if (trace != null) trace.exception(MsgPanelUtils.class, 22, t);
      }
    }
    if (!displayedOk && isHTML) {
      text = HTML_utils.HTML_START + "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>" + content + "</td></tr></table>" + HTML_utils.HTML_END;
      try {
        if (trace != null) trace.data(30, MsgPanelUtils.class, "try 3");
        setText(jMessage, text);
        contentSet(text, jMessage);
        if (trace != null) trace.data(31, "setText length", text.length());
        displayedOk = true;
      } catch (Throwable t) {
        if (trace != null) trace.exception(MsgPanelUtils.class, 35, t);
      }
    }
    if (!displayedOk && isHTML) {
      text = HTML_utils.HTML_START + "<body><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>" + content + "</td></tr></table></body>" + HTML_utils.HTML_END;
      try {
        if (trace != null) trace.data(40, MsgPanelUtils.class, "try 4");
        setText(jMessage, text);
        contentSet(text, jMessage);
        if (trace != null) trace.data(41, "setText length", text.length());
        displayedOk = true;
      } catch (Throwable t) {
        if (trace != null) trace.exception(MsgPanelUtils.class, 45, t);
      }
    }
    if (!displayedOk) {
      //System.out.println("failed content="+content);
      try {
        if (content == null) {
          String t = com.CH_cl.lang.Lang.rb.getString("msg_Message_Body_could_not_be_fetched.");
          setText(jMessage, t);
          //displayedOk = true;
        } else if (isHTML) {
          String t = com.CH_cl.lang.Lang.rb.getString("msg_This_message_contains_an_invalid_HTML_code_and_cannot_be_displayed...");
          setText(jMessage, t);
          //displayedOk = true;
        } else {
          String t = com.CH_cl.lang.Lang.rb.getString("msg_This_message_contains_invalid_content_and_cannot_be_displayed...");
          setText(jMessage, t);
          //displayedOk = true;
        }
      } catch (Throwable t) {
        if (trace != null) trace.exception(MsgPanelUtils.class, 80, t);
      }
    }
    if (displayedOk && !skipCaretPlacement) {
      try {
        if (jMessage instanceof JTextComponent) {
          ((JTextComponent) jMessage).setCaretPosition(0);
        }
      } catch (Throwable t) {
        if (trace != null) trace.exception(MsgPanelUtils.class, 100, t);
      }
    }
    if (jMessage instanceof HTML_ClickablePane) {
      try {
        ((HTML_ClickablePane) jMessage).initFont();
      } catch (Throwable t) {
        if (trace != null) trace.exception(MsgPanelUtils.class, 200, t);
      }
    }
    if (jMessage.isVisible()) {
      try {
        jMessage.revalidate();
      } catch (Throwable t) {
      } try {
        jMessage.doLayout();
      } catch (Throwable t) {
      } try {
        jMessage.repaint();
      } catch (Throwable t) {
      }
    }

    if (trace != null) trace.exit(MsgPanelUtils.class, displayedOk);
    return displayedOk;
  }

  private static void setText(JComponent jMessage, String text) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPanelUtils.class, "setText(JComponent jMessage, String text)");
    if (trace != null) trace.args(jMessage, text);

    if (jMessage instanceof JTextComponent) {
      ((JTextComponent) jMessage).setText(text);
    } else if (jMessage instanceof MyHTMLEditor) {
      try {
        ((MyHTMLEditor) jMessage).setContent(text);
      } catch (Exception x) {
      }
    }

    if (trace != null) trace.exit(MsgPanelUtils.class);
  }

  private static boolean ENABLE_DEBUG_MSG_PANE = false;
  private static JTextPane msgDebugTextPane = null;
  private static JComponent jMessageHandle = null;
  private static void contentSet(String text, JComponent jMessage) {
    if (ENABLE_DEBUG_MSG_PANE) {
      if (msgDebugTextPane == null) {
        JFrame msgPrev = new JFrame("content Set text");
        msgPrev.setSize(900, 900);
        msgPrev.setVisible(true);
        Container cont = msgPrev.getContentPane();
        cont.setLayout(new BorderLayout());
        msgDebugTextPane = new JTextPane();
        cont.add(new JScrollPane(msgDebugTextPane), BorderLayout.CENTER);
        JButton apply = new JButton("Apply");
        cont.add(apply, BorderLayout.SOUTH);
        apply.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            MsgPanelUtils.setText(jMessageHandle, msgDebugTextPane.getText());
          }
        });
      }
      msgDebugTextPane.setText(text);
      msgDebugTextPane.repaint();
      jMessageHandle = jMessage;
    }
  }

  public static String getSigText(UserSettingsRecord userSettingsRecord, boolean inHtmlMode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPanelUtils.class, "getSigText(UserSettingsRecord userSettingsRecord, boolean inHtmlMode)");
    if (trace != null) trace.args(userSettingsRecord);
    if (trace != null) trace.args(inHtmlMode);

    boolean isHtmlMode = getSigType(userSettingsRecord).equals("text/html");
    String text = "";
    String defaultSig = userSettingsRecord.getDefaultSig();
    String defaultSigType = userSettingsRecord.getDefaultSigType();
    if (defaultSig != null) {
      if (defaultSigType.equalsIgnoreCase("text/file")) {
        String error = null;
        try {
          BufferedReader br = new BufferedReader(new FileReader(defaultSig));
          StringBuffer sb = new StringBuffer();
          String line = null;
          while ((line=br.readLine()) != null) {
            if (sb.length() > 0)
              sb.append("\n");
            sb.append(line);
          }
          br.close();
          text = sb.toString();
          // check all characters of the signature for validity
          for (int i=0; i<text.length(); i++) {
            char ch = text.charAt(i);
            if (!Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch) && (ch < 32 || ch > 126)) {
              error = "The signature file specified is not a valid text-file.";
              break;
            }
          }
          if (error != null)
            text = "";
        } catch (Throwable t) {
          error = "The signature text-file could not be loaded. \n\nError code is: " + t.getMessage();
        }
        if (error != null) {
          MessageDialog.showWarningDialog(null, error, "Invalid Signature", true);
        }
      } else {
        text = defaultSig;
      }
    }
    if (isHtmlMode) {
      text = HTML_Ops.clearHTMLheaderAndConditionForDisplay(text, false, false, false, false, true, false, false);
    }
    boolean addSpaces = text.trim().length() > 0;
    if (inHtmlMode != isHtmlMode) {
      if (inHtmlMode)
        text = Misc.encodePlainIntoHtml(text);
      else
        text = extractPlainFromHtml(text);
    }
    if (addSpaces && inHtmlMode)
      text = "<p></p>" + text;
    else if (addSpaces && !inHtmlMode)
      text = "\n\n" + text;

    if (trace != null) trace.exit(MsgPanelUtils.class, text);
    return text;
  }

  private static String getSigType(UserSettingsRecord userSettingsRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPanelUtils.class, "getSigType(UserSettingsRecord userSettingsRecord)");
    if (trace != null) trace.args(userSettingsRecord);

    String sigType = userSettingsRecord.getDefaultSigType();
    String type = null;
    if (sigType == null) {
      type = "";
    } else if (sigType.equalsIgnoreCase("text/html")) {
      type = "text/html";
    } else if (sigType.equalsIgnoreCase("text/plain")) {
      type = "text/plain";
    } else if (sigType.equalsIgnoreCase("text/file")) {
      String fileName = userSettingsRecord.getDefaultSig().toLowerCase();
      if (fileName.endsWith(".htm") || fileName.endsWith(".html"))
        type = "text/html";
      else
        type = "text/plain";
    } else {
      type = null;
    }

    if (trace != null) trace.exit(MsgPanelUtils.class, type);
    return type;
  }


  private static final JEditorPane paneForPlainExtraction = new JEditorPane("text/html", "<html></html>");
  public static String extractPlainFromHtml(String html) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPanelUtils.class, "extractPlainFromHtml(String html)");
    if (trace != null) trace.args(html);

    String originalHtml = html;
    String text = "";
    synchronized (paneForPlainExtraction) {
      html = ArrayUtils.replaceKeyWords(html,
        new String[][] {
          {"<P>",    "</DIV><P><DIV>"},
          {"<p>",    "</DIV><P><DIV>"},
          {"</P>",   " "},
          {"</p>",   " "},
          {"<BR>",   "</p><p>"},
          {"<br />", "</p><p>"},
          {"<br>",   "</p><p>"},
      });
      String[][] startTags = new String[][] {
        { "<style",  "<STYLE" },
        { "<script", "<SCRIPT" },
        { "<map",    "<MAP" },
        { "<img",    "<IMG" },
        { "<td",     "<TD"},
        { "<table",  "<TABLE"},
        { "<input",  "<INPUT"},
      };
      String[][] endTags = new String[][] {
        { "</style>",  "</STYLE>" },
        { "</script>", "</SCRIPT>" },
        { "</map>",    "</MAP>" },
        { ">" }, // end IMG
        { ">" }, // end TD
        { ">" }, // end TABLE
        { ">" }, // end INPUT
      };
      String[] replacementTags = new String[] {
        null,
        null,
        null,
        null,
        "<TD>",
        "<TABLE>",
        "<!--INPUT--!>",
      };
      html = ArrayUtils.removeTags(html, startTags, endTags, replacementTags);
      { // HEAD cleanup
        int indexBodyStart = html.indexOf("<body"); if (indexBodyStart < 0) indexBodyStart = html.indexOf("<BODY");
        int indexHeadEnd = html.indexOf("</head"); if (indexHeadEnd < 0) indexHeadEnd = html.indexOf("</HEAD");
        if (indexBodyStart < indexHeadEnd) {
          // skip HEAD cleanup because it seems that body is inside the HEAD tag
        } else {
          startTags = new String[][] {{ "<head>", "<HEAD>", "<head ", "<HEAD " }};
          endTags = new String[][] {{ "</head>", "</HEAD>" }};
          replacementTags = new String[] { null };
          html = ArrayUtils.removeTags(html, startTags, endTags, replacementTags);
        }
      }
      // don't convert P to BR because plain text extraction would loose \n new-line characters
      String content = HTML_Ops.clearHTMLheaderAndConditionForDisplay(html, true, true, true, true, true, true, false);
      boolean success = MsgPanelUtils.setMessageContent(content, true, paneForPlainExtraction, true);
      if (!success) {
        // don't convert P to BR because plain text extraction would loose \n new-line characters
        content = HTML_Ops.clearHTMLheaderAndConditionForDisplay(originalHtml, true, true, true, true, true, true, false);
        MsgPanelUtils.setMessageContent(content, true, paneForPlainExtraction, true);
      }
      Document d = paneForPlainExtraction.getDocument();
      try {
        text = d.getText(0, d.getLength());
        text = text.trim();
        text = ArrayUtils.replaceKeyWords(text,
          new String[][] {
            {"        \n",              "\n"},
            {"    \n",                  "\n"},
            {"  \n",                    "\n"},
            {" \n",                     "\n"},
            {"\n\n\n\n\n\n\n\n\n\n\n",  "\n\n\n"},
            {"\n\n\n\n\n\n\n",          "\n\n\n"},
            {"\n\n\n\n\n",              "\n\n\n"},
            {"\n\n\n\n",                "\n\n\n"},
        });
      } catch (Throwable t) {
      }
    }

    if (trace != null) trace.exit(MsgPanelUtils.class, text);
    return text;
  }

  private static final HashMap previewContentHM = new HashMap();
  private static Thread previewContentSetter = null;
  private static final Object previewContentSetterMonitor = new Object(); // synchronizes lazy initialization
  private static Component lastMsgViewer = null;
  private static String lastText = null;
  public static void setPreviewContent_Threaded(String content, boolean isHTMLview, boolean convertHTMLtoPLAIN, boolean skipHeaderClearing, boolean skipRemoteLoadingCleaning, JComponent messageViewer) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPanelUtils.class, "setPreviewContent_Threaded(String content, boolean isHTMLview, boolean convertHTMLtoPLAIN, boolean skipHeaderClearing, boolean skipRemoteLoadingCleaning, JComponent messageViewer)");
    if (trace != null) trace.args("content blanked");
    if (trace != null) trace.args(isHTMLview);
    if (trace != null) trace.args(convertHTMLtoPLAIN);
    if (trace != null) trace.args(skipHeaderClearing);
    if (trace != null) trace.args(skipRemoteLoadingCleaning);

    Object[] data = new Object[] { content, Boolean.valueOf(isHTMLview), Boolean.valueOf(convertHTMLtoPLAIN), Boolean.valueOf(skipHeaderClearing), Boolean.valueOf(skipRemoteLoadingCleaning), messageViewer };
    synchronized (previewContentHM) {
      previewContentHM.put(messageViewer, data);
      previewContentHM.notifyAll();
    }
    synchronized (previewContentSetterMonitor) {
      if (previewContentSetter == null) {
        previewContentSetter = new ThreadTraced("Preview Content Setter") {
          public void runTraced() {
            while (!isInterrupted()) {
              try {
                // pick the data to work with
                Object[] data = null;
                synchronized (previewContentHM) {
                  // in case there is no data, wait for it
                  if (previewContentHM.isEmpty()) {
                    try {
                      // wake-up periodically so it has a chance to quit if applet is destroyed without JVM exit
                      previewContentHM.wait(1000);
                    } catch (InterruptedException x) {
                      break;
                    }
                  } else {
                    // pick first data for servicing...
                    Iterator iter = previewContentHM.keySet().iterator();
                    if (iter.hasNext()) {
                      Object key = iter.next();
                      data = (Object[]) previewContentHM.remove(key);
                    }
                  }
                }
                // work with data
                if (data != null) {
                  final String[] content = new String[] { (String) data[0] };
                  final boolean isHTMLview = ((Boolean) data[1]).booleanValue();
                  final boolean convertHTMLtoPLAIN = ((Boolean) data[2]).booleanValue();
                  final boolean skipHeaderClearing = ((Boolean) data[3]).booleanValue();
                  final boolean skipRemoteLoadingCleaning = ((Boolean) data[4]).booleanValue();
                  final JComponent messageViewer = (JComponent) data[5];
                  // if PLAIN text mode and HTML message, condition the text to eliminate the tags
                  if (convertHTMLtoPLAIN) {
                    content[0] = MsgPanelUtils.extractPlainFromHtml(content[0]);
                  }
                  // Set GUI message content on AWT thread
                  try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                      public void run() {
                        try {
                          String text = content[0];
                          if (isHTMLview) {
                            boolean isRemoveStyles = false;
                            boolean isRemoveHead = false;
                            boolean isRemoveRemoteLoading = !skipRemoteLoadingCleaning;

                            text = HTML_Ops.clearHTMLheaderAndConditionForDisplay(text, isRemoveStyles, isRemoveHead, true, true, true, isRemoveRemoteLoading, false);
                          }

                          // Eliminate flickering and improve performance by skipping duplicate requests
                          // that maybe caused due to message updates where body remains the same.
                          if (messageViewer != lastMsgViewer || !text.equals(lastText)) {
                            MsgPanelUtils.setMessageContent(text, isHTMLview, messageViewer, false);
                            lastMsgViewer = messageViewer;
                            lastText = text;
                          }
                        } catch (Throwable t) {
                          t.printStackTrace();
                        }
                      }
                    });
                  } catch (InterruptedException x) {
                    x.printStackTrace();
                  }
                }
              } catch (Throwable t) {
                // just incase so the while loop goes forever
                t.printStackTrace();
              }
            } // end while
            // when thread exits, cleanup static data so it can be reinitialized
            synchronized (previewContentSetterMonitor) {
              previewContentSetter = null;
            }
            synchronized (previewContentHM) {
              previewContentHM.clear();
            }
          } // end run
        };
        previewContentSetter.setDaemon(true);
        previewContentSetter.start();
      }
    } // end synchronized

    if (trace != null) trace.exit(MsgPanelUtils.class);
  }

  public static Hasher.Set getMatchingPasswordHasher(MsgDataRecord msgDataRecord, String pass) {
    Hasher.Set matchingSet = null;
    pass = pass.trim();
    Hasher.Set set = new Hasher.Set(pass.toCharArray());
    if (set.passwordHash.equals(msgDataRecord.bodyPassHash)) {
      matchingSet = set;
    } else {
      // also try the new trimmed versions for Question & Answer
      pass = getTrimmedPassword(pass);
      set = new Hasher.Set(pass.toCharArray());
      if (set.passwordHash.equals(msgDataRecord.bodyPassHash)) {
        matchingSet = set;
      }
    }
    return matchingSet;
  }

  public static String getTrimmedPassword(String pass) {
    String passStripped = null;
    if (pass != null) {
      StringBuffer passStrippedBuf = new StringBuffer();
      for (int i=0; i<pass.length(); i++) {
        char lCh = pass.charAt(i);
        lCh = Character.toLowerCase(lCh);
        if (Character.isLetterOrDigit(lCh))
          passStrippedBuf.append(lCh);
      }
      passStripped = passStrippedBuf.toString();
    }
    return passStripped;
  }

  public static void unlockPassProtectedMsg(MsgDataRecord msgDataRecord, Hasher.Set matchingSet) {
    if (matchingSet != null) {
      BASymCipherBulk encText = msgDataRecord.getEncText();
      if (encText != null && encText.size() > 0 && msgDataRecord.getTextBody() == null) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        cache.addMsgBodyKey(matchingSet);
        CacheMsgUtils.unlockPassProtectedMsg(msgDataRecord, matchingSet);
      }
    }
  }

}
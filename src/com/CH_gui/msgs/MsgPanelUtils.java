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

package com.CH_gui.msgs;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.util.*;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.actions.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.records.*;
import com.CH_cl.service.records.filters.*;

import com.CH_co.cryptx.BASymCipherBulk;
import com.CH_co.gui.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.msg.dataSets.usr.*;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.frame.*;
import com.CH_gui.list.*;

import com.CH_guiLib.util.*;

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
 * <b>$Revision: 1.37 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class MsgPanelUtils extends Object {

  /*
  public static String HTML_START1 = "<html>";
  public static String HTML_START2 = "<font face='Arial' size='-1'>";
  public static String HTML_END1 = "</html>";
  public static String HTML_END2 = "</font>";
   */

  //public static String HTML_FONT_START = "<font face='Arial' size='-1'>";
  public static final String HTML_FONT_START = "<font face='Arial' size='-1'>";
  public static final String HTML_FONT_END = "</font>";

  public static final String HTML_START = "<html>";
  public static final String HTML_END = "</html>";

  public static final String HTML_BODY_START = "<body>";
  public static final String HTML_BODY_END = "</body>";

  /**
   * @return an acknowledged contact record or user record.
   */
  public static Record convertUserIdToFamiliarUser(Long userId, boolean recipientOk, boolean senderOk) {
    return convertUserIdToFamiliarUser(userId, recipientOk, senderOk, true);
  }
  public static Record convertUserIdToFamiliarUser(Long userId, boolean recipientOk, boolean senderOk, boolean includeWebUsers) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPanelUtils.class, "convertUserIdToFamiliarUser(Long userId, boolean recipientOk, boolean senderOk, boolean includeWebUsers)");
    if (trace != null) trace.args(userId);
    if (trace != null) trace.args(recipientOk);
    if (trace != null) trace.args(senderOk);
    if (trace != null) trace.args(includeWebUsers);

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    Record familiarUser = null;
    if (recipientOk) {
      ContactRecord cRec = cache.getContactRecordOwnerWith(cache.getMyUserId(), userId);
      if (cRec != null && cRec.isOfActiveType())
        familiarUser = cRec;
    }
    if (familiarUser == null && senderOk) {
      ContactRecord cRec = cache.getContactRecordOwnerWith(userId, cache.getMyUserId());
      if (cRec != null && cRec.isOfActiveType())
        familiarUser = cRec;
    }
    ContactRecord cRec = (ContactRecord) familiarUser;
    if (cRec == null ||
          // or not acknowledged, only acknowledged contacts have meaningful names...
          (!cRec.isOfActiveType() &&
           cRec.status != null && cRec.status.shortValue() != ContactRecord.STATUS_DECLINED_ACKNOWLEDGED)
        )
    {
      UserRecord uRec = cache.getUserRecord(userId);
      if (uRec != null) {
        if (includeWebUsers || uRec.status == null || !uRec.isWebAccount())
          familiarUser = uRec;
      }
    }

    if (trace != null) trace.exit(MsgPanelUtils.class, familiarUser);
    return familiarUser;
  }


  /**
   * Gather all recipients into a String
   */
  public static String gatherAllMsgRecipients(Record[][] recipients) {
    StringBuffer sb = new StringBuffer();
    for (int i=0; recipients!= null && i<recipients.length; i++) {
      for (int k=0; recipients[i]!=null && k<recipients[i].length; k++) {
        if (i == 1) {
          sb.append(MsgDataRecord.RECIPIENT_COPY);
        } else if (i == 2) {
          sb.append(MsgDataRecord.RECIPIENT_COPY_BLIND);
        }
        Record rec = recipients[i][k];
        if (rec instanceof UserRecord) {
          sb.append(MsgDataRecord.RECIPIENT_USER);
          sb.append(' ');
          sb.append(((UserRecord) rec).userId);
        } else if (rec instanceof ContactRecord) {
          sb.append(MsgDataRecord.RECIPIENT_USER);
          sb.append(' ');
          sb.append(((ContactRecord) rec).contactWithId);
        } else if (rec instanceof FolderPair) {
          sb.append(MsgDataRecord.RECIPIENT_BOARD);
          sb.append(' ');
          sb.append(((FolderPair) rec).getId());
        } else if (rec instanceof InternetAddressRecord) {
          sb.append(MsgDataRecord.RECIPIENT_EMAIL_INTERNET);
          sb.append(' ');
          sb.append(Misc.escapeWhiteEncode(((InternetAddressRecord) rec).address));
        } else if (rec instanceof NewsAddressRecord) {
          sb.append(MsgDataRecord.RECIPIENT_EMAIL_NEWS);
          sb.append(' ');
          sb.append(Misc.escapeWhiteEncode(((NewsAddressRecord) rec).address));
        } else if (rec instanceof MsgDataRecord && ((MsgDataRecord) rec).isTypeAddress()) {
          sb.append(MsgDataRecord.RECIPIENT_EMAIL_INTERNET);
          sb.append(' ');
          sb.append(Misc.escapeWhiteEncode(((MsgDataRecord) rec).getEmailAddress()));
        } else {
          throw new IllegalArgumentException("Don't know how to handle rec from family " + Misc.getPackageName(rec.getClass()) + " and of type " + Misc.getClassNameWithoutPackage(rec.getClass()));
        }
        sb.append(' ');
      }
    }
    return sb.toString().trim();
  }


  /**
   * Gathers all recipients into a Record[]
   */
  public static Record[][] gatherAllMsgRecipients(MsgDataRecord dataRecord) {
    if (dataRecord != null)
      return gatherAllMsgRecipients(dataRecord.getRecipients());
    return null;
  }
  public static Record[][] gatherAllMsgRecipients(String recipients) {
    return gatherAllMsgRecipients(recipients, -1);
  }
  public static Record[][] gatherAllMsgRecipients(String recipients, int gatherFirst_N_only) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPanelUtils.class, "gatherAllMsgRecipients(String recipients, int gatherFirst_N_only)");
    if (trace != null) trace.args(recipients);
    if (trace != null) trace.args(gatherFirst_N_only);

    Vector recsVto = new Vector();
    Vector recsVcc = new Vector();
    Vector recsVbcc = new Vector();
    int countGathered = 0;
    if (recipients != null && recipients.length() > 0) {
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      StringTokenizer st = new StringTokenizer(recipients);
      try { // If recipient list is invalid, skip them all together
        while (st.hasMoreTokens() && (gatherFirst_N_only == -1 || countGathered <= gatherFirst_N_only)) {

          String type = st.nextToken();
          char typeChar = type.charAt(0);
          boolean isCopy = typeChar == MsgDataRecord.RECIPIENT_COPY;
          boolean isCopyBlind = typeChar == MsgDataRecord.RECIPIENT_COPY_BLIND;
          if (isCopy || isCopyBlind) {
            typeChar = type.charAt(1);
          }

          // depending if TO: or CC: collect to a different set
          Vector recsV = isCopy ? recsVcc : recsVto;
          recsV = isCopyBlind ? recsVbcc : recsV;

          String sId = st.nextToken();
          String text = com.CH_gui.lang.Lang.rb.getString("unknown");
          Icon icon = null;
          Record rec = null;
          if (typeChar == MsgDataRecord.RECIPIENT_USER || typeChar == MsgDataRecord.RECIPIENT_BOARD) {
            Long lId = new Long(sId);
            if (typeChar == MsgDataRecord.RECIPIENT_USER) {
              rec = convertUserIdToFamiliarUser(lId, true, false);
              if (rec != null) {
                recsV.addElement(rec);
              } else {
                UserRecord uRec = new UserRecord();
                uRec.userId = lId;
                uRec.handle = com.CH_gui.lang.Lang.rb.getString("User");
                recsV.addElement(uRec);
              }
              countGathered ++;
            } // end "u"
            else if (typeChar == MsgDataRecord.RECIPIENT_BOARD) {
              FolderShareRecord sRec = cache.getFolderShareRecordMy(lId, true);
              FolderRecord fRec = cache.getFolderRecord(lId);
              if (sRec != null && fRec != null) {
                recsV.addElement(new FolderPair(sRec, fRec));
              } else {
                fRec = new FolderRecord();
                fRec.folderId = lId;
                fRec.folderType = new Short(FolderRecord.FILE_FOLDER);
                fRec.numOfShares = new Short((short)1);
                recsV.addElement(fRec);
              }
              countGathered ++;
            } // end "b"
          } else if (typeChar == MsgDataRecord.RECIPIENT_EMAIL_INTERNET) {
            recsV.addElement(new EmailAddressRecord(Misc.escapeWhiteDecode(sId)));
            countGathered ++;
          } else if (typeChar == MsgDataRecord.RECIPIENT_EMAIL_NEWS) {
            recsV.addElement(new NewsAddressRecord(Misc.escapeWhiteDecode(sId)));
            countGathered ++;
          }
        } // end while
      } catch (Throwable t) {
        if (trace != null) trace.data(99, "Invalid recipients list, skipping recipients", recipients);
        if (trace != null) trace.exception(MsgPanelUtils.class, 100, t);
      }
    }

    Record[][] recs = new Record[3][];
    recs[MsgLinkRecord.RECIPIENT_TYPE_TO] = new Record[recsVto.size()];
    recs[MsgLinkRecord.RECIPIENT_TYPE_CC] = new Record[recsVcc.size()];
    recs[MsgLinkRecord.RECIPIENT_TYPE_BCC] = new Record[recsVbcc.size()];
    if (recsVto.size() > 0) {
      recsVto.toArray(recs[MsgLinkRecord.RECIPIENT_TYPE_TO]);
    }
    if (recsVcc.size() > 0) {
      recsVcc.toArray(recs[MsgLinkRecord.RECIPIENT_TYPE_CC]);
    }
    if (recsVbcc.size() > 0) {
      recsVbcc.toArray(recs[MsgLinkRecord.RECIPIENT_TYPE_BCC]);
    }

    if (trace != null) trace.exit(MsgPanelUtils.class, recs);
    return recs;
  }


  /**
   * @return expanded list of recipients
   */
  public static Record[] getExpandedListOfRecipients(Record[] recipients, boolean expandAddressBooks, boolean expandGroups) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPanelUtils.class, "getExpandedListOfRecipients(Record[] recipients, boolean expandAddressBooks, boolean expandGroups)");
    if (trace != null) trace.args(recipients);
    if (trace != null) trace.args(expandAddressBooks);
    if (trace != null) trace.args(expandGroups);
    if (expandAddressBooks) {
      FolderFilter addressBookFilter = new FolderFilter(FolderRecord.ADDRESS_FOLDER);
      Record[] addressBooks = (Record[]) addressBookFilter.filterInclude(recipients);
      recipients = addressBookFilter.filterExclude(recipients);
      // gather address contacts for the address books selected
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      MsgLinkRecord[] addressContactLinks = cache.getMsgLinkRecordsOwnersAndType(RecordUtils.getIDs(addressBooks), new Short(Record.RECORD_TYPE_FOLDER));
      Record[] addressContactDatas = cache.getMsgDataRecordsForLinks(RecordUtils.getIDs(addressContactLinks));
      // filter out messages leaving address contacts objects
      addressContactDatas = new MsgFilter(MsgDataRecord.OBJ_TYPE_ADDR).filterInclude(addressContactDatas);
      // add address contacts from selected books to the list of recipients
      recipients = RecordUtils.concatinate(recipients, addressContactDatas);
    }
    if (expandGroups) {
      FolderFilter groupFilter = new FolderFilter(FolderRecord.GROUP_FOLDER);
      Record[] groups = (Record[]) groupFilter.filterInclude(recipients);
      recipients = groupFilter.filterExclude(recipients);
      // gather group members for the group folders selected
      Record[] members = getOrFetchFamiliarUsers(groups);
      // add members from selected groups to the list of recipients
      recipients = RecordUtils.concatinate(recipients, members);
    }
    if (trace != null) trace.exit(MsgPanelUtils.class, recipients);
    return recipients;
  }


  /**
   * @return converted records into ContactRecords or User records, unwinding Groups as well
   */
  public static Record[] getOrFetchFamiliarUsers(Record[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPanelUtils.class, "getOrFetchFamiliarUsers(Record[] records)");
    if (trace != null) trace.args(records);

    Record[] users = null;
    Vector cRecsV = new Vector();
    Vector fRecsV = new Vector();

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    Long userId = cache.getMyUserId();
    if (records != null && records.length > 0) {
      for (int i=0; i<records.length; i++) {
        if (records[i] instanceof ContactRecord) {
          ContactRecord cRec = (ContactRecord) records[i];
          if (!cRecsV.contains(cRec) && cRec.ownerUserId.equals(userId) && cRec.isOfActiveType())
            cRecsV.addElement(cRec);
        } else if (records[i] instanceof FolderPair) {
          FolderPair fPair = (FolderPair) records[i];
          if (fPair.getFolderRecord().isGroupType()) {
            if (!fRecsV.contains(fPair))
              fRecsV.addElement(fPair);
          }
        }
      }
    }

    Vector usersV = new Vector(cRecsV);

    if (fRecsV.size() > 0) {
      ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
      ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.FLD_Q_GET_ACCESS_USERS, new Obj_IDList_Co(RecordUtils.getIDs(fRecsV))), 30000);
      DefaultReplyRunner.nonThreadedRun(SIL, msgAction);
      if (msgAction != null && msgAction.getActionCode() == CommandCodes.USR_A_GET_HANDLES) {
        Usr_UsrHandles_Rp usrSet = (Usr_UsrHandles_Rp) msgAction.getMsgDataSet();
        UserRecord[] usrRecs = usrSet.userRecords;
        for (int i=0; i<usrRecs.length; i++) {
          Record user = MsgPanelUtils.convertUserIdToFamiliarUser(usrRecs[i].userId, true, false);
          if (!usersV.contains(user))
            usersV.addElement(user);
        }
      }
    }

    users = (Record[]) ArrayUtils.toArray(usersV, Record.class);

    if (trace != null) trace.exit(MsgPanelUtils.class, users);
    return users;
  }


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
        Record[][] recipients = gatherAllMsgRecipients(originalMsg);
        for (int recipientType=0; ourMatchingEmlRec==null && recipientType<recipients.length; recipientType++) {
          if (recipients[recipientType] != null) {
            for (int i=0; ourMatchingEmlRec==null && i<recipients[recipientType].length; i++) {
              Record recipient = recipients[recipientType][i];
              if (recipient instanceof EmailAddressRecord) {
                String emlAddr = ((EmailAddressRecord) recipient).address;
                for (int k=0; ourMatchingEmlRec==null && k<emlRecs.length; k++) {
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
  public static void drawMsgRecipientsPanel(MsgDataRecord dataRecord, JPanel jRecipients, Dimension maxSize, boolean includeLabels, boolean includeTO, boolean includeCC, boolean includeBCC) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPanelUtils.class, "drawMsgRecipientsPanel(MsgDataRecord dataRecord, JPanel jRecipients, Dimension maxSize, boolean includeLabels, boolean includeTO, boolean includeCC, boolean includeBCC)");
    if (trace != null) trace.args(dataRecord, jRecipients, maxSize);
    if (trace != null) trace.args(includeLabels);
    if (trace != null) trace.args(includeTO);
    if (trace != null) trace.args(includeCC);
    if (trace != null) trace.args(includeBCC);

    Record[][] recipients = null;
    if (dataRecord != null)
      recipients = gatherAllMsgRecipients(dataRecord);
    drawRecordFlowPanel(recipients,
                        new Boolean[] { Boolean.valueOf(includeTO), Boolean.valueOf(includeCC), Boolean.valueOf(includeBCC) },
                        includeLabels ? new String[] { com.CH_gui.lang.Lang.rb.getString("label_To"), com.CH_gui.lang.Lang.rb.getString("label_Cc"), com.CH_gui.lang.Lang.rb.getString("label_Bcc") } : null,
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
              label.setText(HTML_START + "<b>"+setHeader+"</b>" + HTML_END);
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
                label.setIcon(ListRenderer.getRenderedIcon(obj));
                label.setText(ListRenderer.getRenderedText(obj, false, true, true));
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
  public static boolean setMessageContent(String content, boolean isHTML, JComponent jMessage) {
    return setMessageContent(content, isHTML, jMessage, false, false);
  }
  public static boolean setMessageContent(String content, boolean isHTML, JComponent jMessage, boolean skipHeaderClearing, boolean skipCaretPlacement) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPanelUtils.class, "setMessageContent(String content, boolean isHTML, JComponent jMessage, boolean skipHeaderClearing, boolean skipCaretPlacement)");
    if (trace != null) trace.args(content == null || content.length() < 255 ? content : "too long length="+content.length());
    if (trace != null) trace.args(isHTML);
    if (trace != null) trace.args(jMessage);
    if (trace != null) trace.args(skipHeaderClearing);
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
      }
      try {
        jTextMessage.setText("");
      } catch (Throwable t) {
      }
      // Remove old styles if any
      try {
        Document doc = jTextMessage.getDocument();
        if (doc instanceof DefaultStyledDocument) {
          DefaultStyledDocument sDoc = (DefaultStyledDocument) doc;
          Enumeration enm = sDoc.getStyleNames();
          Vector stylesV = new Vector();
          while (enm.hasMoreElements()) {
            stylesV.addElement(enm.nextElement());
          }
          for (int i=0; i<stylesV.size(); i++) {
            String styleName = (String) stylesV.elementAt(i);
            sDoc.removeStyle(styleName);
          }
        }
      } catch (Throwable t) {
        if (trace != null) trace.exception(MsgPanelUtils.class, -10, t);
      }
      if (isHTML && !skipHeaderClearing) {
        content = HTML_utils.clearHTMLheaderAndConditionForDisplay(content, true, true, true);
      }
//    } else if (jMessage instanceof HtmlPanel) {
//      ((HtmlPanel) jMessage).clearDocument();
    }

    // Remove old components/Views if any
    try {
      Container cnt = jMessage;
      Component[] comps = cnt.getComponents();
      if (comps != null) {
        //System.out.println("comps length = " + comps.length);
        for (int i=0; i<comps.length; i++) {
          Component comp = comps[i];
          if (comp instanceof Container) {
            Container cnt2 = (Container) comp;
            Component[] comps2 = cnt2.getComponents();
            if (comps2 != null) {
              //System.out.println("cmps2 length = " + comps2.length);
              for (int j=0; j<comps2.length; j++) {
                Component comp2 = comps2[j];
                //System.out.println("j " + comp2.getName() + ", " + comp2.getClass() + ", " + comp2.getClass().getName() + ", " + comp2.getClass().getSimpleName());
                cnt2.remove(comps2[j]);
              }
            }
          }
          //System.out.println("i " + comp.getName() + ", " + comp.getClass() + ", " + comp.getClass().getName() + ", " + comp.getClass().getSimpleName());
          cnt.remove(comp);
        }
      }
    } catch (Throwable t) {
      if (trace != null) trace.exception(MsgPanelUtils.class, -20, t);
    }

    String text = content;

    // first try original data
    {
      text = content;
      try {
        if (trace != null) trace.data(1, MsgPanelUtils.class, "try 0");
        setText(jMessage, text);
        contentSet(text);
        if (trace != null) trace.data(2, "setText length", text.length());
        displayedOk = true;
      } catch (Throwable t) {
        if (trace != null) trace.exception(MsgPanelUtils.class, 3, t);
      }
    }
    if (!displayedOk) {
      try {
        if (trace != null) trace.data(10, MsgPanelUtils.class, "try 1");
        if (isHTML)
          text = HTML_START + HTML_BODY_START + content + HTML_BODY_END + HTML_END;
        setText(jMessage, text);
        contentSet(text);
        if (trace != null) trace.data(11, "setText length", text.length());
        displayedOk = true;
      } catch (Throwable t) {
        if (trace != null) trace.exception(MsgPanelUtils.class, 12, t);
      }
    }
    if (!displayedOk) {
      try {
        if (trace != null) trace.data(20, MsgPanelUtils.class, "try 2");
        if (isHTML)
          text = HTML_START + content + HTML_END;
        setText(jMessage, text);
        contentSet(text);
        if (trace != null) trace.data(21, "setText length", text.length());
        displayedOk = true;
      } catch (Throwable t) {
        if (trace != null) trace.exception(MsgPanelUtils.class, 22, t);
      }
    }
    if (!displayedOk && isHTML) {
      text = HTML_START + "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>" + content + "</td></tr></table>" + HTML_END;
      try {
        if (trace != null) trace.data(30, MsgPanelUtils.class, "try 3");
        setText(jMessage, text);
        contentSet(text);
        if (trace != null) trace.data(31, "setText length", text.length());
        displayedOk = true;
      } catch (Throwable t) {
        if (trace != null) trace.exception(MsgPanelUtils.class, 35, t);
      }
    }
    if (!displayedOk && isHTML) {
      text = HTML_START + "<body><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>" + content + "</td></tr></table></body>" + HTML_END;
      try {
        if (trace != null) trace.data(40, MsgPanelUtils.class, "try 4");
        setText(jMessage, text);
        contentSet(text);
        if (trace != null) trace.data(41, "setText length", text.length());
        displayedOk = true;
      } catch (Throwable t) {
        if (trace != null) trace.exception(MsgPanelUtils.class, 45, t);
      }
    }
    if (!displayedOk) {
      System.out.println("failed content="+content);
      if (content == null) {
        String t = com.CH_gui.lang.Lang.rb.getString("msg_Message_Body_could_not_be_fetched.");
        setText(jMessage, t);
        //displayedOk = true;
      } else if (isHTML) {
        String t = com.CH_gui.lang.Lang.rb.getString("msg_This_message_contains_an_invalid_HTML_code_and_cannot_be_displayed...");
        setText(jMessage, t);
        //displayedOk = true;
      } else {
        String t = com.CH_gui.lang.Lang.rb.getString("msg_This_message_contains_invalid_content_and_cannot_be_displayed...");
        setText(jMessage, t);
        //displayedOk = true;
      }
    }
    if (displayedOk && !skipCaretPlacement) {
      try {
        if (jMessage instanceof JTextComponent) {
          ((JTextComponent) jMessage).setCaretPosition(0);
        } else {
          // TO-DO: cobra
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
    if (jMessage instanceof JTextComponent) {
      ((JTextComponent) jMessage).setText(text);
//    } else if (jMessage instanceof HtmlPanel) {
//      HtmlPanel panel = (HtmlPanel) jMessage;
//      SimpleHtmlRendererContext rcontext = new SimpleHtmlRendererContext(panel, new SimpleUserAgentContext());
//      panel.setHtml(text, HTML_ClickablePane.getDefaultBase().toString(), rcontext);
    }
  }

  private static boolean ENABLE_DEBUG_MSG_PANE = false;
  private static JTextPane msgDebugTextPane = null;
  private static void contentSet(String text) {
    if (ENABLE_DEBUG_MSG_PANE) {
      if (msgDebugTextPane == null) {
        JFrame msgPrev = new JFrame("content Set text");
        msgPrev.setSize(900, 900);
        msgPrev.setVisible(true);
        Container cont = msgPrev.getContentPane();
        cont.setLayout(new BorderLayout());
        msgDebugTextPane = new JTextPane();
        cont.add(new JScrollPane(msgDebugTextPane), BorderLayout.CENTER);
      }
      msgDebugTextPane.setText(text);
      msgDebugTextPane.repaint();
    }
  }

  public static String getSigText(UserSettingsRecord userSettingsRecord, boolean inHtmlMode) {
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
      text = HTML_utils.clearHTMLheaderAndConditionForDisplay(text, true, false, false);
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
    return text;
  }

  private static String getSigType(UserSettingsRecord userSettingsRecord) {
    String sigType = userSettingsRecord.getDefaultSigType();
    if (sigType == null) {
      return "";
    } else if (sigType.equalsIgnoreCase("text/html")) {
      return "text/html";
    } else if (sigType.equalsIgnoreCase("text/plain")) {
      return "text/plain";
    } else if (sigType.equalsIgnoreCase("text/file")) {
      String fileName = userSettingsRecord.getDefaultSig().toLowerCase();
      if (fileName.endsWith(".htm") || fileName.endsWith(".html"))
        return "text/html";
      else
        return "text/plain";
    } else {
      return null;
    }
  }


  private static final JEditorPane paneForPlainExtraction = new JEditorPane("text/html", "<html></html>");
  public static String extractPlainFromHtml(String html) {
    String originalHtml = html;
    String text = "";
    synchronized (paneForPlainExtraction) {
      html = ArrayUtils.replaceKeyWords(html,
        new String[][] {
          {"<P>", "<p>"},
          {"<p>", "</DIV><P><DIV>"},
          {"</P>", " "},
          {"</p>", " "},
          {"<BR>", "<br>"},
          {"<br>", "</p><p>"},
      });
      String[][] startTags = new String[][] {
        { "<head", "<HEAD" },
        { "<style", "<STYLE" },
        { "<script", "<SCRIPT" },
        { "<map", "<MAP" },
        { "<img", "<IMG" },
        { "<td", "<TD"},
        { "<table", "<TABLE"},
        { "<input", "<INPUT"},
      };
      String[][] endTags = new String[][] {
        { "</head>", "</HEAD>" },
        { "</style>", "</STYLE>" },
        { "</script>", "</SCRIPT>" },
        { "</map>", "</MAP>" },
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
        null,
        "<TD>",
        "<TABLE>",
        "<!--INPUT--!>",
      };
      html = ArrayUtils.removeTags(html, startTags, endTags, replacementTags);
      boolean success = MsgPanelUtils.setMessageContent(html, true, paneForPlainExtraction);
      if (!success) {
        MsgPanelUtils.setMessageContent(originalHtml, true, paneForPlainExtraction);
      }
      Document d = paneForPlainExtraction.getDocument();
      try {
        text = d.getText(0, d.getLength());
        text = text.trim();
        text = ArrayUtils.replaceKeyWords(text,
          new String[][] {
            {"        \n", "\n"},
            {"    \n", "\n"},
            {"  \n", "\n"},
            {" \n", "\n"},
            {"\n\n\n\n\n\n\n\n\n\n\n", "\n\n\n"},
            {"\n\n\n\n\n\n\n", "\n\n\n"},
            {"\n\n\n\n\n", "\n\n\n"},
            {"\n\n\n\n", "\n\n\n"},
        });
      } catch (Throwable t) {
      }
    }
    return text;
  }


//  public static void setPreviewContent(String content, boolean isHTMLview, boolean convertHTMLtoPLAIN, boolean skipHeaderClearing, JTextComponent messageViewer) {
//    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPanelUtils.class, "setPreviewContent(String content, boolean isHTMLview, boolean convertHTMLtoPLAIN, boolean skipHeaderClearing, JTextComponent messageViewer)");
//    if (trace != null) trace.args("content blanked");
//    if (trace != null) trace.args(isHTMLview);
//    if (trace != null) trace.args(convertHTMLtoPLAIN);
//    if (trace != null) trace.args(skipHeaderClearing);
//
//    // if PLAIN text mode and HTML message, condition the text to eliminate the tags
//    if (convertHTMLtoPLAIN) {
//      content = MsgPanelUtils.extractPlainFromHtml(content);
//    }
//    MsgPanelUtils.setMessageContent(content, isHTMLview, messageViewer, skipHeaderClearing);
//
//    if (trace != null) trace.exit(MsgPanelUtils.class);
//  }


  private static final Hashtable previewContentHT = new Hashtable();
  private static Thread previewContentSetter = null;
  private static final Object previewContentSetterMonitor = new Object(); // synchronizes lazy initialization
  public static void setPreviewContent_Threaded(String content, boolean isHTMLview, boolean convertHTMLtoPLAIN, boolean skipHeaderClearing, JComponent messageViewer) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgPanelUtils.class, "setPreviewContent_Threaded(String content, boolean isHTMLview, boolean convertHTMLtoPLAIN, boolean skipHeaderClearing, JComponent messageViewer)");
    if (trace != null) trace.args("content blanked");
    if (trace != null) trace.args(isHTMLview);
    if (trace != null) trace.args(convertHTMLtoPLAIN);
    if (trace != null) trace.args(skipHeaderClearing);

    Object[] data = new Object[] { content, Boolean.valueOf(isHTMLview), Boolean.valueOf(convertHTMLtoPLAIN), Boolean.valueOf(skipHeaderClearing), messageViewer };
    synchronized (previewContentHT) {
      previewContentHT.put(messageViewer, data);
      previewContentHT.notifyAll();
    }
    synchronized (previewContentSetterMonitor) {
      if (previewContentSetter == null) {
        previewContentSetter = new ThreadTraced("Preview Content Setter") {
          public void runTraced() {
            while (true) {
              try {
                // pick the data to work with
                Object[] data = null;
                synchronized (previewContentHT) {
                  // in case there is no data, wait for it
                  if (previewContentHT.size() == 0) {
                    try {
                      previewContentHT.wait();
                    } catch (InterruptedException x) {
                    }
                  } else {
                    // pick first data for servicing...
                    Enumeration enm = previewContentHT.keys();
                    if (enm.hasMoreElements()) {
                      Object key = enm.nextElement();
                      data = (Object[]) previewContentHT.remove(key);
                    }
                  }
                }
                // work with data
                if (data != null) {
                  final String[] content = new String[] { (String) data[0] };
                  final boolean isHTMLview = ((Boolean) data[1]).booleanValue();
                  final boolean convertHTMLtoPLAIN = ((Boolean) data[2]).booleanValue();
                  final boolean skipHeaderClearing = ((Boolean) data[3]).booleanValue();
                  final JComponent messageViewer = (JComponent) data[4];
                  // if PLAIN text mode and HTML message, condition the text to eliminate the tags
                  if (convertHTMLtoPLAIN) {
                    content[0] = MsgPanelUtils.extractPlainFromHtml(content[0]);
                  }
                  try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                      public void run() {
                        try {
                          MsgPanelUtils.setMessageContent(content[0], isHTMLview, messageViewer, skipHeaderClearing, false);
                        } catch (Throwable t) {
                        }
                      }
                    });
                  } catch (InterruptedException x) {
                  }
                }
              } catch (Throwable t) {
                // just incase so the while loop goes forever
              }
            } // end while
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
        cache.bodyKeys.addElement(matchingSet);
        CacheUtilities.unlockPassProtectedMsg(msgDataRecord, matchingSet);
      }
    }
  }

}
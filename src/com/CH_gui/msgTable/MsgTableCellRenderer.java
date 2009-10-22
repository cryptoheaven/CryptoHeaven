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

package com.CH_gui.msgTable;

import java.awt.*;
import java.sql.Timestamp;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import com.CH_cl.service.cache.*;

import com.CH_co.gui.*;
import com.CH_co.service.records.*;
import com.CH_co.util.*;

import com.CH_gui.list.ListRenderer;
import com.CH_gui.msgs.MsgPanelUtils;
import com.CH_gui.table.*;
import com.CH_gui.sortedTable.*;

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
 * <b>$Revision: 1.33 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class MsgTableCellRenderer extends RecordTableCellRenderer {

  private static Color regularMsgAltColor = new Color(236, 251, 232);
  private static Color regularMsgAltColorSelected = new Color(189, 201, 186);
  private static Color[] altBkColors = new Color[] { regularMsgAltColor, regularMsgAltColorSelected };

  private JPanel jRecipientPanelRenderer = null;
  private JPanel jTwoLinesRendererFrom = null;
  private JPanel jTwoLinesRendererSent = null;
  private JPanel jTwoLinesRendererSubject = null;
  private JPanel jTwoLinesRendererTo = null;
  private JPanel jTwoLinesRenderer = null;
  private JPanel jIconSetRenderer = null;
  private JLabel jRendererSmallPlainIconized = null;
  private JLabel jRendererSmallPlainIconized2 = null;
  private JLabel jRendererSmallBoldIconized = null;
  private JLabel jRendererSmallPlainText = null;
  private JLabel jRendererPlainIconized = null;
  private JLabel jRendererBoldIconized = null;
  private JLabel jNoIcon1 = new JLabel();
  private JLabel jNoIcon2 = new JLabel();
  private JLabel jNoIcon3 = new JLabel();
  private JLabel jNoIcon4 = new JLabel();
  private JLabel jNoIcon5 = new JLabel();
  private JLabel[] jHeaderRenderers = null;
  private JLabel[] jLabelRenderers = null;

  public MsgTableCellRenderer() {
    jRecipientPanelRenderer = new JPanel();
    jTwoLinesRendererFrom = new JPanel();
    jTwoLinesRendererSent = new JPanel();
    jTwoLinesRendererSubject = new JPanel();
    jTwoLinesRendererTo = new JPanel();
    jTwoLinesRenderer = new JPanel();
    jIconSetRenderer = new JPanel();
    jHeaderRenderers = new JMyLabel[3];
    for (int i=0; i<jHeaderRenderers.length; i++)
      jHeaderRenderers[i] = new JMyLabel();
    jLabelRenderers = new JMyLabel[10];
    for (int i=0; i<jLabelRenderers.length; i++)
      jLabelRenderers[i] = new JMyLabel();
    jRecipientPanelRenderer.setOpaque(true);
    jTwoLinesRendererFrom.setOpaque(true);
    jTwoLinesRendererSent.setOpaque(true);
    jTwoLinesRendererSubject.setOpaque(true);
    jTwoLinesRendererTo.setOpaque(true);
    jTwoLinesRenderer.setOpaque(true);
    jIconSetRenderer.setOpaque(true);
    jIconSetRenderer.setLayout(new GridLayout());
    jRendererSmallPlainIconized = makeLabel(RecordTableCellRenderer.BORDER_ICONIZED, Images.get(ImageNums.TRANSPARENT16), -1, Font.PLAIN, Color.gray);
    jRendererSmallPlainIconized2 = makeLabel(RecordTableCellRenderer.BORDER_ICONIZED, Images.get(ImageNums.TRANSPARENT16), -1, Font.PLAIN, Color.gray);
    jRendererSmallBoldIconized = makeLabel(RecordTableCellRenderer.BORDER_ICONIZED, Images.get(ImageNums.TRANSPARENT16), -1, Font.BOLD, Color.gray);
    jRendererSmallPlainText = makeLabel(RecordTableCellRenderer.BORDER_TEXT, null, -1, Font.PLAIN, Color.gray);
    jRendererPlainIconized = makeLabel(RecordTableCellRenderer.BORDER_ICONIZED, Images.get(ImageNums.TRANSPARENT16), 0, Font.PLAIN, Color.black);
    jRendererBoldIconized = makeLabel(RecordTableCellRenderer.BORDER_ICONIZED, Images.get(ImageNums.TRANSPARENT16), 0, Font.BOLD, Color.black);
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    int rawColumn = getRawColumn(table, column);
    return getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column, rawColumn);
  }
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column, int rawColumn) {

    Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    // if no "Subject" then layout on two lines
    boolean isDoubleLineView = !isColumnVisible(table, 5);

    // importance
    // attachments
    if (rawColumn == 0 || rawColumn == 1 || rawColumn == 2) {
      setBorder(RecordTableCellRenderer.BORDER_ICON);
      setText("");
      // importance
      if (rawColumn == 0) {
        if (value instanceof Short) {
          Icon icon = null;
          short imp = ((Short) value).shortValue();
          Object[] o = MsgDataRecord.getPriorityTextAndIcon(imp);
          String toolTip = o[0].toString();
          icon = (Icon) o[1];
          if (!isDoubleLineView) {
            // if Expiration or Password column is not visible
            boolean col_19 = isColumnVisible(table, 19); // expiry column
            boolean col_20 = isColumnVisible(table, 20); // password protection column
            if (!col_19 || !col_20) {
              FetchedDataCache cache = FetchedDataCache.getSingleInstance();
              MsgLinkRecord mLink = (MsgLinkRecord) getRecord(table, row);
              MsgDataRecord mData = cache.getMsgDataRecord(mLink.msgId);
              if (!col_20) {
                if (mData.bodyPassHash != null) {
                  icon = Images.get(ImageNums.KEY16);
                  toolTip = "Password Protected";
                }
              }
              if (!col_19) {
                Object[] exp = mData.getExpirationIconAndText(cache.getMyUserId());
                if (exp[0] != null) {
                  icon = (Icon) exp[0];
                  toolTip = "Expiration: " + exp[1];
                }
              }
            }
          }
          setIcon(icon);
          setToolTipText(toolTip);
        }
      } // end if rawColumn == 0

      // attachments
      else if (rawColumn == 1) {
        if (value instanceof Short) {
          short attachments = value != null ? ((Short) value).shortValue() : -1;
          Icon icon = null;
          String toolTip = null;
          //setHorizontalAlignment(RIGHT);
          if (attachments > 0) {
            //text = "" + value;
            icon = Images.get(ImageNums.ATTACH16);
            if (attachments == 1)
              toolTip = com.CH_gui.lang.Lang.rb.getString("rowTip_Message_Attachment_Present");
            else
              toolTip = "" + attachments + " " + com.CH_gui.lang.Lang.rb.getString("rowTip_Message_Attachments_Present");
          } else {
            toolTip = com.CH_gui.lang.Lang.rb.getString("rowTip_No_Attachments_Present");
            // since no attachments present, maybe we can put other useful info here
            if (!isDoubleLineView) {
              // if Importance column is not visible
              boolean col_0 = isColumnVisible(table, 0);
              if (!col_0) {
                // if Expiration or Password column is not visible
                boolean col_19 = isColumnVisible(table, 19); // expiry column
                boolean col_20 = isColumnVisible(table, 20); // password protection column
                if (!col_19 || !col_20) {
                  FetchedDataCache cache = FetchedDataCache.getSingleInstance();
                  MsgLinkRecord mLink = (MsgLinkRecord) getRecord(table, row);
                  MsgDataRecord mData = cache.getMsgDataRecord(mLink.msgId);
                  if (!col_20) {
                    if (mData.bodyPassHash != null) {
                      icon = Images.get(ImageNums.KEY16);
                      toolTip = "Password Protected";
                    }
                  }
                  if (!col_19) {
                    Object[] exp = mData.getExpirationIconAndText(cache.getMyUserId());
                    if (exp[0] != null) {
                      icon = (Icon) exp[0];
                      toolTip = "Expiration: " + exp[1];
                    }
                  }
                }
              }
            }
          }
          setIcon(icon);
          setToolTipText(toolTip);
        }
      }

      // Flag mark
      else if (rawColumn == 2) {
        if (value != null) {
          if (table instanceof JSortedTable) {
            JSortedTable sTable = (JSortedTable) table;
            TableModel rawModel = sTable.getRawModel();
            if (rawModel instanceof MsgTableModel) {
              MsgTableModel tableModel = (MsgTableModel) rawModel;
              Record rec = tableModel.getRowObject(sTable.convertMyRowIndexToModel(row));
              if (rec instanceof MsgLinkRecord) {
                MsgLinkRecord mLink = (MsgLinkRecord) rec;

                StatRecord statRecord = FetchedDataCache.getSingleInstance().getStatRecord(mLink.msgLinkId, FetchedDataCache.STAT_TYPE_MESSAGE);
                if (statRecord != null) {
                  setIcon(StatRecord.getIconForFlag((Short) value));
                  setToolTipText(statRecord.getInfo());
                }
              }
            }
          }
        }
      } // end Flag mark
    }

    // From
    else if (rawColumn == 3) {
      // From (internal)
      if (value instanceof Long) {
        setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
        setHorizontalAlignment(LEFT);
        // The From field is the contact name or user's short info, whichever is available
        Long userId = (Long) value;
        Record rec = MsgPanelUtils.convertUserIdToFamiliarUser(userId, false, true);
        if (rec != null) {
          setIcon(ListRenderer.getRenderedIcon(rec));
          setText(ListRenderer.getRenderedText(rec));
        }
        else {
          setText(java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("User_(USER-ID)"), new Object[] {userId}));
          setIcon(Images.get(ImageNums.PERSON_SMALL));
        }
      }
      // From (email)
      else if (value instanceof String) {
        setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
        setHorizontalAlignment(LEFT);
        Record sender = CacheUtilities.convertToFamiliarEmailRecord((String) value);
        setIcon(ListRenderer.getRenderedIcon(sender));
        setText(ListRenderer.getRenderedText(sender));
      }
      setDefaultBackground(this, row, isSelected);

      // if no SUBJECT columns, include it here
      boolean col_5 = isColumnVisible(table, 5);
      TableModel rawModel = null;
      if (!col_5 && table instanceof JSortedTable && (rawModel = ((JSortedTable) table).getRawModel()) instanceof MsgTableModel) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        MsgLinkRecord mLink = (MsgLinkRecord) getRecord(table, row);
        MsgDataRecord mData = cache.getMsgDataRecord(mLink.msgId);
        JLabel thisCloned = null;
        StatRecord statRecord = null;
        if (mLink != null && (statRecord = FetchedDataCache.getSingleInstance().getStatRecord(mLink.msgLinkId, FetchedDataCache.STAT_TYPE_MESSAGE)) != null && statRecord.isFlagNew())
          thisCloned = jRendererBoldIconized;
        else
          thisCloned = jRendererPlainIconized;
        thisCloned.setText(getText());
        thisCloned.setIcon(getIcon());
        thisCloned.setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
        thisCloned.setForeground(getForeground());
        thisCloned.setBackground(getBackground());
        // for Address objects, change the first line to "Name" because we know this column is hidden
        if (mData != null && mData.isTypeAddress())
          thisCloned.setText(ListRenderer.getRenderedText(mData));
        Object subjectValue = MsgTableModel.getSubjectColumnValue((MsgTableModel) rawModel, mLink, mData, false, cache);
        JComponent subjectComp = (JComponent) getTableCellRendererComponent(table, subjectValue, isSelected, hasFocus, row, -1, 5);
        subjectComp.setOpaque(true);
        if (subjectComp instanceof JLabel) {
          JLabel secondLine = (JLabel) subjectComp;
          secondLine.setIcon(Images.get(ImageNums.TRANSPARENT16));
          // for Address objects, change the second line to "Email Address" if such column is hidden
          if (mData != null && mData.isTypeAddress() && !isColumnVisible(table, 16) && !mData.fileAs.equalsIgnoreCase(mData.email))
            secondLine.setText(mData.email);
        }
        // get icon from source object
        Icon icon = null;
        if (mData != null && mData.isTypeAddress()) icon = mData.getIcon();
        if (icon == null) icon = mLink.getIcon();
        thisCloned.setIcon(icon);
        jTwoLinesRendererFrom.removeAll();
        jTwoLinesRendererFrom.setLayout(new GridBagLayout());
        jTwoLinesRendererFrom.add(thisCloned, new GridBagConstraints(0, 0, 1, 1, 10, 0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(0,0,0,0), 0, 0));
        jTwoLinesRendererFrom.add(subjectComp, new GridBagConstraints(0, 1, 1, 1, 10, 10,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(0,0,0,0), 0, 0));
        setDefaultBackground(jTwoLinesRendererFrom, row, isSelected);
        // Fix up the height of the row in case the message subject needs more space.
        int desiredHeight = Math.max(jTwoLinesRendererFrom.getPreferredSize().height, table.getRowHeight());
        if (table.getRowHeight(row) < desiredHeight) {
          table.setRowHeight(row, desiredHeight);
        }
        renderer = jTwoLinesRendererFrom;
      }
    }

    // column 'To'
    else if (rawColumn == 4) {
      if (value != null) {

        StringBuffer toolTipBuf = new StringBuffer();
        Record[][] recipients = MsgPanelUtils.gatherAllMsgRecipients((String) value);

        JPanel jFlowPanel = jRecipientPanelRenderer;
        jFlowPanel.removeAll();
        jFlowPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
        jFlowPanel.setBorder(null);

        if (isSelected) {
          jFlowPanel.setBackground(table.getSelectionBackground());
          jFlowPanel.setForeground(table.getSelectionForeground());
        } else {
          jFlowPanel.setBackground(table.getBackground());
          jFlowPanel.setForeground(table.getForeground());
        }

        if (recipients != null && recipients.length > 0) {
          for (int recipientType=MsgLinkRecord.RECIPIENT_TYPE_TO; recipientType<=MsgLinkRecord.RECIPIENT_TYPE_BCC; recipientType++) {
            if (recipients[recipientType] != null && recipients[recipientType].length > 0) {
              MsgPanelUtils.drawRecordFlowPanel(recipients, new Boolean[] { Boolean.TRUE, Boolean.TRUE, Boolean.TRUE }, new String[] { null, com.CH_gui.lang.Lang.rb.getString("label_Cc"), com.CH_gui.lang.Lang.rb.getString("label_Bcc") }, jFlowPanel, jHeaderRenderers, jLabelRenderers);
            }
          }
        }
        setDefaultToolTip(toolTipBuf.toString(), jFlowPanel, table, row, column);
        setDefaultBackground(jFlowPanel, row, isSelected);
        // if no FROM and SUBJECT columns, include it here
        boolean col_3 = isColumnVisible(table, 3);
        boolean col_5 = isColumnVisible(table, 5);
        TableModel rawModel = null;
        if (!col_3 && !col_5 && table instanceof JSortedTable && (rawModel = ((JSortedTable) table).getRawModel()) instanceof MsgTableModel) {
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          MsgLinkRecord mLink = (MsgLinkRecord) getRecord(table, row);
          MsgDataRecord mData = cache.getMsgDataRecord(mLink.msgId);
          JLabel jAddrRenderer = null;
          StatRecord statRecord = null;
          if (mLink != null && (statRecord = FetchedDataCache.getSingleInstance().getStatRecord(mLink.msgLinkId, FetchedDataCache.STAT_TYPE_MESSAGE)) != null && statRecord.isFlagNew())
            jAddrRenderer = jRendererBoldIconized;
          else
            jAddrRenderer = jRendererPlainIconized;
          // for Address objects, change the first line to "Name" because we know this column is hidden
          if (mData != null && mData.isTypeAddress()) {
            jAddrRenderer.setText(ListRenderer.getRenderedText(mData));
            // get icon from source object
            Icon icon = null;
            if (mData != null && mData.isTypeAddress())
              icon = mData.getIcon();
            if (icon == null)
              icon = mLink.getIcon();
            jAddrRenderer.setIcon(icon);
            setDefaultBackground(jAddrRenderer, row, isSelected);
            jFlowPanel.removeAll();
            jFlowPanel.add(jAddrRenderer);
          }
          Object subjectValue = MsgTableModel.getSubjectColumnValue((MsgTableModel) rawModel, mLink, mData, false, cache);
          JComponent subjectComp = (JComponent) getTableCellRendererComponent(table, subjectValue, isSelected, hasFocus, row, -1, 5);
          subjectComp.setOpaque(true);
          if (subjectComp instanceof JLabel) {
            JLabel secondLine = (JLabel) subjectComp;
            secondLine.setIcon(Images.get(ImageNums.TRANSPARENT16));
            // for Address objects, change the second line to "Email Address" if such column is hidden, "From" column is already hidden so it won't display twice
            if (mData != null && mData.isTypeAddress() && !isColumnVisible(table, 16) && !mData.fileAs.equalsIgnoreCase(mData.email))
              secondLine.setText(mData.email);
          }
          jTwoLinesRendererTo.removeAll();
          jTwoLinesRendererTo.setLayout(new GridLayout(2, 1));
          jTwoLinesRendererTo.add(jFlowPanel);
          jTwoLinesRendererTo.add(subjectComp);
          setDefaultBackground(jTwoLinesRendererTo, row, isSelected);
          // Fix up the height of the row in case the message subject needs more space.
          int desiredHeight = Math.min(36, Math.max(jTwoLinesRendererTo.getPreferredSize().height, table.getRowHeight()));
          if (table.getRowHeight(row) < desiredHeight) {
            table.setRowHeight(row, desiredHeight);
          }
          renderer = jTwoLinesRendererTo;
        } else {
          renderer = jFlowPanel;
        }
      }
    }

    // Subject
    else if (rawColumn == 5 && value instanceof String) {
      String subject = (String) value;
      int indentLevel = 0;
      // If message status is UNREAD then display closed main icon, otherwise open mail icon.
      JSortedTable sTable = (JSortedTable) table;
      TableModel tableModel = sTable.getRawModel();
      Long msgId = null;
      // Since multiple views may display the same message links, we must choose how to view them in the renderer.
      if (tableModel instanceof MsgTableModel) {
        MsgTableModel mtm = (MsgTableModel) tableModel;
        MsgLinkRecord mLink = (MsgLinkRecord) mtm.getRowObject(sTable.convertMyRowIndexToModel(row));
        msgId = mLink.msgId;
        MsgDataRecord mData = FetchedDataCache.getSingleInstance().getMsgDataRecord(mLink.msgId);
        boolean isFlagRed = false;
        StatRecord statRecord = null;
        isFlagRed = mLink != null && (statRecord = FetchedDataCache.getSingleInstance().getStatRecord(mLink.msgLinkId, FetchedDataCache.STAT_TYPE_MESSAGE)) != null && statRecord.isFlagNew();
        // set icon if in its own column (not as part of other)
        if (column > -1) {
          Icon icon = null;
          if (mData != null && mData.isTypeAddress()) {
            icon = mData.getIcon();
          }
          if (icon == null) {
            icon = mLink.getIcon();
          }
          setIcon(icon);
          // check if need to use BOLD
          if (isFlagRed) {
            renderer = jRendererBoldIconized;
            jRendererBoldIconized.setText(getText());
            jRendererBoldIconized.setIcon(getIcon());
            setDefaultBackground(renderer, row, isSelected);
          }
        } else {
          // if part of other column, use the smaller renderer
          if (isFlagRed) {
            renderer = jRendererSmallBoldIconized;
            jRendererSmallBoldIconized.setText(getText());
          } else {
            renderer = jRendererSmallPlainIconized;
            jRendererSmallPlainIconized.setText(getText());
          }
          setDefaultBackground(renderer, row, isSelected);
        }
        if  (((MsgTableSorter) sTable.getModel()).isThreaded())
          indentLevel = mLink.getSortThreadLayer();
        // set Subject
        if (subject == null || subject.length() == 0)
          subject = ListRenderer.getRenderedText(mData);
        ((JLabel) renderer).setText(subject);

        // in Address type tables, if no "Email Address" column, show it here under "Name"
        if (mtm.getMode() == MsgTableModel.MODE_ADDRESS || mtm.getMode() == MsgTableModel.MODE_WHITELIST) {
          if (mData != null && mData.isTypeAddress() && !isColumnVisible(table, 16) && !mData.fileAs.equalsIgnoreCase(mData.email)) {
            JLabel thisCloned = (JLabel) renderer;
            thisCloned.setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
            thisCloned.setForeground(getForeground());
            thisCloned.setBackground(getBackground());
            jTwoLinesRendererSubject.removeAll();
            jTwoLinesRendererSubject.setLayout(new GridLayout(2, 1));
            jTwoLinesRendererSubject.add(thisCloned);
            jRendererSmallPlainIconized2.setText(mData.email);
            jTwoLinesRendererSubject.add(jRendererSmallPlainIconized2);
            setDefaultBackground(jRendererSmallPlainIconized2, row, isSelected);
            setDefaultBackground(jTwoLinesRendererSubject, row, isSelected);
            renderer = jTwoLinesRendererSubject;
          }
        }
      }

      if (indentLevel > 0) {
        boolean isIconized = true;
        // if not in its own column strip the transparent icon and add another Indent
        if (column == -1 && renderer instanceof JLabel) {
          ((JLabel) renderer).setIcon(null);
          isIconized = false;
          indentLevel ++;
        }
        renderer = makeIndentedAreaRenderer(indentLevel, renderer, true, isIconized);
        // since renderer can be a panel here, use this special call to set the tool tip
        if (renderer instanceof JComponent)
          setDefaultToolTip(subject, (JComponent) renderer, table, row, column);
      }

      // Fix up the height of the row in case the message subject needs more space.
      int desiredHeight = Math.max(renderer.getPreferredSize().height, table.getRowHeight());
      if (table.getRowHeight(row) < desiredHeight) {
        table.setRowHeight(row, desiredHeight);
      }
    }

    // Sent Timestamp
    else if (rawColumn == 6) {
      if (isDoubleLineView) {
        JLabel sent = jRendererSmallPlainText;
        jRendererSmallPlainText.setText(getText());
        jRendererSmallPlainText.setIcon(null);
        setDefaultBackground(sent, row, isSelected);

        jIconSetRenderer.removeAll();
        setDefaultBackground(jIconSetRenderer, row, isSelected);
        int iconIndex = 0;

        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        MsgLinkRecord mLink = (MsgLinkRecord) getRecord(table, row);
        MsgDataRecord mData = null;
        if (mLink != null)
          mData = cache.getMsgDataRecord(mLink.msgId);

        // Flag
        if (!isColumnVisible(table, 2)) {
          StatRecord statRecord = null;
          if (mLink != null && (statRecord = cache.getStatRecord(mLink.msgLinkId, FetchedDataCache.STAT_TYPE_MESSAGE)) != null) {
            JLabel icon = new JLabel(statRecord.getIcon());
            jIconSetRenderer.add(icon);
          } else {
            jIconSetRenderer.add(jNoIcon1);
          }
        }
        // Priority
        if (!isColumnVisible(table, 0)) {
          if (mData != null) {
            Object[] o = mData.getPriorityTextAndIcon();
            JLabel icon = new JLabel((Icon) o[1]);
            jIconSetRenderer.add(icon);
          } else {
            jIconSetRenderer.add(jNoIcon2);
          }
        }
        // Secure Lock
        if (!isColumnVisible(table, 15)) {
          // if the long version includes "Secure Lock" add it here
          // do not add blank filler icon if this table doesn't show the lock icon at all
          if (mData != null) {
            if (table instanceof JSortedTable) {
              JSortedTable sTable = (JSortedTable) table;
              TableModel rawModel = sTable.getRawModel();
              if (rawModel instanceof MsgTableModel) {
                MsgTableModel tableModel = (MsgTableModel) rawModel;
                if (ArrayUtils.find(tableModel.getColumnHeaderData().data[ColumnHeaderData.I_VIEWABLE_SEQUENCE_DEFAULT_LONG], new Integer(15)) > -1) {
                  Object[] o = mData.getSecurityTextAndIcon();
                  JLabel icon = new JLabel((Icon) o[1]);
                  jIconSetRenderer.add(icon);
                }
              }
            }
          }
        }
        // Attachments
        if (!isColumnVisible(table, 1)) {
          if (mData != null) {
            int numOfAttachments = mData.attachedFiles.shortValue() + mData.attachedMsgs.shortValue();
            // if regular email, don't show serialized email as attachment in the table...
            if (mData.isEmail()) {
              numOfAttachments --;
            }
            if (numOfAttachments > 0) {
              JLabel icon = new JLabel(Images.get(ImageNums.ATTACH16));
              jIconSetRenderer.add(icon);
            } else {
              jIconSetRenderer.add(jNoIcon3);
            }
          }
        }
        // Expiration Date
        if (!isColumnVisible(table, 19)) {
          if (mData != null) {
            Object[] objs = mData.getExpirationIconAndText(cache.getMyUserId(), true);
            JLabel icon = new JLabel((Icon) objs[0]);
            jIconSetRenderer.add(icon);
          } else {
            jIconSetRenderer.add(jNoIcon4);
          }
        }
        // Password
        if (!isColumnVisible(table, 20)) {
          if (mData != null) {
            if (mData.bodyPassHash != null) {
              JLabel icon = new JLabel(Images.get(ImageNums.KEY16));
              jIconSetRenderer.add(icon, new GridBagConstraints(iconIndex++, 0, 1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0,0,0,0), 0, 0));
            } else {
              jIconSetRenderer.add(jNoIcon5);
            }
          }
        }

        jTwoLinesRendererSent.removeAll();
        jTwoLinesRendererSent.setLayout(new GridLayout(2, 1));
        jTwoLinesRendererSent.add(sent);
        jTwoLinesRendererSent.add(jIconSetRenderer);

        setDefaultBackground(jTwoLinesRendererSent, row, isSelected);

        // Fix up the height of the row in case the lines need more space.
        int rowHeight = table.getRowHeight();
        int thisRowHeight = -1;
        int desiredHeight = Math.max(jTwoLinesRendererSent.getPreferredSize().height, rowHeight);
        if ((thisRowHeight = table.getRowHeight(row)) < desiredHeight) {
          table.setRowHeight(row, desiredHeight);
        }
        renderer = jTwoLinesRendererSent;
      }
    }

    // other Timestamp
    else if (value instanceof Timestamp) {
      // Expiration
      if (rawColumn == 19) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        MsgLinkRecord mLink = (MsgLinkRecord) getRecord(table, row);
        MsgDataRecord mData = cache.getMsgDataRecord(mLink.msgId);
        Object[] exp = mData.getExpirationIconAndText(cache.getMyUserId());
        if (exp[0] != null) {
          setIcon((Icon) exp[0]);
          setToolTipText("Expiration: " + exp[1]);
        }
      }

      // Any timestamp column render in single or double line view as required
      if (isDoubleLineView) {
        renderer = intoTwoLines(this, row, isSelected);
      }
    }

    // Size on Disk
    else if (rawColumn == 13) {
      if (value != null) {
        String sizeString = Misc.getFormattedSize((Integer) value, 3, 2);
        setText(sizeString);
        if (isDoubleLineView) {
          renderer = intoTwoLines(this, row, isSelected);
        } else {
          setBorder(RecordTableCellRenderer.BORDER_TEXT);
        }
      }
    }

    // Secure Lock
    else if (rawColumn == 15) {
      if (value instanceof Short) {
        setBorder(RecordTableCellRenderer.BORDER_ICON);
        setText("");
        short imp = ((Short) value).shortValue();
        Object[] o = MsgDataRecord.getSecurityTextAndIcon(imp);
        setToolTipText(o[0].toString());
        setIcon((Icon)o[1]);
      }
    }

    // Expiration Date
    else if (rawColumn == 19) {
      setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      MsgLinkRecord mLink = (MsgLinkRecord) getRecord(table, row);
      MsgDataRecord mData = cache.getMsgDataRecord(mLink.msgId);
      Object[] objs = mData.getExpirationIconAndText(cache.getMyUserId(), true);
      setText((String) objs[1]);
      setIcon((Icon) objs[0]);
      if (isDoubleLineView) {
        renderer = intoTwoLines(this, row, isSelected);
      }
    }

    // Password
    else if (rawColumn == 20) {
      setText("");
      setBorder(RecordTableCellRenderer.BORDER_ICON);
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      MsgLinkRecord mLink = (MsgLinkRecord) getRecord(table, row);
      MsgDataRecord mData = cache.getMsgDataRecord(mLink.msgId);
      if (mData.bodyPassHash != null) {
        setIcon(Images.get(ImageNums.KEY16));
        setToolTipText("Password Protected");
      }
    }

    // if we have too little space, set tool tip
    setDefaultToolTip(this, table, row, column);

    return renderer;
  }

  private JComponent intoTwoLines(JLabel from, int row, boolean isSelected) {
    JComponent renderer = jRendererSmallPlainText;
    jRendererSmallPlainText.setText(from.getText());
    jRendererSmallPlainText.setIcon(from.getIcon());
    setDefaultBackground(renderer, row, isSelected);
//    JComponent renderer = jTwoLinesRenderer;
//    jRendererSmallPlainText.setText(from.getText());
//    jRendererSmallPlainText.setIcon(from.getIcon());
//    jTwoLinesRenderer.removeAll();
//    jTwoLinesRenderer.setLayout(new GridLayout(2, 1));
//    jTwoLinesRenderer.add(jRendererSmallPlainText);
//    setDefaultBackground(renderer, row, isSelected);
    return renderer;
  }

  private JLabel makeLabel(Border border, Icon icon, int sizeDifference, int fontStyle, Color color) {
    JLabel label = new JMyLabel();
    Font font = label.getFont();
    label.setHorizontalAlignment(LEADING);
    label.setHorizontalTextPosition(TRAILING);
    label.setVerticalAlignment(TOP);
    label.setVerticalTextPosition(CENTER);
    label.setBorder(border);
    label.setIcon(icon);
    label.setFont(font.deriveFont(font.getSize2D()+sizeDifference).deriveFont(fontStyle));
    label.setForeground(color);
    label.setOpaque(true);
    return label;
  }

  /**
   * Provide alternate row background colors.
   */
  public Color[] getAltBkColors() {
    return altBkColors;
  }

}
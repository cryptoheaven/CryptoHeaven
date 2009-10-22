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

package com.CH_gui.contactTable;

import java.awt.*;
import java.sql.Timestamp;
import javax.swing.*;
import javax.swing.table.*;

import com.CH_gui.list.*;
import com.CH_gui.sortedTable.JSortedTable;
import com.CH_gui.table.*;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.records.ContactRecUtil;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 * This class renderers cells of a table, where contacts' information is displayed
 *
 * Class Details:
 * 
 *
 * <b>$Revision: 1.20 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */

public class ContactTableCellRenderer extends RecordTableCellRenderer {

  private static Color fileAltColor = defaultWhite;
  private static Color fileAltColorSelected = new Color(202, 200, 192, ALPHA);
  private static Color[] altBkColors = new Color[] { fileAltColor, fileAltColorSelected };

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    int rawColumn = getRawColumn(table, column);

    // set an appropriate icon beside a contact
    // Arrow Direction
    if (rawColumn == 0) {
      if (value != null) {
        setBorder(RecordTableCellRenderer.BORDER_ICON);
        setText(null);
        Boolean v = (Boolean) value;
        if (v.booleanValue()) {
          // contactWithMe
          setIcon(Images.get(ImageNums.ARROW_RIGHT16));
          setToolTipText(com.CH_gui.lang.Lang.rb.getString("rowTip_Contact_with_you_made_by_another_person."));
        } else {
          // ! contactWithMe = I'm the owner
          setIcon(Images.get(ImageNums.ARROW_LEFT16));
          setToolTipText(com.CH_gui.lang.Lang.rb.getString("rowTip_Your_contact_with_another_person."));
        }
      }
    } 

    // Contact Name
    else if (rawColumn == 1) {
      setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
      // Find the record object type to show appropriate icon
      JSortedTable sTable = (JSortedTable) table;
      TableModel tableModel = sTable.getRawModel();
      if (tableModel instanceof ContactTableModel) {
        ContactTableModel ctm = (ContactTableModel) tableModel;
        Record record = ctm.getRowObject(sTable.convertMyRowIndexToModel(row));
        setText(ListRenderer.getRenderedText(record, false, false, false));
        // If table has no Status column, use Name's column icon for status
        if (ctm.getColumnHeaderData().convertRawColumnToModel(2) == -1 && record instanceof ContactRecord) {
          ContactRecord contactRecord = (ContactRecord) record;
          setIcon(ContactRecUtil.getStatusIcon(contactRecord.status, contactRecord.ownerUserId));
          short s = contactRecord.status.shortValue();
          switch (s) {
            case ContactRecord.STATUS_INITIATED:
              // If contact status is INITIATED, depending who is the owner, display different icon.
              if (contactRecord.ownerUserId.equals(FetchedDataCache.getSingleInstance().getMyUserId())) {
                setToolTipText(com.CH_gui.lang.Lang.rb.getString("rowTip_Contact_Invitation.__Waiting_for_authorization."));
              } else {
                setToolTipText(com.CH_gui.lang.Lang.rb.getString("rowTip_Contact_Invitation.__You_should_either_accept_or_decline_this_invitation."));
              }
              break;
            case ContactRecord.STATUS_ACCEPTED:
            case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED:
              setToolTipText(com.CH_gui.lang.Lang.rb.getString("rowTip_This_contact_has_been_accepted_and_is_active."));
              break;
            case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE:
            case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AVAILABLE :
              setToolTipText(com.CH_gui.lang.Lang.rb.getString("rowTip_This_user_is_currently_online.__This_contact_has_been_accepted_and_is_active."));
              break;
            case ContactRecord.STATUS_DECLINED :
            case ContactRecord.STATUS_DECLINED_ACKNOWLEDGED :
              setToolTipText(com.CH_gui.lang.Lang.rb.getString("rowTip_This_contact_has_been_declined_and_is_inactive."));
              break;
            default :
              setToolTipText(ContactRecUtil.getStatusText(contactRecord.status, contactRecord.ownerUserId));
              break;
          } // end switch()
        } else {
          setIcon(record.getIcon());
        }
      }

      if (getPreferredSize().width > table.getCellRect(row, column, true).width)
        setToolTipText(getText());
    }

    // Status
    else if (rawColumn == 2) {
      if (value != null) {
        setBorder(RecordTableCellRenderer.BORDER_ICON);
        setText(null);

        // Find the contact record
        JSortedTable sTable = (JSortedTable) table;
        TableModel tableModel = sTable.getRawModel();
        if (tableModel instanceof ContactTableModel) {
          ContactTableModel ctm = (ContactTableModel) tableModel;
          ContactRecord contactRecord = (ContactRecord) ctm.getRowObject(sTable.convertMyRowIndexToModel(row));
          if (contactRecord != null) {
            setIcon(ContactRecUtil.getStatusIcon(contactRecord.status, contactRecord.ownerUserId));

            short s = ((Short) value).shortValue();
            switch (s) {
              case ContactRecord.STATUS_INITIATED:
                // If contact status is INITIATED, depending who is the owner, display different icon.
                if (contactRecord.ownerUserId.equals(FetchedDataCache.getSingleInstance().getMyUserId())) {
                  setToolTipText(com.CH_gui.lang.Lang.rb.getString("rowTip_Contact_Invitation.__Waiting_for_authorization."));
                } else {
                  setToolTipText(com.CH_gui.lang.Lang.rb.getString("rowTip_Contact_Invitation.__You_should_either_accept_or_decline_this_invitation."));
                }
                break;
              case ContactRecord.STATUS_ACCEPTED:
              case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED:
                setToolTipText(com.CH_gui.lang.Lang.rb.getString("rowTip_This_contact_has_been_accepted_and_is_active."));
                break;
              case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE:
              case ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AVAILABLE :
                setToolTipText(com.CH_gui.lang.Lang.rb.getString("rowTip_This_user_is_currently_online.__This_contact_has_been_accepted_and_is_active."));
                break;
              case ContactRecord.STATUS_DECLINED :
              case ContactRecord.STATUS_DECLINED_ACKNOWLEDGED :
                setToolTipText(com.CH_gui.lang.Lang.rb.getString("rowTip_This_contact_has_been_declined_and_is_inactive."));
                break;
              default :
                setToolTipText(ContactRecUtil.getStatusText(contactRecord.status, contactRecord.ownerUserId));
                break;
            } // end switch()
          }
        }
      } // end if value != null
    } // end if rawColumn == 2

    // Permissions
    else if (rawColumn == 8) {
      setToolTipText(com.CH_gui.lang.Lang.rb.getString("columnTip_Contact_Permissions"));
    }

    // ---- begin optimization to avoid painting background ----
    //Color back = this.getBackground();
    //boolean colorMatch = (back != null) && (back.equals(table.getBackground())) && table.isOpaque();
    //this.setOpaque(!colorMatch);
//    setOpaque(!getBackground().equals(Color.white));
    // ---- end optimization to aviod painting background ----

    return this;
  }

  /**
   * Provide alternate row background colors.
   */
  public Color[] getAltBkColors() {
    return altBkColors;
  }

}
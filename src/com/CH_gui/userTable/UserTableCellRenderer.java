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

package com.CH_gui.userTable;

import com.CH_cl.service.cache.CacheUsrUtils;
import com.CH_co.service.records.EmailRecord;
import com.CH_co.service.records.Record;
import com.CH_co.service.records.UserRecord;
import com.CH_co.util.Misc;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.sortedTable.JSortedTable;
import com.CH_gui.table.RecordTableCellRenderer;
import com.CH_gui.util.Images;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;

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
* <b>$Revision: 1.21 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class UserTableCellRenderer extends RecordTableCellRenderer {

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    String origText = getText();
    int rawColumn = getRawColumn(table, column);

    // handle
    if (rawColumn == 0) {
      // set an appropriate icon beside a user
      if (value instanceof String) {
        // Find the user record
        JSortedTable sTable = (JSortedTable) table;
        TableModel tableModel = sTable.getRawModel();
        if (tableModel instanceof UserTableModel) {
          UserTableModel tm = (UserTableModel) tableModel;
          UserRecord uRec = (UserRecord) tm.getRowObject(sTable.convertMyRowIndexToModel(row));

          // Convert UserRecord to possibly a ContactRecord if we have one... note the name may change.
          Record rec = CacheUsrUtils.convertUserIdToFamiliarUser(uRec.userId, true, true);
          setIcon(ListRenderer.getRenderedIcon(rec));
          if (rec instanceof UserRecord) {
            // do nothing, username is already there
          } else {
            //ok.. maybe a familiar name is better
            setText(ListRenderer.getRenderedText(rec));
          }

          setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
        }
      }
    }
    // email address, other email address
    else if (rawColumn == 3 || rawColumn == 4) {
      // set an appropriate icon beside an email address
      if (value instanceof String) {
        String email = (String) value;
        if (email.length() > 0) {
          setIcon(Images.get(new EmailRecord().getIcon()));
          setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
        }
      }
    }
    // Accepting Spam -- Messaging option
    else if (rawColumn == 2) {
      if (value instanceof Short) {
        setHorizontalAlignment(SwingConstants.LEFT);
        setBorder(RecordTableCellRenderer.BORDER_TEXT);
        short accSpam = ((Short) value).shortValue();
        boolean innerOk = (accSpam & UserRecord.ACC_SPAM_YES_INTER) != 0;
        boolean emailRegOk = (accSpam & UserRecord.ACC_SPAM_YES_REG_EMAIL) != 0;
        boolean emailSslOk = (accSpam & UserRecord.ACC_SPAM_YES_SSL_EMAIL) != 0;
        if (innerOk && emailRegOk && emailSslOk) {
          setText(com.CH_cl.lang.Lang.rb.getString("messaging_All"));
          setToolTipText(com.CH_cl.lang.Lang.rb.getString("messagingTip_General_public_may_send_messages_to_this_user_without_special_authorization."));
        } else if (!innerOk && !emailRegOk && !emailSslOk) {
          setText(com.CH_cl.lang.Lang.rb.getString("messaging_Contacts"));
          setToolTipText(com.CH_cl.lang.Lang.rb.getString("messagingTip_Only_people_having_an_Active_Contact_with_this_user_are_authorized_to_send_him_messages."));
        } else if (!innerOk && emailRegOk && emailSslOk) {
          setText(com.CH_cl.lang.Lang.rb.getString("messaging_Email"));
          setToolTipText(com.CH_cl.lang.Lang.rb.getString("messagingTip_Only_people_having_an_Active_Contact_with_this_user_are_authorized_to_send_him_messages._This_user_also_accepts_email_correspondance."));
        } else if (innerOk && !emailRegOk && !emailSslOk) {
          setText(com.CH_cl.lang.Lang.rb.getString("messaging_Members"));
          setToolTipText(com.CH_cl.lang.Lang.rb.getString("messagingTip_Only_members_can_message_this_user."));
        } else {
          String text = "";
          if (innerOk) {
            text += com.CH_cl.lang.Lang.rb.getString("messaging_Members");
          }
          if (emailRegOk) {
            if (text.length() > 0) text += ", ";
            text += com.CH_cl.lang.Lang.rb.getString("messaging_Regular_Email");
          }
          if (emailSslOk) {
            if (text.length() > 0) text += ", ";
            text += com.CH_cl.lang.Lang.rb.getString("messaging_Encrypted_Email");
          }
          setText(text);
          setToolTipText(text);
        }
      }
    }
    // parent user id or handle (short info)
    /*
    else if (rawColumn == 3) {
      if (value instanceof String) {
        String parent = (String) value;
        if (parent.length() > 0) {
          setIcon(new UserRecord().getIcon());
          setBorder(RecordTableCellRenderer.BORDER_ICONIZED);
        }
      }
    }
    */
    // Storage Limit, and Storage Used
    else if (rawColumn == 5 || rawColumn == 6) {
      Long amount = (Long) value;
      if (amount == null) {
        setText("");
      } else {
        setText(Misc.getFormattedSize(amount, 4, 3));
      }
    }

    // If we have altered the text see if it needs to be re-highlighted
    if (stringHighlighter != null && stringHighlighter.hasHighlightingStr() && !origText.equals(getText())) {
      setText(getStringHighlight(getText()));
    }

    return this;
  }

}
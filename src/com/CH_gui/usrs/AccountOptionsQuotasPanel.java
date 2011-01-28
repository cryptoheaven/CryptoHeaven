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

package com.CH_gui.usrs;

import com.CH_co.service.records.UserRecord;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.gui.*;
import com.CH_gui.util.HTML_ClickablePane;
import com.CH_guiLib.gui.*;

import java.awt.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

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
 * <b>$Revision: 1.14 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class AccountOptionsQuotasPanel extends JPanel {

  public JCheckBox jIncludeChangesToQuotas;

  public JLabel jStorageUsed;
  public JLabel jStorageCalcDate;
  public JLabel jBandwidthUsed;
  public JLabel jAccountsUsed;

  public JTextField jStorageLimit;
  public JTextField jBandwidthLimit;

  public boolean isChangingMyUserRecord;

  private static String FETCHING_DATA = com.CH_gui.lang.Lang.rb.getString("Fetching_Data...");

  /** Creates new AccountOptionsQuotasPanel */
  public AccountOptionsQuotasPanel(ChangeListener checkBoxListener, UserRecord[] userRecords, boolean isMyUserRec, boolean includePricingInfo) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AccountOptionsQuotasPanel.class, "AccountOptionsQuotasPanel()");

    this.isChangingMyUserRecord = isMyUserRec;

    if (userRecords.length == 1)
      makeQuotasPanel(userRecords[0], includePricingInfo);
    else
      makeQuotasPanel(checkBoxListener, userRecords);

    if (trace != null) trace.exit(AccountOptionsQuotasPanel.class);
  }

  private void makeQuotasPanel(UserRecord userRecord, boolean includePricingInfo) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AccountOptionsQuotasPanel.class, "makeQuotasPanel(UserRecord userRecord, includePricingInfo)");
    JPanel panel = this;

    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    panel.setLayout(new GridBagLayout());

    int posY = 0;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Creation_Date")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JMyLabel(Misc.getFormattedTimestamp(userRecord.dateCreated)), new GridBagConstraints(1, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    if ((userRecord.isFreePromoAccount() || userRecord.isGuestAccount()) && System.currentTimeMillis() < userRecord.dateCreated.getTime() + 2L*7L*24L*60L*60L*1000L) {
      // skip displaying expiry date in the 1st 2 weeks of free accounts...
    } else {
      panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Expiry_Date")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
      Timestamp now = new Timestamp(System.currentTimeMillis());
      String expDate = Misc.getFormattedTimestamp(userRecord.dateExpired);
      if (now.compareTo(userRecord.dateExpired) > 0)
        expDate += "   " + com.CH_gui.lang.Lang.rb.getString("(Expired)");
      else {
        long moreMillis = userRecord.dateExpired.getTime() - now.getTime();
        long moreDays = moreMillis / 1000 / 60 / 60 / 24;
        moreDays = Math.max(moreDays, 0);
        expDate += "   " + java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("(###_days)"), new Object[] {new Long(moreDays)});
      }
      panel.add(new JMyLabel(expDate), new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;
    }

    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 2, 1, 0, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Storage_Limit")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    String storageLimit = "Unlimited";
    if (userRecord.storageLimit != null && userRecord.storageLimit.longValue() != UserRecord.UNLIMITED_AMOUNT)
      storageLimit = Misc.getFormattedSize(userRecord.storageLimit, 4, 3);
    JComponent storageComp = null;
    if (isChangingMyUserRecord) {
      storageComp = new JMyLabel(storageLimit);
    } else {
      jStorageLimit = new JMyTextField(storageLimit);
      storageComp = jStorageLimit;
    }
    panel.add(storageComp, new GridBagConstraints(1, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Storage_Used")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    jStorageUsed = new JMyLabel(FETCHING_DATA);
    panel.add(jStorageUsed, new GridBagConstraints(1, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    if (!userRecord.isCapableToManageUserAccounts()) {
      panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Calculated")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
      jStorageCalcDate = new JMyLabel(FETCHING_DATA);
      panel.add(jStorageCalcDate, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;
    }


    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 2, 1, 0, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;



    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Bandwidth_Limit")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    String bandwidthLimit = "Unlimited";
    if (userRecord.transferLimit != null && userRecord.transferLimit.longValue() != UserRecord.UNLIMITED_AMOUNT)
      bandwidthLimit = Misc.getFormattedSize(userRecord.transferLimit, 4, 3);
    JComponent bandwidthComp = null;
    if (isChangingMyUserRecord) {
      bandwidthComp = new JMyLabel(bandwidthLimit);
    } else {
      jBandwidthLimit = new JMyTextField(bandwidthLimit);
      bandwidthComp = jBandwidthLimit;
    }
    panel.add(bandwidthComp, new GridBagConstraints(1, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Bandwidth_Used")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    jBandwidthUsed = new JMyLabel(FETCHING_DATA);
    panel.add(jBandwidthUsed, new GridBagConstraints(1, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    // display user max sub user accounts
    if (userRecord.isCapableToManageUserAccounts()) {
      // separator
      panel.add(new JSeparator(), new GridBagConstraints(0, posY, 2, 1, 0, 0,
          GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;

      panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_User_Accounts_Limit")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      JLabel jAccountsLimit = new JMyLabel();
      if (userRecord.maxSubAccounts.shortValue() == UserRecord.UNLIMITED_AMOUNT)
        jAccountsLimit.setText("Unlimited");
      else if (userRecord.maxSubAccounts.shortValue() == 0)
        jAccountsLimit.setText("single account");
      else
        jAccountsLimit.setText((userRecord.maxSubAccounts.shortValue() + 1) + " in total (1 administrative and "+userRecord.maxSubAccounts+" managed)");
      panel.add(jAccountsLimit, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;

      panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_User_Accounts_Used")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      jAccountsUsed = new JMyLabel(FETCHING_DATA);
      panel.add(jAccountsUsed, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;
    }

    if (includePricingInfo) {
      // seperator
      panel.add(AccountOptionsSignaturesPanel.makeDivider(com.CH_gui.lang.Lang.rb.getString("tab_Pricing")), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;

      Long userId = userRecord.userId;
      String link = "<i><a href=\""+URLs.get(URLs.SIGNUP_PAGE)+"?UserID="+userId+"\">"+URLs.get(URLs.SIGNUP_PAGE)+"</a></i>";
      String pricingHtmlText = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("text_Various_accounts_are_available"), new Object[] { link });

      HTML_ClickablePane jPane = new HTML_ClickablePane(pricingHtmlText);
      jPane.setCaretPosition(0);
      jPane.setEditable(false);
      jPane.setPreferredSize(new Dimension(100, 50));
      add(new JScrollPane(jPane), new GridBagConstraints(0, posY, 2, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;
    } else {
      // filler
      panel.add(new JMyLabel(), new GridBagConstraints(0, posY, 2, 1, 0, 10,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
      posY ++;
    }

    if (trace != null) trace.exit(AccountOptionsQuotasPanel.class);
  }

  private void makeQuotasPanel(ChangeListener checkBoxListener, UserRecord[] userRecs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AccountOptionsQuotasPanel.class, "makeQuotasPanel(ChangeListener checkBoxListener, UserRecord[] userRecs)");
    JPanel panel = this;

    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    panel.setLayout(new GridBagLayout());

    int posY = 0;

    jIncludeChangesToQuotas = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Include_the_following_settings_in_this_update"));
    jIncludeChangesToQuotas.setFont(jIncludeChangesToQuotas.getFont().deriveFont(Font.BOLD));
    jIncludeChangesToQuotas.addChangeListener(checkBoxListener);
    panel.add(jIncludeChangesToQuotas, new GridBagConstraints(0, posY, 2, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Storage_Limit")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    String storageLimit = "Unlimited";
    Long commonStorageLimit = getCommonStorageLimit(userRecs);
    if (commonStorageLimit != null && commonStorageLimit.longValue() != UserRecord.UNLIMITED_AMOUNT)
      storageLimit = Misc.getFormattedSize(commonStorageLimit, 4, 3);
    jStorageLimit = new JMyTextField(storageLimit);
    panel.add(jStorageLimit, new GridBagConstraints(1, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Bandwidth_Limit")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    String bandwidthLimit = "Unlimited";
    Long commonBandwidthLimit = getCommonBandwidthLimit(userRecs);
    if (commonBandwidthLimit != null && commonBandwidthLimit.longValue() != UserRecord.UNLIMITED_AMOUNT)
      bandwidthLimit = Misc.getFormattedSize(commonBandwidthLimit, 4, 3);
    jBandwidthLimit = new JMyTextField(bandwidthLimit);
    panel.add(jBandwidthLimit, new GridBagConstraints(1, posY, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    // filler
    panel.add(new JMyLabel(), new GridBagConstraints(0, posY, 2, 1, 0, 10,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    posY ++;

    if (trace != null) trace.exit(AccountOptionsQuotasPanel.class);
  }

  private Long getCommonStorageLimit(UserRecord[] userRecs) {
    Long commonLimit = userRecs[0].storageLimit;
    boolean anyDifferent = false;
    if (userRecs.length > 1) {
      for (int i=1; i<userRecs.length; i++) {
        Long limit = userRecs[i].storageLimit;
        if (commonLimit != limit) { // skipps if both null
          if ((commonLimit != null && !commonLimit.equals(limit)) || (limit != null && !limit.equals(commonLimit))) {
            anyDifferent = true;
            break;
          }
        }
      }
    }
    return anyDifferent ? null : commonLimit;
  }
  private Long getCommonBandwidthLimit(UserRecord[] userRecs) {
    Long commonLimit = userRecs[0].transferLimit;
    boolean anyDifferent = false;
    if (userRecs.length > 1) {
      for (int i=1; i<userRecs.length; i++) {
        Long limit = userRecs[i].transferLimit;
        if (commonLimit != limit) { // skipps if both null
          if ((commonLimit != null && !commonLimit.equals(limit)) || (limit != null && !limit.equals(commonLimit))) {
            anyDifferent = true;
            break;
          }
        }
      }
    }
    return anyDifferent ? null : commonLimit;
  }

  public Long getNewStorageLimit() throws ParseException {
    Long limit = null;
    if (!isChangingMyUserRecord) {
      limit = convertLimitTextToValue(jStorageLimit.getText());
    }
    return limit;
  }
  public Long getNewBandwidthLimit() throws ParseException {
    Long limit = null;
    if (!isChangingMyUserRecord) {
      limit = convertLimitTextToValue(jBandwidthLimit.getText());
    }
    return limit;
  }

  private static Long convertLimitTextToValue(String value) throws ParseException {
    value = value != null ? value.trim().toLowerCase() : "";
    Long num = null;
    if (value.length() == 0 || value.equals("unlimited")) {
      num = new Long(UserRecord.UNLIMITED_AMOUNT);
    } else {
      StringTokenizer st = new StringTokenizer(value);
      String val = "";
      String units = "";
      if (st.hasMoreTokens()) {
        val = st.nextToken();
        // separate digits and letters into value and units
        StringBuffer charsSB = new StringBuffer();
        StringBuffer digitsSB = new StringBuffer();
        for (int i=0; i<val.length(); i++) {
          char ch = val.charAt(i);
          if (Character.isLetter(ch))
            charsSB.append(ch);
          else if (Character.isDigit(ch))
            digitsSB.append(ch);
        }
        val = digitsSB.toString();
        units = charsSB.toString();
      }
      if (units.length() == 0 || st.hasMoreTokens())
        units = st.nextToken();

      NumberFormat nf = NumberFormat.getInstance();
      nf.setGroupingUsed(true);
      long longVal = nf.parse(val).longValue();
      if (units.length() == 0 || units.startsWith("byte"))
        num = new Long(longVal);
      else if (units.startsWith("kb"))
        num = new Long(longVal*1024L);
      else if (units.startsWith("mb"))
        num = new Long(longVal*1024*1024L);
      else if (units.startsWith("gb"))
        num = new Long(longVal*1024*1024*1024L);
      else if (units.startsWith("tb"))
        num = new Long(longVal*1024*1024*1024*1024L);
      else
        throw new IllegalArgumentException("Could not parse!");
    }
    return num;
  }

}
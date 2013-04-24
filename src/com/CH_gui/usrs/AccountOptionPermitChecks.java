/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_gui.usrs;

import com.CH_gui.gui.*;
import com.CH_gui.util.MessageDialog;

import com.CH_co.service.records.UserRecord;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * <b>Copyright</b> &copy; 2001-2013
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
public class AccountOptionPermitChecks extends Object {

  public JCheckBox jIncludeChangesToOptions;
  public JCheckBox jIncludeChangesToPermissions;

  public JCheckBox jNotify, jNotifyUpdate, jNotifyGrant;
  public JCheckBox jNotifySubjectAddress, jNotifySubjectAddressUpdate, jNotifySubjectAddressGrant;
  public JCheckBox jWarnExternal, jWarnExternalUpdate, jWarnExternalGrant;
  public JCheckBox jSwitchPreview, jSwitchPreviewUpdate, jSwitchPreviewGrant;

  public JCheckBox jSpamInternal, jSpamInternalUpdate, jSpamInternalGrant;
  public JCheckBox jSpamRegEmail, jSpamRegEmailUpdate, jSpamRegEmailGrant;
  public JCheckBox jSpamSslEmail, jSpamSslEmailUpdate, jSpamSslEmailGrant;
//  public JCheckBox jSpamBlockNumeric, jSpamBlockNumericUpdate, jSpamBlockNumericGrant;

  public JCheckBox jMasterCapable, jMasterCapableUpdate, jMasterCapableGrant;
  public JCheckBox jNicknameChange, jNicknameChangeUpdate, jNicknameChangeGrant;
  public JCheckBox jPasswordChange, jPasswordChangeUpdate, jPasswordChangeGrant;
  public JCheckBox jAccountDelete, jAccountDeleteUpdate, jAccountDeleteGrant;
  public JCheckBox jContactsAlter, jContactsAlterUpdate, jContactsAlterGrant;
  public JCheckBox jContactsDelete, jContactsDeleteUpdate, jContactsDeleteGrant;
  public JCheckBox jEmailAlter, jEmailAlterUpdate, jEmailAlterGrant;
  public JCheckBox jEmailDelete, jEmailDeleteUpdate, jEmailDeleteGrant;
  public JCheckBox jContactOutside, jContactOutsideUpdate, jContactOutsideGrant;
  public JCheckBox jSecureReplyLink, jSecureReplyLinkUpdate, jSecureReplyLinkGrant;
  public JCheckBox jUseEnterKeyChatSend, jUseEnterKeyChatSendUpdate, jUseEnterKeyChatSendGrant;
  public JCheckBox jAutoUpdates, jAutoUpdatesUpdate, jAutoUpdatesGrant;
  public JCheckBox jUserOffline, jUserOfflineUpdate, jUserOfflineGrant;
  public JCheckBox jKeyOnServer, jKeyOnServerUpdate, jKeyOnServerGrant;
  public JCheckBox jFolderDelete, jFolderDeleteUpdate, jFolderDeleteGrant;
  public JCheckBox jPasswordReset, jPasswordResetUpdate, jPasswordResetGrant;

  private boolean keyOnServerInitiallySelected;

  public boolean isUpdatePermitsMode;
  public boolean isGrantPermitsMode;

  // a GUI parent to these checkboxes, a root to popup dialogs
  private Component parent;

  public void setParent(Component parent) {
    this.parent = parent;
  }

  public Short getNewSpamSetting(UserRecord userRecord) {
    short accSpam = 0;
    accSpam |= jSpamInternal.isSelected() ? UserRecord.ACC_SPAM_YES_INTER : 0;
    accSpam |= jSpamRegEmail.isSelected() ? UserRecord.ACC_SPAM_YES_REG_EMAIL : 0;
    accSpam |= jSpamSslEmail.isSelected() ? UserRecord.ACC_SPAM_YES_SSL_EMAIL : 0;
    //accSpam |= jSpamBlockNumeric.isSelected() ? UserRecord.ACC_SPAM_BLOCK_REG_NUMERIC_ADDRESS : 0;
    if (isUpdatePermitsMode) {
      accSpam |= jSpamInternalUpdate.isSelected() ? 0 : UserRecord.ACC_SPAM_YES_INTER__NO_UPDATE;
      accSpam |= jSpamRegEmailUpdate.isSelected() ? 0 : UserRecord.ACC_SPAM_YES_REG_EMAIL__NO_UPDATE;
      accSpam |= jSpamSslEmailUpdate.isSelected() ? 0 : UserRecord.ACC_SPAM_YES_SSL_EMAIL__NO_UPDATE;
      //accSpam |= jSpamBlockNumericUpdate.isSelected() ? 0 : UserRecord.ACC_SPAM_BLOCK_REG_NUMERIC_ADDRESS__NO_UPDATE;
    } else {
      // update permissions are not displayed, just copy the old ones
      if (userRecord != null)
        accSpam |= userRecord.acceptingSpam.shortValue() & UserRecord.ACC_SPAM_MASK__NO_UPDATE;
      else // else if no user, set all NO UPDATE bits
        accSpam |= UserRecord.ACC_SPAM_MASK__NO_UPDATE;
    }
    if (isGrantPermitsMode) {
      accSpam |= jSpamInternalGrant.isSelected() ? 0 : UserRecord.ACC_SPAM_YES_INTER__NO_GRANT;
      accSpam |= jSpamRegEmailGrant.isSelected() ? 0 : UserRecord.ACC_SPAM_YES_REG_EMAIL__NO_GRANT;
      accSpam |= jSpamSslEmailGrant.isSelected() ? 0 : UserRecord.ACC_SPAM_YES_SSL_EMAIL__NO_GRANT;
      //accSpam |= jSpamBlockNumericGrant.isSelected() ? 0 : UserRecord.ACC_SPAM_BLOCK_REG_NUMERIC_ADDRESS__NO_GRANT;
    } else {
      // grant permissions are not displayed, just copy the old ones
      if (userRecord != null)
        accSpam |= userRecord.acceptingSpam.shortValue() & UserRecord.ACC_SPAM_MASK__NO_GRANT;
      else // else if no user, set all NO GRANT bits
        accSpam |= UserRecord.ACC_SPAM_MASK__NO_GRANT;
    }
    return new Short(accSpam);
  }

  public Short getNewNotifySetting(UserRecord userRecord) {
    short notify = 0;
    notify |= jNotify.isSelected() ? UserRecord.EMAIL_NOTIFY_YES : 0;
    notify |= jNotifySubjectAddress.isSelected() ? UserRecord.EMAIL_NOTIFY_INCLUDE_SUBJECT_AND_FROM_ADDRESS : 0;
    notify |= jWarnExternal.isSelected() ? UserRecord.EMAIL_WARN_EXTERNAL : 0;
    notify |= jSwitchPreview.isSelected() ? UserRecord.EMAIL_MANUAL_SELECT_PREVIEW_MODE : 0;
    if (isUpdatePermitsMode) {
      notify |= jNotifyUpdate.isSelected() ? 0 : UserRecord.EMAIL_NOTIFY_YES__NO_UPDATE;
      notify |= jNotifySubjectAddressUpdate.isSelected() ? 0 : UserRecord.EMAIL_NOTIFY_INCLUDE_SUBJECT_AND_FROM_ADDRESS__NO_UPDATE;
      notify |= jWarnExternalUpdate.isSelected() ? 0 : UserRecord.EMAIL_WARN_EXTERNAL__NO_UPDATE;
      notify |= jSwitchPreviewUpdate.isSelected() ? 0 : UserRecord.EMAIL_MANUAL_SELECT_PREVIEW_MODE__NO_UPDATE;
    } else {
      // update permissions are not displayed, just copy the old ones
      if (userRecord != null)
        notify |= userRecord.notifyByEmail.shortValue() & UserRecord.EMAIL_MASK__NO_UPDATE;
      else // else if no user, set all NO UPDATE bits
        notify |= UserRecord.EMAIL_MASK__NO_UPDATE;
    }
    if (isGrantPermitsMode) {
      notify |= jNotifyGrant.isSelected() ? 0 : UserRecord.EMAIL_NOTIFY_YES__NO_GRANT;
      notify |= jNotifySubjectAddressGrant.isSelected() ? 0 : UserRecord.EMAIL_NOTIFY_INCLUDE_SUBJECT_AND_FROM_ADDRESS__NO_GRANT;
      notify |= jWarnExternalGrant.isSelected() ? 0 : UserRecord.EMAIL_WARN_EXTERNAL__NO_GRANT;
      notify |= jSwitchPreviewGrant.isSelected() ? 0 : UserRecord.EMAIL_MANUAL_SELECT_PREVIEW_MODE__NO_GRANT;
    } else {
      // grant permissions are not displayed, just copy the old ones
      if (userRecord != null)
        notify |= userRecord.notifyByEmail.shortValue() & UserRecord.EMAIL_MASK__NO_GRANT;
      else // else if no user, set all NO GRANT bits
        notify |= UserRecord.EMAIL_MASK__NO_GRANT;
    }
    return new Short(notify);
  }

  public Long getNewFlagSetting(UserRecord userRecord) {
    long flags = 0L;
    flags |= jMasterCapable.isSelected() ? UserRecord.FLAG_ENABLE_CREATING_SUBACCOUNTS__MASTER_CAPABLE : 0;
    flags |= jNicknameChange.isSelected() ? UserRecord.FLAG_ENABLE_NICKNAME_CHANGE : 0;
    flags |= jPasswordChange.isSelected() ? UserRecord.FLAG_ENABLE_PASSWORD_CHANGE : 0;
    flags |= jAccountDelete.isSelected() ? UserRecord.FLAG_ENABLE_ACCOUNT_DELETE : 0;
    flags |= jContactsAlter.isSelected() ? UserRecord.FLAG_ENABLE_GIVEN_CONTACTS_ALTER : 0;
    flags |= jContactsDelete.isSelected() ? UserRecord.FLAG_ENABLE_GIVEN_CONTACTS_DELETE : 0;
    flags |= jEmailAlter.isSelected() ? UserRecord.FLAG_ENABLE_GIVEN_EMAILS_ALTER : 0;
    flags |= jEmailDelete.isSelected() ? UserRecord.FLAG_ENABLE_GIVEN_EMAILS_DELETE : 0;
    flags |= jContactOutside.isSelected() ? UserRecord.FLAG_ENABLE_MAKING_CONTACTS_OUTSIDE_ORGANIZATION : 0;
    flags |= jSecureReplyLink.isSelected() ? 0 : UserRecord.FLAG_SKIP_SECURE_REPLY_LINK_IN_EXTERNAL_EMAILS; // inverted meaning of checkbox
    flags |= jUseEnterKeyChatSend.isSelected() ? UserRecord.FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES : 0;
    flags |= jAutoUpdates.isSelected() ? 0 : UserRecord.FLAG_DISABLE_AUTO_UPDATES;
    flags |= jUserOffline.isSelected() ? UserRecord.FLAG_USER_ONLINE_STATUS_POPUP : 0;
    flags |= jKeyOnServer.isSelected() ? UserRecord.FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER : 0;
    flags |= jFolderDelete.isSelected() ? UserRecord.FLAG_ENABLE_GIVEN_MASTER_FOLDERS_DELETE : 0;
    flags |= jPasswordReset.isSelected() ? UserRecord.FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY : 0;
    if (isUpdatePermitsMode) {
      flags |= jMasterCapableUpdate.isSelected() ? 0 : UserRecord.FLAG_ENABLE_CREATING_SUBACCOUNTS__MASTER_CAPABLE__NO_UPDATE;
      flags |= jNicknameChangeUpdate.isSelected() ? 0 : UserRecord.FLAG_ENABLE_NICKNAME_CHANGE__NO_UPDATE;
      flags |= jPasswordChangeUpdate.isSelected() ? 0 : UserRecord.FLAG_ENABLE_PASSWORD_CHANGE__NO_UPDATE;
      flags |= jAccountDeleteUpdate.isSelected() ? 0 : UserRecord.FLAG_ENABLE_ACCOUNT_DELETE__NO_UPDATE;
      flags |= jContactsAlterUpdate.isSelected() ? 0 : UserRecord.FLAG_ENABLE_GIVEN_CONTACTS_ALTER__NO_UPDATE;
      flags |= jContactsDeleteUpdate.isSelected() ? 0 : UserRecord.FLAG_ENABLE_GIVEN_CONTACTS_DELETE__NO_UPDATE;
      flags |= jEmailAlterUpdate.isSelected() ? 0 : UserRecord.FLAG_ENABLE_GIVEN_EMAILS_ALTER__NO_UPDATE;
      flags |= jEmailDeleteUpdate.isSelected() ? 0 : UserRecord.FLAG_ENABLE_GIVEN_EMAILS_DELETE__NO_UPDATE;
      flags |= jContactOutsideUpdate.isSelected() ? 0 : UserRecord.FLAG_ENABLE_MAKING_CONTACTS_OUTSIDE_ORGANIZATION__NO_UPDATE;
      flags |= jSecureReplyLinkUpdate.isSelected() ? 0 : UserRecord.FLAG_SKIP_SECURE_REPLY_LINK_IN_EXTERNAL_EMAILS__NO_UPDATE;
      flags |= jUseEnterKeyChatSendUpdate.isSelected() ? 0 : UserRecord.FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES__NO_UPDATE;
      flags |= jAutoUpdatesUpdate.isSelected() ? 0 : UserRecord.FLAG_DISABLE_AUTO_UPDATES__NO_UPDATE;
      flags |= jUserOfflineUpdate.isSelected() ? 0 : UserRecord.FLAG_USER_ONLINE_STATUS_POPUP__NO_UPDATE;
      flags |= jKeyOnServerUpdate.isSelected() ? 0 : UserRecord.FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER__NO_UPDATE;
      flags |= jFolderDeleteUpdate.isSelected() ? 0 : UserRecord.FLAG_ENABLE_GIVEN_MASTER_FOLDERS_DELETE__NO_UPDATE;
      flags |= jPasswordResetUpdate.isSelected() ? 0 : UserRecord.FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY__NO_UPDATE;
    } else {
      // update permissions are not displayed, just copy the old ones
      if (userRecord != null)
        flags |= userRecord.flags.longValue() & UserRecord.FLAG_MASK__NO_UPDATE;
      else // else if no user, set all NO UPDATE bits
        flags |= UserRecord.FLAG_MASK__NO_UPDATE;
    }
    if (isGrantPermitsMode) {
      flags |= jMasterCapableGrant.isSelected() ? 0 : UserRecord.FLAG_ENABLE_CREATING_SUBACCOUNTS__MASTER_CAPABLE__NO_GRANT;
      flags |= jNicknameChangeGrant.isSelected() ? 0 : UserRecord.FLAG_ENABLE_NICKNAME_CHANGE__NO_GRANT;
      flags |= jPasswordChangeGrant.isSelected() ? 0 : UserRecord.FLAG_ENABLE_PASSWORD_CHANGE__NO_GRANT;
      flags |= jAccountDeleteGrant.isSelected() ? 0 : UserRecord.FLAG_ENABLE_ACCOUNT_DELETE__NO_GRANT;
      flags |= jContactsAlterGrant.isSelected() ? 0 : UserRecord.FLAG_ENABLE_GIVEN_CONTACTS_ALTER__NO_GRANT;
      flags |= jContactsDeleteGrant.isSelected() ? 0 : UserRecord.FLAG_ENABLE_GIVEN_CONTACTS_DELETE__NO_GRANT;
      flags |= jEmailAlterGrant.isSelected() ? 0 : UserRecord.FLAG_ENABLE_GIVEN_EMAILS_ALTER__NO_GRANT;
      flags |= jEmailDeleteGrant.isSelected() ? 0 : UserRecord.FLAG_ENABLE_GIVEN_EMAILS_DELETE__NO_GRANT;
      flags |= jContactOutsideGrant.isSelected() ? 0 : UserRecord.FLAG_ENABLE_MAKING_CONTACTS_OUTSIDE_ORGANIZATION__NO_GRANT;
      flags |= jSecureReplyLinkGrant.isSelected() ? 0 : UserRecord.FLAG_SKIP_SECURE_REPLY_LINK_IN_EXTERNAL_EMAILS__NO_GRANT;
      flags |= jUseEnterKeyChatSendGrant.isSelected() ? 0 : UserRecord.FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES__NO_GRANT;
      flags |= jAutoUpdatesGrant.isSelected() ? 0 : UserRecord.FLAG_DISABLE_AUTO_UPDATES__NO_GRANT;
      flags |= jUserOfflineGrant.isSelected() ? 0 : UserRecord.FLAG_USER_ONLINE_STATUS_POPUP__NO_GRANT;
      flags |= jKeyOnServerGrant.isSelected() ? 0 : UserRecord.FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER__NO_GRANT;
      flags |= jFolderDeleteGrant.isSelected() ? 0 : UserRecord.FLAG_ENABLE_GIVEN_MASTER_FOLDERS_DELETE__NO_GRANT;
      flags |= jPasswordResetGrant.isSelected() ? 0 : UserRecord.FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY__NO_GRANT;
    } else {
      // grant permissions are not displayed, just copy the old ones
      if (userRecord != null)
        flags |= userRecord.flags.longValue() & UserRecord.FLAG_MASK__NO_GRANT;
      else // else if no user, set all NO GRANT bits
        flags |= UserRecord.FLAG_MASK__NO_GRANT;
    }
    return new Long(flags);
  }


  public JPanel createOptionsPanel(ChangeListener checkBoxListener, UserRecord myUserRecord, UserRecord[] userRecs) {
    JPanel panel = new JPanel();

    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    panel.setLayout(new GridBagLayout());

    boolean includeUpdate = isUpdatePermitsMode;
    boolean forSubAccounts = true;

    for (int i=0; i<userRecs.length; i++) {
      if (!userRecs[i].isBusinessSubAccount()) {
        forSubAccounts = false;
        break;
      }
    }

    // insert the center portion of the panel
    JPanel centerPanel = new JPanel();
    centerPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
    centerPanel.setLayout(new BorderLayout(10, 10));
    if (userRecs.length > 1) {
      jIncludeChangesToOptions = new JMyCheckBox(com.CH_cl.lang.Lang.rb.getString("check_Include_the_following_settings_in_this_update"));
      jIncludeChangesToOptions.setFont(jIncludeChangesToOptions.getFont().deriveFont(Font.BOLD));
      jIncludeChangesToOptions.addChangeListener(checkBoxListener);
      centerPanel.add(jIncludeChangesToOptions, BorderLayout.NORTH);
    }
    centerPanel.add(new JMyLabel("Security Settings"), BorderLayout.WEST);
    centerPanel.add(new JMyLabel(), BorderLayout.CENTER);
    if (isUpdatePermitsMode) {
      centerPanel.add(new JMyLabel("Allow User to Change the Setting"), BorderLayout.EAST);
    }
    // insert the center portion of the panel
    panel.add(centerPanel, new GridBagConstraints(0, 0, 1, 1, 10, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));


    JPanel bottomPanel = new JPanel();
    bottomPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
    bottomPanel.setLayout(new GridBagLayout());

    int posY = 0;
    
    jKeyOnServer = new JMyCheckBox(com.CH_cl.lang.Lang.rb.getString("check_Store_encrypted_Private_Key_on_the_server."));
    jKeyOnServer.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!jKeyOnServer.isSelected() && jPasswordReset.isSelected()) {
          jKeyOnServer.setSelected(true);
          MessageDialog.showWarningDialog(parent, "To remove private key from the server, you must have Password Reset and Key Recovery option disabled.", "Invalid selection.");
        } else if (!jKeyOnServer.isSelected() && keyOnServerInitiallySelected) {
          boolean rc = MessageDialog.showDialogYesNo(parent, "When you remove your private key from the server, access to your account will be restricted only to the single computer on which you store your private key.  Password Reset and Key Recovery features will be unavailable.  \n\nAre you sure you want to store your private key on this local computer?", "Confirmation");
          jKeyOnServer.setSelected(!rc);
          if (rc) {
            jPasswordReset.setSelected(false);
          }
        }
      }
    });
    jKeyOnServerUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jKeyOnServer, jKeyOnServerUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecs), UserRecord.FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER, checkBoxListener, posY);
    posY ++;

    jPasswordReset = new JMyCheckBox("Enable Password Reset and Key Recovery");
    jPasswordReset.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (jPasswordReset.isSelected() && !jKeyOnServer.isSelected()) {
          jPasswordReset.setSelected(false);
          MessageDialog.showWarningDialog(parent, "To enable Password Reset and Key Recovery feature, you must have Store encrypted Private Key on the server option enabled.", "Invalid selection.");
        }
      }
    });
    jPasswordResetUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jPasswordReset, jPasswordResetUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecs), UserRecord.FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY, checkBoxListener, posY);
    posY ++;
    if (!forSubAccounts) {
      jPasswordReset.setEnabled(false);
    }

    if (isUpdatePermitsMode) {
      jKeyOnServer.setEnabled(false);
      if (!jKeyOnServer.isSelected()) {
        jPasswordReset.setSelected(false);
        jPasswordReset.setEnabled(false);
      }
    }

    jSpamInternal = new JMyCheckBox(com.CH_cl.lang.Lang.rb.getString("check_Accept_messages_from_outside_of_Contact_List."));
    jSpamInternalUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jSpamInternal, jSpamInternalUpdate, myUserRecord.acceptingSpam, getMostCommonAcceptingSpamBits(userRecs), UserRecord.ACC_SPAM_YES_INTER, checkBoxListener, posY);
    posY ++;

    jSpamRegEmail = new JMyCheckBox(com.CH_cl.lang.Lang.rb.getString("check_Accept_regular_external_email."));
    jSpamRegEmailUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jSpamRegEmail, jSpamRegEmailUpdate, myUserRecord.acceptingSpam, getMostCommonAcceptingSpamBits(userRecs), UserRecord.ACC_SPAM_YES_REG_EMAIL, checkBoxListener, posY);
    posY ++;

    jSpamSslEmail = new JMyCheckBox(com.CH_cl.lang.Lang.rb.getString("check_Accept_encrypted_external_email."));
    jSpamSslEmailUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jSpamSslEmail, jSpamSslEmailUpdate, myUserRecord.acceptingSpam, getMostCommonAcceptingSpamBits(userRecs), UserRecord.ACC_SPAM_YES_SSL_EMAIL, checkBoxListener, posY);
    posY ++;

//    jSpamBlockNumeric = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Block_default_numeric_email_address_for_external_email."));
//    jSpamBlockNumericUpdate = new JMyCheckBox();
//    addCheckBoxes(bottomPanel, includeUpdate, jSpamBlockNumeric, jSpamBlockNumericUpdate, myUserRecord.acceptingSpam, userRecord.acceptingSpam, UserRecord.ACC_SPAM_BLOCK_REG_NUMERIC_ADDRESS, checkBoxListener, posY);
//    posY ++;

    jWarnExternal = new JMyCheckBox(com.CH_cl.lang.Lang.rb.getString("check_Display_a_warning_before_sending_unencrypted_email."));
    jWarnExternalUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jWarnExternal, jWarnExternalUpdate, myUserRecord.notifyByEmail, getMostCommonNotifyByEmailBits(userRecs), UserRecord.EMAIL_WARN_EXTERNAL, checkBoxListener, posY);
    posY ++;

    jSwitchPreview = new JMyCheckBox("Enhance privacy by filtering out images from inbox emails.");
    jSwitchPreviewUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jSwitchPreview, jSwitchPreviewUpdate, myUserRecord.notifyByEmail, getMostCommonNotifyByEmailBits(userRecs), UserRecord.EMAIL_MANUAL_SELECT_PREVIEW_MODE, checkBoxListener, posY);
    posY ++;

    if (jNotify == null) {
      jNotify = new JMyCheckBox(com.CH_cl.lang.Lang.rb.getString("check_Send_email_notification_when_new_messages_arrive."));
      jNotifyUpdate = new JMyCheckBox();
      addCheckBoxes(null, jNotify, jNotifyUpdate, myUserRecord.notifyByEmail, getMostCommonNotifyByEmailBits(userRecs), UserRecord.EMAIL_NOTIFY_YES, checkBoxListener, posY);
      posY ++;
    }

    if (jNotifySubjectAddress == null) {
      jNotifySubjectAddress = new JMyCheckBox(com.CH_cl.lang.Lang.rb.getString("check_Include_sender_address_and_message_subject_in_notifications."));
      jNotifySubjectAddressUpdate = new JMyCheckBox();
      addCheckBoxes(bottomPanel, jNotifySubjectAddress, jNotifySubjectAddressUpdate, myUserRecord.notifyByEmail, getMostCommonNotifyByEmailBits(userRecs), UserRecord.EMAIL_NOTIFY_INCLUDE_SUBJECT_AND_FROM_ADDRESS, checkBoxListener, posY);
      posY ++;
    }

    // Account Options divider
    bottomPanel.add(new JMyLabel("Account Options"), new GridBagConstraints(0, posY, 2, 1, 0, 0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    jSecureReplyLink = new JMyCheckBox("Include secure reply link in external email.");
    jSecureReplyLinkUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jSecureReplyLink, jSecureReplyLinkUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecs), UserRecord.FLAG_SKIP_SECURE_REPLY_LINK_IN_EXTERNAL_EMAILS, true, checkBoxListener, posY);
    posY ++;

    jUserOffline = new JMyCheckBox("Enable online status pop-up notifications.");
    jUserOfflineUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jUserOffline, jUserOfflineUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecs), UserRecord.FLAG_USER_ONLINE_STATUS_POPUP, checkBoxListener, posY);
    posY ++;

    jUseEnterKeyChatSend = new JMyCheckBox("Use ENTER key to send messages during chat sessions.");
    jUseEnterKeyChatSendUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jUseEnterKeyChatSend, jUseEnterKeyChatSendUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecs), UserRecord.FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES, checkBoxListener, posY);
    posY ++;

    jAutoUpdates = new JMyCheckBox("Enable automatic software updates.");
    jAutoUpdatesUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jAutoUpdates, jAutoUpdatesUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecs), UserRecord.FLAG_DISABLE_AUTO_UPDATES, true, checkBoxListener, posY);
    posY ++;


    // filler
    bottomPanel.add(new JMyLabel(), new GridBagConstraints(0, posY, 2, 1, 0, 10,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));

    // insert the bottom portion of the panel
    panel.add(bottomPanel, new GridBagConstraints(0, 2, 1, 1, 10, 10,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0,0,0,0), 0, 0));

    return panel;
  }


  public JPanel createPermissionsPanel(ChangeListener checkBoxListener, UserRecord myUserRecord, UserRecord[] userRecs) {
    JPanel panel = new JPanel();

    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    panel.setLayout(new GridBagLayout());


    boolean includeUpdate = false;


    // insert the center portion of the panel
    JPanel centerPanel = new JPanel();
    centerPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
    centerPanel.setLayout(new BorderLayout(10, 10));
    if (userRecs.length > 1) {
      jIncludeChangesToPermissions = new JMyCheckBox(com.CH_cl.lang.Lang.rb.getString("check_Include_the_following_settings_in_this_update"));
      jIncludeChangesToPermissions.setFont(jIncludeChangesToPermissions.getFont().deriveFont(Font.BOLD));
      jIncludeChangesToPermissions.addChangeListener(checkBoxListener);
      centerPanel.add(jIncludeChangesToPermissions, BorderLayout.NORTH);
    }
    centerPanel.add(new JMyLabel("User Permissions"), BorderLayout.WEST);
    centerPanel.add(new JMyLabel(), BorderLayout.CENTER);
    if (includeUpdate) {
      centerPanel.add(new JMyLabel("Allow User to Change the Setting"), BorderLayout.EAST);
    }
    // insert the center portion of the panel
    panel.add(centerPanel, new GridBagConstraints(0, 0, 1, 1, 10, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));



    JPanel bottomPanel = new JPanel();
    bottomPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
    bottomPanel.setLayout(new GridBagLayout());

    int posY = 0;

    // remove this option as it is not implemented yet -- panel is NULL
    jMasterCapable = new JMyCheckBox("Permit creating sub-user accounts.");
    jMasterCapableUpdate = new JMyCheckBox();
    addCheckBoxes(null, includeUpdate, jMasterCapable, jMasterCapableUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecs), UserRecord.FLAG_ENABLE_CREATING_SUBACCOUNTS__MASTER_CAPABLE, checkBoxListener, posY);
    posY ++;

    jPasswordChange = new JMyCheckBox("Permit change of password.");
    jPasswordChangeUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jPasswordChange, jPasswordChangeUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecs), UserRecord.FLAG_ENABLE_PASSWORD_CHANGE, checkBoxListener, posY);
    posY ++;

    jNicknameChange = new JMyCheckBox("Permit change of nickname.");
    jNicknameChangeUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jNicknameChange, jNicknameChangeUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecs), UserRecord.FLAG_ENABLE_NICKNAME_CHANGE, checkBoxListener, posY);
    posY ++;

    // remove this option as it is not implemented yet -- panel is NULL
    jAccountDelete = new JMyCheckBox("Permit deletion of account.");
    jAccountDeleteUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jAccountDelete, jAccountDeleteUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecs), UserRecord.FLAG_ENABLE_ACCOUNT_DELETE, checkBoxListener, posY);
    posY ++;

    jContactsAlter = new JMyCheckBox("Permit modification of assigned contacts.");
    jContactsAlterUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jContactsAlter, jContactsAlterUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecs), UserRecord.FLAG_ENABLE_GIVEN_CONTACTS_ALTER, checkBoxListener, posY);
    posY ++;

    jContactsDelete = new JMyCheckBox("Permit deletion of assigned contacts.");
    jContactsDeleteUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jContactsDelete, jContactsDeleteUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecs), UserRecord.FLAG_ENABLE_GIVEN_CONTACTS_DELETE, checkBoxListener, posY);
    posY ++;

    jEmailAlter = new JMyCheckBox("Permit change of assigned email address.");
    jEmailAlterUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jEmailAlter, jEmailAlterUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecs), UserRecord.FLAG_ENABLE_GIVEN_EMAILS_ALTER, checkBoxListener, posY);
    posY ++;

    // remove this option as it sounds a little useless (change option controls delete as well) -- panel is NULL
    jEmailDelete = new JMyCheckBox("Allow removing email address assigned to me.");
    jEmailDeleteUpdate = new JMyCheckBox();
    addCheckBoxes(null, includeUpdate, jEmailDelete, jEmailDeleteUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecs), UserRecord.FLAG_ENABLE_GIVEN_EMAILS_DELETE, checkBoxListener, posY);
    posY ++;

    jFolderDelete = new JMyCheckBox("Permit deletion of administrator assigned folders.");
    jFolderDeleteUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jFolderDelete, jFolderDeleteUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecs), UserRecord.FLAG_ENABLE_GIVEN_MASTER_FOLDERS_DELETE, checkBoxListener, posY);
    posY ++;

    jContactOutside = new JMyCheckBox("Permit contacts outside of organization.");
    jContactOutsideUpdate = new JMyCheckBox();
    addCheckBoxes(bottomPanel, includeUpdate, jContactOutside, jContactOutsideUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecs), UserRecord.FLAG_ENABLE_MAKING_CONTACTS_OUTSIDE_ORGANIZATION, checkBoxListener, posY);
    posY ++;

    // filler
    bottomPanel.add(new JMyLabel(), new GridBagConstraints(0, posY, 2, 1, 0, 10,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));

    // insert the bottom portion of the panel
    panel.add(bottomPanel, new GridBagConstraints(0, 2, 1, 1, 10, 10,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0,0,0,0), 0, 0));

    return panel;
  }


  public void addCheckBoxes(JPanel panel, boolean withUpdateCheck, JCheckBox jCheck, JCheckBox jCheckUpdate, Number myBits, Number bits, long bitMask, ChangeListener checkBoxListener, int posY) {
    addCheckBoxes(panel, withUpdateCheck, jCheck, jCheckUpdate, myBits, bits, bitMask, false, checkBoxListener, posY, isUpdatePermitsMode);
  }
  public void addCheckBoxes(JPanel panel, boolean withUpdateCheck, JCheckBox jCheck, JCheckBox jCheckUpdate, Number myBits, Number bits, long bitMask, boolean invertMeaning, ChangeListener checkBoxListener, int posY) {
    addCheckBoxes(panel, withUpdateCheck, jCheck, jCheckUpdate, myBits, bits, bitMask, invertMeaning, checkBoxListener, posY, isUpdatePermitsMode);
  }
  public void addCheckBoxes(JPanel panel, JCheckBox jCheck, JCheckBox jCheckUpdate, Number myBits, Number bits, long bitMask, ChangeListener checkBoxListener, int posY) {
    addCheckBoxes(panel, true, jCheck, jCheckUpdate, myBits, bits, bitMask, false, checkBoxListener, posY, isUpdatePermitsMode);
  }
  private static void addCheckBoxes(JPanel panel, boolean withUpdateCheck, JCheckBox jCheck, JCheckBox jCheckUpdate, Number myBits, Number bits, long bitMask, boolean invertMeaning, ChangeListener checkBoxListener, int posY, boolean isUpdatePermitsMode) {
    updateCheckBox(jCheck, jCheckUpdate, myBits, bits, bitMask, invertMeaning, isUpdatePermitsMode);
    if (panel != null) addCheckBox(panel, jCheck, false, posY);
    if (isUpdatePermitsMode) {
      if (panel != null) {
        if (withUpdateCheck) {
          addCheckBox(panel, jCheckUpdate, true, posY);
        }
      }
    }
    jCheck.addChangeListener(checkBoxListener);
    jCheckUpdate.addChangeListener(checkBoxListener);
  }
  private static void addCheckBox(JPanel panel, JCheckBox jCheck, boolean isUpdate, int posY) {
    panel.add(jCheck, new GridBagConstraints(isUpdate ? 1 : 0, posY, 1, 1, isUpdate ? 0 : 10, 0,
          isUpdate ? GridBagConstraints.EAST : GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
  }


  public void updateCheckBoxes(UserRecord myUserRecord, UserRecord userRecord) {
    updateCheckBoxes(myUserRecord, new UserRecord[] { userRecord });
  }
  public void updateCheckBoxes(UserRecord myUserRecord, UserRecord[] userRecords) {
    updateCheckBox(jSpamInternal, jSpamInternalUpdate, myUserRecord.acceptingSpam, getMostCommonAcceptingSpamBits(userRecords), UserRecord.ACC_SPAM_YES_INTER, false, isUpdatePermitsMode);
    updateCheckBox(jSpamRegEmail, jSpamRegEmailUpdate, myUserRecord.acceptingSpam, getMostCommonAcceptingSpamBits(userRecords), UserRecord.ACC_SPAM_YES_REG_EMAIL, false, isUpdatePermitsMode);
    updateCheckBox(jSpamSslEmail, jSpamSslEmailUpdate, myUserRecord.acceptingSpam, getMostCommonAcceptingSpamBits(userRecords), UserRecord.ACC_SPAM_YES_SSL_EMAIL, false, isUpdatePermitsMode);
//    updateCheckBox(jSpamBlockNumeric, jSpamBlockNumericUpdate, myUserRecord.acceptingSpam, userRecord.acceptingSpam, UserRecord.ACC_SPAM_BLOCK_REG_NUMERIC_ADDRESS, isUpdatePermitsMode);
    updateCheckBox(jWarnExternal, jWarnExternalUpdate, myUserRecord.notifyByEmail, getMostCommonNotifyByEmailBits(userRecords), UserRecord.EMAIL_WARN_EXTERNAL, false, isUpdatePermitsMode);
    updateCheckBox(jSwitchPreview, jSwitchPreviewUpdate, myUserRecord.notifyByEmail, getMostCommonNotifyByEmailBits(userRecords), UserRecord.EMAIL_MANUAL_SELECT_PREVIEW_MODE, false, isUpdatePermitsMode);
    updateCheckBox(jAutoUpdates, jAutoUpdatesUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecords), UserRecord.FLAG_DISABLE_AUTO_UPDATES, true, isUpdatePermitsMode);
    updateCheckBox(jUserOffline, jUserOfflineUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecords), UserRecord.FLAG_USER_ONLINE_STATUS_POPUP, false, isUpdatePermitsMode);
    updateCheckBox(jKeyOnServer, jKeyOnServerUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecords), UserRecord.FLAG_STORE_ENC_PRIVATE_KEY_ON_SERVER, false, isUpdatePermitsMode);
    updateCheckBox(jNotify, jNotifyUpdate, myUserRecord.notifyByEmail, getMostCommonNotifyByEmailBits(userRecords), UserRecord.EMAIL_NOTIFY_YES, false, isUpdatePermitsMode);
    updateCheckBox(jNotifySubjectAddress, jNotifySubjectAddressUpdate, myUserRecord.notifyByEmail, getMostCommonNotifyByEmailBits(userRecords), UserRecord.EMAIL_NOTIFY_INCLUDE_SUBJECT_AND_FROM_ADDRESS, false, isUpdatePermitsMode);
    updateCheckBox(jMasterCapable, jMasterCapableUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecords), UserRecord.FLAG_ENABLE_CREATING_SUBACCOUNTS__MASTER_CAPABLE, false, isUpdatePermitsMode);
    updateCheckBox(jPasswordChange, jPasswordChangeUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecords), UserRecord.FLAG_ENABLE_PASSWORD_CHANGE, false, isUpdatePermitsMode);
    updateCheckBox(jNicknameChange, jNicknameChangeUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecords), UserRecord.FLAG_ENABLE_NICKNAME_CHANGE, false, isUpdatePermitsMode);
    updateCheckBox(jAccountDelete, jAccountDeleteUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecords), UserRecord.FLAG_ENABLE_ACCOUNT_DELETE, false, isUpdatePermitsMode);
    updateCheckBox(jContactsAlter, jContactsAlterUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecords), UserRecord.FLAG_ENABLE_GIVEN_CONTACTS_ALTER, false, isUpdatePermitsMode);
    updateCheckBox(jContactsDelete, jContactsDeleteUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecords), UserRecord.FLAG_ENABLE_GIVEN_CONTACTS_DELETE, false, isUpdatePermitsMode);
    updateCheckBox(jEmailAlter, jEmailAlterUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecords), UserRecord.FLAG_ENABLE_GIVEN_EMAILS_ALTER, false, isUpdatePermitsMode);
    updateCheckBox(jEmailDelete, jEmailDeleteUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecords), UserRecord.FLAG_ENABLE_GIVEN_EMAILS_DELETE, false, isUpdatePermitsMode);
    updateCheckBox(jFolderDelete, jFolderDeleteUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecords), UserRecord.FLAG_ENABLE_GIVEN_MASTER_FOLDERS_DELETE, false, isUpdatePermitsMode);
    updateCheckBox(jContactOutside, jContactOutsideUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecords), UserRecord.FLAG_ENABLE_MAKING_CONTACTS_OUTSIDE_ORGANIZATION, false, isUpdatePermitsMode);
    updateCheckBox(jSecureReplyLink, jSecureReplyLinkUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecords), UserRecord.FLAG_SKIP_SECURE_REPLY_LINK_IN_EXTERNAL_EMAILS, true, isUpdatePermitsMode);
    updateCheckBox(jPasswordReset, jPasswordResetUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecords), UserRecord.FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY, false, isUpdatePermitsMode);
    updateCheckBox(jUseEnterKeyChatSend, jUseEnterKeyChatSendUpdate, myUserRecord.flags, getMostCommonFlagsBits(userRecords), UserRecord.FLAG_USE_ENTER_TO_SEND_CHAT_MESSAGES, false, isUpdatePermitsMode);

    if (isUpdatePermitsMode) {
      jKeyOnServer.setEnabled(false);
      if (!jKeyOnServer.isSelected()) {
        jPasswordReset.setSelected(false);
        jPasswordReset.setEnabled(false);
      }
    }
    keyOnServerInitiallySelected = jKeyOnServer.isSelected();
  }
  private static void updateCheckBox(JCheckBox jCheck, JCheckBox jCheckUpdate, Number myBits, Number bits, long bitMask, boolean invertMeaning, boolean isUpdatePermitsMode) {
    long noUpdateBitMask = bitMask << 1;
    long noGrantBitMask = bitMask << 2;
    boolean selected = (bits.longValue() & bitMask) != 0;
    if (invertMeaning)
      selected = !selected;
    jCheck.setSelected(selected);
    jCheckUpdate.setSelected((bits.longValue() & noUpdateBitMask) == 0);
    if (isUpdatePermitsMode) {
      if ((myBits.longValue() & noGrantBitMask) != 0) {
        jCheckUpdate.setEnabled(false);
      }
    }
    if ((!isUpdatePermitsMode && (myBits.longValue() & noUpdateBitMask) != 0) ||
        (isUpdatePermitsMode && (myBits.longValue() & noGrantBitMask) != 0))
    {
      jCheck.setEnabled(false);
    }
  }

  public static Long getMostCommonAcceptingSpamBits(UserRecord[] uRecs) {
    long commonBits = 0;
    for (int bit=0; bit<64; bit++) {
      long bitMask = 1L << bit;
      int count = 0;
      for (int i=0; i<uRecs.length; i++) {
        if ((uRecs[i].acceptingSpam.longValue() & bitMask) != 0)
          count ++;
      }
      if (count > uRecs.length/2)
        commonBits = commonBits | bitMask;
    }
    return new Long(commonBits);
  }

  public static Long getMostCommonNotifyByEmailBits(UserRecord[] uRecs) {
    long commonBits = 0;
    for (int bit=0; bit<64; bit++) {
      long bitMask = 1L << bit;
      int count = 0;
      for (int i=0; i<uRecs.length; i++) {
        if ((uRecs[i].notifyByEmail.longValue() & bitMask) != 0)
          count ++;
      }
      if (count > uRecs.length/2)
        commonBits = commonBits | bitMask;
    }
    return new Long(commonBits);
  }

  public static Long getMostCommonFlagsBits(UserRecord[] uRecs) {
    long commonBits = 0;
    for (int bit=0; bit<63; bit++) {
      long bitMask = 1L << bit;
      int count = 0;
      for (int i=0; i<uRecs.length; i++) {
        if ((uRecs[i].flags.longValue() & bitMask) != 0)
          count ++;
      }
      if (count > uRecs.length/2)
        commonBits = commonBits | bitMask;
    }
    return new Long(commonBits);
  }
}
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

package com.CH_gui.usrs;

import com.CH_gui.action.*;
import com.CH_gui.actionGui.*;
import com.CH_gui.dialog.*;
import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.MyInsets;
import com.CH_gui.util.ActionProducerI;
import com.CH_gui.util.Images;

import com.CH_co.util.*;
import com.CH_co.trace.Trace;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

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
 * <b>$Revision: 1.17 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class EmailInvitationPanel extends JPanel implements ActionProducerI {

  private JTextComponent initialAddressSource;
  private Action[] actions;

  public static final int SEND_EMAIL_INVITAION_ACTION = 0;

  private int leadingActionId = Actions.LEADING_ACTION_ID_EMAIL_INVITATION_PANEL;

  /** Creates new EmailInvitationPanel */
  public EmailInvitationPanel(JTextComponent initialAddressSource, boolean withInviteActions) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EmailInvitationPanel.class, "EmailInvitationPanel(JTextComponent initialAddressSource, boolean withInviteActions)");
    this.initialAddressSource = initialAddressSource;
    initActions();
    createPanel(withInviteActions);
    if (trace != null) trace.exit(EmailInvitationPanel.class);
  }


  private void createPanel(boolean withInviteActions) {
    setLayout(new GridBagLayout());
    setBorder(new EmptyBorder(0,0,0,0));

    JLabel label = new JMyLabel("If your Friends and Associates have not registered yet - invite them.");
    label.setIcon(Images.get(ImageNums.PEOPLE32));
    add(label, new GridBagConstraints(0, 0, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(10, 10, withInviteActions ? 1 : 10, 10), 0, 0));

    if (withInviteActions) {
      Action action = actions[SEND_EMAIL_INVITAION_ACTION];
      JButton actionButton = new JActionButton(action, false, null, false);
//      actionButton.setText((String) action.getValue(Actions.NAME));
//      actionButton.setHorizontalTextPosition(JButton.RIGHT);
//      actionButton.setVerticalTextPosition(JButton.CENTER);
      add(actionButton, new GridBagConstraints(0, 1, 1, 1, 10, 10, 
          GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new MyInsets(1, 0, 10, 5), 0, 0));
    }
  }

  /** 
   * Send Email Message to invite someone to join.
   */
  private class SendEmailInvitationAction extends AbstractActionTraced {
    public SendEmailInvitationAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Invite_Friends_and_Associates_..."), Images.get(ImageNums.MAIL_SEND_INVITE_16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_New_Email_Message_to_invite_others_to_join_the_service."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_SEND_INVITE_24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Invite_by_Email"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      Window w = SwingUtilities.windowForComponent(EmailInvitationPanel.this);
      if (w instanceof Dialog)
        new InviteByEmailDialog((Dialog) w, initialAddressSource != null ? initialAddressSource.getText() : null);
      else if (w instanceof Frame)
        new InviteByEmailDialog((Frame) w, initialAddressSource != null ? initialAddressSource.getText() : null);
    }
  }

  /****************************************************************************/
  /*        A c t i o n P r o d u c e r I                                  
  /****************************************************************************/

  private void initActions() {
    actions = new Action[1];
    actions[SEND_EMAIL_INVITAION_ACTION] = new SendEmailInvitationAction(leadingActionId + SEND_EMAIL_INVITAION_ACTION);
    setEnabledActions();
  }



  /** @return all the acitons that this objects produces.
   */
  public Action[] getActions() {
    return actions;
  }

  /**
   * Enables or Disables actions based on the current state of the Action Producing component.
   */
  public void setEnabledActions() {
    Window w = SwingUtilities.windowForComponent(this);
    actions[SEND_EMAIL_INVITAION_ACTION].setEnabled(w != null);
  }

  /**
   * Final Action Producers will not be traversed to collect its containing objects' actions.
   * @return true if this object will gather all actions from its childeren or hide them counciously.
   */
  public boolean isFinalActionProducer() {
    return true;
  }

}
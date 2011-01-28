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

package com.CH_gui.contactTable;

import com.CH_gui.util.Images;
import com.CH_gui.util.MessageDialog;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.cache.event.*;
import com.CH_cl.service.engine.*;

import com.CH_co.cryptx.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.msg.dataSets.usr.*;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.action.AbstractActionTraced;
import com.CH_gui.action.ActionUtilities;
import com.CH_gui.dialog.InviteByEmailDialog;
import com.CH_gui.frame.*;
import com.CH_gui.gui.*;
import com.CH_gui.table.*;

import com.CH_guiLib.gui.*;

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
 * <b>$Revision: 1.25 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class ContactTableComponent extends RecordTableComponent {

  private JMyListCombo combo;
  private UserListener userListener;
  private JMyTextField jAddEmail;
  private String addEmailHint = " add email@address or user";

  private boolean autoCreateWebAccounts;

  /** Creates new ContactTableComponent */
  /*
  public ContactTableComponent() {
    super(new ContactActionTable(), Template.EMPTY_CONTACTS, Template.BACK_CONTACTS);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactTableComponent.class, "ContactTableComponent()");
    setTitle("Contacts");
    if (trace != null) trace.exit(ContactTableComponent.class);
  }
   */
  /** Creates new ContactTableComponent */
  public ContactTableComponent(RecordFilter contactFilter, String emptyTemplateName, String backTemplateName, boolean withDoubleClickAction, boolean suppressToolbar, boolean suppressUtilityBar) {
    super(new ContactActionTable(contactFilter, withDoubleClickAction), emptyTemplateName, backTemplateName, null, suppressToolbar, suppressUtilityBar, false);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactTableComponent.class, "ContactTableComponent(Record contactFilter, String emptyTemplateName, String backTemplateName, boolean withDoubleClickAction, boolean suppressToolbar, boolean suppressUtilityBar)");
    if (trace != null) trace.args(contactFilter, emptyTemplateName, backTemplateName);
    if (trace != null) trace.args(withDoubleClickAction);
    if (trace != null) trace.args(suppressToolbar);
    if (trace != null) trace.args(suppressUtilityBar);
    initialize(suppressUtilityBar);
    if (trace != null) trace.exit(ContactTableComponent.class);
  }
  /** Creates new ContactTableComponent */
  public ContactTableComponent(Record[] initialData, RecordFilter contactFilter, String emptyTemplateName, String backTemplateName, boolean withDoubleClickAction, boolean suppressToolbar, boolean suppressUtilityBar) {
    super(new ContactActionTable(initialData, contactFilter, withDoubleClickAction), emptyTemplateName, backTemplateName, null, suppressToolbar, suppressUtilityBar, false);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactTableComponent.class, "ContactTableComponent(Record[] initialData, Record contactFilter, String emptyTemplateName, String backTemplateName, boolean withDoubleClickAction, boolean suppressToolbar, boolean suppressUtilityBar)");
    if (trace != null) trace.args(initialData, contactFilter, emptyTemplateName, backTemplateName);
    if (trace != null) trace.args(withDoubleClickAction);
    if (trace != null) trace.args(suppressToolbar);
    if (trace != null) trace.args(suppressUtilityBar);
    initialize(suppressUtilityBar);
    if (trace != null) trace.exit(ContactTableComponent.class);
  }

  private void initialize(boolean suppressUtilityComponent) {
    JLabel titleLabel = new JMyLabel(com.CH_gui.lang.Lang.rb.getString("compTitle_Contacts"));
    titleLabel.setIcon(Images.get(ImageNums.FLD_CNT_CLOSED16));
    setTitle(titleLabel);
    if (!suppressUtilityComponent)
      addUtilityComponent();
    userListener = new UserListener();
    FetchedDataCache.getSingleInstance().addUserRecordListener(userListener);
  }

  public void addTopContactBuildingPanel() {
    JPanel addAddressPanel = new JPanel(new BorderLayout(0,0));
    Color panelColor = addAddressPanel.getBackground();
    jAddEmail = new JMyTextField();
    jAddEmail.setPreferredSize(new Dimension(100, 18));
    jAddEmail.setBackground(panelColor);
    jAddEmail.setBorder(new LineBorder(panelColor.darker(), 1));
    jAddEmail.setUnfocusedEmptyText(addEmailHint);
    final Action addAction = new AddEmailAddressAction();
    AbstractButton addButton = ActionUtilities.makeSmallComponentToolButton(addAction);
    jAddEmail.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
          addAction.actionPerformed(null);
      }
    });
    addAddressPanel.add(jAddEmail, BorderLayout.CENTER);
    addAddressPanel.add(addButton, BorderLayout.EAST);
    addTopPanel(addAddressPanel);
  }

  /**
   * This call is currently ignored as contacts are only displayed for all folders at once.
   */
  public void initDataModel(Long folderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactTableComponent.class, "initDataModel(Long folderId)");
    if (trace != null) trace.args(folderId);
    //((ContactTableModel) getActionTable().getTableModel()).initData(folderId);
    if (trace != null) trace.exit(ContactTableComponent.class);
  }

  private static int getStatusIndexFromChar(Character ch) {
    int statusIndex = 0;
    if (ch == null || ch.charValue() == 'O') {
      statusIndex = 0;
    } else if (ch.charValue() == 'V' || ch.charValue() == 'A') {
      statusIndex = 1;
    } else if (ch.charValue() == 'N') {
      statusIndex = 2;
    } else if (ch.charValue() == 'D') {
      statusIndex = 3;
    } else if (ch.charValue() == 'I') {
      statusIndex = 4;
    } else {
      statusIndex = 0;
    }
    return statusIndex;
  }

  private void addUtilityComponent() {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    UserRecord userRecord = cache.getUserRecord();

    int myStatusIndex = 4;
    if (userRecord != null)
      myStatusIndex = getStatusIndexFromChar(userRecord.online);

    Object[][] objects = new Object[][] {
      { new JMyLabel("Available", Images.get(ImageNums.STATUS_ONLINE16), JLabel.LEFT), new JMyLabel("", Images.get(ImageNums.STATUS_ONLINE16), JLabel.LEFT), new ChangeStatusAction(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AVAILABLE) },
      { new JMyLabel("Away", Images.get(ImageNums.STATUS_AWAY16), JLabel.LEFT), null, new ChangeStatusAction(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_AWAY) },
      { new JMyLabel("Not Available", Images.get(ImageNums.STATUS_NA16), JLabel.LEFT), new JMyLabel("N/A", Images.get(ImageNums.STATUS_NA16), JLabel.LEFT), new ChangeStatusAction(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_NA) },
      { new JMyLabel("Do Not Disturb", Images.get(ImageNums.STATUS_DND16), JLabel.LEFT), new JMyLabel("DND", Images.get(ImageNums.STATUS_DND16), JLabel.LEFT), new ChangeStatusAction(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED_ONLINE_DND) },
      { new JMyLabel("Invisible", Images.get(ImageNums.STATUS_INVISIBLE16), JLabel.LEFT), null, new ChangeStatusAction(ContactRecord.STATUS_ACCEPTED_ACKNOWLEDGED) },
      { new JMyLabel("Customize..."), null, new CustomizeAction() },
    };
    for (int i=0; i<objects.length; i++) {
      for (int k=0; k<objects[i].length; k++) {
        if (objects[i][k] instanceof JLabel) {
          JLabel label = (JLabel) objects[i][k];
          label.setIconTextGap(0);
        }
      }
    }
    combo = new JMyListCombo(myStatusIndex, objects);
    combo.setBorder(new EmptyBorder(0,0,0,0));

    addUtilityComponent(combo);
  }

  public void setAutoCreateWebAccounts(boolean isAutoCreateWebAccounts) {
    autoCreateWebAccounts = isAutoCreateWebAccounts;
  }

  private class AddEmailAddressAction extends AbstractActionTraced {
    public AddEmailAddressAction() {
      super("Add", Images.get(ImageNums.ADD14));
    }
    public void actionPerformedTraced(ActionEvent event) {
      if (MainFrame.isLoggedIn()) {
        String text = jAddEmail.getText().trim();
        if (text.equalsIgnoreCase(addEmailHint.trim()))
          text = "";
        String[] addresses = EmailRecord.gatherAddresses(text);
        if (addresses != null && addresses.length > 0) {
          InviteByEmailDialog.doInvite(text, "", null, autoCreateWebAccounts);
        } else {
          new FindUserFrame(com.CH_gui.lang.Lang.rb.getString("button_Close"), text);
        }
        if (text.length() > 0)
          jAddEmail.setText("");
      }
    }
  }

  private static class ChangeStatusAction implements ActionListener {
    short status;
    public ChangeStatusAction(short status) {
      this.status = status;
    }
    public void actionPerformed(ActionEvent e) {
      if (MainFrame.isLoggedIn()) {
        ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();
        SIL.submitAndReturn(new MessageAction(CommandCodes.USR_Q_CHANGE_ONLINE_STATUS, new Obj_List_Co(ContactRecord.onlineFlagToChar(status))));
      }
    }
  }

  private class CustomizeAction implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (MainFrame.isLoggedIn()) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.add(new JMyLabel("Change my Available status to Away after a period of inactivity."), new GridBagConstraints(0, 0, 2, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
        panel.add(new JMyLabel("Available status", Images.get(ImageNums.STATUS_ONLINE16), JLabel.LEADING), new GridBagConstraints(0, 1, 2, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 25, 5, 5), 0, 0));
        panel.add(new JMyLabel("Away status", Images.get(ImageNums.STATUS_AWAY16), JLabel.LEADING), new GridBagConstraints(0, 2, 2, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 25, 5, 5), 0, 0));
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        Integer awayMinutes = new Integer(UserSettingsRecord.DEFAULT__AWAY_MINUTES);
        UserSettingsRecord usrSettingsRec = cache.getMyUserSettingsRecord();
        if (usrSettingsRec != null) awayMinutes = usrSettingsRec.awayMinutes;
        final JRadioButton jMinutesLabel = new JMyRadioButton("Set inactivity period in minutes:", awayMinutes.intValue() != 0);
        final JRadioButton jNever = new JMyRadioButton("Do not change my status due to inactivity.", awayMinutes.intValue() == 0);
        ButtonGroup group = new ButtonGroup();
        group.add(jMinutesLabel);
        group.add(jNever);
        final JTextField jMinutes = new JMyTextField(awayMinutes.intValue() > 0 ? awayMinutes.toString() : ""+UserSettingsRecord.DEFAULT__AWAY_MINUTES, 5);
        jMinutes.setEnabled(!jNever.isSelected());
        jMinutesLabel.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            jMinutes.setEnabled(jMinutesLabel.isSelected());
          }
        });
        jNever.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            jMinutes.setEnabled(!jNever.isSelected());
          }
        });
        panel.add(jMinutesLabel, new GridBagConstraints(0, 3, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 3, 5), 0, 0));
        panel.add(jMinutes, new GridBagConstraints(1, 3, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 3, 5), 0, 0));
        panel.add(jNever, new GridBagConstraints(0, 4, 2, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 5, 5), 0, 0));
        final JButton[] buttons = new JButton[2];
        buttons[0] = new JMyButton("OK");
        buttons[1] = new JMyButton("Cancel");
        buttons[0].addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            FetchedDataCache cache = FetchedDataCache.getSingleInstance();
            UserSettingsRecord usrSettingsRec = cache.getMyUserSettingsRecord();
            if (usrSettingsRec == null) {
              usrSettingsRec = new UserSettingsRecord();
              usrSettingsRec.setSymKey(new BASymmetricKey(32));
            }
            boolean success = false;
            try {
              if (jNever.isSelected())
                usrSettingsRec.awayMinutes = new Integer(0);
              else
                usrSettingsRec.awayMinutes = Integer.valueOf(jMinutes.getText().trim());
              success = true;
            } catch (Throwable t) {
              success = false;
            }
            if (success && usrSettingsRec.awayMinutes.intValue() >= 0) {
              usrSettingsRec.setXmlText(usrSettingsRec.makeXMLData());
              usrSettingsRec.seal(cache.getKeyRecordMyCurrent());
              Usr_AltUsrData_Rq request = new Usr_AltUsrData_Rq();
              request.userSettingsRecord = usrSettingsRec;
              MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.USR_Q_ALTER_DATA, request));
              SwingUtilities.windowForComponent(buttons[0]).dispose();
            } else {
              jMinutes.grabFocus();
              jMinutes.selectAll();
              MessageDialog.showDialog(ContactTableComponent.this, "Please enter a valid integer value.", com.CH_gui.lang.Lang.rb.getString("msgTitle_Invalid_Input"), NotificationCenter.ERROR_MESSAGE, false);
            }
          }
        });
        buttons[1].addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            SwingUtilities.windowForComponent(buttons[1]).dispose();
          }
        });
        MessageDialog.showDialog(ContactTableComponent.this, panel, "Status Settings", NotificationCenter.QUESTION_MESSAGE, buttons, null, false, false, false);
      }
    }
  }

  /****************************************************************************************/
  /************* LISTENERS ON CHANGES IN THE CACHE ****************************************/
  /****************************************************************************************/

  /** Listen on updates to the UserRecords in the cache.
    * if the event happens, update my the online status.
    */
  private class UserListener implements UserRecordListener {
    public void userRecordUpdated(UserRecordEvent event) {
      // Exec on event thread since we must preserve selected rows and don't want visuals
      // to seperate from selection for a split second.
      javax.swing.SwingUtilities.invokeLater(new UserGUIUpdater(event));
    }
  }

  private class UserGUIUpdater implements Runnable {
    private UserRecordEvent event;
    public UserGUIUpdater(UserRecordEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserGUIUpdater.class, "UserGUIUpdater(UserRecordEvent event)");
      this.event = event;
      if (trace != null) trace.exit(UserGUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserGUIUpdater.class, "UserGUIUpdater.run()");

      if (event.getEventType() == RecordEvent.SET && combo != null) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        UserRecord userRecord = cache.getUserRecord();
        int myStatusIndex = getStatusIndexFromChar(userRecord != null ? userRecord.online : new Character(UserRecord.ONLINE_INVISIBLE));
        combo.setSelectedIndex(myStatusIndex);
      }

      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(UserGUIUpdater.class);
    }
  }


  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "ContactTableComponent";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }

  public Integer getVisualsVersion() {
    return new Integer(1);
  }

  /**
   * I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
   * Dispose the object and release resources to help in garbage collection.
  */
  public void disposeObj() {
    if (userListener != null) {
      FetchedDataCache.getSingleInstance().removeUserRecordListener(userListener);
      userListener = null;
    }
    super.disposeObj();
  }

}
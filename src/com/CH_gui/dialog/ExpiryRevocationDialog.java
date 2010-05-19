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

package com.CH_gui.dialog;

import com.CH_gui.util.Images;
import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.JMyButton;
import com.CH_gui.gui.MyInsets;
import com.CH_gui.util.GeneralDialog;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;

import com.CH_co.gui.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.frame.*;
import com.CH_gui.gui.*;
import com.CH_guiLib.gui.*;
import com.CH_gui.list.*;
import com.CH_gui.usrs.*;

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
 * <b>$Revision: $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class ExpiryRevocationDialog extends GeneralDialog {

  private static final int DEFAULT_OK_INDEX = 0;
  private static final int DEFAULT_CANCEL_INDEX = 1;

  private JRadioButton jRevokeAccess;
  private JRadioButton jChangeExpiry;
  private JMyCalendarDropdownField jNewExpiry;

  private JButton jOk;

  private MsgLinkRecord[] msgLinks;

  private Vector msgLinksToChangeV;
  private Vector msgLinksOtherV;

  private ServerInterfaceLayer SIL;
  private FetchedDataCache cache;
  private UserRecord myUser;
  private Long myUserId;

  /** Creates new ExpiryRevocationDialog */
  public ExpiryRevocationDialog(Frame owner, MsgLinkRecord[] msgLinks) {
    super(owner, com.CH_gui.lang.Lang.rb.getString("title_Expiry_and_Revocation"));
    constructDialog(owner, msgLinks);
  }
  public ExpiryRevocationDialog(Dialog owner, MsgLinkRecord[] msgLinks) {
    super(owner, com.CH_gui.lang.Lang.rb.getString("title_Expiry_and_Revocation"));
    constructDialog(owner, msgLinks);
  }
  private void constructDialog(Component owner, MsgLinkRecord[] msgLinks) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ExpiryRevocationDialog.class, "constructDialog(Component owner, MsgLinkRecord[] msgLinks)");
    if (trace != null) trace.args(owner, msgLinks);

    //this.parentWindow = owner;
    this.msgLinks = msgLinks;
    this.SIL = MainFrame.getServerInterfaceLayer();
    this.cache = FetchedDataCache.getSingleInstance();

    myUser = cache.getUserRecord();
    myUserId = myUser != null ? myUser.userId : null;

    if (msgLinks != null) {
      msgLinksToChangeV = new Vector();
      msgLinksOtherV = new Vector();
      for (int i=0; i<msgLinks.length; i++) {
        MsgLinkRecord msgLink = msgLinks[i];
        MsgDataRecord msgData = cache.getMsgDataRecord(msgLink.msgId);
        if (msgData != null) {
          if (msgData.senderUserId.equals(myUserId)) {
            msgLinksToChangeV.addElement(msgLink);
          } else {
            msgLinksOtherV.addElement(msgLink);
          }
        }
      }
    }

    createComponents();
    initializeComponents();
    JButton[] buttons = createButtons();
    JComponent mainComponent = createMainComponent();
    init(owner, buttons, mainComponent, DEFAULT_OK_INDEX, DEFAULT_CANCEL_INDEX, false);

    Dimension dim = getSize();
    if (dim.width < 340 || dim.height < 380)
      setSize(Math.max(dim.width, 340), Math.max(dim.height, 380)); // make sure there is enough canvas space to fit popup calendar...
    setVisible(true);
    MiscGui.adjustSizeAndLocationToFitScreen(this); // in Low Res this window maybe too large or move away from windows boundaries due to above setSize

    if (trace != null) trace.exit(ExpiryRevocationDialog.class);
  }


  private JButton[] createButtons() {
    JButton[] buttons = new JButton[2];

    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
    buttons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedOK();
      }
    });
    jOk = buttons[0];

    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedCancel();
      }
    });

    if (msgLinksToChangeV == null || msgLinksToChangeV.size() == 0) {
      jOk.setEnabled(false);
    }

    return buttons;
  }


  private void pressedOK() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ExpiryRevocationDialog.class, "pressedOK()");
    closeDialog();

    Thread th = new ThreadTraced("Change Expiry or Revocation - sending...") {
      public void runTraced() {
        MsgLinkRecord[] msgLinksToChange = (MsgLinkRecord[]) ArrayUtils.toArray(msgLinksToChangeV, MsgLinkRecord.class);
        MsgDataRecord[] msgDatas = cache.getMsgDataRecords(MsgLinkRecord.getMsgIDs(msgLinksToChange));
        Long[] msgIDs = RecordUtils.getIDs(msgDatas);
        Timestamp dateExpired = jRevokeAccess.isSelected() ? new Timestamp(System.currentTimeMillis()) : (jNewExpiry.getDate() == null ? null : new Timestamp(jNewExpiry.getDate().getTime()));
        Boolean isRevoked = Boolean.valueOf(jRevokeAccess.isSelected());
        SIL.submitAndReturn(new MessageAction(CommandCodes.MSG_Q_EXPIRY, new Obj_List_Co(new Object[] { msgIDs, dateExpired, isRevoked })));
      }
    };
    th.setDaemon(true);
    th.start();

    if (trace != null) trace.exit(ExpiryRevocationDialog.class);
  }


  private void pressedCancel() {
    closeDialog();
  }


  private void createComponents() {
    jRevokeAccess = new JMyRadioButton("Revoke Access");
    jChangeExpiry = new JMyRadioButton("Change Expiry Date");
    //jNewExpiry = new JMyCalendarDropdownField(DateFormat.MEDIUM, 3, 1);
    jNewExpiry = new JMyCalendarDropdownField(DateFormat.MEDIUM, 3, 1, true, null,
          new String[] { "Never", "Tomorrow", "One Week", "Two Weeks", "One Month", "Custom..." },
          new int[][] { { 0, -1 },
                        { Calendar.DAY_OF_MONTH, 1 },
                        { Calendar.WEEK_OF_YEAR, 1 },
                        { Calendar.WEEK_OF_YEAR, 2 },
                        { Calendar.MONTH, 1 },
                        { 0, -2 } }, true);

    // set min size of calendar fields
    jNewExpiry.setMinimumSize(jNewExpiry.getPreferredSize());

    ButtonGroup bg = new ButtonGroup();
    bg.add(jRevokeAccess);
    bg.add(jChangeExpiry);
    jRevokeAccess.setSelected(true);
    jChangeExpiry.setSelected(false);
    jNewExpiry.setEnabled(false);
  }

  private void initializeComponents() {
    jRevokeAccess.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jNewExpiry.setEnabled(false);
      }
    });
    jChangeExpiry.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jNewExpiry.setEnabled(true);
      }
    });
  }

  private JComponent createMainComponent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ExpiryRevocationDialog.class, "createMainComponent()");
    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));

    panel.setLayout(new GridBagLayout());

    int posY = 0;
    panel.add(AccountOptionsSignaturesPanel.makeDivider("Dates"), new GridBagConstraints(0, posY, 3, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(Images.get(ImageNums.AUTO_RESPONDER32)), new GridBagConstraints(0, posY, 1, 4, 0, 0,
        GridBagConstraints.NORTH, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 2), 0, 0));
    panel.add(new JMyLabel("I would like to..."), new GridBagConstraints(1, posY, 2, 1, 20, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 2, 2, 5), 0, 0));
    panel.add(jRevokeAccess, new GridBagConstraints(1, posY+1, 2, 1, 20, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 5), 0, 0));
    panel.add(jChangeExpiry, new GridBagConstraints(1, posY+2, 2, 1, 20, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 2, 2, 5), 0, 0));
    JPanel datePanel = new JPanel();
    datePanel.setBorder(new EmptyBorder(0, 0, 0, 0));
    datePanel.setLayout(new BorderLayout(10, 0));
    datePanel.add(new JMyLabel("New Expiry:"), BorderLayout.WEST);
    datePanel.add(jNewExpiry, BorderLayout.CENTER);
    panel.add(datePanel, new GridBagConstraints(1, posY+3, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 2, 5, 5), 0, 0));
    posY += 4;

    panel.add(AccountOptionsSignaturesPanel.makeDivider("Selected Object(s)"), new GridBagConstraints(0, posY, 3, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    if (msgLinksToChangeV != null && msgLinksToChangeV.size() > 0) {
      JPanel panelToChange = new JPanel();
      JScrollPane scrollToChange = new JScrollPane(panelToChange);
      panelToChange.setBorder(new EmptyBorder(5, 5, 5, 5));
      panelToChange.setLayout(new GridBagLayout());
      for (int i=0; i<msgLinksToChangeV.size(); i++) {
        MsgLinkRecord link = (MsgLinkRecord) msgLinksToChangeV.elementAt(i);
        MsgDataRecord data = cache.getMsgDataRecord(link.msgId);
        if (data != null) {
          JLabel label = new JMyLabel();
          label.setIcon(ListRenderer.getRenderedIcon(data));
          label.setText(ListRenderer.getRenderedText(data));
          panelToChange.add(label, new GridBagConstraints(0, i*2, 2, 1, 10, 0,
              GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 2, 5), 0, 0));
          JLabel expiry = new JMyLabel();
          ImageText exp = data.getExpirationIconAndText(myUserId, false);
          expiry.setIcon(Images.get(exp));
          expiry.setText(exp.getText());
          JLabel expiryLabel = new JMyLabel("Currently Expires:");
          expiryLabel.setIcon(Images.get(ImageNums.TRANSPARENT16));
          panelToChange.add(expiryLabel, new GridBagConstraints(0, i*2+1, 1, 1, 0, 0,
              GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 5, 5, 2), 0, 0));
          panelToChange.add(expiry, new GridBagConstraints(1, i*2+1, 1, 1, 10, 0,
              GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 2, 5, 5), 0, 0));
        }
      }
      // filler
      panelToChange.add(new JMyLabel(), new GridBagConstraints(0, msgLinksToChangeV.size()*2, 2, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));

      panel.add(scrollToChange, new GridBagConstraints(0, posY, 3, 1, 10, 1+msgLinksToChangeV.size(),
          GridBagConstraints.NORTH, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;
    }

    if (msgLinksOtherV != null && msgLinksOtherV.size() > 0) {
      panel.add(new JMyLabel("Warning: The following objects' access cannot be changed by You"), new GridBagConstraints(0, posY, 3, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 2, 5), 0, 0));
      posY ++;
      panel.add(AccountOptionsSignaturesPanel.makeDivider("because You are not their originator."), new GridBagConstraints(0, posY, 3, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 5, 5, 5), 0, 0));
      posY ++;

      JPanel panelOther = new JPanel();
      JScrollPane scrollOther = new JScrollPane(panelOther);
      panelOther.setBorder(new EmptyBorder(5, 5, 5, 5));
      panelOther.setLayout(new GridBagLayout());
      for (int i=0; i<msgLinksOtherV.size(); i++) {
        MsgLinkRecord link = (MsgLinkRecord) msgLinksOtherV.elementAt(i);
        MsgDataRecord data = cache.getMsgDataRecord(link.msgId);
        if (data != null) {
          JLabel label = new JMyLabel();
          label.setIcon(ListRenderer.getRenderedIcon(data));
          label.setText(ListRenderer.getRenderedText(data));
          panelOther.add(label, new GridBagConstraints(0, i*2, 2, 1, 10, 0,
              GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 2, 5), 0, 0));
          JLabel expiry = new JMyLabel();
          ImageText exp = data.getExpirationIconAndText(myUserId, false);
          expiry.setIcon(Images.get(exp));
          expiry.setText(exp.getText());
          panelOther.add(new JMyLabel("Currently Expires:"), new GridBagConstraints(0, i*2+1, 1, 1, 0, 0,
              GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 25, 5, 2), 0, 0));
          panelOther.add(expiry, new GridBagConstraints(1, i*2+1, 1, 1, 10, 0,
              GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(2, 2, 5, 5), 0, 0));
        }
      }
      // filler
      panelOther.add(new JMyLabel(), new GridBagConstraints(0, msgLinksOtherV.size()*2, 2, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));

      panel.add(scrollOther, new GridBagConstraints(0, posY, 3, 1, 10, 1+msgLinksOtherV.size(),
          GridBagConstraints.NORTH, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;
    }

//    // filler
//    panel.add(new JMyLabel(), new GridBagConstraints(0, posY, 3, 1, 10, 10, 
//          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
//    posY ++;

    if (trace != null) trace.exit(ExpiryRevocationDialog.class, panel);
    return panel;
  }

}
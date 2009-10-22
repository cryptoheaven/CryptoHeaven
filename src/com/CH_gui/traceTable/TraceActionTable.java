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

package com.CH_gui.traceTable;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;

import com.CH_gui.action.*;
import com.CH_gui.dialog.*;
import com.CH_gui.fileTable.*;
import com.CH_gui.frame.*;
import com.CH_gui.list.*;
import com.CH_gui.msgTable.*;
import com.CH_gui.sortedTable.*;
import com.CH_gui.table.*;

import com.CH_cl.service.cache.*;
import com.CH_cl.service.engine.*;

import com.CH_co.service.records.*;
import com.CH_co.service.msg.dataSets.*;
import com.CH_co.service.msg.*;
import com.CH_co.util.*;
import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.24 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class TraceActionTable extends RecordActionTable implements ActionProducerI {

  private Action[] actions;

  private static final int REFRESH_ACTION = 0;
  private static final int INITIATE_ACTION = 1;
  private static final int MESSAGE_ACTION = 2;


  private int leadingActionId = Actions.LEADING_ACTION_ID_STAT_ACTION_TABLE;
  private ServerInterfaceLayer serverInterfaceLayer;


  /** Creates new TraceActionTable
   */
  public TraceActionTable(Record[] parentObjLinks) {
    super(new TraceTableModel(parentObjLinks));
    initialize();
  }
  private void initialize() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TraceActionTable.class, "initialize()");
    serverInterfaceLayer = MainFrame.getServerInterfaceLayer();
    initActions();
    if (trace != null) trace.exit(TraceActionTable.class);
  }


  public DragGestureListener createDragGestureListener() {
    return null;
  }
  public DropTargetListener createDropTargetListener() {
    return null;
  }


  private void initActions() {
    actions = new Action[3];
    actions[REFRESH_ACTION] = new RefreshAction(leadingActionId + REFRESH_ACTION);
    actions[INITIATE_ACTION] = new InitiateAction(leadingActionId + INITIATE_ACTION);
    actions[MESSAGE_ACTION] = new SendMessageAction(leadingActionId + MESSAGE_ACTION);
    setEnabledActions();
  }
  public Action getRefreshAction() {
    return actions[REFRESH_ACTION];
  }
  public Action getInitiateAction() {
    return actions[INITIATE_ACTION];
  }
  public Action getMessageAction() {
    return actions[MESSAGE_ACTION];
  }


  // =====================================================================
  // LISTENERS FOR THE MENU ITEMS        
  // =====================================================================

  /**
   * Refresh Trace List.
   */
  private class RefreshAction extends AbstractAction {
    public RefreshAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Refresh_Traces"), Images.get(ImageNums.REFRESH16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Refresh_Trace_List_from_the_server."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.REFRESH24));
    }
    public void actionPerformed(ActionEvent event) {
      TraceTableModel tableModel = (TraceTableModel) getTableModel();
      tableModel.refreshData();
    }
  }


  /** 
   * Initiate a new contact.
   */
  private class InitiateAction extends AbstractAction {
    public InitiateAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Add_to_Contact_List_..."), Images.get(ImageNums.CONTACT_ADD16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Add_User_to_your_Contact_List."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.CONTACT_ADD24));
    }
    public void actionPerformed(ActionEvent event) {
      TraceRecord tRec = (TraceRecord) getSelectedRecord();
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      UserRecord uRec = cache.getUserRecord(tRec.ownerUserId);
      if (uRec != null) {
        Window w = SwingUtilities.windowForComponent(TraceActionTable.this);
        if (w instanceof Frame)
          new InitiateContactDialog((Frame) w, new Long[] { uRec.userId });
        else if (w instanceof Dialog)
          new InitiateContactDialog((Dialog) w, new Long[] { uRec.userId });
      }
    }
  }

  /** 
   * Message a user.
   */
  private class SendMessageAction extends AbstractAction {
    public SendMessageAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Send_Message_..."), Images.get(ImageNums.MAIL_COMPOSE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_New_Message_to_the_selected_user(s)."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.MAIL_COMPOSE24));
    }
    public void actionPerformed(ActionEvent event) {
      TraceRecord[] traceRecs = (TraceRecord[]) getSelectedRecords();
      UserRecord[] uRecs = getUserRecords(traceRecs);
      if (uRecs != null && uRecs.length > 0) {
        String subject = null;
        Hashtable usedRecsHT = new Hashtable();
        if (traceRecs.length == 1) {
          usedRecsHT.put(traceRecs[0].objId, ((TraceTableModel) TraceActionTable.this.getTableModel()).getTracedObjRecord(traceRecs[0].objId));
        } else {
          for (int i=0; i<traceRecs.length; i++) {
            Long objId = traceRecs[i].objId;
            Record rec = ((TraceTableModel) TraceActionTable.this.getTableModel()).getTracedObjRecord(objId);
            if (!usedRecsHT.containsKey(objId))
              usedRecsHT.put(objId, rec);
          }
        }
        if (usedRecsHT.size() > 1) {
          StringBuffer sb = new StringBuffer();
          Enumeration enm = usedRecsHT.keys();
          while (enm.hasMoreElements()) {
            Long objId = (Long) enm.nextElement();
            Record rec = (Record) usedRecsHT.get(objId);
            sb.append('"');
            sb.append(ListRenderer.getRenderedText(rec));
            sb.append("\" (");
            sb.append(objId);
            sb.append(')');
            if (enm.hasMoreElements())
              sb.append(", ");
          }
          subject = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("msgSubject_Access_Trace_for"), new Object[] {sb.toString()});
        } else {
          Long objId = traceRecs[0].objId;
          Record rec = (Record) usedRecsHT.get(objId);
          boolean isFile = rec instanceof FileLinkRecord;
          boolean isMsg = rec instanceof MsgLinkRecord;
          String objType = "";
          if (isFile) {
            objType = com.CH_gui.lang.Lang.rb.getString("File");
          } else if (isMsg) {
            FetchedDataCache cache = FetchedDataCache.getSingleInstance();
            MsgDataRecord msgData = cache.getMsgDataRecord(((MsgLinkRecord) rec).msgId);
            if (msgData != null && msgData.isTypeAddress())
              objType = com.CH_gui.lang.Lang.rb.getString("Address");
            else 
              objType = com.CH_gui.lang.Lang.rb.getString("Message");
          } else {
            objType = com.CH_gui.lang.Lang.rb.getString("Folder");
          }
          subject = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("msgSubject_Access_Trace_for_OBJECT-TYPE_-_OBJECT-NAME___(id_OBJECT-ID)"), new Object[] {objType, '"'+ListRenderer.getRenderedText(rec)+'"', objId});
        }
        new MessageFrame(uRecs, subject);
      }
    }
  }


  private UserRecord[] getUserRecords(TraceRecord[] traceRecords) {
    UserRecord[] userRecords = null;
    Vector userRecsV = new Vector();
    if (traceRecords != null && traceRecords.length > 0) {
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      for (int i=0; i<traceRecords.length; i++) {
        UserRecord uRec = cache.getUserRecord(traceRecords[i].ownerUserId);
        if (uRec != null && !userRecsV.contains(uRec))
          userRecsV.addElement(uRec);
      }
      if (userRecsV.size() > 0) {
        userRecords = new UserRecord[userRecsV.size()];
        userRecsV.toArray(userRecords);
      }
    }
    return userRecords;
  }

  /****************************************************************************/
  /*        A c t i o n P r o d u c e r I                                  
  /****************************************************************************/

  /** @return all the acitons that this objects produces.
   */
  public Action[] getActions() {
    return actions;
  }

  /**
   * Enables or Disables actions based on the current state of the Action Producing component.
   */
  public void setEnabledActions() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TraceActionTable.class, "setEnabledActions()");
    actions[REFRESH_ACTION].setEnabled(true);

    int count = 0;
    boolean messageOk = true;
    boolean initiateOk = true;

    TraceRecord[] selectedTraceRecords = (TraceRecord[]) getSelectedRecords();
    UserRecord[] selectedUserRecords = getUserRecords(selectedTraceRecords);
    if (selectedUserRecords != null) {

      count = selectedUserRecords.length;

      FetchedDataCache cache = serverInterfaceLayer.getFetchedDataCache();
      Long userId = cache.getMyUserId();

      for (int i=0; i<selectedUserRecords.length; i++) {
        UserRecord uRec = selectedUserRecords[i];
        ContactRecord cRec = cache.getContactRecordOwnerWith(userId, uRec.userId);

        if (cRec != null || uRec.userId.equals(userId)) {
          initiateOk = false;
        }

        // If user does not want spam...
        if (((uRec.acceptingSpam.shortValue() & UserRecord.ACC_SPAM_YES_INTER)) == 0) {
          // If we don't have an active contact, then we can't message
          if (cRec == null || !cRec.isOfActiveType())
            messageOk = false;
        }
      }
    }

    try {
      if (count == 0) {
        actions[INITIATE_ACTION].setEnabled(false); // This list sometimes throws NullPointerException -- weird!!! why??
        actions[MESSAGE_ACTION].setEnabled(false);
      } else if (count == 1) {
        actions[INITIATE_ACTION].setEnabled(initiateOk);
        actions[MESSAGE_ACTION].setEnabled(messageOk);
      } else {
        actions[INITIATE_ACTION].setEnabled(false);
        actions[MESSAGE_ACTION].setEnabled(messageOk);
      }
    } catch (NullPointerException e) {
    }
    if (trace != null) trace.exit(TraceActionTable.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "TraceActionTable";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
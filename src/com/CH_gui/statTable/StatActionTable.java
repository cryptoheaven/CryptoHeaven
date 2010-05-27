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

package com.CH_gui.statTable;

import com.CH_co.service.records.*;
import com.CH_co.util.*;
import com.CH_co.trace.Trace;

import com.CH_gui.action.*;
import com.CH_gui.table.*;
import com.CH_gui.util.*;

import java.awt.event.*;
import java.awt.dnd.*;
import javax.swing.*;
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
 * <b>$Revision: 1.14 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class StatActionTable extends RecordActionTable implements ActionProducerI {

  private Action[] actions;

  private static final int REFRESH_ACTION = 0;

  private int leadingActionId = Actions.LEADING_ACTION_ID_STAT_ACTION_TABLE;


  /** Creates new StatActionTable
   */
  public StatActionTable(Record parentObjLink) {
    super(new StatTableModel(parentObjLink));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StatActionTable.class, "StatActionTable(Record parentObjLink)");
    if (trace != null) trace.args(parentObjLink);
    initActions();
    if (trace != null) trace.exit(StatActionTable.class);
  }


  public DragGestureListener createDragGestureListener() {
    return null;
  }
  public DropTargetListener createDropTargetListener() {
    return null;
  }


  private void initActions() {
    actions = new Action[1];
    actions[REFRESH_ACTION] = new RefreshAction(leadingActionId + REFRESH_ACTION);
    setEnabledActions();
  }
  public Action getRefreshAction() {
    return actions[REFRESH_ACTION];
  }


  // =====================================================================
  // LISTENERS FOR THE MENU ITEMS        
  // =====================================================================

  /**
   * Refresh Stat List.
   */
  private class RefreshAction extends AbstractActionTraced {
    public RefreshAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Refresh_Stats"), Images.get(ImageNums.REFRESH16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Refresh_Stat_List_from_the_server."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.REFRESH24));
    }
    public void actionPerformedTraced(ActionEvent event) {
      StatTableModel tableModel = (StatTableModel) getTableModel();
      tableModel.refreshData();
    }
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
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StatActionTable.class, "setEnabledActions()");
    actions[REFRESH_ACTION].setEnabled(true);
    if (trace != null) trace.exit(StatActionTable.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "StatActionTable";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
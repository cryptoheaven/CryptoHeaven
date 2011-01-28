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

package com.CH_gui.chatTable;

import com.CH_co.trace.Trace;

import com.CH_gui.gui.*;
import com.CH_gui.msgTable.*;
import com.CH_gui.table.*;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.SwingUtilities;

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
 * <b>$Revision: 1.11 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class ChatTableComponent extends RecordTableComponent {

  /** Creates new ChatTableComponent */
  public ChatTableComponent(boolean suppressToolbar, boolean suppressUtilityBar, boolean suppressVisualsSavable) {
    super(new ChatActionTable(new MsgTableModel(null, MsgTableModel.MODE_CHAT)), Template.get(Template.EMPTY_CHAT), Template.get(Template.NONE), Template.get(Template.CATEGORY_CHAT), suppressToolbar, suppressUtilityBar, suppressVisualsSavable);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ChatTableComponent.class, "ChatTableComponent()");
    getRecordTableScrollPane().getJSortedTable().setRowMargin(0);
    addHierarchyListener(new HierarchyListener() {
      public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
          removeHierarchyListener(this);
          SwingUtilities.getWindowAncestor(ChatTableComponent.this).addWindowListener(new WindowAdapter() {
            boolean reloadPending = true;
            public void windowActivated(WindowEvent e) {
              if (reloadPending) {
                reloadPending = false;
                Long folderId = ((MsgTableModel) getActionTable().getTableModel()).getParentFolderPair().getId();
                ((MsgTableModel) getActionTable().getTableModel()).initData(folderId);
              }
            }
            public void windowDeactivated(WindowEvent e) {
            }
            public void windowIconified(WindowEvent e) {
              reloadPending = true;
            }
          });
        }
      }
    });
    if (trace != null) trace.exit(ChatTableComponent.class);
  }

  public void initDataModel(Long folderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ChatTableComponent.class, "initDataModel(Long folderId)");
    if (trace != null) trace.args(folderId);
    ((MsgTableModel) getActionTable().getTableModel()).initData(folderId);
    if (trace != null) trace.exit(ChatTableComponent.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "ChatTableComponent";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
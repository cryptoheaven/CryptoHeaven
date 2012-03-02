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

package com.CH_gui.statTable;

import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.*;

import com.CH_gui.table.*;

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
 * <b>$Revision: 1.15 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class StatTableComponent extends RecordTableComponent {

  /** Creates new StatTableComponent */
  public StatTableComponent(Record parentObjLink) {
    super(new StatActionTable(parentObjLink));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StatTableComponent.class, "StatTableComponent(Record parentObjLink)");
    if (trace != null) trace.args(parentObjLink);
    changeTitle(parentObjLink.getId());
    if (trace != null) trace.exit(StatTableComponent.class);
  }

  /**
   * This call is currently ignored.
   */
  public void initDataModel(Long parentObjLinkId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StatTableComponent.class, "initDataModel(Long parentObjLinkId)");
    if (trace != null) trace.args(parentObjLinkId);
    ((StatTableModel) getActionTable().getTableModel()).initData(parentObjLinkId);
    changeTitle(parentObjLinkId);
    if (trace != null) trace.exit(StatTableComponent.class);
  }

  private void changeTitle(Long parentObjLinkId) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    FileLinkRecord fileLink = cache.getFileLinkRecord(parentObjLinkId);
    if (fileLink != null) {
      setTitle(java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("title_Access_History_for_file_ID_FILEID"), new Object[] {fileLink.fileId}));
    } else {
      MsgLinkRecord msgLink = cache.getMsgLinkRecord(parentObjLinkId);
      if (msgLink != null) {
        MsgDataRecord msgData = cache.getMsgDataRecord(msgLink.msgId);
        if (msgData.isTypeMessage())
          setTitle(java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("title_Access_History_for_message_ID_MSGID"), new Object[] {msgLink.msgId}));
        else if (msgData.isTypeAddress())
          setTitle(java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("title_Access_History_for_address_ID_ADDRID"), new Object[] {msgLink.msgId}));
      }
    }
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "StatTableComponent";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
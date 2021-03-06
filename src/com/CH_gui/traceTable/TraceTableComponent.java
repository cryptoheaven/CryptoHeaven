/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.traceTable;

import com.CH_cl.service.cache.CacheMsgUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_gui.gui.Template;
import com.CH_gui.table.RecordTableComponent;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.17 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class TraceTableComponent extends RecordTableComponent {


  /** Creates new TraceTableComponent */
  public TraceTableComponent(Record[] parentObjLinks) {
    super(parentObjLinks != null && (parentObjLinks.length > 1 || CacheMsgUtils.hasAttachments(FetchedDataCache.getSingleInstance(), parentObjLinks[0])) ?  new TraceActionMultiTable(parentObjLinks) : new TraceActionTable(parentObjLinks), Template.get(Template.NONE));
    initialize(parentObjLinks);
  }
  private void initialize(Record[] parentObjLinks) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TraceTableComponent.class, "initialize(Record[] parentObjLinks)");
    if (trace != null) trace.args(parentObjLinks);
    changeTitle(parentObjLinks);
    if (trace != null) trace.exit(TraceTableComponent.class);
  }

  /**
   * This call is currently ignored.
   */
  public void initDataModel(Long parentObjLinkId) {
    /*
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TraceTableComponent.class, "initDataModel(Long parentObjLinkId)");
    if (trace != null) trace.args(parentObjLinkId);
    ((TraceTableModel) getActionTable().getTableModel()).initData(parentObjLinkId);
    changeTitle(parentObjLinkId);
    if (trace != null) trace.exit(TraceTableComponent.class);
     */
  }

  private void changeTitle(Record[] parentObjLinks) {
    if (parentObjLinks != null) {
      if (parentObjLinks.length == 1) {
        Record rec = parentObjLinks[0];
        if (rec instanceof FileLinkRecord) {
          setTitle(java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("title_Access_History_for_file_ID_FILEID"), new Object[] {((FileLinkRecord)rec).fileId}));
        } else if (rec instanceof MsgLinkRecord) {
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          MsgDataRecord msgData = cache.getMsgDataRecord(((MsgLinkRecord) rec).msgId);
          if (msgData != null && msgData.isTypeAddress())
            setTitle(java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("title_Access_History_for_address_ID_ADDRID"), new Object[] {((MsgLinkRecord)rec).msgId}));
          else
            setTitle(java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("title_Access_History_for_message_ID_MSGID"), new Object[] {((MsgLinkRecord)rec).msgId}));
        } else if (rec instanceof FolderPair) {
          setTitle(java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("title_Access_History_for_folder_ID_FOLDERID"), new Object[] {((FolderPair)rec).getId()}));
        }
      } else if (parentObjLinks.length >= 2) {
        setTitle(com.CH_cl.lang.Lang.rb.getString("title_Access_History_for_multiple_objects."));
      }
    }
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "TraceTableComponent";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
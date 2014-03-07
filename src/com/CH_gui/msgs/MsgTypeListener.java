/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.msgs;

import com.CH_cl.service.cache.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;

import com.CH_gui.frame.MainFrame;

import javax.swing.event.*;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.7 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class MsgTypeListener extends Object implements DocumentListener {

  private MsgTypeManagerI msgTypeManager;
  private boolean withTypeNotify;


  /** Creates new MsgTypeListener */
  public MsgTypeListener(MsgTypeManagerI msgTypeManager, boolean withTypeNotify) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgTypeListener.class, "MsgTypeListener(MsgTypeManagerI msgTypeManager, boolean withTypeNotify)");
    if (trace != null) trace.args(msgTypeManager);
    if (trace != null) trace.args(withTypeNotify);
    this.msgTypeManager = msgTypeManager;
    this.withTypeNotify = withTypeNotify;
    if (trace != null) trace.exit(MsgTypeListener.class);
  }

  private long lastNotify;
  /**
   * Gives notification that an attribute or set of attributes changed. 
   */
  public void changedUpdate(DocumentEvent e) {
    docChanged(false);
  }
  /**
   * Gives notification that there was an insert into the document. 
   */
  public void insertUpdate(DocumentEvent e) {
    docChanged(true);
  }
  /**
   * Gives notification that a portion of the document has been removed. 
   */
  public void removeUpdate(DocumentEvent e) {
    docChanged(false);
  }
  /**
   * Private helper to trigger action when document is changed.
   */
  private void docChanged(boolean isInsert) {
    msgTypeManager.typeSourceUpdated(isInsert);
    if (withTypeNotify) {
      typeNotify(isInsert);
    }
  }
  private void typeNotify(boolean isInsert) {
    // Send notification that user is typing.
    long time = 0;
    if ((time = System.currentTimeMillis()) > lastNotify + 9000 && msgTypeManager.isTypeNotifyEnabled(isInsert)) {
      lastNotify = time;
      Obj_List_Co dataSet = new Obj_List_Co();
      Long folderId = msgTypeManager.getTypeNotifyFolderId();
      if (folderId != null) {
        Long[] shareIDs = RecordUtils.getIDs(FetchedDataCache.getSingleInstance().getFolderShareRecordsForFolder(folderId));
        if (shareIDs != null && shareIDs.length > 0) {
          dataSet.objs = new Object[] { folderId, shareIDs };
          MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.MSG_Q_TYPING, dataSet), 15000);
        }
      }
    }
  } // end typeNotify()

}
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

package com.CH_gui.msgTable;

import java.awt.dnd.*;
import java.awt.datatransfer.*;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.*;

import com.CH_gui.addressBook.*;

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
 * <b>$Revision: 1.10 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class MsgDND_DragGestureListener extends Object implements DragGestureListener {

  private MsgActionTable msgActionTable;

  /** Creates new MsgDND_DragGestureListener */
  public MsgDND_DragGestureListener(MsgActionTable msgActionTable) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgDND_DragGestureListener.class, "MsgDND_DragGestureListener()");
    this.msgActionTable = msgActionTable;
    if (trace != null) trace.exit(MsgDND_DragGestureListener.class);
  }

  public void dragGestureRecognized(DragGestureEvent event) {
    //System.out.println("MsgDragGestureListener.dragGestureRecognized");
    MsgLinkRecord[] mLinks = (MsgLinkRecord[]) msgActionTable.getSelectedRecords();
    if (mLinks != null && mLinks.length > 0) {
      Transferable transferable = null;
      if (msgActionTable.getTableModel().getParentFolderPair().getFolderRecord().isAddressType())
        transferable = new AddrDND_Transferable(mLinks);
      else
        transferable = new MsgDND_Transferable(mLinks);
      // as the name suggests, starts the dragging
      event.getDragSource().startDrag(event, null, transferable, new MsgDND_DragSourceListener());
    } else {
      //System.out.println( "nothing was selected");   
    }
  }
} // end class MsgDND_DragGestureListener
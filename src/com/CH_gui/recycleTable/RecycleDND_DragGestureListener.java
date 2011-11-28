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

package com.CH_gui.recycleTable;

import java.awt.dnd.*;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.*;

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
 * <b>$Revision: 1.1 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class RecycleDND_DragGestureListener extends Object implements DragGestureListener {

  private RecycleActionTable recycleActionTable;

  /** Creates new RecycleDND_DragGestureListener */
  protected RecycleDND_DragGestureListener(RecycleActionTable recycleActionTable) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleDND_DragGestureListener.class, "RecycleDND_DragGestureListener(RecycleActionTable recycleActionTable)");
    if (trace != null) trace.args(recycleActionTable);
    this.recycleActionTable = recycleActionTable;
    if (trace != null) trace.exit(RecycleDND_DragGestureListener.class);
  }

  public void dragGestureRecognized(DragGestureEvent event) {
    FolderPair[] fPairs = (FolderPair[]) recycleActionTable.getSelectedInstancesOf(FolderPair.class);
    FileLinkRecord[] fLinks = (FileLinkRecord[]) recycleActionTable.getSelectedInstancesOf(FileLinkRecord.class);
    MsgLinkRecord[] mLinks = (MsgLinkRecord[]) recycleActionTable.getSelectedInstancesOf(MsgLinkRecord.class);
    if ((fPairs != null && fPairs.length > 0) ||
        (fLinks != null && fLinks.length > 0) ||
        (mLinks != null && mLinks.length > 0))
    {
      FileLinkRecord[] fLinkAllVersions = null;
      if (recycleActionTable.getTableModel().getIsCollapseFileVersions())
        fLinkAllVersions = (FileLinkRecord[]) recycleActionTable.getSelectedInstancesOf(FileLinkRecord.class, true);
      RecycleDND_Transferable transferable = new RecycleDND_Transferable(fPairs, fLinks, fLinkAllVersions, mLinks);
      // as the name suggests, starts the dragging
      event.getDragSource().startDrag(event, null, transferable, new RecycleDND_DragSourceListener());
    } else {
      //System.out.println( "nothing was selected");   
    }
  }
} // end class FileDND_DragGestureListener
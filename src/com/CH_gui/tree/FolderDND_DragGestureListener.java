/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.tree;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.*;

import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.10 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class FolderDND_DragGestureListener extends Object implements DragGestureListener {

  private FolderTree tree;

  /** Creates new FolderDND_DragGestureListener */
  public FolderDND_DragGestureListener(FolderTree tree) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderDND_DragGestureListener.class, "FolderDND_DragGestureListener(FolderTree tree)");
    if (trace != null) trace.args(tree);
    this.tree = tree;
    if (trace != null) trace.exit(FolderDND_DragGestureListener.class);
  }

  public void dragGestureRecognized(DragGestureEvent event) {
    FolderPair[] fPairs = tree.getLastPathComponentFolderPairs(tree.getSelectionPaths());
    Long[] folderIDs = RecordUtils.getIDs(fPairs);
    if (folderIDs != null && folderIDs.length > 0) {
      FolderDND_Transferable transferable = new FolderDND_Transferable(folderIDs);
      // as the name suggests, starts the dragging
      event.getDragSource().startDrag(event, null, transferable, new FolderDND_DragSourceListener());
    } else {
      //System.out.println( "nothing was selected");   
    }
  }

}
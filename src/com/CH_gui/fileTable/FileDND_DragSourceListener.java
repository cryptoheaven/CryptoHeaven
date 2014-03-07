/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.fileTable;

import com.CH_co.trace.Trace;

import java.awt.dnd.*;
import java.awt.Point;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.10 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class FileDND_DragSourceListener extends Object implements DragSourceListener {

  private Point lastPt;

  /** Creates new FileDND_DragSourceListener */
  public FileDND_DragSourceListener() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileDND_DragSourceListener.class, "FileDND_DragSourceListener()");
    if (trace != null) trace.exit(FileDND_DragSourceListener.class);
  }

  /**
   * this message goes to DragSourceListener, informing it that the dragging
   * has ended
   *
   */
  public void dragDropEnd (DragSourceDropEvent event) {
    //System.out.println("FileDND_DragSourceListener.dragDropEnd " + Thread.currentThread() + " event = " + event);
  }

  /**
   * this message goes to DragSourceListener, informing it that the dragging
   * has entered the DropSite
   *
   */
  public void dragEnter (DragSourceDragEvent event) {
    //System.out.println("FileDND_DragSourceListener.dragEnter " + Thread.currentThread() + " event = " + event);
  }

  /**
   * this message goes to DragSourceListener, informing it that the dragging
   * has exited the DropSite
   *
   */
  public void dragExit (DragSourceEvent event) {
    //System.out.println("FileDND_DragSourceListener.dragExit " + Thread.currentThread() + " event = " + event);
    event.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
  }

  /**
   * this message goes to DragSourceListener, informing it that the dragging is currently
   * ocurring over the DropSite
   *
   */
  public void dragOver (DragSourceDragEvent event) {
    Point pt = event.getLocation();
    if (lastPt == null || lastPt.x != pt.x || lastPt.y != pt.y) {
      lastPt = pt;
      //System.out.println("FileDND_DragSourceListener.dragOver " + Thread.currentThread() + " event = " + event);
      int action = event.getTargetActions();
      DragSourceContext dsc = event.getDragSourceContext();
      if (action == DnDConstants.ACTION_MOVE)
        dsc.setCursor(DragSource.DefaultMoveDrop);
      else if (action == DnDConstants.ACTION_COPY)
        dsc.setCursor(DragSource.DefaultCopyDrop);
    }
  }

  /**
   * is invoked when the user changes the dropAction
   *
   */
  public void dropActionChanged ( DragSourceDragEvent event) {
    //System.out.println("FileDND_DragSourceListener.dropActionChanged " + Thread.currentThread() + " event = " + event);
  }

} // end class FileDND_DragSourceListener
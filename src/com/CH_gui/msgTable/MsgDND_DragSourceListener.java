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

package com.CH_gui.msgTable;

import com.CH_co.trace.Trace;

import java.awt.dnd.*;
import java.awt.Point;

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
 * <b>$Revision: 1.10 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class MsgDND_DragSourceListener extends Object implements DragSourceListener {

  private Point lastPt;

  /** Creates new MsgDND_DragSourceListener */
  public MsgDND_DragSourceListener() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgDND_DragSourceListener.class, "MsgDND_DragSourceListener()");
    if (trace != null) trace.exit(MsgDND_DragSourceListener.class);
  }


  /**
   * this message goes to DragSourceListener, informing it that the dragging 
   * has ended
   * 
   */
  public void dragDropEnd (DragSourceDropEvent event) { 
    //System.out.println("MsgRecordDragSourceListener.dragDropEnd " + Thread.currentThread() + " event = " + event);
  }

  /**
   * this message goes to DragSourceListener, informing it that the dragging 
   * has entered the DropSite
   * 
   */
  public void dragEnter (DragSourceDragEvent event) {
    //System.out.println("MsgRecordDragSourceListener.dragEnter " + Thread.currentThread() + " event = " + event);
  }

  /**
   * this message goes to DragSourceListener, informing it that the dragging 
   * has exited the DropSite
   * 
   */
  public void dragExit (DragSourceEvent event) {
    //System.out.println("MsgRecordDragSourceListener.dragExit " + Thread.currentThread() + " event = " + event);
    event.getDragSourceContext().setCursor(DragSource.DefaultCopyNoDrop);
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
      //System.out.println("MsgRecordDragSourceListener.dragOver " + Thread.currentThread() + " event = " + event);
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
    //System.out.println("MsgRecordDragSourceListener.dropActionChanged " + Thread.currentThread() + " event = " + event);
   }

} // end class MsgDND_DragSourceListener
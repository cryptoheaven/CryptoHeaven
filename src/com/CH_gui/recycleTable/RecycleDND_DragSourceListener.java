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

package com.CH_gui.recycleTable;

import java.awt.dnd.*;
import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.1 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class RecycleDND_DragSourceListener extends Object implements DragSourceListener {

  /** Creates new RecycleDND_DragSourceListener */
  public RecycleDND_DragSourceListener() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecycleDND_DragSourceListener.class, "RecycleDND_DragSourceListener()");
    if (trace != null) trace.exit(RecycleDND_DragSourceListener.class);
  }

  /**
   * this message goes to DragSourceListener, informing it that the dragging 
   * has ended
   * 
   */
  public void dragDropEnd (DragSourceDropEvent event) { 
  }

  /**
   * this message goes to DragSourceListener, informing it that the dragging 
   * has entered the DropSite
   * 
   */
  public void dragEnter (DragSourceDragEvent event) {
  }

  /**
   * this message goes to DragSourceListener, informing it that the dragging 
   * has exited the DropSite
   * 
   */
  public void dragExit (DragSourceEvent event) {
    event.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
  }

  /**
   * this message goes to DragSourceListener, informing it that the dragging is currently 
   * ocurring over the DropSite
   * 
   */
  public void dragOver (DragSourceDragEvent event) {
    int action = event.getTargetActions();
    DragSourceContext dsc = event.getDragSourceContext();
    if (action == DnDConstants.ACTION_MOVE)
      dsc.setCursor(DragSource.DefaultMoveDrop);
    else if (action == DnDConstants.ACTION_COPY)
      dsc.setCursor(DragSource.DefaultCopyDrop);
  }

  /**
   * is invoked when the user changes the dropAction
   * 
   */
  public void dropActionChanged ( DragSourceDragEvent event) {
  }

} // end class RecycleDND_DragSourceListener
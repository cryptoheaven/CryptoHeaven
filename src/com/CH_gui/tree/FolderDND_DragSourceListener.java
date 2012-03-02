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

package com.CH_gui.tree;

import java.awt.dnd.*;

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
 * <b>$Revision: 1.9 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FolderDND_DragSourceListener extends Object implements DragSourceListener {

  /**
   * this message goes to DragSourceListener, informing it that the dragging 
   * has ended
   * 
   */
  public void dragDropEnd (DragSourceDropEvent event) { 
    //System.out.println("FileRecordDragSourceListener.dragDropEnd " + Thread.currentThread() + " event = " + event);
  }

  /**
   * this message goes to DragSourceListener, informing it that the dragging 
   * has entered the DropSite
   * 
   */
  public void dragEnter (DragSourceDragEvent event) {
    //System.out.println("FileRecordDragSourceListener.dragEnter " + Thread.currentThread() + " event = " + event);
  }

  /**
   * this message goes to DragSourceListener, informing it that the dragging 
   * has exited the DropSite
   * 
   */
  public void dragExit (DragSourceEvent event) {
    //System.out.println("FileRecordDragSourceListener.dragExit " + Thread.currentThread() + " event = " + event);
  }

  /**
   * this message goes to DragSourceListener, informing it that the dragging is currently 
   * ocurring over the DropSite
   * 
   */
  public void dragOver (DragSourceDragEvent event) {
    //System.out.println("FileRecordDragSourceListener.dragOver " + Thread.currentThread() + " event = " + event);
  }

  /**
   * is invoked when the user changes the dropAction
   * 
   */
  public void dropActionChanged ( DragSourceDragEvent event) {
    //System.out.println("FileRecordDragSourceListener.dropActionChanged " + Thread.currentThread() + " event = " + event);
  }

} // end class FolderRecordDragSourceListener
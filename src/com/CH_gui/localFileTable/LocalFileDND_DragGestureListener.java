/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.localFileTable;

import java.awt.dnd.*;
import java.awt.Point;
import javax.swing.*;
import java.util.*;
import java.io.*;

import com.CH_co.trace.Trace;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.10 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class LocalFileDND_DragGestureListener extends Object implements DragGestureListener {
  private JFileChooser jFileChooser;

  protected LocalFileDND_DragGestureListener(JFileChooser fileChooser) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LocalFileDND_DragGestureListener.class, "LocalFileDND_DragGestureListener(JFileChooser fileChooser)");
    jFileChooser = fileChooser;
    if (trace != null) trace.exit(LocalFileDND_DragGestureListener.class);
  }

  /**
   * a drag gesture has been initiated
   */
  public void dragGestureRecognized(DragGestureEvent event) {
    File[] selected = jFileChooser.getSelectedFiles();
    if (selected != null && selected.length > 0){
      LocalFileDND_Transferable objs = new LocalFileDND_Transferable(selected);
      // as the name suggests, starts the dragging
      event.getDragSource().startDrag(event, null, objs, new LocalFileDND_DragSourceListener());
    } else {
      //System.out.println( "nothing was selected");   
    }
  }


  /**
   * private inner nested class.
   */
  private class LocalFileDND_DragSourceListener implements DragSourceListener {

    private Point lastPt;

    /**
     * this message goes to DragSourceListener, informing it that the dragging 
     * has ended
     * 
     */
    public void dragDropEnd (DragSourceDropEvent event) {
      if (event.getDropAction() == DnDConstants.ACTION_MOVE) {
        java.util.Timer timer = new java.util.Timer(true);
        timer.schedule(new TimerTask() {
          public void run() {
            jFileChooser.rescanCurrentDirectory();
          }
        }, 3000);
      }
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
    }
  } // end class LocalFileDND_DragSourceListener


} // end class LocalFileDND_DragGestureListener
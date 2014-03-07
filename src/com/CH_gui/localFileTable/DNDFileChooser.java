/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.localFileTable;

import com.CH_co.trace.Trace;
import java.awt.Component;
import java.awt.Container;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.io.File;
import javax.swing.*;

/** 
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.12 $</b>
*
* @author  Marcin Kurzawa
*/
public class DNDFileChooser extends JFileChooser {

  private LocalFileDND_DropTargetListener dropTargetListener;
  private LocalFileDND_DragGestureListener dragGestureListener;

  /** Creates new DNDFileChooser */
  public DNDFileChooser(File currentDirectory) {
    super(currentDirectory);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DNDFileChooser.class, "DNDFileChooser(File currentDirectory)");
    if (trace != null) trace.args(currentDirectory);
    assignDropAndDragComponents(this);
    setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    if (trace != null) trace.exit(DNDFileChooser.class);
  }

  private void assignDropAndDragComponents(Component c) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DNDFileChooser.class, "assignDropAndDragComponents(Component c)");
    if (trace != null) trace.args(c != null ? c.getClass().getName() : "null");

    if (c != null && 
          (
            c instanceof JList ||
            c instanceof JViewport
          )
        )
    {
      if (trace != null) trace.data(10, "assign Drop and Drag to", c);
      if (dropTargetListener == null)
        dropTargetListener = new LocalFileDND_DropTargetListener(this);
      new DropTarget(c, dropTargetListener);
      DragSource dragSource = DragSource.getDefaultDragSource();
      if (dragGestureListener == null)
        dragGestureListener = new LocalFileDND_DragGestureListener(this);
      dragSource.createDefaultDragGestureRecognizer(c, DnDConstants.ACTION_COPY_OR_MOVE, dragGestureListener);
    }
    if (c instanceof Container) {
      Container cont = (Container) c;
      Component[] cc = cont.getComponents();
      if (cc != null) {
        for (int i=0; i<cc.length; i++)
          assignDropAndDragComponents(cc[i]);
      }
    }

    if (trace != null) trace.exit(DNDFileChooser.class);
  }


  /**
  * Testing of JFileChooser
  */
  public static void main(String[] args) {
    try {
      long startTime = new java.util.Date().getTime();
      JFrame f = new JFrame("Test of JFileChooser");
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      final JFileChooser jFileChooser = new JFileChooser("C:\\");
      jFileChooser.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          String cmd = e.getActionCommand();
          if (cmd.equals("ApproveSelection")) {
            System.out.println("Selected " + com.CH_co.util.Misc.objToStr(jFileChooser.getSelectedFile()));
          } else if (cmd.equals("CancelSelection")) {
            System.out.println("Cancel");
            System.exit(-1);
          }
        }
      });
      f.getContentPane().add(new JScrollPane(jFileChooser), "Center");
      f.setSize(600, 400);
      f.setVisible(true);
      long endTime = new java.util.Date().getTime();
      System.out.println("initialization time = " + ((endTime-startTime)/1000.0) + " seconds");
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(-2);
    }
  }

}
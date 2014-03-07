/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.frame;

import com.CH_co.service.records.FolderPair;
import com.CH_gui.actionGui.JActionFrameClosable;
import com.CH_gui.table.RecordTableComponent;
import com.CH_gui.util.Nudge;
import javax.swing.JFrame;

/** 
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.1 $</b>
*
* @author  Marcin Kurzawa
*/
public abstract class RecordTableFrame extends JActionFrameClosable {

  protected RecordTableComponent mainTableComponent = null;

  public RecordTableFrame(String title, boolean withMenuBar, boolean withToolBar) {
    super(title, withMenuBar, withToolBar);
  }

  public static RecordTableFrame getOpenRecordTableFrame(FolderPair folderPair) {
    RecordTableFrame openFrame = null;
    if (allClosableFramesL != null) {
      // reverse order to grab the last matching one
      for (int i=allClosableFramesL.size()-1; i>=0; i--) {
        JActionFrameClosable f = (JActionFrameClosable) allClosableFramesL.get(i);
        if (f instanceof RecordTableFrame) {
          RecordTableFrame tableFrame = (RecordTableFrame) f;
          try {
            if (tableFrame.getTableComponent().getRecordTableScrollPane().getTableModel().getParentFolderPair().equals(folderPair)) {
              openFrame = tableFrame;
              break;
            }
          } catch (Throwable t) {
          }
        }
      }
    }
    return openFrame;
  }

  public RecordTableComponent getTableComponent() {
    return mainTableComponent;
  }

  public static void toFrontAnimation(JFrame frame) {
    int prevState = frame.getState();
    frame.toFront();
    frame.setState(JFrame.NORMAL);
    if (prevState != JFrame.ICONIFIED)
      Nudge.nudge(frame, false, false, true);
  }

}
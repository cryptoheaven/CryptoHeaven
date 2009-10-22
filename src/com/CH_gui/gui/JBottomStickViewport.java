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

package com.CH_gui.gui;

import java.awt.*;
import java.beans.*;
import javax.swing.*;

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
 * <b>$Revision: 1.9 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class JBottomStickViewport extends JViewport {

  int lastBottomPosViewed = -1;
  int lastTopPosViewed = -1;
  int lastViewHeight = -1;

  boolean adjustmentInProgress = false;

  boolean autoScrollEnabled = true;
  boolean autoScrollModeBottom = true;

  /** Creates new JBottomStickViewport */
  public JBottomStickViewport() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JBottomStickViewport.class, "JBottomStickViewport()");
    if (trace != null) trace.exit(JBottomStickViewport.class);
  }

  public synchronized void setAutoScrollEnabled(boolean enable) {
    autoScrollEnabled = enable;
  }

  public synchronized void setAutoScrollMode(boolean isBottom) {
    autoScrollModeBottom = isBottom;
  }

  public void repaint(long tm, int x, int y, int w, int h) {
    super.repaint(tm, x, y, w, h);
  }

  public void reshape(int x, int y, int w, int h) {
    doAutoScrollToBottom();
    super.reshape(x, y, w, h);
  }

  public synchronized void doAutoScrollToBottom() {
    if (autoScrollEnabled && !adjustmentInProgress) {
      try {
        int extentHeight = getExtentSize().height;
        int viewHeight = getView().getHeight();
        int heightAdded = viewHeight - lastViewHeight;

        if ((lastViewHeight > 0 && lastBottomPosViewed > 0 && lastTopPosViewed > 0 &&
            lastViewHeight == lastBottomPosViewed && 
            lastTopPosViewed + extentHeight + heightAdded == viewHeight &&
            lastViewHeight != viewHeight) // || or no scrollbar previously but now more content so scrollbar will be inserted (lastViewHeight > 0 && lastBottomPosViewed > 0 && lastViewHeight <= extentHeight)
            )
        {
          adjustmentInProgress = true;
          if (autoScrollModeBottom) {
            scrollRectToVisible(new Rectangle(getViewPosition().x, viewHeight-1, 1, 1));
          } else {
            scrollRectToVisible(new Rectangle(getViewPosition().x, 0, 1, 1));
          }
          adjustmentInProgress = false;
        }
        int viewPosY = getViewPosition().y;
        lastBottomPosViewed = viewHeight > (viewPosY + extentHeight) ? (viewPosY + extentHeight) : viewHeight;
        lastTopPosViewed = viewPosY;
        lastViewHeight = viewHeight;
      } catch (Throwable t) {
      }
    }
  }

}
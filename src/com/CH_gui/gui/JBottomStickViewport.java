/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.gui;

import com.CH_co.trace.Trace;
import java.awt.Rectangle;
import javax.swing.JViewport;

/**
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.9 $</b>
*
* @author  Marcin Kurzawa
*/
public class JBottomStickViewport extends JViewport {

  int lastBottomPosViewed = -1;
  int lastTopPosViewed = -1;
  int lastViewHeight = -1;

  boolean adjustmentInProgress = false;

  boolean autoScrollEnabled = true;
  boolean autoScrollModeBottom = true;

  // Temporary suppression of scrolling without changing general mode or enablement.
  boolean isAutoScrollSuppressed = false;

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

  public void setAutoScrollSuppressed(boolean flag) {
    isAutoScrollSuppressed = flag;
  }

  public void repaint(long tm, int x, int y, int w, int h) {
    super.repaint(tm, x, y, w, h);
  }

  public void reshape(int x, int y, int w, int h) {
    doAutoScrollToBottom();
    super.reshape(x, y, w, h);
  }

  private synchronized void doAutoScrollToBottom() {
    if (!isAutoScrollSuppressed) {
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

}
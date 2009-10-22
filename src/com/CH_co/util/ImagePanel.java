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

package com.CH_co.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;

/**
 *
 * @author Marcin
 */
public class ImagePanel extends JPanel {

  private Image img;
  private Dimension imgSize;
  double idealRatio;
  boolean zoomMode = false;
  double posX = 0;
  double posY = 0;
  int pressX = 0;
  int pressY = 0;
  boolean isPressed = false;

  public ImagePanel(Image image) {
    this.img = image;
    MediaTracker mediaTracker = new MediaTracker(this);
    mediaTracker.addImage(img, 0);
    try {
      mediaTracker.waitForID(0);
      mediaTracker.removeImage(img, 0);
    } catch (InterruptedException ie) {
    }
    Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
    this.imgSize = size;
    this.idealRatio = (double) size.width / (double) size.height;
    setPreferredSize(size);
    setLayout(null);
    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        zoomMode = false;
        posX = 0;
        posY = 0;
        repaint();
      }
    });
    addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        Dimension size = getSize();
        double scale = Math.max((double) size.width / (double) imgSize.width, (double) size.height / (double) imgSize.height);
        if (scale < 1.0) {
          zoomMode = !zoomMode;
          double x = e.getX();
          double y = e.getY();
          double ratioNow = (double) size.width / (double) size.height;
          if (zoomMode) {
            if (ratioNow > idealRatio) {
              double xP = x / (double) size.width;
              posX = (int) posX/scale -((imgSize.width - size.width) * xP);
              posY = (int) posY/scale -(y / scale - y);
            } else {
              double yP = y / (double) size.height;
              posX = (int) posX/scale -(x / scale - x);
              posY = (int) posY/scale -((imgSize.height - size.height) * yP);
            }
          } else {
            if (ratioNow > idealRatio) {
              double xP = x / (double) size.width;
              posX = (int) posX*scale +((imgSize.width - size.width) * xP)*scale;
              posY = (int) posY*scale +(y / scale - y)*scale;
            } else {
              double yP = y / (double) size.height;
              posX = (int) posX*scale +(x / scale - x)*scale;
              posY = (int) posY*scale +((imgSize.height - size.height) * yP)*scale;
            }
            // zooming out check boundries
            checkBoundries();
          }
          repaint();
          checkCursor();
        }
      }

      public void mousePressed(MouseEvent e) {
        isPressed = true;
        pressX = e.getX();
        pressY = e.getY();
      }

      public void mouseReleased(MouseEvent e) {
        isPressed = false;
        int x = e.getX();
        int y = e.getY();
        posX += x-pressX;
        posY += y-pressY;
        checkBoundries();
        repaint();
      }

      public void mouseEntered(MouseEvent e) {
        checkCursor();
      }
      
      private void checkCursor() {
        Dimension size = getSize();
        if (size.width < imgSize.width && size.height < imgSize.height) {
          if (!zoomMode)
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          else
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        } else {
          double scale = Math.max((double) size.width / (double) imgSize.width, (double) size.height / (double) imgSize.height);
          if (scale != 1.0)
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
          else
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      }
    });
    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent e) {
        if (isPressed) {
          int x = e.getX();
          int y = e.getY();
          posX += x-pressX;
          posY += y-pressY;
          pressX = x;
          pressY = y;
          checkBoundries();
          repaint();
        }
      }
    });
  }

  private void checkBoundries() {
    Dimension size = getSize();
    if (posX > 0)
      posX = 0;
    if (posY > 0)
      posY = 0;
    double scale = 1.0;
    if (!zoomMode) {
      scale = Math.max((double) size.width / (double) imgSize.width, (double) size.height / (double) imgSize.height);
    }
    if (posX+imgSize.width*scale < size.width)
      posX += size.width-(posX+imgSize.width*scale);
    if (posY+imgSize.height*scale < size.height)
      posY += size.height-(posY+imgSize.height*scale);
  }

  public void flushImage() {
    img.flush();
    img = null;
  }
  
  public void paintComponent(Graphics g) {
    double scale = 1.0;
    if (!zoomMode) {
      Dimension size = getSize();
      scale = Math.max((double) size.width / (double) imgSize.width, (double) size.height / (double) imgSize.height);
    }
    if (img != null) {
      g.drawImage(img, (int) posX, (int) posY, (int) (imgSize.width * scale), (int) (imgSize.height * scale), this);
    }
  }

}
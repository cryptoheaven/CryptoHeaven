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

package com.CH_co.util;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.net.URL;
import javax.swing.ImageIcon;

import com.CH_co.trace.Trace;
import java.util.Collection;
import java.util.Iterator;

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
 * <b>$Revision: 1.42 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class Images extends Object {

  private static final ImageIcon[] imageIcons;

  static {
    imageIcons = new ImageIcon[ImageNums.NUMBER_OF_IMAGES];
  }

  public static void printUnusedIconNames() {
    Collection unUsedIconNames = ImageNums.getUnusedImageNames();
    Iterator iter = unUsedIconNames.iterator();
    while (iter.hasNext()) {
      System.out.println(iter.next());
    }
  }

  public static void clearImageCache(int imageCode) {
    imageIcons[imageCode] = null;
  }

  public static ImageIcon get(String name) {
    Integer imageCode = ImageNums.getImageCode(name);
    if (imageCode != null)
      return get(imageCode.intValue());
    else
      return null;
  }
  public static ImageIcon get(ImageText imageText) {
    return get(imageText.getIcon());
  }
  public static ImageIcon get(int imageCode) {
    boolean isShared = imageCode >= ImageNums.SHARED_OFFSET;
    if (isShared) {
      imageCode -= ImageNums.SHARED_OFFSET;
    }
    return get(imageCode, isShared);
  }
  public static ImageIcon get(int imageCode, boolean isShared) {
    ImageIcon icon = null;
    if (imageCode == ImageNums.IMAGE_NONE) {
      icon = null;
    } else if (imageCode == ImageNums.IMAGE_SPECIAL_HANDLING) {
      throw new IllegalArgumentException("Image code needs special handling.");
    } else {
      if (imageIcons[imageCode] == null) {
        String fileName = null;
        URL location = null;
        try {
          ImageNums.setUsedIcon(imageCode);
          String name = ImageNums.getImageName(imageCode);
          // If customized version and logo image is our own, not from URL then null it
          // First 3 (index 0, 1, 2) images MUST be customized
          if (URLs.hasPrivateLabelCustomizationClass() && imageCode <= 2 && name.indexOf("://") < 0 && name.indexOf("jar:file:") < 0) {
            name = "";
          }
          if (name.length() > 0) {
            // If customized version and image is in a URL format, then load directly from URL
            if (URLs.hasPrivateLabelCustomization() && (name.indexOf("://") >= 0 || name.indexOf("jar:file:") >= 0)) {
              location = new URL(name);
            } else {
              fileName = "images/" + name;
              location = URLs.getResourceURL(fileName);
              if (location == null)
                location = URLs.getResourceURL(fileName + ".png");
              if (location == null)
                location = URLs.getResourceURL(fileName + ".gif");
            }
          }
          if (location == null) {
            // we will include this ERROR call in the trace
            traceGetError(imageCode, fileName, null);
          }
          if (location != null) {
            imageIcons[imageCode] = new ImageIcon(location);
          }
          if (isShared) {
            // add a small share hand to the icon
            ImageIcon shareHandSmallImage = get(ImageNums.SHARE_HAND_L, false);
            int newHeight = imageIcons[imageCode].getIconHeight() + 2;
            BufferedImage total = new BufferedImage(imageIcons[imageCode].getIconWidth(), newHeight, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D g = total.createGraphics();
            g.drawImage(imageIcons[imageCode].getImage(), 0, 0, null);
            double scale = ((double) imageIcons[imageCode].getIconWidth()) / ((double) shareHandSmallImage.getIconWidth());
            double moveDownBy = ((double) newHeight - (scale * (double) shareHandSmallImage.getIconHeight())) / scale;
            AffineTransform xform = AffineTransform.getScaleInstance(scale, scale);
            xform.translate(0.0, moveDownBy);
            g.drawImage(shareHandSmallImage.getImage(), xform, null);
            imageIcons[imageCode] = new ImageIcon(total);
          }
        } catch (Throwable t) {
          t.printStackTrace();
          traceGetError(imageCode, fileName, location);
        }
      }
      icon = imageIcons[imageCode];
    }
    return icon;
  }

  private static void traceGetError(int imageCode, String fileName, URL location) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Images.class, "traceGetError(int imageCode, String fileName, URL location)");
    if (trace != null) trace.args(imageCode);
    if (trace != null) trace.args(fileName, location);
    if (trace != null) trace.exit(Images.class);
  }

  public static void main(String[] args) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Images.class, "main(String[])");

    try {
      javax.swing.JFrame frameL = new javax.swing.JFrame("All Available Icons - Large");
      java.awt.Container cL = frameL.getContentPane();
      javax.swing.JPanel panelL = new javax.swing.JPanel();
      panelL.setLayout(new java.awt.GridLayout(5, 3));
      //panelL.setLayout(new java.awt.FlowLayout());
      for (int i=0; i<ImageNums.NUMBER_OF_IMAGES; i++) {
        ImageIcon icon = get(i);
        if (icon != null) {
          if ((icon.getIconHeight() > 32 || icon.getIconWidth() > 32) && icon.getIconWidth() < 200) {
            javax.swing.JLabel item = new javax.swing.JLabel(ImageNums.getImageName(i), icon, javax.swing.JLabel.LEFT);
            item.setVerticalTextPosition(javax.swing.JLabel.CENTER);
            panelL.add(item);
          }
        } else {
          System.out.println("Null icon " + i + " " + ImageNums.getImageName(i));
        }
      }
      cL.add(new javax.swing.JScrollPane(panelL));
      frameL.pack();
      frameL.setVisible(true);

      javax.swing.JFrame frame = new javax.swing.JFrame("All Available Icons - Small");
      java.awt.Container c = frame.getContentPane();
      javax.swing.JPanel panel = new javax.swing.JPanel();
      panel.setLayout(new java.awt.GridLayout(ImageNums.NUMBER_OF_IMAGES / 10 +1, 10));
      //panel.setLayout(new java.awt.FlowLayout());
      for (int i=0; i<ImageNums.NUMBER_OF_IMAGES; i++) {
        ImageIcon icon = get(i);
        if (icon != null) {
          if (icon.getIconHeight() <= 32 || icon.getIconWidth() <= 32) {
            javax.swing.JLabel item = new javax.swing.JLabel(ImageNums.getImageName(i), icon, javax.swing.JLabel.LEFT);
            item.setVerticalTextPosition(javax.swing.JLabel.CENTER);
            panel.add(item);
          }
        } else {
          System.out.println("Null icon " + i + " " + ImageNums.getImageName(i));
        }
      }
      c.add(new javax.swing.JScrollPane(panel));
      frame.pack();
      frame.setVisible(true);

    } catch (Throwable t) {
      t.printStackTrace();
      if (trace != null) trace.exception(Images.class, 100, t);
    }

    if (trace != null) trace.exit(Images.class);
  }
}
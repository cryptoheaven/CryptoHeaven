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

package com.CH_gui.util;

import com.CH_co.util.ImageNums;
import com.CH_co.util.Images;
import com.CH_co.util.MiscGui;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

/**
 *
 * @author Marcin
 */

public class ImageViewer {

  public static void main(String[] args) {
    String filename = args[0];
    File file = new File(filename);
    showImage(file);
  }

  public static void showImage(File file) {
    showImage(file, null);
  }

  public static void showImage(File file, Component invoker) {
    Image img = Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());
    ImagePanel imgPanel = new ImagePanel(img);
    String name = file.getName();
    JFrame frame = new JFrame(name);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.addWindowListener(new MyWindowAdapter(imgPanel));
    frame.getContentPane().add(imgPanel);
    frame.pack();
    // Fix the ratio
    Dimension pref = imgPanel.getPreferredSize();
    Dimension size = imgPanel.getSize();
    if (pref.width != size.width || pref.height != size.height) {
      double ratio = (double) pref.width / (double) pref.height;
      int width = (int) (size.height * ratio);
      int height = (int) (size.width / ratio);
      imgPanel.setPreferredSize(new Dimension(width, height));
      frame.pack();
    }
    ImageIcon frameIcon = Images.get(ImageNums.FRAME_LOCK32);
    if (frameIcon != null) {
      frame.setIconImage(frameIcon.getImage());
    }
    MiscGui.setSuggestedWindowLocation(invoker, frame);
    frame.setVisible(true);
  }

  private static class MyWindowAdapter extends WindowAdapter {
    private ImagePanel imgPanel;
    private MyWindowAdapter(ImagePanel imgPanel) {
      this.imgPanel = imgPanel;
    }
    public void windowClosed(WindowEvent e) {
      imgPanel.flushImage();
      e.getWindow().removeWindowListener(MyWindowAdapter.this);
    }
  }
}
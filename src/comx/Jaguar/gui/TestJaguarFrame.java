/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package comx.Jaguar.gui;

import com.CH_gui.gui.JMyLabel;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.CH_co.gui.*;

/** 
 * <b>Copyright</b> &copy; 2001-2011
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
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class TestJaguarFrame extends Object {

  public static void main(String[] args) {

    JFrame frame = new JFrame("Test Jaguar Frame");
    frame.addWindowListener(new CloseWindowListener());
    Container pane = frame.getContentPane();

    JMenu jMenuFile = new JMenu("File");
    JMenuItem close = new JMenuItem("Close Frame");
    close.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        System.exit(0);
      }
    });
    jMenuFile.add(close);
    JMenuBar jMenuBar = new JMenuBar();
    jMenuBar.add(jMenuFile);
    frame.setJMenuBar(jMenuBar);

    JPanel mainComp = new JPanel();
    boolean showToolBar = true;
    boolean showViewSource = false;
    boolean showMenuIcons = true;
    boolean editModeExclusive = true;
    Jaguar jaguar = new Jaguar(showToolBar, showViewSource, showMenuIcons, editModeExclusive);

    mainComp.setLayout(new BorderLayout());
    mainComp.add(new JMyLabel("Jaguar component:"), BorderLayout.NORTH);
    mainComp.add(jaguar, BorderLayout.CENTER);

    pane.add(mainComp, "Center");

    frame.pack();
    frame.setVisible(true);
  }

  private static class CloseWindowListener extends WindowAdapter {
    public void windowClosing(WindowEvent event) {
      System.exit(0);
    }
  }
}
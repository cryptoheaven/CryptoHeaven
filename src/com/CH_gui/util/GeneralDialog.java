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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version 
 */

 /* Description: This a general dialog that containts a panel and buttons 
 * on the bottom-right corner of the dialog. 
 */
public class GeneralDialog extends JDialog {

  // Default parent window for showing up dialogs.
  private static Frame defaultParent;

  private EscapeKeyListener escapeKeyListener;
  private JButton escapeButton;


  /** Creates new GeneralDialog */
  public GeneralDialog(String title, JButton[] buttons, int default_index, JComponent mainComponent) {
    this(title, buttons, default_index, -1, mainComponent);
  }
  /** Creates new GeneralDialog */
  public GeneralDialog(String title, JButton[] buttons, int default_index, int default_cancel, JComponent mainComponent) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GeneralDialog.class, "GeneralDialog(Frame, String, JButton[], int, JComponent)");
    setTitle(title);
    setDefaultCloseOperation (DO_NOTHING_ON_CLOSE);
    init(null, buttons, mainComponent, default_index, default_cancel);
    if (trace != null) trace.exit(GeneralDialog.class);
  }

  /** Creates new GeneralDialog */
  public GeneralDialog(Frame owner, String title, JButton[] buttons, int default_index, JComponent mainComponent) {
    this(owner, title, buttons, default_index, -1, mainComponent);
  }
  /** Creates new GeneralDialog */
  public GeneralDialog(Frame owner, String title, JButton[] buttons, int default_index, int default_cancel, JComponent mainComponent) {
    super(owner, title);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GeneralDialog.class, "GeneralDialog(Frame, String, JButton[], int, JComponent)");
    setDefaultCloseOperation (DO_NOTHING_ON_CLOSE);
    init(owner, buttons, mainComponent, default_index, default_cancel);
    if (trace != null) trace.exit(GeneralDialog.class);
  }


  /** Creates new GeneralDialog */
  public GeneralDialog(Dialog owner, String title, JButton[] buttons, int default_index, JComponent mainComponent) {
    this(owner, title, buttons, default_index, -1, mainComponent);
  }
  /** Creates new GeneralDialog */
  public GeneralDialog(Dialog owner, String title, JButton[] buttons, int default_index, int default_cancel, JComponent mainComponent) {
    super(owner, title);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GeneralDialog.class, "GeneralDialog(Dialog, String, JButton[], int, JComponent)");
    setDefaultCloseOperation (DO_NOTHING_ON_CLOSE);
    init(owner, buttons, mainComponent, default_index, default_cancel);
    if (trace != null) trace.exit(GeneralDialog.class);
  }


  /** Creates new GeneralDialog */
  protected GeneralDialog(String title) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GeneralDialog.class, "GeneralDialog(Frame, String)");
    setTitle(title);
    if (trace != null) trace.exit(GeneralDialog.class);
  }

  /** Creates new GeneralDialog */
  protected GeneralDialog(Frame owner, String title) {
    super(owner, title);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GeneralDialog.class, "GeneralDialog(Frame, String)");
    if (trace != null) trace.exit(GeneralDialog.class);
  }

  /** Creates new GeneralDialog */
  protected GeneralDialog(Dialog owner, String title) {
    super(owner, title);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GeneralDialog.class, "GeneralDialog(Dialog, String)");
    if (trace != null) trace.exit(GeneralDialog.class);
  }



  /**
   * default_index and default_cancel when negative (-1) are ignored.
   */
  public void init(Component owner, final JButton[] buttons, JComponent mainComponent, int default_index, final int default_cancel) {
    init(owner, buttons, mainComponent, null, default_index, default_cancel, true);
  }
  public void init(Component owner, final JButton[] buttons, JComponent mainComponent, JComponent header, int default_index, final int default_cancel) {
    init(owner, buttons, mainComponent, header, default_index, default_cancel, true);
  }
  protected void init(Component owner, final JButton[] buttons, JComponent mainComponent, int default_index, final int default_cancel, boolean show) {
    init(owner, buttons, mainComponent, null, default_index, default_cancel, show);
  }
  protected void init(Component owner, final JButton[] buttons, JComponent mainComponent, JComponent header, int default_index, final int default_cancel, boolean show) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GeneralDialog.class, "init(Component owner, final JButton[] buttons, JComponent mainComponent, JComponent header, int default_index, final int default_cancel, boolean show)");
    if (trace != null) trace.args(owner, buttons, mainComponent, header);
    if (trace != null) trace.args(default_index);
    if (trace != null) trace.args(default_cancel);
    if (trace != null) trace.args(show);

    if (!Misc.isAllGUIsuppressed()) {
      if (default_index >= 0) {
        if (trace != null) trace.data(10, "setting default button...");
        JButton defaultButton = buttons[default_index];
        this.getRootPane().setDefaultButton(defaultButton);
        if (trace != null) trace.data(11, "setting default button... done.");
      }
      this.getContentPane().setLayout(new BorderLayout());
      if (header != null)
        this.getContentPane().add(header, BorderLayout.NORTH);
      this.getContentPane().add(mainComponent, BorderLayout.CENTER);
      if (buttons != null)
        this.getContentPane().add(MiscGui.createButtonPanel(buttons), BorderLayout.SOUTH);

      if (default_cancel >= 0 && buttons.length > default_cancel) {
        if (trace != null) trace.data(20, "setting cancel button...");
        escapeButton = buttons[default_cancel];
        escapeKeyListener = new EscapeKeyListener();
        this.addKeyListener(escapeKeyListener);
        if (trace != null) trace.data(21, "setting cancel button... done.");
      }

      if (trace != null) trace.data(30, "packing...");
      pack();
      if (trace != null) trace.data(31, "packed.");

      if (this instanceof VisualsSavable) {
        if (trace != null) trace.data(40, "restoring visuals...");
        VisualsSavable vs = (VisualsSavable) this;
        String key = MiscGui.getVisualsKeyName((VisualsSavable) this);
        String visuals = GlobalProperties.getProperty(key);
        vs.restoreVisuals(visuals);
        if (trace != null) trace.data(41, "restoring visuals... done.");
      }

      // enable actions
      if (trace != null) trace.data(60, "enabling actions recursively...");
      ActionUtils.setEnabledActionsRecur(this);
      if (trace != null) trace.data(61, "enabling actions recursively... done.");

      if (trace != null) trace.data(70, "setting window location...");
      Window ownerWindow = null;
      if (owner instanceof Window) {
        ownerWindow = (Window) owner;
      } else if (owner != null) {
        ownerWindow = SwingUtilities.windowForComponent(owner);
      }
      if (ownerWindow != null && ownerWindow.isShowing()) {
        MiscGui.setSuggestedWindowLocation(owner, this);
      } else {
        this.setLocationRelativeTo(owner);
      }
      if (trace != null) trace.data(71, "setting window location... done.");

      if (trace != null) trace.data(80, "adjusting max size to fit screen...");
      Dimension maxSize = MiscGui.getScreenUsableSize(0, 0, this);
      Dimension size = getSize();
      if (size.width > maxSize.width || size.height > (maxSize.height-50))
        setSize(Math.min(size.width, maxSize.width), Math.min(size.height, maxSize.height-50));
      if (trace != null) trace.data(81, "adjusting max size to fit screen... done.");

      
      if (show) {
        if (trace != null) trace.data(90, "setting Visible...");
        this.setVisible(true);
        if (trace != null) trace.data(91, "setting Visible... done.");
      }

      //if (trace != null) trace.exit(GeneralDialog.class); // in modal dialogs, this sometimes throws NullPointerException
    } // end if GUI not suppressed
    if (trace != null) trace.exit(GeneralDialog.class);
  } // end init()


  public void setNewTitle(String newTitle) {
    this.setTitle(newTitle);
  }

  public void closeDialog() {
    if (isShowing())
      MiscGui.storeVisualsSavable(this);
    try {
      setVisible(false);
      dispose();
    } catch (Throwable t) {
    }
  }

  public void dispose() {
    try {
      if (escapeKeyListener != null) {
        this.removeKeyListener(escapeKeyListener);
        escapeKeyListener = null;
        escapeButton = null;
      }
      MiscGui.removeAllComponentsAndListeners(this);
    } catch (Throwable t) {
    }
    super.dispose();
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public String getVisuals() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GeneralDialog.class, "getVisuals()");

    StringBuffer visuals = new StringBuffer();
    visuals.append("Dimension width ");
    Dimension dim = getSize();
    visuals.append(dim.width);
    visuals.append(" height ");
    visuals.append(dim.height);

    String rc = visuals.toString();
    if (trace != null) trace.exit(GeneralDialog.class, rc);
    return rc;
  }

  public void restoreVisuals(String visuals) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GeneralDialog.class, "restoreVisuals(String visuals)");
    if (trace != null) trace.args(visuals);

    try {
      if (visuals == null) {
        pack();
      } else {
        StringTokenizer st = new StringTokenizer(visuals);  
        st.nextToken();
        st.nextToken();
        int width = Integer.parseInt(st.nextToken());
        st.nextToken();
        int height = Integer.parseInt(st.nextToken());
        setSize(width, height);
      }
    } catch (Throwable t) {
      if (trace != null) trace.exception(GeneralDialog.class, 100, t);
      // if failed then at the very least pack() window so it shows up with non-zero size
      pack();
      // reset the properties since they are corrupted
      GlobalProperties.resetMyAndGlobalProperties();
    }

    if (trace != null) trace.exit(GeneralDialog.class);
  }

  public String getExtension() {
    return null;
  }
  public Integer getVisualsVersion() {
    return null;
  }
  public boolean isVisuallyTraversable() {
    return true;
  }


  /**
   * Clicks a specified button when ESCAPE key click is detected.
   */
  private class EscapeKeyListener extends KeyAdapter {
    public void keyPressed(KeyEvent event) {
      if (event.getModifiers() == 0) {
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
          escapeButton.doClick();
        }
      }
    }
  }


  protected void processWindowEvent(WindowEvent windowEvent) {
    super.processWindowEvent(windowEvent);
    if (windowEvent.getID() == WindowEvent.WINDOW_CLOSING) {
      closeDialog();
    }
  }

  public static Frame getDefaultParent() {
    return defaultParent;
  }
  public static void setDefaultParent(Frame parent) {
    defaultParent = parent;
  }

}
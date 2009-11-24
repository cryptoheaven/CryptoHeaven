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

package com.CH_gui.actionGui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.action.*;
import com.CH_gui.frame.*;
import com.CH_gui.gui.*;

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
 * <b>$Revision: 1.23 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public abstract class JActionFrameClosable extends JActionFrame implements ActionProducerI, VetoableI {

  private Action[] actions;
  private static final int CLOSE_ACTION = 0;

  private static Vector allClosableFrames;
  private Vector vetoRisiblesV = null;

  /** Creates new JActionFrameClosable */
  public JActionFrameClosable(String title, boolean withMenuBar, boolean withToolBar) {
    super(title, withMenuBar, withToolBar);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JActionFrameClosable.class, "JActionFrameClosable()");
    if (allClosableFrames == null)
      allClosableFrames = new Vector();
    allClosableFrames.addElement(this);
    if (trace != null) trace.exit(JActionFrameClosable.class);
  }

  private void initActions() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JActionFrameClosable.class, "initActions()");
    int leadingActionId = Actions.LEADING_ACTION_ID_ACTION_FRAME_CLOSABLE;
    actions = new Action[1];
    actions[CLOSE_ACTION] = new CloseAction(leadingActionId + CLOSE_ACTION);
    if (trace != null) trace.exit(JActionFrameClosable.class);
  }
  public Action getCloseAction() {
    return actions[CLOSE_ACTION];
  }


  /**
   * Closes all frames if no veto is raised.
   * @return true is all frames closed.
   */
  public static boolean closeAllClosableFramesVetoable() {
    boolean closed = true;
    if (allClosableFrames != null) {
      boolean closeOK = true;
      for (int i=allClosableFrames.size()-1; i>=0; i--) {
        JActionFrameClosable f = (JActionFrameClosable) allClosableFrames.elementAt(i);
        closeOK = f.closeAction(true);
        if (!closeOK)
          break;
      }
      if (closeOK) {
        for (int i=allClosableFrames.size()-1; i>=0; i--) {
          JActionFrameClosable f = (JActionFrameClosable) allClosableFrames.elementAt(i);
          f.closeFrame();
        }
      }
      closed = closeOK;
    }
    return closed;
  }

  public static void closeAllClosableFramesLeaveNonUserSensitive() {
    if (allClosableFrames != null) {
      for (int i=allClosableFrames.size()-1; i>=0; i--) {
        JActionFrameClosable f = (JActionFrameClosable) allClosableFrames.elementAt(i);
        if (f instanceof ContactTableFrame || 
            f instanceof FolderTreeFrame || 
            f instanceof LocalFileTableFrame ||
            f instanceof MessageFrame)
        {
          // leave open
        } else {
          f.closeFrame();
        }
      }
    }
  }

  public static Point getSpreadWindowLocation(Window w) {
    Point newP = null;
    if (allClosableFrames != null) {
      Point wP = w.getLocation();
      Dimension wD = w.getSize();
      Dimension screen = MiscGui.getScreenUsableSize(wP.x, wP.y, w);
      // traverse existing frames backwards
      for (int i=allClosableFrames.size()-1; i>=0; i--) {
        Window c = (Window) allClosableFrames.elementAt(i);
        Point cP = c.getLocation();
        Dimension cD = c.getSize();
        if (w != c && w.getClass().equals(c.getClass()) && wP.equals(cP) && wD.equals(cD)) {
          int newX = wP.x;
          int newY = wP.y;
          if (wP.x+16+wD.width <= screen.width)
            newX = wP.x+16;
          else if (wP.x-16 >= 0)
            newX = wP.x-16;
          if (wP.y+16+wD.height <= screen.height)
            newY = wP.y+16;
          else if (wP.y-16 >= 0)
            newY = wP.y-16;
          newP = new Point(newX, newY);
          break;
        }
      }
    }
    return newP;
  }

  /**
   * Check if this frame was already closed.  Closed frames are disposed and should not be shown again.
   * @return true if this frame was already closed
   */
  public boolean isClosed() {
    return !allClosableFrames.contains(this);
  }

  /**
   * Closes the frame and saves its properties.  Triggered with the Close Action Menu Item or by pressing 'x'.
   */
  public void closeFrame() {
    saveFrameProperties();

    // remove this frame from closable collection
    try {
      allClosableFrames.remove(this);
    } catch (Throwable t) { }

    try {
      setVisible(false);
      // dispose window native resources
      dispose();
    } catch (Throwable t) { }
    // if last Closable Frame closed and no MainFrame, exit
    if (allClosableFrames.size() == 0 && MainFrame.getSingleInstance() == null) {
      MainFrame.exitAction(null);
    }
  } // end closeFrame()


  private boolean closeAction() {
    return closeAction(false);
  }
  private boolean closeAction(boolean probeOnly) {
    boolean veto = false;
    if (vetoRisiblesV != null) {
      for (int i=0; i<vetoRisiblesV.size(); i++) {
        VetoRisibleI v = (VetoRisibleI) vetoRisiblesV.elementAt(i);
        if (v.isVetoRaised(VetoRisibleI.TYPE_WINDOW_CLOSE)) {
          veto = true;
          break;
        }
      }
    }
    if (!probeOnly && !veto) {
      closeFrame();
    }
    return !veto;
  }

  protected void processWindowEvent(WindowEvent windowEvent) {
    if (windowEvent.getID() == WindowEvent.WINDOW_CLOSING) {
      if (closeAction()) {
        super.processWindowEvent(windowEvent);
      }
    } else {
      super.processWindowEvent(windowEvent);
    }
  }

  /*********************************************/
  /**    A c t i o n   P r o d u c e r  I     **/
  /*********************************************/

  /** @return all the acitons that this objects produces.
   */
  public Action[] getActions() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JActionFrameClosable.class, "getActions()");
    if (actions == null) {
      initActions();
    }
    Action[] a = ActionUtilities.concatinate(super.getActions(), actions);
    if (trace != null) trace.exit(JActionFrameClosable.class, a);
    return a;
  }

  // =====================================================================
  // LISTENERS FOR THE MENU ITEMS        
  // =====================================================================

  /** 
   * Close the frame.
   **/
  private class CloseAction extends AbstractActionTraced {
    public CloseAction(int actionId) {
      super(com.CH_gui.lang.Lang.rb.getString("action_Close"), Images.get(ImageNums.DELETE16));
      putValue(Actions.ACTION_ID, new Integer(actionId));
      putValue(Actions.TOOL_TIP, com.CH_gui.lang.Lang.rb.getString("actionTip_Close_the_Frame."));
      putValue(Actions.TOOL_ICON, Images.get(ImageNums.DELETE24));
      putValue(Actions.TOOL_NAME, com.CH_gui.lang.Lang.rb.getString("actionTool_Close"));
    }
    public void actionPerformedTraced(ActionEvent event) {
      closeAction();
    }
  }

  /*****************************************************************
   * I N T E R F A C E   M E T H O D  ---   V e t o a b l e I  *****
   ****************************************************************/
  public void addVetoRisibleI(VetoRisibleI vetoRisibleI) {
    if (vetoRisiblesV == null)
      vetoRisiblesV = new Vector();
    if (!vetoRisiblesV.contains(vetoRisibleI))
      vetoRisiblesV.addElement(vetoRisibleI);
  }

}
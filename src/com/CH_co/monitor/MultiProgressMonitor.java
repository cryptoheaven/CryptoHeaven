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

package com.CH_co.monitor;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import com.CH_co.gui.MyInsets;
import com.CH_co.util.*;

/**
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 * A class to monitor the progress of some operation(s). If it looks
 * like the operation will take a while, a progress window will be popped up.
 * When the ProgressMonitor is created it is given a numeric range and a
 * descriptive string. As the operation progresses, call the setProgress method
 * to indicate how far along the [min,max] range the operation is.
 * Initially, there is no ProgressDialog. After the first millisToDecideToPopup
 * milliseconds (default 500) the progress monitor will predict how long
 * the operation will take.  If it is longer than millisToPopup (default 2000,
 * 2 seconds) a ProgressDialog will be popped up.
 * <p>
 * From time to time, when the Dialog box is visible, the progress bar will
 * be updated when setProgress is called.  setProgress won't always update
 * the progress bar, it will only be done if the amount of progress is
 * visibly significant.
 * <p>
 * If more than one operation is in progress, multiple progress bars will show.
 *
 *
 * <b>$Revision: 1.2 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class MultiProgressMonitor extends Object {

  private static JFrame progressFrame;
  private static JPanel mainPanel;
  private static final Object synchro = new Object();
  private static final ArrayList monitorsL = new ArrayList();
  private static int yPos = 0;

  private static int MAX_GUI_MONITORS = 500;

  private ProgressPanel   panel;
  private JProgressBar    myBar;
  private Object          value;
  private JLabel          noteLabel;
  private Component       parentComponent;
  private String          note;
  private Object[]        cancelOption = null;
  private Object          message;
  private long            T0;
  private int             millisToDecideToPopup = 500;
  private int             millisToPopup = 2000;
  private int             min;
  private int             max;
  private int             v;
  private int             lastDisp;
  private int             reportDelta;
  private javax.swing.Timer timer;

  private static boolean ENABLE_DEBUG_CANCEL_NOTE = false;
  private StringBuffer debugBuffer;

  private static Component defaultParentComponent;

  /**
   * Constructs a graphic object that shows progress, typically by filling
   * in a rectangular bar as the process nears completion.
   *
   * @param parentComponent the parent component for the dialog box
   * @param message a descriptive message that will be shown
   *        to the user to indicate what operation is being monitored.
   *        This does not change as the operation progresses.
   *        See the message parameters to methods in
   *        {@link JOptionPane#message}
   *        for the range of values.
   * @param note a short note describing the state of the
   *        operation.  As the operation progresses, you can call
   *        setNote to change the note displayed.  This is used,
   *        for example, in operations that iterate through a
   *        list of files to show the name of the file being processes.
   *        If note is initially null, there will be no note line
   *        in the dialog box and setNote will be ineffective
   * @param min the lower bound of the range
   * @param max the upper bound of the range
   */
  public MultiProgressMonitor(Component parentComponent, Object message, String note, int min, int max) {
    this.min = min;
    this.max = max;
    this.parentComponent = parentComponent;

    cancelOption = new Object[1];
    cancelOption[0] = UIManager.getString("OptionPane.cancelButtonText");

    reportDelta = (max - min) / 100;
    if (reportDelta < 1) reportDelta = 1;
    v = min;
    this.message = message;
    this.note = note;
    T0 = System.currentTimeMillis();
    updateDebugNotes("NEW");
    timer = new javax.swing.Timer(500, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        synchronized (MultiProgressMonitor.this) {
          setProgressAWT(v);
        }
      }
    });
    timer.start();
  }

  private class ProgressPanel extends JPanel {
    ProgressPanel(Object messageList) {
      //super(messageList, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, MultiProgressMonitor.this.cancelOption, null);
      setLayout(new GridBagLayout());
      Object[] msgList = (Object[]) messageList;
      int yPos = 0;
      updateDebugNotes("init");
      for (int i=0; i<msgList.length; i++) {
        Component comp = null;
        if (msgList[i] instanceof String)
          comp = new JLabel((String) msgList[i]);
        else
          comp = (Component) msgList[i];
        yPos ++;
        int gridwidth = 2;
        if (i == msgList.length -1)
          gridwidth = 1;
        add(comp, new GridBagConstraints(0, yPos, gridwidth, 1, 1, 0,
          GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
      }
      JButton cancelButton = new JButton(""+cancelOption[0]+(ENABLE_DEBUG_CANCEL_NOTE ? " / Debug":""));
      add(cancelButton, new GridBagConstraints(1, yPos, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0, 10, 0, 0), 0, 0));
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          cancel();
        }
      });
    }

    public int getMaxCharactersPerLineCount() {
      return 60;
    }

    // Equivalent to JOptionPane.createDialog,
    // but create a modeless dialog.
    // This is necessary because the Solaris implementation doesn't
    // support Dialog.setModal yet.
    public JFrame createInFrame(Component parentComponent, String title) {
      JFrame inFrame = null;
      synchronized (synchro) {
        if (progressFrame == null) {
          yPos = 0;
          //Frame parentFrame = JOptionPane.getFrameForComponent(parentComponent);
          //inDialog = new JDialog(parentFrame, title, false);
          inFrame = new JFrame(title);
          ImageIcon frameIcon = Images.get(ImageNums.FRAME_LOCK32);
          if (frameIcon != null) {
            inFrame.setIconImage(frameIcon.getImage());
          }
          progressFrame = inFrame;
          mainPanel = new JPanel(new GridBagLayout());
          mainPanel.add(new JLabel(), new GridBagConstraints(0, MAX_GUI_MONITORS+1, 1, 1, 10, 10, 
            GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
          JScrollPane scrollPane = new JScrollPane(mainPanel);
          scrollPane.getVerticalScrollBar().setUnitIncrement(16);
          Container contentPane = inFrame.getContentPane();
          contentPane.setLayout(new BorderLayout());
          contentPane.add(scrollPane, BorderLayout.CENTER);
        }

        monitorsL.add(MultiProgressMonitor.this);
        updateTitle();
        mainPanel.add(this, new GridBagConstraints(0, yPos ++, 1, 1, 10, 0, 
          GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
        mainPanel.revalidate();
        mainPanel.repaint();
      }

      // if created a new progressFrame
      if (inFrame != null) {
        inFrame.pack();
        Dimension dim = inFrame.getSize();
        inFrame.setSize(dim.width + 100, dim.height);
        Component relativeComponent = parentComponent != null ? parentComponent : defaultParentComponent;
        if (relativeComponent != null) {
          inFrame.setLocationRelativeTo(relativeComponent);
          Frame relativeFrame = JOptionPane.getFrameForComponent(relativeComponent);
          if (relativeFrame != null) {
            inFrame.setState(relativeFrame.getState());
          }
        }
        inFrame.setVisible(true);
        inFrame.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent we) {
            synchronized (synchro) {
              for (int i=monitorsL.size()-1; i>=0; i--) {
                MultiProgressMonitor monitor = (MultiProgressMonitor) monitorsL.get(i);
                monitor.cancel();
              }
            }
          }
        });
      }

      return progressFrame;
    }
  }


  /**
   * Indicate the progress of the operation being monitored.
   * If the specified value is >= the maximum, the progress
   * monitor is closed.
   * @param nv an int specifying the current value, between the
   *        maximum and minimum specified for this component
   * @see #setMinimum
   * @see #setMaximum
   * @see #close
   */
  public void setProgress(final int nv) {
    try {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          setProgressAWT(nv);
        }
      });
    } catch (Throwable t) {
    }
  }
  private synchronized void setProgressAWT(int nv) {
    if (!isCanceled()) {
      v = nv;
      if (nv >= max) {
        // update bar to completion
        if (myBar != null) {
          myBar.setValue(nv);
          updateDebugNotes("done at "+nv);
        }
        // delayed closing -- stop the updating timer right away
        if (timer != null)
          timer.stop();
        // delay closing so user can catch a glimpse of completed progress bar
        new javax.swing.Timer(1000, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            ((javax.swing.Timer) e.getSource()).stop();
            closeAWT();
          }
        }).start();
      }
      else if (nv >= lastDisp + reportDelta || myBar == null) { // if visibly significant or need to determine if progress should be shown
        lastDisp = nv;
        if (myBar != null) {
          myBar.setValue(nv);
          updateDebugNotes(""+nv);
        }
        else {
          long T = System.currentTimeMillis();
          long dT = (int)(T-T0);
          if (dT >= millisToDecideToPopup) {
            int predictedCompletionTime;
            if (nv > min) {
              predictedCompletionTime = (int)((long)dT * (max - min) / (nv - min));
            }
            else {
              predictedCompletionTime = millisToPopup;
            }
            if (predictedCompletionTime >= millisToPopup && yPos < MAX_GUI_MONITORS) {
              myBar = new JProgressBar();
              myBar.setMinimum(min);
              myBar.setMaximum(max);
              myBar.setValue(nv);
              if (note != null) noteLabel = new JLabel(note);
              panel = new ProgressPanel(new Object[] {message, noteLabel, myBar});
              progressFrame = panel.createInFrame(parentComponent, UIManager.getString("ProgressMonitor.progressText"));
              updateDebugNotes(""+nv);
            } else {
              // If not yet time to show GUI progress bar, make sure we have a periodic task that will show it eventually
              // If progress was closed, this will restart the timer...
              if (!timer.isRunning())
                timer.restart();
            }
          }
        }
      }
    }
  }

  private void cancel() {
    if (ENABLE_DEBUG_CANCEL_NOTE)
      com.CH_co.util.MessageDialog.showInfoDialog(null, debugBuffer.toString(), "Debug Notes");
    value = cancelOption[0];
    close();
  }

  /**
   * Indicate that the operation is complete.  This happens automatically
   * when the value set by setProgress is >= max, but it may be called
   * earlier if the operation ends early.
   */
  public void close() {
    try {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          closeAWT();
        }
      });
    } catch (Throwable t) {
    }
  }
  private synchronized void closeAWT() {
    boolean removed = false;
    JFrame toDispose = null;
    if (timer != null) {
      timer.stop();
    }
    synchronized (synchro) {
      if (removed = monitorsL.remove(this)) {
        if (monitorsL.size() == 0) {
          if (progressFrame != null) {
            toDispose = progressFrame;
            progressFrame = null;
            yPos = 0;
          }
        }
      }
    }
    if (removed) {
      updateTitle();
      mainPanel.remove(panel);
      mainPanel.revalidate();
      mainPanel.repaint();
      panel.removeAll();
      panel = null;
      myBar = null;
      updateDebugNotes("close");
    }
    if (toDispose != null) {
      toDispose.setVisible(false);
      toDispose.dispose();
    }
  }

  private void updateDebugNotes(String str) {
    if (ENABLE_DEBUG_CANCEL_NOTE) {
      if (debugBuffer == null)
        debugBuffer = new StringBuffer();
      debugBuffer.append(new Date().toString());
      debugBuffer.append("\n");
      debugBuffer.append(Misc.getStack(new Throwable(str)));
      debugBuffer.append("\n\n");
    }
  }
  
  private void updateTitle() {
    JFrame frame = progressFrame;
    if (frame != null) {
      int size = monitorsL.size();
      String titleStr = UIManager.getString("ProgressMonitor.progressText");
      if (size > 1) {
        frame.setTitle(size + ": " + titleStr);
      } else {
        frame.setTitle(titleStr);
      }
    }
  }

  /**
   * Returns the minimum value -- the lower end of the progress value.
   *
   * @return an int representing the minimum value
   * @see #setMinimum
   */
  public synchronized int getMinimum() {
    return min;
  }

  /**
   * Specifies the minimum value.
   *
   * @param m  an int specifying the minimum value
   * @see #getMinimum
   */
  public synchronized void setMinimum(int m) {
    min = m;
  }

  /**
   * Returns the maximum value -- the higher end of the progress value.
   *
   * @return an int representing the maximum value
   * @see #setMaximum
   */
  public synchronized int getMaximum() {
    return max;
  }

  public static void setDefaultParentComponent(Component defaultParent) {
    defaultParentComponent = defaultParent;
  }

  /**
   * Specifies the maximum value.
   *
   * @param m  an int specifying the maximum value
   * @see #getMaximum
   */
  public synchronized void setMaximum(int m) {
    max = m;
  }

  /**
   * Returns true if the user hits the Cancel button in the progress window.
   */
  public boolean isCanceled() {
    return ((value != null) && (cancelOption.length == 1) && (value.equals(cancelOption[0])));
  }

  /**
   * Specifies the amount of time to wait before deciding whether or
   * not to popup a progress monitor.
   *
   * @param millisToDecideToPopup  an int specifying the time to wait,
   *        in milliseconds
   * @see #getMillisToDecideToPopup
   */
  public synchronized void setMillisToDecideToPopup(int millisToDecideToPopup) {
    this.millisToDecideToPopup = millisToDecideToPopup;
  }


  /**
   * Returns the amount of time this object waits before deciding whether
   * or not to popup a progress monitor.
   *
   * @param millisToDecideToPopup  an int specifying waiting time,
   *        in milliseconds
   * @see #setMillisToDecideToPopup
   */
  public synchronized int getMillisToDecideToPopup() {
    return millisToDecideToPopup;
  }

  /**
   * Specifies the amount of time it will take for the popup to appear.
   * (If the predicted time remaining is less than this time, the popup
   * won't be displayed.)
   *
   * @param millisToPopup  an int specifying the time in milliseconds
   * @see #getMillisToPopup
   */
  public synchronized void setMillisToPopup(int millisToPopup) {
    this.millisToPopup = millisToPopup;
  }

  /**
   * Returns the amount of time it will take for the popup to appear.
   *
   * @param millisToPopup  an int specifying the time in milliseconds
   * @see #setMillisToPopup
   */
  public synchronized int getMillisToPopup() {
    return millisToPopup;
  }

  /**
   * Specifies the additional note that is displayed along with the
   * progress message. Used, for example, to show which file the
   * is currently being copied during a multiple-file copy.
   *
   * @param note  a String specifying the note to display
   * @see #getNote
   */
  public synchronized void setNote(String note) {
    this.note = note;
    if (noteLabel != null) {
      noteLabel.setText(note);
    }
  }

  /**
   * Specifies the additional note that is displayed along with the
   * progress message.
   *
   * @return a String specifying the note to display
   * @see #setNote
   */
  public synchronized String getNote() {
    return note;
  }

  public static void main(String[] args) {
    int progCount = 0;

    final Vector monsV = new Vector();
    final Vector maxsV = new Vector();
    final Vector valuesV = new Vector();

    javax.swing.Timer timer = new javax.swing.Timer(1000, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        for (int i=0; i<monsV.size(); i++) {
          MultiProgressMonitor mon = (MultiProgressMonitor) monsV.elementAt(i);
          int max = ((Integer) maxsV.elementAt(i)).intValue();
          int value = ((Integer) valuesV.elementAt(i)).intValue();
          if (value < max) {
            mon.setProgress(value + 1);
            valuesV.setElementAt(Integer.valueOf(value + 1), i);
          }
        }
      }
    });
    timer.start();

    int i = 0;

    long t = System.currentTimeMillis();
    while (true) {
      try {
        while (true) {
          System.in.read();
          if (System.currentTimeMillis()-t > 100) break;
        }
        t = System.currentTimeMillis();
      } catch (IOException x) {
      }
      int max = new Random().nextInt(20)+6;
      MultiProgressMonitor mon = new MultiProgressMonitor(null, "Test Monitor "+monsV.size(), "total units "+max, 0, max);
      monsV.addElement(mon);
      maxsV.addElement(Integer.valueOf(max));
      valuesV.addElement(Integer.valueOf(0));
    }
  }

}
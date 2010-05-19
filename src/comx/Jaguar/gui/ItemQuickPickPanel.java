/*
 * ColorMenu.java
 *
 * Created on July 24, 2002, 4:04 PM
 */

package comx.Jaguar.gui;

import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.MyInsets;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import com.CH_co.gui.*;

/**
 *
 * @author  marcin
 * @version
 */

public class ItemQuickPickPanel extends JPanel {

  protected Border m_blackBorder;
  protected Border m_unselectedBorder;
  protected Border m_selectedBorder;
  protected Border m_activeBorder;

  protected Hashtable m_panes;
  protected SingleComponentPane m_selected;

  private String actionCommandName;
  private boolean isColorSet;

  public ItemQuickPickPanel(String name, int[] emotions) {
    init(name);
    initEmotionPane(emotions);
  }
  public ItemQuickPickPanel(String name) {
    init(name);
    initColorPane();
  }
  private void init(String name) {
    actionCommandName = name;
    m_blackBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1, getBackground()), new MatteBorder(1, 1, 1, 1, Color.black));
    m_unselectedBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1, getBackground()), new BevelBorder(BevelBorder.LOWERED, Color.white, Color.gray));
    m_selectedBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1, Color.red), new MatteBorder(1, 1, 1, 1, getBackground()));
    m_activeBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1, Color.blue), new MatteBorder(1, 1, 1, 1, getBackground()));
    m_panes = new Hashtable();
  }
  private void initColorPane() {
    JPanel p = new JPanel();
    p.setBorder(new EmptyBorder(0, 0, 0, 0));
    p.setLayout(new GridLayout(8, 8, 0, 0));

    Dimension singleComponentSize = new Dimension(16, 16);
    // predefined colors
    /*
    Color[] predefinedColors = new Color[] { 
          Color.black, null, Color.white, Color.blue, 
          Color.green, Color.orange, Color.red, Color.yellow };
    for (int i=0; i<predefinedColors.length; i++) {
      Color c = predefinedColors[i];
      SingleComponentPane pn = new SingleComponentPane(c, singleComponentSize);
      p.add(pn);
      if (c != null)
        m_panes.put(c, pn);
      else
        m_panes.put("null", pn);
    }
     */
    // other shades
    //int[] values = new int[] { 0, 85, 171, 255 };
    //int[] values = new int[] { 0x00, 0x33, 0x66, 0x99, 0xCC, 0xFF };
    int[] values = new int[] { 0x00, 0x33, 0x99, 0xFF };
    for (int r=0; r<values.length; r++) {
      for (int g=0; g<values.length; g++) {
        for (int b=0; b<values.length; b++) {
          Color c = new Color(values[r], values[g], values[b]);
          SingleComponentPane pn = new SingleComponentPane(c, singleComponentSize);
          p.add(pn);
          m_panes.put(c, pn);
        }
      }
    }
    add(p);
  }
  private void initEmotionPane(int[] emotions) {
    JPanel p = new JPanel();
    p.setBorder(new EmptyBorder(0, 0, 0, 0));
    p.setLayout(new GridLayout(5, 10));

    Dimension singleComponentSize = new Dimension(27, 27);
    for (int i=0; i<emotions.length; i++) {
      Icon icon = com.CH_gui.util.Images.get(emotions[i]);
      JLabel jIcon = new JMyLabel(icon);
      SingleComponentPane pn = new SingleComponentPane(Color.white, jIcon, new Integer(i), singleComponentSize);
      p.add(pn);
      m_panes.put(jIcon, pn);
    }
    add(p);
  }

  private String getActionCommand() {
    return actionCommandName;
  }

  /**
   * adds an ActionListener to the button
   */
  public void addActionListener(ActionListener l) {
    listenerList.add(ActionListener.class, l);
  }

  /**
   * Removes an ActionListener from the button.  If the listener is
   * the currently set Action for the button, then the Action
   * is set to null.
   */
  public void removeActionListener(ActionListener l) {
    listenerList.remove(ActionListener.class, l);
  }

  /*
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   * @see EventListenerList
   */
  protected void fireActionPerformed(ActionEvent event) {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();
    ActionEvent e = null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length-2; i>=0; i-=2) {
      if (listeners[i]==ActionListener.class) {
        // Lazily create the event:
        if (e == null) {
          String actionCommand = event.getActionCommand();
          if(actionCommand == null) {
            actionCommand = getActionCommand();
          }
          e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, actionCommand, event.getModifiers());
        }
        ((ActionListener)listeners[i+1]).actionPerformed(e);
      }
    }
  }

  public boolean isColorSet() {
    return isColorSet;
  }

  public void setColor(Color c) {
    isColorSet = true;
    Object obj = null;
    if (c != null)
      obj = m_panes.get(c);
    else
      obj = m_panes.get("null");
    if (obj == null)
      return;
    if (m_selected != null)
      m_selected.setSelected(false);
    m_selected = (SingleComponentPane) obj;
    m_selected.setSelected(true);
  }

  public Color getColor() {
    if (m_selected == null)
      return null;
    return m_selected.getColor();
  }

  public void setMainComp(Component c) {
    Object obj = m_panes.get(c);
    if (obj == null)
      return;
    if (m_selected != null)
      m_selected.setSelected(false);
    m_selected = (SingleComponentPane) obj;
    m_selected.setSelected(true);
  }

  public Component getMainComp() {
    if (m_selected == null)
      return null;
    return m_selected.getMainComp();
  }

  public Integer getMainCompIndex() {
    if (m_selected == null)
      return null;
    return m_selected.getMainCompIndex();
  }

  public void doSelection() {
    fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand()));
  }




  class SingleComponentPane extends JPanel implements MouseListener {
    protected boolean colorMode;
    protected Color m_c;
    protected Component m_mainComp;
    protected Integer m_index;
    protected boolean m_selected;
    protected Dimension preferredSize;

    public SingleComponentPane(Color c, Dimension prefSize) {
      colorMode = true;
      m_c = c;
      preferredSize = prefSize;
      if (c != null) {
        setBackground(c);
        setForeground(c);
        setBorder(m_unselectedBorder);
      } else {
        setBorder(m_blackBorder);
      }
      String msg = null;
      if (c != null)
        msg = "R " + c.getRed() + ", G " + c.getGreen() + ", B " + c.getBlue();
      else
        msg = "Reset to default";
      setToolTipText(msg);
      addMouseListener(this);
    }
    public SingleComponentPane(Color c, Component mainComponent, Integer mainComponentIndex, Dimension prefSize) {
      m_c = c;
      m_mainComp = mainComponent;
      m_index = mainComponentIndex;
      preferredSize = prefSize;
      setBackground(c);
      setBorder(m_unselectedBorder);
      setLayout(new GridBagLayout());
      add(mainComponent, new GridBagConstraints(0,0,1,1,10,10,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new MyInsets(0,0,0,0), 0,0));
      addMouseListener(this);
    }

    public void paint(Graphics g) {
      super.paint(g);
      if (colorMode && m_c != null) {
        g.fillRect(3, 3, preferredSize.width-6, preferredSize.height-6);
      }
    }

    public Color getColor() {
      return m_c;
    }

    public Component getMainComp() {
      return m_mainComp;
    }
    public Integer getMainCompIndex() {
      return m_index;
    }

    public Dimension getPreferredSize() {
      return preferredSize;
    }

    public Dimension getMaximumSize() {
      return getPreferredSize();
    }

    public Dimension getMinimumSize() {
      return getPreferredSize();
    }

    public void setSelected(boolean selected) {
      m_selected = selected;
      if (m_selected) {
        setBorder(m_selectedBorder);
      } else {
        if (m_c != null)
          setBorder(m_unselectedBorder);
        else
          setBorder(m_blackBorder);
      }
    }

    public boolean isSelected() {
      return m_selected;
    }

    public void mousePressed(MouseEvent e) {}

    public void mouseClicked(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {
      if (m_mainComp != null)
        setMainComp(m_mainComp);
      if (colorMode)
        setColor(m_c);
      MenuSelectionManager.defaultManager().clearSelectedPath();
      doSelection();
    }

    public void mouseEntered(MouseEvent e) {
      setBorder(m_activeBorder);
    }

    public void mouseExited(MouseEvent e) {
      if (m_c != null)
        setBorder(m_selected ? m_selectedBorder : m_unselectedBorder);
      else
        setBorder(m_selected ? m_selectedBorder : m_blackBorder);
    }
  }
}
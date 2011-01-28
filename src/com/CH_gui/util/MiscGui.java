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

package com.CH_gui.util;

import com.CH_co.trace.*;
import com.CH_co.util.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;

/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class MiscGui extends Object {

  private static boolean isAntiAliasingEnabled = false;
  private static boolean isAntiAliasingCapable = false;

  private static boolean isSmallScreen = false;

  public static final String ANTIALIASING_ENABLEMENT_PROPERTY = "antialiasingEnabled";

  /*
  private static String defaultFontName = null;
  private static Font defaultFont = null;
   */

  static {
    String version = System.getProperty("java.version");
    StringTokenizer st = new StringTokenizer(version, ".");
    int majorVersion = 0;
    int minorVersion = 0;
    if (st.hasMoreTokens()) {
      try {
        majorVersion = Integer.parseInt(st.nextToken());
      } catch (Throwable t) {
      }
    }
    if (st.hasMoreTokens()) {
      try {
        minorVersion = Integer.parseInt(st.nextToken());
      } catch (Throwable t) {
      }
    }
    isAntiAliasingCapable = majorVersion >= 2 || (majorVersion == 1 && minorVersion >= 4);
    isAntiAliasingEnabled = Boolean.valueOf(GlobalProperties.getProperty(ANTIALIASING_ENABLEMENT_PROPERTY, "true")).booleanValue();
  }

  private static final JTextComponent.KeyBinding[] defaultBindings = {
     new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.CTRL_MASK), DefaultEditorKit.copyAction),
     new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.SHIFT_MASK), DefaultEditorKit.pasteAction),
     new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_MASK), DefaultEditorKit.cutAction),
  };

  public static void setPaintPrefs(Graphics g) {
    if (isAntiAliasingCapable && isAntiAliasingEnabled && g instanceof Graphics2D) {
      ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
  }
  public static void setAntiAliasingEnabled(boolean enable) {
    isAntiAliasingEnabled = enable;
  }
  public static boolean isAntiAliasingCapable() {
    return isAntiAliasingCapable;
  }

  public static void setSmallScreen(boolean isSmall) {
    isSmallScreen = isSmall;
  }
  public static boolean isSmallScreen() {
    return isSmallScreen;
  }

  /*
  static {
    String[] availableFontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    String[] fontNames = new String[] { "Arial", "Verdana", "Helvetica", "SansSerif" };
    Font font = null;
    for (int i=0; i<fontNames.length; i++) {
      String fontName = fontNames[i];
      for (int k=0; k<availableFontNames.length; k++) {
        String availFontName = availableFontNames[k];
        if (fontName.equalsIgnoreCase(availFontName)) {
          defaultFontName = fontName;
          break;
        }
      }
      if (defaultFontName != null)
        break;
    }
  }
   */

  /**
   * load default bindings
   */
  public static void initKeyBindings(JTextComponent c) {
    Keymap k = c.getKeymap();
    JTextComponent.loadKeymap(k, defaultBindings, c.getActions());
  }

  /*
  public static Font getDefaultFont() {
    if (defaultFont == null)
      defaultFont = new Font(defaultFontName, Font.PLAIN, 12);
    return defaultFont;
  }
  public static String getDefaultFontName() {
    return defaultFontName;
  }
   */
  /**
   * sets default font
   */
  public static void setDefaultFont(Component c) {
    // no-op
    //c.setFont(getDefaultFont());
  }

  public static void setPlainFont(Component c) {
    Font oldFont = c.getFont();
    if (!oldFont.isPlain())
      c.setFont(oldFont.deriveFont(Font.PLAIN));
  }

  public static void setBoldFont(Component c) {
    Font oldFont = c.getFont();
    if (!oldFont.isBold())
      c.setFont(oldFont.deriveFont(Font.BOLD));
  }

  public static JPanel createButtonPanel(JButton[] buttons) {
    /* Add buttons so they are placed on the bottom right corner */
    JPanel buttonPanelBL = new JPanel(new BorderLayout());
    int cols = buttons.length;
    GridLayout grid = new GridLayout(1, cols);
    grid.setHgap(5);
    JPanel buttonPanel = new JPanel(grid);
    EmptyBorder border = new EmptyBorder(5,5,5,5);
    buttonPanel.setBorder(border);

    for (int i=0; i<cols; i++)
      if (buttons[i] != null)
        buttonPanel.add(buttons[i]);

    buttonPanelBL.add(buttonPanel, BorderLayout.EAST);
    return buttonPanelBL;
  }

  public static void setSuggestedWindowLocation(Component owner, Window child) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MiscGui.class, "getSuggestedWindowLocation(Component owner, Window child)");
    if (trace != null) trace.args(owner, child);

    Dimension parentDimension;
    Point parentLocation;
    if (owner != null) {
      parentDimension = owner.getSize();
      parentLocation = owner.getLocationOnScreen();
    } else {
      parentDimension = getScreenUsableSize(0, 0, child);
      parentLocation = new Point(0, 0);
    }
    Insets parentInsets = getScreenInsets(parentLocation.x, parentLocation.y, owner);

    Dimension childDimension = child.getSize();

    // get North-West corner of centralized figures in parent's coordinate system
    int x = (parentDimension.width - childDimension.width) / 2;
    int y = (parentDimension.height - childDimension.height) / 2;

    // move the coordinates to screen coordinates
    int screenX = parentLocation.x + x;
    int screenY = parentLocation.y + y;

    Point absolutePoint = new Point(screenX+Math.abs(parentInsets.left), screenY+Math.abs(parentInsets.top));
    child.setLocation(absolutePoint);
    adjustSizeAndLocationToFitScreen(child);

    if (trace != null) trace.exit(MiscGui.class);
  }

  private static Dimension getSuggestedWindowSizeToFitScreen(Window w) {
    Point p = w.getLocation();
    Dimension d = getScreenUsableSize(p.x, p.y, w);
    Dimension s = w.getSize();
    return new Dimension(Math.min(s.width, d.width), Math.min(s.height, d.height));
  }

  public static void adjustSizeAndLocationToFitScreen(Window w) {
    Dimension windowDim_orig = w.getSize();
    Dimension windowDim = getSuggestedWindowSizeToFitScreen(w);
    if (windowDim_orig.width != windowDim.width || windowDim_orig.height != windowDim.height)
      w.setSize(windowDim);
    Point p = w.getLocation();
    w.setLocation(adjustLocationToFitScreen(p.x, p.y, w));
  }

  public static Rectangle getScreenBounds(int xposition, int yposition, Component invoker) {
    Point p = new Point(xposition, yposition);
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Rectangle screenBounds;
    GraphicsConfiguration gc = null;
    // Try to find GraphicsConfiguration, that includes specified (x, y) position
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gd = ge.getScreenDevices();
    for (int i = 0; i < gd.length; i++) {
      if (gd[i].getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
        GraphicsConfiguration dgc = gd[i].getDefaultConfiguration();
        if (dgc.getBounds().contains(p)) {
          gc = dgc;
          break;
        }
      }
    }
    // If not found and we have invoker, ask invoker about his gc
    if (gc == null && invoker != null) {
      gc = invoker.getGraphicsConfiguration();
    }
    if (gc == null && invoker != null) {
      Container container = invoker.getParent();
      if (container != null)
        gc = container.getGraphicsConfiguration();
    }
    if (gc != null) {
      screenBounds = gc.getBounds();
    } else {
      screenBounds = new Rectangle(toolkit.getScreenSize());
    }
    return screenBounds;
  }
  public static Insets getScreenInsets(int xposition, int yposition, Component invoker) {
    Point p = new Point(xposition, yposition);
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Insets screenInsets;
    GraphicsConfiguration gc = null;
    // Try to find GraphicsConfiguration, that includes specified (x, y) position
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gd = ge.getScreenDevices();
    for (int i = 0; i < gd.length; i++) {
      if (gd[i].getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
        GraphicsConfiguration dgc = gd[i].getDefaultConfiguration();
        if (dgc.getBounds().contains(p)) {
          gc = dgc;
          break;
        }
      }
    }
    // If not found and we have invoker, ask invoker about his gc
    if (gc == null && invoker != null) {
      gc = invoker.getGraphicsConfiguration();
    }
    if (gc == null && invoker != null) {
      Container container = invoker.getParent();
      if (container != null)
        gc = container.getGraphicsConfiguration();
    }
    if (gc != null) {
      screenInsets = toolkit.getScreenInsets(gc);
    } else {
      screenInsets = new Insets(0, 0, 0, 0);
    }
    return screenInsets;
  }

  /**
   * Find screen size adjusted for insets;
   * @return
   */
  public static Dimension getScreenUsableSize(int xposition, int yposition, Component invoker) {
    Point p = new Point(xposition, yposition);
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Rectangle screenBounds;
    Insets screenInsets;
    GraphicsConfiguration gc = null;
    // Try to find GraphicsConfiguration, that includes specified (x, y) position
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gd = ge.getScreenDevices();
    for (int i = 0; i < gd.length; i++) {
      if (gd[i].getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
        GraphicsConfiguration dgc = gd[i].getDefaultConfiguration();
        if (dgc.getBounds().contains(p)) {
          gc = dgc;
          break;
        }
      }
    }
    // If not found and we have invoker, ask invoker about his gc
    if (gc == null && invoker != null) {
      gc = invoker.getGraphicsConfiguration();
    }
    if (gc == null && invoker != null) {
      Container container = invoker.getParent();
      if (container != null)
        gc = container.getGraphicsConfiguration();
    }
    if (gc != null) {
      // If we have GraphicsConfiguration use it to get
      // screen bounds and insets
      screenInsets = toolkit.getScreenInsets(gc);
      screenBounds = gc.getBounds();
    } else {
      // If we don't have GraphicsConfiguration use primary screen
      // and empty insets
      screenInsets = new Insets(0, 0, 0, 0);
      screenBounds = new Rectangle(toolkit.getScreenSize());
    }
    int scrWidth = screenBounds.width - Math.abs(screenInsets.left + screenInsets.right);
    int scrHeight = screenBounds.height - Math.abs(screenInsets.top + screenInsets.bottom);
    Dimension screenSize = new Dimension(scrWidth, scrHeight);
    return screenSize;
  }

  /**
     * Returns an point which has been adjusted to take into account of the
     * desktop bounds, taskbar and multi-monitor configuration.
     * <p>
     * This adustment code is from JPopupMenu.adjustPopupLocationToFitScreen()
     */
  private static Point adjustLocationToFitScreen(int xposition, int yposition, Component invoker) {
    Point p = new Point(xposition, yposition);

    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Rectangle screenBounds;
    Insets screenInsets;
    GraphicsConfiguration gc = null;
    // Try to find GraphicsConfiguration, that includes specified (x, y) position
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gd = ge.getScreenDevices();
    for (int i = 0; i < gd.length; i++) {
      if (gd[i].getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
        GraphicsConfiguration dgc = gd[i].getDefaultConfiguration();
        if (dgc.getBounds().contains(p)) {
          gc = dgc;
          break;
        }
      }
    }

    // If not found and we have invoker, ask invoker about his gc
    if (gc == null) {
      gc = invoker.getGraphicsConfiguration();
    }
    if (gc == null) {
      Container container = invoker.getParent();
      if (container != null)
        gc = container.getGraphicsConfiguration();
    }

    if (gc != null) {
      // If we have GraphicsConfiguration use it to get
      // screen bounds and insets
      screenInsets = toolkit.getScreenInsets(gc);
      screenBounds = gc.getBounds();
    } else {
      // If we don't have GraphicsConfiguration use primary screen
      // and empty insets
      screenInsets = new Insets(0, 0, 0, 0);
      screenBounds = new Rectangle(toolkit.getScreenSize());
    }

    int scrWidth = screenBounds.width - Math.abs(screenInsets.left + screenInsets.right);
    int scrHeight = screenBounds.height - Math.abs(screenInsets.top + screenInsets.bottom);

    Dimension size = invoker.getSize();

    if ((p.x + size.width) > screenBounds.x + scrWidth) {
      p.x = screenBounds.x + scrWidth - size.width;
    }

    if ((p.y + size.height) > screenBounds.y + scrHeight) {
      p.y = screenBounds.y + scrHeight - size.height;
    }

    /* Change is made to the desired (X,Y) values, when the
       Component is too tall OR too wide for the screen */
    if (p.x < screenBounds.x) {
      p.x = screenBounds.x;
    }
    if (p.y < screenBounds.y) {
      p.y = screenBounds.y;
    }

    return p;
  }

  private static Point lastSuggestedSpreadedLocation;
  public static Point getSuggestedSpreadedWindowLocation(Component child) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MiscGui.class, "getSuggestedSpreadedWindowLocation(Component child)");
    if (trace != null) trace.args(child);

    Point p = child.getLocation();
    Rectangle parentBounds = getScreenBounds(p.x, p.y, child);

    Dimension childDimension = child.getSize();

    Point absolutePoint = null;
    if (lastSuggestedSpreadedLocation == null) {
      absolutePoint = new Point(parentBounds.x, parentBounds.y);
    } else {
      if (lastSuggestedSpreadedLocation.x + childDimension.width + 30 > parentBounds.width ||
          lastSuggestedSpreadedLocation.y + childDimension.height + 30 > parentBounds.height)
        absolutePoint = new Point(parentBounds.x, parentBounds.y);
      else
        absolutePoint = new Point(lastSuggestedSpreadedLocation.x + 30, lastSuggestedSpreadedLocation.y + 30);
    }
    lastSuggestedSpreadedLocation = absolutePoint;

    if (trace != null) trace.exit(MiscGui.class, absolutePoint);
    return absolutePoint;
  }

  /**
   * Removes all components from the container 2 levels down.
   * Also removes all components from given conteiner's containers.
   */
  /*
  public static void removeAllComponents(Container cont) {
    Component[] components = cont.getComponents();

    Component comp;

    for (int i=0; i<components.length; i++) {
      comp = components[i];

      if (comp != null) {
        if (comp instanceof Container) {
          removeAllComponents((Container) comp);
        }

        comp.transferFocus();
        cont.remove(comp);
      }
    }
  }
   */


  /**
   * Removes all components in the tree and their listeners.
   * Called to aid in garbage collection.
   */
  public static void removeAllComponentsAndListeners(Component c) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MiscGui.class, "removeAllComponentsAndListeners(Component c)");
    if (trace != null) trace.args(c);
    try {
      removeAllListenersRecur(c);
    } catch (Throwable t) {
      if (trace != null) trace.data(200, "small issue while removing listeners");
    }
    try {
      removeAllComponentsRecur(c);
    } catch (Throwable t) {
      if (trace != null) trace.data(300, "small issue while removing components");
    }
    if (trace != null) trace.exit(MiscGui.class);
  }

  private static void removeAllListenersRecur(Component c) {
    try {
      if (c != null) {
        // special treatment for menus
        if (c instanceof MenuElement) {
          removeAllMenuListenersRecur((MenuElement) c);
        } else {
          // Remove all listeners from parents to children order.
          removeAllListeners(c);
          // Go into containers looking for child components.
          if (c instanceof ComponentContainerI) {
            ComponentContainerI cont = (ComponentContainerI) c;
            Component[] components = cont.getPotentiallyHiddenComponents();
            removeAllListenersRecur2(components);
          }
          if (c instanceof Container) {
            Container cont = (Container) c;
            Component[] components = cont.getComponents();
            removeAllListenersRecur2(components);
          }
        }
      } // end if (c != null)
    } catch (Throwable t) {
      // Removing listeners encountered small problem, not a big deal, continue normally.
    }
  } // end removeAllListenersRecur()
  private static void removeAllListenersRecur2(Component[] components) {
    if (components != null && components.length > 0) {
      for (int i=0; i<components.length; i++) {
        Component comp = components[i];
        if (comp != null)
          removeAllListenersRecur(comp);
      } // end for
    }
  }



  private static void removeAllComponentsRecur(Component c) {
    try {
      if (c != null) {
        // special treatment for menus elements
        if (c instanceof MenuElement) {
          removeAllMenuComponentsRecur((MenuElement) c);
        } else {
          // Go into containers looking for child components.
          if (c instanceof ComponentContainerI) {
            ComponentContainerI cont = (ComponentContainerI) c;
            Component[] components = cont.getPotentiallyHiddenComponents();
            removeAllComponentsRecur2(null, components);
          }
          if (c instanceof Container) {
            Container cont = (Container) c;
            Component[] components = cont.getComponents();
            removeAllComponentsRecur2(cont, components);
          }
        }
      }
    } catch (Throwable t) {
      // Removing components encountered small problem, not a big deal, continue normally.");
      // noop
    }
  } // end removeAllComponentsRecur()
  private static void removeAllComponentsRecur2(Container parent, Component[] components) {
    if (components != null && components.length > 0) {
      for (int i=0; i<components.length; i++) {
        Component comp = components[i];
        if (comp != null) {
          removeAllComponentsRecur(comp);
          // skip menu bars, they have problems later on with popup windows not showing
          if (!(comp instanceof JMenuBar)) {
            boolean componentHandled = false;
            try {
              if (comp instanceof JTextComponent) {
                componentHandled = true;
                JTextComponent textComp = (JTextComponent) comp;
                Keymap keymap = textComp.getKeymap();
                textComp.setKeymap(null);
                if (keymap != null)
                  textComp.removeKeymap(keymap.getName());
                textComp.setActionMap(null);
                textComp.setAutoscrolls(false);
                textComp.setBorder(null);
                textComp.setCaret(null);
                textComp.setDropTarget(null);
                textComp.setFont(null);
                textComp.setHighlighter(null);
                textComp.setLayout(null);
              } else if (comp instanceof AbstractButton) {
                componentHandled = true;
                AbstractButton button = (AbstractButton) comp;
                button.setAction(null);
                button.setActionCommand(null);
                button.setActionMap(null);
                button.setAutoscrolls(false);
                button.setBorder(null);
                button.setCursor(null);
                button.setDisabledIcon(null);
                button.setDisabledSelectedIcon(null);
                button.setDropTarget(null);
                button.setFont(null);
                button.setIcon(null);
                button.setInputVerifier(null);
                button.setLayout(null);
                button.setMargin(null);
                //button.setModel(null); // this caused NullPointerException when button was dissasembled but someone called .setEnabled() on it
                button.setPressedIcon(null);
                button.setRolloverIcon(null);
                button.setRolloverSelectedIcon(null);
                button.setSelectedIcon(null);
              }
            } catch (Throwable t) {
              // noop
            }

            if (componentHandled) {
              // Remove component from its parent next (children get removed first, bottom-up order)
              if (parent != null)
                parent.remove(comp);

              if (comp.isVisible())
                comp.setVisible(false);
              if (comp.hasFocus())
                comp.transferFocus();
            }
          }
        }
      } // end for
    }
    if (parent != null)
      parent.removeAll();
  }


  private static void removeAllMenuListenersRecur(MenuElement menuElement) {
    try {
      if (menuElement != null) {
        Component c = menuElement.getComponent();
        removeAllListeners(c);
        MenuElement[] subElements = menuElement.getSubElements();
        if (subElements != null) {
          for (int i=0; i<subElements.length; i++) {
            removeAllMenuListenersRecur(subElements[i]);
          }
        }
      }
    } catch (Throwable t) {
    }
  }
  private static void removeAllMenuComponentsRecur(MenuElement menuElement) {
    try {
      if (menuElement != null) {
        Component c = menuElement.getComponent();
        MenuElement[] subElements = menuElement.getSubElements();
        if (subElements != null) {
          for (int i=subElements.length-1; i>=0; i--) {
            removeAllMenuComponentsRecur(subElements[i]);
          }
        }
        if (c instanceof Container) {
          Container menuContainer = (Container) c;
          Component[] components = menuContainer.getComponents();
          if (components != null) {
            for (int i=0; i<components.length; i++) {
              Component comp = components[i];
              if (comp instanceof DisposableObj) {
                ((DisposableObj) comp).disposeObj();
              }
            }
          }
          menuContainer.removeAll();
        }
        if (c instanceof DisposableObj) {
          ((DisposableObj) c).disposeObj();
        }
      }
    } catch (Throwable t) {
      // noop
    }
  }

  private static void removeAllListeners(Component c) {
    if (c != null) {
      KeyListener[] keyListeners = (KeyListener[]) c.getListeners(KeyListener.class);
      if (keyListeners != null && keyListeners.length > 0)
        for (int i=0; i<keyListeners.length; i++)
          c.removeKeyListener(keyListeners[i]);

      MouseListener[] mouseListeners = (MouseListener[]) c.getListeners(MouseListener.class);
      if (mouseListeners != null && mouseListeners.length > 0)
        for (int i=0; i<mouseListeners.length; i++)
          c.removeMouseListener(mouseListeners[i]);

      MouseMotionListener[] mouseMotionListeners = (MouseMotionListener[]) c.getListeners(MouseMotionListener.class);
      if (mouseMotionListeners != null && mouseMotionListeners.length > 0)
        for (int i=0; i<mouseMotionListeners.length; i++)
          c.removeMouseMotionListener(mouseMotionListeners[i]);

      ComponentListener[] componentListeners = (ComponentListener[]) c.getListeners(ComponentListener.class);
      if (componentListeners != null && componentListeners.length > 0)
        for (int i=0; i<componentListeners.length; i++)
          c.removeComponentListener(componentListeners[i]);

      FocusListener[] focusListeners = (FocusListener[]) c.getListeners(FocusListener.class);
      if (focusListeners != null && focusListeners.length > 0)
        for (int i=0; i<focusListeners.length; i++)
          c.removeFocusListener(focusListeners[i]);

      HierarchyBoundsListener[] hierarchyBoundsListeners = (HierarchyBoundsListener[]) c.getListeners(HierarchyBoundsListener.class);
      if (hierarchyBoundsListeners != null && hierarchyBoundsListeners.length > 0)
        for (int i=0; i<hierarchyBoundsListeners.length; i++)
          c.removeHierarchyBoundsListener(hierarchyBoundsListeners[i]);

      HierarchyListener[] hierarchyListeners = (HierarchyListener[]) c.getListeners(HierarchyListener.class);
      if (hierarchyListeners != null && hierarchyListeners.length > 0)
        for (int i=0; i<hierarchyListeners.length; i++)
          c.removeHierarchyListener(hierarchyListeners[i]);

      InputMethodListener[] inputMethodListeners = (InputMethodListener[]) c.getListeners(InputMethodListener.class);
      if (inputMethodListeners != null && inputMethodListeners.length > 0)
        for (int i=0; i<inputMethodListeners.length; i++)
          c.removeInputMethodListener(inputMethodListeners[i]);

      PropertyChangeListener[] propertyChangeListeners = (PropertyChangeListener[]) c.getListeners(PropertyChangeListener.class);
      if (propertyChangeListeners != null && propertyChangeListeners.length > 0)
        for (int i=0; i<propertyChangeListeners.length; i++)
          c.removePropertyChangeListener(propertyChangeListeners[i]);

      propertyChangeListeners = c.getPropertyChangeListeners();
      if (propertyChangeListeners != null && propertyChangeListeners.length > 0)
        for (int i=0; i<propertyChangeListeners.length; i++)
          c.removePropertyChangeListener(propertyChangeListeners[i]);

      if (c instanceof Container) {
        Container container = (Container) c;
        ContainerListener[] containerListeners = (ContainerListener[]) container.getListeners(ContainerListener.class);
        if (containerListeners != null && containerListeners.length > 0)
          for (int i=0; i<containerListeners.length; i++)
            container.removeContainerListener(containerListeners[i]);
      }

      if (c instanceof PropertyDrivenItem) {
        PropertyDrivenItem propertyDrivenItem = (PropertyDrivenItem) c;
        propertyDrivenItem.removePropertyChangeListener();
        propertyDrivenItem.setAction(null);
      }

      if (c instanceof AbstractButton) {
        AbstractButton a = (AbstractButton) c;

        ActionListener[] actionListeners = (ActionListener[]) a.getListeners(ActionListener.class);
        if (actionListeners != null && actionListeners.length > 0)
          for (int i=0; i<actionListeners.length; i++)
            a.removeActionListener(actionListeners[i]);

        ChangeListener[] changeListeners = (ChangeListener[]) a.getListeners(ChangeListener.class);
        if (changeListeners != null && changeListeners.length > 0)
          for (int i=0; i<changeListeners.length; i++)
            a.removeChangeListener(changeListeners[i]);

        ItemListener[] itemListeners = (ItemListener[]) a.getListeners(ItemListener.class);
        if (itemListeners != null && itemListeners.length > 0)
          for (int i=0; i<itemListeners.length; i++)
            a.removeItemListener(itemListeners[i]);
      }

      if (c instanceof JComboBox) {
        JComboBox cb = (JComboBox) c;

        ActionListener[] actionListeners = (ActionListener[]) cb.getListeners(ActionListener.class);
        if (actionListeners != null && actionListeners.length > 0)
          for (int i=0; i<actionListeners.length; i++)
            cb.removeActionListener(actionListeners[i]);

        ItemListener[] itemListeners = (ItemListener[]) cb.getListeners(ItemListener.class);
        if (itemListeners != null && itemListeners.length > 0)
          for (int i=0; i<itemListeners.length; i++)
            cb.removeItemListener(itemListeners[i]);
      }

      if (c instanceof JComponent) {
        JComponent jComponent = (JComponent) c;

        AncestorListener[] ancestorListeners = (AncestorListener[]) jComponent.getListeners(AncestorListener.class);
        if (ancestorListeners != null && ancestorListeners.length > 0)
          for (int i=0; i<ancestorListeners.length; i++)
            jComponent.removeAncestorListener(ancestorListeners[i]);

        if (c instanceof AbstractButton) {
          AbstractButton button = (AbstractButton) c;
          ActionListener[] actionListeners = (ActionListener[]) button.getListeners(ActionListener.class);
          if (actionListeners != null && actionListeners.length > 0)
            for (int i=0; i<actionListeners.length; i++)
              button.removeActionListener(actionListeners[i]);

          ChangeListener[] changeListeners = (ChangeListener[]) button.getListeners(ChangeListener.class);
          if (changeListeners != null && changeListeners.length > 0)
            for (int i=0; i<changeListeners.length; i++)
              button.removeChangeListener(changeListeners[i]);

          ItemListener[] itemListeners = (ItemListener[]) button.getListeners(ItemListener.class);
          if (itemListeners != null && itemListeners.length > 0)
            for (int i=0; i<itemListeners.length; i++)
              button.removeItemListener(itemListeners[i]);
        }

        else if (c instanceof JComboBox) {
          JComboBox comboBox = (JComboBox) c;
          ActionListener[] actionListeners = (ActionListener[]) comboBox.getListeners(ActionListener.class);
          if (actionListeners != null && actionListeners.length > 0)
            for (int i=0; i<actionListeners.length; i++)
              comboBox.removeActionListener(actionListeners[i]);

          ItemListener[] itemListeners = (ItemListener[]) comboBox.getListeners(ItemListener.class);
          if (itemListeners != null && itemListeners.length > 0)
            for (int i=0; i<itemListeners.length; i++)
              comboBox.removeItemListener(itemListeners[i]);
        }

        else if (c instanceof JTextComponent) {
          JTextComponent textComp = (JTextComponent) c;
          textComp.getKeymap().removeBindings();

          if (textComp instanceof JTextField) {
            JTextField textField = (JTextField) textComp;
            ActionListener[] actionListeners = (ActionListener[]) textField.getListeners(ActionListener.class);
            if (actionListeners != null && actionListeners.length > 0)
              for (int i=0; i<actionListeners.length; i++)
                textField.removeActionListener(actionListeners[i]);
          }

          CaretListener[] caretListeners = (CaretListener[]) textComp.getListeners(CaretListener.class);
          if (caretListeners != null && caretListeners.length > 0)
            for (int i=0; i<caretListeners.length; i++)
              textComp.removeCaretListener(caretListeners[i]);

          Document doc = textComp.getDocument();
          if (doc != null && doc instanceof AbstractDocument) {
            AbstractDocument aDoc = (AbstractDocument) doc;
            DocumentListener[] docListeners = (DocumentListener[]) aDoc.getListeners(DocumentListener.class);
            if (docListeners != null && docListeners.length > 0)
              for (int i=0; i<docListeners.length; i++)
                aDoc.removeDocumentListener(docListeners[i]);

            UndoableEditListener[] undoListeners = (UndoableEditListener[]) aDoc.getListeners(UndoableEditListener.class);
            if (undoListeners != null && undoListeners.length > 0)
              for (int i=0; i<undoListeners.length; i++)
                aDoc.removeUndoableEditListener(undoListeners[i]);
          }
        }

        else if (c instanceof JTable) {
          JTable table = (JTable) c;
          removeAllListeners(table.getSelectionModel());
          removeAllListeners(table.getModel());

          /*
          if (table instanceof JSortedTable) {
            JSortedTable sTable = (JSortedTable) table;
            removeAllListeners(sTable.getRawModel());

            TableModel tableModel = sTable.getModel();
            if (tableModel instanceof TableMap) {
              TableMap tMap = (TableMap) tableModel;
              tMap.removeTableModelSortListeners();
            }
          }
           */
        }

        else if (c instanceof JList) {
          JList list = (JList) c;
          removeAllListeners(list.getSelectionModel());
          ListModel listModel = list.getModel();
          if (listModel != null && listModel instanceof AbstractListModel) {
            AbstractListModel aListModel = (AbstractListModel) listModel;
            ListDataListener[] listeners = (ListDataListener[]) aListModel.getListeners(ListDataListener.class);
            if (listeners != null && listeners.length > 0)
              for (int i=0; i<listeners.length; i++)
                aListModel.removeListDataListener(listeners[i]);
          }
        }

      } // end if JComponent

      if (c instanceof DisposableObj) {
        DisposableObj disposableObj = (DisposableObj) c;
        disposableObj.disposeObj();
      }
      /*
      if (c instanceof ActionProducerI) {
        ActionProducerI actionProducer = (ActionProducerI) c;
        actionProducer.clearActions();
      }
       */

    } // end if != null
  } // end removeAllListeners()

  private static void removeAllListeners(ListSelectionModel listSelectionModel) {
    if (listSelectionModel != null && listSelectionModel instanceof DefaultListSelectionModel) {
      DefaultListSelectionModel dListSelectionModel = (DefaultListSelectionModel) listSelectionModel;
      ListSelectionListener[] listeners = (ListSelectionListener[]) dListSelectionModel.getListeners(ListSelectionListener.class);
      if (listeners != null && listeners.length > 0)
        for (int i=0; i<listeners.length; i++)
          dListSelectionModel.removeListSelectionListener(listeners[i]);
    }
  }

  private static void removeAllListeners(TableModel tableModel) {
    if (tableModel != null && tableModel instanceof AbstractTableModel) {
      AbstractTableModel aTableModel = (AbstractTableModel) tableModel;
      TableModelListener[] listeners = (TableModelListener[]) aTableModel.getListeners(TableModelListener.class);
      if (listeners != null && listeners.length > 0)
        for (int i=0; i<listeners.length; i++)
          aTableModel.removeTableModelListener(listeners[i]);
    }
  }


  public static void storeVisualsSavable(Component c) {
    boolean traverseContainers = true;
    if (c instanceof VisualsSavable) {
      VisualsSavable v = (VisualsSavable) c;
      String vs = v.getVisuals();
      if (vs != null) {
        String name = getVisualsKeyName(v);
        if (name != null)
          GlobalProperties.setProperty(name, vs);
      }
      traverseContainers = v.isVisuallyTraversable();
    }
    if (traverseContainers && c instanceof Container) {
      Container cont = (Container) c;
      Component[] children = cont.getComponents();
      for (int i=0; i<children.length; i++) {
        storeVisualsSavable(children[i]);
      }
    }
  }

  /**
   * @return key name for GlobalProperties used to store visuals data
   */
  public static String getVisualsKeyName(VisualsSavable v) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MiscGui.class, "getVisualsKeyName(VisualsSavable v)");
    if (trace != null) trace.args(v);
    String extension = v.getExtension();
    String name = getVisualsKeyName(v, extension);
    if (trace != null) trace.exit(MiscGui.class, name);
    return name;
  }

  public static String getVisualsKeyName(VisualsSavable versionedVisual, String propertyName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MiscGui.class, "getVisualsKeyName(VisualsSavable versionedVisual, String propertyName)");
    if (trace != null) trace.args(versionedVisual, propertyName);
    String name = null;
    String visualsClassKeyName = versionedVisual.getVisualsClassKeyName();
    if (visualsClassKeyName != null)
      name = getVisualsKeyName(visualsClassKeyName, versionedVisual.getVisualsVersion(), propertyName);
    if (trace != null) trace.exit(MiscGui.class, name);
    return name;
  }
  public static String getVisualsKeyName(String visualsClassKeyName, Integer visualsVersion, String propertyName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MiscGui.class, "getVisualsKeyName(String visualsClassKeyName, Integer visualsVersion, String propertyName)");
    if (trace != null) trace.args(visualsClassKeyName, visualsVersion, propertyName);
    String name = visualsClassKeyName;
    if (propertyName != null && propertyName.length() > 0)
      name += "_" + propertyName;
    name += "_VS";
    if (visualsVersion != null) {
      name += "_Ver" + visualsVersion;
    }
    if (trace != null) trace.exit(MiscGui.class, name);
    return name;
  }

  /**
   * Gets recursively Components from all Components that descend from <code> c </code>.
   */
  private static void fillComponentsRecursively(Component c, java.util.List targetList, Class instanceOf) {
    if (c != null) {
      if (!targetList.contains(c)) {
        if (instanceOf == null || instanceOf.isAssignableFrom(c.getClass()))
          targetList.add(c);
      }
      Component[] components = null;
      for (int i=0; i<2; i++) {
        if (i == 0 && c instanceof ComponentContainerI) {
          components = ((ComponentContainerI)c).getPotentiallyHiddenComponents();
        } else if (i == 1 && c instanceof Container) {
          components = ((Container)c).getComponents();
        }
        if (components != null) {
          for (int k=0; k<components.length; k++) {
            Component compK = components[k];
            if (compK != null) {
              fillComponentsRecursively(compK, targetList, instanceOf);
            }
          }
        }
      }
    }
  }


  /**
   * Gets recursively all Components that <code> c </code> contains
   * merges them and returns as an array of Components.
   */
  public static Component[] getComponentsRecursively(Component c) {
    return (Component[]) getComponentsRecursively(c, null);
  }
  public static Object[] getComponentsRecursively(Component c, Class instanceOf) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MiscGui.class, "getComponentsRecursively(Component c, Class instanceOf)");
    LinkedList targetList = new LinkedList();
    fillComponentsRecursively(c, targetList, instanceOf);
    Object[] componentArray = ArrayUtils.toArray(targetList, instanceOf == null ? Component.class : instanceOf);
    int length = componentArray != null ? componentArray.length : 0;
    if (trace != null) trace.exit(MiscGui.class, length);
    return componentArray;
  }

  public static JSplitPane getParentSplitPane(Component c) {
    JSplitPane splitPane = null;
    Container cont = c.getParent();
    if (cont != null) {
      while (true) {
        if (cont == null)
          break;
        else if (cont instanceof JSplitPane) {
          splitPane = (JSplitPane) cont;
          break;
        }
        cont = cont.getParent();
      }
    }
    return splitPane;
  }

}
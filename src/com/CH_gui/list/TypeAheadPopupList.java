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

package com.CH_gui.list;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

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
 * <b>$Revision: 1.9 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class TypeAheadPopupList extends Object implements KeyListener, StringHighlighterI, ListUpdatableI, DisposableObj {

  private JTextComponent owner = null;
  private JScrollPane listPane = null;
  private JList list = null;
  private DefaultListModel fullListModel = null;

  private ObjectsProviderUpdaterI provider;
  private ObjectsProviderUpdaterI secondaryProvider;
  private boolean isSingleItemList;
  private boolean isSecondaryProviderDefaultable;
  private javax.swing.Timer noFocusUnPopTimer;
  private ActionListener noFocusUnPopActionListener;
  private String lastNonFocusPopText = null;

  JLayeredPane layeredPane;

  private boolean searchStringChanged = false;
  private boolean searchStringChangeDispatching = false;
  private javax.swing.Timer searchStringTimer;
  private ActionListener searchStringActionListener;

  private String[] searchExcludedStrs = null;
  private String searchString = null;
  private boolean forceNextBroadSearchWithOriginalOrdering = true;
  private static int searchMatchBITS = 
      StringHighlighter.MATCH_STRING__EXACT |
      StringHighlighter.MATCH_STRING__TRIMMED |
      StringHighlighter.MATCH_STRING__NO_CASE |
      StringHighlighter.MATCH_STRING__LEADING_TOKENS |
      StringHighlighter.MATCH_STRING__SEQUENCED_TOKENS;

  /** Creates new TypeAheadPopupList */
  public TypeAheadPopupList(ObjectsProviderUpdaterI provider) {
    this(provider, null);
  }
  public TypeAheadPopupList(ObjectsProviderUpdaterI provider, ObjectsProviderUpdaterI secondaryProvider) {
    this(provider, secondaryProvider, false, false);
  }
  public TypeAheadPopupList(ObjectsProviderUpdaterI provider, boolean isSingleItemList) {
    this(provider, null, isSingleItemList, false);
  }
  public TypeAheadPopupList(ObjectsProviderUpdaterI provider, ObjectsProviderUpdaterI secondaryProvider, boolean isSingleItemList, boolean isSecondaryProviderDefaultable) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TypeAheadPopupList.class, "TypeAheadPopupList()");
    this.provider = provider;
    this.secondaryProvider = secondaryProvider;
    this.isSingleItemList = isSingleItemList;
    this.isSecondaryProviderDefaultable = isSecondaryProviderDefaultable;
    if (trace != null) trace.exit(TypeAheadPopupList.class);
  }

  private void adjustListSize() {
    Dimension dimension = list.getPreferredSize();
    int height = 200;
    int width = owner.getSize().width; //list.getFixedCellWidth();
    int h = Math.min(dimension.height, height);
    int w = Math.min(dimension.width+20, width);
    h += 2;
    listPane.setSize(w, h);
    listPane.validate();
  }

  private void initializeList(final JTextComponent source) {
    if (list == null) {

      if (secondaryProvider != null) {
        secondaryProvider.registerForUpdates(new ListUpdatableI() {
          public void update(Object[] objs) {
            synchronized (TypeAheadPopupList.this) {
              selectedFromList(objs[0], !isSingleItemList);
            }
          }
        });
      }

      Object[] objs = provider.provide(null, this);
      DefaultListModel listModel = new DefaultListModel();
      for (int i=0; i<objs.length; i++)
        listModel.addElement(objs[i]);
      if (secondaryProvider != null)
        listModel.addElement(secondaryProvider);
      list = new JList(listModel);
      list.setVisibleRowCount(10);
      list.setAutoscrolls(true);
      list.addFocusListener(new FocusAdapter() {
        public void focusGained(FocusEvent e) {
          synchronized (TypeAheadPopupList.this) {
            owner.requestFocus();
          }
        }
      });
      list.addMouseMotionListener(new MouseMotionAdapter() {
        public void mouseMoved(MouseEvent e) {
          synchronized (TypeAheadPopupList.this) {
            Point location = e.getPoint();
            int index = list.locationToIndex(location);
            if (index >= 0)
              list.setSelectedIndex(index);
          }
        }
      });
      list.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          synchronized (TypeAheadPopupList.this) {
            Point location = e.getPoint();
            int index = list.locationToIndex(location);
            if (index >= 0) {
              list.setSelectedIndex(index);
              selectFromList(index, !isSingleItemList);
            }
          }
        }
      });
      list.setCellRenderer(new ListRenderer(false, true, true, this));
      fullListModel = new DefaultListModel();
      for (int i=0; i<objs.length; i++)
        fullListModel.addElement(objs[i]);
      if (secondaryProvider != null)
        fullListModel.addElement(secondaryProvider);
      listPane = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      listPane.setVisible(false);
      Dimension dimension = listPane.getPreferredSize();
      listPane.setSize(new Dimension((int)((double)dimension.width * 1.1000000000000001D), dimension.height));
      listPane.setBorder(new LineBorder(Color.blue, 1));
    }
    if (owner == null) {
      owner = source;
      owner.addKeyListener(new ListNavigator(list));
      owner.addFocusListener(new FocusListener() {
        public void focusGained(FocusEvent e) {
          synchronized (TypeAheadPopupList.this) {
            go(owner, Boolean.TRUE);
          }
        }
        public void focusLost(FocusEvent e) {
          synchronized (TypeAheadPopupList.this) {
            startFocusLostUnPopSequence();
          }
        }
      });
      owner.addHierarchyBoundsListener(new HierarchyBoundsListener() {
        public void ancestorMoved(HierarchyEvent e) {
          synchronized (TypeAheadPopupList.this) {
            if (listPane.isVisible()) {
              adjustListSize();
              adjustListLocation();
            }
          }
        }
        public void ancestorResized(HierarchyEvent e) {
          synchronized (TypeAheadPopupList.this) {
            if (listPane.isVisible()) {
              adjustListSize();
              adjustListLocation();
            }
          }
        }
      });
      owner.addHierarchyListener(new HierarchyListener() {
        public void hierarchyChanged(HierarchyEvent e) {
          synchronized (TypeAheadPopupList.this) {
            if (listPane.isVisible()) {
              adjustListSize();
              adjustListLocation();
            }
          }
        }
      });
      Window w = SwingUtilities.windowForComponent(owner);
      w.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          synchronized (TypeAheadPopupList.this) {
            disposeObj();
          }
        }
      });
      // workaround for switching windows and re-activating this window clicking on different component than owner and "quietly" transfering focus away from "owner"
      if (noFocusUnPopTimer == null) {
        noFocusUnPopActionListener = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            synchronized (TypeAheadPopupList.this) {
              if (!owner.hasFocus())
                startFocusLostUnPopSequence();
            }
          }
        };
        noFocusUnPopTimer = new javax.swing.Timer(1000, noFocusUnPopActionListener);
      }
      noFocusUnPopTimer.start();
    }
  }

  private synchronized void startFocusLostUnPopSequence() {
    // In a seperate event check to see if source doesn't have focus, then unPop.
    // Doing this inline would always unPop before the focus is transfered to the list so that user can click on it with a mouse to select an entry
    new javax.swing.Timer(200, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        synchronized (TypeAheadPopupList.this) {
          ((javax.swing.Timer) e.getSource()).stop();
          ((javax.swing.Timer) e.getSource()).removeActionListener(this);
          if (!owner.hasFocus())
            unPop(Boolean.TRUE);
        }
      }
    }).start();
  }

  private synchronized void go(final JTextComponent source, final Boolean dueToFocus) {
    searchStringChanged = true;
    if (searchStringTimer == null) {
      searchStringActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
          synchronized (TypeAheadPopupList.this) {
            goAction(source, dueToFocus);
          }
        }
      };
      searchStringTimer = new javax.swing.Timer(300, searchStringActionListener);
    }
    if (!searchStringChangeDispatching) {
      searchStringChangeDispatching = true;
      searchStringTimer.restart();
    }
  }

  private synchronized void goAction(JTextComponent source, Boolean dueToFocus) {
    searchStringChanged = false;
    searchStringChangeDispatching = false;
    searchStringTimer.stop();
    String searchText = source.getText().trim();
    String search = ListUtils.getLastElement(searchText);
    boolean pop = false;
    if (search.length() > 0) {
      initializeList(source);
      if (forceNextBroadSearchWithOriginalOrdering) {
        // clear all data to start over from originally sorted list disregarding current order
        list.setModel(new DefaultListModel());
      }
      boolean found = setSourceSearchString(search, searchText, search.length() > 0);
      if (found) {
        adjustListSize();
        pop = true;
        // look for pop exception -- if single perfect match then do not show the popup list
        if (list.getModel().getSize() == 1) {
          Object value = list.getSelectedValue();
          if (value != null) {
            String strValue = convertListObjValueToStrValue(value);
            pop = !search.equals(strValue);
          }
        }
        if (pop)
          pop(dueToFocus);
      }
    }
    if (!pop) unPop(dueToFocus);
    if (searchStringChanged) {
      searchStringChangeDispatching = true;
      searchStringTimer.restart();
    }
  }

  private boolean setSourceSearchString(String search, String fullSearchText, boolean withSelect) {
    boolean found = false;
    if (forceNextBroadSearchWithOriginalOrdering || (search != null && !search.equals(searchString))) {
      boolean tighter = searchString == null || (search != null && search.startsWith(searchString));
      if (forceNextBroadSearchWithOriginalOrdering) {
        tighter = false;
        forceNextBroadSearchWithOriginalOrdering = false;
      }
      searchString = search;
      // exclude all elements which are currently in the text field
      {
        String last = ListUtils.getLastElement(fullSearchText);
        Vector exceptionsV = new Vector();
        StringTokenizer st = new StringTokenizer(fullSearchText, ",");
        while (st.hasMoreTokens()) {
          String token = st.nextToken().trim();
          if (token.length() > 0 && !token.equals(last)) {
            exceptionsV.addElement(token);
          }
        }
        String[] exceptions = null;
        if (exceptionsV.size() > 0) {
          exceptions = new String[exceptionsV.size()];
          exceptionsV.toArray(exceptions);
        }
        // remember current exclusions
        this.searchExcludedStrs = exceptions;
      }
      ListUtils.filterAndHighlight(list, this, tighter, fullListModel);
      moveProvidersToBottom((DefaultListModel) list.getModel());
      list.repaint();
    }
    if (withSelect) {
      found = ListUtils.highlightItemByName(list, this, false, false, true);
    }
    return found;
  }

  private void adjustListLocation() {
    Point point = owner.getLocation();
    Dimension dimension = owner.getSize();
    Container container = owner.getParent();
    Point point1 = SwingUtilities.convertPoint(container, point.x, point.y + dimension.height, layeredPane);
    listPane.setLocation(point1);
  }

  private void pop(Boolean dueToFocus) {
    if (dueToFocus != null && dueToFocus.booleanValue() && lastNonFocusPopText != null && owner != null && lastNonFocusPopText.equals(owner.getText())) {
      // no-op
    } else if (isSingleItemList && owner != null && owner.getText().indexOf(',') >= 0) {
      // no-op
    } else {
      if (layeredPane == null)
        layeredPane = getParentLayeredPane();
      if (!listPane.isVisible()) {
        adjustListLocation();
        listPane.setVisible(true);
        layeredPane.add(listPane, JLayeredPane.POPUP_LAYER);
        layeredPane.moveToFront(listPane);
        // disable TAB key when popup list is shown, we want TAB/ENTER to be used as SELECT keys
        if (owner != null)
          owner.setFocusTraversalKeysEnabled(false);
      }
    }
  }

  private void unPop(Boolean dueToFocus) {
    if (dueToFocus != null && !dueToFocus.booleanValue() && owner != null)
      lastNonFocusPopText = owner.getText();
    forceNextBroadSearchWithOriginalOrdering = true;
    if (listPane != null) {
      if (layeredPane != null && listPane.isVisible()) {
        listPane.setVisible(false);
        layeredPane.remove(listPane);
        layeredPane.repaint();
        // re-enable TAB key when popup list is hidden
        if (owner != null)
          owner.setFocusTraversalKeysEnabled(true);
      }
    }
  }

  private JLayeredPane getParentLayeredPane() {
    Container container = owner.getParent();
    JLayeredPane jlayeredpane = null;
    for (Container container1 = container; container1 != null && jlayeredpane == null; container1 = container1.getParent())
      if (container1 instanceof RootPaneContainer)
        jlayeredpane = ((RootPaneContainer) container1).getLayeredPane();
      else
        if (container1 instanceof JLayeredPane)
          jlayeredpane = (JLayeredPane)container1;
    return jlayeredpane;
  }

  private void selectFromList(int index, boolean appendComma) {
    Object value = list.getSelectedValue();
    // unpop so we don't see list changing when removing item
    unPop(Boolean.FALSE);
    if (value instanceof ObjectsProviderUpdaterI) {
      ((ObjectsProviderUpdaterI) value).provide(ListUtils.getLastElement(owner.getText()));
    } else {
      selectedFromList(value, appendComma);
    }
    // unpop again to mark last selected text
    unPop(Boolean.FALSE);
  }

  private void selectedFromList(Object value, boolean appendComma) {
    if (value != null) {
      String strValue = convertListObjValueToStrValue(value);
      String currentValue = ListUtils.stripLastElement(owner.getText());
      owner.setText(currentValue + (currentValue.length() > 0 ? ", " : "") + strValue + (appendComma ? "," : "") + (appendComma ? " " : ""));
      // remove the selected from current list, but not the full list
      DefaultListModel listModel = (DefaultListModel) list.getModel();
      listModel.removeElement(value);
    }
  }

  private String convertListObjValueToStrValue(Object objValue) {
    ListRenderer renderer = (ListRenderer) list.getCellRenderer();
    renderer = (ListRenderer) renderer.clone();
    renderer.setStringHighlighter(null);
    String strValue = renderer.getRenderedTextApplySettings(objValue);
    return strValue;
  }

  private static void moveProvidersToBottom(DefaultListModel listModel) {
    int hitCount = 0;
    for (int i=0; i<listModel.getSize()-hitCount; i++) {
      Object obj = null;
      if ((obj = listModel.elementAt(i)) instanceof ObjectsProviderUpdaterI) {
        hitCount ++;
        listModel.removeElementAt(i);
        listModel.addElement(obj);
      }
    }
  }

  /**********************************************
   * K e y L i s t e n e r    interface methods *
   *********************************************/
  public synchronized void keyPressed(KeyEvent keyEvent) {
    if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER || 
        keyEvent.getKeyCode() == KeyEvent.VK_TAB || 
        (keyEvent.getKeyChar() == ',' && !isSingleItemList)) {
      Object source = keyEvent.getSource();
      if (source instanceof JTextComponent) {
        JTextComponent textComp = (JTextComponent) source;
        if (list != null && listPane.isVisible()) {
          // only skip selection if TAB is pressed on the Secondary Provider
          boolean focusEscapeWithTab = false;
          if (keyEvent.getKeyCode() == KeyEvent.VK_TAB && secondaryProvider != null && list.getSelectedValue() == secondaryProvider) {
            focusEscapeWithTab = true;
          } else {
            boolean appendComma = !isSingleItemList && keyEvent.getKeyChar() != ',';
            int index = list.getSelectedIndex();
            if (secondaryProvider != null && list.getSelectedValue() == secondaryProvider) {
              // If hit an "Action" item then it must be through ENTER to activate it
              if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                selectFromList(index, appendComma);
              }
            } else {
              selectFromList(index, appendComma);
            }
          }
          keyEvent.consume();
          if (focusEscapeWithTab || isSingleItemList) {
            if (keyEvent.getModifiers() == KeyEvent.VK_SHIFT)
              textComp.transferFocusBackward();
            else
              textComp.transferFocus();
          }
        }
      }
    } else if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
      unPop(Boolean.FALSE);
      keyEvent.consume();
    }
  }

  public synchronized void keyReleased(KeyEvent keyEvent) {
    if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
      unPop(Boolean.FALSE);
      keyEvent.consume();
    } else {
      Object source = keyEvent.getSource();
      if (source instanceof JTextComponent) {
        go((JTextComponent) source, Boolean.FALSE);
      }
    }
  }

  public void keyTyped(KeyEvent keyEvent) {
  }


  /***********************************************************
   * S t r i n g H i g h l i g h t e r I   interface methods *
   ***********************************************************/

  public synchronized Object[] getExcludedObjs() {
    return searchExcludedStrs;
  }

  public synchronized int getHighlightMatch() {
    return searchMatchBITS;
  }

  public synchronized String getHighlightStr() {
    return searchString;
  }

  public String[] getHighlightStrs() {
    return null;
  }

  public synchronized boolean hasHighlightingStr() {
    return searchString != null;
  }

  public boolean alwaysArmorInHTML() {
    return false;
  }

  public boolean includePreTags() {
    return false;
  }


  /***************************************************
   * L i s t U p d a t a b l e I   interface methods *
   ***************************************************/

  public void update(final Object[] objects) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        synchronized (TypeAheadPopupList.this) {
          DefaultListModel listModel = (DefaultListModel) list.getModel();
          boolean anyAdded = false;
          for (int i=0; i<objects.length; i++) {
            Object obj = objects[i];
            ListRenderer renderer = (ListRenderer) list.getCellRenderer();
            renderer = (ListRenderer) renderer.clone();
            renderer.setStringHighlighter(null);
            String strValue = renderer.getRenderedTextApplySettings(obj);
            if (StringHighlighter.matchStrings(strValue, TypeAheadPopupList.this, false, null) != StringHighlighter.MATCH_STRING__NO_MATCH) {
              if (!listModel.contains(obj)) {
                listModel.addElement(obj);
                anyAdded = true;
              }
            }
            if (!fullListModel.contains(obj))
              fullListModel.addElement(obj);
          }
          if (anyAdded) {
            moveProvidersToBottom(listModel);
            moveProvidersToBottom(fullListModel);
            adjustListSize();
          }
        }
      }
    });
  }


  /*****************************************************
  *** D i s p o s a b l e O b j    interface methods ***
  *****************************************************/
  public synchronized void disposeObj() {
    if (provider != null) {
      provider.disposeObj();
      provider = null;
    }
    if (secondaryProvider != null) {
      secondaryProvider.disposeObj();
      secondaryProvider = null;
    }
    if (noFocusUnPopTimer != null) {
      noFocusUnPopTimer.stop();
      noFocusUnPopTimer.removeActionListener(noFocusUnPopActionListener);
      noFocusUnPopTimer = null;
      noFocusUnPopActionListener = null;
    }
    if (searchStringTimer != null) {
      searchStringTimer.stop();
      searchStringTimer.removeActionListener(searchStringActionListener);
      searchStringTimer = null;
      searchStringActionListener = null;
    }
  }

}
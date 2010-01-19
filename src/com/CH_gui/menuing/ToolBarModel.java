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

package com.CH_gui.menuing;

import javax.swing.*;

import java.awt.Component;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.action.Actions;
import com.CH_gui.actionGui.*;
import com.CH_gui.table.*;

import com.CH_gui.list.List_Viewable;

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
 * <b>$Revision: 1.20 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class ToolBarModel extends Object {

  private static final String EMPTY_TOOL_SEQUENCE = "Exit|200|";

  private String toolBarPropertyName;
  /** GUI reflection of the model */
  private JToolBar jToolBar;

  /** Vector stores sequence of MenuActionItem objects **/
  private Vector toolBarModel;
  /** Hashtable stores a map of MenuActionItem objects for fast access by ID **/
  private Hashtable toolBarModelHT;

  /** Creates new ToolBarModel */
  public ToolBarModel(String name) {
    this("defaulToolBarModel", name);
  }
  /** Creates new ToolBarModel */
  public ToolBarModel(String toolBarPropertyName, String name) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ToolBarModel.class, "ToolBarModel(String toolBarPropertyName)");
    if (trace != null) trace.args(toolBarPropertyName);

    this.toolBarPropertyName = toolBarPropertyName;

    String toolSequence = GlobalProperties.getProperty("ToolBarModel."+toolBarPropertyName);
    if (toolSequence == null)
      toolSequence = EMPTY_TOOL_SEQUENCE;

    if (trace != null) trace.data(5, toolSequence);

    Object[] _toolBarModel = buildToolBarModel(new StringTokenizer(toolSequence, "|"));
    toolBarModel = (Vector) _toolBarModel[0];
    toolBarModelHT = (Hashtable) _toolBarModel[1];
    //printTools();

    // GUI placeholder for this model.
    String sOrientation = GlobalProperties.getProperty("ToolBarOrientation."+toolBarPropertyName, ""+SwingConstants.HORIZONTAL);
    int orientation = JToolBar.HORIZONTAL;
    try {
      orientation = Integer.parseInt(sOrientation);
      if (orientation != SwingConstants.HORIZONTAL && orientation != SwingConstants.VERTICAL)
        orientation = SwingConstants.HORIZONTAL;
    } catch (Throwable t) {
    }
    jToolBar = new JToolBar(name, orientation);
    jToolBar.setFloatable(false);

    if (trace != null) trace.exit(ToolBarModel.class);
  }



  /**
   * Wipes out entire model.
   */
  public synchronized void clear() {
    if (toolBarModel != null) {
      for (int i=0; i<toolBarModel.size(); i++) {
        MenuActionItem menuActionItem = (MenuActionItem) toolBarModel.elementAt(i);
        menuActionItem.setAction(null);
      }
      toolBarModel.removeAllElements();
      toolBarModel = null;
      toolBarModelHT.clear();
      toolBarModelHT = null;
      MiscGui.removeAllComponentsAndListeners(jToolBar);
      jToolBar = null;
    }
  }


  public synchronized JToolBar getToolBar() {
    return jToolBar;
  }

  public synchronized void addActions(Action[] actionArray) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ToolBarModel.class, "addActions(Action[] actionArray)");

    if (actionArray != null && actionArray.length > 0) {

      // Go through the array and store all unique groups in a hashtable
      // where a key is the old group and value is the new replacement group.
      Hashtable ht = null;

      for (int i=0; i<actionArray.length; i++) {
        Action action = actionArray[i];
        // see if the action is to be included in the toolbar
        Boolean include = (Boolean) action.getValue(Actions.IN_TOOLBAR);
        if (include == null || include.equals(Boolean.TRUE)) {
          Integer actionId = (Integer) action.getValue(Actions.ACTION_ID);
          MenuActionItem toolItem = findTool(actionId);
          if (toolItem != null) {
            // fill in the real action
            toolItem.setAction(action);
          } else {
            toolItem = new MenuActionItem(action);
            toolBarModel.addElement(toolItem);
            toolBarModelHT.put(actionId, toolItem);
          }

          ButtonGroup buttonGroup = (ButtonGroup) action.getValue(Actions.BUTTON_GROUP);
          if (buttonGroup != null) {
            // laizly create a hashtable
            if (ht == null)
              ht = new Hashtable();

            ButtonGroup newButtonGroup = (ButtonGroup) ht.get(buttonGroup);

            // if the first item with that group, create a replacement group
            if (newButtonGroup == null) {
              newButtonGroup = new ButtonGroup();
              ht.put(buttonGroup, newButtonGroup);
            }

            // replace the old group with a new one
            toolItem.setButtonGroup(newButtonGroup);
          }

          // if the tool is ment to be visible and it was made visible...
          if (toolItem.isDefaultProperty() && ensureToolIsVisible(toolItem)) {
            // see if we need to add any separators
            addSeparatorsToTools();
          }
        } // end include
      } // end for
      if (ht != null)
        ht.clear();

      jToolBar.revalidate();
      jToolBar.repaint();
    }

    if (trace != null) trace.exit(ToolBarModel.class);
  } // addActions()

  /**
   * @return tool from the tool model carrying specified actionId
   */
  private MenuActionItem findTool(Integer actionId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ToolBarModel.class, "findTool(Integer actionId)");
    if (trace != null) trace.args(actionId);
    MenuActionItem returnMenuItem = (MenuActionItem) toolBarModelHT.get(actionId);
    if (trace != null) trace.exit(ToolBarModel.class, returnMenuItem);
    return returnMenuItem;
  }

  /**
   * @return true if a tool was made visible
   */
  private boolean ensureToolIsVisible(MenuActionItem toolItem) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ToolBarModel.class, "ensureToolIsVisible(MenuActionItem toolItem)");
    if (trace != null) trace.args(toolItem);

    if (toolItem.isShowing()) {
      if (trace != null) trace.exit(ToolBarModel.class, false);
      return false;
    }

    int visibleBefore = countVisibleBeforeTool(toolItem);
    Component newComponent = null;
    if (toolItem.isActionItem()) {
      if (!toolItem.isGUIButtonSet()) {
        newComponent = convertActionToToolButton(toolItem);
      } else {
        newComponent = toolItem.getGUIButton();
      }
    } else {
      // Separator
      //JToolBar.Separator sep = new JToolBar.Separator(new java.awt.Dimension(1, 20));
      JPanel sep = new JPanel();
      sep.setLayout(null);
      int orientation = jToolBar.getOrientation() != SwingConstants.VERTICAL ? SwingConstants.VERTICAL : SwingConstants.HORIZONTAL;
      if (orientation == SwingConstants.VERTICAL) {
        sep.setSize(new java.awt.Dimension(1, 41));
        sep.setMaximumSize(new java.awt.Dimension(1, 41));
        sep.setPreferredSize(new java.awt.Dimension(1, 41));
      }
      else {
        sep.setSize(new java.awt.Dimension(41, 1));
        sep.setMaximumSize(new java.awt.Dimension(41, 1));
        sep.setPreferredSize(new java.awt.Dimension(41, 1));
      }
      sep.setBackground(sep.getBackground().darker());
      newComponent = sep;
    }

    jToolBar.add(newComponent, visibleBefore);
    toolItem.setShowing(true);

    if (trace != null) trace.exit(ToolBarModel.class, true);
    return true;
  }

  private int countVisibleBeforeTool(MenuActionItem menuTool) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ToolBarModel.class, "ensureToolIsVisible(MenuActionItem toolItem)");
    if (trace != null) trace.args(menuTool);

    int count = 0;
    for (int i=0; i<toolBarModel.size(); i++) {
      MenuActionItem menuItem = (MenuActionItem) toolBarModel.elementAt(i);
      if (menuItem == menuTool)
        break;
      else if (menuItem.isShowing())
        count ++;
    }

    if (trace != null) trace.exit(ToolBarModel.class, count);
    return count;
  }


  private void addSeparatorsToTools() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ToolBarModel.class, "addSeparatorsToTools()");

    MenuActionItem lastHidenSeparatorAfterShownItem = null;

    Enumeration tools = toolBarModel.elements();
    int state = 0; // 0 - looking for shown menu node, 1 - looking for hiden seperator, 2 - looking for another shown menu node
    while (tools.hasMoreElements()) {
      MenuActionItem item = (MenuActionItem) tools.nextElement();

      if (state == 0) {
        if (item.isShowing()) {
          if (item.isActionItem()) {
            state = 1;
          }
        }
      } else if (state == 1) {
        if (!item.isShowing()) {
          if (!item.isActionItem()) {
            lastHidenSeparatorAfterShownItem = item;
            state = 2;
          }
        } else if (!item.isActionItem()) {
          state = 0;
        }
      } else if (state == 2) {
        if (item.isShowing()) {
          if (item.isActionItem()) {
            ensureToolIsVisible(lastHidenSeparatorAfterShownItem);
            state = 1;
          } else {
            state = 0;
          }
        }
      }
    } // end while

    if (trace != null) trace.exit(ToolBarModel.class);
  } // end addSeparatorsToTools();


  /**
   * @return all currently Available tools with specified isShowing flag.
   */
  public synchronized List_Viewable[] getAvailableTools(boolean isShowing, boolean includeGenericTableActions) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ToolBarModel.class, "getAvailableTools(boolean isShowing, boolean includeGenericTableActions)");
    if (trace != null) trace.args(isShowing);

    Vector v = new Vector();
    for (int i=0; i<toolBarModel.size(); i++) {
      MenuActionItem toolItem = (MenuActionItem) toolBarModel.elementAt(i);
      // if isShowing has specified value
      if (toolItem.isShowing() == isShowing) {
        // if item is not showing and its the Separator or expected tool, don't return it.
        if (! (isShowing == false && toolItem.getAction() == null) ) {
          if (includeGenericTableActions) {
            v.addElement(toolItem);
          } else {
            Action action = toolItem.getAction();
            if (!(action instanceof RecordActionTable.SortByColumnAction || 
                  action instanceof RecordActionTable.SortAscDescAction || 
                  action instanceof RecordActionTable.CustomizeColumnsAction)) {
              v.addElement(toolItem);
            }
          }
        }
      }
    }
    List_Viewable[] list_viewable = new List_Viewable[v.size()];
    v.toArray(list_viewable);

    if (trace != null) trace.exit(ToolBarModel.class, list_viewable);
    return list_viewable;
  }

  /**
   * Sets new content of the tool bar and rebuilds its gui.
   */
  public synchronized void updateToolBar(List_Viewable[] chosenToolItems) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ToolBarModel.class, "updateToolBar(List_Viewable[] chosenToolItems)");
    if (trace != null) trace.args(chosenToolItems);

    // remove all Separators
    List_Viewable separator = makeSeparator();
    while (toolBarModel.remove(separator));

    // hide all current tools
    for (int i=0; i<toolBarModel.size(); i++) {
      MenuActionItem toolItem = (MenuActionItem) toolBarModel.elementAt(i);
      toolItem.setDefaultProperty(false);
    }

    // show the chosen tools
    for (int i=0; i<chosenToolItems.length; i++) {
      MenuActionItem toolItem = (MenuActionItem) chosenToolItems[i];
      toolItem.setDefaultProperty(true);
      if (!toolItem.isSeparator()) {
        toolBarModel.remove(toolItem);
        toolBarModel.insertElementAt(toolItem, i);
      } else {
        // need to create brand new separator for each spot so that they have different object references...
        // important when looking for object index or counting visible elements before this...
        toolBarModel.insertElementAt(makeSeparator(), i);
      }
    }
    rebuildToolBar();

    // soft save the properties to Global Properties
    putToolBarProperties();

    if (trace != null) trace.exit(ToolBarModel.class);
  }

  public synchronized void removeActions(Action[] actionArray) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ToolBarModel.class, "removeActions(Action[] actionArray)");
    if (trace != null) trace.args(actionArray);

    if (actionArray != null) {
      boolean anyRemoved = false;
      for (int i=0; i<actionArray.length; i++) {
        Action action = actionArray[i];
        MenuActionItem toolItem = findTool((Integer) action.getValue(Actions.ACTION_ID));
        if (toolItem != null) {
          Boolean removable = (Boolean) action.getValue(Actions.REMOVABLE_TOOLBAR);
          if (!toolItem.isActionItem() || (removable != null && removable.equals(Boolean.TRUE))) {
            anyRemoved = true;
            toolItem.setAction(null);
          }
        }
      }
      if (anyRemoved) {
        rebuildToolBar();
      }
    }
    if (trace != null) trace.exit(ToolBarModel.class);
  }

  public static List_Viewable makeSeparator() {
    return new MenuActionItem(MenuActionItem.STR_SEPARATOR, 0, -1, -1, -1);
  }


  private void rebuildToolBar() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ToolBarModel.class, "rebuildToolBar()");

    jToolBar.removeAll();
    for (int i=0; i<toolBarModel.size(); i++) {
      MenuActionItem toolItem = (MenuActionItem) toolBarModel.elementAt(i);
      // since we removed all items, we must reset isShowing
      toolItem.setShowing(false);
      if (toolItem.isDefaultProperty() && toolItem.getAction() != null)
        ensureToolIsVisible(toolItem);
    }
    addSeparatorsToTools();

    jToolBar.revalidate();
    jToolBar.repaint();

    if (trace != null) trace.exit(ToolBarModel.class);
  }


  /**
   * Converts an Action to a AbstractButton if possible.  Only JActionToggleButton,
   * and JActionButton are converted here.
   * return NULL if object could not be created, an object otherwise
   */
  private static AbstractButton convertActionToToolButton(MenuActionItem toolItem) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ToolBarModel.class, "convertActionToToolButton(MenuActionItem toolItem)");
    if (trace != null) trace.args(toolItem);

    AbstractButton toolButton = null;
    Action action = toolItem.getAction();

    toolButton = convertActionToToolButton(action, toolItem.getButtonGroup());
    toolItem.setGUIButton(toolButton);

    if (trace != null) trace.exit(ToolBarModel.class, toolButton);
    return toolButton;
  }
  public static AbstractButton convertActionToToolButton(Action action, ButtonGroup buttonGroup) {
    AbstractButton toolButton = null;
    Boolean state = (Boolean) action.getValue(Actions.STATE_CHECK);
    if (state != null) {
        toolButton = new JActionToggleButtonNoFocus(action);
    } else {
      Boolean selected = (Boolean) action.getValue(Actions.SELECTED_RADIO);
      if (selected != null) {
        toolButton = new JActionToggleButtonNoFocus(action, false, buttonGroup, true);
      } else {
        toolButton = new JActionButtonNoFocus(action);
      }
    }
    return toolButton;
  }


  /***************************************/
  /* Private helpers for externalization */
  /***************************************/

  private static Object[] buildToolBarModel(StringTokenizer st) {
    Vector newToolBarModel = new Vector();
    Hashtable newToolBarModelHT = new Hashtable();
    while (st.hasMoreTokens()) {
      String toolName = st.nextToken();
      int actionId = Integer.parseInt(st.nextToken());
      MenuActionItem action = new MenuActionItem(toolName, actionId, -1, -1, -1);
      newToolBarModel.addElement(action);
      newToolBarModelHT.put(new Integer(actionId), action);
    }
    return new Object[] { newToolBarModel, newToolBarModelHT };
  }

  private static void dissasambleToolBarModel(Vector toolBarModel, StringBuffer sb) {
    for (int i=0; i<toolBarModel.size(); i++) {
      MenuActionItem toolItem = (MenuActionItem) toolBarModel.elementAt(i);
      if (toolItem.isDefaultProperty()) {
        sb.append(toolItem.getName().replace('|', '_'));
        sb.append("|");
        sb.append(toolItem.getActionId());
        sb.append("|");
      }
    }
  }

  public synchronized void putToolBarProperties() {
    StringBuffer sb = new StringBuffer();
    dissasambleToolBarModel(toolBarModel, sb);
    GlobalProperties.setProperty("ToolBarModel."+toolBarPropertyName, sb.toString());
    GlobalProperties.setProperty("ToolBarOrientation."+toolBarPropertyName, ""+getToolBar().getOrientation());
  }

  public synchronized void printTools() {
    StringBuffer strBuf = new StringBuffer();
    dissasambleToolBarModel(toolBarModel, strBuf);
    System.out.println(strBuf.toString());
  }

}
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

import com.CH_gui.util.MiscGui;
import java.awt.Component;
import java.util.*;

import javax.swing.*;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.action.Actions;
import com.CH_gui.actionGui.*;
import com.CH_gui.table.*;

import com.CH_gui.list.List_Viewable;
import com.CH_gui.util.ActionUtils;

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

  /** Force addition of all tools even if they were ommited in the properties. */
  private boolean forceAddAllTools;

  /** Stores sequence of MenuActionItem objects **/
  private ArrayList toolBarModel;
  /** lookup stores a map of MenuActionItem objects for fast access by ID **/
  private HashMap toolBarModelHM;

  /** Creates new ToolBarModel */
  public ToolBarModel(String name) {
    this("defaulToolBarModel", name);
  }
  /** Creates new ToolBarModel */
  public ToolBarModel(String toolBarPropertyName, String name) {
    this(toolBarPropertyName, name, false);
  }
  public ToolBarModel(String toolBarPropertyName, String name, boolean forceAddAllTools) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ToolBarModel.class, "ToolBarModel(String toolBarPropertyName, String name, boolean forceAddAllTools)");
    if (trace != null) trace.args(toolBarPropertyName, name);
    if (trace != null) trace.args(forceAddAllTools);

    this.toolBarPropertyName = toolBarPropertyName;
    this.forceAddAllTools = forceAddAllTools;

    Object[] _toolBarModel = buildModel("ToolBarModel."+toolBarPropertyName, EMPTY_TOOL_SEQUENCE);
    toolBarModel = (ArrayList) _toolBarModel[0];
    toolBarModelHM = (HashMap) _toolBarModel[1];
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
    jToolBar.setVisible(false);

    if (trace != null) trace.exit(ToolBarModel.class);
  }

  private static Object[] buildModel(String propertyName, String emptySequence) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ToolBarModel.class, "buildModel(String propertyName, String emptySequence)");
    if (trace != null) trace.args(propertyName, emptySequence);

    String toolSequence = GlobalProperties.getProperty(propertyName);
    if (toolSequence == null)
      toolSequence = emptySequence;
    if (trace != null) trace.data(10, toolSequence);
    Object[] modelSet = null;
    try {
      modelSet = buildToolBarModel(new StringTokenizer(toolSequence, "|"));
    } catch (Exception e1) {
      try {
        // we have corrupted properties -- reset them to defaults so that user doesn't have to do it manually
        GlobalProperties.resetMyAndGlobalProperties();
        toolSequence = GlobalProperties.getProperty(propertyName);
        if (toolSequence == null)
          toolSequence = emptySequence;
        if (trace != null) trace.data(20, toolSequence);
        modelSet = buildToolBarModel(new StringTokenizer(toolSequence, "|"));
      } catch (Exception e2) {
        // failed again with reset properties - this is probably programming bug
        e2.printStackTrace();
        // last resort is the empty sequence
        modelSet = buildToolBarModel(new StringTokenizer(emptySequence, "|"));
      }
    }

    if (trace != null) trace.exit(ToolBarModel.class, modelSet);
    return modelSet;
  }

  /**
   * Wipes out entire model.
   */
  public synchronized void clear() {
    if (toolBarModel != null) {
      for (int i=0; i<toolBarModel.size(); i++) {
        MenuActionItem menuActionItem = (MenuActionItem) toolBarModel.get(i);
        menuActionItem.setAction(null);
      }
      toolBarModel.clear();
      toolBarModel = null;
      toolBarModelHM.clear();
      toolBarModelHM = null;
      MiscGui.removeAllComponentsAndListeners(jToolBar);
      jToolBar = null;
    }
  }


  public synchronized JToolBar getToolBar() {
    return jToolBar;
  }

  /**
   * Add menus and tools.
   * @return true if new component causes addition of menus or tools
   */
  public synchronized ToolBarModel addComponentActions(Component source) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ToolBarModel.class, "addComponentActions(Component)");
    if (trace != null) trace.args(source);

    if (source != null) {
      if (trace != null) trace.args(Misc.getClassNameWithoutPackage(source.getClass()));
      Action[] actionArray = ActionUtils.getActionsRecursively(source);
      addActions(actionArray);
      // When component is added, make sure the state of actions is updated.
      ActionUtils.setEnabledActionsRecur(source);
    }

    if (trace != null) trace.exit(ToolBarModel.class, this);
    return this;
  }

  public synchronized void addActions(Action[] actionArray) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ToolBarModel.class, "addActions(Action[] actionArray)");

    if (actionArray != null && actionArray.length > 0) {

      // Go through the array and store all unique groups in a lookup table
      // where a key is the old group and value is the new replacement group.
      HashMap groupsHM = null;
      boolean isAnyMadeVisible = false;

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
            toolBarModel.add(toolItem);
            toolBarModelHM.put(actionId, toolItem);
          }

          ButtonGroup buttonGroup = (ButtonGroup) action.getValue(Actions.BUTTON_GROUP);
          if (buttonGroup != null) {
            // laizly create lookup
            if (groupsHM == null) groupsHM = new HashMap();
            ButtonGroup newButtonGroup = (ButtonGroup) groupsHM.get(buttonGroup);

            // if the first item with that group, create a replacement group
            if (newButtonGroup == null) {
              newButtonGroup = new ButtonGroup();
              groupsHM.put(buttonGroup, newButtonGroup);
            }

            // replace the old group with a new one
            toolItem.setButtonGroup(newButtonGroup);
          }

          // if the tool is ment to be visible and it was made visible...
          if (forceAddAllTools || toolItem.isDefaultProperty()) {
            if (ensureToolIsVisible(toolItem)) {
              isAnyMadeVisible = true;
              // see if we need to add any separators
              addSeparatorsToTools();
            }
          }
        } // end include
      } // end for
      if (groupsHM != null)
        groupsHM.clear();

      if (isAnyMadeVisible && !jToolBar.isVisible())
        jToolBar.setVisible(true);
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
    MenuActionItem returnMenuItem = (MenuActionItem) toolBarModelHM.get(actionId);
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
      MenuActionItem menuItem = (MenuActionItem) toolBarModel.get(i);
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

    Iterator tools = toolBarModel.iterator();
    int state = 0; // 0 - looking for shown menu node, 1 - looking for hiden seperator, 2 - looking for another shown menu node
    while (tools.hasNext()) {
      MenuActionItem item = (MenuActionItem) tools.next();

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

    ArrayList tools = new ArrayList();
    for (int i=0; i<toolBarModel.size(); i++) {
      MenuActionItem toolItem = (MenuActionItem) toolBarModel.get(i);
      // if isShowing has specified value
      if (toolItem.isShowing() == isShowing) {
        // if item is not showing and its the Separator or expected tool, don't return it.
        if (! (isShowing == false && toolItem.getAction() == null) ) {
          if (includeGenericTableActions) {
            tools.add(toolItem);
          } else {
            Action action = toolItem.getAction();
            if (!(action instanceof RecordActionTable.SortByColumnAction ||
                  action instanceof RecordActionTable.SortAscDescAction ||
                  action instanceof RecordActionTable.CustomizeColumnsAction)) {
              tools.add(toolItem);
            }
          }
        }
      }
    }
    List_Viewable[] list_viewable = new List_Viewable[tools.size()];
    tools.toArray(list_viewable);

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
      MenuActionItem toolItem = (MenuActionItem) toolBarModel.get(i);
      toolItem.setDefaultProperty(false);
    }

    // show the chosen tools
    for (int i=0; i<chosenToolItems.length; i++) {
      MenuActionItem toolItem = (MenuActionItem) chosenToolItems[i];
      toolItem.setDefaultProperty(true);
      if (!toolItem.isSeparator()) {
        toolBarModel.remove(toolItem);
        toolBarModel.add(i, toolItem);
      } else {
        // need to create brand new separator for each spot so that they have different object references...
        // important when looking for object index or counting visible elements before this...
        toolBarModel.add(i,makeSeparator());
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
    boolean isAnyMadeVisible = false;

    for (int i=0; i<toolBarModel.size(); i++) {
      MenuActionItem toolItem = (MenuActionItem) toolBarModel.get(i);
      // since we removed all items, we must reset isShowing
      toolItem.setShowing(false);
      if (toolItem.isDefaultProperty() && toolItem.getAction() != null) {
        if (ensureToolIsVisible(toolItem)) {
          isAnyMadeVisible = true;
        }
      }
    }
    addSeparatorsToTools();

    if (isAnyMadeVisible && !jToolBar.isVisible())
      jToolBar.setVisible(true);
    if (!isAnyMadeVisible && jToolBar.isVisible())
      jToolBar.setVisible(false);

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
    ArrayList newToolBarModel = new ArrayList();
    HashMap newToolBarModelHM = new HashMap();
    while (st.hasMoreTokens()) {
      String toolName = st.nextToken();
      int actionId = Integer.parseInt(st.nextToken());
      MenuActionItem action = new MenuActionItem(toolName, actionId, -1, -1, -1);
      newToolBarModel.add(action);
      newToolBarModelHM.put(new Integer(actionId), action);
    }
    return new Object[] { newToolBarModel, newToolBarModelHM };
  }

  private static void dissasambleToolBarModel(List toolBarModel, StringBuffer sb) {
    for (int i=0; i<toolBarModel.size(); i++) {
      MenuActionItem toolItem = (MenuActionItem) toolBarModel.get(i);
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
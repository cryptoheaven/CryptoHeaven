/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_gui.list;

import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import com.CH_gui.gui.*;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *  This is a general panel where there are two lists.
 *  The functionalities include: moving elements back and forth between the lists
 *  moving elements up and down in the destination list, and reseting lists
 *  to the default setting.
 *
* Class Details:
 *  NOTE: The class requires to add default source elements first and to
 *  add default destination elements second since only addition of the second
 *  updates the preferred size of both lists.
 *
 * <b>$Revision: 1.27 $</b>
 * @author  Marcin Kurzawa
 * @version
 */

public class DualListBox extends JPanel implements StringHighlighterI, ListUpdatableI {

  private static final DataFlavor OBJECT_ARRAY_FLAVOUR = new DataFlavor(ListTransferableData.class, "Object array");
  protected static final String ADD_BUTTON_LABEL = com.CH_cl.lang.Lang.rb.getString("button_Add") + " >>";
  protected static final String ADD_ALL_BUTTON_LABEL = com.CH_cl.lang.Lang.rb.getString("button_Add_All") + " >>";
  protected static final String REMOVE_BUTTON_LABEL = "<< " + com.CH_cl.lang.Lang.rb.getString("button_Remove");
  protected static final String REMOVE_ALL_BUTTON_LABEL = "<< " + com.CH_cl.lang.Lang.rb.getString("button_Remove_All");
  protected static final String RESET_BUTTON_LABEL = com.CH_cl.lang.Lang.rb.getString("button_Reset");
  protected static final String UP_BUTTON_LABEL = com.CH_cl.lang.Lang.rb.getString("button_Move_Up");
  protected static final String DOWN_BUTTON_LABEL = com.CH_cl.lang.Lang.rb.getString("button_Move_Down");
  protected static final String DEFAULT_SOURCE_CHOICE_LABEL = com.CH_cl.lang.Lang.rb.getString("label_Available_Choices");
  protected static final String DEFAULT_DEST_CHOICE_LABEL = com.CH_cl.lang.Lang.rb.getString("label_Your_Choices");
  protected static final int UP = -1;
  protected static final int DOWN = 1;

  protected JLabel sourceLabel;
  protected JList sourceList;

  protected DefaultListModel fullSourceListModel; // unfiltered copy for search string operations
  protected DefaultListModel sourceListModel;
  protected DefaultListModel defaultSourceLModel;/* copy for reset operation */

  protected JLabel destLabel;
  protected JList destList;
  protected DefaultListModel destListModel;
  protected DefaultListModel defaultDestLModel; /* copy for reset operation */

  protected JButton addButton;
  protected JButton removeButton;
  protected JButton addAllButton;
  protected JButton removeAllButton;
  protected JButton resetButton;
  protected JButton moveUpButton;
  protected JButton moveDownButton;

  protected EventListenerList myListenerList = new EventListenerList();
  private ListDataListener destListener;

  private String sourceSearchString;
  private static int sourceSearchMatchBITS =
      StringHighlighter.MATCH_STRING__EXACT |
      StringHighlighter.MATCH_STRING__TRIMMED |
      StringHighlighter.MATCH_STRING__NO_CASE |
      StringHighlighter.MATCH_STRING__LEADING_TOKENS |
      StringHighlighter.MATCH_STRING__SEQUENCED_TOKENS;

  /**
   * Enables left and right lists to be a Drop Targets.
   */
  DropTarget dropTargetLeft = null;
  DropTarget dropTargetRight = null;


  // Use global holder for dragged objects to avoid selialization of handled objects!!!
  // This is OK since objects by the lists can only be sourced from the same or neighbour list.
  Object[] dndDraggedObjects = null;
  DefaultListModel dndSourceModel = null;


  private boolean includeAllControls;
  private boolean includeOrderControls;
  private boolean includeResetControl;
  private boolean buttonLocationsOnSides;

  /** Creates new DualListBox */
  public DualListBox(boolean includeAllControls, boolean includeOrderControls, boolean includeResetControl, boolean buttonLocationsOnSides) {
    this.includeAllControls = includeAllControls;
    this.includeOrderControls = includeOrderControls;
    this.includeResetControl = includeResetControl;
    this.buttonLocationsOnSides = buttonLocationsOnSides;

    if (includeAllControls && !buttonLocationsOnSides)
      throw new IllegalArgumentException("Move All controls are supported with side button locations only!");
    if (includeOrderControls && !buttonLocationsOnSides)
      throw new IllegalArgumentException("Order controls are supported with side button locations only!");

    initScreen();

    dropTargetRight = new DropTarget(destList, new ListDropTargetListener(destList));
    dropTargetLeft = new DropTarget(sourceList, new ListDropTargetListener(sourceList));

    DragSource dragSourceLeft = DragSource.getDefaultDragSource();
    DragSource dragSourceRight = DragSource.getDefaultDragSource();
    dragSourceLeft.createDefaultDragGestureRecognizer(sourceList, DnDConstants.ACTION_MOVE, new ListDragGestureListener(sourceList));
    dragSourceRight.createDefaultDragGestureRecognizer(destList, DnDConstants.ACTION_MOVE, new ListDragGestureListener(destList));
  }

  /** Creates new DualListBox */
  public DualListBox() {
    this(false, true, true, true);
  }


  /****************************************
   ***   UserRecord Listener handling   ***
   ****************************************/

  public synchronized void addChangeListener(ChangeListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DualListBox.class, "addChangeListener(ChangeListener l)");
    if (trace != null) trace.args(l);

    myListenerList.add(ChangeListener.class, l);

    if (destListener == null) {
      destListener = new ListDataListener() {
        public void contentsChanged(ListDataEvent e)  { fireChangeEvent(); }
        public void intervalAdded(ListDataEvent e)    { fireChangeEvent(); }
        public void intervalRemoved(ListDataEvent e)  { fireChangeEvent(); }
      };
      destListModel.addListDataListener(destListener);
    }

    if (trace != null) trace.exit(DualListBox.class);
  }

  public synchronized void removeChangeListener(ChangeListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DualListBox.class, "removeChangeListener(ChangeListener l)");
    if (trace != null) trace.args(l);
    myListenerList.remove(ChangeListener.class, l);
    if (trace != null) trace.exit(DualListBox.class);
  }

  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   */
  protected synchronized void fireChangeEvent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DualListBox.class, "fireChangeEvent()");

    // Guaranteed to return a non-null array
    Object[] listeners = myListenerList.getListenerList();
    ChangeEvent e = null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length-2; i>=0; i-=2) {
      if (listeners[i] == ChangeListener.class) {
        // Lazily create the event:
        if (e == null)
          e = new ChangeEvent(this);
        ((ChangeListener)listeners[i+1]).stateChanged(e);
      }
    }
    if (trace != null) trace.exit(DualListBox.class);
  }


  /**
   * @return Add button (for setting the button as default, special case of RecipientsDialog)
   */
  public synchronized JButton getAddButton() {
    return addButton;
  }

  public synchronized void clearAllSourceListModels() {
    fullSourceListModel.clear();
    sourceListModel.clear();
    defaultSourceLModel.clear();
    updateButtons();
  }

  public synchronized void clearAllDestinationListModels() {
    destListModel.clear();
    defaultDestLModel.clear();
    updateButtons();
  }

  private synchronized void clearSourceListModel() {
    fullSourceListModel.clear();
    sourceListModel.clear();
    updateButtons();
  }

  private synchronized void clearDestinationListModel() {
    destListModel.clear();
    updateButtons();
  }

  public synchronized String getSourceChoicesTitle() {
    return sourceLabel.getText();
  }

  public synchronized JList getSourceList() {
    return sourceList;
  }

  public synchronized String getDestinationChoicesTitle() {
    return destLabel.getText();
  }

  public synchronized void setSourceChoicesTitle(String newValue) {
    sourceLabel.setText(newValue);
  }

  public synchronized void setDestinationChoicesTitle(String newValue) {
    destLabel.setText(newValue);
  }


  /************ list model operations using ListModel *************/

  public synchronized void addSourceElements(ListModel newValue) {
    fillListModels(fullSourceListModel, sourceListModel, newValue);
    updateButtons();
  }

  public synchronized void setSourceElements(ListModel newValue) {
    clearSourceListModel();
    addSourceElements(newValue);
  }

  public synchronized void addDestinationElements(ListModel newValue) {
    fillListModel(destListModel, newValue);
    updateButtons();
  }

  private synchronized void fillListModel(DefaultListModel model, ListModel newValues) {
    boolean toSource = model == sourceListModel || model == fullSourceListModel;
    for (int i = 0; i < newValues.getSize(); i++) {
      Object o = newValues.getElementAt(i);
      if (isElementAddable(o, toSource)) {
        model.addElement(o);
      }
    }
  }

  private static void fillListModels(DefaultListModel model1, DefaultListModel model2, ListModel newValues) {
    for (int i = 0; i < newValues.getSize(); i++) {
      model1.addElement(newValues.getElementAt(i));
      model2.addElement(newValues.getElementAt(i));
    }
  }

  /************* list model operations using array of objects ************/

  public synchronized void addDefaultSourceElements(Object[] newValues) {
    fillListModel(defaultSourceLModel, newValues);
    addSourceElements(newValues);
  }

  public synchronized void addDefaultSourceElementsIfNotInLists(Object[] newValues) {
    addDefaultSourceElementsIfNotInLists(newValues, false);
  }
  public synchronized void addDefaultSourceElementsIfNotInLists(Object[] newValues, boolean suppressAddableCheck) {
    for (int i=0; i<newValues.length; i++) {
      Object obj = newValues[i];
      ListRenderer renderer = (ListRenderer) sourceList.getCellRenderer();
      renderer = (ListRenderer) renderer.clone();
      renderer.setStringHighlighter(null);
      String strValue = renderer.getRenderedTextApplySettings(obj);
      boolean matchPassed = getHighlightStr() == null  || StringHighlighter.matchStrings(strValue, this, false, null) != StringHighlighter.MATCH_STRING__NO_MATCH;
      if (suppressAddableCheck || isElementAddable(obj, true)) {
        if (!defaultSourceLModel.contains(obj) &&
            !fullSourceListModel.contains(obj) &&
            !sourceListModel.contains(obj) &&
            !defaultDestLModel.contains(obj) &&
            !destListModel.contains(obj))
        {
          defaultSourceLModel.addElement(obj);
          fullSourceListModel.addElement(obj);
          if (matchPassed)
            sourceListModel.addElement(obj);
        }
      }
    }
    updateButtons();
  }

  public synchronized void addSourceElements(Object[] newValues) {
    fillListModels(fullSourceListModel, sourceListModel, newValues);
  }
  public synchronized void addSourceElements(Collection newValues) {
    fillListModels(fullSourceListModel, sourceListModel, newValues);
  }

  public synchronized void setSourceElements(Object[] newValues) {
    clearSourceListModel();
    addSourceElements(newValues);
  }
  public synchronized void setSourceElements(Collection newValues) {
    clearSourceListModel();
    addSourceElements(newValues);
  }

  public synchronized void addDefaultDestinationElements(Object[] newValue) {
    fillListModel(defaultDestLModel, newValue);
    addDestinationElements(newValue);
  }

  public synchronized void addDestinationElements(Object[] newValue) {
    fillListModel(destListModel, newValue);
  }
  public synchronized void fillListModel(DefaultListModel model, Object[] newValues) {
    boolean toSource = model == sourceListModel || model == fullSourceListModel;
    for (int i = 0; i < newValues.length; i++) {
      Object o = newValues[i];
      if (isElementAddable(o, toSource)) {
        model.addElement(o);
      }
    }
    // maybe some buttons need an update
    updateButtons();
  }
  public synchronized void fillListModels(DefaultListModel model1, DefaultListModel model2, Object[] newValues) {
    boolean toSource = model1 == sourceListModel || model2 == sourceListModel;
    for (int i = 0; i < newValues.length; i++) {
      Object o = newValues[i];
      if (isElementAddable(o, toSource)) {
        model1.addElement(o);
        model2.addElement(o);
      }
    }
    // maybe some buttons need an update
    updateButtons();
  }
  public synchronized void fillListModel(DefaultListModel model, Collection newValues) {
    boolean toSource = model == sourceListModel || model == fullSourceListModel;
    Iterator iter = newValues.iterator();
    while (iter.hasNext()) {
      Object o = iter.next();
      if (isElementAddable(o, toSource)) {
        model.addElement(o);
      }
    }
    // maybe some buttons need an update
    updateButtons();
  }
  public synchronized void fillListModels(DefaultListModel model1, DefaultListModel model2, Collection newValues) {
    boolean toSource = model1 == sourceListModel || model2 == sourceListModel;
    Iterator iter = newValues.iterator();
    while (iter.hasNext()) {
      Object o = iter.next();
      if (isElementAddable(o, toSource)) {
        model1.addElement(o);
        model2.addElement(o);
      }
    }
    // maybe some buttons need an update
    updateButtons();
  }

  /**
   * Sets specified elements as destination defaults, puts them in destination list
   * and removes them from source list, also removes them from source default list.
   * If the requested elements do not exist in the source list they are still
   * added as default destination elements.
   */
  public synchronized void moveToDefaultDestinationElements(Object[] newValue) {
    for (int i=0; i<newValue.length; i++) {
      Object o = newValue[i];
      fullSourceListModel.removeElement(o);
      sourceListModel.removeElement(o);
      defaultSourceLModel.removeElement(o);
      if (!destListModel.contains(o))
        destListModel.addElement(o);
      if (!defaultDestLModel.contains(o))
        defaultDestLModel.addElement(o);
    }
    updateButtons();
  }
  /**
   * Moves specified elements to destination list and removes them from
   * source list.  If the requested elements do not exist in the source list
   * they are still added to the destination list.
   */
  public synchronized void moveToDestinationElements(Object[] newValue) {
    for (int i=0; i<newValue.length; i++) {
      Object o = newValue[i];
      fullSourceListModel.removeElement(o);
      sourceListModel.removeElement(o);
      if (!destListModel.contains(o))
        destListModel.addElement(o);
    }
    updateButtons();
  }
  /**
   * Moves specified elements to source list and removes them from
   * destination list.  If the requested elements do not exist in the destination list
   * they are still added to the source list.
   */
  public synchronized void moveToSourceElements(Object[] newValue) {
    for (int i=0; i<newValue.length; i++) {
      Object o = newValue[i];
      destListModel.removeElement(o);
      if (!fullSourceListModel.contains(o))
        fullSourceListModel.addElement(o);
      if (!sourceListModel.contains(o))
        sourceListModel.addElement(o);
    }
    updateButtons();
  }
  /**
   * Moves specified elements to destination list and removes them from
   * source list.  If the requested elements do not exist in the source list
   * they are NOT added to the destination list.
   * String matching is performed.  When more than one element matches the
   * search, nothing is selected.
   * @return the failed matches
   */
  public synchronized String[] moveToDestinationElementsSearchByUniqueStringsOnly(String[] chosenStrs) {
    Vector matchObjsV = null;
    Vector noMatchStrsV = null;
    ListRenderer renderer = (ListRenderer) sourceList.getCellRenderer();
    renderer = (ListRenderer) renderer.clone();
    renderer.setStringHighlighter(null);
    for (int i=0; i<chosenStrs.length; i++) {
      String str = chosenStrs[i];
      int matchCount = 0;
      Object matchObj = null;
      int matchCountNoCase = 0;
      Object matchObjNoCase = null;
      for (int j=0; j<sourceListModel.size(); j++) {
        Object sourceObj = sourceListModel.elementAt(j);
        String rendStrFull = renderer.getRenderedTextApplySettings(sourceObj);
        if (rendStrFull.equals(str)) {
          matchCount ++;
          matchObj = sourceObj;
        } else if (rendStrFull.equalsIgnoreCase(str)) {
          matchCountNoCase ++;
          matchObjNoCase = sourceObj;
        }
      }
      if (matchCount == 1) {
        if (matchObjsV == null) matchObjsV = new Vector();
        matchObjsV.addElement(matchObj);
      } else if (matchCountNoCase == 1) {
        if (matchObjsV == null) matchObjsV = new Vector();
        matchObjsV.addElement(matchObjNoCase);
      } else {
        if (noMatchStrsV == null) noMatchStrsV = new Vector();
        noMatchStrsV.addElement(str);
      }
    }
    if (matchObjsV != null && matchObjsV.size() > 0) {
      Object[] matchObjs = new Object[matchObjsV.size()];
      matchObjsV.toArray(matchObjs);
      moveToDestinationElements(matchObjs);
    }
    String[] noMatchStrs = null;
    if (noMatchStrsV != null && noMatchStrsV.size() > 0) {
      noMatchStrs = new String[noMatchStrsV.size()];
      noMatchStrsV.toArray(noMatchStrs);
    }
    updateButtons();
    return noMatchStrs;
  }

  /**
   * Highlight an item from the source list that has rendered name or starts
   * with the specified name.
   * @return true if item was highlighted, false if no match was found.
   */
//  public synchronized boolean highlightItemByName(String name, boolean selectBestFit, boolean selectOnlyIfNoFitSelected, boolean deselectWhenNotFound) {
//    boolean rc = ListUtils.highlightItemByName(sourceList, name, getHighlightMatch(), selectBestFit, selectOnlyIfNoFitSelected, deselectWhenNotFound);
//    updateButtons();
//    return rc;
//  }

  public synchronized boolean setSourceSearchString(String search, boolean withSelect) {
    boolean found = false;
    if ((search == null && sourceSearchString == null) || (search != null && search.equals(sourceSearchString))) {
      // same
    } else {
      // different
      boolean tighter = sourceSearchString == null || (search != null && search.startsWith(sourceSearchString));
      sourceSearchString = search;
      ListUtils.filterAndHighlight(sourceList, this, tighter, fullSourceListModel);
      sourceListModel = (DefaultListModel) sourceList.getModel();
      sourceList.repaint();
    }
    if (withSelect) {
      found = ListUtils.highlightItemByName(sourceList, this, false, false, true);
    }
    return found;
  }

  public synchronized Enumeration getSourceElementsFull() {
    return fullSourceListModel.elements();
  }
  public synchronized Enumeration getSourceElementsFiltered() {
    return sourceListModel.elements();
  }
  public synchronized Enumeration getDestinationElements() {
    return destListModel.elements();
  }

  /** Remove all selected elements from specified list.
    * Remove elements from the end, so the indices in model
    * don't have to be updated
    */
  private synchronized void removeSelectedFromList(JList jList) {
    int selected[] = jList.getSelectedIndices();
    DefaultListModel listModel = (DefaultListModel) jList.getModel();
    boolean fromSource = jList == sourceList;
    for (int i=selected.length-1; i>=0; --i) {
      int index = selected[i];
      Object element = listModel.getElementAt(index);
      if (isElementRemovable(element, fromSource)) {
        Object replacement = getElementRemovalReplacement(element, fromSource);
        if (replacement != null) {
          // special treatment for fullSourceListModel
          if (fromSource) {
            int indexFull = fullSourceListModel.indexOf(element);
            fullSourceListModel.setElementAt(replacement, indexFull);
          }
          listModel.setElementAt(replacement, index);
        }
        else {
          // special treatment for fullSourceListModel
          if (fromSource) {
            int indexFull = fullSourceListModel.indexOf(element);
            fullSourceListModel.removeElementAt(indexFull);
          }
          listModel.removeElementAt(index);
        }
      }
    }
    jList.clearSelection();
  }

  // Doesn't properly remove from fullSourceListModel......
  // Removing objects doesn't preserve the residual selection...
//  private void removeElementsFromList(Object[] toRemove, JList jList) {
//    DefaultListModel listModel = (DefaultListModel) jList.getModel();
//    boolean fromSource = jList == sourceList;
//    for (int i=0; i<toRemove.length; i++) {
//      Object element = toRemove[i];
//      int index = listModel.indexOf(element);
//      if (index >= 0) {
//        if (isElementRemovable(element, fromSource)) {
//          Object replacement = getElementRemovalReplacement(element, fromSource);
//          if (replacement != null)
//            listModel.setElementAt(replacement, index);
//          else
//            listModel.removeElementAt(index);
//        }
//      }
//    }
//  }

  /** Sub-classes should overwrite to provide unremovable elements. */
  public synchronized boolean isElementRemovable(Object obj, boolean fromSource) {
    return true;
  }
  /** Sub-classes should overwrite to provide replacement elements for the ones removed. */
  public synchronized Object getElementRemovalReplacement(Object obj, boolean fromSource) {
    return null;
  }

  /** Sub-classes should overwrite to provide unaddable elements. */
  public synchronized boolean isElementAddable(Object obj, boolean toSource) {
    return true;
  }

  /**
   * Move all selected elements from destination to source list
   * @return true is anthing was highlighted (and moved)
   **/
  public synchronized boolean moveHighlightedToSource() {
    Object[] selected = destList.getSelectedValues();
    boolean rc = false;
    if (selected != null && selected.length > 0) {
      addSourceElements(selected);
      removeSelectedFromList(destList); // work with separators since they all are equal
      //removeElementsFromList(selected, destList);
      updateButtons();
      rc = true;
    }
    return rc;
  }

  /**
   * Move all selected elements from source to destination list
   * @return true is anthing was highlighted (and moved)
   **/
  public synchronized boolean moveHighlightedToDestination() {
    Object[] selected = sourceList.getSelectedValues();
    boolean rc = false;
    if (selected != null && selected.length > 0) {
      addDestinationElements(selected);
      removeSelectedFromList(sourceList);
      //removeElementsFromList(selected, sourceList);
      updateButtons();
      rc = true;
    }
    return rc;
  }

  /** This method works for destination list only,
    * it moves element one position down or up if possible
    * single selection required
    * @param dir is a direction: -1 means "up", 1 means "down"
    */
  private synchronized void moveInDirection(int dir) {
    int selected[] = destList.getSelectedIndices();
    if (selected == null || selected.length != 1) return;

    int index = selected[0];

    if ((dir == -1 && index == 0 ) || (dir == 1 && index == destListModel.size()-1))
      return;

    Object obj = destListModel.getElementAt(index);
    destListModel.removeElementAt(index);
    destListModel.insertElementAt(obj, index+dir);
    destList.setSelectedIndex(index+dir);
  }

  /*
  private void disableButtons() {
    addButton.setEnabled(false);
    removeButton.setEnabled(false);
    if (moveUpButton != null)
      moveUpButton.setEnabled(false);
    if (moveDownButton != null)
      moveDownButton.setEnabled(false);
    if (addAllButton != null)
      addAllButton.setEnabled(false);
    if (removeAllButton != null)
      removeAllButton.setEnabled(false);
  }
   */

  private synchronized void updateButtons() {

    int[] destSelected = destList.getSelectedIndices();
    int[] sourceSelected = sourceList.getSelectedIndices();

    addButton.setEnabled( (sourceSelected == null || sourceSelected.length == 0)  ? false : true );
    removeButton.setEnabled( (destSelected == null || destSelected.length == 0) ? false : true );

    if (addAllButton != null)
      addAllButton.setEnabled(sourceListModel.getSize() > 0);
    if (removeAllButton != null)
      removeAllButton.setEnabled(destListModel.getSize() > 0);

    /* if nothing selected or more than one selected or there is one element in the list */
    if (destSelected == null || destSelected.length != 1 || destListModel.size() == 1) {
      if (moveUpButton != null)
        moveUpButton.setEnabled(false);
      if (moveDownButton != null)
        moveDownButton.setEnabled(false);
    }
    /* can't move up if top elemenent selected */
    else if (destSelected[0] == 0) {
      if (moveUpButton != null)
        moveUpButton.setEnabled(false);
      if (moveDownButton != null)
        moveDownButton.setEnabled(true);
    }
    /* can't move down if bottom element selected */
    else if(destSelected[0] == destListModel.size()-1) {
      if (moveUpButton != null)
        moveUpButton.setEnabled(true);
      if (moveDownButton != null)
        moveDownButton.setEnabled(false);
    }
    /* selection is in the middle so enable both buttons */
    else {
      if (moveUpButton != null)
        moveUpButton.setEnabled(true);
      if (moveDownButton != null)
        moveDownButton.setEnabled(true);
    }
  }

  public synchronized Object[] getResult() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DualListBox.class, "getResult()");

    Object[] result = null;
    Enumeration results = getDestinationElements();
    if (results != null) {
      Vector v = new Vector();
      while (results.hasMoreElements()) {
        Object o = results.nextElement();
        v.add(o);
      }
      result = v.toArray();
    }

    if (trace != null) trace.exit(DualListBox.class, result);
    return result;
  }


  /** ************* INITIALIZATION METHODS ******************************************
    * Initialize all elements, i.e. labels, lists and button and add them to the pane
    * using GridBagLayout from left to right
    * ******************************************************************************/

  private synchronized void initSource (ListCellRenderer listCellRenderer) {
    defaultSourceLModel = new DefaultListModel();
    sourceLabel = new JMyLabel(DEFAULT_SOURCE_CHOICE_LABEL);
    fullSourceListModel = new DefaultListModel();
    sourceListModel = new DefaultListModel();
    sourceList = new JList(sourceListModel);
    sourceList.setAutoscrolls(true);
    sourceList.setVisibleRowCount(10);
    sourceList.addListSelectionListener(new SourceListListener());
    sourceList.addMouseListener(new SourceListMouseListener());
    sourceList.setCellRenderer(listCellRenderer);
  }

  private synchronized void initDest (ListCellRenderer listCellRenderer) {
    defaultDestLModel = new DefaultListModel();
    destLabel = new JMyLabel(DEFAULT_DEST_CHOICE_LABEL);
    destListModel = new DefaultListModel();
    destList = new JList(destListModel);
    destList.setAutoscrolls(true);
    destList.setVisibleRowCount(10);
    destList.addListSelectionListener(new DestListListener());
    destList.addMouseListener(new DestListMouseListener());
    destList.setCellRenderer(listCellRenderer);
  }

  private synchronized void initScreen() {
    setBorder(BorderFactory.createEtchedBorder());
    setLayout(new GridBagLayout());

    initSource(new ListRenderer(false, false, true, false, true, true, false, true, this));
    initDest(new ListRenderer(false, false, true, false, true, true, false, true, null));

    addButton = new JMyButton(ADD_BUTTON_LABEL);
    removeButton = new JMyButton(REMOVE_BUTTON_LABEL);
    if (includeResetControl) {
      resetButton = new JMyButton(RESET_BUTTON_LABEL);
      resetButton.addActionListener(new ResetListener());
    }
    if (includeOrderControls) {
      moveUpButton = new JMyButton(UP_BUTTON_LABEL);
      moveDownButton = new JMyButton(DOWN_BUTTON_LABEL);
      moveUpButton.addActionListener(new MoveUpListener());
      moveDownButton.addActionListener(new MoveDownListener());
    }
    if (includeAllControls) {
      addAllButton = new JMyButton(ADD_ALL_BUTTON_LABEL);
      removeAllButton = new JMyButton(REMOVE_ALL_BUTTON_LABEL);
      addAllButton.addActionListener(new AddAllListener());
      removeAllButton.addActionListener(new RemoveAllListener());
    }

    //disableButtons();
    updateButtons();

    addButton.addActionListener(new AddListener());
    removeButton.addActionListener(new RemoveListener());


    /* now, add all the components to the pane from left to right */

    add(sourceLabel, new GridBagConstraints(0, 0, 2, 1, 10, 0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    JScrollPane sPane = new JScrollPane(sourceList);
    sPane.setPreferredSize(new Dimension(150, 200));
    add(sPane, new GridBagConstraints(0, 1, 1, 10, 10, 10,
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));

    if (buttonLocationsOnSides) {
      add(addButton, new GridBagConstraints(1, 1, 1, 1, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

      add(removeButton, new GridBagConstraints(1, 2, 1, 1, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

      if (addAllButton != null) {
        add(addAllButton, new GridBagConstraints(1, 4, 1, 1, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      }

      if (removeAllButton != null) {
        add(removeAllButton, new GridBagConstraints(1, 5, 1, 1, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      }
    } else {
      add(addButton, new GridBagConstraints(0, 11, 1, 1, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));

      add(removeButton, new GridBagConstraints(3, 11, 1, 1, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    }

    add(destLabel, new GridBagConstraints(3, 0, 3, 1, 10, 0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    JScrollPane dPane = new JScrollPane(destList);
    dPane.setPreferredSize(new Dimension(150, 200));
    add(dPane, new GridBagConstraints(3, 1, 2, 10, 10, 10,
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));

    if (resetButton != null) {
      if (buttonLocationsOnSides)
        add(resetButton, new GridBagConstraints(5, 1, 1, 1, 0, 0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      else
        add(resetButton, new GridBagConstraints(4, 11, 1, 1, 0, 0,
            GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    }

    if (moveUpButton != null) {
      add(moveUpButton, new GridBagConstraints(5, 3, 1, 1, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    }

    if (moveDownButton != null) {
      add(moveDownButton, new GridBagConstraints(5, 4, 1, 1, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    }
  }

  // =====================================================================
  // LISTENERS FOR THE INNER COMPONENTS OF THIS PANEL
  // =====================================================================

  /** Adds element to the destination list,
    * i.e. moves it from source list to destination list
    */
  private class AddListener implements ActionListener {
    public void actionPerformed(ActionEvent actionEvent) {
      synchronized (DualListBox.this) {
        moveHighlightedToDestination();
      }
    }
  }

  /** Adds element to the destination list,
    * i.e. moves it from source list to destination list
    */
  private class AddAllListener implements ActionListener {
    public void actionPerformed(ActionEvent actionEvent) {
      synchronized (DualListBox.this) {
        Object[] objs = sourceListModel.toArray();
        if (objs != null && objs.length > 0)
          moveToDestinationElements(objs);
      }
    }
  }

  /** Removes element from the destination list
    * i.e. moves it from destination list to source list
    */
  private class RemoveListener implements ActionListener {
    public void actionPerformed(ActionEvent actionEvent) {
      synchronized (DualListBox.this) {
        moveHighlightedToSource();
      }
    }
  }

  /** Removes element from the destination list
    * i.e. moves it from destination list to source list
    */
  private class RemoveAllListener implements ActionListener {
    public void actionPerformed(ActionEvent actionEvent) {
      synchronized (DualListBox.this) {
        Object[] objs = destListModel.toArray();
        if (objs != null && objs.length > 0)
          moveToSourceElements(objs);
      }
    }
  }

  /** Resets both lists to their original elements **/
  private class ResetListener implements ActionListener {
    public void actionPerformed(ActionEvent actionEvent) {
      synchronized (DualListBox.this) {
        clearSourceListModel();
        clearDestinationListModel();
        addSourceElements(defaultSourceLModel);
        addDestinationElements(defaultDestLModel);
        //disableButtons();
        updateButtons();
      }
    }
  }

  /** Action for "Move up" button **/
  private class MoveUpListener implements ActionListener {
    public void actionPerformed(ActionEvent actionEvent) {
      synchronized (DualListBox.this) {
        moveInDirection(UP);
      }
    }
  }
  /** Action for "Move down" button */
  private class MoveDownListener implements ActionListener {
    public void actionPerformed(ActionEvent actionEvent) {
      synchronized (DualListBox.this) {
        moveInDirection(DOWN);
      }
    }
  }

  /** Makes sure that buttons "Move up" and "Move down" are enabled
    * and disabled when necessary
    */
  private class DestListListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent selectionEvent) {
      synchronized (DualListBox.this) {
        updateButtons();
      }
    }
  }

  /**
    * Listens on the selection of the source list
    * and updates the appropriate buttons
    */
  private class SourceListListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent selectionEvent) {
      synchronized (DualListBox.this) {
        updateButtons();
      }
    }
  }

  /**
    * Listens for double click on the source list,
    * adds the element to the destination list
    */
  private class SourceListMouseListener implements MouseListener {

    public void mouseClicked(MouseEvent mouseEvent) {
      synchronized (DualListBox.this) {
        if (mouseEvent.getClickCount() == 2)
          moveHighlightedToDestination();
      }
    }
    public void mouseEntered(MouseEvent p1) {}
    public void mousePressed(MouseEvent p1) {}
    public void mouseExited(MouseEvent p1) {}
    public void mouseReleased(MouseEvent p1) {}
  }

  /**
    * Listens for the double click on the destination list
    * removes the element from the destination list
    */
  private class DestListMouseListener implements MouseListener {

    public void mouseClicked(MouseEvent mouseEvent) {
      synchronized (DualListBox.this) {
        if (mouseEvent.getClickCount() == 2)
          moveHighlightedToSource();
      }
    }
    public void mouseEntered(MouseEvent p1) {}
    public void mousePressed(MouseEvent p1) {}
    public void mouseExited(MouseEvent p1) {}
    public void mouseReleased(MouseEvent p1) {}
  }


  private class ListDropTargetListener implements DropTargetListener {

    private JList dndDropList;
    private Point lastPt;

    private ListDropTargetListener(JList dropList) {
      dndDropList = dropList;
    }

    /**
     * is invoked when you are dragging over the DropSite
     */
    public void dragEnter (DropTargetDragEvent event) {
      updateCursor(event);
    }

    /**
     * is invoked when you are exit the DropSite without dropping
     */
    public void dragExit (DropTargetEvent event) {
    }

    /**
     * is invoked when a drag operation is going on
     */
    public void dragOver (DropTargetDragEvent event) {
      Point pt = event.getLocation();
      if (lastPt == null || lastPt.x != pt.x || lastPt.y != pt.y) {
        lastPt = pt;
        updateCursor(event);
      }
      /*
      if (!DragSource.isDragImageSupported()) {
        Point pt = event.getLocation();
        if (_pt == null || _pt.x != pt.x || _pt.y != pt.y) {
          _pt = pt;
          Point wpt = dndDropList.getLocation();
          System.out.println("pt="+pt);
          System.out.println("wpt="+wpt);
          // Erase the last ghost image and cue line
          if (_raGhost != null) {
            paintImmediately(_raGhost.getBounds());
          }
          // Remember where you are about to draw the new ghost image
          if (_raGhost == null) _raGhost = new Rectangle();
          _raGhost.setRect(wpt.x + pt.x - _ptOffset.x, wpt.y + pt.y - _ptOffset.y, _imgGhost.getWidth(), _imgGhost.getHeight());
          // Draw the ghost image
          System.out.println("drawing image at "+_raGhost);
          Graphics2D g2 = (Graphics2D) getGraphics();
          g2.drawImage(_imgGhost, AffineTransform.getTranslateInstance(_raGhost.getX(), _raGhost.getY()), null);
        }
      }
       */
    }
    private void updateCursor(DropTargetDragEvent event) {
      try {
        if (event.isDataFlavorSupported(OBJECT_ARRAY_FLAVOUR) && dndDraggedObjects != null)
          event.acceptDrag (DnDConstants.ACTION_MOVE);
        else
          event.rejectDrag();
      } catch (Throwable t) {
      }
    }

    /**
     * a drop has occurred
     */
    public void drop (DropTargetDropEvent event) {
      synchronized (DualListBox.this) {
        Transferable transferable = event.getTransferable();

        if (transferable.isDataFlavorSupported(OBJECT_ARRAY_FLAVOUR)) {
          event.acceptDrop(DnDConstants.ACTION_MOVE);
          Object[] objs = dndDraggedObjects;
          dndDraggedObjects = null;
          if (objs != null) {

            Point location = event.getLocation();
            int dropIndex = dndDropList.locationToIndex(location);
            Object dropAtObj = null;
            DefaultListModel dndDropModel = (DefaultListModel) dndDropList.getModel();

            if (dropIndex >= 0) {
              dropAtObj = dndDropModel.getElementAt(dropIndex);
              if (ArrayUtils.find(objs, dropAtObj) > -1)
                dropAtObj = null;
            }

            for (int i=0; i<objs.length; i++) {
              Object o = objs[i];
              DefaultListModel m = null;
              if (dndSourceModel.contains(o))
                m = dndSourceModel;
              else if (dndDropModel.contains(o))
                m = dndDropModel;

              if (m != null) {
                int index = m.indexOf(o);
                // if changing source list, change the unfiltered source list too....
                int fullIndex = m == sourceListModel ? fullSourceListModel.indexOf(o) : -1;
                if (isElementRemovable(o, m == sourceListModel)) {
                  Object replacement = getElementRemovalReplacement(o, m == sourceListModel);
                  if (replacement != null) {
                    m.setElementAt(replacement, index);
                    if (fullIndex >= 0) fullSourceListModel.setElementAt(replacement, fullIndex);
                  } else {
                    m.removeElementAt(index);
                    if (fullIndex >= 0) fullSourceListModel.removeElementAt(fullIndex);
                  }
                }
              }
            }

            // fix the target drop location
            if (dropAtObj != null)
              dropIndex = dndDropModel.indexOf(dropAtObj);

            // drop at the end if location unknown, or the current item on which user is dropping
            if (dropIndex == -1)
              dropIndex = dndDropModel.size();

            for (int i=0; i<objs.length; i++) {
              Object o = objs[i];
              // if changing the source list, change the unfiltered source list too...
              if (isElementAddable(o, dndDropModel == sourceListModel)) {
                dropIndex = Math.min(dropIndex, dndDropModel.size());
                dndDropModel.add(dropIndex, o);
                if (dndDropModel == sourceListModel) fullSourceListModel.add(dropIndex, o);
              }
            }

          } // end if != null
          event.getDropTargetContext().dropComplete(true);
        }
        else {
          event.rejectDrop();
        }
      }
    } // end drop()

    /**
     * is invoked if the use modifies the current drop gesture
     */
    public void dropActionChanged(DropTargetDragEvent event) {
    }
  } // end private class ListDropTargetListener

  Point _ptOffset;
  Point _pt;
  Rectangle _raGhost;
  BufferedImage _imgGhost;

  private class ListDragGestureListener implements DragGestureListener {

    private JList dndSourceList;

    private ListDragGestureListener(JList sourceList) {
      dndSourceList = sourceList;
    }

    /**
     * a drag gesture has been initiated
     */
    public void dragGestureRecognized(DragGestureEvent event) {
      Object[] selected = dndSourceList.getSelectedValues();
      dndDraggedObjects = selected; // use global holder for objects to avoid DND serialization
      dndSourceModel = (DefaultListModel) dndSourceList.getModel();
      if (selected != null && selected.length > 0){
        ListTransferable objs = new ListTransferable(selected);
        // as the name suggests, starts the dragging
        if (true || !DragSource.isDragImageSupported()) {
          event.startDrag(null, objs, new ListDragSourceListener());
        } else {
          // Point inside the component that recognizes the gesture starting from its origin
          Point ptDragOrigin = event.getDragOrigin();
          Component component = event.getComponent();

          JList jList = (JList) component;
          int index = jList.getSelectedIndex();
          Rectangle raPath = jList.getCellBounds(index, index);
          if (_ptOffset == null) _ptOffset = new Point();
          _ptOffset.setLocation(ptDragOrigin.x-raPath.x, ptDragOrigin.y-raPath.y);

          ListCellRenderer renderer = jList.getCellRenderer();
          JLabel lbl = (JLabel) renderer.getListCellRendererComponent(jList, selected[0], index, true, false);

          // The layout manager normally does this...
          lbl.setSize(raPath.width, raPath.height);
          // Get a buffered image of the selection for dragging a ghost image
          _imgGhost = new BufferedImage(raPath.width, raPath.height, BufferedImage.TYPE_INT_ARGB_PRE);
          // Get a graphics context for this image
          Graphics2D g2 = _imgGhost.createGraphics();
          // Make the image ghostlike
          g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f));
          // Ask the cell renderer to paint itself into the BufferedImage
          lbl.paint(g2);
          // Locate the JLabel's icon so you don't paint under it
          Icon icon = lbl.getIcon();
          int nStartOfText = (icon == null) ? 0 : icon.getIconWidth()+lbl.getIconTextGap();
          // Use DST_OVER to cause under-painting to occur
          g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, 0.5f));
          // Use system colors to match the existing decor
          g2.setPaint(new GradientPaint(nStartOfText, 0, SystemColor.controlShadow, getWidth(), 0, new Color(255,255,255,0)));
          // Paint under the JLabel's text
          g2.fillRect(nStartOfText, 0, getWidth(), _imgGhost.getHeight());
          // Finished with the graphics context now
          g2.dispose();
          // Pass the drag image just in case the platform IS supporting it
          System.out.println("Start Dragging with image");
          event.startDrag(null, _imgGhost, ptDragOrigin, objs, new ListDragSourceListener());
        }
      } else {
        //System.out.println( "nothing was selected");
      }
    }


    /**
     * private inner nested class.
     */
    private class ListDragSourceListener implements DragSourceListener {
      /**
       * this message goes to DragSourceListener, informing it that the dragging
       * has ended
       *
       */
      public void dragDropEnd (DragSourceDropEvent event) {
        dndDraggedObjects = null;
        dndSourceModel = null;
      }

      /**
       * this message goes to DragSourceListener, informing it that the dragging
       * has entered the DropSite
       *
       */
      public void dragEnter (DragSourceDragEvent event) {
      }

      /**
       * this message goes to DragSourceListener, informing it that the dragging
       * has exited the DropSite
       *
       */
      public void dragExit (DragSourceEvent event) {
      }

      /**
       * this message goes to DragSourceListener, informing it that the dragging is currently
       * ocurring over the DropSite
       *
       */
      public void dragOver (DragSourceDragEvent event) {
      }

      /**
       * is invoked when the user changes the dropAction
       *
       */
      public void dropActionChanged ( DragSourceDragEvent event) {
      }
    } // end private class ListDragSourceListener


  } // end private class ListDragGestureListener


  private static class ListTransferable implements Transferable {

    private DataFlavor flavors[] = { OBJECT_ARRAY_FLAVOUR };
    private ListTransferableData data;

    public ListTransferable(Object[] data) {
      this.data = new ListTransferableData(data);
    }

    public DataFlavor[] getTransferDataFlavors() {
      return flavors;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
      Object returnObject;
      if (flavor.equals(OBJECT_ARRAY_FLAVOUR)) {
        returnObject = data;
      } else {
        throw new UnsupportedFlavorException(flavor);
      }
      return returnObject;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return (flavor.equals(OBJECT_ARRAY_FLAVOUR));
    }
  } // end private class ListTransferable


//  public static void main(String[] args) {
//    //DualListBox box = new DualListBox();
//    DualListBox box = new DualListBox(false, false, true, false);
//    String[] sourceItems = new String[] { "word", "goat", "code" };
//    String[] destItems = new String[] { "Freddy", "Greg", "Mary" };
//    box.addDefaultSourceElements(sourceItems);
//    box.addDefaultDestinationElements(destItems);
//    JFrame frame = new JFrame("Dual List Box");
//    frame.getContentPane().add(box, "Center");
//    frame.addWindowListener(new WindowAdapter() {
//      public void windowClosing(WindowEvent e) {
//        System.exit(0);
//      }
//    });
//    frame.pack();
//    frame.show();
//  }


  /******************************
   * StringHighlighterI methods *
   ******************************/

  public Object[] getExcludedObjs() {
    return null;
  }

  public synchronized int getHighlightMatch() {
    return sourceSearchMatchBITS;
  }

  public synchronized String getHighlightStr() {
    return sourceSearchString;
  }

  public synchronized String[] getHighlightStrs() {
    return sourceSearchString != null ? new String[] { sourceSearchString } : null;
  }

  public synchronized boolean hasHighlightingStr() {
    return sourceSearchString != null && sourceSearchString.length() > 0;
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
        synchronized (DualListBox.this) {
          addDefaultSourceElementsIfNotInLists(objects);
        }
      }
    });
  }

}
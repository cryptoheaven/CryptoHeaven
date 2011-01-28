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

package com.CH_gui.list;

import java.util.*;
import javax.swing.*;

import com.CH_co.util.*;

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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class ListUtils extends Object {

  public static void filterAndHighlight(JList sourceList, StringHighlighterI highlighter, boolean tighter, ListModel fullList) {
    if (!highlighter.hasHighlightingStr()) {
      DefaultListModel newSourceListModel = new DefaultListModel();
      int size = fullList.getSize();
      for (int i=0; i<size; i++) {
        Object element = fullList.getElementAt(i);
        newSourceListModel.addElement(element);
      }
      sourceList.setModel(newSourceListModel);
    } else {
      DefaultListModel sourceListModel = (DefaultListModel) sourceList.getModel();
      ListModel sourceModel = tighter ? sourceListModel : fullList;
      Object[] exceptions = highlighter.getExcludedObjs();

      Vector objsToKeepV = new Vector();
      Vector objsToRemoveV = new Vector();

      ListRenderer renderer = (ListRenderer) sourceList.getCellRenderer();
      renderer = (ListRenderer) renderer.clone();
      renderer.setStringHighlighter(null);

      boolean anyExceptions = exceptions != null && exceptions.length > 0;
      int sourceSize = sourceModel.getSize();
      for (int i=0; i<sourceSize; i++) {
        boolean excepted = false;
        Object o = sourceModel.getElementAt(i);
        if (anyExceptions && ArrayUtils.find(exceptions, o) >= 0) {
          excepted = true;
        }
        String renderedText = null;
        if (!excepted)
          renderedText = renderer.getRenderedTextApplySettings(o);
        int matchStrengthReturned = StringHighlighter.MATCH_STRING__NO_MATCH;
        if (!excepted && anyExceptions) {
          if (ArrayUtils.find(exceptions, renderedText) >= 0) {
            excepted = true;
          }
        }
        if (!excepted) {
          matchStrengthReturned = StringHighlighter.matchStrings(renderedText, highlighter, false, null);
        }
        if (o instanceof ObjectsProviderUpdaterI || matchStrengthReturned != StringHighlighter.MATCH_STRING__NO_MATCH) {
          objsToKeepV.addElement(o);
        } else {
          objsToRemoveV.addElement(o);
        }
      }

      for (int i=0; i<objsToRemoveV.size(); i++) {
        sourceListModel.removeElement(objsToRemoveV.elementAt(i));
      }
      for (int i=0; i<objsToKeepV.size(); i++) {
        Object o = objsToKeepV.elementAt(i);
        if (!sourceListModel.contains(o)) {
          sourceListModel.addElement(o);
        }
      }
    }
  }


  public static boolean highlightItemByName(JList sourceList, StringHighlighterI highlighter, boolean selectBestFit, boolean changeSelectionIfFitSelected, boolean deselectWhenNotFound) {
    int matchLevel = StringHighlighter.MATCH_STRING__NO_MATCH;
    int matchIndex = -1;
    String[] returnBuffer = new String[1];

    ListRenderer renderer = (ListRenderer) sourceList.getCellRenderer();
    renderer = (ListRenderer) renderer.clone();
    renderer.setStringHighlighter(null);

    ListModel sourceListModel = sourceList.getModel();

    int currentSelection = sourceList.getSelectedIndex();
    boolean currentSelectionFits = false;

    boolean found = false;
    for (int index=0; index<sourceListModel.getSize(); index++) {
      Object o = sourceListModel.getElementAt(index);
      String label = renderer.getRenderedTextApplySettings(o);
      int match = StringHighlighter.matchStrings(label, highlighter, true, returnBuffer);
      if (o instanceof ObjectsProviderUpdaterI || match != StringHighlighter.MATCH_STRING__NO_MATCH) {
        if (index == currentSelection) currentSelectionFits = true;
        if (selectBestFit) {
          found = true;
          if (matchLevel < match) {
            matchLevel = match;
            matchIndex = index;
          }
        } else {
          found = true;
          matchLevel = match;
          matchIndex = index;
          if (changeSelectionIfFitSelected || currentSelectionFits)
            break;
        }
        if ((currentSelection == -1 && !selectBestFit) || (!changeSelectionIfFitSelected && currentSelectionFits))
          break;
      }
    }
    if (found) {
      if (changeSelectionIfFitSelected || !currentSelectionFits) {
        if (matchIndex != currentSelection) {
          sourceList.setSelectedIndex(matchIndex);
          sourceList.ensureIndexIsVisible(matchIndex);
        }
      }
    }
    if (!found && deselectWhenNotFound) {
      sourceList.clearSelection();
    }
    return found;
  }


  public static void moveSourceSelection(JList sourceList, boolean moveDown, boolean byPage) {
    int size = sourceList.getModel().getSize();
    if (size > 0) {
      boolean rollOverEnabled = false;
      int index = sourceList.getSelectedIndex();
      int newIndex = 0;
      if (index >= 0) {
        int maxIndex = size - 1;
        int moveBy = byPage ? sourceList.getVisibleRowCount() : 1;
        if (moveBy < 1) 
          moveBy = 1;
        if (!moveDown) moveBy = -moveBy;
        newIndex = index + moveBy;
        if (rollOverEnabled) {
          if (newIndex < 0 && index > 0)
            newIndex = 0;
          else if (newIndex > maxIndex && index < maxIndex)
            newIndex = maxIndex;
          else if (newIndex < 0)
            newIndex = maxIndex;
          else if (newIndex > maxIndex)
            newIndex = 0;
        } else {
          if (newIndex < 0)
            newIndex = 0;
          else if (newIndex > maxIndex)
            newIndex = maxIndex;
        }
      }
      sourceList.setSelectedIndex(newIndex);
      sourceList.ensureIndexIsVisible(newIndex);
    }
  }


  public static String getLastElement(String str) {
    String lastToken = "";
    str = str.trim();
    if (str.endsWith(",") || str.endsWith(";"))
      lastToken = "";
    else {
      StringTokenizer st = new StringTokenizer(str, ",;");
      lastToken = str;
      while (st.hasMoreTokens())
        lastToken = st.nextToken().trim();
    }
    return lastToken;
  }


  public static String stripLastElement(String str) {
    String last = getLastElement(str);
    if (last.length() > 0) {
      int index = str.lastIndexOf(last);
      if (index >= 0) {
        if (index == 0)
          str = "";
        else
          str = str.substring(0, index);
        str = str.trim();
        while (str.endsWith(",") || str.endsWith(";")) {
          str = str.substring(0, str.length()-1).trim();
        }
      }
    }
    return str;
  }

}
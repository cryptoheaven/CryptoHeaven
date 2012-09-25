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

package com.CH_gui.util;

import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;

/** 
* <b>Copyright</b> &copy; 2001-2012
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
* Class Description:
*
*
* Class Details:
*
*
* <b>$Revision: 1.17 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class ActionUtils extends Object {

  // property that overwrites actions to become non-disabable
  public static String DISABABLE = "disabable";

  /**
  * Gets recursively Action Producers from all Components that descend from <code> c </code>.
  */
  private static void fillActionProducersRecursively(Component c, List targetList) {
    boolean finalProducer = false;
    if (!targetList.contains(c)) {
      if (c instanceof ActionProducerI) {
        targetList.add(c);
        finalProducer = ((ActionProducerI)c).isFinalActionProducer();
      }

      if (!finalProducer && c instanceof Container) {
        Component[] components = ((Container)c).getComponents();
        for (int i=0; i<components.length; i++)
          fillActionProducersRecursively(components[i], targetList);
      }

      if (!finalProducer && c instanceof ComponentContainerI) {
        Component[] components = ((ComponentContainerI)c).getPotentiallyHiddenComponents();
        for (int i=0; i<components.length; i++)
          fillActionProducersRecursively(components[i], targetList);
      }
    }
  }

  /**
  * Gets recursively actions from all ActionProducersI that <code> c </code> contains
  * merges them and returns as an array of menu names.
  */
  public static Action[] getActionsRecursively(Component forComponent) {
    Action[] actionArray = null;

    ArrayList targetList = new ArrayList();
    fillActionProducersRecursively(forComponent, targetList);

    ArrayList actionList = new ArrayList();
    Iterator iter = targetList.iterator();
    while (iter.hasNext()) {
      ActionProducerI actionProducer = (ActionProducerI) iter.next();
      Action[] actions = actionProducer.getActions();
      // laizly create the list
      if (actions != null && actions.length > 0) {
        for (int k=0; k<actions.length; k++) {
          Action action = actions[k];
          if (action != null)
            actionList.add(action);
        }
      }
    }

    if (actionList.size() > 0) {
      actionArray = (Action[]) ArrayUtils.toArray(actionList, Action.class);
    }
    return actionArray;
  }

  /**
  * Gets recursively all ActionProducerI that <code> c </code> contains
  * merges them and returns as an array of ActionProducerI.
  */
  public static ActionProducerI[] getActionProducersRecursively(Component forComponent) {
    ArrayList targetList = new ArrayList();
    fillActionProducersRecursively(forComponent, targetList);
    ActionProducerI[] producerArray = (ActionProducerI[]) ArrayUtils.toArray(targetList, ActionProducerI.class);
    return producerArray;
  }


  /**
  * Disable all actions that are placed in this component.
  */
  public static void disableAllActions(Component forComponent) {
    Action[] allActions = getActionsRecursively(forComponent);
    if (allActions != null && allActions.length > 0) {
      for (int i=0; i<allActions.length; i++) {
        Boolean disabable = (Boolean) allActions[i].getValue(DISABABLE);
        if (disabable == null || disabable.booleanValue() == true)
          allActions[i].setEnabled(false);
      }
    }
  }

  /**
  * Sets correct enablement for all actions that are placed in this component
  * by calling setEnabledActions on all ActionProducerI objects placed in the specified gui.
  */
  public static void setEnabledActionsRecur(Component forComponent) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ActionUtils.class, "setEnabledActionsRecur(Component forComponent)");
    if (trace != null) trace.args(forComponent != null ? forComponent.getClass().getName() : "null");
    ActionProducerI[] allProducers = getActionProducersRecursively(forComponent);
    if (allProducers != null && allProducers.length > 0) {
      for (int i=0; i<allProducers.length; i++) {
        try {
          allProducers[i].setEnabledActions();
        } catch (Exception e) {
          if (trace != null) trace.exception(ActionUtils.class, 100, e);
        }
      }
    }
    if (trace != null) trace.exit(ActionUtils.class);
  }


  /**
  * Sets all specified actions with specified enablement.
  */
  public static void setEnabledActions(Action[] actions, boolean isEnabled) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ActionUtils.class, "setEnabledActions(Action[] actions, boolean isEnabled)");
    if (trace != null) trace.args(actions);
    if (trace != null) trace.args(isEnabled);
    if (actions != null) {
      for (int i=0; i<actions.length; i++) {
        if (actions[i] != null) {
          if (isEnabled == false) {
            Boolean disabable = (Boolean) actions[i].getValue(DISABABLE);
            if (disabable == null || disabable.booleanValue() == true)
              actions[i].setEnabled(false);
          } else {
            actions[i].setEnabled(true);
          }
        }
      }
    }
    if (trace != null) trace.exit(ActionUtils.class);
  }

}
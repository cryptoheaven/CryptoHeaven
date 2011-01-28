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

package com.CH_gui.action;

import com.CH_co.trace.Trace;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;

/**
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public abstract class AbstractActionTraced extends AbstractAction {

  public AbstractActionTraced() {
    super();
  }

  public AbstractActionTraced(String name) {
    super(name);
  }

  public AbstractActionTraced(String name, Icon icon) {
    super(name, icon);
  }

  public abstract void actionPerformedTraced(ActionEvent event);

  public void actionPerformed(ActionEvent event) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "actionPerformed(ActionEvent event)");
    if (trace != null) trace.args(event);
    try {
      actionPerformedTraced(event);
    } catch (Throwable t) {
      if (trace != null) trace.exception(getClass(), 100, t);
    }
    if (trace != null) trace.exit(getClass());
  }

}
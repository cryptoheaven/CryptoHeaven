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

package com.CH_gui.gui;

import com.CH_gui.util.VisualsSavable;
import com.CH_gui.util.MiscGui;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

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
 * <b>$Revision: 1.2 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class JSplitPaneVS extends JSplitPane implements VisualsSavable {

  private String propertyKey = null;
  private String propertyExtension = null;

  /** Creates new JSplitPaneVS */
  public JSplitPaneVS(String propertyName, int orientation, double resizeWeight) {
    super(orientation);
    initialize(propertyName, resizeWeight);
  }
  public JSplitPaneVS(String propertyName, int orientation, Component c1, Component c2, double resizeWeight) {
    super(orientation, c1, c2);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JSplitPaneVS.class, "JSplitPaneVS()");
    initialize(propertyName, resizeWeight);
    if (trace != null) trace.exit(JSplitPaneVS.class);
  }
  private void initialize(String propertyName, double resizeWeight) {
    propertyExtension = propertyName;
    propertyKey = MiscGui.getVisualsKeyName(this);
    restoreVisuals(GlobalProperties.getProperty(propertyKey));
    setResizeWeight(resizeWeight); // allow resize weight to change with client updates -- overwrite saved property
    if (MiscGui.isSmallScreen())
      setDividerSize(2);
    setBorder(new EmptyBorder(0,0,0,0));
  }


  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public String getVisuals() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JSplitPaneVS.class, "getVisuals()");

    StringBuffer visuals = new StringBuffer();
    visuals.append("Divider location ");
    visuals.append(getDividerLocation());
    visuals.append(' ');
    visuals.append(getLastDividerLocation());
    visuals.append(' ');
    visuals.append((int)(getResizeWeight()*100));
    visuals.append(' ');
    visuals.append(getOrientation());

    String rc = visuals.toString();
    if (trace != null) trace.exit(JSplitPaneVS.class, rc);
    return  rc;
  }

  public void restoreVisuals(String visuals) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JSplitPaneVS.class, "restoreVisuals(String visuals)");
    if (trace != null) trace.args(visuals);

    try {
      if (visuals == null || visuals.length() == 0) {
      } else {
        StringTokenizer st = new StringTokenizer(visuals);  
        st.nextToken(); st.nextToken();
        int divLocation = Integer.parseInt(st.nextToken());
        int divLocationLast = Integer.parseInt(st.nextToken());
        double resizeWeight = ((double) Integer.parseInt(st.nextToken())) / 100d;
        if (st.hasMoreTokens()) {
          setOrientation(Integer.parseInt(st.nextToken()));
        }
        setDividerLocation(divLocation);
        setLastDividerLocation(divLocationLast);
        setResizeWeight(resizeWeight);
      }
    } catch (Throwable t) {
      if (trace != null) trace.exception(JSplitPaneVS.class, 100, t);
      // reset the properties since they are corrupted
      GlobalProperties.resetMyAndGlobalProperties();
    }

    if (trace != null) trace.exit(JSplitPaneVS.class);
  }

  public String getExtension() {
    return propertyExtension;
  }
  public static final String visualsClassKeyName = "JSplitPaneVS";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
  public Integer getVisualsVersion() {
    return null;
  }
  public boolean isVisuallyTraversable() {
    return true;
  }

}
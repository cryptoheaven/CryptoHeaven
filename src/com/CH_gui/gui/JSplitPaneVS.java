/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_gui.gui;

import com.CH_co.trace.Trace;
import com.CH_co.util.GlobalProperties;
import com.CH_gui.util.MiscGui;
import com.CH_gui.util.VisualsSavable;
import java.awt.Component;
import java.util.StringTokenizer;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

/** 
 * <b>Copyright</b> &copy; 2001-2013
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
 * <b>$Revision: 1.2 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class JSplitPaneVS extends JSplitPane implements VisualsSavable {

  private String propertyKey = null;
  private String propertyExtension = null;

  /** Creates new JSplitPaneVS */
  public JSplitPaneVS(String propertyName, int defaultOrientation, double resizeWeightH, double resizeWeightV) {
    super(defaultOrientation);
    initialize(propertyName, resizeWeightH, resizeWeightV);
  }
  public JSplitPaneVS(String propertyName, int defaultOrientation, Component c1, Component c2, double resizeWeightH, double resizeWeightV) {
    super(defaultOrientation, c1, c2);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JSplitPaneVS.class, "JSplitPaneVS()");
    initialize(propertyName, resizeWeightH, resizeWeightV);
    if (trace != null) trace.exit(JSplitPaneVS.class);
  }
  private void initialize(String propertyName, double resizeWeightH, double resizeWeightV) {
    propertyExtension = propertyName;
    propertyKey = MiscGui.getVisualsKeyName(this);
    restoreVisuals(GlobalProperties.getProperty(propertyKey));
    // Adjust resize-weight depending on the final orientation
    if (getOrientation() == JSplitPane.HORIZONTAL_SPLIT)
      setResizeWeight(resizeWeightH);
    else if (getOrientation() == JSplitPane.VERTICAL_SPLIT)
      setResizeWeight(resizeWeightV);
    if (MiscGui.isSmallScreen())
      setDividerSize(2);
    setBorder(new EmptyBorder(0,0,0,0));
    if (getDividerSize() > 5)
      setDividerSize(5);
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
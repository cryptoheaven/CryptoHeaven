/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_gui.localFileTable;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

import com.CH_co.gui.*;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.action.*;
import com.CH_gui.list.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: 1.19 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FileChooserComponent extends JPanel implements VisualsSavable {

  private JFileChooser jFileChooser;
  private String propertyName;

  public FileChooserComponent(String propertyName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileChooserComponent.class, "FileChooserComponent(String propertyName)");
    this.propertyName = propertyName;
    
    jFileChooser = new DNDActionFileChooser(propertyName);
    jFileChooser.setMultiSelectionEnabled(true);
    jFileChooser.setControlButtonsAreShown(false);
    init();
    
    restoreVisuals(GlobalProperties.getProperty(MiscGui.getVisualsKeyName(this)));
    
    if (trace != null) trace.exit(FileChooserComponent.class);
  }
  /** 
   * Creates new FileChooserComponent.
   */
  public FileChooserComponent(File currentDirectory) {
    this(new DNDActionFileChooser(currentDirectory));
    jFileChooser.setMultiSelectionEnabled(true);
    jFileChooser.setControlButtonsAreShown(false);
  }
  private FileChooserComponent(JFileChooser jFileChooser) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileChooserComponent.class, "FileChooserComponent(JFileChooser jFileChooser)");
    if (trace != null) trace.args(jFileChooser);

    this.jFileChooser = jFileChooser;
    init();

    /** If right mouse button is clicked then the popup is shown. */
    //if (recordTableScrollPane instanceof ActionProducerI)
      //addMouseListener(new PopupMouseAdapter(this, (ActionProducerI) recordTableScrollPane));

    if (trace != null) trace.exit(FileChooserComponent.class);
  }
  
  public JFileChooser getJFileChooser() {
    return jFileChooser;
  }

  private void init() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileChooserComponent.class, "init()");
    setLayout(new GridBagLayout());

    AbstractButton refreshButton = null;
    AbstractButton cloneButton = null;

    if (jFileChooser instanceof DNDActionFileChooser) {
      DNDActionFileChooser actionFileChooser = (DNDActionFileChooser) jFileChooser;
      Action refreshAction = actionFileChooser.getRefreshAction();
      Action cloneAction = actionFileChooser.getCloneAction();

      if (refreshAction != null)
        refreshButton = ActionUtilities.makeSmallComponentToolButton(refreshAction);
      if (cloneAction != null)
        cloneButton = ActionUtilities.makeSmallComponentToolButton(cloneAction);
    }

    JLabel titleLabel = new JMyLabel(FolderShareRecord.SHARE_LOCAL_NAME);
    titleLabel.setIcon(Images.get(ImageNums.MY_COMPUTER16));
    add(titleLabel, new GridBagConstraints(0, 0, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    if (refreshButton != null) {
      add(refreshButton, new GridBagConstraints(2, 0, 1, 1, 0, 0, 
          GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(0, 1, 0, 1), 0, 0));
    }
    if (cloneButton != null) {
      add(cloneButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, 
          GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(0, 1, 0, 1), 0, 0));
    }

    add(jFileChooser, new GridBagConstraints(0, 1, 3, 1, 10, 10, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));

    restoreVisuals(GlobalProperties.getProperty(MiscGui.getVisualsKeyName(this), "Dimension width 160 height 180"));

    if (trace != null) trace.exit(FileChooserComponent.class);
  }


  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/

  public String getVisuals() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileChooserComponent.class, "getVisuals()");

    StringBuffer visuals = new StringBuffer();
    visuals.append("Dimension width ");
    Dimension dim = getSize();
    visuals.append(dim.width);
    visuals.append(" height ");
    visuals.append(dim.height);

    String rc = visuals.toString();
    if (trace != null) trace.exit(FileChooserComponent.class, rc);
    return rc;
  }

  public void restoreVisuals(String visuals) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileChooserComponent.class, "restoreVisuals(String visuals)");
    if (trace != null) trace.args(visuals);

    try {
      if (visuals != null && visuals.length() > 0) {
        StringTokenizer st = new StringTokenizer(visuals);  
        st.nextToken();
        st.nextToken();
        int width = Integer.parseInt(st.nextToken());
        st.nextToken();
        int height = Integer.parseInt(st.nextToken());
        setPreferredSize(new Dimension(width, height));
      }
    } catch (Throwable t) {
      if (trace != null) trace.exception(FileChooserComponent.class, 100, t);
    }

    if (trace != null) trace.exit(FileChooserComponent.class);
  }

  public String getExtension() {
    return propertyName;
  }
  public static final String visualsClassKeyName = "FileChooserComponent";
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
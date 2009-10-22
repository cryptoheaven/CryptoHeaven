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

package com.CH_gui.fileTable;

import com.CH_gui.gui.Template;
import java.awt.dnd.*;

import com.CH_co.trace.Trace;

import com.CH_gui.gui.*;
import com.CH_gui.table.*;

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
 * <b>$Revision: 1.16 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FileTableComponent extends RecordTableComponent {

  /** Creates new FileTableComponent */
  public FileTableComponent() {
    super(new FileActionTable(), Template.get(Template.EMPTY_FILES), Template.get(Template.BACK_FILES), Template.get(Template.CATEGORY_FILE));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileTableComponent.class, "FileTableComponent()");
    if (trace != null) trace.exit(FileTableComponent.class);
  }

  public void initDataModel(Long folderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileTableComponent.class, "initDataModel(Long folderId)");
    if (trace != null) trace.args(folderId);
    ((FileTableModel) getActionTable().getTableModel()).initData(folderId);
    if (trace != null) trace.exit(FileTableComponent.class);  
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "FileTableComponent";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
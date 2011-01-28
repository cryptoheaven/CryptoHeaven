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

package com.CH_gui.tree;

import com.CH_co.service.records.FolderPair;
import com.CH_co.service.records.filters.RecordFilter;

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
public class FolderTreeComponent4Frame extends FolderTreeComponent {

  public FolderTreeComponent4Frame(boolean withActions, RecordFilter filter, FolderPair[] initialFolderPairs, boolean withExploreUtilityTool) {
    super(withActions, filter, initialFolderPairs, withExploreUtilityTool);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "FolderTreeComponent4Frame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }

}
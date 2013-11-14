/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.util;

import com.CH_gui.menuing.ToolBarModel;
import java.awt.Component;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public interface ToolBarProducerI {

  public ToolBarModel getToolBarModel();

  public String getToolBarTitle();

  public ToolBarModel initToolBarModel(String propertyKeyName, String toolBarName, Component source);

}
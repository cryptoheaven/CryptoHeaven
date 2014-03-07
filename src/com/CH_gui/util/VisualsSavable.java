/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.util;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public interface VisualsSavable {

  public String getVisuals();

  public void restoreVisuals(String visuals);

  public String getVisualsClassKeyName();

  public String getExtension();

  public Integer getVisualsVersion();

  public boolean isVisuallyTraversable();

}
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

import javax.swing.Action;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved. 
 *
 * @author  Marcin Kurzawa 
 */
public interface ActionProducerI {

  /**
   * @return all the acitons that this objects produces.
   */
  public Action[] getActions();

  /**
   * Final Action Producers will not be traversed to collect its containing objects' actions.
   * @return true if this object will gather all actions from its childeren or hide them counciously.
   */
  public boolean isFinalActionProducer();

  /**
   * Enables or Disables actions based on the current state of the Action Producing component.
   */
  public void setEnabledActions();
}
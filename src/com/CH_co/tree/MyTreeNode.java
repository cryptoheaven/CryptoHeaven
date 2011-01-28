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
package com.CH_co.tree;

import java.util.*;

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
public interface MyTreeNode {

  /**
   * Returns the child <code>TreeNode</code> at index
   * <code>childIndex</code>.
   */
  MyTreeNode getChildNodeAt(int childIndex);

  /**
   * Returns the number of children <code>TreeNode</code>s the receiver
   * contains.
   */
  int getChildCount();

  /**
   * Returns the parent <code>TreeNode</code> of the receiver.
   */
  MyTreeNode getParentNode();

  /**
   * Returns the index of <code>node</code> in the receivers children.
   * If the receiver does not contain <code>node</code>, -1 will be
   * returned.
   */
  int getIndex(MyTreeNode node);

  /**
   * Returns true if the receiver allows children.
   */
  boolean getAllowsChildren();

  /**
   * Returns true if the receiver is a leaf.
   */
  boolean isLeaf();

  /**
   * Returns the children of the receiver as an <code>Enumeration</code>.
   */
  Enumeration children();
}
/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_co.tree;

/**
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public interface MyMutableTreeNode extends MyTreeNode {

  /**
     * Adds <code>child</code> to the receiver at <code>index</code>.
     * <code>child</code> will be messaged with <code>setParent</code>.
     */
    void insert(MyMutableTreeNode child, int index);

    /**
     * Removes the child at <code>index</code> from the receiver.
     */
    void remove(int index);

    /**
     * Removes <code>node</code> from the receiver. <code>setParent</code>
     * will be messaged on <code>node</code>.
     */
    void remove(MyMutableTreeNode node);

    /**
     * Resets the user object of the receiver to <code>object</code>.
     */
    void setUserObject(Object object);

    /**
     * Removes the receiver from its parent.
     */
    void removeFromParent();

    /**
     * Sets the parent of the receiver to <code>newParent</code>.
     */
    void setParent(MyMutableTreeNode newParent);

}
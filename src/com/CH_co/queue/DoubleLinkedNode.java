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

package com.CH_co.queue;

/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class DoubleLinkedNode extends Object {

  private DoubleLinkedNode previous;
  private Object obj;
  private DoubleLinkedNode next;

  /** Creates new DoubleLinkedNode */
  public DoubleLinkedNode() {
  }

  /** Creates new DoubleLinkedNode and set its object. */
  public DoubleLinkedNode(Object obj) {
    this.obj = obj;
  }

  /** Link a given node after 'this' node */
  public void linkNext(DoubleLinkedNode node) {
    this.next = node;
    node.previous = this;
  }

  /** Link a given node in front of 'this' node */
  public void linkBefore(DoubleLinkedNode node) {
    this.previous = node;
    node.next = this;
  }

  public void setObject(Object obj) {
    this.obj = obj;
  }

  public Object getObject() {
    return obj;
  }

  public DoubleLinkedNode getNext() {
    return next;
  }

  public DoubleLinkedNode getPrevious() {
    return previous;
  }

  /** Brake a link between 'this' and the next node.
      @return the next node.
  */
  public DoubleLinkedNode breakNext() {
    DoubleLinkedNode nextNode = next;
    nextNode.previous = null;
    next = null;
    return nextNode;
  }

  /** Brake a link between 'this' and the previous node.
      @return the previous node.
  */
  public DoubleLinkedNode breakPrevious() {
    DoubleLinkedNode previousNode = previous;
    previousNode.next = null;
    previous = null;
    return previousNode;
  }

  /** Set the next and previous nodex to null.  Set the stored object to null. */
  public void clear() {
    previous = null;
    obj = null;
    next = null;
  }
}
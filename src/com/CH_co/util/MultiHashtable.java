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

package com.CH_co.util;

import java.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Hashtable that allows many values for each key.
 * @author  Marcin Kurzawa
 * @version 
 */
public class MultiHashtable extends Object {

  private Hashtable ht;
  private boolean enforceUniqueValues;

  /** Creates new MultiHashtable */
  public MultiHashtable() {
    ht = new Hashtable();
  }
  /**
   * Enforces unique values without requirement for keys to be unique.
   * By default values just like keys don't have to be unique.
   */
  public MultiHashtable(boolean uniqueValues) {
    ht = new Hashtable();
    enforceUniqueValues = uniqueValues;
  }

  public MultiHashtable(int initialCapacity) {
    ht = new Hashtable(initialCapacity);
  }

  public MultiHashtable(int initialCapacity, float loadFactor) {
    ht = new Hashtable(initialCapacity, loadFactor);
  }

  public MultiHashtable(Map t) {
    ht = new Hashtable(t);
  }

  /**
   * Store a value for a specified key.  When value is null store it too.
   */
  public synchronized Object put(Object key, Object value) {
    Object o = ht.get(key);
    if (enforceUniqueValues && value.equals(o)) {
      // skip -- same value
    } else {
      if (o == null)
        ht.put(key, value);
      else if (o instanceof MyVector) {
        MyVector v = (MyVector) o;
        o = v.elementAt(0);
        if (enforceUniqueValues && v.contains(value)) {
          // skip -- already present
        } else {
          v.addElement(value);
        }
      } else {
        MyVector v = new MyVector();
        v.addElement(o);
        v.addElement(value);
        ht.put(key, v);
      }
    }
    return o;
  }

  /**
   * @return the first value stored for a given key.
   */
  public synchronized Object get(Object key) {
    Object o = ht.get(key);
    if (o instanceof MyVector)
      o = ((MyVector)o).elementAt(0);
    return o;
  }

  /**
   * @return all values stored for a given key.
   */
  public synchronized Vector getAll(Object key) {
    Object o = ht.get(key);
    Vector v = null;
    if (o != null) {
      if (o instanceof MyVector) {
        v = (MyVector) o;
      } else {
        v = new MyVector();
        v.addElement(o);
        // Cache the MyVector with stored single object so that
        // next time the same query will return this cached vector.
        ht.put(key, v);
      }
    }
    return v;
  }

  /**
   * Remove the first value from the set stored for a given key.
   * Do not remove MyVector structure if it has at least 1 element...
   */
  public synchronized Object remove(Object key) {
    Object o = ht.remove(key);
    if (o instanceof MyVector) {
      MyVector v = (MyVector) o;
      o = v.elementAt(0);
      v.removeElementAt(0);
      if (v.size() > 0) {
        ht.put(key, v);
//        if (v.size() == 1)
//          ht.put(key, v.elementAt(0));
//        else
//          ht.put(key, v);
      }
    }
    return o;
  }

  /**
   * Remove the 'value' object from the set stored for a given key.
   */
  public synchronized Object remove(Object key, Object value) {
    Object o = ht.remove(key);
    if (o instanceof MyVector) {
      MyVector v = (MyVector) o;
      int index = v.indexOf(value);
      if (index >= 0) {
        o = v.elementAt(index);
        v.removeElementAt(index);
      } else {
        // the vector doesn't have the object we are looking for
        o = null;
      }
      if (v.size() > 0) {
        ht.put(key, v);
//        if (v.size() == 1)
//          ht.put(key, v.elementAt(0));
//        else
//          ht.put(key, v);
      }
    } else if (o != null && !o.equals(value)) {
      // push back the object if its not what we were looking for
      ht.put(key, o);
      o = null;
    }
    return o;
  }

  /**
   * Remove the first value from the set stored for a given key.
   */
  public synchronized Vector removeAll(Object key) {
    Vector v = getAll(key);
    ht.remove(key);
    return v;
  }

  public synchronized Enumeration keys() {
    return ht.keys();
  }

  public synchronized void clear() {
    ht.clear();
  }

  private static class MyVector extends Vector {
  }
}
/*
* Copyright 2001-2013 by CryptoHeaven Corp.,
* Mississauga, Ontario, Canada.
* All rights reserved.
*
* This software is the confidential and proprietary information
* of CryptoHeaven Corp. ("Confidential Information").  You
* shall not disclose such Confidential Information and shall use
* it only in accordance with the terms of the license agreement
* you entered into with CryptoHeaven Corp.
*/

package com.CH_co.util;

import java.util.*;

/**
* <b>Copyright</b> &copy; 2001-2013
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
* HashMap that allows many values for each key. Same as MultiHashtable but not synchronized.
* 
* @author  Marcin Kurzawa
* @version
*/
public class MultiHashMap extends Object {

  private HashMap hm;
  private boolean enforceUniqueValues;
  private int valueCount;

  /** Creates new MultiHashMap */
  public MultiHashMap() {
    hm = new HashMap();
  }
  /**
  * Enforces unique values without requirement for keys to be unique.
  * By default values just like keys don't have to be unique.
  */
  public MultiHashMap(boolean uniqueValues) {
    hm = new HashMap();
    enforceUniqueValues = uniqueValues;
  }

  public MultiHashMap(int initialCapacity) {
    hm = new HashMap(initialCapacity);
  }

  public MultiHashMap(int initialCapacity, float loadFactor) {
    hm = new HashMap(initialCapacity, loadFactor);
  }

  public MultiHashMap(Map m) {
    hm = new HashMap(m);
  }

/**
  * Store a value for a specified key.  When value is null store it too.
  */
  public void put(Object key, Object value) {
    Object o = hm.get(key);
    if (!enforceUniqueValues || !value.equals(o)) {
      if (o == null) {
        hm.put(key, value);
        valueCount ++;
      } else if (o instanceof MyCollection) {
        MyCollection v = (MyCollection) o;
        if (!enforceUniqueValues || !v.contains(value)) {
          v.add(value);
          valueCount ++;
        }
      } else {
        MyCollection c = null;
        if (enforceUniqueValues)
          c = new MyHashSet();
        else
          c = new MyArrayList();
        c.add(o);
        c.add(value);
        hm.put(key, c);
        valueCount ++;
      }
    }
  }

  /**
  * @return the first value stored for a given key.
  */
  public Object get(Object key) {
    Object o = hm.get(key);
    if (o instanceof MyCollection) {
      if (o instanceof MyArrayList) {
        o = ((MyArrayList)o).get(0);
      } else if (o instanceof MyHashSet) {
        o = ((MyHashSet)o).iterator().next();
      }
    }
    return o;
  }

  /**
  * @return all values stored for a given key.
  */
  public Collection getAll(Object key) {
    Object o = hm.get(key);
    Collection c = null;
    if (o != null) {
      if (o instanceof MyCollection) {
        c = (MyCollection) o;
      } else {
        if (enforceUniqueValues)
          c = new MyHashSet();
        else
          c = new MyArrayList();
        c.add(o);
        // Cache the MyCollection with stored single object so that
        // next time the same query will return this cached collection.
        hm.put(key, c);
      }
    }
    // Copy over the values to prevent the caller from changing our collection.
    return c != null ? new ArrayList(c) : null;
  }

  /**
  * Remove the first value from the set stored for a given key.
  * Do not remove MyVector structure if it has at least 1 element...
  */
  public Object remove(Object key) {
    Object o = hm.remove(key);
    if (o instanceof MyCollection) {
      MyCollection c = (MyCollection) o;
      Iterator iter = c.iterator();
      o = iter.next();
      iter.remove();
      valueCount --;
      if (c.size() > 0) {
        hm.put(key, c);
      }
    } else if (o != null) {
      valueCount --;
    }
    return o;
  }

  /**
  * Remove the 'value' object from the set stored for a given key.
  */
  public Object remove(Object key, Object value) {
    Object o = hm.remove(key);
    if (o instanceof MyCollection) {
      MyCollection c = (MyCollection) o;
      boolean removed = c.remove(value);
      if (removed) {
        o = value;
        valueCount --;
      } else {
        // the vector doesn't have the object we are looking for
        o = null;
      }
      if (c.size() > 0) {
        hm.put(key, c);
      }
    } else if (o != null && !o.equals(value)) {
      // push back the object if its not what we were looking for
      hm.put(key, o);
      o = null;
    } else if (o != null) {
      valueCount --;
    }
    return o;
  }

  /**
  * Remove all values stored for a given key.
  */
  public void removeAll(Object key) {
    Object o = hm.remove(key);
    if (o instanceof MyCollection) {
      MyCollection c = (MyCollection) o;
      valueCount -= c.size();
    } else if (o != null) {
      valueCount --;
    }
  }

  /**
  * Pool function is a composite of get and remove functions.
  */
  public Collection poolAll(Object key) {
    Collection c = getAll(key);
    removeAll(key);
    return c;
  }

  public Set keys() {
    return hm.keySet();
  }

  public void clear() {
    hm.clear();
    valueCount = 0;
  }

  public boolean hasMultivalues() {
    return sizeKeys() != sizeValues();
  }

  public boolean isEmpty() {
    return hm.isEmpty();
  }

  public int sizeKeys() {
    return hm.size();
  }

  public int sizeValues() {
    return valueCount;
  }

  private static interface MyCollection extends Collection {
  }
  private static class MyHashSet extends HashSet implements MyCollection {
  }
  private static class MyArrayList extends ArrayList implements MyCollection {
  }
}
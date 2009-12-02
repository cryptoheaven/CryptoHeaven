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

package com.CH_co.service.records;

import java.util.*;
import java.lang.reflect.Array;

import com.CH_co.service.records.filters.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: Static utility methods that work on Records
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.20 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class RecordUtils extends Object {

  /** Creates new RecordUtils */
  public RecordUtils() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "RecordUtils()");
    if (trace != null) trace.exit(RecordUtils.class);
  }

  /** @return records' IDs */
  public static Long[] getIDs(Record[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "getIDs(Record[] records)");
    if (trace != null) trace.args(records);

    Long[] ids = null;
    if (records != null) {
      ids = new Long[records.length];
      for (int i=0; i<records.length; i++) {
        ids[i] = records[i].getId();
      }
    }

    if (trace != null) trace.exit(RecordUtils.class, ids);
    return ids;
  }
  /** @return records' IDs */
  public static Long[] getIDs(Vector records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "getIDs(Vector records)");
    if (trace != null) trace.args(records);

    Long[] ids = null;
    if (records != null) {
      ids = new Long[records.size()];
      for (int i=0; i<records.size(); i++) {
        ids[i] = ((Record) records.elementAt(i)).getId();
      }
    }

    if (trace != null) trace.exit(RecordUtils.class, ids);
    return ids;
  }
  /** @return IDs */
  public static Long[] getIDs2(Vector IDsV) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "getIDs(Vector IDsV)");
    if (trace != null) trace.args(IDsV);
    Long[] ids = (Long[]) ArrayUtils.toArray(IDsV, Long.class);
    if (trace != null) trace.exit(RecordUtils.class, ids);
    return ids;
  }


  /** @return String in brackets of record IDs separated by a comma.  Eg: "(1,2,3)" */
  public static String getIDsStr(Vector recordsV) {
    Record[] records = (Record[]) ArrayUtils.toArray(recordsV, Record.class);
    return getIDsStr(records);
  }

  /** @return String in brackets of record IDs separated by a comma.  Eg: "(1,2,3)" */
  public static String getIDsStr2(Vector IDsV) {
    Long[] IDs = (Long[]) ArrayUtils.toArray(IDsV, Long.class);
    return getIDsStr(IDs);
  }

  /** @return String in brackets of record IDs separated by a comma.  Eg: "(1,2,3)" */
  public static String getIDsStr(Record[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "getIDsStr(Record[] records)");
    if (trace != null) trace.args(records);

    StringBuffer ids = new StringBuffer("(");
    for (int i=0; i<records.length; i++) {
      ids.append(records[i].getId());
      if (i+1 < records.length)
        ids.append(",");
    }
    ids.append(")");

    if (trace != null) trace.exit(RecordUtils.class, ids.toString());
    return ids.toString();
  }

  /** @return String in brackets of integers separated by a comma.  Eg: "(1,2,3)" */
  public static String getIDsStr(Long[] ints) {
    return getIDsStr2(ints);
  }
  /** @return String in brackets of integers separated by a comma.  Eg: "(1,2,3)" */
  public static String getIDsStr2(Object[] ints) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "getIDsStr2(Object[] ints)");
    if (trace != null) trace.args(ints);

    StringBuffer ids = new StringBuffer("(");
    for (int i=0; i<ints.length; i++) {
      ids.append(ints[i]);
      if (i+1 < ints.length)
        ids.append(",");
    }
    ids.append(")");

    if (trace != null) trace.exit(RecordUtils.class, ids.toString());
    return ids.toString();
  }


  /**
   * @return an array with elements that exist in both arrays comparing using the getId() method.  
   * Elements from the sourceMap are returned.
   */
  public static Record[] AND(Map sourceMap, Record[] compareTo) {
    return pick(sourceMap, compareTo, true);
  }
  /**
   * @return an array with elements that exist only in compareTo array as compared using the getId().equals() method
   * Elements from the compareTo array are returned.
   */
  public static Record[] NOT(Map sourceMap, Record[] compareTo) {
    return pick(sourceMap, compareTo, false);
  }


  /**
   * @return an array with elements that EXIST in the sourceMap when probed from compareTo array.
   * Elements from the sourceMap if it exists there, or from compareTo is it doesn't exist in the sorceMap.
   */
  private static Record[] pick(Map sourceMap, Record[] compareTo, boolean exist) {
    Vector resultsV = new Vector();

    for (int i=0; i<compareTo.length; i++) {
      Object o = sourceMap.get(compareTo[i].getId());
      if ((o != null) == exist) {
        if (o != null)
          resultsV.addElement(o);
        else 
          resultsV.addElement(compareTo[i]);
      }
    }

    Record[] results = (Record[]) Array.newInstance(compareTo.getClass().getComponentType(), resultsV.size());
    resultsV.copyInto(results);
    return results;
  }


  /**
   * Merge all existing map entries with new ones, and insert those not in the map yet.
   * @return an array of records that were touched in the map either by merging or insertion.
   * The returned array has the same runtime type as the sourceRecords array.
   */
  public static Record[] merge(Map destinationMap, Record[] sourceRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "merge(Map destinationMap, Record[] sourceRecords)");
    if (trace != null) trace.args(destinationMap);
    if (trace != null) trace.args(sourceRecords);
    Vector resultsV = new Vector();

    for (int i=0; i<sourceRecords.length; i++) {
      Record rec = sourceRecords[i];
        if (rec != null) {
        Long id = rec.getId();
        Record o = (Record) destinationMap.get(id);
        // if not in map, insert
        // else merge with the map element
        if (o == null) {
          destinationMap.put(id, rec);
          resultsV.addElement(rec); // DON'T clone
        } else {
          o.merge(rec);
          resultsV.addElement(o); // DON'T clone
        }

        if (o != null && !(o.getClass().isInstance(rec)))
          throw new IllegalArgumentException("Runtime instance of the specified destinationMap elements and sourceRecords elements do not match.");
      }
    }

    Record[] results = (Record[]) Array.newInstance(sourceRecords.getClass().getComponentType(), resultsV.size());
    resultsV.copyInto(results);

    if (trace != null) trace.exit(RecordUtils.class, results);
    return results;
  }

  /**
   * Removes the elements specified in the array from the map.
   * @return an array of records that were removed from the map if found there, or from toRemoveItems for ones that were not in the map.
   * No records either in the map or from the array are merged during this operation.
   * The returned array has the same runtime type as the toRemoveItems array.
   */
  public static Record[] remove(Map removeFromMap, Record[] toRemoveItems) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "remove(Map removeFromMap, Record[] toRemoveItems)");
    if (trace != null) trace.args(removeFromMap);
    if (trace != null) trace.args(toRemoveItems);
    Vector resultsV = new Vector();

    for (int i=0; i<toRemoveItems.length; i++) {
      Record rec = toRemoveItems[i];
      if (rec != null) {
        Long id = rec.getId();
        Record o = (Record) removeFromMap.remove(id);
        // if found in map put to result vector,
        // else put the queried item from array to result vector.
        if (o == null) {
          resultsV.addElement(rec); // DON'T clone
        } else {
          resultsV.addElement(o); // DON'T clone
        }

        if (o != null && !(o.getClass().isInstance(rec)))
          throw new IllegalArgumentException("Runtime instance of the specified removeFromMap elements and toRemoveItems elements do not match.");
      }
    }

    Record[] results = (Record[]) Array.newInstance(toRemoveItems.getClass().getComponentType(), resultsV.size());
    resultsV.copyInto(results);

    if (trace != null) trace.exit(RecordUtils.class, results);
    return results;
  }

  /**
   * @return the Record from the array with specified ID
   */
  public static Record find(Record[] records, Long id) {
    if (records != null && id != null) {
      for (int i=0; i<records.length; i++) {
        Record r = records[i];
        Long oldId = r.getId();
        if (oldId != null) {
          if (oldId.equals(id)) {
            return r;
          }
        }
      }
    }
    return null;
  }

  /**
   * @return the Record from the Vector with specified ID
   */
  public static Record find(Vector records, Long id) {
    if (records != null && id != null) {
      for (int i=0; i<records.size(); i++) {
        Record r = (Record) records.elementAt(i);
        Long oldId = r.getId();
        if (oldId != null) {
          if (oldId.equals(id)) {
            return r;
          }
        }
      }
    }
    return null;
  }

  /**
   * @return true if record with specified ID is found. 
   */
  public static boolean contains(Record[] records, Long id) {
    return find(records, id) != null ? true : false;
  }
  /**
   * @return true if record with specified ID is found. 
   */
  public static boolean contains(Vector records, Long id) {
    return find(records, id) != null ? true : false;
  }

  /**
   * returns an array of cloned records.
   */
  public static Record[] cloneRecords(Record[] src) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "cloneRecords(Record[] src)");
    if (trace != null) trace.args(src);

    Record[] results = (Record[]) Array.newInstance(src.getClass().getComponentType(), src.length);
    for (int i=0; i<src.length; i++)
      results[i] = (Record) src[i].clone();

    if (trace != null) trace.exit(RecordUtils.class, results);
    return results;
  }


  /**
   * @return the difference between specified arrays.
   * The runtime instance of the array is Record[] !!!
   */
  public static Record[] getDifference(Record[] source, Record[] subtract) {
    return getDifference(source, subtract, null);
  }
  public static Record[] getDifference(Record[] source, Record[] subtract, Comparator comparator) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "getDifference(Record[] source, Record[] subtract, Comparator comparator)");
    if (trace != null) trace.args(source, subtract, comparator);

    Record[] recs = source;
    if (subtract != null && subtract.length > 0) {
      if (comparator != null)
        recs = getDifference(source, Arrays.asList(subtract), comparator);
      else 
        recs = getDifference(source, Arrays.asList(subtract));
    }

    if (trace != null) trace.exit(RecordUtils.class, recs);
    return recs;
  }
  /**
   * @return the difference between specified arrays.
   * The runtime instance of the array is Record[] !!!
   */
  public static Record[] getDifference(Record[] source, Collection subtract, Comparator comparator) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "getDifference(Record[] source, Collection subtract, Comparator comparator)");
    if (trace != null) trace.args(source, subtract, comparator);

    Record[] recs = null;
    if (subtract == null || subtract.size() == 0) {
      recs = source;
    } else {
      LinkedList ll = new LinkedList(Arrays.asList(source));
      Iterator iter1 = ll.iterator();
      while (iter1.hasNext()) {
        Object llObj = iter1.next();
        Iterator iter2 = subtract.iterator();
        while (iter2.hasNext()) {
          Object subObj = iter2.next();
          if (comparator.compare(llObj, subObj) == 0) {
            iter1.remove();
            break;
          }
        }
      }
      recs = (Record[]) ArrayUtils.toArray(ll, Record.class);
    }

    if (trace != null) trace.exit(RecordUtils.class, recs);
    return recs;
  }
  public static Record[] getDifference(Record[] source, Collection subtract) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "getDifference(Record[] source, Collection subtract)");
    if (trace != null) trace.args(source, subtract);

    Record[] recs = null;
    if (subtract == null || subtract.size() == 0) {
      recs = source;
    } else {
      List original = Arrays.asList(source);
      ArrayList aOriginal = new ArrayList(original);
      if (subtract != null && subtract.size() > 0) {
        aOriginal.removeAll(subtract);
      }
      recs = (Record[]) ArrayUtils.toArray(aOriginal, Record.class);
    }

    if (trace != null) trace.exit(RecordUtils.class, recs);
    return recs;
  }
  /**
   * @return the difference between specified arrays.
   * The runtime instance of the returned array has the runtime instance type of the first element
   */
  public static Record[] difference(Record[] source, Record[] subtract) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "difference(Record[] source, Record[] subtract)");
    if (trace != null) trace.args(source);
    if (trace != null) trace.args(subtract);

    Record[] recs = difference(source, Arrays.asList(subtract));

    if (trace != null) trace.exit(RecordUtils.class, recs);
    return recs;
  }
  /**
   * @return the difference between specified arrays.
   * The runtime instance of the returned array has the runtime instance type of the first element
   */
  public static Record[] difference(Record[] source, Collection subtract) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "difference(Record[] source, Collection subtract)");
    if (trace != null) trace.args(source);
    if (trace != null) trace.args(subtract);

    Record[] recs = getDifference(source, subtract);
    if (recs != null) {
      Record[] recs2 = (Record[]) Array.newInstance(source.getClass().getComponentType(), recs.length);
      Arrays.asList(recs).toArray(recs2);
      recs = recs2;
    }

    if (trace != null) trace.exit(RecordUtils.class, recs);
    return recs;
  }

  /**
   * Filter specified array.
   * @return new instance of array of which has the runtime instance type of the source array.
   */
  public static Record[] filter(Record[] records, RecordFilter filter) {
    Record[] keptRecords = null;
    if (records != null) {
      Vector keepV = new Vector();
      for (int i=0; i<records.length; i++) {
        if (filter.keep(records[i]))
          keepV.addElement(records[i]);
      }
      keptRecords = (Record[]) ArrayUtils.toArray(keepV, records.getClass().getComponentType());
    }
    return keptRecords;
  }

  /**
   * Concatinates arrays and returns an Record[]
   */
  public static Record[] concatinate(Record[] a1, Record[] a2) {
    if (a1 == null)
      return a2;
    if (a2 == null)
      return a1;

    List list1 = Arrays.asList(a1);
    List list2 = Arrays.asList(a2);

    LinkedList lList = new LinkedList(list1);
    lList.addAll(list2);

    Record[] array = (Record[]) ArrayUtils.toArray(lList, Record.class);

    return array;
  }

  /**
   * Divides an array of IDs into series of smaller arrays.
   */
  public static Long[][] divideIntoChunks(Long[] ids, int chunkSize) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "divideIntoChunks(Long[] ids, int chunkSize)");
    if (trace != null) trace.args(ids);
    if (trace != null) trace.args(chunkSize);
    Long[][] chunkIDs = null;
    if (ids == null || ids.length == 0) {
      chunkIDs = new Long[0][];
    } else {
      int chunks = ((ids.length-1) / chunkSize) + 1;
      if (trace != null) trace.data(10, chunks);
      chunkIDs = new Long[chunks][];
      int count = 0;
      int batchCount = 0;
      while (count < ids.length) {
        if (trace != null) trace.data(11, "in while");
        int bunch = Math.min(chunkSize, ids.length-count);
        if (trace != null) trace.data(12, bunch);
        Long[] bunchIDs = new Long[bunch];
        for (int i=0; i<bunch; i++) {
          bunchIDs[i] = ids[count+i];
        }
        chunkIDs[batchCount] = bunchIDs;
        batchCount ++;
        count += bunch;
        if (trace != null) trace.data(13, batchCount);
        if (trace != null) trace.data(14, count);
      }
    }
    if (trace != null) trace.exit(RecordUtils.class, chunkIDs);
    return chunkIDs;
  }

}
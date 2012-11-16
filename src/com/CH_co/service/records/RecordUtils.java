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

package com.CH_co.service.records;

import com.CH_co.service.records.filters.RecordFilter;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import java.lang.reflect.Array;
import java.util.*;

/**
* <b>Copyright</b> &copy; 2001-2012
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
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
  public static Long[] getIDs(Collection records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "getIDs(Collection records)");
    if (trace != null) trace.args(records);

    Long[] ids = null;
    if (records != null) {
      ids = new Long[records.size()];
      if (records instanceof List) {
        List list = (List) records;
        for (int i=0; i<records.size(); i++) {
          ids[i] = ((Record) list.get(i)).getId();
        }
      } else {
        Iterator iter = records.iterator();
        int i = 0;
        while (iter.hasNext()) {
          ids[i] = ((Record) iter.next()).getId();
          i ++;
        }
      }
    }

    if (trace != null) trace.exit(RecordUtils.class, ids);
    return ids;
  }
  /** @return IDs */
  public static Long[] getIDs2(Collection IDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "getIDs(Collection IDs)");
    if (trace != null) trace.args(IDs);
    Long[] ids = (Long[]) ArrayUtils.toArray(IDs, Long.class);
    if (trace != null) trace.exit(RecordUtils.class, ids);
    return ids;
  }


  /** @return String in brackets of record IDs separated by a comma.  Eg: "(1,2,3)" */
  public static String getIDsStr(Collection recordsL) {
    Record[] records = (Record[]) ArrayUtils.toArray(recordsL, Record.class);
    return getIDsStr(records);
  }

  /** @return String in brackets of record IDs separated by a comma.  Eg: "(1,2,3)" */
  public static String getIDsStr2(Collection IDsL) {
    Long[] IDs = (Long[]) ArrayUtils.toArray(IDsL, Long.class);
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
  private static Record[] pick(Map sourceMap, Record[] recs, boolean exist) {
    ArrayList resultsL = new ArrayList(recs.length);

    for (int i=0; i<recs.length; i++) {
      Record rec = recs[i];
      Object o = sourceMap.get(rec.getId());
      if ((o != null) == exist) {
        if (o != null)
          resultsL.add(o);
        else
          resultsL.add(rec);
      }
    }

    Record[] results = (Record[]) ArrayUtils.toArray(resultsL, recs.getClass().getComponentType());
    return results;
  }


  /**
  * Merge all existing map entries with new ones, and insert those not in the map yet.
  * @return an array of records that were touched in the map either by merging or insertion.
  * The returned array has the same runtime type as the sourceRecords array.
  */
  public static Record[] merge(Map destinationMap, Record[] recs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "merge(Map destinationMap, Record[] recs)");
    if (trace != null) trace.args(destinationMap);
    if (trace != null) trace.args(recs);
    ArrayList resultsL = new ArrayList(recs.length);

    for (int i=0; i<recs.length; i++) {
      Record rec = recs[i];
      if (rec != null) {
        Long id = rec.getId();
        Record o = (Record) destinationMap.get(id);
        // if not in map, insert
        // else merge with the map element
        if (o == null) {
          destinationMap.put(id, rec);
          resultsL.add(rec); // DON'T clone
        } else {
          o.merge(rec);
          resultsL.add(o); // DON'T clone
        }

        if (o != null && !(o.getClass().isInstance(rec)))
          throw new IllegalArgumentException("Runtime instance of the specified destinationMap elements and sourceRecords elements do not match.");
      }
    }
    Record[] results = (Record[]) ArrayUtils.toArray(resultsL, recs.getClass().getComponentType());

    if (trace != null) trace.exit(RecordUtils.class, results);
    return results;
  }

  /**
  * Removes the elements specified in the array from the map.
  * @return an array of records that were removed from the map if found there, or from toRemoveItems for ones that were not in the map.
  * No records either in the map or from the array are merged during this operation.
  * The returned array has the same runtime type as the toRemoveItems array.
  */
  public static Record[] remove(Map removeFromMap, Record[] recs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "remove(Map removeFromMap, Record[] recs)");
    if (trace != null) trace.args(removeFromMap);
    if (trace != null) trace.args(recs);
    ArrayList resultsL = new ArrayList(recs.length);

    for (int i=0; i<recs.length; i++) {
      Record rec = recs[i];
      if (rec != null) {
        Long id = rec.getId();
        Record o = (Record) removeFromMap.remove(id);
        // if found in map then put to results,
        // else put the queried item from source to results.
        if (o == null) {
          resultsL.add(rec); // DON'T clone
        } else {
          resultsL.add(o); // DON'T clone
        }

        if (o != null && !(o.getClass().isInstance(rec)))
          throw new IllegalArgumentException("Runtime instance of the specified removeFromMap elements and toRemoveItems elements do not match.");
      }
    }

    Record[] results = (Record[]) ArrayUtils.toArray(resultsL, recs.getClass().getComponentType());

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
  * @return the Record from the List with specified ID
  */
  public static Record find(List records, Long id) {
    if (records != null && id != null) {
      for (int i=0; i<records.size(); i++) {
        Record r = (Record) records.get(i);
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
  public static boolean contains(List records, Long id) {
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
      HashSet subtractHS = new HashSet();
      for (int i=0; i<subtract.length; i++)
        subtractHS.add(subtract[i]);
      if (comparator != null)
        recs = getDifference(source, subtractHS, comparator);
      else
        recs = getDifference(source, subtractHS, (Class) null);
    }

    if (trace != null) trace.exit(RecordUtils.class, recs);
    return recs;
  }
  /**
  * @return the difference between specified arrays.
  * The runtime instance of the array is Record[] !!!
  */
  public static Record[] getDifference(Record[] source, Set subtract, Comparator comparator) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "getDifference(Record[] source, Collection subtract, Comparator comparator)");
    if (trace != null) trace.args(source, subtract, comparator);

    Record[] recs = null;
    if (source.length == 0) {
      recs = source;
    } else if (subtract == null || subtract.size() == 0) {
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
  public static Record[] getDifference(Record[] source, Set subtract, Class recordType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "getDifference(Record[] source, Set subtract, Class recordType)");
    if (trace != null) trace.args(source, subtract, recordType);

    Record[] recs = null;
    if (subtract == null || subtract.size() == 0) {
      recs = source;
    } else {
      ArrayList resultL = new ArrayList(source.length);
      for (int i=0; i<source.length; i++) {
        Record rec = source[i];
        if (!subtract.contains(rec))
          resultL.add(rec);
      }
      Class type = recordType == null ? Record.class : recordType;
      recs = (Record[]) ArrayUtils.toArray(resultL, type);
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

    HashSet subtractHS = null;
    if (subtract != null && subtract.length > 0) {
      subtractHS = new HashSet();
      for (int i=0; i<subtract.length; i++)
        subtractHS.add(subtract[i]);
    }
    Record[] recs = difference(source, subtractHS);

    if (trace != null) trace.exit(RecordUtils.class, recs);
    return recs;
  }
  /**
  * @return the difference between specified arrays.
  * The runtime instance of the returned array has the runtime instance type of the first array
  */
  public static Record[] difference(Record[] source, Set subtract) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordUtils.class, "difference(Record[] source, Set subtract)");
    if (trace != null) trace.args(source);
    if (trace != null) trace.args(subtract);

    Class recordType = null;
    if (source != null)
      recordType = source.getClass().getComponentType();
    Record[] recs = getDifference(source, subtract, recordType);

    if (trace != null) trace.exit(RecordUtils.class, recs);
    return recs;
  }

  /**
  * Filter specified array.
  * @return new instance of array of which has the runtime instance type of the source array.
  */
  public static Record[] filter(Record[] recs, RecordFilter filter) {
    Record[] keptRecords = null;
    if (recs != null) {
      ArrayList keepL = new ArrayList(recs.length);
      for (int i=0; i<recs.length; i++) {
        Record rec = recs[i];
        if (filter.keep(rec))
          keepL.add(rec);
      }
      keptRecords = (Record[]) ArrayUtils.toArray(keepL, recs.getClass().getComponentType());
    }
    return keptRecords;
  }

  /**
  * Concatinates arrays and returns an Record[]
  */
  public static Record[] concatinate(Record[] a1, Record[] a2) {
    Record[] array = null;
    if (a1 != null && a2 != null) {
      int size1 = a1.length;
      int size2 = a2.length;
      array = (Record[]) Array.newInstance(Record.class, size1 + size2);
      if (size1 > 0)
        System.arraycopy(a1, 0, array, 0, size1);
      if (size2 > 0)
        System.arraycopy(a2, 0, array, size1, size2);
    } else if (a1 == null) {
      array = a2;
    } else if (a2 == null) {
      array = a1;
    }
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
/*
 * Copyright 2001-2010 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_co.service.msg.dataSets.obj;

import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.*;
import java.util.*;

import com.CH_co.trace.Trace;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.io.DataInputStream2; 
import com.CH_co.io.DataOutputStream2;
import com.CH_co.service.msg.*;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: General purpose data set to transfer an array of primitive objects and itself.
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.14 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class Obj_List_Co extends ProtocolMsgDataSet {

  // Each element can be any type/instance of the class ProtocolMsgDataSet or some other simple type.
  private static final byte TYPE_LONG = 1;
  private static final byte TYPE_INT = 2;
  private static final byte TYPE_STRING = 3;
  private static final byte TYPE_PROTOCOL_MSG_DATA_SET = 4;
  private static final byte TYPE_SHORT = 5;
  private static final byte TYPE_BOOLEAN = 6;
  private static final byte TYPE_BYTES = 7;
  private static final byte TYPE_FLOAT = 8;
  private static final byte TYPE_DOUBLE = 9;
  private static final byte TYPE_OBJECTS = 10;
  private static final byte TYPE_TIMESTAMP = 11;
  private static final byte TYPE_CHARACTER = 12;

  // <numOfElements> { <Object> }+

  public Object objs[];

  /** Creates new Obj_List_Co */
  public Obj_List_Co() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_List_Co.class, "Obj_List_Co()");
    if (trace != null) trace.exit(Obj_List_Co.class);
  }
  /** Creates new Obj_List_Co */
  public Obj_List_Co(Object obj) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_List_Co.class, "Obj_List_Co(Object obj)");
    if (trace != null) trace.args(obj);
    objs = new Object[] { obj };
    if (trace != null) trace.exit(Obj_List_Co.class);
  }
  /** Creates new Obj_List_Co */
  public Obj_List_Co(Object[] objs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_List_Co.class, "Obj_List_Co(Object[] objs)");
    if (trace != null) trace.args(objs);
    this.objs = objs;
    if (trace != null) trace.exit(Obj_List_Co.class);
  }
  /** Creates new Obj_List_Co */
  public Obj_List_Co(List objsL) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_List_Co.class, "Obj_List_Co(List objsL)");
    if (trace != null) trace.args(objsL);
    this.objs = ArrayUtils.toArray(objsL, Object.class);
    if (trace != null) trace.exit(Obj_List_Co.class);
  }


  /** Writes out 'this' object to a stream */
  public void writeToStream(DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_List_Co.class, "writeToStream(DataOutputStream2, ProgMonitor)");

    writeToStream2(objs, dataOut, progressMonitor, clientBuild, serverBuild);

    if (trace != null) trace.exit(Obj_List_Co.class);
  } // end writeToStream()

  private void writeToStream2(Object[] objects, DataOutputStream2 dataOut, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_List_Co.class, "writeToStream2(Object[] objects, DataOutputStream2, ProgMonitor)");
    // write indicator
    if (objects == null)
      dataOut.write(0);
    else {
      dataOut.write(1);

      dataOut.writeShort(objects.length);
      for (int i=0; i<objects.length; i++) {
        Object o = objects[i];
        if (o == null)
          dataOut.write(0);
        else {
          if (o instanceof Long) {
            dataOut.write(TYPE_LONG);
            dataOut.writeLong(((Long)o).longValue());
          }
          else if (o instanceof Integer) {
            dataOut.write(TYPE_INT);
            dataOut.writeInt(((Integer)o).intValue());
          }
          else if (o instanceof String) {
            dataOut.write(TYPE_STRING);
            dataOut.writeString((String)o);
          }
          else if (o instanceof ProtocolMsgDataSet) {
            dataOut.write(TYPE_PROTOCOL_MSG_DATA_SET);
            ProtocolMsgDataSet protocolMsgDataSet = (ProtocolMsgDataSet) o;
            Message.writeToStream(dataOut, progressMonitor, protocolMsgDataSet, clientBuild, serverBuild);
          }
          else if (o instanceof Short) {
            dataOut.write(TYPE_SHORT);
            dataOut.writeShort(((Short)o).shortValue());
          }
          else if (o instanceof Boolean) {
            dataOut.write(TYPE_BOOLEAN);
            dataOut.writeBoolean(((Boolean)o).booleanValue());
          }
          else if (o instanceof byte[]) {
            dataOut.write(TYPE_BYTES);
            dataOut.writeBytes((byte[]) o);
          }
          else if (o instanceof Float) {
            dataOut.write(TYPE_FLOAT);
            dataOut.writeFloatObj((Float) o);
          }
          else if (o instanceof Double) {
            dataOut.write(TYPE_DOUBLE);
            dataOut.writeDoubleObj((Double) o);
          }
          else if (o instanceof Object[]) {
            dataOut.write(TYPE_OBJECTS);
            writeToStream2((Object[]) o, dataOut, progressMonitor, clientBuild, serverBuild);
          }
          else if (o instanceof Timestamp) {
            dataOut.write(TYPE_TIMESTAMP);
            dataOut.writeTimestamp((Timestamp) o);
          }
          else if (o instanceof Character) {
            dataOut.write(TYPE_CHARACTER);
            dataOut.writeCharByte((Character) o);
          }
          else {
            // Not NULL and not supported elements get a zero to look like nulls.
            dataOut.write(0);
          }
        }
      }
    }
    if (trace != null) trace.exit(Obj_List_Co.class);
  }

  /** Initializes 'this' object from a stream. */
  public void initFromStream(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_List_Co.class, "initFromStream(DataInputStream2, ProgMonitor)");

    try {
      objs = initFromStream2(dataIn, progressMonitor, clientBuild, serverBuild);
    } catch (DataSetException x) {
      throw new IllegalStateException(x.getMessage());
    }

    if (trace != null) trace.exit(Obj_List_Co.class);
  } // end initFromStream()

  private Object[] initFromStream2(DataInputStream2 dataIn, ProgMonitorI progressMonitor, short clientBuild, short serverBuild) throws IOException, DataSetException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Obj_List_Co.class, "initFromStream2(DataInputStream2, ProgMonitor)");

    Object[] objects = null;
    // read indicator
    int indicator = dataIn.read();
    if (indicator == 0)
      objects = new Object[0];
    else {
      objects = new Object[dataIn.readShort()];
      for (int i=0; i<objects.length; i++) {
        indicator = dataIn.read();
        switch (indicator) {
          case 0:
            objects[i] = null;
            break;
          case TYPE_LONG:
            objects[i] = new Long(dataIn.readLong());
            break;
          case TYPE_INT:
            objects[i] = new Integer(dataIn.readInt());
            break;
          case TYPE_STRING:
            objects[i] = dataIn.readString();
            break;
          case TYPE_PROTOCOL_MSG_DATA_SET:
            ProtocolMsgDataSet protocolMsgDataSet = Message.readFromStream(dataIn, progressMonitor, clientBuild, serverBuild);
            objects[i] = protocolMsgDataSet;
            break;
          case TYPE_SHORT:
            objects[i] = new Short(dataIn.readShort());
            break;
          case TYPE_BOOLEAN:
            objects[i] = Boolean.valueOf(dataIn.readBoolean());
            break;
          case TYPE_BYTES:
            objects[i] = dataIn.readBytes();
            break;
          case TYPE_FLOAT:
            objects[i] = dataIn.readFloatObj();
            break;
          case TYPE_DOUBLE:
            objects[i] = dataIn.readDoubleObj();
            break;
          case TYPE_OBJECTS:
            objects[i] = initFromStream2(dataIn, progressMonitor, clientBuild, serverBuild);
            break;
          case TYPE_TIMESTAMP:
            objects[i] = dataIn.readTimestamp();
            break;
          case TYPE_CHARACTER:
            objects[i] = dataIn.readCharByte();
            break;
        }
      } // end for
      // check if instance of all objects in the array is the same, then create an array of that dynamic type
      Class type = null;
      boolean sameType = true;
      for (int i=0; i<objects.length; i++) {
        Object o = objects[i];
        if (o instanceof Object[]) {
          sameType = false;
          break;
        }
        if (type == null) {
          if (o != null) {
            type = o.getClass();
          }
        } else if (o != null && !type.equals(o.getClass())) {
          sameType = false;
          break;
        }
      }
      // if same type then create a dynamic array of that type
      if (sameType && type != null) {
        Object[] objArray = (Object[]) Array.newInstance(type, objects.length);
        Arrays.asList(objects).toArray(objArray);
        objects = objArray;
      }
    } // if indicator != 0

    if (trace != null) trace.exit(Obj_List_Co.class, objects);
    return objects;
  }

  /**
   * Passes call to the wrapped data sets asking them if any is sensitive.
   * @return true if at least one of them is sensitive.
   */
  public boolean isTimeSensitive() {
    boolean isSensitive = false;
    for (int i=0; i<objs.length; i++) {
      if (objs[i] instanceof ProtocolMsgDataSet) {
        isSensitive = ((ProtocolMsgDataSet) objs[i]).isTimeSensitive();
      } else if (objs[i] instanceof Object[]) {
        Object[] innerObjs = (Object[]) objs[i];
        for (int k=0; k<innerObjs.length; k++) {
          if (innerObjs[k] instanceof ProtocolMsgDataSet) {
            isSensitive = ((ProtocolMsgDataSet) innerObjs[k]).isTimeSensitive();
            if (isSensitive) break;
          }
        }
      }
      if (isSensitive) break;
    }
    return isSensitive;
  }

  /**
   * Passes call to the wrapped data sets asking them if any is sensitive.
   * @return true if at least one of them is sensitive.
   */
  public boolean isUserSensitive() {
    boolean isSensitive = false;
    for (int i=0; i<objs.length; i++) {
      if (objs[i] instanceof ProtocolMsgDataSet) {
        isSensitive = ((ProtocolMsgDataSet) objs[i]).isUserSensitive();
      } else if (objs[i] instanceof Object[]) {
        Object[] innerObjs = (Object[]) objs[i];
        for (int k=0; k<innerObjs.length; k++) {
          if (innerObjs[k] instanceof ProtocolMsgDataSet) {
            isSensitive = ((ProtocolMsgDataSet) innerObjs[k]).isUserSensitive();
            if (isSensitive) break;
          }
        }
      }
      if (isSensitive) break;
    }
    return isSensitive;
  }

  /**
   * Pass setting to the wrapped data sets.
   */
  public void setServerSessionCurrentStamp(Timestamp ts) {
    for (int i=0; i<objs.length; i++) {
      if (objs[i] instanceof ProtocolMsgDataSet) {
        ((ProtocolMsgDataSet) objs[i]).setServerSessionCurrentStamp(ts);
      } else if (objs[i] instanceof Object[]) {
        Object[] innerObjs = (Object[]) objs[i];
        for (int k=0; k<innerObjs.length; k++) {
          if (innerObjs[k] instanceof ProtocolMsgDataSet) {
            ((ProtocolMsgDataSet) innerObjs[k]).setServerSessionCurrentStamp(ts);
          }
        }
      }
    }
    super.setServerSessionCurrentStamp(ts);
  }
  /**
   * Pass setting to the wrapped data sets.
   */
  public void setServerSessionUserId(Long userId) {
    for (int i=0; i<objs.length; i++) {
      if (objs[i] instanceof ProtocolMsgDataSet) {
        ((ProtocolMsgDataSet) objs[i]).setServerSessionUserId(userId);
      } else if (objs[i] instanceof Object[]) {
        Object[] innerObjs = (Object[]) objs[i];
        for (int k=0; k<innerObjs.length; k++) {
          if (innerObjs[k] instanceof ProtocolMsgDataSet) {
            ((ProtocolMsgDataSet) innerObjs[k]).setServerSessionUserId(userId);
          }
        }
      }
    }
    super.setServerSessionUserId(userId);
  }

  public String toString() {
    return "[Obj_List_Co"
      + ": objs=" + Misc.objToStr(objs)
      + "]";
  }

}
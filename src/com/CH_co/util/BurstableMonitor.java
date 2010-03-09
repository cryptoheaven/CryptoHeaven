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

package com.CH_co.util;

import java.util.*;
import com.CH_co.trace.Trace;

/**
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.2 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class BurstableMonitor extends Object {

  private long burstSize;
  private long burstMinTimeMillis;
  private long minSeperationTime;
  private boolean monitorBusy;
  private final LinkedList passStampsList = new LinkedList();
  private final Vector waitingList = new Vector();

  /** Creates new BurstableMonitor */
  public BurstableMonitor(long burstSize, long burstMinTimeMillis, long minSeperationTime) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BurstableMonitor.class, "BurstableMonitor()");
    this.burstSize = burstSize;
    this.burstMinTimeMillis = burstMinTimeMillis;
    this.minSeperationTime = minSeperationTime;
    if (burstSize < 1)
      throw new IllegalArgumentException("Burst Size must be >= 1");
    if (trace != null) trace.exit(BurstableMonitor.class);
  }

  /**
   * @return 0 if there was no delay
   * @return -1 if delay was interrupted, interrupted delays don't leave mark, it is as if call never happened.
   * @return +ve equal to delay milliseconds
   */
  public synchronized long passThrough() {
    return passThrough(this);
  }
  public long passThrough(Object waitMonitor) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(BurstableMonitor.class, "passThrough(Object waitMonitor)");
    long rc = 0;

    // enter into waiting area before going to the tunnel
    synchronized (passStampsList) {
      waitingList.addElement(waitMonitor);
    }

    // only one thread in the passThrough()
    while (true) {
      boolean shouldWait = false;
      synchronized (passStampsList) {
        shouldWait = monitorBusy;
      }
      if (shouldWait) {
        // waitMonitor was already synchronized outside this function but
        // for strict correctness we must hold a lock to wait on it
        synchronized (waitMonitor) { 
          try { waitMonitor.wait(); } catch (Throwable t) { }
        }
      } else {
        synchronized (passStampsList) {
          if (!monitorBusy) {
            monitorBusy = true;
            break;
          }
        }
      }
    }

    // we are in the tunnel so exit waiting area
    synchronized (passStampsList) {
      waitingList.removeElement(waitMonitor);
    }

    try {
      long toDelay = 0;
      long now = System.currentTimeMillis();
      while (passStampsList.size() > burstSize)
        passStampsList.removeFirst(); // remove oldest entry
      if (passStampsList.size() >= burstSize) {
        long burstStart = ((Long) passStampsList.getFirst()).longValue();
        long elapsed = now - burstStart;
        if (elapsed < burstMinTimeMillis) {
          toDelay = Math.max(0, burstMinTimeMillis - elapsed); // delay always positive
          toDelay = Math.min(toDelay, burstMinTimeMillis); // never delay more than entire burst time
        }
      }
      long minTimeNoInterrupt = 0;
      // enforce minimum seperation time
      if (minSeperationTime > 0 && passStampsList.size() >= 1) {
        long lastStamp = ((Long) passStampsList.getLast()).longValue();
        long timeFromLast = now - lastStamp;
        if (timeFromLast < minSeperationTime) {
          minTimeNoInterrupt = minSeperationTime - timeFromLast;
          if (minTimeNoInterrupt > toDelay)
            toDelay = minTimeNoInterrupt;
        }
      }

      // We now know how much to delay
      if (toDelay > 0) {
        if (trace != null) trace.data(100, "rest for a bit", toDelay);
        long start = System.currentTimeMillis();
        long end = 0;
        do {
          long leftoverDelay = toDelay;
          if (end != 0) { // if already interrupted, cut the delay time to minTimeNoInterrupt
            leftoverDelay = Math.min(toDelay, minTimeNoInterrupt) - (end - start);
          }
          if (leftoverDelay > 0) {
            // waitMonitor was already synchronized outside this function but
            // for strict correctness we must hold a lock to wait on it
            synchronized (waitMonitor) { 
              waitMonitor.wait(leftoverDelay);
            }
          }
          end = System.currentTimeMillis();
        } while (end - start < minTimeNoInterrupt); // cannot interrupt if minimum pass time did not pass
        // return code is time actually waited
        rc = end - start;
      }

      // after wait update NOW
      now = System.currentTimeMillis();
      boolean interrupted = false;
      if (passStampsList.size() >= burstSize) {
        long burstStart = ((Long) passStampsList.getFirst()).longValue();
        long elapsed = now - burstStart;
        if (elapsed < burstMinTimeMillis) {
          interrupted = true;
        }
      }
      if (!interrupted) {
        passStampsList.addLast(Long.valueOf(now));
      } else {
        rc = -1;
      }

    } catch (InterruptedException e1) {
      rc = -1;
    } catch (Throwable t) {
      rc = -1;
      t.printStackTrace();
    }

    // exit passThrough and wake others waiting
    Object[] monitors = null;
    synchronized (passStampsList) {
      monitorBusy = false;
      monitors = new Object[waitingList.size()];
      waitingList.toArray(monitors);
    }
    for (int i=0; i<monitors.length; i++) {
      synchronized (monitors[i]) {
        monitors[i].notifyAll();
      }
    }

    // Since this thread is being slowed down, yield to ensure no starvation to others in this tunnel.
    Thread.yield();

    if (trace != null) trace.exit(BurstableMonitor.class, rc);
    return rc;
  }




  /**
   * Main test of BurstableMonitor
   */
  public static void main(String[] args) {
    final Object mon = new Object();
    final BurstableMonitor bm = new BurstableMonitor(10, 1000, 0);
    final boolean printInterrupts = false;
    new Thread() {
      public void run() {
        while (true) {
          synchronized (mon) {
            long rc = bm.passThrough(mon);
            if (rc >= 0) {
              System.out.print('.');
            } else {
              if (printInterrupts) System.out.print('X');
            }
          }
        }
      }
    }.start();
    new Thread() {
      public void run() {
        while (true) {
          synchronized (mon) {
            long rc = bm.passThrough(mon);
            if (rc >= 0) {
              System.out.print(',');
            } else {
              if (printInterrupts) System.out.print('X');
            }
          }
        }
      }
    }.start();
    new Thread() {
      public void run() {
        while (true) {
          synchronized (mon) {
            long rc = bm.passThrough(mon);
            if (rc >= 0) {
              System.out.print('`');
            } else {
              if (printInterrupts) System.out.print('X');
            }
          }
        }
      }
    }.start();
    new Thread() {
      public void run() {
        while (true) {
          long rc = bm.passThrough();
          if (rc >= 0) {
            System.out.print('+');
          } else {
            if (printInterrupts) System.out.print('X');
          }
        }
      }
    }.start();
    Thread monitorWake = new Thread() {
      public void run() {
        while (true) {
          try {
            Thread.sleep(new Random().nextInt(10));
          } catch (InterruptedException e) {
          }
          synchronized (mon) {
            mon.notifyAll();
          }
        }
      }
    };
    monitorWake.start();
    Thread monitorWake2 = new Thread() {
      public void run() {
        while (true) {
          try {
            Thread.sleep(new Random().nextInt(10));
          } catch (InterruptedException e) {
          }
          synchronized (bm) {
            bm.notifyAll();
          }
        }
      }
    };
    monitorWake2.start();
  }

}
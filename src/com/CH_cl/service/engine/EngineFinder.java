/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.engine;

import com.CH_co.trace.*;
import com.CH_co.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.17 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class EngineFinder extends Object {

  /**
   * Queries the HTTP server for the list of application servers currently accessible.
   * Based on the current query return the best-version-matching list of servers that is available.
   * Update the GlobalProperties with the newest response to server locations.
   * When there are problems with connectivity to specified server, it will return last fetched list
   * of data servers for the specified http server with that http server attached at the end.
   * @param server consists of (String-hostname) (Integer-port)
   */
  public static Object[][] queryServerForHostsAndPorts(Object[] server) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EngineFinder.class, "queryServerForHostsAndPorts(Object[] server)");
    if (trace != null) trace.args(server);
//System.out.println("QUERY SERVER FOR HOSTS AND PORTS");
    Object[][] hostsAndPorts = null;
    Object[][] oldEngineServers = null; // incase the server_list is not updated, this will provide some emergency recovery

    ArrayList serversAndPortsL = new ArrayList();

    serversAndPortsL.add(server);
//System.out.println("1: server=" + Misc.objToStr(server));
    // Add past servers to the trial vectors.
    String serverListProperty = getServerListPropertyName(server);
    String serverListStr = GlobalProperties.getProperty(serverListProperty);
    if (serverListStr != null && serverListStr.trim().length() > 0) {
      Object[][] additionalServers = getServers(serverListStr, true, false, 0, 0);
      if (additionalServers != null && additionalServers.length > 0) {
        for (int i=0; i<additionalServers.length; i++)
          serversAndPortsL.add(additionalServers[i]);
      }
    }
//System.out.println("2: serversAndPortsV=" + Misc.objToStr(serversAndPortsV));
    // Loop through all of the HTTP servers and try fetching the current EngineServers.
    // First successful fetch is sufficient...
    for (int i=0; i<serversAndPortsL.size(); i++) {
      Object[] httpServer = null;
      try {
        httpServer = (Object[]) serversAndPortsL.get(i);
        int portNum = ((Integer)httpServer[1]).intValue();
        // Try the web to serve the list only if it is to ports 80, 8000, 8080
        if (portNum == 80 || portNum == 8000 || portNum == 8080) {
          URL url = getServerListURL(httpServer);
//System.out.println("2: trying i="+i);
          BufferedReader reader = null;
          try {
            reader = new BufferedReader(new InputStreamReader(openStream(url, 3000)));
          } catch (Throwable thX) {
//System.out.println("2: trying i="+i+ " FAILED with " + thX);
            String errMsg = "Could not open an connection to " + url.toExternalForm() + " to fetch the current list of "+URLs.get(URLs.SERVICE_SOFTWARE_NAME)+" data servers.  Error message returned is: \n\n" + thX.getMessage();
            if (trace != null) trace.exception(EngineFinder.class, 40, thX);
            if (trace != null) trace.data(41, errMsg);
          }
          if (reader != null) {
            // Fetch all lines that starts with 'role' and concatinate them with '|' separator.
            StringBuffer serversStrBuf = new StringBuffer();
            String fetchedLineStr = null;
            while ((fetchedLineStr = reader.readLine()) != null) {
              if (fetchedLineStr.startsWith("role"))
                serversStrBuf.append(fetchedLineStr).append('|');
            }
            String serversStr = serversStrBuf.toString();
//System.out.println("2: URL reader got " + serversStr);
            if (trace != null) trace.data(50, serversStr);
            reader.close();
            if (serversStr != null && serversStr.trim().length() > 0) {
              Object[][] engineServers = getServers(serversStr, false, true, GlobalProperties.PROGRAM_VERSION, GlobalProperties.PROGRAM_RELEASE);
              if (engineServers != null && engineServers.length > 0) {
                hostsAndPorts = engineServers;
//System.out.println("2: set hostsAndPorts="+Misc.objToStr(hostsAndPorts));
                GlobalProperties.setProperty(getServerListPropertyName(httpServer), serversStr);
                break;
              } else {
                oldEngineServers = getServers(serversStr, false, true, 0, 0);
//System.out.println("2: set oldEngineServers="+Misc.objToStr(oldEngineServers));
                // if server_list does not contain any new version servers, write all of them out to properties
                if (oldEngineServers != null && oldEngineServers.length > 0) {
                  GlobalProperties.setProperty(getServerListPropertyName(httpServer), serversStr);
                }
              }
            }
          } // end if reader != null
        } // end if http port
      } catch (Throwable t) {
        String errMsg = "Fetching the current list of "+URLs.get(URLs.SERVICE_SOFTWARE_NAME)+" data servers from http://" + httpServer[0] + "/server_list.html at port " + httpServer[1] + " failed.  Error message returned is: \n\n" + t.getMessage();
        if (trace != null) trace.exception(EngineFinder.class, 100+i, t);
        if (trace != null) trace.data(101+i, errMsg);
      }
    } // end for
//System.out.println("3: hostsAndPorts=\n" + Misc.objToStr(hostsAndPorts));
//System.out.println("4: oldEngineServers=\n" + Misc.objToStr(oldEngineServers));
    // if nothing found, return the last fetched array of servers or at least the entered host and port
    if (hostsAndPorts == null) {
      if (trace != null) trace.data(200, "no host found!");
      // clear the http servers used for fetching lists
      serversAndPortsL.clear();
      // Add any old servers fetched from WEB server
      if (oldEngineServers != null) {
//System.out.println("4a: oldEngineServers!=null");
        for (int i=0; i<oldEngineServers.length; i++)
          addOrRemoveServer(serversAndPortsL, true, oldEngineServers[i]);
        if (trace != null) trace.data(210, "After adding old servers fetched from WEB server", serversAndPortsL);
      }
      // if still no servers
      // Add past data servers to the return vector.
      if (serversAndPortsL.isEmpty() && serverListStr != null && serverListStr.trim().length() > 0) {
//System.out.println("4b: Add past data servers to the return vector from list " + serverListStr);
        Object[][] additionalServers = getServers(serverListStr, false, true, 0, 0);
        if (additionalServers != null) {
//System.out.println("4bb: additionalServers != null");
          for (int i=0; i<additionalServers.length; i++)
            addOrRemoveServer(serversAndPortsL, true, additionalServers[i]);
        }
        if (trace != null) trace.data(220, "After adding past data servers", serversAndPortsL);
      }
      // if still no servers, then use the entered host and port
      if (serversAndPortsL.isEmpty()) {
        // add the passed in server as last resort
        serversAndPortsL.add(server);
        if (trace != null) trace.data(230, "After adding passed in server as last resort", serversAndPortsL);
      }
      if (trace != null) trace.data(240, "making return structure...");
      // make return structure
      Object[][] servers = new Object[serversAndPortsL.size()][];
      serversAndPortsL.toArray(servers);
      hostsAndPorts = servers;
    }
//System.out.println("RETURN: "+Misc.objToStr(hostsAndPorts));
    if (trace != null) trace.data(300, "returning hostsAndPorts");
    if (trace != null) trace.exit(EngineFinder.class, hostsAndPorts);
    return hostsAndPorts;
  }

  /**
   * Adds a specified server (ie "d3.cryptoheaven.com:4383") into the array of
   * host:port object pairs, and returns new array with appended server at the end.
   */
  public static Object[][] addOrRemoveServer(Object[][] hostsAndPorts, boolean isAdd, String serverS) {
    if (serverS != null && serverS.length() > 0) {
      Object[] server = Misc.parseHostAndPort(serverS);
      if (server != null) {
        ArrayList hostsAndPortsL = new ArrayList();
        if (hostsAndPorts != null && hostsAndPorts.length > 0)
          hostsAndPortsL.addAll(Arrays.asList(hostsAndPorts));
        EngineFinder.addOrRemoveServer(hostsAndPortsL, isAdd, server);
        hostsAndPorts = new Object[hostsAndPortsL.size()][];
        hostsAndPortsL.toArray(hostsAndPorts);
      }
    }
    return hostsAndPorts;
  }
  private static void addOrRemoveServer(ArrayList serversAndPortsL, boolean isAdd, Object[] toAddServerAndPort) {
    if (serversAndPortsL != null && toAddServerAndPort != null) {
      boolean found = false;
      for (int k=0; k<serversAndPortsL.size(); k++) {
        Object[] serverAndPort = (Object[]) serversAndPortsL.get(k);
        if (serverAndPort[0].toString().equalsIgnoreCase(toAddServerAndPort[0].toString()) && serverAndPort[1].toString().equalsIgnoreCase(toAddServerAndPort[1].toString())) {
          found = true;
          if (isAdd)
            break;
          else {
            serversAndPortsL.remove(k);
            k--;
          }
        }
      }
      if (isAdd && !found) {
        serversAndPortsL.add(toAddServerAndPort);
      }
    }
  }

  private static URL getServerListURL(Object[] serverAndPort) throws MalformedURLException {
    URL url = null;
    String httpSrv = serverAndPort[0].toString().toLowerCase();
    if (httpSrv.startsWith("http://") || httpSrv.startsWith("https://"))
      url = new URL(serverAndPort[0].toString() + ":" + serverAndPort[1].toString() + "/server_list.html");
    else
      url = new URL("http://" + serverAndPort[0].toString() + ":" + serverAndPort[1].toString() + "/server_list.html");
    return url;
  }

  private static Object[][] getServers(String serverList, boolean httpServers, boolean engineServers, float minVer, int minRel) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EngineFinder.class, "getServers(String serverList, boolean httpServers, boolean engineServers, float minVer, int minRel)");
    if (trace != null) trace.args(serverList);
    if (trace != null) trace.args(httpServers);
    if (trace != null) trace.args(engineServers);
    if (trace != null) trace.args(minVer);
    if (trace != null) trace.args(minRel);

    ArrayList serversL = null;

    if (serverList != null) {
      try {
        StringTokenizer st = new StringTokenizer(serverList, "|");
        while (st.hasMoreTokens()) {
          try {
            String serverDescription = st.nextToken();
            StringTokenizer t = new StringTokenizer(serverDescription);
            // Description has format: 'role' <role> 'ver' <version> 'rel' <release> 'host' <hostname> 'port' <port>
            t.nextToken();
            String role = t.nextToken();
            t.nextToken();
            float version = Float.parseFloat(t.nextToken());
            t.nextToken();
            int release = Integer.parseInt(t.nextToken());
            t.nextToken();
            String hostname = t.nextToken();
            t.nextToken();
            Integer port = Integer.valueOf(t.nextToken());

            if (compareVersion(minVer, minRel, version, release) <= 0) {
              if (httpServers && role.equals("HttpServer")) {
                if (serversL == null) serversL = new ArrayList();
                serversL.add(new Object[] { hostname, port });
              }
              if (engineServers && role.equals("EngineServer")) {
                if (serversL == null) serversL = new ArrayList();
                serversL.add(new Object[] { hostname, port });
              }
            }
          } catch (Exception eInner) {
            if (trace != null) trace.exception(EngineFinder.class, 100, eInner);
          }
        } // end while
      } catch (Exception eOuter) {
        if (trace != null) trace.exception(EngineFinder.class, 200, eOuter);
      }
    }

    Object[][] servers = null;
    if (serversL != null && serversL.size() > 0) {
      servers = new Object[serversL.size()][];
      serversL.toArray(servers);
    }

    if (trace != null) trace.exit(EngineFinder.class, servers);
    return servers;
  } // end getBackupServers()

  public static int compareVersion(float aVer, int aRel, float bVer, int bRel) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EngineFinder.class, "compareVersion(float aVer, int aRel, float bVer, int bRel)");
    if (trace != null) trace.args(aVer);
    if (trace != null) trace.args(aRel);
    if (trace != null) trace.args(bVer);
    if (trace != null) trace.args(bRel);

    int rc = 0;
    if (aVer == bVer && aRel == bRel)
      rc = 0;
    else if (aVer > bVer || (aVer == bVer && aRel > bRel))
      rc = 1;
    else if (aVer < bVer || (aVer == bVer && aRel < bRel))
      rc = -1;

    if (trace != null) trace.exit(EngineFinder.class, rc);
    return rc;
  }

  private static String getServerListPropertyName(Object[] server) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EngineFinder.class, "getServerListPropertyName(Object[] server)");
    String propertyName = "ServerList_" + ((String)server[0]).toLowerCase() + "_" + server[1];
    if (trace != null) trace.exit(EngineFinder.class, propertyName);
    return propertyName;
  }

  /**
   * To open a URL input stream this method will wait the maximum of time specified, if it is taking longer, just quit and return null.
   * @param url The URL to open
   * @param maxWaitMillis The maximum amount of time this operation is permited to take.
   * @return The opened InputStream or null if failed or time exceeded.
   */
  private static InputStream openStream(final URL url, int maxWaitMillis) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EngineFinder.class, "openStream(final URL url, int maxWaitMillis)");
    if (trace != null) trace.args(url);
    if (trace != null) trace.args(maxWaitMillis);
    InputStream stream = null;
    final Object[] returnBuffer = new Object[1];
    Thread fetcher = new ThreadTraced("EngineFinder server list fetcher") {
      public void runTraced() {
        try {
          InputStream returnStream = url.openStream();
          returnBuffer[0] = returnStream;
        } catch (IOException x) {
        }
      }
    };
    fetcher.setDaemon(true);
    fetcher.start();
    try {
      fetcher.join(maxWaitMillis);
    } catch (InterruptedException e) {
    }
    stream = (InputStream) returnBuffer[0];
    if (trace != null) trace.exit(EngineFinder.class, stream);
    return stream;
  }

}
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
package com.CH_cl.util;

import com.CH_co.cryptx.SHA256;
import com.CH_co.util.GlobalProperties;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

/** 
* <b>Copyright</b> &copy; 2001-2013
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
* Class Description:
*
*
* Class Details:
*
*
* <b>$Revision: 1.1 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class UserColor {

  private static final String PROPERTY_PREFIX = "userColor";
  // User is the key, value is the Color object
  private static final HashMap altBkUserAssignedColors; // <Long sourceId, Integer color>
  //
  private static final ArrayList altBkUserColors;
  private static MessageDigest sha256;
  private static boolean isRestored;

  static {
    altBkUserColors = new ArrayList();
    altBkUserColors.add(new Integer(rgb(113, 156, 10))); // old chat color
    altBkUserColors.add(new Integer(rgb(55, 62, 156)));
    altBkUserColors.add(new Integer(rgb(133, 103, 2)));
    altBkUserColors.add(new Integer(rgb(219, 68, 150)));

    altBkUserAssignedColors = new HashMap();
    sha256 = new SHA256();
  }

  /**
  * Make color value from source digest or random if NULL source.
  *
  * @param sourceContents to digest or NULL for random values.
  * @return
  */
  private static int makeUserColor(Long sourceId) {
    int source = 0;
    if (sourceId != null) {
      byte[] digest = sha256.digest(sourceId.toString().getBytes());
      source = digest[0] | ((int) digest[1] << 8) | ((int) digest[2] << 16) | ((int) digest[3] << 24);
    } else {
      source = new Random().nextInt();
    }
    int[] colors = new int[3];
    colors[0] = (128 + 64 + 32 + 16 + 8 + 4 + 2 + 1) & source;
    colors[1] = (128 + 64 + 32 + 16 + 8 + 4 + 2 + 1) & (source >> 8);
    colors[2] = (128 + 64 + 32 + 16 + 8 + 4 + 2 + 1) & (source >> 16);
    while (true) {
      int diffToGoal = 300 - (colors[0] + colors[1] + colors[2]);
      int add = diffToGoal / 3;
      int newSum = 0;
      for (int i = 0; i < 3; i++) {
        if (colors[i] + add > 255) {
          colors[i] = 255;
        } else if (colors[i] + add < 0) {
          colors[i] = 0;
        } else {
          colors[i] += add;
        }
        newSum += colors[i];
      }
      if (newSum > 150 && newSum < 450) {
        break;
      }
    }
    return rgb(colors[0], colors[1], colors[2]);
  }

  /**
  * Gets the color assigned to a user, or creates a new one if not already
  * assigned.
  *
  * @param colorKey
  * @return
  */
  public static int getUserColor(String senderName) {
    int hash = senderName.hashCode();
    // use negative hash values for Strings... so we don't clash with <Long sourceId>
    if (hash > 0)
      return getUserColor(new Long(-hash));
    else
      return getUserColor(new Long(hash));
  }
  public static int getUserColor(Long sourceId) {
    Integer color = null;
    synchronized (altBkUserAssignedColors) {
      if (!isRestored) {
        restoreColors();
      }
      color = (Integer) altBkUserAssignedColors.get(sourceId);
      if (color == null) {
        // check properties from previous session
        String colorValue = GlobalProperties.getProperty(PROPERTY_PREFIX + "-" + sourceId);
        try {
          if (colorValue != null) {
            color = Integer.valueOf(colorValue);
          }
        } catch (Throwable t) {
        }
      }
      if (color == null) {
        if (altBkUserAssignedColors.size() < altBkUserColors.size()) {
          color = (Integer) altBkUserColors.get(altBkUserAssignedColors.size());
        } else {
          //color = makeBkColor(colorKey);
          color = new Integer(makeUserColor(null)); // null param=random
        }
        // Store for next query
        altBkUserAssignedColors.put(sourceId, color);
        // Store it for next session, just made color, or one of the defaults
        if (GlobalProperties.setProperty(PROPERTY_PREFIX + "-" + sourceId, color.toString()) == null) {
          // If not already stored present, store it in our global list too.
          String listAll = GlobalProperties.getProperty(PROPERTY_PREFIX + "-all", "");
          GlobalProperties.setProperty(PROPERTY_PREFIX + "-all", listAll + " " + sourceId + " " + color);
        }
      }
    }
    return color.intValue();
  }

  /**
  * Android compatible encoding
  * @return color INT compatible with android.graphics.Color
  */
  private static int rgb(int red, int green, int blue) {
    return (0xFF << 24) | (red << 16) | (green << 8) | blue;
  }

  public static int getRed(int color) {
    return (color >> 16) & 0xFF;
  }
  public static int getGreen(int color) {
    return (color >> 8) & 0xFF;
  }
  public static int getBlue(int color) {
    return color & 0xFF;
  }

  public static String toWeb(int color) {
    // strip off the alpha channel
    return "#"+Integer.toHexString(0x00FFFFFF & color);
  }

  private static void restoreColors() {
    isRestored = true;
    try {
      String colorsAll = GlobalProperties.getProperty(PROPERTY_PREFIX + "-all");
      StringTokenizer st = new StringTokenizer(colorsAll);
      while (st.hasMoreTokens()) {
        Long sourceId = Long.valueOf(st.nextToken());
        Integer color = Integer.valueOf(st.nextToken());
        altBkUserAssignedColors.put(sourceId, color);
      }
    } catch (Throwable t) {
      // no-op, ignore restore error
    }
  }
}
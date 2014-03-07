/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.records;

import com.CH_co.util.ArrayUtils;
import java.util.ArrayList;

/** 
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.1 $</b>
*
* @author  Marcin Kurzawa
*/
public class LinkRecordUtils {

  public static Long[] getObjIDs(LinkRecordI[] links) {
    Long[] objIDs = null;
    if (links != null) {
      objIDs = new Long[links.length];
      for (int i=0; i<links.length; i++) {
        objIDs[i] = links[i].getObjId();
      }
      objIDs = (Long[]) ArrayUtils.removeDuplicates(objIDs);
    } 
    return objIDs;
  }

  public static Long[] getOwnerObjIDs(LinkRecordI[] links, short ownerType) {
    Long[] ownerObjIDs = null;
    if (links != null) {
      ArrayList linksL = null;
      for (int i=0; i<links.length; i++) {
        if (links[i].getOwnerObjType().shortValue() == ownerType) {
          Long id = links[i].getOwnerObjId();
          if (linksL == null) linksL = new ArrayList();
          if (!linksL.contains(id))
            linksL.add(id);
        }
      }
      ownerObjIDs = (Long[]) ArrayUtils.toArray(linksL, Long.class);
    }
    return ownerObjIDs;
  }

  /** @return links' IDs */
  public static Long[] getLinkIDs(LinkRecordI[] links) {
    Long[] linkIDs = null;
    if (links != null) {
      linkIDs = new Long[links.length];
      for (int i=0; i<links.length; i++) {
        linkIDs[i] = links[i].getId();
      }
    } 
    return linkIDs;
  }

}
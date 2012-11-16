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

package com.CH_cl.service.actions.fld;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.actions.file.FileAGetFiles;
import com.CH_cl.service.actions.msg.MsgAGet;
import com.CH_cl.service.cache.CacheFldUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.records.FolderRecUtil;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.file.File_GetLinks_Rp;
import com.CH_co.service.msg.dataSets.msg.Msg_GetLinkAndData_Rp;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import com.CH_co.util.Misc;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** 
* <b>Copyright</b> &copy; 2001-2012
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
public class FldASync extends ClientMessageAction {

  /** Creates new FldASync */
  public FldASync() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FldARingRing.class, "FldASync()");
    if (trace != null) trace.exit(FldASync.class);
  }

  /**
  * The action handler performs all actions related to the received message (reply),
  * and optionally returns a request Message.  If there is no request, null is returned.
  */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FldASync.class, "runAction(Connection)");

    ServerInterfaceLayer SIL = getServerInterfaceLayer();
    FetchedDataCache cache = SIL.getFetchedDataCache();

    try {
      Obj_List_Co objs = (Obj_List_Co) getMsgDataSet();
      Object[] objSets = objs.objs;

      // List for re-sending requests for memory overflow folders, and for fetching continuations..
      List resultRequestSetsL = null;
      ArrayList followUpFolderIDsL = new ArrayList();
      ArrayList followUpStartStampsL = new ArrayList();

      for (int i=0; i<objSets.length; i++) {
        Obj_List_Co objSet = (Obj_List_Co) objSets[i];

        Long folderId = null;
        Short folderType = null;
        Long[] linkIDsWithDiffStats = null;
        Integer[] diffBits = null;
        ProtocolMsgDataSet linksNew = null;
        Long[] linkIDsRemoved = null;
        Timestamp resultOverflowStamp = null;
        Boolean requestIncludeBodies = null;
        Boolean requestTruncated = null;
        Boolean requestProbingForNewItems = null;
        Timestamp requestStampLast = null;
        Timestamp requestStartStamp = null;

        boolean anyDiffLinks = false;
        boolean anyNewLinks = false;
        boolean anyRemoved = false;

        folderId = (Long) objSet.objs[0];
        boolean isMemoryOverflow = objSet.objs.length == 2;
        if (!isMemoryOverflow) {
          folderType = (Short) objSet.objs[1];
          linkIDsWithDiffStats = (Long[]) ArrayUtils.toArrayType((Object[]) objSet.objs[2], Long.class);
          diffBits = (Integer[]) ArrayUtils.toArrayType((Object[]) objSet.objs[3], Integer.class);
          linksNew = (ProtocolMsgDataSet) objSet.objs[4];
          linkIDsRemoved = (Long[]) ArrayUtils.toArrayType((Object[]) objSet.objs[5], Long.class);
          resultOverflowStamp = (Timestamp) objSet.objs[6];
          requestIncludeBodies = (Boolean) objSet.objs[7];
          requestTruncated = (Boolean) objSet.objs[8];
          requestProbingForNewItems = (Boolean) objSet.objs[9];
          requestStampLast = (Timestamp) objSet.objs[10];
        }
        requestStartStamp = (Timestamp) objSet.objs[objSet.objs.length-1];


        if (trace != null) trace.data(10, "folderId="+folderId);
        if (trace != null) trace.data(11, "requestStampFirst="+Misc.objToStr(requestStartStamp));

        if (!isMemoryOverflow) {
          if (trace != null) trace.data(12, "num flags changed="+(linkIDsWithDiffStats != null ? linkIDsWithDiffStats.length : 0));
          if (FolderRecord.isFileType(folderType.shortValue()))
            if (trace != null) trace.data(13, "num new file="+(((File_GetLinks_Rp) linksNew).fileLinks != null ? ((File_GetLinks_Rp) linksNew).fileLinks.length : 0));
          else if (FolderRecord.isMsgType(folderType.shortValue()))
            if (trace != null) trace.data(13, "num new msgs="+(((Msg_GetLinkAndData_Rp) linksNew).linkRecords != null ? ((Msg_GetLinkAndData_Rp) linksNew).linkRecords.length : 0));
          if (trace != null) trace.data(14, "num deleted="+(linkIDsRemoved != null ? linkIDsRemoved.length : 0));
          if (trace != null) trace.data(15, "overflow="+(resultOverflowStamp != null));

          if (trace != null) trace.data(20, "folderType="+folderType);
          if (trace != null) trace.data(21, "linkIDsWithDiffStats="+Misc.objToStr(linkIDsWithDiffStats));
          if (trace != null) trace.data(22, "diffStatMarks="+Misc.objToStr(diffBits));
          if (trace != null) trace.data(23, "linksNew="+Misc.objToStr(linksNew));
          if (trace != null) trace.data(24, "linkIDsRemoved="+Misc.objToStr(linkIDsRemoved));
          if (trace != null) trace.data(25, "resultOverflowStamp="+Misc.objToStr(resultOverflowStamp));
          if (trace != null) trace.data(26, "requestIncludeBodies="+Misc.objToStr(requestIncludeBodies));
          if (trace != null) trace.data(27, "requestTruncated="+Misc.objToStr(requestTruncated));
          if (trace != null) trace.data(28, "requestStampLast="+Misc.objToStr(requestStampLast));
          if (trace != null) trace.data(29, "reply regular");
        } else {
          if (trace != null) trace.data(30, "reply MEMORY OVERFLOW");
        }

        // process each result set
        if (!isMemoryOverflow) {
          boolean isFileFolder = FolderRecord.isFileType(folderType.shortValue());
          boolean isMsgFolder = FolderRecord.isMsgType(folderType.shortValue());
          // update our stats
          if (linkIDsWithDiffStats != null && linkIDsWithDiffStats.length > 0) {
            anyDiffLinks = true;
            ArrayList statsL = new ArrayList();
            for (int k=0; k<linkIDsWithDiffStats.length; k++) {
              Long linkId = linkIDsWithDiffStats[k];
              LinkRecordI link = null;
              if (isFileFolder) {
                link = cache.getFileLinkRecord(linkId);
              } else if (isMsgFolder) {
                link = cache.getMsgLinkRecord(linkId);
              }
              if (link != null) {
                Integer serverMark = diffBits[k];
                short serverStatMark = (short) (serverMark.intValue() >> 16);
                short serverLinkStatus = (short) (serverMark.intValue() & 0xFFFF);
                if (link instanceof MsgLinkRecord) {
                  MsgLinkRecord mLink = (MsgLinkRecord) link;
                  mLink.status = new Short(serverLinkStatus);
                } else if (link instanceof FileLinkRecord) {
                  FileLinkRecord fLink = (FileLinkRecord) link;
                  fLink.status = new Short(serverLinkStatus);
                }
                StatRecord stat = cache.getStatRecord(link.getId(), link.getCompatibleStatTypeIndex());
                if (stat != null) {
                  stat.mark = new Short(serverStatMark);
                  statsL.add(stat);
                }
              }
            }
            if (statsL.size() > 0) {
              cache.addStatRecords((StatRecord[]) ArrayUtils.toArray(statsL, StatRecord.class));
            }
          }
          // add new links and stats
          if (linksNew != null) {
            if (isFileFolder) {
              File_GetLinks_Rp linksReply = (File_GetLinks_Rp) linksNew;
              if (linksReply.fileLinks != null && linksReply.fileLinks.length > 0) {
                anyNewLinks = true;
                FileAGetFiles.runAction(SIL, linksReply, this);
              }
            } else if (isMsgFolder) {
              Msg_GetLinkAndData_Rp linksReply = (Msg_GetLinkAndData_Rp) linksNew;
              if (linksReply.linkRecords != null && linksReply.linkRecords.length > 0) {
                anyNewLinks = true;
                MsgAGet.runAction(SIL, linksReply, this);
              }
            }
          }
          // remove deleted links
          if (linkIDsRemoved != null && linkIDsRemoved.length > 0) {
            anyRemoved = true;
            if (isFileFolder) {
              cache.removeFileLinkRecords(linkIDsRemoved);
            } else if (isMsgFolder) {
              cache.removeMsgLinkRecords(linkIDsRemoved);
            }
          }
        }

        // create continuation requests
        if (resultOverflowStamp != null) {
          // to continue fetching when next item exists we must have a folder that was previously fetched...
          if (FolderRecUtil.wasFolderFetchRequestIssued(folderId)) {
            if (requestProbingForNewItems.booleanValue()) {
              // when probing, we must additionally detect any changes to continue synchronizing entire folder
              if (anyDiffLinks || anyNewLinks || anyRemoved) {
                if (trace != null) trace.data(50, "creating Probe CONTINUATION request");
                followUpFolderIDsL.add(folderId);
                followUpStartStampsL.add(resultOverflowStamp);
              }
            } else {
              // when not probing, overflow-stamp and previously-fetched are sufficient to continue
              if (trace != null) trace.data(51, "creating Overflow CONTINUATION request");
              followUpFolderIDsL.add(folderId);
              followUpStartStampsL.add(resultOverflowStamp);
            }
          }
        } else if (isMemoryOverflow) {
          if (trace != null) trace.data(52, "creating MEMORY OVERFLOW request");
          followUpFolderIDsL.add(folderId);
          followUpStartStampsL.add(resultOverflowStamp);
        }
      }

      if (followUpFolderIDsL.size() > 0) {
        resultRequestSetsL = CacheFldUtils.prepareSynchRequest(cache, followUpFolderIDsL, followUpStartStampsL, resultRequestSetsL);
      }

      // re-send overflow requests, and continuation requests
      if (resultRequestSetsL != null && resultRequestSetsL.size() > 0) {
        if (trace != null) trace.data(60, "Re-synch additional folders SENDING REQUEST ");
        if (trace != null) trace.data(61, "set="+resultRequestSetsL);
        SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_SYNC, new Obj_List_Co(resultRequestSetsL)), 90000);
        if (trace != null) trace.data(62, "Re-synch additional folders SUBMITED");
      }

    } catch (Throwable t) {
      t.printStackTrace();
    }

    if (trace != null) trace.exit(FldASync.class, null);
    return null;
  }
}
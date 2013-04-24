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

package com.CH_cl.service.actions.fld;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.actions.cnt.CntAGetContacts;
import com.CH_cl.service.actions.file.FileAGetFiles;
import com.CH_cl.service.actions.msg.MsgAGet;
import com.CH_cl.service.cache.CacheFldUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.cnt.Cnt_GetCnts_Rp;
import com.CH_co.service.msg.dataSets.file.File_GetLinks_Rp;
import com.CH_co.service.msg.dataSets.fld.Fld_Folders_Rp;
import com.CH_co.service.msg.dataSets.msg.Msg_GetLinkAndData_Rp;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import com.CH_co.util.Misc;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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

    Obj_List_Co objs = (Obj_List_Co) getMsgDataSet();
    Object[] objSets = objs.objs;

    // Lists for re-sending requests for memory overflow folders, and for fetching continuations..
    ArrayList followUpFolderIDsL = new ArrayList();
    ArrayList followUpStartStampsL = new ArrayList();
    ArrayList followUpFetchedFlagsL = new ArrayList();
    ArrayList followUpInvalidatedFlagsL = new ArrayList();

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
      Boolean requestWasFetched = null;
      Boolean requestWasInvalidated = null;
      Timestamp requestStampLast = null;
      Timestamp requestStartStamp = null;

      boolean anyDiffLinks = false;
      boolean anyNewLinks = false;
      boolean anyRemoved = false;

      folderId = (Long) objSet.objs[0];
      requestStartStamp = (Timestamp) objSet.objs[objSet.objs.length-1];
      boolean isMemoryOverflow = objSet.objs.length < 14;
      if (isMemoryOverflow) {
        requestProbingForNewItems = (Boolean) objSet.objs[2];
        requestWasFetched = (Boolean) objSet.objs[3];
        requestWasInvalidated = (Boolean) objSet.objs[4];
      } else {
        folderType = (Short) objSet.objs[1];
        linkIDsWithDiffStats = (Long[]) ArrayUtils.toArrayType((Object[]) objSet.objs[2], Long.class);
        diffBits = (Integer[]) ArrayUtils.toArrayType((Object[]) objSet.objs[3], Integer.class);
        linksNew = (ProtocolMsgDataSet) objSet.objs[4];
        linkIDsRemoved = (Long[]) ArrayUtils.toArrayType((Object[]) objSet.objs[5], Long.class);
        resultOverflowStamp = (Timestamp) objSet.objs[6];
        requestIncludeBodies = (Boolean) objSet.objs[7];
        requestTruncated = (Boolean) objSet.objs[8];
        requestProbingForNewItems = (Boolean) objSet.objs[9];
        requestWasFetched = (Boolean) objSet.objs[10];
        requestWasInvalidated = (Boolean) objSet.objs[11];
        requestStampLast = (Timestamp) objSet.objs[12];
      }

//System.out.println("FldASync: ========================");
      if (trace != null) trace.data(10, "folderId="+folderId);
//        System.out.println("folderId="+folderId);
      if (trace != null) trace.data(11, "requestStampFirst="+Misc.objToStr(requestStartStamp));
//        System.out.println("requestStampFirst="+Misc.objToStr(requestStartStamp));

      if (!isMemoryOverflow) {
        if (trace != null) trace.data(12, "num flags changed="+(linkIDsWithDiffStats != null ? linkIDsWithDiffStats.length : 0));
//          System.out.println("num flags changed="+(linkIDsWithDiffStats != null ? linkIDsWithDiffStats.length : 0));
        if (folderId == null) {
          if (trace != null) trace.data(13, "num new folders="+(((Fld_Folders_Rp) linksNew).folderRecords != null ? ((Fld_Folders_Rp) linksNew).folderRecords.length : 0));
          if (trace != null) trace.data(13, "num new shares="+(((Fld_Folders_Rp) linksNew).shareRecords != null ? ((Fld_Folders_Rp) linksNew).shareRecords.length : 0));

//            System.out.println("num new folders="+(((Fld_Folders_Rp) linksNew).folderRecords != null ? ((Fld_Folders_Rp) linksNew).folderRecords.length : 0));
//            System.out.println("num new shares="+(((Fld_Folders_Rp) linksNew).shareRecords != null ? ((Fld_Folders_Rp) linksNew).shareRecords.length : 0));
//            Fld_Folders_Rp r = (Fld_Folders_Rp) linksNew;
//            System.out.println("New FOLDERS:");
//            for (int k=0; k<r.folderRecords.length; k++) {
//              System.out.println(r.folderRecords[k]);
//            }
//            System.out.println("New SHARES:");
//            for (int k=0; k<r.shareRecords.length; k++) {
//              System.out.println(r.shareRecords[k]);
//            }
//            
//            System.out.println("New FOLDERS:");
//            for (int k=0; k<r.folderRecords.length; k++) {
//              System.out.println(ListRenderer.getRenderedText(r.folderRecords[k]));
//            }
//            System.out.println("New SHARES:");
//            for (int k=0; k<r.shareRecords.length; k++) {
//              System.out.println(ListRenderer.getRenderedText(r.shareRecords[k]));
//            }

        } else if (FolderRecord.isContactType(folderType.shortValue())) {
          if (trace != null) trace.data(13, "num new contacts="+(((Cnt_GetCnts_Rp) linksNew).contactRecords != null ? ((Cnt_GetCnts_Rp) linksNew).contactRecords.length : 0));
//            System.out.println("num new contacts="+(((Cnt_GetCnts_Rp) linksNew).contactRecords != null ? ((Cnt_GetCnts_Rp) linksNew).contactRecords.length : 0));
        } else if (FolderRecord.isFileType(folderType.shortValue())) {
          if (trace != null) trace.data(13, "num new files="+(((File_GetLinks_Rp) linksNew).fileLinks != null ? ((File_GetLinks_Rp) linksNew).fileLinks.length : 0));
//            System.out.println("num new files="+(((File_GetLinks_Rp) linksNew).fileLinks != null ? ((File_GetLinks_Rp) linksNew).fileLinks.length : 0));
        } else if (FolderRecord.isMsgType(folderType.shortValue())) {
          if (trace != null) trace.data(13, "num new msgs="+(((Msg_GetLinkAndData_Rp) linksNew).linkRecords != null ? ((Msg_GetLinkAndData_Rp) linksNew).linkRecords.length : 0));
//            System.out.println("num new msgs="+(((Msg_GetLinkAndData_Rp) linksNew).linkRecords != null ? ((Msg_GetLinkAndData_Rp) linksNew).linkRecords.length : 0));
        }
        if (trace != null) trace.data(14, "num deleted="+(linkIDsRemoved != null ? linkIDsRemoved.length : 0));
//          System.out.println("num deleted="+(linkIDsRemoved != null ? linkIDsRemoved.length : 0));
        if (trace != null) trace.data(15, "overflow="+(resultOverflowStamp != null));
//          System.out.println("overflow="+(resultOverflowStamp != null));

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
//          System.out.println("reply regular");
      } else {
        if (trace != null) trace.data(30, "reply MEMORY OVERFLOW");
//          System.out.println("reply MEMORY OVERFLOW");
      }

      // process each result set
      if (!isMemoryOverflow) {
        boolean isFoldersMode = folderId == null;
        boolean isContactsMode = folderType != null ? FolderRecord.isContactType(folderType.shortValue()) : false;
        boolean isFilesMode = folderType != null ? FolderRecord.isFileType(folderType.shortValue()) : false;
        boolean isMsgsMode = folderType != null ? FolderRecord.isMsgType(folderType.shortValue()) : false;
        // update our stats
        if (linkIDsWithDiffStats != null && linkIDsWithDiffStats.length > 0) {
          anyDiffLinks = true;
          ArrayList statsL = new ArrayList();
          for (int k=0; k<linkIDsWithDiffStats.length; k++) {
            Long linkId = linkIDsWithDiffStats[k];
            Record link = null;
            if (isFoldersMode) {
              link = cache.getFolderShareRecord(linkId);
            } else if (isContactsMode) {
              link = cache.getContactRecord(linkId);
            } else if (isFilesMode) {
              link = cache.getFileLinkRecord(linkId);
            } else if (isMsgsMode) {
              link = cache.getMsgLinkRecord(linkId);
            }
            if (link != null) {
              int serverMark = diffBits[k].intValue();
              if (isContactsMode) {
                int permits = serverMark & 0xFFFF;
                short status = (short) (serverMark >> 16);
                ContactRecord cRec = (ContactRecord) link;
                cRec.permits = new Integer(permits);
                cRec.status = new Short(status);
                // trigger listener update by re-adding to cache
                cache.addContactRecords(new ContactRecord[] {cRec});
              } else {
                short serverStatMark = (short) (serverMark >> 16);
                short serverLinkStatus = (short) (serverMark & 0xFFFF);
                if (link instanceof FolderShareRecord) {
                  FolderShareRecord sLink = (FolderShareRecord) link;
                  short canWrite = (short) (serverLinkStatus & 0x0007);
                  short canDelete = (short) ((serverLinkStatus >> 3) & 0x0007);
                  sLink.canWrite = new Short(canWrite);
                  sLink.canDelete = new Short(canDelete);
                  // trigger listener update by re-adding to cache
                  cache.addFolderShareRecords(new FolderShareRecord[] {sLink});
                } else if (link instanceof MsgLinkRecord) {
                  MsgLinkRecord mLink = (MsgLinkRecord) link;
                  mLink.status = new Short(serverLinkStatus);
                  // trigger listener update by re-adding to cache
                  cache.addMsgLinkRecords(new MsgLinkRecord[] {mLink});
                } else if (link instanceof FileLinkRecord) {
                  FileLinkRecord fLink = (FileLinkRecord) link;
                  fLink.status = new Short(serverLinkStatus);
                  // trigger listener update by re-adding to cache
                  cache.addFileLinkRecords(new FileLinkRecord[] {fLink});
                }
                // server skips StatRecord marks for folder-re-synch, so check for BLANK value
                if (serverStatMark != 0) {
                  StatRecord stat = cache.getStatRecord(link.getId(), ((LinkRecordI) link).getCompatibleStatTypeIndex());
                  if (stat != null) {
                    stat.mark = new Short(serverStatMark);
                    statsL.add(stat);
                  }
                }
              }
            }
          }
          if (statsL.size() > 0) {
            cache.addStatRecords((StatRecord[]) ArrayUtils.toArray(statsL, StatRecord.class));
          }
        }
        // Remove deleted links before adding new items
        // Order is important for removal for Folder Shares because if access through a share is removed, other share may give access,
        // this order will keep the folder in cache, but share can be removed and another one inserted back.
        if (linkIDsRemoved != null && linkIDsRemoved.length > 0) {
          anyRemoved = true;
          if (isFoldersMode) {
            Long[] affectedFldIDs = null;
            // removal of shares should also remove the associated folder record
            FolderShareRecord[] fShares = cache.getFolderShareRecords(linkIDsRemoved);
            if (fShares != null && fShares.length > 0) {
              affectedFldIDs = FolderShareRecord.getFolderIDs(fShares);
              if (affectedFldIDs != null && affectedFldIDs.length > 0) {
                FolderRecord[] fRecs = cache.getFolderRecords(affectedFldIDs);
                if (fRecs != null && fRecs.length > 0) {
                  cache.removeFolderRecords(fRecs);
                }
              }
            }
            // remove the shares too - incase folder removal didn't include some
            cache.removeFolderShareRecords(linkIDsRemoved);
            // Now fetch folders for corresponding removed shares, maybe there are other remaining ways to access it through group shares.
            if (affectedFldIDs != null && affectedFldIDs.length > 0)
              SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_GET_FOLDERS_SOME, new Obj_IDList_Co(affectedFldIDs)), 30000);
          } else if (isContactsMode) {
            cache.removeContactRecords(linkIDsRemoved);
          } else if (isFilesMode) {
            cache.removeFileLinkRecords(linkIDsRemoved);
          } else if (isMsgsMode) {
            cache.removeMsgLinkRecords(linkIDsRemoved);
          }
        }
        // add new links and stats
        if (linksNew != null) {
          if (isFoldersMode) {
            Fld_Folders_Rp foldersReply = (Fld_Folders_Rp) linksNew;
            if ((foldersReply.folderRecords != null && foldersReply.folderRecords.length > 0) ||
                    (foldersReply.shareRecords != null && foldersReply.shareRecords.length > 0)) {
              anyNewLinks = true;
              FldAGetFolders.runAction(SIL, foldersReply, this);
            }
          } else if (isContactsMode) {
            Cnt_GetCnts_Rp contactsReply = (Cnt_GetCnts_Rp) linksNew;
            if (contactsReply.contactRecords != null && contactsReply.contactRecords.length > 0) {
              anyNewLinks = true;
              CntAGetContacts.runAction(SIL, contactsReply, this);
            }
          } else if (isFilesMode) {
            File_GetLinks_Rp linksReply = (File_GetLinks_Rp) linksNew;
            if (linksReply.fileLinks != null && linksReply.fileLinks.length > 0) {
              anyNewLinks = true;
              FileAGetFiles.runAction(SIL, linksReply, this);
            }
          } else if (isMsgsMode) {
            Msg_GetLinkAndData_Rp linksReply = (Msg_GetLinkAndData_Rp) linksNew;
            if (linksReply.linkRecords != null && linksReply.linkRecords.length > 0) {
              anyNewLinks = true;
              MsgAGet.runAction(SIL, linksReply, this);
            }
          }
        }
      }

      // create continuation requests
      if (folderId != null) {
        if (resultOverflowStamp != null) {
          // to continue fetching when next item exists we must have a folder that was previously fetched...
          if (cache.wasFolderFetchRequestIssued(folderId)) {
            if (requestProbingForNewItems.booleanValue()) {
              // when probing, we must additionally detect any changes to continue synchronizing entire folder
              if (anyDiffLinks || anyNewLinks || anyRemoved) {
                if (trace != null) trace.data(50, "creating Probe CONTINUATION request");
//                System.out.println("FldASync: creating Probe CONTINUATION request");
                followUpFolderIDsL.add(folderId);
                followUpStartStampsL.add(resultOverflowStamp);
                followUpFetchedFlagsL.add(requestWasFetched);
                followUpInvalidatedFlagsL.add(requestWasInvalidated);
              }
            } else {
              // when not probing, overflow-stamp and previously-fetched are sufficient to continue
              if (trace != null) trace.data(51, "creating Overflow CONTINUATION request");
//              System.out.println("FldASync: creating Overflow CONTINUATION request");
              followUpFolderIDsL.add(folderId);
              followUpStartStampsL.add(resultOverflowStamp);
              followUpFetchedFlagsL.add(requestWasFetched);
              followUpInvalidatedFlagsL.add(requestWasInvalidated);
            }
          }
        } else if (isMemoryOverflow) {
          if (trace != null) trace.data(52, "creating MEMORY OVERFLOW request");
//          System.out.println("FldASync: creating MEMORY OVERFLOW request");
          followUpFolderIDsL.add(folderId);
          followUpStartStampsL.add(requestStartStamp); // retry using the same StartStamp
          followUpFetchedFlagsL.add(requestWasFetched);
          followUpInvalidatedFlagsL.add(requestWasInvalidated);
        }
      }
    }

    // re-send overflow requests, and continuation requests
    if (followUpFolderIDsL.size() > 0) {
      List resultRequestSetsL = null;
      if (trace != null) trace.data(60, "Prepping re-synch request for folder IDs", followUpFolderIDsL);
      resultRequestSetsL = CacheFldUtils.prepareSynchRequest(cache, followUpFolderIDsL, followUpStartStampsL, followUpFetchedFlagsL, followUpInvalidatedFlagsL, resultRequestSetsL);
      // To prevent increased looping we'll mark folders that are during re-synch continuation as valid, 
      // so no other revalidation starting at top-of-list may begin from scratch while this one has not completed.
      cache.markFolderViewInvalidated(followUpFolderIDsL, false);
      if (trace != null) trace.data(61, "Prepped re-synch request are", resultRequestSetsL);
      if (resultRequestSetsL != null && resultRequestSetsL.size() > 0) {
        if (trace != null) trace.data(62, "Re-synch additional folders SENDING REQUEST ");
//        System.out.println("FldASync: Re-synch additional folders SENDING REQUEST ");
        if (trace != null) trace.data(63, "set="+resultRequestSetsL);
//        System.out.println("FldASync: set="+resultRequestSetsL);
        SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_SYNC_NEXT, new Obj_List_Co(resultRequestSetsL)), 90000);
        if (trace != null) trace.data(64, "Re-synch additional folders SUBMITED");
//        System.out.println("FldASync: Re-synch additional folders SUBMITED");
      }
    }

    if (trace != null) trace.exit(FldASync.class, null);
    return null;
  }
}
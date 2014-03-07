/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.frame;


import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.records.filters.ContactFilterCl;
import com.CH_cl.service.records.filters.FolderFilter;
import com.CH_cl.service.records.filters.InvEmlFilter;
import com.CH_co.service.records.FolderRecord;
import com.CH_co.service.records.filters.MultiFilter;
import com.CH_co.service.records.filters.RecordFilter;
import com.CH_co.trace.Trace;
import com.CH_co.util.GlobalProperties;
import com.CH_gui.contactTable.ContactActionTable;
import com.CH_gui.contactTable.ContactTableComponent;
import com.CH_gui.contactTable.ContactTableComponent4Frame;
import com.CH_gui.gui.Template;
import java.awt.BorderLayout;

/** 
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.18 $</b>
*
* @author  Marcin Kurzawa
*/
public class ContactTableFrame extends RecordTableFrame {

  /** Creates new ContactTableFrame */
  public ContactTableFrame() {
    super(com.CH_cl.lang.Lang.rb.getString("title_Contacts"), true, true);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactTableFrame.class, "ContactTableFrame()");

    String propertyName = ContactActionTable.getTogglePropertyName(this);
    String oldShowS = GlobalProperties.getProperty(propertyName);
    boolean oldShow = oldShowS != null ? Boolean.valueOf(oldShowS).booleanValue() : false;
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    //UserRecord myUserRec = cache.getUserRecord();
    RecordFilter filter = new MultiFilter(new RecordFilter[] { 
      //new ContactFilterCl(myUserRec != null ? myUserRec.contactFolderId : null, oldShow),
      new ContactFilterCl(oldShow),
      new FolderFilter(FolderRecord.GROUP_FOLDER),
      new InvEmlFilter(true, false) }
    , MultiFilter.OR);
    ContactTableComponent mainComponent = new ContactTableComponent4Frame(cache.getContactRecords(), filter, Template.get(Template.EMPTY_CONTACTS), Template.get(Template.BACK_CONTACTS), true, false, false);
    mainComponent.addTopContactBuildingPanel();

    this.getContentPane().add(mainComponent, BorderLayout.CENTER);
    mainTableComponent = mainComponent;

    // all JActionFrames already size themself
    setVisible(true);

    if (trace != null) trace.exit(ContactTableFrame.class);
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "ContactTableFrame";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}
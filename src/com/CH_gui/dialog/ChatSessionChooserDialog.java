/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package com.CH_gui.dialog;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.event.FolderRecordEvent;
import com.CH_cl.service.cache.event.FolderRecordListener;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.service.records.FolderPair;
import com.CH_co.service.records.FolderRecord;
import com.CH_co.service.records.MemberContactRecordI;
import com.CH_co.service.records.RecordUtils;
import com.CH_co.trace.Trace;
import com.CH_co.util.ImageNums;
import com.CH_gui.contactTable.ChatSessionCreator;
import com.CH_gui.frame.ChatTableFrame;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.frame.RecordTableFrame;
import com.CH_gui.gui.JMyButton;
import com.CH_gui.list.ListRenderer;
import com.CH_gui.util.GeneralDialog;
import com.CH_gui.util.Images;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

/**
*
* @author Marcin
*/
public class ChatSessionChooserDialog extends GeneralDialog {


  private static final int DEFAULT_OK_BUTTON_INDEX = 0;
  private static final int DEFAULT_CANCEL_BUTTON_INDEX = 1;

  private MemberContactRecordI[] selectedRecords;
  private FolderPair perfectMatch;
  private ArrayList suitableChatsL;

  private ChatSessionChooserPanel panel;

  private JButton okButton;
  private JButton cancelButton;

  private ServerInterfaceLayer serverInterfaceLayer;
  private FetchedDataCache cache;

  private FolderRecordListener folderListener;

  /** Creates new ChatSessionChooserDialog */
  public ChatSessionChooserDialog(Frame frame, MemberContactRecordI[] selectedRecords, FolderPair perfectMatch, ArrayList suitableChatsL) {
    super(frame, "Open Chat Session");
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ChatSessionChooserDialog.class, "ChangeUserNameDialog(Frame frame)");

    this.selectedRecords = selectedRecords;
    this.perfectMatch = perfectMatch;
    this.suitableChatsL = suitableChatsL;

    serverInterfaceLayer = MainFrame.getServerInterfaceLayer();
    cache = serverInterfaceLayer.getFetchedDataCache();

    JButton[] buttons = createButtons();
    panel = new ChatSessionChooserPanel();

    sortChoices();
    initMainPanel(false);

    super.init(frame, buttons, panel, DEFAULT_OK_BUTTON_INDEX, DEFAULT_CANCEL_BUTTON_INDEX);

    folderListener = new FolderListener();
    cache.addFolderRecordListener(folderListener);

    fetchLastUpdates();

    if (trace != null) trace.exit(ChatSessionChooserDialog.class);
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[2];
    buttons[0] = new JMyButton("Open Chat");
    buttons[0].setDefaultCapable(true);
    buttons[0].addActionListener(new ChatSessionChooserDialog.OKActionListener());
    okButton = buttons[0];

    buttons[1] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Cancel"));
    buttons[1].addActionListener(new ChatSessionChooserDialog.CancelActionListener());
    cancelButton = buttons[1];

    return buttons;
  }

  private synchronized void sortChoices() {
    Collections.sort(suitableChatsL, new Comparator() {
      public int compare(Object o1, Object o2) {
        FolderPair p1 = (FolderPair) o1;
        FolderPair p2 = (FolderPair) o2;
        FolderRecord f1 = p1.getFolderRecord();
        FolderRecord f2 = p2.getFolderRecord();
        int rc = 0;
        // sort in descending order
        if (f1.dateUpdated == null && f2.dateUpdated == null)
          rc = ListRenderer.getRenderedText(o2, false, true, false).compareTo(ListRenderer.getRenderedText(o1, false, true, false));
        else if (f1.dateUpdated == null)
          rc = +1;
        else if (f2.dateUpdated == null)
          rc = -1;
        else
          rc = f2.dateUpdated.compareTo(f1.dateUpdated);
        return rc;
      }
    });
  }
  
  private synchronized void initMainPanel(boolean isFinalOrder) {
    panel.icon1.setIcon(Images.get(ImageNums.CHAT48));
    panel.icon2.setIcon(Images.get(ImageNums.CHAT_GROUP48));

    if (perfectMatch != null)
      panel.jLabelPerfectMatch.setText("Open existing:");

    if (selectedRecords.length > 0) {
      panel.contact1.setIcon(ListRenderer.getRenderedIcon(selectedRecords[0]));
      panel.contact1.setText(ListRenderer.getRenderedText(selectedRecords[0]));
    } else {
      panel.contact1.setVisible(false);
    }
    if (selectedRecords.length > 1) {
      panel.contact2.setIcon(ListRenderer.getRenderedIcon(selectedRecords[1]));
      panel.contact2.setText(ListRenderer.getRenderedText(selectedRecords[1]));
    } else {
      panel.contact2.setVisible(false);
    }
    if (selectedRecords.length > 2) {
      panel.contact3.setIcon(ListRenderer.getRenderedIcon(selectedRecords[2]));
      panel.contact3.setText(ListRenderer.getRenderedText(selectedRecords[2]));
    } else {
      panel.contact3.setVisible(false);
    }
    if (selectedRecords.length > 4) {
      panel.contact4.setText("and " + (selectedRecords.length - 3) + " more...");
    } else {
      if (selectedRecords.length > 3) {
        panel.contact4.setIcon(ListRenderer.getRenderedIcon(selectedRecords[3]));
        panel.contact4.setText(ListRenderer.getRenderedText(selectedRecords[3]));
      } else {
        panel.contact4.setVisible(false);
      }
    }

    int i = 0;
    for (; i<suitableChatsL.size(); i++) {
      JRadioButton radio = getRadioButton(i);
      if (radio != null) {
        if (isFinalOrder)
          radio.setText(ListRenderer.getRenderedText(suitableChatsL.get(i), false, true, false));
        else if (i == 0)
          radio.setText("Fetching data ...");
        else
          radio.setText("...");
      }
      else
        break;
    }
    if (i == suitableChatsL.size()) {
      panel.jLabelMore.setVisible(false);
      for (;i<100;i++) {
        JRadioButton radio = getRadioButton(i);
        if (radio != null)
          radio.setVisible(false);
        else
          break;
      }
    }

    initLastUpdates(isFinalOrder);
  }

  private void initLastUpdates(boolean isFinalOrder) {
    updateStamp(perfectMatch, panel.jUpdate1, -1);
    int i = 0;
    for (; i<suitableChatsL.size(); i++) {
      FolderPair pair = (FolderPair) suitableChatsL.get(i);
      JLabel label = getUpdateLabel(i);
      if (label != null)
        updateStamp(isFinalOrder ? pair : null, label, i);
      else
        break;
    }
    if (i == suitableChatsL.size()) {
      for (;i<100;i++) {
        JLabel label = getUpdateLabel(i);
        if (label != null)
          label.setVisible(false);
        else
          break;
      }
    }
  }
  
  private void updateStamp(FolderPair pair, JLabel label, int listIndex) {
    if (pair == null && listIndex == 0) {
      label.setVisible(true);
      label.setText("Please wait ...");
    } else {
      FolderRecord folder = pair != null ? pair.getFolderRecord() : null;
      Timestamp lastUpdate = null;
      if (folder != null) {
        lastUpdate = folder.dateUpdated;
      }
      if (lastUpdate == null) {
        label.setVisible(false);
      } else {
        label.setVisible(true);
        label.setText(getLastStamp(folder.dateUpdated));
      }
    }
  }
  
  private String getLastStamp(Timestamp stamp) {
    String stampS = null;
    Calendar cal = Calendar.getInstance();
    cal.setTime(stamp);
    Calendar calRef = Calendar.getInstance();
    calRef.setTime(new Date());
    if (cal.get(Calendar.YEAR) == calRef.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == calRef.get(Calendar.DAY_OF_YEAR))
      stampS = "today";
    else {
      calRef.setTimeInMillis(calRef.getTimeInMillis() - (24*60*60*1000L));
      if (cal.get(Calendar.YEAR) == calRef.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == calRef.get(Calendar.DAY_OF_YEAR))
        stampS = "yesterday";
      else {
        for (int i=0; i<13; i++) {
          calRef.setTimeInMillis(calRef.getTimeInMillis() - (24*60*60*1000L));
          if (cal.get(Calendar.YEAR) == calRef.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == calRef.get(Calendar.DAY_OF_YEAR)) {
            stampS = "" + (i+2) + " days ago";
            break;
          }
        }
      }
    }
    if (stampS == null) {
      stampS = new SimpleDateFormat("MMM d yyyy").format(stamp);
    }
    return stampS;
  }

  private JRadioButton getRadioButton(int index) {
    JRadioButton jRadio = null;
    switch (index) {
      case 0:
        jRadio = panel.jRadioButton2;
        break;
      case 1:
        jRadio = panel.jRadioButton3;
        break;
      case 2:
        jRadio = panel.jRadioButton4;
        break;
      case 3:
        jRadio = panel.jRadioButton5;
        break;
      case 4:
        jRadio = panel.jRadioButton6;
        break;
      case 5:
        jRadio = panel.jRadioButton7;
        break;
      case 6:
        jRadio = panel.jRadioButton8;
        break;
    }
    return jRadio;
  }

  private JLabel getUpdateLabel(int index) {
    JLabel jUpdate = null;
    switch (index) {
      case 0:
        jUpdate = panel.jUpdate2;
        break;
      case 1:
        jUpdate = panel.jUpdate3;
        break;
      case 2:
        jUpdate = panel.jUpdate4;
        break;
      case 3:
        jUpdate = panel.jUpdate5;
        break;
      case 4:
        jUpdate = panel.jUpdate6;
        break;
      case 5:
        jUpdate = panel.jUpdate7;
        break;
      case 6:
        jUpdate = panel.jUpdate8;
        break;
    }
    return jUpdate;
  }

  private class OKActionListener implements ActionListener {
    public synchronized void actionPerformed (ActionEvent event) {
      if (panel.jRadioButton1.isSelected()) {
        new ChatSessionCreator(selectedRecords).start();
      } else {
        for (int i=0; i<100; i++) {
          JRadioButton jRadio = getRadioButton(i);
          if (jRadio != null && jRadio.isSelected()) {
            FolderPair folderPair = (FolderPair) suitableChatsL.get(i);
            RecordTableFrame openChat = RecordTableFrame.getOpenRecordTableFrame(folderPair);
            if (openChat != null)
              RecordTableFrame.toFrontAnimation(openChat);
            else
              new ChatTableFrame(folderPair);
            break;
          }
        }
      }
      closeDialog();
    }
  }

  private class CancelActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      closeDialog();
    }
  }

  public void closeDialog() {
    if (folderListener != null) {
      cache.removeFolderRecordListener(folderListener);
      folderListener = null;
    }
    super.closeDialog();
  }

  public void fetchLastUpdates() {
    ServerInterfaceLayer SIL = MainFrame.getServerInterfaceLayer();

    // gather share IDs used in this dialog
    ArrayList shareIDsL = new ArrayList();
    Set groupIDs = cache.getFolderGroupIDsMySet();
    if (perfectMatch != null)
      shareIDsL.add(cache.getFolderShareRecordMy(perfectMatch.getId(), groupIDs).shareId);
    for (int i=0; i<suitableChatsL.size(); i++)
      shareIDsL.add(cache.getFolderShareRecordMy(((FolderPair) suitableChatsL.get(i)).getId(), groupIDs).shareId);

    SIL.submitAndReturn(new MessageAction(CommandCodes.FLD_Q_GET_LAST_UPDATE, new Obj_IDList_Co(shareIDsL)));
  }

  private class FolderListener implements FolderRecordListener {
    public void folderRecordUpdated(FolderRecordEvent e) {
      if (e.getEventType() == FolderRecordEvent.SET) {
        FolderRecord[] folders = e.getFolderRecords();
        boolean isOurFolder = false;
        if (perfectMatch != null && RecordUtils.contains(folders, perfectMatch.getId())) {
          isOurFolder = true;
        } else {
          for (int i=0; i<suitableChatsL.size(); i++) {
            Long id = ((FolderPair) suitableChatsL.get(i)).getId();
            if (RecordUtils.contains(folders, id)) {
              isOurFolder = true;
              break;
            }
          }
        }
        if (isOurFolder) {
          sortChoices();
          initMainPanel(true);
        }
      }
    }
  }

}
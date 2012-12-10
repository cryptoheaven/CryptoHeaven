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

package com.CH_gui.dialog;

import com.CH_cl.service.cache.CacheFldUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.FolderOps;
import com.CH_cl.service.ops.SendMessageRunner;
import com.CH_cl.service.records.filters.FolderFilter;
import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.nanoxml.XMLElement;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.msg.Msg_GetMsgs_Rq;
import com.CH_co.service.msg.dataSets.msg.Msg_New_Rq;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.MsgFilter;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import com.CH_co.util.Misc;
import com.CH_gui.addressBook.*;
import com.CH_gui.csv.CSVParser;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.gui.*;
import com.CH_gui.tree.FolderTree;
import com.CH_gui.tree.FolderTreeModelGui;
import com.CH_gui.util.ExtensionFileFilter;
import com.CH_gui.util.GeneralDialog;
import com.CH_gui.util.MessageDialog;
import com.CH_guiLib.gui.JMyComboBox;
import com.CH_guiLib.gui.JMyRadioButton;
import com.CH_guiLib.gui.JMyTextField;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.util.EventObject;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

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
* <b>$Revision: 1.13 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class AddressBookImportWizardDialog extends WizardDialog {

  private static final int PAGE_SOURCE = 0;
  private static final int PAGE_DESTINATION = 1;
  private static final int PAGE_MAPPING = 2;
  private static final int PAGE_SUMMARY = 3;

  // Source page
  private JTextField jFileToImport;
  private String mappingForFileName;
  private int numberOfEntries;
  private JButton jBrowse;
  private JRadioButton jAllowDuplicates;
  private JRadioButton jDoNotImportDuplicates;

  // Destination page
  private FolderTree filteredTree;

  // Mapping page
  private JTable jMappingTable;
  private JButton jChangeMapping;
  private int lastSelectionRow = -1;

  // Summary page
  private JTextArea jSummaryArea;
  private JProgressBar jProgressBar;


  private ServerInterfaceLayer SIL;
  private FetchedDataCache cache;

  private boolean interrupted;

  private String[] destinationFields = new String[] {
    "Full Name", "Job title", "Company", "File as", 
    "E-mail Address", "E-mail Display Name", "E-mail 2 Address", "E-mail 2 Display Name", "E-mail 3 Address", "E-mail 3 Display Name",
    "Web page", "IM address",
    "Business Address", "Home Address", "Other Address",
    "Business Phone", "Business Phone 2", "Business Fax", "Callback Phone", "Car Phone", "Company Phone", "Home Phone", "Home Phone 2", "Home Fax", "ISDN", "Mobile Phone", "Other Phone", "Other Fax", "Pager", "Primary Phone",
    "Notes"
  };

  private String[][][] defaultHeaderMappings = new String[][][] {
    {{ "Full Name" }, { "Name" }, { "Title", " ", "Last Name", ",", "First Name", " ", "Middle Name", ",", "Suffix" }, { "Nickname" }},
    {{ "Job title" }, { "Job" }},
    {{ "Company" }, { "Company Name" }},
    {{ "File as" }, { "Nickname" }, { "Name" }, { "Last Name", ",", "First Name", " ", "Middle Name" }},
    {{ "E-mail Address" }},
    {{ "E-mail Display Name" }},
    {{ "E-mail 2 Address" }},
    {{ "E-mail 2 Display Name" }},
    {{ "E-mail 3 Address" }},
    {{ "E-mail 3 Display Name" }},
    {{ "Web page" }, { "Home Page" }},
    {{ "IM address" }},
    {{ "Business Address" }, { "Business Street", "\n", "Business Street 2", "\n", "Business Street 3", "\n", "Business City", ",", "Business State", "\n", "Business Postal Code", "\n", "Business Country" }},
    {{ "Home Address" }, { "Home Street", "\n", "Home Street 2", "\n", "Home Street 3", "\n", "Home City", ",", "Home State", "\n", "Home Postal Code", "\n", "Home Country" }},
    {{ "Other Address" }, { "Other Street", "\n", "Other Street 2", "\n", "Other Street 3", "\n", "Other City", ",", "Other State", "\n", "Other Postal Code", "\n", "Other Country" }},
    {{ "Callback Phone" }, { "Callback" }},
    {{ "Company Phone" }, { "Company Main Phone" }},
    {{ "Mobile Phone" }, { "Mobile" }},
    {{ "Notes" }, { "Note" }, { "Comments" }, { "Comment" }},
  };

  private String[] availableHeaders;

  /** Creates new AddressBookImportWizardDialog */
  public AddressBookImportWizardDialog(Frame parent) {
    super(parent, com.CH_cl.lang.Lang.rb.getString("title_Import_Address_Book_Wizard"));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AddressBookImportWizardDialog.class, "AddressBookImportWizardDialog()");
    init();
    if (trace != null) trace.exit(AddressBookImportWizardDialog.class);
  }

  private void init() {
    this.SIL = MainFrame.getServerInterfaceLayer();
    this.cache = SIL.getFetchedDataCache();
    super.initialize();
  }

  public JComponent[] createWizardPages() {
    return new JComponent[] { createSourcePanel(),
                              createDestinationPanel(),
                              createMappingPanel(),
                              createSummaryPanel()
    };
  }

  public boolean finishTaskRunner() {
    String importFileName = getFileNameToImport();
    if (mappingForFileName != null && mappingForFileName.equals(importFileName)) {
      File file = new File(importFileName);
      boolean isFile = file.isFile();
      if (isFile) {
        try {
          boolean allowDuplicates = jAllowDuplicates.isSelected();
          FolderPair destPair = filteredTree.getLastSelectedPair();

          // if no duplicates, fetch all addresses first so we can do comparisons
          if (!allowDuplicates) {
            if (!cache.wasFolderFetchRequestIssued(destPair.getFolderRecord().folderId)) {
              // Mark the folder as "fetch issued"
              cache.markFolderFetchRequestIssued(destPair.getFolderRecord().folderId);
              // <shareId> <ownerObjType> <ownerObjId> <fetchNum> <timestamp>
              // use initial fetch size of MAX size because we are not waiting for multiple stages of fetching before continuing... probably should wait for completion or interruption of all stages
              Msg_GetMsgs_Rq request = new Msg_GetMsgs_Rq(destPair.getFolderShareRecord().shareId, Record.RECORD_TYPE_FOLDER, destPair.getFolderRecord().folderId, null, (short) Msg_GetMsgs_Rq.FETCH_NUM_LIST__MAX_SIZE__HARD_LIMIT, (Timestamp) null);
              MainFrame.getServerInterfaceLayer().submitAndWait(new MessageAction(CommandCodes.MSG_Q_GET_BRIEFS, request), 120000);
            }
          }

          // gather existing addresses so we can exclude duplicates
          MsgLinkRecord[] existingLinks = cache.getMsgLinkRecordsForFolder(destPair.getFolderRecord().folderId);
          MsgDataRecord[] existingDatas = cache.getMsgDataRecords(MsgLinkRecord.getMsgIDs(existingLinks));
          MsgDataRecord[] existingAddresses = (MsgDataRecord[]) RecordUtils.filter(existingDatas, new MsgFilter(MsgDataRecord.OBJ_TYPE_ADDR));

          // import addresses
          Long shareId = destPair.getFolderShareRecord().shareId;
          Record[] recipient = new Record[] { destPair };
          FileInputStream in = new FileInputStream(file);
          CSVParser parser = new CSVParser(in);
          // load headers of import file
          String[] headers = parser.getLine();
          // load data of import file
          String[] line = null;
          int countProcessed = 0;
          int countSkipped = 0;
          int countDuplicates = 0;
          jProgressBar.setVisible(true);
          jProgressBar.revalidate();
          while ((line=parser.getLine()) != null) {
            // escape loop if CANCEL pressed
            if (interrupted) break;
            String[] mappedLine = makeDestinationDataFromRowData(line);
            boolean anyValues = false;
            for (int i=0; i<mappedLine.length; i++) {
              if (mappedLine[i] != null && mappedLine[i].length() > 0) {
                anyValues = true;
                break;
              }
            }
            if (anyValues) {
              boolean isDuplicate = false;
              // different address is when any of 'name', 'file as', 'email' is different
              for (int i=0; existingAddresses!=null && i<existingAddresses.length; i++) {
                MsgDataRecord addr = existingAddresses[i];
                String name = mappedLine[0].trim();
                String fileAs = mappedLine[3].trim();
                if (fileAs.length() == 0)
                  fileAs = name;
                String email = mappedLine[4].trim();
                boolean same = name.equalsIgnoreCase(addr.name) && fileAs.equalsIgnoreCase(addr.fileAs) && EmailRecord.isAddressEqual(email, addr.email);
                if (same) {
                  countDuplicates ++;
                  isDuplicate = true;
                  break;
                }
              }

              countProcessed ++;

              if (!isDuplicate || allowDuplicates) {
                XMLElement[] address = makeAddressFromData(mappedLine, destinationFields.length);
                BASymmetricKey key = new BASymmetricKey(32);
                MsgLinkRecord[] links = SendMessageRunner.prepareMsgLinkRecords(SIL, recipient, key);
                MsgDataRecord data = SendMessageRunner.prepareMsgDataRecord(key, new Short(MsgDataRecord.IMPORTANCE_NORMAL_PLAIN), new Short(MsgDataRecord.OBJ_TYPE_ADDR), address[0].toString(), address[1].toString(), null);
                Msg_New_Rq request = new Msg_New_Rq(shareId, null, links[0], data);
                request.hashes = SendMessageRunner.prepareAddrHashes(data);
                MessageAction action = new MessageAction(CommandCodes.MSG_Q_NEW, request);
                // escape loop if CANCEL pressed
                if (interrupted) break;
                // synchronize every 5 addresses
                if (countProcessed % 5 == 0)
                  SIL.submitAndWait(action, 60000, 3);
                else
                  SIL.submitAndReturn(action);
              } else {
                countSkipped ++;
              }
            } // end if anyValues
            jProgressBar.setValue(countProcessed);
          } // end while
          String msg = null;
          if (interrupted)
            msg = "Import process was interrupted.\n";
          else
            msg = "Import completed successfuly.\n";
          msg += "\nItems processed: " + countProcessed;
          msg += "\nItems imported: " + (countProcessed - countSkipped);
          if (!allowDuplicates)
            msg += "\nDuplicate items skipped: " + countSkipped;
          else 
            msg += "\nDuplicate items found: " + countDuplicates;
          MessageDialog.showInfoDialog(AddressBookImportWizardDialog.this, msg, "Import finished", true);
          in.close();
        } catch (Throwable t) {
        }
      }
    }
    return true;
  }

  public String[] getWizardTabNames() {
    return new String[] { com.CH_cl.lang.Lang.rb.getString("tab_Source"), 
                          com.CH_cl.lang.Lang.rb.getString("tab_Destination"), 
                          com.CH_cl.lang.Lang.rb.getString("tab_Mapping"), 
                          com.CH_cl.lang.Lang.rb.getString("tab_Summary") };
  }

  public boolean goFromTab(int tabIndex) {
    boolean rc = false;
    switch (tabIndex) {
      case PAGE_SOURCE:
        String fileName = getFileNameToImport();
        if (fileName.length() == 0) {
          // no-op, disables next if nothing selected
        } else {
          File file = new File(fileName);
          if (mappingForFileName != null && mappingForFileName.equals(fileName)) {
            rc = true; // the same file as last time - OK
          } else {
            boolean isFile = file.isFile();
            if (isFile) {
              // load headers of import file
              try {
                FileInputStream in = new FileInputStream(file);
                CSVParser parser = new CSVParser(in);
                String[] headers = parser.getLine();
                int count = 0;
                while (parser.getLine() != null)
                  count ++;
                numberOfEntries = count;
                jProgressBar.setMaximum(numberOfEntries);
                //System.out.println("Num= " + numberOfEntries);
                in.close();
                String[] destHeaders = destinationFields;
                DefaultTableModel model = (DefaultTableModel) jMappingTable.getModel();
                Vector rowsV = model.getDataVector();
                // remove extra headers
                for (int i=rowsV.size()-1; i>=0; i--) {
                  Vector rowDataV = (Vector) rowsV.elementAt(i);
                  String textField = (String) rowDataV.elementAt(1);
                  if (ArrayUtils.find(destHeaders, textField) < 0)
                    rowsV.removeElementAt(i);
                }
                // add missing headers
                for (int i=0; i<destHeaders.length; i++) {
                  String header = destHeaders[i];
                  if (!isRowPresent(rowsV, header)) {
                    Vector rowDataV = new Vector();
                    rowDataV.addElement(new Object[] { "", Boolean.FALSE });
                    rowDataV.addElement(header);
                    model.addRow(rowDataV);
                  }
                }
                // all loaded ok, remember import file name
                mappingForFileName = fileName;
                availableHeaders = headers;
                makeDefaultMappingCodes();
                if (jMappingTable.getRowCount() > 0)
                  jMappingTable.getSelectionModel().setSelectionInterval(0, 0);
                jMappingTable.repaint();
                rc = true;
              } catch (Throwable t) {
                t.printStackTrace();
              }
            }
          }
        }
        if (!rc) {
          String msg = "Selected file name is not a valid choice. \nPlease choose a file to import.";
          if (fileName.length() == 0) {
            msg = "You have not selected a file to import. \nPlease choose a file to import.";
          }
          MessageDialog.showWarningDialog(AddressBookImportWizardDialog.this, msg, "Invalid File", false);
        }
        break;
      case PAGE_DESTINATION:
        FolderPair fPair = filteredTree.getLastSelectedPair();
        if (fPair == null) {
          // no-op, disables next if nothing selected
        } else {
          rc = fPair.getFolderRecord().isAddressType();
        }
        if (!rc) {
          String msg = "Selected folder is not a valid Address Book. \nPlease choose an Address Book folder.";
          MessageDialog.showWarningDialog(AddressBookImportWizardDialog.this, msg, "Invalid Folder", false);
        }
        break;
      case PAGE_MAPPING:
        rc = true;
        break;
      case PAGE_SUMMARY:
        rc = true;
        break;
    }
    return rc;
  }

  public void goToTab(int tabIndex) {
    if (tabIndex == PAGE_MAPPING) {
      // no-op
    } else if (tabIndex == PAGE_SUMMARY) {
      // prepare the summary page
      String text = "Source file selected: \n" + getFileNameToImport() + "\n\nNumber of records found: " + numberOfEntries;
      text += "\n\n" + (jAllowDuplicates.isSelected() ? "Allow duplicates to be created." : "Do not import already existing items.");
      FolderPair fPair = filteredTree.getLastSelectedPair();
      FolderShareRecord fShare = fPair != null ? fPair.getFolderShareRecord() : null;
      text += "\n\nDestination folder: " + (fShare != null ? fShare.getFolderName() : "");
      jSummaryArea.setText(text);
      validate();
    }
  }

  public boolean isFinishActionReady() {
    boolean rc = new File(getFileNameToImport()).isFile();
    if (rc) {
      FolderPair fPair = filteredTree.getLastSelectedPair();
      if (fPair == null) {
        rc = false;
      } else {
        rc = fPair.getFolderRecord().isAddressType();
      }
    }
    return rc;
  }

  public void setInterruptProgress(boolean interrupt) {
    interrupted = interrupt;
  }


  private JPanel createSourcePanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    jFileToImport = new JMyTextField();
    jBrowse = new JMyButton("Browse ...");
    jAllowDuplicates = new JMyRadioButton("Allow duplicates to be created.");
    jDoNotImportDuplicates = new JMyRadioButton("Do not import already existing items.");
    ButtonGroup group = new ButtonGroup();
    group.add(jAllowDuplicates);
    group.add(jDoNotImportDuplicates);
    group.setSelected(jAllowDuplicates.getModel(), true);

    // focus on the File text field
    jFileToImport.addHierarchyListener(new InitialFocusRequestor());

    // add File Chooser to Browse button
    jBrowse.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        javax.swing.filechooser.FileFilter ff_csv = new ExtensionFileFilter("Comma Separated Values (*.csv)", "csv");
        javax.swing.filechooser.FileFilter ff_txt = new ExtensionFileFilter("Text Documents (*.txt)", "txt");
        fc.addChoosableFileFilter(ff_csv);
        fc.addChoosableFileFilter(ff_txt);
        fc.setFileFilter(ff_csv);
        int retVal = fc.showOpenDialog(AddressBookImportWizardDialog.this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
          File file = fc.getSelectedFile();
          jFileToImport.setText(file.getAbsolutePath());
        }
      }
    });


    int posY = 0;

    panel.add(new JMyLabel("Import Address Book from Comma Separated Values file."), new GridBagConstraints(0, posY, 2, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;
    panel.add(new JMyLabel("File to import:"), new GridBagConstraints(0, posY, 2, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));
    posY ++;
    panel.add(jFileToImport, new GridBagConstraints(0, posY, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));
    panel.add(jBrowse, new GridBagConstraints(1, posY, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(1, 5, 5, 5), 0, 0));
    posY ++;
    panel.add(new JMyLabel("Options:"), new GridBagConstraints(0, posY, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;
    panel.add(jAllowDuplicates, new GridBagConstraints(0, posY, 2, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));
    posY ++;
    panel.add(jDoNotImportDuplicates, new GridBagConstraints(0, posY, 2, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));
    posY ++;

    // filler
    panel.add(new JMyLabel(), new GridBagConstraints(0, posY, 1, 1, 10, 10, 
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0,0,0,0), 0, 0));

    return panel;
  }


  private JPanel createDestinationPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    // Initially make sure there is an Address Book folder, do this before constructing the tree.
    FolderPair addressBook = FolderOps.getOrCreateAddressBook(MainFrame.getServerInterfaceLayer());
    if (addressBook == null) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          String msg = "Address Book folder could not be created or is temporarily unavailable!";
          MessageDialog.showWarningDialog(null, msg, "Address Book unavailable", true);
          closeDialog();
        }
      });
    }

    FolderFilter filter = FolderFilter.NON_LOCAL_FOLDERS;
    FolderTreeModelGui treeModel = new FolderTreeModelGui(filter);
    FolderRecord[] allFolderRecords = cache.getFolderRecords();
    FolderPair[] allFolderPairs = CacheFldUtils.convertRecordsToPairs(allFolderRecords);
    allFolderPairs = (FolderPair[]) filter.filterInclude(allFolderPairs);
    treeModel.addNodes(allFolderPairs);
    filteredTree = new FolderTree(treeModel);
    filteredTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    if (addressBook != null) { // expand Address Book 
      TreePath treePath = filteredTree.getFolderTreeModel().getPathToRoot(addressBook);
      filteredTree.expandPath(treePath);
      filteredTree.setSelectionPath(treePath);
    }
    JScrollPane treeScrollPane = new JScrollPane(filteredTree);
    treeScrollPane.setPreferredSize(new Dimension(200, 200));

    int posY = 0;

    panel.add(new JMyLabel("Select destination Address Book folder:"), new GridBagConstraints(0, posY, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));
    posY ++;

    panel.add(treeScrollPane, new GridBagConstraints(0, posY, 1, 1, 10, 10, 
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(1, 5, 5, 5), 0, 0));
    posY ++;

    // filler
    /*
    panel.add(new JMyLabel(), new GridBagConstraints(0, posY, 1, 1, 10, 10, 
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0,0,0,0), 0, 0));
    */

    return panel;
  }


  private JPanel createMappingPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    Vector columnNames = new Vector();
    columnNames.addElement("Import From Source Text field(s)");
    columnNames.addElement("Import To Address Book field");

    Vector rowData = new Vector();

    jMappingTable = new JTable(new MyTableModel(rowData, columnNames));
    jMappingTable.getTableHeader().setReorderingAllowed(false);
    jMappingTable.setDefaultEditor(Boolean.class, new DefaultCellEditor(new JMyCheckBox()) {
      public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Component c = null;
        Object[] objSet = (Object[]) value;
        String s = (String) objSet[0];
        s = decodeMapCode(s, availableHeaders);
        Boolean b = (Boolean) objSet[1];
        c = super.getTableCellEditorComponent(table, b, isSelected, row, column);
        if (c instanceof JCheckBox) {
          JCheckBox cb = (JCheckBox) c;
          cb.setText(s);
        }
        return c;
      }
      public boolean isCellEditable(EventObject e) {
        if (e instanceof MouseEvent) {
          MouseEvent me = (MouseEvent) e;
          int x = me.getX();
          return x <= 16;
        } else {
          return false;
        }
      }
    });

    jMappingTable.setDefaultRenderer(Boolean.class, new MyDefaultTableCellRenderer() {
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = null;
        if (value instanceof Object[]) {
          String s = (String) ((Object[]) value)[0];
          s = decodeMapCode(s, availableHeaders);
          Boolean b = (Boolean) ((Object[]) value)[1];
          boolean selected = b.booleanValue();
          Component comp = super.getTableCellRendererComponent(table, b, isSelected, false, row, column);
          c = new JMyCheckBox(s, selected);
          c.setBackground(comp.getBackground());
          c.setForeground(comp.getForeground());
        } 
        return c;
      }
    });
    jMappingTable.setDefaultRenderer(Object.class, new MyDefaultTableCellRenderer() {
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = null;
        if (value instanceof String) {
          String s = (String) value;
          c = super.getTableCellRendererComponent(table, s, isSelected, false, row, column);
        }
        return c;
      }
    });
    jMappingTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        int row = jMappingTable.getSelectedRow();
        if (row >= 0)
          lastSelectionRow = row;
        else if (lastSelectionRow >= 0 && jMappingTable.getRowCount() > lastSelectionRow)
          jMappingTable.getSelectionModel().setSelectionInterval(lastSelectionRow, lastSelectionRow);
        else if (lastSelectionRow >= 0 && jMappingTable.getRowCount() <= lastSelectionRow && jMappingTable.getRowCount() > 0)
          jMappingTable.getSelectionModel().setSelectionInterval(0, 0);
      }
    });
    jMappingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    jMappingTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          changeMappingPressed();
        }
      }
    });
    jChangeMapping = new JMyButton("Change Mapping");
    jChangeMapping.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        changeMappingPressed();
      }
    });
    JScrollPane tableScrollPane = new JScrollPane(jMappingTable);
    tableScrollPane.setPreferredSize(new Dimension(200, 200));

    int posY = 0;

    panel.add(new JMyLabel("Map the fields you wish to Import:"), new GridBagConstraints(0, posY, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));
    posY ++;

    panel.add(tableScrollPane, new GridBagConstraints(0, posY, 1, 1, 10, 10, 
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(1, 5, 1, 5), 0, 0));
    posY ++;

    panel.add(jChangeMapping, new GridBagConstraints(0, posY, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(1, 5, 5, 5), 0, 0));
    posY ++;

    // filler
    /*
    panel.add(new JMyLabel(), new GridBagConstraints(0, posY, 1, 1, 10, 10, 
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0,0,0,0), 0, 0));
    */

    return panel;
  }


  private JPanel createSummaryPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    jSummaryArea = new JMyTextArea();
    jSummaryArea.setLineWrap(true);
    jSummaryArea.setEditable(false);
    jProgressBar = new JProgressBar();
    jProgressBar.setVisible(false);

    int posY = 0;

    panel.add(new JScrollPane(jSummaryArea), new GridBagConstraints(0, posY, 1, 1, 10, 10, 
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(jProgressBar, new GridBagConstraints(0, posY, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    return panel;
  }

  private void changeMappingPressed() {
    int row = jMappingTable.getSelectedRow();
    if (row >= 0) {
      Vector rowDataV = (Vector) ((DefaultTableModel) jMappingTable.getModel()).getDataVector().elementAt(row);
      Object[] objSet = (Object[]) rowDataV.elementAt(0);
      String mapCode = (String) objSet[0];
      Boolean selected = (Boolean) objSet[1];
      String destField = (String) rowDataV.elementAt(1);
      ChangeMappingDialog d = new ChangeMappingDialog(AddressBookImportWizardDialog.this, destField, mapCode, selected.booleanValue(), availableHeaders);
      String newMapCode = mapCode;
      if (!d.isCancelled) {
        newMapCode = d.returnValue;
        objSet[0] = newMapCode;
        objSet[1] = Boolean.valueOf(d.isImport && newMapCode != null && newMapCode.length() > 0);
      }
      if (newMapCode == null || newMapCode.length() == 0) {
        objSet[1] = Boolean.FALSE;
      }
      jMappingTable.repaint();
    }
  }

  private String getFileNameToImport() {
    return jFileToImport.getText().trim();
  }

  private boolean isRowPresent(Vector rowsV, String textField) {
    boolean present = false;
    for (int i=0; i<rowsV.size(); i++) {
      Vector rowDataV = (Vector) rowsV.elementAt(i);
      String rowTextField = (String) rowDataV.elementAt(1);
      if (rowTextField.equals(textField)) {
        present = true;
        break;
      }
    }
    return present;
  }

  /**
  * Apply the default mapping onto the JTable data model
  */
  private void makeDefaultMappingCodes() {
    //System.out.println("Available headers are " + Misc.objToStr(availableHeaders));
    Vector rowsV = ((DefaultTableModel) jMappingTable.getModel()).getDataVector();
    for (int i=0; i<rowsV.size(); i++) {
      Vector dataRowV = (Vector) rowsV.elementAt(i);
      Object[] objSet = (Object[]) dataRowV.elementAt(0);
      String rowDestHeader = (String) dataRowV.elementAt(1);
      String[][] rowDestMapping = findHeaderMapping(rowDestHeader, defaultHeaderMappings);
      //System.out.println("Mapping for " + rowDestHeader + " is " + (rowDestMapping != null ? Misc.objToStr(rowDestMapping) : "default"));
      if (rowDestMapping == null)
        rowDestMapping = new String[][] {{ rowDestHeader }};
      // reset old map
      objSet[0] = "";
      boolean mapFound = false;
      for (int k=0; k<rowDestMapping.length; k++) {
        String mapCode = makeMapCode(rowDestMapping[k], availableHeaders);
        if (mapCode != null && mapCode.length() > 0) {
          objSet[0] = mapCode;
          objSet[1] = Boolean.TRUE;
          mapFound = true;
          break;
        }
      }
      if (!mapFound)
        objSet[1] = Boolean.FALSE;
    }
  }

  /**
  * Find a set of default mappings for a given header name.
  */
  private static String[][] findHeaderMapping(String destHeader, String[][][] availableHeaderMappings) {
    String[][] mapping = null;
    for (int i=0; i<availableHeaderMappings.length; i++) {
      String[][] m = availableHeaderMappings[i];
      if (m[0][0].equalsIgnoreCase(destHeader)) {
        mapping = m;
        break;
      }
    }
    return mapping;
  }

  /**
  * @return generated String representation of column mappings with appropriate separators.
  */
  private static String makeMapCode(String[] headerMapping, String[] availableHeaders) {
    StringBuffer mapCodeBuf = new StringBuffer();
    String lastSeparator = null;
    for (int i=0; i<headerMapping.length; i++) {
      if (headerMapping[i].equals(" ")) {
        lastSeparator = "s";
      } else if (headerMapping[i].equals("\n")) {
        lastSeparator = "n";
      } else if (headerMapping[i].equals(",")) {
        lastSeparator = ",";
      } else if (headerMapping[i].equals("|")) {  
        // noop
      } else {
        int index = -1;
        for (int x=0; x<availableHeaders.length; x++) {
          if (availableHeaders[x].equalsIgnoreCase(headerMapping[i])) {
            index = x;
            break;
          }
        }
        if (index >= 0) {
          if (mapCodeBuf.length() > 0) {
            if (lastSeparator != null) {
              mapCodeBuf.append(lastSeparator);
            } else {
              mapCodeBuf.append("|");
            }
          }
          lastSeparator = null;
          mapCodeBuf.append(index);
        }
      }
    }
    return mapCodeBuf.toString();
  }

  /**
  * @return user readable meaning of map code.
  */
  private static String decodeMapCode(String mapCode, String[] availableHeaders) {
    StringTokenizer st = new StringTokenizer(mapCode, "" + 's' + 'n' + ',' + '|', true);
    StringBuffer strBuf = new StringBuffer();
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      if (token.equals("s"))
        strBuf.append(" ");
      else if (token.equals("n"))
        strBuf.append(" + ");
      else if (token.equals(","))
        strBuf.append(", ");
      else if (token.equals("|"))
        strBuf.append(""); // divider between integers only
      else {
        int fieldIndex = Integer.parseInt(token);
        strBuf.append(availableHeaders[fieldIndex]);
      }
    }
    return strBuf.toString();
  }

  /**
  * @return Vector of data representing a row of JTable with specified destination header.
  */
  private Vector getDataRow(String destinationHeader) {
    Vector dataRowV = null;
    DefaultTableModel model = (DefaultTableModel) jMappingTable.getModel();
    Vector rowsV = model.getDataVector();
    for (int x=0; x<rowsV.size(); x++) {
      Vector dRowV = (Vector) rowsV.elementAt(x);
      String textField = (String) dRowV.elementAt(1);
      if (textField.equalsIgnoreCase(destinationHeader)) {
        dataRowV = dRowV;
        break;
      }
    }
    return dataRowV;
  }

  /**
  * @return All destination fields from rowData and current mapping.
  */
  private String[] makeDestinationDataFromRowData(String[] rowData) {
    String[] mappedData = new String[destinationFields.length];

    for (int i=0; i<destinationFields.length; i++) {
      String destField = destinationFields[i];
      // Find mapping for destination field
      Vector dataRowV = getDataRow(destField);
      Object[] objSet = (Object[]) dataRowV.elementAt(0);
      boolean mappingEnabled = ((Boolean) objSet[1]).booleanValue();
      if (mappingEnabled) {
        String mapCode = (String) objSet[0];
        mappedData[i] = makeMappedData(mapCode, rowData);
      } else {
        // mapping disabled
        mappedData[i] = null;
      }
    } // next i

    return mappedData;
  }

  private static String makeMappedData(String mapCode, String[] rowData) {
    StringTokenizer st = new StringTokenizer(mapCode, "" + 's' + 'n' + ',' + '|', true);
    StringBuffer dataBuf = new StringBuffer();
    String lastSeparator = null;
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      if (token.equals("s")) {
        lastSeparator = " ";
      } else if (token.equals("n")) {
        lastSeparator = "\n";
      } else if (token.equals(",")) {
        lastSeparator = ", ";
      } else if (token.equals("|")) {
        // noop
      } else {
        int fieldIndex = Integer.parseInt(token);
        String rData = null;
        if (fieldIndex < rowData.length)
          rData = rowData[fieldIndex];
        if (rData != null && rData.length() > 0) {
          if (lastSeparator != null && dataBuf.length() > 0)
            dataBuf.append(lastSeparator);
          lastSeparator = null;
          dataBuf.append(rData);
        }
      }
    }
    return dataBuf.toString();
  }


  private static XMLElement[] makeAddressFromData(String[] d, int numFields) {
    String[] dd = new String[numFields];
    for (int i=0; i<numFields; i++) {
      String s = "";
      if (i < d.length && d[i] != null)
        s = d[i];
      dd[i] = s;
    }
    d = dd;
    XMLElement name = NamePanel.getContent(d[0], d[1], d[2], d[3]);
    String[] emailsS = new String[] { d[4], d[6], d[8] };
    String[] displaysS = new String[] { d[5], d[7], d[9] };
    XMLElement emails = EmailPanel.getContent(EmailPanel.getTypes(), emailsS, displaysS, 0);
    XMLElement web = WebPanel.getContent(d[10], d[11]);
    XMLElement addresses = AddressPanel.getContent(AddressPanel.getTypes(), new String[] { d[12], d[13], d[14] }, 0);
    String[] phonesS = new String[] { d[15], d[16], d[17], d[18], d[19], d[20], d[21], d[22], d[23], d[24], d[25], d[26], d[27], d[28], d[29] };
    XMLElement phones = PhonePanel.getContent(PhonePanel.getTypes(), phonesS, new int[] { 0, 6, 2, 10 });
    XMLElement contentPreview = ContactInfoPanel.getContentPreview(d[0], d[3], emailsS, displaysS, 0, PhonePanel.getTypes(), phonesS);
    XMLElement content = ContactInfoPanel.getContent(new XMLElement[] { name, emails, web, addresses, phones });
    if (d[30] != null && d[30].trim().length() > 0) {
      XMLElement notes = new XMLElement();
      notes.setName("Notes");
      notes.setAttribute("type", "text/html");
      notes.setContent(Misc.encodePlainIntoHtml(d[30]));
      content.addChild(notes);
    }
    return new XMLElement[] { contentPreview, content };
  }


  /**
  * Test Blank Wizard
  */
  public static void main(String[] args) {
    com.CH_gui.frame.MainFrameStarter.initLookAndFeelComponentDefaults();
    new AddressBookImportWizardDialog((Frame) null);
  }


  private class MyTableModel extends DefaultTableModel {
    private MyTableModel(Vector data, Vector columnNames) {
      super(data, columnNames);
    }
    public Class getColumnClass(int c) {
      Object value = getValueAt(0, c);
      if (value instanceof Object[])
        //return super.getColumnClass(c);
        return Boolean.class;
      else
        return super.getColumnClass(c);
    }
    public boolean isCellEditable(int row, int col) {
      if (row >= 0 && col == 0) { 
        return true;
      } else {
        return false;
      }
    }
    public void setValueAt(Object value, int row, int col) {
      if (col == 0 && row >= 0 && value instanceof Boolean) {
        Boolean b = (Boolean) value;
        Object[] objSet = (Object[]) getValueAt(row, col);
        objSet[1] = b;
        fireTableCellUpdated(row, col);
        String mapCode = (String) objSet[0];
        if ((mapCode == null || mapCode.length() == 0) && b.booleanValue())
          changeMappingPressed();
      } else {
        super.setValueAt(value, row, col);
      }
    }
  }


  private static class ChangeMappingDialog extends GeneralDialog {
    private Vector sourcesV = new Vector();
    private Vector separatorsV = new Vector();

    private JPanel jSourcePanel;
    private JButton jAddMore;
    private JCheckBox jImport;

    private int DEFAULT_OK = 0;
    private int DEFAULT_CANCEL = 1;

    private boolean isCancelled = true;
    private String returnValue = "";
    private boolean isImport = false;

    private String[] availableFields;

    private ChangeMappingDialog(Dialog parent, String destField, String initialMapCode, boolean importSelected, String[] availableFields) {
      super(parent, "Change Mapping");
      this.availableFields = (String[]) ArrayUtils.concatinate(new String[] { "" }, availableFields);
      JButton[] buttons = createButtons();
      JComponent mainComponent = createMainPanel(destField, initialMapCode, importSelected);
      setModal(true);
      super.init(parent, buttons, mainComponent, DEFAULT_OK, DEFAULT_CANCEL);
    }
    private JButton[] createButtons() {
      JButton[] buttons = new JButton[2];
      buttons[0] = new JButton("OK");
      buttons[0].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          isCancelled = false;
          isImport = jImport.isSelected();
          StringBuffer codeMapBuf = new StringBuffer();
          String lastSeparator = null;
          String separators = "|sn,";
          for (int i=0; i<sourcesV.size(); i++) {
            if (i > 0) {
              JComboBox comboSeparator = (JComboBox) separatorsV.elementAt(i-1);
              int indexSeparator = comboSeparator.getSelectedIndex();
              lastSeparator = "" + separators.charAt(indexSeparator);
            }
            JComboBox comboSource = (JComboBox) sourcesV.elementAt(i);
            int indexSource = comboSource.getSelectedIndex();
            if (indexSource > 0) {
              if (codeMapBuf.length() > 0 && lastSeparator != null)
                codeMapBuf.append(lastSeparator);
              lastSeparator = null;
              codeMapBuf.append(indexSource-1);  // less one for the inserted BLANK as first choice
            }
          }
          returnValue = codeMapBuf.toString();
          closeDialog();
        }
      });
      buttons[1] = new JButton("Cancel");
      buttons[1].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          isCancelled = true;
          closeDialog();
        }
      });
      return buttons;
    }
    private JComponent createMainPanel(String destFieldName, String initialMapCode, boolean importSelected) {
      JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout());

      jSourcePanel = new JPanel();
      jSourcePanel.setLayout(new GridBagLayout());

      int posY = 0;
      JLabel label1 = new JMyLabel("Select source field from file");
      JLabel label2 = new JMyLabel("to map to Address Book field:");
      jSourcePanel.add(label1, new GridBagConstraints(0, posY, 1, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));
      posY ++;
      jSourcePanel.add(label2, new GridBagConstraints(0, posY, 1, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));
      posY ++;
      JLabel destField = new JMyLabel(destFieldName);
      destField.setFont(destField.getFont().deriveFont(Font.BOLD));
      jSourcePanel.add(destField, new GridBagConstraints(0, posY, 1, 1, 10, 0, 
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;

      jAddMore = new JMyButton("Add More");
      jAddMore.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          addSeparatorField("");
          addSourceField(-1);
        }
      });
      jSourcePanel.add(jAddMore, new GridBagConstraints(0, posY, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;

      jImport = new JMyCheckBox("Import this field", importSelected);
      jSourcePanel.add(jImport, new GridBagConstraints(0, posY, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;

      StringTokenizer st = new StringTokenizer(initialMapCode, "" + 's' + 'n' + ',' + '|', true);
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        if (token.equals("s"))
          addSeparatorField("Space");
        else if (token.equals("n"))
          addSeparatorField("New Line");
        else if (token.equals(","))
          addSeparatorField("Comma");
        else if (token.equals("|"))
          addSeparatorField("");
        else {
          int fieldIndex = Integer.parseInt(token);
          addSourceField(fieldIndex);
        }
        posY ++;
      }

      // if no initial mapping, provide one entry field
      if (sourcesV.size() == 0)
        addSourceField(-1);

      panel.add(jSourcePanel, BorderLayout.NORTH);

      JScrollPane jScrollPane = new JScrollPane();
      jScrollPane.setViewport(new JBottomStickViewport());
      jScrollPane.setViewportView(panel);
      jScrollPane.setPreferredSize(new Dimension(200, 310));

      return jScrollPane;
    }


    private void addSourceField(int fieldIndex) {
      jSourcePanel.remove(jAddMore);
      jSourcePanel.remove(jImport);

      JComboBox combo = new JMyComboBox(availableFields);
      combo.setSelectedIndex(fieldIndex+1); // add one because we added BLANK as first choice
      sourcesV.addElement(combo);

      // focus on the first field
      if (sourcesV.size() == 1) {
        ((JComboBox) sourcesV.elementAt(0)).addHierarchyListener(new InitialFocusRequestor());
      }

      int posY = sourcesV.size()*2 + separatorsV.size()*2 + 2;

      jSourcePanel.add(new JMyLabel("Source Field " + sourcesV.size() + ":"), new GridBagConstraints(0, posY-1, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));
      jSourcePanel.add((JComponent) sourcesV.lastElement(), new GridBagConstraints(0, posY, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));

      jSourcePanel.add(jAddMore, new GridBagConstraints(0, posY+1, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
      jSourcePanel.add(jImport, new GridBagConstraints(0, posY+2, 3, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

      jSourcePanel.revalidate();
      jSourcePanel.repaint();
    }


    private void addSeparatorField(String initialChoice) {
      jSourcePanel.remove(jAddMore);
      jSourcePanel.remove(jImport);

      JComboBox combo = new JMyComboBox(new String[] { "", "Space", "New Line", "Comma" });
      combo.setSelectedItem(initialChoice);
      separatorsV.addElement(combo);

      int posY = sourcesV.size()*2 + separatorsV.size()*2 + 2;

      jSourcePanel.add(new JMyLabel("Separate with:"), new GridBagConstraints(0, posY-1, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 1, 5), 0, 0));
      jSourcePanel.add((JComponent) separatorsV.lastElement(), new GridBagConstraints(0, posY, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(1, 5, 5, 5), 0, 0));

      jSourcePanel.add(jAddMore, new GridBagConstraints(0, posY+1, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
      jSourcePanel.add(jImport, new GridBagConstraints(0, posY+2, 3, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

      jSourcePanel.revalidate();
      jSourcePanel.repaint();
    }
  }

}
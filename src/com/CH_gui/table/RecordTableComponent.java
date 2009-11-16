/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */


package com.CH_gui.table;

import com.CH_gui.action.*;
import com.CH_gui.contactTable.*;
import com.CH_gui.fileTable.*;
import com.CH_gui.frame.*;
import com.CH_gui.gui.*;
import com.CH_gui.list.*;
import com.CH_gui.menuing.*;
import com.CH_gui.msgs.*;
import com.CH_gui.msgTable.*;
import com.CH_gui.recycleTable.*;
import com.CH_gui.tree.*;

import com.CH_guiLib.gui.*;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.event.*;
import com.CH_cl.service.records.filters.*;

import com.CH_co.gui.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: 1.32 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public abstract class RecordTableComponent extends JPanel implements VisualsSavable, DisposableObj {

  private JLabel jTitleLabel;
  private JLabel jDescriptionLabel;
  private JTextField jFilterField;
  private JButton jFilterGoButton;
  private JButton jFilterClearButton;
  private JCheckBox jFilterMsgBodyCheck;
  private JButton jFilterCloseButton;

  private JPanel jTopPanel;
  private JPanel jTitlePanel;
  private JPanel jDescriptionPanel;
  private JPanel jUtilityButtonPanel;
  private JPanel jFilterPanel;

  private RecordTableScrollPane recordTableScrollPane;
  private Object title; // Record or String
  private String description;
  private Long lastFolderId;

  private CardLayout cardLayout;
  private JComponent cards;
  private String emptyTemplateName;
  private String backTemplateName;
  private String categoryTemplateName;

  private JComponent mainTableComp;
  private JComponent mainPreviewComp;
  private JPanel mainCardPanel;

  private boolean suppressVisualsSavable;
  private FolderShareListener folderShareListener;


  /** 
   * Creates new RecordTableComponent.
   * @param recordTableScrollPane is often an RecordActionTable which is a subclass of RecordTableScrollPane
   */
  public RecordTableComponent(RecordTableScrollPane recordTableScrollPane) {
    this(recordTableScrollPane, Template.get(Template.NONE), Template.get(Template.NONE));
  }
  public RecordTableComponent(RecordTableScrollPane recordTableScrollPane, String emptyTemplateName) {
    this(recordTableScrollPane, emptyTemplateName, Template.get(Template.NONE), Template.get(Template.NONE), false);
  }
  public RecordTableComponent(RecordTableScrollPane recordTableScrollPane, String emptyTemplateName, boolean suppressVisualsSavable) {
    this(recordTableScrollPane, emptyTemplateName, Template.get(Template.NONE), Template.get(Template.NONE), suppressVisualsSavable);
  }
  public RecordTableComponent(RecordTableScrollPane recordTableScrollPane, String emptyTemplateName, String backTemplateName) {
    this(recordTableScrollPane, emptyTemplateName, backTemplateName, Template.get(Template.NONE), false);
  }
  public RecordTableComponent(RecordTableScrollPane recordTableScrollPane, String emptyTemplateName, String backTemplateName, String categoryTemplateName) {
    this(recordTableScrollPane, emptyTemplateName, backTemplateName, categoryTemplateName, false);
  }
  public RecordTableComponent(RecordTableScrollPane recordTableScrollPane, String emptyTemplateName, String backTemplateName, String categoryTemplateName, boolean suppressVisualsSavable) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableComponent.class, "RecordTableComponent(RecordTableScrollPane recordTableScrollPane, String emptyTemplateName, String backTemplateName, String categoryTemplateName, boolean suppressVisualsSavable)");
    if (trace != null) trace.args(recordTableScrollPane, emptyTemplateName, backTemplateName, categoryTemplateName);
    if (trace != null) trace.args(suppressVisualsSavable);

    this.recordTableScrollPane = recordTableScrollPane;
    this.emptyTemplateName = emptyTemplateName;
    this.backTemplateName = backTemplateName;
    this.categoryTemplateName = categoryTemplateName;
    this.suppressVisualsSavable = suppressVisualsSavable;

    jTitleLabel = new JMyLabel();
    jDescriptionLabel = new JMyLabel();
    jFilterField = new JMyTextField("", 15);
    jFilterField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        setFilterNarrowing(jFilterField.getText(), jFilterMsgBodyCheck.isSelected());
        jFilterField.selectAll();
      }
    });
    jFilterGoButton = new JMyButton(Images.get(ImageNums.GO16));
    jFilterGoButton.setBorder(new EmptyBorder(0,0,0,0));
    jFilterGoButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    jFilterGoButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        setFilterNarrowing(jFilterField.getText(), jFilterMsgBodyCheck.isSelected());
        jFilterField.selectAll();
        jFilterField.requestFocus();
      }
    });
    jFilterClearButton = null;
//    jFilterClearButton = new JMyButton("Clear");
//    jFilterClearButton.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent event) {
//        setFilterNarrowing(null, jFilterMsgBodyCheck.isSelected());
//        jFilterField.setText("");
//        jFilterField.requestFocus();
//      }
//    });
    jFilterMsgBodyCheck = new JMyCheckBox();
    RecordTableModel tableModel = recordTableScrollPane.getTableModel();
    if (tableModel instanceof MsgTableModel) {
      MsgTableModel msgTableModel = (MsgTableModel) tableModel;
      if (msgTableModel.isModeAddr())
        jFilterMsgBodyCheck.setText("include address details");
      else if (!msgTableModel.isModeMsgBody())
        jFilterMsgBodyCheck.setText("include message bodies");
    } else if (tableModel instanceof RecycleTableModel) {
      jFilterMsgBodyCheck.setText("include message bodies");
    }
    jFilterMsgBodyCheck.setVisible(jFilterMsgBodyCheck.getText().trim().length() > 0);
    jFilterMsgBodyCheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        setFilterNarrowing(jFilterField.getText(), jFilterMsgBodyCheck.isSelected());
        jFilterField.selectAll();
        jFilterField.requestFocus();
      }
    });
    jFilterCloseButton = new JMyButton(Images.get(ImageNums.X15));
    jFilterCloseButton.setBorder(new LineBorder(Color.darkGray, 1));
    jFilterCloseButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    jFilterCloseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        ((RecordActionTable) RecordTableComponent.this.recordTableScrollPane).getFilterAction().putValue(Actions.STATE_CHECK, Boolean.FALSE);
        setFilterNarrowing(null, jFilterMsgBodyCheck.isSelected());
      }
    });

    jTopPanel = new JPanel(new GridBagLayout());
    jFilterPanel = new JPanel(new GridBagLayout());
    jFilterPanel.setVisible(false);
    jFilterPanel.setBorder(new LineBorder(Color.darkGray, 1));
    jFilterPanel.add(new JMyLabel("Look for:"), new GridBagConstraints(0, 0, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(3, 3, 3, 3), 0, 0));
    jFilterPanel.add(jFilterField, new GridBagConstraints(1, 0, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 3, 3, 3), 0, 0));
    jFilterPanel.add(jFilterGoButton, new GridBagConstraints(2, 0, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(3, 3, 3, 3), 0, 0));
    if (jFilterClearButton != null) {
      jFilterPanel.add(jFilterClearButton, new GridBagConstraints(3, 0, 1, 1, 0, 0, 
          GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(3, 3, 3, 3), 0, 0));
    }
    jFilterPanel.add(jFilterMsgBodyCheck, new GridBagConstraints(4, 0, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 3, 3, 3), 0, 0));
    jFilterPanel.add(new JLabel(), new GridBagConstraints(5, 0, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
    jFilterPanel.add(jFilterCloseButton, new GridBagConstraints(6, 0, 1, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(3, 3, 3, 3), 0, 0));
    jTitlePanel = new JPanel(new BorderLayout(0, 0));
    jTitlePanel.add(jTitleLabel, BorderLayout.CENTER);
    jDescriptionPanel = new JPanel(new BorderLayout(0, 0));
    jDescriptionPanel.add(jDescriptionLabel, BorderLayout.CENTER); 
    jUtilityButtonPanel = new JPanel(new GridBagLayout());

    init();

    /** If right mouse button is clicked then the popup is shown. */
    if (recordTableScrollPane instanceof ActionProducerI)
      addMouseListener(new PopupMouseAdapter(this, (ActionProducerI) recordTableScrollPane));

    // Listen on folder changes so we can adjust title and description
    FetchedDataCache.getSingleInstance().addFolderShareRecordListener(folderShareListener = new FolderShareListener());

    if (trace != null) trace.exit(RecordTableComponent.class);
  }


  public void removeRecordListeners() {
    if (recordTableScrollPane != null) {
      recordTableScrollPane.removeRecordSelectionListeners();
      if (folderShareListener != null) {
        FetchedDataCache.getSingleInstance().removeFolderShareRecordListener(folderShareListener);
        folderShareListener = null;
      }
    }
  }


  public RecordActionTable getActionTable() {
    if (recordTableScrollPane instanceof RecordActionTable)
      return (RecordActionTable) recordTableScrollPane;
    return null;
  }

  public RecordTableScrollPane getRecordTableScrollPane() {
    return recordTableScrollPane;
  }


  /**
   * Setting the title overwrites the default title construction when initData() is called.
   * Setting it to 'null' resets the custom title setting.
   */
  public void setTitle(Object title) {
    this.title = title;
    if (title != null) {
      jTitleLabel.setText(ListRenderer.getRenderedText(title, false, false, false));
      jTitleLabel.setIcon(ListRenderer.getRenderedIcon(title));
      validate();
    }
    else if (lastFolderId != null)
      changeTitle(lastFolderId);
  }
  /**
   * Setting the description overwrites the default description construction when initData() is called.
   * Setting it to 'null' resets the custom title setting.
   */
  public void setDescription(String description) {
    this.description = description;
    if (description != null) {
      jDescriptionLabel.setText("<html>"+description+"</html>"); // html gives us multi-line label capability
      jDescriptionLabel.revalidate();
    } else if (lastFolderId != null) {
      changeDescription(lastFolderId);
    }
  }
  /**
   * Setting the description overwrites the default description construction when initData() is called.
   * Setting it to 'null' resets the custom title setting.
   * Use this method if you want to control the GUI elements like alignment or icon positioning...
   */
  public void setDescription(JLabel jDescriptionLabel) {
    if (jDescriptionLabel != null) {
      this.jDescriptionLabel = jDescriptionLabel;
      jDescriptionPanel.removeAll();
      jDescriptionPanel.add(jDescriptionLabel, BorderLayout.CENTER);
      this.description = jDescriptionLabel.getText();
    } else {
      setDescription((String) null);
    }
    this.jDescriptionPanel.revalidate();
  }
  public void addPreviewComponent(JSplitPane splitPane, JComponent previewComponent) {
    mainPreviewComp = previewComponent;

    mainTableComp.setBorder(new EmptyBorder(0,0,0,0));
    mainPreviewComp.setBorder(new EmptyBorder(0,0,0,0));

    // link the table with preview panel
    if (mainPreviewComp instanceof RecordSelectionListener)
      getActionTable().addRecordSelectionListener((RecordSelectionListener) mainPreviewComp);

    // clear old content with no split pane to make room for new
    mainCardPanel.removeAll();

    splitPane.setTopComponent(mainTableComp);
    splitPane.setBottomComponent(mainPreviewComp);

    mainCardPanel.add(BorderLayout.CENTER, splitPane);
  }
  public void addUtilityComponent(JComponent utilityComponent) {
    int count = jUtilityButtonPanel.getComponentCount();
    jUtilityButtonPanel.add(utilityComponent, new GridBagConstraints(10-count, 0, 1, 1, 0, 0, 
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    this.jUtilityButtonPanel.revalidate();
  }

  private void init() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableComponent.class, "init()");

    // So the split panes are not limited in movement, but must have at least visible header.
    setMinimumSize(new Dimension(0, 24));
    setLayout(new GridBagLayout());
    setBorder(new EmptyBorder(0,0,0,0));

    AbstractButton refreshButton = null;
    AbstractButton cloneButton = null;
    AbstractButton splitLayoutButton = null;
    AbstractButton filterButton = null;

    if (recordTableScrollPane instanceof RecordActionTable) {
      RecordActionTable actionTable = (RecordActionTable) recordTableScrollPane;
      Action refreshAction = actionTable.getRefreshAction();
      Action cloneAction = actionTable.getCloneAction();
      Action splitLayoutAction = actionTable.getSplitLayoutAction();
      Action filterAction = actionTable.getFilterAction();

      if (refreshAction != null)
        refreshButton = ActionUtilities.makeSmallComponentToolButton(refreshAction);
      if (cloneAction != null)
        cloneButton = ActionUtilities.makeSmallComponentToolButton(cloneAction);
      if (splitLayoutAction != null)
        splitLayoutButton = ActionUtilities.makeSmallComponentToolButton(splitLayoutAction);
      if (filterAction != null) {
        filterButton = ActionUtilities.makeSmallComponentToolButton(filterAction);
        final AbstractButton _filterButton = filterButton;
        filterButton.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent event) {
            if (jFilterPanel != null) {
              boolean setVisible = _filterButton.isSelected();
              jFilterPanel.setVisible(setVisible);
              if (!setVisible) {
                setFilterNarrowing(null, jFilterMsgBodyCheck.isSelected());
              } else {
                setFilterNarrowing(jFilterField.getText(), jFilterMsgBodyCheck.isSelected());
                jFilterField.requestFocus();
                jFilterField.selectAll();
              }
            }
          }
        });
      }
    }

    int posY = 0;
    jTopPanel.add(jFilterPanel, new GridBagConstraints(0, posY, 3, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
    posY ++;
    jTopPanel.add(jTitlePanel, new GridBagConstraints(0, posY, 1, 2, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 3, 3, 5), 0, 0));
    jTopPanel.add(jDescriptionPanel, new GridBagConstraints(1, posY, 1, 2, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 5, 3, 5), 0, 0));
    jTopPanel.add(jUtilityButtonPanel, new GridBagConstraints(2, posY, 1, 1, 0, 0, 
        GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new MyInsets(1, 1, 1, 1), 0, 0));

    add(jTopPanel, new GridBagConstraints(0, 0, 1, 1, 10, 0, 
        GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));

    if (refreshButton != null) {
      jUtilityButtonPanel.add(refreshButton, new GridBagConstraints(10, 0, 1, 1, 0, 0, 
          GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    }
    if (cloneButton != null) {
      jUtilityButtonPanel.add(cloneButton, new GridBagConstraints(9, 0, 1, 1, 0, 0, 
          GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    }
    if (splitLayoutButton != null) {
      jUtilityButtonPanel.add(splitLayoutButton, new GridBagConstraints(8, 0, 1, 1, 0, 0, 
          GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    }
    if (filterButton != null) {
      jUtilityButtonPanel.add(filterButton, new GridBagConstraints(7, 0, 1, 1, 0, 0, 
          GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    }
    /*
    add(recordTableScrollPane, new GridBagConstraints(0, 1, 5, 1, 60, 60, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
     */

    cards = new JPanel();
    cardLayout = new CardLayout();
    cards.setLayout(cardLayout);
    mainTableComp = recordTableScrollPane;
    final JComponent backTemplate = Template.getTemplate(backTemplateName);
    if (backTemplate != null) {
      FilledLayeredPane pane = new FilledLayeredPane();
      pane.add(backTemplate, JLayeredPane.DEFAULT_LAYER);
      pane.add(recordTableScrollPane, JLayeredPane.PALETTE_LAYER);
      recordTableScrollPane.setOpaque(false);
      recordTableScrollPane.setOpaqueTable(false);
      mainTableComp = pane;
    }
    //mainCardPanel.setLayout(new GridBagLayout());
    mainCardPanel = new JPanel();
    mainCardPanel.setLayout(new BorderLayout(0,0));
    mainCardPanel.add(BorderLayout.CENTER, mainTableComp);
    //cards.add(mainTableComp, "table");
    cards.add(mainCardPanel, "table");
    JComponent emptyTemplate = Template.getTemplate(emptyTemplateName);
    if (emptyTemplate != null) {
      JScrollPane scrollPane = new JScrollPane(emptyTemplate);
      scrollPane.setBorder(new EmptyBorder(0,0,0,0));
      cards.add(scrollPane, "emptyTemplate");
    }
    // if Filter action exists, include empty filter results template
    JComponent emptyFilterResultsTemplate = null;
    if (isFilterable()) {
      emptyFilterResultsTemplate = Template.getTemplate(Template.FILTER_NO_RESULTS);
      JScrollPane scrollPane = new JScrollPane(emptyFilterResultsTemplate);
      scrollPane.setBorder(new EmptyBorder(0,0,0,0));
      cards.add(scrollPane, "emptyFilterResultsTemplate");
    }
    JComponent categoryTemplate = Template.getTemplate(categoryTemplateName);
    if (categoryTemplate != null) {
      JScrollPane scrollPane = new JScrollPane(categoryTemplate);
      scrollPane.setBorder(new EmptyBorder(0,0,0,0));
      cards.add(scrollPane, "categoryTemplate");
    }
//    if (emptyTemplate != null) {
//      JScrollPane emptyPane = new JScrollPane(emptyTemplate);
//      FilledLayeredPane pane = new FilledLayeredPane();
//      JComponent backTemplate2 = Template.getTemplate(backTemplateName);
//      if (backTemplate2 != null) {
//        backTemplate2.setOpaque(false);
//        pane.add(backTemplate2, JLayeredPane.DEFAULT_LAYER);
//      }
//      pane.add(emptyPane, JLayeredPane.PALETTE_LAYER);
//      emptyPane.setOpaque(false);
//      cards.add(pane, "template");
//    }
    add(cards, new GridBagConstraints(0, 1, 1, 1, 20, 20, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    // Since we want initially to show the table, wait SHOW_DELAY seconds and if it doesn't fill,
    // show the template.
    // If template exists for this table...
    if (emptyTemplate != null || categoryTemplate != null || emptyFilterResultsTemplate != null) {
      showTableOrDelayedTemplate();
      recordTableScrollPane.getTableModel().addTableModelListener(new TableModelListener() {
        public void tableChanged(TableModelEvent e) {
          showTableOrDelayedTemplate();
        }
      });

      recordTableScrollPane.getTableModel().addParentFolderListener(new ParentFolderListener() {
        public void parentFolderChanged(ParentFolderEvent e) {
          showTableOrDelayedTemplate();
        }
      });
    }

    recordTableScrollPane.setAreaComponent(this);
    restoreVisuals(GlobalProperties.getProperty(MiscGui.getVisualsKeyName(this), "Dimension width 160 height 180"));

    if (trace != null) trace.exit(RecordTableComponent.class);
  }


  private Timer timer = null;
  private final Object timerMonitor = new Object();
  private FolderPair evaluatedFolder = null;
  private void showTableOrDelayedTemplate() {
    FolderPair pfp = recordTableScrollPane.getTableModel().getParentFolderPair();
    int delay = pfp != null ? Template.SHOW_DELAY_FOR_FOLDER_TABLES : Template.SHOW_DELAY_FOR_NON_FOLDER_TABLES;
    showTableOrDelayedTemplate(delay);
  }
  private void showTableOrDelayedTemplate(int delayMillis) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableComponent.class, "showTableOrDelayedTemplate(int delayMillis)");
    synchronized (timerMonitor) {
      boolean runTimer = false;
      RecordTableModel model = recordTableScrollPane.getTableModel();
      FolderPair pfp = model.getParentFolderPair();
      FolderRecord fr = pfp != null ? pfp.getFolderRecord() : null;
      FolderShareRecord fsr = pfp != null ? pfp.getFolderShareRecord() : null;
      if (fr != null && fr.isCategoryType()) {
        cardLayout.show(cards, "categoryTemplate");
      } else {
        int rowCount = model.getRowCount();
        if (rowCount > 0 || fsr == null || !model.isContentFetched(fsr.shareId)) {
          if (trace != null) trace.data(10, "1");
          if (timer != null) timer.stop();
          cardLayout.show(cards, "table");
        } else if (rowCount == 0 && fsr != null && model.isContentFetched(fsr.shareId) && (fr == null || fr.objectCount != null)) {
          if (trace != null) trace.data(20, "2");
          if (timer != null) timer.stop();
          if ((fr != null && fr.objectCount != null && fr.objectCount.longValue() == 0) || recordTableScrollPane.getTableModel().getFilterNarrowing() == null)
            cardLayout.show(cards, "emptyTemplate");
          else
            cardLayout.show(cards, "emptyFilterResultsTemplate");
        }
        if (rowCount == 0 && 
              (   (fsr != null && !model.isContentFetched(fsr.shareId)) || 
                  (fsr == null && model instanceof ContactTableModel)
              )
           )
        {
          runTimer = true;
        }
        if (fr != null && fr.objectCount == null) {
          runTimer = true;
        }
      }
      if (runTimer) {
        if (trace != null) trace.data(30, "timer involved");
        evaluatedFolder = pfp;
        if (timer != null) {
          if (trace != null) trace.data(40, "restarting timer");
          timer.setDelay(delayMillis);
          timer.restart();
        } else {
          if (trace != null) trace.data(50, "new timer needed");
          timer = new Timer(delayMillis, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
              Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "actionPerformed(ActionEvent event)");
              if (trace != null) trace.args(event);
              timer.stop();
              // Run the check in a threaded mode because we don't want to block the gui event thread which calls it.
              performEmptyTableCheck_Threaded();
              if (trace != null) trace.exit(getClass());
            } // actionPerformed()
          });
          timer.start();
        }
      }
    } // end synchronized
    if (trace != null) trace.exit(RecordTableComponent.class);
  } // end showTableOrDelayedTemplate()

  private void performEmptyTableCheck_Threaded() {
    Thread th = new Thread(new Runnable() {
      public void run() {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");
        performEmptyTableCheck();
        if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
        if (trace != null) trace.exit(getClass());
        if (trace != null) trace.clear();
      }
    }, "Empty Table Checker");
    th.setDaemon(true);
    th.start();
  }
  private void performEmptyTableCheck() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableComponent.class, "performEmptyTableCheck");
    RecordTableModel model = recordTableScrollPane.getTableModel();
    // If still no rows have filled
    FolderPair pfp = model.getParentFolderPair();
    FolderRecord fr = pfp != null ? pfp.getFolderRecord() : null;
    if (model.getRowCount() == 0 || (fr != null && fr.objectCount == null)) { // additional check for no count in case of file folder where folders are listed causing row count > 0 but files are not fetched
      if (trace != null) trace.data(10, "row count still zero");
      // Some tables with null Folder (contact list table) have templates too.
      boolean templateOk = evaluatedFolder == null && model instanceof ContactTableModel;
      templateOk = templateOk || (evaluatedFolder != null && evaluatedFolder == model.getParentFolderPair());
      if (trace != null) trace.data(20, templateOk);
      if (templateOk) {
        long countObjs = 0;
        ServerInterfaceLayer SIL = null;
        FetchedDataCache cache = null;
        boolean replyRecived = false;
        if (evaluatedFolder != null) {
          SIL = MainFrame.getServerInterfaceLayer();
          cache = SIL.getFetchedDataCache();
          // check if still logged in as the same user (no switch identity was in progress) and the folder is still in the cache (not deleted)
          Long myUID = cache.getMyUserId();
          if (myUID != null) {
            if (cache.getFolderRecord(evaluatedFolder.getFolderRecord().folderId) != null) {
              Hashtable groupIDsHT = cache.getFolderGroupIDsHT(myUID);
              if (evaluatedFolder.getFolderShareRecord().isOwnedBy(myUID, groupIDsHT)) {

                ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.FLD_Q_GET_FOLDER_SIZE, new Obj_ID_Rq(evaluatedFolder.getFolderShareRecord().shareId)), 60000);

                // default to 1 element if the count times out and we don't know if its empty or not...
                if (msgAction == null) {
                  countObjs = 1;
                }

                DefaultReplyRunner.nonThreadedRun(SIL, msgAction);

                if (msgAction != null && msgAction.getActionCode() == CommandCodes.FLD_A_GET_FOLDER_SIZE) {
                  replyRecived = true;
                  Obj_IDPair_Co data = (Obj_IDPair_Co) msgAction.getMsgDataSet();
                  countObjs = data.objId_1.longValue();
                  evaluatedFolder.getFolderRecord().objectCount = new Long(countObjs);
                }
              }
            }
          }
        }

        boolean refresh = false;
        if (countObjs > 0 && model.getRowCount() == 0 && recordTableScrollPane.getTableModel().getFilterNarrowing() != null) {
          cardLayout.show(cards, "emptyFilterResultsTemplate");
        } else if (countObjs <= 0 && model.getRowCount() == 0) {
          cardLayout.show(cards, "emptyTemplate");
        } else if (model.getRowCount() == 0) {
          if (replyRecived)
            if (recordTableScrollPane.getTableModel().getFilterNarrowing() == null)
              refresh = true;
//        } else if (model instanceof FileTableModel) {
//          // in file folder there may be sub-directories but no files, so refresh if count of files is different - this is possible because all files are fetched at once (no batches)
//          FileLinkRecord[] links = cache.getFileLinkRecordsOwnerAndType(evaluatedFolder.getFolderRecord().folderId, new Short(Record.RECORD_TYPE_FOLDER));
//          if (links != null && links.length != countObjs) {
//            if (replyRecived)
//              if (recordTableScrollPane.getTableModel().getFilterNarrowing() == null)
//                refresh = true;
//          }
        }

        if (refresh) {
          RecordActionTable actionTable = (RecordActionTable) recordTableScrollPane;
          Action refreshAction = actionTable.getRefreshAction();
          if (refreshAction != null) {
            refreshAction.actionPerformed(new ActionEvent(RecordTableComponent.this, 0, "refresh"));
          }
        }
      } // end templateOk
    }
    if (trace != null) trace.exit(RecordTableComponent.class);
  }

  /**
   * This method should take care of setting the TableModel to display specified folders data.
   */
  public abstract void initDataModel(Long folderId);

  /**
   * Initializes to display specified folder's data.  Will call initDataModel(folderId);
   */
  public void initData(Long folderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableComponent.class, "initData(Long folderId)");

    initDataModel(folderId);
    if (recordTableScrollPane instanceof RecordActionTable) {
      ((RecordActionTable) recordTableScrollPane).setEnabledActions();
      getActionTable().fireRecordSelectionChanged();
    }

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    FolderRecord fRec = cache.getFolderRecord(folderId);
    if (fRec != null) {
      jTopPanel.setVisible(!fRec.isCategoryType());
    }
    if (title == null) {
      changeTitle(folderId);
    }
    if (description == null) {
      changeDescription(folderId);
    }

    if (trace != null) trace.exit(RecordTableComponent.class);
  }

  /** 
   * Sets the title of the folder to reflect the description of the given folder.
   */
  private void changeTitle(Long folderId) {
    if (folderId == null) {
      jTitleLabel.setText(null);
      jTitleLabel.setIcon(null);
    }
    else {
      // remember the last folderId
      lastFolderId = folderId;

      // fetch folder title
      String titleName = "";
      Icon titleIcon = null;
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      FolderRecord fRec = cache.getFolderRecord(folderId);
      FolderShareRecord sRec = cache.getFolderShareRecordMy(folderId, true);
      if (fRec != null && sRec != null) {
        FolderPair fPair = new FolderPair(sRec, fRec);
        titleName = ListRenderer.getRenderedText(fPair, false, false, false);
        titleIcon = ListRenderer.getRenderedIcon(fPair);
      }

      jTitleLabel.setText(titleName);
      jTitleLabel.setIcon(titleIcon);
    }
  }
  /** 
   * Sets the description of the folder to reflect the description of the given folder.
   */
  private void changeDescription(Long folderId) {
    if (folderId == null) {
      jDescriptionLabel.setText(null);
    } else {
      // remember the last folderId
      lastFolderId = folderId;

      // fetch folder description
      String desc = "";
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      FolderShareRecord share = null;
      try {
        share = cache.getFolderShareRecordMy(folderId, true);
        if (share == null) {
          desc = "Folder has been REMOVED";
        } else {
          FolderRecord fRec = cache.getFolderRecord(folderId);
          if (fRec != null && fRec.isChatting() && fRec.numOfShares.shortValue() > 1) {
            // chatting folders use altered description or participant list if unmodified description
            String shareDesc = null;
            if (share != null)
              shareDesc = share.getFolderDesc();
            String originalDesc = com.CH_gui.lang.Lang.rb.getString("folderDesc_Chat_Log_common_beginning");
            if (shareDesc != null &&
                shareDesc.length() > 0 &&
                !shareDesc.startsWith(originalDesc)) {
              desc = shareDesc;
            } else {
              String[] notes = FolderTree.getOwnerAndChatNote(fRec);
              desc = "Participants: " + notes[0] + " " + notes[1];
              // correct the Chat Log title to ommit the participants...
              String name  = share.getFolderName();
              String defaultChatFolderName = com.CH_gui.lang.Lang.rb.getString("folderName_Chat_Log");
              if (name.startsWith(defaultChatFolderName))
                jTitleLabel.setText("Chat");
            }
          } else {
            desc = share.getFolderDesc();
          }
          // if still no description, make something generic
          if (desc == null || desc.length() == 0) {
            if (fRec != null) {
              if (fRec.numOfShares.shortValue() == 1) {
                desc = "Private " + fRec.getFolderType();
                if (fRec.isChatting() && fRec.numOfShares.shortValue() == 1)
                  desc += " archive.";
              } else {
                if (fRec.ownerUserId.equals(cache.getMyUserId()))
                  desc = "Your shared " + fRec.getFolderType();
                else {
                  Record owner = MsgPanelUtils.convertUserIdToFamiliarUser(fRec.ownerUserId, true, false, true);
                  if (owner == null) {
                    owner = new UserRecord();
                    ((UserRecord) owner).userId = fRec.ownerUserId;
                  }
                  desc = "Shared " + fRec.getFolderType() + ", owner " + ListRenderer.getRenderedText(owner);
                }
              }
            }
          }
        }
      } catch (Throwable t) {
        // if I'm not logged in, we can expect Exception, just note it in description...
        desc = "Folder could not be located.";
      }
      jDescriptionLabel.setText("<html>"+desc+"</html>"); // html gives us multi-line label capability
    }
    jDescriptionLabel.revalidate();
  }

  /**
   * Folder Share listener to update title and description labels when they change.
   */
  private class FolderShareListener implements FolderShareRecordListener {
    public void folderShareRecordUpdated(FolderShareRecordEvent event) {
      // to prevent deadlocks, run in seperate thread
      javax.swing.SwingUtilities.invokeLater(new GUIUpdater(event));
    }
  }
  
  private class GUIUpdater implements Runnable {
    private FolderShareRecordEvent event;
    public GUIUpdater(FolderShareRecordEvent event) {
      this.event = event;
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GUIUpdater.class, "run()");

      if (recordTableScrollPane != null) {
        FolderPair folderPair = recordTableScrollPane.getTableModel().getParentFolderPair();
        if (folderPair != null) {
          FolderShareRecord parentShare = folderPair.getFolderShareRecord();
          if (parentShare != null) {
            FolderShareRecord[] shareRecords = event.getFolderShareRecords();
            for (int i=0; i<shareRecords.length; i++) {
              if (parentShare.equals(shareRecords[i])) {
                if (event.getEventType() == RecordEvent.SET) {
                  changeTitle(shareRecords[i].folderId);
                } 
                changeDescription(shareRecords[i].folderId);
                break;
              }
            }
          }
        }
      }
      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(GUIUpdater.class);
    }
  }

  /***********************************
  *** F i l t e r    M e t h o d s ***
  ***********************************/
  public boolean isFilterable() {
    return ((RecordActionTable) RecordTableComponent.this.recordTableScrollPane).getFilterAction() != null;
  }
//  public Object[] getFilterProperties() {
//    return new Object[] { getFilterString(), isFilterEnabled() };
//  }
//  public void setFilterProperties(Object[] filterProps) {
//    if (filterProps != null) {
//      setFilterString((String) filterProps[0]);
//      setFilterEnabled((Boolean) filterProps[1]);
//    }
//  }
  public void setFilterFromComponent(RecordTableComponent tableComp) {
    setFilterString(tableComp.getFilterString());
    setFilterIncludeMsgBodies(tableComp.getFilterIncludeMsgBodies());
    setFilterEnabled(tableComp.isFilterEnabled());
    RecordFilter filter = tableComp.recordTableScrollPane.getTableModel().getFilterNarrowing();
    if (filter instanceof TextSearchFilter) {
      RecordTableModel tableModel = recordTableScrollPane.getTableModel();
      boolean includingMsgBodies = getFilterIncludeMsgBodies(); // take default from GUI
      if (tableModel instanceof MsgTableModel) {
        if (((MsgTableModel) tableModel).isModeMsgBody())
          includingMsgBodies = true;
      }
      ((TextSearchFilter) filter).setIncludingMsgBodies(includingMsgBodies);
    }
    recordTableScrollPane.getTableModel().setFilterNarrowing(filter);
    showTableOrDelayedTemplate();
  }
  public boolean getFilterIncludeMsgBodies() {
    return jFilterMsgBodyCheck.isSelected();
  }
  public String getFilterString() {
    return jFilterField.getText().trim();
  }
  public void setFilterIncludeMsgBodies(boolean includeMsgBodies) {
    jFilterMsgBodyCheck.setSelected(includeMsgBodies);
  }
  public void setFilterString(String match) {
    jFilterField.setText(match);
  }
  public Boolean isFilterEnabled() {
    return Boolean.valueOf(jFilterPanel.isVisible());
  }
  public void setFilterEnabled(Boolean enableFilter) {
    if (enableFilter != null)
      ((RecordActionTable) RecordTableComponent.this.recordTableScrollPane).getFilterAction().putValue(Actions.STATE_CHECK, enableFilter);
  }
  public void setFilterNarrowing(String filterStr, boolean includeMsgBodies) {
    if (filterStr != null && filterStr.trim().length() > 0)
      recordTableScrollPane.getTableModel().setFilterNarrowing(new TextSearchFilter(filterStr, includeMsgBodies, recordTableScrollPane.getTableModel()));
    else
      recordTableScrollPane.getTableModel().setFilterNarrowing(null);
  }


  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/

  public String getVisuals() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableComponent.class, "getVisuals()");

    String rc = null;

    if (!suppressVisualsSavable) {
      StringBuffer visuals = new StringBuffer();
      visuals.append("Dimension width ");
      Dimension dim = getSize();
      visuals.append(dim.width);
      visuals.append(" height ");
      visuals.append(dim.height);
      rc = visuals.toString();
    }

    if (trace != null) trace.exit(RecordTableComponent.class, rc);
    return rc;
  }

  public void restoreVisuals(String visuals) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableComponent.class, "restoreVisuals(String visuals)");
    if (trace != null) trace.args(visuals);

    if (!suppressVisualsSavable) {
      try {
        StringTokenizer st = new StringTokenizer(visuals);  
        st.nextToken();
        st.nextToken();
        int width = Integer.parseInt(st.nextToken());
        st.nextToken();
        int height = Integer.parseInt(st.nextToken());
        setPreferredSize(new Dimension(width, height));
      } catch (Throwable t) {
        if (trace != null) trace.exception(RecordTableComponent.class, 100, t);
      }
    }

    if (trace != null) trace.exit(RecordTableComponent.class);
  }
  public String getExtension() {
    return null;
  }
  public Integer getVisualsVersion() {
    return null;
  }
  public boolean isVisuallyTraversable() {
    return true;
  }

  /**
   * I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
   * Dispose the object and release resources to help in garbage collection.
  */
  public void disposeObj() {
    removeRecordListeners();
    if (recordTableScrollPane != null) {
      recordTableScrollPane.disposeObj();
    }
  }

}
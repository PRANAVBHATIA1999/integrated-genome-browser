/*
 * PrototypeOne.java
 *
 * Created on May 30, 2011, 10:18:22 AM
 */
package com.affymetrix.igb.view;

import com.affymetrix.igb.Application;
import java.awt.Color;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.affymetrix.igb.prefs.IPrefEditorComponent;
import com.affymetrix.igb.shared.TierGlyph;
import com.affymetrix.igb.tiers.TierLabelGlyph;
import com.affymetrix.igb.tiers.TrackStyle;
import com.affymetrix.genometryImpl.event.SeqMapRefreshed;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.genometryImpl.style.ITrackStyle;
import com.affymetrix.genoviz.swing.BooleanTableCellRenderer;
import com.affymetrix.genoviz.swing.ColorTableCellRenderer;
import com.affymetrix.igb.glyph.MapViewModeHolder;
import com.affymetrix.igb.tiers.TrackConstants;
import com.affymetrix.igb.tiers.TrackConstants.DIRECTION_TYPE;
import com.affymetrix.igb.view.load.LoadModeDataTableModel;
import com.affymetrix.igb.view.load.LoadModeTable;
import com.jidesoft.combobox.ColorComboBox;
import com.jidesoft.grid.ColorCellEditor;
import java.awt.Font;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author lorainelab
 */
public class TierPrefsView extends IPrefEditorComponent implements ListSelectionListener, WindowListener, SeqMapRefreshed {

	public static final long serialVersionUID = 1l;
	private static final String TRACK_NAME = "Display Name";
	private static final String FOREGROUND = "Foreground";
	private static final String BACKGROUND = "Background";
	private static final String TRACK_NAME_SIZE = "Track Name Size";
	private final static String[] col_headings = {
		TRACK_NAME,
		BACKGROUND, FOREGROUND,
		TRACK_NAME_SIZE //    GRAPH_TIER,
	};
	//subclass variables
	private static final int COL_TRACK_NAME = 0;
	private static final int COL_BACKGROUND = 1;
	private static final int COL_FOREGROUND = 2;
	private static final int COL_TRACK_NAME_SIZE = 3;
	private static final int COL_COLLAPSED = 4;
	private static final int COL_MAX_DEPTH = 5;
	private static final int COL_SHOW2TRACKS = 6;
	private static final int COL_LABEL_FIELD = 7;
	private static final int COL_CONNECTED = 8;
	private static final int COL_DIRECTION_TYPE = 9;
	private static final int COL_POS_STRAND_COLOR = 10;
	private static final int COL_NEG_STRAND_COLOR = 11;
	private static final int COL_VIEW_MODE = 12;
	private TierPrefsTableModel model;
	private ListSelectionModel lsm;
	private static final String PREF_AUTO_REFRESH = "Auto-Apply Track Customizer Changes";
	private static final boolean default_auto_refresh = true;
	private static final String AUTO_REFRESH = "Auto Refresh";
	private SeqMapView smv;
	private boolean initializationDetector; //Test to detect action events triggered by clicking a row in the table.
	private boolean settingValueFromTable;  //Test to prevent action events triggered by the setValueAt method from calling the method again.  This improves efficiency.
	private float trackNameSize;
	private int[] selectedRows;
	private List<TierLabelGlyph> selectedTiers;
	private int selectedRow;
	private TrackStyle selectedStyle;
	private List<TierGlyph> currentTiers;
	private List<TrackStyle> currentStyles;
	private TierGlyph tempTier;

	/** Creates new form PrototypeOne */
	public TierPrefsView() {

		super();
		this.setName("Tracks");
		this.setToolTipText("Set Track Properties");

		Application igb = Application.getSingleton();
		if (igb != null) {
			smv = igb.getMapView();
			smv.addToRefreshList(this);
		}

		initComponents();
		validate();
		displayNameTextField.setEnabled(false);
		viewModeCB.setEnabled(false);
		labelFieldComboBox.setEnabled(false);
		maxDepthTextField.setEnabled(false);
		connectedCheckBox.setEnabled(false);
		collapsedCheckBox.setEnabled(false);
		colorCheckBox.setEnabled(false);
		arrowCheckBox.setEnabled(false);
		possitiveColorComboBox.setEnabled(false);
		negativeColorComboBox.setEnabled(false);
		show2TracksCheckBox.setEnabled(false);
		displayNameTextField.setEnabled(false);
		bgColorComboBox.setEnabled(false);
		fgColorComboBox.setEnabled(false);
		trackNameSizeComboBox.setEnabled(false);
		applyToAllButton.setEnabled(false);
		labelFieldComboBox.setEnabled(false);
		maxDepthTextField.setEnabled(false);
	}

	public void setTier_label_glyphs(List<TierLabelGlyph> tier_label_glyphs) {
		selectedTiers = tier_label_glyphs;

		//set Selected Rows
		ITrackStyle style;
		table.removeRowSelectionInterval(0, table.getRowCount() - 1);
		for (TierLabelGlyph tlg : selectedTiers) {
			tempTier = (TierGlyph) tlg.getInfo();
			style = tempTier.getAnnotStyle();

			for (int i = 0; i < table.getRowCount(); i++) {
				if (model.getValueAt(i, 0).equals(style.getTrackName())) {
					table.addRowSelectionInterval(i, i);
				}
			}
		}
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        showStrandButtonGroup = new javax.swing.ButtonGroup();
        refreshButton = new javax.swing.JButton();
        selectTrackPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        propertiesPanel = new javax.swing.JPanel();
        displayNameLabel = new javax.swing.JLabel();
        displayNameTextField = new javax.swing.JTextField();
        bgLabel = new javax.swing.JLabel();
        bgColorComboBox = new com.jidesoft.combobox.ColorComboBox();
        trackNameSizeLabel = new javax.swing.JLabel();
        trackNameSizeComboBox = new javax.swing.JComboBox();
        labelFieldLabel = new javax.swing.JLabel();
        fgLabel = new javax.swing.JLabel();
        fgColorComboBox = new com.jidesoft.combobox.ColorComboBox();
        labelFieldComboBox = new javax.swing.JComboBox();
        maxDepthLabel = new javax.swing.JLabel();
        maxDepthTextField = new javax.swing.JTextField();
        show2TracksCheckBox = new javax.swing.JCheckBox();
        connectedCheckBox = new javax.swing.JCheckBox();
        collapsedCheckBox = new javax.swing.JCheckBox();
        applyToAllButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        autoRefreshCheckBox = new javax.swing.JCheckBox();
        showStrandPanel = new javax.swing.JPanel();
        possitiveLabel = new javax.swing.JLabel();
        negativeLabel = new javax.swing.JLabel();
        possitiveColorComboBox = new com.jidesoft.combobox.ColorComboBox();
        negativeColorComboBox = new com.jidesoft.combobox.ColorComboBox();
        colorCheckBox = new javax.swing.JCheckBox();
        arrowCheckBox = new javax.swing.JCheckBox();
        viewModelPanel = new javax.swing.JPanel();
        viewModeCB = new javax.swing.JComboBox();

        refreshButton.setText("Refresh");
        // Add a "refresh map" button, if there is an instance of IGB
        if (smv != null) {
            refreshButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    refreshSeqMapView();
                }
            });

            autoRefreshCheckBox = PreferenceUtils.createCheckBox(AUTO_REFRESH,
                PreferenceUtils.getTopNode(), PREF_AUTO_REFRESH, default_auto_refresh);
            autoRefreshCheckBox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    if (refreshButton != null) {
                        refreshButton.setEnabled(!autoRefreshCheckBox.isSelected());
                        if (autoRefreshCheckBox.isSelected()) {
                            refreshSeqMapView();
                        }
                    }
                }
            });
            refreshButton.setEnabled(!autoRefreshCheckBox.isSelected());
        }

        selectTrackPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Select Track"));

        model = new TierPrefsTableModel();
        model.addTableModelListener(new javax.swing.event.TableModelListener() {

            public void tableChanged(javax.swing.event.TableModelEvent e) {
                // do nothing.
            }
        });

        lsm = table.getSelectionModel();
        lsm.addListSelectionListener(this);
        lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        table.setRowSelectionAllowed(true);

        ColorCellEditor cellEditor = new ColorCellEditor() {
            private static final long serialVersionUID = 1L;

            @Override
            protected ColorComboBox createColorComboBox() {
                final ColorComboBox combobox = new ColorComboBox();
                combobox.setColorValueVisible(false);
                combobox.setCrossBackGroundStyle(false);
                combobox.setButtonVisible(false);
                combobox.setStretchToFit(true);
                return combobox;
            }
        };
        table.setDefaultRenderer(Color.class, new ColorTableCellRenderer());
        table.setDefaultEditor(Color.class, cellEditor);
        table.setDefaultRenderer(Boolean.class, new BooleanTableCellRenderer());
        table.setDefaultEditor(Float.class, new DefaultCellEditor(new JComboBox(TrackConstants.SUPPORTED_SIZE)));
        table.setDefaultEditor(TrackConstants.DIRECTION_TYPE.class, new DefaultCellEditor(new JComboBox(TrackConstants.DIRECTION_TYPE.values())));
        table.setModel(model);
        jScrollPane1.setViewportView(table);
        table.getColumnModel().getColumn(COL_FOREGROUND).setPreferredWidth(72);
        table.getColumnModel().getColumn(COL_FOREGROUND).setMinWidth(72);
        table.getColumnModel().getColumn(COL_FOREGROUND).setMaxWidth(72);
        table.getColumnModel().getColumn(COL_BACKGROUND).setPreferredWidth(72);
        table.getColumnModel().getColumn(COL_BACKGROUND).setMinWidth(72);
        table.getColumnModel().getColumn(COL_BACKGROUND).setMaxWidth(72);
        table.getColumnModel().getColumn(COL_TRACK_NAME_SIZE).setPreferredWidth(95);
        table.getColumnModel().getColumn(COL_TRACK_NAME_SIZE).setMinWidth(95);
        table.getColumnModel().getColumn(COL_TRACK_NAME_SIZE).setMaxWidth(95);

        Font f = new Font ("Serif", Font.BOLD, 12);
        table.getTableHeader().setFont(f);

        refreshList();

        org.jdesktop.layout.GroupLayout selectTrackPanelLayout = new org.jdesktop.layout.GroupLayout(selectTrackPanel);
        selectTrackPanel.setLayout(selectTrackPanelLayout);
        selectTrackPanelLayout.setHorizontalGroup(
            selectTrackPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(selectTrackPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)
                .addContainerGap())
        );
        selectTrackPanelLayout.setVerticalGroup(
            selectTrackPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(selectTrackPanelLayout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                .addContainerGap())
        );

        propertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Properties"));

        displayNameLabel.setText("Track Name:");

        displayNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayNameTextFieldActionPerformed(evt);
            }
        });

        bgLabel.setText("Background:");

        bgColorComboBox.setBackground(new java.awt.Color(255, 255, 255));
        bgColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        bgColorComboBox.setButtonVisible(false);
        bgColorComboBox.setColorValueVisible(false);
        bgColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        bgColorComboBox.setStretchToFit(true);
        bgColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bgColorComboBoxActionPerformed(evt);
            }
        });

        trackNameSizeLabel.setText("Name Size:");

        trackNameSizeComboBox.setEditable(true);
        trackNameSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(TrackConstants.SUPPORTED_SIZE));
        trackNameSizeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trackNameSizeComboBoxActionPerformed(evt);
            }
        });

        labelFieldLabel.setText("Label Field:");

        fgLabel.setText("Foreground:");

        fgColorComboBox.setBackground(new java.awt.Color(255, 255, 255));
        fgColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        fgColorComboBox.setButtonVisible(false);
        fgColorComboBox.setColorValueVisible(false);
        fgColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        fgColorComboBox.setStretchToFit(true);
        fgColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fgColorComboBoxActionPerformed(evt);
            }
        });

        labelFieldComboBox.setEditable(true);
        labelFieldComboBox.setModel(new javax.swing.DefaultComboBoxModel(TrackConstants.LABELFIELD));
        labelFieldComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelFieldComboBoxActionPerformed(evt);
            }
        });

        maxDepthLabel.setText("Max Depth:");

        maxDepthTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxDepthTextFieldActionPerformed(evt);
            }
        });

        show2TracksCheckBox.setText("Show 1 track (+/-)");
        show2TracksCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                show2TracksCheckBoxActionPerformed(evt);
            }
        });

        connectedCheckBox.setText("Connected");
        connectedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectedCheckBoxActionPerformed(evt);
            }
        });

        collapsedCheckBox.setText("Collapsed");
        collapsedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                collapsedCheckBoxActionPerformed(evt);
            }
        });

        applyToAllButton.setText("Apply To All Tracks");
        applyToAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyToAllButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout propertiesPanelLayout = new org.jdesktop.layout.GroupLayout(propertiesPanel);
        propertiesPanel.setLayout(propertiesPanelLayout);
        propertiesPanelLayout.setHorizontalGroup(
            propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(propertiesPanelLayout.createSequentialGroup()
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(propertiesPanelLayout.createSequentialGroup()
                        .add(9, 9, 9)
                        .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(bgLabel)
                            .add(trackNameSizeLabel)
                            .add(maxDepthLabel)
                            .add(displayNameLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(propertiesPanelLayout.createSequentialGroup()
                                .add(1, 1, 1)
                                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, fgLabel)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, propertiesPanelLayout.createSequentialGroup()
                                        .add(maxDepthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(28, 28, 28)
                                        .add(labelFieldLabel)))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(fgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(labelFieldComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 83, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(bgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(propertiesPanelLayout.createSequentialGroup()
                                .add(trackNameSizeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(applyToAllButton))
                            .add(displayNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 238, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 26, Short.MAX_VALUE))
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, propertiesPanelLayout.createSequentialGroup()
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(show2TracksCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(connectedCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(collapsedCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 104, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE))
                .addContainerGap())
        );
        propertiesPanelLayout.setVerticalGroup(
            propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(propertiesPanelLayout.createSequentialGroup()
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(displayNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(displayNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, propertiesPanelLayout.createSequentialGroup()
                        .add(4, 4, 4)
                        .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                            .add(bgLabel)
                            .add(bgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(10, 10, 10))
                    .add(propertiesPanelLayout.createSequentialGroup()
                        .add(3, 3, 3)
                        .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                            .add(fgLabel)
                            .add(fgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(10, 10, 10)))
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(trackNameSizeLabel)
                    .add(trackNameSizeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(applyToAllButton))
                .add(1, 1, 1)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(maxDepthLabel)
                    .add(labelFieldLabel)
                    .add(labelFieldComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(maxDepthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(show2TracksCheckBox)
                    .add(connectedCheckBox)
                    .add(collapsedCheckBox))
                .addContainerGap())
        );

        autoRefreshCheckBox.setText("Auto Refresh");

        showStrandPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Show Strand"));

        possitiveLabel.setText("+");

        negativeLabel.setText("-");

        possitiveColorComboBox.setBackground(new java.awt.Color(255, 255, 255));
        possitiveColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        possitiveColorComboBox.setButtonVisible(false);
        possitiveColorComboBox.setColorValueVisible(false);
        possitiveColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        possitiveColorComboBox.setStretchToFit(true);
        possitiveColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                possitiveColorComboBoxActionPerformed(evt);
            }
        });

        negativeColorComboBox.setBackground(new java.awt.Color(255, 255, 255));
        negativeColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        negativeColorComboBox.setButtonVisible(false);
        negativeColorComboBox.setColorValueVisible(false);
        negativeColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        negativeColorComboBox.setStretchToFit(true);
        negativeColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                negativeColorComboBoxActionPerformed(evt);
            }
        });

        colorCheckBox.setText("Color");
        colorCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorCheckBoxActionPerformed(evt);
            }
        });

        arrowCheckBox.setText("Arrow");
        arrowCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                arrowCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout showStrandPanelLayout = new org.jdesktop.layout.GroupLayout(showStrandPanel);
        showStrandPanel.setLayout(showStrandPanelLayout);
        showStrandPanelLayout.setHorizontalGroup(
            showStrandPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(showStrandPanelLayout.createSequentialGroup()
                .add(showStrandPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(colorCheckBox)
                    .add(arrowCheckBox)
                    .add(showStrandPanelLayout.createSequentialGroup()
                        .add(8, 8, 8)
                        .add(possitiveLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(possitiveColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(negativeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(negativeColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(8, Short.MAX_VALUE))
        );
        showStrandPanelLayout.setVerticalGroup(
            showStrandPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(showStrandPanelLayout.createSequentialGroup()
                .add(arrowCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(colorCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(showStrandPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(possitiveLabel)
                    .add(possitiveColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(negativeLabel)
                    .add(negativeColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(9, Short.MAX_VALUE))
        );

        viewModelPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("View Mode"));

        viewModeCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewModeCBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout viewModelPanelLayout = new org.jdesktop.layout.GroupLayout(viewModelPanel);
        viewModelPanel.setLayout(viewModelPanelLayout);
        viewModelPanelLayout.setHorizontalGroup(
            viewModelPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(viewModeCB, 0, 96, Short.MAX_VALUE)
        );
        viewModelPanelLayout.setVerticalGroup(
            viewModelPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(viewModeCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(selectTrackPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(propertiesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 375, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(showStrandPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(viewModelPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(autoRefreshCheckBox))
                            .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(selectTrackPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(layout.createSequentialGroup()
                        .add(showStrandPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 105, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(6, 6, 6)
                        .add(viewModelPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(autoRefreshCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(refreshButton))
                    .add(propertiesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void show2TracksCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_show2TracksCheckBoxActionPerformed
		if (!settingValueFromTable) {
			model.setValueAt(show2TracksCheckBox.isSelected(), selectedRows[0], COL_SHOW2TRACKS);
		}
}//GEN-LAST:event_show2TracksCheckBoxActionPerformed

    private void connectedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectedCheckBoxActionPerformed
		if (!settingValueFromTable) {
			model.setValueAt(connectedCheckBox.isSelected(), selectedRows[0], COL_CONNECTED);
		}
    }//GEN-LAST:event_connectedCheckBoxActionPerformed

	private void collapsedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_collapsedCheckBoxActionPerformed
		if (!settingValueFromTable) {
			model.setValueAt(collapsedCheckBox.isSelected(), selectedRows[0], COL_COLLAPSED);
		}
	}//GEN-LAST:event_collapsedCheckBoxActionPerformed

	private void displayNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayNameTextFieldActionPerformed
		if (!settingValueFromTable) {
			model.setValueAt(displayNameTextField.getText(), selectedRows[0], COL_TRACK_NAME);
		}
	}//GEN-LAST:event_displayNameTextFieldActionPerformed

	private void maxDepthTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxDepthTextFieldActionPerformed
		if (!settingValueFromTable) {
			model.setValueAt(maxDepthTextField.getText(), selectedRows[0], COL_MAX_DEPTH);
		}
	}//GEN-LAST:event_maxDepthTextFieldActionPerformed

	private void trackNameSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackNameSizeComboBoxActionPerformed
		if (!settingValueFromTable && !initializationDetector) {   // !initializationDetector condition is for the initialization when multiple rows are selected to prevent null exception
			trackNameSize = Float.parseFloat(trackNameSizeComboBox.getSelectedItem().toString());
			model.setValueAt(trackNameSize, selectedRows[0], COL_TRACK_NAME_SIZE);
		}
}//GEN-LAST:event_trackNameSizeComboBoxActionPerformed

	private void fgColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fgColorComboBoxActionPerformed
		if (!settingValueFromTable) {
			model.setValueAt(fgColorComboBox.getSelectedColor(), selectedRows[0], COL_FOREGROUND);
		}
}//GEN-LAST:event_fgColorComboBoxActionPerformed

	private void bgColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bgColorComboBoxActionPerformed
		if (!settingValueFromTable) {
			model.setValueAt(bgColorComboBox.getSelectedColor(), selectedRows[0], COL_BACKGROUND);
		}
}//GEN-LAST:event_bgColorComboBoxActionPerformed

	private void applyToAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyToAllButtonActionPerformed
		selectedRow = table.getSelectedRow();
		ITrackStyle style;
		for (int i = 0; i < table.getRowCount(); i++) {
			style = model.getStyles().get(i);
			if (!style.getTrackName().equalsIgnoreCase(
					TrackConstants.NAME_OF_COORDINATE_INSTANCE)) {
				model.setValueAt(model.getStyles().get(selectedRow).getBackground(), i, COL_BACKGROUND, true);
				model.setValueAt(model.getStyles().get(selectedRow).getTrackNameSize(), i, COL_TRACK_NAME_SIZE, true);
				model.setValueAt(model.getStyles().get(selectedRow).getForeground(), i, COL_FOREGROUND, false);
			}
			if (i == table.getRowCount() - 1) {
				applyChanges();
			}
		}
	}//GEN-LAST:event_applyToAllButtonActionPerformed

	private void labelFieldComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelFieldComboBoxActionPerformed
		if (!settingValueFromTable) {
			model.setValueAt(labelFieldComboBox.getSelectedItem(), selectedRows[0], COL_LABEL_FIELD);
		}
}//GEN-LAST:event_labelFieldComboBoxActionPerformed

	private void possitiveColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_possitiveColorComboBoxActionPerformed
		if (!settingValueFromTable) {
			model.setValueAt(possitiveColorComboBox.getSelectedColor(), selectedRows[0], COL_POS_STRAND_COLOR);
		}
}//GEN-LAST:event_possitiveColorComboBoxActionPerformed

	private void negativeColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_negativeColorComboBoxActionPerformed
		if (!settingValueFromTable) {
			model.setValueAt(negativeColorComboBox.getSelectedColor(), selectedRows[0], COL_NEG_STRAND_COLOR);
		}
}//GEN-LAST:event_negativeColorComboBoxActionPerformed

	private void colorCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorCheckBoxActionPerformed
		if (!settingValueFromTable) {
			if (colorCheckBox.isSelected()) {
				if (arrowCheckBox.isSelected()) {
					model.setValueAt(TrackConstants.DIRECTION_TYPE.BOTH, selectedRows[0], COL_DIRECTION_TYPE);
				} else {
					model.setValueAt(TrackConstants.DIRECTION_TYPE.COLOR, selectedRows[0], COL_DIRECTION_TYPE);
				}
			} else {
				if (arrowCheckBox.isSelected()) {
					model.setValueAt(TrackConstants.DIRECTION_TYPE.ARROW, selectedRows[0], COL_DIRECTION_TYPE);
				} else {
					model.setValueAt(TrackConstants.DIRECTION_TYPE.NONE, selectedRows[0], COL_DIRECTION_TYPE);
				}
			}
		}
}//GEN-LAST:event_colorCheckBoxActionPerformed

	private void arrowCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_arrowCheckBoxActionPerformed
		if (!settingValueFromTable) {
			if (colorCheckBox.isSelected()) {
				if (arrowCheckBox.isSelected()) {
					model.setValueAt(TrackConstants.DIRECTION_TYPE.BOTH, selectedRows[0], COL_DIRECTION_TYPE);
				} else {
					model.setValueAt(TrackConstants.DIRECTION_TYPE.COLOR, selectedRows[0], COL_DIRECTION_TYPE);
				}
			} else {
				if (arrowCheckBox.isSelected()) {
					model.setValueAt(TrackConstants.DIRECTION_TYPE.ARROW, selectedRows[0], COL_DIRECTION_TYPE);
				} else {
					model.setValueAt(TrackConstants.DIRECTION_TYPE.NONE, selectedRows[0], COL_DIRECTION_TYPE);
				}
			}
		}
}//GEN-LAST:event_arrowCheckBoxActionPerformed

	private void viewModeCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewModeCBActionPerformed
		if (!settingValueFromTable) {
			for (int i = 0; i < selectedRows.length; i++) {
				model.setValueAt(viewModeCB.getSelectedItem(), selectedRows[i], COL_VIEW_MODE);
			}
		}
	}//GEN-LAST:event_viewModeCBActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyToAllButton;
    private javax.swing.JCheckBox arrowCheckBox;
    private javax.swing.JCheckBox autoRefreshCheckBox;
    private com.jidesoft.combobox.ColorComboBox bgColorComboBox;
    private javax.swing.JLabel bgLabel;
    private javax.swing.JCheckBox collapsedCheckBox;
    private javax.swing.JCheckBox colorCheckBox;
    private javax.swing.JCheckBox connectedCheckBox;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JTextField displayNameTextField;
    private com.jidesoft.combobox.ColorComboBox fgColorComboBox;
    private javax.swing.JLabel fgLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JComboBox labelFieldComboBox;
    private javax.swing.JLabel labelFieldLabel;
    private javax.swing.JLabel maxDepthLabel;
    private javax.swing.JTextField maxDepthTextField;
    private com.jidesoft.combobox.ColorComboBox negativeColorComboBox;
    private javax.swing.JLabel negativeLabel;
    private com.jidesoft.combobox.ColorComboBox possitiveColorComboBox;
    private javax.swing.JLabel possitiveLabel;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JButton refreshButton;
    private javax.swing.JPanel selectTrackPanel;
    private javax.swing.JCheckBox show2TracksCheckBox;
    private javax.swing.ButtonGroup showStrandButtonGroup;
    private javax.swing.JPanel showStrandPanel;
    private javax.swing.JTable table;
    private javax.swing.JComboBox trackNameSizeComboBox;
    private javax.swing.JLabel trackNameSizeLabel;
    private javax.swing.JComboBox viewModeCB;
    private javax.swing.JPanel viewModelPanel;
    // End of variables declaration//GEN-END:variables

	/** Whether or not changes to the trackOptionsTable should automatically be
	 *  applied to the view.
	 */
	boolean autoApplyChanges() {
		return PreferenceUtils.getBooleanParam(PREF_AUTO_REFRESH,
				default_auto_refresh);
	}

	private void refreshSeqMapView() {
		if (smv != null) {
			smv.setAnnotatedSeq(smv.getAnnotatedSeq(), true, true, true);
		}
	}

	// implementation of IPrefEditorComponent
	public void refresh() {
		refreshList();
	}

	void refreshList() {
		if (currentStyles == null) {
			currentStyles = new ArrayList<TrackStyle>();
		}

		boolean isContained = true;

		if (smv != null) {
			currentTiers = smv.getSeqMap().getTiers();
			LinkedHashMap<TrackStyle, TrackStyle> stylemap = new LinkedHashMap<TrackStyle, TrackStyle>();
			Iterator<TierGlyph> titer = currentTiers.iterator();
			while (titer.hasNext()) {
				TierGlyph tier = titer.next();
				ITrackStyle style = tier.getAnnotStyle();

				if (!currentStyles.contains(style)) {
					isContained = false;
				}

				if ((style instanceof TrackStyle)
						&& (style.getShow())
						&& (tier.getChildCount() > 0)) {
					stylemap.put((TrackStyle) style, (TrackStyle) style);
				}
			}

			currentStyles.clear();
			currentStyles.addAll(stylemap.values());
		}
		ArrayList<TrackStyle> customizables = new ArrayList<TrackStyle>(currentStyles.size());
		for (int i = 0; i < currentStyles.size(); i++) {
			TrackStyle the_style = currentStyles.get(i);
			if (the_style.getCustomizable()) {
				// if graph tier style then only include if include_graph_styles toggle is set (app is _not_ IGB)
				if ((!the_style.isGraphTier())) {
					customizables.add(the_style);
				}
			}
		}

		if (!isContained) {
			model.setStyles(customizables);
			model.fireTableDataChanged();
			if (table.getRowCount() != 0) {
				table.setRowSelectionInterval(0, 0);
			}
		}
	}

	private void applyChanges() {
		refreshSeqMapView();
	}

	public void externalChange() {
		model.fireTableDataChanged();
		if (table.getRowCount() != 0) {
			table.setRowSelectionInterval(0, 0);
		}
	}

	/** Called when the user selects a row of the table.
	 * @param evt
	 */
	public void valueChanged(ListSelectionEvent evt) {
		displayNameTextField.setEnabled(true);
		viewModeCB.setEnabled(true);
		labelFieldComboBox.setEnabled(true);
		maxDepthTextField.setEnabled(true);
		connectedCheckBox.setEnabled(true);
		collapsedCheckBox.setEnabled(true);
		colorCheckBox.setEnabled(true);
		arrowCheckBox.setEnabled(true);
		possitiveColorComboBox.setEnabled(true);
		negativeColorComboBox.setEnabled(true);
		show2TracksCheckBox.setEnabled(true);
		displayNameTextField.setEnabled(true);
		bgColorComboBox.setEnabled(true);
		fgColorComboBox.setEnabled(true);
		trackNameSizeComboBox.setEnabled(true);
		applyToAllButton.setEnabled(true);
		labelFieldComboBox.setEnabled(true);
		maxDepthTextField.setEnabled(true);
		selectedRows = table.getSelectedRows();

		initializationDetector = true;

		if (table.getRowCount() == 0) {
			displayNameTextField.setEnabled(false);
			viewModeCB.setEnabled(false);
			labelFieldComboBox.setEnabled(false);
			maxDepthTextField.setEnabled(false);
			connectedCheckBox.setEnabled(false);
			collapsedCheckBox.setEnabled(false);
			colorCheckBox.setEnabled(false);
			arrowCheckBox.setEnabled(false);
			possitiveColorComboBox.setEnabled(false);
			negativeColorComboBox.setEnabled(false);
			show2TracksCheckBox.setEnabled(false);
			displayNameTextField.setEnabled(false);
			bgColorComboBox.setEnabled(false);
			fgColorComboBox.setEnabled(false);
			trackNameSizeComboBox.setEnabled(false);
			applyToAllButton.setEnabled(false);
			labelFieldComboBox.setEnabled(false);
			maxDepthTextField.setEnabled(false);
		}

		if (selectedRows.length > 1) {
			displayNameTextField.setEnabled(false);
			viewModeCB.setEnabled(false);
			bgColorComboBox.setSelectedColor(null);
			fgColorComboBox.setSelectedColor(null);
			trackNameSizeComboBox.setSelectedItem("");
			labelFieldComboBox.setSelectedIndex(-1);
			maxDepthTextField.setText(null);
			show2TracksCheckBox.setSelected(false);
			connectedCheckBox.setSelected(false);
			collapsedCheckBox.setSelected(false);
			colorCheckBox.setSelected(false);
			arrowCheckBox.setSelected(false);
			possitiveColorComboBox.setSelectedColor(null);
			negativeColorComboBox.setSelectedColor(null);
		}

		if (selectedRows.length == 1) {
			selectedStyle = model.getStyles().get(selectedRows[0]);

			if (selectedStyle.getTrackName().equalsIgnoreCase(TrackConstants.NAME_OF_COORDINATE_INSTANCE)) {
				displayNameTextField.setEnabled(false);
				viewModeCB.setEnabled(false);
				labelFieldComboBox.setEnabled(false);
				maxDepthTextField.setEnabled(false);
				connectedCheckBox.setEnabled(false);
				collapsedCheckBox.setEnabled(false);
				colorCheckBox.setEnabled(false);
				arrowCheckBox.setEnabled(false);
				possitiveColorComboBox.setEnabled(false);
				negativeColorComboBox.setEnabled(false);
				show2TracksCheckBox.setEnabled(false);
			}
			possitiveColorComboBox.setSelectedColor(selectedStyle.getForwardColor());
			negativeColorComboBox.setSelectedColor(selectedStyle.getReverseColor());
			String file_type = selectedStyle.getFileType();
			viewModeCB.removeAllItems();
			if (!selectedStyle.getTrackName().equalsIgnoreCase(TrackConstants.NAME_OF_COORDINATE_INSTANCE) && !selectedStyle.isGraphTier()) {
				viewModeCB.setModel(new javax.swing.DefaultComboBoxModel(MapViewModeHolder.getInstance().getAllViewModesFor(file_type)));
				String view_mode = selectedStyle.getViewMode();
				if (view_mode == null) {
					viewModeCB.setSelectedIndex(0);
				} else {
					viewModeCB.setSelectedItem(view_mode);
				}
			}
			int connected = selectedStyle.getGlyphDepth();
			boolean isConnected = true;
			if (connected == 1) {
				isConnected = false;
			}
			displayNameTextField.setText(selectedStyle.getTrackName());
			bgColorComboBox.setSelectedColor(selectedStyle.getBackground());
			fgColorComboBox.setSelectedColor(selectedStyle.getForeground());
			trackNameSizeComboBox.setSelectedItem(selectedStyle.getTrackNameSize());
			labelFieldComboBox.setSelectedItem(selectedStyle.getLabelField());
			maxDepthTextField.setText(String.valueOf(selectedStyle.getMaxDepth()));
			show2TracksCheckBox.setSelected(selectedStyle.getSeparate());
			connectedCheckBox.setSelected(isConnected);
			collapsedCheckBox.setSelected(selectedStyle.getCollapsed());

			switch (DIRECTION_TYPE.valueFor(selectedStyle.getDirectionType())) {
				case NONE:
					colorCheckBox.setSelected(false);
					arrowCheckBox.setSelected(false);
					break;
				case ARROW:
					colorCheckBox.setSelected(false);
					arrowCheckBox.setSelected(true);
					break;
				case COLOR:
					colorCheckBox.setSelected(true);
					arrowCheckBox.setSelected(false);
					break;
				case BOTH:
					colorCheckBox.setSelected(true);
					arrowCheckBox.setSelected(true);
					break;
				default:
					System.out.println("Unknown enum selected");
					break;
			}
		}

		initializationDetector = false;
	}

	public void destroy() {
		removeAll();
		if (lsm != null) {
			lsm.removeListSelectionListener(this);
		}
	}

	/**
	 *  Call this whenver this component is removed from the view, due to the
	 *  tab pane closing or the window closing.  It will decide whether it is
	 *  necessary to update the SeqMapView in response to changes in settings
	 *  in this panel.
	 */
	public void removedFromView() {
		// if autoApplyChanges(), then the changes were already applied,
		// otherwise apply changes as needed.
		if (!autoApplyChanges()) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					applyChanges();
				}
			});
		}
	}

	public void mapRefresh() {
		if (isVisible()) {
			refreshList();
		}
	}

	private void stopEditing() {
		if (table != null && table.getCellEditor() != null) {
			table.getCellEditor().stopCellEditing();
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (!visible) {
			stopEditing();
		}
	}

	public void windowClosed(WindowEvent e) {
		stopEditing();
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	class TierPrefsTableModel extends AbstractTableModel {

		List<TrackStyle> tier_styles;
		private Object tempObject;
		private int tempInt;

		TierPrefsTableModel() {
			this.tier_styles = Collections.<TrackStyle>emptyList();
		}

		public void setStyles(List<TrackStyle> tier_styles) {
			this.tier_styles = tier_styles;
		}

		public List<TrackStyle> getStyles() {
			return this.tier_styles;
		}

		// Allow editing most fields in normal rows, but don't allow editing some
		// fields in the "default" style row.
		@Override
		public boolean isCellEditable(int row, int column) {
			TrackStyle style;
			style = tier_styles.get(row);
			if (column == COL_TRACK_NAME) {
				return false;
			}
			return true;
		}

		@Override
		public Class<?> getColumnClass(int c) {
			tempObject = getValueAt(0, c);
			if (tempObject == null) {
				return Object.class;
			} else {
				return tempObject.getClass();
			}
		}

		public int getColumnCount() {
			return col_headings.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return col_headings[columnIndex];
		}

		public int getRowCount() {
			return tier_styles.size();
		}

		public Object getValueAt(int row, int column) {
			TrackStyle style;
			style = tier_styles.get(row);
			switch (column) {
				case COL_FOREGROUND:
					return style.getForeground();
				case COL_BACKGROUND:
					return style.getBackground();
				case COL_TRACK_NAME_SIZE:
					return style.getTrackNameSize();
				case COL_TRACK_NAME:
					return style.getTrackName();
				default:
					return null;
			}
		}

		@Override
		public void setValueAt(Object value, int row, int col) {

			for (int i = 0; i < selectedRows.length; i++) {
				if (i == selectedRows.length - 1) {
					setValueAt(value, selectedRows[i], col, true);
				} else {
					setValueAt(value, selectedRows[i], col, false);
				}
			}
		}

		public void setValueAt(Object value, int row, int col, boolean apply) {
			settingValueFromTable = true;
			if (value != null && !initializationDetector) {
				try {
					TrackStyle style;
					style = tier_styles.get(row);
					switch (col) {
						case COL_TRACK_NAME:
							//Test prevents a bug allowing Coordinate track to be renamed when multi-selecting
							if (!style.getTrackName().equalsIgnoreCase(TrackConstants.NAME_OF_COORDINATE_INSTANCE)) {
								style.setTrackName((String) value);
								displayNameTextField.setText((String) value);
							}
							break;
						case COL_FOREGROUND:
							style.setForeground((Color) value);
							fgColorComboBox.setSelectedColor((Color) value);
							break;
						case COL_BACKGROUND:
							style.setBackground((Color) value);
							bgColorComboBox.setSelectedColor((Color) value);
							break;
						case COL_TRACK_NAME_SIZE:
							style.setTrackNameSize((Float) value);
							trackNameSizeComboBox.setSelectedItem((Float) value);
							break;
						case COL_LABEL_FIELD:
							style.setLabelField((String) value);
							break;
						case COL_MAX_DEPTH: {
							tempInt = parseInteger(((String) value), 0, style.getMaxDepth());
							style.setMaxDepth(tempInt);
						}
						break;
						case COL_DIRECTION_TYPE:
							style.setDirectionType((TrackConstants.DIRECTION_TYPE) value);
							break;
						case COL_SHOW2TRACKS:
							style.setSeparate(((Boolean) value).booleanValue());
							break;
						case COL_CONNECTED:
							if (Boolean.TRUE.equals(value)) {
								style.setGlyphDepth(2);
							} else {
								style.setGlyphDepth(1);
							}
							break;
						case COL_COLLAPSED:
							style.setCollapsed(((Boolean) value).booleanValue());
							break;
						case COL_POS_STRAND_COLOR:
							style.setForwardColor((Color) value);
							possitiveColorComboBox.setSelectedColor((Color) value);
							break;
						case COL_NEG_STRAND_COLOR:
							style.setReverseColor((Color) value);
							negativeColorComboBox.setSelectedColor((Color) value);
							break;
						case COL_VIEW_MODE:
							style.setViewMode((String) value);
							break;
						default:
							System.out.println("Unknown column selected: " + col);
					}
					fireTableCellUpdated(row, col);
				} catch (Exception e) {
					// exceptions should not happen, but must be caught if they do
					System.out.println("Exception in TierPrefsView.setValueAt(): " + e);
				}

				if (autoApplyChanges() && apply) {
					if (col == COL_BACKGROUND || col == COL_TRACK_NAME_SIZE
							|| col == COL_TRACK_NAME || col == COL_COLLAPSED) {
						if (col == COL_TRACK_NAME || col == COL_COLLAPSED) {
							smv.getSeqMap().setTierStyles();
							smv.getSeqMap().repackTheTiers(true, true, false);
						}
						if (col == COL_TRACK_NAME || col == COL_BACKGROUND) {
							if (LoadModeTable.getModel() != null) {
								LoadModeTable.getModel().fireTableDataChanged();
							}
						}

						smv.getSeqMap().updateWidget();
					} else {
						applyChanges();
					}
				}

			}
			settingValueFromTable = false;
		}

		/** Parse an integer, using the given fallback if any exception occurrs.
		 *  @param s  The String to parse.
		 *  @param empty_string  the value to return if the input is an empty string.
		 *  @param fallback  the value to return if the input String is unparseable.
		 */
		int parseInteger(String s, int empty_string, int fallback) {
			try {
				if ("".equals(s.trim())) {
					return empty_string;
				} else {
					return Integer.parseInt(s);
				}
			} catch (Exception e) {
				//System.out.println("Exception: " + e);
				// don't report the error, use the fallback value
			}
			return fallback;
		}
	};
}

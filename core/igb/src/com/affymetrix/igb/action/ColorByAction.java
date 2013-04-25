package com.affymetrix.igb.action;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map.Entry;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import com.jidesoft.combobox.ColorComboBox;

import com.affymetrix.genometryImpl.color.ColorProvider;
import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.util.ThreadUtils;
import com.affymetrix.genoviz.swing.NumericFilter;
import static com.affymetrix.igb.IGBConstants.BUNDLE;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

/**
 *
 * @author hiralv
 */
public class ColorByAction extends SeqMapViewActionA {
	private static final long serialVersionUID = 1L;
	private static final ColorByAction ACTION = new ColorByAction("colorByAction");
		
	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static ColorByAction getAction() {
		return ACTION;
	}

	private ColorByAction(String transKey) {
		super(BUNDLE.getString(transKey) , "16x16/actions/blank_placeholder.png", null);
	}
	
	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		super.actionPerformed(e);
		ITrackStyleExtended style = getTierManager().getSelectedTiers().get(0).getAnnotStyle();
		ColorProvider cp = style.getColorProvider();
		
		ColorByDialog colorByDialog = new ColorByDialog();
		colorByDialog.setLocationRelativeTo(getSeqMapView());
		colorByDialog.setInitialValue(cp);
		cp = colorByDialog.showDialog();
		
		style.setColorProvider(cp);
		refreshMap(false, false);
		//TrackstylePropertyMonitor.getPropertyTracker().actionPerformed(e);
	}
	
	private class ColorByDialog extends JDialog {

		private ColorProvider initialValue, selectedCP;
		private JOptionPane optionPane;
		private JComboBox comboBox;
		private JPanel paramsPanel;
		
		/**
		 * Creates the reusable dialog.
		 */
		public ColorByDialog () {
			super((Frame)null, true);
			init();
		}

		private void init() throws SecurityException {
			JPanel pan = new JPanel();
			pan.setLayout(new BorderLayout());
	
			optionPane = new JOptionPane(pan, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
			
			setTitle("Color By");
			setResizable(true);
			setContentPane(optionPane);
			//setModal(false);
			setAlwaysOnTop(false);
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			
			comboBox = new JComboBox(ColorProvider.OPTIONS.keySet().toArray());
			comboBox.setSelectedItem(ColorProvider.getCPName(null));
			
			JPanel optionsBox = new JPanel();
			optionsBox.setLayout(new BoxLayout(optionsBox, BoxLayout.X_AXIS));
			optionsBox.add(new JLabel("Color By :  "));
			optionsBox.add(comboBox);
			
			paramsPanel = new JPanel();
			paramsPanel.setLayout(new BoxLayout(paramsPanel, BoxLayout.LINE_AXIS));
			
			pan.add(optionsBox, BorderLayout.CENTER);
			pan.add(paramsPanel, BorderLayout.PAGE_END);
		
			addListeners();
			pack();
		}

		private void addOptions(final ColorProvider cp, final JPanel pan) {
			pan.removeAll();
			pan.add(new JLabel("                "));
			if (cp != null && cp.getParameters() != null) {
				for (Entry<String, Class<?>> entry : cp.getParameters().entrySet()) {
					final String label = entry.getKey();
					Class<?> clazz = entry.getValue();
					JComponent component = null;

					if (Number.class.isAssignableFrom(clazz)) {
						final JTextField tf = new JTextField(6);
						((AbstractDocument)tf.getDocument()).setDocumentFilter(new NumericFilter.FloatNumericFilter());
						tf.setText(String.valueOf(cp.getParameterValue(label)));
						tf.getDocument().addDocumentListener(new DocumentListener(){
							
							public void insertUpdate(DocumentEvent e) { setParameter(); }

							public void removeUpdate(DocumentEvent e) { setParameter(); }

							public void changedUpdate(DocumentEvent e) { setParameter(); }
							
							private void setParameter(){
								cp.setParameter(label, tf.getText());
							}
						});
						
						tf.setMaximumSize(new java.awt.Dimension(60, 20));
						tf.setPreferredSize(new java.awt.Dimension(60, 20));
						tf.setMaximumSize(new java.awt.Dimension(60, 20));
						component = tf;
					} else if (Color.class.isAssignableFrom(clazz)) {
						final ColorComboBox colorComboBox = new ColorComboBox();
						colorComboBox.setSelectedColor((Color)cp.getParameterValue(label));
						colorComboBox.addItemListener(new ItemListener() {
							public void itemStateChanged(ItemEvent e) {
								cp.setParameter(label, e.getItem());
							}
						});
						colorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
						colorComboBox.setButtonVisible(false);
						colorComboBox.setColorValueVisible(false);
						colorComboBox.setMaximumSize(new java.awt.Dimension(20, 20));
						colorComboBox.setPreferredSize(new java.awt.Dimension(20, 20));
						colorComboBox.setMaximumSize(new java.awt.Dimension(20, 20));
						//colorComboBox.setStretchToFit(true);
						component = colorComboBox;
					}
					
					if (component != null) {
						pan.add(new JLabel(label));
						pan.add(component);
						pan.add(Box.createHorizontalStrut(30));
					}
				}
			}
		}
		
		private void initParamPanel(ColorProvider cp) {
			addOptions(cp, paramsPanel);
			ThreadUtils.runOnEventQueue(new Runnable() {
				public void run() {
					pack();
				}
			});
		}
		
		private void addListeners() {
			comboBox.addItemListener(new ItemListener() {
				
				public void itemStateChanged(ItemEvent e) {
					ColorProvider cp = ColorProvider.getCPInstance(ColorProvider.OPTIONS.get(e.getItem().toString()));
					selectedCP = cp;
					initParamPanel(cp);
				}
			});

			optionPane.addPropertyChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					Object value = optionPane.getValue();
					if(value != null){
						if(value.equals(JOptionPane.CANCEL_OPTION)){
							optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
							selectedCP = initialValue;
							dispose();
						}else if (value.equals(JOptionPane.OK_OPTION)){
							optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
							dispose();
						}
					}
				}
			});
		}

		public void setInitialValue(ColorProvider cp){
			initialValue = cp;
			selectedCP = cp;
			comboBox.setSelectedItem(ColorProvider.getCPName(cp == null ? null : cp.getClass()));
			initParamPanel(cp);
		}
		
		public ColorProvider showDialog() {
			setVisible(true);
			return selectedCP;
		}
	}
}

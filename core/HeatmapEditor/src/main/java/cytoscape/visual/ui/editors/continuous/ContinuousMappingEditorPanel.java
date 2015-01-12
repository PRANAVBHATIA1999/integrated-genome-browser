package cytoscape.visual.ui.editors.continuous;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXMultiThumbSlider;
import org.jdesktop.swingx.multislider.Thumb;

/**
 * Abstract class for all Continuous Mapping Editors.
 * 
 * @version 0.5
 * @since Cytoscape 2.5
 * @author kono
 */
public abstract class ContinuousMappingEditorPanel extends JDialog {

	private static final long serialVersionUID = -2558647616344119220L;

	/*
	 * Used by trackrenderers.
	 */
	protected static final String BELOW_VALUE_CHANGED = "BELOW_VALUE_CHANGED";
	protected static final String ABOVE_VALUE_CHANGED = "ABOVE_VALUE_CHANGED";
	
	// Variables declaration - do not modify
	protected javax.swing.JButton addButton;
	protected javax.swing.JButton colorButton;
	protected javax.swing.JButton deleteButton;
	protected javax.swing.JButton okButton;
	protected javax.swing.JButton cancelButton;
	protected javax.swing.JPanel iconPanel;
	private javax.swing.JPanel rangeSettingPanel;
	protected JXMultiThumbSlider slider;
	protected JSpinner valueSpinner;
	private JLabel valueLabel;
	private JLabel propertyLabel;
	protected JSpinner propertySpinner = null;
	protected JComponent propertyComponent;
	protected JXMultiThumbSlider rotaryEncoder;
	protected JButton minMaxButton;

	/*
	 * For Gradient panel only.
	 */
	protected BelowAndAbovePanel abovePanel;
	protected BelowAndAbovePanel belowPanel;
	
	protected float lastSpinnerNumber = 0;
	private Object value = JOptionPane.UNINITIALIZED_VALUE;
	private SpinnerChangeListener spinnerChangeListener;
	
	/** Creates new form ContinuousMapperEditorPanel */
	public ContinuousMappingEditorPanel() {
		this(null);
	}
	
	public ContinuousMappingEditorPanel(Window window) {
		super(window);
		initComponents();
		setModel();
		
		initRangeValues();
		setSpinner();
		// this.addWindowListener(new WindowAdapter() {
		// public void windowOpened(WindowEvent e) {
		// firePropertyChange(EDITOR_WINDOW_OPENED, null, type);
		// }
		//
		// public void windowClosing(WindowEvent e) {
		// firePropertyChange(EDITOR_WINDOW_CLOSED, this, type);
		// }
		// });
	}
	
	protected void setSpinner() {
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0.0d,
				Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, 0.01d);
		spinnerChangeListener = new SpinnerChangeListener();
		spinnerModel.addChangeListener(spinnerChangeListener);
		valueSpinner.setModel(spinnerModel);
	}

	@SuppressWarnings("unchecked")
	private void setModel(){
		slider.setModel(new MultiColorThumbModel());
	}
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */

	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">
	private void initComponents() {
		JPanel mainPanel = new JPanel();

		abovePanel = new BelowAndAbovePanel(MultiColorThumbModel.DEFAULT_ABOVE_COLOR, false, this);
		abovePanel.setName("abovePanel");
		belowPanel = new BelowAndAbovePanel(MultiColorThumbModel.DEFAULT_BELOW_COLOR, true, this);
		belowPanel.setName("belowPanel");

		abovePanel.setPreferredSize(new Dimension(16, 1));
		belowPanel.setPreferredSize(new Dimension(16, 1));

		rangeSettingPanel = new javax.swing.JPanel();
		addButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();

		// New in 2.6
		minMaxButton = new javax.swing.JButton();

		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();

		slider = new JXMultiThumbSlider<>();
		iconPanel = new YValueLegendPanel();

		valueSpinner = new JSpinner();
		valueSpinner.setEnabled(false);
		valueLabel = new JLabel("Attribute Value");
		valueLabel.setLabelFor(valueSpinner);

		// We use the colorButton for both discrete and color
		colorButton = new javax.swing.JButton("");
		colorButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		colorButton.setEnabled(false);
		colorButton.addActionListener(evt -> colorButtonActionPerformed());
		
		propertyComponent = colorButton;
		propertyLabel = new JLabel("Color ");
		propertyLabel.setLabelFor(propertyComponent);

		rotaryEncoder = new JXMultiThumbSlider();

		iconPanel.setPreferredSize(new Dimension(25, 1));

		mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5,
				5, 5));

		rangeSettingPanel.setBorder(javax.swing.BorderFactory
				.createTitledBorder(null, "Handle Settings",
						javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
						javax.swing.border.TitledBorder.DEFAULT_POSITION,
						new java.awt.Font("SansSerif", 1, 10),
						new java.awt.Color(0, 0, 0)));
		addButton.setText("Add");
		addButton.setPreferredSize(new Dimension(100, 10));
		addButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		addButton.addActionListener(this::addButtonActionPerformed);

		deleteButton.setText("Delete");
		deleteButton.setPreferredSize(new Dimension(100, 10));
		deleteButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		deleteButton.addActionListener(this::deleteButtonActionPerformed);

		// New in 2.6
		minMaxButton.setText("Set Range");
		minMaxButton.setPreferredSize(new Dimension(100, 10));
		minMaxButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		minMaxButton.addActionListener(this::minMaxButtonActionPerformed);

		cancelButton.setText("Cancel");
		cancelButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		cancelButton.addActionListener(evt -> optionSelectedEvent(JOptionPane.CANCEL_OPTION));

		okButton.setText("OK");
		// okButton.setPreferredSize(new Dimension(50, 30));
		okButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		okButton.addActionListener(evt -> optionSelectedEvent(JOptionPane.OK_OPTION));

		slider.setMaximumValue(100.0F);
		rotaryEncoder.setMaximumValue(100.0F);

		org.jdesktop.layout.GroupLayout sliderLayout = new org.jdesktop.layout.GroupLayout(
				slider);
		slider.setLayout(sliderLayout);
		sliderLayout.setHorizontalGroup(sliderLayout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(0, 486,
				Short.MAX_VALUE));
		sliderLayout.setVerticalGroup(sliderLayout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(0, 116,
				Short.MAX_VALUE));

		org.jdesktop.layout.GroupLayout jXMultiThumbSlider1Layout = new org.jdesktop.layout.GroupLayout(
				rotaryEncoder);
		rotaryEncoder.setLayout(jXMultiThumbSlider1Layout);
		jXMultiThumbSlider1Layout.setHorizontalGroup(jXMultiThumbSlider1Layout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(0, 84, Short.MAX_VALUE));
		jXMultiThumbSlider1Layout.setVerticalGroup(jXMultiThumbSlider1Layout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(0, 65, Short.MAX_VALUE));

		org.jdesktop.layout.GroupLayout rangeSettingPanelLayout = new org.jdesktop.layout.GroupLayout(
				rangeSettingPanel);
		rangeSettingPanel.setLayout(rangeSettingPanelLayout);
		rangeSettingPanelLayout
				.setHorizontalGroup(rangeSettingPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(rangeSettingPanelLayout
								.createSequentialGroup()
								.addContainerGap()
								.add(valueLabel,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										org.jdesktop.layout.LayoutStyle.RELATED)
								.add(valueSpinner,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										100,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										org.jdesktop.layout.LayoutStyle.RELATED,
										118, Short.MAX_VALUE)
								.add(minMaxButton,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										100,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										org.jdesktop.layout.LayoutStyle.RELATED)
								.add(addButton,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										100,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										org.jdesktop.layout.LayoutStyle.RELATED)
								.add(deleteButton).add(10, 10, 10))
						.add(rangeSettingPanelLayout
								.createSequentialGroup()
								.addContainerGap()
								.add(propertyLabel,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										org.jdesktop.layout.LayoutStyle.RELATED)
								.add(propertyComponent,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										100,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

		rangeSettingPanelLayout
				.setVerticalGroup(rangeSettingPanelLayout
						.createSequentialGroup()
						.add(rangeSettingPanelLayout
								.createParallelGroup(
										org.jdesktop.layout.GroupLayout.BASELINE)
								.add(valueLabel)
								.add(valueSpinner,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
								.add(minMaxButton).add(deleteButton)
								.add(addButton))
						.add(rangeSettingPanelLayout
								.createParallelGroup(
										org.jdesktop.layout.GroupLayout.BASELINE)
								.add(propertyLabel)
								.add(propertyComponent,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(
				mainPanel);
		mainPanel.setLayout(layout);

		layout.setHorizontalGroup(layout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(layout
						.createSequentialGroup()
						.addContainerGap()
						.addPreferredGap(
								org.jdesktop.layout.LayoutStyle.RELATED, 118,
								Short.MAX_VALUE)
						.add(cancelButton,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
								100,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(
								org.jdesktop.layout.LayoutStyle.RELATED)
						.add(okButton,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
								100,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.add(10, 10, 10))
				.add(rangeSettingPanel,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
						Short.MAX_VALUE)
				.add(layout
						.createSequentialGroup()
						.add(iconPanel,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.add(belowPanel,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.add(slider,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								243, Short.MAX_VALUE)
						.add(abovePanel,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

		layout.setVerticalGroup(layout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(layout
						.createSequentialGroup()
						.add(layout
								.createParallelGroup(
										org.jdesktop.layout.GroupLayout.LEADING)
								.add(org.jdesktop.layout.GroupLayout.TRAILING,
										slider,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										145, Short.MAX_VALUE)
								.add(org.jdesktop.layout.GroupLayout.TRAILING,
										iconPanel,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.add(org.jdesktop.layout.GroupLayout.TRAILING,
										belowPanel,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.add(org.jdesktop.layout.GroupLayout.TRAILING,
										abovePanel,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE))
						.addPreferredGap(
								org.jdesktop.layout.LayoutStyle.RELATED)
						.add(rangeSettingPanel,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.add(layout
								.createParallelGroup(
										org.jdesktop.layout.GroupLayout.LEADING)
								.add(cancelButton,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
								.add(okButton,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))));

		// add the main panel to the dialog.
		this.getContentPane().add(mainPanel);
		this.pack();
	} // </editor-fold>

	public Object showDialog(){
		setVisible(true);
		return getValue();
	}
	
	private void optionSelectedEvent(Object value){
		Object oldValue = getValue();
		setValue(value);
		setVisible(false);
		dispose();
		firePropertyChange(JOptionPane.VALUE_PROPERTY, oldValue, value);
	}
	
	private void setValue(Object value){
		this.value = value;
	}
	
	private Object getValue(){
		return value;
	}
	
	protected void minMaxButtonActionPerformed(ActionEvent evt) {
		MultiColorThumbModel model = (MultiColorThumbModel)slider.getModel();
		final Float[] newVal = MinMaxDialog.getMinMax(null, model.getVirtualMinimum(), model.getVirtualMaximum(),
				"min-max-attribute");

		if (newVal == null) {
			return;
		}
		
		model.setVirtualMinimum(newVal[0]);
		model.setVirtualMaximum(newVal[1]);
		
		this.repaint();
	}

	abstract protected void addButtonActionPerformed(
			java.awt.event.ActionEvent evt);

	protected void deleteButtonActionPerformed(ActionEvent evt) {
		if (slider.getSelectedIndex()  >= 0) {
			slider.getModel().removeThumb(slider.getSelectedIndex());
			//mapping.removePoint(selectedIndex);
			//mapping.fireStateChanged();

			repaint();
		}
	}

	protected void colorButtonActionPerformed() {
		final Color newColor = CyColorChooser.showDialog(slider,
				"Choose new color...",
				Color.white);
		if (newColor != null) {
			//Set new color
			setColor(newColor);
		}
	}
		
	@SuppressWarnings("unchecked")
	private void setColor(final Color newColor) {
		final int selectedIndex = slider.getSelectedIndex();
		slider.getModel().getThumbAt(selectedIndex).setObject(newColor);
		setButtonColor(newColor);
		slider.repaint();
	}
	
	private void initRangeValues() {

	}

	protected void setSidePanelIconColor(Color below, Color above) {
		this.abovePanel.setColor(above);
		this.belowPanel.setColor(below);
		repaint();
	}

	@SuppressWarnings("unchecked")
	protected int getSelectedPoint(int selectedIndex) {
		final List<Thumb<?>> thumbs = slider.getModel().getSortedThumbs();
		Thumb<?> selected = slider.getModel().getThumbAt(selectedIndex);
		for (int i = 0; i < thumbs.size(); i++) {
			if (thumbs.get(i) == selected) {
				return i;
			}
		}

		return -1;
	}

	protected void selectThumbAtPosition(float position) {
		int selectedIndex = getThumbIndexAtPosition(position);
		if (selectedIndex != -1) {
			enableSpinner(selectedIndex);
		}
	}

	@SuppressWarnings("unchecked")
	protected int getThumbIndexAtPosition(float position) {
		final List<Thumb<?>> thumbs = slider.getModel().getSortedThumbs();
		for (int i = 0; i < thumbs.size(); i++) {
			if (slider.getModel().getThumbAt(i).getPosition() == position) {
				return i;
			}
		}
		return -1;
	}

	protected void setButtonColor(Color newColor) {
		final int iconWidth = 10;
		final int iconHeight = 10;
		final BufferedImage bi = new BufferedImage(iconWidth, iconHeight,
				BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2 = bi.createGraphics();

		/*
		 * Fill background
		 */
		g2.setColor(newColor);
		g2.fillRect(0, 0, iconWidth, iconHeight);

		Icon colorIcon = new ImageIcon(bi);
		colorButton.setIcon(colorIcon);
		colorButton.setIconTextGap(6);

	}

	protected void enableSpinner(int selectedIndex) {
		valueSpinner.getModel().removeChangeListener(spinnerChangeListener);

		Thumb<?> selectedThumb = slider.getModel().getThumbAt(selectedIndex);
		float newVal = ((MultiColorThumbModel)slider.getModel()).getVirtualValue(selectedThumb.getPosition());
		valueSpinner.setValue(newVal);
		setButtonColor((Color) selectedThumb.getObject());
		
		colorButton.setEnabled(true);
		valueSpinner.setEnabled(true);
		valueSpinner.getModel().addChangeListener(spinnerChangeListener);
	}

	protected void disableSpinner() {
		valueSpinner.setEnabled(false);
		valueSpinner.setValue(0);
	}

	// End of variables declaration
	protected class ThumbMouseListener extends MouseAdapter {
		
		@Override
		public void mouseReleased(MouseEvent e) {
			int selectedIndex = slider.getSelectedIndex();
	
			if ((0 <= selectedIndex) && (slider.getModel().getThumbCount() > 0)) {
				enableSpinner(selectedIndex);
			} else {
				disableSpinner();
			}
		}
	}

	/**
	 * Watching spinner
	 * 
	 * @author kono
	 * 
	 */
	class SpinnerChangeListener implements ChangeListener {
	
		public void stateChanged(ChangeEvent e) {

			MultiColorThumbModel model = ((MultiColorThumbModel)slider.getModel());
			SpinnerNumberModel spinnerModel = (SpinnerNumberModel)e.getSource();
			final Number newVal = spinnerModel.getNumber();
			final int selectedIndex = slider.getSelectedIndex();

			if ((0 <= selectedIndex)
					&& (slider.getModel().getThumbCount() >= 1)) {

				if ((newVal.doubleValue() < model.getVirtualMinimum())
						|| (newVal.doubleValue() > model.getVirtualMaximum())) {

					if ((lastSpinnerNumber > model.getVirtualMinimum()) && (lastSpinnerNumber < model.getVirtualMaximum())) {
						spinnerModel.setValue(lastSpinnerNumber);
					} else {
						spinnerModel.setValue(0);
					}

					return;
				}

				final float newPosition = ((MultiColorThumbModel)slider.getModel()).getPosition(newVal.floatValue());
				slider.getModel().getThumbAt(selectedIndex).setPosition(newPosition);

				JComponent selectedThumb = slider.getSelectedThumb();
				selectedThumb.setLocation((int) ((slider.getSize().width - 12) * model.getFraction(newPosition)), 0);

				selectedThumb.repaint();
				slider.getParent().repaint();
				slider.repaint();
				lastSpinnerNumber = newVal.floatValue();
			}
		}
	}

}

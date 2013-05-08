package cytoscape.visual.ui.editors.continuous;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Gradient editor.
 *
 * @version 0.7
 * @since Cytoscpae 2.5
 * @author kono
 */
public class GradientEditorPanel extends ContinuousMappingEditorPanel
		implements PropertyChangeListener {

	private static final long serialVersionUID = -7645303507318540305L;
	// For presets
	private static final Color DEF_LOWER_COLOR = Color.BLACK;
	private static final Color DEF_UPPER_COLOR = Color.WHITE;

	/**
	 * Creates a new GradientEditorPanel object.
	 *
	 * @param type DOCUMENT ME!
	 */
	public GradientEditorPanel() {
		super();
		iconPanel.setVisible(false);
		initSlider();

		belowPanel.addPropertyChangeListener(this);
		abovePanel.addPropertyChangeListener(this);
		//if(mapping != null && mapping.getPointCount() == 0)
		addButtonActionPerformed(null);

		colorButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				getAndSetColor();
			}
		});
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param width DOCUMENT ME!
	 * @param height DOCUMENT ME!
	 * @param title DOCUMENT ME!
	 * @param type DOCUMENT ME!
	 */
	public static Object showDialog(final int width, final int height, final String title) {
		editor = new GradientEditorPanel();

		final Dimension size = new Dimension(width, height);
		editor.slider.setPreferredSize(size);
		editor.setPreferredSize(size);

		editor.setTitle(title);
		editor.setAlwaysOnTop(true);
//		editor.setLocationRelativeTo(Cytoscape.getDesktop());
		editor.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		editor.setVisible(true);

		return editor;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param width DOCUMENT ME!
	 * @param height DOCUMENT ME!
	 * @param type DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static ImageIcon getLegend(final int width, final int height) {
		editor = new GradientEditorPanel();
		CyGradientTrackRenderer rend = (CyGradientTrackRenderer) editor.slider.getTrackRenderer();
		rend.getRendererComponent(editor.slider);
		return rend.getLegend(width, height);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static ImageIcon getIcon(final int iconWidth, final int iconHeight) {
		editor = new GradientEditorPanel();
		CyGradientTrackRenderer rend = (CyGradientTrackRenderer) editor.slider.getTrackRenderer();
		rend.getRendererComponent(editor.slider);
		return rend.getTrackGraphicIcon(iconWidth, iconHeight);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void addButtonActionPerformed(ActionEvent evt) {

		if (slider.getModel().getThumbCount() == 0) {
			slider.getModel().addThumb(10f, DEF_LOWER_COLOR);
			slider.getModel().addThumb(90f, DEF_UPPER_COLOR);
			slider.repaint();
			repaint();
			return;
		}

		// Add a new white thumb in the min.
		// slider.getModel().addThumb(100f, Color.white);
		// Add a new white thumb near the middle
		slider.getModel().addThumb(51f, Color.white);

		// Make this slider the selected one
		selectThumbAtPosition(51f);

		updateMap();
		slider.repaint();
		repaint();
	}

	@Override
	protected void deleteButtonActionPerformed(ActionEvent evt) {
		if (slider.getSelectedIndex()  >= 0) {
			slider.getModel().removeThumb(slider.getSelectedIndex());
			//mapping.removePoint(selectedIndex);
			updateMap();
			//mapping.fireStateChanged();

			repaint();
		}
	}

	@SuppressWarnings("unchecked")
	private void setColor(final Color newColor) {
		final int selectedIndex = slider.getSelectedIndex();
		int selected = getSelectedPoint(selectedIndex);
		
		slider.getModel().getThumbAt(selectedIndex).setObject(newColor);
		setButtonColor(newColor);
		slider.repaint();
	}

	public void getAndSetColor() {
		final Color newColor = CyColorChooser.showDialog(slider,
				"Choose new color...",
				Color.white);
		if (newColor != null) {
			//Set new color
			setColor(newColor);
		}
	}

	/**
	 * DOCUMENT ME!
	 */
	private void initSlider() {
		slider.updateUI();
		slider.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (!SwingUtilities.isRightMouseButton(e)) {
					if (slider.getSelectedIndex() >= 0) {
						if (e.getClickCount() == 2) {
							getAndSetColor();
						}
					}
				}
			}
		});

		if (true) {
			int no_of_points = 0;
			if (no_of_points != 0) {
				//below = (Color) allPoints.get(0).getRange().lesserValue;
				//above = (Color) allPoints.get(allPoints.size() - 1).getRange().greaterValue;
			} else {
				below = Color.black;
				above = Color.white;
			}

			setSidePanelIconColor((Color) below, (Color) above);
		}

		TriangleThumbRenderer thumbRend = new TriangleThumbRenderer();

		CyGradientTrackRenderer gRend = new CyGradientTrackRenderer();
		//updateBelowAndAbove();
		slider.setThumbRenderer(thumbRend);
		slider.setTrackRenderer(gRend);
		slider.addMouseListener(new ThumbMouseListener());

		/*
		 * Set tooltip for the slider.
		 */
		slider.setToolTipText("Double-click handles to edit boundary colors.");
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals(BelowAndAbovePanel.COLOR_CHANGED)) {
			String sourceName = ((BelowAndAbovePanel) e.getSource()).getName();

			if (sourceName.equals("abovePanel")) {
				this.above = e.getNewValue();
			} else {
				this.below = e.getNewValue();
			}

			final CyGradientTrackRenderer gRend = new CyGradientTrackRenderer();
			slider.setTrackRenderer(gRend);

			repaint();
		}
	}
}

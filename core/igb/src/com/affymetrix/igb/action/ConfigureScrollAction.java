package com.affymetrix.igb.action;

import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.genometryImpl.event.SeqSelectionEvent;
import com.affymetrix.genometryImpl.event.SeqSelectionListener;
import com.affymetrix.genoviz.swing.recordplayback.JRPTextField;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.view.SeqMapView;
import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 *
 * @author sgblanch
 * @version $Id: AutoScrollAction.java 11333 2012-05-01 17:54:56Z anuj4159 $
 */
public class ConfigureScrollAction extends GenericAction implements SeqSelectionListener {
	private static final long serialVersionUID = 1l;
	/*
	 *  units to scroll are either in pixels or bases
	 */
	private int as_bases_per_pix = 75;
	private int as_pix_to_scroll = 4;
	private int as_time_interval = 20;
	private int as_start_pos = 0;
	private int as_end_pos;
	

	private static final ConfigureScrollAction ACTION = new ConfigureScrollAction();

	private ConfigureScrollAction() {
		super(BUNDLE.getString("configureAutoScroll"), null,
			"toolbarButtonGraphics/media/Movie16.gif",
			"toolbarButtonGraphics/media/Movie24.gif", // tool bar eligible
			KeyEvent.VK_A, null, true);
		this.ordinal = -4009000;
	}
	
	protected ConfigureScrollAction(String text, String small_icon, String large_icon){
		super(text, small_icon, large_icon);
	}
	
	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
		GenometryModel model = GenometryModel.getGenometryModel();
		model.addSeqSelectionListener(ACTION);
		ACTION.seqSelectionChanged(new SeqSelectionEvent(
				ACTION, Collections.<BioSeq>singletonList(model.getSelectedSeq())));
	}
	
	public static ConfigureScrollAction getAction() { return ACTION; }

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		this.configure(IGB.getSingleton().getMapView());
	}

	@Override
	public void seqSelectionChanged(SeqSelectionEvent evt) {
		this.setEnabled(evt.getSelectedSeq() != null);
		if(evt.getSelectedSeq() != null && get_start_pos() == 0 && get_end_pos() == 0){
			as_start_pos = evt.getSelectedSeq().getMin();
			as_end_pos = evt.getSelectedSeq().getMax();
		}
	}

	private void configure(final SeqMapView seqMapView) {
		if (seqMapView.getViewSeq() == null) {
			return;
		}
		
		// turn OFF autoscroll while configuring
		AutoScrollAction.getAction().stop();
		
		JPanel pan = new JPanel();

		Rectangle2D.Double cbox = seqMapView.getSeqMap().getViewBounds();
		int bases_in_view = (int) cbox.width;
		as_start_pos = (int) cbox.x;
		as_end_pos = seqMapView.getViewSeq().getLength();
		int pixel_width = seqMapView.getSeqMap().getView().getPixelBox().width;
		as_bases_per_pix = bases_in_view / pixel_width;

		// as_bases_per_pix *should* be a float, or else should simply
		// use the current resolution without asking the user,
		// but since it is an integer, we have to set the minimum value as 1
		if (get_bases_per_pix() < 1) {
			as_bases_per_pix = 1;
		}

		final JRPTextField bases_per_pixTF = new JRPTextField("AutoScrollAction_bases_per_pix", "" + get_bases_per_pix());
		final JRPTextField pix_to_scrollTF = new JRPTextField("AutoScrollAction_pix_to_scroll", "" + get_pix_to_scroll());
		final JRPTextField time_intervalTF = new JRPTextField("AutoScrollAction_time_interval", "" + get_time_interval());
		final JRPTextField start_posTF = new JRPTextField("AutoScrollAction_start_pos", "" + get_start_pos());
		final JRPTextField end_posTF = new JRPTextField("AutoScrollAction_end_pos", "" + get_end_pos());

		float bases_per_minute = (float) // 1000 ==> ms/s , 60 ==> s/minute, as_time_interval ==> ms/scroll
				(1.0 * get_bases_per_pix() * get_pix_to_scroll() * 1000 * 60 / get_time_interval());
		bases_per_minute = Math.abs(bases_per_minute);
		float minutes_per_seq = seqMapView.getViewSeq().getLength() / bases_per_minute;
		final JLabel bases_per_minuteL = new JLabel("" + (bases_per_minute / 1000000));
		final JLabel minutes_per_seqL = new JLabel("" + (minutes_per_seq));

		pan.setLayout(new GridLayout(7, 2));
		pan.add(new JLabel("Resolution (bases per pixel)"));
		pan.add(bases_per_pixTF);
		pan.add(new JLabel("Scroll increment (pixels)"));
		pan.add(pix_to_scrollTF);
		pan.add(new JLabel("Starting base position"));
		pan.add(start_posTF);
		pan.add(new JLabel("Ending base position"));
		pan.add(end_posTF);
		pan.add(new JLabel("Time interval (milliseconds)"));
		pan.add(time_intervalTF);
		pan.add(new JLabel("Megabases per minute:  "));
		pan.add(bases_per_minuteL);
		pan.add(new JLabel("Total minutes for seq:  "));
		pan.add(minutes_per_seqL);

		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				as_bases_per_pix = normalizeTF(bases_per_pixTF, get_bases_per_pix(), 1, Integer.MAX_VALUE);
				as_pix_to_scroll = normalizeTF(pix_to_scrollTF, get_pix_to_scroll(), -1000, 1000);
				as_time_interval = normalizeTF(time_intervalTF, get_time_interval(), 1, 1000);
				as_end_pos = normalizeTF(end_posTF, get_end_pos(), 1, seqMapView.getViewSeq().getLength());
				as_start_pos = normalizeTF(start_posTF, get_start_pos(), 0, get_end_pos());

				float bases_per_minute = (float) // 1000 ==> ms/s , 60 ==> s/minute, as_time_interval ==> ms/scroll
						(1.0 * get_bases_per_pix() * get_pix_to_scroll() * 1000 * 60 / get_time_interval());
				bases_per_minute = Math.abs(bases_per_minute);
				float minutes_per_seq = (get_end_pos() - get_start_pos()) / bases_per_minute;
				bases_per_minuteL.setText("" + (bases_per_minute / 1000000));
				minutes_per_seqL.setText("" + (minutes_per_seq));
			}
		};

		bases_per_pixTF.addActionListener(al);
		pix_to_scrollTF.addActionListener(al);
		time_intervalTF.addActionListener(al);
		start_posTF.addActionListener(al);
		end_posTF.addActionListener(al);

		int val = JOptionPane.showOptionDialog(seqMapView, pan, "AutoScroll Parameters",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null, null, null);
		if (val == JOptionPane.OK_OPTION) {
			as_bases_per_pix = normalizeTF(bases_per_pixTF, get_bases_per_pix(), 1, Integer.MAX_VALUE);
			as_pix_to_scroll = normalizeTF(pix_to_scrollTF, get_pix_to_scroll(), -1000, 1000);
			as_time_interval = normalizeTF(time_intervalTF, get_time_interval(), 1, 1000);
		}
	}

	// Normalize a text field so that it holds an integer, with a fallback value
	// if there is a problem, and a minimum and maximum
	private static int normalizeTF(JRPTextField tf, int fallback, int min, int max) {
		assert min <= fallback && fallback <= max;
		int result;
		try {
			result = Integer.parseInt(tf.getText());
		} catch (NumberFormatException nfe) {
			Toolkit.getDefaultToolkit().beep();
			result = fallback;
		}
		if (result < min) {
			result = min;
		} else if (result > max) {
			result = max;
		}
		tf.setText(Integer.toString(result));
		return result;
	}	

	public int get_bases_per_pix() {
		return as_bases_per_pix;
	}

	public int get_pix_to_scroll() {
		return as_pix_to_scroll;
	}

	public int get_time_interval() {
		return as_time_interval;
	}

	public int get_start_pos() {
		return as_start_pos;
	}

	public int get_end_pos() {
		return as_end_pos;
	}
}

package com.affymetrix.igb.tiers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import javax.swing.Timer;

import com.affymetrix.genoviz.widget.NeoAbstractWidget;
import com.affymetrix.genoviz.widget.NeoMap;

/**
 *
 * @author hiralv
 */
public class AutoScroll {
	private int bases_per_pix = 75;
	private int pix_to_scroll = 4;
	private int time_interval = 20;
	private int start_pos = 0;
	private int end_pos;

	private ActionListener map_auto_scroller = null;
	private Timer swing_timer = null;
		
	public void stop() {
		if(swing_timer != null){
			swing_timer.stop();
			swing_timer = null;
		}
		map_auto_scroller = null;
	}
	
	public void start(final NeoMap map) {
		stop();
		
		final boolean cycle = true;
		final double pix_per_coord = 1.0 / bases_per_pix;
		final double coords_to_scroll = pix_to_scroll / pix_per_coord;
		
		map.zoom(NeoAbstractWidget.X, pix_per_coord);
		map.scroll(NeoAbstractWidget.X, start_pos);
		
		map_auto_scroller = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				Rectangle2D.Double vbox = map.getViewBounds();
				int scrollpos = (int) (vbox.x + coords_to_scroll);
				if ((scrollpos + vbox.width) > end_pos) {
					if (cycle) {
						map.scroll(NeoAbstractWidget.X, start_pos);
						map.updateWidget();
					} else {
						stop();
					}
				} else {
					map.scroll(NeoAbstractWidget.X, scrollpos);
					map.updateWidget();
				}
			}
		};

		swing_timer = new javax.swing.Timer(time_interval, map_auto_scroller);
		swing_timer.start();
	}
		
	public int get_bases_per_pix() {
		return bases_per_pix;
	}

	public void set_bases_per_pix(int as_bases_per_pix) {
		this.bases_per_pix = as_bases_per_pix;
	}

	public int get_pix_to_scroll() {
		return pix_to_scroll;
	}

	public void set_pix_to_scroll(int as_pix_to_scroll) {
		this.pix_to_scroll = as_pix_to_scroll;
	}

	public int get_time_interval() {
		return time_interval;
	}

	public void set_time_interval(int as_time_interval) {
		this.time_interval = as_time_interval;
	}

	public int get_start_pos() {
		return start_pos;
	}

	public void set_start_pos(int as_start_pos) {
		this.start_pos = as_start_pos;
	}

	public int get_end_pos() {
		return end_pos;
	}

	public void set_end_pos(int as_end_pos) {
		this.end_pos = as_end_pos;
	}
		
}

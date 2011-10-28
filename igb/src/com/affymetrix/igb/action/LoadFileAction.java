/**
 *   Copyright (c) 2001-2007 Affymetrix, Inc.
 *
 *   Licensed under the Common Public License, Version 1.0 (the "License").
 *   A copy of the license must be included with any distribution of
 *   this source code.
 *   Distributions from Affymetrix, Inc., place this in the
 *   IGB_LICENSE.html file.
 *
 *   The license is also available at
 *   http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.igb.action;

import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.List;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.TransferHandler;

import com.affymetrix.genometryImpl.parsers.FileTypeHolder;
import com.affymetrix.genometryImpl.util.UniFileFilter;
import com.affymetrix.genometryImpl.util.FileDropHandler;
import com.affymetrix.genometryImpl.util.ErrorHandler;

import com.affymetrix.igb.view.load.GeneralLoadView;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 *
 * @version $Id$
 */
public final class LoadFileAction extends AbstractLoadFileAction {

	private static final long serialVersionUID = 1L;
	private static final LoadFileAction ACTION = new LoadFileAction();

	public static LoadFileAction getAction() {
		return ACTION;
	}

	private final TransferHandler fdh = new FileDropHandler() {

		private static final long serialVersionUID = 1L;

		@Override
		public void openFileAction(File f) {
			URI uri = f.toURI();
			if (!openURI(uri)) {
				ErrorHandler.errorPanel("FORMAT NOT RECOGNIZED", "Format not recognized for file: " + f.getName());
			}
		}

		@Override
		public void openURLAction(String url) {
			if (url.contains("fromTree:")) {
				url = url.substring(url.indexOf(":") + 1, url.length());
				try {
					GeneralLoadView.getLoadView().getFeatureTree().updateTree(url);
					GeneralLoadView.getLoadView().refreshTreeView();
				} catch (URISyntaxException ex) {
					Logger.getLogger(LoadFileAction.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else {
				try {
					URI uri = new URI(url.trim());
					if (!openURI(uri)) {
						ErrorHandler.errorPanel("FORMAT NOT RECOGNIZED", "Format not recognized for file: " + url);
					}
				} catch (URISyntaxException ex) {
					ex.printStackTrace();
					ErrorHandler.errorPanel("INVALID URL", url + "\n Url provided is not valid: ");
				}
			}
		}
	};
	
	/**
	 *  Constructor.
	 *  @param ft  a FileTracker used to keep track of directory to load from
	 */
	private LoadFileAction() {
		super();
		this.gviewerFrame.setTransferHandler(fdh);
	}

	@Override
	protected void addSupportedFiles() {
		Map<String, List<String>> nameToExtensionMap = FileTypeHolder.getInstance().getNameToExtensionMap();
		for (String name : nameToExtensionMap.keySet()) {
			chooser.addChoosableFileFilter(new UniFileFilter(
					nameToExtensionMap.get(name).toArray(new String[]{}),
					name + " Files"));
		}
		
		chooser.addChoosableFileFilter(new UniFileFilter(
				new String[]{"igb", "py"},
				"Script File"));
	}
	
	@Override
	public String getText() {
		return MessageFormat.format(
				BUNDLE.getString("menuItemHasDialog"),
				BUNDLE.getString("openFile"));
	}

	@Override
	public int getMnemonic() {
		return KeyEvent.VK_O;
	} 	 

	@Override
	protected String getID() {
		return "loadFile";
	}
	
	@Override
	protected String getFriendlyNameID(){
		return "openURI";
	}
	
	@Override
	protected boolean loadSequenceAsTrack() {
		return false;
	}
}

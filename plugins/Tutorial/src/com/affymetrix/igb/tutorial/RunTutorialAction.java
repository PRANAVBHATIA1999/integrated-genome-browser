package com.affymetrix.igb.tutorial;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;

import org.codehaus.jackson.map.ObjectMapper;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.util.ErrorHandler;

public class RunTutorialAction extends GenericAction {
	private static final long serialVersionUID = 1L;
	private final TutorialManager tutorialManager;
	private String name;
	private String uri;

	public RunTutorialAction(TutorialManager tutorialManager, String name, String uri) {
		super();
		this.tutorialManager = tutorialManager;
		this.name = name;
		this.uri = uri;
		putValue(Action.NAME, getText());
	}

	@Override
	public String getText() {
		return name;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			URL url = new URL(uri);
			BufferedReader rdr = new BufferedReader(new InputStreamReader(url.openStream()));
			if (rdr != null) {
				TutorialStep[] tutorial = loadTutorial(rdr);
				if (tutorial != null) {
					tutorialManager.runTutorial(tutorial);
				}
			}
			rdr.close();
		}
		catch (Exception x) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Unable to load tutorial " + uri, x);
			ErrorHandler.errorPanel("Tutorial Error", "Unable to load tutorial " + uri);
		}
	}

	private TutorialStep[] loadTutorial(Reader reader) {
		TutorialStep[] tutorial = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			tutorial = mapper.readValue(reader, TutorialStep[].class);
		}
		catch (Exception x) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Unable to load tutorial " + uri, x);
			ErrorHandler.errorPanel("Tutorial Error", "Unable to load tutorial " + uri);
		}
		return tutorial;
	}
}

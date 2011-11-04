package com.affymetrix.igb.tutorial;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingWorker;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.genometryImpl.event.GenericActionListener;
import com.affymetrix.genometryImpl.event.GenericActionDoneCallback;
import com.affymetrix.genometryImpl.util.ErrorHandler;
import com.affymetrix.genoviz.swing.recordplayback.JRPHierarchicalWidget;
import com.affymetrix.genoviz.swing.recordplayback.JRPMenu;
import com.affymetrix.genoviz.swing.recordplayback.JRPMenuItem;
import com.affymetrix.genoviz.swing.recordplayback.JRPWidget;
import com.affymetrix.genoviz.swing.recordplayback.JRPWrapper;
import com.affymetrix.genoviz.swing.recordplayback.RecordPlaybackHolder;

import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.window.service.IWindowService;

import furbelow.AbstractComponentDecorator;

public class TutorialManager implements GenericActionListener, GenericActionDoneCallback {

	private final TutorialNavigator tutorialNavigator;
	private boolean tutorialDisplayed = false;
	private TutorialStep[] tutorial = null;
	private String waitFor = null;
	private Map<String, TutorialStep[]> triggers = new HashMap<String, TutorialStep[]>();
	private Map<String, AbstractComponentDecorator> decoratorMap = new HashMap<String, AbstractComponentDecorator>();
	private MenuListener menuListener = new MenuListener() {
		@Override
		public void menuSelected(MenuEvent e) {
			advanceStep();
			((JRPMenu)e.getSource()).removeMenuListener(this);
		}
		@Override public void menuDeselected(MenuEvent e) {}
		@Override public void menuCanceled(MenuEvent e) {}
	};
	private ActionListener menuItemListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			advanceStep();
			((JRPMenuItem)e.getSource()).removeActionListener(this);
		}
	};
	private int stepIndex = 0;

	public TutorialManager(IGBService igbService, IWindowService windowService) {
		super();
		addRecordPlayback(igbService);
		this.tutorialNavigator = new TutorialNavigator(new TutorialBackAction(this), new TutorialNextAction(this), new TutorialCancelAction(this));
		windowService.setTopComponent1(tutorialNavigator);
		tutorialNavigator.setVisible(false);
		tutorialDisplayed = false;
		TweeningZoomAction.getAction();
	}

	public void setTutorialDisplayed(boolean tutorialDisplayed) {
		this.tutorialDisplayed = tutorialDisplayed;
	}

	private JComponent getWidget(String widgetId) {
		int pos = widgetId.indexOf('.');
		if (pos == -1) {
			return (JComponent)RecordPlaybackHolder.getInstance().getWidget(widgetId);
		}
		String mainWidgetId = widgetId.substring(0, pos);
		JComponent mainWidget = (JComponent)RecordPlaybackHolder.getInstance().getWidget(mainWidgetId);
		if (mainWidget == null) {
			return null;
		}
		if (!(mainWidget instanceof JRPHierarchicalWidget)) {
			ErrorHandler.errorPanel("Tutorial Error", "error in tutorial, widget " + widgetId + " is incorrect, not hierarchical.");
			return null;
		}
		String subId = widgetId.substring(pos + 1);
		return ((JRPHierarchicalWidget)mainWidget).getSubComponent(subId);
	}

	private boolean highlightWidget(String widgetId) {
		JComponent widget = getWidget(widgetId);
		if (widget == null) {
			return false;
		}
		Marquee m = new Marquee(widget);
		decoratorMap.put(widgetId, m);
//		saveBackgroundColor = widget.getBackground();
//		widget.setBackground(HIGHLIGHT_COLOR);
//		widget.requestFocusInWindow();
		return true;
	}

	private void unhighlightWidget(String widgetId) {
		AbstractComponentDecorator decorator = decoratorMap.get(widgetId);
		if (decorator != null) {
			decorator.setVisible(false);
			decorator.dispose();
			decoratorMap.remove(widgetId);
		}
//		JComponent widget = getWidget(widgetId);
//		if (widget != null) {
//			widget.setBackground(saveBackgroundColor);
//		}
	}

	private boolean runTutorialStep(TutorialStep step) {
		if (!tutorialDisplayed) {
			tutorialNavigator.setVisible(true);
			tutorialDisplayed = true;
		}
		if (step.getText() == null) {
			tutorialNavigator.getInstructions().setText("");
		}
		else {
			tutorialNavigator.getInstructions().setText(step.getText());
		}
		if (step.getHighlight() != null) {
			if (!highlightWidget(step.getHighlight())) {
				ErrorHandler.errorPanel("Tutorial Error", "error in tutorial, unable to find widget " + step.getHighlight());
			}
		}
		if (step.getExecute() != null) {
			GenericAction action = GenericActionHolder.getInstance().getGenericAction(step.getExecute().getName());
			if (action instanceof IAmount) {
				((IAmount)action).setAmount(step.getExecute().getAmount());
			}
			action.addDoneCallback(this);
			action.actionPerformed(null);
		}
		if (step.getTrigger() != null) {
			if (step.getSubTutorial() == null) {
				ErrorHandler.errorPanel("Tutorial Error", "error in tutorial, no sub tutorial for trigger " + step.getTrigger());
			}
			else {
				triggers.put(step.getTrigger(), step.getSubTutorial());
			}
		}
		else if (step.getSubTutorial() != null) {
			runTutorial(step.getSubTutorial());
		}
		else if (step.getTimeout() > 0) {
			try {
				Thread.sleep(step.getTimeout() * 1000);
			}
			catch (InterruptedException x) {}
		}
		else if (step.getWaitFor() == null) {
			waitFor = TutorialNextAction.class.getSimpleName(); // default
//			highlightWidget("TutorialNavigator_next");
			return false;
		}
		else {
			String waitForItem = step.getWaitFor();
			JRPWidget widget = RecordPlaybackHolder.getInstance().getWidget(waitForItem);
			if (widget instanceof JRPMenu) {
				((JRPMenu)widget).addMenuListener(menuListener);
			}
			else if (widget instanceof JRPMenuItem) {
				((JRPMenuItem)widget).addActionListener(menuItemListener);
			}
			else {
				waitFor = waitForItem;
			}
			return false;
		}
		return true;
	}

	public void runTutorial(TutorialStep[] tutorial) {
		waitFor = null;
		stepIndex = 0;
		this.tutorial = tutorial;
		nextStep();
	}

	private void nextStep() {
		if (stepIndex >= tutorial.length) {
			tutorialDone();
		}
		else {
			SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					boolean finished = runTutorialStep(tutorial[stepIndex]);
					if (finished) {
						advanceStep();
					}
					return null;
				}
			};
			sw.execute();
		}
	}

	private void advanceStep() {
		TutorialStep step = tutorial[stepIndex];
		if (step.getHighlight() != null) {
			unhighlightWidget(step.getHighlight());
		}
		waitFor = null;
		stepIndex++;
		nextStep();
	}

	public void tutorialDone() {
		waitFor = null;
		stepIndex = 0;
		tutorial = null;
		tutorialNavigator.setVisible(false);
		tutorialDisplayed = false;
		triggers.clear();
	}

	public void back() {
	}

	public void next() {
	}

	public void stop() {
		TutorialStep step = tutorial[stepIndex];
		if (step.getHighlight() != null) {
			unhighlightWidget(step.getHighlight());
		}
		tutorialDone();
	}

	private void addRecordPlayback(IGBService igbService) {
	}

	public void addJComponent(String id, JComponent comp) {
		RecordPlaybackHolder.getInstance().addWidget(new JRPWrapper(id, comp));
	}

	public void removeJComponent(String id) {
		RecordPlaybackHolder.getInstance().removeWidget(id);
	}

	@Override
	public void onCreateGenericAction(GenericAction genericAction) {}

	@Override
	public void notifyGenericAction(GenericAction genericAction) {
		String id = genericAction.getId();
		if (genericAction.getExtraInfo() != null) {
			id += "\\" + genericAction.getExtraInfo();
		}
		if (id.equals(waitFor)) {
			advanceStep();
		}
		else {
			TutorialStep[] tutorial = triggers.get(id);
			if (tutorial != null) {
				runTutorial(tutorial);				
			}
		}
	}

	@Override
	public void actionDone(GenericAction action) {
		advanceStep();
		action.removeDoneCallback(this);
	}
}

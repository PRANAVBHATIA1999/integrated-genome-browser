package com.affymetrix.igb.tutorial;

public class TutorialStep {
	public static class TutorialExecute {
		private String name;
		private float amount;
		private String param;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public float getAmount() {
			return amount;
		}
		public void setAmount(float amount) {
			this.amount = amount;
		}
		public String getParam() {
			return param;
		}
		public void setParam(String param) {
			this.param = param;
		}
	}
	private String text;
	private String highlight;
	private int timeout;
	private String trigger;
	private String waitAction;
	private String waitMenu;
	private TutorialExecute execute;
	private TutorialStep[] subTutorial;
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getHighlight() {
		return highlight;
	}
	public void setHighlight(String highlight) {
		this.highlight = highlight;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public String getTrigger() {
		return trigger;
	}
	public void setTrigger(String trigger) {
		this.trigger = trigger;
	}
	public String getWaitAction() {
		return waitAction;
	}
	public void setWaitAction(String waitAction) {
		this.waitAction = waitAction;
	}
	public String getWaitMenu() {
		return waitMenu;
	}
	public void setWaitMenu(String waitMenu) {
		this.waitMenu = waitMenu;
	}
	public TutorialExecute getExecute() {
		return execute;
	}
	public void setExecute(TutorialExecute execute) {
		this.execute = execute;
	}
	public TutorialStep[] getSubTutorial() {
		return subTutorial;
	}
	public void setSubTutorial(TutorialStep[] subTutorial) {
		this.subTutorial = subTutorial;
	}
}

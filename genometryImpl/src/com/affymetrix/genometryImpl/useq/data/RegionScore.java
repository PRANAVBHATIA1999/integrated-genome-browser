package com.affymetrix.genometryImpl.useq.data;

/** @author david.nix@hci.utah.edu*/
public class RegionScore extends Region{
	//fields
	protected float score;

	//constructor
	public RegionScore (int start, int stop, float score){
		super(start, stop);
		this.score = score;
	}

	//methods
	public String toString(){
		return start+"\t"+stop+"\t"+score;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

}

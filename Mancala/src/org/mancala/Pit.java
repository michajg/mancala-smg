package org.mancala;

public class Pit {
	private int seeds;
	private boolean treasureChest;
	
	public Pit() {
	}
	
	public Pit(int seeds, boolean treasurechest) {
		this.seeds = seeds;
		this.treasureChest = treasurechest;
	}
	
	public void addSeeds(int seeds) {
		this.seeds += seeds;
	}
	
	public void removeSeeds(int seeds) {
		this.seeds -= seeds;
	}
	
	public void removeAllSeeds() {
		this.seeds = 0;
	}
	
	public int getSeeds(){
		return this.seeds;
	}
	
	public boolean isTreasureChest(){
		return this.treasureChest;
	} 
	
	@Override
	public String toString() {
		if(this.treasureChest)
			return this.seeds + " TC";
		else
			return Integer.toString(this.seeds);
	}
}

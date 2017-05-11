package control;

import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;
import java.util.Vector;

public class Game {
	private int resolution;
	private String picturename;
	private Vector<Integer> table = new Vector<Integer>();
	private Timer timer = new Timer();
	private int startTime;
	private Controller c;
	
	public Game(Controller c) {
		this.c = c;
	}
	
	public int getResolution() {
		return resolution;
	}
	protected void setResolution(int resolution) {
		this.resolution = resolution;
	}
	
	public String getPicturename() {
		return picturename;
	}
	
	protected void setPicturename(String picturename) {
		this.picturename = picturename;
	}
	
	public Vector<Integer> getTable() {
		return table;
	}
	
	public Vector<Integer> getSolvedTable() {
		int size = resolution*resolution;
		Vector<Integer> solved = new Vector<Integer>();
		
		for(int i=0;i<size;i++ ){
			solved.add(i);
		}
		return solved;
	}
	
	public int getStartTime() {
		return startTime;
	}
	
	protected void init() {
		table.clear();
		int size = resolution*resolution;
		for(int i=0;i<size;i++ ){
			table.add(i);
		}
		//c.tableChanged();
		
	}
	
	protected void startTimer(int delay) {
		startTime = delay+1;
		timer.scheduleAtFixedRate(new TimerTask() {          
		    @Override
		    public void run() {
		    	startTime--;
		    	if(startTime>=1){
		    		System.out.println(startTime);
		    	}else{
			    	this.cancel();
		    	}
		    	c.startTimerChanged();
		
		    }
		}, 0,1000);
	}
		
	protected void swap(int clickedTile) {
		Collections.swap(table,getBlankTilePosition(),clickedTile);
	}
	
	private int getBlankTilePosition() {
		for(int i=0;i<table.size();i++ ){
			if (table.get(i) == 0){
				return i;
			}
		}
		return -1;
	}
	
	private boolean isSwappable(int clickResult) {
		int blankTile = getBlankTilePosition();
		
		if(getNeighboursOf(clickResult).contains(blankTile)){
			return true;
		}else{
			return false;
		}
			
	}
	
	protected boolean isFinished() {
		for(int i=0;i<table.size();i++ ){
			if (table.get(i) != i){
				return false;
			}
		}
		return true;
	}
	
	protected void mix(int steps) {
		Random r = new Random();
		for(int i=0;i<steps;i++){
			Vector<Integer> neighbours= getNeighboursOf(getBlankTilePosition());
			swap(neighbours.get(r.nextInt(neighbours.size())));
		}
	}
	
	private Vector<Integer> getNeighboursOf(int Tile){
		Vector<Integer> neighbours= new Vector<Integer>();
		if (Tile-resolution >= 0){
			neighbours.add(Tile-resolution);
		}
		if ((Tile + resolution <  resolution*resolution)){
			neighbours.add(Tile+resolution);
		}
		if((Math.floor((Tile+1)/resolution) == Math.floor(Tile/resolution)) && (Tile+1)<resolution*resolution){
			neighbours.add(Tile+1);
		}
		if((Math.floor((Tile-1)/resolution) == Math.floor(Tile/resolution))&&(Tile-1)>=0){
			neighbours.add(Tile-1);
		}
		return neighbours;
	}
	
	protected boolean canMove(int clickResult) {
		if (isStarted() && !isFinished() && isSwappable(clickResult)){
			return true;
		}else{
			return false;
		}
	}
	
	protected boolean isStarted() {
		return startTime==0?true:false;
	}

	protected void setTable(Vector<Integer> deserializeBytes) {
		table = deserializeBytes;
		
	}
}

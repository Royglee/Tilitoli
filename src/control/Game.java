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
	private Boolean needToCancel;
	
	/** Konstruktor
	 * 
	 * @param Controller c
	 */
	public Game(Controller c) {
		this.c = c;
	}
	
	/**
	 * 
	 * @return A j�t�kt�bla m�rete (3x3 eset�n -> 3)
	 */
	public int getResolution() {
		return resolution;
	}
	
	/** Be�ll�tja a j�t�kt�r m�ret�t
	 * 
	 * @param resolution
	 */
	protected void setResolution(int resolution) {
		this.resolution = resolution;
	}
	
	/** Visszaadja a kirakand� k�p azonos�t�j�t
	 * 
	 * @return
	 */
	public String getPicturename() {
		return picturename;
	}
	
	/** Be�ll�that� a kirakand� k�p azonos�t�ja
	 * 
	 * @param picturename
	 */
	protected void setPicturename(String picturename) {
		this.picturename = picturename;
	}
	
	/** 
	 * 
	 * @return A j�t�kteret reprezent�l� Vector
	 */
	public Vector<Integer> getTable() {
		return table;
	}
	
	/** Be�ll�that� egy k�v�nt kever�s
	 * 
	 * @param table - a kever�st reprezent�l� vektor
	 */
	protected void setTable(Vector<Integer> table) {
		this.table = table;
		
	}
	
	/** Inicializ�lja a j�t�kteret az adott felbont�s f�ggv�ny�ben
	 * 
	 */
	protected void init() {
		table.clear();
		int size = resolution*resolution;
		for(int i=0;i<size;i++ ){
			table.add(i);
		}	
	}
	
	/** Visszaadja a kirakott t�bl�t reprezent�l� vektort
	 * 
	 * @return solved
	 */
	public Vector<Integer> getSolvedTable() {
		int size = resolution*resolution;
		Vector<Integer> solved = new Vector<Integer>();
		
		for(int i=0;i<size;i++ ){
			solved.add(i);
		}
		return solved;
	}
	
	/** Param�terk�nt kapott sz�m� random l�p�ssel megkeveri a t�bl�t
	 * 
	 * @param steps
	 */
	protected void mix(int steps) {
		Random r = new Random();
		for(int i=0;i<steps;i++){
			Vector<Integer> neighbours= getNeighboursOf(getBlankTilePosition());
			swap(neighbours.get(r.nextInt(neighbours.size())));
		}
	}
	
	/** A param�ter�l kapott elemet megcser�li az �res elemmel
	 * 
	 * @param clickedTile
	 */
	protected void swap(int clickedTile) {
		Collections.swap(table,getBlankTilePosition(),clickedTile);
	}
	
	/**
	 * 
	 * @return A j�t�k kezd�s�re vonatkoz� visszasz�ml�l� �rt�ke
	 */
	public int getStartTime() {
		return startTime;
	}
	
	/**
	 * 
	 * @return true - A j�t�k elkezd�d�tt
	 */
	protected boolean isStarted() {
		return startTime==0?true:false;
	}
	
	/** Jelzi, hogy a j�t�k kezd�s�re vonatkoz� visszasz�ml�l�st meg kell szak�tani
	 * 
	 */
	protected void startTimerCancel() {
		needToCancel = true;
	}
	
	/** Elind�tja a j�t�k elej�n l�v� visszasz�ml�l�t 
	 * 
	 * @param delay mp-ig sz�mol vissza
	 */
	protected void startTimer(int delay) {
		needToCancel = false;
		startTime = delay+1;
		timer.scheduleAtFixedRate(new TimerTask() {          
		    @Override
		    public void run() {
		    	if(needToCancel){
		    		this.cancel();
		    	}else{
		    		startTime--;
			    	if(startTime>=1){
			    		System.out.println(startTime);
			    	}else{
				    	this.cancel();
			    	}
			    	c.startTimerChanged();
		    	}
		
		    }
		}, 0,1000);
	}
	
	/** Visszaadja, hogy az adott elem elmozd�that�-e
	 * 
	 * @param clickResult
	 * @return
	 */
	protected boolean canMove(int clickResult) {
		if (isStarted() && !isFinished() && isSwappable(clickResult)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 
	 * @return A j�t�kos pontsz�ma
	 */
	protected int getScore(){
		int inPlace=0;
		for(int i=0;i<table.size();i++ ){
			if (table.get(i) == i){
				inPlace++;
			}
		}
		
		return (100*inPlace)/(resolution*resolution);
	}
	
	/** 
	 * 
	 * @return true, ha a j�t�k befejez�d�tt
	 */
	protected boolean isFinished() {
		for(int i=0;i<table.size();i++ ){
			if (table.get(i) != i){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @return Az �res elem poz�ci�j�t a j�t�kt�rben
	 */
	private int getBlankTilePosition() {
		for(int i=0;i<table.size();i++ ){
			if (table.get(i) == 0){
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * 
	 * @param clickResult
	 * @return true, ha a bemeneti param�ter az �res elem mellett van
	 */
	private boolean isSwappable(int clickResult) {
		int blankTile = getBlankTilePosition();
		
		if(getNeighboursOf(clickResult).contains(blankTile)){
			return true;
		}else{
			return false;
		}
			
	}
	
	/**
	 * 
	 * @param Tile
	 * @return Egy adott elem szomsz�dait tartalmaz� vektor
	 */
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
	
}

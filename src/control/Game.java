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
	 * @return A játéktábla mérete (3x3 esetén -> 3)
	 */
	public int getResolution() {
		return resolution;
	}
	
	/** Beállítja a játéktér méretét
	 * 
	 * @param resolution
	 */
	protected void setResolution(int resolution) {
		this.resolution = resolution;
	}
	
	/** Visszaadja a kirakandó kép azonosítóját
	 * 
	 * @return
	 */
	public String getPicturename() {
		return picturename;
	}
	
	/** Beállítható a kirakandó kép azonosítója
	 * 
	 * @param picturename
	 */
	protected void setPicturename(String picturename) {
		this.picturename = picturename;
	}
	
	/** 
	 * 
	 * @return A játékteret reprezentáló Vector
	 */
	public Vector<Integer> getTable() {
		return table;
	}
	
	/** Beállítható egy kívánt keverés
	 * 
	 * @param table - a keverést reprezentáló vektor
	 */
	protected void setTable(Vector<Integer> table) {
		this.table = table;
		
	}
	
	/** Inicializálja a játékteret az adott felbontás függvényében
	 * 
	 */
	protected void init() {
		table.clear();
		int size = resolution*resolution;
		for(int i=0;i<size;i++ ){
			table.add(i);
		}	
	}
	
	/** Visszaadja a kirakott táblát reprezentáló vektort
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
	
	/** Paraméterként kapott számú random lépéssel megkeveri a táblát
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
	
	/** A paraméterül kapott elemet megcseréli az üres elemmel
	 * 
	 * @param clickedTile
	 */
	protected void swap(int clickedTile) {
		Collections.swap(table,getBlankTilePosition(),clickedTile);
	}
	
	/**
	 * 
	 * @return A játék kezdésére vonatkozó visszaszámláló értéke
	 */
	public int getStartTime() {
		return startTime;
	}
	
	/**
	 * 
	 * @return true - A játék elkezdõdött
	 */
	protected boolean isStarted() {
		return startTime==0?true:false;
	}
	
	/** Jelzi, hogy a játék kezdésére vonatkozó visszaszámlálást meg kell szakítani
	 * 
	 */
	protected void startTimerCancel() {
		needToCancel = true;
	}
	
	/** Elindítja a játék elején lévõ visszaszámlálót 
	 * 
	 * @param delay mp-ig számol vissza
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
	
	/** Visszaadja, hogy az adott elem elmozdítható-e
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
	 * @return A játékos pontszáma
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
	 * @return true, ha a játék befejezõdött
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
	 * @return Az üres elem pozícióját a játéktérben
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
	 * @return true, ha a bemeneti paraméter az üres elem mellett van
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
	 * @return Egy adott elem szomszédait tartalmazó vektor
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

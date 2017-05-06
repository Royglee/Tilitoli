package control;

import java.util.Collections;
import java.util.Random;
import java.util.Vector;

public class game {
	private int resolution;
	public String picturename;
	Vector<Integer> table = new Vector<Integer>();
	
	public int getResolution() {
		return resolution;
	}
	public void setResolution(int resolution) {
		this.resolution = resolution;
	}
	
	public String getPicturename() {
		return picturename;
	}
	
	public void setPicturename(String picturename) {
		this.picturename = picturename;
	}
	
	public Vector<Integer> getTable() {
		return table;
	}
	
	public void init() {
		int size = resolution*resolution;
		for(int i=0;i<size;i++ ){
			table.add(i);
		}
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
	
	public boolean isSwappable(int clickResult) {
		int blankTile = getBlankTilePosition();
		
		if(getNeighboursOf(clickResult).contains(blankTile)){
			return true;
		}else{
			return false;
		}
			
	}
	
	public boolean isFinished() {
		for(int i=0;i<table.size();i++ ){
			if (table.get(i) != i){
				return false;
			}
		}
		return true;
	}
	
	public void mix(int steps) {
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
		System.out.println(neighbours);
		return neighbours;
	}
}

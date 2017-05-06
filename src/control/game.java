package control;

import java.util.Vector;

public class game {
	protected int resolution;
	public String picturename;
	Vector<Integer> table = new Vector<Integer>();
	
	public game() {
		// TODO Auto-generated constructor stub
	}
	
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
}

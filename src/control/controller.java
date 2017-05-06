package control;

import java.util.Vector;
import java.util.Collections;
import gui.GUI;

public class controller{
	protected GUI g;
	protected game game = new game();
	
	public game getGame() {
		return game;
	}
	
	public void setGUI(GUI g) {
		this.g = g;
	}
	
	public void init() {
		int size = game.getResolution()*game.getResolution();
		for(int i=0;i<size;i++ ){
			game.getTable().add(i);
		}
	}
	
	public boolean isFinished() {
		for(int i=0;i<game.getTable().size();i++ ){
			if (game.getTable().get(i) != i){
				return false;
			}
		}
		return true;
	}
	
	public void mix() {
		int a=2;
	}

	
	public void clicked(int clickResult) {
		
		if(isSwappable(clickResult)){
			swap(clickResult);
		}
		
		g.makePanel(game.getTable());
		
		if(isFinished()){
			System.out.println("Finished");
		}
	}
	
	private int getBlankTilePosition() {
		for(int i=0;i<game.getTable().size();i++ ){
			if (game.getTable().get(i) == 0){
				return i;
			}
		}
		return -1;
	}
	
	private boolean isSwappable(int clickResult) {
		int blankPosition = getBlankTilePosition();
		
		if(blankPosition+game.getResolution() == clickResult || blankPosition-game.getResolution() == clickResult){
			return true;
		}
		else if((blankPosition+1 == clickResult || blankPosition-1 == clickResult) && (Math.floor(blankPosition/game.getResolution()) == Math.floor(clickResult/game.getResolution()))){
			return true;
		}
		else{
			return false;
		}
			
	}
	
	private void swap(int clickResult) {
		Collections.swap(game.getTable(),getBlankTilePosition(),clickResult);
	}
	

}

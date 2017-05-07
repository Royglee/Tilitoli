package control;



import java.util.Timer;
import java.util.TimerTask;

import gui.GUI;

public class controller{
	protected GUI g;
	protected game game = new game();
	protected Timer timer = new Timer();
	
	public game getGame() {
		return game;
	}
	
	public void setGUI(GUI g) {
		this.g = g;
	}
	
	public void init() {
		game.init();
		timer.schedule(new TimerTask() {          
		    @Override
		    public void run() {
		    	game.mix(500);  
		    	g.makePanel(game.getTable());
		    }
		}, 5000);
	}
	
	public void clicked(int clickResult) {
		
		if(game.isSwappable(clickResult)){ //az ellenõrzést lehet átkéne tenni a game objektumba
			game.swap(clickResult); 
		}
		
		g.makePanel(game.getTable());
		
		if(game.isFinished()){
			System.out.println("Finished");
			g.win();
		}
	}

}

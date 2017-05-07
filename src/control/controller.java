package control;
import gui.GUI;

public class controller{
	private GUI g;
	private game game = new game(this);
	private boolean endScreenDrawn; 
	
	public game getGame() {
		return game;
	}
	
	public void setGUI(GUI g) {
		this.g = g;
	}
	
	public void init() {
		endScreenDrawn = false;
		game.init();
	}
	
	public void clicked(int clickResult) {
		
		if(game.canMove(clickResult)){
			game.swap(clickResult); 
			tableChanged();
		}
		
		if(endScreenDrawn){
			g.drawMainScreen();
		}
		
		if(game.isStarted() && game.isFinished() && !endScreenDrawn){
			System.out.println("Finished");
			endScreenDrawn=true;
			g.win();
		}
		
		
	}
	
	protected void tableChanged(boolean needSync) {
		g.makePanel(game.getTable());
	}
	protected void tableChanged() {
		g.makePanel(game.getTable());
	}

}

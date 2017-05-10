package control;
import gui.GUI;
import multi.Multiplayer;

public class controller{
	private GUI g;
	private game game = new game(this);
	private Multiplayer multi;
	private boolean endScreenDrawn; 
	private boolean isMulti;
	private boolean servermode;
	private String myName;
	
	
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
			g.countBack(":)");
		}
		
		
	}
	
	protected void tableChanged(boolean needSync) {
		g.makePanel(game.getTable());
	}
	protected void tableChanged() {
		g.makePanel(game.getTable());
	}
	
	protected void startTimerChanged(){
		String string = (game.getStartTime()>0)?""+game.getStartTime():"GO!";
		g.countBack(string);
	}
	
	public String[] listServers(){
		String[] servers = new String[] {"dog", "cat","elephant", "giraffe"};
		return servers;
	}

}

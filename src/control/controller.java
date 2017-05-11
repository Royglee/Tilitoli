package control;
import java.io.IOException;
import gui.GUI;
import multi.Multiplayer;
import multi.Puzzle;

public class controller{
	private GUI g;
	private game game = new game(this);
	private Multiplayer multi;
	private boolean endScreenDrawn; 
	private boolean multiplayer=false;
	private boolean servermode=false;
	private String myName;
	
	
	public game getGame() {
		return game;
	}
	
	public void setGUI(GUI g) {
		this.g = g;
	}
	
	private void init() {
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
	
	public void listServers(){
		String[] servers = new String[] {"dog", "cat","elephant", "giraffe"};
		multi = new Multiplayer("IGEN");
		//g.drawClientScreen(multi.listGameNames(1000));
		g.drawClientScreen(servers);
	}
	
	public void setMultiMode(boolean isMulti) {
		multiplayer = isMulti;
	}
	
	public void setServerMode(boolean isServer) {
		this.servermode = isServer;
	}
	
	public boolean getServerMode(){
		return servermode;
	}

	public void createGame() throws IOException, ClassNotFoundException {
		multi = new Multiplayer(myName);
		game.init();
		Puzzle p = new Puzzle(game.getPicturename(), (byte)game.getResolution(),ObjectCastHelper.serializeObject(game.getTable()));
		multi.createGame(p);
		
	}
	
	public void startGame() {
		if(!multiplayer){
			init();
			tableChanged();
			game.mixAfterDelay();
		}else{
			multi.startGame();
		}
	}

	public void setMyName(String name) {
		myName = name;
		
	}
	
	public void setGameParameters(String pictureName, int resolution) {
		game.setPicturename(pictureName);
		game.setResolution(resolution);
	}

}

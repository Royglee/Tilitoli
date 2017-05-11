package control;
import java.io.IOException;
import gui.GUI;
import multi.Multiplayer;
import multi.Puzzle;

public class Controller{
	private GUI g;
	private Game game = new Game(this);
	private Multiplayer multi;
	private boolean endScreenDrawn; 
	private boolean multiplayer=false;
	private boolean servermode=false;
	private String myName;
	
	/**GUI-ra referencia be�ll�t�s
	 * 
	 * @param g
	 * GUI p�ld�ny
	 */
	public void setGUI(GUI g) {
		this.g = g;
	}
	
	/** 
	 * 
	 * @return a j�t�kot le�r� game objektum
	 */
	public Game getGame() {
		return game;
	}
	
	/**Be�ll�tja hogy a j�t�k single/multi m�dban fusson
	 * 
	 * @param isMulti
	 * true - A j�t�k multiplayer m�dban fog menni, false-a j�t�k singleplayer m�dban fog menni
	 */
	public void setMultiMode(boolean isMulti) {
		multiplayer = isMulti;
	}
	
	/**
	 * 
	 * @param isServer
	 * true-szerver m�dban �zemel false-kliens m�dban �zemel a program
	 */
	public void setServerMode(boolean isServer) {
		this.servermode = isServer;
	}
	
	/** Visszaadja, hogy milyen m�dban fut a szoftver
	 * 
	 * @return true, ha szerver m�dban fut. false, ha kliens m�dban
	 */
	public boolean getServerMode(){
		return servermode;
	}
	
	/** Be�l�tja a felhaszn�l� nev�t
	 * 
	 * @param name
	 */
	public void setMyName(String name) {
		myName = name;
		
	}
	
	/**
	 * 
	 * @return a felhaszn�l� neve
	 */
	public String getMyName() {
		return myName;
	}
	
	/** Be�ll�tja a j�t�kra jellemz� param�tereket
	 * 
	 * @param pictureName
	 * @param resolution
	 */
	public void setGameParameters(String pictureName, int resolution) {
		game.setPicturename(pictureName);
		game.setResolution(resolution);
	}
	
	public void createGame() throws IOException, ClassNotFoundException {
		multi = new Multiplayer(myName);
		game.init();
		game.mix(500);
		Puzzle p = new Puzzle(game.getPicturename(), (byte)game.getResolution(),ObjectCastHelper.serializeObject(game.getTable()));
		multi.createGame(p);		
	}
	
	
	public void listServers(){
		//String[] servers = new String[] {"dog", "cat","elephant", "giraffe"};
		multi = new Multiplayer("Clientnicname");
		g.drawClientScreen(multi.listGameNames(1000));
		//g.drawClientScreen(servers);
	}
	
	public void joinServer(String server) {
		multi.joinGame(server);
		startGame();
		
	}
	
	public void startGame() {
		endScreenDrawn = false;
		if(!multiplayer){
			game.init();
			game.mix(500); 
		}else{
			if(servermode){
				multi.startGame();
			}
			else{
				try {
					Puzzle p = multi.getPuzzle();
					game.setPicturename(p.getImage());
					game.setResolution((int)p.getSize());
					game.setTable(ObjectCastHelper.deserializeBytes(p.getPuzzle()));
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		g.makePanel(game.getSolvedTable());
		game.startTimer(5);
	}
	
	protected void startTimerChanged(){
		String string = (game.getStartTime()>0)?""+game.getStartTime():"GO!";
		g.countBack(string);
		
		if(game.getStartTime()==0){
				tableChanged();
		}
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
	
	protected void tableChanged() {
		g.makePanel(game.getTable());
	}

}

package control;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import gui.Gui;
import multi.Multiplayer;
import multi.Puzzle;

public class Controller{
	private Gui g;
	private Game game = new Game(this);
	private Multiplayer multi;
	private boolean endScreenDrawn=true; 
	private boolean waitingForPlayers; 
	private boolean multiplayer=false;
	private boolean servermode=false;
	private String myName;
	private Timer timer = new Timer();
	private int time = 0;
	
	private String[] scores;
	private String[] names;
	

	
	/**GUI-ra referencia be�ll�t�s
	 * 
	 * @param g
	 * GUI p�ld�ny
	 */
	public void setGUI(Gui g) {
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
	
	/** Elk�sz�ti a szervert, ahova csatlakozhatnak a kliensek
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void createGame() throws IOException, ClassNotFoundException {
		game.init();
		game.mix(5);
		Puzzle p = new Puzzle(game.getPicturename(), (byte)game.getResolution(),ObjectCastHelper.serializeObject(game.getTable()));
		if (multi != null){
			multi.finishGame();
		}
		multi = new Multiplayer();
		multi.createGame(myName,p);
		waitingForPlayers=true;
		g.drawServerStartScreen(multi.getConnectionCount());
		timer.scheduleAtFixedRate(new TimerTask() {          
		    @Override
		    public void run() {
		    	if(waitingForPlayers){
		    		g.drawServerStartScreen(multi.getConnectionCount());
		    		System.out.println("Update connected list");
		    	}else{
		    		this.cancel();
		    		System.out.println("Cancel TImer");
		    	}
		    	
		
		    }
		}, 0,1000);
	}
	
	/** Kilist�zza a h�l�zaton el�rhet� szervereket
	 * 
	 */
	public void listServers(){
		if (multi != null){
			multi.finishGame();
		}
		multi = new Multiplayer();
		g.drawClientScreen(multi.listGameNames(1000));
	}
	
	/** A kliens csatlakozik az �ltala v�lasztott j�t�kmester szerver�hez
	 * 
	 * @param server (A j�t�kmester neve)
	 */
	public void joinServer(String server) {
		g.drawWaitForServerScreen();
		multi.joinGame(myName, server);
		startGame();
		
	}
	
	/**	Elind�tja a j�t�kot a be�ll�tott param�terekkel (multi/single/image/size)
	 * 
	 */
	public void startGame() {
		waitingForPlayers = false;
		endScreenDrawn = false;
		time = 0;
		if(!multiplayer){
			game.init();
			game.mix(1); 
			updateScore();
		}else{
			if(servermode){
				if(!multi.startGame()){
					System.out.println("");
				}
			}
			else{
				try {		
					Puzzle p = multi.getPuzzle();
					game.setPicturename(p.getImage());
					game.setResolution((int)p.getSize());
					game.setTable(ObjectCastHelper.deserializeBytes(p.getPuzzle()));
					g.chopImage();
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		g.makePanel(game.getSolvedTable());
		game.startTimer(5);
	}
	
	/** A j�t�k ind�t�sa el�tti 5mp-es sz�ml�l� callback f�ggv�nye
	 * 
	 */
	protected void startTimerChanged(){
		String string = (game.getStartTime()>0)?""+game.getStartTime():"GO!";
		g.countBack(string);
		
		if(game.getStartTime()==0){
				tableChanged();
				if(multiplayer){
					startSyncTimer();
				}
				
		}
	}
	
	/** Egy adott elemre val� kattint�s feldolgoz�s�ra szolg�l� f�ggv�ny	
	 * 
	 * @param clickResult Annak az elemnek a sorsz�ma amelyre a felhaszn�l� kattintott
	 * 3x3 esetben:
	 *  | 0 | 1 | 2 |
	 *  | 3 | 4 | 5 |
	 *  | 6 | 7 | 8 |
	 */
	public void clicked(int clickResult) {	
		if(game.canMove(clickResult)){
			game.swap(clickResult); 
			tableChanged();
			if(!multiplayer){
				updateScore();
			}
		}
		
		if(endScreenDrawn){
			g.drawMainScreen();
			g.drawSideUpperPanel();
		}
		
		endScreenIfSomeoneWon();
		
		
	}
	
	/** A j�t�kt�r megv�ltoz�sa eset�n h�vhat� f�ggv�ny, amely �jrarajzolja a t�bl�t
	 * 
	 */
	protected void tableChanged() {
		g.makePanel(game.getTable());
	}
	
	/** A szervert�l kapott elt�rolt pontok kirajzol�sa a k�perny�re, �s ellen�rz�s, hogy van-e nyertes
	 * 
	 */
	public void updateScore(){
		if(multiplayer){
			g.drawScore(scores, names, ""+time/60, ""+time%60);
			endScreenIfSomeoneWon();
			
		}else{
			String[] names = {myName};
			String[] scores = {""+game.getScore()};
			g.drawScore(scores, names, "", "");
		}
	}

	/** Vissza l�p a f�men�be
	 * 
	 */
	public void backToMainMenu() {
		waitingForPlayers=false;
		endScreenDrawn=true;
		g.drawSideUpperPanel();
		g.drawMainScreen();
		game.startTimerCancel();
		
	}
	
	/** Leellen�rzi, hogy van-e nyertes
	 * 
	 * @return A nyertes neve, vagy ""
	 */
	private String someoneWon(){
		if(multiplayer){
			for (int i = 0; i < scores.length; i++) {
				if (Integer.parseInt(scores[i])==100){
					System.out.println(scores[i]);
					System.out.println(names[i]);
					return names[i];
				}
			}
			
		}else if(game.isStarted() && game.isFinished() && !endScreenDrawn){
			return myName;
		}
		return "";
		
		
	}
	
	/** A Szervernek elk�ldi a saj�t pontsz�mot, 
	 *  �s feldolgozza a v�laszul kapott t�mb�t amelyben a t�bbi j�t�kos neve, �s pontsz�ma szerepel.
	 * 
	 */
	private void getScoresAndNamesArray(){
		if(multiplayer){
			String[] scoresAndNames = multi.syncScore(game.getScore()).listAll();
			
			scores=new String[scoresAndNames.length];
			names=new String[scoresAndNames.length];
			
			for (int i = 0; i < scoresAndNames.length; i++) {
				scores[i] = scoresAndNames[i].split(": ")[1];
				names[i] = scoresAndNames[i].split(": ")[0];	
			}
		}	
	}
	
	/** Elind�t egy timert, ami 1mp-k�nt szinkroniz�lja a pontokat.
	 * 
	 */
	private void startSyncTimer() {
		timer.scheduleAtFixedRate(new TimerTask() {          
		    @Override
		    public void run() {
		    	getScoresAndNamesArray();
		    	time++;
		    	updateScore();
		    	if(endScreenDrawn){
		    		this.cancel();
		    		timer.schedule(new TimerTask() {
						
						@Override
						public void run() {
				    		multi.finishGame();	
				    		System.out.println("Multi finishGame() called");
						}
					}, 3000);
		    	}
		    }
		}, 0,1000);
	}
	
	/** Kirajzolja a j�t�k v�ge k�perny�t, ha valaki nyert
	 * 
	 */
	private void endScreenIfSomeoneWon() {
		if(someoneWon()!=""){
			System.out.println("Finished");
			if(someoneWon().equals(myName)){
				g.win(true, myName);
			}else{
				g.win(false, someoneWon());
			}
			endScreenDrawn=true;
			g.countBack(":)");
		}
	}

}

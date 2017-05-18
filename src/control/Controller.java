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
	

	
	/**GUI-ra referencia beállítás
	 * 
	 * @param g
	 * Gui objektum
	 */
	public void setGUI(Gui g) {
		this.g = g;
	}
	
	/** 
	 * 
	 * @return 
	 * game - a játékot leíró game objektum
	 */
	public Game getGame() {
		return game;
	}
	
	/** Eltárolja, hogy multi üzemmódban fog menni a játék és kirajzolja a következő képernyőt
	 * 
	 */
	public void multiModeSelected() {
		multiplayer = true;
		g.drawServerOrClientScreen();
	}
	
	/** Eltárolja, hogy single üzemmódban fog menni a játék és kirajzolja a következő képernyőt
	 * 
	 */
	public void singleModeSelected() {
		multiplayer = false;
		servermode = false;
		g.drawParameterScreen();
	}
	
	/** Eltárolja, hogy szerver módban fog futni a program és kirajzolja a következő képernyőt
	 * 
	 */
	public void serverModeSelected() {
		servermode = true;
		g.drawParameterScreen();
	}
	
	/** Eltárolja, hogy kliens módban fog futni a program és kirajzolja a következő képernyőt
	 * 
	 */
	public void clientModeSelected() {
		servermode = false;
		
		if (multi != null){multi.finishGame();}
		
		multi = new Multiplayer();
		g.drawClientScreen(multi.listGameNames(1000));
	}
	
	/**
	 * 
	 * @param isServer
	 * true-szerver módban üzemel false-kliens módban üzemel a program
	 * 
	 */
	public void setServerMode(boolean isServer) {
		this.servermode = isServer;
	}
	
	/** Visszaadja, hogy milyen módban fut a szoftver
	 * 
	 * @return
	 * true, ha szerver módban fut. false, ha kliens módban
	 */
	public boolean getServerMode(){
		return servermode;
	}
	
	/** Beálítja a felhasználó nevét
	 * 
	 * @param name
	 * A felhasználó neve
	 */
	public void setMyName(String name) {
		myName = name;
		
	}
	
	/**
	 * 
	 * @return myName
	 * a felhasználó neve
	 */
	public String getMyName() {
		return myName;
	}
	
	/** Beállítja a játékra jellemzõ paramétereket
	 * 
	 * @param pictureName
	 * A kép azonosítója
	 * @param resolution
	 * A tábla mérete
	 */
	public void setGameParameters(String pictureName, int resolution) {
		game.setPicturename(pictureName);
		game.setResolution(resolution);
	}
	
	/** Elkészíti a szervert, ahova csatlakozhatnak a kliensek
	 * 
	 * @throws IOException
	 * ObjectCastHelper által dobott kivételt dobja tovább
	 * @throws ClassNotFoundException
	 * ObjectCastHelper által dobott kivételt dobja tovább
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
	
	/** A kliens csatlakozik az általa választott játékmester szerveréhez
	 * 
	 * @param server 
	 * A szerver neve (A játékmester neve)
	 */
	public void joinServer(String server) {
		g.drawWaitForServerScreen();
		multi.joinGame(myName, server);
		startGame();
		
	}
	
	/**	Elindítja a játékot a beállított paraméterekkel (multi/single/image/size)
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
	
	/** A játék indítása elõtti 5mp-es számláló callback függvénye
	 * 
	 */
	protected void startTimerChanged(){
		String string = (game.getStartTime()>0)?""+game.getStartTime():"GO!";
		g.drawSideLowerScreen(string);
		
		if(game.getStartTime()==0){
				tableChanged();
				if(multiplayer){
					startSyncTimer();
				}
				
		}
	}
	
	/** Egy adott elemre való kattintás feldolgozására szolgáló függvény	
	 * 
	 * @param clickResult 
	 * Annak az elemnek a sorszáma amelyre a felhasználó kattintott
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
			g.drawBasicScreen();
			g.drawSideUpperPanel();
		}
		
		endScreenIfSomeoneWon();
		
		
	}
	
	/** A játéktér megváltozása esetén hívható függvény, amely újrarajzolja a táblát
	 * 
	 */
	protected void tableChanged() {
		g.makePanel(game.getTable());
	}
	
	/** A szervertõl kapott eltárolt pontok kirajzolása a képernyõre, és ellenõrzés, hogy van-e nyertes
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

	/** Vissza lép a fõmenübe
	 * 
	 */
	public void backToMainMenu() {
		waitingForPlayers=false;
		endScreenDrawn=true;
		g.drawSideUpperPanel();
		g.drawBasicScreen();
		game.startTimerCancel();
		
	}
	
	/** Leellenõrzi, hogy van-e nyertes
	 * 
	 * @return 
	 * A nyertes neve, vagy ""
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
	
	/** A Szervernek elküldi a saját pontszámot, 
	 *  és feldolgozza a válaszul kapott tömböt amelyben a többi játékos neve, és pontszáma szerepel.
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
	
	/** Elindít egy timert, ami 1mp-ként szinkronizálja a pontokat.
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
	
	/** Kirajzolja a játék vége képernyõt, ha valaki nyert
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
			g.drawSideLowerScreen(":)");
		}
	}

}

package control;
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
		game.init();
		//TODO: 5mp-ig l�tja a krakott k�pet
		game.mix(500);
	}
	
	public void clicked(int clickResult) {
		
		if(game.isSwappable(clickResult)){ //az ellen�rz�st lehet �tk�ne tenni a game objektumba
			game.swap(clickResult); 
		}
		
		g.makePanel(game.getTable());
		
		if(game.isFinished()){
			System.out.println("Finished");
			g.win();
		}
	}

}

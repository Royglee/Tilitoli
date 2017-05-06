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
		game.mix(500);
	}
	
	public void clicked(int clickResult) {
		
		if(game.isSwappable(clickResult)){ //az ellenõrzést lehet átkéne tenni a game objektumba
			game.swap(clickResult); 
		}
		
		g.makePanel(game.getTable());
		
		if(game.isFinished()){
			System.out.println("Finished");
		}
	}

}

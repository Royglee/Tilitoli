package gui;
import control.Controller;

public class Main {

	public static void main(String[] args) {
		Controller c = new Controller();
		Gui g = new Gui(c);
		c.setGUI(g);
		

	}

}

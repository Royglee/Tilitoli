package gui;
import control.Controller;

public class Main {

	public static void main(String[] args) {
		Controller c = new Controller();
		GUI g = new GUI(c);
		c.setGUI(g);
		

	}

}

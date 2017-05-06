package gui;
import control.controller;

public class Main {

	public static void main(String[] args) {
		controller c = new controller();
		GUI g = new GUI(c);
		c.setGUI(g);
		

	}

}

package gui;
import java.util.ArrayList;
import java.util.Vector;
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import control.Controller;

public class Gui extends JFrame {
	
	private static final long serialVersionUID = 1L;
	protected Controller c;
	public String nickName;
	public int clickResult;
	
	private ArrayList<BufferedImage> partImageList = new ArrayList<BufferedImage>();
	
	public static final int WIN_WIDTH = 1037;		//Ablak szélessége
	public static final int WIN_HEIGHT = 658;		//Ablak magassága
	
	private JPanel main;							//főpanel paraméterezés, kapcsolatlétesítés, játék
	private JPanel sideUpper;						//oldalsó felső panel user paraméterek kijelzésére
	private JPanel sideLower;						//oldalsó alsó, visszaszámlálás, egyéb instrukciók
	
	private JButton single = new JButton("SINGLE");		//felhasznált gombok
	private JButton multi = new JButton("MULTI");;
	private JButton server = new JButton("SERVER");
	private JButton client = new JButton("CLIENT");
	private JButton start = new JButton("START");
	private JButton connect = new JButton("CONNECT");
	private JButton create = new JButton ("CREATE SERVER");
	private JButton backToMenu = new JButton ("BACK TO MAIN MENU");
	private JButton refresh = new JButton ("REFRESH");
	
	private JTextField nickNameInputServerSingle;			//server/single nickname beviteli mező
	private JTextField nickNameInputClient;					//client nickname beviteli mező
	
	//legördülő lista elemek kép és felbontás választáshoz
	private String[] animalTitles = new String[] {"dog", "cat","elephant", "giraffe"};
	private String[] resolutionTitles = new String[] {"3x3", "4x4","5x5", "6x6","7x7","8x8","9x9","10x10"};
	
	//legördülő listák kép, felbontás és szerver választáshoz clienteknek
	private JComboBox<String> animalList = new JComboBox<>(animalTitles);
	private JComboBox<String> resolutionList = new JComboBox<>(resolutionTitles);
	private JComboBox<String> serverList = new JComboBox<>();
	
	/**
	 * Konstruktor
	 * @param c
	 * Adott játékos Controller osztálya
	 */
	public Gui(Controller c){
		this.c = c;
		setTitle("Tili-Toli");
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);	//Kilépés ablak x-re
		setSize(Gui.WIN_WIDTH,Gui.WIN_HEIGHT);
		setLayout(null);

		main = new JPanel();
		add(main);
		
		sideLower = new JPanel();
		add(sideLower);
		drawBasicScreen();
		
		sideUpper = new JPanel();
		add(sideUpper);
		drawSideUpperPanel();
		
		multi.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				c.setMultiMode(true);
				drawServerOrClientScreen();
			}
		});
		
		single.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				c.setMultiMode(false);
				c.setServerMode(false);
				drawParameterScreen();
			}
		});
		
		server.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				c.setServerMode(true);
				drawParameterScreen();
			}
		});
		
		client.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				c.listServers();
			}
		});
		
		start.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String resolutionString;
				String picturename;
				int resolution;
				picturename = animalList.getSelectedItem().toString();
				resolutionString = resolutionList.getSelectedItem().toString();
				resolution = Integer.parseInt(resolutionString.substring(0,resolutionString.indexOf("x")));
				c.setGameParameters(picturename, resolution);
				nickName = nickNameInputServerSingle.getText();
				c.setMyName(nickName);
				chopImage();
				
				
				main.addMouseListener(new MouseAdapter() {
					
					@Override
					public void mousePressed(MouseEvent e) {
						getclickResult(e.getX(),e.getY());
						c.clicked(clickResult);
					}
				});
				c.startGame();
			}
		});
		
		connect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				main.addMouseListener(new MouseAdapter() {

					@Override
					public void mousePressed(MouseEvent e) {
						getclickResult(e.getX(),e.getY());
						c.clicked(clickResult);
					}
				});
				nickName = nickNameInputClient.getText();
				String server = serverList.getSelectedItem().toString();
				c.setMyName(nickName);
				c.joinServer(server);
			}
		});
		
		create.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String resolutionString = resolutionList.getSelectedItem().toString();
				int resolution = Integer.parseInt(resolutionString.substring(0,resolutionString.indexOf("x")));
				c.setGameParameters(animalList.getSelectedItem().toString(), resolution);
				c.setMyName(nickNameInputServerSingle.getText());
				try {
					c.createGame();
				} catch (ClassNotFoundException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		backToMenu.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				c.backToMainMenu();
			}
		});
		
		refresh.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				c.listServers();
			}
		});
		
		setVisible(true);
		
	}
	/**
	 * Képek megnyitása, méret átskálázása, hozzáadás a main panelhez
	 * @param name
	 * Megnyitandó kép neve
	 */
	private void openImage(String name) {
		try {
			Image img = ImageIO.read(getClass().getResource("/pictures/"+name+".jpg"));
			img = img.getScaledInstance(150, 112, Image.SCALE_DEFAULT);
			JLabel label = new JLabel(new ImageIcon(img));
			main.add(label);
		}
		catch (IOException e1) {
			System.out.println(e1.getMessage());
		}
	}
	/**Kép felszeletelése részképekre megadott felbontása
	 * 
	 */
	public void chopImage(){
		partImageList.clear();
		Image img = null;
		int resolution = c.getGame().getResolution();
		String picturename = c.getGame().getPicturename();
		try {
			img = ImageIO.read(getClass().getResource("/pictures/"+picturename+".jpg"));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		img = img.getScaledInstance(800, 600, Image.SCALE_DEFAULT);
		BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		bufferedImage.getGraphics().drawImage(img, 0, 0, null);
		for(int i=0; i<resolution; i++){
			for(int j=0; j<resolution; j++){
				int x = j*(800/resolution);
				int y = i*(600/resolution);
				int w = 800/resolution;
				int h = 600/resolution;
				BufferedImage out= bufferedImage.getSubimage(x, y, w, h);
				partImageList.add(out);
			}
		}
	}
	/**Részképekből main panel (játéktér) újrarajzolása vektorban szereplő számok sorrendjében 
	 * 
	 * @param position
	 * Sorrendet tartalmazó vektor
	 */
	public void makePanel(Vector<Integer> position){
		Image blank = null;
		int resolution = c.getGame().getResolution();
		main.removeAll();
		main.setLayout(new GridLayout(resolution, resolution,1,1));
		for(int i=0; i<(resolution*resolution); i++){
			if(position.get(i)==0){
				try {
					blank = ImageIO.read(getClass().getResource("/pictures/black.jpg"));
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				blank = blank.getScaledInstance((800/resolution-2), (600/resolution-2), Image.SCALE_DEFAULT);
				JLabel part = new JLabel(new ImageIcon(blank));
				main.add(part);
			}
			else{
				Image image = partImageList.get(position.get(i)).getSubimage( 0, 0, (800/resolution-2), (600/resolution-2) );
				JLabel part = new JLabel(new ImageIcon(image));
				main.add(part);
			}
		}
		main.revalidate();
		main.repaint();
	}
	/**Részkép sorszámának visszaadása, amin kattintás történt
	 * 
	 * @param x_coordinate
	 * egér kattintás x koordinátája a JPanelen
	 * @param y_coordinate
	 * egér kattintás y koordinátája a JPanelen
	 */
	private void getclickResult(int x_coordinate,int y_coordinate){
		int w, h, cellwidth, cellheight, row_label, column_label;
		int resolution = c.getGame().getResolution();
		h = main.getHeight(); 
		w = main.getWidth();
		cellwidth = w/resolution;
		cellheight = h/resolution;
		column_label = x_coordinate/cellwidth;
		row_label = y_coordinate/cellheight;
		clickResult = (row_label*resolution)+column_label;
	}
	/**Main képernyő játék vége kép kirajzolása pirossal veszteseknek, zölddel nyerteseknek
	 * 
	 * @param isWinner
	 * az adott játékos nyertes
	 * @param winnerName
	 * nyertes játékos neve
	 */
	public void win(boolean isWinner, String winnerName){
		JLabel label;
		main.removeAll();
		main.setLayout(new GridBagLayout());
		if(isWinner){
			label = new JLabel("YOU WIN!");
			label.setForeground(Color.GREEN);
		}
		else{
			label = new JLabel("The winner is:"+winnerName);
			label.setForeground(Color.RED);
		}
		label.setFont(label.getFont().deriveFont(64f)); 
		main.add(label);
		main.revalidate();
		main.repaint();
	}
	/**Alpaphelyzetű képernyő kirajzolása
	 * 
	 */
	public void drawBasicScreen() {
		MouseListener[] mouseListeners = main.getMouseListeners();
		for (MouseListener mouseListener : mouseListeners) {
		    main.removeMouseListener(mouseListener);
		}
		JLabel label1 = new JLabel("Single or multi?");
		main.removeAll();
		main.setBounds(210, 10, 811, 611);
		main.setBorder(BorderFactory.createLineBorder(Color.black));
		main.setLayout(new FlowLayout());
		main.add(label1);
		main.add(single);
		main.add(multi);
		main.revalidate();
		main.repaint();
		
		sideLower.removeAll();
		sideLower.setBounds(10, 521, 190, 100);
		sideLower.setBorder(BorderFactory.createLineBorder(Color.black));
		sideLower.setLayout(new GridBagLayout());
		JLabel label = new JLabel("HELLO!");
		label.setForeground(Color.RED);
		label.setFont(label.getFont().deriveFont(35f));
		sideLower.add(label);
		sideLower.revalidate();
		sideLower.repaint();
	}
	/**Játék beállító képernyő kirajzolása server és single mód esetére
	 * 
	 */
	public void drawParameterScreen() {
		main.removeAll();
		main.setLayout(new GridLayout(5,2,10,10));
		JLabel nickName_ask = new JLabel("Enter your nickName!",SwingConstants.CENTER);
		main.add(nickName_ask);
		nickNameInputServerSingle = new JTextField("nickName");
		nickNameInputServerSingle.setHorizontalAlignment(JTextField.CENTER);
		main.add(nickNameInputServerSingle);
		if(c.getServerMode()){
			JLabel create_ask = new JLabel("Choose resolution and picture, then push CREATE to start server!",SwingConstants.CENTER);
			main.add(create_ask);
			main.add(create);
		}
		((JLabel)animalList.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		((JLabel)resolutionList.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		main.add(animalList);
		main.add(resolutionList);
		openImage("dog");
		openImage("cat");
		openImage("elephant");
		openImage("giraffe");
		if(!c.getServerMode()){
			JLabel single_start_instruction = new JLabel("After parameters and nickName chosen, you can START!",SwingConstants.CENTER);
			main.add(single_start_instruction);
			main.add(start);
		}
		main.revalidate();
		main.repaint();
	}
	/**Kliens paraméter és server választó képernyő kirajzolása
	 * 
	 * @param servers
	 * hálózaton létező serverek listája
	 */
	public void drawClientScreen(String[] servers) {
		serverList = new JComboBox<>(servers);
		main.removeAll();
		main.setLayout(new GridLayout(3,2,10,10));
		JLabel nickName_ask = new JLabel("Give your nickName",SwingConstants.CENTER);
		main.add(nickName_ask);
		nickNameInputClient = new JTextField("nickName");
		nickNameInputClient.setHorizontalAlignment(JTextField.CENTER);
		nickNameInputClient.setVisible(true);
		main.add(nickNameInputClient);
		if(servers.length ==0){
			JLabel empty_list = new JLabel("There is no reachable server!",SwingConstants.CENTER);
			main.add(empty_list);
		}
		else{
			((JLabel)serverList.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
			main.add(serverList);
		}
		main.add(connect);
		main.add(refresh);
		main.revalidate();
		main.repaint();
	}
	/**Multiplayer módban server vagy client mód választó képernyő kirajzolása
	 * 
	 */
	public void drawServerOrClientScreen() {
		JLabel label1 =new JLabel("Server or client?");
		main.removeAll();
		main.add(label1);
		main.add(server);
		main.add(client);
		main.revalidate();
		main.repaint();
	}
	/**Alsó oldalsó panel kis üzenetének frissítése
	 * 
	 * @param label
	 * kiírandó üzenet
	 */
	public void drawSideLowerScreen(String message){
		sideLower.removeAll();
		JLabel label = new JLabel(message);
		label.setForeground(Color.RED);
		label.setFont(label.getFont().deriveFont(35f));
		sideLower.add(label);
		sideLower.revalidate();
		sideLower.repaint();
	}
	/**Felső oldalsó panel kirajzolása csatlakozott játékban levő playerekkel
	 * 
	 * @param score
	 * pontállás lista
	 * @param playerList
	 * játékosnév lista
	 * @param min
	 * idő paraméter perc része
	 * @param second
	 * idő paraméter másodperc része
	 */
	public void drawScore(String[] score, String[] playerList, String min, String second) {
		sideUpper.removeAll();
		sideUpper.setBounds(10, 10, 190, 501);
		sideUpper.setBorder(BorderFactory.createLineBorder(Color.black));
		sideUpper.setLayout(new GridLayout(13,1));
		sideUpper.add(backToMenu);
		if(min != ""){
			JLabel time = new JLabel("Time:"+min+":"+second,SwingConstants.CENTER);
			time.setForeground(Color.GRAY);
			time.setFont(time.getFont().deriveFont(30f));
			sideUpper.add(time);
		}
		for(int i=0; i < playerList.length; i++){
			JLabel listelementplayer =new JLabel(playerList[i]);
			listelementplayer.setHorizontalAlignment(JTextField.CENTER);
			listelementplayer.setFont(listelementplayer.getFont().deriveFont(25f)); 
			sideUpper.add(listelementplayer);
			JLabel listelementscore =new JLabel(score[i]);
			listelementscore.setHorizontalAlignment(JTextField.CENTER);
			sideUpper.add(listelementscore);
		}
		sideUpper.revalidate();
		sideUpper.repaint();
	}
	
	/**Server mód create utáni clientekre várakozó panel kirajzolása
	 * 
	 * @param connectedCount
	 * csatlakozott clientek száma
	 */
	public void drawServerStartScreen(int connectedCount) {
		JLabel actualState;
		main.removeAll();
		main.setLayout(new GridLayout(10,1,10,10));
		main.add(start);
		JLabel server_start_instruction = new JLabel("Wait, while client are connect, then you can START!",SwingConstants.CENTER);
		main.add(server_start_instruction);
		if(connectedCount ==0){
			actualState = new JLabel("There are no any players, who connected!",SwingConstants.CENTER);
		}
		else{
			actualState = new JLabel("Number of connected players: "+connectedCount,SwingConstants.CENTER);
		}
		main.add(actualState);
		main.revalidate();
		main.repaint();
	}
	/**Felső oldalsó panel alaphelyzetének kirajzolása
	 * 
	 */
	public void drawSideUpperPanel() {
		sideUpper.removeAll();
		sideUpper.setBounds(10, 10, 190, 501);
		sideUpper.setBorder(BorderFactory.createLineBorder(Color.black));
		sideUpper.setLayout(new GridLayout(13,1));
		sideUpper.add(backToMenu);
		sideUpper.revalidate();
		sideUpper.repaint();
	}
	/**Client esetén csatlakozás utáni "várakozás serverre" panel kirajzolás
	 * 
	 */
	public void drawWaitForServerScreen(){
		JLabel label;
		main.removeAll();
		main.setLayout(new GridBagLayout());
		label = new JLabel("WAIT FOR SERVER START!");
		label.setForeground(Color.BLACK);
		label.setFont(label.getFont().deriveFont(64f)); 
		main.add(label);
		main.revalidate();
		main.repaint();
	}
	
}

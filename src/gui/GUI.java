package gui;
import java.util.ArrayList;
import java.util.Vector;
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import control.Controller;

public class GUI extends JFrame {
	
	private static final long serialVersionUID = 1L;
	protected Controller c;
	public String nickname;
	public int clickResult;
	
	ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
	
	public static int winwidth;		//Ablak szélessége
	public static int winheight;	//Ablak magassága
	
	JPanel main;
	JPanel sideupper;
	JPanel sidelower;
	JButton single = new JButton("SINGLE");
	JButton multi = new JButton("MULTI");;
	JButton server = new JButton("SERVER");
	JButton client = new JButton("CLIENT");
	JButton start = new JButton("START");
	JButton connect = new JButton("CONNECT");
	JButton create = new JButton ("CREATE SERVER");
	JLabel single_or_multi;
	JLabel server_or_client;
	JLabel start_instruction;
	JLabel nickname_ask;
	JLabel you_win;
	JLabel sidelower_label;
	JLabel create_ask;
	JTextField nickname_input_server_single;
	JTextField nickname_input_client;
	String[] numberTitles = new String[] {"dog", "cat","elephant", "giraffe"};
	String[] resolutionTitles = new String[] {"3x3", "4x4","5x5", "6x6","7x7","8x8","9x9","10x10"};

	JComboBox<String> animalList = new JComboBox<>(numberTitles);
	JComboBox<String> resolutionList = new JComboBox<>(resolutionTitles);
	JComboBox<String> serverList = new JComboBox<>();
	
	public GUI(Controller c){
		
		this.c = c;
		
		GUI.winwidth = 1037;
		GUI.winheight = 658;
		setTitle("Tili-Toli");
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);	//Kilépés ablak x-re
		setSize(GUI.winwidth,GUI.winheight);
		setLayout(null);

		main = new JPanel();
		add(main);
		sidelower = new JPanel();
		add(sidelower);
		drawMainScreen();
		
		sideupper = new JPanel();
		sideupper.setBounds(10, 10, 190, 501);
		sideupper.setBorder(BorderFactory.createLineBorder(Color.black));
		sideupper.setLayout(new FlowLayout());
		add(sideupper);
		
		
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
				
				nickname = nickname_input_server_single.getText();
				chopImage();
	
				c.startGame();
				
				
				main.addMouseListener(new MouseAdapter() {

					@Override
					public void mousePressed(MouseEvent e) {
						getclickResult(e.getX(),e.getY());
						c.clicked(clickResult);
					}
				});
				
			}
		});
		
		connect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				nickname = nickname_input_client.getText();
				String server = serverList.getSelectedItem().toString();
				c.joinServer(server);
				
			}
		});
		
		create.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String resolutionString = resolutionList.getSelectedItem().toString();
				int resolution = Integer.parseInt(resolutionString.substring(0,resolutionString.indexOf("x")));
				
				c.setGameParameters(animalList.getSelectedItem().toString(), resolution);
				
				c.setMyName(nickname_input_server_single.getText());
				try {
					c.createGame();
				} catch (ClassNotFoundException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		setVisible(true);
		
	}

	private void openImage(String name) {
		try {
			Image img = ImageIO.read(getClass().getResource("/pictures/"+name+".jpg"));
			img = img.getScaledInstance(150, 112, Image.SCALE_DEFAULT);
			JLabel label = new JLabel(new ImageIcon(img));
			main.add(label);
		} catch (IOException e1) {
			System.out.println(e1.getMessage());
		}
	}
	
	public void chopImage(){
		
		images.clear();
		Image img = null;
		int resolution = c.getGame().getResolution();
		String picturename = c.getGame().getPicturename();
		try {
			img = ImageIO.read(getClass().getResource("/pictures/"+picturename+".jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		img = img.getScaledInstance(800, 600, Image.SCALE_DEFAULT);
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		bimage.getGraphics().drawImage(img, 0, 0, null);
		for(int i=0; i<resolution; i++){
			for(int j=0; j<resolution; j++){
				int x = j*(800/resolution);
				int y = i*(600/resolution);
				int w = 800/resolution;
				int h = 600/resolution;
				BufferedImage out= bimage.getSubimage(x, y, w, h);
				images.add(out);
			}
		}
	}
	
	public void makePanel(Vector<Integer> position){
		
		Image blank = null;
		int resolution = c.getGame().getResolution();
		
		main.removeAll();
		main.setLayout(new GridLayout(resolution, resolution,1,1));
		main.setVisible(true);
		for(int i=0; i<(resolution*resolution); i++){
			if(position.get(i)==0){
				try {
					blank = ImageIO.read(getClass().getResource("/pictures/black.jpg"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				blank = blank.getScaledInstance((800/resolution-2), (600/resolution-2), Image.SCALE_DEFAULT);
				JLabel part = new JLabel(new ImageIcon(blank));
				main.add(part);
			}
			else{
				Image image = images.get(position.get(i)).getSubimage( 0, 0, (800/resolution-2), (600/resolution-2) );
				JLabel part = new JLabel(new ImageIcon(image));
				main.add(part);
			}
		}
		main.revalidate();
		main.repaint();
		
	}
	
	private void getclickResult(int x_coordinate,int y_coordinate){
		
		int w, h, cellwidth, cellheight, row_number, column_number;
		int resolution = c.getGame().getResolution();
		h = main.getHeight(); 
		w = main.getWidth();
		cellwidth = w/resolution;
		cellheight = h/resolution;
		column_number = x_coordinate/cellwidth;
		row_number = y_coordinate/cellheight;
		clickResult = (row_number*resolution)+column_number;
		
	}
	
	public void win(){
		main.removeAll();
		main.setLayout(new GridBagLayout());
		you_win = new JLabel("YOU WIN!");
		you_win.setForeground(Color.GREEN);
		you_win.setFont(you_win.getFont().deriveFont(64f)); 
		main.add(you_win);
		main.revalidate();
		main.repaint();
	}
	
	public void drawMainScreen() {
		main.removeAll();
		
		main.setBounds(210, 10, 811, 611);
		main.setBorder(BorderFactory.createLineBorder(Color.black));
		main.setLayout(new FlowLayout());
		main.setVisible(true);
		single_or_multi = new JLabel("Single or multi?");
		single_or_multi.setVisible(true);
		main.add(single_or_multi);
		single.setVisible(true);
		main.add(single);
		multi.setVisible(true);
		main.add(multi);
		
		main.revalidate();
		main.repaint();
		
		sidelower.removeAll();
		
		sidelower.setBounds(10, 521, 190, 100);
		sidelower.setBorder(BorderFactory.createLineBorder(Color.black));
		sidelower.setLayout(new GridBagLayout());
		sidelower_label = new JLabel("HELLO!");
		sidelower_label.setForeground(Color.RED);
		sidelower_label.setFont(sidelower_label.getFont().deriveFont(35f));
		sidelower.add(sidelower_label);
		
		sidelower.revalidate();
		sidelower.repaint();
	}
	
	public void drawParameterScreen() {
		main.removeAll();
		
		if(c.getServerMode()){
			main.setLayout(new GridLayout(6,2,10,10));
		}
		else{
			main.setLayout(new GridLayout(5,2,10,10));
		}
		nickname_ask = new JLabel("Enter your nickname!",SwingConstants.CENTER);
		nickname_ask.setVisible(true);
		main.add(nickname_ask);
		nickname_input_server_single = new JTextField("NICKNAME");
		nickname_input_server_single.setHorizontalAlignment(JTextField.CENTER);
		nickname_input_server_single.setVisible(true);
		main.add(nickname_input_server_single);
		if(c.getServerMode()){
			create_ask = new JLabel("Choose resolution and picture, then push CREATE to start server!",SwingConstants.CENTER);
			create_ask.setVisible(true);
			main.add(create_ask);
			create.setVisible(true);
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
		if(c.getServerMode()){
			start_instruction = new JLabel("If clients are connected, then you can START game!",SwingConstants.CENTER);
		}
		else{
			start_instruction = new JLabel("After the parameters are choosen, push START!",SwingConstants.CENTER);
		}
		start_instruction.setVisible(true);
		main.add(start_instruction);
		
		start.setVisible(true);
		main.add(start);
		
		main.revalidate();
		main.repaint();
	}
	
	public void drawClientScreen(String[] servers) {
		
		serverList = new JComboBox<>(servers);
		
		main.removeAll();
		
		main.setLayout(new GridLayout(2,2,10,10));
		nickname_ask = new JLabel("Give your nickname",SwingConstants.CENTER);
		nickname_ask.setVisible(true);
		main.add(nickname_ask);
		nickname_input_client = new JTextField("NICKNAME");
		nickname_input_client.setHorizontalAlignment(JTextField.CENTER);
		nickname_input_client.setVisible(true);
		main.add(nickname_input_client);
		
		((JLabel)serverList.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		main.add(serverList);
		
		connect.setVisible(true);
		main.add(connect);
		
		main.revalidate();
		main.repaint();
	}
	
	public void drawServerOrClientScreen() {
		main.removeAll();
		
		server_or_client = new JLabel("Server or client?");
		server_or_client.setVisible(true);
		main.add(server_or_client);
		server.setVisible(true);
		main.add(server);
		client.setVisible(true);
		main.add(client);
		
		main.revalidate();
		main.repaint();
	}
	
	public void countBack(String number){
		sidelower.removeAll();
		
		sidelower_label = new JLabel(number);
		sidelower_label.setForeground(Color.RED);
		sidelower_label.setFont(sidelower_label.getFont().deriveFont(35f));
		sidelower.add(sidelower_label);
		
		sidelower.revalidate();
		sidelower.repaint();
	}

	public void drawLobby() {
		
	}
	
}

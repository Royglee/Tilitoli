package tilitoli;

import java.io.*;
import java.net.*;
import java.util.*;

/**Játék alatt létrehozott TCP kapcsolatokat és kommunikációt kezelõ osztály.
 * @author Tarjányi Péter
 *
 */
public class ConnectorModule implements Runnable
{
	private static final int TCP_PORT = 55556;

	private ServerSocket server;
	private Socket client;
	private Thread connectionThread;
	
	private boolean enableAccepting;
	private boolean serverRunning;
	private boolean clientRunning;
	private List<Socket> clients;
	private int nextSocket;
	private Scores playerScores;
	private boolean puzzleDeployed;
	private Puzzle currentPuzzle;
	
	/** A kapcsolatok kezeléséhez szükséges szál.
	 */
	@Override
	public void run() {
		while (serverRunning)
		{
			if (enableAccepting)
			{
				AcceptNewConnection();
			}else if (clients.size()>0)
			{
				if (puzzleDeployed)
				{
					ProcessNextSocket();
				}else
				{
					DeployPuzzle();
					puzzleDeployed = true;
				}
			}
		}
		while (clientRunning && !puzzleDeployed)
		{
			ReceivePuzzle();
		}
	}
	
	/**Fogadja a szervertõl akpott puzzle-t, és tárolja.
	 */
	private void ReceivePuzzle()
	{
		try
		{
			ObjectInputStream iS = new ObjectInputStream(client.getInputStream());
			currentPuzzle = (Puzzle)iS.readObject();
			puzzleDeployed = true;
		}catch (SocketTimeoutException e)
		{
			puzzleDeployed = false;
		}
		catch (Exception e)
		{
			puzzleDeployed = false;
			clientRunning = false;
			System.out.println(e.toString());
		}
	}
	
	/**Fogadja az új játékosokat.
	 */
	private void AcceptNewConnection()
	{
		if (server != null)
		{
			try
			{
				Socket newClient = server.accept();
				newClient.setKeepAlive(true);
				newClient.setSoTimeout(1000);
				clients.add(newClient);
				System.out.println("New player!");
			}catch (Exception e)
			{
				//Should we do anything?
			}
		}
	}
	
	/**Round robin elven megy körbe körbe a csatlakozott klienseken és kezeli azok pontjainak szinkronizálását.
	 */
	private void ProcessNextSocket()
	{
		try
		{
			nextSocket = (++nextSocket)==clients.size()?0:nextSocket;
			Socket actual = clients.get(nextSocket);	//ritka fos láncolt lista, ahol indexelni kell és nincs next object... Vajon mitõl láncolt akkor?!
			ObjectInputStream iS = new ObjectInputStream(actual.getInputStream());
			ObjectOutputStream oS = new ObjectOutputStream(actual.getOutputStream());
			Scores inputScore = (Scores)iS.readObject();
			synchronized (playerScores)
			{
				playerScores.MergeScores(inputScore);
				oS.writeObject(playerScores);
				oS.flush();
			}
		}catch (Exception e)
		{
			System.out.println(e.toString());
		}
	}
	
	/**Megpróbálja a csatlakozott játékosoknak elküldeni a játék keverést.
	 */
	private void DeployPuzzle()
	{
		try
		{
			for (Socket s :clients)
			{
				ObjectOutputStream oS = new ObjectOutputStream(s.getOutputStream());
				oS.writeObject(currentPuzzle);
				oS.flush();
			}
		}catch (Exception e)
		{
			//Dunno
		}
	}
	
	/**Konsturktor.
	 */
	public ConnectorModule()
	{
		connectionThread = new Thread(this);
		playerScores = new Scores();
		clientRunning = false;
		serverRunning = false;
	}
	
	/** Elindítja a szerver módot. Várja a játékosok csatlakozását és ha ezt letiltjuk, szétosztja a puzzle-t, és elindul a játék.
	 * @param puzzle
	 * Szétosztandó játéktér.
	 * @return
	 * False ha nem tud elindulni vagy kliens módban vagyunk, true ha a játék elindult.
	 */
	public boolean StartGameServer(Puzzle puzzle)
	{
		if (puzzle != null && server == null)
		{
			try
			{
				clients = new LinkedList<Socket>();
				server = new ServerSocket(TCP_PORT);
				server.setSoTimeout(100);
				currentPuzzle = puzzle;
				serverRunning = true;
				EnableNewConnections();
				connectionThread.start();
				return true;
			}catch (Exception e)
			{
				serverRunning = false;
			}
		}
		return false;
	}
	
	/**Tiszta lappal kezdi fogadni az új játékosokat.
	 */
	private void EnableNewConnections()
	{
		if (server != null)
		{
			CleanUpConnections();
			enableAccepting = true;
		}
	}
	
	/**Lezárja a nyitott kapcsolatokat és törli azokat.
	 */
	private void CleanUpConnections()
	{
		if (clients != null && clients.size()>0)
		{
			for (Socket s : clients)
			{
				try
				{
					s.close();
				}catch (Exception e)
				{
					//No problem, we are closing everithing.
				}
			}
			clients.clear();
		}
	}
	
	/**Leállítja a további játékosok fogadását, és elindul a játék.
	 */
	public void DisableNewConnections()
	{
		enableAccepting = false;
		nextSocket=-1;
		puzzleDeployed = false;
	}
	
	/**Leállítja a server üzemmódot. Blokkol amíg le nem áll.
	 * @return
	 * true ha leállt, false ha nem is ment a szerver mód.
	 */
	public boolean StopGameServer()
	{
		if (serverRunning)
		{
			serverRunning = false;
			while (connectionThread.isAlive())
			{
				try
				{
					Thread.sleep(1);
				}catch (InterruptedException e)
				{
					//Do nothing, "while" goes on and on
				}
			}
			CleanUpConnections();
			server = null;
			return true;
		}
		return false;
	}
	
	/**A megadott ip címen lévõ játékhoz megpróbál csatlakozni.
	 * @param address
	 * Cél ip cím
	 * @return
	 * false ha nem sikerült csatlakozni, true ha csatlakozott.
	 */
	public boolean JoinGame(InetAddress address)
	{
		if (!serverRunning && !clientRunning && !connectionThread.isAlive())
		{
			try
			{
				client = new Socket(address, TCP_PORT);
				client.setSoTimeout(5000);
				clientRunning = true;
				puzzleDeployed = false;
				connectionThread.start();
				return true;
			}catch (Exception e)
			{
				System.out.println(e.toString());
				clientRunning = false;
				return false;
			}
		}
		return false;
	}
	
	/**Kilép a csatlakotott játékból. Blokkol, amíg ki nem léptünk.
	 * @return
	 * true ha kiléptünk, false ha nem is voltunk játékban
	 */
	public boolean LeaveGame()
	{
		if (clientRunning)
		{
			clientRunning = false;
			try
			{
				while (connectionThread.isAlive())
				{
					Thread.sleep(1);
				}
				client.close();
			}catch (Exception e)
			{
				//no problem
			}
			client = null;
			return true;
		}
		return false;
	}

	/**Vissza adja a játékosok eredményeit, és elküldi a többieknek a saját eredményeket. Kliens esetán blokkol amíg meg nem jön a szervertõl a válasz. 
	 * @param name
	 * Saját név.
	 * @param score
	 * Saját pontok.
	 * @return
	 * Minden játékos eredménye, vagy null, ha nem fut semmi
	 */
	public Scores GetPlayerScores(String name, Integer score)
	{
		
			if (clientRunning)
			{
				if (puzzleDeployed)
				{
					playerScores = SyncScore(name, score);
				}
				else
				{
					return null;
				}
			}else if (serverRunning)
			{
				playerScores.WriteScore(name, score);
				return playerScores;
			}
			return null;
	}
	
	/**Szinkronizálja a pontokat. Elküldi a sajátot a szervernek, és várja amíg vissza nem kapja a többiekét is.
	 * @param name
	 * Saját neve.
	 * @param score
	 * Saját pontszám
	 * @return
	 * Minden játékos eredménye a szervertõl.
	 */
	private Scores SyncScore(String name, Integer score)
	{
		Scores ego = new Scores(name, score);
		try
		{
			ObjectOutputStream oS = new ObjectOutputStream(client.getOutputStream());
			ObjectInputStream iS = new ObjectInputStream(client.getInputStream());
			oS.writeObject(ego);
			ego = (Scores)iS.readObject();
		}catch (Exception e)
		{
			System.out.println(e.toString());
			//Something wrong, so return ego data.
		}
		return ego; 
	}
	
	/**Megadja az aktuális játékteret. Kliens esetén blokkol amíg meg nem kapja a szervertõl, tehát amíg el nem indul a játék.
	 * @return
	 * Kliens: szervertõl akpott játéktér Szerver: szétosztott játéktér. 
	 */
	public Puzzle GetPuzzle()
	{
		while (!puzzleDeployed && clientRunning)
		{
			try
			{
				Thread.sleep(1);
			}catch (InterruptedException e)
			{
				//Do nothing, "while" goes on and on
			}
		}
		return puzzleDeployed?currentPuzzle:null;
	}
	
}

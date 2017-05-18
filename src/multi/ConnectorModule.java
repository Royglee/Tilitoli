package multi;

import java.io.*;
import java.net.*;
import java.util.*;

/**Játék alatt létrehozott TCP kapcsolatokat és kommunikációt kezelő osztály.
 * @author Tarjányi Péter
 *
 */
public class ConnectorModule implements Runnable
{
	private enum Mode {none, server, client};
	
	private static final int TCP_PORT = 55556;

	private ServerSocket server;
	private Socket client;
	private Thread connectionThread;
	private Mode mode;
	
	private boolean enableAccepting;
	private boolean serverRunning;
	private boolean clientRunning;
	private List<NetworkClient> clients;
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
				acceptNewConnection();
			}else
			{
				if (clients.size()>0)
				{
					for (NetworkClient c : clients)
					{
						c.start();
					}
				}
				serverRunning = false;
			}
		}
		while (clientRunning && !puzzleDeployed)
		{
			receivePuzzle();
		}
	}
	
	/**Fogadja a szervertől akpott puzzle-t, és tárolja.
	 */
	private void receivePuzzle()
	{
		try
		{
			ObjectInputStream iS = new ObjectInputStream(client.getInputStream());
			SyncObject sync = (SyncObject)iS.readObject();
			currentPuzzle = sync.puzzle;
			puzzleDeployed = true;
			System.out.println("puzzle deployed");
		}catch (SocketTimeoutException e)
		{
			puzzleDeployed = false;
			//System.out.println("deploy:"+e.toString());
		}
		catch (Exception e)
		{
			puzzleDeployed = false;
			clientRunning = false;
			System.out.println("deploy2:"+e.toString());
		}
	}
	
	/**Fogadja az új játékosokat.
	 */
	private void acceptNewConnection()
	{
		if (server != null)
		{
			try
			{
				NetworkClient newClient = new NetworkClient(server.accept());
				clients.add(newClient);
				System.out.println("New player!");
			}catch (Exception e)
			{
				//Should we do anything?
			}
		}
	}
	
	/**Konsturktor.
	 */
	public ConnectorModule()
	{
		mode = Mode.none;
		connectionThread = new Thread(this);
		clientRunning = false;
		serverRunning = false;
	}
	
	/** Elindítja a szerver módot. Várja a játékosok csatlakozását és ha ezt letiltjuk, szétosztja a puzzle-t, és elindul a játék.
	 * @param puzzle
	 * Szétosztandó játéktér.
	 * @return
	 * False ha nem tud elindulni vagy kliens módban vagyunk, true ha a játék elindult.
	 */
	public boolean startGameServer(Puzzle puzzle)
	{
		if (mode == Mode.none)
		{
			if (puzzle != null && server == null)
			{
				try
				{
					clients = new LinkedList<NetworkClient>();
					server = new ServerSocket(TCP_PORT);
					server.setReuseAddress(true);
					server.setSoTimeout(100);
					NetworkClient.setPuzzle(puzzle);
					NetworkClient.setScores(new Scores());
					serverRunning = true;
					mode = Mode.server;
					enableNewConnections();
					connectionThread.start();
					return true;
				}catch (Exception e)
				{
					serverRunning = false;
					mode = Mode.none;
				}
			}
		}		
		return false;
	}
	
	/**Tiszta lappal kezdi fogadni az új játékosokat.
	 */
	private void enableNewConnections()
	{
		if (server != null)
		{
			cleanUpConnections();
			enableAccepting = true;
		}
	}
	
	/**Lezárja a nyitott kapcsolatokat és törli azokat.
	 */
	private void cleanUpConnections()
	{
		if (clients != null && clients.size()>0)
		{
			for (NetworkClient c : clients)
			{
				c.close();
			}
			clients.clear();
		}
	}
	
	/**Leállítja a további játékosok fogadását, és elindul a játék.
	 */
	public void disableNewConnections()
	{
		enableAccepting = false;
	}
	
	/**Leállítja a server üzemmódot. Blokkol amíg le nem áll.
	 * @return
	 * true ha leállt, false ha nem is ment a szerver mód.
	 */
	public boolean stopGameServer()
	{
		if (mode == Mode.server)
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
			cleanUpConnections();
			try
			{
				if (!server.isClosed())
				{
					server.close();
				}
			}catch (Exception e)
			{
				//
			}
			server = null;
			mode = Mode.none;
			return true;
		}else return false;
	}
	
	/**A megadott ip címen lévő játékhoz megpróbál csatlakozni.
	 * @param myName
	 * A csatalkozó játékos neve.
	 * @param address
	 * Cél ip cím
	 * @return
	 * false ha nem sikerült csatlakozni, true ha csatlakozott.
	 */
	public boolean joinGame(String myName, InetAddress address)
	{
		if (mode == Mode.none)
		{
			try
			{
				mode = Mode.client;
				client = new Socket(address, TCP_PORT);
				client.setSoTimeout(2000);
				clientRunning = true;
				ObjectOutputStream oS = new ObjectOutputStream(client.getOutputStream());
				oS.writeObject(new Scores(myName,0));
				puzzleDeployed = false;
				connectionThread.start();
				return true;
			}catch (Exception e)
			{
				clientRunning = false;
				mode = Mode.none;
			}
		}
		return false;
	}
	
	/**Kilép a csatlakotott játékból. Blokkol, amíg ki nem léptünk.
	 * @return
	 * true ha kiléptünk, false ha nem is voltunk játékban
	 */
	public boolean leaveGame()
	{
		if (mode == Mode.client)
		{
			clientRunning = false;
			try
			{
				while (connectionThread.isAlive())
				{
					Thread.sleep(1);
				}
			}catch (Exception e)
			{
				//no problem
			}
			if (!client.isClosed())
			{
				try
				{
					client.close();
				}catch (Exception e)
				{
					//
				}
			}
			client = null;
			mode = Mode.none;
			return true;
		}else return false;
	}

	/**Vissza adja a játékosok eredményeit, és elküldi a többieknek a saját eredményeket. Kliens esetán blokkol amíg meg nem jön a szervertől a válasz. 
	 * @param name
	 * Saját név.
	 * @param score
	 * Saját pontok.
	 * @return
	 * Minden játékos eredménye, vagy null, ha nem fut semmi
	 */
	public Scores getPlayerScores(String name, Integer score)
	{
		if (mode == Mode.client)
		{
			return puzzleDeployed?syncScore(name, score):null;
		}else if (mode == Mode.server)
		{
			NetworkClient.setScores(new Scores(name, score));
			return NetworkClient.getScores();
		}
		else return null;
	}
	
	/**Szinkronizálja a pontokat. Elküldi a sajátot a szervernek, és várja amíg vissza nem kapja a többiekét is.
	 * @param name
	 * Saját neve.
	 * @param score
	 * Saját pontszám
	 * @return
	 * Minden játékos eredménye a szervertől.
	 */
	private Scores syncScore(String name, Integer score)
	{
		Scores ego = new Scores(name, score);
		try
		{
			SyncObject sync = new SyncObject(ego, null);
			ObjectOutputStream oS = new ObjectOutputStream(client.getOutputStream());
			oS.writeObject(sync);
			System.out.println("Sent");
			ObjectInputStream iS = new ObjectInputStream(client.getInputStream());
			sync = (SyncObject)iS.readObject();
			ego = sync.scores;
			System.out.println("Get");
		}catch (Exception e)
		{
			System.out.println("sync: "+e.toString());
			//Something wrong, so return ego data.
		}
		return ego; 
	}
	
	/**Megadja az aktuális játékteret. Kliens esetén blokkol amíg meg nem kapja a szervertől, tehát amíg el nem indul a játék.
	 * @return
	 * Kliens: szervertől akpott játéktér Szerver: szétosztott játéktér. 
	 */
	public Puzzle getPuzzle()
	{
		while (!puzzleDeployed && mode == Mode.client)
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

	/**Megmondja, hány TCP kapcsolat van már.
	 * @return
	 * Elfogadott TCP kapcsolatok száma.
	 */
	public Integer getConnectionCount()
	{
		return clients.size();
	}
}

package tilitoli;

import java.io.*;
import java.net.*;
import java.util.*;

/**J�t�k alatt l�trehozott TCP kapcsolatokat �s kommunik�ci�t kezel� oszt�ly.
 * @author Tarj�nyi P�ter
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
	
	/** A kapcsolatok kezel�s�hez sz�ks�ges sz�l.
	 */
	@Override
	public void run() {
		while (serverRunning)
		{
			if (enableAccepting)
			{
				AcceptNewConnection();
			}else
			{
				if (clients.size()>0)
				{
					for (NetworkClient c : clients)
					{
						c.Start();
					}
				}
				serverRunning = false;
			}
		}
		while (clientRunning && !puzzleDeployed)
		{
			ReceivePuzzle();
		}
	}
	
	/**Fogadja a szervert�l akpott puzzle-t, �s t�rolja.
	 */
	private void ReceivePuzzle()
	{
		try
		{
			ObjectInputStream iS = new ObjectInputStream(client.getInputStream());
			SyncObject sync = (SyncObject)iS.readObject();
			currentPuzzle = sync.puzzle;
			puzzleDeployed = true;
		}catch (SocketTimeoutException e)
		{
			puzzleDeployed = false;
			System.out.println(e.toString());
		}
		catch (Exception e)
		{
			puzzleDeployed = false;
			clientRunning = false;
			System.out.println(e.toString());
		}
	}
	
	/**Fogadja az �j j�t�kosokat.
	 */
	private void AcceptNewConnection()
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
	
	/** Elind�tja a szerver m�dot. V�rja a j�t�kosok csatlakoz�s�t �s ha ezt letiltjuk, sz�tosztja a puzzle-t, �s elindul a j�t�k.
	 * @param puzzle
	 * Sz�tosztand� j�t�kt�r.
	 * @return
	 * False ha nem tud elindulni vagy kliens m�dban vagyunk, true ha a j�t�k elindult.
	 */
	public boolean StartGameServer(Puzzle puzzle)
	{
		if (mode == Mode.none)
		{
			if (puzzle != null && server == null)
			{
				try
				{
					clients = new LinkedList<NetworkClient>();
					server = new ServerSocket(TCP_PORT);
					server.setSoTimeout(100);
					NetworkClient.SetPuzzle(puzzle);
					NetworkClient.SetScores(new Scores());
					serverRunning = true;
					mode = Mode.server;
					EnableNewConnections();
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
	
	/**Tiszta lappal kezdi fogadni az �j j�t�kosokat.
	 */
	private void EnableNewConnections()
	{
		if (server != null)
		{
			CleanUpConnections();
			enableAccepting = true;
		}
	}
	
	/**Lez�rja a nyitott kapcsolatokat �s t�rli azokat.
	 */
	private void CleanUpConnections()
	{
		if (clients != null && clients.size()>0)
		{
			for (NetworkClient c : clients)
			{
				c.Close();
			}
			clients.clear();
		}
	}
	
	/**Le�ll�tja a tov�bbi j�t�kosok fogad�s�t, �s elindul a j�t�k.
	 */
	public void DisableNewConnections()
	{
		enableAccepting = false;
	}
	
	/**Le�ll�tja a server �zemm�dot. Blokkol am�g le nem �ll.
	 * @return
	 * true ha le�llt, false ha nem is ment a szerver m�d.
	 */
	public boolean StopGameServer()
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
			CleanUpConnections();
			server = null;
			mode = Mode.none;
			return true;
		}else return false;
	}
	
	/**A megadott ip c�men l�v� j�t�khoz megpr�b�l csatlakozni.
	 * @param address
	 * C�l ip c�m
	 * @return
	 * false ha nem siker�lt csatlakozni, true ha csatlakozott.
	 */
	public boolean JoinGame(InetAddress address)
	{
		if (mode == Mode.none)
		{
			try
			{
				mode = Mode.client;
				client = new Socket(address, TCP_PORT);
				client.setSoTimeout(2000);
				clientRunning = true;
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
	
	/**Kil�p a csatlakotott j�t�kb�l. Blokkol, am�g ki nem l�pt�nk.
	 * @return
	 * true ha kil�pt�nk, false ha nem is voltunk j�t�kban
	 */
	public boolean LeaveGame()
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
				client.close();
			}catch (Exception e)
			{
				//no problem
			}
			client = null;
			mode = Mode.none;
			return true;
		}else return false;
	}

	/**Vissza adja a j�t�kosok eredm�nyeit, �s elk�ldi a t�bbieknek a saj�t eredm�nyeket. Kliens eset�n blokkol am�g meg nem j�n a szervert�l a v�lasz. 
	 * @param name
	 * Saj�t n�v.
	 * @param score
	 * Saj�t pontok.
	 * @return
	 * Minden j�t�kos eredm�nye, vagy null, ha nem fut semmi
	 */
	public Scores GetPlayerScores(String name, Integer score)
	{
		if (mode == Mode.client)
		{
			return puzzleDeployed?SyncScore(name, score):null;
		}else if (mode == Mode.server)
		{
			NetworkClient.SetScores(new Scores(name, score));
			return NetworkClient.GetScores();
		}
		else return null;
	}
	
	/**Szinkroniz�lja a pontokat. Elk�ldi a saj�tot a szervernek, �s v�rja am�g vissza nem kapja a t�bbiek�t is.
	 * @param name
	 * Saj�t neve.
	 * @param score
	 * Saj�t pontsz�m
	 * @return
	 * Minden j�t�kos eredm�nye a szervert�l.
	 */
	private Scores SyncScore(String name, Integer score)
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
			System.out.println(e.toString());
			//Something wrong, so return ego data.
		}
		return ego; 
	}
	
	/**Megadja az aktu�lis j�t�kteret. Kliens eset�n blokkol am�g meg nem kapja a szervert�l, teh�t am�g el nem indul a j�t�k.
	 * @return
	 * Kliens: szervert�l akpott j�t�kt�r Szerver: sz�tosztott j�t�kt�r. 
	 */
	public Puzzle GetPuzzle()
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

	/**Megmondja, h�ny TCP kapcsolat van m�r.
	 * @return
	 * Elfogadott TCP kapcsolatok sz�ma.
	 */
	public Integer GetConnectionCount()
	{
		return clients.size();
	}
}

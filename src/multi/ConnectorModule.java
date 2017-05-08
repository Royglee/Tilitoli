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
	
	/** A kapcsolatok kezel�s�hez sz�ks�ges sz�l.
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
	
	/**Fogadja a szervert�l akpott puzzle-t, �s t�rolja.
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
	
	/**Fogadja az �j j�t�kosokat.
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
	
	/**Round robin elven megy k�rbe k�rbe a csatlakozott klienseken �s kezeli azok pontjainak szinkroniz�l�s�t.
	 */
	private void ProcessNextSocket()
	{
		try
		{
			nextSocket = (++nextSocket)==clients.size()?0:nextSocket;
			Socket actual = clients.get(nextSocket);	//ritka fos l�ncolt lista, ahol indexelni kell �s nincs next object... Vajon mit�l l�ncolt akkor?!
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
	
	/**Megpr�b�lja a csatlakozott j�t�kosoknak elk�ldeni a j�t�k kever�st.
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
	
	/** Elind�tja a szerver m�dot. V�rja a j�t�kosok csatlakoz�s�t �s ha ezt letiltjuk, sz�tosztja a puzzle-t, �s elindul a j�t�k.
	 * @param puzzle
	 * Sz�tosztand� j�t�kt�r.
	 * @return
	 * False ha nem tud elindulni vagy kliens m�dban vagyunk, true ha a j�t�k elindult.
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
	
	/**Le�ll�tja a tov�bbi j�t�kosok fogad�s�t, �s elindul a j�t�k.
	 */
	public void DisableNewConnections()
	{
		enableAccepting = false;
		nextSocket=-1;
		puzzleDeployed = false;
	}
	
	/**Le�ll�tja a server �zemm�dot. Blokkol am�g le nem �ll.
	 * @return
	 * true ha le�llt, false ha nem is ment a szerver m�d.
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
	
	/**A megadott ip c�men l�v� j�t�khoz megpr�b�l csatlakozni.
	 * @param address
	 * C�l ip c�m
	 * @return
	 * false ha nem siker�lt csatlakozni, true ha csatlakozott.
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
	
	/**Kil�p a csatlakotott j�t�kb�l. Blokkol, am�g ki nem l�pt�nk.
	 * @return
	 * true ha kil�pt�nk, false ha nem is voltunk j�t�kban
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
	
	/**Megadja az aktu�lis j�t�kteret. Kliens eset�n blokkol am�g meg nem kapja a szervert�l, teh�t am�g el nem indul a j�t�k.
	 * @return
	 * Kliens: szervert�l akpott j�t�kt�r Szerver: sz�tosztott j�t�kt�r. 
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

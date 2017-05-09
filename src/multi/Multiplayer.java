package tilitoli;

import java.net.*;
import java.util.*;

/**Hálózati játék lebonyolítását végzõ osztály.
 * @author Tarjányi Péter
 */
public class Multiplayer
{
	private enum Mode {none, server, client};
	
	private DiscoveryModule discover;
	private ConnectorModule connector;
	
	private String myName;
	private Mode mode;
	private GameList games;
	
	/**Konstuktor
	 * @param myName
	 * Aktuális játékos neve.
	 */
	public Multiplayer(String myName)
	{
		this.myName = myName;
		discover = new DiscoveryModule();
		connector = new ConnectorModule();
		mode = Mode.none;
	}
	
	/**Kislistázza a hálózaton elérhetõ kátékokat. Blokkol arra az ídõre, amíg gyûjti a válaszokat.
	 * @param timeout
	 * Az az idõtartam, amíg várjuk a válaszokat.
	 * @return
	 * A választ adó játékmesterek listája.
	 */
	public String[] ListGameNames(Integer timeout)
	{
		List<InetAddress> bcAddress =  discover.GetAllBroadcastAddress(true);
		discover.StartListening();
		for (InetAddress a : bcAddress)
		{
			discover.SendPing(a);
		}
		try
		{
			Thread.sleep(timeout);
		}catch (Exception e)
		{
			//Itt nem kéne interrupt...
		}
		discover.StopListening();
		games = discover.GetServerReplys();
		return games.GetGameNames();		
	}
	
	/**Ha még nem vagyunk semmilyen módban, csatlakozik a kiválasztott játékhoz.
	 * @param masterName
	 * Választott játékmester / játék.
	 * @return
	 * false ha már valamilyen módban vagyunk vagy nem sikerült csatlakozni.
	 */
	public boolean JoinGame(String masterName)
	{
		if (mode == Mode.none)
		{
			if (connector.JoinGame(games.GetGameAddress(masterName)))
			{
				mode = Mode.client;
				return true;
			}
			System.out.println("Join error.");
		}
		System.out.println("Mode error.");
		return false;
	}
	
	/**Létrehoz egy új játék szervert.
	 * @param imageID
	 * A játszani kívánt kép.
	 * @param puzzle
	 * A játszani kívánt keverés
	 * @return
	 * false ha már fut szerver vagy kliens, illetve ha nem sikerült elindítani.
	 */
	public boolean CreateGame(Puzzle p)
	{
		if (mode == Mode.none)
		{
			if (discover.StartReplyAs(myName, p.GetImage()))
			{
				if(connector.StartGameServer(p))
				{
					mode = Mode.server;
					return true;
				}else
				{
					discover.StopReply();
					return false;
				}
			}
		}
		return false;
	}
	
	/**Szerver esetén megszakítja a további játékosok csatlakozását, és elindul a tényleges játék.
	 * @return
	 * false ha nem szerver módban hívjuk meg.
	 */
	public boolean StartGame()
	{
		if (mode == Mode.server)
		{
			discover.StopReply();
			connector.DisableNewConnections();
			return true;
		}
		return false;
	}
	
	/** Befejezi az aktuális játékot. Szerver esetén lezárja a kapcsolatokat.
	 * @return
	 * false ha nem is futott semmi
	 */
	public boolean FinishGame()
	{
		if (mode == Mode.server)
		{
			if (discover.GetReceiveState())
			{
				discover.StopReply();
			}
			mode = Mode.none;
			return connector.StopGameServer();
		}else if (mode == Mode.client)
		{
			mode = Mode.none;
			return connector.LeaveGame();
		}
		return false;
	}
	
	/**Szinkornizálja a pontszámot a többi játékossal. Kliens esetén blokkol, amíg meg nem jön a szervertõl a firss adat.
	 * @param myScore
	 * A saját pontszámom.
	 * @return
	 * Az összes játékos aktuálisan elérhetõ pontszáma.
	 */
	public Scores SyncScore(Integer myScore)
	{
		return connector.GetPlayerScores(myName, myScore);
	}
	
	/**Megadja az aktuális játékteret. Kliens esetén blokkol amíg meg nem kapja a szervertõl, tehát amíg el nem indul a játék.
	 * @return
	 * Kliens: szervertõl akpott játéktér Szerver: szétosztott játéktér. 
	 */
	public Puzzle GetPuzzle()
	{
		return connector.GetPuzzle();
	}
	
	/**Megadja a csatlakozott játékosok számát.
	 * @return
	 * Csatlakozott játékosok száma, 0 ha kliensek vagyunk.
	 */
	public Integer GetConnectionCount()
	{
		if (mode == Mode.server)
		{
			return connector.GetConnectionCount();
		}else return 0;
	}
}

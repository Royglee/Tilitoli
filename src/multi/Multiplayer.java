package multi;

import java.net.*;
import java.util.*;

/**Hálózati játék lebonyolítását végző osztály.
 * @author Tarjányi Péter
 */
public class Multiplayer
{
	private enum Mode {none, server, client};
	
	private DiscoveryModule discovery;
	private ConnectorModule connector;
	
	private String myName;
	private Mode mode;
	private GameList games;
	
	/**Konstuktor
	 */
	public Multiplayer()
	{
		discovery = new DiscoveryModule();
		connector = new ConnectorModule();
		mode = Mode.none;
	}
	
	/**Kislistázza a hálózaton elérhető kátékokat. Blokkol arra az ídőre, amíg gyűjti a válaszokat.
	 * @param timeout
	 * Az az időtartam, amíg várjuk a válaszokat.
	 * @return
	 * A választ adó játékmesterek listája.
	 */
	public String[] listGameNames(Integer timeout)
	{
		List<InetAddress> bcAddress =  discovery.getAllBroadcastAddress(true);
		discovery.startListening();
		for (InetAddress a : bcAddress)
		{
			discovery.sendPing(a);
		}
		try
		{
			Thread.sleep(timeout);
		}catch (Exception e)
		{
			//Itt nem kéne interrupt...
		}
		discovery.stopListening();
		games = discovery.getServerReplys();
		return games.getGameNames();		
	}
	
	/**Ha még nem vagyunk semmilyen módban, csatlakozik a kiválasztott játékhoz.
	 * @param myName
	 * A csatlakozó játékos neve.
	 * @param masterName
	 * Választott játékmester / játék.
	 * @return
	 * false ha már valamilyen módban vagyunk vagy nem sikerült csatlakozni vagy ha már foglalt a játékos név
	 */
	public boolean joinGame(String myName, String masterName)
	{
		if (mode == Mode.none)
		{
			if (games.containsName(myName))
			{
				System.out.println("Name already taken!");
				return false;
			}else
			{
				this.myName = myName;
				if (connector.joinGame(myName, games.getGameAddress(masterName)))
				{
					mode = Mode.client;
					return true;
				}
				System.out.println("Join error.");
			}
		}
		System.out.println("Mode error.");
		return false;
	}
	
	/**Létrehoz egy új játék szervert.
	 * @param myName
	 * A létrehozó saját neve.
	 * @param p
	 * A játszandó játéktér.
	 * @return
	 * false ha már fut szerver vagy kliens, illetve ha nem sikerült elindítani.
	 */
	public boolean createGame(String myName,Puzzle p)
	{
		if (mode == Mode.none)
		{
			//szervert se lehessen létrehozni létező névvel
			this.myName = myName;
			if (discovery.startReplyAs(myName, p.getImage()))
			{
				if(connector.startGameServer(p))
				{
					mode = Mode.server;
					return true;
				}else
				{
					discovery.stopReply();
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
	public boolean startGame()
	{
		if (mode == Mode.server && getConnectionCount()>0)
		{
			discovery.stopReply();
			connector.disableNewConnections();
			return true;
		}
		return false;
	}
	
	/** Befejezi az aktuális játékot. Szerver esetén lezárja a kapcsolatokat.
	 * @return
	 * false ha nem is futott semmi
	 */
	public boolean finishGame()
	{
		if (mode == Mode.server)
		{
			if (discovery.getReceiveState())
			{
				discovery.stopReply();
			}
			mode = Mode.none;
			return connector.stopGameServer();
		}else if (mode == Mode.client)
		{
			mode = Mode.none;
			return connector.leaveGame();
		}
		return false;
	}
	
	/**Szinkornizálja a pontszámot a többi játékossal. Kliens esetén blokkol, amíg meg nem jön a szervertől a firss adat.
	 * @param myScore
	 * A saját pontszámom.
	 * @return
	 * Az összes játékos aktuálisan elérhető pontszáma.
	 */
	public Scores syncScore(Integer myScore)
	{
		return connector.getPlayerScores(myName, myScore);
	}
	
	/**Megadja az aktuális játékteret. Kliens esetén blokkol amíg meg nem kapja a szervertől, tehát amíg el nem indul a játék.
	 * @return
	 * Kliens: szervertől akpott játéktér Szerver: szétosztott játéktér. 
	 */
	public Puzzle getPuzzle()
	{
		return connector.getPuzzle();
	}
	
	/**Megadja a csatlakozott játékosok számát.
	 * @return
	 * Csatlakozott játékosok száma, 0 ha kliensek vagyunk.
	 */
	public Integer getConnectionCount()
	{
		if (mode == Mode.server)
		{
			return connector.getConnectionCount();
		}else return 0;
	}
}

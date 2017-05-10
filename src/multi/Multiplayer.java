package multi;

import java.net.*;
import java.util.*;

/**Hálózati játék lebonyolítását végzõ osztály.
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
	 * @param myName
	 * Aktuális játékos neve.
	 */
	public Multiplayer(String myName)
	{
		this.myName = myName;
		discovery = new DiscoveryModule();
		connector = new ConnectorModule();
		mode = Mode.none;
	}
	
	/**Kislistázza a hálózaton elérhetõ kátékokat. Blokkol arra az ídõre, amíg gyûjti a válaszokat.
	 * @param timeout
	 * Az az idõtartam, amíg várjuk a válaszokat.
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
	 * @param masterName
	 * Választott játékmester / játék.
	 * @return
	 * false ha már valamilyen módban vagyunk vagy nem sikerült csatlakozni.
	 */
	public boolean joinGame(String masterName)
	{
		if (mode == Mode.none)
		{
			if (connector.joinGame(games.getGameAddress(masterName)))
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
	public boolean createGame(Puzzle p)
	{
		if (mode == Mode.none)
		{
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
		if (mode == Mode.server && getConnectionCount()>1)
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
	
	/**Szinkornizálja a pontszámot a többi játékossal. Kliens esetén blokkol, amíg meg nem jön a szervertõl a firss adat.
	 * @param myScore
	 * A saját pontszámom.
	 * @return
	 * Az összes játékos aktuálisan elérhetõ pontszáma.
	 */
	public Scores syncScore(Integer myScore)
	{
		return connector.getPlayerScores(myName, myScore);
	}
	
	/**Megadja az aktuális játékteret. Kliens esetén blokkol amíg meg nem kapja a szervertõl, tehát amíg el nem indul a játék.
	 * @return
	 * Kliens: szervertõl akpott játéktér Szerver: szétosztott játéktér. 
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

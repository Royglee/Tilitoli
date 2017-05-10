package multi;

import java.net.*;
import java.util.*;

/**H�l�zati j�t�k lebonyol�t�s�t v�gz� oszt�ly.
 * @author Tarj�nyi P�ter
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
	 * Aktu�lis j�t�kos neve.
	 */
	public Multiplayer(String myName)
	{
		this.myName = myName;
		discovery = new DiscoveryModule();
		connector = new ConnectorModule();
		mode = Mode.none;
	}
	
	/**Kislist�zza a h�l�zaton el�rhet� k�t�kokat. Blokkol arra az �d�re, am�g gy�jti a v�laszokat.
	 * @param timeout
	 * Az az id�tartam, am�g v�rjuk a v�laszokat.
	 * @return
	 * A v�laszt ad� j�t�kmesterek list�ja.
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
			//Itt nem k�ne interrupt...
		}
		discovery.stopListening();
		games = discovery.getServerReplys();
		return games.getGameNames();		
	}
	
	/**Ha m�g nem vagyunk semmilyen m�dban, csatlakozik a kiv�lasztott j�t�khoz.
	 * @param masterName
	 * V�lasztott j�t�kmester / j�t�k.
	 * @return
	 * false ha m�r valamilyen m�dban vagyunk vagy nem siker�lt csatlakozni.
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
	
	/**L�trehoz egy �j j�t�k szervert.
	 * @param imageID
	 * A j�tszani k�v�nt k�p.
	 * @param puzzle
	 * A j�tszani k�v�nt kever�s
	 * @return
	 * false ha m�r fut szerver vagy kliens, illetve ha nem siker�lt elind�tani.
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
	
	/**Szerver eset�n megszak�tja a tov�bbi j�t�kosok csatlakoz�s�t, �s elindul a t�nyleges j�t�k.
	 * @return
	 * false ha nem szerver m�dban h�vjuk meg.
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
	
	/** Befejezi az aktu�lis j�t�kot. Szerver eset�n lez�rja a kapcsolatokat.
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
	
	/**Szinkorniz�lja a pontsz�mot a t�bbi j�t�kossal. Kliens eset�n blokkol, am�g meg nem j�n a szervert�l a firss adat.
	 * @param myScore
	 * A saj�t pontsz�mom.
	 * @return
	 * Az �sszes j�t�kos aktu�lisan el�rhet� pontsz�ma.
	 */
	public Scores syncScore(Integer myScore)
	{
		return connector.getPlayerScores(myName, myScore);
	}
	
	/**Megadja az aktu�lis j�t�kteret. Kliens eset�n blokkol am�g meg nem kapja a szervert�l, teh�t am�g el nem indul a j�t�k.
	 * @return
	 * Kliens: szervert�l akpott j�t�kt�r Szerver: sz�tosztott j�t�kt�r. 
	 */
	public Puzzle getPuzzle()
	{
		return connector.getPuzzle();
	}
	
	/**Megadja a csatlakozott j�t�kosok sz�m�t.
	 * @return
	 * Csatlakozott j�t�kosok sz�ma, 0 ha kliensek vagyunk.
	 */
	public Integer getConnectionCount()
	{
		if (mode == Mode.server)
		{
			return connector.getConnectionCount();
		}else return 0;
	}
}

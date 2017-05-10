package multi;

import java.net.*;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**UDP játék feltárást végzõ osztály. Megvalósítja a keresést és az arra adott választ is.
 * @author Tarjányi Péter
 */
public class DiscoveryModule implements Runnable
{

	private static final int MAX_BUFFER_SIZE = 128;
	private static final String INTERNATIONAL_PING = "TiliToli:ping!";
	private static final int UDP_PORT = 55555;
	
	private DatagramSocket socket;	//Kapcsolathoz szükséges socket
	private Thread receiveThread;	//Aszinkorn fogadásra használt szál
	private boolean isReplying; 	//Reply thread enabler
	private boolean isListening;	//Listening thread enabler
	private String masterName;		//Válaszhoz szükséges néve
	private int imageID;			//Aktuális játéktér azonosító

	private GameList availableGames;//Alosztály, az elérhetõ játékok feldolgozására és tárolására
	
	
	/** A thread futásához szükséges run blokk, benne az elágazás asszerint, hogy szerverként válaszolgatunk, vagy várjuk a szerverek válaszát
	 */
	@Override
	public void run()
	{
		while (isReplying || isListening)
		{
			if (socket != null)
			{
				try
				{
					byte[] inputBuffer = new byte[MAX_BUFFER_SIZE];
					DatagramPacket p = new DatagramPacket(inputBuffer, inputBuffer.length);
					socket.receive(p);
					if (isReplying)
					{
						replyIfValid(p);
					}
					if (isListening)
					{
						availableGames.loadData(p);
					}
				}catch (Exception e)
				{
					//System.out.println(e.toString());
					//Do nothing, "while" goes on and on
					//Maybe timeout maybe error, no one cares.
				}
			}
			else
			{
				//Why should I do anything without socket?!
				isReplying = false;
				isListening = false;
			}
		}
	}
	
	/**Belsõ függvény, megnézi a csomagot, és ha ping akkor pong.
	 * @param p
	 * Csomag aminek a tartalmát vizsgáljuk
	 */
	private void replyIfValid(DatagramPacket p)
	{
		if (p != null)
		{
			String s = new String(p.getData());
			if (s.startsWith(INTERNATIONAL_PING))
			{
				sendAnswer(p);
			}
		}
		//System.out.println("DiscoveryModule.ReplyIfValid: NULL data!");
	}
	
	/**Belsõ függvény, ami a socketen keresztül megpróbál egy már összeállított datagramm hálózati adataival egy pong tartalmat elküldeni.
	 * @param p
	 * Cél adatokat tartalmazó datagramPacket.
	 */
	private void sendAnswer(DatagramPacket p)
	{
		if (socket != null && p != null)
		{
			String answer = "ToliTili:"+masterName+":"+imageID+":!";
			p.setData(answer.getBytes());
			try
			{
				socket.send(p);
			}catch (Exception e)
			{
				System.out.println("DiscoveryModule.SendAnswer: Can't send answer!");
			}
		}
		else
		{
			System.out.println("DiscoveryModule.SendAnswer: Socket or data is NULL!");
		}
	}
	
	/**Das Konstruktor. Need no comment. 
	 */
	public DiscoveryModule()
	{
		isReplying = false;
		isListening = false;
		availableGames = new GameList();
		receiveThread = new Thread(this);
		try
		{
			socket = new DatagramSocket(UDP_PORT);
			socket.setSoTimeout(100); // <- ez nagyon is kell, különben a receiverThread örökre blokkolhat, és restartig buktuk a port foglalást.
			socket.setReuseAddress(true);
		}catch (Exception e)
		{
			socket = null;
			System.out.println("DiscoveryModule.Constructor: Unable to Create Socket!");
		}
	}

	/**UDP kapcsolataon keresztül küld egy pinget a megadott cmíre!
	 * @param dest
	 * Cél IP cím
	 * @param port
	 * Cél port
	 * @param data
	 * Küldendõ adat
	 * @return
	 * True ha sikerült a küldés, false ha nem.
	 */
	public boolean sendPing(InetAddress dest)
	{
		if (socket != null && dest != null)
		{
			DatagramPacket p = new DatagramPacket(INTERNATIONAL_PING.getBytes(), INTERNATIONAL_PING.getBytes().length, dest, UDP_PORT);
			try
			{
				socket.send(p);
			}catch (Exception e)
			{
				System.out.println("DiscoveryModule.SnedPing: Unable to send trough socket!");
				return false;
			}
			return true;
		}
		//System.out.println("DiscoveryModule.SendPing: Socket is NULL!");
		return false;
	}
	
	/**Bekapcsolja a hallgatózó módot. Ilyenkor válaszol a boradcast üzenetekre.
	 * @param name
	 * A játékmester neve
	 * @param imageID
	 * A játéktér (kép) azonosítója
	 * @return
	 * True ha sikerült elindítani a módot, false különben (már fut / más fut).
	 */
	public boolean startReplyAs(String masterName, int imageID)
	{
		if (!isReplying && !isListening && !receiveThread.isAlive())
		{
			this.masterName = masterName;
			this.imageID = imageID;
			isReplying = true;
			receiveThread.start();
			return true;
		}
		System.out.println("DiscoveryModule.StartReplyAs: Already receiving!");
		return false;
	}
	
	/**Leállítja az automatikus pong küldést. Blokkol amíg a válaszoló szál ténylegesen le nem áll.
	 * @return
	 * false ha nem is futott semmi, true ha végre leálltunk
	 */
	public boolean stopReply()
	{
		if (isReplying && !isListening)
		{
			isReplying = false;
			while (receiveThread.isAlive())
			{
				try
				{
				Thread.sleep(1);
				}catch (InterruptedException e)
				{
					//Do nothing, "while" goes on and on
				}
			}
			return true;
		}
		return false;

	}
	
	/**No need for comment...
	 * @return
	 * The state of receiveThread as name shows.
	 */
	public boolean getReceiveState()
	{
		return receiveThread.isAlive();
	}

	/**Elindítja a fogadó szálat, ami majd jól fogadja discovery reply üzeneteket.
	 * @return
	 * true ha futunk, false ha már futunk vagy más fut a szálon (ergo ide se kellett volna jutni...)
	 */
	public boolean startListening()
	{
		if (!isListening && !isReplying && !receiveThread.isAlive())
		{
			isListening = true;
			availableGames.clearList();
			receiveThread.start();
			return true;
		}
		System.out.println("DiscoveryModule.StartListening: Already receiving!");
		return false;
	}
	
	/**Leállítja a reply gyûjtést. Blokkol amíg tényleg le nem áll.
	 * @return
	 * false ha nem is fut ez a funkció, true ha leállt.
	 */
	public boolean stopListening()
	{
		if (isListening && !isReplying)
		{
			isListening = false;
			while (receiveThread.isAlive())
			{
				try
				{
				Thread.sleep(1);
				}catch (InterruptedException e)
				{
					//Do nothing, "while" goes on and on
				}
			}
			return true;
		}
		return false;
	}
	
	/**Vissza adja az összegyûjtött elérhetõ játékokat.
	 * @return
	 * Elérhetõ játékok listája.
	 */
	public synchronized GameList getServerReplys()
	{
			return availableGames;
	}
	
	/**Vissza adja a hálózati interfészek boradcast címeit.
	 * @param withoutLoopback
	 * Ha true, akkor a lista nem tartalmazza a loopback címeket.
	 * @return
	 * A gép összes aktív broadcast címe.
	 */
	public List<InetAddress> getAllBroadcastAddress(Boolean withoutLoopback)
	{
		List<InetAddress> result = new LinkedList<InetAddress>();
		try
		{
			Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces();
			for (;list.hasMoreElements();)
			{
				NetworkInterface n = list.nextElement();
				if (!n.isVirtual() && n.isUp() && !(withoutLoopback && n.isLoopback())) 
				{				
					result.add(n.getInterfaceAddresses().get(0).getBroadcast());
				}
			}
		}catch(Exception e)
		{
			result = null;
		}
		return result;
	}
}

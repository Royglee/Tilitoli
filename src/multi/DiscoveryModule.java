package multi;

import java.net.*;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**UDP j�t�k felt�r�st v�gz� oszt�ly. Megval�s�tja a keres�st �s az arra adott v�laszt is.
 * @author Tarj�nyi P�ter
 */
public class DiscoveryModule implements Runnable
{

	private static final int MAX_BUFFER_SIZE = 128;
	private static final String INTERNATIONAL_PING = "TiliToli:ping!";
	private static final int UDP_PORT = 55555;
	
	private DatagramSocket socket;	//Kapcsolathoz sz�ks�ges socket
	private Thread receiveThread;	//Aszinkorn fogad�sra haszn�lt sz�l
	private boolean isReplying; 	//Reply thread enabler
	private boolean isListening;	//Listening thread enabler
	private String masterName;		//V�laszhoz sz�ks�ges n�ve
	private int imageID;			//Aktu�lis j�t�kt�r azonos�t�

	private GameList availableGames;//Aloszt�ly, az el�rhet� j�t�kok feldolgoz�s�ra �s t�rol�s�ra
	
	
	/** A thread fut�s�hoz sz�ks�ges run blokk, benne az el�gaz�s asszerint, hogy szerverk�nt v�laszolgatunk, vagy v�rjuk a szerverek v�lasz�t
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
	
	/**Bels� f�ggv�ny, megn�zi a csomagot, �s ha ping akkor pong.
	 * @param p
	 * Csomag aminek a tartalm�t vizsg�ljuk
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
	
	/**Bels� f�ggv�ny, ami a socketen kereszt�l megpr�b�l egy m�r �ssze�ll�tott datagramm h�l�zati adataival egy pong tartalmat elk�ldeni.
	 * @param p
	 * C�l adatokat tartalmaz� datagramPacket.
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
			socket.setSoTimeout(100); // <- ez nagyon is kell, k�l�nben a receiverThread �r�kre blokkolhat, �s restartig buktuk a port foglal�st.
			socket.setReuseAddress(true);
		}catch (Exception e)
		{
			socket = null;
			System.out.println("DiscoveryModule.Constructor: Unable to Create Socket!");
		}
	}

	/**UDP kapcsolataon kereszt�l k�ld egy pinget a megadott cm�re!
	 * @param dest
	 * C�l IP c�m
	 * @param port
	 * C�l port
	 * @param data
	 * K�ldend� adat
	 * @return
	 * True ha siker�lt a k�ld�s, false ha nem.
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
	
	/**Bekapcsolja a hallgat�z� m�dot. Ilyenkor v�laszol a boradcast �zenetekre.
	 * @param name
	 * A j�t�kmester neve
	 * @param imageID
	 * A j�t�kt�r (k�p) azonos�t�ja
	 * @return
	 * True ha siker�lt elind�tani a m�dot, false k�l�nben (m�r fut / m�s fut).
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
	
	/**Le�ll�tja az automatikus pong k�ld�st. Blokkol am�g a v�laszol� sz�l t�nylegesen le nem �ll.
	 * @return
	 * false ha nem is futott semmi, true ha v�gre le�lltunk
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

	/**Elind�tja a fogad� sz�lat, ami majd j�l fogadja discovery reply �zeneteket.
	 * @return
	 * true ha futunk, false ha m�r futunk vagy m�s fut a sz�lon (ergo ide se kellett volna jutni...)
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
	
	/**Le�ll�tja a reply gy�jt�st. Blokkol am�g t�nyleg le nem �ll.
	 * @return
	 * false ha nem is fut ez a funkci�, true ha le�llt.
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
	
	/**Vissza adja az �sszegy�jt�tt el�rhet� j�t�kokat.
	 * @return
	 * El�rhet� j�t�kok list�ja.
	 */
	public synchronized GameList getServerReplys()
	{
			return availableGames;
	}
	
	/**Vissza adja a h�l�zati interf�szek boradcast c�meit.
	 * @param withoutLoopback
	 * Ha true, akkor a lista nem tartalmazza a loopback c�meket.
	 * @return
	 * A g�p �sszes akt�v broadcast c�me.
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

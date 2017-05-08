package tilitoli;

import java.net.*;
import java.util.*;

/**A hálózaton elérhetõ játékokat tároló osztály.
 * @author Tarjányi Péter
 */
public class GameList
{
	
	private Map<String, DatagramPacket> gameList; //Use HashMap so we don't need searching simply just select by name. Ram doesn't matter, it's cheap. 
	
	/**Good old constructor.
	 */
	public GameList()
	{
		gameList = new HashMap<String,DatagramPacket>();
	}
	
	/**Üressé tesz nekünk a lustát, hogy tiszta lappal indulhassunk.
	 */
	public void ClearList()
	{
		synchronized (gameList)
		{
			gameList.clear();
		}
	}
	
	/**Belenyomunk egy packet-et, és ha az megfelelõ formátumú, felveszi a megfelelõ játékot a listára.
	 * @param p
	 * A feldolgozni kívánt packet.
	 * @return
	 * True ha értelmes válasz volt, és bevette, false ha már van ilyen vagy nem is válasz üzenet.
	 */
	public boolean LoadData(DatagramPacket p)
	{
		if (p != null)
		{
			String s = new String(p.getData());
			String[] parts = s.split(":");
			if (parts.length==4 && parts[0].startsWith("ToliTili") && parts[3].startsWith("!"))
			{
				p.setData(parts[2].getBytes());
				synchronized (gameList)
				{
					if (!gameList.containsKey(parts[1]))
					{
						gameList.put(parts[1], p); //So: Key is the name, datagramIP is the ip data, datagram data is the gamefiled. What a hack?!
					}else return false;
				}
				return true;
			}
		}
		return false;
	}

	/**Kilistázza a belepakolt játékmesterek neveit.
	 * @return
	 * A broadcast-ra válaszoló játékok nevei.
	 */
	public String[] GetGameNames()
	{
		synchronized (gameList)
		{
			String [] result = new String[gameList.size()];
			int i = 0;
			for (String key : gameList.keySet())
			{
				result[i] = key;
				i++;
			}
			return result;
		}

	}
	
	/**Megmondja, hogy a kiválasztott játékhoz milyen cím tartozik.
	 * @param name
	 * A játék neve, aminek a címe kéne nekünk.
	 * @return
	 * IP ha name valid, null ha name nem is létezik.
	 */
	public InetAddress GetGameAddress(String name)
	{
		return gameList.containsKey(name)?gameList.get(name).getAddress():null; //<- that's why I use HashMap with crazy datagram packet.
	}
}

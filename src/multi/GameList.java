package tilitoli;

import java.net.*;
import java.util.*;

/**A h�l�zaton el�rhet� j�t�kokat t�rol� oszt�ly.
 * @author Tarj�nyi P�ter
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
	
	/**�ress� tesz nek�nk a lust�t, hogy tiszta lappal indulhassunk.
	 */
	public void ClearList()
	{
		synchronized (gameList)
		{
			gameList.clear();
		}
	}
	
	/**Belenyomunk egy packet-et, �s ha az megfelel� form�tum�, felveszi a megfelel� j�t�kot a list�ra.
	 * @param p
	 * A feldolgozni k�v�nt packet.
	 * @return
	 * True ha �rtelmes v�lasz volt, �s bevette, false ha m�r van ilyen vagy nem is v�lasz �zenet.
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

	/**Kilist�zza a belepakolt j�t�kmesterek neveit.
	 * @return
	 * A broadcast-ra v�laszol� j�t�kok nevei.
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
	
	/**Megmondja, hogy a kiv�lasztott j�t�khoz milyen c�m tartozik.
	 * @param name
	 * A j�t�k neve, aminek a c�me k�ne nek�nk.
	 * @return
	 * IP ha name valid, null ha name nem is l�tezik.
	 */
	public InetAddress GetGameAddress(String name)
	{
		return gameList.containsKey(name)?gameList.get(name).getAddress():null; //<- that's why I use HashMap with crazy datagram packet.
	}
}

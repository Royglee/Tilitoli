package multi;

import java.net.*;
import java.io.*;

/**Különálló tcp kapcsolatokat kezelõ osztály
 * @author Tarjányi Péter
 *
 */
public class NetworkClient implements Runnable
{
	
	private static Scores scores;
	private static Puzzle puzzle;
	private Socket socket;
	//private ObjectInputStream iS;
	//private ObjectOutputStream oS;
	private boolean enabled;
	private Thread thread;
	private boolean deployed; 
	
	/**Az osztály statikus eredményeit állítja. Threadsafe.
	 * @param s
	 * A beállítani kívánt eredmények.
	 */
	public static synchronized void setScores(Scores s)
	{
		if (scores == null)
		{
			scores = s;
		}else
		{
			scores.mergeScores(s);
		}
	}
	
	/**Vissza adja a aktuális legfrissebb eredményeket. Threadsafe.
	 * @return
	 * Aktuális eredmények.
	 */
	public static synchronized Scores getScores()
	{
		return scores;
	}
	
	/**Beállítja a szétosztandó játékteret. Threadsafe.
	 * @param p
	 * Szétosztandó játéktér.
	 */
	public static synchronized void setPuzzle(Puzzle p)
	{
		puzzle = p;	
	}
	
	/**Visszaadja a beállított játékteret, mondjuk sok haszna nincs, de elfér. Threadsafe.
	 * @return
	 * Korábban beállított szétosztandó játéktér.
	 */
	public static synchronized Puzzle getPuzzle()
	{
		return puzzle;
	}
	
	/**Konstruktor
	 * @param s
	 * A fogadott TCP kapcsolathoz tartozó socket.
	 */
	public NetworkClient(Socket s)
	{
		socket = s;
		try
		{
			socket.setSoTimeout(1000);
			s.setKeepAlive(true);
			thread = new Thread(this);
		}catch (Exception e)
		{
			thread = null;
		}
		enabled = false;
	}
	
	/**A kapcsolatot kezel szálat képezõ folyamat.
	 *
	 */
	@Override
	public void run()
	{
		while (enabled && socket != null)
		{			
			try
			{
				SyncObject sync;
				if (!deployed)
				{
					sync = new SyncObject(getScores(), getPuzzle());
					ObjectOutputStream oS = new ObjectOutputStream(socket.getOutputStream());
					oS.writeObject(sync);
					deployed = true;
				}else
				{
					ObjectInputStream iS = new ObjectInputStream(socket.getInputStream());
					sync = (SyncObject)iS.readObject();
					setScores(sync.scores);
					sync.scores = getScores();
					ObjectOutputStream oS = new ObjectOutputStream(socket.getOutputStream());
					oS.writeObject(sync);
				}
				
			}catch (Exception e)
			{
				//System.out.println("sync-"+e.toString());
			}
		}
	}

	/**Elindítja a konkrét TCP kapcsolatot kezelõ szálat.
	 * @return
	 * true ha minend rendben, false ha nem indult el.
	 */
	public boolean start()
	{
		if (thread != null)
		{
			enabled = true;
			deployed = false;
			thread.start();
		}else
		{
			enabled = false;
		}
		return enabled;
	}
	
	/**Leállítja a szálat, ami kezeli a kapcsolatot.
	 * @return
	 * true ha leállt, false ha nem is futott.
	 */
	public boolean stop() 
	{
		enabled = false;
		if (thread != null)
		{
			while (thread.isAlive())
			{
				try
				{
					Thread.sleep(1);
				}catch (Exception e){}
			}
			return true;
		}else
		{
			return false;
		}
	}
	
	/**
	 * Leállítja a szálat, és aztán lezárja a kapcsolatot.
	 */
	public void close()
	{
		stop();
		try
		{
			socket.close();
		}catch(Exception e){}
	}
	
}

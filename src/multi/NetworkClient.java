package multi;

import java.net.*;
import java.io.*;

/**K�l�n�ll� tcp kapcsolatokat kezel� oszt�ly
 * @author Tarj�nyi P�ter
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
	
	/**Az oszt�ly statikus eredm�nyeit �ll�tja. Threadsafe.
	 * @param s
	 * A be�ll�tani k�v�nt eredm�nyek.
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
	
	/**Vissza adja a aktu�lis legfrissebb eredm�nyeket. Threadsafe.
	 * @return
	 * Aktu�lis eredm�nyek.
	 */
	public static synchronized Scores getScores()
	{
		return scores;
	}
	
	/**Be�ll�tja a sz�tosztand� j�t�kteret. Threadsafe.
	 * @param p
	 * Sz�tosztand� j�t�kt�r.
	 */
	public static synchronized void setPuzzle(Puzzle p)
	{
		puzzle = p;	
	}
	
	/**Visszaadja a be�ll�tott j�t�kteret, mondjuk sok haszna nincs, de elf�r. Threadsafe.
	 * @return
	 * Kor�bban be�ll�tott sz�tosztand� j�t�kt�r.
	 */
	public static synchronized Puzzle getPuzzle()
	{
		return puzzle;
	}
	
	/**Konstruktor
	 * @param s
	 * A fogadott TCP kapcsolathoz tartoz� socket.
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
	
	/**A kapcsolatot kezel sz�lat k�pez� folyamat.
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

	/**Elind�tja a konkr�t TCP kapcsolatot kezel� sz�lat.
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
	
	/**Le�ll�tja a sz�lat, ami kezeli a kapcsolatot.
	 * @return
	 * true ha le�llt, false ha nem is futott.
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
	 * Le�ll�tja a sz�lat, �s azt�n lez�rja a kapcsolatot.
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

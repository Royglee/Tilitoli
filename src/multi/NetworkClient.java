package tilitoli;

import java.net.*;
import java.io.*;

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
	
	public static synchronized void SetScores(Scores s)
	{
		if (scores == null)
		{
			scores = s;
		}else
		{
			scores.MergeScores(s);
		}
	}
	
	public static synchronized Scores GetScores()
	{
		return scores;
	}
	
	public static synchronized void SetPuzzle(Puzzle p)
	{
		puzzle = p;	
	}
	
	public static synchronized Puzzle GetPuzzle()
	{
		return puzzle;
	}
	
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
					sync = new SyncObject(GetScores(), GetPuzzle());
					ObjectOutputStream oS = new ObjectOutputStream(socket.getOutputStream());
					oS.writeObject(sync);
					deployed = true;
				}else
				{
					ObjectInputStream iS = new ObjectInputStream(socket.getInputStream());
					sync = (SyncObject)iS.readObject();
					SetScores(sync.scores);
					sync.scores = GetScores();
					ObjectOutputStream oS = new ObjectOutputStream(socket.getOutputStream());
					oS.writeObject(sync);
				}
				
			}catch (Exception e)
			{
				//System.out.println("sync-"+e.toString());
			}
		}
	}

	public boolean Start()
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
	
	public boolean Stop() 
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
	
	public void Close()
	{
		Stop();
		try
		{
			socket.close();
		}catch(Exception e){}
	}
	
}

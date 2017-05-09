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
			thread = new Thread(this);
		}catch (Exception e)
		{
			thread = null;
		}
		enabled = false;
		deployed = false;
	}
	
	@Override
	public void run()
	{
		while (enabled && socket != null)
		{
			if (!deployed)
			{
				try
				{
					ObjectOutputStream oS = new ObjectOutputStream(socket.getOutputStream());
					oS.writeObject(GetPuzzle());
					deployed = true;
				}catch (Exception e)
				{
					deployed = false;
				}
			}else
			{
				try
				{
					ObjectOutputStream oS = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream iS = new ObjectInputStream(socket.getInputStream());
					Scores newScores = (Scores)iS.readObject();
					SetScores(newScores);
					oS.writeObject(GetScores());
					oS.flush();
				}catch (Exception e)
				{
					System.out.println("sync-"+e.toString());
				}
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
			return true;
		}else
		{
			enabled = false;
			return false;
		}
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

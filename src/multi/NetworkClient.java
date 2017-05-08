package tilitoli;

import java.net.*;
import java.io.*;

public class NetworkClient implements Runnable
{
	private static Scores scores;
	private static Puzzle puzzle;
	private Socket socket;
	private ObjectInputStream iS;
	private ObjectOutputStream oS;
	private boolean enabled;
	private Thread thread;
	private boolean deployed;
	
	public static void SetScores(Scores s)
	{
		synchronized (scores)
		{
			scores.MergeScores(s);	
		}
	}
	
	public static Scores GetScores()
	{
		synchronized (scores)
		{
			return scores;	
		}
	}
	
	public static void SetPuzzle(Puzzle p)
	{
		synchronized (puzzle)
		{
			puzzle = p;	
		}
	}
	
	public static Puzzle GetPuzzle()
	{
		synchronized (puzzle)
		{
			return puzzle;
		}
	}
	
	public NetworkClient(Socket s)
	{
		socket = s;
		try
		{
			socket.setSoTimeout(1000);
			iS = new ObjectInputStream(socket.getInputStream());
			oS = new ObjectOutputStream(socket.getOutputStream());
			thread = new Thread(this);
		}catch (Exception e)
		{
			iS = null;
			oS = null;
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
					Scores newScore = (Scores)iS.readObject();
					SetScores(newScore);
					oS.writeObject(GetScores());
				}catch (Exception e)
				{
					
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
	
}

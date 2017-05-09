package tilitoli;

import java.io.Serializable;

public class SyncObject implements Serializable
{
	static final long serialVersionUID = 1L;
	public Scores scores;
	public Puzzle puzzle;
	
	public SyncObject(Scores scores, Puzzle puzzle)
	{
		this.scores = scores;
		this.puzzle = puzzle;
	}
}

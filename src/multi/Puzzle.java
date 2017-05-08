package tilitoli;

import java.io.Serializable;

public class Puzzle implements Serializable
{
	private static final long serialVersionUID = 1L;
	private byte image;
	private byte[] puzzle;
	private byte size; 
	
	public Puzzle()
	{
		image = 0;
		size = 0;
		puzzle = null;
	}
	
	public Puzzle(byte image, byte size, byte[] puzzle)
	{
		this.image = image;
		this.puzzle = puzzle;
		this.size = size;
	}

	public void SetImage(byte image)
	{
		this.image = image;
	}
	
	public byte GetImage()
	{
		return image;
	}
	
	public void SetPuzzle(byte[] puzzle)
	{
		this.puzzle = puzzle;
	}
	
	public byte[] GetPuzzle()
	{
		return puzzle;
	}

	public void SetSize(byte size)
	{
		this.size = size;
	}
	
	public byte GetSize()
	{
		return size;
	}
}

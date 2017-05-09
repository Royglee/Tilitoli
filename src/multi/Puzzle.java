package tilitoli;

import java.io.Serializable;

/**J�t�kteret t�rol� strukt�ra.
 * @author Tarj�nyi P�ter
 *
 */
public class Puzzle implements Serializable
{
	private static final long serialVersionUID = 1L;
	private byte image;
	private byte[] puzzle;
	private byte size; 
	
	/**Konstruktor
	 * 
	 */
	public Puzzle()
	{
		image = 0;
		size = 0;
		puzzle = null;
	}
	
	/**Param�terezhet� konsturktor
	 * @param image
	 * K�p azonos�t�
	 * @param size
	 * P�lya m�ret
	 * @param puzzle
	 * Kever�s
	 */
	public Puzzle(byte image, byte size, byte[] puzzle)
	{
		this.image = image;
		this.puzzle = puzzle;
		this.size = size;
	}

	/**K�p be�ll�t�sa
	 * @param image
	 * k�p azonos�t�
	 */
	public void SetImage(byte image)
	{
		this.image = image;
	}
	
	/**Visszadja a k�pazonos�t�t
	 * @return
	 * k�pazonos�t�
	 */
	public byte GetImage()
	{
		return image;
	}
	
	/**Be�ll�tja a kever�st
	 * @param puzzle
	 * �j kever�s
	 */
	public void SetPuzzle(byte[] puzzle)
	{
		this.puzzle = puzzle;
	}
	
	/**Visszaadja a kever�st
	 * @return
	 * aktu�lis kever�s
	 */
	public byte[] GetPuzzle()
	{
		return puzzle;
	}

	/**Be�ll�tja a p�lya m�ret�t
	 * @param size
	 * oldalhossz
	 */
	public void SetSize(byte size)
	{
		this.size = size;
	}
	
	/**Visszaadja a p�lyam�retet
	 * @return
	 * oldalhossz
	 */
	public byte GetSize()
	{
		return size;
	}
}

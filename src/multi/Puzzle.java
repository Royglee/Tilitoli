package multi;

import java.io.Serializable;

/**J�t�kteret t�rol� strukt�ra.
 * @author Tarj�nyi P�ter
 *
 */
public class Puzzle implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String image;
	private byte[] puzzle;
	private byte size; 
	
	/**Konstruktor
	 * 
	 */
	public Puzzle()
	{
		image = "";
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
	public Puzzle(String image, byte size, byte[] puzzle)
	{
		this.image = image;
		this.puzzle = puzzle;
		this.size = size;
	}

	/**K�p be�ll�t�sa
	 * @param image
	 * k�p azonos�t�
	 */
	public void setImage(String image)
	{
		this.image = image;
	}
	
	/**Visszadja a k�pazonos�t�t
	 * @return
	 * k�pazonos�t�
	 */
	public String getImage()
	{
		return image;
	}
	
	/**Be�ll�tja a kever�st
	 * @param puzzle
	 * �j kever�s
	 */
	public void setPuzzle(byte[] puzzle)
	{
		this.puzzle = puzzle;
	}
	
	/**Visszaadja a kever�st
	 * @return
	 * aktu�lis kever�s
	 */
	public byte[] getPuzzle()
	{
		return puzzle;
	}

	/**Be�ll�tja a p�lya m�ret�t
	 * @param size
	 * oldalhossz
	 */
	public void setSize(byte size)
	{
		this.size = size;
	}
	
	/**Visszaadja a p�lyam�retet
	 * @return
	 * oldalhossz
	 */
	public byte getSize()
	{
		return size;
	}
}

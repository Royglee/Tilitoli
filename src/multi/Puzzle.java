package multi;

import java.io.Serializable;

/**Játékteret tároló struktúra.
 * @author Tarjányi Péter
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
	
	/**Paraméterezhetõ konsturktor
	 * @param image
	 * Kép azonosító
	 * @param size
	 * Pálya méret
	 * @param puzzle
	 * Keverés
	 */
	public Puzzle(byte image, byte size, byte[] puzzle)
	{
		this.image = image;
		this.puzzle = puzzle;
		this.size = size;
	}

	/**Kép beállítása
	 * @param image
	 * kép azonosító
	 */
	public void setImage(byte image)
	{
		this.image = image;
	}
	
	/**Visszadja a képazonosítót
	 * @return
	 * képazonosító
	 */
	public byte getImage()
	{
		return image;
	}
	
	/**Beállítja a keverést
	 * @param puzzle
	 * új keverés
	 */
	public void setPuzzle(byte[] puzzle)
	{
		this.puzzle = puzzle;
	}
	
	/**Visszaadja a keverést
	 * @return
	 * aktuális keverés
	 */
	public byte[] getPuzzle()
	{
		return puzzle;
	}

	/**Beállítja a pálya méretét
	 * @param size
	 * oldalhossz
	 */
	public void setSize(byte size)
	{
		this.size = size;
	}
	
	/**Visszaadja a pályaméretet
	 * @return
	 * oldalhossz
	 */
	public byte getSize()
	{
		return size;
	}
}

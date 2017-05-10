package multi;

import java.io.Serializable;

/**H�l�zaton k�ld�tt adatstrukt�ra
 * @author Tarj�nyi P�ter
 *
 */
public class SyncObject implements Serializable
{
	static final long serialVersionUID = 1L;
	public Scores scores;
	public Puzzle puzzle;
	
	//Nincs �rtelme getter-setter m�k�nak. A t�bbiben megmutattam, ha volt �rtelme.
	
	/**Konsturkor az adatok azonnali �ll�t�s�hoz.
	 * @param scores
	 * T�rolt eredm�nyek
	 * @param puzzle
	 * T�rolt j�t�k 
	 */
	public SyncObject(Scores scores, Puzzle puzzle)
	{
		this.scores = scores;
		this.puzzle = puzzle;
	}
}

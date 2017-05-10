package multi;

import java.io.Serializable;

/**Hálózaton küldött adatstruktúra
 * @author Tarjányi Péter
 *
 */
public class SyncObject implements Serializable
{
	static final long serialVersionUID = 1L;
	public Scores scores;
	public Puzzle puzzle;
	
	//Nincs értelme getter-setter mókának. A többiben megmutattam, ha volt értelme.
	
	/**Konsturkor az adatok azonnali állításához.
	 * @param scores
	 * Tárolt eredmények
	 * @param puzzle
	 * Tárolt játék 
	 */
	public SyncObject(Scores scores, Puzzle puzzle)
	{
		this.scores = scores;
		this.puzzle = puzzle;
	}
}

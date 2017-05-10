package multi;

import java.io.Serializable;
import java.util.*;

/**A játékosok pontjait tároló osztály
 * @author Tarjányi Péter
 */
public class Scores implements Serializable
{
	static final long serialVersionUID = 1L;
	private Map<String, Integer> scoreMap;
	
	/**Konstruktor
	 */
	public Scores()
	{
		scoreMap = new HashMap<String, Integer>();
	}
	
	/**Második konsturkor, ami egybõl rak is bele adatot
	 * @param name
	 * Játékos neve.
	 * @param socre
	 * Játékos pontjai
	 */
	public Scores(String name, Integer socre)
	{
		scoreMap = new HashMap<String, Integer>();
		scoreMap.put(name, socre);
	}
	
	/**Hozzáad egy új elemet a tárhoz. Ha már létezik, felülírja az új pontszámmal.
	 * @param name
	 * Játékos neve
	 * @param score
	 * Játékos (új) pontszáma.
	 */
	public void writeScore(String name, Integer score)
	{
		if (scoreMap.containsKey(name))
		{
			scoreMap.replace(name, score);
		}else
		{
			scoreMap.put(name, score);
		}
	}
	
	/**A paraméterben kapott pontszámokat hozzáfûzi a meglévõkhöz. Az újakat beleteszi, a létezõket felülírja.
	 * @param s
	 * A hozzáfûzendõ adatok.
	 */
	public void mergeScores(Scores s)
	{
		Set<String> originalKeys = this.scoreMap.keySet();
		Set<String> newKeys = s.scoreMap.keySet();
		for(String nKey : newKeys)
		{
			if (originalKeys.contains(nKey))
			{
				this.scoreMap.replace(nKey, s.scoreMap.get(nKey));
			}else
			{
				this.scoreMap.put(nKey, s.scoreMap.get(nKey));
			}
		}
	}
	
	/**Visszaadja egy konkrét játékos pontszámait.
	 * @param name
	 * Kiválasztott játékos.
	 * @return
	 * A játékos pontszáma, 0 ha nincs ilyen játékos.
	 */
	public Integer getScore(String name)
	{
		if (scoreMap.containsKey(name))
		{
			return scoreMap.get(name);
		}
		return 0;
	}
	
	/**Kilistázza az összes játékost és eredményét egy vektorban, "Név: pont" formátumban.
	 * @return
	 * "Név: pont" elemek vektora.
	 */
	public String[] listAll()
	{
		String[] result = new String[scoreMap.size()];
		Set<String> keys = scoreMap.keySet();
		Integer i = 0;
		for (String key : keys)
		{
			result[i] = key+": "+scoreMap.get(key);
			i++;
		}
		return result;
	}

}

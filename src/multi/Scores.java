package multi;

import java.io.Serializable;
import java.util.*;

/**A j�t�kosok pontjait t�rol� oszt�ly
 * @author Tarj�nyi P�ter
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
	
	/**M�sodik konsturkor, ami egyb�l rak is bele adatot
	 * @param name
	 * J�t�kos neve.
	 * @param socre
	 * J�t�kos pontjai
	 */
	public Scores(String name, Integer socre)
	{
		scoreMap = new HashMap<String, Integer>();
		scoreMap.put(name, socre);
	}
	
	/**Hozz�ad egy �j elemet a t�rhoz. Ha m�r l�tezik, fel�l�rja az �j pontsz�mmal.
	 * @param name
	 * J�t�kos neve
	 * @param score
	 * J�t�kos (�j) pontsz�ma.
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
	
	/**A param�terben kapott pontsz�mokat hozz�f�zi a megl�v�kh�z. Az �jakat beleteszi, a l�tez�ket fel�l�rja.
	 * @param s
	 * A hozz�f�zend� adatok.
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
	
	/**Visszaadja egy konkr�t j�t�kos pontsz�mait.
	 * @param name
	 * Kiv�lasztott j�t�kos.
	 * @return
	 * A j�t�kos pontsz�ma, 0 ha nincs ilyen j�t�kos.
	 */
	public Integer getScore(String name)
	{
		if (scoreMap.containsKey(name))
		{
			return scoreMap.get(name);
		}
		return 0;
	}
	
	/**Kilist�zza az �sszes j�t�kost �s eredm�ny�t egy vektorban, "N�v: pont" form�tumban.
	 * @return
	 * "N�v: pont" elemek vektora.
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

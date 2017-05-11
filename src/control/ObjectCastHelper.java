package control;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

public class ObjectCastHelper {
	
	public static Vector<Integer> deserializeBytes(byte[] bytes) throws IOException, ClassNotFoundException
	{
	    ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
	    ObjectInputStream ois = new ObjectInputStream(bytesIn);
		@SuppressWarnings("unchecked")
		Vector<Integer> obj = (Vector<Integer>) ois.readObject();
	    ois.close();
	    return obj;
	}


	public static byte[] serializeObject(Vector<Integer> obj) throws IOException
	{
	    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream(bytesOut);
	    oos.writeObject(obj);
	    oos.flush();
	    byte[] bytes = bytesOut.toByteArray();
	    bytesOut.close();
	    oos.close();
	    return bytes;
	}

}

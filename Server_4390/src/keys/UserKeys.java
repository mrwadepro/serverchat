package keys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import serverChat.server.Client;

public class UserKeys
{
	public static final String DEFAULT_FILE_NAME = "KEYS.kys";
	
	public static Map<Client, String> keys;
	
	public static boolean loadKeys()
	{
		return loadKeys(DEFAULT_FILE_NAME);
	}
	
	public static boolean loadKeys(String fileName)
	{
		keys = new HashMap<Client, String>();
		
		FileInputStream in = null;
		try 
		{
			in = new FileInputStream(new File(fileName));
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return false;
		}
				
				
		
		try
		{
			in.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	public static String getKey(long id)
	{
		//TODO actually get a key
		return COMP_128.gen_rand_128Bit();
	}
}

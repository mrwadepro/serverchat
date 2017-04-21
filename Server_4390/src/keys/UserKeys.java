package keys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import serverChat.server.Client;

public class UserKeys
{
	public static final String DEFAULT_FILE_NAME = "KEYS.kys";
	
	public static Map<Long, String> keys;
	
	public static boolean loadKeys()
	{
		return loadKeys(DEFAULT_FILE_NAME);
	}
	
	public static boolean loadKeys(String fileName)
	{
		keys = new HashMap<Long, String>();		
		FileInputStream in = null;
		File f = new File(fileName);
		if(!f.exists())
		{
			try 
			{
				f.createNewFile();
				return true;
			} 
			catch (IOException e)
			{
				e.printStackTrace();
				return false;
			}
		}
		try 
		{
			in = new FileInputStream(f);
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return false;
		}
		
		byte nextUser[] = new byte[24];
		int read = 0;
		try
		{
			while(in.available()>0)
			{
				read = in.read(nextUser);
				
				if(read ==24)
				{
					ByteBuffer user = ByteBuffer.allocate(8).put(nextUser,0,8);
					user.position(0);
					long ID = user.getLong();
					String key = "";
					for(int x =8; x<nextUser.length; x++)
					{
						key+=(char)nextUser[x];
					}
					keys.put(ID, key);
				}
			}
		} 
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
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
	
	public static boolean saveKeys()
	{
		return saveKeys(DEFAULT_FILE_NAME);
	}
	public static boolean saveKeys(String fileName)
	{
		FileOutputStream out = null;
		File f=  new File(fileName);
		if(!f.exists())
		{
			try 
			{
				f.createNewFile();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		try 
		{
			out = new FileOutputStream(f);
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		for(long i : keys.keySet())
		{
			String key = keys.get(i);
			ByteBuffer output = ByteBuffer.allocate(24).putLong(i);
			char chars[] = key.toCharArray();
			for(int x =0; x< chars.length; x++)
				output.put((byte)chars[x]);
			try 
			{
				out.write(output.array());
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		try
		{
			out.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public static String getKey(long id)
	{
		return keys.get(id);
		
	}
	public static boolean addKey(long id, String key)
	{
		if(keys.containsKey(id))
			return false;
		else
			keys.put(id, key);
		return true;
	}
}

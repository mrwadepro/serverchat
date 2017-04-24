package keys;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public final class RSA 
{
	private PublicKey kPublic;
	private PrivateKey kPrivate;
	
	public RSA()
	{
		genKeys();
			
	}
	 
	public PublicKey getPublicKey()
	{
		return kPublic;
	}
	
	public boolean genKeys()
	{
		KeyPairGenerator kpg = null;
		try 
		{
			kpg = KeyPairGenerator.getInstance("RSA");
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
			return false;
		}
		kpg.initialize(1024);
		KeyPair kp = kpg.genKeyPair();
		kPublic = kp.getPublic();
		kPrivate = kp.getPrivate();		
		return true;
	}
	
	public byte[] encrypt(String s)
	{
		Cipher c = null;
		try 
		{
			c = Cipher.getInstance("RSA");
		} 
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			return null;
		} 
		catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
			return null;
		}
		
		try 
		{
			c.init(Cipher.ENCRYPT_MODE, kPublic);
		} 
		catch (InvalidKeyException e) 
		{
			e.printStackTrace();
			return null;
		}
		
		try 
		{
			return c.doFinal(s.getBytes());
		} 
		catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
			return null;
		} 
		catch (BadPaddingException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public byte[] decrypt(byte[] s)
	{
		Cipher c;
		try 
		{
			c = Cipher.getInstance("RSA");
		} 
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			return null;
		} 
		catch (NoSuchPaddingException e) 
		{
			e.printStackTrace();
			return null;
		}
		try
		{
			c.init(Cipher.DECRYPT_MODE, kPrivate);
		} 
		catch (InvalidKeyException e) 
		{
			e.printStackTrace();
			return null;
		}
		try 
		{
			return c.doFinal(s);
		}
		catch (IllegalBlockSizeException e) 
		{
			e.printStackTrace();
			return null;
		}
		catch (BadPaddingException e) 
		{
			e.printStackTrace();
			return null;
		}
	}
}

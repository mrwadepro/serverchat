package serverChat.server;

import java.net.Socket;

import javax.crypto.spec.SecretKeySpec;

public class Client
{
	public static final long TIMEOUT_MAX = 15000;
	
	public final long ID;
	public final SecretKeySpec CK_Key;
	
	private long curSession;
	private Socket socket;
	private long lastResponseTime;
	
	public Client(Socket s, long clientID, SecretKeySpec key)
	{
		socket = s;
		ID = clientID;
		CK_Key = key;
		curSession = -1;
		lastResponseTime = System.currentTimeMillis();
	}
	
	
	public long getCurSession()
	{
		return curSession;
	}
	public void startSession(long session)
	{
		curSession = session;
	}
	public void endSession()
	{
		curSession = -1;
	}
	public boolean inSession()
	{
		return curSession !=-1;
	}
	public Socket getSocket()
	{
		return socket;
	}
	
	public void setLastResponseTime()
	{
		lastResponseTime = System.currentTimeMillis();
	}
	public long timeSinceLastResponse()
	{
		return System.currentTimeMillis()-lastResponseTime;
	}
	
	
}

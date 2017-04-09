package serverChat.server;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;

public class Client
{
	private Socket socket;
	private long clientID;
	private String CK_Key;
	
	public Client(Socket s, long clientID, String key)
	{
		socket = s;
		this.clientID = clientID;
		CK_Key = key;
	}
	
	public Socket getSocket()
	{
		return socket;
	}
	
	public String getKey()
	{
		return CK_Key;
	}
	public long getID()
	{
		return clientID;
	}
	
	
}

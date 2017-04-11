package serverChat.server;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;

public class Client
{
	private Socket socket;
	private long clientID;
	private long CK_Key;
	
	public Client(Socket s, long clientID, long key)
	{
		socket = s;
		this.clientID = clientID;
		CK_Key = key;
	}
	
	public Socket getSocket()
	{
		return socket;
	}
	
	public long getKey()
	{
		return CK_Key;
	}
	public long getID()
	{
		return clientID;
	}
	
	
}

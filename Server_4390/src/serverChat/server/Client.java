package serverChat.server;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;

public class Client
{
	private Socket socket;
	int clientID;
	
	public Client(Socket s)
	{
	}
	
	public Socket getSocket()
	{
		return socket;
	}
	
	
}

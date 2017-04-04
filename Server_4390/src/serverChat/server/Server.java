package serverChat.server;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.*;

import keys.RSA;

public class Server
{
	public static final int DEFAULT_PORT = 4434;
	
	public static final byte HELLO = 1;
	public static final byte HELLO_LENGTH = 8;//length of message in bytes
	public static final byte CHALLENGE = 2;
	public static final byte CHALLENGE_LENGTH = 8;
	public static final byte RESPONSE = 3;
	public static final byte RESPONSE_LENGTH = 16;
	public static final byte AUTH_SUCCESS = 4;
	public static final byte AUTH_FAIL = 5;
	public static final byte CONNECT = 6;
	public static final byte CONNECTED = 7;
	public static final byte CHAT_REQUEST = 8;
	public static final byte CHAT_STARTED = 9;
	public static final byte UNREACHABLE = 10;
	public static final byte END_REQUEST = 11;
	public static final byte END_NOTIF = 12;
	public static final byte CHAT = 13;
	public static final byte HISTORY_REQ = 14;
	public static final byte HISTORY_RESP = 15;
	
	private Semaphore clientSem;
	
	private static ArrayList<Client> clients;
	
	private ListeningSocket listener;
	
	
	public Server()
	{
		this(DEFAULT_PORT);
	}
	public Server(int portNum)
	{
		clientSem = new Semaphore(1,true);
		try 
		{
			listener = new ListeningSocket(portNum);
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void addClient()
	{
		
	}
}
